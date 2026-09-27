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

import intellibitz.intellidroid.bean.MessageBean;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.bean.MessageBean;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static intellibitz.intellidroid.db.MessageItemColumns.MESSAGECHAT_SCHEMA;

public class MsgsGrpPeopleChatsContentProvider extends
        ContentProvider {

    public static final String TAG = "MsgsGrpPeopleChatsCP";
    public static final String TABLE_MSGSGRPPEOPLECHATS = "msgsgrppeoplechats";
    public static final String CREATE_TABLE_MSGSGRPPEOPLECHATS = "CREATE TABLE "
            + TABLE_MSGSGRPPEOPLECHATS + MessageItemColumns.MESSAGECHAT_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String MSGSGRPPEOPLECHATS_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String MSGSGRPPEOPLECHATS_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join";
    public static final String MSGSGRPPEOPLECHATS_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String MSGSGRPPEOPLECHATS_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String MSGSGRPPEOPLECHATS_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String MSGSGRPPEOPLECHATS_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    public static final String AUTHORITY =
            "intellibitz.intellidroid.content.MsgsGrpPeopleChatsContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_MSGSGRPPEOPLECHATS);
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGSGRPPEOPLECHATS);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGSGRPPEOPLECHATS);
    // UriMatcher stuff
    private static final int MSGSGRPPEOPLECHATS_DIR_TYPE = 1;
    private static final int MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE = 2;
    private static final int MSGSGRPPEOPLECHATS_ITEM_TYPE = 3;
    private static final int MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE = 4;
    private static final int MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE = 5;
    private static final int MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE = 6;
    private static final int MSGSGRPPEOPLECHATS_RAW_DIR_TYPE = 7;
    private static final int SEARCH_SUGGEST = 8;
    private static final int REFRESH_SHORTCUT = 9;
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPPEOPLECHATS,
                MSGSGRPPEOPLECHATS_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPPEOPLECHATS,
                MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPPEOPLECHATS +
                "/#", MSGSGRPPEOPLECHATS_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPPEOPLECHATS +
                "/*", MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPPEOPLECHATS +
                        "/#", MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPPEOPLECHATS +
                        "/*", MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_MSGSGRPPEOPLECHATS, MSGSGRPPEOPLECHATS_RAW_DIR_TYPE);
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

    public static MessageItem createsMessageThreadItemFromCursor(Cursor cursor) {
        MessageItem messageItem = new MessageItem();
        fillsMessageItemFromCursor(cursor, messageItem);
        return messageItem;
    }

    public static void fillsMessageItemFromCursor(Cursor cursor, MessageItem messageItem) {
        messageItem.set_id(cursor.getLong(cursor.getColumnIndex(
                MessageItemColumns.KEY_ID)));
        messageItem.setThreadId(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_THREAD_ID)));
        messageItem.setThreadIdRef(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_THREAD_IDREF)));
        messageItem.setThreadIdParts(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_THREAD_IDPARTS)));
        messageItem.setGroupId(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_GROUP_ID)));
        messageItem.setGroupIdRef(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_GROUP_IDREF)));
        messageItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_INTELLIBITZ_ID)));
        messageItem.setDeviceContactId(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_DEVICE_CONTACTID)));
        messageItem.setGroup(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_IS_GROUP)));
        messageItem.setEmailItem(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_IS_EMAIL)));
        messageItem.setAnonymous(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_IS_ANONYMOUS)));
        messageItem.setDevice(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_IS_DEVICE)));
        messageItem.setCloud(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_IS_CLOUD)));
        messageItem.setDataId(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DATA_ID)));
        messageItem.setDocType(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DOC_TYPE)));
        messageItem.setBaseType(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_BASE_TYPE)));
        messageItem.setName(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_NAME)));
        messageItem.setFirstName(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_FIRST_NAME)));
        messageItem.setLastName(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_LAST_NAME)));
        messageItem.setDisplayName(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DISPLAY_NAME)));
        messageItem.setDataRev(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DATA_REV)));
        messageItem.setType(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_TYPE)));
        messageItem.setToType(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_TO_TYPE)));
        messageItem.setDocOwnerEmail(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DOC_OWNER_EMAIL)));
        messageItem.setDocOwner(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DOC_OWNER)));
        messageItem.setDocSenderEmail(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DOC_SENDER_EMAIL)));
        messageItem.setFromUid(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_FROM_UID)));
        messageItem.setToUid(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_TO_UID)));
        messageItem.setToChatUid(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_TO_CHAT_UID)));
        messageItem.setChatId(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_CHAT_ID)));
        messageItem.setDocSender(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DOC_SENDER)));
        messageItem.setPendingDocs(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_PENDING_DOCS)));
        messageItem.setUnreadCount(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_UNREAD_COUNT)));
        messageItem.setHasAttachments(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_HAS_ATTACHEMENTS)));
        messageItem.setSubject(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_SUBJECT)));
        messageItem.setLatestMessageText(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_LATEST_MESSAGE)));
        messageItem.setLatestMessageTimestamp(cursor.getLong(cursor.getColumnIndex(
                MessageItemColumns.KEY_LATEST_MESSAGE_TS)));
        messageItem.setRead(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_IS_READ)));
        messageItem.setDelivered(cursor.getInt(cursor.getColumnIndex(
                MessageItemColumns.KEY_IS_DELIVERED)));
        messageItem.setFrom(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_FROM)));
        messageItem.setTo(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_TO)));
        messageItem.setCc(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_CC)));
        messageItem.setBcc(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_BCC)));
        messageItem.setTimestamp(cursor.getLong(cursor.getColumnIndex(
                MessageItemColumns.KEY_TIMESTAMP)));
        messageItem.setDateTime(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_DATETIME)));
    }

    public static MessageItem createChatMessageThread(String uid, MessageItem item) {
        item.setDocType("THREAD");
        item.setType("CHAT");
        item.setToType("USER");
        item.setChatId(uid);
//        item.setDataId(uid);
//        item.setSubject(uid);
        item.setToUid(uid);
        item.setToChatUid(uid);
        item.setDataRev("1");
        item.setHasAttachments(0);
        item.setLatestMessageText("");
        item.setTimestamp(System.currentTimeMillis());
        return item;
    }

    public static MessageBean createChatMessageThread(String uid, MessageBean item) {
        item.setDocType("THREAD");
        item.setType("CHAT");
        item.setToType("USER");
        item.setChatId(uid);
//        item.setDataId(uid);
//        item.setSubject(uid);
        item.setToUid(uid);
        item.setToChatUid(uid);
        item.setRev("1");
        item.setHasAttachments(0);
        item.setLatestMessageText("");
        item.setTimestamp(System.currentTimeMillis());
        return item;
    }

    public static Uri saveMessageThreadItemToDB(
            MessageItem messageItem, Context context) throws
            IOException {
        ContentValues msgThreadContentValues = new ContentValues();
        msgThreadContentValues.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(messageItem));
        Uri uri = context.getApplicationContext().getContentResolver().insert(
                Uri.withAppendedPath(CONTENT_URI, "0"), msgThreadContentValues);
        Log.e(TAG, "MessageThread saved: " + uri);
        return uri;
    }

    public static ArrayList<MessageItem> fillMessageThreadItemsFromCursor(Cursor cursor) {
        ArrayList<MessageItem> messageItems = new ArrayList<>();
        if (null == cursor || 0 == cursor.getCount()) return messageItems;
//        HashSet<MessageItem> set = new HashSet<>();
        Log.e(TAG, "fillMessageItemsFromCursor: count - " + cursor.getCount());
        do {
/*
            long freeMemory = Runtime.getRuntime().freeMemory();
            if (freeMemory < 1024 * 1024) {
                Log.d(TAG, "======================================");
                Log.d(TAG, "Total: " + Runtime.getRuntime().totalMemory());
                Log.d(TAG, "Max: " + Runtime.getRuntime().maxMemory());
                Log.d(TAG, "Free: " + Runtime.getRuntime().freeMemory());
                Log.d(TAG, "======================================");
                Log.e(TAG, "fillMessageItemsFromCursor: Runtime OOM limit - STOPPING " +
                        freeMemory);
                Log.e(TAG, "fillMessageItemsFromCursor: count - " + messageItems.size());
                break;
            } else {
*/
/*
                Log.d(TAG, "======================================");
                Log.d(TAG, "Total: " + Runtime.getRuntime().totalMemory());
                Log.d(TAG, "Max: " + Runtime.getRuntime().maxMemory());
                Log.d(TAG, "Free: " + Runtime.getRuntime().freeMemory());
                Log.d(TAG, "======================================");
*/
            MessageItem messageItem =
                    createsMessageThreadItemFromCursor(cursor);
            messageItems.add(messageItem);
//            set.add(messageItem);
//            }
        } while (cursor.moveToNext());
//        messageItems.addAll(set);
        return messageItems;
    }

    public static int deleteMsgs(String[] item, Context context) throws IOException {
        return context.getApplicationContext().getContentResolver().delete(JOIN_CONTENT_URI,
                null, item);
    }

    public static JSONObject toJson(MessageItem messageItem, String uid, String token,
                                    String device, String deviceRef) throws
            JSONException {
        if (null == messageItem) return null;
        String messageType = messageItem.getMessageType();
        if (null == messageType) messageType = messageItem.getType();
        if ("CHAT".equalsIgnoreCase(messageType)) {
            return toChatJson(messageItem, uid, token, device, deviceRef);
        } else if ("EMAIL".equalsIgnoreCase(messageType)) {
            return toEmailJson(messageItem, uid, token, device, deviceRef);
        }
        return null;
    }

    public static JSONObject toChatJson(MessageItem messageItem, String uid, String token,
                                        String device, String deviceRef) throws
            JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg_type", "CHAT");
        jsonObject.put("doc_type", messageItem.getDocType());
        jsonObject.put("chat_id", messageItem.getChatId());
        jsonObject.put("from_uid", messageItem.getFromUid());
        jsonObject.put("from_name", messageItem.getFromName());
        jsonObject.put("doc_owner", messageItem.getDocOwner());
        jsonObject.put("to_uid", messageItem.getChatId());
        jsonObject.put("to_type", messageItem.getToType());
        jsonObject.put("client_msg_ref", messageItem.getChatMsgRef());
        jsonObject.put("txt", messageItem.getText());
