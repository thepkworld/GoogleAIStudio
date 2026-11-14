package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class VaultRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userVaultCollection(uid: String) =
        db.collection("users").document(uid).collection("vaultEntries")

    fun observeVaultEntries(userId: String): Flow<List<VaultEntry>> = flow {
        val path = "users/$userId/vaultEntries"
        emitAll(
            userVaultCollection(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        VaultEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path, auth)
                    }
                    throw error
                }
        )
    }

    suspend fun getVaultEntry(entryId: String): Result<VaultEntry?> {
        val uid = requireUserId()
        val docRef = userVaultCollection(uid).document(entryId)
        return try {
            val snapshot = docRef.get().await()
            val entry = if (snapshot.exists()) {
                snapshot.toObject(
                    VaultEntry::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
            Result.success(entry)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
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
        containsSpoilers: Boolean = false
    ): Result<String> {
        val uid = requireUserId()
        val docRef = userVaultCollection(uid).document(catalogTitle.id)
        return try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val episodesWatched = if (targetStatus == WatchStatus.WATCHED && catalogTitle.mediaType == MediaType.SERIES) {
                    catalogTitle.totalEpisodes.toLong()
                } else {
                    0L
                }
                val payload = mapOf(
                    "userId" to uid,
                    "catalogId" to catalogTitle.id,
                    "title" to catalogTitle.title.take(160),
                    "mediaType" to catalogTitle.mediaType.value,
                    "status" to targetStatus.value,
                    "priority" to isPriority,
                    "releaseYear" to catalogTitle.releaseYear.toLong(),
                    "genres" to catalogTitle.genres.take(10),
                    "directorOrCreator" to catalogTitle.directorOrCreator.take(120),
                    "runtimeOrSeasons" to catalogTitle.runtimeOrSeasons.take(60),
                    "synopsis" to catalogTitle.synopsis.take(1500),
                    "posterKey" to catalogTitle.posterKey.take(40),
                    "userRating" to userRating.coerceIn(0.0, 10.0),
                    "reviewTitle" to reviewTitle.take(160),
                    "reviewBody" to reviewBody.take(4000),
                    "favoriteQuote" to favoriteQuote.take(400),
                    "containsSpoilers" to containsSpoilers,
                    "rewatchCount" to 0L,
                    "episodesWatched" to episodesWatched,
                    "totalEpisodes" to catalogTitle.totalEpisodes.toLong(),
                    "customTitle" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "status" to targetStatus.value,
                    "priority" to if (targetStatus == WatchStatus.WATCHED) false else isPriority,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(catalogTitle.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
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
        isPriority: Boolean = false
    ): Result<String> {
        val uid = requireUserId()
        val entryId = "custom_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = userVaultCollection(uid).document(entryId)
        val clampedTotalEpisodes = totalEpisodes.coerceIn(0, 5000).toLong()
        val episodesWatched = if (status == WatchStatus.WATCHED && mediaType == MediaType.SERIES) {
            clampedTotalEpisodes
        } else {
            0L
        }
        val payload = mapOf(
            "userId" to uid,
            "catalogId" to entryId,
            "title" to title.trim().take(160),
            "mediaType" to mediaType.value,
            "status" to status.value,
            "priority" to (isPriority && status == WatchStatus.WANT_TO_WATCH),
            "releaseYear" to releaseYear.coerceIn(1888, 2100).toLong(),
            "genres" to genres.map { it.trim() }.filter { it.isNotEmpty() }.take(10),
            "directorOrCreator" to directorOrCreator.trim().take(120),
            "runtimeOrSeasons" to runtimeOrSeasons.trim().take(60),
            "synopsis" to synopsis.trim().take(1500),
            "posterKey" to posterKey.ifBlank { "cinema" }.take(40),
            "userRating" to userRating.coerceIn(0.0, 10.0),
            "reviewTitle" to reviewTitle.trim().take(160),
            "reviewBody" to reviewBody.trim().take(4000),
            "favoriteQuote" to favoriteQuote.trim().take(400),
            "containsSpoilers" to containsSpoilers,
            "rewatchCount" to 0L,
            "episodesWatched" to episodesWatched,
            "totalEpisodes" to clampedTotalEpisodes,
            "customTitle" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(entryId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateStatusAndProgress(
        entryId: String,
        status: WatchStatus,
        isPriority: Boolean,
        episodesWatched: Long,
        rewatchCount: Long
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = userVaultCollection(uid).document(entryId)
        val updates = mapOf(
            "status" to status.value,
            "priority" to isPriority,
            "episodesWatched" to episodesWatched.coerceIn(0L, 5000L),
            "rewatchCount" to rewatchCount.coerceIn(0L, 1000L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
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
        containsSpoilers: Boolean
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = userVaultCollection(uid).document(entryId)
        return try {
            if (existingEntry == null && catalogTitle != null) {
                val episodesWatched = if (status == WatchStatus.WATCHED && catalogTitle.mediaType == MediaType.SERIES) {
                    catalogTitle.totalEpisodes.toLong()
                } else {
                    0L
                }
                val payload = mapOf(
                    "userId" to uid,
                    "catalogId" to catalogTitle.id,
                    "title" to catalogTitle.title.take(160),
                    "mediaType" to catalogTitle.mediaType.value,
                    "status" to status.value,
                    "priority" to false,
                    "releaseYear" to catalogTitle.releaseYear.toLong(),
                    "genres" to catalogTitle.genres.take(10),
                    "directorOrCreator" to catalogTitle.directorOrCreator.take(120),
                    "runtimeOrSeasons" to catalogTitle.runtimeOrSeasons.take(60),
                    "synopsis" to catalogTitle.synopsis.take(1500),
                    "posterKey" to catalogTitle.posterKey.take(40),
                    "userRating" to userRating.coerceIn(0.0, 10.0),
                    "reviewTitle" to reviewTitle.trim().take(160),
                    "reviewBody" to reviewBody.trim().take(4000),
                    "favoriteQuote" to favoriteQuote.trim().take(400),
                    "containsSpoilers" to containsSpoilers,
                    "rewatchCount" to 0L,
                    "episodesWatched" to episodesWatched,
                    "totalEpisodes" to catalogTitle.totalEpisodes.toLong(),
                    "customTitle" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mapOf(
                    "status" to status.value,
                    "userRating" to userRating.coerceIn(0.0, 10.0),
                    "reviewTitle" to reviewTitle.trim().take(160),
                    "reviewBody" to reviewBody.trim().take(4000),
                    "favoriteQuote" to favoriteQuote.trim().take(400),
                    "containsSpoilers" to containsSpoilers,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteEntry(entryId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = userVaultCollection(uid).document(entryId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            Result.failure(e)
        }
    }
}
