package intellibitz.intellidroid.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MsgChatContactsContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.MessageItemColumns
import intellibitz.intellidroid.util.MainApplicationSingleton
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Arrays

class ChatEmailService : Service() {

    companion object {
        const val RCV_DOC = "rcv_doc"
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
        const val ACK_DOC = "ack_doc"
        const val MSG_ID = "msg_id"
        const val DOC_ID = "doc_id"
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
        private const val TAG = "ChatEmailService"
    }

    val mMessenger: Messenger = Messenger(IncomingHandler())
    var socket: Socket? = null
    var mOnListenerRunnable: onListenerRunnable? = null
    val mClients: ArrayList<Messenger> = ArrayList()
    var mValue: Int = 0

    val ackEmitSendMsgListener: Ack = Ack { objects1 ->
        Log.e(
            TAG,
            "ChatEmailService Callback from 'send_msg' with results....." +
                    Arrays.toString(objects1)
        )
    }

    val ackEmitAckDocListner: Ack = Ack { objects ->
        Log.e(
            TAG,
            "ChatEmailService Callback from 'act_msg' with results....." +
                    Arrays.toString(objects)
        )
    }

    private var mNM: NotificationManager? = null
    private var reconnect = false
    private var ref: String? = null
    private var user: ContactItem? = null
    private var url: String? = null
    private val NOTIFICATION = 100

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

    override fun onDestroy() {
        if (socket != null) {
            disconnectFromSocket()
        }
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
        mNM = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
    }

    private fun connectToSocket() {
        runOnListenerRunnable()
        socket?.connect()
    }

    private fun disconnectFromSocket() {
        socket?.let { s ->
            s.off(Socket.EVENT_CONNECT)
            s.off(Socket.EVENT_ERROR)
            s.off(Socket.EVENT_DISCONNECT)

            mOnListenerRunnable = null

            s.off(RCV_DOC)
            s.off("test2")
            s.disconnect()
        }
        Log.e(TAG, "ChatEmailService Socket Disconnected onto .....$url")
    }

    private fun runOnListenerRunnable() {
        if (mOnListenerRunnable == null) {
            mOnListenerRunnable = onListenerRunnable()
        }
        mOnListenerRunnable?.run()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            createSocket(intent)
        }
        if (reconnect) {
            reconnectSocket()
        }
        return result
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

    private fun reconnectSocket() {
        if (user != null) {
            reconnect = false
            disconnectFromSocket()
            connectToSocket()
        }
    }

