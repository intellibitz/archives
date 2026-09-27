package intellibitz.intellidroid.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.listener.ClutterListener;
import intellibitz.intellidroid.listener.ClutterMessageHeaderListener;
import intellibitz.intellidroid.task.CreateDraftTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.listener.ClutterListener;
import intellibitz.intellidroid.listener.ClutterMessageHeaderListener;
import intellibitz.intellidroid.service.EmailService;
import intellibitz.intellidroid.task.CreateDraftTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.widget.NewEmailDialogFragment;

import intellibitz.intellidroid.service.EmailService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import intellibitz.intellidroid.schedule.ScheduleCreateTask;
//import intellibitz.intellidroid.schedule.ScheduleDateTimeFragment;

/**
 *
 */
public class ComposeEmailFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
        CreateDraftTask.CreateDraftTaskListener {
//        ScheduleCreateTask.ScheduleCreateTaskListener,
//        ScheduleDateTimeFragment.OnSchDateTimeFragmentListener {

    private static final String TAG = "ComposeEmailFrag";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final Object lock = new Object();
    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
    private Uri cameraPhotoFileUri;
    private Uri videoPhotoFileUri;
    private Uri audioFileUri;
    private Button btnSend;
    private Button btnDraft;
    private Button btnSchedule;
    private EditText etMessageInput;
    private ImageButton ivAttach;
    private ImageButton btnAudio;
    private ImageButton btnCamera;
    /**
     * Messenger for communicating with service.
     */
    private Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    private boolean mIsBound;
    private MessageItem messageItem;
    private MessageItem selectedItem;
    private MessageItem selfTypingMessage;
    private String title;
    private String subTitle;
    private String filter;
    private ImageButton btnVideo;
    private HandlerThread looperThread;
    private HandlerThread draftLooperThread;
    private HandlerThread scheduleLooperThread;
    private CreateDraftTask createDraftTask;
    private ClutterMessageHeaderListener emailMessageHeaderListener;
    private Toolbar toolbar;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
//            mCallbackText.setText("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        EmailService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.
                msg = Message.obtain(null,
                        EmailService.MSG_SET_VALUE, this.hashCode(), 0);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
//            mCallbackText.setText("Disconnected.");

        }
    };
    private EditText sub;
    private MultiAutoCompleteTextView to;
    private MultiAutoCompleteTextView cc;
    private MultiAutoCompleteTextView bcc;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComposeEmailFragment() {
        super();
    }

    public static ComposeEmailFragment newInstance(
            MessageItem messageItem, ContactItem user, ClutterListener clutterListener) {
        ComposeEmailFragment fragment = new ComposeEmailFragment();
        fragment.setUser(user);
        fragment.setMessageItem(messageItem);
        if (clutterListener != null)
            fragment.setViewModeListener(clutterListener);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(MessageItem.TAG, messageItem);
        fragment.setArguments(args);
        return fragment;
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        getAppCompatActivity().bindService(new Intent(getAppCompatActivity(),
                EmailService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
//        mCallbackText.setText("Binding.");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            EmailService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            getAppCompatActivity().unbindService(mConnection);
            mIsBound = false;
//            mCallbackText.setText("Unbinding.");
        }
    }

    private void sendOnKey() {
        if (null == mService) {
            Log.e(TAG, "Service is NULL - cannot send on key message");
        } else {
            try {
                Message msg = Message.obtain(null,
                        EmailService.MSG_ON_KEY, this.hashCode(), 0);
                msg.obj = messageItem;
                Bundle args = new Bundle();
                args.putParcelable(ContactItem.USER_CONTACT, user);
                args.putParcelable(MessageItem.TAG, messageItem);
                msg.setData(args);
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendOnKey(MessageItem messageItem, ContactItem user) {
        if (null == mService) {
            Log.e(TAG, "Service is NULL - cannot send on key message");
        } else {
            try {
                Message msg = Message.obtain(null,
                        EmailService.MSG_ON_KEY, this.hashCode(), 0);
                msg.obj = messageItem;
                Bundle args = new Bundle();
                args.putParcelable(ContactItem.USER_CONTACT, user);
                args.putParcelable(MessageItem.TAG, messageItem);
                msg.setData(args);
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(MessageItem messageItem, ContactItem user) {
/*
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_TITLE, id);
        contentValues.put(DatabaseHelper.KEY_DESCRIPTION, message);
        getAppCompatActivity().getContentResolver().insert(
                ChatMessageContentProvider.CONTENT_URI, contentValues);
*/
//        getSupportLoaderManager().restartLoader(0, null, this);
//        lvMessagesAdapter.notifyDataSetChanged();
        // Give it some value as an example.
        Message msg = Message.obtain(null,
                EmailService.MSG_SET_VALUE, this.hashCode(), 0);
        msg.obj = messageItem;
//        adds again.. check if the collection holds
//        // TODO: 14-03-2016
//        redundant add - remove this
//        messageItem.addMessage(messageItem);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(MessageItem.TAG, messageItem);
        msg.setData(args);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void refreshEmailMessageLoader(Bundle arguments) {
        restoreStateFromFragmentArguments(arguments);
    }

    public MessageItem getMessageItem() {
        return messageItem;
    }

    public void setMessageItem(MessageItem messageItem) {
        this.messageItem = messageItem;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        doBindService();
    }

    private void restoreStateFromFragmentArguments(Bundle arguments) {
        if (null == arguments) {
            arguments = getArguments();
        }
        if (arguments != null) {
            user = arguments.getParcelable(ContactItem.USER_CONTACT);
            messageItem = arguments.getParcelable(
                    MessageItem.TAG);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        prepareNewEmailToSend();
        outState.putParcelable(MessageItem.TAG, messageItem);
    }

    @Override
    public void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void quitLooperThread() {
        if (looperThread != null)
            looperThread.quit();
        sendMessage(messageItem, user);
        okActivity();
//        broadcastMessagesSend(messageItem);
    }

    private void quitDraftLooperThread() {
        if (draftLooperThread != null)
            draftLooperThread.quit();
    }

    private void quitScheduleLooperThread() {
        if (scheduleLooperThread != null)
            scheduleLooperThread.quit();
    }

    @Override
    public void onPostCreateDraftFromCloudExecute(JSONObject response, MessageItem messageItem) {
        etMessageInput.setEnabled(true);
        etMessageInput.setText("");
        Log.e(TAG, "onPostFoldersGetFromCloudExecute: " + response);
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || -1 == status || 99 == status) {
//            retries again..
            onPostCreateDraftFromCloudExecuteFail(response, messageItem);
        } else {
            try {
                String id = response.getString("draft_id");
                messageItem.setDataId(id);
                Log.d(TAG, "Draft saved Success: " + id);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onPostCreateDraftFromCloudExecuteFail(JSONObject response, MessageItem messageItem) {
        etMessageInput.setEnabled(true);
        etMessageInput.setText("");
        Log.d(TAG, "onPostCreateDraftFromCloudExecuteFail: Draft saved Fail: " + response);
    }

    @Override
    public void setCreateDraftFromCloudTaskToNull() {
        createDraftTask = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_composeemail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
            messageItem = getArguments().getParcelable(
                    MessageItem.TAG);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            messageItem = savedInstanceState.getParcelable(
                    MessageItem.TAG);
        }
        if (null == messageItem) {
            messageItem = new MessageItem();
        }
        setupAppBar();

//        sets up the email drop down for to, cc, bcc
        sub = (EditText) view.findViewById(R.id.new_email_sub);
        to = (MultiAutoCompleteTextView) view.findViewById(R.id.new_email_to);
        cc = (MultiAutoCompleteTextView) view.findViewById(R.id.new_email_cc);
        bcc = (MultiAutoCompleteTextView) view.findViewById(R.id.new_email_bcc);
        etMessageInput = (EditText) view.findViewById(R.id.et_message);

        if (messageItem != null) {
            sub.setText(messageItem.getSubject());
            to.setText(messageItem.getTo());
            cc.setText(messageItem.getCc());
            bcc.setText(messageItem.getBcc());
        }

        to.setTokenizer(new Rfc822Tokenizer());
        cc.setTokenizer(new Rfc822Tokenizer());
        bcc.setTokenizer(new Rfc822Tokenizer());
        new SetupEmailAutoCompleteTask().execute();

        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnDraft = (Button) view.findViewById(R.id.btn_draft);
        btnSchedule = (Button) view.findViewById(R.id.btn_schedule);
        ivAttach = (ImageButton) view.findViewById(R.id.iv_attach);

        btnAudio = (ImageButton) view.findViewById(R.id.ib_audio);
        btnCamera = (ImageButton) view.findViewById(R.id.ib_camera);
        btnVideo = (ImageButton) view.findViewById(R.id.ib_video);
//        rvMessages = (RecyclerView) view.findViewById(R.id.rv_chat_messages);
        if (null == savedInstanceState) {
            etMessageInput.requestFocus();
            etMessageInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    toggleNewMessageView(s);
//                    sendOnKey(messageItem, user);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            btnDraft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message = etMessageInput.getText().toString();
                    if (TextUtils.isEmpty(message)) {
                        return;
                    }
                    etMessageInput.setEnabled(false);
                    messageItem.flagStackMessageToSend();
                    messageItem.getMessageItemConcurrentLinkedQueue().add(
                            messageItem.popMessage());
                    draftLooperThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
                        @Override
                        public void run() {
                            ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
                                    messageItem.getMessageItemConcurrentLinkedQueue();
                            MessageItem[] messageItems;
                            synchronized (lock) {
                                messageItems = messageItemConcurrentLinkedQueue.toArray(new MessageItem[0]);
                            }
                            for (MessageItem messageItem : messageItems) {
                                if (messageItem.isReadyToSend()) {
                                    messageItem.setBaseType(MessageItem.DRAFT);
//                                    the cloud treats this doc type as draft
                                    messageItem.setDocType(MessageItem.DRAFT);
                                    messageItem.setDocOwner(user.getDataId());
                                    messageItem.setType(MessageItem.EMAIL);
                                    messageItem.setMessageType(MessageItem.EMAIL);
                                    messageItem.setToType(MessageItem.USER);
                                    messageItem.setGroup(false);
                                    messageItem.setEmailItem(true);
                                    messageItem.setChatId(ComposeEmailFragment.this.messageItem.getChatId());
                                    messageItem.setDataId(ComposeEmailFragment.this.messageItem.getChatId());
//                                        // TODO: 19-06-2016
//                                        set chat_msg_ref from app
//                                        chat_msg_ref format: fromuid_touid_timestamp
                                    messageItem.setChatMsgRef(user.getDataId() +
                                            "_" + messageItem.getChatId() + "_" + System.currentTimeMillis());
/*
                                    createDraftTask = new CreateDraftTask(messageItem,
                                            user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                                            MainApplicationSingleton.AUTH_CREATE_DRAFT);
                                    createDraftTask.setCreateDraftTaskListener(ClutterEmailFragment.this);
                                    createDraftTask.execute();
*/
                                    broadcastMessagesToDraft(messageItem);
                                    messageItemConcurrentLinkedQueue.remove(messageItem);

/*
                                        Uri uri = MessageContentProvider.savesMessageItem(
                                                messageItem, user, getContext());
                                        Log.d(TAG, "Message Draft saved: " + uri);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Message Draft save FAIL: " + messageItem);
                                    }
*/
                                }
                            }
                            ComposeEmailFragment.this.quitDraftLooperThread();
                        }
                    });
                    etMessageInput.setEnabled(true);
                    etMessageInput.setText("");
                }
            });
            btnSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prepareNewEmailToSend();
//                    // TODO: 22-05-2016
//                    to mainitain input Q, the user can type and hit send madly like a million times
                    String message = etMessageInput.getText().toString();
                    String from = messageItem.getFromEmail();
                    String to = messageItem.getTo();
                    DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    };
                    if (TextUtils.isEmpty(from)) {
                        MainApplicationSingleton.alertDialog(getContext(),
                                "Please fill from email", "Email Incomplete", okListener, null);
                        return;
                    }
                    if (TextUtils.isEmpty(to)) {
                        MainApplicationSingleton.alertDialog(getContext(),
                                "Please fill to email", "Email Incomplete", okListener, null);
                        ComposeEmailFragment.this.to.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(message)) {
                        MainApplicationSingleton.alertDialog(getContext(),
                                "Please fill message", "Email Incomplete", okListener, null);
                        ComposeEmailFragment.this.etMessageInput.requestFocus();
                        return;
                    }

                    etMessageInput.setEnabled(false);
                    messageItem.flagStackMessageToSend();
                    messageItem.getMessageItemConcurrentLinkedQueue().add(
                            messageItem.popMessage());
                    scheduleLooperThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
                        @Override
                        public void run() {
                            ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
                                    messageItem.getMessageItemConcurrentLinkedQueue();
                            MessageItem[] messageItems;
                            synchronized (lock) {
                                messageItems = messageItemConcurrentLinkedQueue.toArray(new MessageItem[0]);
                            }
                            for (MessageItem messageItem : messageItems) {
                                if (messageItem.isReadyToSend()) {
                                    messageItem.setBaseType(MessageItem.SCHEDULE);
//                                    the cloud treats this doc type as draft
                                    messageItem.setDocType(MessageItem.SCHEDULE);
                                    messageItem.setDocOwner(user.getDataId());
                                    messageItem.setType(MessageItem.EMAIL);
                                    messageItem.setMessageType(MessageItem.EMAIL);
                                    messageItem.setToType(MessageItem.USER);
                                    messageItem.setGroup(false);
                                    messageItem.setEmailItem(true);
                                    messageItem.setChatId(ComposeEmailFragment.this.messageItem.getChatId());
                                    messageItem.setDataId(ComposeEmailFragment.this.messageItem.getChatId());
//                                        // TODO: 19-06-2016
//                                        set chat_msg_ref from app
//                                        chat_msg_ref format: fromuid_touid_timestamp
                                    messageItem.setChatMsgRef(user.getDataId() +
                                            "_" + messageItem.getChatId() + "_" + System.currentTimeMillis());
/*
                                    createDraftTask = new CreateDraftTask(messageItem,
                                            user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                                            MainApplicationSingleton.AUTH_CREATE_DRAFT);
                                    createDraftTask.setCreateDraftTaskListener(ClutterEmailFragment.this);
                                    createDraftTask.execute();
*/
//                                    broadcastMessagesToDraft(messageItem);
                                    performScheduling(messageItem);
                                    messageItemConcurrentLinkedQueue.remove(messageItem);

/*
                                        Uri uri = MessageContentProvider.savesMessageItem(
                                                messageItem, user, getContext());
                                        Log.d(TAG, "Message Draft saved: " + uri);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Message Draft save FAIL: " + messageItem);
                                    }
*/
                                }
                            }
                            ComposeEmailFragment.this.quitDraftLooperThread();
                        }
                    });
                    etMessageInput.setEnabled(true);
                    etMessageInput.setText("");
                }
            });
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prepareNewEmailToSend();
//                    // TODO: 22-05-2016
//                    to mainitain input Q, the user can type and hit send madly like a million times
                    String message = etMessageInput.getText().toString();
                    String from = messageItem.getFromEmail();
                    String to = messageItem.getTo();
                    DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    };
                    if (TextUtils.isEmpty(from)) {
                        MainApplicationSingleton.alertDialog(getContext(),
                                "Please fill from email", "Email Incomplete", okListener, null);
                        return;
                    }
                    if (TextUtils.isEmpty(to)) {
                        MainApplicationSingleton.alertDialog(getContext(),
                                "Please fill to email", "Email Incomplete", okListener, null);
                        ComposeEmailFragment.this.to.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(message)) {
                        MainApplicationSingleton.alertDialog(getContext(),
                                "Please fill message", "Email Incomplete", okListener, null);
                        ComposeEmailFragment.this.etMessageInput.requestFocus();
                        return;
                    }

                    etMessageInput.setEnabled(false);
                    messageItem.flagStackMessageToSend();
                    messageItem.getMessageItemConcurrentLinkedQueue().add(
                            messageItem.popMessage());
