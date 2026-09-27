package intellibitz.intellidroid.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.task.GetEmailsByTask;
import intellibitz.intellidroid.task.GetFullEmailsTask;
import intellibitz.intellidroid.task.GetMailboxListTask;
import intellibitz.intellidroid.task.GetRecentEmailsTask;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MessagesEmailContentProvider;
import intellibitz.intellidroid.content.MsgEmailContactsContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.task.GetEmailsByTask;
import intellibitz.intellidroid.task.GetFullEmailsTask;
import intellibitz.intellidroid.task.GetMailboxListTask;
import intellibitz.intellidroid.task.GetRecentEmailsTask;
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

//import intellibitz.intellidroid.MainActivity;

public class EmailService extends
        Service implements
        GetRecentEmailsTask.GetRecentEmailsTaskListener,
        GetEmailsByTask.GetEmailsByTaskListener,
        GetFullEmailsTask.GetFullEmailsTaskListener,
        GetMailboxListTask.GetMailboxListTaskListener {
    //        Response.ErrorListener{
    public static final String SEND_EMAIL = "send_email";
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
    public static final String JOIN_UID = "join_uid";
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
    public static final int MSG_ON_KEY = 4;
    //    on show typing from cloud
    public static final int MSG_SHOW_TYPING = 5;
    public static final String INTENT_ACTION_NEW_EMAIL_MESSAGE = "New_Email_Message";
    public static final String ACTION_UPDATE_EMAIL_MESSAGE = "UPDATE_EMAIL_MESSAGE";
    private static final String TAG = "EmailService";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final Object lock = new Object();
    Ack ackEmitSendEmailMsgListener = new Ack() {
        @Override
        public void call(Object... objects1) {
            Log.e(TAG,
                    "EmailService Callback from 'send_email' with results....." +
                            Arrays.toString(objects1));
        }
    };
    private ContactItem user;
    // write your code here
    private Socket socket;
    private NotificationManager mNM;
    /**
     * Keeps track of all current registered clients.
     */
    private ArrayList<Messenger> mClients = new ArrayList<>();
    /**
     * Holds last value set by a client.
     */
    private int mValue = 0;
    private boolean reconnect = false;
    private String ref;
    //    private GetMailboxListTask getMailboxListTask;
//    private GetRecentEmailsTask getRecentEmailsTask;
//    private GetFullEmailsTask getFullEmailsTask;
//    private GetEmailsByTask getEmailsByTask;
    private HandlerThread handlerThread;
    private String url;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 100;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    // Binder given to clients
//    private final IBinder mBinder = new LocalBinder();
    private String mName;
    private boolean mRedelivery;
    private HandlerThread getRecentEmailsLooperThread;
    private HandlerThread updateFullEmailsLooperThread;

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
    public EmailService() {
        super();
    }

    /**
     * Creates an EmailService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public EmailService(String name) {
        super();
        mName = name;
    }

    public static void asyncUpdateGetFullEmails(
            String emailAccount, String[] emailIds, ContactItem user, Context context) {
//        intent service runs async in its own thread
        Intent intent = new Intent(context, EmailService.class);
        intent.setAction(ACTION_UPDATE_EMAIL_MESSAGE);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra("email", emailAccount);
        intent.putExtra("emailIds", emailIds);
        context.startService(intent);
    }

    private void sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null);
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
/*
            user = intent.getParcelableExtra(ContactItem.TAG);
            reconnect = intent.getBooleanExtra("reconnect", false);
*/

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

    @Override
    public void onDestroy() {
        if (null != socket) {
            disconnectFromSocket();
        }
        // TODO: 16-03-2016
//        quit looper as soon as the handle event is over.. in the handler
        mServiceLooper.quit();
        if (handlerThread != null) {
            handlerThread.quit();
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
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    private void connectToSocket() {
        socket.on(Socket.EVENT_CONNECT, new OnConnectListener());
        socket.on(Socket.EVENT_ERROR, new OnErrorListener());
        socket.on(Socket.EVENT_DISCONNECT, new OnDisconnectListener());

        socket.connect();
        Log.e(TAG, "EmailService Socket connected onto ....." + url);
    }

    private void disconnectFromSocket() {
        socket.off(Socket.EVENT_CONNECT);
        socket.off(Socket.EVENT_ERROR);
        socket.off(Socket.EVENT_DISCONNECT);

        socket.disconnect();
        Log.e(TAG, "EmailService Socket Disconnected onto ....." + url);
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
     *               Context#startService(Intent)}.
     */
    @WorkerThread
    private void onHandleIntent(Intent intent) {
        if (INTENT_ACTION_NEW_EMAIL_MESSAGE.equals(intent.getAction())) {
            ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            MessageItem messageItem = intent.getParcelableExtra(
                    MessageItem.TAG);
            if (null == messageItem) {
                Log.e(TAG, " onHandleIntent: MessageItem is NULL in intent extras");
            } else {
                emitToSendEmailMessage(messageItem, user);
            }
        }
        if (ACTION_UPDATE_EMAIL_MESSAGE.equals(intent.getAction())) {
            ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            String email = intent.getStringExtra("email");
            String[] emailIds = intent.getStringArrayExtra("emailIds");

            if (null == user || TextUtils.isEmpty(email) || null == emailIds || 0 == emailIds.length) {
                Log.e(TAG, " onHandleIntent: Extras are null - skipped get full emails");
            } else {
                asyncGetFullEmailsAndUpdate(emailIds, email, user);
            }
        }
//        gets mailbox list of the user email account (inbox, sent, trash et al)
        asyncGetMailboxList(user);
//        checks for recent emails for every start of this service
        asyncGetRecentEmails(user, 0);
//        // TODO: 29/8/16
//        wire up the logic right
//        asyncGetEmailsBy(user);
    }

    private void asyncGetMailboxList(ContactItem user) {
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "asyncGetRecentEmails: email not available - skipping");
            return;
        }
        GetMailboxListTask getMailboxListTask = new GetMailboxListTask(
                MainApplicationSingleton.API_USER, MainApplicationSingleton.API_KEY,
                email, user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_EMAIL_GET_MAILBOX_LIST, this);
        getMailboxListTask.setRequestTimeoutMillis(30000);
        getMailboxListTask.setGetMailboxListTaskListener(this);
        getMailboxListTask.execute();
    }

    private void asyncGetRecentEmails(ContactItem user, int skip) {
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "asyncGetRecentEmails: email not available - skipping");
            return;
        }
        GetRecentEmailsTask getRecentEmailsTask = new GetRecentEmailsTask(email,
                skip, user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(), user,
                MainApplicationSingleton.AUTH_EMAIL_GET_RECENT_EMAILS, this);
        getRecentEmailsTask.setRequestTimeoutMillis(30000);
        getRecentEmailsTask.setGetRecentEmailsTaskListener(this);
//        getRecentEmailsTask.setErrorListener(this);
        getRecentEmailsTask.execute();
    }

    private void asyncGetFullEmailsAndUpdate(String[] emailIds, String email, ContactItem user) {
//        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "onPostGetFullEmailsResponse: email not available - skipping");
            return;
        }
        GetFullEmailsTask getFullEmailsTask = new GetFullEmailsTask(email,
                emailIds, user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(), user,
                MainApplicationSingleton.AUTH_EMAIL_GET_FULL_EMAILS, this, 1);
        getFullEmailsTask.setRequestTimeoutMillis(30000);
        getFullEmailsTask.setGetFullEmailsTaskListener(this);
        getFullEmailsTask.execute();
    }

    private void asyncGetEmailsBy(ContactItem user) {
        GetEmailsByTask getEmailsByTask = new GetEmailsByTask("intellibitztest@gmail.com",
                "muthu@intellibitz.com", user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_GET_EMAILS_BY, this);
        getEmailsByTask.setGetEmailsByTaskListener(this);
        getEmailsByTask.setRequestTimeoutMillis(30000);
        getEmailsByTask.execute();
    }

    @Override
    public void onPostGetRecentEmailsErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetRecentEmailsErrorResponse:" + response);
    }

    @Override
    public void onPostGetRecentEmailsResponse(final JSONObject response, String email, final int skip,
                                              final ContactItem user) {
//        MainApplicationSingleton.logD(response, TAG + ":onPostGetRecentEmailsResponse:");
//        asyncGetFullEmailsAndUpdate("[49, 50, 51]", email, user);
        int status = response.optInt("status");
        final int[] count = {0};
        if (1 == status) {
            if (MessageEmailContentProvider.isGetRecentEmailsUpdateInDBFromJSONRequired(response, this) <= 0) {
//                ContactItem userItem = (ContactItem) user.clone();
                getRecentEmailsLooperThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            count[0] = MessageEmailContentProvider.savesGetRecentEmailsInDBFromJSON(
                                    response, user, EmailService.this);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Log.e(TAG, TAG + e.toString());
                        }
                        EmailService.this.quitGetRecentEmailsLooperThread(getRecentEmailsLooperThread, skip, count[0], user);
                    }
                });
            }
        } else if (99 == status) {
            onPostGetEmailsByErrorResponse(response);
        } else {
            onPostGetEmailsByErrorResponse(response);
        }
