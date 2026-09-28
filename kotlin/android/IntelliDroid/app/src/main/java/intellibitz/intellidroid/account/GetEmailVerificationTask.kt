package intellibitz.intellidroid.account

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class GetEmailVerificationTask(
    private var email: String?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(url, context) {

    private var getEmailVerificationTaskListener: GetEmailVerificationTaskListener? = null

    fun setGetEmailVerificationTaskListener(getEmailVerificationTaskListener: GetEmailVerificationTaskListener?) {
        this.getEmailVerificationTaskListener = getEmailVerificationTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "Email param is NULL - fail")
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
            getEmailVerificationTaskListener?.onPostGetEmailVerificationErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getEmailVerificationTaskListener?.onPostGetEmailVerificationErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        getEmailVerificationTaskListener?.onPostGetEmailVerificationErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        getEmailVerificationTaskListener?.onPostGetEmailVerificationResponse(response, email, userItem)
        releaseContext()
    }

    interface GetEmailVerificationTaskListener {
        fun onPostGetEmailVerificationResponse(response: JSONObject?, email: String?, user: ContactItem?)
        fun onPostGetEmailVerificationErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "GetEmailVerTask"
    }
}
