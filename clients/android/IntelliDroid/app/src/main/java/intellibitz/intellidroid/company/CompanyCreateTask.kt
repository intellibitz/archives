package intellibitz.intellidroid.company

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class CompanyCreateTask(
    private var companyName: String?,
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    user: ContactItem?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    private var companyCreateTaskListener: CompanyCreateTaskListener? = null

    fun setCompanyCreateTaskListener(companyCreateTaskListener: CompanyCreateTaskListener?) {
        this.companyCreateTaskListener = companyCreateTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        if (TextUtils.isEmpty(companyName)) {
            Log.e(TAG, "Company name param is NULL - fail")
            return false
        }
        try {
            request.put(MainApplicationSingleton.COMPANY_NAME_PARAM, companyName)
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
            companyCreateTaskListener?.onPostCompanyCreateErrorResponse(response)
        }
    }

    override fun onCancelled() {
        companyCreateTaskListener?.onPostCompanyCreateErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        companyCreateTaskListener?.onPostCompanyCreateErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        companyCreateTaskListener?.onPostCompanyCreateResponse(response, companyName, userItem)
        releaseContext()
    }

    interface CompanyCreateTaskListener {
        fun onPostCompanyCreateResponse(response: JSONObject?, companyName: String?, user: ContactItem?)
        fun onPostCompanyCreateErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "CompCreateTask"
    }
}
