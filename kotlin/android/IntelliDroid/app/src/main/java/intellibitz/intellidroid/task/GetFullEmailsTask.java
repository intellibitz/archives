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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 */
public class GetFullEmailsTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GetFullEmailsTask";

    private GetFullEmailsTaskListener getFullEmailsTaskListener;
    private String emailAccount;
    private String emailUids;
    private String[] emailUidsArray;
    private int mode = -1;

    public GetFullEmailsTask(String emailAccount, String[] emailUids,
                             String uid, String token, String device, String deviceRef,
                             ContactItem user, String url, Context context, int mode) {
        super(uid, token, device, deviceRef, url, user, context);
        this.emailAccount = emailAccount;
        this.emailUidsArray = emailUids;
        JSONArray jsonArray = new JSONArray();
        if (emailUids != null && emailUids.length > 0) {
            for (String id : emailUids) {
                try {
                    int value = Integer.parseInt(id);
                    jsonArray.put(value);
                } catch (Throwable ignored) {
                    Log.e(TAG, "GetFullEmailsTask: email ids to retrieve is not an int - skipping " + id);
                }
            }
//            this.emailUids = Arrays.toString(emailUids);
            this.emailUids = jsonArray.toString();
        }
        this.mode = mode;
    }

    public void setGetFullEmailsTaskListener(GetFullEmailsTaskListener getFullEmailsTaskListener) {
        this.getFullEmailsTaskListener = getFullEmailsTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (TextUtils.isEmpty(emailAccount) || TextUtils.isEmpty(emailUids) || !prepareRequest()) {
            Log.e(TAG, "email id param is NULL - fail");
            backgroundResult = false;
        } else {
            try {
                request.put(MainApplicationSingleton.EMAIL_ACCOUNT_PARAM, emailAccount.trim());
                request.put(MainApplicationSingleton.EMAIL_UIDS_PARAM, emailUids);
                backgroundResult = postJsonObjectRequest();
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, TAG + e.toString());
                try {
                    response.put("error", e.toString());
                } catch (Throwable e1) {
                    e1.printStackTrace();
                    Log.e(TAG, TAG + e1.toString());
                }
                backgroundResult = false;
            }
        }
        return backgroundResult;
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            getFullEmailsTaskListener.onPostGetFullEmailsErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        getFullEmailsTaskListener.onPostGetFullEmailsErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getFullEmailsTaskListener.onPostGetFullEmailsErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getFullEmailsTaskListener.onPostGetFullEmailsResponse(response, emailUidsArray, emailAccount,
                getUserItem(), mode);
        releaseContext();
    }

    public interface GetFullEmailsTaskListener {
        void onPostGetFullEmailsResponse(JSONObject response, String[] emailUidsArray, String emailAccount, ContactItem userItem, int mode);

        void onPostGetFullEmailsErrorResponse(JSONObject response);
    }

}
