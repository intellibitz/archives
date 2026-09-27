package intellibitz.intellidroid.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class MarkReadTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "MarkReadTask";
    private MarkReadTaskListener markReadTaskListener;
    private Context context;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public MarkReadTask(Context context, MessageItem item, String uid, String token,
                        String device, String deviceRef, String url) {
        this.context = context;
        this.messageItem = item;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setMarkReadTaskListener(MarkReadTaskListener groupInfoTaskListener) {
        this.markReadTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
//        saves unread to 0 in DB
        if (null == messageItem || null == messageItem.getChatId()) return false;
        if (messageItem.getUnreadCount() > 0) {
            try {
                messageItem.setUnreadCount(0);
                MessageChatContentProvider.updateMessageChatThreadReadItems(messageItem, context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        sends read receipt to the cloud
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.CHAT_ID_PARAM, messageItem.getChatId());

/*
        String[] ids = messageItem.getUnreadMessageIds();
        if (null == ids) return false;
        JSONArray array = new JSONArray();
        for (String id : ids) {
            array.put(id);
        }
        data.put(MainApplicationSingleton.MSGS_PARAM, array.toString());
*/
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        markReadTaskListener.setMarkReadTaskToNull();
        if (success) {
            markReadTaskListener.onPostMarkReadExecute(response, messageItem);
        } else {
            Log.e(TAG, ":error - " + response);
            markReadTaskListener.onPostMarkReadExecuteFail(response, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        markReadTaskListener.setMarkReadTaskToNull();
    }

    public interface MarkReadTaskListener {
        void onPostMarkReadExecute(JSONObject response, MessageItem item);

        void onPostMarkReadExecuteFail(JSONObject response, MessageItem item);

        void setMarkReadTaskToNull();
    }

}
