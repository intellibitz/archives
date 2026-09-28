package intellibitz.intellidroid.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
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
import android.util.Log
import androidx.annotation.WorkerThread
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MessagesEmailContentProvider
import intellibitz.intellidroid.content.MsgEmailContactsContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.task.GetEmailsByTask
import intellibitz.intellidroid.task.GetFullEmailsTask
import intellibitz.intellidroid.task.GetMailboxListTask
import intellibitz.intellidroid.task.GetRecentEmailsTask
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Arrays

class EmailService : Service,
    GetRecentEmailsTask.GetRecentEmailsTaskListener,
    GetEmailsByTask.GetEmailsByTaskListener,
    GetFullEmailsTask.GetFullEmailsTaskListener,
    GetMailboxListTask.GetMailboxListTaskListener {

    companion object {
        const val SEND_EMAIL = "send_email"
        const val GROUP = "group"
        const val AKTPROTOTYPE = "aktprototype"
        const val TO_UID = "to_uid"
        const val TO_TYPE = "to_type"
        const val TXT = "txt"
        const val FROM_UID = "from_uid"
        const val FROM_NAME = "from_name"
        const val ACK_MSG = "ack_msg"
        const val ACK_DOC = "ack_doc"
        const val MSG_ID = "msg_id"
        const val DOC_ID = "doc_id"
        const val JOIN_UID = "join_uid"
        const val UID = "uid"
        const val TOKEN = "token"
        const val DEVICE = "device"
        const val STATUS = "status"
        const val REF = "ref"

        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
        const val MSG_ON_KEY = 4
        const val MSG_SHOW_TYPING = 5
        const val INTENT_ACTION_NEW_EMAIL_MESSAGE = "New_Email_Message"
        const val ACTION_UPDATE_EMAIL_MESSAGE = "UPDATE_EMAIL_MESSAGE"
        private const val TAG = "EmailService"

        @JvmStatic
        fun asyncUpdateGetFullEmails(
            emailAccount: String, emailIds: Array<String>?, user: ContactItem, context: Context
        ) {
            val intent = Intent(context, EmailService::class.java)
            intent.action = ACTION_UPDATE_EMAIL_MESSAGE
            intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
            intent.putExtra("email", emailAccount)
            intent.putExtra("emailIds", emailIds)
            context.startService(intent)
        }
    }

    val mMessenger: Messenger = Messenger(IncomingHandler())
    private val lock = Any()
    val ackEmitSendEmailMsgListener: Ack = Ack { objects1 ->
        Log.e(
            TAG,
            "EmailService Callback from 'send_email' with results....." +
                    Arrays.toString(objects1)
        )
    }
    private var user: ContactItem? = null
    private var socket: Socket? = null
    private var mNM: NotificationManager? = null
    private val mClients: ArrayList<Messenger> = ArrayList()
    private var mValue = 0
    private var reconnect = false
    private var ref: String? = null
    private var handlerThread: HandlerThread? = null
    private var url: String? = null
    private val NOTIFICATION = 100
    @Volatile
    private var mServiceLooper: Looper? = null
    @Volatile
    private var mServiceHandler: ServiceHandler? = null
    private var mName: String? = null
    private var mRedelivery = false
    private var getRecentEmailsLooperThread: HandlerThread? = null
    private var updateFullEmailsLooperThread: HandlerThread? = null

    constructor() : super()

    constructor(name: String) : super() {
        mName = name
    }

    private fun sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null)
    }

    private fun sendMessageToClients(msg: Int, `object`: Any?) {
        for (i in mClients.indices.reversed()) {
            try {
                val message = Message.obtain(null, msg, mValue, 0)
                message.obj = `object`
                mClients[i].send(message)
            } catch (e: RemoteException) {
                mClients.removeAt(i)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mMessenger.binder
    }

    fun setIntentRedelivery(enabled: Boolean) {
        mRedelivery = enabled
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            createSocket(intent)
            val msg = mServiceHandler?.obtainMessage()
            if (msg != null) {
                msg.arg1 = startId
                msg.obj = intent
                mServiceHandler?.sendMessage(msg)
            }
        }
        if (reconnect) {
            reconnectSocket()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun createSocket(intent: Intent) {
        url = intent.getStringExtra("url")
        reconnect = intent.getBooleanExtra("reconnect", false)
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        try {
            if (url == null) {
                Log.e(TAG, "SOCKET url is NULL: ")
            } else {
                socket = IO.socket(url)
                if (user == null) {
                    reconnect = true
                }
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
        }
    }

    override fun onDestroy() {
        if (socket != null) {
            disconnectFromSocket()
        }
        mServiceLooper?.quit()
        handlerThread?.quit()
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
        mNM = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val thread = HandlerThread("IntentService[$mName]")
        thread.start()

        mServiceLooper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLooper!!)
    }

    private fun connectToSocket() {
        socket?.on(Socket.EVENT_CONNECT, OnConnectListener())
        socket?.on(Socket.EVENT_ERROR, OnErrorListener())
        socket?.on(Socket.EVENT_DISCONNECT, OnDisconnectListener())

        socket?.connect()
        Log.e(TAG, "EmailService Socket connected onto .....$url")
    }

    private fun disconnectFromSocket() {
        socket?.off(Socket.EVENT_CONNECT)
        socket?.off(Socket.EVENT_ERROR)
        socket?.off(Socket.EVENT_DISCONNECT)

        socket?.disconnect()
        Log.e(TAG, "EmailService Socket Disconnected onto .....$url")
    }

    private fun reconnectSocket() {
        if (user != null) {
            reconnect = false
            disconnectFromSocket()
            connectToSocket()
        }
    }

    @WorkerThread
    private fun onHandleIntent(intent: Intent) {
        val u = user
        if (INTENT_ACTION_NEW_EMAIL_MESSAGE == intent.action) {
            val contactUser: ContactItem? = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            val messageItem: MessageItem? = intent.getParcelableExtra(MessageItem.TAG)
            if (messageItem == null) {
                Log.e(TAG, " onHandleIntent: MessageItem is NULL in intent extras")
            } else if (contactUser != null) {
                emitToSendEmailMessage(messageItem, contactUser)
            }
        }
        if (ACTION_UPDATE_EMAIL_MESSAGE == intent.action) {
            val contactUser: ContactItem? = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            val email = intent.getStringExtra("email")
            val emailIds = intent.getStringArrayExtra("emailIds")

            if (contactUser == null || TextUtils.isEmpty(email) || emailIds == null || emailIds.isEmpty()) {
                Log.e(TAG, " onHandleIntent: Extras are null - skipped get full emails")
            } else {
                asyncGetFullEmailsAndUpdate(emailIds, email!!, contactUser)
            }
        }
        if (u != null) {
            asyncGetMailboxList(u)
            asyncGetRecentEmails(u, 0)
        }
    }

    private fun asyncGetMailboxList(user: ContactItem) {
        val email = user.email
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "asyncGetRecentEmails: email not available - skipping")
            return
        }
        val getMailboxListTask = GetMailboxListTask(
            MainApplicationSingleton.API_USER, MainApplicationSingleton.API_KEY,
            email, user.dataId, user.token, user.device, user.deviceRef,
            MainApplicationSingleton.AUTH_EMAIL_GET_MAILBOX_LIST, this
        )
        getMailboxListTask.requestTimeoutMillis = 30000
        getMailboxListTask.setGetMailboxListTaskListener(this)
        getMailboxListTask.execute()
    }

    private fun asyncGetRecentEmails(user: ContactItem, skip: Int) {
        val email = user.email
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "asyncGetRecentEmails: email not available - skipping")
            return
        }
        val getRecentEmailsTask = GetRecentEmailsTask(
            email,
            skip, user.dataId, user.token, user.device, user.deviceRef, user,
            MainApplicationSingleton.AUTH_EMAIL_GET_RECENT_EMAILS, this
        )
        getRecentEmailsTask.requestTimeoutMillis = 30000
        getRecentEmailsTask.setGetRecentEmailsTaskListener(this)
        getRecentEmailsTask.execute()
    }

    private fun asyncGetFullEmailsAndUpdate(emailIds: Array<String>, email: String, user: ContactItem) {
        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "onPostGetFullEmailsResponse: email not available - skipping")
            return
        }
        val getFullEmailsTask = GetFullEmailsTask(
            email,
            emailIds, user.dataId, user.token, user.device, user.deviceRef, user,
            MainApplicationSingleton.AUTH_EMAIL_GET_FULL_EMAILS, this, 1
        )
        getFullEmailsTask.requestTimeoutMillis = 30000
        getFullEmailsTask.setGetFullEmailsTaskListener(this)
        getFullEmailsTask.execute()
    }

    private fun asyncGetEmailsBy(user: ContactItem) {
        val getEmailsByTask = GetEmailsByTask(
            "intellibitztest@gmail.com",
            "muthu@intellibitz.com", user.dataId, user.token, user.device, user.deviceRef,
            MainApplicationSingleton.AUTH_GET_EMAILS_BY, this
        )
        getEmailsByTask.setGetEmailsByTaskListener(this)
        getEmailsByTask.requestTimeoutMillis = 30000
        getEmailsByTask.execute()
    }

    override fun onPostGetRecentEmailsErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetRecentEmailsErrorResponse:$response")
    }

    override fun onPostGetRecentEmailsResponse(
        response: JSONObject?, email: String?, skip: Int, user: ContactItem?
    ) {
        if (response == null || user == null) return
        val status = response.optInt("status")
        val count = intArrayOf(0)
        if (1 == status) {
            if (MessageEmailContentProvider.isGetRecentEmailsUpdateInDBFromJSONRequired(response, this) <= 0) {
                getRecentEmailsLooperThread = MainApplicationSingleton.performOnHandlerThread {
                    try {
                        count[0] = MessageEmailContentProvider.savesGetRecentEmailsInDBFromJSON(
                            response, user, this@EmailService
                        )
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        Log.e(TAG, TAG + e.toString())
                    }
                    quitGetRecentEmailsLooperThread(getRecentEmailsLooperThread, skip, count[0], user)
                }
            }
        } else if (99 == status) {
            onPostGetEmailsByErrorResponse(response)
        } else {
            onPostGetEmailsByErrorResponse(response)
        }
    }

    private fun quitGetRecentEmailsLooperThread(
        looperThread: HandlerThread?, skip: Int, count: Int, user: ContactItem
    ) {
        if (looperThread != null) {
            looperThread.quit()
            this.getRecentEmailsLooperThread = null
        }
        if (500 == count) {
            asyncGetRecentEmails(user, skip + 500)
        }
    }

    private fun quitUpdateFullEmailsLooperThread(looperThread: HandlerThread?) {
        if (looperThread != null) {
            looperThread.quit()
            this.updateFullEmailsLooperThread = null
        }
    }

    override fun onPostGetFullEmailsErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetFullEmailsErrorResponse:$response")
    }

    override fun onPostGetFullEmailsResponse(
        response: JSONObject?,
        emailUidsArray: Array<out String>?,
        emailAccount: String?,
        user: ContactItem?,
        mode: Int
    ) {
        if (response == null || user == null) return
        try {
            val status = response.optInt("status")
            if (1 == status) {
                if (mode > 0) {
                    updateFullEmailsLooperThread = MainApplicationSingleton.performOnHandlerThread {
                        try {
                            MessageEmailContentProvider.updatesGetFullEmailsInDBFromJSON(
                                response, user, this@EmailService
                            )
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            Log.e(TAG, TAG + e.toString())
                        }
                        quitUpdateFullEmailsLooperThread(updateFullEmailsLooperThread)
                    }
                }
            } else if (99 == status) {
                onPostGetFullEmailsErrorResponse(response)
            } else {
                onPostGetFullEmailsErrorResponse(response)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, TAG + e.toString())
        }
    }

    override fun onPostGetEmailsByResponse(response: JSONObject?) {
        MainApplicationSingleton.logD(response, "$TAG:onPostGetEmailsByResponse:")
    }

    override fun onPostGetEmailsByErrorResponse(response: JSONObject?) {
        Log.d(TAG, "onPostGetEmailsByErrorResponse:$response")
    }

    private fun emitToSendMessage(msg: Message) {
        val args = msg.data ?: return
        val messageItem: MessageItem? = args.getParcelable(MessageItem.TAG)
        val u: ContactItem? = args.getParcelable(ContactItem.USER_CONTACT)
        if (messageItem != null && u != null) {
            handlerThread = MainApplicationSingleton.performOnHandlerThread {
                emitToSendEmailMessage(messageItem, u)
            }
        }
    }

    private fun emitToSendEmailMessage(emailMessage: MessageItem, user: ContactItem) {
        Log.e(TAG, "emitToSendEmailMessage: " + emailMessage.subject)
        var from = emailMessage.docOwnerEmail
        if (emailMessage.dataId != null && MessageItem.TAG != emailMessage.dataId) {
            val c = getMessageThreadMsgContactsCursor(emailMessage.dataId)
            if (c != null && c.count > 0) {
                if (from == null) {
                    from = MainApplicationSingleton.DUMMY_EMAIL
                }
                var contactItem = emailMessage.contactItem
                if (contactItem == null) {
                    contactItem = ContactItem()
                    emailMessage.contactItem = contactItem
                }
                if (c.moveToFirst()) {
                    do {
                        val emailItem = ContactItem()
                        val email = c.getString(c.getColumnIndex(ContactItemColumns.KEY_TYPE_ID))
                        if (!TextUtils.isEmpty(email)) {
                            emailItem.typeId = email
                            emailItem.dataId = email
                            emailItem.name = c.getString(c.getColumnIndex(ContactItemColumns.KEY_NAME))
                            emailItem.type = c.getString(c.getColumnIndex(ContactItemColumns.KEY_TYPE))
                            contactItem.addContact(emailItem)
                        }
                    } while (c.moveToNext())
                }
                c.close()
            }
        }

        emailMessage.from = from
        emailMessage.invoke()

        val messageItemConcurrentLinkedQueue = emailMessage.messageItemConcurrentLinkedQueue
        val messageItems: Array<MessageItem>
        synchronized(lock) {
            messageItems = messageItemConcurrentLinkedQueue.toTypedArray()
        }
        for (messageItem in messageItems) {
            try {
                if (messageItem.isReadyToSend) {
                    val jsonObject = JSONObject()
                    jsonObject.put("msg_type", "EMAIL")
                    jsonObject.put("to_uid", emailMessage.chatId)
                    jsonObject.put("client_msg_ref", messageItem.chatMsgRef)
                    jsonObject.put(TXT, messageItem.text)
                    val uid = user.dataId
                    val token = user.token
                    val device = user.device
                    val items = messageItem.attachments
                    if (items != null && items.isNotEmpty()) {
                        val attachments = JSONArray()
                        for (item in items) {
                            try {
                                val attachmentDetails = HttpUrlConnectionParser.uploadAttachments(
                                    item,
                                    MainApplicationSingleton.ATTACHMENT_UPLOAD_URL,
                                    uid, device, user.deviceRef, token
                                )
                                attachments.put(attachmentDetails)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                        jsonObject.put("attachments", attachments)
                    }
                    jsonObject.put("email", from)
                    val name = user.name
                    jsonObject.put("from", "$name <$from>")
                    if (TextUtils.isEmpty(emailMessage.to)) {
                        emailMessage.to = MainApplicationSingleton.DUMMY_EMAIL
                    }
                    jsonObject.put("to", emailMessage.to)
                    jsonObject.put("cc", emailMessage.cc)
                    jsonObject.put("bcc", emailMessage.bcc)
                    jsonObject.put("subject", emailMessage.subject)
                    Log.e(TAG, "EmailService Emitting to 'send_email'.....$jsonObject")
                    socket?.emit(SEND_EMAIL, jsonObject, ackEmitSendEmailMsgListener)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, "Exception: $e")
            }
        }
        for (messageItem in messageItems) {
            if (messageItem.isReadyToSend) {
                messageItemConcurrentLinkedQueue.remove(messageItem)
            }
        }
    }

    @Throws(IOException::class)
    protected fun uploadAttachments(
        file: File, url: String, uid: String?, device: String?, deviceRef: String?, token: String?
    ): String? {
        val charset = "UTF-8"
        var result: String? = null
        try {
            val multipart = HttpUrlConnectionParser.MultipartUtility(url, charset)
            multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device)
            multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
            multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid)
            multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token)
            multipart.addFilePart(MainApplicationSingleton.ATTACH_FILE_PARAM, file, file.absolutePath)
            val response = multipart.finishAsJSON()

            var status = -1
            status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status || -1 == status) {
                Log.e(TAG, "PROFILE UPLOAD ERROR - $response")
            } else if (1 == status) {
                result = response.getString("url")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return result
    }

    private fun getMessageThreadItemCursor(id: String): Cursor? {
        val uri = Uri.withAppendedPath(MessagesEmailContentProvider.CONTENT_URI, Uri.encode(id))
        return contentResolver.query(
            uri,
            arrayOf(MessageItemColumns.KEY_ID),
            MessageItemColumns.KEY_DATA_ID + " = ?",
            arrayOf(id), null
        )
    }

    private fun getMessageThreadMsgContactsCursor(id: String): Cursor? {
        val uri = Uri.withAppendedPath(MsgEmailContactsContentProvider.CONTENT_URI, Uri.encode(id))
        return contentResolver.query(uri, null, null, null, null)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun showNotification() {
        val text: CharSequence = MainApplicationSingleton.INTELLIBITZ
        val contentIntent = PendingIntent.getActivity(
            this,
            MainApplicationSingleton.PI_EMAIL_RQ_CODE,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setContentTitle("Chatty Emails!")
            .setContentText(text)
            .setContentIntent(contentIntent)
            .build()
        mNM?.notify(NOTIFICATION, notification)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun showNotification(msg: String) {
        val text: CharSequence = MainApplicationSingleton.INTELLIBITZ
        val contentIntent = PendingIntent.getActivity(
            this,
            MainApplicationSingleton.PI_EMAIL_RQ_CODE,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(msg)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .build()
        mNM?.notify(NOTIFICATION, notification)
    }

    override fun onPostGetMailboxListResponse(response: JSONObject?) {
        MainApplicationSingleton.logD(response, "$TAG:onPostGetMailboxListResponse")
    }

    override fun onPostGetMailboxListErrorResponse(response: JSONObject?) {
        Log.e(TAG, TAG + response)
    }

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> mClients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> mClients.remove(msg.replyTo)
                MSG_SET_VALUE -> {
                    mValue = msg.arg1
                    if (msg.obj != null) {
                        if (reconnect) {
                            reconnectSocket()
                        }
                        emitToSendMessage(msg)
                    }
                }
                MSG_ON_KEY -> {
                    mValue = msg.arg1
                    if (reconnect) {
                        reconnectSocket()
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val intent = msg.obj as? Intent
            if (intent != null) {
                onHandleIntent(intent)
            }
        }
    }

    inner class OnConnectListener : Emitter.Listener {
        val ackEmitJoinUidCallbackListener: Ack = Ack { objects ->
            val result = Arrays.toString(objects)
            Log.e(TAG, "EmailService Callback from 'join_uid' with results.....$result")
            try {
                val jsonArray = JSONArray(result)
                val jsonObject = jsonArray.getJSONObject(0)
                val status = jsonObject.getInt(STATUS)
                if (0 == status) {
                    reconnect = true
                }
                if (1 == status) {
                    ref = jsonObject.getString(REF)
                    Log.e(TAG, "EmailService Joined Socket with ref: $ref")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        override fun call(vararg args1: Any?) {
            try {
                val jsonObject = JSONObject()
                val u = user
                if (u != null) {
                    jsonObject.put(MainApplicationSingleton.UID_PARAM, u.dataId)
                    jsonObject.put(MainApplicationSingleton.TOKEN_PARAM, u.token)
                    jsonObject.put(MainApplicationSingleton.DEVICE_PARAM, u.device)
                    jsonObject.put(MainApplicationSingleton.DEVICE_REF_PARAM, u.deviceRef)
                }
                Log.e(TAG, "EmailService Emitting to 'join_uid'.....$jsonObject")
                socket?.emit(JOIN_UID, jsonObject, ackEmitJoinUidCallbackListener)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    inner class OnDisconnectListener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            Log.e(TAG, "EmailService Disconnect.....")
        }
    }

    inner class OnErrorListener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            Log.e(TAG, "EmailService Error.....")
        }
    }
}
