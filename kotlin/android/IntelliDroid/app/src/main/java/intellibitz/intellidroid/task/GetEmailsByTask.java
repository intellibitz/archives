package intellibitz.intellidroid.task;

import android.content.Context;
import android.text.TextUtils;
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
public class GetEmailsByTask extends
        IntellibitzUserAsyncTask implements
        Response.ErrorListener,
        Response.Listener<JSONObject> {
    public static final String TAG = "GetEmailsByTask";

    private GetEmailsByTaskListener getEmailsByTaskListener;
    private String emailAccount;
    private String byEmail;

    public GetEmailsByTask(String emailAccount, String byEmail, String uid, String token,
                           String device, String deviceRef, String url, Context context) {
        super(uid, token, device, deviceRef, url, context);
        this.emailAccount = emailAccount;
        this.byEmail = byEmail;
    }

    public void setGetEmailsByTaskListener(GetEmailsByTaskListener getEmailsByTaskListener) {
        this.getEmailsByTaskListener = getEmailsByTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        if (TextUtils.isEmpty(emailAccount)) {
            Log.e(TAG, "Email is NULL - fail");
            return false;
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_ACCOUNT_PARAM, emailAccount.trim());
            request.put(MainApplicationSingleton.BYEMAIL_PARAM, byEmail);
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
            getEmailsByTaskListener.onPostGetEmailsByErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        getEmailsByTaskListener.onPostGetEmailsByErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getEmailsByTaskListener.onPostGetEmailsByErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getEmailsByTaskListener.onPostGetEmailsByResponse(response);
        releaseContext();
    }

    public interface GetEmailsByTaskListener {
        void onPostGetEmailsByResponse(JSONObject response);

        void onPostGetEmailsByErrorResponse(JSONObject response);

//        void setGetEmailsByFromCloudTaskToNull();
    }

}
