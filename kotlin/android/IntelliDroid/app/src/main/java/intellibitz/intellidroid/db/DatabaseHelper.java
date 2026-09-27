package intellibitz.intellidroid.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.content.IntellibitzContactContentProvider;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageChatGroupContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgChatContactContentProvider;
import intellibitz.intellidroid.content.MsgChatContactsContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpContactContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgEmailContactContentProvider;
import intellibitz.intellidroid.content.MsgEmailContactsContentProvider;
import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider;
import intellibitz.intellidroid.content.MsgsGrpDraftContentProvider;
import intellibitz.intellidroid.content.MsgsGrpPeopleChatGroupsContentProvider;
import intellibitz.intellidroid.content.MsgsGrpPeopleChatsContentProvider;
import intellibitz.intellidroid.content.MsgsGrpPeopleContentProvider;
import intellibitz.intellidroid.content.MsgsGrpPeopleEmailsContentProvider;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.content.*;


public class DatabaseHelper extends
        SQLiteOpenHelper {
    // Logcat tag
    public static final String TAG = "DBHelper";

    // Database Name
    public static final String DATABASE_NAME = "intellibitz.db";
    public static final String IDX = "_idx";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    public static DatabaseHelper databaseHelper;

    private DatabaseHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    public static DatabaseHelper newInstance(Context context, String dbName) {
        if (null == databaseHelper) {
            if (null == dbName) {
                dbName = DATABASE_NAME;
            }
            databaseHelper = new DatabaseHelper(context, dbName);
        }
        return databaseHelper;
    }

    public static Cursor getRowCountCursor(Uri uri, String table, String where, Context context) {
        if (null == where) where = "";
        return context.getContentResolver().query(
                uri, null,
                "select count(*) as count from " + table + where, null, null);
    }

    public static int getRowCount(Cursor cursor) {
        boolean isZeroCount = (null == cursor || 0 == cursor.getCount());
        if (!isZeroCount) {
//            checks the actual item count.. not the cursor count
            return cursor.getInt(cursor.getColumnIndex("count"));
        }
        return 0;
    }

    public static boolean isRowCountEmpty(Cursor cursor) {
        boolean isZeroCount = (null == cursor || 0 == cursor.getCount());
        if (!isZeroCount) {
//            checks the actual item count.. not the cursor count
            int count = cursor.getInt(cursor.getColumnIndex("count"));
            isZeroCount = (0 == count);
        }
        return isZeroCount;
    }

    public static boolean isRowCountEmpty(Uri uri, String table, String where, Context context) {
        Cursor cursor = getRowCountCursor(uri, table, where, context);
        boolean isEmpty = isRowCountEmpty(cursor);
        if (cursor != null) cursor.close();
        return isEmpty;
    }

    public static int fetchRowCount(Uri uri, String table, String where, Context context) {
        Cursor cursor = getRowCountCursor(uri, table, where, context);
        int count = getRowCount(cursor);
        if (cursor != null) cursor.close();
        return count;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onUpgrade: from old version " + oldVersion + " to new version " + newVersion);
// on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + UserContentProvider.TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + UserContentProvider.TABLE_USERS_EMAILS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + UserEmailContentProvider.TABLE_USEREMAILS);
        db.execSQL("DROP TABLE IF EXISTS " + DeviceContactContentProvider.TABLE_DEVICECONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGECHAT);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT);
        db.execSQL("DROP INDEX IF EXISTS " + MessageItemColumns.KEY_DATA_ID + IDX);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT_MESSAGE_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MsgChatAttachmentContentProvider.TABLE_MSGCHATATTACHMENT);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactContentProvider.TABLE_MSGCHATCONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactContentProvider.TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS_CONTACT_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL);
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL_CONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL);
        db.execSQL("DROP INDEX IF EXISTS " + MessageItemColumns.KEY_DATA_ID + IDX);
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL_CONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL_MESSAGE_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailAttachmentContentProvider.TABLE_MSGEMAILATTACHMENT);
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpPeopleContentProvider.TABLE_MSGSGRPPEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpPeopleChatsContentProvider.TABLE_MSGSGRPPEOPLECHATS);
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpClutterContentProvider.TABLE_MSGSGRPCLUTTER);
        db.execSQL("DROP INDEX IF EXISTS " + MessageItemColumns.KEY_DATA_ID + IDX);

        db.execSQL("DROP TABLE IF EXISTS " + IntellibitzItemColumns.TABLE_INFOS);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGECHATGROUP);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpAttachmentContentProvider.TABLE_MSGCHATGRPATTACHMENT);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS_CONTACT_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT);
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpPeopleChatGroupsContentProvider.CREATE_TABLE_MSGSGRPPEOPLECHATGROUPS);

