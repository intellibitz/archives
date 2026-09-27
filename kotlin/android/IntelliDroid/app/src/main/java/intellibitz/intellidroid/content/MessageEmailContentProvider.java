package intellibitz.intellidroid.content;

import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.ContactsContactJoinColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.IntellibitzItemColumns;
import intellibitz.intellidroid.db.MessageAttachmentJoinColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.db.MessagesContactsJoinColumns;
import intellibitz.intellidroid.db.MessagesMessageJoinColumns;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.*;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static intellibitz.intellidroid.content.MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN;
import static intellibitz.intellidroid.db.IntellibitzItemColumns.KEY_DATA_ID;
import static intellibitz.intellidroid.db.IntellibitzItemColumns.KEY_ID;

public class MessageEmailContentProvider extends
        ContentProvider {
    public static final String TAG = "MessageEmailCP";

    // 1 on 1 JOINS
    public static final String TABLE_MESSAGESEMAIL_CONTACTS_JOIN = "messagesemail_contacts";
    public static final String CREATE_TABLE_MESSAGESEMAIL_CONTACTS_JOIN = "CREATE TABLE "
            + TABLE_MESSAGESEMAIL_CONTACTS_JOIN + "( " +
            IntellibitzItemColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " INTEGER," +
            MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + " INTEGER," +
            MessagesContactsJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    public static final String TABLE_MESSAGESEMAIL = "messagesemail";
    // Table Create Statements
    public static final String CREATE_TABLE_MESSAGESEMAIL_INDEX =
            "CREATE INDEX " +
                    TABLE_MESSAGESEMAIL + IntellibitzItemColumns.KEY_DATA_ID + DatabaseHelper.IDX +
                    " ON " +
                    TABLE_MESSAGESEMAIL + " (" +
                    IntellibitzItemColumns.KEY_DATA_ID + ")";
    public static final String TABLE_MESSAGESEMAIL_MESSAGE_JOIN = "messagesemail_message";
    public static final String CREATE_TABLE_MESSAGESEMAIL_MESSAGE_JOIN = "CREATE TABLE "
            + TABLE_MESSAGESEMAIL_MESSAGE_JOIN + "( " +
            IntellibitzItemColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            MessagesMessageJoinColumns.KEY_MESSAGE_ID + " INTEGER," +
            MessagesMessageJoinColumns.KEY_MSG_THREAD_ID + " INTEGER," +
            MessagesMessageJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    public static final String TABLE_MESSAGEEMAIL = "messageemail";
    public static final String TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN = "messageemail_attachments";
    public static final String CREATE_TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN = "CREATE TABLE "
            + TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN + "( " +
            IntellibitzItemColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID + " INTEGER," +
            MessageAttachmentJoinColumns.KEY_MESSAGE_ID + " INTEGER," +
            MessageAttachmentJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    public static final String TABLE_MESSAGEEMAIL_CNTSGRPBROADCAST_JOIN = "messageemail_cntsgrpbroadcast";
    public static final String TABLE_MESSAGEEMAIL_CONTACTS_JOIN = "messageemail_contacts";
    public static final String CREATE_TABLE_MESSAGEEMAIL_CONTACTS_JOIN = "CREATE TABLE "
            + TABLE_MESSAGEEMAIL_CONTACTS_JOIN + "( " +
            IntellibitzItemColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " INTEGER," +
            MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + " INTEGER," +
            MessagesContactsJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    public static final String CREATE_TABLE_MESSAGEEMAIL = "CREATE TABLE "
            + TABLE_MESSAGEEMAIL + MessageItemColumns.MESSAGECHAT_SCHEMA;
    public static final String CREATE_TABLE_MESSAGESEMAIL = "CREATE TABLE "
            + TABLE_MESSAGESEMAIL + MessageItemColumns.MESSAGECHAT_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String MESSAGEEMAIL_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String MESSAGEEMAIL_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join";
    public static final String MESSAGEEMAIL_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String MESSAGEEMAIL_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String MESSAGEEMAIL_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String MESSAGEEMAIL_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String MESSAGEEMAIL_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    // UriMatcher stuff
    private static final int MESSAGEEMAIL_DIR_TYPE = 1;
    private static final int MESSAGEEMAIL_JOIN_DIR_TYPE = 2;
    private static final int MESSAGEEMAIL_ITEM_TYPE = 3;
    private static final int MESSAGEEMAIL_DATA_ITEM_TYPE = 4;
    private static final int MESSAGEEMAIL_JOIN_ITEM_TYPE = 5;
    private static final int MESSAGEEMAIL_JOIN_DATA_ITEM_TYPE = 6;
    private static final int MESSAGEEMAIL_RAW_DIR_TYPE = 7;
    private static final int SEARCH_SUGGEST = 8;
    private static final int REFRESH_SHORTCUT = 9;
    public static String AUTHORITY =
            "intellibitz.intellidroid.content.MessageEmailContentProvider";
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_MESSAGEEMAIL);
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + MessageEmailContentProvider.TABLE_MESSAGEEMAIL);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MESSAGEEMAIL);
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_MESSAGEEMAIL,
                MESSAGEEMAIL_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MESSAGEEMAIL,
                MESSAGEEMAIL_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MESSAGEEMAIL +
                "/#", MESSAGEEMAIL_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MESSAGEEMAIL +
                "/*", MESSAGEEMAIL_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MESSAGEEMAIL +
                        "/#", MESSAGEEMAIL_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MESSAGEEMAIL +
                        "/*", MESSAGEEMAIL_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_MESSAGEEMAIL, MESSAGEEMAIL_RAW_DIR_TYPE);
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

    public static MessageItem createsChatMessageItemFromJSON(
            JSONObject jsonObject) throws
            JSONException {
        MessageItem messageItem = new MessageItem();
        ContactItem contactThreadItem = new ContactItem();
//        a chat message turns off email
        contactThreadItem.setEmailItem(false);
        messageItem.setContactItem(contactThreadItem);
        String id = jsonObject.optString("_id");
        if (!TextUtils.isEmpty(id))
            messageItem.setDataId(id);
        String rev = jsonObject.optString("_rev");
        if (!TextUtils.isEmpty(rev))
            messageItem.setDataRev(rev);
        String to_type = jsonObject.optString("to_type");
        if (!TextUtils.isEmpty(to_type))
            messageItem.setToType(to_type);
        String from_uid = jsonObject.optString("from_uid");
        if (!TextUtils.isEmpty(from_uid)) {
            messageItem.setFromUid(from_uid);
//        // TODO: 30-06-2016
//            hack.. to identify the last message sender
//        hack
//        // TODO: 21-06-2016
//        remove this hack and provide a separate column for message thread for the latest message
            messageItem.setDocSenderEmail(messageItem.getFromUid());
        }
        String chat_id = jsonObject.optString("chat_id");
        if (!TextUtils.isEmpty(chat_id)) {
            messageItem.setChatId(chat_id);
            messageItem.setToChatUid(messageItem.getChatId());
//            only one msg thread contact per message and message thread
//            the msg thread contact is the latest assembles of contacts
            contactThreadItem.setIntellibitzId(messageItem.getChatId());
            contactThreadItem.setDataId(messageItem.getChatId());
            contactThreadItem.setTypeId(messageItem.getChatId());
            contactThreadItem.setEmailItem(false);
//            // TODO: 01-07-2016
            ContactItem contactItem = new ContactItem();
            contactItem.setIntellibitzId(contactThreadItem.getIntellibitzId());
            contactItem.setDataId(contactThreadItem.getIntellibitzId());
            contactItem.setTypeId(contactThreadItem.getTypeId());
//            single chat only.. group chat would be taken care by group info doc type coming in socket
//            IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(messageItem.getChatId());
            if ("GROUP".equalsIgnoreCase(messageItem.getToType())) {
//                indicates this msg contact itself is a group
                contactThreadItem.setGroup(true);
//                to indicate this msg contact, is part of a group
                contactItem.setGroup(true);
            }
            contactItem.setType(messageItem.getToType());
            contactItem.setEmailItem(false);
            contactThreadItem.addContact(contactItem);
        }
        String txt = jsonObject.optString("txt");
        if (!TextUtils.isEmpty(txt))
            messageItem.setText(txt);
        String to_uid = jsonObject.optString("to_uid");
        if (!TextUtils.isEmpty(to_uid))
            messageItem.setToUid(to_uid);
        String from_name = jsonObject.optString("from_name");
        if (!TextUtils.isEmpty(from_name))
            messageItem.setFromName(from_name);
        String msg_type = jsonObject.optString("msg_type");
        if (!TextUtils.isEmpty(msg_type)) {
            messageItem.setMessageType(msg_type);
            messageItem.setType(messageItem.getMessageType());
        }
        String doc_type = jsonObject.optString("doc_type");
        if (!TextUtils.isEmpty(doc_type)) {
            messageItem.setDocType(doc_type);
            messageItem.setBaseType(doc_type);
        }
        long timestamp = jsonObject.optLong("timestamp");
        if (timestamp > 0)
            messageItem.setTimestamp(timestamp);
        String datetime = jsonObject.optString("datetime");
        if (datetime != null)
            messageItem.setDateTime(datetime);
        String msg_ref = jsonObject.optString("msg_ref");
        if (!TextUtils.isEmpty(msg_ref))
            messageItem.setMsgRef(msg_ref);
        String chat_msg_ref = jsonObject.optString("client_msg_ref");
        if (!TextUtils.isEmpty(chat_msg_ref))
            messageItem.setChatMsgRef(chat_msg_ref);
        String doc_owner = jsonObject.optString("doc_owner");
        if (!TextUtils.isEmpty(doc_owner))
            messageItem.setDocOwner(doc_owner);
        String msg_direction = jsonObject.optString("msg_direction");
        if (!TextUtils.isEmpty(msg_direction))
            messageItem.setMessageDirection(msg_direction);
        String chat_name = jsonObject.optString("chat_name");
        if (!TextUtils.isEmpty(chat_name))
            messageItem.setName(chat_name);
// if read, then sets it true in message
        if (jsonObject.optBoolean("read", false)) {
            messageItem.setRead(1);
        } else {
//            messageItem.setRead(0);
        }
        int pending_docs = jsonObject.optInt("pending_docs");
        if (pending_docs > 0)
            messageItem.setPendingDocs(pending_docs);
//        sets the chat subject - the name of the chat
        String name = messageItem.getName();
        if (!TextUtils.isEmpty(name))
            messageItem.setSubject(name);
//        sets the doc sender
        String fromName = messageItem.getFromName();
        if (!TextUtils.isEmpty(fromName))
            messageItem.setDocSender(fromName);
        JSONArray attachments = jsonObject.optJSONArray("attachments");
        setChatAttachmentsFromJSONArray(messageItem, attachments);


        return messageItem;
//        touid of chat, is stored not in the touid
//        but in the tochatuid of the message item
/*
        messageItem.setToChatUid(jsonObject.optString("to_uid"));
        if (null == messageItem.getName()) {
            messageItem.setName(messageItem.getToChatUid());
        }
*/
/*
[
    {
        "_id": "6427d48d08db02e31100bddbaeb692f7",
        "_rev": "1-18a794e209bbf32de9ec97fe3565212a",
        "attachments": [],
        "txt": "Hi ",
        "to_uid": "USRMASTER_919100504678",
        "msg_type": "CHAT",
        "client_msg_ref": "3d6d3a61223c48afe38cc7f8132c5a6c_USRMASTER_919100504678_1467440159322.685059",
        "to_type": "USER",
        "doc_type": "MSG",
        "timestamp": 1467440159,
        "from_uid": "USRMASTER_919600037000",
        "from_name": "Jeff ",
        "msg_ref": "USRMASTER_919600037000:1467440159-8250",
        "doc_owner": "USRMASTER_919100504678",
        "chat_id": "USRMASTER_919600037000",
        "msg_direction": "IN",
        "chat_name": "Jeff "
    }
]
         */
/*
    {
        "_id": "e88d83fc4da9d4849fe72b7c99201dbb",
        "_rev": "17-4d37762bbdf91e905276011e25f01c7d",
        "txt": "hello sir ",
        "to_uid": "USRMASTER_919840348914",
        "msg_type": "CHAT",
        "attachments": [],
        "doc_type": "MSG",
        "timestamp": 1463739413,
        "from_uid": "USRMASTER_919600037000",
        "from_name": "Jeff",
        "msg_ref": "USRMASTER_919600037000:1463739413-9946",
        "to_type": "USER",
        "doc_owner": "USRMASTER_919840348914",
        "chat_id": "USRMASTER_919600037000",
        "msg_direction": "IN",
        "chat_name": "Jeff",
        "read": true
    }
         */
    }

    public static MessageItem createsEmailMessageItemFromJSON(JSONObject jsonObject, ContactItem user)
            throws JSONException {
        if (null == jsonObject) return null;
        MessageItem messageItem = new MessageItem();
        String id = jsonObject.optString("_id");
        if (!TextUtils.isEmpty(id))
            messageItem.setDataId(id);
        String rev = jsonObject.optString("_rev");
        if (!TextUtils.isEmpty(rev))
            messageItem.setDataRev(rev);

        String to_uid = jsonObject.optString("to_uid");
        if (!TextUtils.isEmpty(to_uid)) {
            messageItem.setToUid(to_uid);
//        touid is set as chat id for email also..
// to identify the messages between email and chat in a uniform way for delete
            messageItem.setChatId(messageItem.getToUid());
            messageItem.setToChatUid(messageItem.getToUid());
            messageItem.setThreadId(messageItem.getToUid());
            messageItem.setThreadIdRef(messageItem.getToUid());
        }
        createsContactsFromMessage(messageItem);
        JSONArray emails = jsonObject.optJSONArray("emails");
        createsEmailContactsForMessageFromJsonArray(messageItem, emails);

        String doc_type = jsonObject.optString("doc_type");
        if (!TextUtils.isEmpty(doc_type)) {
            messageItem.setBaseType(doc_type);
            messageItem.setDocType(doc_type);
        }
        int pending_docs = jsonObject.optInt("pending_docs");
        if (pending_docs > 0)
            messageItem.setPendingDocs(pending_docs);
        String doc_owner = jsonObject.optString("doc_owner");
        if (!TextUtils.isEmpty(doc_owner))
            messageItem.setDocOwner(doc_owner);
        String doc_owner_email = jsonObject.optString("doc_owner_email");
        if (!TextUtils.isEmpty(doc_owner_email))
            messageItem.setDocOwnerEmail(doc_owner_email);
        String sub = jsonObject.optString("sub");
        if (!TextUtils.isEmpty(sub))
            messageItem.setSubject(sub);
        String txt = jsonObject.optString("txt");
        if (!TextUtils.isEmpty(txt))
            messageItem.setText(txt);
        String full_txt = jsonObject.optString("full_txt");
        if (!TextUtils.isEmpty(full_txt))
            messageItem.setFullText(full_txt);
        String html = jsonObject.optString("html");
        if (!TextUtils.isEmpty(html))
            messageItem.setHtml(html);
        String msg_direction = jsonObject.optString("msg_direction");
        if (!TextUtils.isEmpty(msg_direction))
            messageItem.setMessageDirection(msg_direction);
        String from_name = jsonObject.optString("from_name");
        if (!TextUtils.isEmpty(from_name))
            messageItem.setFromName(from_name);
        String from_email = jsonObject.optString("from_email");
        if (!TextUtils.isEmpty(from_email))
            messageItem.setFromEmail(from_email);
        String msg_type = jsonObject.optString("msg_type");
        if (!TextUtils.isEmpty(msg_type))
            messageItem.setMessageType(msg_type);
        messageItem.setType(messageItem.getMessageType());
        String msg_attch_uid = jsonObject.optString("msg_attch_uid");
        if (!TextUtils.isEmpty(msg_attch_uid))
            messageItem.setMessageAttachId(msg_attch_uid);
        boolean broadcast = jsonObject.optBoolean("broadcast");
        if (broadcast)
            messageItem.setBroadcast(broadcast);
        boolean no_text = jsonObject.optBoolean("no_text");
        if (no_text)
            messageItem.setNoText(no_text);
        String flags = jsonObject.optString("flags");
        if (!TextUtils.isEmpty(flags))
            messageItem.setFlags(flags);
        long timestamp = jsonObject.optLong("timestamp");
        if (timestamp > 0)
            messageItem.setTimestamp(timestamp);
        String datetime = jsonObject.optString("datetime");
        if (datetime != null)
            messageItem.setDateTime(datetime);

        messageItem.setHtml(messageItem.getHtml());
        messageItem.setNoText(messageItem.isNoText());
        if (!messageItem.isRead() &&
                !user.getDataId().equals(messageItem.getFromUid())) {
//            // TODO: 22-05-2016
//            to increment from previous count.. get the latest count from DB
            messageItem.setUnreadCount(messageItem.getUnreadCount() + 1);
        }
//        latest message is written, only if message timestamp is newer than the previous
        long ts = messageItem.getTimestamp();
//        long mts = messageThreadItem.getLatestMessageTimestamp();
//        // TODO: 12-07-2016
//        do this elsewhere, with a cursor to cheeck for exisitng latest time stap
//        if (ts > mts) {
        messageItem.setLatestMessageText(messageItem.getText());
        messageItem.setLatestMessageTimestamp(ts);
        messageItem.setText(messageItem.getText());
//        }
//        sets unread increment, only if the incoming message read flag is not true
//        sets unread increment, for others messages NOT self messages
        if (!messageItem.isRead() &&
                !user.getDataId().equals(messageItem.getFromUid())) {
//            // TODO: 22-05-2016
//            to increment from previous count.. get the latest count from DB
            messageItem.setUnreadCount(messageItem.getUnreadCount() + 1);
        }
        String dt = messageItem.getDateTime();
        if (dt != null) {
            String mdt = messageItem.getDateTime();
            if (null == mdt) {
                messageItem.setDateTime(dt);
            } else {
                long dts = MainApplicationSingleton.getDateTimeMillisISO(dt);
                long mdts = MainApplicationSingleton.getDateTimeMillisISO(mdt);
                if (0 == mdts || dts < mdts) {
                    messageItem.setDateTime(dt);
                }
            }
        }

        JSONArray attachments = jsonObject.optJSONArray("attachments");
        setAttachmentsFromJSONArray(messageItem, attachments);
        return messageItem;
/*
[
    {
        "_id": "6427d48d08db02e31100bddbaeb692f7",
        "_rev": "1-18a794e209bbf32de9ec97fe3565212a",
        "attachments": [],
        "txt": "Hi ",
        "to_uid": "USRMASTER_919100504678",
        "msg_type": "CHAT",
        "client_msg_ref": "3d6d3a61223c48afe38cc7f8132c5a6c_USRMASTER_919100504678_1467440159322.685059",
        "to_type": "USER",
        "doc_type": "MSG",
        "timestamp": 1467440159,
        "from_uid": "USRMASTER_919600037000",
        "from_name": "Jeff ",
        "msg_ref": "USRMASTER_919600037000:1467440159-8250",
        "doc_owner": "USRMASTER_919100504678",
        "chat_id": "USRMASTER_919600037000",
        "msg_direction": "IN",
        "chat_name": "Jeff "
    }
]
         */
/**
 * [{"_id":"439420713fa23c89fd388b951701a9fd","_rev":"1-0a8e38d3aeb2d17f64ab033c71a3ccc0",
 * "doc_type":"MSG","doc_owner":"USRMASTER_919840348914","doc_owner_email":"muthu@intellibitz.com",
 * "timestamp":1457776931,"msg_type":"EMAIL","msg_direction":"OUT","txt":"",
 * "html":"\n","attachments":["https:\/\/intellibitz-uploads.s3-ap-southeast-1.amazonaws.com\/email-attachments\/USRMASTER_919840348914\/1457776931_IMG_20160307_194245.jpg"],
 * "from_email":"muthu@intellibitz.com","from_name":"Muthuselvam RS",
 * "to_uid":"439420713fa23c89fd388b9517019ca1"}]
 */
/*
"{
   ""_id"": ""fdcdf32367129dac48c9b71f41d8b672"", (Unique message ID)
   ""_rev"": ""1-928f5a36034f1a77f5d5cf8a864b9db3"",
   ""msg_type"": ""CHAT"",
   ""txt"": ""ohohohoh"",
   ""to_type"": ""GROUP"",
   ""to_uid"": ""aktprototype"",
   ""doc_type"": ""MSG"",
   ""timestamp"": 1461922961,
   ""from_uid"": ""USRMASTER_919600037000"",
   ""from_name"": ""Jeff"",
   ""doc_owner"": ""USRMASTER_919600037000"",
   ""chat_id"": ""aktprototype"",
   ""msg_direction"": ""OUT"",
   ""broadcast"": true (true if sent as broadcast -- use it for UI)
   ""msg_ref"": ""USRMASTER_919600037000:1462893832-3885"" (This is used for refering this message on other docs, such as delivery / read receipts)
}"
         */
    }

    public static MessageItem createsEmailContactsForMessageFromJsonArray(
            MessageItem messageItem, JSONArray jsonArray) throws JSONException {
        if (null == jsonArray || 0 == jsonArray.length()) {
            Log.e(TAG, " contacts json array is EMPTY");
            return messageItem;
        }
        final ContactItem contacts = messageItem.getContactItem();
        if (null == contacts) {
            Log.e(TAG, " contacts is NULL in message for json array " + jsonArray);
            return messageItem;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String email = jsonObject.optString("email");
            String name = jsonObject.optString("name");
            String type = jsonObject.optString("type");
            ContactItem contactItem = new ContactItem();
            contactItem.setDataId(email);
            contactItem.setTypeId(email);
            contactItem.setIntellibitzId(email);
            contactItem.setName(name);
            contactItem.setType(type);
            contactItem.setGroup(false);
            contactItem.setEmailItem(true);
            if ("from".equalsIgnoreCase(type)) {
                if (!TextUtils.isEmpty(email)) {
                    messageItem.setFromEmail(email);
                    messageItem.setFrom(email);
                    messageItem.setDocSenderEmail(email);
                    messageItem.setIntellibitzId(email);
                }
                if (!TextUtils.isEmpty(name)) {
                    messageItem.setFromName(name);
                    messageItem.setDocSender(name);
                }
            }
            contacts.addContact(contactItem);
        }
/*
            String from = messageItem.getDocSenderEmail();
            EmailTags emailTags = new EmailTags(from, messageItem.getEmails()).invoke();
            messageItem.setTo(emailTags.getTo());
            messageItem.setCc(emailTags.getCc());
            messageItem.setBcc(emailTags.getBcc());
*/
        return messageItem.invoke();
    }

    @NonNull
    public static ContactItem createsContactsFromMessage(MessageItem messageItem) {
        //            only one msg thread contact per message and message thread
//            the msg thread contact is the latest assembles of contacts
        ContactItem contactThreadItem = new ContactItem();
        contactThreadItem.setTypeId(messageItem.getToUid());
        contactThreadItem.setIntellibitzId(messageItem.getToUid());
        contactThreadItem.setDataId(messageItem.getToUid());
        contactThreadItem.setGroupId(messageItem.getToUid());
        contactThreadItem.setGroup(false);
        contactThreadItem.setEmailItem(true);
        messageItem.setContactItem(contactThreadItem);
        return contactThreadItem;
    }

    public static ContactItem createsContactsFromGetRecentEmailsMessage(MessageItem messageItem) {
        //            only one msg thread contact per message and message thread
//            the msg thread contact is the latest assembles of contacts

        ContactItem contacts = messageItem.getContactItem();
        if (null == contacts) {
            contacts = new ContactItem();
            messageItem.setContactItem(contacts);
        }
        contacts.setTypeId(messageItem.getToUid());
        contacts.setIntellibitzId(messageItem.getToUid());
        contacts.setDataId(messageItem.getToUid());
        contacts.setGroupId(messageItem.getToUid());
        contacts.setGroup(false);
        contacts.setEmailItem(true);
        return contacts;
    }

    public static void setAttachmentsFromJSONArray(MessageItem messageItem, JSONArray attachments) throws JSONException {
        if (null != attachments && attachments.length() > 0) {
            Set<MessageItem> items = new HashSet<>();
            for (int i = 0; i < attachments.length(); i++) {
                MessageItem attachmentItem = new MessageItem();
                JSONObject js = attachments.getJSONObject(i);
                attachmentItem.setMsgAttachID(messageItem.getMessageAttachId());
                attachmentItem.setPartID(js.getString("partID"));
                attachmentItem.setType(js.getString("type"));
                attachmentItem.setSubType(js.optString("subtype"));
                attachmentItem.setEncoding(js.optString("encoding"));
                attachmentItem.setSize(js.optInt("size"));
                attachmentItem.setLanguage(js.optString("language"));
                attachmentItem.setMd5(js.optString("md5"));
                attachmentItem.setDescription(js.optString("description"));
                JSONObject params = js.optJSONObject("params");
                JSONObject dispo = js.optJSONObject("disposition");
                if (null == params && dispo != null) {
                    params = dispo.optJSONObject("params");
                }
                if (null == params) {
                    attachmentItem.setDataId(messageItem.getDataId() +
                            messageItem.getMessageAttachId() + attachmentItem.getPartID());
                } else {
                    attachmentItem.setName(params.optString("name"));
                    if (null == attachmentItem.getName()) {
                        attachmentItem.setName(params.optString("filename"));
                    }
                    attachmentItem.setDataId(messageItem.getDataId() +
                            messageItem.getMessageAttachId() +
                            attachmentItem.getPartID() + attachmentItem.getName());
                }
//                attachmentItem.setDownloadURL(attachmentItem.getDataId());
                items.add(attachmentItem);
            }
            messageItem.setAttachments(items);
            messageItem.setHasAttachments(items.size());
        }
    }

    public static void setAttachmentsFromGetRecentEmails(
            MessageItem messageItem, JSONArray attachments) throws JSONException {
        if (null != attachments && attachments.length() > 0) {
            Set<MessageItem> items = new HashSet<>();
            for (int i = 0; i < attachments.length(); i++) {
                JSONObject js = attachments.getJSONObject(i);
                MessageItem attachmentItem = new MessageItem();
                attachmentItem.setMessageType(MessageItem.EMAIL);
                attachmentItem.setMessageDirection(messageItem.getMessageDirection());
                attachmentItem.setDocOwnerEmail(messageItem.getDocOwnerEmail());
                attachmentItem.setDocSenderEmail(messageItem.getDocSenderEmail());
                attachmentItem.setMsgAttachID(messageItem.getMessageAttachId());
                attachmentItem.setPartID(js.optString("partID"));
                attachmentItem.setType(js.optString("type"));
                attachmentItem.setSubType(js.optString("subtype"));
                attachmentItem.setEncoding(js.optString("encoding"));
                attachmentItem.setSize(js.optInt("size"));
                attachmentItem.setLanguage(js.optString("language"));
                attachmentItem.setMd5(js.optString("md5"));
                attachmentItem.setDescription(js.optString("description"));
                JSONObject params = js.optJSONObject("params");
                JSONObject dispo = js.optJSONObject("disposition");
                if (null == params && dispo != null) {
                    params = dispo.optJSONObject("params");
                }
                if (null == params) {
                    attachmentItem.setDataId(messageItem.getDataId() +
                            messageItem.getMessageAttachId() + attachmentItem.getPartID());
                } else {
                    String name = params.optString("name");
                    String filename = params.optString("filename");
                    if (TextUtils.isEmpty(filename)) {
                        filename = name;
                    }
                    attachmentItem.setName(name);
                    if (TextUtils.isEmpty(name)) {
                        attachmentItem.setName(filename);
                    }
                    attachmentItem.setDataId(messageItem.getDataId() +
                            messageItem.getMessageAttachId() +
                            attachmentItem.getPartID() + attachmentItem.getName());
                    attachmentItem.setDownloadURL(filename);
                }
                items.add(attachmentItem);
            }
            messageItem.setAttachments(items);
            messageItem.setHasAttachments(items.size());
        }
/*
            "attachments": "[{\"partID\":\"2\",\"type\":\"video\",\"subtype\":\"3gpp\",
            \"params\":{\"name\":\"VID_20140910_175711.3gp\"},\"id\":null,\"description\":null,
            \"encoding\":\"BASE64\",\"size\":4737012,\"md5\":null,
            \"disposition\":{\"type\":\"ATTACHMENT\",
            \"params\":{\"filename\":\"VID_20140910_175711.3gp\"}},\"language\":null}]",

         */
    }

    public static void setChatAttachmentsFromJSONArray(
            MessageItem messageItem, JSONArray jsonArray) throws
            JSONException {
        if (null != jsonArray && jsonArray.length() > 0) {
            Set<MessageItem> items = new HashSet<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                MessageItem attachmentItem = new MessageItem();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MsgEmailAttachmentContentProvider.setAttachmentItemFromJson(
                        attachmentItem, jsonObject);
                attachmentItem.setDataId(attachmentItem.getDownloadURL());
                items.add(attachmentItem);
            }
            messageItem.setAttachments(items);
            messageItem.setHasAttachments(items.size());
        }
    }

    public static Uri savesMessageItem(
            MessageItem messageItem, ContactItem user, Context context) throws
            IOException, JSONException {
        if (messageItem.isDraft() && TextUtils.isEmpty(messageItem.getChatId())) {
            messageItem.setChatId(messageItem.getToUid());
        }
        if (TextUtils.isEmpty(messageItem.getToUid())) {
            Log.e(TAG, "savesMsgDocTypeInDBFromJSON: ToUID cannot be NULL - : " + messageItem);
            return null;
        }
        MessageItem messageThreadItem = clonesMessagesFromMessage(messageItem, user);
        if (messageThreadItem != null) {
            String chatId = messageItem.getChatId();
            Cursor cursor = context.getApplicationContext().getContentResolver().query(
                    Uri.withAppendedPath(MessagesEmailContentProvider.CONTENT_URI, chatId),
                    new String[]{IntellibitzItemColumns.KEY_ID, MessageItemColumns.KEY_LATEST_MESSAGE_TS},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{chatId}, null);
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
            } else {
                long _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID));
                long mts = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE_TS));
                messageThreadItem.set_id(_id);
