package intellibitz.intellidroid.company

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class CompanyJoinTask(
    private var invitedCompanyId: String?,
    private var contactItem: ContactItem?,
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    user: ContactItem?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    private var companyJoinTaskListener: CompanyJoinTaskListener? = null

    fun setCompanyJoinTaskListener(companyJoinTaskListener: CompanyJoinTaskListener?) {
        this.companyJoinTaskListener = companyJoinTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        if (TextUtils.isEmpty(invitedCompanyId)) {
            Log.e(TAG, "Company name param is NULL - fail")
            return false
        }
        try {
            request.put(MainApplicationSingleton.INVITED_COMPANY_ID_PARAM, invitedCompanyId)
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
            companyJoinTaskListener?.onPostCompanyJoinErrorResponse(response)
        }
    }

    override fun onCancelled() {
        companyJoinTaskListener?.onPostCompanyJoinErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        companyJoinTaskListener?.onPostCompanyJoinErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        companyJoinTaskListener?.onPostCompanyJoinResponse(response, invitedCompanyId, contactItem, userItem)
        releaseContext()
    }

    interface CompanyJoinTaskListener {
        fun onPostCompanyJoinResponse(
            response: JSONObject?,
            invitedCompanyId: String?,
            contactItem: ContactItem?,
            user: ContactItem?
        )
        fun onPostCompanyJoinErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "CompJoinTask"
    }
}
