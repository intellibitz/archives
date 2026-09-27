package intellibitz.intellidroid.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import androidx.annotation.WorkerThread;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//import intellibitz.intellidroid.content.MessageChatGroupContentProvider;
//import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;

public class RcvDocService extends
        Service {
    public static final String CONTACT = "CONTACT";
    public static final String GROUP = "GROUP";
    public static final String BROADCAST = "BROADCAST";
    public static final String QUEUE_NOTIFY = "QUEUE-NOTIFY";
    public static final String GROUP_INFO = "GROUP-INFO";
    public static final String MSG = "MSG";
    public static final String MSG_INFO = "MSG-INFO";
    public static final String RCV_DOC = "rcv_doc";
    public static final String FORCE_LOGOUT = "force_logout";
    public static final String ON_ATTACHMENT_URL = "attachment_url";
    public static final String RCV_MSG = "rcv_msg";
    public static final String AKTPROTOTYPE = "aktprototype";
    public static final String TO_UID = "to_uid";
    public static final String TO_TYPE = "to_type";
    public static final String TXT = "txt";
    public static final String FROM_UID = "from_uid";
    public static final String FROM_NAME = "from_name";
    public static final String ACK_MSG = "ack_msg";
    public static final String ACK_DOC = "ack_doc";
    public static final String MSG_ID = "msg_id";
    public static final String DOC_ID = "doc_id";
    public static final String SEND_MSG = "send_msg";
    public static final String JOIN_UID = "join_uid";
    public static final String JOINED = "joined";
    public static final String UID = "uid";
    public static final String TOKEN = "token";
    public static final String DEVICE = "device";
    public static final String STATUS = "status";
    public static final String REF = "ref";
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;
    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;
    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    public static final int MSG_SET_VALUE = 3;
    //    on key messages
    //    on show typing from cloud
    static final int MSG_SHOW_TYPING = 5;
    private static final String TAG = "RcvDocService";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final Object lock = new Object();
    // write your code here
    Socket socket;
    onListenerRunnable mOnListenerRunnable;
    /**
     * Keeps track of all current registered clients.
     */
    ArrayList<Messenger> mClients = new ArrayList<>();
    /**
     * Holds last value set by a client.
     */
    int mValue = 0;
    Ack ackGetAttachmentEmit = new Ack() {
        @Override
        public void call(Object... args) {
            String result = Arrays.toString(args);
            Log.e(TAG, "get_attachment Callback with results....." + result);
            try {
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                int status = jsonObject.getInt(STATUS);
                // error
                if (0 == status) {
                } else if (99 == status) {
//                                handle error
                } else if (1 == status) {
//                                start recieving attachment on socket callbacks
                }
            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }
    };
    Ack ackDocEmit = new Ack() {
        @Override
        public void call(Object... objects) {
            Log.e(TAG, "ackDocEmit:Callback from 'ack_doc' with results....." + Arrays.toString(objects));
        }
    };
    private NotificationManager mNM;
    private String ref;
    private ContactItem user;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler serviceHandler;
    private String mName;
    private String url;
    private boolean reconnect = false;
    // Binder given to clients
//    private final IBinder mBinder = new LocalBinder();
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 100;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
/*
    public class LocalBinder extends Binder {
        ChatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ChatService.this;
        }
    }
*/
    public RcvDocService() {
        super();
    }

    private void sendMessageToClients(int msg, Object object) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message message = Message.obtain(null, msg, mValue, 0);
                message.obj = object;
                mClients.get(i).send(message);
            } catch (Throwable ignored) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
                Log.e(TAG, ignored.getMessage());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     */
    @WorkerThread
    private void onHandleIntent(Intent intent) {
        if (MainApplicationSingleton.INTENT_ACTION_FETCH_EMAIL_ATTACHMENT_CLOUD.equals(intent.getAction())) {
            ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            MessageItem attachmentItem = intent.getParcelableExtra(MessageItem.ATTACHMENT_MESSAGE);
            ResultReceiver resultReceiver = intent.getParcelableExtra("ResultReceiver");
            emitGetAttachment(user, attachmentItem, resultReceiver);
            return;
        }
        if (MainApplicationSingleton.INTENT_ACTION_FETCH_CHAT_ATTACHMENT_CLOUD.equals(intent.getAction())) {
/*
            ContactItem user = intent.getParcelableExtra(ContactItem.TAG);
            MessageItem messageItem = intent.getParcelableExtra(
                    MessageItem.TAG);
*/
            MessageItem attachmentItem = intent.getParcelableExtra(
                    MessageItem.ATTACHMENT_MESSAGE);
            asyncUpdateChatAttachmentFileInDB(attachmentItem, getApplicationContext());
            return;
        }
        if (MainApplicationSingleton.INTENT_ACTION_FETCH_CHATGROUP_ATTACHMENT_CLOUD.equals(intent.getAction())) {
/*
            ContactItem user = intent.getParcelableExtra(ContactItem.TAG);
            MessageItem messageItem = intent.getParcelableExtra(
                    MessageItem.TAG);
*/
            MessageItem attachmentItem = intent.getParcelableExtra(
                    MessageItem.ATTACHMENT_MESSAGE);
            asyncUpdateChatGroupAttachmentFileInDB(attachmentItem, getApplicationContext());
        }
        serviceHandler.obtainMessage().recycle();
    }

    @Override
    public void onDestroy() {
        if (socket != null) {
            disconnectFromSocket(null);
        }
//        quit looper as soon as the handle event is over.. in the handler
        mServiceLooper.quit();
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
//        showNotification();
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(mServiceLooper);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // For each start request, send a message to start a job and deliver the
            // start ID so we know which request we're stopping when we finish the job
//            Message msg = serviceHandler.obtainMessage();
            Message msg = new Message();
            msg.arg1 = startId;
            msg.arg2 = flags;
            msg.obj = intent;

            createSocket(intent);

            if (reconnect) {
                reconnectSocket(msg);
            } else {
                serviceHandler.sendMessage(msg);
            }
            Log.d(TAG, "onStartCommand: ");
        }
        // If we get killed, after returning from here, restart
//        super takes care of sticky start
        return super.onStartCommand(intent, flags, startId);
    }

    public void createSocket(Intent intent) {
        String urls = intent.getStringExtra("url");
        if (TextUtils.isEmpty(urls)) {
            Log.e(TAG, "createSocket: will fallback to url: " +
                    url + " for action: " + intent.getAction());
        } else {
            url = urls;
            Log.e(TAG, "createSocket: url incoming: " + url);
        }
        reconnect = intent.getBooleanExtra("reconnect", false);
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
        try {
            if (null == url) {
                Log.e(TAG, "createSocket: will fallback to url: " +
                        url + " for action: " + intent.getAction());
            } else {
//                socket = IO.socket(MainApplicationSingleton.SOCKET_HOST);
                socket = IO.socket(url);
            }
//            try reconnecting, with start.. if user is null
            if (socket != null && !socket.connected()) {
                reconnect = true;
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            Log.e(TAG, ignored.getMessage());
        }
    }

    private void reconnectSocket(Message msg) {
        if (user != null) {
//        sets reconnect to false..
//        if the callback to join fails, then reconnect will be set to true in the callback
            reconnect = false;
            disconnectFromSocket(msg);
            connectToSocket(msg);
        }
    }

    private void connectToSocket(Message msg) {
        try {
            socket.on(Socket.EVENT_CONNECT, new OnConnectListener(msg));
            socket.on(Socket.EVENT_ERROR, new OnErrorListener());
            socket.on(Socket.EVENT_CONNECT_ERROR, new OnErrorListener());
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, new OnErrorListener());
            socket.on(Socket.EVENT_RECONNECT_ERROR, new OnErrorListener());
            socket.on(Socket.EVENT_MESSAGE, new OnMessageListener());
            socket.on(Socket.EVENT_DISCONNECT, new OnDisconnectListener());
            socket.on(JOINED, new OnJoinedListener());
            socket.on(FORCE_LOGOUT, new OnForceLogoutListener());
            runOnListenerRunnable();
/*
            socket.on(RCV_DOC, new OnRcvDocEmitListener());
            socket.on(ON_ATTACHMENT_URL, new onAttachmentUrlEmitListener());
*/
            socket.connect();
            Log.e(TAG, "connectToSocket: Socket Connected onto ....." + url);
        } catch (Throwable ignored) {
            Log.e(TAG, ignored.getMessage());
        }
    }

    private void disconnectFromSocket(Message msg) {
        socket.off(Socket.EVENT_CONNECT);
        socket.off(Socket.EVENT_ERROR);
        socket.off(Socket.EVENT_CONNECT_ERROR);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT);
        socket.off(Socket.EVENT_RECONNECT_ERROR);
        socket.off(Socket.EVENT_MESSAGE);
        socket.off(Socket.EVENT_DISCONNECT);
        socket.off(JOINED);
        socket.off(FORCE_LOGOUT);
        socket.off(RCV_DOC);
        socket.off(ON_ATTACHMENT_URL);
        mOnListenerRunnable = null;
        if (msg != null) {
            serviceHandler.removeMessages(msg.what, msg.obj);
        }