//        latest message is written, only if message timestamp is newer than the previous
                long ts = messageItem.getTimestamp();
                if (0 == mts || ts > mts) {
                    messageThreadItem.setLatestMessageText(messageItem.getText());
                    messageThreadItem.setLatestMessageTimestamp(ts);
                } else {
                    messageThreadItem.setLatestMessageText(null);
                    messageThreadItem.setLatestMessageTimestamp(0);
                }
                cursor.close();
            }
        }
        ContentValues values = new ContentValues();
        values.put(BaseItem.THREAD,
                MainApplicationSingleton.Serializer.serialize(messageThreadItem));
        values.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(messageItem));
        return context.getApplicationContext().getContentResolver().insert(CONTENT_URI, values);
    }

    public static Uri savesMessageItemFromGetRecentEmails(
            MessageItem messageItem, ContactItem user, Context context) throws
            IOException, JSONException {
        if (null == messageItem) return null;
        if (null == user) return null;
        if (null == context) return null;
/*
        if (messageItem.isDraft() && TextUtils.isEmpty(messageItem.getChatId())) {
            messageItem.setChatId(messageItem.getToUid());
        }
*/
        if (TextUtils.isEmpty(messageItem.getToUid())) {
            Log.e(TAG, "savesMsgDocTypeInDBFromJSON: ToUID cannot be NULL - : " + messageItem);
            return null;
        }
//        // TODO: 10/9/16
//        to work on a single message item, and NOT clone a thread
        MessageItem messageThreadItem = clonesMessagesFromMessageGetRecentEmails(messageItem, user);
        if (messageThreadItem != null) {
            String chatId = messageItem.getChatId();
            Cursor cursor = context.getApplicationContext().getContentResolver().query(
                    Uri.withAppendedPath(MessagesEmailContentProvider.CONTENT_URI, chatId),
                    new String[]{IntellibitzItemColumns.KEY_ID,
                            MessageItemColumns.KEY_LATEST_MESSAGE_TS,
                            MessageItemColumns.KEY_DATA_REV},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{chatId}, null);
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
            } else {
                String rev = cursor.getString(cursor.getColumnIndex(MessageItemColumns.KEY_DATA_REV));
                if (rev.equalsIgnoreCase(messageItem.getDataRev())) {
                    Log.d(TAG, "savesMessageItemFromGetRecentEmails: REV is equal - No changes detected - skipping..");
                    cursor.close();
                    return null;
                }
                long _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID));
                long mts = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE_TS));
                messageThreadItem.set_id(_id);
