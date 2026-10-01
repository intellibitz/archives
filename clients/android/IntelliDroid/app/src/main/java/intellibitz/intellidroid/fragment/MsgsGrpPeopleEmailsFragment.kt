package intellibitz.intellidroid.fragment

import android.app.Activity
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.database.ContentObserver
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.ContactSelectActivity
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity
import intellibitz.intellidroid.bean.BaseItemComparator
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MessageChatGroupContentProvider
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MsgsGrpPeopleContentProvider
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.content.task.FetchMsgsGrpPeopleEmailsTask
import intellibitz.intellidroid.content.task.GroupsSaveToDBTask
import intellibitz.intellidroid.data.BaseItem
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.domain.account.EmailAccountListActivity
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.PeopleDetailListener
import intellibitz.intellidroid.listener.PeopleListener
import intellibitz.intellidroid.listener.PeopleTopicListener
import intellibitz.intellidroid.service.ChatService
import intellibitz.intellidroid.service.EmailService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.CreateGroupTask
import intellibitz.intellidroid.task.DeleteMsgsTask
import intellibitz.intellidroid.task.GroupsAddUsersTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import intellibitz.intellidroid.widget.NewBottomDialogFragment
import intellibitz.intellidroid.widget.NewEmailDialogFragment
import intellibitz.intellidroid.widget.UnLockPasswordFragment
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

class MsgsGrpPeopleEmailsFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    ContactListener,
    PeopleDetailListener,
    CreateGroupTask.CreateGroupTaskListener,
    GroupsAddUsersTask.GroupsAddUsersTaskListener,
    GroupsSaveToDBTask.GroupsSaveToDBTaskListener,
    NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
    AdapterView.OnItemClickListener,
    View.OnClickListener,
    DeleteMsgsTask.DeleteMsgsTaskListener,
    NewBottomDialogFragment.NewBottomDialogListener,
    UnLockPasswordFragment.OnUnLockPasswordFragmentListener,
    FetchMsgsGrpPeopleEmailsTask.FetchMsgsGrpPeopleTaskListener {

    companion object {
        const val TAG = "PeopleEmailsFragment"

        fun newInstance(
            messageItem: MessageItem?,
            user: ContactItem?,
            peopleListener: PeopleListener?
        ): MsgsGrpPeopleEmailsFragment {
            val fragment = MsgsGrpPeopleEmailsFragment()
            fragment.setUser(user)
            if (peopleListener is PeopleTopicListener) {
                fragment.setPeopleTopicListener(peopleListener as PeopleTopicListener)
            }
            if (peopleListener is PeopleDetailListener) {
                fragment.setPeopleDetailListener(peopleListener as PeopleDetailListener)
            }
            fragment.setViewModeListener(peopleListener)
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
    private var snackbar: Snackbar? = null
    private var fab: FloatingActionButton? = null
    private var filter: String? = null
    private var messageContentObserver: ContentObserver? = null
    private var recyclerView: RecyclerView? = null
    private var recyclerViewAdapter: RecyclerViewAdapter? = null
    private var createGroupTask: CreateGroupTask? = null
    private var groupsAddUsersTask: GroupsAddUsersTask? = null
    private var groupsSaveToDBTask: GroupsSaveToDBTask? = null
    private var deleteMsgsTask: DeleteMsgsTask? = null
    private var selectedItem: MessageItem? = null
    private var sharedIntent: Intent? = null
    private var contactItem: ContactItem? = null
    private var newBottomDialogFragment: NewBottomDialogFragment? = null
    private var messageItems: ArrayList<MessageItem>? = null
    private var contactItems: ArrayList<ContactItem>? = null
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
            inflater.inflate(R.menu.menu_context_msgsgrppeople, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_lock -> {
                    lockMessages()
                    mode.finish()
                    true
                }
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

    fun setMessageItems(messageItems: ArrayList<MessageItem>?) {
        this.messageItems = messageItems
        MainApplicationSingleton.getInstance(activity).putGlobalVariable(
            MsgsGrpPeopleEmailsFragment.TAG, messageItems
        )
    }

    fun setContactItems(contactItems: ArrayList<ContactItem>?) {
        this.contactItems = contactItems
        MainApplicationSingleton.getInstance(activity).putGlobalVariable(
            MsgChatGrpContactsFragment.TAG, contactItems
        )
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
                val pos = messageItems?.indexOf(item) ?: -1
                if (pos != -1) {
                    recyclerViewAdapter?.notifyItemChanged(pos)
                    val delay = Handler()
                    delay.postDelayed({
                        item.isTyping = false
                        recyclerViewAdapter?.notifyItemChanged(pos)
                        onPeopleTypingStopped("")
                    }, 1000)
                }
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
        activity?.bindService(
            Intent(activity, ChatService::class.java),
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
            activity?.unbindService(mConnection)
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

    fun setUser(user: ContactItem?) {
        this.user = user
    }

    fun setPeopleTopicListener(peopleTopicListener: PeopleTopicListener?) {
        this.peopleTopicListener = peopleTopicListener
    }

    private fun addUsersToGroups(id: String, name: String, contacts: Array<String>) {
        groupsAddUsersTask = GroupsAddUsersTask(id, name, contacts, user?.dataId, user?.token,
            user?.device, user?.deviceRef, MainApplicationSingleton.AUTH_GROUP_ADD_USERS)
        groupsAddUsersTask?.setGroupsAddUsersTaskListener(this)
        groupsAddUsersTask?.execute()
    }

    override fun setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null
    }

    private fun createNewGroupChat(contactItem: ContactItem) {
        contactItem.isGroup = true
        contactItem.type = "GROUP"
        createGroupTask = CreateGroupTask(contactItem, user?.dataId, user?.token,
            user?.device, user?.deviceRef, MainApplicationSingleton.AUTH_CREATE_GROUP)
        createGroupTask?.setCreateGroupTaskListener(this)
        createGroupTask?.execute()
    }

    override fun onPostCreateGroupExecuteFail(response: JSONObject?,
                                               name: String, file: File?, contacts: Array<String>?,
                                               contactItem: ContactItem) {
        // ERROR
        Log.e(TAG, "CONTACTS GET ERROR - $response")
    }

    override fun onPostCreateGroupExecute(response: JSONObject?,
                                          name: String, file: File?, contacts: Array<String>?,
                                          contactItem: ContactItem) {
        val status = response?.optInt("status") ?: 0
        if (response == null || 99 == status || -1 == status) {
            // retries again..
            onPostCreateGroupExecuteFail(response, name, file, contacts, contactItem)
        } else {
            try {
                val id = response.getString("group_id")
                if (id.isNullOrEmpty()) {
                    // retries again..
                    Log.e(TAG, "Groups save failed: id is null -$id")
                    onPostCreateGroupExecuteFail(response, name, file, contacts, contactItem)
                } else {
                    // sets the saved group id returned by the cloud
                    // only the id is relevant, rest of the info already with the contact
                    contactItem.dataId = id
                    contactItem.intellibitzId = id
                    contactItem.typeId = id

                    // if (!TextUtils.isEmpty(name))
                    //     contactItem.setName(name);
                    // if (file != null)
                    //     contactItem.setProfilePic(file.getAbsolutePath());
                    savesGroupsInDB(contactItem, activity?.applicationContext)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    private fun savesGroupsInDB(contactItem: ContactItem, context: Context?) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contactItem, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    fun savesGroupInDB(name: String, file: File?, id: String) {
        val contactItem = ContactItem()
        contactItem.dataId = id
        contactItem.name = name
        contactItem.profilePic = file?.absolutePath
        val contactItems = ArrayList<ContactItem>(1)
        contactItems.add(contactItem)
        savesGroupsInDB(contactItems, activity?.applicationContext)
    }

    private fun savesGroupsInDB(contacts: Collection<ContactItem>, context: Context?) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contacts, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    override fun setGroupsSaveToDBTaskToNull() {
        groupsSaveToDBTask = null
    }

    override fun onPostGroupsSaveToDBExecute(uri: Uri?, contacts: Collection<ContactItem>) {
        if (contacts.isNotEmpty()) {
            contactItem = contacts.iterator().next()
            addUsersToGroups(contactItem)
            Log.e(TAG, " GROUPS Contacts - SUCCESS - $uri")
        } else {
            Log.e(TAG, "Group Save has returned EMPTY contacts - PLEASE CHECK: $uri")
        }
    }

    private fun addUsersToGroups(contactItem: ContactItem) {
        groupsAddUsersTask = GroupsAddUsersTask(contactItem, user?.dataId, user?.token,
            user?.device, user?.deviceRef, MainApplicationSingleton.AUTH_GROUP_ADD_USERS)
        groupsAddUsersTask?.setGroupsAddUsersTaskListener(this)
        groupsAddUsersTask?.execute()
    }

    override fun onPostGroupsAddUsersExecuteFail(response: JSONObject?,
                                                 id: String, name: String, contacts: Array<String>?,
                                                 contactItem: ContactItem) {
        Log.e(TAG, "onPostGroupsAddUsersExecuteFail: $response")
    }

    override fun onPostGroupsAddUsersExecute(response: JSONObject?,
                                            id: String, name: String, contacts: Array<String>?,
                                            contactItem: ContactItem) {
        val status = response?.optInt("status") ?: 0
        if (response == null || 99 == status || -1 == status) {
            onPostGroupsAddUsersExecuteFail(response, id, name, contacts, contactItem)
            // retries again..
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
        activity?.contentResolver?.registerContentObserver(
            MessageChatContentProvider.CONTENT_URI, true,
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
            val appCompatActivity = activity
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
        activity?.contentResolver?.unregisterContentObserver(messageContentObserver)
        super.onDestroy()
    }

    fun onNewMenuClicked() {
        showNewBottomDialogFragment()
    }

    fun onNewMenuDetailClicked() {
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        outState.putParcelableArrayList(MessageItem.TAG, messageItems)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            user = it.getParcelable(ContactItem.USER_CONTACT)
            messageItems = it.getParcelableArrayList(MessageItem.TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTwoPane(arguments?.getBoolean("twoPane") ?: false)
        user = arguments?.getParcelable(ContactItem.USER_CONTACT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_msgsgrppeople, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        empty = view.findViewById(R.id.tv_empty)
        empty?.setOnClickListener {
            startEmailListActivity()
        }

        recyclerView = view.findViewById(R.id.recyclerview)
        setupSwipe()
        setupFAB()
        setupSnackBar()
        restartCacheLoader()
    }

    private fun startEmailListActivity() {
        val intent = Intent(activity, EmailAccountListActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNT_RQ_CODE)
    }

    fun restartFilterLoaders() {
        if (isMinusChat) {
            restartLoader()
        }
    }

    fun setupSwipe() {
        view?.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)?.let { refreshLayout ->
            refreshLayout.setOnRefreshListener {
                val mHandler = Handler()
                mHandler.postDelayed({
                    refreshLayout.isRefreshing = false
                }, 2000)
            }
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
        val messages = MainApplicationSingleton.getInstance(activity).getGlobalSBValueAsList(
            MsgsGrpPeopleEmailsFragment.TAG
        )
        val groups = MainApplicationSingleton.getInstance(activity).getGlobalSBValueAsList(
            MsgChatGrpContactsFragment.TAG
        )
        if (messages.isNullOrEmpty()) {
            restartLoader()
            return
        }
        if (counter > 0) {
            counter = 0
            restartLoader()
            Log.d(TAG, "quitHandlerThread: counter - $counter")
            return
        }
        this.messageItems = messages as ArrayList<MessageItem>?
        this.contactItems = groups as ArrayList<ContactItem>?
        createAsyncUpdateRecycleAdapter()
    }

    private fun restartLoader() {
        val fetchMsgsGrpPeopleTask = FetchMsgsGrpPeopleEmailsTask(
            filter, activity
        )
        fetchMsgsGrpPeopleTask.setFetchMsgsGrpPeopleTaskListener(this)
        fetchMsgsGrpPeopleTask.execute()
    }

    fun messageSend(intent: Intent) {
        restartLoader()
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

    private fun createAsyncUpdateRecycleAdapter() {
        resetAsyncUpdateRecycleAdapter()
        showActiveMessage()
    }

    private fun resetRecycleAdapter() {
        if (recyclerView == null && view != null) {
            recyclerView = view?.findViewById(R.id.recyclerview)
        }
        if (recyclerView == null) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL")
            return
        }

        if (messageItems == null && contactItems == null) return

        if (messageItems != null && messageItems!!.isNotEmpty()) {
            Collections.sort(messageItems, MessageItem.MessageItemComparator<MessageItem>(
                BaseItemComparator.SORT_MODE.DESC
            ))

            if (contactItems != null && contactItems!!.isNotEmpty()) {
                val contactItemsArray = contactItems!!.toTypedArray()
                for (contactItem in contactItemsArray) {
                    for (messageItem in messageItems!!) {
                        if (contactItem.dataId == messageItem.chatId) {
                            contactItems!!.remove(contactItem)
                        }
                    }
                }
            }
            val ids = HashSet<String>()
            for (messageItem in messageItems!!) {
                var text = messageItem.text
                if (TextUtils.isEmpty(text)) {
                    text = messageItem.latestMessageText
                }
                if (TextUtils.isEmpty(text)) {
                    ids.add(messageItem.dataId)
                }
            }
            if (ids.isNotEmpty())
                EmailService.asyncUpdateGetFullEmails(
                    user?.email, ids.toTypedArray(), user, context
                )
        }

        if (contactItems != null && contactItems!!.isNotEmpty())
            Collections.sort(contactItems, ContactItem.ContactItemComparator<ContactItem>(
                BaseItemComparator.SORT_MODE.DESC
            ))

        recyclerViewAdapter = RecyclerViewAdapter(messageItems, contactItems)
        recyclerViewAdapter?.setHasStableIds(true)
        recyclerView?.adapter = recyclerViewAdapter
        recyclerViewAdapter?.notifyDataSetChanged()
    }

    private fun resetAsyncUpdateRecycleAdapter() {
        if (recyclerView == null && view != null) {
            recyclerView = view?.findViewById(R.id.recyclerview)
        }
        if (recyclerView == null) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL")
            return
        }

        if (messageItems == null && contactItems == null) return

        if (messageItems != null && messageItems!!.isNotEmpty()) {
            Collections.sort(messageItems, MessageItem.MessageItemComparator<MessageItem>(
                BaseItemComparator.SORT_MODE.DESC
            ))

            if (contactItems != null && contactItems!!.isNotEmpty()) {
                val contactItemsArray = contactItems!!.toTypedArray()
                for (contactItem in contactItemsArray) {
                    for (messageItem in messageItems!!) {
                        if (contactItem.dataId == messageItem.chatId) {
                            contactItems!!.remove(contactItem)
                        }
                    }
                }
            }
            val ids = HashSet<String>()
            for (messageItem in messageItems!!) {
                var text = messageItem.text
                if (TextUtils.isEmpty(text)) {
                    text = messageItem.latestMessageText
                }
                if (TextUtils.isEmpty(text)) {
                    ids.add(messageItem.dataId)
                }
            }
            if (ids.isNotEmpty())
                EmailService.asyncUpdateGetFullEmails(
                    user?.email, ids.toTypedArray(), user, context
                )
        }

        if (contactItems != null && contactItems!!.isNotEmpty())
            Collections.sort(contactItems, ContactItem.ContactItemComparator<ContactItem>(
                BaseItemComparator.SORT_MODE.DESC
            ))

        recyclerViewAdapter = RecyclerViewAdapter(messageItems, contactItems)
        recyclerViewAdapter?.setHasStableIds(true)
        recyclerView?.adapter = recyclerViewAdapter
        recyclerViewAdapter?.notifyDataSetChanged()
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

        val intellibitzContactItem = sharedIntent?.getParcelableExtra<ContactItem>(
            ContactItem.INTELLIBITZ_CONTACT
        )
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

        val deviceContactItem = sharedIntent?.getParcelableExtra<ContactItem>(
            ContactItem.DEVICE_CONTACT
        )
        if (deviceContactItem != null) {
            // Handle device contact item if needed
        }

        sharedIntent = null
        return false
    }

    private fun processNewChatMessage(contactItem: ContactItem, context: Context): MessageItem? {
        val id = contactItem.dataId
        if (id != null) {
            Log.d(TAG, "User ready for Chat: $id")
            var item: MessageItem? = null
            if (messageItems != null && messageItems!!.isNotEmpty()) {
                item = MainApplicationSingleton.getBaseItem(id, messageItems)
            }
            if (item == null) {
                item = MessageItem()
                item?.dataId = MessageItem.TAG
            }
            item?.docOwner = user?.dataId
            item?.name = contactItem.name
            MessageChatContentProvider.createMessageChatThread(id, item)
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
        if (contactItem == null) return null
        val id = contactItem.intellibitzId
        if (id.isNullOrEmpty()) return null
        Log.d(TAG, "User ready for Chat: $id")
        var messageItem: MessageItem? = null
        if (messageItems != null && messageItems!!.isNotEmpty()) {
            messageItem = MainApplicationSingleton.getBaseItem(id, messageItems)
        }
        if (messageItem == null) {
            messageItem = MessageItem()
            messageItem?.dataId = MessageItem.TAG
        }
        messageItem?.docOwner = user?.dataId
        messageItem?.name = contactItem.name
        if (messageItem?.name == null) {
            messageItem?.name = messageItem?.dataId
        }
        if (contactItem.isGroup) {
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

    private fun showMessageThreadMessage(id: String, user: ContactItem) {
        if (id.isNotEmpty()) {
            val activeChat = MainApplicationSingleton.getBaseItem(id, messageItems)
            if (activeChat != null) {
                Handler(Looper.getMainLooper()).post {
                    showMessageThreadMessage(activeChat, user)
                }
            }
        }
    }

    private fun setupSnackBar() {
        view?.let {
            snackbar = Snackbar.make(
                it, "Please Add Account to see Emails", Snackbar.LENGTH_LONG
            )
            snackbar?.setAction("ADD EMAIL") { startEmailListActivity() }
        }
    }

    private fun setupFAB() {
        view?.findViewById<FloatingActionButton>(R.id.fab)?.let { fab ->
            fab.setOnClickListener { showNewBottomDialogFragment() }
        }
    }

    fun onBackPressed(): Boolean {
        return false
    }

    fun showContactSelectItemFragment() {
    }

    fun showNewBottomDialogFragment() {
        activity?.let { appCompatActivity ->
            newBottomDialogFragment = NewBottomDialogFragment()
            newBottomDialogFragment?.setNewBottomDialogListener(this)
            newBottomDialogFragment?.show((appCompatActivity as AppCompatActivity).supportFragmentManager, "Create")
        }
    }

    fun onMessageForward(intent: Intent) {
        if (intent == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            activity?.onBackPressed()
            return
        }
        messageItemToForward = intent.getParcelableExtra(MessageItem.TAG)
        val source = intent.action
        if (messageItemToForward == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            activity?.onBackPressed()
            return
        }
    }

    fun showContactThreadDetail(intent: Intent) {
        startMsgChatGrpContactsDetailActivity()
    }

    override fun onNewDialogClose(newBottomDialogFragment: NewBottomDialogFragment) {
        this.newBottomDialogFragment?.dismiss()
    }

    override fun onNewGroup(newBottomDialogFragment: NewBottomDialogFragment) {
        onNewDialogClose(newBottomDialogFragment)
        contactItem = ContactItem()
        contactItem?.isNewGroup = true
        contactItem?.isGroup = true
        startMsgChatGrpContactsDetailActivity()
    }

    override fun onNewChat(newBottomDialogFragment: NewBottomDialogFragment) {
        onNewDialogClose(newBottomDialogFragment)
        contactItem = ContactItem()
        contactItem?.isNewGroup = false
        contactItem?.isGroup = false
        startContactSelectActivity()
    }

    private fun startContactSelectActivity() {
        val intent = Intent(activity, ContactSelectActivity::class.java)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE)
    }

    fun startMsgChatGrpContactsDetailActivity() {
        val intent = Intent(activity, MsgChatGrpContactsDetailActivity::class.java)
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

    fun onOkPressed(intent: Intent) {
        if (intent == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            activity?.onBackPressed()
            return
        }
        val item = intent.getParcelableExtra<ContactItem>(ContactItem.TAG)
        val source = intent.action
        if (item == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            if (IntellibitzContactSelectItemFragment.TAG.equals(source)) {
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
        if (IntellibitzContactSelectItemFragment.TAG.equals(source)) {
            if (0 == size) {
                Log.e(TAG, " selected contacts is 0 by pressing ok.. cancelling : ")
                activity?.onBackPressed()
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
        if (IntellibitzContactSelectItemFragment.TAG.equals(source)) {
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
            // Handle single chat
        } else {
            val name = contactItem?.name
            if (name.isNullOrEmpty()) {
                Log.e(TAG, "Name is empty - cannot group chat")
                activity?.onBackPressed()
                return
            }
            contactItem?.isGroup = true
            contactItem?.type = "GROUP"
            Log.e(TAG, " selected contacts for Group chat: $count")
        }
        createNewChat(contactItem, user)
    }

    override fun onNewEmail(newBottomDialogFragment: NewBottomDialogFragment) {
        if (TextUtils.isEmpty(user?.email)) {
            UserEmailContentProvider.populateUserEmailsJoinById(user, activity)
        }
        onNewDialogClose(newBottomDialogFragment)
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
            activity?.supportFragmentManager, "NewMessageDialog"
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
        setSelectedItem(item)
        if (item == null) {
            val contactItem = MainApplicationSingleton.getBaseItem(messageThreadId, contactItems)
            if (contactItem != null) {
                createNewChat(contactItem, user)
                return
            }
        }
        if (item == null) {
            showMessageThreadMessage(messageThreadId)
        } else {
            if (item.isLocked) {
                showUnLockPwdDialog()
            } else {
                showMessageThreadMessage(item, user)
            }
        }
    }

    private fun showMessageThreadMessage(messageThreadId: String) {
        val messageItem = MessageItem()
        messageItem.dataId = messageThreadId
        showMessageThreadMessage(messageItem, user)
    }

    private fun showMessageThreadMessage(contactItem: ContactItem, user: ContactItem) {
        val id = contactItem.dataId
        showMessageThreadMessage(id, user)
    }

    private fun showMessageThreadMessage(messageItem: MessageItem?, user: ContactItem?) {
        if (peopleTopicListener == null) {
            return
        }
        if (messageItem?.isLocked == true) {
            showUnLockPwdDialog()
            return
        }
        val id = messageItem?.dataId
        if (id != null && id.isNotEmpty() && !MessageItem.TAG.equals(id)) {
            if (messageItem?.isEmailItem == true) {
                MessageEmailContentProvider.queryMessageEmailThreadFullJoin(
                    messageItem, activity
                )
            } else {
                if (messageItem?.isGroup == true) {
                    MessageChatGroupContentProvider.queryMessageChatThreadFullJoin(
                        messageItem, activity
                    )
                } else {
                    MessageChatContentProvider.queryMessageChatThreadFullJoin(
                        messageItem, activity
                    )
                }
            }
        }
        if (messageItem?.isLocked == true) {
            showUnLockPwdDialog()
        } else {
            peopleTopicListener?.onPeopleTopicClicked(messageItem, user)
        }
    }

    private fun setSelectedItem(mItem: BaseItem?) {
        if (mItem is MessageItem)
            selectedItem = mItem
    }

    private fun execDeleteMsgsTask(msgs: Array<String>) {
        val deleteMsgsTask = DeleteMsgsTask(msgs, user?.dataId, user?.token,
            user?.device, user?.deviceRef, MainApplicationSingleton.AUTH_DELETED_MSGS, context)
        deleteMsgsTask.requestTimeoutMillis = 30000
        deleteMsgsTask.setDeleteMsgsTaskListener(this)
        deleteMsgsTask.execute()
    }

    private fun deleteMessages() {
        execDeleteMsgsTask(arrayOf(selectedItem?.dataId ?: ""))
    }

    private fun lockMessages() {
        try {
            val count = MsgsGrpPeopleContentProvider.lockMsgs(arrayOf(selectedItem?.dataId ?: ""), context)
            val messageItem = MainApplicationSingleton.getBaseItem(selectedItem?.dataId ?: "", messageItems)
            messageItem?.isLocked = true
            restartLoader()
            Log.e(TAG, "lockMessages:$count")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun showUnLockPwdDialog() {
        UnLockPasswordFragment.newInstance(user, this).show(
            appCompatActivity?.supportFragmentManager, "UnLockPasswordDialog"
        )
    }

    private fun unlockMessages() {
        try {
            val count = MsgsGrpPeopleContentProvider.unlockMsgs(arrayOf(selectedItem?.dataId ?: ""), context)
            val messageItem = MainApplicationSingleton.getBaseItem(selectedItem?.dataId ?: "", messageItems)
            messageItem?.isLocked = false
            restartLoader()
            Log.e(TAG, "unlockMessages:$count")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun flagMessages() {
        // Implement flagMessages if needed
    }

    override fun onPostDeleteMsgsResponse(response: JSONObject, item: Array<String>) {
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (1 == status) {
                Log.e(TAG, "Delete Msgs SUCCESS - $response")
                try {
                    val count = MsgsGrpPeopleContentProvider.deleteMsgs(item, context)
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

    override fun onPostDeleteMsgsErrorResponse(response: JSONObject, item: Array<String>) {
        Log.e(TAG, "Delete Msgs FAIL - $response: ${Arrays.toString(item)}")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is NewEmailDialogFragment) {
            val newEmailDialogFragment = dialog
            if (newEmailDialogFragment.isChatMode()) {
                performNewChat(newEmailDialogFragment)
            } else {
                performNewEmail(newEmailDialogFragment)
            }
        } else if (dialog is UnLockPasswordFragment) {
            unlockMessages()
        }
    }

    private fun performNewEmail(newEmailDialogFragment: NewEmailDialogFragment) {
        val email = user?.email
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "performNewEmail: User email is empty - No email account signed up")
            return
        }
        val messageItem = MessageItem()
        val contactThreadItem = messageItem.contactItem ?: ContactItem().also { messageItem.contactItem = it }
        messageItem.dataId = MessageItem.TAG
        messageItem.baseType = MessageItem.THREAD
        messageItem.docType = MessageItem.MSG
        messageItem.type = MessageItem.EMAIL
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
        contactThreadItem.addContact(contactItem)
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
        LocalBroadcastManager.getInstance(activity?.applicationContext).sendBroadcast(intent)
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

    override fun onFetchMsgsGrpPeopleTaskExecute(messageItems: ArrayList<MessageItem>) {
        setMessageItems(messageItems)
        createRecycleAdapter()
    }

    override fun onFetchMsgsGrpPeopleTaskExecuteFail(messageItems: ArrayList<MessageItem>) {
        setMessageItems(messageItems)
        createRecycleAdapter()
        Log.e(TAG, "onFetchMsgsGrpPeopleTaskExecuteFail")
    }

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ChatService.MSG_SHOW_TYPING -> {
                    if (peopleDetailListener != null) {
                        try {
                            val obj = msg.obj as String
                            if (obj != null) {
                                val jsonArray = JSONArray(obj)
                                val jsonObject = jsonArray.getJSONObject(0)
                                try {
                                    val toUid = jsonObject.getString("to_uid")
                                    val fromUid = jsonObject.getString("from_uid")
                                    val fromName = jsonObject.getString("from_name")
                                    showTyping(toUid, fromUid, fromName, user ?: ContactItem())
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

    inner class RecyclerViewAdapter(
        private val messageItems: List<MessageItem>?,
        private val contactItems: List<ContactItem>?
    ) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private val baseItems = ArrayList<BaseItem>()
        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        init {
            if (messageItems != null) baseItems.addAll(messageItems)
            if (contactItems != null) baseItems.addAll(contactItems)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_msgsgrppeople_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItem = baseItems[position]
            holder.tvId.text = holder.mItem.dataId

            var locked = false
            var chat = false
            var groupchat = false
            var name = ""
            var subject = ""
            var text = ""
            var from = ""
            var to = ""
            var cc = ""
            var singleAddress = ""
            var self = false
            var delivered = false
            var read = false
            var typing = false
            var attachments = false
            val pic = holder.mItem.profilePic
            var unreadCount = 0

            if (holder.mItem is MessageItem) {
                val messageItem = holder.mItem as MessageItem
                locked = messageItem.isLocked
                chat = messageItem.isChat
                groupchat = messageItem.isGroupChat
                subject = messageItem.subject

                name = holder.mItem.name
                if (TextUtils.isEmpty(from) && !TextUtils.isEmpty(name)) from = name
                if (TextUtils.isEmpty(from) && !TextUtils.isEmpty(messageItem.displayName)) from = messageItem.displayName
                if (TextUtils.isEmpty(from) && !TextUtils.isEmpty(holder.mItem.firstName)) {
                    from = holder.mItem.firstName
                    if (!TextUtils.isEmpty(holder.mItem.lastName)) {
                        from += " ${holder.mItem.lastName}"
                    }
                }
                if (TextUtils.isEmpty(from) && !TextUtils.isEmpty(holder.mItem.intellibitzId)) {
                    from = holder.mItem.intellibitzId
                }
                to = messageItem.to
                cc = messageItem.cc
                singleAddress = MessageItem.getSingleAddress(from, to, cc, user?.email, user?.name)
                self = user?.dataId == messageItem.docSenderEmail
                delivered = messageItem.isDelivered
                read = messageItem.isRead

                if (groupchat) {
                    from = name
                } else if (chat) {
                    subject = holder.mItem.name
                    if (TextUtils.isEmpty(subject) && !TextUtils.isEmpty(holder.mItem.displayName)) {
                        subject = holder.mItem.displayName
                    }
                    if (TextUtils.isEmpty(subject) && !TextUtils.isEmpty(holder.mItem.firstName)) {
                        subject = holder.mItem.firstName
                        if (!TextUtils.isEmpty(holder.mItem.lastName)) {
                            subject += " ${holder.mItem.lastName}"
                        }
                    }
                } else {
                    if (TextUtils.isEmpty(subject) && !TextUtils.isEmpty(holder.mItem.docOwner)) {
                        subject = holder.mItem.docOwner
                    }
                }

                typing = messageItem.isTyping
                if (typing && !TextUtils.isEmpty(messageItem.typingText)) {
                    text = messageItem.typingText
                } else {
                    var latestMessageText = messageItem.latestMessageText
                    if (TextUtils.isEmpty(latestMessageText)) latestMessageText = ""
                    if (!TextUtils.isEmpty(messageItem.text)) {
                        latestMessageText = messageItem.text
                    }
                    if (chat) {
                        text = latestMessageText
                    } else {
                        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(singleAddress) &&
                            text.equals(singleAddress, ignoreCase = true)) {
                            text = latestMessageText
                        } else {
                            text = latestMessageText
                        }
                    }
                    text = text.replace("\n", " ")
                }

                attachments = messageItem.hasAttachments
                unreadCount = messageItem.unreadCount
            }
            if (holder.mItem is ContactItem) {
                val contactItem = holder.mItem as ContactItem
                subject = contactItem.name
            }

            val lock = holder.mView.findViewById<View>(R.id.ll_msgsgrppeople_rv_lock)
            val unlock = holder.mView.findViewById<View>(R.id.ll_msgsgrppeople_rv)

            if (locked) {
                lock.visibility = View.VISIBLE
                unlock.visibility = View.GONE
                setSelectedItem(holder.mItem)
                return
            } else {
                lock.visibility = View.GONE
                unlock.visibility = View.VISIBLE
            }

            var checks: Drawable? = null
            if (self && chat) {
                if (delivered) {
                    checks = getDrawable(R.drawable.ic_done_all_black_18dp, activity?.theme)
                } else if (read) {
                    checks = getDrawable(R.drawable.ic_done_black_18dp, activity?.theme)
                } else {
                    checks = getDrawable(R.drawable.ic_restore_black_18dp, activity?.theme)
                }
            }
            var drawable = getDrawable(R.drawable.ic_mail_outline_black_18dp, activity?.theme)
            if (chat) {
                drawable = getDrawable(R.drawable.ic_chat_bubble_outline_black_18dp, activity?.theme)
                if (groupchat) {
                    drawable = getDrawable(R.drawable.ic_question_answer_black_18dp, activity?.theme)
                }
            }
            if (drawable != null) {
                drawable.setBounds(Rect(0, 0, 20, 20))
                if (checks != null) {
                    checks.setBounds(Rect(0, 0, 20, 20))
                }
            }

            holder.tvTo.visibility = View.GONE
            holder.tvSubject.visibility = View.GONE

            if (TextUtils.isEmpty(from)) from = ""
            if (!TextUtils.isEmpty(from)) {
                holder.tvFrom.text = from
                holder.tvFrom.visibility = View.VISIBLE
            } else {
                holder.tvFrom.visibility = View.GONE
            }

            if (TextUtils.isEmpty(singleAddress) || !singleAddress.equals(from, ignoreCase = true)) {
                holder.tvTo.text = singleAddress
                holder.tvTo.visibility = View.VISIBLE
            } else {
                holder.tvTo.text = ""
                holder.tvTo.visibility = View.GONE
            }
            if (TextUtils.isEmpty(subject) || !subject.equals(from, ignoreCase = true)) {
                holder.tvSubject.text = subject
                if (TextUtils.isEmpty(subject)) {
                    holder.tvSubject.visibility = View.GONE
                } else {
                    setCompoundDrawablesRelative(holder.tvSubject, drawable, null, null, checks)
                    holder.tvSubject.visibility = View.VISIBLE
                }
            } else {
                holder.tvSubject.text = ""
                holder.tvSubject.visibility = View.GONE
            }
            holder.tvMessage.text = text
            if (TextUtils.isEmpty(text)) {
                holder.tvMessage.visibility = View.GONE
            } else {
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, checks)
                holder.tvMessage.visibility = View.VISIBLE
            }

            if (attachments) {
                drawable = getDrawable(R.drawable.ic_attach_file_black_18dp, activity?.theme)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                holder.ivAttachment.visibility = View.VISIBLE
            } else {
                holder.ivAttachment.visibility = View.INVISIBLE
            }

            val createDate = holder.mItem.timestamp * 1000
            val timestamp = Timestamp(createDate)
            val now = System.currentTimeMillis()
            var df: DateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            if ((now - createDate) < 1000 * 60 * 60 * 24) {
                df = SimpleDateFormat("hh mm a", Locale.getDefault())
            }
            val dt = df.format(timestamp.time)
            holder.tvTimestamp.text = dt

            val generator = ColorGenerator.MATERIAL
            if (TextUtils.isEmpty(from)) from = subject
            if (TextUtils.isEmpty(from)) from = "C"
            val color2 = generator.getColor(from)

            val builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round()
            val ic2 = builder.build(from.substring(0, 1), color2)

            if (pic.isNullOrEmpty()) {
                // Handle empty pic
            } else if (pic.startsWith("http")) {
                bitmapFromUrlTask = BitmapFromUrlTask(holder.tvSender, pic, activity?.applicationContext)
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

            if (unreadCount == 0) {
                holder.ivUnreadCount.visibility = View.INVISIBLE
                holder.mView.setBackgroundResource(R.drawable.selectable_item_bg)
            } else {
                holder.mView.setBackgroundResource(R.drawable.item_gray_bg_layer)
                val badge = unreadCount.toString()
                val unreadBuilder = TextDrawable.builder()
                    .beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .roundRect(20)
                val unreadDrawable = unreadBuilder.build(badge, Color.RED)
                holder.ivUnreadCount.setImageDrawable(unreadDrawable)
                holder.ivUnreadCount.visibility = View.VISIBLE
            }
        }

        override fun getItemId(position: Int): Long {
            return baseItems[position]._id
        }

        override fun getItemCount(): Int {
            return baseItems.size
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap, view: View, context: Context) {
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

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
            val mView: View = view
            val tvId: TextView = view.findViewById(R.id.tv_id)
            val tvTo: TextView = view.findViewById(R.id.tv_to)
            val tvSubject: TextView = view.findViewById(R.id.tv_subject)
            val tvFrom: TextView = view.findViewById(R.id.tv_from)
            val tvMessage: TextView = view.findViewById(R.id.tv_text)
            val tvTimestamp: TextView = view.findViewById(R.id.tv_timestamp)
            val tvSender: ImageView = view.findViewById(R.id.tv_sender)
            val ivUnreadCount: ImageView = view.findViewById(R.id.iv_unread_count)
            val ivAttachment: ImageView = view.findViewById(R.id.iv_attach)
            var mItem: BaseItem? = null

            init {
                view.setOnLongClickListener(this)
                mView.setOnClickListener(this@MsgsGrpPeopleEmailsFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvMessage.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                setSelectedItem(mItem)
                if (mActionMode == null) {
                    mActionMode = (activity as AppCompatActivity).startSupportActionMode(mActionModeCallback)
                    v.isSelected = !v.isSelected
                    return true
                }
                v.isSelected = !v.isSelected
                return false
            }
        }
    }
}
