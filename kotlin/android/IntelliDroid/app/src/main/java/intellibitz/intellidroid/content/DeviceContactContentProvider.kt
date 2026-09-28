

package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.*
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.i18n.phonenumbers.PhoneNumberUtil
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*

class DeviceContactContentProvider : ContentProvider() {
    companion object {
        const val TAG = "DeviceContactCP"
        const val TABLE_DEVICECONTACTS = "devicecontact"
        const val CREATE_TABLE_DEVICECONTACTS = (("CREATE TABLE " + TABLE_DEVICECONTACTS) + ContactItemColumns.TABLE_CONTACTS_SCHEMA)
        const val SCHEME = "content://"
        const val CONTACTS_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all")
        const val CONTACTS_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id")
        const val CONTACTS_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id")
        const val CONTACTS_JOIN_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/all")
        const val CONTACTS_JOIN_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id")
        const val CONTACTS_JOIN_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id")
        const val CONTACTS_RAW_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all")
        private const val CONTACTS_DIR_TYPE = 0
        private const val CONTACTS_ITEM_TYPE = 1
        private const val CONTACTS_DATA_ITEM_TYPE = 2
        private const val CONTACTS_JOIN_DIR_TYPE = 3
        private const val CONTACTS_JOIN_ITEM_TYPE = 4
        private const val CONTACTS_JOIN_DATA_ITEM_TYPE = 5
        private const val CONTACTS_RAW_DIR_TYPE = 6
        private const val SEARCH_SUGGEST = 7
        private const val REFRESH_SHORTCUT = 8
        var AUTHORITY: String = "intellibitz.intellidroid.content.DeviceContactContentProvider"
        val CONTENT_URI: Uri = Uri.parse((((SCHEME + AUTHORITY) + "/") + TABLE_DEVICECONTACTS))
        val JOIN_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "join_") + TABLE_DEVICECONTACTS))
        val RAW_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "raw_") + TABLE_DEVICECONTACTS))
        private val URI_MATCHER: UriMatcher = buildUriMatcher()
        private fun buildUriMatcher(): UriMatcher {
            var matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_DEVICECONTACTS, CONTACTS_DIR_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_DEVICECONTACTS + "/#"), CONTACTS_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_DEVICECONTACTS + "/*"), CONTACTS_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("join_" + TABLE_DEVICECONTACTS), CONTACTS_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_DEVICECONTACTS) + "/#"), CONTACTS_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_DEVICECONTACTS) + "/*"), CONTACTS_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("raw_" + TABLE_DEVICECONTACTS), CONTACTS_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_QUERY + "/*"), SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*"), REFRESH_SHORTCUT)
            return matcher
        }
        fun updatesDeviceContactJoin(deviceContactItem: ContactItem, context: Context): Uri {
            try {
                var contentValues: ContentValues = ContentValues()
                contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(deviceContactItem))
                var row: Int = context.getContentResolver()
                return ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, row)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        fun savesOrUpdatesDeviceContact(deviceContactItem: ContactItem, context: Context): Uri {
            var contentValues: ContentValues = ContentValues()
            try {
                contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(deviceContactItem))
                return context.getContentResolver()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        fun savesOrUpdatesDeviceContacts(contacts: Collection<ContactItem>, context: Context): Uri {
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
        fun savesOrUpdatesWorkContacts(contacts: Collection<ContactItem>, context: Context): Uri {
            try {
                var contentValues: ContentValues = ContentValues()
                contentValues.put(ContactItem.WORK_CONTACT, MainApplicationSingleton.Serializer.serialize(contacts))
                return context.getContentResolver()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        @Throws(JSONException::class)
        fun createsDeviceContactItemFromJSON(jsonObject: JSONObject, deviceRef: String): ContactItem {
            var deviceContactItem: ContactItem = ContactItem()
            var cid: Long = jsonObject.optLong("device_contact_id")
            deviceContactItem.deviceContactId = cid
            var deviceRef_: String = jsonObject.optString("device_ref")
            if ((null == deviceRef_)) {
                deviceContactItem.deviceRef = deviceRef
            }
            else {
                deviceContactItem.deviceRef = deviceRef_
            }
            if ((deviceRef == deviceContactItem.deviceRef)) {
                var c_id: Long = jsonObject.optLong("c_id")
                if ((c_id > 0)) {
                    deviceContactItem._id = c_id
                }
                var id: String = jsonObject.optString("_id")
                if (!TextUtils.isEmpty(id)) {
                    deviceContactItem.dataId = id
                }
                var rev: String = jsonObject.optString("_rev")
                if (!TextUtils.isEmpty(rev)) {
                    deviceContactItem.dataRev = rev
                }
                var version: Int = jsonObject.optInt("version")
                if ((version > 0)) {
                    deviceContactItem.version = version
                }
                var firstName: String = jsonObject.optString("first_name")
                if (!TextUtils.isEmpty(firstName)) {
                    deviceContactItem.firstName = firstName
                }
                var lastName: String = jsonObject.optString("last_name")
                if (!TextUtils.isEmpty(lastName)) {
                    deviceContactItem.lastName = lastName
                }
                var dispName: String = jsonObject.optString("display_name")
                if (!TextUtils.isEmpty(dispName)) {
                    deviceContactItem.displayName = dispName
                }
                var docType: String = jsonObject.optString("doc_type")
                if (!TextUtils.isEmpty(docType)) {
                    deviceContactItem.docType = docType
                }
                var docOwner: String = jsonObject.optString("doc_owner")
                if (!TextUtils.isEmpty(docOwner)) {
                    deviceContactItem.docOwner = docOwner
                }
                var timestamp: Long = jsonObject.optLong("timestamp")
                if ((timestamp > 0)) {
                    deviceContactItem.timestamp = timestamp
                }
                var mobiles: JSONArray = jsonObject.getJSONArray("mobiles")
                if (((null != mobiles) && (0 != mobiles.length()))) {
                    var m: Int = mobiles.length()
                    var j: Int = 0
                    while ((j < m)) {
                        var mobilesJSONObject: JSONObject = mobiles.getJSONObject(j)
                        var number: String = mobilesJSONObject.getString("number")
                        if (((number != null) && (number.length() > 0))) {
                            var aktId: String = mobilesJSONObject.optString("akt_id")
                            if (((aktId != null) && (aktId.length() > 0))) {
                                var intellibitzContactItem: ContactItem = ContactItem(number)
                                intellibitzContactItem.intellibitzId = aktId
                                intellibitzContactItem.typeId = number
                                intellibitzContactItem.dataId = aktId
                                intellibitzContactItem.device = true
                                intellibitzContactItem.cloud = true
                                var akt_name: String = mobilesJSONObject.optString("akt_name")
                                intellibitzContactItem.name = akt_name
                                intellibitzContactItem.firstName = deviceContactItem.firstName
                                intellibitzContactItem.lastName = deviceContactItem.lastName
                                intellibitzContactItem.displayName = deviceContactItem.displayName
                                var akt_status: String = "akt_status"
                                var aktStatus: String = mobilesJSONObject.optString(akt_status)
                                if (!TextUtils.isEmpty(aktStatus)) {
                                    intellibitzContactItem.status = aktStatus
                                }
                                var akt_profile_pic: String = mobilesJSONObject.optString("akt_profile_pic")
                                intellibitzContactItem.profilePic = akt_profile_pic
                                intellibitzContactItem.cloudPic = akt_profile_pic
                                deviceContactItem.isIntellibitzContact = 1
                                deviceContactItem.intellibitzId = intellibitzContactItem.intellibitzId
                                if ((null == deviceContactItem.profilePic)) {
                                    deviceContactItem.profilePic = intellibitzContactItem.profilePic
                                }
                                deviceContactItem.cloudPic = akt_profile_pic
                                if ((null == deviceContactItem.status)) {
                                    deviceContactItem.status = intellibitzContactItem.status
                                }
                                deviceContactItem.intellibitzContacts
                            }
                        }
                        j++
                    }
                }
            }
            return deviceContactItem
        }
        @Throws(JSONException::class)
        fun createsWorkContactItemFromJSON(jsonObject: JSONObject): ContactItem {
            if ((null == jsonObject)) {
                return null
            }
            var uid: String = jsonObject.optString("uid")
            if (TextUtils.isEmpty(uid)) {
                return null
            }
            var email: String = jsonObject.optString("email")
            var name: String = jsonObject.optString("name")
            var status: String = jsonObject.optString("status")
            var pic: String = jsonObject.optString("profile_pic")
            return createWorkContactItem(uid, email, name, status, pic)
        }
        fun createWorkContactItem(uid: String, email: String, name: String, status: String, pic: String): ContactItem {
            var workContactItem: ContactItem = ContactItem(uid, name, email)
            workContactItem.intellibitzId = uid
            workContactItem.typeId = email
            workContactItem.isWorkContact = 1
            workContactItem.cloud = true
            workContactItem.status = status
            workContactItem.cloudPic = pic
            return workContactItem
        }
        fun fillWorkContactItemFromJSONArray(contactsJSONArray: JSONArray): HashSet<ContactItem> {
            var results: HashSet<ContactItem> = HashSet()
            var len: Int = contactsJSONArray.length()
            var i: Int = 0
            while ((i < len)) {
                try {
                    var jsonObject: JSONObject = contactsJSONArray.getJSONObject(i)
                    var workContact: ContactItem = createsWorkContactItemFromJSON(jsonObject)
                    if ((workContact != null)) {
                        results.add(workContact)
                    }
                }
                catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, ("Exception: " + e.getMessage()))
                }
                i++
            }
            Log.d(TAG, ("JSON Contacts Array :" + len))
            Log.d(TAG, ("Contacts ToSave Array :" + results.size()))
            return results
        }
        fun fillDeviceContactItemFromJSONArray(contactsJSONArray: JSONArray, deviceRef: String): HashMap<Long, ContactItem> {
            var results: HashMap<Long, ContactItem> = HashMap()
            var len: Int = contactsJSONArray.length()
            var i: Int = 0
            while ((i < len)) {
                try {
                    var jsonObject: JSONObject = contactsJSONArray.getJSONObject(i)
                    var deviceContactItem: ContactItem = createsDeviceContactItemFromJSON(jsonObject, deviceRef)
                    var intellibitzContacts: HashSet<ContactItem> = deviceContactItem.intellibitzContacts
                    if (((intellibitzContacts != null) && !intellibitzContacts.empty)) {
                        results.put(deviceContactItem.deviceContactId, deviceContactItem)
                    }
                }
                catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, ("Exception: " + e.getMessage()))
                }
                i++
            }
            Log.d(TAG, ("JSON Contacts Array :" + len))
            Log.d(TAG, ("Contacts ToSave Array :" + results.size()))
            return results
        }
        @Synchronized
        fun fillJsonArrayFromDeviceContactItem(deviceContactItems: Collection<ContactItem>, context: Context): JSONArray {
            var deviceContacts: JSONArray = JSONArray()
            var array: Array<ContactItem> = deviceContactItems.toArray(arrayOfNulls<ContactItem>(0))
            for (deviceContactItem in array) {
                try {
                    var firstName: String = deviceContactItem.firstName
                    var jsonObject: JSONObject = JSONObject()
                    jsonObject.put("version", deviceContactItem.version)
                    jsonObject.put("first_name", firstName)
                    jsonObject.put("last_name", deviceContactItem.lastName)
                    jsonObject.put("display_name", deviceContactItem.displayName)
                    jsonObject.put("device_ref", deviceContactItem.deviceRef)
                    jsonObject.put("c_id", deviceContactItem._id)
                    jsonObject.put("device_contact_id", deviceContactItem.deviceContactId)
                    var mobiles: JSONArray = JSONArray()
                    var mobileItems: JSONArray = deviceContactItem.mobiles
                    var i: Int = 0
                    while ((i < mobileItems.length())) {
                        var mobile: JSONObject = JSONObject()
                        var number: String = mobileItems.getString(i)
                        var number2: String = MainApplicationSingleton.parseFormatPhoneNumberByISO(number, MainApplicationSingleton.getSimCountryIso(context))
                        if ((number2 != null)) {
                            number = number2
                        }
                        number = PhoneNumberUtil.normalizeDigitsOnly(number)
                        mobile.put("number", number)
                        mobile.put("mobile_id", deviceContactItem._id)
                        mobiles.put(mobile)
                        i++
                    }
                    jsonObject.put("mobiles", mobiles)
                    jsonObject.put("emails", deviceContactItem.emails)
                    deviceContacts.put(jsonObject)
                }
                catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return deviceContacts
        }
        fun fillDeviceContactItemFromCursor(cursor: Cursor): SparseArray<ContactItem> {
            var contactItems: SparseArray<ContactItem> = SparseArray()
            do {
                var deviceContactItem: ContactItem = ContactItem()
                var ctid: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                deviceContactItem.deviceContactId = ctid
                deviceContactItem._id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                deviceContactItem.dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID))
                deviceContactItem.intellibitzId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_INTELLIBITZ_ID))
                deviceContactItem.version = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_VERSION))
                deviceContactItem.deviceRef = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_REF))
                deviceContactItem.name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
                deviceContactItem.firstName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                deviceContactItem.lastName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                deviceContactItem.displayName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                deviceContactItem.companyName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_COMPANY_NAME))
                deviceContactItem.status = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_STATUS))
                deviceContactItem.emails = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_EMAILS))
                deviceContactItem.mobiles = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PHONES))
                deviceContactItem.profilePic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                deviceContactItem.cloudPic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                contactItems.put((deviceContactItem.deviceContactId as Int), deviceContactItem)
            } while (cursor.moveToNext())
            return contactItems
        }
        fun fillsDeviceContactItemFromCursor(cursor: Cursor): SparseArray<ContactItem> {
            var contactItems: SparseArray<ContactItem> = SparseArray()
            do {
                var deviceContactItem: ContactItem = ContactItem()
                var ctid: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                deviceContactItem.deviceContactId = ctid
                deviceContactItem._id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                deviceContactItem.dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID))
                deviceContactItem.intellibitzId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_INTELLIBITZ_ID))
                deviceContactItem.version = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_VERSION))
                deviceContactItem.deviceRef = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_REF))
                deviceContactItem.name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
                deviceContactItem.firstName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                deviceContactItem.lastName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                deviceContactItem.displayName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                deviceContactItem.companyName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_COMPANY_NAME))
                deviceContactItem.status = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_STATUS))
                deviceContactItem.emails = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_EMAILS))
                deviceContactItem.mobiles = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PHONES))
                deviceContactItem.profilePic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                deviceContactItem.cloudPic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                contactItems.put((deviceContactItem._id as Int), deviceContactItem)
            } while (cursor.moveToNext())
            return contactItems
        }
        @Throws(IOException::class)
        fun openPhoto(contactId: Long, context: Context): Bitmap {
            var contactUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            var photoUri: Uri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
            return MainApplicationSingleton.getBitmapDecodePhotoUri(photoUri, context)
        }
        @Throws(IOException::class)
        fun openDisplayPhoto(contactId: Long, context: Context): Bitmap {
            var contactUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            var displayPhotoUri: Uri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
            var fd: AssetFileDescriptor = context.getApplicationContext()
            if ((fd != null)) {
                var inputStream: InputStream = fd.createInputStream()
                if ((null == inputStream)) {
                    return null
                }
                var bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                return bitmap
            }
            return null
        }
        fun isContactsEmptyInDB(context: Context): Boolean {
            return DatabaseHelper.isRowCountEmpty(RAW_CONTENT_URI, TABLE_DEVICECONTACTS, null, context)
        }
        fun fillContentValuesFromDeviceContactItemDataId(deviceContactItem: ContactItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_ID, deviceContactItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_REV, deviceContactItem.dataRev)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_INTELLIBITZ_ID, deviceContactItem.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_INTELLIBITZ, deviceContactItem.isIntellibitzContact)
            return values
        }
        fun fillContentValuesFromDeviceContactItem(deviceContactItem: ContactItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_ID, deviceContactItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_CONTACTID, deviceContactItem.deviceContactId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_INTELLIBITZ_ID, deviceContactItem.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_REV, deviceContactItem.dataRev)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_VERSION, deviceContactItem.version)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_REF, deviceContactItem.deviceRef)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DOC_TYPE, deviceContactItem.docType)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DOC_OWNER, deviceContactItem.docOwner)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_STATUS, deviceContactItem.status)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_NAME, deviceContactItem.name)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE, deviceContactItem.getType())
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_FIRST_NAME, deviceContactItem.firstName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_LAST_NAME, deviceContactItem.lastName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DISPLAY_NAME, deviceContactItem.displayName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_COMPANY_NAME, deviceContactItem.companyName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PIC, deviceContactItem.profilePic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_CLOUD_PIC, deviceContactItem.cloudPic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PHONES, deviceContactItem.mobiles)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_EMAILS, deviceContactItem.emails)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_INTELLIBITZ, deviceContactItem.isIntellibitzContact)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_ISWORK, deviceContactItem.isWorkContact)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TIMESTAMP, deviceContactItem.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis(deviceContactItem.timestamp))
            return values
        }
        fun createOrUpdateDeviceContact(databaseHelper: DatabaseHelper, item: ContactItem): Long {
            var deviceContactItems: Collection<ContactItem> = ArrayList(1)
            deviceContactItems.add(item)
            var ids: Array<Long> = createOrUpdateDeviceContacts(databaseHelper, deviceContactItems)
            if ((null == ids)) {
                return 0
            }
            return ids[0]
        }
        fun createOrUpdateDeviceContacts(databaseHelper: DatabaseHelper, items: Collection<ContactItem>): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                var array: Array<ContactItem> = items.toArray(arrayOfNulls<ContactItem>(0))
                for (item in array) {
                    var l: Long = createOrUpdateDeviceContact(databaseHelper, db, item)
                    if ((0 == l)) {
                        Log.e(TAG, ("Failed to insert row: " + item))
                        throw SQLException(("Failed to insert row into " + item))
                    }
                    ids[i++] = l
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
            return ids
        }
        fun createOrUpdateWorkContacts(databaseHelper: DatabaseHelper, items: Collection<ContactItem>): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                var array: Array<ContactItem> = items.toArray(arrayOfNulls<ContactItem>(0))
                for (item in array) {
                    var l: Long = createOrUpdateWorkContact(databaseHelper, db, item)
                    if ((0 == l)) {
                        Log.e(TAG, ("Failed to insert row: " + item))
                        throw SQLException(("Failed to insert row into " + item))
                    }
                    ids[i++] = l
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
            return ids
        }
        fun createDeviceContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem): Long {
            var _id: Long = databaseHelper.insert(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, null, DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, ContentValues()))
            item._id = _id
            return item._id
        }
        fun createOrUpdateDeviceContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem): Long {
            var cursor: Cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_ID, ContactItemColumns.KEY_DEVICE_CONTACTID), (ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?"), arrayOf(String.valueOf(item.deviceContactId)), null)
            var count: Int = 0
            var _id: Long = 0
            if ((null != cursor)) {
                count = cursor.getCount()
                if ((count > 0)) {
                    _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                }
                cursor.close()
            }
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, null, DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, ContentValues()))
                item._id = _id
            }
            else {
                item._id = _id
                databaseHelper.update(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, ContentValues()), (ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?"), arrayOf(String.valueOf(item.deviceContactId)))
            }
            createOrUpdateIntellibitzContactsDeviceContactJoin(databaseHelper, db, item.intellibitzContacts, item._id)
            return item._id
        }
        fun createOrUpdateWorkContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, workContact: ContactItem): Long {
            if ((null == workContact)) {
                Log.e(TAG, "createOrUpdateWorkContact: work contact is null - returning 0")
                return 0
            }
            if ((null == db)) {
                Log.e(TAG, "createOrUpdateWorkContact: db is null - returning 0")
                return 0
            }
            if ((null == databaseHelper)) {
                Log.e(TAG, "createOrUpdateWorkContact: db helper is null - returning 0")
                return 0
            }
            var deviceContact: ContactItem = null
            var uid: String = workContact.dataId
            var email: String = workContact.email
            var typeId: String = workContact.typeId
            var cursor: Cursor = null
            if ((typeId != null)) {
                cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_ID, ContactItemColumns.KEY_VERSION, ContactItemColumns.KEY_DATA_ID, ContactItemColumns.KEY_DEVICE_CONTACTID, ContactItemColumns.KEY_NAME, ContactItemColumns.KEY_FIRST_NAME, ContactItemColumns.KEY_LAST_NAME, ContactItemColumns.KEY_DISPLAY_NAME, ContactItemColumns.KEY_PIC, ContactItemColumns.KEY_CLOUD_PIC), ((((((("TRIM(" + ContactItemColumns.KEY_EMAILS) + ")") + " LIKE '%' || ? || '%'  OR ") + ContactItemColumns.KEY_INTELLIBITZ_ID) + " LIKE '%' || ? || '%'  OR ") + ContactItemColumns.KEY_INTELLIBITZ_ID) + " = ? "), arrayOf(email, typeId, uid), null)
            }
            var _id: Long = 0
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                deviceContact = createsDeviceContactItem(workContact)
                deviceContact.intellibitzContacts
                _id = databaseHelper.insert(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, null, DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(deviceContact, ContentValues()))
                deviceContact._id = _id
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                var ver: Int = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_VERSION))
                var dataId: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID))
                var dcid: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                var name: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
                var first: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                var last: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                var display: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
                var pic: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                var cpic: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                cursor.close()
                deviceContact = createsDeviceContactItem(_id, ver, dataId, name, first, last, display, pic, cpic)
                deviceContact._id = _id
                deviceContact.deviceContactId = dcid
                deviceContact.intellibitzContacts
                databaseHelper.update(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(deviceContact, ContentValues()), (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(deviceContact._id)))
            }
            createOrUpdateIntellibitzContactsDeviceContactJoin(databaseHelper, db, deviceContact.intellibitzContacts, deviceContact._id)
            return deviceContact._id
        }
        fun createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var _id: Long = IntellibitzContactContentProvider.createOrUpdateIntellibitzContact(databaseHelper, db, item)
            IntellibitzContactContentProvider.createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper, db, _id, id)
            return item._id
        }
        fun createOrUpdateIntellibitzContactsDeviceContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, items: Collection<ContactItem>, id: Long): Array<Long> {
            if ((null == items)) {
                return LongArray(0)
            }
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            for (item in items) {
                var l: Long = createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper, db, item, id)
                if ((0 == l)) {
                    Log.e(TAG, ("Failed to insert row: " + item))
                    throw SQLException(("Failed to insert row into " + item))
                }
                ids[i++] = l
            }
            return ids
        }
        fun createDeviceContact(databaseHelper: DatabaseHelper, items: Set<ContactItem>): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                for (item in items) {
                    var l: Long = createDeviceContact(databaseHelper, db, item)
                    if ((0 == l)) {
                        Log.e(TAG, ("Failed to insert row: " + item))
                        throw SQLException(("Failed to insert row into " + item))
                    }
                    ids[i++] = l
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
            return ids
        }
        fun getDeviceContactsJoin(databaseHelper: DatabaseHelper, id: Long): Cursor {
            var selectQuery: String = ((((((("SELECT  " + "*, ct.id as ctid, m.id as mid  FROM  ") + DeviceContactContentProvider.TABLE_DEVICECONTACTS) + " WHERE ct.") + ContactItemColumns.KEY_ID) + " = ") + id) + "")
            Log.e(TAG, selectQuery)
            return databaseHelper.rawQuery(selectQuery, null)
        }
        fun getDeviceContactsJoin(databaseHelper: DatabaseHelper, id: String): Cursor {
            var selectQuery: String = ((((((("SELECT  " + "*, ct.id as ctid, m.id as mid  FROM  ") + DeviceContactContentProvider.TABLE_DEVICECONTACTS) + " WHERE ct.") + ContactItemColumns.KEY_DATA_ID) + " = ") + id) + "")
            Log.e(TAG, selectQuery)
            return databaseHelper.rawQuery(selectQuery, null)
        }
        fun updateDeviceContactJoin(databaseHelper: DatabaseHelper, item: ContactItem): Long {
            if ((0 == item._id)) {
                getDeviceContactDBId(databaseHelper, item)
            }
            if ((item._id != 0)) {
                updateDeviceContactDataId(databaseHelper, item)
            }
            return item._id
        }
        fun getDeviceContactDBId(databaseHelper: DatabaseHelper, item: ContactItem): Long {
            var cursor: Cursor = databaseHelper.query(DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_ID, ContactItemColumns.KEY_DEVICE_CONTACTID), (ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?"), arrayOf(String.valueOf(item.deviceContactId)), null)
            if ((cursor != null)) {
                if ((cursor.getCount() != 0)) {
                    var _id: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                    item._id = _id
                }
                cursor.close()
            }
            return item._id
        }
        fun updateDeviceContactJoin_(databaseHelper: DatabaseHelper, item: ContactItem): Long {
            var cursor: Cursor = databaseHelper.query(DeviceContactContentProvider.TABLE_DEVICECONTACTS, arrayOf(ContactItemColumns.KEY_ID, ContactItemColumns.KEY_DEVICE_CONTACTID), (ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?"), arrayOf(String.valueOf(item.deviceContactId)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                return 0
            }
            else {
                var _id: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
                updateDeviceContactDataId(databaseHelper, item)
            }
            return item._id
        }
        fun updateDeviceContactDataId(databaseHelper: DatabaseHelper, item: ContactItem): Int {
            return databaseHelper.update(DeviceContactContentProvider.TABLE_DEVICECONTACTS, DeviceContactContentProvider.fillContentValuesFromDeviceContactItemDataId(item, ContentValues()), (ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?"), arrayOf(String.valueOf(item.deviceContactId)))
        }
        fun updateDeviceContact(databaseHelper: DatabaseHelper, item: ContactItem): Int {
            return databaseHelper.update(DeviceContactContentProvider.TABLE_DEVICECONTACTS, DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, ContentValues()), (ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?"), arrayOf(String.valueOf(item.deviceContactId)))
        }
        fun updateDeviceContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem): Int {
            return databaseHelper.update(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, ContentValues()), (ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?"), arrayOf(String.valueOf(item.deviceContactId)))
        }
        fun createsDeviceContactItem(contactId: Int, ver: Int, dataId: String, name: String, firstName: String, lastName: String, displayName: String, profilePic: String, mobiles: Collection<String>, emails: Collection<String>): ContactItem {
            var deviceContact: ContactItem = createsDeviceContactItem(contactId, ver, dataId, name, firstName, lastName, displayName, profilePic, profilePic)
            deviceContact.mobiles = mobiles
            deviceContact.emails = emails
            return deviceContact
        }
        fun createsDeviceContactItem(contactId: Long, ver: Int, dataId: String, name: String, firstName: String, lastName: String, displayName: String, profilePic: String, cloudPic: String): ContactItem {
            var deviceContact: ContactItem = ContactItem()
            deviceContact._id = contactId
            if ((ver > 0)) {
                deviceContact.version = ver
            }
            deviceContact.deviceContactId = contactId
            deviceContact.dataId = dataId
            deviceContact.name = name
            deviceContact.firstName = firstName
            deviceContact.lastName = lastName
            deviceContact.displayName = displayName
            deviceContact.profilePic = profilePic
            deviceContact.cloudPic = cloudPic
            return deviceContact
        }
        fun createsDeviceContactItem(workContact: ContactItem): ContactItem {
            if ((null == workContact)) {
                return null
            }
            var deviceContact: ContactItem = createsDeviceContactItem(0, 1, workContact.dataId, workContact.name, workContact.name, workContact.name, workContact.name, workContact.profilePic, workContact.cloudPic)
            deviceContact.deviceContactId = 0
            return deviceContact
        }
    }
    private var databaseHelper: DatabaseHelper? = null
    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(getContext(), DatabaseHelper.DATABASE_NAME)
        return true
    }
    override fun getType(uri: Uri): String {
        when (URI_MATCHER.match(uri)) {
            CONTACTS_DIR_TYPE -> {
                return CONTACTS_DIR_MIME_TYPE
            }
            CONTACTS_ITEM_TYPE -> {
                return CONTACTS_ITEM_MIME_TYPE
            }
            CONTACTS_DATA_ITEM_TYPE -> {
                return CONTACTS_DATA_ITEM_MIME_TYPE
            }
            CONTACTS_JOIN_DIR_TYPE -> {
                return CONTACTS_JOIN_DIR_MIME_TYPE
            }
            CONTACTS_JOIN_ITEM_TYPE -> {
                return CONTACTS_JOIN_ITEM_MIME_TYPE
            }
            CONTACTS_JOIN_DATA_ITEM_TYPE -> {
                return CONTACTS_JOIN_DATA_ITEM_MIME_TYPE
            }
            CONTACTS_RAW_DIR_TYPE -> {
                return CONTACTS_RAW_DIR_MIME_TYPE
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
            CONTACTS_DIR_TYPE, CONTACTS_JOIN_DIR_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_DEVICECONTACTS, projection, selection, selectionArgs, sortOrder)
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
            CONTACTS_ITEM_TYPE, CONTACTS_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_DEVICECONTACTS, projection, selection, selectionArgs, sortOrder)
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
            CONTACTS_JOIN_ITEM_TYPE -> {
                try {
                    cursor = getDeviceContactsJoin(databaseHelper, ContentUris.parseId(uri))
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
            CONTACTS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    cursor = getDeviceContactsJoin(databaseHelper, uri.getLastPathSegment())
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
            CONTACTS_RAW_DIR_TYPE -> {
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
        var vals2: Array<Byte> = values.getAsByteArray(ContactItem.WORK_CONTACT)
        var ids: Array<Long> = null
        when (URI_MATCHER.match(uri)) {
            CONTACTS_DIR_TYPE -> {
                try {
                    if ((vals1 != null)) {
                        var deviceContactItems: Collection<ContactItem> = (MainApplicationSingleton.Serializer.deserialize(vals1) as Collection<ContactItem>)
                        if (((null == deviceContactItems) || deviceContactItems.empty)) {
                            Log.e(TAG, "INSERT returns null - EMPTY contacts to insert - check input")
                            return null
                        }
                        ids = createOrUpdateDeviceContacts(databaseHelper, deviceContactItems)
                    }
                    else {
                        if ((vals2 != null)) {
                            var deviceContactItems: Collection<ContactItem> = (MainApplicationSingleton.Serializer.deserialize(vals2) as Collection<ContactItem>)
                            if (((null == deviceContactItems) || deviceContactItems.empty)) {
                                Log.e(TAG, "INSERT returns null - EMPTY contacts to insert - check input")
                                return null
                            }
                            ids = createOrUpdateWorkContacts(databaseHelper, deviceContactItems)
                        }
                    }
                    if (((null == ids) || (0 == ids.size))) {
                        Log.e(TAG, "INSERT returns null - contacts failed to insert - check input")
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
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            CONTACTS_ITEM_TYPE -> {
                try {
                    var item: ContactItem = (MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem)
                    var id: Long = createOrUpdateDeviceContact(databaseHelper, item)
                    if ((0 == id)) {
                        Log.e(TAG, "INSERT returns null - contacts failed to insert - check input")
                        return null
                    }
                    var context: Context = getContext()
                    var insertUri: Uri = ContentUris.withAppendedId(uri, id)
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return insertUri
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
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
            CONTACTS_DIR_TYPE, CONTACTS_ITEM_TYPE -> {
                try {
                    id = databaseHelper.update(TABLE_DEVICECONTACTS, values, selection, selectionArgs)
                    updateUri = ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, id)
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
            CONTACTS_JOIN_DIR_TYPE, CONTACTS_JOIN_ITEM_TYPE -> {
                try {
                    var vals1: Array<Byte> = values.getAsByteArray(ContactItem.DEVICE_CONTACT)
                    var item: ContactItem = (MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem)
                    id = (updateDeviceContactJoin(databaseHelper, item) as Int)
                    updateUri = ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, id)
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return id
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
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
    override fun delete(uri: Uri, selection: String, selectionArgs: Array<String>): Int {
        when (URI_MATCHER.match(uri)) {
            CONTACTS_DIR_TYPE, CONTACTS_ITEM_TYPE -> {
                try {
                    var id: Int = databaseHelper.delete(TABLE_DEVICECONTACTS, selection, selectionArgs)
                    var insertUri: Uri = ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, id)
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
