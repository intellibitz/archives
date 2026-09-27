package intellibitz.intellidroid.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Rfc822Token;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.PeopleDetailListener;
import intellibitz.intellidroid.listener.PeopleHeaderListener;
import intellibitz.intellidroid.listener.PeopleListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateDraftTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.task.FlagMsgsTask;
import intellibitz.intellidroid.task.MarkReadTask;
import intellibitz.intellidroid.task.UnFlagMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.content.task.FetchPeopleDetailTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.PeopleDetailListener;
import intellibitz.intellidroid.listener.PeopleHeaderListener;
import intellibitz.intellidroid.listener.PeopleListener;
import intellibitz.intellidroid.service.ChatEmailService;
import intellibitz.intellidroid.service.ChatService;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateDraftTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.task.FlagMsgsTask;
import intellibitz.intellidroid.task.MarkReadTask;
import intellibitz.intellidroid.task.UnFlagMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerFile;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.widget.NewEmailDialogFragment;

import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.service.ChatEmailService;
import intellibitz.intellidroid.service.ChatService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 */
public class PeopleDetailFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        View.OnClickListener,
        ContactListener,
        PeopleDetailListener,
        MarkReadTask.MarkReadTaskListener,
        DeleteMsgsTask.DeleteMsgsTaskListener,
        FlagMsgsTask.FlagMsgsTaskListener,
        UnFlagMsgsTask.UnFlagMsgsTaskListener,
        CreateDraftTask.CreateDraftTaskListener,
        NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
        FetchPeopleDetailTask.FetchMsgsGrpPeopleDetailTaskListener {

    public static final String TAG = "PeopleDetailFrag";
    /**
     * The fragment argument representing the messageItem ID that this fragment
     * represents.
     */
    public static final String ARG_DATA_ID = "id";
    public static final String ARG_TOPIC_ID = "item_id";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final Object lock = new Object();
    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
    /**
     * Messenger for communicating with service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;
    private Uri cameraPhotoFileUri;
    private Uri videoPhotoFileUri;
    private Uri audioFileUri;
    private ContentObserver messageThreadContentObserver;
    private ContentObserver attachmentContentObserver;
    private ImageButton btnSend;
    private ImageButton btnUpload;
    private View llUpload;
    private Button btnDraft;
    private EditText etMessageInput;
    private ImageButton ivAttach;
    private ImageButton btnAudio;
    private ImageButton btnCamera;
    private ImageButton btnCall;
    private ImageButton btnSms;
    private RecyclerView rvMessages;
    private RecyclerView.Adapter rvMessagesAdapter;
    private MarkReadTask markReadTask;
    private DeleteMsgsTask deleteMsgsTask;
    private FlagMsgsTask flagMsgsTask;
    private UnFlagMsgsTask unflagMsgsTask;
    private FetchPeopleDetailTask fetchPeopleDetailTask;
    private MessageItem messageItem;
    private MessageItem selectedItem;
    private MessageItem selfTypingMessage;
    //    private String id;
//    private String topic;
    private Context c;
    private String title;
    private String subTitle;
    private String filter;
    private ImageButton btnVideo;
    private HandlerThread looperThread;
    private CreateDraftTask createDraftTask;
    private HandlerThread draftLooperThread;
    private PeopleListener peopleListener;
    private PeopleDetailListener peopleDetailListener;
    private PeopleHeaderListener peopleHeaderListener;
    //    private Toolbar detailToolbar;
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
                        ChatService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.
                msg = Message.obtain(null,
                        ChatService.MSG_SET_VALUE, this.hashCode(), 0);
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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PeopleDetailFragment() {
        super();
    }

    public static PeopleDetailFragment newInstance(
            MessageItem messageItem, ContactItem user, PeopleListener peopleListener) {
        PeopleDetailFragment fragment = new PeopleDetailFragment();
        fragment.setUser(user);
        fragment.setMessageItem(messageItem);
        fragment.setPeopleListener(peopleListener);
        if (peopleListener instanceof PeopleHeaderListener)
            fragment.setPeopleHeaderListener((PeopleHeaderListener) peopleListener);
        if (peopleListener instanceof PeopleDetailListener)
            fragment.setPeopleDetailListener((PeopleDetailListener) peopleListener);
        if (peopleListener != null)
            fragment.setViewModeListener(peopleListener);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(MessageItem.TAG, messageItem);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPeopleHeaderListener(PeopleHeaderListener peopleHeaderListener) {
        this.peopleHeaderListener = peopleHeaderListener;
    }

    public void setPeopleDetailListener(PeopleDetailListener peopleDetailListener) {
        this.peopleDetailListener = peopleDetailListener;
    }

    private void showTyping(String toUid, String fromUid, String fromName, ContactItem user) {
        if (fromUid.equals(user.getDataId())) {
// if self is typing ignore..
//                shows typing only from other senders
        } else {
            onPeopleTyping(fromName + " is typing..");
//                                setToolBarShowTyping(name + " is typing..");
            final Handler delay = new Handler();
            delay.postDelayed(new Runnable() {
                @Override
                public void run() {
//                                        setToolBarSubTitle(toolbarSubTitle);
//                    name = null;
//                    restartLoader(null);
                    onPeopleTypingStopped("");
                }
            }, 1000);
        }
    }

    @Override
    public void onPeopleTyping(String text) {
        if (null == peopleDetailListener) {
// // TODO: 21-05-2016
//            listeners need to be saved, restored or recreated brand new every time
        } else {
            peopleDetailListener.onPeopleTyping(text);
//        getAppCompatActivity().getToolbar().setSubtitle(text);
//        toolbar.setSubtitleTextColor(Color.GREEN);
        }
    }

    @Override
    public void onPeopleTypingStopped(String text) {
        if (null == peopleDetailListener) {
// // TODO: 21-05-2016
//            listeners need to be saved, restored or recreated brand new every time
        } else {
            peopleDetailListener.onPeopleTypingStopped(text);
//        getAppCompatActivity().getToolbar().setSubtitle(text);
//        toolbar.setSubtitleTextColor(Color.WHITE);
        }
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        getAppCompatActivity().bindService(new Intent(getAppCompatActivity(),
                ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
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
                            ChatEmailService.MSG_UNREGISTER_CLIENT);
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

    private void sendOnKey(MessageItem messageItem, ContactItem user) {
        if (null == mService) {
            Log.e(TAG, "Service is NULL - cannot send on key message");
        } else {
            try {
                Message msg = Message.obtain(null,
                        ChatService.MSG_ON_KEY, this.hashCode(), 0);
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
                ChatService.MSG_SET_VALUE, this.hashCode(), 0);
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

    public void refreshChatMessageLoader(Bundle arguments) {
        restoreStateFromFragmentArguments(arguments);
        createRecyclerAdapter();
/*
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_TITLE, id);
        contentValues.put(DatabaseHelper.KEY_DESCRIPTION, message);
        getAppCompatActivity().getContentResolver().insert(
                ChatMessageContentProvider.CONTENT_URI, contentValues);
*/
    }

    public void refreshEmailMessageLoader(Bundle arguments) {
        restoreStateFromFragmentArguments(arguments);
        createRecyclerAdapter();
    }

    private void sendMessage(String id, String topic, String fromName, String message) {
        Message msg = Message.obtain(null,
                ChatService.MSG_SET_VALUE, this.hashCode(), 0);
        msg.obj = message;
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("topic", topic);
        args.putString("fromName", fromName);
        args.putString("msg", message);
        msg.setData(args);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setPeopleListener(PeopleListener peopleListener) {
        this.peopleListener = peopleListener;
    }

    /*
        private void restoreStateFromFragmentArguments(Bundle arguments) {
            if (null == arguments) {
                arguments = getArguments();
            }
            ContactItem auser = arguments.getParcelable(ContactItem.TAG);
            if (auser != null) {
                user = auser;
            }
            MessageItem amessageThreadItem =
                    arguments.getParcelable(MessageItem.TAG);
            if (amessageThreadItem != null) {
                messageItem = amessageThreadItem;
            }
        }

    */
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
        if (peopleDetailListener != null) {
            peopleDetailListener.onPeopleTypingStopped(messageItem.getName());
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
                MessageChatContentProvider.CONTENT_URI, true, messageThreadContentObserver);
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
                MsgChatAttachmentContentProvider.CONTENT_URI, true,
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
        Log.e(TAG, "onPostCreateDraftFromCloudExecute: " + response);
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
        return inflater.inflate(R.layout.fragment_peopledetail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAppBar();
        etMessageInput = (EditText) view.findViewById(R.id.tv_chat_message);
        btnSend = (ImageButton) view.findViewById(R.id.btn_chat_send);
        btnUpload = (ImageButton) view.findViewById(R.id.btn_upload);
        llUpload = view.findViewById(R.id.ll_upload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int visibility = llUpload.getVisibility();
                if (View.VISIBLE == visibility)
                    llUpload.setVisibility(View.GONE);
                else
                    llUpload.setVisibility(View.VISIBLE);
            }
        });
        btnDraft = (Button) view.findViewById(R.id.btn_draft);
        ivAttach = (ImageButton) view.findViewById(R.id.iv_chat_attach);
        btnAudio = (ImageButton) view.findViewById(R.id.ib_audio);
        btnCamera = (ImageButton) view.findViewById(R.id.ib_camera);
        btnVideo = (ImageButton) view.findViewById(R.id.ib_video);
        btnCall = (ImageButton) view.findViewById(R.id.ib_call);
        btnSms = (ImageButton) view.findViewById(R.id.ib_sms);
        rvMessages = (RecyclerView) view.findViewById(R.id.rv_chat_messages);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
            messageItem = getArguments().getParcelable(
                    MessageItem.TAG);
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
//                    MessageItem messageItem = messageItem.popMessage();
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
                                    messageItem.setType(MessageItem.CHAT);
                                    messageItem.setMessageType(MessageItem.CHAT);
                                    messageItem.setToType(MessageItem.USER);
                                    messageItem.setGroup(false);
                                    messageItem.setEmailItem(false);
                                    messageItem.setChatId(PeopleDetailFragment.this.messageItem.getChatId());
                                    messageItem.setDataId(PeopleDetailFragment.this.messageItem.getChatId());
