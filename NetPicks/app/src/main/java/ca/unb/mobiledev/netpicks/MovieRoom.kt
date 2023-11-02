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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    private var userNumber = ""
    private var topThreeMovies = HashMap<String, Int>()


    private val apiKey = "ef1e33d142b3fca8b88033b3ebecd001"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(TmdbApi::class.java)
    private var movies: List<Movie> = emptyList()
    private var currentMovieIndex = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movie_room)

        roomId = intent.getStringExtra("roomId").toString()
        playerId = intent.getStringExtra("userId").toString()
        userNumber = intent.getStringExtra("userNumber").toString()


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
            if(topThreeMovies.size>=3){
                roomsRef.child(roomId).child("match").setValue(true)
            }
        }

        val nextScreenRef = roomsRef.child(roomId).child("match")
        nextScreenRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nextScreen = dataSnapshot.getValue(Boolean::class.java)
                if (nextScreen == true && topThreeMovies.size>=3) {
                    val intent = Intent(this@MovieRoom, MatchRoom::class.java)
                    var i = 1
                    for((likeMovie,count) in topThreeMovies){
                        intent.putExtra("movieID$i",likeMovie.toInt())
                        intent.putExtra("movieLike$i",count)
                        i++
                    }
                    intent.putExtra("roomId", roomId)
                    topThreeMovies.clear()
                    startActivity(intent)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("error", "some error")
            }
        })

        roomsRef.child(roomId).child("movie").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val movieMap = snapshot.getValue(object: GenericTypeIndicator<HashMap<String,Int>>(){})?: hashMapOf()
                var likecount = 0
                var likeMovieId = ""
                val topMovies = movieMap.entries.sortedByDescending { it.value }.take(1)
                for((likeMovie,count) in topMovies) {
                    likeMovieId = likeMovie
                    likecount = count
                }
                if(likecount.toString() == userNumber && userNumber.toInt() >= 2){
                    val intent = Intent(this@MovieRoom, EarlyMatchRoom::class.java)
                    roomsRef.child(roomId).child("match").setValue(true)
                    intent.putExtra("movieID1", likeMovieId.toInt())
                    intent.putExtra("roomId", roomId)
                    startActivity(intent)
                }
                val topThreeLikeMovies = movieMap.entries.sortedByDescending { it.value }.take(3)
                for((likeMovie,count) in topThreeLikeMovies) {
                    topThreeMovies[likeMovie] = count
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Failure", "Failed to read value.", error.toException())
            }
        })


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
                addLikeMovie(movie.id)
            } else {
                val movieIds = hashMapOf<String,Int>()
                movieIds[movie.id.toString()] = 1
                roomsRef.child(roomId).child("movie").setValue(movieIds)
            }
        }
        showNextMovie()
    }

    private fun onDislikeButtonClicked() {
        showNextMovie()
    }
    private fun addLikeMovie(id: Int){
        roomsRef.child(roomId).child("movie").runTransaction(object: Transaction.Handler{
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val movieMap = currentData.getValue(object :GenericTypeIndicator<HashMap<String, Int>>(){})?: hashMapOf()
                val currentCount = movieMap[id.toString()]?:0
                movieMap[id.toString()] = currentCount+1

                currentData.value = movieMap
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.w("database error", "Failed to commit transaction", error.toException())
                } else {
                    Log.d("database success", "Transaction committed successfully")
                }
            }
        })
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