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
import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.ContactsContactJoinColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.db.MessagesContactsJoinColumns;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.bean.MessageBean;
import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.ContactsContactJoinColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.db.MessagesContactsJoinColumns;
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

import static intellibitz.intellidroid.content.MessageChatContentProvider.createsChatMessageItemFromJSON;
import static intellibitz.intellidroid.content.MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN;
import static intellibitz.intellidroid.content.MessageEmailContentProvider.createsEmailMessageItemFromJSON;

public class MsgsGrpDraftContentProvider extends
        ContentProvider {

    public static final String TAG = "MsgsGrpDraftCP";
    public static final String TABLE_MSGSGRPDRAFT = "msgsgrpdraft";
    public static final String TABLE_MSGSGRPDRAFT_MESSAGES_JOIN = "msgsgrpdraft_messages";
    public static final String CREATE_TABLE_MSGSGRPDRAFT = "CREATE TABLE "
            + TABLE_MSGSGRPDRAFT + MessageItemColumns.MESSAGECHAT_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String MSGSGRPDRAFT_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String MSGSGRPDRAFT_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join";
    public static final String MSGSGRPDRAFT_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String MSGSGRPDRAFT_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String MSGSGRPDRAFT_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String MSGSGRPDRAFT_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String MSGSGRPDRAFT_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    // UriMatcher stuff
    private static final int MSGSGRPDRAFT_DIR_TYPE = 1;
    private static final int MSGSGRPDRAFT_JOIN_DIR_TYPE = 2;
    private static final int MSGSGRPDRAFT_ITEM_TYPE = 3;
    private static final int MSGSGRPDRAFT_DATA_ITEM_TYPE = 4;
    private static final int MSGSGRPDRAFT_JOIN_ITEM_TYPE = 5;
    private static final int MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE = 6;
    private static final int MSGSGRPDRAFT_RAW_DIR_TYPE = 7;
    private static final int SEARCH_SUGGEST = 8;
    private static final int REFRESH_SHORTCUT = 9;
    public static String AUTHORITY = "intellibitz.intellidroid.content.MsgsGrpDraftContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_MSGSGRPDRAFT);
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGSGRPDRAFT);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGSGRPDRAFT);
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPDRAFT,
                MSGSGRPDRAFT_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPDRAFT,
                MSGSGRPDRAFT_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPDRAFT +
                "/#", MSGSGRPDRAFT_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGSGRPDRAFT +
                "/*", MSGSGRPDRAFT_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPDRAFT +
                        "/#", MSGSGRPDRAFT_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGSGRPDRAFT +
                        "/*", MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_MSGSGRPDRAFT, MSGSGRPDRAFT_RAW_DIR_TYPE);
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

    public static MessageItem savesMsgsGrpDraftFromJSON(
            JSONObject jsonObject, ContactItem user, Context context) throws
            JSONException, IOException {
        String msgType = jsonObject.optString("msg_type");
        if (TextUtils.isEmpty(msgType)) return null;
        MessageItem messageItem = null;
        if ("EMAIL".equals(msgType)) {
            messageItem = MessageEmailContentProvider.createsEmailMessageItemFromJSON(jsonObject, user);
        } else if ("CHAT".equals(msgType)) {
            messageItem = createsChatMessageItemFromJSON(jsonObject, user);
        }
        return savesMsgsGrpDraft(messageItem, context);
    }

    public static MessageItem savesMsgsGrpDraft(MessageItem messageItem, Context context) {
        if (null == messageItem) return null;
//        swaps the ids here.. so the chat id becomes the data id (plays nicely with the message collections)
//        any query and views based on messages with chat id as data id will work
        messageItem.setThreadId(messageItem.getDataId());
        messageItem.setDataId(messageItem.getChatId());
        messageItem.setLatestMessageText(messageItem.getText());
        messageItem.setLatestMessageTimestamp(messageItem.getTimestamp());
        messageItem.setDocSender(messageItem.getFromName());
        ContentValues values = new ContentValues();
        MessageEmailContentProvider.fillContentValuesFromMessageItem(messageItem, values);
        Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_THREAD_ID + " = ?",
                new String[]{messageItem.getThreadId()}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            context.getContentResolver().insert(CONTENT_URI, values);
        } else {
            cursor.close();
            context.getContentResolver().update(CONTENT_URI,
                    values,
                    MessageItemColumns.KEY_THREAD_ID + " = ?",
                    new String[]{messageItem.getThreadId()});
        }
        return messageItem;
    }

    public static MessageItem createsMsgsGrpDraftFromCursor(Cursor cursor) {
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

    public static MessageItem createDemoMessageThread(String email) {
        MessageItem messageItem = new MessageItem();
        ContactItem contactItem = new ContactItem();
        messageItem.setContactItem(contactItem);
        messageItem.setDocType("THREAD");
        messageItem.setType("CHAT");
        messageItem.setDataId("aktprototype");
        messageItem.setDataRev("1");
        messageItem.setDocOwner("DEMO");
        messageItem.setDocSender("DEMO");
        messageItem.setDocOwnerEmail(email);
        messageItem.setDocSenderEmail(email);
        messageItem.setSubject("DEMO");
        messageItem.setHasAttachments(1);
        ContactItem from = new ContactItem();
        from.setDataId(email);
        from.setTypeId(email);
        from.setIntellibitzId(email);
        from.setName("DEMO");
        from.setType("from");
        contactItem.addContact(from);
        ContactItem to = new ContactItem();
        to.setDataId("nishanth@intellibitz.com");
        to.setTypeId("nishanth@intellibitz.com");
        to.setIntellibitzId("nishanth@intellibitz.com");
        to.setName("DEMO");
        to.setType("to");
        contactItem.addContact(to);
        ContactItem cc = new ContactItem();
        cc.setDataId("jeff@intellibitz.com");
        cc.setTypeId("jeff@intellibitz.com");
        cc.setIntellibitzId("jeff@intellibitz.com");
        cc.setName("DEMO");
        cc.setType("to");
        contactItem.addContact(cc);
        messageItem.setTimestamp(System.currentTimeMillis());
        return messageItem;
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

    public static MessageItem createMessagesFromMessage(
            MessageItem messageItem, ContactItem user) {
/*
[{"_id":"7e6d22cca73df90375091f51c05880dc","_rev":"1-a9afbdafe44c2817d5da2aa5730a4684",
"doc_type":"MSG","doc_owner":"intellibitz","msg_type":"CHAT","to_type":"group",
"from_uid":"USRMASTER_919840348914","to_uid":"aktprototype","from_name":"Muthu Ramadoss",
"txt":"thanks for the chat","timestamp":1459344961}]
         */
/*
[{"_id":"b2d6880d954afe7ee6663554cb3e9c6f","_rev":"1-75875289f5d8ba7553d6aeaa7b36b3a5",
"txt":"hiiij","to_uid":"aktprototype","msg_type":"CHAT","doc_type":"MSG","timestamp":1459592231,
"from_uid":"USRMASTER_919655653929","from_name":"Nishanth","to_type":"GROUP",
"doc_owner":"USRMASTER_919840348914","chat_id":"aktprototype","msg_direction":"IN","chat_name":"Demo Group"}]
         */
        MessageItem messageThreadItem = new MessageItem();
        messageThreadItem.setBaseType("THREAD");
        messageThreadItem.setDocType("THREAD");
        return fillMessagesFromMessage(messageThreadItem, messageItem, user);
    }

    public static MessageItem fillMessagesFromMessage(
            MessageItem messageThreadItem, MessageItem messageItem, ContactItem user) {
/*
[{"_id":"7e6d22cca73df90375091f51c05880dc","_rev":"1-a9afbdafe44c2817d5da2aa5730a4684",
"doc_type":"MSG","doc_owner":"intellibitz","msg_type":"CHAT","to_type":"group",
"from_uid":"USRMASTER_919840348914","to_uid":"aktprototype","from_name":"Muthu Ramadoss",
"txt":"thanks for the chat","timestamp":1459344961}]
         */
/*
[{"_id":"b2d6880d954afe7ee6663554cb3e9c6f","_rev":"1-75875289f5d8ba7553d6aeaa7b36b3a5",
"txt":"hiiij","to_uid":"aktprototype","msg_type":"CHAT","doc_type":"MSG","timestamp":1459592231,
"from_uid":"USRMASTER_919655653929","from_name":"Nishanth","to_type":"GROUP",
"doc_owner":"USRMASTER_919840348914","chat_id":"aktprototype","msg_direction":"IN","chat_name":"Demo Group"}]
         */
        messageThreadItem.setDataRev(messageItem.getDataRev());
        messageThreadItem.setBaseType(messageItem.getBaseType());
        messageThreadItem.setDocType(messageItem.getDocType());
//        CHAT ID is the thread ID for CHAT
        String chatId = messageItem.getChatId();
        if (null == chatId) chatId = messageItem.getDataId();
//        for chat
        messageThreadItem.setChatId(messageItem.getChatId());
//        TO UID is the thread ID for email
        String toUid = messageItem.getToUid();
        if (null == toUid) toUid = messageItem.getDataId();
//        for email
        messageThreadItem.setToUid(toUid);
        if ("CHAT".equalsIgnoreCase(messageItem.getMessageType()) ||
                "CHAT".equalsIgnoreCase(messageItem.getType())) {
            messageThreadItem.setDataId(chatId);
            messageThreadItem.setThreadId(chatId);
            messageThreadItem.setThreadIdRef(chatId);
            messageItem.setThreadId(chatId);
            messageItem.setThreadIdRef(chatId);
        } else {
            messageThreadItem.setDataId(toUid);
            messageThreadItem.setThreadId(toUid);
            messageThreadItem.setThreadIdRef(toUid);
            messageItem.setThreadId(toUid);
            messageItem.setThreadIdRef(toUid);
        }
//        sets id as chatid and tochatid for sameness between chat and email
        messageThreadItem.setChatId(messageThreadItem.getDataId());
        messageThreadItem.setToChatUid(messageThreadItem.getDataId());
        messageThreadItem.setTimestamp(messageItem.getTimestamp());
        messageThreadItem.setType(messageItem.getMessageType());
        messageThreadItem.setToType(messageItem.getToType());
        messageThreadItem.setName(messageItem.getName());
        messageThreadItem.setSubject(messageItem.getSubject());
        messageThreadItem.setDocOwner(messageItem.getDocOwner());
        messageThreadItem.setDocSender(messageItem.getDocSender());
        messageThreadItem.setDocOwnerEmail(messageItem.getDocOwnerEmail());
        messageThreadItem.setDocSenderEmail(messageItem.getDocSenderEmail());
        messageThreadItem.setFromUid(messageItem.getFromUid());
        messageThreadItem.setHasAttachments(messageItem.getAttachments().size());
        messageThreadItem.setRead(messageItem.isRead());
        messageThreadItem.setDelivered(messageItem.isDelivered());
//        sets unread increment, only if the incoming message read flag is not true
//        sets unread increment, for others messages NOT self messages
        if (!messageItem.isRead() &&
                !user.getDataId().equals(messageItem.getFromUid())) {
//            // TODO: 22-05-2016
//            to increment from previous count.. get the latest count from DB
            messageThreadItem.setUnreadCount(messageThreadItem.getUnreadCount() + 1);
        }
//        latest message is written, only if message timestamp is newer than the previous
        long ts = messageItem.getTimestamp();
        long mts = messageThreadItem.getLatestMessageTimestamp();
        if (ts > mts) {
            messageThreadItem.setLatestMessageText(messageItem.getText());
            messageThreadItem.setLatestMessageTimestamp(ts);
        }
        String dt = messageItem.getDateTime();
        if (dt != null) {
            String mdt = messageThreadItem.getDateTime();
            if (null == mdt) {
                messageThreadItem.setDateTime(dt);
            } else {
                long dts = MainApplicationSingleton.getDateTimeMillisISO(dt);
                long mdts = MainApplicationSingleton.getDateTimeMillisISO(mdt);
                if (0 == mdts || dts < mdts) {
                    messageThreadItem.setDateTime(dt);
                }
            }
        }
        messageThreadItem.setFrom(messageItem.getFromEmail());
        messageThreadItem.setTo(messageItem.getTo());
        messageThreadItem.setCc(messageItem.getCc());
        messageThreadItem.setBcc(messageItem.getBcc());
//        creates msg thread contact from message
        if ("CHAT".equalsIgnoreCase(messageItem.getMessageType()) ||
                "CHAT".equalsIgnoreCase(messageItem.getType())) {
//            hack.. to identify the last message sender
//            // TODO: 21-06-2016
//            remove this hack, and provide a separate column
/*
            messageThreadItem.setDocSenderEmail(messageItem.getFromUid());
//            single chat only.. group chat would be taken care by group info doc type coming in socket
            IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(messageItem.getChatId());
            if ("GROUP".equalsIgnoreCase(messageThreadItem.getToType())) {
                intellibitzContactItem.setGroup(true);
            }
            intellibitzContactItem.setType(messageThreadItem.getToType());
            intellibitzContactItem.setEmailItem(false);
            ContactItem msgContactItem = new ContactItem(intellibitzContactItem);
            ContactItem contactItem = new ContactItem(msgContactItem);
            messageThreadItem.setContactItem(contactItem);
*/
//        // TODO: 30-06-2016
//        sets emails
//                sets the last message item, emails and normalizes
//                to, cc, bcc etc., is set
//        messageThreadItem.getEmails().clear();
//        messageThreadItem.getEmails().addAll(messageItem.getEmails());
            messageThreadItem.setContactItem(messageItem.getContactItem());
        } else {
/*
            Collection<EmailItem> items = messageItem.getEmails();
            messageThreadItem.setEmails(items);
*/
            messageThreadItem.setContactItem(messageItem.getContactItem());
            messageThreadItem.compose();
/*
            if (items != null && !items.isEmpty()) {
                ContactItem contactItem = new ContactItem();
                contactItem.setDataId(messageItem.getChatId());
                contactItem.setGroupId(messageItem.getChatId());
                contactItem.setDataRev(messageItem.getDataRev());
                contactItem.setType(messageItem.getType());
                contactItem.setDocOwner(messageItem.getDocOwner());
                contactItem.setName(messageItem.getName());
                contactItem.setProfilePic(messageItem.getProfilePic());
                contactItem.setGroup(true);
                contactItem.setEmailItem(true);
                contactItem.setTimestamp(messageItem.getTimestamp());
                EmailItem[] emailItems = items.toArray(new EmailItem[0]);
                for (EmailItem emailItem : emailItems) {
                    IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(emailItem);
                    intellibitzContactItem.setEmailItem(true);
                    ContactItem msgContactItem = new ContactItem(intellibitzContactItem);
                    contactItem.addContact(msgContactItem);
                }
                messageThreadItem.setContactItem(contactItem);
            }
*/
        }
        return messageThreadItem;
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

    public static List<MessageItem> fillMessageItemsFromCursor(Cursor cursor) {
        List<MessageItem> messageItems = new ArrayList<>();
        if (null == cursor || 0 == cursor.getCount()) return messageItems;
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
                    createsMsgsGrpDraftFromCursor(cursor);
            messageItems.add(messageItem);
//            }
        } while (cursor.moveToNext());
        return messageItems;
    }

    public static int deleteMsgs(String[] item, Context context) throws IOException {
        return context.getApplicationContext().getContentResolver().delete(JOIN_CONTENT_URI,
                null, item);
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

    public static Cursor getMessageThreadJoin(
            DatabaseHelper databaseHelper, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  mt." + MessageItemColumns.KEY_ID + " as mt_id, " +
                        " mt." + MessageItemColumns.KEY_THREAD_ID + " as mthid, " +
                        " mt." + MessageItemColumns.KEY_THREAD_IDREF + " as mthidref, " +
                        " mt." + MessageItemColumns.KEY_THREAD_IDPARTS + " as mthidparts, " +
                        " mt." + MessageItemColumns.KEY_GROUP_ID + " as mtgid, " +
                        " mt." + MessageItemColumns.KEY_GROUP_IDREF + " as mtgidref, " +
                        " mt." + MessageItemColumns.KEY_INTELLIBITZ_ID + " as mtclutid, " +
                        " mt." + MessageItemColumns.KEY_IS_GROUP + " as mtisgroup, " +
                        " mt." + MessageItemColumns.KEY_IS_EMAIL + " as mtisemail, " +
                        " mt." + MessageItemColumns.KEY_IS_ANONYMOUS + " as mtisanon, " +
                        " mt." + MessageItemColumns.KEY_IS_DEVICE + " as mtisdev, " +
                        " mt." + MessageItemColumns.KEY_IS_CLOUD + " as mtiscloud, " +
                        " mt." + MessageItemColumns.KEY_FIRST_NAME + " as mtfirst, " +
                        " mt." + MessageItemColumns.KEY_LAST_NAME + " as mtlast, " +
                        " mt." + MessageItemColumns.KEY_DISPLAY_NAME + " as mtdisplay, " +
                        " mt." + MessageItemColumns.KEY_DEVICE_CONTACTID + " as mtdevcid, " +
                        " mt." + MessageItemColumns.KEY_DATA_ID + " as mtid, " +
                        " mt." + MessageItemColumns.KEY_TYPE + " as mttype, " +
                        " mt." + MessageItemColumns.KEY_TO_TYPE + " as mttotype, " +
                        " mt." + MessageItemColumns.KEY_DATA_REV + " as mtrev, " +
                        " mt." + MessageItemColumns.KEY_NAME + " as mtname, " +
                        " mt." + MessageItemColumns.KEY_DOC_OWNER + " as mtdo, " +
                        " mt." + MessageItemColumns.KEY_DOC_SENDER + " as mtds, " +
                        " mt." + MessageItemColumns.KEY_DOC_OWNER_EMAIL + " as mtdoe, " +
                        " mt." + MessageItemColumns.KEY_DOC_SENDER_EMAIL + " as mtdse, " +
                        " mt." + MessageItemColumns.KEY_FROM_UID + " as mtfuid, " +
                        " mt." + MessageItemColumns.KEY_TO_UID + " as mttuid, " +
                        " mt." + MessageItemColumns.KEY_TO_CHAT_UID + " as mttcuid, " +
                        " mt." + MessageItemColumns.KEY_CHAT_ID + " as mtcuid, " +
                        " mt." + MessageItemColumns.KEY_DOC_TYPE + " as mtdoct, " +
                        " mt." + MessageItemColumns.KEY_BASE_TYPE + " as mtbaset, " +
                        " mt." + MessageItemColumns.KEY_SUBJECT + " as mtsub, " +
                        " mt." + MessageItemColumns.KEY_FROM + " as mtfrom, " +
                        " mt." + MessageItemColumns.KEY_TO + " as mtto, " +
                        " mt." + MessageItemColumns.KEY_CC + " as mtcc, " +
                        " mt." + MessageItemColumns.KEY_BCC + " as mtbcc, " +
                        " mt." + MessageItemColumns.KEY_LATEST_MESSAGE + " as mtlate, " +
                        " mt." + MessageItemColumns.KEY_LATEST_MESSAGE_TS + " as mtlatets, " +
                        " mt." + MessageItemColumns.KEY_IS_READ + " as mtisr, " +
                        " mt." + MessageItemColumns.KEY_IS_DELIVERED + " as mtisd, " +
                        " mt." + MessageItemColumns.KEY_IS_FLAGGED + " as mtisf, " +
                        " mt." + MessageItemColumns.KEY_UNREAD_COUNT + " as mtuc, " +
                        " mt." + MessageItemColumns.KEY_PENDING_DOCS + " as mtpd, " +
                        " mt." + MessageItemColumns.KEY_HAS_ATTACHEMENTS + " as mtha, " +
                        " mt." + MessageItemColumns.KEY_TIMESTAMP + " as mttime, " +
                        " mt." + MessageItemColumns.KEY_DATETIME + " as mtdtime, " +
                        " m." + MessageItemColumns.KEY_ID + " as m_id," +
                        " m." + MessageItemColumns.KEY_DATA_ID + " as mid," +
                        " m." + MessageItemColumns.KEY_FROM_NAME + " as mfn," +
                        " m." + MessageItemColumns.KEY_FROM_UID + " as mfuid," +
                        " m." + MessageItemColumns.KEY_TEXT + " as mtxt," +
                        " m." + MessageItemColumns.KEY_DOC_OWNER_EMAIL + " as mdoe," +
                        " m." + MessageItemColumns.KEY_DOC_SENDER_EMAIL + " as mdse," +
                        " m." + MessageItemColumns.KEY_MESSAGE_DIRECTION + " as mdir," +
                        " m." + MessageItemColumns.KEY_MESSAGE_ATTACH_ID + " as maid," +
                        " m." + MessageItemColumns.KEY_PENDING_DOCS + " as mpd," +
                        " m." + MessageItemColumns.KEY_HAS_ATTACHEMENTS + " as mha," +
                        " m." + MessageItemColumns.KEY_IS_READ + " as misr," +
                        " m." + MessageItemColumns.KEY_IS_DELIVERED + " as misd," +
                        " m." + MessageItemColumns.KEY_IS_FLAGGED + " as misf," +
                        " m." + MessageItemColumns.KEY_TIMESTAMP + " as mtime," +
                        " gc._id as gc_id, gc.id as gcid, gc.name as gcname," +
                        " gc.profile_pic as gcpic, gc.type as gctype, " +
                        " ak._id as ak_id, ak.id as akid, ak.name as akname, " +
                        MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + "] " +
                        "left outer join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN +
                        " gcak on gc.[_id] = gcak.[" +
                        MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + "] " +
                        "left outer join " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT +
                        " ak on ak.[_id] = gcak.[" +
                        ContactsContactJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN +
                        " mte on ak.[_id] = mte.[contact_id] " +
                        "left outer join " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT +
                        " e on e.[_id] = mte.[intellibitzcontact_id] " +
                        "left outer join " + TABLE_MSGSGRPDRAFT_MESSAGES_JOIN +
                        " mtm on mt.[_id] = mtm.[msg_thread_id] " +
                        "left outer join " + TABLE_MSGSGRPDRAFT + " m on m.[_id] = mtm.[msg_id] " +
                        "left outer join " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN +
                        " ma on m.[_id] = ma.[msg_id] " +
//                        // TODO: 4/8/16
//                        to fetch the correct attachment - chat, group or email
//                        "left outer join " + AttachmentContentProvider.TABLE_ATTACHMENT + " a on a.[_id] = ma.[attachment_id] ";
                        "left outer join " + MsgChatAttachmentContentProvider.TABLE_MSGCHATATTACHMENT + " a on a.[_id] = ma.[attachment_id] ";
        if (selection != null && !selection.isEmpty()) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, selectionArgs);
    }

    public static Cursor getMessageThreadShalGroupJoin(
            DatabaseHelper databaseHelper, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  mt." + MessageItemColumns.KEY_ID + " as mt_id, " +
                        " mt." + MessageItemColumns.KEY_THREAD_ID + " as mthid, " +
                        " mt." + MessageItemColumns.KEY_THREAD_IDREF + " as mthidref, " +
                        " mt." + MessageItemColumns.KEY_THREAD_IDPARTS + " as mthidparts, " +
                        " mt." + MessageItemColumns.KEY_GROUP_ID + " as mtgid, " +
                        " mt." + MessageItemColumns.KEY_GROUP_IDREF + " as mtgidref, " +
                        " mt." + MessageItemColumns.KEY_INTELLIBITZ_ID + " as mtclutid, " +
                        " mt." + MessageItemColumns.KEY_IS_GROUP + " as mtisgroup, " +
                        " mt." + MessageItemColumns.KEY_IS_EMAIL + " as mtisemail, " +
                        " mt." + MessageItemColumns.KEY_IS_ANONYMOUS + " as mtisanon, " +
                        " mt." + MessageItemColumns.KEY_IS_DEVICE + " as mtisdev, " +
                        " mt." + MessageItemColumns.KEY_IS_CLOUD + " as mtiscloud, " +
                        " mt." + MessageItemColumns.KEY_FIRST_NAME + " as mtfirst, " +
                        " mt." + MessageItemColumns.KEY_LAST_NAME + " as mtlast, " +
                        " mt." + MessageItemColumns.KEY_DISPLAY_NAME + " as mtdisplay, " +
                        " mt." + MessageItemColumns.KEY_DEVICE_CONTACTID + " as mtdevcid, " +
                        " mt." + MessageItemColumns.KEY_DATA_ID + " as mtid, " +
                        " mt." + MessageItemColumns.KEY_TYPE + " as mttype, " +
                        " mt." + MessageItemColumns.KEY_TO_TYPE + " as mttotype, " +
                        " mt." + MessageItemColumns.KEY_DATA_REV + " as mtrev, " +
                        " mt." + MessageItemColumns.KEY_NAME + " as mtname, " +
                        " mt." + MessageItemColumns.KEY_DOC_OWNER + " as mtdo, " +
                        " mt." + MessageItemColumns.KEY_DOC_SENDER + " as mtds, " +
                        " mt." + MessageItemColumns.KEY_DOC_OWNER_EMAIL + " as mtdoe, " +
                        " mt." + MessageItemColumns.KEY_DOC_SENDER_EMAIL + " as mtdse, " +
                        " mt." + MessageItemColumns.KEY_FROM_UID + " as mtfuid, " +
                        " mt." + MessageItemColumns.KEY_TO_UID + " as mttuid, " +
                        " mt." + MessageItemColumns.KEY_TO_CHAT_UID + " as mttcuid, " +
                        " mt." + MessageItemColumns.KEY_CHAT_ID + " as mtcuid, " +
                        " mt." + MessageItemColumns.KEY_DOC_TYPE + " as mtdoct, " +
                        " mt." + MessageItemColumns.KEY_BASE_TYPE + " as mtbaset, " +
                        " mt." + MessageItemColumns.KEY_SUBJECT + " as mtsub, " +
                        " mt." + MessageItemColumns.KEY_FROM + " as mtfrom, " +
                        " mt." + MessageItemColumns.KEY_TO + " as mtto, " +
                        " mt." + MessageItemColumns.KEY_CC + " as mtcc, " +
                        " mt." + MessageItemColumns.KEY_BCC + " as mtbcc, " +
                        " mt." + MessageItemColumns.KEY_LATEST_MESSAGE + " as mtlate, " +
                        " mt." + MessageItemColumns.KEY_LATEST_MESSAGE_TS + " as mtlatets, " +
                        " mt." + MessageItemColumns.KEY_IS_READ + " as mtisr, " +
                        " mt." + MessageItemColumns.KEY_IS_DELIVERED + " as mtisd, " +
                        " mt." + MessageItemColumns.KEY_IS_FLAGGED + " as mtisf, " +
                        " mt." + MessageItemColumns.KEY_UNREAD_COUNT + " as mtuc, " +
                        " mt." + MessageItemColumns.KEY_PENDING_DOCS + " as mtpd, " +
                        " mt." + MessageItemColumns.KEY_HAS_ATTACHEMENTS + " as mtha, " +
                        " mt." + MessageItemColumns.KEY_TIMESTAMP + " as mttime, " +
                        " mt." + MessageItemColumns.KEY_DATETIME + " as mtdtime, " +
/*
                        " m." + MessageItemColumns.KEY_ID + " as m_id," +
                        " m." + MessageItemColumns.KEY_DATA_ID + " as mid," +
                        " m." + MessageItemColumns.KEY_PENDING_DOCS + " as mpd," +
                        " m." + MessageItemColumns.KEY_FROM_NAME + " as mfn," +
                        " m." + MessageItemColumns.KEY_TEXT + " as mtxt," +
                        " m." + MessageItemColumns.KEY_DOC_OWNER_EMAIL + " as mdoe," +
                        " m." + MessageItemColumns.KEY_DOC_SENDER_EMAIL + " as mdse," +
                        " m." + MessageItemColumns.KEY_MESSAGE_DIRECTION + " as mdir," +
                        " m." + MessageItemColumns.KEY_MESSAGE_ATTACH_ID + " as maid," +
                        " m." + MessageItemColumns.KEY_TIMESTAMP + " as mtime," +
                        " ak._id as ak_id, ak.id as akid, ak.name as akname, " +
                        " ak.profile_pic as akpic, ak.type as aktype, " +
                        " ak.intellibitz_id as akonid, ak.status as akstatus, " +
                        " e.name as ename, e.email as email, e.type as etype, " +
                        "a." + MessageItemColumns.KEY_ID + " as aid, " +
                        "a." + MessageItemColumns.KEY_DATA_ID + " as adid, " +
                        "a." + MessageItemColumns.KEY_MSGATTCH_ID + " as amid, " +
                        "a." + MessageItemColumns.KEY_PARTID + " as apid, " +
                        "a." + MessageItemColumns.KEY_NAME + " as aname, " +
                        "a." + MessageItemColumns.KEY_TYPE + " as atype, " +
                        "a." + MessageItemColumns.KEY_SUBTYPE + " as astype, " +
                        "a." + MessageItemColumns.KEY_SIZE + " as asize, " +
                        "a." + MessageItemColumns.KEY_ENCODING + " as aenc, " +
                        "a." + MessageItemColumns.KEY_DOWNLOAD_URL + " as aurl " +
*/
//                        GROUP CHAT INFO
                        " gc._id as gc_id, gc.id as gcid, gc.name as gcname," +
                        " gc.profile_pic as gcpic, gc.type as gctype, " +
//                        ONE ON ONE CHAT INFO
                        " cc.profile_pic as ccpic" +
                        "  FROM  " +
                        TABLE_MSGSGRPDRAFT + " mt "
/*
                        +
                        "left outer join " + TABLE_GRPCONTACTS_AKCONTACTS_JOIN +
                        " gcak on gc.[_id] = gcak.[group_id] " +
                        "left outer join " + TABLE_INTELLIBITZCONTACT + " ak on ak.[_id] = gcak.[akcontact_id] " +
                        "left outer join " + TABLE_MESSAGETHREADS_EMAILS_JOIN +
                        " mte on mt.[_id] = mte.[msg_thread_id] " +
                        "left outer join " + TABLE_MSGTHREADEMAILS + " e on e.[_id] = mte.[email_id] " +
                        "left outer join " + TABLE_MSGSGRPDRAFT_MESSAGES_JOIN +
                        " mtm on mt.[_id] = mtm.[msg_thread_id] " +
                        "left outer join " + TABLE_MESSAGE + " m on m.[_id] = mtm.[msg_id] " +
                        "left outer join " + TABLE_MESSAGE_ATTACHMENTS_JOIN +
                        " ma on m.[_id] = ma.[msg_id] " +
                        "left outer join " + TABLE_MSGCHATATTACHMENT + " a on a.[_id] = ma.[attachment_id] "
*/
                        +
                        "left outer join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN +
                        " mtg on mt.[_id] = mtg.[msgthread_id] " +
                        "left outer join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS +
                        " gc on gc.[_id] = mtg.[" +
                        MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + "] "
                        +
                        "left outer join " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT +
                        " cc on cc.[" + ContactItemColumns.KEY_INTELLIBITZ_ID + "] = mt.[" +
                        MessageItemColumns.KEY_CHAT_ID + "] ";
        if (selection != null && !selection.isEmpty()) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, selectionArgs);
    }

    public static long createOrUpdateMessages(
            DatabaseHelper databaseHelper, SQLiteDatabase db, MessageItem messageItem) {
        Cursor cursor = databaseHelper.query(db, MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT,
                new String[]{
                        MessageItemColumns.KEY_ID,
                        MessageItemColumns.KEY_LATEST_MESSAGE,
                        MessageItemColumns.KEY_LATEST_MESSAGE_TS,
                        MessageItemColumns.KEY_DOC_SENDER},
                MessageItemColumns.KEY_DATA_ID + " = ?",
                new String[]{messageItem.getDataId()}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            ContentValues values = new ContentValues();
            MsgsGrpDraftContentProvider.fillContentValuesFromMessageItem(messageItem, values);
            long _id = databaseHelper.insert(db, MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT, null, values);
            messageItem.set_id(_id);
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    MessageItemColumns.KEY_ID));
            messageItem.set_id(_id);
            String msg = cursor.getString(cursor.getColumnIndex(
                    MessageItemColumns.KEY_LATEST_MESSAGE));
            String sender = cursor.getString(cursor.getColumnIndex(
                    MessageItemColumns.KEY_DOC_SENDER));
            cursor.close();
//            // TODO: 18-05-2016
//            why this update on latest text and sender..
//            not required.. investigate
/*
            String latestMessageText = item.getLatestMessageText();
            if (null == latestMessageText || "".equals(latestMessageText))
                item.setLatestMessageText(msg);
            String docSender = item.getDocSender();
            if (null == docSender || "".equals(docSender))
                item.setDocSender(sender);
*/
            ContentValues values = new ContentValues();
            MsgsGrpDraftContentProvider.fillContentValuesFromMessageItem(messageItem, values);
            // updating row
            databaseHelper.update(db, MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT, values,
                    MessageItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(messageItem.getDataId())});
        }