//        setGetRecentEmailsFromCloudTaskToNull();
    }

    private void quitGetRecentEmailsLooperThread(HandlerThread looperThread, int skip, int count, ContactItem user) {
        if (looperThread != null) {
            looperThread.quit();
            this.getRecentEmailsLooperThread = null;
        }
//        get recent emails, returned the full 500 limit.. more to come.. recurse
        if (500 == count) {
            asyncGetRecentEmails(user, skip + 500);
        }
    }

    private void quitUpdateFullEmailsLooperThread(HandlerThread looperThread) {
        if (looperThread != null) {
            looperThread.quit();
            this.updateFullEmailsLooperThread = null;
        }
    }

    @Override
    public void onPostGetFullEmailsErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetFullEmailsErrorResponse:" + response);
    }

    @Override
    public void onPostGetFullEmailsResponse(
            final JSONObject response, String[] emailUidsArray, String emailAccount, final ContactItem user, int mode) {
//        MainApplicationSingleton.logD(response, TAG + ":onPostGetFullEmailsResponse");
        try {
            int status = response.optInt("status");
            if (1 == status) {
                if (mode > 0) {
//            saves in db
//                    ContactItem userItem = (ContactItem) user.clone();
//                    if (MessageEmailContentProvider.isGetRecentEmailsUpdateInDBFromJSONRequired(response, this) <= 0) {
//                ContactItem userItem = (ContactItem) user.clone();
                    updateFullEmailsLooperThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MessageEmailContentProvider.updatesGetFullEmailsInDBFromJSON(response, user, EmailService.this);
                            } catch (Throwable e) {
                                e.printStackTrace();
                                Log.e(TAG, TAG + e.toString());
                            }
                            EmailService.this.quitUpdateFullEmailsLooperThread(updateFullEmailsLooperThread);
                        }
                    });
