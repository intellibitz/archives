package intellibitz.intellidroid.task

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

/**
 */
class GetMailboxListTask(
    apiUser: String,
    apiKey: String,
    private val email: String,
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
        const val TAG = "GetMailboxListTask"
    }

    private var getMailboxListTaskListener: GetMailboxListTaskListener? = null

    fun setGetMailboxListTaskListener(getMailboxListTaskListener: GetMailboxListTaskListener) {
        this.getMailboxListTaskListener = getMailboxListTaskListener
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_ACCOUNT_PARAM, email)
            if (postJsonObjectRequest()) {
                return true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "$TAG$e")
            try {
                response.put("error", e.toString())
            } catch (e1: Throwable) {
                e1.printStackTrace()
                Log.e(TAG, "$TAG$e1")
            }
        }
        return false
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            Log.d(TAG, "onPostExecute: success!")
        } else {
            getMailboxListTaskListener?.onPostGetMailboxListErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getMailboxListTaskListener?.onPostGetMailboxListErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError) {
        super.onErrorResponse(error)
        getMailboxListTaskListener?.onPostGetMailboxListErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject) {
        getMailboxListTaskListener?.onPostGetMailboxListResponse(response)
        releaseContext()
    }

    interface GetMailboxListTaskListener {
        fun onPostGetMailboxListResponse(response: JSONObject)

        fun onPostGetMailboxListErrorResponse(response: JSONObject)
    }
}
