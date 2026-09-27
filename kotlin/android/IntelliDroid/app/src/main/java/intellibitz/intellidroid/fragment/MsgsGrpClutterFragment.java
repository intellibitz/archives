package intellibitz.intellidroid.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.listener.ClutterListener;
import intellibitz.intellidroid.listener.ClutterTopicListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider;
import intellibitz.intellidroid.content.task.FetchMsgsGrpClutterTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.domain.account.EmailAccountListActivity;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ClutterListener;
import intellibitz.intellidroid.listener.ClutterTopicListener;
import intellibitz.intellidroid.service.EmailService;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.DeleteMsgsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.widget.NewEmailDialogFragment;

import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider;
import intellibitz.intellidroid.service.EmailService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MsgsGrpClutterFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
        AdapterView.OnItemClickListener,
        View.OnClickListener,
        DeleteMsgsTask.DeleteMsgsTaskListener,
        FetchMsgsGrpClutterTask.FetchMsgsGrpClutterTaskListener {

    public static final String TAG = "MsgsGrpClutterFrag";
    private final Object lock = new Object();
    private Snackbar snackbar;
    private FloatingActionButton fab;
    private ClutterTopicListener clutterTopicListener;
    BroadcastReceiver contactEmailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String contact = intent.getStringExtra(MainApplicationSingleton.EMAIL_PARAM);
            Log.d(TAG, "received contact: " + contact);
            showNewEmailMessage(contact, intent);
        }
    };
    BroadcastReceiver contactPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String contact = intent.getStringExtra(MainApplicationSingleton.MOBILE_PARAM);
            Log.d(TAG, "received contact: " + contact);
            showNewChatMessage(contact);
        }
    };
    BroadcastReceiver newEmailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            MessageItem messageItem =
                    intent.getParcelableExtra(MessageItem.TAG);
//            Log.d(TAG, "received Message: " + messageItem);
            showNewEmailMessage(messageItem, user);
        }
    };
    private String filter = null;
    private ContentObserver messageContentObserver;
    private RecyclerView rvMessages;
    private RecyclerViewAdapter rvMessagesAdapter;
    private MessageItem selectedItem;
    private Intent sharedIntent;
    private ContactItem contactItem;
    private ArrayList<MessageItem> messageItems;
//    private FetchMsgsGrpClutterTask fetchMsgsGrpClutterTask;


    //    private HashMap<String, MessageItem> messageThreadItemHashMap = new HashMap<>();
    private int counter = 0;
    private HandlerThread looperThread;
    private TextView empty;
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
                    deleteMessages();
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

    public MsgsGrpClutterFragment() {
        super();
    }

    public static MsgsGrpClutterFragment newInstance(
            MessageItem messageItem, ContactItem user, ClutterListener clutterListener) {
        MsgsGrpClutterFragment fragment = new MsgsGrpClutterFragment();
//        fragment.setIntellibitzActivity(c);
        fragment.setUser(user);
        if (clutterListener instanceof ClutterTopicListener)
            fragment.setClutterTopicListener((ClutterTopicListener) clutterListener);
        fragment.setViewModeListener(clutterListener);
        Bundle args = new Bundle();
        args.putParcelable(MessageItem.TAG, messageItem);
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public void setTwoPane(boolean twoPane) {
        this.twoPane = twoPane;
    }

    public void setMessageItems(List<MessageItem> items) {
        this.messageItems = (ArrayList<MessageItem>) items;
        if (messageItems != null && !messageItems.isEmpty())
            MainApplicationSingleton.getInstance(getActivity()).putGlobalVariable(
                    MsgsGrpClutterFragment.TAG, this.messageItems
            );
    }

    public void showEmpty() {
        if (empty != null) {
            this.empty.setVisibility(View.VISIBLE);
            empty.setText(R.string.empty_email_account);
        }
    }

    public void hideEmpty() {
        if (empty != null) {
            empty.setVisibility(View.GONE);
        }
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(newEmailReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(contactPhoneReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(contactEmailReceiver);
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(newEmailReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(contactPhoneReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(contactEmailReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_CONTACT_EMAIL_SELECTED));
    }

    public void setClutterTopicListener(ClutterTopicListener messageTopicListener) {
        this.clutterTopicListener = messageTopicListener;
    }

    @Override
    public void onDestroy() {
        getActivity().getContentResolver().unregisterContentObserver(messageContentObserver);
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        getActivity().getContentResolver().unregisterContentObserver(messageContentObserver);
        unregisterReceiver();
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        clutterTopicListener = (ClutterTopicListener) context;
        registerReceiver();
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
                                Thread.sleep(3000);
                            } catch (InterruptedException ignored) {
//                                e.printStackTrace();
                                Log.e(TAG, ignored.getMessage());
                            }
                            MsgsGrpClutterFragment.this.quitHandlerThread();
                        }
                    });
                }
            }
        };
        getActivity().getContentResolver().registerContentObserver(
                MsgsGrpClutterContentProvider.CONTENT_URI, true,
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
            restartLoader();
            counter = 0;
            Log.d(TAG, "quitHandlerThread: counter - " + counter);
        }
    }

    public void onNewMenuClicked() {
        showNewMessageDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_msgsgrpclutter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
/*
        if (getActivity() != null)
            getActivity().getToolbar().setTitle(R.string.menu_title_email);
*/

        empty = (TextView) view.findViewById(R.id.tv_empty);
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmailListActivity();
            }
        });

        rvMessages = (RecyclerView) view.findViewById(R.id.recyclerview);
        setupSwipe();

        setupFAB(view);
        setupSnackBar(view);
        restartCacheLoader();
