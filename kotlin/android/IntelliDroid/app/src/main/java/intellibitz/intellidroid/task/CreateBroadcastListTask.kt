package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.text.TextUtils
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap

/**
 */
class CreateBroadcastListTask(
    private val contactItem: ContactItem,
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var createBroadcastTaskListener: CreateBroadcastTaskListener? = null
    private var response: JSONObject? = null

    fun setCreateBroadcastTaskListener(createBroadcastTaskListener: CreateBroadcastTaskListener) {
        this.createBroadcastTaskListener = createBroadcastTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        if (null == contactItem) return false
        if (TextUtils.isEmpty(contactItem.name)) return false
        val jsonArray = contactItem.contactsAsJsonObjectArray
        if (null == jsonArray || 0 == jsonArray.length()) return false

        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token

        data[MainApplicationSingleton.NAME_PARAM] = contactItem.name
        data["users"] = jsonArray.toString()
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        createBroadcastTaskListener?.setCreateBroadcastTaskToNull()
        if (success) {
            createBroadcastTaskListener?.onPostCreateBroadcastExecute(response, contactItem)
        } else {
            createBroadcastTaskListener?.onPostCreateBroadcastExecuteFail(response, contactItem)
        }
    }

    override fun onCancelled() {
        createBroadcastTaskListener?.setCreateBroadcastTaskToNull()
    }

    interface CreateBroadcastTaskListener {
        fun onPostCreateBroadcastExecute(response: JSONObject?, contactItem: ContactItem)

        fun onPostCreateBroadcastExecuteFail(response: JSONObject?, contactItem: ContactItem)

        fun setCreateBroadcastTaskToNull()
    }

    companion object {
        private const val TAG = "CreateBroadcastTask"
    }
}
