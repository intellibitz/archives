package com.mobeegal.android;

/*
<!--
$Id:: MobeegalApplication.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.util.Log;

/**
 * MobeegalApplication<br> Application class to maintain Global state.. will be
 * invoked once during mobeegal startup by Android
 */
public class MobeegalApplication
        extends Application
{

    public static final String PROVIDER_NAME = "gps";
    static final String LOG_TAG = "MobeegalApplication";

    @Override
    public void onCreate()
    {
        super.onCreate();
        initLocation();
//        openOrCreateDatabase();
    }

    void initLocation()
    {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Settings.System.putString(getContentResolver(),
                Settings.System.LOCATION_PROVIDERS_ALLOWED, PROVIDER_NAME);
        locationManager.updateProviders();
        LocationProvider prov = locationManager.getProvider(PROVIDER_NAME);
        Log.i(LOG_TAG, prov.toString());

/*
        Location loc = locationManager.getCurrentLocation(PROVIDER_NAME);
        if (loc == null)
        {
            Log.e(LOG_TAG, "current location null");
        }
        else
        {
            Log.d(LOG_TAG, "location: " + loc.toString());
        }
*/

        Location loc = locationManager.getLastKnownLocation(PROVIDER_NAME);
        if (loc == null)
        {
            Log.e(LOG_TAG, "last known location null");
        }
        else
        {
            Log.d(LOG_TAG, "last known location: " + loc.toString());
        }

    }

/*
    void openOrCreateDatabase()
    {
        SQLiteDatabase myDB =
                this.openOrCreateDatabase("Mobeegal", MODE_PRIVATE, null);
        try
        {
            String[] col = {"UserID"};
            Cursor c = myDB.query("MobeegalUser", col, null, null,
                    null, null, null);
//                int viewname = c.getColumnIndexOrThrow("UserID");
            if (c != null)
            {
                c.close();
            }
        }
        catch (Exception e)
        {
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS MobeegalUser (IMSI VARCHAR,UserID VARCHAR);");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS mStuffmapview (details VARCHAR,latitude NUMERIC,laongitude NUMERIC,location VARCHAR);");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS favorites (details VARCHAR,latitude NUMERIC,laongitude NUMERIC,location VARCHAR);");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS searchResults (details VARCHAR,latitude NUMERIC,laongitude NUMERIC,location VARCHAR);");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS MStuff (mUserID NUMERIC, age NUMERIC,sex VARCHAR,Height NUMERIC,Weight NUMERIC,Area VARCHAR,City VARCHAR,Country VARCHAR);");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS Preferences (preference VARCHAR, views VARCHAR, service VARCHAR, time VARCHAR, settime VARCHAR,lbservice VARCHAR, lbstate VARCHAR, lbtimesettings VARCHAR);");
            myDB.execSQL(
                    "INSERT INTO Preferences (preference, views, service, time, settime, lbservice, lbstate, lbtimesettings) " +
                            "VALUES ('" + "preference" + "','" + "MapView" +
                            "','" + "Auto" + "','" + "1" + "','" +
                            "10 Seconds" + "','" + "off" + "','" + "auto" +
                            "','" + "1" + "');");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS serviceactivation (service VARCHAR,status NUMERIC);");
            myDB.execSQL(
                    "INSERT INTO serviceactivation (service,status) VALUES ('" +
                            "deactivate" + "','" + "1" + "');");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS selectedlocation (latitude NUMERIC,longitude NUMERIC,zoomlevel NUMERIC);");
            myDB.execSQL(
                    "INSERT INTO selectedlocation ( latitude, longitude, zoomlevel) VALUES ( 12961539, 80186860, 10 );");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS catalogs(catalogID NUMERIC(3),catalogname VARCHAR,state VARCHAR,catalogtype VARCHAR);");
            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS category(categoryID NUMERIC(3),catalogID1 NUMERIC(3),categoryname VARCHAR,status VARCHAR,querystatus VARCHAR,catalogtype VARCHAR,modes VARCHAR,catalogname VARCHAR);");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS Cars" +
                    " (key INTEGER PRIMARY KEY,imake VARCHAR,imodel VARCHAR,iyear VARCHAR,icolor VARCHAR,ifuel_type VARCHAR,iprice VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,umake VARCHAR,umodel VARCHAR,uyear VARCHAR,ucolor VARCHAR,ufuel_type VARCHAR,uprice VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
            myDB.execSQL("CREATE TABLE IF NOT EXISTS CarsPosition" +
                    " (key INTEGER PRIMARY KEY, imakeposition NUMERIC ,imodelposition NUMERIC ,iyearposition NUMERIC,icolorposition NUMERIC , ifuelposition NUMERIC , ipriceposition NUMERIC,iarea VARCHAR, icity VARCHAR, icountry VARCHAR , ilatitude VARCHAR, ilongitude VARCHAR, umakeposition NUMERIC ,umodelposition NUMERIC ,uyearposition NUMERIC , ucolorposition NUMERIC , ufuelposition NUMERIC , upriceposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR , ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS Dating" +
                    " (key INTEGER PRIMARY KEY,iage VARCHAR, isex VARCHAR, iheight VARCHAR, " +
                    "iweight VARCHAR, iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR," +
                    "uage VARCHAR, usex VARCHAR, uheight VARCHAR," +
                    "uweight VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, queryStatus VARCHAR,queryDate DATE);");
            myDB.execSQL("CREATE TABLE IF NOT EXISTS datingposition" +
                    " (key INTEGER PRIMARY KEY,iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS Home" +
                    " (key INTEGER PRIMARY KEY,irental VARCHAR, imisc VARCHAR, irate VARCHAR, " +
                    "istatus VARCHAR, iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR," +
                    "urental VARCHAR, umisc VARCHAR, urate VARCHAR," +
                    "ustatus VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, queryStatus VARCHAR,queryDate DATE);");
            myDB.execSQL("CREATE TABLE IF NOT EXISTS rentalposition" +
                    " (key INTEGER PRIMARY KEY,irentalposition NUMERIC, imiscposition NUMERIC, irateposition NUMERIC, istatusposition NUMERIC,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, urentalposition NUMERIC, umiscposition NUMERIC, urateposition NUMERIC,ustatusposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS Jewelry" +
                    " (key INTEGER PRIMARY KEY,ijewelry VARCHAR,igender VARCHAR,istone VARCHAR,imetal VARCHAR, iweight VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,ujewelry VARCHAR,ugender VARCHAR,ustone VARCHAR,umetal VARCHAR,uweight VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
            myDB.execSQL("CREATE TABLE IF NOT EXISTS JewelryPosition" +
                    "(key INTEGER PRIMARY KEY, ijewelryposition NUMERIC, igenderposition NUMERIC, istoneposition NUMERIC, imetalposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ujewelryposition NUMERIC, ugenderposition NUMERIC, ustoneposition NUMERIC, umetalposition NUMERIC, uweightposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR);");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS Matrimony" +
                    " (key INTEGER PRIMARY KEY,ireligion VARCHAR,icaste VARCHAR,iage VARCHAR, isex VARCHAR, iheight VARCHAR, " +
                    "iweight VARCHAR,icolor VARCHAR ,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,ureligion VARCHAR,ucaste VARCHAR,uage VARCHAR, usex VARCHAR, uheight VARCHAR," +
                    "uweight VARCHAR,ucolor VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
            myDB.execSQL("CREATE TABLE IF NOT EXISTS matrimonyposition" +
                    " (key INTEGER PRIMARY KEY,ireligionposition NUMERIC, icasteposition NUMERIC, iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, icolorposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ureligionposition NUMERIC, ucasteposition NUMERIC, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC,ucolorposition NUMERIC,  uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");

            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS Restaurants(key INTEGER PRIMARY KEY,iStuffCuisinetype VARCHAR,iStuffCookingMethod VARCHAR,iStuffDietetic VARCHAR,iStuffCourseType VARCHAR,iStuffDishType VARCHAR,iStuffMainIngredient VARCHAR,iStuffOccasionOrSeason VARCHAR,iStuffMiscellaneous VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,uStuffCuisinetype VARCHAR,uStuffCookingMethod VARCHAR,uStuffDietetic VARCHAR,uStuffCourseType VARCHAR,uStuffDishType VARCHAR,uStuffMainIngredient VARCHAR,uStuffOccasionOrSeason VARCHAR,uStuffMiscellaneous VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
            myDB.execSQL("CREATE TABLE IF NOT EXISTS restaurantsposition" +
                    " (key INTEGER PRIMARY KEY,iCuisineTypeposition NUMERIC, iCookingMethodposition NUMERIC, " +
                    "iDieteticposition NUMERIC,iCourseTypeposition NUMERIC,iDishTypeposition NUMERIC,iMainIngredientposition NUMERIC," +
                    "iOccasionOrSeasonposition NUMERIC,iMiscellaneousposition NUMERIC ,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, " +
                    "uCuisineTypeposition NUMERIC, uCookingMethodposition NUMERIC, uDieteticposition NUMERIC,uCourseTypeposition NUMERIC," +
                    "uDishTypeposition NUMERIC,uMainIngredientposition NUMERIC,uOccasionOrSeasonposition NUMERIC,uMiscellaneousposition NUMERIC ," +
                    "uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR," +
                    " category VARCHAR, stufftype VARCHAR);");

            myDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS Movies(key INTEGER PRIMARY KEY,imovietype VARCHAR,imovielanguage VARCHAR,iseatingstyle VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR,umovietype VARCHAR,umovielanguage VARCHAR,useatingstyle VARCHAR, uarea VARCHAR,ucity VARCHAR, ucountry VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, querystatus VARCHAR,queryDate DATE);");
            myDB.execSQL("CREATE TABLE IF NOT EXISTS moviesposition" +
                    " (key INTEGER PRIMARY KEY,imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR,ilatitude VARCHAR, ilongitude VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, useatingstyleposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,  ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
        }
    }
*/

}