//        restartLoader();
    }

    private void startEmailListActivity() {
        Intent intent = new Intent(getActivity(), EmailAccountListActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNT_RQ_CODE);
    }

    public void emptyOnClick(View view) {
        startEmailListActivity();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
//        outState.putParcelableArrayList(MessageItem.TAG, messageItems);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
/*
            messageItems = savedInstanceState.getParcelableArrayList(
                    MessageItem.TAG);
*/
        }
    }

    private void restartLoader(Uri uri) {
        try {
            long id = ContentUris.parseId(uri);
//            empties the list..
//            // TODO: 06-04-2016
//            to update only the changed item
            messageItems = null;
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
                        MsgsGrpClutterFragment.TAG
                );
        if (null == objects) {
            restartLoader();
            return;
        }
        this.messageItems = (ArrayList<MessageItem>) objects;
        createAsyncUpdateRecycleAdapter();
    }

    private void restartLoader() {
//        // TODO: 28-03-2016
//        already done in content fragment.. not required
//        Log.d(TAG, "restartLoader: " + messageThreadItem);
//        resets the view, only if the items are null
//        // TODO: 06-04-2016
//        for a specific item change.. implement other methods
/*
        getActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.MSGSGRPCLUTTER_LOADERID, null, this);
*/
        FetchMsgsGrpClutterTask fetchMsgsGrpClutterTask = new FetchMsgsGrpClutterTask(
                filter, getActivity());
        fetchMsgsGrpClutterTask.setFetchMsgsGrpClutterTaskListener(this);
        fetchMsgsGrpClutterTask.execute();
    }

/*
    private void initRecycleAdapter(List<MessageItem> messageItems) {
        rvMessagesAdapter = new RecyclerViewAdapter(messageItems);
        rvMessagesAdapter.setHasStableIds(true);
        rvMessages.setAdapter(rvMessagesAdapter);
        rvMessages.scrollToPosition(rvMessagesAdapter.getItemCount() - 1);
    }
*/

    public void messageSend(Intent shared) {
        restartLoader();
    }

    private void createRecycleAdapter() {
        if (null == rvMessages && getView() != null) {
            rvMessages = (RecyclerView) getView().findViewById(R.id.recyclerview);
        }
        if (null == rvMessages) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL");
            return;
        }

        if (null == messageItems) return;
        if (!messageItems.isEmpty()) {
//        List<MessageItem> adapterList = new ArrayList<>(messageItems);
//            sorts messages by asc.. so the latest msg is at the bottom
            Collections.sort(messageItems, new MessageItem.MessageItemComparator<>(
                    BaseItemComparator.SORT_MODE.DESC_DT));
/*
            HashSet<String> ids = new HashSet<>();
            for (MessageItem messageItem : messageItems) {
                String text = messageItem.getText();
                if (TextUtils.isEmpty(text)) {
                    text = messageItem.getLatestMessageText();
                }
                if (TextUtils.isEmpty(text)) {
                    ids.add(messageItem.getDataId());
                }
            }
            if (!ids.isEmpty())
                EmailService.asyncUpdateGetFullEmails(
                        user.getEmail(), ids.toArray(new String[0]), user, getContext());
*/
        }

        rvMessagesAdapter =
                new RecyclerViewAdapter(messageItems);
        rvMessagesAdapter.setHasStableIds(true);
        rvMessages.setAdapter(rvMessagesAdapter);
        rvMessagesAdapter.notifyDataSetChanged();
