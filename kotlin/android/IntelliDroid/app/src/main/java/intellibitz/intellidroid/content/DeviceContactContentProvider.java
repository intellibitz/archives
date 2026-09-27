package intellibitz.intellidroid.content;

import android.app.SearchManager;
import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 *
 */
public class DeviceContactContentProvider extends
        ContentProvider {

    public static final String TAG = "DeviceContactCP";

    public static final String TABLE_DEVICECONTACTS = "devicecontact";
    public static final String CREATE_TABLE_DEVICECONTACTS = "CREATE TABLE "
            + TABLE_DEVICECONTACTS + ContactItemColumns.TABLE_CONTACTS_SCHEMA;

    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String CONTACTS_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String CONTACTS_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String CONTACTS_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String CONTACTS_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/all";
    public static final String CONTACTS_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String CONTACTS_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String CONTACTS_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    // UriMatcher stuff
    private static final int CONTACTS_DIR_TYPE = 0;
    private static final int CONTACTS_ITEM_TYPE = 1;
    private static final int CONTACTS_DATA_ITEM_TYPE = 2;
    private static final int CONTACTS_JOIN_DIR_TYPE = 3;
    private static final int CONTACTS_JOIN_ITEM_TYPE = 4;
    private static final int CONTACTS_JOIN_DATA_ITEM_TYPE = 5;
    private static final int CONTACTS_RAW_DIR_TYPE = 6;
    private static final int SEARCH_SUGGEST = 7;
    private static final int REFRESH_SHORTCUT = 8;
    public static String AUTHORITY = "intellibitz.intellidroid.content.DeviceContactContentProvider";
    //    to get only pure contacts minus join
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_DEVICECONTACTS);
    //    to execute contacts with join.. example with mobiles and emails
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + TABLE_DEVICECONTACTS);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_DEVICECONTACTS);
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_DEVICECONTACTS, CONTACTS_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_DEVICECONTACTS +
                "/#", CONTACTS_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_DEVICECONTACTS +
                "/*", CONTACTS_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_DEVICECONTACTS, CONTACTS_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_DEVICECONTACTS +
                        "/#", CONTACTS_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_DEVICECONTACTS +
                        "/*", CONTACTS_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_DEVICECONTACTS, CONTACTS_RAW_DIR_TYPE);
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

    public static Uri updatesDeviceContactJoin(ContactItem deviceContactItem, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.DEVICE_CONTACT,
                    MainApplicationSingleton.Serializer.serialize(deviceContactItem));
