package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.io.IOException
import java.util.*

class MessagesEmailContentProvider : ContentProvider() {

    companion object {
        const val TAG = "MessagesEmailCP"

        //    private static final String TABLE_USERS_DEVICES = "users_devices";

        //    The content provider scheme
        const val SCHEME = "content://"
        // MIME types used for searching words or looking up a single definition
        const val MESSAGESEMAIL_DIR_MIME_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.intellibitz.android.intellibitzdb/all"
        const val MESSAGESEMAIL_JOIN_DIR_MIME_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.intellibitz.android.intellibitzdb/join"
        const val MESSAGESEMAIL_ITEM_MIME_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.intellibitz.android.intellibitzdb/_id"
        const val MESSAGESEMAIL_DATA_ITEM_MIME_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.intellibitz.android.intellibitzdb/id"
        const val MESSAGESEMAIL_JOIN_ITEM_MIME_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.intellibitz.android.intellibitzdb/join/_id"
        const val MESSAGESEMAIL_JOIN_DATA_ITEM_MIME_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.intellibitz.android.intellibitzdb/join/id"
        const val MESSAGESEMAIL_RAW_DIR_MIME_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.intellibitz.android.intellibitzdb/raw/all"
        // UriMatcher stuff
        private const val MESSAGESEMAIL_DIR_TYPE = 1
        private const val MESSAGESEMAIL_JOIN_DIR_TYPE = 2
        private const val MESSAGESEMAIL_ITEM_TYPE = 3
        private const val MESSAGESEMAIL_DATA_ITEM_TYPE = 4
        private const val MESSAGESEMAIL_JOIN_ITEM_TYPE = 5
        private const val MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE = 6
        private const val MESSAGESEMAIL_RAW_DIR_TYPE = 7
        private const val SEARCH_SUGGEST = 8
        private const val REFRESH_SHORTCUT = 9
        var AUTHORITY = "intellibitz.intellidroid.content.MessagesEmailContentProvider"
        val CONTENT_URI = Uri.parse(
            "$SCHEME$AUTHORITY/${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}"
        )
        val JOIN_CONTENT_URI = Uri.parse(
            "$SCHEME$AUTHORITY/join_${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}"
        )
        //    to execute raw sql.. example select count(*)
        val RAW_CONTENT_URI = Uri.parse(
            "$SCHEME$AUTHORITY/raw_${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}"
        )
        private val URI_MATCHER = buildUriMatcher()

        /**
         * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
         */
        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            // to get definitions...
            matcher.addURI(AUTHORITY, MessageEmailContentProvider.TABLE_MESSAGESEMAIL,
                MESSAGESEMAIL_DIR_TYPE)
            matcher.addURI(AUTHORITY,
                "join_${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}",
                MESSAGESEMAIL_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, "${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}/#",
                MESSAGESEMAIL_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}/*",
                MESSAGESEMAIL_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY,
                "join_${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}/#",
                MESSAGESEMAIL_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY,
                "join_${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}/*",
                MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY,
                "raw_${MessageEmailContentProvider.TABLE_MESSAGESEMAIL}", MESSAGESEMAIL_RAW_DIR_TYPE)
            // to get suggestions...
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, "${SearchManager.SUGGEST_URI_PATH_QUERY}/*", SEARCH_SUGGEST)

            /* The following are unused in this implementation, but if we include
             * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
             * could expect to receive refresh queries when a shortcutted suggestion is displayed in
             * Quick Search Box, in which case, the following Uris would be provided and we
             * would return a cursor with a single item representing the refreshed suggestion data.
             */
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, "${SearchManager.SUGGEST_URI_PATH_SHORTCUT}/*", REFRESH_SHORTCUT)
            return matcher
        }
    }

    private var databaseHelper: DatabaseHelper? = null

    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(context, DatabaseHelper.DATABASE_NAME)
        return true
    }

    @Nullable
    override fun getType(@NonNull uri: Uri): String? {
        return when (URI_MATCHER.match(uri)) {
            MESSAGESEMAIL_DIR_TYPE -> MESSAGESEMAIL_DIR_MIME_TYPE
            MESSAGESEMAIL_JOIN_DIR_TYPE -> MESSAGESEMAIL_JOIN_DIR_MIME_TYPE
            MESSAGESEMAIL_ITEM_TYPE -> MESSAGESEMAIL_ITEM_MIME_TYPE
            MESSAGESEMAIL_DATA_ITEM_TYPE -> MESSAGESEMAIL_DATA_ITEM_MIME_TYPE
            MESSAGESEMAIL_JOIN_ITEM_TYPE -> MESSAGESEMAIL_JOIN_ITEM_MIME_TYPE
            MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE -> MESSAGESEMAIL_JOIN_DATA_ITEM_MIME_TYPE
            MESSAGESEMAIL_RAW_DIR_TYPE -> MESSAGESEMAIL_RAW_DIR_MIME_TYPE
            SEARCH_SUGGEST -> SearchManager.SUGGEST_MIME_TYPE
            REFRESH_SHORTCUT -> SearchManager.SHORTCUT_MIME_TYPE
            else -> throw IllegalArgumentException("Unknown URL $uri")
        }
    }

    @Nullable
    override fun query(
        @NonNull uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        var cursor: Cursor? = null
//        String id = uri.getLastPathSegment();
        when (URI_MATCHER.match(uri)) {
            MESSAGESEMAIL_DIR_TYPE, MESSAGESEMAIL_ITEM_TYPE, MESSAGESEMAIL_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper?.query(
                        MessageEmailContentProvider.TABLE_MESSAGESEMAIL,
                        projection, selection, selectionArgs, sortOrder
                    )
                    if (null != cursor) {
                        val context = context
                        if (null != context) {
                            cursor.setNotificationUri(context.contentResolver, uri)
                        }
                    }
                    return cursor
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            MESSAGESEMAIL_JOIN_DIR_TYPE -> {
                try {
                    cursor = MessageEmailContentProvider.getMessagesThreadShalGroupJoin(
                        databaseHelper,
                        selection, selectionArgs, sortOrder
                    )
                    if (null != cursor) {
                        val context = context
                        if (null != context) {
                            cursor.setNotificationUri(context.contentResolver, uri)
                        }
                    }
                    return cursor
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            MESSAGESEMAIL_JOIN_ITEM_TYPE, MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE -> {
                try {
                    cursor = MessageEmailContentProvider.getMessagesThreadJoin(
                        databaseHelper,
                        selection, selectionArgs, sortOrder
                    )
                    if (null != cursor) {
                        val context = context
                        if (null != context) {
                            cursor.setNotificationUri(context.contentResolver, uri)
                        }
                    }
                    return cursor
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            MESSAGESEMAIL_RAW_DIR_TYPE -> {
                try {
                    cursor = databaseHelper?.getRawCursor(selection, selectionArgs)
                    if (null != cursor) {
                        val context = context
                        if (null != context) {
                            cursor.setNotificationUri(context.contentResolver, uri)
                        }
                    }
                    return cursor
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return cursor
    }

    @Nullable
    override fun insert(@NonNull uri: Uri, values: ContentValues?): Uri? {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        val vals = values?.getAsByteArray(MessageItem.TAG)
        when (URI_MATCHER.match(uri)) {
            MESSAGESEMAIL_DIR_TYPE, MESSAGESEMAIL_JOIN_DIR_TYPE -> {
                try {
                    val items = MainApplicationSingleton.Serializer.deserialize(vals) as Collection<MessageItem>
                    if (null == items) {
                        return null
                    }
                    val ids = MessageEmailContentProvider.createOrUpdateMessages(databaseHelper, items)
                    if (null == ids || ids.size != items.size) {
                        return null
                    }
                    val context = context
                    if (null != context) {
                        context.contentResolver.notifyChange(uri, null)
                    }
                    return uri
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            MESSAGESEMAIL_ITEM_TYPE, MESSAGESEMAIL_DATA_ITEM_TYPE -> {
                try {
                    val item = MainApplicationSingleton.Serializer.deserialize(vals) as MessageItem
                    val id = MessageEmailContentProvider.createOrUpdateMessages(databaseHelper, item)
                    val insertUri = ContentUris.withAppendedId(
                        MessagesEmailContentProvider.CONTENT_URI, id
                    )
                    val context = context
                    if (null != context) {
//                        context.getContentResolver().notifyChange(uri, null);
                        context.contentResolver.notifyChange(insertUri, null)
                    }
                    return insertUri
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return uri
    }

    override fun update(
        @NonNull uri: Uri,
        values: ContentValues?,
        where: String?,
        whereArgs: Array<String>?
    ): Int {
        var id = 0
        var updateUri: Uri? = null
        when (URI_MATCHER.match(uri)) {
            MESSAGESEMAIL_DIR_TYPE, MESSAGESEMAIL_ITEM_TYPE, MESSAGESEMAIL_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper?.update(
                        MessageEmailContentProvider.TABLE_MESSAGESEMAIL, values, where, whereArgs
                    ) ?: 0
                    updateUri = ContentUris.withAppendedId(
                        MessagesEmailContentProvider.CONTENT_URI, id.toLong()
                    )
                    val context = context
                    if (null != context) {
                        context.contentResolver.notifyChange(updateUri, null)
                    }
                    return id
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            MESSAGESEMAIL_JOIN_DIR_TYPE, MESSAGESEMAIL_JOIN_ITEM_TYPE, MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE -> {
                try {
                    val vals1 = values?.getAsByteArray(MessageItem.TAG)
                    val item = MainApplicationSingleton.Serializer.deserialize(vals1) as MessageItem
                    id = MessageEmailContentProvider.createOrUpdateMessages(databaseHelper, item).toInt()
                    updateUri = ContentUris.withAppendedId(MessagesEmailContentProvider.CONTENT_URI, id.toLong())
                    val context = context
                    if (null != context) {
                        context.contentResolver.notifyChange(updateUri, null)
                    }
                    return id
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return 0
    }

    override fun delete(@NonNull uri: Uri, where: String?, whereArgs: Array<String>?): Int {
        var id = 0
        var delUri: Uri? = null
        when (URI_MATCHER.match(uri)) {
            MESSAGESEMAIL_DIR_TYPE, MESSAGESEMAIL_ITEM_TYPE, MESSAGESEMAIL_DATA_ITEM_TYPE,
            MESSAGESEMAIL_JOIN_DIR_TYPE, MESSAGESEMAIL_JOIN_ITEM_TYPE, MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper?.delete(
                        MessageEmailContentProvider.TABLE_MESSAGESEMAIL, where, whereArgs
                    ) ?: 0
                    delUri = ContentUris.withAppendedId(
                        MessagesEmailContentProvider.CONTENT_URI, id.toLong()
                    )
                    val context = context
                    if (null != context) {
                        context.contentResolver.notifyChange(delUri, null)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return id
    }

    /**
     * A test package can call this to get a handle to the database underlying NotePadProvider,
     * so it can insert test data into the database. The test case class is responsible for
     * instantiating the provider in a test context; {@link android.test.ProviderTestCase2} does
     * this during the call to setUp()
     *
     * @return a handle to the database helper object for the provider's data.
     */
    fun getOpenHelperForTest(): DatabaseHelper? {
        return databaseHelper
    }

}
