

package intellibitz.intellidroid.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.support.v4.os.ResultReceiver
import android.text.TextUtils
import android.util.Log
import intellibitz.intellidroid.data.BaseItem
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.MediaPickerFile
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.content.MessageChatContentProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.Set
import androidx.annotation.WorkerThread
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class RcvDocService : Service() {
    companion object {
        const val CONTACT = "CONTACT"
        const val GROUP = "GROUP"
        const val BROADCAST = "BROADCAST"
        const val QUEUE_NOTIFY = "QUEUE-NOTIFY"
        const val GROUP_INFO = "GROUP-INFO"
        const val MSG = "MSG"
        const val MSG_INFO = "MSG-INFO"
        const val RCV_DOC = "rcv_doc"
        const val FORCE_LOGOUT = "force_logout"
        const val ON_ATTACHMENT_URL = "attachment_url"
        const val RCV_MSG = "rcv_msg"
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
        const val JOINED = "joined"
        const val UID = "uid"
        const val TOKEN = "token"
        const val DEVICE = "device"
        const val STATUS = "status"
        const val REF = "ref"
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
        const val MSG_SHOW_TYPING = 5
        private const val TAG = "RcvDocService"
    }
    val mMessenger: Messenger = Messenger(IncomingHandler())
    private val lock: Object = Object()
    var socket: Socket? = null
    var mOnListenerRunnable: onListenerRunnable? = null
    var mClients: ArrayList<Messenger> = ArrayList()
    var mValue: Int = 0
    var ackGetAttachmentEmit: Ack = object : Ack() {    override fun call(vararg args: Object) {
        var result: String = Arrays.toString(args)
        Log.e(TAG, ("get_attachment Callback with results....." + result))
        try {
            var jsonArray: JSONArray = JSONArray(result)
            var jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var status: Int = jsonObject.getInt(STATUS)
            if ((0 == status)) {

            }
            else {
                if ((99 == status)) {

                }
                else {
                    if ((1 == status)) {

                    }
                }
            }
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
}
    var ackDocEmit: Ack = object : Ack() {    override fun call(vararg objects: Object) {
        Log.e(TAG, ("ackDocEmit:Callback from 'ack_doc' with results....." + Arrays.toString(objects)))
    }
}
    private var mNM: NotificationManager? = null
    private var ref: String? = null
    private var user: ContactItem? = null
    private var mServiceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    private var mName: String? = null
    private var url: String? = null
    private var reconnect: Boolean = false
    private var NOTIFICATION: Int = 100
    private fun sendMessageToClients(msg: Int, object: Object) {
        var i: Int = (mClients.size() - 1)
        while ((i >= 0)) {
            try {
                var message: Message = Message.obtain(null, msg, mValue, 0)
                message.obj = object
                mClients.get(i)
            }
            catch (ignored: Throwable) {
                mClients.remove(i)
                Log.e(TAG, ignored.getMessage())
            }
            i--
        }
    }
    override fun onBind(intent: Intent): IBinder {
        return mMessenger.binder
    }
    override private fun onHandleIntent(intent: Intent) {
        if ((MainApplicationSingleton.INTENT_ACTION_FETCH_EMAIL_ATTACHMENT_CLOUD == intent.getAction())) {
            var user: ContactItem = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            var attachmentItem: MessageItem = intent.getParcelableExtra(MessageItem.ATTACHMENT_MESSAGE)
            var resultReceiver: ResultReceiver = intent.getParcelableExtra("ResultReceiver")
            emitGetAttachment(user, attachmentItem, resultReceiver)
            return
        }
        if ((MainApplicationSingleton.INTENT_ACTION_FETCH_CHAT_ATTACHMENT_CLOUD == intent.getAction())) {
            var attachmentItem: MessageItem = intent.getParcelableExtra(MessageItem.ATTACHMENT_MESSAGE)
            asyncUpdateChatAttachmentFileInDB(attachmentItem, getApplicationContext())
            return
        }
        if ((MainApplicationSingleton.INTENT_ACTION_FETCH_CHATGROUP_ATTACHMENT_CLOUD == intent.getAction())) {
            var attachmentItem: MessageItem = intent.getParcelableExtra(MessageItem.ATTACHMENT_MESSAGE)
            asyncUpdateChatGroupAttachmentFileInDB(attachmentItem, getApplicationContext())
        }
        serviceHandler.obtainMessage()
    }
    override fun onDestroy() {
        if ((socket != null)) {
            disconnectFromSocket(null)
        }
        mServiceLooper.quit()
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
    override fun onCreate() {
        super.onCreate()
        mNM = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        var thread: HandlerThread = HandlerThread((("IntentService[" + mName) + "]"), Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        mServiceLooper = thread.looper
        serviceHandler = ServiceHandler(mServiceLooper)
        Log.d(TAG, "onCreate: ")
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if ((intent != null)) {
            var msg: Message = Message()
            msg.arg1 = startId
            msg.arg2 = flags
            msg.obj = intent
            createSocket(intent)
            if (reconnect) {
                reconnectSocket(msg)
            }
            else {
                serviceHandler.sendMessage(msg)
            }
            Log.d(TAG, "onStartCommand: ")
        }
        return super.onStartCommand(intent, flags, startId)
    }
    fun createSocket(intent: Intent) {
        var urls: String = intent.getStringExtra("url")
        if (TextUtils.isEmpty(urls)) {
            Log.e(TAG, ((("createSocket: will fallback to url: " + url) + " for action: ") + intent.getAction()))
        }
        else {
            url = urls
            Log.e(TAG, ("createSocket: url incoming: " + url))
        }
        reconnect = intent.getBooleanExtra("reconnect", false)
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        try {
            if ((null == url)) {
                Log.e(TAG, ((("createSocket: will fallback to url: " + url) + " for action: ") + intent.getAction()))
            }
            else {
                socket = IO.socket(url)
            }
            if (((socket != null) && !socket.connected())) {
                reconnect = true
            }
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    private fun reconnectSocket(msg: Message) {
        if ((user != null)) {
            reconnect = false
            disconnectFromSocket(msg)
            connectToSocket(msg)
        }
    }
    private fun connectToSocket(msg: Message) {
        try {
            socket.on(Socket.EVENT_CONNECT, OnConnectListener(msg))
            socket.on(Socket.EVENT_ERROR, OnErrorListener())
            socket.on(Socket.EVENT_CONNECT_ERROR, OnErrorListener())
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, OnErrorListener())
            socket.on(Socket.EVENT_RECONNECT_ERROR, OnErrorListener())
            socket.on(Socket.EVENT_MESSAGE, OnMessageListener())
            socket.on(Socket.EVENT_DISCONNECT, OnDisconnectListener())
            socket.on(JOINED, OnJoinedListener())
            socket.on(FORCE_LOGOUT, OnForceLogoutListener())
            runOnListenerRunnable()
            socket.connect()
            Log.e(TAG, ("connectToSocket: Socket Connected onto ....." + url))
        }
        catch (ignored: Throwable) {
            Log.e(TAG, ignored.getMessage())
        }
    }
    private fun disconnectFromSocket(msg: Message) {
        socket.off(Socket.EVENT_CONNECT)
        socket.off(Socket.EVENT_ERROR)
        socket.off(Socket.EVENT_CONNECT_ERROR)
        socket.off(Socket.EVENT_CONNECT_TIMEOUT)
        socket.off(Socket.EVENT_RECONNECT_ERROR)
        socket.off(Socket.EVENT_MESSAGE)
        socket.off(Socket.EVENT_DISCONNECT)
        socket.off(JOINED)
        socket.off(FORCE_LOGOUT)
        socket.off(RCV_DOC)
        socket.off(ON_ATTACHMENT_URL)
        mOnListenerRunnable = null
        if ((msg != null)) {
            serviceHandler.removeMessages(msg.what, msg.obj)
        }
        socket.disconnect()
        Log.e(TAG, ("disconnectFromSocket: Socket Disconnected onto ....." + url))
    }
    private fun saveMessageThreadItemToDB(msg: Message) {
        try {
            var args: Bundle = msg.getData()
            var item: MessageItem = args.getParcelable(MessageItem.TAG)
            MessageChatContentProvider.savesMessageItem(item, user, this)
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    fun asyncUpdateChatAttachmentFileInDB(attachment: MessageItem, context: Context) {
        try {
            var downloadURL: String = attachment.downloadURL
            if (downloadURL.startsWith("http")) {
                var key: String = MainApplicationSingleton.getLastPathFromURI(downloadURL)
                var file: File = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key)
                HttpUrlConnectionParser.downloadURLToFile(downloadURL, file)
                attachment.downloadURL = file.absolutePath
                MessageAttachmentIntentService.asyncUpdateChatAttachmentFilePathInDB(context, attachment)
            }
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    fun asyncUpdateChatGroupAttachmentFileInDB(attachment: MessageItem, context: Context) {
        try {
            var downloadURL: String = attachment.downloadURL
            if (downloadURL.startsWith("http")) {
                var key: String = MainApplicationSingleton.getLastPathFromURI(downloadURL)
                var file: File = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key)
                HttpUrlConnectionParser.downloadURLToFile(downloadURL, file)
                attachment.downloadURL = file.absolutePath
                MessageAttachmentIntentService.asyncUpdateChatGroupAttachmentFilePathInDB(context, attachment)
            }
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    @Throws(JSONException::class, IOException::class)
    private fun rcvDocContact(jsonObject: JSONObject) {
        var deviceContactItem: ContactItem = DeviceContactContentProvider.createsDeviceContactItemFromJSON(jsonObject, user.deviceRef)
        var uri: Uri = DeviceContactContentProvider.savesOrUpdatesDeviceContact(deviceContactItem, this)
        if ((uri != null)) {
            var id: Long = ContentUris.parseId(uri)
            deviceContactItem._id = id
            ackRcvDoc(deviceContactItem)
        }
        return
    }
    @Throws(JSONException::class, IOException::class)
    private fun rcvDocMessage(jsonObject: JSONObject, user: ContactItem) {
        var msgType: String = jsonObject.optString("msg_type")
        if (TextUtils.isEmpty(msgType)) {
            return
        }
        var messageItem: MessageItem = null
        if ("EMAIL") {

        }
        else {
            if ("CHAT") {
                var to_type: String = jsonObject.optString("to_type")
                if ("GROUP") {

                }
                else {
                    messageItem = MessageChatContentProvider.savesMsgDocTypeInDBFromJSON(jsonObject, user, this)
                }
            }
        }
        if (((null == messageItem) || (0 == messageItem._id))) {
            Log.e(TAG, ("rcvDocMessage: failed saving message -" + messageItem))
        }
        else {
            ackRcvDoc(messageItem)
        }
    }
    @Throws(JSONException::class, IOException::class)
    private fun rcvDocMessageInfo(jsonObject: JSONObject) {
        if ((null == jsonObject)) {
            return
        }
        var chatId: String = jsonObject.optString("chat_id")
        if ((null == chatId)) {
            return
        }
        if (MessageChatContentProvider.isMessageFoundByChatId(chatId, this)) {
            var messageItem: MessageItem = MessageItem()
            messageItem.chatId = chatId
            var result: Int = MessageChatContentProvider.updateMessageInfoFromJSONByChatId(jsonObject, messageItem, this)
            if ((0 == result)) {

            }
            else {
                result = MessageChatContentProvider.updateMessageInfo(chatId, messageItem, this)
                if ((0 == result)) {

                }
                ackRcvDoc(messageItem)
            }
        }
        else {
            Log.e(TAG, ("Message Ref not found: " + jsonObject))
        }
    }
    private fun emitGetAttachment(user: ContactItem, attachmentItem: MessageItem, resultReceiver: ResultReceiver) {
        try {
            var payload: JSONObject = JSONObject()
            payload.put(MainApplicationSingleton.UID_PARAM, user.dataId)
            if ("OUT") {
                payload.put("mailbox", "sent")
            }
            else {
                payload.put("mailbox", "INBOX")
            }
            payload.put(MainApplicationSingleton.EMAIL_PARAM, user.signupEmail)
            payload.put("msg_uid", attachmentItem.msgAttachID)
            payload.put("attch_id", attachmentItem.partID)
            payload.put("filename", attachmentItem.name)
            payload.put("encoding", attachmentItem.encoding)
            var content: String = ((attachmentItem.getType() + "/") + attachmentItem.subType)
            payload.put("content_type", content)
            Log.e(TAG, payload.toString())
            socket.emit("get_attachment", payload, ackGetAttachmentEmit)
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    @Throws(JSONException::class)
    private fun ackRcvDoc(item: BaseItem) {
        var payload: JSONObject = JSONObject()
        payload.put(DOC_ID, item.dataId)
        socket.emit(ACK_DOC, payload, ackDocEmit)
    }
    private fun showNotification() {
        var text: CharSequence = MainApplicationSingleton.INTELLIBITZ
        var contentIntent: PendingIntent = PendingIntent.getActivity(this, MainApplicationSingleton.PI_RCVDOC_RQ_CODE, Intent(this, MainApplicationSingleton.MAIN_ACTIVITY_CLASS), 0)
        var notification: Notification = Notification.Builder(this)
        mNM.notify(NOTIFICATION, notification)
    }
    private fun showNotification(msg: String) {
        var text: CharSequence = MainApplicationSingleton.INTELLIBITZ
        var contentIntent: PendingIntent = PendingIntent.getActivity(this, MainApplicationSingleton.PI_RCVDOC_RQ_CODE, Intent(this, MainApplicationSingleton.MAIN_ACTIVITY_CLASS), 0)
        var notification: Notification = Notification.Builder(this)
        mNM.notify(NOTIFICATION, notification)
    }
    @Throws(JSONException::class, IOException::class)
    private fun rcvDocMessageThread(jsonObject: JSONObject) {
        var messageItem: MessageItem = MessageItem()
        MessageChatContentProvider.fillMessageFromJSON(jsonObject, messageItem)
        var uri: Uri = MessageChatContentProvider.savesMessageItem(messageItem, user, this)
        var id: Long = Long.parseLong(uri.getLastPathSegment())
        messageItem._id = id
        ackRcvDoc(messageItem)
    }
    private fun emitGetAttachmentsAndUpdateFilePathInDB(messageItem: MessageItem, user: ContactItem) {
        var attachmentItems: Set<MessageItem> = messageItem.attachments
        for (attachmentItem in attachmentItems) {
            emitGetAttachment(user, attachmentItem, null)
        }
    }
    private fun sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null)
    }
    private fun sendShowTypingToClients(s: Object) {
        sendMessageToClients(MSG_SHOW_TYPING, s)
    }
    private fun runOnListenerRunnable() {
        if ((null == mOnListenerRunnable)) {
            mOnListenerRunnable = onListenerRunnable()
            mOnListenerRunnable.run()
            Log.e(TAG, "runOnListenerRunnable : started")
        }
    }
    class IncomingHandler : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_REGISTER_CLIENT -> {
                mClients.add(msg.replyTo)
                break
            }
            MSG_UNREGISTER_CLIENT -> {
                mClients.remove(msg.replyTo)
                break
            }
            MSG_SET_VALUE -> {
                if (reconnect) {
                    reconnectSocket(msg)
                }
                saveMessageThreadItemToDB(msg)
                break
            }
            else -> {
                super.handleMessage(msg)
            }
        }
    }
    }
    class ServiceHandler : Handler() {
    constructor(looper: Looper) : super(looper)
    override fun handleMessage(msg: Message) {
        if (((msg != null) && (msg.obj is Intent))) {
            onHandleIntent((msg.obj as Intent))
        }
    }
    }
    class onListenerRunnable : Runnable {
    override fun run() {
        try {
            socket.on(RCV_DOC, OnRcvDocEmitListener())
            socket.on(ON_ATTACHMENT_URL, onAttachmentUrlEmitListener())
            Log.e(TAG, ((((("onListenerRunnable: Listening on " + RCV_DOC) + " : ") + ON_ATTACHMENT_URL) + ".... ") + url))
        }
        catch (ignored: Throwable) {
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class JoinAck : Ack {
    var messg: Message = null
    constructor(msg: Message) : super() {
        messg = msg
    }
    override fun call(vararg objects: Object) {
        if (((messg != null) && (messg.obj != null))) {
            serviceHandler.sendMessage(messg)
        }
        var result: String = Arrays.toString(objects)
        Log.e(TAG, ("ackJoinUidEmit:Callback from 'join_uid' with results....." + result))
        try {
            var jsonArray: JSONArray = JSONArray(result)
            var jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var status: Int = jsonObject.getInt(STATUS)
            if ((0 == status)) {
                reconnect = true
            }
            if ((1 == status)) {
                ref = jsonObject.getString(REF)
                Log.e(TAG, ("RcvDocService Joined Socket with ref: " + ref))
            }
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class OnConnectListener : Emitter.Listener {
    private var mssg: Message = null
    constructor(msg: Message) : super() {
        mssg = msg
    }
    override fun call(vararg args1: Object) {
        try {
            var jsonObject: JSONObject = JSONObject()
            jsonObject.put(MainApplicationSingleton.UID_PARAM, user.dataId)
            jsonObject.put(MainApplicationSingleton.TOKEN_PARAM, user.token)
            jsonObject.put(MainApplicationSingleton.DEVICE_PARAM, user.device)
            jsonObject.put(MainApplicationSingleton.DEVICE_REF_PARAM, user.deviceRef)
            if ((null == mssg)) {
                socket.emit(JOIN_UID, jsonObject, JoinAck())
            }
            else {
                socket.emit(JOIN_UID, jsonObject, JoinAck(mssg))
            }
            Log.e(TAG, ("OnConnectListener: RcvDocService Emitting to 'join_uid'....." + MainApplicationSingleton.newJSONArray(args1)))
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class onAttachmentUrlEmitListener : Emitter.Listener {
    override fun call(vararg args: Object) {
        try {
            var jsonArray: JSONArray = MainApplicationSingleton.newJSONArray(args)
            Log.e(TAG, ("onAttachmentUrlEmitListener: " + jsonArray))
            var jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var attachmentItem: MessageItem = MessageItem()
            attachmentItem.name = jsonObject.getString("filename")
            var content_type: String = jsonObject.getString("content_type")
            var types: Array<String> = content_type.split("/")
            attachmentItem.type = types[0]
            attachmentItem.subType = types[1]
            attachmentItem.msgAttachID = jsonObject.getString("msg_uid")
            attachmentItem.partID = jsonObject.getString("attch_id")
            var key: String = ((attachmentItem.msgAttachID + attachmentItem.partID) + attachmentItem.name)
            var url: String = jsonObject.getString("attachment_url")
            var file: File = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key)
            attachmentItem.downloadURL = file.absolutePath
            HttpUrlConnectionParser.downloadURLToFile(url, file)
            MessageAttachmentIntentService.asyncUpdateEmailAttachmentFilePathInDB(getApplicationContext(), attachmentItem)
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class OnRcvDocEmitListener : Emitter.Listener {
    override fun call(vararg args1: Object) {
        var incomingMsg: String = Arrays.toString(args1)
        synchronized(lock) {
            if (!MainApplicationSingleton.isJSONValid(incomingMsg)) {
                Log.e(TAG, ("INVALID JSON: " + incomingMsg))
                return
            }
        }
        Log.e(TAG, ("RcvDocService Listening on 'rcv_doc'....." + incomingMsg))
        try {
            var jsonArray: JSONArray = JSONArray(incomingMsg)
            var jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var docType: String = jsonObject.getString("doc_type")
            if ((CONTACT.equals(other = docType, ignoreCase = true))) {
                Log.e(TAG, ((CONTACT + ":") + jsonObject))
                rcvDocContact(jsonObject)
            }
            else {
                if ((MSG.equals(other = docType, ignoreCase = true))) {
                    Log.e(TAG, ((MSG + ":") + jsonObject))
                    rcvDocMessage(jsonObject, user)
                }
                else {
                    if ((MSG_INFO.equals(other = docType, ignoreCase = true))) {
                        Log.e(TAG, ((MSG_INFO + ":") + jsonObject))
                        rcvDocMessageInfo(jsonObject)
                    }
                    else {
                        if ((GROUP.equals(other = docType, ignoreCase = true))) {
                            Log.e(TAG, ((GROUP + ":") + jsonObject))
                        }
                        else {
                            if ((GROUP_INFO.equals(other = docType, ignoreCase = true))) {
                                Log.e(TAG, ((GROUP_INFO + ":") + jsonObject))
                            }
                            else {
                                if ((BROADCAST.equals(other = docType, ignoreCase = true))) {
                                    Log.e(TAG, ((BROADCAST + ":") + jsonObject))
                                }
                                else {
                                    if ((QUEUE_NOTIFY.equals(other = docType, ignoreCase = true))) {
                                        Log.e(TAG, ((QUEUE_NOTIFY + ":") + jsonObject))
                                    }
                                    else {
                                        if ("THREAD") {

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class OnJoinedListener : Emitter.Listener {
    override fun call(vararg args1: Object) {
        try {
            Log.e(TAG, ("OnJoinedListener....." + MainApplicationSingleton.newJSONArray(args1)))
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    }
    class OnForceLogoutListener : Emitter.Listener {
    override fun call(vararg args1: Object) {
        try {
            Log.e(TAG, ("OnForceLogoutListener....." + MainApplicationSingleton.newJSONArray(args1)))
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    }
    class OnDisconnectListener : Emitter.Listener {
    override fun call(vararg args1: Object) {
        try {
            Log.e(TAG, ("OnDisconnectListener: ....." + MainApplicationSingleton.newJSONArray(args1)))
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    }
    class OnErrorListener : Emitter.Listener {
    override fun call(vararg args1: Object) {
        try {
            Log.e(TAG, ("OnErrorListener: ....." + MainApplicationSingleton.newJSONArray(args1)))
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    }
    class OnMessageListener : Emitter.Listener {
    override fun call(vararg args1: Object) {
        try {
            Log.e(TAG, ("OnMessageListener: ....." + MainApplicationSingleton.newJSONArray(args1)))
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    }
    class onAttachmentDataStartEmitListener : Emitter.Listener {
    override fun call(vararg args: Object) {
        try {
            var jsonArray: JSONArray = MainApplicationSingleton.newJSONArray(args)
            var jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var filename: String = jsonObject.getString("filename")
            var encoding: String = jsonObject.getString("encoding")
            var content_type: String = jsonObject.getString("content_type")
            var msg_uid: String = jsonObject.getString("msg_uid")
            var attach_id: String = jsonObject.getString("attch_id")
            var key: String = ((msg_uid + attach_id) + filename)
            var singleton: MainApplicationSingleton = MainApplicationSingleton.getInstance(getApplicationContext())
            singleton.initGlobalSB(key)
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class onAttachmentDataChunkEmitListener : Emitter.Listener {
    override fun call(vararg args: Object) {
        try {
            var jsonArray: JSONArray = MainApplicationSingleton.newJSONArray(args)
            var jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var filename: String = jsonObject.getString("filename")
            var encoding: String = jsonObject.getString("encoding")
            var content_type: String = jsonObject.getString("content_type")
            var msg_uid: String = jsonObject.getString("msg_uid")
            var attach_id: String = jsonObject.getString("attch_id")
            var chunk: String = jsonObject.getString("chunk")
            var key: String = ((msg_uid + attach_id) + filename)
            var singleton: MainApplicationSingleton = MainApplicationSingleton.getInstance(getApplicationContext())
            singleton.appendValToGlobalSB(key, chunk)
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class onAttachmentDataEndEmitListener : Emitter.Listener {
    override fun call(vararg args: Object) {
        try {
            var jsonArray: JSONArray = MainApplicationSingleton.newJSONArray(args)
            var jsonObject: JSONObject = jsonArray.getJSONObject(0)
            var attachmentItem: MessageItem = MessageItem()
            attachmentItem.name = jsonObject.getString("filename")
            attachmentItem.encoding = jsonObject.getString("encoding")
            var content_type: String = jsonObject.getString("content_type")
            var types: Array<String> = content_type.split("/")
            attachmentItem.type = types[0]
            attachmentItem.subType = types[1]
            attachmentItem.msgAttachID = jsonObject.getString("msg_uid")
            attachmentItem.partID = jsonObject.getString("attch_id")
            var key: String = ((attachmentItem.msgAttachID + attachmentItem.partID) + attachmentItem.name)
            var singleton: MainApplicationSingleton = MainApplicationSingleton.getInstance(getApplicationContext())
            var fileBase64: String = singleton.removeGlobalSB(key)
            if (!fileBase64.empty) {
                try {
                    var out: String = ""
                    if ("BASE64") {
                        out = MainApplicationSingleton.decodeBase64String(fileBase64)
                    }
                    else {
                        out = fileBase64
                    }
                    var file: File = MediaPickerFile.createFileInES(MainApplicationSingleton.INTELLIBITZ, key)
                    MainApplicationSingleton.writeStringToFile(out, file)
                    Log.d(TAG, ("attachment file: " + file.absolutePath))
                    attachmentItem.downloadURL = file.absolutePath
                    MessageAttachmentIntentService.asyncUpdateEmailAttachmentFilePathInDB(this, attachmentItem)
                }
                catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        catch (ignored: Throwable) {
            ignored.printStackTrace()
            Log.e(TAG, ignored.getMessage())
        }
    }
    }
    class onAttachmentStreamEmitListener : Emitter.Listener {
    override fun call(vararg args: Object) {
        var result: String = Arrays.toString(args)
        Log.e(TAG, ("Callback from 'attachment_stream' with results....." + result))
    }
    }
}
