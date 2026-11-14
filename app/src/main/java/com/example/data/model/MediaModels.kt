package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MediaType(val value: String, val label: String) {
    MOVIE("movie", "Movie"),
    SERIES("series", "Series");

    companion object {
        fun fromValue(value: String): MediaType =
            entries.firstOrNull { it.value == value } ?: MOVIE
    }
}

enum class WatchStatus(val value: String, val label: String) {
    WANT_TO_WATCH("want_to_watch", "Want to Watch"),
    WATCHED("watched", "Watched");

    companion object {
        fun fromValue(value: String): WatchStatus =
            entries.firstOrNull { it.value == value } ?: WANT_TO_WATCH
    }
}

data class CastMember(
    val actor: String,
    val role: String
)

data class CatalogTitle(
    val id: String,
    val title: String,
    val tagline: String,
    val mediaType: MediaType,
    val releaseYear: Int,
    val genres: List<String>,
    val directorOrCreator: String,
    val studioOrNetwork: String,
    val country: String,
    val language: String,
    val ageRating: String,
    val runtimeOrSeasons: String,
    val totalEpisodes: Int,
    val criticScore: Double,
    val synopsis: String,
    val posterKey: String,
    val cast: List<CastMember>,
    val awardsHighlight: String = "",
    val isFeatured: Boolean = false
)

@IgnoreExtraProperties
data class VaultEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val catalogId: String = "",
    val title: String = "",
    val mediaType: String = MediaType.MOVIE.value,
    val status: String = WatchStatus.WANT_TO_WATCH.value,
    val priority: Boolean = false,
    val releaseYear: Long = 2024L,
    val genres: List<String> = emptyList(),
    val directorOrCreator: String = "",
    val runtimeOrSeasons: String = "",
    val synopsis: String = "",
    val posterKey: String = "scifi",
    val userRating: Double = 0.0,
    val reviewTitle: String = "",
    val reviewBody: String = "",
    val favoriteQuote: String = "",
    val containsSpoilers: Boolean = false,
    val rewatchCount: Long = 0L,
    val episodesWatched: Long = 0L,
    val totalEpisodes: Long = 0L,
    val customTitle: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val mediaTypeEnum: MediaType
        get() = MediaType.fromValue(mediaType)

    val statusEnum: WatchStatus
        get() = WatchStatus.fromValue(status)

    val hasReview: Boolean
        get() = reviewBody.isNotBlank() || reviewTitle.isNotBlank() || favoriteQuote.isNotBlank()
}
