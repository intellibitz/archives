package intellibitz.intellidroid.fragment

import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.os.Parcelable
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
import intellibitz.intellidroid.bean.BaseItemComparator
import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider
import intellibitz.intellidroid.content.task.FetchMsgsGrpClutterTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.domain.account.EmailAccountListActivity
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ClutterListener
import intellibitz.intellidroid.listener.ClutterTopicListener
import intellibitz.intellidroid.service.EmailService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.DeleteMsgsTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import intellibitz.intellidroid.widget.NewEmailDialogFragment
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashSet
import java.util.List
import java.util.Locale

class MsgsGrpClutterFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
    AdapterView.OnItemClickListener,
    View.OnClickListener,
    DeleteMsgsTask.DeleteMsgsTaskListener,
    FetchMsgsGrpClutterTask.FetchMsgsGrpClutterTaskListener {

    companion object {
        const val TAG = "MsgsGrpClutterFrag"
    }

    private val lock = Any()
    private var snackbar: Snackbar? = null
    private var fab: FloatingActionButton? = null
    private var clutterTopicListener: ClutterTopicListener? = null
    private val contactEmailReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val contact = intent.getStringExtra(MainApplicationSingleton.EMAIL_PARAM)
            Log.d(TAG, "received contact: $contact")
            showNewEmailMessage(contact, intent)
        }
    }
    private val contactPhoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val contact = intent.getStringExtra(MainApplicationSingleton.MOBILE_PARAM)
            Log.d(TAG, "received contact: $contact")
            showNewChatMessage(contact)
        }
    }
    private val newEmailReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val user = intent.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
            val messageItem = intent.getParcelableExtra<MessageItem>(MessageItem.TAG)
            showNewEmailMessage(messageItem, user)
        }
    }
    private var filter: String? = null
    private var messageContentObserver: ContentObserver? = null
    private var rvMessages: RecyclerView? = null
    private var rvMessagesAdapter: RecyclerViewAdapter? = null
    private var selectedItem: MessageItem? = null
    private var sharedIntent: Intent? = null
    private var contactItem: ContactItem? = null
    private var messageItems: ArrayList<MessageItem>? = null
    private var counter = 0
    private var looperThread: HandlerThread? = null
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
            when (item.itemId) {
                R.id.menu_delete -> {
                    deleteMessages()
                    mode.finish()
                    return true
                }
                R.id.menu_moveto -> {
                    mode.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
        }
    }

    constructor() : super()

    fun setTwoPane(twoPane: Boolean) {
        this.twoPane = twoPane
    }

    fun setMessageItems(items: List<MessageItem>?) {
        this.messageItems = items as ArrayList<MessageItem>?
        if (messageItems != null && !messageItems!!.isEmpty()) {
            MainApplicationSingleton.getInstance(activity).putGlobalVariable(
                MsgsGrpClutterFragment.TAG, this.messageItems
            )
        }
    }

    fun showEmpty() {
        empty?.let {
            it.visibility = View.VISIBLE
            it.setText(R.string.empty_email_account)
        }
    }

    fun hideEmpty() {
        empty?.visibility = View.GONE
    }

    private fun unregisterReceiver() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(newEmailReceiver)
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(contactPhoneReceiver)
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(contactEmailReceiver)
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(activity).registerReceiver(
            newEmailReceiver,
            IntentFilter(MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK)
        )
        LocalBroadcastManager.getInstance(activity).registerReceiver(
            contactPhoneReceiver,
            IntentFilter(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED)
        )
        LocalBroadcastManager.getInstance(activity).registerReceiver(
            contactEmailReceiver,
            IntentFilter(MainApplicationSingleton.BROADCAST_CONTACT_EMAIL_SELECTED)
        )
    }

    fun setClutterTopicListener(messageTopicListener: ClutterTopicListener?) {
        this.clutterTopicListener = messageTopicListener
    }

    override fun onDestroy() {
        activity.contentResolver.unregisterContentObserver(messageContentObserver)
        unregisterReceiver()
        super.onDestroy()
    }

    override fun onDetach() {
        activity.contentResolver.unregisterContentObserver(messageContentObserver)
        unregisterReceiver()
        super.onDetach()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerReceiver()
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
                            Thread.sleep(3000)
                        } catch (ignored: InterruptedException) {
                            Log.e(TAG, ignored.message)
                        }
                        quitHandlerThread()
                    }
                }
            }
        }
        activity.contentResolver.registerContentObserver(
            MsgsGrpClutterContentProvider.CONTENT_URI, true,
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
            restartLoader()
            counter = 0
            Log.d(TAG, "quitHandlerThread: counter - $counter")
        }
    }

    fun onNewMenuClicked() {
        showNewMessageDialog()
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_msgsgrpclutter, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }

        empty = view.findViewById(R.id.tv_empty)
        empty?.setOnClickListener {
            startEmailListActivity()
        }

        rvMessages = view.findViewById(R.id.recyclerview)
        setupSwipe()

        setupFAB(view)
        setupSnackBar(view)
        restartCacheLoader()
    }

    private fun startEmailListActivity() {
        val intent = Intent(activity, EmailAccountListActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNT_RQ_CODE)
    }

    fun emptyOnClick(view: View) {
        startEmailListActivity()
    }

    fun setupSwipe() {
        view?.let { view ->
            val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
            refreshLayout?.setOnRefreshListener {
                val mHandler = Handler()
                mHandler.postDelayed({
                    refreshLayout.isRefreshing = false
                }, 2000)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(@Nullable savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            user = it.getParcelable(ContactItem.USER_CONTACT)
        }
    }

    private fun restartLoader(uri: Uri) {
        try {
            val id = ContentUris.parseId(uri)
            messageItems = null
            restartLoader()
        } catch (e: NumberFormatException) {
        }
    }

    protected fun restartLoader(filter: String) {
        this.filter = filter
        restartLoader()
    }

    private fun restartCacheLoader() {
        val objects = MainApplicationSingleton.getInstance(activity).getGlobalSBValueAsList(
            MsgsGrpClutterFragment.TAG
        )
        if (null == objects) {
            restartLoader()
            return
        }
        this.messageItems = objects as ArrayList<MessageItem>?
        createAsyncUpdateRecycleAdapter()
    }

    private fun restartLoader() {
        val fetchMsgsGrpClutterTask = FetchMsgsGrpClutterTask(
            filter, activity
        )
        fetchMsgsGrpClutterTask.setFetchMsgsGrpClutterTaskListener(this)
        fetchMsgsGrpClutterTask.execute()
    }

    fun messageSend(shared: Intent) {
        restartLoader()
    }

    private fun createRecycleAdapter() {
        if (null == rvMessages && view != null) {
            rvMessages = view?.findViewById(R.id.recyclerview)
        }
        if (null == rvMessages) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL")
            return
        }

        if (null == messageItems) return
        if (!messageItems!!.isEmpty()) {
            Collections.sort(messageItems, MessageItem.MessageItemComparator<>(
                BaseItemComparator.SORT_MODE.DESC_DT
            ))
        }

        rvMessagesAdapter = RecyclerViewAdapter(messageItems!!)
        rvMessagesAdapter?.setHasStableIds(true)
        rvMessages?.adapter = rvMessagesAdapter
        rvMessagesAdapter?.notifyDataSetChanged()
    }

    private fun createAsyncUpdateRecycleAdapter() {
        if (null == rvMessages && view != null) {
            rvMessages = view?.findViewById(R.id.recyclerview)
        }
        if (null == rvMessages) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL")
            return
        }

        if (null == messageItems) return
        if (!messageItems!!.isEmpty()) {
            Collections.sort(messageItems, MessageItem.MessageItemComparator<>(
                BaseItemComparator.SORT_MODE.DESC_DT
            ))
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
            if (!ids.isEmpty())
                EmailService.asyncUpdateGetFullEmails(
                    user.email, ids.toTypedArray(), user, context
                )
        }

        rvMessagesAdapter = RecyclerViewAdapter(messageItems!!)
        rvMessagesAdapter?.setHasStableIds(true)
        rvMessages?.adapter = rvMessagesAdapter
        rvMessagesAdapter?.notifyDataSetChanged()
    }

    private fun processNewMessageFromSharedIntent(): Boolean {
        if (null == sharedIntent) return false
        if (null == sharedIntent?.extras) return false
        val intellibitzContactItem = sharedIntent?.getParcelableExtra<ContactItem>(
            ContactItem.INTELLIBITZ_CONTACT
        )
        if (null == intellibitzContactItem) {
            val contactItem = sharedIntent?.getParcelableExtra<ContactItem>(
                ContactItem.TAG
            )
            if (null == contactItem) {
            } else {
                MainApplicationSingleton.performOnUIHandlerThread {
                    sharedIntent = null
                    contactItem.isGroup = true
                    contactItem.type = "GROUP"
                }
                return true
            }

        } else {
            MainApplicationSingleton.performOnUIHandlerThread {
                sharedIntent = null
                contactItem = ContactItem()
                val contactItem = ContactItem(intellibitzContactItem)
                this.contactItem?.dataId = contactItem.intellibitzId
                this.contactItem?.name = contactItem.name
                this.contactItem?.addContact(contactItem)
                this.contactItem?.isGroup = false
                this.contactItem?.type = "USER"
            }
            return true
        }
        sharedIntent = null
        return false
    }

    fun onBackPressed(): Boolean {
        return false
    }

    fun onMessageForward(intent: Intent) {

    }

    fun onOkPressed(intent: Intent?) {
        if (null == intent) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            activity.onBackPressed()
            return
        }
        val item = intent.getParcelableExtra<ContactItem>(
            ContactItem.TAG
        )
        val source = intent.action
        if (null == item) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            if (ContactSelectFragment.TAG.equals(source)) {
                if (contactItem?.isNewGroup == true) {
                    return
                }
            }
            Log.e(TAG, " cancelled group by pressing back: ")
            return
        }
        if (null == contactItem) contactItem = item
        if (contactItem == item) {
            Log.e(TAG, " Already selected contacts: " +
                    contactItem?.selectedContacts?.size)
        } else {
            contactItem?.selectedContacts = item.selectedContacts
        }
        contactItem?.mergeSelectedContacts()
        val count = contactItem?.contactItems?.size
        if (ContactSelectFragment.TAG.equals(source)) {
            if (contactItem?.isNewGroup == true) {
                return
            } else {
                if (1 == count) {
                    val contactItem =
                        this.contactItem?.contactItems?.iterator()?.next()
                    this.contactItem?.dataId = contactItem?.intellibitzId
                    this.contactItem?.name = contactItem?.name
                    this.contactItem?.isGroup = false
                    this.contactItem?.type = "USER"
                    return
                }
            }
        }

        if (count!! > 1) {
            val name = contactItem?.name
            if (null == name || name.isEmpty()) {
                Log.e(TAG, "Name is empty - cannot group chat")
                activity.onBackPressed()
                return
            }
            contactItem?.isGroup = true
            contactItem?.type = "GROUP"
            Log.e(TAG, " selected contacts for Group chat: $count")
            return
        }
        if (1 == count) {
            val contactItem =
                this.contactItem?.contactItems?.iterator()?.next()
            this.contactItem?.dataId = contactItem?.intellibitzId
            this.contactItem?.name = contactItem?.name
            this.contactItem?.isGroup = false
            this.contactItem?.type = "USER"
            return
        }
        Log.e(TAG, " selected contacts: $count")
        activity.onBackPressed()
        return
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
        showMessageThreadMessage(messageThreadId)
    }

    private fun showNewEmailMessage(item: MessageItem?, user: ContactItem?) {
        val messageItem = MessageItem()
        messageItem.dataId = MessageItem.TAG
        messageItem.from = this.user.name
        messageItem.docOwnerEmail = this.user.email
        messageItem.docSenderEmail = this.user.email
        messageItem.subject = item?.subject
        MessageItem.setMessageThreadEmailAddress(messageItem,
            item?.to, item?.cc, item?.bcc)
        showMessageThreadMessage(messageItem, user)
    }

    private fun showNewEmailMessage(contact: String, shared: Intent) {
        showMessageThreadMessage(MessageItem.TAG, contact, shared)
    }

    private fun showNewChatMessage(contact: String) {
        showMessageThreadMessage(MessageItem.TAG, contact, null)
    }

    private fun showMessageThreadMessage(messageThreadId: String, contact: String, shared: Intent?) {
        val messageItem = MessageItem()
        messageItem.dataId = messageThreadId
        messageItem.from = user.name
        messageItem.docOwnerEmail = user.email
        messageItem.docSenderEmail = user.email
        MessageItem.setMessageThreadEmailAddress(messageItem,
            contact, null, null)

        if (shared != null) {
            val sharedText = shared.getStringExtra(Intent.EXTRA_TEXT)
            val imageUri = shared.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            val imageUris = shared.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            val deviceContactItem = shared.getParcelableExtra<ContactItem>(ContactItem.DEVICE_CONTACT)
            messageItem.sharedText = sharedText
            messageItem.sharedUri = imageUri
            messageItem.sharedUris = imageUris
            messageItem.sharedDeviceContactItem = deviceContactItem
        }
        showMessageThreadMessage(messageItem, user)
    }

    private fun showMessageThreadMessage(messageThreadId: String) {
        val messageItem = MessageItem()
        messageItem.dataId = messageThreadId
        showMessageThreadMessage(messageItem, user)
    }

    private fun showMessageThreadMessage(messageItem: MessageItem, user: ContactItem?) {
        if (null == clutterTopicListener) {
            Log.e(TAG, "clutterTopicListener is NULL")
            return
        }
        val id = messageItem.dataId
        if (id != null && !id.isEmpty() && !MessageItem.TAG.equals(id)) {
            MsgsGrpClutterContentProvider.query(messageItem, activity)
        }
        clutterTopicListener?.onClutterTopicClicked(messageItem, user)
    }

    private fun setSelectedItem(mItem: MessageItem) {
        selectedItem = mItem
    }

    private fun execDeleteMsgsTask(msgs: Array<String>) {
        val deleteMsgsTask = DeleteMsgsTask(msgs, user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_DELETED_MSGS, context)
        deleteMsgsTask.requestTimeoutMillis = 30000
        deleteMsgsTask.setDeleteMsgsTaskListener(this)
        deleteMsgsTask.execute()
    }

    private fun deleteMessages() {
        execDeleteMsgsTask(arrayOf(selectedItem?.dataId!!))
    }

    override fun onPostDeleteMsgsResponse(response: JSONObject, item: Array<String>) {
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (1 == status) {
                Log.e(TAG, "Delete Msgs SUCCESS - $response")
                try {
                    val count = MsgsGrpClutterContentProvider.deleteMsgs(item, context)
                    Log.e(TAG, "Delete in DB: $count")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (-1 == status) {
                onPostDeleteMsgsErrorResponse(response, item)
            } else if (99 == status) {
                onPostDeleteMsgsErrorResponse(response, item)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPostDeleteMsgsErrorResponse(response: JSONObject, item: Array<String>) {
        Log.e(TAG, "Delete Msgs FAIL - $response: ${Arrays.toString(item)}")
    }

    private fun setupSnackBar(view: View) {
        snackbar = Snackbar.make(
            view, R.string.empty_email_account, Snackbar.LENGTH_LONG
        )
        snackbar?.setAction("ADD EMAIL") {
            startEmailListActivity()
        }
    }

    private fun setupFAB(view: View) {
        fab = view.findViewById(R.id.fab)
        fab?.setOnClickListener {
            showNewMessageDialog()
        }
    }

    fun showNewMessageDialog() {
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
            activity.supportFragmentManager, "NewMessageDialog"
        )
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        val newEmailDialogFragment = dialog as NewEmailDialogFragment
        if (newEmailDialogFragment.isChatMode) {
        } else {
            performNewEmail(newEmailDialogFragment)
        }
    }

    private fun performNewEmail(newEmailDialogFragment: NewEmailDialogFragment) {
        val messageItem = MessageItem()
        messageItem.dataId = MessageItem.TAG
        messageItem.baseType = "THREAD"
        messageItem.docType = "THREAD"
        messageItem.dataRev = "1"
        messageItem.from = user.name
        messageItem.subject = newEmailDialogFragment.subject
        messageItem.docOwner = user.docOwner
        messageItem.docSender = user.name
        messageItem.docOwnerEmail = user.email
        messageItem.docSenderEmail = user.email
        messageItem.timestamp = System.currentTimeMillis()
        val to = newEmailDialogFragment.to
        val cc = newEmailDialogFragment.cc
        val bcc = newEmailDialogFragment.bcc
        MessageItem.setMessageThreadEmailAddress(messageItem, to, cc, bcc)
        messageItem.docOwner = user.docOwner
        messageItem.docSender = user.name
        messageItem.docOwnerEmail = user.email
        messageItem.docSenderEmail = user.email
        messageItem.timestamp = System.currentTimeMillis()

        val intent = Intent()
        if (newEmailDialogFragment.isChatMode) {
            intent.action = MainApplicationSingleton.BROADCAST_NEW_CHAT_DIALOG_OK
        } else {
            performNewEmail(newEmailDialogFragment)
            intent.action = MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK
        }
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }

    fun onDetailQueryTextSubmit(query: String): Boolean {
        return onQueryTextSubmit(query)
    }

    fun onDetailQueryTextChange(newText: String): Boolean {
        return onQueryTextChange(newText)
    }

    fun onDetailClose(): Boolean {
        return onClose()
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

    override fun onFetchMsgsGrpClutterTaskExecute(messageItems: ArrayList<MessageItem>) {
        setMessageItems(messageItems)
        createRecycleAdapter()
    }

    override fun onFetchMsgsGrpClutterTaskExecuteFail(messageItems: ArrayList<MessageItem>) {
        setMessageItems(messageItems)
        createRecycleAdapter()
        Log.e(TAG, "onFetchMsgsGrpClutterTaskExecuteFail")
    }

    inner class RecyclerViewAdapter(private val messageItems: List<MessageItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_msgsgrpclutter_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.messageItem = messageItems[position]

            holder.tvId.text = holder.messageItem.dataId

            var name = holder.messageItem.name
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.messageItem.displayName))
                name = holder.messageItem.displayName
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.messageItem.firstName)) {
                name = holder.messageItem.firstName
                if (!TextUtils.isEmpty(holder.messageItem.lastName)) {
                    name += " " + holder.messageItem.lastName
                }
            }
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.messageItem.intellibitzId)) {
                name = holder.messageItem.intellibitzId
            }
            holder.tvName.text = name

            holder.tvId.text = holder.messageItem.dataId
            var from = holder.messageItem.docSender
            if (null == from)
                from = holder.messageItem.from
            val to = holder.messageItem.to
            val cc = holder.messageItem.cc
            var val = MessageItem.getSingleAddress(name, to, cc,
                user.email, user.name)
            var drawable = getDrawable(
                R.drawable.ic_person_outline_black_18dp, activity.theme
            )
            drawable?.setBounds(Rect(0, 0, 20, 20))

            if (MainApplicationSingleton.isAPI17()) {
                holder.tvFrom.setCompoundDrawablesRelative(drawable, null, null, null)
            } else {
                holder.tvFrom.setCompoundDrawables(drawable, null, null, null)
            }
            holder.tvFrom.text = val

            val = holder.messageItem.subject
            if (holder.messageItem.hasAttachments) {
                drawable = getDrawable(
                    R.drawable.ic_link_black_18dp, activity.theme
                )
                drawable?.setBounds(Rect(0, 0, 20, 20))
                holder.tvSubject.setCompoundDrawables(drawable, null, null, null)
            }
            holder.tvSubject.text = val

            var latestMessageText = holder.messageItem.latestMessageText
            if (TextUtils.isEmpty(latestMessageText)) latestMessageText = ""
            if (TextUtils.isEmpty(latestMessageText) && !TextUtils.isEmpty(holder.messageItem.text)) {
                latestMessageText = holder.messageItem.text
            }
            if (TextUtils.isEmpty(latestMessageText) && !TextUtils.isEmpty(holder.messageItem.html)) {
                latestMessageText = holder.messageItem.html
            }
            latestMessageText = latestMessageText.replace("\n", " ")
            holder.tvMessage.text = latestMessageText

            val createDate = holder.messageItem.timestamp * 1000
            val timestamp = Timestamp(createDate)
            val now = System.currentTimeMillis()
            var df = SimpleDateFormat("MMM dd", Locale.getDefault())
            if ((now - createDate) < 1000 * 60 * 60 * 24) {
                df = SimpleDateFormat("hh mm a", Locale.getDefault())
            }
            val dt = df.format(timestamp.time)
            holder.tvTimestamp.text = dt

            var generator = ColorGenerator.MATERIAL
            val color2 = generator.getColor(val)
            var builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round()
            val pic = holder.messageItem.profilePic
            if (null == pic || pic.isEmpty()) {
            } else if (pic.startsWith("http")) {
                bitmapFromUrlTask = BitmapFromUrlTask(
                    holder.ivSender, pic, activity.applicationContext
                )
                bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                bitmapFromUrlTask?.execute()
            } else {
                var bitmap: Bitmap? = null
                try {
                    bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                    if (null == bitmap) {
                        holder.ivSender.setImageDrawable(builder.build(val.substring(0, 1), color2))
                    } else {
                        val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                        val bitmapDrawable = BitmapDrawable(resources, croppedBitmap)
                        bitmapDrawable.setBounds(Rect(0, 0, 100, 100))
                        holder.ivSender.setImageDrawable(bitmapDrawable)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            generator = ColorGenerator.MATERIAL
            val anInt = holder.messageItem.unreadCount
            if (0 == anInt) {
                holder.ivUnreadCount.visibility = View.INVISIBLE
                holder.mView.setBackgroundResource(R.drawable.selectable_item_bg)
            } else {
                holder.mView.setBackgroundResource(R.drawable.item_gray_bg_layer)
                val = anInt.toString()

                builder = TextDrawable.builder()
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

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap?, view: View, context: Context) {
            if (null == context) return
            val resources = context.resources
            if (null == resources) return
            if (null == bitmap) {
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

        inner class ViewHolder(val mView: View) :
            RecyclerView.ViewHolder(mView),
            View.OnLongClickListener {

            val tvId: TextView = mView.findViewById(R.id.tv_id)
            val tvName: TextView = mView.findViewById(R.id.tv_name)
            val tvSubject: TextView = mView.findViewById(R.id.tv_subject)
            val tvFrom: TextView = mView.findViewById(R.id.tv_from)
            val tvMessage: TextView = mView.findViewById(R.id.tv_text)
            val tvTimestamp: TextView = mView.findViewById(R.id.tv_timestamp)
            val ivSender: ImageView = mView.findViewById(R.id.iv_sender)
            val ivUnreadCount: ImageView = mView.findViewById(R.id.iv_unread_count)
            val ivAttachment: ImageView = mView.findViewById(R.id.iv_attach)
            var messageItem: MessageItem? = null

            init {
                mView.setOnClickListener(this@MsgsGrpClutterFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvMessage.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                setSelectedItem(messageItem!!)
                if (null == mActionMode) {
                    mActionMode = (activity as AppCompatActivity).startSupportActionMode(mActionModeCallback)
                    v.isSelected = !v.isSelected
                    return true
                }
                v.isSelected = !v.isSelected
                return false
            }
        }
    }

    companion object {
        fun newInstance(
            messageItem: MessageItem?,
            user: ContactItem?,
            clutterListener: ClutterListener?
        ): MsgsGrpClutterFragment {
            val fragment = MsgsGrpClutterFragment()
            fragment.setUser(user)
            if (clutterListener is ClutterTopicListener)
                fragment.setClutterTopicListener(clutterListener)
            fragment.setViewModeListener(clutterListener)
            val args = Bundle()
            args.putParcelable(MessageItem.TAG, messageItem)
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
