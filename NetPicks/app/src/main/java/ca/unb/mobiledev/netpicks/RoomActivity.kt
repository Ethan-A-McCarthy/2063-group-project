package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RoomActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val roomsRef = database.getReference("rooms")


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.room_activity)

        val textViewRoomId: TextView = findViewById(R.id.text_view_room_id)
        val textViewPlayerCount: TextView = findViewById(R.id.text_view_player_count)

        val roomId = intent.getStringExtra("roomId")
        val playerId = intent.getStringExtra("userId").toString()
        var number = ""

        textViewRoomId.text = "RoomID: $roomId"

        if (roomId != null) {
            roomsRef.child(roomId).child("number").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    textViewPlayerCount.text = "Number of People: ${dataSnapshot.value}"
                    number = dataSnapshot.value.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("failure","Connection Failed")
                }
            })

            val buttonContinue :Button = findViewById(R.id.button_continue)
            buttonContinue.setOnClickListener{
                roomsRef.child(roomId).child("start").setValue(true)
            }

            val nextScreenRef = roomsRef.child(roomId).child("start")
            nextScreenRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nextScreen = dataSnapshot.getValue(Boolean::class.java)
                    if (nextScreen == true) {
                        val intent = Intent(this@RoomActivity, MovieRoom::class.java).apply {
                            putExtra("roomId", roomId)
                            putExtra("userId", playerId)
                            putExtra("userNumber", number)
                        }
                        startActivity(intent)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("error", "some error")
                }
            })
        }



    }
}