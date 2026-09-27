package intellibitz.intellidroid.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 */
public class RemoveEmailTask extends
        AsyncTask<Void, Void, Boolean> implements
        Response.ErrorListener,
        Response.Listener<JSONObject> {
    public static final String TAG = "RemoveEmailTask";
    private final WeakReference<Context> context;
    private RemoveEmailTaskListener removeEmailTaskListener;
    private String email;
    private String uid;
    private String token;
    private String device;
    private String deviceRef;
    private String url;
    private JSONObject response = new JSONObject();
    private int requestTimeoutMillis = 30000;

    public RemoveEmailTask(String email, String uid, String token, String device, String deviceRef,
                           String url, Context context) {
        this.email = email;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.deviceRef = deviceRef;
        this.url = url;
        this.context = new WeakReference<>(context);
    }

    public void setRemoveEmailTaskListener(RemoveEmailTaskListener removeEmailTaskListener) {
        this.removeEmailTaskListener = removeEmailTaskListener;
    }

    public void setRequestTimeoutMillis(int requestTimeoutMillis) {
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Context context = this.context.get();
        if (null == context) return false;
        try {
            JSONObject data = new JSONObject();
            data.put(MainApplicationSingleton.UID_PARAM, uid);
            data.put(MainApplicationSingleton.DEVICE_PARAM, device);
            data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            data.put(MainApplicationSingleton.TOKEN_PARAM, token);
            data.put(MainApplicationSingleton.EMAIL_PARAM, email);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url, data, this, this);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    requestTimeoutMillis,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            MainApplicationSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                response.put("error", e.toString());
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            removeEmailTaskListener.onPostRemoveEmailErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        removeEmailTaskListener.setEmailRemoveTaskToNull();
    }

    @Override
    public void onResponse(JSONObject response) {
        if (null == response)
            removeEmailTaskListener.onPostRemoveEmailErrorResponse(null);
        removeEmailTaskListener.onPostRemoveEmailResponse(response);
//        removeEmailTaskListener.setEmailRemoveTaskToNull();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try {
            response = new JSONObject();
            response.put("error", error.toString());
            removeEmailTaskListener.onPostRemoveEmailErrorResponse(response);
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                response = new JSONObject();
                response.put("error", TAG + e.getLocalizedMessage());
                removeEmailTaskListener.onPostRemoveEmailErrorResponse(response);
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
//        removeEmailTaskListener.setEmailRemoveTaskToNull();
    }

    public interface RemoveEmailTaskListener {
        void onPostRemoveEmailResponse(JSONObject response);

        void onPostRemoveEmailErrorResponse(JSONObject response);

        void setEmailRemoveTaskToNull();
    }

}
