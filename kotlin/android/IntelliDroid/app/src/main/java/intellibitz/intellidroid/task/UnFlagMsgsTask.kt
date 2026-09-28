package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class UnFlagMsgsTask : AsyncTask<Void?, Void?, Boolean?> {
    companion object {
        private const val TAG = "UnFlagMsgsTask"
    }

    private var unFlagMsgsTaskListener: UnFlagMsgsTaskListener? = null
    private var ids: Array<String>? = null
    private var mids: Array<MessageItem>? = null
    private var uid: String? = null
    private var token: String? = null
    private var device = "android"
    private var deviceRef: String? = null
    private var url: String? = null
    private var response: JSONObject? = null

    constructor(ids: Array<String>?, uid: String?, token: String?,
                device: String, deviceRef: String?, url: String?) {
        this.ids = ids
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    constructor(mids: Array<MessageItem>?, uid: String?, token: String?,
                device: String, deviceRef: String?, url: String?) {
        this.mids = mids
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    fun setUnFlagMsgsTaskListener(groupInfoTaskListener: UnFlagMsgsTaskListener?) {
        unFlagMsgsTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
//        sends read receipt to the cloud
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token

        if (ids == null && mids == null) return false
        val array = JSONArray()
        if (mids == null) {
            for (id in ids!!) {
                try {
                    val msg = JSONObject()
                    msg.put("msg_id", id)
                    msg.put("note", id)
                    array.put(msg)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } else {
            for (id in mids!!) {
                try {
                    val msg = JSONObject()
                    msg.put("msg_id", id.dataId)
                    msg.put("note", id.msgRef)
                    array.put(msg)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        }
        data[MainApplicationSingleton.MSGS_PARAM] = array.toString()
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return response != null
    }

    override fun onPostExecute(success: Boolean?) {
        unFlagMsgsTaskListener?.setUnFlagMsgsTaskToNull()
        if (success == true) {
            unFlagMsgsTaskListener?.onPostUnFlagMsgsExecute(response, mids, ids)
        } else {
            Log.e(TAG, ":error - $response")
            unFlagMsgsTaskListener?.onPostUnFlagMsgsExecuteFail(response, mids, ids)
        }
    }

    override fun onCancelled() {
        unFlagMsgsTaskListener?.setUnFlagMsgsTaskToNull()
    }

    interface UnFlagMsgsTaskListener {
        fun onPostUnFlagMsgsExecute(response: JSONObject?, mids: Array<MessageItem>?, item: Array<String>?)

        fun onPostUnFlagMsgsExecuteFail(response: JSONObject?, mids: Array<MessageItem>?, item: Array<String>?)

        fun setUnFlagMsgsTaskToNull()
    }
}
