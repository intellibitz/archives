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

import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.ContactsContactJoinColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.db.MessagesContactsJoinColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.data.BaseItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.ContactsContactJoinColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.db.MessagesContactsJoinColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static intellibitz.intellidroid.content.DeviceContactContentProvider.getDeviceContactsJoin;
import static intellibitz.intellidroid.content.MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT;
import static intellibitz.intellidroid.db.MessagesContactsJoinColumns.KEY_MSGTHREAD_ID;

/**
 *
 */
public class MsgEmailContactsContentProvider extends
        ContentProvider {

    public static final String TAG = "MsgEmailContactsCP";
    public static final String TABLE_MSGEMAILCONTACTS = "msgemailcontacts";
    public static final String TABLE_MSGEMAILCONTACTS_CONTACT_JOIN = "msgemailcontacts_contact";
    public static final String CREATE_TABLE_MSGEMAILCONTACTS = "CREATE TABLE "
            + TABLE_MSGEMAILCONTACTS + ContactItemColumns.TABLE_CONTACTS_SCHEMA;
    //    The content provider scheme
    public static final String SCHEME = "content://";
    // MIME types used for searching words or looking up a single definition
    public static final String CONTACTS_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/all";
    public static final String CONTACTS_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/_id";
    public static final String CONTACTS_DATA_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.intellibitz.android.intellibitzdb/id";
    public static final String CONTACTS_JOIN_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/all";
    public static final String CONTACTS_JOIN_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/_id";
    public static final String CONTACTS_JOIN_DATA_ITEM_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/join/id";
    public static final String CONTACTS_RAW_DIR_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.intellibitz.android.intellibitzdb/raw/all";
    public static final String CREATE_TABLE_MSGEMAILCONTACTS_CONTACT_JOIN = "CREATE TABLE "
            + TABLE_MSGEMAILCONTACTS_CONTACT_JOIN + "( " +
            ContactsContactJoinColumns.KEY_ID + " INTEGER PRIMARY KEY," +
            ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID + " INTEGER," +
            ContactsContactJoinColumns.KEY_CONTACT_ID + " INTEGER," +
            ContactsContactJoinColumns.KEY_TIMESTAMP + " LONG" + ")";
    // UriMatcher stuff
    private static final int CONTACTS_DIR_TYPE = 0;
    private static final int CONTACTS_ITEM_TYPE = 1;
    private static final int CONTACTS_DATA_ITEM_TYPE = 2;
    private static final int CONTACTS_JOIN_DIR_TYPE = 3;
    private static final int CONTACTS_JOIN_ITEM_TYPE = 4;
    private static final int CONTACTS_JOIN_DATA_ITEM_TYPE = 5;
    private static final int CONTACTS_RAW_DIR_TYPE = 6;
    private static final int SEARCH_SUGGEST = 7;
    private static final int REFRESH_SHORTCUT = 8;
    public static String AUTHORITY = "intellibitz.intellidroid.content.MsgEmailContactsContentProvider";
    //    to get only pure contacts minus join
    public static final Uri CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + TABLE_MSGEMAILCONTACTS);
    //    to execute contacts with join.. example with mobiles and emails
    public static final Uri JOIN_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "join_" + TABLE_MSGEMAILCONTACTS);
    //    to execute raw sql.. example select count(*)
    public static final Uri RAW_CONTENT_URI = Uri.parse(
            SCHEME + AUTHORITY + "/" + "raw_" + TABLE_MSGEMAILCONTACTS);
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private DatabaseHelper databaseHelper;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACTS, CONTACTS_DIR_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACTS +
                "/#", CONTACTS_ITEM_TYPE);
        matcher.addURI(AUTHORITY, TABLE_MSGEMAILCONTACTS +
                "/*", CONTACTS_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGEMAILCONTACTS, CONTACTS_JOIN_DIR_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGEMAILCONTACTS +
                        "/#", CONTACTS_JOIN_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "join_" + TABLE_MSGEMAILCONTACTS +
                        "/*", CONTACTS_JOIN_DATA_ITEM_TYPE);
        matcher.addURI(AUTHORITY,
                "raw_" + TABLE_MSGEMAILCONTACTS, CONTACTS_RAW_DIR_TYPE);
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

    @NonNull
    public static Collection<ContactItem> createsContactItemsFromGetGroupsJSONArray(
            JSONArray jsonArray) {
        if (null == jsonArray) return null;
        int len = jsonArray.length();
        Collection<ContactItem> contactItems = new ArrayList<>(len);
        if (0 == len) return contactItems;
        for (int i = 0; i < len; i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    JSONObject group = jsonObject.optJSONObject("group");
                    if (group != null) {
                        ContactItem contactItem = createsContactFromJSON(jsonObject);
                        JSONArray groupInfo = jsonObject.optJSONArray("group_info");
                        updateContactThreadWithGroupInfoFromJSON(groupInfo, contactItem);
                        contactItem.setNewGroup(false);
                        contactItem.setGroup(true);
                        contactItems.add(contactItem);
                    }
                }
/*
                ContactItem contactItem =
                        createsContactFromGetGroupsJSON(jsonObject);
*/
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
        return contactItems;
    }

    @NonNull
    public static JSONArray fillJsonArrayFromContactItem(
            Collection<ContactItem> contactItems, Context context) {
        JSONArray contacts = new JSONArray();
        for (ContactItem contact : contactItems) {
            try {
                String firstName = contact.getName();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("first_name", firstName);
                jsonObject.put("device_ref", contact.getDeviceRef());
                jsonObject.put("c_id", contact.get_id());
                jsonObject.put("profile_pic", contact.getProfilePic());
                JSONArray mobiles = new JSONArray();
                contacts.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return contacts;
    }

    public static void fillContactThreadItemFromCursor(ContactItem contactItem,
                                                       Cursor cursor) {
        contactItem.set_id(cursor.getLong(cursor.getColumnIndex(
                ContactItemColumns.KEY_ID)));
        contactItem.setDeviceContactId(cursor.getLong(cursor.getColumnIndex(
                ContactItemColumns.KEY_DEVICE_CONTACTID)));
        contactItem.setDataId(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_DATA_ID)));
        contactItem.setTypeId(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_TYPE_ID)));
        contactItem.setDocType(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_DOC_TYPE)));
        contactItem.setName(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_NAME)));
        contactItem.setFirstName(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_FIRST_NAME)));
        contactItem.setLastName(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_LAST_NAME)));
        contactItem.setDisplayName(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_DISPLAY_NAME)));
        contactItem.setProfilePic(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_PIC)));
        contactItem.setStatus(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_STATUS)));
        contactItem.setDataRev(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_DATA_REV)));
        contactItem.setType(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_TYPE)));
        contactItem.setDocOwner(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_DOC_OWNER)));
        contactItem.setTimestamp(cursor.getLong(cursor.getColumnIndex(
                ContactItemColumns.KEY_TIMESTAMP)));
        contactItem.setDateTime(cursor.getString(cursor.getColumnIndex(
                ContactItemColumns.KEY_DATETIME)));
    }

    public static HashMap<String, ContactItem> fillGroupContactsItemFromCursor(
            HashMap<String, ContactItem> contactItems, Cursor cursor) {
        do {
            ContactItem contactItem = new ContactItem();
            fillContactThreadItemFromCursor(contactItem, cursor);
            contactItems.put(contactItem.getDataId(), contactItem);
        } while (cursor.moveToNext());
        return contactItems;
    }

    public static List<ContactItem> createsContactsFromJoinCursor(
            Cursor cursor) {
        List<ContactItem> contactItems = new ArrayList<>();
        ContactItem contactThreadItem;
        do {
            String sid = cursor.getString(cursor.getColumnIndex("gcid"));
            contactThreadItem = MainApplicationSingleton.getBaseItem(sid, contactItems);
            if (null == contactThreadItem) {
                contactThreadItem = new ContactItem();
                contactThreadItem.set_id(cursor.getLong(cursor.getColumnIndex("gc_id")));
                contactThreadItem.setDeviceContactId(cursor.getLong(cursor.getColumnIndex("gcdcid")));
                contactThreadItem.setDataId(sid);
//                contactItem.setGroupId(cursor.getString(cursor.getColumnIndex("gcgid")));
                contactThreadItem.setName(cursor.getString(cursor.getColumnIndex("gcname")));
                contactThreadItem.setFirstName(cursor.getString(cursor.getColumnIndex("gcfname")));
                contactThreadItem.setLastName(cursor.getString(cursor.getColumnIndex("gclname")));
                contactThreadItem.setDisplayName(cursor.getString(cursor.getColumnIndex("gcdname")));
                contactThreadItem.setProfilePic(cursor.getString(cursor.getColumnIndex("gcpic")));
                contactThreadItem.setStatus(cursor.getString(cursor.getColumnIndex("gcstat")));
                contactThreadItem.setType(cursor.getString(cursor.getColumnIndex("gctype")));
                contactThreadItem.setTypeId(cursor.getString(cursor.getColumnIndex("gctypeid")));
                contactThreadItem.setGroup(cursor.getInt(cursor.getColumnIndex("gcisg")));
                contactThreadItem.setEmailItem(cursor.getInt(cursor.getColumnIndex("gcise")));
                contactItems.add(contactThreadItem);
            }
            String akid = cursor.getString(cursor.getColumnIndex("akid"));
            if (null == akid) {

            } else {
                ContactItem contactItem = contactThreadItem.getContactItem(akid);
                if (null == contactItem) {
//            adds contact to message
//                    // TODO: 20-06-2016
//                    to fetch mobile
//                    MobileItem mobileItem = new MobileItem();
//                    // TODO: 30-06-2016
//                    mobileItem.setIntellibitzId(akid);
//                    // TODO: 30-06-2016
                    ContactItem intellibitzContactItem = new ContactItem(akid);
                    contactItem = new ContactItem(intellibitzContactItem);
//                    msgContactItem.setDataId(akid);
//                    msgContactItem.setIntellibitzId(akid);
                    contactThreadItem.addContact(contactItem);
                }
                contactItem.set_id(cursor.getLong(cursor.getColumnIndex("ak_id")));
                contactItem.setTypeId(cursor.getString(cursor.getColumnIndex("aktypeid")));
                contactItem.setName(cursor.getString(cursor.getColumnIndex("akname")));
                contactItem.setType(cursor.getString(cursor.getColumnIndex("aktype")));
                contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("akonid")));
                contactItem.setStatus(cursor.getString(cursor.getColumnIndex("akstatus")));
                contactItem.setGroup(cursor.getInt(cursor.getColumnIndex("akisg")));
                contactItem.setEmailItem(cursor.getInt(cursor.getColumnIndex("akise")));
                contactItem.setCloud(cursor.getInt(cursor.getColumnIndex("akisc")));
                contactItem.setDevice(cursor.getInt(cursor.getColumnIndex("akisd")));
                contactItem.setAnonymous(cursor.getInt(cursor.getColumnIndex("akisa")));
            }
        } while (cursor.moveToNext());
        return contactItems;
    }

    public static void fillContactThreadFromJoinCursor(ContactItem contactThreadItem, Cursor cursor) {
        contactThreadItem.set_id(cursor.getLong(cursor.getColumnIndex("gc_id")));
        contactThreadItem.setDeviceContactId(cursor.getLong(cursor.getColumnIndex("gcdcid")));
        contactThreadItem.setDataId(cursor.getString(cursor.getColumnIndex("gcid")));
        contactThreadItem.setTypeId(cursor.getString(cursor.getColumnIndex("gctypeid")));
        contactThreadItem.setName(cursor.getString(cursor.getColumnIndex("gcname")));
        contactThreadItem.setFirstName(cursor.getString(cursor.getColumnIndex("gcfname")));
        contactThreadItem.setLastName(cursor.getString(cursor.getColumnIndex("gclname")));
        contactThreadItem.setDisplayName(cursor.getString(cursor.getColumnIndex("gcdname")));
        contactThreadItem.setProfilePic(cursor.getString(cursor.getColumnIndex("gcpic")));
        contactThreadItem.setStatus(cursor.getString(cursor.getColumnIndex("gcstat")));
        contactThreadItem.setType(cursor.getString(cursor.getColumnIndex("gctype")));
        contactThreadItem.setTypeId(cursor.getString(cursor.getColumnIndex("gctypeid")));
        contactThreadItem.setGroup(cursor.getInt(cursor.getColumnIndex("gcisg")));
        contactThreadItem.setEmailItem(cursor.getInt(cursor.getColumnIndex("gcise")));
        do {
            String akid = cursor.getString(cursor.getColumnIndex("akid"));
            if (null == akid) {

            } else {
                ContactItem contactItem = contactThreadItem.getContactItem(akid);
                if (null == contactItem) {
//            adds contact to message
//                    // TODO: 20-06-2016
//                    to fetch mobile
//                    MobileItem mobileItem = new MobileItem();
//                    // TODO: 30-06-2016
//                    mobileItem.setIntellibitzId(akid);
                    ContactItem intellibitzContactItem = new ContactItem(akid);
                    contactItem = new ContactItem(intellibitzContactItem);
//                    contactItem.setDataId(akid);
//                    contactItem.setIntellibitzId(akid);
                    contactThreadItem.addContact(contactItem);
                }
                contactItem.set_id(cursor.getLong(cursor.getColumnIndex("ak_id")));
                contactItem.setName(cursor.getString(cursor.getColumnIndex("akname")));
                contactItem.setType(cursor.getString(cursor.getColumnIndex("aktype")));
                contactItem.setIntellibitzId(cursor.getString(cursor.getColumnIndex("akonid")));
                contactItem.setProfilePic(cursor.getString(cursor.getColumnIndex("akpic")));
                contactItem.setStatus(cursor.getString(cursor.getColumnIndex("akstatus")));
                contactItem.setTypeId(cursor.getString(cursor.getColumnIndex("aktypeid")));
                contactItem.setGroup(cursor.getInt(cursor.getColumnIndex("akisg")));
                contactItem.setEmailItem(cursor.getInt(cursor.getColumnIndex("akise")));
                contactItem.setCloud(cursor.getInt(cursor.getColumnIndex("akisc")));
                contactItem.setDevice(cursor.getInt(cursor.getColumnIndex("akisd")));
                contactItem.setAnonymous(cursor.getInt(cursor.getColumnIndex("akisa")));
            }
        } while (cursor.moveToNext());
    }

    public static ContactItem updateContactThreadWithGroupInfoFromJSON(
            JSONArray jsonArray, ContactItem contactItem) throws
            JSONException {
        if (null == jsonArray || 0 == jsonArray.length()) return contactItem;
        for (int i = 0; i < jsonArray.length(); i++) {
            updateContactThreadWithGroupInfoFromJSON(jsonArray.getJSONObject(i), contactItem);
        }
        return contactItem;
    }

    public static ContactItem updateContactThreadWithGroupInfoFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws
            JSONException {
        if (null == jsonObject) return contactItem;
        String actionText = jsonObject.getString("action_text");
        if ("CREATED_GROUP".equalsIgnoreCase(actionText)) {
            fillCreatedGroupInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
//            return savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("ADDED_USERS".equalsIgnoreCase(actionText)) {
            fillAddedUsersInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
//            return savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("REMOVED_USERS".equalsIgnoreCase(actionText)) {
            fillRemovedUsersInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
//            return deleteContact(contactItem, context.getApplicationContext());
        } else if ("MADE_ADMIN".equalsIgnoreCase(actionText)) {
            fillMadeAdminInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
//            return savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("MADE_USER".equalsIgnoreCase(actionText)) {
            fillMadeUserInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
//            return savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("LEFT_GROUP".equalsIgnoreCase(actionText)) {
            fillLeftGroupInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
//            return deleteContact(contactItem, context.getApplicationContext());
        }
        if (TextUtils.isEmpty(contactItem.getDataId())) return null;
//        return savesContactThreadInDB(contactItem, context.getApplicationContext());
        return contactItem;
    }

    public static ContactItem updatePlusSaveContactThreadWithGroupInfoFromJSON(
            JSONObject jsonObject, Context context) throws
            JSONException {
        ContactItem contactItem = new ContactItem();
        String actionText = jsonObject.getString("action_text");
        if ("CREATED_GROUP".equalsIgnoreCase(actionText)) {
            fillCreatedGroupInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
            savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("ADDED_USERS".equalsIgnoreCase(actionText)) {
            fillAddedUsersInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
            savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("REMOVED_USERS".equalsIgnoreCase(actionText)) {
            fillRemovedUsersInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
            deleteContact(contactItem, context.getApplicationContext());
        } else if ("MADE_ADMIN".equalsIgnoreCase(actionText)) {
            fillMadeAdminInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
            savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("MADE_USER".equalsIgnoreCase(actionText)) {
            fillMadeUserInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
            savesContactThreadInDB(contactItem, context.getApplicationContext());
        } else if ("LEFT_GROUP".equalsIgnoreCase(actionText)) {
            fillLeftGroupInfoFromJSON(jsonObject, contactItem);
            if (TextUtils.isEmpty(contactItem.getDataId())) return null;
            deleteContact(contactItem, context.getApplicationContext());
        }
        if (TextUtils.isEmpty(contactItem.getDataId())) return null;
        savesContactThreadInDB(contactItem, context.getApplicationContext());
        return contactItem;
/*

{
    "_id": "e88d83fc4da9d4849fe72b7c9925597c",
    "_rev": "1-641359b5dbfc7591466028fabd55b922",
    "doc_type": "GROUP-INFO",
    "doc_owner": "USRMASTER_919840348914",
    "timestamp": 1463739453,
    "group_id": "e88d83fc4da9d4849fe72b7c99247219",
    "action_by": "USRMASTER_919600037000",
    "action_on": [
        {
            "uid": "USRMASTER_919655653929"
        },
        {
            "uid": "USRMASTER_919840348914"
        }
    ],
    "action_text": "ADDED_USERS",
    "pending_docs": 4
}
{
    "_id": "544492954dcdc39ecffa42fc1e9bd27f",
    "_rev": "1-507a8ca50544d812142744b7e9605c70",
    "doc_type": "GROUP-INFO",
    "doc_owner": "USRMASTER_919100504678",
    "timestamp": 1464002226,
    "group_id": "f663e13b0aefc45e8277d6cdcc3eefe6",
    "action_by": "USRMASTER_919655653929",
    "action_on": [
        {
            "type": "admin"
        }
    ],
    "action_text": "MADE_ADMIN",
    "pending_docs": 17
}
*/
    }

    public static Uri createOrUpdateContactFromJSON(
            JSONObject jsonObject, Context context) throws
            JSONException {
        ContactItem contactItem = new ContactItem();
        fillContactsFromJSON(jsonObject, contactItem);
        return savesContactThreadInDB(contactItem, context.getApplicationContext());
/*
{
    "status": 1,
    "err": "",
    "info": {
        "_id": "e88d83fc4da9d4849fe72b7c99247219",
        "_rev": "2-9bdd276f021d0f3e35495874e14ce6be",
        "doc_type": "GROUP",
        "doc_owner": "intellibitz",
        "timestamp": 1463739453,
        "name": "Intellibitz Dev",
        "profile_pic": "",
        "users": [
            {
                "uid": "USRMASTER_919600037000",
                "type": "superadmin",
                "name": "Jeff",
                "mobile": "919600037000"
            },
            {
                "uid": "USRMASTER_919655653929",
                "type": "user",
                "name": "Nish",
                "mobile": "919655653929"
            },
            {
                "uid": "USRMASTER_919840348914",
                "type": "user",
                "name": "Muthu Ramadoss",
                "mobile": "919840348914"
            }
        ]
    }
}
         */
    }

    public static Uri updateGroupDetailsInDBFromJSON(JSONObject jsonObject, ContactItem contactItem,
                                                     Context context) throws
            JSONException {
        JSONObject info = jsonObject.getJSONObject("info");
        fillContactsFromJSON(info, contactItem);
        return savesContactThreadInDB(contactItem, context.getApplicationContext());
/*
{
    "status": 1,
    "err": "",
    "info": {
        "_id": "e88d83fc4da9d4849fe72b7c99247219",
        "_rev": "2-9bdd276f021d0f3e35495874e14ce6be",
        "doc_type": "GROUP",
        "doc_owner": "intellibitz",
        "timestamp": 1463739453,
        "name": "Intellibitz Dev",
        "profile_pic": "",
        "users": [
            {
                "uid": "USRMASTER_919600037000",
                "type": "superadmin",
                "name": "Jeff",
                "mobile": "919600037000"
            },
            {
                "uid": "USRMASTER_919655653929",
                "type": "user",
                "name": "Nish",
                "mobile": "919655653929"
            },
            {
                "uid": "USRMASTER_919840348914",
                "type": "user",
                "name": "Muthu Ramadoss",
                "mobile": "919840348914"
            }
        ]
    }
}
         */
    }

    public static ContactItem fillAddedUsersInfoFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws JSONException {
        return fillsContactThreadInfoFromJSON(jsonObject, contactItem);
//        created group and added users follow similar json payload..
//        action_on will be filled in added users, emtpy in created groups.
/*
// CREATED_GROUP
RcvDocService Listening on 'rcv_doc'.....[{"_id":"f96cf8979354dfc939201c918b4210dd","_rev":"1-166f0db68706396b223a2bbe377d009e","doc_type":"GROUP-INFO","doc_owner":"USRMASTER_919840348914","timestamp":1465408686,"group_id":"f96cf8979354dfc939201c918b3af720","action_by":"USRMASTER_919840348914","action_on":[],"action_text":"CREATED_GROUP","pending_docs":2}]
Group Info: {"_id":"f96cf8979354dfc939201c918b4210dd","_rev":"1-166f0db68706396b223a2bbe377d009e","doc_type":"GROUP-INFO","doc_owner":"USRMASTER_919840348914","timestamp":1465408686,"group_id":"f96cf8979354dfc939201c918b3af720","action_by":"USRMASTER_919840348914","action_on":[],"action_text":"CREATED_GROUP","pending_docs":2}

// ADDED_USERS
RcvDocService Listening on 'rcv_doc'.....[{"_id":"f96cf8979354dfc939201c918b59e866","_rev":"1-8345c6b9e5d15969431f53c143f219c5","doc_type":"GROUP-INFO","doc_owner":"USRMASTER_919840348914","timestamp":1465408687,"group_id":"f96cf8979354dfc939201c918b3af720","action_by":"USRMASTER_919840348914","action_on":[{"uid":"USRMASTER_919840348914","type":"superadmin","name":"Muthu Ramadoss","mobile":"919840348914"},{"uid":"USRMASTER_919600037000","type":"user","name":"Jeff","mobile":"919600037000"},{"uid":"USRMASTER_919655653929","type":"user","name":"Nish","mobile":"919655653929"}],"action_text":"ADDED_USERS","pending_docs":1}]
Group Info: {"_id":"f96cf8979354dfc939201c918b59e866","_rev":"1-8345c6b9e5d15969431f53c143f219c5","doc_type":"GROUP-INFO","doc_owner":"USRMASTER_919840348914","timestamp":1465408687,"group_id":"f96cf8979354dfc939201c918b3af720","action_by":"USRMASTER_919840348914","action_on":[{"uid":"USRMASTER_919840348914","type":"superadmin","name":"Muthu Ramadoss","mobile":"919840348914"},{"uid":"USRMASTER_919600037000","type":"user","name":"Jeff","mobile":"919600037000"},{"uid":"USRMASTER_919655653929","type":"user","name":"Nish","mobile":"919655653929"}],"action_text":"ADDED_USERS","pending_docs":1}

 */
    }

    public static ContactItem fillCreatedGroupInfoFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws JSONException {
        return fillsContactThreadInfoFromJSON(jsonObject, contactItem);
    }

    public static ContactItem fillRemovedUsersInfoFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws JSONException {
        return fillsContactThreadInfoFromJSON(jsonObject, contactItem);
    }

    public static ContactItem fillMadeAdminInfoFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws JSONException {
        return fillsContactThreadInfoFromJSON(jsonObject, contactItem);
    }

    public static ContactItem fillMadeUserInfoFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws JSONException {
        return fillsContactThreadInfoFromJSON(jsonObject, contactItem);
    }

    public static ContactItem fillLeftGroupInfoFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws JSONException {
        return fillsContactThreadInfoFromJSON(jsonObject, contactItem);
    }

    public static ContactItem fillsContactThreadInfoFromJSON(
            JSONObject jsonObject, ContactItem contactThreadItem) throws JSONException {
        String actionText = jsonObject.getString("action_text");
        String _id = jsonObject.getString("_id");
        contactThreadItem.setDataId(jsonObject.getString("group_id"));
        contactThreadItem.setIntellibitzId(jsonObject.getString("group_id"));
        contactThreadItem.setTypeId(jsonObject.getString("group_id"));
        contactThreadItem.setGroupId(jsonObject.getString("group_id"));
        contactThreadItem.setEmailItem(false);
        contactThreadItem.setGroup(true);
        String group_name = jsonObject.optString("group_name");
        if (!TextUtils.isEmpty(group_name))
            contactThreadItem.setName(group_name);

//        adds the info, to the parent
//        // TODO: 10-06-2016
//        to save the info item, in the db.. with a 1..N with msgthreadcontact
        BaseItem infoItem = new BaseItem(_id, contactThreadItem.getDataId(), "infoitem", actionText);
        contactThreadItem.getInfoItems().add(infoItem);

        contactThreadItem.setDataRev(jsonObject.optString("_rev"));
        String doc_type = jsonObject.optString("doc_type");
        if (!TextUtils.isEmpty(doc_type)) {
            contactThreadItem.setBaseType(doc_type);
            contactThreadItem.setDocType(doc_type);
        }
        String doc_owner = jsonObject.optString("doc_owner");
        if (!TextUtils.isEmpty(doc_owner)) {
            contactThreadItem.setDocOwner(doc_owner);
        }
        long timestamp = jsonObject.optLong("timestamp");
        if (timestamp > 0) {
            contactThreadItem.setTimestamp(timestamp);
        }
//        action_by is done by one of the group members.. do not add to the group (redundant)
//        action_on will include all group members
//        action_by can used to highlight member actions in the group.. add, remove, change pic etc.,
        String actionBy = jsonObject.optString("action_by");
        if (actionBy != null && !actionBy.isEmpty()) {
            // TODO: 09-06-2016
//            add this information to the group.. who done what
//                    // TODO: 20-06-2016
//                    to fetch mobile
//            MobileItem mobileItem = new MobileItem();
//            // TODO: 30-06-2016
//            mobileItem.setIntellibitzId(actionBy);
            ContactItem intellibitzContactItem = new ContactItem(actionBy);
            ContactItem contactItem = new ContactItem(intellibitzContactItem);
//            contactItem.setDataId(actionBy);
//            contactItem.setIntellibitzId(actionBy);
//            contactItem.addContact(contactItem);
        }
        JSONArray jsonArray = jsonObject.optJSONArray("action_on");
        if (jsonArray != null && jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String uid = object.optString("uid");
                String type = object.optString("type");
                String name = object.optString("name");
                String mobile = object.optString("mobile");
                if (!TextUtils.isEmpty(uid)) {
//                    MobileItem mobileItem = new MobileItem(uid, mobile, name, type);
//                    // TODO: 30-06-2016
//                    mobileItem.setIntellibitzId(uid);
//                    ContactItem intellibitzContactItem = new ContactItem(uid);
                    ContactItem contactItem = new ContactItem();
                    contactItem.setDataId(uid);
                    contactItem.setIntellibitzId(uid);
                    if (!TextUtils.isEmpty(name))
                        contactItem.setName(name);
                    if (!TextUtils.isEmpty(type))
                        contactItem.setType(type);
                    if (!TextUtils.isEmpty(mobile))
                        contactItem.setTypeId(mobile);
                    contactThreadItem.addContact(contactItem);
                }
            }
        }
        return contactThreadItem;
    }

    public static ContactItem createsContactFromGetGroupsJSON(
            JSONObject jsonObject) throws JSONException {
//        // TODO: 10-06-2016
//        id must be filled, for cloud id
        ContactItem contactItem = new ContactItem();
        contactItem.setDataId(jsonObject.getString("group_id"));
//        contactItem.setGroupId(jsonObject.getString("group_id"));
        contactItem.setName(jsonObject.optString("group_name"));
        return contactItem;
/*
{
    "status": 1,
    "err": "",
    "groups": [
        {
            "group": {
                "_id": "1326067f6fa961e733bfc81852152f36",
                "_rev": "2-459b5fe5a10a9b94ba20ba5edbdc9c5c",
                "doc_type": "GROUP",
                "doc_owner": "intellibitz",
                "timestamp": 1466522127,
                "name": "the first ",
                "profile_pic": "",
                "users": [
                    {
                        "uid": "USRMASTER_919100504678",
                        "type": "superadmin"
                    },
                    {
                        "uid": "USRMASTER_919600037000",
                        "type": "user"
                    },
                    {
                        "uid": "USRMASTER_919655653929",
                        "type": "user"
                    }
                ]
            },
            "group_info": [
                {
                    "_id": "1326067f6fa961e733bfc8185215c8fd",
                    "_rev": "1-f887810504d50f7556b224ad8dcb4e56",
                    "doc_type": "GROUP-INFO",
                    "doc_owner": "USRMASTER_919100504678",
                    "timestamp": 1466522127,
                    "group_id": "1326067f6fa961e733bfc81852152f36",
                    "group_name": "the first ",
                    "profile_pic": "",
                    "action_by": "USRMASTER_919100504678",
                    "action_on": [],
                    "action_text": "CREATED_GROUP"
                },
                {
                    "_id": "1326067f6fa961e733bfc818521752b9",
                    "_rev": "1-ecea3c809a7aaacdb196ba60ea862d2c",
                    "doc_type": "GROUP-INFO",
                    "doc_owner": "USRMASTER_919100504678",
                    "timestamp": 1466522127,
                    "group_id": "1326067f6fa961e733bfc81852152f36",
                    "group_name": "the first ",
                    "profile_pic": "",
                    "action_by": "USRMASTER_919100504678",
                    "action_on": [
                        {
                            "uid": "USRMASTER_919100504678",
                            "type": "superadmin",
                            "name": "Android KitKat nishy ",
                            "mobile": "919100504678"
                        },
                        {
                            "uid": "USRMASTER_919600037000",
                            "type": "user",
                            "name": "Jeffrey",
                            "mobile": "919600037000"
                        },
                        {
                            "uid": "USRMASTER_919655653929",
                            "type": "user",
                            "name": "Nishu",
                            "mobile": "919655653929"
                        }
                    ],
                    "action_text": "ADDED_USERS"
                },
                {
                    "_id": "1326067f6fa961e733bfc81852175a6d",
                    "_rev": "1-ab77810ee557163e57e5f110e728aa68",
                    "doc_type": "GROUP-INFO",
                    "doc_owner": "USRMASTER_919600037000",
                    "timestamp": 1466522127,
                    "group_id": "1326067f6fa961e733bfc81852152f36",
                    "group_name": "the first ",
                    "profile_pic": "",
                    "action_by": "USRMASTER_919100504678",
                    "action_on": [
                        {
                            "uid": "USRMASTER_919100504678",
                            "type": "superadmin",
                            "name": "Android KitKat nishy ",
                            "mobile": "919100504678"
                        },
                        {
                            "uid": "USRMASTER_919600037000",
                            "type": "user",
                            "name": "Jeffrey",
                            "mobile": "919600037000"
                        },
                        {
                            "uid": "USRMASTER_919655653929",
                            "type": "user",
                            "name": "Nishu",
                            "mobile": "919655653929"
                        }
                    ],
                    "action_text": "ADDED_USERS"
                }
            ]
        },
        {
            "group": {
                "_id": "1326067f6fa961e733bfc81852247d73",
                "_rev": "2-5148567df11f8fc6decffbc3567707d4",
                "doc_type": "GROUP",
                "doc_owner": "intellibitz",
                "timestamp": 1466522184,
                "name": "the only one ",
                "profile_pic": "",
                "users": [
                    {
                        "uid": "USRMASTER_919100504678",
                        "type": "superadmin"
                    },
                    {
                        "uid": "USRMASTER_919600037000",
                        "type": "user"
                    },
                    {
                        "uid": "USRMASTER_919655653929",
                        "type": "user"
                    }
                ]
            },
            "group_info": [
                {
                    "_id": "1326067f6fa961e733bfc8185225ac07",
                    "_rev": "1-cf7de88b27ca202c82d66343196f1212",
                    "doc_type": "GROUP-INFO",
                    "doc_owner": "USRMASTER_919100504678",
                    "timestamp": 1466522184,
                    "group_id": "1326067f6fa961e733bfc81852247d73",
                    "group_name": "the only one ",
                    "profile_pic": "",
                    "action_by": "USRMASTER_919100504678",
                    "action_on": [],
                    "action_text": "CREATED_GROUP"
                },
                {
                    "_id": "1326067f6fa961e733bfc81852275105",
                    "_rev": "1-befbac13acd3bc05b739a17412b69108",
                    "doc_type": "GROUP-INFO",
                    "doc_owner": "USRMASTER_919100504678",
                    "timestamp": 1466522184,
                    "group_id": "1326067f6fa961e733bfc81852247d73",
                    "group_name": "the only one ",
                    "profile_pic": "",
                    "action_by": "USRMASTER_919100504678",
                    "action_on": [
                        {
                            "uid": "USRMASTER_919100504678",
                            "type": "superadmin",
                            "name": "Android KitKat nishy ",
                            "mobile": "919100504678"
                        },
                        {
                            "uid": "USRMASTER_919600037000",
                            "type": "user",
                            "name": "Jeffrey",
                            "mobile": "919600037000"
                        },
                        {
                            "uid": "USRMASTER_919655653929",
                            "type": "user",
                            "name": "Nishu",
                            "mobile": "919655653929"
                        }
                    ],
                    "action_text": "ADDED_USERS"
                },
                {
                    "_id": "1326067f6fa961e733bfc81852275cbe",
                    "_rev": "1-b643ad3da525c3261ac14c6976bdf2fb",
                    "doc_type": "GROUP-INFO",
                    "doc_owner": "USRMASTER_919600037000",
                    "timestamp": 1466522184,
                    "group_id": "1326067f6fa961e733bfc81852247d73",
                    "group_name": "the only one ",
                    "profile_pic": "",
                    "action_by": "USRMASTER_919100504678",
                    "action_on": [
                        {
                            "uid": "USRMASTER_919100504678",
                            "type": "superadmin",
                            "name": "Android KitKat nishy ",
                            "mobile": "919100504678"
                        },
                        {
                            "uid": "USRMASTER_919600037000",
                            "type": "user",
                            "name": "Jeffrey",
                            "mobile": "919600037000"
                        },
                        {
                            "uid": "USRMASTER_919655653929",
                            "type": "user",
                            "name": "Nishu",
                            "mobile": "919655653929"
                        }
                    ],
                    "action_text": "ADDED_USERS"
                }
            ]
        }
    ]
}


// GET-GROUPS
{
    "status": 1,
    "err": "",
    "groups": [
        {
            "group_name": "Intellibitz Dev",
            "group_id": "e88d83fc4da9d4849fe72b7c99247219"
        },
        {
            "group_name": "fjush",
            "group_id": "f663e13b0aefc45e8277d6cdcc3eefe6"
        }
    ]
}
         */
    }

    public static ContactItem fillGroupFromJSON(
            JSONObject jsonObject, ContactItem contactItem) throws JSONException {
        return fillContactsFromJSON(jsonObject, contactItem);

/*
{
    "status": 1,
    "err": "",
    "info": {
        "_id": "e88d83fc4da9d4849fe72b7c99247219",
        "_rev": "2-9bdd276f021d0f3e35495874e14ce6be",
        "doc_type": "GROUP",
        "doc_owner": "intellibitz",
        "timestamp": 1463739453,
        "name": "Intellibitz Dev",
        "profile_pic": "",
        "users": [
            {
                "uid": "USRMASTER_919600037000",
                "type": "superadmin",
                "name": "Jeff",
                "mobile": "919600037000"
            },
            {
                "uid": "USRMASTER_919655653929",
                "type": "user",
                "name": "Nish",
                "mobile": "919655653929"
            },
            {
                "uid": "USRMASTER_919840348914",
                "type": "user",
                "name": "Muthu Ramadoss",
                "mobile": "919840348914"
            }
        ]
    }
}
         */
    }

    public static ContactItem createsContactFromJSON(JSONObject jsonObject) throws
            JSONException {
        ContactItem contactItem = new ContactItem();
        return fillContactsFromJSON(jsonObject, contactItem);
    }

    public static ContactItem fillContactsFromJSON(
            JSONObject jsonObject, ContactItem contactThreadItem) throws JSONException {
        contactThreadItem.setDataId(jsonObject.optString("_id"));
        contactThreadItem.setDataRev(jsonObject.optString("_rev"));
        contactThreadItem.setName(jsonObject.optString("name"));
        contactThreadItem.setProfilePic(jsonObject.optString("profile_pic"));
        contactThreadItem.setBaseType(jsonObject.optString("doc_type"));
        contactThreadItem.setDocType(jsonObject.optString("doc_type"));
        contactThreadItem.setDocOwner(jsonObject.optString("doc_owner"));
        contactThreadItem.setTimestamp(jsonObject.optLong("timestamp"));
        JSONArray jsonArray = jsonObject.optJSONArray("users");
        if (jsonArray != null && jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.optJSONObject(i);
                String intellibitzId = object.optString("uid");
                String type = object.optString("type");
                String name = object.optString("name");
                String mobile = object.optString("mobile");
//                    // TODO: 20-06-2016
//                    to fetch mobile
                if (mobile != null && !mobile.isEmpty()) {
//                    MobileItem mobileItem = new MobileItem(intellibitzId, mobile, name, type);
//                    // TODO: 30-06-2016
//                    mobileItem.setIntellibitzId(intellibitzId);
//                    ContactItem intellibitzContactItem = new ContactItem(intellibitzId);
                    ContactItem contactItem = new ContactItem();
                    contactItem.setIntellibitzId(intellibitzId);
                    contactItem.setDataId(contactItem.getIntellibitzId());
                    contactItem.setType(type);
                    contactItem.setName(name);
                    contactItem.setTypeId(mobile);
                    contactThreadItem.addContact(contactItem);
                }
            }
        }
        return contactThreadItem;
/*
{
    "status": 1,
    "err": "",
    "info": {
        "_id": "e88d83fc4da9d4849fe72b7c99247219",
        "_rev": "2-9bdd276f021d0f3e35495874e14ce6be",
        "doc_type": "GROUP",
        "doc_owner": "intellibitz",
        "timestamp": 1463739453,
        "name": "Intellibitz Dev",
        "profile_pic": "",
        "users": [
            {
                "uid": "USRMASTER_919600037000",
                "type": "superadmin",
                "name": "Jeff",
                "mobile": "919600037000"
            },
            {
                "uid": "USRMASTER_919655653929",
                "type": "user",
                "name": "Nish",
                "mobile": "919655653929"
            },
            {
                "uid": "USRMASTER_919840348914",
                "type": "user",
                "name": "Muthu Ramadoss",
                "mobile": "919840348914"
            }
        ]
    }
}
         */
    }

    public static ContentValues fillContentValuesFromContactThreadItem(
            ContactItem contactItem, ContentValues values) {
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATA_ID, contactItem.getDataId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DEVICE_CONTACTID, contactItem.getDeviceContactId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TYPE_ID, contactItem.getTypeId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_INTELLIBITZ_ID, contactItem.getIntellibitzId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_GROUP_ID, contactItem.getGroupId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TYPE_ID, contactItem.getTypeId());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_NAME, contactItem.getName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_FIRST_NAME, contactItem.getFirstName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_LAST_NAME, contactItem.getLastName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DISPLAY_NAME, contactItem.getDisplayName());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATA_REV, contactItem.getDataRev());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DEVICE_REF, contactItem.getDeviceRef());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DOC_TYPE, contactItem.getDocType());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_BASE_TYPE, contactItem.getBaseType());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DOC_OWNER, contactItem.getDocOwner());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TYPE, contactItem.getType());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_STATUS, contactItem.getStatus());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_PIC, contactItem.getProfilePic());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_GROUP, contactItem.isGroup());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_IS_EMAIL, contactItem.isEmailItem());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_TIMESTAMP, contactItem.getTimestamp());
        MainApplicationSingleton.fillIfNotNull(values,
                ContactItemColumns.KEY_DATETIME,
                MainApplicationSingleton.getDateTimeMillis(contactItem.getTimestamp()));
        return values;
    }

    public static Uri saveContactItemToDB(ContactItem deviceContactItem, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.DEVICE_CONTACT, MainApplicationSingleton.Serializer.serialize(deviceContactItem));