//                not required.. message would be already populated
//                messageItem.getMessages().setText(message);
//                messageItem.getMessages().addAttachments(messageItem.getAttachments());
//                    saves to db before sending to cloud
                    looperThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
                        @Override
                        public void run() {
//                            // TODO: 28-06-2016
//                            revisit.. brand new message, and how to track them effectively by id
//                            new messages sent, should be shown in sent
/*
                            ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
                                    messageItem.getMessageItemConcurrentLinkedQueue();
                            MessageItem[] messageItems;
                            synchronized (lock) {
                                messageItems = messageItemConcurrentLinkedQueue.toArray(new MessageItem[0]);
                            }
                            for (MessageItem messageItem : messageItems) {
                                if (messageItem.isReadyToSend()) {
                                    try {
                                        messageItem.setType("EMAIL");
                                        messageItem.setChatId(messageItem.getChatId());
                                        messageItem.setDataId(messageItem.getChatId());
                                        messageItem.setToType(messageItem.getToType());
//                                        // TODO: 19-06-2016
//                                        set chat_msg_ref from app
//                                        chat_msg_ref format: fromuid_touid_timestamp
                                        messageItem.setChatMsgRef(user.getDataId() +
                                                "_" + messageItem.getChatId() + "_" + System.currentTimeMillis());
                                        Uri uri = MessageContentProvider.savesMessageItem(
                                                messageItem, user, getContext());
                                        Log.d(TAG, "Message saved: " + uri);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Message save FAIL: " + messageItem);
                                    }
                                }
                            }
*/
                            ComposeEmailFragment.this.quitLooperThread();
                        }
                    });
