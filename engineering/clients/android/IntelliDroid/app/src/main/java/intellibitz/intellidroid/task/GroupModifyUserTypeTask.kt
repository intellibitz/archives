package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class GroupModifyUserTypeTask(
    item: ContactItem,
    contactItem: ContactItem,
    flag: Int,
    uid: String,
    token: String,
    device: String,
    deviceRef: String,
    url: String
) : AsyncTask<Void, Void, Boolean>() {

    companion object {
        private const val TAG = "GroupModifyUserTypeTask"
    }

    var flag = flag
    private var groupModifyUserTypeTaskListener: GroupModifyUserTypeTaskListener? = null
    private var contactThreadItem: ContactItem = item
    private var contactItem: ContactItem = contactItem
    private var uid: String = uid
    private var token: String = token
    private var device: String = device
    private var deviceRef: String = deviceRef
    private var url: String = url
    private var response: JSONObject? = null

    fun setGroupModifyUserTypeTaskListener(groupDetailsTaskListener: GroupModifyUserTypeTaskListener) {
        this.groupModifyUserTypeTaskListener = groupDetailsTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.GROUP_ID_PARAM] = contactItem.dataId
        data[MainApplicationSingleton.USER_UID_PARAM] = contactItem.dataId
        data[MainApplicationSingleton.MODIFY_TYPE_PARAM] = contactItem.type
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        groupModifyUserTypeTaskListener?.setGroupModifyUserTypeTaskToNull()
        if (success) {
            groupModifyUserTypeTaskListener?.onPostGroupModifyUserTypeExecute(
                response, contactThreadItem, contactItem, flag
            )
        } else {
            // ERROR
            Log.e(TAG, "ERROR - $response")
            groupModifyUserTypeTaskListener?.onPostGroupModifyUserTypeExecuteFail(
                response, contactThreadItem, contactItem, flag
            )
        }
    }

    override fun onCancelled() {
        groupModifyUserTypeTaskListener?.setGroupModifyUserTypeTaskToNull()
    }

    interface GroupModifyUserTypeTaskListener {
        fun onPostGroupModifyUserTypeExecute(
            response: JSONObject?,
            item: ContactItem,
            contactItem: ContactItem,
            flag: Int
        )

        fun onPostGroupModifyUserTypeExecuteFail(
            response: JSONObject?,
            item: ContactItem,
            contactItem: ContactItem,
            flag: Int
        )

        fun setGroupModifyUserTypeTaskToNull()
    }
}
