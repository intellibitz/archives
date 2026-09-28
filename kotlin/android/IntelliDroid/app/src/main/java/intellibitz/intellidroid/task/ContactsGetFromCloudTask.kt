package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.util.HashMap

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class ContactsGetFromCloudTask(
    private val uid: String,
    private val token: String,
    private var device: String = "android",
    private val deviceRef: String,
    private val url: String
) : AsyncTask<Void, Void, Boolean>() {
    private var contactsGetFromCloudTaskListener: ContactsGetFromCloudTaskListener? = null
    private var response: JSONObject? = null

    fun setContactsGetFromCloudTaskListener(contactsUploadTaskListener: ContactsGetFromCloudTaskListener) {
        this.contactsGetFromCloudTaskListener = contactsUploadTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
        data[MainApplicationSingleton.UID_PARAM] = uid
        data[MainApplicationSingleton.TOKEN_PARAM] = token
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        contactsGetFromCloudTaskListener?.setContactsGetFromCloudTaskToNull()
        if (success == true) {
            contactsGetFromCloudTaskListener?.onPostContactsGetFromCloudExecute(response)
        } else {
            contactsGetFromCloudTaskListener?.onPostContactsGetFromCloudExecuteFail(response)
        }
    }

    override fun onCancelled() {
        contactsGetFromCloudTaskListener?.setContactsGetFromCloudTaskToNull()
        Log.e(TAG, "CONTACTS GET ERROR - $response")
    }

    interface ContactsGetFromCloudTaskListener {
        fun onPostContactsGetFromCloudExecute(response: JSONObject?)

        fun onPostContactsGetFromCloudExecuteFail(response: JSONObject?)

        fun setContactsGetFromCloudTaskToNull()
    }

    companion object {
        private const val TAG = "ContactsGetFromCloud"
    }
}
