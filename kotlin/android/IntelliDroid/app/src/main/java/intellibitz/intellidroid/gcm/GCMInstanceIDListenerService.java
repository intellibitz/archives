package intellibitz.intellidroid.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.UserContentProvider;

public class GCMInstanceIDListenerService extends
        FirebaseInstanceIdService {

    private static final String TAG = "FirebaseTokenID";
//    private ContactItem user;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    public static void updateToken(ContactItem user, Context context) {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "GCM token: " + token);
        // TODO: Implement this method to send any registration to your app's servers.
//        sendRegistrationToServer(refreshedToken);
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        if (null == user) {
            Log.e(TAG, "User null - cannot update GCM token: " + token);
        } else {
            GCMTokenIntentService.startGCMTokenService(token, user, context);
        }
    }

    public static void startInstanceIdListenerService(ContactItem user, Context context) {
        Intent intent = new Intent(context, GCMInstanceIDListenerService.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        context.startService(intent);
    }


    @Override
    public void onStart(Intent intent, int startId) {
//        user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    // [END refresh_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        ContactItem user = UserContentProvider.newActiveUserForGCMFromDB(this);
        if (user != null && !TextUtils.isEmpty(user.getDataId())) {
            updateToken(user, this);
        }
    }

}