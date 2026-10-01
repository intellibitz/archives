package intellibitz.intellidroid.task

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import intellibitz.intellidroid.data.MessageItem

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class StacksSaveToDBTask(
    private val nestItems: Collection<MessageItem>,
    private val context: Context
) : AsyncTask<Void, Void, Boolean>() {

    private var nestsSaveToDBTaskListener: StacksSaveToDBTaskListener? = null
    private var uri: Uri? = null

    fun setStacksSaveToDBTaskListener(nestsSaveToDBTaskListener: StacksSaveToDBTaskListener) {
        this.nestsSaveToDBTaskListener = nestsSaveToDBTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
//        uri = MsgsStackNestContentProvider.saveStacksInDB(nestItems, context)
        return uri != null
    }

    override fun onPostExecute(success: Boolean) {
        nestsSaveToDBTaskListener?.setStacksSaveToDBTaskToNull()
        if (success) {
            nestsSaveToDBTaskListener?.onPostStacksSaveToDBExecute(uri, nestItems)
        } else {
            nestsSaveToDBTaskListener?.onPostStacksSaveToDBExecuteFail(uri, nestItems)
        }
    }

    override fun onCancelled() {
        nestsSaveToDBTaskListener?.setStacksSaveToDBTaskToNull()
    }

    interface StacksSaveToDBTaskListener {
        fun onPostStacksSaveToDBExecute(uri: Uri?, nestItems: Collection<MessageItem>)

        fun onPostStacksSaveToDBExecuteFail(uri: Uri?, nestItems: Collection<MessageItem>)

        fun setStacksSaveToDBTaskToNull()
    }
}
