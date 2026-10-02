package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.text.TextUtils
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class GroupDetailsTask(
    private val contactItem: ContactItem,
    private val uid: String,
    private val token: String,
    private val device: String = "android",
    private val deviceRef: String,
    private val url: String,
    private var flag: Int = 0
) : AsyncTask<Void, Void, Boolean>() {

    private var groupDetailsTaskListener: GroupDetailsTaskListener? = null
    private var response: JSONObject? = null

    constructor(
        item: ContactItem,
        flag: Int,
        uid: String,
        token: String,
        device: String,
        deviceRef: String,
        url: String
    ) : this(item, uid, token, device, deviceRef, url) {
        this.flag = flag
    }

    fun setGroupDetailsTaskListener(groupDetailsTaskListener: GroupDetailsTaskListener) {
        this.groupDetailsTaskListener = groupDetailsTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        if (contactItem == null) return false
        val dataId = contactItem.dataId
        if (TextUtils.isEmpty(dataId)) return false
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.GROUP_ID_PARAM] = dataId
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return response != null
    }

    override fun onPostExecute(success: Boolean) {
        groupDetailsTaskListener?.setGroupDetailsTaskToNull()
        if (success) {
            groupDetailsTaskListener?.onPostGroupDetailsTaskExecute(response, contactItem)
        } else {
            groupDetailsTaskListener?.onPostGroupDetailsTaskExecuteFail(response, contactItem)
        }
    }

    override fun onCancelled() {
        groupDetailsTaskListener?.setGroupDetailsTaskToNull()
    }

    interface GroupDetailsTaskListener {
        fun onPostGroupDetailsTaskExecute(response: JSONObject?, item: ContactItem)

        fun onPostGroupDetailsTaskExecuteFail(response: JSONObject?, item: ContactItem)

        fun setGroupDetailsTaskToNull()
    }

    companion object {
        private const val TAG = "GroupDetailsTask"
    }
}
