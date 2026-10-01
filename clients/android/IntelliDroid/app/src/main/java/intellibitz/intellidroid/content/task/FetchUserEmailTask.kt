package intellibitz.intellidroid.content.task

import android.content.Context
import android.os.AsyncTask
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.data.ContactItem
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

class FetchUserEmailTask(user: ContactItem?, context: Context?) :
    AsyncTask<Void?, Void?, Boolean>() {

    companion object {
        private const val TAG = "FetchUserEmailTask"
    }

    private val context: WeakReference<Context?> = WeakReference(context)
    private val user: SoftReference<ContactItem?> = SoftReference(user)
    private var fetchUserEmailTaskListener: FetchUserEmailTaskListener? = null

    fun setFetchUserEmailTaskListener(listener: FetchUserEmailTaskListener?) {
        this.fetchUserEmailTaskListener = listener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val u = user.get() ?: return false
        val c = context.get() ?: return false
        UserEmailContentProvider.populateUserEmailsJoinByDataId(u, c)
        return true
    }

    override fun onPostExecute(success: Boolean) {
        val listener = fetchUserEmailTaskListener ?: return
        listener.setFetchUserEmailTaskToNull()
        val u = user.get()
        if (success) {
            listener.onFetchUserEmailTaskExecute(u)
        } else {
            listener.onFetchUserEmailTaskExecuteFail(u)
        }
    }

    override fun onCancelled() {
        fetchUserEmailTaskListener?.setFetchUserEmailTaskToNull()
    }

    interface FetchUserEmailTaskListener {
        fun onFetchUserEmailTaskExecute(userItem: ContactItem?)
        fun onFetchUserEmailTaskExecuteFail(userItem: ContactItem?)
        fun setFetchUserEmailTaskToNull()
    }
}
