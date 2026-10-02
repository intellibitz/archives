package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

/**
 */
class UpdateDraftTask(
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    private val device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var updateDraftTaskListener: UpdateDraftTaskListener? = null
    private var response: JSONObject? = null

    fun setUpdateDraftTaskListener(updateDraftTaskListener: UpdateDraftTaskListener) {
        this.updateDraftTaskListener = updateDraftTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        try {
            if (messageItem == null) return false
            val id = messageItem.dataId
            if (TextUtils.isEmpty(id)) return false
            val jsonObject = MessageChatContentProvider.toJson(messageItem, uid, token, device, deviceRef)
            if (jsonObject == null) return false

            // TODO: attempt authentication against a network service.
            val data = HashMap<String, String>()
            data[MainApplicationSingleton.DEVICE_PARAM] = device
            data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
            data[MainApplicationSingleton.UID_PARAM] = uid
            data[MainApplicationSingleton.TOKEN_PARAM] = token

            data[MainApplicationSingleton.DRAFT_ID_PARAM] = id
            data[MainApplicationSingleton.DRAFT_OBJ_PARAM] = jsonObject.toString()
            // params comes from the execute() call: params[0] is the url.
            response = HttpUrlConnectionParser.postHTTP(url, data)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
        return response != null
    }

    override fun onPostExecute(success: Boolean?) {
        updateDraftTaskListener?.setUpdateDraftFromCloudTaskToNull()
        if (success == true) {
            updateDraftTaskListener?.onPostUpdateDraftFromCloudExecute(response, messageItem)
        } else {
            updateDraftTaskListener?.onPostUpdateDraftFromCloudExecuteFail(response, messageItem)
        }
    }

    override fun onCancelled() {
        updateDraftTaskListener?.setUpdateDraftFromCloudTaskToNull()
    }

    interface UpdateDraftTaskListener {
        fun onPostUpdateDraftFromCloudExecute(response: JSONObject?, messageItem: MessageItem)

        fun onPostUpdateDraftFromCloudExecuteFail(response: JSONObject?, messageItem: MessageItem)

        fun setUpdateDraftFromCloudTaskToNull()
    }

    companion object {
        private const val TAG = "UpdateDraftTask"
    }
}
