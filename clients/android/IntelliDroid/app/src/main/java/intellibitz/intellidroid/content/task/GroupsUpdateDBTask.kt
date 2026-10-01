package intellibitz.intellidroid.content.task

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider
import intellibitz.intellidroid.data.ContactItem
import java.io.IOException
import java.util.Collections
import java.util.HashSet

class GroupsUpdateDBTask : AsyncTask<Void?, Void?, Boolean> {
    companion object {
        private const val TAG = "GroupsUpdateDBTask"
    }

    private var groupsUpdateDBTaskListener: GroupsUpdateDBTaskListener? = null
    private var contacts: MutableCollection<ContactItem> =
        Collections.synchronizedSet(HashSet(1))
    private var context: Context? = null
    private var result = 0

    constructor(contactItem: ContactItem, context: Context?) : super() {
        contacts.add(contactItem)
        this.context = context
    }

    fun setGroupsUpdateDBTaskListener(listener: GroupsUpdateDBTaskListener?) {
        this.groupsUpdateDBTaskListener = listener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        try {
            result = MsgChatGrpContactsContentProvider.updatesContactThreadsInDB(contacts, context)
        } catch (e: IOException) {
            Log.e(TAG, " ERROR - " + e.message)
            e.printStackTrace()
        }
        return result != 0
    }

    override fun onPostExecute(success: Boolean) {
        groupsUpdateDBTaskListener?.setGroupsUpdateDBTaskToNull()
        if (success) {
            groupsUpdateDBTaskListener?.onPostGroupsUpdateDBExecute(result, contacts)
        } else {
            groupsUpdateDBTaskListener?.onPostGroupsUpdateDBExecuteFail(result, contacts)
        }
    }

    override fun onCancelled() {
        groupsUpdateDBTaskListener?.setGroupsUpdateDBTaskToNull()
    }

    interface GroupsUpdateDBTaskListener {
        fun onPostGroupsUpdateDBExecute(uri: Int, contacts: Collection<ContactItem>?)
        fun onPostGroupsUpdateDBExecuteFail(uri: Int, contacts: Collection<ContactItem>?)
        fun setGroupsUpdateDBTaskToNull()
    }
}
