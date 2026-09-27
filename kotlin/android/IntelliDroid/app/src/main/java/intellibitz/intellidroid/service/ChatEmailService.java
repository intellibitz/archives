package intellibitz.intellidroid.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MsgChatContactsContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//import intellibitz.intellidroid.MainActivity;

public class ChatEmailService extends Service {
    public static final String RCV_DOC = "rcv_doc";
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
    public static final String ACK_DOC = "ack_doc";
    public static final String MSG_ID = "msg_id";
    public static final String DOC_ID = "doc_id";
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
    private static final String TAG = "ChatEmailService";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
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
    Ack ackEmitSendMsgListener = new Ack() {
        @Override
        public void call(Object... objects1) {
            Log.e(TAG,
                    "ChatEmailService Callback from 'send_msg' with results....." +
                            Arrays.toString(objects1));
        }
    };
    Ack ackEmitAckDocListner = new Ack() {
        @Override
        public void call(Object... objects) {
            Log.e(TAG, "ChatEmailService Callback from 'act_msg' with results....." + Arrays.toString(objects));
        }
    };
    private NotificationManager mNM;
    private boolean reconnect = false;
    private String ref;
    private ContactItem user;
    private String url;
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
    public ChatEmailService() {
        super();
    }

    private void sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null);
    }

    // Binder given to clients