//            inserts a single item, so invoke with the content item uri.. not the dir uri
            return context.getContentResolver().insert(
                    Uri.withAppendedPath(CONTENT_URI, "0"), contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri savesContactThreadInDB(ContactItem contact, Context context) {
        Collection<ContactItem> contacts =
                Collections.synchronizedSet(new HashSet<ContactItem>(1));
        contacts.add(contact);
        return savesContactThreadsInDB(contacts, context);
    }

    public static Uri savesContactThreadsInDB(
            Collection<ContactItem> contacts, Context context) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactItem.TAG,
                    MainApplicationSingleton.Serializer.serialize(contacts));
            return context.getContentResolver().insert(CONTENT_URI, contentValues);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int updatesContactThreadsInDB(
            Collection<ContactItem> contactItems, Context context) throws
            IOException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactItem.TAG,
                MainApplicationSingleton.Serializer.serialize(contactItems));
        return context.getContentResolver().update(CONTENT_URI, contentValues, null, null);
    }

    public static int deleteContact(
            ContactItem contactItem, ContactItem contactThreadItem, Context context) {
        String[] selectionArgs = new String[2];
        long _id = contactItem.get_id();
        long id = contactThreadItem.get_id();
//        requires valid ids
        if (0 == _id || 0 == id) return 0;
        selectionArgs[0] = String.valueOf(_id);
        selectionArgs[1] = String.valueOf(id);

        return context.getContentResolver().delete(Uri.withAppendedPath(JOIN_CONTENT_URI, "delete"),
                null, selectionArgs);
    }

    public static Uri deleteContact(ContactItem contactThreadItem, Context context) {
        String[] selectionArgs = new String[2];
        HashSet<ContactItem> contactItems = contactThreadItem.getContactItems();
        if (null == contactItems || contactItems.isEmpty()) return null;
        ContactItem contactItem = contactItems.iterator().next();
        if (null == contactItem || null == contactItem.getDataId()) return null;
        long _id = contactItem.get_id();
        if (0 == _id) {
            MsgEmailContactContentProvider.queryContactForDBId(contactItem, context);
        }
        long id = contactThreadItem.get_id();
//        requires valid ids
        if (0 == _id || 0 == id) return null;
        selectionArgs[0] = String.valueOf(_id);
        selectionArgs[1] = String.valueOf(id);

        Uri uri = Uri.withAppendedPath(JOIN_CONTENT_URI, "delete/" + _id);
        int row = context.getContentResolver().delete(uri,
                null, selectionArgs);
        if (0 == row) return null;
        return uri;
    }

    public static Cursor getContactJoin(DatabaseHelper databaseHelper,
                                        String selection, String[] selectionArgs, String sortOrder) {
        String selectQuery =
                "SELECT  " +
//                        " gc._id as gc_id, " +
//                        "gc.id as gcid, gc.name as gcname," +
//                        " gc.profile_pic as gcpic, gc.type as gctype, " +
//                        " ak._id as ak_id, ak.id as akid, ak.name as akname, " +
//                        " ak.profile_pic as akpic, ak.type as aktype, " +
//                        " ak.intellibitz_id as akonid, ak.status as akstatus, " +
                        " gc." + ContactItemColumns.KEY_ID +
                        " as gc_id, " +
                        " gc." + ContactItemColumns.KEY_DEVICE_CONTACTID +
                        " as gcdcid, " +
                        " gc." + ContactItemColumns.KEY_DATA_ID + " as gcid, " +
                        " gc." + ContactItemColumns.KEY_NAME + " as gcname, " +
                        " gc." + ContactItemColumns.KEY_TYPE_ID + " as gctypeid, " +
                        " gc." + ContactItemColumns.KEY_FIRST_NAME + " as gcfname, " +
                        " gc." + ContactItemColumns.KEY_LAST_NAME + " as gclname, " +
                        " gc." + ContactItemColumns.KEY_DISPLAY_NAME + " as gcdname, " +
                        " gc." + ContactItemColumns.KEY_STATUS + " as gcstat, " +
                        " gc." + ContactItemColumns.KEY_PIC + " as gcpic, " +
                        " gc." + ContactItemColumns.KEY_TYPE + " as gctype, " +
                        " gc." + ContactItemColumns.KEY_DOC_OWNER + " as gcdo, " +
                        " gc." + ContactItemColumns.KEY_IS_GROUP + " as gcisg, " +
                        " gc." + ContactItemColumns.KEY_IS_EMAIL + " as gcise, " +
                        " ak." + ContactItemColumns.KEY_ID + " as ak_id," +
                        " ak." + ContactItemColumns.KEY_DATA_ID + " as akid," +
                        " ak." + ContactItemColumns.KEY_TYPE_ID + " as aktypeid," +
                        " ak." + ContactItemColumns.KEY_NAME + " as akname," +
                        " ak." + ContactItemColumns.KEY_PIC + " as akpic," +
                        " ak." + ContactItemColumns.KEY_TYPE + " as aktype," +
                        " ak." + ContactItemColumns.KEY_INTELLIBITZ_ID + " as akonid," +
                        " ak." + ContactItemColumns.KEY_IS_ANONYMOUS + " as akisa," +
                        " ak." + ContactItemColumns.KEY_IS_CLOUD + " as akisc," +
                        " ak." + ContactItemColumns.KEY_IS_DEVICE + " as akisd," +
                        " ak." + ContactItemColumns.KEY_IS_EMAIL + " as akise," +
                        " ak." + ContactItemColumns.KEY_IS_GROUP + " as akisg," +
                        " ak." + ContactItemColumns.KEY_STATUS + " as akstatus," +
                        " ak." + ContactItemColumns.KEY_TIMESTAMP + " as aktime" +
                        "  FROM  " +
                        TABLE_MSGEMAILCONTACTS + " gc " +
                        "left outer join " + TABLE_MSGEMAILCONTACTS_CONTACT_JOIN +
                        " gcak on gc.[" + ContactItemColumns.KEY_ID +
                        "] = gcak.[" + ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID +
                        "] " +
                        "left outer join " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT +
                        " ak on ak.[" + ContactItemColumns.KEY_ID +
                        "] = gcak.[" + ContactsContactJoinColumns.KEY_CONTACT_ID +
                        "] ";
        if (selection != null && !selection.isEmpty()) {
            selectQuery += " WHERE " + selection;
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            selectQuery += " ORDER BY " + sortOrder;
        }
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, selectionArgs);
    }

    public static long createOrUpdateContactsContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item, long id) {
//        checks if the same email component is already present for the message thread
//        Cursor cursor = getAKContactCursorJoin(db, item, id);
        Cursor cursor = databaseHelper.query(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT,
//                projection
                new String[]{ContactItemColumns.KEY_ID},
//                selection (email contacts uniquely identified by email and type)
                ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? AND " +
                        ContactItemColumns.KEY_TYPE + " = ? ",
//                selection args
                new String[]{item.getIntellibitzId(), item.getType()},
                null);
        long _id;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            ContentValues values = new ContentValues();
//        message thread is the parent, email item child
            MsgEmailContactContentProvider.fillContentValuesFromContactItem(item, values);
            _id = databaseHelper.insert(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT, null, values);
            item.set_id(_id);
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
//            // TODO: 12-07-2016
//            delays the update, after the intellibitz contacts join
        }
