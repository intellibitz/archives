package intellibitz.intellidroid.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.GCMTokenUploadTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.GCMTokenUploadTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.task.GCMTokenUploadTask;
import org.json.JSONObject;

import java.io.IOException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class GCMTokenIntentService extends
        IntentService implements
        GCMTokenUploadTask.GCMTokenUploadTaskListener {

    private static final String TAG = "GCMTokenService";
    //            projects/intellibitz-15956/topics/repository-changes.default
    private static final String[] TOPICS = {"global"};

    public GCMTokenIntentService() {
        super(TAG);
    }

    public static void startGCMTokenService(String token, ContactItem user, Context context) {
        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(context, GCMTokenIntentService.class);
        intent.putExtra("token", token);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
//            FCM DOES NOT REQUIRE THIS
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
/*
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
*/
            String token = intent.getStringExtra("token");

            ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            // TODO: Implement this method to send any registration to your app's servers.
            sendGCMTokenToCloud(token, user);
            // Subscribe to topic channels
            subscribeTopics(token);
            // [END get_token]
            Log.d(TAG, "GCM Registration Token: " + token);

            MainApplicationSingleton applicationSingleton =
                    MainApplicationSingleton.getInstance(getApplicationContext());
            applicationSingleton.putStringValueSP("gcm_token", token);
            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            applicationSingleton.putBooleanValueSP(MainApplicationSingleton.SENT_TOKEN_TO_SERVER, true);

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
//            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(
                MainApplicationSingleton.BROADCAST_GCM_REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     * @param user
     */
    private void sendGCMTokenToCloud(String token, ContactItem user) {
        // Add custom implementation, as needed.
//        // TODO: 12-04-2016
        user.setGcmToken(token);
//        update this to true, after cloud sync
        user.setGcmTokenSentToCloud(false);
        GCMTokenUploadTask gcmTokenUploadTask = new GCMTokenUploadTask(
                user.getGcmToken(), user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.GCMTOKEN_UPLOAD_URL, user, this);
        gcmTokenUploadTask.setRequestTimeoutMillis(30000);
        gcmTokenUploadTask.setGcmTokenUploadTaskListener(this);
        gcmTokenUploadTask.execute();
    }

    //    called after the gcm token is send successfully to the cloud
    private void postGCMTokenSuccess(ContactItem user) {
//        saves user, in db
        UserContentProvider.updateGCMTokenInDB(user, getApplicationContext());
    }
    // [END subscribe_topics]

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
// Blocking methods. Execute them inside an AsyncTask or background thread.
//        FCM DOES NOT REQUIRE THIS
//        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
//            projects/intellibitz-15956/topics/repository-changes.default
//            pubSub.subscribe(token, "/topics/" + topic, null);
// Non-blocking methods. No need to use AsyncTask or background thread.
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
//        FirebaseMessaging.getInstance().unsubscribeFromTopic("mytopic");
    }

    @Override
    public void onPostGCMTokenUploadResponse(JSONObject response, ContactItem user) {
        int status = 0;
        if (response != null)
            status = response.optInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
        if (null == response || -1 == status || 99 == status) {
            onPostGCMTokenUploadErrorResponse(response);
        } else {
            Log.e(TAG, "Token upload SUCCESS - " + response);
            user.setGcmTokenSentToCloud(true);
            postGCMTokenSuccess(user);
        }
    }

    @Override
    public void onPostGCMTokenUploadErrorResponse(JSONObject response) {
        Log.e(TAG, " ERROR - " + response);
        MainActivity.broadcastForceLogoutIfNeg1(response, this);
    }


}