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
public class GroupsAddUsersTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "GroupsAddUsersTask";
    private GroupsAddUsersTaskListener groupsAddUsersTaskListener;
    private String id;
    private String name;
    private ContactItem contactItem;
    private String[] contacts;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public GroupsAddUsersTask(String id, String name, String[] contacts,
                              String uid, String token, String device,
                              String deviceRef, String url) {
        this.id = id;
        this.name = name;
        this.contacts = contacts;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public GroupsAddUsersTask(ContactItem contactItem,
                              String uid, String token, String device,
                              String deviceRef, String url) {
        this.contactItem = contactItem;
        this.id = contactItem.getDataId();
        this.name = contactItem.getName();
        this.contacts = contactItem.getContactsAsArray();
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setGroupsAddUsersTaskListener(GroupsAddUsersTaskListener groupsAddUsersTaskListener) {
        this.groupsAddUsersTaskListener = groupsAddUsersTaskListener;
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
                jsonObject.put("type", "user");
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
        groupsAddUsersTaskListener.setGroupsAddUsersTaskToNull();
        if (success) {
            groupsAddUsersTaskListener.onPostGroupsAddUsersExecute(response,
                    id, name, contacts, contactItem);
        } else {
            groupsAddUsersTaskListener.onPostGroupsAddUsersExecuteFail(response,
                    id, name, contacts, contactItem);
        }
    }

    @Override
    protected void onCancelled() {
//        groupsAddUsersTask = null;
        groupsAddUsersTaskListener.setGroupsAddUsersTaskToNull();
    }

    public interface GroupsAddUsersTaskListener {
        void onPostGroupsAddUsersExecute(JSONObject response, String id, String name,
                                         String[] contacts, ContactItem contactItem);

        void onPostGroupsAddUsersExecuteFail(JSONObject response, String id, String name,
                                             String[] contacts, ContactItem contactItem);

        void setGroupsAddUsersTaskToNull();
    }

}