//        String uid = user.getDataId();
//        String token = user.getToken();
//        String device = user.getDevice();
        Set<MessageItem> items = messageItem.getAttachments();
        if (items != null && !items.isEmpty()) {
            JSONArray attachments = new JSONArray();
            for (MessageItem item : items) {
                try {
                    JSONObject attachmentDetails = HttpUrlConnectionParser.uploadAttachments(item,
                            MainApplicationSingleton.ATTACHMENT_UPLOAD_URL,
                            uid, device, deviceRef, token);
                    attachments.put(attachmentDetails);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            jsonObject.put("attachments", attachments);
        }
        return jsonObject;
    }

    public static JSONObject toEmailJson(MessageItem messageItem, String uid, String token,
                                         String device, String deviceRef) throws
            JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg_type", "EMAIL");
        jsonObject.put("to_uid", messageItem.getChatId());
        jsonObject.put("client_msg_ref", messageItem.getChatMsgRef());
        jsonObject.put("txt", messageItem.getText());
//        String uid = user.getDataId();
//        String token = user.getToken();
//        String device = user.getDevice();
        Set<MessageItem> items = messageItem.getAttachments();
        if (items != null && !items.isEmpty()) {
            JSONArray attachments = new JSONArray();
            for (MessageItem item : items) {
                try {
                    JSONObject attachmentDetails = HttpUrlConnectionParser.uploadAttachments(item,
                            MainApplicationSingleton.ATTACHMENT_UPLOAD_URL,
                            uid, device, deviceRef, token);
                    attachments.put(attachmentDetails);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            jsonObject.put("attachments", attachments);
        }
//        jsonObject.put("doc_type", "MSG");
//        jsonObject.put("msg_type", "EMAIL");
//        try {
        jsonObject.put("email", messageItem.getFromEmail());
//        String name = user.getName();
        jsonObject.put("from", messageItem.getFrom() + " <" + messageItem.getFromEmail() + ">");
//            // TODO: 16-03-2016
//        this can never happen.. inform user if email null
        if (TextUtils.isEmpty(messageItem.getTo())) {
            messageItem.setTo(MainApplicationSingleton.DUMMY_EMAIL);
        }
        jsonObject.put("to", messageItem.getTo());
//        if (!cc.isEmpty())
        jsonObject.put("cc", messageItem.getCc());
//        if (!bcc.isEmpty())
        jsonObject.put("bcc", messageItem.getBcc());
        jsonObject.put("subject", messageItem.getSubject());
        return jsonObject;
    }

    public static Uri saveNestsInDB(Collection<MessageItem> messageItems, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MessageItem.TAG,
                    MainApplicationSingleton.Serializer.serialize(messageItems));
            return context.getContentResolver().insert(CONTENT_URI, contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MessageItem fillNestItemFromJSON(
            JSONObject jsonObject, MessageItem nestItem) throws JSONException {
        final String name = jsonObject.optString("name");
        if (!TextUtils.isEmpty(name))
            nestItem.setName(name);
        final String code = jsonObject.optString("code");
        if (!TextUtils.isEmpty(code)) {
            nestItem.setFolderCode(code);
            nestItem.setDataId(nestItem.getFolderCode());
        }
        boolean default_folder = jsonObject.optBoolean("default_folder");
        nestItem.setDefaultFolder(default_folder ? 1 : 0);
        String stack = jsonObject.optString("stack_game");
        if (!TextUtils.isEmpty(stack))
            nestItem.setMsgRef(stack);
        return nestItem;
    }

    @NonNull
    public static List<MessageItem> fillNestItemFromJSONArray(JSONArray nests) {
        int len = nests.length();
        List<MessageItem> messageItems = new ArrayList<>(len);
        if (0 == len) return messageItems;
        for (int i = 0; i < len; i++) {
            try {
                JSONObject jsonObject = nests.getJSONObject(i);
                MessageItem messageItem = new MessageItem();
                messageItem.setNest();
                fillNestItemFromJSON(jsonObject, messageItem);
                messageItems.add(messageItem);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
        return messageItems;
    }

    public static long createOrUpdateMsgsGrpPeopleChats(
            DatabaseHelper databaseHelper, SQLiteDatabase db, MessageItem messageItem) {
        if (null == messageItem) return 0;
        final ContactItem contacts = messageItem.getContactItem();
        if (null == contacts) return 0;
        MessageItem messages = (MessageItem) messageItem.cloneShal();
        messages.set_id(0);
//        chat and chat group
        savesMsgsGrpPeopleChats(databaseHelper, db, messages, contacts);

//        emails
/*
        final HashSet<ContactItem> contactItems = contacts.getContactItems();
        if (null == contactItems || contactItems.isEmpty()) return 0;
        ContactItem[] items = contactItems.toArray(new ContactItem[0]);
        for (ContactItem item : items) {
            savesMsgsGrpPeopleChats(databaseHelper, db, messages, item);
        }
*/
        return messageItem.get_id();
    }

    public static void savesMsgsGrpPeopleChats(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            MessageItem messages, ContactItem contactItem) {
        long did = contactItem.getDeviceContactId();
        final String intellibitzId = contactItem.getIntellibitzId();
        final String deviceContactId = String.valueOf(did);
        String devId = intellibitzId;
        ContentValues values = new ContentValues();
        messages.setDeviceContactId(did);
        messages.setIntellibitzId(intellibitzId);
        messages.setTypeId(contactItem.getTypeId());
        messages.setDevice(contactItem.isDevice());
        messages.setGroup(contactItem.isGroup());
        messages.setEmailItem(contactItem.isEmailItem());
        messages.setFirstName(contactItem.getFirstName());
        messages.setLastName(contactItem.getLastName());
        messages.setDisplayName(contactItem.getDisplayName());
        fillContentValuesFromMessageItem(messages, values);
        Cursor cursor;
        String colName = MessageItemColumns.KEY_INTELLIBITZ_ID;
        if (0 == did) {
            cursor = databaseHelper.query(db, TABLE_MSGSGRPPEOPLECHATS,
                    new String[]{MessageItemColumns.KEY_ID},
                    colName + " = ?",
                    new String[]{devId}, null);
        } else {
            colName = MessageItemColumns.KEY_DEVICE_CONTACTID;
            devId = deviceContactId;
            cursor = databaseHelper.query(db, MsgsGrpPeopleChatsContentProvider.TABLE_MSGSGRPPEOPLECHATS,
                    new String[]{MessageItemColumns.KEY_ID},
                    colName + " = ?",
                    new String[]{devId}, null);
        }
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            databaseHelper.insert(db, MsgsGrpPeopleChatsContentProvider.TABLE_MSGSGRPPEOPLECHATS, null, values);
        } else {
            cursor.close();
            databaseHelper.update(db, MsgsGrpPeopleChatsContentProvider.TABLE_MSGSGRPPEOPLECHATS, values,
                    colName + " = ?",
                    new String[]{devId});
        }
    }

    public static ContentValues fillContentValuesFromMessageItem(
            MessageItem messageItem, ContentValues values) {
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_THREAD_ID, messageItem.getThreadId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_THREAD_IDREF, messageItem.getThreadIdRef());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_THREAD_IDPARTS, messageItem.getThreadIdParts());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_GROUP_ID, messageItem.getGroupId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_GROUP_IDREF, messageItem.getGroupIdRef());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DEVICE_CONTACTID, messageItem.getDeviceContactId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_INTELLIBITZ_ID, messageItem.getIntellibitzId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DATA_ID, messageItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CHAT_ID, messageItem.getChatId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_UID, messageItem.getToUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_CHAT_UID, messageItem.getToChatUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DATA_REV, messageItem.getDataRev());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_NAME, messageItem.getName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FIRST_NAME, messageItem.getFirstName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_LAST_NAME, messageItem.getLastName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DISPLAY_NAME, messageItem.getDisplayName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FOLDER_CODE, messageItem.getFolderCode());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_DEFAULT_FOLDER, messageItem.getDefaultFolder());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TYPE, messageItem.getType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_TYPE, messageItem.getToType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_OWNER, messageItem.getDocOwner());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_OWNER_EMAIL, messageItem.getDocOwnerEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_SENDER, messageItem.getDocSender());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_SENDER_EMAIL, messageItem.getDocSenderEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM_UID, messageItem.getFromUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_TYPE, messageItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_BASE_TYPE, messageItem.getBaseType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TEXT, messageItem.getText());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_HTML, messageItem.getHtml());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_SUBJECT, messageItem.getSubject());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM, messageItem.getFrom());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO, messageItem.getTo());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CC, messageItem.getCc());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_BCC, messageItem.getBcc());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_HAS_ATTACHEMENTS, messageItem.hasAttachments());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_LATEST_MESSAGE, messageItem.getLatestMessageText());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_LATEST_MESSAGE_TS, messageItem.getLatestMessageTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_READ, messageItem.isRead());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_DELIVERED, messageItem.isDelivered());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_GROUP, messageItem.isGroup());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_EMAIL, messageItem.isEmailItem());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_ANONYMOUS, messageItem.isAnonymous());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_DEVICE, messageItem.isDevice());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_CLOUD, messageItem.isCloud());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_UNREAD_COUNT, messageItem.getUnreadCount());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TIMESTAMP, messageItem.getTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DATETIME,
//                MainApplicationSingleton.getDateTimeMillis(messageItem.getTimestamp()));
                messageItem.getDateTime());
        return values;
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
            case MSGSGRPPEOPLECHATS_DIR_TYPE:
                return MSGSGRPPEOPLECHATS_DIR_MIME_TYPE;
            case MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE:
                return MSGSGRPPEOPLECHATS_JOIN_DIR_MIME_TYPE;
            case MSGSGRPPEOPLECHATS_ITEM_TYPE:
                return MSGSGRPPEOPLECHATS_ITEM_MIME_TYPE;
            case MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE:
                return MSGSGRPPEOPLECHATS_DATA_ITEM_MIME_TYPE;
            case MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE:
                return MSGSGRPPEOPLECHATS_JOIN_ITEM_MIME_TYPE;
            case MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE:
                return MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_MIME_TYPE;
            case MSGSGRPPEOPLECHATS_RAW_DIR_TYPE:
                return MSGSGRPPEOPLECHATS_RAW_DIR_MIME_TYPE;
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
            case MSGSGRPPEOPLECHATS_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_MSGSGRPPEOPLECHATS,
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
            case MSGSGRPPEOPLECHATS_RAW_DIR_TYPE:
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
            case MSGSGRPPEOPLECHATS_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE:
                try {
                    long id = databaseHelper.insert(TABLE_MSGSGRPPEOPLECHATS, null, values);
                    Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, id);
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
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        int id = 0;
        Uri updateUri = null;
        switch (URI_MATCHER.match(uri)) {
            case MSGSGRPPEOPLECHATS_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.update(TABLE_MSGSGRPPEOPLECHATS, values, where, whereArgs);
                    updateUri = ContentUris.withAppendedId(
                            MsgsGrpPeopleChatsContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
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
            case MSGSGRPPEOPLECHATS_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_DATA_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DIR_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_ITEM_TYPE:
            case MSGSGRPPEOPLECHATS_JOIN_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.delete(
                            TABLE_MSGSGRPPEOPLECHATS, where, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MsgsGrpPeopleChatsContentProvider.CONTENT_URI, id);
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