//                                        // TODO: 19-06-2016
//                                        set chat_msg_ref from app
//                                        chat_msg_ref format: fromuid_touid_timestamp
                                    messageItem.setChatMsgRef(user.getDataId() +
                                            "_" + messageItem.getChatId() + "_" + System.currentTimeMillis());
/*
                                    createDraftTask = new CreateDraftTask(messageItem,
                                            user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                                            MainApplicationSingleton.AUTH_CREATE_DRAFT);
                                    createDraftTask.setCreateDraftTaskListener(PeopleDetailFragment.this);
                                    createDraftTask.execute();
*/
                                    broadcastMessagesToDraft(messageItem);
                                    messageItemConcurrentLinkedQueue.remove(messageItem);
                                }
                            }
                            PeopleDetailFragment.this.quitDraftLooperThread();
                        }
                    });
                    etMessageInput.setEnabled(true);
                    etMessageInput.setText("");
                }
            });

/*
                    messageItem.getMessageItemConcurrentLinkedQueue().add(messageItem);
                    looperThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
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
//                                    try {
*/
/*
                                        Uri uri = MessageContentProvider.savesMessageItem(
                                                messageItem, user, getContext());
                                        Log.d(TAG, "Message Draft saved: " + uri);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Message Draft save FAIL: " + messageItem);
                                    }
*//*

                                }
                            }
//        2nd loop, clears send messages
//        clears of all messages, send
                            for (MessageItem messageItem : messageItems) {
                                if (messageItem.isReadyToSend()) {
                                    messageItemConcurrentLinkedQueue.remove(messageItem);
                                }
                            }

                            ChatDetailFragment.this.quitLooperThread();
                        }

                    });
*/
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
                            ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
                                    messageItem.getMessageItemConcurrentLinkedQueue();
                            MessageItem[] messageItems;
                            synchronized (lock) {
                                messageItems = messageItemConcurrentLinkedQueue.toArray(new MessageItem[0]);
                            }
                            for (MessageItem messageItem : messageItems) {
                                if (messageItem.isReadyToSend()) {
                                    try {
                                        messageItem.setDocType(MessageItem.MSG);
                                        messageItem.setType(MessageItem.CHAT);
                                        messageItem.setMessageType(MessageItem.CHAT);
                                        messageItem.setToType(MessageItem.USER);
                                        messageItem.setGroup(false);
                                        messageItem.setEmailItem(false);
                                        messageItem.setChatId(PeopleDetailFragment.this.messageItem.getChatId());
                                        messageItem.setDataId(PeopleDetailFragment.this.messageItem.getChatId());
//                                        messageItem.setToType(PeopleDetailFragment.this.messageItem.getToType());
//                                        // TODO: 19-06-2016
//                                        set chat_msg_ref from app
//                                        chat_msg_ref format: fromuid_touid_timestamp
                                        messageItem.setChatMsgRef(user.getDataId() +
                                                "_" + messageItem.getChatId() + "_" +
                                                System.currentTimeMillis());
                                        MessageChatContentProvider.createsContactsFromMessage(
                                                messageItem);
                                        Uri uri = MessageChatContentProvider.savesMessageItem(
                                                messageItem, user, getContext());
                                        Log.d(TAG, "Message saved: " + uri);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Message save FAIL: " + messageItem);
                                    }
                                }
                            }
                            PeopleDetailFragment.this.quitLooperThread();
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

        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            messageItem = savedInstanceState.getParcelable(
                    MessageItem.TAG);
        }
        if (messageItem != null) {
//            set read as true, if the message is NOT new
            if (!MessageItem.TAG.equals(messageItem.getDataId())) {
                if (messageItem.getUnreadCount() > 0) {
                    execMarkReadTask(messageItem);
                } else {
                    Log.e(TAG, "No unread message - mark read is not invoked");
                }
            }
//            createRecyclerAdapter();
//            toggleNewMessageView("");
        }
        setChatHeaders(messageItem);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewModeListener != null) {
                    viewModeListener.onViewModeChanged();
                }
