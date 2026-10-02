package intellibitz.intellidroid.content.task

import android.content.Context
import android.util.Log
import intellibitz.intellidroid.IntellibitzAsyncTask
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import java.util.ArrayList

class FetchMsgChatGrpContactsTask(
    private val filter: String?,
    context: Context?
) : IntellibitzAsyncTask(context) {

    companion object {
        const val TAG = "FtchMsgChatGrpCtctsTask"
    }

    private var fetchMsgChatGrpContactsTaskListener: FetchMsgChatGrpContactsTaskListener? = null
    private var contactItems: ArrayList<ContactItem>? = null

    fun setFetchMsgChatGrpContactsTaskListener(listener: FetchMsgChatGrpContactsTaskListener?) {
        this.fetchMsgChatGrpContactsTaskListener = listener
    }

    override fun doInBackground(vararg params: Any?): Boolean {
        val selection = (" ( name IS NULL OR name like ? ) "
                + " AND ( " +
                ContactItemColumns.KEY_IS_GROUP +
                " = 1 ) AND ( " +
                ContactItemColumns.KEY_IS_EMAIL +
                " = 0 OR " +
                ContactItemColumns.KEY_IS_EMAIL +
                " IS NULL ) ")
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
            MsgChatGrpContactsContentProvider.CONTENT_URI,
            null,
            selection, selArgs,
            ContactItemColumns.KEY_TIMESTAMP + " DESC"
        )
        if (cursor != null && cursor.count > 0) {
            contactItems = MsgChatGrpContactsContentProvider.fillContactThreadItemsFromCursor(cursor)
        }
        cursor?.close()
        return !contactItems.isNullOrEmpty()
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            fetchMsgChatGrpContactsTaskListener?.onFetchMsgChatGrpContactsTaskExecute(contactItems)
        } else {
            fetchMsgChatGrpContactsTaskListener?.onFetchMsgChatGrpContactsTaskExecuteFail(contactItems)
        }
        releaseContext()
    }

    override fun onCancelled() {
        fetchMsgChatGrpContactsTaskListener?.onFetchMsgChatGrpContactsTaskExecuteFail(contactItems)
        releaseContext()
    }

    interface FetchMsgChatGrpContactsTaskListener {
        fun onFetchMsgChatGrpContactsTaskExecute(contactItem: ArrayList<ContactItem>?)
        fun onFetchMsgChatGrpContactsTaskExecuteFail(contactItem: ArrayList<ContactItem>?)
    }
}
