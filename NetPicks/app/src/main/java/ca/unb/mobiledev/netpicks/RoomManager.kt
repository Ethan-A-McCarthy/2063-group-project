package ca.unb.mobiledev.netpicks
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomManager {

    private val database = FirebaseDatabase.getInstance()


    private val roomsRef = database.getReference("rooms")



    fun createRoom(roomId: String, userId: String) {
        roomsRef.child(roomId).setValue(Room(roomId))
        roomsRef.child(roomId).child("number").setValue(1)
        roomsRef.child(roomId).child("start").setValue(false)
        roomsRef.child(roomId).child("match").setValue(false)
        roomsRef.child(roomId).child("users").child(userId).setValue(true)
        roomsRef.child(roomId).child("endRoom").setValue(false)

    }

    suspend fun joinRoom(roomId: String, userId: String): Boolean = suspendCoroutine { continuation ->
        val roomRef = roomsRef.child(roomId)
        roomRef.get().addOnCompleteListener{
            if(it.isSuccessful){
                val snapshot = it.result
                if(snapshot.exists()){
                    roomsRef.child(roomId).child("users").child(userId).setValue(true)
                    roomsRef.child(roomId).child("number").runTransaction(object : Transaction.Handler{
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val current = currentData.getValue(Int::class.java)?:return Transaction.success(currentData)
                            currentData.value = current + 1
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {
                            Log.d("number","Number incremented successfully")
                        }
                    })
                    continuation.resume(true)
                } else {
                    continuation.resume(false)
                }
            } else {
                continuation.resumeWithException(it.exception ?: RuntimeException("Unknown error"))
            }
        }
    }

    fun leaveRoom(roomId: String) {
        roomsRef.child(roomId).removeValue()

    }

    data class Room(val id: String)
}