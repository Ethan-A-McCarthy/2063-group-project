package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private lateinit var likeText1: TextView
    private lateinit var likeText2: TextView
    private lateinit var likeText3: TextView
    private lateinit var continueButton: Button
    private lateinit var EndButton: Button
    private lateinit var googleButton1: Button
    private lateinit var googleButton2: Button
    private lateinit var googleButton3: Button
    private var id1 = 0
    private var id2: Int = 0
    private var id3: Int = 0
    private var like1: Int = 0
    private var like2: Int = 0
    private var like3: Int = 0
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")
    private var roomId = ""


    private val apiKey = "ef1e33d142b3fca8b88033b3ebecd001"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(TmdbApi::class.java)
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        likeText1 = findViewById(R.id.like_1)
        likeText2 = findViewById(R.id.like_2)
        likeText3 = findViewById(R.id.like_3)
        roomId = intent.getStringExtra("roomId").toString()


        id1 = intent.getIntExtra("movieID1",0)
        id2 = intent.getIntExtra("movieID2", 0)
        id3 = intent.getIntExtra("movieID3", 0)
        like1 = intent.getIntExtra("movieLike1", 0)
        like2 = intent.getIntExtra("movieLike2", 0)
        like3 = intent.getIntExtra("movieLike3", 0)
        likeText1.text = "Likes: $like1"
        likeText2.text = "Likes: $like2"
        likeText3.text = "Likes: $like3"

        googleButton1 = findViewById(R.id.openGoogleButton1)
        googleButton2 = findViewById(R.id.openGoogleButton2)
        googleButton3 = findViewById(R.id.openGoogleButton3)

        googleButton1.setOnClickListener{
            val title = movieTitle1.text.toString()
            if (title.isNotEmpty()){
                val googleSearch = "http://www.google.com/search?q=$title"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleSearch))
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Action Failed", Toast.LENGTH_SHORT).show()
            }
        }

        googleButton2.setOnClickListener{
            val title = movieTitle2.text.toString()
            if (title.isNotEmpty()){
                val googleSearch = "http://www.google.com/search?q=$title"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleSearch))
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Action Failed", Toast.LENGTH_SHORT).show()
            }
        }

        googleButton3.setOnClickListener{
            val title = movieTitle3.text.toString()
            if (title.isNotEmpty()){
                val googleSearch = "http://www.google.com/search?q=$title"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleSearch))
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Action Failed", Toast.LENGTH_SHORT).show()
            }
        }


        continueButton.setOnClickListener {
            roomsRef.child(roomId).child("movie").child(id1.toString()).removeValue().addOnCompleteListener { task->
                if(task.isSuccessful){
                    Log.d("success","Successfully remove")
                }else{
                    Log.d("Fail","Fail to remove")
                }
            }
            roomsRef.child(roomId).child("movie").child(id2.toString()).removeValue().addOnCompleteListener { task->
                if(task.isSuccessful){
                    Log.d("success","Successfully remove")
                }else{
                    Log.d("Fail","Fail to remove")
                }
            }
            roomsRef.child(roomId).child("movie").child(id3.toString()).removeValue().addOnCompleteListener { task->
                if(task.isSuccessful){
                    Log.d("success","Successfully remove")
                }else{
                    Log.d("Fail","Fail to remove")
                }
            }
            roomsRef.child(roomId).child("match").setValue(false)

//            finish();
        }

        val nextScreenRef = roomsRef.child(roomId).child("match")
        nextScreenRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nextScreen = dataSnapshot.getValue(Boolean::class.java)
                if (nextScreen == false) {
                    finish()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("error", "some error")
            }
        })

        EndButton.setOnClickListener {
            roomsRef.child(roomId).child("endRoom").setValue(true)
        }

        val endRoomRef = roomsRef.child(roomId).child("endRoom")
        endRoomRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val endRoom = snapshot.getValue(Boolean::class.java)
                if( endRoom == true){
                    val roomManager = RoomManager()
                    roomManager.leaveRoom(roomId)
                    val intent = Intent(this@MatchRoom, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("error","some error")
            }

        })


        val call1 = service.getMovieDetails(id1, apiKey)
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


        val call2 = service.getMovieDetails(id2, apiKey)
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


        val call3 = service.getMovieDetails(id3, apiKey)
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