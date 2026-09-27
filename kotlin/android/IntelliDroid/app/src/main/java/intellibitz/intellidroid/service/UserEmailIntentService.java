package intellibitz.intellidroid.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.GetEmailsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.GetEmailsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 *
 */
public class UserEmailIntentService extends
        IntentService implements
        GetEmailsTask.GetEmailsTaskListener {
    public static final String TAG = "UserEmailIntentService";

    public static final String ACTION_SYNC_USER_EMAIL =
            "intellibitz.intellidroid.service.action.SYNC_USER_EMAIL";
    public static final String ACTION_SYNC_SAVE_USER_EMAIL =
            "intellibitz.intellidroid.service.action.SYNC_SAVE_USER_EMAIL";
    public static final String ACTION_SAVE_USER_EMAIL =
            "intellibitz.intellidroid.service.action.SAVE_USER_EMAIL";

    public UserEmailIntentService() {
        super("UserEmailIntentService");
    }

    public static void asyncEmailsFromCloudAndSavesInDb(ContactItem user, Context context) {
        if (null == user) {
            Log.e(TAG, "asyncEmailsFromCloudAndSavesInDb: user is null");
            return;
        }
        if (null == context) {
            Log.e(TAG, "asyncEmailsFromCloudAndSavesInDb: context is null");
            return;
        }
        if (null == user.getContactItems() || user.getContactItems().isEmpty()) {
            try {
                // syncs email from cloud
//        intent service runs async in its own thread
                Intent intent = new Intent(context, UserEmailIntentService.class);
                intent.setAction(ACTION_SYNC_SAVE_USER_EMAIL);
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user.clone());
                context.startService(intent);
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, TAG + e.toString());
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            if (ACTION_SAVE_USER_EMAIL.equals(action)) {
                savesUserEmail(user);
            } else if (ACTION_SYNC_SAVE_USER_EMAIL.equals(action)) {
                syncsUserEmailsFromCloudAndSaveInDb(user);
            } else if (ACTION_SYNC_USER_EMAIL.equals(action)) {
                syncsUsersEmailsFromCloud(user);
            }
        }
    }

    /**
     */
    private void savesUserEmail(ContactItem user) {
        try {
/*
            for (String email : emails) {
                user.addEmail(new ContactItem(email, email, email));
            }
*/
            Uri uri = UserEmailContentProvider.savesUserEmailsInDB(user, this);
            Log.e(TAG, "SUCCESS - Email insert: " + uri);
/*
            ContentValues values = new ContentValues();
            values.put(ContactItem.TAG, MainApplicationSingleton.Serializer.serialize(user));
            ContentResolver contentResolver = getContentResolver();
            Uri uri = contentResolver.insert(UserEmailContentProvider.CONTENT_URI, values);
*/
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.toString());
        }
    }

    private void syncsUserEmailsFromCloudAndSaveInDb(ContactItem user) {
        syncsUsersEmailsFromCloud(user, 1);
    }

    private void syncsUsersEmailsFromCloud(ContactItem user, int mode) {
        GetEmailsTask getEmailsTask = new GetEmailsTask(user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_GET_EMAILS, user, this, mode);
        getEmailsTask.setRequestTimeoutMillis(30000);
        getEmailsTask.setGetEmailsTaskListener(this);
        getEmailsTask.execute();
    }

    private void syncsUsersEmailsFromCloud(ContactItem user) {
/*
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, user.getDevice());
        data.put(MainApplicationSingleton.DEVICE_REF_PARAM, user.getDeviceRef());
        data.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
        data.put(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
        // params comes from the execute() call: params[0] is the url.
        JSONObject response = HttpUrlConnectionParser.postHTTP(
                MainApplicationSingleton.AUTH_GET_EMAILS, data);
*/
        syncsUsersEmailsFromCloud(user, -1);
    }

    @Override
    public void onPostGetEmailsResponse(JSONObject response, ContactItem user, int mode) {
        if (response != null) {
            try {
                int status = response.getInt("status");
                if (1 == status) {
                    JSONArray responseJSONArray = response.getJSONArray("emails");
                    user.getContactItems().addAll(UserContentProvider.setsEmailsFromJSONArray(responseJSONArray, this));
                    if (!user.getContactItems().isEmpty()) {
                        if (1 == mode) {
                            try {
                                Uri uri = UserEmailContentProvider.savesUserEmailsInDB(user, this);
                                Intent intent = new Intent(MainApplicationSingleton.BROADCAST_USER_UPDATED);
                                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                                Log.e(TAG, "onPostGetEmailsResponse: SUCCESS - Email insert: " + uri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    onPostGetEmailsErrorResponse(response);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, TAG + e.toString());
            }
        }
    }

    @Override
    public void onPostGetEmailsErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetEmailsErrorResponse:" + response);
    }

}
