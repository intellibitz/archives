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
public class AckDocTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "AckDocTask";
    private AckDocTaskListener ackDocTaskListener;
    private String docId;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public AckDocTask(String docId, String uid, String token,
                      String device, String deviceRef, String url) {
        this.docId = docId;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.deviceRef = deviceRef;
        this.url = url;
    }

    public void setAckDocTaskListener(AckDocTaskListener groupInfoTaskListener) {
        this.ackDocTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.DOC_ID, docId);

        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);

        if (null == response) {
            return false;
        }
        // TODO: register the new account here.
//            investigate response and take action
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        ackDocTaskListener.setAckDocTaskToNull();
        if (success) {
            ackDocTaskListener.onPostAckDocExecute(response);
        } else {
            ackDocTaskListener.onPostAckDocExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        ackDocTaskListener.setAckDocTaskToNull();
    }

    public interface AckDocTaskListener {
        void onPostAckDocExecute(JSONObject response);

        void onPostAckDocExecuteFail(JSONObject response);

        void setAckDocTaskToNull();
    }

}
