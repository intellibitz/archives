package intellibitz.intellidroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.gcm.GCMInstanceIDListenerService;
import intellibitz.intellidroid.gcm.GCMTokenIntentService;
import intellibitz.intellidroid.task.GetIpTask;
import intellibitz.intellidroid.task.UpdateGroupTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.AddEmailActivity;
import intellibitz.intellidroid.activity.AddEmailsActivity;
import intellibitz.intellidroid.activity.ClutterEmailActivity;
import intellibitz.intellidroid.activity.ClutterEmailsActivity;
import intellibitz.intellidroid.activity.ComposeEmailActivity;
import intellibitz.intellidroid.activity.ContactSelectActivity;
import intellibitz.intellidroid.activity.MessageChatGroupActivity;
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity;
import intellibitz.intellidroid.activity.MsgsGrpDraftActivity;
import intellibitz.intellidroid.activity.PeopleDetailActivity;
import intellibitz.intellidroid.activity.ProfileActivity;
import intellibitz.intellidroid.company.CompanyCreateActivity;
import intellibitz.intellidroid.company.CompanyListActivity;
import intellibitz.intellidroid.company.GetInvitesActivity;
import intellibitz.intellidroid.company.InviteUsersActivity;
import intellibitz.intellidroid.company.InviteUsersTask;
import intellibitz.intellidroid.contact.ContactDetailActivity;
import intellibitz.intellidroid.content.IntellibitzContactContentProvider;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageChatGroupContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MessagesChatContentProvider;
import intellibitz.intellidroid.content.MessagesChatGroupContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.domain.MainSettingsActivity;
import intellibitz.intellidroid.domain.account.EmailAccountListActivity;
import intellibitz.intellidroid.domain.help.IntroScreenActivity;
import intellibitz.intellidroid.fragment.AttachmentsFragment;
import intellibitz.intellidroid.fragment.DeviceContactsFragment;
import intellibitz.intellidroid.fragment.IntellibitzContactsFragment;
import intellibitz.intellidroid.fragment.MsgChatGrpContactsFragment;
import intellibitz.intellidroid.fragment.MsgsGrpClutterFragment;
import intellibitz.intellidroid.fragment.MsgsGrpPeopleChatGroupsFragment;
import intellibitz.intellidroid.fragment.MsgsGrpPeopleChatsFragment;
import intellibitz.intellidroid.fragment.MsgsGrpPeopleEmailsFragment;
import intellibitz.intellidroid.fragment.MsgsGrpPeopleFragment;
import intellibitz.intellidroid.gcm.GCMInstanceIDListenerService;
import intellibitz.intellidroid.gcm.GCMTokenIntentService;
import intellibitz.intellidroid.listener.ClutterListener;
import intellibitz.intellidroid.listener.ClutterTopicListener;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.ContactsTopicListener;
import intellibitz.intellidroid.listener.DeviceContactTopicListener;
import intellibitz.intellidroid.listener.IntellibitzContactTopicListener;
import intellibitz.intellidroid.listener.PeopleDetailListener;
import intellibitz.intellidroid.listener.PeopleHeaderListener;
import intellibitz.intellidroid.listener.PeopleListener;
import intellibitz.intellidroid.listener.PeopleTopicListener;
import intellibitz.intellidroid.service.ChatService;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.service.EmailService;
import intellibitz.intellidroid.service.RcvDocService;
import intellibitz.intellidroid.task.GetIpTask;
import intellibitz.intellidroid.task.UpdateGroupTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.widget.EditGroupDialogFragment;
import intellibitz.intellidroid.widget.NewBottomDialogFragment;
import intellibitz.intellidroid.widget.NewEmailDialogFragment;

import intellibitz.intellidroid.activity.*;
import intellibitz.intellidroid.company.GetInvitesActivity;
import intellibitz.intellidroid.company.InviteUsersActivity;
import intellibitz.intellidroid.company.InviteUsersTask;
import intellibitz.intellidroid.contact.ContactDetailActivity;
import intellibitz.intellidroid.content.*;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.domain.account.EmailAccountListActivity;
import intellibitz.intellidroid.domain.help.IntroScreenActivity;
import intellibitz.intellidroid.fragment.*;
import intellibitz.intellidroid.gcm.GCMInstanceIDListenerService;
import intellibitz.intellidroid.listener.*;
import intellibitz.intellidroid.service.ChatService;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.service.EmailService;
import intellibitz.intellidroid.service.RcvDocService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static intellibitz.intellidroid.util.MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE;
import static intellibitz.intellidroid.util.MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE;

//import intellibitz.intellidroid.activity.MsgsGrpNestActivity;

//import intellibitz.intellidroid.feed.ComposeFeedActivity;
//import intellibitz.intellidroid.feed.FeedsFragment;
//import intellibitz.intellidroid.schedule.SchedulesActivity;

public class IntellibitzActivity extends
        IntellibitzTwoPaneUserActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        Toolbar.OnMenuItemClickListener,
//        TabLayout.OnTabSelectedListener,
        GetIpTask.GetIpTaskListener,
        NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        View.OnClickListener,
        MenuItem.OnMenuItemClickListener,
        EditGroupDialogFragment.OnEditGroupDialogFragmentListener,
//        MainboxListener,
        PeopleListener,
        ClutterListener,
//        ViewModeListener,
        PeopleTopicListener,
        ClutterTopicListener,
        PeopleHeaderListener,
        PeopleDetailListener,
//        IntellibitzActivity.BaseItemListener,
        ContactListener,
        DeviceContactTopicListener,
        ContactsTopicListener,
        IntellibitzContactTopicListener,
        UpdateGroupTask.UpdateGroupTaskListener,
        NewBottomDialogFragment.NewBottomDialogListener, InviteUsersTask.InviteUsersTaskListener {

    private static final String TAG = "IntellibitzActivity";
//    one shot state.. for re-user registration.. coming from registration menu
//    private transient boolean registrationIntent = false;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    /**
     * Messenger for communicating with service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;
    BroadcastReceiver messageToNestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messageToNest(intent);
        }
    };
    BroadcastReceiver messageToDraftReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messageToDraft(intent);
        }
    };
    ArrayList<TabLayout.Tab> tabArrayList = new ArrayList<>();
    HashMap<Integer, TabLayout.Tab> tabHashMap = new HashMap<>();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    BroadcastReceiver emailAccountAddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            userUpdated(intent);
        }
    };
    private MenuItem prevMenuItem;
    private View tbView;
    private AppBarLayout appBarLayout;

    //    the main toolbar at the top
    private Toolbar toolbar;
    //    private TabLayout tabOverflowLayoutFilter;
    View.OnClickListener overflowFilterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntellibitzActivity.this.onClick(v);
        }
    };
    private Toolbar tbarBottom;
    private Toolbar tbarMainbox;
    private Toolbar tbarPeople;
    private Toolbar tbarContacts;
    //    the tabs, in place of the bottom bar
//    private TabLayout tabLayoutBottom;
    private TextView tvToolbarTitle;
    private TextView tvToolbarSubTitle;
    //    private Toolbar detailFilterToolbar;
    //    private TabLayout detailTabLayoutFilter;
//    private TabLayout detailTabOverflowLayoutFilter;
    //    private ProfileListFragment profileListFragment;
//    private NestListFragment nestListFragment;
    private Intent backIntent;
    //    private GetIpTask getIpTask;
    private int currentItem = 0;
    private MessageItem messageItem;
    //    private View view;
    private Snackbar snackbar;
    private FloatingActionButton fab;
    private String subTitle;
    private String title;
    //    private MsgsGrpPeopleFragment msgsGrpPeopleFragment;
//    private MsgsGrpPeopleChatsFragment msgsGrpPeopleChatsFragment;
//    private MsgsGrpClutterFragment msgsGrpClutterFragment;
    private UpdateGroupTask updateGroupTask;
    //    private TextView tvFilter;
//    private TabLayout tabLayoutFilter;
    //    private MainboxListFragment mainboxListFragment;
//    private MainListFragment mainboxListFragment;
//    private Toolbar detailToolbar;
    private Intent shared;
    private String onQueryTextSubmit;
    //    private DeviceContactsFragment deviceContactsFragment;
//    private IntellibitzContactsFragment intellibitzContactsFragment;
    private MsgChatGrpContactsFragment groupContactsItemFragment;
    //    private BroadcastContactsFragment broadcastContactsFragment;
    private Intent sharedIntent;
    //    private Toolbar filterToolbar;
    private MenuItem newMsgMenuItem;
    private MenuItem newContactMenuItem;
    private View rbBadgePeople;
    private View rbBadgeInbox;
    private View llTbarMainboxPeopleFilter;
    private View llTbarContactsAllTidyFilter;
    BroadcastReceiver contactProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupContactsFragment(intent);
        }
    };
    /*
        private CheckBox cbChat;
        private CheckBox cbMail;
        private CheckBox cbGroups;
    */
    private RadioButton cbChat;
    private RadioButton cbMail;
    private RadioButton cbGroups;
    BroadcastReceiver contactPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupMessagesFragment(intent);
//            showNewChatMessage(contact);
        }
    };
    BroadcastReceiver contactThreadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupMessagesFragment(intent);
//            showNewChatMessage(contact);
        }
    };
    private ToggleButton toggleButton;
    private BroadcastReceiver forceLogoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainApplicationSingleton.forceLogout(IntellibitzActivity.this);
        }
    };
    private BroadcastReceiver userUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            userUpdated(intent);
        }
    };
    private BroadcastReceiver emailAccountRemovedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            userUpdated(intent);
        }
    };
    private BroadcastReceiver messageSendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messageSend(intent);
        }
    };
    private BroadcastReceiver gcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
            boolean sentToken = MainApplicationSingleton.getInstance(
                    getApplicationContext()).getBooleanValueSP(
                    MainApplicationSingleton.SENT_TOKEN_TO_SERVER);
            if (sentToken) {
//                    mInformationTextView.setText(getString(R.string.gcm_send_message));
            } else {
//                    mInformationTextView.setText(getString(R.string.token_error_message));
            }
        }
    };
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
                        RcvDocService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
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


    public IntellibitzActivity() {
        super();
    }

    private void userUpdated(Intent intent) {
        ContactItem userItem = intent.getParcelableExtra(ContactItem.USER_CONTACT);
//        updates only, if the changes were done to this instance of user
        if (userItem != null && userItem == this.user) {
            this.user = userItem;
            addEmailsNavMenuFromUser(this.user);
            notifyUserBaseItemListeners();
        }
    }

    public Intent getBackIntent() {
        return backIntent;
    }

    public void setBackIntent(Intent backIntent) {
        this.backIntent = backIntent;
    }

    private void unregisterReceiver() {
    }

    private void registerReceiver() {
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this, RcvDocService.class), mConnection,
                Context.BIND_AUTO_CREATE);
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
                            RcvDocService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
//            mCallbackText.setText("Unbinding.");
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
//        unRegsiterALLReceivers();
        doUnbindService();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        stopALLServices();
        unRegsiterALLReceivers();
        doUnbindService();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alertReadContacts() && alertReadStorage() && alertWriteStorage()) ;
/*
        if (-1 == getSelectedTabPosition()) {
            selectMainTab();
        }
*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intellibitz);
        Intent intent = getIntent();
        if (null == savedInstanceState) {
//            check if this activity is being invoked with intent extras
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
        } else {
//            get user from the saved state
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
        doBindService();
        registerALLReceivers();
        startALLServices();

        setupTwopane();
        setupAppbar();
        setupNavigationDrawer();
        syncNavigationDrawerContent();
        notifyUserBaseItemListeners();
        String action = null;
        if (intent != null) {
            action = intent.getAction();
            if ("intellibitz.intellidroid.EMAIL_LIST".equals(action)) {
//                startEmailListActivity();
                startAddEmailsActivity();
            }
        }
        handleSharedIntent();

/*
        if (!handleSharedIntent()) {
//        setupMessagesFragment();
        selectProfileTab();

        int count = tabLayoutBottom.getTabCount();
        for (int i = 0; i < count; i++) {
            TabLayout.Tab tabAt = tabLayoutBottom.getTabAt(i);
            if (tabAt != null && R.id.action_settings == (int) tabAt.getTag()) {
                tabAt.select();
            }
            TabLayout.Tab tab1 = tabLayoutBottom.getTabAt(0);
            if (tab1 != null)
                tab1.select();
        }
        }
*/
        ;
        if (null == savedInstanceState) {
            checkTbarBottomMainboxIfNoSelection();
        }
    }

    private void checkTbarBottomMainboxIfNoSelection() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_bottom);
        if (-1 == radioGroup.getCheckedRadioButtonId()) {
            checkTbarBottomMainbox();
        }
    }

    private void checkTbarBottomMainbox() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_bottom);
        View childAt = radioGroup.getChildAt(0);
//        childAt.setSelected(true);
        radioGroup.check(R.id.rb_mainbox);
        childAt.callOnClick();
    }

    private void checkTbarMainboxPeople() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tb_mainbox);
        View childAt = radioGroup.getChildAt(0);
//        childAt.setSelected(true);
        radioGroup.check(R.id.rb_people);
        childAt.callOnClick();
    }

    private void checkTbarMainboxInbox() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tb_mainbox);
        View childAt = radioGroup.getChildAt(1);
//        childAt.setSelected(true);
        radioGroup.check(R.id.rb_inbox);
        childAt.callOnClick();
    }

    private void checkBadgeMainboxPeople() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_badge_mainbox);
        View childAt = radioGroup.getChildAt(0);
//        childAt.setSelected(true);
        radioGroup.check(R.id.rb_badge_people);
//        childAt.callOnClick();
    }

    private void checkBadgeMainboxInbox() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_badge_mainbox);
        View childAt = radioGroup.getChildAt(1);
//        childAt.setSelected(true);
        radioGroup.check(R.id.rb_badge_inbox);
//        childAt.callOnClick();
    }

    private void checkTbarContactsAll() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tb_contacts);
        View childAt = radioGroup.getChildAt(0);
//        childAt.setSelected(true);
        radioGroup.check(R.id.rb_tb_contacts_all);
        childAt.callOnClick();
    }

    private boolean isChecked(int rg, int id, boolean flag) {
        if (flag) {
            return isChecked(rg, id);
        } else {
            return isCheckedByPos(rg, id);
        }
    }

    private boolean isChecked(int rg, int id) {
        if (id < 0) return false;
        RadioGroup radioGroup = (RadioGroup) findViewById(rg);
        return isCheckedById(radioGroup, id);
    }

    private boolean isCheckedByPos(int rg, int pos) {
        if (pos < 0) return false;
        RadioGroup radioGroup = (RadioGroup) findViewById(rg);
        return isCheckedByPos(radioGroup, pos);
    }

    private boolean isCheckedById(RadioGroup radioGroup, int id) {
        if (id < 0) return false;
        if (null == radioGroup) return false;
        return id == radioGroup.getCheckedRadioButtonId();
    }

    private boolean isCheckedByPos(RadioGroup radioGroup, int pos) {
        if (pos < 0) return false;
        if (null == radioGroup) return false;
        RadioButton childAt = (RadioButton) radioGroup.getChildAt(pos);
        return childAt != null && childAt.isChecked();
    }

    private boolean isInboxTab() {
        return isChecked(R.id.rg_tb_mainbox, R.id.rb_inbox);
//        return isCheckedByPos(R.id.rg_tb_mainbox, 1);
/*
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tb_mainbox);
        View childAt = radioGroup.getChildAt(1);
        return radioGroup.isShown() && childAt.isSelected();
*/
    }

    private boolean isInboxTab(TabLayout.Tab tab) {
        return isInboxTab();
    }

    private boolean isPeopleTab() {
        return isChecked(R.id.rg_tb_mainbox, R.id.rb_people);
//        return isCheckedByPos(R.id.rg_tb_mainbox, 0);
/*
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tb_mainbox);
        View childAt = radioGroup.getChildAt(0);
        return radioGroup.isShown() && childAt.isSelected();
*/
    }

    private boolean isPeopleTab(TabLayout.Tab tab) {
        return isPeopleTab();
    }

    private boolean isAllContactsTab() {
        return isChecked(R.id.rg_tb_contacts, R.id.rb_tb_contacts_all);
/*
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tb_contacts);
        View childAt = radioGroup.getChildAt(0);
        return radioGroup.isShown() && childAt.isSelected();
*/
    }

    private boolean isWorkContactsTab() {
        return isChecked(R.id.rg_tb_contacts, R.id.rb_tb_contacts_work);
/*
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_tb_contacts);
        View childAt = radioGroup.getChildAt(1);
        return radioGroup.isShown() && childAt.isSelected();
*/
    }


    private boolean isMainTab() {
        return isChecked(R.id.rg_bottom, R.id.rb_mainbox);
//        return isCheckedByPos(R.id.rg_bottom, 0);
/*
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_bottom);
        RadioButton childAt = (RadioButton) radioGroup.getChildAt(0);
        return radioGroup.isShown() && childAt.isCheckedByPos();
*/
    }

    private boolean isContactsTab() {
        return isChecked(R.id.rg_bottom, R.id.rb_contacts);
//        return isCheckedByPos(R.id.rg_bottom, 1);
/*
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_bottom);
        View childAt = radioGroup.getChildAt(1);
        return radioGroup.isShown() && childAt.isSelected();
*/
    }

    private boolean isFilesTab() {
        return isChecked(R.id.rg_bottom, R.id.rb_files);
//        return isCheckedByPos(R.id.rg_bottom, 2);
    }

    private boolean isFeedsTab() {
        return isChecked(R.id.rg_bottom, R.id.rb_feeds);
    }

    private boolean isMainTab(TabLayout.Tab tab) {
        return isMainTab();
    }

    private boolean isMainTab(int id) {
        return isMainTab();
    }

    private boolean isContactsTab(int id) {
        return isContactsTab();
    }

    private boolean isProfileTab(int id) {
        return isContactsTab();
    }

    private boolean isContactsTab(TabLayout.Tab tab) {
        return isContactsTab();
    }

    private boolean isFilesTab(TabLayout.Tab tab) {
        return isFilesTab();
    }

    private void startALLServices() {
        //        if (!startActivationIfUIDOrTokenNull()) {
//        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
//        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
        if (MainApplicationSingleton.checkPlayServices(this)) {
//            FCM DOES NOT REQUIRE THIS
//            startGCMRegistrationService();
            startInstanceIdListenerService();
        }
        getIPAndStartSockets(true);
        startContactService();
/*
            syncNavigationDrawerContent();
            startSocketService(true);
*/
    }

    public void stopALLServices() {
        stopService(new Intent(this, GCMTokenIntentService.class));
        stopService(new Intent(this, GCMInstanceIDListenerService.class));
        stopService(new Intent(this, RcvDocService.class));
        stopService(new Intent(this, EmailService.class));
        stopService(new Intent(this, ChatService.class));
        stopContactService();
    }

    private void registerALLReceivers() {
        registerGCMReceiver();
        registerContactPhoneReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageSendReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_MESSAGES_SEND));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageToDraftReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_MESSAGETO_DRAFT));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageToNestReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_MESSAGETO_NEST));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                contactProfileReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_CONTACT_PROFILE_VIEW));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                emailAccountAddedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_ADDED));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                emailAccountRemovedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_REMOVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                userUpdatedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_USER_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                forceLogoutReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_FORCE_LOGOUT));
    }

    private void registerContactPhoneReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                contactPhoneReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                contactThreadReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_CONTACT_GROUP_SELECTED));
//        }
    }

    private void registerGCMReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(gcmReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_GCM_REGISTRATION_COMPLETE));
    }

    private void unRegsiterALLReceivers() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationAccountBroadcastReceiver);
        unRegisterGCMReceiver();
        unRegisterContactPhoneReceiver();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageSendReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageToDraftReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageToNestReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactProfileReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emailAccountAddedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emailAccountRemovedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userUpdatedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(forceLogoutReceiver);
    }

    private void unRegisterGCMReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gcmReceiver);
    }

    private void unRegisterContactPhoneReceiver() {
//        one on one chat
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactPhoneReceiver);
//        group chat
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactThreadReceiver);
    }

    public void selectProfileTab(MenuItem item) {
        selectProfileTab();
        drawerLayout.closeDrawers();
    }

    private void selectProfileTab() {
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutBottom, R.id.action_settings);
    }

    public void selectMainTab(MenuItem item) {
        selectContactsTab();
        drawerLayout.closeDrawers();
    }

    public void selectMainTab() {
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutBottom, R.id.action_chattymail);
    }

