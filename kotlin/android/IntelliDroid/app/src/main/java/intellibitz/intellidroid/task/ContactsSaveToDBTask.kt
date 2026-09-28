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
class ContactsSaveToDBTask(
    contacts: Collection<ContactItem>,
    context: Context
) : IntellibitzAsyncTask(context) {
    private var contactsSaveToDBTaskListener: ContactsSaveToDBTaskListener? = null
    private val contacts: List<ContactItem> = ArrayList(contacts)
    private var result: Uri? = null

    fun setContactsSaveToDBTaskListener(contactsSaveToDBTaskListener: ContactsSaveToDBTaskListener?) {
        this.contactsSaveToDBTaskListener = contactsSaveToDBTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        result = DeviceContactContentProvider.savesOrUpdatesDeviceContacts(contacts, contextRef)
        return result != null
    }

    override fun onPostExecute(params: Any?) {
        if (success) {
            contactsSaveToDBTaskListener?.onPostContactsSaveToDBExecute(result)
        } else {
            contactsSaveToDBTaskListener?.onPostContactsSaveToDBExecuteFail(result)
        }
    }

    override fun onCancelled() {
        contactsSaveToDBTaskListener?.onPostContactsSaveToDBExecuteFail(result)
    }

    interface ContactsSaveToDBTaskListener {
        fun onPostContactsSaveToDBExecute(result: Uri?)

        fun onPostContactsSaveToDBExecuteFail(result: Uri?)
    }
}
