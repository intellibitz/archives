package intellibitz.intellidroid.content.task

import android.content.Context
import android.util.Log
import intellibitz.intellidroid.IntellibitzAsyncTask
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import java.util.ArrayList

class FetchMsgsGrpClutterTask(
    private val filter: String?,
    context: Context?
) : IntellibitzAsyncTask(context) {

    companion object {
        const val TAG = "FetchMsgsGrpClutterTask"
    }

    private var fetchMsgsGrpClutterTaskListener: FetchMsgsGrpClutterTaskListener? = null
    private var messageItems: ArrayList<MessageItem>? = null

    fun setFetchMsgsGrpClutterTaskListener(listener: FetchMsgsGrpClutterTaskListener?) {
        this.fetchMsgsGrpClutterTaskListener = listener
    }

    override fun doInBackground(vararg params: Any?): Boolean {
        var selection = (" ( " + MessageItemColumns.KEY_SUBJECT +
                " IS NULL OR " +
                MessageItemColumns.KEY_SUBJECT +
                " like ? ) ")
        selection += (" AND ( " + MessageItemColumns.KEY_TYPE +
                " = 'EMAIL' ) ")
        selection += (" AND ( " +
                MessageItemColumns.KEY_DEVICE_CONTACTID +
                " = 0 OR " +
                MessageItemColumns.KEY_DEVICE_CONTACTID +
                " IS NULL OR " +
                MessageItemColumns.KEY_IS_GROUP +
                " = 0 ) " +
                " AND ( " +
                MessageItemColumns.KEY_DOC_TYPE +
                " = 'MSG' ) ")
        val selArgs = if (!filter.isNullOrEmpty()) {
            arrayOf("%$filter%")
        } else {
            arrayOf("%%")
        }
        val ctx = this.context.get()
        if (ctx == null) {
            Log.e(TAG, "Context is null - stopping task")
            return false
        }
        val cursor = ctx.contentResolver.query(
            MsgsGrpClutterContentProvider.CONTENT_URI,
            null,
            selection, selArgs,
            MessageItemColumns.KEY_TIMESTAMP + " DESC"
        )
        if (cursor != null && cursor.count > 0) {
            messageItems = MessageChatContentProvider.fillMessageThreadItemsFromCursor(cursor)
        }
        cursor?.close()
        return !messageItems.isNullOrEmpty()
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            fetchMsgsGrpClutterTaskListener?.onFetchMsgsGrpClutterTaskExecute(messageItems)
        } else {
            fetchMsgsGrpClutterTaskListener?.onFetchMsgsGrpClutterTaskExecuteFail(messageItems)
        }
        releaseContext()
    }

    override fun onCancelled() {
        fetchMsgsGrpClutterTaskListener?.onFetchMsgsGrpClutterTaskExecuteFail(messageItems)
        releaseContext()
    }

    interface FetchMsgsGrpClutterTaskListener {
        fun onFetchMsgsGrpClutterTaskExecute(messageItem: ArrayList<MessageItem>?)
        fun onFetchMsgsGrpClutterTaskExecuteFail(messageItem: ArrayList<MessageItem>?)
    }
}
