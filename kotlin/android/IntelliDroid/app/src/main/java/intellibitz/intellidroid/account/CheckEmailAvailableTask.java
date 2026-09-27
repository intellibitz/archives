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
public class CheckEmailAvailableTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "ChkEmailAvblTask";

    private CheckEmailAvailableTaskListener checkEmailAvailableTaskListener;
    private String email;

    public CheckEmailAvailableTask(String email, String url, Context context) {
        super(url, context);
        this.email = email;
    }

    public void setCheckEmailAvailableTaskListener(CheckEmailAvailableTaskListener CheckEmailAvailableTaskListener) {
        this.checkEmailAvailableTaskListener = CheckEmailAvailableTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "Email is NULL - fail");
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
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            checkEmailAvailableTaskListener.onPostCheckEmailAvailableErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        checkEmailAvailableTaskListener.onPostCheckEmailAvailableErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        checkEmailAvailableTaskListener.onPostCheckEmailAvailableErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        checkEmailAvailableTaskListener.onPostCheckEmailAvailableResponse(response, email, getUserItem());
        releaseContext();
    }

    public interface CheckEmailAvailableTaskListener {
        void onPostCheckEmailAvailableResponse(JSONObject response, String email, ContactItem user);

        void onPostCheckEmailAvailableErrorResponse(JSONObject response);

    }

}