/*
    public void selectNestTab(MenuItem item) {
        selectNestTab();
        drawerLayout.closeDrawers();
    }

    private void selectNestTab() {
        IntellibitzUserFragment.selectTabAtByTag(tabLayoutBottom, R.id.action_nest);
    }
*/

    public void selectContactsTab(MenuItem item) {
        selectContactsTab();
        drawerLayout.closeDrawers();
    }

    private void selectContactsTab() {
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutBottom, R.id.action_contacts);
    }

    public void showBottomToolbar() {
//        tabLayoutBottom.setVisibility(View.VISIBLE);
    }

/*
    private void asyncEmailsFromCloudAndSavesInDb() {
        //        the user can reinstall app.. try to get the emails from cloud
        if (null == user.getEmails() || user.getEmails().isEmpty()) {
            try {
                // syncs email from cloud
//        intent service runs async in its own thread
                Intent intent = new Intent(this, UserEmailIntentService.class);
                intent.setAction(UserEmailIntentService.ACTION_UPDATE_CONTACT_DBEMPTY_URL);
                intent.putExtra(ContactItem.TAG, (Parcelable) user.clone());
                intent.putExtra(UserEmailIntentService.EXTRA_PARAM1,
                        MainApplicationSingleton.getInstance().getLongValueSP(
                                MainApplicationSingleton.ID_PARAM));
                startService(intent);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }
*/

    public void hideBottomToolbar() {
//        tabLayoutBottom.setVisibility(View.GONE);
    }

    public void showDetailToolbar() {
//        toolbar.setVisibility(View.GONE);
//        tabLayoutFilter.setVisibility(View.GONE);
//        tabLayoutBottom.setVisibility(View.GONE);
//        detailToolbar.setVisibility(View.VISIBLE);
    }

    public void hideDetailToolbar() {
//        detailToolbar.setVisibility(View.GONE);
//        detailFilterToolbar.setVisibility(View.GONE);
//        detailTabLayoutFilter.setVisibility(View.GONE);
//        detailTabOverflowLayoutFilter.setVisibility(View.GONE);
    }

    public void showMainToolbar() {
        hideDetailToolbar();
//        detailToolbar.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
//        tabLayoutFilter.setVisibility(View.VISIBLE);
//        tabLayoutBottom.setVisibility(View.VISIBLE);
    }

    private void setupAppbar() {
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        setupToolbar();
        setupMainboxToolbar();
        setupContactsToolbar();
        setupBottomToolbar();
//        setupFilterToolbar();
//        setupDetailToolbar();
//        setupDetailFilterToolbar();
//        setupPeopleFilterView();
    }

    public TextView getTvToolbarTitle() {
        return tvToolbarTitle;
    }

    public void setTvToolbarTitle(int toolbar_messages_title) {
        if (null == tvToolbarTitle) initToolbar();
        if (null == tvToolbarTitle) {
            Log.e(TAG, "Title toolbar NULL - cannot set title");
            return;
        }
        tvToolbarTitle.setText(toolbar_messages_title);
    }

    public void setTvToolbarSubTitle(String title) {
        if (null == tvToolbarSubTitle) initToolbar();
        if (null == tvToolbarSubTitle) {
            Log.e(TAG, "Sub title toolbar NULL - cannot set subtitle");
            return;
        }
        tvToolbarSubTitle.setText(title);
    }

    public void setDetailToolbarSubTitle(String title) {
        getDetailToolbar().setSubtitle(title);
    }

    public void setDetailToolbarTitle(String title) {
        getDetailToolbar().setTitle(title);
    }

    public void setTvToolbarSubTitle(int toolbar_messages_title) {
        if (null == tvToolbarSubTitle) initToolbar();
        if (null == tvToolbarSubTitle) {
            Log.e(TAG, "Sub title toolbar NULL - cannot set subtitle");
            return;
        }
        tvToolbarSubTitle.setText(toolbar_messages_title);
    }

    private void setupToolbar() {
        tbView = findViewById(R.id.ll_toolbar);
        initToolbar();
//        toolbar.setLogo(R.drawable.ic_action_ic_launcher_trans);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setTitle(R.string.app_name);
//            actionBar.setSubtitle(R.string.app_title);
            actionBar.setDisplayUseLogoEnabled(false);
//            actionBar.setDisplayShowTitleEnabled(false);
        }
        setTvToolbarTitle(R.string.app_name);
        setTvToolbarSubTitle(R.string.app_title);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            tvToolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
            tvToolbarSubTitle = (TextView) toolbar.findViewById(R.id.toolbar_subtitle);
        }
    }

    public void showOverFlowFilter() {
//        tabOverflowLayoutFilter.setVisibility(View.VISIBLE);
    }

    public void showFilter() {
//        tabLayoutFilter.setVisibility(View.VISIBLE);
    }

    public void hideFilter() {
//        tabLayoutFilter.setVisibility(View.GONE);
    }

    public void hideOverFlowFilter() {
//        tabOverflowLayoutFilter.setVisibility(View.GONE);
    }

    public void showDetailOverFlowFilter() {
//        detailTabOverflowLayoutFilter.setVisibility(View.VISIBLE);
    }

    public void showDetailFilter() {
//        detailTabLayoutFilter.setVisibility(View.VISIBLE);
    }

    public void hideDetailFilter() {
//        detailTabLayoutFilter.setVisibility(View.GONE);
    }

    public void hideDetailOverFlowFilter() {
//        detailTabOverflowLayoutFilter.setVisibility(View.GONE);
    }

    public void hideFilters() {
        hideItemFilters();
        hideDetailFilters();
    }

    public void hideItemFilters() {
        hideFilter();
        hideOverFlowFilter();
    }

    public void hideDetailFilters() {
        hideDetailFilter();
        hideDetailOverFlowFilter();
    }

    private void setupFilterToolbar() {
//        filterToolbar = (Toolbar) findViewById(R.id.toolbar3);
//        assert filterToolbar != null;
//        setSupportActionBar(filterToolbar);
//        tabLayoutFilter = (TabLayout) findViewById(R.id.tablayout3);
//        tabOverflowLayoutFilter = (TabLayout) findViewById(R.id.tab_layout3);
        hideFilter();
        hideOverFlowFilter();
    }

    public void showTbarMainboxPeopleFilterView() {
        if (llTbarMainboxPeopleFilter != null)
            llTbarMainboxPeopleFilter.setVisibility(View.VISIBLE);
    }

    public void hideTbarMainboxPeopleFilterView() {
        if (llTbarMainboxPeopleFilter != null)
            llTbarMainboxPeopleFilter.setVisibility(View.GONE);
    }

    public void showTbarContactsAllTidyFilterView() {
        if (llTbarContactsAllTidyFilter != null)
            llTbarContactsAllTidyFilter.setVisibility(View.VISIBLE);
    }

    public void hideTbarContactsAllTidyFilterView() {
        if (llTbarContactsAllTidyFilter != null)
            llTbarContactsAllTidyFilter.setVisibility(View.GONE);
    }

    public RadioButton getCbChat() {
        return cbChat;
    }

    public RadioButton getCbMail() {
        return cbMail;
    }

    public RadioButton getCbGroups() {
        return cbGroups;
    }

/*
    private void setupPeopleFilterView() {
        llTbarMainboxPeopleFilter = findViewById(R.id.ll_msgsgrppeople_filter);
        cbChat = (CheckBox) findViewById(R.id.cb_chat);
        cbGroups = (CheckBox) findViewById(R.id.cb_groups);
        cbMail = (CheckBox) findViewById(R.id.cb_mail);

        hideTbarMainboxPeopleFilterView();
    }

    private void setupDetailFilterToolbar() {
//        detailFilterToolbar = (Toolbar) findViewById(R.id.toolbar5);
//        detailTabLayoutFilter = (TabLayout) findViewById(R.id.tablayout5);
//        detailTabOverflowLayoutFilter = (TabLayout) findViewById(R.id.tab_layout5);
        hideDetailFilter();
        hideDetailOverFlowFilter();
    }
*/

    private void setupBottomToolbar() {
        if (null == tbarBottom) {
            tbarBottom = (Toolbar) findViewById(R.id.tb_bottom);
        }
        View view = findViewById(R.id.rb_mainbox);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTbarBottomMainboxClicked();
//                setupMessagesFragment();
            }
        });
        View feeds = findViewById(R.id.rb_feeds);
        feeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTbarBottomFeedsClicked();
            }
        });
        View view2 = findViewById(R.id.rb_contacts);
        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTbarBottomContactsClicked();
//                setupContactsFragment();
            }
        });
        View view3 = findViewById(R.id.rb_files);
        view3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTbarBottomFilesClicked();
//                hideFilters();
//                showFilesFragment();
            }
        });
    }

    private void onTbarBottomFilesClicked() {
        hideTbarPeople();
        hideTbarMainbox();
        hideTbarContacts();
        showFilesFragment();
//        hideFilters();
    }

    private void onTbarBottomFeedsClicked() {
        hideTbarMainbox();
        hideTbarPeople();
        hideTbarContacts();
        showNewMsgMenuItem();
//        showFeedsFragment();
    }

    private void onTbarBottomContactsClicked() {
        hideTbarPeople();
        hideTbarMainbox();
        hideNewMsgMenuItem();
        showContactsTitle();
        showTbarContacts();
        checkTbarContactsAll();
    }

    private void onTbarBottomMainboxClicked() {
        hideTbarContacts();
        showMainboxTitle();
        showNewMsgMenuItem();
        showTbarMainbox();
        checkTbarMainboxPeople();
    }

    private void onTbarContactsAllClicked() {
        showTbarContactsAllTidyFilterView();
        showAllContactsFragment();
    }

    private void onTbarContactsWorkClicked() {
        hideTbarContactsAllTidyFilterView();
        showWorkContactsFragment();
    }

    private void onTbarMainboxInboxClicked() {
        hideTbarPeople();
        showMsgsGrpClutterFragment();
    }

    private void onTbarMainboxPeopleClicked() {
        showMsgsGrpPeopleFragment();
    }

    private void hideTbarContacts() {
        tbarContacts.setVisibility(View.GONE);
    }

    private void showTbarContacts() {
        tbarContacts.setVisibility(View.VISIBLE);
    }

    private void showTbarMainbox() {
        tbarMainbox.setVisibility(View.VISIBLE);
    }

    private void showTbarPeople() {
        tbarPeople.setVisibility(View.VISIBLE);
    }

    private void hideTbarPeople() {
        tbarPeople.setVisibility(View.GONE);
    }

    private void hideTbarMainbox() {
        tbarMainbox.setVisibility(View.GONE);
    }

    private void setupMainboxToolbar() {
        showMainboxTitle();
        if (null == tbarMainbox) {
            tbarMainbox = (Toolbar) findViewById(R.id.tb_mainbox);
            final View people = findViewById(R.id.rb_people);
            rbBadgePeople = findViewById(R.id.rb_badge_people);
            people.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBadgeMainboxPeople();
                    onTbarMainboxPeopleClicked();
                }
            });
            rbBadgePeople.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkTbarMainboxPeople();
                }
            });
            final View inbox = findViewById(R.id.rb_inbox);
            rbBadgeInbox = findViewById(R.id.rb_badge_inbox);
            inbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBadgeMainboxInbox();
                    onTbarMainboxInboxClicked();
                }
            });
            rbBadgeInbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkTbarMainboxInbox();
                }
            });

            hideBadgePeople();
            hideBadgeInbox();

            tbarPeople = (Toolbar) findViewById(R.id.tb_people);
            cbChat = (RadioButton) findViewById(R.id.cb_chat);
            cbGroups = (RadioButton) findViewById(R.id.cb_groups);
            cbMail = (RadioButton) findViewById(R.id.cb_mail);

            llTbarMainboxPeopleFilter = findViewById(R.id.ll_tb_mainbox_people_filter);
            setupTbarMainboxPeopleFilter(1, llTbarMainboxPeopleFilter);

            cbChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((RadioButton) v).isChecked()) {
                        showMsgsGrpPeopleChatsFragment();
                    }
                }
            });
            cbGroups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((RadioButton) v).isChecked()) {
                        showMsgsGrpPeopleChatGroupsFragment();
                    }
                }
            });
            cbMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((RadioButton) v).isChecked()) {
                        showMsgsGrpPeopleEmailsFragment();
                    }
                }
            });
        }

//        setupMainboxFilterToolbar();
//        setupDetailToolbar();
        hideMainboxToolbar();
    }

    private void hideBadgeInbox() {
        rbBadgeInbox.setVisibility(View.GONE);
    }

    private void hideBadgePeople() {
        rbBadgePeople.setVisibility(View.GONE);
    }

    private void showMainboxTitle() {
        setTvToolbarTitle(R.string.mainbox);
    }

    private void setupContactsToolbar() {
        showContactsTitle();
//        setupContactsFilterToolbar();
//        setupContactsDetailToolbar();
        if (null == tbarContacts) {
            tbarContacts = (Toolbar) findViewById(R.id.tb_contacts);
            View all = findViewById(R.id.rb_tb_contacts_all);
            all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTbarContactsAllClicked();
//                    setupContactsFragment();
//                    showAllContactsFragment();
                }
            });
            View work = findViewById(R.id.rb_tb_contacts_work);
            work.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTbarContactsWorkClicked();
//                    showWorkContactsFragment();
                }
            });
            llTbarContactsAllTidyFilter = findViewById(R.id.ll_tidy);
            setupTbarContactsAllTidyFilter(2, llTbarContactsAllTidyFilter);
        }
        hideContactsToolbar();
    }

    private void showContactsTitle() {
        setTvToolbarTitle(R.string.contacts);
    }

    private void hideMainboxToolbar() {
        tbarMainbox.setVisibility(View.GONE);
        tbarPeople.setVisibility(View.GONE);
    }

    private void hideContactsToolbar() {
        tbarContacts.setVisibility(View.GONE);
    }

    private void setupTbarContactsAllTidyFilter(int id, View view) {
        final Switch sw = (Switch) view.findViewById(R.id.sw_tidy);
//        sw.setText(R.string.tidy_up);
        sw.setTag(id);
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sw = ((Switch) v);
                if (sw.isChecked()) {
                    showWorkContactsFragment();
                } else {
                    showAllContactsFragment();
                }
            }
        });
    }

    private void setupTbarMainboxPeopleFilter(int id, View view) {
        toggleButton = (ToggleButton) view.findViewById(R.id.tb_filter);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton.performClick();
            }
        };

        view.setOnClickListener(listener);

        toggleButton.setTag(id);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton sw = ((ToggleButton) v);
                if (sw.isChecked()) {
//                        Drawable drawable = getDrawable(R.drawable.filters_selected);
//                        drawable.setBounds(0, 0, 20, 20);
//                        sw.setCompoundDrawables(null, null, drawable, null);
//                    showTbarMainboxPeopleFilterView();
                    showTbarPeople();
                    showMsgsGrpPeopleChatsFragment();
                } else {
//                        Drawable drawable = getDrawable(R.drawable.filters_closed);
//                        drawable.setBounds(0, 0, 20, 20);
//                        sw.setCompoundDrawables(null, null, drawable, null);
//                    hideTbarMainboxPeopleFilterView();
                    hideTbarPeople();
                    showMsgsGrpPeopleFragment();
                }
                return;
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = 0;
                try {
//                i = Integer.parseInt(tvFilter.getText().toString());
                    i = Integer.parseInt(toggleButton.getText().toString());
                } catch (Throwable ignored) {

                }
                CheckBox cb = ((CheckBox) view);
                if (R.id.cb_chat == cb.getId()) {
                    if (cb.isChecked()) {
                        i++;
                    } else {
                        i--;
                    }

                } else if (R.id.cb_groups == cb.getId()) {
                    if (cb.isChecked()) {
                        i++;
                    } else {
                        i--;

                    }

                } else if (R.id.cb_mail == cb.getId()) {
                    if (cb.isChecked()) {
                        i++;
                    } else {
                        i--;

                    }

                }
                if (i <= 0 || i > 3) {
                    i = 3;
                }
                String count = String.valueOf(i);
//                setOverflowFilterCount(count);
//                    IntellibitzActivity.this.onClick(view);
            }
        };
/*
        RadioButton cbChat = getCbChat();
        cbChat.setOnClickListener(onClickListener);
        RadioButton cbGroups = getCbGroups();
        cbGroups.setOnClickListener(onClickListener);
        RadioButton cbMail = getCbMail();
        cbMail.setOnClickListener(onClickListener);
*/

/*
        int i = 0;

        if (cbChat.isChecked()) i++;
        if (cbGroups.isChecked()) i++;
        if (cbMail.isChecked()) i++;
*/

//        String count = String.valueOf(i);
//            tvFilter.setText(count);
//        setOverflowFilterCount(count);

    }

    private void setOverflowFilterCount(String count) {
        if (null == toggleButton) {
            toggleButton = (ToggleButton) getRootView().findViewById(R.id.tb_filter);
        }
        toggleButton.setText(count);
        toggleButton.setTextOff(count);
        toggleButton.setTextOn(count);
    }

/*
    private void setupBottomToolbar_() {
        tbarBottom = (Toolbar) findViewById(R.id.tb_bottom);
        assert tbarBottom != null;
//        tbarBottom.setDisplayShowTitleEnabled(false);
//        tbarBottom.setLogo(R.drawable.ic_action_ic_launcher_trans);
//        tbarBottom.setDisplayUseLogoEnabled(true);
//        setSupportActionBar(tbarBottom);
        tbarBottom.inflateMenu(R.menu.menu_bottom);
        tbarBottom.setOnMenuItemClickListener(this);
        tabLayoutBottom = (TabLayout) findViewById(R.id.tablayout2);
        assert tabLayoutBottom != null;
        Menu menu = tbarBottom.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            TabLayout.Tab tab = tabLayoutBottom.newTab();
            MenuItem item = menu.getItem(i);
            tab.setTag(item.getItemId());
            tab.setText(item.getTitle());
            tab.setIcon(item.getIcon());
            tabLayoutBottom.addTab(tab, false);
        }
        tabLayoutBottom.addOnTabSelectedListener(this);
        tbarBottom.setVisibility(View.GONE);
    }
*/

    private void setupNavigationDrawer() {
        //        sets up navigation drawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
/*
        Toolbar tbarBottom = (Toolbar) findViewById(R.id.tbarBottom);
        ActionBarDrawerToggle toggle2 = new ActionBarDrawerToggle(
                this, drawerLayout, tbarBottom, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle2);
        toggle2.syncState();
*/

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        setNewEmailNavigationMenuIntent();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setNewEmailNavigationMenuIntent() {
        //        add email account menu
        MenuItem mi = navigationView.getMenu().findItem(R.id.action_email_accounts);
        mi.setIntent(newEmailAccountListIntent());
//        list company
        MenuItem mi2 = navigationView.getMenu().findItem(R.id.mi_company_accounts);
        mi.setIntent(newCompanyListIntent());
    }

    private void syncNavigationDrawerContent() {
        setNewEmailNavigationMenuIntent();
//        sets user profile
        View header = navigationView.getHeaderView(0);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                selectProfileTab(null);
                startProfileActivity(user);
            }
        });
        View profile = header.findViewById(R.id.ll_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity(user);
            }
        });
        ImageView imageView = (ImageView) header.findViewById(R.id.niv_profile);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity(user);
            }
        });
//        sets user profile pic
// show The Image in a ImageView
        if (user != null) {
            ((TextView) header.findViewById(R.id.text1)).setText(user.getName());
            ((TextView) header.findViewById(R.id.text2)).setText(user.getMobile());

            String pic = user.getProfilePic();
            if (null != pic && !pic.isEmpty()) {
                ((NetworkImageView) imageView).setImageUrl(pic,
                        MainApplicationSingleton.getInstance(this).getImageLoader());
            }
            addEmailsNavMenuFromUser(user);
            addCompaniesNavMenuFromUser(user);
        }
    }

    private void addEmailsNavMenuFromUser(ContactItem user) {
        if (null == navigationView) {
            Log.e(TAG, "addEmailsNavMenuFromUser: nav view is null");
            return;
        }
        if (null == user) {
            Log.e(TAG, "addEmailsNavMenuFromUser: user is null");
            return;
        }
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.menu_email_accounts);
        SubMenu subMenu = item.getSubMenu();
        int sz = subMenu.size();
