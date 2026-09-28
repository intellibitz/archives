package intellibitz.intellidroid.fragment

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ComponentName
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.os.RemoteException
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.util.Rfc822Token
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.PermissionChecker
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.content.task.FetchPeopleDetailTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.PeopleDetailListener
import intellibitz.intellidroid.listener.PeopleHeaderListener
import intellibitz.intellidroid.listener.PeopleListener
import intellibitz.intellidroid.service.ChatEmailService
import intellibitz.intellidroid.service.ChatService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.CreateDraftTask
import intellibitz.intellidroid.task.DeleteMsgsTask
import intellibitz.intellidroid.task.FlagMsgsTask
import intellibitz.intellidroid.task.MarkReadTask
import intellibitz.intellidroid.task.UnFlagMsgsTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.MediaPickerFile
import intellibitz.intellidroid.util.MediaPickerUri
import intellibitz.intellidroid.widget.NewEmailDialogFragment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 */
class PeopleDetailFragment : IntellibitzActivityFragment(),
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

    companion object {
        const val TAG = "PeopleDetailFrag"
        /**
         * The fragment argument representing the messageItem ID that this fragment
         * represents.
         */
        const val ARG_DATA_ID = "id"
        const val ARG_TOPIC_ID = "item_id"

        fun newInstance(
            messageItem: MessageItem?,
            user: ContactItem?,
            peopleListener: PeopleListener?
        ): PeopleDetailFragment {
            val fragment = PeopleDetailFragment()
            fragment.setUser(user)
            if (messageItem != null) {
                fragment.setMessageItem(messageItem)
            }
            if (peopleListener != null) {
                fragment.setPeopleListener(peopleListener)
            }
            if (peopleListener is PeopleHeaderListener) {
                fragment.setPeopleHeaderListener(peopleListener)
            }
            if (peopleListener is PeopleDetailListener) {
                fragment.setPeopleDetailListener(peopleListener)
            }
            if (peopleListener != null) {
                fragment.setViewModeListener(peopleListener)
            }
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            args.putParcelable(MessageItem.TAG, messageItem)
            fragment.arguments = args
            return fragment
        }
    }

    // If non-null, this is the current filter the user has provided.
    var mCurFilter: String? = null
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    val mMessenger = Messenger(IncomingHandler())
    private val lock = Any()
    /**
     * Messenger for communicating with service.
     */
    var mService: Messenger? = null
    /**
     * Flag indicating whether we have called bind on the service.
     */
    var mIsBound = false
    var cameraPhotoFileUri: Uri? = null
    var videoPhotoFileUri: Uri? = null
    var audioFileUri: Uri? = null
    var messageThreadContentObserver: ContentObserver? = null
    var attachmentContentObserver: ContentObserver? = null
    var btnSend: ImageButton? = null
    var btnUpload: ImageButton? = null
    var llUpload: View? = null
    var btnDraft: Button? = null
    var etMessageInput: EditText? = null
    var ivAttach: ImageButton? = null
    var btnAudio: ImageButton? = null
    var btnCamera: ImageButton? = null
    var btnCall: ImageButton? = null
    var btnSms: ImageButton? = null
    var rvMessages: RecyclerView? = null
    var rvMessagesAdapter: RecyclerView.Adapter<*>? = null
    var markReadTask: MarkReadTask? = null
    var deleteMsgsTask: DeleteMsgsTask? = null
    var flagMsgsTask: FlagMsgsTask? = null
    var unflagMsgsTask: UnFlagMsgsTask? = null
    var fetchPeopleDetailTask: FetchPeopleDetailTask? = null
    var messageItem: MessageItem? = null
    var selectedItem: MessageItem? = null
    var selfTypingMessage: MessageItem? = null
    //    private String id;
