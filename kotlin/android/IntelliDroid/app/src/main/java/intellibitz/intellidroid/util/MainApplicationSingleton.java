package intellibitz.intellidroid.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.bean.BaseBean;

import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.bean.BaseBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.LruCache;
import androidx.core.view.ViewCompat;

/*
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
*/

/**
 */
public class MainApplicationSingleton {
    public static final String TAG = "MainSingleton";

    public static final Class<MainActivity> MAIN_ACTIVITY_CLASS = MainActivity.class;
    public static final String INTELLIBITZ = "Intellibitz";
    public static final String INTELLIBITZ_STORAGE_DIR = INTELLIBITZ;
    /*
        public static final String SOCKET_HOST =
                "http://dev-socket.intellibitz.com:3010/";
        public static final String API_HOST =
                "http://52.76.65.10:3010/";
        public static final String SOCKET_HOST =
                "http://52.76.65.10:3010/";
        public static final String API_HOST =
                "http://ec2-52-76-65-10.ap-southeast-1.compute.amazonaws.com:3010/";
        public static final String SOCKET_HOST =
                "http://ec2-52-77-67-190.ap-southeast-1.compute.amazonaws.com:3010/";
        public static final String DB_HOST =
                "http://52.77.67.190:5984/";
    */
/*
    public static final String API_HOST =
            "http://uat-api.intellibitz.com:3010/";
    public static final String SOCKET_HOST =
            "http://uat-socket.intellibitz.com:3010/";
*/
/*
        public static final String API_HOST =
                "http://qa-api.intellibitz.com:3010/";
        public static final String SOCKET_HOST =
                "http://qa-socket.intellibitz.com:3010/";
*/
    public static final String API_HOST =
            "http://dev-api.intellibitz.com:3010/";
    public static final String SOCKET_HOST =
            "http://dev-socket.intellibitz.com:3010/";
    /*
        public static final String API_HOST =
                "http://api.intellibitz.com:3010/";
        public static final String SOCKET_HOST =
                "http://socket.intellibitz.com:3010/";
    */
    public static final String API_USER = "muthu";
    public static final String USER_PARAM = "user";
    public static final String API_KEY = "86e3be35b73888f648c337e46c6dad35";
    public static final String KEY_PARAM = "key";

    public static final String AWS_ACCESS_KEY = "INSERT_VALID_KEY_HERE";
    public static final String AWS_SECRET_KEY = "INSERT_VALID_KEY_HERE";
    public static final String AWS_S3_BUCKET = "intellibitz-uploads";
    public static final String UID_USER_LOGGED_IN_PARAM = "uid_user_logged_in";
    public static final String DEVICE_ID = "android";
    public static final String URL_PARAM = "url";
    public static final String DUMMY_EMAIL = "info@intellibitz.net";
    public static final String EMAIL_ACCOUNT_PARAM = "email_account";
    public static final String EMAIL_UIDS_PARAM = "email_uids";
    public static final String SKIP_PARAM = "skip";
    public static final String BYEMAIL_PARAM = "by_email";
    public static final String GCM_TOKEN_PARAM = "push_token";
    public static final String DEVICE_PARAM = "device";
    public static final String DEVICE_ID_PARAM = "device_id";
    public static final String DEVICE_NAME_PARAM = "device_name";
    public static final String DEVICE_REF_PARAM = "device_ref";
    public static final String MOBILE_PARAM = "mobile";
    public static final String COUNTRY_PARAM = "country";
    public static final String COUNTRY_CODE_PARAM = "country_code";
    public static final String EMAIL_PARAM = "email";
    public static final String COMPANY_ID_PARAM = "company_id";

    public static final String POST_ID_PARAM = "post_id";
    public static final String TXT_PARAM = "txt";
    public static final String LAST_TIMESTAMP_PARAM = "last_timestamp";

    public static final String SCHEDULE_ID_PARAM = "schedule_id";
    public static final String SCHEDULE_TIME_PARAM = "schedule_time";
    public static final String SCHEDULE_MSG_PARAM = "schedule_msg";

    public static final String COMPANY_NAME_PARAM = "company_name";
    public static final String INVITED_COMPANY_ID_PARAM = "invited_company_id";
    public static final String INVITE_EMAILS_PARAM = "invite_emails";
    public static final String LOCKPWD_PARAM = "lock_pwd";
    public static final String STARTUPLOCK_PARAM = "startup_lock";
    public static final String INAPPLOCK_PARAM = "inapp_lock";
    public static final String PWD_PARAM = "pwd";
    public static final String OTP_PARAM = "otp";
    public static final String UID_PARAM = "uid";
    public static final String ID_PARAM = "_id";
    public static final String TOKEN_PARAM = "token";
    public static final String CHAT_ID_PARAM = "chat_id";
    public static final String GROUP_ID_PARAM = "group_id";
    public static final String DRAFT_ID_PARAM = "draft_id";
    public static final String USER_UID_PARAM = "user_uid";
    public static final String MODIFY_TYPE_PARAM = "modify_type";
    public static final String DOC_ID = "doc_id";
    public static final String STACK_NAME_PARAM = "stack_name";
    public static final String NAME_PARAM = "name";
    public static final String NEWPWD_PARAM = "newpwd";
    public static final String DRAFT_OBJ_PARAM = "draft_obj";
    public static final String STATUS_PARAM = "status";
    public static final String PROFILE_PIC_PARAM = "profile_pic";
    public static final String ATTACH_FILE_PARAM = "attach_file";
    public static final String CODE_PARAM = "code";
    public static final String RESP_CODE = "resp_code";
    public static final String MSGS_PARAM = "msgs";
    public static final String INTENT_ACTION_FETCH_CHAT_ATTACHMENT_CLOUD = "ACTION_FETCH_CHAT_ATTACHMENT_CLOUD";
    public static final String INTENT_ACTION_FETCH_CHATGROUP_ATTACHMENT_CLOUD = "ACTION_FETCH_CHATGROUP_ATTACHMENT_CLOUD";
    public static final String INTENT_ACTION_FETCH_EMAIL_ATTACHMENT_CLOUD = "ACTION_FETCH_EMAIL_ATTACHMENT_CLOUD";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // PERMISSIONS
    public static final int PERM_READ_PHONE_STATE = 11;
    public static final int PERM_READ_CONTACTS = 21;
    public static final int PERM_READ_EXTERNAL_STORAGE = 31;
    public static final int PERM_WRITE_EXTERNAL_STORAGE = 41;
    public static final int PERM_CAMERA = 51;
    public static final int PERM_STORAGE = 61;
    //    PENDING INTENT REQUEST CODES
    public static final int ACT_ACCT_LANDING_RQ_CODE = 111;
    public static final int ACT_CHK_EMAIL_AVBL_RQ_CODE = 101;
    public static final int ACT_VERIFY_CODE_RQ_CODE = 102;
    public static final int ACT_PROFILE_SIGNUP_RQ_CODE = 103;
    public static final int ACT_COMPANY_CREATE_RQ_CODE = 104;
    public static final int ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE = 105;
    public static final int ACT_COMPANY_GETINVITES_RQ_CODE = 106;
    public static final int ACT_COMPANY_LIST_RQ_CODE = 107;
    public static final int ACT_COMPANY_INVITE_USERS_RQ_CODE = 108;
    public static final int ACT_CONTACT_DETAIL_RQ_CODE = 109;
    public static final int ACT_SCHEDULE_RQ_CODE = 110;

