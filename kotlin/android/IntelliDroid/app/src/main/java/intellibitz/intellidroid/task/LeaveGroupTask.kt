package intellibitz.intellidroid.task

import android.os.AsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 */
class LeaveGroupTask(
    private val contactThreadItem: ContactItem,
    private val contactItem: ContactItem,
    private val uid: String,
    private val token: String,
    private val device: String,
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void?, Void?, Boolean?>() {

    private var leaveGroupTaskListener: LeaveGroupTaskListener? = null
    private val id: String = contactThreadItem.dataId
    private val name: String = contactThreadItem.name
    private var response: JSONObject? = null

    fun setLeaveGroupTaskListener(leaveGroupTaskListener: LeaveGroupTaskListener) {
        this.leaveGroupTaskListener = leaveGroupTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data["group_id"] = contactThreadItem.dataId
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        leaveGroupTaskListener?.setLeaveGroupTaskToNull()
        if (success == true) {
            leaveGroupTaskListener?.onPostLeaveGroupExecute(
                response,
                id,
                name,
                contactItem,
                contactThreadItem
            )
        } else {
            leaveGroupTaskListener?.onPostLeaveGroupExecuteFail(
                response,
                id,
                name,
                contactItem,
                contactThreadItem
            )
        }
    }

    override fun onCancelled() {
        leaveGroupTaskListener?.setLeaveGroupTaskToNull()
    }

    interface LeaveGroupTaskListener {
        fun onPostLeaveGroupExecute(
            response: JSONObject?,
            id: String,
            name: String,
            contactItem: ContactItem,
            contactThreadItem: ContactItem
        )

        fun onPostLeaveGroupExecuteFail(
            response: JSONObject?,
            id: String,
            name: String,
            contactItem: ContactItem,
            contactThreadItem: ContactItem
        )

        fun setLeaveGroupTaskToNull()
    }

    companion object {
        private const val TAG = "LeaveGroupTask"
    }
}
