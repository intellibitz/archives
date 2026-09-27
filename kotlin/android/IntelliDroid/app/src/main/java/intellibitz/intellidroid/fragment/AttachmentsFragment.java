package intellibitz.intellidroid.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.MainboxListener;
import intellibitz.intellidroid.listener.PeopleDetailListener;
import intellibitz.intellidroid.listener.PeopleListener;
import intellibitz.intellidroid.listener.PeopleTopicListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.ContactSelectActivity;
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity;
import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;
import intellibitz.intellidroid.content.task.FetchAttachmentsTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.domain.account.EmailAccountListActivity;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.MainboxListener;
import intellibitz.intellidroid.listener.PeopleDetailListener;
import intellibitz.intellidroid.listener.PeopleListener;
import intellibitz.intellidroid.listener.PeopleTopicListener;
import intellibitz.intellidroid.service.ChatService;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;

import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;
import intellibitz.intellidroid.service.ChatService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static intellibitz.intellidroid.util.MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE;
import static intellibitz.intellidroid.util.MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE;
import static intellibitz.intellidroid.util.MainApplicationSingleton.getBaseItem;

public class AttachmentsFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        ContactListener,
        PeopleDetailListener,
        FetchAttachmentsTask.FetchAttachmentsTaskListener {

    public static final String TAG = "AttachmentsFragment";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final Object lock = new Object();
    /**
     * Messenger for communicating with service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;
    private Snackbar snackbar;
    private FloatingActionButton fab;
    private String filter = null;
    private ContentObserver messageContentObserver;
    private RecyclerView rvMessages;
    private RecyclerViewAdapter rvMessagesAdapter;
    private DeleteMsgsTask deleteMsgsTask;
    private MessageItem selectedItem;
    private Intent sharedIntent;
    private ContactItem contactItem;
    private ArrayList<MessageItem> attachmentItems;
    private int counter = 0;
    private HandlerThread looperThread;

    //    private String intellibitzId;
    private MessageItem messageItemToForward;
    private PeopleTopicListener peopleTopicListener;
    private PeopleDetailListener peopleDetailListener;
    private TextView empty;
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
            inflater.inflate(R.menu.menu_context_chatty, menu);
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
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_moveto:
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

    public AttachmentsFragment() {
        super();
    }

    public static AttachmentsFragment newInstance(ContactItem user, MainboxListener peopleListener) {
        AttachmentsFragment fragment = new AttachmentsFragment();
//        PeopleDetailFragment detailFragment = new PeopleDetailFragment();
//        fragment.setFragmentContext(c);
        fragment.setUser(user);
        if (peopleListener instanceof PeopleTopicListener)
            fragment.setPeopleTopicListener((PeopleTopicListener) peopleListener);
        if (peopleListener instanceof PeopleDetailListener)
            fragment.setPeopleDetailListener((PeopleDetailListener) peopleListener);
        if (peopleListener instanceof PeopleListener)
            fragment.setViewModeListener(peopleListener);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public void setAttachmentItems(ArrayList<MessageItem> attachmentItems) {
        this.attachmentItems = attachmentItems;
        MainApplicationSingleton.getInstance(getActivity()).putGlobalVariable(
                AttachmentsFragment.TAG, attachmentItems
        );
    }

    public void setSharedIntent(Intent sharedIntent) {
        this.sharedIntent = sharedIntent;
    }

    public void setPeopleDetailListener(PeopleDetailListener peopleDetailListener) {
        this.peopleDetailListener = peopleDetailListener;
    }

    private void showTyping(String toUid, String fromUid, String fromName, ContactItem user) {
        final MessageItem item = MainApplicationSingleton.getBaseItem(fromUid, attachmentItems);
        if (null == item) {

        } else {
            if (fromUid.equals(user.getDataId())) {
// if self is typing ignore..
//                shows typing only from other senders
            } else {
                item.setTyping(true);
                item.setTypingText(fromName + " is typing..");
                final int pos = attachmentItems.indexOf(item);
                rvMessagesAdapter.notifyItemChanged(pos);
//            restartLoader(null);
                onPeopleTyping(fromName + " is typing..");
//                                setToolBarShowTyping(name + " is typing..");
                final Handler delay = new Handler();
                delay.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                                        setToolBarSubTitle(toolbarSubTitle);
//                    name = null;
//                    restartLoader(null);
                        item.setTyping(false);
                        rvMessagesAdapter.notifyItemChanged(pos);
                        onPeopleTypingStopped("");
                    }
                }, 1000);
            }
        }
    }

    @Override
    public void onPeopleTyping(String text) {
        if (null == peopleDetailListener) {
// // TODO: 21-05-2016
//            listeners need to be saved, restored or recreated brand new every time
        } else {
            peopleDetailListener.onPeopleTyping(text);
//        getActivity().getToolbar().setSubtitle(text);
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
//        getActivity().getToolbar().setSubtitle(text);
//        toolbar.setSubtitleTextColor(Color.WHITE);
        }
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        getActivity().bindService(new Intent(getActivity(),
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
                            ChatService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
//            mCallbackText.setText("Unbinding.");
        }
    }

    public void showEmpty() {
        this.empty.setVisibility(View.VISIBLE);
        empty.setText(R.string.empty_chat_account);
    }

    public void showEmpty(String msg) {
        this.empty.setVisibility(View.VISIBLE);
        empty.setText(msg);
    }

    public void hideEmpty() {
        if (empty != null)
            empty.setVisibility(View.GONE);
    }

    public void setUser(ContactItem user) {
        this.user = user;
    }

    public void setPeopleTopicListener(PeopleTopicListener peopleTopicListener) {
        this.peopleTopicListener = peopleTopicListener;
    }

    @Override
    public void onViewModeChanged() {
        if (viewModeListener != null) {
            viewModeListener.onViewModeChanged();
        }
        super.onViewModeChanged();
    }

    @Override
    public void onViewModeItem() {
        if (viewModeListener != null) {
            viewModeListener.onViewModeItem();
        }
        super.onViewModeItem();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        doBindService();
//        peopleTopicListener = (PeopleTopicListener) context;
        messageContentObserver = new ContentObserver(new Handler()) {
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
                synchronized (lock) {
                    counter++;
                    looperThread = MainApplicationSingleton.performOnHandlerThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
//                                sleeps 30 seconds.. let the rcv doc catch up
//                                Thread.sleep(30000);
                                Thread.sleep(500);
                            } catch (InterruptedException ignored) {
//                                e.printStackTrace();
                                Log.e(TAG, ignored.getMessage());
                            }
                            AttachmentsFragment.this.quitHandlerThread();
                        }
                    });
                }
            }
        };
        getActivity().getContentResolver().registerContentObserver(
                MsgChatAttachmentContentProvider.CONTENT_URI, true,
                messageContentObserver);
    }

    private void quitHandlerThread() {
        if (looperThread != null)
            looperThread.quit();
        if (counter > 1) {
            Log.d(TAG, "quitHandlerThread: counter - " + counter);
            counter--;
            return;
        }
        if (counter <= 1) {
            counter = 0;
            final Activity appCompatActivity = getActivity();
            if (null == appCompatActivity) return;
            if (appCompatActivity.isFinishing()) return;
            if (MainApplicationSingleton.isAPI17()) {
                if (appCompatActivity.isDestroyed()) return;
            }
            restartLoader();
            Log.d(TAG, "quitHandlerThread: counter - " + counter);
        }
    }

    @Override
    public void onDestroy() {
        doUnbindService();
        getActivity().getContentResolver().unregisterContentObserver(messageContentObserver);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            attachmentItems = savedInstanceState.getParcelableArrayList(
                    MessageItem.TAG);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTwoPane(getArguments().getBoolean("twoPane"));
        user = getArguments().getParcelable(ContactItem.USER_CONTACT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attachments, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        empty = (TextView) view.findViewById(R.id.tv_empty);
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmailListActivity();
            }
        });

        rvMessages = (RecyclerView) view.findViewById(R.id.recyclerview);
        setupSwipe();
        setupSnackBar();
//        restartCacheLoader();
        restartLoader();
    }

    private void startEmailListActivity() {
        Intent intent = new Intent(getActivity(), EmailAccountListActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNT_RQ_CODE);
    }

    public void setupSwipe() {
        if (null == getView()) return;
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)
                getView().findViewById(R.id.swiperefresh);
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                private final Handler mHandler = new Handler();

                @Override
                public void onRefresh() {
                    // Post a delayed runnable to reset the refreshing state in 2 seconds
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    }, 2000);
                }
            });
        }
    }

    private void restartLoader(Uri uri) {
        try {
            long id = ContentUris.parseId(uri);
/*
            if (MessagesContentProvider.AUTHORITY.equals(uri.getAuthority())) {
                Cursor cursor = getActivity().getContentResolver().query(
                        uri, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    MessageItem messageThreadItem = new MessageItem();
                    MessagesContentProvider.createsMsgsGrpDraftFromCursor(
                            messageThreadItem, cursor);
                    cursor.close();
                    reloadRecycleAdapter(messageThreadItem);
                }
            }
*/
//            empties the list..
//            // TODO: 06-04-2016
//            to update only the changed item
            attachmentItems = null;
            restartLoader();
        } catch (NumberFormatException e) {
//            ignore
        }
    }

    protected void restartLoader(String filter) {
        this.filter = filter;
        restartLoader();
    }

    private void restartCacheLoader() {
        ArrayList<? extends Object> objects =
                MainApplicationSingleton.getInstance(getActivity()).getGlobalSBValueAsList(
                        AttachmentsFragment.TAG
                );
        if (null == objects || objects.isEmpty()) {
            restartLoader();
            return;
        }
        if (counter > 0) {
            counter = 0;
            restartLoader();
            Log.d(TAG, "quitHandlerThread: counter - " + counter);
            return;
        }
        this.attachmentItems = (ArrayList<MessageItem>) objects;
        createRecycleAdapter();
    }

    private void restartLoader() {
//        resets the view, only if the items are null
//        // TODO: 06-04-2016
//        for a specific item change.. implement other methods
        FetchAttachmentsTask fetchAttachmentsTask = new FetchAttachmentsTask(
                filter, getActivity());
        fetchAttachmentsTask.setFetchAttachmentsTaskListener(this);
        fetchAttachmentsTask.execute();
    }

    public void messageSend(Intent shared) {
        restartLoader();
    }

    private void createRecycleAdapter() {
        resetRecycleAdapter();
//        showActiveMessage();
    }

    private void resetRecycleAdapter() {
        if (null == rvMessages && getView() != null) {
            rvMessages = (RecyclerView) getView().findViewById(R.id.recyclerview);
        }
        if (null == rvMessages) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL");
            return;
        }

        if (null == attachmentItems) return;
