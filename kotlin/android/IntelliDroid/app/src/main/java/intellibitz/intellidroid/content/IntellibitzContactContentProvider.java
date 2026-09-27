package intellibitz.intellidroid.content;

import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactIntellibitzContactJoinColumns;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactIntellibitzContactJoinColumns;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static intellibitz.intellidroid.content.DeviceContactContentProvider.*;


/**
 *
 */
public class IntellibitzContactContentProvider extends
        ContentProvider {

    public static final String TAG = "IntellibitzContactProvider";
    public static final String TABLE_INTELLIBITZCONTACT = "intellibitzcontact";
    public static final String TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN = "intellibitzcontact_devicecontact";
    public static final String CREATE_TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN = "CREATE TABLE "
            + TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN + "( " +
            ContactIntellibitzContactJoinColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " INTEGER," +
            ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + " INTEGER," +
            ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    public static final String CREATE_TABLE_INTELLIBITZCONTACTS = "CREATE TABLE "
            + TABLE_INTELLIBITZCONTACT + ContactItemColumns.TABLE_CONTACTS_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String INTELLIBITZCONTACTS_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String INTELLIBITZCONTACTS_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String INTELLIBITZCONTACTS_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String INTELLIBITZCONTACTS_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/all";
    public static final String INTELLIBITZCONTACTS_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String INTELLIBITZCONTACTS_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String INTELLIBITZCONTACTS_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    // UriMatcher stuff
    private static final int INTELLIBITZCONTACTS_DIR_TYPE = 0;
    private static final int INTELLIBITZCONTACTS_ITEM_TYPE = 1;
    private static final int INTELLIBITZCONTACTS_DATA_ITEM_TYPE = 2;
    private static final int INTELLIBITZCONTACTS_JOIN_DIR_TYPE = 3;
    private static final int INTELLIBITZCONTACTS_JOIN_ITEM_TYPE = 4;
    private static final int INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE = 5;
    private static final int INTELLIBITZCONTACTS_RAW_DIR_TYPE = 6;
    private static final int SEARCH_SUGGEST = 7;
    private static final int REFRESH_SHORTCUT = 8;
    public static String AUTHORITY = "intellibitz.intellidroid.content.IntellibitzContactContentProvider";
    //    to get only pure contacts minus join
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_INTELLIBITZCONTACT);
    //    to execute contacts with join.. example with mobiles and emails
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + TABLE_INTELLIBITZCONTACT);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_INTELLIBITZCONTACT);
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    public static long createOrUpdateIntellibitzContactDeviceContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = getIntellibitzContactDeviceContactJoin(databaseHelper, db, id, fk);
        ContentValues values = new ContentValues();
        values.put(ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID, id);
        values.put(ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID, fk);
        values.put(ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP,
                MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db, TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN, null, values);
        } else {
            _id = databaseHelper.update(db,
                    TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN, values,
                    ContactIntellibitzContactJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long getIntellibitzContactDeviceContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(db, TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN,
                new String[]{ContactIntellibitzContactJoinColumns.KEY_ID},
                ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " = ? and " +
                        ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(fk)}, null, null, null);
        if (c != null && c.getCount() > 0) {
            _id = c.getLong(c.getColumnIndex("_id"));
            c.close();
        }
        return _id;
    }

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_INTELLIBITZCONTACT, INTELLIBITZCONTACTS_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_INTELLIBITZCONTACT +
                "/#", INTELLIBITZCONTACTS_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_INTELLIBITZCONTACT +
                "/*", INTELLIBITZCONTACTS_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_INTELLIBITZCONTACT, INTELLIBITZCONTACTS_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_INTELLIBITZCONTACT +
                        "/#", INTELLIBITZCONTACTS_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_INTELLIBITZCONTACT +
                        "/*", INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_INTELLIBITZCONTACT, INTELLIBITZCONTACTS_RAW_DIR_TYPE);
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

    public static Uri updateContactItemToDB(ContactItem deviceContactItem, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(deviceContactItem));