//                // TODO: 15-07-2016
//                to land in this contact page
                broadcastContactForProfile(messageItem);
//                startMsgChatGrpContactsDetailActivity(messageItem.getContactItem(), user);
/*
                msgChatGrpContactsDetailFragment =
                        GroupsDetailFragment.newInstance(PeopleDetailFragment.this,
                                getAppCompatActivity(), twoPane,
                                messageItem.getContactItem(), user);
                getAppCompatActivity().removeFragment(msgChatGrpContactsDetailFragment);
                getAppCompatActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
*/
            }
        });
        MessageItem messageItemToForward = messageItem.popMessage();
        if (messageItemToForward != null &&
                !MessageItem.TAG.equals(messageItemToForward.getDataId())) {
//            toggleNewMessageView(messageItemToForward.getText());
            etMessageInput.setText(messageItemToForward.getText());
            messageItemToForward = null;
        }
        if (messageItem.isDraft()) {
//            toggleNewMessageView(messageItemToForward.getText());
            etMessageInput.setText(messageItem.getText());
        }
        restartLoader();
        //        // TODO: 28-03-2016
//        already done in content fragment.. not required
/*
        getAppCompatActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.CHAT_MESSAGE_FRAGMENT_LOADERID, null,
                ChatDetailFragment.this);
*/
    }

    public void broadcastContactForProfile(MessageItem messageItem) {
        Intent intent =
                new Intent(MainApplicationSingleton.BROADCAST_CONTACT_PROFILE_VIEW);
        intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, messageItem.getIntellibitzId());
        LocalBroadcastManager.getInstance(getAppCompatActivity()).sendBroadcast(intent);
