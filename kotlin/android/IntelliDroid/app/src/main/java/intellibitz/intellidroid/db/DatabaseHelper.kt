package intellibitz.intellidroid.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.content.IntellibitzContactContentProvider
import intellibitz.intellidroid.content.MessageChatContentProvider
import intellibitz.intellidroid.content.MessageChatGroupContentProvider
import intellibitz.intellidroid.content.MessageEmailContentProvider
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.content.MsgChatContactContentProvider
import intellibitz.intellidroid.content.MsgChatContactsContentProvider
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider
import intellibitz.intellidroid.content.MsgChatGrpContactContentProvider
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider
import intellibitz.intellidroid.content.MsgEmailContactContentProvider
import intellibitz.intellidroid.content.MsgEmailContactsContentProvider
import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider
import intellibitz.intellidroid.content.MsgsGrpDraftContentProvider
import intellibitz.intellidroid.content.MsgsGrpPeopleChatGroupsContentProvider
import intellibitz.intellidroid.content.MsgsGrpPeopleChatsContentProvider
import intellibitz.intellidroid.content.MsgsGrpPeopleContentProvider
import intellibitz.intellidroid.content.MsgsGrpPeopleEmailsContentProvider
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.content.UserEmailContentProvider

class DatabaseHelper private constructor(context: Context, dbName: String) :
    SQLiteOpenHelper(context, dbName, null, DATABASE_VERSION) {

    companion object {
        const val TAG = "DBHelper"
        const val DATABASE_NAME = "intellibitz.db"
        const val IDX = "_idx"
        private const val DATABASE_VERSION = 1

        @JvmField
        var databaseHelper: DatabaseHelper? = null

        @JvmStatic
        fun newInstance(context: Context, dbName: String?): DatabaseHelper {
            if (null == databaseHelper) {
                val actualName = dbName ?: DATABASE_NAME
                databaseHelper = DatabaseHelper(context, actualName)
            }
            return databaseHelper!!
        }

        @JvmStatic
        fun getRowCountCursor(uri: Uri, table: String, where: String?, context: Context): Cursor? {
            val actualWhere = where ?: ""
            return context.contentResolver.query(
                uri, null,
                "select count(*) as count from $table$actualWhere", null, null
            )
        }

        @JvmStatic
        fun getRowCount(cursor: Cursor?): Int {
            val isZeroCount = cursor == null || 0 == cursor.count
            if (!isZeroCount) {
                return cursor!!.getInt(cursor.getColumnIndex("count"))
            }
            return 0
        }

        @JvmStatic
        fun isRowCountEmpty(cursor: Cursor?): Boolean {
            var isZeroCount = cursor == null || 0 == cursor.count
            if (!isZeroCount) {
                val count = cursor!!.getInt(cursor.getColumnIndex("count"))
                isZeroCount = (0 == count)
            }
            return isZeroCount
        }

        @JvmStatic
        fun isRowCountEmpty(uri: Uri, table: String, where: String?, context: Context): Boolean {
            val cursor = getRowCountCursor(uri, table, where, context)
            val isEmpty = isRowCountEmpty(cursor)
            cursor?.close()
            return isEmpty
        }

        @JvmStatic
        fun fetchRowCount(uri: Uri, table: String, where: String?, context: Context): Int {
            val cursor = getRowCountCursor(uri, table, where, context)
            val count = getRowCount(cursor)
            cursor?.close()
            return count
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e(TAG, "onUpgrade: from old version $oldVersion to new version $newVersion")
        db.execSQL("DROP TABLE IF EXISTS " + UserContentProvider.TABLE_USERS)
        db.execSQL("DROP TABLE IF EXISTS " + UserContentProvider.TABLE_USERS_EMAILS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + UserEmailContentProvider.TABLE_USEREMAILS)
        db.execSQL("DROP TABLE IF EXISTS " + DeviceContactContentProvider.TABLE_DEVICECONTACTS)
        db.execSQL("DROP TABLE IF EXISTS " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT)
        db.execSQL("DROP TABLE IF EXISTS " + IntellibitzContactContentProvider.TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN)

        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGECHAT)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGECHAT_ATTACHMENTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT)
        db.execSQL("DROP INDEX IF EXISTS " + MessageItemColumns.KEY_DATA_ID + IDX)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT_CONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatContentProvider.TABLE_MESSAGESCHAT_MESSAGE_JOIN)

        db.execSQL("DROP TABLE IF EXISTS " + MsgChatAttachmentContentProvider.TABLE_MSGCHATATTACHMENT)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactContentProvider.TABLE_MSGCHATCONTACT)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactContentProvider.TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatContactsContentProvider.TABLE_MSGCHATCONTACTS_CONTACT_JOIN)

        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL)
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL_CONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL)
        db.execSQL("DROP INDEX IF EXISTS " + MessageItemColumns.KEY_DATA_ID + IDX)
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL_CONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageEmailContentProvider.TABLE_MESSAGESEMAIL_MESSAGE_JOIN)

        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailAttachmentContentProvider.TABLE_MSGEMAILATTACHMENT)
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT)
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactContentProvider.TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS)
        db.execSQL("DROP TABLE IF EXISTS " + MsgEmailContactsContentProvider.TABLE_MSGEMAILCONTACTS_CONTACT_JOIN)

        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpPeopleContentProvider.TABLE_MSGSGRPPEOPLE)
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpPeopleChatsContentProvider.TABLE_MSGSGRPPEOPLECHATS)
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpPeopleEmailsContentProvider.TABLE_MSGSGRPPEOPLEEMAILS)
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpClutterContentProvider.TABLE_MSGSGRPCLUTTER)

        db.execSQL("DROP TABLE IF EXISTS " + IntellibitzItemColumns.TABLE_INFOS)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGECHATGROUP)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MessageChatGroupContentProvider.TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN)

        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpAttachmentContentProvider.TABLE_MSGCHATGRPATTACHMENT)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactContentProvider.TABLE_MSGCHATGRPCONTACT_INTELLIBITZCONTACTS_JOIN)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS)
        db.execSQL("DROP TABLE IF EXISTS " + MsgChatGrpContactsContentProvider.TABLE_MSGCHATGRPCONTACTS_CONTACT_JOIN)

        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpDraftContentProvider.TABLE_MSGSGRPDRAFT)
        db.execSQL("DROP TABLE IF EXISTS " + MsgsGrpPeopleChatGroupsContentProvider.CREATE_TABLE_MSGSGRPPEOPLECHATGROUPS)

        onCreate(db)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(IntellibitzItemColumns.CREATE_TABLE_INFOS)
        db.execSQL(UserContentProvider.CREATE_TABLE_USERS)
        db.execSQL(UserEmailContentProvider.CREATE_TABLE_USEREMAILS)
        db.execSQL(UserEmailContentProvider.CREATE_TABLE_USERS_EMAILS_JOIN)
        db.execSQL(DeviceContactContentProvider.CREATE_TABLE_DEVICECONTACTS)
        db.execSQL(IntellibitzContactContentProvider.CREATE_TABLE_INTELLIBITZCONTACTS)
        db.execSQL(IntellibitzContactContentProvider.CREATE_TABLE_INTELLIBITZCONTACT_DEVICECONTACT_JOIN)

        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGECHAT)
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGECHAT_CONTACTS_JOIN)
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGECHAT_ATTACHMENTS_JOIN)
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT)
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT_INDEX)
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT_CONTACTS_JOIN)
        db.execSQL(MessageChatContentProvider.CREATE_TABLE_MESSAGESCHAT_MESSAGE_JOIN)
        db.execSQL(MsgChatAttachmentContentProvider.CREATE_TABLE_MSGCHATATTACHMENT)
        db.execSQL(MsgChatContactContentProvider.CREATE_TABLE_MSGCHATCONTACT)
        db.execSQL(MsgChatContactContentProvider.CREATE_TABLE_MSGCHATCONTACT_INTELLIBITZCONTACTS_JOIN)
        db.execSQL(MsgChatContactsContentProvider.CREATE_TABLE_MSGCHATCONTACTS)
        db.execSQL(MsgChatContactsContentProvider.CREATE_TABLE_MSGCHATCONTACTS_CONTACT_JOIN)

        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGEEMAIL)
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGEEMAIL_CONTACTS_JOIN)
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGEEMAIL_ATTACHMENTS_JOIN)
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL)
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL_INDEX)
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL_CONTACTS_JOIN)
        db.execSQL(MessageEmailContentProvider.CREATE_TABLE_MESSAGESEMAIL_MESSAGE_JOIN)
        db.execSQL(MsgEmailAttachmentContentProvider.CREATE_TABLE_MSGEMAILATTACHMENT)
        db.execSQL(MsgEmailContactContentProvider.CREATE_TABLE_MSGEMAILCONTACT)
        db.execSQL(MsgEmailContactContentProvider.CREATE_TABLE_MSGEMAILCONTACT_INTELLIBITZCONTACTS_JOIN)
        db.execSQL(MsgEmailContactsContentProvider.CREATE_TABLE_MSGEMAILCONTACTS)
        db.execSQL(MsgEmailContactsContentProvider.CREATE_TABLE_MSGEMAILCONTACTS_CONTACT_JOIN)

        db.execSQL(MsgsGrpPeopleContentProvider.CREATE_TABLE_MSGSGRPPEOPLE)
        db.execSQL(MsgsGrpPeopleChatsContentProvider.CREATE_TABLE_MSGSGRPPEOPLECHATS)
        db.execSQL(MsgsGrpPeopleEmailsContentProvider.CREATE_TABLE_MSGSGRPPEOPLEEMAILS)
        db.execSQL(MsgsGrpClutterContentProvider.CREATE_TABLE_MSGSGRPCLUTTER)

        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGECHATGROUP)
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGECHATGROUP_CONTACTS_JOIN)
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGECHATGROUP_ATTACHMENTS_JOIN)
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP)
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP_INDEX)
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP_CONTACTS_JOIN)
        db.execSQL(MessageChatGroupContentProvider.CREATE_TABLE_MESSAGESCHATGROUP_MESSAGE_JOIN)
        db.execSQL(MsgChatGrpAttachmentContentProvider.CREATE_TABLE_MSGCHATGRPATTACHMENT)
        db.execSQL(MsgChatGrpContactContentProvider.CREATE_TABLE_MSGCHATGRPCONTACT)
        db.execSQL(MsgChatGrpContactContentProvider.CREATE_TABLE_MSGCHATGRPCONTACT_INTELLIBITZCONTACTS_JOIN)
        db.execSQL(MsgChatGrpContactsContentProvider.CREATE_TABLE_MSGCHATGRPCONTACTS)
        db.execSQL(MsgChatGrpContactsContentProvider.CREATE_TABLE_MSGCHATGRPCONTACTS_CONTACT_JOIN)
        db.execSQL(MsgsGrpDraftContentProvider.CREATE_TABLE_MSGSGRPDRAFT)
        db.execSQL(MsgsGrpPeopleChatGroupsContentProvider.CREATE_TABLE_MSGSGRPPEOPLECHATGROUPS)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e(TAG, "onDowngrade: from old version $oldVersion to new version $newVersion")
        onUpgrade(db, oldVersion, newVersion)
    }

    fun closeDB() {
        val db = readableDatabase
        if (db != null && db.isOpen) db.close()
    }

    fun rawQuery(sql: String?, selectionArgs: Array<String>?): Cursor? {
        val db = readableDatabase
        return rawQuery(db, sql, selectionArgs)
    }

    fun rawQuery(db: SQLiteDatabase, sql: String?, selectionArgs: Array<String>?): Cursor? {
        val c = db.rawQuery(sql, selectionArgs)
        if (c != null && c.moveToFirst()) return c
        c?.close()
        return null
    }

    fun query(
        table: String?, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return query(table, projection, selection, selectionArgs, null, null, sortOrder)
    }

    fun query(
        db: SQLiteDatabase, table: String?, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return query(db, table, projection, selection, selectionArgs, null, null, sortOrder)
    }

    fun query(
        table: String?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
        groupBy: String?, having: String?, sortOrder: String?
    ): Cursor? {
        val db = readableDatabase
        return query(db, table, projection, selection, selectionArgs, groupBy, having, sortOrder)
    }

    fun queryIfKeyFound(
        table: String?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
        groupBy: String?, having: String?, sortOrder: String?
    ): Boolean {
        val db = readableDatabase
        return queryIfKeyFound(db, table, projection, selection, selectionArgs, groupBy, having, sortOrder)
    }

    fun query(
        db: SQLiteDatabase,
        table: String?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
        groupBy: String?, having: String?, sortOrder: String?
    ): Cursor? {
        val c = db.query(table, projection, selection, selectionArgs, groupBy, having, sortOrder)
        if (c != null && c.moveToFirst()) return c
        c?.close()
        return null
    }

    fun queryIfKeyFound(
        db: SQLiteDatabase,
        table: String?, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
        groupBy: String?, having: String?, sortOrder: String?
    ): Boolean {
        var found = false
        val c = db.query(table, projection, selection, selectionArgs, groupBy, having, sortOrder)
        if (c != null && c.moveToFirst()) {
            found = c.count > 0
        }
        c?.close()
        return found
    }

    fun insert(table: String?, nullColumnHack: String?, values: ContentValues?): Long {
        val db = writableDatabase
        return insert(db, table, nullColumnHack, values)
    }

    fun insert(
        db: SQLiteDatabase,
        table: String?, nullColumnHack: String?, values: ContentValues?
    ): Long {
        return db.insert(table, nullColumnHack, values)
    }

    fun update(table: String?, values: ContentValues?, where: String?, whereArgs: Array<String>?): Int {
        val db = writableDatabase
        return update(db, table, values, where, whereArgs)
    }

    fun update(
        db: SQLiteDatabase,
        table: String?, values: ContentValues?, where: String?, whereArgs: Array<String>?
    ): Int {
        return db.update(table, values, where, whereArgs)
    }

    fun delete(table: String?, where: String?, whereArgs: Array<String>?): Int {
        val db = writableDatabase
        return delete(db, table, where, whereArgs)
    }

    fun delete(db: SQLiteDatabase, table: String?, where: String?, whereArgs: Array<String>?): Int {
        return db.delete(table, where, whereArgs)
    }

    fun getRawCursor(sql: String?, selectionArgs: Array<String>?): Cursor? {
        return rawQuery(sql, selectionArgs)
    }
}
