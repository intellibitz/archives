package intellibitz.intellidroid.content;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.IntellibitzItemColumns;
import intellibitz.intellidroid.db.MessageAttachmentJoinColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.service.RcvDocService;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.IntellibitzItemColumns;
import intellibitz.intellidroid.db.MessageAttachmentJoinColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.service.RcvDocService;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.service.RcvDocService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 *
 */
public class MsgEmailAttachmentContentProvider extends
        ContentProvider {

    public static final String TAG = "MsgEmailAttachmentCP";
    public static final String TABLE_MSGEMAILATTACHMENT = "msgemailattachment";
    public static final String CREATE_TABLE_MSGEMAILATTACHMENT = "CREATE TABLE "
            + TABLE_MSGEMAILATTACHMENT + MessageItemColumns.MESSAGECHAT_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String ATTACHMENT_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String ATTACHMENT_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String ATTACHMENT_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id";
    // UriMatcher stuff
    private static final int ATTACHMENT_DIR_TYPE = 1;
    private static final int ATTACHMENT_ITEM_TYPE = 2;
    private static final int ATTACHMENT_DATA_ITEM_TYPE = 3;
    private static final int SEARCH_SUGGEST = 4;
    private static final int REFRESH_SHORTCUT = 5;
    public static String AUTHORITY =
            "intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_MSGEMAILATTACHMENT);
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILATTACHMENT,
                ATTACHMENT_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILATTACHMENT +
                "/#", ATTACHMENT_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILATTACHMENT +
                "/*", ATTACHMENT_DATA_ITEM_TYPE);
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

    public static ContentValues fillAttachmentItemContentValues(MessageItem attachmentItem,
                                                                ContentValues values) {
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATA_ID, attachmentItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MSGATTCH_ID, attachmentItem.getMsgAttachID());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_PARTID, attachmentItem.getPartID());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TYPE, attachmentItem.getType());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MESSAGE_TYPE, attachmentItem.getMessageType());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MESSAGE_DIRECTION, attachmentItem.getMessageDirection());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_OWNER_EMAIL, attachmentItem.getDocOwnerEmail());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_SENDER_EMAIL, attachmentItem.getDocSenderEmail());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOC_TYPE, attachmentItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_NAME, attachmentItem.getName());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DESCRIPTION, attachmentItem.getDescription());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_SUBTYPE, attachmentItem.getSubType());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_ENCODING, attachmentItem.getEncoding());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_LANGUAGE, attachmentItem.getLanguage());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_MD5, attachmentItem.getMd5());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DOWNLOAD_URL, attachmentItem.getDownloadURL());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_PIC, attachmentItem.getProfilePic());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_CLOUD_PIC, attachmentItem.getCloudPic());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_SIZE, attachmentItem.getSize());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_TIMESTAMP, attachmentItem.getTimestamp());
        MainApplicationSingleton.fillIfNotNull(values, MessageItemColumns.KEY_DATETIME, MainApplicationSingleton.getDateTimeMillis(attachmentItem.getTimestamp()));
        return values;
    }

    public static void setAttachmentItemFromJson(MessageItem attachmentMessage, JSONObject response) throws
            JSONException {
        String path = response.getString("path");
        attachmentMessage.setDownloadURL(path);
        String contentType = response.getString("content_type");
        String[] types = contentType.split("/");
        attachmentMessage.setType(types[0]);
        attachmentMessage.setSubType(types[1]);
        int len = response.getInt("content_length");
        attachmentMessage.setSize(len);
        String units = response.getString("content_length_units");
        attachmentMessage.setPartID(units);
        attachmentMessage.setMessageType(MessageItem.EMAIL);
    }

    public static String getMimeType(MessageItem item) {
        String type = item.getType();
        String subType = item.getSubType();
        if (null == type && null == subType) return null;
        String part1 = "";
        String part2 = "";
        if (type != null) part1 = type + "/";
        if (subType != null) part2 = subType;
        return part1 + subType;
    }

    public static long[] createOrUpdateMessageAttachments(
            DatabaseHelper databaseHelper, Set<MessageItem> items, long id) {
        long[] ids = new long[items.size()];
        int i = 0;
        for (MessageItem attachment : items) {
            ids[i++] = createOrUpdateMessageAttachment(databaseHelper, attachment, id);
        }
        return ids;
    }

    public static long[] createOrUpdateMessageAttachments(
            DatabaseHelper databaseHelper, SQLiteDatabase db, Set<MessageItem> items, long id) {
        long[] ids = new long[items.size()];
        int i = 0;
        for (MessageItem attachment : items) {
            ids[i++] = createOrUpdateMessageAttachment(databaseHelper, db, attachment, id);
        }
        return ids;
    }

    public static long createOrUpdateMessageAttachment(
            DatabaseHelper databaseHelper, MessageItem item, long id) {
//        Cursor cursor = getEmailsCursor(item);
        ContentValues values = new ContentValues();
//        message thread is the parent, email item child
        fillAttachmentItemContentValues(item, values);

//        checks if the same email attachment is already present for the message thread
        Cursor cursor = getAttachmentCursor(databaseHelper, item.getDataId());
        long _id;
        if (cursor != null && cursor.getCount() > 0) {
            _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID));
