package intellibitz.intellidroid.account

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class CheckEmailAvailableTask(
    private var email: String?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(url, context) {

    private var checkEmailAvailableTaskListener: CheckEmailAvailableTaskListener? = null

    fun setCheckEmailAvailableTaskListener(checkEmailAvailableTaskListener: CheckEmailAvailableTaskListener?) {
        this.checkEmailAvailableTaskListener = checkEmailAvailableTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "Email is NULL - fail")
            return false
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_PARAM, email?.trim())
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
            checkEmailAvailableTaskListener?.onPostCheckEmailAvailableErrorResponse(response)
        }
    }

    override fun onCancelled() {
        checkEmailAvailableTaskListener?.onPostCheckEmailAvailableErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        checkEmailAvailableTaskListener?.onPostCheckEmailAvailableErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        checkEmailAvailableTaskListener?.onPostCheckEmailAvailableResponse(response, email, userItem)
        releaseContext()
    }

    interface CheckEmailAvailableTaskListener {
        fun onPostCheckEmailAvailableResponse(response: JSONObject?, email: String?, user: ContactItem?)
        fun onPostCheckEmailAvailableErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "ChkEmailAvblTask"
    }
}