    public static final int ACTIVITY_LOGIN_RQ_CODE = 11;
    public static final int ACTIVITY_OTP_RQ_CODE = 12;
    public static final int ACTIVITY_PROFILEINFO_RQ_CODE = 13;
    public static final int ACTIVITY_ADDEMAIL_RQ_CODE = 14;
    public static final int ACTIVITY_ADDEMAILS_RQ_CODE = 15;
    public static final int ACTIVITY_NEWEMAILACCOUNT_RQ_CODE = 16;
    public static final int ACTIVITY_MSGSGRPDRAFT_RQ_CODE = 17;
    public static final int ACTIVITY_MSGSGRPNEST_RQ_CODE = 18;
    public static final int ACTIVITY_CONTACTSELECT_RQ_CODE = 19;
    public static final int ACTIVITY_BROADCASTCONTACTS_RQ_CODE = 20;
    public static final int ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE = 21;
    public static final int ACTIVITY_MESSAGECHATGROUP_RQ_CODE = 22;
    public static final int ACTIVITY_MSGSGRPNESTDETAIL_RQ_CODE = 23;
    public static final int ACTIVITY_PEOPLEDETAIL_RQ_CODE = 24;
    public static final int ACTIVITY_CLUTTEREMAILS_RQ_CODE = 25;
    public static final int ACTIVITY_CLUTTEREMAIL_RQ_CODE = 26;
    public static final int ACTIVITY_EMAILACCOUNT_RQ_CODE = 27;
    public static final int ACTIVITY_PROILE_RQ_CODE = 28;
    public static final int PI_MSG_LISTENER_RQ_CODE = 29;
    public static final int PI_CHATTYMAIL_RQ_CODE = 30;
    public static final int PI_EMAIL_RQ_CODE = 31;
    public static final int PI_CHAT_RQ_CODE = 32;
    public static final int PI_RCVDOC_RQ_CODE = 33;
    public static final int ACTIVITY_MYACCOUNT_RQ_CODE = 34;
    public static final int ACTIVITY_EMAILACCOUNTDETAIL_RQ_CODE = 35;
    public static final int ACTIVITY_COMPOSEEMAIL_RQ_CODE = 36;
    public static final int ACTIVITY_COMPOSEFEED_RQ_CODE = 37;

    //    LOADER ID CONSTANTS
    public static final int LOGIN_FRAGMENT_LOADERID = 701;
    public static final int MSGSGRPNESTMESSAGES_LOADERID = 581;
    public static final int CLUTTEREMAIL_LOADERID = 951;
    public static final int CLUTTEREMAILS_LOADERID = 961;
    public static final int MSGSGRPCLUTTER_LOADERID = 971;
    public static final int MSGSGRPPEOPLE_LOADERID = 981;
    public static final int MSGSGRPPEOPLECHATS_LOADERID = 991;
    public static final int MSGSGRPDRAFT_LOADERID = 917;
    public static final int MESSAGES_LOADERID = 991;
    public static final int CONTACTSELECT_LOADERID = 801;
    public static final int MESSAGECHATGROUP_LOADERID = 821;
    public static final int BROADCAST_CONTACTS_LOADERID = 919;
    public static final int CHATEMAIL_CONTENT_FRAGMENT_LOADERID = 101;
    public static final int CHATEMAIL_MESSAGE_FRAGMENT_LOADERID = 201;
    public static final int EMAIL_CONTENT_FRAGMENT_LOADERID = 301;
    public static final int EMAIL_MESSAGE_FRAGMENT_LOADERID = 401;
    public static final int CHAT_CONTENT_FRAGMENT_LOADERID = 501;
    public static final int CHAT_MESSAGE_FRAGMENT_LOADERID = 601;
    public static final int INTELLIBITZCONTACT_FRAGMENT_LOADERID = 801;
    public static final int CONTACTITEM_FRAGMENT_LOADERID = 801;
    public static final int FAV_CONTACTITEM_FRAGMENT_LOADERID = 901;
    public static final int GROUP_CONTACTITEM_FRAGMENT_LOADERID = 111;
    public static final int GROUPCONTACT_DETAIL_FRAGMENT_LOADERID = 211;
    public static final int NESTITEM_FRAGMENT_LOADERID = 311;
    public static final int STACKITEM_FRAGMENT_LOADERID = 411;
    public static final int DRAFTITEM_FRAGMENT_LOADERID = 511;
    public static final int FAV_NESTITEM_FRAGMENT_LOADERID = 611;
    //    GCM CONSTANTS
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    //    BROADCAST EVENTS
    public static final String BROADCAST_FORCE_LOGOUT = "FORCE_LOGOUT";
    public static final String BROADCAST_USER_UPDATED = "USER_UPDATED";
    public static final String BROADCAST_NEW_USER_COMPLETE = "NEW_USER_COMPLETE";
    public static final String BROADCAST_GCM_REGISTRATION_COMPLETE = "GCM_REGISTRATION_COMPLETE";
    public static final String BROADCAST_EMAIL_ACCOUNT_ADDED = "EMAIL_ACCOUNT_ADDED";
    public static final String BROADCAST_EMAIL_ACCOUNT_REMOVED = "EMAIL_ACCOUNT_REMOVED";
    public static final String BROADCAST_CONTACT_EMAIL_SELECTED = "CONTACT_EMAIL_SELECTED";
    public static final String BROADCAST_CONTACT_PHONE_SELECTED = "CONTACT_PHONE_SELECTED";
    public static final String BROADCAST_CONTACT_PROFILE_VIEW = "CONTACT_PROFILE_VIEW";
    public static final String BROADCAST_MESSAGETO_NEST = "MESSAGETO_NEST";
    public static final String BROADCAST_MESSAGETO_DRAFT = "MESSAGETO_DRAFT";
    public static final String BROADCAST_MESSAGES_SEND = "MESSAGES_SEND";
    public static final String BROADCAST_CONTACT_GROUP_SELECTED = "CONTACT_GROUP_SELECTED";
    public static final String BROADCAST_NEW_EMAIL_DIALOG_OK = "NEW_EMAIL_DIALOG_OK";
    public static final String BROADCAST_NEW_CHAT_DIALOG_OK = "NEW_CHAT_DIALOG_OK";
    //    SOCKET SERVERS
    public static final String AUTH_GET_IP =
            MainApplicationSingleton.SOCKET_HOST + "/auth/get-ip";
    //    API SERVERS
    public static final String AUTH_ACCOUNT_CHECK_EMAIL_AVAILABLE =
            MainApplicationSingleton.API_HOST + "/auth/account/check-email-available";
    public static final String AUTH_ACCOUNT_GET_EMAIL_VERIFICATION =
            MainApplicationSingleton.API_HOST + "/auth/account/get-email-verification";
    public static final String AUTH_ACCOUNT_VERIFY_CODE =
            MainApplicationSingleton.API_HOST + "/auth/account/verify-code";
    public static final String AUTH_ACCOUNT_SIGNUP =
            MainApplicationSingleton.API_HOST + "/auth/account/signup";
    public static final String AUTH_ACCOUNT_SET_PWD =
            MainApplicationSingleton.API_HOST + "/auth/account/set-pwd";
    public static final String AUTH_ACCOUNT_LOGIN =
            MainApplicationSingleton.API_HOST + "/auth/account/login";
    public static final String AUTH_SET_PWD =
            MainApplicationSingleton.API_HOST + "/auth/account/set-pwd";
    public static final String GCMTOKEN_UPLOAD_URL =
            MainApplicationSingleton.API_HOST + "/auth/account/update-push-token";
    public static final String AUTH_GET_PROFILE =
            MainApplicationSingleton.API_HOST + "/auth/account/get-profile";
    public static final String AUTH_UPDATE_PROFILE =
            MainApplicationSingleton.API_HOST + "/auth/account/update-profile";

