package intellibitz.intellidroid.task

import android.content.Context
import android.os.AsyncTask
import android.util.SparseArray
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.service.ContactFetch

/**
 * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
 * the email text field with results on the main UI thread.
 */
class ContactsFetchTask(
    private val user: ContactItem,
    private val flag: Int,
    private val context: Context
) : AsyncTask<Any?, Any?, SparseArray<ContactItem>>() {

    companion object {
        private const val TAG = "ContactsFetchTask"
    }

    private var contactsFetchTaskListener: ContactsFetchTaskListener? = null

    fun setContactsFetchTaskListener(contactsFetchTaskListener: ContactsFetchTaskListener) {
        this.contactsFetchTaskListener = contactsFetchTaskListener
    }

    override fun doInBackground(vararg voids: Any?): SparseArray<ContactItem> {
        return ContactFetch(context).getDetailedContactList(null)
    }

    override fun onPostExecute(contactItems: SparseArray<ContactItem>) {
        contactsFetchTaskListener?.onPostContactsFetchExecute(user, contactItems, flag)
    }

    override fun onCancelled() {
        contactsFetchTaskListener?.onPostContactsFetchExecuteFail(user, flag)
    }

    interface ContactsFetchTaskListener {
        fun onPostContactsFetchExecute(user: ContactItem, contactItems: SparseArray<ContactItem>, flag: Int)

        fun onPostContactsFetchExecuteFail(user: ContactItem, flag: Int)
    }
}
