package com.androidworks.navsys.wuffit.content

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log

object Tracker {
    const val AUTHORITY = "com.androidworks.navsys.wuffit.content.Tracker"
    const val DATABASE_NAME = "trackers.db"
    const val DATABASE_VERSION = 3

    class DatabaseHelper(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE " + Details.TABLE_NAME + " (" +
                    Details._ID + " INTEGER PRIMARY KEY," +
                    Details.ID + " INTEGER NOT NULL UNIQUE," +
                    Details.NAME + " TEXT NOT NULL UNIQUE," +
                    Details.NUMBER + " TEXT NOT NULL UNIQUE" +
                    ");"
            )
            db.execSQL(
                "CREATE TABLE " + Setup.TABLE_NAME + " (" +
                    Setup._ID + " INTEGER PRIMARY KEY," +
                    Setup._FID + " INTEGER NOT NULL UNIQUE," +
                    Setup.REPLY + " TEXT," +
                    Setup.REPLY_TIME + " INTEGER," +
                    Setup.SETUP_TIME + " INTEGER" +
                    ");"
            )
            db.execSQL(
                "CREATE TABLE " + Locations.TABLE_NAME + " (" +
                    Locations._ID + " INTEGER PRIMARY KEY," +
                    Locations._FID + " INTEGER NOT NULL UNIQUE," +
                    Locations.LOCATION + " TEXT," +
                    Locations.UPDATE_TIME + " INTEGER," +
                    Locations.REQUEST_TIME + " INTEGER" +
                    ");"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                "Trackers#DatabaseHelper ==>",
                "Upgrading database from version $oldVersion to $newVersion, which will destroy all old data"
            )
            db.execSQL("DROP TABLE IF EXISTS " + Details.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + Setup.TABLE_NAME)
            db.execSQL("DROP TABLE IF EXISTS " + Locations.TABLE_NAME)
            onCreate(db)
        }
    }

    interface TrackerColumns : BaseColumns {
        companion object {
            const val CONTENT_TYPE_PREFIX = "vnd.navsys.cursor.dir/vnd.navsys."
            const val CONTENT_ITEM_TYPE_PREFIX = "vnd.navsys.cursor.item/vnd.navsys."
            const val CONTENT_URI_PREFIX = "content://"
            const val CONTENT_SEPARATOR = "/"
        }
    }

    object Details : TrackerColumns {
        const val _ID = BaseColumns._ID
        const val TABLE_NAME = "details"
        const val CONTENT_TYPE = TrackerColumns.CONTENT_TYPE_PREFIX + TABLE_NAME
        const val CONTENT_ITEM_TYPE = TrackerColumns.CONTENT_ITEM_TYPE_PREFIX + TABLE_NAME
        val CONTENT_URI: Uri = Uri.parse(TrackerColumns.CONTENT_URI_PREFIX + AUTHORITY + TrackerColumns.CONTENT_SEPARATOR + TABLE_NAME)

        const val ID = "id"
        const val NAME = "name"
        const val NUMBER = "number"
    }

    object Setup : TrackerColumns {
        const val _ID = BaseColumns._ID
        const val TABLE_NAME = "setup"
        const val CONTENT_TYPE = TrackerColumns.CONTENT_TYPE_PREFIX + TABLE_NAME
        const val CONTENT_ITEM_TYPE = TrackerColumns.CONTENT_ITEM_TYPE_PREFIX + TABLE_NAME
        val CONTENT_URI: Uri = Uri.parse(TrackerColumns.CONTENT_URI_PREFIX + AUTHORITY + TrackerColumns.CONTENT_SEPARATOR + TABLE_NAME)

        const val _FID = "_fid"
        const val SETUP_TIME = "setup_t"
        const val REPLY_TIME = "reply_t"
        const val REPLY = "reply"
    }

    object Locations : TrackerColumns {
        const val _ID = BaseColumns._ID
        const val TABLE_NAME = "locations"
        const val CONTENT_TYPE = TrackerColumns.CONTENT_TYPE_PREFIX + TABLE_NAME
        const val CONTENT_ITEM_TYPE = TrackerColumns.CONTENT_ITEM_TYPE_PREFIX + TABLE_NAME
        val CONTENT_URI: Uri = Uri.parse(TrackerColumns.CONTENT_URI_PREFIX + AUTHORITY + TrackerColumns.CONTENT_SEPARATOR + TABLE_NAME)

        const val _FID = "_fid"
        const val LOCATION = "location"
        const val UPDATE_TIME = "update_t"
        const val REQUEST_TIME = "request_t"
    }
}
