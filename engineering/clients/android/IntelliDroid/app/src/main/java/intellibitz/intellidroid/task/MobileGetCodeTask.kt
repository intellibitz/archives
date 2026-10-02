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
class MobileGetCodeTask(
    private val device: String,
    private val mobile: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var mobileGetCodeTaskListener: MobileGetCodeTaskListener? = null
    private var response: JSONObject? = null

    fun setMobileGetCodeTaskListener(mobileGetCodeTaskListener: MobileGetCodeTaskListener) {
        this.mobileGetCodeTaskListener = mobileGetCodeTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        // TODO: attempt authentication against a network service.

        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
//            normalizeUserMobile();
        data[MainApplicationSingleton.MOBILE_PARAM] = mobile
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        mobileGetCodeTaskListener?.setMobileGetCodeTaskToNull()
        if (success) {
            mobileGetCodeTaskListener?.onPostMobileGetCodeExecute(response)
        } else {
            mobileGetCodeTaskListener?.onPostMobileGetCodeExecuteFail(response)
        }
    }

    override fun onCancelled() {
        mobileGetCodeTaskListener?.setMobileGetCodeTaskToNull()
    }

    interface MobileGetCodeTaskListener {
        fun onPostMobileGetCodeExecute(response: JSONObject?)

        fun onPostMobileGetCodeExecuteFail(response: JSONObject?)

        fun setMobileGetCodeTaskToNull()
    }

    companion object {
        private const val TAG = "CreateGroupTask"
    }
}
