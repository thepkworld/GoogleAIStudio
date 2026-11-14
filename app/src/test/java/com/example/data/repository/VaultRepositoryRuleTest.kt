package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.catalog.CatalogRepository
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class VaultRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun uniqueCatalogId(prefix: String): String =
        "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(12)}"

    @Test
    fun upsertFromCatalog_authenticatedOwner_createsAndObservesEntry() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = VaultRepository(firestore, auth)
        val sampleTitle = CatalogRepository.featuredTitle().copy(id = uniqueCatalogId("dune"))

        val upsertResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertFromCatalog(
                catalogTitle = sampleTitle,
                targetStatus = WatchStatus.WANT_TO_WATCH,
                isPriority = true
            )
        }
        assertTrue("upsert failed: ${upsertResult.exceptionOrNull()}", upsertResult.isSuccess)

        val entries = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeVaultEntries(uid).first { list -> list.any { it.id == sampleTitle.id } }
        }
        val saved = entries.first { it.id == sampleTitle.id }
        assertEquals(sampleTitle.title, saved.title)
        assertEquals(WatchStatus.WANT_TO_WATCH.value, saved.status)
        assertTrue(saved.priority)
    }

    @Test
    fun saveReviewAndRating_authenticatedOwner_updatesReviewFields() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = VaultRepository(firestore, auth)
        val sampleTitle = CatalogRepository.allTitles()[1].copy(id = uniqueCatalogId("sev"))

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertFromCatalog(sampleTitle, WatchStatus.WANT_TO_WATCH).getOrThrow()
        }
        val existing = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getVaultEntry(sampleTitle.id).getOrThrow()
        }
        assertNotNull(existing)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveReviewAndRating(
                catalogTitle = sampleTitle,
                existingEntry = existing,
                entryId = sampleTitle.id,
                status = WatchStatus.WATCHED,
                userRating = 9.5,
                reviewTitle = "Masterpiece",
                reviewBody = "Extraordinary cinematography and pacing.",
                favoriteQuote = "Look closer.",
                containsSpoilers = false
            )
        }
        assertTrue(saveResult.isSuccess)

        val entries = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeVaultEntries(uid).first { list ->
                list.any { it.id == sampleTitle.id && it.status == WatchStatus.WATCHED.value }
            }
        }
        val updated = entries.first { it.id == sampleTitle.id }
        assertEquals(9.5, updated.userRating, 0.01)
        assertEquals("Masterpiece", updated.reviewTitle)
    }

    @Test
    fun createAndDeleteCustomTitle_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = VaultRepository(firestore, auth)

        val entryId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCustomTitle(
                title = "Midnight Solaris",
                mediaType = MediaType.SERIES,
                status = WatchStatus.WANT_TO_WATCH,
                releaseYear = 2025,
                genres = listOf("Sci-Fi", "Mystery"),
                directorOrCreator = "K. Vance",
                runtimeOrSeasons = "1 Season · 8 Episodes",
                synopsis = "An orbital station crew uncovers an acoustic signal from beneath Europa.",
                posterKey = "scifi",
                totalEpisodes = 8,
                isPriority = true
            ).getOrThrow()
        }

        val created = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeVaultEntries(uid).first { list -> list.any { it.id == entryId } }
        }
        assertTrue(created.any { it.id == entryId && it.customTitle })

        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteEntry(entryId)
        }
        assertTrue(deleteResult.isSuccess)

        val afterDelete = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getVaultEntry(entryId).getOrThrow()
        }
        assertNull(afterDelete)
    }

    @Test
    fun observeVaultEntries_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = VaultRepository(firestore, auth)
        val sampleTitle = CatalogRepository.featuredTitle().copy(id = uniqueCatalogId("cross"))
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.upsertFromCatalog(sampleTitle, WatchStatus.WANT_TO_WATCH).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = VaultRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeVaultEntries(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED when Bob observes Alice's vaultEntries")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    @Test
    fun observeVaultEntries_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = VaultRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeVaultEntries(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated caller observes vaultEntries")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_cine@test.com"
        const val BOB_EMAIL = "bob_cine@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
