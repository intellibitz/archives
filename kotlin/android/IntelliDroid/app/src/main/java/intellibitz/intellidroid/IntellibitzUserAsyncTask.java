package intellibitz.intellidroid;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.lang.ref.SoftReference;

/**
 */
public abstract class IntellibitzUserAsyncTask extends
        IntellibitzAsyncTask implements
        Response.ErrorListener,
        Response.Listener<JSONObject> {
    public static final String TAG = "IntellibitzUserAsyncTask";

    protected final SoftReference<ContactItem> user;
    protected ContactItem userItem;
    protected String uid;
    protected String token;
    protected String device = "android";
    protected String deviceRef;
    //    protected String apiUser;
//    protected String apiKey;
    protected String url;
    protected JSONObject request = new JSONObject();
    protected JSONObject response = new JSONObject();
    protected int requestTimeoutMillis = 30000;
    protected boolean backgroundResult = false;

    public IntellibitzUserAsyncTask(String url, Context context) {
        this(null, null, null, null, url, context);
    }

    public IntellibitzUserAsyncTask(String device, String url, ContactItem user, Context context) {
        this(null, null, device, null, url, user, context);
    }

    public IntellibitzUserAsyncTask(String uid, String token,
                                    String device, String deviceRef,
                                    String url) {
        this(uid, token, device, deviceRef, url, null);
    }

    public IntellibitzUserAsyncTask(String uid, String token,
                                    String device, String deviceRef,
                                    String url, Context context) {
        this(uid, token, device, deviceRef, url, null, context);
    }

    public IntellibitzUserAsyncTask(String uid, String token,
                                    String device, String deviceRef,
                                    String url, ContactItem user, Context context) {
        super(context);
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
        this.userItem = user;
        this.user = new SoftReference<>(this.userItem);
    }

    protected ContactItem getUserItem() {
        ContactItem user = this.user.get();
        if (null == user) {
            user = userItem;
        }
        return user;
    }

    public void setRequestTimeoutMillis(int requestTimeoutMillis) {
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    protected boolean prepareRequest() {
        try {
            request.put(MainApplicationSingleton.UID_PARAM, uid);
            request.put(MainApplicationSingleton.DEVICE_PARAM, device);
            request.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            request.put(MainApplicationSingleton.TOKEN_PARAM, token);
            backgroundResult = true;
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e);
            try {
                response.put("error", TAG + e);
            } catch (Throwable e1) {
                e1.printStackTrace();
                Log.e(TAG, TAG + e);
            }
            backgroundResult = false;
        }
        return backgroundResult;
    }

    protected boolean postJsonObjectRequest() {
        Context context1 = getContextRef();
        if (null == context1) {
            Log.e(TAG, "Context is NULL - fail");
            backgroundResult = false;
        } else {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url, request, this, this);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    requestTimeoutMillis,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainApplicationSingleton.getInstance(context1).addToRequestQueue(jsonObjectRequest);
            backgroundResult = true;
        }
        return backgroundResult;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try {
            response = new JSONObject();
            response.put("error", error.toString());
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                response = new JSONObject();
                response.put("error", TAG + e.getLocalizedMessage());
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
    }

}
