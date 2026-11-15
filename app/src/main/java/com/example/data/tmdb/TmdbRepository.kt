package com.example.data.tmdb

import com.example.BuildConfig
import com.example.data.model.CastMember
import com.example.data.model.CatalogTitle
import com.example.data.model.CollectionDetail
import com.example.data.model.CollectionSummary
import com.example.data.model.CrewMember
import com.example.data.model.DiscoverSort
import com.example.data.model.EpisodeItem
import com.example.data.model.MediaType
import com.example.data.model.OttPlatform
import com.example.data.model.PersonDetail
import com.example.data.model.PersonSummary
import com.example.data.model.SearchScope
import com.example.data.model.SeasonDetail
import com.example.data.model.SeasonSummary
import com.example.data.model.TmdbGenre
import com.example.data.model.TmdbReviewItem
import com.example.data.model.TmdbSection
import com.example.data.model.TmdbVideoItem
import com.example.data.model.WatchProviderItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class TmdbPageResult(
    val items: List<CatalogTitle>,
    val page: Int,
    val totalPages: Int
)

data class TmdbSearchBundle(
    val titles: List<CatalogTitle> = emptyList(),
    val people: List<PersonSummary> = emptyList(),
    val collections: List<CollectionSummary> = emptyList(),
    val page: Int = 1,
    val totalPages: Int = 1
)

