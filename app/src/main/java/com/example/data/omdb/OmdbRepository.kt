package com.example.data.omdb

import com.example.BuildConfig
import com.example.data.model.CastMember
import com.example.data.model.CatalogTitle
import com.example.data.model.DataSourceOrigin
import com.example.data.model.ExternalRatingsAndAwards
import com.example.data.model.MediaType
import com.example.data.tmdb.TmdbPageResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class OmdbFallbackFeeds(
    val featuredTitle: CatalogTitle?,
    val trendingNow: List<CatalogTitle> = emptyList(),
    val bollywoodMovies: List<CatalogTitle> = emptyList(),
    val hollywoodMovies: List<CatalogTitle> = emptyList(),
    val topRatedMovies: List<CatalogTitle> = emptyList(),
    val classicMovies: List<CatalogTitle> = emptyList()
)

class OmdbRepository(
    private val apiKeyProvider: () -> String = { BuildConfig.OMDB_API_KEY },
    private val serviceOverride: OmdbApiService? = null
) {
    private val detailCache = ConcurrentHashMap<String, CatalogTitle>()
    private val ratingsCache = ConcurrentHashMap<String, ExternalRatingsAndAwards>()

    private val apiService: OmdbApiService by lazy {
        serviceOverride ?: createRetrofitService(apiKeyProvider)
    }

    fun hasConfiguredApiKey(): Boolean {
        val key = apiKeyProvider().trim()
        return key.isNotEmpty() && key != PLACEHOLDER_KEY
    }

    fun findCachedById(id: String): CatalogTitle? = detailCache[id]

    suspend fun fetchRatingsAndAwards(
        imdbId: String? = null,
        title: String,
        releaseYear: Int? = null,
        mediaType: MediaType = MediaType.MOVIE,
        tmdbScore: Double = 0.0
    ): Result<ExternalRatingsAndAwards> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("OMDb API key not configured"))
        }

        val cacheKey = buildCacheKey(imdbId, title, releaseYear, mediaType)
        ratingsCache[cacheKey]?.let { cached ->
            return Result.success(cached.copy(tmdbScore = if (tmdbScore > 0.0) tmdbScore else cached.tmdbScore))
        }

        return try {
            val dto = fetchRawDto(
                imdbId = imdbId,
                title = title,
                releaseYear = releaseYear,
                mediaType = mediaType
            )
            if (dto.isSuccess()) {
                val mapped = mapRatingsAndAwards(dto, tmdbScore)
                ratingsCache[cacheKey] = mapped
                dto.imdbId?.takeIf { it.isNotBlank() }?.let { id ->
                    ratingsCache["imdb_$id"] = mapped
                }
                Result.success(mapped)
            } else {
                Result.failure(IllegalStateException(dto.error?.ifBlank { null } ?: "Title not found on OMDb"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchTitleDetail(
        imdbId: String? = null,
        title: String? = null,
        releaseYear: Int? = null,
        mediaType: MediaType? = null
    ): Result<CatalogTitle> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("OMDb API key not configured"))
        }
        val cleanImdbId = imdbId?.trim()?.takeIf { it.startsWith("tt", ignoreCase = true) }
        val cleanTitle = title?.trim().orEmpty()
        if (cleanImdbId == null && cleanTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Missing IMDb ID and title for OMDb lookup"))
        }

        return try {
            val dto = fetchRawDto(
                imdbId = cleanImdbId,
                title = cleanTitle,
                releaseYear = releaseYear,
                mediaType = mediaType
            )
            if (dto.isSuccess()) {
                val mapped = mapOmdbDetail(dto)
                detailCache[mapped.id] = mapped
                mapped.ratingsAndAwards?.let { r ->
                    if (mapped.imdbId.isNotBlank()) {
                        ratingsCache["imdb_${mapped.imdbId}"] = r
                    }
                }
                Result.success(mapped)
            } else {
                Result.failure(IllegalStateException(dto.error?.ifBlank { null } ?: "OMDb title lookup failed"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchTitles(
        query: String,
        mediaType: MediaType? = null,
        page: Int = 1
    ): Result<TmdbPageResult> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("OMDb API key not configured"))
        }
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return Result.success(TmdbPageResult(emptyList(), page = 1, totalPages = 1))
        }
        return try {
            val typeParam = when (mediaType) {
                MediaType.MOVIE -> "movie"
                MediaType.SERIES -> "series"
                null -> null
            }
            val response = apiService.search(
                query = cleanQuery,
                type = typeParam,
                page = page.coerceAtLeast(1)
            )
            if (!response.response.equals("True", ignoreCase = true)) {
                val err = response.error.orEmpty()
                if (err.contains("not found", ignoreCase = true)) {
                    return Result.success(TmdbPageResult(emptyList(), page = page, totalPages = 1))
                }
                return Result.failure(IllegalStateException(err.ifBlank { "OMDb search failed" }))
            }
            val totalResults = response.totalResults?.toIntOrNull() ?: response.search.size
            val totalPages = ((totalResults + 9) / 10).coerceAtLeast(1)
            val items = response.search
                .filter { !it.imdbId.isNullOrBlank() && !it.title.isNullOrBlank() }
                .map { dto ->
                    mapSearchItem(dto).also { detailCache[it.id] = it }
                }
            Result.success(
                TmdbPageResult(
                    items = items,
                    page = page,
                    totalPages = totalPages
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFallbackHomeFeeds(): Result<OmdbFallbackFeeds> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("OMDb API key not configured"))
        }
        return try {
            coroutineScope {
                val trendingDeferred = async { searchTitles("Dune", MediaType.MOVIE, 1) }
                val bollywoodDeferred = async { searchTitles("Pathaan", MediaType.MOVIE, 1) }
                val hollywoodDeferred = async { searchTitles("Oppenheimer", MediaType.MOVIE, 1) }
                val topRatedDeferred = async { searchTitles("Batman", MediaType.MOVIE, 1) }
                val classicDeferred = async { searchTitles("Godfather", MediaType.MOVIE, 1) }

                val trending = trendingDeferred.await().getOrNull()?.items.orEmpty()
                val bollywood = bollywoodDeferred.await().getOrNull()?.items.orEmpty()
                val hollywood = hollywoodDeferred.await().getOrNull()?.items.orEmpty()
                val topRated = topRatedDeferred.await().getOrNull()?.items.orEmpty()
                val classic = classicDeferred.await().getOrNull()?.items.orEmpty()

                val combined = (trending + hollywood + topRated).distinctBy { it.id }
                if (combined.isEmpty() && bollywood.isEmpty() && classic.isEmpty()) {
                    Result.failure(IllegalStateException("OMDb fallback feeds returned no items"))
                } else {
                    val featured = trending.firstOrNull()?.imdbId?.takeIf { it.isNotBlank() }?.let { imdb ->
                        fetchTitleDetail(imdbId = imdb).getOrNull()
                    } ?: combined.firstOrNull()
                    Result.success(
                        OmdbFallbackFeeds(
                            featuredTitle = featured,
                            trendingNow = combined,
                            bollywoodMovies = bollywood,
                            hollywoodMovies = hollywood,
                            topRatedMovies = topRated,
                            classicMovies = classic
                        )
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchRawDto(
        imdbId: String?,
        title: String,
        releaseYear: Int?,
        mediaType: MediaType?
    ): OmdbTitleDetailDto {
        val cleanId = imdbId?.trim()?.takeIf { it.startsWith("tt", ignoreCase = true) }
        if (cleanId != null) {
            val byId = apiService.getByImdbId(cleanId)
            if (byId.isSuccess() || title.isBlank()) {
                return byId
            }
        }

        val typeParam = when (mediaType) {
            MediaType.SERIES -> "series"
            MediaType.MOVIE -> "movie"
            null -> null
        }

        val cleanYear = releaseYear?.takeIf { it in 1880..2100 }
        if (cleanYear != null) {
            val exact = apiService.getByTitle(title = title, year = cleanYear, type = typeParam)
            if (exact.isSuccess()) return exact
        }

        val byTitleAndType = apiService.getByTitle(title = title, year = null, type = typeParam)
        if (byTitleAndType.isSuccess() || typeParam == null) return byTitleAndType

        return apiService.getByTitle(title = title, year = null, type = null)
    }

    internal fun mapRatingsAndAwards(
        dto: OmdbTitleDetailDto,
        tmdbScore: Double = 0.0
    ): ExternalRatingsAndAwards {
        var imdbFromRatings = ""
        var rtFromRatings = ""
        var mcFromRatings = ""

        dto.ratings.forEach { r ->
            val src = r.source.orEmpty().trim()
            val value = cleanNa(r.value)
            if (value.isNotEmpty()) {
                when {
                    src.contains("Internet Movie Database", ignoreCase = true) -> imdbFromRatings = value
                    src.contains("Rotten Tomatoes", ignoreCase = true) -> rtFromRatings = value
                    src.contains("Metacritic", ignoreCase = true) -> mcFromRatings = value
                }
            }
        }

        val directImdb = cleanNa(dto.imdbRating)
        val finalImdb = when {
            imdbFromRatings.isNotEmpty() -> imdbFromRatings
            directImdb.isNotEmpty() -> "$directImdb/10"
            else -> ""
        }

        val directMeta = cleanNa(dto.metascore)
        val finalMeta = when {
            mcFromRatings.isNotEmpty() -> mcFromRatings
            directMeta.isNotEmpty() -> "$directMeta/100"
            else -> ""
        }

        return ExternalRatingsAndAwards(
            imdbId = cleanNa(dto.imdbId),
            imdbRating = finalImdb,
            imdbVotes = cleanNa(dto.imdbVotes),
            rottenTomatoes = rtFromRatings,
            metacritic = finalMeta,
            awards = cleanNa(dto.awards),
            tmdbScore = tmdbScore
        )
    }

    internal fun mapOmdbDetail(dto: OmdbTitleDetailDto): CatalogTitle {
        val type = if (dto.type.equals("series", ignoreCase = true)) MediaType.SERIES else MediaType.MOVIE
        val imdbId = cleanNa(dto.imdbId)
        val id = if (imdbId.isNotEmpty()) buildOmdbId(imdbId) else "omdb_${cleanNa(dto.title).lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_")}"
        val genres = cleanNa(dto.genre)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(type.label) }
        val year = cleanNa(dto.year).take(4).toIntOrNull() ?: 2024
        val isoDate = parseOmdbReleaseDate(cleanNa(dto.released))
        val imdbScore = cleanNa(dto.imdbRating).toDoubleOrNull() ?: 0.0
        val voteCount = cleanNa(dto.imdbVotes).replace(",", "").toIntOrNull() ?: 0
        val castList = cleanNa(dto.actors)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { actor -> CastMember(actor = actor, role = "Cast") }
        val director = cleanNa(dto.director).ifBlank {
            cleanNa(dto.writer).ifBlank { if (type == MediaType.SERIES) "Series Creator" else "Director N/A" }
        }
        val runtimeText = if (type == MediaType.SERIES) {
            val seasons = cleanNa(dto.totalSeasons).toIntOrNull()
            when {
                seasons != null && seasons > 1 -> "$seasons Seasons"
                seasons == 1 -> "1 Season"
                else -> formatOmdbRuntime(cleanNa(dto.runtime))
            }
        } else {
            formatOmdbRuntime(cleanNa(dto.runtime))
        }
        val poster = sanitizePosterUrl(dto.poster)
        val awards = cleanNa(dto.awards)
        val ratings = mapRatingsAndAwards(dto)

        return CatalogTitle(
            id = id,
            title = cleanNa(dto.title).ifBlank { "Untitled" },
            tagline = "",
            mediaType = type,
            releaseYear = year,
            genres = genres,
            directorOrCreator = director,
            studioOrNetwork = "OMDb Catalogue",
            country = cleanNa(dto.country),
            language = cleanNa(dto.language).ifBlank { "English" },
            ageRating = cleanNa(dto.rated),
            runtimeOrSeasons = runtimeText.ifBlank { year.toString() },
            totalEpisodes = 0,
            criticScore = imdbScore,
            synopsis = cleanNa(dto.plot).ifBlank { "Synopsis unavailable." },
            posterKey = posterKeyForGenres(genres),
            cast = castList,
            awardsHighlight = awards,
            posterUrl = poster,
            backdropUrl = poster,
            voteCount = voteCount,
            releaseDate = isoDate,
            imdbId = imdbId,
            ratingsAndAwards = ratings,
            dataSource = DataSourceOrigin.OMDB_FALLBACK
        )
    }

    internal fun mapSearchItem(dto: OmdbSearchItemDto): CatalogTitle {
        val imdbId = cleanNa(dto.imdbId)
        val type = if (dto.type.equals("series", ignoreCase = true)) MediaType.SERIES else MediaType.MOVIE
        val year = cleanNa(dto.year).take(4).toIntOrNull() ?: 2024
        val poster = sanitizePosterUrl(dto.poster)
        return CatalogTitle(
            id = buildOmdbId(imdbId),
            title = cleanNa(dto.title).ifBlank { "Untitled" },
            tagline = "",
            mediaType = type,
            releaseYear = year,
            genres = listOf(type.label),
            directorOrCreator = "OMDb Catalogue",
            studioOrNetwork = "OMDb",
            country = "",
            language = "English",
            ageRating = "",
            runtimeOrSeasons = year.toString(),
            totalEpisodes = 0,
            criticScore = 0.0,
            synopsis = "Tap to load full plot, IMDb/Rotten Tomatoes/Metacritic ratings, and awards via OMDb.",
            posterKey = "cinema",
            posterUrl = poster,
            backdropUrl = poster,
            releaseDate = "$year-01-01",
            imdbId = imdbId,
            dataSource = DataSourceOrigin.OMDB_FALLBACK
        )
    }

    private fun OmdbTitleDetailDto.isSuccess(): Boolean =
        response.equals("True", ignoreCase = true) && !title.isNullOrBlank()

    companion object {
        private const val BASE_URL = "https://www.omdbapi.com/"
        private const val PLACEHOLDER_KEY = "YOUR_OMDB_API_KEY"
        private const val OMDB_ID_PREFIX = "omdb_"

        fun buildOmdbId(imdbId: String): String = "$OMDB_ID_PREFIX$imdbId"

        fun parseOmdbId(id: String): String? =
            if (id.startsWith(OMDB_ID_PREFIX)) id.removePrefix(OMDB_ID_PREFIX).takeIf { it.isNotBlank() } else null

        internal fun cleanNa(raw: String?): String {
            val trimmed = raw?.trim().orEmpty()
            return if (trimmed.equals("N/A", ignoreCase = true)) "" else trimmed
        }

        internal fun sanitizePosterUrl(raw: String?): String {
            val clean = cleanNa(raw)
            if (clean.isEmpty()) return ""
            return if (clean.startsWith("http://", ignoreCase = true)) {
                "https://" + clean.removePrefix("http://")
            } else {
                clean
            }
        }

        internal fun formatOmdbRuntime(rawRuntime: String): String {
            if (rawRuntime.isBlank()) return ""
            val mins = Regex("(\\d+)").find(rawRuntime)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: return rawRuntime
            val h = mins / 60
            val m = mins % 60
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }

        internal fun parseOmdbReleaseDate(rawReleased: String): String {
            if (rawReleased.isBlank()) return ""
            return runCatching {
                val parsed = SimpleDateFormat("dd MMM yyyy", Locale.US).apply {
                    isLenient = false
                }.parse(rawReleased) ?: return ""
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed)
            }.getOrDefault("")
        }

        private fun buildCacheKey(
            imdbId: String?,
            title: String,
            year: Int?,
            mediaType: MediaType
        ): String {
            val cleanId = imdbId?.trim()?.takeIf { it.startsWith("tt", ignoreCase = true) }
            if (cleanId != null) return "imdb_$cleanId"
            return "${mediaType.value}_${title.trim().lowercase(Locale.US)}_${year ?: 0}"
        }

        private fun posterKeyForGenres(genres: List<String>): String {
            val lower = genres.joinToString(" ").lowercase(Locale.US)
            return when {
                lower.contains("sci") || lower.contains("fantasy") || lower.contains("animation") -> "scifi"
                lower.contains("thriller") || lower.contains("crime") || lower.contains("mystery") || lower.contains("horror") -> "noir"
                lower.contains("drama") || lower.contains("romance") || lower.contains("history") -> "drama"
                else -> "cinema"
            }
        }

        private fun createRetrofitService(apiKeyProvider: () -> String): OmdbApiService {
            val apiKeyInterceptor = Interceptor { chain ->
                val original = chain.request()
                val key = apiKeyProvider().trim()
                val url = original.url.newBuilder()
                    .apply {
                        if (key.isNotEmpty() && key != PLACEHOLDER_KEY) {
                            addQueryParameter("apikey", key)
                        }
                    }
                    .build()
                val request = original.newBuilder()
                    .header("Accept", "application/json")
                    .url(url)
                    .build()
                chain.proceed(request)
            }

            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(logging)
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OmdbApiService::class.java)
        }
    }
}
