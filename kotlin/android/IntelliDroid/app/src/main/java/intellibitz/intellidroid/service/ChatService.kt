package intellibitz.intellidroid.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import androidx.annotation.WorkerThread
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.db.MessageItemColumns
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

class ChatService : Service {

    companion object {
        const val RCV_MSG = "rcv_msg"
        const val SHOW_TYPING = "show_typing"
        const val TEST_2 = "test2"
        const val IS_TYPING = "is_typing"
        const val GROUP = "group"
        const val AKTPROTOTYPE = "aktprototype"
        const val TO_UID = "to_uid"
        const val TO_TYPE = "to_type"
        const val TXT = "txt"
        const val FROM_UID = "from_uid"
        const val FROM_NAME = "from_name"
        const val ACK_MSG = "ack_msg"
        const val MSG_ID = "msg_id"
        const val SEND_MSG = "send_msg"
        const val JOIN_UID = "join_uid"
        const val UID = "uid"
        const val TOKEN = "token"
        const val DEVICE = "device"
        const val TEST_1 = "test1"
        const val MY_OTHER_EVENT = "my other event"
        const val STATUS = "status"
        const val REF = "ref"

        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
        const val MSG_ON_KEY = 4
        const val MSG_SHOW_TYPING = 5
        const val INTENT_ACTION_NEW_CHAT_MESSAGE = "NEW_CHAT_MESSAGE"
        private const val TAG = "ChatService"

        @JvmStatic
        fun performOnBackgroundThread(runnable: Runnable): Thread {
            val t = Thread {
                runnable.run()
            }
            t.start()
            return t
        }
    }

    val mMessenger: Messenger = Messenger(IncomingHandler())
    private val lock = Any()
    val ackEmitSendMsgListener: Ack = Ack { objects1 ->
        Log.d(
            TAG,
            "ChatService Callback from 'send_msg' with results....." +
                    Arrays.toString(objects1)
        )
    }
    private var user: ContactItem? = null
    private var socket: Socket? = null
    private var mOnListenerRunnable: onListenerRunnable? = null
    private var mNM: NotificationManager? = null
    private val mClients: ArrayList<Messenger> = ArrayList()
    private var mValue = 0
    private var reconnect = false
    private var ref: String? = null
    private var url: String? = null
    private val NOTIFICATION = 100
    @Volatile
    private var mServiceLooper: Looper? = null
    @Volatile
    private var mServiceHandler: ServiceHandler? = null
    private var mName: String? = null
    private var mRedelivery = false

    constructor() : super()

    constructor(name: String) : super() {
        mName = name
    }

