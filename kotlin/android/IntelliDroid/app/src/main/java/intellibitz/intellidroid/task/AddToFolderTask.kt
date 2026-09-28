package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.text.TextUtils
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collection
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class AddToFolderTask : AsyncTask<Void?, Void?, Boolean?> {
    private var addToFolderTaskListener: AddToFolderTaskListener? = null
    private var uid: String? = null
    private var token: String? = null
    private var device: String? = "android"
    private var deviceRef: String? = null
    private var url: String? = null
    private var code: String? = null
    private var messageItems: Collection<MessageItem>? = null
    private var messageItem: MessageItem? = null
    private var response: JSONObject? = null

    constructor(
        messageItems: Collection<MessageItem>?,
        code: String?,
        uid: String?,
        token: String?,
        device: String?,
        deviceRef: String?,
        url: String?
    ) {
        this.messageItems = messageItems
        this.code = code
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    constructor(
        messageItems: Collection<MessageItem>?,
        nest: MessageItem?,
        uid: String?,
        token: String?,
        device: String?,
        deviceRef: String?,
        url: String?
    ) {
        this.messageItems = messageItems
        this.messageItem = nest
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    fun setAddToFolderTaskListener(groupInfoTaskListener: AddToFolderTaskListener?) {
        this.addToFolderTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        if (null == messageItems || messageItems!!.isEmpty()) return false
        if (code == null && messageItem == null) return false
        if (TextUtils.isEmpty(code)) {
            code = messageItem!!.folderCode
        }
        if (TextUtils.isEmpty(code)) return false
        val data = HashMap<String, String?>()

        val array = JSONArray()
        for (messageItem in messageItems!!) {
            val dataId = messageItem.dataId
            if (!TextUtils.isEmpty(dataId)) array.put(dataId)
        }

        if (0 == array.length()) return false

        data[MainApplicationSingleton.MSGS_PARAM] = array.toString()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        data[MainApplicationSingleton.CODE_PARAM] = code

        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        addToFolderTaskListener?.setAddToFolderTaskToNull()
        if (success == true) {
            addToFolderTaskListener?.onPostAddToFolderTaskExecute(response, messageItems, messageItem)
        } else {
            addToFolderTaskListener?.onPostAddToFolderTaskExecuteFail(response, messageItems, messageItem)
        }
    }

    override fun onCancelled() {
        addToFolderTaskListener?.setAddToFolderTaskToNull()
    }

    interface AddToFolderTaskListener {
        fun onPostAddToFolderTaskExecute(
            response: JSONObject?,
            item: Collection<MessageItem>?,
            messageItem: MessageItem?
        )

        fun onPostAddToFolderTaskExecuteFail(
            response: JSONObject?,
            item: Collection<MessageItem>?,
            messageItem: MessageItem?
        )

        fun setAddToFolderTaskToNull()
    }

    companion object {
        private const val TAG = "AddToFolderTask"
    }
}