//        creates email thread
//        // TODO: 16-05-2016
//        transform email items to a group contact item.. just like chat
//        // TODO: 30-06-2016
//        createOrUpdateMessageThreadEmails(db, item.getEmails(), item.get_id());

//        creates chat thread
//        check if group contact exists for message thread in db
//            joins if exists
//        if message thread exists, join
//        checks group contact, msg thread join first
        String ctidCol = MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID;
        cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
                new String[]{ctidCol},
                MessageItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(messageItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            ctidCol = ContactItemColumns.KEY_ID;
//            throws sql execeptions here.. to break transaction
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ctidCol},
                    ContactItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{messageItem.getDataId()}, null
            );
        }
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            ContactItem contactItem = messageItem.getContactItem();
            if (null == contactItem) {
//            throws sql execeptions here.. to break transaction
            } else {
//            saves or updates msgthreadcontact first
                long id = MsgEmailContactsContentProvider.createOrUpdateMessagesContacts(
                        databaseHelper, db, contactItem);
                Set<ContactItem> contactItems = contactItem.getContactItems();
                if (contactItems != null && !contactItems.isEmpty())
//                only for group chat
//            // TODO: 16-05-2016
//            single chat can also hold a single group contact item with a single intellibitz contact
                    MsgEmailContactsContentProvider.createOrUpdateMessagesContactsJoin(
                            databaseHelper, db, contactItem, messageItem.get_id());
//                the individual contact does not require updating, since its already linked by group
//            // TODO: 16-05-2016
//            link only the group
//                createOrUpdateMsgThreadGroupContactsJoin(contacts, item.get_id());
            }

        } else {
//                group found, join
            long _id = cursor.getLong(cursor.getColumnIndex(ctidCol));
            cursor.close();
            MsgEmailContactsContentProvider.createOrUpdateMessagesContactsJoin(
                    databaseHelper, db, messageItem.get_id(), _id);
        }
        return messageItem.get_id();
    }

    public static long createOrUpdateMessages(
            DatabaseHelper databaseHelper, MessageItem messageItem) {
        ArrayList<MessageItem> messageItems = new ArrayList<>(1);
        messageItems.add(messageItem);
        long[] ids = createOrUpdateMessages(databaseHelper, messageItems);
        if (null == ids || 0 == ids.length) return 0;
        return ids[0];
    }

    public static long[] createOrUpdateMessages(
            DatabaseHelper databaseHelper, Collection<MessageItem> messageItems) {
        long[] ids = new long[messageItems.size()];
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ids = createOrUpdateMessages(databaseHelper, db, messageItems);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ids;
    }

    public static long[] createOrUpdateMessages(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            Collection<MessageItem> messageItems) {
        long[] ids = new long[messageItems.size()];
        int i = 0;
        MessageItem[] array = messageItems.toArray(new MessageItem[0]);
        for (MessageItem messageItem : array) {
            long l = createOrUpdateMessages(databaseHelper, db, messageItem);
/*
            MsgsGrpPeopleContentProvider.createOrUpdateMsgsGrpPeople(
                    databaseHelper, db, messageItem);
*/
            if (0 == l) {
                Log.e(TAG, "Failed to insert row: " + messageItem);
                throw new SQLException("Failed to insert row into " + messageItem);
            }
            ids[i++] = l;
        }
        return ids;
    }

    /*
        public static List<MessageItem> savesMsgsGrpDraftFromJSON(
                JSONArray jsonArray, ContactItem user, Context context) throws
                JSONException, IOException {
            List<MessageItem> messageItems = new ArrayList<>();
            if (null == jsonArray || 0 == jsonArray.length()) return messageItems;
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                MessageItem messageItem =
                        savesMsgsGrpDraftFromJSON(
                                jsonArray.getJSONObject(i), user, context);
                if (messageItem != null)
                    messageItems.add(messageItem);
            }
            return messageItems;
        }

    */
    public static List<MessageItem> savesMsgsGrpDraftFromJSON(
            JSONArray jsonArray, ContactItem user, Context context) throws
            JSONException, IOException {
        List<MessageItem> messageItems = new ArrayList<>();
        if (null == jsonArray || 0 == jsonArray.length()) return messageItems;
        int count = jsonArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONObject draft = jsonObject.getJSONObject("draft");
            draft.put("_id", jsonObject.getString("_id"));
            draft.put("_rev", jsonObject.getString("_rev"));
            draft.put("doc_type", jsonObject.getString("doc_type"));
            draft.put("doc_owner", jsonObject.getString("doc_owner"));
            draft.put("timestamp", jsonObject.getString("timestamp"));
            draft.put("datetime", jsonObject.optString("datetime"));
            MessageItem messageItem =
                    savesMsgsGrpDraftFromJSON(draft, user, context);
            if (messageItem != null)
                messageItems.add(messageItem);
        }
        return messageItems;
