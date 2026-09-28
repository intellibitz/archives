package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.db.IntellibitzItemColumns
import intellibitz.intellidroid.db.MessageAttachmentJoinColumns
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.service.EmailService
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 *
 */
class MsgChatAttachmentContentProvider : ContentProvider() {

    companion object {
        const val TAG = "MsgChatAttachmentCP"
        const val TABLE_MSGCHATATTACHMENT = "msgchatattachment"
        const val CREATE_TABLE_MSGCHATATTACHMENT = "CREATE TABLE " + TABLE_MSGCHATATTACHMENT + MessageItemColumns.MESSAGECHAT_SCHEMA
        //    The content provider scheme
        const val SCHEME = "content://"
        // MIME types used for searching words or looking up a single definition
        const val ATTACHMENT_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all"
        const val ATTACHMENT_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id"
        const val ATTACHMENT_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id"
        // UriMatcher stuff
        private const val ATTACHMENT_DIR_TYPE = 1
        private const val ATTACHMENT_ITEM_TYPE = 2
        private const val ATTACHMENT_DATA_ITEM_TYPE = 3
        private const val SEARCH_SUGGEST = 4
        private const val REFRESH_SHORTCUT = 5
        var AUTHORITY = "intellibitz.intellidroid.content.MsgChatAttachmentContentProvider"
        val CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_MSGCHATATTACHMENT)
        private val sURIMatcher = buildUriMatcher()

        /**
         * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
         */
        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            // to get definitions...
            matcher.addURI(AUTHORITY, TABLE_MSGCHATATTACHMENT, ATTACHMENT_DIR_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGCHATATTACHMENT + "/#", ATTACHMENT_ITEM_TYPE)
            matcher.addURI(AUTHORITY, TABLE_MSGCHATATTACHMENT + "/*", ATTACHMENT_DATA_ITEM_TYPE)
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

