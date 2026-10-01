package intellibitz.intellidroid.content.task

import android.content.Context
import android.text.TextUtils
import android.util.Log
import intellibitz.intellidroid.IntellibitzAsyncTask
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import java.util.ArrayList

class FetchAttachmentsTask(private val filter: String?, context: Context?) :
    IntellibitzAsyncTask(context) {

    companion object {
        private const val TAG = "FetchAttachmentsTask"
    }

    private var fetchAttachmentsTaskListener: FetchAttachmentsTaskListener? = null
    private val attachmentItems: ArrayList<MessageItem> = ArrayList()

    fun setFetchAttachmentsTaskListener(listener: FetchAttachmentsTaskListener?) {
        this.fetchAttachmentsTaskListener = listener
    }

    override fun doInBackground(vararg params: Any?): Boolean {
        val ctx = this.context.get()
        if (ctx == null) {
            Log.e(TAG, "Context NULL - skipping")
            return false
        }
        var selection = (" ( " +
                MessageItemColumns.KEY_NAME +
                " IS NULL OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " IS NULL OR " +
                MessageItemColumns.KEY_NAME +
                " like ? OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " like ? ) ")
        var selArgs: Array<String> = if (!filter.isNullOrEmpty()) {
            arrayOf("%$filter%", "%$filter%")
        } else {
            arrayOf("%%", "%%")
        }
        var sortOrder = MessageItemColumns.KEY_TIMESTAMP + " ASC"
        var cursor = ctx.contentResolver.query(
            MsgChatAttachmentContentProvider.CONTENT_URI, null, selection, selArgs, sortOrder
        )
        var items = MsgChatAttachmentContentProvider.fillAttachmentsFromCursor(cursor)
        cursor?.close()
        if (!items.isNullOrEmpty()) attachmentItems.addAll(items)

        selection = (" ( " +
                MessageItemColumns.KEY_NAME +
                " IS NULL OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " IS NULL OR " +
                MessageItemColumns.KEY_NAME +
                " like ? OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " like ? ) ")
        selArgs = if (!filter.isNullOrEmpty()) {
            arrayOf("%$filter%", "%$filter%")
        } else {
            arrayOf("%%", "%%")
        }
        sortOrder = MessageItemColumns.KEY_TIMESTAMP + " ASC"
        cursor = ctx.contentResolver.query(
            MsgChatGrpAttachmentContentProvider.CONTENT_URI, null, selection, selArgs, sortOrder
        )
        items = MsgChatAttachmentContentProvider.fillAttachmentsFromCursor(cursor)
        cursor?.close()
        if (!items.isNullOrEmpty()) attachmentItems.addAll(items)

        selection = (" ( " +
                MessageItemColumns.KEY_NAME +
                " IS NULL OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " IS NULL OR " +
                MessageItemColumns.KEY_NAME +
                " like ? OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " like ? ) ")
        if (!filter.isNullOrEmpty()) {
            selArgs = arrayOf("%$filter%", "%$filter%")
        } else {
            selArgs = arrayOf("%%", "%%")
        }
        sortOrder = MessageItemColumns.KEY_TIMESTAMP + " ASC"
        cursor = ctx.contentResolver.query(
            MsgEmailAttachmentContentProvider.CONTENT_URI, null, selection, selArgs, sortOrder
        )
        items = MsgChatAttachmentContentProvider.fillAttachmentsFromCursor(cursor)
        cursor?.close()
        if (!items.isNullOrEmpty()) attachmentItems.addAll(items)

        if (attachmentItems.isNotEmpty()) {
            val items1 = attachmentItems.toTypedArray()
            for (item in items1) {
                if (TextUtils.isEmpty(item.name)) {
                    val description = item.description
                    if (TextUtils.isEmpty(description) || "null".equals(description, ignoreCase = true)) {
                        attachmentItems.remove(item)
                    }
                }
            }
        }
        return attachmentItems.isNotEmpty()
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            fetchAttachmentsTaskListener?.onFetchAttachmentsTaskExecute(attachmentItems)
        } else {
            fetchAttachmentsTaskListener?.onFetchAttachmentsTaskExecuteFail(attachmentItems)
        }
        releaseContext()
    }

    override fun onCancelled() {
        fetchAttachmentsTaskListener?.onFetchAttachmentsTaskExecuteFail(attachmentItems)
        releaseContext()
    }

    interface FetchAttachmentsTaskListener {
        fun onFetchAttachmentsTaskExecute(attachmentItems: ArrayList<MessageItem>?)
        fun onFetchAttachmentsTaskExecuteFail(attachmentItems: ArrayList<MessageItem>?)
    }
}