//                        finishes.. so doesn't show up in back stack
        getAppCompatActivity().finish();
    }

    private void setupAppBar() {
        toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_group);
        toolbar.setSubtitle(R.string.app_title);
        getAppCompatActivity().setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getAppCompatActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * You are calling startActivityForResult() from your Fragment. When you do this,
     * the requestCode is changed by the Activity that owns the Fragment.
     * If you want to get the correct resultCode in your activity try this:
     * Change:
     * startActivityForResult(intent, 1);
     * To:
     * getActivity().startActivityForResult(intent, 1);
     * Just a note: if you use startActivityForResult in a fragment and expect the result from
     * onActivityResult in that fragment, just make sure you call super.onActivityResult in the
     * host activity (in case you override that method there).
     * This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
     * Also, note that the request code, when it travels through the activity's onActivityResult,
     * is altered
     * "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
     */
    public void startMsgChatGrpContactsDetailActivity(ContactItem contactItem, ContactItem user) {
        Intent intent = new Intent(getAppCompatActivity(), MsgChatGrpContactsDetailActivity.class);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE);
/*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getAppCompatActivity(), twoPane,
                        selectedItem, user);
//        msgChatGrpContactsDetailFragment.onOkPressed(null);
        getAppCompatActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
        return msgChatGrpContactsDetailFragment;
*/
    }

    public boolean onBackPressed() {
        return false;
    }

    public void setChatHeaders(MessageItem messageItem) {
/*
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_dialog_close_light);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performNavigationClose();
                }
            });
        }
*/
        setupDetailTitle(messageItem, toolbar);
    }

    private void performNavigationClose() {
        getAppCompatActivity().onBackPressed();
    }

    private void setupDetailTitle(MessageItem messageItem, Toolbar toolbar) {
        if (null == messageItem) {
            Log.e(TAG, "MessageThread NULL");
            return;
        }
        title = messageItem.getName();
        if (TextUtils.isEmpty(title)) {
            title = messageItem.getDisplayName();
        }
        if (TextUtils.isEmpty(title)) {
            title = messageItem.getFirstName();
        }
        if (TextUtils.isEmpty(title)) {
            title = messageItem.getLastName();
        }
        if (TextUtils.isEmpty(title)) {
            ContactItem contactItem = messageItem.getSharedDeviceContactItem();
            if (null == contactItem) {
                contactItem = messageItem.getContactItem();
            }
            if (contactItem != null) {
                title = contactItem.getName();
                if (null == title || 0 == title.length()) {
                    title = contactItem.getFirstName();
                }
                if (null == title || 0 == title.length()) {
                    title = contactItem.getLastName();
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
            String[] names = contactItem.getContactsNameAsArray();
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
//        Toolbar toolbar = setupDetailToolbar();
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

    private void execMarkReadTask(MessageItem messageItem) {
        markReadTask = new MarkReadTask(getContext(), messageItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_MARK_READ);
        markReadTask.setMarkReadTaskListener(this);
        markReadTask.execute();
    }

    @Override
    public void onPostMarkReadExecute(JSONObject response, MessageItem item) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostMarkReadExecuteFail(response, item);
        } else {
            Log.e(TAG, "onPostMarkReadExecute:SUCCESS " + response);
        }
    }

    @Override
    public void onPostMarkReadExecuteFail(JSONObject response, MessageItem item) {
        Log.e(TAG, "Mark Read - FAIL " + response);
    }

    @Override
    public void setMarkReadTaskToNull() {
        markReadTask = null;
    }

    private void createRecyclerAdapter(Uri uri) {
        try {
            long id = ContentUris.parseId(uri);
//            empties the list..
//            // TODO: 06-04-2016
//            to update only the changed item
            if (id > 0)
                createRecyclerAdapter(id);
        } catch (NumberFormatException e) {
//            ignore
        }
    }

    private void createRecyclerAdapter(long id) {
        Cursor cursor = getAppCompatActivity().getContentResolver().query(
                Uri.withAppendedPath(MsgChatAttachmentContentProvider.CONTENT_URI, String.valueOf(id)),
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
                createRecyclerAdapter();
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
        messageItem.getMessages().clear();
        fetchPeopleDetailTask = new FetchPeopleDetailTask(messageItem,
                filter, getAppCompatActivity());
        fetchPeopleDetailTask.setFetchMsgsGrpPeopleDetailTaskListener(this);
        fetchPeopleDetailTask.execute();
//        first time init
/*
        try {
            getAppCompatActivity().getSupportLoaderManager().restartLoader(
                    MainApplicationSingleton.CHAT_MESSAGE_FRAGMENT_LOADERID, null, this);
        } catch (NullPointerException e) {
            getAppCompatActivity().getSupportLoaderManager().initLoader(
                    MainApplicationSingleton.CHAT_MESSAGE_FRAGMENT_LOADERID, null, this);
        }
*/
    }

    private void createRecyclerAdapter() {
        if (null == rvMessages && getView() != null)
            rvMessages = (RecyclerView) getView().findViewById(R.id.rv_chat_messages);
        if (null == rvMessages) {
            Log.e(TAG, "createRecyclerAdapter: Recycler view NULL");
            return;
        }
        Set<MessageItem> messages = messageItem.getMessages();
        if (selfTypingMessage != null) {
            try {
                messageItem.addMessage(selfTypingMessage);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        List<MessageItem> adapterList = new ArrayList<>(messages);
//            sorts messages by asc.. so the latest msg is at the bottom
        Collections.sort(adapterList, new MessageItem.MessageItemComparator());

        rvMessagesAdapter = new RecyclerViewAdapter(adapterList);
        rvMessagesAdapter.setHasStableIds(true);
        rvMessages.setAdapter(rvMessagesAdapter);
        rvMessages.scrollToPosition(rvMessagesAdapter.getItemCount() - 1);
    }

    public void fillItemsFromTask(Collection<MessageItem> items) {
        Collection<MessageItem> messageItems = messageItem.getMessages();
        if (null == messageItems) messageItems = new HashSet<>();
        messageItems.clear();
        messageItems.addAll(items);
    }

    @Override
    public void onFetchMsgsGrpPeopleDetailTaskExecute(
            MessageItem messageItem) {
        this.messageItem = messageItem;
/*
        if (null == result || result.isEmpty()) {
            onFetchMsgsGrpPeopleDetailTaskExecuteFail(this.messageItem);
            return;
        }
*/
//        fillItemsFromTask(result);
/*
        MainApplicationSingleton.getInstance(getAppCompatActivity()).putGlobalVariable(
                MsgsGrpPeopleFragment.TAG, messageItems
        );
*/
        createRecyclerAdapter();
    }

    @Override
    public void onFetchMsgsGrpPeopleDetailTaskExecuteFail(
            MessageItem messageItem) {
        Log.e(TAG, "onFetchMsgsGrpPeopleDetailTaskExecuteFail: ");
    }

    @Override
    public void setFetchMsgsGrpPeopleDetailTaskToNull() {
        fetchPeopleDetailTask = null;
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
        peopleHeaderListener.onPeopleHeaderChanged(messageItem);
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
                last.setChatId(messageItem.getChatId());
                last.setToUid(messageItem.getToUid());
                last.setToChatUid(messageItem.getToChatUid());

                last.setSubject(messageItem.getSubject());
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

    @Override
    public void onClick(View v) {

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
    }

    private void execFlagMsgsTask(MessageItem[] msgs) {
        flagMsgsTask = new FlagMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_FLAG_MSGS);
        flagMsgsTask.setFlagMsgsTaskListener(this);
        flagMsgsTask.execute();
    }

    private void execUnFlagMsgsTask(MessageItem[] msgs) {
        unflagMsgsTask = new UnFlagMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_UNFLAG_MSGS);
        unflagMsgsTask.setUnFlagMsgsTaskListener(this);
        unflagMsgsTask.execute();
    }

    private void deleteMessages() {
        if (null == selectedItem) return;
        execDeleteMsgsTask(new String[]{selectedItem.getDataId()});
    }

    private void unflagMessages() {
        if (null == selectedItem) return;
        selectedItem.setFlagged(false);
        execUnFlagMsgsTask(new MessageItem[]{selectedItem});
    }

    private void flagMessages() {
        if (null == selectedItem) return;
        selectedItem.setFlagged(true);
        execFlagMsgsTask(new MessageItem[]{selectedItem});
    }

    private void forwardMessages() {
/*
        IntellibitzActivity intellibitzActivity = removeSelf();
        if (intellibitzActivity == null) return;
//        this.onDestroy();
//        intellibitzActivity.onBackPressed(intent);
        Intent intent = new Intent();
        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) selectedItem);
        intellibitzActivity.onMessageForward(intent);
*/
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

/*
    private void forwardMessagesToNest() {
        IntellibitzActivity intellibitzActivity = removeSelf();
        if (intellibitzActivity == null) return;
//        this.onDestroy();
//        intellibitzActivity.onBackPressed(intent);
        Intent intent = new Intent();
        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) selectedItem);
        intellibitzActivity.onMessageForwardToNest(intent);
    }
*/

    @Override
    public void onPostUnFlagMsgsExecute(JSONObject response, MessageItem[] mids, String[] item) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostUnFlagMsgsExecuteFail(response, mids, item);
        } else {
            if (null == mids || 0 == mids.length) return;
            ContentValues values = new ContentValues();
            values.put(MessageItemColumns.KEY_IS_FLAGGED, false);
            for (MessageItem id : mids) {
                Uri uri;
                if (id.isChat()) {
                    uri = MessageChatContentProvider.RAW_CONTENT_URI;
                } else {
                    uri = MessageEmailContentProvider.RAW_CONTENT_URI;
                }
                getAppCompatActivity().getContentResolver().update(
                        uri, values,
                        MessageItemColumns.KEY_DATA_ID + " = ? ",
                        new String[]{id.getDataId()});
            }
            restartLoader();
            Log.e(TAG, "onPostUnFlagMsgsExecute: SUCCESS - " + response);
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
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostFlagMsgsExecuteFail(response, mids, item);
        } else {
            if (null == mids || 0 == mids.length) return;
            ContentValues values = new ContentValues();
            values.put(MessageItemColumns.KEY_IS_FLAGGED, true);
            for (MessageItem id : mids) {
                Uri uri;
                if (id.isChat()) {
                    uri = MessageChatContentProvider.RAW_CONTENT_URI;
                } else {
                    uri = MessageEmailContentProvider.RAW_CONTENT_URI;
                }
                getAppCompatActivity().getContentResolver().update(
                        uri, values,
                        MessageItemColumns.KEY_DATA_ID + " = ? ",
                        new String[]{id.getDataId()});
            }
            restartLoader();
            Log.e(TAG, "onPostFlagMsgsExecute: SUCCESS - " + response);
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
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostDeleteMsgsErrorResponse(response, item);
        } else {
            Log.e(TAG, "onPostDeleteMsgsResponse: SUCCESS - " + response);
            deleteMessagesInDB(item);
        }

    }

    public void deleteMessagesInDB(String[] item) {
        try {
            int count = MessageChatContentProvider.deleteMsgs(item, getContext());
            restartLoader();
            Log.e(TAG, "deleteMessagesInDB: Delete in DB: " + count);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onPostDeleteMsgsErrorResponse(JSONObject response, String[] item) {
        Log.e(TAG, "deleteMessagesInDB: Delete Msgs FAIL - " + response + ": " + Arrays.toString(item));
//        deleteMessagesInDB(item);
    }

    public void showNewMessageDialog() {
        // Create an instance of the dialog fragment and show it
        NewEmailDialogFragment.newMessageDialog(this, 2, user, messageItem).show(
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

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ChatService.MSG_SET_VALUE:
                    restartLoader();
/*
                    getAppCompatActivity().getSupportLoaderManager().restartLoader(
                            MainApplicationSingleton.CHAT_MESSAGE_FRAGMENT_LOADERID, null,
                            ChatDetailFragment.this);
                    rvMessages.notify();
*/
                    break;
                case ChatService.MSG_SHOW_TYPING:
                    if (peopleListener != null) {
                        try {
                            String obj = (String) msg.obj;
                            if (null != obj) {
                                JSONArray jsonArray = new JSONArray(obj);
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                try {
                                    String toUid = jsonObject.getString("to_uid");
                                    String fromUid = jsonObject.getString("from_uid");
                                    String fromName = jsonObject.getString("from_name");
                                    showTyping(toUid, fromUid, fromName, user);
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
                    .inflate(R.layout.fragment_peopledetail_rv, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = adapterItems.get(position);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                holder.mView.setAlpha(1f);
            }
            boolean me = false;
            if (user.getDataId().equals(holder.mItem.getFromUid())) {
                me = true;
            }


/*
            LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            startParams.gravity = Gravity.TOP;
            startParams.gravity = Gravity.START;

            LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            endParams.gravity = Gravity.TOP;
            endParams.gravity = Gravity.END;
*/

            LinearLayout rootView = (LinearLayout) holder.mView.getRootView();
            rootView.setGravity(Gravity.START);
/*
            if (rootView instanceof LinearLayout){
//                    ((LinearLayout) rootView).setGravity(Gravity.END);
//                    ((LinearLayout) rootView).setLayoutParams(params);
            }
*/
//            rootView.setLayoutParams(LinearLayout.LayoutParams.);
/*
            if (rootView instanceof LinearLayout)
                ((LinearLayout) rootView).setGravity(Gravity.START);
*/
            LinearLayout llChat = (LinearLayout) rootView.findViewById(R.id.ll_chat);
//            llChat.setLayoutParams(startParams);
            llChat.setBackground(getDrawable(R.drawable.shape_bubble_white));
            if (me) {
//                rootView.setLayoutParams(endParams);
                rootView.setGravity(Gravity.END);
//                llChat.setLayoutParams(endParams);
//                llChat.setGravity(Gravity.END);
                llChat.setBackground(getDrawable(R.drawable.shape_roundrect_blue));
                holder.tvMessage.setTextColor(getColor(R.color.white));
//                    ||
//                    MessageItem.TAG.equals(holder.userEmailItem.getDataId()))
//            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    holder.tvFromName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
//                    holder.tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                }
            }
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.list_item_chat_attch_msg);
//            clears old views
            linearLayout.removeAllViews();


            holder.tvMessage.setTextColor(getColor(R.color.black));
            holder.tvMessage.setText(holder.mItem.getText());

            Drawable flagged = null;
            if (holder.mItem.isFlagged()) {
                flagged = getDrawable(R.drawable.ic_bookmark_black_18dp);
                if (flagged != null) flagged.setBounds(new Rect(0, 0, 20, 20));
            }
            if (holder.mItem.isDelivered()) {
                Drawable drawable = getDrawable(R.drawable.ic_done_all_black_18dp,
                        getAppCompatActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 20, 20));
                if (me)
                    setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged);
                else
                    setCompoundDrawablesRelative(holder.tvMessage, drawable, null, flagged, null);
            } else if (holder.mItem.isRead()) {
                Drawable drawable = getDrawable(R.drawable.ic_done_black_18dp,
                        getAppCompatActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 20, 20));
                if (me)
                    setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged);
                else
                    setCompoundDrawablesRelative(holder.tvMessage, drawable, null, flagged, null);
            } else {
                Drawable drawable = getDrawable(R.drawable.ic_restore_black_18dp,
                        getAppCompatActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 20, 20));
                if (me)
                    setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged);
                else
                    setCompoundDrawablesRelative(holder.tvMessage, drawable, null, flagged, null);
            }

            holder.tvFromName.setVisibility(View.VISIBLE);

//            handles new message
//            new message has the message thread item class name as an hack to indicate new
/*
            if (MessageItem.TAG.equals(holder.userEmailItem.getDataId())) {
                if (holder.userEmailItem.getAttachments().isEmpty()) {
                    holder.mView.setBackground(getDrawable(android.R.color.transparent
                            , getContext().getTheme()));
                    holder.mView.setAlpha(1f);
                    holder.mView.setVisibility(View.GONE);
                    return;
                } else {
                    holder.mView.setAlpha(0.5f);
                    holder.tvFromName.setVisibility(View.GONE);
                    holder.mView.setBackground(getDrawable(R.color.graylte
                            , getContext().getTheme()));
                }
            } else {
            }
*/
//            binds the message
/*
            TextDrawable textDrawable = ColorGenerator.getTextDrawable(holder.mItem.getFromName());
            textDrawable.setBounds(new Rect(0, 0, 60, 60));
            if (me)
                setCompoundDrawablesRelative(holder.tvFromName, null, null, textDrawable, null);
            else
                setCompoundDrawablesRelative(holder.tvFromName, textDrawable, null, null, null);
*/

//            holder.tvFromName.setText(holder.messageItem.getFromName());
//                    converts to milliseconds, for correct date conversion
            long createDate = holder.mItem.getTimestamp() * 1000;
            Timestamp timestamp = new Timestamp(createDate);
            long now = System.currentTimeMillis();
            DateFormat df = new SimpleDateFormat("MMM dd", Locale.getDefault());
            if ((now - createDate) < 1000 * 60 * 60 * 24) {
                df = new SimpleDateFormat("hh mm a", Locale.getDefault());
            }
            String dt = df.format(timestamp.getTime());
//            holder.tvFromName.setText(holder.tvFromName.getText() + "  @" + dt);
            holder.tvFromName.setText(dt);


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
                        R.layout.list_item_chat_attch_msg,
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
                    Drawable drawable = null;
                    String type = attachmentItem.getType();
                    if (type != null && type.contains("image") ||
                            (mimetype != null && mimetype.contains("image"))) {
                        try {
                            bm = BitmapFactory.decodeFile(downloadURL);
                            drawable = new BitmapDrawable(getResources(), bm);
                        } catch (Throwable ignored) {
                            Log.e(TAG, TAG + ignored.getMessage());
                        }
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
                    if (drawable != null) {
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        setCompoundDrawablesRelative(tv, drawable, null, null, null);
                    }
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainApplicationSingleton.startMimeActivity(file,
                                    MsgChatAttachmentContentProvider.getMimeType(attachmentItem),
                                    getContext());
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
                                        MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
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
                                MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                                        getAppCompatActivity(), user, attachmentItem);
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

        public class ViewHolder extends
                RecyclerView.ViewHolder implements
                View.OnLongClickListener {
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

            public ViewHolder(View view) {
                super(view);
                mView = view;
                view.setOnLongClickListener(this);
                mView.setOnClickListener(PeopleDetailFragment.this);
                mView.setOnClickListener(PeopleDetailFragment.this);
                tvSub = (TextView) view.findViewById(R.id.tv_sub);
                tvTo = (TextView) view.findViewById(R.id.tv_to);
                tvCc = (TextView) view.findViewById(R.id.tv_cc);
                tvBcc = (TextView) view.findViewById(R.id.tv_bcc);
                tvFromName = (TextView) view.findViewById(R.id.tv_from_name);
                tvMessage = (TextView) view.findViewById(R.id.tv_msg);
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

}