//        creates the join
        createOrUpdateContactsContactJoin(databaseHelper, db, _id, id);
//        creates or updates intellibitz contact
//        createOrUpdateIntellibitzContactDeviceContactJoin(db, item.getIntellibitzContactItem());
//        creates the join
//        // TODO: 01-07-2016
        MsgEmailContactContentProvider.createOrUpdateContactIntellibitzContactByContactJoin(
                databaseHelper, db, item, item.get_id());

//        // TODO: 12-07-2016
//        the contacts, would have the device information from intellibitz contacts
//        updates here
        ContentValues values = new ContentValues();
//        message thread is the parent, email item child
        MsgEmailContactContentProvider.fillContentValuesFromContactItem(item, values);
        databaseHelper.update(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT, values,
                ContactItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(_id)});
        return item.get_id();
    }

    public static long updateContactThreadContact_(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item, long id) {
        ContentValues values = new ContentValues();
        MsgEmailContactContentProvider.fillContentValuesFromContactItem(item, values);
//        Cursor cursor = getAllGroupContactAKContactsCursor(item, id);
//        Cursor cursor =
        long _id = updateContactThreadContact(databaseHelper, db, item, id);
/*
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            _id = insert(db, TABLE_INTELLIBITZCONTACT, null, values);
            item.set_id(_id);
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
            item.set_id(_id);
            cursor.close();
            update(db, TABLE_INTELLIBITZCONTACT, values, ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
*/
//        creates the join
        createOrUpdateContactsContactJoin(databaseHelper, db, _id, id);
        return item.get_id();
    }

    public static long updateContactThreadContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item, long id) {
        ContentValues values = new ContentValues();
//        message thread is the parent, email item child
        MsgEmailContactContentProvider.fillContentValuesFromContactItem(item, values);
//        checks if the same email component is already present for the message thread
//        Cursor cursor = getAKContactCursorJoin(db, item, id);
        Cursor cursor = databaseHelper.query(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT,
//                projection
                new String[]{ContactItemColumns.KEY_ID},
//                selection
                ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
//                selection args
                new String[]{item.getIntellibitzId()},
                null);
        long _id;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            _id = databaseHelper.insert(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT, null, values);
            item.set_id(_id);
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
//            updateMessageThreadEmail(item, id);
            databaseHelper.update(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT, values, ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
//        creates the join
        createOrUpdateContactsContactJoin(databaseHelper, db, _id, id);
/*
        MobileItem selectedMobile = item.getMobileItem();
        if (selectedMobile != null)
            createOrUpdateIntellibitzContactMobileJoin(db, selectedMobile, item.get_id());
        EmailItem selectedEmail = item.getEmailItem();
        if (selectedEmail != null)
            createOrUpdateIntellibitzContactEmailJoin(db, selectedEmail, item.get_id());
*/
        return item.get_id();
    }

    public static long deleteContactThreadContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item, long id) {
        Cursor cursor = databaseHelper.query(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT,
//                projection
                new String[]{ContactItemColumns.KEY_ID},
//                selection
                ContactItemColumns.KEY_DATA_ID + " = ? ",
//                selection args
                new String[]{item.getDataId()},
                null);
        long _id;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            item not found in db, cannot delete.. returns 0
            return 0;
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
//        deletes the join
            deleteContactThreadContactJoin(databaseHelper, db, item.get_id(), id);
//            deletes the item
            _id = databaseHelper.delete(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT,
                    ContactItemColumns.KEY_ID + " = ? ",
                    new String[]{String.valueOf(item.get_id())});
        }
        return _id;
    }

    public static long deleteContactThreadContact(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long _id, long id) {
//        deletes the join
        long row = deleteContactThreadContactJoin(databaseHelper, db, _id, id);
        if (0 == row) return 0;
//            deletes the item
        return databaseHelper.delete(db, MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT,
                ContactItemColumns.KEY_ID + " = ? ",
                new String[]{String.valueOf(_id)});
    }

    public static long deleteContactThreadContact(DatabaseHelper databaseHelper,
                                                  ContactItem contactItem, ContactItem contactThreadItem) {
        long rows = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rows = deleteContactThreadContact(databaseHelper, db, contactItem, contactThreadItem.get_id());
            if (0 == rows) {
                Log.e(TAG, "Failed to delete item: " + contactItem);
                throw new SQLException("Failed to insert row into " + contactItem);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return rows;
    }

    public static long deleteContactThreadContact(DatabaseHelper databaseHelper, long id, long fk) {
        long rows = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            rows = deleteContactThreadContact(databaseHelper, db, id, fk);
            if (0 == rows) {
                Log.e(TAG, "Failed to delete item: " + id);
                throw new SQLException("Failed to insert row into " + id);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return rows;
    }

    public static long[] createOrUpdateContactsContacts(DatabaseHelper databaseHelper,
                                                        SQLiteDatabase db, Set<ContactItem> items, long id) {
        long[] ids = new long[items.size()];
        int i = 0;
        ContactItem[] array = items.toArray(new ContactItem[0]);
        for (ContactItem item : array) {
            long l = createOrUpdateContactsContact(databaseHelper, db, item, id);
            if (0 == l) {
                Log.e(TAG, "Failed to insert row: " + item);
                throw new SQLException("Failed to insert row into " + item);
            }
            ids[i++] = l;
        }
        items.addAll(Arrays.asList(array));
        return ids;
    }

    public static long[] updateContactThreadContacts(
            DatabaseHelper databaseHelper, SQLiteDatabase db, Set<ContactItem> items, long id) {
        long[] ids = new long[items.size()];
        int i = 0;
        for (ContactItem item : items) {
            long l = updateContactThreadContact(databaseHelper, db, item, id);
            if (0 == l) {
                Log.e(TAG, "Failed to insert row: " + item);
                throw new SQLException("Failed to insert row into " + item);
            }
            ids[i++] = l;
        }
        return ids;
    }

    public static long createOrUpdateContacts(
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem contactItem) {
//        checks if GroupContact already exists
        Cursor cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
//                projection
                new String[]{ContactItemColumns.KEY_ID},
//                selection
                ContactItemColumns.KEY_DATA_ID + " = ? ",
//                selection args
                new String[]{contactItem.getDataId()},
                null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ContactItemColumns.KEY_ID},
                    ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{contactItem.getIntellibitzId()},
                    null);
        }
