package intellibitz.intellidroid.task

import android.content.Context
import android.net.Uri
import intellibitz.intellidroid.IntellibitzAsyncTask
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.data.ContactItem
import java.util.ArrayList

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class WorkContactsSaveToDBTask(context: Context, contacts: Collection<ContactItem>) :
    IntellibitzAsyncTask(context) {

    companion object {
        private const val TAG = "WorkContactsSaveToDBTask"
    }

    private var workContactsSaveToDBTaskListener: WorkContactsSaveToDBTaskListener? = null
    private val contacts: List<ContactItem> = ArrayList(contacts)
    private var result: Uri? = null

    fun setWorkContactsSaveToDBTaskListener(workContactsSaveToDBTaskListener: WorkContactsSaveToDBTaskListener?) {
        this.workContactsSaveToDBTaskListener = workContactsSaveToDBTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        result = DeviceContactContentProvider.savesOrUpdatesWorkContacts(contacts, contextRef)
        return result != null
    }

    override fun onPostExecute(params: Any?) {
        if (success) {
            workContactsSaveToDBTaskListener?.onPostWorkContactsSaveToDBExecute(result)
        } else {
            workContactsSaveToDBTaskListener?.onPostWorkContactsSaveToDBExecuteFail(result)
        }
    }

    override fun onCancelled() {
        workContactsSaveToDBTaskListener?.onPostWorkContactsSaveToDBExecuteFail(result)
    }

    interface WorkContactsSaveToDBTaskListener {
        fun onPostWorkContactsSaveToDBExecute(result: Uri?)

        fun onPostWorkContactsSaveToDBExecuteFail(result: Uri?)
    }
}
