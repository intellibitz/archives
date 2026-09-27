package intellibitz.intellidroid.task;

import android.os.AsyncTask;

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
public class UpdateProfileTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "UpdateProfileTask";
    private UpdateProfileTaskListener updateProfileTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;
    private String name;
    private String status;

    public UpdateProfileTask(String uid, String token, String device, String deviceRef,
                             String name, String status, String url) {
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
        this.name = name;
        this.status = status;
    }

    public void setUpdateProfileTaskListener(UpdateProfileTaskListener groupInfoTaskListener) {
        this.updateProfileTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == name) return false;
//        sends read receipt to the cloud
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.NAME_PARAM, name);
        if (status != null)
            data.put(MainApplicationSingleton.STATUS_PARAM, status);

        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        updateProfileTaskListener.setUpdateProfileTaskToNull();
        updateProfileTaskListener.onPostUpdateProfileExecute(response);
    }

    @Override
    protected void onCancelled() {
        updateProfileTaskListener.setUpdateProfileTaskToNull();
        updateProfileTaskListener.onPostUpdateProfileExecuteFail(response);
    }

    public interface UpdateProfileTaskListener {
        void onPostUpdateProfileExecute(JSONObject response);

        void onPostUpdateProfileExecuteFail(JSONObject response);

        void setUpdateProfileTaskToNull();
    }

}
