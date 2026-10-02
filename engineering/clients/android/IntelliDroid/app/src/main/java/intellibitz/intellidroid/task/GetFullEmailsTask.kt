package intellibitz.intellidroid.task

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONObject

/**
 */
class GetFullEmailsTask(
    emailAccount: String,
    emailUids: Array<String>,
    uid: String,
    token: String,
    device: String,
    deviceRef: String,
    user: ContactItem,
    url: String,
    context: Context,
    mode: Int
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    companion object {
        const val TAG = "GetFullEmailsTask"
    }

    private var getFullEmailsTaskListener: GetFullEmailsTaskListener? = null
    private val emailAccount: String = emailAccount
    private var emailUids: String? = null
    private val emailUidsArray: Array<String> = emailUids
    private val mode: Int = mode

    init {
        val jsonArray = JSONArray()
        if (emailUids != null && emailUids.isNotEmpty()) {
            for (id in emailUids) {
                try {
                    val value = id.toInt()
                    jsonArray.put(value)
                } catch (ignored: Throwable) {
                    Log.e(TAG, "GetFullEmailsTask: email ids to retrieve is not an int - skipping $id")
                }
            }
            this.emailUids = jsonArray.toString()
        }
    }

    fun setGetFullEmailsTaskListener(getFullEmailsTaskListener: GetFullEmailsTaskListener) {
        this.getFullEmailsTaskListener = getFullEmailsTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (TextUtils.isEmpty(emailAccount) || TextUtils.isEmpty(emailUids) || !prepareRequest()) {
            Log.e(TAG, "email id param is NULL - fail")
            backgroundResult = false
        } else {
            try {
                request.put(MainApplicationSingleton.EMAIL_ACCOUNT_PARAM, emailAccount.trim())
                request.put(MainApplicationSingleton.EMAIL_UIDS_PARAM, emailUids)
                backgroundResult = postJsonObjectRequest()
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, "$TAG${e.toString()}")
                try {
                    response.put("error", e.toString())
                } catch (e1: Throwable) {
                    e1.printStackTrace()
                    Log.e(TAG, "$TAG${e1.toString()}")
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
            getFullEmailsTaskListener?.onPostGetFullEmailsErrorResponse(response)
        }
    }

    override fun onCancelled() {
        getFullEmailsTaskListener?.onPostGetFullEmailsErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError) {
        super.onErrorResponse(error)
        getFullEmailsTaskListener?.onPostGetFullEmailsErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject) {
        getFullEmailsTaskListener?.onPostGetFullEmailsResponse(response, emailUidsArray, emailAccount, getUserItem(), mode)
        releaseContext()
    }

    interface GetFullEmailsTaskListener {
        fun onPostGetFullEmailsResponse(response: JSONObject, emailUidsArray: Array<String>, emailAccount: String, userItem: ContactItem, mode: Int)
        fun onPostGetFullEmailsErrorResponse(response: JSONObject)
    }
}
