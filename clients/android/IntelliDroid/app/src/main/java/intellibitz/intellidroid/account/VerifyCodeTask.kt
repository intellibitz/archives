package intellibitz.intellidroid.account

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class VerifyCodeTask(
    private var email: String?,
    private var code: String?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(url, context) {

    private var verifyCodeTaskListener: VerifyCodeTaskListener? = null

    fun setVerifyCodeTaskListener(verifyCodeTaskListener: VerifyCodeTaskListener?) {
        this.verifyCodeTaskListener = verifyCodeTaskListener
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
        try {
            request.put(MainApplicationSingleton.EMAIL_PARAM, email?.trim())
            request.put(MainApplicationSingleton.CODE_PARAM, code)
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
            verifyCodeTaskListener?.onPostVerifyCodeErrorResponse(response)
        }
    }

    override fun onCancelled() {
        verifyCodeTaskListener?.onPostVerifyCodeErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        verifyCodeTaskListener?.onPostVerifyCodeErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        verifyCodeTaskListener?.onPostVerifyCodeResponse(response, email, userItem)
        releaseContext()
    }

    interface VerifyCodeTaskListener {
        fun onPostVerifyCodeResponse(response: JSONObject?, email: String?, user: ContactItem?)
        fun onPostVerifyCodeErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "VerifyCodeTask"
    }
}
