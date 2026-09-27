package intellibitz.intellidroid.task;

import android.content.Context;
import android.os.AsyncTask;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class BulkUploadContactsTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "ContactsUploadTask";
    private final ContactItem user;
    private DeviceContactsUploadTaskListener deviceContactsUploadTaskListener;
    private Collection<ContactItem> deviceContactItems;
    private String url;
    private JSONObject response;
    private Context context;

    public BulkUploadContactsTask(Collection<ContactItem> deviceContactItems, ContactItem user,
                                  String url, Context context) {
        this.context = context;
        this.user = user;
        this.deviceContactItems = deviceContactItems;
        this.url = url;
    }

    public void setDeviceContactsUploadTaskListener(DeviceContactsUploadTaskListener deviceContactsUploadTaskListener) {
        this.deviceContactsUploadTaskListener = deviceContactsUploadTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, user.getDevice());
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, user.getDeviceRef());
        data.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
        data.put(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
        JSONArray contacts = DeviceContactContentProvider.fillJsonArrayFromDeviceContactItem(
                deviceContactItems, context);
        data.put("contacts", contacts.toString());

        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);

        if (null == response) {
            return false;
        }
        // TODO: register the new account here.
//            investigate response and take action
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        deviceContactsUploadTaskListener.setContactsUploadTaskToNull();
        if (success) {
            deviceContactsUploadTaskListener.onPostDeviceContactsUploadExecute(response, deviceContactItems, user);
        } else {
            deviceContactsUploadTaskListener.onPostContactsUploadExecuteFail(response, deviceContactItems, user);
        }
    }

    @Override
    protected void onCancelled() {
        deviceContactsUploadTaskListener.setContactsUploadTaskToNull();
    }

    public interface DeviceContactsUploadTaskListener {
        void onPostDeviceContactsUploadExecute(JSONObject response,
                                               Collection<ContactItem> deviceContactItems, ContactItem user);

        void onPostContactsUploadExecuteFail(JSONObject response,
                                             Collection<ContactItem> deviceContactItems, ContactItem user);

        void setContactsUploadTaskToNull();
    }

}
