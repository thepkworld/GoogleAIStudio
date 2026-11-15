package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.VaultEntry
import com.example.data.remote.VaultCloudRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
class VaultCloudRuleTest : FirestoreEmulatorTestBase() {

    private fun sampleEntry(id: String = "dune_part_two"): VaultEntry = VaultEntry(
        id = id,
        catalogId = id,
        tmdbId = 693134,
        title = "Dune: Part Two",
        mediaType = "movie",
        status = "want_to_watch",
        priority = true,
        releaseYear = 2024L,
        genres = listOf("Sci-Fi", "Adventure"),
        directorOrCreator = "Denis Villeneuve",
        runtimeOrSeasons = "2h 46m",
        synopsis = "Paul Atreides unites with the Fremen.",
        posterKey = "scifi",
        posterUrl = "https://image.tmdb.org/t/p/w500/dune2.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w780/dune2_bg.jpg",
        userRating = 0.0,
        reviewTitle = "",
        reviewBody = "",
        favoriteQuote = "",
        containsSpoilers = false,
        rewatchCount = 0L,
        episodesWatched = 0L,
        totalEpisodes = 0L,
        customTitle = false,
        releaseDate = "2024-02-27",
        language = "English",
        studioOrNetwork = "Warner Bros.",
        watchedAt = 0L
    )

    @Test
    fun authenticatedUser_canUpsertFetchUpdateAndDeleteOwnEntry() = runBlocking {
        withTimeout(15_000L) {
            val uid = signInAsTestUser("alice@example.com", "Alice")
            val cloudRepo = VaultCloudRepository(firestore, auth)

            val initial = sampleEntry()
            val createRes = cloudRepo.upsertEntry(uid, initial)
            assertTrue("Expected upsert to succeed: ${createRes.exceptionOrNull()}", createRes.isSuccess)

            val fetched = cloudRepo.fetchUserEntries(uid).getOrThrow()
            assertEquals(1, fetched.size)
            assertEquals("Dune: Part Two", fetched.first().title)

            val updated = initial.copy(
                status = "watched",
                userRating = 9.0,
                reviewTitle = "Visually arresting",
                watchedAt = System.currentTimeMillis() - 10_000L
            )
            val updateRes = cloudRepo.upsertEntry(uid, updated)
            assertTrue("Expected update to succeed: ${updateRes.exceptionOrNull()}", updateRes.isSuccess)

            val afterUpdate = cloudRepo.fetchUserEntries(uid).getOrThrow()
            assertEquals("watched", afterUpdate.first().status)
            assertEquals(9.0, afterUpdate.first().userRating, 0.01)

            val deleteRes = cloudRepo.deleteEntry(uid, initial.id)
            assertTrue("Expected delete to succeed: ${deleteRes.exceptionOrNull()}", deleteRes.isSuccess)
        }
    }

    @Test
    fun unauthenticatedUser_isDeniedByRules() = runBlocking {
        withTimeout(15_000L) {
            val aliceUid = signInAsTestUser("alice@example.com", "Alice")
            val cloudRepo = VaultCloudRepository(firestore, auth)
            cloudRepo.upsertEntry(aliceUid, sampleEntry()).getOrThrow()

            auth.signOut()

            val anonRead = cloudRepo.fetchUserEntries(aliceUid)
            assertTrue("Expected unauthenticated read to fail", anonRead.isFailure)
            val readException = anonRead.exceptionOrNull() as? FirebaseFirestoreException
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, readException?.code)

            val anonWrite = cloudRepo.upsertEntry(aliceUid, sampleEntry("anon_item"))
            assertTrue("Expected unauthenticated write to fail", anonWrite.isFailure)
            val writeException = anonWrite.exceptionOrNull() as? FirebaseFirestoreException
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, writeException?.code)
        }
    }

    @Test
    fun crossUserAccessAndSpoofedOwnership_areDeniedByRules() = runBlocking {
        withTimeout(15_000L) {
            val aliceUid = signInAsTestUser("alice@example.com", "Alice")
            val cloudRepo = VaultCloudRepository(firestore, auth)
            cloudRepo.upsertEntry(aliceUid, sampleEntry()).getOrThrow()

            // Sign in as Bob and try to read or overwrite Alice's vault
            val bobUid = signInAsTestUser("bob@example.com", "Bob")
            val crossRead = cloudRepo.fetchUserEntries(aliceUid)
            assertTrue(crossRead.isFailure)
            val readException = crossRead.exceptionOrNull() as? FirebaseFirestoreException
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, readException?.code)

            val crossWrite = cloudRepo.upsertEntry(aliceUid, sampleEntry("hacked_item"))
            assertTrue(crossWrite.isFailure)

            // Direct spoofed userId write into Bob's own path should fail
            val spoofResult = runCatching {
                firestore.collection("users").document(bobUid)
                    .collection("vaultEntries").document("spoof_item")
                    .set(
                        mapOf(
                            "userId" to aliceUid,
                            "catalogId" to "spoof_item",
                            "title" to "Spoofed"
                        )
                    ).await()
            }
            assertTrue(spoofResult.isFailure)
        }
    }
}
