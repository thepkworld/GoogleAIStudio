package com.example.data.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("trending/{media_type}/{time_window}")
    suspend fun getTrending(
        @Path("media_type") mediaType: String = "all",
        @Path("time_window") timeWindow: String = "day",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("trending/person/{time_window}")
    suspend fun getTrendingPeople(
        @Path("time_window") timeWindow: String = "week",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbPersonSummaryDto>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("tv/airing_today")
    suspend fun getAiringTodayTv(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("language") language: String = "en-US"
    ): TmdbGenreListResponse

    @GET("genre/tv/list")
    suspend fun getTvGenres(
        @Query("language") language: String = "en-US"
    ): TmdbGenreListResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("with_genres") withGenres: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_count.gte") minVotes: Int? = null,
        @Query("primary_release_year") year: Int? = null,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("page") page: Int = 1,
        @Query("with_genres") withGenres: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_count.gte") minVotes: Int? = null,
        @Query("first_air_date_year") year: Int? = null,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbMediaDto>

    @GET("search/person")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbPersonSummaryDto>

    @GET("search/collection")
    suspend fun searchCollections(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TmdbPagedResponse<TmdbCollectionRefDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String =
            "credits,reviews,images,videos,recommendations,similar,release_dates,watch/providers,keywords",
        @Query("language") language: String = "en-US"
    ): TmdbMovieDetailDto

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") appendToResponse: String =
            "credits,reviews,images,videos,recommendations,similar,content_ratings,watch/providers,keywords",
        @Query("language") language: String = "en-US"
    ): TmdbTvDetailDto

    @GET("tv/{series_id}/season/{season_number}")
    suspend fun getTvSeasonDetails(
        @Path("series_id") seriesId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("language") language: String = "en-US"
    ): TmdbSeasonDetailDto

    @GET("person/{person_id}")
    suspend fun getPersonDetails(
        @Path("person_id") personId: Int,
        @Query("append_to_response") appendToResponse: String = "combined_credits,images",
        @Query("language") language: String = "en-US"
    ): TmdbPersonDetailDto

    @GET("collection/{collection_id}")
    suspend fun getCollectionDetails(
        @Path("collection_id") collectionId: Int,
        @Query("language") language: String = "en-US"
    ): TmdbCollectionDetailDto
}