//        socket.off("attachment_data_start");
//        socket.off("attachment_data_chunk");
//        socket.off("attachment_data_end");
//        socket.off("attachment_stream");
        socket.disconnect();
        Log.e(TAG, "disconnectFromSocket: Socket Disconnected onto ....." + url);
    }

    private void saveMessageThreadItemToDB(Message msg) {
        try {
            Bundle args = msg.getData();
            MessageItem item = args.getParcelable(MessageItem.TAG);
            MessageChatContentProvider.savesMessageItem(item, user, this);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            Log.e(TAG, ignored.getMessage());
        }
    }

/*
    private Cursor getMessageThreadEmailsCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MessageThreadEmailContentProvider.CONTENT_URI,
                Uri.encode(id));
        return getContentResolver().query(uri, null, null, null, null);

    }
*/

    public void asyncUpdateChatAttachmentFileInDB(MessageItem attachment, Context context) {
        try {
            String downloadURL = attachment.getDownloadURL();
            if (downloadURL.startsWith("http")) {
//                String key = MainApplicationSingleton.getLastPathFromURI(Uri.decode(downloadURL));
                String key = MainApplicationSingleton.getLastPathFromURI(downloadURL);
                File file = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key);
                HttpUrlConnectionParser.downloadURLToFile(downloadURL, file);
                attachment.setDownloadURL(file.getAbsolutePath());
                MessageAttachmentIntentService.asyncUpdateChatAttachmentFilePathInDB(context, attachment);
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            Log.e(TAG, ignored.getMessage());
        }
    }

    public void asyncUpdateChatGroupAttachmentFileInDB(MessageItem attachment, Context context) {
        try {
            String downloadURL = attachment.getDownloadURL();
            if (downloadURL.startsWith("http")) {
//                String key = MainApplicationSingleton.getLastPathFromURI(Uri.decode(downloadURL));
                String key = MainApplicationSingleton.getLastPathFromURI(downloadURL);
                File file = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key);
                HttpUrlConnectionParser.downloadURLToFile(downloadURL, file);
                attachment.setDownloadURL(file.getAbsolutePath());
                MessageAttachmentIntentService.asyncUpdateChatGroupAttachmentFilePathInDB(context, attachment);
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            Log.e(TAG, ignored.getMessage());
        }
    }

    private void rcvDocContact(JSONObject jsonObject) throws JSONException, IOException {
        ContactItem deviceContactItem = DeviceContactContentProvider.createsDeviceContactItemFromJSON(
                jsonObject, user.getDeviceRef());
        Uri uri = DeviceContactContentProvider.savesOrUpdatesDeviceContact(deviceContactItem, this);
        if (uri != null) {
            long id = ContentUris.parseId(uri);
            deviceContactItem.set_id(id);
            ackRcvDoc(deviceContactItem);
        }
        return;
    }

