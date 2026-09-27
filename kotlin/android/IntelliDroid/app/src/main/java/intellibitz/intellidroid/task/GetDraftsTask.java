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
public class GetDraftsTask extends AsyncTask<Void, Void, Boolean> {
    private GetDraftsTaskListener getDraftsTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GetDraftsTask(String uid, String token, String device, String deviceRef, String url) {
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGetDraftsTaskListener(GetDraftsTaskListener getDraftsTaskListener) {
        this.getDraftsTaskListener = getDraftsTaskListener;
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
        getDraftsTaskListener.setDraftsGetFromCloudTaskToNull();
        if (success) {
            getDraftsTaskListener.onPostDraftsGetFromCloudExecute(response);
        } else {
//                ERROR
            getDraftsTaskListener.onPostDraftsGetFromCloudExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        getDraftsTaskListener.setDraftsGetFromCloudTaskToNull();
    }

    public interface GetDraftsTaskListener {
        void onPostDraftsGetFromCloudExecute(JSONObject response);

        void onPostDraftsGetFromCloudExecuteFail(JSONObject response);

        void setDraftsGetFromCloudTaskToNull();
    }

}