//                    sendMessage(messageItem, user);
//                empties the input text
//                etMessageInput.setText("");
                }
            });

            ivAttach.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFilePicker();
                }
            });
            btnAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAudioPicker();
                }
            });
            btnCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCameraPicker();
                }
            });
            btnVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openVideoPicker();
                }
            });

        }
    }

    private void performScheduling(MessageItem messageItem) {
/*
        ScheduleDateTimeFragment scheduleDateTimeFragment = ScheduleDateTimeFragment.newInstance(messageItem, user);
        scheduleDateTimeFragment.addSchDateTimeFragmentListener(this);
        scheduleDateTimeFragment.show(getFragmentManager(), "Select Date & Time");
*/
    }

    private void execScheduleCreateTask(String scheduledTime, MessageItem messageItem, ContactItem user) {
        String txt = messageItem.getText();

        JSONObject schMsgJson = new JSONObject();
        try {
            schMsgJson.put("txt", txt);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String schMsg = schMsgJson.toString();
/*
        ScheduleCreateTask scheduleCreateTask = new ScheduleCreateTask(scheduledTime, schMsg, user.getCompanyId(),
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                user, MainApplicationSingleton.AUTH_SCHEDULE_CREATE, getContext());
        scheduleCreateTask.setRequestTimeoutMillis(30000);
        scheduleCreateTask.setScheduleCreateTaskListener(this);
        scheduleCreateTask.execute();
*/
    }

    public boolean onBackPressed() {
        return false;
    }

    private void setupAppBar() {
        View view = getView();
        if (view != null) {
            toolbar = (Toolbar) view.findViewById(R.id.toolbar);

//            toolbar.setTitle(R.string.menu_title_email);
//            toolbar.setSubtitle(R.string.app_title);
            TextView textView = (TextView) view.findViewById(R.id.tv_subtitle);
            textView.setText(user.getEmail());

            TextView tvClose = (TextView) view.findViewById(R.id.tv_close);
            tvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelActivity();
                }
            });
            getAppCompatActivity().setSupportActionBar(toolbar);
            // Show the Up button in the action bar.
            ActionBar actionBar = getAppCompatActivity().getSupportActionBar();
            if (actionBar != null) {
//                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    protected void cancelActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
//        getAppCompatActivity().onBackPressed();
    }

    protected void okActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
