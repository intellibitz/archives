package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.HashMap

/**
 */
class UpdateGroupTask(
    private val contactItem: ContactItem,
    private val messageItem: MessageItem,
    private val uid: String,
    private val token: String,
    private val device: String,
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {

    private var updateGroupTaskListener: UpdateGroupTaskListener? = null
    private val name: String = contactItem.name
    private val file: File? = contactItem.profilePic?.let { File(it) }
    private var response: JSONObject? = null

    fun setUpdateGroupTaskListener(groupsCreateTaskListener: UpdateGroupTaskListener) {
        updateGroupTaskListener = groupsCreateTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        if (contactItem == null) return false
        val id = contactItem.dataId
        if (id == null) return false
        if (file == null) {
            val data = HashMap<String, String>()
            data[MainApplicationSingleton.DEVICE_PARAM] = device
            data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
            data[MainApplicationSingleton.UID_PARAM] = uid
            data[MainApplicationSingleton.TOKEN_PARAM] = token
            data[MainApplicationSingleton.NAME_PARAM] = name
            data[MainApplicationSingleton.GROUP_ID_PARAM] = id
            // params comes from the execute() call: params[0] is the url.
            response = HttpUrlConnectionParser.postHTTP(url, data)
        } else {
            val fileName = file.absolutePath
            val charset = "UTF-8"
            try {
                val multipart = HttpUrlConnectionParser.MultipartUtility(url, charset)
                multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device)
                multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
                multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid)
                multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token)
                multipart.addFormField(MainApplicationSingleton.NAME_PARAM, name)
                multipart.addFormField(MainApplicationSingleton.GROUP_ID_PARAM, id)
                multipart.addFilePart(MainApplicationSingleton.PROFILE_PIC_PARAM, file, fileName)
                // params comes from the execute() call: params[0] is the url.
                response = multipart.finishAsJSON() // response from server.
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
        return response != null
    }

    override fun onPostExecute(success: Boolean?) {
        updateGroupTaskListener?.setUpdateGroupTaskToNull()
        if (success == true) {
            updateGroupTaskListener?.onPostUpdateGroupExecute(
                response,
                name,
                file,
                contactItem,
                messageItem
            )
        } else {
            updateGroupTaskListener?.onPostUpdateGroupExecuteFail(
                response,
                name,
                file,
                contactItem,
                messageItem
            )
        }
    }

    override fun onCancelled() {
        updateGroupTaskListener?.setUpdateGroupTaskToNull()
    }

    interface UpdateGroupTaskListener {
        fun onPostUpdateGroupExecute(
            response: JSONObject?,
            name: String,
            file: File?,
            contactItem: ContactItem,
            messageItem: MessageItem
        )

        fun onPostUpdateGroupExecuteFail(
            response: JSONObject?,
            name: String,
            file: File?,
            contactItem: ContactItem,
            messageItem: MessageItem
        )

        fun setUpdateGroupTaskToNull()
    }

    companion object {
        private const val TAG = "UpdateGroupTask"
    }
}