//        remove all emails from nav menu
        for (int i = 0; i < sz; i++) {
            MenuItem menuItem = subMenu.getItem(i);
            int itemId = menuItem.getItemId();
            if (R.id.action_email_accounts == itemId) {
//                    skip
            } else {
                subMenu.removeItem(itemId);
            }
        }
        //        sets email accounts
        Set<ContactItem> emails = user.getContactItems();
        if (emails != null && !emails.isEmpty()) {
            for (ContactItem email : emails) {
                if (null == subMenu.findItem(email.hashCode())) {
                    MenuItem mi = subMenu.add(R.id.menu_email_accounts, email.hashCode(),
                            Menu.FIRST, email.getEmail());
                    mi.setIcon(R.drawable.ic_account_circle_black_18dp);
                    mi.setCheckable(true);
                }
            }
        }
    }

    private void addCompaniesNavMenuFromUser(ContactItem user) {
        if (null == navigationView) {
            Log.e(TAG, "addCompaniesNavMenuFromUser: nav view is null");
            return;
        }
        if (null == user) {
            Log.e(TAG, "addCompaniesNavMenuFromUser: user is null");
            return;
        }
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.gi_company_accounts);
        SubMenu subMenu = item.getSubMenu();
        int sz = subMenu.size();
//        remove all emails from nav menu
        for (int i = 0; i < sz; i++) {
            MenuItem menuItem = subMenu.getItem(i);
            int itemId = menuItem.getItemId();
            if (R.id.mi_company_accounts == itemId) {
//                    skip
            } else {
                subMenu.removeItem(itemId);
            }
        }
        String companyName = user.getCompanyName();
        String companyId = user.getCompanyId();
        if (!TextUtils.isEmpty(companyId)) {
            MenuItem mi = subMenu.add(R.id.gi_company_accounts, companyId.hashCode(),
                    Menu.FIRST, companyName);
            mi.setIcon(R.drawable.other_mail_icon);
            mi.setCheckable(true);
        }
        //        sets email accounts
/*
        Set<ContactItem> emails = user.getEmails();
        if (emails != null && !emails.isEmpty()) {
            for (ContactItem email : emails) {
                if (null == subMenu.findItem(email.hashCode())) {
                    MenuItem mi = subMenu.add(R.id.menu_email_accounts, email.hashCode(),
                            Menu.FIRST, email.getEmail());
                    mi.setIcon(R.drawable.ic_account_circle_black_18dp);
                    mi.setCheckable(true);
                }
            }
        }
*/
    }

    public boolean isEmptyEmailsMenu() {
        Menu menu = navigationView.getMenu();
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.menu_email_accounts);
            if (item != null) {
                SubMenu subMenu = item.getSubMenu();
                return null == subMenu || subMenu.size() <= 1;
            }
        }
        return true;
    }

    public boolean isEmptyCompaniesMenu() {
        Menu menu = navigationView.getMenu();
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.mi_company_accounts);
            if (item != null) {
                SubMenu subMenu = item.getSubMenu();
                return null == subMenu || subMenu.size() <= 1;
            }
        }
        return true;
    }

/*
    private ProfileListFragment setupProfileFragment() {
        if (null == profileListFragment)
            profileListFragment = newProfileFragment();
        profileListFragment.setupAndPrepareChildFragments();
//        replaceContentFragment(profileListFragment);
        return profileListFragment;
    }

    private NestListFragment setupNestFragment() {
        if (null == nestListFragment)
            nestListFragment = newNestFragment();
        nestListFragment.setupAndPrepareChildFragments();
//        replaceContentFragment(nestListFragment);
        return nestListFragment;
    }
*/

    public void messageSend(Intent shared) {
        // TODO: 9/9/16
//        to impl new toolbar selection logic
        if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.messageSend(shared);
            return;
        }
        if (isInboxTab()) {
            MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
            msgsGrpClutterFragment.messageSend(shared);
            return;
        }
    }


/*
    private MainboxListFragment setupMessagesFragment(Intent shared) {
        if (null == mainboxListFragment)
            mainboxListFragment = newMessageFragment();
        mainboxListFragment.setSharedIntent(shared);
        mainboxListFragment.setupAndPrepareChildFragments();
//        replaceContentFragment(mainboxListFragment);
        return mainboxListFragment;

    }

    private MainboxListFragment setupMessagesFragment() {
        if (null == mainboxListFragment)
            mainboxListFragment = newMessageFragment();
        mainboxListFragment.setupAndPrepareChildFragments();

//        replaceContentFragment(mainboxListFragment);
        return mainboxListFragment;
    }
*/

    private void setupMessagesFragment(Intent shared) {
//        if (null == mainboxListFragment)
//            mainboxListFragment = newMessageFragment();
//        mainboxListFragment.setupAndPrepareChildFragments();
        setupMainboxToolbars();
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutFilter, R.id.mi_people);
//        setSharedIntent(shared);
        showNewMsgMenuItem();
//        replaceContentFragment(mainboxListFragment);
//        return mainboxListFragment;

    }

    private void setupMessagesFragment() {
//        if (null == mainboxListFragment)
//            mainboxListFragment = newMessageFragment();
//        mainboxListFragment.setupAndPrepareChildFragments();
        setupMainboxToolbars();
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutFilter, R.id.mi_people);

        showNewMsgMenuItem();
//        replaceContentFragment(mainboxListFragment);
//        return mainboxListFragment;
    }

    private void setupContactsFragment(Intent shared) {
        selectContactsTab();
        setupContactsToolbar();
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutFilter, R.id.mic_all);
        showNewMsgMenuItem();
    }

    private void setupContactsFragment() {
        setupContactsToolbar();
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutFilter, R.id.mic_all);
        hideNewMsgMenuItem();
        hideTbarMainboxPeopleFilterView();
        showNewContactMenuItem();
    }

    private void showWorkContactsFragment() {
        showMainToolbar();
//        if (null == intellibitzContactsFragment)
        IntellibitzContactsFragment intellibitzContactsFragment = newIntellibitzContactItemFragment();
        replaceContentFragment(intellibitzContactsFragment, IntellibitzContactsFragment.TAG);
    }

    private void showAllContactsFragment() {
        showMainToolbar();
//        if (null == deviceContactsFragment)
        DeviceContactsFragment deviceContactsFragment = newDeviceContactItemFragment();
//        setupAllContactsFilter();
        replaceContentFragment(deviceContactsFragment, DeviceContactsFragment.TAG);
    }

    public MsgsGrpClutterFragment showMsgsGrpClutterFragment() {
        showMainToolbar();
        hideTbarMainboxPeopleFilterView();
//        setupClutterFilter();
//        if (null == msgsGrpClutterFragment)
        MsgsGrpClutterFragment msgsGrpClutterFragment = newMsgsGrpClutterFragment();
        replaceContentFragment(msgsGrpClutterFragment, MsgsGrpClutterFragment.TAG);
//        filterToolbar.setVisibility(View.VISIBLE);
//        this can come from 2 places
//        from people item, or from email item
/*
        int position = tabLayoutFilter.getSelectedTabPosition();
        if (R.id.mi_people == tab.getTag()) {
            replaceContentFragment(msgsGrpPeopleFragment);
        } else if (R.id.mi_clutter == position) {
        }
*/
        return msgsGrpClutterFragment;
    }

    public MsgsGrpPeopleFragment showMsgsGrpPeopleFragment() {
        hideDetailToolbar();
        hideDetailFilters();
        showMainToolbar();
        showTbarMainboxPeopleFilterView();
//        showTbarMainboxPeopleFilterView();
//        setupPeopleFilter();
//        cannot remove detail, for two pane.. the detail is always showing
//        removeFragment(chatDetailFragment);
//        chatDetailFragment = null;
//        sets the mode to item, to handle hard press back button
//        viewMode = IntellibitzActivityFragment.VIEW_MODE.ITEM;
//        if (null == msgsGrpPeopleFragment)
        MsgsGrpPeopleFragment msgsGrpPeopleFragment = newMsgsGrpPeopleFragment();
        replaceContentFragment(msgsGrpPeopleFragment, MsgsGrpPeopleFragment.TAG);
//        filterToolbar.setVisibility(View.VISIBLE);
//        this can come from 2 places
//        from people item, or from email item
/*
        int position = tabLayoutFilter.getSelectedTabPosition();
        if (0 == position) {
        } else if (1 == position) {
            replaceContentFragment(msgsGrpClutterFragment);
        }
*/
        return msgsGrpPeopleFragment;
    }

    private void showMsgsGrpPeopleChatsFragment() {
        hideDetailToolbar();
        hideDetailFilters();
        showMainToolbar();
        showTbarMainboxPeopleFilterView();
        MsgsGrpPeopleChatsFragment msgsGrpPeopleChatsFragment = newMsgsGrpPeopleChatsFragment();
        replaceContentFragment(msgsGrpPeopleChatsFragment, MsgsGrpPeopleChatsFragment.TAG);
    }

    private void showMsgsGrpPeopleChatGroupsFragment() {
        hideDetailToolbar();
        hideDetailFilters();
        showMainToolbar();
        showTbarMainboxPeopleFilterView();
        MsgsGrpPeopleChatGroupsFragment msgsGrpPeopleChatGroupsFragment = newMsgsGrpPeopleChatGroupsFragment();
        replaceContentFragment(msgsGrpPeopleChatGroupsFragment, MsgsGrpPeopleChatGroupsFragment.TAG);
    }

    private void showMsgsGrpPeopleEmailsFragment() {
        hideDetailToolbar();
        hideDetailFilters();
        showMainToolbar();
        showTbarMainboxPeopleFilterView();
        MsgsGrpPeopleEmailsFragment msgsGrpPeopleEmailsFragment = newMsgsGrpPeopleEmailsFragment();
        replaceContentFragment(msgsGrpPeopleEmailsFragment, MsgsGrpPeopleEmailsFragment.TAG);
    }

    private MsgsGrpPeopleChatsFragment newMsgsGrpPeopleChatsFragment() {
        MessageItem messageItem = null;
        Intent intent = getIntent();
        if (intent != null) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG);
        }
        return MsgsGrpPeopleChatsFragment.newInstance(messageItem, user, this);
    }

    private MsgsGrpPeopleChatGroupsFragment newMsgsGrpPeopleChatGroupsFragment() {
        MessageItem messageItem = null;
        Intent intent = getIntent();
        if (intent != null) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG);
        }
        return MsgsGrpPeopleChatGroupsFragment.newInstance(messageItem, user, this);
    }

    private MsgsGrpPeopleEmailsFragment newMsgsGrpPeopleEmailsFragment() {
        MessageItem messageItem = null;
        Intent intent = getIntent();
        if (intent != null) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG);
        }
        return MsgsGrpPeopleEmailsFragment.newInstance(messageItem, user, this);
    }

    private MsgsGrpPeopleFragment newMsgsGrpPeopleFragment() {
        MessageItem messageItem = null;
        Intent intent = getIntent();
        if (intent != null) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG);
        }
        return MsgsGrpPeopleFragment.newInstance(messageItem, user, this);
    }

    private MsgsGrpClutterFragment newMsgsGrpClutterFragment() {
        MessageItem messageItem = null;
        Intent intent = getIntent();
        if (intent != null) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG);
        }
        return MsgsGrpClutterFragment.newInstance(messageItem, user, this);
    }

    private AttachmentsFragment showFilesFragment() {
        showFilesTitle();
        hideNewMsgMenuItem();
        hideTbarMainboxPeopleFilterView();
        hideNewContactMenuItem();
        AttachmentsFragment attachmentsFragment = AttachmentsFragment.newInstance(user, this);
        replaceContentFragment(attachmentsFragment, AttachmentsFragment.TAG);
        return attachmentsFragment;
    }

/*
    private FeedsFragment showFeedsFragment() {
        showFeedsTitle();
        hideTbarMainboxPeopleFilterView();
        hideNewContactMenuItem();
        FeedsFragment feedsFragment = FeedsFragment.newInstance(user, this);
        replaceContentFragment(feedsFragment, FeedsFragment.TAG);
        return feedsFragment;
    }
*/

    private void showFilesTitle() {
        setTvToolbarTitle(R.string.files);
    }

    private void showFeedsTitle() {
        setTvToolbarTitle(R.string.feeds);
    }

    public Fragment removeMessageListFragment() {
        hideFilters();
//        return removeFragment(msgsGrpPeopleFragment);
        return null;
    }

    public Fragment removeContactsFragment() {
        hideFilters();
//        return removeFragment(deviceContactsFragment);
        return null;
    }

    public Fragment removeFilesFragment() {
//        hideFilters();
//        return removeFragment(attachmentsFragment);
        return null;
    }

/*
    public Fragment removeNestsFragment() {
        hideFilters();
        return removeFragment(nestListFragment);
    }
*/

/*
    public Fragment removeProfilesFragment() {
        hideFilters();
        return removeFragment(profileListFragment);
    }
*/

    public void popFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            getSupportFragmentManager().popBackStack();
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public Fragment removeFragment(Fragment fragment) {
        if (null == fragment) return null;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            getSupportFragmentManager().popBackStack();
            Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(
                    fragment.getClass().getSimpleName());
            if (fragmentByTag != null)
                fragmentTransaction.remove(fragmentByTag);
            fragmentTransaction.remove(fragment);
//            fragmentTransaction.hide(fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
        return fragment;
    }

    public Fragment removeChildFragment(Fragment fragment) {
        if (null == fragment) return null;
        FragmentManager childFragmentManager = fragment.getChildFragmentManager();
        if (childFragmentManager.getBackStackEntryCount() > 0) {
            FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
//            childFragmentManager.popBackStack();
            Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(
                    fragment.getClass().getSimpleName());
            if (fragmentByTag != null)
                fragmentTransaction.remove(fragmentByTag);
            fragmentTransaction.remove(fragment);
//            fragmentTransaction.hide(fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
        return fragment;
    }

    /**
     * @param fragment the fragment to be replaced in the two pane container
     * @return fragment the replaced fragment
     */
    public Fragment replaceContentFragment(Fragment fragment, String tag) {
        if (null == fragment) {
            return null;
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.two_pane_container, fragment,
        fragmentTransaction.replace(R.id.two_pane_container, fragment, tag);
        fragmentTransaction.commit();
        return fragment;
//        itemView.setVisibility(View.GONE);
//        removeFragment(fragment);
/*
        Fragment twoPaneContainer = getSupportFragmentManager().findFragmentById(R.id.two_pane_container);
        if (twoPaneContainer != null) {
            View view = twoPaneContainer.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
*/
/*
        Fragment prev = getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
*/
//        getSupportFragmentManager().popBackStack();
//        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        Log.d(TAG, "======FRAGS====="+ fragments);
//        Log.d(TAG, "======SZ====="+ getSupportFragmentManager().getBackStackEntryCount());
//        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
//        fragmentTransaction.commitAllowingStateLoss();
//        fragmentTransaction.commitNow();
//        itemView.setVisibility(View.VISIBLE);
    }

    public Fragment replaceContentFragment(Fragment fragment) {
        if (null == fragment) {
            return null;
        }
        return replaceContentFragment(fragment, fragment.getClass().getSimpleName());
    }

    /**
     * @param fragment the detail fragment to be replaced in the two pane container
     * @return fragment the replaced detail fragment
     */
    public Fragment replaceDetailFragment(Fragment fragment) {
        if (null == fragment) return null;
//        itemView.setVisibility(View.GONE);
        removeFragment(fragment);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (twoPane) {
/*
            Fragment twoPaneContainer = getSupportFragmentManager().findFragmentById(
                    R.id.two_pane_empty_container);
            if (twoPaneContainer != null) {
                View view = twoPaneContainer.getView();
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
*/
            detailView.setVisibility(View.GONE);
/*
            Fragment prev = getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
*/
            fragmentTransaction.replace(R.id.two_pane_empty_container, fragment,
//            fragmentTransaction.add(R.id.two_pane_empty_container, fragment,
                    fragment.getClass().getSimpleName());
            detailView.setVisibility(View.VISIBLE);
        } else {
/*
            Fragment twoPaneContainer = getSupportFragmentManager().findFragmentById(
                    R.id.two_pane_container);
            if (twoPaneContainer != null) {
                View view = twoPaneContainer.getView();
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
*/
/*
            Fragment prev = getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
*/
            fragmentTransaction.replace(R.id.two_pane_container, fragment,
//            fragmentTransaction.add(R.id.two_pane_container, fragment,
                    fragment.getClass().getSimpleName());
        }
//        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
//        fragmentTransaction.commitAllowingStateLoss();
        fragmentTransaction.commit();
//        itemView.setVisibility(View.VISIBLE);
        return fragment;
    }

    public Fragment replaceChildDetailFragment(Fragment fragment, Fragment parent) {
        if (null == fragment) return null;
        itemView.setVisibility(View.GONE);
//        removeFragment(fragment);
        FragmentTransaction fragmentTransaction = parent.getChildFragmentManager().beginTransaction();
        if (twoPane) {
            detailView.setVisibility(View.GONE);
            fragmentTransaction.replace(R.id.two_pane_empty_container, fragment,
                    fragment.getClass().getSimpleName());
//            fragmentTransaction.replace(parent.getDataId(), fragment);
            detailView.setVisibility(View.VISIBLE);
        } else {
            fragmentTransaction.replace(R.id.two_pane_container, fragment,
                    fragment.getClass().getSimpleName());
//            fragmentTransaction.replace(parent.getDataId(), fragment);
        }
//        add the child to the back stack.. so the back pressed can be handled by the child
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
        itemView.setVisibility(View.VISIBLE);
        return fragment;
    }

/*
    private ProfileListFragment newProfileFragment() {
        return ProfileListFragment.newInstance(this, twoPane, user);
    }
*/

/*
    private NestListFragment newNestFragment() {
        return NestListFragment.newInstance(this, twoPane, user);
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        newMsgMenuItem = menu.findItem(R.id.action_new_message);
        newContactMenuItem = menu.findItem(R.id.mi_c_contact);
        hideNewContactMenuItem();
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvToolbarTitle.setVisibility(View.GONE);
                    tvToolbarSubTitle.setVisibility(View.GONE);
                    tbView.setVisibility(View.GONE);
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvToolbarTitle.setVisibility(View.GONE);
                    tvToolbarSubTitle.setVisibility(View.GONE);
                    tbView.setVisibility(View.GONE);
                    hideBottomToolbar();
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    tvToolbarTitle.setVisibility(View.VISIBLE);
                    tvToolbarSubTitle.setVisibility(View.VISIBLE);
                    tbView.setVisibility(View.VISIBLE);
                    onQueryClose();
                    showBottomToolbar();
                    return false;
                }
            });
            // use this method for search process
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    onQuerySubmit(query);
                    hideBottomToolbar();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // use this method for auto complete search process
                    onQueryChange(newText);
                    hideBottomToolbar();
                    return false;
                }
            });
//            searchView.setOnQueryTextListener(messagesContentFragment);
//            searchView.setOnCloseListener(messagesContentFragment);
        }
/*
        MenuItem emailAccounts = menu.findItem(R.id.action_email_accounts);
        if (null != emailAccounts) {
            emailAccounts.setIntent(newEmailAccountListIntent());
        }
        MenuItem contactsMenu = menu.findItem(R.id.action_contacts);
        if (null != contactsMenu) {
            contactsMenu.setIntent(newContactListIntent());
        }
        MenuItem settingsMenu = menu.findItem(R.id.action_settings);
        if (null != settingsMenu) {
            settingsMenu.setIntent(newMainSettingsIntent());
        }
*/
//        // TODO: 26-02-2016
//        change this to status action
/*
        MenuItem status = menu.findItem(R.id.action_help);
        if (null != status) {
            status.setIntent(newIntroScreenIntent());
        }
*/
//        // TODO: 26-02-2016
//        change this to starred action
/*
        MenuItem starred = menu.findItem(R.id.action_starred);
        if (null != starred) {
            starred.setIntent(newMainSettingsIntent());
        }
*/

        return super.onCreateOptionsMenu(menu);
    }

    private void showNewMsgMenuItem() {
        hideNewContactMenuItem();
        if (newMsgMenuItem != null)
            newMsgMenuItem.setVisible(true);
    }

    private void hideNewMsgMenuItem() {
        if (newMsgMenuItem != null)
            newMsgMenuItem.setVisible(false);
    }

    private void showNewContactMenuItem() {
        hideNewMsgMenuItem();
        if (newContactMenuItem != null)
            newContactMenuItem.setVisible(true);
    }

    private void hideNewContactMenuItem() {
        if (newContactMenuItem != null)
            newContactMenuItem.setVisible(false);
    }

/*
    private MainboxListFragment newMessageFragment() {
        return MainboxListFragment.newInstance(this, twoPane, user);
    }
*/

/*
    private void setupDetailToolbar() {
        detailToolbar = (Toolbar) findViewById(R.id.toolbar4);
        if (detailToolbar != null) {
            detailToolbar.inflateMenu(R.menu.menu_tb_detail);
            Menu menu = detailToolbar.getMenu();
            MenuItem item = menu.getItem(0);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    onDetailQueryClose();
                    return false;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    onDetailQuerySubmit(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    onDetailQueryChange(newText);
                    return false;
                }
            });
            MenuItem edit = menu.getItem(1);
            edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    onNewMenuDetailClicked();
                    return false;
                }
            });
        }

    }
*/

    private void onQueryClose() {
        if (isMainTab()) {
            if (isInboxTab()) {
                MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
                msgsGrpClutterFragment.onClose();
            } else if (isPeopleTab()) {
                MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
                msgsGrpPeopleFragment.onClose();
            }
            showTabs();
//            return true;
//            mainboxListFragment.onClose();
//        } else if (isNestTab()) {
        } else if (isContactsTab()) {
            if (isAllContactsTab()) {
                DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                        getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
                deviceContactsFragment.onClose();
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onClose();
*/
            } else if (isWorkContactsTab()) {
                IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                        getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
                intellibitzContactsFragment.onClose();
            }
            showContactsTabs();
        } else if (isFilesTab()) {
            AttachmentsFragment attachmentsFragment = (AttachmentsFragment)
                    getSupportFragmentManager().findFragmentByTag(AttachmentsFragment.TAG);
            attachmentsFragment.onClose();

        }
    }

    private void onQueryChange(String query) {
        if (isMainTab()) {
            if (isInboxTab()) {
                MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
                if (msgsGrpClutterFragment != null)
                    msgsGrpClutterFragment.onQueryTextChange(query);
            } else if (isPeopleTab()) {
                MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
                if (msgsGrpPeopleFragment != null)
                    msgsGrpPeopleFragment.onQueryTextChange(query);
            }
            hideTabs();
//            mainboxListFragment.onQueryTextChange(query);
//        } else if (isNestTab()) {
        }
        if (isContactsTab()) {
            if (isAllContactsTab()) {
                DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                        getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
                if (deviceContactsFragment != null)
                    deviceContactsFragment.onQueryTextChange(query);
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onQueryTextChange(newText);
*/
            } else if (isWorkContactsTab()) {
                IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                        getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
                if (intellibitzContactsFragment != null)
                    intellibitzContactsFragment.onQueryTextChange(query);
            }
            hideContactsTabs();
        }
        if (isFilesTab()) {
            AttachmentsFragment attachmentsFragment = (AttachmentsFragment)
                    getSupportFragmentManager().findFragmentByTag(AttachmentsFragment.TAG);
            if (attachmentsFragment != null)
                attachmentsFragment.onQueryTextChange(query);

        }
/*
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onQueryTextChange(query);
*/
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onQueryTextSubmit(query);
*//*

        } else if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onQueryTextChange(query);
        }
        hideContactsTabs();
