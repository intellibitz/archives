/**
 * BLUKIT DATA: MESSAGE DATABASE
 *
 * A high-performance, raw SQLite implementation of the Message history.
 *
 * Logic:
 * - Git-inspired Storage: Each message is an entry with a parent reference.
 * - Selective Sync: Peers can bridge missing history.
 * - Hardware Encrypted: Payloads are stored as encrypted blobs.
 */
package cc.thevar.blukit.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cc.thevar.blukit.domain.model.MeshMessage

class MessageDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "message_mesh.db"
        private const val DATABASE_VERSION = 1
        private const val TAB_MESSAGES = "messages"

        // Columns
        private const val COL_ID = "_id"
        private const val COL_MSG_ID = "message_id"
        private const val COL_PARENT_HASH = "parent_hash"
        private const val COL_GROUP_ID = "group_id"
        private const val COL_PAYLOAD = "payload"
        private const val COL_WEIGHT = "social_weight"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_TYPE = "type"
        private const val COL_PRIORITY = "is_priority"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TAB_MESSAGES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MSG_ID TEXT UNIQUE,
                $COL_PARENT_HASH TEXT,
                $COL_GROUP_ID TEXT,
                $COL_PAYLOAD BLOB,
                $COL_WEIGHT INTEGER DEFAULT 0,
                $COL_TIMESTAMP INTEGER,
                $COL_TYPE INTEGER,
                $COL_PRIORITY INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
        db.execSQL("CREATE INDEX idx_msg_group ON $TAB_MESSAGES ($COL_GROUP_ID)")
        db.execSQL("CREATE INDEX idx_msg_timestamp ON $TAB_MESSAGES ($COL_TIMESTAMP)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    /** Inserts a message into the database. */
    fun insertMessage(payload: MeshMessage, encryptedBytes: ByteArray) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(COL_MSG_ID, payload.messageId)
                put(COL_PARENT_HASH, payload.parentMessageId)
                put(COL_GROUP_ID, payload.groupId)
                put(COL_PAYLOAD, encryptedBytes)
                put(COL_WEIGHT, payload.resonanceWeight)
                put(COL_TIMESTAMP, payload.timestamp)
                put(COL_TYPE, payload.type)
                put(COL_PRIORITY, if (payload.isPriority) 1 else 0)
            }
            db.insertWithOnConflict(TAB_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun getAllRawMessages(): List<ByteArray> {
        return getRawMessagesSince(0)
    }

    fun getRawMessagesSince(timestamp: Long): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        readableDatabase.rawQuery(
            "SELECT $COL_PAYLOAD FROM $TAB_MESSAGES WHERE $COL_TIMESTAMP > ? ORDER BY $COL_TIMESTAMP ASC",
            arrayOf(timestamp.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getBlob(0))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun getLatestMessageId(): String? {
        readableDatabase.rawQuery(
            "SELECT $COL_MSG_ID FROM $TAB_MESSAGES ORDER BY $COL_TIMESTAMP DESC LIMIT 1",
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0)
        }
        return null
    }

    fun updateWeight(messageId: String, newWeight: Int) {
        writableDatabase.execSQL(
            "UPDATE $TAB_MESSAGES SET $COL_WEIGHT = ? WHERE $COL_MSG_ID = ?",
            arrayOf(newWeight.toString(), messageId)
        )
    }

    fun deleteMessage(messageId: String) {
        writableDatabase.delete(TAB_MESSAGES, "$COL_MSG_ID = ?", arrayOf(messageId))
    }
}
