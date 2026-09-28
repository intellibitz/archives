package intellibitz.intellidroid.task

import android.os.AsyncTask
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
class CreateDraftTask(
    private var messageItem: MessageItem?,
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var createDraftTaskListener: CreateDraftTaskListener? = null
    private var response: JSONObject? = null

    fun setCreateDraftTaskListener(createDraftTaskListener: CreateDraftTaskListener) {
        this.createDraftTaskListener = createDraftTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        try {
            if (null == messageItem) return false
            val jsonObject = MessageChatContentProvider.toJson(messageItem!!, uid, token, device, deviceRef)
            if (null == jsonObject) return false
            // TODO: attempt authentication against a network service.
            val data = HashMap<String, String>()
            data[MainApplicationSingleton.DEVICE_PARAM] = device
            data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
            data[MainApplicationSingleton.UID_PARAM] = uid
            data[MainApplicationSingleton.TOKEN_PARAM] = token

            data[MainApplicationSingleton.DRAFT_OBJ_PARAM] = jsonObject.toString()
            // params comes from the execute() call: params[0] is the url.
            response = HttpUrlConnectionParser.postHTTP(url, data)
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        createDraftTaskListener?.setCreateDraftFromCloudTaskToNull()
        if (success == true) {
            createDraftTaskListener?.onPostCreateDraftFromCloudExecute(response, messageItem)
        } else {
            createDraftTaskListener?.onPostCreateDraftFromCloudExecuteFail(response, messageItem)
        }
    }

    override fun onCancelled() {
        createDraftTaskListener?.setCreateDraftFromCloudTaskToNull()
    }

    interface CreateDraftTaskListener {
        fun onPostCreateDraftFromCloudExecute(response: JSONObject?, messageItem: MessageItem?)

        fun onPostCreateDraftFromCloudExecuteFail(response: JSONObject?, messageItem: MessageItem?)

        fun setCreateDraftFromCloudTaskToNull()
    }

    companion object {
        private const val TAG = "CreateDraftTask"
    }
}