*/
//        return true;
    }

    private void onQuerySubmit(String query) {
        if (isMainTab()) {
            if (isInboxTab()) {
                MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
                if (msgsGrpClutterFragment != null)
                    msgsGrpClutterFragment.onQueryTextSubmit(query);
            } else if (isPeopleTab()) {
                MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
                if (msgsGrpPeopleFragment != null)
                    msgsGrpPeopleFragment.onQueryTextSubmit(query);
            }
            hideTabs();
//            mainboxListFragment.onQueryTextSubmit(query);
//        } else if (isNestTab()) {
        }
        if (isContactsTab()) {
            if (isAllContactsTab()) {
                DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                        getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
                if (deviceContactsFragment != null)
                    deviceContactsFragment.onQueryTextSubmit(query);
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onQueryTextChange(newText);
*/
            } else if (isWorkContactsTab()) {
                IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                        getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
                if (intellibitzContactsFragment != null)
                    intellibitzContactsFragment.onQueryTextChange(query);
            }
            hideContactsTabs();
        }
        if (isFilesTab()) {
            AttachmentsFragment attachmentsFragment = (AttachmentsFragment)
                    getSupportFragmentManager().findFragmentByTag(AttachmentsFragment.TAG);
            if (attachmentsFragment != null)
                attachmentsFragment.onQueryTextChange(query);

        }
/*
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onQueryTextSubmit(query);
*/
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onQueryTextSubmit(query);
*//*

        } else if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onQueryTextSubmit(query);
        }
        hideContactsTabs();
*/
//        return true;
    }

    private void onDetailQueryClose() {
        if (isMainTab()) {
            if (isInboxTab()) {
//            msgsGrpClutterFragment.onClose();
            } else if (isPeopleTab()) {
                MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
                msgsGrpPeopleFragment.onDetailClose();
//            chatDetailFragment.onClose();
            }
//            mainboxListFragment.onDetailClose();
//        } else if (isNestTab()) {
        } else if (isContactsTab()) {
        } else if (isProfileTab()) {

        }
    }

    private void onDetailQueryChange(String query) {
        if (isMainTab()) {
            if (isInboxTab()) {
//            msgsGrpClutterFragment.onQueryTextChange(newText);
            } else if (isPeopleTab()) {
                MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
                msgsGrpPeopleFragment.onDetailQueryTextChange(query);
//            chatDetailFragment.onQueryTextChange(newText);
            }
//            mainboxListFragment.onDetailQueryTextChange(query);
//        } else if (isNestTab()) {
        } else if (isContactsTab()) {
        } else if (isProfileTab()) {

        }
    }

    private void onDetailQuerySubmit(String query) {
        if (isMainTab()) {
            if (isInboxTab()) {
//            msgsGrpClutterFragment.onQueryTextSubmit(query);
            } else if (isPeopleTab()) {
                MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                        getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
                msgsGrpPeopleFragment.onDetailQueryTextSubmit(query);
                msgsGrpPeopleFragment.onQueryTextSubmit(query);
            }
//            mainboxListFragment.onDetailQueryTextSubmit(query);
//        } else if (isNestTab()) {
        } else if (isContactsTab()) {
        } else if (isProfileTab()) {

        }
    }

    public int getSelectedTabPosition() {
//        return IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutBottom);
        return -1;
    }

    public TabLayout.Tab getSelectedMainboxTab() {
//        return IntellibitzActivityFragment.getSelectedTab(tabLayoutFilter);
        return null;
    }

    public TabLayout.Tab getSelectedTab() {
//        return IntellibitzActivityFragment.getSelectedTab(tabLayoutBottom);
//        tbarBottom.getview
        return null;
    }

/*
    private boolean isNestTab(int id) {
        return R.id.action_nest == id;
    }
*/


/*
    private boolean isNestTab() {
        TabLayout.Tab tab = getSelectedMainboxTab();
        return isNestTab(tab);
    }

    private boolean isNestTab(TabLayout.Tab tab) {
        return null != tab && R.id.action_nest == (int) tab.getTag();
    }
*/

    private boolean isProfileTab() {
        TabLayout.Tab tab = getSelectedTab();
        return isProfileTab(tab);
    }

    private boolean isProfileTab(TabLayout.Tab tab) {
        return null != tab && R.id.action_settings == (int) tab.getTag();
    }

/*
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (null == tab) return;
        if (R.id.mic_all == (int) tab.getTag()) {
            removeAllContacts();
            return;
        }
        if (R.id.mic_work == (int) tab.getTag()) {
            removeFavContacts();
            return;
        }
        if (isChatTab()) {
            removeChatMessages();
            return;
        }
        if (isMainTab() && isPeopleTab()) {
            removePeopleMessages();
            return;
        }

        if (isMainTab() && isInboxTab()) {
            removeClutterBoxMessages();
            return;
        }

        if (isMainTab()) {
            removeMessageListFragment();
            return;
        }
        if (isContactsTab()) {
            removeContactsFragment();
            return;
        }
        if (isFilesTab()) {
            removeFilesFragment();
            return;
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (null == tab) return;
        if (R.id.mic_all == (int) tab.getTag()) {
            showAllContactsFragment();
            return;
        }
        if (R.id.mic_work == (int) tab.getTag()) {
            showWorkContactsFragment();
            return;
        }
        if (R.id.mi_tidy == (int) tab.getTag()) {
            showWorkContactsFragment();
            return;
        }
        if (isPeopleTab()) {
            onMainboxTabSelected(tab);
            return;
        }

        if (isInboxTab()) {
            onMainboxTabSelected(tab);
            return;
        }

*/
/*
        if (isMainTab()) {
            setupMessagesFragment();
            return;
        }
        if (isContactsTab()) {
            setupContactsFragment();
            return;
        }
        if (isFilesTab()) {
            hideFilters();
            showFilesFragment();
            return;
        }
*//*

*/
/*
        if (isProfileTab()) {
            setupProfileFragment();
            return;
        }
*//*

*/
/*
        if (isNestTab()) {
            setupNestFragment();
            return;
        }
*//*

    }
*/

    public void onMainboxTabSelected(TabLayout.Tab tab) {
        if (null == tab) return;
        boolean people = false, clutter = false, chat = false, group = false,
                email = false, files = false, flagged = false, unread = false;
//        overflow filter can be selected, along with the primary filter
        if (isPeopleTab()) {
            setupPeopleFilter();
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment = showMsgsGrpPeopleFragment();
//            return;
            people = true;
        }
        if (isInboxTab()) {
            setupClutterFilter();
            MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
            msgsGrpClutterFragment = showMsgsGrpClutterFragment();
//            return;
            clutter = true;
        }
        if (isChatTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            } else {
                chat = true;
            }
//            toggleTab(tab);
//            showMsgsGrpPeopleChatsFragment();
//            return;
        }
        if (isGroupTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            } else {
                group = true;
            }
//            showGroupMessages();
//            return;
        }
        if (isEmailTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            } else {
                email = true;
            }
//            showPeopleEmailMessages();
//            return;
        }
        if (isFilesTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            } else {
                files = true;
            }
//            showFilesMessages();
//            return;
        }
        if (isFlaggedTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            } else {
                flagged = true;
            }
//            showMsgsGrpClutterFragment();
//            return;
        }
        if (isUnreadTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            } else {
                unread = true;
            }
//            showMsgsGrpClutterFragment();
//            return;
        }
/*
        if (R.id.mi_reset == (int) tab.getTag()) {
//            toggleTab(tab);
            resetOverflowTabs();
            return;
        }
*/
        chat = isChatSelected();
        group = isGroupSelected();
        email = isEmailSelected();
        files = isFilesSelected();
        flagged = isFlaggedSelected();
        unread = isUnreadSelected();
        onTabSelected(people, clutter, chat, group, email, files, flagged, unread);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (prevMenuItem != null)
            prevMenuItem.getIcon().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
        prevMenuItem = item;
        item.getIcon().setColorFilter(Color.GRAY, PorterDuff.Mode.OVERLAY);
        if (isMainTab(item.getItemId())) {
            setupMessagesFragment();
            return true;
        }
        if (isContactsTab(item.getItemId())) {
            setupContactsFragment();
            return true;
        }
        if (isProfileTab(item.getItemId())) {
//            setupProfileFragment();
            return true;
        }
        if (R.id.action_email_accounts == item.getItemId()) {
//            startEmailListActivity();
            if (isEmptyEmailsMenu()) {
                startAddEmailActivity();
            } else {
                startAddEmailsActivity();
            }
            return true;
        }
        if (R.id.gi_company_accounts == item.getItemId()) {
            if (isEmptyCompaniesMenu()) {
                startAddEmailActivity();
            } else {
                startAddEmailsActivity();
            }
            return true;
        }
        if (R.id.action_settings == item.getItemId()) {
            startProfileActivity(user);
            return true;
        }
        if (R.id.mi_clutter == item.getItemId()) {
            showMsgsGrpClutterFragment();
            return true;
        }
        if (R.id.mi_people == item.getItemId()) {
            showMsgsGrpPeopleFragment();
            return true;
        }
        if (R.id.mi_chat == item.getItemId()) {
            onMenuChatMessages(item);
            return true;
        }
        if (R.id.mi_group == item.getItemId()) {
            showGroupMessages();
            return true;
        }
        if (R.id.mi_email == item.getItemId()) {
            showPeopleEmailMessages();
            return true;
        }
        if (R.id.mi_files == item.getItemId()) {
            showFilesMessages();
            return true;
        }
/*
        if (isNestTab(item.getItemId())) {
            setupNestFragment();
            return true;
        }
*/
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (null != item && item.getItemId() == R.id.action_new_message) {
            if (isFeedsTab()) {
                onNewFeedsMenuClicked();
                return true;
            }
            onNewMenuClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
/*
        if (null != item && item.getItemId() == R.id.action_new_group) {
            createDemoMessageThread();
            showNewBottomDialogFragment();
            return true;
        }
        if (null != item && item.getItemId() == R.id.action_new_message) {
//            createDemoMessageThread();
            showNewBottomDialogFragment();
            return true;
        }
*/
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.mi_schedules) {
            startScheduleActivity(user);
        } else if (id == R.id.mi_create_company) {
            startCompanyCreateActivity(user);
        } else if (id == R.id.mi_join_company) {
            startGetInvitesActivity(user);
        } else if (id == R.id.mi_invite_users) {
            startInviteUsersActivity(user);
        } else if (id == R.id.nav_contact) {
/*
            removeContactsFragment();
            setupContactsFragment();
*/
            selectContactsTab(item);
        } else if (id == R.id.action_settings) {
//            selectProfileTab(item);
            startProfileActivity(user);
/*
        } else if (id == R.id.action_help) {
            startHelpActivity();
*/
        } else if (id == R.id.action_email_accounts) {
//            startEmailListActivity();
            if (isEmptyEmailsMenu()) {
                startAddEmailActivity();
            } else {
                startAddEmailsActivity();
            }
        } else if (id == R.id.mi_company_accounts) {
            startCompanyListActivity(user);
        }

/*
        if ("New Item".equals(item.getTitle())){
            Toast.makeText(this, "New Item", Toast.LENGTH_LONG).show();
        }
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void saveMessageThreadItem(MessageItem item) {
        Message msg = Message.obtain(null,
                RcvDocService.MSG_SET_VALUE, this.hashCode(), 0);
        Bundle args = new Bundle();
        args.putParcelable(MessageItem.TAG, item);
        msg.setData(args);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void getIPAndStartSockets(boolean b) {
        GetIpTask getIpTask = new GetIpTask(b, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GET_IP, this);
        getIpTask.setRequestTimeoutMillis(30000);
        getIpTask.setGetIpTaskListener(this);
        getIpTask.execute();
    }

    @Override
    public void onPostGetIpResponse(JSONObject response, boolean b) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || -1 == status || 99 == status) {
//            retries again..
            onPostGetIpErrorResponse(response, b);
        } else {
            try {
                String url = response.getString("url");
                String ip = response.optString("ip");
                String port = response.optString("port");
                Log.e(TAG, ip + port);
                startSocketService(b, url);
                Log.e(TAG, "onPostGetIpResponse:SUCCESS - " + response);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onPostGetIpErrorResponse(JSONObject response, boolean b) {
        Log.e(TAG, "onPostGetIpErrorResponse:FAIL - " + response);
//            the dynamic socket ip failed, falls back to static socket ip url
        startSocketService(b, MainApplicationSingleton.API_HOST);
        Log.e(TAG, "onPostGetIpResponse:FAIL fallback to - " +
                MainApplicationSingleton.API_HOST);
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (-1 == status) {
            MainApplicationSingleton.forceLogout(this);
        }
    }

    private void startSocketService(boolean b, String socketURL) {
        try {
            ContactItem userForService = getUserCloneForService();
            startsRcvDocService(b, socketURL, userForService);
            startsEmailService(b, socketURL, userForService);
            startsChatService(b, socketURL, userForService);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

    private void startsChatService(boolean b, String socketURL, ContactItem userForService) {
        //        starts the chat service
        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra("url", socketURL);
        intent.putExtra("reconnect", b);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) userForService);
        startService(intent);
    }

    private void startsEmailService(boolean b, String socketURL, ContactItem userForService) {
        //        starts the email service
        Intent intent = new Intent(this, EmailService.class);
        intent.putExtra("url", socketURL);
        intent.putExtra("reconnect", b);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) userForService);
        startService(intent);
    }

    private void startsRcvDocService(boolean b, String socketURL, ContactItem userForService) {
        //        starts the rcvdoc service
        Intent intent = new Intent(this, RcvDocService.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) userForService);
        intent.putExtra("url", socketURL);
        intent.putExtra("reconnect", b);
        startService(intent);
    }

    private void startContactService() {
        try {
            ContactService.startContactService(getUserCloneForService(), this);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

    private void stopContactService() {
        ContactService.stopContactService(this);
    }

    private void startInstanceIdListenerService() {
//        createFileInES a bare user with just the id, good enough to save them in DB
        try {
            GCMInstanceIDListenerService.startInstanceIdListenerService(
                    getUserCloneForService(), this);
            GCMInstanceIDListenerService.updateToken(user, this);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

/*
    private void startGCMRegistrationService() {
        // Start IntentService to register this application with GCM.
//        createFileInES a bare user with just the id, good enough to save them in DB
        try {
            GCMTokenIntentService.startGCMTokenService(getUserCloneForService(), this);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
*/

    @NonNull
    private ContactItem getUserCloneForService() throws CloneNotSupportedException {
        return UserContentProvider.getUserCloneForService(user);
    }

    private void startMainSettingsActivity() {
        startActivity(newMainSettingsIntent());
    }

    private void startHelpActivity() {
        startActivity(newIntroScreenIntent());
    }

    @NonNull
    private Intent newEmailAccountListIntent() {
        try {
            Intent intent = new Intent(this, EmailAccountListActivity.class);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            return intent;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Intent newCompanyListIntent() {
        try {
            Intent intent = new Intent(this, CompanyListActivity.class);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            return intent;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
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
    private void startAddEmailsActivity() {
        Intent intent = new Intent(this, AddEmailsActivity.class);
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) UserContentProvider.getUserCloneForService(user));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAILS_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    private void startAddEmailActivity() {
        Intent intent = new Intent(this, AddEmailActivity.class);
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) UserContentProvider.getUserCloneForService(user));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    public void startEmailListActivity() {
        startActivity(newEmailAccountListIntent());
    }

    @NonNull
    private Intent newMainSettingsIntent() {
        Intent intent = new Intent(this, MainSettingsActivity.class);
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) getUserCloneForService());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return intent;
    }

/*
    private Intent newContactListIntent() {
        Intent intent = new Intent(this, ContactListActivity.class);
        intent.putExtra(ContactItem.TAG, (Parcelable) user);
        return intent;
    }
*/

    @NonNull
    private Intent newIntroScreenIntent() {
        Intent intent = new Intent(this, IntroScreenActivity.class);
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) getUserCloneForService());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return intent;
    }

