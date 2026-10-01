package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.io.IOException
import java.util.*

/**
 *
 */
class MsgChatContactContentProvider : ContentProvider() {

    companion object {
        const val TAG = "MsgChatContactCP"
        const val TABLE_MSGCHATCONTACT = "msgchatcontact"
        const val TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN = "msgchatcontact_intellibitzcontacts"
        const val CREATE_TABLE_MSGCHATCONTACT = "CREATE TABLE " + TABLE_MSGCHATCONTACT + ContactItemColumns.TABLE_CONTACTS_SCHEMA
        //    The content provider scheme
        const val SCHEME = "content://"
        // MIME types used for searching words or looking up a single definition
        const val CONTACT_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all"
        const val CONTACT_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id"
        const val CONTACT_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id"
        const val CONTACT_JOIN_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/all"
        const val CONTACT_JOIN_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id"
        const val CONTACT_JOIN_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id"
        const val CONTACT_RAW_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all"
        const val CREATE_TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN = "CREATE TABLE " + TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN + "( " + ContactIntellibitzContactJoinColumns.KEY_ID + " INTEGER PRIMARY KEY," + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + " INTEGER," + ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " INTEGER," + ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP + " LONG" + ")"
        // UriMatcher stuff
        private const val CONTACT_DIR_TYPE = 0
        private const val CONTACT_ITEM_TYPE = 1
        private const val CONTACT_DATA_ITEM_TYPE = 2
        private const val CONTACT_JOIN_DIR_TYPE = 3
        private const val CONTACT_JOIN_ITEM_TYPE = 4
        private const val CONTACT_JOIN_DATA_ITEM_TYPE = 5
        private const val CONTACT_RAW_DIR_TYPE = 6
        private const val SEARCH_SUGGEST = 7
        private const val REFRESH_SHORTCUT = 8
        var AUTHORITY = "intellibitz.intellidroid.content.MsgChatContactContentProvider"
        //    to get only pure contacts minus join
        val CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_MSGCHATCONTACT)
        //    to execute contacts with join.. example with mobiles and emails
        val JOIN_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGCHATCONTACT)
        //    to execute raw sql.. example select count(*)
        val RAW_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGCHATCONTACT)
        private val URI_MATCHER = buildUriMatcher()

        /**
         * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
         */
        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            // to get definitions...
            matcher.addURI(AUTHORITY, TABLE_MSGCHATCONTACT, CONTACT_DIR_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGCHATCONTACT + "/#", CONTACT_ITEM_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGCHATCONTACT + "/*", CONTACT_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGCHATCONTACT, CONTACT_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGCHATCONTACT + "/#", CONTACT_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGCHATCONTACT + "/*", CONTACT_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "raw_" + TABLE_MSGCHATCONTACT, CONTACT_RAW_DIR_TYPE)
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

        fun fillContentValuesFromContactItem(item: ContactItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_ID, item.dataId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_CONTACTID, item.deviceContactId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE_ID, item.typeId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_INTELLIBITZ_ID, item.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_NAME, item.name)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_FIRST_NAME, item.firstName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_LAST_NAME, item.lastName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DISPLAY_NAME, item.displayName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE, item.type)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_STATUS, item.status)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PIC, item.profilePic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_GROUP, item.isGroup)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_EMAIL, item.isEmailItem)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_ANONYMOUS, item.isAnonymous)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_DEVICE, item.isDevice)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_CLOUD, item.isCloud)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TIMESTAMP, item.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis(item.timestamp))
            return values
        }

        fun updatesTypeInDB(contactItem: ContactItem, context: Context): Int {
            val contentValues = ContentValues()
            contentValues.put(ContactItemColumns.KEY_TYPE, contactItem.type)
            return context.applicationContext.contentResolver.update(CONTENT_URI, contentValues, ContactItemColumns.KEY_DATA_ID + " = ?", arrayOf(contactItem.dataId))
        }

        fun queryContactForDBId(contactItem: ContactItem, context: Context): Long {
            val cursor = context.contentResolver.query(CONTENT_URI, arrayOf(ContactItemColumns.KEY_ID), ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ", arrayOf(contactItem.dataId), null)
            if (null == cursor || 0 == cursor.count) return 0
            val _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
            cursor.close()
            contactItem._id = _id
            return _id
        }

        fun createOrUpdateIntellibitzContactByContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem): Long {
            //        checks if emails, or in device contacts.. if yes, join device contact
            var id: Long = 0
            val cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_ID), ContactItemColumns.KEY_EMAILS + " LIKE '%' || ? || '%' ", arrayOf(contactItem.intellibitzId), null)
            if (cursor != null && cursor.count > 0) {
                id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
            }
            if (id > 0) {
                //            if device contact has intellibitz contactables in emails
                contactItem.isDevice = true
            }
            // updates from the device contact info, will go into the contact as well
            // the intellibitz contact update.. below method will take care of keeping intellibitz contact in sync with device contact
            val _id = IntellibitzContactContentProvider.createOrUpdateIntellibitzContact(databaseHelper, db, contactItem)
            if (id > 0) {
                //            if device contact has intellibitz contactables in emails
                IntellibitzContactContentProvider.createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper, db, _id, id)
            }
            //        // TODO: 30-06-2016
            //        creates or updates intellibitz contact, email and mobile join
            /*
            val selectedMobile = item.mobileItem
            if (selectedMobile != null) createOrUpdateIntellibitzContactMobileJoin(db, selectedMobile, item._id)
            val selectedEmail = item.emailItem
            if (selectedEmail != null) createOrUpdateIntellibitzContactEmailJoin(db, selectedEmail, item._id)
            */
            return contactItem._id
        }

        fun createOrUpdateContactIntellibitzContactByContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem, id: Long): Long {
            //            ContactItem contactItem = item.getIntellibitzContactItem();
            if (null == contactItem) return 0
            //        // TODO: 04-07-2016
            //        contact item to be updated, with intellibitz contact, device contact details earlier than this
            //        reverse the update order.. fetch the device contacts, then update contact item
            val cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
                    //                projection
                    arrayOf(ContactItemColumns.KEY_ID, ContactItemColumns.KEY_DEVICE_CONTACTID, ContactItemColumns.KEY_FIRST_NAME, ContactItemColumns.KEY_LAST_NAME, ContactItemColumns.KEY_DISPLAY_NAME),
                    //                selection
                    ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    //                selection args
                    arrayOf(contactItem.intellibitzId), null)
            if (null == cursor || 0 == cursor.count) {
                val _id = createOrUpdateIntellibitzContactByContact(databaseHelper, db, contactItem)
                return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id)
            } else {
                val _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                val did = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                val first = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                val last = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                val disp = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                cursor.close()
                //            // TODO: 04-07-2016
                //            too late.. do it earlier and save the contact
                //            the hack is for msg thread groups.. so it can get device contact from contact item
                contactItem.deviceContactId = did
                contactItem.firstName = first
                contactItem.lastName = last
                contactItem.displayName = disp
                return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id)
            }
        }

        fun getContactIntellibitzContactCursorJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Cursor? {
            val args = arrayOf(item.dataId, id.toString())
            val selectQuery = "SELECT  * FROM " + MsgChatContactContentProvider.TABLE_MSGCHATCONTACT + " nt " + " left join " + TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN + " ntm on nt.[" + ContactIntellibitzContactJoinColumns.KEY_ID + "] = ntm.[" + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + "]  " + " left join " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT + " mt on ntm.[" + ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + "] = mt.[" + ContactItemColumns.KEY_ID + "] " + " WHERE " + " mt." + ContactItemColumns.KEY_ID + " = ? " + " AND nt." + ContactItemColumns.KEY_ID + " = ? "
            //        Log.e(TAG, selectQuery);
            return databaseHelper.rawQuery(db, selectQuery, args)
        }

        fun createOrUpdateContactIntellibitzContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id = getContactIntellibitzContactJoin(databaseHelper, db, id, fk)
            val values = ContentValues()
            values.put(ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID, id)
            values.put(ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID, fk)
            values.put(ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis())
            if (0 == _id) {
                _id = databaseHelper.insert(db, TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN, null, values)
            } else {
                _id = databaseHelper.update(db, TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN, values, ContactIntellibitzContactJoinColumns.KEY_ID + " = ?", arrayOf(_id.toString()))
            }
            return _id
        }

        fun getContactIntellibitzContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = 0
            val c = databaseHelper.query(db, TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN, arrayOf(ContactIntellibitzContactJoinColumns.KEY_ID), ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " = ? and " + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + " = ?", arrayOf(id.toString(), fk.toString()), null, null, null)
            if (c != null && c.count > 0) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
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
            CONTACT_DIR_TYPE -> CONTACT_DIR_MIME_TYPE
            CONTACT_ITEM_TYPE -> CONTACT_ITEM_MIME_TYPE
            CONTACT_DATA_ITEM_TYPE -> CONTACT_DATA_ITEM_MIME_TYPE
            CONTACT_JOIN_DIR_TYPE -> CONTACT_JOIN_DIR_MIME_TYPE
            CONTACT_JOIN_ITEM_TYPE -> CONTACT_JOIN_ITEM_MIME_TYPE
            CONTACT_JOIN_DATA_ITEM_TYPE -> CONTACT_JOIN_DATA_ITEM_MIME_TYPE
            CONTACT_RAW_DIR_TYPE -> CONTACT_RAW_DIR_MIME_TYPE
            SEARCH_SUGGEST -> SearchManager.SUGGEST_MIME_TYPE
            REFRESH_SHORTCUT -> SearchManager.SHORTCUT_MIME_TYPE
            else -> throw IllegalArgumentException("Unknown URL $uri")
        }
    }

    @Nullable
    override fun query(@NonNull uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        var cursor: Cursor? = null
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE, CONTACT_JOIN_DIR_TYPE -> try {
                cursor = databaseHelper?.query(TABLE_MSGCHATCONTACT, projection, selection, selectionArgs, sortOrder)
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
            CONTACT_ITEM_TYPE, CONTACT_DATA_ITEM_TYPE -> try {
                cursor = databaseHelper?.query(TABLE_MSGCHATCONTACT, projection, selection, selectionArgs, sortOrder)
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
            CONTACT_JOIN_ITEM_TYPE -> try {
                cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, ContentUris.parseId(uri))
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
            CONTACT_JOIN_DATA_ITEM_TYPE -> try {
                cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, uri.lastPathSegment)
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
            CONTACT_RAW_DIR_TYPE -> try {
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
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return cursor
    }

    @Nullable
    override fun insert(@NonNull uri: Uri, values: ContentValues?): Uri? {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        val vals1 = values?.getAsByteArray(ContactItem.DEVICE_CONTACT)
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE -> try {
                val deviceContactItems = MainApplicationSingleton.Serializer.deserialize(vals1) as? Set<ContactItem>
                if (null == deviceContactItems) {
                    return null
                }
                val ids = DeviceContactContentProvider.createDeviceContact(databaseHelper, deviceContactItems)
                if (null == ids || ids.size != deviceContactItems.size) {
                    return null
                }
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(uri, null)
                }
                return uri
            } catch (e: SQLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            CONTACT_ITEM_TYPE -> try {
                val item = MainApplicationSingleton.Serializer.deserialize(vals1) as? ContactItem
                val id = DeviceContactContentProvider.createOrUpdateDeviceContact(databaseHelper, item)
                val insertUri = ContentUris.withAppendedId(MsgChatContactContentProvider.CONTENT_URI, id)
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(insertUri, null)
                    //                    context.getContentResolver().notifyChange(uri, null);
                }
                return insertUri
            } catch (e: SQLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return uri
    }

    override fun update(@NonNull uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        var id = 0
        var updateUri: Uri
        val context = context
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE, CONTACT_ITEM_TYPE -> try {
                id = databaseHelper?.update(TABLE_MSGCHATCONTACT, values, selection, selectionArgs) ?: 0
                updateUri = ContentUris.withAppendedId(MsgChatContactContentProvider.CONTENT_URI, id.toLong())
                if (null != context) {
                    context.contentResolver.notifyChange(updateUri, null)
                }
                return id
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            CONTACT_JOIN_DIR_TYPE, CONTACT_JOIN_ITEM_TYPE -> try {
                val vals1 = values?.getAsByteArray(ContactItem.DEVICE_CONTACT)
                val item = MainApplicationSingleton.Serializer.deserialize(vals1) as? ContactItem
                id = DeviceContactContentProvider.updateDeviceContactJoin(databaseHelper, item).toInt()
                updateUri = ContentUris.withAppendedId(MsgChatContactContentProvider.CONTENT_URI, id.toLong())
                if (null != context) {
                    context.contentResolver.notifyChange(updateUri, null)
                }
                return id
            } catch (e: SQLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return 0
    }

    override fun delete(@NonNull uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE, CONTACT_ITEM_TYPE -> try {
                val id = databaseHelper?.delete(TABLE_MSGCHATCONTACT, selection, selectionArgs) ?: 0
                val delUri = ContentUris.withAppendedId(MsgChatContactContentProvider.CONTENT_URI, id.toLong())
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(delUri, null)
                }
                return id
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return 0
    }

    /*
        public static long createOrUpdateContactIntellibitzContactJoin(DatabaseHelper databaseHelper,
                                                                  SQLiteDatabase db, ContactItem intellibitzContactItem, long id) {
    //            ContactItem contactItem = item.getIntellibitzContactItem();
            if (null == intellibitzContactItem) return 0;
            Cursor cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
    //                projection
                    new String[]{IntellibitzContactContentProvider.ContactItemColumns.KEY_ID},
    //                selection
                    ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
    //                selection args
                    new String[]{intellibitzContactItem.getIntellibitzId()},
                    null);
            if (null == cursor || 0 == cursor.getCount()) {
                long _id = IntellibitzContactContentProvider.createOrUpdateIntellibitzContact(
                        databaseHelper, db, intellibitzContactItem);
                return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id);
            } else {
                long _id = cursor.getLong(cursor.getColumnIndex(
                        IntellibitzContactContentProvider.ContactItemColumns.KEY_ID));
                cursor.close();
                return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id);
            }
        }

        public static long createOrUpdateContactIntellibitzContactJoin(DatabaseHelper databaseHelper,
                                                                  SQLiteDatabase db, ContactItem item, long id) {
            Cursor cursor = getContactIntellibitzContactCursorJoin(databaseHelper, db, item, id);
            long _id = 0;
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
                cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
                        new String[]{ContactItemColumns.KEY_ID},
                        ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                        new String[]{item.getIntellibitzId()}, null);
                if (null != cursor && 0 != cursor.getCount()) {
                    _id = cursor.getLong(cursor.getColumnIndex(
                            ContactItemColumns.KEY_ID));
                    cursor.close();
                    item.set_id(_id);
                }
            } else {
                _id = cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
            if (_id != 0) {
    //        creates the join
                createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, id, _id);
            }
            return item.get_id();
        }
    */

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

    /*
    public Cursor getAllGrpContactAKGrpContactsCursor(ContactItem item, long id) {
*/
/*
        SELECT  * FROM msg_threads mt
left join msg_threads_emails mte on mt.[_id] = mte.[msg_thread_id]
left join emails e
where e.name = 'Jeffrey Roshan' and e.email = 'jeff@intellibitz.com' and e.type='to'
and mt._id = '11'
         *//*

        String[] args = new String[]{item.getIntellibitzId(), String.valueOf(id)};
        String selectQuery = "SELECT  * FROM " + ContactsContentProvider.TABLE_MSGCHATCONTACTS + " ct " +
                " left join " + TABLE_MSGCHATCONTACTS_CONTACT_JOIN +
                " cte on ct.[_id] = cte.[" + DatabaseHelper.ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID + "]  " +
                " left join " + ContactContentProvider.TABLE_MSGCHATCONTACT + " em on cte.[" +
                DatabaseHelper.ContactsContactJoinColumns.KEY_CONTACT_ID + "] = em.[_id] " +
                " WHERE " + " em." + ContactsContentProvider.ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? " +
                " AND ct." + ContactsContentProvider.ContactItemColumns.KEY_ID + " = ? ";

//        Log.e(TAG, selectQuery);
        return rawQuery(selectQuery, args);
    }
*/

}
