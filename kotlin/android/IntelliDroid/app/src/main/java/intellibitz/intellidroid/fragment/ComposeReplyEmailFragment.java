package intellibitz.intellidroid.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ClutterListener;
import intellibitz.intellidroid.listener.ClutterMessageHeaderListener;
import intellibitz.intellidroid.listener.ClutterMessageListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateDraftTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.task.FlagMsgsTask;
import intellibitz.intellidroid.task.UnFlagMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.IntellibitzActivity;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MessagesEmailContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ClutterListener;
import intellibitz.intellidroid.listener.ClutterMessageHeaderListener;
import intellibitz.intellidroid.listener.ClutterMessageListener;
import intellibitz.intellidroid.service.EmailService;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateDraftTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.task.FlagMsgsTask;
import intellibitz.intellidroid.task.UnFlagMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.widget.NewEmailDialogFragment;

import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MessagesEmailContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;
import intellibitz.intellidroid.service.EmailService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 */
public class ComposeReplyEmailFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        DeleteMsgsTask.DeleteMsgsTaskListener,
        FlagMsgsTask.FlagMsgsTaskListener,
        UnFlagMsgsTask.UnFlagMsgsTaskListener,
        CreateDraftTask.CreateDraftTaskListener {

    private static final String TAG = "ClutterEmailFrag";
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
    private ContentObserver messageThreadContentObserver;
    private ContentObserver attachmentContentObserver;
    private Button btnSend;
    private Button btnDraft;
    private EditText etMessageInput;
    private ImageButton ivAttach;
    private ImageButton btnAudio;
    private ImageButton btnCamera;
    private ImageButton btnCall;
    private ImageButton btnSms;
    private RecyclerView rvMessages;
    private RecyclerView.Adapter rvMessagesAdapter;
    //    private MarkReadTask markReadTask;
    private DeleteMsgsTask deleteMsgsTask;
    private FlagMsgsTask flagMsgsTask;
    private UnFlagMsgsTask unflagMsgsTask;
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
    private ClutterMessageListener emailMessageListener;
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
    private ActionMode mActionMode;
    //    private Toolbar detailToolbar;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_context_chat_details, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    deleteMessages();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_forward:
                    forwardMessages();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_addtonest:
//                    forwardMessagesToNest();
                    broadcastMessagesToNest();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_flag:
                    flagMessages();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_unflag:
                    unflagMessages();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
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
    public ComposeReplyEmailFragment() {
        super();
    }

    public static ComposeReplyEmailFragment newInstance(
            MessageItem messageItem, ContactItem user, ClutterListener clutterListener) {
        ComposeReplyEmailFragment fragment = new ComposeReplyEmailFragment();
        fragment.setUser(user);
        fragment.setMessageItem(messageItem);
        if (clutterListener instanceof ClutterMessageHeaderListener) {
            fragment.setEmailMessageHeaderListener((ClutterMessageHeaderListener) clutterListener);
        }
        if (clutterListener instanceof ClutterMessageListener) {
            fragment.setEmailMessageListener((ClutterMessageListener) clutterListener);
        }
        if (clutterListener != null)
            fragment.setViewModeListener(clutterListener);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(MessageItem.TAG, messageItem);
        fragment.setArguments(args);
        return fragment;
    }

    public void setEmailMessageListener(ClutterMessageListener emailMessageListener) {
        this.emailMessageListener = emailMessageListener;
    }

    public void setEmailMessageHeaderListener(ClutterMessageHeaderListener emailMessageHeaderListener) {
        this.emailMessageHeaderListener = emailMessageHeaderListener;
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
        createRecyclerAdapter();
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
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        outState.putParcelable(MessageItem.TAG, messageItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        doUnbindService();
        getAppCompatActivity().getContentResolver().unregisterContentObserver(
                messageThreadContentObserver);
        getAppCompatActivity().getContentResolver().unregisterContentObserver(
                attachmentContentObserver);
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (emailMessageListener != null) {
            emailMessageListener.onEmailMessageTypingStopped(messageItem.getName());
        }
        messageThreadContentObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
//                clears the text of previous message.. it has already been send and received now
                if (null == etMessageInput && getView() != null)
                    etMessageInput = (EditText) getView().findViewById(R.id.tv_chat_message);
                if (etMessageInput != null) {
                    etMessageInput.setEnabled(true);
                    etMessageInput.setText("");
                }
//                toggleNewMessageView("");
                restartLoader();
/*
                if (getAppCompatActivity() != null) {
                    try {
                        if (ContentUris.parseId(uri) > 0) {
//                    // TODO: 02-04-2016
//                    view to be synced with
//                if (!uri.getLastPathSegment().contains(DatabaseHelper.TABLE_MESSAGE)) {
                            Cursor cursor = getAppCompatActivity().getContentResolver().query(
                                    uri, null, null, null, null);
                            if (null == cursor) return;
                            if (cursor.getCount() > 0) {
                                MessageItem messageItem = new MessageItem();
                                MessageContentProvider.createsMessageItemFromCursor(
                                        messageItem, cursor);
                                cursor.close();
//                                createRecyclerAdapter(messageItem);
                                createRecyclerAdapter();
                            }
                        }
                    } catch (NumberFormatException e) {
//                        ignore e
                    }
                }
*/
            }
        };
        getAppCompatActivity().getContentResolver().registerContentObserver(
                MessageEmailContentProvider.CONTENT_URI, true, messageThreadContentObserver);
        attachmentContentObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                restartLoader();
            }
        };
        getAppCompatActivity().getContentResolver().registerContentObserver(
                MsgEmailAttachmentContentProvider.CONTENT_URI, true,
                attachmentContentObserver);
    }

    private void quitLooperThread() {
        if (looperThread != null)
            looperThread.quit();
        sendMessage(messageItem, user);
        broadcastMessagesSend(messageItem);
    }

    private void quitDraftLooperThread() {
        if (draftLooperThread != null)
            draftLooperThread.quit();
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
        return inflater.inflate(R.layout.fragment_composereplyemail, container, false);
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

        etMessageInput = (EditText) view.findViewById(R.id.et_message);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnDraft = (Button) view.findViewById(R.id.btn_draft);
        ivAttach = (ImageButton) view.findViewById(R.id.iv_attach);
        btnAudio = (ImageButton) view.findViewById(R.id.ib_audio);
        btnCamera = (ImageButton) view.findViewById(R.id.ib_camera);
        btnVideo = (ImageButton) view.findViewById(R.id.ib_video);
        btnCall = (ImageButton) view.findViewById(R.id.ib_call);
        btnSms = (ImageButton) view.findViewById(R.id.ib_sms);
        rvMessages = (RecyclerView) view.findViewById(R.id.rv_chat_messages);
        if (null == savedInstanceState) {
            etMessageInput.requestFocus();
            etMessageInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    toggleNewMessageView(s);
                    sendOnKey(messageItem, user);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            btnDraft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etMessageInput.setEnabled(false);
                    String message = etMessageInput.getText().toString();
                    if (TextUtils.isEmpty(message)) {
                        return;
                    }
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
                                    messageItem.setChatId(ComposeReplyEmailFragment.this.messageItem.getChatId());
                                    messageItem.setDataId(ComposeReplyEmailFragment.this.messageItem.getChatId());
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
                            ComposeReplyEmailFragment.this.quitDraftLooperThread();
                        }
                    });
                    etMessageInput.setEnabled(true);
                    etMessageInput.setText("");
                }
            });
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etMessageInput.setEnabled(false);
//                    // TODO: 22-05-2016
//                    to mainitain input Q, the user can type and hit send madly like a million times
                    String message = etMessageInput.getText().toString();
                    if (TextUtils.isEmpty(message)) {
                        return;
                    }
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
                            ComposeReplyEmailFragment.this.quitLooperThread();
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
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCallPicker();
                }
            });
            btnSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSmsPicker();
                }
            });

        }
