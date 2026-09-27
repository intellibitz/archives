package intellibitz.intellidroid.db;

/**
 */
public interface IntellibitzItemColumns {
    String KEY_ID = "_id";
    String KEY_DATA_ID = "id";
    String KEY_THREAD_ID = "thread_id";
    String KEY_THREAD_IDREF = "thread_idref";
    String KEY_THREAD_IDPARTS = "thread_idparts";
    String KEY_GROUP_ID = "group_id";
    String KEY_GROUP_IDREF = "group_idref";
    String KEY_TYPE_ID = "type_id";
    String KEY_DATA_REV = "_rev";
    String KEY_INTELLIBITZ_ID = "intellibitz_id";
    String KEY_IS_GROUP = "is_group";
    String KEY_IS_EMAIL = "is_email";
    String KEY_IS_ANONYMOUS = "is_anonymous";
    String KEY_IS_DEVICE = "is_device";
    String KEY_IS_CLOUD = "is_cloud";
    String KEY_FIRST_NAME = "first_name";
    String KEY_LAST_NAME = "last_name";
    String KEY_DISPLAY_NAME = "display_name";
    String KEY_DEVICE_CONTACTID = "contact_id";
    //        ex: msg
    String KEY_DOC_TYPE = "doc_type";
    //        ex: thread, nest, draft
    String KEY_BASE_TYPE = "base_type";
    //        ex: chat, email
    String KEY_TYPE = "type";
    String KEY_NAME = "name";
    String KEY_PIC = "profile_pic";
    String KEY_CLOUD_PIC = "cloud_pic";

    String KEY_COMPANY_ID = "company_id";
    String KEY_COMPANY_NAME = "company_name";

    String KEY_EMAIL = "email";
    String KEY_EMAIL_CODE = "email_code";
    String KEY_ACTIVE = "active";
    String KEY_DEVICE_ID = "device_id";
    String KEY_DEVICE_NAME = "device_name";
    String KEY_DEVICE_REF = "device_ref";
    String KEY_DOC_OWNER = "doc_owner";
    String KEY_STATUS = "status";

    String KEY_REF_ID = "ref_id";

    String KEY_TIMESTAMP = "timestamp";
    String KEY_DATETIME = "datetime";

    String TABLE_INFOS_SCHEMA = "( "
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_DATA_ID + " TEXT,"
            + KEY_REF_ID + " TEXT,"
            + KEY_DATA_REV + " TEXT,"
            + KEY_TYPE + " TEXT,"
            + KEY_DEVICE_REF + " TEXT,"
            + KEY_DOC_OWNER + " TEXT,"
            + KEY_DOC_TYPE + " TEXT,"
            + KEY_NAME + " TEXT,"
            + KEY_PIC + " TEXT,"
            + KEY_TIMESTAMP + " LONG,"
            + KEY_DATETIME + " DATETIME" + ")";
    //    // TODO: 12/9/16
//    to move table infos to info content provider
    //    emails used in messages and message threads.. not the same as user email accounts
    String TABLE_INFOS = "infos";
    String CREATE_TABLE_INFOS = "CREATE TABLE "
            + TABLE_INFOS + TABLE_INFOS_SCHEMA;
}
