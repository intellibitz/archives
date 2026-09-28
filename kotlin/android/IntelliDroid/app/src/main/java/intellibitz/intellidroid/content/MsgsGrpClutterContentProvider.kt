

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
import java.util.Collection
import java.util.HashSet
import java.util.Set
import androidx.annotation.NonNull
import androidx.annotation.Nullable

class MsgsGrpClutterContentProvider : ContentProvider() {
    companion object {
        const val TAG = "MsgsGrpClutterCP"
        const val TABLE_MSGSGRPCLUTTER = "msgsgrpclutter"
        const val CREATE_TABLE_MSGSGRPCLUTTER = (("CREATE TABLE " + TABLE_MSGSGRPCLUTTER) + MessageItemColumns.MESSAGECHAT_SCHEMA)
        const val SCHEME = "content://"
        const val MESSAGE_THREADS_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all")
        const val MESSAGE_THREADS_JOIN_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join")
        const val MESSAGE_THREADS_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id")
        const val MESSAGE_THREADS_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id")
        const val MESSAGE_THREADS_JOIN_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/_id")
        const val MESSAGE_THREADS_JOIN_DATA_ITEM_MIME_TYPE = (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/join/id")
        const val MESSAGE_THREADS_RAW_DIR_MIME_TYPE = (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/raw/all")
        const val AUTHORITY = "intellibitz.intellidroid.content.MsgsGrpClutterContentProvider"
        val CONTENT_URI: Uri = Uri.parse((((SCHEME + AUTHORITY) + "/") + TABLE_MSGSGRPCLUTTER))
        val JOIN_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "join_") + TABLE_MSGSGRPCLUTTER))
        val RAW_CONTENT_URI: Uri = Uri.parse(((((SCHEME + AUTHORITY) + "/") + "raw_") + TABLE_MSGSGRPCLUTTER))
        private const val MESSAGE_THREADS_DIR_TYPE = 1
        private const val MESSAGE_THREADS_JOIN_DIR_TYPE = 2
        private const val MESSAGE_THREADS_ITEM_TYPE = 3
        private const val MESSAGE_THREADS_DATA_ITEM_TYPE = 4
        private const val MESSAGE_THREADS_JOIN_ITEM_TYPE = 5
        private const val MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE = 6
        private const val MESSAGE_THREADS_RAW_DIR_TYPE = 7
        private const val SEARCH_SUGGEST = 8
        private const val REFRESH_SHORTCUT = 9
        private val URI_MATCHER: UriMatcher = buildUriMatcher()
        private fun buildUriMatcher(): UriMatcher {
            var matcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(AUTHORITY, TABLE_MSGSGRPCLUTTER, MESSAGE_THREADS_DIR_TYPE)
            matcher.addURI(AUTHORITY, ("join_" + TABLE_MSGSGRPCLUTTER), MESSAGE_THREADS_JOIN_DIR_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MSGSGRPCLUTTER + "/#"), MESSAGE_THREADS_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (TABLE_MSGSGRPCLUTTER + "/*"), MESSAGE_THREADS_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MSGSGRPCLUTTER) + "/#"), MESSAGE_THREADS_JOIN_ITEM_TYPE)
            matcher.addURI(AUTHORITY, (("join_" + TABLE_MSGSGRPCLUTTER) + "/*"), MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE)
            matcher.addURI(AUTHORITY, ("raw_" + TABLE_MSGSGRPCLUTTER), MESSAGE_THREADS_RAW_DIR_TYPE)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_QUERY + "/*"), SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, (SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*"), REFRESH_SHORTCUT)
            return matcher
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
        @Throws(CloneNotSupportedException::class)
        fun fillMessageThreadItemFromAllJoinCursor(messageThreadItem: MessageItem, cursor: Cursor): MessageItem {
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
                var email: String = cursor.getString(cursor.getColumnIndex("email"))
                var ename: String = cursor.getString(cursor.getColumnIndex("ename"))
                var etype: String = cursor.getString(cursor.getColumnIndex("etype"))
                if ((email != null)) {
                    var contactItem: ContactItem = ContactItem()
                    contactItem.dataId = email
                    contactItem.typeId = email
                    contactItem.intellibitzId = email
                    contactItem.name = ename
                    contactItem.type = etype
                    contactThreadItem.addContact(contactItem)
                }
                var gcid: String = cursor.getString(cursor.getColumnIndex("gcid"))
                if ((gcid != null)) {
                    contactThreadItem._id = cursor.getLong(cursor.getColumnIndex("gc_id"))
                    contactThreadItem.dataId = cursor.getString(cursor.getColumnIndex("gcid"))
                    contactThreadItem.name = cursor.getString(cursor.getColumnIndex("gcname"))
                    contactThreadItem.profilePic = cursor.getString(cursor.getColumnIndex("gcpic"))
                    contactThreadItem.type = cursor.getString(cursor.getColumnIndex("gctype"))
                    var intellibitzId: String = cursor.getString(cursor.getColumnIndex("akid"))
                    var contactItem: ContactItem = contactThreadItem.getContactItem(intellibitzId)
                    if (((null == contactItem) && (intellibitzId != null))) {
                        var intellibitzContactItem: ContactItem = ContactItem(intellibitzId)
                        contactItem = ContactItem(intellibitzContactItem)
                        contactThreadItem.addContact(contactItem)
                    }
                    if ((contactItem != null)) {
                        contactItem._id = cursor.getLong(cursor.getColumnIndex("ak_id"))
                        contactItem.dataId = cursor.getString(cursor.getColumnIndex("akid"))
                        contactItem.name = cursor.getString(cursor.getColumnIndex("akname"))
                        contactItem.type = cursor.getString(cursor.getColumnIndex("aktype"))
                        contactItem.intellibitzId = cursor.getString(cursor.getColumnIndex("akonid"))
                        contactItem.status = cursor.getString(cursor.getColumnIndex("akstatus"))
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
        @Throws(IOException::class)
        fun saveMessageThreadItemToDB(messageItem: MessageItem, context: Context): Uri {
            var msgThreadContentValues: ContentValues = ContentValues()
            msgThreadContentValues.put(MessageItem.TAG, MainApplicationSingleton.Serializer.serialize(messageItem))
            var uri: Uri = context.getApplicationContext()
            Log.e(TAG, ("MessageThread saved: " + uri))
            return uri
        }
        fun queryMessageThreadFullJoin(messageItem: MessageItem, context: Context): MessageItem {
            var id: String = messageItem.dataId
            if ((null == id)) {
                Log.e(TAG, ("Query fails, ID null: " + messageItem))
                return messageItem
            }
            var uri: Uri = Uri.withAppendedPath(JOIN_CONTENT_URI, Uri.encode(id))
            var selection: String = (("mt." + MessageItemColumns.KEY_DATA_ID) + " = ? ")
            var selectionArgs: Array<String> = arrayOf(id)
            var sortOrder: String = (("mt." + MessageItemColumns.KEY_TIMESTAMP) + " ASC")
            var cursor: Cursor = context.getApplicationContext()
            if ((cursor != null)) {
                try {
                    fillMessageThreadItemFromAllJoinCursor(messageItem, cursor)
                }
                catch (e: CloneNotSupportedException) {
                    e.printStackTrace()
                }
                cursor.close()
            }
            return messageItem
        }
        override fun query(messageItem: MessageItem, context: Context): MessageItem {
            var id: String = messageItem.dataId
            if ((null == id)) {
                Log.e(TAG, ("Query fails, ID null: " + messageItem))
                return messageItem
            }
            var uri: Uri = Uri.withAppendedPath(CONTENT_URI, Uri.encode(id))
            var selection: String = (MessageItemColumns.KEY_DATA_ID + " = ? ")
            var selectionArgs: Array<String> = arrayOf(id)
            var sortOrder: String = (MessageItemColumns.KEY_TIMESTAMP + " ASC")
            var cursor: Cursor = context.getApplicationContext()
            if ((cursor != null)) {
                fillsMessageItemFromCursor(cursor, messageItem)
                cursor.close()
            }
            return messageItem
        }
        @Throws(IOException::class)
        fun deleteMsgs(item: Array<String>, context: Context): Int {
            return context.getApplicationContext()
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
        fun createOrUpdateMsgsGrpClutter(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messageItem: MessageItem): Long {
            if ((null == messageItem)) {
                return 0
            }
            val contacts: ContactItem = messageItem.contactItem
            if ((null == contacts)) {
                return 0
            }
            var messages: MessageItem = (messageItem.cloneShal() as MessageItem)
            messages._id = 0
            val contactItems: HashSet<ContactItem> = contacts.contactItems
            if (((null == contactItems) || contactItems.empty)) {
                return 0
            }
            var items: Array<ContactItem> = contactItems.toArray(arrayOfNulls<ContactItem>(0))
            for (contactItem in items) {
                if ("from") {
                    savesMsgsGrpClutter(databaseHelper, db, messages, contactItem)
                    if ((contactItem.deviceContactId > 0)) {
                        MsgsGrpPeopleContentProvider.savesMsgsGrpPeople(databaseHelper, db, messages, contactItem)
                    }
                }
            }
            return messageItem._id
        }
        fun savesMsgsGrpClutter(databaseHelper: DatabaseHelper, db: SQLiteDatabase, messages: MessageItem, contactItem: ContactItem) {
            val intellibitzId: String = contactItem.intellibitzId
            var values: ContentValues = ContentValues()
            messages.intellibitzId = intellibitzId
            messages.deviceContactId = contactItem.deviceContactId
            messages.typeId = contactItem.typeId
            messages.device = contactItem.device
            messages.group = contactItem.group
            messages.emailItem = contactItem.emailItem
            messages.firstName = contactItem.firstName
            messages.lastName = contactItem.lastName
            messages.displayName = contactItem.displayName
            MessageEmailContentProvider.fillContentValuesFromMessageItem(messages, values)
            var cursor: Cursor
            cursor = databaseHelper.query(db, TABLE_MSGSGRPCLUTTER, arrayOf(MessageItemColumns.KEY_ID), (MessageItemColumns.KEY_INTELLIBITZ_ID + " = ?"), arrayOf(intellibitzId), null)
            if (((null == cursor) || (0 == cursor.getCount()))) {
                if ((cursor != null)) {
                    cursor.close()
                }
                databaseHelper.insert(db, MsgsGrpClutterContentProvider.TABLE_MSGSGRPCLUTTER, null, values)
            }
            else {
                cursor.close()
                databaseHelper.update(db, MsgsGrpClutterContentProvider.TABLE_MSGSGRPCLUTTER, values, (MessageItemColumns.KEY_INTELLIBITZ_ID + " = ?"), arrayOf(intellibitzId))
            }
        }
        fun queryThreadId(subject: String, email: String, context: Context): String {
            var sql: String = (((((((((("select to_uid from " + TABLE_MSGSGRPCLUTTER) + " where ") + "length( ltrim( ") + "ltrim (ltrim   (lower(?),lower('fwd:')),lower('re:')), ") + "ltrim (ltrim   (lower(?),lower('fwd:')),lower('re:')) ") + ")) = 0 ") + " and ( ") + "from_email = ? or ") + "instr(email_to, ?) <> 0 or ") + "instr(email_cc, ?) <> 0) ")
            var cursor: Cursor = context.getContentResolver()
            if ((null == cursor)) {
                return null
            }
            else {
                var toUid: String = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO_UID))
                cursor.close()
                return toUid
            }
        }
    }
    private var databaseHelper: DatabaseHelper? = null
    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(getContext(), DatabaseHelper.DATABASE_NAME)
        return true
    }
    override fun getType(uri: Uri): String {
        when (URI_MATCHER.match(uri)) {
            MESSAGE_THREADS_DIR_TYPE -> {
                return MESSAGE_THREADS_DIR_MIME_TYPE
            }
            MESSAGE_THREADS_JOIN_DIR_TYPE -> {
                return MESSAGE_THREADS_JOIN_DIR_MIME_TYPE
            }
            MESSAGE_THREADS_ITEM_TYPE -> {
                return MESSAGE_THREADS_ITEM_MIME_TYPE
            }
            MESSAGE_THREADS_DATA_ITEM_TYPE -> {
                return MESSAGE_THREADS_DATA_ITEM_MIME_TYPE
            }
            MESSAGE_THREADS_JOIN_ITEM_TYPE -> {
                return MESSAGE_THREADS_JOIN_ITEM_MIME_TYPE
            }
            MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE -> {
                return MESSAGE_THREADS_JOIN_DATA_ITEM_MIME_TYPE
            }
            MESSAGE_THREADS_RAW_DIR_TYPE -> {
                return MESSAGE_THREADS_RAW_DIR_MIME_TYPE
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
            MESSAGE_THREADS_DIR_TYPE, MESSAGE_THREADS_ITEM_TYPE, MESSAGE_THREADS_DATA_ITEM_TYPE, MESSAGE_THREADS_JOIN_DIR_TYPE, MESSAGE_THREADS_JOIN_ITEM_TYPE, MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    cursor = databaseHelper.query(TABLE_MSGSGRPCLUTTER, projection, selection, selectionArgs, sortOrder)
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
            MESSAGE_THREADS_RAW_DIR_TYPE -> {
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
        var vals: Array<Byte> = values.getAsByteArray(MessageItem.TAG)
        when (URI_MATCHER.match(uri)) {
            MESSAGE_THREADS_DIR_TYPE, MESSAGE_THREADS_ITEM_TYPE, MESSAGE_THREADS_DATA_ITEM_TYPE, MESSAGE_THREADS_JOIN_DIR_TYPE, MESSAGE_THREADS_JOIN_ITEM_TYPE, MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    var id: Long = databaseHelper.insert(TABLE_MSGSGRPCLUTTER, null, values)
                    var insertUri: Uri = ContentUris.withAppendedId(CONTENT_URI, id)
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
            MESSAGE_THREADS_DIR_TYPE, MESSAGE_THREADS_ITEM_TYPE, MESSAGE_THREADS_DATA_ITEM_TYPE, MESSAGE_THREADS_JOIN_DIR_TYPE, MESSAGE_THREADS_JOIN_ITEM_TYPE, MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper.update(TABLE_MSGSGRPCLUTTER, values, where, whereArgs)
                    updateUri = ContentUris.withAppendedId(MsgsGrpClutterContentProvider.CONTENT_URI, id)
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
            MESSAGE_THREADS_DIR_TYPE, MESSAGE_THREADS_ITEM_TYPE, MESSAGE_THREADS_DATA_ITEM_TYPE, MESSAGE_THREADS_JOIN_DIR_TYPE, MESSAGE_THREADS_JOIN_ITEM_TYPE, MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE -> {
                try {
                    id = databaseHelper.delete(TABLE_MSGSGRPCLUTTER, where, whereArgs)
                    delUri = ContentUris.withAppendedId(MsgsGrpClutterContentProvider.CONTENT_URI, id)
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
