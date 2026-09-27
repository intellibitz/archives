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
public class RcvDocTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "RcvDocTask";
    private RcvDocTaskListener rcvDocTaskListener;
    private String docId;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public RcvDocTask(String docId, String uid, String token,
                      String device, String deviceRef, String url) {
        this.docId = docId;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.deviceRef = deviceRef;
        this.url = url;
    }

    public void setRcvDocTaskListener(RcvDocTaskListener groupInfoTaskListener) {
        this.rcvDocTaskListener = groupInfoTaskListener;
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
        rcvDocTaskListener.setRcvDocTaskToNull();
        if (success) {
            rcvDocTaskListener.onPostRcvDocExecute(response);
        } else {
            rcvDocTaskListener.onPostRcvDocExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        rcvDocTaskListener.setRcvDocTaskToNull();
    }

    public interface RcvDocTaskListener {
        void onPostRcvDocExecute(JSONObject response);

        void onPostRcvDocExecuteFail(JSONObject response);

        void setRcvDocTaskToNull();
    }

}
