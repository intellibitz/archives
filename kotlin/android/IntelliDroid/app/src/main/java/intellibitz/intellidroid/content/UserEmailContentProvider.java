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
import intellibitz.intellidroid.db.IntellibitzItemColumns;
import intellibitz.intellidroid.db.UserEmailJoinColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.IntellibitzItemColumns;
import intellibitz.intellidroid.db.UserEmailJoinColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *
 */
public class UserEmailContentProvider extends
        ContentProvider {

    public static final String TAG = "UserEmailCP";
    //    The content provider scheme
    public static final String SCHEME = "content://";
    //    users email accounts
    public static final String TABLE_USEREMAILS = "useremail";
    // MIME types used for searching words or looking up a single definition
    public static final String USERS_EMAIL_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String USERS_EMAIL_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String USERS_EMAIL_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String CREATE_TABLE_USERS_EMAILS_JOIN = "CREATE TABLE "
            + UserContentProvider.TABLE_USERS_EMAILS_JOIN + "( " +
            UserEmailJoinColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            UserEmailJoinColumns.KEY_EMAIL_ID + " INTEGER," +
            UserEmailJoinColumns.KEY_USER_ID + " INTEGER," +
            UserEmailJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    public static final String CREATE_TABLE_USEREMAILS = "CREATE TABLE "
            + TABLE_USEREMAILS + ContactItemColumns.TABLE_CONTACTS_SCHEMA;
    // UriMatcher stuff
    private static final int USERS_EMAIL_DIR_TYPE = 0;
    private static final int USERS_EMAIL_ITEM_TYPE = 1;
    private static final int USERS_EMAIL_DATA_ITEM_TYPE = 2;
    private static final int SEARCH_SUGGEST = 3;
    private static final int REFRESH_SHORTCUT = 4;
    public static String AUTHORITY = "intellibitz.intellidroid.content.UserEmailContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_USEREMAILS);
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_USEREMAILS, USERS_EMAIL_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_USEREMAILS +
                "/#", USERS_EMAIL_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_USEREMAILS +
                "/*", USERS_EMAIL_DATA_ITEM_TYPE);
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

    public static ContentValues fillContentValuesFromUserEmailItem(ContactItem userEmailItem,
                                                                   ContentValues values) {
        DeviceContactContentProvider.fillContentValuesFromDeviceContactItem(userEmailItem, values);
        MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_EMAIL, userEmailItem.getEmail());
        MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_EMAIL_CODE, userEmailItem.getEmailCode());
        MainApplicationSingleton.fillIfNotNull(values, ContactItemColumns.KEY_ACTIVE, userEmailItem.getActive());
        return values;
    }

    public static Cursor getUserEmailsCursor(DatabaseHelper databaseHelper, ContactItem item) {
        return databaseHelper.query(TABLE_USEREMAILS, new String[]{UserEmailJoinColumns.KEY_ID},
                UserEmailJoinColumns.KEY_EMAIL + " = ? and " +
                        UserEmailJoinColumns.KEY_NAME + " = ?",
                new String[]{item.getEmail(), item.getName()}, null, null, null);
    }

    public static long createOrUpdateUserEmail(DatabaseHelper databaseHelper, ContactItem item, long id) {
        ContentValues values = new ContentValues();
        fillContentValuesFromUserEmailItem(item, values);
//        Cursor cursor = getAllUserEmailsCursor(item, id);
        Cursor cursor = getUserEmailsCursor(databaseHelper, item);
        long _id;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            _id = databaseHelper.insert(TABLE_USEREMAILS, null, values);
//            Log.d(TAG, "EmailItem: " + _id + " User: " + id);
            item.set_id(_id);
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(UserEmailJoinColumns.KEY_ID));
            item.set_id(_id);
            cursor.close();
            databaseHelper.update(TABLE_USEREMAILS, values, UserEmailJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
//        creates the join
        createOrUpdateUserEmailJoin(databaseHelper, _id, id);
        return item.get_id();
    }

    public static long getUserEmailJoin(DatabaseHelper databaseHelper, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(UserContentProvider.TABLE_USERS_EMAILS_JOIN, new String[]{IntellibitzItemColumns.KEY_ID},
                UserEmailJoinColumns.KEY_EMAIL_ID + " = ? and " +
                        UserEmailJoinColumns.KEY_USER_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(fk)}, null, null, null);
        if (c != null && c.getCount() > 0) {
            _id = c.getLong(c.getColumnIndex("_id"));
            c.close();
        }
        return _id;
    }

    public static long createOrUpdateUserEmailJoin(DatabaseHelper databaseHelper, long id, long fk) {
        long _id = getUserEmailJoin(databaseHelper, id, fk);
        ContentValues values = new ContentValues();
        values.put(UserEmailJoinColumns.KEY_EMAIL_ID, id);
        values.put(UserEmailJoinColumns.KEY_USER_ID, fk);
        values.put(UserEmailJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(UserContentProvider.TABLE_USERS_EMAILS_JOIN, null, values);
        } else {
            databaseHelper.update(UserContentProvider.TABLE_USERS_EMAILS_JOIN, values,
                    UserEmailJoinColumns.KEY_ID + " = ?", new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long[] createOrUpdateUserEmails(DatabaseHelper databaseHelper, Set<ContactItem> items, long id) {
        if (null == items) return new long[0];
        long[] ids = new long[items.size()];
        int i = 0;
        for (ContactItem email : items) {
            ids[i++] = createOrUpdateUserEmail(databaseHelper, email, id);
        }
        return ids;
    }

    public static Cursor getUserEmailsJoin(DatabaseHelper databaseHelper, long id) {
        String selectQuery = "SELECT  *, u._id as u_id, u.name as uname, em._id as emid, em.name as emname, em.type as emtype FROM " +
                UserContentProvider.TABLE_USERS + " u " +
                " left join " + UserContentProvider.TABLE_USERS_EMAILS_JOIN +
                " uem on u.[_id] = uem.[user_id]  " +
                " left join " + TABLE_USEREMAILS + " em  on uem.[email_id] = em.[_id] " +
                " WHERE u." + ContactItemColumns.KEY_ID + " = " + id + "";

/*
        String selectQuery = "SELECT  * FROM " + TABLE_MSGTHREADEMAILS + " WHERE "
                + UserEmailJoinColumns.KEY_ID + " = " + id;
*/
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, null);
    }

    public static Cursor getUserEmailsJoin(DatabaseHelper databaseHelper, String id) {
/*

SELECT  * FROM users mt
left join users_emails  mte on mt.[_id] = mte.[user_id]
left join emails em  on mte.[email_id] = em.[_id]
WHERE mt.id = 'USRMASTER_919840348914'

        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " mt, " +
                TABLE_MSGTHREADEMAILS + " tm, " + TABLE_USERS_EMAILS_JOIN +
                " ttm WHERE mt." +
                ContactItemColumns.KEY_DATA_ID + " = '" + id +
                "'" + " AND tm." + UserEmailJoinColumns.KEY_ID + " = " + "ttm." +
                UserEmailJoinColumns.KEY_EMAIL_ID + " AND mt." +
                ContactItemColumns.KEY_ID +
                " = " + "ttm." + UserEmailJoinColumns.KEY_USER_ID;
*/
        String selectQuery = "SELECT  *, u._id as u_id, u.name as uname, " +
                " em._id as emid, em.name as emname, em.type as emtype FROM " +
                UserContentProvider.TABLE_USERS +
                " u  left join " + UserContentProvider.TABLE_USERS_EMAILS_JOIN +
                " uem on u.[_id] = uem.[user_id]  " +
                " left join " + TABLE_USEREMAILS +
                " em  on uem.[email_id] = em.[_id]  WHERE u." +
                ContactItemColumns.KEY_DATA_ID + " = '" + id + "'";


//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, null);
    }

    public static void removeUserEmail(DatabaseHelper databaseHelper, long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.delete(TABLE_USEREMAILS, UserEmailJoinColumns.KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(UserContentProvider.TABLE_USERS_EMAILS_JOIN,
                UserEmailJoinColumns.KEY_EMAIL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public static ContactItem populateUserEmailsJoinByDataId(ContactItem user, Context context) {
        String dataId = user.getDataId();
        Uri uri = CONTENT_URI;
        String sel = null;
        String[] selArgs = null;
        if (!TextUtils.isEmpty(dataId)) {
            uri = Uri.withAppendedPath(CONTENT_URI, dataId);
            sel = ContactItemColumns.KEY_DATA_ID + " = ? ";
            selArgs = new String[]{dataId};
        }
//        the above selection and selection args has no effect, since cp does customized query
//        // TODO: 1/9/16
//        to change cp to accept sel and selargs
        Cursor cursor = context.getContentResolver().query(uri, null, sel, selArgs, null);
        return UserContentProvider.packUserEmailsFromCursor(cursor, user);
    }

    public static ContactItem populateUserEmailsJoinById(ContactItem user, Context context) {
        long id = user.get_id();
        Uri uri = CONTENT_URI;
        String sel = null;
        String[] selArgs = null;
        if (id > 0) {
            uri = ContentUris.withAppendedId(CONTENT_URI, id);
            sel = ContactItemColumns.KEY_ID + " = ? ";
            selArgs = new String[]{String.valueOf(id)};
        }
//        the above selection and selection args has no effect, since cp does customized query
//        // TODO: 1/9/16
//        to change cp to accept sel and selargs
        Cursor cursor = context.getContentResolver().query(uri, null, sel, selArgs, null);
        return UserContentProvider.packUserEmailsFromCursor(cursor, user);
    }

    public static Uri savesUserEmailsInDB(ContactItem user, Context context) throws IOException {
        ContentValues values = new ContentValues();
        values.put(ContactItem.USER_CONTACT, MainApplicationSingleton.Serializer.serialize(user));
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.insert(CONTENT_URI, values);
    }

    public static int deletesUserEmail(long emailId, Context context) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, emailId);
        return context.getContentResolver().delete(uri, null, null);
    }

    public static int deletesUserEmail(String email, Context context) {
        Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                new String[]{UserEmailJoinColumns.KEY_ID},
                UserEmailJoinColumns.KEY_DATA_ID + " = ? OR " +
                        ContactItemColumns.KEY_EMAIL + " = ? ",
                new String[]{email, email}, null);
        if (null == cursor) return 0;
        int count = 0;
        cursor.moveToFirst();
        do {
            long id = cursor.getInt(cursor.getColumnIndex(UserEmailJoinColumns.KEY_ID));
            count += deletesUserEmail(id, context);
        } while (cursor.moveToNext());
        cursor.close();
        return count;
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
            case USERS_EMAIL_DIR_TYPE:
                return USERS_EMAIL_DIR_MIME_TYPE;
            case USERS_EMAIL_ITEM_TYPE:
                return USERS_EMAIL_ITEM_MIME_TYPE;
            case USERS_EMAIL_DATA_ITEM_TYPE:
                return USERS_EMAIL_DATA_ITEM_MIME_TYPE;
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
        switch (sURIMatcher.match(uri)) {
            case USERS_EMAIL_DIR_TYPE:
                try {
//                    String selectQuery = "SELECT  * FROM " + UserEmailContentProvider.TABLE_USEREMAILS;
//        Log.e(TAG, selectQuery);
//                    cursor = databaseHelper.rawQuery(selectQuery, null);
                    cursor = databaseHelper.query(TABLE_USEREMAILS,
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
            case USERS_EMAIL_ITEM_TYPE:
                try {
                    cursor = getUserEmailsJoin(databaseHelper, ContentUris.parseId(uri));
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
            case USERS_EMAIL_DATA_ITEM_TYPE:
                try {
                    cursor = getUserEmailsJoin(databaseHelper, uri.getLastPathSegment());
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
            case USERS_EMAIL_DIR_TYPE:
            case USERS_EMAIL_ITEM_TYPE:
                try {
                    // Use the UriMatcher to see what kind of query we have and format the db query accordingly
                    ContactItem user = (ContactItem) MainApplicationSingleton.Serializer.deserialize(
                            values.getAsByteArray(ContactItem.USER_CONTACT));
                    Set<ContactItem> emails = user.getContactItems();
                    if (emails != null && !emails.isEmpty()) {
                        long[] ids = createOrUpdateUserEmails(databaseHelper, emails, user.get_id());
                        Context context = getContext();
                        if (null != context) {
                            context.getContentResolver().notifyChange(uri, null);
                        }
                        return ContentUris.withAppendedId(UserEmailContentProvider.CONTENT_URI,
                                ids[0]);
                    }
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case USERS_EMAIL_DATA_ITEM_TYPE:
                try {
                    // Use the UriMatcher to see what kind of query we have and format the db query accordingly
                    ContactItem user = (ContactItem) MainApplicationSingleton.Serializer.deserialize(
                            values.getAsByteArray(ContactItem.USER_CONTACT));
                    long id = createOrUpdateUserEmail(databaseHelper,
                            user.getEmail(user.getEmail()), user.get_id());
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(uri, null);
                    }
                    return ContentUris.withAppendedId(UserEmailContentProvider.CONTENT_URI, id);
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
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (sURIMatcher.match(uri)) {
            case USERS_EMAIL_DIR_TYPE:
            case USERS_EMAIL_ITEM_TYPE:
                try {
                    removeUserEmail(databaseHelper, ContentUris.parseId(uri));
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(uri, null);
                    }
                    return 1;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
