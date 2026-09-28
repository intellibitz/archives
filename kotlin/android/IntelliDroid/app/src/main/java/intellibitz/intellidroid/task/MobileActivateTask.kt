package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.text.TextUtils
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous registration task used to authenticate
 * the mobile number.
 */
class MobileActivateTask(
    private val mobile: String,
    private val otp: String,
    private val name: String,
    private val deviceId: String,
    private val device: String,
    private val deviceName: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var mobileActivateTaskListener: MobileActivateTaskListener? = null
    private var response: JSONObject? = null

    fun setMobileActivateTaskListener(mobileActivateTaskListener: MobileActivateTaskListener) {
        this.mobileActivateTaskListener = mobileActivateTaskListener
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        if (TextUtils.isEmpty(otp)) return false
        if (TextUtils.isEmpty(mobile)) return false
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_ID_PARAM] = deviceId
        data[MainApplicationSingleton.DEVICE_NAME_PARAM] = deviceName
        // adds the country code to the mobile number
        data[MainApplicationSingleton.MOBILE_PARAM] = mobile
        data[MainApplicationSingleton.OTP_PARAM] = otp

        if (!TextUtils.isEmpty(name)) data[MainApplicationSingleton.NAME_PARAM] = name
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        mobileActivateTaskListener?.setMobileActivateTaskToNull()
        if (success == true) {
            mobileActivateTaskListener?.onPostMobileActivateExecute(response)
        } else {
            mobileActivateTaskListener?.onPostMobileActivateExecuteFail(response)
        }
    }

    override fun onCancelled() {
        mobileActivateTaskListener?.setMobileActivateTaskToNull()
    }

    interface MobileActivateTaskListener {
        fun onPostMobileActivateExecute(response: JSONObject?)

        fun onPostMobileActivateExecuteFail(response: JSONObject?)

        fun setMobileActivateTaskToNull()
    }
}
