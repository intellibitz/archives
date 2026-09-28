package intellibitz.intellidroid.gcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MessageChatGroupContentProvider
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.task.AckDocTask
import intellibitz.intellidroid.task.RcvDocTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class GCMMessageListenerService :
    FirebaseMessagingService(),
    RcvDocTask.RcvDocTaskListener,
    AckDocTask.AckDocTaskListener {

    private var rcvDocTask: RcvDocTask? = null
    private var ackDocTask: AckDocTask? = null

    private fun processAckDocFromCloud(response: JSONObject) {
        Log.e(TAG, "ACK DOC result: $response")
    }

    private fun processGetMsgDocFromCloud(response: JSONObject, currentUser: ContactItem?) {
        try {
            val jsonObject = response.getJSONObject("doc")
            val msgType = jsonObject.optString("msg_type")
            if (TextUtils.isEmpty(msgType)) return
            var messageItem: MessageItem? = null
            if ("EMAIL" == msgType) {
                messageItem = MessageEmailContentProvider.savesMsgDocTypeInDBFromJSON(
                    jsonObject, currentUser, this
                )
            } else if ("CHAT" == msgType) {
                val toType = jsonObject.optString("to_type")
                messageItem = if ("GROUP".equals(toType, ignoreCase = true)) {
                    MessageChatGroupContentProvider.savesMsgDocTypeInDBFromJSON(
                        jsonObject, currentUser, this
                    )
                } else {
                    MessageChatContentProvider.savesMsgDocTypeInDBFromJSON(
                        jsonObject, currentUser, this
                    )
                }
            }

            if (messageItem != null && 0L != messageItem._id) {
                execAckDocTask(messageItem)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun execAckDocTask(messageItem: MessageItem) {
        val currentUser = user ?: return
        val task = AckDocTask(
            messageItem.dataId, currentUser.dataId, currentUser.token,
            currentUser.device, currentUser.deviceRef,
            MainApplicationSingleton.AUTH_ACK_DOC
        )
        task.setAckDocTaskListener(this)
        ackDocTask = task
        task.execute()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val from = remoteMessage.from
        Log.d(TAG, "From: $from")
        val data = remoteMessage.data
        Log.d(TAG, "Data: $data")
        val bundle = Bundle().apply {
            for ((key, value) in data) {
                putString(key, value)
            }
        }
        onMessageReceived(from, bundle)
    }

    fun onMessageReceived(from: String?, bundle: Bundle) {
        Log.d(TAG, "Bundle: $bundle")
        val message = bundle.getString("message")
        val chatParams = bundle.getString("chat_params")
        Log.d(TAG, "From: $from")
        Log.d(TAG, "Message: $message")

        if (chatParams != null) {
            try {
                val msg = JSONObject(chatParams)
                val docId = msg.getString("msg_id")
                if (null == user) {
                    user = UserContentProvider.newActiveUserFromDB(this)
                }
                execRcvDocTask(docId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        if (message != null) {
            sendNotification(message)
        }
    }

    fun execRcvDocTask(docId: String) {
        val currentUser = user ?: return
        val task = RcvDocTask(
            docId, currentUser.dataId, currentUser.token,
            currentUser.device, currentUser.deviceRef,
            MainApplicationSingleton.AUTH_GET_DOC
        )
        task.setRcvDocTaskListener(this)
        rcvDocTask = task
        try {
            task.execute()
        } catch (e: NullPointerException) {
            Log.e(TAG, "RcvDocTask failed with NPE: " + e.message)
            onPostRcvDocExecuteFail(null)
        }
    }

    override fun onPostRcvDocExecute(response: JSONObject) {
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (1 == status) {
                Log.e(TAG, "GET DOC SUCCESS - $response")
                processGetMsgDocFromCloud(response, user)
            } else if (-1 == status || 99 == status) {
                onPostRcvDocExecuteFail(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPostRcvDocExecuteFail(response: JSONObject?) {
        Log.e(TAG, "GET DOC ERROR - $response")
    }

    override fun setRcvDocTaskToNull() {
        rcvDocTask = null
    }

    override fun onPostAckDocExecute(response: JSONObject) {
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (1 == status) {
                Log.e(TAG, "GET DOC SUCCESS - $response")
                processAckDocFromCloud(response)
            } else if (-1 == status || 99 == status) {
                onPostRcvDocExecuteFail(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPostAckDocExecuteFail(response: JSONObject?) {
        Log.e(TAG, "GET DOC ERROR - $response")
    }

    override fun setAckDocTaskToNull() {
        ackDocTask = null
    }

    private fun sendNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val flags = PendingIntent.FLAG_ONE_SHOT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getActivity(
            this, MainApplicationSingleton.PI_MSG_LISTENER_RQ_CODE, intent,
            flags
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var title: String = resources.getString(R.string.app_title)
        var msg = message
        if (message.contains(":")) {
            val parts = message.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size == 2) {
                title = parts[0]
                msg = parts[1]
            }
        }

        @Suppress("DEPRECATION")
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_arrow)
            .setContentTitle(title)
            .setContentText(msg)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setColor(Color.RED)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            MainApplicationSingleton.PI_MSG_LISTENER_RQ_CODE,
            notificationBuilder.build()
        )
    }

    companion object {
        private const val TAG = "GCMMessageService"
        private var user: ContactItem? = null
    }
}
