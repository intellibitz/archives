package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.text.TextUtils
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 */
class UpdateFolderTask(
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var updateFolderTaskListener: UpdateFolderTaskListener? = null
    private var response: JSONObject? = null

    fun setUpdateFolderTaskListener(updateFolderTaskListener: UpdateFolderTaskListener) {
        this.updateFolderTaskListener = updateFolderTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val folderCode = messageItem.folderCode
        if (TextUtils.isEmpty(folderCode)) return false
        val name = messageItem.name
        if (TextUtils.isEmpty(name)) return false

        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.CODE_PARAM] = folderCode
        data[MainApplicationSingleton.NAME_PARAM] = name
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        updateFolderTaskListener?.setUpdateFolderFromCloudTaskToNull()
        if (success) {
            updateFolderTaskListener?.onPostUpdateFolderFromCloudExecute(response, messageItem)
        } else {
            updateFolderTaskListener?.onPostUpdateFolderFromCloudExecuteFail(response, messageItem)
        }
    }

    override fun onCancelled() {
        updateFolderTaskListener?.setUpdateFolderFromCloudTaskToNull()
    }

    interface UpdateFolderTaskListener {
        fun onPostUpdateFolderFromCloudExecute(response: JSONObject?, messageItem: MessageItem)

        fun onPostUpdateFolderFromCloudExecuteFail(response: JSONObject?, messageItem: MessageItem)

        fun setUpdateFolderFromCloudTaskToNull()
    }
}
