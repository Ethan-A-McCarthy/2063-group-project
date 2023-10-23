package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RoomActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.room_activity)

        val textViewRoomId: TextView = findViewById(R.id.text_view_room_id)
        val textViewPlayerCount: TextView = findViewById(R.id.text_view_player_count)

        val roomId = intent.getStringExtra("roomId")
        val databaseHelper = DatabaseHelper(this)
        val room = roomId?.let { databaseHelper.getRoom(it) }

        textViewRoomId.text = "RoomID: $roomId"
        if (room != null) {
            textViewPlayerCount.text = "Number of People: ${room.players.size}"
            Log.d("usernumber", room.players.size.toString())
        }

        val buttonContinue :Button = findViewById(R.id.button_continue)
        buttonContinue.setOnClickListener{
            val intent = Intent(this@RoomActivity, MovieRoom::class.java)
            startActivity(intent)
        }
    }
}