//        does not exist.. insert
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = databaseHelper.insert(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS, null,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()));
            contactItem.set_id(_id);
//            exists.. update
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            contactItem.set_id(_id);
            cursor.close();
            databaseHelper.update(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()),
                    ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(contactItem.get_id())});
        }
/*
        HashSet<ContactItem> contacts = item.getContactItems();
        if (contacts != null && !contacts.isEmpty())
            createOrUpdateGroupContactsAKContactsJoin(db, contacts, item.get_id());
*/
//        createOrUpdateGroupContactAKContacts(db, item.getContactItems(), item.get_id());
        createOrUpdateContactsContacts(databaseHelper, db,
                contactItem.getContactItems(), contactItem.get_id());
        return contactItem.get_id();
    }

    public static long createOrUpdateContactsContactJoin(DatabaseHelper databaseHelper,
                                                         SQLiteDatabase db, long id, long fk) {
        long _id = getContactThreadContactJoin(databaseHelper, db, id, fk);
        ContentValues values = new ContentValues();
        values.put(ContactsContactJoinColumns.KEY_CONTACT_ID, id);
        values.put(ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID, fk);
        values.put(ContactsContactJoinColumns.KEY_TIMESTAMP,
                MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db, TABLE_MSGEMAILCONTACTS_CONTACT_JOIN, null, values);
        } else {
            _id = databaseHelper.update(db,
                    TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
                    values,
                    ContactsContactJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long deleteContactThreadContactJoin(DatabaseHelper databaseHelper,
                                                      SQLiteDatabase db, long id, long fk) {
        long _id = getContactThreadContactJoin(databaseHelper, db, id, fk);
        if (0 == _id) {
            return 0;
        } else {
            _id = databaseHelper.delete(db,
                    TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
                    ContactsContactJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long getContactThreadContactJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(db, TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
                new String[]{ContactsContactJoinColumns.KEY_ID},
                ContactsContactJoinColumns.KEY_CONTACTTHREAD_ID + " = ? and " +
                        ContactsContactJoinColumns.KEY_CONTACT_ID + " = ?",
                new String[]{String.valueOf(fk),
                        String.valueOf(id)}, null, null, null);
        if (c != null && c.getCount() > 0) {
            _id = c.getLong(c.getColumnIndex("_id"));
            c.close();
        }
        return _id;
    }

    public static long createOrUpdateMessagesContacts(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            ContactItem contactItem) {
//        checks if GroupContact already exists
/*
        Cursor cursor = query(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS,
//                projection
                new String[]{ContactsContentProvider.ContactItemColumns.KEY_ID},
//                selection
                ContactsContentProvider.ContactItemColumns.KEY_DATA_ID + " = ? ",
//                selection args
                new String[]{contactItem.getDataId()},
                null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            cursor = query(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ContactsContentProvider.ContactItemColumns.KEY_ID},
                    ContactsContentProvider.ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{contactItem.getIntellibitzId()},
                    null);
        }
//        does not exist.. insert
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = insert(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS, null,
                    ContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()));
            contactItem.set_id(_id);
//            exists.. update
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    ContactsContentProvider.ContactItemColumns.KEY_ID));
            contactItem.set_id(_id);
            cursor.close();
            update(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    ContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()),
                    ContactsContentProvider.ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(contactItem.get_id())});
        }
*/
/*
        HashSet<ContactItem> contacts = item.getContactItems();
        if (contacts != null && !contacts.isEmpty())
            createOrUpdateGroupContactsAKContactsJoin(db, contacts, item.get_id());
*//*

//        createOrUpdateGroupContactAKContacts(db, item.getContactItems(), item.get_id());
        createOrUpdateContactsContacts(db,
                contactItem.getContactItems(), contactItem.get_id());
*/

        long _id = createOrUpdateContacts(databaseHelper, db, contactItem);
        if (0 == _id) return 0;

//        if message thread exists, join
//        checks group contact, msg thread join first
        Cursor cursor = databaseHelper.query(db, TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
                new String[]{MessagesContactsJoinColumns.KEY_MSGTHREAD_ID},
                ContactItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(contactItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            cursor = databaseHelper.query(db, MessageEmailContentProvider.TABLE_MESSAGESEMAIL,
                    new String[]{MessageItemColumns.KEY_ID},
                    MessageItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(contactItem.getDataId())}, null
            );
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            } else {
//                message thread found, join
                _id = cursor.getLong(cursor.getColumnIndex(
                        MessageItemColumns.KEY_ID));
                cursor.close();
                createOrUpdateMessagesContactsJoin(databaseHelper,
                        db, _id, contactItem.get_id());
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
        Cursor cursor = query(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS,
//                projection
                new String[]{ContactsContentProvider.ContactItemColumns.KEY_ID},
//                selection
                ContactsContentProvider.ContactItemColumns.KEY_DATA_ID + " = ? ",
//                selection args
                new String[]{contactItem.getDataId()},
                null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            cursor = query(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ContactsContentProvider.ContactItemColumns.KEY_ID},
                    ContactsContentProvider.ContactItemColumns.KEY_INTELLIBITZ_ID + " = ? ",
                    new String[]{contactItem.getIntellibitzId()},
                    null);
        }
//        does not exist.. insert
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            long _id = insert(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS, null,
                    ContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()));
            contactItem.set_id(_id);
//            exists.. update
        } else {
            long _id = cursor.getLong(cursor.getColumnIndex(
                    ContactsContentProvider.ContactItemColumns.KEY_ID));
            contactItem.set_id(_id);
            cursor.close();
            update(db, ContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    ContactsContentProvider.fillContentValuesFromContactThreadItem(
                            contactItem, new ContentValues()),
                    ContactsContentProvider.ContactItemColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(contactItem.get_id())});
        }
*/
/*
        HashSet<ContactItem> contacts = item.getContactItems();
        if (contacts != null && !contacts.isEmpty())
            createOrUpdateGroupContactsAKContactsJoin(db, contacts, item.get_id());
*//*

//        createOrUpdateGroupContactAKContacts(db, item.getContactItems(), item.get_id());
        createOrUpdateContactsContacts(db,
                contactItem.getContactItems(), contactItem.get_id());
*/

        long _id = createOrUpdateContacts(databaseHelper, db, contactItem);
        if (0 == _id) return 0;

//        if message thread exists, join
//        checks group contact, msg thread join first
        Cursor cursor = databaseHelper.query(db, TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
                new String[]{MessagesContactsJoinColumns.KEY_MSGTHREAD_ID},
                ContactItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(contactItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            cursor = databaseHelper.query(db, MessageEmailContentProvider.TABLE_MESSAGEEMAIL,
                    new String[]{MessageItemColumns.KEY_ID},
                    MessageItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(contactItem.getDataId())}, null
            );
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            } else {
//                message thread found, join
                _id = cursor.getLong(cursor.getColumnIndex(
                        MessageItemColumns.KEY_ID));
                cursor.close();
                createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, contactItem.get_id());
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

    public static long[] updateContactThreads(DatabaseHelper databaseHelper, Collection<ContactItem> items) {
        long[] ids = new long[items.size()];
        int i = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContactItem[] array = items.toArray(new ContactItem[0]);
            for (ContactItem item : array) {
                long l = updateContactThread(databaseHelper, db, item);
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

    public static long updateContactThread(DatabaseHelper databaseHelper, SQLiteDatabase db,
                                           ContactItem contactItem) {
        databaseHelper.update(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                MsgEmailContactsContentProvider.fillContentValuesFromContactThreadItem(
                        contactItem, new ContentValues()),
                ContactItemColumns.KEY_DATA_ID + " = ?",
                new String[]{String.valueOf(contactItem.getDataId())});
/*
        HashSet<ContactItem> contacts = item.getContactItems();
        if (contacts != null && !contacts.isEmpty())
            createOrUpdateGroupContactsAKContactsJoin(db, contacts, item.get_id());
*/
//        createOrUpdateGroupContactAKContacts(db, item.getContactItems(), item.get_id());
        createOrUpdateContactsContacts(databaseHelper, db, contactItem.getContactItems(),
                contactItem.get_id());
//        if message thread exists, join
//        checks group contact, msg thread join first
        Cursor cursor = databaseHelper.query(db, TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
                new String[]{MessagesContactsJoinColumns.KEY_MSGTHREAD_ID},
                ContactItemColumns.KEY_ID + " = ?",
                new String[]{String.valueOf(contactItem.get_id())}, null
        );
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            cursor = databaseHelper.query(db, MessageEmailContentProvider.TABLE_MESSAGESEMAIL,
                    new String[]{MessageItemColumns.KEY_ID},
                    MessageItemColumns.KEY_DATA_ID + " = ?",
                    new String[]{String.valueOf(contactItem.getDataId())}, null
            );
            if (null == cursor || 0 == cursor.getCount()) {
                if (cursor != null) cursor.close();
//            throws sql execeptions here.. to break transaction
            } else {
//                message thread found, join
                long _id = cursor.getLong(cursor.getColumnIndex(
                        MessageItemColumns.KEY_ID));
                cursor.close();
                createOrUpdateMessagesContactsJoin(databaseHelper,
                        db, _id, contactItem.get_id());
            }
        } else {
//            message thread join exists, ok
            cursor.close();
        }
        return contactItem.get_id();
    }

    public static long createOrUpdateMessageContactsJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db,
            ContactItem item, long id) {
        Cursor cursor = getMessageContactThreadCursorJoin(databaseHelper, item, id);
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ContactItemColumns.KEY_ID},
                    ContactItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{item.getDataId()}, null);
            if (null != cursor && 0 != cursor.getCount()) {
                _id = cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
        }
        if (_id != 0) {
//        creates the join
            createOrUpdateMessagesContactsJoin(databaseHelper, db, _id, id);
        }
        return item.get_id();
    }

    public static Cursor getMessageContactThreadCursorJoin(
            DatabaseHelper databaseHelper, ContactItem item, long id) {
        String[] args = new String[]{item.getDataId(), String.valueOf(id)};
        String selectQuery = "SELECT  * FROM " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL + " nt " +
                " left join " + TABLE_MSGEMAILCONTACTS_CONTACT_JOIN +
                " ntm on nt.[" + ContactItemColumns.KEY_ID +
                "] = ntm.[" + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + "]  " +
                " left join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS + " mt on ntm.[" +
                MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID +
                "] = mt.[" + ContactItemColumns.KEY_ID + "] " +
                " WHERE " + " mt." + ContactItemColumns.KEY_ID + " = ? " +
                " AND nt." + MessageItemColumns.KEY_ID + " = ? ";
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, args);
    }

    public static long createOrUpdateMessagesContactsJoin(
            DatabaseHelper databaseHelper, ContactItem item, long id) {
        Cursor cursor = getMessageThreadContactThreadCursorJoin(databaseHelper, item, id);
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ContactItemColumns.KEY_ID},
                    ContactItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{item.getDataId()}, null);
            if (null != cursor && 0 != cursor.getCount()) {
                _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(ContactItemColumns.KEY_ID));
            cursor.close();
            item.set_id(_id);
        }
        if (_id != 0) {
//        creates the join
            createOrUpdateMessagesContactsJoin(databaseHelper, databaseHelper.getWritableDatabase(), _id, id);
        }
        return item.get_id();
    }

    public static long createOrUpdateMessagesContactsJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = getMessagesContactsJoin(databaseHelper, db, id, fk);
        ContentValues values = new ContentValues();
        values.put(MessagesContactsJoinColumns.KEY_MSGTHREAD_ID, id);
        values.put(MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID, fk);
        values.put(MessagesContactsJoinColumns.KEY_TIMESTAMP,
                MainApplicationSingleton.getDateTimeMillis());
        if (0 == _id) {
            _id = databaseHelper.insert(db,
                    CREATE_TABLE_MSGEMAILCONTACTS_CONTACT_JOIN, null, values);
        } else {
            _id = databaseHelper.update(db,
                    TABLE_MSGEMAILCONTACTS_CONTACT_JOIN, values,
                    MessagesContactsJoinColumns.KEY_ID + " = ?",
                    new String[]{String.valueOf(_id)});
        }
        return _id;
    }

    public static long getMessagesContactsJoin(
            DatabaseHelper databaseHelper, SQLiteDatabase db, long id, long fk) {
        long _id = 0;
        Cursor c = databaseHelper.query(db, TABLE_MSGEMAILCONTACTS_CONTACT_JOIN,
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
            DatabaseHelper databaseHelper, SQLiteDatabase db, ContactItem item, long id) {
        Cursor cursor = getMessageThreadContactThreadCursorJoin(databaseHelper, item, id);
        long _id = 0;
        if (null == cursor || 0 == cursor.getCount()) {
            if (cursor != null) cursor.close();
            cursor = databaseHelper.query(db, MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS,
                    new String[]{ContactItemColumns.KEY_ID},
                    ContactItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{item.getDataId()}, null);
            if (null != cursor && 0 != cursor.getCount()) {
                _id = cursor.getLong(cursor.getColumnIndex(
                        ContactItemColumns.KEY_ID));
                cursor.close();
                item.set_id(_id);
            }
        } else {
            _id = cursor.getLong(cursor.getColumnIndex(
                    ContactItemColumns.KEY_ID));
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
        String selectQuery = "SELECT  * FROM " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL + " nt " +
                " left join " + TABLE_MSGEMAILCONTACTS_CONTACT_JOIN +
                " ntm on nt.[" + MessageItemColumns.KEY_ID +
                "] = ntm.[" + MessagesContactsJoinColumns.KEY_MSGTHREAD_ID + "]  " +
                " left join " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS + " mt on ntm.[" +
                MessagesContactsJoinColumns.KEY_CONTACTTHREAD_ID +
                "] = mt.[" + ContactItemColumns.KEY_ID + "] " +
                " WHERE " + " mt." + ContactItemColumns.KEY_ID + " = ? " +
                " AND nt." + MessageItemColumns.KEY_ID + " = ? ";
//        Log.e(TAG, selectQuery);
        return databaseHelper.rawQuery(selectQuery, args);
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
            case CONTACTS_DIR_TYPE:
                return CONTACTS_DIR_MIME_TYPE;
            case CONTACTS_ITEM_TYPE:
                return CONTACTS_ITEM_MIME_TYPE;
            case CONTACTS_DATA_ITEM_TYPE:
                return CONTACTS_DATA_ITEM_MIME_TYPE;
            case CONTACTS_JOIN_DIR_TYPE:
                return CONTACTS_JOIN_DIR_MIME_TYPE;
            case CONTACTS_JOIN_ITEM_TYPE:
                return CONTACTS_JOIN_ITEM_MIME_TYPE;
            case CONTACTS_JOIN_DATA_ITEM_TYPE:
                return CONTACTS_JOIN_DATA_ITEM_MIME_TYPE;
            case CONTACTS_RAW_DIR_TYPE:
                return CONTACTS_RAW_DIR_MIME_TYPE;
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
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
            case CONTACTS_JOIN_DIR_TYPE:
                try {
                    cursor = getContactJoin(databaseHelper, selection, selectionArgs, sortOrder);
/*
                cursor = databaseHelper.query(DatabaseHelper.TABLE_MSGEMAILCONTACTS,
                        projection,
                        selection, selectionArgs, sortOrder);
*/
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
            case CONTACTS_ITEM_TYPE:
            case CONTACTS_DATA_ITEM_TYPE:
                try {
                    cursor = databaseHelper.query(
                            TABLE_MSGEMAILCONTACTS, projection,
                            selection,
                            selectionArgs, sortOrder);
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
            case CONTACTS_JOIN_ITEM_TYPE:
                try {
                    cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, ContentUris.parseId(uri));
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
            case CONTACTS_JOIN_DATA_ITEM_TYPE:
                try {
                    cursor = DeviceContactContentProvider.getDeviceContactsJoin(databaseHelper, uri.getLastPathSegment());
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
            case CONTACTS_RAW_DIR_TYPE:
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
        byte[] vals1 = values.getAsByteArray(ContactItem.TAG);
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
                try {
                    Collection<ContactItem> contactItems =
                            (Collection<ContactItem>)
                                    MainApplicationSingleton.Serializer.deserialize(vals1);
                    if (null == contactItems || contactItems.isEmpty()) {
                        return null;
                    }
                    long[] ids = createOrUpdateContacts(databaseHelper, contactItems);
                    if (null == ids || 0 == ids.length || ids.length != contactItems.size()) {
                        return null;
                    }
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(uri, null);
                    }
                    return Uri.withAppendedPath(uri, String.valueOf(ids[0]));
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CONTACTS_ITEM_TYPE:
                try {
                    ContactItem contactItem = (ContactItem)
                            MainApplicationSingleton.Serializer.deserialize(vals1);
                    ArrayList<ContactItem> contactItems = new ArrayList<>(1);
                    contactItems.add(contactItem);
                    long[] ids = createOrUpdateContacts(databaseHelper, contactItems);
                    Uri insertUri = ContentUris.withAppendedId(
                            MsgEmailContactsContentProvider.CONTENT_URI, ids[0]);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(insertUri, null);
//                    context.getContentResolver().notifyChange(uri, null);
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
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        byte[] vals1 = values.getAsByteArray(ContactItem.TAG);
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
            case CONTACTS_JOIN_DIR_TYPE:
                try {
                    Collection<ContactItem> contactItems =
                            (Collection<ContactItem>)
                                    MainApplicationSingleton.Serializer.deserialize(vals1);
                    if (null == contactItems || contactItems.isEmpty()) {
                        return 0;
                    }
                    long[] ids = updateContactThreads(databaseHelper, contactItems);
                    if (null == ids || 0 == ids.length || ids.length != contactItems.size()) {
                        return 0;
                    }
                    Context context = getContext();
                    if (null != context) {
                        Uri updateUri = ContentUris.withAppendedId(
                                MsgEmailContactsContentProvider.CONTENT_URI, ids[0]);
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return (int) ids[0];
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CONTACTS_ITEM_TYPE:
            case CONTACTS_JOIN_ITEM_TYPE:
                try {
                    ContactItem contactItem = (ContactItem)
                            MainApplicationSingleton.Serializer.deserialize(vals1);
                    if (null == contactItem) {
                        return 0;
                    }
                    ArrayList<ContactItem> contactItems = new ArrayList<>(1);
                    contactItems.add(contactItem);
                    long[] ids = updateContactThreads(databaseHelper, contactItems);
                    if (null == ids || 0 == ids.length || ids.length != contactItems.size()) {
                        return 0;
                    }
//                    id = (int) databaseHelper.createOrUpdateMessagesContacts(item);
                    Context context = getContext();
                    if (null != context) {
                        Uri updateUri = ContentUris.withAppendedId(
                                MsgEmailContactsContentProvider.CONTENT_URI, ids[0]);
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return (int) ids[0];
                } catch (SQLException | IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CONTACTS_DATA_ITEM_TYPE:
            case CONTACTS_JOIN_DATA_ITEM_TYPE:
                try {
                    int rows = databaseHelper.update(TABLE_MSGEMAILCONTACTS,
                            values, selection, selectionArgs);
                    if (0 == rows) {
                        return 0;
                    }
                    Context context = getContext();
                    if (null != context) {
                        Uri updateUri = ContentUris.withAppendedId(
                                MsgEmailContactsContentProvider.CONTENT_URI, rows);
                        context.getContentResolver().notifyChange(updateUri, null);
                    }
                    return rows;
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
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (URI_MATCHER.match(uri)) {
            case CONTACTS_DIR_TYPE:
            case CONTACTS_ITEM_TYPE:
            case CONTACTS_DATA_ITEM_TYPE:
                try {
                    int id = databaseHelper.delete(TABLE_MSGEMAILCONTACTS,
                            selection, selectionArgs);
                    Uri insertUri = ContentUris.withAppendedId(MsgEmailContactsContentProvider.CONTENT_URI, id);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(insertUri, null);
                    }
                    return id;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                break;
            case CONTACTS_JOIN_DIR_TYPE:
            case CONTACTS_JOIN_ITEM_TYPE:
            case CONTACTS_JOIN_DATA_ITEM_TYPE:
                try {
                    if (null == selectionArgs || selectionArgs.length < 2) return 0;
                    long _id = Long.parseLong(selectionArgs[0]);
                    long id = Long.parseLong(selectionArgs[1]);
                    int row = (int) deleteContactThreadContact(databaseHelper, _id, id);
                    Uri insertUri = ContentUris.withAppendedId(
                            MsgEmailContactsContentProvider.CONTENT_URI, row);
                    Context context = getContext();
                    if (null != context) {
                        context.getContentResolver().notifyChange(insertUri, null);
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
