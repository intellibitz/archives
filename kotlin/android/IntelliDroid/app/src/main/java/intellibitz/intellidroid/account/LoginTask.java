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
public class LoginTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "LoginTask";

    private LoginTaskListener loginTaskListener;
    private String email;
    private String pwd;
    private String deviceId;
    private String deviceName;

    public LoginTask(String email, String pwd, String device, String deviceId, String deviceName,
                     String url, ContactItem user, Context context) {
        super(device, url, user, context);
        this.email = email;
        this.pwd = pwd;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    public void setLoginTaskListener(LoginTaskListener LoginTaskListener) {
        this.loginTaskListener = LoginTaskListener;
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
        if (TextUtils.isEmpty(pwd)) {
            Log.e(TAG, "Password param is NULL - fail");
            return false;
        }
        if (TextUtils.isEmpty(device)) {
            Log.e(TAG, "Device param is NULL - fail");
            return false;
        }
        if (TextUtils.isEmpty(deviceName)) {
            Log.e(TAG, "Device name is NULL - fail");
            return false;
        }
        try {
            request.put(MainApplicationSingleton.EMAIL_PARAM, email.trim());
            request.put(MainApplicationSingleton.PWD_PARAM, pwd);
            request.put(MainApplicationSingleton.DEVICE_ID_PARAM, deviceId);
            request.put(MainApplicationSingleton.DEVICE_NAME_PARAM, deviceName);
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
  "pwd": "account-password",
  "device": "ios",
  "device_id": "UAID-NAD2-4545-1212",
  "device_name": "iPad"
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            loginTaskListener.onPostLoginErrorResponse(response);
        }
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string",
  "existing_devices": "string",
  "existing_websessions": "string"
}
         */
    }

    @Override
    protected void onCancelled() {
        loginTaskListener.onPostLoginErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        loginTaskListener.onPostLoginErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        loginTaskListener.onPostLoginResponse(response, email, getUserItem());
        releaseContext();
    }

    public interface LoginTaskListener {
        void onPostLoginResponse(JSONObject response, String email, ContactItem user);

        void onPostLoginErrorResponse(JSONObject response);

    }

}
