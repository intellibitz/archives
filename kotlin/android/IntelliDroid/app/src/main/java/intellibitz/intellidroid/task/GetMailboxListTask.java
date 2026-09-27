package intellibitz.intellidroid.task;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import org.json.JSONObject;

/**
 */
public class GetMailboxListTask extends
        IntellibitzUserAsyncTask implements
        Response.ErrorListener,
        Response.Listener<JSONObject> {
    public static final String TAG = "GetMailboxListTask";

    private GetMailboxListTaskListener getMailboxListTaskListener;
    private String email;

    public GetMailboxListTask(String apiUser, String apiKey, String email, String uid, String token,
                              String device, String deviceRef, String url, Context context) {
        super(uid, token, device, deviceRef, url, context);
        this.email = email;
    }

    public void setGetMailboxListTaskListener(GetMailboxListTaskListener getMailboxListTaskListener) {
        this.getMailboxListTaskListener = getMailboxListTaskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_ACCOUNT_PARAM, email);
            if (postJsonObjectRequest()) {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.toString());
            try {
                response.put("error", e.toString());
            } catch (Throwable e1) {
                e1.printStackTrace();
                Log.e(TAG, TAG + e1.toString());
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            getMailboxListTaskListener.onPostGetMailboxListErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        getMailboxListTaskListener.onPostGetMailboxListErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getMailboxListTaskListener.onPostGetMailboxListErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getMailboxListTaskListener.onPostGetMailboxListResponse(response);
        releaseContext();
    }

    public interface GetMailboxListTaskListener {
        void onPostGetMailboxListResponse(JSONObject response);

        void onPostGetMailboxListErrorResponse(JSONObject response);
    }

}
