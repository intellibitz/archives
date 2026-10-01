package intellibitz.intellidroid.task

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import org.json.JSONObject

/**
 */
class GetEmailsTask(
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    url: String?,
    user: ContactItem?,
    context: Context?,
    private var mode: Int
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    companion object {
        const val TAG = "GetEmailsTask"
    }

    private var getEmailsTaskListener: GetEmailsTaskListener? = null

    fun setGetEmailsTaskListener(getEmailsTaskListener: GetEmailsTaskListener?) {
        this.getEmailsTaskListener = getEmailsTaskListener
    }

    fun setRequestTimeoutMillis(requestTimeoutMillis: Int) {
        this.requestTimeoutMillis = requestTimeoutMillis
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        try {
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
        if (this.success) {
            Log.d(TAG, "onPostExecute: success!")
        } else {
            getEmailsTaskListener?.onPostGetEmailsErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getEmailsTaskListener?.onPostGetEmailsErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        if (null == response) getEmailsTaskListener?.onPostGetEmailsErrorResponse(null)
        getEmailsTaskListener?.onPostGetEmailsResponse(response, getUserItem(), mode)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError) {
        try {
            response = JSONObject()
            response.put("error", error.toString())
            getEmailsTaskListener?.onPostGetEmailsErrorResponse(response)
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                response = JSONObject()
                response.put("error", "$TAG${e.localizedMessage}")
                getEmailsTaskListener?.onPostGetEmailsErrorResponse(response)
            } catch (e1: Throwable) {
                e1.printStackTrace()
            }
        }
        releaseContext()
    }

    interface GetEmailsTaskListener {
        fun onPostGetEmailsResponse(response: JSONObject?, userItem: ContactItem?, mode: Int)

        fun onPostGetEmailsErrorResponse(response: JSONObject?)
    }
}
