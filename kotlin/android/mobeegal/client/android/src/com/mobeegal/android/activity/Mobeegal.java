package com.mobeegal.android.activity;

/*
<!--
$Id:: Mobeegal.java 14 2008-08-19 06:36:45Z muthu.ramadoss                      $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.mobeegal.android.R;

public class Mobeegal
        extends Activity
{

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.mobeegal);
        showResultsView();
// don't show this again.. remove this activity from history
        finish();
    }

    void showResultsView()
    {
        // If this were my app's main activity, I would load the default values so they're set even if
        // the user does not go into the preferences screen.
        PreferenceManager
                .setDefaultValues(this, R.xml.preferences, false);
        // Since we're in the same package, we can use this context to get
        // the default shared preferences
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        final boolean mapview =
                sharedPref.getBoolean("mapview_preference", true);

        if (mapview)
        {
            startActivity(
                    new Intent(Mobeegal.this, MapResults.class));
        }
        else
        {
            startActivity(
                    new Intent(Mobeegal.this, MStuffTextView.class));
        }
/*
        SQLiteDatabase myDB;
        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String viewtype[] = {"views"};
            Cursor cur = myDB.query("Preferences", viewtype, null, null,
                    null, null, null);
            if (cur.isFirst())
            {
                do
                {
                    int viewname = cur.getColumnIndexOrThrow("views");
                    String viewtypename = cur.getString(viewname);
                    if (viewtypename.equals("MapView"))
                    {
                        startActivity(
                                new Intent(Mobeegal.this, MapResults.class));
                    }
                    else
                    {
                        startActivity(
                                new Intent(Mobeegal.this, MStuffTextView.class));
                    }
                }
                while (cur.moveToNext());
            }

            myDB.query("mStuffdetails", null, null, null, null, null,
                    null);
        }
        catch (Exception e)
        {
            myDB
                    .execSQL("CREATE TABLE IF NOT EXISTS "
                            + "mStuffdetails"
                            +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);");
            myDB
                    .execSQL("INSERT INTO "
                            + "mStuffdetails"
                            +
                            " (mstuffid, catagory,  details, latitude, longitude, location)"
                            + " VALUES ('" + 000 + "','" + "Marker" + "','"
                            + "Details: Intellibitz Technologies" + "',"
                            + 12967842 + "," + 80189257 + ",'" + "chennai"
                            + "');");
            myDB
                    .execSQL("INSERT INTO "
                            + "mStuffdetails"
                            +
                            " (mstuffid, catagory,  details, latitude, longitude, location)"
                            + " VALUES ('" + 001 + "','" + "Marker" + "','"
                            + "Details: Marina Beach" + "'," + 13053487 + ","
                            + 80284168 + ",'" + "chennai" + "');");
            myDB
                    .execSQL("INSERT INTO "
                            + "mStuffdetails"
                            +
                            " (mstuffid, catagory,  details, latitude, longitude, location)"
                            + " VALUES ('" + 002 + "','" + "Marker" + "','"
                            + "Details: Chennai Airport" + "'," + 12989391
                            + "," + 80171387 + ",'" + "chennai" + "');");
            myDB
                    .execSQL(
                            "INSERT INTO mStuffdetails (mstuffid, catagory, details, latitude, longitude, location) VALUES ('"
                                    + "001"
                                    + "','"
                                    + "userDating"
                                    + "','"
                                    + "Details: User Dating Query"
                                    + "','"
                                    + 13.053487
                                    + "','" + 80.284168 + "','" +
                                    "geticountry" + "');");
            myDB
                    .execSQL(
                            "INSERT INTO mStuffdetails (mstuffid, catagory, details, latitude, longitude, location) VALUES ('"
                                    + "002"
                                    + "','"
                                    + "userMatrimony"
                                    + "','"
                                    + "Details: User Matrimony Query"
                                    + "','"
                                    + 12.053487
                                    + "','"
                                    + 81.284168
                                    + "','"
                                    + "geticountry" + "');");
            myDB
                    .execSQL(
                            "INSERT INTO mStuffdetails (mstuffid, catagory, details, latitude, longitude, location) VALUES ('"
                                    + "003"
                                    + "','"
                                    + "userJewelry"
                                    + "','"
                                    + "Details: User Jewelry"
                                    + "','"
                                    + 13.053487
                                    + "','" + 80.284168 + "','" +
                                    "geticountry" + "');");
            myDB
                    .execSQL(
                            "INSERT INTO mStuffdetails (mstuffid, catagory, details, latitude, longitude, location) VALUES ('"
                                    + "004"
                                    + "','"
                                    + "userCars"
                                    + "','"
                                    + "Details: User Car Query"
                                    + "','"
                                    + 13.053487
                                    + "','" + 80.284168 + "','" +
                                    "geticountry" + "');");
            myDB
                    .execSQL(
                            "INSERT INTO mStuffdetails (mstuffid, catagory, details, latitude, longitude, location) VALUES ('"
                                    + "005"
                                    + "','"
                                    + "userRental"
                                    + "','"
                                    + "Details: User Rental Query"
                                    + "','"
                                    + 13.053487
                                    + "','" + 80.284168 + "','" +
                                    "geticountry" + "');");
            myDB
                    .execSQL(
                            "INSERT INTO mStuffdetails (mstuffid, catagory, details, latitude, longitude, location) VALUES ('"
                                    + "006"
                                    + "','"
                                    + "userRestaurant"
                                    + "','"
                                    + "Details: User Restaurant Query"
                                    + "','"
                                    + 13.053487
                                    + "','"
                                    + 80.284168
                                    + "','"
                                    + "geticountry" + "');");
            myDB
                    .execSQL(
                            "INSERT INTO mStuffdetails (mstuffid, catagory, details, latitude, longitude, location) VALUES ('"
                                    + "007"
                                    + "','"
                                    + "userMovies"
                                    + "','"
                                    + "Details: User Movies Query"
                                    + "','"
                                    + 13.053487
                                    + "','" + 80.284168 + "','" +
                                    "geticountry" + "');");
            // Toast.makeText(this, "This is the place where Mobeegal
            // developed", Toast.LENGTH_SHORT).show();
        }
*/
    }


}