/*
        db.execSQL("DROP TABLE IF EXISTS " + BroadcastContactContentProvider.TABLE_BROADCASTCONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + BroadcastContactContentProvider.TABLE_BROADCASTCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + BroadcastContactsContentProvider.TABLE_BROADCASTCONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + BroadcastContactsContentProvider.TABLE_BROADCASTCONTACTS_CONTACT_JOIN);

        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpNestContentProvider.TABLE_MSGSGRPNEST);
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpNestContentProvider.TABLE_MSGSGRPNEST_MESSAGES_JOIN);
        db.execSQL("DROP TABLE IF EXISTS " + MsgsStackNestContentProvider.TABLE_MSGSSTACKNEST);
*/
//        db.execSQL("DROP TABLE IF EXISTS " + CntsGrpBroadcastContentProvider.TABLE_CNTSGRPBROADCAST);
//        db.execSQL("DROP TABLE IF EXISTS " + AttachmentContentProvider.TABLE_ATTACHMENT);

        // creates new tables
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(IntellibitzItemColumns.CREATE_TABLE_INFOS);
        db.execSQL(UserContentProvider.CREATE_TABLE_USERS);
        db.execSQL(UserEmailContentProvider.CREATE_TABLE_USEREMAILS);
        db.execSQL(UserEmailContentProvider.CREATE_TABLE_USERS_EMAILS_JOIN);
        db.execSQL(DeviceContactContentProvider.CREATE_TABLE_DEVICECONTACTS);
        db.execSQL(IntellibitzContactContentProvider.CREATE_TABLE_INTELLIBITZCONTACTS);
        db.execSQL(IntellibitzContactContentProvider.CREATE_TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN);

        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGECHAT);
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGECHAT_CONTACTS_JOIN);
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGECHAT_ATTACHMENTS_JOIN);
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT);
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT_INDEX);
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT_CONTACTS_JOIN);
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT_MESSAGE_JOIN);
        db.execSQL(MsgChatAttachmentContentProvider.CREATE_TABLE_MSGCHATATTACHMENT);
        db.execSQL(MsgChatContactContentProvider.CREATE_TABLE_MSGCHATCONTACT);
        db.execSQL(MsgChatContactContentProvider.CREATE_TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL(MsgChatContactsContentProvider.CREATE_TABLE_MSGCHATCONTACTS);
        db.execSQL(MsgChatContactsContentProvider.CREATE_TABLE_MSGCHATCONTACTS_CONTACT_JOIN);

        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGEEMAIL);
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGEEMAIL_CONTACTS_JOIN);
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN);
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL);
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL_INDEX);
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL_CONTACTS_JOIN);
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL_MESSAGE_JOIN);
        db.execSQL(MsgEmailAttachmentContentProvider.CREATE_TABLE_MSGEMAILATTACHMENT);
        db.execSQL(MsgEmailContactContentProvider.CREATE_TABLE_MSGEMAILCONTACT);
        db.execSQL(MsgEmailContactContentProvider.CREATE_TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL(MsgEmailContactsContentProvider.CREATE_TABLE_MSGEMAILCONTACTS);
        db.execSQL(MsgEmailContactsContentProvider.CREATE_TABLE_MSGEMAILCONTACTS_CONTACT_JOIN);

        db.execSQL(MsgsGrpPeopleContentProvider.CREATE_TABLE_MSGSGRPPEOPLE);
        db.execSQL(MsgsGrpPeopleChatsContentProvider.CREATE_TABLE_MSGSGRPPEOPLECHATS);
        db.execSQL(MsgsGrpPeopleEmailsContentProvider.CREATE_TABLE_MSGSGRPPEOPLEEMAILS);
        db.execSQL(MsgsGrpClutterContentProvider.CREATE_TABLE_MSGSGRPCLUTTER);

        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGECHATGROUP);
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGECHATGROUP_CONTACTS_JOIN);
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN);
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP);
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP_INDEX);
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN);
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN);
        db.execSQL(MsgChatGrpAttachmentContentProvider.CREATE_TABLE_MSGCHATGRPATTACHMENT);
        db.execSQL(MsgChatGrpContactContentProvider.CREATE_TABLE_MSGCHATGRPCONTACT);
        db.execSQL(MsgChatGrpContactContentProvider.CREATE_TABLE_MSGCHATGRPCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL(MsgChatGrpContactsContentProvider.CREATE_TABLE_MSGCHATGRPCONTACTS);
        db.execSQL(MsgChatGrpContactsContentProvider.CREATE_TABLE_MSGCHATGRPCONTACTS_CONTACT_JOIN);
        db.execSQL(MsgsGrpDraftContentProvider.CREATE_TABLE_MSGSGRPDRAFT);
        db.execSQL(MsgsGrpPeopleChatGroupsContentProvider.CREATE_TABLE_MSGSGRPPEOPLECHATGROUPS);

/*
        db.execSQL(BroadcastContactContentProvider.CREATE_TABLE_BROADCASTCONTACT);
        db.execSQL(BroadcastContactContentProvider.CREATE_TABLE_BROADCASTCONTACT_INTELLIBITZCONTACTS_JOIN);
        db.execSQL(BroadcastContactsContentProvider.CREATE_TABLE_BROADCASTCONTACTS);
        db.execSQL(BroadcastContactsContentProvider.CREATE_TABLE_BROADCASTCONTACTS_CONTACT_JOIN);

        db.execSQL(FeedContentProvider.CREATE_TABLE_FEED);
        db.execSQL(ScheduleContentProvider.CREATE_TABLE_SCHEDULE);

        db.execSQL(MsgsGrpNestContentProvider.CREATE_TABLE_MSGSGRPNEST);
        db.execSQL(MsgsGrpNestContentProvider.CREATE_TABLE_MSGSGRPNEST_MESSAGES_JOIN);
        db.execSQL(MsgsStackNestContentProvider.CREATE_TABLE_MSGSSTACKNEST);
*/
//        db.execSQL(CntsGrpBroadcastContentProvider.CREATE_TABLE_CNTSGRPBROADCAST);
//        db.execSQL(AttachmentContentProvider.CREATE_TABLE_ATTACHMENT);
    }

    // ------------------------  SQL Generic parameterized queries ----------------//

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onDowngrade: from old version " + oldVersion + " to new version " + newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return rawQuery(db, sql, selectionArgs);
    }

    public Cursor rawQuery(SQLiteDatabase db, String sql, String[] selectionArgs) {
        Cursor c = db.rawQuery(sql, selectionArgs);
        if (c != null && c.moveToFirst())
            return c;
        if (c != null) c.close();
        return null;
    }

    public Cursor query(String table, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return query(table, projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    public Cursor query(SQLiteDatabase db, String table, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return query(db, table, projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    public Cursor query(String table, String[] projection, String selection, String[] selectionArgs,
                        String groupBy, String having, String sortOrder) {
        SQLiteDatabase db = this.getReadableDatabase();
        return query(db, table, projection, selection, selectionArgs,
                groupBy, having, sortOrder);
    }

    public boolean queryIfKeyFound(String table, String[] projection, String selection, String[] selectionArgs,
                                   String groupBy, String having, String sortOrder) {
        SQLiteDatabase db = this.getReadableDatabase();
        return queryIfKeyFound(db, table, projection, selection, selectionArgs,
                groupBy, having, sortOrder);
    }

    public Cursor query(SQLiteDatabase db,
                        String table, String[] projection, String selection, String[] selectionArgs,
                        String groupBy, String having, String sortOrder) {
        Cursor c = db.query(table, projection, selection, selectionArgs,
                groupBy, having, sortOrder);
        if (c != null && c.moveToFirst())
            return c;
        if (c != null) c.close();
        return null;
    }

    public boolean queryIfKeyFound(SQLiteDatabase db,
                                   String table, String[] projection, String selection, String[] selectionArgs,
                                   String groupBy, String having, String sortOrder) {
        boolean found = false;
        Cursor c = db.query(table, projection, selection, selectionArgs,
                groupBy, having, sortOrder);
        if (c != null && c.moveToFirst()) {
            found = c.getCount() > 0;
        }
        if (c != null) c.close();
        return found;
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insert(db, table, nullColumnHack, values);
    }

    public long insert(SQLiteDatabase db,
                       String table, String nullColumnHack, ContentValues values) {
        return db.insert(table, nullColumnHack, values);
    }

    public int update(String table, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return update(db, table, values, where, whereArgs);
    }

    public int update(SQLiteDatabase db,
                      String table, ContentValues values, String where, String[] whereArgs) {
        return db.update(table, values, where, whereArgs);
    }

    public int delete(String table, String where, String[] whereArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        return delete(db, table, where, whereArgs);
    }

    public int delete(SQLiteDatabase db, String table, String where, String[] whereArgs) {
        return db.delete(table, where, whereArgs);
    }

    public Cursor getRawCursor(String sql, String[] selectionArgs) {
        return rawQuery(sql, selectionArgs);
    }

    // ------------------------  table methods ----------------//

}
