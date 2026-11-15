package com.example.data.repository

import android.content.Context
import com.example.data.local.VaultDao
import com.example.data.local.VaultDatabase
import com.example.data.local.VaultEntryEntity
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class VaultRepository(
    private val dao: VaultDao
) {
    constructor(context: Context) : this(
        dao = VaultDatabase.getInstance(context.applicationContext).vaultDao()
    )

    fun observeVaultEntries(): Flow<List<VaultEntry>> =
        dao.observeAllEntries().map { entities -> entities.map { it.toDomain() } }

    suspend fun getVaultEntry(entryId: String): Result<VaultEntry?> {
        return try {
            Result.success(dao.getEntryById(entryId)?.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertFromCatalog(
        catalogTitle: CatalogTitle,
        targetStatus: WatchStatus,
        isPriority: Boolean = false,
        userRating: Double = 0.0,
        reviewTitle: String = "",
        reviewBody: String = "",
        favoriteQuote: String = "",
        containsSpoilers: Boolean = false,
        watchedAt: Long? = null
    ): Result<String> {
        return try {
            val now = System.currentTimeMillis()
            val resolvedWatchedAt = when {
                watchedAt != null && watchedAt > 0L -> watchedAt
                targetStatus == WatchStatus.WATCHED -> now
                else -> 0L
            }
            val existing = dao.getEntryById(catalogTitle.id)
            if (existing == null) {
                val episodesWatched = if (targetStatus == WatchStatus.WATCHED && catalogTitle.mediaType == MediaType.SERIES) {
                    catalogTitle.totalEpisodes.toLong()
                } else {
                    0L
                }
                val studioText = buildString {
                    if (catalogTitle.studioOrNetwork.isNotBlank()) {
                        append(catalogTitle.studioOrNetwork)
                    } else if (catalogTitle.watchProviders.isNotEmpty()) {
                        append(catalogTitle.watchProviders.first().name)
                    }
                }.take(120)
                val entity = VaultEntryEntity(
                    id = catalogTitle.id,
                    catalogId = catalogTitle.id,
                    tmdbId = catalogTitle.tmdbId,
                    title = catalogTitle.title.take(160),
                    mediaType = catalogTitle.mediaType.value,
                    status = targetStatus.value,
                    priority = isPriority && targetStatus == WatchStatus.WANT_TO_WATCH,
                    releaseYear = catalogTitle.releaseYear.toLong(),
                    genres = catalogTitle.genres.take(10),
                    directorOrCreator = catalogTitle.directorOrCreator.take(120),
                    runtimeOrSeasons = catalogTitle.runtimeOrSeasons.take(60),
                    synopsis = catalogTitle.synopsis.take(1500),
                    posterKey = catalogTitle.posterKey.take(40),
                    posterUrl = catalogTitle.posterUrl,
                    backdropUrl = catalogTitle.backdropUrl,
                    userRating = userRating.coerceIn(0.0, 10.0),
                    reviewTitle = reviewTitle.trim().take(160),
                    reviewBody = reviewBody.trim().take(4000),
                    favoriteQuote = favoriteQuote.trim().take(400),
                    containsSpoilers = containsSpoilers,
                    rewatchCount = 0L,
                    episodesWatched = episodesWatched,
                    totalEpisodes = catalogTitle.totalEpisodes.toLong(),
                    customTitle = false,
                    releaseDate = catalogTitle.releaseDate.take(30),
                    language = catalogTitle.language.take(60),
                    studioOrNetwork = studioText,
                    watchedAt = resolvedWatchedAt,
                    createdAt = now,
                    updatedAt = now
                )
                dao.upsertEntry(entity)
            } else {
                val nextWatchedAt = when {
                    watchedAt != null && watchedAt > 0L -> watchedAt
                    targetStatus == WatchStatus.WATCHED && existing.watchedAt <= 0L -> now
                    else -> existing.watchedAt
                }
                val nextStudio = existing.studioOrNetwork.ifBlank {
                    catalogTitle.studioOrNetwork.ifBlank {
                        catalogTitle.watchProviders.firstOrNull()?.name.orEmpty()
                    }.take(120)
                }
                val updated = existing.copy(
                    status = targetStatus.value,
                    priority = if (targetStatus == WatchStatus.WATCHED) false else isPriority,
                    tmdbId = existing.tmdbId ?: catalogTitle.tmdbId,
                    posterUrl = existing.posterUrl.ifBlank { catalogTitle.posterUrl },
                    backdropUrl = existing.backdropUrl.ifBlank { catalogTitle.backdropUrl },
                    directorOrCreator = existing.directorOrCreator.ifBlank { catalogTitle.directorOrCreator.take(120) },
                    runtimeOrSeasons = existing.runtimeOrSeasons.ifBlank { catalogTitle.runtimeOrSeasons.take(60) },
                    releaseDate = existing.releaseDate.ifBlank { catalogTitle.releaseDate.take(30) },
                    language = existing.language.ifBlank { catalogTitle.language.take(60) },
                    studioOrNetwork = nextStudio,
                    totalEpisodes = maxOf(existing.totalEpisodes, catalogTitle.totalEpisodes.toLong()),
                    userRating = if (userRating > 0.0) userRating.coerceIn(0.0, 10.0) else existing.userRating,
                    reviewTitle = if (reviewTitle.isNotBlank()) reviewTitle.trim().take(160) else existing.reviewTitle,
                    reviewBody = if (reviewBody.isNotBlank()) reviewBody.trim().take(4000) else existing.reviewBody,
                    favoriteQuote = if (favoriteQuote.isNotBlank()) favoriteQuote.trim().take(400) else existing.favoriteQuote,
                    watchedAt = nextWatchedAt,
                    updatedAt = now
                )
                dao.upsertEntry(updated)
            }
            Result.success(catalogTitle.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCustomTitle(
        title: String,
        mediaType: MediaType,
        status: WatchStatus,
        releaseYear: Int,
        genres: List<String>,
        directorOrCreator: String,
        runtimeOrSeasons: String,
        synopsis: String,
        posterKey: String,
        totalEpisodes: Int,
        userRating: Double = 0.0,
        reviewTitle: String = "",
        reviewBody: String = "",
        favoriteQuote: String = "",
        containsSpoilers: Boolean = false,
        isPriority: Boolean = false,
        watchedAt: Long? = null
    ): Result<String> {
        return try {
            val now = System.currentTimeMillis()
            val entryId = "custom_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val clampedTotalEpisodes = totalEpisodes.coerceIn(0, 5000).toLong()
            val episodesWatched = if (status == WatchStatus.WATCHED && mediaType == MediaType.SERIES) {
                clampedTotalEpisodes
            } else {
                0L
            }
            val resolvedWatchedAt = when {
                watchedAt != null && watchedAt > 0L -> watchedAt
                status == WatchStatus.WATCHED -> now
                else -> 0L
            }
            val entity = VaultEntryEntity(
                id = entryId,
                catalogId = entryId,
                tmdbId = null,
                title = title.trim().take(160),
                mediaType = mediaType.value,
                status = status.value,
                priority = isPriority && status == WatchStatus.WANT_TO_WATCH,
                releaseYear = releaseYear.coerceIn(1888, 2100).toLong(),
                genres = genres.map { it.trim() }.filter { it.isNotEmpty() }.take(10),
                directorOrCreator = directorOrCreator.trim().take(120),
                runtimeOrSeasons = runtimeOrSeasons.trim().take(60),
                synopsis = synopsis.trim().take(1500),
                posterKey = posterKey.ifBlank { "cinema" }.take(40),
                posterUrl = "",
                backdropUrl = "",
                userRating = userRating.coerceIn(0.0, 10.0),
                reviewTitle = reviewTitle.trim().take(160),
                reviewBody = reviewBody.trim().take(4000),
                favoriteQuote = favoriteQuote.trim().take(400),
                containsSpoilers = containsSpoilers,
                rewatchCount = 0L,
                episodesWatched = episodesWatched,
                totalEpisodes = clampedTotalEpisodes,
                customTitle = true,
                watchedAt = resolvedWatchedAt,
                createdAt = now,
                updatedAt = now
            )
            dao.upsertEntry(entity)
            Result.success(entryId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStatusAndProgress(
        entryId: String,
        status: WatchStatus,
        isPriority: Boolean,
        episodesWatched: Long,
        rewatchCount: Long,
        watchedAt: Long? = null
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val existing = dao.getEntryById(entryId)
                ?: return Result.failure(IllegalStateException("Entry not found in Vault"))
            val nextWatchedAt = when {
                watchedAt != null && watchedAt > 0L -> watchedAt
                status == WatchStatus.WATCHED && existing.watchedAt <= 0L -> now
                else -> existing.watchedAt
            }
            val updated = existing.copy(
                status = status.value,
                priority = if (status == WatchStatus.WATCHED) false else isPriority,
                episodesWatched = episodesWatched.coerceIn(0L, 5000L),
                rewatchCount = rewatchCount.coerceIn(0L, 1000L),
                watchedAt = nextWatchedAt,
                updatedAt = now
            )
            dao.upsertEntry(updated)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveReviewAndRating(
        catalogTitle: CatalogTitle?,
        existingEntry: VaultEntry?,
        entryId: String,
        status: WatchStatus,
        userRating: Double,
        reviewTitle: String,
        reviewBody: String,
        favoriteQuote: String,
        containsSpoilers: Boolean,
        watchedAt: Long? = null
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val currentDb = dao.getEntryById(entryId)
            if (currentDb == null && existingEntry == null && catalogTitle != null) {
                val episodesWatched = if (status == WatchStatus.WATCHED && catalogTitle.mediaType == MediaType.SERIES) {
                    catalogTitle.totalEpisodes.toLong()
                } else {
                    0L
                }
                val resolvedWatchedAt = when {
                    watchedAt != null && watchedAt > 0L -> watchedAt
                    status == WatchStatus.WATCHED -> now
                    else -> 0L
                }
                val entity = VaultEntryEntity(
                    id = catalogTitle.id,
                    catalogId = catalogTitle.id,
                    tmdbId = catalogTitle.tmdbId,
                    title = catalogTitle.title.take(160),
                    mediaType = catalogTitle.mediaType.value,
                    status = status.value,
                    priority = false,
                    releaseYear = catalogTitle.releaseYear.toLong(),
                    genres = catalogTitle.genres.take(10),
                    directorOrCreator = catalogTitle.directorOrCreator.take(120),
                    runtimeOrSeasons = catalogTitle.runtimeOrSeasons.take(60),
                    synopsis = catalogTitle.synopsis.take(1500),
                    posterKey = catalogTitle.posterKey.take(40),
                    posterUrl = catalogTitle.posterUrl,
                    backdropUrl = catalogTitle.backdropUrl,
                    userRating = userRating.coerceIn(0.0, 10.0),
                    reviewTitle = reviewTitle.trim().take(160),
                    reviewBody = reviewBody.trim().take(4000),
                    favoriteQuote = favoriteQuote.trim().take(400),
                    containsSpoilers = containsSpoilers,
                    rewatchCount = 0L,
                    episodesWatched = episodesWatched,
                    totalEpisodes = catalogTitle.totalEpisodes.toLong(),
                    customTitle = false,
                    releaseDate = catalogTitle.releaseDate.take(30),
                    language = catalogTitle.language.take(60),
                    studioOrNetwork = catalogTitle.studioOrNetwork.ifBlank {
                        catalogTitle.watchProviders.firstOrNull()?.name.orEmpty()
                    }.take(120),
                    watchedAt = resolvedWatchedAt,
                    createdAt = now,
                    updatedAt = now
                )
                dao.upsertEntry(entity)
            } else {
                val base = currentDb ?: existingEntry?.let { VaultEntryEntity.fromDomain(it) }
                    ?: return Result.failure(IllegalStateException("Unable to save review for missing title"))
                val nextWatchedAt = when {
                    watchedAt != null && watchedAt > 0L -> watchedAt
                    status == WatchStatus.WATCHED && base.watchedAt <= 0L -> now
                    else -> base.watchedAt
                }
                val nextEpisodesWatched = if (
                    status == WatchStatus.WATCHED &&
                    base.mediaType == MediaType.SERIES.value &&
                    base.totalEpisodes > 0L &&
                    base.episodesWatched == 0L
                ) {
                    base.totalEpisodes
                } else {
                    base.episodesWatched
                }
                val updated = base.copy(
                    status = status.value,
                    priority = if (status == WatchStatus.WATCHED) false else base.priority,
                    episodesWatched = nextEpisodesWatched,
                    userRating = userRating.coerceIn(0.0, 10.0),
                    reviewTitle = reviewTitle.trim().take(160),
                    reviewBody = reviewBody.trim().take(4000),
                    favoriteQuote = favoriteQuote.trim().take(400),
                    containsSpoilers = containsSpoilers,
                    tmdbId = base.tmdbId ?: catalogTitle?.tmdbId,
                    posterUrl = base.posterUrl.ifBlank { catalogTitle?.posterUrl.orEmpty() },
                    backdropUrl = base.backdropUrl.ifBlank { catalogTitle?.backdropUrl.orEmpty() },
                    releaseDate = base.releaseDate.ifBlank { catalogTitle?.releaseDate.orEmpty().take(30) },
                    language = base.language.ifBlank { catalogTitle?.language.orEmpty().take(60) },
                    studioOrNetwork = base.studioOrNetwork.ifBlank {
                        catalogTitle?.studioOrNetwork?.ifBlank {
                            catalogTitle.watchProviders.firstOrNull()?.name.orEmpty()
                        }.orEmpty().take(120)
                    },
                    watchedAt = nextWatchedAt,
                    updatedAt = now
                )
                dao.upsertEntry(updated)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEntry(entryId: String): Result<Unit> {
        return try {
            dao.deleteEntryById(entryId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
