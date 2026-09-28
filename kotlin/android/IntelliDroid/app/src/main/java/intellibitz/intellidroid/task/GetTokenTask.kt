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
class GetTokenTask(
    private val email: String,
    private val emailCode: String,
    private val uid: String,
    private val token: String,
    private val device: String,
    private val deviceRef: String,
    private val URL: String
) : AsyncTask<Void, Void, Boolean>() {
    private var getTokenTaskListener: GetTokenTaskListener? = null
    private var response: JSONObject? = null

    fun setGetTokenTaskListener(getTokenTaskListener: GetTokenTaskListener?) {
        this.getTokenTaskListener = getTokenTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // TODO: attempt authentication against a network service.

        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.EMAIL_PARAM] = email
        data[MainApplicationSingleton.CODE_PARAM] = emailCode
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(this.URL, data)

        if (null == response) {
            return false
        }
        // TODO: register the new account here.
//            investigate response and take action
        return true
    }

    override fun onPostExecute(success: Boolean?) {
        getTokenTaskListener?.setGetTokenTaskToNull()
        if (success == true) {
            getTokenTaskListener?.onPostGetTokenTaskExecute(response)
        } else {
            getTokenTaskListener?.onPostGetTokenTaskExecuteFail(response)
        }
    }

    override fun onCancelled() {
        getTokenTaskListener?.setGetTokenTaskToNull()
//        showProgress(false);
    }

    interface GetTokenTaskListener {
        fun onPostGetTokenTaskExecute(response: JSONObject?)

        fun onPostGetTokenTaskExecuteFail(response: JSONObject?)

        fun setGetTokenTaskToNull()
    }
}
