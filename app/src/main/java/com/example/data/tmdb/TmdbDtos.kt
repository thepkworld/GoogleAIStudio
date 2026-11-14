package com.example.data.tmdb

import com.squareup.moshi.Json

data class TmdbPagedResponse<T>(
    @param:Json(name = "page") val page: Int = 1,
    @param:Json(name = "results") val results: List<T> = emptyList(),
    @param:Json(name = "total_pages") val totalPages: Int = 1,
    @param:Json(name = "total_results") val totalResults: Int = 0
)

data class TmdbMediaDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "media_type") val mediaType: String? = null,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "original_title") val originalTitle: String? = null,
    @param:Json(name = "original_name") val originalName: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null,
    @param:Json(name = "release_date") val releaseDate: String? = null,
    @param:Json(name = "first_air_date") val firstAirDate: String? = null,
    @param:Json(name = "vote_average") val voteAverage: Double = 0.0,
    @param:Json(name = "vote_count") val voteCount: Int = 0,
    @param:Json(name = "popularity") val popularity: Double = 0.0,
    @param:Json(name = "genre_ids") val genreIds: List<Int> = emptyList(),
    @param:Json(name = "original_language") val originalLanguage: String? = null,
    @param:Json(name = "character") val character: String? = null,
    @param:Json(name = "job") val job: String? = null
)

data class TmdbGenreListResponse(
    @param:Json(name = "genres") val genres: List<TmdbGenreDto> = emptyList()
)

data class TmdbGenreDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = ""
)

data class TmdbCollectionRefDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null
)

data class TmdbCollectionDetailDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null,
    @param:Json(name = "parts") val parts: List<TmdbMediaDto> = emptyList()
)

data class TmdbSeasonRefDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "season_number") val seasonNumber: Int = 0,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "episode_count") val episodeCount: Int = 0,
    @param:Json(name = "air_date") val airDate: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "overview") val overview: String? = null
)

data class TmdbSeasonDetailDto(
    @param:Json(name = "season_number") val seasonNumber: Int = 0,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "air_date") val airDate: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "episodes") val episodes: List<TmdbEpisodeDto> = emptyList()
)

data class TmdbEpisodeDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "episode_number") val episodeNumber: Int = 0,
    @param:Json(name = "season_number") val seasonNumber: Int = 0,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "air_date") val airDate: String? = null,
    @param:Json(name = "runtime") val runtime: Int? = null,
    @param:Json(name = "vote_average") val voteAverage: Double = 0.0,
    @param:Json(name = "still_path") val stillPath: String? = null
)

data class TmdbKeywordsResponse(
    @param:Json(name = "keywords") val keywords: List<TmdbKeywordDto>? = null,
    @param:Json(name = "results") val results: List<TmdbKeywordDto>? = null
)

data class TmdbKeywordDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = ""
)

data class TmdbWatchProvidersResponse(
    @param:Json(name = "results") val results: Map<String, TmdbWatchRegionDto> = emptyMap()
)

data class TmdbWatchRegionDto(
    @param:Json(name = "link") val link: String? = null,
    @param:Json(name = "flatrate") val flatrate: List<TmdbWatchProviderDto> = emptyList(),
    @param:Json(name = "rent") val rent: List<TmdbWatchProviderDto> = emptyList(),
    @param:Json(name = "buy") val buy: List<TmdbWatchProviderDto> = emptyList()
)

data class TmdbWatchProviderDto(
    @param:Json(name = "provider_id") val providerId: Int = 0,
    @param:Json(name = "provider_name") val providerName: String = "",
    @param:Json(name = "logo_path") val logoPath: String? = null
)

data class TmdbPersonSummaryDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "known_for_department") val knownForDepartment: String? = null,
    @param:Json(name = "profile_path") val profilePath: String? = null,
    @param:Json(name = "known_for") val knownFor: List<TmdbMediaDto> = emptyList()
)

