package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class FlagMsgsTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "FlagMsgsTask";
    private FlagMsgsTaskListener flagMsgsTaskListener;
    private String[] ids;
    private MessageItem[] mids;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public FlagMsgsTask(String[] ids, String uid, String token,
                        String device, String deviceRef, String url) {
        this.ids = ids;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public FlagMsgsTask(MessageItem[] mids, String uid, String token,
                        String device, String deviceRef, String url) {
        this.mids = mids;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setFlagMsgsTaskListener(FlagMsgsTaskListener groupInfoTaskListener) {
        this.flagMsgsTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
//        sends read receipt to the cloud
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);

        if (null == ids && null == mids) return false;
        JSONArray array = new JSONArray();
        if (null == mids) {
            for (String id : ids) {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("msg_id", id);
                    msg.put("note", id);
                    array.put(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (MessageItem id : mids) {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("msg_id", id.getDataId());
                    msg.put("note", id.getMsgRef());
                    array.put(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        data.put(MainApplicationSingleton.MSGS_PARAM, array.toString());
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        flagMsgsTaskListener.setFlagMsgsTaskToNull();
        if (success) {
            flagMsgsTaskListener.onPostFlagMsgsExecute(response, mids, ids);
        } else {
            Log.e(TAG, ":error - " + response);
            flagMsgsTaskListener.onPostFlagMsgsExecuteFail(response, mids, ids);
        }
    }

    @Override
    protected void onCancelled() {
        flagMsgsTaskListener.setFlagMsgsTaskToNull();
    }

    public interface FlagMsgsTaskListener {
        void onPostFlagMsgsExecute(JSONObject response, MessageItem[] mids, String[] item);

        void onPostFlagMsgsExecuteFail(JSONObject response, MessageItem[] mids, String[] item);

        void setFlagMsgsTaskToNull();
    }

}