//            inserts a single item, so invoke with the content item uri.. not the dir uri
            int row = context.getContentResolver().update(
                    Uri.withAppendedPath(JOIN_CONTENT_URI,
                            String.valueOf(deviceContactItem.getDeviceContactId())), contentValues, null, null);
            return ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, row);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri saveContactItemToDB(ContactItem deviceContactItem, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(deviceContactItem));
//            inserts a single item, so invoke with the content item uri.. not the dir uri
            return context.getContentResolver().insert(
                    Uri.withAppendedPath(CONTENT_URI, "0"), contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri saveContactsInDB(HashSet<ContactItem> contacts, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(contacts));
            return context.getContentResolver().insert(CONTENT_URI, contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseFormatPhoneNumberByISO(String number, String iso) {
        String formattedPhoneNumber = null;
        Phonenumber.PhoneNumber phoneNumber;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            phoneNumber = phoneNumberUtil.parse(number, iso);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                formattedPhoneNumber = phoneNumberUtil.format(phoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (NumberParseException npe) {
            formattedPhoneNumber = null;
        }

        return formattedPhoneNumber;
    }

    public static String getSimCountryIso(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        String country = telephonyManager.getNetworkCountryIso();
        if (null == country) {
            country = telephonyManager.getSimCountryIso();
        }
        if (null == country) {
            country = Locale.getDefault().getCountry();
        }
//        call IN .. if everything fails
        if (null == country) {
            country = "IN";
        }
        return country.toUpperCase();
    }

    public static HashMap<Long, ContactItem> fillIntellibitzContactFromCursor(
            HashMap<Long, ContactItem> contactItems, Cursor cursor) {
        ContactItem contactItem = new ContactItem();
        do {
            long ctid = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            if (ctid == contactItem.get_id()) {

            } else {
                contactItem = new ContactItem();
                contactItem.set_id(cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_ID)));
                contactItem.setDeviceContactId(cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_DEVICE_CONTACTID)));
                contactItem.setDataId(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_DATA_ID)));
                contactItem.setDataId(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_TYPE_ID)));
                contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_INTELLIBITZ_ID)));
                contactItem.setType(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_TYPE)));
                contactItem.setName(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_NAME)));
                contactItem.setFirstName(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_FIRST_NAME)));
                contactItem.setLastName(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_LAST_NAME)));
                contactItem.setDisplayName(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_DISPLAY_NAME)));
                contactItem.setStatus(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_STATUS)));
                contactItem.setProfilePic(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_PIC)));
                contactItem.setCloudPic(cursor.getString(cursor.getColumnIndex(
                        ContactItemColumns.KEY_CLOUD_PIC)));
                contactItem.setEmailItem(cursor.getInt(cursor.getColumnIndex(
                        ContactItemColumns.KEY_IS_EMAIL)));
                contactItem.setTimestamp(cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_TIMESTAMP)));
                contactItems.put(contactItem.get_id(), contactItem);
            }
        } while (cursor.moveToNext());
        return contactItems;
    }

    public static ContentValues fillContentValuesFromIntellibitzContactItem(ContactItem item,
                                                                            ContentValues values) {
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATA_ID, item.getDataId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DEVICE_CONTACTID, item.getDeviceContactId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TYPE_ID, item.getTypeId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_INTELLIBITZ_ID, item.getIntellibitzId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_NAME, item.getName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_FIRST_NAME, item.getFirstName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_LAST_NAME, item.getLastName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DISPLAY_NAME, item.getDisplayName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TYPE, item.getType());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_STATUS, item.getStatus());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_PIC, item.getProfilePic());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_CLOUD_PIC, item.getCloudPic());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_GROUP, item.isGroup());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_EMAIL, item.isEmailItem());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_ANONYMOUS, item.isAnonymous());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_DEVICE, item.isDevice());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_CLOUD, item.isCloud());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_ISWORK, item.getIsWorkContact());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TIMESTAMP, item.getTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATETIME,
                MainApplicationSingleton.getDateTimeMillis(item.getTimestamp()));
        return values;
    }

    public static SparseArray<ContactItem> fillIntellibitzContactItemFromAllJoinCursor(Cursor cursor) {
        SparseArray<ContactItem> contactItems = new SparseArray<>();
        ContactItem contactItem = new ContactItem();
        do {
            long ctid = cursor.getLong(cursor.getColumnIndex("ct_id"));
            if (ctid == contactItem.get_id()) {
/*
                        "ct._id as ct_id, ct.id as ctid, ct.intellibitz_id as ctcid, ct.type as cttype, " +
                        " ct.device_ref as ctdr, ct.name as ctname,  ct.status as ctst, " +
                        " ct.profile_pic as ctppic, " +
                        " e._id as e_id, e.email as email, e.id as eid, " +
                        " m._id as m_id,  m.id as mid, m.intellibitz_id as makid,  " +
                        " m.mobile as mobile, m.name as mname, m.profile_pic as mppic, " +
                        " m.status as mst" +

 */
            } else {
                contactItem = new ContactItem();
                contactItem.set_id(cursor.getLong(cursor.getColumnIndex("ct_id")));
                contactItem.setDataId(cursor.getString(cursor.getColumnIndex("ctid")));
                contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("ctcid")));
                contactItem.setDeviceRef(cursor.getString(cursor.getColumnIndex("ctdr")));
                contactItem.setName(cursor.getString(cursor.getColumnIndex("ctname")));
                contactItem.setStatus(cursor.getString(cursor.getColumnIndex("ctst")));
                contactItem.setProfilePic(cursor.getString(cursor.getColumnIndex("ctppic")));
                contactItems.put((int) contactItem.get_id(), contactItem);
            }