    public static final String AUTH_FEED_CREATE =
            MainApplicationSingleton.API_HOST + "/auth/feed/create";
    public static final String AUTH_FEED_UPDATE =
            MainApplicationSingleton.API_HOST + "/auth/feed/update";
    public static final String AUTH_FEED_DELETE =
            MainApplicationSingleton.API_HOST + "/auth/feed/delete";
    public static final String AUTH_FEED_LIST =
            MainApplicationSingleton.API_HOST + "/auth/feed/list";

    public static final String AUTH_SCHEDULE_CREATE =
            MainApplicationSingleton.API_HOST + "/auth/schedule/create";
    public static final String AUTH_SCHEDULE_UPDATE =
            MainApplicationSingleton.API_HOST + "/auth/schedule/update";
    public static final String AUTH_SCHEDULE_DELETE =
            MainApplicationSingleton.API_HOST + "/auth/schedule/delete";
    public static final String AUTH_SCHEDULE_LIST =
            MainApplicationSingleton.API_HOST + "/auth/schedule/list";

    public static final String AUTH_COMPANY_CREATE =
            MainApplicationSingleton.API_HOST + "/auth/company/create";
    public static final String AUTH_COMPANY_INVITE_USERS =
            MainApplicationSingleton.API_HOST + "/auth/company/invite-users";
    public static final String AUTH_COMPANY_GET_INVITES =
            MainApplicationSingleton.API_HOST + "/auth/company/get-invites";
    public static final String AUTH_COMPANY_JOIN =
            MainApplicationSingleton.API_HOST + "/auth/company/join";
    public static final String AUTH_COMPANY_LIST =
            MainApplicationSingleton.API_HOST + "/auth/company/list";

    public static final String AUTH_CONTACT_GET_WORK_CONTACTS =
            MainApplicationSingleton.API_HOST + "/auth/contact/get-work-contacts";

    public static final String AUTH_CHAT_SCHEDULE =
            MainApplicationSingleton.API_HOST + "/auth/chat/schedule";

    public static final String AUTH_EMAIL_GET_RECENT_EMAILS =
            MainApplicationSingleton.API_HOST + "/auth/email/get-recent-emails";
    public static final String AUTH_EMAIL_GET_MAILBOX_LIST =
            MainApplicationSingleton.API_HOST + "/auth/email/get-mailbox-list";
    public static final String AUTH_EMAIL_GET_FULL_EMAILS =
            MainApplicationSingleton.API_HOST + "/auth/email/get-full-emails";

