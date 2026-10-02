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
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.os.RemoteException
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.ContactSelectActivity
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity
import intellibitz.intellidroid.bean.BaseItemComparator
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider
import intellibitz.intellidroid.content.task.FetchAttachmentsTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.domain.account.EmailAccountListActivity
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.MainboxListener
import intellibitz.intellidroid.listener.PeopleDetailListener
import intellibitz.intellidroid.listener.PeopleListener
import intellibitz.intellidroid.listener.PeopleTopicListener
import intellibitz.intellidroid.service.ChatService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.DeleteMsgsTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.util.List

class AttachmentsFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    ContactListener,
    PeopleDetailListener,
    FetchAttachmentsTask.FetchAttachmentsTaskListener {

    companion object {
        const val TAG = "AttachmentsFragment"
        fun newInstance(user: ContactItem, peopleListener: MainboxListener): AttachmentsFragment {
            val fragment = AttachmentsFragment()
            fragment.user = user
            if (peopleListener is PeopleTopicListener) {
                fragment.peopleTopicListener = peopleListener
            }
            if (peopleListener is PeopleDetailListener) {
                fragment.peopleDetailListener = peopleListener
            }
            if (peopleListener is PeopleListener) {
                fragment.viewModeListener = peopleListener
            }
            val args = Bundle()
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
    private var rvMessages: RecyclerView? = null
    private var rvMessagesAdapter: RecyclerViewAdapter? = null
    private var deleteMsgsTask: DeleteMsgsTask? = null
    private var selectedItem: MessageItem? = null
    private var sharedIntent: Intent? = null
    private var contactItem: ContactItem? = null
    private var attachmentItems: ArrayList<MessageItem>? = null
    private var counter = 0
    private var looperThread: HandlerThread? = null
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
            when (item.itemId) {
                R.id.menu_delete -> {
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
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
        }
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
            MsgChatAttachmentContentProvider.CONTENT_URI, true,
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            user = it.getParcelable(ContactItem.USER_CONTACT)
            attachmentItems = it.getParcelableArrayList(MessageItem.TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        return inflater.inflate(R.layout.fragment_attachments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        empty = view.findViewById(R.id.tv_empty)
        empty?.setOnClickListener {
            startEmailListActivity()
        }
        rvMessages = view.findViewById(R.id.recyclerview)
        setupSwipe()
        setupSnackBar()
        restartLoader()
    }

    private fun startEmailListActivity() {
        val intent = Intent(activity, EmailAccountListActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNT_RQ_CODE)
    }

    private fun setupSwipe() {
        view?.let { v ->
            val refreshLayout = v.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
            refreshLayout?.setOnRefreshListener {
                Handler().postDelayed({
                    refreshLayout.isRefreshing = false
                }, 2000)
            }
        }
    }

    private fun restartLoader(uri: Uri) {
        try {
            val id = ContentUris.parseId(uri)
            attachmentItems = null
            restartLoader()
        } catch (e: NumberFormatException) {
        }
    }

    private fun restartLoader(filter: String) {
        this.filter = filter
        restartLoader()
    }

    private fun restartCacheLoader() {
        val objects = MainApplicationSingleton.getInstance(activity)
            .getGlobalSBValueAsList(AttachmentsFragment.TAG)
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
        attachmentItems = objects as ArrayList<MessageItem>
        createRecycleAdapter()
    }

    private fun restartLoader() {
        val fetchAttachmentsTask = FetchAttachmentsTask(filter, activity)
        fetchAttachmentsTask.setFetchAttachmentsTaskListener(this)
        fetchAttachmentsTask.execute()
    }

    fun messageSend(shared: Intent) {
        restartLoader()
    }

    private fun createRecycleAdapter() {
        resetRecycleAdapter()
    }

    private fun resetRecycleAdapter() {
        if (rvMessages == null && view != null) {
            rvMessages = view?.findViewById(R.id.recyclerview)
        }
        if (rvMessages == null) {
            Log.e(TAG, "resetRecycleAdapter: recyclerview is NULL")
            return
        }
        attachmentItems?.let { items ->
            Collections.sort(items, MessageItem.MessageItemComparator(BaseItemComparator.SORT_MODE.DESC))
            rvMessagesAdapter = RecyclerViewAdapter(items)
            rvMessagesAdapter?.setHasStableIds(true)
            rvMessages?.adapter = rvMessagesAdapter
        }
    }

    private fun setupSnackBar() {
        view?.let { v ->
            snackbar = Snackbar.make(v, "Please Add Account to see Emails", Snackbar.LENGTH_LONG)
            snackbar?.setAction("ADD EMAIL") {
                startEmailListActivity()
            }
        }
    }

    fun onBackPressed(): Boolean {
        return false
    }

    fun onMessageForward(intent: Intent?) {
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
        when (requestCode) {
            MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
                }
            }
            MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
                }
            }
        }
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

    override fun onFetchAttachmentsTaskExecute(attachmentItems: ArrayList<MessageItem>) {
        setAttachmentItems(attachmentItems)
        createRecycleAdapter()
    }

    override fun onFetchAttachmentsTaskExecuteFail(attachmentItems: ArrayList<MessageItem>) {
        Log.e(TAG, "onFetchAttachmentsTaskExecuteFail: ")
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
            mService?.let {
                try {
                    val msg = Message.obtain(null, ChatService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    it.send(msg)
                } catch (e: RemoteException) {
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

    fun setUser(user: ContactItem) {
        this.user = user
    }

    fun setPeopleTopicListener(peopleTopicListener: PeopleTopicListener) {
        this.peopleTopicListener = peopleTopicListener
    }

    override fun onViewModeChanged() {
        viewModeListener?.onViewModeChanged()
        super.onViewModeChanged()
    }

    override fun onViewModeItem() {
        viewModeListener?.onViewModeItem()
        super.onViewModeItem()
    }

    override fun onPeopleTyping(text: String) {
        peopleDetailListener?.onPeopleTyping(text)
    }

    override fun onPeopleTypingStopped(text: String) {
        peopleDetailListener?.onPeopleTypingStopped(text)
    }

    private fun showTyping(toUid: String, fromUid: String, fromName: String, user: ContactItem) {
        val item = MainApplicationSingleton.getBaseItem(fromUid, attachmentItems)
        if (item == null) {
        } else {
            if (fromUid == user.dataId) {
            } else {
                item.isTyping = true
                item.typingText = "$fromName is typing.."
                val pos = attachmentItems?.indexOf(item) ?: -1
                rvMessagesAdapter?.notifyItemChanged(pos)
                onPeopleTyping(fromName + " is typing..")
                Handler().postDelayed({
                    item.isTyping = false
                    rvMessagesAdapter?.notifyItemChanged(pos)
                    onPeopleTypingStopped("")
                }, 1000)
            }
        }
    }

    fun setAttachmentItems(attachmentItems: ArrayList<MessageItem>) {
        this.attachmentItems = attachmentItems
        MainApplicationSingleton.getInstance(activity).putGlobalVariable(
            AttachmentsFragment.TAG, attachmentItems
        )
    }

    fun setSharedIntent(sharedIntent: Intent) {
        this.sharedIntent = sharedIntent
    }

    fun setPeopleDetailListener(peopleDetailListener: PeopleDetailListener) {
        this.peopleDetailListener = peopleDetailListener
    }

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ChatService.MSG_SHOW_TYPING -> {
                    peopleDetailListener?.let {
                        try {
                            val obj = msg.obj as String
                            if (obj != null) {
                                val jsonArray = JSONArray(obj)
                                val jsonObject = jsonArray.getJSONObject(0)
                                try {
                                    val toUid = jsonObject.getString("to_uid")
                                    val fromUid = jsonObject.getString("from_uid")
                                    val fromName = jsonObject.getString("from_name")
                                    showTyping(toUid, fromUid, fromName, user)
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

    inner class RecyclerViewAdapter(private val attachmentItems: List<MessageItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_attachments_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItem = attachmentItems[position]
            holder.tvId.text = holder.mItem.dataId
            var filename = holder.mItem.profilePic
            if (TextUtils.isEmpty(filename)) {
                filename = holder.mItem.downloadURL
            }
            var name = holder.mItem.name
            if (TextUtils.isEmpty(name)) {
                name = MainApplicationSingleton.getLastPath(filename)
            }
            holder.tvName.text = name
            var attachmentType = holder.mItem.type
            if (attachmentType == null) attachmentType = ""
            val sz = "$attachmentType ${holder.mItem.size} bytes"
            holder.tvSubject.text = sz
            if (TextUtils.isEmpty(filename)) {
                Log.e(TAG, "Attachment Download URL is NULL ")
            } else if (filename.startsWith("/")) {
                val file = File(filename)
                val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
                val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                holder.mView.setOnClickListener {
                    MainApplicationSingleton.startMimeActivity(
                        file,
                        MsgChatAttachmentContentProvider.getMimeType(holder.mItem),
                        context
                    )
                }
            } else if (filename.startsWith("http")) {
                val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.parse(filename).toString())
                val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                holder.mView.setOnClickListener {
                    if (MessageItem.EMAIL.equals(holder.mItem.messageType, ignoreCase = true)) {
                        MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                            appCompatActivity,
                            user,
                            holder.mItem
                        )
                    }
                    if (MessageItem.CHAT.equals(holder.mItem.messageType, ignoreCase = true)) {
                        MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                            appCompatActivity,
                            user,
                            holder.mItem
                        )
                    }
                    if (MessageItem.GROUP.equals(holder.mItem.messageType, ignoreCase = true)) {
                        MsgChatGrpAttachmentContentProvider.asyncUpdateChatGroupAttachmentFilePathInDB(
                            appCompatActivity,
                            user,
                            holder.mItem
                        )
                    }
                }
            } else {
                holder.mView.setOnClickListener {
                    if (MessageItem.EMAIL.equals(holder.mItem.messageType, ignoreCase = true)) {
                        MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                            appCompatActivity,
                            user,
                            holder.mItem
                        )
                    }
                    if (MessageItem.CHAT.equals(holder.mItem.messageType, ignoreCase = true)) {
                        MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                            appCompatActivity,
                            user,
                            holder.mItem
                        )
                    }
                    if (MessageItem.GROUP.equals(holder.mItem.messageType, ignoreCase = true)) {
                        MsgChatGrpAttachmentContentProvider.asyncUpdateChatGroupAttachmentFilePathInDB(
                            appCompatActivity,
                            user,
                            holder.mItem
                        )
                    }
                }
            }
            var bm: Bitmap? = null
            var drawable: Drawable? = null
            try {
                if (attachmentType.contains("image") || !TextUtils.isEmpty(mimetype) && mimetype.contains("image")) {
                    if (!TextUtils.isEmpty(filename) && filename.startsWith("http")) {
                        bitmapFromUrlTask = BitmapFromUrlTask(
                            holder.ivAttachment,
                            filename,
                            activity?.applicationContext
                        )
                        bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                        bitmapFromUrlTask?.execute()
                    } else {
                        if (!TextUtils.isEmpty(filename)) {
                            bm = BitmapFactory.decodeFile(filename)
                            drawable = BitmapDrawable(resources, bm)
                        }
                    }
                } else if (attachmentType.contains("video") || !TextUtils.isEmpty(mimetype) && mimetype.contains("video")) {
                    bm = ThumbnailUtils.createVideoThumbnail(
                        filename,
                        MediaStore.Video.Thumbnails.MINI_KIND
                    )
                    drawable = BitmapDrawable(resources, bm)
                } else if (attachmentType.contains("audio") || !TextUtils.isEmpty(mimetype) && mimetype.contains("audio")) {
                    drawable = getDrawable(android.R.drawable.ic_media_play, activity?.theme)
                } else if (attachmentType.contains("application") || !TextUtils.isEmpty(mimetype) && mimetype.contains("application")) {
                    drawable = getDrawable(
                        android.R.drawable.ic_menu_slideshow,
                        activity?.theme
                    )
                }
            } catch (ignored: Throwable) {
                Log.e(TAG, "$TAG${ignored.message}")
            }
            if (drawable == null && ("jpeg".equals(extension, ignoreCase = true) || "jpg".equals(
                    extension,
                    ignoreCase = true
                ))
            ) {
                drawable = getDrawable(R.drawable.jpg)
            }
            if (drawable == null && "pdf".equals(extension, ignoreCase = true)) {
                drawable = getDrawable(R.drawable.pdf)
            }
            if (drawable == null && "png".equals(extension, ignoreCase = true)) {
                drawable = getDrawable(R.drawable.png)
            }
            if (drawable == null) {
                holder.ivAttachment.setImageResource(R.drawable.ic_get_app_black_18dp)
            }
            if (drawable != null) {
                drawable.setBounds(Rect(0, 0, 100, 100))
                holder.ivAttachment.setImageDrawable(drawable)
            }
        }

        override fun getItemId(position: Int): Long {
            return attachmentItems[position]._id
        }

        override fun getItemCount(): Int {
            return attachmentItems.size
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap, view: View, context: Context) {
            if (context == null) return
            val resources = context.resources
            if (resources == null) return
            if (bitmap == null) {
                val drawable = getDrawable(
                    R.drawable.ic_get_app_black_18dp,
                    activity?.theme
                )
                drawable?.setBounds(Rect(0, 0, 100, 100))
                (view as ImageView).setImageDrawable(drawable)
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

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView),
            View.OnLongClickListener {
            val tvId: TextView = mView.findViewById(R.id.tv_id)
            val tvName: TextView = mView.findViewById(R.id.tv_name)
            val tvSubject: TextView = mView.findViewById(R.id.tv_subject)
            val ivAttachment: ImageView = mView.findViewById(R.id.iv_attach)
            var mItem: MessageItem? = null

            override fun toString(): String {
                return super.toString() + " '" + tvName.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
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
