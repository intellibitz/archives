package intellibitz.intellidroid.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.task.GetEmailsTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.io.IOException

class UserEmailIntentService :
    IntentService("UserEmailIntentService"),
    GetEmailsTask.GetEmailsTaskListener {

    companion object {
        const val TAG = "UserEmailIntentService"
        const val ACTION_SYNC_USER_EMAIL =
            "intellibitz.intellidroid.service.action.SYNC_USER_EMAIL"
        const val ACTION_SYNC_SAVE_USER_EMAIL =
            "intellibitz.intellidroid.service.action.SYNC_SAVE_USER_EMAIL"
        const val ACTION_SAVE_USER_EMAIL =
            "intellibitz.intellidroid.service.action.SAVE_USER_EMAIL"

        @JvmStatic
        fun asyncEmailsFromCloudAndSavesInDb(user: ContactItem?, context: Context?) {
            if (null == user) {
                Log.e(TAG, "asyncEmailsFromCloudAndSavesInDb: user is null")
                return
            }
            if (null == context) {
                Log.e(TAG, "asyncEmailsFromCloudAndSavesInDb: context is null")
                return
            }
            if (null == user.contactItems || user.contactItems.isEmpty()) {
                try {
                    // syncs email from cloud
                    // intent service runs async in its own thread
                    val intent = Intent(context, UserEmailIntentService::class.java)
                    intent.action = ACTION_SYNC_SAVE_USER_EMAIL
                    intent.putExtra(ContactItem.USER_CONTACT, user.clone() as Parcelable)
                    context.startService(intent)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    Log.e(TAG, TAG + e.toString())
                }
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            val user: ContactItem? = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            if (ACTION_SAVE_USER_EMAIL == action) {
                savesUserEmail(user)
            } else if (ACTION_SYNC_SAVE_USER_EMAIL == action) {
                syncsUserEmailsFromCloudAndSaveInDb(user)
            } else if (ACTION_SYNC_USER_EMAIL == action) {
                syncsUsersEmailsFromCloud(user)
            }
        }
    }

    private fun savesUserEmail(user: ContactItem?) {
        try {
            val uri = UserEmailContentProvider.savesUserEmailsInDB(user, this)
            Log.e(TAG, "SUCCESS - Email insert: $uri")
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, TAG + e.toString())
        }
    }

    private fun syncsUserEmailsFromCloudAndSaveInDb(user: ContactItem?) {
        syncsUsersEmailsFromCloud(user, 1)
    }

    private fun syncsUsersEmailsFromCloud(user: ContactItem?, mode: Int) {
        if (user == null) return
        val getEmailsTask = GetEmailsTask(
            user.dataId, user.token, user.device, user.deviceRef,
            MainApplicationSingleton.AUTH_GET_EMAILS, user, this, mode
        )
        getEmailsTask.setRequestTimeoutMillis(30000)
        getEmailsTask.setGetEmailsTaskListener(this)
        getEmailsTask.execute()
    }

    private fun syncsUsersEmailsFromCloud(user: ContactItem?) {
        syncsUsersEmailsFromCloud(user, -1)
    }

    override fun onPostGetEmailsResponse(response: JSONObject?, user: ContactItem?, mode: Int) {
        if (response != null && user != null) {
            try {
                val status = response.getInt("status")
                if (1 == status) {
                    val responseJSONArray = response.getJSONArray("emails")
                    user.contactItems.addAll(
                        UserContentProvider.setsEmailsFromJSONArray(responseJSONArray, this)
                    )
                    if (!user.contactItems.isEmpty()) {
                        if (1 == mode) {
                            try {
                                val uri = UserEmailContentProvider.savesUserEmailsInDB(user, this)
                                val intent = Intent(MainApplicationSingleton.BROADCAST_USER_UPDATED)
                                intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                                Log.e(TAG, "onPostGetEmailsResponse: SUCCESS - Email insert: $uri")
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    onPostGetEmailsErrorResponse(response)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, TAG + e.toString())
            }
        }
    }

    override fun onPostGetEmailsErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetEmailsErrorResponse:$response")
    }
}
