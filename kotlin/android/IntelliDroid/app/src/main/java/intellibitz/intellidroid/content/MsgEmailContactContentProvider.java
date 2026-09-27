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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static intellibitz.intellidroid.content.DeviceContactContentProvider.createDeviceContact;
import static intellibitz.intellidroid.content.DeviceContactContentProvider.createOrUpdateDeviceContact;
import static intellibitz.intellidroid.content.DeviceContactContentProvider.getDeviceContactsJoin;
import static intellibitz.intellidroid.content.DeviceContactContentProvider.updateDeviceContactJoin;

/**
 *
 */
public class MsgEmailContactContentProvider extends
        ContentProvider {

    public static final String TAG = "MsgEmailContactCP";
    public static final String TABLE_MSGEMAILCONTACT = "msgemailcontact";
    public static final String TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN = "msgemailcontact_intellibitzcontacts";
    public static final String CREATE_TABLE_MSGEMAILCONTACT = "CREATE TABLE "
            + TABLE_MSGEMAILCONTACT + ContactItemColumns.TABLE_CONTACTS_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String CONTACT_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String CONTACT_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String CONTACT_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String CONTACT_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/all";
    public static final String CONTACT_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String CONTACT_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String CONTACT_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    public static final String CREATE_TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN = "CREATE TABLE "
            + TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN + "( " +
            ContactIntellibitzContactJoinColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + " INTEGER," +
            ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID + " INTEGER," +
            ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    // UriMatcher stuff
    private static final int CONTACT_DIR_TYPE = 0;
    private static final int CONTACT_ITEM_TYPE = 1;
    private static final int CONTACT_DATA_ITEM_TYPE = 2;
    private static final int CONTACT_JOIN_DIR_TYPE = 3;
    private static final int CONTACT_JOIN_ITEM_TYPE = 4;
    private static final int CONTACT_JOIN_DATA_ITEM_TYPE = 5;
    private static final int CONTACT_RAW_DIR_TYPE = 6;
    private static final int SEARCH_SUGGEST = 7;
    private static final int REFRESH_SHORTCUT = 8;
    public static String AUTHORITY = "intellibitz.intellidroid.content.MsgEmailContactContentProvider";
    //    to get only pure contacts minus join
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_MSGEMAILCONTACT);
    //    to execute contacts with join.. example with mobiles and emails
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGEMAILCONTACT);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGEMAILCONTACT);
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACT, CONTACT_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACT +
                "/#", CONTACT_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACT +
                "/*", CONTACT_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGEMAILCONTACT, CONTACT_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGEMAILCONTACT +
                        "/#", CONTACT_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGEMAILCONTACT +
                        "/*", CONTACT_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_MSGEMAILCONTACT, CONTACT_RAW_DIR_TYPE);
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

    public static ContentValues fillContentValuesFromContactItem(
            ContactItem item, ContentValues values) {
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
                ContactItemColumns.KEY_TIMESTAMP, item.getTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATETIME,
                MainApplicationSingleton.getDateTimeMillis(item.getTimestamp()));
        return values;
    }

    public static int updatesTypeInDB(ContactItem contactItem, Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactItemColumns.KEY_TYPE, contactItem.getType());
        return context.getApplicationContext().getContentResolver().update(CONTENT_URI,
                contentValues, ContactItemColumns.KEY_DATA_ID + " = ?",
                new String[]{contactItem.getDataId()});
    }

    public static long queryContactForDBId(ContactItem contactItem, Context context) {
        Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                new String[]{ContactItemColumns.KEY_ID},
                ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                new String[]{contactItem.getDataId()}, null);
        if (null == cursor || 0 == cursor.getCount()) return 0;
        long _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
        cursor.close();
        contactItem.set_id(_id);
        return _id;
    }

    public static long createOrUpdateIntellibitzContactByContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem contactItem) {
//        checks if emails, or in device contacts.. if yes, join device contact
        long id = 0;
        Cursor cursor = databaseHelper.query(db, DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                new String[]{ContactItemColumns.KEY_ID},
                ContactItemColumns.KEY_EMAILS + " LIKE '%' || ? || '%' ",
                new String[]{contactItem.getIntellibitzId()}, null);
        if (cursor != null && cursor.getCount() > 0) {
            id = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            cursor.close();
        }
        if (id > 0) {
//            if device contact has intellibitz contactables in emails
            contactItem.setDevice(true);
        }
// updates from the device contact info, will go into the contact as well
// the intellibitz contact update.. below method will take care of keeping intellibitz contact in sync with device contact
        long _id = IntellibitzContactContentProvider.createOrUpdateIntellibitzContact(
                databaseHelper, db, contactItem);
        if (id > 0) {
//            if device contact has intellibitz contactables in emails
            IntellibitzContactContentProvider.createOrUpdateIntellibitzContactDeviceContactJoin(
                    databaseHelper, db, _id, id);
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
        return contactItem.get_id();
    }

    public static long createOrUpdateContactIntellibitzContactByContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem contactItem, long id) {
//            ContactItem contactItem = item.getIntellibitzContactItem();
        if (null == contactItem) return 0;
//        // TODO: 04-07-2016
//        contact item to be updated, with intellibitz contact, device contact details earlier than this
//        reverse the update order.. fetch the device contacts, then update contact item
        Cursor cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
//                projection
                new String[]{
                        ContactItemColumns.KEY_ID,
                        ContactItemColumns.KEY_DEVICE_CONTACTID,
                        ContactItemColumns.KEY_FIRST_NAME,
                        ContactItemColumns.KEY_LAST_NAME,
                        ContactItemColumns.KEY_DISPLAY_NAME,
                        ContactItemColumns.KEY_PIC,
                        ContactItemColumns.KEY_CLOUD_PIC
                },
//                selection
                ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
//                selection args
                new String[]{contactItem.getIntellibitzId()},
                null);
        if (null == cursor || 0 == cursor.getCount()) {
            long _id = createOrUpdateIntellibitzContactByContact(databaseHelper, db, contactItem);
            return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id);
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
            long did = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_DEVICE_CONTACTID));
            String first = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_FIRST_NAME));
            String last = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_LAST_NAME));
            String disp = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_DISPLAY_NAME));
            String pic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_PIC));
            String cloudPic = cursor.getString(cursor.getColumnIndex(ContactItemColumns.KEY_CLOUD_PIC));
            cursor.close();
