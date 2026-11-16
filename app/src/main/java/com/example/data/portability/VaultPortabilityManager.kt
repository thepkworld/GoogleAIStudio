package com.example.data.portability

import com.example.data.model.MediaType
import com.example.data.model.VaultEntry
import com.example.data.model.WatchStatus
import com.example.ui.VaultStats
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class VaultImportPackage(
    val entries: List<VaultEntry>,
    val exportedAtMillis: Long
)

object VaultPortabilityManager {
    private const val SCHEMA_VERSION = 1

    fun exportToJson(
        entries: List<VaultEntry>,
        stats: VaultStats,
        exportedAtMillis: Long = System.currentTimeMillis()
    ): String {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("appName", "CinemaPaglu")
        root.put("exportedAt", exportedAtMillis)
        root.put(
            "exportedAtFormatted",
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(exportedAtMillis))
        )

        val statsObj = JSONObject().apply {
            put("totalWatched", stats.totalWatched)
            put("watchedMovies", stats.watchedMovies)
            put("watchedSeries", stats.watchedSeries)
            put("watchedThisMonth", stats.watchedThisMonth)
            put("currentMonthLabel", stats.currentMonthLabel)
            put("totalBucketlist", stats.totalWatchlist)
            put("priorityBucketlist", stats.priorityWatchlist)
            put("averageRating", stats.averageRating)
            put("ratedCount", stats.ratedCount)
            put("totalReviews", stats.totalReviews)
        }
        root.put("statsSummary", statsObj)

        val entriesArray = JSONArray()
        entries.forEach { entry ->
            entriesArray.put(entryToJson(entry))
        }
        root.put("entries", entriesArray)

