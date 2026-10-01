package intellibitz.intellidroid.company

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import org.json.JSONObject

class GetWorkContactsTask(
    private var companyId: String?,
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    user: ContactItem?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    private var getWorkContactsTaskListener: GetWorkContactsTaskListener? = null

    fun setGetWorkContactsTaskListener(getWorkContactsTaskListener: GetWorkContactsTaskListener?) {
        this.getWorkContactsTaskListener = getWorkContactsTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (TextUtils.isEmpty(companyId)) {
            Log.e(TAG, "Company name param is NULL - fail")
            return false
        }
        if (!prepareRequest()) {
            return false
        }
        try {
            request.put("company_id", companyId)
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
            getWorkContactsTaskListener?.onPostGetWorkContactsErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getWorkContactsTaskListener?.onPostGetWorkContactsErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        getWorkContactsTaskListener?.onPostGetWorkContactsErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        getWorkContactsTaskListener?.onPostGetWorkContactsResponse(response, companyId, userItem)
        releaseContext()
    }

    interface GetWorkContactsTaskListener {
        fun onPostGetWorkContactsResponse(response: JSONObject?, companyId: String?, user: ContactItem?)
        fun onPostGetWorkContactsErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "GetWorkContactsTask"
    }
}
