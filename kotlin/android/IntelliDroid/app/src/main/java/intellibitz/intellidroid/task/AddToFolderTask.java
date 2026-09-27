package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class AddToFolderTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "AddToFolderTask";
    private AddToFolderTaskListener addToFolderTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private String code;
    private Collection<MessageItem> messageItems;
    private MessageItem messageItem;
    private JSONObject response;

    public AddToFolderTask(Collection<MessageItem> messageItems, String code, String uid, String token,
                           String device, String deviceRef, String url) {
        this.messageItems = messageItems;
        this.code = code;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public AddToFolderTask(Collection<MessageItem> messageItems, MessageItem nest, String uid, String token,
                           String device, String deviceRef, String url) {
        this.messageItems = messageItems;
        this.messageItem = nest;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setAddToFolderTaskListener(AddToFolderTaskListener groupInfoTaskListener) {
        this.addToFolderTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == messageItems || messageItems.isEmpty()) return false;
        if (code == null && messageItem == null) return false;
        if (TextUtils.isEmpty(code)) {
            code = messageItem.getFolderCode();
        }
        if (TextUtils.isEmpty(code)) return false;
        HashMap<String, String> data = new HashMap<>();

        JSONArray array = new JSONArray();
        for (MessageItem messageItem : messageItems) {
            final String dataId = messageItem.getDataId();
            if (!TextUtils.isEmpty(dataId))
                array.put(dataId);
        }

        if (0 == array.length()) return false;

        data.put(MainApplicationSingleton.MSGS_PARAM, array.toString());
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.CODE_PARAM, code);

        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        addToFolderTaskListener.setAddToFolderTaskToNull();
        if (success) {
            addToFolderTaskListener.onPostAddToFolderTaskExecute(response, messageItems, messageItem);
        } else {
            addToFolderTaskListener.onPostAddToFolderTaskExecuteFail(response, messageItems, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        addToFolderTaskListener.setAddToFolderTaskToNull();
    }

    public interface AddToFolderTaskListener {
        void onPostAddToFolderTaskExecute(JSONObject response, Collection<MessageItem> item, MessageItem messageItem);

        void onPostAddToFolderTaskExecuteFail(JSONObject response, Collection<MessageItem> item, MessageItem messageItem);

        void setAddToFolderTaskToNull();
    }

}
