package intellibitz.intellidroid.company

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import org.json.JSONObject

class GetInvitesTask(
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    user: ContactItem?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    private var getInvitesTaskListener: GetInvitesTaskListener? = null

    fun setGetInvitesTaskListener(getInvitesTaskListener: GetInvitesTaskListener?) {
        this.getInvitesTaskListener = getInvitesTaskListener
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
            getInvitesTaskListener?.onPostGetInvitesErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getInvitesTaskListener?.onPostGetInvitesErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        getInvitesTaskListener?.onPostGetInvitesErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        getInvitesTaskListener?.onPostGetInvitesResponse(response, userItem)
        releaseContext()
    }

    interface GetInvitesTaskListener {
        fun onPostGetInvitesResponse(response: JSONObject?, user: ContactItem?)
        fun onPostGetInvitesErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "GetInvitesTask"
    }
}
