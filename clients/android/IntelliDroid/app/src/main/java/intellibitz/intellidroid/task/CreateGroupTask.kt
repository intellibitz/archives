package intellibitz.intellidroid.task

import android.os.AsyncTask
import android.util.Log
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.HashMap

/**
 */
class CreateGroupTask : AsyncTask<Void?, Void?, Boolean?> {
    private var createGroupTaskListener: CreateGroupTaskListener? = null
    private var contactItem: ContactItem? = null
    private var name: String? = null
    private var file: File? = null
    private var contacts: Array<String>? = null
    private var uid: String? = null
    private var token: String? = null
    private var device = "android"
    private var deviceRef: String? = null
    private var url: String? = null
    private var response: JSONObject? = null

    constructor(contactItem: ContactItem, uid: String,
                token: String, device: String, deviceRef: String, url: String) {
        this.contactItem = contactItem
        this.name = contactItem.name
        val profilePic = contactItem.profilePic
        if (profilePic != null) {
            this.file = File(profilePic)
        }
        this.contacts = contactItem.contactsAsArray
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    constructor(name: String, file: File?, contacts: Array<String>?, uid: String,
                token: String, device: String, deviceRef: String, url: String) {
        this.name = name
        this.file = file
        this.contacts = contacts
        this.uid = uid
        this.token = token
        this.device = device
        this.url = url
        this.deviceRef = deviceRef
    }

    fun setCreateGroupTaskListener(createGroupTaskListener: CreateGroupTaskListener?) {
        this.createGroupTaskListener = createGroupTaskListener
    }

    override fun doInBackground(vararg params: Void?): Boolean? {
        if (null == file) {
            val data = HashMap<String, String>()
            data[MainApplicationSingleton.DEVICE_PARAM] = device
            data[MainApplicationSingleton.DEVICE_REF_PARAM] = deviceRef
            data[MainApplicationSingleton.UID_PARAM] = uid
            data[MainApplicationSingleton.TOKEN_PARAM] = token
            data[MainApplicationSingleton.NAME_PARAM] = name
            // params comes from the execute() call: params[0] is the url.
            response = HttpUrlConnectionParser.postHTTP(url, data)
        } else {
            val fileName = file!!.absolutePath
            val charset = "UTF-8"
            try {
                val multipart =
                    HttpUrlConnectionParser.MultipartUtility(url, charset)
                multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device)
                multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
                multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid)
                multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token)
                multipart.addFormField(MainApplicationSingleton.NAME_PARAM, name)
                multipart.addFilePart(MainApplicationSingleton.PROFILE_PIC_PARAM, file, fileName)
                // params comes from the execute() call: params[0] is the url.
                response = multipart.finishAsJSON() // response from server.
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }

        }
        return null != response
    }

    override fun onPostExecute(success: Boolean?) {
        //        groupsCreateTask = null;
        createGroupTaskListener!!.setCreateGroupTaskToNull()
        if (success!!) {
            createGroupTaskListener!!.onPostCreateGroupExecute(response,
                name, file, contacts, contactItem)
        } else {
            createGroupTaskListener!!.onPostCreateGroupExecuteFail(response,
                name, file, contacts, contactItem)
        }
    }

    override fun onCancelled() {
        //        groupsCreateTask = null;
        createGroupTaskListener!!.setCreateGroupTaskToNull()
    }

    interface CreateGroupTaskListener {
        fun onPostCreateGroupExecute(response: JSONObject?,
                                     name: String?, file: File?, contacts: Array<String>?,
                                     contactItem: ContactItem?)

        fun onPostCreateGroupExecuteFail(response: JSONObject?,
                                         name: String?, file: File?, contacts: Array<String>?,
                                         contactItem: ContactItem?)

        fun setCreateGroupTaskToNull()
    }

    companion object {
        private const val TAG = "CreateGroupTask"
    }
}
