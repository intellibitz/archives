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
class UpdateProfileTask(
    private val uid: String,
    private val token: String,
    private var device: String,
    private val deviceRef: String,
    private val name: String,
    private val status: String?,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {

    companion object {
        private const val TAG = "UpdateProfileTask"
    }

    private var updateProfileTaskListener: UpdateProfileTaskListener? = null
    private var response: JSONObject? = null

    init {
        this.device = device
    }

    fun setUpdateProfileTaskListener(groupInfoTaskListener: UpdateProfileTaskListener) {
        this.updateProfileTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        if (name == null) return false
        // sends read receipt to the cloud
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.NAME_PARAM] = name
        if (status != null) data[MainApplicationSingleton.STATUS_PARAM] = status

        response = HttpUrlConnectionParser.postHTTP(url, data)
        return response != null
    }

    override fun onPostExecute(success: Boolean) {
        updateProfileTaskListener?.setUpdateProfileTaskToNull()
        updateProfileTaskListener?.onPostUpdateProfileExecute(response)
    }

    override fun onCancelled() {
        updateProfileTaskListener?.setUpdateProfileTaskToNull()
        updateProfileTaskListener?.onPostUpdateProfileExecuteFail(response)
    }

    interface UpdateProfileTaskListener {
        fun onPostUpdateProfileExecute(response: JSONObject?)

        fun onPostUpdateProfileExecuteFail(response: JSONObject?)

        fun setUpdateProfileTaskToNull()
    }
}