//        getAppCompatActivity().onBackPressed();
    }

    private MessageItem toggleNewMessageView(CharSequence s) {
        MessageItem last = null;
        String txt = s.toString();
        if (0 == txt.length() || "".equals(txt)) {
            hideSend();
//            btnCamera.setVisibility(View.VISIBLE);
//            btnAudio.setVisibility(View.VISIBLE);
//            removes the message
//                pop, then to sync the stack state.. always add to the thread
            last = messageItem.popMessage();
//            removes the last new message, if the user input is empty
//            removes the last new message, if the text is empty AND the attachments are empty
            if (last != null &&
                    MessageItem.TAG.equals(last.getDataId()) &&
                    !last.getAttachments().isEmpty()) {
//                remove only, when the latest message.. is the new message created here
//                retains the valid db saved latest message
                messageItem.getMessages().remove(last);
            }
//            checks for shared items coming in from device
            if (messageItem.getSharedText() != null ||
                    messageItem.getSharedUri() != null ||
                    !messageItem.getSharedUris().isEmpty()) {
                try {
                    last = addNewMessageToMessageThread("", messageItem);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
//            lvNewMessage.setVisibility(View.GONE);
//            adds a new message, if the user input is not empty
        } else {
            try {
//                pop, then to sync the stack state.. always add to the thread
                last = messageItem.popMessage();
                if (last != null &&
                        MessageItem.TAG.equals(last.getDataId())) {
//                    a new message already exists, reset the typed text from user
                    last.setText(txt);
                    messageItem.addMessage(last);
                } else {
//                adds a new messaage, for the first letter every time, with the user typed text
                    last = addNewMessageToMessageThread(txt, messageItem);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            showSend();
        }
//        createRecyclerAdapter();
        return last;
    }

    private void showSend() {
        btnSend.setVisibility(View.VISIBLE);
        btnDraft.setVisibility(View.VISIBLE);
    }

    private void hideSend() {
        btnSend.setVisibility(View.GONE);
        btnDraft.setVisibility(View.GONE);
    }

    private void showNewMessageWithLatestHeader(MessageItem item) {
        messageItem.setSubject(item.getSubject());
        messageItem.setTo(item.getTo());
        messageItem.setCc(item.getCc());
        messageItem.setBcc(item.getBcc());
//        lets the listeners know for the new headers
        emailMessageHeaderListener.onEmailMessageHeaderChanged(messageItem);
//        show the latest new message info
        String text = "";
        MessageItem messageItem = this.messageItem.peekMessageInStack();
        if (messageItem != null)
            text = messageItem.getText();
        toggleNewMessageView(text);
    }

    private void openAudioPicker() {
        startGetAudioContentActivityOnPermissions();
    }

    private void openCameraPicker() {
        startGetCameraContentActivityOnPermissions();
    }

    private void openVideoPicker() {
        startGetVideoContentActivityOnPermissions();
    }

    private void startGetCameraContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                PermissionChecker.checkSelfPermission(
                        getActivity(), Manifest.permission.CAMERA) !=
                        PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION);
        } else {
            startGetCameraContentActivity();
        }
    }

    private void startGetVideoContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                PermissionChecker.checkSelfPermission(
                        getActivity(), Manifest.permission.CAMERA) !=
                        PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION);
        } else {
            startGetVideoContentActivity();
        }
    }

    private void startGetAudioContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                PermissionChecker.checkSelfPermission(
                        getActivity(), Manifest.permission.RECORD_AUDIO) !=
                        PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION);
        } else {
            startGetAudioContentActivity();
        }
    }


