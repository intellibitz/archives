package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.db.IntellibitzItemColumns
import intellibitz.intellidroid.db.UserEmailJoinColumns
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.io.IOException
import java.util.Set

/**
 *
 */
class UserEmailContentProvider : ContentProvider() {

    companion object {
        const val TAG = "UserEmailCP"
        //    The content provider scheme
        const val SCHEME = "content://"
        //    users email accounts
        const val TABLE_USEREMAILS = "useremail"
        // MIME types used for searching words or looking up a single definition
        const val USERS_EMAIL_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/vnd.intellibitz.android.intellibitzdb/all"
        const val USERS_EMAIL_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/vnd.intellibitz.android.intellibitzdb/_id"
        const val USERS_EMAIL_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id"
        const val CREATE_TABLE_USERS_EMAILS_JOIN = "CREATE TABLE "
                + UserContentProvider.TABLE_USERS_EMAILS_JOIN + "( " +
                UserEmailJoinColumns.KEY_ID + " INTEGER PRIMARY KEY," +
                UserEmailJoinColumns.KEY_EMAIL_ID + " INTEGER," +
                UserEmailJoinColumns.KEY_USER_ID + " INTEGER," +
                UserEmailJoinColumns.KEY_TIMESTAMP + " LONG" + ")"
        const val CREATE_TABLE_USEREMAILS = "CREATE TABLE "
                + TABLE_USEREMAILS + ContactItemColumns.TABLE_CONTACTS_SCHEMA
        // UriMatcher stuff
        private const val USERS_EMAIL_DIR_TYPE = 0
        private const val USERS_EMAIL_ITEM_TYPE = 1
        private const val USERS_EMAIL_DATA_ITEM_TYPE = 2
        private const val SEARCH_SUGGEST = 3
        private const val REFRESH_SHORTCUT = 4
        var AUTHORITY = "intellibitz.intellidroid.content.UserEmailContentProvider"
        val CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_USEREMAILS)
        private val sURIMatcher = buildUriMatcher()