//        latest message is written, only if message timestamp is newer than the previous
                long ts = messageItem.getTimestamp();
                if (0 == mts || ts > mts) {
                    messageThreadItem.setLatestMessageText(messageItem.getText());
                    messageThreadItem.setLatestMessageTimestamp(ts);
                } else {
                    messageThreadItem.setLatestMessageText(null);
                    messageThreadItem.setLatestMessageTimestamp(0);
                }
                cursor.close();
            }
            ContentValues values = new ContentValues();
            values.put(BaseItem.THREAD,
                    MainApplicationSingleton.Serializer.serialize(messageThreadItem));
            values.put(MessageItem.TAG,
                    MainApplicationSingleton.Serializer.serialize(messageItem));
            return context.getApplicationContext().getContentResolver().insert(CONTENT_URI, values);
        }
        return null;
    }

    public static int updatesMessageItemFromGetFullEmails(
            MessageItem messageItem, Context context) throws
            IOException, JSONException {
/*
        if (messageItem.isDraft() && TextUtils.isEmpty(messageItem.getChatId())) {
            messageItem.setChatId(messageItem.getToUid());
        }
        if (TextUtils.isEmpty(messageItem.getToUid())) {
            Log.e(TAG, "savesMsgDocTypeInDBFromJSON: ToUID cannot be NULL - : " + messageItem);
            return null;
        }
        MessageItem messageThreadItem = clonesMessagesFromMessageGetRecentEmails(messageItem, user);
        if (messageThreadItem != null) {
            String chatId = messageItem.getChatId();
            Cursor cursor = context.getApplicationContext().getContentResolver().query(
                    Uri.withAppendedPath(MessagesEmailContentProvider.CONTENT_URI, chatId),
                    new String[]{KEY_ID, MessageItemColumns.KEY_LATEST_MESSAGE_TS},
                    KEY_DATA_ID + " = ? ",
                    new String[]{chatId}, null);
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
            } else {
                long _id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                long mts = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_LATEST_MESSAGE_TS));
                messageThreadItem.set_id(_id);
//        latest message is written, only if message timestamp is newer than the previous
                long ts = messageItem.getTimestamp();
                if (0 == mts || ts > mts) {
                    messageThreadItem.setLatestMessageText(messageItem.getText());
                    messageThreadItem.setLatestMessageTimestamp(ts);
                } else {
                    messageThreadItem.setLatestMessageText(null);
                    messageThreadItem.setLatestMessageTimestamp(0);
                }
                cursor.close();
            }
        }
*/
        ContentValues values = new ContentValues();
/*
        values.put(BaseItem.THREAD,
                MainApplicationSingleton.Serializer.serialize(messageItem));
        values.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(messageItem));
*/
        values.put(MessageItemColumns.KEY_TEXT, messageItem.getText());
        values.put(MessageItemColumns.KEY_HTML, messageItem.getHtml());
        values.put(MessageItemColumns.KEY_LATEST_MESSAGE, messageItem.getLatestMessageText());
        values.put(MessageItemColumns.KEY_LATEST_MESSAGE_TS, messageItem.getLatestMessageTimestamp());
        context.getApplicationContext().getContentResolver().update(
                RAW_CONTENT_URI, values, MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{messageItem.getDataId()});
        context.getApplicationContext().getContentResolver().update(
                MessagesEmailContentProvider.CONTENT_URI, values, MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{messageItem.getDataId()});
        context.getApplicationContext().getContentResolver().update(
                MsgsGrpClutterContentProvider.CONTENT_URI, values, MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{messageItem.getDataId()});
        int res = context.getApplicationContext().getContentResolver().update(
                MsgsGrpPeopleContentProvider.CONTENT_URI, values, MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{messageItem.getDataId()});
        return res;
    }

    public static int isGetRecentEmailsUpdateInDBFromJSONRequired(JSONObject jsonObject, Context context) {
        int result = -1;
        if (null == jsonObject) return result;
        JSONArray emails = jsonObject.optJSONArray("emails");
        if (null == emails || 0 == emails.length()) return result;
        int length = emails.length();
        JSONObject email = emails.optJSONObject(length - 1);
        if (null == email) return result;
        int msguid = email.optInt("msg_uid");
        if (0 == msguid) return result;
        Log.d(TAG, "isGetRecentEmailsUpdateInDBFromJSONRequired: checking if latest message is in db with msg id " + msguid);
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{String.valueOf(msguid)}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            return result;
        } else {
            result = cursor.getInt(cursor.getColumnIndex(MessageItemColumns.KEY_ID));
        }
        Log.d(TAG, "isGetRecentEmailsUpdateInDBFromJSONRequired: found latest message in db with db id " + result);
        return result;
    }

    public static int savesGetRecentEmailsInDBFromJSON(
            JSONObject jsonObject, ContactItem user, Context context) {
        if (null == jsonObject) return 0;
        int result = 0;
        int status = jsonObject.optInt("status");
        int fromseq = jsonObject.optInt("fromseq");
        String toseq = jsonObject.optString("toseq");
        String highestmodseq = jsonObject.optString("highestmodseq");
        String err = jsonObject.optString("err");
        JSONArray emails = jsonObject.optJSONArray("emails");
        return savesGetRecentEmails(emails, user, context);
    }

    public static int updatesGetFullEmailsInDBFromJSON(
            JSONObject jsonObject, ContactItem user, Context context) {
        if (null == jsonObject) return 0;
        int result = 0;
        int status = jsonObject.optInt("status");
        int fromseq = jsonObject.optInt("fromseq");
        String toseq = jsonObject.optString("toseq");
        String highestmodseq = jsonObject.optString("highestmodseq");
        String err = jsonObject.optString("err");
        JSONArray emails = jsonObject.optJSONArray("emails");
        result = updatesGetFullEmails(emails, user, context);
        return result;
    }

    public static int savesGetRecentEmails(JSONArray emails, ContactItem user, Context context) {
        if (null == emails) return 0;
        int result = 0;
        int length = emails.length();
        for (int i = 0; i < length; i++) {
            JSONObject email = emails.optJSONObject(i);
            if (email != null) {
                MessageItem messageItem = savesGetRecentEmail(user, context, email);
                if (null == messageItem) {

                } else {
                    result++;
                }
            }
        }
        return length;
    }

    public static int updatesGetFullEmails(JSONArray emails, ContactItem user, Context context) {
        if (null == emails) return 0;
        int result = 0;
        for (int i = 0; i < emails.length(); i++) {
            JSONObject email = emails.optJSONObject(i);
            if (email != null) {
                MessageItem messageItem = updatesGetFullEmail(user, context, email);
                if (null == messageItem) {

                } else {
                    result++;
                }
            }
        }
        return result;
    }

    public static MessageItem savesGetRecentEmail(ContactItem user, Context context, JSONObject email) {
        if (null == email) return null;
        try {
            MessageItem messageItem = createsMessageItemFromGetRecentEmailsJSON(email, user, context);
            if (null == messageItem) {
                Log.e(TAG, "savesMsgDocTypeInDBFromJSON: FAIL - : " + email);
                return null;
            }
            Uri uri = savesMessageItemFromGetRecentEmails(messageItem, user, context);
            if (uri != null) {
                long id = ContentUris.parseId(uri);
                messageItem.set_id(id);
            }
            return messageItem;
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.toString());
        }
        return null;
    }

    public static MessageItem updatesGetFullEmail(ContactItem user, Context context, JSONObject email) {
        if (null == email) return null;
        try {
            MessageItem messageItem = createsMessageItemFromGetFullEmailsJSON(email, user);
            if (null == messageItem) {
                Log.e(TAG, "savesMsgDocTypeInDBFromJSON: FAIL - : " + email);
                return null;
            }
            int count = updatesMessageItemFromGetFullEmails(messageItem, context);
            if (0 == count) {
//                Log.e(TAG, ":updatesGetFullEmail - failed " + messageItem);
            }
            return messageItem;
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.toString());
        }
        return null;
    }

    public static MessageItem createsEmailContactsFromGetRecentEmails(
            MessageItem messageItem, JSONObject jsonObject) throws JSONException {
        if (null == jsonObject || 0 == jsonObject.length()) {
            Log.e(TAG, " contacts json array is EMPTY");
            return messageItem;
        }
        final ContactItem contacts = messageItem.getContactItem();
        if (null == contacts) {
            Log.e(TAG, " contacts is NULL in message for json array " + jsonObject);
            return messageItem;
        }
        JSONArray from = jsonObject.optJSONArray("from");
        createsEmailContactsFromGetRecentEmails(messageItem, contacts, from, "from");
        JSONArray to = jsonObject.optJSONArray("to");
        createsEmailContactsFromGetRecentEmails(messageItem, contacts, to, "to");
        JSONArray cc = jsonObject.optJSONArray("cc");
        createsEmailContactsFromGetRecentEmails(messageItem, contacts, cc, "cc");
/*
            String from = messageItem.getDocSenderEmail();
            EmailTags emailTags = new EmailTags(from, messageItem.getEmails()).invoke();
            messageItem.setTo(emailTags.getTo());
            messageItem.setCc(emailTags.getCc());
            messageItem.setBcc(emailTags.getBcc());
*/
        return messageItem.invoke();
    }

    public static void createsEmailContactsFromGetRecentEmails(
            MessageItem messageItem, ContactItem contacts, JSONArray jsonArray, String type) {
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject emailTarget = jsonArray.optJSONObject(i);
                if (emailTarget != null) {
                    String name = emailTarget.optString("name");
                    String mailbox = emailTarget.optString("mailbox");
                    String host = emailTarget.optString("host");
                    String email = emailTarget.optString("address");
                    ContactItem contactItem = new ContactItem();
                    contactItem.setDataId(email);
                    contactItem.setTypeId(email);
                    contactItem.setIntellibitzId(email);
                    contactItem.setName(name);
                    contactItem.setType(type);
                    contactItem.setGroup(false);
                    contactItem.setEmailItem(true);
                    if ("from".equalsIgnoreCase(type)) {
                        if (!TextUtils.isEmpty(email)) {
                            messageItem.setFromEmail(email);
                            messageItem.setFrom(email);
                            messageItem.setDocSenderEmail(email);
                            messageItem.setIntellibitzId(email);
                        }
                        if (!TextUtils.isEmpty(name)) {
                            messageItem.setFromName(name);
                            messageItem.setDocSender(name);
                        }
                    }
                    contactItem.setIncludeTypeEquals(true);
                    contacts.addContact(contactItem);
                }
            }
        }
    }

    public static MessageItem createsMessageItemFromGetRecentEmailsJSON(
            JSONObject jsonObject, ContactItem user, Context context)
            throws JSONException {
        if (null == jsonObject) return null;
        int msguid = jsonObject.optInt("msg_uid");
        if (0 == msguid) return null;

        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) email = "";

        MessageItem messageItem = new MessageItem();
        messageItem.setDataId(String.valueOf(msguid));

        messageItem.setBaseType(MessageItem.EMAIL);
        messageItem.setDocType(MessageItem.MSG);
        messageItem.setType(MessageItem.EMAIL);

        String usr = jsonObject.optString("user");
        if (TextUtils.isEmpty(usr)) {
            messageItem.setUser(user.getDataId());
        } else {
            messageItem.setUser(usr);
        }
        String mailbox = jsonObject.optString("mailbox");
        messageItem.setMailbox(mailbox);
        String msgmodseq = jsonObject.optString("msg_modseq");
        if (!TextUtils.isEmpty(msgmodseq)) {
            messageItem.setDataRev(msgmodseq);
        }
        int msgseqno = jsonObject.optInt("msg_seqno");
        String date = jsonObject.optString("date");
        String dts = MainApplicationSingleton.getDateTimeISO(date);
        messageItem.setDateTime(dts);
        long ts = MainApplicationSingleton.getDateTimeMillisISO(date);
        messageItem.setTimestamp(ts);

        String flags = jsonObject.optString("flags");
        if (!TextUtils.isEmpty(flags))
            messageItem.setFlags(flags);

        String sub = jsonObject.optString("subject");
        if (!TextUtils.isEmpty(sub))
            messageItem.setSubject(sub);