/*
    private void rcvDocGroup(JSONObject jsonObject) throws JSONException, IOException {
        ContactItem contactItem =
                MsgChatGrpContactsContentProvider.createsContactFromJSON(jsonObject);
        Uri uri = MsgChatGrpContactsContentProvider.savesContactThreadInDB(
                contactItem, this);
        if (uri != null) {
            long id = ContentUris.parseId(uri);
            contactItem.set_id(id);
            ackRcvDoc(contactItem);
        }
        return;
    }

    private void rcvDocGroupInfo(JSONObject jsonObject) throws JSONException, IOException {
        ContactItem contactItem =
                MsgChatGrpContactsContentProvider.updatePlusSaveContactThreadWithGroupInfoFromJSON(
                        jsonObject, this);
//        update of group , for delete group info.. can return null.. if the info is already consumed
//            acknowledges the info (NOTE: not the group, but the group info)
        if (contactItem != null && contactItem.getLatestInfo() != null) {
            ackRcvDoc(contactItem.getLatestInfo());
        }
*/
/*
        if (uri != null) {
            long id = ContentUris.parseId(uri);
            contactItem.set_id(id);
        }
*//*

        return;
    }
*/

    private void rcvDocMessage(JSONObject jsonObject, ContactItem user) throws JSONException, IOException {
        String msgType = jsonObject.optString("msg_type");
        if (TextUtils.isEmpty(msgType)) return;
        MessageItem messageItem = null;
//                    handles emails
        if ("EMAIL".equalsIgnoreCase(msgType)) {
//            // TODO: 3/8/16
//            to enable emails again with the new api
/*
            messageItem = MessageEmailContentProvider.savesMsgDocTypeInDBFromJSON(
                            jsonObject, user, this);
*/
        } else if ("CHAT".equalsIgnoreCase(msgType)) {
            String to_type = jsonObject.optString("to_type");
            if ("GROUP".equalsIgnoreCase(to_type)) {
/*
                messageItem = MessageChatGroupContentProvider.savesMsgDocTypeInDBFromJSON(
                        jsonObject, user, this);
*/
            } else {
                messageItem = MessageChatContentProvider.savesMsgDocTypeInDBFromJSON(
                        jsonObject, user, this);
            }
        }
        if (null == messageItem || 0 == messageItem.get_id()) {
//                        message arrived without a thread
//                        // TODO: 12-03-2016
//                        to handle zombie message scenario
            Log.e(TAG, "rcvDocMessage: failed saving message -" + messageItem);
        } else {
            ackRcvDoc(messageItem);
        }
/*
[
    {
        "_id": "6427d48d08db02e31100bddbaeb692f7",
        "_rev": "1-18a794e209bbf32de9ec97fe3565212a",
        "attachments": [],
        "txt": "Hi ",
        "to_uid": "USRMASTER_919100504678",
        "msg_type": "CHAT",
        "client_msg_ref": "3d6d3a61223c48afe38cc7f8132c5a6c_USRMASTER_919100504678_1467440159322.685059",
        "to_type": "USER",
        "doc_type": "MSG",
        "timestamp": 1467440159,
        "from_uid": "USRMASTER_919600037000",
        "from_name": "Jeff ",
        "msg_ref": "USRMASTER_919600037000:1467440159-8250",
        "doc_owner": "USRMASTER_919100504678",
        "chat_id": "USRMASTER_919600037000",
        "msg_direction": "IN",
        "chat_name": "Jeff "
    }
]
         */
    }

    private void rcvDocMessageInfo(JSONObject jsonObject) throws JSONException, IOException {
        if (null == jsonObject) return;
        String chatId = jsonObject.optString("chat_id");
        if (null == chatId) return;
        if (MessageChatContentProvider.isMessageFoundByChatId(chatId, this)) {
            MessageItem messageItem = new MessageItem();
            messageItem.setChatId(chatId);
            int result = MessageChatContentProvider.updateMessageInfoFromJSONByChatId(
                    jsonObject, messageItem, this);
            if (0 == result) {
//                update failed
//                // TODO: 12-05-2016
            } else {
                result = MessageChatContentProvider.updateMessageInfo(chatId, messageItem, this);
                if (0 == result) {
//                update failed
//                // TODO: 12-05-2016
                }
                ackRcvDoc(messageItem);
            }
/*
        } else if (MessageChatGroupContentProvider.isMessageFoundByChatId(chatId, this)) {
            MessageItem messageItem = new MessageItem();
            messageItem.setChatId(chatId);
            int result = MessageChatGroupContentProvider.updateMessageInfoFromJSONByChatId(
                    jsonObject, messageItem, this);
            if (0 == result) {
//                update failed
//                // TODO: 12-05-2016
            } else {
                result = MessageChatGroupContentProvider.updateMessageInfo(chatId, messageItem, this);
                if (0 == result) {
//                update failed
//                // TODO: 12-05-2016
                }
                ackRcvDoc(messageItem);
            }
*/
        } else {
            Log.e(TAG, "Message Ref not found: " + jsonObject);
        }

    }

    private void emitGetAttachment(ContactItem user, MessageItem attachmentItem,
                                   ResultReceiver resultReceiver) {
        try {
            JSONObject payload = new JSONObject();
            payload.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
            if ("OUT".equalsIgnoreCase(attachmentItem.getMessageDirection())) {
                payload.put("mailbox", "sent");
            } else {
                payload.put("mailbox", "INBOX");
//                payload.put(MainApplicationSingleton.EMAIL_PARAM, attachmentItem.getDocSenderEmail());
            }
//            payload.put("mailbox", attachmentItem.getMailbox());
//            payload.put("mailbox", "");
//            // TODO: 13-04-2016
//            email is always, docowner.. the intellibitz user
//            this will change, when multiple emails are setup and connected
            payload.put(MainApplicationSingleton.EMAIL_PARAM, user.getSignupEmail());
            payload.put("msg_uid", attachmentItem.getMsgAttachID());
            payload.put("attch_id", attachmentItem.getPartID());
            payload.put("filename", attachmentItem.getName());
            payload.put("encoding", attachmentItem.getEncoding());
            String content = attachmentItem.getType() + "/" + attachmentItem.getSubType();
            payload.put("content_type", content);
            Log.e(TAG, payload.toString());
            socket.emit("get_attachment", payload, ackGetAttachmentEmit);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            Log.e(TAG, ignored.getMessage());
        }
/*
emit get_attachment
                "{
uid: ""USRMASTER_919655653929"",
email: ""nishanth@intellibitz.com"",
mailbox: ""sent"",
msg_uid: 6,
attch_id: 2,
filename: ""image1.PNG"",
encoding: ""BASE64"",
content_type: ""image/png""
}"
                 */
    }

    private void ackRcvDoc(BaseItem item) throws JSONException {
//                    sendMessageToClients();
//                getContentResolver().notifyChange(ChatMessageContentProvider.CONTENT_URI, );
//                showNotification(msg);
//                String toId = jsonObject.getString(TO_UID);
        JSONObject payload = new JSONObject();
        payload.put(DOC_ID, item.getDataId());
//                payload.put(TO_UID, toId);
//                // TODO: 05-03-2016
//                acknowledge doc
        socket.emit(ACK_DOC, payload, ackDocEmit);
    }

    /**
     * Show a notification while this service is running.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = MainApplicationSingleton.INTELLIBITZ;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                MainApplicationSingleton.PI_RCVDOC_RQ_CODE,
                new Intent(this, MainApplicationSingleton.MAIN_ACTIVITY_CLASS), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_arrow)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Chatty Emails!")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    /**
     * Show a notification while this service is running.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification(String msg) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = MainApplicationSingleton.INTELLIBITZ;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                MainApplicationSingleton.PI_RCVDOC_RQ_CODE,
                new Intent(this, MainApplicationSingleton.MAIN_ACTIVITY_CLASS), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_arrow)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(msg)  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    private void rcvDocMessageThread(JSONObject jsonObject) throws JSONException, IOException {
        MessageItem messageItem = new MessageItem();
        MessageChatContentProvider.fillMessageFromJSON(jsonObject, messageItem);
        Uri uri = MessageChatContentProvider.savesMessageItem(messageItem, user, this);
        long id = Long.parseLong(uri.getLastPathSegment());
        messageItem.set_id(id);
        ackRcvDoc(messageItem);
    }

    private void emitGetAttachmentsAndUpdateFilePathInDB(MessageItem messageItem, ContactItem user) {
        Set<MessageItem> attachmentItems = messageItem.getAttachments();
        for (MessageItem attachmentItem : attachmentItems) {
            emitGetAttachment(user, attachmentItem, null);
        }
    }

    private void sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null);
    }

    private void sendShowTypingToClients(Object s) {
        sendMessageToClients(MSG_SHOW_TYPING, s);
    }

    private void runOnListenerRunnable() {
        if (null == mOnListenerRunnable) {
            mOnListenerRunnable = new onListenerRunnable();
            mOnListenerRunnable.run();
            Log.e(TAG, "runOnListenerRunnable : started");
        }
    }

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    if (reconnect) {
                        reconnectSocket(msg);
                    }
                    saveMessageThreadItemToDB(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg != null && msg.obj instanceof Intent) {
                onHandleIntent((Intent) msg.obj);
            }
//            stopSelf(msg.arg1);
//            mServiceLooper.quit();

        }
    }

    class onListenerRunnable implements Runnable {
        @Override
        public void run() {
            try {
//            // TODO: 12-02-2016
//            effective thread management strategies from the platform, to investigate
//            run forever
//            while (true){
//            Listens to CONNECT event on socket
                socket.on(RCV_DOC, new OnRcvDocEmitListener());
                socket.on(ON_ATTACHMENT_URL, new onAttachmentUrlEmitListener());
//            socket.on("attachment_data_start", new onAttachmentDataStartEmitListener());
//            socket.on("attachment_data_chunk", new onAttachmentDataChunkEmitListener());
//            socket.on("attachment_data_end", new onAttachmentDataEndEmitListener());
//            socket.on("attachment_stream", new onAttachmentStreamEmitListener());
                Log.e(TAG, "onListenerRunnable: Listening on " + RCV_DOC + " : " + ON_ATTACHMENT_URL + ".... " + url);
            } catch (Throwable ignored) {
                Log.e(TAG, ignored.getMessage());
            }
        }
    }

    private class JoinAck implements Ack {
        Message messg = null;

        public JoinAck() {
        }

        public JoinAck(Message msg) {
            messg = msg;
        }

        @Override
        public void call(Object... objects) {
            if (messg != null && messg.obj != null)
                serviceHandler.sendMessage(messg);
/*
            if (old != null) {
                Message msg = serviceHandler.obtainMessage();
                if (old != msg)
                    serviceHandler.sendMessage(old);
            }
*/
            String result = Arrays.toString(objects);
            Log.e(TAG,
                    "ackJoinUidEmit:Callback from 'join_uid' with results....." + result);
            try {
                JSONArray jsonArray = new JSONArray(result);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                int status = jsonObject.getInt(STATUS);
                // error
                if (0 == status) {
//                        sets reconnect to true
//                        next attempt must rejoin again
                    reconnect = true;
                }
                if (1 == status) {
                    ref = jsonObject.getString(REF);
                    Log.e(TAG, "RcvDocService Joined Socket with ref: " + ref);
                }
            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }

    }

    private class OnConnectListener implements Emitter.Listener {
        private Message mssg = null;

        public OnConnectListener(Message msg) {
            mssg = msg;
        }

        @Override
        public void call(Object... args1) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
                jsonObject.put(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
                jsonObject.put(MainApplicationSingleton.DEVICE_PARAM, user.getDevice());
                jsonObject.put(MainApplicationSingleton.DEVICE_REF_PARAM, user.getDeviceRef());
                if (null == mssg) {
                    socket.emit(JOIN_UID, jsonObject, new JoinAck());

                } else {
                    socket.emit(JOIN_UID, jsonObject, new JoinAck(mssg));

                }
//                runOnListenerRunnable();
//                socket.on(RCV_DOC, new OnRcvDocEmitListener());
//                socket.on(ON_ATTACHMENT_URL, new onAttachmentUrlEmitListener());
                Log.e(TAG, "OnConnectListener: RcvDocService Emitting to 'join_uid'....." +
                        MainApplicationSingleton.newJSONArray(args1));
            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }
    }

    private class onAttachmentUrlEmitListener implements Emitter.Listener {
        @Override
        public void call(Object... args) {
//            String result = Arrays.toString(args);
//            Log.e(TAG, "Callback from 'attachment_data_start' with results....." + result);
/*
[{"filename":"intellibitz-logo.png","encoding":"BASE64","content_type":"image\/png","msg_uid":"18",
"attch_id":"2"}]

  {
        "status": 1,
        "err": "",
        "filename": "camels-shadows.jpeg",
        "content_type": "image/jpeg",
        "msg_uid": "103",
        "attch_id": "2",
        "attachment_url": "https://intellibitz-uploads.s3-ap-southeast-1.amazonaws.com/email-attachments/USRMASTER_919840348914_camels-shadows.jpeg"
    }
 */
            try {
                JSONArray jsonArray = MainApplicationSingleton.newJSONArray(args);
                Log.e(TAG, "onAttachmentUrlEmitListener: " + jsonArray);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                MessageItem attachmentItem = new MessageItem();
                attachmentItem.setName(jsonObject.getString("filename"));
//                attachmentItem.setEncoding(jsonObject.getString("encoding"));
                String content_type = jsonObject.getString("content_type");
                String[] types = content_type.split("/");
                attachmentItem.setType(types[0]);
                attachmentItem.setSubType(types[1]);
                attachmentItem.setMsgAttachID(jsonObject.getString("msg_uid"));
                attachmentItem.setPartID(jsonObject.getString("attch_id"));
                String key = attachmentItem.getMsgAttachID() + attachmentItem.getPartID() +
                        attachmentItem.getName();
                String url = jsonObject.getString("attachment_url");
                File file = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key);
                attachmentItem.setDownloadURL(file.getAbsolutePath());
/*
                MainApplicationSingleton.downloadS3Url(
                        url, file, attachmentItem, getApplicationContext());
*/
                HttpUrlConnectionParser.downloadURLToFile(url, file);
                MessageAttachmentIntentService.asyncUpdateEmailAttachmentFilePathInDB(
                        getApplicationContext(), attachmentItem);

            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }
    }

    class OnRcvDocEmitListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            String incomingMsg = Arrays.toString(args1);
