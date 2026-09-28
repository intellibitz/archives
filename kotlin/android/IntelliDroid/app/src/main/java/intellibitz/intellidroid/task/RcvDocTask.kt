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
class RcvDocTask(
    private val docId: String,
    private val uid: String,
    private val token: String,
    device: String,
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    companion object {
        private const val TAG = "RcvDocTask"
    }

    private var rcvDocTaskListener: RcvDocTaskListener? = null
    private var device: String = device
    private var response: JSONObject? = null

    fun setRcvDocTaskListener(groupInfoTaskListener: RcvDocTaskListener) {
        this.rcvDocTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
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

    override fun onPostExecute(success: Boolean) {
        rcvDocTaskListener?.setRcvDocTaskToNull()
        if (success) {
            rcvDocTaskListener?.onPostRcvDocExecute(response)
        } else {
            rcvDocTaskListener?.onPostRcvDocExecuteFail(response)
        }
    }

    override fun onCancelled() {
        rcvDocTaskListener?.setRcvDocTaskToNull()
    }

    interface RcvDocTaskListener {
        fun onPostRcvDocExecute(response: JSONObject?)

        fun onPostRcvDocExecuteFail(response: JSONObject?)

        fun setRcvDocTaskToNull()
    }
}
