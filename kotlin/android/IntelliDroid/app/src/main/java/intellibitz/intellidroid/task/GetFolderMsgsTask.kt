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
class GetFolderMsgsTask(
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    private val device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {

    private var getFolderMsgsTaskListener: GetFolderMsgsTaskListener? = null
    private var response: JSONObject? = null

    fun setGetFolderMsgsTaskListener(deleteFolderTaskListener: GetFolderMsgsTaskListener) {
        this.getFolderMsgsTaskListener = deleteFolderTaskListener
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
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        getFolderMsgsTaskListener?.setGetFolderMsgsFromCloudTaskToNull()
        if (success) {
            getFolderMsgsTaskListener?.onPostGetFolderMsgsFromCloudExecute(response, messageItem)
        } else {
            getFolderMsgsTaskListener?.onPostGetFolderMsgsFromCloudExecuteFail(response, messageItem)
        }
    }

    override fun onCancelled() {
        getFolderMsgsTaskListener?.setGetFolderMsgsFromCloudTaskToNull()
    }

    interface GetFolderMsgsTaskListener {
        fun onPostGetFolderMsgsFromCloudExecute(response: JSONObject?, messageItem: MessageItem)

        fun onPostGetFolderMsgsFromCloudExecuteFail(response: JSONObject?, messageItem: MessageItem)

        fun setGetFolderMsgsFromCloudTaskToNull()
    }
}
