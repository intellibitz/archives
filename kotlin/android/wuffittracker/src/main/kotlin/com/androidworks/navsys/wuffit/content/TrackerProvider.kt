package com.androidworks.navsys.wuffit.content

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils

class TrackerProvider : ContentProvider() {

    private var databaseHelper: Tracker.DatabaseHelper? = null

    override fun onCreate(): Boolean {
        databaseHelper = Tracker.DatabaseHelper(context)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val qb = SQLiteQueryBuilder()

        when (uriMatcher.match(uri)) {
            LOCATIONS -> qb.tables = Tracker.Locations.TABLE_NAME
            LOCATIONS_ITEM -> {
                qb.tables = Tracker.Locations.TABLE_NAME
                qb.appendWhere("${Tracker.Locations._ID}=${uri.pathSegments[1]}")
            }
            SETUP -> qb.tables = Tracker.Setup.TABLE_NAME
            SETUP_ITEM -> {
                qb.tables = Tracker.Setup.TABLE_NAME
                qb.appendWhere("${Tracker.Setup._ID}=${uri.pathSegments[1]}")
            }
            DETAILS -> qb.tables = Tracker.Details.TABLE_NAME
            DETAILS_ITEM -> {
                qb.tables = Tracker.Details.TABLE_NAME
                qb.appendWhere("${Tracker.Details._ID}=${uri.pathSegments[1]}")
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        val db = databaseHelper?.readableDatabase
        val cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        context?.contentResolver?.let { resolver ->
            cursor?.setNotificationUri(resolver, uri)
        }
        return cursor
    }

    override fun insert(uri: Uri, initialValues: ContentValues?): Uri? {
        val values = if (initialValues != null) ContentValues(initialValues) else ContentValues()
        val db = databaseHelper?.writableDatabase
        val rowId: Long

        when (uriMatcher.match(uri)) {
            SETUP -> {
                rowId = db?.insert(Tracker.Setup.TABLE_NAME, Tracker.Setup._FID, values) ?: -1
                if (rowId > 0) {
                    val noteUri = ContentUris.withAppendedId(Tracker.Setup.CONTENT_URI, rowId)
                    context?.contentResolver?.notifyChange(noteUri, null)
                    return noteUri
                }
            }
            LOCATIONS -> {
                rowId = db?.insert(Tracker.Locations.TABLE_NAME, Tracker.Locations._FID, values) ?: -1
                if (rowId > 0) {
                    val noteUri = ContentUris.withAppendedId(Tracker.Locations.CONTENT_URI, rowId)
                    context?.contentResolver?.notifyChange(noteUri, null)
                    return noteUri
                }
            }
            DETAILS -> {
                rowId = db?.insert(Tracker.Details.TABLE_NAME, Tracker.Details.ID, values) ?: -1
                if (rowId > 0) {
                    val noteUri = ContentUris.withAppendedId(Tracker.Details.CONTENT_URI, rowId)
                    context?.contentResolver?.notifyChange(noteUri, null)
                    return noteUri
                }
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        throw SQLException("Failed to insert row into $uri")
    }

    override fun delete(uri: Uri, where: String?, whereArgs: Array<String>?): Int {
        val db = databaseHelper?.writableDatabase ?: return 0
        val count: Int

        when (uriMatcher.match(uri)) {
            SETUP -> count = db.delete(Tracker.Setup.TABLE_NAME, where, whereArgs)
            SETUP_ITEM -> {
                val resultId = uri.pathSegments[1]
                val extraWhere = if (!TextUtils.isEmpty(where)) " AND ($where)" else ""
                val selection = "${Tracker.Setup._ID}=$resultId$extraWhere"
                count = db.delete(Tracker.Setup.TABLE_NAME, selection, whereArgs)
            }
            LOCATIONS -> count = db.delete(Tracker.Locations.TABLE_NAME, where, whereArgs)
            LOCATIONS_ITEM -> {
                val resultId = uri.pathSegments[1]
                val extraWhere = if (!TextUtils.isEmpty(where)) " AND ($where)" else ""
                val selection = "${Tracker.Locations._ID}=$resultId$extraWhere"
                count = db.delete(Tracker.Locations.TABLE_NAME, selection, whereArgs)
            }
            DETAILS -> count = db.delete(Tracker.Details.TABLE_NAME, where, whereArgs)
            DETAILS_ITEM -> {
                val queryId = uri.pathSegments[1]
                val extraWhere = if (!TextUtils.isEmpty(where)) " AND ($where)" else ""
                val selection = "${Tracker.Details._ID}=$queryId$extraWhere"
                count = db.delete(Tracker.Details.TABLE_NAME, selection, whereArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        where: String?,
        whereArgs: Array<String>?
    ): Int {
        val db = databaseHelper?.writableDatabase ?: return 0
        val count: Int

        when (uriMatcher.match(uri)) {
            DETAILS -> count = db.update(Tracker.Details.TABLE_NAME, values, where, whereArgs)
            DETAILS_ITEM -> {
                val noteId = uri.pathSegments[1]
                val extraWhere = if (!TextUtils.isEmpty(where)) " AND ($where)" else ""
                val selection = "${Tracker.Details._ID}=$noteId$extraWhere"
                count = db.update(Tracker.Details.TABLE_NAME, values, selection, whereArgs)
            }
            SETUP -> count = db.update(Tracker.Setup.TABLE_NAME, values, where, whereArgs)
            SETUP_ITEM -> {
                val noteId = uri.pathSegments[1]
                val extraWhere = if (!TextUtils.isEmpty(where)) " AND ($where)" else ""
                val selection = "${Tracker.Setup._ID}=$noteId$extraWhere"
                count = db.update(Tracker.Setup.TABLE_NAME, values, selection, whereArgs)
            }
            LOCATIONS -> count = db.update(Tracker.Locations.TABLE_NAME, values, where, whereArgs)
            LOCATIONS_ITEM -> {
                val noteId = uri.pathSegments[1]
                val extraWhere = if (!TextUtils.isEmpty(where)) " AND ($where)" else ""
                val selection = "${Tracker.Locations._ID}=$noteId$extraWhere"
                count = db.update(Tracker.Locations.TABLE_NAME, values, selection, whereArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            SETUP -> Tracker.Setup.CONTENT_TYPE
            SETUP_ITEM -> Tracker.Setup.CONTENT_ITEM_TYPE
            LOCATIONS -> Tracker.Locations.CONTENT_TYPE
            LOCATIONS_ITEM -> Tracker.Locations.CONTENT_ITEM_TYPE
            DETAILS -> Tracker.Details.CONTENT_TYPE
            DETAILS_ITEM -> Tracker.Details.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    companion object {
        private const val DETAILS = 1
        private const val DETAILS_ITEM = 2
        private const val SETUP = 11
        private const val SETUP_ITEM = 22
        private const val LOCATIONS = 111
        private const val LOCATIONS_ITEM = 222

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(Tracker.AUTHORITY, Tracker.Details.TABLE_NAME, DETAILS)
            addURI(Tracker.AUTHORITY, "${Tracker.Details.TABLE_NAME}/#", DETAILS_ITEM)
            addURI(Tracker.AUTHORITY, Tracker.Setup.TABLE_NAME, SETUP)
            addURI(Tracker.AUTHORITY, "${Tracker.Setup.TABLE_NAME}/#", SETUP_ITEM)
            addURI(Tracker.AUTHORITY, Tracker.Locations.TABLE_NAME, LOCATIONS)
            addURI(Tracker.AUTHORITY, "${Tracker.Locations.TABLE_NAME}/#", LOCATIONS_ITEM)
        }
    }
}