//                    }
                }
            } else if (99 == status) {
                onPostGetFullEmailsErrorResponse(response);
            } else {
                onPostGetFullEmailsErrorResponse(response);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.toString());
        }
//        setGetFullEmailsFromCloudTaskToNull();
    }

    @Override
    public void onPostGetEmailsByResponse(JSONObject response) {
        MainApplicationSingleton.logD(response, TAG + ":onPostGetEmailsByResponse:");
/*
{
    "status": 1,
    "err": "",
    "emails": [
        {
            "user": "",
            "mailbox": "",
            "msg_uid": 12,
            "msg_seqno": 11,
            "msg_modseq": "4385",
            "subject": "Re : demo interrupted",
            "from": [
                {
                    "address": "muthu@intellibitz.com",
                    "name": "Android lover nishy"
                }
            ],
            "to": [
                {
                    "address": "jeff@intellibitz.com",
                    "name": ""
                },
                {
                    "address": "intellibitztest@gmail.com",
                    "name": ""
                }
            ],
            "cc": [
                {
                    "address": "muthu@intellibitz.com",
                    "name": ""
                }
            ],
            "date": "2016-06-28T14:09:50.000Z",
            "plain_text": "Demi come",
            "full_text": "Demi come \n",
            "no_text": false,
            "attachments": "[]",
            "attachments_count": 0,
            "flags": "[\"\\\\Seen\"]"
        }
    ]
}
         */
    }

    @Override
    public void onPostGetEmailsByErrorResponse(JSONObject response) {
        Log.d(TAG, "onPostGetEmailsByErrorResponse:" + response);
    }

    private void emitToSendMessage(Message msg) {
        Bundle args = msg.getData();
        final MessageItem messageItem =
                args.getParcelable(MessageItem.TAG);
        final ContactItem user = args.getParcelable(ContactItem.USER_CONTACT);
        handlerThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
            @Override
            public void run() {
                emitToSendEmailMessage(messageItem, user);
            }
        });
    }

    private void emitToSendEmailMessage(MessageItem emailMessage, ContactItem user) {
//        // TODO: 13-03-2016
//        get the message thread item.. and work with that.. avoid going to DB again
//        Cursor cursor = getMessageThreadItemCursor(id);
        Log.e(TAG, "emitToSendEmailMessage: " + emailMessage.getSubject());
        String from = emailMessage.getDocOwnerEmail();
//        // TODO: 16-03-2016
//        retrieves emails from DB.. for existing message thread item
        if (emailMessage.getDataId() != null &&
                !MessageItem.TAG.equals(emailMessage.getDataId())) {
            Cursor c = getMessageThreadMsgContactsCursor(emailMessage.getDataId());
            if (c != null && c.getCount() > 0) {
//        // TODO: 05-03-2016
//        to get the active email used now
                if (null == from) {
//            // TODO: 07-03-2016
//            this should not happen...
                    from = MainApplicationSingleton.DUMMY_EMAIL;
                }
                ContactItem contactItem = emailMessage.getContactItem();
                if (null == contactItem) {
                    contactItem = new ContactItem();
                    emailMessage.setContactItem(contactItem);
                }
                // looping through all rows and adding to list
                if (c.moveToFirst()) {
                    do {
                        ContactItem emailItem = new ContactItem();
                        String email = c.getString(c.getColumnIndex(
                                ContactItemColumns.KEY_TYPE_ID));
                        if (!TextUtils.isEmpty(email)) {
                            emailItem.setTypeId(email);
                            emailItem.setDataId(email);
                            emailItem.setName(c.getString(c.getColumnIndex(
                                    ContactItemColumns.KEY_NAME)));
                            emailItem.setType(c.getString(c.getColumnIndex(
                                    ContactItemColumns.KEY_TYPE)));
                            contactItem.addContact(emailItem);
                        }
                    } while (c.moveToNext());
                }
                c.close();
            } else {
//            to = emailMessage
            }
        }

        emailMessage.setFrom(from);
        emailMessage.invoke();
/*
        Set<EmailItem> emailItems = emailMessage.getEmails();
        EmailTags emailTags = new EmailTags(
                from, emailItems).invoke();
        String to = emailTags.getTo();
        String cc = emailTags.getCc();
        String bcc = emailTags.getBcc();
*/

        ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
                emailMessage.getMessageItemConcurrentLinkedQueue();
        MessageItem[] messageItems;
        synchronized (lock) {
            messageItems = messageItemConcurrentLinkedQueue.toArray(new MessageItem[0]);
        }
//        first loop, send to cloud
        for (MessageItem messageItem : messageItems) {
            try {
                if (messageItem.isReadyToSend()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("msg_type", "EMAIL");
                    jsonObject.put("to_uid", emailMessage.getChatId());
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
                                JSONObject attachmentDetails = HttpUrlConnectionParser.uploadAttachments(item,
                                        MainApplicationSingleton.ATTACHMENT_UPLOAD_URL,
                                        uid, device, user.getDeviceRef(), token);
                                attachments.put(attachmentDetails);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        jsonObject.put("attachments", attachments);
                    }
//        jsonObject.put("doc_type", "MSG");
//        jsonObject.put("msg_type", "EMAIL");
//        try {
                    jsonObject.put("email", from);
                    String name = user.getName();
                    jsonObject.put("from", name + " <" + from + ">");
//            // TODO: 16-03-2016
//        this can never happen.. inform user if email null
                    if (TextUtils.isEmpty(emailMessage.getTo())) {
                        emailMessage.setTo(MainApplicationSingleton.DUMMY_EMAIL);
                    }
                    jsonObject.put("to", emailMessage.getTo());
//        if (!cc.isEmpty())
                    jsonObject.put("cc", emailMessage.getCc());
//        if (!bcc.isEmpty())
                    jsonObject.put("bcc", emailMessage.getBcc());
                    jsonObject.put("subject", emailMessage.getSubject());
//                    MessageItem messageItem = emailMessage.peekMessageInStack();
                    Log.e(TAG, "EmailService Emitting to 'send_email'....." + jsonObject);
                    socket.emit(SEND_EMAIL, jsonObject, ackEmitSendEmailMsgListener);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception: " + e);
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

            int status = -1;
            if (response != null)
                status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (99 == status || -1 == status) {
                Bundle bundle = new Bundle();
                if (response != null)
                    bundle.putString("error", response.toString());
                Log.e(TAG, "PROFILE UPLOAD ERROR - " + response);
            } else if (1 == status) {
//                    SUCCESS
                result = response.getString("url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Cursor getMessageThreadItemCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MessagesEmailContentProvider.CONTENT_URI, Uri.encode(id));
        return getContentResolver().query(uri,
                new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_DATA_ID + " = ?",
                new String[]{id}, null);

    }

    private Cursor getMessageThreadMsgContactsCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MsgEmailContactsContentProvider.CONTENT_URI,
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
                MainApplicationSingleton.PI_EMAIL_RQ_CODE,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // the status icon
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
                MainApplicationSingleton.PI_EMAIL_RQ_CODE,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(msg)  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    @Override
    public void onPostGetMailboxListResponse(JSONObject response) {
        MainApplicationSingleton.logD(response, TAG + ":onPostGetMailboxListResponse");
    }

/*
    private Cursor getMessageThreadEmailsCursor(String id) {
//                    gets message thread by data id
        Uri uri = Uri.withAppendedPath(MessageThreadEmailContentProvider.CONTENT_URI,
                Uri.encode(id));
        return getContentResolver().query(uri, null, null, null, null);

    }
*/

    @Override
    public void onPostGetMailboxListErrorResponse(JSONObject response) {
        Log.e(TAG, TAG + response);
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
        Log.e(TAG, "EmailService Message Thread Email saved: " + uri);
        return uri;
    }

    private Uri saveMessageThreadItem(MessageItem item) throws IOException {
        ContentValues msgThreadContentValues = new ContentValues();
        msgThreadContentValues.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(item));
        Uri uri = getContentResolver().insert(MessagesEmailContentProvider.CONTENT_URI,
                msgThreadContentValues);
        Log.e(TAG, "EmailService Message Thread saved: " + uri);
        return uri;
    }
*/

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
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

    class OnConnectListener implements Emitter.Listener {
        Ack ackEmitJoinUidCallbackListener = new Ack() {
            @Override
            public void call(Object... objects) {
                String result = Arrays.toString(objects);
                Log.e(TAG,
                        "EmailService Callback from 'join_uid' with results....." + result);
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
                        Log.e(TAG, "EmailService Joined Socket with ref: " + ref);
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
                Log.e(TAG, "EmailService Emitting to 'join_uid'....." + jsonObject);
                socket.emit(JOIN_UID, jsonObject, ackEmitJoinUidCallbackListener);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    class OnDisconnectListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            Log.e(TAG, "EmailService Disconnect.....");
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }

    class OnErrorListener implements Emitter.Listener {
        @Override
        public void call(Object... args1) {
            Log.e(TAG, "EmailService Error.....");
            // socket.emit("my other event", "968768");
            //socket.disconnect();
        }
    }

}


