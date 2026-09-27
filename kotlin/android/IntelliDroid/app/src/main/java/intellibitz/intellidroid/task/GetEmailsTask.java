package intellibitz.intellidroid.task;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;

import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import org.json.JSONObject;

/**
 */
public class GetEmailsTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GetEmailsTask";

    private int mode = -1;
    private GetEmailsTaskListener getEmailsTaskListener;

    public GetEmailsTask(String uid, String token, String device, String deviceRef,
                         String url, ContactItem user, Context context, int mode) {
        super(uid, token, device, deviceRef, url, user, context);
        this.mode = mode;
    }

    public void setGetEmailsTaskListener(GetEmailsTaskListener getEmailsTaskListener) {
        this.getEmailsTaskListener = getEmailsTaskListener;
    }

    public void setRequestTimeoutMillis(int requestTimeoutMillis) {
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        try {
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
        if (this.success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            getEmailsTaskListener.onPostGetEmailsErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        getEmailsTaskListener.onPostGetEmailsErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        if (null == response) getEmailsTaskListener.onPostGetEmailsErrorResponse(null);
        getEmailsTaskListener.onPostGetEmailsResponse(response, getUserItem(), mode);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        try {
            response = new JSONObject();
            response.put("error", error.toString());
            getEmailsTaskListener.onPostGetEmailsErrorResponse(response);
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                response = new JSONObject();
                response.put("error", TAG + e.getLocalizedMessage());
                getEmailsTaskListener.onPostGetEmailsErrorResponse(response);
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
        releaseContext();
    }

    public interface GetEmailsTaskListener {
        void onPostGetEmailsResponse(JSONObject response, ContactItem userItem, int mode);

        void onPostGetEmailsErrorResponse(JSONObject response);

    }

}