//    private final IBinder mBinder = new LocalBinder();

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

    @Override
    public void onDestroy() {
        if (null != socket) {
            disconnectFromSocket();
        }
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
    }

    private void connectToSocket() {
        runOnListenerRunnable();
        socket.connect();
    }

    private void disconnectFromSocket() {
        socket.off(Socket.EVENT_CONNECT);
        socket.off(Socket.EVENT_ERROR);
        socket.off(Socket.EVENT_DISCONNECT);

        mOnListenerRunnable = null;

        socket.off(RCV_DOC);
//        socket.off(RCV_MSG);
//        socket.off("show_typing");
        socket.off("test2");
        socket.disconnect();
        Log.e(TAG, "ChatEmailService Socket Disconnected onto ....." + url);
    }

    private void runOnListenerRunnable() {
        if (null == mOnListenerRunnable) {
            mOnListenerRunnable = new onListenerRunnable();
        }
        mOnListenerRunnable.run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        if (null != intent) {
            createSocket(intent);
/*
            reconnect = intent.getBooleanExtra("reconnect", false);
            user = intent.getParcelableExtra(ContactItem.TAG);
*/
        }
        if (reconnect) {
            reconnectSocket();
        }
        return result;
    }

    public void createSocket(Intent intent) {
        url = intent.getStringExtra("url");
        reconnect = intent.getBooleanExtra("reconnect", false);
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
        try {
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
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

    private void emitToSendMessage(Message msg) {
        String val = (String) msg.obj;
        Bundle args = msg.getData();
        String id = args.getString("id");
        String sub = args.getString("topic");
        String docOwnerEmail = args.getString("docOwnerEmail");
        try {
            emitToSendEmailMessage(id, sub, docOwnerEmail, val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitToIsTyping() {
        try {
            emitToIsTyping(GROUP, AKTPROTOTYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitToSendEmailMessage(String id, String topic, String docOwnerEmail, String val) throws JSONException {
//        Cursor cursor = getMessageThreadItemCursor(id);
//        Cursor c = getMessageThreadEmailsCursor(id);
        Cursor c = getMessageThreadMsgContactsCursor(id);
//        // TODO: 05-03-2016
//        to get the active email used now
        String from = docOwnerEmail;
        if (null == from) {
//        // TODO: 05-03-2016
//        this can never happen.. inform user if email null
            from = MainApplicationSingleton.DUMMY_EMAIL;
        }
        String to = "";
        String cc = "";
        String bcc = "";
        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                ContactItem item = new ContactItem();
                item.setTypeId(c.getString(c.getColumnIndex(
                        ContactItemColumns.KEY_TYPE_ID)));
                item.setName(c.getString(c.getColumnIndex(
                        ContactItemColumns.KEY_NAME)));
                item.setType(c.getString(c.getColumnIndex(
                        ContactItemColumns.KEY_TYPE)));
                String email = item.getTypeId();
                if ("to".equals(item.getType()) && !from.equals(email)) {
                    if (!"".equals(to) && !to.contains(email)) {
                        to += ",";
                    }
                    if (!to.contains(email)) {
                        to += email;
                    }
                } else if ("from".equals(item.getType()) && !from.equals(email)) {
                    if (!"".equals(to) && !to.contains(email)) {
                        to += ",";
                    }
                    if (!to.contains(email)) {
                        to += email;
                    }
                } else if ("cc".equals(item.getType())) {
                    if (!"".equals(cc) && !cc.contains(email)) {
                        cc += ",";
                    }
                    if (!cc.contains(email)) {
                        cc += email;
                    }
                } else if ("bcc".equals(item.getType())) {
                    if (!"".equals(bcc) && !bcc.contains(email)) {
                        bcc += ",";
                    }
                    if (!bcc.contains(email)) {
                        bcc += email;
                    }
                }
            } while (c.moveToNext());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg_type", "EMAIL");
        jsonObject.put("from", from);
        jsonObject.put("to", to);
//        if (!cc.isEmpty())
        jsonObject.put("cc", cc);
//        if (!bcc.isEmpty())
        jsonObject.put("bcc", bcc);
        jsonObject.put("subject", topic);
        jsonObject.put(TXT, val);
        Log.e(TAG, "ChatEmailService Emitting to 'send_msg'....." + jsonObject);
        socket.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener);
    }

    private void emitToSendMessage(String text, String type, String uid) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TXT, text);
        jsonObject.put(TO_TYPE, type);
        jsonObject.put(TO_UID, uid);
        Log.e(TAG, "ChatEmailService Emitting to 'send_msg'....." + jsonObject);
        socket.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener);
    }

    private void emitToIsTyping(String type, String uid) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TO_TYPE, type);
        jsonObject.put(TO_UID, uid);
//        Log.e(TAG, "ChatEmailService Emitting to 'is_typing'....." + jsonObject);
        socket.emit(IS_TYPING, jsonObject);
    }

    private Cursor getMessageThreadItemCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MessageChatContentProvider.CONTENT_URI, Uri.encode(id));
        return getContentResolver().query(uri,
                new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_DATA_ID + " = ?",
                new String[]{id}, null);
    }

    private Cursor getMessageThreadMsgContactsCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MsgChatContactsContentProvider.CONTENT_URI,
                Uri.encode(id));
        return getContentResolver().query(uri, null, null, null, null);

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
                MainApplicationSingleton.PI_CHATTYMAIL_RQ_CODE,
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
                MainApplicationSingleton.PI_CHATTYMAIL_RQ_CODE,
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

/*
    private Cursor getMessageThreadEmailsCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MessageThreadEmailContentProvider.CONTENT_URI,
                Uri.encode(id));
        return getContentResolver().query(uri, null, null, null, null);

    }
*/

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
                        emitToSendMessage(msg);
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
                    emitToIsTyping();
                    break;
                default:
                    super.handleMessage(msg);
            }
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
        Log.e(TAG, "ChatEmailService Message Thread Email saved: " + uri);
        return uri;
    }

    private Uri saveMessageThreadItem(MessageItem item) throws IOException {
        ContentValues msgThreadContentValues = new ContentValues();
        msgThreadContentValues.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(item));
        Uri uri = getContentResolver().insert(MessagesContentProvider.CONTENT_URI,
                msgThreadContentValues);
        Log.e(TAG, "ChatEmailService Message Thread saved: " + uri);
        return uri;
    }

    private void ackRcvDoc(MessageItem item) throws JSONException {
//                    sendMessageToClients();
//                getContentResolver().notifyChange(ChatMessageContentProvider.CONTENT_URI, );
//                showNotification(msg);
        String msgId = item.getDataId();
//                String toId = jsonObject.getString(TO_UID);
        JSONObject payload = new JSONObject();
        payload.put(DOC_ID, msgId);
//                payload.put(TO_UID, toId);
//                // TODO: 05-03-2016
//                acknowledge doc
        socket.emit(ACK_DOC, payload, ackEmitAckDocListner);

    }