//            updateMessageAttachment(item, id);
            cursor.close();
            databaseHelper.update(TABLE_MSGEMAILATTACHMENT, values,
                    MessageItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
            item.set_id(_id);
        } else {
            if (cursor != null) cursor.close();
            _id = databaseHelper.insert(TABLE_MSGEMAILATTACHMENT, null, values);
            item.set_id(_id);
        }
//        creates the join
        createMessageAttachmentJoin(databaseHelper, _id, id);
        return item.get_id();
    }

    public static long createOrUpdateMessageAttachment(
            DatabaseHelper databaseHelper, SQLiteDatabase db, MessageItem item, long id) {
//        Cursor cursor = getEmailsCursor(item);
        ContentValues values = new ContentValues();
//        message thread is the parent, email item child
        fillAttachmentItemContentValues(item, values);

//        Cursor cursor = getAttachmentCursor(databaseHelper, dataId);
//        data id can change.. since the cloud url is different than the local url
//        local url is stored in description
//        // TODO: 11/8/16
//        move local url into a separate column
//        checks if the same email attachment is already present for the message thread
        String dataId = item.getDataId();
        Cursor cursor = databaseHelper.query(TABLE_MSGEMAILATTACHMENT,
                new String[]{
                        MessageItemColumns.KEY_ID
                        , MessageItemColumns.KEY_NAME
                        , MessageItemColumns.KEY_DESCRIPTION
                },
                MessageItemColumns.KEY_DATA_ID + " = ?", new String[]{dataId}, null, null, null);
        if (null == cursor || 0 == cursor.getCount()) {
//        // TODO: 11/8/16
//        revisit.. to update attachment, with the right parent message
//        the same file can be attached for different message.. so check the join.. get the msg id
//        then update the attachment that belongs to the correct msg id
//        hack!!
            String[] bits = dataId.split("//");
            if (bits.length > 1) {
                dataId = "/" + bits[bits.length - 1];
                Log.d(TAG, "createOrUpdateMessageAttachment: " + dataId);
            }
            cursor = databaseHelper.query(TABLE_MSGEMAILATTACHMENT,
                    new String[]{
                            MessageItemColumns.KEY_ID
                            , MessageItemColumns.KEY_NAME
                            , MessageItemColumns.KEY_DESCRIPTION
                    },
                    MessageItemColumns.KEY_DATA_ID + " = ?", new String[]{dataId}, null, null, null);
        }
        long _id;
        if (cursor != null && cursor.getCount() > 0) {
            _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID));
            item.setName(cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_NAME)));
            item.setDescription(cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DESCRIPTION)));
//            // TODO: 11/8/16
//            to set cloud url, pointing to the attachment stored in the cloud
//            currently points to local storage, if the attachment originated from device
//            updateMessageAttachment(item, id);
            cursor.close();
            databaseHelper.update(db, TABLE_MSGEMAILATTACHMENT, values,
                    MessageItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
            item.set_id(_id);
        } else {
            if (cursor != null) cursor.close();
            _id = databaseHelper.insert(db, TABLE_MSGEMAILATTACHMENT, null, values);
            item.set_id(_id);
        }
