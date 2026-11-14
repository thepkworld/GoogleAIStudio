package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.VaultEntry

@Entity(tableName = "vault_entries")
data class VaultEntryEntity(
    @PrimaryKey val id: String,
    val catalogId: String,
    val tmdbId: Int?,
    val title: String,
    val mediaType: String,
    val status: String,
    val priority: Boolean,
    val releaseYear: Long,
    val genres: List<String>,
    val directorOrCreator: String,
    val runtimeOrSeasons: String,
    val synopsis: String,
    val posterKey: String,
    val posterUrl: String,
    val backdropUrl: String,
    val userRating: Double,
    val reviewTitle: String,
    val reviewBody: String,
    val favoriteQuote: String,
    val containsSpoilers: Boolean,
    val rewatchCount: Long,
    val episodesWatched: Long,
    val totalEpisodes: Long,
    val customTitle: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): VaultEntry = VaultEntry(
        id = id,
        catalogId = catalogId,
        tmdbId = tmdbId,
        title = title,
        mediaType = mediaType,
        status = status,
        priority = priority,
        releaseYear = releaseYear,
        genres = genres,
        directorOrCreator = directorOrCreator,
        runtimeOrSeasons = runtimeOrSeasons,
        synopsis = synopsis,
        posterKey = posterKey,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        userRating = userRating,
        reviewTitle = reviewTitle,
        reviewBody = reviewBody,
        favoriteQuote = favoriteQuote,
        containsSpoilers = containsSpoilers,
        rewatchCount = rewatchCount,
        episodesWatched = episodesWatched,
        totalEpisodes = totalEpisodes,
        customTitle = customTitle,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(entry: VaultEntry): VaultEntryEntity = VaultEntryEntity(
            id = entry.id,
            catalogId = entry.catalogId,
            tmdbId = entry.tmdbId,
            title = entry.title,
            mediaType = entry.mediaType,
            status = entry.status,
            priority = entry.priority,
            releaseYear = entry.releaseYear,
            genres = entry.genres,
            directorOrCreator = entry.directorOrCreator,
            runtimeOrSeasons = entry.runtimeOrSeasons,
            synopsis = entry.synopsis,
            posterKey = entry.posterKey,
            posterUrl = entry.posterUrl,
            backdropUrl = entry.backdropUrl,
            userRating = entry.userRating,
            reviewTitle = entry.reviewTitle,
            reviewBody = entry.reviewBody,
            favoriteQuote = entry.favoriteQuote,
            containsSpoilers = entry.containsSpoilers,
            rewatchCount = entry.rewatchCount,
            episodesWatched = entry.episodesWatched,
            totalEpisodes = entry.totalEpisodes,
            customTitle = entry.customTitle,
            createdAt = entry.createdAt,
            updatedAt = entry.updatedAt
        )
    }
}