        fun fillAttachmentItemContentValues(attachmentItem: MessageItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATA_ID, attachmentItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MSGATTCH_ID, attachmentItem.msgAttachID)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_PARTID, attachmentItem.partID)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_TYPE, attachmentItem.docType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TYPE, attachmentItem.type)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MESSAGE_TYPE, attachmentItem.messageType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_NAME, attachmentItem.name)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DESCRIPTION, attachmentItem.description)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_SUBTYPE, attachmentItem.subType)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_ENCODING, attachmentItem.encoding)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LANGUAGE, attachmentItem.language)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MD5, attachmentItem.md5)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOWNLOAD_URL, attachmentItem.downloadURL)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_PIC, attachmentItem.profilePic)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CLOUD_PIC, attachmentItem.cloudPic)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_SIZE, attachmentItem.size)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TIMESTAMP, attachmentItem.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis(attachmentItem.timestamp))
            return values
        }

        fun fillAttachmentsFromCursor(cursor: Cursor?): ArrayList<MessageItem>? {
            if (null == cursor || 0 == cursor.count) return null
            val attachmentItems = ArrayList<MessageItem>(cursor.count)
            cursor.moveToFirst()
            do {
                val attachmentItem = MessageItem()
                attachmentItem._id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                attachmentItem.dataId = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATA_ID))
                attachmentItem.msgAttachID = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_MSGATTCH_ID))
                attachmentItem.partID = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_PARTID))
                attachmentItem.docType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOC_TYPE))
                attachmentItem.type = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TYPE))
                attachmentItem.messageType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_MESSAGE_TYPE))
                attachmentItem.subType = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_SUBTYPE))
                attachmentItem.name = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_NAME))
                attachmentItem.description = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DESCRIPTION))
                attachmentItem.encoding = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_ENCODING))
                attachmentItem.language = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_LANGUAGE))
                attachmentItem.md5 = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_MD5))
                attachmentItem.downloadURL = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DOWNLOAD_URL))
                attachmentItem.profilePic = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_PIC))
                attachmentItem.cloudPic = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_CLOUD_PIC))
                attachmentItem.size = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_SIZE))
                attachmentItem.timestamp = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_TIMESTAMP))
                attachmentItem.dateTime = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATETIME))
                attachmentItems.add(attachmentItem)
            } while (cursor.moveToNext())
            return attachmentItems
        }

        @Throws(JSONException::class)
        fun setAttachmentItemFromJson(attachmentMessage: MessageItem, response: JSONObject) {
            val path = response.getString("path")
            attachmentMessage.downloadURL = path
            val contentType = response.getString("content_type")
            val types = contentType.split("/".toRegex()).toTypedArray()
            attachmentMessage.type = types[0]
            attachmentMessage.subType = types[1]
            val len = response.getInt("content_length")
            attachmentMessage.size = len
            val units = response.getString("content_length_units")
            attachmentMessage.partID = units
            attachmentMessage.messageType = MessageItem.CHAT
        }

        fun getMimeType(item: MessageItem): String? {
            val type = item.type
            val subType = item.subType
            if (null == type && null == subType) return null
            var part1 = ""
            var part2 = ""
            if (type != null) part1 = "$type/"
            if (subType != null) part2 = subType
            return part1 + part2
        }

        fun createOrUpdateMessageAttachments(databaseHelper: DatabaseHelper, items: Set<MessageItem>, id: Long): LongArray {
            val ids = LongArray(items.size)
            var i = 0
            for (attachment in items) {
                ids[i++] = createOrUpdateMessageAttachment(databaseHelper, databaseHelper.writableDatabase, attachment, id)
            }
            return ids
        }

        fun createOrUpdateMessageAttachments(databaseHelper: DatabaseHelper, db: SQLiteDatabase, items: Set<MessageItem>, id: Long): LongArray {
            val ids = LongArray(items.size)
            var i = 0
            for (attachment in items) {
                ids[i++] = createOrUpdateMessageAttachment(databaseHelper, db, attachment, id)
            }
            return ids
        }

        fun createOrUpdateMessageAttachment(databaseHelper: DatabaseHelper, db: SQLiteDatabase, item: MessageItem, id: Long): Long {
//        Cursor cursor = getEmailsCursor(item);
            val values = ContentValues()
//        message thread is the parent, email item child
            fillAttachmentItemContentValues(item, values)

//        Cursor cursor = getAttachmentCursor(databaseHelper, dataId);
//        data id can change.. since the cloud url is different than the local url
//        local url is stored in description
//        // TODO: 11/8/16
//        move local url into a separate column
//        checks if the same email attachment is already present for the message thread
            var dataId = item.dataId
            var cursor = databaseHelper.query(TABLE_MSGCHATATTACHMENT,
                    arrayOf(
                            MessageItemColumns.KEY_ID
                            , MessageItemColumns.KEY_NAME
                            , MessageItemColumns.KEY_DESCRIPTION
                    ),
                    MessageItemColumns.KEY_DATA_ID + " = ?", arrayOf(dataId), null, null, null)
            if (null == cursor || 0 == cursor.count) {
//        // TODO: 11/8/16
//        revisit.. to update attachment, with the right parent message
//        the same file can be attached for different message.. so check the join.. get the msg id
//        then update the attachment that belongs to the correct msg id
//        hack!!
                val bits = dataId.split("//".toRegex()).toTypedArray()
                if (bits.size > 1) {
                    dataId = "/" + bits[bits.size - 1]
                    Log.d(TAG, "createOrUpdateMessageAttachment: $dataId")
                }
                cursor = databaseHelper.query(TABLE_MSGCHATATTACHMENT,
                        arrayOf(
                                MessageItemColumns.KEY_ID
                                , MessageItemColumns.KEY_NAME
                                , MessageItemColumns.KEY_DESCRIPTION
                        ),
                        MessageItemColumns.KEY_DATA_ID + " = ?", arrayOf(dataId), null, null, null)
            }
            val _id: Long
            if (cursor != null && cursor.count > 0) {
                _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                item.name = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_NAME))
                item.description = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DESCRIPTION))
//            // TODO: 11/8/16
//            to set cloud url, pointing to the attachment stored in the cloud
//            currently points to local storage, if the attachment originated from device
//            updateMessageAttachment(item, id);
                cursor.close()
                databaseHelper.update(db, TABLE_MSGCHATATTACHMENT, values,
                        MessageItemColumns.KEY_ID + " = ?",
                        arrayOf(_id.toString()))
                item._id = _id
            } else {
                if (cursor != null) cursor.close()
                _id = databaseHelper.insert(db, TABLE_MSGCHATATTACHMENT, null, values)
                item._id = _id
            }
