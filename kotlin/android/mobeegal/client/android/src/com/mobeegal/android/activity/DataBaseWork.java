package com.mobeegal.android.activity;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;


public class DataBaseWork
        extends ListActivity
{

    private final String DATABASE_NAME = "Mobeegal";
    private final String DATABASE_TABLE = "mStuffdetails";

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        SQLiteDatabase myDB = null;
        try
        {
            this.openOrCreateDatabase(DATABASE_NAME,
                    Context.MODE_PRIVATE, null);
            myDB = this.openOrCreateDatabase(DATABASE_NAME,
                    Context.MODE_PRIVATE, null);
            myDB.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE +
                    " (mstuffid VARCHAR, details VARCHAR, latitude NUMERIC," +
                    "  longitude NUMERIC, location VARCHAR);");
            myDB.execSQL("INSERT INTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3331', 'age=20, height =150, weight = 55, location = IntellibtzTechnologies', 12961539, 80186860, 'Intellibitz Technologies');");
            myDB.execSQL("INSERT INTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3332', 'age=20, height =150, weight = 55, location = St.Thomas Mount', 12994577, 80199297, 'St.Thomas Mount');");
            myDB.execSQL("INSERT INTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3333', 'age=20, height =150, weight = 55, location = Guindy', 13009590, 80211818, 'Guindy');");
            myDB.execSQL("INSERT INTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3334', 'age=20, height =150, weight = 55, location = Saidapet', 13019908, 80224771, 'Saidapet');");
            myDB.execSQL("INSERT IDataBaseWorkNTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3335', 'age=20, height =150, weight = 55, location = TNagar-Renganathan Street', 13036939, 80230285, 'TNagar-Renganathan Street');");
            myDB.execSQL("INSERT INTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3336', 'age=20, height =150, weight = 55, location = TNagar-Natesan Street', 13035791, 80231888, 'TNagar-Natesan Street');");
            myDB.execSQL("INSERT INTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3337', 'age=20, height =150, weight = 55, location = TNagar-Panagalpark', 13042928, 80232570, 'TNagar-Panagalpark');");
            myDB.execSQL("INSERT INTO " + DATABASE_TABLE +
                    " (mstuffid, details, latitude, longitude, location)" +
                    " VALUES ('3338', 'age=20, height =150, weight = 55, location = Adyar', 13003145, 80253532, 'Adyar');");
        }
        catch (Exception e)
        {
        }
        finally
        {
            if (myDB != null)
            {
                myDB.close();
            }
        }
        Intent intent = new Intent(DataBaseWork.this, TimeSettings.class);
        startActivity(intent);
    }

}