    private fun sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null)
    }

    private fun sendShowTypingToClients(s: Any?) {
        sendMessageToClients(MSG_SHOW_TYPING, s)
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
        createSocket()
    }

    fun createSocket() {
        try {
            createsIOSocket()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Log.e(TAG, TAG + e.message)
        }
    }

    @Throws(URISyntaxException::class)
    fun createsIOSocket() {
        if (url == null) {
            Log.e(TAG, "SOCKET url is NULL: ")
        } else {
            socket = IO.socket(url)
            if (user == null) {
                reconnect = true
            }
        }
    }

    override fun onDestroy() {
        if (socket != null) {
            disconnectFromSocket()
        }
        mServiceLooper?.quit()
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
        if (socket == null) createSocket()
        socket?.on(Socket.EVENT_CONNECT, OnConnectListener())
        socket?.on(Socket.EVENT_ERROR, OnErrorListener())
        socket?.on(Socket.EVENT_DISCONNECT, OnDisconnectListener())

        runOnListenerRunnable()
        socket?.connect()
        Log.d(TAG, "ChatService Socket connected onto .....$url")
    }

    private fun disconnectFromSocket() {
        socket?.let { s ->
            s.off(Socket.EVENT_CONNECT)
            s.off(Socket.EVENT_ERROR)
            s.off(Socket.EVENT_DISCONNECT)

            s.off(SHOW_TYPING)
            s.disconnect()
        }

        mOnListenerRunnable = null
        Log.d(TAG, "ChatService Socket Disconnected onto .....$url")
    }

    private fun runOnListenerRunnable() {
        if (mOnListenerRunnable == null) {
            mOnListenerRunnable = onListenerRunnable()
        }
        mOnListenerRunnable?.run()
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
        if (INTENT_ACTION_NEW_CHAT_MESSAGE == intent.action) {
            val u: ContactItem? = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            val messageItem: MessageItem? = intent.getParcelableExtra(MessageItem.TAG)
            if (messageItem == null) {
                Log.e(TAG, " onHandleIntent: MessageItem is NULL in intent extras")
            } else if (u != null) {
                emitsChatMessage(messageItem, u)
            }
        }
    }

    private fun addAfterJoinListenersToSocket() {
    }

    private fun emitSendMessage(msg: Message) {
        val args = msg.data ?: return
        val messageItem: MessageItem? = args.getParcelable(MessageItem.TAG)
        val u: ContactItem? = args.getParcelable(ContactItem.USER_CONTACT)
        if (messageItem != null && u != null) {
            performOnBackgroundThread {
                emitsChatMessage(messageItem, u)
            }
        }
    }

    private fun emitToSendMessage(msg: Message) {
        val `val` = msg.obj as? String ?: return
        val args = msg.data ?: return
        val id = args.getString("id") ?: return
        val sub = args.getString("topic")
        val name = args.getString("fromName")
        try {
            emitToSendChatMessage(id, sub, name, `val`)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun emitToIsTyping(msg: Message) {
        try {
            val args = msg.data ?: return
            val messageItem: MessageItem? = args.getParcelable(MessageItem.TAG)
            if (messageItem != null) {
                emitToIsTyping(messageItem)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun emitToIsTyping(messageItem: MessageItem) {
        val jsonObject = JSONObject()
        val chatId = messageItem.chatId
        if (TextUtils.isEmpty(chatId)) {
            Log.e(TAG, "emitToIsTyping: chat id is NULL")
            return
        }
        var toType = messageItem.toType
        if (TextUtils.isEmpty(toType)) {
            Log.e(TAG, "emitToIsTyping: to type is NULL.. fallback to 'USER'")
            toType = "USER"
        }
        jsonObject.put(TO_TYPE, toType)
        jsonObject.put(TO_UID, chatId)
        if (socket == null) reconnectSocket()
        if (socket == null) {
            Log.e(TAG, "Socket is NULL.. cannot emit typing")
            return
        }
        socket?.emit(IS_TYPING, jsonObject)
    }

    @Throws(JSONException::class)
    private fun emitToIsTyping(type: String, uid: String) {
        val jsonObject = JSONObject()
        jsonObject.put(TO_TYPE, type)
        jsonObject.put(TO_UID, uid)
        socket?.emit(IS_TYPING, jsonObject)
    }

    @Throws(JSONException::class)
    private fun emitToSendChatMessage(id: String, topic: String?, fromName: String?, `val`: String) {
        val name = fromName ?: "DEMO USER"
        val jsonObject = JSONObject()
        jsonObject.put("msg_type", "CHAT")
        jsonObject.put("to_uid", id)
        jsonObject.put(TXT, `val`)
        Log.e(TAG, "ChatService Emitting to 'send_msg'.....$jsonObject")
        if (socket == null) reconnectSocket()
        if (socket == null) {
            Log.e(TAG, "Socket is NULL.. cannot emit typing")
            return
        }
        socket?.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener)
    }

    private fun emitsChatMessage(messageThreadItem: MessageItem, user: ContactItem) {
        val messageItemConcurrentLinkedQueue = messageThreadItem.messageItemConcurrentLinkedQueue
        val messageItems: Array<MessageItem>
        synchronized(lock) {
            messageItems = messageItemConcurrentLinkedQueue.toTypedArray()
        }
        for (messageItem in messageItems) {
            try {
                if (messageItem.isReadyToSend) {
                    val jsonObject = JSONObject()
                    jsonObject.put("msg_type", "CHAT")
                    jsonObject.put("to_uid", messageThreadItem.chatId)
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
                                val attachmentDetails = uploadAttachments(
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
                    Log.e(TAG, "ChatService Emitting to 'send_msg'.....$jsonObject")
                    if (socket == null) reconnectSocket()
                    if (socket == null) {
                        Log.e(TAG, "Socket is NULL.. cannot emit typing")
                        return
                    }
                    socket?.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
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
        item: MessageItem,
        url: String,
        uid: String?,
        device: String?,
        deviceRef: String?,
        token: String?
    ): JSONObject? {
        try {
            val charset = "UTF-8"
            val multipart = HttpUrlConnectionParser.MultipartUtility(url, charset)
            multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device)
            multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
            multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid)
            multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token)
            val file = File(item.description)
            multipart.addFilePart(MainApplicationSingleton.ATTACH_FILE_PARAM, file, file.absolutePath)
            val response = multipart.finishAsJSON()

            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status) {
                Log.e(TAG, "Attachments Upload ERROR - $response")
            } else if (1 == status) {
                MsgChatAttachmentContentProvider.setAttachmentItemFromJson(item, response)
            }
            return response
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    protected fun uploadAttachments(
        file: File,
        url: String,
        uid: String?,
        device: String?,
        deviceRef: String?,
        token: String?
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

            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status) {
                Log.e(TAG, "Attachments Upload ERROR - $response")
            } else if (1 == status) {
                result = response.getString("url")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return result
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun showNotification() {
        val text: CharSequence = MainApplicationSingleton.INTELLIBITZ
        val contentIntent = PendingIntent.getActivity(
            this,
            MainApplicationSingleton.PI_CHAT_RQ_CODE,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_arrow)
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
            MainApplicationSingleton.PI_CHAT_RQ_CODE,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_arrow)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(msg)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .build()
        mNM?.notify(NOTIFICATION, notification)
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
                        emitSendMessage(msg)
                    }
                }
                MSG_ON_KEY -> {
                    mValue = msg.arg1
                    if (reconnect) {
                        reconnectSocket()
                    }
                    emitToIsTyping(msg)
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

    inner class onListenerRunnable : Runnable {
        override fun run() {
            socket?.on(SHOW_TYPING, OnShowTypingListener())
        }
    }

    inner class onReceiveMessageRunnable : Runnable {
        override fun run() {
            socket?.on(RCV_MSG, OnReceiveMessageListener())
        }
    }

    inner class OnConnectListener : Emitter.Listener {
        val ackEmitJoinUidCallbackListener: Ack = Ack { objects ->
            val result = Arrays.toString(objects)
            Log.d(
                TAG,
                "ChatService Callback from 'join_uid' with results.....$result"
            )
            try {
                val jsonArray = JSONArray(result)
                val jsonObject = jsonArray.getJSONObject(0)
                val status = jsonObject.getInt(STATUS)
                if (0 == status) {
                    reconnect = true
                }
                if (1 == status) {
                    ref = jsonObject.getString(REF)
                    Log.e(TAG, "Joined Socket with ref: $ref")
                    addAfterJoinListenersToSocket()
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
                Log.d(TAG, "ChatService Emitting to 'join_uid'.....$jsonObject")
                socket?.emit(JOIN_UID, jsonObject, ackEmitJoinUidCallbackListener)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    inner class OnShowTypingListener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            val json = Arrays.toString(args1)
            sendShowTypingToClients(json)
        }
    }

    inner class OnReceiveMessageListener : Emitter.Listener {
        val ackEmitAckMsgListner: Ack = Ack { objects ->
            Log.d(
                TAG,
                "ChatService Callback from 'act_msg' with results....." +
                        Arrays.toString(objects)
            )
        }

        override fun call(vararg args1: Any?) {
            val incomingMsg = Arrays.toString(args1)
            Log.d(TAG, "ChatService Listening on 'rcv_message'.....$incomingMsg")
            val mainApplication = MainApplicationSingleton.getInstance(applicationContext)
            try {
                val jsonArray = JSONArray(incomingMsg)
                val jsonObject = jsonArray.getJSONObject(0)
                val fromUid = jsonObject.getString(FROM_UID)
                val fromName = jsonObject.getString(FROM_NAME)
                val txt = jsonObject.getString(TXT)
                val contentValues = ContentValues()
                contentValues.put(MessageItemColumns.KEY_FROM_NAME, fromName)
                contentValues.put(MessageItemColumns.KEY_TEXT, txt)

                val uid = mainApplication.getStringValueSP(MainApplicationSingleton.UID_PARAM)
                val name = mainApplication.getStringValueSP(MainApplicationSingleton.NAME_PARAM)

                if (uid != null && uid == fromUid && name != null && name == fromName) {
                    Log.d(TAG, "ChatService self message - not accepting from cloud")
                } else {
                    contentResolver.insert(MessageChatContentProvider.CONTENT_URI, contentValues)
                    sendMessageToClients()
                    val msgId = jsonObject.getString("_id")
                    val toId = jsonObject.getString(TO_UID)
                    val payload = JSONObject()
                    payload.put(MSG_ID, msgId)
                    payload.put(TO_UID, toId)
                    if (socket == null) reconnectSocket()
                    if (socket == null) {
                        Log.e(TAG, "Socket is NULL.. cannot emit typing")
                        return
                    }
                    socket?.emit(ACK_MSG, payload, ackEmitAckMsgListner)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            mainApplication.incomingQueue?.add(incomingMsg)
        }
    }

    inner class OnTest2Listener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            Log.d(TAG, "ChatService Listening on 'test2'....." + Arrays.toString(args1))
        }
    }

    inner class OnDisconnectListener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            Log.d(TAG, "ChatService Disconnect.....")
        }
    }

    inner class OnErrorListener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            Log.d(TAG, "ChatService Error.....")
        }
    }
}
