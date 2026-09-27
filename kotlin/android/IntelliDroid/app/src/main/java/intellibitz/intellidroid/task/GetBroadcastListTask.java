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
public class GetBroadcastListTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "GetBroadcastTask";
    private GetBroadcastTaskListener getBroadcastTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GetBroadcastListTask(String uid, String token, String device, String deviceRef, String url) {
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGetBroadcastTaskListener(GetBroadcastTaskListener getBroadcastTaskListener) {
        this.getBroadcastTaskListener = getBroadcastTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        getBroadcastTaskListener.setGetBroadcastTaskToNull();
        if (success) {
            getBroadcastTaskListener.onPostGetBroadcastTaskExecute(response);
        } else {
            getBroadcastTaskListener.onPostGetBroadcastTaskExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        getBroadcastTaskListener.setGetBroadcastTaskToNull();
    }

    public interface GetBroadcastTaskListener {
        void onPostGetBroadcastTaskExecute(JSONObject response);

        void onPostGetBroadcastTaskExecuteFail(JSONObject response);

        void setGetBroadcastTaskToNull();
    }

}
