package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.content.MessageChatContentProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class UpdateDraftTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "UpdateDraftTask";
    private UpdateDraftTaskListener updateDraftTaskListener;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public UpdateDraftTask(MessageItem messageItem,
                           String uid, String token, String device, String deviceRef, String url) {
        this.messageItem = messageItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setUpdateDraftTaskListener(UpdateDraftTaskListener updateDraftTaskListener) {
        this.updateDraftTaskListener = updateDraftTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (null == messageItem) return false;
            final String id = messageItem.getDataId();
            if (TextUtils.isEmpty(id)) return false;
            JSONObject jsonObject = MessageChatContentProvider.toJson(messageItem,
                    uid, token, device, deviceRef);
            if (null == jsonObject) return false;

            // TODO: attempt authentication against a network service.
            HashMap<String, String> data = new HashMap<>();
            data.put(MainApplicationSingleton.DEVICE_PARAM, device);
            data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            data.put(MainApplicationSingleton.UID_PARAM, uid);
            data.put(MainApplicationSingleton.TOKEN_PARAM, token);

            data.put(MainApplicationSingleton.DRAFT_ID_PARAM, id);
            data.put(MainApplicationSingleton.DRAFT_OBJ_PARAM, jsonObject.toString());
            // params comes from the execute() call: params[0] is the url.
            response = HttpUrlConnectionParser.postHTTP(url, data);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        updateDraftTaskListener.setUpdateDraftFromCloudTaskToNull();
        if (success) {
            updateDraftTaskListener.onPostUpdateDraftFromCloudExecute(response, messageItem);
        } else {
            updateDraftTaskListener.onPostUpdateDraftFromCloudExecuteFail(response, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        updateDraftTaskListener.setUpdateDraftFromCloudTaskToNull();
    }

    public interface UpdateDraftTaskListener {
        void onPostUpdateDraftFromCloudExecute(JSONObject response, MessageItem messageItem);

        void onPostUpdateDraftFromCloudExecuteFail(JSONObject response, MessageItem messageItem);

        void setUpdateDraftFromCloudTaskToNull();
    }

}