//    private String topic;
    var c: Context? = null
    var title: String? = null
    var subTitle: String? = null
    var filter: String? = null
    var btnVideo: ImageButton? = null
    var looperThread: HandlerThread? = null
    var createDraftTask: CreateDraftTask? = null
    var draftLooperThread: HandlerThread? = null
    var peopleListener: PeopleListener? = null
    var peopleDetailListener: PeopleDetailListener? = null
    var peopleHeaderListener: PeopleHeaderListener? = null
    //    private Toolbar detailToolbar;
    var toolbar: Toolbar? = null
    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = Messenger(service)
            //            mCallbackText.setText("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                val msg = Message.obtain(null, ChatService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)

                // Give it some value as an example.
                val msg2 = Message.obtain(null, ChatService.MSG_SET_VALUE, this.hashCode(), 0)
                mService?.send(msg2)
            } catch (e: RemoteException) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null
            //            mCallbackText.setText("Disconnected.");
        }
    }
    var mActionMode: ActionMode? = null
    private val mActionModeCallback = object : ActionMode.Callback {

        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.menu_context_chat_details, menu)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_delete -> {
                    deleteMessages()
                    //                    shareCurrentItem();
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                R.id.menu_forward -> {
                    forwardMessages()
                    //                    shareCurrentItem();
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                R.id.menu_addtonest -> {
                    //                    forwardMessagesToNest();
                    broadcastMessagesToNest()
                    //                    shareCurrentItem();
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                R.id.menu_flag -> {
                    flagMessages()
                    //                    shareCurrentItem();
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                R.id.menu_unflag -> {
                    unflagMessages()
                    //                    shareCurrentItem();
                    mode.finish() // Action picked, so close the CAB
                    return true
                }
                else -> return false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    constructor() : super()

    constructor(messageItem: MessageItem, user: ContactItem, peopleListener: PeopleListener) : super() {
        this.user = user
        this.messageItem = messageItem
        this.peopleListener = peopleListener
        if (peopleListener is PeopleHeaderListener) this.peopleHeaderListener = peopleListener as PeopleHeaderListener
        if (peopleListener is PeopleDetailListener) this.peopleDetailListener = peopleListener as PeopleDetailListener
        if (peopleListener != null) this.viewModeListener = peopleListener
        val args = Bundle()
        args.putParcelable(ContactItem.USER_CONTACT, user)
        args.putParcelable(MessageItem.TAG, messageItem)
        arguments = args
    }

    fun setPeopleHeaderListener(peopleHeaderListener: PeopleHeaderListener) {
        this.peopleHeaderListener = peopleHeaderListener
    }

    fun setPeopleDetailListener(peopleDetailListener: PeopleDetailListener) {
        this.peopleDetailListener = peopleDetailListener
    }

    private fun showTyping(toUid: String, fromUid: String, fromName: String, user: ContactItem) {
        if (fromUid == user.dataId) {
            // if self is typing ignore..
            //                shows typing only from other senders
        } else {
            onPeopleTyping("$fromName is typing..")
            //                                setToolBarShowTyping(name + " is typing..");
            val delay = Handler()
            delay.postDelayed({
                //                                        setToolBarSubTitle(toolbarSubTitle);
                //                    name = null;
                //                    restartLoader(null);
                onPeopleTypingStopped("")
            }, 1000)
        }
    }

    override fun onPeopleTyping(text: String) {
        if (peopleDetailListener == null) {
            // // TODO: 21-05-2016
            //            listeners need to be saved, restored or recreated brand new every time
        } else {
            peopleDetailListener?.onPeopleTyping(text)
            //        getAppCompatActivity().getToolbar().setSubtitle(text);
            //        toolbar.setSubtitleTextColor(Color.GREEN);
        }
    }

    override fun onPeopleTypingStopped(text: String) {
        if (peopleDetailListener == null) {
            // // TODO: 21-05-2016
            //            listeners need to be saved, restored or recreated brand new every time
        } else {
            peopleDetailListener?.onPeopleTypingStopped(text)
            //        getAppCompatActivity().getToolbar().setSubtitle(text);
            //        toolbar.setSubtitleTextColor(Color.WHITE);
        }
    }

    fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        appCompatActivity?.bindService(
            Intent(appCompatActivity, ChatService::class.java),
            mConnection,
            Context.BIND_AUTO_CREATE
        )
        mIsBound = true
        //        mCallbackText.setText("Binding.");
    }

    fun doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    val msg = Message.obtain(null, ChatEmailService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    mService?.send(msg)
                } catch (e: RemoteException) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            appCompatActivity?.unbindService(mConnection)
            mIsBound = false
            //            mCallbackText.setText("Unbinding.");
        }
    }

    private fun sendOnKey(messageItem: MessageItem, user: ContactItem) {
        if (mService == null) {
            Log.e(TAG, "Service is NULL - cannot send on key message")
        } else {
            try {
                val msg = Message.obtain(null, ChatService.MSG_ON_KEY, this.hashCode(), 0)
                msg.obj = messageItem
                val args = Bundle()
                args.putParcelable(ContactItem.USER_CONTACT, user)
                args.putParcelable(MessageItem.TAG, messageItem)
                msg.data = args
                mService?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendMessage(messageItem: MessageItem, user: ContactItem) {
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
        val msg = Message.obtain(null, ChatService.MSG_SET_VALUE, this.hashCode(), 0)
        msg.obj = messageItem
        //        adds again.. check if the collection holds
        //        // TODO: 14-03-2016
        //        redundant add - remove this
        //        messageItem.addMessage(messageItem);
        val args = Bundle()
        args.putParcelable(ContactItem.USER_CONTACT, user)
        args.putParcelable(MessageItem.TAG, messageItem)
        msg.data = args
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun refreshChatMessageLoader(arguments: Bundle?) {
        restoreStateFromFragmentArguments(arguments)
        createRecyclerAdapter()
        /*
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_TITLE, id);
        contentValues.put(DatabaseHelper.KEY_DESCRIPTION, message);
        getAppCompatActivity().getContentResolver().insert(
                ChatMessageContentProvider.CONTENT_URI, contentValues);
*/
    }

    fun refreshEmailMessageLoader(arguments: Bundle?) {
        restoreStateFromFragmentArguments(arguments)
        createRecyclerAdapter()
    }

    private fun sendMessage(id: String, topic: String, fromName: String, message: String) {
        val msg = Message.obtain(null, ChatService.MSG_SET_VALUE, this.hashCode(), 0)
        msg.obj = message
        val args = Bundle()
        args.putString("id", id)
        args.putString("topic", topic)
        args.putString("fromName", fromName)
        args.putString("msg", message)
        msg.data = args
        try {
            mService?.send(msg)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun setPeopleListener(peopleListener: PeopleListener) {
        this.peopleListener = peopleListener
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
    private fun restoreStateFromFragmentArguments(arguments: Bundle?) {
        var args = arguments
        if (args == null) {
            args = arguments
        }
        if (args != null) {
            user = args.getParcelable(ContactItem.USER_CONTACT)
            messageItem = args.getParcelable(MessageItem.TAG)
        }
    }

    fun getMessageItem(): MessageItem? {
        return messageItem
    }

    fun setMessageItem(messageItem: MessageItem) {
        this.messageItem = messageItem
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        doBindService()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        outState.putParcelable(MessageItem.TAG, messageItem)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        doUnbindService()
        appCompatActivity?.contentResolver?.unregisterContentObserver(messageThreadContentObserver)
        appCompatActivity?.contentResolver?.unregisterContentObserver(attachmentContentObserver)
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (peopleDetailListener != null) {
            peopleDetailListener?.onPeopleTypingStopped(messageItem?.name)
        }
        messageThreadContentObserver = object : ContentObserver(Handler()) {
            override fun deliverSelfNotifications(): Boolean {
                return super.deliverSelfNotifications()
            }

            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                //                clears the text of previous message.. it has already been send and received now
                if (etMessageInput == null && view != null) etMessageInput = view?.findViewById(R.id.tv_chat_message)
                if (etMessageInput != null) {
                    etMessageInput?.isEnabled = true
                    etMessageInput?.setText("")
                }
                //                toggleNewMessageView("");
                restartLoader()
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
        }
        appCompatActivity?.contentResolver?.registerContentObserver(
            MessageChatContentProvider.CONTENT_URI,
            true,
            messageThreadContentObserver
        )
        attachmentContentObserver = object : ContentObserver(Handler()) {
            override fun deliverSelfNotifications(): Boolean {
                return super.deliverSelfNotifications()
            }

            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                restartLoader()
            }
        }
        appCompatActivity?.contentResolver?.registerContentObserver(
            MsgChatAttachmentContentProvider.CONTENT_URI,
            true,
            attachmentContentObserver
        )
    }

    private fun quitLooperThread() {
        if (looperThread != null) looperThread?.quit()
        sendMessage(messageItem!!, user!!)
        broadcastMessagesSend(messageItem!!)
    }

    private fun quitDraftLooperThread() {
        if (draftLooperThread != null) draftLooperThread?.quit()
    }

    override fun onPostCreateDraftFromCloudExecute(response: JSONObject?, messageItem: MessageItem) {
        etMessageInput?.isEnabled = true
        etMessageInput?.setText("")
        Log.e(TAG, "onPostCreateDraftFromCloudExecute: $response")
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || -1 == status || 99 == status) {
            //            retries again..
            onPostCreateDraftFromCloudExecuteFail(response, messageItem)
        } else {
            try {
                val id = response.getString("draft_id")
                messageItem.dataId = id
                Log.d(TAG, "Draft saved Success: $id")
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    override fun onPostCreateDraftFromCloudExecuteFail(response: JSONObject?, messageItem: MessageItem) {
        etMessageInput?.isEnabled = true
        etMessageInput?.setText("")
        Log.d(TAG, "onPostCreateDraftFromCloudExecuteFail: Draft saved Fail: $response")
    }

    override fun setCreateDraftFromCloudTaskToNull() {
        createDraftTask = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_peopledetail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppBar()
        etMessageInput = view.findViewById(R.id.tv_chat_message)
        btnSend = view.findViewById(R.id.btn_chat_send)
        btnUpload = view.findViewById(R.id.btn_upload)
        llUpload = view.findViewById(R.id.ll_upload)
        btnUpload?.setOnClickListener { view ->
            val visibility = llUpload?.visibility
            if (View.VISIBLE == visibility) llUpload?.visibility = View.GONE else llUpload?.visibility = View.VISIBLE
        }
        btnDraft = view.findViewById(R.id.btn_draft)
        ivAttach = view.findViewById(R.id.iv_chat_attach)
        btnAudio = view.findViewById(R.id.ib_audio)
        btnCamera = view.findViewById(R.id.ib_camera)
        btnVideo = view.findViewById(R.id.ib_video)
        btnCall = view.findViewById(R.id.ib_call)
        btnSms = view.findViewById(R.id.ib_sms)
        rvMessages = view.findViewById(R.id.rv_chat_messages)
        if (savedInstanceState == null) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            messageItem = arguments?.getParcelable(MessageItem.TAG)
            etMessageInput?.requestFocus()
            etMessageInput?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    toggleNewMessageView(s)
                    sendOnKey(messageItem!!, user!!)
                }

                override fun afterTextChanged(s: Editable) {}
            })

            btnDraft?.setOnClickListener { view ->
                etMessageInput?.isEnabled = false
                val message = etMessageInput?.text.toString()
                if (TextUtils.isEmpty(message)) {
                    return@setOnClickListener
                }
                messageItem?.flagStackMessageToSend()
                messageItem?.messageItemConcurrentLinkedQueue?.add(messageItem?.popMessage())
                //                    MessageItem messageItem = messageItem.popMessage();
                draftLooperThread = MainApplicationSingleton.performOnHandlerThread {
                    val messageItemConcurrentLinkedQueue = messageItem?.messageItemConcurrentLinkedQueue
                    val messageItems: Array<MessageItem>
                    synchronized(lock) {
                        messageItems = messageItemConcurrentLinkedQueue?.toTypedArray() ?: arrayOf()
                    }
                    for (messageItem in messageItems) {
                        if (messageItem.isReadyToSend) {
                            messageItem.baseType = MessageItem.DRAFT
                            //                                    the cloud treats this doc type as draft
                            messageItem.docType = MessageItem.DRAFT
                            messageItem.docOwner = user?.dataId
                            messageItem.type = MessageItem.CHAT
                            messageItem.messageType = MessageItem.CHAT
                            messageItem.toType = MessageItem.USER
                            messageItem.isGroup = false
                            messageItem.isEmailItem = false
                            messageItem.chatId = this@PeopleDetailFragment.messageItem?.chatId
                            messageItem.dataId = this@PeopleDetailFragment.messageItem?.chatId
                            //                                        // TODO: 19-06-2016
                            //                                        set chat_msg_ref from app
                            //                                        chat_msg_ref format: fromuid_touid_timestamp
                            messageItem.chatMsgRef = user?.dataId + "_" + messageItem.chatId + "_" + System.currentTimeMillis()
                            /*
                                    createDraftTask = new CreateDraftTask(messageItem,
                                            user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                                            MainApplicationSingleton.AUTH_CREATE_DRAFT);
                                    createDraftTask.setCreateDraftTaskListener(PeopleDetailFragment.this);
                                    createDraftTask.execute();
*/
                            broadcastMessagesToDraft(messageItem)
                            messageItemConcurrentLinkedQueue?.remove(messageItem)
                        }
                    }
                    this@PeopleDetailFragment.quitDraftLooperThread()
                }
                etMessageInput?.isEnabled = true
                etMessageInput?.setText("")
            }

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
*/
            /*
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
            btnSend?.setOnClickListener { v ->
                etMessageInput?.isEnabled = false
                //                    // TODO: 22-05-2016
                //                    to mainitain input Q, the user can type and hit send madly like a million times
                val message = etMessageInput?.text.toString()
                if (TextUtils.isEmpty(message)) {
                    return@setOnClickListener
                }
                messageItem?.flagStackMessageToSend()
                messageItem?.messageItemConcurrentLinkedQueue?.add(messageItem?.popMessage())
                //                not required.. message would be already populated
                //                messageItem.getMessages().setText(message);
                //                messageItem.getMessages().addAttachments(messageItem.getAttachments());
                //                    saves to db before sending to cloud
                looperThread = MainApplicationSingleton.performOnHandlerThread {
                    val messageItemConcurrentLinkedQueue = messageItem?.messageItemConcurrentLinkedQueue
                    val messageItems: Array<MessageItem>
                    synchronized(lock) {
                        messageItems = messageItemConcurrentLinkedQueue?.toTypedArray() ?: arrayOf()
                    }
                    for (messageItem in messageItems) {
                        if (messageItem.isReadyToSend) {
                            try {
                                messageItem.docType = MessageItem.MSG
                                messageItem.type = MessageItem.CHAT
                                messageItem.messageType = MessageItem.CHAT
                                messageItem.toType = MessageItem.USER
                                messageItem.isGroup = false
                                messageItem.isEmailItem = false
                                messageItem.chatId = this@PeopleDetailFragment.messageItem?.chatId
                                messageItem.dataId = this@PeopleDetailFragment.messageItem?.chatId
                                //                                        messageItem.setToType(PeopleDetailFragment.this.messageItem.getToType());
                                //                                        // TODO: 19-06-2016
                                //                                        set chat_msg_ref from app
                                //                                        chat_msg_ref format: fromuid_touid_timestamp
                                messageItem.chatMsgRef = user?.dataId + "_" + messageItem.chatId + "_" + System.currentTimeMillis()
                                MessageChatContentProvider.createsContactsFromMessage(messageItem)
                                val uri = MessageChatContentProvider.savesMessageItem(messageItem, user!!, context)
                                Log.d(TAG, "Message saved: $uri")
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Log.e(TAG, "Message save FAIL: $messageItem")
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Log.e(TAG, "Message save FAIL: $messageItem")
                            }
                        }
                    }
                    this@PeopleDetailFragment.quitLooperThread()
                }
                //                    sendMessage(messageItem, user);
                //                empties the input text
                //                etMessageInput.setText("");
            }

            ivAttach?.setOnClickListener { v -> openFilePicker() }
            btnAudio?.setOnClickListener { v -> openAudioPicker() }
            btnCamera?.setOnClickListener { v -> openCameraPicker() }
            btnVideo?.setOnClickListener { v -> openVideoPicker() }
            btnCall?.setOnClickListener { v -> openCallPicker() }
            btnSms?.setOnClickListener { v -> openSmsPicker() }
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            messageItem = savedInstanceState.getParcelable(MessageItem.TAG)
        }
        if (messageItem != null) {
            //            set read as true, if the message is NOT new
            if (!MessageItem.TAG.equals(messageItem?.dataId)) {
                if (messageItem?.unreadCount!! > 0) {
                    execMarkReadTask(messageItem)
                } else {
                    Log.e(TAG, "No unread message - mark read is not invoked")
                }
            }
            //            createRecyclerAdapter();
            //            toggleNewMessageView("");
        }
        setChatHeaders(messageItem)
        toolbar?.setOnClickListener { v ->
            if (viewModeListener != null) {
                viewModeListener?.onViewModeChanged()
            }
            //                // TODO: 15-07-2016
            //                to land in this contact page
            broadcastContactForProfile(messageItem!!)
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
        val messageItemToForward = messageItem?.popMessage()
        if (messageItemToForward != null && !MessageItem.TAG.equals(messageItemToForward?.dataId)) {
            //            toggleNewMessageView(messageItemToForward.getText());
            etMessageInput?.setText(messageItemToForward?.text)
        }
        if (messageItem?.isDraft!!) {
            //            toggleNewMessageView(messageItemToForward.getText());
            etMessageInput?.setText(messageItem?.text)
        }
        restartLoader()
        //        // TODO: 28-03-2016
        //        already done in content fragment.. not required
        /*
        getAppCompatActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.CHAT_MESSAGE_FRAGMENT_LOADERID, null,
                ChatDetailFragment.this);
*/
    }

    fun broadcastContactForProfile(messageItem: MessageItem) {
        val intent = Intent(MainApplicationSingleton.BROADCAST_CONTACT_PROFILE_VIEW)
        intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, messageItem.intellibitzId)
        LocalBroadcastManager.getInstance(appCompatActivity).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        appCompatActivity?.finish()
    }

    private fun setupAppBar() {
        toolbar = view?.findViewById(R.id.toolbar)
        toolbar?.setTitle(R.string.new_group)
        toolbar?.setSubtitle(R.string.app_title)
        appCompatActivity?.setSupportActionBar(toolbar)
        // Show the Up button in the action bar.
        val actionBar = appCompatActivity?.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
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
    fun startMsgChatGrpContactsDetailActivity(contactItem: ContactItem, user: ContactItem) {
        val intent = Intent(appCompatActivity, MsgChatGrpContactsDetailActivity::class.java)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE)
        /*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getAppCompatActivity(), twoPane,
                        selectedItem, user);
        msgChatGrpContactsDetailFragment.onOkPressed(null);
        getAppCompatActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
        return msgChatGrpContactsDetailFragment;
*/
    }

    fun onBackPressed(): Boolean {
        return false
    }

    fun setChatHeaders(messageItem: MessageItem?) {
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
        setupDetailTitle(messageItem, toolbar)
    }

    private fun performNavigationClose() {
        appCompatActivity?.onBackPressed()
    }

    private fun setupDetailTitle(messageItem: MessageItem?, toolbar: Toolbar?) {
        if (messageItem == null) {
            Log.e(TAG, "MessageThread NULL")
            return
        }
        title = messageItem.name
        if (TextUtils.isEmpty(title)) {
            title = messageItem.displayName
        }
        if (TextUtils.isEmpty(title)) {
            title = messageItem.firstName
        }
        if (TextUtils.isEmpty(title)) {
            title = messageItem.lastName
        }
        if (TextUtils.isEmpty(title)) {
            var contactItem = messageItem.sharedDeviceContactItem
            if (contactItem == null) {
                contactItem = messageItem.contactItem
            }
            if (contactItem != null) {
                title = contactItem.name
                if (title == null || title?.length == 0) {
                    title = contactItem.firstName
                }
                if (title == null || title?.length == 0) {
                    title = contactItem.lastName
                }
            }
            if (title == null || title?.length == 0) {
                title = messageItem.chatId
            }
            if (title == null || title?.length == 0) {
                title = messageItem.docOwner
            }
        }
        var from = messageItem.docSender
        if (from == null || from.length == 0) {
            from = messageItem.from
        }
        subTitle = "last seen on"
        /*
            String recipients = MessageItem.getSingleAddress(messageItem.getCc(),
                    messageItem.getTo(), from, user.getEmail(), user.getName());
*/
        val contactItem = messageItem.contactItem
        if (contactItem != null) {
            val names = contactItem.contactsNameAsArray
            if (names == null || names.size == 0) {
                subTitle = "last seen on"
            } else {
                subTitle = ""
                for (name in names) {
                    if (name != null && !subTitle?.isEmpty()!! && !subTitle?.contains(name)!!) {
                        subTitle += ","
                    }
                    subTitle += name
                }
            }
        }
        toolbar?.title = title
        toolbar?.subtitle = subTitle
    }

    @Nullable
    private fun setMessageHeaders(messageItem: MessageItem): Toolbar? {
        //        Toolbar toolbar = setupDetailToolbar();
        if (toolbar != null) {
            toolbar?.setNavigationIcon(R.drawable.ic_dialog_close_light)
            toolbar?.setNavigationOnClickListener { v -> performNavigationClose() }
            var from = messageItem.docSender
            if (from == null) {
                from = messageItem.from
            }
            val recipients = MessageItem.getSingleAddress(
                messageItem.cc,
                messageItem.to,
                from,
                user?.email,
                user?.name
            )
            toolbar?.title = messageItem.subject
            toolbar?.subtitle = recipients
        }
        return toolbar
    }

    private fun execMarkReadTask(messageItem: MessageItem) {
        markReadTask = MarkReadTask(
            context,
            messageItem,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_MARK_READ
        )
        markReadTask?.setMarkReadTaskListener(this)
        markReadTask?.execute()
    }

    override fun onPostMarkReadExecute(response: JSONObject?, item: MessageItem) {
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || 99 == status || -1 == status) {
            //            retries again..
            onPostMarkReadExecuteFail(response, item)
        } else {
            Log.e(TAG, "onPostMarkReadExecute:SUCCESS $response")
        }
    }

    override fun onPostMarkReadExecuteFail(response: JSONObject?, item: MessageItem) {
        Log.e(TAG, "Mark Read - FAIL $response")
    }

    override fun setMarkReadTaskToNull() {
        markReadTask = null
    }

    private fun createRecyclerAdapter(uri: Uri) {
        try {
            val id = ContentUris.parseId(uri)
            //            empties the list..
            //            // TODO: 06-04-2016
            //            to update only the changed item
            if (id > 0) createRecyclerAdapter(id)
        } catch (e: NumberFormatException) {
            //            ignore
        }
    }

    private fun createRecyclerAdapter(id: Long) {
        val cursor = appCompatActivity?.contentResolver?.query(
            Uri.withAppendedPath(MsgChatAttachmentContentProvider.CONTENT_URI, id.toString()),
            arrayOf(MessageItemColumns.KEY_DOWNLOAD_URL),
            MessageItemColumns.KEY_ID,
            arrayOf(id.toString()),
            null
        )
        if (cursor == null) return
        if (cursor.count > 0) {
            val attachmentItem = messageItem?.getAttachment(id)
            //            // TODO: 18-05-2016
            //            null check
            if (attachmentItem != null) {
                attachmentItem.downloadURL = cursor.getString(
                    cursor.getColumnIndex(
                        MessageItemColumns.KEY_DOWNLOAD_URL
                    )
                )
                //            rvMessagesAdapter.notifyDataSetChanged();
                createRecyclerAdapter()
            }
            cursor.close()
        }
    }

    private fun restartLoader(query: String) {
        this.filter = query
        restartLoader()
    }

    private fun restartLoader() {
        if (messageItem != null) {
            val last = messageItem?.peekMessageInStack()
            if (last != null && MessageItem.TAG.equals(last.dataId)) {
                selfTypingMessage = last
            }
        }
        messageItem?.messages?.clear()
        fetchPeopleDetailTask = FetchPeopleDetailTask(messageItem, filter, appCompatActivity)
        fetchPeopleDetailTask?.setFetchMsgsGrpPeopleDetailTaskListener(this)
        fetchPeopleDetailTask?.execute()
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

    private fun createRecyclerAdapter() {
        if (rvMessages == null && view != null) rvMessages = view?.findViewById(R.id.rv_chat_messages)
        if (rvMessages == null) {
            Log.e(TAG, "createRecyclerAdapter: Recycler view NULL")
            return
        }
        val messages = messageItem?.messages
        if (selfTypingMessage != null) {
            try {
                messageItem?.addMessage(selfTypingMessage)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }
        val adapterList = ArrayList(messages)
        //            sorts messages by asc.. so the latest msg is at the bottom
        Collections.sort(adapterList, MessageItem.MessageItemComparator())

        rvMessagesAdapter = RecyclerViewAdapter(adapterList)
        rvMessagesAdapter?.setHasStableIds(true)
        rvMessages?.adapter = rvMessagesAdapter
        rvMessages?.scrollToPosition(rvMessagesAdapter?.itemCount!! - 1)
    }

    fun fillItemsFromTask(items: Collection<MessageItem>) {
        var messageItems = messageItem?.messages
        if (messageItems == null) messageItems = HashSet()
        messageItems?.clear()
        messageItems?.addAll(items)
    }

    override fun onFetchMsgsGrpPeopleDetailTaskExecute(messageItem: MessageItem) {
        this.messageItem = messageItem
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
        createRecyclerAdapter()
    }

    override fun onFetchMsgsGrpPeopleDetailTaskExecuteFail(messageItem: MessageItem) {
        Log.e(TAG, "onFetchMsgsGrpPeopleDetailTaskExecuteFail: ")
    }

    override fun setFetchMsgsGrpPeopleDetailTaskToNull() {
        fetchPeopleDetailTask = null
    }

    private fun toggleNewMessageView(s: CharSequence): MessageItem? {
        var last: MessageItem? = null
        val txt = s.toString()
        if (0 == txt.length || "" == txt) {
            hideSend()
            //            btnCamera.setVisibility(View.VISIBLE);
            //            btnAudio.setVisibility(View.VISIBLE);
            //            removes the message
            //                pop, then to sync the stack state.. always add to the thread
            last = messageItem?.popMessage()
            //            removes the last new message, if the user input is empty
            //            removes the last new message, if the text is empty AND the attachments are empty
            if (last != null && MessageItem.TAG.equals(last.dataId) && !last.attachments.isEmpty()) {
                //                remove only, when the latest message.. is the new message created here
                //                retains the valid db saved latest message
                messageItem?.messages?.remove(last)
            }
            //            checks for shared items coming in from device
            if (messageItem?.sharedText != null || messageItem?.sharedUri != null || !messageItem?.sharedUris?.isEmpty()!!) {
                try {
                    last = addNewMessageToMessageThread("", messageItem)
                } catch (e: CloneNotSupportedException) {
                    e.printStackTrace()
                }
            }
            //            lvNewMessage.setVisibility(View.GONE);
            //            adds a new message, if the user input is not empty
        } else {
            try {
                //                pop, then to sync the stack state.. always add to the thread
                last = messageItem?.popMessage()
                if (last != null && MessageItem.TAG.equals(last.dataId)) {
                    //                    a new message already exists, reset the typed text from user
                    last.text = txt
                    messageItem?.addMessage(last)
                } else {
                    //                adds a new messaage, for the first letter every time, with the user typed text
                    last = addNewMessageToMessageThread(txt, messageItem)
                }
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
            showSend()
        }
        //        createRecyclerAdapter();
        return last
    }

    private fun showSend() {
        btnSend?.visibility = View.VISIBLE
        btnDraft?.visibility = View.VISIBLE
    }

    private fun hideSend() {
        btnSend?.visibility = View.GONE
        btnDraft?.visibility = View.GONE
    }

    private fun showNewMessageWithLatestHeader(item: MessageItem) {
        messageItem?.subject = item.subject
        messageItem?.to = item.to
        messageItem?.cc = item.cc
        messageItem?.bcc = item.bcc
        //        lets the listeners know for the new headers
        peopleHeaderListener?.onPeopleHeaderChanged(messageItem)
        //        show the latest new message info
        var text = ""
        val messageItem = this.messageItem?.peekMessageInStack()
        if (messageItem != null) text = messageItem.text
        toggleNewMessageView(text)
    }

    private fun openAudioPicker() {
        startGetAudioContentActivityOnPermissions()
    }

    private fun openCameraPicker() {
        startGetCameraContentActivityOnPermissions()
    }

    private fun openVideoPicker() {
        startGetVideoContentActivityOnPermissions()
    }

    private fun openCallPicker() {
        startCallActivityOnPermissions()
    }

    private fun openSmsPicker() {
        startSMSActivity()
    }

    private fun startCallActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.CALL_PHONE
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CALL_PHONE),
                MainApplicationSingleton.REQUEST_CALL_PHONE_PERMISSION
            )
        } else {
            startCallActivity()
        }
    }

    private fun startGetCameraContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION
            )
        } else {
            startGetCameraContentActivity()
        }
    }

    private fun startGetVideoContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION
            )
        } else {
            startGetVideoContentActivity()
        }
    }

    private fun startGetAudioContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION
            )
        } else {
            startGetAudioContentActivity()
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

    private fun startGetContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                MainApplicationSingleton.REQUEST_CAMERA_AND_STORAGE_PERMISSION
            )
        } else {
            //            setupMediaChooser();
            //            openFilePicker();
            startGetContentActivity()
        }
    }

    private fun startSMSActivity() {
        val deviceContactItem = messageItem?.sharedDeviceContactItem
        val message = messageItem?.peekMessageInStack()
        if (deviceContactItem != null && message != null) {
            val text = message.text
            if (text != null && text.length > 0) {
                val length = text.length
                //                Set<MobileItem> mobiles = deviceContactItem.getMobiles();
                val mobiles = deviceContactItem.mobiles
                //                MobileItem phone = mobiles.iterator().next();
                var phone: String? = null
                try {
                    phone = mobiles.getString(0)
                    if (phone != null && !phone.isEmpty()) {
                        val intent = Intent(Intent.ACTION_SENDTO)
                        //                sms will make any apps respond
                        //                  BUT smsto ensures only SMS apps respond
                        intent.data = Uri.parse("smsto:$phone")
                        //                send only the first 120 chars
                        val len = if (length > 120) 120 else length
                        intent.putExtra("sms_body", text.substring(0, len))
                        //                intent.putExtra(Intent.EXTRA_STREAM, attachment);
                        if (intent.resolveActivity(appCompatActivity?.packageManager!!) != null) {
                            startActivity(intent)
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startCallActivity() {
        val deviceContactItem = messageItem?.sharedDeviceContactItem
        if (deviceContactItem != null) {
            //            Set<MobileItem> mobiles = deviceContactItem.getMobiles();
            val mobiles = deviceContactItem.mobiles
            var phone: String? = null
            try {
                phone = mobiles.getString(0)
                if (phone != null && !phone.isEmpty()) {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse("tel:$phone")
                    if (intent.resolveActivity(appCompatActivity?.packageManager!!) != null) {
                        startActivity(intent)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun startGetContentActivity() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // Filter to only show results that can be "opened", such as a file (as opposed to a list
        // of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

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
        startActivityForResult(intent, MainApplicationSingleton.ACTION_GET_CONTENT)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = "JPEG_$timeStamp" + "_"
        return MediaPickerFile.createImageFileInESPublicDir(file, ".jpg")
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = "3GP_$timeStamp" + "_"
        return MediaPickerFile.createImageFileInESPublicDir(file, ".3gp")
    }

    @Throws(IOException::class)
    private fun createAudioFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = "3GP_$timeStamp" + "_"
        // Save a file: path for use with ACTION_VIEW intents
        return MediaPickerFile.createSoundFileInESPublicDir(file, ".3gp")
    }

    private fun startGetCameraContentActivity() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        //        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(appCompatActivity?.packageManager!!) != null) {
            try {
                // Create the File where the photo should go
                val file = createImageFile()
                // Continue only if the File was successfully created
                cameraPhotoFileUri = Uri.fromFile(file)
                messageItem?.photoFileUri = cameraPhotoFileUri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoFileUri)
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
                startActivityForResult(
                    intent,
                    MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION
                )
            } catch (e: IOException) {
                // Error occurred while creating the File
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    private fun startGetVideoContentActivity() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        //        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(appCompatActivity?.packageManager!!) != null) {
            // Create the File where the photo should go
            try {
                val file = createVideoFile()
                // Continue only if the File was successfully created
                videoPhotoFileUri = Uri.fromFile(file)
                messageItem?.videoFileUri = videoPhotoFileUri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoPhotoFileUri)
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
                startActivityForResult(
                    intent,
                    MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION
                )
            } catch (e: IOException) {
                // Error occurred while creating the File
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    private fun startGetAudioContentActivity() {
        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        if (intent.resolveActivity(appCompatActivity?.packageManager!!) != null) {
            try {
                // Continue only if the File was successfully created
                // Create the File where the photo should go
                val file = createAudioFile()
                audioFileUri = Uri.fromFile(file)
                messageItem?.audioFileUri = audioFileUri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, audioFileUri)
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
                startActivityForResult(
                    intent,
                    MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION
                )
            } catch (e: IOException) {
                // Error occurred while creating the File
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainApplicationSingleton.REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //                setupMediaChooser();
                startGetContentActivity()
            }
        } else if (requestCode == MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //                setupMediaChooser();
                startGetAudioContentActivity()
            }
        } else if (requestCode == MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //                setupMediaChooser();
                startGetVideoContentActivity()
            }
        } else if (requestCode == MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //                setupMediaChooser();
                startGetCameraContentActivity()
            }
        }
    }

    private fun openFilePicker() {
        startGetContentActivityOnPermissions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (MainApplicationSingleton.ACTION_GET_CONTENT == requestCode && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    val clipData = intent.clipData
                    if (clipData == null) {
                        val uri = intent.data
                        if (uri != null) {
                            onAttachmentPicked(uri)
                        }
                    } else {
                        val count = clipData.itemCount
                        for (i in 0 until count) {
                            val uri = clipData.getItemAt(i).uri
                            if (uri != null) {
                                onAttachmentPicked(uri)
                            }
                        }
                    }
                }
            }
        } else if (MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION == requestCode && resultCode == Activity.RESULT_OK) {
            if (intent == null) {
                if (messageItem == null) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(arguments)
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem?.photoFileUri)
                }
            } else {
                //                Bundle extras = data.getExtras();
                //                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //            mImageView.setImageBitmap(imageBitmap);
                var uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
                if (uri == null) uri = intent.data
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION == requestCode && resultCode == Activity.RESULT_OK) {
            if (intent == null) {
                if (messageItem == null) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(arguments)
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem?.videoFileUri)
                }
            } else {
                //                Bundle extras = data.getExtras();
                //                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //            mImageView.setImageBitmap(imageBitmap);
                var uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
                if (uri == null) uri = intent.data
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION == requestCode && resultCode == Activity.RESULT_OK) {
            if (intent == null) {
                if (messageItem == null) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(arguments)
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem?.audioFileUri)
                }
            } else {
                //                Bundle extras = data.getExtras();
                //                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //            mImageView.setImageBitmap(imageBitmap);
                var uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
                if (uri == null) uri = intent.data
                onAttachmentPicked(uri)
            }
        }
    }

    private fun onAttachmentPicked(uri: Uri?, messageThreadItem: MessageItem?) {
        if (uri == null) {
            Log.e(TAG, "Attached picked is NULL")
        } else {
            try {
                if (messageThreadItem == null) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(arguments)
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                val file = MediaPickerUri.resolveToFile(appCompatActivity, uri)
                val attachmentItem = MessageItem(file.path)
                attachmentItem.downloadURL = file.absolutePath
                attachmentItem.name = file.name
                if (messageThreadItem != null) {
                    var messageItem = messageThreadItem.peekMessageInStack()
                    if (messageItem == null || !MessageItem.TAG.equals(messageItem.dataId)) {
                        //                        toggleNewMessageView("");
                        //                        messageItem = new MessageItem(MessageItem.TAG);
                        addNewMessageToMessageThread("", messageThreadItem)
                    }
                    messageItem = messageThreadItem.popMessage()
                    messageItem?.addAttachment(attachmentItem)
                    //                    adds message again, to resync stack after a pop
                    messageThreadItem.addMessage(messageItem)
                }
                createRecyclerAdapter()
                //                rvNewMessageAdapter.notifyDataSetChanged();
                //                rvNewMessage.requestLayout();
                Log.d(TAG, "Attachment Button click: $file")
                //                MediaPickerUri.dumpImageMetaData(getAppCompatActivity(), uri);
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    private fun onAttachmentPicked(uri: Uri?) {
        onAttachmentPicked(uri, messageItem)
    }

    @Throws(CloneNotSupportedException::class)
    private fun addNewMessageToMessageThread(txt: String, messageItem: MessageItem?): MessageItem? {
        var last: MessageItem? = null
        if (messageItem != null) {
            last = messageItem.popMessage()
            if (last == null || !MessageItem.TAG.equals(last.dataId)) {
                last = MessageItem()
                last.dataId = MessageItem.TAG
                //                sets in seconds
                last.timestamp = System.currentTimeMillis() / 1000
                last.fromName = user?.name
                last.to = messageItem.from
            }
            if (MessageItem.TAG.equals(last.dataId)) {

                last.timestamp = System.currentTimeMillis() / 1000
                last.fromName = user?.name
                last.to = messageItem.from

                last.text = txt
                last.chatId = messageItem.chatId
                last.toUid = messageItem.toUid
                last.toChatUid = messageItem.toChatUid

                last.subject = messageItem.subject
                last.cc = messageItem.cc
                last.bcc = messageItem.bcc
                //                last.setFromEmail(messageItem.getDocOwnerEmail());
                //                checks for shared content from the device
                if (messageItem.sharedText != null) {
                    last.text = messageItem.sharedText
                }
                messageItem.addMessage(last)
                if (messageItem.sharedUri != null) {
                    onAttachmentPicked(messageItem.sharedUri, messageItem)
                }
                val uris = messageItem.sharedUris
                for (uri in uris) {
                    onAttachmentPicked(uri, messageItem)
                }
            }
        }
        return last
    }

    override fun onClick(v: View) {}

    private fun setSelectedItem(mItem: MessageItem) {
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

    private fun execFlagMsgsTask(msgs: Array<MessageItem>) {
        flagMsgsTask = FlagMsgsTask(
            msgs,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_FLAG_MSGS
        )
        flagMsgsTask?.setFlagMsgsTaskListener(this)
        flagMsgsTask?.execute()
    }

    private fun execUnFlagMsgsTask(msgs: Array<MessageItem>) {
        unflagMsgsTask = UnFlagMsgsTask(
            msgs,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_UNFLAG_MSGS
        )
        unflagMsgsTask?.setUnFlagMsgsTaskListener(this)
        unflagMsgsTask?.execute()
    }

    private fun deleteMessages() {
        if (selectedItem == null) return
        execDeleteMsgsTask(arrayOf(selectedItem?.dataId!!))
    }

    private fun unflagMessages() {
        if (selectedItem == null) return
        selectedItem?.isFlagged = false
        execUnFlagMsgsTask(arrayOf(selectedItem!!))
    }

    private fun flagMessages() {
        if (selectedItem == null) return
        selectedItem?.isFlagged = true
        execFlagMsgsTask(arrayOf(selectedItem!!))
    }

    private fun forwardMessages() {
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

    private fun broadcastMessagesToNest() {
        val intent = Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_NEST)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, selectedItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        appCompatActivity?.finish()
    }

    private fun broadcastMessagesToDraft(messageItem: MessageItem) {
        val intent = Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_DRAFT)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        appCompatActivity?.finish()
    }

    private fun broadcastMessagesSend(messageItem: MessageItem) {
        val intent = Intent(MainApplicationSingleton.BROADCAST_MESSAGES_SEND)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity).sendBroadcast(intent)
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

    override fun onPostUnFlagMsgsExecute(response: JSONObject?, mids: Array<MessageItem>?, item: Array<String>) {
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || 99 == status || -1 == status) {
            //            retries again..
            onPostUnFlagMsgsExecuteFail(response, mids, item)
        } else {
            if (mids == null || mids.size == 0) return
            val values = ContentValues()
            values.put(MessageItemColumns.KEY_IS_FLAGGED, false)
            for (id in mids) {
                val uri: Uri
                if (id.isChat) {
                    uri = MessageChatContentProvider.RAW_CONTENT_URI
                } else {
                    uri = MessageEmailContentProvider.RAW_CONTENT_URI
                }
                appCompatActivity?.contentResolver?.update(
                    uri, values,
                    MessageItemColumns.KEY_DATA_ID + " = ? ",
                    arrayOf(id.dataId)
                )
            }
            restartLoader()
            Log.e(TAG, "onPostUnFlagMsgsExecute: SUCCESS - $response")
        }
    }

    override fun onPostUnFlagMsgsExecuteFail(response: JSONObject?, mids: Array<MessageItem>?, item: Array<String>) {
        Log.e(TAG, "UnFlag Msgs FAIL - $response: " + Arrays.toString(item))
    }

    override fun setUnFlagMsgsTaskToNull() {
        unflagMsgsTask = null
    }

    override fun onPostFlagMsgsExecute(response: JSONObject?, mids: Array<MessageItem>?, item: Array<String>) {
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || 99 == status || -1 == status) {
            //            retries again..
            onPostFlagMsgsExecuteFail(response, mids, item)
        } else {
            if (mids == null || mids.size == 0) return
            val values = ContentValues()
            values.put(MessageItemColumns.KEY_IS_FLAGGED, true)
            for (id in mids) {
                val uri: Uri
                if (id.isChat) {
                    uri = MessageChatContentProvider.RAW_CONTENT_URI
                } else {
                    uri = MessageEmailContentProvider.RAW_CONTENT_URI
                }
                appCompatActivity?.contentResolver?.update(
                    uri, values,
                    MessageItemColumns.KEY_DATA_ID + " = ? ",
                    arrayOf(id.dataId)
                )
            }
            restartLoader()
            Log.e(TAG, "onPostFlagMsgsExecute: SUCCESS - $response")
        }
    }

    override fun onPostFlagMsgsExecuteFail(response: JSONObject?, mids: Array<MessageItem>?, item: Array<String>) {
        Log.e(TAG, "Flag Msgs FAIL - $response: " + Arrays.toString(item))
    }

    override fun setFlagMsgsTaskToNull() {
        flagMsgsTask = null
    }

    override fun onPostDeleteMsgsResponse(response: JSONObject?, item: Array<String>) {
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || 99 == status || -1 == status) {
            //            retries again..
            onPostDeleteMsgsErrorResponse(response, item)
        } else {
            Log.e(TAG, "onPostDeleteMsgsResponse: SUCCESS - $response")
            deleteMessagesInDB(item)
        }
    }

    fun deleteMessagesInDB(item: Array<String>) {
        try {
            val count = MessageChatContentProvider.deleteMsgs(item, context)
            restartLoader()
            Log.e(TAG, "deleteMessagesInDB: Delete in DB: $count")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
    }

    override fun onPostDeleteMsgsErrorResponse(response: JSONObject?, item: Array<String>) {
        Log.e(TAG, "deleteMessagesInDB: Delete Msgs FAIL - $response: " + Arrays.toString(item))
        //        deleteMessagesInDB(item);
    }

    fun showNewMessageDialog() {
        // Create an instance of the dialog fragment and show it
        NewEmailDialogFragment.newMessageDialog(this, 2, user, messageItem).show(
            fragmentManager, "NewMessageDialog"
        )
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        //        // TODO: 16-03-2016
        /*
        MessageItem item = dialog.getArguments().getParcelable(
                MessageItem.TAG);
*/
        //        user must be signed into atleast one email account

        val newEmailDialogFragment = dialog as NewEmailDialogFragment
        val mtItem = MessageItem()
        mtItem.subject = newEmailDialogFragment.subject
        val to = newEmailDialogFragment.to
        val cc = newEmailDialogFragment.cc
        val bcc = newEmailDialogFragment.bcc
        /*
        String to = newEmailDialogFragment.getTo();
        String cc = newEmailDialogFragment.getCc();
        String bcc = newEmailDialogFragment.getBcc();
*/
        MessageItem.setMessageThreadEmailAddress(mtItem, to, cc, bcc)
        /*
        mtItem.setTo(to.toString());
        mtItem.setCc(cc.toString());
        mtItem.setBcc(bcc.toString());
*/
        showNewMessageWithLatestHeader(mtItem)
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

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
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

    /**
     * Handler of incoming messages from service.
     */
    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ChatService.MSG_SET_VALUE -> restartLoader()
                /*
                    getAppCompatActivity().getSupportLoaderManager().restartLoader(
                            MainApplicationSingleton.CHAT_MESSAGE_FRAGMENT_LOADERID, null,
                            ChatDetailFragment.this);
                    rvMessages.notify();
*/
                ChatService.MSG_SHOW_TYPING -> if (peopleListener != null) {
                    try {
                        val obj = msg.obj as String
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
                else -> super.handleMessage(msg)
            }
        }
    }

    inner class RecyclerViewAdapter(private val adapterItems: List<MessageItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        var bitmapFromUrlTask: BitmapFromUrlTask? = null
        var looperThread: HandlerThread? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_peopledetail_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItem = adapterItems[position]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                holder.mView.alpha = 1f
            }
            var me = false
            if (user?.dataId == holder.mItem?.fromUid) {
                me = true
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

            val rootView = holder.mView.rootView as LinearLayout
            rootView.gravity = Gravity.START
            /*
            if (rootView instanceof LinearLayout){
                    ((LinearLayout) rootView).setGravity(Gravity.END);
                    ((LinearLayout) rootView).setLayoutParams(params);
            }
*/
            //            rootView.setLayoutParams(LinearLayout.LayoutParams./*);
            /*
            if (rootView instanceof LinearLayout)
                ((LinearLayout) rootView).setGravity(Gravity.START);
*/
            val llChat = rootView.findViewById<LinearLayout>(R.id.ll_chat)
            //            llChat.setLayoutParams(startParams);
            llChat.background = getDrawable(R.drawable.shape_bubble_white)
            if (me) {
                //                rootView.setLayoutParams(endParams);
                rootView.gravity = Gravity.END
                //                llChat.setLayoutParams(endParams);
                //                llChat.setGravity(Gravity.END);
                llChat.background = getDrawable(R.drawable.shape_roundrect_blue)
                holder.tvMessage.setTextColor(getColor(R.color.white))
                //                    ||
                //                    MessageItem.TAG.equals(holder.userEmailItem.getDataId()))
                //            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    //                    holder.tvFromName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                    //                    holder.tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                }
            }
            val linearLayout = rootView.findViewById<LinearLayout>(R.id.list_item_chat_attch_msg)
            //            clears old views
            linearLayout.removeAllViews()

            holder.tvMessage.setTextColor(getColor(R.color.black))
            holder.tvMessage.text = holder.mItem?.text

            var flagged: Drawable? = null
            if (holder.mItem?.isFlagged!!) {
                flagged = getDrawable(R.drawable.ic_bookmark_black_18dp)
                if (flagged != null) flagged.setBounds(Rect(0, 0, 20, 20))
            }
            if (holder.mItem?.isDelivered!!) {
                var drawable = getDrawable(R.drawable.ic_done_all_black_18dp, appCompatActivity?.theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                if (me) setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged) else setCompoundDrawablesRelative(
                    holder.tvMessage,
                    drawable,
                    null,
                    flagged,
                    null
                )
            } else if (holder.mItem?.isRead!!) {
                var drawable = getDrawable(R.drawable.ic_done_black_18dp, appCompatActivity?.theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                if (me) setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged) else setCompoundDrawablesRelative(
                    holder.tvMessage,
                    drawable,
                    null,
                    flagged,
                    null
                )
            } else {
                var drawable = getDrawable(R.drawable.ic_restore_black_18dp, appCompatActivity?.theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                if (me) setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged) else setCompoundDrawablesRelative(
                    holder.tvMessage,
                    drawable,
                    null,
                    flagged,
                    null
                )
            }

            holder.tvFromName.visibility = View.VISIBLE

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
            val createDate = holder.mItem?.timestamp!! * 1000
            val timestamp = Timestamp(createDate)
            val now = System.currentTimeMillis()
            var df: DateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            if (now - createDate < 1000 * 60 * 60 * 24) {
                df = SimpleDateFormat("hh mm a", Locale.getDefault())
            }
            val dt = df.format(timestamp.time)
            //            holder.tvFromName.setText(holder.tvFromName.getText() + "  @" + dt);
            holder.tvFromName.text = dt

            if (MessageItem.TAG.equals(holder.mItem?.dataId)) {
                //            handles new message
                //            new message has the message thread item class name as an hack to indicate new

                if (holder.mItem?.attachments?.isEmpty()!!) {
                    /*
                    holder.mView.setBackground(getDrawable(android.R.color.transparent
                            , getContext().getTheme()));
*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        holder.mView.alpha = 1f
                    }
                    holder.mView.visibility = View.GONE
                    return
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        holder.mView.alpha = 0.5f
                    }
                    holder.tvFromName.visibility = View.GONE
                    holder.tvMessage.text = "Attachment preview - "
                    /*
                    holder.mView.setBackground(getDrawable(R.color.graylte
                            , getContext().getTheme()));
*/
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    linearLayout.background = getDrawable(R.color.red, context?.theme)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    linearLayout.background = getDrawable(
                        android.R.color.transparent,
                        context?.theme
                    )
                }
            }

            //            binds the attachments for each message
            val attachmentItems = holder.mItem?.attachments
            for (attachmentItem in attachmentItems!!) {
                val view = LayoutInflater.from(holder.mView.context).inflate(
                    R.layout.list_item_chat_attch_msg,
                    holder.mView.rootView as ViewGroup, false
                )
                val tv = view.findViewById<TextView>(R.id.tv_attach_name)
                tv.text = attachmentItem.name
                val progressBar = view.findViewById<ProgressBar>(R.id.progress)

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
                val downloadURL = attachmentItem.downloadURL
                if (downloadURL == null || downloadURL.isEmpty()) {

                    Log.e(TAG, "Attachment Download URL is NULL ")
                } else if (downloadURL.startsWith("/")) {
                    progressBar.visibility = View.GONE
                    val file = File(downloadURL)
                    val extension = MimeTypeMap.getFileExtensionFromUrl(
                        Uri.fromFile(file).toString()
                    )
                    val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

                    var bm: Bitmap?
                    var drawable: Drawable? = null
                    val type = attachmentItem.type
                    if (type != null && type.contains("image") || mimetype != null && mimetype.contains(
                            "image"
                        )
                    ) {
                        try {
                            bm = BitmapFactory.decodeFile(downloadURL)
                            drawable = BitmapDrawable(resources, bm)
                        } catch (ignored: Throwable) {
                            Log.e(TAG, TAG + ignored.message)
                        }
                    } else if (type != null && type.contains("video") || mimetype != null && mimetype.contains(
                            "video"
                        )
                    ) {
                        bm = ThumbnailUtils.createVideoThumbnail(
                            downloadURL, MediaStore.Video.Thumbnails.MINI_KIND
                        )
                        drawable = BitmapDrawable(resources, bm)
                    } else if (type != null && type.contains("audio") || mimetype != null && mimetype.contains(
                            "audio"
                        )
                    ) {
                        drawable = getDrawable(android.R.drawable.ic_media_play, appCompatActivity?.theme)
                    } else if (type != null && type.contains("application") || mimetype != null && mimetype.contains(
                            "application"
                        )
                    ) {
                        drawable = getDrawable(
                            android.R.drawable.ic_menu_slideshow,
                            appCompatActivity?.theme
                        )
                    } else {
                        drawable = ColorGenerator.getTextDrawable(attachmentItem.name)
                    }
                    if (drawable != null) {
                        drawable.setBounds(Rect(0, 0, 100, 100))
                        setCompoundDrawablesRelative(tv, drawable, null, null, null)
                    }
                    tv.setOnClickListener { v ->
                        MainApplicationSingleton.startMimeActivity(
                            file,
                            MsgChatAttachmentContentProvider.getMimeType(attachmentItem),
                            context
                        )
                    }
                } else if (downloadURL.startsWith("http")) {
                    if (holder.mItem?.fromUid?.equals(user?.dataId, ignoreCase = true)!!) {
                        //                        if self message, download immediately
                        //                    cloud pic.. not always a pic.. so image download is not always applicable
                        /*
                        bitmapFromUrlTask = new BitmapFromUrlTask(
                                tv, downloadURL, getAppCompatActivity().getApplicationContext());
                        bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                        bitmapFromUrlTask.execute();
*/
                        looperThread = MainApplicationSingleton.performOnHandlerThread {
                            MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                                appCompatActivity,
                                user,
                                attachmentItem
                            )
                            this@RecyclerViewAdapter.quitLooperThread()
                        }
                    } else {
                        var drawable = getDrawable(
                            R.drawable.ic_get_app_black_18dp,
                            appCompatActivity?.theme
                        )
                        assert(drawable != null)
                        drawable?.setBounds(Rect(0, 0, 100, 100))
                        setCompoundDrawablesRelative(tv, drawable, null, null, null)
                        val `val` = attachmentItem.type + " " + attachmentItem.size
                        tv.text = `val`
                        tv.setOnClickListener { v ->
                            Log.d(
                                TAG,
                                "Fetching attachment in background :" + attachmentItem.dataId
                            )
                            progressBar.visibility = View.VISIBLE
                            MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                                appCompatActivity,
                                user,
                                attachmentItem
                            )
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
                    }
                }
                linearLayout.addView(view)
            }
        }

        override fun getItemId(position: Int): Long {
            return adapterItems[position]._id
        }

        override fun getItemCount(): Int {
            return adapterItems.size
        }

        private fun quitLooperThread() {
            if (looperThread != null) looperThread?.quit()
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap, textView: View, context: Context?) {
            var ctx = context
            if (ctx == null) {
                ctx = context
            }
            if (ctx == null) return
            val resources = ctx.resources
            if (resources == null) return
            //            Bitmap roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds(Rect(0, 0, 100, 100))
            setCompoundDrawablesRelative((textView as TextView), drawable, null, null, null)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView),
            View.OnLongClickListener {
            //            the views
            val tvSub: TextView = mView.findViewById(R.id.tv_sub)
            val tvTo: TextView = mView.findViewById(R.id.tv_to)
            val tvCc: TextView = mView.findViewById(R.id.tv_cc)
            val tvBcc: TextView = mView.findViewById(R.id.tv_bcc)
            val tvFromName: TextView = mView.findViewById(R.id.tv_from_name)
            val tvMessage: TextView = mView.findViewById(R.id.tv_msg)
            //            the data
            var mItem: MessageItem? = null

            init {
                mView.setOnLongClickListener(this)
                mView.setOnClickListener(this@PeopleDetailFragment)
                mView.setOnClickListener(this@PeopleDetailFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvMessage.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                setSelectedItem(mItem!!)
                if (mActionMode == null) {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = appCompatActivity?.startSupportActionMode(mActionModeCallback)
                    //                view.setSelected(true);
                    v.isSelected = !v.isSelected
                    return true
                }
                v.isSelected = !v.isSelected
                return false
            }
        }
    }
}
