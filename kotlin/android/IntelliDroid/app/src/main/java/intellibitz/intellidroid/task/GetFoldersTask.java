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
public class GetFoldersTask extends AsyncTask<Void, Void, Boolean> {
    private GetFoldersTaskListener getFoldersTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GetFoldersTask(String uid, String token, String device, String deviceRef, String url) {
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGetFoldersTaskListener(GetFoldersTaskListener getFoldersTaskListener) {
        this.getFoldersTaskListener = getFoldersTaskListener;
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
        getFoldersTaskListener.setFoldersGetFromCloudTaskToNull();
        if (success) {
            getFoldersTaskListener.onPostFoldersGetFromCloudExecute(response);
        } else {
//                ERROR
            getFoldersTaskListener.onPostFoldersGetFromCloudExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        getFoldersTaskListener.setFoldersGetFromCloudTaskToNull();
    }

    public interface GetFoldersTaskListener {
        void onPostFoldersGetFromCloudExecute(JSONObject response);

        void onPostFoldersGetFromCloudExecuteFail(JSONObject response);

        void setFoldersGetFromCloudTaskToNull();
    }

}