//            inserts a single item, so invoke with the content item uri.. not the dir uri
            int row = context.getContentResolver().update(
                    Uri.withAppendedPath(JOIN_CONTENT_URI,
                            String.valueOf(deviceContactItem.getDeviceContactId())), contentValues, null, null);
            return ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, row);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri savesOrUpdatesDeviceContact(ContactItem deviceContactItem, Context context) {
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put(ContactItem.DEVICE_CONTACT,
                    MainApplicationSingleton.Serializer.serialize(deviceContactItem));
//            inserts a single item, so invoke with the content item uri.. not the dir uri
            return context.getContentResolver().insert(
                    Uri.withAppendedPath(CONTENT_URI, "0"), contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri savesOrUpdatesDeviceContacts(Collection<ContactItem> contacts, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.DEVICE_CONTACT,
                    MainApplicationSingleton.Serializer.serialize(contacts));
            return context.getContentResolver().insert(CONTENT_URI, contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri savesOrUpdatesWorkContacts(Collection<ContactItem> contacts, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.WORK_CONTACT,
                    MainApplicationSingleton.Serializer.serialize(contacts));
            return context.getContentResolver().insert(CONTENT_URI, contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ContactItem createsDeviceContactItemFromJSON(
            JSONObject jsonObject, String deviceRef) throws JSONException {
//                ContactItem deviceContactItem = contactItems.get(cid);
        ContactItem deviceContactItem = new ContactItem();
        long cid = jsonObject.optLong("device_contact_id");
        deviceContactItem.setDeviceContactId(cid);
        String deviceRef_ = jsonObject.optString("device_ref");
//        // TODO: 03-05-2016
//        contacts from multiple devices - may be a consolidated view??
//        consider contacts relevant for this user device only.. ignore other devices
        if (null == deviceRef_) {
            deviceContactItem.setDeviceRef(deviceRef);
        } else {
            deviceContactItem.setDeviceRef(deviceRef_);
        }
//        // TODO: 03-05-2016
//        contacts from multiple devices - may be a consolidated view??
//        consider contacts relevant for this user device only.. ignore other devices
        if (deviceRef.equals(deviceContactItem.getDeviceRef())) {
//            deviceContactItem.setDeviceContactId(jsonObject.optLong("device_contact_id"));
//            deviceContactItem.setDevice(true);
            long c_id = jsonObject.optLong("c_id");
            if (c_id > 0)
                deviceContactItem.set_id(c_id);
            String id = jsonObject.optString("_id");
            if (!TextUtils.isEmpty(id))
                deviceContactItem.setDataId(id);
            String rev = jsonObject.optString("_rev");
            if (!TextUtils.isEmpty(rev))
                deviceContactItem.setDataRev(rev);
            int version = jsonObject.optInt("version");
            if (version > 0)
                deviceContactItem.setVersion(version);
            String firstName = jsonObject.optString("first_name");
            if (!TextUtils.isEmpty(firstName))
                deviceContactItem.setFirstName(firstName);
            String lastName = jsonObject.optString("last_name");
            if (!TextUtils.isEmpty(lastName))
                deviceContactItem.setLastName(lastName);
            String dispName = jsonObject.optString("display_name");
            if (!TextUtils.isEmpty(dispName))
                deviceContactItem.setDisplayName(dispName);
            String docType = jsonObject.optString("doc_type");
            if (!TextUtils.isEmpty(docType))
                deviceContactItem.setDocType(docType);
            String docOwner = jsonObject.optString("doc_owner");
            if (!TextUtils.isEmpty(docOwner))
                deviceContactItem.setDocOwner(docOwner);
            long timestamp = jsonObject.optLong("timestamp");
            if (timestamp > 0)
                deviceContactItem.setTimestamp(timestamp);
            JSONArray mobiles = jsonObject.getJSONArray("mobiles");
//            // TODO: 01-07-2016
//            deviceContactItem.setMobiles(mobiles);
//            Log.d(TAG, "mobiles: " + mobiles);
//            // TODO: 30-06-2016
            if (null != mobiles && 0 != mobiles.length()) {
                int m = mobiles.length();
                for (int j = 0; j < m; j++) {
                    JSONObject mobilesJSONObject = mobiles.getJSONObject(j);
//                    MobileItem mobileItem = new MobileItem();
                    String number = mobilesJSONObject.getString("number");
                    if (number != null && number.length() > 0) {
                        String aktId = mobilesJSONObject.optString("akt_id");
                        if (aktId != null && aktId.length() > 0) {
//                            // TODO: 30-06-2016
                            ContactItem intellibitzContactItem = new ContactItem(number);
                            intellibitzContactItem.setIntellibitzId(aktId);
                            intellibitzContactItem.setTypeId(number);
                            intellibitzContactItem.setDataId(aktId);
                            intellibitzContactItem.setDevice(true);
                            intellibitzContactItem.setCloud(true);
                            String akt_name = mobilesJSONObject.optString("akt_name");
                            intellibitzContactItem.setName(akt_name);
                            intellibitzContactItem.setFirstName(deviceContactItem.getFirstName());
                            intellibitzContactItem.setLastName(deviceContactItem.getLastName());
                            intellibitzContactItem.setDisplayName(deviceContactItem.getDisplayName());
                            String akt_status = "akt_status";
                            String aktStatus = mobilesJSONObject.optString(akt_status);
                            if (!TextUtils.isEmpty(aktStatus))
                                intellibitzContactItem.setStatus(aktStatus);
                            String akt_profile_pic = mobilesJSONObject.optString("akt_profile_pic");
                            intellibitzContactItem.setProfilePic(akt_profile_pic);
                            intellibitzContactItem.setCloudPic(akt_profile_pic);
                            deviceContactItem.setIsIntellibitzContact(1);
                            deviceContactItem.setIntellibitzId(intellibitzContactItem.getIntellibitzId());
                            if (null == deviceContactItem.getProfilePic()) {
                                deviceContactItem.setProfilePic(intellibitzContactItem.getProfilePic());
                            }
                            deviceContactItem.setCloudPic(akt_profile_pic);
                            if (null == deviceContactItem.getStatus())
                                deviceContactItem.setStatus(intellibitzContactItem.getStatus());
//                            // TODO: 30-06-2016
//                            mobileItem.setIntellibitzId(aktId);
//                            mobileItem.setDevice(true);
//                            mobileItem.setCloud(true);
//                            long mobile_id = mobile.optLong("mobile_id");
//                            intellibitzContactItem.set_id(mobile_id);
//                            intellibitzContactItem.setMobile(number);
//                            deviceContactItem.addMobile(mobileItem);
//                            deviceContactItem.addMobile(number);
/*
                            intellibitzContactItem.setDataId(aktId);
                            intellibitzContactItem.setIntellibitzId(aktId);
                            intellibitzContactItem.setMobileItem(mobileItem);
                            intellibitzContactItem.setName(mobileItem.getName());
                            if (null == intellibitzContactItem.getName()) {
                                intellibitzContactItem.setName(deviceContactItem.getName());
                            }
                            intellibitzContactItem.setStatus(mobileItem.getStatus());
                            intellibitzContactItem.setProfilePic(mobileItem.getProfilePic());
                            if (null == intellibitzContactItem.getProfilePic()) {
                                intellibitzContactItem.setProfilePic(deviceContactItem.getProfilePic());
                            }
*/
                            deviceContactItem.getIntellibitzContacts().add(intellibitzContactItem);
                        }
                    }
                }
            }
//            // TODO: 30-06-2016
/*
            JSONArray emails = jsonObject.getJSONArray("emails");
            deviceContactItem.setEmails(emails);
            if (null != emails && 0 != emails.length()) {
                int m = emails.length();
                for (int j = 0; j < m; j++) {
                    String email = emails.getString(j);
//                    deviceContactItem.addContact(new EmailItem(email, email, email, email));
                    deviceContactItem.addContact(email);
                }
            }
*/
        }
        return deviceContactItem;
/*
{
    "_id": "ef8380b7a7120a663634612420ce37b4",
    "_rev": "2-4edeee7bf3692555af120105289a882d",
    "first_name": "Jeffrey",
    "last_name": "Intellibitz",
    "mobiles": [
        {
            "number": "919600037000",
            "akt_id": "USRMASTER_919600037000",
            "akt_name": "Jeff",
            "akt_status": "",
            "akt_profile_pic": "https://intellibitz-uploads.s3-ap-southeast-1.amazonaws.com/profile-pics/USRMASTER_919600037000_image.jpg"
        }
    ],
    "emails": [
        "jeff@intellibitz.com"
    ],
    "doc_type": "CONTACT",
    "doc_owner": "USRMASTER_919840348914",
    "timestamp": 1461932367
}
         */
    }

    public static ContactItem createsWorkContactItemFromJSON(
            JSONObject jsonObject) throws JSONException {
        if (null == jsonObject) return null;
        String uid = jsonObject.optString("uid");
        if (TextUtils.isEmpty(uid)) return null;
        String email = jsonObject.optString("email");
        String name = jsonObject.optString("name");
        String status = jsonObject.optString("status");
        String pic = jsonObject.optString("profile_pic");
        return createWorkContactItem(uid, email, name, status, pic);
    }

    @NonNull
    public static ContactItem createWorkContactItem(String uid, String email, String name, String status, String pic) {
        ContactItem workContactItem = new ContactItem(uid, name, email);
        workContactItem.setIntellibitzId(uid);
        workContactItem.setTypeId(email);
        workContactItem.setIsWorkContact(1);
        workContactItem.setCloud(true);
        workContactItem.setStatus(status);
        workContactItem.setCloudPic(pic);
        return workContactItem;
    }

    @NonNull
    public static HashSet<ContactItem> fillWorkContactItemFromJSONArray(
            JSONArray contactsJSONArray) {
        HashSet<ContactItem> results = new HashSet<>();
        int len = contactsJSONArray.length();
        for (int i = 0; i < len; i++) {
            try {
                JSONObject jsonObject = contactsJSONArray.getJSONObject(i);
                ContactItem workContact = createsWorkContactItemFromJSON(jsonObject);
                if (workContact != null)
                    results.add(workContact);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
        Log.d(TAG, "JSON Contacts Array :" + len);
//        Log.d(TAG, "User Contacts Array :" + contactItems.size());
        Log.d(TAG, "Contacts ToSave Array :" + results.size());
        return results;
    }

    public static HashMap<Long, ContactItem> fillDeviceContactItemFromJSONArray(
            JSONArray contactsJSONArray, String deviceRef) {
        HashMap<Long, ContactItem> results = new HashMap<>();
        // compares contacts from cloud to local
//            syncs cloud.. if there is a diff (add, update, delete)
//        if first name, last name changes - remove old.. create new
//        if mobile, email is updated - update in cloud
//        if a contact is not in device - remove from cloud
//        if the db is empty, store the contacts without the smart sync logic
/*
{
    "status": 1,
    "contacts": [
        {
            "_id": "ed483769d081371a3281d868f24eb956",
            "_rev": "2-a5bb407da94609c8bdd2c7777fd9e104",
            "first_name": "Jeffrey",
            "last_name": "Intellibitz",
            "mobiles": [
                {
                    "number": "919600037000",
                    "akt_id": "USRMASTER_919600037000",
                    "akt_name": "Jeff",
                    "akt_status": "",
                    "akt_profile_pic": "https://intellibitz-uploads.s3-ap-southeast-1.amazonaws.com/profile-pics/USRMASTER_919600037000_image.jpg"
                }
            ],
            "emails": [
                "jeff@intellibitz.com"
            ],
            "doc_type": "CONTACT",
            "doc_owner": "USRMASTER_919840348914",
            "timestamp": 1461916025
        }
    ]
}         */
        int len = contactsJSONArray.length();
        for (int i = 0; i < len; i++) {
            try {
                JSONObject jsonObject = contactsJSONArray.getJSONObject(i);
//                ContactItem deviceContactItem = new ContactItem();
/*
                if (null == deviceContactItem) {
                    Log.e(TAG, "Contact is NULL: not found in User: " + jsonObject);
                }
*/
                ContactItem deviceContactItem = createsDeviceContactItemFromJSON(jsonObject, deviceRef);
                HashSet<ContactItem> intellibitzContacts = deviceContactItem.getIntellibitzContacts();
                if (intellibitzContacts != null && !intellibitzContacts.isEmpty())
                    results.put(deviceContactItem.getDeviceContactId(), deviceContactItem);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
        Log.d(TAG, "JSON Contacts Array :" + len);
//        Log.d(TAG, "User Contacts Array :" + contactItems.size());
        Log.d(TAG, "Contacts ToSave Array :" + results.size());
        return results;
    }

    @NonNull
    public static synchronized JSONArray fillJsonArrayFromDeviceContactItem(
            Collection<ContactItem> deviceContactItems, Context context) {
        JSONArray deviceContacts = new JSONArray();
        ContactItem[] array = deviceContactItems.toArray(new ContactItem[0]);
        for (ContactItem deviceContactItem : array) {
            try {
                String firstName = deviceContactItem.getFirstName();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("version", deviceContactItem.getVersion());
                jsonObject.put("first_name", firstName);
                jsonObject.put("last_name", deviceContactItem.getLastName());
                jsonObject.put("display_name", deviceContactItem.getDisplayName());
                jsonObject.put("device_ref", deviceContactItem.getDeviceRef());
                jsonObject.put("c_id", deviceContactItem.get_id());
                jsonObject.put("device_contact_id", deviceContactItem.getDeviceContactId());
//                    jsonObject.put("profile_pic", contact.getProfilePic());
                JSONArray mobiles = new JSONArray();
//                Set<MobileItem> mobileItems = contact.getMobiles();
                JSONArray mobileItems = deviceContactItem.getMobiles();
                for (int i = 0; i < mobileItems.length(); i++) {
                    JSONObject mobile = new JSONObject();
                    String number = mobileItems.getString(i);
                    String number2 = MainApplicationSingleton.parseFormatPhoneNumberByISO(
                            number, MainApplicationSingleton.getSimCountryIso(context));
                    if (number2 != null)
                        number = number2;
                    number = PhoneNumberUtil.normalizeDigitsOnly(number);
                    mobile.put("number", number);
                    mobile.put("mobile_id", deviceContactItem.get_id());
//                        adds the mobile json to the mobiles json array
                    mobiles.put(mobile);
                }
                jsonObject.put("mobiles", mobiles);
/*
                JSONArray emails = new JSONArray();
                Set<EmailItem> emailItems = deviceContactItem.getEmails();
                for (EmailItem emailItem : emailItems) {
//                        adds the email string to the array
                    emails.put(emailItem.getEmail());
                }
*/
//                jsonObject.put("emails", emails);
                jsonObject.put("emails", deviceContactItem.getEmails());
                deviceContacts.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return deviceContacts;
    }

    public static SparseArray<ContactItem> fillDeviceContactItemFromCursor(Cursor cursor) {
        SparseArray<ContactItem> contactItems = new SparseArray<>();
        do {
            ContactItem deviceContactItem = new ContactItem();
            long ctid = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DEVICE_CONTACTID));
            deviceContactItem.setDeviceContactId(ctid);
            deviceContactItem.set_id(cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID)));
            deviceContactItem.setDataId(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DATA_ID)));
            deviceContactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_INTELLIBITZ_ID)));
            deviceContactItem.setVersion(cursor.getInt(cursor.getColumnIndex(
                    ContactItemColumns.KEY_VERSION)));
            deviceContactItem.setDeviceRef(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DEVICE_REF)));
            deviceContactItem.setName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_NAME)));
            deviceContactItem.setFirstName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_FIRST_NAME)));
            deviceContactItem.setLastName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_LAST_NAME)));
            deviceContactItem.setDisplayName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DISPLAY_NAME)));
            deviceContactItem.setCompanyName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_COMPANY_NAME)));
            deviceContactItem.setStatus(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_STATUS)));
            deviceContactItem.setEmails(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_EMAILS)));
            deviceContactItem.setMobiles(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_PHONES)));
            deviceContactItem.setProfilePic(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_PIC)));
            deviceContactItem.setCloudPic(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_CLOUD_PIC)));
            contactItems.put((int) deviceContactItem.getDeviceContactId(), deviceContactItem);
        } while (cursor.moveToNext());
        return contactItems;
    }

    public static SparseArray<ContactItem> fillsDeviceContactItemFromCursor(Cursor cursor) {
        SparseArray<ContactItem> contactItems = new SparseArray<>();
        do {
            ContactItem deviceContactItem = new ContactItem();
            long ctid = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            deviceContactItem.setDeviceContactId(ctid);
            deviceContactItem.set_id(cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID)));
            deviceContactItem.setDataId(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DATA_ID)));
            deviceContactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_INTELLIBITZ_ID)));
            deviceContactItem.setVersion(cursor.getInt(cursor.getColumnIndex(
                    ContactItemColumns.KEY_VERSION)));
            deviceContactItem.setDeviceRef(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DEVICE_REF)));
            deviceContactItem.setName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_NAME)));
            deviceContactItem.setFirstName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_FIRST_NAME)));
            deviceContactItem.setLastName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_LAST_NAME)));
            deviceContactItem.setDisplayName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DISPLAY_NAME)));
            deviceContactItem.setCompanyName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_COMPANY_NAME)));
            deviceContactItem.setStatus(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_STATUS)));
            deviceContactItem.setEmails(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_EMAILS)));
            deviceContactItem.setMobiles(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_PHONES)));
            deviceContactItem.setProfilePic(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_PIC)));
            deviceContactItem.setCloudPic(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_CLOUD_PIC)));
            contactItems.put((int) deviceContactItem.get_id(), deviceContactItem);
        } while (cursor.moveToNext());
        return contactItems;
    }

    public static Bitmap openPhoto(long contactId, Context context) throws IOException {
        Uri contactUri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(
                contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        return MainApplicationSingleton.getBitmapDecodePhotoUri(photoUri, context);
    }

    public static Bitmap openDisplayPhoto(long contactId, Context context) throws IOException {
        Uri contactUri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(
                contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        AssetFileDescriptor fd =
                context.getApplicationContext().getContentResolver().openAssetFileDescriptor(
                        displayPhotoUri, "r");
        if (fd != null) {
            InputStream inputStream = fd.createInputStream();
            if (null == inputStream) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        }
        return null;
    }

    public static boolean isContactsEmptyInDB(Context context) {
        return DatabaseHelper.isRowCountEmpty(
                RAW_CONTENT_URI,
                TABLE_DEVICECONTACTS,
                null,
                context);
    }

    public static ContentValues fillContentValuesFromDeviceContactItemDataId(
            ContactItem deviceContactItem, ContentValues values) {
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DATA_ID, deviceContactItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_DATA_REV, deviceContactItem.getDataRev());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_INTELLIBITZ_ID, deviceContactItem.getIntellibitzId());
        MainApplicationSingleton.fillIfNotNull(
                values, ContactItemColumns.KEY_IS_INTELLIBITZ, deviceContactItem.getIsIntellibitzContact());
        return values;
    }

    public static ContentValues fillContentValuesFromDeviceContactItem(
            ContactItem deviceContactItem, ContentValues values) {
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATA_ID, deviceContactItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DEVICE_CONTACTID, deviceContactItem.getDeviceContactId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_INTELLIBITZ_ID, deviceContactItem.getIntellibitzId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATA_REV, deviceContactItem.getDataRev());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_VERSION, deviceContactItem.getVersion());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DEVICE_REF, deviceContactItem.getDeviceRef());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DOC_TYPE, deviceContactItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DOC_OWNER, deviceContactItem.getDocOwner());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_STATUS, deviceContactItem.getStatus());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_NAME, deviceContactItem.getName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TYPE, deviceContactItem.getType());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_FIRST_NAME, deviceContactItem.getFirstName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_LAST_NAME, deviceContactItem.getLastName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DISPLAY_NAME, deviceContactItem.getDisplayName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_COMPANY_NAME, deviceContactItem.getCompanyName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_PIC, deviceContactItem.getProfilePic());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_CLOUD_PIC, deviceContactItem.getCloudPic());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_PHONES, deviceContactItem.getMobiles());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_EMAILS, deviceContactItem.getEmails());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_INTELLIBITZ, deviceContactItem.getIsIntellibitzContact());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_ISWORK, deviceContactItem.getIsWorkContact());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TIMESTAMP, deviceContactItem.getTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATETIME,
                MainApplicationSingleton.getDateTimeMillis(deviceContactItem.getTimestamp()));
        return values;
    }

    public static long createOrUpdateDeviceContact(
            DatabaseHelper databaseHelper, ContactItem item) {
        Collection<ContactItem> deviceContactItems = new ArrayList<>(1);
        deviceContactItems.add(item);
        long[] ids = createOrUpdateDeviceContacts(databaseHelper, deviceContactItems);
        if (null == ids) return 0;
        return ids[0];
    }

