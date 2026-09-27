package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class GroupRemoveUsersTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "GroupsAddUsersTask";
    private GroupsRemoveUsersTaskListener groupsRemoveUsersTaskListener;
    private String id;
    private String name;
    private ContactItem contactThreadItem;
    private ContactItem contactItem;
    private String[] contacts;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GroupRemoveUsersTask(ContactItem contactThreadItem, ContactItem contactItem,
                                String uid, String token, String device,
                                String deviceRef, String url) {
        this.contactThreadItem = contactThreadItem;
        this.contactItem = contactItem;
        this.id = contactThreadItem.getDataId();
        this.name = contactThreadItem.getName();
        this.contacts = new String[]{contactItem.getDataId()};
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGroupsRemoveUsersTaskListener(GroupsRemoveUsersTaskListener groupsRemoveUsersTaskListener) {
        this.groupsRemoveUsersTaskListener = groupsRemoveUsersTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
        data.put(MainApplicationSingleton.UID_PARAM, uid);
        data.put(MainApplicationSingleton.TOKEN_PARAM, token);
        data.put("group_id", id);
        JSONArray jsonArray = new JSONArray();
        for (String contact : contacts) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uid", contact);
//                jsonObject.put("type", "user");
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        data.put("users", jsonArray.toString());
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
//        groupsAddUsersTask = null;
        groupsRemoveUsersTaskListener.setGroupsRemoveUsersTaskToNull();
        if (success) {
            groupsRemoveUsersTaskListener.onPostGroupsRemoveUsersExecute(response,
                    id, name, contacts, contactItem, contactThreadItem);
        } else {
            groupsRemoveUsersTaskListener.onPostGroupsRemoveUsersExecuteFail(response,
                    id, name, contacts, contactItem, contactThreadItem);
        }
    }

    @Override
    protected void onCancelled() {
//        groupsAddUsersTask = null;
        groupsRemoveUsersTaskListener.setGroupsRemoveUsersTaskToNull();
    }

    public interface GroupsRemoveUsersTaskListener {
        void onPostGroupsRemoveUsersExecute(JSONObject response, String id, String name,
                                            String[] contacts, ContactItem contactItem, ContactItem contactThreadItem);

        void onPostGroupsRemoveUsersExecuteFail(JSONObject response, String id, String name,
                                                String[] contacts, ContactItem contactItem, ContactItem contactThreadItem);

        void setGroupsRemoveUsersTaskToNull();
    }

}
