package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.CatalogRepository
import com.example.data.local.VaultDatabase
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.data.repository.VaultRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VaultRepositoryTest {

    private lateinit var database: VaultDatabase
    private lateinit var repository: VaultRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, VaultDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VaultRepository(database.vaultDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun upsertFromCatalog_movesWatchlistToWatched_andPersistsReview() = runTest {
        val sample = CatalogRepository.featuredTitle()

        val insertResult = repository.upsertFromCatalog(
            catalogTitle = sample,
            targetStatus = WatchStatus.WANT_TO_WATCH,
            isPriority = true
        )
        assertTrue(insertResult.isSuccess)

        var entries = repository.observeVaultEntries().first()
        assertEquals(1, entries.size)
        assertEquals(WatchStatus.WANT_TO_WATCH, entries.first().statusEnum)
        assertTrue(entries.first().priority)

        val saveReviewResult = repository.saveReviewAndRating(
            catalogTitle = sample,
            existingEntry = entries.first(),
            entryId = sample.id,
            status = WatchStatus.WATCHED,
            userRating = 9.5,
            reviewTitle = "Masterpiece",
            reviewBody = "Stunning score and cinematography.",
            favoriteQuote = "May thy knife chip and shatter.",
            containsSpoilers = false
        )
        assertTrue(saveReviewResult.isSuccess)

        entries = repository.observeVaultEntries().first()
        val updated = entries.first()
        assertEquals(WatchStatus.WATCHED, updated.statusEnum)
        assertEquals(9.5, updated.userRating, 0.001)
        assertEquals("Masterpiece", updated.reviewTitle)
        assertTrue(updated.hasReview)
    }

    @Test
    fun createCustomTitle_andDeleteEntry_updatesFlow() = runTest {
        val createResult = repository.createCustomTitle(
            title = "Andor Season 2",
            mediaType = MediaType.SERIES,
            status = WatchStatus.WANT_TO_WATCH,
            releaseYear = 2025,
            genres = listOf("Sci-Fi", "Thriller"),
            directorOrCreator = "Tony Gilroy",
            runtimeOrSeasons = "1 Season · 12 Episodes",
            synopsis = "The road to Rogue One.",
            posterKey = "scifi",
            totalEpisodes = 12,
            isPriority = true
        )
        assertTrue(createResult.isSuccess)
        val createdId = createResult.getOrThrow()

        val fetched = repository.getVaultEntry(createdId).getOrNull()
        assertNotNull(fetched)
        assertEquals("Andor Season 2", fetched?.title)
        assertEquals(12L, fetched?.totalEpisodes)
        assertTrue(fetched?.customTitle == true)

        val deleteResult = repository.deleteEntry(createdId)
        assertTrue(deleteResult.isSuccess)
        assertNull(repository.getVaultEntry(createdId).getOrNull())
        assertFalse(repository.observeVaultEntries().first().any { it.id == createdId })
    }

    @Test
    fun ratingScale_returnsExactLabelsForOneThroughTen() {
        val expected = mapOf(
            10 to "Masterpiece",
            9 to "Excellent",
            8 to "Great",
            7 to "Very Good",
            6 to "Good",
            5 to "Average",
            4 to "Mediocre",
            3 to "Bad",
            2 to "Very Bad",
            1 to "Awful",
            0 to "Unrated"
        )
        expected.forEach { (score, label) ->
            assertEquals(label, com.example.data.model.RatingScale.labelFor(score))
        }
        assertEquals("Masterpiece", com.example.data.model.RatingScale.labelFor(9.8))
        assertEquals("Good", com.example.data.model.RatingScale.labelFor(6.0))
        assertEquals("Unrated", com.example.data.model.RatingScale.labelFor(0.0))
    }

    @Test
    fun watchedAt_andSeriesEpisodes_persistAcrossMarkSeenFlows() = runTest {
        val series = CatalogRepository.allTitles().first { it.mediaType == MediaType.SERIES && it.totalEpisodes > 0 }
        val customWatchedDate = 1735689600000L // Jan 1, 2025 UTC

        repository.upsertFromCatalog(series, WatchStatus.WANT_TO_WATCH, isPriority = true)
        val existing = repository.getVaultEntry(series.id).getOrNull()!!
        assertEquals(0L, existing.episodesWatched)

        val res = repository.saveReviewAndRating(
            catalogTitle = series,
            existingEntry = existing,
            entryId = series.id,
            status = WatchStatus.WATCHED,
            userRating = 8.0,
            reviewTitle = "Gripping season",
            reviewBody = "",
            favoriteQuote = "",
            containsSpoilers = false,
            watchedAt = customWatchedDate
        )
        assertTrue(res.isSuccess)

        val updated = repository.getVaultEntry(series.id).getOrNull()!!
        assertEquals(WatchStatus.WATCHED, updated.statusEnum)
        assertFalse(updated.priority)
        assertEquals(series.totalEpisodes.toLong(), updated.episodesWatched)
        assertEquals(customWatchedDate, updated.watchedAt)
        assertEquals(customWatchedDate, updated.effectiveWatchedAt)
        assertEquals(8.0, updated.userRating, 0.001)
    }

    @Test
    fun datePickerHelpers_roundTripLocalDayCleanly() {
        val today = com.example.ui.components.todayLocalNoonMillis()
        val utc = com.example.ui.components.localMillisToUtcDatePickerMillis(today)
        val backToLocal = com.example.ui.components.utcDatePickerMillisToLocalNoonMillis(utc)
        assertTrue(com.example.ui.components.isSameLocalDay(today, backToLocal))
        assertFalse(com.example.ui.components.isSameLocalDay(today, today - 3L * 24L * 60L * 60L * 1000L))
    }

    @Test
    fun releaseValidator_distinguishesReleasedAndUpcomingTitles() {
        val today = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.NOVEMBER, 15, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertTrue(
            com.example.data.model.ReleaseValidator.isReleased(
                releaseDate = "2024-03-01",
                releaseYear = 2024,
                todayMillis = today
            )
        )
        assertTrue(
            com.example.data.model.ReleaseValidator.isReleased(
                releaseDate = "2026-11-15",
                releaseYear = 2026,
                todayMillis = today
            )
        )
        assertFalse(
            com.example.data.model.ReleaseValidator.isReleased(
                releaseDate = "2026-11-16",
                releaseYear = 2026,
                todayMillis = today
            )
        )
        assertFalse(
            com.example.data.model.ReleaseValidator.isReleased(
                releaseDate = "",
                releaseYear = 2028,
                todayMillis = today
            )
        )
        assertFalse(
            com.example.data.model.ReleaseValidator.isReleased(
                releaseDate = "",
                releaseYear = 2026,
                statusText = "In Production",
                todayMillis = today
            )
        )
    }

    @Test
    fun upsertFromCatalog_persistsReleaseDateLanguageAndStudio() = runTest {
        val upcoming = CatalogRepository.upcomingFallback().first { !it.isReleased }
        assertFalse(upcoming.isReleased)

        val result = repository.upsertFromCatalog(
            catalogTitle = upcoming,
            targetStatus = WatchStatus.WANT_TO_WATCH,
            isPriority = true
        )
        assertTrue(result.isSuccess)

        val saved = repository.getVaultEntry(upcoming.id).getOrNull()!!
        assertEquals(upcoming.releaseDate, saved.releaseDate)
        assertEquals(upcoming.language, saved.language)
        assertFalse(saved.isReleased)
    }
}

