package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 */
public class UpdateGroupTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "UpdateGroupTask";
    private UpdateGroupTaskListener updateGroupTaskListener;
    private String name;
    private File file;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;
    private ContactItem contactItem;
    private MessageItem messageItem;

    public UpdateGroupTask(ContactItem contactItem,
                           MessageItem messageItem, String uid,
                           String token, String device, String deviceRef, String url) {
        this.contactItem = contactItem;
        this.messageItem = messageItem;
        this.name = contactItem.getName();
        String profilePic = contactItem.getProfilePic();
        if (profilePic != null) {
            this.file = new File(profilePic);
        }
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setUpdateGroupTaskListener(UpdateGroupTaskListener groupsCreateTaskListener) {
        this.updateGroupTaskListener = groupsCreateTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == contactItem) return false;
        String id = contactItem.getDataId();
        if (null == id) return false;
        if (null == file) {
            HashMap<String, String> data = new HashMap<>();
            data.put(MainApplicationSingleton.DEVICE_PARAM, device);
            data.put(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            data.put(MainApplicationSingleton.UID_PARAM, uid);
            data.put(MainApplicationSingleton.TOKEN_PARAM, token);
            data.put(MainApplicationSingleton.NAME_PARAM, name);
            data.put(MainApplicationSingleton.GROUP_ID_PARAM, id);
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
                multipart.addFormField(MainApplicationSingleton.GROUP_ID_PARAM, id);
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
        updateGroupTaskListener.setUpdateGroupTaskToNull();
        if (success) {
            updateGroupTaskListener.onPostUpdateGroupExecute(response,
                    name, file, contactItem, messageItem);
        } else {
            updateGroupTaskListener.onPostUpdateGroupExecuteFail(response,
                    name, file, contactItem, messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        updateGroupTaskListener.setUpdateGroupTaskToNull();
    }

    public interface UpdateGroupTaskListener {
        void onPostUpdateGroupExecute(JSONObject response,
                                      String name, File file,
                                      ContactItem contactItem, MessageItem messageItem);

        void onPostUpdateGroupExecuteFail(JSONObject response,
                                          String name, File file,
                                          ContactItem contactItem, MessageItem messageItem);

        void setUpdateGroupTaskToNull();
    }

}