//            // TODO: 30-06-2016
/*
            String email = cursor.getString(cursor.getColumnIndex("email"));
            if (email != null) {
                long e_id = cursor.getLong(cursor.getColumnIndex("e_id"));
                String eid = cursor.getString(cursor.getColumnIndex("eid"));
                EmailItem item = new EmailItem(eid, email, email, email);
                item.set_id(e_id);
                contactItem.setEmailItem(item);
            }
            String mobile = cursor.getString(cursor.getColumnIndex("mobile"));
            if (mobile != null) {
                String mid = cursor.getString(cursor.getColumnIndex("mid"));
                MobileItem mobileItem = new MobileItem(mid, mobile, mobile, mobile);
                long m_id = cursor.getLong(cursor.getColumnIndex("m_id"));
                mobileItem.set_id(m_id);
                mobileItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("makid")));
                mobileItem.setName(cursor.getString(cursor.getColumnIndex("mname")));
                mobileItem.setProfilePic(cursor.getString(cursor.getColumnIndex("mppic")));
                mobileItem.setStatus(cursor.getString(cursor.getColumnIndex("mst")));
                contactItem.setMobileItem(mobileItem);
            }
*/
//                    removes, if already exist
//            contactItems.remove(contactItem);
//                    adds the latest message thread
//                    contactItems.add(contactItem);

        } while (cursor.moveToNext());
        return contactItems;
    }

    public static SparseArray<ContactItem> fillIntellibitzContactItemFromCursor(Cursor cursor) {
        SparseArray<ContactItem> contactItems = new SparseArray<>();
        do {
            ContactItem contactItem = new ContactItem();
            contactItem.set_id(cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID)));
            contactItem.setDeviceContactId(cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DEVICE_CONTACTID)));
            contactItem.setDataId(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DATA_ID)));
            contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_INTELLIBITZ_ID)));
            contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_TYPE_ID)));
            contactItem.setDeviceRef(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DEVICE_REF)));
            contactItem.setName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_NAME)));
            contactItem.setFirstName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_FIRST_NAME)));
            contactItem.setLastName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_LAST_NAME)));
            contactItem.setDisplayName(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DISPLAY_NAME)));
            contactItem.setStatus(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_STATUS)));
            contactItem.setProfilePic(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_PIC)));
            contactItem.setCloudPic(cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_CLOUD_PIC)));
            contactItems.put((int) contactItem.get_id(), contactItem);