//        rvMessages.scrollToPosition(rvMessagesAdapter.getItemCount() - 1);
    }

    private void createAsyncUpdateRecycleAdapter() {
        if (null == rvMessages && getView() != null) {
            rvMessages = (RecyclerView) getView().findViewById(R.id.recyclerview);
        }
        if (null == rvMessages) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL");
            return;
        }

        if (null == messageItems) return;
        if (!messageItems.isEmpty()) {
//        List<MessageItem> adapterList = new ArrayList<>(messageItems);
//            sorts messages by asc.. so the latest msg is at the bottom
            Collections.sort(messageItems, new MessageItem.MessageItemComparator<>(
                    BaseItemComparator.SORT_MODE.DESC_DT));
            HashSet<String> ids = new HashSet<>();
            for (MessageItem messageItem : messageItems) {
                String text = messageItem.getText();
                if (TextUtils.isEmpty(text)) {
                    text = messageItem.getLatestMessageText();
                }
                if (TextUtils.isEmpty(text)) {
                    ids.add(messageItem.getDataId());
                }
            }
            if (!ids.isEmpty())
                EmailService.asyncUpdateGetFullEmails(
                        user.getEmail(), ids.toArray(new String[0]), user, getContext());
        }

        rvMessagesAdapter =
                new RecyclerViewAdapter(messageItems);
        rvMessagesAdapter.setHasStableIds(true);
        rvMessages.setAdapter(rvMessagesAdapter);
        rvMessagesAdapter.notifyDataSetChanged();
//        rvMessages.scrollToPosition(rvMessagesAdapter.getItemCount() - 1);
    }

    private boolean processNewMessageFromSharedIntent() {
        if (null == sharedIntent) return false;
        if (null == sharedIntent.getExtras()) return false;
        final ContactItem intellibitzContactItem = sharedIntent.getParcelableExtra(
                ContactItem.INTELLIBITZ_CONTACT);
        if (null == intellibitzContactItem) {
            final ContactItem contactItem = sharedIntent.getParcelableExtra(
                    ContactItem.TAG);
            if (null == contactItem) {
//                Log.e(TAG, "processNewMessageFromSharedIntent: MsgThreadContact is NULL");
            } else {
                MainApplicationSingleton.performOnUIHandlerThread(new Runnable() {
                    @Override
                    public void run() {
//            clears the shared intent
                        sharedIntent = null;
//                        getActivity().selectMainTab();
                        contactItem.setGroup(true);
                        contactItem.setType("GROUP");
//                        createNewChat(contactItem, user);
//                        getActivity().nullSharedIntent();
                    }
                });
                return true;
            }

        } else {
            MainApplicationSingleton.performOnUIHandlerThread(new Runnable() {
                @Override
                public void run() {
//            clears the shared intent
                    sharedIntent = null;
//                    getActivity().selectMainTab();
                    contactItem = new ContactItem();
                    ContactItem contactItem = new ContactItem(intellibitzContactItem);
                    MsgsGrpClutterFragment.this.contactItem.setDataId(contactItem.getIntellibitzId());
                    MsgsGrpClutterFragment.this.contactItem.setName(contactItem.getName());
                    MsgsGrpClutterFragment.this.contactItem.addContact(contactItem);
                    MsgsGrpClutterFragment.this.contactItem.setGroup(false);
                    MsgsGrpClutterFragment.this.contactItem.setType("USER");
//                    createNewChat(contactItem, user);
//            processNewChatMessage(intellibitzId, name);
//                    getActivity().nullSharedIntent();
                }
            });
            return true;
        }
//            clears the shared intent
        sharedIntent = null;
        return false;
    }

    public boolean onBackPressed() {
        return false;
//        return true;
    }

    public void onMessageForward(Intent intent) {

    }

    public void onOkPressed(Intent intent) {
        if (null == intent) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            getActivity().onBackPressed();
            return;
        }
        ContactItem item = intent.getParcelableExtra(
                ContactItem.TAG);
        String source = intent.getAction();
        if (null == item) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            if (ContactSelectFragment.TAG.equals(source)) {
//                new group workflow.. go back to the group screen, for selecting contacts again
                if (contactItem.isNewGroup()) {
/*
                    msgChatGrpContactsDetailFragment =
                            GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                                    contactItem, user);
                    getActivity().removeFragment(intellibitzContactSelectItemFragment);
                    getActivity().removeFragment(msgChatGrpContactsDetailFragment);
                    msgChatGrpContactsDetailFragment.onOkPressed(intent);
                    getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
*/
                    return;
                }
            }
            Log.e(TAG, " cancelled group by pressing back: ");
//            getActivity().onBackPressed();
            return;
        }
//        back button press.. will recreate state.. so the previously selected item , need to be
//        restored back from the back pressed intent from the clicked fragment
        if (null == contactItem) contactItem = item;
        if (contactItem == item) {
            Log.e(TAG, " Already selected contacts: " +
                    contactItem.getSelectedContacts().size());
        } else {
            contactItem.setSelectedContacts(item.getSelectedContacts());
        }
//            merges selected contacts into group contacts and clears selected contacts
        contactItem.mergeSelectedContacts();
        int count = contactItem.getContactItems().size();
