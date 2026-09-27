/**
 * 
 */
package com.uc.dca.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * @author muthu
 *
 */
public class IncidentReport {

	
    private IncidentReport() {
		super();
		// TODO Auto-generated constructor stub
	}
    
	public static final String AUTHORITY = "com.uc.dca.content.IncidentReport";
    public static final String DATABASE_NAME = "ira.db";
    public static final int DATABASE_VERSION = 2;

    /**
     * Common Trackers columns
     */
    public static class IRColumns
            implements BaseColumns {

        private IRColumns() {/*This class cannot be instantiated*/}

        static final String CONTENT_TYPE_PREFIX = "vnd.uc.cursor.dir/vnd.uc.";
        static final String CONTENT_ITEM_TYPE_PREFIX = "vnd.uc.cursor.item/vnd.uc.";
        static final String CONTENT_URI_PREFIX = "content://";
        static final String CONTENT_SEPARATOR = "/";
    }

    public static final class Details
    		extends IRColumns {
		private Details() {/*This class cannot be instantiated*/}
		
		public static final String TABLE_NAME = "details";
		
		public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_PREFIX + TABLE_NAME;
		public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_PREFIX
		        + AUTHORITY + CONTENT_SEPARATOR + TABLE_NAME);
		
		public static final String ID = "id";
		public static final String DETAILS = "details";
}

    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    static class DatabaseHelper
            extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Details.TABLE_NAME + " ("
                    + Details._ID + " INTEGER PRIMARY KEY,"
                    + Details.ID + " INTEGER NOT NULL,"
                    + Details.DETAILS + " TEXT NOT NULL"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("IR#DatabaseHelper ==>", "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + Details.TABLE_NAME);
            onCreate(db);
        }
    }

}
