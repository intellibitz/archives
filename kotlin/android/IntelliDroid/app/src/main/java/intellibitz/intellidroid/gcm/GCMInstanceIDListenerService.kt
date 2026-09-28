package intellibitz.intellidroid.gcm

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem

@Suppress("DEPRECATION")
class GCMInstanceIDListenerService : FirebaseInstanceIdService() {

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTokenRefresh() {
        val user = UserContentProvider.newActiveUserForGCMFromDB(this)
        if (user != null && !TextUtils.isEmpty(user.dataId)) {
            updateToken(user, this)
        }
    }

    companion object {
        private const val TAG = "FirebaseTokenID"

        @JvmStatic
        fun updateToken(user: ContactItem?, context: Context) {
            val token = FirebaseInstanceId.getInstance().token
            Log.d(TAG, "GCM token: $token")
            if (null == user) {
                Log.e(TAG, "User null - cannot update GCM token: $token")
            } else {
                GCMTokenIntentService.startGCMTokenService(token, user, context)
            }
        }

        @JvmStatic
        fun startInstanceIdListenerService(user: ContactItem?, context: Context) {
            val intent = Intent(context, GCMInstanceIDListenerService::class.java)
            intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
            context.startService(intent)
        }
    }
}
