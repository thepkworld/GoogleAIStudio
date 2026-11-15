package com.example.data.omdb

import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApiService {
    @GET(".")
    suspend fun getByImdbId(
        @Query("i") imdbId: String,
        @Query("plot") plot: String = "full",
        @Query("r") format: String = "json"
    ): OmdbTitleDetailDto

    @GET(".")
    suspend fun getByTitle(
        @Query("t") title: String,
        @Query("y") year: Int? = null,
        @Query("type") type: String? = null,
        @Query("plot") plot: String = "full",
        @Query("r") format: String = "json"
    ): OmdbTitleDetailDto

    @GET(".")
    suspend fun search(
        @Query("s") query: String,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("r") format: String = "json"
    ): OmdbSearchResponseDto
}
