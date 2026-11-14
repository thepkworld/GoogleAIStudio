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
}