//            // TODO: 30-06-2016
        } while (cursor.moveToNext());
        return contactItems;
    }

    public static long createOrUpdateIntellibitzContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem intellibitzContactItem) {
//        checks if emails or phones, is in device contacts.. if yes, join device contact
        final String typeId = intellibitzContactItem.getTypeId();
        Cursor cursor = null;
        if (typeId != null) {
            cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                    new String[]{
                            ContactItemColumns.KEY_DEVICE_CONTACTID,
                            ContactItemColumns.KEY_FIRST_NAME,
                            ContactItemColumns.KEY_LAST_NAME,
                            ContactItemColumns.KEY_DISPLAY_NAME,
                            ContactItemColumns.KEY_PIC,
                            ContactItemColumns.KEY_CLOUD_PIC
                    },
                    "TRIM(" + ContactItemColumns.KEY_EMAILS + ")" +
                            " LIKE '%' || ? || '%'  OR " +
                            "TRIM(" + ContactItemColumns.KEY_PHONES + ")" +
                            " LIKE '%' || ? || '%'  OR " +
                            ContactItemColumns.KEY_INTELLIBITZ_ID +
                            " LIKE '%' || ? || '%'  ",
                    new String[]{typeId, typeId, typeId}, null);
        }
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                    new String[]{
                            ContactItemColumns.KEY_DEVICE_CONTACTID,
                            ContactItemColumns.KEY_FIRST_NAME,
                            ContactItemColumns.KEY_LAST_NAME,
                            ContactItemColumns.KEY_DISPLAY_NAME,
                            ContactItemColumns.KEY_PIC,
                            ContactItemColumns.KEY_CLOUD_PIC
                    },
                    ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{intellibitzContactItem.getIntellibitzId()}, null);

        }
        if (cursor != null && cursor.getCount() > 0) {
            long dcid = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DEVICE_CONTACTID));
            String first = cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_FIRST_NAME));
            String last = cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_LAST_NAME));
            String display = cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_DISPLAY_NAME));
            String pic = cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_PIC));
            String cpic = cursor.getString(cursor.getColumnIndex(
                    ContactItemColumns.KEY_CLOUD_PIC));
            cursor.close();
//            if device contact has intellibitz contactables in emails
            intellibitzContactItem.setDeviceContactId(dcid);
            intellibitzContactItem.setFirstName(first);
            intellibitzContactItem.setLastName(last);
            intellibitzContactItem.setDisplayName(display);
            intellibitzContactItem.setProfilePic(pic);
            intellibitzContactItem.setCloudPic(cpic);
        }
        if (cursor != null) cursor.close();
        ContentValues values = new ContentValues();
//        message thread is the parent, email item child
        IntellibitzContactContentProvider.fillContentValuesFromIntellibitzContactItem(intellibitzContactItem, values);
//        checks if the same email component is already present for the message thread
//        Cursor cursor = getAKContactCursorJoin(db, item, id);
        cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
                new String[]{ContactItemColumns.KEY_ID},
                ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                new String[]{intellibitzContactItem.getIntellibitzId()}, null);
        long _id;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            _id = databaseHelper.insert(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT, null, values);
            intellibitzContactItem.set_id(_id);
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            cursor.close();
            intellibitzContactItem.set_id(_id);