//        creates the join
        createMessageAttachmentJoin(databaseHelper, db, _id, id);
        return item.get_id();
    }

    /**
     * @param id the message id, the attachment belongs to
     * @return Cursor
     */
    public static Cursor getAttachmentCursor(DatabaseHelper databaseHelper, long id) {
        return databaseHelper.query(TABLE_MSGEMAILATTACHMENT,
                null,
                MessageItemColumns.KEY_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
    }

    public static Cursor getAttachmentCursor(DatabaseHelper databaseHelper, String id) {
        return databaseHelper.query(TABLE_MSGEMAILATTACHMENT,
                new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_DATA_ID + " = ?", new String[]{id}, null, null, null);
    }

    public static Cursor getAllMessageAttachmentsCursor(
            DatabaseHelper databaseHelper, MessageItem attachmentItem, long id) {

        String selectQuery = "SELECT  * FROM " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL + " msgs " +
                " left join " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN +
                " mta on msgs.[_id] = mta.[msg_id]  " +
                " left join attachments am  on mta.[attachment_id] = am.[_id]" +
                " WHERE " + "  am." + MessageItemColumns.KEY_DATA_ID + " = '"
                + attachmentItem.getDataId() +
                "' AND msgs." + MessageItemColumns.KEY_ID + " = '" + id + "'";

//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, null);
    }

    public static long updateMessageThreadMessageChatAttachmentURL(
            DatabaseHelper databaseHelper, MessageItem attachmentItem) {
        ContentValues values = new ContentValues();
        values.put(MessageItemColumns.KEY_DOWNLOAD_URL, attachmentItem.getDownloadURL());
        String msgAttachID = attachmentItem.getDataId();
        int row = databaseHelper.update(TABLE_MSGEMAILATTACHMENT, values,
                MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{msgAttachID});
//        fetch the attachment and send the id back.. for content updates
        if (row > 0) {
            Cursor cursor = databaseHelper.query(TABLE_MSGEMAILATTACHMENT,
                    new String[]{MessageItemColumns.KEY_ID,
                            MessageItemColumns.KEY_DATA_ID
                    },
                    MessageItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{msgAttachID}, null);
            String atchid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATA_ID));
            if (msgAttachID.equals(atchid)) {
                return cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID));
            }
        }
        return 0;
    }

    public static long getMessageAttachmentJoin(DatabaseHelper databaseHelper, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN, new String[]{IntellibitzItemColumns.KEY_ID},
                MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID + " = ? and " +
                        MessageAttachmentJoinColumns.KEY_MESSAGE_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(fk)}, null, null, null);
        if (c != null) {
            if (c.getCount() > 0)
                _id = c.getLong(c.getColumnIndex("_id"));
            c.close();
        }
        return _id;
    }

    public static long createMessageAttachmentJoin(DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = getMessageAttachmentJoin(databaseHelper, id, fk);
        ContentValues values = new ContentValues();
        values.put(MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID, id);
        values.put(MessageAttachmentJoinColumns.KEY_MESSAGE_ID, fk);
        values.put(MessageAttachmentJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db, MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN, null, values);
        } else {
            databaseHelper.update(db, MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN, values,
                    MessageAttachmentJoinColumns.KEY_ID + " = ?", new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long createMessageAttachmentJoin(DatabaseHelper databaseHelper, long id, long fk) {
        long _id = getMessageAttachmentJoin(databaseHelper, id, fk);
        ContentValues values = new ContentValues();
        values.put(MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID, id);
        values.put(MessageAttachmentJoinColumns.KEY_MESSAGE_ID, fk);
        values.put(MessageAttachmentJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN, null, values);
        } else {
            databaseHelper.update(MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN, values,
                    MessageAttachmentJoinColumns.KEY_ID + " = ?", new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static ComponentName asyncUpdateEmailAttachmentFilePathInDB(
            Context context, ContactItem user, MessageItem attachmentItem) {
        return asyncUpdateEmailAttachmentFilePathInDB(context, user, attachmentItem, null);
    }

    public static ComponentName asyncUpdateEmailAttachmentFilePathInDB(
            Context context, ContactItem user, MessageItem attachmentItem, ResultReceiver resultReceiver) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, RcvDocService.class);
            intent.setAction(MainApplicationSingleton.INTENT_ACTION_FETCH_EMAIL_ATTACHMENT_CLOUD);
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable)
                    UserContentProvider.getUserCloneForService(user));
            intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, (Parcelable) attachmentItem);
            intent.putExtra("ResultReceiver", resultReceiver);
            return context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
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
        switch (sURIMatcher.match(uri)) {
            case ATTACHMENT_DIR_TYPE:
                return ATTACHMENT_DIR_MIME_TYPE;
            case ATTACHMENT_ITEM_TYPE:
                return ATTACHMENT_ITEM_MIME_TYPE;
            case ATTACHMENT_DATA_ITEM_TYPE:
                return ATTACHMENT_DATA_ITEM_MIME_TYPE;
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
            case ATTACHMENT_DIR_TYPE:
            case ATTACHMENT_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_MSGEMAILATTACHMENT,
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
            case ATTACHMENT_ITEM_TYPE:
                try {
                    cursor = getAttachmentCursor(databaseHelper, ContentUris.parseId(uri));
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
        switch (sURIMatcher.match(uri)) {
            case ATTACHMENT_DIR_TYPE:
            case ATTACHMENT_ITEM_TYPE:
                try {
                    byte[] vals = values.getAsByteArray(MessageItem.ATTACHMENT_MESSAGE);
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
        byte[] vals = values.getAsByteArray(MessageItem.ATTACHMENT_MESSAGE);
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case ATTACHMENT_DIR_TYPE:
            case ATTACHMENT_ITEM_TYPE:
                try {
                    MessageItem attachmentItem = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    long _id = updateMessageThreadMessageAttachmentURL(
                            databaseHelper, attachmentItem);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(
                                Uri.withAppendedPath(uri, String.valueOf(_id)), null);
                    }
                    return (int) _id;
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case ATTACHMENT_DATA_ITEM_TYPE:
                try {
                    MessageItem attachmentItem = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    long _id = updateMessageThreadMessageChatAttachmentURL(
                            databaseHelper, attachmentItem);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(
                                Uri.withAppendedPath(uri, String.valueOf(_id)), null);
                    }
                    return (int) _id;
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

    public long updateMessageThreadMessageAttachmentURL(
            DatabaseHelper databaseHelper, MessageItem attachmentItem) {
        ContentValues values = new ContentValues();
        values.put(MessageItemColumns.KEY_DOWNLOAD_URL, attachmentItem.getDownloadURL());
        String msgAttachID = attachmentItem.getMsgAttachID();
        String partID = attachmentItem.getPartID();
        String attachmentName = attachmentItem.getName();
        int row = databaseHelper.update(TABLE_MSGEMAILATTACHMENT, values,
                MessageItemColumns.KEY_MSGATTCH_ID + " = ? and " +
                        MessageItemColumns.KEY_PARTID + " = ? and " +
                        MessageItemColumns.KEY_NAME + " = ? ",
                new String[]{msgAttachID, partID, attachmentName});
//        fetch the attachment and send the id back.. for content updates
        if (row > 0) {
            Cursor cursor = databaseHelper.query(TABLE_MSGEMAILATTACHMENT,
                    new String[]{MessageItemColumns.KEY_ID,
                            MessageItemColumns.KEY_MSGATTCH_ID,
                            MessageItemColumns.KEY_PARTID,
                            MessageItemColumns.KEY_NAME},
                    MessageItemColumns.KEY_MSGATTCH_ID + " = ? and " +
                            MessageItemColumns.KEY_PARTID + " = ? and " +
                            MessageItemColumns.KEY_NAME + " = ? ",
                    new String[]{msgAttachID, partID, attachmentName}, null);
            String name = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_NAME));
            String part = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_PARTID));
            String atchid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_MSGATTCH_ID));
            if (attachmentName.equals(name) && msgAttachID.equals(atchid) && partID.equals(part)) {
                return cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID));
            }
        }
        return 0;
    }

}
