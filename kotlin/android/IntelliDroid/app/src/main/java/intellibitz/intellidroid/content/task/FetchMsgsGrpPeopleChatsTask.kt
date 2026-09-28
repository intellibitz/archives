package intellibitz.intellidroid.content.task

import android.content.Context
import android.util.Log
import intellibitz.intellidroid.IntellibitzAsyncTask
import intellibitz.intellidroid.content.MsgsGrpPeopleChatsContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import java.util.ArrayList

class FetchMsgsGrpPeopleChatsTask(
    private val filter: String?,
    context: Context?
) : IntellibitzAsyncTask(context) {

    companion object {
        private const val TAG = "MsgsGrpPeopleChatTask"
    }

    private var fetchMsgsGrpPeopleTaskListener: FetchMsgsGrpPeopleTaskListener? = null
    private var messageItems: ArrayList<MessageItem>? = null

    fun setFetchMsgsGrpPeopleTaskListener(listener: FetchMsgsGrpPeopleTaskListener?) {
        this.fetchMsgsGrpPeopleTaskListener = listener
    }

    override fun doInBackground(vararg params: Any?): Boolean {
        var selection = (" ( " +
                MessageItemColumns.KEY_SUBJECT +
                " IS NULL OR " +
                MessageItemColumns.KEY_SUBJECT +
                " like ? ) ")
        val selArgs = if (!filter.isNullOrEmpty()) {
            arrayOf("%$filter%")
        } else {
            arrayOf("%%")
        }
        selection += (" AND ( " +
                MessageItemColumns.KEY_TYPE +
                " = 'CHAT' ) ")
        selection += (" AND ( " +
                MessageItemColumns.KEY_TO_TYPE +
                " = 'USER' ) ")
        selection += (" AND ( " +
                MessageItemColumns.KEY_IS_GROUP +
                " = 0 ) ")
        selection += (" AND ( " +
                MessageItemColumns.KEY_DOC_TYPE +
                " = 'MSG' ) ")
        val ctx = this.context.get()
        if (ctx == null) {
            Log.e(TAG, "Context is null - stopping task")
            return false
        }
        val cursor = ctx.contentResolver.query(
            MsgsGrpPeopleChatsContentProvider.CONTENT_URI,
            null,
            selection, selArgs,
            MessageItemColumns.KEY_TIMESTAMP + " DESC"
        )
        if (cursor != null && cursor.count > 0) {
            messageItems = MsgsGrpPeopleChatsContentProvider.fillMessageThreadItemsFromCursor(cursor)
        }
        cursor?.close()
        return !messageItems.isNullOrEmpty()
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            fetchMsgsGrpPeopleTaskListener?.onFetchMsgsGrpPeopleTaskExecute(messageItems)
        } else {
            fetchMsgsGrpPeopleTaskListener?.onFetchMsgsGrpPeopleTaskExecuteFail(messageItems)
        }
        releaseContext()
    }

    override fun onCancelled() {
        fetchMsgsGrpPeopleTaskListener?.onFetchMsgsGrpPeopleTaskExecuteFail(messageItems)
        releaseContext()
    }

    interface FetchMsgsGrpPeopleTaskListener {
        fun onFetchMsgsGrpPeopleTaskExecute(messageItem: ArrayList<MessageItem>?)
        fun onFetchMsgsGrpPeopleTaskExecuteFail(messageItem: ArrayList<MessageItem>?)
    }
}
