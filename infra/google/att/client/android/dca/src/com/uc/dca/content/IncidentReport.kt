/**
 *
 */
package com.uc.dca.content

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log

/**
 * @author muthu
 *
 */
class IncidentReport private constructor() {

    /**
     * Common Trackers columns
     */
    open class IRColumns
    /**
     * This class cannot be instantiated
     */
    protected constructor() : BaseColumns {
        companion object {
            const val CONTENT_TYPE_PREFIX = "vnd.uc.cursor.dir/vnd.uc."
            const val CONTENT_ITEM_TYPE_PREFIX = "vnd.uc.cursor.item/vnd.uc."
            const val CONTENT_URI_PREFIX = "content://"
            const val CONTENT_SEPARATOR = "/"
            const val _ID = BaseColumns._ID
        }
    }

    class Details
    /**
     * This class cannot be instantiated
     */
    private constructor() : IRColumns() {
        companion object {
            const val TABLE_NAME = "details"

            val CONTENT_TYPE = CONTENT_TYPE_PREFIX + TABLE_NAME
            val CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_PREFIX + TABLE_NAME
            val CONTENT_URI: Uri = Uri.parse(
                CONTENT_URI_PREFIX
                        + AUTHORITY + CONTENT_SEPARATOR + TABLE_NAME
            )

            const val ID = "id"
            const val DETAILS = "details"
            const val _ID = IRColumns._ID
        }
    }

    /**
     * This class helps open, create, and upgrade the database file.
     */
    internal class DatabaseHelper(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE " + Details.TABLE_NAME + " ("
                        + Details._ID + " INTEGER PRIMARY KEY,"
                        + Details.ID + " INTEGER NOT NULL,"
                        + Details.DETAILS + " TEXT NOT NULL"
                        + ");"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                "IR#DatabaseHelper ==>", "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
            )
            db.execSQL("DROP TABLE IF EXISTS " + Details.TABLE_NAME)
            onCreate(db)
        }
    }

    companion object {
        const val AUTHORITY = "com.uc.dca.content.IncidentReport"
        const val DATABASE_NAME = "ira.db"
        const val DATABASE_VERSION = 2
    }
}