//        List<MessageItem> adapterList = new ArrayList<>(attachmentItems);
//            sorts messages by asc.. so the latest msg is at the bottom
        Collections.sort(attachmentItems, new MessageItem.MessageItemComparator<>(
                BaseItemComparator.SORT_MODE.DESC));

        rvMessagesAdapter = new RecyclerViewAdapter(attachmentItems);
        rvMessagesAdapter.setHasStableIds(true);
        rvMessages.setAdapter(rvMessagesAdapter);
//        rvMessages.scrollToPosition(rvMessagesAdapter.getItemCount() - 1);
    }

    private void setupSnackBar() {
        if (null == getView()) return;
        snackbar = Snackbar.make(
                getView(), "Please Add Account to see Emails", Snackbar.LENGTH_LONG);
        snackbar.setAction("ADD EMAIL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmailListActivity();
            }
        });
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onMessageForward(Intent intent) {
        if (null == intent) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            getActivity().onBackPressed();
            return;
        }
        messageItemToForward = intent.getParcelableExtra(MessageItem.TAG);
        String source = intent.getAction();
        if (null == messageItemToForward) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            getActivity().onBackPressed();
            return;
        }
/*
        if (ChatDetailFragment.TAG.equals(source)) {
//            Log.d(TAG, "onMessageForward: "+messageItem);
//            getActivity().hideDetailToolbar();
//            getActivity().replaceContentFragment(this);
        }
*/
    }

    public void showContactThreadDetail(Intent intent) {
        startMsgChatGrpContactsDetailActivity();
//        GroupsDetailFragment msgChatGrpContactsDetailFragment = showContactThreadDetail();
//        msgChatGrpContactsDetailFragment.onOkPressed(intent);
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
    private void startContactSelectActivity() {
        Intent intent = new Intent(getActivity(), ContactSelectActivity.class);
//        intent.setAction(IntellibitzContactSelectItemFragment.TAG);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    public void startMsgChatGrpContactsDetailActivity() {
        Intent intent = new Intent(getActivity(), MsgChatGrpContactsDetailActivity.class);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE);
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
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ");
            }
        } else if (MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ");
            }
        }
    }

