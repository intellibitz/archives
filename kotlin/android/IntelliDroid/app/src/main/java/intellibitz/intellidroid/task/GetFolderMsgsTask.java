package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class GetFolderMsgsTask extends AsyncTask<Void, Void, Boolean> {
    private GetFolderMsgsTaskListener getFolderMsgsTaskListener;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GetFolderMsgsTask(MessageItem messageItem,
                             String uid, String token, String device, String deviceRef, String url) {
        this.messageItem = messageItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGetFolderMsgsTaskListener(GetFolderMsgsTaskListener deleteFolderTaskListener) {
        this.getFolderMsgsTaskListener = deleteFolderTaskListener;
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
        getFolderMsgsTaskListener.setGetFolderMsgsFromCloudTaskToNull();
        if (success) {
            getFolderMsgsTaskListener.onPostGetFolderMsgsFromCloudExecute(response, messageItem);
        } else {
            getFolderMsgsTaskListener.onPostGetFolderMsgsFromCloudExecuteFail(response, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        getFolderMsgsTaskListener.setGetFolderMsgsFromCloudTaskToNull();
    }

    public interface GetFolderMsgsTaskListener {
        void onPostGetFolderMsgsFromCloudExecute(JSONObject response, MessageItem messageItem);

        void onPostGetFolderMsgsFromCloudExecuteFail(JSONObject response, MessageItem messageItem);

        void setGetFolderMsgsFromCloudTaskToNull();
    }

}
