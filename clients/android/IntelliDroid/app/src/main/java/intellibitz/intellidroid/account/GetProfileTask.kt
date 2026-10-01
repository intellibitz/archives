package intellibitz.intellidroid.account

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import org.json.JSONObject

class GetProfileTask(
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    user: ContactItem?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    private var getProfileTaskListener: GetProfileTaskListener? = null

    fun setGetProfileTaskListener(getProfileTaskListener: GetProfileTaskListener?) {
        this.getProfileTaskListener = getProfileTaskListener
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
            getProfileTaskListener?.onPostGetProfileErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getProfileTaskListener?.onPostGetProfileErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        getProfileTaskListener?.onPostGetProfileErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        if (null == response) {
            getProfileTaskListener?.onPostGetProfileErrorResponse(response)
        }
        getProfileTaskListener?.onPostGetProfileResponse(response, userItem)
        releaseContext()
    }

    interface GetProfileTaskListener {
        fun onPostGetProfileResponse(response: JSONObject?, user: ContactItem?)
        fun onPostGetProfileErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "GetProfileTask"
    }
}
