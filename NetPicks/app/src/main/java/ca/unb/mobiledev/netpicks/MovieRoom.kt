package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
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
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")
    private var roomId = ""
    private var playerId = ""


    private val apiKey = "ef1e33d142b3fca8b88033b3ebecd001"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(TmdbApi::class.java)
    private var movies: List<Movie> = emptyList()
    private var currentMovieIndex = 0
    private val likeMoviesList = ArrayList<Movie>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movie_room)

        roomId = intent.getStringExtra("roomId").toString()
        playerId = intent.getStringExtra("userId").toString()

        moviePosterImageView = findViewById(R.id.moviePosterImageView)
        likeButton = findViewById(R.id.likeButton)
        dislikeButton = findViewById(R.id.dislikeButton)
        movieTitle = findViewById(R.id.MovieTitle)
        endMatch = findViewById(R.id.endMatch)
        likeButton.setOnClickListener {
            onLikeButtonClicked()
        }
        dislikeButton.setOnClickListener {
            onDislikeButtonClicked()
        }
        val introductionButton = findViewById<Button>(R.id.introductionButton)
        introductionButton.setOnClickListener {
            showIntroductionDialog()
        }

        endMatch.setOnClickListener {
            val intent = Intent(this@MovieRoom, MatchRoom::class.java)
            likeMoviesList.sortBy { it.likes }
            intent.putExtra("movieID1",likeMoviesList[0].id)
            intent.putExtra("movieID2",likeMoviesList[1].id)
            intent.putExtra("movieID3",likeMoviesList[2].id)
            intent.putExtra("movieLike1",likeMoviesList[0].likes)
            intent.putExtra("movieLike2",likeMoviesList[1].likes)
            intent.putExtra("movieLike3",likeMoviesList[2].likes)
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

    private fun onLikeButtonClicked() {
        val movie = movies[currentMovieIndex]
        roomsRef.child(roomId).child("movie").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                println("Movie exists")
            } else {
                val movieIds = hashMapOf<String,Int>()
                movieIds[movie.id.toString()] = 1
                roomsRef.child(roomId).child("movie").setValue(movieIds)
            }
        }
        movie.likes++
        likeMoviesList.add(movie)
        showNextMovie()
    }

    private fun onDislikeButtonClicked() {
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