package intellibitz.intellidroid.fragment

import android.app.Activity
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.os.RemoteException
import android.text.TextUtils
import android.text.util.Rfc822Token
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.ClutterEmailsActivity
import intellibitz.intellidroid.activity.ContactSelectActivity
import intellibitz.intellidroid.activity.MessageChatGroupActivity
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity
import intellibitz.intellidroid.activity.PeopleDetailActivity
import intellibitz.intellidroid.bean.BaseItemComparator
import intellibitz.intellidroid.content.IntellibitzContactContentProvider
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MessageChatGroupContentProvider
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MessagesChatContentProvider
import intellibitz.intellidroid.content.MsgsGrpPeopleContentProvider
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.content.task.GroupsSaveToDBTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.domain.account.EmailAccountListActivity
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.MessageListener
import intellibitz.intellidroid.listener.PeopleDetailListener
import intellibitz.intellidroid.listener.PeopleTopicListener
import intellibitz.intellidroid.service.ChatService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.CreateGroupTask
import intellibitz.intellidroid.task.DeleteMsgsTask
import intellibitz.intellidroid.task.GroupsAddUsersTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import intellibitz.intellidroid.widget.NewBottomDialogFragment
import intellibitz.intellidroid.widget.NewEmailDialogFragment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.HashSet
import java.util.List
import java.util.Locale
import java.util.Map

class MessagesFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    ContactListener,
    PeopleDetailListener,
    CreateGroupTask.CreateGroupTaskListener,
    GroupsAddUsersTask.GroupsAddUsersTaskListener,
    GroupsSaveToDBTask.GroupsSaveToDBTaskListener,
    NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
    LoaderManager.LoaderCallbacks<Cursor>,
    AdapterView.OnItemClickListener,
    View.OnClickListener,
    DeleteMsgsTask.DeleteMsgsTaskListener,
    NewBottomDialogFragment.NewBottomDialogListener {

    companion object {
        private const val TAG = "MessagesFrag"

        fun newInstance(
            messageItem: MessageItem?,
            user: ContactItem?,
            messageListener: MessageListener?
        ): MessagesFragment {
            val fragment = MessagesFragment()
            fragment.setUser(user)
            if (messageListener is PeopleTopicListener) {
                fragment.setPeopleTopicListener(messageListener as PeopleTopicListener)
            }
            if (messageListener is PeopleDetailListener) {
                fragment.setPeopleDetailListener(messageListener as PeopleDetailListener)
            }
            fragment.setViewModeListener(messageListener)
            val args = Bundle()
            args.putParcelable(MessageItem.TAG, messageItem)
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }

    private val mMessenger = Messenger(IncomingHandler())
    private val lock = Any()
    private var mService: Messenger? = null
    private var mIsBound = false
    private var rootView: View? = null
    private var snackbar: Snackbar? = null
    private var fab: FloatingActionButton? = null
    private var filter: String? = null
    private var messageContentObserver: ContentObserver? = null
    private var rvMessages: RecyclerView? = null
    private var rvMessagesAdapter: RecyclerViewAdapter? = null
    private var createGroupTask: CreateGroupTask? = null
    private var groupsAddUsersTask: GroupsAddUsersTask? = null
    private var groupsSaveToDBTask: GroupsSaveToDBTask? = null
    private var deleteMsgsTask: DeleteMsgsTask? = null
    private var selectedItem: MessageItem? = null
    private var sharedIntent: Intent? = null
    private var contactItem: ContactItem? = null
    private var newBottomDialogFragment: NewBottomDialogFragment? = null
    private var messageItems = ArrayList<MessageItem>()
    private var counter = 0
    private var looperThread: HandlerThread? = null
    private var isMinusChat = false
    private var messageItemToForward: MessageItem? = null
    private var peopleTopicListener: PeopleTopicListener? = null
    private var peopleDetailListener: PeopleDetailListener? = null
    private var empty: TextView? = null
    private var mActionMode: ActionMode? = null

    private val mActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.menu_context_chatty, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_delete -> {
                    deleteMessages()
                    mode.finish()
                    true
                }
                R.id.menu_moveto -> {
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
        }
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = Messenger(service)
            try {
                var msg = Message.obtain(null, ChatService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)

                msg = Message.obtain(null, ChatService.MSG_SET_VALUE, this.hashCode(), 0)
                mService?.send(msg)
            } catch (e: RemoteException) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
        }
    }

    fun setSharedIntent(sharedIntent: Intent?) {
        this.sharedIntent = sharedIntent
    }

    fun setPeopleDetailListener(peopleDetailListener: PeopleDetailListener?) {
        this.peopleDetailListener = peopleDetailListener
    }

    private fun showTyping(toUid: String, fromUid: String, fromName: String, user: ContactItem) {
        val item = MainApplicationSingleton.getBaseItem(fromUid, messageItems)
        if (item == null) {
            // Do nothing
        } else {
            if (fromUid == user.dataId) {
                // if self is typing ignore..
                // shows typing only from other senders
            } else {
                item.isTyping = true
                item.typingText = "$fromName is typing.."
                val pos = messageItems.indexOf(item)
                rvMessagesAdapter?.notifyItemChanged(pos)
                val delay = Handler()
                delay.postDelayed({
                    item.isTyping = false
                    rvMessagesAdapter?.notifyItemChanged(pos)
                    onPeopleTypingStopped("")
                }, 1000)
            }
        }
    }

    override fun onPeopleTyping(text: String) {
        peopleDetailListener?.onPeopleTyping(text)
    }

    override fun onPeopleTypingStopped(text: String) {
        peopleDetailListener?.onPeopleTypingStopped(text)
    }

    private fun doBindService() {
        appCompatActivity?.bindService(
            Intent(appCompatActivity, ChatService::class.java),
            mConnection,
            Context.BIND_AUTO_CREATE
        )
        mIsBound = true
    }

    private fun doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    val msg = Message.obtain(null, ChatService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    mService?.send(msg)
                } catch (e: RemoteException) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            appCompatActivity?.unbindService(mConnection)
            mIsBound = false
        }
    }

    fun showEmpty() {
        empty?.visibility = View.VISIBLE
        empty?.setText(R.string.empty_chat_account)
    }

    fun showEmpty(msg: String) {
        empty?.visibility = View.VISIBLE
        empty?.text = msg
    }

    fun hideEmpty() {
        empty?.visibility = View.GONE
    }

    fun getRootView(): View? {
        return rootView
    }

    fun setUser(user: ContactItem?) {
        this.user = user
    }

    fun setPeopleTopicListener(peopleTopicListener: PeopleTopicListener?) {
        this.peopleTopicListener = peopleTopicListener
    }

    private fun addUsersToGroups(id: String, name: String, contacts: Array<String>) {
        groupsAddUsersTask = GroupsAddUsersTask(
            id,
            name,
            contacts,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_GROUP_ADD_USERS
        )
        groupsAddUsersTask?.setGroupsAddUsersTaskListener(this)
        groupsAddUsersTask?.execute()
    }

    override fun setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null
    }

    private fun createNewGroupChat(contactItem: ContactItem) {
        contactItem.isGroup = true
        contactItem.type = "GROUP"
        createGroupTask = CreateGroupTask(
            contactItem,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_CREATE_GROUP
        )
        createGroupTask?.setCreateGroupTaskListener(this)
        createGroupTask?.execute()
    }

    override fun onPostCreateGroupExecuteFail(
        response: JSONObject?,
        name: String?,
        file: File?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        Log.e(TAG, "CONTACTS GET ERROR - $response")
    }

    override fun onPostCreateGroupExecute(
        response: JSONObject?,
        name: String?,
        file: File?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        val status = response?.optInt("status") ?: 0
        if (response == null || 99 == status || -1 == status) {
            onPostCreateGroupExecuteFail(response, name, file, contacts, contactItem)
        } else {
            try {
                val id = response.getString("group_id")
                if (id.isNullOrEmpty()) {
                    Log.e(TAG, "Groups save failed: id is null -$id")
                    onPostCreateGroupExecuteFail(response, name, file, contacts, contactItem)
                } else {
                    contactItem?.dataId = id
                    contactItem?.intellibitzId = id
                    contactItem?.typeId = id
                    savesGroupsInDB(contactItem, appCompatActivity?.applicationContext)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    private fun savesGroupsInDB(contactItem: ContactItem?, context: Context?) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contactItem, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    fun savesGroupInDB(name: String?, file: File?, id: String) {
        val contactItem = ContactItem()
        contactItem.dataId = id
        contactItem.name = name
        contactItem.profilePic = file?.absolutePath
        val contactItems = ArrayList<ContactItem>(1)
        contactItems.add(contactItem)
        savesGroupsInDB(contactItems, appCompatActivity?.applicationContext)
    }

    private fun savesGroupsInDB(contacts: Collection<ContactItem>?, context: Context?) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contacts, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    override fun setGroupsSaveToDBTaskToNull() {
        groupsSaveToDBTask = null
    }

    override fun onPostGroupsSaveToDBExecute(uri: Uri?, contacts: Collection<ContactItem>?) {
        if (contacts != null && contacts.isNotEmpty()) {
            contactItem = contacts.iterator().next()
            addUsersToGroups(contactItem)
            Log.e(TAG, " GROUPS Contacts - SUCCESS - $uri")
        } else {
            Log.e(TAG, "Group Save has returned EMPTY contacts - PLEASE CHECK: $uri")
        }
    }

    private fun addUsersToGroups(contactItem: ContactItem?) {
        groupsAddUsersTask = GroupsAddUsersTask(
            contactItem,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_GROUP_ADD_USERS
        )
        groupsAddUsersTask?.setGroupsAddUsersTaskListener(this)
        groupsAddUsersTask?.execute()
    }

    override fun onPostGroupsAddUsersExecuteFail(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        Log.e(TAG, "onPostGroupsAddUsersExecuteFail: $response")
    }

    override fun onPostGroupsAddUsersExecute(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        val status = response?.optInt("status") ?: 0
        if (response == null || 99 == status || -1 == status) {
            onPostGroupsAddUsersExecuteFail(response, id, name, contacts, contactItem)
        } else {
            createNewChat(contactItem, user)
            Log.e(TAG, " GROUPS ADD USERS - SUCCESS - ")
        }
    }

    override fun setCreateGroupTaskToNull() {
        createGroupTask = null
    }

    override fun onViewModeChanged() {
        viewModeListener?.onViewModeChanged()
        super.onViewModeChanged()
    }

    override fun onViewModeItem() {
        viewModeListener?.onViewModeItem()
        super.onViewModeItem()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        doBindService()
        messageContentObserver = object : ContentObserver(Handler()) {
            override fun deliverSelfNotifications(): Boolean {
                return super.deliverSelfNotifications()
            }

            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                synchronized(lock) {
                    counter++
                    looperThread = MainApplicationSingleton.performOnHandlerThread {
                        try {
                            Thread.sleep(500)
                        } catch (ignored: InterruptedException) {
                            Log.e(TAG, ignored.message)
                        }
                        quitHandlerThread()
                    }
                }
            }
        }
        appCompatActivity?.contentResolver?.registerContentObserver(
            MsgsGrpPeopleContentProvider.CONTENT_URI,
            true,
            messageContentObserver
        )
    }

    private fun quitHandlerThread() {
        looperThread?.quit()
        if (counter > 1) {
            Log.d(TAG, "quitHandlerThread: counter - $counter")
            counter--
            return
        }
        if (counter <= 1) {
            counter = 0
            val appCompatActivity = appCompatActivity
            if (appCompatActivity == null) return
            if (appCompatActivity.isFinishing) return
            if (MainApplicationSingleton.isAPI17()) {
                if (appCompatActivity.isDestroyed) return
            }
            restartLoader()
            Log.d(TAG, "quitHandlerThread: counter - $counter")
        }
    }

    override fun onDestroy() {
        doUnbindService()
        appCompatActivity?.contentResolver?.unregisterContentObserver(messageContentObserver)
        super.onDestroy()
    }

    fun onNewMenuClicked() {
        showNewMessageDialog()
    }

    fun onNewMenuDetailClicked() {
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        outState.putParcelableArrayList(MessageItem.TAG, messageItems)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(@Nullable savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            user = it.getParcelable(ContactItem.USER_CONTACT)
            messageItems = it.getParcelableArrayList(MessageItem.TAG) ?: ArrayList()
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            setTwoPane(it.getBoolean("twoPane"))
            user = it.getParcelable(ContactItem.USER_CONTACT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_messages, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        empty = rootView?.findViewById(R.id.tv_empty)
        empty?.setOnClickListener {
            startEmailListActivity()
        }
        rvMessages = rootView?.findViewById(R.id.recyclerview)
        setupSwipe(rootView)
        setupFAB(rootView)
        setupSnackBar(rootView)
        restartLoader()
    }

    private fun startEmailListActivity() {
        val intent = Intent(appCompatActivity, EmailAccountListActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNT_RQ_CODE)
    }

    fun restartFilterLoaders() {
        if (isMinusChat) {
            restartLoader()
        }
    }

    fun setupSwipe(rootView: View?) {
        val refreshLayout = rootView?.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        refreshLayout?.setOnRefreshListener {
            val mHandler = Handler()
            mHandler.postDelayed({
                refreshLayout.isRefreshing = false
            }, 2000)
        }
    }

    private fun restartLoader(uri: Uri) {
        try {
            val id = ContentUris.parseId(uri)
            messageItems = null
            restartLoader()
        } catch (e: NumberFormatException) {
            // ignore
        }
    }

    protected fun restartLoader(filter: String) {
        this.filter = filter
        restartLoader()
    }

    private fun restartCacheLoader() {
        val objects = MainApplicationSingleton.getInstance(appCompatActivity)
            .getGlobalSBValueAsList(MessagesFragment.TAG)
        if (objects == null || objects.isEmpty()) {
            restartLoader()
            return
        }
        if (counter > 0) {
            counter = 0
            restartLoader()
            Log.d(TAG, "quitHandlerThread: counter - $counter")
            return
        }
        this.messageItems = objects as ArrayList<MessageItem>
        createRecycleAdapter()
    }

    private fun restartLoader() {
        appCompatActivity?.supportLoaderManager?.restartLoader(
            MainApplicationSingleton.MESSAGES_LOADERID,
            null,
            this
        )
    }

    private fun showActiveMessage() {
        if (processNewMessageFromSharedIntent()) {
            Log.d(TAG, "restartLoader:processNewMessageFromSharedIntent is true.. Loader skipped")
            return
        }
    }

    private fun createRecycleAdapter() {
        resetRecycleAdapter()
        showActiveMessage()
    }

    private fun resetRecycleAdapter() {
        if (rvMessages == null && view != null) {
            rvMessages = rootView?.findViewById(R.id.recyclerview)
        }
        if (rvMessages == null) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL")
            return
        }
        if (messageItems == null) return
        Collections.sort(messageItems, MessageItem.MessageItemComparator(BaseItemComparator.SORT_MODE.DESC))
        rvMessagesAdapter = RecyclerViewAdapter(messageItems)
        rvMessagesAdapter?.setHasStableIds(true)
        rvMessages?.adapter = rvMessagesAdapter
    }

    private fun processNewMessageFromSharedIntent(): Boolean {
        if (sharedIntent == null) return false
        if (sharedIntent?.extras == null) return false
        val contactItem = sharedIntent?.getParcelableExtra<ContactItem>(ContactItem.TAG)
        if (contactItem != null) {
            MainApplicationSingleton.performOnUIHandlerThread {
                sharedIntent = null
                createNewChat(contactItem, user)
            }
            return true
        }
        val intellibitzContactItem = sharedIntent?.getParcelableExtra<ContactItem>(ContactItem.INTELLIBITZ_CONTACT)
        if (intellibitzContactItem != null) {
            MainApplicationSingleton.performOnUIHandlerThread {
                sharedIntent = null
                val contactItem = ContactItem(intellibitzContactItem)
                contactItem.isGroup = false
                contactItem.type = "USER"
                createNewChat(contactItem, user)
            }
            return true
        }
        val deviceContactItem = sharedIntent?.getParcelableExtra<ContactItem>(ContactItem.DEVICE_CONTACT)
        if (deviceContactItem != null) {
            // Do nothing
        }
        sharedIntent = null
        return false
    }

    private fun processNewChatMessage(contactItem: ContactItem?, context: Context?): MessageItem? {
        val id = contactItem?.dataId
        if (id != null) {
            Log.d(TAG, "User ready for Chat: $id")
            var item: MessageItem? = null
            if (messageItems.isNotEmpty()) {
                item = MainApplicationSingleton.getBaseItem(id, messageItems)
            }
            if (item == null) {
                item = MessageItem()
                item?.dataId = MessageItem.TAG
            }
            item?.docOwner = user?.dataId
            item?.name = contactItem?.name
            if (contactItem?.isGroup == true) {
                MessageChatGroupContentProvider.createMessageChatThread(id, item)
            } else {
                MessageChatContentProvider.createMessageChatThread(id, item)
            }
            if (item?.name == null) {
                item?.name = item?.dataId
            }
            item?.contactItem = contactItem
            return item
        }
        return null
    }

    private fun processNewChatMessage(id: String, name: String) {
        if (id.isNullOrEmpty()) {
            Log.e(TAG, "processNewChatMessage: id is NULL - $name")
            return
        }
        contactItem = ContactItem()
        contactItem?.dataId = id
        contactItem?.name = name
        createNewChat(contactItem, user)
    }

    private fun processNewChatMessage(contactItem: ContactItem?): MessageItem? {
        val id = contactItem?.intellibitzId
        if (id.isNullOrEmpty()) return null
        Log.d(TAG, "User ready for Chat: $id")
        var messageItem: MessageItem? = null
        if (messageItems.isNotEmpty()) {
            messageItem = MainApplicationSingleton.getBaseItem(id, messageItems)
        }
        if (messageItem == null) {
            messageItem = MessageItem()
            messageItem?.dataId = MessageItem.TAG
        }
        messageItem?.docOwner = user?.dataId
        messageItem?.name = contactItem?.name
        if (messageItem?.name == null) {
            messageItem?.name = messageItem?.dataId
        }
        if (contactItem?.isGroup == true) {
            MessageChatGroupContentProvider.createMessageChatThread(id, messageItem)
        } else {
            MessageChatContentProvider.createMessageChatThread(id, messageItem)
        }
        return messageItem
    }

    private fun createNewChat(contactItem: ContactItem?, user: ContactItem?) {
        val item = processNewChatMessage(contactItem)
        resetRecycleAdapter()
        showMessageThreadMessage(item, user)
    }

    private fun showMessageThreadMessage(id: String, user: ContactItem?) {
        if (id.isNotEmpty()) {
            val activeChat = MainApplicationSingleton.getBaseItem(id, messageItems)
            if (activeChat != null) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    showMessageThreadMessage(activeChat, user)
                }
            }
        }
    }

    private fun setupSnackBar(view: View?) {
        view?.let {
            snackbar = Snackbar.make(it, "Please Add Account to see Emails", Snackbar.LENGTH_LONG)
            snackbar?.setAction("ADD EMAIL") {
                startEmailListActivity()
            }
        }
    }

    private fun setupFAB(view: View?) {
        view?.let {
            fab = it.findViewById(R.id.fab)
            fab?.setOnClickListener {
                showNewMessageDialog()
            }
        }
    }

    fun showNewMessageDialog() {
        val appCompatActivity = appCompatActivity
        if (appCompatActivity == null) {
            Log.e(TAG, "showNewBottomDialogFragment: Activity NULL - cannot show new dialog")
            return
        }
        newBottomDialogFragment = NewBottomDialogFragment()
        newBottomDialogFragment?.setNewBottomDialogListener(this)
        newBottomDialogFragment?.show(appCompatActivity.supportFragmentManager, "Create")
    }

    override fun onBackPressed(): Boolean {
        val back = appCompatActivity?.intent
        val result: Boolean
        if (back == null) {
            result = true
        } else {
            val action = back.action
            result = if (ContactSelectFragment.TAG == action) {
                startMsgChatGrpContactsDetailActivity()
                false
            } else {
                true
            }
        }
        return result
    }

    fun onMessageForward(intent: Intent?) {
        if (intent == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            appCompatActivity?.onBackPressed()
            return
        }
        messageItemToForward = intent.getParcelableExtra(MessageItem.TAG)
        val source = intent.action
        if (messageItemToForward == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            appCompatActivity?.onBackPressed()
            return
        }
        if (PeopleDetailFragment.TAG == source) {
            // Do nothing
        }
    }

    fun showContactThreadDetail(intent: Intent) {
        startMsgChatGrpContactsDetailActivity()
    }

    fun showContactThreadDetail(): MsgChatGrpContactsDetailFragment? {
        return null
    }

    override fun onNewDialogClose(newBottomDialogFragment: NewBottomDialogFragment?) {
        this.newBottomDialogFragment?.dismiss()
    }

    override fun onNewGroup(newBottomDialogFragment: NewBottomDialogFragment?) {
        onNewDialogClose(newBottomDialogFragment)
        contactItem = ContactItem()
        contactItem?.isNewGroup = true
        contactItem?.isGroup = true
        startMsgChatGrpContactsDetailActivity()
    }

    override fun onNewChat(newBottomDialogFragment: NewBottomDialogFragment?) {
        onNewDialogClose(newBottomDialogFragment)
        contactItem = ContactItem()
        contactItem?.isNewGroup = false
        contactItem?.isGroup = false
        startContactSelectActivity()
    }

    fun showContactSelectItemFragment() {
        // Do nothing
    }

    private fun startContactSelectActivity() {
        val intent = Intent(appCompatActivity, ContactSelectActivity::class.java)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE)
    }

    fun startMsgChatGrpContactsDetailActivity() {
        val intent = Intent(appCompatActivity, MsgChatGrpContactsDetailActivity::class.java)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                data?.let {
                    val item = it.getParcelableExtra<ContactItem>(ContactItem.TAG)
                    if (contactItem == null) contactItem = item
                    val size = contactItem?.selectedContacts?.size ?: 0
                    if (contactItem == item) {
                        Log.e(TAG, " Already selected contacts: $size")
                    } else {
                        contactItem?.selectedContacts = item?.selectedContacts
                    }
                    contactItem?.mergeSelectedContacts()
                    val count = contactItem?.contactItems?.size ?: 0
                    if (1 == count) {
                        val selectedContactItemForChat = contactItem?.contactItems?.iterator()?.next()
                        contactItem?.dataId = selectedContactItemForChat?.intellibitzId
                        contactItem?.intellibitzId = selectedContactItemForChat?.intellibitzId
                        contactItem?.typeId = selectedContactItemForChat?.typeId
                        contactItem?.deviceContactId = selectedContactItemForChat?.deviceContactId
                        contactItem?.name = selectedContactItemForChat?.name
                        contactItem?.isGroup = false
                        contactItem?.isEmailItem = false
                        contactItem?.type = "USER"
                        createNewChat(contactItem, user)
                    } else {
                        Log.e(TAG, "onActivityResult: Please select 1 contact - $count")
                    }
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
            }
        } else if (MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                data?.let {
                    val item = it.getParcelableExtra<ContactItem>(ContactItem.TAG)
                    if (item != null) {
                        contactItem = item
                    }
                    createNewChat(contactItem, user)
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
            }
        }
    }

    fun onOkPressed(intent: Intent?) {
        if (intent == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            appCompatActivity?.onBackPressed()
            return
        }
        val item = intent.getParcelableExtra<ContactItem>(ContactItem.TAG)
        val source = intent.action
        if (item == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            if (ContactSelectFragment.TAG == source) {
                if (contactItem?.isNewGroup == true) {
                    showContactThreadDetail(intent)
                    return
                }
            }
            Log.e(TAG, " cancelled group by pressing back: ")
            return
        }
        if (contactItem == null) contactItem = item
        val size = contactItem?.selectedContacts?.size ?: 0
        if (ContactSelectFragment.TAG == source) {
            if (0 == size) {
                Log.e(TAG, " selected contacts is 0 by pressing ok.. cancelling : ")
                appCompatActivity?.onBackPressed()
                return
            }
        }
        if (contactItem == item) {
            Log.e(TAG, " Already selected contacts: $size")
        } else {
            contactItem?.selectedContacts = item?.selectedContacts
        }
        contactItem?.mergeSelectedContacts()
        val count = contactItem?.contactItems?.size ?: 0
        if (ContactSelectFragment.TAG == source) {
            if (contactItem?.isNewGroup == true) {
                showContactThreadDetail(intent)
                return
            } else {
                if (1 == count) {
                    val contactItem = this.contactItem?.contactItems?.iterator()?.next()
                    this.contactItem?.dataId = contactItem?.intellibitzId
                    this.contactItem?.intellibitzId = contactItem?.intellibitzId
                    this.contactItem?.typeId = contactItem?.typeId
                    this.contactItem?.deviceContactId = contactItem?.deviceContactId
                    this.contactItem?.name = contactItem?.name
                    this.contactItem?.isGroup = false
                    this.contactItem?.type = "USER"
                    createNewChat(this.contactItem, user)
                    return
                }
            }
        }
        if (1 == count) {
            // Do nothing
        } else {
            val name = contactItem?.name
            if (name.isNullOrEmpty()) {
                Log.e(TAG, "Name is empty - cannot group chat")
                appCompatActivity?.onBackPressed()
                return
            }
            contactItem?.isGroup = true
            contactItem?.type = "GROUP"
            Log.e(TAG, " selected contacts for Group chat: $count")
        }
        createNewChat(contactItem, user)
        return
    }

    override fun onNewEmail(newBottomDialogFragment: NewBottomDialogFragment?) {
        if (TextUtils.isEmpty(user?.email)) {
            UserEmailContentProvider.populateUserEmailsJoinById(user, appCompatActivity)
        }
        onNewDialogClose(newBottomDialogFragment)
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
            appCompatActivity?.supportFragmentManager, "NewMessageDialog"
        )
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        onClick(view)
    }

    override fun onClick(v: View) {
        if (R.id.tv_empty == v.id) {
            startEmailListActivity()
            return
        }
        val dataId = v.findViewById<TextView>(R.id.tv_id)
        val messageThreadId = dataId.text.toString()
        val item = MainApplicationSingleton.getBaseItem(messageThreadId, messageItems)
        if (item == null) {
            showMessageThreadMessage(messageThreadId)
        } else {
            showMessageThreadMessage(item, user)
        }
    }

    private fun showMessageThreadMessage(messageThreadId: String) {
        val messageItem = MessageItem()
        messageItem.dataId = messageThreadId
        showMessageThreadMessage(messageItem, user)
    }

    private fun showMessageThreadMessage(contactItem: ContactItem?, user: ContactItem?) {
        val id = contactItem?.dataId
        showMessageThreadMessage(id, user)
    }

    private fun showMessageThreadMessage(messageItem: MessageItem?, user: ContactItem?) {
        val id = messageItem?.dataId
        if (id != null && id.isNotEmpty() && MessageItem.TAG != id) {
            if (messageItem?.isEmailItem == true) {
                MessageEmailContentProvider.queryMessageEmailThreadFullJoin(messageItem, appCompatActivity)
            } else {
                if (messageItem?.isGroup == true) {
                    MessageChatGroupContentProvider.queryMessageChatThreadFullJoin(messageItem, appCompatActivity)
                } else {
                    MessageChatContentProvider.queryMessageChatThreadFullJoin(messageItem, appCompatActivity)
                }
            }
        }
        if (peopleTopicListener != null) {
            peopleTopicListener?.onPeopleTopicClicked(messageItem, user)
        }
        onPeopleTopicClicked(messageItem, user)
    }

    fun onPeopleTopicClicked(messageItem: MessageItem?, user: ContactItem?) {
        val type = messageItem?.type
        if ("CHAT".equals(type, ignoreCase = true)) {
            if ("USER".equals(messageItem?.toType, ignoreCase = true)) {
                startPeopleDetailActivity(messageItem, user)
            } else {
                startMessageChatGroupActivity(messageItem, user)
            }
        } else if ("EMAIL".equals(type, ignoreCase = true)) {
            val did = messageItem?.deviceContactId ?: 0
            if (did > 0) {
                val cursor = appCompatActivity?.contentResolver?.query(
                    IntellibitzContactContentProvider.CONTENT_URI, null,
                    " ( " + ContactItemColumns.KEY_IS_EMAIL + " = 0 OR " +
                            ContactItemColumns.KEY_IS_EMAIL + " IS NULL ) AND ( " +
                            ContactItemColumns.KEY_IS_GROUP + " = 0 OR " +
                            ContactItemColumns.KEY_IS_GROUP + " IS NULL ) AND " +
                            ContactItemColumns.KEY_DEVICE_CONTACTID + " = ? ",
                    arrayOf(did.toString()), null
                )
                if (cursor != null && cursor.count > 0) {
                    val id = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_INTELLIBITZ_ID))
                    cursor.close()
                    if (TextUtils.isEmpty(id)) {
                        messageItem?.intellibitzId = messageItem?.docSenderEmail
                        startClutterEmailsActivity(messageItem, user)
                    } else {
                        val cursor = appCompatActivity?.contentResolver?.query(
                            MessagesChatContentProvider.CONTENT_URI, null,
                            MessageItemColumns.KEY_CHAT_ID + " = ? ",
                            arrayOf(id), null
                        )
                        val chatMessage = MessageChatContentProvider.fillsMessageItemFromCursor(cursor)
                        cursor?.close()
                        if (chatMessage == null) {
                            messageItem?.intellibitzId = messageItem?.docSenderEmail
                            startClutterEmailsActivity(messageItem, user)
                        } else {
                            startPeopleDetailActivity(chatMessage, user)
                        }
                    }
                }
                cursor?.close()
            } else {
                messageItem?.intellibitzId = messageItem?.docSenderEmail
                startClutterEmailsActivity(messageItem, user)
            }
        } else {
            startPeopleDetailActivity(messageItem, user)
        }
    }

    fun startMessageChatGroupActivity(messageItem: MessageItem?, user: ContactItem?) {
        val intent = Intent(appCompatActivity, MessageChatGroupActivity::class.java)
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        appCompatActivity?.startActivityForResult(
            intent, MainApplicationSingleton.ACTIVITY_MESSAGECHATGROUP_RQ_CODE
        )
    }

    fun startPeopleDetailActivity(messageItem: MessageItem?, user: ContactItem?) {
        val intent = Intent(appCompatActivity, PeopleDetailActivity::class.java)
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        appCompatActivity?.startActivityForResult(
            intent, MainApplicationSingleton.ACTIVITY_PEOPLEDETAIL_RQ_CODE
        )
    }

    fun startClutterEmailsActivity(messageItem: MessageItem?, user: ContactItem?) {
        val intent = Intent(appCompatActivity, ClutterEmailsActivity::class.java)
        intent.putExtra(MessageItem.EMAIL_MESSAGE, messageItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        appCompatActivity?.startActivityForResult(
            intent, MainApplicationSingleton.ACTIVITY_CLUTTEREMAILS_RQ_CODE
        )
    }

    private fun setSelectedItem(mItem: MessageItem?) {
        selectedItem = mItem
    }

    private fun execDeleteMsgsTask(msgs: Array<String>) {
        val deleteMsgsTask = DeleteMsgsTask(
            msgs,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_DELETED_MSGS,
            context
        )
        deleteMsgsTask.requestTimeoutMillis = 30000
        deleteMsgsTask.setDeleteMsgsTaskListener(this)
        deleteMsgsTask.execute()
    }

    private fun deleteMessages() {
        execDeleteMsgsTask(arrayOf(selectedItem?.dataId ?: ""))
    }

    private fun flagMessages() {
        // Do nothing
    }

    override fun onPostDeleteMsgsResponse(response: JSONObject?, item: Array<String>?) {
        try {
            val status = response?.getInt(MainApplicationSingleton.STATUS_PARAM) ?: 0
            if (1 == status) {
                Log.e(TAG, "Delete Msgs SUCCESS - $response")
                try {
                    val count = MessageChatContentProvider.deleteMsgs(item, context)
                    Log.e(TAG, "Delete in DB: $count")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (-1 == status || 99 == status) {
                onPostDeleteMsgsErrorResponse(response, item)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPostDeleteMsgsErrorResponse(response: JSONObject?, item: Array<String>?) {
        Log.e(TAG, "Delete Msgs FAIL - $response: ${Arrays.toString(item)}")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val newEmailDialogFragment = dialog as NewEmailDialogFragment
        if (newEmailDialogFragment.isChatMode()) {
            performNewChat(newEmailDialogFragment)
        } else {
            performNewEmail(newEmailDialogFragment)
        }
    }

    private fun performNewEmail(newEmailDialogFragment: NewEmailDialogFragment) {
        val email = user?.email
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "performNewEmail: User email is empty - No email account signed up")
            return
        }
        val messageItem = MessageItem()
        val contactThreadItem = messageItem.contactItem
        if (contactThreadItem == null) {
            messageItem.contactItem = ContactItem()
        }
        messageItem.dataId = MessageItem.TAG
        messageItem.baseType = "THREAD"
        messageItem.docType = "THREAD"
        messageItem.type = "EMAIL"
        messageItem.dataRev = "1"
        val name = user?.name
        messageItem.from = name
        messageItem.subject = newEmailDialogFragment.subject
        messageItem.docOwner = user?.docOwner
        messageItem.docSender = name
        messageItem.docOwnerEmail = email
        messageItem.docSenderEmail = email
        messageItem.timestamp = System.currentTimeMillis()
        val to = newEmailDialogFragment.to
        val cc = newEmailDialogFragment.cc
        val bcc = newEmailDialogFragment.bcc
        val contactItem = ContactItem()
        contactItem.dataId = email
        contactItem.typeId = email
        contactItem.intellibitzId = email
        contactItem.name = name
        contactItem.type = "from"
        messageItem.contactItem?.addContact(contactItem)
        messageItem.compose(to, cc, bcc)
        val intent = Intent()
        if (newEmailDialogFragment.isChatMode()) {
            performNewChat(newEmailDialogFragment)
            intent.action = MainApplicationSingleton.BROADCAST_NEW_CHAT_DIALOG_OK
        } else {
            intent.action = MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK
            showMessageThreadMessage(messageItem, user)
        }
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity?.applicationContext!!)
            .sendBroadcast(intent)
    }

    private fun performNewChat(newEmailDialogFragment: NewEmailDialogFragment) {
        val to = newEmailDialogFragment.to
        if (to != null && to.isNotEmpty()) {
            var i = 0
            val sto = arrayOfNulls<String>(to.size)
            to.forEach { s ->
                val address = s.address
                sto[i++] = address
            }
            Log.d(TAG, Arrays.toString(sto))
            val subject = newEmailDialogFragment.subject
            val intellibitzContacts = newEmailDialogFragment.intellibitzContacts
            if (sto.size == 1) {
                contactItem = ContactItem()
                val id = sto[0]
                contactItem?.dataId = id
                contactItem?.name = subject
                val intellibitzContactItem = intellibitzContacts[id]
                if (intellibitzContactItem != null) {
                    val contactItem = ContactItem(intellibitzContactItem)
                    this.contactItem?.name = contactItem.name
                    this.contactItem?.addContact(contactItem)
                }
                contactItem?.isGroup = false
                createNewChat(contactItem, user)
            } else if (sto.size > 1) {
                contactItem = ContactItem()
                contactItem?.name = subject
                val items = Collections.synchronizedSet(HashSet<ContactItem>(sto.size))
                sto.forEach { contact ->
                    val intellibitzContactItem = ContactItem(contact)
                    val contactItem = ContactItem(intellibitzContactItem)
                    val item = intellibitzContacts[contact]
                    if (item != null) {
                        contactItem.name = item.name
                    }
                    items.add(contactItem)
                }
                contactItem?.contactItems = items
                createNewGroupChat(contactItem)
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
    }

    fun onDetailQueryTextSubmit(query: String): Boolean {
        return false
    }

    fun onDetailQueryTextChange(newText: String): Boolean {
        return false
    }

    fun onDetailClose(): Boolean {
        return false
    }

    fun minusChat() {
        isMinusChat = true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        restartLoader(query)
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        restartLoader(newText)
        return false
    }

    override fun onClose(): Boolean {
        restartLoader("")
        return false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
        if (MainApplicationSingleton.MESSAGES_LOADERID == id) {
            val selection = " ( name IS NULL OR name like ? ) " + " AND ( " + MessageItemColumns.KEY_IS_FLAGGED + " = 1 ) "
            val selArgs: Array<String>
            val filter = null
            selArgs = if (filter != null && filter.isNotEmpty()) {
                arrayOf("%$filter%")
            } else {
                arrayOf("%%")
            }
            return CursorLoader(
                appCompatActivity?.applicationContext!!,
                MessageChatContentProvider.CONTENT_URI,
                null,
                selection,
                selArgs,
                MessageItemColumns.KEY_TIMESTAMP + " DESC"
            )
        }
        return null
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (MainApplicationSingleton.MESSAGES_LOADERID == loader.id) {
            if (cursor == null) {
                messageItems.clear()
                createRecycleAdapter()
                showEmpty()
                return
            }
            val count = cursor.count
            peopleTopicListener?.onPeopleTopicsLoaded(count)
            if (0 == count) {
                cursor.close()
                messageItems.clear()
                createRecycleAdapter()
                showEmpty()
                return
            }
            if (count > 0 && 0 == cursor.position) {
                hideEmpty()
                messageItems.clear()
                messageItems = MessageChatContentProvider.fillMessageItemsFromCursor(cursor)
                cursor.close()
                createRecycleAdapter()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (MainApplicationSingleton.MESSAGES_LOADERID == loader.id) {
            // Do nothing
        }
    }

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ChatService.MSG_SHOW_TYPING -> {
                    if (peopleDetailListener != null) {
                        try {
                            val obj = msg.obj as String?
                            if (obj != null) {
                                val jsonArray = JSONArray(obj)
                                val jsonObject = jsonArray.getJSONObject(0)
                                try {
                                    val toUid = jsonObject.getString("to_uid")
                                    val fromUid = jsonObject.getString("from_uid")
                                    val fromName = jsonObject.getString("from_name")
                                    showTyping(toUid, fromUid, fromName, user!!)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    inner class RecyclerViewAdapter(private val messageItems: List<MessageItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_emailchat_topic, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItem = messageItems[position]
            holder.tvId.text = holder.mItem.dataId
            var val = holder.mItem.subject
            if (holder.mItem.isChat()) {
                val = holder.mItem.dataId
            } else {
                if (val == null) {
                    val = holder.mItem.docOwner
                }
            }
            if (holder.mItem.isChat()) {
                val = holder.mItem.name
            }
            holder.tvName.text = val
            var from = holder.mItem.docSender
            if (from == null) from = holder.mItem.from
            val to = holder.mItem.to
            val cc = holder.mItem.cc
            var val2 = MessageItem.getSingleAddress(from, to, cc, user?.email, user?.name)
            var read: Drawable? = null
            if (user?.dataId == holder.mItem.docSenderEmail && !holder.mItem.isEmailItem()) {
                if (holder.mItem.isDelivered()) {
                    read = getDrawable(R.drawable.ic_done_all_black_18dp, appCompatActivity?.theme)
                } else if (holder.mItem.isRead()) {
                    read = getDrawable(R.drawable.ic_done_black_18dp, appCompatActivity?.theme)
                } else {
                    read = getDrawable(R.drawable.ic_restore_black_18dp, appCompatActivity?.theme)
                }
            }
            var drawable = getDrawable(R.drawable.ic_mail_outline_black_18dp, appCompatActivity?.theme)
            if (holder.mItem.isChat()) {
                drawable = getDrawable(R.drawable.ic_chat_bubble_outline_black_18dp, appCompatActivity?.theme)
                if (holder.mItem.isGroupChat()) {
                    drawable = getDrawable(R.drawable.ic_question_answer_black_18dp, appCompatActivity?.theme)
                }
            }
            if (drawable != null) {
                drawable.setBounds(Rect(0, 0, 20, 20))
                if (read != null) {
                    read.setBounds(Rect(0, 0, 20, 20))
                }
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, read)
                setCompoundDrawablesRelative(holder.tvSubject, drawable, null, null, read)
            }
            if (holder.mItem.isTyping()) {
                val = holder.mItem.typingText
            } else {
                if (!TextUtils.isEmpty(val) && !TextUtils.isEmpty(val2) && val.equals(val2, ignoreCase = true)) {
                    val = " " + holder.mItem.latestMessageText
                } else {
                    val = "$val2: ${holder.mItem.latestMessageText}"
                }
                val = val.replace("\n", " ")
            }
            holder.tvMessage.text = val
            holder.tvSubject.text = val
            if (holder.mItem.hasAttachments) {
                drawable = getDrawable(R.drawable.ic_attach_file_black_18dp, appCompatActivity?.theme)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                holder.ivAttachment.visibility = View.VISIBLE
            } else {
                holder.ivAttachment.visibility = View.INVISIBLE
            }
            val createDate = holder.mItem.timestamp * 1000
            val timestamp = Timestamp(createDate)
            val now = System.currentTimeMillis()
            var df: DateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            if (now - createDate < 1000 * 60 * 60 * 24) {
                df = SimpleDateFormat("hh mm a", Locale.getDefault())
            }
            val dt = df.format(timestamp.time)
            holder.tvTimestamp.text = dt
            val generator = ColorGenerator.MATERIAL
            val = holder.mItem.from
            if (val == null || val.isEmpty()) val = holder.mItem.subject
            if ("CHAT".equals(holder.mItem.type)) {
                if (val == null || val.isEmpty()) val = "C"
            } else {
                if (val == null || val.isEmpty()) val = "E"
            }
            val color2 = generator.getColor(val)
            val builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round()
            val ic2 = builder.build(val.substring(0, 1), color2)
            val pic = holder.mItem.profilePic
            if (pic.isNullOrEmpty()) {
                holder.tvSender.setImageDrawable(ic2)
            } else if (pic.startsWith("http")) {
                bitmapFromUrlTask = BitmapFromUrlTask(holder.tvSender, pic, appCompatActivity?.applicationContext)
                bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                bitmapFromUrlTask?.execute()
            } else {
                try {
                    val bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                    if (bitmap == null) {
                        holder.tvSender.setImageDrawable(ic2)
                    } else {
                        val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                        val bitmapDrawable = BitmapDrawable(resources, croppedBitmap)
                        bitmapDrawable.setBounds(Rect(0, 0, 100, 100))
                        holder.tvSender.setImageDrawable(bitmapDrawable)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            generator
            val anInt = holder.mItem.unreadCount
            if (0 == anInt) {
                holder.ivUnreadCount.visibility = View.INVISIBLE
                holder.mView.setBackgroundResource(R.drawable.selectable_item_bg)
            } else {
                holder.mView.setBackgroundResource(R.drawable.item_gray_bg_layer)
                val = anInt.toString()
                val builder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .roundRect(20)
                val ic2 = builder.build(val, Color.RED)
                holder.ivUnreadCount.setImageDrawable(ic2)
                holder.ivUnreadCount.visibility = View.VISIBLE
            }
        }

        override fun getItemId(position: Int): Long {
            return messageItems[position]._id
        }

        override fun getItemCount(): Int {
            return messageItems.size
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap?, view: View?, context: Context?) {
            if (context == null) return
            val resources = context.resources
            if (resources == null) return
            if (bitmap == null) {
                val builder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round()
                val generator = ColorGenerator.MATERIAL
                val ic2 = builder.build("EC", generator.getColor("EC"))
                (view as ImageView).setImageDrawable(ic2)
            } else {
                val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                setImageDrawable(view, croppedBitmap)
            }
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap?) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
            val mView: View = view
            val tvId: TextView = view.findViewById(R.id.tv_id)
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvSubject: TextView = view.findViewById(R.id.tv_subject)
            val tvFrom: TextView = view.findViewById(R.id.tv_from)
            val tvMessage: TextView = view.findViewById(R.id.tv_text)
            val tvTimestamp: TextView = view.findViewById(R.id.tv_timestamp)
            val tvSender: ImageView = view.findViewById(R.id.tv_sender)
            val ivUnreadCount: ImageView = view.findViewById(R.id.iv_unread_count)
            val ivAttachment: ImageView = view.findViewById(R.id.iv_attach)
            var mItem: MessageItem = MessageItem()

            init {
                view.setOnLongClickListener(this)
                mView.setOnClickListener(this@MessagesFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvMessage.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                setSelectedItem(mItem)
                if (mActionMode == null) {
                    mActionMode = appCompatActivity?.startSupportActionMode(mActionModeCallback)
                    v.isSelected = !v.isSelected
                    return true
                }
                v.isSelected = !v.isSelected
                return false
            }
        }
    }
}