*/

    class onListenerRunnable implements Runnable {
        @Override
        public void run() {
//            // TODO: 12-02-2016
//            effective thread management strategies from the platform, to investigate
//            run forever
//            while (true){
//            Listens to CONNECT event on socket
//            socket.on(RCV_MSG, new OnReceiveMessageListener());
//            socket.on(RCV_DOC, new OnRcvDocEmitListener());
            socket.on(Socket.EVENT_CONNECT, new OnConnectListener());
            socket.on(Socket.EVENT_ERROR, new OnErrorListener());
            socket.on(Socket.EVENT_DISCONNECT, new OnDisconnectListener());
            Log.e(TAG, "ChatEmailService Socket onto ....." + url);
        }
    }

    class OnConnectListener implements Emitter.Listener {
        Ack ackEmitJoinUidCallbackListener = new Ack() {
            @Override
            public void call(Object... objects) {
                String result = Arrays.toString(objects);
                Log.e(TAG,
                        "ChatEmailService Callback from 'join_uid' with results....." + result);
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
                        Log.e(TAG, "ChatEmailService Joined Socket with ref: " + ref);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        };

        @Override
        public void call(Object... args1) {
            socket.emit(MY_OTHER_EVENT, "968768");
            socket.emit(TEST_1, "968768");
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
                jsonObject.put(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
                jsonObject.put(MainApplicationSingleton.DEVICE_PARAM, user.getDevice());
                jsonObject.put(MainApplicationSingleton.DEVICE_REF_PARAM, user.getDeviceRef());
                Log.e(TAG, "ChatEmailService Emitting to 'join_uid'....." + jsonObject);
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
//            Log.e(TAG, "ChatEmailService Listening on 'show_typing'....." + json);
            sendShowTypingToClients(json);
            //socket.disconnect();
        }
    }

    class OnReceiveMessageListener implements Emitter.Listener {
        Ack ackEmitAckMsgListner = new Ack() {
            @Override
            public void call(Object... objects) {
                Log.e(TAG, "ChatEmailService Callback from 'act_msg' with results....." + Arrays.toString(objects));
            }
        };

        @Override
        public void call(Object... args1) {
            String incomingMsg = Arrays.toString(args1);
            Log.e(TAG, "ChatEmailService Listening on 'rcv_message'....." + incomingMsg);
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
                    Log.e(TAG, "ChatEmailService self message - not accepting from cloud");
                } else {
//                    // TODO: 09-03-2016
//                    Use the correct content provider
                    getContentResolver().insert(MessageChatContentProvider.CONTENT_URI, contentValues);
                    sendMessageToClients();
//                getContentResolver().notifyChange(ChatMessageContentProvider.CONTENT_URI, );
//                    showNotification(msg);
                    String msgId = jsonObject.getString("_id");
                    String toId = jsonObject.getString(TO_UID);
                    JSONObject payload = new JSONObject();
                    payload.put(MSG_ID, msgId);
                    payload.put(TO_UID, toId);
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
            Log.e(TAG, "ChatEmailService Listening on 'test2'....." + Arrays.toString(args1));
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }

    class OnDisconnectListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            Log.e(TAG, "ChatEmailService Disconnect.....");
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }

    class OnErrorListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            Log.e(TAG, "ChatEmailService Error.....");
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }
}
