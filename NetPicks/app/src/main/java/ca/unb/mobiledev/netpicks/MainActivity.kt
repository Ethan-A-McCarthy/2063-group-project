package ca.unb.mobiledev.netpicks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import java.util.UUID


private const val DEBUG_TAG = "Gestures"

class MainActivity:AppCompatActivity(){


    @OptIn(DelicateCoroutinesApi::class)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)


        val buttonCreateRoom: Button = findViewById(R.id.button_create_room)
        val buttonJoinRoom: Button = findViewById(R.id.button_join_room)
        val editTextRoomId: EditText = findViewById(R.id.edit_text_room_id)

        val playerid = UUID.randomUUID().toString();

        buttonCreateRoom.setOnClickListener {
            val roomManager = RoomManager()
            val roomid = generateRoomId()
            roomManager.createRoom(roomid,playerid)
            Toast.makeText(this, "Room Created and the ID is $roomid", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, RoomActivity::class.java).apply {
                putExtra("userId", playerid)
                putExtra("roomId", roomid)
            }
            startActivity(intent)
        }

        buttonJoinRoom.setOnClickListener {
            GlobalScope.launch {
                val roomId = editTextRoomId.text.toString()
                val roomManager = RoomManager()
                if (roomManager.joinRoom(roomId,playerid)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Join Room：$roomId", Toast.LENGTH_SHORT).show()
                    }
                    val intent = Intent(this@MainActivity, RoomActivity::class.java).apply {
                        putExtra("roomId", roomId)
                    }
                    startActivity(intent)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Wrong Room ID", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun generateRoomId(): String {
        val random = Random()
        val roomId = StringBuilder()
        for (i in 0 until 6) {
            roomId.append(random.nextInt(10))
        }
        return roomId.toString()
    }


}


