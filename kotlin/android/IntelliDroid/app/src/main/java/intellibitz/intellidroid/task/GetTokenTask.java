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
public class GetTokenTask extends AsyncTask<Void, Void, Boolean> {
    private GetTokenTask.GetTokenTaskListener getTokenTaskListener;
    private String email;
    private String emailCode;
    private String uid;
    private String token;
    private String device;
    private String deviceRef;
    private String URL;
    private JSONObject response;

    public GetTokenTask(String email, String emailCode,
                        String uid, String token, String device, String deviceRef, String url) {
        this.email = email;
        this.emailCode = emailCode;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.deviceRef = deviceRef;
        this.URL = url;
    }

    public void setGetTokenTaskListener(GetTokenTask.GetTokenTaskListener getTokenTaskListener) {
        this.getTokenTaskListener = getTokenTaskListener;
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
        data.put(MainApplicationSingleton.CODE_PARAM, emailCode);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(this.URL, data);

        if (null == response) {
            return false;
        }
        // TODO: register the new account here.
//            investigate response and take action
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        getTokenTaskListener.setGetTokenTaskToNull();
        if (success) {
            getTokenTaskListener.onPostGetTokenTaskExecute(response);
        } else {
            getTokenTaskListener.onPostGetTokenTaskExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        getTokenTaskListener.setGetTokenTaskToNull();
//        showProgress(false);
    }

    public interface GetTokenTaskListener {
        void onPostGetTokenTaskExecute(JSONObject response);

        void onPostGetTokenTaskExecuteFail(JSONObject response);

        void setGetTokenTaskToNull();
    }
}