/*
    public void composeMmsMessage(String message, Uri attachment) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.putExtra("sms_body", message);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void composeMmsMessage(String message, Uri attachment) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("smsto:"));  // This ensures only SMS apps respond
        intent.putExtra("sms_body", message);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
*/

    private void startGetContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker.checkSelfPermission(
                getActivity(), Manifest.permission.CAMERA) !=
                PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainApplicationSingleton.REQUEST_CAMERA_AND_STORAGE_PERMISSION);
        } else {
//            setupMediaChooser();
//            openFilePicker();
            startGetContentActivity();
        }
    }

    private void startGetContentActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Filter to only show results that can be "opened", such as a file (as opposed to a list
        // of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

//        Log.d(TAG, "start get content: " + messageItem);
        /**
         * You are calling startActivityForResult() from your Fragment. When you do this,
         * the requestCode is changed by the Activity that owns the Fragment.
         If you want to get the correct resultCode in your activity try this:
         Change:
         startActivityForResult(intent, 1);
         To:
         getActivity().startActivityForResult(intent, 1);
         Just a note: if you use startActivityForResult in a fragment and expect the result from
         onActivityResult in that fragment, just make sure you call super.onActivityResult in the
         host activity (in case you override that method there).
         This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
         Also, note that the request code, when it travels through the activity's onActivityResult,
         is altered
         "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
         */
        startActivityForResult(intent,
                MainApplicationSingleton.ACTION_GET_CONTENT);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String file = "JPEG_" + timeStamp + "_";
        return MediaPickerFile.createImageFileInESPublicDir(file, ".jpg");
    }

    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String file = "3GP_" + timeStamp + "_";
        return MediaPickerFile.createImageFileInESPublicDir(file, ".3gp");
    }

    private File createAudioFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String file = "3GP_" + timeStamp + "_";
        // Save a file: path for use with ACTION_VIEW intents
        return MediaPickerFile.createSoundFileInESPublicDir(file, ".3gp");
    }

    private void startGetCameraContentActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
