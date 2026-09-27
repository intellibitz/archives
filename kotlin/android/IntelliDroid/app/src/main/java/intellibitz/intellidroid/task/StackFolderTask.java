package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class StackFolderTask extends AsyncTask<Void, Void, Boolean> {
    private StackFolderTaskListener stackFolderTaskListener;
    private String name;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public StackFolderTask(String name, MessageItem messageItem,
                           String uid, String token, String device, String deviceRef, String url) {
        this.name = name;
        this.messageItem = messageItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setStackFolderTaskListener(StackFolderTaskListener createFolderTaskListener) {
        this.stackFolderTaskListener = createFolderTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.CODE_PARAM, messageItem.getFolderCode());
        data.put(MainApplicationSingleton.STACK_NAME_PARAM, name);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        stackFolderTaskListener.setStackFolderFromCloudTaskToNull();
        if (success) {
            stackFolderTaskListener.onPostStackFolderFromCloudExecute(response, name, messageItem);
        } else {
            stackFolderTaskListener.onPostStackFolderFromCloudExecuteFail(response, name, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        stackFolderTaskListener.setStackFolderFromCloudTaskToNull();
    }

    public interface StackFolderTaskListener {
        void onPostStackFolderFromCloudExecute(JSONObject response, String name, MessageItem messageItem);

        void onPostStackFolderFromCloudExecuteFail(JSONObject response, String name, MessageItem messageItem);

        void setStackFolderFromCloudTaskToNull();
    }

}
