package intellibitz.intellidroid.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.WorkerThread;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatService extends Service {
    public static final String RCV_MSG = "rcv_msg";
    public static final String SHOW_TYPING = "show_typing";
    public static final String TEST_2 = "test2";
    public static final String IS_TYPING = "is_typing";
    public static final String GROUP = "group";
    public static final String AKTPROTOTYPE = "aktprototype";
    public static final String TO_UID = "to_uid";
    public static final String TO_TYPE = "to_type";
    public static final String TXT = "txt";
    public static final String FROM_UID = "from_uid";
    public static final String FROM_NAME = "from_name";
    public static final String ACK_MSG = "ack_msg";
    public static final String MSG_ID = "msg_id";
    public static final String SEND_MSG = "send_msg";
    public static final String JOIN_UID = "join_uid";
    public static final String UID = "uid";
    public static final String TOKEN = "token";
    public static final String DEVICE = "device";
    public static final String TEST_1 = "test1";
    public static final String MY_OTHER_EVENT = "my other event";
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
    public static final int MSG_ON_KEY = 4;
    //    on show typing from cloud
    public static final int MSG_SHOW_TYPING = 5;
    public static final String INTENT_ACTION_NEW_CHAT_MESSAGE = "NEW_CHAT_MESSAGE";
    private static final String TAG = "ChatService";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final Object lock = new Object();
    Ack ackEmitSendMsgListener = new Ack() {
        @Override
        public void call(Object... objects1) {
            Log.d(TAG,
                    "ChatService Callback from 'send_msg' with results....." +
                            Arrays.toString(objects1));
        }
    };
    private ContactItem user;
    // write your code here
    private Socket socket;
    private onListenerRunnable mOnListenerRunnable;
    private NotificationManager mNM;
    /**
     * Keeps track of all current registered clients.
     */
    private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /**
     * Holds last value set by a client.
     */
    private int mValue = 0;
    private boolean reconnect = false;
    private String ref;
    private String url;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 100;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;

    // Binder given to clients
//    private final IBinder mBinder = new LocalBinder();
    private boolean mRedelivery;

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
    public ChatService() {
        super();
    }

    /**
     * Creates an EmailService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ChatService(String name) {
        super();
        mName = name;
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    private void sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null);
    }

    private void sendShowTypingToClients(Object s) {
        sendMessageToClients(MSG_SHOW_TYPING, s);
    }

    private void sendMessageToClients(int msg, Object object) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message message = Message.obtain(null, msg, mValue, 0);
                message.obj = object;
                mClients.get(i).send(message);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Sets intent redelivery preferences.  Usually called from the constructor
     * with your preferred semantics.
     * <p/>
     * <p>If enabled is true,
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
     * {@link #onHandleIntent(Intent)} returns, the process will be restarted
     * and the intent redelivered.  If multiple Intents have been sent, only
     * the most recent one is guaranteed to be redelivered.
     * <p/>
     * <p>If enabled is false (the default),
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent
     * dies along with it.
     */
    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            createSocket(intent);
//            user = intent.getParcelableExtra(ContactItem.TAG);
//            reconnect = intent.getBooleanExtra("reconnect", false);

            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent;
            mServiceHandler.sendMessage(msg);
        }
        if (reconnect) {
            reconnectSocket();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void createSocket(Intent intent) {
        url = intent.getStringExtra("url");
        reconnect = intent.getBooleanExtra("reconnect", false);
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
        createSocket();
    }

    public void createSocket() {
        try {
            createsIOSocket();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.getMessage());
        }
    }

    public void createsIOSocket() throws URISyntaxException {
        if (null == url) {
            Log.e(TAG, "SOCKET url is NULL: ");
        } else {
//                socket = IO.socket(MainApplicationSingleton.SOCKET_HOST);
            socket = IO.socket(url);
//            try reconnecting, with start.. if user is null
            if (null == user) {
                reconnect = true;
            } else {
//                user is available, safe to connect
//                reconnectSocket();
            }
        }
    }


    @Override
    public void onDestroy() {
        if (null != socket) {
            disconnectFromSocket();
        }
        // TODO: 16-03-2016
//        quit looper as soon as the handle event is over.. in the handler
        mServiceLooper.quit();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
/*
        try {
            socket = IO.socket(MainApplicationSingleton.SOCKET_HOST);
//            try reconnecting, with start.. if user is null
            if (null == user) {
                reconnect = true;
            } else {
//                user is available, safe to connect
//                reconnectSocket();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
*/
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
//        showNotification();
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    private void connectToSocket() {
        if (null == socket) createSocket();
        socket.on(Socket.EVENT_CONNECT, new OnConnectListener());
        socket.on(Socket.EVENT_ERROR, new OnErrorListener());
        socket.on(Socket.EVENT_DISCONNECT, new OnDisconnectListener());

        runOnListenerRunnable();
        socket.connect();
        Log.d(TAG, "ChatService Socket connected onto ....." + url);
    }

    private void disconnectFromSocket() {
        if (socket != null) {
            socket.off(Socket.EVENT_CONNECT);
            socket.off(Socket.EVENT_ERROR);
            socket.off(Socket.EVENT_DISCONNECT);

            socket.off(SHOW_TYPING);
            socket.disconnect();
        }

        mOnListenerRunnable = null;

        Log.d(TAG, "ChatService Socket Disconnected onto ....." + url);
    }

    private void runOnListenerRunnable() {
        if (null == mOnListenerRunnable) {
            mOnListenerRunnable = new onListenerRunnable();
        }
        mOnListenerRunnable.run();
    }

    private void reconnectSocket() {
        if (user != null) {
//        sets reconnect to false..
//        if the callback to join fails, then reconnect will be set to true in the callback
            reconnect = false;
            disconnectFromSocket();
            connectToSocket();
        }
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
     *               android.content.Context#startService(Intent)}.
     */
    @WorkerThread
    private void onHandleIntent(Intent intent) {
        if (INTENT_ACTION_NEW_CHAT_MESSAGE.equals(intent.getAction())) {
            ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            MessageItem messageItem = intent.getParcelableExtra(
                    MessageItem.TAG);
            if (null == messageItem) {
                Log.e(TAG, " onHandleIntent: MessageItem is NULL in intent extras");
            } else {
                emitsChatMessage(messageItem, user);
            }
        }
    }

    private void addAfterJoinListenersToSocket() {

    }

    private void emitSendMessage(Message msg) {
        Bundle args = msg.getData();
        final MessageItem messageItem =
                args.getParcelable(MessageItem.TAG);
        final ContactItem user = args.getParcelable(ContactItem.USER_CONTACT);
        performOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                emitsChatMessage(messageItem, user);
            }
        });
    }

    private void emitToSendMessage(Message msg) {
        String val = (String) msg.obj;
        Bundle args = msg.getData();
        String id = args.getString("id");
        String sub = args.getString("topic");
        String name = args.getString("fromName");
        try {
            emitToSendChatMessage(id, sub, name, val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
/*
        String val = (String) msg.obj;
        try {
            emitSendMessage(val, GROUP, AKTPROTOTYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/

    }

    private void emitToIsTyping(Message msg) {
        try {
            Bundle args = msg.getData();
            final MessageItem messageItem =
                    args.getParcelable(MessageItem.TAG);
            final ContactItem user = args.getParcelable(ContactItem.USER_CONTACT);
            if (messageItem != null) {
//                emitToIsTyping(GROUP, messageItem.getChatId());
                emitToIsTyping(messageItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitToIsTyping(MessageItem messageItem) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        final String chatId = messageItem.getChatId();
        if (TextUtils.isEmpty(chatId)) {
            Log.e(TAG, "emitToIsTyping: chat id is NULL");
            return;
        }
        String toType = messageItem.getToType();
        if (TextUtils.isEmpty(toType)) {
            Log.e(TAG, "emitToIsTyping: to type is NULL.. fallback to 'USER'");
        }
//        ContactItem contactItem = messageItem.getContactItem();
        if (null == toType) {
//            if (contactItem.isGroup()) toType = "GROUP";
            toType = "USER";
        }
        jsonObject.put(TO_TYPE, toType);
        jsonObject.put(TO_UID, chatId);
//        Log.d(TAG, "ChatService Emitting to 'is_typing'....." + jsonObject);
//        // TODO: 19-06-2016
//        to check if socket is null, and create new socket
        if (null == socket) reconnectSocket();
        if (null == socket) {
            Log.e(TAG, "Socket is NULL.. cannot emit typing");
            return;
        }
        socket.emit(IS_TYPING, jsonObject);
    }

    private void emitToIsTyping(String type, String uid) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TO_TYPE, type);
        jsonObject.put(TO_UID, uid);
//        Log.d(TAG, "ChatService Emitting to 'is_typing'....." + jsonObject);
//        // TODO: 19-06-2016
//        to check if socket is null, and create new socket
        socket.emit(IS_TYPING, jsonObject);
    }

    private void emitToSendChatMessage(String id, String topic, String fromName, String val) throws JSONException {
//        Cursor cursor = getMessageThreadItemCursor(id);
//        Cursor c = getMessageThreadEmailsCursor(id);
//        // TODO: 05-03-2016
//        to get the active email used now
        if (null == fromName) {
//            not supposed to happen
            fromName = "DEMO USER";
        }
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("doc_type", "MSG");
//        jsonObject.put("doc_owner", "intellibitz");
        jsonObject.put("msg_type", "CHAT");
//        jsonObject.put("to_type", "group");
//        jsonObject.put("from_uid", id);
        jsonObject.put("to_uid", id);
//        jsonObject.put("from_name", fromName);
//        jsonObject.put("subject", topic);
        jsonObject.put(TXT, val);
        Log.e(TAG, "ChatService Emitting to 'send_msg'....." + jsonObject);
        if (null == socket) reconnectSocket();
        if (null == socket) {
            Log.e(TAG, "Socket is NULL.. cannot emit typing");
            return;
        }
        socket.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener);
    }

    private void emitsChatMessage(MessageItem messageThreadItem, ContactItem user) {
//        // TODO: 13-03-2016
//        get the message thread item.. and work with that.. avoid going to DB again
//        Cursor cursor = getMessageThreadItemCursor(id);
//        // TODO: 16-03-2016
//        retrieves emails from DB.. for existing message thread item
/*
        if (messageThreadItem.getDataId() != null &&
                !MessageItem.TAG.equals(messageThreadItem.getDataId())) {
            Cursor c = getMessageThreadEmailsCursor(messageThreadItem.getDataId());
            if (c != null && c.getCount() > 0) {
//        // TODO: 05-03-2016
//        to get the active email used now
                if (null == from) {
//            // TODO: 07-03-2016
//            this should not happen...
                    from = MainApplicationSingleton.DUMMY_EMAIL;
                }
                // looping through all rows and adding to list
                if (c.moveToFirst()) {
                    do {
                        EmailItem emailItem = new EmailItem();
                        emailItem.setEmailItem(c.getString(c.getColumnIndex(
                                DatabaseHelper.EmailItemColumns.KEY_SIGNUP_EMAIL)));
                        emailItem.setName(c.getString(c.getColumnIndex(
                                DatabaseHelper.EmailItemColumns.KEY_NAME)));
                        emailItem.setType(c.getString(c.getColumnIndex(
                                DatabaseHelper.EmailItemColumns.KEY_TYPE)));
                        messageThreadItem.addContact(emailItem);
                    } while (c.moveToNext());
                }
                c.close();
            } else {
//            to = messageThreadItem
            }
        }

        Set<EmailItem> emailItems = messageThreadItem.getEmails();
        MessageItem.EmailTags emailTags = new MessageItem.EmailTags(
                from, emailItems).invoke();
        String to = emailTags.getTo();
        String cc = emailTags.getCc();
        String bcc = emailTags.getBcc();
*/

        ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
                messageThreadItem.getMessageItemConcurrentLinkedQueue();
        MessageItem[] messageItems;
        synchronized (lock) {
            messageItems = messageItemConcurrentLinkedQueue.toArray(new MessageItem[0]);
        }
//        first loop, send to cloud
        for (MessageItem messageItem : messageItems) {
            try {
                if (messageItem.isReadyToSend()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("msg_type", "CHAT");
                    jsonObject.put("to_uid", messageThreadItem.getChatId());
                    jsonObject.put("client_msg_ref", messageItem.getChatMsgRef());
                    jsonObject.put(TXT, messageItem.getText());
                    String uid = user.getDataId();
                    String token = user.getToken();
                    String device = user.getDevice();
                    Set<MessageItem> items = messageItem.getAttachments();
                    if (items != null && !items.isEmpty()) {
                        JSONArray attachments = new JSONArray();
                        for (MessageItem item : items) {
                            try {
                                JSONObject attachmentDetails = uploadAttachments(item,
                                        MainApplicationSingleton.ATTACHMENT_UPLOAD_URL,
                                        uid, device, user.getDeviceRef(), token);
                                attachments.put(attachmentDetails);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        jsonObject.put("attachments", attachments);
                    }
                    Log.e(TAG, "ChatService Emitting to 'send_msg'....." + jsonObject);
                    if (null == socket) reconnectSocket();
                    if (null == socket) {
                        Log.e(TAG, "Socket is NULL.. cannot emit typing");
                        return;
                    }
                    socket.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        2nd loop, clears send messages
//        clears of all messages, send
        for (MessageItem messageItem : messageItems) {
            if (messageItem.isReadyToSend()) {
                messageItemConcurrentLinkedQueue.remove(messageItem);
            }
        }
    }

    protected JSONObject uploadAttachments(MessageItem item, String url, String uid, String device,
                                           String deviceRef, String token) throws IOException {
        try {
            String charset = "UTF-8";
            HttpUrlConnectionParser.MultipartUtility multipart =
                    new HttpUrlConnectionParser.MultipartUtility(url, charset);
            multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device);
            multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid);
            multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token);
            File file = new File(item.getDescription());
            multipart.addFilePart(MainApplicationSingleton.ATTACH_FILE_PARAM, file,
                    file.getAbsolutePath());
            // params comes from the execute() call: params[0] is the url.
            JSONObject response = multipart.finishAsJSON(); // response from server.

            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (99 == status) {
                Log.e(TAG, "Attachments Upload ERROR - " + response);
            } else if (1 == status) {
//                    SUCCESS
                MsgChatAttachmentContentProvider.setAttachmentItemFromJson(item, response);
            }
            return response;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String uploadAttachments(File file, String url, String uid, String device,
                                       String deviceRef, String token) throws IOException {
        String charset = "UTF-8";
        String result = null;
        try {
            HttpUrlConnectionParser.MultipartUtility multipart =
                    new HttpUrlConnectionParser.MultipartUtility(url, charset);
            multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device);
            multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid);
            multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token);
            multipart.addFilePart(MainApplicationSingleton.ATTACH_FILE_PARAM, file,
                    file.getAbsolutePath());
            // params comes from the execute() call: params[0] is the url.
            JSONObject response = multipart.finishAsJSON(); // response from server.

            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (99 == status) {
                Log.e(TAG, "Attachments Upload ERROR - " + response);
            } else if (1 == status) {
//                    SUCCESS
                result = response.getString("url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
                MainApplicationSingleton.PI_CHAT_RQ_CODE,
                new Intent(this, MainActivity.class), 0);

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
                MainApplicationSingleton.PI_CHAT_RQ_CODE,
                new Intent(this, MainActivity.class), 0);

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
                    mValue = msg.arg1;
                    if (null != msg.obj) {
//                    // TODO: 18-02-2016
//                    emit to socket
//                    sendMessageToClients();
                        if (reconnect) {
                            reconnectSocket();
                        }
                        emitSendMessage(msg);
                    }
                    break;
                case MSG_ON_KEY:
                    mValue = msg.arg1;
//                    // TODO: 18-02-2016
//                    emit to socket
//                    sendMessageToClients();
                    if (reconnect) {
                        reconnectSocket();
                    }
                    emitToIsTyping(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
//            stopSelf(msg.arg1);
//            mServiceLooper.quit();

        }
    }

    class onListenerRunnable implements Runnable {
        @Override
        public void run() {
            socket.on(SHOW_TYPING, new OnShowTypingListener());
        }
    }

/*
    private void emitSendMessage(String text, String type, String uid) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg_type", "CHAT");
        jsonObject.put(TXT, text);
        jsonObject.put(TO_TYPE, type);
        jsonObject.put(TO_UID, uid);
        Log.d(TAG, "ChatService Emitting to 'send_msg'....." + jsonObject);
        socket.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener);
    }
*/

/*
    private Cursor getMessageThreadEmailsCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MessageThreadEmailContentProvider.CONTENT_URI,
                Uri.encode(id));
        return getContentResolver().query(uri, null, null, null, null);

    }
*/

    class onReceiveMessageRunnable implements Runnable {
        @Override
        public void run() {
//            // TODO: 12-02-2016
//            effective thread management strategies from the platform, to investigate
//            run forever
//            while (true){
            socket.on(RCV_MSG, new OnReceiveMessageListener());
//                Thread.yield();
//            }
        }
    }

    class OnConnectListener implements Emitter.Listener {
        Ack ackEmitJoinUidCallbackListener = new Ack() {
            @Override
            public void call(Object... objects) {
                String result = Arrays.toString(objects);
                Log.d(TAG,
                        "ChatService Callback from 'join_uid' with results....." + result);
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
                        Log.e(TAG, "Joined Socket with ref: " + ref);
                        addAfterJoinListenersToSocket();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };

        @Override
        public void call(Object... args1) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
                jsonObject.put(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
                jsonObject.put(MainApplicationSingleton.DEVICE_PARAM, user.getDevice());
                jsonObject.put(MainApplicationSingleton.DEVICE_REF_PARAM, user.getDeviceRef());
                Log.d(TAG, "ChatService Emitting to 'join_uid'....." + jsonObject);
                socket.emit(JOIN_UID, jsonObject, ackEmitJoinUidCallbackListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    class OnShowTypingListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            String json = Arrays.toString(args1);
//            Log.d(TAG, "ChatService Listening on 'show_typing'....." + json);
            sendShowTypingToClients(json);
            //socket.disconnect();
        }
    }

    class OnReceiveMessageListener implements Emitter.Listener {
        Ack ackEmitAckMsgListner = new Ack() {
            @Override
            public void call(Object... objects) {
                Log.d(TAG, "ChatService Callback from 'act_msg' with results....." + Arrays.toString(objects));
            }
        };

        @Override
        public void call(Object... args1) {
            String incomingMsg = Arrays.toString(args1);
            Log.d(TAG, "ChatService Listening on 'rcv_message'....." + incomingMsg);
            MainApplicationSingleton mainApplication =
                    MainApplicationSingleton.getInstance(getApplicationContext());
            try {
                JSONArray jsonArray = new JSONArray(incomingMsg);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String fromUid = jsonObject.getString(FROM_UID);
                String fromName = jsonObject.getString(FROM_NAME);
                String txt = jsonObject.getString(TXT);
                String msg = fromName + " saying " + txt;
                ContentValues contentValues = new ContentValues();
                contentValues.put(MessageItemColumns.KEY_FROM_NAME, fromName);
                contentValues.put(MessageItemColumns.KEY_TEXT, txt);

                String uid = mainApplication.getStringValueSP(MainApplicationSingleton.UID_PARAM);
                String name = mainApplication.getStringValueSP(MainApplicationSingleton.NAME_PARAM);

                if (uid.equals(fromUid) && name.equals(fromName)) {
                    Log.d(TAG, "ChatService self message - not accepting from cloud");
                } else {
                    getContentResolver().insert(
                            MessageChatContentProvider.CONTENT_URI, contentValues);
                    sendMessageToClients();
//                getContentResolver().notifyChange(ChatMessageContentProvider.CONTENT_URI, );
//                    showNotification(msg);
                    String msgId = jsonObject.getString("_id");
                    String toId = jsonObject.getString(TO_UID);
                    JSONObject payload = new JSONObject();
                    payload.put(MSG_ID, msgId);
                    payload.put(TO_UID, toId);
                    if (null == socket) reconnectSocket();
                    if (null == socket) {
                        Log.e(TAG, "Socket is NULL.. cannot emit typing");
                        return;
                    }
                    socket.emit(ACK_MSG, payload, ackEmitAckMsgListner);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mainApplication.getIncomingQueue().add(incomingMsg);
            //socket.disconnect();
        }


    }

    class OnTest2Listener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            Log.d(TAG, "ChatService Listening on 'test2'....." + Arrays.toString(args1));
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }

    class OnDisconnectListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            Log.d(TAG, "ChatService Disconnect.....");
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }

    class OnErrorListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            Log.d(TAG, "ChatService Error.....");
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }

}
