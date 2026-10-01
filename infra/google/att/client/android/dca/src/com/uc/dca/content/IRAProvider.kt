/**
 *
 */
package com.uc.dca.content

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

/**
 * @author muthu
 *
 */
class IRAProvider : ContentProvider() {
    private var databaseHelper: IncidentReport.DatabaseHelper? = null

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // TODO Auto-generated method stub
        return 0
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
    override fun getType(uri: Uri): String? {
        // TODO Auto-generated method stub
        return when (uriMatcher.match(uri)) {
            DETAILS -> IncidentReport.Details.CONTENT_TYPE
            DETAILS_ITEM -> IncidentReport.Details.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
    override fun insert(uri: Uri, initialValues: ContentValues?): Uri? {
        // TODO Auto-generated method stub
        val values = ContentValues(initialValues)
        val db = databaseHelper!!.writableDatabase
        val rowId: Long
        when (uriMatcher.match(uri)) {
            DETAILS -> {
                rowId = db.insert(
                    IncidentReport.Details.TABLE_NAME,
                    IncidentReport.Details.ID,
                    values
                )
                if (rowId > 0) {
                    val noteUri =
                        ContentUris.withAppendedId(IncidentReport.Details.CONTENT_URI, rowId)
                    context!!.contentResolver.notifyChange(noteUri, null)
                    return noteUri
                }
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        throw SQLException("Failed to insert row into $uri")
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
    override fun onCreate(): Boolean {
        // TODO Auto-generated method stub
        databaseHelper = IncidentReport.DatabaseHelper(context)
        return true
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        // TODO Auto-generated method stub
        val qb = SQLiteQueryBuilder()

        when (uriMatcher.match(uri)) {
            DETAILS -> qb.tables = IncidentReport.Details.TABLE_NAME
            DETAILS_ITEM -> {
                qb.tables = IncidentReport.Details.TABLE_NAME
                qb.appendWhere(
                    IncidentReport.Details._ID + "=" +
                            uri.pathSegments[1]
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        // Get the database and run the query
        val db = databaseHelper!!.readableDatabase
        val c = qb.query(
            db, projection, selection, selectionArgs, null,
            null, sortOrder
        )

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    /* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        // TODO Auto-generated method stub
        return 0
    }

    companion object {
        private const val TAG = "ContentProvider"

        private const val DETAILS = 1
        private const val DETAILS_ITEM = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(IncidentReport.AUTHORITY, IncidentReport.Details.TABLE_NAME, DETAILS)
            addURI(
                IncidentReport.AUTHORITY,
                IncidentReport.Details.TABLE_NAME + "/#",
                DETAILS_ITEM
            )
        }
    }
}
