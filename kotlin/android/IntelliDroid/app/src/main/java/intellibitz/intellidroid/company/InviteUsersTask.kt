package intellibitz.intellidroid.company

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

class InviteUsersTask(
    private var inviteEmails: String?,
    private var invitedCompanyId: String?,
    uid: String?,
    token: String?,
    device: String?,
    deviceRef: String?,
    user: ContactItem?,
    url: String?,
    context: Context?
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    private var inviteUsersTaskListener: InviteUsersTaskListener? = null

    fun setInviteUsersTaskListener(inviteUsersTaskListener: InviteUsersTaskListener?) {
        this.inviteUsersTaskListener = inviteUsersTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (TextUtils.isEmpty(inviteEmails) || TextUtils.isEmpty(invitedCompanyId) || !prepareRequest()) {
            backgroundResult = false
            Log.e(
                TAG, "Invite param is NULL - fail: invited contacts - " +
                        inviteEmails + " : companyId - " + invitedCompanyId
            )
        } else {
            try {
                request.put(MainApplicationSingleton.INVITED_COMPANY_ID_PARAM, invitedCompanyId)
                request.put(MainApplicationSingleton.INVITE_EMAILS_PARAM, inviteEmails)
                backgroundResult = postJsonObjectRequest()
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, TAG + e)
                try {
                    response.put("error", TAG + e)
                } catch (e1: Throwable) {
                    e1.printStackTrace()
                    Log.e(TAG, TAG + e1)
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
            inviteUsersTaskListener?.onPostInviteUsersErrorResponse(response)
        }
    }

    override fun onCancelled() {
        inviteUsersTaskListener?.onPostInviteUsersErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError?) {
        super.onErrorResponse(error)
        inviteUsersTaskListener?.onPostInviteUsersErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject?) {
        inviteUsersTaskListener?.onPostInviteUsersResponse(response, inviteEmails, userItem)
        releaseContext()
    }

    interface InviteUsersTaskListener {
        fun onPostInviteUsersResponse(response: JSONObject?, companyName: String?, user: ContactItem?)
        fun onPostInviteUsersErrorResponse(response: JSONObject?)
    }

    companion object {
        const val TAG = "InviteUsersTask"
    }
}
