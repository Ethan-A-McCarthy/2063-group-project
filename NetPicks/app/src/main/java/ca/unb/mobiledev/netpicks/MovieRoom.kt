package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.squareup.picasso.Picasso

class MovieRoom : AppCompatActivity() {
    private lateinit var moviePosterImageView: ImageView
    private lateinit var likeButton: Button
    private lateinit var dislikeButton: Button
    private lateinit var endMatch: Button
    private lateinit var movieTitle:TextView


    private val apiKey = "ef1e33d142b3fca8b88033b3ebecd001"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(TmdbApi::class.java)
    private var movies: List<Movie> = emptyList()
    private var currentMovieIndex = 0
    private val likeMovieList = ArrayList<Int>();

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movie_room)

        moviePosterImageView = findViewById(R.id.moviePosterImageView)
        likeButton = findViewById(R.id.likeButton)
        dislikeButton = findViewById(R.id.dislikeButton)
        movieTitle = findViewById(R.id.MovieTitle)
        endMatch = findViewById(R.id.endMatch)
        likeButton.setOnClickListener {
            onLikeButtonClicked(it)
        }
        dislikeButton.setOnClickListener {
            onDislikeButtonClicked(it)
        }
        val introductionButton = findViewById<Button>(R.id.introductionButton)
        introductionButton.setOnClickListener {
            showIntroductionDialog()
        }

        endMatch.setOnClickListener {
            val intent = Intent(this@MovieRoom, MatchRoom::class.java)
            intent.putIntegerArrayListExtra("movieID",likeMovieList);
            Log.d("movieroom", intent.getIntegerArrayListExtra("movieID").toString())
            startActivity(intent)
        }


        fetchMovieList()
    }

    private fun fetchMovieList() {
        val call = service.getMovies(apiKey)
        call.enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    movies = movieResponse?.results ?: emptyList()
                    Log.d("movielist", movies.toString())
                    showMovie(currentMovieIndex)
                } else {
                    Toast.makeText(this@MovieRoom, "Movie Request Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {

                Toast.makeText(this@MovieRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showMovie(index: Int) {
        if (index >= 0 && index < movies.size) {
            val movie = movies[index]
            val movieId = movie.id
            fetchMovieDetails(movieId)
        }
    }

    private fun fetchMovieDetails(movieId: Int) {
        val call = service.getMovieDetails(movieId, apiKey)
        call.enqueue(object : Callback<MovieDetails> {
            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (response.isSuccessful) {
                    val movieDetails = response.body()
                    val title = movieDetails?.title
                    val posterPath = movieDetails?.poster_path

                    loadAndDisplayMovieDetails(title, posterPath)
                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Toast.makeText(this@MovieRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAndDisplayMovieDetails(title: String?, posterPath: String?) {
        Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath").into(moviePosterImageView)

        movieTitle.text = title
    }
    private fun onLikeButtonClicked(view: View) {
        val movie = movies[currentMovieIndex]
        val movieId = movie.id
        likeMovieList.add(movieId)
        Log.d("movieroom", likeMovieList.toString())
        showNextMovie()
    }

    private fun onDislikeButtonClicked(view: View) {
        showNextMovie()
    }

    private fun showNextMovie() {
        currentMovieIndex++
        if (currentMovieIndex < movies.size) {
            showMovie(currentMovieIndex)
        } else {
            moviePosterImageView.setImageResource(R.drawable.placeholder_image)
            movieTitle.text = "No more movies"
        }
    }

    private fun showIntroductionDialog() {
        val movie = movies[currentMovieIndex]
        val movieId = movie.id

        val call = service.getMovieDetails(movieId, apiKey)
        call.enqueue(object : Callback<MovieDetails> {
            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (response.isSuccessful) {
                    val movieDetails = response.body()
                    val introduction = movieDetails?.overview

                    val builder = AlertDialog.Builder(this@MovieRoom)
                    builder.setTitle("Movie Introduction")
                    builder.setMessage(introduction)
                    builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

                    val dialog = builder.create()
                    dialog.show()
                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Toast.makeText(this@MovieRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })
    }


}