/*
    private void showNewEmailMessage(MessageItem item, ContactItem user) {
        //        creates a message thread
        MessageItem messageThreadItem = new MessageItem();
        messageThreadItem.setDataId(MessageItem.TAG);
        messageThreadItem.setFrom(this.user.getName());
        messageThreadItem.setDocOwnerEmail(this.user.getEmail());
        messageThreadItem.setDocSenderEmail(this.user.getEmail());
        messageThreadItem.setSubject(item.getSubject());
        MessageItem.setMessageThreadEmailAddress(messageThreadItem,
                item.getTo(), item.getCc(), item.getBcc());
        showMessageThreadMessage(messageThreadItem, user);
    }

    private void showNewEmailMessage(String contact) {
        showMessageThreadMessage(MessageItem.TAG, contact);
    }

    private void showNewChatMessage(String contact) {
        showMessageThreadMessage(MessageItem.TAG, contact);
    }
*/

/*
    private void showMessageThreadMessage(String messageThreadId, String contact) {
        //        creates a message thread
        MessageItem messageThreadItem = new MessageItem();
        messageThreadItem.setDataId(messageThreadId);
        messageThreadItem.setFrom(user.getName());
        messageThreadItem.setDocOwnerEmail(user.getEmail());
        messageThreadItem.setDocSenderEmail(user.getEmail());
        MessageItem.setMessageThreadEmailAddress(messageThreadItem,
                contact, null, null);
        showMessageThreadMessage(messageThreadItem, user);
    }
*/

    public boolean onDetailQueryTextSubmit(String query) {
        return false;
    }

    public boolean onDetailQueryTextChange(String newText) {
        return false;
    }

    public boolean onDetailClose() {
        return false;
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
    public void onFetchAttachmentsTaskExecute(ArrayList<MessageItem> attachmentItems) {
        setAttachmentItems(attachmentItems);
        createRecycleAdapter();
    }

    @Override
    public void onFetchAttachmentsTaskExecuteFail(ArrayList<MessageItem> attachmentItems) {
        Log.e(TAG, "onFetchAttachmentsTaskExecuteFail: ");
    }

/*
    @Override
    public void onFetchCursorTaskExecute(List<MessageItem> result) {
        if (null == result || result.isEmpty()) {
            onFetchCursorTaskExecuteFail(result);
            return;
        }
        attachmentItems = (ArrayList<MessageItem>) result;
        MainApplicationSingleton.getInstance(getActivity()).putGlobalVariable(
                MsgsGrpPeopleFragment.TAG, attachmentItems
        );
        createRecycleAdapter();
    }

    @Override
    public void onFetchCursorTaskExecuteFail(List<MessageItem> result) {
        Log.e(TAG, "onFetchCursorTaskExecuteFail: " + result);
    }

    @Override
    public void setFetchCursorTaskToNull() {
        fetchMessageThreadShalJoinCursorTask = null;
    }
*/

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ChatService.MSG_SHOW_TYPING:
                    if (peopleDetailListener != null) {
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

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements
            BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private List<MessageItem> attachmentItems;
        private BitmapFromUrlTask bitmapFromUrlTask;

        public RecyclerViewAdapter(List<MessageItem> items) {
            super();
            this.attachmentItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            bind to the message thread list item view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_attachments_rv, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = attachmentItems.get(position);
            holder.tvId.setText(holder.mItem.getDataId());

            String filename = holder.mItem.getProfilePic();
            if (TextUtils.isEmpty(filename)) {
                filename = holder.mItem.getDownloadURL();
            }

            String name = holder.mItem.getName();
            if (TextUtils.isEmpty(name)) {
                name = MainApplicationSingleton.getLastPath(filename);
            }
            holder.tvName.setText(name);

            String attachmentType = holder.mItem.getType();
            if (null == attachmentType) attachmentType = "";
            String sz = attachmentType + " " + holder.mItem.getSize() + " bytes";
            holder.tvSubject.setText(sz);


/*
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainApplicationSingleton.startMimeActivity(holder.mItem.getDownloadURL(),
                            MsgChatAttachmentContentProvider.getMimeType(holder.mItem), getActivity());
                }
            });
*/

            String extension = "";
            String mimetype = "";
            if (TextUtils.isEmpty(filename)) {
                Log.e(TAG, "Attachment Download URL is NULL ");
            } else if (filename.startsWith("/")) {
//                progressBar.setVisibility(View.GONE);
                final File file = new File(filename);
                extension = MimeTypeMap.getFileExtensionFromUrl(
                        Uri.fromFile(file).toString());
                mimetype =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainApplicationSingleton.startMimeActivity(file,
                                MsgChatAttachmentContentProvider.getMimeType(holder.mItem),
                                getContext());
                    }
                });
            } else if (filename.startsWith("http")) {
                extension = MimeTypeMap.getFileExtensionFromUrl(
                        Uri.parse(filename).toString());
                mimetype =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MessageItem.EMAIL.equalsIgnoreCase(holder.mItem.getMessageType()))
                            MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                                    getAppCompatActivity(),
                                    user, holder.mItem);
                        if (MessageItem.CHAT.equalsIgnoreCase(holder.mItem.getMessageType()))
                            MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                                    getAppCompatActivity(), user, holder.mItem);
                        if (MessageItem.GROUP.equalsIgnoreCase(holder.mItem.getMessageType()))
                            MsgChatGrpAttachmentContentProvider.asyncUpdateChatGroupAttachmentFilePathInDB(
                                    getAppCompatActivity(), user, holder.mItem);
                    }
                });
            } else {
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MessageItem.EMAIL.equalsIgnoreCase(holder.mItem.getMessageType()))
                            MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                                    getAppCompatActivity(),
                                    user, holder.mItem);
                        if (MessageItem.CHAT.equalsIgnoreCase(holder.mItem.getMessageType()))
                            MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                                    getAppCompatActivity(), user, holder.mItem);
                        if (MessageItem.GROUP.equalsIgnoreCase(holder.mItem.getMessageType()))
                            MsgChatGrpAttachmentContentProvider.asyncUpdateChatGroupAttachmentFilePathInDB(
                                    getAppCompatActivity(), user, holder.mItem);
                    }
                });
            }

