package intellibitz.intellidroid.task

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 */
class RemoveEmailTask(
    private val email: String,
    private val uid: String,
    private val token: String,
    private val device: String,
    private val deviceRef: String,
    private val url: String,
    context: Context
) : AsyncTask<Void, Void, Boolean>(), Response.ErrorListener, Response.Listener<JSONObject> {

    companion object {
        const val TAG = "RemoveEmailTask"
    }

    private val context: WeakReference<Context> = WeakReference(context)
    private var removeEmailTaskListener: RemoveEmailTaskListener? = null
    private var response: JSONObject = JSONObject()
    private var requestTimeoutMillis: Int = 30000

    fun setRemoveEmailTaskListener(removeEmailTaskListener: RemoveEmailTaskListener) {
        this.removeEmailTaskListener = removeEmailTaskListener
    }

    fun setRequestTimeoutMillis(requestTimeoutMillis: Int) {
        this.requestTimeoutMillis = requestTimeoutMillis
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val context = this.context.get()
        if (context == null) return false
        try {
            val data = JSONObject()
            data.put(MainApplicationSingleton.UID_PARAM, uid)
            data.put(MainApplicationSingleton.DEVICE_PARAM, device)
            data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
            data.put(MainApplicationSingleton.TOKEN_PARAM, token)
            data.put(MainApplicationSingleton.EMAIL_PARAM, email)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url,
                data,
                this,
                this
            )
            jsonObjectRequest.setRetryPolicy(
                DefaultRetryPolicy(
                    requestTimeoutMillis,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
            MainApplicationSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest)
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                response.put("error", e.toString())
            } catch (e1: Throwable) {
                e1.printStackTrace()
            }
            return false
        }
    }

    override fun onPostExecute(success: Boolean) {
        if (success) {
            Log.d(TAG, "onPostExecute: success!")
        } else {
            removeEmailTaskListener?.onPostRemoveEmailErrorResponse(response)
        }
    }

    override fun onCancelled() {
        removeEmailTaskListener?.setEmailRemoveTaskToNull()
    }

    override fun onResponse(response: JSONObject) {
        if (response == null) {
            removeEmailTaskListener?.onPostRemoveEmailErrorResponse(null)
        }
        removeEmailTaskListener?.onPostRemoveEmailResponse(response)
    }

    override fun onErrorResponse(error: VolleyError) {
        try {
            response = JSONObject()
            response.put("error", error.toString())
            removeEmailTaskListener?.onPostRemoveEmailErrorResponse(response)
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                response = JSONObject()
                response.put("error", "$TAG${e.localizedMessage}")
                removeEmailTaskListener?.onPostRemoveEmailErrorResponse(response)
            } catch (e1: Throwable) {
                e1.printStackTrace()
            }
        }
    }

    interface RemoveEmailTaskListener {
        fun onPostRemoveEmailResponse(response: JSONObject)
        fun onPostRemoveEmailErrorResponse(response: JSONObject?)
        fun setEmailRemoveTaskToNull()
    }
}
