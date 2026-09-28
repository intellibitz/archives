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
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.io.IOException
import java.util.Set

/**
 *
 */
class MsgEmailContactContentProvider : ContentProvider() {

    companion object {
        const val TAG = "MsgEmailContactCP"
        const val TABLE_MSGEMAILCONTACT = "msgemailcontact"
        const val TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN = "msgemailcontact_intellibitzcontacts"
        const val CREATE_TABLE_MSGEMAILCONTACT = "CREATE TABLE " + TABLE_MSGEMAILCONTACT + ContactItemColumns.TABLE_CONTACTS_SCHEMA
        const val SCHEME = "content://"
        const val CONTACT_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all"
        const val CONTACT_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id"
        const val CONTACT_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id"
        const val CONTACT_JOIN_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/all"
        const val CONTACT_JOIN_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id"
        const val CONTACT_JOIN_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id"
        const val CONTACT_RAW_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all"
        const val CREATE_TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN = "CREATE TABLE " + TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN + "( " + ContactIntellibitzContactJoinColumns.KEY_ID + " INTEGER PRIMARY KEY," + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + " INTEGER," + ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " INTEGER," + ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP + " LONG" + ")"
        private const val CONTACT_DIR_TYPE = 0
        private const val CONTACT_ITEM_TYPE = 1
        private const val CONTACT_DATA_ITEM_TYPE = 2
        private const val CONTACT_JOIN_DIR_TYPE = 3
        private const val CONTACT_JOIN_ITEM_TYPE = 4
        private const val CONTACT_JOIN_DATA_ITEM_TYPE = 5
        private const val CONTACT_RAW_DIR_TYPE = 6
        private const val SEARCH_SUGGEST = 7
        private const val REFRESH_SHORTCUT = 8
        var AUTHORITY = "intellibitz.intellidroid.content.MsgEmailContactContentProvider"
        val CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_MSGEMAILCONTACT)
        val JOIN_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGEMAILCONTACT)
        val RAW_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGEMAILCONTACT)
        private val URI_MATCHER = buildUriMatcher()

        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACT, CONTACT_DIR_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACT + "/#", CONTACT_ITEM_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACT + "/*", CONTACT_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGEMAILCONTACT, CONTACT_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGEMAILCONTACT + "/#", CONTACT_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGEMAILCONTACT + "/*", CONTACT_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "raw_" + TABLE_MSGEMAILCONTACT, CONTACT_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST)
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
            var id = 0L
            val cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_ID), ContactItemColumns.KEY_EMAILS + " LIKE '%' || ? || '%' ", arrayOf(contactItem.intellibitzId), null)
            if (cursor != null && cursor.count > 0) {
                id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
            }
            if (id > 0) {
                contactItem.isDevice = true
            }
            val _id = IntellibitzContactContentProvider.createOrUpdateIntellibitzContact(databaseHelper, db, contactItem)
            if (id > 0) {
                IntellibitzContactContentProvider.createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper, db, _id, id)
            }
            return contactItem._id
        }

        fun createOrUpdateContactIntellibitzContactByContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem, id: Long): Long {
            if (null == contactItem) return 0
            val cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT, arrayOf(ContactItemColumns.KEY_ID, ContactItemColumns.KEY_DEVICE_CONTACTID, ContactItemColumns.KEY_FIRST_NAME, ContactItemColumns.KEY_LAST_NAME, ContactItemColumns.KEY_DISPLAY_NAME, ContactItemColumns.KEY_PIC, ContactItemColumns.KEY_CLOUD_PIC), ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ", arrayOf(contactItem.intellibitzId), null)
            if (null == cursor || 0 == cursor.count) {
                val _id = createOrUpdateIntellibitzContactByContact(databaseHelper, db, contactItem)
                return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id)
            } else {
                val _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                val did = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                val first = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                val last = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                val disp = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                val pic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                val cloudPic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                cursor.close()
                contactItem.deviceContactId = did
                if (!TextUtils.isEmpty(first)) contactItem.firstName = first
                if (!TextUtils.isEmpty(last)) contactItem.lastName = last
                if (!TextUtils.isEmpty(disp)) contactItem.displayName = disp
                if (!TextUtils.isEmpty(disp)) contactItem.name = disp
                if (!TextUtils.isEmpty(pic)) contactItem.profilePic = pic
                if (!TextUtils.isEmpty(cloudPic)) contactItem.cloudPic = cloudPic
                return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id)
            }
        }

        fun getContactIntellibitzContactCursorJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Cursor {
            val args = arrayOf(item.dataId, id.toString())
            val selectQuery = "SELECT  * FROM " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT + " nt " + " left join " + TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN + " ntm on nt.[" + ContactIntellibitzContactJoinColumns.KEY_ID + "] = ntm.[" + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + "]  " + " left join " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT + " mt on ntm.[" + ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + "] = mt.[" + ContactItemColumns.KEY_ID + "] " + " WHERE " + " mt." + ContactItemColumns.KEY_ID + " = ? " + " AND nt." + ContactItemColumns.KEY_ID + " = ? "
            return databaseHelper.rawQuery(db, selectQuery, args)
        }

        fun createOrUpdateContactIntellibitzContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id = getContactIntellibitzContactJoin(databaseHelper, db, id, fk)
            val values = ContentValues()
            values.put(ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID, id)
            values.put(ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID, fk)
            values.put(ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis())
            if (0 == _id) {
                _id = databaseHelper.insert(db, TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN, null, values)
            } else {
                _id = databaseHelper.update(db, TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN, values, ContactIntellibitzContactJoinColumns.KEY_ID + " = ?", arrayOf(_id.toString()))
            }
            return _id
        }

        fun getContactIntellibitzContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id = 0L
            val c = databaseHelper.query(db, TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN, arrayOf(ContactIntellibitzContactJoinColumns.KEY_ID), ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " = ? and " + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + " = ?", arrayOf(id.toString(), fk.toString()), null, null, null)
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

    override fun getType(uri: Uri): String? {
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

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var cursor: Cursor? = null
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE, CONTACT_JOIN_DIR_TYPE -> try {
                cursor = databaseHelper?.query(TABLE_MSGEMAILCONTACT, projection, selection, selectionArgs, sortOrder)
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
                cursor = databaseHelper?.query(TABLE_MSGEMAILCONTACT, projection, selection, selectionArgs, sortOrder)
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

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val vals1 = values?.getAsByteArray(ContactItem.DEVICE_CONTACT)
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE -> try {
                val deviceContactItems = MainApplicationSingleton.Serializer.deserialize(vals1) as Set<ContactItem>
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
                val item = MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem
                val id = DeviceContactContentProvider.createOrUpdateDeviceContact(databaseHelper, item)
                val insertUri = ContentUris.withAppendedId(MsgEmailContactContentProvider.CONTENT_URI, id)
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(insertUri, null)
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

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        var id = 0
        val updateUri: Uri
        val context = context
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE, CONTACT_ITEM_TYPE -> try {
                id = databaseHelper?.update(TABLE_MSGEMAILCONTACT, values, selection, selectionArgs) ?: 0
                updateUri = ContentUris.withAppendedId(MsgEmailContactContentProvider.CONTENT_URI, id.toLong())
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
                val item = MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem
                id = DeviceContactContentProvider.updateDeviceContactJoin(databaseHelper, item).toInt()
                updateUri = ContentUris.withAppendedId(MsgEmailContactContentProvider.CONTENT_URI, id.toLong())
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

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        when (URI_MATCHER.match(uri)) {
            CONTACT_DIR_TYPE, CONTACT_ITEM_TYPE -> try {
                val id = databaseHelper?.delete(TABLE_MSGEMAILCONTACT, selection, selectionArgs) ?: 0
                val delUri = ContentUris.withAppendedId(MsgEmailContactContentProvider.CONTENT_URI, id.toLong())
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

    fun getOpenHelperForTest(): DatabaseHelper? {
        return databaseHelper
    }
}