/*
        if (messageItem != null) {
//            restartLoader();
            toggleNewMessageView("");
        }
*/
        setChatHeaders(messageItem);
        restartLoader();
    }

    public boolean onBackPressed() {
        return false;
    }

    private void setupAppBar() {
        toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu_title_email);
        toolbar.setSubtitle(R.string.app_title);
        getAppCompatActivity().setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getAppCompatActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private Toolbar setupDetailToolbar() {
//        detailToolbar = getAppCompatActivity().getDetailToolbar();
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewMessageDialog();
            }
        });

        return toolbar;
    }

    private void setChatHeaders(MessageItem messageItem) {
        Toolbar toolbar = setupDetailToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_dialog_close_light);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performNavigationClose();
                }
            });
            setupDetailTitle(messageItem, toolbar);
        }
    }

    private void performNavigationClose() {
        getAppCompatActivity().onBackPressed();
    }

    private void setupDetailTitle(MessageItem messageItem, Toolbar toolbar) {
        if (null == messageItem) {
            Log.e(TAG, "MessageThread NULL");
            return;
        }
        title = messageItem.getSubject();
        if (null == title || 0 == title.length()) {
            ContactItem deviceContactItem = messageItem.getSharedDeviceContactItem();
            if (deviceContactItem != null) {
                title = deviceContactItem.getName();
                if (null == title || 0 == title.length()) {
                    title = deviceContactItem.getFirstName();
                }
                if (null == title || 0 == title.length()) {
                    title = deviceContactItem.getLastName();
                }
            }
            if (null == title || 0 == title.length()) {
                title = messageItem.getChatId();
            }
            if (null == title || 0 == title.length()) {
                title = messageItem.getDocOwner();
            }
        }
        String from = messageItem.getDocSender();
        if (null == from || 0 == from.length()) {
            from = messageItem.getFrom();
        }
        subTitle = "last seen on";
/*
            String recipients = MessageItem.getSingleAddress(messageItem.getCc(),
                    messageItem.getTo(), from, user.getEmail(), user.getName());
*/
        ContactItem contactItem = messageItem.getContactItem();
        if (contactItem != null) {
            String[] names = contactItem.getContactsAsArray();
            if (null == names || 0 == names.length) {
                subTitle = "last seen on";
            } else {
                subTitle = "";
                for (String name : names) {
                    if (name != null && !subTitle.isEmpty() && !subTitle.contains(name)) {
                        subTitle += ",";
                    }
                    subTitle += name;
                }
            }
        }
        toolbar.setTitle(title);
        toolbar.setSubtitle(subTitle);
    }

    @Nullable
    private Toolbar setMessageHeaders(MessageItem messageItem) {
        Toolbar toolbar = setupDetailToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_dialog_close_light);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performNavigationClose();
                }
            });
            String from = messageItem.getDocSender();
            if (null == from) {
                from = messageItem.getFrom();
            }
            String recipients = MessageItem.getSingleAddress(messageItem.getCc(),
                    messageItem.getTo(), from, user.getEmail(), user.getName());
            toolbar.setTitle(messageItem.getSubject());
            toolbar.setSubtitle(recipients);
        }
        return toolbar;
    }

    private void restartLoader(Uri uri) {
        try {
            long id = ContentUris.parseId(uri);
//            empties the list..
//            // TODO: 06-04-2016
//            to update only the changed item
            restartLoader(id);
        } catch (NumberFormatException e) {
//            ignore
        }
    }

    private void restartLoader(long id) {
        Cursor cursor = getAppCompatActivity().getContentResolver().query(
                Uri.withAppendedPath(MsgEmailAttachmentContentProvider.CONTENT_URI, String.valueOf(id)),
                new String[]{MessageItemColumns.KEY_DOWNLOAD_URL},
                MessageItemColumns.KEY_ID,
                new String[]{String.valueOf(id)}, null);
        if (null == cursor) return;
        if (cursor.getCount() > 0) {
            MessageItem attachmentItem = messageItem.getAttachment(id);
//            // TODO: 18-05-2016
//            null check
            if (attachmentItem != null) {
                attachmentItem.setDownloadURL(cursor.getString(cursor.getColumnIndex(
                        MessageItemColumns.KEY_DOWNLOAD_URL)));
//            rvMessagesAdapter.notifyDataSetChanged();
                restartLoader();
            }
            cursor.close();
        }
    }

    private void restartLoader(String query) {
        this.filter = query;
        restartLoader();
    }

    private void restartLoader() {
        if (messageItem != null) {
            MessageItem last = messageItem.peekMessageInStack();
            if (last != null && MessageItem.TAG.equals(last.getDataId())) {
                selfTypingMessage = last;
            }
        }
        try {
            getAppCompatActivity().getSupportLoaderManager().restartLoader(
                    MainApplicationSingleton.CLUTTEREMAIL_LOADERID, null, this);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            Log.e(TAG, TAG + ignored.getMessage());
        }
    }

    private void createRecyclerAdapter() {
        if (null == rvMessages && getView() != null)
            rvMessages = (RecyclerView) getView().findViewById(R.id.rv_email_messages);
        if (null == rvMessages) {
            Log.e(TAG, "createRecyclerAdapter: Recycler view NULL");
            return;
        }
        Collection<MessageItem> messages2 = messageItem.getMessages();
        if (selfTypingMessage != null) {
            try {
                messageItem.addMessage(selfTypingMessage);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        List<MessageItem> adapterList = new ArrayList<>(messages2);
//            sorts messages by asc.. so the latest msg is at the bottom
        Collections.sort(adapterList, new MessageItem.MessageItemComparator());

        rvMessagesAdapter = new RecyclerViewAdapter(adapterList);
        rvMessagesAdapter.setHasStableIds(true);
        rvMessages.setAdapter(rvMessagesAdapter);
        rvMessages.scrollToPosition(rvMessagesAdapter.getItemCount() - 1);
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

    private void openCallPicker() {
        startCallActivityOnPermissions();
    }

    private void openSmsPicker() {
        startSMSActivity();
    }

    private void startCallActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                PermissionChecker.checkSelfPermission(
                        getActivity(), Manifest.permission.CALL_PHONE) !=
                        PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                    MainApplicationSingleton.REQUEST_CALL_PHONE_PERMISSION);
        } else {
            startCallActivity();
        }
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

    private void startSMSActivity() {
        ContactItem deviceContactItem = messageItem.getSharedDeviceContactItem();
        MessageItem message = messageItem.peekMessageInStack();
        if (deviceContactItem != null && message != null) {
            String text = message.getText();
            if (text != null && text.length() > 0) {
                int length = text.length();
//                Set<MobileItem> mobiles = deviceContactItem.getMobiles();
                JSONArray mobiles = deviceContactItem.getMobiles();
//                MobileItem phone = mobiles.iterator().next();
                String phone = null;
                try {
                    phone = mobiles.getString(0);
                    if (phone != null && !phone.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
//                sms will make any apps respond
//                  BUT smsto ensures only SMS apps respond
                        intent.setData(Uri.parse("smsto:" + phone));
//                send only the first 120 chars
                        int len = (length > 120) ? 120 : length;
                        intent.putExtra("sms_body", text.substring(0, len));
//                intent.putExtra(Intent.EXTRA_STREAM, attachment);
                        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startCallActivity() {
        ContactItem deviceContactItem = messageItem.getSharedDeviceContactItem();
        if (deviceContactItem != null) {
//            Set<MobileItem> mobiles = deviceContactItem.getMobiles();
            JSONArray mobiles = deviceContactItem.getMobiles();
            String phone = null;
            try {
                phone = mobiles.getString(0);
                if (phone != null && !phone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phone));
                    if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                createRecyclerAdapter();
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

    private void execDeleteMsgsTask(String[] msgs) {
        DeleteMsgsTask deleteMsgsTask = new DeleteMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_DELETED_MSGS, getContext());
        deleteMsgsTask.setRequestTimeoutMillis(30000);
        deleteMsgsTask.setDeleteMsgsTaskListener(this);
        deleteMsgsTask.execute();
/*
        deleteMsgsTask = new DeleteMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_DELETED_MSGS);
        deleteMsgsTask.setDeleteMsgsTaskListener(this);
        deleteMsgsTask.execute();
*/
    }

    private void execFlagMsgsTask(String[] msgs) {
        flagMsgsTask = new FlagMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_FLAG_MSGS);
        flagMsgsTask.setFlagMsgsTaskListener(this);
        flagMsgsTask.execute();
    }

    private void execUnFlagMsgsTask(String[] msgs) {
        unflagMsgsTask = new UnFlagMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_UNFLAG_MSGS);
        unflagMsgsTask.setUnFlagMsgsTaskListener(this);
        unflagMsgsTask.execute();
    }

    private void deleteMessages() {
        execDeleteMsgsTask(new String[]{selectedItem.getDataId()});
    }

    private void flagMessages() {
        execFlagMsgsTask(new String[]{selectedItem.getDataId()});
    }

    private void unflagMessages() {
        execUnFlagMsgsTask(new String[]{selectedItem.getDataId()});
    }

    private void forwardMessages() {
        IntellibitzActivity intellibitzActivity = removeSelf();
        if (intellibitzActivity == null) return;
//        this.onDestroy();
//        intellibitzActivity.onBackPressed(intent);
        Intent intent = new Intent();
        intent.setAction(ComposeReplyEmailFragment.TAG);
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

    @Override
    public void onPostUnFlagMsgsExecute(JSONObject response, MessageItem[] mids, String[] item) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "UnFlag Msgs SUCCESS - " + response);
            } else if (-1 == status) {
                onPostUnFlagMsgsExecute(response, mids, item);
            } else if (99 == status) {
                onPostUnFlagMsgsExecute(response, mids, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostUnFlagMsgsExecuteFail(JSONObject response, MessageItem[] mids, String[] item) {
        Log.e(TAG, "UnFlag Msgs FAIL - " + response + ": " + Arrays.toString(item));
    }

    @Override
    public void setUnFlagMsgsTaskToNull() {
        unflagMsgsTask = null;
    }

    @Override
    public void onPostFlagMsgsExecute(JSONObject response, MessageItem[] mids, String[] item) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "Flag Msgs SUCCESS - " + response);
            } else if (-1 == status) {
                onPostFlagMsgsExecuteFail(response, mids, item);
            } else if (99 == status) {
                onPostFlagMsgsExecuteFail(response, mids, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostFlagMsgsExecuteFail(JSONObject response, MessageItem[] mids, String[] item) {
        Log.e(TAG, "Flag Msgs FAIL - " + response + ": " + Arrays.toString(item));
    }

    @Override
    public void setFlagMsgsTaskToNull() {
        flagMsgsTask = null;
    }

    @Override
    public void onPostDeleteMsgsResponse(JSONObject response, String[] item) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "Delete Msgs SUCCESS - " + response);
                deleteMessagesInDB(item);
            } else if (-1 == status) {
                onPostDeleteMsgsErrorResponse(response, item);
            } else if (99 == status) {
                onPostDeleteMsgsErrorResponse(response, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void deleteMessagesInDB(String[] item) {
        try {
            int count = MessageEmailContentProvider.deleteMsgs(item, getContext());
            Log.e(TAG, "Delete in DB: " + count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostDeleteMsgsErrorResponse(JSONObject response, String[] item) {
        Log.e(TAG, "Delete Msgs FAIL - " + response + ": " + Arrays.toString(item));
//        deleteMessagesInDB(item);
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
        restartLoader(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        restartLoader(newText);
        return false;
    }

    @Override
    public boolean onClose() {
        restartLoader("");
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        CursorLoader cursorLoader = null;
        if (MainApplicationSingleton.CLUTTEREMAIL_LOADERID == id) {
//===========================================================================
//            IMPORTANT
//  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
// MT AS PREFIX for message thread IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
//            M AS prefix for message IS REQUIRED
//===========================================================================
            //        // TODO: 13-03-2016
//        loads the complete message thread, with all messages and all attachments
//        handles new
            if (messageItem != null &&
                    !MessageItem.TAG.equals(messageItem.getDataId()) &&
                    !TextUtils.isEmpty(messageItem.getDataId())) {

//                check for group contact joins..
                String selection = " ( " +
                        "m." + MessageItemColumns.KEY_TEXT +
                        " IS NULL OR " +
                        "m." + MessageItemColumns.KEY_TEXT +
                        " like ? ) AND mt." +
                        MessageItemColumns.KEY_DATA_ID + " = ? ";
                String[] selArgs;
                if (filter != null && !filter.isEmpty()) {
                    selArgs = new String[]{"%" + filter + "%", messageItem.getDataId()};
                } else {
                    selArgs = new String[]{"%%", messageItem.getDataId()};
                }
                String sortOrder = "mt." +
                        MessageItemColumns.KEY_TIMESTAMP + " ASC";
                Uri uri = Uri.withAppendedPath(
//                        MessageEmailContentProvider.JOIN_CONTENT_URI,
                        MessagesEmailContentProvider.JOIN_CONTENT_URI,
                        Uri.encode(messageItem.getDataId()));
                cursorLoader = new CursorLoader(getAppCompatActivity(),
                        uri, null, selection, selArgs, sortOrder);
            }
        } else {
//        returns an empty dummy cursor.. for the cursor loader to play game
            cursorLoader = new CursorLoader(getAppCompatActivity(),
                    Uri.withAppendedPath(MessagesEmailContentProvider.CONTENT_URI, "0"),
                    null,
                    MessageItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{"0"}, null);

        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.CLUTTEREMAIL_LOADERID == loader.getId()) {
            if (null == cursor) {
                if (null == messageItem) {
                    Log.e(TAG, "onLoadFinished: message thread is NULL");
                    return;
                }
                messageItem.getMessages().clear();
                createRecyclerAdapter();
                return;
            }
            int count = cursor.getCount();
            if (0 == count) {
                cursor.close();
                messageItem.getMessages().clear();
                createRecyclerAdapter();
                return;
            }
            if (count > 0 && 0 == cursor.getPosition()) {
                fillItemsFromLoader(cursor);
                cursor.close();
                createRecyclerAdapter();
            }
        }
    }

    public void fillItemsFromLoader(Cursor cursor) {
        try {
            messageItem.getMessages().clear();
/*
            MessageEmailContentProvider.fillMessagesFromAllJoinCursor(
                    messageItem, cursor);
*/
            MessageEmailContentProvider.fillMessageItemFromAllJoinCursor(
                    messageItem, cursor);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.CLUTTEREMAIL_LOADERID == loader.getId()) {
            messageItem.getMessages().clear();
            createRecyclerAdapter();
        }
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

    public Rfc822Token[] getBcc() {
        return Rfc822Tokenizer.tokenize(String.valueOf(bcc.getText()));
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

    private void performNewEmail() {
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "performNewEmail: User email is empty - No email account signed up");
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

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EmailService.MSG_SET_VALUE:
                    restartLoader();
/*
                    getAppCompatActivity().getSupportLoaderManager().restartLoader(
                            MainApplicationSingleton.CLUTTEREMAIL_LOADERID, null,
                            ClutterEmailFragment.this);
                    rvMessages.notify();
*/
                    break;
                case EmailService.MSG_SHOW_TYPING:
                    if (emailMessageListener != null) {
                        try {
                            String obj = (String) msg.obj;
                            if (null != obj) {
                                JSONArray jsonArray = new JSONArray(obj);
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                try {
                                    String name = jsonObject.getString("from_name");
                                    emailMessageListener.onEmailMessageTyping(name + " is typing..");
//                                setToolBarShowTyping(name + " is typing..");
                                    final Handler delay = new Handler();
                                    delay.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
//                                        setToolBarSubTitle(toolbarSubTitle);
                                            emailMessageListener.onEmailMessageTypingStopped("");
                                        }
                                    }, 1000);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public class RecyclerViewAdapter extends
            RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements
            BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private List<MessageItem> adapterItems;
        private BitmapFromUrlTask bitmapFromUrlTask;
        private HandlerThread looperThread;

        public RecyclerViewAdapter(List<MessageItem> items) {
            adapterItems = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_email_msg, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = adapterItems.get(position);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                holder.mView.setAlpha(1f);
            }
            holder.tvFromName.setVisibility(View.VISIBLE);

//            handles new message
            if (MessageItem.TAG.equals(holder.mItem.getDataId())) {
                holder.tvSub.setVisibility(View.VISIBLE);
                holder.tvTo.setVisibility(View.VISIBLE);
                holder.tvCc.setVisibility(View.VISIBLE);
                holder.tvBcc.setVisibility(View.VISIBLE);

                holder.tvSub.setText(holder.mItem.getSubject());
                holder.tvTo.setText(holder.mItem.getTo());
                holder.tvCc.setText(holder.mItem.getCc());
                holder.tvBcc.setText(holder.mItem.getBcc());

                holder.tvSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNewMessageDialog();
                    }
                });
                holder.tvTo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNewMessageDialog();
                    }
                });
                holder.tvCc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNewMessageDialog();
                    }
                });
                holder.tvBcc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNewMessageDialog();
                    }
                });
            }
//            binds the message
            TextDrawable textDrawable = ColorGenerator.getTextDrawable(holder.mItem.getFromName());
            textDrawable.setBounds(new Rect(0, 0, 60, 60));
            setCompoundDrawablesRelative(holder.tvFromName,
                    textDrawable, null, null, null);
            holder.tvFromName.setText(holder.mItem.getFromName());
//                    converts to milliseconds, for correct date conversion
            long createDate = holder.mItem.getTimestamp() * 1000;
            Timestamp timestamp = new Timestamp(createDate);
            long now = System.currentTimeMillis();
            DateFormat df = new SimpleDateFormat("MMM dd", Locale.getDefault());
            if ((now - createDate) < 1000 * 60 * 60 * 24) {
                df = new SimpleDateFormat("hh mm a", Locale.getDefault());
            }
            String dt = df.format(timestamp.getTime());
            holder.tvFromName.setText(holder.tvFromName.getText() + "  @" + dt);

            if (holder.mItem.isDelivered()) {
                Drawable drawable = getDrawable(R.drawable.ic_done_all_black_18dp,
                        getAppCompatActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 20, 20));
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, null);
            } else if (holder.mItem.isRead()) {
                Drawable drawable = getDrawable(R.drawable.ic_done_black_18dp,
                        getAppCompatActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 20, 20));
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, null);
            } else {
                Drawable drawable = getDrawable(R.drawable.ic_restore_black_18dp,
                        getAppCompatActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 20, 20));
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, null);
            }
            holder.tvMessage.setText(holder.mItem.getText());

            ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
            if (user.getName().equals(holder.mItem.getFromName())) {
//                    ||
//                    MessageItem.TAG.equals(holder.userEmailItem.getDataId()))
//            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    holder.tvFromName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                    holder.tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                }
            }
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.list_item_email_attch_msg);
//            clears old views
            linearLayout.removeAllViews();

            if (MessageItem.TAG.equals(holder.mItem.getDataId())) {
//            handles new message
//            new message has the message thread item class name as an hack to indicate new

                if (holder.mItem.getAttachments().isEmpty()) {
/*
                    holder.mView.setBackground(getDrawable(android.R.color.transparent
                            , getContext().getTheme()));
*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        holder.mView.setAlpha(1f);
                    }
                    holder.mView.setVisibility(View.GONE);
                    return;
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        holder.mView.setAlpha(0.5f);
                    }
                    holder.tvFromName.setVisibility(View.GONE);
                    holder.tvMessage.setText("Attachment preview - ");
/*
                    holder.mView.setBackground(getDrawable(R.color.graylte
                            , getContext().getTheme()));
*/
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    linearLayout.setBackground(getDrawable(R.color.red
                            , getContext().getTheme()));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    linearLayout.setBackground(getDrawable(android.R.color.transparent
                            , getContext().getTheme()));
                }
            }