/*
    public static SparseArray<ContactItem> fillDeviceContactItemFromAllJoinCursor(Cursor cursor) {
        SparseArray<ContactItem> contactItems = new SparseArray<>();
        ContactItem deviceContactItem = new ContactItem();
        do {
            long ctid = cursor.getLong(cursor.getColumnIndex("ctcid"));
            if (ctid == deviceContactItem.getDeviceContactId()) {

            } else {
                deviceContactItem = new ContactItem();
                deviceContactItem.setDeviceContactId(ctid);
                deviceContactItem.set_id(cursor.getLong(cursor.getColumnIndex("ct_id")));
                deviceContactItem.setDataId(cursor.getString(cursor.getColumnIndex("ctid")));
                deviceContactItem.setVersion(cursor.getInt(cursor.getColumnIndex("ctversion")));
                deviceContactItem.setDeviceRef(cursor.getString(cursor.getColumnIndex("ctdr")));
                deviceContactItem.setName(cursor.getString(cursor.getColumnIndex("ctname")));
                deviceContactItem.setFirstName(cursor.getString(cursor.getColumnIndex("ctfn")));
                deviceContactItem.setLastName(cursor.getString(cursor.getColumnIndex("ctln")));
                deviceContactItem.setCompanyName(cursor.getString(cursor.getColumnIndex("ctcomp")));
                deviceContactItem.setStatus(cursor.getString(cursor.getColumnIndex("ctst")));
                deviceContactItem.setProfilePic(cursor.getString(cursor.getColumnIndex("ctppic")));
                contactItems.put((int) deviceContactItem.getDeviceContactId(), deviceContactItem);
            }
            String email = cursor.getString(cursor.getColumnIndex("email"));
            if (email != null) {
                long e_id = cursor.getLong(cursor.getColumnIndex("e_id"));
                String eid = cursor.getString(cursor.getColumnIndex("eid"));
                EmailItem item = new EmailItem(eid, email, email, email);
                item.set_id(e_id);
//                deviceContactItem.addContact(item);
                deviceContactItem.addEmail(email);
            }
            String mobile = cursor.getString(cursor.getColumnIndex("mobile"));
            if (mobile != null) {
                String mid = cursor.getString(cursor.getColumnIndex("mid"));
                MobileItem mobileItem = new MobileItem(mid, mobile, mobile, mobile);
                long m_id = cursor.getLong(cursor.getColumnIndex("m_id"));
                mobileItem.set_id(m_id);
//                // TODO: 30-06-2016  
//                mobileItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("makid")));
                mobileItem.setName(cursor.getString(cursor.getColumnIndex("mname")));
                mobileItem.setProfilePic(cursor.getString(cursor.getColumnIndex("mppic")));
                mobileItem.setStatus(cursor.getString(cursor.getColumnIndex("mst")));
//                deviceContactItem.addMobile(mobileItem);
                deviceContactItem.addMobile(mobile);
            }
//                    removes, if already exist
//            contactItems.remove(deviceContactItem);
//                    adds the latest message thread
//                    contactItems.add(deviceContactItem);

        } while (cursor.moveToNext());
        return contactItems;
    }
*/

    public static long[] createOrUpdateDeviceContacts(
            DatabaseHelper databaseHelper, Collection<ContactItem> items) {
        long[] ids = new long[items.size()];
        int i = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContactItem[] array = items.toArray(new ContactItem[0]);
            for (ContactItem item : array) {
                long l = createOrUpdateDeviceContact(databaseHelper, db, item);
                if (0 == l) {
                    Log.e(TAG, "Failed to insert row: " + item);
                    throw new SQLException("Failed to insert row into " + item);
                }
                ids[i++] = l;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ids;
    }

    public static long[] createOrUpdateWorkContacts(
            DatabaseHelper databaseHelper, Collection<ContactItem> items) {
        long[] ids = new long[items.size()];
        int i = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContactItem[] array = items.toArray(new ContactItem[0]);
            for (ContactItem item : array) {
                long l = createOrUpdateWorkContact(databaseHelper, db, item);
                if (0 == l) {
                    Log.e(TAG, "Failed to insert row: " + item);
                    throw new SQLException("Failed to insert row into " + item);
                }
                ids[i++] = l;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ids;
    }

    public static long createDeviceContact(DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item) {
//        contacts are different.. the device is the master
//        device contact id must be checked.. not the cloud id
        long _id = databaseHelper.insert(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, null,
                DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, new ContentValues()));
        item.set_id(_id);
//        // TODO: 30-06-2016
//        createContactEmails(db, item.getEmails(), item.get_id());
//        createContactMobiles(db, item.getMobiles(), item.get_id());
        return item.get_id();
    }

    public static long createOrUpdateDeviceContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item) {
//        contacts are different.. the device is the master
//        device contact id must be checked.. not the cloud id
        Cursor cursor = databaseHelper.query(db,
                DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                new String[]{ContactItemColumns.KEY_ID,
                        ContactItemColumns.KEY_DEVICE_CONTACTID},
                ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?",
                new String[]{String.valueOf(item.getDeviceContactId())}, null);
        int count = 0;
        long _id = 0;
        if (null != cursor) {
            count = cursor.getCount();
            if (count > 0)
                _id = cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_ID));
            cursor.close();
        }
        if (0 == _id) {
            _id = databaseHelper.insert(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, null,
                    DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, new ContentValues()));
            item.set_id(_id);
        } else {
            item.set_id(_id);
            databaseHelper.update(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                    DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, new ContentValues()),
                    ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?",
                    new String[]{String.valueOf(item.getDeviceContactId())});
        }
        createOrUpdateIntellibitzContactsDeviceContactJoin(
                databaseHelper, db, item.getIntellibitzContacts(), item.get_id());
        return item.get_id();
    }

    public static long createOrUpdateWorkContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem workContact) {
//        contacts are different.. the device is the master
//        device contact id must be checked.. not the cloud id

        if (null == workContact) {
            Log.e(TAG, "createOrUpdateWorkContact: work contact is null - returning 0");
            return 0;
        }
        if (null == db) {
            Log.e(TAG, "createOrUpdateWorkContact: db is null - returning 0");
            return 0;
        }
        if (null == databaseHelper) {
            Log.e(TAG, "createOrUpdateWorkContact: db helper is null - returning 0");
            return 0;
        }

        ContactItem deviceContact = null;

//        checks if work contact has a representation in device contact
//        if device contact available, add the work contact and update
//        if not available, create new device contact and insert


        String uid = workContact.getDataId();
        String email = workContact.getEmail();
        String typeId = workContact.getTypeId();
        Cursor cursor = null;
        if (typeId != null) {
            cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                    new String[]{
                            ContactItemColumns.KEY_ID,
                            ContactItemColumns.KEY_VERSION,
                            ContactItemColumns.KEY_DATA_ID,
                            ContactItemColumns.KEY_DEVICE_CONTACTID,
                            ContactItemColumns.KEY_NAME,
                            ContactItemColumns.KEY_FIRST_NAME,
                            ContactItemColumns.KEY_LAST_NAME,
                            ContactItemColumns.KEY_DISPLAY_NAME,
                            ContactItemColumns.KEY_PIC,
                            ContactItemColumns.KEY_CLOUD_PIC
                    },
                    "TRIM(" + ContactItemColumns.KEY_EMAILS + ")" +
                            " LIKE '%' || ? || '%'  OR " +
                            ContactItemColumns.KEY_INTELLIBITZ_ID +
                            " LIKE '%' || ? || '%'  OR " +
                            ContactItemColumns.KEY_INTELLIBITZ_ID +
                            " = ? ",
                    new String[]{email, typeId, uid}, null);
        }
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//        if not available, create new device contact and insert
            deviceContact = createsDeviceContactItem(workContact);
            deviceContact.getIntellibitzContacts().add(workContact);
            _id = databaseHelper.insert(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS, null,
                    DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(deviceContact, new ContentValues()));
            deviceContact.set_id(_id);
        } else {
//        if device contact available, add the work contact and update
            _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
            int ver = cursor.getInt(cursor.getColumnIndex(ContactItemColumns.KEY_VERSION));
            String dataId = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DATA_ID));
            long dcid = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID));
            String name = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_NAME));
            String first = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME));
            String last = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME));
            String display = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME));
            String pic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC));
            String cpic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC));
            cursor.close();
            deviceContact = createsDeviceContactItem(_id, ver, dataId, name, first, last, display, pic, cpic);
            deviceContact.set_id(_id);
            deviceContact.setDeviceContactId(dcid);
            deviceContact.getIntellibitzContacts().add(workContact);
            databaseHelper.update(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                    DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(deviceContact, new ContentValues()),
                    ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(deviceContact.get_id())});

        }
        createOrUpdateIntellibitzContactsDeviceContactJoin(
                databaseHelper, db, deviceContact.getIntellibitzContacts(), deviceContact.get_id());
        return deviceContact.get_id();
    }

    public static long createOrUpdateIntellibitzContactDeviceContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item, long id) {
        long _id = IntellibitzContactContentProvider.createOrUpdateIntellibitzContact(databaseHelper, db, item);
//        creates the join
        IntellibitzContactContentProvider.createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper, db, _id, id);
