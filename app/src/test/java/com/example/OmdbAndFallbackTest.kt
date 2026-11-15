package com.example

import com.example.data.model.CatalogTitle
import com.example.data.model.DataSourceOrigin
import com.example.data.model.MediaType
import com.example.data.model.SearchScope
import com.example.data.omdb.OmdbApiService
import com.example.data.omdb.OmdbRatingDto
import com.example.data.omdb.OmdbRepository
import com.example.data.omdb.OmdbSearchItemDto
import com.example.data.omdb.OmdbSearchResponseDto
import com.example.data.omdb.OmdbTitleDetailDto
import com.example.data.repository.UnifiedMediaService
import com.example.data.tmdb.TmdbApiService
import com.example.data.tmdb.TmdbCastDto
import com.example.data.tmdb.TmdbCreditsDto
import com.example.data.tmdb.TmdbExternalIdsDto
import com.example.data.tmdb.TmdbGenreDto
import com.example.data.tmdb.TmdbMovieDetailDto
import com.example.data.tmdb.TmdbRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.lang.reflect.Proxy

class OmdbAndFallbackTest {

    @Test
    fun omdbRepository_mapsRatingsAndAwards_andNormalizesNa() {
        val repo = OmdbRepository(apiKeyProvider = { "f3aaa85e" })
        val dto = OmdbTitleDetailDto(
            title = "Oppenheimer",
            year = "2023",
            rated = "R",
            released = "21 Jul 2023",
            runtime = "180 min",
            genre = "Biography, Drama, History",
            director = "Christopher Nolan",
            actors = "Cillian Murphy, Emily Blunt, Matt Damon",
            plot = "The story of American scientist J. Robert Oppenheimer.",
            language = "English, German, Italian",
            country = "United States, United Kingdom",
            awards = "Won 7 Oscars. 363 wins & 364 nominations total",
            poster = "http://m.media-amazon.com/images/opp.jpg",
            ratings = listOf(
                OmdbRatingDto("Internet Movie Database", "8.3/10"),
                OmdbRatingDto("Rotten Tomatoes", "93%"),
                OmdbRatingDto("Metacritic", "90/100")
            ),
            metascore = "90",
            imdbRating = "8.3",
            imdbVotes = "750,000",
            imdbId = "tt15398776",
            type = "movie",
            response = "True"
        )

        val mapped = repo.mapOmdbDetail(dto)
        assertEquals("omdb_tt15398776", mapped.id)
        assertEquals(DataSourceOrigin.OMDB_FALLBACK, mapped.dataSource)
        assertEquals("3h 0m", mapped.runtimeOrSeasons)
        assertEquals("2023-07-21", mapped.releaseDate)
        assertEquals("https://m.media-amazon.com/images/opp.jpg", mapped.posterUrl)
        assertEquals(3, mapped.cast.size)
        assertNotNull(mapped.ratingsAndAwards)
        assertEquals("8.3/10", mapped.ratingsAndAwards?.imdbRating)
        assertEquals("93%", mapped.ratingsAndAwards?.rottenTomatoes)
        assertEquals("90/100", mapped.ratingsAndAwards?.metacritic)
        assertEquals("Won 7 Oscars. 363 wins & 364 nominations total", mapped.ratingsAndAwards?.awards)
    }

