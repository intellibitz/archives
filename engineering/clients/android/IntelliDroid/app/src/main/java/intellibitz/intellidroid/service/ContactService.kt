package intellibitz.intellidroid.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import androidx.annotation.WorkerThread
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.task.BulkUploadContactsTask
import intellibitz.intellidroid.task.ContactsFetchTask
import intellibitz.intellidroid.task.ContactsSaveToDBTask
import intellibitz.intellidroid.task.GetBroadcastListTask
import intellibitz.intellidroid.task.GetGroupsTask
import intellibitz.intellidroid.task.GetWorkContactsTask
import intellibitz.intellidroid.task.GroupDetailsTask
import intellibitz.intellidroid.task.GroupsAddUsersTask
import intellibitz.intellidroid.task.WorkContactsSaveToDBTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet

class ContactService : Service,
    BulkUploadContactsTask.DeviceContactsUploadTaskListener,
    ContactsFetchTask.ContactsFetchTaskListener,
    ContactsSaveToDBTask.ContactsSaveToDBTaskListener,
    GetGroupsTask.GetGroupsTaskListener,
    GroupDetailsTask.GroupDetailsTaskListener,
    GroupsAddUsersTask.GroupsAddUsersTaskListener,
    GetBroadcastListTask.GetBroadcastTaskListener,
    GetWorkContactsTask.GetWorkContactsTaskListener,
    WorkContactsSaveToDBTask.WorkContactsSaveToDBTaskListener {

    companion object {
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
        const val MSG_ON_KEY = 4
        const val MSG_SHOW_TYPING = 5

        const val ACTION_UPDATE_CONTACT_DBEMPTY_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_DBEMPTY_URL"
        const val ACTION_UPDATE_CONTACT_BYCOUNT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_BYCOUNT_URL"
        const val ACTION_UPDATE_CONTACT_BYVERSION_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_BYVERSION_URL"
        const val ACTION_UPDATE_CONTACT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_URL"
        const val ACTION_UPDATE_WORK_CONTACT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_WORK_CONTACT_URL"
        const val ACTION_GET_GROUPS_URL =
            "intellibitz.intellidroid.service.action.ACTION_GET_GROUPS_URL"
        const val ACTION_GET_BROADCAST_URL =
            "intellibitz.intellidroid.service.action.ACTION_GET_BROADCAST_URL"
        const val ACTION_GET_GROUPS_DETAILS_URL =
            "intellibitz.intellidroid.service.action.ACTION_GET_GROUPS_DETAILS_URL"
        private const val TAG = "ContactService"

        @JvmStatic
        fun getGroups(user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_GET_GROUPS_URL
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun getBroadcast(user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_GET_BROADCAST_URL
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun getGroupDetails(contactItem: ContactItem, user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_GET_GROUPS_DETAILS_URL
                intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun asyncUpdateContacts(user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_UPDATE_CONTACT_URL
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun asyncUpdateWorkContacts(user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_UPDATE_WORK_CONTACT_URL
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun asyncUpdateContactsByCount(user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_UPDATE_CONTACT_BYCOUNT_URL
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun asyncUpdateContactsIfDBEmpty(user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_UPDATE_CONTACT_DBEMPTY_URL
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun asyncUpdateContactsByVersion(user: ContactItem, context: Context) {
            try {
                val intent = Intent(context, ContactService::class.java)
                intent.action = ACTION_UPDATE_CONTACT_BYVERSION_URL
                intent.putExtra(
                    ContactItem.USER_CONTACT,
                    UserContentProvider.getUserCloneForService(user) as Parcelable
                )
                context.startService(intent)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun startContactService(user: ContactItem, context: Context) {
            val intent = Intent(context, ContactService::class.java)
            intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
            context.startService(intent)
        }

        @JvmStatic
        fun stopContactService(context: Context) {
            val intent = Intent(context, ContactService::class.java)
            context.stopService(intent)
        }
    }

    val mMessenger: Messenger = Messenger(IncomingHandler())
    private val lock = Any()
    val mClients: ArrayList<Messenger> = ArrayList()
    var mValue: Int = 0
    private var contentObserver: ContentObserver? = null
    private var reconnect = false
    private var ref: String? = null
    private var user: ContactItem? = null
    private var bulkUploadContactsTask: BulkUploadContactsTask? = null
    private var getGroupsTask: GetGroupsTask? = null
    private var groupDetailsTask: GroupDetailsTask? = null
    private var groupsAddUsersTask: GroupsAddUsersTask? = null
    private var getBroadcastListTask: GetBroadcastListTask? = null
    private val NOTIFICATION = 100
    @Volatile
    private var mServiceLooper: Looper? = null
    @Volatile
    private var mServiceHandler: ServiceHandler? = null
    private var mName: String? = null
    private var mRedelivery = false

    constructor() : super()

    constructor(name: String) : super() {
        mName = name
    }

    private fun sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null)
    }

    private fun sendMessageToClients(msg: Int, `object`: Any?) {
        for (i in mClients.indices.reversed()) {
            try {
                val message = Message.obtain(null, msg, mValue, 0)
                message.obj = `object`
                mClients[i].send(message)
            } catch (e: RemoteException) {
                mClients.removeAt(i)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mMessenger.binder
    }

    fun setIntentRedelivery(enabled: Boolean) {
        mRedelivery = enabled
    }

    override fun onDestroy() {
        mServiceLooper?.quit()
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)

            val thread = HandlerThread("IntentService[$mName]")
            thread.start()

            mServiceLooper = thread.looper
            mServiceHandler = ServiceHandler(mServiceLooper!!)
            val msg = mServiceHandler?.obtainMessage()
            if (msg != null) {
                msg.arg1 = startId
                msg.obj = intent
                mServiceHandler?.sendMessage(msg)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        contentObserver = object : ContentObserver(Handler()) {
            override fun deliverSelfNotifications(): Boolean {
                return super.deliverSelfNotifications()
            }

            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                val u = user
                if (u == null) {
                    Log.e(TAG, "Contacts Content Observer: User NULL - ASYNC updates cannot start")
                } else {
                    Log.e(TAG, "Contacts Content Observer: ASYNC Updates of Contacts BEGIN")
                    asyncUpdateContactsByCount(u, applicationContext)
                }
            }
        }
        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI, false,
            contentObserver!!
        )
    }

    @WorkerThread
    protected fun onHandleIntent(intent: Intent?) {
        synchronized(lock) {
            if (intent != null) {
                val action = intent.action
                val u: ContactItem? = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                if (ACTION_UPDATE_CONTACT_URL == action && u != null) {
                    updateContacts(u)
                    return
                }
                if (ACTION_UPDATE_WORK_CONTACT_URL == action && u != null) {
                    execGetWorkContactsTask(u, this)
                    return
                }
                if (ACTION_UPDATE_CONTACT_BYVERSION_URL == action && u != null) {
                    updateContactsByVersion(u)
                    return
                }
                if (ACTION_UPDATE_CONTACT_DBEMPTY_URL == action && u != null) {
                    if (DeviceContactContentProvider.isContactsEmptyInDB(this)) {
                        updateContacts(u)
                        return
                    }
                }
                if (ACTION_UPDATE_CONTACT_BYCOUNT_URL == action && u != null) {
                    updateContactsByCount(u)
                    return
                }
                if (ACTION_GET_GROUPS_URL == action && u != null) {
                    getGroups(u)
                    return
                }
                if (ACTION_GET_BROADCAST_URL == action && u != null) {
                    getBroadcast(u)
                    return
                }
                if (ACTION_GET_GROUPS_DETAILS_URL == action && u != null) {
                    val contactItem: ContactItem? = intent.getParcelableExtra(ContactItem.TAG)
                    if (contactItem != null) {
                        getGroupDetails(contactItem, u)
                    }
                    return
                }
            }
        }
    }

    private fun updateContacts(user: ContactItem) {
        if (IntellibitzPermissionFragment.isReadContactsPermissionGranted(this)) {
            val contactsFetchTask = ContactsFetchTask(user, -1, applicationContext)
            contactsFetchTask.setContactsFetchTaskListener(this)
            contactsFetchTask.execute()
        }
    }

    private fun updateContactsByCount(user: ContactItem) {
        if (IntellibitzPermissionFragment.isReadContactsPermissionGranted(this)) {
            val contactsFetchTask = ContactsFetchTask(user, 0, applicationContext)
            contactsFetchTask.setContactsFetchTaskListener(this)
            contactsFetchTask.execute()
        }
    }

    private fun updateContactsByVersion(user: ContactItem) {
        if (IntellibitzPermissionFragment.isReadContactsPermissionGranted(this)) {
            val contactsFetchTask = ContactsFetchTask(user, -2, applicationContext)
            contactsFetchTask.setContactsFetchTaskListener(this)
            contactsFetchTask.execute()
        }
    }

    override fun onPostContactsFetchExecute(
        user: ContactItem?, sparseArray: SparseArray<ContactItem>?, flag: Int
    ) {
        if (sparseArray == null || 0 == sparseArray.size() || user == null) return
        if (-1 == flag) {
            saveDeviceContactsInDB(MainApplicationSingleton.asList(sparseArray) ?: emptyList(), this)
            execGetWorkContactsTask(user, this)
            return
        }
        if (-2 == flag) {
            uploadContactsToCloudIfDBVersionMismatch(sparseArray, user, this)
            execGetWorkContactsTask(user, this)
            return
        }
        if (0 == flag) {
            uploadContactsToCloudIfCountMismatch(sparseArray, user)
            execGetWorkContactsTask(user, this)
            return
        }
    }

    override fun onPostContactsFetchExecuteFail(user: ContactItem?, flag: Int) {
    }

    private fun uploadContactsToCloudIfCountMismatch(
        sparseArray: SparseArray<ContactItem>?, user: ContactItem
    ) {
        if (sparseArray != null && sparseArray.size() > 0) {
            val dbcount = DatabaseHelper.fetchRowCount(
                DeviceContactContentProvider.RAW_CONTENT_URI,
                DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                null,
                this
            )
            if (dbcount == sparseArray.size()) {
                Log.e(TAG, "uploadContactsToCloudIfCountMismatch: No Contacts SYNC require : $dbcount")
            } else if (dbcount < sparseArray.size()) {
                saveDeviceContactsInDB(MainApplicationSingleton.asList(sparseArray) ?: emptyList(), this)
                execGetWorkContactsTask(user, this)
            } else {
                Log.e(TAG, "uploadContactsToCloudIfCountMismatch: Fetch Device Contacts - SUCCESS - ")
                uploadContactsToCloudIfDBVersionMismatch(sparseArray, user, this)
            }
        }
    }

    private fun uploadContactsToCloudIfDBVersionMismatch(
        sparseArray: SparseArray<ContactItem>?,
        user: ContactItem,
        context: Context
    ) {
        if (sparseArray == null || 0 == sparseArray.size()) {
            Log.e(TAG, " Contacts EMPTY - Cannot upload to cloud")
            return
        }
        if (DeviceContactContentProvider.isContactsEmptyInDB(this)) {
            // do nothing
        } else {
            val deviceContactItemsToUpload = HashSet<ContactItem>()
            val cursor = contentResolver.query(
                Uri.withAppendedPath(DeviceContactContentProvider.CONTENT_URI, "0"),
                arrayOf(
                    ContactItemColumns.KEY_DEVICE_CONTACTID,
                    ContactItemColumns.KEY_VERSION
                ),
                null, null, null
            )
            if (cursor != null && cursor.count > 0) {
                val dbContacts = SparseIntArray()
                cursor.moveToFirst()
                do {
                    val id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID))
                    val ver = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_VERSION))
                    dbContacts.put(id.toInt(), ver)
                    val deviceContactItem = sparseArray.get(id.toInt())
                    if (deviceContactItem != null && ver < deviceContactItem.version) {
                        deviceContactItemsToUpload.add(deviceContactItem)
                    }
                } while (cursor.moveToNext())
                cursor.close()

                for (i in 0 until sparseArray.size()) {
                    val key = sparseArray.keyAt(i)
                    val item = sparseArray.get(key)
                    if (0 == dbContacts.get(item.deviceContactId.toInt())) {
                        deviceContactItemsToUpload.add(item)
                    }
                }
            }
            cursor?.close()
            saveDeviceContactsInDB(deviceContactItemsToUpload, context)
            execGetWorkContactsTask(user, this)
        }
    }

    private fun uploadContactsToCloud(
        contacts: Collection<ContactItem>, user: ContactItem, context: Context
    ) {
        bulkUploadContactsTask = BulkUploadContactsTask(
            contacts, user,
            MainApplicationSingleton.CONTACTS_BULK_UPLOAD_URL, context
        )
        bulkUploadContactsTask?.setDeviceContactsUploadTaskListener(this)
        bulkUploadContactsTask?.execute()
    }

    private fun execGetWorkContactsTask(user: ContactItem, context: Context) {
        val task = GetWorkContactsTask(
            user.companyId,
            user.dataId, user.token, user.device, user.deviceRef,
            user, MainApplicationSingleton.AUTH_CONTACT_GET_WORK_CONTACTS, context
        )
        task.requestTimeoutMillis = 30000
        task.setGetWorkContactsTaskListener(this)
        task.execute()
    }

    override fun onPostGetWorkContactsResponse(response: JSONObject?, companyId: String?, user: ContactItem?) {
        if (response == null) return
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (99 == status || -1 == status) {
                onPostGetWorkContactsErrorResponse(response)
            } else if (1 == status) {
                val contactsJSONArray = response.optJSONArray("contacts")
                if (contactsJSONArray != null && contactsJSONArray.length() > 0) {
                    saveWorkContactsInDB(contactsJSONArray, this)
                } else {
                    Log.e(TAG, "onPostGetWorkContactsResponse: Contacts Update is EMPTY - $response")
                    onPostGetWorkContactsErrorResponse(response)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "onPostGetWorkContactsResponse: Exception - " + e.message)
            onPostGetWorkContactsErrorResponse(response)
        }
    }

    override fun onPostGetWorkContactsErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetWorkContactsErrorResponse: - $response")
    }

    override fun onPostDeviceContactsUploadExecute(
        response: JSONObject?, deviceContactItems: Collection<ContactItem>?, user: ContactItem?
    ) {
        if (response == null || user == null) return
        try {
            val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (1 == status) {
                val contactsJSONArray = response.optJSONArray("contacts")
                if (contactsJSONArray != null && contactsJSONArray.length() > 0) {
                    saveDeviceContactsInDB(contactsJSONArray, user, this)
                } else {
                    Log.e(TAG, "onPostDeviceContactsUploadExecute: Contacts Update is EMPTY - $response")
                }
            } else {
                onPostContactsUploadExecuteFail(response, deviceContactItems, user)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, "onPostDeviceContactsUploadExecute: Exception - " + e.message)
        }
    }

    override fun onPostContactsUploadExecuteFail(
        response: JSONObject?, deviceContactItems: Collection<ContactItem>?, user: ContactItem?
    ) {
        Log.e(TAG, "onPostContactsUploadExecuteFail: $response")
    }

    override fun setContactsUploadTaskToNull() {
        bulkUploadContactsTask = null
    }

    private fun saveDeviceContactsInDB(contactsJSONArray: JSONArray, user: ContactItem, context: Context) {
        val cloudSyncedContacts =
            DeviceContactContentProvider.fillDeviceContactItemFromJSONArray(
                contactsJSONArray, user.deviceRef
            )
        saveDeviceContactsInDB(cloudSyncedContacts.values, context)
    }

    private fun saveWorkContactsInDB(contactsJSONArray: JSONArray, context: Context) {
        val workContacts =
            DeviceContactContentProvider.fillWorkContactItemFromJSONArray(contactsJSONArray)
        saveWorkContactsInDB(workContacts, context)
    }

    private fun saveWorkContactsInDB(deviceContactItems: Collection<ContactItem>, context: Context) {
        val contactsSaveToDBTask = WorkContactsSaveToDBTask(deviceContactItems, context)
        contactsSaveToDBTask.setWorkContactsSaveToDBTaskListener(this)
        contactsSaveToDBTask.execute()
    }

    private fun saveDeviceContactsInDB(deviceContactItems: Collection<ContactItem>, context: Context) {
        val contactsSaveToDBTask = ContactsSaveToDBTask(deviceContactItems, context)
        contactsSaveToDBTask.setContactsSaveToDBTaskListener(this)
        contactsSaveToDBTask.execute()
    }

    override fun onPostWorkContactsSaveToDBExecute(result: Uri?) {
        Log.e(TAG, "onPostWorkContactsSaveToDBExecute: Save in DB - SUCCESS - $result")
    }

    override fun onPostWorkContactsSaveToDBExecuteFail(result: Uri?) {
        Log.e(TAG, "onPostWorkContactsSaveToDBExecuteFail: ERROR - $result")
    }

    override fun onPostContactsSaveToDBExecute(result: Uri?) {
        Log.e(TAG, "onFetchCursorTaskExecute: Save in DB - SUCCESS - $result")
    }

    override fun onPostContactsSaveToDBExecuteFail(result: Uri?) {
        Log.e(TAG, "onFetchCursorTaskExecuteFail: ERROR - $result")
    }

    private fun getGroups(user: ContactItem) {
        getGroupsTask = GetGroupsTask(
            user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_GET_GROUPS
        )
        getGroupsTask?.setGetGroupsTaskListener(this)
        getGroupsTask?.execute()
    }

    private fun getBroadcast(user: ContactItem) {
        getBroadcastListTask = GetBroadcastListTask(
            user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_GET_BROADCAST_LISTS
        )
        getBroadcastListTask?.setGetBroadcastTaskListener(this)
        getBroadcastListTask?.execute()
    }

    override fun onPostGetGroupsTaskExecuteFail(response: JSONObject?) {
        Log.e(TAG, "onPostGetGroupsTaskExecuteFail: Cloud response: - $response")
    }

    override fun setGetGroupsTaskToNull() {
        getGroupsTask = null
    }

    override fun onPostGetGroupsTaskExecute(response: JSONObject?) {
        Log.e(TAG, "onPostGetGroupsTaskExecute: GROUPS GET SUCCESS - $response")
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || -1 == status || 99 == status) {
            onPostGetGroupsTaskExecuteFail(response)
        } else {
            val array = response.optJSONArray("groups")
            if (array == null || 0 == array.length()) {
                onPostGetGroupsTaskExecuteFail(response)
            } else {
                Log.e(TAG, " Groups get - SUCCESS - ")
            }
        }
    }

    override fun onPostGetBroadcastTaskExecuteFail(response: JSONObject?) {
        Log.e(TAG, "onPostGetBroadcastTaskExecuteFail: Cloud response: - $response")
    }

    override fun setGetBroadcastTaskToNull() {
        getBroadcastListTask = null
    }

    override fun onPostGetBroadcastTaskExecute(response: JSONObject?) {
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || -1 == status || 99 == status) {
            onPostGetBroadcastTaskExecuteFail(response)
        } else {
            val array = response.optJSONArray("lists")
            if (array == null || 0 == array.length()) {
                onPostGetBroadcastTaskExecuteFail(response)
            } else {
                Log.e(TAG, "onPostGetBroadcastTaskExecute: Success - $response")
            }
        }
    }

    private fun getGroupDetails(contactItem: ContactItem, user: ContactItem) {
        groupDetailsTask = GroupDetailsTask(
            contactItem, user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_GROUP_DETAILS
        )
        groupDetailsTask?.setGroupDetailsTaskListener(this)
        groupDetailsTask?.execute()
    }

    override fun onPostGroupDetailsTaskExecute(response: JSONObject?, item: ContactItem?) {
        val uri: Uri? = null
        if (uri != null && item != null) {
            addUsersToGroups(item)
        }
    }

    override fun onPostGroupDetailsTaskExecuteFail(response: JSONObject?, item: ContactItem?) {
        Log.e(TAG, "onPostGroupDetailsTaskExecuteFail: Cloud response: - $response")
    }

    override fun setGroupDetailsTaskToNull() {
        groupDetailsTask = null
    }

    private fun addUsersToGroups(contactItem: ContactItem) {
        val u = user ?: return
        groupsAddUsersTask = GroupsAddUsersTask(
            contactItem, u.dataId, u.token,
            u.device, u.deviceRef, MainApplicationSingleton.AUTH_GROUP_ADD_USERS
        )
        groupsAddUsersTask?.setGroupsAddUsersTaskListener(this)
        groupsAddUsersTask?.execute()
    }

    override fun onPostGroupsAddUsersExecuteFail(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<out String>?,
        contactItem: ContactItem?
    ) {
        Log.e(TAG, "onPostGroupsAddUsersExecuteFail: $response")
    }

    override fun setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null
    }

    override fun onPostGroupsAddUsersExecute(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<out String>?,
        contactItem: ContactItem?
    ) {
        var status = 0
        if (response != null) status = response.optInt("status")
        if (response == null || 99 == status || -1 == status) {
            onPostGroupsAddUsersExecuteFail(response, id, name, contacts, contactItem)
        } else {
            Log.e(TAG, " GROUPS ADD USERS - SUCCESS - ")
        }
    }

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> mClients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> mClients.remove(msg.replyTo)
                MSG_SET_VALUE -> mValue = msg.arg1
                MSG_ON_KEY -> mValue = msg.arg1
                else -> super.handleMessage(msg)
            }
        }
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            onHandleIntent(msg.obj as? Intent)
            mServiceLooper?.quit()
        }
    }
}
