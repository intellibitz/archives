package intellibitz.intellidroid.task

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import org.json.JSONObject

/**
 */
class GetIpTask(
    private var b: Boolean,
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, context) {

    private var getIpTaskListener: GetIpTaskListener? = null

    fun setGetIpTaskListener(getIpTaskListener: GetIpTaskListener?) {
        this.getIpTaskListener = getIpTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            backgroundResult = false
        } else {
            try {
                backgroundResult = postJsonObjectRequest()
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, TAG + e.toString())
                try {
                    response.put("error", e.toString())
                } catch (e1: Throwable) {
                    e1.printStackTrace()
                    Log.e(TAG, TAG + e1.toString())
                }
                backgroundResult = false
            }
        }
        return backgroundResult
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            Log.d(TAG, "onPostExecute: success!")
        } else {
            getIpTaskListener?.onPostGetIpErrorResponse(response, b)
        }
    }

    override fun onCancelled() {
        getIpTaskListener?.onPostGetIpErrorResponse(response, b)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        getIpTaskListener?.onPostGetIpErrorResponse(response, b)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        getIpTaskListener?.onPostGetIpResponse(response, b)
        releaseContext()
    }

    interface GetIpTaskListener {
        fun onPostGetIpResponse(response: JSONObject?, b: Boolean)
        fun onPostGetIpErrorResponse(response: JSONObject?, b: Boolean)
    }

    companion object {
        private const val TAG = "GetIpTask"
    }
}
