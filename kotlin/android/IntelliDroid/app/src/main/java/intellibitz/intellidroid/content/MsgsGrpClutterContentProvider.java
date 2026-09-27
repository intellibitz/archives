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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class MsgsGrpClutterContentProvider extends
        ContentProvider {

    public static final String TAG = "MsgsGrpClutterCP";
    public static final String TABLE_MSGSGRPCLUTTER = "msgsgrpclutter";
    public static final String CREATE_TABLE_MSGSGRPCLUTTER = "CREATE TABLE "
            + TABLE_MSGSGRPCLUTTER + MessageItemColumns.MESSAGECHAT_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String MESSAGE_THREADS_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String MESSAGE_THREADS_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join";
    public static final String MESSAGE_THREADS_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String MESSAGE_THREADS_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String MESSAGE_THREADS_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String MESSAGE_THREADS_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String MESSAGE_THREADS_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    public static final String AUTHORITY = "intellibitz.intellidroid.content.MsgsGrpClutterContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_MSGSGRPCLUTTER);
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGSGRPCLUTTER);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGSGRPCLUTTER);
    // UriMatcher stuff
    private static final int MESSAGE_THREADS_DIR_TYPE = 1;
    private static final int MESSAGE_THREADS_JOIN_DIR_TYPE = 2;
    private static final int MESSAGE_THREADS_ITEM_TYPE = 3;
    private static final int MESSAGE_THREADS_DATA_ITEM_TYPE = 4;
    private static final int MESSAGE_THREADS_JOIN_ITEM_TYPE = 5;
    private static final int MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE = 6;
    private static final int MESSAGE_THREADS_RAW_DIR_TYPE = 7;
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
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPCLUTTER,
                MESSAGE_THREADS_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPCLUTTER,
                MESSAGE_THREADS_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPCLUTTER +
                "/#", MESSAGE_THREADS_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPCLUTTER +
                "/*", MESSAGE_THREADS_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPCLUTTER +
                        "/#", MESSAGE_THREADS_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPCLUTTER +
                        "/*", MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_MSGSGRPCLUTTER, MESSAGE_THREADS_RAW_DIR_TYPE);
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
        messageItem.setProfilePic(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_PIC)));
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

    public static MessageItem fillMessageThreadItemFromAllJoinCursor(
            MessageItem messageThreadItem, Cursor cursor) throws
            CloneNotSupportedException {
        messageThreadItem.set_id(cursor.getLong(cursor.getColumnIndex("mt_id")));
        messageThreadItem.setThreadId(cursor.getString(cursor.getColumnIndex("mthid")));
        messageThreadItem.setThreadIdRef(cursor.getString(cursor.getColumnIndex("mthidref")));
        messageThreadItem.setThreadIdParts(cursor.getString(cursor.getColumnIndex("mthidparts")));
        messageThreadItem.setGroupId(cursor.getString(cursor.getColumnIndex("mtgid")));
        messageThreadItem.setGroupIdRef(cursor.getString(cursor.getColumnIndex("mtgidref")));
        messageThreadItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("mtclutid")));
        messageThreadItem.setGroup(cursor.getInt(cursor.getColumnIndex("mtisgroup")));
        messageThreadItem.setEmailItem(cursor.getInt(cursor.getColumnIndex("mtisemail")));
        messageThreadItem.setAnonymous(cursor.getInt(cursor.getColumnIndex("mtisanon")));
        messageThreadItem.setDevice(cursor.getInt(cursor.getColumnIndex("mtisdev")));
        messageThreadItem.setCloud(cursor.getInt(cursor.getColumnIndex("mtiscloud")));
        messageThreadItem.setFirstName(cursor.getString(cursor.getColumnIndex("mtfirst")));
        messageThreadItem.setLastName(cursor.getString(cursor.getColumnIndex("mtlast")));
        messageThreadItem.setDisplayName(cursor.getString(cursor.getColumnIndex("mtdisplay")));
        messageThreadItem.setDeviceContactId(cursor.getInt(cursor.getColumnIndex("mtdevcid")));
        messageThreadItem.setDataId(cursor.getString(cursor.getColumnIndex("mtid")));
        messageThreadItem.setBaseType(cursor.getString(cursor.getColumnIndex("mtbaset")));
        messageThreadItem.setToUid(cursor.getString(cursor.getColumnIndex("mttuid")));
        messageThreadItem.setFromUid(cursor.getString(cursor.getColumnIndex("mtfuid")));
        messageThreadItem.setToChatUid(cursor.getString(cursor.getColumnIndex("mttcuid")));
        messageThreadItem.setChatId(cursor.getString(cursor.getColumnIndex("mtcuid")));
        messageThreadItem.setType(cursor.getString(cursor.getColumnIndex("mttype")));
        messageThreadItem.setToType(cursor.getString(cursor.getColumnIndex("mttotype")));
        messageThreadItem.setDocOwnerEmail(cursor.getString(cursor.getColumnIndex("mtdoe")));
        messageThreadItem.setDocSenderEmail(cursor.getString(cursor.getColumnIndex("mtdse")));
        messageThreadItem.setDocSender(cursor.getString(cursor.getColumnIndex("mtds")));
        messageThreadItem.setSubject(cursor.getString(cursor.getColumnIndex("mtsub")));
        messageThreadItem.setDocType(cursor.getString(cursor.getColumnIndex("mtdoct")));
        messageThreadItem.setDataRev(cursor.getString(cursor.getColumnIndex("mtrev")));
        messageThreadItem.setName(cursor.getString(cursor.getColumnIndex("mtname")));
        messageThreadItem.setDocOwner(cursor.getString(cursor.getColumnIndex("mtdo")));
        messageThreadItem.setFrom(cursor.getString(cursor.getColumnIndex("mtfrom")));
        messageThreadItem.setTo(cursor.getString(cursor.getColumnIndex("mtto")));
        messageThreadItem.setCc(cursor.getString(cursor.getColumnIndex("mtcc")));
        messageThreadItem.setBcc(cursor.getString(cursor.getColumnIndex("mtbcc")));
        messageThreadItem.setLatestMessageText(cursor.getString(cursor.getColumnIndex("mtlate")));
        messageThreadItem.setLatestMessageTimestamp(cursor.getLong(cursor.getColumnIndex("mtlatets")));
        messageThreadItem.setRead(cursor.getInt(cursor.getColumnIndex("mtisr")));
        messageThreadItem.setDelivered(cursor.getInt(cursor.getColumnIndex("mtisd")));
        messageThreadItem.setFlagged(cursor.getInt(cursor.getColumnIndex("mtisf")));
        messageThreadItem.setUnreadCount(cursor.getInt(cursor.getColumnIndex("mtuc")));
        messageThreadItem.setPendingDocs(cursor.getInt(cursor.getColumnIndex("mtpd")));
        messageThreadItem.setHasAttachments(cursor.getInt(cursor.getColumnIndex("mtha")));
        messageThreadItem.setTimestamp(cursor.getLong(cursor.getColumnIndex("mttime")));
        messageThreadItem.setDateTime(cursor.getString(cursor.getColumnIndex("mtdtime")));
        ContactItem contactThreadItem = messageThreadItem.getContactItem();
        if (null == contactThreadItem) {
            contactThreadItem = new ContactItem();
            messageThreadItem.setContactItem(contactThreadItem);
        }
        do {
            String email = cursor.getString(cursor.getColumnIndex("email"));
            String ename = cursor.getString(cursor.getColumnIndex("ename"));
            String etype = cursor.getString(cursor.getColumnIndex("etype"));
            if (email != null) {
//            adds email to message thread
                ContactItem contactItem = new ContactItem();
                contactItem.setDataId(email);
                contactItem.setTypeId(email);
                contactItem.setIntellibitzId(email);
                contactItem.setName(ename);
                contactItem.setType(etype);
                contactThreadItem.addContact(contactItem);
            }

            String gcid = cursor.getString(cursor.getColumnIndex("gcid"));
            if (gcid != null) {
//                ContactItem contactItem = messageThreadItem.getContactItem();
/*
                if (null == contactItem) {
                    contactItem = new ContactItem();
                    messageThreadItem.setContactItem(contactItem);
                }
*/
                contactThreadItem.set_id(cursor.getLong(cursor.getColumnIndex("gc_id")));
                contactThreadItem.setDataId(cursor.getString(cursor.getColumnIndex("gcid")));
                contactThreadItem.setName(cursor.getString(cursor.getColumnIndex("gcname")));
                contactThreadItem.setProfilePic(cursor.getString(cursor.getColumnIndex("gcpic")));
                contactThreadItem.setType(cursor.getString(cursor.getColumnIndex("gctype")));
                String intellibitzId = cursor.getString(cursor.getColumnIndex("akid"));
                ContactItem contactItem =
                        contactThreadItem.getContactItem(intellibitzId);
                if (null == contactItem && intellibitzId != null) {
//            adds contact to message
//                    // TODO: 20-06-2016
//                    to fetch mobile
//                    MobileItem mobileItem = new MobileItem();
//                    mobileItem.setIntellibitzId(intellibitzId);
                    ContactItem intellibitzContactItem = new ContactItem(intellibitzId);
                    contactItem = new ContactItem(intellibitzContactItem);
//                    contactItem.setDataId(intellibitzId);
//                    contactItem.setIntellibitzId(intellibitzId);
                    contactThreadItem.addContact(contactItem);
                }
                if (contactItem != null) {
                    contactItem.set_id(cursor.getLong(cursor.getColumnIndex("ak_id")));
                    contactItem.setDataId(cursor.getString(cursor.getColumnIndex("akid")));
                    contactItem.setName(cursor.getString(cursor.getColumnIndex("akname")));
                    contactItem.setType(cursor.getString(cursor.getColumnIndex("aktype")));
                    contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("akonid")));
                    contactItem.setStatus(cursor.getString(cursor.getColumnIndex("akstatus")));
                }
            }

            String mid = cursor.getString(cursor.getColumnIndex("mid"));
            if (mid != null) {
// adds message to message thread
//                // TODO: 28-03-2016
//                fill the thread item with more
                MessageItem messageItem = messageThreadItem.getMessage(mid);
                if (null == messageItem) {
//                    first message
                    messageItem = messageThreadItem.addMessage(mid);
                }
                if (messageItem != null) {
                    messageItem.set_id(cursor.getLong(cursor.getColumnIndex("m_id")));
                    messageItem.setPendingDocs(cursor.getInt(cursor.getColumnIndex("mpd")));
                    messageItem.setFromName(cursor.getString(cursor.getColumnIndex("mfn")));
                    messageItem.setFromUid(cursor.getString(cursor.getColumnIndex("mfuid")));
                    messageItem.setText(cursor.getString(cursor.getColumnIndex("mtxt")));
                    messageItem.setDocOwnerEmail(cursor.getString(cursor.getColumnIndex("mdoe")));
                    messageItem.setDocSenderEmail(cursor.getString(cursor.getColumnIndex("mdse")));
                    messageItem.setMessageDirection(cursor.getString(cursor.getColumnIndex("mdir")));
                    messageItem.setMessageAttachId(cursor.getString(cursor.getColumnIndex("maid")));
                    messageItem.setHasAttachments(cursor.getInt(cursor.getColumnIndex("mha")));
                    messageItem.setRead(cursor.getInt(cursor.getColumnIndex("misr")));
                    messageItem.setDelivered(cursor.getInt(cursor.getColumnIndex("misd")));
                    messageItem.setFlagged(cursor.getInt(cursor.getColumnIndex("misf")));
                    messageItem.setTimestamp(cursor.getLong(cursor.getColumnIndex("mtime")));
                    String aid = cursor.getString(cursor.getColumnIndex("aid"));
                    MessageItem attachmentItem = messageItem.getAttachment(aid);
                    if (null == attachmentItem && aid != null) {
//            adds attachment to message
                        attachmentItem = messageItem.addAttachment(aid);
                    }
                    if (attachmentItem != null) {
                        attachmentItem.set_id(cursor.getLong(cursor.getColumnIndex("a_id")));
                        attachmentItem.setMsgAttachID(cursor.getString(cursor.getColumnIndex("amid")));
                        attachmentItem.setPartID(cursor.getString(cursor.getColumnIndex("apid")));
                        attachmentItem.setName(cursor.getString(cursor.getColumnIndex("aname")));
                        attachmentItem.setType(cursor.getString(cursor.getColumnIndex("atype")));
                        attachmentItem.setSubType(cursor.getString(cursor.getColumnIndex("astype")));
                        attachmentItem.setSize(cursor.getInt(cursor.getColumnIndex("asize")));
                        attachmentItem.setEncoding(cursor.getString(cursor.getColumnIndex("aenc")));
                        attachmentItem.setDownloadURL(cursor.getString(cursor.getColumnIndex("aurl")));
                    }
                }
            }
        } while (cursor.moveToNext());
        return messageThreadItem;
