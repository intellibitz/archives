package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class LeaveGroupTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "LeaveGroupTask";
    private LeaveGroupTaskListener leaveGroupTaskListener;
    private String id;
    private String name;
    private ContactItem contactThreadItem;
    private ContactItem contactItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public LeaveGroupTask(ContactItem contactThreadItem, ContactItem contactItem,
                          String uid, String token, String device,
                          String deviceRef, String url) {
        this.contactThreadItem = contactThreadItem;
        this.contactItem = contactItem;
        this.id = contactThreadItem.getDataId();
        this.name = contactThreadItem.getName();
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setLeaveGroupTaskListener(LeaveGroupTaskListener LeaveGroupTaskListener) {
        this.leaveGroupTaskListener = LeaveGroupTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put("group_id", contactThreadItem.getDataId());
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        leaveGroupTaskListener.setLeaveGroupTaskToNull();
        if (success) {
            leaveGroupTaskListener.onPostLeaveGroupExecute(response,
                    id, name, contactItem, contactThreadItem);
        } else {
            leaveGroupTaskListener.onPostLeaveGroupExecuteFail(response,
                    id, name, contactItem, contactThreadItem);
        }
    }

    @Override
    protected void onCancelled() {
        leaveGroupTaskListener.setLeaveGroupTaskToNull();
    }

    public interface LeaveGroupTaskListener {
        void onPostLeaveGroupExecute(JSONObject response, String id, String name,
                                     ContactItem contactItem, ContactItem contactThreadItem);

        void onPostLeaveGroupExecuteFail(JSONObject response, String id, String name,
                                         ContactItem contactItem, ContactItem contactThreadItem);

        void setLeaveGroupTaskToNull();
    }

}
