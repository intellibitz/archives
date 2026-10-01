

package intellibitz.intellidroid

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.os.RemoteException
import android.text.TextUtils
import android.text.util.Rfc822Token
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.gcm.GCMInstanceIDListenerService
import intellibitz.intellidroid.gcm.GCMTokenIntentService
import intellibitz.intellidroid.task.GetIpTask
import intellibitz.intellidroid.task.UpdateGroupTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.AddEmailActivity
import intellibitz.intellidroid.activity.AddEmailsActivity
import intellibitz.intellidroid.activity.ClutterEmailActivity
import intellibitz.intellidroid.activity.ClutterEmailsActivity
import intellibitz.intellidroid.activity.ComposeEmailActivity
import intellibitz.intellidroid.activity.ContactSelectActivity
import intellibitz.intellidroid.activity.MessageChatGroupActivity
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity
import intellibitz.intellidroid.activity.MsgsGrpDraftActivity
import intellibitz.intellidroid.activity.PeopleDetailActivity
import intellibitz.intellidroid.activity.ProfileActivity
import intellibitz.intellidroid.company.CompanyCreateActivity
import intellibitz.intellidroid.company.CompanyListActivity
import intellibitz.intellidroid.company.GetInvitesActivity
import intellibitz.intellidroid.company.InviteUsersActivity
import intellibitz.intellidroid.company.InviteUsersTask
import intellibitz.intellidroid.contact.ContactDetailActivity
import intellibitz.intellidroid.content.IntellibitzContactContentProvider
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MessageChatGroupContentProvider
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MessagesChatContentProvider
import intellibitz.intellidroid.content.MessagesChatGroupContentProvider
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.domain.MainSettingsActivity
import intellibitz.intellidroid.domain.account.EmailAccountListActivity
import intellibitz.intellidroid.domain.help.IntroScreenActivity
import intellibitz.intellidroid.fragment.AttachmentsFragment
import intellibitz.intellidroid.fragment.DeviceContactsFragment
import intellibitz.intellidroid.fragment.IntellibitzContactsFragment
import intellibitz.intellidroid.fragment.MsgChatGrpContactsFragment
import intellibitz.intellidroid.fragment.MsgsGrpClutterFragment
import intellibitz.intellidroid.fragment.MsgsGrpPeopleChatGroupsFragment
import intellibitz.intellidroid.fragment.MsgsGrpPeopleChatsFragment
import intellibitz.intellidroid.fragment.MsgsGrpPeopleEmailsFragment
import intellibitz.intellidroid.fragment.MsgsGrpPeopleFragment
import intellibitz.intellidroid.listener.ClutterListener
import intellibitz.intellidroid.listener.ClutterTopicListener
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.ContactsTopicListener
import intellibitz.intellidroid.listener.DeviceContactTopicListener
import intellibitz.intellidroid.listener.IntellibitzContactTopicListener
import intellibitz.intellidroid.listener.PeopleDetailListener
import intellibitz.intellidroid.listener.PeopleHeaderListener
import intellibitz.intellidroid.listener.PeopleListener
import intellibitz.intellidroid.listener.PeopleTopicListener
import intellibitz.intellidroid.service.ChatService
import intellibitz.intellidroid.service.ContactService
import intellibitz.intellidroid.service.EmailService
import intellibitz.intellidroid.service.RcvDocService
import intellibitz.intellidroid.widget.EditGroupDialogFragment
import intellibitz.intellidroid.widget.NewBottomDialogFragment
import intellibitz.intellidroid.widget.NewEmailDialogFragment
import intellibitz.intellidroid.activity.*
import intellibitz.intellidroid.content.*
import intellibitz.intellidroid.fragment.*
import intellibitz.intellidroid.listener.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.Set
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import intellibitz.intellidroid.util.MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE
import intellibitz.intellidroid.util.MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE

open class IntellibitzActivity : IntellibitzTwoPaneUserActivity(), NavigationView.OnNavigationItemSelectedListener, Toolbar.OnMenuItemClickListener, GetIpTask.GetIpTaskListener, NewEmailDialogFragment.OnNewEmailDialogFragmentListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener, View.OnClickListener, MenuItem.OnMenuItemClickListener, EditGroupDialogFragment.OnEditGroupDialogFragmentListener, PeopleListener, ClutterListener, PeopleTopicListener, ClutterTopicListener, PeopleHeaderListener, PeopleDetailListener, ContactListener, DeviceContactTopicListener, ContactsTopicListener, IntellibitzContactTopicListener, UpdateGroupTask.UpdateGroupTaskListener, NewBottomDialogFragment.NewBottomDialogListener, InviteUsersTask.InviteUsersTaskListener {
    companion object {
        private const val TAG = "IntellibitzActivity"
    }
    val mMessenger: Messenger = Messenger(IncomingHandler())
    var mService: Messenger = null
    var mIsBound: Boolean = false
    var messageToNestReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        messageToNest(intent)
    }
}
    var messageToDraftReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        messageToDraft(intent)
    }
}
    var tabArrayList: ArrayList<TabLayout.Tab> = ArrayList()
    var tabHashMap: HashMap<Integer, TabLayout.Tab> = HashMap()
    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    var emailAccountAddedReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        userUpdated(intent)
    }
}
    private var prevMenuItem: MenuItem? = null
    private var tbView: View? = null
    private var appBarLayout: AppBarLayout? = null
    private var toolbar: Toolbar? = null
    var overflowFilterClickListener: View.OnClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        this
    }
}
    private var tbarBottom: Toolbar? = null
    private var tbarMainbox: Toolbar? = null
    private var tbarPeople: Toolbar? = null
    private var tbarContacts: Toolbar? = null
    private var tvToolbarTitle: TextView? = null
    private var tvToolbarSubTitle: TextView? = null
    private var backIntent: Intent? = null
    private var currentItem: Int = 0
    private var messageItem: MessageItem? = null
    private var snackbar: Snackbar? = null
    private var fab: FloatingActionButton? = null
    private var subTitle: String? = null
    private var title: String? = null
    private var updateGroupTask: UpdateGroupTask? = null
    private var shared: Intent? = null
    private var onQueryTextSubmit: String? = null
    private var groupContactsItemFragment: MsgChatGrpContactsFragment? = null
    private var sharedIntent: Intent? = null
    private var newMsgMenuItem: MenuItem? = null
    private var newContactMenuItem: MenuItem? = null
    private var rbBadgePeople: View? = null
    private var rbBadgeInbox: View? = null
    private var llTbarMainboxPeopleFilter: View? = null
    private var llTbarContactsAllTidyFilter: View? = null
    var contactProfileReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        upContactsFragment = intent
    }
}
    private var cbChat: RadioButton? = null
    private var cbMail: RadioButton? = null
    private var cbGroups: RadioButton? = null
    var contactPhoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        upMessagesFragment = intent
    }
}
    var contactThreadReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        upMessagesFragment = intent
    }
}
    private var toggleButton: ToggleButton? = null
    private var forceLogoutReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        MainApplicationSingleton.forceLogout(this)
    }
}
    private var userUpdatedReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        userUpdated(intent)
    }
}
    private var emailAccountRemovedReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        userUpdated(intent)
    }
}
    private var messageSendReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        messageSend(intent)
    }
}
    private var gcmReceiver: BroadcastReceiver = object : BroadcastReceiver() {    override fun onReceive(context: Context, intent: Intent) {
        var sentToken: Boolean = MainApplicationSingleton.getInstance(getApplicationContext())
        if (sentToken) {

        }
        else {

        }
    }
}
    private var mConnection: ServiceConnection = object : ServiceConnection() {    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        mService = Messenger(service)
        try {
            var msg: Message = Message.obtain(null, RcvDocService.MSG_REGISTER_CLIENT)
            msg.replyTo = mMessenger
            mService.send(msg)
        }
        catch (e: RemoteException) {

        }
    }
    override fun onServiceDisconnected(className: ComponentName) {
        mService = null
    }
}
    private fun userUpdated(intent: Intent) {
        var userItem: ContactItem = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        if (((userItem != null) && (userItem == this))) {
            this = userItem
            addEmailsNavMenuFromUser(this)
            notifyUserBaseItemListeners()
        }
    }
    fun getBackIntent(): Intent {
        return backIntent
    }
    fun setBackIntent(backIntent: Intent) {
        this = backIntent
    }
    private fun unregisterReceiver() {

    }
    private fun registerReceiver() {

    }
    fun doBindService() {
        bindService(Intent(this, RcvDocService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true
    }
    fun doUnbindService() {
        if (mIsBound) {
            if ((mService != null)) {
                try {
                    var msg: Message = Message.obtain(null, RcvDocService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    mService.send(msg)
                }
                catch (e: RemoteException) {

                }
            }
            unbindService(mConnection)
            mIsBound = false
        }
    }
    override protected fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
    }
    override protected fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        doUnbindService()
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        stopALLServices()
        unRegsiterALLReceivers()
        doUnbindService()
        super.onDestroy()
    }
    override protected fun onResume() {
        super.onResume()
        if (((alertReadContacts() && alertReadStorage()) && alertWriteStorage())) {
        }
    }
    override protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        contentView = R.layout.activity_intellibitz
        var intent: Intent = getIntent()
        if ((null == savedInstanceState)) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        }
        else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        doBindService()
        registerALLReceivers()
        startALLServices()
        setupTwopane()
        setupAppbar()
        setupNavigationDrawer()
        syncNavigationDrawerContent()
        notifyUserBaseItemListeners()
        var action: String = null
        if ((intent != null)) {
            action = intent.getAction()
            if ("intellibitz.intellidroid.EMAIL_LIST") {
                startAddEmailsActivity()
            }
        }
        handleSharedIntent()
        if ((null == savedInstanceState)) {
            checkTbarBottomMainboxIfNoSelection()
        }
    }
    private fun checkTbarBottomMainboxIfNoSelection() {
        var radioGroup: RadioGroup = (findViewById(R.id.rg_bottom) as RadioGroup)
        if ((1 == radioGroup.checkedRadioButtonId)) {
            checkTbarBottomMainbox()
        }
    }
    private fun checkTbarBottomMainbox() {
        var radioGroup: RadioGroup = (findViewById(R.id.rg_bottom) as RadioGroup)
        var childAt: View = radioGroup.getChildAt(0)
        radioGroup.check(R.id.rb_mainbox)
        childAt.callOnClick()
    }
    private fun checkTbarMainboxPeople() {
        var radioGroup: RadioGroup = (findViewById(R.id.rg_tb_mainbox) as RadioGroup)
        var childAt: View = radioGroup.getChildAt(0)
        radioGroup.check(R.id.rb_people)
        childAt.callOnClick()
    }
    private fun checkTbarMainboxInbox() {
        var radioGroup: RadioGroup = (findViewById(R.id.rg_tb_mainbox) as RadioGroup)
        var childAt: View = radioGroup.getChildAt(1)
        radioGroup.check(R.id.rb_inbox)
        childAt.callOnClick()
    }
    private fun checkBadgeMainboxPeople() {
        var radioGroup: RadioGroup = (findViewById(R.id.rg_badge_mainbox) as RadioGroup)
        var childAt: View = radioGroup.getChildAt(0)
        radioGroup.check(R.id.rb_badge_people)
    }
    private fun checkBadgeMainboxInbox() {
        var radioGroup: RadioGroup = (findViewById(R.id.rg_badge_mainbox) as RadioGroup)
        var childAt: View = radioGroup.getChildAt(1)
        radioGroup.check(R.id.rb_badge_inbox)
    }
    private fun checkTbarContactsAll() {
        var radioGroup: RadioGroup = (findViewById(R.id.rg_tb_contacts) as RadioGroup)
        var childAt: View = radioGroup.getChildAt(0)
        radioGroup.check(R.id.rb_tb_contacts_all)
        childAt.callOnClick()
    }
    private fun isChecked(rg: Int, id: Int, flag: Boolean): Boolean {
        if (flag) {
            return isChecked(rg, id)
        }
        else {
            return isCheckedByPos(rg, id)
        }
    }
    private fun isChecked(rg: Int, id: Int): Boolean {
        if ((id < 0)) {
            return false
        }
        var radioGroup: RadioGroup = (findViewById(rg) as RadioGroup)
        return isCheckedById(radioGroup, id)
    }
    private fun isCheckedByPos(rg: Int, pos: Int): Boolean {
        if ((pos < 0)) {
            return false
        }
        var radioGroup: RadioGroup = (findViewById(rg) as RadioGroup)
        return isCheckedByPos(radioGroup, pos)
    }
    private fun isCheckedById(radioGroup: RadioGroup, id: Int): Boolean {
        if ((id < 0)) {
            return false
        }
        if ((null == radioGroup)) {
            return false
        }
        return (id == radioGroup.checkedRadioButtonId)
    }
    private fun isCheckedByPos(radioGroup: RadioGroup, pos: Int): Boolean {
        if ((pos < 0)) {
            return false
        }
        if ((null == radioGroup)) {
            return false
        }
        var childAt: RadioButton = (radioGroup.getChildAt(pos) as RadioButton)
        return ((childAt != null) && childAt.checked)
    }
    private fun isInboxTab(): Boolean {
        return isChecked(R.id.rg_tb_mainbox, R.id.rb_inbox)
    }
    private fun isInboxTab(tab: TabLayout.Tab): Boolean {
        return isInboxTab()
    }
    private fun isPeopleTab(): Boolean {
        return isChecked(R.id.rg_tb_mainbox, R.id.rb_people)
    }
    private fun isPeopleTab(tab: TabLayout.Tab): Boolean {
        return isPeopleTab()
    }
    private fun isAllContactsTab(): Boolean {
        return isChecked(R.id.rg_tb_contacts, R.id.rb_tb_contacts_all)
    }
    private fun isWorkContactsTab(): Boolean {
        return isChecked(R.id.rg_tb_contacts, R.id.rb_tb_contacts_work)
    }
    private fun isMainTab(): Boolean {
        return isChecked(R.id.rg_bottom, R.id.rb_mainbox)
    }
    private fun isContactsTab(): Boolean {
        return isChecked(R.id.rg_bottom, R.id.rb_contacts)
    }
    private fun isFilesTab(): Boolean {
        return isChecked(R.id.rg_bottom, R.id.rb_files)
    }
    private fun isFeedsTab(): Boolean {
        return isChecked(R.id.rg_bottom, R.id.rb_feeds)
    }
    private fun isMainTab(tab: TabLayout.Tab): Boolean {
        return isMainTab()
    }
    private fun isMainTab(id: Int): Boolean {
        return isMainTab()
    }
    private fun isContactsTab(id: Int): Boolean {
        return isContactsTab()
    }
    private fun isProfileTab(id: Int): Boolean {
        return isContactsTab()
    }
    private fun isContactsTab(tab: TabLayout.Tab): Boolean {
        return isContactsTab()
    }
    private fun isFilesTab(tab: TabLayout.Tab): Boolean {
        return isFilesTab()
    }
    private fun startALLServices() {
        if (MainApplicationSingleton.checkPlayServices(this)) {
            startInstanceIdListenerService()
        }
        getIPAndStartSockets(true)
        startContactService()
    }
    fun stopALLServices() {
        stopService(Intent(this, GCMTokenIntentService::class.java))
        stopService(Intent(this, GCMInstanceIDListenerService::class.java))
        stopService(Intent(this, RcvDocService::class.java))
        stopService(Intent(this, EmailService::class.java))
        stopService(Intent(this, ChatService::class.java))
        stopContactService()
    }
    private fun registerALLReceivers() {
        registerGCMReceiver()
        registerContactPhoneReceiver()
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
    }
    private fun registerContactPhoneReceiver() {
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
    }
    private fun registerGCMReceiver() {
        LocalBroadcastManager.getInstance(this)
    }
    private fun unRegsiterALLReceivers() {
        unRegisterGCMReceiver()
        unRegisterContactPhoneReceiver()
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
    }
    private fun unRegisterGCMReceiver() {
        LocalBroadcastManager.getInstance(this)
    }
    private fun unRegisterContactPhoneReceiver() {
        LocalBroadcastManager.getInstance(this)
        LocalBroadcastManager.getInstance(this)
    }
    fun selectProfileTab(item: MenuItem) {
        selectProfileTab()
        drawerLayout.closeDrawers()
    }
    private fun selectProfileTab() {

    }
    fun selectMainTab(item: MenuItem) {
        selectContactsTab()
        drawerLayout.closeDrawers()
    }
    fun selectMainTab() {

    }
    fun selectContactsTab(item: MenuItem) {
        selectContactsTab()
        drawerLayout.closeDrawers()
    }
    private fun selectContactsTab() {

    }
    fun showBottomToolbar() {

    }
    fun hideBottomToolbar() {

    }
    fun showDetailToolbar() {

    }
    fun hideDetailToolbar() {

    }
    fun showMainToolbar() {
        hideDetailToolbar()
        toolbar.visibility = View.VISIBLE
    }
    private fun setupAppbar() {
        appBarLayout = (findViewById(R.id.appbar) as AppBarLayout)
        setupToolbar()
        setupMainboxToolbar()
        setupContactsToolbar()
        setupBottomToolbar()
    }
    fun getTvToolbarTitle(): TextView {
        return tvToolbarTitle
    }
    fun setTvToolbarTitle(toolbar_messages_title: Int) {
        if ((null == tvToolbarTitle)) {
            initToolbar()
        }
        if ((null == tvToolbarTitle)) {
            Log.e(TAG, "Title toolbar NULL - cannot set title")
            return
        }
        tvToolbarTitle.text = toolbar_messages_title
    }
    fun setTvToolbarSubTitle(title: String) {
        if ((null == tvToolbarSubTitle)) {
            initToolbar()
        }
        if ((null == tvToolbarSubTitle)) {
            Log.e(TAG, "Sub title toolbar NULL - cannot set subtitle")
            return
        }
        tvToolbarSubTitle.text = title
    }
    fun setDetailToolbarSubTitle(title: String) {
        detailToolbar
    }
    fun setDetailToolbarTitle(title: String) {
        detailToolbar
    }
    fun setTvToolbarSubTitle(toolbar_messages_title: Int) {
        if ((null == tvToolbarSubTitle)) {
            initToolbar()
        }
        if ((null == tvToolbarSubTitle)) {
            Log.e(TAG, "Sub title toolbar NULL - cannot set subtitle")
            return
        }
        tvToolbarSubTitle.text = toolbar_messages_title
    }
    private fun setupToolbar() {
        tbView = findViewById(R.id.ll_toolbar)
        initToolbar()
        supportActionBar = toolbar
        var actionBar: ActionBar = supportActionBar
        if ((actionBar != null)) {
            actionBar.displayUseLogoEnabled = false
        }
        tvToolbarTitle = R.string.app_name
        tvToolbarSubTitle = R.string.app_title
    }
    private fun initToolbar() {
        toolbar = (findViewById(R.id.toolbar) as Toolbar)
        if ((toolbar != null)) {
            tvToolbarTitle = (toolbar.findViewById(R.id.toolbar_title) as TextView)
            tvToolbarSubTitle = (toolbar.findViewById(R.id.toolbar_subtitle) as TextView)
        }
    }
    fun showOverFlowFilter() {

    }
    fun showFilter() {

    }
    fun hideFilter() {

    }
    fun hideOverFlowFilter() {

    }
    fun showDetailOverFlowFilter() {

    }
    fun showDetailFilter() {

    }
    fun hideDetailFilter() {

    }
    fun hideDetailOverFlowFilter() {

    }
    fun hideFilters() {
        hideItemFilters()
        hideDetailFilters()
    }
    fun hideItemFilters() {
        hideFilter()
        hideOverFlowFilter()
    }
    fun hideDetailFilters() {
        hideDetailFilter()
        hideDetailOverFlowFilter()
    }
    private fun setupFilterToolbar() {
        hideFilter()
        hideOverFlowFilter()
    }
    fun showTbarMainboxPeopleFilterView() {
        if ((llTbarMainboxPeopleFilter != null)) {
            llTbarMainboxPeopleFilter.visibility = View.VISIBLE
        }
    }
    fun hideTbarMainboxPeopleFilterView() {
        if ((llTbarMainboxPeopleFilter != null)) {
            llTbarMainboxPeopleFilter.visibility = View.GONE
        }
    }
    fun showTbarContactsAllTidyFilterView() {
        if ((llTbarContactsAllTidyFilter != null)) {
            llTbarContactsAllTidyFilter.visibility = View.VISIBLE
        }
    }
    fun hideTbarContactsAllTidyFilterView() {
        if ((llTbarContactsAllTidyFilter != null)) {
            llTbarContactsAllTidyFilter.visibility = View.GONE
        }
    }
    fun getCbChat(): RadioButton {
        return cbChat
    }
    fun getCbMail(): RadioButton {
        return cbMail
    }
    fun getCbGroups(): RadioButton {
        return cbGroups
    }
    private fun setupBottomToolbar() {
        if ((null == tbarBottom)) {
            tbarBottom = (findViewById(R.id.tb_bottom) as Toolbar)
        }
        var view: View = findViewById(R.id.rb_mainbox)
        view.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        onTbarBottomMainboxClicked()
    }
}
        var feeds: View = findViewById(R.id.rb_feeds)
        feeds.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        onTbarBottomFeedsClicked()
    }
}
        var view2: View = findViewById(R.id.rb_contacts)
        view2.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        onTbarBottomContactsClicked()
    }
}
        var view3: View = findViewById(R.id.rb_files)
        view3.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        onTbarBottomFilesClicked()
    }
}
    }
    private fun onTbarBottomFilesClicked() {
        hideTbarPeople()
        hideTbarMainbox()
        hideTbarContacts()
        showFilesFragment()
    }
    private fun onTbarBottomFeedsClicked() {
        hideTbarMainbox()
        hideTbarPeople()
        hideTbarContacts()
        showNewMsgMenuItem()
    }
    private fun onTbarBottomContactsClicked() {
        hideTbarPeople()
        hideTbarMainbox()
        hideNewMsgMenuItem()
        showContactsTitle()
        showTbarContacts()
        checkTbarContactsAll()
    }
    private fun onTbarBottomMainboxClicked() {
        hideTbarContacts()
        showMainboxTitle()
        showNewMsgMenuItem()
        showTbarMainbox()
        checkTbarMainboxPeople()
    }
    private fun onTbarContactsAllClicked() {
        showTbarContactsAllTidyFilterView()
        showAllContactsFragment()
    }
    private fun onTbarContactsWorkClicked() {
        hideTbarContactsAllTidyFilterView()
        showWorkContactsFragment()
    }
    private fun onTbarMainboxInboxClicked() {
        hideTbarPeople()
        showMsgsGrpClutterFragment()
    }
    private fun onTbarMainboxPeopleClicked() {
        showMsgsGrpPeopleFragment()
    }
    private fun hideTbarContacts() {
        tbarContacts.visibility = View.GONE
    }
    private fun showTbarContacts() {
        tbarContacts.visibility = View.VISIBLE
    }
    private fun showTbarMainbox() {
        tbarMainbox.visibility = View.VISIBLE
    }
    private fun showTbarPeople() {
        tbarPeople.visibility = View.VISIBLE
    }
    private fun hideTbarPeople() {
        tbarPeople.visibility = View.GONE
    }
    private fun hideTbarMainbox() {
        tbarMainbox.visibility = View.GONE
    }
    private fun setupMainboxToolbar() {
        showMainboxTitle()
        if ((null == tbarMainbox)) {
            tbarMainbox = (findViewById(R.id.tb_mainbox) as Toolbar)
            val people: View = findViewById(R.id.rb_people)
            rbBadgePeople = findViewById(R.id.rb_badge_people)
            people.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        checkBadgeMainboxPeople()
        onTbarMainboxPeopleClicked()
    }
}
            rbBadgePeople.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        checkTbarMainboxPeople()
    }
}
            val inbox: View = findViewById(R.id.rb_inbox)
            rbBadgeInbox = findViewById(R.id.rb_badge_inbox)
            inbox.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        checkBadgeMainboxInbox()
        onTbarMainboxInboxClicked()
    }
}
            rbBadgeInbox.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        checkTbarMainboxInbox()
    }
}
            hideBadgePeople()
            hideBadgeInbox()
            tbarPeople = (findViewById(R.id.tb_people) as Toolbar)
            cbChat = (findViewById(R.id.cb_chat) as RadioButton)
            cbGroups = (findViewById(R.id.cb_groups) as RadioButton)
            cbMail = (findViewById(R.id.cb_mail) as RadioButton)
            llTbarMainboxPeopleFilter = findViewById(R.id.ll_tb_mainbox_people_filter)
            setupTbarMainboxPeopleFilter(1, llTbarMainboxPeopleFilter)
            cbChat.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        if ((v as RadioButton)) {
            showMsgsGrpPeopleChatsFragment()
        }
    }
}
            cbGroups.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        if ((v as RadioButton)) {
            showMsgsGrpPeopleChatGroupsFragment()
        }
    }
}
            cbMail.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        if ((v as RadioButton)) {
            showMsgsGrpPeopleEmailsFragment()
        }
    }
}
        }
        hideMainboxToolbar()
    }
    private fun hideBadgeInbox() {
        rbBadgeInbox.visibility = View.GONE
    }
    private fun hideBadgePeople() {
        rbBadgePeople.visibility = View.GONE
    }
    private fun showMainboxTitle() {
        tvToolbarTitle = R.string.mainbox
    }
    private fun setupContactsToolbar() {
        showContactsTitle()
        if ((null == tbarContacts)) {
            tbarContacts = (findViewById(R.id.tb_contacts) as Toolbar)
            var all: View = findViewById(R.id.rb_tb_contacts_all)
            all.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        onTbarContactsAllClicked()
    }
}
            var work: View = findViewById(R.id.rb_tb_contacts_work)
            work.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        onTbarContactsWorkClicked()
    }
}
            llTbarContactsAllTidyFilter = findViewById(R.id.ll_tidy)
            setupTbarContactsAllTidyFilter(2, llTbarContactsAllTidyFilter)
        }
        hideContactsToolbar()
    }
    private fun showContactsTitle() {
        tvToolbarTitle = R.string.contacts
    }
    private fun hideMainboxToolbar() {
        tbarMainbox.visibility = View.GONE
        tbarPeople.visibility = View.GONE
    }
    private fun hideContactsToolbar() {
        tbarContacts.visibility = View.GONE
    }
    private fun setupTbarContactsAllTidyFilter(id: Int, view: View) {
        val sw: Switch = (view.findViewById(R.id.sw_tidy) as Switch)
        sw.tag = id
        sw.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        var sw: Switch = (v as Switch)
        if (sw.checked) {
            showWorkContactsFragment()
        }
        else {
            showAllContactsFragment()
        }
    }
}
    }
    private fun setupTbarMainboxPeopleFilter(id: Int, view: View) {
        toggleButton = (view.findViewById(R.id.tb_filter) as ToggleButton)
        var listener: View.OnClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        toggleButton.performClick()
    }
}
        view.onClickListener = listener
        toggleButton.tag = id
        toggleButton.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        var sw: ToggleButton = (v as ToggleButton)
        if (sw.checked) {
            showTbarPeople()
            showMsgsGrpPeopleChatsFragment()
        }
        else {
            hideTbarPeople()
            showMsgsGrpPeopleFragment()
        }
        return
    }
}
        var onClickListener: View.OnClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {
        var i: Int = 0
        try {
            i = Integer.parseInt(toggleButton.getText())
        }
        catch (ignored: Throwable) {

        }
        var cb: CheckBox = (view as CheckBox)
        if ((R.id.cb_chat == cb.getId())) {
            if (cb.checked) {
                i++
            }
            else {
                i--
            }
        }
        else {
            if ((R.id.cb_groups == cb.getId())) {
                if (cb.checked) {
                    i++
                }
                else {
                    i--
                }
            }
            else {
                if ((R.id.cb_mail == cb.getId())) {
                    if (cb.checked) {
                        i++
                    }
                    else {
                        i--
                    }
                }
            }
        }
        if (((i <= 0) || (i > 3))) {
            i = 3
        }
        var count: String = String.valueOf(i)
    }
}
    }
    private fun setOverflowFilterCount(count: String) {
        if ((null == toggleButton)) {
            toggleButton = (rootView as ToggleButton)
        }
        toggleButton.text = count
        toggleButton.textOff = count
        toggleButton.textOn = count
    }
    private fun setupNavigationDrawer() {
        drawerLayout = (findViewById(R.id.drawer_layout) as DrawerLayout)
        var toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView = (findViewById(R.id.navigation_view) as NavigationView)
        setNewEmailNavigationMenuIntent()
        navigationView.navigationItemSelectedListener = this
    }
    private fun setNewEmailNavigationMenuIntent() {
        var mi: MenuItem = navigationView.menu
        mi.intent = newEmailAccountListIntent()
        var mi2: MenuItem = navigationView.menu
        mi.intent = newCompanyListIntent()
    }
    private fun syncNavigationDrawerContent() {
        setNewEmailNavigationMenuIntent()
        var header: View = navigationView.getHeaderView(0)
        header.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        startProfileActivity(user)
    }
}
        var profile: View = header.findViewById(R.id.ll_profile)
        profile.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        startProfileActivity(user)
    }
}
        var imageView: ImageView = (header.findViewById(R.id.niv_profile) as ImageView)
        imageView.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        startProfileActivity(user)
    }
}
        if ((user != null)) {
            (header.findViewById(R.id.text1) as TextView)
            (header.findViewById(R.id.text2) as TextView)
            var pic: String = user.profilePic
            if (((null != pic) && !pic.empty)) {
                (imageView as NetworkImageView)
            }
            addEmailsNavMenuFromUser(user)
            addCompaniesNavMenuFromUser(user)
        }
    }
    private fun addEmailsNavMenuFromUser(user: ContactItem) {
        if ((null == navigationView)) {
            Log.e(TAG, "addEmailsNavMenuFromUser: nav view is null")
            return
        }
        if ((null == user)) {
            Log.e(TAG, "addEmailsNavMenuFromUser: user is null")
            return
        }
        var menu: Menu = navigationView.menu
        var item: MenuItem = menu.findItem(R.id.menu_email_accounts)
        var subMenu: SubMenu = item.subMenu
        var sz: Int = subMenu.size()
        var i: Int = 0
        while ((i < sz)) {
            var menuItem: MenuItem = subMenu.getItem(i)
            var itemId: Int = menuItem.getItemId()
            if ((R.id.action_email_accounts == itemId)) {

            }
            else {
                subMenu.removeItem(itemId)
            }
            i++
        }
        var emails: Set<ContactItem> = user.contactItems
        if (((emails != null) && !emails.empty)) {
            for (email in emails) {
                if ((null == subMenu.findItem(email.hashCode()))) {
                    var mi: MenuItem = subMenu.add(R.id.menu_email_accounts, email.hashCode(), Menu.FIRST, email.email)
                    mi.icon = R.drawable.ic_account_circle_black_18dp
                    mi.checkable = true
                }
            }
        }
    }
    private fun addCompaniesNavMenuFromUser(user: ContactItem) {
        if ((null == navigationView)) {
            Log.e(TAG, "addCompaniesNavMenuFromUser: nav view is null")
            return
        }
        if ((null == user)) {
            Log.e(TAG, "addCompaniesNavMenuFromUser: user is null")
            return
        }
        var menu: Menu = navigationView.menu
        var item: MenuItem = menu.findItem(R.id.gi_company_accounts)
        var subMenu: SubMenu = item.subMenu
        var sz: Int = subMenu.size()
        var i: Int = 0
        while ((i < sz)) {
            var menuItem: MenuItem = subMenu.getItem(i)
            var itemId: Int = menuItem.getItemId()
            if ((R.id.mi_company_accounts == itemId)) {

            }
            else {
                subMenu.removeItem(itemId)
            }
            i++
        }
        var companyName: String = user.companyName
        var companyId: String = user.companyId
        if (!TextUtils.isEmpty(companyId)) {
            var mi: MenuItem = subMenu.add(R.id.gi_company_accounts, companyId.hashCode(), Menu.FIRST, companyName)
            mi.icon = R.drawable.other_mail_icon
            mi.checkable = true
        }
    }
    fun isEmptyEmailsMenu(): Boolean {
        var menu: Menu = navigationView.menu
        if ((menu != null)) {
            var item: MenuItem = menu.findItem(R.id.menu_email_accounts)
            if ((item != null)) {
                var subMenu: SubMenu = item.subMenu
                return ((null == subMenu) || (subMenu.size() <= 1))
            }
        }
        return true
    }
    fun isEmptyCompaniesMenu(): Boolean {
        var menu: Menu = navigationView.menu
        if ((menu != null)) {
            var item: MenuItem = menu.findItem(R.id.mi_company_accounts)
            if ((item != null)) {
                var subMenu: SubMenu = item.subMenu
                return ((null == subMenu) || (subMenu.size() <= 1))
            }
        }
        return true
    }
    fun messageSend(shared: Intent) {
        if (isPeopleTab()) {
            var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
            msgsGrpPeopleFragment.messageSend(shared)
            return
        }
        if (isInboxTab()) {
            var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
            msgsGrpClutterFragment.messageSend(shared)
            return
        }
    }
    private fun setupMessagesFragment(shared: Intent) {
        setupMainboxToolbars()
        showNewMsgMenuItem()
    }
    private fun setupMessagesFragment() {
        setupMainboxToolbars()
        showNewMsgMenuItem()
    }
    private fun setupContactsFragment(shared: Intent) {
        selectContactsTab()
        setupContactsToolbar()
        showNewMsgMenuItem()
    }
    private fun setupContactsFragment() {
        setupContactsToolbar()
        hideNewMsgMenuItem()
        hideTbarMainboxPeopleFilterView()
        showNewContactMenuItem()
    }
    private fun showWorkContactsFragment() {
        showMainToolbar()
        var intellibitzContactsFragment: IntellibitzContactsFragment = newIntellibitzContactItemFragment()
        replaceContentFragment(intellibitzContactsFragment, IntellibitzContactsFragment.TAG)
    }
    private fun showAllContactsFragment() {
        showMainToolbar()
        var deviceContactsFragment: DeviceContactsFragment = newDeviceContactItemFragment()
        replaceContentFragment(deviceContactsFragment, DeviceContactsFragment.TAG)
    }
    fun showMsgsGrpClutterFragment(): MsgsGrpClutterFragment {
        showMainToolbar()
        hideTbarMainboxPeopleFilterView()
        var msgsGrpClutterFragment: MsgsGrpClutterFragment = newMsgsGrpClutterFragment()
        replaceContentFragment(msgsGrpClutterFragment, MsgsGrpClutterFragment.TAG)
        return msgsGrpClutterFragment
    }
    fun showMsgsGrpPeopleFragment(): MsgsGrpPeopleFragment {
        hideDetailToolbar()
        hideDetailFilters()
        showMainToolbar()
        showTbarMainboxPeopleFilterView()
        var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = newMsgsGrpPeopleFragment()
        replaceContentFragment(msgsGrpPeopleFragment, MsgsGrpPeopleFragment.TAG)
        return msgsGrpPeopleFragment
    }
    private fun showMsgsGrpPeopleChatsFragment() {
        hideDetailToolbar()
        hideDetailFilters()
        showMainToolbar()
        showTbarMainboxPeopleFilterView()
        var msgsGrpPeopleChatsFragment: MsgsGrpPeopleChatsFragment = newMsgsGrpPeopleChatsFragment()
        replaceContentFragment(msgsGrpPeopleChatsFragment, MsgsGrpPeopleChatsFragment.TAG)
    }
    private fun showMsgsGrpPeopleChatGroupsFragment() {
        hideDetailToolbar()
        hideDetailFilters()
        showMainToolbar()
        showTbarMainboxPeopleFilterView()
        var msgsGrpPeopleChatGroupsFragment: MsgsGrpPeopleChatGroupsFragment = newMsgsGrpPeopleChatGroupsFragment()
        replaceContentFragment(msgsGrpPeopleChatGroupsFragment, MsgsGrpPeopleChatGroupsFragment.TAG)
    }
    private fun showMsgsGrpPeopleEmailsFragment() {
        hideDetailToolbar()
        hideDetailFilters()
        showMainToolbar()
        showTbarMainboxPeopleFilterView()
        var msgsGrpPeopleEmailsFragment: MsgsGrpPeopleEmailsFragment = newMsgsGrpPeopleEmailsFragment()
        replaceContentFragment(msgsGrpPeopleEmailsFragment, MsgsGrpPeopleEmailsFragment.TAG)
    }
    private fun newMsgsGrpPeopleChatsFragment(): MsgsGrpPeopleChatsFragment {
        var messageItem: MessageItem = null
        var intent: Intent = getIntent()
        if ((intent != null)) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG)
        }
        return MsgsGrpPeopleChatsFragment.newInstance(messageItem, user, this)
    }
    private fun newMsgsGrpPeopleChatGroupsFragment(): MsgsGrpPeopleChatGroupsFragment {
        var messageItem: MessageItem = null
        var intent: Intent = getIntent()
        if ((intent != null)) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG)
        }
        return MsgsGrpPeopleChatGroupsFragment.newInstance(messageItem, user, this)
    }
    private fun newMsgsGrpPeopleEmailsFragment(): MsgsGrpPeopleEmailsFragment {
        var messageItem: MessageItem = null
        var intent: Intent = getIntent()
        if ((intent != null)) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG)
        }
        return MsgsGrpPeopleEmailsFragment.newInstance(messageItem, user, this)
    }
    private fun newMsgsGrpPeopleFragment(): MsgsGrpPeopleFragment {
        var messageItem: MessageItem = null
        var intent: Intent = getIntent()
        if ((intent != null)) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG)
        }
        return MsgsGrpPeopleFragment.newInstance(messageItem, user, this)
    }
    private fun newMsgsGrpClutterFragment(): MsgsGrpClutterFragment {
        var messageItem: MessageItem = null
        var intent: Intent = getIntent()
        if ((intent != null)) {
            messageItem = intent.getParcelableExtra(MessageItem.TAG)
        }
        return MsgsGrpClutterFragment.newInstance(messageItem, user, this)
    }
    private fun showFilesFragment(): AttachmentsFragment {
        showFilesTitle()
        hideNewMsgMenuItem()
        hideTbarMainboxPeopleFilterView()
        hideNewContactMenuItem()
        var attachmentsFragment: AttachmentsFragment = AttachmentsFragment.newInstance(user, this)
        replaceContentFragment(attachmentsFragment, AttachmentsFragment.TAG)
        return attachmentsFragment
    }
    private fun showFilesTitle() {
        tvToolbarTitle = R.string.files
    }
    private fun showFeedsTitle() {
        tvToolbarTitle = R.string.feeds
    }
    fun removeMessageListFragment(): Fragment {
        hideFilters()
        return null
    }
    fun removeContactsFragment(): Fragment {
        hideFilters()
        return null
    }
    fun removeFilesFragment(): Fragment {
        return null
    }
    fun popFragment() {
        if ((getSupportFragmentManager() > 0)) {
            var fragmentTransaction: FragmentTransaction = getSupportFragmentManager()
            getSupportFragmentManager()
            fragmentTransaction.commitAllowingStateLoss()
        }
    }
    fun removeFragment(fragment: Fragment): Fragment {
        if ((null == fragment)) {
            return null
        }
        if ((getSupportFragmentManager() > 0)) {
            var fragmentTransaction: FragmentTransaction = getSupportFragmentManager()
            var fragmentByTag: Fragment = getSupportFragmentManager()
            if ((fragmentByTag != null)) {
                fragmentTransaction.remove(fragmentByTag)
            }
            fragmentTransaction.remove(fragment)
            fragmentTransaction.commitAllowingStateLoss()
        }
        return fragment
    }
    fun removeChildFragment(fragment: Fragment): Fragment {
        if ((null == fragment)) {
            return null
        }
        var childFragmentManager: FragmentManager = fragment.childFragmentManager
        if ((childFragmentManager.backStackEntryCount > 0)) {
            var fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
            var fragmentByTag: Fragment = getSupportFragmentManager()
            if ((fragmentByTag != null)) {
                fragmentTransaction.remove(fragmentByTag)
            }
            fragmentTransaction.remove(fragment)
            fragmentTransaction.commitAllowingStateLoss()
        }
        return fragment
    }
    fun replaceContentFragment(fragment: Fragment, tag: String): Fragment {
        if ((null == fragment)) {
            return null
        }
        var fragmentTransaction: FragmentTransaction = getSupportFragmentManager()
        fragmentTransaction.replace(R.id.two_pane_container, fragment, tag)
        fragmentTransaction.commit()
        return fragment
    }
    fun replaceContentFragment(fragment: Fragment): Fragment {
        if ((null == fragment)) {
            return null
        }
        return replaceContentFragment(fragment, fragment.getClass())
    }
    fun replaceDetailFragment(fragment: Fragment): Fragment {
        if ((null == fragment)) {
            return null
        }
        removeFragment(fragment)
        var fragmentTransaction: FragmentTransaction = getSupportFragmentManager()
        if (twoPane) {
            detailView.visibility = View.GONE
            fragmentTransaction.replace(R.id.two_pane_empty_container, fragment, fragment.getClass())
            detailView.visibility = View.VISIBLE
        }
        else {
            fragmentTransaction.replace(R.id.two_pane_container, fragment, fragment.getClass())
        }
        fragmentTransaction.commit()
        return fragment
    }
    fun replaceChildDetailFragment(fragment: Fragment, parent: Fragment): Fragment {
        if ((null == fragment)) {
            return null
        }
        itemView.visibility = View.GONE
        var fragmentTransaction: FragmentTransaction = parent.childFragmentManager
        if (twoPane) {
            detailView.visibility = View.GONE
            fragmentTransaction.replace(R.id.two_pane_empty_container, fragment, fragment.getClass())
            detailView.visibility = View.VISIBLE
        }
        else {
            fragmentTransaction.replace(R.id.two_pane_container, fragment, fragment.getClass())
        }
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commitAllowingStateLoss()
        itemView.visibility = View.VISIBLE
        return fragment
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater
        newMsgMenuItem = menu.findItem(R.id.action_new_message)
        newContactMenuItem = menu.findItem(R.id.mi_c_contact)
        hideNewContactMenuItem()
        var searchItem: MenuItem = menu.findItem(R.id.action_search)
        if ((searchItem != null)) {
            var searchView: SearchView = (MenuItemCompat.getActionView(searchItem) as SearchView)
            searchView.onClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        tvToolbarTitle.visibility = View.GONE
        tvToolbarSubTitle.visibility = View.GONE
        tbView.visibility = View.GONE
    }
}
            searchView.onSearchClickListener = object : View.OnClickListener() {    override fun onClick(v: View) {
        tvToolbarTitle.visibility = View.GONE
        tvToolbarSubTitle.visibility = View.GONE
        tbView.visibility = View.GONE
        hideBottomToolbar()
    }
}
            searchView.onCloseListener = object : SearchView.OnCloseListener() {    override fun onClose(): Boolean {
        tvToolbarTitle.visibility = View.VISIBLE
        tvToolbarSubTitle.visibility = View.VISIBLE
        tbView.visibility = View.VISIBLE
        onQueryClose()
        showBottomToolbar()
        return false
    }
}
            searchView.onQueryTextListener = object : SearchView.OnQueryTextListener() {    override fun onQueryTextSubmit(query: String): Boolean {
        onQuerySubmit(query)
        hideBottomToolbar()
        return false
    }
    override fun onQueryTextChange(newText: String): Boolean {
        onQueryChange(newText)
        hideBottomToolbar()
        return false
    }
}
        }
        return super.onCreateOptionsMenu(menu)
    }
    private fun showNewMsgMenuItem() {
        hideNewContactMenuItem()
        if ((newMsgMenuItem != null)) {
            newMsgMenuItem.visible = true
        }
    }
    private fun hideNewMsgMenuItem() {
        if ((newMsgMenuItem != null)) {
            newMsgMenuItem.visible = false
        }
    }
    private fun showNewContactMenuItem() {
        hideNewMsgMenuItem()
        if ((newContactMenuItem != null)) {
            newContactMenuItem.visible = true
        }
    }
    private fun hideNewContactMenuItem() {
        if ((newContactMenuItem != null)) {
            newContactMenuItem.visible = false
        }
    }
    private fun onQueryClose() {
        if (isMainTab()) {
            if (isInboxTab()) {
                var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
                msgsGrpClutterFragment.onClose()
            }
            else {
                if (isPeopleTab()) {
                    var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                    msgsGrpPeopleFragment.onClose()
                }
            }
            showTabs()
        }
        else {
            if (isContactsTab()) {
                if (isAllContactsTab()) {
                    var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
                    deviceContactsFragment.onClose()
                }
                else {
                    if (isWorkContactsTab()) {
                        var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
                        intellibitzContactsFragment.onClose()
                    }
                }
                showContactsTabs()
            }
            else {
                if (isFilesTab()) {
                    var attachmentsFragment: AttachmentsFragment = (getSupportFragmentManager() as AttachmentsFragment)
                    attachmentsFragment.onClose()
                }
            }
        }
    }
    private fun onQueryChange(query: String) {
        if (isMainTab()) {
            if (isInboxTab()) {
                var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
                if ((msgsGrpClutterFragment != null)) {
                    msgsGrpClutterFragment.onQueryTextChange(query)
                }
            }
            else {
                if (isPeopleTab()) {
                    var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                    if ((msgsGrpPeopleFragment != null)) {
                        msgsGrpPeopleFragment.onQueryTextChange(query)
                    }
                }
            }
            hideTabs()
        }
        if (isContactsTab()) {
            if (isAllContactsTab()) {
                var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
                if ((deviceContactsFragment != null)) {
                    deviceContactsFragment.onQueryTextChange(query)
                }
            }
            else {
                if (isWorkContactsTab()) {
                    var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
                    if ((intellibitzContactsFragment != null)) {
                        intellibitzContactsFragment.onQueryTextChange(query)
                    }
                }
            }
            hideContactsTabs()
        }
        if (isFilesTab()) {
            var attachmentsFragment: AttachmentsFragment = (getSupportFragmentManager() as AttachmentsFragment)
            if ((attachmentsFragment != null)) {
                attachmentsFragment.onQueryTextChange(query)
            }
        }
    }
    private fun onQuerySubmit(query: String) {
        if (isMainTab()) {
            if (isInboxTab()) {
                var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
                if ((msgsGrpClutterFragment != null)) {
                    msgsGrpClutterFragment.onQueryTextSubmit(query)
                }
            }
            else {
                if (isPeopleTab()) {
                    var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                    if ((msgsGrpPeopleFragment != null)) {
                        msgsGrpPeopleFragment.onQueryTextSubmit(query)
                    }
                }
            }
            hideTabs()
        }
        if (isContactsTab()) {
            if (isAllContactsTab()) {
                var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
                if ((deviceContactsFragment != null)) {
                    deviceContactsFragment.onQueryTextSubmit(query)
                }
            }
            else {
                if (isWorkContactsTab()) {
                    var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
                    if ((intellibitzContactsFragment != null)) {
                        intellibitzContactsFragment.onQueryTextChange(query)
                    }
                }
            }
            hideContactsTabs()
        }
        if (isFilesTab()) {
            var attachmentsFragment: AttachmentsFragment = (getSupportFragmentManager() as AttachmentsFragment)
            if ((attachmentsFragment != null)) {
                attachmentsFragment.onQueryTextChange(query)
            }
        }
    }
    private fun onDetailQueryClose() {
        if (isMainTab()) {
            if (isInboxTab()) {

            }
            else {
                if (isPeopleTab()) {
                    var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                    msgsGrpPeopleFragment.onDetailClose()
                }
            }
        }
        else {
            if (isContactsTab()) {

            }
            else {
                if (isProfileTab()) {

                }
            }
        }
    }
    private fun onDetailQueryChange(query: String) {
        if (isMainTab()) {
            if (isInboxTab()) {

            }
            else {
                if (isPeopleTab()) {
                    var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                    msgsGrpPeopleFragment.onDetailQueryTextChange(query)
                }
            }
        }
        else {
            if (isContactsTab()) {

            }
            else {
                if (isProfileTab()) {

                }
            }
        }
    }
    private fun onDetailQuerySubmit(query: String) {
        if (isMainTab()) {
            if (isInboxTab()) {

            }
            else {
                if (isPeopleTab()) {
                    var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                    msgsGrpPeopleFragment.onDetailQueryTextSubmit(query)
                    msgsGrpPeopleFragment.onQueryTextSubmit(query)
                }
            }
        }
        else {
            if (isContactsTab()) {

            }
            else {
                if (isProfileTab()) {

                }
            }
        }
    }
    fun getSelectedTabPosition(): Int {
        return 1
    }
    fun getSelectedMainboxTab(): TabLayout.Tab {
        return null
    }
    fun getSelectedTab(): TabLayout.Tab {
        return null
    }
    private fun isProfileTab(): Boolean {
        var tab: TabLayout.Tab = selectedTab
        return isProfileTab(tab)
    }
    private fun isProfileTab(tab: TabLayout.Tab): Boolean {
        return ((null != tab) && (R.id.action_settings == (tab.getTag() as Int)))
    }
    fun onMainboxTabSelected(tab: TabLayout.Tab) {
        if ((null == tab)) {
            return
        }
        var people: Boolean = false
        var clutter: Boolean = false
        var chat: Boolean = false
        var group: Boolean = false
        var email: Boolean = false
        var files: Boolean = false
        var flagged: Boolean = false
        var unread: Boolean = false
        if (isPeopleTab()) {
            setupPeopleFilter()
            var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
            msgsGrpPeopleFragment = showMsgsGrpPeopleFragment()
            people = true
        }
        if (isInboxTab()) {
            setupClutterFilter()
            var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
            msgsGrpClutterFragment = showMsgsGrpClutterFragment()
            clutter = true
        }
        if (isChatTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            }
            else {
                chat = true
            }
        }
        if (isGroupTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            }
            else {
                group = true
            }
        }
        if (isEmailTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            }
            else {
                email = true
            }
        }
        if (isFilesTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            }
            else {
                files = true
            }
        }
        if (isFlaggedTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            }
            else {
                flagged = true
            }
        }
        if (isUnreadTab()) {
            if (isMainboxTabViewButtonSelected(tab)) {

            }
            else {
                unread = true
            }
        }
        chat = isChatSelected()
        group = isGroupSelected()
        email = isEmailSelected()
        files = isFilesSelected()
        flagged = isFlaggedSelected()
        unread = isUnreadSelected()
        onTabSelected(people, clutter, chat, group, email, files, flagged, unread)
    }
    override fun onMenuItemClick(item: MenuItem): Boolean {
        if ((prevMenuItem != null)) {
            prevMenuItem.icon
        }
        prevMenuItem = item
        item.icon
        if (isMainTab(item.getItemId())) {
            setupMessagesFragment()
            return true
        }
        if (isContactsTab(item.getItemId())) {
            setupContactsFragment()
            return true
        }
        if (isProfileTab(item.getItemId())) {
            return true
        }
        if ((R.id.action_email_accounts == item.getItemId())) {
            if (isEmptyEmailsMenu()) {
                startAddEmailActivity()
            }
            else {
                startAddEmailsActivity()
            }
            return true
        }
        if ((R.id.gi_company_accounts == item.getItemId())) {
            if (isEmptyCompaniesMenu()) {
                startAddEmailActivity()
            }
            else {
                startAddEmailsActivity()
            }
            return true
        }
        if ((R.id.action_settings == item.getItemId())) {
            startProfileActivity(user)
            return true
        }
        if ((R.id.mi_clutter == item.getItemId())) {
            showMsgsGrpClutterFragment()
            return true
        }
        if ((R.id.mi_people == item.getItemId())) {
            showMsgsGrpPeopleFragment()
            return true
        }
        if ((R.id.mi_chat == item.getItemId())) {
            onMenuChatMessages(item)
            return true
        }
        if ((R.id.mi_group == item.getItemId())) {
            showGroupMessages()
            return true
        }
        if ((R.id.mi_email == item.getItemId())) {
            showPeopleEmailMessages()
            return true
        }
        if ((R.id.mi_files == item.getItemId())) {
            showFilesMessages()
            return true
        }
        return false
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (((null != item) && (item.getItemId() == R.id.action_new_message))) {
            if (isFeedsTab()) {
                onNewFeedsMenuClicked()
                return true
            }
            onNewMenuClicked()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var id: Int = item.getItemId()
        if ((id == R.id.mi_schedules)) {
            startScheduleActivity(user)
        }
        else {
            if ((id == R.id.mi_create_company)) {
                startCompanyCreateActivity(user)
            }
            else {
                if ((id == R.id.mi_join_company)) {
                    startGetInvitesActivity(user)
                }
                else {
                    if ((id == R.id.mi_invite_users)) {
                        startInviteUsersActivity(user)
                    }
                    else {
                        if ((id == R.id.nav_contact)) {
                            selectContactsTab(item)
                        }
                        else {
                            if ((id == R.id.action_settings)) {
                                startProfileActivity(user)
                            }
                            else {
                                if ((id == R.id.action_email_accounts)) {
                                    if (isEmptyEmailsMenu()) {
                                        startAddEmailActivity()
                                    }
                                    else {
                                        startAddEmailsActivity()
                                    }
                                }
                                else {
                                    if ((id == R.id.mi_company_accounts)) {
                                        startCompanyListActivity(user)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        var drawer: DrawerLayout = (findViewById(R.id.drawer_layout) as DrawerLayout)
        if ((drawer != null)) {
            drawer.closeDrawer(GravityCompat.START)
        }
        return true
    }
    private fun saveMessageThreadItem(item: MessageItem) {
        var msg: Message = Message.obtain(null, RcvDocService.MSG_SET_VALUE, this, 0)
        var args: Bundle = Bundle()
        args.putParcelable(MessageItem.TAG, item)
        msg.data = args
        try {
            mService.send(msg)
        }
        catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
    private fun getIPAndStartSockets(b: Boolean) {
        var getIpTask: GetIpTask = GetIpTask(b, user.dataId, user.token, user.device, user.deviceRef, MainApplicationSingleton.AUTH_GET_IP, this)
        getIpTask.requestTimeoutMillis = 30000
        getIpTask.getIpTaskListener = this
        getIpTask.execute()
    }
    override fun onPostGetIpResponse(response: JSONObject, b: Boolean) {
        var status: Int = 0
        if ((response != null)) {
            status = response.optInt("status")
        }
        if ((((null == response) || (1 == status)) || (99 == status))) {
            onPostGetIpErrorResponse(response, b)
        }
        else {
            try {
                var url: String = response.getString("url")
                var ip: String = response.optString("ip")
                var port: String = response.optString("port")
                Log.e(TAG, (ip + port))
                startSocketService(b, url)
                Log.e(TAG, ("onPostGetIpResponse:SUCCESS - " + response))
            }
            catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, e.getMessage())
            }
        }
    }
    override fun onPostGetIpErrorResponse(response: JSONObject, b: Boolean) {
        Log.e(TAG, ("onPostGetIpErrorResponse:FAIL - " + response))
        startSocketService(b, MainApplicationSingleton.API_HOST)
        Log.e(TAG, ("onPostGetIpResponse:FAIL fallback to - " + MainApplicationSingleton.API_HOST))
        var status: Int = 0
        if ((response != null)) {
            status = response.optInt("status")
        }
        if ((1 == status)) {
            MainApplicationSingleton.forceLogout(this)
        }
    }
    private fun startSocketService(b: Boolean, socketURL: String) {
        try {
            var userForService: ContactItem = userCloneForService
            startsRcvDocService(b, socketURL, userForService)
            startsEmailService(b, socketURL, userForService)
            startsChatService(b, socketURL, userForService)
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }
    private fun startsChatService(b: Boolean, socketURL: String, userForService: ContactItem) {
        var intent: Intent = Intent(this, ChatService::class.java)
        intent.putExtra("url", socketURL)
        intent.putExtra("reconnect", b)
        intent.putExtra(ContactItem.USER_CONTACT, (userForService as Parcelable))
        startService(intent)
    }
    private fun startsEmailService(b: Boolean, socketURL: String, userForService: ContactItem) {
        var intent: Intent = Intent(this, EmailService::class.java)
        intent.putExtra("url", socketURL)
        intent.putExtra("reconnect", b)
        intent.putExtra(ContactItem.USER_CONTACT, (userForService as Parcelable))
        startService(intent)
    }
    private fun startsRcvDocService(b: Boolean, socketURL: String, userForService: ContactItem) {
        var intent: Intent = Intent(this, RcvDocService::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (userForService as Parcelable))
        intent.putExtra("url", socketURL)
        intent.putExtra("reconnect", b)
        startService(intent)
    }
    private fun startContactService() {
        try {
            ContactService.startContactService(userCloneForService, this)
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }
    private fun stopContactService() {
        ContactService.stopContactService(this)
    }
    private fun startInstanceIdListenerService() {
        try {
            GCMInstanceIDListenerService.startInstanceIdListenerService(userCloneForService, this)
            GCMInstanceIDListenerService.updateToken(user, this)
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }
    @Throws(CloneNotSupportedException::class)
    private fun getUserCloneForService(): ContactItem {
        return UserContentProvider.getUserCloneForService(user)
    }
    private fun startMainSettingsActivity() {
        startActivity(newMainSettingsIntent())
    }
    private fun startHelpActivity() {
        startActivity(newIntroScreenIntent())
    }
    private fun newEmailAccountListIntent(): Intent {
        try {
            var intent: Intent = Intent(this, EmailAccountListActivity::class.java)
            intent.putExtra(ContactItem.USER_CONTACT, (UserContentProvider.getUserCloneForService(user) as Parcelable))
            return intent
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return null
    }
    private fun newCompanyListIntent(): Intent {
        try {
            var intent: Intent = Intent(this, CompanyListActivity::class.java)
            intent.putExtra(ContactItem.USER_CONTACT, (UserContentProvider.getUserCloneForService(user) as Parcelable))
            return intent
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return null
    }
    private fun startAddEmailsActivity() {
        var intent: Intent = Intent(this, AddEmailsActivity::class.java)
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (UserContentProvider.getUserCloneForService(user) as Parcelable))
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAILS_RQ_CODE)
    }
    private fun startAddEmailActivity() {
        var intent: Intent = Intent(this, AddEmailActivity::class.java)
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (UserContentProvider.getUserCloneForService(user) as Parcelable))
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE)
    }
    fun startEmailListActivity() {
        startActivity(newEmailAccountListIntent())
    }
    private fun newMainSettingsIntent(): Intent {
        var intent: Intent = Intent(this, MainSettingsActivity::class.java)
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (userCloneForService as Parcelable))
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return intent
    }
    private fun newIntroScreenIntent(): Intent {
        var intent: Intent = Intent(this, IntroScreenActivity::class.java)
        try {
            intent.putExtra(ContactItem.USER_CONTACT, (userCloneForService as Parcelable))
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return intent
    }
    fun onNewMenuDetailClicked() {

    }
    override fun onNewEmail(newBottomDialogFragment: NewBottomDialogFragment) {
        if (TextUtils.isEmpty(user.email)) {
            UserEmailContentProvider.populateUserEmailsJoinById(user, this)
        }
        onNewDialogClose(newBottomDialogFragment)
        startComposeEmailActivity(MessageItem(), user)
    }
    override fun onNewDialogClose(newBottomDialogFragment: NewBottomDialogFragment) {
        newBottomDialogFragment.dismiss()
    }
    override fun onNewGroup(newBottomDialogFragment: NewBottomDialogFragment) {
        onNewDialogClose(newBottomDialogFragment)
        var contactItem: ContactItem = ContactItem()
        contactItem.newGroup = true
        contactItem.group = true
        startMsgChatGrpContactsDetailActivity(contactItem)
    }
    override fun onNewChat(newBottomDialogFragment: NewBottomDialogFragment) {
        onNewDialogClose(newBottomDialogFragment)
        var contactItem: ContactItem = ContactItem()
        contactItem.newGroup = false
        contactItem.group = false
        startContactSelectActivity(contactItem)
    }
    fun showNewBottomDialogFragment() {
        var newBottomDialogFragment: NewBottomDialogFragment = NewBottomDialogFragment()
        newBottomDialogFragment.newBottomDialogListener = this
        newBottomDialogFragment.show(getSupportFragmentManager(), "Create")
    }
    fun onNewFeedsMenuClicked() {
        startComposeFeedActivity(MessageItem(), user)
    }
    fun onNewMainboxMenuClicked() {
        showNewBottomDialogFragment()
    }
    fun onNewMenuClicked() {
        onNewMainboxMenuClicked()
    }
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, ("onTrimMemory: " + level))
        if (isMainTab()) {
            return
        }
        if (isContactsTab()) {
            return
        }
        if (isProfileTab()) {
            return
        }
    }
    fun onMessageForward(intent: Intent) {
        var drawer: DrawerLayout = (findViewById(R.id.drawer_layout) as DrawerLayout)
        if ((drawer != null)) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            }
            else {
                onMainboxMessageForward(intent)
            }
        }
    }
    fun onMainboxMessageForward(intent: Intent) {
        var position: Int = 1
        var tab: TabLayout.Tab = null
        if (((tab != null) && (R.id.mi_people == (tab.getTag() as Int)))) {
            var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
            msgsGrpPeopleFragment.onMessageForward(intent)
            return
        }
        if (((tab != null) && (R.id.mi_clutter == (tab.getTag() as Int)))) {
            var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
            msgsGrpClutterFragment.onMessageForward(intent)
            return
        }
        if (((tab != null) && (R.id.mi_chat == (tab.getTag() as Int)))) {
            return
        }
    }
    private fun messageToNest(intent: Intent) {

    }
    private fun messageToDraft(intent: Intent) {
        var intent2: Intent = Intent(this, MsgsGrpDraftActivity::class.java)
        try {
            intent2.putExtra(ContactItem.USER_CONTACT, (userCloneForService as Parcelable))
        }
        catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        intent2.putExtras(intent)
        startActivityForResult(intent2, MainApplicationSingleton.ACTIVITY_MSGSGRPDRAFT_RQ_CODE)
    }
    private fun startContactSelectActivity(contactItem: ContactItem) {
        var intent: Intent = Intent(this, ContactSelectActivity::class.java)
        intent.putExtra(ContactItem.TAG, (contactItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE)
    }
    private fun startComposeEmailActivity(messageItem: MessageItem, user: ContactItem) {
        var intent: Intent = Intent(this, ComposeEmailActivity::class.java)
        intent.putExtra(MessageItem.TAG, (messageItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_COMPOSEEMAIL_RQ_CODE)
    }
    private fun startComposeFeedActivity(messageItem: MessageItem, user: ContactItem) {

    }
    fun startMsgChatGrpContactsDetailActivity(contactItem: ContactItem) {
        var intent: Intent = Intent(this, MsgChatGrpContactsDetailActivity::class.java)
        intent.putExtra(ContactItem.TAG, (contactItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE)
    }
    private fun startCompanyCreateActivity(user: ContactItem) {
        var intent: Intent = Intent(this, CompanyCreateActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_CREATE_RQ_CODE)
    }
    private fun startCompanyListActivity(user: ContactItem) {
        var intent: Intent = Intent(this, CompanyListActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_LIST_RQ_CODE)
    }
    private fun startGetInvitesActivity(user: ContactItem) {
        var intent: Intent = Intent(this, GetInvitesActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_GETINVITES_RQ_CODE)
    }
    private fun startInviteUsersActivity(user: ContactItem) {
        var intent: Intent = Intent(this, InviteUsersActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_INVITE_USERS_RQ_CODE)
    }
    private fun startContactDetailActivity(contactItem: ContactItem, user: ContactItem) {
        var intent: Intent = Intent(this, ContactDetailActivity::class.java)
        intent.putExtra(ContactItem.TAG, (contactItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_CONTACT_DETAIL_RQ_CODE)
    }
    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode)) {
            if ((Activity.RESULT_OK == resultCode)) {
                if ((data != null)) {
                    var item: ContactItem = data.getParcelableExtra(ContactItem.USER_CONTACT)
                    var emails: HashSet<ContactItem> = item.contactItems
                    if (((emails != null) && !emails.empty)) {
                        for (email in emails) {
                            user.addEmail(email)
                        }
                        addEmailsNavMenuFromUser(user)
                    }
                }
            }
            else {
                if ((Activity.RESULT_CANCELED == resultCode)) {
                    Log.e(TAG, "onActivityResult: Add emails cancelled ")
                }
            }
        }
        if ((MainApplicationSingleton.ACT_COMPANY_INVITE_USERS_RQ_CODE == requestCode)) {
            if ((Activity.RESULT_OK == resultCode)) {
                if ((data != null)) {
                    var contactItem: ContactItem = data.getParcelableExtra(ContactItem.TAG)
                    contactItem.mergeSelectedContacts()
                    var contactItems: HashSet<ContactItem> = contactItem.contactItems
                    var count: Int = contactItems.size()
                    if ((0 == count)) {
                        Log.e(TAG, ("onActivityResult: Please select 1 contact - " + count))
                    }
                    else {
                        var emails: JSONArray = JSONArray()
                        for (device in contactItems) {
                            var email: JSONArray = device.emails
                            if (((email != null) && (email.length() > 0))) {
                                var i: Int = 0
                                while ((i < email.length())) {
                                    try {
                                        emails.put(email.getString(i))
                                    }
                                    catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                    i++
                                }
                            }
                        }
                        if ((emails.length() > 0)) {
                            execInviteUsersTask(emails.toString(), user.companyId, user)
                        }
                        Log.d(TAG, ("onActivityResult: " + emails))
                    }
                }
            }
        }
        if ((MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE == requestCode)) {
            if ((Activity.RESULT_OK == resultCode)) {
                if ((data != null)) {
                    var contactItem: ContactItem = data.getParcelableExtra(ContactItem.TAG)
                    contactItem.mergeSelectedContacts()
                    var count: Int = contactItem.contactItems
                    if ((1 == count)) {
                        var selectedContactItemForChat: ContactItem = contactItem.contactItems
                        contactItem.dataId = selectedContactItemForChat.intellibitzId
                        contactItem.intellibitzId = selectedContactItemForChat.intellibitzId
                        contactItem.typeId = selectedContactItemForChat.typeId
                        contactItem.deviceContactId = selectedContactItemForChat.deviceContactId
                        contactItem.name = selectedContactItemForChat.name
                        contactItem.group = false
                        contactItem.emailItem = false
                        contactItem.type = "USER"
                        createNewChat(contactItem, user)
                    }
                    else {
                        Log.e(TAG, ("onActivityResult: Please select 1 contact - " + count))
                    }
                }
            }
            else {
                if ((Activity.RESULT_CANCELED == resultCode)) {
                    Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
                }
            }
        }
        else {
            if ((MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE == requestCode)) {
                if ((Activity.RESULT_OK == resultCode)) {
                    if ((data != null)) {
                        var contactItem: ContactItem = data.getParcelableExtra(ContactItem.TAG)
                        if ((contactItem != null)) {
                            createNewChat(contactItem, user)
                        }
                    }
                }
                else {
                    if ((Activity.RESULT_CANCELED == resultCode)) {
                        Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
                    }
                }
            }
        }
    }
    override fun onPostInviteUsersResponse(response: JSONObject, companyName: String, user: ContactItem) {
        var status: Int = response.optInt("status", 1)
        if ((1 == status)) {
            Log.d(TAG, ("onPostInviteUsersResponse: " + response))
        }
        else {
            if ((99 == status)) {
                onPostInviteUsersErrorResponse(response)
            }
            else {
                if ((1 == status)) {
                    onPostInviteUsersErrorResponse(response)
                }
            }
        }
    }
    override fun onPostInviteUsersErrorResponse(response: JSONObject) {
        Log.d(TAG, ("onPostInviteUsersErrorResponse: " + response))
    }
    private fun createNewChat(contactItem: ContactItem, user: ContactItem) {
        var item: MessageItem = processNewChatMessage(contactItem)
        showMessageThreadMessage(item, user)
    }
    private fun showMessageThreadMessage(messageItem: MessageItem, user: ContactItem) {
        var id: String = messageItem.dataId
        if ((((id != null) && !id.empty) && (MessageItem.TAG == id))) {
            if (messageItem.emailItem) {
                MessageEmailContentProvider.queryMessageEmailThreadFullJoin(messageItem, this)
            }
            else {
                if (messageItem.group) {
                    MessageChatGroupContentProvider.queryMessageChatThreadFullJoin(messageItem, this)
                }
                else {
                    MessageChatContentProvider.queryMessageChatThreadFullJoin(messageItem, this)
                }
            }
        }
        onPeopleTopicClicked(messageItem, user)
    }
    private fun processNewChatMessage(contactItem: ContactItem): MessageItem {
        var id: String = contactItem.dataId
        if ((id != null)) {
            Log.d(TAG, ("User ready for Chat: " + id))
            var item: MessageItem = null
            item = MessageItem()
            item.dataId = MessageItem.TAG
            item.docOwner = user.dataId
            item.name = contactItem.name
            MessageChatContentProvider.createMessageChatThread(id, item)
            if ((null == item.name)) {
                item.name = item.dataId
            }
            item.contactItem = contactItem
            return item
        }
        return null
    }
    fun onOkPressed(intent: Intent) {
        var drawer: DrawerLayout = (findViewById(R.id.drawer_layout) as DrawerLayout)
        if ((drawer != null)) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            }
            else {
                onMainboxOkPressed(intent)
            }
        }
    }
    fun onMainboxOkPressed(intent: Intent) {
        if (isPeopleTab()) {
            var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
            msgsGrpPeopleFragment.onOkPressed(intent)
            return
        }
        if (isInboxTab()) {
            return
        }
    }
    override fun onBackPressed() {
        var drawer: DrawerLayout = (findViewById(R.id.drawer_layout) as DrawerLayout)
        if ((drawer != null)) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            }
            else {
                super.onBackPressed()
            }
        }
    }
    fun nullSharedIntent() {
        sharedIntent = null
    }
    fun getSharedIntent(): Intent {
        return sharedIntent
    }
    fun setSharedIntent(sharedIntent: Intent) {
        this = sharedIntent
        var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
        if ((intellibitzContactsFragment != null)) {
            intellibitzContactsFragment.sharedIntent = sharedIntent
        }
    }
    private fun handleSharedIntent(): Boolean {
        sharedIntent = getIntent()
        var action: String = sharedIntent.getAction()
        var type: String = sharedIntent.getType()
        if (((Intent.ACTION_SEND == action) && (type != null))) {
            if ("text/plain") {
                handleSendText(sharedIntent)
                return true
            }
            else {
                if ((((type.startsWith("image/") || type.startsWith("video/")) || type.startsWith("audio/")) || type.startsWith("application/"))) {
                    handleSendImage(sharedIntent)
                    return true
                }
            }
        }
        else {
            if (((Intent.ACTION_SEND_MULTIPLE == action) && (type != null))) {
                if ((((type.startsWith("image/") || type.startsWith("video/")) || type.startsWith("audio/")) || type.startsWith("application/"))) {
                    handleSendMultipleImages(sharedIntent)
                    return true
                }
            }
            else {
                return false
            }
        }
        return false
    }
    fun handleSendText(shared: Intent) {
        var sharedText: String = shared.getStringExtra(Intent.EXTRA_TEXT)
        if ((sharedText != null)) {

        }
    }
    fun handleSendImage(shared: Intent) {
        var imageUri: Uri = shared.getParcelableExtra(Intent.EXTRA_STREAM)
        if ((imageUri != null)) {

        }
    }
    fun handleSendMultipleImages(shared: Intent) {
        var imageUris: ArrayList<Uri> = shared.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        if ((imageUris != null)) {

        }
    }
    fun getToolbar(): Toolbar {
        return toolbar
    }
    fun getDetailToolbar(): Toolbar {
        return null
    }
    fun getFilterToolbar(): Toolbar {
        return null
    }
    fun getAppBarLayout(): AppBarLayout {
        return appBarLayout
    }
    fun getTabLayoutFilter(): TabLayout {
        return null
    }
    fun getRootView(): View {
        return drawerLayout
    }
    fun emptyOnClick(view: View) {
        startAddEmailsActivity()
    }
    override fun onViewModeChanged() {

    }
    override fun onViewModeItem() {

    }
    fun setupMainboxToolbars() {
        setupMainboxToolbar()
    }
    private fun clearMainboxFilters() {

    }
    private fun setupClutterFilter() {
        upMainboxOverflowFilter = R.menu.menu_clutter_overflow
        showOverFlowFilter()
    }
    private fun setupPeopleFilter() {
        upMainboxOverflowFilter = R.menu.menu_people_overflow
        showOverFlowFilter()
    }
    private fun setupMainboxOverflowFilter(menuResource: Int) {
        var menu: Menu
        var size: Int
    }
    private fun newTabFromMainboxMenuItem(item: MenuItem): TabLayout.Tab {
        var tab: TabLayout.Tab = null
        return tab
    }
    private fun toggleTab(tab: TabLayout.Tab) {
        var view: View = tab.customView
        if ((view != null)) {
            val button: ImageButton = (view.findViewById(R.id.tg_tab) as ImageButton)
            button.selected = !button.selected
        }
    }
    private fun getMainboxTabViewButton(tab: TabLayout.Tab): ImageButton {
        if ((null == tab)) {
            return null
        }
        var view: View = tab.customView
        if ((null == view)) {
            return null
        }
        var button: ImageButton = (view.findViewById(R.id.tg_tab) as ImageButton)
        return button
    }
    private fun isMainboxTabViewButtonSelected(tab: TabLayout.Tab): Boolean {
        var button: ImageButton = getMainboxTabViewButton(tab)
        if ((null == button)) {
            return false
        }
        return button.selected
    }
    private fun isChatSelected(): Boolean {
        return false
    }
    private fun isGroupSelected(): Boolean {
        return false
    }
    private fun isEmailSelected(): Boolean {
        return false
    }
    private fun isFilesSelected(): Boolean {
        return false
    }
    private fun isFlaggedSelected(): Boolean {
        return false
    }
    private fun isUnreadSelected(): Boolean {
        return false
    }
    private fun newOverflowTabFromMainboxMenuItem(item: MenuItem): TabLayout.Tab {
        return null
    }
    fun onMenuItemClick_(item: MenuItem): Boolean {
        if ((R.id.mi_clutter == item.getItemId())) {
            showMsgsGrpClutterFragment()
            return true
        }
        if ((R.id.mi_people == item.getItemId())) {
            showMsgsGrpPeopleFragment()
            return true
        }
        if ((R.id.mi_chat == item.getItemId())) {
            onMenuChatMessages(item)
            return true
        }
        if ((R.id.mi_group == item.getItemId())) {
            showGroupMessages()
            return true
        }
        if ((R.id.mi_email == item.getItemId())) {
            showPeopleEmailMessages()
            return true
        }
        if ((R.id.mi_files == item.getItemId())) {
            showFilesMessages()
            return true
        }
        return false
    }
    fun getSelectedOverflowTab(): TabLayout.Tab {
        return null
    }
    private fun isChatTab(): Boolean {
        var tab: TabLayout.Tab = selectedOverflowTab
        return isChatTab(tab)
    }
    private fun isChatTab(tab: TabLayout.Tab): Boolean {
        return (((null != tab) && (null != tab.getTag())) && (R.id.mi_chat == (tab.getTag() as Int)))
    }
    private fun isGroupTab(): Boolean {
        var tab: TabLayout.Tab = selectedOverflowTab
        return isGroupTab(tab)
    }
    private fun isGroupTab(tab: TabLayout.Tab): Boolean {
        return (((null != tab) && (null != tab.getTag())) && (R.id.mi_group == (tab.getTag() as Int)))
    }
    private fun isEmailTab(): Boolean {
        var tab: TabLayout.Tab = selectedOverflowTab
        return isEmailTab(tab)
    }
    private fun isEmailTab(tab: TabLayout.Tab): Boolean {
        return (((null != tab) && (null != tab.getTag())) && (R.id.mi_email == (tab.getTag() as Int)))
    }
    private fun isFlaggedTab(): Boolean {
        var tab: TabLayout.Tab = selectedOverflowTab
        return isFlaggedTab(tab)
    }
    private fun isFlaggedTab(tab: TabLayout.Tab): Boolean {
        return (((null != tab) && (null != tab.getTag())) && (R.id.mi_flagged == (tab.getTag() as Int)))
    }
    private fun isUnreadTab(): Boolean {
        var tab: TabLayout.Tab = selectedOverflowTab
        return isUnreadTab(tab)
    }
    private fun isUnreadTab(tab: TabLayout.Tab): Boolean {
        return (((null != tab) && (null != tab.getTag())) && (R.id.mi_unread == (tab.getTag() as Int)))
    }
    private fun isChatTab(view: View): Boolean {
        return (((null != view) && (null != view.getTag())) && (R.id.mi_chat == (view.getTag() as Int)))
    }
    private fun isGroupTab(view: View): Boolean {
        return (((null != view) && (null != view.getTag())) && (R.id.mi_group == (view.getTag() as Int)))
    }
    private fun isEmailTab(view: View): Boolean {
        return (((null != view) && (null != view.getTag())) && (R.id.mi_email == (view.getTag() as Int)))
    }
    private fun isFilesTab(view: View): Boolean {
        return (((null != view) && (null != view.getTag())) && (R.id.mi_files == (view.getTag() as Int)))
    }
    private fun isFlaggedTab(view: View): Boolean {
        return (((null != view) && (null != view.getTag())) && (R.id.mi_flagged == (view.getTag() as Int)))
    }
    private fun isUnreadTab(view: View): Boolean {
        return (((null != view) && (null != view.getTag())) && (R.id.mi_unread == (view.getTag() as Int)))
    }
    override fun onClick(view: View) {
        var position: Int = 1
        if ((0 == position)) {
            showAllContactsFragment()
        }
        else {
            if ((1 == position)) {
                showWorkContactsFragment()
            }
            else {
                if ((2 == position)) {

                }
            }
        }
        var id: Int = 1
        var val: Integer = (view.getTag() as Integer)
        if ((val != null)) {
            id = val
        }
        var people: Boolean = false
        var clutter: Boolean = false
        var chat: Boolean = false
        var group: Boolean = false
        var email: Boolean = false
        var files: Boolean = false
        var flagged: Boolean = false
        var unread: Boolean = false
        if (isChatTab(view)) {
            if (((id != 1) && view.selected)) {

            }
            else {
                chat = true
            }
        }
        if (isGroupTab(view)) {
            if (((id != 1) && view.selected)) {

            }
            else {
                group = true
            }
        }
        if (isEmailTab(view)) {
            if (((id != 1) && view.selected)) {

            }
            else {
                email = true
            }
        }
        if (isFilesTab(view)) {
            if (((id != 1) && view.selected)) {

            }
            else {
                files = true
            }
        }
        if (isFlaggedTab(view)) {
            if (((id != 1) && view.selected)) {

            }
            else {
                flagged = true
            }
        }
        if (isUnreadTab(view)) {
            if (((id != 1) && view.selected)) {

            }
            else {
                unread = true
            }
        }
        people = isPeopleTab()
        clutter = isInboxTab()
        chat = isChatSelected()
        group = isGroupSelected()
        email = isEmailSelected()
        files = isFilesSelected()
        flagged = isFlaggedSelected()
        unread = isUnreadSelected()
        onTabSelected(people, clutter, chat, group, email, files, flagged, unread)
    }
    fun onTabSelected(people: Boolean, clutter: Boolean, chat: Boolean, group: Boolean, email: Boolean, files: Boolean, flagged: Boolean, unread: Boolean) {
        if (people) {
            if ((((chat && group) && email) && files)) {
                showMsgsGrpPeopleFragment()
                return
            }
            if (((chat && group) && email)) {
                showPeopleMessagesMinusFiles()
                return
            }
            if ((chat && group)) {
                showMsgsGrpPeopleChatsFragment()
                return
            }
            if (chat) {
                showMsgsGrpPeopleChatsFragment()
                return
            }
            if (((group && email) && files)) {
                showPeopleMessagesMinusChat()
                return
            }
        }
        if (clutter) {
            if ((flagged && unread)) {
                showMsgsGrpClutterFragment()
            }
        }
    }
    private fun resetOverflowTabs() {

    }
    private fun onMenuChatMessages(item: MenuItem) {
        toggleOverflowMenu(item)
        showMsgsGrpPeopleChatsFragment()
    }
    private fun onMenuGroupMessages(item: MenuItem) {
        toggleOverflowMenu(item)
        showGroupMessages()
    }
    private fun onMenuEmailMessages(item: MenuItem) {
        toggleOverflowMenu(item)
        showPeopleEmailMessages()
    }
    private fun onMenuFilesMessages(item: MenuItem) {
        toggleOverflowMenu(item)
        showFilesMessages()
    }
    private fun getTabView(id: Int): View {
        return null
    }
    private fun toggleOverflowMenu(item: MenuItem) {

    }
    private fun showPeopleMessagesMinusChat() {
        showMainToolbar()
        var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = newMsgsGrpPeopleFragment()
        replaceContentFragment(msgsGrpPeopleFragment, MsgsGrpPeopleFragment.TAG)
        msgsGrpPeopleFragment.minusChat()
        msgsGrpPeopleFragment.restartFilterLoaders()
    }
    private fun showPeopleMessagesMinusFiles() {
        showMainToolbar()
        var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = newMsgsGrpPeopleFragment()
        replaceContentFragment(msgsGrpPeopleFragment, MsgsGrpPeopleFragment.TAG)
        msgsGrpPeopleFragment.minusChat()
        msgsGrpPeopleFragment.restartFilterLoaders()
    }
    private fun showGroupMessages() {
        showMsgsGrpPeopleChatsFragment()
    }
    private fun showPeopleEmailMessages() {
        showMsgsGrpPeopleChatsFragment()
    }
    private fun showFilesMessages() {
        showMsgsGrpPeopleChatsFragment()
    }
    private fun showTabs() {

    }
    private fun hideTabs() {

    }
    private fun hideFilterToolbars() {
        hideOverFlowFilter()
    }
    private fun removeChatMessages() {

    }
    private fun removeClutterBoxMessages() {
        hideOverFlowFilter()
    }
    private fun removePeopleMessages() {
        hideOverFlowFilter()
    }
    private fun setupSnackBar() {
        snackbar = Snackbar.make(rootView, "Please Add Account to see Emails", Snackbar.LENGTH_LONG)
        snackbar.setAction("ADD EMAIL", object : View.OnClickListener() {    override fun onClick(v: View) {
        startEmailListActivity()
    }
})
    }
    fun showNewMessageDialog() {
        NewEmailDialogFragment.newMessageDialog(this, 0, user)
    }
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if ((dialog is NewEmailDialogFragment)) {
            performNewEmailOk((dialog as NewEmailDialogFragment))
        }
        else {
            if ((dialog is EditGroupDialogFragment)) {
                performEditGroupOk((dialog as EditGroupDialogFragment))
            }
        }
    }
    private fun execUpdateGroupTask(contactItem: ContactItem, messageItem: MessageItem) {
        updateGroupTask = UpdateGroupTask(contactItem, messageItem, user.dataId, user.token, user.device, user.deviceRef, MainApplicationSingleton.AUTH_UPDATE_GROUP)
        updateGroupTask.updateGroupTaskListener = this
        updateGroupTask.execute()
    }
    private fun execInviteUsersTask(inviteEmails: String, companyId: String, user: ContactItem) {
        var inviteUsersTask: InviteUsersTask = InviteUsersTask(inviteEmails, companyId, user.dataId, user.token, user.device, user.deviceRef, user, MainApplicationSingleton.AUTH_COMPANY_INVITE_USERS, this)
        inviteUsersTask.requestTimeoutMillis = 30000
        inviteUsersTask.inviteUsersTaskListener = this
        inviteUsersTask.execute()
    }
    fun performEditGroupOk(editGroupDialogFragment: EditGroupDialogFragment) {
        Log.d(TAG, "performEditGroupOk")
        var messageItem: MessageItem = editGroupDialogFragment.messageItem
        if ((null == messageItem)) {
            return
        }
        var contactItem: ContactItem = messageItem.contactItem
        if ((null == contactItem)) {
            return
        }
        var name: String = editGroupDialogFragment.subject
        if (TextUtils.isEmpty(name)) {
            return
        }
        messageItem.name = name
        contactItem.name = name
        execUpdateGroupTask(contactItem, messageItem)
    }
    fun performNewEmailOk(newEmailDialogFragment: NewEmailDialogFragment) {
        notifyUserBaseItemListeners()
        var messageItem: MessageItem = MessageItem()
        messageItem.dataId = MessageItem.TAG
        messageItem.baseType = "THREAD"
        messageItem.docType = "THREAD"
        messageItem.dataRev = "1"
        messageItem.from = user.name
        messageItem.subject = newEmailDialogFragment.subject
        var to: Array<Rfc822Token> = newEmailDialogFragment.to
        var cc: Array<Rfc822Token> = newEmailDialogFragment.cc
        var bcc: Array<Rfc822Token> = newEmailDialogFragment.bcc
        MessageItem.setMessageThreadEmailAddress(messageItem, to, cc, bcc)
        messageItem.docOwner = user.docOwner
        messageItem.docSender = user.name
        messageItem.docOwnerEmail = user.email
        messageItem.docSenderEmail = user.email
        messageItem.timestamp = System.currentTimeMillis()
        var intent: Intent = Intent()
        if (newEmailDialogFragment.isChatMode()) {
            intent.action = MainApplicationSingleton.BROADCAST_NEW_CHAT_DIALOG_OK
        }
        else {
            intent.action = MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK
        }
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        intent.putExtra(MessageItem.TAG, (messageItem as Parcelable))
        LocalBroadcastManager.getInstance(this)
    }
    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }
    fun setMessageItem(messageItem: MessageItem) {
        this = messageItem
    }
    fun getCount(): Int {
        return 3
    }
    override fun onQueryTextSubmit(query: String): Boolean {
        if (isInboxTab()) {
            var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
            msgsGrpClutterFragment.onQueryTextSubmit(query)
        }
        else {
            if (isPeopleTab()) {
                var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                msgsGrpPeopleFragment.onQueryTextSubmit(query)
            }
        }
        hideTabs()
        return true
    }
    override fun onQueryTextChange(newText: String): Boolean {
        if (isInboxTab()) {
            var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
            msgsGrpClutterFragment.onQueryTextChange(newText)
        }
        else {
            if (isPeopleTab()) {
                var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                msgsGrpPeopleFragment.onQueryTextChange(newText)
            }
        }
        hideTabs()
        return true
    }
    override fun onClose(): Boolean {
        if (isInboxTab()) {
            var msgsGrpClutterFragment: MsgsGrpClutterFragment = (getSupportFragmentManager() as MsgsGrpClutterFragment)
            msgsGrpClutterFragment.onClose()
            showTabs()
            return true
        }
        else {
            if (isPeopleTab()) {
                var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                msgsGrpPeopleFragment.onClose()
                showTabs()
                return true
            }
        }
        if (isAllContactsTab()) {
            var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
            deviceContactsFragment.onClose()
        }
        else {
            if (isWorkContactsTab()) {
                var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
                intellibitzContactsFragment.onClose()
            }
        }
        showContactsTabs()
        return true
    }
    fun onDetailQueryTextSubmit(query: String): Boolean {
        if (isInboxTab()) {

        }
        else {
            if (isPeopleTab()) {
                var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                msgsGrpPeopleFragment.onDetailQueryTextSubmit(query)
                msgsGrpPeopleFragment.onQueryTextSubmit(query)
            }
        }
        return true
    }
    fun onDetailQueryTextChange(newText: String): Boolean {
        if (isInboxTab()) {

        }
        else {
            if (isPeopleTab()) {
                var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                msgsGrpPeopleFragment.onDetailQueryTextChange(newText)
            }
        }
        return true
    }
    fun onDetailClose(): Boolean {
        if (isInboxTab()) {

        }
        else {
            if (isPeopleTab()) {
                var msgsGrpPeopleFragment: MsgsGrpPeopleFragment = (getSupportFragmentManager() as MsgsGrpPeopleFragment)
                msgsGrpPeopleFragment.onDetailClose()
            }
        }
        return true
    }
    override fun onPeopleTopicClicked(messageItem: MessageItem, user: ContactItem) {
        val type: String = messageItem.getType()
        if ("CHAT") {
            if ("USER") {
                onPeopleMessageClicked(messageItem, user)
            }
            else {
                onChatGroupClicked(messageItem, user)
            }
        }
        else {
            if ("EMAIL") {
                var did: Long = messageItem.deviceContactId
                if ((did > 0)) {
                    var cursor: Cursor = getContentResolver()
                    if (((cursor != null) && (cursor.getCount() > 0))) {
                        var id: String = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_INTELLIBITZ_ID))
                        cursor.close()
                        if (TextUtils.isEmpty(id)) {
                            messageItem.intellibitzId = messageItem.docSenderEmail
                            onClutterTopicClicked(messageItem, user)
                        }
                        else {
                            cursor = getContentResolver()
                            if (((cursor != null) && (cursor.getCount() > 0))) {
                                var chatMessage: MessageItem = MessageChatContentProvider.fillsMessageItemFromCursor(cursor)
                                cursor.close()
                                if ((null == chatMessage)) {
                                    messageItem.intellibitzId = messageItem.docSenderEmail
                                    onClutterTopicClicked(messageItem, user)
                                }
                                else {
                                    onPeopleMessageClicked(chatMessage, user)
                                }
                            }
                            onClutterTopicClicked(messageItem, user)
                            if ((cursor != null)) {
                                cursor.close()
                            }
                        }
                    }
                    else {
                        onClutterTopicClicked(messageItem, user)
                        if ((cursor != null)) {
                            cursor.close()
                        }
                    }
                }
                else {
                    messageItem.intellibitzId = messageItem.docSenderEmail
                    onClutterTopicClicked(messageItem, user)
                }
            }
            else {
                onPeopleMessageClicked(messageItem, user)
            }
        }
    }
    override fun onClutterTopicClicked(messageItem: MessageItem, user: ContactItem) {
        if ((MessageItem.TAG.equals(other = messageItem.dataId, ignoreCase = true))) {
            startClutterEmailActivity(messageItem, user)
        }
        else {
            startClutterEmailsActivity(messageItem, user)
        }
    }
    fun onPeopleMessageClicked(messageItem: MessageItem, user: ContactItem) {
        startPeopleDetailActivity(messageItem, user)
    }
    fun onChatGroupClicked(messageItem: MessageItem, user: ContactItem) {
        startMessageChatGroupActivity(messageItem, user)
    }
    fun startProfileActivity(user: ContactItem) {
        nullSharedIntent()
        var intent: Intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROILE_RQ_CODE)
    }
    fun startMessageChatGroupActivity(messageItem: MessageItem, user: ContactItem) {
        nullSharedIntent()
        var intent: Intent = Intent(this, MessageChatGroupActivity::class.java)
        intent.putExtra(MessageItem.TAG, (messageItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MESSAGECHATGROUP_RQ_CODE)
    }
    fun startPeopleDetailActivity(messageItem: MessageItem, user: ContactItem) {
        nullSharedIntent()
        var intent: Intent = Intent(this, PeopleDetailActivity::class.java)
        intent.putExtra(MessageItem.TAG, (messageItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PEOPLEDETAIL_RQ_CODE)
    }
    fun startClutterEmailsActivity(messageItem: MessageItem, user: ContactItem) {
        nullSharedIntent()
        var intent: Intent = Intent(this, ClutterEmailsActivity::class.java)
        intent.putExtra(MessageItem.EMAIL_MESSAGE, (messageItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CLUTTEREMAILS_RQ_CODE)
    }
    fun startClutterEmailActivity(messageItem: MessageItem, user: ContactItem) {
        nullSharedIntent()
        var intent: Intent = Intent(this, ClutterEmailActivity::class.java)
        intent.putExtra(MessageItem.EMAIL_MESSAGE, (messageItem as Parcelable))
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CLUTTEREMAIL_RQ_CODE)
    }
    fun startScheduleActivity(user: ContactItem) {
        nullSharedIntent()
    }
    override fun onPeopleTopicsLoaded(count: Int) {
        if ((0 == count)) {
            if ((snackbar != null)) {
                snackbar.show()
            }
        }
        else {
            if ((snackbar != null)) {
                snackbar.dismiss()
            }
        }
    }
    override fun onClutterTopicsLoaded(count: Int) {
        if ((0 == count)) {
            if ((snackbar != null)) {
                snackbar.show()
            }
        }
        else {
            if ((snackbar != null)) {
                snackbar.dismiss()
            }
        }
    }
    override fun onPeopleTyping(text: String) {
        var toolbar: Toolbar = toolbar
        assert((toolbar != null))
        toolbar.subtitle = text
        toolbar.subtitleTextColor = Color.GREEN
    }
    override fun onPeopleTypingStopped(text: String) {
        var toolbar: Toolbar = toolbar
        assert((toolbar != null))
        toolbar.subtitle = text
        toolbar.subtitleTextColor = Color.WHITE
    }
    override fun onPostUpdateGroupExecute(response: JSONObject, name: String, file: File, contactItem: ContactItem, messageItem: MessageItem) {
        var status: Int = 0
        if ((response != null)) {
            status = response.optInt("status")
        }
        if ((((null == response) || (99 == status)) || (1 == status))) {
            onPostUpdateGroupExecuteFail(response, name, file, contactItem, messageItem)
        }
        else {
            var values: ContentValues = ContentValues()
            if ((name != null)) {
                values.put(MessageItemColumns.KEY_NAME, name)
                if ((file != null)) {
                    values.put(MessageItemColumns.KEY_PIC, file.absolutePath)
                }
                var id: String = messageItem.dataId
                if (!TextUtils.isEmpty(id)) {
                    getContentResolver()
                }
                id = contactItem.dataId
                if (!TextUtils.isEmpty(id)) {
                    getContentResolver()
                }
            }
            Log.d(TAG, ("onPostUpdateGroupExecute: Success -" + name))
        }
    }
    override fun onPostUpdateGroupExecuteFail(response: JSONObject, name: String, file: File, contactItem: ContactItem, messageItem: MessageItem) {
        Log.e(TAG, ("onPostUpdateGroupExecuteFail: " + response))
    }
    override fun setUpdateGroupTaskToNull() {
        updateGroupTask = null
    }
    override fun onPeopleHeaderChanged(messageItem: MessageItem) {

    }
    fun setTwoPane(mTwoPane: Boolean) {
        this = mTwoPane
    }
    private fun newDeviceContactItemFragment(): DeviceContactsFragment {
        return DeviceContactsFragment.newInstance(user, this)
    }
    private fun newIntellibitzContactItemFragment(): IntellibitzContactsFragment {
        return IntellibitzContactsFragment.newInstance(user, this)
    }
    private fun newMsgChatGrpContactsFragment() {
        groupContactsItemFragment = MsgChatGrpContactsFragment.newInstance(user, this)
    }
    private fun newBroadcastContactsFragment() {

    }
    fun setupAndPrepareContactChildFragments() {
        setupContactsToolbar()
    }
    fun setupFAB(view: View) {
        var fab: FloatingActionButton = (view.findViewById(R.id.fab) as FloatingActionButton)
        assert((fab != null))
        fab.onClickListener = object : View.OnClickListener() {    override fun onClick(view: View) {

    }
}
    }
    private fun clearFilters(): Toolbar {
        var filterToolbar: Toolbar = filterToolbar
        assert((filterToolbar != null))
        var menu: Menu = filterToolbar.menu
        menu.clear()
        return filterToolbar
    }
    private fun newTabFromContactsMenuItem(item: MenuItem): TabLayout.Tab {
        return null
    }
    private fun setupContactsDetailToolbar(): Toolbar {
        return null
    }
    private fun setupAllContactsFilter() {
        upContactsOverflowFilter = R.menu.contacts_all_overflow
        showOverFlowFilter()
    }
    private fun setupContactsOverflowFilter(menuResource: Int) {

    }
    private fun newOverflowTabFromContactsMenuItem(item: MenuItem): TabLayout.Tab {
        return null
    }
    private fun performNavigationClose() {

    }
    fun onTrimMemory_(level: Int) {
        Log.d(TAG, ("onTrimMemory: " + level))
        if (isAllContactsTab()) {
            var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
            intellibitzContactsFragment.onTrimMemory(level)
            groupContactsItemFragment.onTrimMemory(level)
            return
        }
        if (isWorkContactsTab()) {
            var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
            deviceContactsFragment.onTrimMemory(level)
            groupContactsItemFragment.onTrimMemory(level)
            return
        }
    }
    fun onBackPressed_() {
        return
    }
    fun onOkPressed_(intent: Intent) {
        if ((null == intent)) {
            onBackPressed()
            hideDetailFilters()
        }
        else {
            onContactsChildOkPressed(intent)
        }
    }
    fun onActivityResult_(requestCode: Int, resultCode: Int, data: Intent) {
        if (isAllContactsTab()) {
            var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
            deviceContactsFragment.onActivityResult(requestCode, resultCode, data)
            return
        }
        if (isWorkContactsTab()) {
            var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
            intellibitzContactsFragment.onActivityResult(requestCode, resultCode, data)
            return
        }
    }
    fun onContactsChildOkPressed(intent: Intent) {

    }
    fun onContactsNewMenuClicked() {

    }
    private fun showContactsTabs() {

    }
    private fun hideContactsTabs() {

    }
    fun onQueryTextSubmit_(query: String): Boolean {
        if (isAllContactsTab()) {
            var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
            deviceContactsFragment.onQueryTextSubmit(query)
        }
        else {
            if (isWorkContactsTab()) {
                var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
                intellibitzContactsFragment.onQueryTextSubmit(query)
            }
        }
        hideContactsTabs()
        return true
    }
    fun onQueryTextChange_(newText: String): Boolean {
        if (isAllContactsTab()) {
            var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
            deviceContactsFragment.onQueryTextChange(newText)
        }
        else {
            if (isWorkContactsTab()) {
                var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
                intellibitzContactsFragment.onQueryTextChange(newText)
            }
        }
        hideContactsTabs()
        return true
    }
    fun onClose_(): Boolean {
        if (isAllContactsTab()) {
            var deviceContactsFragment: DeviceContactsFragment = (getSupportFragmentManager() as DeviceContactsFragment)
            deviceContactsFragment.onClose()
        }
        else {
            if (isWorkContactsTab()) {
                var intellibitzContactsFragment: IntellibitzContactsFragment = (getSupportFragmentManager() as IntellibitzContactsFragment)
                intellibitzContactsFragment.onClose()
            }
        }
        showContactsTabs()
        return true
    }
    fun onTabUnselected_(tab: TabLayout.Tab) {
        if ((null == tab)) {
            return
        }
        if ((R.id.mic_all == (tab.getTag() as Int))) {
            removeAllContacts()
            return
        }
        if ((R.id.mic_work == (tab.getTag() as Int))) {
            removeFavContacts()
            return
        }
    }
    fun onTabSelected_(tab: TabLayout.Tab) {
        if ((null == tab)) {
            return
        }
        if ((R.id.mic_all == (tab.getTag() as Int))) {
            showAllContactsFragment()
            return
        }
        if ((R.id.mic_work == (tab.getTag() as Int))) {
            showWorkContactsFragment()
            return
        }
        if ((R.id.mi_tidy == (tab.getTag() as Int))) {
            showWorkContactsFragment()
            return
        }
    }
    private fun showGroupsContacts() {
        hideDetailToolbar()
        hideDetailFilters()
        showMainToolbar()
        if ((null == groupContactsItemFragment)) {
            newMsgChatGrpContactsFragment()
        }
        replaceContentFragment(groupContactsItemFragment, MsgChatGrpContactsFragment.TAG)
    }
    private fun showBroadcastContacts() {
        hideDetailToolbar()
        hideDetailFilters()
        showMainToolbar()
    }
    private fun removeGroupsContacts() {
        removeFragment(groupContactsItemFragment)
    }
    private fun removeBroadcastContacts() {

    }
    private fun removeFavContacts() {

    }
    private fun removeAllContacts() {
        hideOverFlowFilter()
    }
    fun onOptionsItemSelected_(item: MenuItem): Boolean {
        var id: Int = item.getItemId()
        if ((id == android.R.id.home)) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setContactHeaders(deviceContactItem: ContactItem): Toolbar {
        var toolbar: Toolbar = setupContactsDetailToolbar()
        if ((toolbar != null)) {
            toolbar.navigationIcon = R.drawable.ic_dialog_close_light
            toolbar.navigationOnClickListener = this
            var title: String = "Contacts"
            var subTitle: String = MainApplicationSingleton.INTELLIBITZ
            if ((deviceContactItem != null)) {
                title = deviceContactItem.firstName
                if (((null == title) || (0 == title.length()))) {
                    title = ("Contact " + deviceContactItem.deviceContactId)
                }
                subTitle = deviceContactItem.lastName
                if (((null == subTitle) || (0 == subTitle.length()))) {
                    subTitle = deviceContactItem.displayName
                }
            }
            toolbar.title = title
            toolbar.subtitle = subTitle
        }
        return toolbar
    }
    override fun onDeviceContactTopicClicked(item: ContactItem, user: ContactItem) {
        startContactDetailActivity(item, user)
    }
    override fun onDeviceContactTopicsLoaded(count: Int) {

    }
    override fun onContactsTopicClicked(item: ContactItem, user: ContactItem) {

    }
    override fun onContactsTopicsLoaded(count: Int) {

    }
    override fun onIntellibitzContactTopicClicked(item: ContactItem, user: ContactItem) {
        startContactDetailActivity(item, user)
    }
    override fun onIntellibitzContactTopicsLoaded(count: Int) {

    }
    fun onClick_(v: View) {

    }
    fun onMenuItemClick__(item: MenuItem): Boolean {
        if ((R.id.mi_tidy == item.getItemId())) {
            return true
        }
        return false
    }
    fun alertReadContacts(): Boolean {
        return mayRequestReadContacts(rootView)
    }
    fun alertReadStorage(): Boolean {
        return mayRequestReadExternalStorage(rootView)
    }
    fun alertWriteStorage(): Boolean {
        return mayRequestWriteExternalStorage(rootView)
    }
    class IncomingHandler : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            else -> {
                super.handleMessage(msg)
            }
        }
    }
    }
}
