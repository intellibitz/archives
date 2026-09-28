package intellibitz.intellidroid.content.task

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider
import intellibitz.intellidroid.data.ContactItem
import java.util.Collections
import java.util.HashSet

class GroupsSaveToDBTask : AsyncTask<Void?, Void?, Boolean> {
    companion object {
        private const val TAG = "GroupsSaveToDBTask"
    }

    private var groupsSaveToDBTaskListener: GroupsSaveToDBTaskListener? = null
    private var contacts: MutableCollection<ContactItem> =
        Collections.synchronizedSet(HashSet(1))
    private var context: Context? = null
    private var result: Uri? = null

    constructor(contactItem: ContactItem, context: Context?) : super() {
        contacts.add(contactItem)
        this.context = context
    }

    constructor(contacts: MutableCollection<ContactItem>, context: Context?) : super() {
        this.contacts = contacts
        this.context = context
    }

    fun setGroupsSaveToDBTaskListener(listener: GroupsSaveToDBTaskListener?) {
        this.groupsSaveToDBTaskListener = listener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        if (contacts.isEmpty()) return false
        result = MsgChatGrpContactsContentProvider.savesContactThreadsInDB(contacts, context)
        return result != null
    }

    override fun onPostExecute(success: Boolean) {
        groupsSaveToDBTaskListener?.setGroupsSaveToDBTaskToNull()
        if (success) {
            groupsSaveToDBTaskListener?.onPostGroupsSaveToDBExecute(result, contacts)
        } else {
            Log.e(TAG, " ERROR - $result")
        }
    }

    override fun onCancelled() {
        groupsSaveToDBTaskListener?.setGroupsSaveToDBTaskToNull()
    }

    interface GroupsSaveToDBTaskListener {
        fun onPostGroupsSaveToDBExecute(uri: Uri?, contacts: Collection<ContactItem>?)
        fun setGroupsSaveToDBTaskToNull()
    }
}
