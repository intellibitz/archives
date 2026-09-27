package intellibitz.intellidroid.account;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import org.json.JSONObject;

/**
 */
public class GetEmailVerificationTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GetEmailVerTask";

    private GetEmailVerificationTaskListener getEmailVerificationTaskListener;
    private String email;

    public GetEmailVerificationTask(String email, String url, Context context) {
        super(url, context);
        this.email = email;
    }

    public void setGetEmailVerificationTaskListener(GetEmailVerificationTaskListener GetEmailVerificationTaskListener) {
        this.getEmailVerificationTaskListener = GetEmailVerificationTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "Email param is NULL - fail");
            return false;
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_PARAM, email.trim());
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
/*
{
  "email": "jeff@intellibitz.com"
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            getEmailVerificationTaskListener.onPostGetEmailVerificationErrorResponse(response);
        }
/*
{
  "status": 0,
  "err": "string"
}
         */
    }

    @Override
    protected void onCancelled() {
        getEmailVerificationTaskListener.onPostGetEmailVerificationErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getEmailVerificationTaskListener.onPostGetEmailVerificationErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getEmailVerificationTaskListener.onPostGetEmailVerificationResponse(response, email, getUserItem());
        releaseContext();
    }

    public interface GetEmailVerificationTaskListener {
        void onPostGetEmailVerificationResponse(JSONObject response, String email, ContactItem user);

        void onPostGetEmailVerificationErrorResponse(JSONObject response);

    }

}