//        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            try {
                // Create the File where the photo should go
                File file = createImageFile();
                // Continue only if the File was successfully created
                cameraPhotoFileUri = Uri.fromFile(file);
                messageItem.setPhotoFileUri(cameraPhotoFileUri);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoFileUri);
                /**
                 * You are calling startActivityForResult() from your Fragment. When you do this,
                 * the requestCode is changed by the Activity that owns the Fragment.
                 If you want to get the correct resultCode in your activity try this:
                 Change:
                 startActivityForResult(intent, 1);
                 To:
                 getActivity().startActivityForResult(intent, 1);
                 Just a note: if you use startActivityForResult in a fragment and expect the result from
                 onActivityResult in that fragment, just make sure you call super.onActivityResult in the
                 host activity (in case you override that method there).
                 This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
                 Also, note that the request code, when it travels through the activity's onActivityResult,
                 is altered
                 "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
                 */
                startActivityForResult(intent,
                        MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION);
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void startGetVideoContentActivity() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                File file = createVideoFile();
                // Continue only if the File was successfully created
                videoPhotoFileUri = Uri.fromFile(file);
                messageItem.setVideoFileUri(videoPhotoFileUri);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoPhotoFileUri);
                /**
                 * You are calling startActivityForResult() from your Fragment. When you do this,
                 * the requestCode is changed by the Activity that owns the Fragment.
                 If you want to get the correct resultCode in your activity try this:
                 Change:
                 startActivityForResult(intent, 1);
                 To:
                 getActivity().startActivityForResult(intent, 1);
                 Just a note: if you use startActivityForResult in a fragment and expect the result from
                 onActivityResult in that fragment, just make sure you call super.onActivityResult in the
                 host activity (in case you override that method there).
                 This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
                 Also, note that the request code, when it travels through the activity's onActivityResult,
                 is altered
                 "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
                 */
                startActivityForResult(intent,
                        MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION);
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void startGetAudioContentActivity() {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            try {
                // Continue only if the File was successfully created
                // Create the File where the photo should go
                File file = createAudioFile();
                audioFileUri = Uri.fromFile(file);
                messageItem.setAudioFileUri(audioFileUri);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, audioFileUri);
/*
                startActivityForResult(intent,
                        MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION);
*/
                /**
                 * You are calling startActivityForResult() from your Fragment. When you do this,
                 * the requestCode is changed by the Activity that owns the Fragment.
                 If you want to get the correct resultCode in your activity try this:
                 Change:
                 startActivityForResult(intent, 1);
                 To:
                 getActivity().startActivityForResult(intent, 1);
                 Just a note: if you use startActivityForResult in a fragment and expect the result from
                 onActivityResult in that fragment, just make sure you call super.onActivityResult in the
                 host activity (in case you override that method there).
                 This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
                 Also, note that the request code, when it travels through the activity's onActivityResult,
                 is altered
                 "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
                 */
                startActivityForResult(intent,
                        MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION);
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MainApplicationSingleton.REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setupMediaChooser();
                startGetContentActivity();
            }
        } else if (requestCode == MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setupMediaChooser();
                startGetAudioContentActivity();
            }
        } else if (requestCode == MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setupMediaChooser();
                startGetVideoContentActivity();
            }
        } else if (requestCode == MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setupMediaChooser();
                startGetCameraContentActivity();
            }
        }
    }

    private void openFilePicker() {
        startGetContentActivityOnPermissions();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (MainApplicationSingleton.ACTION_GET_CONTENT == requestCode
                && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clipData = intent.getClipData();
                    if (null == clipData) {
                        Uri uri = intent.getData();
                        if (uri != null) {
                            onAttachmentPicked(uri);
                        }
                    } else {
                        int count = clipData.getItemCount();
                        for (int i = 0; i < count; ++i) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            if (uri != null) {
                                onAttachmentPicked(uri);
                            }
                        }
                    }
                }
            }
        } else if (MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION == requestCode
                && resultCode == Activity.RESULT_OK) {
            if (null == intent) {
                if (null == messageItem) {
//                force load the latest
                    restoreStateFromFragmentArguments(getArguments());
/*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem.getPhotoFileUri());
                }
            } else {
//                Bundle extras = data.getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
                Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                if (null == uri) uri = intent.getData();
                onAttachmentPicked(uri);
            }
        } else if (MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION == requestCode
                && resultCode == Activity.RESULT_OK) {
            if (null == intent) {
                if (null == messageItem) {
//                force load the latest
                    restoreStateFromFragmentArguments(getArguments());
/*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem.getVideoFileUri());
                }
            } else {
//                Bundle extras = data.getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
                Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                if (null == uri) uri = intent.getData();
                onAttachmentPicked(uri);
            }
        } else if (MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION == requestCode
                && resultCode == Activity.RESULT_OK) {
            if (null == intent) {
                if (null == messageItem) {
//                force load the latest
                    restoreStateFromFragmentArguments(getArguments());
/*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem.getAudioFileUri());
                }
            } else {
//                Bundle extras = data.getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mImageView.setImageBitmap(imageBitmap);
                Uri uri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                if (null == uri) uri = intent.getData();
                onAttachmentPicked(uri);
            }
        }
    }

    private void onAttachmentPicked(Uri uri, MessageItem messageThreadItem) {
        if (null == uri) {
            Log.e(TAG, "Attached picked is NULL");
        } else {
            try {
                if (null == messageThreadItem) {
//                force load the latest
                    restoreStateFromFragmentArguments(getArguments());
/*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                File file = MediaPickerUri.resolveToFile(getAppCompatActivity(), uri);
                MessageItem attachmentItem = new MessageItem(file.getPath());
                attachmentItem.setDownloadURL(file.getAbsolutePath());
                attachmentItem.setName(file.getName());
                if (messageThreadItem != null) {
                    MessageItem messageItem = messageThreadItem.peekMessageInStack();
                    if (null == messageItem ||
                            !MessageItem.TAG.equals(messageItem.getDataId())) {
//                        toggleNewMessageView("");
//                        messageItem = new MessageItem(MessageItem.TAG);
                        addNewMessageToMessageThread("", messageThreadItem);
                    }
                    messageItem = messageThreadItem.popMessage();
                    messageItem.addAttachment(attachmentItem);
//                    adds message again, to resync stack after a pop
                    messageThreadItem.addMessage(messageItem);
                }
//                createRecyclerAdapter();
//                rvNewMessageAdapter.notifyDataSetChanged();
//                rvNewMessage.requestLayout();
                Log.d(TAG, "Attachment Button click: " + file);
//                MediaPickerUri.dumpImageMetaData(getAppCompatActivity(), uri);
            } catch (IOException | CloneNotSupportedException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void onAttachmentPicked(Uri uri) {
        onAttachmentPicked(uri, messageItem);
    }

    private MessageItem addNewMessageToMessageThread(String txt,
                                                     MessageItem messageItem) throws
            CloneNotSupportedException {
        MessageItem last = null;
        if (messageItem != null) {
            last = messageItem.popMessage();
            if (null == last ||
                    !MessageItem.TAG.equals(last.getDataId())) {
                last = new MessageItem();
                last.setDataId(MessageItem.TAG);
//                sets in seconds
                last.setTimestamp(System.currentTimeMillis() / 1000);
                last.setFromName(user.getName());
                last.setTo(messageItem.getFrom());
            }
            if (MessageItem.TAG.equals(last.getDataId())) {

                last.setTimestamp(System.currentTimeMillis() / 1000);
                last.setFromName(user.getName());
                last.setTo(messageItem.getFrom());

                last.setText(txt);
                last.setFromName(messageItem.getFrom());
                last.setChatId(messageItem.getChatId());
                last.setToUid(messageItem.getToUid());
                last.setToChatUid(messageItem.getToChatUid());

                last.setSubject(messageItem.getSubject());
                last.setTo(messageItem.getTo());
                last.setCc(messageItem.getCc());
                last.setBcc(messageItem.getBcc());
//                last.setFromEmail(messageItem.getDocOwnerEmail());
//                checks for shared content from the device
                if (messageItem.getSharedText() != null) {
                    last.setText(messageItem.getSharedText());
                }
                messageItem.addMessage(last);
                if (messageItem.getSharedUri() != null) {
                    onAttachmentPicked(messageItem.getSharedUri(), messageItem);
                }
                List<Uri> uris = messageItem.getSharedUris();
                for (Uri uri : uris) {
                    onAttachmentPicked(uri, messageItem);
                }
            }
        }
        return last;
    }

    private void setSelectedItem(MessageItem mItem) {
        selectedItem = mItem;
    }

    private void forwardMessages() {
        IntellibitzActivity intellibitzActivity = removeSelf();
        if (intellibitzActivity == null) return;
//        this.onDestroy();
//        intellibitzActivity.onBackPressed(intent);
        Intent intent = new Intent();
        intent.setAction(ComposeEmailFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) selectedItem);
        intellibitzActivity.onMessageForward(intent);
    }

    private void broadcastMessagesToNest() {
        Intent intent =
                new Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_NEST);
//        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) selectedItem);
        LocalBroadcastManager.getInstance(getAppCompatActivity()).sendBroadcast(intent);
//                        finishes.. so doesn't show up in back stack
        getAppCompatActivity().finish();
    }

    private void broadcastMessagesToDraft(MessageItem messageItem) {
        Intent intent =
                new Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_DRAFT);
//        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        LocalBroadcastManager.getInstance(getAppCompatActivity()).sendBroadcast(intent);
//                        finishes.. so doesn't show up in back stack
        getAppCompatActivity().finish();
    }

    private void broadcastMessagesSend(MessageItem messageItem) {
        Intent intent =
                new Intent(MainApplicationSingleton.BROADCAST_MESSAGES_SEND);
//        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        LocalBroadcastManager.getInstance(getAppCompatActivity()).sendBroadcast(intent);
//                        finishes.. so doesn't show up in back stack
//        getAppCompatActivity().finish();
    }

    private void startMimeActivity(File file, String mimeType) {
        //                        Log.d(TAG, "Clicked view: "+v);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Intent target = intent;
        Uri uri = Uri.fromFile(file);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        final String mt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mt != null) mimeType = mt;
        if (null == mimeType) {
            intent.setData(uri);
            target = Intent.createChooser(intent, "Choose an app to open with:");
        } else {
            intent.setDataAndType(uri, mimeType);
        }
        startActivity(target);
//        // TODO: 24-05-2016
//        to set the correct mime type
/*
                        intent.setDataAndType(
                                Uri.fromFile(new File(attachmentItem.getDownloadURL())),
                                attachmentItem.getType() + "/" + attachmentItem.getSubType());
*/
    }

    public void showNewMessageDialog() {
        // Create an instance of the dialog fragment and show it
        NewEmailDialogFragment.newMessageDialog(this, 1, user, messageItem).show(
                getFragmentManager(), "NewMessageDialog");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
//        // TODO: 16-03-2016
/*
        MessageItem item = dialog.getArguments().getParcelable(
                MessageItem.TAG);
*/
//        user must be signed into atleast one email account

        NewEmailDialogFragment newEmailDialogFragment = (NewEmailDialogFragment) dialog;
        MessageItem mtItem = new MessageItem();
        mtItem.setSubject(newEmailDialogFragment.getSubject());
        Rfc822Token[] to = newEmailDialogFragment.getTo();
        Rfc822Token[] cc = newEmailDialogFragment.getCc();
        Rfc822Token[] bcc = newEmailDialogFragment.getBcc();
/*
        String to = newEmailDialogFragment.getTo();
        String cc = newEmailDialogFragment.getCc();
        String bcc = newEmailDialogFragment.getBcc();
*/
        MessageItem.setMessageThreadEmailAddress(mtItem, to, cc, bcc);
/*
        mtItem.setTo(to.toString());
        mtItem.setCc(cc.toString());
        mtItem.setBcc(bcc.toString());
*/
        showNewMessageWithLatestHeader(mtItem);
/*
        messageItem.setDocType("THREAD");
//        new message.. sets id to a constant.. global new message id
        messageItem.setDataId(MessageItem.TAG);
        messageItem.setDataRev("1");
        messageItem.setDocOwner("DEMO");
        messageItem.setDocSender("DEMO");
        messageItem.setSubject("DEMO");
        messageItem.setTimestamp(System.currentTimeMillis());
*/

//        // TODO: 16-03-2016
//        retrieve the info from dialog
//        investigate .. user is not fully populated.. check dialog fragment life cycle
//        messageItem.setDocOwnerEmail(user.getEmail());
//        messageItem.setDocSenderEmail(user.getEmail());
/*
        messageItem.setHasAttachments(1);
        messageItem.addContact(user.getEmail(), "Jobs", "from");
        messageItem.addContact("nishanth@intellibitz.com", "Nishi", "to");
        messageItem.addContact("jeff@intellibitz.com", "Jeffy", "to");
        MessageItem messageItem = null;
        try {
            messageItem = messageItem.addMessage(" DEMO Test Message");
            messageItem.addAttachments(messageItem.getAttachments());

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, EmailService.class);
        intent.setAction(EmailService.INTENT_ACTION_NEW_EMAIL_MESSAGE);
        intent.putExtra(MessageItem.TAG, (Serializable) messageItem);
        startService(intent);
*/
/*
        Intent intent = new Intent(
                MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        LocalBroadcastManager.getInstance(getAppCompatActivity()).sendBroadcast(intent);
*/

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    private void addEmailsToAutoComplete(List<Map<String, String>> contacts) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        Context context = getContext();
//        the activity might disappear faster on cancel, before the loader arrives
        if (context != null) {
            SimpleAdapter adapter = new SimpleAdapter(context, contacts,
                    android.R.layout.simple_dropdown_item_1line, new String[]{"text1", "text2"},
                    new int[]{android.R.id.text1, android.R.id.text1}) {
            };
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (null == data || "".equals(data)) return true;
                    TextView textView = (TextView) view;
                    textView.setCompoundDrawables(null, null, null, null);
                    String photo = (String) data;
                    if (photo.startsWith("content")) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    getContext().getContentResolver(), Uri.parse(photo));
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            drawable.setBounds(new Rect(0, 0, 60, 60));
                            textView.setCompoundDrawables(null, null, drawable, null);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        return true;
                    }
                    return false;
                }

            });

            to.setAdapter(adapter);
            cc.setAdapter(adapter);
            bcc.setAdapter(adapter);
        }
    }

    public String getSubject() {
        return String.valueOf(sub.getText());
    }

    public Rfc822Token[] getTo() {
        return Rfc822Tokenizer.tokenize(String.valueOf(to.getText()));
    }

    public Rfc822Token[] getCc() {
        return Rfc822Tokenizer.tokenize(String.valueOf(cc.getText()));
    }

