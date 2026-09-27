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
public class VerifyCodeTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "VerifyCodeTask";

    private VerifyCodeTaskListener verifyCodeTaskListener;
    private String email;
    private String code;

    public VerifyCodeTask(String email, String code, String url, Context context) {
        super(url, context);
        this.email = email;
        this.code = code;
    }

    public void setVerifyCodeTaskListener(VerifyCodeTaskListener VerifyCodeTaskListener) {
        this.verifyCodeTaskListener = VerifyCodeTaskListener;
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
        if (TextUtils.isEmpty(code)) {
            Log.e(TAG, "Code param is NULL - fail");
            return false;
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_PARAM, email.trim());
            request.put(MainApplicationSingleton.CODE_PARAM, code);
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
  "email": "jeff@intellibitz.com",
  "code": 1234
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            verifyCodeTaskListener.onPostVerifyCodeErrorResponse(response);
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
        verifyCodeTaskListener.onPostVerifyCodeErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        verifyCodeTaskListener.onPostVerifyCodeErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        verifyCodeTaskListener.onPostVerifyCodeResponse(response, email, getUserItem());
        releaseContext();
    }

    public interface VerifyCodeTaskListener {
        void onPostVerifyCodeResponse(JSONObject response, String email, ContactItem user);

        void onPostVerifyCodeErrorResponse(JSONObject response);

    }

}
