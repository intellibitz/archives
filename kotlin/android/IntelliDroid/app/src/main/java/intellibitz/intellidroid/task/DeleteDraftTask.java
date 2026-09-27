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
public class DeleteDraftTask extends AsyncTask<Void, Void, Boolean> {
    private DeleteDraftTaskListener deleteDraftTaskListener;
    private MessageItem messageItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public DeleteDraftTask(MessageItem messageItem,
                           String uid, String token, String device, String deviceRef, String url) {
        this.messageItem = messageItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setDeleteDraftTaskListener(DeleteDraftTaskListener deleteDraftTaskListener) {
        this.deleteDraftTaskListener = deleteDraftTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        final String id = messageItem.getDataId();
        if (TextUtils.isEmpty(id)) return false;

        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put(MainApplicationSingleton.DRAFT_ID_PARAM, id);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        deleteDraftTaskListener.setDeleteDraftFromCloudTaskToNull();
        if (success) {
            deleteDraftTaskListener.onPostDeleteDraftFromCloudExecute(response, messageItem);
        } else {
            deleteDraftTaskListener.onPostDeleteDraftFromCloudExecuteFail(response, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        deleteDraftTaskListener.setDeleteDraftFromCloudTaskToNull();
    }

    public interface DeleteDraftTaskListener {
        void onPostDeleteDraftFromCloudExecute(JSONObject response, MessageItem messageItem);

        void onPostDeleteDraftFromCloudExecuteFail(JSONObject response, MessageItem messageItem);

        void setDeleteDraftFromCloudTaskToNull();
    }

}
