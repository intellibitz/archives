package intellibitz.intellidroid.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.provider.MediaStore
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.collection.LruCache
import androidx.core.view.ViewCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.bean.BaseBean
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInput
import java.io.ObjectInputStream
import java.io.ObjectOutput
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.lang.reflect.Array as JavaArray
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.HashSet
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class MainApplicationSingleton private constructor(context: Context?) {

    private var applicationContext: Context? = null
    private var mGlobalVariables: ConcurrentHashMap<String, ArrayList<out Any>>? = null
    var uidCurrentUser: String? = null
    private var sharedPreferences: SharedPreferences? = null
    var incomingQueue: ConcurrentLinkedQueue<Any>? = null
    var outgoingQueue: ConcurrentLinkedQueue<Any>? = null
    private var mRequestQueue: RequestQueue? = null
    private var mImageLoader: ImageLoader? = null

    init {
        if (context == null) {
            Log.e(TAG, "Context is null: $context")
        } else {
            initializeInstance(context)
        }
    }

    private fun initializeInstance(context: Context?) {
        mGlobalVariables = ConcurrentHashMap()
        incomingQueue = ConcurrentLinkedQueue()
        outgoingQueue = ConcurrentLinkedQueue()

        typeCodeMAP["None"] = "UnRegistered"
        typeCodeMAP["Other"] = "Unknown Organization"
        typeCodeMAP["Pvt Ltd"] = "Private Limited"
        typeCodeMAP["Public"] = "Public Limited"
        typeCodeMAP["INC"] = "Incorporated"
        typeCodeMAP["Partner"] = "Limited Liability, Partner"

        if (context == null) {
            Log.e(TAG, "Context is null: ")
        } else {
            applicationContext = context.applicationContext
        }
        if (applicationContext == null) {
            Log.e(TAG, "Application Context is null: $context")
        } else {
            getDefaultSharedPreferences(applicationContext!!)
            uidCurrentUser = getStringValueSP(UID_USER_LOGGED_IN_PARAM, false)
            mRequestQueue = requestQueue
            mImageLoader = imageLoader
        }
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(applicationContext)
            }
            return mRequestQueue!!
        }

    fun getRequestQueue(): RequestQueue = requestQueue

    val imageLoader: ImageLoader
        get() {
            if (mImageLoader == null) {
                mImageLoader = ImageLoader(requestQueue, imageCache)
            }
            return mImageLoader!!
        }

    fun getImageLoader(): ImageLoader = imageLoader

    private val imageCache: ImageLoader.ImageCache
        get() = object : ImageLoader.ImageCache {
            private val cache = LruCache<String, Bitmap>(20)

            override fun getBitmap(url: String): Bitmap? {
                if (!url.contains("http") || !url.contains("https")) {
                    val local = url.substring(url.indexOf("/"))
                    val bm = BitmapFactory.decodeFile(local)
                    return if (bm == null) {
                        null
                    } else {
                        putBitmap(url, bm)
                        bm
                    }
                }
                return cache.get(url)
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                cache.put(url, bitmap)
            }
        }

    fun getDefaultSharedPreferences(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
        return sharedPreferences!!
    }

    fun removeGlobalSBAsList(key: String): ArrayList<out Any>? {
        return mGlobalVariables?.remove(key)
    }

    fun removeGlobalSB(key: String): String {
        val `val` = getGlobalSBValue(key)
        mGlobalVariables?.remove(key)
        return `val`
    }

    fun initGlobalSB(key: String) {
        val vals = mGlobalVariables?.get(key)
        if (vals == null) {
            val newVals = ArrayList<Any>()
            mGlobalVariables?.put(key, newVals)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun appendValToGlobalSB(key: String, value: String) {
        initGlobalSB(key)
        val vals = mGlobalVariables?.get(key) as? ArrayList<String>
        vals?.add(value)
        if (vals != null) {
            mGlobalVariables?.put(key, vals)
        }
    }

    fun putGlobalVariable(key: String?, `val`: ArrayList<out Any>?) {
        if (key == null) return
        mGlobalVariables?.remove(key)
        if (`val` != null) {
            mGlobalVariables?.put(key, `val`)
        }
    }

    fun getGlobalSBValueAsList(key: String): ArrayList<out Any>? {
        return mGlobalVariables?.get(key)
    }

    fun getGlobalSBValue(key: String): String {
        val vals = mGlobalVariables?.get(key) ?: return ""
        val result = StringBuilder()
        for (`val` in vals) {
            result.append(`val`)
        }
        return result.toString()
    }

    fun getUidCurrentUser(): String? = uidCurrentUser

    fun setUidCurrentUser(uid: String?) {
        this.uidCurrentUser = uid
    }

    fun getIncomingQueue(): ConcurrentLinkedQueue<Any>? = incomingQueue

    fun setIncomingQueue(queue: ConcurrentLinkedQueue<Any>?) {
        this.incomingQueue = queue
    }

    fun getOutgoingQueue(): ConcurrentLinkedQueue<Any>? = outgoingQueue

    fun setOutgoingQueue(queue: ConcurrentLinkedQueue<Any>?) {
        this.outgoingQueue = queue
    }

    private fun getKey(key: String): String {
        return (getUidCurrentUser() ?: "") + key
    }

    fun getLongValueSP(key: String?): Long {
        if (!key.isNullOrEmpty()) {
            return sharedPreferences?.getLong(getKey(key), 0) ?: 0
        }
        return 0
    }

    @JvmOverloads
    fun getStringValueSP(key: String?, useUserPrefix: Boolean = true): String? {
        if (sharedPreferences == null) {
            Log.e(TAG, "Shared Preferences is null: cannot get key - $key")
            return null
        }
        if (!key.isNullOrEmpty()) {
            val targetKey = if (useUserPrefix) getKey(key) else key
            return sharedPreferences?.getString(targetKey, null)
        }
        return null
    }

    fun getStringSetValueSP(key: String?): MutableSet<String>? {
        if (!key.isNullOrEmpty()) {
            return sharedPreferences?.getStringSet(getKey(key), HashSet())
        }
        return null
    }

    fun getIntValueSP(key: String?): Int {
        if (!key.isNullOrEmpty()) {
            return sharedPreferences?.getInt(getKey(key), 0) ?: 0
        }
        return 0
    }

    fun getIntValueByDefaultSP(key: String?): Int {
        if (!key.isNullOrEmpty()) {
            return sharedPreferences?.getInt(getKey(key), 0) ?: 0
        }
        return 0
    }

    fun getBooleanValueSP(key: String?): Boolean {
        if (!key.isNullOrEmpty()) {
            return sharedPreferences?.getBoolean(getKey(key), false) ?: true
        }
        return true
    }

    fun getFloatValueSP(key: String?): Float {
        if (!key.isNullOrEmpty()) {
            return sharedPreferences?.getFloat(getKey(key), 0f) ?: 0f
        }
        return 0f
    }

    @JvmOverloads
    fun putStringValueSP(key: String?, value: String?, useUserPrefix: Boolean = true) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            val targetKey = if (useUserPrefix) getKey(key) else key
            editor.putString(targetKey, value)
            editor.apply()
        }
    }

    fun putStringSetValueSP(key: String?, value: Set<String>?) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            editor.putStringSet(getKey(key), value)
            editor.apply()
        }
    }

    fun addStringSetValueSP(key: String?, value: String) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            val values = getStringSetValueSP(key) ?: HashSet()
            values.add(value)
            editor.putStringSet(getKey(key), values)
            editor.apply()
        }
    }

    fun removeStringSetValueSP(key: String?, value: String) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            val values = getStringSetValueSP(key) ?: HashSet()
            values.remove(value)
            clearStringSetValueSP(key)
            editor.putStringSet(getKey(key), values)
            editor.apply()
        }
    }

    fun clearStringSetValueSP(key: String?) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            editor.remove(getKey(key))
            editor.apply()
        }
    }

    fun putIntValueSP(key: String?, value: Int) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            editor.putInt(getKey(key), value)
            editor.apply()
        }
    }

    fun putBooleanValueSP(key: String?, value: Boolean) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            editor.putBoolean(getKey(key), value)
            editor.apply()
        }
    }

    fun putLongValueSP(key: String?, value: Long) {
        if (!key.isNullOrEmpty()) {
            val editor = sharedPreferences?.edit() ?: return
            editor.putLong(getKey(key), value)
            editor.apply()
        }
    }

    fun putFloatValueSP(key: String?, value: Float?) {
        if (!key.isNullOrEmpty() && value != null) {
            val editor = sharedPreferences?.edit() ?: return
            editor.putFloat(getKey(key), value)
            editor.apply()
        }
    }

    companion object {
        const val TAG = "MainSingleton"

        @JvmField
        val MAIN_ACTIVITY_CLASS: Class<MainActivity> = MainActivity::class.java

        const val INTELLIBITZ = "Intellibitz"
        const val INTELLIBITZ_STORAGE_DIR = INTELLIBITZ

        const val API_HOST = "http://dev-api.intellibitz.com:3010/"
        const val SOCKET_HOST = "http://dev-socket.intellibitz.com:3010/"

        const val API_USER = "muthu"
        const val USER_PARAM = "user"
        const val API_KEY = "YOUR_API_KEY_HERE"
        const val KEY_PARAM = "key"

        const val AWS_ACCESS_KEY = "INSERT_VALID_KEY_HERE"
        const val AWS_SECRET_KEY = "INSERT_VALID_KEY_HERE"
        const val AWS_S3_BUCKET = "intellibitz-uploads"
        const val UID_USER_LOGGED_IN_PARAM = "uid_user_logged_in"
        const val DEVICE_ID = "android"
        const val URL_PARAM = "url"
        const val DUMMY_EMAIL = "info@intellibitz.net"
        const val EMAIL_ACCOUNT_PARAM = "email_account"
        const val EMAIL_UIDS_PARAM = "email_uids"
        const val SKIP_PARAM = "skip"
        const val BYEMAIL_PARAM = "by_email"
        const val GCM_TOKEN_PARAM = "push_token"
        const val DEVICE_PARAM = "device"
        const val DEVICE_ID_PARAM = "device_id"
        const val DEVICE_NAME_PARAM = "device_name"
        const val DEVICE_REF_PARAM = "device_ref"
        const val MOBILE_PARAM = "mobile"
        const val COUNTRY_PARAM = "country"
        const val COUNTRY_CODE_PARAM = "country_code"
        const val EMAIL_PARAM = "email"
        const val COMPANY_ID_PARAM = "company_id"

        const val POST_ID_PARAM = "post_id"
        const val TXT_PARAM = "txt"
        const val LAST_TIMESTAMP_PARAM = "last_timestamp"

        const val SCHEDULE_ID_PARAM = "schedule_id"
        const val SCHEDULE_TIME_PARAM = "schedule_time"
        const val SCHEDULE_MSG_PARAM = "schedule_msg"

        const val COMPANY_NAME_PARAM = "company_name"
        const val INVITED_COMPANY_ID_PARAM = "invited_company_id"
        const val INVITE_EMAILS_PARAM = "invite_emails"
        const val LOCKPWD_PARAM = "lock_pwd"
        const val STARTUPLOCK_PARAM = "startup_lock"
        const val INAPPLOCK_PARAM = "inapp_lock"
        const val PWD_PARAM = "pwd"
        const val OTP_PARAM = "otp"
        const val UID_PARAM = "uid"
        const val ID_PARAM = "_id"
        const val TOKEN_PARAM = "token"
        const val CHAT_ID_PARAM = "chat_id"
        const val GROUP_ID_PARAM = "group_id"
        const val DRAFT_ID_PARAM = "draft_id"
        const val USER_UID_PARAM = "user_uid"
        const val MODIFY_TYPE_PARAM = "modify_type"
        const val DOC_ID = "doc_id"
        const val STACK_NAME_PARAM = "stack_name"
        const val NAME_PARAM = "name"
        const val NEWPWD_PARAM = "newpwd"
        const val DRAFT_OBJ_PARAM = "draft_obj"
        const val STATUS_PARAM = "status"
        const val PROFILE_PIC_PARAM = "profile_pic"
        const val ATTACH_FILE_PARAM = "attach_file"
        const val CODE_PARAM = "code"
        const val RESP_CODE = "resp_code"
        const val MSGS_PARAM = "msgs"
        const val INTENT_ACTION_FETCH_CHAT_ATTACHMENT_CLOUD = "ACTION_FETCH_CHAT_ATTACHMENT_CLOUD"
        const val INTENT_ACTION_FETCH_CHATGROUP_ATTACHMENT_CLOUD = "ACTION_FETCH_CHATGROUP_ATTACHMENT_CLOUD"
        const val INTENT_ACTION_FETCH_EMAIL_ATTACHMENT_CLOUD = "ACTION_FETCH_EMAIL_ATTACHMENT_CLOUD"
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

        // PERMISSIONS
        const val PERM_READ_PHONE_STATE = 11
        const val PERM_READ_CONTACTS = 21
        const val PERM_READ_EXTERNAL_STORAGE = 31
        const val PERM_WRITE_EXTERNAL_STORAGE = 41
        const val PERM_CAMERA = 51
        const val PERM_STORAGE = 61

        // PENDING INTENT REQUEST CODES
        const val ACT_ACCT_LANDING_RQ_CODE = 111
        const val ACT_CHK_EMAIL_AVBL_RQ_CODE = 101
        const val ACT_VERIFY_CODE_RQ_CODE = 102
        const val ACT_PROFILE_SIGNUP_RQ_CODE = 103
        const val ACT_COMPANY_CREATE_RQ_CODE = 104
        const val ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE = 105
        const val ACT_COMPANY_GETINVITES_RQ_CODE = 106
        const val ACT_COMPANY_LIST_RQ_CODE = 107
        const val ACT_COMPANY_INVITE_USERS_RQ_CODE = 108
        const val ACT_CONTACT_DETAIL_RQ_CODE = 109
        const val ACT_SCHEDULE_RQ_CODE = 110

        const val ACTIVITY_LOGIN_RQ_CODE = 11
        const val ACTIVITY_OTP_RQ_CODE = 12
        const val ACTIVITY_PROFILEINFO_RQ_CODE = 13
        const val ACTIVITY_ADDEMAIL_RQ_CODE = 14
        const val ACTIVITY_ADDEMAILS_RQ_CODE = 15
        const val ACTIVITY_NEWEMAILACCOUNT_RQ_CODE = 16
        const val ACTIVITY_MSGSGRPDRAFT_RQ_CODE = 17
        const val ACTIVITY_MSGSGRPNEST_RQ_CODE = 18
        const val ACTIVITY_CONTACTSELECT_RQ_CODE = 19
        const val ACTIVITY_BROADCASTCONTACTS_RQ_CODE = 20
        const val ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE = 21
        const val ACTIVITY_MESSAGECHATGROUP_RQ_CODE = 22
        const val ACTIVITY_MSGSGRPNESTDETAIL_RQ_CODE = 23
        const val ACTIVITY_PEOPLEDETAIL_RQ_CODE = 24
        const val ACTIVITY_CLUTTEREMAILS_RQ_CODE = 25
        const val ACTIVITY_CLUTTEREMAIL_RQ_CODE = 26
        const val ACTIVITY_EMAILACCOUNT_RQ_CODE = 27
        const val ACTIVITY_PROILE_RQ_CODE = 28
        const val PI_MSG_LISTENER_RQ_CODE = 29
        const val PI_CHATTYMAIL_RQ_CODE = 30
        const val PI_EMAIL_RQ_CODE = 31
        const val PI_CHAT_RQ_CODE = 32
        const val PI_RCVDOC_RQ_CODE = 33
        const val ACTIVITY_MYACCOUNT_RQ_CODE = 34
        const val ACTIVITY_EMAILACCOUNTDETAIL_RQ_CODE = 35
        const val ACTIVITY_COMPOSEEMAIL_RQ_CODE = 36
        const val ACTIVITY_COMPOSEFEED_RQ_CODE = 37

        // LOADER ID CONSTANTS
        const val LOGIN_FRAGMENT_LOADERID = 701
        const val MSGSGRPNESTMESSAGES_LOADERID = 581
        const val CLUTTEREMAIL_LOADERID = 951
        const val CLUTTEREMAILS_LOADERID = 961
        const val MSGSGRPCLUTTER_LOADERID = 971
        const val MSGSGRPPEOPLE_LOADERID = 981
        const val MSGSGRPPEOPLECHATS_LOADERID = 991
        const val MSGSGRPDRAFT_LOADERID = 917
        const val MESSAGES_LOADERID = 991
        const val CONTACTSELECT_LOADERID = 801
        const val MESSAGECHATGROUP_LOADERID = 821
        const val BROADCAST_CONTACTS_LOADERID = 919
        const val CHATEMAIL_CONTENT_FRAGMENT_LOADERID = 101
        const val CHATEMAIL_MESSAGE_FRAGMENT_LOADERID = 201
        const val EMAIL_CONTENT_FRAGMENT_LOADERID = 301
        const val EMAIL_MESSAGE_FRAGMENT_LOADERID = 401
        const val CHAT_CONTENT_FRAGMENT_LOADERID = 501
        const val CHAT_MESSAGE_FRAGMENT_LOADERID = 601
        const val INTELLIBITZCONTACT_FRAGMENT_LOADERID = 801
        const val CONTACTITEM_FRAGMENT_LOADERID = 801
        const val FAV_CONTACTITEM_FRAGMENT_LOADERID = 901
        const val GROUP_CONTACTITEM_FRAGMENT_LOADERID = 111
        const val GROUPCONTACT_DETAIL_FRAGMENT_LOADERID = 211
        const val NESTITEM_FRAGMENT_LOADERID = 311
        const val STACKITEM_FRAGMENT_LOADERID = 411
        const val DRAFTITEM_FRAGMENT_LOADERID = 511
        const val FAV_NESTITEM_FRAGMENT_LOADERID = 611

        // GCM CONSTANTS
        const val SENT_TOKEN_TO_SERVER = "sentTokenToServer"

        // BROADCAST EVENTS
        const val BROADCAST_FORCE_LOGOUT = "FORCE_LOGOUT"
        const val BROADCAST_USER_UPDATED = "USER_UPDATED"
        const val BROADCAST_NEW_USER_COMPLETE = "NEW_USER_COMPLETE"
        const val BROADCAST_GCM_REGISTRATION_COMPLETE = "GCM_REGISTRATION_COMPLETE"
        const val BROADCAST_EMAIL_ACCOUNT_ADDED = "EMAIL_ACCOUNT_ADDED"
        const val BROADCAST_EMAIL_ACCOUNT_REMOVED = "EMAIL_ACCOUNT_REMOVED"
        const val BROADCAST_CONTACT_EMAIL_SELECTED = "CONTACT_EMAIL_SELECTED"
        const val BROADCAST_CONTACT_PHONE_SELECTED = "CONTACT_PHONE_SELECTED"
        const val BROADCAST_CONTACT_PROFILE_VIEW = "CONTACT_PROFILE_VIEW"
        const val BROADCAST_MESSAGETO_NEST = "MESSAGETO_NEST"
        const val BROADCAST_MESSAGETO_DRAFT = "MESSAGETO_DRAFT"
        const val BROADCAST_MESSAGES_SEND = "MESSAGES_SEND"
        const val BROADCAST_CONTACT_GROUP_SELECTED = "CONTACT_GROUP_SELECTED"
        const val BROADCAST_NEW_EMAIL_DIALOG_OK = "NEW_EMAIL_DIALOG_OK"
        const val BROADCAST_NEW_CHAT_DIALOG_OK = "NEW_CHAT_DIALOG_OK"

        // SOCKET SERVERS
        const val AUTH_GET_IP = "$SOCKET_HOST/auth/get-ip"

        // API SERVERS
        const val AUTH_ACCOUNT_CHECK_EMAIL_AVAILABLE = "$API_HOST/auth/account/check-email-available"
        const val AUTH_ACCOUNT_GET_EMAIL_VERIFICATION = "$API_HOST/auth/account/get-email-verification"
        const val AUTH_ACCOUNT_VERIFY_CODE = "$API_HOST/auth/account/verify-code"
        const val AUTH_ACCOUNT_SIGNUP = "$API_HOST/auth/account/signup"
        const val AUTH_ACCOUNT_SET_PWD = "$API_HOST/auth/account/set-pwd"
        const val AUTH_ACCOUNT_LOGIN = "$API_HOST/auth/account/login"
        const val AUTH_SET_PWD = "$API_HOST/auth/account/set-pwd"
        const val GCMTOKEN_UPLOAD_URL = "$API_HOST/auth/account/update-push-token"
        const val AUTH_GET_PROFILE = "$API_HOST/auth/account/get-profile"
        const val AUTH_UPDATE_PROFILE = "$API_HOST/auth/account/update-profile"

        const val AUTH_FEED_CREATE = "$API_HOST/auth/feed/create"
        const val AUTH_FEED_UPDATE = "$API_HOST/auth/feed/update"
        const val AUTH_FEED_DELETE = "$API_HOST/auth/feed/delete"
        const val AUTH_FEED_LIST = "$API_HOST/auth/feed/list"

        const val AUTH_SCHEDULE_CREATE = "$API_HOST/auth/schedule/create"
        const val AUTH_SCHEDULE_UPDATE = "$API_HOST/auth/schedule/update"
        const val AUTH_SCHEDULE_DELETE = "$API_HOST/auth/schedule/delete"
        const val AUTH_SCHEDULE_LIST = "$API_HOST/auth/schedule/list"

        const val AUTH_COMPANY_CREATE = "$API_HOST/auth/company/create"
        const val AUTH_COMPANY_INVITE_USERS = "$API_HOST/auth/company/invite-users"
        const val AUTH_COMPANY_GET_INVITES = "$API_HOST/auth/company/get-invites"
        const val AUTH_COMPANY_JOIN = "$API_HOST/auth/company/join"
        const val AUTH_COMPANY_LIST = "$API_HOST/auth/company/list"

        const val AUTH_CONTACT_GET_WORK_CONTACTS = "$API_HOST/auth/contact/get-work-contacts"

        const val AUTH_CHAT_SCHEDULE = "$API_HOST/auth/chat/schedule"

        const val AUTH_EMAIL_GET_RECENT_EMAILS = "$API_HOST/auth/email/get-recent-emails"
        const val AUTH_EMAIL_GET_MAILBOX_LIST = "$API_HOST/auth/email/get-mailbox-list"
        const val AUTH_EMAIL_GET_FULL_EMAILS = "$API_HOST/auth/email/get-full-emails"

        const val MOBILE_GETCODE_URL = "$API_HOST/auth/mobile-get-code"
        const val AUTH_OTP_CALL = "$API_HOST/auth/otp-call"
        const val AUTH_MOBILE_ACTIVATE = "$API_HOST/auth/mobile-activate"
        const val AUTH_ADD_EMAIL = "$API_HOST/auth/add-email"
        const val AUTH_GET_EMAILS = "$API_HOST/auth/get-emails"
        const val AUTH_GET_EMAILS_BY = "$API_HOST/auth/get-emails-by"
        const val AUTH_REMOVE_EMAIL = "$API_HOST/auth/remove-email"
        const val AUTH_EMAIL_GOOGLE_TOKEN = "$API_HOST/auth/email-google-token"
        const val CONTACTS_GET_URL = "$API_HOST/auth/get-contacts"
        const val CONTACTS_UPLOAD_URL = "$API_HOST/auth/upload-contacts"
        const val CONTACTS_BULK_UPLOAD_URL = "$API_HOST/auth/bulk-upload-contacts"
        const val ATTACHMENT_UPLOAD_URL = "$API_HOST/auth/upload-attachment"
        const val ATTACHMENT_GET_URL = "$API_HOST/auth/get-attachment"
        const val AUTH_UPLOAD_PROFILE_PIC = "$API_HOST/auth/upload-profile-pic"
        const val AUTH_GET_DOC = "$API_HOST/auth/get-doc"
        const val AUTH_ACK_DOC = "$API_HOST/auth/ack-doc"
        const val AUTH_MARK_READ = "$API_HOST/auth/mark-read"
        const val AUTH_DELETED_MSGS = "$API_HOST/auth/delete-msgs"
        const val AUTH_FLAG_MSGS = "$API_HOST/auth/flag-msgs"
        const val AUTH_UNFLAG_MSGS = "$API_HOST/auth/unflag-msgs"
        const val AUTH_GET_GROUPS = "$API_HOST/auth/get-groups"
        const val AUTH_GET_BROADCAST_LISTS = "$API_HOST/auth/get-broadcast-lists"
        const val AUTH_GROUP_DETAILS = "$API_HOST/auth/group-details"
        const val AUTH_GROUP_MODIFY_USER_TYPE = "$API_HOST/auth/group-modify-user-type"
        const val AUTH_CREATE_GROUP = "$API_HOST/auth/create-group"
        const val AUTH_CREATE_BROADCAST_LIST = "$API_HOST/auth/create-broadcast-list"
        const val AUTH_UPDATE_GROUP = "$API_HOST/auth/update-group"
        const val AUTH_GROUP_ADD_USERS = "$API_HOST/auth/group-add-users"
        const val AUTH_UPDATE_BROADCAST_LIST = "$API_HOST/auth/update-broadcast-list"
        const val AUTH_ADD_TO_FOLDER = "$API_HOST/auth/add-to-folder"
        const val AUTH_REMOVE_FROM_FOLDER = "$API_HOST/auth/remove-from-folder"
        const val AUTH_GET_FOLDER_MSGS = "$API_HOST/auth/get-folder-msgs"
        const val AUTH_GROUP_REMOVE_USERS = "$API_HOST/auth/group-remove-users"
        const val AUTH_LEAVE_GROUP = "$API_HOST/auth/leave-group"
        const val AUTH_GET_DRAFTS = "$API_HOST/auth/get-drafts"
        const val AUTH_CREATE_FOLDER = "$API_HOST/auth/create-folder"
        const val AUTH_GET_FOLDERS = "$API_HOST/auth/get-folders"
        const val AUTH_UPDATE_FOLDER = "$API_HOST/auth/update-folder"
        const val AUTH_DELETE_FOLDER = "$API_HOST/auth/delete-folder"
        const val AUTH_CREATE_DRAFT = "$API_HOST/auth/create-draft"

        const val ACTION_GET_CONTENT = 11
        const val REQUEST_CAMERA_AND_STORAGE_PERMISSION = 12
        const val REQUEST_AUDIO_AND_STORAGE_PERMISSION = 13
        const val REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION = 14
        const val REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION = 15
        const val REQUEST_CALL_PHONE_PERMISSION = 16

        @JvmField
        val NULL: Any = object : Any() {
            override fun equals(other: Any?): Boolean {
                return other === this || other == null
            }

            override fun hashCode(): Int {
                return 0
            }

            override fun toString(): String {
                return "null"
            }
        }

        @JvmField
        val typeCodeMAP: HashMap<String, String> = HashMap()

        @Volatile
        private var ourInstance: MainApplicationSingleton? = null

        @JvmStatic
        fun getInstance(context: Context?): MainApplicationSingleton {
            var instance = ourInstance
            if (instance == null) {
                synchronized(MainApplicationSingleton::class.java) {
                    instance = ourInstance
                    if (instance == null) {
                        instance = MainApplicationSingleton(context)
                        ourInstance = instance
                    }
                }
            }
            if (instance!!.mGlobalVariables == null && context != null) {
                instance!!.initializeInstance(context)
            }
            return instance!!
        }

        @JvmStatic
        fun checkPlayServices(context: Activity): Boolean {
            try {
                val apiAvailability = GoogleApiAvailability.getInstance()
                val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
                if (resultCode != ConnectionResult.SUCCESS) {
                    if (apiAvailability.isUserResolvableError(resultCode)) {
                        apiAvailability.getErrorDialog(
                            context, resultCode,
                            PLAY_SERVICES_RESOLUTION_REQUEST
                        )?.show()
                    } else {
                        Log.e(
                            TAG,
                            "GCM Play Services required for Notifications - Please update Google Play Services"
                        )
                    }
                    return false
                }
                return true
            } catch (ignored: Throwable) {
                Log.e(TAG, ignored.message ?: "")
                return false
            }
        }

        @JvmStatic
        fun decodeBase64String(fileBase64: ArrayList<String>): String {
            val result = StringBuilder()
            for (`val` in fileBase64) {
                val b = Base64.decode(`val`, Base64.DEFAULT)
                result.append(String(b))
            }
            return result.toString()
        }

        @JvmStatic
        fun decodeBase64String(fileBase64: String): String {
            val vals = ArrayList<String>()
            val fullBytes = fileBase64.toByteArray()
            val len = fullBytes.size
            try {
                val b = Base64.decode(fullBytes, 0, len, Base64.DEFAULT)
                vals.add(String(b))
            } catch (e: Throwable) {
                vals.add(fileBase64)
            }
            val result = StringBuilder()
            for (`val` in vals) {
                result.append(`val`)
            }
            return result.toString()
        }

        @JvmStatic
        fun decodeBase64String_(fileBase64: String): String {
            val vals = ArrayList<String>()
            val fullBytes = fileBase64.toByteArray()
            val len = fullBytes.size
            val parts2 = len % 1024
            var parts = len / 1024
            if (parts2 > 0) {
                parts += 1
            }
            var start: Int
            var finish = 0
            for (i in 0 until parts) {
                start = finish
                finish = if (i == parts - 1) {
                    if (0 == finish) len else len - finish
                } else {
                    finish + 1024
                }
                try {
                    val b = Base64.decode(fullBytes, start, finish, Base64.DEFAULT)
                    vals.add(String(b))
                } catch (e: Throwable) {
                    vals.add(fileBase64)
                    break
                }
            }
            val result = StringBuilder()
            for (`val` in vals) {
                result.append(`val`)
            }
            return result.toString()
        }

        @JvmStatic
        @Throws(IOException::class)
        fun writeStringToFile(out: String, file: File) {
            val fileOutputStream = FileOutputStream(file)
            val fullBytes = out.toByteArray()
            val byteArrayInputStream = ByteArrayInputStream(fullBytes)
            val len = fullBytes.size
            val parts2 = len % 1024
            var parts = len / 1024
            if (parts2 > 0) {
                parts += 1
            }
            var start: Int
            var finish = 0
            for (i in 0 until parts) {
                start = finish
                finish = if (i == parts - 1) {
                    len
                } else {
                    finish + 1024
                }
                val count = finish - start
                val buffer = ByteArray(count)
                val read = byteArrayInputStream.read(buffer, 0, count)
                if (read != -1) {
                    fileOutputStream.write(buffer, 0, count)
                }
            }
            byteArrayInputStream.close()
            fileOutputStream.close()
        }

        @JvmStatic
        fun performOnUIHandlerThread(runnable: Runnable): HandlerThread {
            val handlerThread = HandlerThread("MyHandlerThread")
            handlerThread.start()
            val handler = Handler(Looper.getMainLooper())
            handler.post(runnable)
            return handlerThread
        }

        @JvmStatic
        @JvmOverloads
        fun performOnHandlerThread(runnable: Runnable, looper: Looper? = null): HandlerThread {
            val handlerThread = HandlerThread("MyHandlerThread")
            handlerThread.start()
            val targetLooper = looper ?: handlerThread.looper
            val handler = Handler(targetLooper)
            handler.post(runnable)
            return handlerThread
        }

        @JvmStatic
        fun getDateTimeMillis(time: Long): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            var adjustedTime = time
            val now = getDateTimeMillis()
            if (java.lang.Long.bitCount(now) != java.lang.Long.bitCount(time)) {
                adjustedTime = time * 1000
            }
            val date = Date(adjustedTime)
            return dateFormat.format(date)
        }

        @JvmStatic
        fun getDateTimeISO(time: String?): String? {
            if (time.isNullOrEmpty()) return null
            var datetime: String? = null
            try {
                val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
                val date = dateFormat.parse(time)
                if (date != null) {
                    datetime = dateFormat.format(date)
                }
            } catch (ignored: ParseException) {
            }
            return datetime
        }

        @JvmStatic
        fun getDateTimeMillisISO(time: String?): Long {
            var datetime: Long = 0
            if (time.isNullOrEmpty()) return datetime
            try {
                val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
                val date = dateFormat.parse(time)
                if (date != null) {
                    datetime = date.time
                }
            } catch (ignored: ParseException) {
            }
            return datetime
        }

        @JvmStatic
        fun getDateTimeMillis(): Long {
            return System.currentTimeMillis()
        }

        @NonNull
        @JvmStatic
        @Throws(URISyntaxException::class)
        fun getLastPathFromURI(downloadURL: String): String {
            val uri = URI(downloadURL)
            val path = uri.path ?: return ""
            return path.substring(path.lastIndexOf('/') + 1)
        }

        @JvmStatic
        fun fillIfNotNull(values: ContentValues, name: String, `val`: Any?): ContentValues {
            if (`val` != null) {
                if (`val` is JSONArray) {
                    if (`val`.length() > 0) values.put(name, `val`.toString())
                } else {
                    values.put(name, `val`.toString())
                }
            }
            return values
        }

        @JvmStatic
        fun fillIfNotNull(values: ContentValues, name: String, `val`: String?): ContentValues {
            if (!isEmpty(`val`)) {
                values.put(name, `val`)
            }
            return values
        }

        @JvmStatic
        fun fillIfNotNull(values: ContentValues, name: String, `val`: Long): ContentValues {
            if (`val` != 0L) {
                values.put(name, `val`)
            }
            return values
        }

        @JvmStatic
        fun fillIfNotNull(values: ContentValues, name: String, `val`: Boolean): ContentValues {
            if (`val`) {
                values.put(name, `val`)
            }
            return values
        }

        @JvmStatic
        fun parseFormatNoCCPhoneNumberByISO(number: String?, iso: String?): String? {
            if (number.isNullOrEmpty()) return null
            var formattedPhoneNumber: String? = null
            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            try {
                val phoneNumber = phoneNumberUtil.parse(number, iso)
                if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                    formattedPhoneNumber = phoneNumber.nationalNumber.toString()
                }
            } catch (npe: NumberParseException) {
                formattedPhoneNumber = number
            }
            return formattedPhoneNumber
        }

        @JvmStatic
        fun parseFormatPhoneNumberByISO(number: String?, iso: String?): String? {
            if (number.isNullOrEmpty()) return null
            var formattedPhoneNumber: String? = null
            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            try {
                val phoneNumber = phoneNumberUtil.parse(number, iso)
                if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                    formattedPhoneNumber = phoneNumberUtil.format(
                        phoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.E164
                    )
                }
            } catch (npe: NumberParseException) {
                formattedPhoneNumber = null
            }
            return formattedPhoneNumber
        }

        @JvmStatic
        fun getSimCountryIso(context: Context): String {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            var country = telephonyManager?.networkCountryIso
            if (country == null) {
                country = telephonyManager?.simCountryIso
            }
            if (country == null) {
                country = Locale.getDefault().country
            }
            if (country == null) {
                country = "IN"
            }
            return country.uppercase(Locale.getDefault())
        }

        @JvmStatic
        fun hitTest(v: View, x: Int, y: Int): Boolean {
            val tx = (ViewCompat.getTranslationX(v) + 0.5f).toInt()
            val ty = (ViewCompat.getTranslationY(v) + 0.5f).toInt()
            val left = v.left + tx
            val right = v.right + tx
            val top = v.top + ty
            val bottom = v.bottom + ty
            return x in left..right && y in top..bottom
        }

        @JvmStatic
        fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                val bmp = drawable.bitmap
                if (bmp != null) {
                    return bmp
                }
            }
            val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        @JvmStatic
        fun getRoundedShape(scaleBitmapImage: Bitmap, w: Int, h: Int): Bitmap {
            val targetBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(targetBitmap)
            val path = Path()
            path.addCircle(
                (w.toFloat() - 1) / 2,
                (h.toFloat() - 1) / 2,
                Math.min(w.toFloat(), h.toFloat()) / 2,
                Path.Direction.CCW
            )
            canvas.clipPath(path)
            canvas.drawBitmap(
                scaleBitmapImage,
                Rect(0, 0, scaleBitmapImage.width, scaleBitmapImage.height),
                Rect(0, 0, w, h), null
            )
            return targetBitmap
        }

        @JvmStatic
        fun decodeStream(inputStream: InputStream, sz: Int): Bitmap? {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, o)

            var scale = 1
            while (o.outWidth / scale / 2 >= sz && o.outHeight / scale / 2 >= sz) {
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            return BitmapFactory.decodeStream(inputStream, null, o2)
        }

        @JvmStatic
        fun <C> asList(sparseArray: SparseArray<C>?): List<C>? {
            if (sparseArray == null) return null
            val arrayList = ArrayList<C>(sparseArray.size())
            for (i in 0 until sparseArray.size()) {
                arrayList.add(sparseArray.valueAt(i))
            }
            return arrayList
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        inline fun <reified C> asArray(sparseArray: SparseArray<C>?): kotlin.Array<C>? {
            val arrayList = asList(sparseArray) ?: return null
            return (arrayList as ArrayList<C>).toTypedArray()
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> getBaseItem(id: String?, items: Collection<BaseBean>?): T? {
            if (id == null || items == null || items.isEmpty()) return null
            for (item in items) {
                if (id == item.dataId) return item as? T
            }
            return null
        }

        @JvmStatic
        fun createsJSONArrayFromCollection(objects: Collection<*>?): JSONArray {
            val jsonArray = JSONArray()
            if (objects == null || objects.isEmpty()) return jsonArray
            for (`object` in objects) {
                jsonArray.put(`object`?.toString() ?: "")
            }
            return jsonArray
        }

        @JvmStatic
        fun isJSONValid(test: String?): Boolean {
            if (!isEmpty(test) &&
                (test!!.startsWith("{") || test.startsWith("[")) &&
                (test.endsWith("}") || test.endsWith("]"))
            ) {
                return try {
                    JSONObject(test)
                    true
                } catch (ex: JSONException) {
                    try {
                        JSONArray(test)
                        true
                    } catch (ex1: JSONException) {
                        false
                    }
                }
            }
            return false
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getBitmapDecodeHttp(pic: String?): Bitmap? {
            if (isEmpty(pic)) return null
            var bitmap: Bitmap? = null
            if (pic!!.startsWith("http")) {
                bitmap = HttpUrlConnectionParser.getBitmapFromURL(pic)
            }
            return bitmap
        }

        @Nullable
        @JvmStatic
        fun getBitmapDecodeFile(pic: String?): Bitmap? {
            if (isEmpty(pic)) return null
            var bitmap: Bitmap? = null
            if (pic!!.startsWith("/")) {
                bitmap = BitmapFactory.decodeFile(pic)
            }
            return bitmap
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getBitmapDecodeAnyUri(pic: String?, context: Context): Bitmap? {
            if (pic.isNullOrEmpty()) return null
            return getBitmapDecodeAnyUri(Uri.parse(pic), context)
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getBitmapDecodeAnyUri(uri: Uri, context: Context): Bitmap? {
            var bitmap: Bitmap? = null
            val pic = uri.toString()
            if (isEmpty(pic)) return bitmap
            try {
                bitmap = getBitmapDecodePhotoUri(uri, context)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                Log.d(TAG, e.message ?: "")
            }
            if (bitmap == null) {
                try {
                    bitmap = getBitmapDecodeContactsUri(uri, context)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    Log.d(TAG, e.message ?: "")
                }
            }
            if (bitmap == null) {
                bitmap = getBitmapDecodeFile(pic)
            }
            if (bitmap == null) {
                try {
                    bitmap = getBitmapMediaStoreImages(uri, context)
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, e.message ?: "")
                }
            }
            if (bitmap == null) {
                bitmap = getBitmapDecodeHttp(pic)
            }
            return bitmap
        }

        @Nullable
        @JvmStatic
        @Throws(IOException::class)
        fun getBitmapMediaStoreImages(uri: Uri, context: Context): Bitmap? {
            return MediaStore.Images.Media.getBitmap(
                context.applicationContext.contentResolver, uri
            )
        }

        @Nullable
        @JvmStatic
        @Throws(IOException::class)
        fun getBitmapDecodePhotoUri(photoURI: Uri?, context: Context): Bitmap? {
            if (photoURI == null) return null
            val cursor: Cursor? = context.applicationContext.contentResolver.query(
                photoURI,
                arrayOf(ContactsContract.CommonDataKinds.Photo.PHOTO), null, null, null
            )
            return try {
                if (cursor == null || !cursor.moveToNext()) {
                    null
                } else {
                    val data = cursor.getBlob(0) ?: return null
                    val inputStream = ByteArrayInputStream(data)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    bitmap
                }
            } finally {
                cursor?.close()
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getBitmapDecodeContactsUri(uri: Uri, context: Context): Bitmap? {
            val inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                context.applicationContext.contentResolver, uri
            ) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            return bitmap
        }

        @JvmStatic
        fun getMimeTypeFromURL(url: String?): String? {
            if (url.isNullOrEmpty()) return url
            val extension = url.substring(url.lastIndexOf(".") + 1)
            if (extension.isEmpty()) return extension
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        @JvmStatic
        fun getLastPath(url: String?): String? {
            if (url.isNullOrEmpty()) return url
            return url.substring(url.lastIndexOf("/") + 1)
        }

        @JvmStatic
        fun startMimeActivity(uri: String?, mimeType: String?, context: Context) {
            if (!isEmpty(uri)) {
                startMimeActivity(Uri.parse(uri), mimeType, context)
            }
        }

        @JvmStatic
        fun startMimeActivity(uri: Uri, mimeType: String?, context: Context) {
            val intent = Intent(Intent.ACTION_VIEW)
            var target = intent
            var resolvedMimeType = mimeType
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            val mt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mt != null) resolvedMimeType = mt
            if (resolvedMimeType == null) {
                intent.data = uri
                target = Intent.createChooser(intent, "Choose an app to open with:")
            } else {
                intent.setDataAndType(uri, resolvedMimeType)
            }
            context.startActivity(target)
        }

        @JvmStatic
        fun startMimeActivity(file: File, mimeType: String?, context: Context) {
            val uri = Uri.fromFile(file)
            startMimeActivity(uri, mimeType, context)
        }

        @JvmStatic
        fun logD(sb: JSONObject?, TAG: String) {
            if (sb == null || sb.length() == 0) {
                Log.d(TAG, " EMPTY log message")
                return
            }
            logD(StringBuffer(sb.toString()), TAG)
        }

        @JvmStatic
        fun logD(sb: StringBuffer?, TAG: String) {
            if (sb == null || sb.length == 0) {
                Log.d(TAG, " EMPTY log message")
                return
            }
            if (sb.length > 4000) {
                Log.d(TAG, "sb.length = " + sb.length)
                val chunkCount = sb.length / 4000
                for (i in 0..chunkCount) {
                    val max = 4000 * (i + 1)
                    if (max >= sb.length) {
                        Log.d(TAG, "$i of $chunkCount:${sb.substring(4000 * i)}")
                    } else {
                        Log.d(TAG, "$i of $chunkCount:${sb.substring(4000 * i, max)}")
                    }
                }
            } else {
                Log.d(TAG, sb.toString())
            }
        }

        @JvmStatic
        fun forceLogout(context: Context) {
            val dialog = alertDialog(
                context,
                "Authentication Failed.. Please Register again for Intellibitz to work",
                "Force Logout",
                { _, _ -> },
                { _, _ -> }
            )
            dialog.show()
        }

        @JvmStatic
        fun alertDialog(
            context: Context,
            msg: String,
            title: String,
            okListener: DialogInterface.OnClickListener?,
            cancelListener: DialogInterface.OnClickListener?
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(msg).setTitle(title)
            if (okListener != null) {
                builder.setPositiveButton(R.string.ok, okListener)
            }
            if (cancelListener != null) {
                builder.setNegativeButton(R.string.cancel, cancelListener)
            }
            val dialog = builder.create()
            dialog.show()
            return dialog
        }

        @JvmStatic
        fun isEmpty(valStr: String?): Boolean {
            return "null".equals(valStr, ignoreCase = true) || TextUtils.isEmpty(valStr)
        }

        @JvmStatic
        fun isAPI15(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1

        @JvmStatic
        fun isAPI16(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN

        @JvmStatic
        fun isAPI17(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

        @JvmStatic
        fun isAPI21(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

        @JvmStatic
        fun wrap(o: Any?): Any? {
            if (o == null) {
                return NULL
            }
            if (o is JSONArray || o is JSONObject) {
                return o
            }
            if (o == NULL) {
                return o
            }
            try {
                if (o is Collection<*>) {
                    return JSONArray(o)
                } else if (o.javaClass.isArray) {
                    val jsonArray = JSONArray()
                    val length = JavaArray.getLength(o)
                    for (i in 0 until length) {
                        jsonArray.put(wrap(JavaArray.get(o, i)))
                    }
                    return jsonArray
                }
                if (o is Map<*, *>) {
                    return JSONObject(o)
                }
                if (o is Boolean ||
                    o is Byte ||
                    o is Char ||
                    o is Double ||
                    o is Float ||
                    o is Int ||
                    o is Long ||
                    o is Short ||
                    o is String
                ) {
                    return o
                }
                if (o.javaClass.`package`?.name?.startsWith("java.") == true) {
                    return o.toString()
                }
            } catch (ignored: Exception) {
            }
            return null
        }

        @JvmStatic
        @Throws(JSONException::class)
        fun newJSONArray(array: Any): JSONArray {
            var result: JSONArray
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    result = JSONArray(array)
                } catch (e: JSONException) {
                    Log.e(TAG, "newJSONArray: $e")
                    throw e
                }
            } else {
                if (!array.javaClass.isArray) {
                    throw JSONException("Not a primitive array: " + array.javaClass)
                }
                result = JSONArray()
                val length = JavaArray.getLength(array)
                for (i in 0 until length) {
                    result.put(wrap(JavaArray.get(array, i)))
                }
            }
            return result
        }
    }

    class CheckNetworkConnection {
        companion object {
            @JvmStatic
            fun isNetworkConnectionAvailable(context: Context): Boolean {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                if (connectivityManager != null) {
                    val netInfo = connectivityManager.activeNetworkInfo
                    if (netInfo != null && netInfo.isConnected && netInfo.isConnectedOrConnecting && netInfo.isAvailable) {
                        return true
                    }
                }
                return false
            }

            @JvmStatic
            fun isNetworkConnectedOrConnecting(context: Context): Boolean {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                if (connectivityManager != null) {
                    val netInfo = connectivityManager.activeNetworkInfo
                    if (netInfo != null && netInfo.isConnected && netInfo.isConnectedOrConnecting) {
                        return true
                    }
                }
                return false
            }
        }
    }

    class Serializer {
        companion object {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @JvmStatic
            @Throws(IOException::class)
            fun serialize_(obj: Any): ByteArray {
                ByteArrayOutputStream().use { b ->
                    ObjectOutputStream(b).use { o ->
                        o.writeObject(obj)
                    }
                    return b.toByteArray()
                }
            }

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @JvmStatic
            @Throws(IOException::class, ClassNotFoundException::class)
            fun deserialize_(bytes: ByteArray): Any {
                ByteArrayInputStream(bytes).use { b ->
                    ObjectInputStream(b).use { o ->
                        return o.readObject()
                    }
                }
            }

            @Synchronized
            @JvmStatic
            @Throws(IOException::class, ClassNotFoundException::class)
            fun deserialize(bytes: ByteArray): Any? {
                val bis = ByteArrayInputStream(bytes)
                var `in`: ObjectInput? = null
                val o: Any?
                try {
                    `in` = ObjectInputStream(bis)
                    o = `in`.readObject()
                } finally {
                    try {
                        bis.close()
                    } catch (ignored: IOException) {
                    }
                    try {
                        `in`?.close()
                    } catch (ignored: IOException) {
                    }
                }
                return o
            }

            @Synchronized
            @JvmStatic
            @Throws(IOException::class)
            fun serialize(obj: Any): ByteArray? {
                val bos = ByteArrayOutputStream()
                var out: ObjectOutput? = null
                val bytes: ByteArray?
                try {
                    out = ObjectOutputStream(bos)
                    out.writeObject(obj)
                    bytes = bos.toByteArray()
                } finally {
                    try {
                        out?.close()
                    } catch (ignored: IOException) {
                    }
                    try {
                        bos.close()
                    } catch (ignored: IOException) {
                    }
                }
                return bytes
            }
        }
    }

    private inner class DownloadImageTask(var bmImage: ImageView) : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            val urldisplay = urls[0]
            var mIcon11: Bitmap? = null
            try {
                val `in` = URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message ?: "")
                e.printStackTrace()
            }
            return mIcon11
        }

        override fun onPostExecute(result: Bitmap?) {
            bmImage.setImageBitmap(result)
        }
    }

    private inner class DownloadTask(private val context: Context) : AsyncTask<String, Int, String>() {
        private val mWakeLock: PowerManager.WakeLock? = null

        override fun doInBackground(vararg sUrl: String): String? {
            var input: InputStream? = null
            var output: OutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                val url = URL(sUrl[0])
                connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP ${connection.responseCode} ${connection.responseMessage}"
                }

                val fileLength = connection.contentLength
                input = connection.inputStream
                output = FileOutputStream("/sdcard/file_name.extension")

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    if (isCancelled) {
                        input.close()
                        return null
                    }
                    total += count.toLong()
                    if (fileLength > 0) {
                        publishProgress((total * 100 / fileLength).toInt())
                    }
                    output.write(data, 0, count)
                }
            } catch (e: Exception) {
                return e.toString()
            } finally {
                try {
                    output?.close()
                    input?.close()
                } catch (ignored: IOException) {
                }
                connection?.disconnect()
            }
            return null
        }
    }
}
