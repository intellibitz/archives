package intellibitz.intellidroid.content.task

import android.content.Context
import android.util.Log
import intellibitz.intellidroid.IntellibitzAsyncTask
import intellibitz.intellidroid.content.MsgsGrpPeopleContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import java.util.ArrayList

class FetchMsgsGrpPeopleTask(
    private val filter: String?,
    context: Context?
) : IntellibitzAsyncTask(context) {

    companion object {
        const val TAG = "FetchMsgsGrpPeopleTask"
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
        selection += (" AND ( " +
                MessageItemColumns.KEY_TYPE +
                " = 'CHAT' OR " +
                MessageItemColumns.KEY_TYPE +
                " = 'EMAIL' ) ")
        selection += (" AND ( " +
                MessageItemColumns.KEY_DEVICE_CONTACTID +
                " > 0 OR " +
                MessageItemColumns.KEY_IS_GROUP +
                " = 1 ) " +
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
            MsgsGrpPeopleContentProvider.CONTENT_URI,
            null,
            selection, selArgs,
            MessageItemColumns.KEY_TIMESTAMP + " DESC"
        )
        if (cursor != null && cursor.count > 0) {
            messageItems = MsgsGrpPeopleContentProvider.fillMessageThreadItemsFromCursor(cursor)
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
