package intellibitz.intellidroid.task

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import intellibitz.intellidroid.IntellibitzUserAsyncTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class DeleteMsgsTask(
    private val ids: Array<String>,
    uid: String,
    token: String,
    device: String,
    deviceRef: String,
    url: String,
    context: Context
) : IntellibitzUserAsyncTask(uid, token, device, deviceRef, url, context) {

    companion object {
        private const val TAG = "DeleteMsgsTask"
    }

    private var deleteMsgsTaskListener: DeleteMsgsTaskListener? = null

    fun setDeleteMsgsTaskListener(deleteMsgsTaskListener: DeleteMsgsTaskListener) {
        this.deleteMsgsTaskListener = deleteMsgsTaskListener
    }

    override fun doInBackground(vararg params: Any?): Boolean? {
        if (!prepareRequest()) {
            return false
        }
        if (ids.isNullOrEmpty()) {
            Log.e(TAG, "Msgs ids params is NULL - fail")
            return false
        }
        try {
            val array = JSONArray()
            for (id in ids) {
                array.put(id)
            }
            request.put(MainApplicationSingleton.MSGS_PARAM, array.toString())
            if (postJsonObjectRequest()) {
                return true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "$TAG$e")
            try {
                response.put("error", e.toString())
            } catch (e1: Throwable) {
                e1.printStackTrace()
                Log.e(TAG, "$TAG$e1")
            }
        }
        return false
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        if (success) {
            deleteMsgsTaskListener?.onPostDeleteMsgsResponse(response, ids)
        } else {
            Log.e(TAG, ":error - $response")
            deleteMsgsTaskListener?.onPostDeleteMsgsErrorResponse(response, ids)
        }
    }

    override fun onCancelled() {
        deleteMsgsTaskListener?.onPostDeleteMsgsErrorResponse(response, ids)
        releaseContext()
    }

    override fun onErrorResponse(error: VolleyError) {
        super.onErrorResponse(error)
        deleteMsgsTaskListener?.onPostDeleteMsgsErrorResponse(response, ids)
        releaseContext()
    }

    override fun onResponse(response: JSONObject) {
        deleteMsgsTaskListener?.onPostDeleteMsgsResponse(response, ids)
        releaseContext()
    }

    interface DeleteMsgsTaskListener {
        fun onPostDeleteMsgsResponse(response: JSONObject, item: Array<String>)

        fun onPostDeleteMsgsErrorResponse(response: JSONObject, item: Array<String>)
    }
}