/*
    private void createDemoMessageThread() {
        MessageItem item = MessagesContentProvider.createDemoMessageThread(user.getEmail());
        saveMessageThreadItem(item);
    }
*/

    public void onNewMenuDetailClicked() {
/*
        int position = IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutBottom);
//        if none of the tab is selected.. outlier.. the app must avoid this logic
        if (-1 == position) {
//            if (mainboxListFragment != null)
//                mainboxListFragment.onNewMenuDetailClicked();
            return;
        }
        TabLayout.Tab tab = tabLayoutBottom.getTabAt(position);
        if (tab != null && R.id.action_chattymail == (int) tab.getTag()) {
//            mainboxListFragment.onNewMenuDetailClicked();
            return;
        }
        if (tab != null && R.id.action_contacts == (int) tab.getTag()) {
//            contactListFragment.onContactsNewMenuClicked();
            return;
        }
*/
/*
        if (tab != null && R.id.action_nest == (int) tab.getTag()) {
//            nestListFragment.onContactsNewMenuClicked();
            return;
        }
*/
    }

    @Override
    public void onNewEmail(NewBottomDialogFragment newBottomDialogFragment) {
        if (TextUtils.isEmpty(user.getEmail())) {
            UserEmailContentProvider.populateUserEmailsJoinById(user, this);
        }
        onNewDialogClose(newBottomDialogFragment);
        startComposeEmailActivity(new MessageItem(), user);
/*
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
                getSupportFragmentManager(), "NewMessageDialog");
*/
    }

    @Override
    public void onNewDialogClose(NewBottomDialogFragment newBottomDialogFragment) {
        newBottomDialogFragment.dismiss();
//        getActivity().onBackPressed();
    }

    @Override
    public void onNewGroup(NewBottomDialogFragment newBottomDialogFragment) {
        onNewDialogClose(newBottomDialogFragment);
/*
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
                getActivity().getSupportFragmentManager(), "NewMessageDialog");
*/
        ContactItem contactItem = new ContactItem();
        contactItem.setNewGroup(true);
        contactItem.setGroup(true);
//        showContactThreadDetail();
        startMsgChatGrpContactsDetailActivity(contactItem);
    }

    @Override
    public void onNewChat(NewBottomDialogFragment newBottomDialogFragment) {
        // Create an instance of the dialog fragment and show it
//        // TODO: 25-04-2016
        onNewDialogClose(newBottomDialogFragment);
/*
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
                getActivity().getSupportFragmentManager(), "NewMessageDialog");
*/
        ContactItem contactItem = new ContactItem();
        contactItem.setNewGroup(false);
        contactItem.setGroup(false);
//        showContactSelectItemFragment();
        startContactSelectActivity(contactItem);
    }

    public void showNewBottomDialogFragment() {
/*
        Activity appCompatActivity = getActivity();
        if (null == appCompatActivity) {
            Log.e(TAG, "showNewBottomDialogFragment: Activity NULL - cannot show new dialog");
            return;
        }
*/
        NewBottomDialogFragment newBottomDialogFragment = new NewBottomDialogFragment();
        newBottomDialogFragment.setNewBottomDialogListener(this);
        newBottomDialogFragment.show(getSupportFragmentManager(), "Create");
    }

    public void onNewFeedsMenuClicked() {
        startComposeFeedActivity(new MessageItem(), user);
    }

    public void onNewMainboxMenuClicked() {
        showNewBottomDialogFragment();
/*
        MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
        msgsGrpPeopleFragment.onNewMenuClicked();
        return;
        int position = tabLayoutFilter.getSelectedTabPosition();
        TabLayout.Tab tab = tabLayoutFilter.getTabAt(position);
        if (tab != null && R.id.mi_people == (int) tab.getTag()) {
//            sends to people
        }
        if (tab != null && R.id.mi_clutter == (int) tab.getTag()) {
//            sends to clutter
//            msgsGrpClutterFragment.onContactsNewMenuClicked();
//            msgsGrpPeopleFragment.onContactsNewMenuClicked();
            return;
        }
        if (tab != null && R.id.mi_chat == (int) tab.getTag()) {
//            sends to people, chat
//            msgsGrpPeopleChatsFragment.onContactsNewMenuClicked();
//            msgsGrpPeopleFragment.onContactsNewMenuClicked();
            return;
        }
*/
    }

    public void onNewMenuClicked() {
        onNewMainboxMenuClicked();
/*
        int position = IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutBottom);
//        if none of the tab is selected.. outlier.. the app must avoid this logic
        if (-1 == position) {
//            if (mainboxListFragment != null)
//                mainboxListFragment.onContactsNewMenuClicked();
            return;
        }
        TabLayout.Tab tab = tabLayoutBottom.getTabAt(position);
        if (isMainTab(tab)) {
//            mainboxListFragment.onContactsNewMenuClicked();
            onNewMainboxMenuClicked();
            return;
        }
        if (isContactsTab(tab)) {
            onContactsNewMenuClicked();
            return;
        }
        if (isProfileTab(tab)) {
            profileListFragment.onNewMenuClicked();
            return;
        }
*/
/*
        if (isNestTab(tab)) {
            nestListFragment.onContactsNewMenuClicked();
            return;
        }
*/
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory: " + level);
        if (isMainTab()) {
/*
            if (nestListFragment != null)
                nestListFragment.onTrimMemory(level);
            if (profileListFragment != null)
                profileListFragment.onTrimMemory(level);
*/
            return;
        }
        if (isContactsTab()) {
//            if (mainboxListFragment != null)
//            onTrimMemory(level);
/*
            if (nestListFragment != null)
                nestListFragment.onTrimMemory(level);
            if (profileListFragment != null)
                profileListFragment.onTrimMemory(level);
*/
            return;

        }
        if (isProfileTab()) {
//            if (mainboxListFragment != null)
//            onTrimMemory(level);
/*
            if (nestListFragment != null)
                nestListFragment.onTrimMemory(level);
*/
            return;

        }
/*
        if (isNestTab()) {
            if (mainboxListFragment != null)
                mainboxListFragment.onTrimMemory(level);
            if (contactListFragment != null)
                contactListFragment.onTrimMemory(level);
            if (profileListFragment != null)
                profileListFragment.onTrimMemory(level);
            return;

        }
*/
    }

    public void onMessageForward(Intent intent) {
//        onBackPressed(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                onMainboxMessageForward(intent);
//                int position = IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutBottom);
//        if none of the tab is selected.. outlier.. the app must avoid this logic
/*
                if (-1 == position) {
                    super.onBackPressed();
                    return;
                }
                TabLayout.Tab tab = tabLayoutBottom.getTabAt(position);
                if (tab != null && R.id.action_chattymail == (int) tab.getTag()) {
                    onMainboxMessageForward(intent);
//                    mainboxListFragment.onMessageForward(intent);
                    return;
                }
*/
/*
                if (tab != null && R.id.action_nest == (int) tab.getTag()) {
//                    nestListFragment.onMessageForward();
                    return;
                }
*/
            }
        }
    }

    public void onMainboxMessageForward(Intent intent) {
//        // TODO: 10/9/16
//        wire up new toolbar logic
//        int position = tabLayoutFilter.getSelectedTabPosition();
        int position = -1;
//        TabLayout.Tab tab = tabLayoutFilter.getTabAt(position);
        TabLayout.Tab tab = null;

        if (tab != null && R.id.mi_people == (int) tab.getTag()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onMessageForward(intent);
            return;
        }
        if (tab != null && R.id.mi_clutter == (int) tab.getTag()) {
            MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
            msgsGrpClutterFragment.onMessageForward(intent);
            return;
        }
        if (tab != null && R.id.mi_chat == (int) tab.getTag()) {
//            msgsGrpPeopleFragment.onBackPressed(intent);
            return;
        }

    }

    private void messageToNest(Intent intent) {
/*
        Intent intent2 = new Intent(this, MsgsGrpNestActivity.class);
        try {
            intent2.putExtra(ContactItem.USER_CONTACT, (Parcelable) getUserCloneForService());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        intent2.putExtras(intent);
        startActivityForResult(intent2, MainApplicationSingleton.ACTIVITY_MSGSGRPNEST_RQ_CODE);
*/
    }

    private void messageToDraft(Intent intent) {
        Intent intent2 = new Intent(this, MsgsGrpDraftActivity.class);
        try {
            intent2.putExtra(ContactItem.USER_CONTACT, (Parcelable) getUserCloneForService());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        intent2.putExtras(intent);
        startActivityForResult(intent2, MainApplicationSingleton.ACTIVITY_MSGSGRPDRAFT_RQ_CODE);
    }

/*
    public void onMessageForwardToNest(Intent intent) {
//        onBackPressed(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                int position = IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutBottom);
//        if none of the tab is selected.. outlier.. the app must avoid this logic
                if (-1 == position) {
                    super.onBackPressed();
                    return;
                }
                if (null == nestListFragment)
                    IntellibitzActivityFragment.selectTabAtByTag(tabLayoutBottom, R.id.action_nest);
                nestListFragment.onMessageForward(intent);
                return;
            }
        }
    }
*/

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
    private void startContactSelectActivity(ContactItem contactItem) {
        Intent intent = new Intent(this, ContactSelectActivity.class);
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
    private void startComposeEmailActivity(MessageItem messageItem, ContactItem user) {
        Intent intent = new Intent(this, ComposeEmailActivity.class);
//        intent.setAction(IntellibitzContactSelectItemFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_COMPOSEEMAIL_RQ_CODE);

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
    private void startComposeFeedActivity(MessageItem messageItem, ContactItem user) {
/*
        Intent intent = new Intent(this, ComposeFeedActivity.class);
//        intent.setAction(IntellibitzContactSelectItemFragment.TAG);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_COMPOSEFEED_RQ_CODE);
*/

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
    public void startMsgChatGrpContactsDetailActivity(ContactItem contactItem) {
        Intent intent = new Intent(this, MsgChatGrpContactsDetailActivity.class);
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
     */
    private void startCompanyCreateActivity(ContactItem user) {
        Intent intent = new Intent(this, CompanyCreateActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_CREATE_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startCompanyListActivity(ContactItem user) {
        Intent intent = new Intent(this, CompanyListActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_LIST_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startGetInvitesActivity(ContactItem user) {
        Intent intent = new Intent(this, GetInvitesActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_GETINVITES_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startInviteUsersActivity(ContactItem user) {
        Intent intent = new Intent(this, InviteUsersActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_INVITE_USERS_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startContactDetailActivity(ContactItem contactItem, ContactItem user) {
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_CONTACT_DETAIL_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
     * @param requestCode the request code with which the activity started
     * @param resultCode  the result code send back by the activity
     * @param data        the intent data with extras
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    ContactItem item = data.getParcelableExtra(ContactItem.USER_CONTACT);
                    HashSet<ContactItem> emails = item.getContactItems();
                    if (emails != null && !emails.isEmpty()) {
                        for (ContactItem email : emails)
                            user.addEmail(email);
                        addEmailsNavMenuFromUser(user);
                    }
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: Add emails cancelled ");
            }
        }
        if (MainApplicationSingleton.ACT_COMPANY_INVITE_USERS_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for invite users
//                    contacts selected for chat
                    ContactItem contactItem = data.getParcelableExtra(ContactItem.TAG);
//            merges selected contacts into group contacts and clears selected contacts
                    contactItem.mergeSelectedContacts();
                    HashSet<ContactItem> contactItems = contactItem.getContactItems();
                    int count = contactItems.size();
                    if (0 == count) {
                        Log.e(TAG, "onActivityResult: Please select 1 contact - " + count);
                    } else {
//                        invites users to company
                        JSONArray emails = new JSONArray();
                        for (ContactItem device : contactItems) {
                            JSONArray email = device.getEmails();
                            if (email != null && email.length() > 0) {
                                for (int i = 0; i < email.length(); i++) {
                                    try {
                                        emails.put(email.getString(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (emails.length() > 0)
                            execInviteUsersTask(emails.toString(), user.getCompanyId(), user);
                        Log.d(TAG, "onActivityResult: " + emails);
                    }
                }
            }
        }
        if (MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    ContactItem contactItem = data.getParcelableExtra(ContactItem.TAG);
//                    if (null == contactItem) contactItem = item;
//                    final int size = contactItem.getSelectedContacts().size();
/*
                    if (contactItem == item) {
                        Log.e(TAG, " Already selected contacts: " + size);
                    } else {
                        contactItem.setSelectedContacts(item.getSelectedContacts());
                    }
*/
//            merges selected contacts into group contacts and clears selected contacts
                    contactItem.mergeSelectedContacts();
                    int count = contactItem.getContactItems().size();
                    if (1 == count) {
//            for a single chat, the intellibitz id of a contact becomes the id of msg contact
//            as well as msg thread contact
                        ContactItem selectedContactItemForChat =
                                contactItem.getContactItems().iterator().next();
                        contactItem.setDataId(selectedContactItemForChat.getIntellibitzId());
                        contactItem.setIntellibitzId(selectedContactItemForChat.getIntellibitzId());
                        contactItem.setTypeId(selectedContactItemForChat.getTypeId());
                        contactItem.setDeviceContactId(selectedContactItemForChat.getDeviceContactId());
                        contactItem.setName(selectedContactItemForChat.getName());
                        contactItem.setGroup(false);
                        contactItem.setEmailItem(false);
                        contactItem.setType("USER");
                        createNewChat(contactItem, user);
                    } else {
                        Log.e(TAG, "onActivityResult: Please select 1 contact - " + count);
                    }
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ");
            }
        } else if (MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    ContactItem contactItem = data.getParcelableExtra(ContactItem.TAG);
                    if (contactItem != null) {
//                        contactItem = item;
                        createNewChat(contactItem, user);
                    }
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ");
            }
        }
/*
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            if (null == deviceContactsFragment) {
                Log.e(TAG, "All tab selected - but fragment null - please check if tab is selected");
                return;
            }
            deviceContactsFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            if (null == intellibitzContactsFragment) {
                Log.e(TAG, "Work tab selected - but fragment null - please check if tab is selected");
                return;
            }
            intellibitzContactsFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
*/
/*
        if (isMainTab()) {
            mainboxListFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (isContactsTab()) {
            contactListFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (isNestTab()) {
            nestListFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
*/
    }

    @Override
    public void onPostInviteUsersResponse(JSONObject response, String companyName, ContactItem user) {
        int status = response.optInt("status", -1);
        if (1 == status) {
            Log.d(TAG, "onPostInviteUsersResponse: " + response);
        } else if (99 == status) {
            onPostInviteUsersErrorResponse(response);
        } else if (-1 == status) {
            onPostInviteUsersErrorResponse(response);
        }
    }

    @Override
    public void onPostInviteUsersErrorResponse(JSONObject response) {
        Log.d(TAG, "onPostInviteUsersErrorResponse: " + response);
    }

    private void createNewChat(ContactItem contactItem, ContactItem user) {
//        getActivity().hideDetailFilters();
        MessageItem item = processNewChatMessage(contactItem);
//        resetRecycleAdapter();
//        showMessageThreadMessage(contactItem, user);
        showMessageThreadMessage(item, user);
    }

    private void showMessageThreadMessage(MessageItem messageItem, ContactItem user) {
        //        // TODO: 13-03-2016
//        loads the complete message thread, with all messages and all attachments
//        handles new
        String id = messageItem.getDataId();
        if (id != null && !id.isEmpty() && !MessageItem.TAG.equals(id)) {
//            // TODO: 09-07-2016
//            chat or email
//            // TODO: 13-07-2016
//            the full joins are for the headers.. can be skipped and let the fragments take care
            if (messageItem.isEmailItem()) {
                MessageEmailContentProvider.queryMessageEmailThreadFullJoin(
                        messageItem, this);

            } else {
                if (messageItem.isGroup()) {
                    MessageChatGroupContentProvider.queryMessageChatThreadFullJoin(
                            messageItem, this);
                } else {
                    MessageChatContentProvider.queryMessageChatThreadFullJoin(messageItem, this);
                }
            }
        }
        onPeopleTopicClicked(messageItem, user);
    }

    private MessageItem processNewChatMessage(ContactItem contactItem) {
        String id = contactItem.getDataId();
        if (id != null) {
            Log.d(TAG, "User ready for Chat: " + id);
            MessageItem item = null;
            item = new MessageItem();
//                    item.setDataId(id);
            item.setDataId(MessageItem.TAG);
            item.setDocOwner(user.getDataId());
            item.setName(contactItem.getName());
//                creates a temp thread, with chat id as message id
//                // TODO: 16-05-2016
//                cloud will generate a brand new message id.. query local db with chatid to sync
            MessageChatContentProvider.createMessageChatThread(id, item);
            if (null == item.getName()) {
                item.setName(item.getDataId());
            }
            item.setContactItem(contactItem);
//                saves the message thread..
//                MessagesContentProvider.saveMessageThreadItemToDB(item, context);
            return item;
        }
        return null;
    }

    public void onOkPressed(Intent intent) {
//        onBackPressed(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                onMainboxOkPressed(intent);
/*
                int position = IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutBottom);
//        if none of the tab is selected.. outlier.. the app must avoid this logic
                if (-1 == position) {
                    super.onBackPressed();
                    return;
                }
                TabLayout.Tab tab = tabLayoutBottom.getTabAt(position);
                if (tab != null && R.id.action_chattymail == (int) tab.getTag()) {
                    onMainboxOkPressed(intent);
//                    mainboxListFragment.onOkPressed(intent);
                    return;
                }
                if (tab != null && R.id.action_contacts == (int) tab.getTag()) {
//                    contactListFragment.onOkPressed(intent);
                    return;
                }
*/
/*
                if (tab != null && R.id.action_nest == (int) tab.getTag()) {
                    nestListFragment.onOkPressed();
                    return;
                }
*//*

                if (tab != null && R.id.action_settings == (int) tab.getTag()) {
                    profileListFragment.onOkPressed();
                    return;
                }
*/
            }
        }
    }

    public void onMainboxOkPressed(Intent intent) {
        if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onOkPressed(intent);
            return;
        }
        if (isInboxTab()) {
//            msgsGrpPeopleFragment.onOkPressed(intent);
//            msgsGrpClutterFragment.onOkPressed(intent);
            return;
        }
/*
        if (tab != null && R.id.mi_chat == (int) tab.getTag()) {
//            msgsGrpPeopleFragment.onBackPressed(intent);
            return;
        }
*/

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
/*
                int position = IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutBottom);
//        if none of the tab is selected.. outlier.. the app must avoid this logic
                if (-1 == position) {
                    super.onBackPressed();
                    return;
                }
                TabLayout.Tab tab = tabLayoutBottom.getTabAt(position);
                if (tab != null && R.id.action_chattymail == (int) tab.getTag()) {
                    mainboxListFragment.onBackPressed();
                    return;
                }
                if (tab != null && R.id.action_contacts == (int) tab.getTag()) {
                    contactListFragment.onBackPressed();
                    return;
                }
*/
/*
                if (tab != null && R.id.action_nest == (int) tab.getTag()) {
                    nestListFragment.onBackPressed();
                    return;
                }
*//*

                if (tab != null && R.id.action_settings == (int) tab.getTag()) {
                    profileListFragment.onBackPressed();
                    return;
                }
*/
            }
        }
    }

    public void nullSharedIntent() {
        sharedIntent = null;

    }

    public Intent getSharedIntent() {
        return sharedIntent;
    }

    public void setSharedIntent(Intent sharedIntent) {
        this.sharedIntent = sharedIntent;
        IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
        if (intellibitzContactsFragment != null)
            intellibitzContactsFragment.setSharedIntent(sharedIntent);
    }

    private boolean handleSharedIntent() {
        //        handles shared intent
        // Get intent, action and MIME type
        sharedIntent = getIntent();
        String action = sharedIntent.getAction();
        String type = sharedIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(sharedIntent); // Handle text being sent
                return true;
            } else if (type.startsWith("image/") || type.startsWith("video/")
                    || type.startsWith("audio/") || type.startsWith("application/")) {
                handleSendImage(sharedIntent); // Handle single image being sent
                return true;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/") || type.startsWith("video/")
                    || type.startsWith("audio/") || type.startsWith("application/")) {
                handleSendMultipleImages(sharedIntent); // Handle multiple images being sent
                return true;
            }
        } else {
            // Handle other intents, such as being started from the home screen
            return false;
        }
        return false;
    }

    void handleSendText(Intent shared) {
        String sharedText = shared.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
//            newContactsFragment(shared);
            // Update UI to reflect text being shared
//            Intent intent = newContactListIntent(shared);
//            startActivity(intent);
        }
    }

    void handleSendImage(Intent shared) {
        Uri imageUri = shared.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
//            newContactsFragment(shared);
            // Update UI to reflect image being shared
//            Intent intent = newContactListIntent(shared);
//            startActivity(intent);
        }
    }

    void handleSendMultipleImages(Intent shared) {
        ArrayList<Uri> imageUris = shared.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
//            newContactsFragment(shared);
            // Update UI to reflect multiple images being shared
//            Intent intent = newContactListIntent(shared);
//            startActivity(intent);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

/*
    public Toolbar getTbarBottom() {
        return tbarBottom;
    }

    public Toolbar getDetailFilterToolbar() {
        return detailFilterToolbar;
    }
*/

/*
    public TabLayout getDetailTabLayoutFilter() {
//        return detailTabLayoutFilter;
    }

    public TabLayout getDetailTabOverflowLayoutFilter() {
//        return detailTabOverflowLayoutFilter;
    }

    public TabLayout getTabOverflowLayoutFilter() {
//        return tabOverflowLayoutFilter;
    }
*/

    public Toolbar getDetailToolbar() {
//        return detailToolbar;
        return null;
    }

    public Toolbar getFilterToolbar() {
//        return filterToolbar;
        return null;
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public TabLayout getTabLayoutFilter() {
//        return tabLayoutFilter;
        return null;
    }

    public View getRootView() {
        return drawerLayout;
    }

    public void emptyOnClick(View view) {
//        startEmailListActivity();
        startAddEmailsActivity();
    }

    @Override
    public void onViewModeChanged() {

    }

    @Override
    public void onViewModeItem() {

    }

    public void setupMainboxToolbars() {
        //        // TODO: 25-04-2016
//        refactor this.. the fragments and the main activity interlude
        setupMainboxToolbar();
//        if (mainActivity != null) {
//            setupFAB(view);
//            setupSnackBar();
//        addBaseItemListener(messagesContentFragment);
//        }
    }

/*
    private void setupMainboxFilterToolbar() {
        clearMainboxFilters();
        Menu menu;
        filterToolbar.inflateMenu(R.menu.menu_messages);
        menu = filterToolbar.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            TabLayout.Tab tab = newTabFromMainboxMenuItem(item);
//            tabLayoutFilter.addTab(tab, false);
//            the local cache
            tabArrayList.add(tab);
            tabHashMap.put(item.getItemId(), tab);
        }
//        tabLayoutFilter.addTab(newFilterTabFromLayout(), false);
//        tabLayoutFilter.addOnTabSelectedListener(this);

        setupClutterFilter();
//        setupPeopleFilter();
//        hideOverFlowFilter();
    }
*/

    private void clearMainboxFilters() {
/*
        filterToolbar = getFilterToolbar();
        assert filterToolbar != null;
        Menu menu = filterToolbar.getMenu();
        menu.clear();
*/
/*
        tabLayoutFilter = getTabLayoutFilter();
        tabLayoutFilter.removeAllTabs();
        tabLayoutFilter.removeOnTabSelectedListener(this);
        tabLayoutFilter.clearOnTabSelectedListeners();
*/
    }

    private void setupClutterFilter() {
        setupMainboxOverflowFilter(R.menu.menu_clutter_overflow);
        showOverFlowFilter();
    }

    private void setupPeopleFilter() {
        setupMainboxOverflowFilter(R.menu.menu_people_overflow);
        showOverFlowFilter();
    }

    private void setupMainboxOverflowFilter(int menuResource) {
        Menu menu;
        int size;
/*
        tabOverflowLayoutFilter = getTabOverflowLayoutFilter();
        tabOverflowLayoutFilter.removeAllTabs();
        tabOverflowLayoutFilter.removeOnTabSelectedListener(this);
        tabOverflowLayoutFilter.clearOnTabSelectedListeners();
*/
/*
        filterToolbar.getMenu().clear();
        filterToolbar.inflateMenu(menuResource);
        menu = filterToolbar.getMenu();
        size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            TabLayout.Tab tab = newOverflowTabFromMainboxMenuItem(item);
//            item.setOnMenuItemClickListener(this);
//            TabLayout.Tab tab = newTabFromMainboxMenuItem(item);
//            tabOverflowLayoutFilter.addTab(tab, false);
            tabArrayList.add(tab);
            tabHashMap.put(item.getItemId(), tab);
        }
        filterToolbar.setOnMenuItemClickListener(this);
*/
//        filterToolbar.setVisibility(View.VISIBLE);
//        tabOverflowLayoutFilter.addOnTabSelectedListener(this);
    }

    @NonNull
    private TabLayout.Tab newTabFromMainboxMenuItem(MenuItem item) {
//        TabLayout.Tab tab = tabLayoutFilter.newTab();
        TabLayout.Tab tab = null;
/*
        tab.setTag(item.getItemId());
        tab.setText(item.getTitle());
        tab.setCustomView(R.layout.mainbox_tab);
        View view = tab.getCustomView();
        if (view != null) {
            TextView text = (TextView) view.findViewById(R.id.tv_tab);
            text.setText(item.getTitle());
        }
*/
/*
        Drawable drawable = item.getIcon();
        drawable.setBounds(new Rect(0, 0, 20, 20));
        text.setCompoundDrawablesRelative(null, drawable, null, null);
*/
        return tab;
    }

/*
    @NonNull
    private TabLayout.Tab newFilterTabFromLayout() {
        TabLayout.Tab tab = tabLayoutFilter.newTab();
//        tab.setTag(item.getItemId());
//        tab.setText(item.getTitle());
        tab.setCustomView(R.layout.tab_filter_row);
//        View view = tab.getCustomView();
//        TextView text = (TextView) view.findViewById(R.id.tv_tab);
//        text.setText(item.getTitle());
        return tab;
    }
*/

    private void toggleTab(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        if (view != null) {
            final ImageButton button = (ImageButton) view.findViewById(R.id.tg_tab);
            button.setSelected(!button.isSelected());
        }
    }

    private ImageButton getMainboxTabViewButton(TabLayout.Tab tab) {
        if (null == tab) return null;
        View view = tab.getCustomView();
        if (null == view) return null;
        ImageButton button = (ImageButton) view.findViewById(R.id.tg_tab);
        return button;
    }

    private boolean isMainboxTabViewButtonSelected(TabLayout.Tab tab) {
        ImageButton button = getMainboxTabViewButton(tab);
        if (null == button) return false;
        return button.isSelected();
    }

    private boolean isChatSelected() {
        return false;
/*
        return isMainboxTabViewButtonSelected(
                IntellibitzUserFragment.findTabAtByTag(tabOverflowLayoutFilter, R.id.mi_chat));
*/
    }

    private boolean isGroupSelected() {
        return false;
/*
        return isMainboxTabViewButtonSelected(
                IntellibitzUserFragment.findTabAtByTag(tabOverflowLayoutFilter, R.id.mi_group));
*/
    }

    private boolean isEmailSelected() {
        return false;
/*
        return isMainboxTabViewButtonSelected(
                IntellibitzUserFragment.findTabAtByTag(tabOverflowLayoutFilter, R.id.mi_email));
*/
    }

    private boolean isFilesSelected() {
        return false;
/*
        return isMainboxTabViewButtonSelected(
                IntellibitzUserFragment.findTabAtByTag(tabOverflowLayoutFilter, R.id.mi_files));
*/
    }

    private boolean isFlaggedSelected() {
        return false;
/*
        return isMainboxTabViewButtonSelected(
                IntellibitzUserFragment.findTabAtByTag(tabOverflowLayoutFilter, R.id.mi_flagged));
*/
    }

    private boolean isUnreadSelected() {
        return false;
/*
        return isMainboxTabViewButtonSelected(
                IntellibitzUserFragment.findTabAtByTag(tabOverflowLayoutFilter, R.id.mi_unread));
*/
    }

    @NonNull
    private TabLayout.Tab newOverflowTabFromMainboxMenuItem(MenuItem item) {
/*
        TabLayout.Tab tab = tabOverflowLayoutFilter.newTab();
        tab.setTag(item.getItemId());
        tab.setCustomView(R.layout.tab_expand_overflow);
        View view = tab.getCustomView();

        if (view != null) {
            view.setOnClickListener(this);

            setupTbarMainboxPeopleFilter(item.getItemId(), view);

*/
/*
            tvFilter = (TextView) view.findViewById(R.id.tv_filter);
            tvFilter.setTag(item.getItemId());
            tvFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleButton.performClick();
                }
            });
*//*


        }
        return tab;
*/
        return null;
    }

    //    @Override
    public boolean onMenuItemClick_(MenuItem item) {
        if (R.id.mi_clutter == item.getItemId()) {
            showMsgsGrpClutterFragment();
            return true;
        }
        if (R.id.mi_people == item.getItemId()) {
            showMsgsGrpPeopleFragment();
            return true;
        }
        if (R.id.mi_chat == item.getItemId()) {
            onMenuChatMessages(item);
            return true;
        }
        if (R.id.mi_group == item.getItemId()) {
            showGroupMessages();
            return true;
        }
        if (R.id.mi_email == item.getItemId()) {
            showPeopleEmailMessages();
            return true;
        }
        if (R.id.mi_files == item.getItemId()) {
            showFilesMessages();
            return true;
        }
/*
        if (R.id.mi_reset == item.getItemId()) {
            resetOverflowTabs();
            return true;
        }
*/
/*
        if (android.R.id.home == item.getItemId()) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(mainActivity, new Intent(mainActivity, IntellibitzActivity.class));
            return true;
        }
        return super.onop
*/
        return false;
    }


/*
    public TabLayout.Tab getSelectedMainboxTab() {
        return IntellibitzUserFragment.getSelectedMainboxTab(tabLayoutFilter);
    }
*/

    public TabLayout.Tab getSelectedOverflowTab() {
//        return IntellibitzUserFragment.getSelectedTab(tabOverflowLayoutFilter);
        return null;
    }

    private boolean isChatTab() {
        TabLayout.Tab tab = getSelectedOverflowTab();
        return isChatTab(tab);
    }

    private boolean isChatTab(TabLayout.Tab tab) {
        return null != tab &&
                null != tab.getTag() &&
                R.id.mi_chat == (int) tab.getTag();
    }

    private boolean isGroupTab() {
        TabLayout.Tab tab = getSelectedOverflowTab();
        return isGroupTab(tab);
    }

    private boolean isGroupTab(TabLayout.Tab tab) {
        return null != tab &&
                null != tab.getTag() &&
                R.id.mi_group == (int) tab.getTag();
    }

    private boolean isEmailTab() {
        TabLayout.Tab tab = getSelectedOverflowTab();
        return isEmailTab(tab);
    }

    private boolean isEmailTab(TabLayout.Tab tab) {
        return null != tab &&
                null != tab.getTag() &&
                R.id.mi_email == (int) tab.getTag();
    }

/*
    private boolean isFilesTab() {
        TabLayout.Tab tab = getSelectedMainboxOverflowTab();
        return isFilesTab(tab);
    }
*/

/*
    private boolean isFilesTab(TabLayout.Tab tab) {
        return null != tab &&
                null != tab.getTag() &&
                R.id.mi_files == (int) tab.getTag();
    }
*/

    private boolean isFlaggedTab() {
        TabLayout.Tab tab = getSelectedOverflowTab();
        return isFlaggedTab(tab);
    }

    private boolean isFlaggedTab(TabLayout.Tab tab) {
        return null != tab &&
                null != tab.getTag() &&
                R.id.mi_flagged == (int) tab.getTag();
    }

    private boolean isUnreadTab() {
        TabLayout.Tab tab = getSelectedOverflowTab();
        return isUnreadTab(tab);
    }

    private boolean isUnreadTab(TabLayout.Tab tab) {
        return null != tab &&
                null != tab.getTag() &&
                R.id.mi_unread == (int) tab.getTag();
    }

    private boolean isChatTab(View view) {
        return null != view &&
                null != view.getTag() &&
                R.id.mi_chat == (int) view.getTag();
    }

    private boolean isGroupTab(View view) {
        return null != view &&
                null != view.getTag() &&
                R.id.mi_group == (int) view.getTag();
    }

    private boolean isEmailTab(View view) {
        return null != view &&
                null != view.getTag() &&
                R.id.mi_email == (int) view.getTag();
    }

    private boolean isFilesTab(View view) {
        return null != view &&
                null != view.getTag() &&
                R.id.mi_files == (int) view.getTag();
    }

    private boolean isFlaggedTab(View view) {
        return null != view &&
                null != view.getTag() &&
                R.id.mi_flagged == (int) view.getTag();
    }

    private boolean isUnreadTab(View view) {
        return null != view &&
                null != view.getTag() &&
                R.id.mi_unread == (int) view.getTag();
    }

/*
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (null == tab) return;
        if (isInboxTab()) {
            removeClutterBoxMessages();
            return;
        }
        if (isPeopleTab()) {
            removePeopleMessages();
            return;
        }
        if (isChatTab()) {
            removeChatMessages();
            return;
        }
    }
*/

    @Override
    public void onClick(View view) {
//        int position = tabLayoutFilter.getSelectedTabPosition();
        int position = -1;
        if (0 == position) {
            showAllContactsFragment();
        } else if (1 == position) {
            showWorkContactsFragment();
        } else if (2 == position) {
        }
//        this image button clicked.. check for selection state
        int id = -1;
        Integer val = (Integer) view.getTag();
        if (val != null) id = val;
        boolean people = false, clutter = false, chat = false, group = false,
                email = false, files = false, flagged = false, unread = false;
        if (isChatTab(view)) {
//            toggleTab(tab);
            if (id != -1 && view.isSelected()) {

            } else {
                chat = true;
            }
//            showMsgsGrpPeopleChatsFragment();
//            return;
        }
        if (isGroupTab(view)) {
            if (id != -1 && view.isSelected()) {

            } else {
                group = true;
            }
//            showGroupMessages();
//            return;
        }
        if (isEmailTab(view)) {
            if (id != -1 && view.isSelected()) {

            } else {
                email = true;
            }
//            showPeopleEmailMessages();
//            return;
        }
        if (isFilesTab(view)) {
            if (id != -1 && view.isSelected()) {

            } else {
                files = true;
            }
//            showFilesMessages();
//            return;
        }
        if (isFlaggedTab(view)) {
            if (id != -1 && view.isSelected()) {

            } else {
                flagged = true;
            }
//            showMsgsGrpClutterFragment();
//            return;
        }
        if (isUnreadTab(view)) {
            if (id != -1 && view.isSelected()) {

            } else {
                unread = true;
            }
//            showMsgsGrpClutterFragment();
//            return;
        }
        people = isPeopleTab();
        clutter = isInboxTab();
        chat = isChatSelected();
        group = isGroupSelected();
        email = isEmailSelected();
        files = isFilesSelected();
        flagged = isFlaggedSelected();
        unread = isUnreadSelected();
        onTabSelected(people, clutter, chat, group, email, files, flagged, unread);
/*
        if (view instanceof ToggleButton) {
            ToggleButton sw = ((ToggleButton) view);
            if (sw.isCheckedByPos()) {
                Drawable drawable = getDrawable(R.drawable.filters_selected);
                drawable.setBounds(0, 0, 20, 20);
                sw.setCompoundDrawables(null, null, drawable, null);
                showTbarMainboxPeopleFilterView();
                showMsgsGrpPeopleChatsFragment();
            } else {
                Drawable drawable = getDrawable(R.drawable.filters_closed);
                drawable.setBounds(0, 0, 20, 20);
                sw.setCompoundDrawables(null, null, drawable, null);
                hideTbarMainboxPeopleFilterView();
                showMsgsGrpPeopleFragment();
            }
            return;
        }
*/
/*
        if (view instanceof CheckBox) {
            int i = 0;
            try {
//                i = Integer.parseInt(tvFilter.getText().toString());
                i = Integer.parseInt(toggleButton.getText().toString());
            } catch (Throwable ignored) {

            }
            CheckBox cb = ((CheckBox) view);
            if (R.id.cb_chat == cb.getId()) {
                if (cb.isCheckedByPos()) {
                    i++;
                } else {
                    i--;
                }

            } else if (R.id.cb_groups == cb.getId()) {
                if (cb.isCheckedByPos()) {
                    i++;
                } else {
                    i--;

                }

            } else if (R.id.cb_mail == cb.getId()) {
                if (cb.isCheckedByPos()) {
                    i++;
                } else {
                    i--;

                }

            }
            if (i <= 0 || i > 3) {
                i = 3;
            }
            String count = String.valueOf(i);
            setOverflowFilterCount(count);
//            tvFilter.setText(count);
        }
*/
/*
        if (view instanceof Switch) {
            Switch sw = ((Switch) view);
            if (sw.isCheckedByPos()) {
                showWorkContactsFragment();
            } else {
                showAllContactsFragment();
            }
            return;
        }
*/

    }

/*
    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (null == tab) return;
    }
*/

    public void onTabSelected(boolean people, boolean clutter, boolean chat, boolean group,
                              boolean email, boolean files, boolean flagged, boolean unread) {
        if (people) {
            if (chat && group && email && files) {
                showMsgsGrpPeopleFragment();
                return;
            }
            if (chat && group && email) {
                showPeopleMessagesMinusFiles();
                return;
            }
            if (chat && group) {
                showMsgsGrpPeopleChatsFragment();
                return;
            }
            if (chat) {
                showMsgsGrpPeopleChatsFragment();
                return;
            }
            if (group && email && files) {
                showPeopleMessagesMinusChat();
                return;
            }
        }
        if (clutter) {
            if (flagged && unread) {
                showMsgsGrpClutterFragment();
            }
        }
    }

/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, IntellibitzActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/

    private void resetOverflowTabs() {
/*
        for (int i = 2; i < tabLayoutFilter.getTabCount(); i++) {
            tabLayoutFilter.removeTabAt(i);
        }
*/
    }

    private void onMenuChatMessages(MenuItem item) {
/*
        if (msgsGrpPeopleChatsFragment != null) {
//            msgsGrpPeopleChatsFragment.getUnreadCount();
        }
*/
        toggleOverflowMenu(item);
        showMsgsGrpPeopleChatsFragment();
    }

    private void onMenuGroupMessages(MenuItem item) {
        toggleOverflowMenu(item);
        showGroupMessages();
    }

    private void onMenuEmailMessages(MenuItem item) {
        toggleOverflowMenu(item);
        showPeopleEmailMessages();
    }

    private void onMenuFilesMessages(MenuItem item) {
        toggleOverflowMenu(item);
        showFilesMessages();
    }

    private View getTabView(int id) {
        return null;
    }

    private void toggleOverflowMenu(MenuItem item) {
/*
        TabLayout.Tab tab = tabHashMap.get(item.getItemId());
        int count = tabOverflowLayoutFilter.getTabCount();
        boolean found = false;
        for (int i = 2; i < count; i++) {
            TabLayout.Tab at = tabOverflowLayoutFilter.getTabAt(i);
            if (at != null && at.getTag() != null && at.getTag().equals(tab.getTag())) {
                found = true;
                break;
            }
        }
        if (!found) {
            tabOverflowLayoutFilter.addTab(newOverflowTabFromMainboxMenuItem(item), true);
        }
*/
    }

    private void showPeopleMessagesMinusChat() {
/*
        Bundle arguments = msgsGrpPeopleFragment.getArguments();
        if (null == arguments){
            arguments = new Bundle();
            msgsGrpPeopleFragment.setArguments(arguments);
        }
        arguments.putBoolean("isMinusChat", true);
*/
        showMainToolbar();
//        if (null == msgsGrpPeopleFragment)
        MsgsGrpPeopleFragment msgsGrpPeopleFragment = newMsgsGrpPeopleFragment();
        replaceContentFragment(msgsGrpPeopleFragment, MsgsGrpPeopleFragment.TAG);
        msgsGrpPeopleFragment.minusChat();
        msgsGrpPeopleFragment.restartFilterLoaders();
    }

    private void showPeopleMessagesMinusFiles() {
        showMainToolbar();
//        if (null == msgsGrpPeopleFragment)
        MsgsGrpPeopleFragment msgsGrpPeopleFragment = newMsgsGrpPeopleFragment();
        replaceContentFragment(msgsGrpPeopleFragment, MsgsGrpPeopleFragment.TAG);
        msgsGrpPeopleFragment.minusChat();
        msgsGrpPeopleFragment.restartFilterLoaders();
    }

    private void showGroupMessages() {
        showMsgsGrpPeopleChatsFragment();
    }

    private void showPeopleEmailMessages() {
        showMsgsGrpPeopleChatsFragment();
    }

    private void showFilesMessages() {
        showMsgsGrpPeopleChatsFragment();
    }

    private void showTabs() {
/*
        tabLayoutFilter.setVisibility(View.VISIBLE);
        tabOverflowLayoutFilter.setVisibility(View.VISIBLE);
*/
    }

    private void hideTabs() {
/*
        tabLayoutFilter.setVisibility(View.GONE);
        tabOverflowLayoutFilter.setVisibility(View.GONE);
*/
    }

    private void hideFilterToolbars() {
        hideOverFlowFilter();
//        filterToolbar.setVisibility(View.GONE);
    }

    private void removeChatMessages() {
//        removeFragment(msgsGrpPeopleChatsFragment);
    }

    private void removeClutterBoxMessages() {
        hideOverFlowFilter();
//        removeFragment(msgsGrpClutterFragment);
    }

    private void removePeopleMessages() {
        hideOverFlowFilter();
//        removeFragment(msgsGrpPeopleFragment);
    }

    private void setupSnackBar() {
        snackbar = Snackbar.make(
                getRootView(), "Please Add Account to see Emails", Snackbar.LENGTH_LONG);
        snackbar.setAction("ADD EMAIL", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmailListActivity();
            }
        });
    }

/*
    private void setupFAB(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewBottomDialogFragment();
            }
        });
    }
*/

    public void showNewMessageDialog() {
        // Create an instance of the dialog fragment and show it
//        // TODO: 25-04-2016
//        move this to the respective item fragment
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
                getSupportFragmentManager(), "NewMessageDialog");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof NewEmailDialogFragment) {
            performNewEmailOk((NewEmailDialogFragment) dialog);
        } else if (dialog instanceof EditGroupDialogFragment) {
            performEditGroupOk((EditGroupDialogFragment) dialog);
        }
    }

    private void execUpdateGroupTask(ContactItem contactItem,
                                     MessageItem messageItem) {
        updateGroupTask = new UpdateGroupTask(contactItem, messageItem,
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_UPDATE_GROUP);
        updateGroupTask.setUpdateGroupTaskListener(this);
        updateGroupTask.execute();
    }

    private void execInviteUsersTask(String inviteEmails, String companyId, ContactItem user) {
        InviteUsersTask inviteUsersTask = new InviteUsersTask(inviteEmails, companyId,
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                user, MainApplicationSingleton.AUTH_COMPANY_INVITE_USERS, this);
        inviteUsersTask.setRequestTimeoutMillis(30000);
        inviteUsersTask.setInviteUsersTaskListener(this);
        inviteUsersTask.execute();
    }

    public void performEditGroupOk(EditGroupDialogFragment editGroupDialogFragment) {
        Log.d(TAG, "performEditGroupOk");
        MessageItem messageItem = editGroupDialogFragment.getMessageItem();
        if (null == messageItem) return;
        ContactItem contactItem = messageItem.getContactItem();
        if (null == contactItem) return;
        String name = editGroupDialogFragment.getSubject();
        if (TextUtils.isEmpty(name)) return;
        messageItem.setName(name);
        contactItem.setName(name);
        execUpdateGroupTask(contactItem, messageItem);

    }

    public void performNewEmailOk(NewEmailDialogFragment newEmailDialogFragment) {
        // User touched the dialog's positive button
//        // TODO: 16-03-2016
//        user = dialog.getArguments().getParcelable(ContactItem.TAG);
        notifyUserBaseItemListeners();

//        user must be signed into atleast one email account

//        NewEmailDialogFragment newEmailDialogFragment = newEmailDialogFragment1;
        MessageItem messageItem = new MessageItem();
//        new message.. sets id to a constant.. global new message id
        messageItem.setDataId(MessageItem.TAG);
        messageItem.setBaseType("THREAD");
        messageItem.setDocType("THREAD");
        messageItem.setDataRev("1");
        messageItem.setFrom(user.getName());
        messageItem.setSubject(newEmailDialogFragment.getSubject());
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
            intent.setAction(MainApplicationSingleton.BROADCAST_NEW_CHAT_DIALOG_OK);
        } else {
            intent.setAction(MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK);
        }
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    public void setMessageItem(MessageItem messageItem) {
        this.messageItem = messageItem;
    }

    // Returns total number of pages
    public int getCount() {
        return 3;
    }

/*
    private void newMsgsGrpPeopleFragment() {
        MessageItem messageItem = null;
        Intent intent = getIntent();
        if (intent != null) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG);
        }
        msgsGrpPeopleFragment =
                MsgsGrpPeopleFragment.newInstance(messageItem, user, this);
    }
*/

/*
    private void newMsgsGrpClutterFragment() {
        MessageItem messageItem = null;
        Intent intent = getIntent();
        if (intent != null) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG);
        }
        msgsGrpClutterFragment =
                MsgsGrpClutterFragment.newInstance(messageItem, user, this);
    }
*/

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (isInboxTab()) {
            MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
            msgsGrpClutterFragment.onQueryTextSubmit(query);
        } else if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onQueryTextSubmit(query);
        }
        hideTabs();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (isInboxTab()) {
            MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
            msgsGrpClutterFragment.onQueryTextChange(newText);
        } else if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onQueryTextChange(newText);
        }
        hideTabs();
        return true;
    }

    @Override
    public boolean onClose() {
        if (isInboxTab()) {
            MsgsGrpClutterFragment msgsGrpClutterFragment = (MsgsGrpClutterFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpClutterFragment.TAG);
            msgsGrpClutterFragment.onClose();
            showTabs();
            return true;
        } else if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onClose();
            showTabs();
            return true;
        }
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onClose();
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onClose();
*/
        } else if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onClose();
        }
        showContactsTabs();
        return true;
    }

    public boolean onDetailQueryTextSubmit(String query) {
        if (isInboxTab()) {
//            msgsGrpClutterFragment.onQueryTextSubmit(query);
        } else if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onDetailQueryTextSubmit(query);
            msgsGrpPeopleFragment.onQueryTextSubmit(query);
        }
        return true;
    }

    public boolean onDetailQueryTextChange(String newText) {
        if (isInboxTab()) {
//            msgsGrpClutterFragment.onQueryTextChange(newText);
        } else if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onDetailQueryTextChange(newText);
//            chatDetailFragment.onQueryTextChange(newText);
        }
        return true;
    }

    public boolean onDetailClose() {
        if (isInboxTab()) {
//            msgsGrpClutterFragment.onClose();
        } else if (isPeopleTab()) {
            MsgsGrpPeopleFragment msgsGrpPeopleFragment = (MsgsGrpPeopleFragment)
                    getSupportFragmentManager().findFragmentByTag(MsgsGrpPeopleFragment.TAG);
            msgsGrpPeopleFragment.onDetailClose();
//            chatDetailFragment.onClose();
        }
        return true;
    }

