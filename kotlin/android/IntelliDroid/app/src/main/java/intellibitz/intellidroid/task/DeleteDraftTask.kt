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
class DeleteDraftTask(
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var deleteDraftTaskListener: DeleteDraftTaskListener? = null
    private var response: JSONObject? = null

    fun setDeleteDraftTaskListener(deleteDraftTaskListener: DeleteDraftTaskListener) {
        this.deleteDraftTaskListener = deleteDraftTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        val id = messageItem.dataId
        if (TextUtils.isEmpty(id)) return false

        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.DRAFT_ID_PARAM] = id
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return response != null
    }

    override fun onPostExecute(success: Boolean) {
        deleteDraftTaskListener?.setDeleteDraftFromCloudTaskToNull()
        if (success) {
            deleteDraftTaskListener?.onPostDeleteDraftFromCloudExecute(response, messageItem)
        } else {
            deleteDraftTaskListener?.onPostDeleteDraftFromCloudExecuteFail(response, messageItem)
        }
    }

    override fun onCancelled() {
        deleteDraftTaskListener?.setDeleteDraftFromCloudTaskToNull()
    }

    interface DeleteDraftTaskListener {
        fun onPostDeleteDraftFromCloudExecute(response: JSONObject?, messageItem: MessageItem)

        fun onPostDeleteDraftFromCloudExecuteFail(response: JSONObject?, messageItem: MessageItem)

        fun setDeleteDraftFromCloudTaskToNull()
    }
}
