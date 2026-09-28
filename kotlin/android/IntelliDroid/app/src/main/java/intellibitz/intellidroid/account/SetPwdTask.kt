package intellibitz.intellidroid.account

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class SetPwdTask(
    private var email: String?,
    private var mode: Int,
    private var code: String?,
    private var pwd: String?,
    device: String?,
    private var deviceId: String?,
    private var deviceName: String?,
    url: String?,
    user: ContactItem?,
    context: Context?
) : IntellibitzUserAsyncTask(device, url, user, context) {

    private var setPwdTaskListener: SetPwdTaskListener? = null

    fun setSetPwdTaskListener(setPwdTaskListener: SetPwdTaskListener?) {
        this.setPwdTaskListener = setPwdTaskListener
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
            request.put(MainApplicationSingleton.PWD_PARAM, pwd)
            request.put(MainApplicationSingleton.DEVICE_ID_PARAM, deviceId)
            request.put(MainApplicationSingleton.DEVICE_NAME_PARAM, deviceName)
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
            setPwdTaskListener?.onPostSetPwdErrorResponse(response)
        }
    }

    override fun onCancelled() {
        setPwdTaskListener?.onPostSetPwdErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        setPwdTaskListener?.onPostSetPwdErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        setPwdTaskListener?.onPostSetPwdResponse(response, email, userItem, mode)
        releaseContext()
    }

    interface SetPwdTaskListener {
        fun onPostSetPwdResponse(response: JSONObject?, email: String?, user: ContactItem?, mode: Int)
        fun onPostSetPwdErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "SetPwdTask"
    }
}