//            binds the attachments for each message
            Set<MessageItem> attachmentItems = holder.mItem.getAttachments();
            for (final MessageItem attachmentItem : attachmentItems) {
                View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                        R.layout.list_item_email_attch_msg,
                        (ViewGroup) holder.mView.getRootView(), false);
                TextView tv = (TextView) view.findViewById(R.id.tv_attach_name);
                tv.setText(attachmentItem.getName());
                final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);

/*
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                byte[] rawArt;
                Bitmap bm = null;
                BitmapFactory.Options bfo = new BitmapFactory.Options();

                mmr.setDataSource(attachmentItem.getDownloadURL());
                rawArt = mmr.getEmbeddedPicture();

// if rawArt is null then no cover art is embedded in the file or is not
// recognized as such.
                if (null != rawArt)
                    bm = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
*/
/*
                if (null == bm)
*/
//                show preview attachments, and open to view
                String downloadURL = attachmentItem.getDownloadURL();
                if (null == downloadURL || downloadURL.isEmpty()) {

                    Log.e(TAG, "Attachment Download URL is NULL ");
                } else if (downloadURL.startsWith("/")) {
                    progressBar.setVisibility(View.GONE);
                    final File file = new File(downloadURL);
                    String extension = MimeTypeMap.getFileExtensionFromUrl(
                            Uri.fromFile(file).toString());
                    final String mimetype =
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                    Bitmap bm;
                    Drawable drawable;
                    String type = attachmentItem.getType();
                    if (type != null && type.contains("image") ||
                            (mimetype != null && mimetype.contains("image"))) {
                        bm = BitmapFactory.decodeFile(downloadURL);
                        drawable = new BitmapDrawable(getResources(), bm);
                    } else if (type != null && type.contains("video") ||
                            (mimetype != null && mimetype.contains("video"))) {
                        bm = ThumbnailUtils.createVideoThumbnail(
                                downloadURL, MediaStore.Video.Thumbnails.MINI_KIND);
                        drawable = new BitmapDrawable(getResources(), bm);
                    } else if (type != null && type.contains("audio") ||
                            (mimetype != null && mimetype.contains("audio"))) {
                        drawable = getDrawable(android.R.drawable.ic_media_play,
                                getAppCompatActivity().getTheme());
                    } else if (type != null && type.contains("application") ||
                            (mimetype != null && mimetype.contains("application"))) {
                        drawable = getDrawable(android.R.drawable.ic_menu_slideshow,
                                getAppCompatActivity().getTheme());
                    } else {
                        drawable = ColorGenerator.getTextDrawable(attachmentItem.getName());
                    }
                    assert drawable != null;
                    drawable.setBounds(new Rect(0, 0, 100, 100));
                    setCompoundDrawablesRelative(tv, drawable, null, null, null);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startMimeActivity(file,
                                    MsgEmailAttachmentContentProvider.getMimeType(attachmentItem));
                        }
                    });
                } else if (downloadURL.startsWith("http")) {
                    if (holder.mItem.getFromUid().equalsIgnoreCase(user.getDataId())) {
//                        if self message, download immediately
//                    cloud pic.. not always a pic.. so image download is not always applicable
/*
                        bitmapFromUrlTask = new BitmapFromUrlTask(
                                tv, downloadURL, getAppCompatActivity().getApplicationContext());
                        bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                        bitmapFromUrlTask.execute();
*/
                        looperThread =
                                MainApplicationSingleton.performOnHandlerThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                                                getAppCompatActivity(), user, attachmentItem);
                                        RecyclerViewAdapter.this.quitLooperThread();
                                    }
                                });
                    } else {
                        Drawable drawable = getDrawable(R.drawable.ic_get_app_black_18dp,
                                getAppCompatActivity().getTheme());
                        assert drawable != null;
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        setCompoundDrawablesRelative(tv, drawable, null, null, null);
                        String val = attachmentItem.getType() + " " + attachmentItem.getSize();
                        tv.setText(val);
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "Fetching attachment in background :" + attachmentItem.getDataId());
                                progressBar.setVisibility(View.VISIBLE);
                                MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                                        getAppCompatActivity(),
                                        user, attachmentItem);
