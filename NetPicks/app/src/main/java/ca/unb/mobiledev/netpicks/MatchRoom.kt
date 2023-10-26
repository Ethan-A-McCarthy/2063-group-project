package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MatchRoom: AppCompatActivity() {
    private lateinit var moviePosterImageView1: ImageView
    private lateinit var moviePosterImageView2: ImageView
    private lateinit var moviePosterImageView3: ImageView
    private lateinit var movieTitle1: TextView
    private lateinit var movieTitle2:TextView
    private lateinit var movieTitle3:TextView
    private lateinit var continueButton: Button
    private lateinit var EndButton: Button
    private var list = ArrayList<Int>();

    private val apiKey = "ef1e33d142b3fca8b88033b3ebecd001"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(TmdbApi::class.java)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("check",savedInstanceState.toString())
        if (savedInstanceState != null) {
            super.onSaveInstanceState(savedInstanceState)
        };
        setContentView(R.layout.match_room)

        moviePosterImageView1 = findViewById(R.id.moviePosterImageView1)
        moviePosterImageView2 = findViewById(R.id.moviePosterImageView2)
        moviePosterImageView3 = findViewById(R.id.moviePosterImageView3)
        movieTitle1 = findViewById(R.id.MovieTitle1)
        movieTitle2 = findViewById(R.id.MovieTitle2)
        movieTitle3 = findViewById(R.id.MovieTitle3)
        continueButton = findViewById(R.id.continue_match)
        EndButton = findViewById(R.id.end_session)

        list = intent.getIntegerArrayListExtra("movieID")!!

        val number = list.size

        continueButton.setOnClickListener {
            finish();
        }

        EndButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }




        val call1 = service.getMovieDetails(list[0], apiKey)
        call1.enqueue(object : Callback<MovieDetails> {
            override fun onResponse(call1: Call<MovieDetails>, response1: Response<MovieDetails>) {
                if (response1.isSuccessful) {
                    val movieDetails1 = response1.body()
                    val title1 = movieDetails1?.title
                    val posterPath1 = movieDetails1?.poster_path
                    if (title1 != null) {
                        Log.d("matchroom", title1)
                    }
                    Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath1").into(moviePosterImageView1)

                    movieTitle1.text = title1


                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Toast.makeText(this@MatchRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })


        val call2 = service.getMovieDetails(list[1], apiKey)
        call2.enqueue(object : Callback<MovieDetails> {
            override fun onResponse(call2: Call<MovieDetails>, response2: Response<MovieDetails>) {
                if (response2.isSuccessful) {
                    val movieDetails2 = response2.body()
                    val title2 = movieDetails2?.title
                    val posterPath2 = movieDetails2?.poster_path

                    Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath2").into(moviePosterImageView2)

                    movieTitle2.text = title2
                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Toast.makeText(this@MatchRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })


        val call3 = service.getMovieDetails(list[2], apiKey)
        call3.enqueue(object : Callback<MovieDetails> {
            override fun onResponse(call3: Call<MovieDetails>, response3: Response<MovieDetails>) {
                if (response3.isSuccessful) {
                    val movieDetails3 = response3.body()
                    val title3 = movieDetails3?.title
                    val posterPath3 = movieDetails3?.poster_path

                    Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath3").into(moviePosterImageView3)

                    movieTitle3.text = title3


                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Toast.makeText(this@MatchRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })

    }


}