//        // TODO: 3/9/16
//        remove this hack
//        // TODO: 3/9/16
//        chat id logic from sender, participants and subject and derive to uid
/*
        String to_uid = jsonObject.optString("to_uid");
        if (!TextUtils.isEmpty(to_uid)) {
            messageItem.setToUid(to_uid);
//        touid is set as chat id for email also..
// to identify the messages between email and chat in a uniform way for delete
            messageItem.setChatId(messageItem.getToUid());
            messageItem.setToChatUid(messageItem.getToUid());
            messageItem.setThreadId(messageItem.getToUid());
            messageItem.setThreadIdRef(messageItem.getToUid());
        }
*/

        messageItem.setToUid(messageItem.getDataId());
        messageItem.setChatId(messageItem.getToUid());
        messageItem.setToChatUid(messageItem.getToUid());
        messageItem.setThreadId(messageItem.getToUid());
        messageItem.setThreadIdRef(messageItem.getToUid());

        createsContactsFromGetRecentEmailsMessage(messageItem);
        createsEmailContactsFromGetRecentEmails(messageItem, jsonObject);
        if (createsToUidFromGetRecentEmails(context, messageItem)) {
            createsContactsFromGetRecentEmailsMessage(messageItem);
        }

        if (email.equalsIgnoreCase(messageItem.getFromEmail())) {
            messageItem.setMessageDirection("OUT");
        } else {
            messageItem.setMessageDirection("IN");
        }
        messageItem.setDocOwnerEmail(email);
        messageItem.setDocSenderEmail(messageItem.getFromEmail());
        messageItem.setDocSender(messageItem.getFrom());

        int attachmentsCount = jsonObject.optInt("attachments_count");
        if (attachmentsCount > 0) {
            messageItem.setMessageAttachId(messageItem.getDataId());
            messageItem.setHasAttachments(attachmentsCount);
            String attachments = jsonObject.optString("attachments");
            if (!TextUtils.isEmpty(attachments)) {
                JSONArray jsonArray = new JSONArray(attachments);
                setAttachmentsFromGetRecentEmails(messageItem, jsonArray);
            }
        }

        return messageItem;
    }

    private static boolean createsToUidFromGetRecentEmails(Context context, MessageItem messageItem) {
        String toUid = MsgsGrpClutterContentProvider.queryThreadId(messageItem.getSubject(),
                messageItem.getFrom(), context);
        if (TextUtils.isEmpty(toUid)) return false;
        String fromUid1 = toUid;
        ContactItem contacts = messageItem.getContactItem();
        HashSet<ContactItem> contactItems = contacts.getContactItems();
        if (contactItems != null && !contactItems.isEmpty()) {
            boolean otherFound = false;
            String otherUid2 = null;
            for (ContactItem contactItem : contactItems) {
                String type = contactItem.getType();
                if ("from".equals(type)) break;
                String email = contactItem.getTypeId();
                toUid = MsgsGrpClutterContentProvider.queryThreadId(messageItem.getSubject(),
                        email, context);
                if (TextUtils.isEmpty(toUid)) {
                    if ("from".equals(type)) return false;
                } else {
                    if ("from".equals(type)) {
                        fromUid1 = toUid;
                    } else {
                        otherUid2 = toUid;
                        otherFound = true;
                    }
                }
                if (otherFound) {
                    if (fromUid1.equals(otherUid2)) {
                        messageItem.setToUid(fromUid1);
                        messageItem.setChatId(messageItem.getToUid());
                        messageItem.setToChatUid(messageItem.getToUid());
                        messageItem.setThreadId(messageItem.getToUid());
                        messageItem.setThreadIdRef(messageItem.getToUid());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static MessageItem createsMessageItemFromGetFullEmailsJSON(JSONObject jsonObject, ContactItem user)
            throws JSONException {
        if (null == jsonObject) return null;
        int msguid = jsonObject.optInt("msg_uid");
        if (0 == msguid) return null;
        MessageItem messageItem = new MessageItem();
        messageItem.setDataId(String.valueOf(msguid));
        String txt = jsonObject.optString("plain_text");
        if (!TextUtils.isEmpty(txt))
            messageItem.setText(txt);
        String full_txt = jsonObject.optString("full_text");
        if (!TextUtils.isEmpty(full_txt))
            messageItem.setFullText(full_txt);
        String html = jsonObject.optString("html");
        if (!TextUtils.isEmpty(html))
            messageItem.setHtml(html);
        boolean no_text = jsonObject.optBoolean("no_text");
        messageItem.setNoText(no_text);

        messageItem.setLatestMessageText(messageItem.getText());
        String date = jsonObject.optString("date");
        long ts = MainApplicationSingleton.getDateTimeMillisISO(date);
        messageItem.setLatestMessageTimestamp(ts);

        return messageItem;
    }

    public static MessageItem savesMsgDocTypeInDBFromJSON(
            JSONObject jsonObject, ContactItem user, Context context) throws
            JSONException, IOException {
        MessageItem messageItem = createsEmailMessageItemFromJSON(jsonObject, user);
        if (null == messageItem) {
            Log.e(TAG, "savesMsgDocTypeInDBFromJSON: FAIL - : " + jsonObject);
            return null;
        }
        Uri uri = savesMessageItem(messageItem, user, context);
        if (uri != null) {
            long id = ContentUris.parseId(uri);
            messageItem.set_id(id);
        }
        return messageItem;
/*
[
    {
        "_id": "6427d48d08db02e31100bddbaeb692f7",
        "_rev": "1-18a794e209bbf32de9ec97fe3565212a",
        "attachments": [],
        "txt": "Hi ",
        "to_uid": "USRMASTER_919100504678",
        "msg_type": "CHAT",
        "client_msg_ref": "3d6d3a61223c48afe38cc7f8132c5a6c_USRMASTER_919100504678_1467440159322.685059",
        "to_type": "USER",
        "doc_type": "MSG",
        "timestamp": 1467440159,
        "from_uid": "USRMASTER_919600037000",
        "from_name": "Jeff ",
        "msg_ref": "USRMASTER_919600037000:1467440159-8250",
        "doc_owner": "USRMASTER_919100504678",
        "chat_id": "USRMASTER_919600037000",
        "msg_direction": "IN",
        "chat_name": "Jeff "
    }
]
         */
    }

    public static int updateMessageInfoFromJSONByChatId(
            JSONObject jsonObject, MessageItem messageItem, Context context) throws
            JSONException, IOException {
        String receiptType = jsonObject.optString("receipt_type");
        if (TextUtils.isEmpty(receiptType)) return 0;
        String msgRef = jsonObject.optString("msg_ref");
        if (TextUtils.isEmpty(msgRef)) return 0;

        if ("DELIVERED".equals(receiptType)) {
            messageItem.setDelivered(1);
        } else if ("READ".equals(receiptType)) {
            messageItem.setRead(1);
        }
        messageItem.setMsgRef(msgRef);
        String id = jsonObject.optString("_id");
        if (!TextUtils.isEmpty(id))
            messageItem.setDataId(id);
        String rev = jsonObject.optString("_rev");
        if (!TextUtils.isEmpty(rev))
            messageItem.setDataRev(rev);
        String doc_owner = jsonObject.optString("doc_owner");
        if (!TextUtils.isEmpty(doc_owner))
            messageItem.setDocOwner(doc_owner);
        long timestamp = jsonObject.optLong("timestamp");
        if (timestamp > 0)
            messageItem.setTimestamp(timestamp);
        String datetime = jsonObject.optString("datetime");
        if (!TextUtils.isEmpty(datetime))
            messageItem.setDateTime(datetime);
        String user_uid = jsonObject.optString("user_uid");
        if (!TextUtils.isEmpty(user_uid))
            messageItem.setFromUid(user_uid);
        String user_name = jsonObject.optString("user_name");
        if (!TextUtils.isEmpty(user_name))
            messageItem.setDocSender(user_name);
        ContentValues values = new ContentValues();
        values.put(MessageItem.TAG,
                MainApplicationSingleton.Serializer.serialize(messageItem));
        return context.getContentResolver().update(CONTENT_URI, values, null, null);
/*
{
    "_id": "USRMASTER_919840348914_USRMASTER_919600037000_1467192156924_D_USRMASTER_919600037000",
    "_rev": "1-46fd8f525a4986ca6dec3776ba5f3e34",
    "doc_type": "MSG-INFO",
    "doc_owner": "USRMASTER_919840348914",
    "timestamp": 1467205708,
    "msg_ref": "USRMASTER_919840348914_USRMASTER_919600037000_1467192156924",
    "chat_id": "USRMASTER_919600037000",
    "msg_timestamp": 1467192158,
    "user_uid": "USRMASTER_919600037000",
    "receipt_type": "DELIVERED"
}
{
    ""_id"": ""efc2e801e79959c290a909d3f838803f"",
    ""_rev"": ""1-a220ccad8fc039ec2d251133d24484f9"",
    ""doc_type"": ""MSG-INFO"",
    ""doc_owner"": ""USRMASTER_919600037000"",
    ""timestamp"": 1462893832,
    ""msg_ref"": ""USRMASTER_919600037000: 1462893832-3885"",
    (Thesamemsg_reffoundinthemsgdoc)
    "chat_id":"USRMASTER_919600037000",
    "user_name":"Jeffrey",
    ""user_uid"": ""USRMASTER_919600037000"",
    ""receipt_type"": ""DELIVERED""(Thiscanbe""DELIVERED""or""READ"")
}
 */
    }

    public static boolean isMessageFoundByMsgRef(String msgRef, Context context) {
        boolean found = false;
        Cursor c = getMessageByMsgRefCursor(msgRef, context);
        if (c != null && c.moveToFirst()) {
            found = c.getCount() > 0;
        }
        if (c != null) c.close();
        return found;
    }

    public static boolean isMessageFoundByChatId(String chatId, Context context) {
        boolean found = false;
        Cursor c = getMessageByChatIdCursor(chatId, context);
        if (c != null && c.moveToFirst()) {
            found = c.getCount() > 0;
        }
        if (c != null) c.close();
        return found;
    }

    public static Cursor getMessageByChatIdCursor(String chatId, Context context) {
        return context.getContentResolver().query(CONTENT_URI,
                new String[]{MessageItemColumns.KEY_CHAT_ID},
                MessageItemColumns.KEY_CHAT_ID + " = ?",
                new String[]{chatId},
                null);
    }

    public static Cursor getMessageByMsgRefCursor(String msgRef, Context context) {
        return context.getContentResolver().query(CONTENT_URI,
                new String[]{MessageItemColumns.KEY_MSG_REF},
                MessageItemColumns.KEY_MSG_REF + " = ?",
                new String[]{msgRef},
                null);
    }

    public static void setChatAttachmentsFromJSONArray_(
            MessageItem messageItem, JSONArray jsonArray) throws
            JSONException {
        if (null != jsonArray && jsonArray.length() > 0) {
            Set<MessageItem> items = new HashSet<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                MessageItem attachmentItem = new MessageItem();
                String url = jsonArray.getString(i);
//                head request to the cloud for add on info
                JSONObject header = HttpUrlConnectionParser.headHTTP(url);
                attachmentItem.setDownloadURL(url);
                attachmentItem.setMsgAttachID(url);
                attachmentItem.setDataId(messageItem.getDataId());
                if (header != null) {
                    String contentType = header.optString("Content-Type");
                    if (contentType != null && !contentType.isEmpty()) {
                        String[] types = contentType.split("/");
                        attachmentItem.setType(types[0]);
                        attachmentItem.setSubType(types[1]);
                    }
                    int len = header.optInt("Content-Length");
                    attachmentItem.setSize(len);
                }
                items.add(attachmentItem);
            }
            messageItem.setAttachments(items);
            messageItem.setHasAttachments(items.size());
        }
    }

    public static int deleteMsgs(String[] item, Context context) throws IOException {
        return context.getApplicationContext().getContentResolver().delete(
//                triggers dataitem type with a string appended to the uri
                Uri.withAppendedPath(CONTENT_URI, "delete"), null, item);
    }

    public static int deleteMsgs_(String[] item, Context context) throws IOException {
        return context.getApplicationContext().getContentResolver().delete(JOIN_CONTENT_URI,
                null, item);
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
        messageItem.setMailbox(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_MAILBOX)));
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
        messageItem.setText(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_TEXT)));
        messageItem.setHtml(cursor.getString(cursor.getColumnIndex(
                MessageItemColumns.KEY_HTML)));
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
                MessageItemColumns.KEY_INTELLIBITZ_ID, messageItem.getIntellibitzId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DEVICE_CONTACTID, messageItem.getDeviceContactId());
        MainApplicationSingleton.fillIfNotNull(values,
                IntellibitzItemColumns.KEY_DATA_ID, messageItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CHAT_ID, messageItem.getChatId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_UID, messageItem.getToUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_CHAT_UID, messageItem.getToChatUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MSG_REF, messageItem.getMsgRef());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CLIENT_MSG_REF, messageItem.getChatMsgRef());
//        the chat name.. the subject
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_NAME, messageItem.getName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DATA_REV, messageItem.getDataRev());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TYPE, messageItem.getType());
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
                MessageItemColumns.KEY_TO_TYPE, messageItem.getToType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MESSAGE_TYPE, messageItem.getMessageType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_TYPE, messageItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MAILBOX, messageItem.getMailbox());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_OWNER, messageItem.getDocOwner());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_SENDER, messageItem.getDocSender());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_OWNER_EMAIL, messageItem.getDocOwnerEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_SENDER_EMAIL, messageItem.getDocSenderEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM_EMAIL, messageItem.getFromEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM_NAME, messageItem.getFromName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM_UID, messageItem.getFromUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_TYPE, messageItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_BASE_TYPE, messageItem.getBaseType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM, messageItem.getFrom());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO, messageItem.getTo());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CC, messageItem.getCc());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_BCC, messageItem.getBcc());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MESSAGE_DIRECTION, messageItem.getMessageDirection());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TEXT, messageItem.getText());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_HTML, messageItem.getHtml());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_SUBJECT, messageItem.getSubject());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_LATEST_MESSAGE, messageItem.getLatestMessageText());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_LATEST_MESSAGE_TS, messageItem.getLatestMessageTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_UID, messageItem.getToUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MESSAGE_ATTACH_ID, messageItem.getMessageAttachId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_HAS_ATTACHEMENTS, messageItem.getHasAttachments());
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

    public static ContentValues fillContentValuesFromMessageItem_(
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
                MessageItemColumns.KEY_INTELLIBITZ_ID, messageItem.getIntellibitzId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DEVICE_CONTACTID, messageItem.getDeviceContactId());
        MainApplicationSingleton.fillIfNotNull(values,
                IntellibitzItemColumns.KEY_DATA_ID, messageItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CHAT_ID, messageItem.getChatId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_UID, messageItem.getToUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_CHAT_UID, messageItem.getToChatUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MSG_REF, messageItem.getMsgRef());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CLIENT_MSG_REF, messageItem.getChatMsgRef());
//        the chat name.. the subject
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_NAME, messageItem.getName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DATA_REV, messageItem.getDataRev());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TYPE, messageItem.getType());
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
                MessageItemColumns.KEY_TO_TYPE, messageItem.getToType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MESSAGE_TYPE, messageItem.getMessageType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_TYPE, messageItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_OWNER, messageItem.getDocOwner());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_OWNER_EMAIL, messageItem.getDocOwnerEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_SENDER, messageItem.getDocSender());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_SENDER_EMAIL, messageItem.getDocSenderEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM_EMAIL, messageItem.getFromEmail());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM_NAME, messageItem.getFromName());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM_UID, messageItem.getFromUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_DOC_TYPE, messageItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_BASE_TYPE, messageItem.getBaseType());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_FROM, messageItem.getFrom());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO, messageItem.getTo());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_CC, messageItem.getCc());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_BCC, messageItem.getBcc());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MESSAGE_DIRECTION, messageItem.getMessageDirection());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TEXT, messageItem.getText());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_HTML, messageItem.getHtml());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_SUBJECT, messageItem.getSubject());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_LATEST_MESSAGE, messageItem.getLatestMessageText());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_LATEST_MESSAGE_TS, messageItem.getLatestMessageTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_TO_UID, messageItem.getToUid());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_MESSAGE_ATTACH_ID, messageItem.getMessageAttachId());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_HAS_ATTACHEMENTS, messageItem.hasAttachments());
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

    public static ContentValues fillContentValuesForMsgRefUpdate(MessageItem messageItem) {
        ContentValues contentValues = new ContentValues();
        if (messageItem.isRead()) {
            contentValues.put(
                    MessageItemColumns.KEY_IS_READ, true);
        }
        if (messageItem.isDelivered()) {
            contentValues.put(
                    MessageItemColumns.KEY_IS_DELIVERED, true);
        }
        return contentValues;
    }

    public static MessageItem fillMessageItemFromAllJoinCursor(
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
        messageThreadItem.setMailbox(cursor.getString(cursor.getColumnIndex("mtbox")));
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
            String gcid = cursor.getString(cursor.getColumnIndex("gcid"));
            if (gcid != null) {
//                ContactItem contactItem = messageThreadItem.getContactItem();
/*
                if (null == contactItem) {
                    contactItem = new ContactItem();
                    messageThreadItem.setContactItem(contactItem);
                }
                        " gc._id as gc_id, gc.id as gcid, gc.name as gcname," +
                        " gc.profile_pic as gcpic, gc.type as gctype, " +
*/
                contactThreadItem.set_id(cursor.getLong(cursor.getColumnIndex("gc_id")));
                contactThreadItem.setDataId(cursor.getString(cursor.getColumnIndex("gcid")));
                contactThreadItem.setName(cursor.getString(cursor.getColumnIndex("gcname")));
                contactThreadItem.setProfilePic(cursor.getString(cursor.getColumnIndex("gcpic")));
                contactThreadItem.setType(cursor.getString(cursor.getColumnIndex("gctype")));
                String akid = cursor.getString(cursor.getColumnIndex("akid"));
                ContactItem contactItem =
                        contactThreadItem.getContactItem(akid);
                if (null == contactItem) {
//            adds contact to message
//                    // TODO: 20-06-2016
//                    to fetch mobile
//                    MobileItem mobileItem = new MobileItem();
//                    mobileItem.setIntellibitzId(intellibitzId);
//                    IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(intellibitzId);
//                    contactItem = new ContactItem(intellibitzContactItem);
                    contactItem = new ContactItem();
                    contactItem.setDataId(akid);
//                    contactItem.setDataId(intellibitzId);
//                    contactItem.setIntellibitzId(intellibitzId);
                    contactThreadItem.addContact(contactItem);
                }
/*
                        " ak._id as ak_id, ak.id as akid, ak.name as akname, " +
                        " ak.profile_pic as akpic, ak.type as aktype, " +
                        " ak.intellibitz_id as akonid, ak.status as akstatus, " +
                        " e.name as ename, e.type_id as email, e.type as etype, e.type_id as etypeid, " +
                 */
                contactItem.set_id(cursor.getLong(cursor.getColumnIndex("ak_id")));
//                contactItem.setDataId(cursor.getString(cursor.getColumnIndex("akid")));
                contactItem.setName(cursor.getString(cursor.getColumnIndex("akname")));
                contactItem.setType(cursor.getString(cursor.getColumnIndex("aktype")));
                contactItem.setProfilePic(cursor.getString(cursor.getColumnIndex("akpic")));
                contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("akonid")));
                contactItem.setStatus(cursor.getString(cursor.getColumnIndex("akstatus")));
//                String email = cursor.getString(cursor.getColumnIndex("email"));
//                String ename = cursor.getString(cursor.getColumnIndex("ename"));
//                String etype = cursor.getString(cursor.getColumnIndex("etype"));
                String etypeid = cursor.getString(cursor.getColumnIndex("etypeid"));
                contactItem.setTypeId(etypeid);