    public static final String MOBILE_GETCODE_URL =
            MainApplicationSingleton.API_HOST + "/auth/mobile-get-code";
    public static final String AUTH_OTP_CALL =
            MainApplicationSingleton.API_HOST + "/auth/otp-call";
    public static final String AUTH_MOBILE_ACTIVATE =
            MainApplicationSingleton.API_HOST + "/auth/mobile-activate";
    public static final String AUTH_ADD_EMAIL =
            MainApplicationSingleton.API_HOST + "/auth/add-email";
    public static final String AUTH_GET_EMAILS =
            MainApplicationSingleton.API_HOST + "/auth/get-emails";
    public static final String AUTH_GET_EMAILS_BY =
            MainApplicationSingleton.API_HOST + "/auth/get-emails-by";
    public static final String AUTH_REMOVE_EMAIL =
            MainApplicationSingleton.API_HOST + "/auth/remove-email";
    public static final String AUTH_EMAIL_GOOGLE_TOKEN =
            MainApplicationSingleton.API_HOST + "/auth/email-google-token";
    public static final String CONTACTS_GET_URL =
            MainApplicationSingleton.API_HOST + "/auth/get-contacts";
    public static final String CONTACTS_UPLOAD_URL =
            MainApplicationSingleton.API_HOST + "/auth/upload-contacts";
    public static final String CONTACTS_BULK_UPLOAD_URL =
            MainApplicationSingleton.API_HOST + "/auth/bulk-upload-contacts";
    public static final String ATTACHMENT_UPLOAD_URL =
            MainApplicationSingleton.API_HOST + "/auth/upload-attachment";
    public static final String ATTACHMENT_GET_URL =
            MainApplicationSingleton.API_HOST + "/auth/get-attachment";
    public static final String AUTH_UPLOAD_PROFILE_PIC =
            MainApplicationSingleton.API_HOST + "/auth/upload-profile-pic";
    public static final String AUTH_GET_DOC =
            MainApplicationSingleton.API_HOST + "/auth/get-doc";
    public static final String AUTH_ACK_DOC =
            MainApplicationSingleton.API_HOST + "/auth/ack-doc";
    public static final String AUTH_MARK_READ =
            MainApplicationSingleton.API_HOST + "/auth/mark-read";
    public static final String AUTH_DELETED_MSGS =
            MainApplicationSingleton.API_HOST + "/auth/delete-msgs";
    public static final String AUTH_FLAG_MSGS =
            MainApplicationSingleton.API_HOST + "/auth/flag-msgs";
    public static final String AUTH_UNFLAG_MSGS =
            MainApplicationSingleton.API_HOST + "/auth/unflag-msgs";
    public static final String AUTH_GET_GROUPS =
            MainApplicationSingleton.API_HOST + "/auth/get-groups";
    public static final String AUTH_GET_BROADCAST_LISTS =
            MainApplicationSingleton.API_HOST + "/auth/get-broadcast-lists";
    public static final String AUTH_GROUP_DETAILS =
            MainApplicationSingleton.API_HOST + "/auth/group-details";
    public static final String AUTH_GROUP_MODIFY_USER_TYPE =
            MainApplicationSingleton.API_HOST + "/auth/group-modify-user-type";
    public static final String AUTH_CREATE_GROUP =
            MainApplicationSingleton.API_HOST + "/auth/create-group";
    public static final String AUTH_CREATE_BROADCAST_LIST =
            MainApplicationSingleton.API_HOST + "/auth/create-broadcast-list";
    public static final String AUTH_UPDATE_GROUP =
            MainApplicationSingleton.API_HOST + "/auth/update-group";
    public static final String AUTH_GROUP_ADD_USERS =
            MainApplicationSingleton.API_HOST + "/auth/group-add-users";
    public static final String AUTH_UPDATE_BROADCAST_LIST =
            MainApplicationSingleton.API_HOST + "/auth/update-broadcast-list";
    public static final String AUTH_ADD_TO_FOLDER =
            MainApplicationSingleton.API_HOST + "/auth/add-to-folder";
    public static final String AUTH_REMOVE_FROM_FOLDER =
            MainApplicationSingleton.API_HOST + "/auth/remove-from-folder";
    public static final String AUTH_GET_FOLDER_MSGS =
            MainApplicationSingleton.API_HOST + "/auth/get-folder-msgs";
    public static final String AUTH_GROUP_REMOVE_USERS =
            MainApplicationSingleton.API_HOST + "/auth/group-remove-users";
    public static final String AUTH_LEAVE_GROUP =
            MainApplicationSingleton.API_HOST + "/auth/leave-group";
    public static final String AUTH_GET_DRAFTS =
            MainApplicationSingleton.API_HOST + "/auth/get-drafts";
    public static final String AUTH_CREATE_FOLDER =
            MainApplicationSingleton.API_HOST + "/auth/create-folder";
    public static final String AUTH_GET_FOLDERS =
            MainApplicationSingleton.API_HOST + "/auth/get-folders";
    public static final String AUTH_UPDATE_FOLDER =
            MainApplicationSingleton.API_HOST + "/auth/update-folder";
    public static final String AUTH_DELETE_FOLDER =
            MainApplicationSingleton.API_HOST + "/auth/delete-folder";
    public static final String AUTH_CREATE_DRAFT =
            MainApplicationSingleton.API_HOST + "/auth/create-draft";
    //    public static final String GOOGLE_TOKEN_PARAM = "google-token";
    public final static int ACTION_GET_CONTENT = 11;
    public final static int REQUEST_CAMERA_AND_STORAGE_PERMISSION = 12;
    public final static int REQUEST_AUDIO_AND_STORAGE_PERMISSION = 13;
    public final static int REQUEST_CAMERA_PHOTO_AND_STORAGE_PERMISSION = 14;
    public final static int REQUEST_CAMERA_VIDEO_AND_STORAGE_PERMISSION = 15;
    public final static int REQUEST_CALL_PHONE_PERMISSION = 16;
    public static final Object NULL = new Object() {
        @Override
        public boolean equals(Object o) {
            return o == this || o == null; // API specifies this broken equals implementation
        }

        @Override
        public String toString() {
            return "null";
        }
    };
    public static final HashMap<String, String> typeCodeMAP = new HashMap<>();
    private static MainApplicationSingleton ourInstance = null;
    private Context applicationContext;
    private ConcurrentHashMap<String, ArrayList<? extends Object>> mGlobalVariables;
    private String uidCurrentUser;
    //    SHARED PREFERENCES - DEFAULT
    private SharedPreferences sharedPreferences = null;
    private ConcurrentLinkedQueue<Object> mIncomingQueue;
    private ConcurrentLinkedQueue<Object> mOutgoingQueue;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private MainApplicationSingleton(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null: " + context);

        } else {
            initializeInstance(context);
        }
    }

    public static MainApplicationSingleton getInstance(Context context) {
        if (null == ourInstance) {
            ourInstance = new MainApplicationSingleton(context);
        }
        if (null == ourInstance.mGlobalVariables) {
            ourInstance.initializeInstance(context);
        }
        return ourInstance;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Activity context) {
        try {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    apiAvailability.getErrorDialog(context, resultCode,
                            PLAY_SERVICES_RESOLUTION_REQUEST)
                            .show();
                } else {
                    Log.e(TAG, "GCM Play Services required for Notifications - Please update Google Play Services");
//                finish();
                }
                return false;
            }
            return true;
        } catch (Throwable ignored) {
            Log.e(TAG, ignored.getMessage());
            return false;
        }
    }

    public static String decodeBase64String(ArrayList<String> fileBase64) {
        String result = "";
        for (String val : fileBase64) {
// Receiving side
            byte[] b = Base64.decode(val, Base64.DEFAULT);
            result += new String(b);
        }
        return result;
    }

    public static String decodeBase64String(String fileBase64) {
        ArrayList<String> vals = new ArrayList<>();
        byte[] fullBytes = fileBase64.getBytes();
        int len = fullBytes.length;
        try {
// Receiving side
            byte[] b = Base64.decode(fullBytes, 0, len, Base64.DEFAULT);
            vals.add(new String(b));
        } catch (Throwable e) {
            vals.add(fileBase64);
        }
        String result = "";
        for (String val : vals) {
            result += val;
        }
        return result;
    }

    public static String decodeBase64String_(String fileBase64) {
        ArrayList<String> vals = new ArrayList<>();
        byte[] fullBytes = fileBase64.getBytes();
        int len = fullBytes.length;
        int parts2 = len % 1024;
        int parts = len / 1024;
        if (parts2 > 0) {
            parts += 1;
        }
        int start = 0;
        int finish = 0;
        for (int i = 0; i < parts; i++) {
            start = finish;
            if (i == parts - 1) {
                if (0 == finish)
                    finish = len;
                else
                    finish = len - finish;
            } else {
                finish += 1024;
            }
            try {
// Receiving side
                byte[] b = Base64.decode(fullBytes, start, finish, Base64.DEFAULT);
                vals.add(new String(b));
            } catch (Throwable e) {
                vals.add(fileBase64);
                break;
            }
        }
        String result = "";
        for (String val : vals) {
            result += val;
        }
        return result;
    }

    public static void writeStringToFile(String out, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] fullBytes = out.getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullBytes);
        int len = fullBytes.length;
        int parts2 = len % 1024;
        int parts = len / 1024;
        if (parts2 > 0) {
            parts += 1;
        }
        int start = 0;
        int finish = 0;
        for (int i = 0; i < parts; i++) {
            start = finish;
            if (i == parts - 1) {
                finish = len;
            } else {
                finish += 1024;
            }
// Receiving side
            int count = finish - start;
            byte[] buffer = new byte[count];
            int read = byteArrayInputStream.read(buffer, 0, count);
//            this check not required, since buffer is managed by this method
//            anyways.. no harm
            if (read != -1)
                fileOutputStream.write(buffer, 0, count);
        }
        byteArrayInputStream.close();
        fileOutputStream.close();
    }

    /*
    HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
    handlerThread.start();
    Looper looper = handlerThread.getLooper();
    Handler handler = new Handler(looper);
         */
    public static HandlerThread performOnUIHandlerThread(final Runnable runnable) {
        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Handler handler = new Handler();
        handler.post(runnable);
        return handlerThread;
    }

    public static HandlerThread performOnHandlerThread(final Runnable runnable) {
        return performOnHandlerThread(runnable, null);
    }

    public static HandlerThread performOnHandlerThread(final Runnable runnable, Looper looper) {
        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Handler handler;
        if (null == looper) {
            looper = handlerThread.getLooper();
        }
        handler = new Handler(looper);
        handler.post(runnable);
/*
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
*/
        return handlerThread;
    }

