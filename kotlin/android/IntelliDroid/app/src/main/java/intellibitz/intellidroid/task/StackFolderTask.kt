package intellibitz.intellidroid.task

import android.os.AsyncTask
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 */
class StackFolderTask(
    private val name: String,
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    device: String,
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var stackFolderTaskListener: StackFolderTaskListener? = null
    private var device: String = device
    private var response: JSONObject? = null

    fun setStackFolderTaskListener(createFolderTaskListener: StackFolderTaskListener) {
        stackFolderTaskListener = createFolderTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.CODE_PARAM] = messageItem.folderCode
        data[MainApplicationSingleton.STACK_NAME_PARAM] = name
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return response != null
    }

    override fun onPostExecute(success: Boolean?) {
        stackFolderTaskListener?.setStackFolderFromCloudTaskToNull()
        if (success == true) {
            stackFolderTaskListener?.onPostStackFolderFromCloudExecute(response, name, messageItem)
        } else {
            stackFolderTaskListener?.onPostStackFolderFromCloudExecuteFail(response, name, messageItem)
        }
    }

    override fun onCancelled() {
        stackFolderTaskListener?.setStackFolderFromCloudTaskToNull()
    }

    interface StackFolderTaskListener {
        fun onPostStackFolderFromCloudExecute(response: JSONObject?, name: String, messageItem: MessageItem)

        fun onPostStackFolderFromCloudExecuteFail(response: JSONObject?, name: String, messageItem: MessageItem)

        fun setStackFolderFromCloudTaskToNull()
    }
}
