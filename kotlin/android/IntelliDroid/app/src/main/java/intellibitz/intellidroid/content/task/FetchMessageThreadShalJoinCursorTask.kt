package intellibitz.intellidroid.content.task

import android.content.Context
import android.os.AsyncTask
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.IntellibitzItemColumns
import intellibitz.intellidroid.db.MessageItemColumns

class FetchMessageThreadShalJoinCursorTask(
    private val filter: String?,
    private val context: Context
) : AsyncTask<Void?, Void?, Boolean>() {

    companion object {
        private const val TAG = "FetchMessageThreadShalJoinCursorTask"
    }

    private var messageBeen: List<MessageItem>? = null
    private var fetchCursorTaskListener: FetchCursorTaskListener? = null

    fun setFetchCursorTaskListener(listener: FetchCursorTaskListener?) {
        this.fetchCursorTaskListener = listener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val selection = (" ( " +
                "mt." + MessageItemColumns.KEY_SUBJECT +
                " IS NULL OR " +
                "mt." + MessageItemColumns.KEY_SUBJECT +
                " like ? ) "
                + " AND ( " +
                "mt." + IntellibitzItemColumns.KEY_DOC_TYPE + " = 'THREAD' ) ")
        val selArgs = if (!filter.isNullOrEmpty()) {
            arrayOf("%$filter%")
        } else {
            arrayOf("%%")
        }
        val cursor = context.contentResolver.query(
            MessageChatContentProvider.JOIN_CONTENT_URI,
            null,
            selection, selArgs,
            "mt." + MessageItemColumns.KEY_TIMESTAMP + " DESC"
        )
        messageBeen = MessageChatContentProvider.fillMessageThreadItemsFromCursor(cursor)
        cursor?.close()
        return !messageBeen.isNullOrEmpty()
    }

    override fun onPostExecute(success: Boolean) {
        fetchCursorTaskListener?.setFetchCursorTaskToNull()
        if (success) {
            fetchCursorTaskListener?.onFetchCursorTaskExecute(messageBeen)
        } else {
            fetchCursorTaskListener?.onFetchCursorTaskExecuteFail(messageBeen)
        }
    }

    override fun onCancelled() {
        fetchCursorTaskListener?.setFetchCursorTaskToNull()
    }

    interface FetchCursorTaskListener {
        fun onFetchCursorTaskExecute(result: List<MessageItem>?)
        fun onFetchCursorTaskExecuteFail(result: List<MessageItem>?)
        fun setFetchCursorTaskToNull()
    }
}