//            updateMessageThreadEmail(item, id);
            databaseHelper.update(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
                    values, ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
//        // TODO: 30-06-2016
//        creates or updates intellibitz contact, email and mobile join
/*
        MobileItem selectedMobile = item.getMobileItem();
        if (selectedMobile != null)
            createOrUpdateIntellibitzContactMobileJoin(db, selectedMobile, item.get_id());
        EmailItem selectedEmail = item.getEmailItem();
        if (selectedEmail != null)
            createOrUpdateIntellibitzContactEmailJoin(db, selectedEmail, item.get_id());
*/
        return intellibitzContactItem.get_id();
    }

    @Override
    public boolean onCreate() {
        databaseHelper = DatabaseHelper.newInstance(getContext(),
                DatabaseHelper.DATABASE_NAME);
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case INTELLIBITZCONTACTS_DIR_TYPE:
                return INTELLIBITZCONTACTS_DIR_MIME_TYPE;
            case INTELLIBITZCONTACTS_ITEM_TYPE:
                return INTELLIBITZCONTACTS_ITEM_MIME_TYPE;
            case INTELLIBITZCONTACTS_DATA_ITEM_TYPE:
                return INTELLIBITZCONTACTS_DATA_ITEM_MIME_TYPE;
            case INTELLIBITZCONTACTS_JOIN_DIR_TYPE:
                return INTELLIBITZCONTACTS_JOIN_DIR_MIME_TYPE;
            case INTELLIBITZCONTACTS_JOIN_ITEM_TYPE:
                return INTELLIBITZCONTACTS_JOIN_ITEM_MIME_TYPE;
            case INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE:
                return INTELLIBITZCONTACTS_JOIN_DATA_ITEM_MIME_TYPE;
            case INTELLIBITZCONTACTS_RAW_DIR_TYPE:
                return INTELLIBITZCONTACTS_RAW_DIR_MIME_TYPE;
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
            case INTELLIBITZCONTACTS_DIR_TYPE:
            case INTELLIBITZCONTACTS_ITEM_TYPE:
            case INTELLIBITZCONTACTS_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_INTELLIBITZCONTACT,
                            projection,
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
            case INTELLIBITZCONTACTS_JOIN_DIR_TYPE:
            case INTELLIBITZCONTACTS_JOIN_ITEM_TYPE:
            case INTELLIBITZCONTACTS_JOIN_DATA_ITEM_TYPE:
                try {
/*
                    cursor = databaseHelper.getAllIntellibitzContactsJoinCursor(
                            projection, selection, selectionArgs, sortOrder);
*/
                    cursor = databaseHelper.query(TABLE_INTELLIBITZCONTACT,
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
            case INTELLIBITZCONTACTS_RAW_DIR_TYPE:
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
        switch (URI_MATCHER.match(uri)) {
            case INTELLIBITZCONTACTS_DIR_TYPE:
                try {
                    Set<ContactItem> deviceContactItems = (Set<ContactItem>) MainApplicationSingleton.Serializer.deserialize(vals1);
                    if (null == deviceContactItems) {
                        return null;
                    }
                    long[] ids = DeviceContactContentProvider.createDeviceContact(databaseHelper, deviceContactItems);
                    if (null == ids || ids.length != deviceContactItems.size()) {
                        return null;
                    }
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(uri, null);
                    }
                    return uri;
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case INTELLIBITZCONTACTS_ITEM_TYPE:
                try {
                    ContactItem item = (ContactItem) MainApplicationSingleton.Serializer.deserialize(vals1);
                    long id = DeviceContactContentProvider.createOrUpdateDeviceContact(databaseHelper, item);
                    Uri insertUri = ContentUris.withAppendedId(
                            IntellibitzContactContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(insertUri, null);
//                    context.getContentResolver().notifyChange(uri, null);
                    }
                    return insertUri;
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
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
            case INTELLIBITZCONTACTS_DIR_TYPE:
            case INTELLIBITZCONTACTS_ITEM_TYPE:
                try {
                    id = databaseHelper.update(DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                            values,
                            selection, selectionArgs);
                    updateUri = ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, id);
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case INTELLIBITZCONTACTS_JOIN_DIR_TYPE:
            case INTELLIBITZCONTACTS_JOIN_ITEM_TYPE:
                try {
                    byte[] vals1 = values.getAsByteArray(ContactItem.DEVICE_CONTACT);
                    ContactItem item = (ContactItem) MainApplicationSingleton.Serializer.deserialize(vals1);
                    id = (int) DeviceContactContentProvider.updateDeviceContactJoin(databaseHelper, item);
                    updateUri = ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, id);
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
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
            case INTELLIBITZCONTACTS_DIR_TYPE:
            case INTELLIBITZCONTACTS_ITEM_TYPE:
                try {
                    int id = databaseHelper.delete(DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                            selection, selectionArgs);
                    Uri insertUri = ContentUris.withAppendedId(IntellibitzContactContentProvider.CONTENT_URI, id);
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


/*
    public Cursor getAllDeviceContactsJoinCursor(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  " +
                        "ct._id as ct_id, ct.id as ctid, ct.version as ctversion, ct.contact_id as ctcid, " +
                        " ct.device_ref as ctdr, ct.name as ctname,  ct.first_name as ctfn, " +
                        " ct.last_name as ctln, ct.companyName as ctcomp, ct.profile_pic as ctppic, " +
                        " e._id as e_id, e.email as email, e.id as eid, " +
                        " m._id as m_id,  m.id as mid, m.intellibitz_id as makid,  " +
                        " m.mobile as mobile, m.name as mname, m.profile_pic as mppic, " +
                        " m.status as mst,  ct.status as ctst " +
                        "FROM  " +
                        DeviceContactContentProvider.TABLE_DEVICECONTACTS +
*/
/*
                        " ct left outer join " + TABLE_CONTACTS_EMAILS_JOIN +
                        " cte on ct.[_id] = cte.[" + ContactEmailJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + TABLE_EMAILS +
                        " e on e.[_id] = cte.[" + ContactEmailJoinColumns.KEY_EMAIL_ID + "] " +
                        "left outer join " + TABLE_CONTACTS_MOBILES_JOIN +
                        " ctm on ct.[_id] = ctm.[" + ContactMobileJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + TABLE_MOBILES +
                        " m on m.[_id] = ctm.[" + ContactMobileJoinColumns.KEY_MOBILE_ID + "] " +
*//*

//                        " WHERE ct." + MessageItemColumns.KEY_ID + " = " + id
                        "";
        if (selection != null) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
        Log.e(TAG, selectQuery);
        return rawQuery(selectQuery, selectionArgs);
    }

    public Cursor getAllIntellibitzContactsJoinCursor(
            String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  " +
                        "ct._id as ct_id, ct.id as ctid, ct.intellibitz_id as ctcid, ct.type as cttype, " +
                        " ct.device_ref as ctdr, ct.name as ctname,  ct.status as ctst, " +
                        " ct.profile_pic as ctppic, " +
                        " e._id as e_id, e.email as email, e.id as eid, " +
                        " m._id as m_id,  m.id as mid, m.intellibitz_id as makid,  " +
                        " m.mobile as mobile, m.name as mname, m.profile_pic as mppic, " +
                        " m.status as mst " +
                        " FROM  " +
                        IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT +
*/
/*
                        " ct left outer join " + TABLE_INTELLIBITZCONTACT_EMAIL_JOIN +
                        " cte on ct.[_id] = cte.[" + IntellibitzContactEmailJoinColumns.KEY_INTELLIBITZCONTACT_ID + "] " +
                        "left outer join " + TABLE_EMAILS +
                        " e on e.[_id] = cte.[" + IntellibitzContactEmailJoinColumns.KEY_EMAIL_ID + "] " +
                        "left outer join " + TABLE_INTELLIBITZCONTACT_MOBILE_JOIN +
                        " ctm on ct.[_id] = ctm.[" + IntellibitzContactMobileJoinColumns.KEY_INTELLIBITZCONTACT_ID + "] " +
                        "left outer join " + TABLE_MOBILES +
                        " m on m.[_id] = ctm.[" + IntellibitzContactMobileJoinColumns.KEY_MOBILE_ID + "] " +
*//*

                        "";
        if (selection != null) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
        Log.e(TAG, selectQuery);
        return rawQuery(selectQuery, selectionArgs);
    }
*/

}