//        creates the join
            createMessageAttachmentJoin(databaseHelper, db, _id, id)
            return item._id
        }

        /**
         * @param id the message id, the attachment belongs to
         * @return Cursor
         */
        fun getAttachmentCursor(databaseHelper: DatabaseHelper, id: Long): Cursor? {
            return databaseHelper.query(TABLE_MSGCHATATTACHMENT,
                    null,
                    MessageItemColumns.KEY_ID + " = ?", arrayOf(id.toString()),
                    null, null, null)
        }

        fun getAttachmentCursor(databaseHelper: DatabaseHelper, id: String): Cursor? {
            return databaseHelper.query(TABLE_MSGCHATATTACHMENT,
                    arrayOf(
                            MessageItemColumns.KEY_ID
                            , MessageItemColumns.KEY_NAME
                            , MessageItemColumns.KEY_DESCRIPTION
                    ),
                    MessageItemColumns.KEY_DATA_ID + " = ?", arrayOf(id), null, null, null)
        }

        fun getAllMessageAttachmentsCursor(databaseHelper: DatabaseHelper, attachmentItem: MessageItem, id: Long): Cursor? {

            val selectQuery = "SELECT  * FROM " + MessageChatContentProvider.TABLE_MESSAGECHAT + " msgs " +
                    " left join " + MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN +
                    " mta on msgs.[_id] = mta.[msg_id]  " +
                    " left join attachments am  on mta.[attachment_id] = am.[_id]" +
                    " WHERE " + "  am." + MessageItemColumns.KEY_DATA_ID + " = '"
                    + attachmentItem.dataId +
                    "' AND msgs." + MessageItemColumns.KEY_ID + " = '" + id + "'"

//        Log.e(TAG, selectQuery);
            return databaseHelper.rawQuery(selectQuery, null)
        }

        fun updateMessageThreadMessageChatAttachmentURL(databaseHelper: DatabaseHelper, attachmentItem: MessageItem): Long {
            val values = ContentValues()
            values.put(MessageItemColumns.KEY_DOWNLOAD_URL, attachmentItem.downloadURL)
            val msgAttachID = attachmentItem.dataId
            val row = databaseHelper.update(TABLE_MSGCHATATTACHMENT, values,
                    MessageItemColumns.KEY_DATA_ID + " = ? ",
                    arrayOf(msgAttachID))
//        fetch the attachment and send the id back.. for content updates
            if (row > 0) {
                val cursor = databaseHelper.query(TABLE_MSGCHATATTACHMENT,
                        arrayOf(MessageItemColumns.KEY_ID,
                                MessageItemColumns.KEY_DATA_ID
                        ),
                        MessageItemColumns.KEY_DATA_ID + " = ? ",
                        arrayOf(msgAttachID), null)
                val atchid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATA_ID))
                if (msgAttachID == atchid) {
                    return cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
                }
            }
            return 0
        }

        fun getMessageAttachmentJoin(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var _id: Long = 0
            val c = databaseHelper.query(MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN, arrayOf(IntellibitzItemColumns.KEY_ID),
                    MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID + " = ? and " +
                            MessageAttachmentJoinColumns.KEY_MESSAGE_ID + " = ?",
                    arrayOf(id.toString(), fk.toString()), null, null, null)
            if (c != null) {
                if (c.count > 0) _id = c.getLong(c.getColumnIndex("_id"))
                c.close()
            }
            return _id
        }

        fun createMessageAttachmentJoin(databaseHelper: DatabaseHelper, db: SQLiteDatabase, id: Long, fk: Long): Long {
            var _id = getMessageAttachmentJoin(databaseHelper, id, fk)
            val values = ContentValues()
            values.put(MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID, id)
            values.put(MessageAttachmentJoinColumns.KEY_MESSAGE_ID, fk)
            values.put(MessageAttachmentJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis())
            if (0 == _id) {
                _id = databaseHelper.insert(db, MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN, null, values)
            } else {
                databaseHelper.update(db, MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN, values,
                        MessageAttachmentJoinColumns.KEY_ID + " = ?", arrayOf(_id.toString()))
            }
            return _id
        }

        fun createMessageAttachmentJoin(databaseHelper: DatabaseHelper, id: Long, fk: Long): Long {
            var _id = getMessageAttachmentJoin(databaseHelper, id, fk)
            val values = ContentValues()
            values.put(MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID, id)
            values.put(MessageAttachmentJoinColumns.KEY_MESSAGE_ID, fk)
            values.put(MessageAttachmentJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis())
            if (0 == _id) {
                _id = databaseHelper.insert(MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN, null, values)
            } else {
                databaseHelper.update(MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN, values,
                        MessageAttachmentJoinColumns.KEY_ID + " = ?", arrayOf(_id.toString()))
            }
            return _id
        }

        fun asyncUpdateChatAttachmentFilePathInDB(context: Context, user: ContactItem, attachmentItem: MessageItem) {
            try {
//        intent service runs async in its own thread
//            Intent intent = new Intent(context, RcvDocService.class);
//            Intent intent = new Intent(context, RcvDocService.class);
                val intent = Intent(context, EmailService::class.java)
                intent.action = MainApplicationSingleton.INTENT_ACTION_FETCH_CHAT_ATTACHMENT_CLOUD
                intent.putExtra(ContactItem.USER_CONTACT, UserContentProvider.getUserCloneForService(user) as Parcelable)
                intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, attachmentItem as Parcelable)
//        intent.putExtra("ResultReceiver", resultReceiver);
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }
    }

    private var databaseHelper: DatabaseHelper? = null

    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(context, DatabaseHelper.DATABASE_NAME)
        return true
    }

    @Nullable
    override fun getType(@NonNull uri: Uri): String? {
        return when (sURIMatcher.match(uri)) {
            ATTACHMENT_DIR_TYPE -> ATTACHMENT_DIR_MIME_TYPE
            ATTACHMENT_ITEM_TYPE -> ATTACHMENT_ITEM_MIME_TYPE
            ATTACHMENT_DATA_ITEM_TYPE -> ATTACHMENT_DATA_ITEM_MIME_TYPE
            SEARCH_SUGGEST -> SearchManager.SUGGEST_MIME_TYPE
            REFRESH_SHORTCUT -> SearchManager.SHORTCUT_MIME_TYPE
            else -> throw IllegalArgumentException("Unknown URL $uri")
        }
    }

    @Nullable
    override fun query(@NonNull uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        var cursor: Cursor? = null
        when (sURIMatcher.match(uri)) {
            ATTACHMENT_DIR_TYPE, ATTACHMENT_DATA_ITEM_TYPE -> try {
                cursor = databaseHelper?.query(TABLE_MSGCHATATTACHMENT,
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
            ATTACHMENT_ITEM_TYPE -> try {
                cursor = getAttachmentCursor(databaseHelper!!, ContentUris.parseId(uri))
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
        when (sURIMatcher.match(uri)) {
            ATTACHMENT_DIR_TYPE, ATTACHMENT_ITEM_TYPE -> try {
                val vals = values?.getAsByteArray(MessageItem.ATTACHMENT_MESSAGE)
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return uri
    }

    override fun update(@NonNull uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val vals = values?.getAsByteArray(MessageItem.ATTACHMENT_MESSAGE)
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        when (sURIMatcher.match(uri)) {
            ATTACHMENT_DIR_TYPE, ATTACHMENT_ITEM_TYPE -> try {
                val attachmentItem = MainApplicationSingleton.Serializer.deserialize(vals) as MessageItem
                val _id = updateMessageThreadMessageAttachmentURL(databaseHelper!!, attachmentItem)
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(
                            Uri.withAppendedPath(uri, _id.toString()), null)
                }
                return _id.toInt()
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
            ATTACHMENT_DATA_ITEM_TYPE -> try {
                val attachmentItem = MainApplicationSingleton.Serializer.deserialize(vals) as MessageItem
                val _id = updateMessageThreadMessageChatAttachmentURL(databaseHelper!!, attachmentItem)
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(
                            Uri.withAppendedPath(uri, _id.toString()), null)
                }
                return _id.toInt()
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
        return 0
    }

    override fun delete(@NonNull uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
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

    fun updateMessageThreadMessageAttachmentURL(databaseHelper: DatabaseHelper, attachmentItem: MessageItem): Long {
        val values = ContentValues()
        values.put(MessageItemColumns.KEY_DOWNLOAD_URL, attachmentItem.downloadURL)
        val msgAttachID = attachmentItem.msgAttachID
        val partID = attachmentItem.partID
        val attachmentName = attachmentItem.name
        val row = databaseHelper.update(TABLE_MSGCHATATTACHMENT, values,
                MessageItemColumns.KEY_MSGATTCH_ID + " = ? and " +
                        MessageItemColumns.KEY_PARTID + " = ? and " +
                        MessageItemColumns.KEY_NAME + " = ? ",
                arrayOf(msgAttachID, partID, attachmentName))
//        fetch the attachment and send the id back.. for content updates
        if (row > 0) {
            val cursor = databaseHelper.query(TABLE_MSGCHATATTACHMENT,
                    arrayOf(MessageItemColumns.KEY_ID,
                            MessageItemColumns.KEY_MSGATTCH_ID,
                            MessageItemColumns.KEY_PARTID,
                            MessageItemColumns.KEY_NAME),
                    MessageItemColumns.KEY_MSGATTCH_ID + " = ? and " +
                            MessageItemColumns.KEY_PARTID + " = ? and " +
                            MessageItemColumns.KEY_NAME + " = ? ",
                    arrayOf(msgAttachID, partID, attachmentName), null)
            val name = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_NAME))
            val part = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_PARTID))
            val atchid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_MSGATTCH_ID))
            if (attachmentName == name && msgAttachID == atchid && partID == part) {
                return cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID))
            }
        }
        return 0
    }

}
