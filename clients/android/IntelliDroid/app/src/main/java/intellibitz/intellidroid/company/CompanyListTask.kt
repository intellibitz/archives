package intellibitz.intellidroid.company

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import org.json.JSONObject

class CompanyListTask(
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    user: ContactItem?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    private var companyListTaskListener: CompanyListTaskListener? = null

    fun setCompanyListTaskListener(companyListTaskListener: CompanyListTaskListener?) {
        this.companyListTaskListener = companyListTaskListener
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
            companyListTaskListener?.onPostCompanyListErrorResponse(response)
        }
    }

    override fun onCancelled() {
        companyListTaskListener?.onPostCompanyListErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        companyListTaskListener?.onPostCompanyListErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        companyListTaskListener?.onPostCompanyListResponse(response, userItem)
        releaseContext()
    }

    interface CompanyListTaskListener {
        fun onPostCompanyListResponse(response: JSONObject?, user: ContactItem?)
        fun onPostCompanyListErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "CompanyListTask"
    }
}
