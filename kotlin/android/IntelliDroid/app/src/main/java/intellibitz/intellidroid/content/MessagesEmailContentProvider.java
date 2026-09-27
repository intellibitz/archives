package intellibitz.intellidroid.content;

import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.io.IOException;
import java.util.Collection;

import static intellibitz.intellidroid.content.MessageEmailContentProvider.createOrUpdateMessages;

public class MessagesEmailContentProvider extends
        ContentProvider {

    public static final String TAG = "MessagesEmailCP";

    //    private static final String TABLE_USERS_DEVICES = "users_devices";

    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String MESSAGESEMAIL_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String MESSAGESEMAIL_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join";
    public static final String MESSAGESEMAIL_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String MESSAGESEMAIL_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String MESSAGESEMAIL_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String MESSAGESEMAIL_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String MESSAGESEMAIL_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    // UriMatcher stuff
    private static final int MESSAGESEMAIL_DIR_TYPE = 1;
    private static final int MESSAGESEMAIL_JOIN_DIR_TYPE = 2;
    private static final int MESSAGESEMAIL_ITEM_TYPE = 3;
    private static final int MESSAGESEMAIL_DATA_ITEM_TYPE = 4;
    private static final int MESSAGESEMAIL_JOIN_ITEM_TYPE = 5;
    private static final int MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE = 6;
    private static final int MESSAGESEMAIL_RAW_DIR_TYPE = 7;
    private static final int SEARCH_SUGGEST = 8;
    private static final int REFRESH_SHORTCUT = 9;
    public static String AUTHORITY = "intellibitz.intellidroid.content.MessagesEmailContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + MessageEmailContentProvider.TABLE_MESSAGESEMAIL);
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + MessageEmailContentProvider.TABLE_MESSAGESEMAIL);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + MessageEmailContentProvider.TABLE_MESSAGESEMAIL);
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, MessageEmailContentProvider.TABLE_MESSAGESEMAIL,
                MESSAGESEMAIL_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + MessageEmailContentProvider.TABLE_MESSAGESEMAIL,
                MESSAGESEMAIL_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY, MessageEmailContentProvider.TABLE_MESSAGESEMAIL +
                "/#", MESSAGESEMAIL_ITEM_TYPE);
        matcher.addURI(AUTHORITY, MessageEmailContentProvider.TABLE_MESSAGESEMAIL +
                "/*", MESSAGESEMAIL_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + MessageEmailContentProvider.TABLE_MESSAGESEMAIL +
                        "/#", MESSAGESEMAIL_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + MessageEmailContentProvider.TABLE_MESSAGESEMAIL +
                        "/*", MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + MessageEmailContentProvider.TABLE_MESSAGESEMAIL, MESSAGESEMAIL_RAW_DIR_TYPE);
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
            case MESSAGESEMAIL_DIR_TYPE:
                return MESSAGESEMAIL_DIR_MIME_TYPE;
            case MESSAGESEMAIL_JOIN_DIR_TYPE:
                return MESSAGESEMAIL_JOIN_DIR_MIME_TYPE;
            case MESSAGESEMAIL_ITEM_TYPE:
                return MESSAGESEMAIL_ITEM_MIME_TYPE;
            case MESSAGESEMAIL_DATA_ITEM_TYPE:
                return MESSAGESEMAIL_DATA_ITEM_MIME_TYPE;
            case MESSAGESEMAIL_JOIN_ITEM_TYPE:
                return MESSAGESEMAIL_JOIN_ITEM_MIME_TYPE;
            case MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE:
                return MESSAGESEMAIL_JOIN_DATA_ITEM_MIME_TYPE;
            case MESSAGESEMAIL_RAW_DIR_TYPE:
                return MESSAGESEMAIL_RAW_DIR_MIME_TYPE;
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
//        String id = uri.getLastPathSegment();
        switch (URI_MATCHER.match(uri)) {
            case MESSAGESEMAIL_DIR_TYPE:
            case MESSAGESEMAIL_ITEM_TYPE:
            case MESSAGESEMAIL_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(MessageEmailContentProvider.TABLE_MESSAGESEMAIL,
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
            case MESSAGESEMAIL_JOIN_DIR_TYPE:
                try {
                    cursor = MessageEmailContentProvider.getMessagesThreadShalGroupJoin(databaseHelper,
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
            case MESSAGESEMAIL_JOIN_ITEM_TYPE:
            case MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = MessageEmailContentProvider.getMessagesThreadJoin(databaseHelper,
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
            case MESSAGESEMAIL_RAW_DIR_TYPE:
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
        byte[] vals = values.getAsByteArray(MessageItem.TAG);
        switch (URI_MATCHER.match(uri)) {
            case MESSAGESEMAIL_DIR_TYPE:
            case MESSAGESEMAIL_JOIN_DIR_TYPE:
                try {
                    Collection<MessageItem> items = (Collection<MessageItem>)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    if (null == items) {
                        return null;
                    }
                    long[] ids = MessageEmailContentProvider.createOrUpdateMessages(databaseHelper, items);
                    if (null == ids || ids.length != items.size()) {
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
            case MESSAGESEMAIL_ITEM_TYPE:
            case MESSAGESEMAIL_DATA_ITEM_TYPE:
                try {
                    MessageItem item = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    long id = MessageEmailContentProvider.createOrUpdateMessages(databaseHelper, item);
                    Uri insertUri = ContentUris.withAppendedId(
                            MessagesEmailContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
//                        context.getContentResolver().notifyChange(uri, null);
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
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        int id = 0;
        Uri updateUri = null;
        switch (URI_MATCHER.match(uri)) {
            case MESSAGESEMAIL_DIR_TYPE:
            case MESSAGESEMAIL_ITEM_TYPE:
            case MESSAGESEMAIL_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.update(MessageEmailContentProvider.TABLE_MESSAGESEMAIL, values, where, whereArgs);
                    updateUri = ContentUris.withAppendedId(
                            MessagesEmailContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case MESSAGESEMAIL_JOIN_DIR_TYPE:
            case MESSAGESEMAIL_JOIN_ITEM_TYPE:
            case MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE:
                try {
                    byte[] vals1 = values.getAsByteArray(MessageItem.TAG);
                    MessageItem item = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals1);
                    id = (int) MessageEmailContentProvider.createOrUpdateMessages(databaseHelper, item);
                    updateUri = ContentUris.withAppendedId(MessagesEmailContentProvider.CONTENT_URI, id);
                    Context context = getContext();
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
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        int id = 0;
        Uri delUri = null;
        switch (URI_MATCHER.match(uri)) {
            case MESSAGESEMAIL_DIR_TYPE:
            case MESSAGESEMAIL_ITEM_TYPE:
            case MESSAGESEMAIL_DATA_ITEM_TYPE:
            case MESSAGESEMAIL_JOIN_DIR_TYPE:
            case MESSAGESEMAIL_JOIN_ITEM_TYPE:
            case MESSAGESEMAIL_JOIN_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.delete(
                            MessageEmailContentProvider.TABLE_MESSAGESEMAIL, where, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MessagesEmailContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(delUri, null);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        return id;
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