/*
                if (email != null) {
//            adds email to message thread
//                        ContactItem contactItem = new ContactItem();
//                        contactItem.setDataId(email);
//                        contactItem.setIntellibitzId(email);
//                        contactItem.setName(ename);
//                        contactItem.setType(etype);
                    contactItem.addContact(contactItem);
                }
*/
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
//        Log.e(TAG, " MessageEmailThread: " + messageThreadItem);
    }

    public static MessageItem fillMessageItemFromAllJoinCursor_(
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
        messageThreadItem.setMailbox(cursor.getString(cursor.getColumnIndex("mtbox")));
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
            String gcid = cursor.getString(cursor.getColumnIndex("gcid"));
            if (gcid != null) {
//                ContactItem contactItem = messageThreadItem.getContactItem();
/*
                if (null == contactItem) {
                    contactItem = new ContactItem();
                    messageThreadItem.setContactItem(contactItem);
                }
                        " gc._id as gc_id, gc.id as gcid, gc.name as gcname," +
                        " gc.profile_pic as gcpic, gc.type as gctype, " +
*/
                contactThreadItem.set_id(cursor.getLong(cursor.getColumnIndex("gc_id")));
                contactThreadItem.setDataId(cursor.getString(cursor.getColumnIndex("gcid")));
                contactThreadItem.setName(cursor.getString(cursor.getColumnIndex("gcname")));
                contactThreadItem.setProfilePic(cursor.getString(cursor.getColumnIndex("gcpic")));
                contactThreadItem.setType(cursor.getString(cursor.getColumnIndex("gctype")));
                String akid = cursor.getString(cursor.getColumnIndex("akid"));
                ContactItem contactItem =
                        contactThreadItem.getContactItem(akid);
                if (null == contactItem) {
//            adds contact to message
//                    // TODO: 20-06-2016
//                    to fetch mobile
//                    MobileItem mobileItem = new MobileItem();
//                    mobileItem.setIntellibitzId(intellibitzId);
//                    IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(intellibitzId);
//                    contactItem = new ContactItem(intellibitzContactItem);
                    contactItem = new ContactItem();
                    contactItem.setDataId(akid);
//                    contactItem.setDataId(intellibitzId);
//                    contactItem.setIntellibitzId(intellibitzId);
                    contactThreadItem.addContact(contactItem);
                }
/*
                        " ak._id as ak_id, ak.id as akid, ak.name as akname, " +
                        " ak.profile_pic as akpic, ak.type as aktype, " +
                        " ak.intellibitz_id as akonid, ak.status as akstatus, " +
                        " e.name as ename, e.type_id as email, e.type as etype, e.type_id as etypeid, " +
                 */
                contactItem.set_id(cursor.getLong(cursor.getColumnIndex("ak_id")));
//                contactItem.setDataId(cursor.getString(cursor.getColumnIndex("akid")));
                contactItem.setName(cursor.getString(cursor.getColumnIndex("akname")));
                contactItem.setType(cursor.getString(cursor.getColumnIndex("aktype")));
                contactItem.setProfilePic(cursor.getString(cursor.getColumnIndex("akpic")));
                contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("akonid")));
                contactItem.setStatus(cursor.getString(cursor.getColumnIndex("akstatus")));
//                String email = cursor.getString(cursor.getColumnIndex("email"));
//                String ename = cursor.getString(cursor.getColumnIndex("ename"));
//                String etype = cursor.getString(cursor.getColumnIndex("etype"));
                String etypeid = cursor.getString(cursor.getColumnIndex("etypeid"));
                contactItem.setTypeId(etypeid);
/*
                if (email != null) {
//            adds email to message thread
//                        ContactItem contactItem = new ContactItem();
//                        contactItem.setDataId(email);
//                        contactItem.setIntellibitzId(email);
//                        contactItem.setName(ename);
//                        contactItem.setType(etype);
                    contactItem.addContact(contactItem);
                }
*/
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
//        Log.e(TAG, " MessageEmailThread: " + messageThreadItem);
    }


    public static Cursor getMessageJoin(
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
                        " mt." + MessageItemColumns.KEY_MAILBOX + " as mtbox, " +
                        " mt." + MessageItemColumns.KEY_DATA_REV + " as mtrev, " +
                        " mt." + MessageItemColumns.KEY_NAME + " as mtname, " +
                        " mt." + MessageItemColumns.KEY_MAILBOX + " as mtbox, " +
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
*/
                        " gc._id as gc_id, gc.id as gcid, gc.name as gcname," +
                        " gc.profile_pic as gcpic, gc.type as gctype, " +
                        " ak._id as ak_id, ak.id as akid, ak.name as akname, " +
                        " ak.profile_pic as akpic, ak.type as aktype, " +
                        " ak.intellibitz_id as akonid, ak.status as akstatus, " +
                        " e.name as ename, e.type_id as email, e.type as etype, e.type_id as etypeid, " +
                        " e.profile_pic as epic, e.cloud_pic as ecpic,  " +
                        "a." + MessageItemColumns.KEY_ID + " as a_id, " +
                        "a." + MessageItemColumns.KEY_DATA_ID + " as aid, " +
                        "a." + MessageItemColumns.KEY_MSGATTCH_ID + " as amid, " +
                        "a." + MessageItemColumns.KEY_PARTID + " as apid, " +
                        "a." + MessageItemColumns.KEY_NAME + " as aname, " +
                        "a." + MessageItemColumns.KEY_TYPE + " as atype, " +
                        "a." + MessageItemColumns.KEY_SUBTYPE + " as astype, " +
                        "a." + MessageItemColumns.KEY_SIZE + " as asize, " +
                        "a." + MessageItemColumns.KEY_ENCODING + " as aenc, " +
                        "a." + MessageItemColumns.KEY_DOWNLOAD_URL + " as aurl " +
                        "  FROM  " +
                        MessageEmailContentProvider.TABLE_MESSAGEEMAIL + " mt " +
                        "left outer join " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL_CONTACTS_JOIN +
                        " mtg on mt.[_id] = mtg.[msgthread_id] " +
                        "left outer join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS +
                        " gc on gc.[_id] = mtg.[" +
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
//                        "left outer join " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL_MESSAGE_JOIN +
//                        " mtm on mt.[_id] = mtm.[msg_thread_id] " +
//                        "left outer join " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL + " m on m.[_id] = mtm.[msg_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN +
                        " ma on mt.[_id] = ma.[msg_id] " +
                        "left outer join " + MsgEmailAttachmentContentProvider.TABLE_MSGEMAILATTACHMENT + " a on a.[_id] = ma.[attachment_id] ";
        if (selection != null && !selection.isEmpty()) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, selectionArgs);
    }

    public static Cursor getMessageShalGroupJoin(
            DatabaseHelper databaseHelper, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  mt." + IntellibitzItemColumns.KEY_ID + " as mt_id, " +
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
                        " mt." + IntellibitzItemColumns.KEY_DATA_ID + " as mtid, " +
                        " mt." + MessageItemColumns.KEY_TYPE + " as mttype, " +
                        " mt." + MessageItemColumns.KEY_TO_TYPE + " as mttotype, " +
                        " mt." + MessageItemColumns.KEY_DATA_REV + " as mtrev, " +
                        " mt." + MessageItemColumns.KEY_NAME + " as mtname, " +
                        " mt." + MessageItemColumns.KEY_MAILBOX + " as mtbox, " +
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
                        TABLE_MESSAGEEMAIL + " mt "
/*
                        +
                        "left outer join " + TABLE_GRPCONTACTS_AKCONTACTS_JOIN +
                        " gcak on gc.[_id] = gcak.[group_id] " +
                        "left outer join " + TABLE_INTELLIBITZCONTACT + " ak on ak.[_id] = gcak.[akcontact_id] " +
                        "left outer join " + TABLE_MESSAGETHREADS_EMAILS_JOIN +
                        " mte on mt.[_id] = mte.[msg_thread_id] " +
                        "left outer join " + TABLE_MSGTHREADEMAILS + " e on e.[_id] = mte.[email_id] " +
                        "left outer join " + TABLE_MESSAGESEMAIL_MESSAGE_JOIN +
                        " mtm on mt.[_id] = mtm.[msg_thread_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL + " m on m.[_id] = mtm.[msg_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN +
                        " ma on m.[_id] = ma.[msg_id] " +
                        "left outer join " + TABLE_MSGCHATATTACHMENT + " a on a.[_id] = ma.[attachment_id] "
*/
                        +
                        "left outer join " + TABLE_MESSAGEEMAIL_CONTACTS_JOIN +
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

    public static Cursor getMessageJoin_(
            DatabaseHelper databaseHelper, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  mt." + IntellibitzItemColumns.KEY_ID + " as mt_id, " +
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
                        " mt." + IntellibitzItemColumns.KEY_DATA_ID + " as mtid, " +
                        " mt." + MessageItemColumns.KEY_TYPE + " as mttype, " +
                        " mt." + MessageItemColumns.KEY_TO_TYPE + " as mttotype, " +
                        " mt." + MessageItemColumns.KEY_MAILBOX + " as mtbox, " +
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
                        " m." + IntellibitzItemColumns.KEY_ID + " as m_id," +
                        " m." + IntellibitzItemColumns.KEY_DATA_ID + " as mid," +
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
                        " ak.profile_pic as akpic, ak.type as aktype, " +
                        " ak.intellibitz_id as akonid, ak.status as akstatus, " +
                        " e.name as ename, e.type_id as email, e.type as etype, e.type_id as etypeid, " +
                        "a." + IntellibitzItemColumns.KEY_ID + " as a_id, " +
                        "a." + IntellibitzItemColumns.KEY_DATA_ID + " as aid, " +
                        "a." + MessageItemColumns.KEY_MSGATTCH_ID + " as amid, " +
                        "a." + MessageItemColumns.KEY_PARTID + " as apid, " +
                        "a." + MessageItemColumns.KEY_NAME + " as aname, " +
                        "a." + MessageItemColumns.KEY_TYPE + " as atype, " +
                        "a." + MessageItemColumns.KEY_SUBTYPE + " as astype, " +
                        "a." + MessageItemColumns.KEY_SIZE + " as asize, " +
                        "a." + MessageItemColumns.KEY_ENCODING + " as aenc, " +
                        "a." + MessageItemColumns.KEY_DOWNLOAD_URL + " as aurl " +
                        "  FROM  " +
                        TABLE_MESSAGEEMAIL + " mt " +
                        "left outer join " + TABLE_MESSAGESEMAIL_CONTACTS_JOIN +
                        " mtg on mt.[_id] = mtg.[msgthread_id] " +
                        "left outer join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS +
                        " gc on gc.[_id] = mtg.[" +
                        MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + "] " +
                        "left outer join " + TABLE_MESSAGEEMAIL_CONTACTS_JOIN +
                        " gcak on gc.[_id] = gcak.[" +
                        MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + "] " +
                        "left outer join " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT +
                        " ak on ak.[_id] = gcak.[" +
                        ContactsContactJoinColumns.KEY_CONTACT_ID + "] " +
                        "left outer join " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN +
                        " mte on ak.[_id] = mte.[contact_id] " +
                        "left outer join " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT +
                        " e on e.[_id] = mte.[intellibitzcontact_id] " +
                        "left outer join " + TABLE_MESSAGESEMAIL_MESSAGE_JOIN +
                        " mtm on mt.[_id] = mtm.[msg_thread_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL + " m on m.[_id] = mtm.[msg_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN +
                        " ma on m.[_id] = ma.[msg_id] " +
                        "left outer join " + MsgEmailAttachmentContentProvider.TABLE_MSGEMAILATTACHMENT + " a on a.[_id] = ma.[attachment_id] ";
        if (selection != null && !selection.isEmpty()) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, selectionArgs);
    }

    public static long createOrUpdateMessagesMessage(DatabaseHelper databaseHelper,
                                                     MessageItem messageItem, MessageItem messageThreadItem) {
// updates message thread.. the latest message info will be updated from message item
        ArrayList<MessageItem> messageItems = new ArrayList<>(1);
        messageItems.add(messageItem);
        long[] ids = createOrUpdateMessagesMessage(databaseHelper, messageItems, messageThreadItem);
        if (null == ids || 0 == ids.length) return 0;
        return ids[0];
    }

    public static long[] createOrUpdateMessagesMessage(DatabaseHelper databaseHelper,
                                                       Collection<MessageItem> messageItems, MessageItem messageThreadItem) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        ArrayList<MessageItem> messageThreadItems = new ArrayList<>(1);
        messageThreadItems.add(messageThreadItem);
        long[] ids = createOrUpdateMessages(databaseHelper, db, messageThreadItems);
        if (null == ids || 0 == ids.length) {
            Log.e(TAG, "Failed to insert row: " + messageThreadItem);
            throw new SQLException("Failed to insert row into " + messageThreadItem);
        }
        try {
            MessageItem[] array = messageItems.toArray(new MessageItem[0]);
            int i = 0;
            for (MessageItem messageItem : array) {
                long did = messageThreadItem.getDeviceContactId();
                if (did > 0) {
                    messageItem.setDeviceContactId(did);
                }
                long l = createOrUpdateMessagesMessage(
                        databaseHelper, db, messageItem, messageThreadItem.get_id());
                if (0 == l) {
                    Log.e(TAG, "Failed to insert row: " + messageItem);
                    throw new SQLException("Failed to insert row into " + messageItem);
                }
                ids[i++] = l;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ids;
    }

    public static long createOrUpdateMessagesMessage(
            DatabaseHelper databaseHelper, MessageItem messageItem, long id) {
        ArrayList<MessageItem> items = new ArrayList<>(1);
        items.add(messageItem);
        long[] ids = createOrUpdateMessageThreadMessages(databaseHelper, items, id);
        if (null == ids || 0 == ids.length) return 0;
        return ids[0];
    }

    public static long[] createOrUpdateMessageThreadMessages(
            DatabaseHelper databaseHelper, Collection<MessageItem> items, long id) {
        long[] ids = new long[items.size()];
        int i = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            MessageItem[] array = items.toArray(new MessageItem[0]);
            for (MessageItem item : array) {
                long l = createOrUpdateMessagesMessage(databaseHelper, db, item, id);
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

    public static long createOrUpdateMessagesMessage(
            DatabaseHelper databaseHelper, SQLiteDatabase db, MessageItem messageItem, long id) {
// a message can be identified by id, chat msg ref, chat id and to uid
        Cursor cursor = null;
        long _id = 0;
        String dataId = messageItem.getDataId();
        String chatId = messageItem.getChatId();
        String chatMsgRef = messageItem.getChatMsgRef();
//        QUERY
        if (!TextUtils.isEmpty(chatMsgRef)) {
            cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL,
                    new String[]{IntellibitzItemColumns.KEY_ID},
                    MessageItemColumns.KEY_CLIENT_MSG_REF + " = ?",
                    new String[]{chatMsgRef},
                    null);
        }
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//        new messages are identified by chat id.. to uid from email becomes the chat id for email messages
//        chat id from chat becomes the chat id for chat messages
//            existing messages will have a unique id from cloud, not equal to chat id
//            all other cases will point to a new message
            if (!TextUtils.isEmpty(dataId) &&
                    !TextUtils.isEmpty(chatId) &&
                    !dataId.equals(chatId)) {
                cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL,
                        new String[]{IntellibitzItemColumns.KEY_ID},
                        IntellibitzItemColumns.KEY_DATA_ID + " = ?",
                        new String[]{dataId},
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID));
                    cursor.close();
                }
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID));
            cursor.close();
        }
//        NOTE: for a group, chat id will be the same for all messages
/*
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//        new messages are identified by chat id.. to uid from email becomes the chat id for email messages
//        chat id from chat becomes the chat id for chat messages
            if (messageItem.getChatId() != null) {
                cursor = query(db, TABLE_MESSAGEEMAIL,
                        new String[]{MessageItemColumns.KEY_ID},
                        MessageItemColumns.KEY_CHAT_ID + " = ?",
                        new String[]{messageItem.getChatId()},
                        null);
            }
        }
*/
//        INSERT
        if (0 == _id) {
            _id = databaseHelper.insert(db, TABLE_MESSAGEEMAIL, null,
                    MessageEmailContentProvider.fillContentValuesFromMessageItem(
                            messageItem, new ContentValues()));
        } else {
//            UPDATE
//        message thread is the parent, message item child
            // updating row
            if (!TextUtils.isEmpty(chatMsgRef)) {
                databaseHelper.update(db, TABLE_MESSAGEEMAIL,
                        MessageEmailContentProvider.fillContentValuesFromMessageItem(
                                messageItem, new ContentValues()),
                        MessageItemColumns.KEY_CLIENT_MSG_REF + " = ?",
                        new String[]{String.valueOf(chatMsgRef)});
            } else if (!TextUtils.isEmpty(dataId)) {
                databaseHelper.update(db, TABLE_MESSAGEEMAIL,
                        MessageEmailContentProvider.fillContentValuesFromMessageItem(
                                messageItem, new ContentValues()),
                        IntellibitzItemColumns.KEY_DATA_ID + " = ?",
                        new String[]{String.valueOf(dataId)});
//        NOTE: for a group, chat id will be the same for all messages
/*
            } else if (messageItem.getChatId() != null) {
                update(db, TABLE_MESSAGEEMAIL,
                        MessageEmailContentProvider.fillContentValuesFromMessageItem(
                                messageItem, new ContentValues()),
                        MessageItemColumns.KEY_CHAT_ID + " = ?",
                        new String[]{String.valueOf(messageItem.getChatId())});
*/
            }
        }
