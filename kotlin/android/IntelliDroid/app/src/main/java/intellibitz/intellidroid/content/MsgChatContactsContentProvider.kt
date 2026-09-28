

package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import intellibitz.intellidroid.data.BaseItem
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.ContactsContactJoinColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.db.MessagesContactsJoinColumns
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.db.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import intellibitz.intellidroid.content.DeviceContactContentProvider.getDeviceContactsJoin
import intellibitz.intellidroid.content.MsgChatContactContentProvider.TABLE_MSGCHATCONTACT
import intellibitz.intellidroid.db.MessagesContactsJoinColumns.KEY_MSGTHREAD_ID

class MsgChatContactsContentProvider : ContentProvider() {
    companion object {
        const val TAG = "MsgChatContactsCP"
        const val TABLE_MSGCHATCONTACTS = "msgchatcontacts"
        const val TABLE_MSGCHATCONTACTS_CONTACT_JOIN = "msgchatcontacts_contact"
        const val CREATE_TABLE_MSGCHATCONTACTS = (("CREATE TABLE " + TABLE_MSGCHATCONTACTS) + ContactItemColumns.TABLE_CONTACTS_SCHEMA)
        const val SCHEME = "content://"
        const val CONTACTS_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all")
        const val CONTACTS_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id")
        const val CONTACTS_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id")
        const val CONTACTS_JOIN_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/all")
        const val CONTACTS_JOIN_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id")
        const val CONTACTS_JOIN_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id")
        const val CONTACTS_RAW_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all")
        const val CREATE_TABLE_MSGCHATCONTACTS_CONTACT_JOIN = ((((((((((("CREATE TABLE " + TABLE_MSGCHATCONTACTS_CONTACT_JOIN) + "( ") + ContactsContactJoinColumns.KEY_ID) + " INTEGER PRIMARY KEY,") + ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID) + " INTEGER,") + ContactsContactJoinColumns.KEY_CONTACT_ID) + " INTEGER,") + ContactsContactJoinColumns.KEY_TIMESTAMP) + " LONG") + ")")
        private const val CONTACTS_DIR_TYPE = 0
        private const val CONTACTS_ITEM_TYPE = 1
        private const val CONTACTS_DATA_ITEM_TYPE = 2
        private const val CONTACTS_JOIN_DIR_TYPE = 3
        private const val CONTACTS_JOIN_ITEM_TYPE = 4
        private const val CONTACTS_JOIN_DATA_ITEM_TYPE = 5
        private const val CONTACTS_RAW_DIR_TYPE = 6
        private const val SEARCH_SUGGEST = 7
        private const val REFRESH_SHORTCUT = 8
        var AUTHORITY: String = "intellibitz.intellidroid.content.MsgChatContactsContentProvider"
        val CONTENT_URI: Uri = Uri.parse((((SCHEME + AUTHORITY) + "/") + TABLE_MSGCHATCONTACTS))
        val JOIN_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "join_") + TABLE_MSGCHATCONTACTS))
        val RAW_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "raw_") + TABLE_MSGCHATCONTACTS))
        private val URI_MATCHER: UriMatcher = buildUriMatcher()
        private fun buildUriMatcher(): UriMatcher {
            var matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_MSGCHATCONTACTS, CONTACTS_DIR_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MSGCHATCONTACTS + "/#"), CONTACTS_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MSGCHATCONTACTS + "/*"), CONTACTS_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("join_" + TABLE_MSGCHATCONTACTS), CONTACTS_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MSGCHATCONTACTS) + "/#"), CONTACTS_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MSGCHATCONTACTS) + "/*"), CONTACTS_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("raw_" + TABLE_MSGCHATCONTACTS), CONTACTS_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_QUERY + "/*"), SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*"), REFRESH_SHORTCUT)
            return matcher
        }
        fun createsContactItemsFromGetGroupsJSONArray(jsonArray: JSONArray): Collection<ContactItem> {
            if ((null == jsonArray)) {
                return null
            }
            var len: Int = jsonArray.length()
            var contactItems: Collection<ContactItem> = ArrayList(len)
            if ((0 == len)) {
                return contactItems
            }
            var i: Int = 0
            while ((i < len)) {
                try {
                    var jsonObject: JSONObject = jsonArray.getJSONObject(i)
                    if ((jsonObject != null)) {
                        var group: JSONObject = jsonObject.optJSONObject("group")
                        if ((group != null)) {
                            var contactItem: ContactItem = createsContactFromJSON(jsonObject)
                            var groupInfo: JSONArray = jsonObject.optJSONArray("group_info")
                            updateContactThreadWithGroupInfoFromJSON(groupInfo, contactItem)
                            contactItem.newGroup = false
                            contactItem.group = true
                            contactItems.add(contactItem)
                        }
                    }
                }
                catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                i++
            }
            return contactItems
        }
        fun fillJsonArrayFromContactItem(contactItems: Collection<ContactItem>, context: Context): JSONArray {
            var contacts: JSONArray = JSONArray()
            for (contact in contactItems) {
                try {
                    var firstName: String = contact.name
                    var jsonObject: JSONObject = JSONObject()
                    jsonObject.put("first_name", firstName)
                    jsonObject.put("device_ref", contact.deviceRef)
                    jsonObject.put("c_id", contact._id)
                    jsonObject.put("profile_pic", contact.profilePic)
                    var mobiles: JSONArray = JSONArray()
                    contacts.put(jsonObject)
                }
                catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            return contacts
        }
        fun fillContactThreadItemFromCursor(contactItem: ContactItem, cursor: Cursor) {
            contactItem._id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
            contactItem.deviceContactId = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
            contactItem.dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID))
            contactItem.typeId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TYPE_ID))
            contactItem.docType = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DOC_TYPE))
            contactItem.name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
            contactItem.firstName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
            contactItem.lastName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
            contactItem.displayName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME))
            contactItem.profilePic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
            contactItem.status = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_STATUS))
            contactItem.dataRev = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_REV))
            contactItem.type = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TYPE))
            contactItem.docOwner = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DOC_OWNER))
            contactItem.timestamp = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_TIMESTAMP))
            contactItem.dateTime = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATETIME))
        }
        fun createsContactsFromCursor(cursor: Cursor): List<ContactItem> {
            var contactItems: List<ContactItem> = ArrayList()
            do {
                var contactItem: ContactItem = ContactItem()
                fillContactThreadItemFromCursor(contactItem, cursor)
                contactItems.add(contactItem)
            } while (cursor.moveToNext())
            return contactItems
        }
        fun createsContactsFromJoinCursor(cursor: Cursor): List<ContactItem> {
            var contactItems: List<ContactItem> = ArrayList()
            var contactThreadItem: ContactItem
            do {
                var sid: String = cursor.getString(cursor.getColumnIndex("gcid"))
                contactThreadItem = MainApplicationSingleton.getBaseItem(sid, contactItems)
                if ((null == contactThreadItem)) {
                    contactThreadItem = ContactItem()
                    contactThreadItem._id = cursor.getLong(cursor.getColumnIndex("gc_id"))
                    contactThreadItem.deviceContactId = cursor.getLong(cursor.getColumnIndex("gcdcid"))
                    contactThreadItem.dataId = sid
                    contactThreadItem.name = cursor.getString(cursor.getColumnIndex("gcname"))
                    contactThreadItem.firstName = cursor.getString(cursor.getColumnIndex("gcfname"))
                    contactThreadItem.lastName = cursor.getString(cursor.getColumnIndex("gclname"))
                    contactThreadItem.displayName = cursor.getString(cursor.getColumnIndex("gcdname"))
                    contactThreadItem.profilePic = cursor.getString(cursor.getColumnIndex("gcpic"))
                    contactThreadItem.status = cursor.getString(cursor.getColumnIndex("gcstat"))
                    contactThreadItem.type = cursor.getString(cursor.getColumnIndex("gctype"))
                    contactThreadItem.typeId = cursor.getString(cursor.getColumnIndex("gctypeid"))
                    contactThreadItem.group = cursor.getInt(cursor.getColumnIndex("gcisg"))
                    contactThreadItem.emailItem = cursor.getInt(cursor.getColumnIndex("gcise"))
                    contactItems.add(contactThreadItem)
                }
                var akid: String = cursor.getString(cursor.getColumnIndex("akid"))
                if ((null == akid)) {

                }
                else {
                    var contactItem: ContactItem = contactThreadItem.getContactItem(akid)
                    if ((null == contactItem)) {
                        var intellibitzContactItem: ContactItem = ContactItem(akid)
                        contactItem = ContactItem(intellibitzContactItem)
                        contactThreadItem.addContact(contactItem)
                    }
                    contactItem._id = cursor.getLong(cursor.getColumnIndex("ak_id"))
                    contactItem.typeId = cursor.getString(cursor.getColumnIndex("aktypeid"))
                    contactItem.name = cursor.getString(cursor.getColumnIndex("akname"))
                    contactItem.type = cursor.getString(cursor.getColumnIndex("aktype"))
                    contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex("akonid"))
                    contactItem.status = cursor.getString(cursor.getColumnIndex("akstatus"))
                    contactItem.group = cursor.getInt(cursor.getColumnIndex("akisg"))
                    contactItem.emailItem = cursor.getInt(cursor.getColumnIndex("akise"))
                    contactItem.cloud = cursor.getInt(cursor.getColumnIndex("akisc"))
                    contactItem.device = cursor.getInt(cursor.getColumnIndex("akisd"))
                    contactItem.anonymous = cursor.getInt(cursor.getColumnIndex("akisa"))
                }
            } while (cursor.moveToNext())
            return contactItems
        }
        fun fillContactThreadFromJoinCursor(contactThreadItem: ContactItem, cursor: Cursor) {
            contactThreadItem._id = cursor.getLong(cursor.getColumnIndex("gc_id"))
            contactThreadItem.deviceContactId = cursor.getLong(cursor.getColumnIndex("gcdcid"))
            contactThreadItem.dataId = cursor.getString(cursor.getColumnIndex("gcid"))
            contactThreadItem.typeId = cursor.getString(cursor.getColumnIndex("gctypeid"))
            contactThreadItem.name = cursor.getString(cursor.getColumnIndex("gcname"))
            contactThreadItem.firstName = cursor.getString(cursor.getColumnIndex("gcfname"))
            contactThreadItem.lastName = cursor.getString(cursor.getColumnIndex("gclname"))
            contactThreadItem.displayName = cursor.getString(cursor.getColumnIndex("gcdname"))
            contactThreadItem.profilePic = cursor.getString(cursor.getColumnIndex("gcpic"))
            contactThreadItem.status = cursor.getString(cursor.getColumnIndex("gcstat"))
            contactThreadItem.type = cursor.getString(cursor.getColumnIndex("gctype"))
            contactThreadItem.typeId = cursor.getString(cursor.getColumnIndex("gctypeid"))
            contactThreadItem.group = cursor.getInt(cursor.getColumnIndex("gcisg"))
            contactThreadItem.emailItem = cursor.getInt(cursor.getColumnIndex("gcise"))
            do {
                var akid: String = cursor.getString(cursor.getColumnIndex("akid"))
                if ((null == akid)) {

                }
                else {
                    var contactItem: ContactItem = contactThreadItem.getContactItem(akid)
                    if ((null == contactItem)) {
                        var intellibitzContactItem: ContactItem = ContactItem(akid)
                        contactItem = ContactItem(intellibitzContactItem)
                        contactThreadItem.addContact(contactItem)
                    }
                    contactItem._id = cursor.getLong(cursor.getColumnIndex("ak_id"))
                    contactItem.name = cursor.getString(cursor.getColumnIndex("akname"))
                    contactItem.type = cursor.getString(cursor.getColumnIndex("aktype"))
                    contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex("akonid"))
                    contactItem.profilePic = cursor.getString(cursor.getColumnIndex("akpic"))
                    contactItem.status = cursor.getString(cursor.getColumnIndex("akstatus"))
                    contactItem.typeId = cursor.getString(cursor.getColumnIndex("aktypeid"))
                    contactItem.group = cursor.getInt(cursor.getColumnIndex("akisg"))
                    contactItem.emailItem = cursor.getInt(cursor.getColumnIndex("akise"))
                    contactItem.cloud = cursor.getInt(cursor.getColumnIndex("akisc"))
                    contactItem.device = cursor.getInt(cursor.getColumnIndex("akisd"))
                    contactItem.anonymous = cursor.getInt(cursor.getColumnIndex("akisa"))
                }
            } while (cursor.moveToNext())
        }
        @Throws(JSONException::class)
        fun updateContactThreadWithGroupInfoFromJSON(jsonArray: JSONArray, contactItem: ContactItem): ContactItem {
            if (((null == jsonArray) || (0 == jsonArray.length()))) {
                return contactItem
            }
            var i: Int = 0
            while ((i < jsonArray.length())) {
                updateContactThreadWithGroupInfoFromJSON(jsonArray.getJSONObject(i), contactItem)
                i++
            }
            return contactItem
        }
        @Throws(JSONException::class)
        fun updateContactThreadWithGroupInfoFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            if ((null == jsonObject)) {
                return contactItem
            }
            var actionText: String = jsonObject.getString("action_text")
            if ("CREATED_GROUP") {
                fillCreatedGroupInfoFromJSON(jsonObject, contactItem)
                if (TextUtils.isEmpty(contactItem.dataId)) {
                    return null
                }
            }
            else {
                if ("ADDED_USERS") {
                    fillAddedUsersInfoFromJSON(jsonObject, contactItem)
                    if (TextUtils.isEmpty(contactItem.dataId)) {
                        return null
                    }
                }
                else {
                    if ("REMOVED_USERS") {
                        fillRemovedUsersInfoFromJSON(jsonObject, contactItem)
                        if (TextUtils.isEmpty(contactItem.dataId)) {
                            return null
                        }
                    }
                    else {
                        if ("MADE_ADMIN") {
                            fillMadeAdminInfoFromJSON(jsonObject, contactItem)
                            if (TextUtils.isEmpty(contactItem.dataId)) {
                                return null
                            }
                        }
                        else {
                            if ("MADE_USER") {
                                fillMadeUserInfoFromJSON(jsonObject, contactItem)
                                if (TextUtils.isEmpty(contactItem.dataId)) {
                                    return null
                                }
                            }
                            else {
                                if ("LEFT_GROUP") {
                                    fillLeftGroupInfoFromJSON(jsonObject, contactItem)
                                    if (TextUtils.isEmpty(contactItem.dataId)) {
                                        return null
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(contactItem.dataId)) {
                return null
            }
            return contactItem
        }
        @Throws(JSONException::class)
        fun updatePlusSaveContactThreadWithGroupInfoFromJSON(jsonObject: JSONObject, context: Context): ContactItem {
            var contactItem: ContactItem = ContactItem()
            var actionText: String = jsonObject.getString("action_text")
            if ("CREATED_GROUP") {
                fillCreatedGroupInfoFromJSON(jsonObject, contactItem)
                if (TextUtils.isEmpty(contactItem.dataId)) {
                    return null
                }
                savesContactThreadInDB(contactItem, context.getApplicationContext())
            }
            else {
                if ("ADDED_USERS") {
                    fillAddedUsersInfoFromJSON(jsonObject, contactItem)
                    if (TextUtils.isEmpty(contactItem.dataId)) {
                        return null
                    }
                    savesContactThreadInDB(contactItem, context.getApplicationContext())
                }
                else {
                    if ("REMOVED_USERS") {
                        fillRemovedUsersInfoFromJSON(jsonObject, contactItem)
                        if (TextUtils.isEmpty(contactItem.dataId)) {
                            return null
                        }
                        deleteContact(contactItem, context.getApplicationContext())
                    }
                    else {
                        if ("MADE_ADMIN") {
                            fillMadeAdminInfoFromJSON(jsonObject, contactItem)
                            if (TextUtils.isEmpty(contactItem.dataId)) {
                                return null
                            }
                            savesContactThreadInDB(contactItem, context.getApplicationContext())
                        }
                        else {
                            if ("MADE_USER") {
                                fillMadeUserInfoFromJSON(jsonObject, contactItem)
                                if (TextUtils.isEmpty(contactItem.dataId)) {
                                    return null
                                }
                                savesContactThreadInDB(contactItem, context.getApplicationContext())
                            }
                            else {
                                if ("LEFT_GROUP") {
                                    fillLeftGroupInfoFromJSON(jsonObject, contactItem)
                                    if (TextUtils.isEmpty(contactItem.dataId)) {
                                        return null
                                    }
                                    deleteContact(contactItem, context.getApplicationContext())
                                }
                            }
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(contactItem.dataId)) {
                return null
            }
            savesContactThreadInDB(contactItem, context.getApplicationContext())
            return contactItem
        }
        @Throws(JSONException::class)
        fun createOrUpdateContactFromJSON(jsonObject: JSONObject, context: Context): Uri {
            var contactItem: ContactItem = ContactItem()
            fillContactsFromJSON(jsonObject, contactItem)
            return savesContactThreadInDB(contactItem, context.getApplicationContext())
        }
        @Throws(JSONException::class)
        fun updateGroupDetailsInDBFromJSON(jsonObject: JSONObject, contactItem: ContactItem, context: Context): Uri {
            var info: JSONObject = jsonObject.getJSONObject("info")
            fillContactsFromJSON(info, contactItem)
            return savesContactThreadInDB(contactItem, context.getApplicationContext())
        }
        @Throws(JSONException::class)
        fun fillAddedUsersInfoFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            return fillsContactThreadInfoFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun fillCreatedGroupInfoFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            return fillsContactThreadInfoFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun fillRemovedUsersInfoFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            return fillsContactThreadInfoFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun fillMadeAdminInfoFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            return fillsContactThreadInfoFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun fillMadeUserInfoFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            return fillsContactThreadInfoFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun fillLeftGroupInfoFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            return fillsContactThreadInfoFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun fillsContactThreadInfoFromJSON(jsonObject: JSONObject, contactThreadItem: ContactItem): ContactItem {
            var actionText: String = jsonObject.getString("action_text")
            var _id: String = jsonObject.getString("_id")
            contactThreadItem.dataId = jsonObject.getString("group_id")
            contactThreadItem.intellibitzId = jsonObject.getString("group_id")
            contactThreadItem.typeId = jsonObject.getString("group_id")
            contactThreadItem.groupId = jsonObject.getString("group_id")
            contactThreadItem.emailItem = false
            contactThreadItem.group = true
            var group_name: String = jsonObject.optString("group_name")
            if (!TextUtils.isEmpty(group_name)) {
                contactThreadItem.name = group_name
            }
            var infoItem: BaseItem = BaseItem(_id, contactThreadItem.dataId, "infoitem", actionText)
            contactThreadItem.infoItems
            contactThreadItem.dataRev = jsonObject.optString("_rev")
            var doc_type: String = jsonObject.optString("doc_type")
            if (!TextUtils.isEmpty(doc_type)) {
                contactThreadItem.baseType = doc_type
                contactThreadItem.docType = doc_type
            }
            var doc_owner: String = jsonObject.optString("doc_owner")
            if (!TextUtils.isEmpty(doc_owner)) {
                contactThreadItem.docOwner = doc_owner
            }
            var timestamp: Long = jsonObject.optLong("timestamp")
            if ((timestamp > 0)) {
                contactThreadItem.timestamp = timestamp
            }
            var actionBy: String = jsonObject.optString("action_by")
            if (((actionBy != null) && !actionBy.empty)) {
                var intellibitzContactItem: ContactItem = ContactItem(actionBy)
                var contactItem: ContactItem = ContactItem(intellibitzContactItem)
            }
            var jsonArray: JSONArray = jsonObject.optJSONArray("action_on")
            if (((jsonArray != null) && (jsonArray.length() > 0))) {
                var i: Int = 0
                while ((i < jsonArray.length())) {
                    var object: JSONObject = jsonArray.getJSONObject(i)
                    var uid: String = object.optString("uid")
                    var type: String = object.optString("type")
                    var name: String = object.optString("name")
                    var mobile: String = object.optString("mobile")
                    if (!TextUtils.isEmpty(uid)) {
                        var contactItem: ContactItem = ContactItem()
                        contactItem.dataId = uid
                        contactItem.intellibitzId = uid
                        if (!TextUtils.isEmpty(name)) {
                            contactItem.name = name
                        }
                        if (!TextUtils.isEmpty(type)) {
                            contactItem.type = type
                        }
                        if (!TextUtils.isEmpty(mobile)) {
                            contactItem.typeId = mobile
                        }
                        contactThreadItem.addContact(contactItem)
                    }
                    i++
                }
            }
            return contactThreadItem
        }
        @Throws(JSONException::class)
        fun createsContactFromGetGroupsJSON(jsonObject: JSONObject): ContactItem {
            var contactItem: ContactItem = ContactItem()
            contactItem.dataId = jsonObject.getString("group_id")
            contactItem.name = jsonObject.optString("group_name")
            return contactItem
        }
        @Throws(JSONException::class)
        fun fillGroupFromJSON(jsonObject: JSONObject, contactItem: ContactItem): ContactItem {
            return fillContactsFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun createsContactFromJSON(jsonObject: JSONObject): ContactItem {
            var contactItem: ContactItem = ContactItem()
            return fillContactsFromJSON(jsonObject, contactItem)
        }
        @Throws(JSONException::class)
        fun fillContactsFromJSON(jsonObject: JSONObject, contactThreadItem: ContactItem): ContactItem {
            contactThreadItem.dataId = jsonObject.optString("_id")
            contactThreadItem.dataRev = jsonObject.optString("_rev")
            contactThreadItem.name = jsonObject.optString("name")
            contactThreadItem.profilePic = jsonObject.optString("profile_pic")
            contactThreadItem.baseType = jsonObject.optString("doc_type")
            contactThreadItem.docType = jsonObject.optString("doc_type")
            contactThreadItem.docOwner = jsonObject.optString("doc_owner")
            contactThreadItem.timestamp = jsonObject.optLong("timestamp")
            var jsonArray: JSONArray = jsonObject.optJSONArray("users")
            if (((jsonArray != null) && (jsonArray.length() > 0))) {
                var i: Int = 0
                while ((i < jsonArray.length())) {
                    var object: JSONObject = jsonArray.optJSONObject(i)
                    var intellibitzId: String = object.optString("uid")
                    var type: String = object.optString("type")
                    var name: String = object.optString("name")
                    var mobile: String = object.optString("mobile")
                    if (((mobile != null) && !mobile.empty)) {
                        var contactItem: ContactItem = ContactItem()
                        contactItem.intellibitzId = intellibitzId
                        contactItem.dataId = contactItem.intellibitzId
                        contactItem.type = type
                        contactItem.name = name
                        contactItem.typeId = mobile
                        contactThreadItem.addContact(contactItem)
                    }
                    i++
                }
            }
            return contactThreadItem
        }
        fun fillContentValuesFromContactThreadItem(contactItem: ContactItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_ID, contactItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_CONTACTID, contactItem.deviceContactId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE_ID, contactItem.typeId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_INTELLIBITZ_ID, contactItem.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_GROUP_ID, contactItem.groupId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE_ID, contactItem.typeId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_NAME, contactItem.name)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_FIRST_NAME, contactItem.firstName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_LAST_NAME, contactItem.lastName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DISPLAY_NAME, contactItem.displayName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_REV, contactItem.dataRev)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_REF, contactItem.deviceRef)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DOC_TYPE, contactItem.docType)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_BASE_TYPE, contactItem.baseType)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DOC_OWNER, contactItem.docOwner)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TYPE, contactItem.getType())
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_STATUS, contactItem.status)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PIC, contactItem.profilePic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_GROUP, contactItem.group)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_EMAIL, contactItem.emailItem)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TIMESTAMP, contactItem.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis(contactItem.timestamp))
            return values
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
        fun savesContactThreadInDB(contact: ContactItem, context: Context): Uri {
            var contacts: Collection<ContactItem> = Collections.synchronizedSet(HashSet<ContactItem>(1))
            contacts.add(contact)
            return savesContactThreadsInDB(contacts, context)
        }
        fun savesContactThreadsInDB(contacts: Collection<ContactItem>, context: Context): Uri {
            try {
                var contentValues: ContentValues = ContentValues()
                contentValues.put(ContactItem.TAG, MainApplicationSingleton.Serializer.serialize(contacts))
                return context.getContentResolver()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        @Throws(IOException::class)
        fun updatesContactThreadsInDB(contactItems: Collection<ContactItem>, context: Context): Int {
            var contentValues: ContentValues = ContentValues()
            contentValues.put(ContactItem.TAG, MainApplicationSingleton.Serializer.serialize(contactItems))
            return context.getContentResolver()
        }
        fun deleteContact(contactItem: ContactItem, contactThreadItem: ContactItem, context: Context): Int {
            var selectionArgs: Array<String?> = arrayOfNulls<String>(2)
            var _id: Long = contactItem._id
            var id: Long = contactThreadItem._id
            if (((0 == _id) || (0 == id))) {
                return 0
            }
            selectionArgs[0] = String.valueOf(_id)
            selectionArgs[1] = String.valueOf(id)
            return context.getContentResolver()
        }
        fun deleteContact(contactThreadItem: ContactItem, context: Context): Uri {
            var selectionArgs: Array<String?> = arrayOfNulls<String>(2)
            var contactItems: HashSet<ContactItem> = contactThreadItem.contactItems
            if (((null == contactItems) || contactItems.empty)) {
                return null
            }
            var contactItem: ContactItem = contactItems.iterator()
            if (((null == contactItem) || (null == contactItem.dataId))) {
                return null
            }
            var _id: Long = contactItem._id
            if ((0 == _id)) {
                MsgChatContactContentProvider.queryContactForDBId(contactItem, context)
            }
            var id: Long = contactThreadItem._id
            if (((0 == _id) || (0 == id))) {
                return null
            }
            selectionArgs[0] = String.valueOf(_id)
            selectionArgs[1] = String.valueOf(id)
            var uri: Uri = Uri.withAppendedPath(JOIN_CONTENT_URI, ("delete/" + _id))
            var row: Int = context.getContentResolver()
            if ((0 == row)) {
                return null
            }
            return uri
        }
        fun getContactJoin(databaseHelper: DatabaseHelper, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
            var selectQuery: String = ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("SELECT  " + " gc.") + ContactItemColumns.KEY_ID) + " as gc_id, ") + " gc.") + ContactItemColumns.KEY_DEVICE_CONTACTID) + " as gcdcid, ") + " gc.") + ContactItemColumns.KEY_DATA_ID) + " as gcid, ") + " gc.") + ContactItemColumns.KEY_NAME) + " as gcname, ") + " gc.") + ContactItemColumns.KEY_TYPE_ID) + " as gctypeid, ") + " gc.") + ContactItemColumns.KEY_FIRST_NAME) + " as gcfname, ") + " gc.") + ContactItemColumns.KEY_LAST_NAME) + " as gclname, ") + " gc.") + ContactItemColumns.KEY_DISPLAY_NAME) + " as gcdname, ") + " gc.") + ContactItemColumns.KEY_STATUS) + " as gcstat, ") + " gc.") + ContactItemColumns.KEY_PIC) + " as gcpic, ") + " gc.") + ContactItemColumns.KEY_TYPE) + " as gctype, ") + " gc.") + ContactItemColumns.KEY_DOC_OWNER) + " as gcdo, ") + " gc.") + ContactItemColumns.KEY_IS_GROUP) + " as gcisg, ") + " gc.") + ContactItemColumns.KEY_IS_EMAIL) + " as gcise, ") + " ak.") + ContactItemColumns.KEY_ID) + " as ak_id,") + " ak.") + ContactItemColumns.KEY_DATA_ID) + " as akid,") + " ak.") + ContactItemColumns.KEY_TYPE_ID) + " as aktypeid,") + " ak.") + ContactItemColumns.KEY_NAME) + " as akname,") + " ak.") + ContactItemColumns.KEY_PIC) + " as akpic,") + " ak.") + ContactItemColumns.KEY_TYPE) + " as aktype,") + " ak.") + ContactItemColumns.KEY_INTELLIBITZ_ID) + " as akonid,") + " ak.") + ContactItemColumns.KEY_IS_ANONYMOUS) + " as akisa,") + " ak.") + ContactItemColumns.KEY_IS_CLOUD) + " as akisc,") + " ak.") + ContactItemColumns.KEY_IS_DEVICE) + " as akisd,") + " ak.") + ContactItemColumns.KEY_IS_EMAIL) + " as akise,") + " ak.") + ContactItemColumns.KEY_IS_GROUP) + " as akisg,") + " ak.") + ContactItemColumns.KEY_STATUS) + " as akstatus,") + " ak.") + ContactItemColumns.KEY_TIMESTAMP) + " as aktime") + "  FROM  ") + TABLE_MSGCHATCONTACTS) + " gc ") + "left outer join ") + TABLE_MSGCHATCONTACTS_CONTACT_JOIN) + " gcak on gc.[") + ContactItemColumns.KEY_ID) + "] = gcak.[") + ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + MsgChatContactContentProvider.TABLE_MSGCHATCONTACT) + " ak on ak.[") + ContactItemColumns.KEY_ID) + "] = gcak.[") + ContactsContactJoinColumns.KEY_CONTACT_ID) + "] ")
            if (((selection != null) && !selection.empty)) {
                selectQuery += (" WHERE " + selection)
            }
            if (((sortOrder != null) && !sortOrder.empty)) {
                selectQuery += (" ORDER BY " + sortOrder)
            }
            return databaseHelper.rawQuery(selectQuery, selectionArgs)
        }
        fun createOrUpdateContactsContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var cursor: Cursor = databaseHelper.query(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? "), arrayOf(item.intellibitzId), null)
            var _id: Long
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                var values: ContentValues = ContentValues()
                MsgChatContactContentProvider.fillContentValuesFromContactItem(item, values)
                _id = databaseHelper.insert(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, null, values)
                item._id = _id
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
            }
            createOrUpdateContactsContactJoin(databaseHelper, db, _id, id)
            MsgChatContactContentProvider.createOrUpdateContactIntellibitzContactByContactJoin(databaseHelper, db, item, item._id)
            var values: ContentValues = ContentValues()
            MsgChatContactContentProvider.fillContentValuesFromContactItem(item, values)
            databaseHelper.update(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, values, (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            return item._id
        }
        fun updateContactThreadContact_(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var values: ContentValues = ContentValues()
            MsgChatContactContentProvider.fillContentValuesFromContactItem(item, values)
            var _id: Long = updateContactThreadContact(databaseHelper, db, item, id)
            createOrUpdateContactsContactJoin(databaseHelper, db, _id, id)
            return item._id
        }
        fun updateContactThreadContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var values: ContentValues = ContentValues()
            MsgChatContactContentProvider.fillContentValuesFromContactItem(item, values)
            var cursor: Cursor = databaseHelper.query(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? "), arrayOf(item.intellibitzId), null)
            var _id: Long
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                _id = databaseHelper.insert(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, null, values)
                item._id = _id
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
                databaseHelper.update(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, values, (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            createOrUpdateContactsContactJoin(databaseHelper, db, _id, id)
            return item._id
        }
        fun deleteContactThreadContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var cursor: Cursor = databaseHelper.query(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_DATA_ID + " = ? "), arrayOf(item.dataId), null)
            var _id: Long
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                return 0
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
                deleteContactThreadContactJoin(databaseHelper, db, item._id, id)
                _id = databaseHelper.delete(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, (ContactItemColumns.KEY_ID + " = ? "), arrayOf(String.valueOf(item._id)))
            }
            return _id
        }
        fun deleteContactThreadContact(databaseHelper: DatabaseHelper, db: SQLiteDatabase, _id: Long, id: Long): Long {
            var row: Long = deleteContactThreadContactJoin(databaseHelper, db, _id, id)
            if ((0 == row)) {
                return 0
            }
            return databaseHelper.delete(db, MsgChatContactContentProvider.TABLE_MSGCHATCONTACT, (ContactItemColumns.KEY_ID + " = ? "), arrayOf(String.valueOf(_id)))
        }
        fun deleteContactThreadContact(databaseHelper: DatabaseHelper, contactItem: ContactItem, contactThreadItem: ContactItem): Long {
            var rows: Long = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                rows = deleteContactThreadContact(databaseHelper, db, contactItem, contactThreadItem._id)
                if ((0 == rows)) {
                    Log.e(TAG, ("Failed to delete item: " + contactItem))
                    throw SQLException(("Failed to insert row into " + contactItem))
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
            return rows
        }
        fun deleteContactThreadContact(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var rows: Long = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                rows = deleteContactThreadContact(databaseHelper, db, id, fk)
                if ((0 == rows)) {
                    Log.e(TAG, ("Failed to delete item: " + id))
                    throw SQLException(("Failed to insert row into " + id))
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
            return rows
        }
        fun createOrUpdateContactsContacts(databaseHelper: DatabaseHelper, db: SQLiteDatabase, items: Set<ContactItem>, id: Long): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            var array: Array<ContactItem> = items.toArray(arrayOfNulls<ContactItem>(0))
            for (item in array) {
                var l: Long = createOrUpdateContactsContact(databaseHelper, db, item, id)
                if ((0 == l)) {
                    Log.e(TAG, ("Failed to insert row: " + item))
                    throw SQLException(("Failed to insert row into " + item))
                }
                ids[i++] = l
            }
            items.addAll(Arrays.asList(array))
            return ids
        }
        fun updateContactThreadContacts(databaseHelper: DatabaseHelper, db: SQLiteDatabase, items: Set<ContactItem>, id: Long): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            for (item in items) {
                var l: Long = updateContactThreadContact(databaseHelper, db, item, id)
                if ((0 == l)) {
                    Log.e(TAG, ("Failed to insert row: " + item))
                    throw SQLException(("Failed to insert row into " + item))
                }
                ids[i++] = l
            }
            return ids
        }
        fun createOrUpdateContacts(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem): Long {
            var cursor: Cursor = databaseHelper.query(db, MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_DATA_ID + " = ? "), arrayOf(contactItem.dataId), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                cursor = databaseHelper.query(db, MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? "), arrayOf(contactItem.intellibitzId), null)
            }
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                var _id: Long = databaseHelper.insert(db, MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, null, MsgChatContactsContentProvider.fillContentValuesFromContactThreadItem(contactItem, ContentValues()))
                contactItem._id = _id
            }
            else {
                var _id: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                contactItem._id = _id
                cursor.close()
            }
            MsgChatContactContentProvider.createOrUpdateContactIntellibitzContactByContactJoin(databaseHelper, db, contactItem, contactItem._id)
            databaseHelper.update(db, MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, MsgChatContactsContentProvider.fillContentValuesFromContactThreadItem(contactItem, ContentValues()), (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(contactItem._id)))
            val contactItems: HashSet<ContactItem> = contactItem.contactItems
            if (((contactItems != null) && !contactItems.empty)) {
                createOrUpdateContactsContacts(databaseHelper, db, contactItems, contactItem._id)
            }
            return contactItem._id
        }
        fun createOrUpdateContactsContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = getContactThreadContactJoin(databaseHelper, db, id, fk)
            var values: ContentValues = ContentValues()
            values.put(ContactsContactJoinColumns.KEY_CONTACT_ID, id)
            values.put(ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID, fk)
            values.put(ContactsContactJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.dateTimeMillis)
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, TABLE_MSGCHATCONTACTS_CONTACT_JOIN, null, values)
            }
            else {
                _id = databaseHelper.update(db, TABLE_MSGCHATCONTACTS_CONTACT_JOIN, values, (ContactsContactJoinColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return _id
        }
        fun deleteContactThreadContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = getContactThreadContactJoin(databaseHelper, db, id, fk)
            if ((0 == _id)) {
                return 0
            }
            else {
                _id = databaseHelper.delete(db, TABLE_MSGCHATCONTACTS_CONTACT_JOIN, (ContactsContactJoinColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return _id
        }
        fun getContactThreadContactJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = 0
            var c: Cursor = databaseHelper.query(db, TABLE_MSGCHATCONTACTS_CONTACT_JOIN, arrayOf(ContactsContactJoinColumns.KEY_ID), (((ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID + " = ? and ") + ContactsContactJoinColumns.KEY_CONTACT_ID) + " = ?"), arrayOf(String.valueOf(fk), String.valueOf(id)), null, null, null)
            if (((c != null) && (c.getCount() > 0))) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }
        fun createOrUpdateMessagesContacts(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem): Long {
            var _id: Long = createOrUpdateContacts(databaseHelper, db, contactItem)
            if ((0 == _id)) {
                return 0
            }
            var cursor: Cursor = databaseHelper.query(db, TABLE_MSGCHATCONTACTS_CONTACT_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID), (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(contactItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, TABLE_MSGCHATCONTACTS, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(contactItem.dataId)), null)
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                }
                else {
                    _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                    cursor.close()
                    createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, contactItem._id)
                }
            }
            else {
                cursor.close()
            }
            return contactItem._id
        }
        fun createOrUpdateMessageContacts(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem): Long {
            var _id: Long = createOrUpdateContacts(databaseHelper, db, contactItem)
            if ((0 == _id)) {
                return 0
            }
            var cursor: Cursor = databaseHelper.query(db, MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID), (MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + " = ?"), arrayOf(String.valueOf(contactItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, MessageChatContentProvider.TABLE_MESSAGESCHAT, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(contactItem.dataId)), null)
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                }
                else {
                    _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                    cursor.close()
                    createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, contactItem._id)
                }
            }
            else {
                cursor.close()
            }
            return contactItem._id
        }
        fun createOrUpdateContacts(databaseHelper: DatabaseHelper, items: Collection<ContactItem>): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                var array: Array<ContactItem> = items.toArray(arrayOfNulls<ContactItem>(0))
                for (item in array) {
                    var l: Long = createOrUpdateMessagesContacts(databaseHelper, db, item)
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
        fun updateContactThreads(databaseHelper: DatabaseHelper, items: Collection<ContactItem>): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                var array: Array<ContactItem> = items.toArray(arrayOfNulls<ContactItem>(0))
                for (item in array) {
                    var l: Long = updateContactThread(databaseHelper, db, item)
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
        fun updateContactThread(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem): Long {
            databaseHelper.update(db, MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, MsgChatContactsContentProvider.fillContentValuesFromContactThreadItem(contactItem, ContentValues()), (ContactItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(contactItem.dataId)))
            createOrUpdateContactsContacts(databaseHelper, db, contactItem.contactItems, contactItem._id)
            var cursor: Cursor = databaseHelper.query(db, MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID), (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(contactItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, MessageChatContentProvider.TABLE_MESSAGESCHAT, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(contactItem.dataId)), null)
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                }
                else {
                    var _id: Long = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                    cursor.close()
                    createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, contactItem._id)
                }
            }
            else {
                cursor.close()
            }
            return contactItem._id
        }
        fun createOrUpdateMessageContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var cursor: Cursor = getMessageContactThreadCursorJoin(databaseHelper, item, id)
            var _id: Long = 0
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_DATA_ID + " = ? "), arrayOf(item.dataId), null)
                if (((null != cursor) && (0 != cursor.getCount()))) {
                    _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                    cursor.close()
                    item._id = _id
                }
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
            }
            if ((_id != 0)) {
                createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, id)
            }
            return item._id
        }
        fun getMessageContactThreadCursorJoin(databaseHelper: DatabaseHelper, item: ContactItem, id: Long): Cursor {
            var args: Array<String> = arrayOf(item.dataId, String.valueOf(id))
            var selectQuery: String = ((((((((((((((((((((((("SELECT  * FROM " + MessageChatContentProvider.TABLE_MESSAGECHAT) + " nt ") + " left join ") + MessageChatContentProvider.TABLE_MESSAGECHAT_CONTACTS_JOIN) + " ntm on nt.[") + ContactItemColumns.KEY_ID) + "] = ntm.[") + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID) + "]  ") + " left join ") + MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS) + " mt on ntm.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] = mt.[") + ContactItemColumns.KEY_ID) + "] ") + " WHERE ") + " mt.") + ContactItemColumns.KEY_ID) + " = ? ") + " AND nt.") + MessageItemColumns.KEY_ID) + " = ? ")
            return databaseHelper.rawQuery(selectQuery, args)
        }
        fun createOrUpdateMessagesContactsJoin(databaseHelper: DatabaseHelper, item: ContactItem, id: Long): Long {
            var cursor: Cursor = getMessageThreadContactThreadCursorJoin(databaseHelper, item, id)
            var _id: Long = 0
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_DATA_ID + " = ? "), arrayOf(item.dataId), null)
                if (((null != cursor) && (0 != cursor.getCount()))) {
                    _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                    cursor.close()
                    item._id = _id
                }
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
            }
            if ((_id != 0)) {
                createOrUpdateMessagesContactsJoin(databaseHelper, databaseHelper.getWritableDatabase(), _id, id)
            }
            return item._id
        }
        fun createOrUpdateMessagesContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var cursor: Cursor = getMessageThreadContactThreadCursorJoin(databaseHelper, item, id)
            var _id: Long = 0
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_DATA_ID + " = ? "), arrayOf(item.dataId), null)
                if (((null != cursor) && (0 != cursor.getCount()))) {
                    _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                    cursor.close()
                    item._id = _id
                }
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
            }
            if ((_id != 0)) {
                createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, id)
            }
            return item._id
        }
        fun createOrUpdateMessagesContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = getMessagesContactsJoin(databaseHelper, db, id, fk)
            var values: ContentValues = ContentValues()
            values.put(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID, id)
            values.put(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID, fk)
            values.put(MessagesContactsJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.dateTimeMillis)
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT_CONTACTS_JOIN, null, values)
            }
            else {
                _id = databaseHelper.update(db, MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN, values, (MessagesContactsJoinColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return _id
        }
        fun getMessagesContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = 0
            var c: Cursor = databaseHelper.query(db, TABLE_MSGCHATCONTACTS_CONTACT_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_ID), (((MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ? and ") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + " = ?"), arrayOf(String.valueOf(id), String.valueOf(fk)), null, null, null)
            if (((c != null) && (c.getCount() > 0))) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }
        fun getMessageThreadContactThreadCursorJoin(databaseHelper: DatabaseHelper, item: ContactItem, id: Long): Cursor {
            var args: Array<String> = arrayOf(item.dataId, String.valueOf(id))
            var selectQuery: String = ((((((((((((((((((((((("SELECT  * FROM " + MessageChatContentProvider.TABLE_MESSAGESCHAT) + " nt ") + " left join ") + MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN) + " ntm on nt.[") + MessageItemColumns.KEY_ID) + "] = ntm.[") + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID) + "]  ") + " left join ") + MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS) + " mt on ntm.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] = mt.[") + ContactItemColumns.KEY_ID) + "] ") + " WHERE ") + " mt.") + ContactItemColumns.KEY_ID) + " = ? ") + " AND nt.") + MessageItemColumns.KEY_ID) + " = ? ")
            return databaseHelper.rawQuery(selectQuery, args)
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
            CONTACTS_DIR_TYPE, CONTACTS_ITEM_TYPE, CONTACTS_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_MSGCHATCONTACTS, projection, selection, selectionArgs, sortOrder)
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
            CONTACTS_JOIN_DIR_TYPE -> {
                try {
                    cursor = getContactJoin(databaseHelper, selection, selectionArgs, sortOrder)
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
                    cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, ContentUris.parseId(uri))
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
                    cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, uri.getLastPathSegment())
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
        var vals1: Array<Byte> = values.getAsByteArray(ContactItem.TAG)
        when (URI_MATCHER.match(uri)) {
            CONTACTS_DIR_TYPE -> {
                try {
                    var contactItems: Collection<ContactItem> = (MainApplicationSingleton.Serializer.deserialize(vals1) as Collection<ContactItem>)
                    if (((null == contactItems) || contactItems.empty)) {
                        return null
                    }
                    var ids: Array<Long> = createOrUpdateContacts(databaseHelper, contactItems)
                    if ((((null == ids) || (0 == ids.size)) || (ids.size != contactItems.size()))) {
                        return null
                    }
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return Uri.withAppendedPath(uri, String.valueOf(ids[0]))
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            CONTACTS_ITEM_TYPE -> {
                try {
                    var contactItem: ContactItem = (MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem)
                    var contactItems: ArrayList<ContactItem> = ArrayList(1)
                    contactItems.add(contactItem)
                    var ids: Array<Long> = createOrUpdateContacts(databaseHelper, contactItems)
                    var insertUri: Uri = ContentUris.withAppendedId(MsgChatContactsContentProvider.CONTENT_URI, ids[0])
                    var context: Context = getContext()
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
        var vals1: Array<Byte> = values.getAsByteArray(ContactItem.TAG)
        when (URI_MATCHER.match(uri)) {
            CONTACTS_DIR_TYPE, CONTACTS_JOIN_DIR_TYPE -> {
                try {
                    var contactItems: Collection<ContactItem> = (MainApplicationSingleton.Serializer.deserialize(vals1) as Collection<ContactItem>)
                    if (((null == contactItems) || contactItems.empty)) {
                        return 0
                    }
                    var ids: Array<Long> = updateContactThreads(databaseHelper, contactItems)
                    if ((((null == ids) || (0 == ids.size)) || (ids.size != contactItems.size()))) {
                        return 0
                    }
                    var context: Context = getContext()
                    if ((null != context)) {
                        var updateUri: Uri = ContentUris.withAppendedId(MsgChatContactsContentProvider.CONTENT_URI, ids[0])
                        context.getContentResolver()
                    }
                    return (ids[0] as Int)
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            CONTACTS_ITEM_TYPE, CONTACTS_JOIN_ITEM_TYPE -> {
                try {
                    var contactItem: ContactItem = (MainApplicationSingleton.Serializer.deserialize(vals1) as ContactItem)
                    if ((null == contactItem)) {
                        return 0
                    }
                    var contactItems: ArrayList<ContactItem> = ArrayList(1)
                    contactItems.add(contactItem)
                    var ids: Array<Long> = updateContactThreads(databaseHelper, contactItems)
                    if ((((null == ids) || (0 == ids.size)) || (ids.size != contactItems.size()))) {
                        return 0
                    }
                    var context: Context = getContext()
                    if ((null != context)) {
                        var updateUri: Uri = ContentUris.withAppendedId(MsgChatContactsContentProvider.CONTENT_URI, ids[0])
                        context.getContentResolver()
                    }
                    return (ids[0] as Int)
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            CONTACTS_DATA_ITEM_TYPE, CONTACTS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    var rows: Int = databaseHelper.update(TABLE_MSGCHATCONTACTS, values, selection, selectionArgs)
                    if ((0 == rows)) {
                        return 0
                    }
                    var context: Context = getContext()
                    if ((null != context)) {
                        var updateUri: Uri = ContentUris.withAppendedId(MsgChatContactsContentProvider.CONTENT_URI, rows)
                        context.getContentResolver()
                    }
                    return rows
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
    override fun delete(uri: Uri, selection: String, selectionArgs: Array<String>): Int {
        when (URI_MATCHER.match(uri)) {
            CONTACTS_DIR_TYPE, CONTACTS_ITEM_TYPE, CONTACTS_DATA_ITEM_TYPE -> {
                try {
                    var id: Int = databaseHelper.delete(TABLE_MSGCHATCONTACTS, selection, selectionArgs)
                    var insertUri: Uri = ContentUris.withAppendedId(MsgChatContactsContentProvider.CONTENT_URI, id)
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
            CONTACTS_JOIN_DIR_TYPE, CONTACTS_JOIN_ITEM_TYPE, CONTACTS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    if (((null == selectionArgs) || (selectionArgs.size < 2))) {
                        return 0
                    }
                    var _id: Long = Long.parseLong(selectionArgs[0])
                    var id: Long = Long.parseLong(selectionArgs[1])
                    var row: Int = (deleteContactThreadContact(databaseHelper, _id, id) as Int)
                    var insertUri: Uri = ContentUris.withAppendedId(MsgChatContactsContentProvider.CONTENT_URI, row)
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return row
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