/*
    @Override
    public void onBaseItemStateChanged(String key, BaseItem value) {
        if (ContactItem.TAG.equals(key)) {
            setUser((ContactItem) value);
        }
    }
*/

/*
    @Override
    public void onChatTopicsLoaded(int count) {

    }
*/

    @Override
    public void onPeopleTopicClicked(MessageItem messageItem, ContactItem user) {
        final String type = messageItem.getType();
        if ("CHAT".equalsIgnoreCase(type)) {
            if ("USER".equalsIgnoreCase(messageItem.getToType())) {
                onPeopleMessageClicked(messageItem, user);
            } else {
//            chat group always lands in chat detail view
                onChatGroupClicked(messageItem, user);
            }
        } else if ("EMAIL".equalsIgnoreCase(type)) {
//            checks if the device contact id.. has a chat id already
//            if chat id available, shows people details view
//            if chat id not available, shows clutter mails view
            long did = messageItem.getDeviceContactId();
            if (did > 0) {
                Cursor cursor = getContentResolver().query(
                        IntellibitzContactContentProvider.CONTENT_URI, null,
                        ContactItemColumns.KEY_IS_CLOUD + " = 1  OR  " +
//                                ContactItemColumns.KEY_IS_EMAIL + " <> 1  AND  " +
//                                ContactItemColumns.KEY_IS_GROUP + " <> 1 ) OR " +
                                ContactItemColumns.KEY_ISWORK + " = 1  AND " +
                                ContactItemColumns.KEY_DEVICE_CONTACTID + " = ? ",
                        new String[]{String.valueOf(did)}, null);
                if (cursor != null && cursor.getCount() > 0) {
                    String id = cursor.getString(cursor.getColumnIndex(
                            ContactItemColumns.KEY_INTELLIBITZ_ID));
                    cursor.close();
                    if (TextUtils.isEmpty(id)) {
                        messageItem.setIntellibitzId(messageItem.getDocSenderEmail());
                        onClutterTopicClicked(messageItem, user);
                    } else {
                        cursor = getContentResolver().query(
                                MessagesChatContentProvider.CONTENT_URI, null,
                                MessageItemColumns.KEY_CHAT_ID + " = ? ",
                                new String[]{id}, null
                        );
                        if (cursor != null && cursor.getCount() > 0) {
                            MessageItem chatMessage =
                                    MessageChatContentProvider.fillsMessageItemFromCursor(cursor);
                            cursor.close();
                            if (null == chatMessage) {
//                            // TODO: 14-07-2016
//                            the chat id is available, but no chat has been initiated
//                            shows clutter view ..
//  to change to the people view (a brand new chat with existing emails)
                                messageItem.setIntellibitzId(messageItem.getDocSenderEmail());
                                onClutterTopicClicked(messageItem, user);
                            } else {
                                onPeopleMessageClicked(chatMessage, user);
                            }
                        }
                        onClutterTopicClicked(messageItem, user);
                        if (cursor != null) cursor.close();
                    }
                } else {
                    onClutterTopicClicked(messageItem, user);
                    if (cursor != null) cursor.close();
                }
            } else {
                messageItem.setIntellibitzId(messageItem.getDocSenderEmail());
                onClutterTopicClicked(messageItem, user);
            }
//            onPeopleMessageClicked(messageItem, user);
        } else {
//            people message, is a group of chat + email threads
            onPeopleMessageClicked(messageItem, user);
//            onChatTopicClicked(messageItem, user);
        }
    }

    @Override
    public void onClutterTopicClicked(MessageItem messageItem, ContactItem user) {
//        this can be invoked from 2 instances
//        from people item, or from email item
/*
        if (msgsGrpClutterFragment != null) {
            View view = msgsGrpClutterFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        if (msgsGrpPeopleFragment != null) {
            View view = msgsGrpPeopleFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        hideFilterToolbars();
*/
//        showClutterEmailsFragment(messageItem, user);
        if (MessageItem.TAG.equalsIgnoreCase(messageItem.getDataId())) {
//        if ("1".equalsIgnoreCase(messageItem.getDataRev())){
            startClutterEmailActivity(messageItem, user);
        } else {
            startClutterEmailsActivity(messageItem, user);
        }
    }

    public void onPeopleMessageClicked(MessageItem messageItem, ContactItem user) {
//        this can be invoked from 2 instances
//        from people item, or from email item
/*
        if (msgsGrpPeopleChatsFragment != null) {
            View view = msgsGrpPeopleChatsFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        if (msgsGrpPeopleFragment != null) {
            View view = msgsGrpPeopleFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

//        sets the toolbar for the detail fragment
//        chatDetailFragment.setChatHeaders(messageItem);
        hideFilterToolbars();
*/
//        showPeopleDetailFragment(messageItem, user);
        startPeopleDetailActivity(messageItem, user);
    }

    public void onChatGroupClicked(MessageItem messageItem, ContactItem user) {
/*
//        this can be invoked from 2 instances
//        from people item, or from email item
        if (msgsGrpPeopleChatsFragment != null) {
            View view = msgsGrpPeopleChatsFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        if (msgsGrpPeopleFragment != null) {
            View view = msgsGrpPeopleFragment.getView();
            if (view != null) {
//                view.setVisibility(View.GONE);
            }
        }
//        sets the toolbar for the detail fragment
//        chatDetailFragment.setChatHeaders(messageItem);
        hideFilterToolbars();
*/
//        showMessageChatGroupFragment(messageItem, user);
        startMessageChatGroupActivity(messageItem, user);
    }

