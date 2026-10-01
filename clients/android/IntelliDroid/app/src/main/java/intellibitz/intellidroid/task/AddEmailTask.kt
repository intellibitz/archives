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
class AddEmailTask(
    private val email: String?,
    private val password: String?,
    private var uid: String?,
    private var token: String?,
    private var device: String? = "android",
    private var deviceRef: String?,
    private var url: String?
) : AsyncTask<Void?, Void?, Boolean?>() {
    private var addEmailTaskListener: AddEmailTaskListener? = null
    private var response: JSONObject? = null

    fun setAddEmailTaskListener(addEmailTaskListener: AddEmailTaskListener?) {
        this.addEmailTaskListener = addEmailTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // TODO: attempt authentication against a network service.

        val data = HashMap<String, String?>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.EMAIL_PARAM] = email
        data[MainApplicationSingleton.PWD_PARAM] = password
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        addEmailTaskListener?.setAddEmailTaskToNull()
        if (success == true) {
            addEmailTaskListener?.onPostAddEmailTaskExecute(response)
        } else {
            addEmailTaskListener?.onPostAddEmailTaskExecuteFail(response)
        }
    }

    override fun onCancelled() {
        addEmailTaskListener?.setAddEmailTaskToNull()
    }

    interface AddEmailTaskListener {
        fun onPostAddEmailTaskExecute(response: JSONObject?)
        fun onPostAddEmailTaskExecuteFail(response: JSONObject?)
        fun setAddEmailTaskToNull()
    }
}
