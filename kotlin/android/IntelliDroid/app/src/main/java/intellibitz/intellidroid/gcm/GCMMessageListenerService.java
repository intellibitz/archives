package intellibitz.intellidroid.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.task.AckDocTask;
import intellibitz.intellidroid.task.RcvDocTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageChatGroupContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.task.AckDocTask;
import intellibitz.intellidroid.task.RcvDocTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageChatGroupContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.UserContentProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import androidx.core.app.NotificationCompat;

public class GCMMessageListenerService extends
        FirebaseMessagingService implements
        RcvDocTask.RcvDocTaskListener,
        AckDocTask.AckDocTaskListener {

    private static final String TAG = "GCMMessageService";
    private static ContactItem user = null;
    private RcvDocTask rcvDocTask;
    private AckDocTask ackDocTask;


    private void processAckDocFromCloud(JSONObject response) {
        Log.e(TAG, "ACK DOC result: " + response);
    }

    private void processGetMsgDocFromCloud(JSONObject response, ContactItem user) {
        try {
            JSONObject jsonObject = response.getJSONObject("doc");
            String msgType = jsonObject.optString("msg_type");
            if (TextUtils.isEmpty(msgType)) return;
            MessageItem messageItem = null;
//                    handles emails
            if ("EMAIL".equals(msgType)) {
                messageItem =
                        MessageEmailContentProvider.savesMsgDocTypeInDBFromJSON(
                                jsonObject, user, this);
            } else if ("CHAT".equals(msgType)) {
                String to_type = jsonObject.optString("to_type");
                if ("GROUP".equalsIgnoreCase(to_type)) {
                    messageItem =
                            MessageChatGroupContentProvider.savesMsgDocTypeInDBFromJSON(
                                    jsonObject, user, this);
                } else {
                    messageItem =
                            MessageChatContentProvider.savesMsgDocTypeInDBFromJSON(
                                    jsonObject, user, this);
                }
            }
/*
            MessageItem messageItem = MessageContentProvider.savesMsgDocTypeInDBFromJSON(
                    jsonObject, user, this);
*/
            if (null == messageItem || 0 == messageItem.get_id()) {
//                        message arrived without a thread
//                        // TODO: 12-03-2016
//                        to handle zombie message scenario
            } else {
                execAckDocTask(messageItem);

            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void execAckDocTask(MessageItem messageItem) {
        //                sends acknowledgement
        ackDocTask = new AckDocTask(messageItem.getDataId(), GCMMessageListenerService.user.getDataId(), GCMMessageListenerService.user.getToken(),
                GCMMessageListenerService.user.getDevice(), GCMMessageListenerService.user.getDeviceRef(),
                MainApplicationSingleton.AUTH_ACK_DOC);
        ackDocTask.setAckDocTaskListener(this);
        ackDocTask.execute();
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        String from = remoteMessage.getFrom();
        Log.d(TAG, "From: " + from);
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "Data: " + data);
//        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Bundle bundle = new Bundle();
        for (String key : data.keySet()) {
            bundle.putString(key, data.get(key));
        }
        onMessageReceived(from, bundle);
    }
    // [END receive_message]

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */
    // [START receive_message]
//    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        Log.d(TAG, "Bundle: " + bundle);
        String message = bundle.getString("message");
        String chatParams = bundle.getString("chat_params");
        String displayMsg = bundle.getString("display_msg");
        String collapse = bundle.getString("collapse_key");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (chatParams != null) {
            try {
                JSONObject msg = new JSONObject(chatParams);
                String docId = msg.getString("msg_id");
                if (null == user) {
                    user = UserContentProvider.newActiveUserFromDB(this);
//                    UserContentProvider.initUserFromAppContext(user, this);
//                    UserEmailContentProvider.populateUserEmailsJoinByDataId(user, this);
                }
                execRcvDocTask(docId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        // [END_EXCLUDE]
    }

    public void execRcvDocTask(String docId) {
        rcvDocTask = new RcvDocTask(docId, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_GET_DOC);
        rcvDocTask.setRcvDocTaskListener(this);
        try {
            rcvDocTask.execute();
        } catch (NullPointerException e) {
            Log.e(TAG, "RcvDocTask failed with NPE: " + e.getMessage());
            onPostRcvDocExecuteFail(null);
        }
    }
    // [END receive_message]

    @Override
    public void onPostRcvDocExecute(JSONObject response) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "GET DOC SUCCESS - " + response);
                processGetMsgDocFromCloud(response, user);
            } else if (-1 == status) {
                onPostRcvDocExecuteFail(response);
            } else if (99 == status) {
                onPostRcvDocExecuteFail(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostRcvDocExecuteFail(JSONObject response) {
        Log.e(TAG, "GET DOC ERROR - " + response);
    }

    @Override
    public void setRcvDocTaskToNull() {
        rcvDocTask = null;
    }


    @Override
    public void onPostAckDocExecute(JSONObject response) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(GCMMessageListenerService.TAG, "GET DOC SUCCESS - " + response);
                processAckDocFromCloud(response);
            } else if (-1 == status) {
                onPostRcvDocExecuteFail(response);
            } else if (99 == status) {
                onPostRcvDocExecuteFail(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostAckDocExecuteFail(JSONObject response) {
        Log.e(GCMMessageListenerService.TAG, "GET DOC ERROR - " + response);
    }

    @Override
    public void setAckDocTaskToNull() {
        ackDocTask = null;
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
//        MainActivity is the router
//        GCM will be running, even when the user has cleared the app data.. so always fall back
//        to MainActivity.. do not route to IntellibitzActivity (for the use case mentioned above)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, MainApplicationSingleton.PI_MSG_LISTENER_RQ_CODE /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        String title = MainApplicationSingleton.INTELLIBITZ;
        String title = getResources().getString(R.string.app_title);
        String msg = message;
        if (message.contains(":")) {
            String[] parts = message.split(":");
            if (2 == parts.length) {
                title = parts[0];
                msg = parts[1];
            }
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_arrow)  // the status icon
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.akon_logo))  // the status icon
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(Color.RED)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MainApplicationSingleton.PI_MSG_LISTENER_RQ_CODE /* ID of notification */,
                notificationBuilder.build());
    }


}