package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class AddEmailTask extends AsyncTask<Void, Void, Boolean> {
    private final String email;
    private final String password;
    private AddEmailTask.AddEmailTaskListener addEmailTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public AddEmailTask(String email, String password,
                        String uid, String token, String device, String deviceRef, String url) {
        this.email = email;
        this.password = password;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.deviceRef = deviceRef;
        this.url = url;
    }

    public void setAddEmailTaskListener(AddEmailTask.AddEmailTaskListener addEmailTaskListener) {
        this.addEmailTaskListener = addEmailTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.

        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.EMAIL_PARAM, email);
        data.put(MainApplicationSingleton.PWD_PARAM, password);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        addEmailTaskListener.setAddEmailTaskToNull();
        if (success) {
            addEmailTaskListener.onPostAddEmailTaskExecute(response);
        } else {
            addEmailTaskListener.onPostAddEmailTaskExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        addEmailTaskListener.setAddEmailTaskToNull();
    }

    public interface AddEmailTaskListener {
        void onPostAddEmailTaskExecute(JSONObject response);

        void onPostAddEmailTaskExecuteFail(JSONObject response);

        void setAddEmailTaskToNull();
    }
}
