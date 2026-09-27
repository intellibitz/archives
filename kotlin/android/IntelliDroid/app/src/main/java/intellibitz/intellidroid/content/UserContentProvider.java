package intellibitz.intellidroid.content;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.service.ContactService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *
 */
public class UserContentProvider extends
        ContentProvider {

    public static final String TAG = "UserCP";
    public static final String TABLE_USERS = "user";
    //    1 to many JOINS
    public static final String TABLE_USERS_EMAILS_JOIN = "users_emails";
    public static final String CREATE_TABLE_USERS = "CREATE TABLE "
            + TABLE_USERS + ContactItemColumns.TABLE_CONTACTS_SCHEMA;

    //    The content provider scheme
    public static final String SCHEME = "content://";
    public static final String AUTHORITY = "intellibitz.intellidroid.content.UserContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + TABLE_USERS);

    // MIME types used for searching words or looking up a single definition
    public static final String USERS_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String USERS_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String USERS_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/id";
    // UriMatcher stuff
    private static final int USERS_DIR_TYPE = 0;
    private static final int USERS_ITEM_TYPE = 1;
    private static final int USERS_DATA_ITEM_TYPE = 2;
    private static final int SEARCH_SUGGEST = 3;
    private static final int REFRESH_SHORTCUT = 4;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_USERS, USERS_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_USERS + "/#", USERS_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_USERS + "/*", USERS_DATA_ITEM_TYPE);
        // to get suggestions...
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        /* The following are unused in this implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
         * could expect to receive refresh queries when a shortcutted suggestion is displayed in
         * Quick Search Box, in which case, the following Uris would be provided and we
         * would return a cursor with a single item representing the refreshed suggestion data.
         */
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
        return matcher;
    }

    public static ContentValues fillContentValuesFromUserItem(ContactItem userItem, ContentValues values) {
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DATA_ID, userItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_NAME, userItem.getName());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_FIRST_NAME, userItem.getFirstName());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_LAST_NAME, userItem.getLastName());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_PHONES, userItem.getMobiles());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_EMAILS, userItem.getEmails());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_MOBILE, userItem.getMobile());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_SIGNUP_EMAIL, userItem.getSignupEmail());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_PWD, userItem.getPwd());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_COMPANY_ID, userItem.getCompanyId());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_COMPANY_NAME, userItem.getCompanyName());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_PIC, userItem.getProfilePic());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_CLOUD_PIC, userItem.getCloudPic());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_OTP, userItem.getOtp());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_TOKEN, userItem.getToken());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_GCM_TOKEN, userItem.getGcmToken());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_GCM_TOKEN_SENDTO_CLOUD, userItem.isGcmTokenSentToCloud());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DEVICE, userItem.getDevice());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DEVICE_ID, userItem.getDeviceId());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DEVICE_NAME, userItem.getDeviceName());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DEVICE_REF, userItem.getDeviceRef());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_IS_INTELLIBITZ, userItem.getIsIntellibitzContact());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_ISWORK, userItem.getIsWorkContact());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_TIMESTAMP, userItem.getTimestamp());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis());
        return values;
    }

    public static ContactItem queryUsers(ContactItem user, Context context) {
//        populates the first user
//        // TODO: 19-05-2016
//        check for active user and load.. for multi user sign on
        String dataId = user.getDataId();
        Uri uri = CONTENT_URI;
        String sel = null;
        String[] selArgs = null;
        if (!TextUtils.isEmpty(dataId)) {
            uri = Uri.withAppendedPath(CONTENT_URI, dataId);
            sel = ContactItemColumns.KEY_DATA_ID + " = ? ";
            selArgs = new String[]{dataId};
        }

        Cursor cursor = context.getContentResolver().query(uri, null, sel, selArgs, null);
        return fillsUserFromCursor(user, cursor);
    }

    public static ContactItem fillsUserFromCursor(ContactItem user, Cursor cursor) {
        if (null == cursor) return user;
        if (cursor.getCount() > 0) {
            user.set_id(cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID)));
            user.setDataId(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID)));
            user.setDevice(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE)));
            user.setDeviceId(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_ID)));
            user.setDeviceName(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_NAME)));
            user.setDeviceRef(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_REF)));
            user.setToken(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_TOKEN)));
            user.setMobiles(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PHONES)));
            user.setEmails(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_EMAILS)));
            user.setMobile(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_MOBILE)));
            user.setSignupEmail(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_SIGNUP_EMAIL)));
            user.setPwd(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PWD)));
            user.setCompanyId(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_COMPANY_ID)));
            user.setCompanyName(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_COMPANY_NAME)));
            user.setName(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME)));
            user.setFirstName(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME)));
            user.setLastName(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME)));
            user.setProfilePic(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC)));
            user.setCloudPic(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC)));
            user.setOtp(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_OTP)));
            user.setStatus(cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_STATUS)));
            user.setIsIntellibitzContact(cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_IS_INTELLIBITZ)));
            user.setIsWorkContact(cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_ISWORK)));
            user.setTimestamp(cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_TIMESTAMP)));
            cursor.close();
        }
        return user;
    }

    public static Uri savesUserInDB(ContactItem user, Context context) throws IOException {
//        values.put(ContactItem.TAG, MainApplicationSingleton.Serializer.serialize(user));
        return context.getApplicationContext().getContentResolver().insert(
                CONTENT_URI, fillContentValuesFromUserItem(user, new ContentValues()));
    }

    public static int updateGCMTokenInDB(ContactItem user, Context context) {
        ContentValues contentValues = new ContentValues();
        MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_GCM_TOKEN, user.getGcmToken());
        contentValues.put(ContactItemColumns.KEY_GCM_TOKEN_SENDTO_CLOUD, user.isGcmTokenSentToCloud());
        return context.getApplicationContext().getContentResolver().update(
                UserContentProvider.CONTENT_URI,
                contentValues, ContactItemColumns.KEY_DATA_ID + " = ?",
                new String[]{user.getDataId()});
    }

    public static int updatesProfileInDB(ContactItem user, Context context) {
        ContentValues contentValues = new ContentValues();
        MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_NAME, user.getName());
        MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_STATUS, user.getStatus());
        MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_PIC, user.getProfilePic());
        MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_CLOUD_PIC, user.getCloudPic());
        return context.getApplicationContext().getContentResolver().update(
                UserContentProvider.CONTENT_URI,
                contentValues, ContactItemColumns.KEY_DATA_ID + " = ?",
                new String[]{user.getDataId()});
    }

    public static int updatesCompanyInDB(ContactItem user, Context context) {
        ContentValues contentValues = new ContentValues();
        MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_COMPANY_ID, user.getCompanyId());
        MainApplicationSingleton.fillIfNotNull(contentValues, ContactItemColumns.KEY_COMPANY_NAME, user.getCompanyName());
        if (contentValues.size() > 0)
            return context.getApplicationContext().getContentResolver().update(
                    UserContentProvider.CONTENT_URI,
                    contentValues, ContactItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{user.getDataId()});
        return 0;
    }

    public static ContactItem packUserEmailsFromCursor(Cursor cursor, ContactItem user) {
        if (null == cursor) return user;
        if (cursor.getCount() > 0) {
            do {
                String email = cursor.getString(
                        cursor.getColumnIndex(ContactItemColumns.KEY_SIGNUP_EMAIL));
                if (!TextUtils.isEmpty(email)) {
                    long emid = cursor.getLong(cursor.getColumnIndex("emid"));
                    String name = cursor.getString(cursor.getColumnIndex("emname"));
                    String type = cursor.getString(cursor.getColumnIndex("emtype"));
                    ContactItem item = new ContactItem(email, email, name, type);
                    item.set_id(emid);
                    user.addEmail(item);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return user;
    }

    public static ContactItem initUserFromAppContext(ContactItem user, Context context) {
        //        the bundle will be null - the first time the activity is brought up
//            // TODO: 17-03-2016
//            retrieves user from db
//            for now, get it from preferences
//        user = new ContactItem();
//            device is always ANDROID
        user.setDevice(MainApplicationSingleton.DEVICE_ID);
        user.setDataId(MainApplicationSingleton.getInstance(
                context.getApplicationContext()).getStringValueSP(MainApplicationSingleton.UID_PARAM));
        return user;
    }

    public static ContactItem getUserCloneForService(ContactItem user) throws CloneNotSupportedException {
        ContactItem userForService = (ContactItem) user.clone();
        return userForService;
    }

    /**
     * Creating User
     */
    public static long createOrUpdateUser(DatabaseHelper databaseHelper, ContactItem item) {
        Cursor cursor = databaseHelper.query(UserContentProvider.TABLE_USERS,
                null,
                ContactItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{item.getDataId()},
                null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = databaseHelper.insert(UserContentProvider.TABLE_USERS, null,
                    UserContentProvider.fillContentValuesFromUserItem(item, new ContentValues()));
            item.set_id(_id);
        } else {
            cursor.close();
            long _id = databaseHelper.update(UserContentProvider.TABLE_USERS,
                    UserContentProvider.fillContentValuesFromUserItem(item, new ContentValues()),
                    ContactItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(item.getDataId())});
            item.set_id(_id);
        }
        return item.get_id();
    }

    public static void savesUserInDBPlusSP(ContactItem user, Context context) {
//                    saves user to local db
        try {
            Uri uri = savesUserInDB(user, context);
            long id = ContentUris.parseId(uri);
            MainApplicationSingleton.getInstance(context).putLongValueSP(
                    MainApplicationSingleton.ID_PARAM, id);
//                set the db id
            user.set_id(id);
            Log.e(TAG, "SUCCESS - User insert: " + uri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        savesUserInSP(user, context);
        Log.d(TAG, "User: " + user);


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

    public static void savesUserInSP(ContactItem user, Context context) {
        //            stores user in preferences
//            // TODO: 18-03-2016
//            to store all info in db, except for the active user id
        final MainApplicationSingleton mainApplication =
                MainApplicationSingleton.getInstance(context);
        mainApplication.setUidCurrentUser(user.getDataId());

//            set the user id, multi profiles can be supported.. do this first
        mainApplication.putStringValueSP(
                MainApplicationSingleton.UID_USER_LOGGED_IN_PARAM,
                user.getDataId(), false);
        mainApplication.putStringValueSP(MainApplicationSingleton.UID_PARAM,
                user.getDataId());
        mainApplication.putStringValueSP(MainApplicationSingleton.TOKEN_PARAM,
                user.getToken());
        mainApplication.putStringValueSP(MainApplicationSingleton.NAME_PARAM,
                user.getName());
        mainApplication.putStringValueSP(MainApplicationSingleton.MOBILE_PARAM,
                user.getMobile());
        mainApplication.putStringValueSP(MainApplicationSingleton.EMAIL_PARAM,
                user.getSignupEmail());
        mainApplication.putStringValueSP(MainApplicationSingleton.PWD_PARAM,
                user.getPwd());
        mainApplication.putStringValueSP(MainApplicationSingleton.COMPANY_ID_PARAM,
                user.getCompanyId());
        mainApplication.putStringValueSP(MainApplicationSingleton.COMPANY_NAME_PARAM,
                user.getCompanyName());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_PARAM,
                user.getDevice());
        mainApplication.putStringValueSP(MainApplicationSingleton.OTP_PARAM,
                user.getOtp());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_NAME_PARAM,
                user.getDeviceName());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_ID_PARAM,
                user.getDeviceId());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_REF_PARAM,
                user.getDeviceRef());
    }

    public static void setsDeviceIdNameInfo(ContactItem user, Context context) {
        user.setDeviceId(Build.SERIAL);
        final String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (null == user.getDeviceId()) {
            if (IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(context)) {
                final String deviceId = ((TelephonyManager) context.getSystemService(
                        Context.TELEPHONY_SERVICE)).getDeviceId();
                user.setDeviceId(deviceId);
            }
            if (null == user.getDeviceId()) {
                user.setDeviceId(androidId);
            }
        }
        user.setDeviceName(Build.MODEL);
        if (null == user.getDeviceName()) {
            user.setDeviceName(Build.MANUFACTURER + Build.PRODUCT);
            if (null == user.getDeviceName()) {
                user.setDeviceName(Build.BRAND + Build.ID);
            }
        }
        if (null == user.getDeviceId() || null == user.getDeviceName()) {
            Log.e(MainApplicationSingleton.TAG, "setsDeviceIdNameInfo: User device id or device name is NULL" +
                    user.getDeviceId() + " " + user.getDeviceName());
        }
    }

    public static void activateUserInDB(JSONObject response, ContactItem user, Context context) throws JSONException {
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string"
}*/
        parseLoginResponse(response, user.getEmail(), user);
//                    SUCCESS
//        UserContentProvider.savesUserInDBPlusSP(user, getContext());
//                startAddEmailActivity();
//        okActivity();
//        syncs contacts from the device to the cloud..right away
        ContactService.asyncUpdateContacts(user, context);
//                    stores user in db..
        savesUserInDBPlusSP(user, context);
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

    public static void activateUserSignupInDB(JSONObject response, ContactItem user, Context context) throws JSONException {
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string"
}*/
        parseLoginResponse(response, user.getEmail(), user);
//                    SUCCESS
//        UserContentProvider.savesUserInDBPlusSP(user, getContext());
//                startAddEmailActivity();
//        okActivity();
//        syncs contacts from the device to the cloud..right away
        ContactService.asyncUpdateContacts(user, context);
//                    stores user in db..
        savesUserInDBPlusSP(user, context);
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

    public static void parseLoginResponse(
            JSONObject response, String email, ContactItem user) throws JSONException {
/*
{
  "status": 0,
  "err": "string",
  "uid": "string",
  "token": "string",
  "device_ref": "string"
}*/
        String uid = response.optString(MainApplicationSingleton.UID_PARAM);
        String token = response.optString(MainApplicationSingleton.TOKEN_PARAM);
        String deviceRef = response.optString(MainApplicationSingleton.DEVICE_REF_PARAM);
        int existingDevices = response.optInt("existing_devices");
        int existingWebsessions = response.optInt("existing_websessions");
        user.setIsIntellibitzContact(existingDevices);
        user.setIsWorkContact(existingWebsessions);
        if (!TextUtils.isEmpty(uid))
            user.setDataId(uid);
        if (!TextUtils.isEmpty(token))
            user.setToken(token);
        if (!TextUtils.isEmpty(deviceRef))
            user.setDeviceRef(deviceRef);
        if (!TextUtils.isEmpty(email))
            user.setSignupEmail(email);
    }

    public static void parseGetProfileResponse(
            JSONObject response, ContactItem user) throws JSONException {
        String name = response.optString(MainApplicationSingleton.NAME_PARAM);
        String status = response.optString(MainApplicationSingleton.STATUS_PARAM);
        String pic = response.optString(MainApplicationSingleton.PROFILE_PIC_PARAM);
        if (!TextUtils.isEmpty(name)) {
            user.setName(name);
            user.setDisplayName(name);
            user.setFirstName(name);
            user.setLastName(name);
        }
        if (!TextUtils.isEmpty(status))
            user.setStatus(status);
        if (!TextUtils.isEmpty(pic)) {
            user.setCloudPic(pic);
            user.setProfilePic(pic);
        }
    }

    public static Set<ContactItem> setsEmailsFromJSONArray(JSONArray emails, Context context) {
        if (null == emails) {
            return null;
        }
        MainApplicationSingleton singleton = MainApplicationSingleton.getInstance(context);
        singleton.clearStringSetValueSP("email");
        int size = emails.length();
        if (0 == size) return null;
        Set<ContactItem> items = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            try {
                JSONObject jsonObject = emails.getJSONObject(i);
                if (null != jsonObject) {
                    String email = jsonObject.getString("email");
                    String type = jsonObject.optString("type");
                    if (!TextUtils.isEmpty(email)) {
                        singleton.addStringSetValueSP(MainApplicationSingleton.EMAIL_PARAM, email);
                        items.add(new ContactItem(email, email, email, type));
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, "setsEmailsFromJSONArray: " + e);
            }
        }
        return items;
    }

    public static ContactItem newActiveUserFromDB(Context context) {
        ContactItem user = new ContactItem();
        initUserFromAppContext(user, context);
        queryUsers(user, context);
        if (!TextUtils.isEmpty(user.getDataId())) {
            UserEmailContentProvider.populateUserEmailsJoinByDataId(user, context);
        }
        return user;
    }

    public static ContactItem newActiveUserForGCMFromDB(Context context) {
        ContactItem user = new ContactItem();
        initUserFromAppContext(user, context);
        queryUsers(user, context);
        return user;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = DatabaseHelper.newInstance(getContext(),
                DatabaseHelper.DATABASE_NAME);
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case USERS_DIR_TYPE:
                return USERS_DIR_MIME_TYPE;
            case USERS_ITEM_TYPE:
                return USERS_ITEM_MIME_TYPE;
            case USERS_DATA_ITEM_TYPE:
                return USERS_DATA_ITEM_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (sURIMatcher.match(uri)) {
            case USERS_DIR_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_USERS,
                            projection, selection, selectionArgs, sortOrder);
                    if (null != cursor) {
                        Context context = getContext();
                        if (null != context) {
                            cursor.setNotificationUri(context.getContentResolver(), uri);
                        }
                    }
                    return cursor;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case USERS_ITEM_TYPE:
                try {
                    long id = ContentUris.parseId(uri);
                    if (id > 0)
                        cursor = databaseHelper.query(TABLE_USERS,
                                projection,
                                ContactItemColumns.KEY_ID + " = ? ",
                                new String[]{String.valueOf(id)}, sortOrder);
                    if (null != cursor) {
                        Context context = getContext();
                        if (null != context) {
                            cursor.setNotificationUri(context.getContentResolver(), uri);
                        }
                    }
                    return cursor;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case USERS_DATA_ITEM_TYPE:
                try {
                    String did = uri.getLastPathSegment();
                    if (did != null && !did.isEmpty())
                        cursor = databaseHelper.query(TABLE_USERS,
                                projection,
                                ContactItemColumns.KEY_DATA_ID + " = ? ",
                                new String[]{did}, sortOrder);
                    if (null != cursor) {
                        Context context = getContext();
                        if (null != context) {
                            cursor.setNotificationUri(context.getContentResolver(), uri);
                        }
                    }
                    return cursor;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (sURIMatcher.match(uri)) {
            case USERS_DIR_TYPE:
            case USERS_ITEM_TYPE:
                try {
                    long id = databaseHelper.insert(TABLE_USERS, null, values);
                    Uri insertUri = ContentUris.withAppendedId(UserContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(insertUri, null);
                    }
                    return insertUri;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return uri;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case USERS_DIR_TYPE:
            case USERS_ITEM_TYPE:
                try {
                    int id = databaseHelper.update(TABLE_USERS,
                            values, selection, selectionArgs);
                    Uri insertUri = ContentUris.withAppendedId(UserContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(insertUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * A test package can call this to get a handle to the database underlying NotePadProvider,
     * so it can insert test data into the database. The test case class is responsible for
     * instantiating the provider in a test context; {@link android.test.ProviderTestCase2} does
     * this during the call to setUp()
     *
     * @return a handle to the database helper object for the provider's data.
     */
    public DatabaseHelper getOpenHelperForTest() {
        return databaseHelper;
    }

}
