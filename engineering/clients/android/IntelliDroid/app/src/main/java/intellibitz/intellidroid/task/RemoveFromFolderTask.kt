package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONObject

/**
 */
class RemoveFromFolderTask(
    private val messageItems: Collection<MessageItem>,
    private val code: String,
    private val uid: String,
    private val token: String,
    device: String,
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    companion object {
        private const val TAG = "RemoveFromFolderTask"
    }

    private var removeFromFolderTaskListener: RemoveFromFolderTaskListener? = null
    private var device: String = device
    private var response: JSONObject? = null

    fun setRemoveFromFolderTaskListener(removeFromFolderTaskListener: RemoveFromFolderTaskListener) {
        this.removeFromFolderTaskListener = removeFromFolderTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        if (messageItems.isEmpty()) return false
        // sends read receipt to the cloud
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.CODE_PARAM] = code

        val array = JSONArray()
        for (messageItem in messageItems) {
            array.put(messageItem.dataId)
        }
        data[MainApplicationSingleton.MSGS_PARAM] = array.toString()
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return response != null
    }

    override fun onPostExecute(success: Boolean) {
        removeFromFolderTaskListener?.setRemoveFromFolderTaskToNull()
        if (success) {
            removeFromFolderTaskListener?.onPostRemoveFromFolderTaskExecute(response, messageItems)
        } else {
            Log.e(TAG, ":error - $response")
            removeFromFolderTaskListener?.onPostRemoveFromFolderTaskExecuteFail(response, messageItems)
        }
    }

    override fun onCancelled() {
        removeFromFolderTaskListener?.setRemoveFromFolderTaskToNull()
    }

    interface RemoveFromFolderTaskListener {
        fun onPostRemoveFromFolderTaskExecute(response: JSONObject?, item: Collection<MessageItem>)

        fun onPostRemoveFromFolderTaskExecuteFail(response: JSONObject?, item: Collection<MessageItem>)

        fun setRemoveFromFolderTaskToNull()
    }
}