//        checks group workflow first.. so the selection can continue for the group
        if (ContactSelectFragment.TAG.equals(source)) {
            if (contactItem.isNewGroup()) {
/*
                msgChatGrpContactsDetailFragment =
                        GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                                contactItem, user);
                getActivity().removeFragment(intellibitzContactSelectItemFragment);
                getActivity().removeFragment(msgChatGrpContactsDetailFragment);
                msgChatGrpContactsDetailFragment.onOkPressed(intent);
                getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
*/
                return;
            } else {
                if (1 == count) {
//            for a single chat, the intellibitz id of a contact becomes the id of msg contact
//            as well as msg thread contact
                    ContactItem contactItem =
                            this.contactItem.getContactItems().iterator().next();
//            ContactItem intellibitzContactItem = contactItem.getIntellibitzContactItem();
                    this.contactItem.setDataId(contactItem.getIntellibitzId());
                    this.contactItem.setName(contactItem.getName());
                    this.contactItem.setGroup(false);
                    this.contactItem.setType("USER");
//                    createNewChat(contactItem, user);
                    return;
                }
//                existing group.. let the parent handle the hard back press
//                getActivity().onBackPressed();
//                return;
            }
        }

//        create group workflow done
        if (count > 1) {
            String name = contactItem.getName();
            if (null == name || name.isEmpty()) {
                Log.e(TAG, "Name is empty - cannot group chat");
                getActivity().onBackPressed();
                return;
            }
/*
            contactItem.setDataId(msgContactItem.getIntellibitzId());
            contactItem.setName(msgContactItem.getName());
*/
            contactItem.setGroup(true);
            contactItem.setType("GROUP");
//            createNewGroupChat(contactItem);
            Log.e(TAG, " selected contacts for Group chat: " + count);
//            getActivity().onBackPressed();
            return;
/*
            if (ContactSelectFragment.TAG.equals(source)) {
                if (contactItem.isNewGroup()) {
                    contactItem.setGroup(true);
                    msgChatGrpContactsDetailFragment =
                            GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                                    contactItem, user);
                    getActivity().removeFragment(intellibitzContactSelectItemFragment);
                    getActivity().removeFragment(msgChatGrpContactsDetailFragment);
                    getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
                    return;
                } else {
//                existing group.. let the parent handle the hard back press
                    getActivity().onBackPressed();
                    return;
                }
            }
*/

//                contactItem.setName(intellibitzContactItem.getName());
        }
