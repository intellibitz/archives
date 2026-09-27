package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class GroupDetailsTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "GroupDetailsTask";
    int flag = 0;
    private GroupDetailsTaskListener groupDetailsTaskListener;
    private ContactItem contactItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GroupDetailsTask(ContactItem item, String uid, String token,
                            String device, String deviceRef, String url) {
        this.contactItem = item;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public GroupDetailsTask(ContactItem item, int flag, String uid, String token,
                            String device, String deviceRef, String url) {
        this.flag = flag;
        this.contactItem = item;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGroupDetailsTaskListener(GroupDetailsTaskListener groupDetailsTaskListener) {
        this.groupDetailsTaskListener = groupDetailsTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == contactItem) return false;
        final String dataId = contactItem.getDataId();
        if (TextUtils.isEmpty(dataId)) return false;
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.GROUP_ID_PARAM, dataId);
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        groupDetailsTaskListener.setGroupDetailsTaskToNull();
        if (success) {
            groupDetailsTaskListener.onPostGroupDetailsTaskExecute(response, contactItem);
        } else {
            groupDetailsTaskListener.onPostGroupDetailsTaskExecuteFail(response, contactItem);
        }
    }

    @Override
    protected void onCancelled() {
        groupDetailsTaskListener.setGroupDetailsTaskToNull();
    }

    public interface GroupDetailsTaskListener {
        void onPostGroupDetailsTaskExecute(JSONObject response, ContactItem item);

        void onPostGroupDetailsTaskExecuteFail(JSONObject response, ContactItem item);

        void setGroupDetailsTaskToNull();
    }

}