class TmdbRepository(
    private val apiKeyProvider: () -> String = { BuildConfig.TMDB_API_KEY },
    private val serviceOverride: TmdbApiService? = null
) {
    private val titleCache = ConcurrentHashMap<String, CatalogTitle>()
    private val movieGenreMap = ConcurrentHashMap<Int, String>()
    private val tvGenreMap = ConcurrentHashMap<Int, String>()

    private val apiService: TmdbApiService by lazy {
        serviceOverride ?: createRetrofitService(apiKeyProvider)
    }

    fun hasConfiguredApiKey(): Boolean {
        val raw = normalizeKey(apiKeyProvider())
        return raw.isNotEmpty() && raw != PLACEHOLDER_KEY
    }

    fun findCachedById(id: String): CatalogTitle? = titleCache[id]

    suspend fun fetchGenres(): Result<Pair<List<TmdbGenre>, List<TmdbGenre>>> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val movieRes = apiService.getMovieGenres()
            val tvRes = apiService.getTvGenres()
            val movies = movieRes.genres.map { dto ->
                movieGenreMap[dto.id] = dto.name
                TmdbGenre(dto.id, dto.name, MediaType.MOVIE)
            }
            val tv = tvRes.genres.map { dto ->
                tvGenreMap[dto.id] = dto.name
                TmdbGenre(dto.id, dto.name, MediaType.SERIES)
            }
            Result.success(movies to tv)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchTrendingPeople(): Result<List<PersonSummary>> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val response = apiService.getTrendingPeople(timeWindow = "week", page = 1)
            Result.success(response.results.map { mapPersonSummary(it) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchSection(
        section: TmdbSection,
        page: Int = 1
    ): Result<TmdbPageResult> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val response = when (section) {
                TmdbSection.TRENDING_ALL -> apiService.getTrending("all", "day", page)
                TmdbSection.TRENDING_MOVIES -> apiService.getTrending("movie", "week", page)
                TmdbSection.TRENDING_TV -> apiService.getTrending("tv", "week", page)
                TmdbSection.MOVIE_POPULAR -> apiService.getPopularMovies(page)
                TmdbSection.MOVIE_TOP_RATED -> apiService.getTopRatedMovies(page)
                TmdbSection.MOVIE_NOW_PLAYING -> apiService.getNowPlayingMovies(page)
                TmdbSection.MOVIE_UPCOMING -> apiService.getUpcomingMovies(page)
                TmdbSection.TV_POPULAR -> apiService.getPopularTv(page)
                TmdbSection.TV_TOP_RATED -> apiService.getTopRatedTv(page)
                TmdbSection.TV_ON_THE_AIR -> apiService.getOnTheAirTv(page)
                TmdbSection.TV_AIRING_TODAY -> apiService.getAiringTodayTv(page)
            }
            val mapped = response.results
                .filter { it.mediaType == null || it.mediaType == "movie" || it.mediaType == "tv" }
                .map { dto ->
                    val fallbackType = section.mediaHint ?: MediaType.MOVIE
                    mapMediaDto(dto, fallbackType).also { cacheTitle(it) }
                }
            Result.success(
                TmdbPageResult(
                    items = mapped,
                    page = response.page,
                    totalPages = response.totalPages
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun discover(
        mediaType: MediaType,
        genreId: Int?,
        sort: DiscoverSort = DiscoverSort.POPULARITY,
        year: Int? = null,
        page: Int = 1
    ): Result<TmdbPageResult> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val genreParam = genreId?.toString()
            val minVotes = if (sort == DiscoverSort.TOP_RATED) 150 else null
            val response = if (mediaType == MediaType.SERIES) {
                apiService.discoverTv(
                    page = page,
                    withGenres = genreParam,
                    sortBy = sort.tvParam,
                    minVotes = minVotes,
                    year = year
                )
            } else {
                apiService.discoverMovies(
                    page = page,
                    withGenres = genreParam,
                    sortBy = sort.movieParam,
                    minVotes = minVotes,
                    year = year
                )
            }
            val mapped = response.results.map { dto ->
                mapMediaDto(dto, mediaType).also { cacheTitle(it) }
            }
            Result.success(TmdbPageResult(mapped, response.page, response.totalPages))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun dateDaysOffsetIso(daysOffset: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysOffset) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    suspend fun fetchBollywoodMovies(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            val todayIso = dateDaysOffsetIso(0)
            val twoYearsAgoIso = dateDaysOffsetIso(-730)
            apiService.discoverMovies(
                page = page,
                sortBy = "primary_release_date.desc",
                minVotes = 10,
                withOriginalLanguage = "hi",
                withOriginCountry = "IN",
                releaseDateGte = twoYearsAgoIso,
                releaseDateLte = todayIso
            )
        }

    suspend fun fetchHollywoodMovies(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            val todayIso = dateDaysOffsetIso(0)
            val eighteenMonthsAgoIso = dateDaysOffsetIso(-540)
            apiService.discoverMovies(
                page = page,
                sortBy = "popularity.desc",
                minVotes = 60,
                withOriginalLanguage = "en",
                withOriginCountry = "US",
                releaseDateGte = eighteenMonthsAgoIso,
                releaseDateLte = todayIso
            )
        }

    suspend fun fetchUpcomingMovies(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            val tomorrowIso = dateDaysOffsetIso(1)
            apiService.discoverMovies(
                page = page,
                sortBy = "primary_release_date.asc",
                releaseDateGte = tomorrowIso
            )
        }

    suspend fun fetchMostAnticipatedMovies(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            val tomorrowIso = dateDaysOffsetIso(1)
            apiService.discoverMovies(
                page = page,
                sortBy = "popularity.desc",
                releaseDateGte = tomorrowIso
            )
        }

    suspend fun fetchPopularOnNetflix(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            val todayIso = dateDaysOffsetIso(0)
            apiService.discoverMovies(
                page = page,
                sortBy = "popularity.desc",
                minVotes = 50,
                releaseDateLte = todayIso,
                withWatchProviders = OttPlatform.NETFLIX.providerIds,
                watchRegion = OttPlatform.NETFLIX.region
            )
        }

    suspend fun fetchPopularOnPrime(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            val todayIso = dateDaysOffsetIso(0)
            apiService.discoverMovies(
                page = page,
                sortBy = "popularity.desc",
                minVotes = 50,
                releaseDateLte = todayIso,
                withWatchProviders = OttPlatform.PRIME.providerIds,
                watchRegion = OttPlatform.PRIME.region
            )
        }

    suspend fun fetchOttNewMovies(platform: OttPlatform, page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            val todayIso = dateDaysOffsetIso(0)
            apiService.discoverMovies(
                page = page,
                sortBy = "primary_release_date.desc",
                minVotes = 15,
                releaseDateLte = todayIso,
                withWatchProviders = platform.providerIds,
                watchRegion = platform.region
            )
        }

    suspend fun fetchClassicMovies(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            apiService.discoverMovies(
                page = page,
                sortBy = "vote_average.desc",
                minVotes = 1500,
                releaseDateLte = "1999-12-31"
            )
        }

    suspend fun fetchFamousMovies(page: Int = 1): Result<List<CatalogTitle>> =
        discoverMovieRail {
            apiService.discoverMovies(
                page = page,
                sortBy = "vote_count.desc",
                minVotes = 5000
            )
        }

    private suspend fun discoverMovieRail(
        block: suspend () -> TmdbPagedResponse<TmdbMediaDto>
    ): Result<List<CatalogTitle>> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val response = block()
            val mapped = response.results.map { dto ->
                mapMediaDto(dto, MediaType.MOVIE).also { cacheTitle(it) }
            }
            Result.success(mapped)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun search(
        query: String,
        scope: SearchScope = SearchScope.MULTI,
        page: Int = 1
    ): Result<TmdbSearchBundle> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return Result.success(TmdbSearchBundle())
        }
        return try {
            when (scope) {
                SearchScope.PEOPLE -> {
                    val res = apiService.searchPeople(query = cleanQuery, page = page)
                    Result.success(
                        TmdbSearchBundle(
                            people = res.results.map { mapPersonSummary(it) },
                            page = res.page,
                            totalPages = res.totalPages
                        )
                    )
                }
                SearchScope.COLLECTIONS -> {
                    val res = apiService.searchCollections(query = cleanQuery, page = page)
                    Result.success(
                        TmdbSearchBundle(
                            collections = res.results.map { c ->
                                CollectionSummary(
                                    id = c.id,
                                    name = c.name,
                                    posterUrl = buildImageUrl(c.posterPath, "w500"),
                                    backdropUrl = buildImageUrl(c.backdropPath, "w780")
                                )
                            },
                            page = res.page,
                            totalPages = res.totalPages
                        )
                    )
                }
                SearchScope.MOVIES -> {
                    val res = apiService.searchMovies(query = cleanQuery, page = page)
                    val items = res.results.map { mapMediaDto(it, MediaType.MOVIE).also(::cacheTitle) }
                    Result.success(TmdbSearchBundle(titles = items, page = res.page, totalPages = res.totalPages))
                }
                SearchScope.SERIES -> {
                    val res = apiService.searchTv(query = cleanQuery, page = page)
                    val items = res.results.map { mapMediaDto(it, MediaType.SERIES).also(::cacheTitle) }
                    Result.success(TmdbSearchBundle(titles = items, page = res.page, totalPages = res.totalPages))
                }
                SearchScope.MULTI -> {
                    val res = apiService.searchMulti(query = cleanQuery, page = page)
                    val items = res.results
                        .filter { it.mediaType == null || it.mediaType == "movie" || it.mediaType == "tv" }
                        .map { mapMediaDto(it, MediaType.MOVIE).also(::cacheTitle) }
                    Result.success(TmdbSearchBundle(titles = items, page = res.page, totalPages = res.totalPages))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFullDetails(titleId: String): Result<CatalogTitle> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        val parsed = parseTmdbId(titleId)
            ?: return Result.failure(IllegalArgumentException("Not a TMDB title ID: $titleId"))

        return try {
            val (mediaType, tmdbId) = parsed
            val detailed = if (mediaType == MediaType.SERIES) {
                mapTvDetail(apiService.getTvDetails(tmdbId))
            } else {
                mapMovieDetail(apiService.getMovieDetails(tmdbId))
            }
            cacheTitle(detailed)
            detailed.recommendations.forEach { cacheTitle(it) }
            detailed.similarTitles.forEach { cacheTitle(it) }
            Result.success(detailed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchSeasonDetails(seriesId: Int, seasonNumber: Int): Result<SeasonDetail> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val dto = apiService.getTvSeasonDetails(seriesId, seasonNumber)
            Result.success(
                SeasonDetail(
                    seasonNumber = dto.seasonNumber,
                    name = dto.name.orEmpty().ifBlank { "Season ${dto.seasonNumber}" },
                    overview = dto.overview.orEmpty(),
                    airDate = dto.airDate.orEmpty(),
                    posterUrl = buildImageUrl(dto.posterPath, "w500"),
                    episodes = dto.episodes.map { ep ->
                        EpisodeItem(
                            id = ep.id,
                            episodeNumber = ep.episodeNumber,
                            seasonNumber = ep.seasonNumber,
                            name = ep.name.orEmpty().ifBlank { "Episode ${ep.episodeNumber}" },
                            overview = ep.overview.orEmpty(),
                            airDate = ep.airDate.orEmpty(),
                            runtimeMinutes = ep.runtime ?: 0,
                            voteAverage = ((ep.voteAverage * 10.0).toInt() / 10.0),
                            stillUrl = buildImageUrl(ep.stillPath, "w500")
                        )
                    }
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPersonDetails(personId: Int): Result<PersonDetail> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val dto = apiService.getPersonDetails(personId)
            val credits = (dto.combinedCredits?.cast.orEmpty() + dto.combinedCredits?.crew.orEmpty())
                .filter { it.id > 0 && (it.mediaType == "movie" || it.mediaType == "tv" || it.title != null || it.name != null) }
                .sortedByDescending { it.popularity }
                .distinctBy { "${it.mediaType}_${it.id}" }
                .take(24)
                .map { media ->
                    val mapped = mapMediaDto(media, MediaType.MOVIE)
                    cacheTitle(mapped)
                    mapped
                }
            Result.success(
                PersonDetail(
                    id = dto.id,
                    name = dto.name,
                    biography = dto.biography.orEmpty().ifBlank { "No biography available on TMDB." },
                    birthday = dto.birthday.orEmpty(),
                    deathday = dto.deathday.orEmpty(),
                    placeOfBirth = dto.placeOfBirth.orEmpty(),
                    knownForDepartment = dto.knownForDepartment.orEmpty(),
                    profileUrl = buildImageUrl(dto.profilePath, "w500"),
                    photos = dto.images?.profiles?.take(10)?.map { buildImageUrl(it.filePath, "w500") }
                        ?.filter { it.isNotBlank() }.orEmpty(),
                    credits = credits
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCollectionDetails(collectionId: Int): Result<CollectionDetail> {
        if (!hasConfiguredApiKey()) {
            return Result.failure(IllegalStateException("TMDB API key not configured"))
        }
        return try {
            val dto = apiService.getCollectionDetails(collectionId)
            val parts = dto.parts
                .sortedBy { it.releaseDate.orEmpty().ifBlank { "9999" } }
                .map { part ->
                    mapMediaDto(part, MediaType.MOVIE).also { cacheTitle(it) }
                }
            Result.success(
                CollectionDetail(
                    id = dto.id,
                    name = dto.name,
                    overview = dto.overview.orEmpty(),
                    posterUrl = buildImageUrl(dto.posterPath, "w500"),
                    backdropUrl = buildImageUrl(dto.backdropPath, "w780"),
                    parts = parts
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cacheTitle(item: CatalogTitle) {
        val existing = titleCache[item.id]
        if (existing != null && existing.cast.isNotEmpty() && item.cast.isEmpty()) {
            return
        }
        titleCache[item.id] = item
    }

    internal fun mapPersonSummary(dto: TmdbPersonSummaryDto): PersonSummary {
        val knownTitles = dto.knownFor.take(2).mapNotNull { it.title ?: it.name }.joinToString(", ")
        return PersonSummary(
            id = dto.id,
            name = dto.name,
            knownForDepartment = dto.knownForDepartment.orEmpty().ifBlank { "Acting" },
            profileUrl = buildImageUrl(dto.profilePath, "w185"),
            knownForTitles = knownTitles
        )
    }

    internal fun mapMediaDto(dto: TmdbMediaDto, fallbackType: MediaType): CatalogTitle {
        val type = when (dto.mediaType?.lowercase()) {
            "tv" -> MediaType.SERIES
            "movie" -> MediaType.MOVIE
            else -> fallbackType
        }
        val titleText = if (type == MediaType.SERIES) {
            dto.name ?: dto.originalName ?: dto.title ?: "Untitled Series"
        } else {
            dto.title ?: dto.originalTitle ?: dto.name ?: "Untitled Movie"
        }
        val rawDate = if (type == MediaType.SERIES) dto.firstAirDate else dto.releaseDate
        val year = rawDate?.take(4)?.toIntOrNull() ?: 2024
        val genreMap = if (type == MediaType.SERIES) tvGenreMap else movieGenreMap
        val genreNames = dto.genreIds.mapNotNull { id ->
            genreMap[id] ?: DEFAULT_GENRE_NAMES[id]
        }.distinct()
        val id = buildTmdbId(type, dto.id)
        val score = ((dto.voteAverage * 10.0).toInt() / 10.0).coerceIn(0.0, 10.0)
        val roleNote = dto.character?.takeIf { it.isNotBlank() }?.let { "as $it" }
            ?: dto.job?.takeIf { it.isNotBlank() }
        return CatalogTitle(
            id = id,
            title = titleText,
            tagline = "",
            mediaType = type,
            releaseYear = year,
            genres = genreNames.ifEmpty { listOf(type.label) },
            directorOrCreator = roleNote ?: if (type == MediaType.SERIES) "TMDB Series" else "TMDB Film",
            studioOrNetwork = "The Movie Database",
            country = "",
            language = formatLanguageCode(dto.originalLanguage),
            ageRating = "",
            runtimeOrSeasons = rawDate.orEmpty().ifBlank { year.toString() },
            totalEpisodes = 0,
            criticScore = score,
            synopsis = dto.overview.orEmpty().ifBlank { "No overview available yet on TMDB." },
            posterKey = posterKeyForGenres(genreNames),
            tmdbId = dto.id,
            posterUrl = buildImageUrl(dto.posterPath, "w500"),
            backdropUrl = buildImageUrl(dto.backdropPath, "w780"),
            voteCount = dto.voteCount,
            popularity = dto.popularity,
            releaseDate = rawDate.orEmpty()
        )
    }

    internal fun mapMovieDetail(dto: TmdbMovieDetailDto): CatalogTitle {
        val genreNames = dto.genres.map { it.name }.filter { it.isNotBlank() }
        val year = dto.releaseDate?.take(4)?.toIntOrNull() ?: 2024
        val runtimeText = dto.runtime?.takeIf { it > 0 }?.let { mins ->
            val h = mins / 60
            val m = mins % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        }.orEmpty()
        val directors = dto.credits?.crew
            ?.filter { it.job.equals("Director", ignoreCase = true) }
            ?.map { it.name }
            ?.distinct()
            ?.joinToString(", ")
            .orEmpty()
        val cert = dto.releaseDates?.results
            ?.firstOrNull { it.iso.equals("US", ignoreCase = true) }
            ?.releaseDates
            ?.firstOrNull { it.certification.isNotBlank() }
            ?.certification
            .orEmpty()

        return CatalogTitle(
            id = buildTmdbId(MediaType.MOVIE, dto.id),
            title = dto.title.orEmpty().ifBlank { "Untitled" },
            tagline = dto.tagline.orEmpty(),
            mediaType = MediaType.MOVIE,
            releaseYear = year,
            genres = genreNames,
            directorOrCreator = directors.ifBlank { "Director N/A" },
            studioOrNetwork = dto.productionCompanies.take(2).joinToString(" · ") { it.name }.ifBlank { "Independent" },
            country = dto.productionCountries.take(2).joinToString(" / ") { it.name },
            language = dto.spokenLanguages.take(2).joinToString(" / ") { it.englishName.ifBlank { it.name } }
                .ifBlank { formatLanguageCode(dto.originalLanguage) },
            ageRating = cert,
            runtimeOrSeasons = runtimeText.ifBlank { dto.status.orEmpty() },
            totalEpisodes = 0,
            criticScore = ((dto.voteAverage * 10.0).toInt() / 10.0).coerceIn(0.0, 10.0),
            synopsis = dto.overview.orEmpty(),
            posterKey = posterKeyForGenres(genreNames),
            cast = dto.credits?.cast?.sortedBy { it.order }?.take(16)?.map { c ->
                CastMember(
                    id = c.id,
                    actor = c.name,
                    role = c.character.orEmpty().ifBlank { "Cast" },
                    profileUrl = buildImageUrl(c.profilePath, "w185")
                )
            }.orEmpty(),
            awardsHighlight = dto.status?.takeIf { it.isNotBlank() }?.let { "Status: $it" }.orEmpty(),
            tmdbId = dto.id,
            posterUrl = buildImageUrl(dto.posterPath, "w500"),
            backdropUrl = buildImageUrl(dto.backdropPath, "w780"),
            voteCount = dto.voteCount,
            popularity = dto.popularity,
            releaseDate = dto.releaseDate.orEmpty(),
            statusText = dto.status.orEmpty(),
            budget = dto.budget,
            revenue = dto.revenue,
            crew = dto.credits?.crew?.take(10)?.map { cr ->
                CrewMember(cr.id, cr.name, cr.job.orEmpty(), cr.department.orEmpty())
            }.orEmpty(),
            tmdbReviews = dto.reviews?.results?.take(8)?.map { r ->
                TmdbReviewItem(
                    id = r.id,
                    author = r.author.ifBlank { r.authorDetails?.username ?: "TMDB Critic" },
                    username = r.authorDetails?.username.orEmpty(),
                    rating = r.authorDetails?.rating,
                    content = r.content,
                    createdAt = r.createdAt.orEmpty().take(10)
                )
            }.orEmpty(),
            videos = dto.videos?.results?.take(6)?.map { v ->
                TmdbVideoItem(v.id, v.key, v.name, v.site, v.type, v.official)
            }.orEmpty(),
            backdrops = dto.images?.backdrops?.take(8)?.map { img ->
                buildImageUrl(img.filePath, "w780")
            }?.filter { it.isNotBlank() }.orEmpty(),
            recommendations = dto.recommendations?.results?.take(10)?.map { rec ->
                mapMediaDto(rec, MediaType.MOVIE)
            }.orEmpty(),
            similarTitles = dto.similar?.results?.take(10)?.map { sim ->
                mapMediaDto(sim, MediaType.MOVIE)
            }.orEmpty(),
            watchProviders = extractWatchProviders(dto.watchProviders),
            keywords = (dto.keywords?.keywords ?: dto.keywords?.results).orEmpty().take(10).map { it.name },
            collection = dto.belongsToCollection?.let { c ->
                CollectionSummary(
                    id = c.id,
                    name = c.name,
                    posterUrl = buildImageUrl(c.posterPath, "w500"),
                    backdropUrl = buildImageUrl(c.backdropPath, "w780")
                )
            }
        )
    }

    internal fun mapTvDetail(dto: TmdbTvDetailDto): CatalogTitle {
        val genreNames = dto.genres.map { it.name }.filter { it.isNotBlank() }
        val year = dto.firstAirDate?.take(4)?.toIntOrNull() ?: 2024
        val seasonsPart = if (dto.numberOfSeasons > 0) {
            "${dto.numberOfSeasons} Season" + if (dto.numberOfSeasons > 1) "s" else ""
        } else ""
        val episodesPart = if (dto.numberOfEpisodes > 0) "${dto.numberOfEpisodes} Episodes" else ""
        val runtimeOrSeasons = listOf(seasonsPart, episodesPart).filter { it.isNotBlank() }.joinToString(" · ")
        val creators = dto.createdBy.joinToString(", ") { it.name }
            .ifBlank {
                dto.credits?.crew?.firstOrNull {
                    it.job.equals("Executive Producer", ignoreCase = true) ||
                        it.job.equals("Creator", ignoreCase = true)
                }?.name.orEmpty()
            }
        val cert = dto.contentRatings?.results
            ?.firstOrNull { it.iso.equals("US", ignoreCase = true) }
            ?.rating
            .orEmpty()

        return CatalogTitle(
            id = buildTmdbId(MediaType.SERIES, dto.id),
            title = dto.name.orEmpty().ifBlank { "Untitled Series" },
            tagline = dto.tagline.orEmpty(),
            mediaType = MediaType.SERIES,
            releaseYear = year,
            genres = genreNames,
            directorOrCreator = creators.ifBlank { "Creator N/A" },
            studioOrNetwork = dto.networks.take(2).joinToString(" · ") { it.name }
                .ifBlank { dto.productionCompanies.take(2).joinToString(" · ") { it.name } },
            country = dto.originCountry.take(2).joinToString(" / "),
            language = dto.spokenLanguages.take(2).joinToString(" / ") { it.englishName.ifBlank { it.name } }
                .ifBlank { formatLanguageCode(dto.originalLanguage) },
            ageRating = cert,
            runtimeOrSeasons = runtimeOrSeasons.ifBlank { dto.status.orEmpty() },
            totalEpisodes = dto.numberOfEpisodes,
            criticScore = ((dto.voteAverage * 10.0).toInt() / 10.0).coerceIn(0.0, 10.0),
            synopsis = dto.overview.orEmpty(),
            posterKey = posterKeyForGenres(genreNames),
            cast = dto.credits?.cast?.sortedBy { it.order }?.take(16)?.map { c ->
                CastMember(
                    id = c.id,
                    actor = c.name,
                    role = c.character.orEmpty().ifBlank { "Series Regular" },
                    profileUrl = buildImageUrl(c.profilePath, "w185")
                )
            }.orEmpty(),
            awardsHighlight = dto.status?.takeIf { it.isNotBlank() }?.let { "Series Status: $it" }.orEmpty(),
            tmdbId = dto.id,
            posterUrl = buildImageUrl(dto.posterPath, "w500"),
            backdropUrl = buildImageUrl(dto.backdropPath, "w780"),
            voteCount = dto.voteCount,
            popularity = dto.popularity,
            releaseDate = dto.firstAirDate.orEmpty(),
            statusText = dto.status.orEmpty(),
            crew = dto.credits?.crew?.take(10)?.map { cr ->
                CrewMember(cr.id, cr.name, cr.job.orEmpty(), cr.department.orEmpty())
            }.orEmpty(),
            tmdbReviews = dto.reviews?.results?.take(8)?.map { r ->
                TmdbReviewItem(
                    id = r.id,
                    author = r.author.ifBlank { r.authorDetails?.username ?: "TMDB Critic" },
                    username = r.authorDetails?.username.orEmpty(),
                    rating = r.authorDetails?.rating,
                    content = r.content,
                    createdAt = r.createdAt.orEmpty().take(10)
                )
            }.orEmpty(),
            videos = dto.videos?.results?.take(6)?.map { v ->
                TmdbVideoItem(v.id, v.key, v.name, v.site, v.type, v.official)
            }.orEmpty(),
            backdrops = dto.images?.backdrops?.take(8)?.map { img ->
                buildImageUrl(img.filePath, "w780")
            }?.filter { it.isNotBlank() }.orEmpty(),
            recommendations = dto.recommendations?.results?.take(10)?.map { rec ->
                mapMediaDto(rec, MediaType.SERIES)
            }.orEmpty(),
            similarTitles = dto.similar?.results?.take(10)?.map { sim ->
                mapMediaDto(sim, MediaType.SERIES)
            }.orEmpty(),
            watchProviders = extractWatchProviders(dto.watchProviders),
            keywords = (dto.keywords?.results ?: dto.keywords?.keywords).orEmpty().take(10).map { it.name },
            seasons = dto.seasons.map { s ->
                SeasonSummary(
                    seasonNumber = s.seasonNumber,
                    name = s.name.orEmpty().ifBlank { "Season ${s.seasonNumber}" },
                    episodeCount = s.episodeCount,
                    airDate = s.airDate.orEmpty(),
                    posterUrl = buildImageUrl(s.posterPath, "w342"),
                    overview = s.overview.orEmpty()
                )
            }
        )
    }

    private fun extractWatchProviders(response: TmdbWatchProvidersResponse?): List<WatchProviderItem> {
        val map = response?.results ?: return emptyList()
        val region = map["US"] ?: map["IN"] ?: map["GB"] ?: map.values.firstOrNull() ?: return emptyList()
        val stream = region.flatrate.map { p ->
            WatchProviderItem(p.providerId, p.providerName, buildImageUrl(p.logoPath, "w92"), "Stream")
        }
        val rent = region.rent.map { p ->
            WatchProviderItem(p.providerId, p.providerName, buildImageUrl(p.logoPath, "w92"), "Rent")
        }
        val buy = region.buy.map { p ->
            WatchProviderItem(p.providerId, p.providerName, buildImageUrl(p.logoPath, "w92"), "Buy")
        }
        return (stream + rent + buy).distinctBy { "${it.id}_${it.tier}" }.take(10)
    }

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"
        private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        private const val PLACEHOLDER_KEY = "YOUR_TMDB_API_KEY"
        private val V3_AUD_REGEX = Regex("\"aud\"\\s*:\\s*\"([a-fA-F0-9]{32})\"")

        fun buildTmdbId(type: MediaType, tmdbId: Int): String =
            if (type == MediaType.SERIES) "tmdb_tv_$tmdbId" else "tmdb_movie_$tmdbId"

        fun parseTmdbId(id: String): Pair<MediaType, Int>? {
            return when {
                id.startsWith("tmdb_movie_") -> id.removePrefix("tmdb_movie_").toIntOrNull()?.let { MediaType.MOVIE to it }
                id.startsWith("tmdb_tv_") -> id.removePrefix("tmdb_tv_").toIntOrNull()?.let { MediaType.SERIES to it }
                else -> null
            }
        }

        fun buildImageUrl(path: String?, size: String): String {
            if (path.isNullOrBlank()) return ""
            val clean = if (path.startsWith("/")) path else "/$path"
            return "$IMAGE_BASE_URL$size$clean"
        }

        internal fun normalizeKey(raw: String): String {
            return raw.trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")
                .removePrefix("Bearer ")
                .removePrefix("bearer ")
                .trim()
        }

        /**
         * Resolves whether to authenticate using a complete 3-part JWT Bearer token,
         * or a TMDB v3 API key (either provided directly, or extracted from the `aud` claim
         * if a user pasted a truncated TMDB v4 JWT that lacks its `.signature` segment).
         */
        internal fun resolveAuthMode(rawKey: String): Pair<Boolean, String?> {
            val key = normalizeKey(rawKey)
            if (key.isEmpty() || key == PLACEHOLDER_KEY) return false to null

            if (key.startsWith("eyJ")) {
                val dotCount = key.count { it == '.' }
                if (dotCount >= 2 && key.substringAfterLast('.').isNotBlank()) {
                    return true to key // Complete 3-part Bearer JWT
                }
                // Truncated JWT: try extracting the 32-char v3 key from the `aud` claim in segment 1
                val extractedV3 = extractV3KeyFromJwtPayload(key)
                return if (extractedV3 != null) {
                    false to extractedV3
                } else {
                    true to key
                }
            }
            return false to key // Direct v3 API key
        }

        internal fun extractV3KeyFromJwtPayload(jwt: String): String? {
            val parts = jwt.split('.')
            if (parts.size < 2) return null
            val payloadSeg = parts[1]
            val decoded = decodeBase64UrlPrefix(payloadSeg) ?: return null
            return V3_AUD_REGEX.find(decoded)?.groupValues?.getOrNull(1)
        }

        private fun decodeBase64UrlPrefix(input: String): String? {
            if (input.isBlank()) return null
            val usableLen = (input.length / 4) * 4
            val clean = if (usableLen >= 4) input.substring(0, usableLen) else return null
            val out = ArrayList<Byte>((clean.length * 3) / 4)
            var i = 0
            while (i < clean.length) {
                val c0 = base64UrlValue(clean[i])
                val c1 = base64UrlValue(clean[i + 1])
                val c2 = base64UrlValue(clean[i + 2])
                val c3 = base64UrlValue(clean[i + 3])
                if (c0 < 0 || c1 < 0) break
                val triple = (c0 shl 18) or (c1 shl 12) or
                    ((if (c2 >= 0) c2 else 0) shl 6) or
                    (if (c3 >= 0) c3 else 0)
                out.add(((triple shr 16) and 0xFF).toByte())
                if (c2 >= 0) out.add(((triple shr 8) and 0xFF).toByte())
                if (c3 >= 0) out.add((triple and 0xFF).toByte())
                i += 4
            }
            return out.toByteArray().toString(Charsets.UTF_8)
        }

        private fun base64UrlValue(ch: Char): Int = when (ch) {
            in 'A'..'Z' -> ch - 'A'
            in 'a'..'z' -> ch - 'a' + 26
            in '0'..'9' -> ch - '0' + 52
            '-', '+' -> 62
            '_', '/' -> 63
            else -> -1
        }

        private fun posterKeyForGenres(genres: List<String>): String {
            val lower = genres.joinToString(" ").lowercase()
            return when {
                lower.contains("sci") || lower.contains("fantasy") || lower.contains("animation") -> "scifi"
                lower.contains("thriller") || lower.contains("crime") || lower.contains("mystery") || lower.contains("horror") -> "noir"
                lower.contains("drama") || lower.contains("romance") || lower.contains("history") -> "drama"
                else -> "cinema"
            }
        }

        private fun formatLanguageCode(code: String?): String {
            if (code.isNullOrBlank()) return "English"
            return Locale.forLanguageTag(code).displayLanguage.ifBlank { code.uppercase() }
        }

        private val DEFAULT_GENRE_NAMES = mapOf(
            28 to "Action",
            12 to "Adventure",
            16 to "Animation",
            35 to "Comedy",
            80 to "Crime",
            99 to "Documentary",
            18 to "Drama",
            10751 to "Family",
            14 to "Fantasy",
            36 to "History",
            27 to "Horror",
            10402 to "Music",
            9648 to "Mystery",
            10749 to "Romance",
            878 to "Sci-Fi",
            53 to "Thriller",
            10752 to "War",
            37 to "Western",
            10759 to "Action & Adventure",
            10765 to "Sci-Fi & Fantasy",
            10768 to "War & Politics"
        )

        private fun createRetrofitService(apiKeyProvider: () -> String): TmdbApiService {
            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val raw = apiKeyProvider()
                val (useBearer, credential) = resolveAuthMode(raw)

                val requestBuilder = original.newBuilder()
                    .header("Accept", "application/json")

                val primaryRequest = if (credential != null) {
                    if (useBearer) {
                        requestBuilder
                            .header("Authorization", "Bearer $credential")
                            .build()
                    } else {
                        val url = original.url.newBuilder()
                            .addQueryParameter("api_key", credential)
                            .build()
                        requestBuilder.url(url).build()
                    }
                } else {
                    requestBuilder.build()
                }

                val response = chain.proceed(primaryRequest)
                // If Bearer JWT fails with 401 and has a recoverable v3 `aud` key, retry once cleanly
                if (response.code == 401 && useBearer && credential != null) {
                    val fallbackV3 = extractV3KeyFromJwtPayload(credential)
                    if (fallbackV3 != null) {
                        response.close()
                        val fallbackUrl = original.url.newBuilder()
                            .addQueryParameter("api_key", fallbackV3)
                            .build()
                        val fallbackReq = original.newBuilder()
                            .header("Accept", "application/json")
                            .url(fallbackUrl)
                            .build()
                        return@Interceptor chain.proceed(fallbackReq)
                    }
                }
                response
            }

            val logging = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(TmdbApiService::class.java)
        }
    }
}
