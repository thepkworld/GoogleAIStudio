package com.example.data.omdb

import com.squareup.moshi.Json

data class OmdbRatingDto(
    @param:Json(name = "Source") val source: String? = null,
    @param:Json(name = "Value") val value: String? = null
)

data class OmdbTitleDetailDto(
    @param:Json(name = "Title") val title: String? = null,
    @param:Json(name = "Year") val year: String? = null,
    @param:Json(name = "Rated") val rated: String? = null,
    @param:Json(name = "Released") val released: String? = null,
    @param:Json(name = "Runtime") val runtime: String? = null,
    @param:Json(name = "Genre") val genre: String? = null,
    @param:Json(name = "Director") val director: String? = null,
    @param:Json(name = "Writer") val writer: String? = null,
    @param:Json(name = "Actors") val actors: String? = null,
    @param:Json(name = "Plot") val plot: String? = null,
    @param:Json(name = "Language") val language: String? = null,
    @param:Json(name = "Country") val country: String? = null,
    @param:Json(name = "Awards") val awards: String? = null,
    @param:Json(name = "Poster") val poster: String? = null,
    @param:Json(name = "Ratings") val ratings: List<OmdbRatingDto> = emptyList(),
    @param:Json(name = "Metascore") val metascore: String? = null,
    @param:Json(name = "imdbRating") val imdbRating: String? = null,
    @param:Json(name = "imdbVotes") val imdbVotes: String? = null,
    @param:Json(name = "imdbID") val imdbId: String? = null,
    @param:Json(name = "Type") val type: String? = null,
    @param:Json(name = "totalSeasons") val totalSeasons: String? = null,
    @param:Json(name = "Response") val response: String? = null,
    @param:Json(name = "Error") val error: String? = null
)

data class OmdbSearchItemDto(
    @param:Json(name = "Title") val title: String? = null,
    @param:Json(name = "Year") val year: String? = null,
    @param:Json(name = "imdbID") val imdbId: String? = null,
    @param:Json(name = "Type") val type: String? = null,
    @param:Json(name = "Poster") val poster: String? = null
)

data class OmdbSearchResponseDto(
    @param:Json(name = "Search") val search: List<OmdbSearchItemDto> = emptyList(),
    @param:Json(name = "totalResults") val totalResults: String? = null,
    @param:Json(name = "Response") val response: String? = null,
    @param:Json(name = "Error") val error: String? = null
)
