package intellibitz.intellidroid.task

import android.os.AsyncTask
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous registration task used to authenticate
 * the mobile number.
 */
class OTPCallTask(
    private val device: String,
    private val mobile: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    companion object {
        private const val TAG = "OTPCallTask"
    }

    private var otpCallTaskListener: OTPCallTaskListener? = null
    private var response: JSONObject? = null

    fun setOtpCallTaskListener(otpCallTaskListener: OTPCallTaskListener) {
        this.otpCallTaskListener = otpCallTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.MOBILE_PARAM] = mobile
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        otpCallTaskListener?.setOTPCallTaskToNull()
        if (success) {
            otpCallTaskListener?.onPostOTPCallExecute(response)
        } else {
            otpCallTaskListener?.onPostOTPCallExecuteFail(response)
        }
//        hideProgress()
    }

    override fun onCancelled() {
        otpCallTaskListener?.setOTPCallTaskToNull()
//        hideProgress()
    }

    interface OTPCallTaskListener {
        fun onPostOTPCallExecute(response: JSONObject?)

        fun onPostOTPCallExecuteFail(response: JSONObject?)

        fun setOTPCallTaskToNull()
    }
}
