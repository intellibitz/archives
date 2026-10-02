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
class GetDraftsTask(
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var getDraftsTaskListener: GetDraftsTaskListener? = null
    private var response: JSONObject? = null

    fun setGetDraftsTaskListener(getDraftsTaskListener: GetDraftsTaskListener) {
        this.getDraftsTaskListener = getDraftsTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        getDraftsTaskListener?.setDraftsGetFromCloudTaskToNull()
        if (success) {
            getDraftsTaskListener?.onPostDraftsGetFromCloudExecute(response)
        } else {
            // ERROR
            getDraftsTaskListener?.onPostDraftsGetFromCloudExecuteFail(response)
        }
    }

    override fun onCancelled() {
        getDraftsTaskListener?.setDraftsGetFromCloudTaskToNull()
    }

    interface GetDraftsTaskListener {
        fun onPostDraftsGetFromCloudExecute(response: JSONObject?)

        fun onPostDraftsGetFromCloudExecuteFail(response: JSONObject?)

        fun setDraftsGetFromCloudTaskToNull()
    }
}
