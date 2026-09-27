package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class UpdateFolderTask extends AsyncTask<Void, Void, Boolean> {
    private UpdateFolderTaskListener updateFolderTaskListener;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public UpdateFolderTask(MessageItem messageItem,
                            String uid, String token, String device, String deviceRef, String url) {
        this.messageItem = messageItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setUpdateFolderTaskListener(UpdateFolderTaskListener updateFolderTaskListener) {
        this.updateFolderTaskListener = updateFolderTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        final String folderCode = messageItem.getFolderCode();
        if (TextUtils.isEmpty(folderCode)) return false;
        final String name = messageItem.getName();
        if (TextUtils.isEmpty(name)) return false;

        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.CODE_PARAM, folderCode);
        data.put(MainApplicationSingleton.NAME_PARAM, name);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        updateFolderTaskListener.setUpdateFolderFromCloudTaskToNull();
        if (success) {
            updateFolderTaskListener.onPostUpdateFolderFromCloudExecute(response, messageItem);
        } else {
            updateFolderTaskListener.onPostUpdateFolderFromCloudExecuteFail(response, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        updateFolderTaskListener.setUpdateFolderFromCloudTaskToNull();
    }

    public interface UpdateFolderTaskListener {
        void onPostUpdateFolderFromCloudExecute(JSONObject response, MessageItem messageItem);

        void onPostUpdateFolderFromCloudExecuteFail(JSONObject response, MessageItem messageItem);

        void setUpdateFolderFromCloudTaskToNull();
    }

}
