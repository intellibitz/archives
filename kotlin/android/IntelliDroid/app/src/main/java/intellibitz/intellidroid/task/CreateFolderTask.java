package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by muthuselvam on 24-06-2016.
 */
public class CreateFolderTask extends AsyncTask<Void, Void, Boolean> {
    private CreateFolderTaskListener createFolderTaskListener;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public CreateFolderTask(MessageItem messageItem,
                            String uid, String token, String device, String deviceRef, String url) {
        this.messageItem = messageItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setCreateFolderTaskListener(CreateFolderTaskListener createFolderTaskListener) {
        this.createFolderTaskListener = createFolderTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.NAME_PARAM, messageItem.getName());
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        createFolderTaskListener.setCreateFolderFromCloudTaskToNull();
        if (success) {
            createFolderTaskListener.onPostCreateFolderFromCloudExecute(response, messageItem);
        } else {
            createFolderTaskListener.onPostCreateFolderFromCloudExecuteFail(response, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        createFolderTaskListener.setCreateFolderFromCloudTaskToNull();
    }

    public interface CreateFolderTaskListener {
        void onPostCreateFolderFromCloudExecute(JSONObject response, MessageItem nestItem);

        void onPostCreateFolderFromCloudExecuteFail(JSONObject response, MessageItem nestItem);

        void setCreateFolderFromCloudTaskToNull();
    }

}
