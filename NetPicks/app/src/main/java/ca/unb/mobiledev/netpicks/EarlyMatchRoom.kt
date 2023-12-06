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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EarlyMatchRoom: AppCompatActivity() {
    private lateinit var continueButton: Button
    private lateinit var EndButton: Button
    private lateinit var moviePosterImageView4: ImageView
    private lateinit var movieTitle: TextView
    private lateinit var googleButton1: Button
    private var id: Int = 0
    private var roomId = ""
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")

    private val apiKey = "ef1e33d142b3fca8b88033b3ebecd001"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(TmdbApi::class.java)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.early_match_room)
        var title = ""
        continueButton = findViewById(R.id.continue_match)
        EndButton = findViewById(R.id.end_session)
        moviePosterImageView4 = findViewById(R.id.moviePosterImageView4)
        movieTitle = findViewById(R.id.movieTitle)
        googleButton1 = findViewById(R.id.openGoogleButton)
        id = intent.getIntExtra("movieID1", 0)
        roomId = intent.getStringExtra("roomId").toString()
        val introductionButton = findViewById<Button>(R.id.introductionButton)

        introductionButton.setOnClickListener {
            showIntroductionDialog()
        }

        googleButton1.setOnClickListener{
            if (title.isNotEmpty()){
                val intent = Intent(Intent.ACTION_SEARCH)
                intent.setPackage("com.google.android.youtube")
                intent.putExtra("query", title)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Action Failed", Toast.LENGTH_SHORT).show()
            }
        }

        continueButton.setOnClickListener {
            roomsRef.child(roomId).child("movie").child(id.toString()).removeValue().addOnCompleteListener { task->
                if(task.isSuccessful){
                    Log.d("success","Successfully remove")
                }else{
                    Log.d("Fail","Fail to remove")
                }
            }
            roomsRef.child(roomId).child("earlyMatch").setValue(false)
        }

        val nextScreenRef = roomsRef.child(roomId).child("earlyMatch")
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
                    val intent = Intent(this@EarlyMatchRoom, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("error","some error")
            }

        })

        val call1 = service.getMovieDetails(id, apiKey)
        call1.enqueue(object : Callback<MovieDetails> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call1: Call<MovieDetails>, response1: Response<MovieDetails>) {
                if (response1.isSuccessful) {
                    val movieDetails1 = response1.body()
                    val title1 = movieDetails1?.title
                    if (title1 != null) {
                        title = title1
                    }
                    val posterPath1 = movieDetails1?.poster_path
                    if (title1 != null) {
                        Log.d("matchroom", title1)
                    }
                    Picasso.get().load("https://image.tmdb.org/t/p/w500$posterPath1").into(moviePosterImageView4)

                    movieTitle.text = "All the users like this movie:\n $title1"

                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Toast.makeText(this@EarlyMatchRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })

    }
    private fun showIntroductionDialog() {
        val call = service.getMovieDetails(id, apiKey)
        call.enqueue(object : Callback<MovieDetails> {
            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (response.isSuccessful) {
                    val movieDetails = response.body()
                    val introduction = movieDetails?.overview

                    val builder = AlertDialog.Builder(this@EarlyMatchRoom)
                    builder.setTitle("Movie Introduction")
                    builder.setMessage(introduction)
                    builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

                    val dialog = builder.create()
                    dialog.show()
                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                Toast.makeText(this@EarlyMatchRoom, "Network request failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
}