    @Test
    fun unifiedMediaService_whenTmdbUp_usesTmdbPrimaryAndEnrichesWithOmdbRatingsAndAwards() = runTest {
        var capturedImdbQuery: String? = null
        val tmdbService = proxyTmdbService(
            onGetMovieDetails = { id ->
                TmdbMovieDetailDto(
                    id = id,
                    title = "Dune: Part Two",
                    overview = "TMDB authoritative overview",
                    posterPath = "/dune2.jpg",
                    releaseDate = "2024-02-27",
                    runtime = 166,
                    status = "Released",
                    voteAverage = 8.4,
                    imdbId = "tt15239678",
                    externalIds = TmdbExternalIdsDto(imdbId = "tt15239678"),
                    genres = listOf(TmdbGenreDto(878, "Science Fiction")),
                    credits = TmdbCreditsDto(
                        cast = listOf(TmdbCastDto(1, "Timothée Chalamet", "Paul Atreides", "/tim.jpg", 0))
                    )
                )
            }
        )
        val omdbService = object : OmdbApiService {
            override suspend fun getByImdbId(imdbId: String, plot: String, format: String): OmdbTitleDetailDto {
                capturedImdbQuery = imdbId
                return OmdbTitleDetailDto(
                    title = "Dune: Part Two",
                    awards = "Won 2 Oscars. 55 wins & 140 nominations total",
                    ratings = listOf(
                        OmdbRatingDto("Internet Movie Database", "8.5/10"),
                        OmdbRatingDto("Rotten Tomatoes", "92%"),
                        OmdbRatingDto("Metacritic", "79/100")
                    ),
                    imdbRating = "8.5",
                    imdbVotes = "540,000",
                    metascore = "79",
                    imdbId = imdbId,
                    type = "movie",
                    response = "True"
                )
            }

            override suspend fun getByTitle(title: String, year: Int?, type: String?, plot: String, format: String) =
                error("Should have matched via imdb_id first")

            override suspend fun search(query: String, type: String?, page: Int, format: String) =
                OmdbSearchResponseDto()
        }

        val unified = UnifiedMediaService(
            tmdbRepository = TmdbRepository(apiKeyProvider = { "test_tmdb_key" }, serviceOverride = tmdbService),
            omdbRepository = OmdbRepository(apiKeyProvider = { "f3aaa85e" }, serviceOverride = omdbService)
        )

        val result = unified.fetchUnifiedTitleDetails("tmdb_movie_693134")
        assertTrue(result.isSuccess)
        val detail = result.getOrThrow()

        assertEquals("tt15239678", capturedImdbQuery)
        assertEquals(DataSourceOrigin.TMDB_WITH_OMDB, detail.dataSource)
        assertEquals("TMDB authoritative overview", detail.synopsis)
        assertEquals("8.5/10", detail.ratingsAndAwards?.imdbRating)
        assertEquals("92%", detail.ratingsAndAwards?.rottenTomatoes)
        assertEquals("79/100", detail.ratingsAndAwards?.metacritic)
        assertEquals("Won 2 Oscars. 55 wins & 140 nominations total", detail.ratingsAndAwards?.awards)
    }