/*
    @Override
    public void onEmailTopicClicked(MessageItem messageItem, ContactItem user) {
//        this can be invoked from 2 instances
//        from people item, or from email item
*/
/*
        if (msgsGrpClutterFragment != null) {
            View view = msgsGrpClutterFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        if (msgsGrpPeopleFragment != null) {
            View view = msgsGrpPeopleFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        hideFilterToolbars();
*//*

//        showClutterEmailFragment(messageItem, user);
        startClutterEmailActivity(messageItem, user);
    }
*/

/*
    @Override
    public void onChatTopicClicked(MessageItem messageItem, ContactItem user) {
//        this can be invoked from 2 instances
//        from people item, or from email item
        if (msgsGrpPeopleChatsFragment != null) {
            View view = msgsGrpPeopleChatsFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        if (msgsGrpPeopleFragment != null) {
            View view = msgsGrpPeopleFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

//        sets the toolbar for the detail fragment
//        chatDetailFragment.setChatHeaders(messageItem);
        hideFilterToolbars();
        showChatDetailFragment(messageItem, user);
    }
*/

/*
    @Override
    public void onEmailTopicsLoaded(int count) {

    }
*/

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
    public void startProfileActivity(ContactItem user) {
//        nulls shared intent (safe case)
        nullSharedIntent();
        Intent intent = new Intent(this, ProfileActivity.class);
//        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(
                intent, MainApplicationSingleton.ACTIVITY_PROILE_RQ_CODE);
//        the following fails with not attached to activity
//        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MESSAGECHATGROUP_RQ_CODE);
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
    public void startMessageChatGroupActivity(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        nullSharedIntent();
        Intent intent = new Intent(this, MessageChatGroupActivity.class);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(
                intent, MainApplicationSingleton.ACTIVITY_MESSAGECHATGROUP_RQ_CODE);
//        the following fails with not attached to activity
//        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MESSAGECHATGROUP_RQ_CODE);
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
    public void startPeopleDetailActivity(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        nullSharedIntent();
        Intent intent = new Intent(this, PeopleDetailActivity.class);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(
                intent, MainApplicationSingleton.ACTIVITY_PEOPLEDETAIL_RQ_CODE);
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
    public void startClutterEmailsActivity(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        nullSharedIntent();
        Intent intent = new Intent(this, ClutterEmailsActivity.class);
        intent.putExtra(MessageItem.EMAIL_MESSAGE, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CLUTTEREMAILS_RQ_CODE);
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
    public void startClutterEmailActivity(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        nullSharedIntent();
        Intent intent = new Intent(this, ClutterEmailActivity.class);
        intent.putExtra(MessageItem.EMAIL_MESSAGE, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(
                intent, MainApplicationSingleton.ACTIVITY_CLUTTEREMAIL_RQ_CODE);
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
    public void startScheduleActivity(ContactItem user) {
//        nulls shared intent (safe case)
        nullSharedIntent();
/*
        Intent intent = new Intent(this, SchedulesActivity.class);
//        intent.putExtra(MessageItem.EMAIL_MESSAGE, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACT_SCHEDULE_RQ_CODE);
*/
    }

/*
    public void showMessageChatGroupFragment(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        getMainActivity().nullSharedIntent();
        messageChatGroupFragment = MessageChatGroupFragment.newInstance(
                messageItem, user, this);
        showDetailToolbar();
        replaceDetailFragment(messageChatGroupFragment);
    }

    public void showMessageChatGroupFragment_(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        getMainActivity().nullSharedIntent();
        messageChatGroupFragment = MessageChatGroupFragment.newInstance(
                messageItem, user, this);
        showDetailToolbar();
        replaceDetailFragment(messageChatGroupFragment);
    }

    public void showPeopleDetailFragment(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        getMainActivity().nullSharedIntent();
        peopleDetailFragment = PeopleDetailFragment.newInstance(
                messageItem, user, this);
//                this, getMainActivity(), twoPane, messageItem, user);
        showDetailToolbar();
        replaceDetailFragment(peopleDetailFragment);
    }

    public void showChatDetailFragment(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        getMainActivity().nullSharedIntent();
        chatDetailFragment = ChatDetailFragment.newInstance(
                this, getMainActivity(), twoPane, messageItem, user);
        showDetailToolbar();
        replaceDetailFragment(chatDetailFragment);
    }

    public void showClutterEmailFragment(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        getMainActivity().nullSharedIntent();
//        sets the toolbar for the detail fragment
        clutterEmailFragment = ClutterEmailFragment.newInstance(
                messageItem, user, this);
//        clutterEmailFragment.setMessageHeaders(messageItem);
        showDetailToolbar();
        replaceDetailFragment(clutterEmailFragment);
    }

    public void showClutterEmailsFragment(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
        getMainActivity().nullSharedIntent();
//        sets the toolbar for the detail fragment
        clutterEmailsFragment = ClutterEmailsFragment.newInstance(
                messageItem, user, this);
//        clutterEmailFragment.setMessageHeaders(messageItem);
        showDetailToolbar();
        replaceDetailFragment(clutterEmailsFragment);
    }
*/

    @Override
    public void onPeopleTopicsLoaded(int count) {
        if (0 == count) {
            if (snackbar != null)
                snackbar.show();
        } else {
            if (snackbar != null)
                snackbar.dismiss();
        }
    }

    @Override
    public void onClutterTopicsLoaded(int count) {
        if (0 == count) {
            if (snackbar != null)
                snackbar.show();
        } else {
            if (snackbar != null)
                snackbar.dismiss();
        }
    }

/*
    @Override
    public void onChatMessageTyping(String text) {
        setDetailToolbarSubTitle(text);
//        toolbar.setSubtitle(text);
//        toolbar.setSubtitleTextColor(Color.GREEN);
    }

    @Override
    public void onChatMessageTypingStopped(String text) {
//        setTvToolbarSubTitle(text);
        setDetailToolbarSubTitle(subTitle);
//        toolbar.setSubtitle(text);
//        toolbar.setSubtitleTextColor(Color.BLACK);
    }
*/
//    end ChatMessageFragment.ChatMessageListener  implementation

    //    EmailMessageFragment.ChatMessageListener  implementation
/*
    @Override
    public void onEmailMessageHeaderChanged(MessageItem messageItem) {
//        setMessageHeaders(messageItem);
    }

    @Override
    public void onEmailMessageTyping(String text) {
        Toolbar toolbar = getToolbar();
        assert toolbar != null;
//        toolbar.setSubtitle(text);
//        toolbar.setSubtitleTextColor(Color.GREEN);
    }

    @Override
    public void onEmailMessageTypingStopped(String text) {
        Toolbar toolbar = getToolbar();
        assert toolbar != null;
//        toolbar.setSubtitle(text);
//        toolbar.setSubtitleTextColor(Color.WHITE);
    }
*/
//    end EmailMessageFragment.ChatMessageListener  implementation

    //    ChatEmailMessageFragment.PeopleDetailListener  implementation
    @Override
    public void onPeopleTyping(String text) {
        Toolbar toolbar = getToolbar();
        assert toolbar != null;
        toolbar.setSubtitle(text);
        toolbar.setSubtitleTextColor(Color.GREEN);
    }

    @Override
    public void onPeopleTypingStopped(String text) {
        Toolbar toolbar = getToolbar();
        assert toolbar != null;
        toolbar.setSubtitle(text);
        toolbar.setSubtitleTextColor(Color.WHITE);
    }

//    end EmailMessageFragment.ChatMessageListener  implementation

/*
    private void onChatTopicClicked_(MessageItem messageItem, ContactItem user) {
        if (twoPane) {
//        // TODO: 13-03-2016
//        loads the complete message thread, with all messages and all attachments
            Uri uri = MessagesContentProvider.JOIN_CONTENT_URI;
//        uri = Uri.withAppendedPath(uri, Uri.encode("join"));
            uri = Uri.withAppendedPath(uri, Uri.encode(messageItem.getDataId()));
            String selection = "mt." +
                    MessageContentProvider.MessageItemColumns.KEY_DATA_ID + " = ? ";
            String[] selectionArgs = new String[]{messageItem.getDataId()};
            String sortOrder = "mt." +
                    MessageContentProvider.MessageItemColumns.KEY_TIMESTAMP + " ASC";
            Cursor cursor = getContentResolver().query(
                    uri, null, selection, selectionArgs, sortOrder);
            try {
                MessagesContentProvider.fillMessageItemFromAllJoinCursor(
                        messageItem, cursor);
                if (cursor != null) cursor.close();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }
            setUser(user);
            setMessageItem(messageItem);
//        sets the toolbar for the detail fragment
            chatDetailFragment = ChatDetailFragment.newInstance(
                    this, mainActivity, twoPane, messageItem, user);
//            setChatHeaders(messageItem);
            Bundle arguments = new Bundle();
            arguments.putParcelable(ContactItem.TAG, user);
            arguments.putParcelable(MessageItem.TAG, messageItem);
            chatDetailFragment.setArguments(arguments);
            chatDetailFragment.refreshChatMessageLoader(arguments);
            showDetailToolbar();
            replaceDetailFragment(chatDetailFragment);
        }
    }
*/


/*
    public void onEmailTopicClicked_(MessageItem messageItem, ContactItem user) {
        if (twoPane) {
//        // TODO: 13-03-2016
//        loads the complete message thread, with all messages and all attachments
            Uri uri = MessagesContentProvider.JOIN_CONTENT_URI;
//        uri = Uri.withAppendedPath(uri, Uri.encode("join"));
            uri = Uri.withAppendedPath(uri, Uri.encode(messageItem.getDataId()));
            String selection = "mt." +
                    MessageContentProvider.MessageItemColumns.KEY_DATA_ID + " = ? ";
            String[] selectionArgs = new String[]{messageItem.getDataId()};
            String sortOrder = "mt." +
                    MessageContentProvider.MessageItemColumns.KEY_TIMESTAMP + " ASC";
            Cursor cursor = getContentResolver().query(
                    uri, null, selection, selectionArgs, sortOrder);
            try {
                MessagesContentProvider.fillMessageItemFromAllJoinCursor(
                        messageItem, cursor);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }

            setUser(user);
            setMessageItem(messageItem);
            EmailDetailFragment emailDetailFragment = (EmailDetailFragment)
                    EmailDetailFragment.newInstance(this, mainActivity, twoPane, messageItem, user);
            Bundle arguments = new Bundle();
            arguments.putParcelable(ContactItem.TAG, user);
            arguments.putParcelable(MessageItem.TAG, messageItem);
            emailDetailFragment.setArguments(arguments);
            emailDetailFragment.refreshEmailMessageLoader(arguments);
        }
    }
*/

/*
    private void onMessageTopicClicked_(MessageItem messageItem, ContactItem user) {
        if (twoPane) {
//        // TODO: 13-03-2016
//        loads the complete message thread, with all messages and all attachments
            Uri uri = MessagesContentProvider.JOIN_CONTENT_URI;
//        uri = Uri.withAppendedPath(uri, Uri.encode("join"));
            uri = Uri.withAppendedPath(uri, Uri.encode(messageItem.getDataId()));
            String selection = "mt." +
                    MessageContentProvider.MessageItemColumns.KEY_DATA_ID + " = ? ";
            String[] selectionArgs = new String[]{messageItem.getDataId()};
            String sortOrder = "mt." +
                    MessageContentProvider.MessageItemColumns.KEY_TIMESTAMP + " ASC";
            Cursor cursor = getContentResolver().query(
                    uri, null, selection, selectionArgs, sortOrder);
            try {
                MessagesContentProvider.fillMessageItemFromAllJoinCursor(
                        messageItem, cursor);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }
            setUser(user);
            setMessageItem(messageItem);
            PeopleDetailFragment messageDetailFragment = PeopleDetailFragment.newInstance(
                    messageItem, user, this);
            Bundle arguments = new Bundle();
            arguments.putParcelable(ContactItem.TAG, user);
            arguments.putParcelable(MessageItem.TAG, messageItem);
            messageDetailFragment.setArguments(arguments);
            messageDetailFragment.refreshChatMessageLoader(arguments);
        }
    }
*/

    @Override
    public void onPostUpdateGroupExecute(JSONObject response, String name, File file,
                                         ContactItem contactItem,
                                         MessageItem messageItem) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostUpdateGroupExecuteFail(response, name, file, contactItem, messageItem);
        } else {
            ContentValues values = new ContentValues();
            if (name != null) {
                values.put(MessageItemColumns.KEY_NAME, name);
                if (file != null) {
                    values.put(MessageItemColumns.KEY_PIC,
                            file.getAbsolutePath());
                }
                String id = messageItem.getDataId();
                if (!TextUtils.isEmpty(id))
                    getContentResolver().update(
                            MessagesChatGroupContentProvider.CONTENT_URI, values,
                            MessageItemColumns.KEY_DATA_ID + " = ? ",
                            new String[]{id});
                id = contactItem.getDataId();
                if (!TextUtils.isEmpty(id))
                    getContentResolver().update(
                            Uri.withAppendedPath(MsgChatGrpContactsContentProvider.CONTENT_URI, "update"),
                            values,
                            ContactItemColumns.KEY_DATA_ID + " = ? ",
                            new String[]{id});
            }
            Log.d(TAG, "onPostUpdateGroupExecute: Success -" + name);
        }
    }

    @Override
    public void onPostUpdateGroupExecuteFail(JSONObject response, String name, File file,
                                             ContactItem contactItem,
                                             MessageItem messageItem) {
        Log.e(TAG, "onPostUpdateGroupExecuteFail: " + response);
    }

    @Override
    public void setUpdateGroupTaskToNull() {
        updateGroupTask = null;
    }

    //    ChatMessageFragment.ChatMessageListener  implementation
/*
    @Override
    public void onChatMessageHeaderChanged(MessageItem messageItem) {
//        chatDetailFragment.setChatHeaders(messageItem);
    }
*/

    @Override
    public void onPeopleHeaderChanged(MessageItem messageItem) {
//        chatDetailFragment.setChatHeaders(messageItem);
//        msgsGrpPeopleFragment.set
    }

    public void setTwoPane(boolean mTwoPane) {
        this.twoPane = mTwoPane;
    }

    private DeviceContactsFragment newDeviceContactItemFragment() {
        return DeviceContactsFragment.newInstance(user, this);
    }

    private IntellibitzContactsFragment newIntellibitzContactItemFragment() {
        return IntellibitzContactsFragment.newInstance(user, this);
    }

    private void newMsgChatGrpContactsFragment() {
        groupContactsItemFragment = MsgChatGrpContactsFragment.newInstance(user, this);
    }

    private void newBroadcastContactsFragment() {
//        broadcastContactsFragment = BroadcastContactsFragment.newInstance(user, this);
    }

    public void setupAndPrepareContactChildFragments() {
        setupContactsToolbar();
//        IntellibitzUserFragment.selectTabAtByTag(tabLayoutFilter, R.id.mic_all);
    }

    public void setupFAB(View view) {
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                // TODO: 25-02-2016
//                sets the action for the floating bar menu
//                refreshes the contacts again here.. check android contacts provider and sync
//                Intent intent = new Intent(ContactListActivity.this, ContactActivity.class);
//                hack!! now just show the contact picker.. to be replaced with contact sync adaptor
/*
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivity(intent);
*/
//                contactsMenu.setIntent(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI));
/*
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
*/
//                // TODO: 25-02-2016
//                hack!! not action based, must be aync loaded when contacts change
//        populates the contacts from device
//                populateContactsFromDevice();
/*
                ContactService.asyncUpdateContacts(
                        user, getApplicationContext());
*/
            }
        });
    }