//        this will not happen for group, group is always more than 1..
//        if only single selection.. do a single chat
        if (1 == count) {
//            for a single chat, the intellibitz id of a contact becomes the id of msg contact
//            as well as msg thread contact
            ContactItem contactItem =
                    this.contactItem.getContactItems().iterator().next();
//            ContactItem intellibitzContactItem = contactItem.getIntellibitzContactItem();
            this.contactItem.setDataId(contactItem.getIntellibitzId());
            this.contactItem.setName(contactItem.getName());
            this.contactItem.setGroup(false);
            this.contactItem.setType("USER");
//            createNewChat(contactItem, user);
            return;
        }
        Log.e(TAG, " selected contacts: " + count);
        getActivity().onBackPressed();
        return;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Insert desired behavior here.
        onClick(view);
    }

    @Override
    public void onClick(View v) {
        // Insert desired behavior here.
//        Log.d(TAG, "Item clicked: " + v);
        if (R.id.tv_empty == v.getId()) {
            startEmailListActivity();
            return;
        }
        TextView dataId = (TextView) v.findViewById(R.id.tv_id);
        String messageThreadId = dataId.getText().toString();
        showMessageThreadMessage(messageThreadId);
/*
        MessageItem item = MainApplicationSingleton.getBaseItem(messageThreadId, messageItems);
        if (null == item) {
            showMessageThreadMessage(messageThreadId);
        } else {
            showMessageThreadMessage(item, user);
        }
*/
    }

    private void showNewEmailMessage(MessageItem item, ContactItem user) {
        //        creates a message thread
        MessageItem messageItem = new MessageItem();
        messageItem.setDataId(MessageItem.TAG);
        messageItem.setFrom(this.user.getName());
        messageItem.setDocOwnerEmail(this.user.getEmail());
        messageItem.setDocSenderEmail(this.user.getEmail());
        messageItem.setSubject(item.getSubject());
        MessageItem.setMessageThreadEmailAddress(messageItem,
                item.getTo(), item.getCc(), item.getBcc());
        showMessageThreadMessage(messageItem, user);
    }

    private void showNewEmailMessage(String contact, Intent shared) {
        showMessageThreadMessage(MessageItem.TAG, contact, shared);
    }

    private void showNewChatMessage(String contact) {
        showMessageThreadMessage(MessageItem.TAG, contact, null);
    }

    private void showMessageThreadMessage(String messageThreadId, String contact, Intent shared) {
        //        creates a message thread
        MessageItem messageItem = new MessageItem();
        messageItem.setDataId(messageThreadId);
        messageItem.setFrom(user.getName());
        messageItem.setDocOwnerEmail(user.getEmail());
        messageItem.setDocSenderEmail(user.getEmail());
        MessageItem.setMessageThreadEmailAddress(messageItem,
                contact, null, null);

        if (shared != null) {
//            parcel the shared attachments coming from device
            String sharedText = shared.getStringExtra(Intent.EXTRA_TEXT);
            Uri imageUri = shared.getParcelableExtra(Intent.EXTRA_STREAM);
            ArrayList<Uri> imageUris = shared.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            ContactItem deviceContactItem = shared.getParcelableExtra(ContactItem.DEVICE_CONTACT);
            messageItem.setSharedText(sharedText);
            messageItem.setSharedUri(imageUri);
            messageItem.setSharedUris(imageUris);
            messageItem.setSharedDeviceContactItem(deviceContactItem);
        }
        showMessageThreadMessage(messageItem, user);
    }

    private void showMessageThreadMessage(String messageThreadId) {
        //        creates a message thread
        MessageItem messageItem = new MessageItem();
        messageItem.setDataId(messageThreadId);
        showMessageThreadMessage(messageItem, user);
    }

    private void showMessageThreadMessage(MessageItem messageItem, ContactItem user) {
        if (null == clutterTopicListener) {
            Log.e(TAG, "clutterTopicListener is NULL");
            return;
        }
        //        // TODO: 13-03-2016
//        loads the complete message thread, with all messages and all attachments
//        handles new
        String id = messageItem.getDataId();
        if (id != null && !id.isEmpty() && !MessageItem.TAG.equals(id)) {
            MsgsGrpClutterContentProvider.query(messageItem, getActivity());
/*
            MsgsGrpClutterContentProvider.queryMessageThreadFullJoin(messageItem, getActivity());
*/
        }
        clutterTopicListener.onClutterTopicClicked(messageItem, user);
/*
        if (twoPane) {
//            main activity can handle the two pane layout, since it can be more fragment aware
        } else {
            ClutterEmailFragment emailDetailFragment = (ClutterEmailFragment)
                    ClutterEmailFragment.newInstance(clutterTopicListener, getActivity(), twoPane, messageItem, user);
*/
/*
            Context context = getActivity();
            Intent intent = new Intent(context, EmailDetailActivity.class);
            intent.putExtra(ContactItem.TAG, (Parcelable) user);
            intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
            context.startActivity(intent);
*//*

        }
*/
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

    private void deleteMessages() {
        execDeleteMsgsTask(new String[]{selectedItem.getDataId()});
    }

    @Override
    public void onPostDeleteMsgsResponse(JSONObject response, String[] item) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "Delete Msgs SUCCESS - " + response);
                try {
                    int count = MsgsGrpClutterContentProvider.deleteMsgs(item, getContext());
                    Log.e(TAG, "Delete in DB: " + count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (-1 == status) {
                onPostDeleteMsgsErrorResponse(response, item);
            } else if (99 == status) {
                onPostDeleteMsgsErrorResponse(response, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostDeleteMsgsErrorResponse(JSONObject response, String[] item) {
        Log.e(TAG, "Delete Msgs FAIL - " + response + ": " + Arrays.toString(item));
    }

    private void setupSnackBar(View view) {
        snackbar = Snackbar.make(
                view, R.string.empty_email_account, Snackbar.LENGTH_LONG);
        snackbar.setAction("ADD EMAIL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmailListActivity();
            }
        });
    }

    private void setupFAB(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewMessageDialog();
            }
        });
    }

    public void showNewMessageDialog() {
        // Create an instance of the dialog fragment and show it
//        // TODO: 25-04-2016
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
                getActivity().getSupportFragmentManager(), "NewMessageDialog");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