    @Test
    fun unifiedMediaService_whenTmdbDown_fallsBackToOmdbSeamlessly() = runTest {
        val failingTmdb = proxyTmdbService(
            onGetMovieDetails = { throw IOException("TMDB gateway timeout") },
            onSearchMulti = { throw IOException("TMDB unreachable") }
        )
        val omdbService = object : OmdbApiService {
            override suspend fun getByImdbId(imdbId: String, plot: String, format: String) =
                OmdbTitleDetailDto(response = "False", error = "Not found")

            override suspend fun getByTitle(title: String, year: Int?, type: String?, plot: String, format: String) =
                OmdbTitleDetailDto(
                    title = "Interstellar",
                    year = "2014",
                    released = "07 Nov 2014",
                    runtime = "169 min",
                    genre = "Adventure, Drama, Sci-Fi",
                    director = "Christopher Nolan",
                    actors = "Matthew McConaughey, Anne Hathaway",
                    plot = "OMDb full plot fallback.",
                    awards = "Won 1 Oscar. 44 wins & 148 nominations total",
                    poster = "https://m.media-amazon.com/images/interstellar.jpg",
                    ratings = listOf(
                        OmdbRatingDto("Internet Movie Database", "8.7/10"),
                        OmdbRatingDto("Rotten Tomatoes", "73%"),
                        OmdbRatingDto("Metacritic", "74/100")
                    ),
                    imdbRating = "8.7",
                    metascore = "74",
                    imdbId = "tt0816692",
                    type = "movie",
                    response = "True"
                )

            override suspend fun search(query: String, type: String?, page: Int, format: String) =
                OmdbSearchResponseDto(
                    search = listOf(
                        OmdbSearchItemDto(
                            title = "Interstellar",
                            year = "2014",
                            imdbId = "tt0816692",
                            type = "movie",
                            poster = "https://m.media-amazon.com/images/interstellar.jpg"
                        )
                    ),
                    totalResults = "1",
                    response = "True"
                )
        }

        val unified = UnifiedMediaService(
            tmdbRepository = TmdbRepository(apiKeyProvider = { "test_tmdb_key" }, serviceOverride = failingTmdb),
            omdbRepository = OmdbRepository(apiKeyProvider = { "f3aaa85e" }, serviceOverride = omdbService)
        )

        val searchRes = unified.searchWithFallback("Interstellar", SearchScope.MULTI)
        assertTrue(searchRes.isSuccess)
        val (bundle, origin) = searchRes.getOrThrow()
        assertEquals(DataSourceOrigin.OMDB_FALLBACK, origin)
        assertEquals("omdb_tt0816692", bundle.titles.first().id)

        val detailRes = unified.fetchUnifiedTitleDetails(
            titleId = "tmdb_movie_157336",
            hintTitle = CatalogTitle(
                id = "tmdb_movie_157336",
                title = "Interstellar",
                tagline = "",
                mediaType = MediaType.MOVIE,
                releaseYear = 2014,
                genres = listOf("Sci-Fi"),
                directorOrCreator = "",
                studioOrNetwork = "",
                country = "",
                language = "English",
                ageRating = "",
                runtimeOrSeasons = "",
                totalEpisodes = 0,
                criticScore = 8.4,
                synopsis = "",
                posterKey = "scifi",
                tmdbId = 157336
            )
        )
        assertTrue(detailRes.isSuccess)
        val fallbackDetail = detailRes.getOrThrow()
        assertEquals(DataSourceOrigin.OMDB_FALLBACK, fallbackDetail.dataSource)
        assertEquals("OMDb full plot fallback.", fallbackDetail.synopsis)
        assertEquals("8.7/10", fallbackDetail.ratingsAndAwards?.imdbRating)
        assertEquals("73%", fallbackDetail.ratingsAndAwards?.rottenTomatoes)
        assertEquals("74/100", fallbackDetail.ratingsAndAwards?.metacritic)
        assertEquals("Won 1 Oscar. 44 wins & 148 nominations total", fallbackDetail.ratingsAndAwards?.awards)
    }

    private fun proxyTmdbService(
        onGetMovieDetails: suspend (Int) -> TmdbMovieDetailDto = { error("Not mocked") },
        onSearchMulti: suspend () -> Nothing = { error("Not mocked") }
    ): TmdbApiService {
        return Proxy.newProxyInstance(
            TmdbApiService::class.java.classLoader,
            arrayOf(TmdbApiService::class.java)
        ) { _, method, args ->
            when (method.name) {
                "getMovieDetails" -> {
                    val movieId = args[0] as Int
                    val cont = args.last() as kotlin.coroutines.Continuation<Any?>
                    runSuspend(cont) { onGetMovieDetails(movieId) }
                }
                "searchMulti" -> {
                    val cont = args.last() as kotlin.coroutines.Continuation<Any?>
                    runSuspend(cont) { onSearchMulti() }
                }
                else -> throw UnsupportedOperationException("Unexpected call: ${method.name}")
            }
        } as TmdbApiService
    }

    @Suppress("UNCHECKED_CAST")
    private fun runSuspend(
        continuation: kotlin.coroutines.Continuation<Any?>,
        block: suspend () -> Any?
    ): Any? {
        val suspendFn: suspend () -> Any? = block
        val fn2 = suspendFn as Function1<kotlin.coroutines.Continuation<Any?>, Any?>
        return fn2.invoke(continuation)
    }
}
