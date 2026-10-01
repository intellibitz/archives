

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
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.ContactsContactJoinColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.db.IntellibitzItemColumns
import intellibitz.intellidroid.db.MessageAttachmentJoinColumns
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.db.MessagesContactsJoinColumns
import intellibitz.intellidroid.db.MessagesMessageJoinColumns
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.db.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import intellibitz.intellidroid.db.IntellibitzItemColumns.KEY_DATA_ID
import intellibitz.intellidroid.db.IntellibitzItemColumns.KEY_ID

class MessageChatGroupContentProvider : ContentProvider() {
    companion object {
        const val TAG = "MessageCP"
        const val TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN = "messageschatgroup_contacts"
        const val CREATE_TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN = ((((((((((("CREATE TABLE " + TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN) + "( ") + MessagesContactsJoinColumns.KEY_ID) + " INTEGER PRIMARY KEY,") + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID) + " INTEGER,") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + " INTEGER,") + MessagesContactsJoinColumns.KEY_TIMESTAMP) + " LONG") + ")")
        const val TABLE_MESSAGESCHATGROUP = "messageschatgroup"
        const val CREATE_TABLE_MESSAGESCHATGROUP_INDEX = (((((((("CREATE INDEX " + TABLE_MESSAGESCHATGROUP) + MessageItemColumns.KEY_DATA_ID) + DatabaseHelper.IDX) + " ON ") + TABLE_MESSAGESCHATGROUP) + " (") + MessageItemColumns.KEY_DATA_ID) + ")")
        const val TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN = "messageschatgroup_message"
        const val CREATE_TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN = ((((((((((("CREATE TABLE " + TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN) + "( ") + MessagesMessageJoinColumns.KEY_ID) + " INTEGER PRIMARY KEY,") + MessagesMessageJoinColumns.KEY_MESSAGE_ID) + " INTEGER,") + MessagesMessageJoinColumns.KEY_MSG_THREAD_ID) + " INTEGER,") + MessagesMessageJoinColumns.KEY_TIMESTAMP) + " LONG") + ")")
        const val TABLE_MESSAGECHATGROUP = "messagechatgroup"
        const val TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN = "messagechatgroup_attachments"
        const val CREATE_TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN = ((((((((((("CREATE TABLE " + TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN) + "( ") + MessageAttachmentJoinColumns.KEY_ID) + " INTEGER PRIMARY KEY,") + MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID) + " INTEGER,") + MessageAttachmentJoinColumns.KEY_MESSAGE_ID) + " INTEGER,") + MessageAttachmentJoinColumns.KEY_TIMESTAMP) + " LONG") + ")")
        const val TABLE_MESSAGECHATGROUP_CNTSGRPBROADCAST_JOIN = "messagechatgroup_cntsgrpbroadcast"
        const val TABLE_MESSAGECHATGROUP_CONTACTS_JOIN = "messagechatgroup_contacts"
        const val CREATE_TABLE_MESSAGECHATGROUP_CONTACTS_JOIN = ((((((((((("CREATE TABLE " + TABLE_MESSAGECHATGROUP_CONTACTS_JOIN) + "( ") + MessagesContactsJoinColumns.KEY_ID) + " INTEGER PRIMARY KEY,") + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID) + " INTEGER,") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + " INTEGER,") + MessagesContactsJoinColumns.KEY_TIMESTAMP) + " LONG") + ")")
        const val CREATE_TABLE_MESSAGECHATGROUP = (("CREATE TABLE " + TABLE_MESSAGECHATGROUP) + MessageItemColumns.MESSAGECHAT_SCHEMA)
        const val CREATE_TABLE_MESSAGESCHATGROUP = (("CREATE TABLE " + TABLE_MESSAGESCHATGROUP) + MessageItemColumns.MESSAGECHAT_SCHEMA)
        const val SCHEME = "content://"
        const val MESSAGECHATGROUP_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all")
        const val MESSAGECHATGROUP_JOIN_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join")
        const val MESSAGECHATGROUP_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id")
        const val MESSAGECHATGROUP_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id")
        const val MESSAGECHATGROUP_JOIN_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id")
        const val MESSAGECHATGROUP_JOIN_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id")
        const val MESSAGECHATGROUP_RAW_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all")
        const val AUTHORITY = "intellibitz.intellidroid.content.MessageChatGroupContentProvider"
        val CONTENT_URI: Uri = Uri.parse((((SCHEME + AUTHORITY) + "/") + TABLE_MESSAGECHATGROUP))
        val JOIN_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "join_") + TABLE_MESSAGECHATGROUP))
        val RAW_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "raw_") + TABLE_MESSAGECHATGROUP))
        private const val MESSAGECHATGROUP_DIR_TYPE = 1
        private const val MESSAGECHATGROUP_JOIN_DIR_TYPE = 2
        private const val MESSAGECHATGROUP_ITEM_TYPE = 3
        private const val MESSAGECHATGROUP_DATA_ITEM_TYPE = 4
        private const val MESSAGECHATGROUP_JOIN_ITEM_TYPE = 5
        private const val MESSAGECHATGROUP_JOIN_DATA_ITEM_TYPE = 6
        private const val MESSAGECHATGROUP_RAW_DIR_TYPE = 7
        private const val SEARCH_SUGGEST = 8
        private const val REFRESH_SHORTCUT = 9
        private val URI_MATCHER: UriMatcher = buildUriMatcher()
        private fun buildUriMatcher(): UriMatcher {
            var matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_MESSAGECHATGROUP, MESSAGECHATGROUP_DIR_TYPE)
            matcher.addURI(AUTHORITY, ("join_" + TABLE_MESSAGECHATGROUP), MESSAGECHATGROUP_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MESSAGECHATGROUP + "/#"), MESSAGECHATGROUP_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MESSAGECHATGROUP + "/*"), MESSAGECHATGROUP_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MESSAGECHATGROUP) + "/#"), MESSAGECHATGROUP_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MESSAGECHATGROUP) + "/*"), MESSAGECHATGROUP_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("raw_" + TABLE_MESSAGECHATGROUP), MESSAGECHATGROUP_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_QUERY + "/*"), SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*"), REFRESH_SHORTCUT)
            return matcher
        }
        @Throws(JSONException::class)
        fun createsChatMessageItemFromJSON(jsonObject: JSONObject, user: ContactItem): MessageItem {
            if ((null == jsonObject)) {
                return null
            }
            var messageItem: MessageItem = MessageItem()
            var id: String = jsonObject.optString("_id")
            if (!TextUtils.isEmpty(id)) {
                messageItem.dataId = id
            }
            var rev: String = jsonObject.optString("_rev")
            if (!TextUtils.isEmpty(rev)) {
                messageItem.dataRev = rev
            }
            messageItem.group = true
            messageItem.emailItem = false
            var chat_name: String = jsonObject.optString("chat_name")
            if (!TextUtils.isEmpty(chat_name)) {
                messageItem.name = chat_name
            }
            var chat_id: String = jsonObject.optString("chat_id")
            if (!TextUtils.isEmpty(chat_id)) {
                messageItem.chatId = chat_id
                messageItem.toChatUid = messageItem.chatId
                messageItem.threadId = messageItem.chatId
                messageItem.threadIdRef = messageItem.chatId
            }
            createsContactsFromMessage(messageItem)
            var to_type: String = jsonObject.optString("to_type")
            if (!TextUtils.isEmpty(to_type)) {
                messageItem.toType = to_type
            }
            var from_uid: String = jsonObject.optString("from_uid")
            if (!TextUtils.isEmpty(from_uid)) {
                messageItem.fromUid = from_uid
                messageItem.docSenderEmail = messageItem.fromUid
            }
            var txt: String = jsonObject.optString("txt")
            if (!TextUtils.isEmpty(txt)) {
                messageItem.text = txt
            }
            var to_uid: String = jsonObject.optString("to_uid")
            if (!TextUtils.isEmpty(to_uid)) {
                messageItem.toUid = to_uid
            }
            var from_name: String = jsonObject.optString("from_name")
            if (!TextUtils.isEmpty(from_name)) {
                messageItem.fromName = from_name
            }
            var msg_type: String = jsonObject.optString("msg_type")
            if (!TextUtils.isEmpty(msg_type)) {
                messageItem.messageType = msg_type
                messageItem.type = messageItem.messageType
            }
            var doc_type: String = jsonObject.optString("doc_type")
            if (!TextUtils.isEmpty(doc_type)) {
                messageItem.docType = doc_type
                messageItem.baseType = doc_type
            }
            var timestamp: Long = jsonObject.optLong("timestamp")
            if ((timestamp > 0)) {
                messageItem.timestamp = timestamp
            }
            var datetime: String = jsonObject.optString("datetime")
            if ((datetime != null)) {
                messageItem.dateTime = datetime
            }
            var msg_ref: String = jsonObject.optString("msg_ref")
            if (!TextUtils.isEmpty(msg_ref)) {
                messageItem.msgRef = msg_ref
            }
            var chat_msg_ref: String = jsonObject.optString("client_msg_ref")
            if (!TextUtils.isEmpty(chat_msg_ref)) {
                messageItem.chatMsgRef = chat_msg_ref
            }
            var doc_owner: String = jsonObject.optString("doc_owner")
            if (!TextUtils.isEmpty(doc_owner)) {
                messageItem.docOwner = doc_owner
            }
            var msg_direction: String = jsonObject.optString("msg_direction")
            if (!TextUtils.isEmpty(msg_direction)) {
                messageItem.messageDirection = msg_direction
            }
            if (jsonObject.optBoolean("read", false)) {
                messageItem.read = 1
            }
            else {

            }
            var pending_docs: Int = jsonObject.optInt("pending_docs")
            if ((pending_docs > 0)) {
                messageItem.pendingDocs = pending_docs
            }
            var name: String = messageItem.name
            if (!TextUtils.isEmpty(name)) {
                messageItem.subject = name
            }
            var fromName: String = messageItem.fromName
            if (!TextUtils.isEmpty(fromName)) {
                messageItem.docSender = fromName
            }
            messageItem.html = messageItem.html
            messageItem.noText = messageItem.isNoText()
            if ((!messageItem.isRead() && !user.dataId)) {
                messageItem.unreadCount = (messageItem.unreadCount + 1)
            }
            var ts: Long = messageItem.timestamp
            messageItem.latestMessageText = messageItem.getText()
            messageItem.latestMessageTimestamp = ts
            messageItem.text = messageItem.getText()
            if ((!messageItem.isRead() && !user.dataId)) {
                messageItem.unreadCount = (messageItem.unreadCount + 1)
            }
            var dt: String = messageItem.dateTime
            if ((dt != null)) {
                var mdt: String = messageItem.dateTime
                if ((null == mdt)) {
                    messageItem.dateTime = dt
                }
                else {
                    var dts: Long = MainApplicationSingleton.getDateTimeMillisISO(dt)
                    var mdts: Long = MainApplicationSingleton.getDateTimeMillisISO(mdt)
                    if (((0 == mdts) || (dts < mdts))) {
                        messageItem.dateTime = dt
                    }
                }
            }
            var attachments: JSONArray = jsonObject.optJSONArray("attachments")
            setChatAttachmentsFromJSONArray(messageItem, attachments)
            return messageItem
        }
        fun createsContactsFromMessage(messageItem: MessageItem): ContactItem {
            var contacts: ContactItem = ContactItem()
            contacts.name = messageItem.name
            contacts.intellibitzId = messageItem.chatId
            contacts.groupId = contacts.intellibitzId
            contacts.dataId = messageItem.chatId
            contacts.typeId = messageItem.chatId
            contacts.emailItem = false
            contacts.group = true
            messageItem.contactItem = contacts
            return contacts
        }
        @Throws(JSONException::class)
        fun createsEmailMessageItemFromJSON(jsonObject: JSONObject): MessageItem {
            if ((null == jsonObject)) {
                return null
            }
            var messageItem: MessageItem = MessageItem()
            var contactThreadItem: ContactItem = ContactItem()
            messageItem.contactItem = contactThreadItem
            var doc_type: String = jsonObject.optString("doc_type")
            if (!TextUtils.isEmpty(doc_type)) {
                messageItem.baseType = doc_type
                messageItem.docType = doc_type
            }
            var rev: String = jsonObject.optString("_rev")
            if (!TextUtils.isEmpty(rev)) {
                messageItem.dataRev = rev
            }
            var id: String = jsonObject.optString("_id")
            if (!TextUtils.isEmpty(id)) {
                messageItem.dataId = id
            }
            var to_uid: String = jsonObject.optString("to_uid")
            if (!TextUtils.isEmpty(to_uid)) {
                messageItem.toUid = to_uid
                messageItem.chatId = messageItem.toUid
                messageItem.toChatUid = messageItem.toUid
                contactThreadItem.typeId = messageItem.toUid
                contactThreadItem.intellibitzId = messageItem.toUid
                contactThreadItem.dataId = messageItem.toUid
                contactThreadItem.groupId = messageItem.toUid
                contactThreadItem.group = true
                contactThreadItem.emailItem = true
            }
            var pending_docs: Int = jsonObject.optInt("pending_docs")
            if ((pending_docs > 0)) {
                messageItem.pendingDocs = pending_docs
            }
            var doc_owner: String = jsonObject.optString("doc_owner")
            if (!TextUtils.isEmpty(doc_owner)) {
                messageItem.docOwner = doc_owner
            }
            var doc_owner_email: String = jsonObject.optString("doc_owner_email")
            if (!TextUtils.isEmpty(doc_owner_email)) {
                messageItem.docOwnerEmail = doc_owner_email
            }
            var sub: String = jsonObject.optString("sub")
            if (!TextUtils.isEmpty(sub)) {
                messageItem.subject = sub
            }
            var txt: String = jsonObject.optString("txt")
            if (!TextUtils.isEmpty(txt)) {
                messageItem.text = txt
            }
            var full_txt: String = jsonObject.optString("full_txt")
            if (!TextUtils.isEmpty(full_txt)) {
                messageItem.fullText = full_txt
            }
            var html: String = jsonObject.optString("html")
            if (!TextUtils.isEmpty(html)) {
                messageItem.html = html
            }
            var msg_direction: String = jsonObject.optString("msg_direction")
            if (!TextUtils.isEmpty(msg_direction)) {
                messageItem.messageDirection = msg_direction
            }
            var from_name: String = jsonObject.optString("from_name")
            if (!TextUtils.isEmpty(from_name)) {
                messageItem.fromName = from_name
            }
            var from_email: String = jsonObject.optString("from_email")
            if (!TextUtils.isEmpty(from_email)) {
                messageItem.fromEmail = from_email
            }
            var msg_type: String = jsonObject.optString("msg_type")
            if (!TextUtils.isEmpty(msg_type)) {
                messageItem.messageType = msg_type
            }
            messageItem.type = messageItem.messageType
            var msg_attch_uid: String = jsonObject.optString("msg_attch_uid")
            if (!TextUtils.isEmpty(msg_attch_uid)) {
                messageItem.messageAttachId = msg_attch_uid
            }
            var broadcast: Boolean = jsonObject.optBoolean("broadcast")
            if (broadcast) {
                messageItem.broadcast = broadcast
            }
            var no_text: Boolean = jsonObject.optBoolean("no_text")
            if (no_text) {
                messageItem.noText = no_text
            }
            var flags: String = jsonObject.optString("flags")
            if (!TextUtils.isEmpty(flags)) {
                messageItem.flags = flags
            }
            var timestamp: Long = jsonObject.optLong("timestamp")
            if ((timestamp > 0)) {
                messageItem.timestamp = timestamp
            }
            var datetime: String = jsonObject.optString("datetime")
            if ((datetime != null)) {
                messageItem.dateTime = datetime
            }
            var emails: JSONArray = jsonObject.optJSONArray("emails")
            if ((null != emails)) {
                var i: Int = 0
                while ((i < emails.length())) {
                    var js: JSONObject = emails.getJSONObject(i)
                    var email: String = js.optString("email")
                    var name: String = js.optString("name")
                    var type: String = js.optString("type")
                    var contactItem: ContactItem = ContactItem()
                    contactItem.dataId = email
                    contactItem.typeId = email
                    contactItem.intellibitzId = email
                    contactItem.name = name
                    contactItem.type = type
                    contactItem.emailItem = true
                    var emailItem: ContactItem = contactThreadItem.addContact(contactItem)
                    if ("from") {
                        var emailItemEmail: String = emailItem.typeId
                        if (!TextUtils.isEmpty(emailItemEmail)) {
                            messageItem.from = emailItemEmail
                            messageItem.docSenderEmail = emailItemEmail
                        }
                        var emailItemName: String = emailItem.name
                        if (!TextUtils.isEmpty(emailItemName)) {
                            messageItem.docSender = emailItemName
                        }
                    }
                    i++
                }
                messageItem.invoke()
            }
            var attachments: JSONArray = jsonObject.optJSONArray("attachments")
            setAttachmentsFromJSONArray(messageItem, attachments)
            return messageItem
        }
        @Throws(JSONException::class)
        fun setAttachmentsFromJSONArray(messageItem: MessageItem, attachments: JSONArray) {
            if (((null != attachments) && (attachments.length() > 0))) {
                var items: Set<MessageItem> = HashSet()
                var i: Int = 0
                while ((i < attachments.length())) {
                    var attachmentItem: MessageItem = MessageItem()
                    var js: JSONObject = attachments.getJSONObject(i)
                    attachmentItem.msgAttachID = messageItem.messageAttachId
                    attachmentItem.partID = js.getString("partID")
                    attachmentItem.type = js.getString("type")
                    attachmentItem.subType = js.optString("subtype")
                    attachmentItem.encoding = js.optString("encoding")
                    attachmentItem.size = js.optInt("size")
                    attachmentItem.language = js.optString("language")
                    attachmentItem.md5 = js.optString("md5")
                    attachmentItem.description = js.optString("description")
                    var params: JSONObject = js.optJSONObject("params")
                    var dispo: JSONObject = js.optJSONObject("disposition")
                    if (((null == params) && (dispo != null))) {
                        params = dispo.optJSONObject("params")
                    }
                    if ((null == params)) {
                        attachmentItem.dataId = ((messageItem.dataId + messageItem.messageAttachId) + attachmentItem.partID)
                    }
                    else {
                        attachmentItem.name = params.optString("name")
                        if ((null == attachmentItem.name)) {
                            attachmentItem.name = params.optString("filename")
                        }
                        attachmentItem.dataId = (((messageItem.dataId + messageItem.messageAttachId) + attachmentItem.partID) + attachmentItem.name)
                    }
                    items.add(attachmentItem)
                    i++
                }
                messageItem.attachments = items
                messageItem.hasAttachments = items.size()
            }
        }
        @Throws(JSONException::class)
        fun setChatAttachmentsFromJSONArray(messageItem: MessageItem, jsonArray: JSONArray) {
            if (((null != jsonArray) && (jsonArray.length() > 0))) {
                var items: Set<MessageItem> = HashSet()
                var i: Int = 0
                while ((i < jsonArray.length())) {
                    var attachmentItem: MessageItem = MessageItem()
                    var jsonObject: JSONObject = jsonArray.getJSONObject(i)
                    MsgChatGrpAttachmentContentProvider.setAttachmentItemFromJson(attachmentItem, jsonObject)
                    attachmentItem.dataId = attachmentItem.downloadURL
                    items.add(attachmentItem)
                    i++
                }
                messageItem.attachments = items
                messageItem.hasAttachments = items.size()
            }
        }
        @Throws(IOException::class, JSONException::class)
        fun savesMessageItem(messageItem: MessageItem, user: ContactItem, context: Context): Uri {
            if ((messageItem.isDraft() && TextUtils.isEmpty(messageItem.chatId))) {
                messageItem.chatId = messageItem.toUid
            }
            if (TextUtils.isEmpty(messageItem.chatId)) {
                Log.e(TAG, ("savesMsgDocTypeInDBFromJSON: ChatID cannot be NULL - : " + messageItem))
                return null
            }
            var messageThreadItem: MessageItem = clonesMessagesFromMessage(messageItem, user)
            if ((messageThreadItem != null)) {
                var chatId: String = messageItem.chatId
                var cursor: Cursor = context.getApplicationContext()
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                }
                else {
                    var _id: Long = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID))
                    var mts: Long = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE_TS))
                    messageThreadItem._id = _id
                    var ts: Long = messageItem.timestamp
                    if (((0 == mts) || (ts > mts))) {
                        messageThreadItem.latestMessageText = messageItem.getText()
                        messageThreadItem.latestMessageTimestamp = ts
                    }
                    else {
                        messageThreadItem.latestMessageText = null
                        messageThreadItem.latestMessageTimestamp = 0
                    }
                    cursor.close()
                }
            }
            var values: ContentValues = ContentValues()
            values.put(BaseItem.THREAD, MainApplicationSingleton.Serializer.serialize(messageThreadItem))
            values.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItem))
            return context.getApplicationContext()
        }
        @Throws(JSONException::class, IOException::class)
        fun savesMsgDocTypeInDBFromJSON(jsonObject: JSONObject, user: ContactItem, context: Context): MessageItem {
            var messageItem: MessageItem = createsChatMessageItemFromJSON(jsonObject, user)
            if ((null == messageItem)) {
                Log.e(TAG, ("savesMsgDocTypeInDBFromJSON: FAIL - : " + jsonObject))
                return null
            }
            var uri: Uri = savesMessageItem(messageItem, user, context)
            if ((uri != null)) {
                var id: Long = ContentUris.parseId(uri)
                messageItem._id = id
            }
            return messageItem
        }
        @Throws(JSONException::class, IOException::class)
        fun updateMessageInfoFromJSONByChatId(jsonObject: JSONObject, messageItem: MessageItem, context: Context): Int {
            var receiptType: String = jsonObject.optString("receipt_type")
            if (TextUtils.isEmpty(receiptType)) {
                return 0
            }
            var msgRef: String = jsonObject.optString("msg_ref")
            if (TextUtils.isEmpty(msgRef)) {
                return 0
            }
            if ("DELIVERED") {
                messageItem.delivered = 1
            }
            else {
                if ("READ") {
                    messageItem.read = 1
                }
            }
            messageItem.msgRef = msgRef
            var id: String = jsonObject.optString("_id")
            if (!TextUtils.isEmpty(id)) {
                messageItem.dataId = id
            }
            var rev: String = jsonObject.optString("_rev")
            if (!TextUtils.isEmpty(rev)) {
                messageItem.dataRev = rev
            }
            var doc_owner: String = jsonObject.optString("doc_owner")
            if (!TextUtils.isEmpty(doc_owner)) {
                messageItem.docOwner = doc_owner
            }
            var timestamp: Long = jsonObject.optLong("timestamp")
            if ((timestamp > 0)) {
                messageItem.timestamp = timestamp
            }
            var datetime: String = jsonObject.optString("datetime")
            if (!TextUtils.isEmpty(datetime)) {
                messageItem.dateTime = datetime
            }
            var user_uid: String = jsonObject.optString("user_uid")
            if (!TextUtils.isEmpty(user_uid)) {
                messageItem.fromUid = user_uid
            }
            var user_name: String = jsonObject.optString("user_name")
            if (!TextUtils.isEmpty(user_name)) {
                messageItem.docSender = user_name
            }
            var values: ContentValues = ContentValues()
            values.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItem))
            return context.getContentResolver()
        }
        fun isMessageFoundByMsgRef(msgRef: String, context: Context): Boolean {
            var found: Boolean = false
            var c: Cursor = getMessageByMsgRefCursor(msgRef, context)
            if (((c != null) && c.moveToFirst())) {
                found = (c.getCount() > 0)
            }
            if ((c != null)) {
                c.close()
            }
            return found
        }
        fun isMessageFoundByChatId(chatId: String, context: Context): Boolean {
            var found: Boolean = false
            var c: Cursor = getMessageByChatIdCursor(chatId, context)
            if (((c != null) && c.moveToFirst())) {
                found = (c.getCount() > 0)
            }
            if ((c != null)) {
                c.close()
            }
            return found
        }
        fun getMessageByChatIdCursor(chatId: String, context: Context): Cursor {
            return context.getContentResolver()
        }
        fun getMessageByMsgRefCursor(msgRef: String, context: Context): Cursor {
            return context.getContentResolver()
        }
        @Throws(JSONException::class)
        fun setChatAttachmentsFromJSONArray_(messageItem: MessageItem, jsonArray: JSONArray) {
            if (((null != jsonArray) && (jsonArray.length() > 0))) {
                var items: Set<MessageItem> = HashSet()
                var i: Int = 0
                while ((i < jsonArray.length())) {
                    var attachmentItem: MessageItem = MessageItem()
                    var url: String = jsonArray.getString(i)
                    var header: JSONObject = HttpUrlConnectionParser.headHTTP(url)
                    attachmentItem.downloadURL = url
                    attachmentItem.msgAttachID = url
                    attachmentItem.dataId = messageItem.dataId
                    if ((header != null)) {
                        var contentType: String = header.optString("Content-Type")
                        if (((contentType != null) && !contentType.empty)) {
                            var types: Array<String> = contentType.split("/")
                            attachmentItem.type = types[0]
                            attachmentItem.subType = types[1]
                        }
                        var len: Int = header.optInt("Content-Length")
                        attachmentItem.size = len
                    }
                    items.add(attachmentItem)
                    i++
                }
                messageItem.attachments = items
                messageItem.hasAttachments = items.size()
            }
        }
        @Throws(IOException::class)
        fun deleteMsgs(item: Array<String>, context: Context): Int {
            return context.getApplicationContext()
        }
        @Throws(IOException::class)
        fun deleteMsgs_(item: Array<String>, context: Context): Int {
            return context.getApplicationContext()
        }
        fun fillsMessageItemFromCursor(cursor: Cursor, messageItem: MessageItem) {
            messageItem._id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
            messageItem.threadId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_THREAD_ID))
            messageItem.threadIdRef = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_THREAD_IDREF))
            messageItem.threadIdParts = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_THREAD_IDPARTS))
            messageItem.groupId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_GROUP_ID))
            messageItem.groupIdRef = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_GROUP_IDREF))
            messageItem.intellibitzId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_INTELLIBITZ_ID))
            messageItem.profilePic = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_PIC))
            messageItem.deviceContactId = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_DEVICE_CONTACTID))
            messageItem.group = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_GROUP))
            messageItem.emailItem = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_EMAIL))
            messageItem.anonymous = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_ANONYMOUS))
            messageItem.device = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_DEVICE))
            messageItem.cloud = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_CLOUD))
            messageItem.dataId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATA_ID))
            messageItem.docType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_TYPE))
            messageItem.baseType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_BASE_TYPE))
            messageItem.chatMsgRef = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_CLIENT_MSG_REF))
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
            messageItem.fromEmail = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_FROM_EMAIL))
            messageItem.fromName = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_FROM_NAME))
            messageItem.fromUid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_FROM_UID))
            messageItem.toUid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO_UID))
            messageItem.toChatUid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO_CHAT_UID))
            messageItem.chatId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_CHAT_ID))
            messageItem.docSender = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_SENDER))
            messageItem.pendingDocs = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_PENDING_DOCS))
            messageItem.unreadCount = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_UNREAD_COUNT))
            messageItem.hasAttachments = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_HAS_ATTACHEMENTS))
            messageItem.subject = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_SUBJECT))
            messageItem.to = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO))
            messageItem.cc = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_CC))
            messageItem.bcc = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_BCC))
            messageItem.text = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TEXT))
            messageItem.messageDirection = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_MESSAGE_DIRECTION))
            messageItem.messageAttachId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_MESSAGE_ATTACH_ID))
            messageItem.read = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_READ))
            messageItem.delivered = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_IS_DELIVERED))
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
        fun fillContentValuesFromMessageItem(messageItem: MessageItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_ID, messageItem.threadId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_IDREF, messageItem.threadIdRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_IDPARTS, messageItem.threadIdParts)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_GROUP_ID, messageItem.groupId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_GROUP_IDREF, messageItem.groupIdRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_INTELLIBITZ_ID, messageItem.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_PIC, messageItem.profilePic)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DEVICE_CONTACTID, messageItem.deviceContactId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_GROUP, messageItem.group)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_EMAIL, messageItem.emailItem)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_ANONYMOUS, messageItem.anonymous)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DEVICE, messageItem.device)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_CLOUD, messageItem.cloud)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATA_ID, messageItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CHAT_ID, messageItem.chatId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_UID, messageItem.toUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_CHAT_UID, messageItem.toChatUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MSG_REF, messageItem.msgRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CLIENT_MSG_REF, messageItem.chatMsgRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATA_REV, messageItem.dataRev)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_NAME, messageItem.name)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FIRST_NAME, messageItem.firstName)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LAST_NAME, messageItem.lastName)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DISPLAY_NAME, messageItem.displayName)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FOLDER_CODE, messageItem.folderCode)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DEFAULT_FOLDER, messageItem.defaultFolder)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TYPE, messageItem.getType())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_TYPE, messageItem.toType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MESSAGE_TYPE, messageItem.messageType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_TYPE, messageItem.docType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_OWNER, messageItem.docOwner)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_OWNER_EMAIL, messageItem.docOwnerEmail)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_SENDER, messageItem.docSender)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_SENDER_EMAIL, messageItem.docSenderEmail)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FROM_EMAIL, messageItem.fromEmail)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FROM_NAME, messageItem.fromName)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FROM_UID, messageItem.fromUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_TYPE, messageItem.docType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_BASE_TYPE, messageItem.baseType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_SUBJECT, messageItem.subject)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_FROM, messageItem.from)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO, messageItem.to)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CC, messageItem.cc)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_BCC, messageItem.bcc)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_HAS_ATTACHEMENTS, messageItem.hasAttachments)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LATEST_MESSAGE, messageItem.latestMessageText)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_READ, messageItem.isRead())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DELIVERED, messageItem.isDelivered())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_GROUP, messageItem.group)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_EMAIL, messageItem.emailItem)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_ANONYMOUS, messageItem.anonymous)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DEVICE, messageItem.device)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_CLOUD, messageItem.cloud)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_UNREAD_COUNT, messageItem.unreadCount)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MESSAGE_DIRECTION, messageItem.messageDirection)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TEXT, messageItem.getText())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_HTML, messageItem.html)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LATEST_MESSAGE_TS, messageItem.latestMessageTimestamp)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_UID, messageItem.toUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MESSAGE_ATTACH_ID, messageItem.messageAttachId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TIMESTAMP, messageItem.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATETIME, messageItem.dateTime)
            return values
        }
        fun fillContentValuesFromMessageItem_(messageItem: MessageItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_ID, messageItem.threadId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_IDREF, messageItem.threadIdRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_THREAD_IDPARTS, messageItem.threadIdParts)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_GROUP_ID, messageItem.groupId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_GROUP_IDREF, messageItem.groupIdRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DEVICE_CONTACTID, messageItem.deviceContactId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_INTELLIBITZ_ID, messageItem.intellibitzId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_GROUP, messageItem.group)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_EMAIL, messageItem.emailItem)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_ANONYMOUS, messageItem.anonymous)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DEVICE, messageItem.device)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_CLOUD, messageItem.cloud)
            MainApplicationSingleton.fillIfNotNull(values, IntellibitzItemColumns.KEY_DATA_ID, messageItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CHAT_ID, messageItem.chatId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_UID, messageItem.toUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TO_CHAT_UID, messageItem.toChatUid)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MSG_REF, messageItem.msgRef)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CLIENT_MSG_REF, messageItem.chatMsgRef)
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
        fun fillContentValuesForMsgRefUpdate(messageItem: MessageItem): ContentValues {
            var contentValues: ContentValues = ContentValues()
            if (messageItem.isRead()) {
                contentValues.put(MessageItemColumns.KEY_IS_READ, true)
            }
            if (messageItem.isDelivered()) {
                contentValues.put(MessageItemColumns.KEY_IS_DELIVERED, true)
            }
            return contentValues
        }
        fun getMessageShalGroupJoin(databaseHelper: DatabaseHelper, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
            var selectQuery: String = ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("SELECT  mt." + MessageItemColumns.KEY_ID) + " as mt_id, ") + " mt.") + MessageItemColumns.KEY_THREAD_ID) + " as mthid, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDREF) + " as mthidref, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDPARTS) + " as mthidparts, ") + " mt.") + MessageItemColumns.KEY_GROUP_ID) + " as mtgid, ") + " mt.") + MessageItemColumns.KEY_GROUP_IDREF) + " as mtgidref, ") + " mt.") + MessageItemColumns.KEY_INTELLIBITZ_ID) + " as mtclutid, ") + " mt.") + MessageItemColumns.KEY_IS_GROUP) + " as mtisgroup, ") + " mt.") + MessageItemColumns.KEY_IS_EMAIL) + " as mtisemail, ") + " mt.") + MessageItemColumns.KEY_IS_ANONYMOUS) + " as mtisanon, ") + " mt.") + MessageItemColumns.KEY_IS_DEVICE) + " as mtisdev, ") + " mt.") + MessageItemColumns.KEY_IS_CLOUD) + " as mtiscloud, ") + " mt.") + MessageItemColumns.KEY_CLIENT_MSG_REF) + " as mtcmr, ") + " mt.") + MessageItemColumns.KEY_FIRST_NAME) + " as mtfirst, ") + " mt.") + MessageItemColumns.KEY_LAST_NAME) + " as mtlast, ") + " mt.") + MessageItemColumns.KEY_DISPLAY_NAME) + " as mtdisplay, ") + " mt.") + MessageItemColumns.KEY_DEVICE_CONTACTID) + " as mtdevcid, ") + " mt.") + MessageItemColumns.KEY_DATA_ID) + " as mtid, ") + " mt.") + MessageItemColumns.KEY_TYPE) + " as mttype, ") + " mt.") + MessageItemColumns.KEY_TO_TYPE) + " as mttotype, ") + " mt.") + MessageItemColumns.KEY_DATA_REV) + " as mtrev, ") + " mt.") + MessageItemColumns.KEY_NAME) + " as mtname, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER) + " as mtdo, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER) + " as mtds, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mtdoe, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mtdse, ") + " mt.") + MessageItemColumns.KEY_FROM_UID) + " as mtfuid, ") + " mt.") + MessageItemColumns.KEY_TO_UID) + " as mttuid, ") + " mt.") + MessageItemColumns.KEY_TO_CHAT_UID) + " as mttcuid, ") + " mt.") + MessageItemColumns.KEY_CHAT_ID) + " as mtcuid, ") + " mt.") + MessageItemColumns.KEY_DOC_TYPE) + " as mtdoct, ") + " mt.") + MessageItemColumns.KEY_BASE_TYPE) + " as mtbaset, ") + " mt.") + MessageItemColumns.KEY_SUBJECT) + " as mtsub, ") + " mt.") + MessageItemColumns.KEY_FROM) + " as mtfrom, ") + " mt.") + MessageItemColumns.KEY_TO) + " as mtto, ") + " mt.") + MessageItemColumns.KEY_CC) + " as mtcc, ") + " mt.") + MessageItemColumns.KEY_BCC) + " as mtbcc, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE) + " as mtlate, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE_TS) + " as mtlatets, ") + " mt.") + MessageItemColumns.KEY_IS_READ) + " as mtisr, ") + " mt.") + MessageItemColumns.KEY_IS_DELIVERED) + " as mtisd, ") + " mt.") + MessageItemColumns.KEY_IS_FLAGGED) + " as mtisf, ") + " mt.") + MessageItemColumns.KEY_UNREAD_COUNT) + " as mtuc, ") + " mt.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mtpd, ") + " mt.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mtha, ") + " mt.") + MessageItemColumns.KEY_TIMESTAMP) + " as mttime, ") + " mt.") + MessageItemColumns.KEY_DATETIME) + " as mtdtime, ") + " gc._id as gc_id, gc.id as gcid, gc.name as gcname,") + " gc.profile_pic as gcpic, gc.type as gctype, ") + " cc.profile_pic as ccpic") + "  FROM  ") + TABLE_MESSAGECHATGROUP) + " mt ") + "left outer join ") + TABLE_MESSAGECHATGROUP_CONTACTS_JOIN) + " mtg on mt.[_id] = mtg.[msgthread_id] ") + "left outer join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS) + " gc on gc.[_id] = mtg.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT) + " cc on cc.[") + ContactItemColumns.KEY_INTELLIBITZ_ID) + "] = mt.[") + MessageItemColumns.KEY_CHAT_ID) + "] ")
            if (((selection != null) && !selection.empty)) {
                selectQuery += (" WHERE " + selection)
            }
            if (((sortOrder != null) && !sortOrder.empty)) {
                selectQuery += (" ORDER BY " + sortOrder)
            }
            return databaseHelper.rawQuery(selectQuery, selectionArgs)
        }
        fun getMessageJoin(databaseHelper: DatabaseHelper, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
            var selectQuery: String = (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("SELECT  mt." + MessageItemColumns.KEY_ID) + " as mt_id, ") + " mt.") + MessageItemColumns.KEY_THREAD_ID) + " as mthid, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDREF) + " as mthidref, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDPARTS) + " as mthidparts, ") + " mt.") + MessageItemColumns.KEY_GROUP_ID) + " as mtgid, ") + " mt.") + MessageItemColumns.KEY_GROUP_IDREF) + " as mtgidref, ") + " mt.") + MessageItemColumns.KEY_INTELLIBITZ_ID) + " as mtclutid, ") + " mt.") + MessageItemColumns.KEY_IS_GROUP) + " as mtisgroup, ") + " mt.") + MessageItemColumns.KEY_IS_EMAIL) + " as mtisemail, ") + " mt.") + MessageItemColumns.KEY_IS_ANONYMOUS) + " as mtisanon, ") + " mt.") + MessageItemColumns.KEY_IS_DEVICE) + " as mtisdev, ") + " mt.") + MessageItemColumns.KEY_IS_CLOUD) + " as mtiscloud, ") + " mt.") + MessageItemColumns.KEY_CLIENT_MSG_REF) + " as mtcmr, ") + " mt.") + MessageItemColumns.KEY_FIRST_NAME) + " as mtfirst, ") + " mt.") + MessageItemColumns.KEY_LAST_NAME) + " as mtlast, ") + " mt.") + MessageItemColumns.KEY_DISPLAY_NAME) + " as mtdisplay, ") + " mt.") + MessageItemColumns.KEY_DEVICE_CONTACTID) + " as mtdevcid, ") + " mt.") + MessageItemColumns.KEY_DATA_ID) + " as mtid, ") + " mt.") + MessageItemColumns.KEY_TYPE) + " as mttype, ") + " mt.") + MessageItemColumns.KEY_TO_TYPE) + " as mttotype, ") + " mt.") + MessageItemColumns.KEY_DATA_REV) + " as mtrev, ") + " mt.") + MessageItemColumns.KEY_NAME) + " as mtname, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER) + " as mtdo, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER) + " as mtds, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mtdoe, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mtdse, ") + " mt.") + MessageItemColumns.KEY_FROM_UID) + " as mtfuid, ") + " mt.") + MessageItemColumns.KEY_TO_UID) + " as mttuid, ") + " mt.") + MessageItemColumns.KEY_TO_CHAT_UID) + " as mttcuid, ") + " mt.") + MessageItemColumns.KEY_CHAT_ID) + " as mtcuid, ") + " mt.") + MessageItemColumns.KEY_DOC_TYPE) + " as mtdoct, ") + " mt.") + MessageItemColumns.KEY_BASE_TYPE) + " as mtbaset, ") + " mt.") + MessageItemColumns.KEY_SUBJECT) + " as mtsub, ") + " mt.") + MessageItemColumns.KEY_FROM) + " as mtfrom, ") + " mt.") + MessageItemColumns.KEY_TO) + " as mtto, ") + " mt.") + MessageItemColumns.KEY_CC) + " as mtcc, ") + " mt.") + MessageItemColumns.KEY_BCC) + " as mtbcc, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE) + " as mtlate, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE_TS) + " as mtlatets, ") + " mt.") + MessageItemColumns.KEY_IS_READ) + " as mtisr, ") + " mt.") + MessageItemColumns.KEY_IS_DELIVERED) + " as mtisd, ") + " mt.") + MessageItemColumns.KEY_IS_FLAGGED) + " as mtisf, ") + " mt.") + MessageItemColumns.KEY_UNREAD_COUNT) + " as mtuc, ") + " mt.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mtpd, ") + " mt.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mtha, ") + " mt.") + MessageItemColumns.KEY_TIMESTAMP) + " as mttime, ") + " mt.") + MessageItemColumns.KEY_DATETIME) + " as mtdtime, ") + " m.") + MessageItemColumns.KEY_ID) + " as m_id,") + " m.") + MessageItemColumns.KEY_DATA_ID) + " as mid,") + " m.") + MessageItemColumns.KEY_FROM_NAME) + " as mfn,") + " m.") + MessageItemColumns.KEY_FROM_UID) + " as mfuid,") + " m.") + MessageItemColumns.KEY_TEXT) + " as mtxt,") + " m.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mdoe,") + " m.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mdse,") + " m.") + MessageItemColumns.KEY_MESSAGE_DIRECTION) + " as mdir,") + " m.") + MessageItemColumns.KEY_MESSAGE_ATTACH_ID) + " as maid,") + " m.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mpd,") + " m.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mha,") + " m.") + MessageItemColumns.KEY_IS_READ) + " as misr,") + " m.") + MessageItemColumns.KEY_IS_DELIVERED) + " as misd,") + " m.") + MessageItemColumns.KEY_IS_FLAGGED) + " as misf,") + " m.") + MessageItemColumns.KEY_TIMESTAMP) + " as mtime,") + " gc._id as gc_id, gc.id as gcid, gc.name as gcname,") + " gc.profile_pic as gcpic, gc.type as gctype, ") + " ak._id as ak_id, ak.id as akid, ak.name as akname, ") + " ak.profile_pic as akpic, ak.type as aktype, ") + " ak.intellibitz_id as akonid, ak.status as akstatus, ") + " e.name as ename, e.type_id as email, e.type as etype, e.type_id as etypeid, ") + "a.") + MessageItemColumns.KEY_ID) + " as a_id, ") + "a.") + MessageItemColumns.KEY_DATA_ID) + " as aid, ") + "a.") + MessageItemColumns.KEY_MSGATTCH_ID) + " as amid, ") + "a.") + MessageItemColumns.KEY_PARTID) + " as apid, ") + "a.") + MessageItemColumns.KEY_NAME) + " as aname, ") + "a.") + MessageItemColumns.KEY_TYPE) + " as atype, ") + "a.") + MessageItemColumns.KEY_SUBTYPE) + " as astype, ") + "a.") + MessageItemColumns.KEY_SIZE) + " as asize, ") + "a.") + MessageItemColumns.KEY_ENCODING) + " as aenc, ") + "a.") + MessageItemColumns.KEY_DOWNLOAD_URL) + " as aurl ") + "  FROM  ") + TABLE_MESSAGECHATGROUP) + " mt ") + "left outer join ") + TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN) + " mtg on mt.[_id] = mtg.[msgthread_id] ") + "left outer join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS) + " gc on gc.[_id] = mtg.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS_CONTACT_JOIN) + " gcak on gc.[_id] = gcak.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT) + " ak on ak.[_id] = gcak.[") + ContactsContactJoinColumns.KEY_CONTACT_ID) + "] ") + "left outer join ") + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT_INTELLIBITZCONTACTS_JOIN) + " mte on ak.[_id] = mte.[contact_id] ") + "left outer join ") + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT) + " e on e.[_id] = mte.[intellibitzcontact_id] ") + "left outer join ") + TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN) + " mtm on mt.[_id] = mtm.[msg_thread_id] ") + "left outer join ") + TABLE_MESSAGECHATGROUP) + " m on m.[_id] = mtm.[msg_id] ") + "left outer join ") + TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN) + " ma on m.[_id] = ma.[msg_id] ") + "left outer join ") + MsgChatGrpAttachmentContentProvider.TABLE_MSGCHATGRPATTACHMENT) + " a on a.[_id] = ma.[attachment_id] ")
            if (((selection != null) && !selection.empty)) {
                selectQuery += (" WHERE " + selection)
            }
            if (((sortOrder != null) && !sortOrder.empty)) {
                selectQuery += (" ORDER BY " + sortOrder)
            }
            return databaseHelper.rawQuery(selectQuery, selectionArgs)
        }
        fun createOrUpdateMessagesMessage(databaseHelper: DatabaseHelper, messageItem: MessageItem, messageThreadItem: MessageItem): Long {
            var messageItems: ArrayList<MessageItem> = ArrayList(1)
            messageItems.add(messageItem)
            var ids: Array<Long> = createOrUpdateMessagesMessage(databaseHelper, messageItems, messageThreadItem)
            if (((null == ids) || (0 == ids.size))) {
                return 0
            }
            return ids[0]
        }
        fun createOrUpdateMessagesMessage(databaseHelper: DatabaseHelper, messageItems: Collection<MessageItem>, messageThreadItem: MessageItem): Array<Long> {
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            var messageThreadItems: ArrayList<MessageItem> = ArrayList(1)
            messageThreadItems.add(messageThreadItem)
            var ids: Array<Long> = createOrUpdateMessages(databaseHelper, db, messageThreadItems)
            if (((null == ids) || (0 == ids.size))) {
                Log.e(TAG, ("Failed to insert row: " + messageThreadItem))
                throw SQLException(("Failed to insert row into " + messageThreadItem))
            }
            try {
                var array: Array<MessageItem> = messageItems.toArray(arrayOfNulls<MessageItem>(0))
                var i: Int = 0
                for (messageItem in array) {
                    var did: Long = messageThreadItem.deviceContactId
                    if ((did > 0)) {
                        messageItem.deviceContactId = did
                    }
                    var l: Long = createOrUpdateMessagesMessage(databaseHelper, db, messageItem, messageThreadItem._id)
                    if ((0 == l)) {
                        Log.e(TAG, ("Failed to insert row: " + messageItem))
                        throw SQLException(("Failed to insert row into " + messageItem))
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
        fun createOrUpdateMessagesMessage(databaseHelper: DatabaseHelper, messageItem: MessageItem, id: Long): Long {
            var items: ArrayList<MessageItem> = ArrayList(1)
            items.add(messageItem)
            var ids: Array<Long> = createOrUpdateMessageThreadMessages(databaseHelper, items, id)
            if (((null == ids) || (0 == ids.size))) {
                return 0
            }
            return ids[0]
        }
        fun createOrUpdateMessageThreadMessages(databaseHelper: DatabaseHelper, items: Collection<MessageItem>, id: Long): Array<Long> {
            var ids: LongArray = LongArray(items.size())
            var i: Int = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                var array: Array<MessageItem> = items.toArray(arrayOfNulls<MessageItem>(0))
                for (item in array) {
                    var l: Long = createOrUpdateMessagesMessage(databaseHelper, db, item, id)
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
        fun createOrUpdateMessagesMessage(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messageItem: MessageItem, id: Long): Long {
            var cursor: Cursor = null
            var _id: Long = 0
            var dataId: String = messageItem.dataId
            var chatId: String = messageItem.chatId
            var chatMsgRef: String = messageItem.chatMsgRef
            if (!TextUtils.isEmpty(chatMsgRef)) {
                cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_CLIENT_MSG_REF + " = ?"), arrayOf(chatMsgRef), null)
            }
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                if (((!TextUtils.isEmpty(dataId) && !TextUtils.isEmpty(chatId)) && (dataId == chatId))) {
                    cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(dataId), null)
                    if (((cursor != null) && (cursor.getCount() > 0))) {
                        _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                        cursor.close()
                    }
                }
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                cursor.close()
            }
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, TABLE_MESSAGECHATGROUP, null, MessageChatGroupContentProvider.fillContentValuesFromMessageItem(messageItem, ContentValues()))
            }
            else {
                if (!TextUtils.isEmpty(chatMsgRef)) {
                    databaseHelper.update(db, TABLE_MESSAGECHATGROUP, MessageChatGroupContentProvider.fillContentValuesFromMessageItem(messageItem, ContentValues()), (MessageItemColumns.KEY_CLIENT_MSG_REF + " = ?"), arrayOf(String.valueOf(chatMsgRef)))
                }
                else {
                    if (!TextUtils.isEmpty(dataId)) {
                        databaseHelper.update(db, TABLE_MESSAGECHATGROUP, MessageChatGroupContentProvider.fillContentValuesFromMessageItem(messageItem, ContentValues()), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(dataId)))
                    }
                }
            }
            if ((0 == _id)) {
                if (!TextUtils.isEmpty(chatMsgRef)) {
                    cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_CLIENT_MSG_REF + " = ?"), arrayOf(chatMsgRef), null)
                }
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                    if (!TextUtils.isEmpty(dataId)) {
                        cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(dataId), null)
                        if (((cursor != null) && (cursor.getCount() > 0))) {
                            _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                            cursor.close()
                        }
                    }
                }
                else {
                    _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                    cursor.close()
                }
            }
            if ((0 == _id)) {
                Log.e(TAG, "createOrUpdateNestMessageJoin: Message not found")
            }
            else {
                messageItem._id = _id
                createOrUpdateMessagesMessageJoin(databaseHelper, db, messageItem._id, id)
                var attachments: Set<MessageItem> = messageItem.attachments
                if (((null != attachments) && !attachments.empty)) {
                    MsgChatGrpAttachmentContentProvider.createOrUpdateMessageAttachments(databaseHelper, db, attachments, messageItem._id)
                    var rows: Int = updateMessageThreadHasAttachments(databaseHelper, id)
                    if ((0 == rows)) {
                        Log.e(TAG, ("createOrUpdateNestMessageJoin: MessageThread updated with Attachments: Rows Affected=" + rows))
                    }
                }
            }
            var ctidCol: String = MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID
            cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP_CONTACTS_JOIN, arrayOf(ctidCol), (MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ?"), arrayOf(String.valueOf(messageItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                ctidCol = ContactItemColumns.KEY_ID
                cursor = databaseHelper.query(db, MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS, arrayOf(ctidCol), (ContactItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(dataId)), null)
            }
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                var contactItem: ContactItem = messageItem.contactItem
                if ((null == contactItem)) {

                }
                else {
                    id = createOrUpdateMessageContacts(databaseHelper, db, contactItem)
                    var contactItems: Set<ContactItem> = contactItem.contactItems
                    if (((contactItems != null) && !contactItems.empty)) {
                        createOrUpdateMessageContactsJoin(databaseHelper, db, contactItem, messageItem._id)
                    }
                }
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(ctidCol))
                cursor.close()
                createOrUpdateMessageContactsJoin(databaseHelper, db, messageItem._id, _id)
            }
            return messageItem._id
        }
        fun updateMessageThreadHasAttachments(databaseHelper: DatabaseHelper, id: Long): Int {
            var values: ContentValues = ContentValues()
            values.put(MessageItemColumns.KEY_HAS_ATTACHEMENTS, 1)
            return databaseHelper.update(TABLE_MESSAGESCHATGROUP, values, (MessageItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(id)))
        }
        fun updateMessageThreadMessageAttachmentsURL(databaseHelper: DatabaseHelper, messageItem: MessageItem): Int {
            var rows: Int = 0
            var attachments: Set<MessageItem> = messageItem.attachments
            if (((null != attachments) && !attachments.empty)) {
                var values: ContentValues = ContentValues()
                for (attachmentItem in attachments) {
                    values.clear()
                    values.put(MessageItemColumns.KEY_DOWNLOAD_URL, attachmentItem.downloadURL)
                    rows += databaseHelper.update(MsgChatGrpAttachmentContentProvider.TABLE_MSGCHATGRPATTACHMENT, values, (MessageItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(attachmentItem._id)))
                }
            }
            return rows
        }
        fun createOrUpdateMessages(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messageItem: MessageItem): Long {
            var cursor: Cursor = databaseHelper.query(db, TABLE_MESSAGESCHATGROUP, arrayOf(MessageItemColumns.KEY_ID, MessageItemColumns.KEY_LATEST_MESSAGE, MessageItemColumns.KEY_LATEST_MESSAGE_TS, MessageItemColumns.KEY_DOC_SENDER), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(messageItem.dataId), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                var values: ContentValues = ContentValues()
                fillContentValuesFromMessageItem(messageItem, values)
                var _id: Long = databaseHelper.insert(db, TABLE_MESSAGESCHATGROUP, null, values)
                messageItem._id = _id
            }
            else {
                var _id: Long = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                messageItem._id = _id
                var msg: String = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE))
                var sender: String = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_SENDER))
                cursor.close()
                var values: ContentValues = ContentValues()
                fillContentValuesFromMessageItem(messageItem, values)
                databaseHelper.update(db, TABLE_MESSAGESCHATGROUP, values, (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(messageItem.dataId)))
            }
            cursor = databaseHelper.query(db, TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID), (MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ?"), arrayOf(String.valueOf(messageItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_DATA_ID + " = ?"), arrayOf(messageItem.dataId), null)
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                    var contactItem: ContactItem = messageItem.contactItem
                    if ((null == contactItem)) {

                    }
                    else {
                        var id: Long = createOrUpdateMessagesContacts(databaseHelper, db, contactItem)
                        var contactItems: Set<ContactItem> = contactItem.contactItems
                        if (((contactItems != null) && !contactItems.empty)) {
                            createOrUpdateMessagesContactsJoin(databaseHelper, db, contactItem, messageItem._id)
                        }
                    }
                }
                else {
                    var _id: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                    cursor.close()
                    createOrUpdateMessagesContactsJoin(databaseHelper, db, messageItem._id, _id)
                }
            }
            else {
                var cid: Long = cursor.getLong(cursor.getColumnIndex(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID))
                if ((cid > 0)) {
                    cursor = databaseHelper.query(db, MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS, arrayOf(ContactItemColumns.KEY_DATA_ID), (ContactItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(cid)), null)
                    if (((null == cursor) || (0 == cursor.getCount()))) {
                        if ((cursor != null)) {
                            cursor.close()
                        }
                        cursor = databaseHelper.query(db, MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS, arrayOf(ContactItemColumns.KEY_ID), (ContactItemColumns.KEY_DATA_ID + " = ?"), arrayOf(messageItem.dataId), null)
                        if (((null == cursor) || (0 == cursor.getCount()))) {
                            if ((cursor != null)) {
                                cursor.close()
                            }
                        }
                        else {
                            var gid: Long = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                            if ((gid > 0)) {
                                createOrUpdateMessagesContactsJoin(databaseHelper, db, messageItem._id, gid)
                            }
                        }
                    }
                }
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
                else {
                    MsgsGrpPeopleContentProvider.createOrUpdateMsgsGrpPeople(databaseHelper, db, messageItem)
                    MsgsGrpPeopleChatGroupsContentProvider.createOrUpdateMsgsGrpPeopleChatGroups(databaseHelper, db, messageItem)
                }
                ids[i++] = l
            }
            return ids
        }
        fun createMessagesFromMessage(messageItem: MessageItem, user: ContactItem): MessageItem {
            var messageThreadItem: MessageItem = MessageItem()
            messageThreadItem.baseType = "THREAD"
            messageThreadItem.docType = "THREAD"
            return fillMessagesFromMessage(messageThreadItem, messageItem, user)
        }
        fun clonesMessagesFromMessage(messageItem: MessageItem, user: ContactItem): MessageItem {
            try {
                var messageThreadItem: MessageItem = (messageItem.clone() as MessageItem)
                messageThreadItem.baseType = MessageItem.THREAD
                var chatId: String = messageItem.chatId
                if ((null == chatId)) {
                    chatId = messageItem.toUid
                }
                if ((null == chatId)) {
                    chatId = messageItem.dataId
                }
                messageThreadItem.dataId = chatId
                return messageThreadItem
            }
            catch (ignored: CloneNotSupportedException) {
                Log.e(TAG, ignored.getMessage())
            }
            return null
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
        @Throws(JSONException::class)
        fun fillMessageFromJSON(jsonObject: JSONObject, messageItem: MessageItem): MessageItem {
            messageItem.type = jsonObject.optString("msg_type")
            messageItem.toType = jsonObject.optString("to_type")
            messageItem.docType = jsonObject.getString("doc_type")
            messageItem.dataId = jsonObject.getString("_id")
            messageItem.dataRev = jsonObject.getString("_rev")
            messageItem.docOwner = jsonObject.getString("doc_owner")
            messageItem.docOwnerEmail = jsonObject.getString("doc_owner_email")
            messageItem.fromUid = jsonObject.optString("from_uid")
            messageItem.subject = jsonObject.getString("sub")
            messageItem.timestamp = jsonObject.getLong("timestamp")
            messageItem.dateTime = jsonObject.optString("datetime")
            var contactThreadItem: ContactItem = messageItem.contactItem
            if ((null == contactThreadItem)) {
                contactThreadItem = ContactItem()
                messageItem.contactItem = contactThreadItem
            }
            var emails: JSONArray = jsonObject.getJSONArray("emails")
            if ((null != emails)) {
                var i: Int = 0
                while ((i < emails.length())) {
                    var js: JSONObject = emails.getJSONObject(i)
                    var email: String = js.getString("email")
                    var name: String = js.getString("name")
                    var type: String = js.getString("type")
                    var contactItem: ContactItem = ContactItem()
                    contactItem.dataId = email
                    contactItem.typeId = email
                    contactItem.intellibitzId = email
                    contactItem.name = name
                    contactItem.type = type
                    var em: ContactItem = contactThreadItem.addContact(contactItem)
                    if ("from") {
                        messageItem.from = em.typeId
                        messageItem.docSender = em.name
                        messageItem.docSenderEmail = em.typeId
                    }
                    i++
                }
                messageItem.invoke()
            }
            return messageItem
        }
        @Throws(JSONException::class, IOException::class)
        fun updateMessageInfo(chatId: String, messageItem: MessageItem, context: Context): Int {
            var values: ContentValues = ContentValues()
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_READ, messageItem.isRead())
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_IS_DELIVERED, messageItem.isDelivered())
            return context.getContentResolver()
        }
        fun fillMessageItemsFromCursor(cursor: Cursor): List<MessageItem> {
            var messageItems: List<MessageItem> = ArrayList()
            if (((null == cursor) || (0 == cursor.getCount()))) {
                return messageItems
            }
            Log.e(MessagesChatGroupContentProvider.TAG, ("fillMessageItemsFromCursor: count - " + cursor.getCount()))
            do {
                var messageItem: MessageItem = createsMessageThreadItemFromCursor(cursor)
                messageItems.add(messageItem)
            } while (cursor.moveToNext())
            return messageItems
        }
        fun createOrUpdateMessagesContacts(databaseHelper: DatabaseHelper, db: SQLiteDatabase, contactItem: ContactItem): Long {
            var _id: Long = MsgChatGrpContactsContentProvider.createOrUpdateContacts(databaseHelper, db, contactItem)
            if ((0 == _id)) {
                return 0
            }
            var cursor: Cursor = databaseHelper.query(db, TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID), (IntellibitzItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(contactItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, TABLE_MESSAGESCHATGROUP, arrayOf(IntellibitzItemColumns.KEY_ID), (IntellibitzItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(contactItem.dataId)), null)
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                }
                else {
                    _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID))
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
            var _id: Long = MsgChatGrpContactsContentProvider.createOrUpdateContacts(databaseHelper, db, contactItem)
            if ((0 == _id)) {
                return 0
            }
            var cursor: Cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP_CONTACTS_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID), (IntellibitzItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(contactItem._id)), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP, arrayOf(IntellibitzItemColumns.KEY_ID), (IntellibitzItemColumns.KEY_DATA_ID + " = ?"), arrayOf(String.valueOf(contactItem.dataId)), null)
                if (((null == cursor) || (0 == cursor.getCount()))) {
                    if ((cursor != null)) {
                        cursor.close()
                    }
                }
                else {
                    _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID))
                    cursor.close()
                    createOrUpdateMessageContactsJoin(databaseHelper, db, _id, contactItem._id)
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
        fun createOrUpdateMessageContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var cursor: Cursor = getMessageContactThreadCursorJoin(databaseHelper, item, id)
            var _id: Long = 0
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS, arrayOf(IntellibitzItemColumns.KEY_ID), (IntellibitzItemColumns.KEY_DATA_ID + " = ? "), arrayOf(item.dataId), null)
                if (((null != cursor) && (0 != cursor.getCount()))) {
                    _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID))
                    cursor.close()
                    item._id = _id
                }
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
            }
            if ((_id != 0)) {
                createOrUpdateMessageContactsJoin(databaseHelper, db, _id, id)
            }
            return item._id
        }
        fun getMessageContactThreadCursorJoin(databaseHelper: DatabaseHelper, item: ContactItem, id: Long): Cursor {
            var args: Array<String> = arrayOf(item.dataId, String.valueOf(id))
            var selectQuery: String = ((((((((((((((((((((((("SELECT  * FROM " + TABLE_MESSAGECHATGROUP) + " nt ") + " left join ") + TABLE_MESSAGECHATGROUP_CONTACTS_JOIN) + " ntm on nt.[") + IntellibitzItemColumns.KEY_ID) + "] = ntm.[") + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID) + "]  ") + " left join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS) + " mt on ntm.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] = mt.[") + IntellibitzItemColumns.KEY_ID) + "] ") + " WHERE ") + " mt.") + IntellibitzItemColumns.KEY_ID) + " = ? ") + " AND nt.") + MessageItemColumns.KEY_ID) + " = ? ")
            return databaseHelper.rawQuery(selectQuery, args)
        }
        fun createOrUpdateMessagesContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: ContactItem, id: Long): Long {
            var cursor: Cursor = getMessageThreadContactThreadCursorJoin(databaseHelper, item, id)
            var _id: Long = 0
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                cursor = databaseHelper.query(db, MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS, arrayOf(IntellibitzItemColumns.KEY_ID), (IntellibitzItemColumns.KEY_DATA_ID + " = ? "), arrayOf(item.dataId), null)
                if (((null != cursor) && (0 != cursor.getCount()))) {
                    _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID))
                    cursor.close()
                    item._id = _id
                }
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID))
                cursor.close()
                item._id = _id
            }
            if ((_id != 0)) {
                createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, id)
            }
            return item._id
        }
        fun getMessageThreadContactThreadCursorJoin(databaseHelper: DatabaseHelper, item: ContactItem, id: Long): Cursor {
            var args: Array<String> = arrayOf(item.dataId, String.valueOf(id))
            var selectQuery: String = ((((((((((((((((((((((("SELECT  * FROM " + TABLE_MESSAGESCHATGROUP) + " nt ") + " left join ") + TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN) + " ntm on nt.[") + IntellibitzItemColumns.KEY_ID) + "] = ntm.[") + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID) + "]  ") + " left join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS) + " mt on ntm.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] = mt.[") + IntellibitzItemColumns.KEY_ID) + "] ") + " WHERE ") + " mt.") + IntellibitzItemColumns.KEY_ID) + " = ? ") + " AND nt.") + MessageItemColumns.KEY_ID) + " = ? ")
            return databaseHelper.rawQuery(selectQuery, args)
        }
        fun fillMessageThreadItemsFromCursor(cursor: Cursor): ArrayList<MessageItem> {
            var messageItems: ArrayList<MessageItem> = ArrayList()
            if (((null == cursor) || (0 == cursor.getCount()))) {
                return messageItems
            }
            var set: HashSet<MessageItem> = HashSet()
            Log.e(TAG, ("fillMessageItemsFromCursor: count - " + cursor.getCount()))
            do {
                var messageItem: MessageItem = createsMessageThreadItemFromCursor(cursor)
                set.add(messageItem)
            } while (cursor.moveToNext())
            messageItems.addAll(set)
            return messageItems
        }
        fun createsMessageThreadItemFromCursor(cursor: Cursor): MessageItem {
            var messageItem: MessageItem = MessageItem()
            fillsMessageItemFromCursor(cursor, messageItem)
            return messageItem
        }
        fun getMessagesContactsJoin(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var _id: Long = 0
            var c: Cursor = databaseHelper.query(TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN, arrayOf(MessagesContactsJoinColumns.KEY_ID), (((MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ? and ") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + " = ?"), arrayOf(String.valueOf(id), String.valueOf(fk)), null, null, null)
            if (((c != null) && (c.getCount() > 0))) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }
        fun createOrUpdateMessagesContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = getMessagesContactsJoin(databaseHelper, id, fk)
            var values: ContentValues = ContentValues()
            values.put(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID, id)
            values.put(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID, fk)
            values.put(MessagesContactsJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.dateTimeMillis)
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN, null, values)
            }
            else {
                _id = databaseHelper.update(db, TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN, values, (MessagesContactsJoinColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return _id
        }
        fun createOrUpdateMessageContactsJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = getMessageContactThreadJoin(databaseHelper, id, fk)
            var values: ContentValues = ContentValues()
            values.put(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID, id)
            values.put(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID, fk)
            values.put(MessagesContactsJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.dateTimeMillis)
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, TABLE_MESSAGECHATGROUP_CONTACTS_JOIN, null, values)
            }
            else {
                _id = databaseHelper.update(db, TABLE_MESSAGECHATGROUP_CONTACTS_JOIN, values, (MessagesContactsJoinColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return _id
        }
        fun getMessageContactThreadJoin(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var _id: Long = 0
            var c: Cursor = databaseHelper.query(TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN, arrayOf(IntellibitzItemColumns.KEY_ID), (((MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ? and ") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + " = ?"), arrayOf(String.valueOf(id), String.valueOf(fk)), null, null, null)
            if (((c != null) && (c.getCount() > 0))) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }
        fun createOrUpdateMessagesMessageJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id: Long = getMessageThreadMessageJoin(databaseHelper, id, fk)
            var values: ContentValues = ContentValues()
            values.put(MessagesMessageJoinColumns.KEY_MESSAGE_ID, id)
            values.put(MessagesMessageJoinColumns.KEY_MSG_THREAD_ID, fk)
            values.put(MessagesMessageJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.dateTimeMillis)
            if ((0 == _id)) {
                _id = databaseHelper.insert(db, TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN, null, values)
            }
            else {
                databaseHelper.update(db, TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN, values, (MessagesMessageJoinColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            }
            return _id
        }
        fun getMessageThreadMessageJoin(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var _id: Long = 0
            var c: Cursor = databaseHelper.query(TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN, arrayOf(IntellibitzItemColumns.KEY_ID), (((MessagesMessageJoinColumns.KEY_MESSAGE_ID + " = ? and ") + MessagesMessageJoinColumns.KEY_MSG_THREAD_ID) + " = ?"), arrayOf(String.valueOf(id), String.valueOf(fk)), null, null, null)
            if (((c != null) && (c.getCount() > 0))) {
                _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }
        fun deleteMessages(databaseHelper: DatabaseHelper, items: Array<String>): LongArray {
            var ids: LongArray = LongArray(items.size)
            var i: Int = 0
            var db: SQLiteDatabase = databaseHelper.getWritableDatabase()
            db.beginTransaction()
            try {
                for (id in items) {
                    var l: Long = deleteMessage(databaseHelper, db, id)
                    if ((0 == l)) {
                        Log.e(TAG, ("Failed to insert row: " + id))
                        return longArrayOf(0)
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
        fun deleteMessage(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: String): Long {
            var _id: Long = 0
            var cursor: Cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_DATA_ID + " = ?"), arrayOf(id), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                return 0
            }
            else {
                _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                cursor.close()
            }
            cursor = databaseHelper.query(db, TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN, arrayOf(MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID), (MessageItemColumns.KEY_ID + " = ?"), arrayOf(id), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
            }
            else {
                var i: Int = 0
                var aids: Array<String?> = arrayOfNulls<String>(cursor.getCount())
                do {
                    aids[i++] = String.valueOf(cursor.getLong(cursor.getColumnIndex(MessagesMessageJoinColumns.KEY_MSG_THREAD_ID)))
                } while (cursor.moveToNext())
                cursor.close()
                var w: String = ""
                i = 0
                while ((i < aids.size)) {
                    w += if ((i > 0)) ",?" else "?"
                    i++
                }
                databaseHelper.delete(db, MsgChatGrpAttachmentContentProvider.TABLE_MSGCHATGRPATTACHMENT, (((MessageItemColumns.KEY_ID + " IN( ") + w) + " )"), aids)
            }
            databaseHelper.delete(db, TABLE_MESSAGECHATGROUP, (MessageItemColumns.KEY_ID + " = ?"), arrayOf(String.valueOf(_id)))
            return _id
        }
        fun getMessagesThreadJoin(databaseHelper: DatabaseHelper, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
            var selectQuery: String = ((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("SELECT  mt." + IntellibitzItemColumns.KEY_ID) + " as mt_id, ") + " mt.") + MessageItemColumns.KEY_THREAD_ID) + " as mthid, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDREF) + " as mthidref, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDPARTS) + " as mthidparts, ") + " mt.") + MessageItemColumns.KEY_GROUP_ID) + " as mtgid, ") + " mt.") + MessageItemColumns.KEY_GROUP_IDREF) + " as mtgidref, ") + " mt.") + MessageItemColumns.KEY_INTELLIBITZ_ID) + " as mtclutid, ") + " mt.") + MessageItemColumns.KEY_IS_GROUP) + " as mtisgroup, ") + " mt.") + MessageItemColumns.KEY_IS_EMAIL) + " as mtisemail, ") + " mt.") + MessageItemColumns.KEY_IS_ANONYMOUS) + " as mtisanon, ") + " mt.") + MessageItemColumns.KEY_IS_DEVICE) + " as mtisdev, ") + " mt.") + MessageItemColumns.KEY_IS_CLOUD) + " as mtiscloud, ") + " mt.") + MessageItemColumns.KEY_FIRST_NAME) + " as mtfirst, ") + " mt.") + MessageItemColumns.KEY_LAST_NAME) + " as mtlast, ") + " mt.") + MessageItemColumns.KEY_DISPLAY_NAME) + " as mtdisplay, ") + " mt.") + MessageItemColumns.KEY_DEVICE_CONTACTID) + " as mtdevcid, ") + " mt.") + IntellibitzItemColumns.KEY_DATA_ID) + " as mtid, ") + " mt.") + MessageItemColumns.KEY_TYPE) + " as mttype, ") + " mt.") + MessageItemColumns.KEY_TO_TYPE) + " as mttotype, ") + " mt.") + MessageItemColumns.KEY_DATA_REV) + " as mtrev, ") + " mt.") + MessageItemColumns.KEY_NAME) + " as mtname, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER) + " as mtdo, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER) + " as mtds, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mtdoe, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mtdse, ") + " mt.") + MessageItemColumns.KEY_FROM_UID) + " as mtfuid, ") + " mt.") + MessageItemColumns.KEY_TO_UID) + " as mttuid, ") + " mt.") + MessageItemColumns.KEY_TO_CHAT_UID) + " as mttcuid, ") + " mt.") + MessageItemColumns.KEY_CHAT_ID) + " as mtcuid, ") + " mt.") + MessageItemColumns.KEY_DOC_TYPE) + " as mtdoct, ") + " mt.") + MessageItemColumns.KEY_BASE_TYPE) + " as mtbaset, ") + " mt.") + MessageItemColumns.KEY_SUBJECT) + " as mtsub, ") + " mt.") + MessageItemColumns.KEY_FROM) + " as mtfrom, ") + " mt.") + MessageItemColumns.KEY_TO) + " as mtto, ") + " mt.") + MessageItemColumns.KEY_CC) + " as mtcc, ") + " mt.") + MessageItemColumns.KEY_BCC) + " as mtbcc, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE) + " as mtlate, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE_TS) + " as mtlatets, ") + " mt.") + MessageItemColumns.KEY_IS_READ) + " as mtisr, ") + " mt.") + MessageItemColumns.KEY_IS_DELIVERED) + " as mtisd, ") + " mt.") + MessageItemColumns.KEY_IS_FLAGGED) + " as mtisf, ") + " mt.") + MessageItemColumns.KEY_UNREAD_COUNT) + " as mtuc, ") + " mt.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mtpd, ") + " mt.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mtha, ") + " mt.") + MessageItemColumns.KEY_TIMESTAMP) + " as mttime, ") + " mt.") + MessageItemColumns.KEY_DATETIME) + " as mtdtime, ") + " m.") + IntellibitzItemColumns.KEY_ID) + " as m_id,") + " m.") + IntellibitzItemColumns.KEY_DATA_ID) + " as mid,") + " m.") + MessageItemColumns.KEY_FROM_NAME) + " as mfn,") + " m.") + MessageItemColumns.KEY_FROM_UID) + " as mfuid,") + " m.") + MessageItemColumns.KEY_TEXT) + " as mtxt,") + " m.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mdoe,") + " m.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mdse,") + " m.") + MessageItemColumns.KEY_MESSAGE_DIRECTION) + " as mdir,") + " m.") + MessageItemColumns.KEY_MESSAGE_ATTACH_ID) + " as maid,") + " m.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mpd,") + " m.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mha,") + " m.") + MessageItemColumns.KEY_IS_READ) + " as misr,") + " m.") + MessageItemColumns.KEY_IS_DELIVERED) + " as misd,") + " m.") + MessageItemColumns.KEY_IS_FLAGGED) + " as misf,") + " m.") + MessageItemColumns.KEY_TIMESTAMP) + " as mtime,") + " gc._id as gc_id, gc.id as gcid, gc.name as gcname,") + " gc.profile_pic as gcpic, gc.type as gctype, ") + " ak._id as ak_id, ak.id as akid, ak.name as akname, ") + " ak.profile_pic as akpic, ak.type as aktype, ") + " ak.intellibitz_id as akonid, ak.status as akstatus, ") + " e.name as ename, e.type_id as email, e.type as etype, e.type_id as etypeid, ") + "a.") + IntellibitzItemColumns.KEY_ID) + " as a_id, ") + "a.") + IntellibitzItemColumns.KEY_DATA_ID) + " as aid, ") + "a.") + MessageItemColumns.KEY_MSGATTCH_ID) + " as amid, ") + "a.") + MessageItemColumns.KEY_PARTID) + " as apid, ") + "a.") + MessageItemColumns.KEY_NAME) + " as aname, ") + "a.") + MessageItemColumns.KEY_TYPE) + " as atype, ") + "a.") + MessageItemColumns.KEY_SUBTYPE) + " as astype, ") + "a.") + MessageItemColumns.KEY_SIZE) + " as asize, ") + "a.") + MessageItemColumns.KEY_ENCODING) + " as aenc, ") + "a.") + MessageItemColumns.KEY_DOWNLOAD_URL) + " as aurl ") + "  FROM  ") + TABLE_MESSAGESCHATGROUP) + " mt ") + "left outer join ") + TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN) + " mtg on mt.[_id] = mtg.[msgthread_id] ") + "left outer join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS) + " gc on gc.[_id] = mtg.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS_CONTACT_JOIN) + " gcak on gc.[_id] = gcak.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT) + " ak on ak.[_id] = gcak.[") + ContactsContactJoinColumns.KEY_CONTACT_ID) + "] ") + "left outer join ") + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT_INTELLIBITZCONTACTS_JOIN) + " mte on ak.[_id] = mte.[contact_id] ") + "left outer join ") + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT) + " e on e.[_id] = mte.[intellibitzcontact_id] ") + "left outer join ") + TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN) + " mtm on mt.[_id] = mtm.[msg_thread_id] ") + "left outer join ") + TABLE_MESSAGECHATGROUP) + " m on m.[_id] = mtm.[msg_id] ") + "left outer join ") + TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN) + " ma on m.[_id] = ma.[msg_id] ") + "left outer join ") + MsgChatGrpAttachmentContentProvider.TABLE_MSGCHATGRPATTACHMENT) + " a on a.[_id] = ma.[attachment_id] ")
            if (((selection != null) && !selection.empty)) {
                selectQuery += (" WHERE " + selection)
            }
            if (((sortOrder != null) && !sortOrder.empty)) {
                selectQuery += (" ORDER BY " + sortOrder)
            }
            return databaseHelper.rawQuery(selectQuery, selectionArgs)
        }
        fun getMessagesThreadShalGroupJoin(databaseHelper: DatabaseHelper, selection: String, selectionArgs: Array<String>, sortOrder: String): Cursor {
            var selectQuery: String = (((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((("SELECT  mt." + IntellibitzItemColumns.KEY_ID) + " as mt_id, ") + " mt.") + MessageItemColumns.KEY_THREAD_ID) + " as mthid, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDREF) + " as mthidref, ") + " mt.") + MessageItemColumns.KEY_THREAD_IDPARTS) + " as mthidparts, ") + " mt.") + MessageItemColumns.KEY_GROUP_ID) + " as mtgid, ") + " mt.") + MessageItemColumns.KEY_GROUP_IDREF) + " as mtgidref, ") + " mt.") + MessageItemColumns.KEY_INTELLIBITZ_ID) + " as mtclutid, ") + " mt.") + MessageItemColumns.KEY_IS_GROUP) + " as mtisgroup, ") + " mt.") + MessageItemColumns.KEY_IS_EMAIL) + " as mtisemail, ") + " mt.") + MessageItemColumns.KEY_IS_ANONYMOUS) + " as mtisanon, ") + " mt.") + MessageItemColumns.KEY_IS_DEVICE) + " as mtisdev, ") + " mt.") + MessageItemColumns.KEY_IS_CLOUD) + " as mtiscloud, ") + " mt.") + MessageItemColumns.KEY_FIRST_NAME) + " as mtfirst, ") + " mt.") + MessageItemColumns.KEY_LAST_NAME) + " as mtlast, ") + " mt.") + MessageItemColumns.KEY_DISPLAY_NAME) + " as mtdisplay, ") + " mt.") + MessageItemColumns.KEY_DEVICE_CONTACTID) + " as mtdevcid, ") + " mt.") + IntellibitzItemColumns.KEY_DATA_ID) + " as mtid, ") + " mt.") + MessageItemColumns.KEY_TYPE) + " as mttype, ") + " mt.") + MessageItemColumns.KEY_TO_TYPE) + " as mttotype, ") + " mt.") + MessageItemColumns.KEY_DATA_REV) + " as mtrev, ") + " mt.") + MessageItemColumns.KEY_NAME) + " as mtname, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER) + " as mtdo, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER) + " as mtds, ") + " mt.") + MessageItemColumns.KEY_DOC_OWNER_EMAIL) + " as mtdoe, ") + " mt.") + MessageItemColumns.KEY_DOC_SENDER_EMAIL) + " as mtdse, ") + " mt.") + MessageItemColumns.KEY_FROM_UID) + " as mtfuid, ") + " mt.") + MessageItemColumns.KEY_TO_UID) + " as mttuid, ") + " mt.") + MessageItemColumns.KEY_TO_CHAT_UID) + " as mttcuid, ") + " mt.") + MessageItemColumns.KEY_CHAT_ID) + " as mtcuid, ") + " mt.") + MessageItemColumns.KEY_DOC_TYPE) + " as mtdoct, ") + " mt.") + MessageItemColumns.KEY_BASE_TYPE) + " as mtbaset, ") + " mt.") + MessageItemColumns.KEY_SUBJECT) + " as mtsub, ") + " mt.") + MessageItemColumns.KEY_FROM) + " as mtfrom, ") + " mt.") + MessageItemColumns.KEY_TO) + " as mtto, ") + " mt.") + MessageItemColumns.KEY_CC) + " as mtcc, ") + " mt.") + MessageItemColumns.KEY_BCC) + " as mtbcc, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE) + " as mtlate, ") + " mt.") + MessageItemColumns.KEY_LATEST_MESSAGE_TS) + " as mtlatets, ") + " mt.") + MessageItemColumns.KEY_IS_READ) + " as mtisr, ") + " mt.") + MessageItemColumns.KEY_IS_DELIVERED) + " as mtisd, ") + " mt.") + MessageItemColumns.KEY_IS_FLAGGED) + " as mtisf, ") + " mt.") + MessageItemColumns.KEY_UNREAD_COUNT) + " as mtuc, ") + " mt.") + MessageItemColumns.KEY_PENDING_DOCS) + " as mtpd, ") + " mt.") + MessageItemColumns.KEY_HAS_ATTACHEMENTS) + " as mtha, ") + " mt.") + MessageItemColumns.KEY_TIMESTAMP) + " as mttime, ") + " mt.") + MessageItemColumns.KEY_DATETIME) + " as mtdtime, ") + " gc._id as gc_id, gc.id as gcid, gc.name as gcname,") + " gc.profile_pic as gcpic, gc.type as gctype, ") + " cc.profile_pic as ccpic") + "  FROM  ") + TABLE_MESSAGESCHATGROUP) + " mt ") + "left outer join ") + TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN) + " mtg on mt.[_id] = mtg.[msgthread_id] ") + "left outer join ") + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS) + " gc on gc.[_id] = mtg.[") + MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID) + "] ") + "left outer join ") + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT) + " cc on cc.[") + ContactItemColumns.KEY_INTELLIBITZ_ID) + "] = mt.[") + MessageItemColumns.KEY_CHAT_ID) + "] ")
            if (((selection != null) && !selection.empty)) {
                selectQuery += (" WHERE " + selection)
            }
            if (((sortOrder != null) && !sortOrder.empty)) {
                selectQuery += (" ORDER BY " + sortOrder)
            }
            return databaseHelper.rawQuery(selectQuery, selectionArgs)
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
        @Throws(CloneNotSupportedException::class)
        fun fillMessageItemFromAllJoinCursor(messageThreadItem: MessageItem, cursor: Cursor): MessageItem {
            messageThreadItem._id = cursor.getLong(cursor.getColumnIndex("mt_id"))
            messageThreadItem.threadId = cursor.getString(cursor.getColumnIndex("mthid"))
            messageThreadItem.threadIdRef = cursor.getString(cursor.getColumnIndex("mthidref"))
            messageThreadItem.threadIdParts = cursor.getString(cursor.getColumnIndex("mthidparts"))
            messageThreadItem.groupId = cursor.getString(cursor.getColumnIndex("mtgid"))
            messageThreadItem.groupIdRef = cursor.getString(cursor.getColumnIndex("mtgidref"))
            messageThreadItem.intellibitzId = cursor.getString(cursor.getColumnIndex("mtclutid"))
            messageThreadItem.group = cursor.getInt(cursor.getColumnIndex("mtisgroup"))
            messageThreadItem.emailItem = cursor.getInt(cursor.getColumnIndex("mtisemail"))
            messageThreadItem.anonymous = cursor.getInt(cursor.getColumnIndex("mtisanon"))
            messageThreadItem.device = cursor.getInt(cursor.getColumnIndex("mtisdev"))
            messageThreadItem.cloud = cursor.getInt(cursor.getColumnIndex("mtiscloud"))
            messageThreadItem.firstName = cursor.getString(cursor.getColumnIndex("mtfirst"))
            messageThreadItem.lastName = cursor.getString(cursor.getColumnIndex("mtlast"))
            messageThreadItem.displayName = cursor.getString(cursor.getColumnIndex("mtdisplay"))
            messageThreadItem.deviceContactId = cursor.getInt(cursor.getColumnIndex("mtdevcid"))
            messageThreadItem.dataId = cursor.getString(cursor.getColumnIndex("mtid"))
            messageThreadItem.baseType = cursor.getString(cursor.getColumnIndex("mtbaset"))
            messageThreadItem.toUid = cursor.getString(cursor.getColumnIndex("mttuid"))
            messageThreadItem.fromUid = cursor.getString(cursor.getColumnIndex("mtfuid"))
            messageThreadItem.toChatUid = cursor.getString(cursor.getColumnIndex("mttcuid"))
            messageThreadItem.chatId = cursor.getString(cursor.getColumnIndex("mtcuid"))
            messageThreadItem.type = cursor.getString(cursor.getColumnIndex("mttype"))
            messageThreadItem.toType = cursor.getString(cursor.getColumnIndex("mttotype"))
            messageThreadItem.docOwnerEmail = cursor.getString(cursor.getColumnIndex("mtdoe"))
            messageThreadItem.docSenderEmail = cursor.getString(cursor.getColumnIndex("mtdse"))
            messageThreadItem.docSender = cursor.getString(cursor.getColumnIndex("mtds"))
            messageThreadItem.subject = cursor.getString(cursor.getColumnIndex("mtsub"))
            messageThreadItem.docType = cursor.getString(cursor.getColumnIndex("mtdoct"))
            messageThreadItem.dataRev = cursor.getString(cursor.getColumnIndex("mtrev"))
            messageThreadItem.name = cursor.getString(cursor.getColumnIndex("mtname"))
            messageThreadItem.docOwner = cursor.getString(cursor.getColumnIndex("mtdo"))
            messageThreadItem.from = cursor.getString(cursor.getColumnIndex("mtfrom"))
            messageThreadItem.to = cursor.getString(cursor.getColumnIndex("mtto"))
            messageThreadItem.cc = cursor.getString(cursor.getColumnIndex("mtcc"))
            messageThreadItem.bcc = cursor.getString(cursor.getColumnIndex("mtbcc"))
            messageThreadItem.latestMessageText = cursor.getString(cursor.getColumnIndex("mtlate"))
            messageThreadItem.latestMessageTimestamp = cursor.getLong(cursor.getColumnIndex("mtlatets"))
            messageThreadItem.read = cursor.getInt(cursor.getColumnIndex("mtisr"))
            messageThreadItem.delivered = cursor.getInt(cursor.getColumnIndex("mtisd"))
            messageThreadItem.flagged = cursor.getInt(cursor.getColumnIndex("mtisf"))
            messageThreadItem.unreadCount = cursor.getInt(cursor.getColumnIndex("mtuc"))
            messageThreadItem.pendingDocs = cursor.getInt(cursor.getColumnIndex("mtpd"))
            messageThreadItem.hasAttachments = cursor.getInt(cursor.getColumnIndex("mtha"))
            messageThreadItem.timestamp = cursor.getLong(cursor.getColumnIndex("mttime"))
            messageThreadItem.dateTime = cursor.getString(cursor.getColumnIndex("mtdtime"))
            var contactThreadItem: ContactItem = messageThreadItem.contactItem
            if ((null == contactThreadItem)) {
                contactThreadItem = ContactItem()
                messageThreadItem.contactItem = contactThreadItem
            }
            do {
                var gcid: String = cursor.getString(cursor.getColumnIndex("gcid"))
                if ((gcid != null)) {
                    contactThreadItem._id = cursor.getLong(cursor.getColumnIndex("gc_id"))
                    contactThreadItem.dataId = cursor.getString(cursor.getColumnIndex("gcid"))
                    contactThreadItem.name = cursor.getString(cursor.getColumnIndex("gcname"))
                    contactThreadItem.profilePic = cursor.getString(cursor.getColumnIndex("gcpic"))
                    contactThreadItem.type = cursor.getString(cursor.getColumnIndex("gctype"))
                    var akid: String = cursor.getString(cursor.getColumnIndex("akid"))
                    var contactItem: ContactItem = contactThreadItem.getContactItem(akid)
                    if (((null == contactItem) && !TextUtils.isEmpty(akid))) {
                        contactItem = ContactItem()
                        contactItem.dataId = akid
                        contactThreadItem.addContact(contactItem)
                    }
                    if ((contactItem != null)) {
                        contactItem._id = cursor.getLong(cursor.getColumnIndex("ak_id"))
                        contactItem.name = cursor.getString(cursor.getColumnIndex("akname"))
                        contactItem.type = cursor.getString(cursor.getColumnIndex("aktype"))
                        contactItem.profilePic = cursor.getString(cursor.getColumnIndex("akpic"))
                        contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex("akonid"))
                        contactItem.status = cursor.getString(cursor.getColumnIndex("akstatus"))
                        var etypeid: String = cursor.getString(cursor.getColumnIndex("etypeid"))
                        contactItem.typeId = etypeid
                    }
                }
                var mid: String = cursor.getString(cursor.getColumnIndex("mid"))
                if ((mid != null)) {
                    var messageItem: MessageItem = messageThreadItem.getMessage(mid)
                    if ((null == messageItem)) {
                        messageItem = messageThreadItem.addMessage(mid)
                    }
                    if ((messageItem != null)) {
                        messageItem._id = cursor.getLong(cursor.getColumnIndex("m_id"))
                        messageItem.pendingDocs = cursor.getInt(cursor.getColumnIndex("mpd"))
                        messageItem.fromName = cursor.getString(cursor.getColumnIndex("mfn"))
                        messageItem.fromUid = cursor.getString(cursor.getColumnIndex("mfuid"))
                        messageItem.text = cursor.getString(cursor.getColumnIndex("mtxt"))
                        messageItem.docOwnerEmail = cursor.getString(cursor.getColumnIndex("mdoe"))
                        messageItem.docSenderEmail = cursor.getString(cursor.getColumnIndex("mdse"))
                        messageItem.messageDirection = cursor.getString(cursor.getColumnIndex("mdir"))
                        messageItem.messageAttachId = cursor.getString(cursor.getColumnIndex("maid"))
                        messageItem.hasAttachments = cursor.getInt(cursor.getColumnIndex("mha"))
                        messageItem.read = cursor.getInt(cursor.getColumnIndex("misr"))
                        messageItem.delivered = cursor.getInt(cursor.getColumnIndex("misd"))
                        messageItem.flagged = cursor.getInt(cursor.getColumnIndex("misf"))
                        messageItem.timestamp = cursor.getLong(cursor.getColumnIndex("mtime"))
                        var aid: String = cursor.getString(cursor.getColumnIndex("aid"))
                        var attachmentItem: MessageItem = messageItem.getAttachment(aid)
                        if (((null == attachmentItem) && (aid != null))) {
                            attachmentItem = messageItem.addAttachment(aid)
                        }
                        if ((attachmentItem != null)) {
                            attachmentItem._id = cursor.getLong(cursor.getColumnIndex("a_id"))
                            attachmentItem.msgAttachID = cursor.getString(cursor.getColumnIndex("amid"))
                            attachmentItem.partID = cursor.getString(cursor.getColumnIndex("apid"))
                            attachmentItem.name = cursor.getString(cursor.getColumnIndex("aname"))
                            attachmentItem.type = cursor.getString(cursor.getColumnIndex("atype"))
                            attachmentItem.subType = cursor.getString(cursor.getColumnIndex("astype"))
                            attachmentItem.size = cursor.getInt(cursor.getColumnIndex("asize"))
                            attachmentItem.encoding = cursor.getString(cursor.getColumnIndex("aenc"))
                            attachmentItem.downloadURL = cursor.getString(cursor.getColumnIndex("aurl"))
                        }
                    }
                }
            } while (cursor.moveToNext())
            return messageThreadItem
        }
        @Throws(IOException::class)
        fun saveMessageChatThreadItemToDB(messageItem: MessageItem, context: Context): Uri {
            var msgThreadContentValues: ContentValues = ContentValues()
            msgThreadContentValues.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItem))
            var uri: Uri = context.getApplicationContext()
            Log.e(TAG, ("MessageChatThread saved: " + uri))
            return uri
        }
        @Throws(IOException::class)
        fun updateMessageChatThreadReadItems(messageItem: MessageItem, context: Context): Int {
            var values: ContentValues = ContentValues()
            values.put(MessageItemColumns.KEY_UNREAD_COUNT, messageItem.unreadCount)
            return context.getApplicationContext()
        }
        fun queryMessageChatThreadFullJoin(messageItem: MessageItem, context: Context): MessageItem {
            var id: String = messageItem.dataId
            if ((null == id)) {
                Log.e(MessagesChatGroupContentProvider.TAG, ("Query fails, ID null: " + messageItem))
                return messageItem
            }
            var uri: Uri = Uri.withAppendedPath(MessagesChatGroupContentProvider.JOIN_CONTENT_URI, Uri.encode(id))
            var selection: String = (((((" ( ak." + MessageItemColumns.KEY_IS_GROUP) + " = 0 OR ") + " ak.") + MessageItemColumns.KEY_IS_GROUP) + " IS NULL ) ")
            selection += ((" AND mt." + IntellibitzItemColumns.KEY_DATA_ID) + " = ? ")
            var selectionArgs: Array<String> = arrayOf(id)
            var sortOrder: String = (("mt." + MessageItemColumns.KEY_TIMESTAMP) + " ASC")
            var cursor: Cursor = context.getApplicationContext()
            if ((cursor != null)) {
                try {
                    fillMessageItemFromAllJoinCursor(messageItem, cursor)
                }
                catch (e: CloneNotSupportedException) {
                    e.printStackTrace()
                }
                cursor.close()
            }
            return messageItem
        }
        fun createMessageChatThread(uid: String, item: MessageItem): MessageItem {
            item.docType = "MSG"
            item.messageType = "CHAT"
            item.type = "CHAT"
            item.toType = "GROUP"
            item.chatId = uid
            item.threadId = uid
            item.threadIdRef = uid
            item.toUid = uid
            item.toChatUid = uid
            item.dataRev = "1"
            item.hasAttachments = 0
            item.latestMessageText = ""
            item.timestamp = System.currentTimeMillis()
            return item
        }
    }
    private var databaseHelper: DatabaseHelper? = null
    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(getContext(), DatabaseHelper.DATABASE_NAME)
        return true
    }
    override fun getType(uri: Uri): String {
        when (URI_MATCHER.match(uri)) {
            MESSAGECHATGROUP_DIR_TYPE -> {
                return MESSAGECHATGROUP_DIR_MIME_TYPE
            }
            MESSAGECHATGROUP_JOIN_DIR_TYPE -> {
                return MESSAGECHATGROUP_JOIN_DIR_MIME_TYPE
            }
            MESSAGECHATGROUP_ITEM_TYPE -> {
                return MESSAGECHATGROUP_ITEM_MIME_TYPE
            }
            MESSAGECHATGROUP_DATA_ITEM_TYPE -> {
                return MESSAGECHATGROUP_DATA_ITEM_MIME_TYPE
            }
            MESSAGECHATGROUP_JOIN_ITEM_TYPE -> {
                return MESSAGECHATGROUP_JOIN_ITEM_MIME_TYPE
            }
            MESSAGECHATGROUP_JOIN_DATA_ITEM_TYPE -> {
                return MESSAGECHATGROUP_JOIN_DATA_ITEM_MIME_TYPE
            }
            MESSAGECHATGROUP_RAW_DIR_TYPE -> {
                return MESSAGECHATGROUP_RAW_DIR_MIME_TYPE
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
            MESSAGECHATGROUP_DIR_TYPE, MESSAGECHATGROUP_ITEM_TYPE, MESSAGECHATGROUP_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_MESSAGECHATGROUP, projection, selection, selectionArgs, sortOrder)
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
            MESSAGECHATGROUP_JOIN_DIR_TYPE -> {
                try {
                    cursor = getMessageShalGroupJoin(databaseHelper, selection, selectionArgs, sortOrder)
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
            MESSAGECHATGROUP_JOIN_ITEM_TYPE, MESSAGECHATGROUP_JOIN_DATA_ITEM_TYPE -> {
                try {
                    cursor = getMessageJoin(databaseHelper, selection, selectionArgs, sortOrder)
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
            MESSAGECHATGROUP_RAW_DIR_TYPE -> {
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
        when (URI_MATCHER.match(uri)) {
            MESSAGECHATGROUP_DIR_TYPE, MESSAGECHATGROUP_ITEM_TYPE, MESSAGECHATGROUP_DATA_ITEM_TYPE -> {
                var vals: Array<Byte> = values.getAsByteArray(BaseItem.THREAD)
                var vals2: Array<Byte> = values.getAsByteArray(MessageItem.TAG)
                try {
                    var messageThreadItem: MessageItem = (MainApplicationSingleton.Serializer.deserialize(vals) as MessageItem)
                    var messageItem: MessageItem = (MainApplicationSingleton.Serializer.deserialize(vals2) as MessageItem)
                    var id: Long = createOrUpdateMessagesMessage(databaseHelper, messageItem, messageThreadItem)
                    var insertUri: Uri = ContentUris.withAppendedId(uri, id)
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
        when (URI_MATCHER.match(uri)) {
            MESSAGECHATGROUP_DIR_TYPE, MESSAGECHATGROUP_ITEM_TYPE, MESSAGECHATGROUP_DATA_ITEM_TYPE -> {
                var vals2: Array<Byte> = values.getAsByteArray(MessageItem.TAG)
                try {
                    var rows: Int = 0
                    var messageItem: MessageItem = (MainApplicationSingleton.Serializer.deserialize(vals2) as MessageItem)
                    var attachments: Set<MessageItem> = messageItem.attachments
                    if (((null == attachments) || attachments.empty)) {
                        rows = databaseHelper.update(TABLE_MESSAGECHATGROUP, fillContentValuesForMsgRefUpdate(messageItem), (MessageItemColumns.KEY_MSG_REF + " = ?"), arrayOf(messageItem.msgRef))
                    }
                    else {
                        rows = updateMessageThreadMessageAttachmentsURL(databaseHelper, messageItem)
                    }
                    var context: Context = getContext()
                    if ((null != context)) {

                    }
                    return rows
                }
                catch (e: SQLException | IOException | ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, e.getMessage())
                }
                break
            }
            MESSAGECHATGROUP_RAW_DIR_TYPE -> {
                try {
                    var row: Int = databaseHelper.update(TABLE_MESSAGECHATGROUP, values, where, whereArgs)
                    var updUri: Uri = ContentUris.withAppendedId(MessagesChatGroupContentProvider.CONTENT_URI, row)
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
    override fun delete(uri: Uri, where: String, whereArgs: Array<String>): Int {
        var id: Int = 0
        var delUri: Uri = null
        when (URI_MATCHER.match(uri)) {
            MESSAGECHATGROUP_DIR_TYPE, MESSAGECHATGROUP_ITEM_TYPE -> {
                try {
                    id = databaseHelper.delete(TABLE_MESSAGECHATGROUP, where, whereArgs)
                    delUri = ContentUris.withAppendedId(MessageChatGroupContentProvider.CONTENT_URI, id)
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
            MESSAGECHATGROUP_DATA_ITEM_TYPE -> {
                try {
                    var rows: Array<Long> = deleteMessages(databaseHelper, whereArgs)
                    delUri = ContentUris.withAppendedId(MessagesChatGroupContentProvider.CONTENT_URI, rows[0])
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
            MESSAGECHATGROUP_JOIN_DIR_TYPE, MESSAGECHATGROUP_JOIN_ITEM_TYPE, MESSAGECHATGROUP_JOIN_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper.delete(TABLE_MESSAGECHATGROUP, where, whereArgs)
                    delUri = ContentUris.withAppendedId(MessageChatGroupContentProvider.CONTENT_URI, id)
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
        return id
    }
    fun getOpenHelperForTest(): DatabaseHelper {
        return databaseHelper
    }
}
