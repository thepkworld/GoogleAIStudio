package com.example.data.repository

import com.example.data.catalog.CatalogRepository
import com.example.data.model.CatalogTitle
import com.example.data.model.DataSourceOrigin
import com.example.data.model.ExternalRatingsAndAwards
import com.example.data.model.MediaType
import com.example.data.model.SearchScope
import com.example.data.model.VaultEntry
import com.example.data.omdb.OmdbFallbackFeeds
import com.example.data.omdb.OmdbRepository
import com.example.data.tmdb.TmdbPageResult
import com.example.data.tmdb.TmdbRepository
import com.example.data.tmdb.TmdbSearchBundle
import kotlinx.coroutines.CancellationException

/**
 * Coordinates primary TMDB requests with automatic OMDb fallback routing and mandatory
 * OMDb Ratings (IMDb, Rotten Tomatoes, Metacritic) & Awards enrichment.
 */
class UnifiedMediaService(
    val tmdbRepository: TmdbRepository = TmdbRepository(),
    val omdbRepository: OmdbRepository = OmdbRepository()
) {
    fun hasTmdbKey(): Boolean = tmdbRepository.hasConfiguredApiKey()

    fun hasOmdbKey(): Boolean = omdbRepository.hasConfiguredApiKey()

    fun findCachedById(id: String): CatalogTitle? {
        return CatalogRepository.findById(id)
            ?: tmdbRepository.findCachedById(id)
            ?: omdbRepository.findCachedById(id)
    }

    /**
     * Fetches unified details for any title ID:
     * 1. If TMDB ID & TMDB is reachable -> fetch TMDB details, then ALWAYS enrich with OMDb Ratings & Awards
     *    matched via TMDB `imdb_id` (or exact Title + Release Year fallback).
     * 2. If TMDB is unreachable / key missing -> automatically fall back to OMDb full title lookup.
     * 3. If OMDb ID (`omdb_tt...`) -> fetch full details from OMDb directly.
     * 4. If curated local ID -> enrich curated metadata with live OMDb Ratings & Awards.
     */
    suspend fun fetchUnifiedTitleDetails(
        titleId: String,
        hintTitle: CatalogTitle? = null,
        hintEntry: VaultEntry? = null
    ): Result<CatalogTitle> {
        val parsedTmdb = TmdbRepository.parseTmdbId(titleId)
        val parsedOmdbImdb = OmdbRepository.parseOmdbId(titleId)

        if (parsedOmdbImdb != null) {
            val omdbRes = omdbRepository.fetchTitleDetail(
                imdbId = parsedOmdbImdb,
                title = hintTitle?.title ?: hintEntry?.title,
                releaseYear = hintTitle?.releaseYear ?: hintEntry?.releaseYear?.toInt(),
                mediaType = hintTitle?.mediaType ?: hintEntry?.mediaTypeEnum
            )
            return omdbRes.recoverCatching { err ->
                hintTitle ?: throw err
            }
        }

        if (parsedTmdb != null) {
            val tmdbResult = if (tmdbRepository.hasConfiguredApiKey()) {
                try {
                    tmdbRepository.fetchFullDetails(titleId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(IllegalStateException("TMDB API key not configured"))
            }

            tmdbResult.getOrNull()?.let { tmdbDetail ->
                // TMDB succeeded: ALWAYS enrich with OMDb Ratings & Awards
                val enriched = enrichWithOmdbRatingsAndAwards(tmdbDetail)
                return Result.success(enriched)
            }

            // TMDB failed/unreachable: automatically fall back to OMDb API
            val fallbackTitle = hintTitle?.title?.ifBlank { null } ?: hintEntry?.title?.ifBlank { null }
            val fallbackYear = hintTitle?.releaseYear ?: hintEntry?.releaseYear?.toInt()
            val fallbackType = hintTitle?.mediaType ?: hintEntry?.mediaTypeEnum ?: parsedTmdb.first
            val fallbackImdb = hintTitle?.imdbId?.ifBlank { null }

            if (omdbRepository.hasConfiguredApiKey() && (fallbackImdb != null || fallbackTitle != null)) {
                val omdbDetail = omdbRepository.fetchTitleDetail(
                    imdbId = fallbackImdb,
                    title = fallbackTitle,
                    releaseYear = fallbackYear,
                    mediaType = fallbackType
                )
                omdbDetail.getOrNull()?.let { omdbTitle ->
                    return Result.success(mergeHintWithOmdb(hintTitle, omdbTitle))
                }
            }

            return if (hintTitle != null) {
                Result.success(hintTitle)
            } else {
                Result.failure(
                    tmdbResult.exceptionOrNull()
                        ?: IllegalStateException("Unable to load title from TMDB or OMDb")
                )
            }
        }

        // Local curated / custom title -> enrich with OMDb Ratings & Awards if available
        val local = CatalogRepository.findById(titleId) ?: hintTitle
        if (local != null) {
            return Result.success(enrichWithOmdbRatingsAndAwards(local))
        }

        val entryTitle = hintEntry?.title?.takeIf { it.isNotBlank() }
        if (entryTitle != null && omdbRepository.hasConfiguredApiKey()) {
            return omdbRepository.fetchTitleDetail(
                title = entryTitle,
                releaseYear = hintEntry.releaseYear.toInt(),
                mediaType = hintEntry.mediaTypeEnum
            )
        }

        return Result.failure(IllegalStateException("Title unavailable across TMDB and OMDb"))
    }

    suspend fun enrichWithOmdbRatingsAndAwards(base: CatalogTitle): CatalogTitle {
        if (!omdbRepository.hasConfiguredApiKey()) return base
        val existingRatings = base.ratingsAndAwards
        if (existingRatings != null && existingRatings.hasRatings && existingRatings.hasAwards) {
            return base
        }

        val omdbRes = try {
            omdbRepository.fetchRatingsAndAwards(
                imdbId = base.imdbId.ifBlank { null },
                title = base.title,
                releaseYear = base.releaseYear,
                mediaType = base.mediaType,
                tmdbScore = base.criticScore
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return base
        }

        val ratings = omdbRes.getOrNull() ?: return base
        val upgradedAwards = if (ratings.hasAwards) {
            ratings.awards
        } else {
            base.awardsHighlight
        }
        val resolvedImdbId = base.imdbId.ifBlank { ratings.imdbId }

        return base.copy(
            imdbId = resolvedImdbId,
            ratingsAndAwards = ratings,
            awardsHighlight = upgradedAwards,
            dataSource = if (base.tmdbId != null) DataSourceOrigin.TMDB_WITH_OMDB else base.dataSource
        )
    }

    /**
     * Searches TMDB first; when TMDB is unavailable or fails, falls back cleanly to OMDb search.
     */
    suspend fun searchWithFallback(
        query: String,
        scope: SearchScope,
        page: Int = 1
    ): Result<Pair<TmdbSearchBundle, DataSourceOrigin>> {
        if (tmdbRepository.hasConfiguredApiKey()) {
            val tmdbResult = try {
                tmdbRepository.search(query, scope, page)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
            tmdbResult.getOrNull()?.let { bundle ->
                return Result.success(bundle to DataSourceOrigin.TMDB_WITH_OMDB)
            }
        }

        if (scope == SearchScope.PEOPLE || scope == SearchScope.COLLECTIONS) {
            return Result.failure(IllegalStateException("TMDB is unreachable for People/Collections search"))
        }

        if (omdbRepository.hasConfiguredApiKey()) {
            val typeFilter = when (scope) {
                SearchScope.MOVIES -> MediaType.MOVIE
                SearchScope.SERIES -> MediaType.SERIES
                else -> null
            }
            val omdbResult: Result<TmdbPageResult> = omdbRepository.searchTitles(
                query = query,
                mediaType = typeFilter,
                page = page
            )
            omdbResult.getOrNull()?.let { pageResult ->
                return Result.success(
                    TmdbSearchBundle(
                        titles = pageResult.items,
                        page = pageResult.page,
                        totalPages = pageResult.totalPages
                    ) to DataSourceOrigin.OMDB_FALLBACK
                )
            }
            return Result.failure(omdbResult.exceptionOrNull() ?: IllegalStateException("OMDb search failed"))
        }

        return Result.failure(IllegalStateException("Both TMDB and OMDb are unavailable"))
    }

    suspend fun fetchOmdbFallbackHomeFeeds(): Result<OmdbFallbackFeeds> {
        return omdbRepository.fetchFallbackHomeFeeds()
    }

    private fun mergeHintWithOmdb(hint: CatalogTitle?, omdb: CatalogTitle): CatalogTitle {
        if (hint == null) return omdb
        return omdb.copy(
            id = hint.id,
            tmdbId = hint.tmdbId,
            posterUrl = omdb.posterUrl.ifBlank { hint.posterUrl },
            backdropUrl = hint.backdropUrl.ifBlank { hint.backdropUrl }.ifBlank { hint.posterUrl },
            voteCount = maxOf(omdb.voteCount, hint.voteCount),
            criticScore = if (omdb.criticScore > 0.0) omdb.criticScore else hint.criticScore,
            popularity = hint.popularity,
            dataSource = DataSourceOrigin.OMDB_FALLBACK
        )
    }
}