        /**
         * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
         */
        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            // to get definitions...
            matcher.addURI(AUTHORITY, TABLE_USEREMAILS, USERS_EMAIL_DIR_TYPE)
            matcher.addURI(AUTHORITY, TABLE_USEREMAILS +
                    "/#", USERS_EMAIL_ITEM_TYPE)
            matcher.addURI(AUTHORITY, TABLE_USEREMAILS +
                    "/*", USERS_EMAIL_DATA_ITEM_TYPE)
            // to get suggestions...
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST)

            /* The following are unused in this implementation, but if we include
             * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
             * could expect to receive refresh queries when a shortcutted suggestion is displayed in
             * Quick Search Box, in which case, the following Uris would be provided and we
             * would return a cursor with a single item representing the refreshed suggestion data.
             */
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT)
            return matcher
        }

        fun fillContentValuesFromUserEmailItem(userEmailItem: ContactItem,
                                               values: ContentValues): ContentValues {
            DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(userEmailItem, values)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_EMAIL, userEmailItem.email)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_EMAIL_CODE, userEmailItem.emailCode)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_ACTIVE, userEmailItem.active)
            return values
        }

        fun getUserEmailsCursor(databaseHelper: DatabaseHelper, item: ContactItem): Cursor? {
            return databaseHelper.query(TABLE_USEREMAILS, arrayOf(UserEmailJoinColumns.KEY_ID),
                UserEmailJoinColumns.KEY_EMAIL + " = ? and " +
                        UserEmailJoinColumns.KEY_NAME + " = ?",
                arrayOf(item.email, item.name), null, null, null)
        }

        fun createOrUpdateUserEmail(databaseHelper: DatabaseHelper, item: ContactItem, id: Long): Long {
            val values = ContentValues()
            fillContentValuesFromUserEmailItem(item, values)
//        Cursor cursor = getAllUserEmailsCursor(item, id);
            val cursor = getUserEmailsCursor(databaseHelper, item)
            val _id: Long
            if (null == cursor || 0 == cursor.count) {
                if (cursor != null) cursor.close()
                _id = databaseHelper.insert(TABLE_USEREMAILS, null, values)
//            Log.d(TAG, "EmailItem: " + _id + " User: " + id);
                item._id = _id
            } else {
                _id = cursor.getLong(cursor.getColumnIndex(UserEmailJoinColumns.KEY_ID))
                item._id = _id
                cursor.close()
                databaseHelper.update(TABLE_USEREMAILS, values, UserEmailJoinColumns.KEY_ID + " = ?",
                    arrayOf(_id.toString()))
            }
//        creates the join
            createOrUpdateUserEmailJoin(databaseHelper, _id, id)
            return item._id
        }

        fun getUserEmailJoin(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var _id: Long = 0
            val c = databaseHelper.query(UserContentProvider.TABLE_USERS_EMAILS_JOIN, arrayOf(IntellibitzItemColumns.KEY_ID),
                UserEmailJoinColumns.KEY_EMAIL_ID + " = ? and " +
                        UserEmailJoinColumns.KEY_USER_ID + " = ?",
                arrayOf(id.toString(), fk.toString()), null, null, null)
            if (c != null && c.count > 0) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }

        fun createOrUpdateUserEmailJoin(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var _id = getUserEmailJoin(databaseHelper, id, fk)
            val values = ContentValues()
            values.put(UserEmailJoinColumns.KEY_EMAIL_ID, id)
            values.put(UserEmailJoinColumns.KEY_USER_ID, fk)
            values.put(UserEmailJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis())
            if (0 == _id) {
                _id = databaseHelper.insert(UserContentProvider.TABLE_USERS_EMAILS_JOIN, null, values)
            } else {
                databaseHelper.update(UserContentProvider.TABLE_USERS_EMAILS_JOIN, values,
                    UserEmailJoinColumns.KEY_ID + " = ?", arrayOf(_id.toString()))
            }
            return _id
        }

        fun createOrUpdateUserEmails(databaseHelper: DatabaseHelper, items: Set<ContactItem>?, id: Long): LongArray {
            if (null == items) return LongArray(0)
            val ids = LongArray(items.size)
            var i = 0
            for (email in items) {
                ids[i++] = createOrUpdateUserEmail(databaseHelper, email, id)
            }
            return ids
        }

        fun getUserEmailsJoin(databaseHelper: DatabaseHelper, id: Long): Cursor? {
            val selectQuery = "SELECT  *, u._id as u_id, u.name as uname, em._id as emid, em.name as emname, em.type as emtype FROM " +
                    UserContentProvider.TABLE_USERS + " u " +
                    " left join " + UserContentProvider.TABLE_USERS_EMAILS_JOIN +
                    " uem on u.[_id] = uem.[user_id]  " +
                    " left join " + TABLE_USEREMAILS + " em  on uem.[email_id] = em.[_id] " +
                    " WHERE u." + ContactItemColumns.KEY_ID + " = " + id + ""

/*
        String selectQuery = "SELECT  * FROM " + TABLE_MSGTHREADEMAILS + " WHERE "
                + UserEmailJoinColumns.KEY_ID + " = " + id;
*/
//        Log.e(TAG, selectQuery);
            return databaseHelper.rawQuery(selectQuery, null)
        }

        fun getUserEmailsJoin(databaseHelper: DatabaseHelper, id: String): Cursor? {

/*

SELECT  * FROM users mt
left join users_emails  mte on mt.[_id] = mte.[user_id]
left join emails em  on mte.[email_id] = em.[_id]
WHERE mt.id = 'USRMASTER_919840348914'

        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " mt, " +
                TABLE_MSGTHREADEMAILS + " tm, " + TABLE_USERS_EMAILS_JOIN +
                " ttm WHERE mt." +
                ContactItemColumns.KEY_DATA_ID + " = '" + id +
                "'" + " AND tm." + UserEmailJoinColumns.KEY_ID + " = " + "ttm." +
                UserEmailJoinColumns.KEY_EMAIL_ID + " AND mt." +
                ContactItemColumns.KEY_ID +
                " = " + "ttm." + UserEmailJoinColumns.KEY_USER_ID;
*/
            val selectQuery = "SELECT  *, u._id as u_id, u.name as uname, " +
                    " em._id as emid, em.name as emname, em.type as emtype FROM " +
                    UserContentProvider.TABLE_USERS +
                    " u  left join " + UserContentProvider.TABLE_USERS_EMAILS_JOIN +
                    " uem on u.[_id] = uem.[user_id]  " +
                    " left join " + TABLE_USEREMAILS +
                    " em  on uem.[email_id] = em.[_id]  WHERE u." +
                    ContactItemColumns.KEY_DATA_ID + " = '" + id + "'"


//        Log.e(TAG, selectQuery);
            return databaseHelper.rawQuery(selectQuery, null)
        }

        fun removeUserEmail(databaseHelper: DatabaseHelper, id: Long) {
            val db = databaseHelper.readableDatabase
            db.delete(TABLE_USEREMAILS, UserEmailJoinColumns.KEY_ID + " = ?", arrayOf(id.toString()))
            db.delete(UserContentProvider.TABLE_USERS_EMAILS_JOIN,
                UserEmailJoinColumns.KEY_EMAIL_ID + " = ?",
                arrayOf(id.toString()))
        }

        fun populateUserEmailsJoinByDataId(user: ContactItem, context: Context): ContactItem {
            val dataId = user.dataId
            var uri = CONTENT_URI
            var sel: String? = null
            var selArgs: Array<String>? = null
            if (!TextUtils.isEmpty(dataId)) {
                uri = Uri.withAppendedPath(CONTENT_URI, dataId)
                sel = ContactItemColumns.KEY_DATA_ID + " = ? "
                selArgs = arrayOf(dataId)
            }
//        the above selection and selection args has no effect, since cp does customized query
//        // TODO: 1/9/16
//        to change cp to accept sel and selargs
            val cursor = context.contentResolver.query(uri, null, sel, selArgs, null)
            return UserContentProvider.packUserEmailsFromCursor(cursor, user)
        }

        fun populateUserEmailsJoinById(user: ContactItem, context: Context): ContactItem {
            val id = user._id
            var uri = CONTENT_URI
            var sel: String? = null
            var selArgs: Array<String>? = null
            if (id > 0) {
                uri = ContentUris.withAppendedId(CONTENT_URI, id)
                sel = ContactItemColumns.KEY_ID + " = ? "
                selArgs = arrayOf(id.toString())
            }
//        the above selection and selection args has no effect, since cp does customized query
//        // TODO: 1/9/16
//        to change cp to accept sel and selargs
            val cursor = context.contentResolver.query(uri, null, sel, selArgs, null)
            return UserContentProvider.packUserEmailsFromCursor(cursor, user)
        }

        @Throws(IOException::class)
        fun savesUserEmailsInDB(user: ContactItem, context: Context): Uri? {
            val values = ContentValues()
            values.put(ContactItem.USER_CONTACT, MainApplicationSingleton.Serializer.serialize(user))
            val contentResolver = context.contentResolver
            return contentResolver.insert(CONTENT_URI, values)
        }

        fun deletesUserEmail(emailId: Long, context: Context): Int {
            val uri = ContentUris.withAppendedId(CONTENT_URI, emailId)
            return context.contentResolver.delete(uri, null, null)
        }

        fun deletesUserEmail(email: String, context: Context): Int {
            val cursor = context.contentResolver.query(CONTENT_URI,
                arrayOf(UserEmailJoinColumns.KEY_ID),
                UserEmailJoinColumns.KEY_DATA_ID + " = ? OR " +
                        ContactItemColumns.KEY_EMAIL + " = ? ",
                arrayOf(email, email), null)
            if (null == cursor) return 0
            var count = 0
            cursor.moveToFirst()
            do {
                val id = cursor.getInt(cursor.getColumnIndex(UserEmailJoinColumns.KEY_ID))
                count += deletesUserEmail(id.toLong(), context)
            } while (cursor.moveToNext())
            cursor.close()
            return count
        }
    }

    private var databaseHelper: DatabaseHelper? = null

    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(context,
            DatabaseHelper.DATABASE_NAME)
        return true
    }

    override fun getType(uri: Uri): String? {
        return when (sURIMatcher.match(uri)) {
            USERS_EMAIL_DIR_TYPE -> USERS_EMAIL_DIR_MIME_TYPE
            USERS_EMAIL_ITEM_TYPE -> USERS_EMAIL_ITEM_MIME_TYPE
            USERS_EMAIL_DATA_ITEM_TYPE -> USERS_EMAIL_DATA_ITEM_MIME_TYPE
            SEARCH_SUGGEST -> SearchManager.SUGGEST_MIME_TYPE
            REFRESH_SHORTCUT -> SearchManager.SHORTCUT_MIME_TYPE
            else -> throw IllegalArgumentException("Unknown URL $uri")
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        var cursor: Cursor? = null
        when (sURIMatcher.match(uri)) {
            USERS_EMAIL_DIR_TYPE -> try {
//                    String selectQuery = "SELECT  * FROM " + UserEmailContentProvider.TABLE_USEREMAILS;
//        Log.e(TAG, selectQuery);
//                    cursor = databaseHelper.rawQuery(selectQuery, null);
                cursor = databaseHelper?.query(TABLE_USEREMAILS,
                    projection, selection, selectionArgs, sortOrder)
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
            USERS_EMAIL_ITEM_TYPE -> try {
                cursor = getUserEmailsJoin(databaseHelper!!, ContentUris.parseId(uri))
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
            USERS_EMAIL_DATA_ITEM_TYPE -> try {
                cursor = getUserEmailsJoin(databaseHelper!!, uri.lastPathSegment)
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
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        when (sURIMatcher.match(uri)) {
            USERS_EMAIL_DIR_TYPE, USERS_EMAIL_ITEM_TYPE -> try {
                // Use the UriMatcher to see what kind of query we have and format the db query accordingly
                val user = MainApplicationSingleton.Serializer.deserialize(
                    values?.getAsByteArray(ContactItem.USER_CONTACT)) as ContactItem
                val emails = user.contactItems
                if (emails != null && !emails.isEmpty()) {
                    val ids = createOrUpdateUserEmails(databaseHelper!!, emails, user._id)
                    val context = context
                    if (null != context) {
                        context.contentResolver.notifyChange(uri, null)
                    }
                    return ContentUris.withAppendedId(UserEmailContentProvider.CONTENT_URI,
                        ids[0])
                }
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
            USERS_EMAIL_DATA_ITEM_TYPE -> try {
                // Use the UriMatcher to see what kind of query we have and format the db query accordingly
                val user = MainApplicationSingleton.Serializer.deserialize(
                    values?.getAsByteArray(ContactItem.USER_CONTACT)) as ContactItem
                val id = createOrUpdateUserEmail(databaseHelper!!,
                    user.getEmail(user.email), user._id)
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(uri, null)
                }
                return ContentUris.withAppendedId(UserEmailContentProvider.CONTENT_URI, id)
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
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        when (sURIMatcher.match(uri)) {
            USERS_EMAIL_DIR_TYPE, USERS_EMAIL_ITEM_TYPE -> try {
                removeUserEmail(databaseHelper!!, ContentUris.parseId(uri))
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(uri, null)
                }
                return 1
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        return 0
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
