package intellibitz.intellidroid.account

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class SignupTask(
    private var email: String?,
    private var code: String?,
    private var name: String?,
    private var pwd: String?,
    device: String?,
    private var deviceId: String?,
    private var deviceName: String?,
    private var status: String?,
    url: String?,
    user: ContactItem?,
    context: Context?
) : IntellibitzUserAsyncTask(device, url, user, context) {

    private var signupTaskListener: SignupTaskListener? = null

    fun setSignupTaskListener(signupTaskListener: SignupTaskListener?) {
        this.signupTaskListener = signupTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "Email param is NULL - fail")
            return false
        }
        if (TextUtils.isEmpty(code)) {
            Log.e(TAG, "Code param is NULL - fail")
            return false
        }
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "Name param is NULL - fail")
            return false
        }
        if (TextUtils.isEmpty(pwd)) {
            Log.e(TAG, "Password param is NULL - fail")
            return false
        }
        if (TextUtils.isEmpty(device)) {
            Log.e(TAG, "Device param is NULL - fail")
            return false
        }
        if (TextUtils.isEmpty(deviceName)) {
            Log.e(TAG, "Device name is NULL - fail")
            return false
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_PARAM, email?.trim())
            request.put(MainApplicationSingleton.CODE_PARAM, code)
            request.put(MainApplicationSingleton.NAME_PARAM, name)
            request.put(MainApplicationSingleton.PWD_PARAM, pwd)
            request.put(MainApplicationSingleton.DEVICE_ID_PARAM, deviceId)
            request.put(MainApplicationSingleton.DEVICE_NAME_PARAM, deviceName)
            request.put(MainApplicationSingleton.STATUS_PARAM, status)
            if (postJsonObjectRequest()) {
                return true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, TAG + e.toString())
            try {
                response.put("error", e.toString())
            } catch (e1: Throwable) {
                e1.printStackTrace()
                Log.e(TAG, TAG + e1.toString())
            }
        }
        return false
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            Log.d(TAG, "onPostExecute: success!")
        } else {
            signupTaskListener?.onPostSignupErrorResponse(response)
        }
    }

    override fun onCancelled() {
        signupTaskListener?.onPostSignupErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        signupTaskListener?.onPostSignupErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        signupTaskListener?.onPostSignupResponse(response, email, userItem)
        releaseContext()
    }

    interface SignupTaskListener {
        fun onPostSignupResponse(response: JSONObject?, email: String?, user: ContactItem?)
        fun onPostSignupErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "SignupTask"
    }
}
