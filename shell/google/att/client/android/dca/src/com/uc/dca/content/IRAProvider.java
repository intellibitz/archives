/**
 * 
 */
package com.uc.dca.content;





import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * @author muthu
 *
 */
public class IRAProvider extends ContentProvider {
    private static final String TAG = "ContentProvider";

    private static final int DETAILS = 1;
    private static final int DETAILS_ITEM = 2;
    private IncidentReport.DatabaseHelper databaseHelper;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(IncidentReport.AUTHORITY, IncidentReport.Details.TABLE_NAME, DETAILS);
        uriMatcher.addURI(IncidentReport.AUTHORITY, IncidentReport.Details.TABLE_NAME + "/#", DETAILS_ITEM);
    }


	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
        switch (uriMatcher.match(uri)) {
        case DETAILS:
            return IncidentReport.Details.CONTENT_TYPE;
        case DETAILS_ITEM:
            return IncidentReport.Details.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
    }
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// TODO Auto-generated method stub
        ContentValues values = new ContentValues(initialValues);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        long rowId;
        switch (uriMatcher.match(uri)) {
            case DETAILS:
                rowId = db.insert(IncidentReport.Details.TABLE_NAME, IncidentReport.Details.ID, values);
                if (rowId > 0) {
                    Uri noteUri = ContentUris.withAppendedId(IncidentReport.Details.CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(noteUri, null);
                    return noteUri;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        throw new SQLException("Failed to insert row into " + uri);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
        databaseHelper = new IncidentReport.DatabaseHelper(getContext());
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case DETAILS:
                qb.setTables(IncidentReport.Details.TABLE_NAME);
                break;
            case DETAILS_ITEM:
                qb.setTables(IncidentReport.Details.TABLE_NAME);
                qb.appendWhere(IncidentReport.Details._ID + "=" +
                        uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
