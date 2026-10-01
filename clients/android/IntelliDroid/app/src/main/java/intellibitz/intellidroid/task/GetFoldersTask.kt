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
class GetFoldersTask(
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {

    private var getFoldersTaskListener: GetFoldersTaskListener? = null
    private var response: JSONObject? = null

    fun setGetFoldersTaskListener(getFoldersTaskListener: GetFoldersTaskListener) {
        this.getFoldersTaskListener = getFoldersTaskListener
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
        getFoldersTaskListener?.setFoldersGetFromCloudTaskToNull()
        if (success) {
            getFoldersTaskListener?.onPostFoldersGetFromCloudExecute(response)
        } else {
            // ERROR
            getFoldersTaskListener?.onPostFoldersGetFromCloudExecuteFail(response)
        }
    }

    override fun onCancelled() {
        getFoldersTaskListener?.setFoldersGetFromCloudTaskToNull()
    }

    interface GetFoldersTaskListener {
        fun onPostFoldersGetFromCloudExecute(response: JSONObject?)

        fun onPostFoldersGetFromCloudExecuteFail(response: JSONObject?)

        fun setFoldersGetFromCloudTaskToNull()
    }
}
