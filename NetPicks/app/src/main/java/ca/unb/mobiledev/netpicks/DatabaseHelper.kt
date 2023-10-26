package ca.unb.mobiledev.netpicks

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import java.util.Random
import java.util.UUID

class DatabaseHelper(context: Context) :SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    override fun onCreate(db: SQLiteDatabase) {
        val creatTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID TEXT PRIMARY KEY, $COLUMN_NAME TEXT, $COLUMN_PLAYERS TEXT)"
        db.execSQL(creatTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun createRoom(name: String): String {
        val db = this.writableDatabase
        val roomId = generateRoomId()
        val playerid = UUID.randomUUID().toString();
        val values = ContentValues().apply {
            put(COLUMN_ID, roomId)
            put(COLUMN_NAME, name)
            put(COLUMN_PLAYERS, playerid)
        }
        db.insert(TABLE_NAME, null, values)
        return roomId
    }

    @SuppressLint("Range")
    fun joinRoom(roomId: String, playerId: String) {
        val db = this.writableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_PLAYERS), "$COLUMN_ID = ?", arrayOf(roomId), null, null, null)
        if (cursor.moveToFirst()) {
            var players = cursor.getString(cursor.getColumnIndex(COLUMN_PLAYERS))
            players += "$playerId,"
            val values = ContentValues().apply {
                put(COLUMN_PLAYERS, players)
            }
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(roomId))
        }
        cursor.close()
    }

    @SuppressLint("Range")
    fun leaveRoom(roomId: String, playerId: String) {
        val db = this.writableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_PLAYERS), "$COLUMN_ID = ?", arrayOf(roomId), null, null, null)
        if (cursor.moveToFirst()) {
            var players = cursor.getString(cursor.getColumnIndex(COLUMN_PLAYERS))
            players = players.replace("$playerId,", "")
            val values = ContentValues().apply {
                put(COLUMN_PLAYERS, players)
            }
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(roomId))
        }
        cursor.close()
    }

    @SuppressLint("Range")
    fun getRoom(roomId: String): Room? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PLAYERS), "$COLUMN_ID = ?", arrayOf(roomId), null, null, null)
        var room: Room? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getString(cursor.getColumnIndex(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            val players = cursor.getString(cursor.getColumnIndex(COLUMN_PLAYERS)).split(",").filter { it.isNotEmpty() }
            room = Room(id, name, players)
        }
        cursor.close()
        return room
    }

    private fun generateRoomId(): String {
        val random = Random()
        val roomId = StringBuilder()
        for (i in 0 until 6) {
            roomId.append(random.nextInt(10)) // 生成一个0-9之间的随机数
        }
        return roomId.toString()
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "database.db"
        private const val TABLE_NAME = "rooms"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PLAYERS = "players"
    }
}