/*
    public static void downloadS3Url(final String url, final File file,
                                     final MessageItem attachmentItem, final Context context) {
        // Create an S3 client
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(
                AWS_ACCESS_KEY, AWS_SECRET_KEY);
        AmazonS3 s3 = new AmazonS3Client(basicAWSCredentials);
// Set the region of your S3 bucket
//        singapore
        s3.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        TransferUtility transferUtility = new TransferUtility(s3, context);
        String key = url.substring(url.indexOf("com/") + 4);
        TransferObserver transferObserver = transferUtility.download(
                AWS_S3_BUCKET,     */
/* The bucket to download from *//*

                key,    */
/* The key for the object to download *//*

                file        */
/* The file to download the object to *//*

        );
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                // do something
                if (TransferState.COMPLETED == state) {
//                    file download done
//                    attachmentItem.setDownloadURL(path);
//                saves in db
                    MessageAttachmentIntentService.asyncUpdateContactsIfDBEmpty(
                            context, attachmentItem);
//                    Log.e(TAG, "attachment file: saved in DB: " + attachmentItem.getDownloadURL());
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                //Display percentage transfered to user
                if (100 == percentage) {
//                    Log.e(TAG, "attachment file: 100% downloaded" + attachmentItem.getDownloadURL());
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                // do something
                ex.printStackTrace();
            }

        });
    }
*/

    /**
     * get datetime
     */
    public static String getDateTimeMillis(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long now = getDateTimeMillis();
//        checks if time is in seconds, converts to milli seconds
        if (Long.bitCount(now) != Long.bitCount(time)) {
            time = time * 1000;
        }
        Date date = new Date(time);
        return dateFormat.format(date);
    }

    public static String getDateTimeISO(String time) {
        String datetime = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
            Date date = dateFormat.parse(time);
            datetime = dateFormat.format(date);
        } catch (ParseException ignored) {
//            ignored.printStackTrace();
        }
        return datetime;

