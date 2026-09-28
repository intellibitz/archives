package intellibitz.intellidroid.content

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.db.DatabaseHelper
import intellibitz.intellidroid.service.ContactService
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.HashSet

/**
 *
 */
class UserContentProvider : ContentProvider() {

    companion object {
        const val TAG = "UserCP"
        const val TABLE_USERS = "user"
        //    1 to many JOINS
        const val TABLE_USERS_EMAILS_JOIN = "users_emails"
        const val CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + ContactItemColumns.TABLE_CONTACTS_SCHEMA

        //    The content provider scheme
        const val SCHEME = "content://"
        const val AUTHORITY = "intellibitz.intellidroid.content.UserContentProvider"
        val CONTENT_URI: Uri = Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_USERS)

        // MIME types used for searching words or looking up a single definition
        const val USERS_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/all"
        const val USERS_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/_id"
        const val USERS_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.intellibitz.android.intellibitzdb/id"
        // UriMatcher stuff
        private const val USERS_DIR_TYPE = 0
        private const val USERS_ITEM_TYPE = 1
        private const val USERS_DATA_ITEM_TYPE = 2
        private const val SEARCH_SUGGEST = 3
        private const val REFRESH_SHORTCUT = 4
        private val sURIMatcher: UriMatcher = buildUriMatcher()

