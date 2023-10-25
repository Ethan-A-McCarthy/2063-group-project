package ca.unb.mobiledev.netpicks

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    fun getMovies(@Query("api_key") apiKey:String): Call<MovieResponse>

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "credits" // 通过credits参数获取电影标题
    ): Call<MovieDetails>
}