//            holder.tvExt.setText(extension);

            Bitmap bm;
            Drawable drawable = null;
            try {
                if (attachmentType.contains("image") || !TextUtils.isEmpty(mimetype) && mimetype.contains("image")) {
                    if (!TextUtils.isEmpty(filename) && filename.startsWith("http")) {
                        bitmapFromUrlTask = new BitmapFromUrlTask(
                                holder.ivAttachment, filename, getActivity().getApplicationContext());
                        bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                        bitmapFromUrlTask.execute();
/*
                        ((NetworkImageView) holder.ivAttachment).setImageUrl(downloadURL,
                                MainApplicationSingleton.getInstance(getActivity()).getImageLoader());
*/
                    } else {
                        if (!TextUtils.isEmpty(filename)) {
                            bm = BitmapFactory.decodeFile(filename);
                            drawable = new BitmapDrawable(getResources(), bm);
                        }
                    }
                } else if (attachmentType.contains("video") || !TextUtils.isEmpty(mimetype) && mimetype.contains("video")) {
                    bm = ThumbnailUtils.createVideoThumbnail(
                            filename, MediaStore.Video.Thumbnails.MINI_KIND);
                    drawable = new BitmapDrawable(getResources(), bm);
/*
                    if (null == drawable) {
                        drawable = getDrawable(R.drawable.mpg, getActivity().getTheme());
                    }
*/
                } else if (attachmentType.contains("audio") || !TextUtils.isEmpty(mimetype) && mimetype.contains("audio")) {
                    drawable = getDrawable(android.R.drawable.ic_media_play,
                            getActivity().getTheme());
                } else if (attachmentType.contains("application") || !TextUtils.isEmpty(mimetype) && mimetype.contains("application")) {
                    drawable = getDrawable(android.R.drawable.ic_menu_slideshow,
                            getActivity().getTheme());
                }
            } catch (Throwable ignored) {
                Log.e(TAG, TAG + ignored.getMessage());
            }

            if (null == drawable &&
                    ("jpeg".equalsIgnoreCase(extension) || "jpg".equalsIgnoreCase(extension))) {
                drawable = getDrawable(R.drawable.jpg);
            }
            if (null == drawable && "pdf".equalsIgnoreCase(extension)) {
                drawable = getDrawable(R.drawable.pdf);
            }
            if (null == drawable && "png".equalsIgnoreCase(extension)) {
                drawable = getDrawable(R.drawable.png);
            }
            if (null == drawable) {
//                drawable = getDrawable(R.drawable.ic_get_app_black_18dp);
                holder.ivAttachment.setImageResource(R.drawable.ic_get_app_black_18dp);
            }
            if (drawable != null) {
                drawable.setBounds(new Rect(0, 0, 100, 100));
//                holder.ivAttachment.setBackground(drawable);
                holder.ivAttachment.setImageDrawable(drawable);
            }

