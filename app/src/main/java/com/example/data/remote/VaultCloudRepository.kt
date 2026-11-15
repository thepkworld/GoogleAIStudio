package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.VaultEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class VaultCloudRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    constructor(context: Context) : this(
        firestore = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = FirebaseAuth.getInstance()
    )

    private fun userVaultCollection(userId: String) =
        firestore.collection("users").document(userId).collection("vaultEntries")

    fun observeUserEntries(userId: String): Flow<List<VaultEntry>> = callbackFlow {
        val path = "users/$userId/vaultEntries"
        val registration = userVaultCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(
                        runCatching {
                            handleFirestoreError(error, OperationType.LIST, path, auth)
                        }.exceptionOrNull() ?: error
                    )
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toVaultEntryOrNull() }
                    .orEmpty()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    suspend fun fetchUserEntries(userId: String): Result<List<VaultEntry>> {
        val path = "users/$userId/vaultEntries"
        return try {
            val snapshot = userVaultCollection(userId).get().await()
            val list = snapshot.documents.mapNotNull { it.toVaultEntryOrNull() }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { handleFirestoreError(e, OperationType.LIST, path, auth) }
                .fold(onSuccess = { Result.success(emptyList()) }, onFailure = { Result.failure(it) })
        }
    }

    suspend fun upsertEntry(userId: String, entry: VaultEntry): Result<Unit> {
        val docId = sanitizeDocumentId(entry.id)
        val catalogId = sanitizeDocumentId(entry.catalogId.ifBlank { docId })
        val path = "users/$userId/vaultEntries/$docId"
        val docRef = userVaultCollection(userId).document(docId)

        return try {
            val existingSnap = docRef.get().await()
            if (existingSnap.exists()) {
                val updatePayload = buildMutablePayload(entry)
                docRef.update(updatePayload).await()
            } else {
                val createPayload = buildMutablePayload(entry).apply {
                    put("userId", userId)
                    put("catalogId", catalogId)
                    put("customTitle", entry.customTitle)
                    put("createdAt", FieldValue.serverTimestamp())
                }
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val op = if (entry.createdAt > 0L) OperationType.WRITE else OperationType.CREATE
            runCatching { handleFirestoreError(e, op, path, auth) }
                .fold(onSuccess = { Result.success(Unit) }, onFailure = { Result.failure(it) })
        }
    }

    suspend fun deleteEntry(userId: String, entryId: String): Result<Unit> {
        val docId = sanitizeDocumentId(entryId)
        val path = "users/$userId/vaultEntries/$docId"
        return try {
            userVaultCollection(userId).document(docId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { handleFirestoreError(e, OperationType.DELETE, path, auth) }
                .fold(onSuccess = { Result.success(Unit) }, onFailure = { Result.failure(it) })
        }
    }

    suspend fun mergeLocalEntriesToCloud(
        userId: String,
        localEntries: List<VaultEntry>
    ): Result<Int> {
        return try {
            var syncedCount = 0
            for (entry in localEntries) {
                upsertEntry(userId, entry).getOrThrow()
                syncedCount++
            }
            Result.success(syncedCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildMutablePayload(entry: VaultEntry): MutableMap<String, Any?> {
        val cleanGenres = entry.genres.map { it.trim() }.filter { it.isNotEmpty() }.take(10)
        return hashMapOf(
            "tmdbId" to entry.tmdbId,
            "title" to entry.title.ifBlank { "Untitled" }.take(160),
            "mediaType" to entry.mediaTypeEnum.value,
            "status" to entry.statusEnum.value,
            "priority" to (entry.priority && entry.statusEnum.value == "want_to_watch"),
            "releaseYear" to entry.releaseYear.coerceIn(1888L, 2100L),
            "genres" to cleanGenres,
            "directorOrCreator" to entry.directorOrCreator.take(120),
            "runtimeOrSeasons" to entry.runtimeOrSeasons.take(60),
            "synopsis" to entry.synopsis.take(1500),
            "posterKey" to entry.posterKey.ifBlank { "cinema" }.take(40),
            "posterUrl" to entry.posterUrl.take(500),
            "backdropUrl" to entry.backdropUrl.take(500),
            "releaseDate" to entry.releaseDate.take(30),
            "language" to entry.language.take(60),
            "studioOrNetwork" to entry.studioOrNetwork.take(120),
            "watchedAt" to entry.watchedAt.coerceAtLeast(0L),
            "userRating" to entry.userRating.coerceIn(0.0, 10.0),
            "reviewTitle" to entry.reviewTitle.take(160),
            "reviewBody" to entry.reviewBody.take(4000),
            "favoriteQuote" to entry.favoriteQuote.take(400),
            "containsSpoilers" to entry.containsSpoilers,
            "rewatchCount" to entry.rewatchCount.coerceIn(0L, 1000L),
            "episodesWatched" to entry.episodesWatched.coerceIn(0L, 5000L),
            "totalEpisodes" to entry.totalEpisodes.coerceIn(0L, 5000L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun DocumentSnapshot.toVaultEntryOrNull(): VaultEntry? {
        val title = getString("title") ?: return null
        val createdTs = getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        val updatedTs = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        val rawGenres = (get("genres") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        return VaultEntry(
            id = id,
            catalogId = getString("catalogId") ?: id,
            tmdbId = getLong("tmdbId")?.toInt(),
            title = title,
            mediaType = getString("mediaType") ?: "movie",
            status = getString("status") ?: "want_to_watch",
            priority = getBoolean("priority") ?: false,
            releaseYear = getLong("releaseYear") ?: 2024L,
            genres = rawGenres,
            directorOrCreator = getString("directorOrCreator").orEmpty(),
            runtimeOrSeasons = getString("runtimeOrSeasons").orEmpty(),
            synopsis = getString("synopsis").orEmpty(),
            posterKey = getString("posterKey") ?: "cinema",
            posterUrl = getString("posterUrl").orEmpty(),
            backdropUrl = getString("backdropUrl").orEmpty(),
            userRating = getDouble("userRating") ?: 0.0,
            reviewTitle = getString("reviewTitle").orEmpty(),
            reviewBody = getString("reviewBody").orEmpty(),
            favoriteQuote = getString("favoriteQuote").orEmpty(),
            containsSpoilers = getBoolean("containsSpoilers") ?: false,
            rewatchCount = getLong("rewatchCount") ?: 0L,
            episodesWatched = getLong("episodesWatched") ?: 0L,
            totalEpisodes = getLong("totalEpisodes") ?: 0L,
            customTitle = getBoolean("customTitle") ?: false,
            releaseDate = getString("releaseDate").orEmpty(),
            language = getString("language").orEmpty(),
            studioOrNetwork = getString("studioOrNetwork").orEmpty(),
            watchedAt = getLong("watchedAt") ?: 0L,
            createdAt = createdTs?.toDate()?.time ?: 0L,
            updatedAt = updatedTs?.toDate()?.time ?: 0L
        )
    }

    companion object {
        fun sanitizeDocumentId(raw: String): String {
            val cleaned = raw.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            return cleaned.ifBlank { "entry_item" }.take(128)
        }
    }
}
