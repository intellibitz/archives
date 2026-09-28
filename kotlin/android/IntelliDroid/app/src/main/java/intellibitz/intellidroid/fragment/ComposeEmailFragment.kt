package intellibitz.intellidroid.fragment

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.MultiAutoCompleteTextView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.PermissionChecker
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import intellibitz.intellidroid.IntellibitzActivity
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.listener.ClutterListener
import intellibitz.intellidroid.listener.ClutterMessageHeaderListener
import intellibitz.intellidroid.service.EmailService
import intellibitz.intellidroid.task.CreateDraftTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.MediaPickerFile
import intellibitz.intellidroid.util.MediaPickerUri
import intellibitz.intellidroid.widget.NewEmailDialogFragment
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *
 */
class ComposeEmailFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    NewEmailDialogFragment.OnNewEmailDialogFragmentListener,
    CreateDraftTask.CreateDraftTaskListener {

    companion object {
        private const val TAG = "ComposeEmailFrag"

        fun newInstance(
            messageItem: MessageItem?,
            user: ContactItem?,
            clutterListener: ClutterListener?
        ): ComposeEmailFragment {
            val fragment = ComposeEmailFragment()
            fragment.setUser(user)
            fragment.setMessageItem(messageItem)
            if (clutterListener != null) fragment.setViewModeListener(clutterListener)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            args.putParcelable(MessageItem.TAG, messageItem)
            fragment.arguments = args
            return fragment
        }
    }

    // If non-null, this is the current filter the user has provided.
    private var mCurFilter: String? = null
    private var cameraPhotoFileUri: Uri? = null
    private var videoPhotoFileUri: Uri? = null
    private var audioFileUri: Uri? = null
    private var btnSend: Button? = null
    private var btnDraft: Button? = null
    private var btnSchedule: Button? = null
    private var etMessageInput: EditText? = null
    private var ivAttach: ImageButton? = null
    private var btnAudio: ImageButton? = null
    private var btnCamera: ImageButton? = null
    private var btnVideo: ImageButton? = null
    private var sub: EditText? = null
    private var to: MultiAutoCompleteTextView? = null
    private var cc: MultiAutoCompleteTextView? = null
    private var bcc: MultiAutoCompleteTextView? = null
    private var messageItem: MessageItem? = null
    private var selectedItem: MessageItem? = null
    private var selfTypingMessage: MessageItem? = null
    private var title: String? = null
    private var subTitle: String? = null
    private var filter: String? = null
    private var emailMessageHeaderListener: ClutterMessageHeaderListener? = null
    private var toolbar: Toolbar? = null
    private var looperThread: HandlerThread? = null
    private var draftLooperThread: HandlerThread? = null
    private var scheduleLooperThread: HandlerThread? = null
    private var createDraftTask: CreateDraftTask? = null

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private val mMessenger = Messenger(IncomingHandler())

    private val lock = Any()

    /**
     * Messenger for communicating with service.
     */
    private var mService: Messenger? = null

    /**
     * Flag indicating whether we have called bind on the service.
     */
    private var mIsBound = false

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
                var msg = Message.obtain(null, EmailService.MSG_REGISTER_CLIENT)
                msg.replyTo = mMessenger
                mService?.send(msg)

                // Give it some value as an example.
                msg = Message.obtain(null, EmailService.MSG_SET_VALUE, this.hashCode(), 0)
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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    constructor() : super()

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
    }

    fun getMessageItem(): MessageItem? {
        return messageItem
    }

    fun setMessageItem(messageItem: MessageItem?) {
        this.messageItem = messageItem
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        doBindService()
    }

    private fun restoreStateFromFragmentArguments(arguments: Bundle?) {
        var arguments = arguments
        if (null == arguments) {
            arguments = this.arguments
        }
        if (arguments != null) {
            user = arguments.getParcelable(ContactItem.USER_CONTACT)
            messageItem = arguments.getParcelable(MessageItem.TAG)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        prepareNewEmailToSend()
        outState.putParcelable(MessageItem.TAG, messageItem)
    }

    override fun onDestroy() {
        doUnbindService()
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    private fun quitLooperThread() {
        if (looperThread != null) looperThread?.quit()
        sendMessage(messageItem, user)
        okActivity()
        //        broadcastMessagesSend(messageItem);
    }

    private fun quitDraftLooperThread() {
        if (draftLooperThread != null) draftLooperThread?.quit()
    }

    private fun quitScheduleLooperThread() {
        if (scheduleLooperThread != null) scheduleLooperThread?.quit()
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
                val id = response.getString("draft_id")
                messageItem?.setDataId(id)
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
        return inflater.inflate(R.layout.fragment_composeemail, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
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
        etMessageInput = view.findViewById(R.id.et_message)

        if (messageItem != null) {
            sub?.setText(messageItem?.getSubject())
            to?.setText(messageItem?.getTo())
            cc?.setText(messageItem?.getCc())
            bcc?.setText(messageItem?.getBcc())
        }

        to?.setTokenizer(Rfc822Tokenizer())
        cc?.setTokenizer(Rfc822Tokenizer())
        bcc?.setTokenizer(Rfc822Tokenizer())
        SetupEmailAutoCompleteTask().execute()

        btnSend = view.findViewById(R.id.btn_send)
        btnDraft = view.findViewById(R.id.btn_draft)
        btnSchedule = view.findViewById(R.id.btn_schedule)
        ivAttach = view.findViewById(R.id.iv_attach)

        btnAudio = view.findViewById(R.id.ib_audio)
        btnCamera = view.findViewById(R.id.ib_camera)
        btnVideo = view.findViewById(R.id.ib_video)
        //        rvMessages = (RecyclerView) view.findViewById(R.id.rv_chat_messages);
        if (null == savedInstanceState) {
            etMessageInput?.requestFocus()
            etMessageInput?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    toggleNewMessageView(s)
                    //                    sendOnKey(messageItem, user);
                }

                override fun afterTextChanged(s: Editable) {}
            })

            btnDraft?.setOnClickListener { view ->
                val message = etMessageInput?.text.toString()
                if (TextUtils.isEmpty(message)) {
                    return@setOnClickListener
                }
                etMessageInput?.isEnabled = false
                messageItem?.flagStackMessageToSend()
                messageItem?.getMessageItemConcurrentLinkedQueue()?.add(messageItem?.popMessage())
                draftLooperThread = MainApplicationSingleton.performOnHandlerThread {
                    val messageItemConcurrentLinkedQueue = messageItem?.getMessageItemConcurrentLinkedQueue()
                    val messageItems: Array<MessageItem?>
                    synchronized(lock) {
                        messageItems = messageItemConcurrentLinkedQueue?.toArray(arrayOfNulls<MessageItem>(0))!!
                    }
                    for (messageItem in messageItems) {
                        if (messageItem?.isReadyToSend() == true) {
                            messageItem.setBaseType(MessageItem.DRAFT)
                            //                                    the cloud treats this doc type as draft
                            messageItem.setDocType(MessageItem.DRAFT)
                            messageItem.setDocOwner(user?.getDataId())
                            messageItem.setType(MessageItem.EMAIL)
                            messageItem.setMessageType(MessageItem.EMAIL)
                            messageItem.setToType(MessageItem.USER)
                            messageItem.setGroup(false)
                            messageItem.setEmailItem(true)
                            messageItem.setChatId(this.messageItem?.getChatId())
                            messageItem.setDataId(this.messageItem?.getChatId())
                            //                                        // TODO: 19-06-2016
                            //                                        set chat_msg_ref from app
                            //                                        chat_msg_ref format: fromuid_touid_timestamp
                            messageItem.setChatMsgRef(
                                user?.getDataId() + "_" + messageItem.getChatId() + "_" + System.currentTimeMillis()
                            )
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
                    this.quitDraftLooperThread()
                }
                etMessageInput?.isEnabled = true
                etMessageInput?.setText("")
            }
            btnSchedule?.setOnClickListener { view ->
                prepareNewEmailToSend()
                //                    // TODO: 22-05-2016
                //                    to mainitain input Q, the user can type and hit send madly like a million times
                val message = etMessageInput?.text.toString()
                val from = messageItem?.getFromEmail()
                val to = messageItem?.getTo()
                val okListener = DialogInterface.OnClickListener { dialog, which -> }
                if (TextUtils.isEmpty(from)) {
                    MainApplicationSingleton.alertDialog(
                        context,
                        "Please fill from email",
                        "Email Incomplete",
                        okListener,
                        null
                    )
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(to)) {
                    MainApplicationSingleton.alertDialog(
                        context,
                        "Please fill to email",
                        "Email Incomplete",
                        okListener,
                        null
                    )
                    this.to?.requestFocus()
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(message)) {
                    MainApplicationSingleton.alertDialog(
                        context,
                        "Please fill message",
                        "Email Incomplete",
                        okListener,
                        null
                    )
                    etMessageInput?.requestFocus()
                    return@setOnClickListener
                }

                etMessageInput?.isEnabled = false
                messageItem?.flagStackMessageToSend()
                messageItem?.getMessageItemConcurrentLinkedQueue()?.add(messageItem?.popMessage())
                scheduleLooperThread = MainApplicationSingleton.performOnHandlerThread {
                    val messageItemConcurrentLinkedQueue = messageItem?.getMessageItemConcurrentLinkedQueue()
                    val messageItems: Array<MessageItem?>
                    synchronized(lock) {
                        messageItems = messageItemConcurrentLinkedQueue?.toArray(arrayOfNulls<MessageItem>(0))!!
                    }
                    for (messageItem in messageItems) {
                        if (messageItem?.isReadyToSend() == true) {
                            messageItem.setBaseType(MessageItem.SCHEDULE)
                            //                                    the cloud treats this doc type as draft
                            messageItem.setDocType(MessageItem.SCHEDULE)
                            messageItem.setDocOwner(user?.getDataId())
                            messageItem.setType(MessageItem.EMAIL)
                            messageItem.setMessageType(MessageItem.EMAIL)
                            messageItem.setToType(MessageItem.USER)
                            messageItem.setGroup(false)
                            messageItem.setEmailItem(true)
                            messageItem.setChatId(this.messageItem?.getChatId())
                            messageItem.setDataId(this.messageItem?.getChatId())
                            //                                        // TODO: 19-06-2016
                            //                                        set chat_msg_ref from app
                            //                                        chat_msg_ref format: fromuid_touid_timestamp
                            messageItem.setChatMsgRef(
                                user?.getDataId() + "_" + messageItem.getChatId() + "_" + System.currentTimeMillis()
                            )
                            /*
                                    createDraftTask = new CreateDraftTask(messageItem,
                                            user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                                            MainApplicationSingleton.AUTH_CREATE_DRAFT);
                                    createDraftTask.setCreateDraftTaskListener(ClutterEmailFragment.this);
                                    createDraftTask.execute();
*/
                            //                                    broadcastMessagesToDraft(messageItem);
                            performScheduling(messageItem)
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
                    this.quitDraftLooperThread()
                }
                etMessageInput?.isEnabled = true
                etMessageInput?.setText("")
            }
            btnSend?.setOnClickListener { v ->
                prepareNewEmailToSend()
                //                    // TODO: 22-05-2016
                //                    to mainitain input Q, the user can type and hit send madly like a million times
                val message = etMessageInput?.text.toString()
                val from = messageItem?.getFromEmail()
                val to = messageItem?.getTo()
                val okListener = DialogInterface.OnClickListener { dialog, which -> }
                if (TextUtils.isEmpty(from)) {
                    MainApplicationSingleton.alertDialog(
                        context,
                        "Please fill from email",
                        "Email Incomplete",
                        okListener,
                        null
                    )
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(to)) {
                    MainApplicationSingleton.alertDialog(
                        context,
                        "Please fill to email",
                        "Email Incomplete",
                        okListener,
                        null
                    )
                    this.to?.requestFocus()
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(message)) {
                    MainApplicationSingleton.alertDialog(
                        context,
                        "Please fill message",
                        "Email Incomplete",
                        okListener,
                        null
                    )
                    etMessageInput?.requestFocus()
                    return@setOnClickListener
                }

                etMessageInput?.isEnabled = false
                messageItem?.flagStackMessageToSend()
                messageItem?.getMessageItemConcurrentLinkedQueue()?.add(messageItem?.popMessage())
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
                    this.quitLooperThread()
                }
                //                    sendMessage(messageItem, user);
                //                empties the input text
                //                etMessageInput.setText("");
            }

            ivAttach?.setOnClickListener { v -> openFilePicker() }
            btnAudio?.setOnClickListener { v -> openAudioPicker() }
            btnCamera?.setOnClickListener { v -> openCameraPicker() }
            btnVideo?.setOnClickListener { v -> openVideoPicker() }
        }
    }

    private fun performScheduling(messageItem: MessageItem?) {
        /*
        ScheduleDateTimeFragment scheduleDateTimeFragment = ScheduleDateTimeFragment.newInstance(messageItem, user);
        scheduleDateTimeFragment.addSchDateTimeFragmentListener(this);
        scheduleDateTimeFragment.show(getFragmentManager(), "Select Date & Time");
*/
    }

    private fun execScheduleCreateTask(scheduledTime: String?, messageItem: MessageItem?, user: ContactItem?) {
        val txt = messageItem?.getText()

        val schMsgJson = JSONObject()
        try {
            schMsgJson.put("txt", txt)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val schMsg = schMsgJson.toString()
        /*
        ScheduleCreateTask scheduleCreateTask = new ScheduleCreateTask(scheduledTime, schMsg, user.getCompanyId(),
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                user, MainApplicationSingleton.AUTH_SCHEDULE_CREATE, getContext());
        scheduleCreateTask.setRequestTimeoutMillis(30000);
        scheduleCreateTask.setScheduleCreateTaskListener(this);
        scheduleCreateTask.execute();
*/
    }

    fun onBackPressed(): Boolean {
        return false
    }

    private fun setupAppBar() {
        val view = view
        if (view != null) {
            toolbar = view.findViewById(R.id.toolbar)

            //            toolbar.setTitle(R.string.menu_title_email);
            //            toolbar.setSubtitle(R.string.app_title);
            val textView = view.findViewById<TextView>(R.id.tv_subtitle)
            textView.text = user?.getEmail()

            val tvClose = view.findViewById<TextView>(R.id.tv_close)
            tvClose.setOnClickListener { v -> cancelActivity() }
            appCompatActivity?.setSupportActionBar(toolbar)
            // Show the Up button in the action bar.
            val actionBar = appCompatActivity?.supportActionBar
            if (actionBar != null) {
                //                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    protected fun cancelActivity() {
        val activity = activity
        val intent = activity?.intent
        intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent?.putExtra(MessageItem.TAG, messageItem as Parcelable)
        activity?.setResult(Activity.RESULT_CANCELED, intent)
        activity?.finish()
        //        getAppCompatActivity().onBackPressed();
    }

    protected fun okActivity() {
        val activity = activity
        val intent = activity?.intent
        intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent?.putExtra(MessageItem.TAG, messageItem as Parcelable)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
        //        getAppCompatActivity().onBackPressed();
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
            if (last != null && MessageItem.TAG == last.getDataId() && !last.getAttachments().isEmpty()) {
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
                if (last != null && MessageItem.TAG == last.getDataId()) {
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

    private fun showNewMessageWithLatestHeader(item: MessageItem?) {
        messageItem?.setSubject(item?.getSubject())
        messageItem?.setTo(item?.getTo())
        messageItem?.setCc(item?.getCc())
        messageItem?.setBcc(item?.getBcc())
        //        lets the listeners know for the new headers
        emailMessageHeaderListener?.onEmailMessageHeaderChanged(messageItem)
        //        show the latest new message info
        var text = ""
        val messageItem = this.messageItem?.peekMessageInStack()
        if (messageItem != null) text = messageItem.getText()
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

    private fun startGetCameraContentActivityOnPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker.checkSelfPermission(
                activity!!, Manifest.permission.CAMERA
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
                activity!!, Manifest.permission.CAMERA
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
                activity!!, Manifest.permission.RECORD_AUDIO
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
                activity!!, Manifest.permission.CAMERA
            ) != PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                activity!!, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = "JPEG_" + timeStamp + "_"
        return MediaPickerFile.createImageFileInESPublicDir(file, ".jpg")
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = "3GP_" + timeStamp + "_"
        return MediaPickerFile.createImageFileInESPublicDir(file, ".3gp")
    }

    @Throws(IOException::class)
    private fun createAudioFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = "3GP_" + timeStamp + "_"
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
        if (intent.resolveActivity(appCompatActivity?.packageManager!!) != null) {
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
        if (intent.resolveActivity(appCompatActivity?.packageManager!!) != null) {
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
                    restoreStateFromFragmentArguments(arguments)
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
                if (null == uri) uri = intent.data
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION == requestCode && resultCode == Activity.RESULT_OK) {
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
                    onAttachmentPicked(messageItem?.getVideoFileUri())
                }
            } else {
                //                Bundle extras = data.getExtras();
                //                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //            mImageView.setImageBitmap(imageBitmap);
                var uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
                if (null == uri) uri = intent.data
                onAttachmentPicked(uri)
            }
        } else if (MainApplicationSingleton.REQUEST_AUDIO_AND_STORAGE_PERMISSION == requestCode && resultCode == Activity.RESULT_OK) {
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
                    onAttachmentPicked(messageItem?.getAudioFileUri())
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
                attachmentItem.setDownloadURL(file.absolutePath)
                attachmentItem.setName(file.name)
                if (messageThreadItem != null) {
                    var messageItem = messageThreadItem.peekMessageInStack()
                    if (null == messageItem || !MessageItem.TAG.equals(messageItem.getDataId())) {
                        //                        toggleNewMessageView("");
                        //                        messageItem = new MessageItem(MessageItem.TAG);
                        addNewMessageToMessageThread("", messageThreadItem)
                    }
                    messageItem = messageThreadItem.popMessage()
                    messageItem?.addAttachment(attachmentItem)
                    //                    adds message again, to resync stack after a pop
                    messageThreadItem.addMessage(messageItem)
                }
                //                createRecyclerAdapter();
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
                last.setFromName(messageItem.getFrom())
                last.setChatId(messageItem.getChatId())
                last.setToUid(messageItem.getToUid())
                last.setToChatUid(messageItem.getToChatUid())

                last.setSubject(messageItem.getSubject())
                last.setTo(messageItem.getTo())
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

    private fun setSelectedItem(mItem: MessageItem?) {
        selectedItem = mItem
    }

    private fun forwardMessages() {
        val intellibitzActivity = removeSelf()
        if (intellibitzActivity == null) return
        //        this.onDestroy();
        //        intellibitzActivity.onBackPressed(intent);
        val intent = Intent()
        intent.action = TAG
        intent.putExtra(MessageItem.TAG, selectedItem as Parcelable)
        intellibitzActivity.onMessageForward(intent)
    }

    private fun broadcastMessagesToNest() {
        val intent = Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_NEST)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, selectedItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity!!).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        appCompatActivity?.finish()
    }

    private fun broadcastMessagesToDraft(messageItem: MessageItem?) {
        val intent = Intent(MainApplicationSingleton.BROADCAST_MESSAGETO_DRAFT)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity!!).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        appCompatActivity?.finish()
    }

    private fun broadcastMessagesSend(messageItem: MessageItem?) {
        val intent = Intent(MainApplicationSingleton.BROADCAST_MESSAGES_SEND)
        //        intent.setAction(PeopleDetailFragment.TAG);
        intent.putExtra(MessageItem.TAG, messageItem as Parcelable)
        LocalBroadcastManager.getInstance(appCompatActivity!!).sendBroadcast(intent)
        //                        finishes.. so doesn't show up in back stack
        //        getAppCompatActivity().finish();
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
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }

    override fun onClose(): Boolean {
        return false
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

    fun getBcc(): Array<Rfc822Token> {
        return Rfc822Tokenizer.tokenize(bcc?.text.toString())
    }

    private fun prepareNewEmailToSend() {
        val email = user?.getEmail()
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "prepareNewEmailToSend: User email is empty - No email account signed up")
            return
        }
        //        user must be signed into atleast one email account
        //        MessageItem messageItem = new MessageItem();
        //        // TODO: 30-06-2016
        var contactThreadItem = messageItem?.getContactItem()
        if (null == contactThreadItem) {
            contactThreadItem = ContactItem()
            messageItem?.setContactItem(contactThreadItem)
        }
        //        new message.. sets id to a constant.. global new message id
        messageItem?.setDataId(MessageItem.TAG)
        messageItem?.setBaseType(MessageItem.THREAD)
        messageItem?.setDocType(MessageItem.MSG)
        messageItem?.setType(MessageItem.EMAIL)
        messageItem?.setDataRev("1")
        val name = user?.getName()
        messageItem?.setFrom(name)
        messageItem?.setSubject(getSubject())
        messageItem?.setDocOwner(user?.getDocOwner())
        messageItem?.setDocSender(name)
        messageItem?.setDocOwnerEmail(email)
        messageItem?.setDocSenderEmail(email)
        messageItem?.setFromEmail(email)
        messageItem?.setTimestamp(System.currentTimeMillis())
        val to = getTo()
        val cc = getCc()
        val bcc = getBcc()
        //        adds from
        val contactItem = ContactItem()
        contactItem.setDataId(email)
        contactItem.setTypeId(email)
        contactItem.setIntellibitzId(email)
        contactItem.setName(name)
        contactItem.setType("from")
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

    //    @Override
    fun onPostScheduleCreateResponse(response: JSONObject?, scheduleName: String?, user: ContactItem?) {
        Log.d(TAG, "onPostScheduleCreateResponse: $response")
        if (null == response) onPostScheduleCreateErrorResponse(null)
        val status = response?.optInt("status", -1)
        if (1 == status) {
            val scheduleId = response?.optString("schedule_id")
            Log.d(TAG, "onPostScheduleCreateResponse: success $scheduleId")
            okActivity()
        } else {
            onPostScheduleCreateErrorResponse(response)
        }
    }

    //    @Override
    fun onPostScheduleCreateErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostScheduleCreateResponse: $response")
        cancelActivity()
    }

    //    @Override
    fun onSchDateTimeDialogPositiveClick(dialog: DialogFragment, messageItem: MessageItem?) {
        /*
        ScheduleDateTimeFragment scheduleDateTimeFragment = ((ScheduleDateTimeFragment) dialog);
        if (scheduleDateTimeFragment != null) {
            String stime = scheduleDateTimeFragment.getScheduledTime();
            execScheduleCreateTask(stime, messageItem, user);
        }
*/
        if (dialog != null) dialog.dismiss()
    }

    //    @Override
    fun onSchDateTimeDialogNegativeClick(dialog: DialogFragment) {}

    /**
     * Handler of incoming messages from service.
     */
    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EmailService.MSG_SET_VALUE -> {}
                EmailService.MSG_SHOW_TYPING -> {}
                else -> super.handleMessage(msg)
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
                    val val = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                    )
                    val photo = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                    )
                    val vals = object : HashMap<String, String>() {
                        override fun toString(): String {
                            //                            return super.toString();
                            return get("text1")!!
                        }
                    }
                    vals["text1"] = val
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