//        // TODO: 16-03-2016
//        user = dialog.getArguments().getParcelable(ContactItem.TAG);
//        getActivity().notifyUserBaseItemListeners();
        NewEmailDialogFragment newEmailDialogFragment = (NewEmailDialogFragment) dialog;
        if (newEmailDialogFragment.isChatMode()) {
//            performNewChat(newEmailDialogFragment);
        } else {
            performNewEmail(newEmailDialogFragment);
        }
    }

    private void performNewEmail(NewEmailDialogFragment newEmailDialogFragment) {
//        user must be signed into atleast one email account
        MessageItem messageItem = new MessageItem();
//        new message.. sets id to a constant.. global new message id
        messageItem.setDataId(MessageItem.TAG);
        messageItem.setBaseType("THREAD");
        messageItem.setDocType("THREAD");
        messageItem.setDataRev("1");
        messageItem.setFrom(user.getName());
        messageItem.setSubject(newEmailDialogFragment.getSubject());
        messageItem.setDocOwner(user.getDocOwner());
        messageItem.setDocSender(user.getName());
        messageItem.setDocOwnerEmail(user.getEmail());
        messageItem.setDocSenderEmail(user.getEmail());
        messageItem.setTimestamp(System.currentTimeMillis());
        Rfc822Token[] to = newEmailDialogFragment.getTo();
        Rfc822Token[] cc = newEmailDialogFragment.getCc();
        Rfc822Token[] bcc = newEmailDialogFragment.getBcc();
/*
        String to = newEmailDialogFragment.getTo();
        String cc = newEmailDialogFragment.getCc();
        String bcc = newEmailDialogFragment.getBcc();
*/
        MessageItem.setMessageThreadEmailAddress(messageItem, to, cc, bcc);
        messageItem.setDocOwner(user.getDocOwner());
        messageItem.setDocSender(user.getName());
        messageItem.setDocOwnerEmail(user.getEmail());
        messageItem.setDocSenderEmail(user.getEmail());
        messageItem.setTimestamp(System.currentTimeMillis());

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
        Intent intent = new Intent();
        if (newEmailDialogFragment.isChatMode()) {
//            performNewChat(newEmailDialogFragment);
//            setCurrentItem(2);
            intent.setAction(MainApplicationSingleton.BROADCAST_NEW_CHAT_DIALOG_OK);
        } else {
            performNewEmail(newEmailDialogFragment);
//            setCurrentItem(1);
            intent.setAction(MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK);
        }
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    public boolean onDetailQueryTextSubmit(String query) {
/*
        return null != intellibitzContactSelectItemFragment &&
                intellibitzContactSelectItemFragment.onQueryTextSubmit(query);
*/
        return onQueryTextSubmit(query);
    }

    public boolean onDetailQueryTextChange(String newText) {
/*
        return null != intellibitzContactSelectItemFragment &&
                intellibitzContactSelectItemFragment.onQueryTextChange(newText);
*/
        return onQueryTextChange(newText);
    }

    public boolean onDetailClose() {
/*
        return null != intellibitzContactSelectItemFragment &&
                intellibitzContactSelectItemFragment.onClose();
*/
        return onClose();
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

/*
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are// currently filtering.
        if (MainApplicationSingleton.MSGSGRPCLUTTER_LOADERID == id) {
            if (clutterTopicListener != null) {
                clutterTopicListener.onClutterTopicsLoaded(0);
            }
            // Now createFileInES and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
//        // TODO: 19-02-2016
//        to set the context right, else the loader will fail from the calling activity
//        return new CursorLoader(context, messageThreadUri,
//===========================================================================
//            IMPORTANT
//  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
// MT AS PREFIX IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
//===========================================================================
            String selection = " ( " + MessageItemColumns.KEY_SUBJECT +
                    " IS NULL OR " +
                    MessageItemColumns.KEY_SUBJECT +
                    " like ? ) ";
            selection += " AND ( " + MessageItemColumns.KEY_TYPE +
                    " = 'EMAIL' ) ";
            selection += " AND ( " +
                    MessageItemColumns.KEY_DEVICE_CONTACTID +
                    " = 0 OR " +
                    MessageItemColumns.KEY_DEVICE_CONTACTID +
                    " IS NULL OR " +
                    MessageItemColumns.KEY_IS_GROUP +
                    " = 0 ) " +
                    " AND ( " +
                    MessageItemColumns.KEY_DOC_TYPE +
                    " = 'MSG' ) ";
            String[] selArgs;
            if (filter != null && !filter.isEmpty()) {
                selArgs = new String[]{"%" + filter + "%"};
            } else {
                selArgs = new String[]{"%%"};
            }
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
            return new CursorLoader(getActivity(),
                    MsgsGrpClutterContentProvider.CONTENT_URI,
                    null,
                    selection, selArgs,
                    MessageItemColumns.KEY_TIMESTAMP +
                            " DESC");
        }
//        returns an empty dummy cursor.. for the cursor loader to play game
        return new CursorLoader(getActivity(),
                MsgsGrpClutterContentProvider.CONTENT_URI,
                null,
                MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{"0"}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.MSGSGRPCLUTTER_LOADERID == loader.getId()) {
            if (null == cursor) {
                setAttachmentItems(null);
                createRecycleAdapter();
                showEmpty();
                return;
            }
            int count = cursor.getCount();
            if (clutterTopicListener != null) {
                clutterTopicListener.onClutterTopicsLoaded(count);
            }
            if (0 == count) {
                cursor.close();
                setAttachmentItems(null);
                createRecycleAdapter();
                showEmpty();
                return;
            }
            if (count > 0 && 0 == cursor.getPosition()) {
                hideEmpty();
                setAttachmentItems(null);
                fillItemsFromLoader(cursor);
                cursor.close();
                createRecycleAdapter();
            }
        }
    }

    public void fillItemsFromLoader(Cursor cursor) {
        setAttachmentItems(
                MessageChatContentProvider.fillMessageThreadItemsFromCursor(cursor));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.MSGSGRPCLUTTER_LOADERID == loader.getId()) {
            setAttachmentItems(null);
            createRecycleAdapter();
            showEmpty();
        }
    }
*/

    @Override
    public void onFetchMsgsGrpClutterTaskExecute(ArrayList<MessageItem> messageItems) {
        setMessageItems(messageItems);
        createRecycleAdapter();
    }

    @Override
    public void onFetchMsgsGrpClutterTaskExecuteFail(ArrayList<MessageItem> messageItems) {
        setMessageItems(messageItems);
        createRecycleAdapter();
        Log.e(TAG, "onFetchMsgsGrpClutterTaskExecuteFail");
    }

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements
            BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private List<MessageItem> messageItems;
        private BitmapFromUrlTask bitmapFromUrlTask;

        public RecyclerViewAdapter(List<MessageItem> items) {
            super();
            this.messageItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            bind to the message thread list item view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_msgsgrpclutter_rv, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.messageItem = messageItems.get(position);

            holder.tvId.setText(holder.messageItem.getDataId());

            String name = holder.messageItem.getName();
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.messageItem.getDisplayName()))
                name = holder.messageItem.getDisplayName();
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.messageItem.getFirstName())) {
                name = holder.messageItem.getFirstName();
                if (!TextUtils.isEmpty(holder.messageItem.getLastName())) {
                    name += " " + holder.messageItem.getLastName();
                }
            }
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.messageItem.getIntellibitzId())) {
                name = holder.messageItem.getIntellibitzId();
            }
            holder.tvName.setText(name);

            holder.tvId.setText(holder.messageItem.getDataId());
