package intellibitz.intellidroid.task

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class GCMTokenUploadTask(
    private val gcm: String,
    uid: String,
    token: String,
    device: String,
    deviceRef: String,
    url: String,
    user: ContactItem,
    context: Context
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, user, context) {

    companion object {
        const val TAG = "GCMTokenUploadTask"
    }

    private var gcmTokenUploadTaskListener: GCMTokenUploadTaskListener? = null

    fun setGcmTokenUploadTaskListener(contactsUploadTaskListener: GCMTokenUploadTaskListener) {
        this.gcmTokenUploadTaskListener = contactsUploadTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (TextUtils.isEmpty(gcm) || !prepareRequest()) {
            Log.e(TAG, "GCM param is NULL - fail")
            backgroundResult = false
        } else {
            try {
                request.put(MainApplicationSingleton.GCM_TOKEN_PARAM, gcm)
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
            gcmTokenUploadTaskListener?.onPostGCMTokenUploadErrorResponse(response)
        }
    }

    override fun onCancelled() {
        gcmTokenUploadTaskListener?.onPostGCMTokenUploadErrorResponse(response)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError) {
        super.onErrorResponse(error)
        gcmTokenUploadTaskListener?.onPostGCMTokenUploadErrorResponse(response)
        releaseContext()
    }

    override fun onResponse(response: JSONObject) {
        gcmTokenUploadTaskListener?.onPostGCMTokenUploadResponse(response, getUserItem())
        releaseContext()
    }

    interface GCMTokenUploadTaskListener {
        fun onPostGCMTokenUploadResponse(response: JSONObject, user: ContactItem)

        fun onPostGCMTokenUploadErrorResponse(response: JSONObject)
    }
}