/*
    private void setupContactsFilterToolbar() {
        filterToolbar = clearFilters();
        Menu menu;
        filterToolbar.inflateMenu(R.menu.menu_contacts);
        menu = filterToolbar.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            TabLayout.Tab tab = newTabFromContactsMenuItem(item);
//            tabLayoutFilter.addTab(tab, false);
        }
//        tabLayoutFilter.addOnTabSelectedListener(this);
        filterToolbar.setVisibility(View.GONE);
    }
*/

    @NonNull
    private Toolbar clearFilters() {
        Toolbar filterToolbar = getFilterToolbar();
        assert filterToolbar != null;
        Menu menu = filterToolbar.getMenu();
        menu.clear();
//        tabLayoutFilter = getTabLayoutFilter();
//        tabLayoutFilter.removeAllTabs();
//        tabLayoutFilter.removeOnTabSelectedListener(this);
//        tabLayoutFilter.clearOnTabSelectedListeners();
        return filterToolbar;
    }

    @NonNull
    private TabLayout.Tab newTabFromContactsMenuItem(MenuItem item) {
/*
        TabLayout.Tab tab = tabLayoutFilter.newTab();
        tab.setTag(item.getItemId());
        tab.setText(item.getTitle());
        tab.setCustomView(R.layout.tab_item);
        View view = tab.getCustomView();
        TextView text = (TextView) view.findViewById(R.id.tv_tab);
        text.setText(item.getTitle());
*/
/*
        Drawable drawable = item.getIcon();
        drawable.setBounds(new Rect(0, 0, 20, 20));
        text.setCompoundDrawablesRelative(null, drawable, null, null);
*/
//        return tab;
        return null;
    }

    private Toolbar setupContactsDetailToolbar() {
/*
        detailToolbar = getDetailToolbar();
        return detailToolbar;
*/
        return null;
    }

    private void setupAllContactsFilter() {
        setupContactsOverflowFilter(R.menu.contacts_all_overflow);
        showOverFlowFilter();
    }

    private void setupContactsOverflowFilter(int menuResource) {
/*
        Menu menu;
        int size;
        tabOverflowLayoutFilter = getTabOverflowLayoutFilter();
        tabOverflowLayoutFilter.removeAllTabs();
        tabOverflowLayoutFilter.removeOnTabSelectedListener(this);
        tabOverflowLayoutFilter.clearOnTabSelectedListeners();
        filterToolbar.getMenu().clear();
        filterToolbar.inflateMenu(menuResource);
        menu = filterToolbar.getMenu();
        size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            TabLayout.Tab tab = newOverflowTabFromContactsMenuItem(item);
            tabOverflowLayoutFilter.addTab(tab, false);
        }
        filterToolbar.setOnMenuItemClickListener(this);
        tabOverflowLayoutFilter.addOnTabSelectedListener(this);
*/
    }

    private TabLayout.Tab newOverflowTabFromContactsMenuItem(MenuItem item) {
/*
        TabLayout.Tab tab = tabOverflowLayoutFilter.newTab();
        tab.setTag(item.getItemId());
        tab.setCustomView(R.layout.tab_switch_overflow);
        View view = tab.getCustomView();

        if (view != null) {
            setupTbarContactsAllTidyFilter(item.getItemId(), view);
        }
        return tab;
*/
        return null;
    }

    private void performNavigationClose() {
/*
        int position = tabLayoutFilter.getSelectedTabPosition();
        TabLayout.Tab tab = tabLayoutFilter.getTabAt(position);
        onTabSelected(tab);
*/
    }

    public void onTrimMemory_(int level) {
        Log.d(TAG, "onTrimMemory: " + level);
        if (isAllContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onTrimMemory(level);
            groupContactsItemFragment.onTrimMemory(level);
            return;
        }
        if (isWorkContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onTrimMemory(level);
            groupContactsItemFragment.onTrimMemory(level);
            return;

        }
/*
        if (isGroupsTab()) {
            deviceContactsFragment.onTrimMemory(level);
            intellibitzContactsFragment.onTrimMemory(level);
            return;

        }
*/
    }

/*
    public int getSelectedTabPosition_() {
        return IntellibitzActivityFragment.getSelectedTabPosition(tabLayoutFilter);
    }

    public TabLayout.Tab getSelectedContactsTab() {
        return IntellibitzActivityFragment.getSelectedTab(tabLayoutFilter);
    }
*/

/*
    private boolean isGroupsTab() {
        TabLayout.Tab tab = getSelectedMainboxTab();
        return null != tab && R.id.mic_groups == (int) tab.getTag();
    }
*/

    public void onBackPressed_() {
//        int position = tabLayoutFilter.getSelectedTabPosition();
//        TabLayout.Tab tab = tabLayoutFilter.getTabAt(position);
//        onTabSelected(tab);
/*
        if (tab != null && R.id.mic_groups == (int) tab.getTag()) {
//            gives item fragment a chance to handle hard back button press
//                checks if detail fragments, have spawned child
//            gives item fragment a chance to handle hard back button press
            if (groupContactsItemFragment == null) {
                getMainActivity().hideDetailFilters();
                performNavigationClose();
                return;
            } else {
                if (groupContactsItemFragment.onBackPressed()) {
//                    getMainActivity().hideDetailFilters();
//                    getMainActivity().popFragment();
                    performNavigationClose();
                    return;
                } else {
                }

            }

*/
/*
//                checks if detail fragments, have spawned child
                if (intellibitzContactsFragment != null && !intellibitzContactsFragment.onBackPressed()) {

                }
*//*

//                checks if detail fragments, have spawned child
        }
        if (VIEW_MODE.ITEM == viewMode) {
            getMainActivity().hideDetailFilters();
            performNavigationClose();
            return;
        } else {
            viewMode = VIEW_MODE.ITEM;
            getMainActivity().hideDetailFilters();
//            getMainActivity().popFragment();
        }
*/
        return;
    }

    public void onOkPressed_(Intent intent) {
//        lets the individual fragments handled the back pressed, by removing themselves
//        // TODO: 27-05-2016
        if (null == intent) {
            onBackPressed();
//        the detail fragment is done.. hard back button in phone keyboard pressed by user
//        so close the filters if any.. the fragment cannot react to the back button press in phone
            hideDetailFilters();
        } else {
            onContactsChildOkPressed(intent);
        }
    }

/*
    public void onBackPressed(Intent intent) {
//        lets the individual fragments handled the back pressed, by removing themselves
//        // TODO: 27-05-2016
        if (null == intent) {
            onBackPressed();
//        the detail fragment is done.. hard back button in phone keyboard pressed by user
//        so close the filters if any.. the fragment cannot react to the back button press in phone
            getMainActivity().hideDetailFilters();
        } else {
            onChildBackPressed(intent);
        }
    }
*/

/*
    public void onChildBackPressed(Intent intent) {
        int position = tabLayoutFilter.getSelectedTabPosition();
        TabLayout.Tab tab = tabLayoutFilter.getTabAt(position);

        if (tab != null && R.id.mic_all == (int) tab.getTag()) {
            deviceContactsFragment.onBackPressed(intent);
            return;
        }
        if (tab != null && R.id.mic_fav == (int) tab.getTag()) {
            intellibitzContactsFragment.onBackPressed(intent);
            return;
        }
        if (tab != null && R.id.mic_groups == (int) tab.getTag()) {
            groupContactsItemFragment.onBackPressed(intent);
            return;
        }

    }
*/

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
//    @Override
    public void onActivityResult_(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
/*
        if (isGroupsTab()) {
            groupContactsItemFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
*/
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    public void onContactsChildOkPressed(Intent intent) {
/*
        int position = tabLayoutFilter.getSelectedTabPosition();
        TabLayout.Tab tab = tabLayoutFilter.getTabAt(position);

        if (tab != null && R.id.mic_all == (int) tab.getTag()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onOkPressed(intent);
            return;
        }
        if (tab != null && R.id.mic_work == (int) tab.getTag()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onOkPressed(intent);
            return;
        }
*/
/*
        if (tab != null && R.id.mic_groups == (int) tab.getTag()) {
            groupContactsItemFragment.onOkPressed(intent);
            return;
        }
*/

    }

    public void onContactsNewMenuClicked() {
/*
        int position = tabLayoutFilter.getSelectedTabPosition();
        TabLayout.Tab tab = tabLayoutFilter.getTabAt(position);

        if (tab != null && R.id.mic_all == (int) tab.getTag()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onNewMenuClicked();
            return;
        }
        if (tab != null && R.id.mic_work == (int) tab.getTag()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onNewMenuClicked();
            return;
        }
*/
/*
        if (tab != null && R.id.mic_groups == (int) tab.getTag()) {
            groupContactsItemFragment.onContactsNewMenuClicked();
            return;
        }
*/
/*
        if (tab != null && R.id.mic_broadcasts == (int) tab.getTag()) {
            broadcastContactsFragment.onContactsNewMenuClicked();
            return;
        }
*/

    }

    private void showContactsTabs() {
//        tabLayoutFilter.setVisibility(View.VISIBLE);
    }

    private void hideContactsTabs() {
//        tabLayoutFilter.setVisibility(View.GONE);
    }

    //    @Override
    public boolean onQueryTextSubmit_(String query) {
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onQueryTextSubmit(query);
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onQueryTextSubmit(query);
*/
        } else if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onQueryTextSubmit(query);
        }
        hideContactsTabs();
        return true;
    }

    //    @Override
    public boolean onQueryTextChange_(String newText) {
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onQueryTextChange(newText);
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onQueryTextChange(newText);
*/
        } else if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onQueryTextChange(newText);
        }
        hideContactsTabs();
        return true;
    }

    //    @Override
    public boolean onClose_() {
        if (isAllContactsTab()) {
            DeviceContactsFragment deviceContactsFragment = (DeviceContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(DeviceContactsFragment.TAG);
            deviceContactsFragment.onClose();
/*
        } else if (isGroupsTab()) {
            groupContactsItemFragment.onClose();
*/
        } else if (isWorkContactsTab()) {
            IntellibitzContactsFragment intellibitzContactsFragment = (IntellibitzContactsFragment)
                    getSupportFragmentManager().findFragmentByTag(IntellibitzContactsFragment.TAG);
            intellibitzContactsFragment.onClose();
        }
        showContactsTabs();
        return true;
    }

    //    @Override
    public void onTabUnselected_(TabLayout.Tab tab) {
        if (null == tab) return;
        if (R.id.mic_all == (int) tab.getTag()) {
            removeAllContacts();
            return;
        }
        if (R.id.mic_work == (int) tab.getTag()) {
            removeFavContacts();
            return;
        }
/*
        if (R.id.mic_groups == (int) tab.getTag()) {
            removeGroupsContacts();
            return;
        }
*/
/*
        if (R.id.mic_broadcasts == (int) tab.getTag()) {
            removeBroadcastContacts();
            return;
        }
*/
    }

    //    @Override
    public void onTabSelected_(TabLayout.Tab tab) {
        if (null == tab) return;
        if (R.id.mic_all == (int) tab.getTag()) {
            showAllContactsFragment();
            return;
        }
        if (R.id.mic_work == (int) tab.getTag()) {
            showWorkContactsFragment();
            return;
        }
        if (R.id.mi_tidy == (int) tab.getTag()) {
            showWorkContactsFragment();
            return;
        }
/*
        if (R.id.mic_groups == (int) tab.getTag()) {
            showGroupsContacts();
            return;
        }
*/
/*
        if (R.id.mic_broadcasts == (int) tab.getTag()) {
            showBroadcastContacts();
            return;
        }
*/
    }

    private void showGroupsContacts() {
        hideDetailToolbar();
        hideDetailFilters();
        showMainToolbar();
//        sets the mode to item, to handle hard press back button
//        viewMode = IntellibitzActivityFragment.VIEW_MODE.ITEM;
        if (null == groupContactsItemFragment) newMsgChatGrpContactsFragment();
        replaceContentFragment(groupContactsItemFragment, MsgChatGrpContactsFragment.TAG);
    }

    private void showBroadcastContacts() {
        hideDetailToolbar();
        hideDetailFilters();
        showMainToolbar();
//        sets the mode to item, to handle hard press back button
//        viewMode = IntellibitzActivityFragment.VIEW_MODE.ITEM;
//        if (null == broadcastContactsFragment) newBroadcastContactsFragment();
//        replaceContentFragment(broadcastContactsFragment, BroadcastContactsFragment.TAG);
    }

    private void removeGroupsContacts() {
        removeFragment(groupContactsItemFragment);
    }

    private void removeBroadcastContacts() {
//        removeFragment(broadcastContactsFragment);
    }

    private void removeFavContacts() {
//        removeFragment(intellibitzContactsFragment);
    }

    private void removeAllContacts() {
        hideOverFlowFilter();
//        removeFragment(deviceContactsFragment);
    }

    //    @Override
    public boolean onOptionsItemSelected_(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Toolbar setContactHeaders(ContactItem deviceContactItem) {
        Toolbar toolbar = setupContactsDetailToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_dialog_close_light);
            toolbar.setNavigationOnClickListener(this);
            String title = "Contacts";
            String subTitle = MainApplicationSingleton.INTELLIBITZ;
            if (deviceContactItem != null) {
                title = deviceContactItem.getFirstName();
                if (null == title || 0 == title.length()) {
                    title = "Contact " + deviceContactItem.getDeviceContactId();
                }
                subTitle = deviceContactItem.getLastName();
                if (null == subTitle || 0 == subTitle.length()) {
                    subTitle = deviceContactItem.getDisplayName();
                }
            }
            toolbar.setTitle(title);
            toolbar.setSubtitle(subTitle);
        }
        return toolbar;
    }

    @Override
    public void onDeviceContactTopicClicked(ContactItem item, ContactItem user) {

//        // TODO: 8/9/16
//        to start contact details activity
        startContactDetailActivity(item, user);
/*
        setContactHeaders(item);
        DeviceContactDetailFragment detailFragment = DeviceContactDetailFragment.newInstance(
                this, this, twoPane, item, user);
        showDetailToolbar();
        replaceDetailFragment(detailFragment);
*/
    }

    @Override
    public void onDeviceContactTopicsLoaded(int count) {

    }

    @Override
    public void onContactsTopicClicked(ContactItem item, ContactItem user) {

    }

    @Override
    public void onContactsTopicsLoaded(int count) {

    }

    @Override
    public void onIntellibitzContactTopicClicked(ContactItem item, ContactItem user) {
//        // TODO: 8/9/16
//        to start contact details activity
        startContactDetailActivity(item, user);

//        setContactHeaders(item);
/*
        IntellibitzContactDetailFragment detailFragment = IntellibitzContactDetailFragment.newInstance(
                this, this, twoPane, item, user);
        showDetailToolbar();
        replaceDetailFragment(detailFragment);
*/
    }

    @Override
    public void onIntellibitzContactTopicsLoaded(int count) {

    }

    //    @Override
    public void onClick_(View v) {
/*
        if (v instanceof Switch) {
            Switch sw = ((Switch) v);
            if (sw.isCheckedByPos()) {
                showWorkContactsFragment();
            } else {
                showAllContactsFragment();
            }
            return;
        }
        int position = tabLayoutFilter.getSelectedTabPosition();
        if (0 == position) {
            showAllContactsFragment();
        } else if (1 == position) {
            showWorkContactsFragment();
        } else if (2 == position) {
        }
*/
    }

    //    @Override
    public boolean onMenuItemClick__(MenuItem item) {
        if (R.id.mi_tidy == item.getItemId()) {
            return true;
        }
        return false;
    }

    public boolean alertReadContacts() {
        return mayRequestReadContacts(getRootView());
    }

    public boolean alertReadStorage() {
        return mayRequestReadExternalStorage(getRootView());
    }

    public boolean alertWriteStorage() {
        return mayRequestWriteExternalStorage(getRootView());
    }

    /**
     * Handler of incoming messages from service.
     */
    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    super.handleMessage(msg);
            }
        }

    }


}
