package intellibitz.intellidroid.task

import android.os.AsyncTask
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class AckDocTask(
    private var docId: String?,
    private var uid: String?,
    private var token: String?,
    private var device: String? = "android",
    private var deviceRef: String?,
    private var url: String?
) : AsyncTask<Void?, Void?, Boolean?>() {
    private var ackDocTaskListener: AckDocTaskListener? = null
    private var response: JSONObject? = null

    fun setAckDocTaskListener(groupInfoTaskListener: AckDocTaskListener?) {
        this.ackDocTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String?>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.DOC_ID] = docId

        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)

        if (null == response) {
            return false
        }
        // TODO: register the new account here.
//            investigate response and take action
        return true
    }

    override fun onPostExecute(success: Boolean?) {
        ackDocTaskListener?.setAckDocTaskToNull()
        if (success == true) {
            ackDocTaskListener?.onPostAckDocExecute(response)
        } else {
            ackDocTaskListener?.onPostAckDocExecuteFail(response)
        }
    }

    override fun onCancelled() {
        ackDocTaskListener?.setAckDocTaskToNull()
    }

    interface AckDocTaskListener {
        fun onPostAckDocExecute(response: JSONObject?)
        fun onPostAckDocExecuteFail(response: JSONObject?)
        fun setAckDocTaskToNull()
    }

    companion object {
        private const val TAG = "AckDocTask"
    }
}
