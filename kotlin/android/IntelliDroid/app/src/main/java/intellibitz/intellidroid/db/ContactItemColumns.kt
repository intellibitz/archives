package intellibitz.intellidroid.db

interface ContactItemColumns : IntellibitzItemColumns {
    companion object {
        const val KEY_ID = IntellibitzItemColumns.KEY_ID
        const val KEY_DATA_ID = IntellibitzItemColumns.KEY_DATA_ID
        const val KEY_THREAD_ID = IntellibitzItemColumns.KEY_THREAD_ID
        const val KEY_THREAD_IDREF = IntellibitzItemColumns.KEY_THREAD_IDREF
        const val KEY_THREAD_IDPARTS = IntellibitzItemColumns.KEY_THREAD_IDPARTS
        const val KEY_GROUP_ID = IntellibitzItemColumns.KEY_GROUP_ID
        const val KEY_GROUP_IDREF = IntellibitzItemColumns.KEY_GROUP_IDREF
        const val KEY_TYPE_ID = IntellibitzItemColumns.KEY_TYPE_ID
        const val KEY_DATA_REV = IntellibitzItemColumns.KEY_DATA_REV
        const val KEY_INTELLIBITZ_ID = IntellibitzItemColumns.KEY_INTELLIBITZ_ID
        const val KEY_IS_GROUP = IntellibitzItemColumns.KEY_IS_GROUP
        const val KEY_IS_EMAIL = IntellibitzItemColumns.KEY_IS_EMAIL
        const val KEY_IS_ANONYMOUS = IntellibitzItemColumns.KEY_IS_ANONYMOUS
        const val KEY_IS_DEVICE = IntellibitzItemColumns.KEY_IS_DEVICE
        const val KEY_IS_CLOUD = IntellibitzItemColumns.KEY_IS_CLOUD
        const val KEY_FIRST_NAME = IntellibitzItemColumns.KEY_FIRST_NAME
        const val KEY_LAST_NAME = IntellibitzItemColumns.KEY_LAST_NAME
        const val KEY_DISPLAY_NAME = IntellibitzItemColumns.KEY_DISPLAY_NAME
        const val KEY_DEVICE_CONTACTID = IntellibitzItemColumns.KEY_DEVICE_CONTACTID
        const val KEY_DOC_TYPE = IntellibitzItemColumns.KEY_DOC_TYPE
        const val KEY_BASE_TYPE = IntellibitzItemColumns.KEY_BASE_TYPE
        const val KEY_TYPE = IntellibitzItemColumns.KEY_TYPE
        const val KEY_NAME = IntellibitzItemColumns.KEY_NAME
        const val KEY_PIC = IntellibitzItemColumns.KEY_PIC
        const val KEY_CLOUD_PIC = IntellibitzItemColumns.KEY_CLOUD_PIC
        const val KEY_COMPANY_ID = IntellibitzItemColumns.KEY_COMPANY_ID
        const val KEY_COMPANY_NAME = IntellibitzItemColumns.KEY_COMPANY_NAME
        const val KEY_EMAIL = IntellibitzItemColumns.KEY_EMAIL
        const val KEY_EMAIL_CODE = IntellibitzItemColumns.KEY_EMAIL_CODE
        const val KEY_ACTIVE = IntellibitzItemColumns.KEY_ACTIVE
        const val KEY_DEVICE_ID = IntellibitzItemColumns.KEY_DEVICE_ID
        const val KEY_DEVICE_NAME = IntellibitzItemColumns.KEY_DEVICE_NAME
        const val KEY_DEVICE_REF = IntellibitzItemColumns.KEY_DEVICE_REF
        const val KEY_DOC_OWNER = IntellibitzItemColumns.KEY_DOC_OWNER
        const val KEY_STATUS = IntellibitzItemColumns.KEY_STATUS
        const val KEY_REF_ID = IntellibitzItemColumns.KEY_REF_ID
        const val KEY_TIMESTAMP = IntellibitzItemColumns.KEY_TIMESTAMP
        const val KEY_DATETIME = IntellibitzItemColumns.KEY_DATETIME

        const val KEY_VERSION = "version"
        const val KEY_PHONES = "phones"
        const val KEY_EMAILS = "emails"
        const val KEY_IS_INTELLIBITZ = "intellibitz_contact"
        const val KEY_ISWORK = "work_contact"
        const val KEY_DEVICE = "device"
        const val KEY_MOBILE = "mobile"
        const val KEY_SIGNUP_EMAIL = "email"
        const val KEY_PWD = "password"
        const val KEY_TOKEN = "token"
        const val KEY_OTP = "otp"
        const val KEY_GCM_TOKEN = "gcm_token"
        const val KEY_GCM_TOKEN_SENDTO_CLOUD = "gcm_token_enabled"

        const val TABLE_CONTACTS_SCHEMA = ("( "
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
                + KEY_DATETIME + " DATETIME" + ")")
    }
}
