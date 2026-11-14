package com.example.data.model

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

enum class TmdbSection(val label: String, val mediaHint: MediaType?) {
    TRENDING_ALL("Trending", null),
    TRENDING_MOVIES("Trending Films", MediaType.MOVIE),
    TRENDING_TV("Trending Series", MediaType.SERIES),
    MOVIE_POPULAR("Popular Films", MediaType.MOVIE),
    MOVIE_TOP_RATED("Top Rated Films", MediaType.MOVIE),
    MOVIE_NOW_PLAYING("In Theatres", MediaType.MOVIE),
    MOVIE_UPCOMING("Upcoming Films", MediaType.MOVIE),
    TV_POPULAR("Popular Series", MediaType.SERIES),
    TV_TOP_RATED("Top Rated Series", MediaType.SERIES),
    TV_ON_THE_AIR("On The Air", MediaType.SERIES),
    TV_AIRING_TODAY("Airing Today", MediaType.SERIES)
}

enum class DiscoverSort(val label: String, val movieParam: String, val tvParam: String) {
    POPULARITY("Most Popular", "popularity.desc", "popularity.desc"),
    TOP_RATED("Highest Rated", "vote_average.desc", "vote_average.desc"),
    NEWEST("Newest Releases", "primary_release_date.desc", "first_air_date.desc"),
    MOST_VOTED("Most Voted", "vote_count.desc", "vote_count.desc")
}

enum class SearchScope(val label: String) {
    MULTI("All"),
    MOVIES("Movies"),
    SERIES("Series"),
    PEOPLE("People"),
    COLLECTIONS("Collections")
}

enum class OttPlatform(val label: String, val providerIds: String, val region: String) {
    ALL("All OTT", "8|119|337|350|1899", "US"),
    NETFLIX("Netflix", "8", "US"),
    PRIME("Prime Video", "119", "US"),
    DISNEY("Disney+", "337", "US"),
    APPLE_TV("Apple TV+", "350", "US"),
    MAX("Max", "1899", "US"),
    HOTSTAR("JioHotstar", "122", "IN")
}

object RatingScale {
    val allLevels: List<Pair<Int, String>> = listOf(
        10 to "Masterpiece",
        9 to "Excellent",
        8 to "Great",
        7 to "Very Good",
        6 to "Good",
        5 to "Average",
        4 to "Mediocre",
        3 to "Bad",
        2 to "Very Bad",
        1 to "Awful"
    )

    fun labelFor(score: Int): String = when (score) {
        10 -> "Masterpiece"
        9 -> "Excellent"
        8 -> "Great"
        7 -> "Very Good"
        6 -> "Good"
        5 -> "Average"
        4 -> "Mediocre"
        3 -> "Bad"
        2 -> "Very Bad"
        1 -> "Awful"
        else -> "Unrated"
    }

    fun labelFor(score: Double): String {
        if (score <= 0.0) return "Unrated"
        return labelFor(kotlin.math.round(score).toInt().coerceIn(1, 10))
    }
}

data class CastMember(
    val actor: String,
    val role: String,
    val profileUrl: String = "",
    val id: Int = 0
)

data class CrewMember(
    val id: Int = 0,
    val name: String,
    val job: String,
    val department: String = ""
)

data class TmdbReviewItem(
    val id: String,
    val author: String,
    val username: String = "",
    val rating: Double? = null,
    val content: String,
    val createdAt: String = ""
)

data class TmdbVideoItem(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean = false
) {
    val youtubeUrl: String?
        get() = if (site.equals("YouTube", ignoreCase = true) && key.isNotBlank()) {
            "https://www.youtube.com/watch?v=$key"
        } else {
            null
        }
}

data class TmdbGenre(
    val id: Int,
    val name: String,
    val mediaType: MediaType
)

data class WatchProviderItem(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val tier: String // "Stream", "Rent", "Buy"
)

data class CollectionSummary(
    val id: Int,
    val name: String,
    val posterUrl: String = "",
    val backdropUrl: String = ""
)

data class CollectionDetail(
    val id: Int,
    val name: String,
    val overview: String,
    val posterUrl: String,
    val backdropUrl: String,
    val parts: List<CatalogTitle>
)

data class SeasonSummary(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val airDate: String,
    val posterUrl: String,
    val overview: String
)

data class EpisodeItem(
    val id: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String,
    val airDate: String,
    val runtimeMinutes: Int,
    val voteAverage: Double,
    val stillUrl: String
)

data class SeasonDetail(
    val seasonNumber: Int,
    val name: String,
    val overview: String,
    val airDate: String,
    val posterUrl: String,
    val episodes: List<EpisodeItem>
)

data class PersonSummary(
    val id: Int,
    val name: String,
    val knownForDepartment: String,
    val profileUrl: String,
    val knownForTitles: String = ""
)

data class PersonDetail(
    val id: Int,
    val name: String,
    val biography: String,
    val birthday: String,
    val deathday: String,
    val placeOfBirth: String,
    val knownForDepartment: String,
    val profileUrl: String,
    val photos: List<String>,
    val credits: List<CatalogTitle>
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
    val cast: List<CastMember> = emptyList(),
    val awardsHighlight: String = "",
    val isFeatured: Boolean = false,
    val tmdbId: Int? = null,
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val releaseDate: String = "",
    val statusText: String = "",
    val budget: Long = 0L,
    val revenue: Long = 0L,
    val crew: List<CrewMember> = emptyList(),
    val tmdbReviews: List<TmdbReviewItem> = emptyList(),
    val videos: List<TmdbVideoItem> = emptyList(),
    val backdrops: List<String> = emptyList(),
    val recommendations: List<CatalogTitle> = emptyList(),
    val similarTitles: List<CatalogTitle> = emptyList(),
    val watchProviders: List<WatchProviderItem> = emptyList(),
    val keywords: List<String> = emptyList(),
    val collection: CollectionSummary? = null,
    val seasons: List<SeasonSummary> = emptyList()
)

data class VaultEntry(
    val id: String = "",
    val catalogId: String = "",
    val tmdbId: Int? = null,
    val title: String = "",
    val mediaType: String = MediaType.MOVIE.value,
    val status: String = WatchStatus.WANT_TO_WATCH.value,
    val priority: Boolean = false,
    val releaseYear: Long = 2024L,
    val genres: List<String> = emptyList(),
    val directorOrCreator: String = "",
    val runtimeOrSeasons: String = "",
    val synopsis: String = "",
    val posterKey: String = "cinema",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val userRating: Double = 0.0,
    val reviewTitle: String = "",
    val reviewBody: String = "",
    val favoriteQuote: String = "",
    val containsSpoilers: Boolean = false,
    val rewatchCount: Long = 0L,
    val episodesWatched: Long = 0L,
    val totalEpisodes: Long = 0L,
    val customTitle: Boolean = false,
    val watchedAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val mediaTypeEnum: MediaType
        get() = MediaType.fromValue(mediaType)

    val statusEnum: WatchStatus
        get() = WatchStatus.fromValue(status)

    val hasReview: Boolean
        get() = reviewBody.isNotBlank() || reviewTitle.isNotBlank() || favoriteQuote.isNotBlank()

    val effectiveWatchedAt: Long
        get() = when {
            watchedAt > 0L -> watchedAt
            updatedAt > 0L -> updatedAt
            else -> createdAt
        }
}
