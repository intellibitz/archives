package intellibitz.intellidroid.db;

/**
 */
public interface MessageItemColumns extends
        IntellibitzItemColumns {
    String KEY_IS_LOCKED = "is_locked";
    String KEY_TO_TYPE = "to_type";
    String KEY_CHAT_ID = "chat_id";
    String KEY_TO_CHAT_UID = "to_chat_uid";
    String KEY_FROM_UID = "from_uid";
    String KEY_TO_UID = "to_uid";
    String KEY_MSG_REF = "msg_ref";
    String KEY_CLIENT_MSG_REF = "client_msg_ref";
    String KEY_MESSAGE_DIRECTION = "msg_direction";
    String KEY_MESSAGE_TYPE = "msg_type";
    String KEY_MAILBOX = "mailbox";
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
    String KEY_MSGATTCH_ID = "msg_attch_id";
    String KEY_PARTID = "part_id";
    String KEY_SUBTYPE = "subtype";
    String KEY_ENCODING = "encoding";
    String KEY_LANGUAGE = "language";
    String KEY_SIZE = "size";
    String KEY_MD5 = "md5";
    String KEY_DOWNLOAD_URL = "cloud_url";
    String KEY_DESCRIPTION = "description";
    String KEY_IS_READ = "is_read";
    String KEY_IS_DELIVERED = "is_delivered";
    String KEY_IS_FLAGGED = "is_flagged";
    String KEY_SUBJECT = "subject";
    String KEY_UNREAD_COUNT = "unread_count";
    String KEY_LATEST_MESSAGE = "last_msg";
    //        last messagechatgroup timestamp
    String KEY_LATEST_MESSAGE_TS = "last_msg_ts";
    String KEY_IS_DEFAULT_FOLDER = "default_folder";
    String KEY_FOLDER_CODE = "folder_code";

    String MESSAGECHAT_SCHEMA = "( "

            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_THREAD_ID + " TEXT,"
            + KEY_THREAD_IDREF + " TEXT,"
            + KEY_THREAD_IDPARTS + " TEXT,"
            + KEY_GROUP_ID + " TEXT,"
            + KEY_GROUP_IDREF + " TEXT,"
            + KEY_DEVICE_CONTACTID + " INTEGER,"
            + KEY_IS_GROUP + " INTEGER,"
            + KEY_IS_EMAIL + " INTEGER,"
            + KEY_IS_DEVICE + " INTEGER,"
            + KEY_IS_CLOUD + " INTEGER,"
            + KEY_IS_ANONYMOUS + " INTEGER,"
            + KEY_INTELLIBITZ_ID + " TEXT,"
            + KEY_PIC + " TEXT,"
            + KEY_CLOUD_PIC + " TEXT,"
            + KEY_DATA_ID + " TEXT,"
            + KEY_DATA_REV + " TEXT,"
            + KEY_CHAT_ID + " TEXT,"
            + KEY_TO_CHAT_UID + " TEXT,"
            + KEY_TO_UID + " TEXT,"
            + KEY_MSG_REF + " TEXT,"
            + KEY_CLIENT_MSG_REF + " TEXT,"
            + KEY_NAME + " TEXT,"
            + KEY_FIRST_NAME + " TEXT,"
            + KEY_LAST_NAME + " TEXT,"
            + KEY_DISPLAY_NAME + " TEXT,"
            + KEY_TYPE + " TEXT,"
            + KEY_TO_TYPE + " TEXT,"
            + KEY_MESSAGE_TYPE + " TEXT,"
            + KEY_DOC_OWNER + " TEXT,"
            + KEY_DOC_TYPE + " TEXT,"
            + KEY_BASE_TYPE + " TEXT,"
            + KEY_MAILBOX + " TEXT,"
            + KEY_DOC_OWNER_EMAIL + " TEXT,"
            + KEY_DOC_SENDER + " TEXT,"
            + KEY_DOC_SENDER_EMAIL + " TEXT,"
            + KEY_FROM_EMAIL + " TEXT,"
            + KEY_FROM_NAME + " TEXT,"
            + KEY_FROM_UID + " TEXT,"
            + KEY_SUBJECT + " TEXT,"
            + KEY_FROM + " TEXT,"
            + KEY_TO + " TEXT,"
            + KEY_CC + " TEXT,"
            + KEY_BCC + " TEXT,"
            + KEY_MESSAGE_DIRECTION + " TEXT,"
            + KEY_TEXT + " TEXT,"
            + KEY_HTML + " TEXT,"
            + KEY_MESSAGE_ATTACH_ID + " TEXT,"
            + KEY_PENDING_DOCS + " INTEGER,"
            + KEY_HAS_ATTACHEMENTS + " INTEGER,"
            + KEY_DESCRIPTION + " TEXT,"
            + KEY_SUBTYPE + " TEXT,"
            + KEY_MSGATTCH_ID + " TEXT,"
            + KEY_PARTID + " TEXT,"
            + KEY_ENCODING + " TEXT,"
            + KEY_LANGUAGE + " TEXT,"
            + KEY_MD5 + " TEXT,"
            + KEY_DOWNLOAD_URL + " TEXT,"
            + KEY_SIZE + " INTEGER,"
            + KEY_UNREAD_COUNT + " INTEGER,"
            + KEY_LATEST_MESSAGE + " TEXT,"
            + KEY_LATEST_MESSAGE_TS + " LONG,"
            + KEY_IS_READ + " INTEGER,"
            + KEY_IS_DELIVERED + " INTEGER,"
            + KEY_IS_FLAGGED + " INTEGER,"
            + KEY_FOLDER_CODE + " TEXT,"
            + KEY_IS_DEFAULT_FOLDER + " INTEGER,"
            + KEY_IS_LOCKED + " INTEGER,"
            + KEY_TIMESTAMP + " LONG,"
            + KEY_DATETIME + " DATETIME" + ")";

}