//        QUERY AGAIN, FOR UPDATED ID
//            long _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID));
//            cursor.close();
//            messageItem.set_id(_id);
        if (0 == _id) {
            if (!TextUtils.isEmpty(chatMsgRef)) {
                cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL,
                        new String[]{IntellibitzItemColumns.KEY_ID},
                        MessageItemColumns.KEY_CLIENT_MSG_REF + " = ?",
                        new String[]{chatMsgRef},
                        null);
            }
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
                if (!TextUtils.isEmpty(dataId)) {
                    cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL,
                            new String[]{IntellibitzItemColumns.KEY_ID},
                            IntellibitzItemColumns.KEY_DATA_ID + " = ?",
                            new String[]{dataId},
                            null);
                    if (cursor != null && cursor.getCount() > 0) {
                        _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID));
                        cursor.close();
                    }
//        NOTE: for a group, chat id will be the same for all messages
/*
        } else if (messageItem.getChatId() != null) {
            cursor = query(db, TABLE_MESSAGEEMAIL,
                    new String[]{MessageItemColumns.KEY_ID},
                    MessageItemColumns.KEY_CHAT_ID + " = ?",
                    new String[]{messageItem.getChatId()},
                    null);
*/
                }
            } else {
                _id = cursor.getLong(cursor.getColumnIndex(IntellibitzItemColumns.KEY_ID));
                cursor.close();
            }
        }
//        JOIN
        if (0 == _id) {
            Log.e(TAG, "createOrUpdateNestMessageJoin: Message not found");
        } else {
            messageItem.set_id(_id);
            //        creates the join
            createOrUpdateMessagesMessageJoin(databaseHelper, db, messageItem.get_id(), id);
            Set<MessageItem> attachments = messageItem.getAttachments();
            if (null != attachments && !attachments.isEmpty()) {
                MsgEmailAttachmentContentProvider.createOrUpdateMessageAttachments(
                        databaseHelper, db, attachments, messageItem.get_id());
                int rows = updateMessageThreadHasAttachments(databaseHelper, id);
                if (0 == rows) {
                    Log.e(TAG, "createOrUpdateNestMessageJoin: MessageThread updated with Attachments: Rows Affected=" +
                            rows);
                }
            }
        }


