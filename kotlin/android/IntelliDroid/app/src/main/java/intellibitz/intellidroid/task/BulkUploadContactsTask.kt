package intellibitz.intellidroid.task

import android.content.Context
import android.os.AsyncTask
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
class BulkUploadContactsTask(
    private val deviceContactItems: Collection<ContactItem>,
    private val user: ContactItem,
    private val url: String,
    private val context: Context
) : AsyncTask<Void, Void, Boolean>() {

    companion object {
        private const val TAG = "ContactsUploadTask"
    }

    private var deviceContactsUploadTaskListener: DeviceContactsUploadTaskListener? = null
    private var response: JSONObject? = null

    fun setDeviceContactsUploadTaskListener(deviceContactsUploadTaskListener: DeviceContactsUploadTaskListener) {
        this.deviceContactsUploadTaskListener = deviceContactsUploadTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        // TODO: attempt authentication against a network service.
        val data = HashMap<String, String>()
        data[MainApplicationSingleton.DEVICE_PARAM] = user.device
        data[MainApplicationSingleton.DEVICE_REF_PARAM] = user.deviceRef
        data[MainApplicationSingleton.UID_PARAM] = user.dataId
        data[MainApplicationSingleton.TOKEN_PARAM] = user.token
        val contacts = DeviceContactContentProvider.fillJsonArrayFromDeviceContactItem(
            deviceContactItems, context
        )
        data["contacts"] = contacts.toString()

        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data)

        if (null == response) {
            return false
        }
        // TODO: register the new account here.
//            investigate response and take action
        return true
    }

    override fun onPostExecute(success: Boolean) {
        deviceContactsUploadTaskListener?.setContactsUploadTaskToNull()
        if (success) {
            deviceContactsUploadTaskListener?.onPostDeviceContactsUploadExecute(response, deviceContactItems, user)
        } else {
            deviceContactsUploadTaskListener?.onPostContactsUploadExecuteFail(response, deviceContactItems, user)
        }
    }

    override fun onCancelled() {
        deviceContactsUploadTaskListener?.setContactsUploadTaskToNull()
    }

    interface DeviceContactsUploadTaskListener {
        fun onPostDeviceContactsUploadExecute(
            response: JSONObject?,
            deviceContactItems: Collection<ContactItem>,
            user: ContactItem
        )

        fun onPostContactsUploadExecuteFail(
            response: JSONObject?,
            deviceContactItems: Collection<ContactItem>,
            user: ContactItem
        )

        fun setContactsUploadTaskToNull()
    }
}
