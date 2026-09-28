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
class GroupsAddUsersTask : AsyncTask<Void?, Void?, Boolean?> {
    companion object {
        private const val TAG = "GroupsAddUsersTask"
    }

    private var groupsAddUsersTaskListener: GroupsAddUsersTaskListener? = null
    private var id: String? = null
    private var name: String? = null
    private var contactItem: ContactItem? = null
    private var contacts: Array<String>? = null
    private var uid: String? = null
    private var token: String? = null
    private var device: String? = "android"
    private var deviceRef: String? = null
    private var url: String? = null
    private var response: JSONObject? = null

    constructor(id: String, name: String, contacts: Array<String>,
                uid: String, token: String, device: String,
                deviceRef: String, url: String) {
        this.id = id
        this.name = name
        this.contacts = contacts
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    constructor(contactItem: ContactItem,
                uid: String, token: String, device: String,
                deviceRef: String, url: String) {
        this.contactItem = contactItem
        this.id = contactItem.dataId
        this.name = contactItem.name
        this.contacts = contactItem.contactsAsArray
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    fun setGroupsAddUsersTaskListener(groupsAddUsersTaskListener: GroupsAddUsersTaskListener?) {
        this.groupsAddUsersTaskListener = groupsAddUsersTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device!!
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef!!
        data[MainApplicationSingleton.UID_PARAM] = uid!!
        data[MainApplicationSingleton.TOKEN_PARAM] = token!!
        data["group_id"] = id!!
        val jsonArray = JSONArray()
        for (contact in contacts!!) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("uid", contact)
                jsonObject.put("type", "user")
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

    override fun onPostExecute(success: Boolean?) {
        //        groupsAddUsersTask = null;
        groupsAddUsersTaskListener?.setGroupsAddUsersTaskToNull()
        if (success!!) {
            groupsAddUsersTaskListener?.onPostGroupsAddUsersExecute(response,
                    id, name, contacts, contactItem)
        } else {
            groupsAddUsersTaskListener?.onPostGroupsAddUsersExecuteFail(response,
                    id, name, contacts, contactItem)
        }
    }

    override fun onCancelled() {
        //        groupsAddUsersTask = null;
        groupsAddUsersTaskListener?.setGroupsAddUsersTaskToNull()
    }

    interface GroupsAddUsersTaskListener {
        fun onPostGroupsAddUsersExecute(response: JSONObject?, id: String?, name: String?,
                                        contacts: Array<String>?, contactItem: ContactItem?)

        fun onPostGroupsAddUsersExecuteFail(response: JSONObject?, id: String?, name: String?,
                                            contacts: Array<String>?, contactItem: ContactItem?)

        fun setGroupsAddUsersTaskToNull()
    }
}
