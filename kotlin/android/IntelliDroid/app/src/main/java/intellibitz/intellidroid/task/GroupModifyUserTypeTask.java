package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
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
public class GroupModifyUserTypeTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "GroupModifyUserTypeTask";
    int flag = 0;
    private GroupModifyUserTypeTaskListener groupModifyUserTypeTaskListener;
    private ContactItem contactThreadItem;
    private ContactItem contactItem;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GroupModifyUserTypeTask(ContactItem item, ContactItem contactItem,
                                   int flag, String uid, String token,
                                   String device, String deviceRef, String url) {
        this.flag = flag;
        this.contactThreadItem = item;
        this.contactItem = contactItem;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGroupModifyUserTypeTaskListener(GroupModifyUserTypeTaskListener groupDetailsTaskListener) {
        this.groupModifyUserTypeTaskListener = groupDetailsTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.GROUP_ID_PARAM, contactItem.getDataId());
        data.put(MainApplicationSingleton.USER_UID_PARAM, contactItem.getDataId());
        data.put(MainApplicationSingleton.MODIFY_TYPE_PARAM, contactItem.getType());
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
        groupModifyUserTypeTaskListener.setGroupModifyUserTypeTaskToNull();
        if (success) {
            groupModifyUserTypeTaskListener.onPostGroupModifyUserTypeExecute(
                    response, contactThreadItem, contactItem, flag);
        } else {
//                ERROR
            Log.e(TAG, "ERROR - " + response);
            groupModifyUserTypeTaskListener.onPostGroupModifyUserTypeExecuteFail(
                    response, contactThreadItem, contactItem, flag);
        }
    }

    @Override
    protected void onCancelled() {
        groupModifyUserTypeTaskListener.setGroupModifyUserTypeTaskToNull();
    }

    public interface GroupModifyUserTypeTaskListener {
        void onPostGroupModifyUserTypeExecute(
                JSONObject response, ContactItem item, ContactItem contactItem, int flag);

        void onPostGroupModifyUserTypeExecuteFail(
                JSONObject response, ContactItem item, ContactItem contactItem, int flag);

        void setGroupModifyUserTypeTaskToNull();
    }

}
