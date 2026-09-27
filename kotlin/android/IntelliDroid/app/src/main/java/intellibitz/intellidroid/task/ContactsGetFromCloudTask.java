package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class ContactsGetFromCloudTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "ContactsGetFromCloud";
    private ContactsGetFromCloudTaskListener contactsGetFromCloudTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;

    ContactsGetFromCloudTask(String uid, String token, String device, String deviceRef, String url) {
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setContactsGetFromCloudTaskListener(ContactsGetFromCloudTaskListener contactsUploadTaskListener) {
        this.contactsGetFromCloudTaskListener = contactsUploadTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
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
        contactsGetFromCloudTaskListener.setContactsGetFromCloudTaskToNull();
        if (success) {
            contactsGetFromCloudTaskListener.onPostContactsGetFromCloudExecute(response);
        } else {
            contactsGetFromCloudTaskListener.onPostContactsGetFromCloudExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        contactsGetFromCloudTaskListener.setContactsGetFromCloudTaskToNull();
        Log.e(TAG, "CONTACTS GET ERROR - " + response);
    }

    public interface ContactsGetFromCloudTaskListener {
        void onPostContactsGetFromCloudExecute(JSONObject response);

        void onPostContactsGetFromCloudExecuteFail(JSONObject response);

        void setContactsGetFromCloudTaskToNull();
    }

}
