package intellibitz.intellidroid.content.task

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MessagesChatContentProvider
import intellibitz.intellidroid.content.MessagesEmailContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns

class FetchPeopleDetailTask(
    private val messageItem: MessageItem?,
    private val filter: String?,
    private val context: Context
) : AsyncTask<Void?, Void?, Boolean>() {

    companion object {
        private const val TAG = "FetchMessageThreadShalJoinCursorTask"
    }

    private var fetchMsgsGrpPeopleDetailTaskListener: FetchMsgsGrpPeopleDetailTaskListener? = null

    fun setFetchMsgsGrpPeopleDetailTaskListener(listener: FetchMsgsGrpPeopleDetailTaskListener?) {
        this.fetchMsgsGrpPeopleDetailTaskListener = listener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        if (null == messageItem) return false
        var chatId = messageItem.chatId
        if (TextUtils.isEmpty(chatId)) return false

        var selection = (" ( " +
                "m." + MessageItemColumns.KEY_TEXT +
                " IS NULL OR " +
                "m." + MessageItemColumns.KEY_TEXT +
                " like ? ) AND " +
                " ( ak." + MessageItemColumns.KEY_IS_GROUP +
                " = 0 OR " +
                " ak." + MessageItemColumns.KEY_IS_GROUP +
                " IS NULL ) AND " +
                "mt." + MessageItemColumns.KEY_CHAT_ID + " = ? ")
        var selArgs: Array<String> = if (!filter.isNullOrEmpty()) {
            arrayOf("%$filter%", chatId!!)
        } else {
            arrayOf("%%", chatId!!)
        }
        var sortOrder = "m." + MessageItemColumns.KEY_TIMESTAMP + " ASC"
        var uri = Uri.withAppendedPath(
            MessagesChatContentProvider.JOIN_CONTENT_URI,
            Uri.encode(chatId)
        )
        var cursor = context.contentResolver.query(
            uri, null, selection, selArgs, sortOrder
        )
        if (cursor != null && cursor.count > 0) {
            try {
                MessageChatContentProvider.fillMessagesFromAllJoinCursor(messageItem, cursor)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }
        cursor?.close()

        selection = (" ( " +
                MessageItemColumns.KEY_SUBJECT +
                " IS NULL OR " +
                MessageItemColumns.KEY_SUBJECT +
                " like ? ) AND " +
                MessageItemColumns.KEY_TYPE +
                " = ? "
                + " AND ( " +
                MessageItemColumns.KEY_DEVICE_CONTACTID + " = ? ) "
                + " AND ( " +
                MessageItemColumns.KEY_DOC_TYPE + " = 'MSG' ) ")
        val deviceContactId = messageItem.deviceContactId
        if (0L == deviceContactId) return false
        if (deviceContactId > 0) {
            chatId = deviceContactId.toString()
            selArgs = if (!filter.isNullOrEmpty()) {
                arrayOf("%$filter%", "EMAIL", chatId)
            } else {
                arrayOf("%%", "EMAIL", chatId)
            }
            sortOrder = MessageItemColumns.KEY_TIMESTAMP + " ASC"
            uri = Uri.withAppendedPath(
                MessagesEmailContentProvider.CONTENT_URI,
                Uri.encode(chatId)
            )
            cursor = context.contentResolver.query(
                uri, null, selection, selArgs, sortOrder
            )
            if (cursor != null && cursor.count > 0) {
                try {
                    MessageEmailContentProvider.fillMessagesFromCursor(messageItem, cursor)
                } catch (e: CloneNotSupportedException) {
                    e.printStackTrace()
                }
            }
            cursor?.close()
        }
        return true
    }

    override fun onPostExecute(success: Boolean) {
        fetchMsgsGrpPeopleDetailTaskListener?.setFetchMsgsGrpPeopleDetailTaskToNull()
        if (success) {
            fetchMsgsGrpPeopleDetailTaskListener?.onFetchMsgsGrpPeopleDetailTaskExecute(messageItem)
        } else {
            fetchMsgsGrpPeopleDetailTaskListener?.onFetchMsgsGrpPeopleDetailTaskExecuteFail(messageItem)
        }
    }

    override fun onCancelled() {
        fetchMsgsGrpPeopleDetailTaskListener?.setFetchMsgsGrpPeopleDetailTaskToNull()
    }

    interface FetchMsgsGrpPeopleDetailTaskListener {
        fun onFetchMsgsGrpPeopleDetailTaskExecute(messageItem: MessageItem?)
        fun onFetchMsgsGrpPeopleDetailTaskExecuteFail(messageItem: MessageItem?)
        fun setFetchMsgsGrpPeopleDetailTaskToNull()
    }
}