    private fun emitToSendMessage(msg: Message) {
        val `val` = msg.obj as? String ?: return
        val args = msg.data ?: return
        val id = args.getString("id") ?: return
        val sub = args.getString("topic")
        val docOwnerEmail = args.getString("docOwnerEmail")
        try {
            emitToSendEmailMessage(id, sub, docOwnerEmail, `val`)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun emitToIsTyping() {
        try {
            emitToIsTyping(GROUP, AKTPROTOTYPE)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun emitToSendEmailMessage(id: String, topic: String?, docOwnerEmail: String?, `val`: String) {
        val c = getMessageThreadMsgContactsCursor(id)
        var from = docOwnerEmail
        if (from == null) {
            from = MainApplicationSingleton.DUMMY_EMAIL
        }
        var to = ""
        var cc = ""
        var bcc = ""
        c?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val item = ContactItem()
                    item.typeId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TYPE_ID))
                    item.name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
                    item.type = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TYPE))
                    val email = item.typeId
                    if ("to" == item.type && from != email) {
                        if (to.isNotEmpty() && !to.contains(email)) {
                            to += ","
                        }
                        if (!to.contains(email)) {
                            to += email
                        }
                    } else if ("from" == item.type && from != email) {
                        if (to.isNotEmpty() && !to.contains(email)) {
                            to += ","
                        }
                        if (!to.contains(email)) {
                            to += email
                        }
                    } else if ("cc" == item.type) {
                        if (cc.isNotEmpty() && !cc.contains(email)) {
                            cc += ","
                        }
                        if (!cc.contains(email)) {
                            cc += email
                        }
                    } else if ("bcc" == item.type) {
                        if (bcc.isNotEmpty() && !bcc.contains(email)) {
                            bcc += ","
                        }
                        if (!bcc.contains(email)) {
                            bcc += email
                        }
                    }
                } while (cursor.moveToNext())
            }
        }
        val jsonObject = JSONObject()
        jsonObject.put("msg_type", "EMAIL")
        jsonObject.put("from", from)
        jsonObject.put("to", to)
        jsonObject.put("cc", cc)
        jsonObject.put("bcc", bcc)
        jsonObject.put("subject", topic)
        jsonObject.put(TXT, `val`)
        Log.e(TAG, "ChatEmailService Emitting to 'send_msg'.....$jsonObject")
        socket?.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener)
    }

    @Throws(JSONException::class)
    private fun emitToSendMessage(text: String, type: String, uid: String) {
        val jsonObject = JSONObject()
        jsonObject.put(TXT, text)
        jsonObject.put(TO_TYPE, type)
        jsonObject.put(TO_UID, uid)
        Log.e(TAG, "ChatEmailService Emitting to 'send_msg'.....$jsonObject")
        socket?.emit(SEND_MSG, jsonObject, ackEmitSendMsgListener)
    }

    @Throws(JSONException::class)
    private fun emitToIsTyping(type: String, uid: String) {
        val jsonObject = JSONObject()
        jsonObject.put(TO_TYPE, type)
        jsonObject.put(TO_UID, uid)
        socket?.emit(IS_TYPING, jsonObject)
    }

    private fun getMessageThreadItemCursor(id: String): Cursor? {
        val uri = Uri.withAppendedPath(MessageChatContentProvider.CONTENT_URI, Uri.encode(id))
        return contentResolver.query(
            uri,
            arrayOf(MessageItemColumns.KEY_ID),
            MessageItemColumns.KEY_DATA_ID + " = ?",
            arrayOf(id),
            null
        )
    }

    private fun getMessageThreadMsgContactsCursor(id: String): Cursor? {
        val uri = Uri.withAppendedPath(MsgChatContactsContentProvider.CONTENT_URI, Uri.encode(id))
        return contentResolver.query(uri, null, null, null, null)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun showNotification() {
        val text: CharSequence = MainApplicationSingleton.INTELLIBITZ
        val contentIntent = PendingIntent.getActivity(
            this,
            MainApplicationSingleton.PI_CHATTYMAIL_RQ_CODE,
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
            MainApplicationSingleton.PI_CHATTYMAIL_RQ_CODE,
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
                        emitToSendMessage(msg)
                    }
                }
                MSG_ON_KEY -> {
                    mValue = msg.arg1
                    if (reconnect) {
                        reconnectSocket()
                    }
                    emitToIsTyping()
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    inner class onListenerRunnable : Runnable {
        override fun run() {
            socket?.let { s ->
                s.on(Socket.EVENT_CONNECT, OnConnectListener())
                s.on(Socket.EVENT_ERROR, OnErrorListener())
                s.on(Socket.EVENT_DISCONNECT, OnDisconnectListener())
            }
            Log.e(TAG, "ChatEmailService Socket onto .....$url")
        }
    }

    inner class OnConnectListener : Emitter.Listener {
        val ackEmitJoinUidCallbackListener: Ack = Ack { objects ->
            val result = Arrays.toString(objects)
            Log.e(
                TAG,
                "ChatEmailService Callback from 'join_uid' with results.....$result"
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
                    Log.e(TAG, "ChatEmailService Joined Socket with ref: $ref")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        override fun call(vararg args1: Any?) {
            socket?.emit(MY_OTHER_EVENT, "968768")
            socket?.emit(TEST_1, "968768")
            try {
                val jsonObject = JSONObject()
                val u = user
                if (u != null) {
                    jsonObject.put(MainApplicationSingleton.UID_PARAM, u.dataId)
                    jsonObject.put(MainApplicationSingleton.TOKEN_PARAM, u.token)
                    jsonObject.put(MainApplicationSingleton.DEVICE_PARAM, u.device)
                    jsonObject.put(MainApplicationSingleton.DEVICE_REF_PARAM, u.deviceRef)
                }
                Log.e(TAG, "ChatEmailService Emitting to 'join_uid'.....$jsonObject")
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
            Log.e(
                TAG,
                "ChatEmailService Callback from 'act_msg' with results....." +
                        Arrays.toString(objects)
            )
        }

        override fun call(vararg args1: Any?) {
            val incomingMsg = Arrays.toString(args1)
            Log.e(TAG, "ChatEmailService Listening on 'rcv_message'.....$incomingMsg")
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
                    Log.e(TAG, "ChatEmailService self message - not accepting from cloud")
                } else {
                    contentResolver.insert(MessageChatContentProvider.CONTENT_URI, contentValues)
                    sendMessageToClients()
                    val msgId = jsonObject.getString("_id")
                    val toId = jsonObject.getString(TO_UID)
                    val payload = JSONObject()
                    payload.put(MSG_ID, msgId)
                    payload.put(TO_UID, toId)
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
            Log.e(TAG, "ChatEmailService Listening on 'test2'....." + Arrays.toString(args1))
        }
    }

    inner class OnDisconnectListener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            Log.e(TAG, "ChatEmailService Disconnect.....")
        }
    }

    inner class OnErrorListener : Emitter.Listener {
        override fun call(vararg args1: Any?) {
            Log.e(TAG, "ChatEmailService Error.....")
        }
    }
}
