package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.text.TextUtils
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class SetPwdTask(
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val newpwd: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var setPwdTaskListener: SetPwdTaskListener? = null
    private var response: JSONObject? = null

    fun setSetPwdTaskListener(setPwdTaskListener: SetPwdTaskListener) {
        this.setPwdTaskListener = setPwdTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        if (TextUtils.isEmpty(newpwd)) return false
        // sends read receipt to the cloud
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.NEWPWD_PARAM] = newpwd
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        setPwdTaskListener?.setSetPwdTaskToNull()
        if (success == true) {
            setPwdTaskListener?.onPostSetPwdExecute(response, newpwd)
        } else {
            setPwdTaskListener?.onPostSetPwdExecuteFail(response, newpwd)
        }
    }

    override fun onCancelled() {
        setPwdTaskListener?.setSetPwdTaskToNull()
        setPwdTaskListener?.onPostSetPwdExecuteFail(response, newpwd)
    }

    interface SetPwdTaskListener {
        fun onPostSetPwdExecute(response: JSONObject?, newpwd: String)

        fun onPostSetPwdExecuteFail(response: JSONObject?, newpwd: String)

        fun setSetPwdTaskToNull()
    }

    companion object {
        private const val TAG = "SetPwdTask"
    }
}
