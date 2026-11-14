package com.example

import com.example.data.model.MediaType
import com.example.data.tmdb.TmdbAuthorDetailsDto
import com.example.data.tmdb.TmdbCastDto
import com.example.data.tmdb.TmdbCollectionRefDto
import com.example.data.tmdb.TmdbCreditsDto
import com.example.data.tmdb.TmdbGenreDto
import com.example.data.tmdb.TmdbImageDto
import com.example.data.tmdb.TmdbImagesResponse
import com.example.data.tmdb.TmdbKeywordDto
import com.example.data.tmdb.TmdbKeywordsResponse
import com.example.data.tmdb.TmdbMediaDto
import com.example.data.tmdb.TmdbMovieDetailDto
import com.example.data.tmdb.TmdbPagedResponse
import com.example.data.tmdb.TmdbPersonSummaryDto
import com.example.data.tmdb.TmdbRepository
import com.example.data.tmdb.TmdbReviewDto
import com.example.data.tmdb.TmdbReviewsResponse
import com.example.data.tmdb.TmdbSeasonRefDto
import com.example.data.tmdb.TmdbTvDetailDto
import com.example.data.tmdb.TmdbVideoDto
import com.example.data.tmdb.TmdbVideosResponse
import com.example.data.tmdb.TmdbWatchProviderDto
import com.example.data.tmdb.TmdbWatchProvidersResponse
import com.example.data.tmdb.TmdbWatchRegionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class TmdbRepositoryTest {

    @Test
    fun hasConfiguredApiKey_rejectsPlaceholderAndBlank() {
        assertFalse(TmdbRepository(apiKeyProvider = { "" }).hasConfiguredApiKey())
        assertFalse(TmdbRepository(apiKeyProvider = { "YOUR_TMDB_API_KEY" }).hasConfiguredApiKey())
        assertTrue(TmdbRepository(apiKeyProvider = { "eyJhbGciOiJIUzI1NiJ9.test.sig" }).hasConfiguredApiKey())
    }

    @Test
    fun resolveAuthMode_handlesFullJwtDirectV3AndTruncatedJwt() {
        assertEquals(false to null, TmdbRepository.resolveAuthMode(""))
        assertEquals(false to null, TmdbRepository.resolveAuthMode("YOUR_TMDB_API_KEY"))

        val directV3 = "0123456789abcdef0123456789abcdef"
        assertEquals(false to directV3, TmdbRepository.resolveAuthMode("  $directV3  "))

        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"HS256"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"aud":"$directV3","nbf":1700000000,"sub":"abc"}""".toByteArray())
        val fullJwt = "$header.$payload.signatureSegment"
        assertEquals(true to fullJwt, TmdbRepository.resolveAuthMode("Bearer $fullJwt"))

        // Truncated JWT missing final chars and `.signature` still recovers v3 `aud` key cleanly
        val truncatedJwt = "$header.${payload.dropLast(5)}"
        val (useBearer, credential) = TmdbRepository.resolveAuthMode(truncatedJwt)
        assertFalse(useBearer)
        assertEquals(directV3, credential)
    }

    @Test
    fun mapMovieDetail_populatesCastReviewsVideosCollectionWatchProvidersAndSimilar() {
        val repo = TmdbRepository(apiKeyProvider = { "test_key" })
        val dto = TmdbMovieDetailDto(
            id = 693134,
            title = "Dune: Part Two",
            tagline = "Long live the fighters.",
            overview = "Follow the mythic journey of Paul Atreides.",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-02-27",
            runtime = 166,
            status = "Released",
            budget = 190000000L,
            revenue = 714000000L,
            voteAverage = 8.34,
            voteCount = 5200,
            belongsToCollection = TmdbCollectionRefDto(726871, "Dune Collection", "/col.jpg", "/col_bg.jpg"),
            genres = listOf(TmdbGenreDto(878, "Science Fiction"), TmdbGenreDto(12, "Adventure")),
            credits = TmdbCreditsDto(
                cast = listOf(TmdbCastDto(1190668, "Timothée Chalamet", "Paul Atreides", "/tim.jpg", 0))
            ),
            reviews = TmdbReviewsResponse(
                results = listOf(
                    TmdbReviewDto(
                        id = "rev1",
                        author = "CinemaCritic",
                        authorDetails = TmdbAuthorDetailsDto("critic1", 9.0),
                        content = "Monumental filmmaking.",
                        createdAt = "2024-03-01T12:00:00Z"
                    )
                )
            ),
            videos = TmdbVideosResponse(
                results = listOf(TmdbVideoDto("v1", "Way9Dexny3w", "Official Trailer", "YouTube", "Trailer", true))
            ),
            images = TmdbImagesResponse(
                backdrops = listOf(TmdbImageDto("/still1.jpg"))
            ),
            recommendations = TmdbPagedResponse(
                results = listOf(
                    TmdbMediaDto(id = 438631, title = "Dune", releaseDate = "2021-09-15", voteAverage = 7.8)
                )
            ),
            similar = TmdbPagedResponse(
                results = listOf(
                    TmdbMediaDto(id = 335984, title = "Blade Runner 2049", releaseDate = "2017-10-04", voteAverage = 7.6)
                )
            ),
            watchProviders = TmdbWatchProvidersResponse(
                results = mapOf(
                    "US" to TmdbWatchRegionDto(
                        flatrate = listOf(TmdbWatchProviderDto(1899, "Max", "/max.jpg")),
                        rent = listOf(TmdbWatchProviderDto(2, "Apple TV", "/apple.jpg"))
                    )
                )
            ),
            keywords = TmdbKeywordsResponse(
                keywords = listOf(TmdbKeywordDto(1, "sandworm"), TmdbKeywordDto(2, "prophecy"))
            )
        )

        val mapped = repo.mapMovieDetail(dto)
        assertEquals("tmdb_movie_693134", mapped.id)
        assertEquals(MediaType.MOVIE, mapped.mediaType)
        assertEquals(2024, mapped.releaseYear)
        assertEquals("2h 46m", mapped.runtimeOrSeasons)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", mapped.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", mapped.backdropUrl)
        assertEquals(1, mapped.cast.size)
        assertEquals(1190668, mapped.cast.first().id)
        assertEquals(1, mapped.tmdbReviews.size)
        assertEquals("https://www.youtube.com/watch?v=Way9Dexny3w", mapped.videos.first().youtubeUrl)
        assertEquals(1, mapped.backdrops.size)
        assertEquals("tmdb_movie_438631", mapped.recommendations.first().id)
        assertEquals("tmdb_movie_335984", mapped.similarTitles.first().id)
        assertNotNull(mapped.collection)
        assertEquals("Dune Collection", mapped.collection?.name)
        assertEquals(2, mapped.watchProviders.size)
        assertEquals("Stream", mapped.watchProviders.first().tier)
        assertEquals(listOf("sandworm", "prophecy"), mapped.keywords)
        assertEquals(190000000L, mapped.budget)
        assertEquals(714000000L, mapped.revenue)
    }

    @Test
    fun mapTvDetail_mapsSeasonsEpisodesAndKeywords() {
        val repo = TmdbRepository(apiKeyProvider = { "test_key" })
        val dto = TmdbTvDetailDto(
            id = 95396,
            name = "Severance",
            firstAirDate = "2022-02-18",
            numberOfSeasons = 2,
            numberOfEpisodes = 19,
            voteAverage = 8.4,
            seasons = listOf(
                TmdbSeasonRefDto(1, 1, "Season 1", 9, "2022-02-18", "/s1.jpg", "Macrodata refinement."),
                TmdbSeasonRefDto(2, 2, "Season 2", 10, "2025-01-17", "/s2.jpg", "Return to Lumon.")
            ),
            keywords = TmdbKeywordsResponse(
                results = listOf(TmdbKeywordDto(10, "corporate satire"))
            )
        )
        val mapped = repo.mapTvDetail(dto)
        assertEquals("tmdb_tv_95396", mapped.id)
        assertEquals(MediaType.SERIES, mapped.mediaType)
        assertEquals(19, mapped.totalEpisodes)
        assertEquals("2 Seasons · 19 Episodes", mapped.runtimeOrSeasons)
        assertEquals(2, mapped.seasons.size)
        assertEquals(9, mapped.seasons.first().episodeCount)
        assertEquals(listOf("corporate satire"), mapped.keywords)
        assertNull(mapped.collection)
    }

    @Test
    fun mapPersonSummary_populatesKnownForAndProfileUrl() {
        val repo = TmdbRepository(apiKeyProvider = { "test_key" })
        val person = repo.mapPersonSummary(
            TmdbPersonSummaryDto(
                id = 525,
                name = "Christopher Nolan",
                knownForDepartment = "Directing",
                profilePath = "/nolan.jpg",
                knownFor = listOf(
                    TmdbMediaDto(id = 27205, title = "Inception"),
                    TmdbMediaDto(id = 157336, title = "Interstellar")
                )
            )
        )
        assertEquals(525, person.id)
        assertEquals("Christopher Nolan", person.name)
        assertEquals("Directing", person.knownForDepartment)
        assertEquals("https://image.tmdb.org/t/p/w185/nolan.jpg", person.profileUrl)
        assertEquals("Inception, Interstellar", person.knownForTitles)
    }
}

