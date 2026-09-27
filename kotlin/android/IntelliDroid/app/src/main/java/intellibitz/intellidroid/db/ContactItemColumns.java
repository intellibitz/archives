package intellibitz.intellidroid.db;

/**
 */
public interface ContactItemColumns extends
        IntellibitzItemColumns {
    String KEY_VERSION = "version";
    String KEY_PHONES = "phones";
    String KEY_EMAILS = "emails";
    String KEY_IS_INTELLIBITZ = "intellibitz_contact";
    String KEY_ISWORK = "work_contact";
    String KEY_DEVICE = "device";
    String KEY_FIRST_NAME = "first_name";
    String KEY_LAST_NAME = "last_name";
    String KEY_MOBILE = "mobile";
    String KEY_SIGNUP_EMAIL = "email";
    String KEY_PWD = "password";
    String KEY_TOKEN = "token";
    String KEY_OTP = "otp";
    String KEY_GCM_TOKEN = "gcm_token";
    String KEY_GCM_TOKEN_SENDTO_CLOUD = "gcm_token_enabled";

    String TABLE_CONTACTS_SCHEMA = "( "
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_DATA_ID + " TEXT,"
            + KEY_DATA_REV + " TEXT,"
            + KEY_DEVICE + " TEXT,"
            + KEY_DEVICE_ID + " TEXT,"
            + KEY_FIRST_NAME + " TEXT,"
            + KEY_LAST_NAME + " TEXT,"
            + KEY_DEVICE_NAME + " TEXT,"
            + KEY_DEVICE_REF + " TEXT,"
            + KEY_NAME + " TEXT,"
            + KEY_PIC + " TEXT,"
            + KEY_CLOUD_PIC + " TEXT,"
            + KEY_TOKEN + " TEXT,"
            + KEY_GCM_TOKEN + " TEXT,"
            + KEY_OTP + " TEXT,"
            + KEY_MOBILE + " TEXT,"
            + KEY_SIGNUP_EMAIL + " TEXT,"
            + KEY_EMAIL_CODE + " TEXT,"
            + KEY_PWD + " TEXT,"
            + KEY_COMPANY_ID + " TEXT,"
            + KEY_COMPANY_NAME + " TEXT,"
            + KEY_STATUS + " TEXT,"
            + KEY_GCM_TOKEN_SENDTO_CLOUD + " INTEGER,"
            + KEY_DEVICE_CONTACTID + " INTEGER,"
            + KEY_GROUP_ID + " TEXT,"
            + KEY_TYPE_ID + " TEXT,"
            + KEY_INTELLIBITZ_ID + " TEXT,"
            + KEY_VERSION + " INTEGER,"
            + KEY_BASE_TYPE + " TEXT,"
            + KEY_TYPE + " TEXT,"
            + KEY_DOC_OWNER + " TEXT,"
            + KEY_DOC_TYPE + " TEXT,"
            + KEY_DISPLAY_NAME + " TEXT,"
            + KEY_PHONES + " TEXT,"
            + KEY_EMAILS + " TEXT,"
            + KEY_IS_INTELLIBITZ + " INTEGER,"
            + KEY_IS_GROUP + " INTEGER,"
            + KEY_IS_EMAIL + " INTEGER,"
            + KEY_IS_ANONYMOUS + " INTEGER,"
            + KEY_IS_DEVICE + " INTEGER,"
            + KEY_IS_CLOUD + " INTEGER,"
            + KEY_ISWORK + " INTEGER,"
            + KEY_TIMESTAMP + " LONG,"
            + KEY_DATETIME + " DATETIME" + ")";
}
