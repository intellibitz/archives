package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 */
public class CreateGroupTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "CreateGroupTask";
    private CreateGroupTaskListener createGroupTaskListener;
    private ContactItem contactItem;
    private String name;
    private File file;
    private String[] contacts;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    public CreateGroupTask(ContactItem contactItem, String uid,
                           String token, String device, String deviceRef, String url) {
        this.contactItem = contactItem;
        this.name = contactItem.getName();
        String profilePic = contactItem.getProfilePic();
        if (profilePic != null) {
            this.file = new File(profilePic);
        }
        this.contacts = contactItem.getContactsAsArray();
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public CreateGroupTask(String name, File file, String[] contacts, String uid,
                           String token, String device, String deviceRef, String url) {
        this.name = name;
        this.file = file;
        this.contacts = contacts;
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setCreateGroupTaskListener(CreateGroupTaskListener createGroupTaskListener) {
        this.createGroupTaskListener = createGroupTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == file) {
            HashMap<String, String> data = new HashMap<>();
            data.put(MainApplicationSingleton.DEVICE_PARAM, device);
            data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            data.put(MainApplicationSingleton.UID_PARAM, uid);
            data.put(MainApplicationSingleton.TOKEN_PARAM, token);
            data.put(MainApplicationSingleton.NAME_PARAM, name);
            // params comes from the execute() call: params[0] is the url.
            response = HttpUrlConnectionParser.postHTTP(url, data);
        } else {
            String fileName = file.getAbsolutePath();
            String charset = "UTF-8";
            try {
                HttpUrlConnectionParser.MultipartUtility multipart =
                        new HttpUrlConnectionParser.MultipartUtility(url, charset);
                multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device);
                multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
                multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid);
                multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token);
                multipart.addFormField(MainApplicationSingleton.NAME_PARAM, name);
                multipart.addFilePart(MainApplicationSingleton.PROFILE_PIC_PARAM, file, fileName);
                // params comes from the execute() call: params[0] is the url.
                response = multipart.finishAsJSON(); // response from server.
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }

        }
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
//        groupsCreateTask = null;
        createGroupTaskListener.setCreateGroupTaskToNull();
        if (success) {
            createGroupTaskListener.onPostCreateGroupExecute(response,
                    name, file, contacts, contactItem);
        } else {
            createGroupTaskListener.onPostCreateGroupExecuteFail(response,
                    name, file, contacts, contactItem);
        }
    }

    @Override
    protected void onCancelled() {
//        groupsCreateTask = null;
        createGroupTaskListener.setCreateGroupTaskToNull();
    }

    public interface CreateGroupTaskListener {
        void onPostCreateGroupExecute(JSONObject response,
                                      String name, File file, String[] contacts,
                                      ContactItem contactItem);

        void onPostCreateGroupExecuteFail(JSONObject response,
                                          String name, File file, String[] contacts,
                                          ContactItem contactItem);

        void setCreateGroupTaskToNull();
    }

}
