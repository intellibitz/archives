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
import androidx.core.app.NavUtils
import androidx.core.content.PermissionChecker
import androidx.fragment.app.DialogFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.IntellibitzActivity
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity
import intellibitz.intellidroid.content.MessageChatGroupContentProvider
import intellibitz.intellidroid.content.MessagesChatGroupContentProvider
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ChatListener
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.PeopleDetailListener
import intellibitz.intellidroid.listener.PeopleHeaderListener
import intellibitz.intellidroid.service.ChatService
import intellibitz.intellidroid.service.ContactService
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
class MessageChatGroupFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    ContactListener,
    PeopleDetailListener,
    NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
    LoaderManager.LoaderCallbacks<Cursor>,
    MarkReadTask.MarkReadTaskListener,
    View.OnClickListener,
    DeleteMsgsTask.DeleteMsgsTaskListener,
    FlagMsgsTask.FlagMsgsTaskListener,
    UnFlagMsgsTask.UnFlagMsgsTaskListener,
    CreateDraftTask.CreateDraftTaskListener {

    companion object {
        const val TAG = "MessageChatGroupFrag"

        fun newInstance(
            messageItem: MessageItem,
            user: ContactItem,
            chatListener: ChatListener?
        ): MessageChatGroupFragment {
            val fragment = MessageChatGroupFragment()
            fragment.setUser(user)
            fragment.setMessageItem(messageItem)
            if (chatListener is PeopleDetailListener) {
                fragment.setChatMessageListener(chatListener)
            }
            if (chatListener is PeopleHeaderListener) {
                fragment.setChatMessageHeaderListener(chatListener)
            }
            if (chatListener != null) {
                fragment.setViewModeListener(chatListener)
            }
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            args.putParcelable(MessageItem.TAG, messageItem)
            fragment.setArguments(args)
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

    private var cameraPhotoFileUri: Uri? = null
    private var videoPhotoFileUri: Uri? = null
    private var audioFileUri: Uri? = null
    private var messageThreadContentObserver: ContentObserver? = null
    private var attachmentContentObserver: ContentObserver? = null
    private var btnSend: ImageButton? = null
    private var btnUpload: ImageButton? = null
    private var llUpload: View? = null
    private var btnDraft: Button? = null
    private var etMessageInput: EditText? = null
    private var ivAttach: ImageButton? = null
    private var btnAudio: ImageButton? = null
    private var btnCamera: ImageButton? = null
    private var btnCall: ImageButton? = null
    private var btnSms: ImageButton? = null
    private var rvMessages: RecyclerView? = null
    private var rvMessagesAdapter: RecyclerView.Adapter<*>? = null
    private var markReadTask: MarkReadTask? = null
    private var deleteMsgsTask: DeleteMsgsTask? = null
    private var flagMsgsTask: FlagMsgsTask? = null
    private var unflagMsgsTask: UnFlagMsgsTask? = null

    /**
     * The fragment argument representing the messageItem ID that this fragment
     * represents.
     */
    private var messageItem: MessageItem? = null
    private var selectedItem: MessageItem? = null
    private var selfTypingMessage: MessageItem? = null

    //    private String id;
//    private String topic;
    private var c: Context? = null
    private var chatMessageHeaderListener: PeopleHeaderListener? = null
    private var title: String? = null
    private var subTitle: String? = null
    private var filter: String? = null
    private var btnVideo: ImageButton? = null
    private var looperThread: HandlerThread? = null
    private var createDraftTask: CreateDraftTask? = null
    private var draftLooperThread: HandlerThread? = null
    private var chatMessageListener: PeopleDetailListener? = null

    //    private Toolbar detailToolbar;
//    private Toolbar actionBar;
    private var toolbar: Toolbar? = null

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
                var msg = Message.obtain(null, ChatService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)

                // Give it some value as an example.
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
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null
            //            mCallbackText.setText("Disconnected.");
        }
    }

    /*
        public void setActionBar(Toolbar actionBar) {
            this.actionBar = actionBar;
        }
    */
    private var mActionMode: ActionMode? = null
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
                    forwardMessagesToNest()
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

    fun setChatMessageHeaderListener(chatMessageHeaderListener: PeopleHeaderListener) {
        this.chatMessageHeaderListener = chatMessageHeaderListener
    }

    private fun showTyping(toUid: String, fromUid: String, fromName: String, user: ContactItem) {
        if (fromUid == user.getDataId()) {
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
        if (null == chatMessageListener) {
            // // TODO: 21-05-2016
            //            listeners need to be saved, restored or recreated brand new every time
        } else {
            chatMessageListener?.onPeopleTyping(text)
            //        getAppCompatActivity().getToolbar().setSubtitle(text);
            //        toolbar.setSubtitleTextColor(Color.GREEN);
        }
    }

    /*
    private void sendMessage(String name, String message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_TITLE, name);
        contentValues.put(DatabaseHelper.KEY_DESCRIPTION, message);
        getAppCompatActivity().getContentResolver().insert(
                ChatMessageChatContentProvider.CONTENT_URI, contentValues);
    //        getSupportLoaderManager().restartLoader(0, null, this);
    //        mAdapter.notifyDataSetChanged();
        // Give it some value as an example.
        Message msg = Message.obtain(null,
                ChatService.MSG_SET_VALUE, this.hashCode(), 0);
        msg.obj = message;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
*/

    override fun onPeopleTypingStopped(text: String) {
        if (null == chatMessageListener) {
            // // TODO: 21-05-2016
            //            listeners need to be saved, restored or recreated brand new every time
        } else {
            chatMessageListener?.onPeopleTypingStopped(text)
            //        getAppCompatActivity().getToolbar().setSubtitle(text);
            //        toolbar.setSubtitleTextColor(Color.WHITE);
        }
    }

    fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        getAppCompatActivity().bindService(
            Intent(getAppCompatActivity(), ChatService::class.java),
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
                    val msg = Message.obtain(null, ChatService.MSG_UNREGISTER_CLIENT)
                    msg.replyTo = mMessenger
                    mService?.send(msg)
                } catch (e: RemoteException) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            getAppCompatActivity().unbindService(mConnection)
            mIsBound = false
            //            mCallbackText.setText("Unbinding.");
        }
    }

    private fun sendOnKey(messageItem: MessageItem, user: ContactItem) {
        if (null == mService) {
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
                ChatMessageChatContentProvider.CONTENT_URI, contentValues);
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
        getAppCompatActivity().getSupportLoaderManager().restartLoader(
                MainApplication.EMAIL_MESSAGE_FRAGMENT_LOADERID, null, this);
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

    fun setChatMessageListener(chatMessageListener: PeopleDetailListener) {
        this.chatMessageListener = chatMessageListener
    }

    fun getMessageItem(): MessageItem? {
        return messageItem
    }

    fun setMessageItem(messageItem: MessageItem) {
        this.messageItem = messageItem
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        restoreStateFromFragmentArguments(savedInstanceState)
        doBindService()
    }

    private fun restoreStateFromFragmentArguments(arguments: Bundle?) {
        var args = arguments
        if (null == args) {
            args = getArguments()
        }
        if (args != null) {
            if (null == user) {
                user = args.getParcelable(ContactItem.USER_CONTACT)
            }
            if (null == messageItem) {
                messageItem = args.getParcelable(MessageItem.TAG)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        outState.putParcelable(MessageItem.TAG, messageItem)
    }

    override fun onDestroy() {
        doUnbindService()
        getAppCompatActivity().getContentResolver().unregisterContentObserver(messageThreadContentObserver)
        getAppCompatActivity().getContentResolver().unregisterContentObserver(attachmentContentObserver)
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (chatMessageListener != null) {
            chatMessageListener?.onPeopleTypingStopped(messageItem?.getName())
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
                if (null == etMessageInput && getView() != null) {
                    etMessageInput = getView()?.findViewById(R.id.tv_chat_message)
                }
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
                                MessageChatGroupContentProvider.createsMessageItemFromCursor(
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
        getAppCompatActivity().getContentResolver().registerContentObserver(
            MessageChatGroupContentProvider.CONTENT_URI,
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
        getAppCompatActivity().getContentResolver().registerContentObserver(
            MsgChatGrpAttachmentContentProvider.CONTENT_URI,
            true,
            attachmentContentObserver
        )
    }

    private fun quitLooperThread() {
        if (looperThread != null) {
            looperThread?.quit()
        }
        sendMessage(messageItem, user)
        broadcastMessagesSend(messageItem)
    }

    private fun quitDraftLooperThread() {
        if (draftLooperThread != null) {
            draftLooperThread?.quit()
        }
    }

    override fun onPostCreateDraftFromCloudExecute(response: JSONObject?, messageItem: MessageItem) {
        etMessageInput?.isEnabled = true
        etMessageInput?.setText("")
        Log.e(TAG, "onPostCreateDraftFromCloudExecute: $response")
        var status = 0
        if (response != null) {
            status = response.optInt("status")
        }
        if (null == response || -1 == status || 99 == status) {
            //            retries again..
            onPostCreateDraftFromCloudExecuteFail(response, messageItem)
        } else {
            try {
                val id = response?.getString("draft_id")
                messageItem.setDataId(id)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messagechatgroup, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppBar()
        etMessageInput = view.findViewById(R.id.tv_chat_message)
        btnSend = view.findViewById(R.id.btn_chat_send)
        btnUpload = view.findViewById(R.id.btn_upload)
        llUpload = view.findViewById(R.id.ll_upload)
        btnUpload?.setOnClickListener {
            val visibility = llUpload?.visibility
            if (View.VISIBLE == visibility) {
                llUpload?.visibility = View.GONE
            } else {
                llUpload?.visibility = View.VISIBLE
            }
        }
        btnDraft = view.findViewById(R.id.btn_draft)
        ivAttach = view.findViewById(R.id.iv_chat_attach)
        btnAudio = view.findViewById(R.id.ib_audio)
        btnCamera = view.findViewById(R.id.ib_camera)
        btnVideo = view.findViewById(R.id.ib_video)
        btnCall = view.findViewById(R.id.ib_call)
        btnSms = view.findViewById(R.id.ib_sms)
        rvMessages = view.findViewById(R.id.rv_chat_messages)
        if (null == savedInstanceState) {
            user = getArguments()?.getParcelable(ContactItem.USER_CONTACT)
            messageItem = getArguments()?.getParcelable(MessageItem.TAG)
            etMessageInput?.requestFocus()
            etMessageInput?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    toggleNewMessageView(s)
                    sendOnKey(messageItem, user)
                }

                override fun afterTextChanged(s: Editable) {}
            })

            btnDraft?.setOnClickListener {
                etMessageInput?.isEnabled = false
                val message = etMessageInput?.text.toString()
                if (TextUtils.isEmpty(message)) {
                    return@setOnClickListener
                }
                messageItem?.flagStackMessageToSend()
                messageItem?.getMessageItemConcurrentLinkedQueue()?.add(messageItem?.popMessage())
                //                    MessageItem messageItem = messageItem.popMessage();
                draftLooperThread = MainApplicationSingleton.performOnHandlerThread {
                    val messageItemConcurrentLinkedQueue = messageItem?.getMessageItemConcurrentLinkedQueue()
                    val messageItems: Array<MessageItem>
                    synchronized(lock) {
                        messageItems = messageItemConcurrentLinkedQueue?.toArray(arrayOfNulls<MessageItem>(0))!!
                    }
                    for (messageItem in messageItems) {
                        if (messageItem.isReadyToSend()) {
                            messageItem.setBaseType(MessageItem.DRAFT)
                            //                                    the cloud treats this doc type as draft
                            messageItem.setDocType(MessageItem.DRAFT)
                            messageItem.setDocOwner(user?.getDataId())
                            messageItem.setType(MessageItem.CHAT)
                            messageItem.setMessageType(MessageItem.CHAT)
                            messageItem.setToType(MessageItem.GROUP)
                            messageItem.setGroup(true)
                            messageItem.setEmailItem(false)
                            messageItem.setChatId(this@MessageChatGroupFragment.messageItem?.getChatId())
                            messageItem.setDataId(this@MessageChatGroupFragment.messageItem?.getChatId())
                            //                                        // TODO: 19-06-2016
                            //                                        set chat_msg_ref from app
                            //                                        chat_msg_ref format: fromuid_touid_timestamp
                            messageItem.setChatMsgRef(
                                user?.getDataId() + "_" + messageItem.getChatId() + "_" + System.currentTimeMillis()
                            )
                            createDraftTask = CreateDraftTask(
                                messageItem,
                                user?.getDataId(),
                                user?.getToken(),
                                user?.getDevice(),
                                user?.getDeviceRef(),
                                MainApplicationSingleton.AUTH_CREATE_DRAFT
                            )
                            createDraftTask?.setCreateDraftTaskListener(this@MessageChatGroupFragment)
                            createDraftTask?.execute()
                        }
                    }
                    this@MessageChatGroupFragment.quitDraftLooperThread()
                }
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
                                        Uri uri = MessageChatGroupContentProvider.savesMessageItem(
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
            btnSend?.setOnClickListener {
                etMessageInput?.isEnabled = false
                //                    // TODO: 22-05-2016
                //                    to mainitain input Q, the user can type and hit send madly like a million times
                val message = etMessageInput?.text.toString()
                if (TextUtils.isEmpty(message)) {
                    return@setOnClickListener
                }
                messageItem?.flagStackMessageToSend()
                messageItem?.getMessageItemConcurrentLinkedQueue()?.add(messageItem?.popMessage())
                //                not required.. message would be already populated
                //                messageItem.getMessages().setText(message);
                //                messageItem.getMessages().addAttachments(messageItem.getAttachments());
                //                    saves to db before sending to cloud
                looperThread = MainApplicationSingleton.performOnHandlerThread {
                    val messageItemConcurrentLinkedQueue = messageItem?.getMessageItemConcurrentLinkedQueue()
                    val messageItems: Array<MessageItem>
                    synchronized(lock) {
                        messageItems = messageItemConcurrentLinkedQueue?.toArray(arrayOfNulls<MessageItem>(0))!!
                    }
                    for (messageItem in messageItems) {
                        if (messageItem.isReadyToSend()) {
                            try {
                                messageItem.setDocType(MessageItem.MSG)
                                messageItem.setType(MessageItem.CHAT)
                                messageItem.setMessageType(MessageItem.CHAT)
                                messageItem.setToType(MessageItem.GROUP)
                                messageItem.setGroup(true)
                                messageItem.setEmailItem(false)
                                messageItem.setChatId(this@MessageChatGroupFragment.messageItem?.getChatId())
                                messageItem.setGroupId(this@MessageChatGroupFragment.messageItem?.getChatId())
                                messageItem.setDataId(this@MessageChatGroupFragment.messageItem?.getChatId())
                                messageItem.setThreadId(this@MessageChatGroupFragment.messageItem?.getChatId())
                                messageItem.setThreadIdRef(this@MessageChatGroupFragment.messageItem?.getChatId())
                                //                                        messageItem.setToType(MessageChatGroupFragment.this.messageItem.getToType());
                                //                                        // TODO: 19-06-2016
                                //                                        set chat_msg_ref from app
                                //                                        chat_msg_ref format: fromuid_touid_timestamp
                                messageItem.setChatMsgRef(
                                    user?.getDataId() + "_" + messageItem.getChatId() + "_" + System.currentTimeMillis()
                                )
                                // creates the chat group contact, from the chat id.. so the contact can be messaged
                                MessageChatGroupContentProvider.createsContactsFromMessage(messageItem)
                                val uri = MessageChatGroupContentProvider.savesMessageItem(messageItem, user, context)
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
                    this@MessageChatGroupFragment.quitLooperThread()
                }
                //                    sendMessage(messageItem, user);
                //                empties the input text
                //                etMessageInput.setText("");
            }

            ivAttach?.setOnClickListener { openFilePicker() }
            btnAudio?.setOnClickListener { openAudioPicker() }
            btnCamera?.setOnClickListener { openCameraPicker() }
            btnVideo?.setOnClickListener { openVideoPicker() }
            btnCall?.setOnClickListener { openCallPicker() }
            btnSms?.setOnClickListener { openSmsPicker() }
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            messageItem = savedInstanceState.getParcelable(MessageItem.TAG)
        }
        if (messageItem != null) {
            //            set read as true, if the message is NOT new
            if (!MessageItem.TAG.equals(messageItem?.getDataId())) {
                if (messageItem?.getUnreadCount()!! > 0) {
                    execMarkReadTask(messageItem)
                } else {
                    Log.e(TAG, "No unread message - mark read is not invoked")
                }
            }
            //            createRecyclerAdapter();
            //            toggleNewMessageView("");
        }
        setChatHeaders(messageItem)
        toolbar?.setOnClickListener {
            if (viewModeListener != null) {
                viewModeListener?.onViewModeChanged()
            }
            //                showContactThreadDetailFragment();
            startMsgChatGrpContactsActivity()
        }
        val messageItemToForward = messageItem?.popMessage()
        if (messageItemToForward != null && !MessageItem.TAG.equals(messageItemToForward?.getDataId())) {
            //            toggleNewMessageView(messageItemToForward.getText());
            etMessageInput?.setText(messageItemToForward?.getText())
            messageItemToForward = null
        }
        restartLoader()
        //        // TODO: 28-03-2016
        //        already done in content fragment.. not required
        /*
        getAppCompatActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.MESSAGECHATGROUP_LOADERID, null,
                ChatDetailFragment.this);
*/
    }

    fun broadcastContactForProfile(messageItem: MessageItem) {
        val intent = Intent(MainApplicationSingleton.BROADCAST_CONTACT_PROFILE_VIEW)
        intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, messageItem.getIntellibitzId())
        LocalBroadcastManager.getInstance(getAppCompatActivity()).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        getAppCompatActivity().finish()
    }

    private fun broadcastMessagesSend(messageItem: MessageItem?) {
        val intent = Intent(MainApplicationSingleton.BROADCAST_MESSAGES_SEND)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable?)
        LocalBroadcastManager.getInstance(getAppCompatActivity()).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        //        getAppCompatActivity().finish();
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(
                getAppCompatActivity(),
                Intent(getAppCompatActivity(), IntellibitzActivity::class.java)
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupAppBar() {
        toolbar = view?.findViewById(R.id.toolbar)
        toolbar?.setTitle(R.string.new_group)
        toolbar?.setSubtitle(R.string.app_title)
        getAppCompatActivity().setSupportActionBar(toolbar)
        // Show the Up button in the action bar.
        val actionBar = getAppCompatActivity().supportActionBar
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
    fun startMsgChatGrpContactsActivity() {
        val contactItem = messageItem?.getContactItem()
        if (null == contactItem || contactItem.isEmpty()) {
            /*
            IntellibitzActivity.alertDialog(getAppCompatActivity(),
                    "Contact Info not available", "Group Info");
*/
            if (null == contactItem) {
                MessageChatGroupContentProvider.createsContactsFromMessage(messageItem)
            } else {
                if (null == contactItem.getDataId()) {
                    contactItem.setDataId(messageItem?.getDataId())
                }
            }
            ContactService.getGroupDetails(contactItem, user, getAppCompatActivity())
            broadcastContactForProfile(messageItem)
            return
        }
        startMsgChatGrpContactsDetailActivity(contactItem, user)
    }

    fun startMsgChatGrpContactsDetailActivity(contactItem: ContactItem, user: ContactItem) {
        val intent = Intent(getAppCompatActivity(), MsgChatGrpContactsDetailActivity::class.java)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable?)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE)
    }

    fun showContactThreadDetailFragment() {
        startMsgChatGrpContactsActivity()
        /*
        msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(MessageChatGroupFragment.this,
                        getAppCompatActivity(), twoPane,
                        messageItem.getContactItem(), user);
        getAppCompatActivity().removeFragment(msgChatGrpContactsDetailFragment);
        getAppCompatActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
*/
    }

    /*
    private Toolbar setupDetailToolbar() {
    //        detailToolbar = getAppCompatActivity().getDetailToolbar();
        return actionBar;
    }
*/

    fun onBackPressed(): Boolean {
        return false
    }

    fun setChatHeaders(messageItem: MessageItem?) {
        /*
        Toolbar toolbar = setupDetailToolbar();
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
        getAppCompatActivity().onBackPressed()
    }

    private fun setupDetailTitle(messageItem: MessageItem?, toolbar: Toolbar?) {
        if (null == messageItem) {
            Log.e(TAG, "MessageThread NULL")
            return
        }
        title = messageItem?.getName()
        if (null == title || 0 == title?.length) {
            val deviceContactItem = messageItem?.getSharedDeviceContactItem()
            if (deviceContactItem != null) {
                title = deviceContactItem.getName()
                if (null == title || 0 == title?.length) {
                    title = deviceContactItem.getFirstName()
                }
                if (null == title || 0 == title?.length) {
                    title = deviceContactItem.getLastName()
                }
            }
            if (null == title || 0 == title?.length) {
                title = messageItem?.getChatId()
            }
            if (null == title || 0 == title?.length) {
                title = messageItem?.getDocOwner()
            }
        }
        var from = messageItem?.getDocSender()
        if (null == from || 0 == from?.length) {
            from = messageItem?.getFrom()
        }
        subTitle = "last seen on"
        /*
            String recipients = MessageItem.getSingleAddress(messageItem.getCc(),
                    messageItem.getTo(), from, user.getEmail(), user.getName());
*/
        val contactItem = messageItem?.getContactItem()
        if (contactItem != null) {
            val names = contactItem.getContactsNameAsArray()
            if (null == names || 0 == names.size) {
                subTitle = "last seen on"
            } else {
                subTitle = ""
                for (name in names) {
                    if (name != null && !subTitle.isEmpty() && !subTitle.contains(name)) {
                        subTitle += ","
                    }
                    subTitle += name
                }
            }
        }
        if (toolbar != null) {
            toolbar.setTitle(title)
            toolbar.setSubtitle(subTitle)
        }
    }

    @Nullable
    private fun setMessageHeaders(messageItem: MessageItem): Toolbar? {
        //        Toolbar toolbar = setupDetailToolbar();
        if (toolbar != null) {
            toolbar?.setNavigationIcon(R.drawable.ic_dialog_close_light)
            toolbar?.setNavigationOnClickListener {
                performNavigationClose()
            }
            var from = messageItem.getDocSender()
            if (null == from) {
                from = messageItem.getFrom()
            }
            val recipients = MessageItem.getSingleAddress(
                messageItem.getCc(),
                messageItem.getTo(),
                from,
                user.getEmail(),
                user.getName()
            )
            toolbar?.setTitle(messageItem.getSubject())
            toolbar?.setSubtitle(recipients)
        }
        return toolbar
    }

    private fun execMarkReadTask(messageItem: MessageItem) {
        markReadTask = MarkReadTask(
            context,
            messageItem,
            user?.getDataId(),
            user?.getToken(),
            user?.getDevice(),
            user?.getDeviceRef(),
            MainApplicationSingleton.AUTH_MARK_READ
        )
        markReadTask?.setMarkReadTaskListener(this)
        markReadTask?.execute()
    }

    override fun onPostMarkReadExecute(response: JSONObject?, item: MessageItem) {
        var status = 0
        if (response != null) {
            status = response.optInt("status")
        }
        if (null == response || 99 == status || -1 == status) {
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
            if (id > 0) {
                createRecyclerAdapter(id)
            }
        } catch (e: NumberFormatException) {
            //            ignore
        }
    }

    private fun createRecyclerAdapter(id: Long) {
        val cursor = getAppCompatActivity().getContentResolver().query(
            Uri.withAppendedPath(MsgChatGrpAttachmentContentProvider.CONTENT_URI, id.toString()),
            arrayOf(MessageItemColumns.KEY_DOWNLOAD_URL),
            MessageItemColumns.KEY_ID,
            arrayOf(id.toString()),
            null
        )
        if (null == cursor) {
            return
        }
        if (cursor.count > 0) {
            val attachmentItem = messageItem?.getAttachment(id)
            //            // TODO: 18-05-2016
            //            null check
            if (attachmentItem != null) {
                attachmentItem.setDownloadURL(
                    cursor.getString(
                        cursor.getColumnIndex(
                            MessageItemColumns.KEY_DOWNLOAD_URL
                        )
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
            if (last != null && MessageItem.TAG.equals(last.getDataId())) {
                selfTypingMessage = last
            }
        }
        //        first time init
        try {
            getAppCompatActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.MESSAGECHATGROUP_LOADERID,
                null,
                this
            )
        } catch (ignored: NullPointerException) {
            Log.e(TAG, "$TAG${ignored.message}")
        }
    }

    private fun createRecyclerAdapter() {
        if (null == rvMessages && getView() != null) {
            rvMessages = getView()?.findViewById(R.id.rv_chat_messages)
        }
        if (null == rvMessages) {
            Log.e(TAG, "createRecyclerAdapter: Recycler view NULL")
            return
        }
        val messages = messageItem?.getMessages()
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
            if (last != null && MessageItem.TAG.equals(last.getDataId()) && !last.getAttachments().isEmpty()) {
                //                remove only, when the latest message.. is the new message created here
                //                retains the valid db saved latest message
                messageItem?.getMessages()?.remove(last)
            }
            //            checks for shared items coming in from device
            if (messageItem?.getSharedText() != null || messageItem?.getSharedUri() != null || !messageItem?.getSharedUris()?.isEmpty()!!) {
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
                if (last != null && MessageItem.TAG.equals(last.getDataId())) {
                    //                    a new message already exists, reset the typed text from user
                    last.setText(txt)
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
        messageItem?.setSubject(item.getSubject())
        messageItem?.setTo(item.getTo())
        messageItem?.setCc(item.getCc())
        messageItem?.setBcc(item.getBcc())
        //        lets the listeners know for the new headers
        chatMessageHeaderListener?.onPeopleHeaderChanged(messageItem)
        //        show the latest new message info
        var text = ""
        val messageItem = this.messageItem?.peekMessageInStack()
        if (messageItem != null) {
            text = messageItem.getText()
        }
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
        val deviceContactItem = messageItem?.getSharedDeviceContactItem()
        val message = messageItem?.peekMessageInStack()
        if (deviceContactItem != null && message != null) {
            val text = message.getText()
            if (text != null && text.length > 0) {
                val length = text.length
                //                Set<MobileItem> mobiles = deviceContactItem.getMobiles();
                val mobiles = deviceContactItem.getMobiles()
                //                MobileItem phone = mobiles.iterator().next();
                var phone: String? = null
                try {
                    phone = mobiles.getString(0)
                    if (phone != null && !phone.isEmpty()) {
                        val intent = Intent(Intent.ACTION_SENDTO)
                        //                sms will make any apps respond
                        //                  BUT smsto ensures only SMS apps respond
                        intent.setData(Uri.parse("smsto:$phone"))
                        //                send only the first 120 chars
                        val len = if (length > 120) 120 else length
                        intent.putExtra("sms_body", text.substring(0, len))
                        //                intent.putExtra(Intent.EXTRA_STREAM, attachment);
                        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
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
        val deviceContactItem = messageItem?.getSharedDeviceContactItem()
        if (deviceContactItem != null) {
            //            Set<MobileItem> mobiles = deviceContactItem.getMobiles();
            val mobiles = deviceContactItem.getMobiles()
            var phone: String? = null
            try {
                phone = mobiles.getString(0)
                if (phone != null && !phone.isEmpty()) {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.setData(Uri.parse("tel:$phone"))
                    if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
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

        intent.setType("*/*")
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
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            try {
                // Create the File where the photo should go
                val file = createImageFile()
                // Continue only if the File was successfully created
                cameraPhotoFileUri = Uri.fromFile(file)
                messageItem?.setPhotoFileUri(cameraPhotoFileUri)
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
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                val file = createVideoFile()
                // Continue only if the File was successfully created
                videoPhotoFileUri = Uri.fromFile(file)
                messageItem?.setVideoFileUri(videoPhotoFileUri)
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
        if (intent.resolveActivity(getAppCompatActivity().getPackageManager()) != null) {
            try {
                // Continue only if the File was successfully created
                // Create the File where the photo should go
                val file = createAudioFile()
                audioFileUri = Uri.fromFile(file)
                messageItem?.setAudioFileUri(audioFileUri)
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
                    if (null == clipData) {
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
            if (null == intent) {
                if (null == messageItem) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(getArguments())
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem?.getPhotoFileUri())
                }
            } else {
                //                Bundle extras = data.getExtras();
                //                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //            mImageView.setImageBitmap(imageBitmap);
                var uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
                if (null == uri) {
                    uri = intent.data
                }
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION == requestCode && resultCode == Activity.RESULT_OK) {
            if (null == intent) {
                if (null == messageItem) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(getArguments())
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem?.getVideoFileUri())
                }
            } else {
                //                Bundle extras = data.getExtras();
                //                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //            mImageView.setImageBitmap(imageBitmap);
                var uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
                if (null == uri) {
                    uri = intent.data
                }
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION == requestCode && resultCode == Activity.RESULT_OK) {
            if (null == intent) {
                if (null == messageItem) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(getArguments())
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                if (messageItem != null) {
                    onAttachmentPicked(messageItem?.getAudioFileUri())
                }
            } else {
                //                Bundle extras = data.getExtras();
                //                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //            mImageView.setImageBitmap(imageBitmap);
                var uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
                if (null == uri) {
                    uri = intent.data
                }
                onAttachmentPicked(uri)
            }
        }
    }

    private fun onAttachmentPicked(uri: Uri?, messageThreadItem: MessageItem?) {
        if (null == uri) {
            Log.e(TAG, "Attached picked is NULL")
        } else {
            try {
                if (null == messageThreadItem) {
                    //                force load the latest
                    restoreStateFromFragmentArguments(getArguments())
                    /*
                    messageItem = getArguments().getParcelable(
                            MessageItem.TAG);
*/
                }
                val file = MediaPickerUri.resolveToFile(getAppCompatActivity(), uri)
                val attachmentItem = MessageItem(file.path)
                attachmentItem.setDownloadURL(file.absolutePath)
                attachmentItem.setName(file.name)
                if (messageThreadItem != null) {
                    var messageItem = messageThreadItem.peekMessageInStack()
                    if (null == messageItem || !MessageItem.TAG.equals(messageItem?.getDataId())) {
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
    private fun addNewMessageToMessageThread(
        txt: String,
        messageItem: MessageItem?
    ): MessageItem? {
        var last: MessageItem? = null
        if (messageItem != null) {
            last = messageItem.popMessage()
            if (null == last || !MessageItem.TAG.equals(last.getDataId())) {
                last = MessageItem()
                last.setDataId(MessageItem.TAG)
                //                sets in seconds
                last.setTimestamp(System.currentTimeMillis() / 1000)
                last.setFromName(user?.getName())
                last.setTo(messageItem.getFrom())
            }
            if (MessageItem.TAG.equals(last.getDataId())) {

                last.setTimestamp(System.currentTimeMillis() / 1000)
                last.setFromName(user?.getName())
                last.setTo(messageItem.getFrom())

                last.setText(txt)
                last.setChatId(messageItem.getChatId())
                last.setToUid(messageItem.getToUid())
                last.setToChatUid(messageItem.getToChatUid())
                last.setToType(MessageItem.GROUP)

                last.setSubject(messageItem.getSubject())
                last.setCc(messageItem.getCc())
                last.setBcc(messageItem.getBcc())
                //                last.setFromEmail(messageItem.getDocOwnerEmail());
                //                checks for shared content from the device
                if (messageItem.getSharedText() != null) {
                    last.setText(messageItem.getSharedText())
                }
                messageItem.addMessage(last)
                if (messageItem.getSharedUri() != null) {
                    onAttachmentPicked(messageItem.getSharedUri(), messageItem)
                }
                val uris = messageItem.getSharedUris()
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
            user?.getDataId(),
            user?.getToken(),
            user?.getDevice(),
            user?.getDeviceRef(),
            MainApplicationSingleton.AUTH_DELETED_MSGS,
            context
        )
        deleteMsgsTask.setRequestTimeoutMillis(30000)
        deleteMsgsTask.setDeleteMsgsTaskListener(this)
        deleteMsgsTask.execute()
        /*
        deleteMsgsTask = new DeleteMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_DELETED_MSGS);
        deleteMsgsTask.setDeleteMsgsTaskListener(this);
        deleteMsgsTask.execute();
*/
    }

    private fun execFlagMsgsTask(msgs: Array<String>) {
        flagMsgsTask = FlagMsgsTask(
            msgs,
            user?.getDataId(),
            user?.getToken(),
            user?.getDevice(),
            user?.getDeviceRef(),
            MainApplicationSingleton.AUTH_FLAG_MSGS
        )
        flagMsgsTask?.setFlagMsgsTaskListener(this)
        flagMsgsTask?.execute()
    }

    private fun execUnFlagMsgsTask(msgs: Array<String>) {
        unflagMsgsTask = UnFlagMsgsTask(
            msgs,
            user?.getDataId(),
            user?.getToken(),
            user?.getDevice(),
            user?.getDeviceRef(),
            MainApplicationSingleton.AUTH_UNFLAG_MSGS
        )
        unflagMsgsTask?.setUnFlagMsgsTaskListener(this)
        unflagMsgsTask?.execute()
    }

    private fun unflagMessages() {
        if (null == selectedItem) {
            return
        }
        selectedItem?.setFlagged(false)
        execUnFlagMsgsTask(arrayOf(selectedItem?.getDataId()!!))
    }

    private fun deleteMessages() {
        if (null == selectedItem) {
            return
        }
        execDeleteMsgsTask(arrayOf(selectedItem?.getDataId()!!))
    }

    private fun flagMessages() {
        if (null == selectedItem) {
            return
        }
        selectedItem?.setFlagged(true)
        execFlagMsgsTask(arrayOf(selectedItem?.getDataId()!!))
    }

    private fun forwardMessages() {
        val intellibitzActivity = removeSelf()
        if (intellibitzActivity == null) {
            return
        }
        //        this.onDestroy();
        //        intellibitzActivity.onBackPressed(intent);
        val intent = Intent()
        intent.setAction(MessageChatGroupFragment.TAG)
        intent.putExtra(MessageItem.TAG, selectedItem as Parcelable?)
        intellibitzActivity.onMessageForward(intent)
    }

    private fun forwardMessagesToNest() {
        val intellibitzActivity = removeSelf()
        if (intellibitzActivity == null) {
            return
        }
        //        this.onDestroy();
        //        intellibitzActivity.onBackPressed(intent);
        val intent = Intent()
        intent.setAction(MessageChatGroupFragment.TAG)
        intent.putExtra(MessageItem.TAG, selectedItem as Parcelable?)
        //        intellibitzActivity.onMessageForwardToNest(intent);
    }

    override fun onPostUnFlagMsgsExecute(response: JSONObject?, mids: Array<MessageItem>, item: Array<String>) {
        var status = 0
        if (response != null) {
            status = response.optInt("status")
        }
        if (null == response || 99 == status || -1 == status) {
            //            retries again..
            onPostUnFlagMsgsExecute(response, mids, item)
        } else {
            if (null == item || 0 == item.size) {
                return
            }
            val values = ContentValues()
            values.put(MessageItemColumns.KEY_IS_FLAGGED, false)
            for (id in item) {
                getAppCompatActivity().getContentResolver().update(
                    MessageChatGroupContentProvider.RAW_CONTENT_URI,
                    values,
                    MessageItemColumns.KEY_DATA_ID + " = ? ",
                    arrayOf(id)
                )
            }
            restartLoader()
            Log.e(TAG, "onPostUnFlagMsgsExecute: SUCCESS - $response")
        }
    }

    override fun onPostUnFlagMsgsExecuteFail(response: JSONObject?, mids: Array<MessageItem>, item: Array<String>) {
        Log.e(TAG, "UnFlag Msgs FAIL - $response: ${Arrays.toString(item)}")
    }

    override fun setUnFlagMsgsTaskToNull() {
        unflagMsgsTask = null
    }

    override fun onPostFlagMsgsExecute(response: JSONObject?, mids: Array<MessageItem>, item: Array<String>) {
        var status = 0
        if (response != null) {
            status = response.optInt("status")
        }
        if (null == response || 99 == status || -1 == status) {
            //            retries again..
            onPostFlagMsgsExecuteFail(response, mids, item)
        } else {
            if (null == item || 0 == item.size) {
                return
            }
            val values = ContentValues()
            values.put(MessageItemColumns.KEY_IS_FLAGGED, true)
            for (id in item) {
                getAppCompatActivity().getContentResolver().update(
                    MessageChatGroupContentProvider.RAW_CONTENT_URI,
                    values,
                    MessageItemColumns.KEY_DATA_ID + " = ? ",
                    arrayOf(id)
                )
            }
            restartLoader()
            Log.e(TAG, "onPostFlagMsgsExecute: SUCCESS - $response")
        }
    }

    override fun onPostFlagMsgsExecuteFail(response: JSONObject?, mids: Array<MessageItem>, item: Array<String>) {
        Log.e(TAG, "Flag Msgs FAIL - $response: ${Arrays.toString(item)}")
    }

    override fun setFlagMsgsTaskToNull() {
        flagMsgsTask = null
    }

    override fun onPostDeleteMsgsResponse(response: JSONObject?, item: Array<String>) {
        var status = 0
        if (response != null) {
            status = response.optInt("status")
        }
        if (null == response || 99 == status || -1 == status) {
            //            retries again..
            onPostDeleteMsgsErrorResponse(response, item)
        } else {
            Log.e(TAG, "onPostDeleteMsgsResponse: SUCCESS - $response")
            deleteMessagesInDB(item)
        }
    }

    fun deleteMessagesInDB(item: Array<String>) {
        try {
            val count = MessageChatGroupContentProvider.deleteMsgs(item, context)
            restartLoader()
            Log.e(TAG, "deleteMessagesInDB: Delete in DB: $count")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
    }

    override fun onPostDeleteMsgsErrorResponse(response: JSONObject?, item: Array<String>) {
        Log.e(TAG, "deleteMessagesInDB: Delete Msgs FAIL - $response: ${Arrays.toString(item)}")
        //        deleteMessagesInDB(item);
    }

    /*
    private void startMimeActivity(File file, String mimeType) {
        //                        Log.d(TAG, "Clicked view: "+v);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Intent target = intent;
        Uri uri = Uri.fromFile(file);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        final String mt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mt != null) mimeType = mt;
        if (null == mimeType) {
            intent.setData(uri);
            target = Intent.createChooser(intent, "Choose an app to open with:");
        } else {
            intent.setDataAndType(uri, mimeType);
        }
        startActivity(target);
    //        // TODO: 24-05-2016
    //        to set the correct mime type
    /*
                        intent.setDataAndType(
                                Uri.fromFile(new File(attachmentItem.getDownloadURL())),
                                attachmentItem.getType() + "/" + attachmentItem.getSubType());
    */
    }
*/

    fun showNewMessageDialog() {
        // Create an instance of the dialog fragment and show it
        NewEmailDialogFragment.newMessageDialog(this, 2, user, messageItem).show(
            fragmentManager,
            "NewMessageDialog"
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
        mtItem.setSubject(newEmailDialogFragment.getSubject())
        val to = newEmailDialogFragment.getTo()
        val cc = newEmailDialogFragment.getCc()
        val bcc = newEmailDialogFragment.getBcc()
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

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        if (MainApplicationSingleton.MESSAGECHATGROUP_LOADERID == id) {
            if (null == messageItem) {
                //        returns an empty dummy cursor.. for the cursor loader to play game
                return CursorLoader(
                    getAppCompatActivity(),
                    Uri.withAppendedPath(MessagesChatGroupContentProvider.CONTENT_URI, "0"),
                    null,
                    MessageItemColumns.KEY_CHAT_ID + " = ? ",
                    arrayOf("0"),
                    null
                )
            }
            //            String idParam = messageItem.getDataId();
            val chatId = messageItem?.getChatId()
            //===========================================================================
            //            IMPORTANT
            //  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
            // MT AS PREFIX for message thread IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
            //            M AS prefix for message IS REQUIRED
            //===========================================================================
            //        // TODO: 13-03-2016
            //        loads the complete message thread, with all messages and all attachments
            //        handles new
            if (!TextUtils.isEmpty(chatId)) {

                //                check for group contact joins..
                var selection = " ( " + "m." + MessageItemColumns.KEY_TEXT + " IS NULL OR " + "m." + MessageItemColumns.KEY_TEXT + " like ? ) AND " + " ( ak." + MessageItemColumns.KEY_IS_GROUP + " = 0 OR " + " ak." + MessageItemColumns.KEY_IS_GROUP + " IS NULL ) AND " + "mt." + MessageItemColumns.KEY_CHAT_ID + " = ? "
                val selArgs: Array<String>
                if (filter != null && !filter.isEmpty()) {
                    selArgs = arrayOf("%$filter%", chatId)
                } else {
                    selArgs = arrayOf("%%", chatId)
                }
                val sortOrder = "m." + MessageItemColumns.KEY_TIMESTAMP + " ASC"
                val uri = Uri.withAppendedPath(MessagesChatGroupContentProvider.JOIN_CONTENT_URI, Uri.encode(chatId))
                return CursorLoader(getAppCompatActivity(), uri, null, selection, selArgs, sortOrder)
            }
        }
        //        returns an empty dummy cursor.. for the cursor loader to play game
        return CursorLoader(
            getAppCompatActivity(),
            Uri.withAppendedPath(MessagesChatGroupContentProvider.CONTENT_URI, "0"),
            null,
            MessageItemColumns.KEY_DATA_ID + " = ? ",
            arrayOf("0"),
            null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.MESSAGECHATGROUP_LOADERID == loader.id) {
            if (null == cursor) {
                if (null == messageItem) {
                    Log.e(TAG, "onLoadFinished: message thread is NULL")
                    return
                }
                var messageItems = messageItem?.getMessages()
                if (null == messageItems) {
                    messageItems = HashSet()
                }
                messageItems.clear()
                createRecyclerAdapter()
                return
            }
            val count = cursor.count
            if (0 == count) {
                cursor.close()
                var messageItems = messageItem?.getMessages()
                if (null == messageItems) {
                    messageItems = HashSet()
                }
                messageItems.clear()
                createRecyclerAdapter()
                return
            }
            if (count > 0 && 0 == cursor.position) {
                fillItemsFromLoader(cursor)
                cursor.close()
                createRecyclerAdapter()
            }
        }
    }

    fun fillItemsFromLoader(cursor: Cursor) {
        try {
            var messageItems = messageItem?.getMessages()
            if (null == messageItems) {
                messageItems = HashSet()
            }
            messageItems.clear()
            MessageChatGroupContentProvider.fillMessageItemFromAllJoinCursor(messageItem, cursor)
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.MESSAGECHATGROUP_LOADERID == loader.id) {
            var messageItems = messageItem?.getMessages()
            if (null == messageItems) {
                messageItems = HashSet()
            }
            messageItems.clear()
            createRecyclerAdapter()
        }
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
                            MainApplicationSingleton.MESSAGECHATGROUP_LOADERID, null,
                            ChatDetailFragment.this);
                    rvMessages.notify();
*/
                ChatService.MSG_SHOW_TYPING -> if (chatMessageListener != null) {
                    try {
                        val obj = msg.obj as String
                        if (null != obj) {
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
                else -> super.handleMessage(msg)
            }
        }
    }

    inner class RecyclerViewAdapter(private val adapterItems: List<MessageItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private var bitmapFromUrlTask: BitmapFromUrlTask? = null
        private var looperThread: HandlerThread? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_messagechatgroup_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItem = adapterItems[position]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                holder.mView.alpha = 1f
            }
            var me = false
            if (user.getDataId() == holder.mItem?.getFromUid()) {
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
    //                    ((LinearLayout) rootView).setGravity(Gravity.END);
    //                    ((LinearLayout) rootView).setLayoutParams(params);
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
            holder.tvMessage.text = holder.mItem?.getText()

            var flagged: Drawable? = null
            if (holder.mItem?.isFlagged()!!) {
                flagged = getDrawable(R.drawable.ic_bookmark_black_18dp)
                if (flagged != null) {
                    flagged.setBounds(Rect(0, 0, 20, 20))
                }
            }
            if (holder.mItem?.isDelivered()!!) {
                var drawable = getDrawable(R.drawable.ic_done_all_black_18dp, getAppCompatActivity().theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                if (me) {
                    setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged)
                } else {
                    setCompoundDrawablesRelative(holder.tvMessage, drawable, null, flagged, null)
                }
            } else if (holder.mItem?.isRead()!!) {
                var drawable = getDrawable(R.drawable.ic_done_black_18dp, getAppCompatActivity().theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                if (me) {
                    setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged)
                } else {
                    setCompoundDrawablesRelative(holder.tvMessage, drawable, null, flagged, null)
                }
            } else {
                var drawable = getDrawable(R.drawable.ic_restore_black_18dp, getAppCompatActivity().theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                if (me) {
                    setCompoundDrawablesRelative(holder.tvMessage, null, null, drawable, flagged)
                } else {
                    setCompoundDrawablesRelative(holder.tvMessage, drawable, null, flagged, null)
                }
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
            val textDrawable = ColorGenerator.getTextDrawable(holder.mItem?.getFromName())
            textDrawable.setBounds(Rect(0, 0, 60, 60))
            if (me) {
                //                setCompoundDrawablesRelative(holder.tvFromName, null, null, textDrawable, null);
                setCompoundDrawablesRelative(holder.tvFromName, null, null, null, null)
            } else {
                setCompoundDrawablesRelative(holder.tvFromName, textDrawable, null, null, null)
            }

            //            holder.tvFromName.setText(holder.messageItem.getFromName());
            //                    converts to milliseconds, for correct date conversion
            val createDate = holder.mItem?.getTimestamp()!! * 1000
            val timestamp = Timestamp(createDate)
            val now = System.currentTimeMillis()
            var df: DateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            if (now - createDate < 1000 * 60 * 60 * 24) {
                df = SimpleDateFormat("hh mm a", Locale.getDefault())
            }
            val dt = df.format(timestamp.time)
            //            holder.tvFromName.setText(holder.tvFromName.getText() + "  @" + dt);
            holder.tvFromName.text = dt

            if (MessageItem.TAG.equals(holder.mItem?.getDataId())) {
                //            handles new message
                //            new message has the message thread item class name as an hack to indicate new

                if (holder.mItem?.getAttachments()?.isEmpty()!!) {
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
                    linearLayout.background = getDrawable(android.R.color.transparent, context?.theme)
                }
            }

            //            binds the attachments for each message
            val attachmentItems = holder.mItem?.getAttachments()
            for (attachmentItem in attachmentItems!!) {
                val view = LayoutInflater.from(holder.mView.context).inflate(
                    R.layout.list_item_chat_attch_msg,
                    holder.mView.rootView as ViewGroup,
                    false
                )
                val tv = view.findViewById<TextView>(R.id.tv_attach_name)
                tv.text = attachmentItem.getName()
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
                val downloadURL = attachmentItem.getDownloadURL()
                if (null == downloadURL || downloadURL.isEmpty()) {

                    Log.e(TAG, "Attachment Download URL is NULL ")
                } else if (downloadURL.startsWith("/")) {
                    progressBar.visibility = View.GONE
                    val file = File(downloadURL)
                    val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
                    var mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

                    var bm: Bitmap?
                    var drawable: Drawable? = null
                    val type = attachmentItem.getType()
                    if (type != null && type.contains("image") || mimetype != null && mimetype.contains("image")) {
                        try {
                            bm = BitmapFactory.decodeFile(downloadURL)
                            drawable = BitmapDrawable(resources, bm)
                        } catch (ignored: Throwable) {
                            Log.e(TAG, "$TAG${ignored.message}")
                        }
                    } else if (type != null && type.contains("video") || mimetype != null && mimetype.contains("video")) {
                        bm = ThumbnailUtils.createVideoThumbnail(downloadURL, MediaStore.Video.Thumbnails.MINI_KIND)
                        drawable = BitmapDrawable(resources, bm)
                    } else if (type != null && type.contains("audio") || mimetype != null && mimetype.contains("audio")) {
                        drawable = getDrawable(android.R.drawable.ic_media_play, getAppCompatActivity().theme)
                    } else if (type != null && type.contains("application") || mimetype != null && mimetype.contains("application")) {
                        drawable = getDrawable(android.R.drawable.ic_menu_slideshow, getAppCompatActivity().theme)
                    } else {
                        drawable = ColorGenerator.getTextDrawable(attachmentItem.getName())
                    }
                    if (drawable != null) {
                        drawable.setBounds(Rect(0, 0, 100, 100))
                        setCompoundDrawablesRelative(tv, drawable, null, null, null)
                    }
                    tv.setOnClickListener {
                        MainApplicationSingleton.startMimeActivity(
                            file,
                            MsgChatGrpAttachmentContentProvider.getMimeType(attachmentItem),
                            context
                        )
                    }
                } else if (downloadURL.startsWith("http")) {
                    if (holder.mItem?.getFromUid()?.equals(user.getDataId(), ignoreCase = true)!!) {
                        //                        if self message, download immediately
                        //                    cloud pic.. not always a pic.. so image download is not always applicable
                        /*
                        bitmapFromUrlTask = new BitmapFromUrlTask(
                                tv, downloadURL, getAppCompatActivity().getApplicationContext());
                        bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                        bitmapFromUrlTask.execute();
*/
                        looperThread = MainApplicationSingleton.performOnHandlerThread {
                            MsgChatGrpAttachmentContentProvider.asyncUpdateChatGroupAttachmentFilePathInDB(
                                getAppCompatActivity(),
                                user,
                                attachmentItem
                            )
                            this@RecyclerViewAdapter.quitLooperThread()
                        }
                    } else {
                        var drawable = getDrawable(R.drawable.ic_get_app_black_18dp, getAppCompatActivity().theme)
                        assert(drawable != null)
                        drawable?.setBounds(Rect(0, 0, 100, 100))
                        setCompoundDrawablesRelative(tv, drawable, null, null, null)
                        val `val` = attachmentItem.getType() + " " + attachmentItem.getSize()
                        tv.text = `val`
                        tv.setOnClickListener {
                            Log.d(TAG, "Fetching attachment in background :" + attachmentItem.getDataId())
                            progressBar.visibility = View.VISIBLE
                            MsgChatAttachmentContentProvider.asyncUpdateChatAttachmentFilePathInDB(
                                getAppCompatActivity(),
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
            return adapterItems[position].get_id()
        }

        override fun getItemCount(): Int {
            return adapterItems.size
        }

        private fun quitLooperThread() {
            if (looperThread != null) {
                looperThread?.quit()
            }
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap, textView: View, context: Context?) {
            var context = context
            if (null == context) {
                context = this@MessageChatGroupFragment.context
            }
            if (null == context) {
                return
            }
            val resources = context.resources
            if (null == resources) {
                return
            }
            //            Bitmap roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds(Rect(0, 0, 100, 100))
            setCompoundDrawablesRelative(textView as TextView, drawable, null, null, null)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView), View.OnLongClickListener {
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
                mView.setOnClickListener(this@MessageChatGroupFragment)
                mView.setOnClickListener(this@MessageChatGroupFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvMessage.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                setSelectedItem(mItem!!)
                if (null == mActionMode) {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = getAppCompatActivity().startSupportActionMode(mActionModeCallback)
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