/*
{
    "status": 1,
    "err": "",
    "drafts": [
        {
            "_id": "16ac66f9e55ca22368ef9e915af75f48",
            "_rev": "1-1bcc5021b9721971becffa742ab4d901",
            "doc_type": "DRAFT",
            "doc_owner": "USRMASTER_919840348914",
            "timestamp": 1466870855,
            "draft": {
                "msg_type": "CHAT",
                "to_uid": "USRMASTER_919100504678",
                "chat_msg_ref": "USRMASTER_919840348914_USRMASTER_919100504678_1466870573075",
                "txt": "fbfnd"
            }
        },
        {
            "_id": "f0e55d1ad093fe99ef5b29e5998b586d",
            "_rev": "1-0e8eda0302070ebba12bc4f53c146376",
            "doc_type": "DRAFT",
            "doc_owner": "USRMASTER_919840348914",
            "timestamp": 1466871049,
            "draft": {
                "msg_type": "CHAT",
                "doc_type": "DRAFT",
                "chat_id": "USRMASTER_919100504678",
                "from_name": "Muthu Ramadoss",
                "to_uid": "USRMASTER_919100504678",
                "to_type": "USER",
                "chat_msg_ref": "USRMASTER_919840348914_USRMASTER_919100504678_1466870892364",
                "txt": "fhdndb"
            }
        }
    ]
}
         */
    }

    public static Uri savesMessageItem(
            MessageItem messageItem, ContactItem user, Context context) throws
            IOException, JSONException {
        if (messageItem.isDraft() || messageItem.isEmailItem()) {
            if (TextUtils.isEmpty(messageItem.getChatId()))
                messageItem.setChatId(messageItem.getToUid());
        }
        if (messageItem.isChat() && TextUtils.isEmpty(messageItem.getChatId())) {
            Log.e(TAG, "savesMsgsGrpDraftFromJSON: ChatID cannot be NULL - : " + messageItem);
            return null;
        }
        if (messageItem.isEmailItem() && TextUtils.isEmpty(messageItem.getToUid())) {
            Log.e(TAG, "savesMsgsGrpDraftFromJSON: ToUID cannot be NULL - : " + messageItem);
            return null;
        }
        MessageItem messageThreadItem = new MessageItem();
        String chatId = messageItem.getChatId();
        //                    gets message thread by data id
// // TODO: 05-03-2016
//                    gets message thread by message to_uid and save message
//                    gets message thread by data id
        Cursor cursor = context.getApplicationContext().getContentResolver().query(
                Uri.withAppendedPath(CONTENT_URI, chatId),
                new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{chatId}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//                        // TODO: 06-03-2016
//                        message has arrived, without a thread in local db
//                        fetch the thread from the cloud and sync to local db
//            // TODO: 13-03-2016
//            hack !! createFileInES a demo thread, and attach the message item
//            temp fix.. put them in a cache, and wait for the real msg thread to arrive - // TODO: 13-03-2016
//            messageThreadItem = createDemoMessageThread();
            messageThreadItem = createMessagesFromMessage(
                    messageItem, user);
//            Uri uri = saveMessageItemToDB(messageThreadItem);
//            _id = ContentUris.parseId(uri);
//            // TODO: 13-03-2016
//            do not alter the message item, coming in from the intellibitz cloud
//            hack!! do this to still show the message in the demo theread
//            messageItem.setToUid(messageThreadItem.getDataId());
        } else {
/*
            messageThreadItem =
                    createsMessageItemFromCursor(cursor);
*/
            long _id = cursor.getLong(cursor.getColumnIndex(
                    MessageItemColumns.KEY_ID));
            messageThreadItem.set_id(_id);
/*
            messageThreadItem.setDataId(cursor.getString(cursor.getColumnIndex(
                    MessageItemColumns.KEY_DATA_ID)));
*/
//            the message thread .. updated with the latest message.. always
            fillMessagesFromMessage(
                    messageThreadItem, messageItem, user);
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(BaseItem.THREAD,
                MainApplicationSingleton.Serializer.serialize(messageThreadItem));
        values.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(messageItem));
        return context.getApplicationContext().getContentResolver().insert(CONTENT_URI, values);
        //        Log.e(TAG, " MessageThread updated: " + uri);
//        Log.e(TAG, " Message saved: " + uri);
/*
        if (messageItem.getAttachments() != null && !messageItem.getAttachments().isEmpty()) {
//        fires off async attachment link fetch
            emitGetAttachmentsAndUpdateFilePathInDB(messageItem, user);
        }
*/
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
            case MSGSGRPDRAFT_DIR_TYPE:
                return MSGSGRPDRAFT_DIR_MIME_TYPE;
            case MSGSGRPDRAFT_JOIN_DIR_TYPE:
                return MSGSGRPDRAFT_JOIN_DIR_MIME_TYPE;
            case MSGSGRPDRAFT_ITEM_TYPE:
                return MSGSGRPDRAFT_ITEM_MIME_TYPE;
            case MSGSGRPDRAFT_DATA_ITEM_TYPE:
                return MSGSGRPDRAFT_DATA_ITEM_MIME_TYPE;
            case MSGSGRPDRAFT_JOIN_ITEM_TYPE:
                return MSGSGRPDRAFT_JOIN_ITEM_MIME_TYPE;
            case MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE:
                return MSGSGRPDRAFT_JOIN_DATA_ITEM_MIME_TYPE;
            case MSGSGRPDRAFT_RAW_DIR_TYPE:
                return MSGSGRPDRAFT_RAW_DIR_MIME_TYPE;
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
            case MSGSGRPDRAFT_DIR_TYPE:
            case MSGSGRPDRAFT_ITEM_TYPE:
            case MSGSGRPDRAFT_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_MSGSGRPDRAFT,
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
            case MSGSGRPDRAFT_JOIN_DIR_TYPE:
                try {
                    cursor = getMessageThreadShalGroupJoin(databaseHelper,
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
            case MSGSGRPDRAFT_JOIN_ITEM_TYPE:
            case MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = getMessageThreadJoin(databaseHelper,
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
            case MSGSGRPDRAFT_RAW_DIR_TYPE:
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
//        byte[] vals = values.getAsByteArray(MessageItem.TAG);
        byte[] vals = values.getAsByteArray(BaseItem.THREAD);
        byte[] vals2 = values.getAsByteArray(MessageItem.TAG);
        switch (URI_MATCHER.match(uri)) {
            case MSGSGRPDRAFT_DIR_TYPE:
                try {
/*
                    MessageItem messageThreadItem = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    MessageItem messageItem = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals2);
*/
                    long id = databaseHelper.insert(TABLE_MSGSGRPDRAFT, null, values);
/*
                    long id = createOrUpdateMessagesMessage(databaseHelper,
                            messageItem, messageThreadItem);
*/
                    Uri insertUri = ContentUris.withAppendedId(uri, id);
/*
                    Uri messageThreadUri = ContentUris.withAppendedId(
                            MessagesContentProvider.CONTENT_URI, messageThreadItem.get_id());
*/
                    Context context = getContext();
                    if (null != context) {
//                        context.getContentResolver().notifyChange(messageThreadUri, null);
                        context.getContentResolver().notifyChange(insertUri, null);
                    }
                    return insertUri;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            case MSGSGRPDRAFT_JOIN_DIR_TYPE:
                try {
                    Collection<MessageItem> items = (Collection<MessageItem>)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    if (null == items) {
                        return null;
                    }
                    long[] ids = createOrUpdateMessages(databaseHelper, items);
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
            case MSGSGRPDRAFT_ITEM_TYPE:
            case MSGSGRPDRAFT_DATA_ITEM_TYPE:
                try {
                    MessageItem item = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    long id = createOrUpdateMessages(databaseHelper, item);
                    Uri insertUri = ContentUris.withAppendedId(
                            MsgsGrpDraftContentProvider.CONTENT_URI, id);
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
            case MSGSGRPDRAFT_DIR_TYPE:
            case MSGSGRPDRAFT_ITEM_TYPE:
            case MSGSGRPDRAFT_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.update(TABLE_MSGSGRPDRAFT, values, where, whereArgs);
                    updateUri = ContentUris.withAppendedId(
                            MsgsGrpDraftContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case MSGSGRPDRAFT_JOIN_DIR_TYPE:
            case MSGSGRPDRAFT_JOIN_ITEM_TYPE:
            case MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE:
                try {
                    byte[] vals1 = values.getAsByteArray(MessageItem.TAG);
                    MessageItem item = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals1);
                    id = (int) createOrUpdateMessages(databaseHelper, item);
                    updateUri = ContentUris.withAppendedId(MsgsGrpDraftContentProvider.CONTENT_URI, id);
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
            case MSGSGRPDRAFT_DIR_TYPE:
            case MSGSGRPDRAFT_ITEM_TYPE:
            case MSGSGRPDRAFT_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.delete(
                            TABLE_MSGSGRPDRAFT, where, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MsgsGrpDraftContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(delUri, null);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case MSGSGRPDRAFT_JOIN_DIR_TYPE:
            case MSGSGRPDRAFT_JOIN_ITEM_TYPE:
            case MSGSGRPDRAFT_JOIN_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.delete(
                            TABLE_MSGSGRPDRAFT, where, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MsgsGrpDraftContentProvider.CONTENT_URI, id);
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
