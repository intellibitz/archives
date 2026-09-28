package intellibitz.intellidroid.task

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.io.IOException
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class MarkReadTask(
    private val context: Context,
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    private var device: String,
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {

    private var markReadTaskListener: MarkReadTaskListener? = null
    private var response: JSONObject? = null

    companion object {
        private const val TAG = "MarkReadTask"
    }

    fun setMarkReadTaskListener(groupInfoTaskListener: MarkReadTaskListener) {
        this.markReadTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // saves unread to 0 in DB
        if (messageItem == null || messageItem.chatId == null) return false
        if (messageItem.unreadCount > 0) {
            try {
                messageItem.unreadCount = 0
                MessageChatContentProvider.updateMessageChatThreadReadItems(messageItem, context)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // sends read receipt to the cloud
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.CHAT_ID_PARAM] = messageItem.chatId

        /*
        val ids = messageItem.unreadMessageIds
        if (ids == null) return false
        val array = JSONArray()
        for (id in ids) {
            array.put(id)
        }
        data[MainApplicationSingleton.MSGS_PARAM] = array.toString()
        */
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return response != null
    }

    override fun onPostExecute(success: Boolean?) {
        markReadTaskListener?.setMarkReadTaskToNull()
        if (success == true) {
            markReadTaskListener?.onPostMarkReadExecute(response, messageItem)
        } else {
            Log.e(TAG, ":error - $response")
            markReadTaskListener?.onPostMarkReadExecuteFail(response, messageItem)
        }
    }

    override fun onCancelled() {
        markReadTaskListener?.setMarkReadTaskToNull()
    }

    interface MarkReadTaskListener {
        fun onPostMarkReadExecute(response: JSONObject?, item: MessageItem)

        fun onPostMarkReadExecuteFail(response: JSONObject?, item: MessageItem)

        fun setMarkReadTaskToNull()
    }
}