//            sets from
            String from = holder.messageItem.getDocSender();
            if (null == from)
                from = holder.messageItem.getFrom();
            String to = holder.messageItem.getTo();
            String cc = holder.messageItem.getCc();
            String val = MessageItem.getSingleAddress(name, to, cc,
                    user.getEmail(), user.getName());
            Drawable drawable = getDrawable(
                    R.drawable.ic_person_outline_black_18dp, getActivity().getTheme());
            assert drawable != null;
            drawable.setBounds(new Rect(0, 0, 20, 20));

            if (MainApplicationSingleton.isAPI17()) {
                holder.tvFrom.setCompoundDrawablesRelative(drawable, null, null, null);
            } else {
                holder.tvFrom.setCompoundDrawables(drawable, null, null, null);
            }
            holder.tvFrom.setText(val);

//            sets subject
            val = holder.messageItem.getSubject();
            if (holder.messageItem.getHasAttachments()) {
                drawable = getDrawable(
                        R.drawable.ic_link_black_18dp, getActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 20, 20));
                holder.tvSubject.setCompoundDrawables(drawable, null, null, null);
            }
            holder.tvSubject.setText(val);

            String latestMessageText = holder.messageItem.getLatestMessageText();
            if (TextUtils.isEmpty(latestMessageText)) latestMessageText = "";
            if (TextUtils.isEmpty(latestMessageText) && !TextUtils.isEmpty(holder.messageItem.getText())) {
                latestMessageText = holder.messageItem.getText();
            }
            if (TextUtils.isEmpty(latestMessageText) && !TextUtils.isEmpty(holder.messageItem.getHtml())) {
                latestMessageText = holder.messageItem.getHtml();
            }
            latestMessageText = latestMessageText.replace("\n", " ");
            holder.tvMessage.setText(latestMessageText);

//            sets timestamp
//                    converts to milliseconds, for correct date conversion
            long createDate = holder.messageItem.getTimestamp() * 1000;
            Timestamp timestamp = new Timestamp(createDate);
            long now = System.currentTimeMillis();
            DateFormat df = new SimpleDateFormat("MMM dd", Locale.getDefault());
            if ((now - createDate) < 1000 * 60 * 60 * 24) {
                df = new SimpleDateFormat("hh mm a", Locale.getDefault());
            }
            String dt = df.format(timestamp.getTime());
            holder.tvTimestamp.setText(dt);

//            sets sender
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
// generate random color
//                    int color1 = generator.getRandomColor();
// generate color based on a key (same key returns the same color), useful for list/grid views
            val = holder.messageItem.getFrom();
            if (val == null) val = "E";
            int color2 = generator.getColor(val);