data class TmdbPersonDetailDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "biography") val biography: String? = null,
    @param:Json(name = "birthday") val birthday: String? = null,
    @param:Json(name = "deathday") val deathday: String? = null,
    @param:Json(name = "place_of_birth") val placeOfBirth: String? = null,
    @param:Json(name = "known_for_department") val knownForDepartment: String? = null,
    @param:Json(name = "profile_path") val profilePath: String? = null,
    @param:Json(name = "combined_credits") val combinedCredits: TmdbPersonCreditsDto? = null,
    @param:Json(name = "images") val images: TmdbPersonImagesDto? = null
)

data class TmdbPersonCreditsDto(
    @param:Json(name = "cast") val cast: List<TmdbMediaDto> = emptyList(),
    @param:Json(name = "crew") val crew: List<TmdbMediaDto> = emptyList()
)

data class TmdbPersonImagesDto(
    @param:Json(name = "profiles") val profiles: List<TmdbImageDto> = emptyList()
)

data class TmdbMovieDetailDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "tagline") val tagline: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null,
    @param:Json(name = "release_date") val releaseDate: String? = null,
    @param:Json(name = "runtime") val runtime: Int? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "budget") val budget: Long = 0L,
    @param:Json(name = "revenue") val revenue: Long = 0L,
    @param:Json(name = "vote_average") val voteAverage: Double = 0.0,
    @param:Json(name = "vote_count") val voteCount: Int = 0,
    @param:Json(name = "popularity") val popularity: Double = 0.0,
    @param:Json(name = "original_language") val originalLanguage: String? = null,
    @param:Json(name = "belongs_to_collection") val belongsToCollection: TmdbCollectionRefDto? = null,
    @param:Json(name = "genres") val genres: List<TmdbGenreDto> = emptyList(),
    @param:Json(name = "production_companies") val productionCompanies: List<TmdbProductionCompanyDto> = emptyList(),
    @param:Json(name = "production_countries") val productionCountries: List<TmdbCountryDto> = emptyList(),
    @param:Json(name = "spoken_languages") val spokenLanguages: List<TmdbSpokenLanguageDto> = emptyList(),
    @param:Json(name = "credits") val credits: TmdbCreditsDto? = null,
    @param:Json(name = "reviews") val reviews: TmdbReviewsResponse? = null,
    @param:Json(name = "videos") val videos: TmdbVideosResponse? = null,
    @param:Json(name = "images") val images: TmdbImagesResponse? = null,
    @param:Json(name = "recommendations") val recommendations: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "similar") val similar: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "release_dates") val releaseDates: TmdbReleaseDatesResponse? = null,
    @param:Json(name = "watch/providers") val watchProviders: TmdbWatchProvidersResponse? = null,
    @param:Json(name = "keywords") val keywords: TmdbKeywordsResponse? = null
)

data class TmdbTvDetailDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "tagline") val tagline: String? = null,
    @param:Json(name = "overview") val overview: String? = null,
    @param:Json(name = "poster_path") val posterPath: String? = null,
    @param:Json(name = "backdrop_path") val backdropPath: String? = null,
    @param:Json(name = "first_air_date") val firstAirDate: String? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "number_of_seasons") val numberOfSeasons: Int = 0,
    @param:Json(name = "number_of_episodes") val numberOfEpisodes: Int = 0,
    @param:Json(name = "episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    @param:Json(name = "vote_average") val voteAverage: Double = 0.0,
    @param:Json(name = "vote_count") val voteCount: Int = 0,
    @param:Json(name = "popularity") val popularity: Double = 0.0,
    @param:Json(name = "original_language") val originalLanguage: String? = null,
    @param:Json(name = "genres") val genres: List<TmdbGenreDto> = emptyList(),
    @param:Json(name = "seasons") val seasons: List<TmdbSeasonRefDto> = emptyList(),
    @param:Json(name = "created_by") val createdBy: List<TmdbCreatedByDto> = emptyList(),
    @param:Json(name = "networks") val networks: List<TmdbProductionCompanyDto> = emptyList(),
    @param:Json(name = "production_companies") val productionCompanies: List<TmdbProductionCompanyDto> = emptyList(),
    @param:Json(name = "origin_country") val originCountry: List<String> = emptyList(),
    @param:Json(name = "spoken_languages") val spokenLanguages: List<TmdbSpokenLanguageDto> = emptyList(),
    @param:Json(name = "credits") val credits: TmdbCreditsDto? = null,
    @param:Json(name = "reviews") val reviews: TmdbReviewsResponse? = null,
    @param:Json(name = "videos") val videos: TmdbVideosResponse? = null,
    @param:Json(name = "images") val images: TmdbImagesResponse? = null,
    @param:Json(name = "recommendations") val recommendations: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "similar") val similar: TmdbPagedResponse<TmdbMediaDto>? = null,
    @param:Json(name = "content_ratings") val contentRatings: TmdbContentRatingsResponse? = null,
    @param:Json(name = "watch/providers") val watchProviders: TmdbWatchProvidersResponse? = null,
    @param:Json(name = "keywords") val keywords: TmdbKeywordsResponse? = null
)