/*
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String string2 = "2001-07-04T12:08:56.235-07:00";
        Date result2 = df2.parse(string2);
*/

    }

    public static long getDateTimeMillisISO(String time) {
        long datetime = 0;
        if (null == time || time.isEmpty()) return datetime;
        try {
            DateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
            Date date = dateFormat.parse(time);
            datetime = date.getTime();
        } catch (ParseException ignored) {
//            ignored.printStackTrace();
        }
        return datetime;

/*
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String string2 = "2001-07-04T12:08:56.235-07:00";
        Date result2 = df2.parse(string2);
*/

    }

    public static long getDateTimeMillis() {
        return System.currentTimeMillis();
    }

    @NonNull
    public static String getLastPathFromURI(String downloadURL) throws URISyntaxException {
        URI uri = new URI(downloadURL);
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static ContentValues fillIfNotNull(ContentValues values, String name, Object val) {
        if (val != null) {
            if (JSONArray.class.isInstance(val)) {
                JSONArray jsonArray = ((JSONArray) val);
                if (jsonArray.length() > 0) values.put(name, val.toString());
            } else {
                values.put(name, val.toString());
            }
        }
        return values;
    }

    public static ContentValues fillIfNotNull(ContentValues values, String name, String val) {
        if (!isEmpty(val)) {
            values.put(name, val);
        }
        return values;
    }

    public static ContentValues fillIfNotNull(ContentValues values, String name, long val) {
        if (val != 0) {
            values.put(name, val);
        }
        return values;
    }

    public static ContentValues fillIfNotNull(ContentValues values, String name, boolean val) {
        if (val) {
            values.put(name, val);
        }
        return values;
    }

    public static String parseFormatNoCCPhoneNumberByISO(String number, String iso) {
        String formattedPhoneNumber = null;
        Phonenumber.PhoneNumber phoneNumber;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
//            parse the number, to the given region
            phoneNumber = phoneNumberUtil.parse(number, iso);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
//                clears country code, since number is valid for the country..
// and number without cc is required
/*
                Phonenumber.PhoneNumber noCCPhoneNumber = phoneNumber.clearCountryCode();
                noCCPhoneNumber.clearNumberOfLeadingZeros();
                noCCPhoneNumber.clearCountryCodeSource();
                noCCPhoneNumber.clearExtension();
                noCCPhoneNumber.clearItalianLeadingZero();
                noCCPhoneNumber.clearPreferredDomesticCarrierCode();
                noCCPhoneNumber.clearNationalNumber();
*/
//                Log.d(TAG, String.valueOf(phoneNumber.getNationalNumber()));
//                formats in neutral E164 format, and takes out the special chars
/*
                formattedPhoneNumber = PhoneNumberUtil.normalizeDigitsOnly(
                        phoneNumberUtil.format(noCCPhoneNumber,
                                PhoneNumberUtil.PhoneNumberFormat.E164));
*/
/*
                formattedPhoneNumber = phoneNumberUtil.format(phoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.E164);
*/
                formattedPhoneNumber = String.valueOf(phoneNumber.getNationalNumber());
            }
        } catch (NumberParseException npe) {
            formattedPhoneNumber = number;
        }
        return formattedPhoneNumber;
    }

    public static String parseFormatPhoneNumberByISO(String number, String iso) {
        String formattedPhoneNumber = null;
        if (null == number || number.isEmpty()) return formattedPhoneNumber;
        Phonenumber.PhoneNumber phoneNumber;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            phoneNumber = phoneNumberUtil.parse(number, iso);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                formattedPhoneNumber = phoneNumberUtil.format(phoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (NumberParseException npe) {
            formattedPhoneNumber = null;
        }

        return formattedPhoneNumber;
    }

    public static String getSimCountryIso(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        String country = telephonyManager.getNetworkCountryIso();
        if (null == country) {
            country = telephonyManager.getSimCountryIso();
        }
        if (null == country) {
            country = Locale.getDefault().getCountry();
        }
//        call IN .. if everything fails
        if (null == country) {
            country = "IN";
        }
        return country.toUpperCase();
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage, int w, int h) {
        Bitmap targetBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) w - 1) / 2,
                ((float) h - 1) / 2,
                (Math.min(((float) w),
                        ((float) h)) / 2),
                Path.Direction.CCW);
        canvas.clipPath(path);
        canvas.drawBitmap(scaleBitmapImage,
                new Rect(0, 0, scaleBitmapImage.getWidth(),
                        scaleBitmapImage.getHeight()),
                new Rect(0, 0, w, h), null);
        return targetBitmap;
    }

    //decodes image and scales it to reduce memory consumption
    public static Bitmap decodeStream(InputStream inputStream, int sz) {
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, o);

        //The new size we want to scale to
        final int REQUIRED_SIZE = sz;

        //Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(inputStream, null, o2);
    }

    public static <C> List<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<C>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    public static <C> C[] asArray(SparseArray<C> sparseArray) {
        List<C> arrayList = asList(sparseArray);
        if (null == arrayList) return null;
        return (C[]) arrayList.toArray();
    }

    public static <T> T getBaseItem(String id, Collection<? extends BaseBean> items) {
        if (null == id || null == items || 0 == items.size()) return null;
        for (BaseBean item : items) {
            if (id.equals(item.getDataId())) return (T) item;
        }
        return null;
    }

    public static JSONArray createsJSONArrayFromCollection(Collection<?> objects) {
        JSONArray jsonArray = new JSONArray();
        if (null == objects || objects.isEmpty()) return jsonArray;
        for (Object object : objects) {
            jsonArray.put(object.toString());
        }
        return jsonArray;
    }

    public static boolean isJSONValid(String test) {
        boolean result;
/*
        String a = test.substring(0,1);
        int len = test.length();
        int begin = len -1;
        if (begin < 0) begin = 0;
        int end = len;
        if (end < 1) end = 1;
        String z = test.substring(begin, end);
        if (!a.equals("{") || !a.equals("[")){
            return false;
        }
        if (!z.equals("}") || !z.equals("]")){
            return false;
        }
*/
        if (!isEmpty(test) &&
                (test.startsWith("{") || test.startsWith("[")) &&
                (test.endsWith("}") || test.endsWith("]"))) {
            try {
                new JSONObject(test);
                result = true;
            } catch (JSONException ex) {
                // edited, to include @Arthur's comment
                // e.g. in case JSONArray is valid as well...
                try {
                    new JSONArray(test);
                    result = true;
                } catch (JSONException ex1) {
                    result = false;
                }
            }
        } else {
            result = false;
        }
        return result;
    }

    public static Bitmap getBitmapDecodeHttp(String pic) throws IOException {
        if (isEmpty(pic)) return null;
        Bitmap bitmap = null;
        if (pic.startsWith("http")) {
//            cloud pic
            bitmap = HttpUrlConnectionParser.getBitmapFromURL(pic);
        }
        return bitmap;
    }

    @Nullable
    public static Bitmap getBitmapDecodeFile(String pic) {
        if (isEmpty(pic)) return null;
        Bitmap bitmap = null;
        if (pic.startsWith("/")) {
//                    storage pic
            bitmap = BitmapFactory.decodeFile(pic);
        }
        return bitmap;
    }

    public static Bitmap getBitmapDecodeAnyUri(String pic, Context context) throws IOException {
        if (null == pic || pic.isEmpty()) return null;
        return getBitmapDecodeAnyUri(Uri.parse(pic), context);
    }

    public static Bitmap getBitmapDecodeAnyUri(Uri uri, Context context) throws IOException {
        Bitmap bitmap = null;
        String pic = uri.toString();
        if (isEmpty(pic)) return bitmap;
        try {
//            tries the photo content uri
//            URI: content://com.android.contacts/contacts/776/photo
            bitmap = getBitmapDecodePhotoUri(uri, context);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
        if (null == bitmap)
            try {
//            tries the contacts content uri
//            URI: content://com.android.contacts/contacts/776
                bitmap = getBitmapDecodeContactsUri(uri, context);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        if (null == bitmap) {
            bitmap = getBitmapDecodeFile(pic);
        }
        if (null == bitmap)
//        falls back on either "resource" or "file" content
            try {
//            tries the resource or file content uri
//            URI: content://android.resource/
//            URI: content://file/
                bitmap = getBitmapMediaStoreImages(uri, context);
            } catch (FileNotFoundException e) {
//                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        if (null == bitmap) {
            bitmap = getBitmapDecodeHttp(pic);
        }
        return bitmap;
    }

    @Nullable
    public static Bitmap getBitmapMediaStoreImages(Uri uri, Context context) throws IOException {
        return MediaStore.Images.Media.getBitmap(
                context.getApplicationContext().getContentResolver(), uri);
/*
        InputStream input =
                context.getApplicationContext().getContentResolver().openInputStream(uri);
        if (input != null) {
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            return bitmap;
        }
        return null;
*/
    }

    @Nullable
    public static Bitmap getBitmapDecodePhotoUri(Uri photoURI, Context context) throws IOException {
        if (photoURI == null) {
            return null;
        }
        Cursor cursor = context.getApplicationContext().getContentResolver().query(photoURI,
                new String[]{
                        ContactsContract.CommonDataKinds.Photo.PHOTO
                }, null, null, null);
        try {
            if (cursor == null || !cursor.moveToNext()) {
                return null;
            }
            byte[] data = cursor.getBlob(0);
            if (data == null) {
                return null;
            }
            InputStream inputStream = new ByteArrayInputStream(data);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap getBitmapDecodeContactsUri(Uri uri, Context context) throws
            IOException {
//        uri is the contact uri.. with the last path as contact id
//        NOTE: the photo content uri is suffixed 'photo' in the target method below
        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getApplicationContext().getContentResolver(), uri);
        if (null == inputStream) return null;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        return bitmap;
    }

    public static String getMimeTypeFromURL(String url) {
        if (null == url || url.isEmpty()) return url;
        String extension = url.substring(url.lastIndexOf(".") + 1, url.length());
        if (extension.isEmpty()) return extension;
//            Log.d(TAG, "extension: " + extension);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static String getLastPath(String url) {
        if (null == url || url.isEmpty()) return url;
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    /**
     * 1.3. Example URI
     * https://www.ietf.org/rfc/rfc2396.txt
     * <p>
     * The following examples illustrate URI that are in common use.
     * <p>
     * ftp://ftp.is.co.za/rfc/rfc1808.txt
     * -- ftp scheme for File Transfer Protocol services
     * <p>
     * gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles
     * -- gopher scheme for Gopher and Gopher+ Protocol services
     * <p>
     * http://www.math.uio.no/faq/compression-faq/part1.html
     * -- http scheme for Hypertext Transfer Protocol services
     * <p>
     * mailto:mduerst@ifi.unizh.ch
     * -- mailto scheme for electronic mail addresses
     * <p>
     * news:comp.infosystems.www.servers.unix
     * -- news scheme for USENET news groups and articles
     * <p>
     * telnet://melvyl.ucop.edu/
     * -- telnet scheme for interactive services via the TELNET Protocol
     *
     * @param uri
     * @param mimeType
     * @param context
     */
    public static void startMimeActivity(String uri, String mimeType, Context context) {
        if (!isEmpty(uri))
            startMimeActivity(Uri.parse(uri), mimeType, context);
    }

    public static void startMimeActivity(Uri uri, String mimeType, Context context) {
        //                        Log.d(TAG, "Clicked view: "+v);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Intent target = intent;
//        Uri uri = Uri.fromFile(file);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        final String mt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mt != null) mimeType = mt;
        if (null == mimeType) {
            intent.setData(uri);
            target = Intent.createChooser(intent, "Choose an app to open with:");
        } else {
            intent.setDataAndType(uri, mimeType);
        }
        context.startActivity(target);
//        // TODO: 24-05-2016
//        to set the correct mime type
/*
                        intent.setDataAndType(
                                Uri.fromFile(new File(attachmentItem.getDownloadURL())),
                                attachmentItem.getType() + "/" + attachmentItem.getSubType());
*/
    }

    public static void startMimeActivity(File file, String mimeType, Context context) {
        Uri uri = Uri.fromFile(file);
        startMimeActivity(uri, mimeType, context);
    }

    public static void logD(JSONObject sb, String TAG) {
        if (null == sb || 0 == sb.length()) {
            Log.d(TAG, " EMPTY log message");
            return;
        }
        logD(new StringBuffer(sb.toString()), TAG);
    }

    public static void logD(StringBuffer sb, String TAG) {
        if (null == sb || 0 == sb.length()) {
            Log.d(TAG, " EMPTY log message");
            return;
        }
        if (sb.length() > 4000) {
            Log.d(TAG, "sb.length = " + sb.length());
            int chunkCount = sb.length() / 4000;     // integer division
            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                if (max >= sb.length()) {
                    Log.d(TAG, i + " of " + chunkCount + ":" + sb.substring(4000 * i));
                } else {
                    Log.d(TAG, i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
                }
            }
        } else {
            Log.d(TAG, sb.toString());
        }
    }

    public static void forceLogout(final Context context) {
// AUTHENTICATION FAIL.. FORCE LOGOUT
        AlertDialog dialog = alertDialog(context,
                "Authentication Failed.. Please Register again for Intellibitz to work",
                "Force Logout",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
//                        MainActivity.startMainActivityForceLogout(context);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        dialog.show();
    }

    public static AlertDialog alertDialog(final Context context, String msg, String title,
                                          DialogInterface.OnClickListener okListener,
                                          DialogInterface.OnClickListener cancelListener) {
// 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(msg).setTitle(title);
// Add the buttons
        if (okListener != null)
            builder.setPositiveButton(R.string.ok, okListener);
        if (cancelListener != null)
            builder.setNegativeButton(R.string.cancel, cancelListener);
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static boolean isEmpty(String val) {
        return "null".equalsIgnoreCase(val) || TextUtils.isEmpty(val);
    }

    public static boolean isAPI15() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public static boolean isAPI16() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isAPI17() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean isAPI21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static Object wrap(Object o) {
        if (o == null) {
            return NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            } else if (o.getClass().isArray()) {
                JSONArray jsonArray = new JSONArray();
                if (!o.getClass().isArray()) {
                    throw new JSONException("Not a primitive array: " + o.getClass());
                }
                final int length = Array.getLength(o);
                ArrayList<Object> values = new ArrayList<Object>(length);
                for (int i = 0; i < length; ++i) {
                    jsonArray.put(wrap(Array.get(o, i)));
                }
                return jsonArray;
            }
            if (o instanceof Map) {
                return new JSONObject((Map) o);
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Creates a new {@code JSONArray} with values from the given primitive array.
     */
    public static JSONArray newJSONArray(Object array) throws JSONException {
        JSONArray result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                result = new JSONArray(array);
            } catch (JSONException e) {
                Log.e(TAG, "newJSONArray: " + e);
                throw e;
            }
        } else {
            if (!array.getClass().isArray()) {
                throw new JSONException("Not a primitive array: " + array.getClass());
            }
            result = new JSONArray();
            final int length = Array.getLength(array);
            for (int i = 0; i < length; ++i) {
                result.put(wrap(Array.get(array, i)));
            }
        }
        return result;
    }

/*
    public static Set<ContactItem> mockWorkContacts(Set<ContactItem> workContacts) {
        if (null == workContacts) workContacts = new HashSet<>();
        workContacts.add(DeviceContactContentProvider.createWorkContactItem(
                "USR_ineya.rm@gmail.com", "ineya.rm@gmail.com", "Ineya", "Mock", null));
        workContacts.add(DeviceContactContentProvider.createWorkContactItem(
                "USR_rmsara07@gmail.com", "rmsara07@gmail.com", "Sara", "Mock", null));
        workContacts.add(DeviceContactContentProvider.createWorkContactItem(
                "USR_bharath@intellibitz.com", "bharath@intellibitz.com", "Bharath", "Mock", null));
        workContacts.add(DeviceContactContentProvider.createWorkContactItem(
                "USR_jeff@intellibitz.com", "jeff@intellibitz.com", "Jeff", "Mock", null));
        return workContacts;
    }
*/

    private void initializeInstance(Context context) {
// Called before any other component is created
        mGlobalVariables = new ConcurrentHashMap<>();
        mIncomingQueue = new ConcurrentLinkedQueue<>();
        mOutgoingQueue = new ConcurrentLinkedQueue<>();


        typeCodeMAP.put("None", "UnRegistered");
        typeCodeMAP.put("Other", "Unknown Organization");
        typeCodeMAP.put("Pvt Ltd", "Private Limited");
        typeCodeMAP.put("Public", "Public Limited");
        typeCodeMAP.put("INC", "Incorporated");
        typeCodeMAP.put("Partner", "Limited Liability, Partner");

        if (null == context) {
            Log.e(TAG, "Context is null: ");

        } else {
            applicationContext = context.getApplicationContext();
        }
        if (null == applicationContext) {
            Log.e(TAG, "Application Context is null: " + context);
        } else {
            // do all your initialization here
            getDefaultSharedPreferences(applicationContext);
//        sets the currently logged in user, last known
            setUidCurrentUser(getStringValueSP(MainApplicationSingleton.UID_USER_LOGGED_IN_PARAM, false));

// Volley init
            mRequestQueue = getRequestQueue();
            mImageLoader = getImageLoader();
        }
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(applicationContext);
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        if (null == mImageLoader)
            mImageLoader = new ImageLoader(getRequestQueue(), getImageCache());
        return mImageLoader;
    }

    @NonNull
    private ImageLoader.ImageCache getImageCache() {
        return new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(20);

            @Override
            public Bitmap getBitmap(String url) {
                if (!url.contains("http") || !url.contains("https")) {
                    String local = url.substring(url.indexOf("/"), url.length());
//                            handles /storage urls
//                            // TODO: 12-03-2016
//                            handle file:/// urls
                    Bitmap bm = BitmapFactory.decodeFile(local);
//                            image might have been deleted in device.. handle null
                    if (null == bm) {
                        return null;
                    } else {
                        putBitmap(url, bm);
                        return bm;
                    }
                }
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        };
    }

    public SharedPreferences getDefaultSharedPreferences(Context context) {
        if (null == sharedPreferences) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sharedPreferences;
    }

    public ArrayList<? extends Object> removeGlobalSBAsList(String key) {
        return mGlobalVariables.remove(key);
    }

    public String removeGlobalSB(String key) {
        String val = getGlobalSBValue(key);
        mGlobalVariables.remove(key);
        return val;
    }

    public void initGlobalSB(String key) {
        ArrayList<? extends Object> vals = mGlobalVariables.get(key);
        if (null == vals) {
            vals = new ArrayList<>();
            mGlobalVariables.put(key, vals);
        }
    }

    public void appendValToGlobalSB(String key, String value) {
        initGlobalSB(key);
        ArrayList<String> vals = (ArrayList<String>) mGlobalVariables.get(key);
        vals.add(value);
        mGlobalVariables.put(key, vals);
    }

    public void putGlobalVariable(String key, ArrayList<? extends Object> val) {
        if (null == key) return;
        ArrayList<? extends Object> objects = mGlobalVariables.get(key);
        if (null == objects) {

        } else {
            mGlobalVariables.remove(key);
        }
        if (val != null)
            mGlobalVariables.put(key, val);
    }

    public ArrayList<? extends Object> getGlobalSBValueAsList(String key) {
        return mGlobalVariables.get(key);
    }

    public String getGlobalSBValue(String key) {
        ArrayList<? extends Object> vals = mGlobalVariables.get(key);
        String result = "";
        for (Object val : vals) {
            result += val;
        }
        return result;
    }

    public String getUidCurrentUser() {
        return uidCurrentUser;
    }

    public void setUidCurrentUser(String uidCurrentUser) {
        this.uidCurrentUser = uidCurrentUser;
    }

    public ConcurrentLinkedQueue<Object> getIncomingQueue() {
        return mIncomingQueue;
    }

    public void setIncomingQueue(ConcurrentLinkedQueue<Object> mIncomingQueue) {
        this.mIncomingQueue = mIncomingQueue;
    }

    public ConcurrentLinkedQueue<Object> getOutgoingQueue() {
        return mOutgoingQueue;
    }

    public void setOutgoingQueue(ConcurrentLinkedQueue<Object> mOutgoingQueue) {
        this.mOutgoingQueue = mOutgoingQueue;
    }

    //    SHARED PREFERENCES
    public long getLongValueSP(String key) {
        if (key != null && !key.equals("")) {
            return sharedPreferences.getLong(getKey(key), 0);
        }
        return 0;
    }

    private String getKey(String key) {
        return getUidCurrentUser() + key;
    }

    public String getStringValueSP(String key, boolean b) {
        if (null == sharedPreferences) {
            Log.e(TAG, "Shared Preferences is null: cannot get key - " + key);
            return null;
        }
        if (key != null && !key.equals("")) {
            if (b) {
                key = getKey(key);
            }
            return sharedPreferences.getString(key, null);
        }
        return null;
    }

    public String getStringValueSP(String key) {
        return getStringValueSP(key, true);
    }

    public Set<String> getStringSetValueSP(String key) {
        if (key != null && !key.equals("")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return sharedPreferences.getStringSet(getKey(key), new HashSet<String>());
            }
        }
        return null;
    }

    public int getIntValueSP(String key) {
        if (key != null && !key.equals("")) {
            return sharedPreferences.getInt(getKey(key), 0);
        }
        return 0;
    }

    public int getIntValueByDefaultSP(String key) {
        if (key != null && !key.equals("")) {
            return sharedPreferences.getInt(getKey(key), 0);
        }
        return 0;
    }

    public boolean getBooleanValueSP(String key) {
        if (key != null && !key.equals("")) {
            return sharedPreferences.getBoolean(getKey(key), false);
        }
        return true;
    }

    public float getFloatValueSP(String key) {
        if (key != null && !key.equals("")) {
            return sharedPreferences.getFloat(getKey(key), 0);
        }
        return 0;
    }

    public void putStringValueSP(String key, String value, boolean b) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (b) {
                key = getKey(key);
            }
            editor.putString(key, value);
            editor.apply();
        }
    }

    public void putStringValueSP(String key, String value) {
        putStringValueSP(key, value, true);
    }

    public void putStringSetValueSP(String key, Set<String> value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
/*
            if (value != null && !value.isEmpty()){
                for (String val: value){
                    Set<String> target = getStringSetValueSP(key);
                    target.remove(val);
                    editor.apply();
                }
            }
*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet(getKey(key), value);
                editor.apply();
            }
        }
    }

    public void addStringSetValueSP(String key, String value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> values = getStringSetValueSP(key);
            values.add(value);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet(getKey(key), values);
                editor.apply();
            }
        }
    }

    public void removeStringSetValueSP(String key, String value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> values = getStringSetValueSP(key);
//            remove the value from the collection
            values.remove(value);
//            clears the old collection
            clearStringSetValueSP(key);
//            saves the new collecion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet(getKey(key), values);
                editor.apply();
            }
        }
    }

    public void clearStringSetValueSP(String key) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
//            Set<String> values = getStringSetValueSP(key);
//            values.clear();
            editor.remove(getKey(key));
//            editor.putStringSet(getKey(key), values);
            editor.apply();
        }
    }

    public void putIntValueSP(String key, int value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getKey(key), value);
            editor.apply();
        }
    }

    public void putBooleanValueSP(String key, boolean value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getKey(key), value);
            editor.apply();
        }
    }

    public void putLongValueSP(String key, long value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(getKey(key), value);
            editor.apply();
        }
    }

    public void putFloatValueSP(String key, Float value) {
        if (key != null && !key.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat(getKey(key), value);
            editor.apply();
        }
    }

    public static class CheckNetworkConnection {

        public static boolean isNetworkConnectionAvailable(Context context) {

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()
                        && netInfo.isConnectedOrConnecting()
                        && netInfo.isAvailable()) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isNetworkConnectedOrConnecting(Context context) {

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()
                        && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     */
    public static class Serializer {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public static byte[] serialize_(Object obj) throws IOException {
            try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
                try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                    o.writeObject(obj);
                }
                return b.toByteArray();
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public static Object deserialize_(byte[] bytes) throws IOException, ClassNotFoundException {
            try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
                try (ObjectInputStream o = new ObjectInputStream(b)) {
                    return o.readObject();
                }
            }
        }

        public static synchronized Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = null;
            Object o = null;
            try {
                in = new ObjectInputStream(bis);
                o = in.readObject();
            } finally {
                try {
                    bis.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
            }
            return o;
        }

        public static synchronized byte[] serialize(Object obj) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            byte[] bytes = null;
            try {
                out = new ObjectOutputStream(bos);
                out.writeObject(obj);
                bytes = bos.toByteArray();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
                try {
                    bos.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }
            return bytes;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
//            // TODO: 11-03-2016
//            keep local cache
//            MediaPickerUri.writeToTempImageAndGetPathUri(getApplicationContext(), result);
            bmImage.setImageBitmap(result);
        }
    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("/sdcard/file_name.extension");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }


}