// declare the builder object once.
            TextDrawable.IBuilder builder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();

// reuse the builder specs to createFileInES multiple drawables
//                    TextDrawable ic1 = builder.build("A", color1);
            TextDrawable ic2 = builder.build(val.substring(0, 1), color2);
//            holder.ivSender.setImageDrawable(ic2);
            String pic = holder.messageItem.getProfilePic();
            if (null == pic || pic.isEmpty()) {
//                holder.ivSender.setImageDrawable(ic2);

/*
                TextDrawable textDrawable = ColorGenerator.getTextDrawable(name);
                textDrawable.setBounds(new Rect(0, 0, 100, 100));
                holder.tvTo.setCompoundDrawablesRelative(
                        textDrawable, null, null, null);
*/
            } else if (pic.startsWith("http")) {
                bitmapFromUrlTask = new BitmapFromUrlTask(
                        holder.ivSender, pic, getActivity().getApplicationContext());
                bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                bitmapFromUrlTask.execute();
/*
                    new AsyncGettingBitmapFromUrl(
                            holder.tvTo, pic, getActivity().getApplicationContext()).execute();
*/
            } else {
//                    Uri uri = Uri.parse(pic);
                Bitmap bitmap = null;
                try {
                    bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, getContext());
                    if (null == bitmap) {
// reuse the builder specs to createFileInES multiple drawables
//                    TextDrawable ic1 = builder.build("A", color1);
//                        TextDrawable ic2 = builder.build(val.substring(0, 1), color2);
//            ic2.setBounds(new Rect(0, 0, 30, 30));
                        holder.ivSender.setImageDrawable(ic2);
                    } else {
                        Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), croppedBitmap);
                        bitmapDrawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.ivSender.setImageDrawable(bitmapDrawable);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


//            sets unread count
            generator = ColorGenerator.MATERIAL; // or use DEFAULT
//                    int color2 = generator.getColor(R.id.email_unread_count);
// generate random color
//                    int color1 = generator.getRandomColor();
// generate color based on a key (same key returns the same color), useful for list/grid views
            int anInt = holder.messageItem.getUnreadCount();
            if (0 == anInt) {
                holder.ivUnreadCount.setVisibility(View.INVISIBLE);
                holder.mView.setBackgroundResource(R.drawable.selectable_item_bg);
            } else {
                holder.mView.setBackgroundResource(R.drawable.item_gray_bg_layer);
                val = String.valueOf(anInt);

// declare the builder object once.
                builder = TextDrawable.builder()
                        .beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .roundRect(20);

// reuse the builder specs to createFileInES multiple drawables
//                    TextDrawable ic1 = builder.build("A", color1);
//                    TextDrawable ic2 = builder.build(string.substring(0, 1), color2);
                ic2 = builder.build(val, Color.RED);
                holder.ivUnreadCount.setImageDrawable(ic2);
                holder.ivUnreadCount.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public long getItemId(int position) {
            return messageItems.get(position).get_id();
        }

        @Override
        public int getItemCount() {
            return messageItems.size();
        }

        @Override
        public void onPostBitmapFromUrlExecute(Bitmap bitmap, View view, Context context) {
            if (null == context) return;
            Resources resources = context.getResources();
            if (null == resources) return;
            if (null == bitmap) {
// declare the builder object once.
                TextDrawable.IBuilder builder = TextDrawable.builder()
                        .beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .round();
                ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                TextDrawable ic2 = builder.build("EC", generator.getColor("EC"));
                ((ImageView) view).setImageDrawable(ic2);
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
            public final TextView tvFrom;
            public final TextView tvMessage;
            public final TextView tvTimestamp;
            public final ImageView ivSender;
            public final ImageView ivUnreadCount;
            public final ImageView ivAttachment;
            //            the data
            public MessageItem messageItem;


            public ViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnClickListener(MsgsGrpClutterFragment.this);
                tvId = (TextView) view.findViewById(R.id.tv_id);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                tvSubject = (TextView) view.findViewById(R.id.tv_subject);
                tvFrom = (TextView) view.findViewById(R.id.tv_from);
                tvMessage = (TextView) view.findViewById(R.id.tv_text);
                tvTimestamp = (TextView) view.findViewById(R.id.tv_timestamp);
                ivSender = (ImageView) view.findViewById(R.id.iv_sender);
                ivUnreadCount = (ImageView) view.findViewById(R.id.iv_unread_count);
                ivAttachment = (ImageView) view.findViewById(R.id.iv_attach);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + tvMessage.getText() + "'";
            }

            @Override
            public boolean onLongClick(View v) {
                setSelectedItem(messageItem);
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