//        // TODO: 30-06-2016
/*
        MobileItem selectedMobile = item.getMobileItem();
        if (selectedMobile != null)
            createOrUpdateIntellibitzContactMobileJoin(db, selectedMobile, item.get_id());
        EmailItem selectedEmail = item.getEmailItem();
        if (selectedEmail != null)
            createOrUpdateIntellibitzContactEmailJoin(db, selectedEmail, item.get_id());
*/
        return item.get_id();
    }

    public static long[] createOrUpdateIntellibitzContactsDeviceContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, Collection<ContactItem> items, long id) {
        if (null == items) return new long[0];
        long[] ids = new long[items.size()];
        int i = 0;
        for (ContactItem item : items) {
            long l = createOrUpdateIntellibitzContactDeviceContactJoin(databaseHelper, db, item, id);
            if (0 == l) {
                Log.e(TAG, "Failed to insert row: " + item);
                throw new SQLException("Failed to insert row into " + item);
            }
            ids[i++] = l;
        }
        return ids;
    }

    public static long[] createDeviceContact(DatabaseHelper databaseHelper, Set<ContactItem> items) {
        long[] ids = new long[items.size()];
        int i = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContactItem item : items) {
                long l = createDeviceContact(databaseHelper, db, item);
                if (0 == l) {
                    Log.e(TAG, "Failed to insert row: " + item);
                    throw new SQLException("Failed to insert row into " + item);
                }
//                updates the contacts, with the db id
//                item.set_id(l);
                ids[i++] = l;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ids;
    }

    public static Cursor getDeviceContactsJoin(DatabaseHelper databaseHelper, long id) {
        String selectQuery =
                "SELECT  " +
                        "*, ct.id as ctid, m.id as mid  FROM  " +
                        DeviceContactContentProvider.TABLE_DEVICECONTACTS +
/*
                        " ct left outer join " + TABLE_CONTACTS_EMAILS_JOIN +
                        " cte on ct.[_id] = cte.[" + ContactEmailJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + TABLE_EMAILS +
                        " e on e.[_id] = cte.[" + ContactEmailJoinColumns.KEY_EMAIL_ID + "] " +
                        "left outer join " + TABLE_CONTACTS_MOBILES_JOIN +
                        " ctm on ct.[_id] = ctm.[" + ContactMobileJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + TABLE_MOBILES +
                        " m on m.[_id] = ctm.[" + ContactMobileJoinColumns.KEY_MOBILE_ID + "] " +
*/
                        " WHERE ct." + ContactItemColumns.KEY_ID + " = " + id +
                        "";
        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, null);
    }

    public static Cursor getDeviceContactsJoin(DatabaseHelper databaseHelper, String id) {
        String selectQuery =
                "SELECT  " +
                        "*, ct.id as ctid, m.id as mid  FROM  " +
                        DeviceContactContentProvider.TABLE_DEVICECONTACTS +
/*
                        " ct left outer join " + TABLE_CONTACTS_EMAILS_JOIN +
                        " cte on ct.[_id] = cte.[" + ContactEmailJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + TABLE_EMAILS +
                        " e on e.[_id] = cte.[" + ContactEmailJoinColumns.KEY_EMAIL_ID + "] " +
                        "left outer join " + TABLE_CONTACTS_MOBILES_JOIN +
                        " ctm on ct.[_id] = ctm.[" + ContactMobileJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + TABLE_MOBILES +
                        " m on m.[_id] = ctm.[" + ContactMobileJoinColumns.KEY_MOBILE_ID + "] " +
*/
                        " WHERE ct." + ContactItemColumns.KEY_DATA_ID + " = " + id +
                        "";
        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, null);
    }

    public static long updateDeviceContactJoin(DatabaseHelper databaseHelper, ContactItem item) {
//        contacts are different.. the device is the master
//        device contact id must be checked.. not the cloud id
//            updates only the data id from the intellibitz cloud
        if (0 == item.get_id()) {
            getDeviceContactDBId(databaseHelper, item);
        }
        if (item.get_id() != 0) {
            updateDeviceContactDataId(databaseHelper, item);
//        createOrUpdateContactEmails(item.getEmails(), item.get_id());
//        contacts are different.. the device is the master
//        use db id, for joins
//            // TODO: 30-06-2016
//            updateContactMobiles(item.getMobiles(), item.get_id());
        }
        return item.get_id();
    }

    public static long getDeviceContactDBId(DatabaseHelper databaseHelper, ContactItem item) {
        Cursor cursor = databaseHelper.query(DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                new String[]{
                        ContactItemColumns.KEY_ID, ContactItemColumns.KEY_DEVICE_CONTACTID},
                ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?",
                new String[]{String.valueOf(item.getDeviceContactId())}, null);
        if (cursor != null) {
            if (cursor.getCount() != 0) {
                long _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
                item.set_id(_id);
            }
            cursor.close();
        }
        return item.get_id();
    }

    public static long updateDeviceContactJoin_(DatabaseHelper databaseHelper, ContactItem item) {
//        contacts are different.. the device is the master
//        device contact id must be checked.. not the cloud id
        Cursor cursor = databaseHelper.query(DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                new String[]{
                        ContactItemColumns.KEY_ID, ContactItemColumns.KEY_DEVICE_CONTACTID},
                ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?",
                new String[]{String.valueOf(item.getDeviceContactId())}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            return 0;
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
//            updates only the data id from the intellibitz cloud
            updateDeviceContactDataId(databaseHelper, item);
        }
//        createOrUpdateContactEmails(item.getEmails(), item.get_id());
//        // TODO: 30-06-2016
//        updateContactMobiles(item.getMobiles(), item.get_id());
        return item.get_id();
    }

    public static int updateDeviceContactDataId(DatabaseHelper databaseHelper, ContactItem item) {
        // updating row
//        contacts are different.. device is the master
//        device contact id must be used.. not the cloud id
        return databaseHelper.update(DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                DeviceContactContentProvider.fillContentValuesFromDeviceContactItemDataId(item, new ContentValues()),
                ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?",
                new String[]{String.valueOf(item.getDeviceContactId())});
    }

    public static int updateDeviceContact(DatabaseHelper databaseHelper, ContactItem item) {
        // updating row
//        contacts are different.. device is the master
//        device contact id must be used.. not the cloud id
        return databaseHelper.update(DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, new ContentValues()),
                ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?",
                new String[]{String.valueOf(item.getDeviceContactId())});
    }

    public static int updateDeviceContact(DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item) {
        // updating row
//        contacts are different.. device is the master
//        device contact id must be used.. not the cloud id
        return databaseHelper.update(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(item, new ContentValues()),
                ContactItemColumns.KEY_DEVICE_CONTACTID + " = ?",
                new String[]{String.valueOf(item.getDeviceContactId())});
    }

    @NonNull
    public static ContactItem createsDeviceContactItem(
            int contactId, int ver, String dataId, String name, String firstName, String lastName,
            String displayName, String profilePic, Collection<String> mobiles, Collection<String> emails) {
        ContactItem deviceContact = createsDeviceContactItem(
                contactId, ver, dataId, name, firstName, lastName, displayName, profilePic, profilePic);
        deviceContact.setMobiles(mobiles);
        deviceContact.setEmails(emails);
        return deviceContact;
    }

    public static ContactItem createsDeviceContactItem(
            long contactId, int ver, String dataId, String name, String firstName, String lastName,
            String displayName, String profilePic, String cloudPic) {
        ContactItem deviceContact = new ContactItem();
        deviceContact.set_id(contactId);
        if (ver > 0)
            deviceContact.setVersion(ver);
        deviceContact.setDeviceContactId(contactId);
        deviceContact.setDataId(dataId);
        deviceContact.setName(name);
        deviceContact.setFirstName(firstName);
        deviceContact.setLastName(lastName);
        deviceContact.setDisplayName(displayName);
        deviceContact.setProfilePic(profilePic);
        deviceContact.setCloudPic(cloudPic);
        return deviceContact;
    }

    public static ContactItem createsDeviceContactItem(ContactItem workContact) {
        if (null == workContact) return null;
        ContactItem deviceContact = createsDeviceContactItem(0, 1, workContact.getDataId(),
                workContact.getName(), workContact.getName(), workContact.getName(),
                workContact.getName(), workContact.getProfilePic(), workContact.getCloudPic());
        deviceContact.setDeviceContactId(0);
        return deviceContact;
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
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
                return CONTACTS_DIR_MIME_TYPE;
            case CONTACTS_ITEM_TYPE:
                return CONTACTS_ITEM_MIME_TYPE;
            case CONTACTS_DATA_ITEM_TYPE:
                return CONTACTS_DATA_ITEM_MIME_TYPE;
            case CONTACTS_JOIN_DIR_TYPE:
                return CONTACTS_JOIN_DIR_MIME_TYPE;
            case CONTACTS_JOIN_ITEM_TYPE:
                return CONTACTS_JOIN_ITEM_MIME_TYPE;
            case CONTACTS_JOIN_DATA_ITEM_TYPE:
                return CONTACTS_JOIN_DATA_ITEM_MIME_TYPE;
            case CONTACTS_RAW_DIR_TYPE:
                return CONTACTS_RAW_DIR_MIME_TYPE;
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
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        Cursor cursor = null;
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
            case CONTACTS_JOIN_DIR_TYPE:
                try {
/*
                    cursor = databaseHelper.getAllDeviceContactsJoinCursor(projection,
                            selection, selectionArgs, sortOrder);
*/
                    cursor = databaseHelper.query(TABLE_DEVICECONTACTS, projection,
                            selection, selectionArgs, sortOrder);
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
            case CONTACTS_ITEM_TYPE:
            case CONTACTS_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(
                            TABLE_DEVICECONTACTS, projection,
                            selection,
                            selectionArgs, sortOrder);
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
            case CONTACTS_JOIN_ITEM_TYPE:
                try {
                    cursor = getDeviceContactsJoin(databaseHelper, ContentUris.parseId(uri));
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
            case CONTACTS_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = getDeviceContactsJoin(databaseHelper, uri.getLastPathSegment());
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
            case CONTACTS_RAW_DIR_TYPE:
                try {
                    cursor = databaseHelper.getRawCursor(selection, selectionArgs);
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
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        byte[] vals1 = values.getAsByteArray(ContactItem.DEVICE_CONTACT);
        byte[] vals2 = values.getAsByteArray(ContactItem.WORK_CONTACT);
        long[] ids = null;
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
                try {
                    if (vals1 != null) {
                        Collection<ContactItem> deviceContactItems = (Collection<ContactItem>)
                                MainApplicationSingleton.Serializer.deserialize(vals1);
                        if (null == deviceContactItems || deviceContactItems.isEmpty()) {
                            Log.e(TAG, "INSERT returns null - EMPTY contacts to insert - check input");
                            return null;
                        }
                        ids = createOrUpdateDeviceContacts(databaseHelper, deviceContactItems);
                    } else if (vals2 != null) {
                        Collection<ContactItem> deviceContactItems = (Collection<ContactItem>)
                                MainApplicationSingleton.Serializer.deserialize(vals2);
                        if (null == deviceContactItems || deviceContactItems.isEmpty()) {
                            Log.e(TAG, "INSERT returns null - EMPTY contacts to insert - check input");
                            return null;
                        }
                        ids = createOrUpdateWorkContacts(databaseHelper, deviceContactItems);
                    }
                    if (null == ids || 0 == ids.length) {
                        Log.e(TAG, "INSERT returns null - contacts failed to insert - check input");
                        return null;
                    }
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(uri, null);
                    }
                    return uri;
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CONTACTS_ITEM_TYPE:
                try {
                    ContactItem item = (ContactItem) MainApplicationSingleton.Serializer.deserialize(vals1);
                    long id = createOrUpdateDeviceContact(databaseHelper, item);
                    if (0 == id) {
                        Log.e(TAG, "INSERT returns null - contacts failed to insert - check input");
                        return null;
                    }
                    Context context = getContext();
                    Uri insertUri = ContentUris.withAppendedId(uri, id);
                    if (null != context) {
                        context.getContentResolver().notifyChange(insertUri, null);
                    }
                    return insertUri;
                } catch (SQLException | IOException | ClassNotFoundException e) {
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
        int id = 0;
        Uri updateUri;
        Context context = getContext();
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
            case CONTACTS_ITEM_TYPE:
                try {
                    id = databaseHelper.update(TABLE_DEVICECONTACTS,
                            values,
                            selection, selectionArgs);
                    updateUri = ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, id);
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CONTACTS_JOIN_DIR_TYPE:
            case CONTACTS_JOIN_ITEM_TYPE:
                try {
                    byte[] vals1 = values.getAsByteArray(ContactItem.DEVICE_CONTACT);
                    ContactItem item = (ContactItem) MainApplicationSingleton.Serializer.deserialize(vals1);
                    id = (int) updateDeviceContactJoin(databaseHelper, item);
                    updateUri = ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, id);
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException | IOException | ClassNotFoundException e) {
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
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
            case CONTACTS_ITEM_TYPE:
                try {
                    int id = databaseHelper.delete(TABLE_DEVICECONTACTS,
                            selection, selectionArgs);
                    Uri insertUri = ContentUris.withAppendedId(DeviceContactContentProvider.CONTENT_URI, id);
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