//            // TODO: 04-07-2016
//            too late.. do it earlier and save the contact
//            the hack is for msg thread groups.. so it can get device contact from contact item
            contactItem.setDeviceContactId(did);
            if (!TextUtils.isEmpty(first))
                contactItem.setFirstName(first);
            if (!TextUtils.isEmpty(last))
                contactItem.setLastName(last);
            if (!TextUtils.isEmpty(disp))
                contactItem.setDisplayName(disp);
            if (!TextUtils.isEmpty(disp))
                contactItem.setName(disp);
            if (!TextUtils.isEmpty(pic))
                contactItem.setProfilePic(pic);
            if (!TextUtils.isEmpty(cloudPic))
                contactItem.setCloudPic(cloudPic);
            return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id);
        }
    }

    public static Cursor getContactIntellibitzContactCursorJoin(DatabaseHelper databaseHelper,
                                                                SQLiteDatabase db, ContactItem item, long id) {
        String[] args = new String[]{item.getDataId(), String.valueOf(id)};
        String selectQuery = "SELECT  * FROM " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT + " nt " +
                " left join " + TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN +
                " ntm on nt.[" + ContactIntellibitzContactJoinColumns.KEY_ID +
                "] = ntm.[" + ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID + "]  " +
                " left join " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT + " mt on ntm.[" +
                ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID +
                "] = mt.[" + ContactItemColumns.KEY_ID + "] " +
                " WHERE " + " mt." + ContactItemColumns.KEY_ID + " = ? " +
                " AND nt." + ContactItemColumns.KEY_ID + " = ? ";
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(db, selectQuery, args);
    }

    public static long createOrUpdateContactIntellibitzContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = getContactIntellibitzContactJoin(databaseHelper, db, id, fk);
        ContentValues values = new ContentValues();
        values.put(ContactIntellibitzContactJoinColumns.KEY_INTELLIBITZCONTACT_ID, id);
        values.put(ContactIntellibitzContactJoinColumns.KEY_CONTACT_ID, fk);
        values.put(ContactIntellibitzContactJoinColumns.KEY_TIMESTAMP,
                MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db, TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN, null, values);
        } else {
            _id = databaseHelper.update(db,
                    TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN, values,
                    ContactIntellibitzContactJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long getContactIntellibitzContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(db, TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN,
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
            case CONTACT_DIR_TYPE:
                return CONTACT_DIR_MIME_TYPE;
            case CONTACT_ITEM_TYPE:
                return CONTACT_ITEM_MIME_TYPE;
            case CONTACT_DATA_ITEM_TYPE:
                return CONTACT_DATA_ITEM_MIME_TYPE;
            case CONTACT_JOIN_DIR_TYPE:
                return CONTACT_JOIN_DIR_MIME_TYPE;
            case CONTACT_JOIN_ITEM_TYPE:
                return CONTACT_JOIN_ITEM_MIME_TYPE;
            case CONTACT_JOIN_DATA_ITEM_TYPE:
                return CONTACT_JOIN_DATA_ITEM_MIME_TYPE;
            case CONTACT_RAW_DIR_TYPE:
                return CONTACT_RAW_DIR_MIME_TYPE;
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
            case CONTACT_DIR_TYPE:
            case CONTACT_JOIN_DIR_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_MSGEMAILCONTACT,
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
            case CONTACT_ITEM_TYPE:
            case CONTACT_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_MSGEMAILCONTACT,
                            projection,
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
            case CONTACT_JOIN_ITEM_TYPE:
                try {
                    cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, ContentUris.parseId(uri));
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
            case CONTACT_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, uri.getLastPathSegment());
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
            case CONTACT_RAW_DIR_TYPE:
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
            case CONTACT_DIR_TYPE:
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
            case CONTACT_ITEM_TYPE:
                try {
                    ContactItem item = (ContactItem) MainApplicationSingleton.Serializer.deserialize(vals1);
                    long id = DeviceContactContentProvider.createOrUpdateDeviceContact(databaseHelper, item);
                    Uri insertUri = ContentUris.withAppendedId(
                            MsgEmailContactContentProvider.CONTENT_URI, id);
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
            case CONTACT_DIR_TYPE:
            case CONTACT_ITEM_TYPE:
                try {
                    id = databaseHelper.update(TABLE_MSGEMAILCONTACT,
                            values,
                            selection, selectionArgs);
                    updateUri = ContentUris.withAppendedId(MsgEmailContactContentProvider.CONTENT_URI, id);
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CONTACT_JOIN_DIR_TYPE:
            case CONTACT_JOIN_ITEM_TYPE:
                try {
                    byte[] vals1 = values.getAsByteArray(ContactItem.DEVICE_CONTACT);
                    ContactItem item = (ContactItem) MainApplicationSingleton.Serializer.deserialize(vals1);
                    id = (int) DeviceContactContentProvider.updateDeviceContactJoin(databaseHelper, item);
                    updateUri = ContentUris.withAppendedId(MsgEmailContactContentProvider.CONTENT_URI, id);
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
            case CONTACT_DIR_TYPE:
            case CONTACT_ITEM_TYPE:
                try {
                    int id = databaseHelper.delete(TABLE_MSGEMAILCONTACT,
                            selection, selectionArgs);
                    Uri delUri = ContentUris.withAppendedId(MsgEmailContactContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(delUri, null);
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

/*
    public static long createOrUpdateContactIntellibitzContactJoin(DatabaseHelper databaseHelper,
                                                              SQLiteDatabase db, ContactItem intellibitzContactItem, long id) {
//            ContactItem contactItem = item.getIntellibitzContactItem();
        if (null == intellibitzContactItem) return 0;
        Cursor cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
//                projection
                new String[]{IntellibitzContactContentProvider.ContactItemColumns.KEY_ID},
//                selection
                ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
//                selection args
                new String[]{intellibitzContactItem.getIntellibitzId()},
                null);
        if (null == cursor || 0 == cursor.getCount()) {
            long _id = IntellibitzContactContentProvider.createOrUpdateIntellibitzContact(
                    databaseHelper, db, intellibitzContactItem);
            return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id);
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    IntellibitzContactContentProvider.ContactItemColumns.KEY_ID));
            cursor.close();
            return createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, _id, id);
        }
    }

    public static long createOrUpdateContactIntellibitzContactJoin(DatabaseHelper databaseHelper,
                                                              SQLiteDatabase db, ContactItem item, long id) {
        Cursor cursor = getContactIntellibitzContactCursorJoin(databaseHelper, db, item, id);
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(db, IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
                    new String[]{ContactItemColumns.KEY_ID},
                    ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{item.getIntellibitzId()}, null);
            if (null != cursor && 0 != cursor.getCount()) {
                _id = cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
        }
        if (_id != 0) {
//        creates the join
            createOrUpdateContactIntellibitzContactJoin(databaseHelper, db, id, _id);
        }
        return item.get_id();
    }
*/

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
    public Cursor getAllGrpContactAKGrpContactsCursor(ContactItem item, long id) {
*/
/*
        SELECT  * FROM msg_threads mt
left join msg_threads_emails mte on mt.[_id] = mte.[msg_thread_id]
left join emails e
where e.name = 'Jeffrey Roshan' and e.email = 'jeff@intellibitz.com' and e.type='to'
and mt._id = '11'
         *//*

        String[] args = new String[]{item.getIntellibitzId(), String.valueOf(id)};
        String selectQuery = "SELECT  * FROM " + ContactsContentProvider.TABLE_MSGEMAILCONTACTS + " ct " +
                " left join " + TABLE_MSGEMAILCONTACTS_CONTACT_JOIN +
                " cte on ct.[_id] = cte.[" + DatabaseHelper.ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID + "]  " +
                " left join " + ContactContentProvider.TABLE_MSGEMAILCONTACT + " em on cte.[" +
                DatabaseHelper.ContactsContactJoinColumns.KEY_CONTACT_ID + "] = em.[_id] " +
                " WHERE " + " em." + ContactsContentProvider.ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? " +
                " AND ct." + ContactsContentProvider.ContactItemColumns.KEY_ID + " = ? ";

//        Log.e(TAG, selectQuery);
        return rawQuery(selectQuery, args);
    }
*/


}