//        Log.e(TAG, " MessageThread: " + messageThreadItem);
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

    public static MessageItem queryMessageThreadFullJoin(
            MessageItem messageItem, Context context) {
        String id = messageItem.getDataId();
        if (null == id) {
            Log.e(TAG, "Query fails, ID null: " + messageItem);
            return messageItem;
        }
        Uri uri = Uri.withAppendedPath(JOIN_CONTENT_URI, Uri.encode(id));
        String selection = "mt." +
                MessageItemColumns.KEY_DATA_ID + " = ? ";
        String[] selectionArgs = new String[]{id};
        String sortOrder = "mt." +
                MessageItemColumns.KEY_TIMESTAMP + " ASC";
        Cursor cursor = context.getApplicationContext().getContentResolver().query(
                uri, null, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            try {
                fillMessageThreadItemFromAllJoinCursor(
                        messageItem, cursor);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            cursor.close();
        }
        return messageItem;
    }

    public static MessageItem query(
            MessageItem messageItem, Context context) {
        String id = messageItem.getDataId();
        if (null == id) {
            Log.e(TAG, "Query fails, ID null: " + messageItem);
            return messageItem;
        }
        Uri uri = Uri.withAppendedPath(CONTENT_URI, Uri.encode(id));
        String selection = MessageItemColumns.KEY_DATA_ID + " = ? ";
        String[] selectionArgs = new String[]{id};
        String sortOrder = MessageItemColumns.KEY_TIMESTAMP + " ASC";
        Cursor cursor = context.getApplicationContext().getContentResolver().query(
                uri, null, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            fillsMessageItemFromCursor(cursor, messageItem);
            cursor.close();
        }
        return messageItem;
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

    public static long createOrUpdateMsgsGrpClutter(
            DatabaseHelper databaseHelper, SQLiteDatabase db, MessageItem messageItem) {
        if (null == messageItem) return 0;
        final ContactItem contacts = messageItem.getContactItem();
        if (null == contacts) return 0;
        MessageItem messages = (MessageItem) messageItem.cloneShal();
        messages.set_id(0);
//        chat and chat group
//        savesMsgsGrpClutter(databaseHelper, db, messages, contacts);
//        emails
        final HashSet<ContactItem> contactItems = contacts.getContactItems();
        if (null == contactItems || contactItems.isEmpty()) return 0;
        ContactItem[] items = contactItems.toArray(new ContactItem[0]);
        for (ContactItem contactItem : items) {
//            group by "from".. emails always by intellibitz id
            if ("from".equalsIgnoreCase(contactItem.getType())) {
                savesMsgsGrpClutter(databaseHelper, db, messages, contactItem);
//            if email is in device, group by "people" also.. by device contact id
                if (contactItem.getDeviceContactId() > 0) {
                    MsgsGrpPeopleContentProvider.savesMsgsGrpPeople(databaseHelper, db, messages, contactItem);
                }
            }
        }
        return messageItem.get_id();
    }

    public static void savesMsgsGrpClutter(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            MessageItem messages, ContactItem contactItem) {
//        long did = contactItem.getDeviceContactId();
        final String intellibitzId = contactItem.getIntellibitzId();
//        final String deviceContactId = String.valueOf(did);
//        String devId = intellibitzId;
        ContentValues values = new ContentValues();
        messages.setIntellibitzId(intellibitzId);
        messages.setDeviceContactId(contactItem.getDeviceContactId());
        messages.setTypeId(contactItem.getTypeId());
        messages.setDevice(contactItem.isDevice());
        messages.setGroup(contactItem.isGroup());
        messages.setEmailItem(contactItem.isEmailItem());
        messages.setFirstName(contactItem.getFirstName());
        messages.setLastName(contactItem.getLastName());
        messages.setDisplayName(contactItem.getDisplayName());
        MessageEmailContentProvider.fillContentValuesFromMessageItem(messages, values);
        Cursor cursor;
//        String colName = MessageItemColumns.KEY_INTELLIBITZ_ID;
        cursor = databaseHelper.query(db, TABLE_MSGSGRPCLUTTER,
                new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_INTELLIBITZ_ID + " = ?",
                new String[]{intellibitzId}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            databaseHelper.insert(db, MsgsGrpClutterContentProvider.TABLE_MSGSGRPCLUTTER, null, values);
        } else {
            cursor.close();
            databaseHelper.update(db, MsgsGrpClutterContentProvider.TABLE_MSGSGRPCLUTTER, values,
                    MessageItemColumns.KEY_INTELLIBITZ_ID + " = ?",
                    new String[]{intellibitzId});
        }
    }

    public static String queryThreadId(String subject, String email, Context context) {
        String sql = "select to_uid from " +
                TABLE_MSGSGRPCLUTTER
                + " where " +
                "length( ltrim( " +
                "ltrim (ltrim   (lower(?),lower('fwd:')),lower('re:')), " +
                "ltrim (ltrim   (lower(?),lower('fwd:')),lower('re:')) " +
                ")) = 0 " +
                " and ( " +
                "from_email = ? or " +
                "instr(email_to, ?) <> 0 or " +
                "instr(email_cc, ?) <> 0) ";
        Cursor cursor = context.getContentResolver().query(RAW_CONTENT_URI,
                null,
                sql,
                new String[]{subject, subject, email, email, email}, null);
        if (null == cursor) {
            return null;
        } else {
            String toUid = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_TO_UID));
            cursor.close();
            return toUid;
        }
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
            case MESSAGE_THREADS_DIR_TYPE:
                return MESSAGE_THREADS_DIR_MIME_TYPE;
            case MESSAGE_THREADS_JOIN_DIR_TYPE:
                return MESSAGE_THREADS_JOIN_DIR_MIME_TYPE;
            case MESSAGE_THREADS_ITEM_TYPE:
                return MESSAGE_THREADS_ITEM_MIME_TYPE;
            case MESSAGE_THREADS_DATA_ITEM_TYPE:
                return MESSAGE_THREADS_DATA_ITEM_MIME_TYPE;
            case MESSAGE_THREADS_JOIN_ITEM_TYPE:
                return MESSAGE_THREADS_JOIN_ITEM_MIME_TYPE;
            case MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE:
                return MESSAGE_THREADS_JOIN_DATA_ITEM_MIME_TYPE;
            case MESSAGE_THREADS_RAW_DIR_TYPE:
                return MESSAGE_THREADS_RAW_DIR_MIME_TYPE;
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
            case MESSAGE_THREADS_DIR_TYPE:
            case MESSAGE_THREADS_ITEM_TYPE:
            case MESSAGE_THREADS_DATA_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DIR_TYPE:
            case MESSAGE_THREADS_JOIN_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_MSGSGRPCLUTTER,
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
            case MESSAGE_THREADS_RAW_DIR_TYPE:
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
            case MESSAGE_THREADS_DIR_TYPE:
            case MESSAGE_THREADS_ITEM_TYPE:
            case MESSAGE_THREADS_DATA_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DIR_TYPE:
            case MESSAGE_THREADS_JOIN_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE:
                try {
                    long id = databaseHelper.insert(TABLE_MSGSGRPCLUTTER, null, values);
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
            case MESSAGE_THREADS_DIR_TYPE:
            case MESSAGE_THREADS_ITEM_TYPE:
            case MESSAGE_THREADS_DATA_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DIR_TYPE:
            case MESSAGE_THREADS_JOIN_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.update(TABLE_MSGSGRPCLUTTER, values, where, whereArgs);
                    updateUri = ContentUris.withAppendedId(
                            MsgsGrpClutterContentProvider.CONTENT_URI, id);
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
            case MESSAGE_THREADS_DIR_TYPE:
            case MESSAGE_THREADS_ITEM_TYPE:
            case MESSAGE_THREADS_DATA_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DIR_TYPE:
            case MESSAGE_THREADS_JOIN_ITEM_TYPE:
            case MESSAGE_THREADS_JOIN_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.delete(
                            TABLE_MSGSGRPCLUTTER, where, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MsgsGrpClutterContentProvider.CONTENT_URI, id);
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
