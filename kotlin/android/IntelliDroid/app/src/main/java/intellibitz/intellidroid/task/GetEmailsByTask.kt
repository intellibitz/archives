package intellibitz.intellidroid.task

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

/**
 */
class GetEmailsByTask(
    emailAccount: String,
    byEmail: String,
    uid: String,
    token: String,
    device: String,
    deviceRef: String,
    url: String,
    context: Context
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, context),
    Response.ErrorListener,
    Response.Listener<JSONObject> {

    companion object {
        const val TAG = "GetEmailsByTask"
    }

    private var getEmailsByTaskListener: GetEmailsByTaskListener? = null
    private val emailAccount: String = emailAccount
    private val byEmail: String = byEmail

    fun setGetEmailsByTaskListener(getEmailsByTaskListener: GetEmailsByTaskListener) {
        this.getEmailsByTaskListener = getEmailsByTaskListener
    }

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
            request.put(MainApplicationSingleton.BYEMAIL_PARAM, byEmail)
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
            getEmailsByTaskListener?.onPostGetEmailsByErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getEmailsByTaskListener?.onPostGetEmailsByErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError) {
        super.onErrorResponse(error)
        getEmailsByTaskListener?.onPostGetEmailsByErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject) {
        getEmailsByTaskListener?.onPostGetEmailsByResponse(response)
        releaseContext()
    }

    interface GetEmailsByTaskListener {
        fun onPostGetEmailsByResponse(response: JSONObject)

        fun onPostGetEmailsByErrorResponse(response: JSONObject)

//        fun setGetEmailsByFromCloudTaskToNull()
    }
}