//            sets timestamp
//                    converts to milliseconds, for correct date conversion
/*
            long createDate = holder.messageItem.getTimestamp() * 1000;
            Timestamp timestamp = new Timestamp(createDate);
            long now = System.currentTimeMillis();
            DateFormat df = new SimpleDateFormat("MMM dd", Locale.getDefault());
            if ((now - createDate) < 1000 * 60 * 60 * 24) {
                df = new SimpleDateFormat("hh mm a", Locale.getDefault());
            }
            String dt = df.format(timestamp.getTime());
            holder.tvTimestamp.setText(dt);
*/


        }

        @Override
        public long getItemId(int position) {
            return attachmentItems.get(position).get_id();
        }

        @Override
        public int getItemCount() {
            return attachmentItems.size();
        }

        @Override
        public void onPostBitmapFromUrlExecute(Bitmap bitmap, View view, Context context) {
            if (null == context) return;
            Resources resources = context.getResources();
            if (null == resources) return;
            if (null == bitmap) {
                Drawable drawable = getDrawable(R.drawable.ic_get_app_black_18dp, getActivity().getTheme());
                drawable.setBounds(new Rect(0, 0, 100, 100));
                ((ImageView) view).setImageDrawable(drawable);
            } else {
                Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                setImageDrawable(view, croppedBitmap);
            }
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
            //            the root view
            public final View mView;
            //            the list item views
            public final TextView tvId;
            public final TextView tvName;
            public final TextView tvSubject;
            //            public final TextView tvExt;
            //            public final TextView tvFrom;
//            public final TextView tvMessage;
//            public final TextView tvTimestamp;
            public final ImageView ivAttachment;
            //            the data
            public MessageItem mItem;


            public ViewHolder(View view) {
                super(view);
                mView = view;
                tvId = (TextView) view.findViewById(R.id.tv_id);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                tvSubject = (TextView) view.findViewById(R.id.tv_subject);
//                tvExt = (TextView) view.findViewById(R.id.tv_ext);
//                tvFrom = (TextView) view.findViewById(R.id.tv_from);
//                tvMessage = (TextView) view.findViewById(R.id.tv_text);
//                tvTimestamp = (TextView) view.findViewById(R.id.tv_timestamp);
                ivAttachment = (ImageView) view.findViewById(R.id.iv_attach);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + tvName.getText() + "'";
            }

            @Override
            public boolean onLongClick(View v) {
//                setSelectedItem(messageItem);
                if (null == mActionMode) {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = ((AppCompatActivity)
                            getActivity()).startSupportActionMode(mActionModeCallback);
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