//        check if group contact exists for message thread in db
//            joins if exists
//        if message thread exists, join
//        checks group contact, msg thread join first
        String ctidCol = MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID;
        cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL_CONTACTS_JOIN,
                new String[]{ctidCol},
                IntellibitzItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(messageItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            ctidCol = IntellibitzItemColumns.KEY_ID;
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ctidCol},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(dataId)}, null
            );
        }
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            ContactItem contactItem = messageItem.getContactItem();
            if (null == contactItem) {
//            throws sql execeptions here.. to break transaction
            } else {
//            saves or updates msgthreadcontact first
                id = createOrUpdateMessageContacts(
                        databaseHelper, db, contactItem);
                Set<ContactItem> contactItems = contactItem.getContactItems();
                if (contactItems != null && !contactItems.isEmpty())
//                only for group chat
//            // TODO: 16-05-2016
//            single chat can also hold a single group contact item with a single intellibitz contact
                    createOrUpdateMessageContactsJoin(
                            databaseHelper,
                            db, contactItem, messageItem.get_id());
//                the individual contact does not require updating, since its already linked by group
//            // TODO: 16-05-2016
//            link only the group
//                createOrUpdateMsgThreadGroupContactsJoin(contacts, item.get_id());
            }
        } else {
//                group found, join
            _id = cursor.getLong(cursor.getColumnIndex(ctidCol));
            cursor.close();
            createOrUpdateMessageContactsJoin(databaseHelper, db, messageItem.get_id(), _id);
        }
        //        finally update the message thread, if this has attachments
        return messageItem.get_id();
    }

    public static int updateMessageThreadHasAttachments(DatabaseHelper databaseHelper, long id) {
        ContentValues values = new ContentValues();
        values.put(MessageItemColumns.KEY_HAS_ATTACHEMENTS, 1);
        return databaseHelper.update(TABLE_MESSAGESEMAIL,
                values,
                IntellibitzItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public static int updateMessageThreadMessageAttachmentsURL(
            DatabaseHelper databaseHelper, MessageItem messageItem) {
        int rows = 0;
        Set<MessageItem> attachments = messageItem.getAttachments();
        if (null != attachments && !attachments.isEmpty()) {
            ContentValues values = new ContentValues();
            for (MessageItem attachmentItem : attachments) {
                values.clear();
                values.put(MessageItemColumns.KEY_DOWNLOAD_URL,
                        attachmentItem.getDownloadURL());
                rows += databaseHelper.update(MsgEmailAttachmentContentProvider.TABLE_MSGEMAILATTACHMENT,
                        values, IntellibitzItemColumns.KEY_ID + " = ?",
                        new String[]{String.valueOf(attachmentItem.get_id())});
            }
        }
        return rows;
    }

    public static long createOrUpdateMessages(
            DatabaseHelper databaseHelper, SQLiteDatabase db, MessageItem messageItem) {
        if (null == messageItem) return 0;
        ContentValues values = new ContentValues();
        fillContentValuesFromMessageItem(messageItem, values);
        Cursor cursor = databaseHelper.query(db, TABLE_MESSAGESEMAIL,
                new String[]{
                        IntellibitzItemColumns.KEY_ID,
                        MessageItemColumns.KEY_LATEST_MESSAGE,
                        MessageItemColumns.KEY_LATEST_MESSAGE_TS,
                        MessageItemColumns.KEY_DOC_SENDER},
                IntellibitzItemColumns.KEY_DATA_ID + " = ?",
                new String[]{messageItem.getDataId()}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = databaseHelper.insert(db, TABLE_MESSAGESEMAIL, null, values);
            messageItem.set_id(_id);
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    IntellibitzItemColumns.KEY_ID));
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
            // updating row
            databaseHelper.update(db, TABLE_MESSAGESEMAIL, values,
                    IntellibitzItemColumns.KEY_DATA_ID + " = ?",
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
        cursor = databaseHelper.query(db, TABLE_MESSAGESEMAIL_CONTACTS_JOIN,
                new String[]{ctidCol},
                MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ?",
                new String[]{String.valueOf(messageItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            ctidCol = IntellibitzItemColumns.KEY_ID;
//            throws sql execeptions here.. to break transaction
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ctidCol},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ?",
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
                long id = createOrUpdateMessagesContacts(databaseHelper, db, contactItem);
                Set<ContactItem> contactItems = contactItem.getContactItems();
                if (contactItems != null && !contactItems.isEmpty()) {
//                only for group chat
//            // TODO: 16-05-2016
//            single chat can also hold a single group contact item with a single intellibitz contact
                    createOrUpdateMessagesContactsJoin(
                            databaseHelper, db, contactItem, messageItem.get_id());
//                the individual contact does not require updating, since its already linked by group
//            // TODO: 16-05-2016
//            link only the group
//                createOrUpdateMsgThreadGroupContactsJoin(contacts, item.get_id());
//                // TODO: 11-07-2016
//                updates, the message with device contact id
                    if (0 == messageItem.getDeviceContactId()) {
                        ContactItem[] items = contactItems.toArray(new ContactItem[0]);
                        for (ContactItem deviceContact : items) {
                            if ("from".equalsIgnoreCase(deviceContact.getType())) {
                                long did = deviceContact.getDeviceContactId();
                                if (did > 0) {
                                    ContentValues values2 = new ContentValues();
                                    values2.put(MessageItemColumns.KEY_DEVICE_CONTACTID, did);
                                    databaseHelper.update(TABLE_MESSAGESEMAIL, values2,
                                            MessageItemColumns.KEY_ID + " = ? ",
                                            new String[]{String.valueOf(messageItem.get_id())});
                                    messageItem.setDeviceContactId(did);
                                    String profilePic = deviceContact.getProfilePic();
                                    if (!TextUtils.isEmpty(profilePic))
                                        messageItem.setProfilePic(profilePic);
                                    String cloudPic = deviceContact.getCloudPic();
                                    if (!TextUtils.isEmpty(cloudPic))
                                        messageItem.setCloudPic(cloudPic);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
//                group found, join
            long _id = cursor.getLong(cursor.getColumnIndex(ctidCol));
            cursor.close();
            createOrUpdateMessagesContactsJoin(databaseHelper, db, messageItem.get_id(), _id);
            if (0 == messageItem.getDeviceContactId()) {
                ContactItem contactItem = messageItem.getContactItem();
                if (contactItem != null) {
                    Set<ContactItem> contactItems = contactItem.getContactItems();
                    if (contactItems != null && !contactItems.isEmpty()) {
                        ContactItem[] items = contactItems.toArray(new ContactItem[0]);
                        for (ContactItem item : items) {
                            if ("from".equalsIgnoreCase(item.getType())) {
                                cursor = databaseHelper.query(IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT,
                                        new String[]{ContactItemColumns.KEY_DEVICE_CONTACTID},
                                        ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                                        new String[]{item.getIntellibitzId()}, null);
                                if (null == cursor || 0 == cursor.getCount()) {
                                    if (cursor != null) cursor.close();
                                } else {
                                    long did = cursor.getLong(cursor.getColumnIndex(
                                            ContactItemColumns.KEY_DEVICE_CONTACTID));
                                    cursor.close();
                                    if (did > 0) {
                                        ContentValues values2 = new ContentValues();
                                        values2.put(MessageItemColumns.KEY_DEVICE_CONTACTID, did);
                                        databaseHelper.update(db, TABLE_MESSAGESEMAIL, values2,
                                                MessageItemColumns.KEY_ID + " = ? ",
                                                new String[]{String.valueOf(messageItem.get_id())});
                                        messageItem.setDeviceContactId(did);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
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
//            all emails in clutter
            MsgsGrpClutterContentProvider.createOrUpdateMsgsGrpClutter(
                    databaseHelper, db, messageItem);
            MsgsGrpPeopleEmailsContentProvider.createOrUpdateMsgsGrpPeopleEmails(
                    databaseHelper, db, messageItem);
            if (0 == l) {
                Log.e(TAG, "Failed to insert row: " + messageItem);
                throw new SQLException("Failed to insert row into " + messageItem);
            }
            ids[i++] = l;
        }
        return ids;
    }

/*
    public int deleteMessage(SQLiteDatabase db, long _id) {
        return delete(db, MessageEmailContentProvider.TABLE_MESSAGEEMAIL,
                MessagesEmailContentProvider.MessageItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(_id)});
    }

    public int updateMessage(MessageItem item) {
        ContentValues values = new ContentValues();
//        message thread is the parent, message item child
        MessageEmailContentProvider.fillContentValuesFromMessageItem(item, values);
        // updating row
        return update(MessageEmailContentProvider.TABLE_MESSAGEEMAIL, values,
                MessagesEmailContentProvider.MessageItemColumns.KEY_DATA_ID + " = ?",
                new String[]{String.valueOf(item.getDataId())});
    }
*/

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

    public static MessageItem clonesMessagesFromMessage(
            MessageItem messageItem, ContactItem user) {
        try {
            MessageItem messageThreadItem = (MessageItem) messageItem.clone();
            messageThreadItem.setBaseType(MessageItem.THREAD);
//        CHAT ID is the thread ID for CHAT
//        for chat
//        TO UID is the thread ID for email
            String toUid = messageItem.getToUid();
            if (null == toUid) toUid = messageItem.getChatId();
            if (null == toUid) toUid = messageItem.getDataId();
//        for email
            messageThreadItem.setDataId(toUid);
/*
            if (!messageItem.isRead() &&
                    !user.getDataId().equals(messageItem.getFromUid())) {
//            // TODO: 22-05-2016
//            to increment from previous count.. get the latest count from DB
                messageItem.setUnreadCount(messageItem.getUnreadCount() + 1);
            }
//        latest message is written, only if message timestamp is newer than the previous
            long ts = messageItem.getTimestamp();
            long mts = messageThreadItem.getLatestMessageTimestamp();
            if (ts > mts) {
                messageThreadItem.setLatestMessageText(messageItem.getText());
                messageThreadItem.setLatestMessageTimestamp(ts);
                messageThreadItem.setText(messageItem.getText());
                messageThreadItem.setHtml(messageItem.getHtml());
                messageThreadItem.setNoText(messageItem.isNoText());
            }
*/
            return messageThreadItem;
        } catch (CloneNotSupportedException ignored) {
            Log.e(TAG, ignored.getMessage());
        }
        return null;
    }

    public static MessageItem clonesMessagesFromMessageGetRecentEmails(
            MessageItem messageItem, ContactItem user) {
        try {
            MessageItem messageThreadItem = (MessageItem) messageItem.clone();
            messageThreadItem.setBaseType(MessageItem.THREAD);
//        CHAT ID is the thread ID for CHAT
//        for chat
//        TO UID is the thread ID for email
            String toUid = messageItem.getToUid();
            if (null == toUid) toUid = messageItem.getChatId();
            if (null == toUid) toUid = messageItem.getDataId();
//        for email
            messageThreadItem.setDataId(toUid);
/*
            if (!messageItem.isRead() &&
                    !user.getDataId().equals(messageItem.getFromUid())) {
//            // TODO: 22-05-2016
//            to increment from previous count.. get the latest count from DB
                messageItem.setUnreadCount(messageItem.getUnreadCount() + 1);
            }
//        latest message is written, only if message timestamp is newer than the previous
            long ts = messageItem.getTimestamp();
            long mts = messageThreadItem.getLatestMessageTimestamp();
            if (ts > mts) {
                messageThreadItem.setLatestMessageText(messageItem.getText());
                messageThreadItem.setLatestMessageTimestamp(ts);
                messageThreadItem.setText(messageItem.getText());
                messageThreadItem.setHtml(messageItem.getHtml());
                messageThreadItem.setNoText(messageItem.isNoText());
            }
*/
            return messageThreadItem;
        } catch (CloneNotSupportedException ignored) {
            Log.e(TAG, ignored.getMessage());
        }
        return null;
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
        final String dataRev = messageItem.getDataRev();
        messageThreadItem.setDataRev(dataRev);
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
            messageThreadItem.setText(messageItem.getText());
            messageThreadItem.setHtml(messageItem.getHtml());
            messageThreadItem.setNoText(messageItem.isNoText());
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

    public static MessageItem fillMessageFromJSON(
            JSONObject jsonObject, MessageItem messageItem) throws
            JSONException {
/**
 * [{"_id":"439420713fa23c89fd388b95170059e6","_rev":"1-fa47e6927066c6524fa43f33077699fe",
 * "doc_type":"THREAD","doc_owner":"USRMASTER_919840348914","doc_owner_email":"muthu@intellibitz.com",
 * "timestamp":1457773254,"sub":"230","emails":[{"name":"Muthuselvam RS","email":"muthu@intellibitz.com",
 * "type":"from"},{"name":"Nishanth Kumar","email":"nishanth@intellibitz.com","type":"to"},
 * {"name":"Jeffrey Roshan","email":"jeff@intellibitz.com","type":"to"}]}]
 */
/*
[{"_id":"b2d6880d954afe7ee6663554cb3e9c6f","_rev":"1-75875289f5d8ba7553d6aeaa7b36b3a5",
"txt":"hiiij","to_uid":"aktprototype","msg_type":"CHAT","doc_type":"MSG","timestamp":1459592231,
"from_uid":"USRMASTER_919655653929","from_name":"Nishanth","to_type":"GROUP",
"doc_owner":"USRMASTER_919840348914","chat_id":"aktprototype","msg_direction":"IN","chat_name":"Demo Group"}]
         */
        messageItem.setType(jsonObject.optString("msg_type"));
        messageItem.setToType(jsonObject.optString("to_type"));
        messageItem.setDocType(jsonObject.getString("doc_type"));
        messageItem.setDataId(jsonObject.getString("_id"));
        messageItem.setDataRev(jsonObject.getString("_rev"));
        messageItem.setDocOwner(jsonObject.getString("doc_owner"));
        messageItem.setDocOwnerEmail(jsonObject.getString("doc_owner_email"));
        messageItem.setFromUid(jsonObject.optString("from_uid"));
        messageItem.setSubject(jsonObject.getString("sub"));
        messageItem.setTimestamp(jsonObject.getLong("timestamp"));
        messageItem.setDateTime(jsonObject.optString("datetime"));
        ContactItem contactThreadItem = messageItem.getContactItem();
        if (null == contactThreadItem) {
            contactThreadItem = new ContactItem();
            messageItem.setContactItem(contactThreadItem);
        }
        JSONArray emails = jsonObject.getJSONArray("emails");
        if (null != emails) {
            for (int i = 0; i < emails.length(); i++) {
                JSONObject js = emails.getJSONObject(i);
                String email = js.getString("email");
                String name = js.getString("name");
                String type = js.getString("type");
                ContactItem contactItem = new ContactItem();
                contactItem.setDataId(email);
                contactItem.setTypeId(email);
                contactItem.setIntellibitzId(email);
                contactItem.setName(name);
                contactItem.setType(type);
                ContactItem em = contactThreadItem.addContact(contactItem);
                if ("from".equalsIgnoreCase(em.getType())) {
                    messageItem.setFrom(em.getTypeId());
                    messageItem.setDocSender(em.getName());
                    messageItem.setDocSenderEmail(em.getTypeId());
                }
            }
/*
            String from = messageItem.getDocSenderEmail();
            EmailTags emailTags = new EmailTags(from, messageItem.getEmails()).invoke();
            messageItem.setTo(emailTags.getTo());
            messageItem.setCc(emailTags.getCc());
            messageItem.setBcc(emailTags.getBcc());
*/
            messageItem.invoke();

        }
        return messageItem;
    }

    public static int updateMessageInfo(
            String chatId, MessageItem messageItem, Context context) throws
            JSONException, IOException {
        ContentValues values = new ContentValues();
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_READ, messageItem.isRead());
        MainApplicationSingleton.fillIfNotNull(values,
                MessageItemColumns.KEY_IS_DELIVERED, messageItem.isDelivered());
        return context.getContentResolver().update(MessagesEmailContentProvider.CONTENT_URI, values,
                MessageItemColumns.KEY_CHAT_ID + " = ?", new String[]{chatId});
    }

    public static List<MessageItem> fillMessageItemsFromCursor(Cursor cursor) {
        List<MessageItem> messageItems = new ArrayList<>();
        if (null == cursor || 0 == cursor.getCount()) return messageItems;
        Log.e(MessagesEmailContentProvider.TAG, "fillMessageItemsFromCursor: count - " + cursor.getCount());
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
//            }
        } while (cursor.moveToNext());
        return messageItems;
    }

    public static long createOrUpdateMessagesContacts(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            ContactItem contactItem) {
//        checks if GroupContact already exists
/*
        Cursor cursor = query(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
//                projection
                new String[]{MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID},
//                selection
                MsgEmailContactsContentProvider.ContactItemColumns.KEY_DATA_ID + " = ? ",
//                selection args
                new String[]{contactItem.getDataId()},
                null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            cursor = query(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
                    new String[]{MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID},
                    MsgEmailContactsContentProvider.ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{contactItem.getIntellibitzId()},
                    null);
        }
//        does not exist.. insert
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = insert(db, MsgEmailContactsContentProvider.TABLE_CONTACTS, null,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()));
            contactItem.set_id(_id);
//            exists.. update
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID));
            contactItem.set_id(_id);
            cursor.close();
            update(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()),
                    MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(contactItem.get_id())});
        }
*/
/*
        HashSet<IntellibitzContactItem> contacts = item.getContactItems();
        if (contacts != null && !contacts.isEmpty())
            createOrUpdateGroupContactsAKContactsJoin(db, contacts, item.get_id());
*//*

//        createOrUpdateGroupContactAKContacts(db, item.getContactItems(), item.get_id());
        createOrUpdateContactsContacts(db,
                contactItem.getContactItems(), contactItem.get_id());
*/

        long _id = MsgEmailContactsContentProvider.createOrUpdateContacts(databaseHelper, db, contactItem);
        if (0 == _id) return 0;

//        if message thread exists, join
//        checks group contact, msg thread join first
        Cursor cursor = databaseHelper.query(db, TABLE_MESSAGESEMAIL_CONTACTS_JOIN,
                new String[]{MessagesContactsJoinColumns.KEY_MSGTHREAD_ID},
                IntellibitzItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(contactItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            cursor = databaseHelper.query(db, TABLE_MESSAGESEMAIL,
                    new String[]{IntellibitzItemColumns.KEY_ID},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(contactItem.getDataId())}, null
            );
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            } else {
//                message thread found, join
                _id = cursor.getLong(cursor.getColumnIndex(
                        IntellibitzItemColumns.KEY_ID));
                cursor.close();
                createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, contactItem.get_id());
            }
        } else {
//            message thread join exists, ok
            cursor.close();
        }
        return contactItem.get_id();
    }

    public static long createOrUpdateMessageContacts(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            ContactItem contactItem) {
//        checks if GroupContact already exists
/*
        Cursor cursor = query(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
//                projection
                new String[]{MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID},
//                selection
                MsgEmailContactsContentProvider.ContactItemColumns.KEY_DATA_ID + " = ? ",
//                selection args
                new String[]{contactItem.getDataId()},
                null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            cursor = query(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
                    new String[]{MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID},
                    MsgEmailContactsContentProvider.ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{contactItem.getIntellibitzId()},
                    null);
        }
//        does not exist.. insert
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = insert(db, MsgEmailContactsContentProvider.TABLE_CONTACTS, null,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()));
            contactItem.set_id(_id);
//            exists.. update
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID));
            contactItem.set_id(_id);
            cursor.close();
            update(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()),
                    MsgEmailContactsContentProvider.ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(contactItem.get_id())});
        }
*/
/*
        HashSet<IntellibitzContactItem> contacts = item.getContactItems();
        if (contacts != null && !contacts.isEmpty())
            createOrUpdateGroupContactsAKContactsJoin(db, contacts, item.get_id());
*//*

//        createOrUpdateGroupContactAKContacts(db, item.getContactItems(), item.get_id());
        createOrUpdateContactsContacts(db,
                contactItem.getContactItems(), contactItem.get_id());
*/

        long _id = MsgEmailContactsContentProvider.createOrUpdateContacts(databaseHelper, db, contactItem);
        if (0 == _id) return 0;

//        if message thread exists, join
//        checks group contact, msg thread join first
        Cursor cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL_CONTACTS_JOIN,
                new String[]{MessagesContactsJoinColumns.KEY_MSGTHREAD_ID},
                IntellibitzItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(contactItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL,
                    new String[]{IntellibitzItemColumns.KEY_ID},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(contactItem.getDataId())}, null
            );
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            } else {
//                message thread found, join
                _id = cursor.getLong(cursor.getColumnIndex(
                        IntellibitzItemColumns.KEY_ID));
                cursor.close();
                createOrUpdateMessageContactsJoin(databaseHelper, db, _id, contactItem.get_id());
            }
        } else {
//            message thread join exists, ok
            cursor.close();
        }
        return contactItem.get_id();
    }

    public static long[] createOrUpdateContacts(
            DatabaseHelper databaseHelper, Collection<ContactItem> items) {
        long[] ids = new long[items.size()];
        int i = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContactItem[] array = items.toArray(new ContactItem[0]);
            for (ContactItem item : array) {
                long l = createOrUpdateMessagesContacts(databaseHelper, db, item);
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

    public static long createOrUpdateMessageContactsJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            ContactItem item, long id) {
        Cursor cursor = getMessageContactThreadCursorJoin(databaseHelper, item, id);
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{IntellibitzItemColumns.KEY_ID},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{item.getDataId()}, null);
            if (null != cursor && 0 != cursor.getCount()) {
                _id = cursor.getLong(cursor.getColumnIndex(
                        IntellibitzItemColumns.KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(
                    IntellibitzItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
        }
        if (_id != 0) {
//        creates the join
            createOrUpdateMessageContactsJoin(databaseHelper, db, _id, id);
        }
        return item.get_id();
    }

    public static Cursor getMessageContactThreadCursorJoin(
            DatabaseHelper databaseHelper, ContactItem item, long id) {
        String[] args = new String[]{item.getDataId(), String.valueOf(id)};
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGEEMAIL + " nt " +
                " left join " + TABLE_MESSAGEEMAIL_CONTACTS_JOIN +
                " ntm on nt.[" + IntellibitzItemColumns.KEY_ID +
                "] = ntm.[" + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + "]  " +
                " left join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS + " mt on ntm.[" +
                MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID +
                "] = mt.[" + IntellibitzItemColumns.KEY_ID + "] " +
                " WHERE " + " mt." + IntellibitzItemColumns.KEY_ID + " = ? " +
                " AND nt." + MessageItemColumns.KEY_ID + " = ? ";
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, args);
    }

    public static long createOrUpdateMessagesContactsJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item, long id) {
        Cursor cursor = getMessageThreadContactThreadCursorJoin(databaseHelper, item, id);
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{IntellibitzItemColumns.KEY_ID},
                    IntellibitzItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{item.getDataId()}, null);
            if (null != cursor && 0 != cursor.getCount()) {
                _id = cursor.getLong(cursor.getColumnIndex(
                        IntellibitzItemColumns.KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(
                    IntellibitzItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
        }
        if (_id != 0) {
//        creates the join
            createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, id);
        }
        return item.get_id();
    }

    public static Cursor getMessageThreadContactThreadCursorJoin(
            DatabaseHelper databaseHelper, ContactItem item, long id) {
        String[] args = new String[]{item.getDataId(), String.valueOf(id)};
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGESEMAIL + " nt " +
                " left join " + TABLE_MESSAGESEMAIL_CONTACTS_JOIN +
                " ntm on nt.[" + IntellibitzItemColumns.KEY_ID +
                "] = ntm.[" + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + "]  " +
                " left join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS + " mt on ntm.[" +
                MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID +
                "] = mt.[" + IntellibitzItemColumns.KEY_ID + "] " +
                " WHERE " + " mt." + IntellibitzItemColumns.KEY_ID + " = ? " +
                " AND nt." + MessageItemColumns.KEY_ID + " = ? ";
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, args);
    }

    public static ArrayList<MessageItem> fillMessageThreadItemsFromCursor(Cursor cursor) {
        ArrayList<MessageItem> messageItems = new ArrayList<>();
        if (null == cursor || 0 == cursor.getCount()) return messageItems;
        HashSet<MessageItem> set = new HashSet<>();
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
//            messageItems.add(messageItem);
            set.add(messageItem);
//            }
        } while (cursor.moveToNext());
        messageItems.addAll(set);
        return messageItems;
    }

    public static MessageItem createsMessageThreadItemFromCursor(Cursor cursor) {
        MessageItem messageItem = new MessageItem();
        fillsMessageItemFromCursor(cursor, messageItem);
        return messageItem;
    }

    public static long getMessagesContactsJoin(DatabaseHelper databaseHelper, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(TABLE_MESSAGESEMAIL_CONTACTS_JOIN,
                new String[]{MessagesContactsJoinColumns.KEY_ID},
                MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ? and " +
                        MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(fk)}, null, null, null);
        if (c != null && c.getCount() > 0) {
            _id = c.getLong(c.getColumnIndex("_id"));
            c.close();
        }
        return _id;
    }

    public static long createOrUpdateMessagesContactsJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = getMessagesContactsJoin(databaseHelper, id, fk);
        ContentValues values = new ContentValues();
        values.put(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID, id);
        values.put(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID, fk);
        values.put(MessagesContactsJoinColumns.KEY_TIMESTAMP,
                MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db, TABLE_MESSAGESEMAIL_CONTACTS_JOIN, null, values);
        } else {
            _id = databaseHelper.update(db, TABLE_MESSAGESEMAIL_CONTACTS_JOIN, values,
                    MessagesContactsJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long createOrUpdateMessageContactsJoin(DatabaseHelper databaseHelper,
                                                         SQLiteDatabase db, long id, long fk) {
        long _id = getMessageContactThreadJoin(databaseHelper, id, fk);
        ContentValues values = new ContentValues();
        values.put(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID, id);
        values.put(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID, fk);
        values.put(MessagesContactsJoinColumns.KEY_TIMESTAMP,
                MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db, TABLE_MESSAGEEMAIL_CONTACTS_JOIN, null, values);
        } else {
            _id = databaseHelper.update(db, TABLE_MESSAGEEMAIL_CONTACTS_JOIN, values,
                    MessagesContactsJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long getMessageContactThreadJoin(DatabaseHelper databaseHelper, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(TABLE_MESSAGEEMAIL_CONTACTS_JOIN,
                new String[]{IntellibitzItemColumns.KEY_ID},
                MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + " = ? and " +
                        MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(fk)}, null, null, null);
        if (c != null && c.getCount() > 0) {
            _id = c.getLong(c.getColumnIndex("_id"));
            c.close();
        }
        return _id;
    }

    public static long createOrUpdateMessagesMessageJoin(DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = getMessageThreadMessageJoin(databaseHelper, id, fk);
        ContentValues values = new ContentValues();
        values.put(MessagesMessageJoinColumns.KEY_MESSAGE_ID, id);
        values.put(MessagesMessageJoinColumns.KEY_MSG_THREAD_ID, fk);
        values.put(MessagesMessageJoinColumns.KEY_TIMESTAMP, MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db, TABLE_MESSAGESEMAIL_MESSAGE_JOIN, null, values);
        } else {
            databaseHelper.update(db, TABLE_MESSAGESEMAIL_MESSAGE_JOIN, values,
                    MessagesMessageJoinColumns.KEY_ID + " = ?", new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long getMessageThreadMessageJoin(DatabaseHelper databaseHelper, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(TABLE_MESSAGESEMAIL_MESSAGE_JOIN, new String[]{IntellibitzItemColumns.KEY_ID},
                MessagesMessageJoinColumns.KEY_MESSAGE_ID + " = ? and " +
                        MessagesMessageJoinColumns.KEY_MSG_THREAD_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(fk)}, null, null, null);
        if (c != null && c.getCount() > 0) {
            _id = c.getLong(c.getColumnIndex("_id"));
            c.close();
        }
        return _id;
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

    /**
     * Creating Message
     */
    public static long[] deleteMessages(DatabaseHelper databaseHelper, String[] items) {
        long[] ids = new long[items.length];
        int i = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String id : items) {
                long l = deleteMessage(databaseHelper, db, id);
                if (0 == l) {
                    Log.e(TAG, "Failed to insert row: " + id);
                    return new long[]{0};
                }
                ids[i++] = l;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ids;
    }

    public static long deleteMessage(DatabaseHelper databaseHelper, SQLiteDatabase db, String id) {
        long _id = 0;
//        fetches message db id
        Cursor cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL,
                new String[]{MessageItemColumns.KEY_ID},
                MessageItemColumns.KEY_DATA_ID + " = ?",
                new String[]{id},
                null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            return 0;
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(MessageItemColumns.KEY_ID));
            cursor.close();
        }
//        fetches messages attachments db id
        cursor = databaseHelper.query(db, TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN,
                new String[]{MessageAttachmentJoinColumns.KEY_ATTACHMENT_ID},
                MessageItemColumns.KEY_ID + " = ?", new String[]{id}, null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
        } else {
            int i = 0;
            String[] aids = new String[cursor.getCount()];
            do {
                aids[i++] = String.valueOf(cursor.getLong(cursor.getColumnIndex(
                        MessagesMessageJoinColumns.KEY_MSG_THREAD_ID)));
            } while (cursor.moveToNext());
            cursor.close();
            String w = "";
            for (i = 0; i < aids.length; i++) {
                w += ((i > 0) ? ",?" : "?");
            }
            databaseHelper.delete(db, MsgEmailAttachmentContentProvider.TABLE_MSGEMAILATTACHMENT,
                    MessageItemColumns.KEY_ID + " IN( " + w + " )",
                    aids);
        }
//            deleteMessage(db, _id);
        databaseHelper.delete(db, TABLE_MESSAGEEMAIL,
                MessageItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(_id)});
//        // TODO: 3/8/16
//        to delete from messages also (thread)
/*
        databaseHelper.delete(db, TABLE_MESSAGESCHAT,
                MessageItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(_id)});
*/

//        deletes the message thread join
/*
        cursor = query(db, TABLE_MESSAGES_MESSAGE_JOIN,
                new String[]{MessagesMessageJoinColumns.KEY_MSG_THREAD_ID},
                MessageItemColumns.KEY_ID + " = ?",
                new String[]{id},
                null);
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(MessagesMessageJoinColumns.KEY_MSG_THREAD_ID));
            cursor.close();
            deleteMessageThread(db, _id);
        }
        createOrUpdateMessagesMessageJoin(messageItem.get_id(), id);
*/
        //        finally update the message thread, if this has attachments
        return _id;
    }

    public static Cursor getMessagesThreadJoin(
            DatabaseHelper databaseHelper, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  mt." + IntellibitzItemColumns.KEY_ID + " as mt_id, " +
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
                        " mt." + IntellibitzItemColumns.KEY_DATA_ID + " as mtid, " +
                        " mt." + MessageItemColumns.KEY_TYPE + " as mttype, " +
                        " mt." + MessageItemColumns.KEY_TO_TYPE + " as mttotype, " +
                        " mt." + MessageItemColumns.KEY_MAILBOX + " as mtbox, " +
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
                        " m." + IntellibitzItemColumns.KEY_ID + " as m_id," +
                        " m." + IntellibitzItemColumns.KEY_DATA_ID + " as mid," +
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
                        " ak.profile_pic as akpic, ak.type as aktype, " +
                        " ak.intellibitz_id as akonid, ak.status as akstatus, " +
                        " e.name as ename, e.type_id as email, e.type as etype, e.type_id as etypeid, " +
                        "a." + IntellibitzItemColumns.KEY_ID + " as a_id, " +
                        "a." + IntellibitzItemColumns.KEY_DATA_ID + " as aid, " +
                        "a." + MessageItemColumns.KEY_MSGATTCH_ID + " as amid, " +
                        "a." + MessageItemColumns.KEY_PARTID + " as apid, " +
                        "a." + MessageItemColumns.KEY_NAME + " as aname, " +
                        "a." + MessageItemColumns.KEY_TYPE + " as atype, " +
                        "a." + MessageItemColumns.KEY_SUBTYPE + " as astype, " +
                        "a." + MessageItemColumns.KEY_SIZE + " as asize, " +
                        "a." + MessageItemColumns.KEY_ENCODING + " as aenc, " +
                        "a." + MessageItemColumns.KEY_DOWNLOAD_URL + " as aurl " +
                        "  FROM  " +
                        TABLE_MESSAGESEMAIL + " mt " +
                        "left outer join " + TABLE_MESSAGESEMAIL_CONTACTS_JOIN +
                        " mtg on mt.[_id] = mtg.[msgthread_id] " +
                        "left outer join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS +
                        " gc on gc.[_id] = mtg.[" +
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
                        "left outer join " + TABLE_MESSAGESEMAIL_MESSAGE_JOIN +
                        " mtm on mt.[_id] = mtm.[msg_thread_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL + " m on m.[_id] = mtm.[msg_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN +
                        " ma on m.[_id] = ma.[msg_id] " +
                        "left outer join " + MsgEmailAttachmentContentProvider.TABLE_MSGEMAILATTACHMENT + " a on a.[_id] = ma.[attachment_id] ";
        if (selection != null && !selection.isEmpty()) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, selectionArgs);
    }

    public static Cursor getMessagesThreadShalGroupJoin(
            DatabaseHelper databaseHelper, String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  mt." + IntellibitzItemColumns.KEY_ID + " as mt_id, " +
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
                        " mt." + IntellibitzItemColumns.KEY_DATA_ID + " as mtid, " +
                        " mt." + MessageItemColumns.KEY_TYPE + " as mttype, " +
                        " mt." + MessageItemColumns.KEY_TO_TYPE + " as mttotype, " +
                        " mt." + MessageItemColumns.KEY_MAILBOX + " as mtbox, " +
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
                        TABLE_MESSAGESEMAIL + " mt "
/*
                        +
                        "left outer join " + TABLE_GRPCONTACTS_AKCONTACTS_JOIN +
                        " gcak on gc.[_id] = gcak.[group_id] " +
                        "left outer join " + TABLE_INTELLIBITZCONTACT + " ak on ak.[_id] = gcak.[akcontact_id] " +
                        "left outer join " + TABLE_MESSAGETHREADS_EMAILS_JOIN +
                        " mte on mt.[_id] = mte.[msg_thread_id] " +
                        "left outer join " + TABLE_MSGTHREADEMAILS + " e on e.[_id] = mte.[email_id] " +
                        "left outer join " + TABLE_MESSAGESEMAIL_MESSAGE_JOIN +
                        " mtm on mt.[_id] = mtm.[msg_thread_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL + " m on m.[_id] = mtm.[msg_id] " +
                        "left outer join " + TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN +
                        " ma on m.[_id] = ma.[msg_id] " +
                        "left outer join " + TABLE_MSGCHATATTACHMENT + " a on a.[_id] = ma.[attachment_id] "
*/
                        +
                        "left outer join " + TABLE_MESSAGESEMAIL_CONTACTS_JOIN +
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

    public static JSONObject toEmailJson_(MessageItem messageItem, String uid, String token,
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

    public static MessageItem queryMessageEmailThreadFullJoin(
            MessageItem messageItem, Context context) {
        String id = messageItem.getDataId();
        if (null == id) {
            Log.e(MessagesEmailContentProvider.TAG, "Query fails, ID null: " + messageItem);
            return messageItem;
        }
        Uri uri = Uri.withAppendedPath(MessagesEmailContentProvider.JOIN_CONTENT_URI, Uri.encode(id));
        String selection =
                " ( ak." + MessageItemColumns.KEY_IS_GROUP +
                        " = 0 OR " +
                        " ak." + MessageItemColumns.KEY_IS_GROUP +
                        " IS NULL ) ";
        selection += " AND mt." + IntellibitzItemColumns.KEY_DATA_ID + " = ? ";
        String[] selectionArgs = new String[]{id};
        String sortOrder = "mt." +
                MessageItemColumns.KEY_TIMESTAMP + " ASC";
        Cursor cursor = context.getApplicationContext().getContentResolver().query(
                uri, null, selection, selectionArgs, sortOrder);
        if (cursor != null) {
            try {
                fillMessageItemFromAllJoinCursor(
                        messageItem, cursor);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            cursor.close();
        }
        return messageItem;
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
            case MESSAGEEMAIL_DIR_TYPE:
                return MESSAGEEMAIL_DIR_MIME_TYPE;
            case MESSAGEEMAIL_JOIN_DIR_TYPE:
                return MESSAGEEMAIL_JOIN_DIR_MIME_TYPE;
            case MESSAGEEMAIL_ITEM_TYPE:
                return MESSAGEEMAIL_ITEM_MIME_TYPE;
            case MESSAGEEMAIL_DATA_ITEM_TYPE:
                return MESSAGEEMAIL_DATA_ITEM_MIME_TYPE;
            case MESSAGEEMAIL_JOIN_ITEM_TYPE:
                return MESSAGEEMAIL_JOIN_ITEM_MIME_TYPE;
            case MESSAGEEMAIL_JOIN_DATA_ITEM_TYPE:
                return MESSAGEEMAIL_JOIN_DATA_ITEM_MIME_TYPE;
            case MESSAGEEMAIL_RAW_DIR_TYPE:
                return MESSAGEEMAIL_RAW_DIR_MIME_TYPE;
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case REFRESH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

/*
    public static long createOrUpdateMessagesContactsJoin(
            DatabaseHelper databaseHelper, ContactItem item, long id) {
        Cursor cursor = getMessageThreadContactThreadCursorJoin(databaseHelper, item, id);
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(MsgEmailContactsContentProvider.TABLE_CONTACTS,
                    new String[]{KEY_ID},
                    KEY_DATA_ID + " = ? ",
                    new String[]{item.getDataId()}, null);
            if (null != cursor && 0 != cursor.getCount()) {
                _id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            cursor.close();
            item.set_id(_id);
        }
        if (_id != 0) {
//        creates the join
            createOrUpdateMessagesContactsJoin(databaseHelper,_id, id);
        }
        return item.get_id();
    }
*/

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        Cursor cursor = null;
        switch (URI_MATCHER.match(uri)) {
            case MESSAGEEMAIL_DIR_TYPE:
            case MESSAGEEMAIL_ITEM_TYPE:
            case MESSAGEEMAIL_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(TABLE_MESSAGEEMAIL,
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
            case MESSAGEEMAIL_JOIN_DIR_TYPE:
                try {
                    cursor = getMessageShalGroupJoin(databaseHelper,
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
            case MESSAGEEMAIL_JOIN_ITEM_TYPE:
            case MESSAGEEMAIL_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = getMessageJoin(databaseHelper,
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
            case MESSAGEEMAIL_RAW_DIR_TYPE:
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
        switch (URI_MATCHER.match(uri)) {
            case MESSAGEEMAIL_DIR_TYPE:
            case MESSAGEEMAIL_ITEM_TYPE:
            case MESSAGEEMAIL_DATA_ITEM_TYPE:
                byte[] vals = values.getAsByteArray(BaseItem.THREAD);
                byte[] vals2 = values.getAsByteArray(MessageItem.TAG);
                try {
                    MessageItem messageThreadItem = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals);
                    MessageItem messageItem = (MessageItem)
                            MainApplicationSingleton.Serializer.deserialize(vals2);
                    long id = createOrUpdateMessagesMessage(databaseHelper,
                            messageItem, messageThreadItem);
                    Uri insertUri = ContentUris.withAppendedId(uri, id);
/*
                    Uri messageThreadUri = ContentUris.withAppendedId(
                            MessagesEmailContentProvider.CONTENT_URI, messageThreadItem.get_id());
*/
                    Context context = getContext();
                    if (null != context) {
//                        context.getContentResolver().notifyChange(messageThreadUri, null);
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
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (URI_MATCHER.match(uri)) {
            case MESSAGEEMAIL_DIR_TYPE:
            case MESSAGEEMAIL_ITEM_TYPE:
            case MESSAGEEMAIL_DATA_ITEM_TYPE:
                byte[] vals2 = values.getAsByteArray(MessageItem.TAG);
                try {
                    int rows = 0;
                    MessageItem messageItem = (MessageItem) MainApplicationSingleton.Serializer.deserialize(vals2);
                    Set<MessageItem> attachments = messageItem.getAttachments();
                    if (null == attachments || attachments.isEmpty()) {
                        rows = databaseHelper.update(
                                TABLE_MESSAGEEMAIL,
                                fillContentValuesForMsgRefUpdate(messageItem),
                                MessageItemColumns.KEY_MSG_REF + " = ?",
                                new String[]{messageItem.getMsgRef()});
                    } else {
//                        default.. updates the messageemail by message ref
//                        to handle more scenarios
//                        // TODO: 12-05-2016
                        rows = updateMessageThreadMessageAttachmentsURL(databaseHelper,
                                messageItem);
                    }
                    Context context = getContext();
                    if (null != context) {
//                        attachments in a message of a message thread updates
//                        not required to let the view know.. right now
//                        // TODO: 06-04-2016
//                        revisit.. notification to be turned on for view to update
//                        context.getContentResolver().notifyChange(uri, null);
                    }
                    return rows;
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case MESSAGEEMAIL_RAW_DIR_TYPE:
                try {
                    int row = databaseHelper.update(TABLE_MESSAGEEMAIL,
                            values, where, whereArgs);
                    Uri updUri = ContentUris.withAppendedId(
                            MessageEmailContentProvider.CONTENT_URI, row);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(updUri, null);
                    }
                    return row;
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
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        int id = 0;
        Uri delUri = null;
        switch (URI_MATCHER.match(uri)) {
            case MESSAGEEMAIL_DIR_TYPE:
            case MESSAGEEMAIL_ITEM_TYPE:
                try {
                    id = databaseHelper.delete(
                            TABLE_MESSAGEEMAIL, where, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MessageEmailContentProvider.CONTENT_URI, id);
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
            case MESSAGEEMAIL_DATA_ITEM_TYPE:
                try {
                    long[] rows = deleteMessages(databaseHelper, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MessagesEmailContentProvider.CONTENT_URI, rows[0]);
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
            case MESSAGEEMAIL_JOIN_DIR_TYPE:
            case MESSAGEEMAIL_JOIN_ITEM_TYPE:
            case MESSAGEEMAIL_JOIN_DATA_ITEM_TYPE:
                try {
                    id = databaseHelper.delete(
                            TABLE_MESSAGEEMAIL, where, whereArgs);
                    delUri = ContentUris.withAppendedId(
                            MessageEmailContentProvider.CONTENT_URI, id);
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

/*
    public interface MessageItemColumns extends MessageItemColumns {
    }

    //    join table
    public interface MessagesMessageJoinColumns extends IntellibitzItemColumns {
        String KEY_MESSAGE_ID = "msg_id";
        String KEY_MSG_THREAD_ID = "msg_thread_id";
    }

    public interface MessagesContactsJoinColumns extends IntellibitzItemColumns {
        String KEY_MSGTHREAD_ID = "msgthread_id";
        String KEY_CONTACTTHREAD_ID = "contactthread_id";
    }

    //    join table
    public interface MessageAttachmentJoinColumns extends IntellibitzItemColumns {
        String KEY_ATTACHMENT_ID = "attachment_id";
        String KEY_MESSAGE_ID = "msg_id";
    }

    public interface MessageItemColumns extends IntellibitzItemColumns {
        String KEY_TO_TYPE = "to_type";
        String KEY_CHAT_ID = "chat_id";
        String KEY_TO_CHAT_UID = "to_chat_uid";
        String KEY_FROM_UID = "from_uid";
        String KEY_TO_UID = "to_uid";
        String KEY_MSG_REF = "msg_ref";
        String KEY_CLIENT_MSG_REF = "client_msg_ref";
        String KEY_MESSAGE_DIRECTION = "msg_direction";
        String KEY_MESSAGE_TYPE = "msg_type";
        String KEY_DOC_OWNER_EMAIL = "doc_owner_email";
        String KEY_DOC_SENDER = "doc_sender";
        String KEY_DOC_SENDER_EMAIL = "doc_sender_email";
        String KEY_FROM_EMAIL = "from_email";
        String KEY_FROM_NAME = "from_name";
        String KEY_MESSAGE_ATTACH_ID = "msg_attach_id";
        String KEY_FROM = "email_from";
        String KEY_TO = "email_to";
        String KEY_CC = "email_cc";
        String KEY_BCC = "email_bcc";
        String KEY_TEXT = "text";
        String KEY_HTML = "html";
        String KEY_PENDING_DOCS = "pending_docs";
        String KEY_HAS_ATTACHEMENTS = "has_attachments";
        String KEY_IS_READ = "is_read";
        String KEY_IS_DELIVERED = "is_delivered";
        String KEY_IS_FLAGGED = "is_flagged";
        String KEY_SUBJECT = "subject";
        String KEY_UNREAD_COUNT = "unread_count";
        String KEY_LATEST_MESSAGE = "last_msg";
        //        last messageemail timestamp
        String KEY_LATEST_MESSAGE_TS = "last_msg_ts";
        String KEY_IS_DEFAULT_FOLDER = "default_folder";
        String KEY_FOLDER_CODE = "folder_code";
    }
*/

/*
    public static long createOrUpdateContacts(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem contactItem) {
//        checks if GroupContact already exists
        Cursor cursor = databaseHelper.query(db, TABLE_CONTACTS,
//                projection
                new String[]{KEY_ID},
//                selection
                KEY_DATA_ID + " = ? ",
//                selection args
                new String[]{contactItem.getDataId()},
                null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
                    new String[]{KEY_ID},
                    MsgEmailContactsContentProvider.ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{contactItem.getIntellibitzId()},
                    null);
        }
//        does not exist.. insert
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = databaseHelper.insert(db, MsgEmailContactsContentProvider.TABLE_CONTACTS, null,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()));
            contactItem.set_id(_id);
//            exists.. update
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    KEY_ID));
            contactItem.set_id(_id);
            cursor.close();
            databaseHelper.update(db, MsgEmailContactsContentProvider.TABLE_CONTACTS,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()),
                    KEY_ID + " = ?",
                    new String[]{String.valueOf(contactItem.get_id())});
        }
*/
/*
        HashSet<IntellibitzContactItem> contacts = item.getContactItems();
        if (contacts != null && !contacts.isEmpty())
            createOrUpdateGroupContactsAKContactsJoin(db, contacts, item.get_id());
*//*

//        createOrUpdateGroupContactAKContacts(db, item.getContactItems(), item.get_id());
        createOrUpdateContactsContacts(databaseHelper, db,
                contactItem.getContactItems(), contactItem.get_id());
        return contactItem.get_id();
    }
*/

}
