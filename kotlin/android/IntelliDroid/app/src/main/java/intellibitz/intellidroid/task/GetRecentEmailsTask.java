package intellibitz.intellidroid.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import org.json.JSONObject;

/**
 */
public class GetRecentEmailsTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GetRecentEmailsTask";

    private GetRecentEmailsTaskListener getRecentEmailsTaskListener;
    private int skip = 0;
    private String emailAccount;

    public GetRecentEmailsTask(String emailAccount, int skip, String uid, String token,
                               String device, String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
        this.emailAccount = emailAccount;
        this.skip = skip;
    }

    public void setGetRecentEmailsTaskListener(GetRecentEmailsTaskListener getRecentEmailsTaskListener) {
        this.getRecentEmailsTaskListener = getRecentEmailsTaskListener;
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
            request.put(MainApplicationSingleton.SKIP_PARAM, skip);
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
            getRecentEmailsTaskListener.onPostGetRecentEmailsErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        getRecentEmailsTaskListener.onPostGetRecentEmailsErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getRecentEmailsTaskListener.onPostGetRecentEmailsErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getRecentEmailsTaskListener.onPostGetRecentEmailsResponse(response, emailAccount, skip, getUserItem());
        releaseContext();
    }

    public interface GetRecentEmailsTaskListener {
        void onPostGetRecentEmailsResponse(JSONObject response, String email, int skip, ContactItem user);

        void onPostGetRecentEmailsErrorResponse(JSONObject response);

    }

}
