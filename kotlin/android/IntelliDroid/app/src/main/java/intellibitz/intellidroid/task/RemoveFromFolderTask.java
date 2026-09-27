package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;

/**
 */
public class RemoveFromFolderTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "RemoveFromFolderTask";
    private RemoveFromFolderTaskListener removeFromFolderTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private String code;
    private Collection<MessageItem> messageItems;
    private JSONObject response;

    public RemoveFromFolderTask(Collection<MessageItem> messageItems, String code, String uid, String token,
                                String device, String deviceRef, String url) {
        this.messageItems = messageItems;
        this.code = code;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setRemoveFromFolderTaskListener(RemoveFromFolderTaskListener groupInfoTaskListener) {
        this.removeFromFolderTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == messageItems || messageItems.isEmpty()) return false;
//        sends read receipt to the cloud
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.CODE_PARAM, code);

        JSONArray array = new JSONArray();
        for (MessageItem messageItem : messageItems) {
            array.put(messageItem.getDataId());
        }
        data.put(MainApplicationSingleton.MSGS_PARAM, array.toString());
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        removeFromFolderTaskListener.setRemoveFromFolderTaskToNull();
        if (success) {
            removeFromFolderTaskListener.onPostRemoveFromFolderTaskExecute(response, messageItems);
        } else {
            Log.e(TAG, ":error - " + response);
            removeFromFolderTaskListener.onPostRemoveFromFolderTaskExecuteFail(response, messageItems);
        }
    }

    @Override
    protected void onCancelled() {
        removeFromFolderTaskListener.setRemoveFromFolderTaskToNull();
    }

    public interface RemoveFromFolderTaskListener {
        void onPostRemoveFromFolderTaskExecute(JSONObject response, Collection<MessageItem> item);

        void onPostRemoveFromFolderTaskExecuteFail(JSONObject response, Collection<MessageItem> item);

        void setRemoveFromFolderTaskToNull();
    }

}
