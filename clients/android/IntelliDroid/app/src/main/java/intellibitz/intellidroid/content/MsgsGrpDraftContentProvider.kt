

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
import intellibitz.intellidroid.data.BaseItem
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.ContactsContactJoinColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.db.MessagesContactsJoinColumns
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
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import intellibitz.intellidroid.content.MessageChatContentProvider.createsChatMessageItemFromJSON
import intellibitz.intellidroid.content.MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN
import intellibitz.intellidroid.content.MessageEmailContentProvider.createsEmailMessageItemFromJSON

class MsgsGrpDraftContentProvider : ContentProvider() {
    companion object {
        const val TAG = "MsgsGrpDraftCP"
        const val TABLE_MSGSGRPDRAFT = "msgsgrpdraft"
        const val TABLE_MSGSGRPDRAFT_MESSAGES_JOIN = "msgsgrpdraft_messages"
        const val CREATE_TABLE_MSGSGRPDRAFT = (("CREATE TABLE " + TABLE_MSGSGRPDRAFT) + MessageItemColumns.MESSAGECHAT_SCHEMA)
        const val SCHEME = "content://"
        const val MSGSGRPDRAFT_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all")
        const val MSGSGRPDRAFT_JOIN_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join")
        const val MSGSGRPDRAFT_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id")
        const val MSGSGRPDRAFT_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id")
        const val MSGSGRPDRAFT_JOIN_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id")
        const val MSGSGRPDRAFT_JOIN_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id")
        const val MSGSGRPDRAFT_RAW_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all")
        private const val MSGSGRPDRAFT_DIR_TYPE = 1
        private const val MSGSGRPDRAFT_JOIN_DIR_TYPE = 2
        private const val MSGSGRPDRAFT_ITEM_TYPE = 3
        private const val MSGSGRPDRAFT_DATA_ITEM_TYPE = 4
        private const val MSGSGRPDRAFT_JOIN_ITEM_TYPE = 5
        private const val MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE = 6
        private const val MSGSGRPDRAFT_RAW_DIR_TYPE = 7
        private const val SEARCH_SUGGEST = 8
        private const val REFRESH_SHORTCUT = 9
        var AUTHORITY: String = "intellibitz.intellidroid.content.MsgsGrpDraftContentProvider"
        val CONTENT_URI: Uri = Uri.parse((((SCHEME + AUTHORITY) + "/") + TABLE_MSGSGRPDRAFT))
        val JOIN_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "join_") + TABLE_MSGSGRPDRAFT))
        val RAW_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "raw_") + TABLE_MSGSGRPDRAFT))
        private val URI_MATCHER: UriMatcher = buildUriMatcher()
        private fun buildUriMatcher(): UriMatcher {
            var matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_MSGSGRPDRAFT, MSGSGRPDRAFT_DIR_TYPE)
            matcher.addURI(AUTHORITY, ("join_" + TABLE_MSGSGRPDRAFT), MSGSGRPDRAFT_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MSGSGRPDRAFT + "/#"), MSGSGRPDRAFT_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MSGSGRPDRAFT + "/*"), MSGSGRPDRAFT_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MSGSGRPDRAFT) + "/#"), MSGSGRPDRAFT_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MSGSGRPDRAFT) + "/*"), MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("raw_" + TABLE_MSGSGRPDRAFT), MSGSGRPDRAFT_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_QUERY + "/*"), SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*"), REFRESH_SHORTCUT)
            return matcher
        }
        @Throws(JSONException::class, IOException::class)
        fun savesMsgsGrpDraftFromJSON(jsonObject: JSONObject, user: ContactItem, context: Context): MessageItem {
            var msgType: String = jsonObject.optString("msg_type")
            if (TextUtils.isEmpty(msgType)) {
                return null
            }
            var messageItem: MessageItem = null
            if ("EMAIL") {
                messageItem = MessageEmailContentProvider.createsEmailMessageItemFromJSON(jsonObject, user)
            }
            else {
                if ("CHAT") {
                    messageItem = createsChatMessageItemFromJSON(jsonObject, user)
                }
            }
            return savesMsgsGrpDraft(messageItem, context)
        }
        fun savesMsgsGrpDraft(messageItem: MessageItem, context: Context): MessageItem {
            if ((null == messageItem)) {
                return null
            }
            messageItem.threadId = messageItem.dataId
            messageItem.dataId = messageItem.chatId
            messageItem.latestMessageText = messageItem.getText()
            messageItem.latestMessageTimestamp = messageItem.timestamp
            messageItem.docSender = messageItem.fromName
            var values: ContentValues = ContentValues()
            MessageEmailContentProvider.fillContentValuesFromMessageItem(messageItem, values)
            var cursor: Cursor = context.getContentResolver()
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                context.getContentResolver()
            }
            else {
                cursor.close()
                context.getContentResolver()
            }
            return messageItem
        }
        fun createsMsgsGrpDraftFromCursor(cursor: Cursor): MessageItem {
            var messageItem: MessageItem = MessageItem()
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
        fun createDemoMessageThread(email: String): MessageItem {
            var messageItem: MessageItem = MessageItem()
            var contactItem: ContactItem = ContactItem()
            messageItem.contactItem = contactItem
            messageItem.docType = "THREAD"
            messageItem.type = "CHAT"
            messageItem.dataId = "aktprototype"
            messageItem.dataRev = "1"
            messageItem.docOwner = "DEMO"
            messageItem.docSender = "DEMO"
            messageItem.docOwnerEmail = email
            messageItem.docSenderEmail = email
            messageItem.subject = "DEMO"
            messageItem.hasAttachments = 1
            var from: ContactItem = ContactItem()
            from.dataId = email
            from.typeId = email
            from.intellibitzId = email
            from.name = "DEMO"
            from.type = "from"
            contactItem.addContact(from)
            var to: ContactItem = ContactItem()
            to.dataId = "nishanth@intellibitz.com"
            to.typeId = "nishanth@intellibitz.com"
            to.intellibitzId = "nishanth@intellibitz.com"
            to.name = "DEMO"
            to.type = "to"
            contactItem.addContact(to)
            var cc: ContactItem = ContactItem()
            cc.dataId = "jeff@intellibitz.com"
            cc.typeId = "jeff@intellibitz.com"
            cc.intellibitzId = "jeff@intellibitz.com"
            cc.name = "DEMO"
            cc.type = "to"
            contactItem.addContact(cc)
            messageItem.timestamp = System.currentTimeMillis()
            return messageItem
        }
        fun createChatMessageThread(uid: String, item: MessageItem): MessageItem {
            item.docType = "THREAD"
            item.type = "CHAT"
            item.toType = "USER"
            item.chatId = uid
            item.toUid = uid
            item.toChatUid = uid
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
            item.toUid = uid
            item.toChatUid = uid
            item.rev = "1"
            item.hasAttachments = 0
            item.latestMessageText = ""
            item.timestamp = System.currentTimeMillis()
            return item
        }
        fun createMessagesFromMessage(messageItem: MessageItem, user: ContactItem): MessageItem {
            var messageThreadItem: MessageItem = MessageItem()
            messageThreadItem.baseType = "THREAD"
            messageThreadItem.docType = "THREAD"
            return fillMessagesFromMessage(messageThreadItem, messageItem, user)
        }
        fun fillMessagesFromMessage(messageThreadItem: MessageItem, messageItem: MessageItem, user: ContactItem): MessageItem {
            messageThreadItem.dataRev = messageItem.dataRev
            messageThreadItem.baseType = messageItem.baseType
            messageThreadItem.docType = messageItem.docType
            var chatId: String = messageItem.chatId
            if ((null == chatId)) {
                chatId = messageItem.dataId
            }
            messageThreadItem.chatId = messageItem.chatId
            var toUid: String = messageItem.toUid
            if ((null == toUid)) {
                toUid = messageItem.dataId
            }
            messageThreadItem.toUid = toUid
            if (("CHAT" || "CHAT")) {
                messageThreadItem.dataId = chatId
                messageThreadItem.threadId = chatId
                messageThreadItem.threadIdRef = chatId
                messageItem.threadId = chatId
                messageItem.threadIdRef = chatId
            }
            else {
                messageThreadItem.dataId = toUid
                messageThreadItem.threadId = toUid
                messageThreadItem.threadIdRef = toUid
                messageItem.threadId = toUid
                messageItem.threadIdRef = toUid
            }
            messageThreadItem.chatId = messageThreadItem.dataId
            messageThreadItem.toChatUid = messageThreadItem.dataId
            messageThreadItem.timestamp = messageItem.timestamp
            messageThreadItem.type = messageItem.messageType
            messageThreadItem.toType = messageItem.toType
            messageThreadItem.name = messageItem.name
            messageThreadItem.subject = messageItem.subject
            messageThreadItem.docOwner = messageItem.docOwner
            messageThreadItem.docSender = messageItem.docSender
            messageThreadItem.docOwnerEmail = messageItem.docOwnerEmail
            messageThreadItem.docSenderEmail = messageItem.docSenderEmail
            messageThreadItem.fromUid = messageItem.fromUid
            messageThreadItem.hasAttachments = messageItem.attachments
            messageThreadItem.read = messageItem.isRead()
            messageThreadItem.delivered = messageItem.isDelivered()
            if ((!messageItem.isRead() && !user.dataId)) {
                messageThreadItem.unreadCount = (messageThreadItem.unreadCount + 1)
            }
            var ts: Long = messageItem.timestamp
            var mts: Long = messageThreadItem.latestMessageTimestamp
            if ((ts > mts)) {
                messageThreadItem.latestMessageText = messageItem.getText()
                messageThreadItem.latestMessageTimestamp = ts
            }
            var dt: String = messageItem.dateTime
            if ((dt != null)) {
                var mdt: String = messageThreadItem.dateTime
                if ((null == mdt)) {
                    messageThreadItem.dateTime = dt
                }
                else {
                    var dts: Long = MainApplicationSingleton.getDateTimeMillisISO(dt)
                    var mdts: Long = MainApplicationSingleton.getDateTimeMillisISO(mdt)
                    if (((0 == mdts) || (dts < mdts))) {
                        messageThreadItem.dateTime = dt
                    }
                }
            }
            messageThreadItem.from = messageItem.fromEmail
            messageThreadItem.to = messageItem.to
            messageThreadItem.cc = messageItem.cc
            messageThreadItem.bcc = messageItem.bcc
            if (("CHAT" || "CHAT")) {
                messageThreadItem.contactItem = messageItem.contactItem
            }
            else {
                messageThreadItem.contactItem = messageItem.contactItem
                messageThreadItem.compose()
            }
            return messageThreadItem
        }
        @Throws(IOException::class)
        fun saveMessageThreadItemToDB(messageItem: MessageItem, context: Context): Uri {
            var msgThreadContentValues: ContentValues = ContentValues()
            msgThreadContentValues.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItem))
            var uri: Uri = context.getApplicationContext()
            Log.e(TAG, ("MessageThread saved: " + uri))
            return uri
        }
        fun fillMessageItemsFromCursor(cursor: Cursor): List<MessageItem> {
            var messageItems: List<MessageItem> = ArrayList()
            if (((null == cursor) || (0 == cursor.getCount()))) {
                return messageItems
            }
            Log.e(TAG, ("fillMessageItemsFromCursor: count - " + cursor.getCount()))
            do {
                var messageItem: MessageItem = createsMsgsGrpDraftFromCursor(cursor)
                messageItems.add(messageItem)
            } while (cursor.moveToNext())
            return messageItems
        }
        @Throws(IOException::class)
        fun deleteMsgs(item: Array<String>, context: Context): Int {
            return context.getApplicationContext()
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
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TYPE, messageItem.getType())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_TYPE, messageItem.toType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_OWNER, messageItem.docOwner)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_OWNER_EMAIL, messageItem.docOwnerEmail)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_SENDER, messageItem.docSender)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_SENDER_EMAIL, messageItem.docSenderEmail)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FROM_UID, messageItem.fromUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_TYPE, messageItem.docType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_BASE_TYPE, messageItem.baseType)
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
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_GROUP, messageItem.group)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_EMAIL, messageItem.emailItem)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_ANONYMOUS, messageItem.anonymous)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DEVICE, messageItem.device)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_CLOUD, messageItem.cloud)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_UNREAD_COUNT, messageItem.unreadCount)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TIMESTAMP, messageItem.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATETIME, messageItem.dateTime)
            return values
        }
        @Throws(JSONException::class)
        fun toJson(messageItem: MessageItem, uid: String, token: String, device: String, deviceRef: String): JSONObject {
            if ((null == messageItem)) {
                return null
            }
            var messageType: String = messageItem.messageType
            if ((null == messageType)) {
                messageType = messageItem.getType()
            }
            if ("CHAT") {
                return toChatJson(messageItem, uid, token, device, deviceRef)
            }
            else {
                if ("EMAIL") {
                    return toEmailJson(messageItem, uid, token, device, deviceRef)
                }
            }
            return null
        }
        @Throws(JSONException::class)
        fun toChatJson(messageItem: MessageItem, uid: String, token: String, device: String, deviceRef: String): JSONObject {
            var jsonObject: JSONObject = JSONObject()
            jsonObject.put("msg_type", "CHAT")
            jsonObject.put("doc_type", messageItem.docType)
            jsonObject.put("chat_id", messageItem.chatId)
            jsonObject.put("from_uid", messageItem.fromUid)
            jsonObject.put("from_name", messageItem.fromName)
            jsonObject.put("doc_owner", messageItem.docOwner)
            jsonObject.put("to_uid", messageItem.chatId)
            jsonObject.put("to_type", messageItem.toType)
            jsonObject.put("client_msg_ref", messageItem.chatMsgRef)
            jsonObject.put("txt", messageItem.getText())
            var items: Set<MessageItem> = messageItem.attachments
            if (((items != null) && !items.empty)) {
                var attachments: JSONArray = JSONArray()
                for (item in items) {
                    try {
                        var attachmentDetails: JSONObject = HttpUrlConnectionParser.uploadAttachments(item, MainApplicationSingleton.ATTACHMENT_UPLOAD_URL, uid, device, deviceRef, token)
                        attachments.put(attachmentDetails)
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                jsonObject.put("attachments", attachments)
            }
            return jsonObject
        }
        @Throws(JSONException::class)
        fun toEmailJson(messageItem: MessageItem, uid: String, token: String, device: String, deviceRef: String): JSONObject {
            var jsonObject: JSONObject = JSONObject()
            jsonObject.put("msg_type", "EMAIL")
            jsonObject.put("to_uid", messageItem.chatId)
            jsonObject.put("client_msg_ref", messageItem.chatMsgRef)
            jsonObject.put("txt", messageItem.getText())
            var items: Set<MessageItem> = messageItem.attachments
            if (((items != null) && !items.empty)) {
                var attachments: JSONArray = JSONArray()
                for (item in items) {
                    try {
                        var attachmentDetails: JSONObject = HttpUrlConnectionParser.uploadAttachments(item, MainApplicationSingleton.ATTACHMENT_UPLOAD_URL, uid, device, deviceRef, token)
                        attachments.put(attachmentDetails)
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                jsonObject.put("attachments", attachments)
            }
            jsonObject.put("email", messageItem.fromEmail)
            jsonObject.put("from", (((messageItem.from + " <") + messageItem.fromEmail) + ">"))
            if (TextUtils.isEmpty(messageItem.to)) {
                messageItem.to = MainApplicationSingleton.DUMMY_EMAIL
            }
            jsonObject.put("to", messageItem.to)
            jsonObject.put("cc", messageItem.cc)
            jsonObject.put("bcc", messageItem.bcc)
            jsonObject.put("subject", messageItem.subject)
            return jsonObject
        }
        fun saveNestsInDB(messageItems: Collection<MessageItem>, context: Context): Uri {
            try {
                var contentValues: ContentValues = ContentValues()
                contentValues.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItems))
                return context.getContentResolver()
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        fun getMessageThreadJoin(databaseHelper: DatabaseHelper, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
            var selectQuery: String = ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("SELECT  mt." + MessageItemColumns.KEY_ID) + " as mt_id, ") + " mt.") + MessageItemColumns.KEY_THREAD_ID) + " as mthid, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDREF) + " as mthidref, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDPARTS) + " as mthidparts, ") + " mt.") + MessageItemColumns.KEY_GROUP_ID) + " as mtgid, ") + " mt.") + MessageItemColumns.KEY_GROUP_IDREF) + " as mtgidref, ") + " mt.") + MessageItemColumns.KEY_INTELLIBITZ_ID) + " as mtclutid, ") + " mt.") + MessageItemColumns.KEY_IS_GROUP) + " as mtisgroup, ") + " mt.") + MessageItemColumns.KEY_IS_EMAIL) + " as mtisemail, ") + " mt.") + MessageItemColumns.KEY_IS_ANONYMOUS) + " as mtisanon, ") + " mt.") + MessageItemColumns.KEY_IS_DEVICE) + " as mtisdev, ") + " mt.") + MessageItemColumns.KEY_IS_CLOUD) + " as mtiscloud, ") + " mt.") + MessageItemColumns.KEY_FIRST_NAME) + " as mtfirst, ") + " mt.") + MessageItemColumns.KEY_LAST_NAME) + " as mtlast, ") + " mt.") + MessageItemColumns.KEY_DISPLAY_NAME) + " as mtdisplay, ") + " mt.") + MessageItemColumns.KEY_DEVICE_CONTACTID) + " as mtdevcid, ") + " mt.") + MessageItemColumns.KEY_DATA_ID) + " as mtid, ") + " mt.") + MessageItemColumns.KEY_TYPE) + " as mttype, ") + " mt.") + MessageItemColumns.KEY_TO_TYPE) + " as mttotype, ") + " mt.") + MessageItemColumns.KEY_DATA_REV) + " as mtrev, ") + " mt.") + MessageItemColumns.KEY_NAME) + " as mtname, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER) + " as mtdo, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER) + " as mtds, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mtdoe, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mtdse, ") + " mt.") + MessageItemColumns.KEY_FROM_UID) + " as mtfuid, ") + " mt.") + MessageItemColumns.KEY_TO_UID) + " as mttuid, ") + " mt.") + MessageItemColumns.KEY_TO_CHAT_UID) + " as mttcuid, ") + " mt.") + MessageItemColumns.KEY_CHAT_ID) + " as mtcuid, ") + " mt.") + MessageItemColumns.KEY_DOC_TYPE) + " as mtdoct, ") + " mt.") + MessageItemColumns.KEY_BASE_TYPE) + " as mtbaset, ") + " mt.") + MessageItemColumns.KEY_SUBJECT) + " as mtsub, ") + " mt.") + MessageItemColumns.KEY_FROM) + " as mtfrom, ") + " mt.") + MessageItemColumns.KEY_TO) + " as mtto, ") + " mt.") + MessageItemColumns.KEY_CC) + " as mtcc, ") + " mt.") + MessageItemColumns.KEY_BCC) + " as mtbcc, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE) + " as mtlate, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE_TS) + " as mtlatets, ") + " mt.") + MessageItemColumns.KEY_IS_READ) + " as mtisr, ") + " mt.") + MessageItemColumns.KEY_IS_DELIVERED) + " as mtisd, ") + " mt.") + MessageItemColumns.KEY_IS_FLAGGED) + " as mtisf, ") + " mt.") + MessageItemColumns.KEY_UNREAD_COUNT) + " as mtuc, ") + " mt.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mtpd, ") + " mt.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mtha, ") + " mt.") + MessageItemColumns.KEY_TIMESTAMP) + " as mttime, ") + " mt.") + MessageItemColumns.KEY_DATETIME) + " as mtdtime, ") + " m.") + MessageItemColumns.KEY_ID) + " as m_id,") + " m.") + MessageItemColumns.KEY_DATA_ID) + " as mid,") + " m.") + MessageItemColumns.KEY_FROM_NAME) + " as mfn,") + " m.") + MessageItemColumns.KEY_FROM_UID) + " as mfuid,") + " m.") + MessageItemColumns.KEY_TEXT) + " as mtxt,") + " m.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mdoe,") + " m.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mdse,") + " m.") + MessageItemColumns.KEY_MESSAGE_DIRECTION) + " as mdir,") + " m.") + MessageItemColumns.KEY_MESSAGE_ATTACH_ID) + " as maid,") + " m.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mpd,") + " m.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mha,") + " m.") + MessageItemColumns.KEY_IS_READ) + " as misr,") + " m.") + MessageItemColumns.KEY_IS_DELIVERED) + " as misd,") + " m.") + MessageItemColumns.KEY_IS_FLAGGED) + " as misf,") + " m.") + MessageItemColumns.KEY_TIMESTAMP) + " as mtime,") + " gc._id as gc_id, gc.id as gcid, gc.name as gcname,") + " gc.profile_pic as gcpic, gc.type as gctype, ") + " ak._id as ak_id, ak.id as akid, ak.name as akname, ") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN) + " gcak on gc.[_id] = gcak.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT) + " ak on ak.[_id] = gcak.[") + ContactsContactJoinColumns.KEY_CONTACT_ID) + "] ") + "left outer join ") + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN) + " mte on ak.[_id] = mte.[contact_id] ") + "left outer join ") + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT) + " e on e.[_id] = mte.[intellibitzcontact_id] ") + "left outer join ") + TABLE_MSGSGRPDRAFT_MESSAGES_JOIN) + " mtm on mt.[_id] = mtm.[msg_thread_id] ") + "left outer join ") + TABLE_MSGSGRPDRAFT) + " m on m.[_id] = mtm.[msg_id] ") + "left outer join ") + MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN) + " ma on m.[_id] = ma.[msg_id] ") + "left outer join ") + MsgChatAttachmentContentProvider.TABLE_MSGCHATATTACHMENT) + " a on a.[_id] = ma.[attachment_id] ")
            if (((selection != null) && !selection.empty)) {
                selectQuery += (" WHERE " + selection)
            }
            if (((sortOrder != null) && !sortOrder.empty)) {
                selectQuery += (" ORDER BY " + sortOrder)
            }
            return databaseHelper.rawQuery(selectQuery, selectionArgs)
        }
        fun getMessageThreadShalGroupJoin(databaseHelper: DatabaseHelper, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
            var selectQuery: String = (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("SELECT  mt." + MessageItemColumns.KEY_ID) + " as mt_id, ") + " mt.") + MessageItemColumns.KEY_THREAD_ID) + " as mthid, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDREF) + " as mthidref, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDPARTS) + " as mthidparts, ") + " mt.") + MessageItemColumns.KEY_GROUP_ID) + " as mtgid, ") + " mt.") + MessageItemColumns.KEY_GROUP_IDREF) + " as mtgidref, ") + " mt.") + MessageItemColumns.KEY_INTELLIBITZ_ID) + " as mtclutid, ") + " mt.") + MessageItemColumns.KEY_IS_GROUP) + " as mtisgroup, ") + " mt.") + MessageItemColumns.KEY_IS_EMAIL) + " as mtisemail, ") + " mt.") + MessageItemColumns.KEY_IS_ANONYMOUS) + " as mtisanon, ") + " mt.") + MessageItemColumns.KEY_IS_DEVICE) + " as mtisdev, ") + " mt.") + MessageItemColumns.KEY_IS_CLOUD) + " as mtiscloud, ") + " mt.") + MessageItemColumns.KEY_FIRST_NAME) + " as mtfirst, ") + " mt.") + MessageItemColumns.KEY_LAST_NAME) + " as mtlast, ") + " mt.") + MessageItemColumns.KEY_DISPLAY_NAME) + " as mtdisplay, ") + " mt.") + MessageItemColumns.KEY_DEVICE_CONTACTID) + " as mtdevcid, ") + " mt.") + MessageItemColumns.KEY_DATA_ID) + " as mtid, ") + " mt.") + MessageItemColumns.KEY_TYPE) + " as mttype, ") + " mt.") + MessageItemColumns.KEY_TO_TYPE) + " as mttotype, ") + " mt.") + MessageItemColumns.KEY_DATA_REV) + " as mtrev, ") + " mt.") + MessageItemColumns.KEY_NAME) + " as mtname, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER) + " as mtdo, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER) + " as mtds, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mtdoe, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mtdse, ") + " mt.") + MessageItemColumns.KEY_FROM_UID) + " as mtfuid, ") + " mt.") + MessageItemColumns.KEY_TO_UID) + " as mttuid, ") + " mt.") + MessageItemColumns.KEY_TO_CHAT_UID) + " as mttcuid, ") + " mt.") + MessageItemColumns.KEY_CHAT_ID) + " as mtcuid, ") + " mt.") + MessageItemColumns.KEY_DOC_TYPE) + " as mtdoct, ") + " mt.") + MessageItemColumns.KEY_BASE_TYPE) + " as mtbaset, ") + " mt.") + MessageItemColumns.KEY_SUBJECT) + " as mtsub, ") + " mt.") + MessageItemColumns.KEY_FROM) + " as mtfrom, ") + " mt.") + MessageItemColumns.KEY_TO) + " as mtto, ") + " mt.") + MessageItemColumns.KEY_CC) + " as mtcc, ") + " mt.") + MessageItemColumns.KEY_BCC) + " as mtbcc, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE) + " as mtlate, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE_TS) + " as mtlatets, ") + " mt.") + MessageItemColumns.KEY_IS_READ) + " as mtisr, ") + " mt.") + MessageItemColumns.KEY_IS_DELIVERED) + " as mtisd, ") + " mt.") + MessageItemColumns.KEY_IS_FLAGGED) + " as mtisf, ") + " mt.") + MessageItemColumns.KEY_UNREAD_COUNT) + " as mtuc, ") + " mt.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mtpd, ") + " mt.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mtha, ") + " mt.") + MessageItemColumns.KEY_TIMESTAMP) + " as mttime, ") + " mt.") + MessageItemColumns.KEY_DATETIME) + " as mtdtime, ") + " gc._id as gc_id, gc.id as gcid, gc.name as gcname,") + " gc.profile_pic as gcpic, gc.type as gctype, ") + " cc.profile_pic as ccpic") + "  FROM  ") + TABLE_MSGSGRPDRAFT) + " mt ") + "left outer join ") + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN) + " mtg on mt.[_id] = mtg.[msgthread_id] ") + "left outer join ") + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS) + " gc on gc.[_id] = mtg.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT) + " cc on cc.[") + ContactItemColumns.KEY_INTELLIBITZ_ID) + "] = mt.[") + MessageItemColumns.KEY_CHAT_ID) + "] ")
            if (((selection != null) && !selection.empty)) {
                selectQuery += (" WHERE " + selection)
            }
            if (((sortOrder != null) && !sortOrder.empty)) {
                selectQuery += (" ORDER BY " + sortOrder)
            }
            return databaseHelper.rawQuery(selectQuery, selectionArgs)
        }
        fun createOrUpdateMessages(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messageItem: MessageItem): Long {
            var cursor: Cursor = databaseHelper.query(db, MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT, arrayOf(MessageItemColumns.KEY_ID, MessageItemColumns.KEY_LATEST_MESSAGE, MessageItemColumns.KEY_LATEST_MESSAGE_TS, MessageItemColumns.KEY_DOC_SENDER), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(messageItem.dataId), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                var values: ContentValues = ContentValues()
                MsgsGrpDraftContentProvider.fillContentValuesFromMessageItem(messageItem, values)
                var _id: Long = databaseHelper.insert(db, MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT, null, values)
                messageItem._id = _id
            }
            else {
                var _id: Long = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                messageItem._id = _id
                var msg: String = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE))
                var sender: String = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_SENDER))
                cursor.close()
                var values: ContentValues = ContentValues()
                MsgsGrpDraftContentProvider.fillContentValuesFromMessageItem(messageItem, values)
                databaseHelper.update(db, MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT, values, (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(messageItem.dataId)))
            }
            var ctidCol: String = MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN, arrayOf(ctidCol), (MessageItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(messageItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                ctidCol = ContactItemColumns.KEY_ID
                cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS, arrayOf(ctidCol), (ContactItemColumns.KEY_DATA_ID + " = ?"), arrayOf(messageItem.dataId), null)
            }
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                var contactItem: ContactItem = messageItem.contactItem
                if ((null == contactItem)) {

                }
                else {
                    var id: Long = MsgEmailContactsContentProvider.createOrUpdateMessagesContacts(databaseHelper, db, contactItem)
                    var contactItems: Set<ContactItem> = contactItem.contactItems
                    if (((contactItems != null) && !contactItems.empty)) {
                        MsgEmailContactsContentProvider.createOrUpdateMessagesContactsJoin(databaseHelper, db, contactItem, messageItem._id)
                    }
                }
            }
            else {
                var _id: Long = cursor.getLong(cursor.getColumnIndex(ctidCol))
                cursor.close()
                MsgEmailContactsContentProvider.createOrUpdateMessagesContactsJoin(databaseHelper, db, messageItem._id, _id)
            }
            return messageItem._id
        }
        fun createOrUpdateMessages(databaseHelper: DatabaseHelper, messageItem: MessageItem): Long {
            var messageItems: ArrayList<MessageItem> = ArrayList(1)
            messageItems.add(messageItem)
            var ids: Array<Long> = createOrUpdateMessages(databaseHelper, messageItems)
            if (((null == ids) || (0 == ids.size))) {
                return 0
            }
            return ids[0]
        }
        fun createOrUpdateMessages(databaseHelper: DatabaseHelper, messageItems: Collection<MessageItem>): Array<Long> {
            var ids: LongArray = LongArray(messageItems.size())
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                ids = createOrUpdateMessages(databaseHelper, db, messageItems)
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
            return ids
        }
        fun createOrUpdateMessages(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messageItems: Collection<MessageItem>): Array<Long> {
            var ids: LongArray = LongArray(messageItems.size())
            var i: Int = 0
            var array: Array<MessageItem> = messageItems.toArray(arrayOfNulls<MessageItem>(0))
            for (messageItem in array) {
                var l: Long = createOrUpdateMessages(databaseHelper, db, messageItem)
                if ((0 == l)) {
                    Log.e(TAG, ("Failed to insert row: " + messageItem))
                    throw SQLException(("Failed to insert row into " + messageItem))
                }
                ids[i++] = l
            }
            return ids
        }
        @Throws(JSONException::class, IOException::class)
        fun savesMsgsGrpDraftFromJSON(jsonArray: JSONArray, user: ContactItem, context: Context): List<MessageItem> {
            var messageItems: List<MessageItem> = ArrayList()
            if (((null == jsonArray) || (0 == jsonArray.length()))) {
                return messageItems
            }
            var count: Int = jsonArray.length()
            var i: Int = 0
            while ((i < count)) {
                var jsonObject: JSONObject = jsonArray.getJSONObject(i)
                var draft: JSONObject = jsonObject.getJSONObject("draft")
                draft.put("_id", jsonObject.getString("_id"))
                draft.put("_rev", jsonObject.getString("_rev"))
                draft.put("doc_type", jsonObject.getString("doc_type"))
                draft.put("doc_owner", jsonObject.getString("doc_owner"))
                draft.put("timestamp", jsonObject.getString("timestamp"))
                draft.put("datetime", jsonObject.optString("datetime"))
                var messageItem: MessageItem = savesMsgsGrpDraftFromJSON(draft, user, context)
                if ((messageItem != null)) {
                    messageItems.add(messageItem)
                }
                i++
            }
            return messageItems
        }
        @Throws(IOException::class, JSONException::class)
        fun savesMessageItem(messageItem: MessageItem, user: ContactItem, context: Context): Uri {
            if ((messageItem.isDraft() || messageItem.emailItem)) {
                if (TextUtils.isEmpty(messageItem.chatId)) {
                    messageItem.chatId = messageItem.toUid
                }
            }
            if ((messageItem.isChat() && TextUtils.isEmpty(messageItem.chatId))) {
                Log.e(TAG, ("savesMsgsGrpDraftFromJSON: ChatID cannot be NULL - : " + messageItem))
                return null
            }
            if ((messageItem.emailItem && TextUtils.isEmpty(messageItem.toUid))) {
                Log.e(TAG, ("savesMsgsGrpDraftFromJSON: ToUID cannot be NULL - : " + messageItem))
                return null
            }
            var messageThreadItem: MessageItem = MessageItem()
            var chatId: String = messageItem.chatId
            var cursor: Cursor = context.getApplicationContext()
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                messageThreadItem = createMessagesFromMessage(messageItem, user)
            }
            else {
                var _id: Long = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                messageThreadItem._id = _id
                fillMessagesFromMessage(messageThreadItem, messageItem, user)
                cursor.close()
            }
            var values: ContentValues = ContentValues()
            values.put(BaseItem.THREAD, MainApplicationSingleton.Serializer.serialize(messageThreadItem))
            values.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItem))
            return context.getApplicationContext()
        }
    }
    private var databaseHelper: DatabaseHelper? = null
    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(getContext(), DatabaseHelper.DATABASE_NAME)
        return true
    }
    override fun getType(uri: Uri): String {
        when (URI_MATCHER.match(uri)) {
            MSGSGRPDRAFT_DIR_TYPE -> {
                return MSGSGRPDRAFT_DIR_MIME_TYPE
            }
            MSGSGRPDRAFT_JOIN_DIR_TYPE -> {
                return MSGSGRPDRAFT_JOIN_DIR_MIME_TYPE
            }
            MSGSGRPDRAFT_ITEM_TYPE -> {
                return MSGSGRPDRAFT_ITEM_MIME_TYPE
            }
            MSGSGRPDRAFT_DATA_ITEM_TYPE -> {
                return MSGSGRPDRAFT_DATA_ITEM_MIME_TYPE
            }
            MSGSGRPDRAFT_JOIN_ITEM_TYPE -> {
                return MSGSGRPDRAFT_JOIN_ITEM_MIME_TYPE
            }
            MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE -> {
                return MSGSGRPDRAFT_JOIN_DATA_ITEM_MIME_TYPE
            }
            MSGSGRPDRAFT_RAW_DIR_TYPE -> {
                return MSGSGRPDRAFT_RAW_DIR_MIME_TYPE
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
            MSGSGRPDRAFT_DIR_TYPE, MSGSGRPDRAFT_ITEM_TYPE, MSGSGRPDRAFT_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_MSGSGRPDRAFT, projection, selection, selectionArgs, sortOrder)
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
            MSGSGRPDRAFT_JOIN_DIR_TYPE -> {
                try {
                    cursor = getMessageThreadShalGroupJoin(databaseHelper, selection, selectionArgs, sortOrder)
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
            MSGSGRPDRAFT_JOIN_ITEM_TYPE, MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE -> {
                try {
                    cursor = getMessageThreadJoin(databaseHelper, selection, selectionArgs, sortOrder)
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
            MSGSGRPDRAFT_RAW_DIR_TYPE -> {
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
        var vals: Array<Byte> = values.getAsByteArray(BaseItem.THREAD)
        var vals2: Array<Byte> = values.getAsByteArray(MessageItem.TAG)
        when (URI_MATCHER.match(uri)) {
            MSGSGRPDRAFT_DIR_TYPE -> {
                try {
                    var id: Long = databaseHelper.insert(TABLE_MSGSGRPDRAFT, null, values)
                    var insertUri: Uri = ContentUris.withAppendedId(uri, id)
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return insertUri
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
            }
            MSGSGRPDRAFT_JOIN_DIR_TYPE -> {
                try {
                    var items: Collection<MessageItem> = (MainApplicationSingleton.Serializer.deserialize(vals) as Collection<MessageItem>)
                    if ((null == items)) {
                        return null
                    }
                    var ids: Array<Long> = createOrUpdateMessages(databaseHelper, items)
                    if (((null == ids) || (ids.size != items.size()))) {
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
            MSGSGRPDRAFT_ITEM_TYPE, MSGSGRPDRAFT_DATA_ITEM_TYPE -> {
                try {
                    var item: MessageItem = (MainApplicationSingleton.Serializer.deserialize(vals) as MessageItem)
                    var id: Long = createOrUpdateMessages(databaseHelper, item)
                    var insertUri: Uri = ContentUris.withAppendedId(MsgsGrpDraftContentProvider.CONTENT_URI, id)
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
    override fun update(uri: Uri, values: ContentValues, where: String, whereArgs: Array<String>): Int {
        var id: Int = 0
        var updateUri: Uri = null
        when (URI_MATCHER.match(uri)) {
            MSGSGRPDRAFT_DIR_TYPE, MSGSGRPDRAFT_ITEM_TYPE, MSGSGRPDRAFT_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper.update(TABLE_MSGSGRPDRAFT, values, where, whereArgs)
                    updateUri = ContentUris.withAppendedId(MsgsGrpDraftContentProvider.CONTENT_URI, id)
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                    return id
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                }
                break
            }
            MSGSGRPDRAFT_JOIN_DIR_TYPE, MSGSGRPDRAFT_JOIN_ITEM_TYPE, MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE -> {
                try {
                    var vals1: Array<Byte> = values.getAsByteArray(MessageItem.TAG)
                    var item: MessageItem = (MainApplicationSingleton.Serializer.deserialize(vals1) as MessageItem)
                    id = (createOrUpdateMessages(databaseHelper, item) as Int)
                    updateUri = ContentUris.withAppendedId(MsgsGrpDraftContentProvider.CONTENT_URI, id)
                    var context: Context = getContext()
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
    override fun delete(uri: Uri, where: String, whereArgs: Array<String>): Int {
        var id: Int = 0
        var delUri: Uri = null
        when (URI_MATCHER.match(uri)) {
            MSGSGRPDRAFT_DIR_TYPE, MSGSGRPDRAFT_ITEM_TYPE, MSGSGRPDRAFT_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper.delete(TABLE_MSGSGRPDRAFT, where, whereArgs)
                    delUri = ContentUris.withAppendedId(MsgsGrpDraftContentProvider.CONTENT_URI, id)
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
                }
                catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            MSGSGRPDRAFT_JOIN_DIR_TYPE, MSGSGRPDRAFT_JOIN_ITEM_TYPE, MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper.delete(TABLE_MSGSGRPDRAFT, where, whereArgs)
                    delUri = ContentUris.withAppendedId(MsgsGrpDraftContentProvider.CONTENT_URI, id)
                    var context: Context = getContext()
                    if ((null != context)) {
                        context.getContentResolver()
                    }
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
        return id
    }
    fun getOpenHelperForTest(): DatabaseHelper {
        return databaseHelper
    }
}