data class TmdbProductionCompanyDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = ""
)

data class TmdbCountryDto(
    @param:Json(name = "iso_3166_1") val iso: String = "",
    @param:Json(name = "name") val name: String = ""
)

data class TmdbSpokenLanguageDto(
    @param:Json(name = "english_name") val englishName: String = "",
    @param:Json(name = "name") val name: String = ""
)

data class TmdbCreatedByDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = ""
)

data class TmdbCreditsDto(
    @param:Json(name = "cast") val cast: List<TmdbCastDto> = emptyList(),
    @param:Json(name = "crew") val crew: List<TmdbCrewDto> = emptyList()
)

data class TmdbCastDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "character") val character: String? = null,
    @param:Json(name = "profile_path") val profilePath: String? = null,
    @param:Json(name = "order") val order: Int = 0
)

data class TmdbCrewDto(
    @param:Json(name = "id") val id: Int = 0,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "job") val job: String? = null,
    @param:Json(name = "department") val department: String? = null
)

data class TmdbReviewsResponse(
    @param:Json(name = "results") val results: List<TmdbReviewDto> = emptyList()
)

data class TmdbReviewDto(
    @param:Json(name = "id") val id: String = "",
    @param:Json(name = "author") val author: String = "",
    @param:Json(name = "author_details") val authorDetails: TmdbAuthorDetailsDto? = null,
    @param:Json(name = "content") val content: String = "",
    @param:Json(name = "created_at") val createdAt: String? = null
)

data class TmdbAuthorDetailsDto(
    @param:Json(name = "username") val username: String? = null,
    @param:Json(name = "rating") val rating: Double? = null
)

data class TmdbVideosResponse(
    @param:Json(name = "results") val results: List<TmdbVideoDto> = emptyList()
)

data class TmdbVideoDto(
    @param:Json(name = "id") val id: String = "",
    @param:Json(name = "key") val key: String = "",
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "site") val site: String = "",
    @param:Json(name = "type") val type: String = "",
    @param:Json(name = "official") val official: Boolean = false
)

data class TmdbImagesResponse(
    @param:Json(name = "backdrops") val backdrops: List<TmdbImageDto> = emptyList(),
    @param:Json(name = "posters") val posters: List<TmdbImageDto> = emptyList()
)

data class TmdbImageDto(
    @param:Json(name = "file_path") val filePath: String = "",
    @param:Json(name = "aspect_ratio") val aspectRatio: Double = 1.77
)

data class TmdbReleaseDatesResponse(
    @param:Json(name = "results") val results: List<TmdbReleaseCountryDto> = emptyList()
)

data class TmdbReleaseCountryDto(
    @param:Json(name = "iso_3166_1") val iso: String = "",
    @param:Json(name = "release_dates") val releaseDates: List<TmdbReleaseCertificationDto> = emptyList()
)

data class TmdbReleaseCertificationDto(
    @param:Json(name = "certification") val certification: String = ""
)

data class TmdbContentRatingsResponse(
    @param:Json(name = "results") val results: List<TmdbContentRatingDto> = emptyList()
)

data class TmdbContentRatingDto(
    @param:Json(name = "iso_3166_1") val iso: String = "",
    @param:Json(name = "rating") val rating: String = ""
)
