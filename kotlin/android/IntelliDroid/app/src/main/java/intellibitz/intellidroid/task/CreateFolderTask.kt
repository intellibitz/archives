package intellibitz.intellidroid.task

import android.os.AsyncTask
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Created by muthuselvam on 24-06-2016.
 */
class CreateFolderTask(
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    private val device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var createFolderTaskListener: CreateFolderTaskListener? = null
    private var response: JSONObject? = null

    fun setCreateFolderTaskListener(createFolderTaskListener: CreateFolderTaskListener) {
        this.createFolderTaskListener = createFolderTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.NAME_PARAM] = messageItem.name
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean) {
        createFolderTaskListener?.setCreateFolderFromCloudTaskToNull()
        if (success) {
            createFolderTaskListener?.onPostCreateFolderFromCloudExecute(response, messageItem)
        } else {
            createFolderTaskListener?.onPostCreateFolderFromCloudExecuteFail(response, messageItem)
        }
    }

    override fun onCancelled() {
        createFolderTaskListener?.setCreateFolderFromCloudTaskToNull()
    }

    interface CreateFolderTaskListener {
        fun onPostCreateFolderFromCloudExecute(response: JSONObject?, nestItem: MessageItem)

        fun onPostCreateFolderFromCloudExecuteFail(response: JSONObject?, nestItem: MessageItem)

        fun setCreateFolderFromCloudTaskToNull()
    }
}