        /**
         * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
         */
        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            // to get definitions...
            matcher.addURI(AUTHORITY, TABLE_USERS, USERS_DIR_TYPE)
            matcher.addURI(AUTHORITY, "$TABLE_USERS/#", USERS_ITEM_TYPE)
            matcher.addURI(AUTHORITY, "$TABLE_USERS/*", USERS_DATA_ITEM_TYPE)
            // to get suggestions...
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST)

            /* The following are unused in this implementation, but if we include
             * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
             * could expect to receive refresh queries when a shortcutted suggestion is displayed in
             * Quick Search Box, in which case, the following Uris would be provided and we
             * would return a cursor with a single item representing the refreshed suggestion data.
             */
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT)
            matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT)
            return matcher
        }

        fun fillContentValuesFromUserItem(userItem: ContactItem, values: ContentValues): ContentValues {
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATA_ID, userItem.dataId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_NAME, userItem.name)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_FIRST_NAME, userItem.firstName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_LAST_NAME, userItem.lastName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PHONES, userItem.mobiles)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_EMAILS, userItem.emails)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_MOBILE, userItem.mobile)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_SIGNUP_EMAIL, userItem.signupEmail)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PWD, userItem.pwd)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_COMPANY_ID, userItem.companyId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_COMPANY_NAME, userItem.companyName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_PIC, userItem.profilePic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_CLOUD_PIC, userItem.cloudPic)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_OTP, userItem.otp)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TOKEN, userItem.token)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_GCM_TOKEN, userItem.gcmToken)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_GCM_TOKEN_SENDTO_CLOUD, userItem.isGcmTokenSentToCloud)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE, userItem.device)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_ID, userItem.deviceId)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_NAME, userItem.deviceName)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DEVICE_REF, userItem.deviceRef)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_IS_INTELLIBITZ, userItem.isIntellibitzContact)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_ISWORK, userItem.isWorkContact)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_TIMESTAMP, userItem.timestamp)
            MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis())
            return values
        }

        fun queryUsers(user: ContactItem, context: Context): ContactItem {
//        populates the first user
//        // TODO: 19-05-2016
//        check for active user and load.. for multi user sign on
            val dataId = user.dataId
            var uri = CONTENT_URI
            var sel: String? = null
            var selArgs: Array<String>? = null
            if (!TextUtils.isEmpty(dataId)) {
                uri = Uri.withAppendedPath(CONTENT_URI, dataId)
                sel = ContactItemColumns.KEY_DATA_ID + " = ? "
                selArgs = arrayOf(dataId)
            }

            val cursor = context.contentResolver.query(uri, null, sel, selArgs, null)
            return fillsUserFromCursor(user, cursor)
        }

        fun fillsUserFromCursor(user: ContactItem, cursor: Cursor?): ContactItem {
            if (null == cursor) return user
            if (cursor.count > 0) {
                user._id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID))
                user.dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID))
                user.device = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE))
                user.deviceId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_ID))
                user.deviceName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_NAME))
                user.deviceRef = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_REF))
                user.token = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TOKEN))
                user.mobiles = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PHONES))
                user.emails = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_EMAILS))
                user.mobile = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_MOBILE))
                user.signupEmail = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_SIGNUP_EMAIL))
                user.pwd = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PWD))
                user.companyId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_COMPANY_ID))
                user.companyName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_COMPANY_NAME))
                user.name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME))
                user.firstName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME))
                user.lastName = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME))
                user.profilePic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC))
                user.cloudPic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC))
                user.otp = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_OTP))
                user.status = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_STATUS))
                user.isIntellibitzContact = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_IS_INTELLIBITZ))
                user.isWorkContact = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_ISWORK))
                user.timestamp = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_TIMESTAMP))
                cursor.close()
            }
            return user
        }

        @Throws(IOException::class)
        fun savesUserInDB(user: ContactItem, context: Context): Uri {
//        values.put(ContactItem.TAG, MainApplicationSingleton.Serializer.serialize(user));
            return context.applicationContext.contentResolver.insert(
                    CONTENT_URI, fillContentValuesFromUserItem(user, ContentValues()))
        }

        fun updateGCMTokenInDB(user: ContactItem, context: Context): Int {
            val contentValues = ContentValues()
            MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_GCM_TOKEN, user.gcmToken)
            contentValues.put(ContactItemColumns.KEY_GCM_TOKEN_SENDTO_CLOUD, user.isGcmTokenSentToCloud)
            return context.applicationContext.contentResolver.update(
                    UserContentProvider.CONTENT_URI,
                    contentValues, ContactItemColumns.KEY_DATA_ID + " = ?",
                    arrayOf(user.dataId))
        }

        fun updatesProfileInDB(user: ContactItem, context: Context): Int {
            val contentValues = ContentValues()
            MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_NAME, user.name)
            MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_STATUS, user.status)
            MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_PIC, user.profilePic)
            MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_CLOUD_PIC, user.cloudPic)
            return context.applicationContext.contentResolver.update(
                    UserContentProvider.CONTENT_URI,
                    contentValues, ContactItemColumns.KEY_DATA_ID + " = ?",
                    arrayOf(user.dataId))
        }

        fun updatesCompanyInDB(user: ContactItem, context: Context): Int {
            val contentValues = ContentValues()
            MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_COMPANY_ID, user.companyId)
            MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_COMPANY_NAME, user.companyName)
            if (contentValues.size() > 0)
                return context.applicationContext.contentResolver.update(
                        UserContentProvider.CONTENT_URI,
                        contentValues, ContactItemColumns.KEY_DATA_ID + " = ?",
                        arrayOf(user.dataId))
            return 0
        }

        fun packUserEmailsFromCursor(cursor: Cursor?, user: ContactItem): ContactItem {
            if (null == cursor) return user
            if (cursor.count > 0) {
                do {
                    val email = cursor.getString(
                            cursor.getColumnIndex(ContactItemColumns.KEY_SIGNUP_EMAIL))
                    if (!TextUtils.isEmpty(email)) {
                        val emid = cursor.getLong(cursor.getColumnIndex("emid"))
                        val name = cursor.getString(cursor.getColumnIndex("emname"))
                        val type = cursor.getString(cursor.getColumnIndex("emtype"))
                        val item = ContactItem(email, email, name, type)
                        item._id = emid
                        user.addEmail(item)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            return user
        }

        fun initUserFromAppContext(user: ContactItem, context: Context): ContactItem {
            //        the bundle will be null - the first time the activity is brought up
//            // TODO: 17-03-2016
//            retrieves user from db
//            for now, get it from preferences
//        user = new ContactItem();
//            device is always ANDROID
            user.device = MainApplicationSingleton.DEVICE_ID
            user.dataId = MainApplicationSingleton.getInstance(
                    context.applicationContext).getStringValueSP(MainApplicationSingleton.UID_PARAM)
            return user
        }

        @Throws(CloneNotSupportedException::class)
        fun getUserCloneForService(user: ContactItem): ContactItem {
            val userForService = user.clone() as ContactItem
            return userForService
        }

        /**
         * Creating User
         */
        fun createOrUpdateUser(databaseHelper: DatabaseHelper, item: ContactItem): Long {
            val cursor = databaseHelper.query(UserContentProvider.TABLE_USERS,
                    null,
                    ContactItemColumns.KEY_DATA_ID + " = ? ",
                    arrayOf(item.dataId),
                    null)
            if (null == cursor || 0 == cursor.count) {
                if (cursor != null) cursor.close()
                val _id = databaseHelper.insert(UserContentProvider.TABLE_USERS, null,
                        UserContentProvider.fillContentValuesFromUserItem(item, ContentValues()))
                item._id = _id
            } else {
                cursor.close()
                val _id = databaseHelper.update(UserContentProvider.TABLE_USERS,
                        UserContentProvider.fillContentValuesFromUserItem(item, ContentValues()),
                        ContactItemColumns.KEY_DATA_ID + " = ?",
                        arrayOf(item.dataId))
                item._id = _id
            }
            return item._id
        }

        fun savesUserInDBPlusSP(user: ContactItem, context: Context) {
//                    saves user to local db
            try {
                val uri = savesUserInDB(user, context)
                val id = ContentUris.parseId(uri)
                MainApplicationSingleton.getInstance(context).putLongValueSP(
                        MainApplicationSingleton.ID_PARAM, id)
//                set the db id
                user._id = id
                Log.e(TAG, "SUCCESS - User insert: $uri")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            savesUserInSP(user, context)
            Log.d(TAG, "User: $user")


//                // TODO: 05-03-2016
//                remove this hack.. do in async, or a service
/*
            HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
            mHandlerThread.start();
            Handler mHandler = new Handler(mHandlerThread.getLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
*/
/*
            if (null == user.getName() || user.getName().isEmpty()) {
//                    fetch user from cloud, if not available local

                HashMap<String, String> data = new HashMap<>();
                MainApplicationSingleton mainApplication =
                        MainApplicationSingleton.getInstance();
                data.put(MainApplicationSingleton.DEVICE_PARAM, user.getDevice());
                data.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
                data.put(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
                // params comes from the execute() call: params[0] is the url.
                JSONObject response = HttpUrlConnectionParser.postHTTP(
                        MainApplicationSingleton.AUTH_GET_PROFILE, data);
                if (response != null) {
                    try {
//                                    // TODO: 07-03-2016
//                                    handle error
//                                String err = response.getString("err");
//                                String mob = response.getString("mobile");
                        user.setName(response.getString("name"));
                        user.setStatus(response.getString("status"));
                        user.setProfilePic(response.getString("profile_pic"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mainApplication.putStringValueSP(MainApplicationSingleton.NAME_PARAM,
                        user.getName());
                mainApplication.putStringValueSP(MainApplicationSingleton.STATUS_PARAM,
                        user.getStatus());
                mainApplication.putStringValueSP(MainApplicationSingleton.PROFILE_PIC_PARAM,
                        user.getProfilePic());
*/

//                        // TODO: 07-03-2016
//                        The user restore from cloud, to be handled clean
//                        sync previous email accounts from cloud
//                        EmailSyncHandler.SyncEmailsFromCloud();

//                    }
        }

        fun savesUserInSP(user: ContactItem, context: Context) {
            //            stores user in preferences
//            // TODO: 18-03-2016
//            to store all info in db, except for the active user id
            val mainApplication = MainApplicationSingleton.getInstance(context)
            mainApplication.uidCurrentUser = user.dataId

//            set the user id, multi profiles can be supported.. do this first
            mainApplication.putStringValueSP(
                    MainApplicationSingleton.UID_USER_LOGGED_IN_PARAM,
                    user.dataId, false)
            mainApplication.putStringValueSP(MainApplicationSingleton.UID_PARAM,
                    user.dataId)
            mainApplication.putStringValueSP(MainApplicationSingleton.TOKEN_PARAM,
                    user.token)
            mainApplication.putStringValueSP(MainApplicationSingleton.NAME_PARAM,
                    user.name)
            mainApplication.putStringValueSP(MainApplicationSingleton.MOBILE_PARAM,
                    user.mobile)
            mainApplication.putStringValueSP(MainApplicationSingleton.EMAIL_PARAM,
                    user.signupEmail)
            mainApplication.putStringValueSP(MainApplicationSingleton.PWD_PARAM,
                    user.pwd)
            mainApplication.putStringValueSP(MainApplicationSingleton.COMPANY_ID_PARAM,
                    user.companyId)
            mainApplication.putStringValueSP(MainApplicationSingleton.COMPANY_NAME_PARAM,
                    user.companyName)
            mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_PARAM,
                    user.device)
            mainApplication.putStringValueSP(MainApplicationSingleton.OTP_PARAM,
                    user.otp)
            mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_NAME_PARAM,
                    user.deviceName)
            mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_ID_PARAM,
                    user.deviceId)
            mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_REF_PARAM,
                    user.deviceRef)
        }

        fun setsDeviceIdNameInfo(user: ContactItem, context: Context) {
            user.deviceId = Build.SERIAL
            val androidId = Settings.Secure.getString(
                    context.contentResolver, Settings.Secure.ANDROID_ID)
            if (null == user.deviceId) {
                if (IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(context)) {
                    val deviceId = (context.getSystemService(
                            Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
                    user.deviceId = deviceId
                }
                if (null == user.deviceId) {
                    user.deviceId = androidId
                }
            }
            user.deviceName = Build.MODEL
            if (null == user.deviceName) {
                user.deviceName = Build.MANUFACTURER + Build.PRODUCT
                if (null == user.deviceName) {
                    user.deviceName = Build.BRAND + Build.ID
                }
            }
            if (null == user.deviceId || null == user.deviceName) {
                Log.e(MainApplicationSingleton.TAG, "setsDeviceIdNameInfo: User device id or device name is NULL" +
                        user.deviceId + " " + user.deviceName)
            }
        }

        @Throws(JSONException::class)
        fun activateUserInDB(response: JSONObject, user: ContactItem, context: Context) {
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string"
}*/
            parseLoginResponse(response, user.email, user)
//                    SUCCESS
//        UserContentProvider.savesUserInDBPlusSP(user, getContext());
//                startAddEmailActivity();
//        okActivity();
//        syncs contacts from the device to the cloud..right away
            ContactService.asyncUpdateContacts(user, context)
//                    stores user in db..
            savesUserInDBPlusSP(user, context)
//                    user already exists, if email exists in cloud.. get them
/*
        if (1 == user.getAccountExists()) {
//                    fetch emails async from cloud
            UserEmailIntentService.asyncEmailsFromCloudAndSavesInDb(user, getContext());
        }
*/
/*
        if (profileTopicListener != null)
            profileTopicListener.onProfileTopicClicked(user);
*/
//                startProfileInfoActivity();
        }

        @Throws(JSONException::class)
        fun activateUserSignupInDB(response: JSONObject, user: ContactItem, context: Context) {
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string"
}*/
            parseLoginResponse(response, user.email, user)
//                    SUCCESS
//        UserContentProvider.savesUserInDBPlusSP(user, getContext());
//                startAddEmailActivity();
//        okActivity();
//        syncs contacts from the device to the cloud..right away
            ContactService.asyncUpdateContacts(user, context)
//                    stores user in db..
            savesUserInDBPlusSP(user, context)
//                    user already exists, if email exists in cloud.. get them
/*
        if (1 == user.getAccountExists()) {
//                    fetch emails async from cloud
            UserEmailIntentService.asyncEmailsFromCloudAndSavesInDb(user, getContext());
        }
*/
/*
        if (profileTopicListener != null)
            profileTopicListener.onProfileTopicClicked(user);
*/
//                startProfileInfoActivity();
        }

        @Throws(JSONException::class)
        fun parseLoginResponse(response: JSONObject, email: String, user: ContactItem) {
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string"
}*/
            val uid = response.optString(MainApplicationSingleton.UID_PARAM)
            val token = response.optString(MainApplicationSingleton.TOKEN_PARAM)
            val deviceRef = response.optString(MainApplicationSingleton.DEVICE_REF_PARAM)
            val existingDevices = response.optInt("existing_devices")
            val existingWebsessions = response.optInt("existing_websessions")
            user.isIntellibitzContact = existingDevices
            user.isWorkContact = existingWebsessions
            if (!TextUtils.isEmpty(uid))
                user.dataId = uid
            if (!TextUtils.isEmpty(token))
                user.token = token
            if (!TextUtils.isEmpty(deviceRef))
                user.deviceRef = deviceRef
            if (!TextUtils.isEmpty(email))
                user.signupEmail = email
        }

        @Throws(JSONException::class)
        fun parseGetProfileResponse(response: JSONObject, user: ContactItem) {
            val name = response.optString(MainApplicationSingleton.NAME_PARAM)
            val status = response.optString(MainApplicationSingleton.STATUS_PARAM)
            val pic = response.optString(MainApplicationSingleton.PROFILE_PIC_PARAM)
            if (!TextUtils.isEmpty(name)) {
                user.name = name
                user.displayName = name
                user.firstName = name
                user.lastName = name
            }
            if (!TextUtils.isEmpty(status))
                user.status = status
            if (!TextUtils.isEmpty(pic)) {
                user.cloudPic = pic
                user.profilePic = pic
            }
        }

        fun setsEmailsFromJSONArray(emails: JSONArray?, context: Context): Set<ContactItem>? {
            if (null == emails) {
                return null
            }
            val singleton = MainApplicationSingleton.getInstance(context)
            singleton.clearStringSetValueSP("email")
            val size = emails.length()
            if (0 == size) return null
            val items = HashSet<ContactItem>(size)
            for (i in 0 until size) {
                try {
                    val jsonObject = emails.getJSONObject(i)
                    if (null != jsonObject) {
                        val email = jsonObject.getString("email")
                        val type = jsonObject.optString("type")
                        if (!TextUtils.isEmpty(email)) {
                            singleton.addStringSetValueSP(MainApplicationSingleton.EMAIL_PARAM, email)
                            items.add(ContactItem(email, email, email, type))
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    Log.e(TAG, "setsEmailsFromJSONArray: $e")
                }
            }
            return items
        }

        fun newActiveUserFromDB(context: Context): ContactItem {
            val user = ContactItem()
            initUserFromAppContext(user, context)
            queryUsers(user, context)
            if (!TextUtils.isEmpty(user.dataId)) {
                UserEmailContentProvider.populateUserEmailsJoinByDataId(user, context)
            }
            return user
        }

        fun newActiveUserForGCMFromDB(context: Context): ContactItem {
            val user = ContactItem()
            initUserFromAppContext(user, context)
            queryUsers(user, context)
            return user
        }
    }

    private var databaseHelper: DatabaseHelper? = null

    override fun onCreate(): Boolean {
        databaseHelper = DatabaseHelper.newInstance(context, DatabaseHelper.DATABASE_NAME)
        return true
    }

    @Nullable
    override fun getType(uri: Uri): String? {
        return when (sURIMatcher.match(uri)) {
            USERS_DIR_TYPE -> USERS_DIR_MIME_TYPE
            USERS_ITEM_TYPE -> USERS_ITEM_MIME_TYPE
            USERS_DATA_ITEM_TYPE -> USERS_DATA_ITEM_MIME_TYPE
            SEARCH_SUGGEST -> SearchManager.SUGGEST_MIME_TYPE
            REFRESH_SHORTCUT -> SearchManager.SHORTCUT_MIME_TYPE
            else -> throw IllegalArgumentException("Unknown URL $uri")
        }
    }

    @Nullable
    override fun query(@NonNull uri: Uri, projection: Array<String>?, selection: String?,
                        selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var cursor: Cursor? = null
        when (sURIMatcher.match(uri)) {
            USERS_DIR_TYPE -> try {
                cursor = databaseHelper?.query(TABLE_USERS,
                        projection, selection, selectionArgs, sortOrder)
                if (null != cursor) {
                    val context = context
                    if (null != context) {
                        cursor.setNotificationUri(context.contentResolver, uri)
                    }
                }
                return cursor
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            USERS_ITEM_TYPE -> try {
                val id = ContentUris.parseId(uri)
                if (id > 0)
                    cursor = databaseHelper?.query(TABLE_USERS,
                            projection,
                            ContactItemColumns.KEY_ID + " = ? ",
                            arrayOf(id.toString()), sortOrder)
                if (null != cursor) {
                    val context = context
                    if (null != context) {
                        cursor.setNotificationUri(context.contentResolver, uri)
                    }
                }
                return cursor
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            USERS_DATA_ITEM_TYPE -> try {
                val did = uri.lastPathSegment
                if (did != null && did.isNotEmpty())
                    cursor = databaseHelper?.query(TABLE_USERS,
                            projection,
                            ContactItemColumns.KEY_DATA_ID + " = ? ",
                            arrayOf(did), sortOrder)
                if (null != cursor) {
                    val context = context
                    if (null != context) {
                        cursor.setNotificationUri(context.contentResolver, uri)
                    }
                }
                return cursor
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return cursor
    }

    @Nullable
    override fun insert(@NonNull uri: Uri, values: ContentValues?): Uri? {
        when (sURIMatcher.match(uri)) {
            USERS_DIR_TYPE, USERS_ITEM_TYPE -> try {
                val id = databaseHelper?.insert(TABLE_USERS, null, values) ?: return uri
                val insertUri = ContentUris.withAppendedId(UserContentProvider.CONTENT_URI, id)
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(insertUri, null)
                }
                return insertUri
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return uri
    }

    override fun update(@NonNull uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        when (sURIMatcher.match(uri)) {
            USERS_DIR_TYPE, USERS_ITEM_TYPE -> try {
                val id = databaseHelper?.update(TABLE_USERS,
                        values, selection, selectionArgs) ?: return 0
                val insertUri = ContentUris.withAppendedId(UserContentProvider.CONTENT_URI, id.toLong())
                val context = context
                if (null != context) {
                    context.contentResolver.notifyChange(insertUri, null)
                }
                return id
            } catch (e: SQLException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        return 0
    }

    override fun delete(@NonNull uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    /**
     * A test package can call this to get a handle to the database underlying NotePadProvider,
     * so it can insert test data into the database. The test case class is responsible for
     * instantiating the provider in a test context; {@link android.test.ProviderTestCase2} does
     * this during the call to setUp()
     *
     * @return a handle to the database helper object for the provider's data.
     */
    fun getOpenHelperForTest(): DatabaseHelper? {
        return databaseHelper
    }

}
