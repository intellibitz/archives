package intellibitz.intellidroid.task

import android.os.AsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

/**
 */
class GroupRemoveUsersTask(
    private val contactThreadItem: ContactItem,
    private val contactItem: ContactItem,
    private val uid: String,
    private val token: String,
    private val device: String,
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    companion object {
        private const val TAG = "GroupsAddUsersTask"
    }

    private var groupsRemoveUsersTaskListener: GroupsRemoveUsersTaskListener? = null
    private val id: String = contactThreadItem.dataId
    private val name: String = contactThreadItem.name
    private val contacts: Array<String> = arrayOf(contactItem.dataId)
    private var response: JSONObject? = null

    fun setGroupsRemoveUsersTaskListener(groupsRemoveUsersTaskListener: GroupsRemoveUsersTaskListener) {
        this.groupsRemoveUsersTaskListener = groupsRemoveUsersTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data["group_id"] = id
        val jsonArray = JSONArray()
        for (contact in contacts) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("uid", contact)
//                jsonObject.put("type", "user")
                jsonArray.put(jsonObject)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        data["users"] = jsonArray.toString()
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
//        groupsAddUsersTask = null;
        groupsRemoveUsersTaskListener?.setGroupsRemoveUsersTaskToNull()
        if (success) {
            groupsRemoveUsersTaskListener?.onPostGroupsRemoveUsersExecute(
                response,
                id,
                name,
                contacts,
                contactItem,
                contactThreadItem
            )
        } else {
            groupsRemoveUsersTaskListener?.onPostGroupsRemoveUsersExecuteFail(
                response,
                id,
                name,
                contacts,
                contactItem,
                contactThreadItem
            )
        }
    }

    override fun onCancelled() {
//        groupsAddUsersTask = null;
        groupsRemoveUsersTaskListener?.setGroupsRemoveUsersTaskToNull()
    }

    interface GroupsRemoveUsersTaskListener {
        fun onPostGroupsRemoveUsersExecute(
            response: JSONObject?,
            id: String,
            name: String,
            contacts: Array<String>,
            contactItem: ContactItem,
            contactThreadItem: ContactItem
        )

        fun onPostGroupsRemoveUsersExecuteFail(
            response: JSONObject?,
            id: String,
            name: String,
            contacts: Array<String>,
            contactItem: ContactItem,
            contactThreadItem: ContactItem
        )

        fun setGroupsRemoveUsersTaskToNull()
    }
}
