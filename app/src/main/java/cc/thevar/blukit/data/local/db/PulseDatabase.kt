/**
 * BLUKIT DATA: PULSE DATABASE
 *
 * A high-performance, raw SQLite implementation of the Pulse DAG (Directed Acyclic Graph).
 * Avoids bloated ORMs (Room/SQLDelight) to ensure maximum speed and battery optimization.
 *
 * Logic:
 * - Git-inspired Storage: Each pulse is a "commit" with a parent hash.
 * - Selective Sync: Peers can bridge missing history by traversing the DAG.
 * - Hardware Encrypted: Payloads are stored as encrypted blobs.
 */
package cc.thevar.blukit.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cc.thevar.blukit.domain.model.MessagePayload

class PulseDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pulse_mesh.db"
        private const val DATABASE_VERSION = 1
        private const val TAB_PULSES = "pulses"

        // Columns
        private const val COL_ID = "_id"
        private const val COL_PULSE_ID = "pulse_id"
        private const val COL_PARENT_HASH = "parent_hash"
        private const val COL_GROUP_ID = "group_id"
        private const val COL_PAYLOAD = "payload"
        private const val COL_WEIGHT = "resonance_weight"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_TYPE = "type"
        private const val COL_PRIORITY = "is_priority"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TAB_PULSES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PULSE_ID TEXT UNIQUE,
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
        // Index for rapid DAG traversal
        db.execSQL("CREATE INDEX idx_pulse_group ON $TAB_PULSES ($COL_GROUP_ID)")
        db.execSQL("CREATE INDEX idx_pulse_timestamp ON $TAB_PULSES ($COL_TIMESTAMP)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // High-performance migration logic would go here
    }

    /** Inserts a pulse into the DAG. Ensures uniqueness via pulse_id. */
    fun insertPulse(payload: MessagePayload, encryptedBytes: ByteArray) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(COL_PULSE_ID, payload.messageId)
                put(COL_PARENT_HASH, payload.parentMessageId)
                put(COL_GROUP_ID, payload.groupId)
                put(COL_PAYLOAD, encryptedBytes)
                put(COL_WEIGHT, payload.resonanceWeight)
                put(COL_TIMESTAMP, payload.timestamp)
                put(COL_TYPE, payload.type)
                put(COL_PRIORITY, if (payload.isPriority) 1 else 0)
            }
            db.insertWithOnConflict(TAB_PULSES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    /** Returns all raw encrypted pulses for a full memory bridge. */
    fun getAllRawPulses(): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        readableDatabase.rawQuery("SELECT $COL_PAYLOAD FROM $TAB_PULSES", null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getBlob(0))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun updateWeight(pulseId: String, newWeight: Int) {
        writableDatabase.execSQL(
            "UPDATE $TAB_PULSES SET $COL_WEIGHT = ? WHERE $COL_PULSE_ID = ?",
            arrayOf(newWeight.toString(), pulseId)
        )
    }

    fun deletePulse(pulseId: String) {
        writableDatabase.delete(TAB_PULSES, "$COL_PULSE_ID = ?", arrayOf(pulseId))
    }
}
