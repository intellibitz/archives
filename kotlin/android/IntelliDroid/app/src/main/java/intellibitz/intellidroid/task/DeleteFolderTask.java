package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class DeleteFolderTask extends AsyncTask<Void, Void, Boolean> {
    private DeleteFolderTaskListener deleteFolderTaskListener;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public DeleteFolderTask(MessageItem messageItem,
                            String uid, String token, String device, String deviceRef, String url) {
        this.messageItem = messageItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setDeleteFolderTaskListener(DeleteFolderTaskListener deleteFolderTaskListener) {
        this.deleteFolderTaskListener = deleteFolderTaskListener;
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
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        deleteFolderTaskListener.setDeleteFolderFromCloudTaskToNull();
        if (success) {
            deleteFolderTaskListener.onPostDeleteFolderFromCloudExecute(response, messageItem);
        } else {
            deleteFolderTaskListener.onPostDeleteFolderFromCloudExecuteFail(response, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        deleteFolderTaskListener.setDeleteFolderFromCloudTaskToNull();
    }

    public interface DeleteFolderTaskListener {
        void onPostDeleteFolderFromCloudExecute(JSONObject response, MessageItem messageItem);

        void onPostDeleteFolderFromCloudExecuteFail(JSONObject response, MessageItem messageItem);

        void setDeleteFolderFromCloudTaskToNull();
    }

}