/*
    public String getTo (){
        return String.valueOf(to.getText());
    }

    public String getCc (){
        return String.valueOf(cc.getText());
    }

    public String getBcc (){
        return String.valueOf(bcc.getText());
    }
*/

    public Rfc822Token[] getBcc() {
        return Rfc822Tokenizer.tokenize(String.valueOf(bcc.getText()));
    }

    private void prepareNewEmailToSend() {
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "prepareNewEmailToSend: User email is empty - No email account signed up");
            return;
        }
//        user must be signed into atleast one email account
//        MessageItem messageItem = new MessageItem();
//        // TODO: 30-06-2016
        ContactItem contactThreadItem = messageItem.getContactItem();
        if (null == contactThreadItem) {
            contactThreadItem = new ContactItem();
            messageItem.setContactItem(contactThreadItem);
        }
//        new message.. sets id to a constant.. global new message id
        messageItem.setDataId(MessageItem.TAG);
        messageItem.setBaseType(MessageItem.THREAD);
        messageItem.setDocType(MessageItem.MSG);
        messageItem.setType(MessageItem.EMAIL);
        messageItem.setDataRev("1");
        String name = user.getName();
        messageItem.setFrom(name);
        messageItem.setSubject(getSubject());
        messageItem.setDocOwner(user.getDocOwner());
        messageItem.setDocSender(name);
        messageItem.setDocOwnerEmail(email);
        messageItem.setDocSenderEmail(email);
        messageItem.setFromEmail(email);
        messageItem.setTimestamp(System.currentTimeMillis());
        Rfc822Token[] to = getTo();
        Rfc822Token[] cc = getCc();
        Rfc822Token[] bcc = getBcc();
