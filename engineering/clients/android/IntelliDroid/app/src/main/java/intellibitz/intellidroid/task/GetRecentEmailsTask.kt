package intellibitz.intellidroid.task

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

/**
 */
class GetRecentEmailsTask(
    emailAccount: String,
    skip: Int,
    uid: String,
    token: String,
    device: String,
    deviceRef: String,
    user: ContactItem,
    url: String,
    context: Context
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    companion object {
        const val TAG = "GetRecentEmailsTask"
    }

    var getRecentEmailsTaskListener: GetRecentEmailsTaskListener? = null
    var skip: Int = skip
        private set
    var emailAccount: String = emailAccount
        private set

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        if (TextUtils.isEmpty(emailAccount)) {
            Log.e(TAG, "Email is NULL - fail")
            return false
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_ACCOUNT_PARAM, emailAccount.trim())
            request.put(MainApplicationSingleton.SKIP_PARAM, skip)
            if (postJsonObjectRequest()) {
                return true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "$TAG${e.toString()}")
            try {
                response.put("error", e.toString())
            } catch (e1: Throwable) {
                e1.printStackTrace()
                Log.e(TAG, "$TAG${e1.toString()}")
            }
        }
        return false
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            Log.d(TAG, "onPostExecute: success!")
        } else {
            getRecentEmailsTaskListener?.onPostGetRecentEmailsErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getRecentEmailsTaskListener?.onPostGetRecentEmailsErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError) {
        super.onErrorResponse(error)
        getRecentEmailsTaskListener?.onPostGetRecentEmailsErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject) {
        getRecentEmailsTaskListener?.onPostGetRecentEmailsResponse(response, emailAccount, skip, getUserItem())
        releaseContext()
    }

    interface GetRecentEmailsTaskListener {
        fun onPostGetRecentEmailsResponse(response: JSONObject, email: String, skip: Int, user: ContactItem)

        fun onPostGetRecentEmailsErrorResponse(response: JSONObject)
    }
}
