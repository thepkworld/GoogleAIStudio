package com.example

import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.data.portability.VaultPortabilityManager
import com.example.ui.VaultStats
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VaultPortabilityTest {

    @Test
    fun exportAndImport_roundTripPreservesAllVaultFieldsAndStatsSummary() {
        val sampleEntries = listOf(
            VaultEntry(
                id = "tmdb_movie_27205",
                catalogId = "tmdb_movie_27205",
                tmdbId = 27205,
                title = "Inception",
                mediaType = MediaType.MOVIE.value,
                status = WatchStatus.WATCHED.value,
                priority = false,
                releaseYear = 2010L,
                genres = listOf("Action", "Sci-Fi", "Thriller"),
                directorOrCreator = "Christopher Nolan",
                runtimeOrSeasons = "2h 28m",
                synopsis = "A thief who steals corporate secrets through dream-sharing technology.",
                posterKey = "scifi",
                posterUrl = "https://image.tmdb.org/t/p/w500/inception.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/w780/inception_bg.jpg",
                userRating = 9.0,
                reviewTitle = "Mind-bending heist masterpiece",
                reviewBody = "Zimmer score + layered storytelling.",
                favoriteQuote = "You mustn't be afraid to dream a little bigger, darling.",
                containsSpoilers = false,
                rewatchCount = 3L,
                episodesWatched = 0L,
                totalEpisodes = 0L,
                customTitle = false,
                releaseDate = "2010-07-16",
                language = "English",
                studioOrNetwork = "Warner Bros.",
                watchedAt = 1731500000000L,
                createdAt = 1731400000000L,
                updatedAt = 1731500000000L
            ),
            VaultEntry(
                id = "omdb_tt0903747",
                catalogId = "omdb_tt0903747",
                tmdbId = null,
                title = "Breaking Bad",
                mediaType = MediaType.SERIES.value,
                status = WatchStatus.WANT_TO_WATCH.value,
                priority = true,
                releaseYear = 2008L,
                genres = listOf("Crime", "Drama"),
                directorOrCreator = "Vince Gilligan",
                runtimeOrSeasons = "5 Seasons",
                synopsis = "A chemistry teacher diagnosed with cancer turns to manufacturing.",
                posterKey = "drama",
                posterUrl = "https://m.media-amazon.com/images/bb.jpg",
                backdropUrl = "",
                userRating = 0.0,
                reviewTitle = "",
                reviewBody = "",
                favoriteQuote = "",
                containsSpoilers = false,
                rewatchCount = 0L,
                episodesWatched = 14L,
                totalEpisodes = 62L,
                customTitle = false,
                releaseDate = "2008-01-20",
                language = "English",
                studioOrNetwork = "AMC",
                watchedAt = 0L,
                createdAt = 1731300000000L,
                updatedAt = 1731300000000L
            )
        )

        val stats = VaultStats(
            totalWatched = 1,
            watchedMovies = 1,
            watchedSeries = 0,
            watchedThisMonth = 1,
            watchedMoviesThisMonth = 1,
            watchedSeriesThisMonth = 0,
            currentMonthLabel = "Nov 2026",
            totalWatchlist = 1,
            watchlistMovies = 0,
            watchlistSeries = 1,
            priorityWatchlist = 1,
            averageRating = 9.0,
            ratedCount = 1,
            totalReviews = 1
        )

        val json = VaultPortabilityManager.exportToJson(
            entries = sampleEntries,
            stats = stats,
            exportedAtMillis = 1731600000000L
        )

        val root = JSONObject(json)
        assertEquals(1, root.getInt("schemaVersion"))
        assertEquals("CinemaPaglu", root.getString("appName"))
        val summary = root.getJSONObject("statsSummary")
        assertEquals(1, summary.getInt("totalWatched"))
        assertEquals(1, summary.getInt("totalBucketlist"))
        assertEquals(9.0, summary.getDouble("averageRating"), 0.001)

        val imported = VaultPortabilityManager.importFromJson(json)
        assertTrue(imported.isSuccess)
        val pack = imported.getOrThrow()
        assertEquals(1731600000000L, pack.exportedAtMillis)
        assertEquals(2, pack.entries.size)

        val first = pack.entries[0]
        assertEquals("tmdb_movie_27205", first.id)
        assertEquals(27205, first.tmdbId)
        assertEquals("Inception", first.title)
        assertEquals(WatchStatus.WATCHED, first.statusEnum)
        assertEquals(9.0, first.userRating, 0.001)
        assertEquals("Mind-bending heist masterpiece", first.reviewTitle)
        assertEquals(3L, first.rewatchCount)
        assertEquals(listOf("Action", "Sci-Fi", "Thriller"), first.genres)
        assertEquals(1731500000000L, first.watchedAt)

        val second = pack.entries[1]
        assertEquals("omdb_tt0903747", second.id)
        assertNull(second.tmdbId)
        assertEquals(MediaType.SERIES, second.mediaTypeEnum)
        assertEquals(WatchStatus.WANT_TO_WATCH, second.statusEnum)
        assertTrue(second.priority)
        assertEquals(14L, second.episodesWatched)
        assertEquals(62L, second.totalEpisodes)
    }

    @Test
    fun importFromJson_rejectsBlankOrMalformedInput_andClampsOutOfRangeFields() {
        assertTrue(VaultPortabilityManager.importFromJson("   ").isFailure)
        assertTrue(VaultPortabilityManager.importFromJson("{\"appName\":\"CinemaPaglu\"}").isFailure)
        assertTrue(VaultPortabilityManager.importFromJson("{not-json").isFailure)

        val dirtyJson = """
            [
              {
                "id": "bad id!@#",
                "title": "  Clamped Film  ",
                "status": "WATCHED",
                "priority": true,
                "releaseYear": 1500,
                "userRating": 99.5,
                "rewatchCount": -5,
                "episodesWatched": 99999
              }
            ]
        """.trimIndent()

        val result = VaultPortabilityManager.importFromJson(dirtyJson)
        assertTrue(result.isSuccess)
        val entry = result.getOrThrow().entries.single()
        assertEquals("bad_id___", entry.id)
        assertEquals("Clamped Film", entry.title)
        // Priority is cleared when status is WATCHED
        assertEquals(false, entry.priority)
        assertEquals(1888L, entry.releaseYear)
        assertEquals(10.0, entry.userRating, 0.001)
        assertEquals(0L, entry.rewatchCount)
        assertEquals(5000L, entry.episodesWatched)
    }
}
