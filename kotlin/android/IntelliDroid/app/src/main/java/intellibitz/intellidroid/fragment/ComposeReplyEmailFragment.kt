package intellibitz.intellidroid.fragment

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentUris
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
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.os.RemoteException
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.util.Rfc822Token
import android.text.util.Rfc822Tokenizer
import android.util.Log
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
import android.widget.MultiAutoCompleteTextView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
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
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MessagesEmailContentProvider
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ClutterListener
import intellibitz.intellidroid.listener.ClutterMessageHeaderListener
import intellibitz.intellidroid.listener.ClutterMessageListener
import intellibitz.intellidroid.service.EmailService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.CreateDraftTask
import intellibitz.intellidroid.task.DeleteMsgsTask
import intellibitz.intellidroid.task.FlagMsgsTask
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
class ComposeReplyEmailFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
    LoaderManager.LoaderCallbacks<Cursor>,
    DeleteMsgsTask.DeleteMsgsTaskListener,
    FlagMsgsTask.FlagMsgsTaskListener,
    UnFlagMsgsTask.UnFlagMsgsTaskListener,
    CreateDraftTask.CreateDraftTaskListener {

    companion object {
        private const val TAG = "ClutterEmailFrag"

        fun newInstance(
            messageItem: MessageItem?,
            user: ContactItem?,
            clutterListener: ClutterListener?
        ): ComposeReplyEmailFragment {
            val fragment = ComposeReplyEmailFragment()
            fragment.setUser(user)
            fragment.setMessageItem(messageItem)
            if (clutterListener is ClutterMessageHeaderListener) {
                fragment.setEmailMessageHeaderListener(clutterListener)
            }
            if (clutterListener is ClutterMessageListener) {
                fragment.setEmailMessageListener(clutterListener)
            }
            if (clutterListener != null) {
                fragment.setViewModeListener(clutterListener)
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
    private var cameraPhotoFileUri: Uri? = null
    private var videoPhotoFileUri: Uri? = null
    private var audioFileUri: Uri? = null
    private var messageThreadContentObserver: ContentObserver? = null
    private var attachmentContentObserver: ContentObserver? = null
    private var btnSend: Button? = null
    private var btnDraft: Button? = null
    private var etMessageInput: EditText? = null
    private var ivAttach: ImageButton? = null
    private var btnAudio: ImageButton? = null
    private var btnCamera: ImageButton? = null
    private var btnCall: ImageButton? = null
    private var btnSms: ImageButton? = null
    private var rvMessages: RecyclerView? = null
    private var rvMessagesAdapter: RecyclerView.Adapter<*>? = null
    //    private MarkReadTask markReadTask;
    private var deleteMsgsTask: DeleteMsgsTask? = null
    private var flagMsgsTask: FlagMsgsTask? = null
    private var unflagMsgsTask: UnFlagMsgsTask? = null
    /**
     * Messenger for communicating with service.
     */
    private var mService: Messenger? = null
    /**
     * Flag indicating whether we have called bind on the service.
     */
    private var mIsBound = false
    private var messageItem: MessageItem? = null
    private var selectedItem: MessageItem? = null
    private var selfTypingMessage: MessageItem? = null
    private var title: String? = null
    private var subTitle: String? = null
    private var filter: String? = null
    private var btnVideo: ImageButton? = null
    private var looperThread: HandlerThread? = null
    private var draftLooperThread: HandlerThread? = null
    private var emailMessageListener: ClutterMessageListener? = null
    private var createDraftTask: CreateDraftTask? = null
    private var emailMessageHeaderListener: ClutterMessageHeaderListener? = null
    private var toolbar: Toolbar? = null
    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection: ServiceConnection = object : ServiceConnection {
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
                val msg = Message.obtain(null, EmailService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)

                // Give it some value as an example.
                val msg2 = Message.obtain(null, EmailService.MSG_SET_VALUE, this.hashCode(), 0)
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
    private var mActionMode: ActionMode? = null
    //    private Toolbar detailToolbar;
    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {

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
    private var sub: EditText? = null
    private var to: MultiAutoCompleteTextView? = null
    private var cc: MultiAutoCompleteTextView? = null
    private var bcc: MultiAutoCompleteTextView? = null

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private val mMessenger = Messenger(IncomingHandler())
    private val lock = Any()

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    constructor() : super()

    constructor(messageItem: MessageItem?, user: ContactItem?, clutterListener: ClutterListener?) : this() {
        setUser(user)
        setMessageItem(messageItem)
        if (clutterListener is ClutterMessageHeaderListener) {
            setEmailMessageHeaderListener(clutterListener)
        }
        if (clutterListener is ClutterMessageListener) {
            setEmailMessageListener(clutterListener)
        }
        if (clutterListener != null) setViewModeListener(clutterListener)
        val args = Bundle()
        args.putParcelable(ContactItem.USER_CONTACT, user)
        args.putParcelable(MessageItem.TAG, messageItem)
        arguments = args
    }

    fun setEmailMessageListener(emailMessageListener: ClutterMessageListener?) {
        this.emailMessageListener = emailMessageListener
    }

    fun setEmailMessageHeaderListener(emailMessageHeaderListener: ClutterMessageHeaderListener?) {
        this.emailMessageHeaderListener = emailMessageHeaderListener
    }

    private fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        appCompatActivity?.bindService(
            Intent(appCompatActivity, EmailService::class.java),
            mConnection,
            Context.BIND_AUTO_CREATE
        )
        mIsBound = true
        //        mCallbackText.setText("Binding.");
    }

    private fun doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    val msg = Message.obtain(null, EmailService.MSG_UNREGISTER_CLIENT)
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

    private fun sendOnKey() {
        if (null == mService) {
            Log.e(TAG, "Service is NULL - cannot send on key message")
        } else {
            try {
                val msg = Message.obtain(null, EmailService.MSG_ON_KEY, this.hashCode(), 0)
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

    private fun sendOnKey(messageItem: MessageItem?, user: ContactItem?) {
        if (null == mService) {
            Log.e(TAG, "Service is NULL - cannot send on key message")
        } else {
            try {
                val msg = Message.obtain(null, EmailService.MSG_ON_KEY, this.hashCode(), 0)
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

    private fun sendMessage(messageItem: MessageItem?, user: ContactItem?) {
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
        val msg = Message.obtain(null, EmailService.MSG_SET_VALUE, this.hashCode(), 0)
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

    fun refreshEmailMessageLoader(arguments: Bundle?) {
        restoreStateFromFragmentArguments(arguments)
        createRecyclerAdapter()
    }

    fun getMessageItem(): MessageItem? {
        return messageItem
    }

    fun setMessageItem(messageItem: MessageItem?) {
        this.messageItem = messageItem
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        doBindService()
    }

    private fun restoreStateFromFragmentArguments(arguments: Bundle?) {
        var args = arguments
        if (null == args) {
            args = arguments
        }
        if (args != null) {
            user = args.getParcelable(ContactItem.USER_CONTACT)
            messageItem = args.getParcelable(MessageItem.TAG)
        }
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
        if (emailMessageListener != null) {
            emailMessageListener?.onEmailMessageTypingStopped(messageItem?.name)
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
                if (null == etMessageInput && view != null) etMessageInput =
                    view?.findViewById(R.id.tv_chat_message)
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
            MessageEmailContentProvider.CONTENT_URI,
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
            MsgEmailAttachmentContentProvider.CONTENT_URI,
            true,
            attachmentContentObserver
        )
    }

    private fun quitLooperThread() {
        if (looperThread != null) looperThread?.quit()
        sendMessage(messageItem, user)
        broadcastMessagesSend(messageItem)
    }

    private fun quitDraftLooperThread() {
        if (draftLooperThread != null) draftLooperThread?.quit()
    }

    override fun onPostCreateDraftFromCloudExecute(response: JSONObject?, messageItem: MessageItem?) {
        etMessageInput?.isEnabled = true
        etMessageInput?.setText("")
        Log.e(TAG, "onPostFoldersGetFromCloudExecute: $response")
        var status = 0
        if (response != null) status = response.optInt("status")
        if (null == response || -1 == status || 99 == status) {
            //            retries again..
            onPostCreateDraftFromCloudExecuteFail(response, messageItem)
        } else {
            try {
                val id = response?.getString("draft_id")
                messageItem?.dataId = id
                Log.d(TAG, "Draft saved Success: $id")
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    override fun onPostCreateDraftFromCloudExecuteFail(response: JSONObject?, messageItem: MessageItem?) {
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
        return inflater.inflate(R.layout.fragment_composereplyemail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            messageItem = arguments?.getParcelable(MessageItem.TAG)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            messageItem = savedInstanceState.getParcelable(MessageItem.TAG)
        }
        if (null == messageItem) {
            messageItem = MessageItem()
        }
        setupAppBar()

        //        sets up the email drop down for to, cc, bcc
        sub = view.findViewById(R.id.new_email_sub)
        to = view.findViewById(R.id.new_email_to)
        cc = view.findViewById(R.id.new_email_cc)
        bcc = view.findViewById(R.id.new_email_bcc)

        if (messageItem != null) {
            sub?.setText(messageItem?.subject)
            to?.setText(messageItem?.to)
            cc?.setText(messageItem?.cc)
            bcc?.setText(messageItem?.bcc)
        }

        to?.setTokenizer(Rfc822Tokenizer())
        cc?.setTokenizer(Rfc822Tokenizer())
        bcc?.setTokenizer(Rfc822Tokenizer())
        SetupEmailAutoCompleteTask().execute()

        etMessageInput = view.findViewById(R.id.et_message)
        btnSend = view.findViewById(R.id.btn_send)
        btnDraft = view.findViewById(R.id.btn_draft)
        ivAttach = view.findViewById(R.id.iv_attach)
        btnAudio = view.findViewById(R.id.ib_audio)
        btnCamera = view.findViewById(R.id.ib_camera)
        btnVideo = view.findViewById(R.id.ib_video)
        btnCall = view.findViewById(R.id.ib_call)
        btnSms = view.findViewById(R.id.ib_sms)
        rvMessages = view.findViewById(R.id.rv_chat_messages)
        if (null == savedInstanceState) {
            etMessageInput?.requestFocus()
            etMessageInput?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    toggleNewMessageView(s)
                    sendOnKey(messageItem, user)
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
                draftLooperThread = MainApplicationSingleton.performOnHandlerThread {
                    val messageItemConcurrentLinkedQueue = messageItem?.messageItemConcurrentLinkedQueue
                    val messageItems: Array<MessageItem?>
                    synchronized(lock) {
                        messageItems = messageItemConcurrentLinkedQueue?.toArray(arrayOfNulls(0))!!
                    }
                    for (messageItem in messageItems) {
                        if (messageItem?.isReadyToSend == true) {
                            messageItem.baseType = MessageItem.DRAFT
                            //                                    the cloud treats this doc type as draft
                            messageItem.docType = MessageItem.DRAFT
                            messageItem.docOwner = user?.dataId
                            messageItem.type = MessageItem.EMAIL
                            messageItem.messageType = MessageItem.EMAIL
                            messageItem.toType = MessageItem.USER
                            messageItem.isGroup = false
                            messageItem.isEmailItem = true
                            messageItem.chatId = this@ComposeReplyEmailFragment.messageItem?.chatId
                            messageItem.dataId = this@ComposeReplyEmailFragment.messageItem?.chatId
                            //                                        // TODO: 19-06-2016
                            //                                        set chat_msg_ref from app
                            //                                        chat_msg_ref format: fromuid_touid_timestamp
                            messageItem.chatMsgRef =
                                user?.dataId + "_" + messageItem.chatId + "_" + System.currentTimeMillis()
                            /*
                                    createDraftTask = new CreateDraftTask(messageItem,
                                            user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                                            MainApplicationSingleton.AUTH_CREATE_DRAFT);
                                    createDraftTask.setCreateDraftTaskListener(ClutterEmailFragment.this);
                                    createDraftTask.execute();
*/
                            broadcastMessagesToDraft(messageItem)
                            messageItemConcurrentLinkedQueue?.remove(messageItem)

                            /*
                                        Uri uri = MessageContentProvider.savesMessageItem(
                                                messageItem, user, getContext());
                                        Log.d(TAG, "Message Draft saved: " + uri);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Message Draft save FAIL: " + messageItem);
                                    }
*/
                        }
                    }
                    this@ComposeReplyEmailFragment.quitDraftLooperThread()
                }
                etMessageInput?.isEnabled = true
                etMessageInput?.setText("")
            }
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
                    //                            // TODO: 28-06-2016
                    //                            revisit.. brand new message, and how to track them effectively by id
                    //                            new messages sent, should be shown in sent
                    /*
                            ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
                                    messageItem.getMessageItemConcurrentLinkedQueue();
                            MessageItem[] messageItems;
                            synchronized (lock) {
                                messageItems = messageItemConcurrentLinkedQueue.toArray(new MessageItem[0]);
                            }
                            for (MessageItem messageItem : messageItems) {
                                if (messageItem.isReadyToSend()) {
                                    try {
                                        messageItem.setType("EMAIL");
                                        messageItem.setChatId(messageItem.getChatId());
                                        messageItem.setDataId(messageItem.getChatId());
                                        messageItem.setToType(messageItem.getToType());
//                                        // TODO: 19-06-2016
//                                        set chat_msg_ref from app
//                                        chat_msg_ref format: fromuid_touid_timestamp
                                        messageItem.setChatMsgRef(user.getDataId() +
                                                "_" + messageItem.getChatId() + "_" + System.currentTimeMillis());
                                        Uri uri = MessageContentProvider.savesMessageItem(
                                                messageItem, user, getContext());
                                        Log.d(TAG, "Message saved: " + uri);
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Message save FAIL: " + messageItem);
                                    }
                                }
                            }
*/
                    this@ComposeReplyEmailFragment.quitLooperThread()
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
        }
        /*
        if (messageItem != null) {
            //            restartLoader();
            toggleNewMessageView("");
        }
*/
        setChatHeaders(messageItem)
        restartLoader()
    }

    fun onBackPressed(): Boolean {
        return false
    }

    private fun setupAppBar() {
        toolbar = view?.findViewById(R.id.toolbar)
        toolbar?.setTitle(R.string.menu_title_email)
        toolbar?.setSubtitle(R.string.app_title)
        appCompatActivity?.setSupportActionBar(toolbar)
        // Show the Up button in the action bar.
        val actionBar = appCompatActivity?.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupDetailToolbar(): Toolbar? {
        //        detailToolbar = getAppCompatActivity().getDetailToolbar();
        toolbar?.setOnClickListener { showNewMessageDialog() }

        return toolbar
    }

    private fun setChatHeaders(messageItem: MessageItem?) {
        val toolbar = setupDetailToolbar()
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_dialog_close_light)
            toolbar.setNavigationOnClickListener { performNavigationClose() }
            setupDetailTitle(messageItem, toolbar)
        }
    }

    private fun performNavigationClose() {
        appCompatActivity?.onBackPressed()
    }

    private fun setupDetailTitle(messageItem: MessageItem?, toolbar: Toolbar) {
        if (null == messageItem) {
            Log.e(TAG, "MessageThread NULL")
            return
        }
        title = messageItem?.subject
        if (null == title || 0 == title?.length) {
            val deviceContactItem = messageItem?.sharedDeviceContactItem
            if (deviceContactItem != null) {
                title = deviceContactItem.name
                if (null == title || 0 == title?.length) {
                    title = deviceContactItem.firstName
                }
                if (null == title || 0 == title?.length) {
                    title = deviceContactItem.lastName
                }
            }
            if (null == title || 0 == title?.length) {
                title = messageItem?.chatId
            }
            if (null == title || 0 == title?.length) {
                title = messageItem?.docOwner
            }
        }
        var from = messageItem?.docSender
        if (null == from || 0 == from?.length) {
            from = messageItem?.from
        }
        subTitle = "last seen on"
        /*
            String recipients = MessageItem.getSingleAddress(messageItem.getCc(),
                    messageItem.getTo(), from, user.getEmail(), user.getName());
*/
        val contactItem = messageItem?.contactItem
        if (contactItem != null) {
            val names = contactItem.contactsAsArray
            if (null == names || 0 == names.size) {
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
        toolbar.title = title
        toolbar.subtitle = subTitle
    }

    @Nullable
    private fun setMessageHeaders(messageItem: MessageItem?): Toolbar? {
        val toolbar = setupDetailToolbar()
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_dialog_close_light)
            toolbar.setNavigationOnClickListener { performNavigationClose() }
            var from = messageItem?.docSender
            if (null == from) {
                from = messageItem?.from
            }
            val recipients = MessageItem.getSingleAddress(
                messageItem?.cc,
                messageItem?.to,
                from,
                user?.email,
                user?.name
            )
            toolbar.title = messageItem?.subject
            toolbar.subtitle = recipients
        }
        return toolbar
    }

    private fun restartLoader(uri: Uri) {
        try {
            val id = ContentUris.parseId(uri)
            //            empties the list..
            //            // TODO: 06-04-2016
            //            to update only the changed item
            restartLoader(id)
        } catch (e: NumberFormatException) {
            //            ignore
        }
    }

    private fun restartLoader(id: Long) {
        val cursor = appCompatActivity?.contentResolver?.query(
            Uri.withAppendedPath(MsgEmailAttachmentContentProvider.CONTENT_URI, id.toString()),
            arrayOf(MessageItemColumns.KEY_DOWNLOAD_URL),
            MessageItemColumns.KEY_ID,
            arrayOf(id.toString()), null
        )
        if (null == cursor) return
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
                restartLoader()
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
            if (last != null && MessageItem.TAG == last.dataId) {
                selfTypingMessage = last
            }
        }
        try {
            appCompatActivity?.supportLoaderManager?.restartLoader(
                MainApplicationSingleton.CLUTTEREMAIL_LOADERID, null, this
            )
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, "$TAG${ignored.message}")
        }
    }

    private fun createRecyclerAdapter() {
        if (null == rvMessages && view != null) rvMessages = view?.findViewById(R.id.rv_email_messages)
        if (null == rvMessages) {
            Log.e(TAG, "createRecyclerAdapter: Recycler view NULL")
            return
        }
        val messages2 = messageItem?.messages
        if (selfTypingMessage != null) {
            try {
                messageItem?.addMessage(selfTypingMessage)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }
        val adapterList = ArrayList(messages2)
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
            if (last != null &&
                MessageItem.TAG == last.dataId &&
                !last.attachments.isEmpty()
            ) {
                //                remove only, when the latest message.. is the new message created here
                //                retains the valid db saved latest message
                messageItem?.messages?.remove(last)
            }
            //            checks for shared items coming in from device
            if (messageItem?.sharedText != null ||
                messageItem?.sharedUri != null ||
                !messageItem?.sharedUris?.isEmpty()!!
            ) {
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
                if (last != null &&
                    MessageItem.TAG == last.dataId
                ) {
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
        emailMessageHeaderListener?.onEmailMessageHeaderChanged(messageItem)
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
        if (Build.VERSION.SDK_INT >= 23 &&
            PermissionChecker.checkSelfPermission(
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
        if (Build.VERSION.SDK_INT >= 23 &&
            PermissionChecker.checkSelfPermission(
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
        if (Build.VERSION.SDK_INT >= 23 &&
            PermissionChecker.checkSelfPermission(
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
        if (Build.VERSION.SDK_INT >= 23 &&
            PermissionChecker.checkSelfPermission(
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
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        startActivityForResult(
            intent,
            MainApplicationSingleton.ACTION_GET_CONTENT
        )
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val file = "JPEG_$timeStamp" + "_"
        return MediaPickerFile.createImageFileInESPublicDir(file, ".jpg")
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val file = "3GP_$timeStamp" + "_"
        return MediaPickerFile.createImageFileInESPublicDir(file, ".3gp")
    }

    @Throws(IOException::class)
    private fun createAudioFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
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
        if (MainApplicationSingleton.ACTION_GET_CONTENT == requestCode
            && resultCode == Activity.RESULT_OK
        ) {
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
        } else if (MainApplicationSingleton.REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION == requestCode
            && resultCode == Activity.RESULT_OK
        ) {
            if (null == intent) {
                if (null == messageItem) {
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
                if (null == uri) uri = intent.data
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION == requestCode
            && resultCode == Activity.RESULT_OK
        ) {
            if (null == intent) {
                if (null == messageItem) {
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
                if (null == uri) uri = intent.data
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION == requestCode
            && resultCode == Activity.RESULT_OK
        ) {
            if (null == intent) {
                if (null == messageItem) {
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
                if (null == uri) uri = intent.data
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
                    if (null == messageItem ||
                        !MessageItem.TAG.equals(messageItem.dataId)
                    ) {
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
            if (null == last ||
                !MessageItem.TAG.equals(last.dataId)
            ) {
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
                last.fromName = messageItem.from
                last.chatId = messageItem.chatId
                last.toUid = messageItem.toUid
                last.toChatUid = messageItem.toChatUid

                last.subject = messageItem.subject
                last.to = messageItem.to
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

    private fun setSelectedItem(mItem: MessageItem?) {
        selectedItem = mItem
    }

    private fun execDeleteMsgsTask(msgs: Array<String?>) {
        val deleteMsgsTask = DeleteMsgsTask(
            msgs, user?.dataId, user?.token,
            user?.device, user?.deviceRef, MainApplicationSingleton.AUTH_DELETED_MSGS, context
        )
        deleteMsgsTask.requestTimeoutMillis = 30000
        deleteMsgsTask.setDeleteMsgsTaskListener(this)
        deleteMsgsTask.execute()
        /*
        deleteMsgsTask = new DeleteMsgsTask(msgs, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_DELETED_MSGS);
        deleteMsgsTask.setDeleteMsgsTaskListener(this);
        deleteMsgsTask.execute();
*/
    }

    private fun execFlagMsgsTask(msgs: Array<String?>) {
        flagMsgsTask = FlagMsgsTask(
            msgs, user?.dataId, user?.token,
            user?.device, user?.deviceRef, MainApplicationSingleton.AUTH_FLAG_MSGS
        )
        flagMsgsTask?.setFlagMsgsTaskListener(this)
        flagMsgsTask?.execute()
    }

    private fun execUnFlagMsgsTask(msgs: Array<String?>) {
        unflagMsgsTask = UnFlagMsgsTask(
            msgs, user?.dataId, user?.token,
            user?.device, user?.deviceRef, MainApplicationSingleton.AUTH_UNFLAG_MSGS
        )
        unflagMsgsTask?.setUnFlagMsgsTaskListener(this)
        unflagMsgsTask?.execute()
    }

    private fun deleteMessages() {
        execDeleteMsgsTask(arrayOf(selectedItem?.dataId))
    }

    private fun flagMessages() {
        execFlagMsgsTask(arrayOf(selectedItem?.dataId))
    }

    private fun unflagMessages() {
        execUnFlagMsgsTask(arrayOf(selectedItem?.dataId))
    }

    private fun forwardMessages() {
        val intellibitzActivity = removeSelf()
        if (intellibitzActivity == null) return
        //        this.onDestroy();
        //        intellibitzActivity.onBackPressed(intent);
        val intent = Intent()
        intent.action = ComposeReplyEmailFragment.TAG
        intent.putExtra(MessageItem.TAG, selectedItem as Parcelable)
        intellibitzActivity.onMessageForward(intent)
    }

    private fun broadcastMessagesToNest() {
        val intent =
            Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_NEST)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, selectedItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity!!).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        appCompatActivity?.finish()
    }

    private fun broadcastMessagesToDraft(messageItem: MessageItem?) {
        val intent =
            Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_DRAFT)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity!!).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        appCompatActivity?.finish()
    }

    private fun broadcastMessagesSend(messageItem: MessageItem?) {
        val intent =
            Intent(MainApplicationSingleton.BROADCAST_MESSAGES_SEND)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity!!).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        //        getAppCompatActivity().finish();
    }

    override fun onPostUnFlagMsgsExecute(response: JSONObject?, mids: Array<MessageItem?>?, item: Array<String?>) {
        try {
            val status = response?.getInt(MainApplicationSingleton.STATUS_PARAM)
            //                ERROR
            if (1 == status) {
                Log.e(TAG, "UnFlag Msgs SUCCESS - $response")
            } else if (-1 == status) {
                onPostUnFlagMsgsExecute(response, mids, item)
            } else if (99 == status) {
                onPostUnFlagMsgsExecute(response, mids, item)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPostUnFlagMsgsExecuteFail(response: JSONObject?, mids: Array<MessageItem?>?, item: Array<String?>) {
        Log.e(TAG, "UnFlag Msgs FAIL - $response: ${Arrays.toString(item)}")
    }

    override fun setUnFlagMsgsTaskToNull() {
        unflagMsgsTask = null
    }

    override fun onPostFlagMsgsExecute(response: JSONObject?, mids: Array<MessageItem?>?, item: Array<String?>) {
        try {
            val status = response?.getInt(MainApplicationSingleton.STATUS_PARAM)
            //                ERROR
            if (1 == status) {
                Log.e(TAG, "Flag Msgs SUCCESS - $response")
            } else if (-1 == status) {
                onPostFlagMsgsExecuteFail(response, mids, item)
            } else if (99 == status) {
                onPostFlagMsgsExecuteFail(response, mids, item)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPostFlagMsgsExecuteFail(response: JSONObject?, mids: Array<MessageItem?>?, item: Array<String?>) {
        Log.e(TAG, "Flag Msgs FAIL - $response: ${Arrays.toString(item)}")
    }

    override fun setFlagMsgsTaskToNull() {
        flagMsgsTask = null
    }

    override fun onPostDeleteMsgsResponse(response: JSONObject?, item: Array<String?>) {
        try {
            val status = response?.getInt(MainApplicationSingleton.STATUS_PARAM)
            //                ERROR
            if (1 == status) {
                Log.e(TAG, "Delete Msgs SUCCESS - $response")
                deleteMessagesInDB(item)
            } else if (-1 == status) {
                onPostDeleteMsgsErrorResponse(response, item)
            } else if (99 == status) {
                onPostDeleteMsgsErrorResponse(response, item)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun deleteMessagesInDB(item: Array<String?>) {
        try {
            val count = MessageEmailContentProvider.deleteMsgs(item, context)
            Log.e(TAG, "Delete in DB: $count")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onPostDeleteMsgsErrorResponse(response: JSONObject?, item: Array<String?>) {
        Log.e(TAG, "Delete Msgs FAIL - $response: ${Arrays.toString(item)}")
        //        deleteMessagesInDB(item);
    }

    private fun startMimeActivity(file: File, mimeType: String?) {
        //                        Log.d(TAG, "Clicked view: "+v);
        val intent = Intent(Intent.ACTION_VIEW)
        var target = intent
        val uri = Uri.fromFile(file)
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        val mt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        var mimeType = mimeType
        if (mt != null) mimeType = mt
        if (null == mimeType) {
            intent.data = uri
            target = Intent.createChooser(intent, "Choose an app to open with:")
        } else {
            intent.setDataAndType(uri, mimeType)
        }
        startActivity(target)
        //        // TODO: 24-05-2016
        //        to set the correct mime type
        /*
                        intent.setDataAndType(
                                Uri.fromFile(new File(attachmentItem.getDownloadURL())),
                                attachmentItem.getType() + "/" + attachmentItem.getSubType());
*/
    }

    fun showNewMessageDialog() {
        // Create an instance of the dialog fragment and show it
        NewEmailDialogFragment.newMessageDialog(this, 1, user, messageItem).show(
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

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        var cursorLoader: CursorLoader? = null
        if (MainApplicationSingleton.CLUTTEREMAIL_LOADERID == id) {
            //===========================================================================
            //            IMPORTANT
            //  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
            // MT AS PREFIX for message thread IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
            //            M AS prefix for message IS REQUIRED
            //===========================================================================
            //        // TODO: 13-03-2016
            //        loads the complete message thread, with all messages and all attachments
            //        handles new
            if (messageItem != null &&
                !MessageItem.TAG.equals(messageItem?.dataId) &&
                !TextUtils.isEmpty(messageItem?.dataId)
            ) {

                //                check for group contact joins..
                var selection = " ( " +
                        "m." + MessageItemColumns.KEY_TEXT +
                        " IS NULL OR " +
                        "m." + MessageItemColumns.KEY_TEXT +
                        " like ? ) AND mt." +
                        MessageItemColumns.KEY_DATA_ID + " = ? "
                var selArgs: Array<String?>
                if (filter != null && !filter?.isEmpty()!!) {
                    selArgs = arrayOf("%$filter%", messageItem?.dataId)
                } else {
                    selArgs = arrayOf("%%", messageItem?.dataId)
                }
                val sortOrder = "mt." +
                        MessageItemColumns.KEY_TIMESTAMP + " ASC"
                val uri = Uri.withAppendedPath(
                    //                        MessageEmailContentProvider.JOIN_CONTENT_URI,
                    MessagesEmailContentProvider.JOIN_CONTENT_URI,
                    Uri.encode(messageItem?.dataId)
                )
                cursorLoader = CursorLoader(appCompatActivity, uri, null, selection, selArgs, sortOrder)
            }
        } else {
            //        returns an empty dummy cursor.. for the cursor loader to play game
            cursorLoader = CursorLoader(
                appCompatActivity,
                Uri.withAppendedPath(MessagesEmailContentProvider.CONTENT_URI, "0"),
                null,
                MessageItemColumns.KEY_DATA_ID + " = ? ",
                arrayOf("0"), null
            )
        }
        return cursorLoader!!
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.CLUTTEREMAIL_LOADERID == loader.id) {
            if (null == cursor) {
                if (null == messageItem) {
                    Log.e(TAG, "onLoadFinished: message thread is NULL")
                    return
                }
                messageItem?.messages?.clear()
                createRecyclerAdapter()
                return
            }
            val count = cursor.count
            if (0 == count) {
                cursor.close()
                messageItem?.messages?.clear()
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
            messageItem?.messages?.clear()
            /*
            MessageEmailContentProvider.fillMessagesFromAllJoinCursor(
                    messageItem, cursor);
*/
            MessageEmailContentProvider.fillMessageItemFromAllJoinCursor(
                messageItem, cursor
            )
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.CLUTTEREMAIL_LOADERID == loader.id) {
            messageItem?.messages?.clear()
            createRecyclerAdapter()
        }
    }

    private fun addEmailsToAutoComplete(contacts: List<Map<String, String>>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val context = context
        //        the activity might disappear faster on cancel, before the loader arrives
        if (context != null) {
            val adapter = object : SimpleAdapter(
                context, contacts,
                android.R.layout.simple_dropdown_item_1line, arrayOf("text1", "text2"),
                intArrayOf(android.R.id.text1, android.R.id.text1)
            ) {
            }
            adapter.setViewBinder { view, data, textRepresentation ->
                if (null == data || "" == data) return@setViewBinder true
                val textView = view as TextView
                textView.setCompoundDrawables(null, null, null, null)
                val photo = data as String
                if (photo.startsWith("content")) {
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            context.contentResolver, Uri.parse(photo)
                        )
                        val drawable = BitmapDrawable(resources, bitmap)
                        drawable.setBounds(Rect(0, 0, 60, 60))
                        textView.setCompoundDrawables(null, null, drawable, null)
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }
                    return@setViewBinder true
                }
                false
            }

            to?.setAdapter(adapter)
            cc?.setAdapter(adapter)
            bcc?.setAdapter(adapter)
        }
    }

    fun getSubject(): String {
        return sub?.text.toString()
    }

    fun getTo(): Array<Rfc822Token> {
        return Rfc822Tokenizer.tokenize(to?.text.toString())
    }

    fun getCc(): Array<Rfc822Token> {
        return Rfc822Tokenizer.tokenize(cc?.text.toString())
    }

    fun getBcc(): Array<Rfc822Token> {
        return Rfc822Tokenizer.tokenize(bcc?.text.toString())
    }

    /*
    public String getTo (){
        return String.valueOf(to.getText());
    }

    public String getCc (){
        return String.valueOf(cc.getText());
    }

    public String getBcc (){
        return String.valueOf(bcc.getText());
    }
*/

    private fun performNewEmail() {
        val email = user?.email
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "performNewEmail: User email is empty - No email account signed up")
            return
        }
        //        user must be signed into atleast one email account
        //        MessageItem messageItem = new MessageItem();
        //        // TODO: 30-06-2016
        var contactThreadItem = messageItem?.contactItem
        if (null == contactThreadItem) {
            contactThreadItem = ContactItem()
            messageItem?.contactItem = contactThreadItem
        }
        //        new message.. sets id to a constant.. global new message id
        messageItem?.dataId = MessageItem.TAG
        messageItem?.baseType = MessageItem.THREAD
        messageItem?.docType = MessageItem.MSG
        messageItem?.type = MessageItem.EMAIL
        messageItem?.dataRev = "1"
        val name = user?.name
        messageItem?.from = name
        messageItem?.subject = getSubject()
        messageItem?.docOwner = user?.docOwner
        messageItem?.docSender = name
        messageItem?.docOwnerEmail = email
        messageItem?.docSenderEmail = email
        messageItem?.timestamp = System.currentTimeMillis()
        val to = getTo()
        val cc = getCc()
        val bcc = getBcc()
        //        adds from
        val contactItem = ContactItem()
        contactItem.dataId = email
        contactItem.typeId = email
        contactItem.intellibitzId = email
        contactItem.name = name
        contactItem.type = "from"
        contactThreadItem?.addContact(contactItem)
        //        compose msg thread contact aka email group contact from the selected
        messageItem?.compose(to, cc, bcc)
        //        MessageItem.setMessageThreadEmailAddress(messageItem, to, cc, bcc);
        /*
        Intent intent = new Intent();
        intent.setAction(MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK);
        showMessageThreadMessage(messageItem, user);
        intent.putExtra(ContactItem.TAG, (Parcelable) user);
        intent.putExtra(MessageItem.TAG, (Parcelable) messageItem);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).sendBroadcast(intent);
*/
        /*
        String to = newEmailDialogFragment.getTo();
        String cc = newEmailDialogFragment.getCc();
        String bcc = newEmailDialogFragment.getBcc();
*/
        //        messageItem.setDocOwner(user.getDocOwner());
        //        messageItem.setDocSender(user.getName());
        //        messageItem.setDocOwnerEmail(user.getEmail());
        //        messageItem.setDocSenderEmail(user.getEmail());
        //        messageItem.setTimestamp(System.currentTimeMillis());

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
    }

    /**
     * Handler of incoming messages from service.
     */
    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EmailService.MSG_SET_VALUE -> restartLoader()
                /*
                    getAppCompatActivity().getSupportLoaderManager().restartLoader(
                            MainApplicationSingleton.CLUTTEREMAIL_LOADERID, null,
                            ClutterEmailFragment.this);
                    rvMessages.notify();
*/
                EmailService.MSG_SHOW_TYPING -> if (emailMessageListener != null) {
                    try {
                        val obj = msg.obj as String
                        if (null != obj) {
                            val jsonArray = JSONArray(obj)
                            val jsonObject = jsonArray.getJSONObject(0)
                            try {
                                val name = jsonObject.getString("from_name")
                                emailMessageListener?.onEmailMessageTyping("$name is typing..")
                                //                                setToolBarShowTyping(name + " is typing..");
                                val delay = Handler()
                                delay.postDelayed({
                                    //                                        setToolBarSubTitle(toolbarSubTitle);
                                    emailMessageListener?.onEmailMessageTypingStopped("")
                                }, 1000)
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
                .inflate(R.layout.list_item_email_msg, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItem = adapterItems[position]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                holder.mView.alpha = 1f
            }
            holder.tvFromName.visibility = View.VISIBLE

            //            handles new message
            if (MessageItem.TAG.equals(holder.mItem?.dataId)) {
                holder.tvSub.visibility = View.VISIBLE
                holder.tvTo.visibility = View.VISIBLE
                holder.tvCc.visibility = View.VISIBLE
                holder.tvBcc.visibility = View.VISIBLE

                holder.tvSub.text = holder.mItem?.subject
                holder.tvTo.text = holder.mItem?.to
                holder.tvCc.text = holder.mItem?.cc
                holder.tvBcc.text = holder.mItem?.bcc

                holder.tvSub.setOnClickListener { showNewMessageDialog() }
                holder.tvTo.setOnClickListener { showNewMessageDialog() }
                holder.tvCc.setOnClickListener { showNewMessageDialog() }
                holder.tvBcc.setOnClickListener { showNewMessageDialog() }
            }
            //            binds the message
            val textDrawable = ColorGenerator.getTextDrawable(holder.mItem?.fromName)
            textDrawable.setBounds(Rect(0, 0, 60, 60))
            setCompoundDrawablesRelative(holder.tvFromName, textDrawable, null, null, null)
            holder.tvFromName.text = holder.mItem?.fromName
            //                    converts to milliseconds, for correct date conversion
            val createDate = holder.mItem?.timestamp!! * 1000
            val timestamp = Timestamp(createDate)
            val now = System.currentTimeMillis()
            var df: DateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            if (now - createDate < 1000 * 60 * 60 * 24) {
                df = SimpleDateFormat("hh mm a", Locale.getDefault())
            }
            val dt = df.format(timestamp.time)
            holder.tvFromName.text = holder.tvFromName.text.toString() + "  @" + dt

            if (holder.mItem?.isDelivered == true) {
                var drawable = getDrawable(R.drawable.ic_done_all_black_18dp, appCompatActivity?.theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, null)
            } else if (holder.mItem?.isRead == true) {
                var drawable = getDrawable(R.drawable.ic_done_black_18dp, appCompatActivity?.theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, null)
            } else {
                var drawable = getDrawable(R.drawable.ic_restore_black_18dp, appCompatActivity?.theme)
                assert(drawable != null)
                drawable?.setBounds(Rect(0, 0, 20, 20))
                setCompoundDrawablesRelative(holder.tvMessage, drawable, null, null, null)
            }
            holder.tvMessage.text = holder.mItem?.text

            val rootView = holder.mView.rootView as ViewGroup
            if (user?.name == holder.mItem?.fromName) {
                //                    ||
                //                    MessageItem.TAG.equals(holder.userEmailItem.getDataId()))
                //            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    holder.tvFromName.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    holder.tvMessage.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                }
            }
            val linearLayout = rootView.findViewById<LinearLayout>(R.id.list_item_email_attch_msg)
            //            clears old views
            linearLayout.removeAllViews()

            if (MessageItem.TAG.equals(holder.mItem?.dataId)) {
                //            handles new message
                //            new message has the message thread item class name as an hack to indicate new

                if (holder.mItem?.attachments?.isEmpty()!!) {
                    /*
                    holder.mView.setBackground(getDrawable(android.R.color.transparent
                            , getContext().getTheme()));
*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        holder.mView.alpha = 0.5f
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
                    linearLayout.setBackground(getDrawable(R.color.red, context?.theme))
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    linearLayout.setBackground(getDrawable(android.R.color.transparent, context?.theme))
                }
            }

            //            binds the attachments for each message
            val attachmentItems = holder.mItem?.attachments
            for (attachmentItem in attachmentItems!!) {
                val view = LayoutInflater.from(holder.mView.context).inflate(
                    R.layout.list_item_email_attch_msg,
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
                if (null == downloadURL || downloadURL.isEmpty()) {

                    Log.e(TAG, "Attachment Download URL is NULL ")
                } else if (downloadURL.startsWith("/")) {
                    progressBar.visibility = View.GONE
                    val file = File(downloadURL)
                    val extension = MimeTypeMap.getFileExtensionFromUrl(
                        Uri.fromFile(file).toString()
                    )
                    val mimetype =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

                    var bm: Bitmap?
                    var drawable: Drawable?
                    val type = attachmentItem.type
                    if (type != null && type.contains("image") ||
                        mimetype != null && mimetype.contains("image")
                    ) {
                        bm = BitmapFactory.decodeFile(downloadURL)
                        drawable = BitmapDrawable(resources, bm)
                    } else if (type != null && type.contains("video") ||
                        mimetype != null && mimetype.contains("video")
                    ) {
                        bm = ThumbnailUtils.createVideoThumbnail(
                            downloadURL, MediaStore.Video.Thumbnails.MINI_KIND
                        )
                        drawable = BitmapDrawable(resources, bm)
                    } else if (type != null && type.contains("audio") ||
                        mimetype != null && mimetype.contains("audio")
                    ) {
                        drawable = getDrawable(android.R.drawable.ic_media_play, appCompatActivity?.theme)
                    } else if (type != null && type.contains("application") ||
                        mimetype != null && mimetype.contains("application")
                    ) {
                        drawable = getDrawable(android.R.drawable.ic_menu_slideshow, appCompatActivity?.theme)
                    } else {
                        drawable = ColorGenerator.getTextDrawable(attachmentItem.name)
                    }
                    assert(drawable != null)
                    drawable?.setBounds(Rect(0, 0, 100, 100))
                    setCompoundDrawablesRelative(tv, drawable, null, null, null)
                    tv.setOnClickListener {
                        startMimeActivity(
                            file,
                            MsgEmailAttachmentContentProvider.getMimeType(attachmentItem)
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
                        looperThread =
                            MainApplicationSingleton.performOnHandlerThread {
                                MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                                    appCompatActivity, user, attachmentItem
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
                            MsgEmailAttachmentContentProvider.asyncUpdateEmailAttachmentFilePathInDB(
                                appCompatActivity,
                                user, attachmentItem
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

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap?, textView: View?, context: Context?) {
            var context = context
            if (null == context) {
                context = this@ComposeReplyEmailFragment.context
            }
            if (null == context) return
            val resources = context.resources
            if (null == resources) return
            //            Bitmap roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds(Rect(0, 0, 100, 100))
            setCompoundDrawablesRelative(textView as TextView, drawable, null, null, null)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap?) {
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
            //            public final ProgressBar progress;

            init {
                mView.setOnLongClickListener(this)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvMessage.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                setSelectedItem(mItem)
                if (null == mActionMode) {
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

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    inner class SetupEmailAutoCompleteTask : AsyncTask<Void?, Void?, List<Map<String, String>>>() {

        override fun doInBackground(vararg voids: Void?): List<Map<String, String>> {
            // TODO: register the new account here.
            //            investigate response and take action
            val contacts = ArrayList<Map<String, String>>()

            // Get all emails from the user's contacts and copy them to a list.
            val context = context
            if (null == context) return contacts
            val cr = context.contentResolver
            val cursor = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                null, null, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val `val` = cursor.getString(
                        cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Email.DATA
                        )
                    )
                    val photo = cursor.getString(
                        cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                        )
                    )
                    val vals = object : HashMap<String, String>() {
                        override fun toString(): String {
                            //                            return super.toString();
                            return get("text1")!!
                        }
                    }
                    vals["text1"] = `val`
                    var photo = photo
                    if (null == photo) photo = ""
                    vals["text2"] = photo
                    contacts.add(vals)
                }
                cursor.close()
            }

            return contacts
        }

        override fun onPostExecute(emailAddressCollection: List<Map<String, String>>) {
            addEmailsToAutoComplete(emailAddressCollection)
        }
    }

}