        return root.toString(2)
    }

    fun importFromJson(rawJson: String): Result<VaultImportPackage> {
        return try {
            val trimmed = rawJson.trim()
            if (trimmed.isEmpty()) {
                return Result.failure(IllegalArgumentException("Backup file is empty."))
            }

            val exportedAt: Long
            val entriesArray: JSONArray
            if (trimmed.startsWith("[")) {
                exportedAt = 0L
                entriesArray = JSONArray(trimmed)
            } else {
                val root = JSONObject(trimmed)
                if (!root.has("entries")) {
                    return Result.failure(IllegalArgumentException("Invalid CinemaPaglu backup: missing 'entries' list."))
                }
                exportedAt = root.optLong("exportedAt", 0L)
                entriesArray = root.getJSONArray("entries")
            }

            val parsed = mutableListOf<VaultEntry>()
            val seenIds = HashSet<String>()
            for (i in 0 until entriesArray.length()) {
                val obj = entriesArray.optJSONObject(i) ?: continue
                val entry = jsonToEntry(obj) ?: continue
                if (seenIds.add(entry.id)) {
                    parsed.add(entry)
                }
            }

            Result.success(VaultImportPackage(entries = parsed, exportedAtMillis = exportedAt))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Could not parse backup JSON: ${e.localizedMessage ?: "invalid format"}"))
        }
    }

    private fun entryToJson(entry: VaultEntry): JSONObject {
        val genresArray = JSONArray()
        entry.genres.forEach { genresArray.put(it) }

        return JSONObject().apply {
            put("id", entry.id)
            put("catalogId", entry.catalogId)
            if (entry.tmdbId != null) {
                put("tmdbId", entry.tmdbId)
            } else {
                put("tmdbId", JSONObject.NULL)
            }
            put("title", entry.title)
            put("mediaType", entry.mediaTypeEnum.value)
            put("status", entry.statusEnum.value)
            put("priority", entry.priority)
            put("releaseYear", entry.releaseYear)
            put("genres", genresArray)
            put("directorOrCreator", entry.directorOrCreator)
            put("runtimeOrSeasons", entry.runtimeOrSeasons)
            put("synopsis", entry.synopsis)
            put("posterKey", entry.posterKey)
            put("posterUrl", entry.posterUrl)
            put("backdropUrl", entry.backdropUrl)
            put("userRating", entry.userRating)
            put("reviewTitle", entry.reviewTitle)
            put("reviewBody", entry.reviewBody)
            put("favoriteQuote", entry.favoriteQuote)
            put("containsSpoilers", entry.containsSpoilers)
            put("rewatchCount", entry.rewatchCount)
            put("episodesWatched", entry.episodesWatched)
            put("totalEpisodes", entry.totalEpisodes)
            put("customTitle", entry.customTitle)
            put("releaseDate", entry.releaseDate)
            put("language", entry.language)
            put("studioOrNetwork", entry.studioOrNetwork)
            put("watchedAt", entry.watchedAt)
            put("createdAt", entry.createdAt)
            put("updatedAt", entry.updatedAt)
        }
    }

    private fun jsonToEntry(obj: JSONObject): VaultEntry? {
        val rawTitle = obj.optString("title", "").trim()
        if (rawTitle.isEmpty()) return null

        val now = System.currentTimeMillis()
        val rawId = obj.optString("id", "").trim()
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(128)
        val safeId = rawId.ifBlank {
            "import_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        }
        val safeCatalogId = obj.optString("catalogId", safeId).trim()
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .ifBlank { safeId }
            .take(128)

        val tmdbId = if (obj.has("tmdbId") && !obj.isNull("tmdbId")) {
            obj.optInt("tmdbId", -1).takeIf { it > 0 }
        } else {
            null
        }

        val genresList = mutableListOf<String>()
        val rawGenres = obj.optJSONArray("genres")
        if (rawGenres != null) {
            for (g in 0 until rawGenres.length()) {
                val label = rawGenres.optString(g, "").trim()
                if (label.isNotEmpty() && genresList.size < 10) {
                    genresList.add(label)
                }
            }
        }

        val rawStatus = obj.optString("status", WatchStatus.WANT_TO_WATCH.value).trim()
        val normalizedStatus = if (rawStatus.equals(WatchStatus.WATCHED.value, ignoreCase = true)) {
            WatchStatus.WATCHED.value
        } else {
            WatchStatus.WANT_TO_WATCH.value
        }

        val rawMediaType = obj.optString("mediaType", MediaType.MOVIE.value).trim()
        val normalizedMediaType = if (
            rawMediaType.equals(MediaType.SERIES.value, ignoreCase = true) ||
            rawMediaType.equals("tv", ignoreCase = true)
        ) {
            MediaType.SERIES.value
        } else {
            MediaType.MOVIE.value
        }

        val createdAt = obj.optLong("createdAt", now).let { if (it > 0L) it else now }
        val updatedAt = obj.optLong("updatedAt", createdAt).let { if (it > 0L) it else createdAt }

        return VaultEntry(
            id = safeId,
            catalogId = safeCatalogId,
            tmdbId = tmdbId,
            title = rawTitle.take(160),
            mediaType = normalizedMediaType,
            status = normalizedStatus,
            priority = obj.optBoolean("priority", false) && normalizedStatus == WatchStatus.WANT_TO_WATCH.value,
            releaseYear = obj.optLong("releaseYear", 2024L).coerceIn(1888L, 2100L),
            genres = genresList,
            directorOrCreator = obj.optString("directorOrCreator", "").take(120),
            runtimeOrSeasons = obj.optString("runtimeOrSeasons", "").take(60),
            synopsis = obj.optString("synopsis", "").take(1500),
            posterKey = obj.optString("posterKey", "cinema").ifBlank { "cinema" }.take(40),
            posterUrl = obj.optString("posterUrl", "").take(500),
            backdropUrl = obj.optString("backdropUrl", "").take(500),
            userRating = obj.optDouble("userRating", 0.0).let { if (it.isNaN()) 0.0 else it.coerceIn(0.0, 10.0) },
            reviewTitle = obj.optString("reviewTitle", "").take(160),
            reviewBody = obj.optString("reviewBody", "").take(4000),
            favoriteQuote = obj.optString("favoriteQuote", "").take(400),
            containsSpoilers = obj.optBoolean("containsSpoilers", false),
            rewatchCount = obj.optLong("rewatchCount", 0L).coerceIn(0L, 1000L),
            episodesWatched = obj.optLong("episodesWatched", 0L).coerceIn(0L, 5000L),
            totalEpisodes = obj.optLong("totalEpisodes", 0L).coerceIn(0L, 5000L),
            customTitle = obj.optBoolean("customTitle", false),
            releaseDate = obj.optString("releaseDate", "").take(30),
            language = obj.optString("language", "").take(60),
            studioOrNetwork = obj.optString("studioOrNetwork", "").take(120),
            watchedAt = obj.optLong("watchedAt", 0L).coerceAtLeast(0L),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
