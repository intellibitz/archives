package intellibitz.intellidroid

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.lang.ref.SoftReference

abstract class IntellibitzUserAsyncTask : IntellibitzAsyncTask,
    Response.ErrorListener,
    Response.Listener<JSONObject> {

    companion object {
        const val TAG = "IntellibitzUserAsyncTask"
    }

    @JvmField
    protected val user: SoftReference<ContactItem?>
    @JvmField
    protected var userItem: ContactItem?
    @JvmField
    protected var uid: String? = null
    @JvmField
    protected var token: String? = null
    @JvmField
    protected var device: String? = "android"
    @JvmField
    protected var deviceRef: String? = null
    @JvmField
    protected var url: String? = null
    @JvmField
    protected var request: JSONObject = JSONObject()
    @JvmField
    protected var response: JSONObject = JSONObject()
    @JvmField
    protected var requestTimeoutMillis = 30000
    @JvmField
    protected var backgroundResult = false

    constructor(url: String?, context: Context?) : this(null, null, null, null, url, context)

    constructor(device: String?, url: String?, user: ContactItem?, context: Context?) : this(
        null,
        null,
        device,
        null,
        url,
        user,
        context
    )

    constructor(
        uid: String?,
        token: String?,
        device: String?,
        deviceRef: String?,
        url: String?
    ) : this(uid, token, device, deviceRef, url, null, null)

    constructor(
        uid: String?,
        token: String?,
        device: String?,
        deviceRef: String?,
        url: String?,
        context: Context?
    ) : this(uid, token, device, deviceRef, url, null, context)

    constructor(
        uid: String?,
        token: String?,
        device: String?,
        deviceRef: String?,
        url: String?,
        user: ContactItem?,
        context: Context?
    ) : super(context) {
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
        this.userItem = user
        this.user = SoftReference(this.userItem)
    }

    protected open fun getUserItem(): ContactItem? {
        var u = user.get()
        if (u == null) {
            u = userItem
        }
        return u
    }

    fun setRequestTimeoutMillis(requestTimeoutMillis: Int) {
        this.requestTimeoutMillis = requestTimeoutMillis
    }

    protected open fun prepareRequest(): Boolean {
        try {
            request.put(MainApplicationSingleton.UID_PARAM, uid)
            request.put(MainApplicationSingleton.DEVICE_PARAM, device)
            request.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
            request.put(MainApplicationSingleton.TOKEN_PARAM, token)
            backgroundResult = true
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, TAG + e)
            try {
                response.put("error", TAG + e)
            } catch (e1: Throwable) {
                e1.printStackTrace()
                Log.e(TAG, TAG + e)
            }
            backgroundResult = false
        }
        return backgroundResult
    }

    protected open fun postJsonObjectRequest(): Boolean {
        val context1 = getContextRef()
        if (context1 == null) {
            Log.e(TAG, "Context is NULL - fail")
            backgroundResult = false
        } else {
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST,
                url, request, this, this
            )
            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                requestTimeoutMillis,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            MainApplicationSingleton.getInstance(context1).addToRequestQueue(jsonObjectRequest)
            backgroundResult = true
        }
        return backgroundResult
    }

    override fun onErrorResponse(error: VolleyError) {
        try {
            response = JSONObject()
            response.put("error", error.toString())
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                response = JSONObject()
                response.put("error", TAG + e.localizedMessage)
            } catch (e1: Throwable) {
                e1.printStackTrace()
            }
        }
    }
}