//        adds from
        ContactItem contactItem = new ContactItem();
        contactItem.setDataId(email);
        contactItem.setTypeId(email);
        contactItem.setIntellibitzId(email);
        contactItem.setName(name);
        contactItem.setType("from");
        contactThreadItem.addContact(contactItem);
//        compose msg thread contact aka email group contact from the selected
        messageItem.compose(to, cc, bcc);
//        MessageItem.setMessageThreadEmailAddress(messageItem, to, cc, bcc);
/*
        Intent intent = new Intent();
        intent.setAction(MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK);
        showMessageThreadMessage(messageItem, user);
        intent.putExtra(ContactItem.TAG, (Parcelable) user);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(intent);
*/
/*
        String to = newEmailDialogFragment.getTo();
        String cc = newEmailDialogFragment.getCc();
        String bcc = newEmailDialogFragment.getBcc();
*/
//        messageItem.setDocOwner(user.getDocOwner());
//        messageItem.setDocSender(user.getName());
//        messageItem.setDocOwnerEmail(user.getEmail());
//        messageItem.setDocSenderEmail(user.getEmail());
//        messageItem.setTimestamp(System.currentTimeMillis());

//        // TODO: 16-03-2016
//        retrieve the info from dialog
//        investigate .. user is not fully populated.. check dialog fragment life cycle
/*
        messageItem.setHasAttachments(1);
        messageItem.addContact(user.getEmail(), "Jobs", "from");
        messageItem.addContact("nishanth@intellibitz.com", "Nishi", "to");
        messageItem.addContact("jeff@intellibitz.com", "Jeffy", "to");
        MessageItem messageItem = null;
        try {
            messageItem = messageItem.addMessage(" DEMO Test Message");
            messageItem.addAttachments(messageItem.getAttachments());

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, EmailService.class);
        intent.setAction(EmailService.INTENT_ACTION_NEW_EMAIL_MESSAGE);
        intent.putExtra(ContactItem.TAG, (Parcelable) user);
        intent.putExtra(MessageItem.TAG, (Serializable) messageItem);
        startService(intent);
*/
    }

    //    @Override
    public void onPostScheduleCreateResponse(JSONObject response, String scheduleName, ContactItem user) {
        Log.d(TAG, "onPostScheduleCreateResponse: " + response);
        if (null == response) onPostScheduleCreateErrorResponse(null);
        int status = response.optInt("status", -1);
        if (1 == status) {
            String scheduleId = response.optString("schedule_id");
            Log.d(TAG, "onPostScheduleCreateResponse: success " + scheduleId);
            okActivity();
        } else {
            onPostScheduleCreateErrorResponse(response);
        }

    }

    //    @Override
    public void onPostScheduleCreateErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostScheduleCreateResponse: " + response);
        cancelActivity();
    }

    //    @Override
    public void onSchDateTimeDialogPositiveClick(DialogFragment dialog, MessageItem messageItem) {
/*
        ScheduleDateTimeFragment scheduleDateTimeFragment = ((ScheduleDateTimeFragment) dialog);
        if (scheduleDateTimeFragment != null) {
            String stime = scheduleDateTimeFragment.getScheduledTime();
            execScheduleCreateTask(stime, messageItem, user);
        }
*/
        if (dialog != null)
            dialog.dismiss();
    }

    //    @Override
    public void onSchDateTimeDialogNegativeClick(DialogFragment dialog) {

    }

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EmailService.MSG_SET_VALUE:
                    break;
                case EmailService.MSG_SHOW_TYPING:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<Map<String, String>>> {

        @Override
        protected List<Map<String, String>> doInBackground(Void... voids) {
            // TODO: register the new account here.
//            investigate response and take action
            ArrayList<Map<String, String>> contacts = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            Context context = getContext();
            if (null == context) return contacts;
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String val = cursor.getString(cursor.getColumnIndex(ContactsContract
                            .CommonDataKinds.Email.DATA));
                    String photo = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    HashMap<String, String> vals = new HashMap<String, String>() {
                        @Override
                        public String toString() {
//                            return super.toString();
                            return get("text1");
                        }
                    };
                    vals.put("text1", val);
                    if (null == photo) photo = "";
                    vals.put("text2", photo);
                    contacts.add(vals);
                }
                cursor.close();
            }

            return contacts;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

}

