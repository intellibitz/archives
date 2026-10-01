

package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.telephony.TelephonyManager
import android.util.Log
import android.util.SparseArray
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactIntellibitzContactJoinColumns
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.io.IOException
import java.util.HashMap
import java.util.HashSet
import java.util.Locale
import java.util.Set
import intellibitz.intellidroid.content.DeviceContactContentProvider.*

class IntellibitzContactContentProvider : ContentProvider() {
    companion object {
        const val TAG = "IntellibitzContactProvider"
        const val TABLE_INTELLIBITZCONTACT = "intellibitzcontact"
        const val TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN = "intellibitzcontact_devicecontact"
        const val CREATE_TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN = ((((((((((("CREATE TABLE " + TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN) + "( ") + ContactIntellibitzContactJoinColumns.KEY_ID) + " INTEGER PRIMARY KEY,") + ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID) + " INTEGER,") + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID) + " INTEGER,") + ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP) + " LONG") + ")")
        const val CREATE_TABLE_INTELLIBITZCONTACTS = (("CREATE TABLE " + TABLE_INTELLIBITZCONTACT) + ContactItemColumns.TABLE_CONTACTS_SCHEMA)
        const val SCHEME = "content://"
        const val INTELLIBITZCONTACTS_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all")
        const val INTELLIBITZCONTACTS_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id")
        const val INTELLIBITZCONTACTS_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id")
        const val INTELLIBITZCONTACTS_JOIN_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/all")
        const val INTELLIBITZCONTACTS_JOIN_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id")
        const val INTELLIBITZCONTACTS_JOIN_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id")
        const val INTELLIBITZCONTACTS_RAW_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all")
        private const val INTELLIBITZCONTACTS_DIR_TYPE = 0
        private const val INTELLIBITZCONTACTS_ITEM_TYPE = 1
        private const val INTELLIBITZCONTACTS_DATA_ITEM_TYPE = 2
        private const val INTELLIBITZCONTACTS_JOIN_DIR_TYPE = 3
        private const val INTELLIBITZCONTACTS_JOIN_ITEM_TYPE = 4
        private const val INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE = 5
        private const val INTELLIBITZCONTACTS_RAW_DIR_TYPE = 6
        private const val SEARCH_SUGGEST = 7
        private const val REFRESH_SHORTCUT = 8
        var AUTHORITY: String = "intellibitz.intellidroid.content.IntellibitzContactContentProvider"
        val CONTENT_URI: Uri = Uri.parse((((SCHEME + AUTHORITY) + "/") + TABLE_INTELLIBITZCONTACT))
        val JOIN_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "join_") + TABLE_INTELLIBITZCONTACT))
        val RAW_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "raw_") + TABLE_INTELLIBITZCONTACT))
        private val URI_MATCHER: UriMatcher = buildUriMatcher()
        fun createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = getIntellibitzContactDeviceContactJoin(databaseHelper, db, id, fk)
            var values: ContentValues = ContentValues()
            values.put(ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID, id)
            values.put(ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID, fk)
            values.put(ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.dateTimeMillis)
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN, null, values)
            }
            else {
                _id = databaseHelper.update(db, TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN, values, (ContactIntellibitzContactJoinColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return _id
        }
        fun getIntellibitzContactDeviceContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = 0
            var c: Cursor = databaseHelper.query(db, TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN, arrayOf(ContactIntellibitzContactJoinColumns.KEY_ID), (((ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " = ? and ") + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID) + " = ?"), arrayOf(String.valueOf(id), String.valueOf(fk)), null, null, null)
            if (((c != null) && (c.getCount() > 0))) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }
        private fun buildUriMatcher(): UriMatcher {
            var matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_INTELLIBITZCONTACT, INTELLIBITZCONTACTS_DIR_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_INTELLIBITZCONTACT + "/#"), INTELLIBITZCONTACTS_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_INTELLIBITZCONTACT + "/*"), INTELLIBITZCONTACTS_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("join_" + TABLE_INTELLIBITZCONTACT), INTELLIBITZCONTACTS_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_INTELLIBITZCONTACT) + "/#"), INTELLIBITZCONTACTS_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_INTELLIBITZCONTACT) + "/*"), INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("raw_" + TABLE_INTELLIBITZCONTACT), INTELLIBITZCONTACTS_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_QUERY + "/*"), SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*"), REFRESH_SHORTCUT)
            return matcher
        }
        fun updateContactItemToDB(deviceContactItem: ContactItem, context: Context): Uri {
            try {
                var contentValues: ContentValues = ContentValues()
                contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(deviceContactItem))
                var row: Int = context.getContentResolver()
                return ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, row)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        fun saveContactItemToDB(deviceContactItem: ContactItem, context: Context): Uri {
            try {
                var contentValues: ContentValues = ContentValues()
                contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(deviceContactItem))
                return context.getContentResolver()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        fun saveContactsInDB(contacts: HashSet<ContactItem>, context: Context): Uri {
            try {
                var contentValues: ContentValues = ContentValues()
                contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(contacts))
                return context.getContentResolver()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        fun parseFormatPhoneNumberByISO(number: String, iso: String): String {
            var formattedPhoneNumber: String = null
            var phoneNumber: Phonenumber.PhoneNumber
            var phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            try {
                phoneNumber = phoneNumberUtil.parse(number, iso)
                if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                    formattedPhoneNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
                }
            }
            catch (npe: NumberParseException) {
                formattedPhoneNumber = null
            }
            return formattedPhoneNumber
        }
        fun getSimCountryIso(context: Context): String {
            var telephonyManager: TelephonyManager = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
            var country: String = telephonyManager.networkCountryIso
            if ((null == country)) {
                country = telephonyManager.simCountryIso
            }
            if ((null == country)) {
                country = Locale.default
            }
            if ((null == country)) {
                country = "IN"
            }
            return country.toUpperCase()
        }
        fun fillIntellibitzContactFromCursor(contactItems: HashMap<Long, ContactItem>, cursor: Cursor): HashMap<Long, ContactItem> {
            var contactItem: ContactItem = ContactItem()
            do {
                var ctid: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                if ((ctid == contactItem._id)) {

                }
                else {
                    contactItem = ContactItem()
                    contactItem._id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                    contactItem.deviceContactId = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                    contactItem.dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID))
                    contactItem.dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TYPE_ID))
                    contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_INTELLIBITZ_ID))
                    contactItem.type = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TYPE))
                    contactItem.name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
                    contactItem.firstName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                    contactItem.lastName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                    contactItem.displayName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                    contactItem.status = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_STATUS))
                    contactItem.profilePic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                    contactItem.cloudPic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                    contactItem.emailItem = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_IS_EMAIL))
                    contactItem.timestamp = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_TIMESTAMP))
                    contactItems.put(contactItem._id, contactItem)
                }
            } while (cursor.moveToNext())
            return contactItems
        }
        fun fillContentValuesFromIntellibitzContactItem(item: ContactItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_ID, item.dataId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_CONTACTID, item.deviceContactId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE_ID, item.typeId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_INTELLIBITZ_ID, item.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_NAME, item.name)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_FIRST_NAME, item.firstName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_LAST_NAME, item.lastName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DISPLAY_NAME, item.displayName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE, item.getType())
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_STATUS, item.status)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PIC, item.profilePic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_CLOUD_PIC, item.cloudPic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_GROUP, item.group)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_EMAIL, item.emailItem)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_ANONYMOUS, item.anonymous)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_DEVICE, item.device)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_CLOUD, item.cloud)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_ISWORK, item.isWorkContact)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TIMESTAMP, item.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis(item.timestamp))
            return values
        }
        fun fillIntellibitzContactItemFromAllJoinCursor(cursor: Cursor): SparseArray<ContactItem> {
            var contactItems: SparseArray<ContactItem> = SparseArray()
            var contactItem: ContactItem = ContactItem()
            do {
                var ctid: Long = cursor.getLong(cursor.getColumnIndex("ct_id"))
                if ((ctid == contactItem._id)) {

                }
                else {
                    contactItem = ContactItem()
                    contactItem._id = cursor.getLong(cursor.getColumnIndex("ct_id"))
                    contactItem.dataId = cursor.getString(cursor.getColumnIndex("ctid"))
                    contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex("ctcid"))
                    contactItem.deviceRef = cursor.getString(cursor.getColumnIndex("ctdr"))
                    contactItem.name = cursor.getString(cursor.getColumnIndex("ctname"))
                    contactItem.status = cursor.getString(cursor.getColumnIndex("ctst"))
                    contactItem.profilePic = cursor.getString(cursor.getColumnIndex("ctppic"))
                    contactItems.put((contactItem._id as Int), contactItem)
                }
            } while (cursor.moveToNext())
            return contactItems
        }
        fun fillIntellibitzContactItemFromCursor(cursor: Cursor): SparseArray<ContactItem> {
            var contactItems: SparseArray<ContactItem> = SparseArray()
            do {
                var contactItem: ContactItem = ContactItem()
                contactItem._id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                contactItem.deviceContactId = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                contactItem.dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID))
                contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_INTELLIBITZ_ID))
                contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TYPE_ID))
                contactItem.deviceRef = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_REF))
                contactItem.name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
                contactItem.firstName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                contactItem.lastName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                contactItem.displayName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                contactItem.status = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_STATUS))
                contactItem.profilePic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                contactItem.cloudPic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                contactItems.put((contactItem._id as Int), contactItem)
            } while (cursor.moveToNext())
            return contactItems
        }
        fun createOrUpdateIntellibitzContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, intellibitzContactItem: ContactItem): Long {
            val typeId: String = intellibitzContactItem.typeId
            var cursor: Cursor = null
            if ((typeId != null)) {
                cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_DEVICE_CONTACTID, ContactItemColumns.KEY_FIRST_NAME, ContactItemColumns.KEY_LAST_NAME, ContactItemColumns.KEY_DISPLAY_NAME, ContactItemColumns.KEY_PIC, ContactItemColumns.KEY_CLOUD_PIC), ((((((((("TRIM(" + ContactItemColumns.KEY_EMAILS) + ")") + " LIKE '%' || ? || '%'  OR ") + "TRIM(") + ContactItemColumns.KEY_PHONES) + ")") + " LIKE '%' || ? || '%'  OR ") + ContactItemColumns.KEY_INTELLIBITZ_ID) + " LIKE '%' || ? || '%'  "), arrayOf(typeId, typeId, typeId), null)
            }
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_DEVICE_CONTACTID, ContactItemColumns.KEY_FIRST_NAME, ContactItemColumns.KEY_LAST_NAME, ContactItemColumns.KEY_DISPLAY_NAME, ContactItemColumns.KEY_PIC, ContactItemColumns.KEY_CLOUD_PIC), (ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? "), arrayOf(intellibitzContactItem.intellibitzId), null)
            }
            if (((cursor != null) && (cursor.getCount() > 0))) {
                var dcid: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                var first: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                var last: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                var display: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                var pic: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                var cpic: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                cursor.close()
                intellibitzContactItem.deviceContactId = dcid
                intellibitzContactItem.firstName = first
                intellibitzContactItem.lastName = last
                intellibitzContactItem.displayName = display
                intellibitzContactItem.profilePic = pic
                intellibitzContactItem.cloudPic = cpic
            }
            if ((cursor != null)) {
                cursor.close()
            }
            var values: ContentValues = ContentValues()
            IntellibitzContactContentProvider.fillContentValuesFromIntellibitzContactItem(intellibitzContactItem, values)
            cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? "), arrayOf(intellibitzContactItem.intellibitzId), null)
            var _id: Long
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                _id = databaseHelper.insert(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT, null, values)
                intellibitzContactItem._id = _id
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                intellibitzContactItem._id = _id
                databaseHelper.update(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT, values, (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return intellibitzContactItem._id
        }
    }
    private var databaseHelper: DatabaseHelper? = null
    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(getContext(), DatabaseHelper.DATABASE_NAME)
        return true
    }
    override fun getType(uri: Uri): String {
        when (URI_MATCHER.match(uri)) {
            INTELLIBITZCONTACTS_DIR_TYPE -> {
                return INTELLIBITZCONTACTS_DIR_MIME_TYPE
            }
            INTELLIBITZCONTACTS_ITEM_TYPE -> {
                return INTELLIBITZCONTACTS_ITEM_MIME_TYPE
            }
            INTELLIBITZCONTACTS_DATA_ITEM_TYPE -> {
                return INTELLIBITZCONTACTS_DATA_ITEM_MIME_TYPE
            }
            INTELLIBITZCONTACTS_JOIN_DIR_TYPE -> {
                return INTELLIBITZCONTACTS_JOIN_DIR_MIME_TYPE
            }
            INTELLIBITZCONTACTS_JOIN_ITEM_TYPE -> {
                return INTELLIBITZCONTACTS_JOIN_ITEM_MIME_TYPE
            }
            INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE -> {
                return INTELLIBITZCONTACTS_JOIN_DATA_ITEM_MIME_TYPE
            }
            INTELLIBITZCONTACTS_RAW_DIR_TYPE -> {
                return INTELLIBITZCONTACTS_RAW_DIR_MIME_TYPE
            }
            SEARCH_SUGGEST -> {
                return SearchManager.SUGGEST_MIME_TYPE
            }
            REFRESH_SHORTCUT -> {
                return SearchManager.SHORTCUT_MIME_TYPE
            }
            else -> {
                throw IllegalArgumentException(("Unknown URL " + uri))
            }
        }
    }
    override fun query(uri: Uri, projection: Array<String>, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
        var cursor: Cursor = null
        when (URI_MATCHER.match(uri)) {
            INTELLIBITZCONTACTS_DIR_TYPE, INTELLIBITZCONTACTS_ITEM_TYPE, INTELLIBITZCONTACTS_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_INTELLIBITZCONTACT, projection, selection, selectionArgs, sortOrder)
                    if ((null != cursor)) {
                        var context: Context = getContext()
                        if ((null != context)) {
                            cursor.setNotificationUri(context.getContentResolver(), uri)
                        }
                    }
                    return cursor
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            INTELLIBITZCONTACTS_JOIN_DIR_TYPE, INTELLIBITZCONTACTS_JOIN_ITEM_TYPE, INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_INTELLIBITZCONTACT, projection, selection, selectionArgs, sortOrder)
                    if ((null != cursor)) {
                        var context: Context = getContext()
                        if ((null != context)) {
                            cursor.setNotificationUri(context.getContentResolver(), uri)
                        }
                    }
                    return cursor
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            INTELLIBITZCONTACTS_RAW_DIR_TYPE -> {
                try {
                    cursor = databaseHelper.getRawCursor(selection, selectionArgs)
                    if ((null != cursor)) {
                        var context: Context = getContext()
                        if ((null != context)) {
                            cursor.setNotificationUri(context.getContentResolver(), uri)
                        }
                    }
                    return cursor
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            else -> {
                throw IllegalArgumentException(("Unknown Uri: " + uri))
            }
        }
        return cursor
    }
    override fun insert(uri: Uri, values: ContentValues): Uri {
        var vals1: Array<Byte> = values.getAsByteArray(ContactItem.DEVICE_CONTACT)
        when (URI_MATCHER.match(uri)) {
            INTELLIBITZCONTACTS_DIR_TYPE -> {
                try {
                    var deviceContactItems: Set<ContactItem> = (MainApplicationSingleton.Serializer.deserialize(vals1) as Set<ContactItem>)
                    if ((null == deviceContactItems)) {
                        return null
                    }
                    var ids: Array<Long> = DeviceContactContentProvider.createDeviceContact(databaseHelper, deviceContactItems)
                    if (((null == ids) || (ids.size != deviceContactItems.size()))) {
                        return null
                    }
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return uri
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                }
                break
            }
            INTELLIBITZCONTACTS_ITEM_TYPE -> {
                try {
                    var item: ContactItem = (MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem)
                    var id: Long = DeviceContactContentProvider.createOrUpdateDeviceContact(databaseHelper, item)
                    var insertUri: Uri = ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, id)
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return insertUri
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                }
                break
            }
            else -> {
                throw IllegalArgumentException(("Unknown Uri: " + uri))
            }
        }
        return uri
    }
    override fun update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array<String>): Int {
        var id: Int = 0
        var updateUri: Uri
        var context: Context = getContext()
        when (URI_MATCHER.match(uri)) {
            INTELLIBITZCONTACTS_DIR_TYPE, INTELLIBITZCONTACTS_ITEM_TYPE -> {
                try {
                    id = databaseHelper.update(DeviceContactContentProvider.TABLE_DEVICECONTACTS, values, selection, selectionArgs)
                    updateUri = ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, id)
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return id
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            INTELLIBITZCONTACTS_JOIN_DIR_TYPE, INTELLIBITZCONTACTS_JOIN_ITEM_TYPE -> {
                try {
                    var vals1: Array<Byte> = values.getAsByteArray(ContactItem.DEVICE_CONTACT)
                    var item: ContactItem = (MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem)
                    id = (DeviceContactContentProvider.updateDeviceContactJoin(databaseHelper, item) as Int)
                    updateUri = ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, id)
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return id
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                }
                break
            }
            else -> {
                throw IllegalArgumentException(("Unknown Uri: " + uri))
            }
        }
        return 0
    }
    override fun delete(uri: Uri, selection: String, selectionArgs: Array<String>): Int {
        when (URI_MATCHER.match(uri)) {
            INTELLIBITZCONTACTS_DIR_TYPE, INTELLIBITZCONTACTS_ITEM_TYPE -> {
                try {
                    var id: Int = databaseHelper.delete(DeviceContactContentProvider.TABLE_DEVICECONTACTS, selection, selectionArgs)
                    var insertUri: Uri = ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, id)
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return id
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            else -> {
                throw IllegalArgumentException(("Unknown Uri: " + uri))
            }
        }
        return 0
    }
    fun getOpenHelperForTest(): DatabaseHelper {
        return databaseHelper
    }
}