/*
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    holder.progress.setProgress(100);
                                }
                            }, 500);
*/
//                                    new DownloadReceiver(new Handler()));
//                            startMimeActivity(file);
                            }
                        });
                    }
                }
                linearLayout.addView(view);
            }
        }

        @Override
        public long getItemId(int position) {
            return adapterItems.get(position).get_id();
        }

        @Override
        public int getItemCount() {
            return adapterItems.size();
        }

        private void quitLooperThread() {
            if (looperThread != null)
                looperThread.quit();
        }

        @Override
        public void onPostBitmapFromUrlExecute(Bitmap bitmap, View textView, Context context) {
            if (null == context) {
                context = getContext();
            }
            if (null == context) return;
            Resources resources = context.getResources();
            if (null == resources) return;
//            Bitmap roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
            BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
            drawable.setBounds(new Rect(0, 0, 100, 100));
            setCompoundDrawablesRelative(((TextView) textView), drawable, null, null, null);
        }

        @Override
        public void onPostBitmapFromUrlExecuteFail(Bitmap bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: " + bitmap);
        }

        @Override
        public void setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null;
        }

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnLongClickListener {
            //            the views
            public final View mView;
            public final TextView tvSub;
            public final TextView tvTo;
            public final TextView tvCc;
            public final TextView tvBcc;
            public final TextView tvFromName;
            public final TextView tvMessage;
            //            the data
            public MessageItem mItem;
//            public final ProgressBar progress;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                view.setOnLongClickListener(this);
                tvSub = (TextView) view.findViewById(R.id.tv_sub);
                tvTo = (TextView) view.findViewById(R.id.tv_to);
                tvCc = (TextView) view.findViewById(R.id.tv_cc);
                tvBcc = (TextView) view.findViewById(R.id.tv_bcc);
                tvFromName = (TextView) view.findViewById(R.id.tv_from_name);
                tvMessage = (TextView) view.findViewById(R.id.tv_msg);
//                progress = (ProgressBar) view.findViewById(R.id.progress);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + tvMessage.getText() + "'";
            }

            @Override
            public boolean onLongClick(View v) {
                setSelectedItem(mItem);
                if (null == mActionMode) {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = getAppCompatActivity().startSupportActionMode(mActionModeCallback);
//                view.setSelected(true);
                    v.setSelected(!v.isSelected());
                    return true;
                }
                v.setSelected(!v.isSelected());
                return false;
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

