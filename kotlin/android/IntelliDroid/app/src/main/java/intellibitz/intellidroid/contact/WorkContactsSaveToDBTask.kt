package intellibitz.intellidroid.contact

import android.content.Context
import android.net.Uri
import intellibitz.intellidroid.IntellibitzAsyncTask
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.data.ContactItem
import java.util.ArrayList

class WorkContactsSaveToDBTask(
    contacts: Collection<ContactItem>,
    context: Context
) : IntellibitzAsyncTask(context) {

    private var workContactsSaveToDBTaskListener: WorkContactsSaveToDBTaskListener? = null
    private val contacts: List<ContactItem> = ArrayList(contacts)
    private var result: Uri? = null

    fun setWorkContactsSaveToDBTaskListener(listener: WorkContactsSaveToDBTaskListener?) {
        this.workContactsSaveToDBTaskListener = listener
    }

    override fun doInBackground(vararg params: Any?): Boolean {
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

    companion object {
        private const val TAG = "WorkContactsSaveToDBTask"
    }
}
