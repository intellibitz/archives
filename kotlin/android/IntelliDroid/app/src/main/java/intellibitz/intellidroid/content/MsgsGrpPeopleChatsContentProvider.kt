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
import intellibitz.intellidroid.bean.MessageBean
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.Set

class MsgsGrpPeopleChatsContentProvider : ContentProvider() {

    companion object {
        const val TAG = "MsgsGrpPeopleChatsCP"
        const val TABLE_MSGSGRPPEOPLECHATS = "msgsgrppeoplechats"
        const val CREATE_TABLE_MSGSGRPPEOPLECHATS = "CREATE TABLE " + TABLE_MSGSGRPPEOPLECHATS + MessageItemColumns.MESSAGECHAT_SCHEMA
        const val SCHEME = "content://"
        const val MSGSGRPPEOPLECHATS_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all"
        const val MSGSGRPPEOPLECHATS_JOIN_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join"
        const val MSGSGRPPEOPLECHATS_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id"
        const val MSGSGRPPEOPLECHATS_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id"
        const val MSGSGRPPEOPLECHATS_JOIN_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id"
        const val MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id"
        const val MSGSGRPPEOPLECHATS_RAW_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all"
        const val AUTHORITY = "intellibitz.intellidroid.content.MsgsGrpPeopleChatsContentProvider"
        val CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_MSGSGRPPEOPLECHATS)
        val JOIN_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGSGRPPEOPLECHATS)
        val RAW_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGSGRPPEOPLECHATS)
        private const val MSGSGRPPEOPLECHATS_DIR_TYPE = 1
        private const val MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE = 2
        private const val MSGSGRPPEOPLECHATS_ITEM_TYPE = 3
        private const val MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE = 4
        private const val MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE = 5
        private const val MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE = 6
        private const val MSGSGRPPEOPLECHATS_RAW_DIR_TYPE = 7
        private const val SEARCH_SUGGEST = 8
        private const val REFRESH_SHORTCUT = 9
        private val URI_MATCHER = buildUriMatcher()

        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_MSGSGRPPEOPLECHATS, MSGSGRPPEOPLECHATS_DIR_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGSGRPPEOPLECHATS, MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGSGRPPEOPLECHATS + "/#", MSGSGRPPEOPLECHATS_ITEM_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGSGRPPEOPLECHATS + "/*", MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGSGRPPEOPLECHATS + "/#", MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "join_" + TABLE_MSGSGRPPEOPLECHATS + "/*", MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "raw_" + TABLE_MSGSGRPPEOPLECHATS, MSGSGRPPEOPLECHATS_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT)
            return matcher
        }

        fun createsMessageThreadItemFromCursor(cursor: Cursor): MessageItem {
            val messageItem = MessageItem()
            fillsMessageItemFromCursor(cursor, messageItem)
            return messageItem
        }

        fun fillsMessageItemFromCursor(cursor: Cursor, messageItem: MessageItem) {
            messageItem._id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
            messageItem.threadId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_THREAD_ID))
            messageItem.threadIdRef = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_THREAD_IDREF))
            messageItem.threadIdParts = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_THREAD_IDPARTS))
            messageItem.groupId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_GROUP_ID))
            messageItem.groupIdRef = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_GROUP_IDREF))
            messageItem.intellibitzId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_INTELLIBITZ_ID))
            messageItem.deviceContactId = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_DEVICE_CONTACTID))
            messageItem.group = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_GROUP))
            messageItem.emailItem = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_EMAIL))
            messageItem.anonymous = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_ANONYMOUS))
            messageItem.device = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_DEVICE))
            messageItem.cloud = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_CLOUD))
            messageItem.dataId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATA_ID))
            messageItem.docType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_TYPE))
            messageItem.baseType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_BASE_TYPE))
            messageItem.name = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_NAME))
            messageItem.firstName = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_FIRST_NAME))
            messageItem.lastName = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_LAST_NAME))
            messageItem.displayName = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DISPLAY_NAME))
            messageItem.dataRev = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATA_REV))
            messageItem.type = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TYPE))
            messageItem.toType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO_TYPE))
            messageItem.docOwnerEmail = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_OWNER_EMAIL))
            messageItem.docOwner = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_OWNER))
            messageItem.docSenderEmail = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_SENDER_EMAIL))
            messageItem.fromUid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_FROM_UID))
            messageItem.toUid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO_UID))
            messageItem.toChatUid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO_CHAT_UID))
            messageItem.chatId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_CHAT_ID))
            messageItem.docSender = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_SENDER))
            messageItem.pendingDocs = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_PENDING_DOCS))
            messageItem.unreadCount = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_UNREAD_COUNT))
            messageItem.hasAttachments = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_HAS_ATTACHEMENTS))
            messageItem.subject = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_SUBJECT))
            messageItem.latestMessageText = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE))
            messageItem.latestMessageTimestamp = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE_TS))
            messageItem.read = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_READ))
            messageItem.delivered = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_DELIVERED))
            messageItem.from = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_FROM))
            messageItem.to = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO))
            messageItem.cc = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_CC))
            messageItem.bcc = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_BCC))
            messageItem.timestamp = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_TIMESTAMP))
            messageItem.dateTime = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATETIME))
        }

        fun createChatMessageThread(uid: String, item: MessageItem): MessageItem {
            item.docType = "THREAD"
            item.type = "CHAT"
            item.toType = "USER"
            item.chatId = uid
            item.dataRev = "1"
            item.hasAttachments = 0
            item.latestMessageText = ""
            item.timestamp = System.currentTimeMillis()
            return item
        }

        fun createChatMessageThread(uid: String, item: MessageBean): MessageBean {
            item.docType = "THREAD"
            item.type = "CHAT"
            item.toType = "USER"
            item.chatId = uid
            item.rev = "1"
            item.hasAttachments = 0
            item.latestMessageText = ""
            item.timestamp = System.currentTimeMillis()
            return item
        }

        @Throws(IOException::class)
        fun saveMessageThreadItemToDB(messageItem: MessageItem, context: Context): Uri {
            val msgThreadContentValues = ContentValues()
            msgThreadContentValues.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItem))
            val uri = context.applicationContext.contentResolver.insert(Uri.withAppendedPath(CONTENT_URI, "0"), msgThreadContentValues)
            Log.e(TAG, "MessageThread saved: $uri")
            return uri
        }

        fun fillMessageThreadItemsFromCursor(cursor: Cursor?): ArrayList<MessageItem> {
            val messageItems = ArrayList<MessageItem>()
            if (null == cursor || 0 == cursor.count) return messageItems
            Log.e(TAG, "fillMessageItemsFromCursor: count - " + cursor.count)
            do {
                val messageItem = createsMessageThreadItemFromCursor(cursor)
                messageItems.add(messageItem)
            } while (cursor.moveToNext())
            return messageItems
        }

        @Throws(IOException::class)
        fun deleteMsgs(item: Array<String>, context: Context): Int {
            return context.applicationContext.contentResolver.delete(JOIN_CONTENT_URI, null, item)
        }

        @Throws(JSONException::class)
        fun toJson(messageItem: MessageItem?, uid: String, token: String, device: String, deviceRef: String): JSONObject? {
            if (null == messageItem) return null
            var messageType = messageItem.messageType
            if (null == messageType) messageType = messageItem.type
            if ("CHAT".equals(messageType, ignoreCase = true)) {
                return toChatJson(messageItem, uid, token, device, deviceRef)
            } else if ("EMAIL".equals(messageType, ignoreCase = true)) {
                return toEmailJson(messageItem, uid, token, device, deviceRef)
            }
            return null
        }

        @Throws(JSONException::class)
        fun toChatJson(messageItem: MessageItem, uid: String, token: String, device: String, deviceRef: String): JSONObject {
            val jsonObject = JSONObject()
            jsonObject.put("msg_type", "CHAT")
            jsonObject.put("doc_type", messageItem.docType)
            jsonObject.put("chat_id", messageItem.chatId)
            jsonObject.put("from_uid", messageItem.fromUid)
            jsonObject.put("from_name", messageItem.fromName)
            jsonObject.put("doc_owner", messageItem.docOwner)
            jsonObject.put("to_uid", messageItem.chatId)
            jsonObject.put("to_type", messageItem.toType)
            jsonObject.put("client_msg_ref", messageItem.chatMsgRef)
            jsonObject.put("txt", messageItem.text)
            val items = messageItem.attachments
            if (items != null && !items.isEmpty()) {
                val attachments = JSONArray()
                for (item in items) {
                    try {
                        val attachmentDetails = HttpUrlConnectionParser.uploadAttachments(item, MainApplicationSingleton.ATTACHMENT_UPLOAD_URL, uid, device, deviceRef, token)
                        attachments.put(attachmentDetails)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                jsonObject.put("attachments", attachments)
            }
            return jsonObject
        }

        @Throws(JSONException::class)
        fun toEmailJson(messageItem: MessageItem, uid: String, token: String, device: String, deviceRef: String): JSONObject {
            val jsonObject = JSONObject()
            jsonObject.put("msg_type", "EMAIL")
            jsonObject.put("to_uid", messageItem.chatId)
            jsonObject.put("client_msg_ref", messageItem.chatMsgRef)
            jsonObject.put("txt", messageItem.text)
            val items = messageItem.attachments
            if (items != null && !items.isEmpty()) {
                val attachments = JSONArray()
                for (item in items) {
                    try {
                        val attachmentDetails = HttpUrlConnectionParser.uploadAttachments(item, MainApplicationSingleton.ATTACHMENT_UPLOAD_URL, uid, device, deviceRef, token)
                        attachments.put(attachmentDetails)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                jsonObject.put("attachments", attachments)
            }
            jsonObject.put("email", messageItem.fromEmail)
            jsonObject.put("from", messageItem.from + " <" + messageItem.fromEmail + ">")
            if (TextUtils.isEmpty(messageItem.to)) {
                messageItem.to = MainApplicationSingleton.DUMMY_EMAIL
            }
            jsonObject.put("to", messageItem.to)
            jsonObject.put("cc", messageItem.cc)
            jsonObject.put("bcc", messageItem.bcc)
            jsonObject.put("subject", messageItem.subject)
            return jsonObject
        }

        fun saveNestsInDB(messageItems: Collection<MessageItem>, context: Context): Uri? {
            try {
                val contentValues = ContentValues()
                contentValues.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItems))
                return context.contentResolver.insert(CONTENT_URI, contentValues)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        @Throws(JSONException::class)
        fun fillNestItemFromJSON(jsonObject: JSONObject, nestItem: MessageItem): MessageItem {
            val name = jsonObject.optString("name")
            if (!TextUtils.isEmpty(name)) nestItem.name = name
            val code = jsonObject.optString("code")
            if (!TextUtils.isEmpty(code)) {
                nestItem.folderCode = code
                nestItem.dataId = nestItem.folderCode
            }
            val default_folder = jsonObject.optBoolean("default_folder")
            nestItem.defaultFolder = if (default_folder) 1 else 0
            val stack = jsonObject.optString("stack_game")
            if (!TextUtils.isEmpty(stack)) nestItem.msgRef = stack
            return nestItem
        }

        fun fillNestItemFromJSONArray(nests: JSONArray): List<MessageItem> {
            val len = nests.length()
            val messageItems = ArrayList<MessageItem>(len)
            if (0 == len) return messageItems
            for (i in 0 until len) {
                try {
                    val jsonObject = nests.getJSONObject(i)
                    val messageItem = MessageItem()
                    messageItem.setNest()
                    fillNestItemFromJSON(jsonObject, messageItem)
                    messageItems.add(messageItem)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
                }
            }
            return messageItems
        }

        fun createOrUpdateMsgsGrpPeopleChats(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messageItem: MessageItem): Long {
            if (null == messageItem) return 0
            val contacts = messageItem.contactItem
            if (null == contacts) return 0
            val messages = messageItem.cloneShal() as MessageItem
            messages._id = 0
            savesMsgsGrpPeopleChats(databaseHelper, db, messages, contacts)
            return messageItem._id
        }

        fun savesMsgsGrpPeopleChats(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messages: MessageItem, contactItem: ContactItem) {
            val did = contactItem.deviceContactId
            val intellibitzId = contactItem.intellibitzId
            val deviceContactId = did.toString()
            var devId = intellibitzId
            val values = ContentValues()
            messages.deviceContactId = did
            messages.intellibitzId = intellibitzId
            messages.typeId = contactItem.typeId
            messages.device = contactItem.isDevice()
            messages.group = contactItem.isGroup()
            messages.emailItem = contactItem.isEmailItem()
            messages.firstName = contactItem.firstName
            messages.lastName = contactItem.lastName
            messages.displayName = contactItem.displayName
            fillContentValuesFromMessageItem(messages, values)
            var cursor: Cursor?
            var colName = MessageItemColumns.KEY_INTELLIBITZ_ID
            if (0 == did) {
                cursor = databaseHelper.query(db, TABLE_MSGSGRPPEOPLECHATS, arrayOf(MessageItemColumns.KEY_ID), colName + " = ?", arrayOf(devId), null)
            } else {
                colName = MessageItemColumns.KEY_DEVICE_CONTACTID
                devId = deviceContactId
                cursor = databaseHelper.query(db, TABLE_MSGSGRPPEOPLECHATS, arrayOf(MessageItemColumns.KEY_ID), colName + " = ?", arrayOf(devId), null)
            }
            if (null == cursor || 0 == cursor.count) {
                cursor?.close()
                databaseHelper.insert(db, TABLE_MSGSGRPPEOPLECHATS, null, values)
            } else {
                cursor.close()
                databaseHelper.update(db, TABLE_MSGSGRPPEOPLECHATS, values, colName + " = ?", arrayOf(devId))
            }
        }

        fun fillContentValuesFromMessageItem(messageItem: MessageItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_ID, messageItem.threadId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_IDREF, messageItem.threadIdRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_IDPARTS, messageItem.threadIdParts)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_GROUP_ID, messageItem.groupId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_GROUP_IDREF, messageItem.groupIdRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DEVICE_CONTACTID, messageItem.deviceContactId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_INTELLIBITZ_ID, messageItem.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATA_ID, messageItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CHAT_ID, messageItem.chatId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_UID, messageItem.toUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_CHAT_UID, messageItem.toChatUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATA_REV, messageItem.dataRev)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_NAME, messageItem.name)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FIRST_NAME, messageItem.firstName)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LAST_NAME, messageItem.lastName)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DISPLAY_NAME, messageItem.displayName)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FOLDER_CODE, messageItem.folderCode)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DEFAULT_FOLDER, messageItem.defaultFolder)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TYPE, messageItem.type)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_TYPE, messageItem.toType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_OWNER, messageItem.docOwner)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_OWNER_EMAIL, messageItem.docOwnerEmail)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_SENDER, messageItem.docSender)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_SENDER_EMAIL, messageItem.docSenderEmail)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FROM_UID, messageItem.fromUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_TYPE, messageItem.docType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_BASE_TYPE, messageItem.baseType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TEXT, messageItem.text)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_HTML, messageItem.html)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_SUBJECT, messageItem.subject)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FROM, messageItem.from)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO, messageItem.to)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CC, messageItem.cc)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_BCC, messageItem.bcc)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_HAS_ATTACHEMENTS, messageItem.hasAttachments())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LATEST_MESSAGE, messageItem.latestMessageText)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LATEST_MESSAGE_TS, messageItem.latestMessageTimestamp)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_READ, messageItem.isRead())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DELIVERED, messageItem.isDelivered())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_GROUP, messageItem.isGroup())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_EMAIL, messageItem.isEmailItem())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_ANONYMOUS, messageItem.isAnonymous())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DEVICE, messageItem.isDevice())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_CLOUD, messageItem.isCloud())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_UNREAD_COUNT, messageItem.unreadCount)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TIMESTAMP, messageItem.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATETIME, messageItem.dateTime)
            return values
        }
    }

    private var databaseHelper: DatabaseHelper? = null

    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(context, DatabaseHelper.DATABASE_NAME)
        return true
    }

    override fun getType(uri: Uri): String? {
        return when (URI_MATCHER.match(uri)) {
            MSGSGRPPEOPLECHATS_DIR_TYPE -> MSGSGRPPEOPLECHATS_DIR_MIME_TYPE
            MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE -> MSGSGRPPEOPLECHATS_JOIN_DIR_MIME_TYPE
            MSGSGRPPEOPLECHATS_ITEM_TYPE -> MSGSGRPPEOPLECHATS_ITEM_MIME_TYPE
            MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE -> MSGSGRPPEOPLECHATS_DATA_ITEM_MIME_TYPE
            MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE -> MSGSGRPPEOPLECHATS_JOIN_ITEM_MIME_TYPE
            MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE -> MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_MIME_TYPE
            MSGSGRPPEOPLECHATS_RAW_DIR_TYPE -> MSGSGRPPEOPLECHATS_RAW_DIR_MIME_TYPE
            SEARCH_SUGGEST -> SearchManager.SUGGEST_MIME_TYPE
            REFRESH_SHORTCUT -> SearchManager.SHORTCUT_MIME_TYPE
            else -> throw IllegalArgumentException("Unknown URL $uri")
        }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var cursor: Cursor? = null
        when (URI_MATCHER.match(uri)) {
            MSGSGRPPEOPLECHATS_DIR_TYPE, MSGSGRPPEOPLECHATS_ITEM_TYPE, MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE, MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE -> try {
                cursor = databaseHelper?.query(TABLE_MSGSGRPPEOPLECHATS, projection, selection, selectionArgs, sortOrder)
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
            MSGSGRPPEOPLECHATS_RAW_DIR_TYPE -> try {
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
        val vals = values?.getAsByteArray(MessageItem.TAG)
        when (URI_MATCHER.match(uri)) {
            MSGSGRPPEOPLECHATS_DIR_TYPE, MSGSGRPPEOPLECHATS_ITEM_TYPE, MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE, MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE -> try {
                val id = databaseHelper?.insert(TABLE_MSGSGRPPEOPLECHATS, null, values) ?: 0
                val insertUri = ContentUris.withAppendedId(CONTENT_URI, id)
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(insertUri, null)
                }
                return insertUri
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return uri
    }

    override fun update(uri: Uri, values: ContentValues?, where: String?, whereArgs: Array<String>?): Int {
        var id = 0
        var updateUri: Uri? = null
        when (URI_MATCHER.match(uri)) {
            MSGSGRPPEOPLECHATS_DIR_TYPE, MSGSGRPPEOPLECHATS_ITEM_TYPE, MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE, MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE -> try {
                id = databaseHelper?.update(TABLE_MSGSGRPPEOPLECHATS, values, where, whereArgs) ?: 0
                updateUri = ContentUris.withAppendedId(CONTENT_URI, id.toLong())
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(updateUri, null)
                }
                return id
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return 0
    }

    override fun delete(uri: Uri, where: String?, whereArgs: Array<String>?): Int {
        var id = 0
        var delUri: Uri? = null
        when (URI_MATCHER.match(uri)) {
            MSGSGRPPEOPLECHATS_DIR_TYPE, MSGSGRPPEOPLECHATS_ITEM_TYPE, MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE, MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE, MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE -> try {
                id = databaseHelper?.delete(TABLE_MSGSGRPPEOPLECHATS, where, whereArgs) ?: 0
                delUri = ContentUris.withAppendedId(CONTENT_URI, id.toLong())
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(delUri, null)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return id
    }

    fun getOpenHelperForTest(): DatabaseHelper? {
        return databaseHelper
    }
}