//            // TODO: 28-06-2016
//            make this single threaded ??
            synchronized (lock) {
                if (!MainApplicationSingleton.isJSONValid(incomingMsg)) {
                    Log.e(TAG, "INVALID JSON: " + incomingMsg);
                    return;
                }
            }
            Log.e(TAG, "RcvDocService Listening on 'rcv_doc'....." + incomingMsg);
            try {
                JSONArray jsonArray = new JSONArray(incomingMsg);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String docType = jsonObject.getString("doc_type");
//                handles all message
                if (CONTACT.equalsIgnoreCase(docType)) {
                    Log.e(TAG, CONTACT + ":" + jsonObject);
                    rcvDocContact(jsonObject);
                } else if (MSG.equalsIgnoreCase(docType)) {
                    Log.e(TAG, MSG + ":" + jsonObject);
                    rcvDocMessage(jsonObject, user);
                } else if (MSG_INFO.equalsIgnoreCase(docType)) {
                    Log.e(TAG, MSG_INFO + ":" + jsonObject);
                    rcvDocMessageInfo(jsonObject);
                } else if (GROUP.equalsIgnoreCase(docType)) {
                    Log.e(TAG, GROUP + ":" + jsonObject);
//                    rcvDocGroup(jsonObject);
                } else if (GROUP_INFO.equalsIgnoreCase(docType)) {
                    Log.e(TAG, GROUP_INFO + ":" + jsonObject);
//                    rcvDocGroupInfo(jsonObject);
                } else if (BROADCAST.equalsIgnoreCase(docType)) {
                    Log.e(TAG, BROADCAST + ":" + jsonObject);
                } else if (QUEUE_NOTIFY.equalsIgnoreCase(docType)) {
                    Log.e(TAG, QUEUE_NOTIFY + ":" + jsonObject);
                } else if ("THREAD".equals(docType)) {
//                    message thread concepts.. goes away
//                    rcvDocMessageThread(jsonObject);
                }
            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }

    }

    class OnJoinedListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            try {
//                runOnListenerRunnable();
                Log.e(TAG, "OnJoinedListener....." + MainApplicationSingleton.newJSONArray(args1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class OnForceLogoutListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            try {
                Log.e(TAG, "OnForceLogoutListener....." + MainApplicationSingleton.newJSONArray(args1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class OnDisconnectListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            try {
                Log.e(TAG, "OnDisconnectListener: ....." + MainApplicationSingleton.newJSONArray(args1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class OnErrorListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            try {
                Log.e(TAG, "OnErrorListener: ....." + MainApplicationSingleton.newJSONArray(args1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    class OnMessageListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            try {
                Log.e(TAG, "OnMessageListener: ....." + MainApplicationSingleton.newJSONArray(args1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    class onAttachmentDataStartEmitListener implements Emitter.Listener {
        @Override
        public void call(Object... args) {
//            String result = Arrays.toString(args);
//            Log.e(TAG, "Callback from 'attachment_data_start' with results....." + result);
/*
[{"filename":"intellibitz-logo.png","encoding":"BASE64","content_type":"image\/png","msg_uid":"18",
"attch_id":"2"}]
 */
            try {
                JSONArray jsonArray = MainApplicationSingleton.newJSONArray(args);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String filename = jsonObject.getString("filename");
                String encoding = jsonObject.getString("encoding");
                String content_type = jsonObject.getString("content_type");
                String msg_uid = jsonObject.getString("msg_uid");
                String attach_id = jsonObject.getString("attch_id");
                String key = msg_uid + attach_id + filename;
                MainApplicationSingleton singleton =
                        MainApplicationSingleton.getInstance(getApplicationContext());
                singleton.initGlobalSB(key);
            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }
    }

    class onAttachmentDataChunkEmitListener implements Emitter.Listener {
        @Override
        public void call(Object... args) {
//            String result = Arrays.toString(args);
//            Log.e(TAG, "Callback from 'attachment_data_chunk' with results....." + result);
/*
[{"filename":"intellibitz-logo.png","encoding":"BASE64","content_type":"image\/png","msg_uid":"18",
"attch_id":"2","chunk":"iV"}]
             */
            try {
                JSONArray jsonArray = MainApplicationSingleton.newJSONArray(args);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String filename = jsonObject.getString("filename");
                String encoding = jsonObject.getString("encoding");
                String content_type = jsonObject.getString("content_type");
                String msg_uid = jsonObject.getString("msg_uid");
                String attach_id = jsonObject.getString("attch_id");
                String chunk = jsonObject.getString("chunk");
                String key = msg_uid + attach_id + filename;
                MainApplicationSingleton singleton =
                        MainApplicationSingleton.getInstance(getApplicationContext());
                singleton.appendValToGlobalSB(key, chunk);
            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }
    }

    class onAttachmentDataEndEmitListener implements Emitter.Listener {
        @Override
        public void call(Object... args) {
//            String result = Arrays.toString(args);
//            Log.e(TAG, "Callback from 'attachment_data_end' with results....." + result);
/*
[{"filename":"intellibitz-logo.png","encoding":"BASE64","content_type":"image\/png","msg_uid":"18",
"attch_id":"2"}]

             */
            try {
                JSONArray jsonArray = MainApplicationSingleton.newJSONArray(args);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                MessageItem attachmentItem = new MessageItem();
                attachmentItem.setName(jsonObject.getString("filename"));
                attachmentItem.setEncoding(jsonObject.getString("encoding"));
                String content_type = jsonObject.getString("content_type");
                String[] types = content_type.split("/");
                attachmentItem.setType(types[0]);
                attachmentItem.setSubType(types[1]);
                attachmentItem.setMsgAttachID(jsonObject.getString("msg_uid"));
                attachmentItem.setPartID(jsonObject.getString("attch_id"));
                String key = attachmentItem.getMsgAttachID() + attachmentItem.getPartID() +
                        attachmentItem.getName();
                MainApplicationSingleton singleton =
                        MainApplicationSingleton.getInstance(getApplicationContext());
//                resets, once we have the full file
//                ArrayList<String> fileBase64 = singleton.removeGlobalSBAsList(key);
                String fileBase64 = singleton.removeGlobalSB(key);
//                decodes base64 encoded string
//                        Log.d(TAG, fileBase64);
                if (!fileBase64.isEmpty()) {
//                decodes string to byte[]
                    try {
                        String out = "";
                        if ("BASE64".equals(attachmentItem.getEncoding())) {
                            out = MainApplicationSingleton.decodeBase64String(fileBase64);
                        } else {
                            out = fileBase64;
/*
                            for (String val : fileBase64)
                                out += val;
*/
                        }
                        File file = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key);
                        MainApplicationSingleton.writeStringToFile(out, file);
                        Log.d(TAG, "attachment file: " + file.getAbsolutePath());
                        attachmentItem.setDownloadURL(file.getAbsolutePath());
//                saves in db
                        MessageAttachmentIntentService.asyncUpdateEmailAttachmentFilePathInDB(
                                RcvDocService.this, attachmentItem);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            } catch (Throwable ignored) {
                ignored.printStackTrace();
                Log.e(TAG, ignored.getMessage());
            }
        }
    }

    class onAttachmentStreamEmitListener implements Emitter.Listener {
        @Override
        public void call(Object... args) {
            String result = Arrays.toString(args);
            Log.e(TAG, "Callback from 'attachment_stream' with results....." + result);
/*
[{"filename":"intellibitz-logo.png","encoding":"BASE64","content_type":"image\/png","msg_uid":"18",
"attch_id":"2"}]

             */
        }
    }

/*
    private Uri saveMessageThreadEmailItem(MessageItem item, EmailItem em) throws IOException {
        Uri uri;
        ContentValues emailContentValues = new ContentValues();
        emailContentValues.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(item));
        emailContentValues.put("EmailItem", MainApplicationSingleton.Serializer.serialize(em));
        uri = getContentResolver().insert(
                MessageThreadEmailContentProvider.CONTENT_URI, emailContentValues);
        Log.e(TAG, "RcvDocService Message Thread Email saved: " + uri);
        return uri;
    }
*/

}
