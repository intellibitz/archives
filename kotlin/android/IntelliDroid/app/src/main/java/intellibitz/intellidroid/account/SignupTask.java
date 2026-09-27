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
public class SignupTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "SignupTask";

    private SignupTaskListener signupTaskListener;
    private String email;
    private String code;
    private String name;
    private String pwd;
    private String deviceId;
    private String deviceName;
    private String status;

    public SignupTask(String email, String code, String name, String pwd, String device, String deviceId,
                      String deviceName, String status, String url, ContactItem user, Context context) {
        super(device, url, user, context);
        this.email = email;
        this.code = code;
        this.name = name;
        this.pwd = pwd;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.status = status;
    }

    public void setSignupTaskListener(SignupTaskListener SignupTaskListener) {
        this.signupTaskListener = SignupTaskListener;
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
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "Name param is NULL - fail");
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
            request.put(MainApplicationSingleton.CODE_PARAM, code);
            request.put(MainApplicationSingleton.NAME_PARAM, name);
            request.put(MainApplicationSingleton.PWD_PARAM, pwd);
            request.put(MainApplicationSingleton.DEVICE_ID_PARAM, deviceId);
            request.put(MainApplicationSingleton.DEVICE_NAME_PARAM, deviceName);
            request.put(MainApplicationSingleton.STATUS_PARAM, status);
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
  "code": 1234,
  "name": "Jeffrey Roshan",
  "pwd": "some-password",
  "device": "ios",
  "device_id": "UAID-NAD2-22K8-8AW3",
  "device_name": "iPhone 6S",
  "status": "Available"
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            signupTaskListener.onPostSignupErrorResponse(response);
        }
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string"
}*/
    }

    @Override
    protected void onCancelled() {
        signupTaskListener.onPostSignupErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        signupTaskListener.onPostSignupErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        signupTaskListener.onPostSignupResponse(response, email, getUserItem());
        releaseContext();
    }

    public interface SignupTaskListener {
        void onPostSignupResponse(JSONObject response, String email, ContactItem user);

        void onPostSignupErrorResponse(JSONObject response);

    }

}
