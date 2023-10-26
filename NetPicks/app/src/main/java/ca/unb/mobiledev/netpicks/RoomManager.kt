package ca.unb.mobiledev.netpicks
import android.util.Log
import com.google.firebase.database.*

class RoomManager {

    private val database = FirebaseDatabase.getInstance()


    private val roomsRef = database.getReference("rooms")
    private val usersRef = database.getReference("users")


    fun createRoom(roomId: String, userId: String) {

        roomsRef.child(roomId).setValue(Room(roomId))

        roomsRef.child(roomId).child("users").child(userId).setValue(true)

        usersRef.child(userId).child("rooms").child(roomId).setValue(true)

        roomsRef.child(roomId).child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount == 0L) {

                    roomsRef.child(roomId).removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("failure","Connection Failed")
            }
        })
    }

    fun joinRoom(roomId: String, userId: String) {
        roomsRef.child(roomId).child("users").child(userId).setValue(true)

        usersRef.child(userId).child("rooms").child(roomId).setValue(true)
    }

    fun leaveRoom(roomId: String, userId: String) {
        roomsRef.child(roomId).child("users").child(userId).removeValue()

        usersRef.child(userId).child("rooms").child(roomId).removeValue()
    }

    data class Room(val id: String)
}