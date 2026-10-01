package intellibitz.intellidroid.gcm

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.task.GCMTokenUploadTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.io.IOException

@Suppress("DEPRECATION")
class GCMTokenIntentService : IntentService(TAG), GCMTokenUploadTask.GCMTokenUploadTaskListener {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        try {
            val token = intent.getStringExtra("token")
            val user = intent.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
            if (token != null && user != null) {
                sendGCMTokenToCloud(token, user)
                subscribeTopics(token)
                Log.d(TAG, "GCM Registration Token: $token")

                val applicationSingleton = MainApplicationSingleton.getInstance(applicationContext)
                applicationSingleton.putStringValueSP("gcm_token", token)
                applicationSingleton.putBooleanValueSP(MainApplicationSingleton.SENT_TOKEN_TO_SERVER, true)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failed to complete token refresh", e)
        }

        val registrationComplete = Intent(MainApplicationSingleton.BROADCAST_GCM_REGISTRATION_COMPLETE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
    }

    private fun sendGCMTokenToCloud(token: String, user: ContactItem) {
        user.gcmToken = token
        user.isGcmTokenSentToCloud = 0
        val gcmTokenUploadTask = GCMTokenUploadTask(
            user.gcmToken, user.dataId, user.token, user.device, user.deviceRef,
            MainApplicationSingleton.GCMTOKEN_UPLOAD_URL, user, this
        )
        gcmTokenUploadTask.requestTimeoutMillis = 30000
        gcmTokenUploadTask.setGcmTokenUploadTaskListener(this)
        gcmTokenUploadTask.execute()
    }

    private fun postGCMTokenSuccess(user: ContactItem) {
        UserContentProvider.updateGCMTokenInDB(user, applicationContext)
    }

    @Throws(IOException::class)
    private fun subscribeTopics(token: String) {
        for (topic in TOPICS) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
        }
    }

    override fun onPostGCMTokenUploadResponse(response: JSONObject?, user: ContactItem?) {
        var status = 0
        if (response != null) {
            status = response.optInt(MainApplicationSingleton.STATUS_PARAM)
        }
        if (response == null || -1 == status || 99 == status) {
            onPostGCMTokenUploadErrorResponse(response)
        } else {
            Log.e(TAG, "Token upload SUCCESS - $response")
            user?.isGcmTokenSentToCloud = 1
            if (user != null) {
                postGCMTokenSuccess(user)
            }
        }
    }

    override fun onPostGCMTokenUploadErrorResponse(response: JSONObject?) {
        Log.e(TAG, " ERROR - $response")
        MainActivity.broadcastForceLogoutIfNeg1(response, this)
    }

    companion object {
        private const val TAG = "GCMTokenService"
        private val TOPICS = arrayOf("global")

        @JvmStatic
        fun startGCMTokenService(token: String?, user: ContactItem, context: Context) {
            val intent = Intent(context, GCMTokenIntentService::class.java).apply {
                putExtra("token", token)
                putExtra(ContactItem.USER_CONTACT, user as Parcelable)
            }
            context.startService(intent)
        }
    }
}
