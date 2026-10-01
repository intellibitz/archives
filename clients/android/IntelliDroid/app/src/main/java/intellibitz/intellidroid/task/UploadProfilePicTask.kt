package intellibitz.intellidroid.task

import android.os.AsyncTask
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class UploadProfilePicTask(
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<File, Void, Boolean>() {
    private var uploadProfilePicTaskListener: UploadProfilePicTaskListener? = null
    private var response: JSONObject? = null
    private var fileName: String? = null

    fun setUploadProfilePicTaskListener(groupInfoTaskListener: UploadProfilePicTaskListener) {
        this.uploadProfilePicTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: File): Boolean? {
        if (params.isEmpty()) return null
        val file = params[0]
        fileName = file.absolutePath
        val charset = "UTF-8"
        try {
            val multipart = HttpUrlConnectionParser.MultipartUtility(url, charset)
            multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device)
            multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
            multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid)
            multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token)
            multipart.addFilePart(MainApplicationSingleton.PROFILE_PIC_PARAM, file, fileName)
            // params comes from the execute() call: params[0] is the url.
            response = multipart.finishAsJSON() // response from server.
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return response != null
    }

    override fun onPostExecute(success: Boolean?) {
        uploadProfilePicTaskListener?.setUploadProfilePicTaskToNull()
        uploadProfilePicTaskListener?.onPostUploadProfilePicExecute(response, fileName)
    }

    override fun onCancelled() {
        uploadProfilePicTaskListener?.setUploadProfilePicTaskToNull()
        uploadProfilePicTaskListener?.onPostUploadProfilePicExecuteFail(response, fileName)
    }

    interface UploadProfilePicTaskListener {
        fun onPostUploadProfilePicExecute(response: JSONObject?, filename: String?)

        fun onPostUploadProfilePicExecuteFail(response: JSONObject?, filename: String?)

        fun setUploadProfilePicTaskToNull()
    }

    companion object {
        private const val TAG = "UploadProfilePicTask"
    }
}
