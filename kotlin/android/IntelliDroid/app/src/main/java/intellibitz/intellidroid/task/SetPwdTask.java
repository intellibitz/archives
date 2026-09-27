package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class SetPwdTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "SetPwdTask";
    private SetPwdTaskListener setPwdTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;
    private String newpwd;

    public SetPwdTask(String uid, String token, String device, String deviceRef,
                      String newpwd, String url) {
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.deviceRef = deviceRef;
        this.newpwd = newpwd;
        this.url = url;
    }

    public void setSetPwdTaskListener(SetPwdTaskListener setPwdTaskListener) {
        this.setPwdTaskListener = setPwdTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (TextUtils.isEmpty(newpwd)) return false;
//        sends read receipt to the cloud
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.NEWPWD_PARAM, newpwd);
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        setPwdTaskListener.setSetPwdTaskToNull();
        if (success)
            setPwdTaskListener.onPostSetPwdExecute(response, newpwd);
        else
            setPwdTaskListener.onPostSetPwdExecuteFail(response, newpwd);
    }

    @Override
    protected void onCancelled() {
        setPwdTaskListener.setSetPwdTaskToNull();
        setPwdTaskListener.onPostSetPwdExecuteFail(response, newpwd);
    }

    public interface SetPwdTaskListener {
        void onPostSetPwdExecute(JSONObject response, String newpwd);

        void onPostSetPwdExecuteFail(JSONObject response, String newpwd);

        void setSetPwdTaskToNull();
    }

}
