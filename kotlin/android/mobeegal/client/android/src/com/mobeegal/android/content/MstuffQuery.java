package com.mobeegal.android.content;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.activity.StatusbarNotification;
import com.mobeegal.android.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class MstuffQuery
        extends BroadcastReceiver
{

    private static Logger logger = Logger.getLogger("MStuffQuery");
    SQLiteDatabase myDB = null;
    String catalogs = null;
    String mY_DATING_DATABASE = "Mobeegal";
    String mY_catalogs_TABLE = "catalogs";
    String mY_category_TABLE = "category";
    ArrayList categoryal = new ArrayList();
    ArrayList categoryalc = new ArrayList();
    ArrayList catalogsal = new ArrayList();
    JSONStringer js = new JSONStringer();
    ArrayList<String> mStuff0i = new ArrayList<String>();
    ArrayList<String> mStuff0i2 = new ArrayList<String>();
    ArrayList<String> mStuff0i1 = new ArrayList<String>();
    String values;
    String keys;
    String response = null;
    String request = null;
    String id = "";
    String details = "";
    String location = "";
    String dblatitude;
    String dblongitude;
    int latitude;
    int longitude;
    int latitude1;
    int longitude1;
    double lat;
    double lon;
    double lat1;
    double lon1;
    String catagory;
    //MobeegalUser
    private static String mY_MobeegalUser_TABLE = "MobeegalUser";
    String catalogsname = "catalogname";
    String catalogsstate = "state";
    String categorysname = "categoryname";
    String categorystate = "status";
    //Dating
    private static String mY_Dating_TABLE = "Dating";
    ArrayList idatingal = new ArrayList();
    ArrayList udatingal = new ArrayList();
    ArrayList<String> datingcolumnal = new ArrayList<String>();
    ArrayList<String> idatingcolumnal = new ArrayList<String>();
    ArrayList<String> udatingcolumnal = new ArrayList<String>();
    String iStuffAge = null;
    String iStuffSex = null;
    String iStuffHeight = null;
    String iStuffWeight = null;
    String iStuffLocation = null;
    String iStuffCity = null;
    String iStuffCountry = null;
    String uStuffAge = null;
    String uStuffSex = null;
    String uStuffHeight = null;
    String uStuffWeight = null;
    String uStuffLocation = null;
    String uStuffCity = null;
    String uStuffCountry = null;
    String iStufflatitude = null;
    String iStufflongitude = null;
    String uStufflatitude = null;
    String uStufflongitude = null;
    //Matrimony
    private static String mY_Matrimony_TABLE = "Matrimony";
    ArrayList iMatrimony = new ArrayList();
    ArrayList uMatrimony = new ArrayList();
    ArrayList<String> matrimonycolumnal = new ArrayList<String>();
    ArrayList<String> iMatrimonycolumnal = new ArrayList<String>();
    ArrayList<String> uMatrimonycolumnal = new ArrayList<String>();
    String iReligion = null;
    String iCaste = null;
    String iColor = null;
    String iAge = null;
    String iSex = null;
    String iHeight = null;
    String iWeight = null;
    String iArea = null;
    String iCity = null;
    String iCountry = null;
    String iLatitude = null;
    String iLongitude = null;
    String uReligion = null;
    String uCaste = null;
    String uColor = null;
    String uAge = null;
    String uSex = null;
    String uHeight = null;
    String uWeight = null;
    String uArea = null;
    String uCity = null;
    String uCountry = null;
    String uLatitude = null;
    String uLongitude = null;
    //Cars
    private static String mY_Cars_TABLE = "Cars";
    ArrayList<String> carscolumnal = new ArrayList<String>();
    ArrayList<String> iCarscolumnal = new ArrayList<String>();
    ArrayList<String> uCarscolumnal = new ArrayList<String>();
    ArrayList iCars = new ArrayList();
    ArrayList uCars = new ArrayList();
    String iCarMake = null;
    String iCarModel = null;
    String iCarYear = null;
    String iCarColor = null;
    String iCarFuel_Type = null;
    String iCarPrice = null;
    String iCarArea = null;
    String iCarCity = null;
    String iCarCountry = null;
    String iCarLatitude = null;
    String iCarLongitude = null;
    String uCarMake = null;
    String uCarModel = null;
    String uCarYear = null;
    String uCarColor = null;
    String uCarFuel_Type = null;
    String uCarPrice = null;
    String uCarArea = null;
    String uCarCity = null;
    String uCarCountry = null;
    String uCarLatitude = null;
    String uCarLongitude = null;
    //Jewelry
    private static String mY_Jewelry_TABLE = "Jewelry";
    ArrayList<String> jewelrycolumnal = new ArrayList<String>();
    ArrayList<String> iJewelrycolumnal = new ArrayList<String>();
    ArrayList<String> uJewelrycolumnal = new ArrayList<String>();
    ArrayList iJewelry = new ArrayList();
    ArrayList uJewelry = new ArrayList();
    String iJewelryType = null;
    String iGender = null;
    String iStoneType = null;
    String iMetalType = null;
    String iJewelryWeight = null;
    String iJewelryCountry = null;
    String iJewelryCity = null;
    String iJewelryArea = null;
    String iJewelryLatitude = null;
    String iJewelryLongitude = null;
    String uJewelryType = null;
    String uGender = null;
    String uStoneType = null;
    String uMetalType = null;
    String uJewelryWeight = null;
    String uJewelryCountry = null;
    String uJewelryCity = null;
    String uJewelryArea = null;
    String uJewelryLatitude = null;
    String uJewelryLongitude = null;
    //Rental
    private static String mY_home_TABLE = "Home";
    ArrayList<String> rentalcolumnal = new ArrayList<String>();
    ArrayList<String> iRentalcolumnal = new ArrayList<String>();
    ArrayList<String> uRentalcolumnal = new ArrayList<String>();
    ArrayList iRental = new ArrayList();
    ArrayList uRental = new ArrayList();
    String iRentalType = null;
    String iRentalMisc = null;
    String iRentalRaterange = null;
    String iRentalCountry = null;
    String iRentalstatus = null;
    String iRentalCity = null;
    String iRentalArea = null;
    String uRentalType = null;
    String uRentalMisc = null;
    String uRentalRaterange = null;
    String uRentalCountry = null;
    String uRentalstatus = null;
    String uRentalCity = null;
    String uRentalArea = null;
    String iRentallatitude = null;
    String iRentallongitude = null;
    String uRentallatitude = null;
    String uRentallongitude = null;
    //Restaurants
    private static String mY_Restaurants_TABLE = "Restaurants";
    ArrayList<String> restaurantscolumnal = new ArrayList<String>();
    ArrayList<String> iRestaurantscolumnal = new ArrayList<String>();
    ArrayList<String> uRestaurantscolumnal = new ArrayList<String>();
    ArrayList iRestaurants = new ArrayList();
    ArrayList uRestaurants = new ArrayList();
    String iCuisineType = null;
    String iCookingMethod = null;
    String iDietetic = null;
    String iCourseType = null;
    String iDishType = null;
    String iMainIngredient = null;
    String iOccasionOrSeason = null;
    String iMiscellaneous = null;
    String iRestaurantsCity = null;
    String iRestaurantsArea = null;
    String iRestaurantsCountry = null;
    String iRestaurantsLatitude = null;
    String iRestaurantsLongitude = null;
    String uCuisineType = null;
    String uCookingMethod = null;
    String uDietetic = null;
    String uCourseType = null;
    String uDishType = null;
    String uMainIngredient = null;
    String uOccasionOrSeason = null;
    String uMiscellaneous = null;
    String uRestaurantsCity = null;
    String uRestaurantsArea = null;
    String uRestaurantsCountry = null;
    String uRestaurantsLatitude = null;
    String uRestaurantsLongitude = null;
    //Movies
    private static String mY_Movies_TABLE = "Movies";
    ArrayList<String> moviescolumnal = new ArrayList<String>();
    ArrayList<String> iMoviescolumnal = new ArrayList<String>();
    ArrayList<String> uMoviescolumnal = new ArrayList<String>();
    ArrayList iMovies = new ArrayList();
    ArrayList uMovies = new ArrayList();
    String iMovieType = null;
    String iMovieLanguage = null;
    String iSeatingStyle = null;
    String iMovieArea = null;
    String iMovieCity = null;
    String iMovieCountry = null;
    String iMovieLatitude = null;
    String iMovieLongitude = null;
    String uMovieType = null;
    String uMovieLanguage = null;
    String uSeatingStyle = null;
    String uMovieArea = null;
    String uMovieCity = null;
    String uMovieCountry = null;
    String uMovieLatitude = null;
    String uMovieLongitude = null;
    String decrypted;
    String datingstring = "Dating";
    String matrimonystring = "Matrimony";
    String carsstring = "Cars";
    String jewelrystring = "Jewelry";
    String rentalstring = "Rental";
    String restaurantstring = "Restaurants";
    String moviesstring = "Movies";
    Context con;

    public void onReceive(final Context context, Intent intent)
    {
        con = context;
        new Thread()
        {

            private int catalogsindex;

            @Override
            public void run()
            {
                Looper.prepare();
                try
                {
                    myDB = context.openOrCreateDatabase(mY_DATING_DATABASE,
                            Context.MODE_PRIVATE, null);

                    Cursor mobeegalUserCursor =
                            myDB.query(mY_MobeegalUser_TABLE, null,
                                    null, null, null, null, null);
                    int UseridColumn =
                            mobeegalUserCursor.getColumnIndexOrThrow("UserID");
                    String useridColumn = null;
                    if (mobeegalUserCursor != null)
                    {
                        if (mobeegalUserCursor.isFirst())
                        {
                            useridColumn =
                                    mobeegalUserCursor.getString(UseridColumn);
                            // logger.info("mobeegalUserID = " + useridColumn);
                        }
                    }

                    /*String[] catalogcolumn = new String[1];
                    catalogcolumn[0] = "catalogname";
                    String testquery = "select categoryname,catalogname from category,catalogs where category.catalogID1" + "=" + "catalogs.catalogID and category.querystatus='true'";
                    String[] testquerycolumn = new String[0];
                    Cursor catalogsCursor = myDB.rawQuery(testquery, testquerycolumn);
                    mobeegalUserCursor.close();
                    if (catalogsCursor.isFirst()) {
                        do {
                            String catalogs = catalogsCursor.getString(1);
                            // logger.info("catalogs = " + catalogs);
                            catalogsal.add(catalogs);
                        } while (catalogsCursor.moveToNext());
                    }
                    catalogsCursor.close();*/
                    // added new
                    String[] catalogcolumn = {"catalogname"};
                    Cursor catalogsCursor = myDB.query("category",
                            catalogcolumn, "querystatus='true'",
                            null, null, null, "catalogname");
                    catalogsindex =
                            catalogsCursor.getColumnIndexOrThrow("catalogname");
                    if (catalogsCursor.isFirst())
                    {
                        do
                        {
                            String catalogs =
                                    catalogsCursor.getString(catalogsindex);
                            //           logger.info("catalogs = " + catalogs);
                            catalogsal.add(catalogs);
                        }
                        while (catalogsCursor.moveToNext());
                    }
                    catalogsCursor.close();

                    // using category cursor
                    /* String[] categorycolumn = new String[1];
                    categorycolumn[0] = "categoryname";
                    Cursor categorysc = myDB.query(true, "category", categorycolumn,
                            "querystatus='true'", null, null, null, null);
                    if (categorysc.isFirst()) {
                        do {
                            String categorys = categorysc.getString(0);
                            categoryal.add(categorys);
                        } while (categorysc.moveToNext());
                    }
                    categorysc.close();*/
                    String[] categorycolumn = new String[1];
                    categorycolumn[0] = "categoryname";
                    Cursor categoryscs = myDB.query("category",
                            categorycolumn, "querystatus='true'", null, null,
                            null, "categoryname");
                    // using category cursor
                    if (categoryscs.isFirst())
                    {
                        do
                        {
                            String categorys = categoryscs.getString(0);
                            categoryalc.add(categorys);
                            //  logger.info("categoryscs loop do-while : " + categoryalc.size());
                            //  logger.info("categoryscs loop do-while : " + categoryalc.isEmpty());
                        }
                        while (categoryscs.moveToNext());
                    }
                    categoryscs.close();


                    if (categoryalc.isEmpty())
                    {
                        js.object();
                        js.key("action").value("my_mstuff").key("query")
                                .object().key("id").value(useridColumn)
                                .endObject();
                        js.endObject();
                    }
                    else
                    {

                        js.object();
                        js.key("action").value("mstuff").key("query").object()
                                .key("id").value(useridColumn)
                                .key("mStuff_Query").array();
                        for (int l = 0; l < catalogsal.size(); l++)
                        {
                            js.object();
                            js.key("catalog").value(catalogsal.get(l));
                            // logger.info("catalogs " + l + " = " + catalogsal.get(l));
                            // js.key("catagory").value(categoryal.get(j)).key( "mStuff_query_criteria").array();

                            categorycolumn[0] = "categoryname";
                            Cursor categorys = myDB.query("category",
                                    categorycolumn,
                                    "querystatus='true' and catalogname ='" +
                                            catalogsal.get(l) + "'", null, null,
                                    null, null);
                            if (categorys.isFirst())
                            {
                                do
                                {
                                    String category = categorys.getString(0);
                                    categoryal.add(category);
                                    //        logger.info("category loop  : " + categorys);
                                }
                                while (categorys.moveToNext());
                            }
                            for (int i = 0; i < categoryal.size(); i++)
                            {
                                js.key("catagory").value(categoryal.get(l))
                                        .key("mStuff_query_criteria").array();
                                // logger.info("category " + l + " = " + categoryal.get(l));

                                if (datingstring.equals(categoryal.get(l)))
                                {
                                    // Calling Dating Method
                                    dating();
                                }
                                else
                                if (matrimonystring.equals(categoryal.get(l)))
                                {
                                    // Calling Matrimony Method
                                    matrimony();
                                }
                                else if (carsstring.equals(categoryal.get(l)))
                                {
                                    //Calling Cars Method
                                    cars();
                                }
                                else
                                if (jewelrystring.equals(categoryal.get(l)))
                                {
                                    //Calling Jewelry Method
                                    jewelry();
                                }
                                else if (rentalstring.equals(categoryal.get(l)))
                                {
                                    //calling rental method
                                    rental();
                                }
                                else
                                if (restaurantstring.equals(categoryal.get(l)))
                                {
                                    //Calling Restaurant Method
                                    restaurants();
                                }
                                else if (moviesstring.equals(categoryal.get(l)))
                                {
                                    //Calling Movies Method
                                    movies();
                                }
                                js.endArray();
                            }
                            js.endObject();
                        }
                        js.endArray();
                        js.endObject();
                        js.endObject();
                    }
                    String query = js.toString();
                    /*for (int w = 0; w < categoryal.size(); w++) {
                    logger.info(" categoryal  = " + categoryal.get(w));
                    myDB.execSQL("update category set querystatus='" + "false" + "' where categoryname='" + categoryal.get(w) + "';");
                    }*/
                    categoryal.clear();
                    catalogsal.clear();
                    logger.info(" Query = " + query);
                    HttpClient httpclient = new DefaultHttpClient();
                    String key = "intellibitz";
                    ArrayList<NameValuePair> data =
                            new ArrayList<NameValuePair>();
                    data.add(new BasicNameValuePair("data_pack",
                            query));
                    HttpPost httpPost = new HttpPost(
                            context.getString(R.string.RemoteServer));
                    httpPost.setEntity(
                            new UrlEncodedFormEntity(data, HTTP.UTF_8));
                    HttpResponse resp = httpclient.execute(httpPost);
                    String response = HttpUtils.getResponseString(resp);
                    //EncryptionDecryption encryptDecrypt = new EncryptionDecryption();
                    //String encrypted = encryptDecrypt.EncryptionDecryption(query, key);
//                    request = httpPost.getQueryString();
                    //decrypted = encryptDecrypt.EncryptionDecryption(response.trim(), key);
/*
                    Log.i("from client side..........................................",
                            data);
*/
                    Log.i("Server response.........................", response);
                    //logger.info("Request " + encrypted);
                    //logger.info("Decrypted Response " + decrypted);
                    //String response1 = "{\"mStuff\":[{\"catalog\":\"People\",\"category\":\"Dating\",\"result\":[]},{\"catalog\":\"People\",\"category\":\"Matrimony\",\"result\":[{\"id\":\"417391\",\"ireligion\":\"Hindu\",\"icaste\":\"BrahminShivalli\",\"iage\":\"34\",\"isex\":\"male\",\"iheight\":\"159\",\"iweight\":\"92\",\"icolor\":\"Average\",\"iarea\":\"Chandigarh\",\"icity\":\"Chandigarh\",\"icountry\":\"India\",\"ilatitude\":\"30.773958\",\"ilongitude\":\"76.801544\"},{\"id\":\"426585\",\"ireligion\":\"Christian\",\"icaste\":\"Evangelical\",\"iage\":\"35\",\"isex\":\"female\",\"iheight\":\"150\",\"iweight\":\"86\",\"icolor\":\"Average\",\"iarea\":\"padmala\",\"icity\":\"Kolhapur\",\"icountry\":\"India\",\"ilatitude\":\"16.695763\",\"ilongitude\":\"74.231132\"},{\"id\":\"475875\",\"ireligion\":\"Hindu\",\"icaste\":\"Kunbi\",\"iage\":\"31\",\"isex\":\"female\",\"iheight\":\"194\",\"iweight\":\"67\",\"icolor\":\"Any\",\"iarea\":\"Tirupathi\",\"icity\":\"Tirupathi\",\"icountry\":\"India\",\"ilatitude\":\"34.707762\",\"ilongitude\":\"-95.51566\"}]}]}";
                    // String response2 ="{\"mStuff\":[{\"catalog\":\"People\",\"category\":\"Dating\",\"result\":[{\"id\":\"378644\",\"iage\":\"42\",\"isex\":\"male\",\"iheight\":\"177\",\"iweight\":\"78\",\"iarea\":\"Kala_Danda\",\"icity\":\"Allahabad\",\"icountry\":\"India\",\"ilatitude\":\"25.437831\",\"ilongitude\":\"81.816344\"},{\"id\":\"391606\",\"iage\":\"40\",\"isex\":\"female\",\"iheight\":\"150\",\"iweight\":\"105\",\"iarea\":\"Munnar\",\"icity\":\"Munnar\",\"icountry\":\"India\",\"ilatitude\":\"0.999637\",\"ilongitude\":\"77.085548\"}]},{\"catalog\":\"People\",\"category\":\"Matrimony\",\"result\":[{\"id\":\"417391\",\"ireligion\":\"Hindu\",\"icaste\":\"BrahminShivalli\",\"iage\":\"34\",\"isex\":\"male\",\"iheight\":\"159\",\"iweight\":\"92\",\"icolor\":\"Average\",\"iarea\":\"Chandigarh\",\"icity\":\"Chandigarh\",\"icountry\":\"India\",\"ilatitude\":\"30.773958\",\"ilongitude\":\"76.801544\"},{\"id\":\"426585\",\"ireligion\":\"Christian\",\"icaste\":\"Evangelical\",\"iage\":\"35\",\"isex\":\"female\",\"iheight\":\"150\",\"iweight\":\"86\",\"icolor\":\"Average\",\"iarea\":\"padmala\",\"icity\":\"Kolhapur\",\"icountry\":\"India\",\"ilatitude\":\"16.695763\",\"ilongitude\":\"74.231132\"},{\"id\":\"475875\",\"ireligion\":\"Hindu\",\"icaste\":\"Kunbi\",\"iage\":\"31\",\"isex\":\"female\",\"iheight\":\"194\",\"iweight\":\"67\",\"icolor\":\"Any\",\"iarea\":\"Tirupathi\",\"icity\":\"Tirupathi\",\"icountry\":\"India\",\"ilatitude\":\"34.707762\",\"ilongitude\":\"-95.51566\"}]}]}";
                    JSONObject responseJson = new JSONObject(response);
                    String mStuff = responseJson.getString("mStuff");
                    // logger.info("mstuff:" + mStuff);
                    JSONArray mStuffJsonArray = new JSONArray(mStuff);
                    int mStuffJsonArraylength = mStuffJsonArray.length();
                    // logger.info("arry lenght :" + mStuffJsonArraylength);
                    // logger.info("mstuff:" + mStuff);

                    for (int i = 0; i < mStuffJsonArraylength; i++)
                    {
                        JSONObject mStuffinnerJson =
                                mStuffJsonArray.getJSONObject(i);
                        JSONArray mStuffinnerJsonArray =
                                mStuffinnerJson.names();
                        int mStuffinnerJsonArraylength =
                                mStuffinnerJsonArray.length();
                        // logger.info("arry lenghtiner :" + mStuffinnerJsonArraylength);
                        for (int j = 0; j < mStuffinnerJsonArraylength; j++)
                        {
                            keys = mStuffinnerJsonArray.getString(j);
                            mStuff0i2.add(keys);
                            // logger.info("mstuff arry key:" + j + ":" + keys);
                            String cc = mStuff0i2.get(j);
                            // logger.info("keys in list :" + cc);
                            values = mStuffinnerJson.getString(keys);
                            // logger.info("mstuff arry value:" + j + ":" + values);
                            mStuff0i.add(values);
                        }
                        logger.info("keys in list length:" + mStuff0i2.size());
                        logger.info("values in list length:" + mStuff0i.size());
                        if (mStuff0i.contains("Matrimony"))
                        {
                            values = mStuff0i.get(1);
                            logger.info("inside contain matrimony" + values);
                            matrimonyResponse();
                        }
                        else if (mStuff0i.contains("Dating"))
                        {
                            values = mStuff0i.get(1);
                            logger.info("inside contain dating" + values);
                            datingResponse();
                        }
                        else if (mStuff0i.contains("Cars"))
                        {
                            values = mStuff0i.get(1);
                            logger.info("inside contain cars" + values);
                            carsResponse();
                        }
                        else if (mStuff0i.contains("Jewelry"))
                        {
                            values = mStuff0i.get(1);
                            logger.info("inside contain jewelry" + values);
                            jewelryResponse();
                        }
                        else if (mStuff0i.contains("Rental"))
                        {
                            values = mStuff0i.get(1);
                            logger.info("inside contain rental" + values);
                            rentalResponse();
                        }
                        else if (mStuff0i.contains("Restaurants"))
                        {
                            values = mStuff0i.get(1);
                            logger.info("inside contain Restaurants" + values);
                            restaurantsResponse();
                        }
                        else if (mStuff0i.contains("Movies"))
                        {
                            values = mStuff0i.get(1);
                            logger.info("inside contain Movies" + values);
                            moviesResponse();
                        }
                        mStuff0i2.clear();
                        mStuff0i.clear();
                    }
                }
                catch (FileNotFoundException e)
                {
                    logger.info("Error = " + e.getMessage());
                }
                catch (SQLiteException e)
                {
                    logger.info("Error = " + e.getMessage());
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
                catch (JSONException e)
                {
                    logger.info("Error = " + e.getMessage());
                }
                catch (IOException e)
                {
                    Toast.makeText(context, "Unable to Connect to Server",
                            Toast.LENGTH_LONG).show();
                }
                //Displaying MStuffdating in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    String[] cols =
                            {"mStuffId", "mCatagory", "mStuffAge", "mStuffsex",
                                    "mStuffHeight", "mStuffWeight",
                                    "mStuffArea", "mStuffCity",
                                    "mStuffcountry", "mStuffLatitude",
                                    "mStuffLongitude"
                            };
                    Cursor c = myDB.query("MStuffdating", cols, null,
                            null, null, null, null);

                    final int mStuffId = c.getColumnIndexOrThrow("mStuffId");
                    final int mStuffAge = c.getColumnIndexOrThrow("mStuffAge");
                    final int mStuffHeight =
                            c.getColumnIndexOrThrow("mStuffHeight");
                    final int mStuffWeight =
                            c.getColumnIndexOrThrow("mStuffWeight");
                    final int mStuffArea =
                            c.getColumnIndexOrThrow("mStuffArea");
                    final int mStuffCity =
                            c.getColumnIndexOrThrow("mStuffCity");
                    final int mStuffcountry =
                            c.getColumnIndexOrThrow("mStuffcountry");
                    final int mStuffLatitude =
                            c.getColumnIndexOrThrow("mStuffLatitude");
                    final int mStuffLongitude =
                            c.getColumnIndexOrThrow("mStuffLongitude");
                    final int mStuffsex = c.getColumnIndexOrThrow("mStuffsex");
                    final int mCatagory = c.getColumnIndexOrThrow("mCatagory");
                    myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);");

                    if (c != null)
                    {
                        if (c.isFirst())
                        {
                            myDB.execSQL(
                                    "delete from mStuffdetails where catagory='Dating' OR catagory='Marker';");
                            do
                            {
                                id = c.getString(mStuffId);
                                catagory = c.getString(mCatagory);
                                location = c.getString(mStuffArea) + ", " +
                                        c.getString(mStuffCity) + ", " +
                                        c.getString(mStuffcountry);
                                details = "Age=" + c.getString(mStuffAge) +
                                        ", Sex=" + c.getString(mStuffsex) +
                                        ", Height=" +
                                        c.getString(mStuffHeight) +
                                        ", Weight=" +
                                        c.getString(mStuffWeight) +
                                        ", Location=" + location;
                                dblatitude = c.getString(mStuffLatitude);
                                dblongitude = c.getString(mStuffLongitude);
                                lat = Double.parseDouble(dblatitude);
                                lon = Double.parseDouble(dblongitude);
                                lat = lat * 1000000;
                                lon = lon * 1000000;
                                latitude = (int) lat;
                                longitude = (int) lon;
                                myDB.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');");

                            }
                            while (c.moveToNext());
                        }
                    }
                    // myDB.execSQL("update category set querystatus='" + "false" + "' where categoryname='" + "Dating" + "';");
                    c.close();
                }
                catch (Exception e)
                {
                }
                //Displaying MStuffmatrimony in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    String[] cols1 = {"mStuffId", "mCatagory", "mReligion",
                            "mCaste", "mStuffAge",
                            "mStuffsex", "mStuffHeight", "mStuffWeight",
                            "mColor", "mStuffArea",
                            "mStuffCity", "mStuffcountry", "mStuffLatitude",
                            "mStuffLongitude"
                    };
                    Cursor c1 = myDB.query("MStuffmatrimony", cols1, null,
                            null, null, null, null);
                    final int mstuffId1 = c1.getColumnIndexOrThrow("mStuffId");
                    final int mcatagory1 =
                            c1.getColumnIndexOrThrow("mCatagory");
                    final int mReligion1 =
                            c1.getColumnIndexOrThrow("mReligion");
                    final int mcaste1 = c1.getColumnIndexOrThrow("mCaste");
                    final int mstuffAge1 =
                            c1.getColumnIndexOrThrow("mStuffAge");
                    final int mstuffsex1 =
                            c1.getColumnIndexOrThrow("mStuffsex");
                    final int mstuffHeight1 =
                            c1.getColumnIndexOrThrow("mStuffHeight");
                    final int mstuffWeight1 =
                            c1.getColumnIndexOrThrow("mStuffWeight");
                    final int mColor1 = c1.getColumnIndexOrThrow("mColor");
                    final int mstuffArea1 =
                            c1.getColumnIndexOrThrow("mStuffArea");
                    final int mstuffCity1 =
                            c1.getColumnIndexOrThrow("mStuffCity");
                    final int mstuffcountry1 =
                            c1.getColumnIndexOrThrow("mStuffcountry");
                    final int mstuffLatitude1 =
                            c1.getColumnIndexOrThrow("mStuffLatitude");
                    final int mstuffLongitude1 =
                            c1.getColumnIndexOrThrow("mStuffLongitude");
                    myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);");

                    if (c1 != null)
                    {
                        if (c1.isFirst())
                        {
                            myDB.execSQL(
                                    "delete from mStuffdetails where catagory='Matrimony' OR catagory='Marker';");
                            do
                            {
                                id = c1.getString(mstuffId1);
                                catagory = c1.getString(mcatagory1);
                                String religion = c1.getString(mReligion1);
                                String caste = c1.getString(mcaste1);
                                String Age = c1.getString(mstuffAge1);
                                String sex = c1.getString(mstuffsex1);
                                String height = c1.getString(mstuffHeight1);
                                String weight = c1.getString(mstuffWeight1);
                                String color = c1.getString(mColor1);
                                String area = c1.getString(mstuffArea1);
                                String city = c1.getString(mstuffCity1);
                                String country = c1.getString(mstuffcountry1);
                                location = area + ", " + city + ", " + country;
                                details = "Religion=" + religion + ", Caste=" +
                                        caste + ", Age=" + Age + ", Sex=" +
                                        sex + ", Height=" + height +
                                        ", Weight=" + weight + ", Color=" +
                                        color + ", Location=" + location;
                                dblatitude = c1.getString(mstuffLatitude1);
                                dblongitude = c1.getString(mstuffLongitude1);
                                lat = Double.parseDouble(dblatitude);
                                lon = Double.parseDouble(dblongitude);
                                lat = lat * 1000000;
                                lon = lon * 1000000;
                                latitude = (int) lat;
                                longitude = (int) lon;

                                myDB.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');");

                            }
                            while (c1.moveToNext());
                        }
                    }
                    c1.close();
                }
                catch (Exception e)
                {
                }
                //Displaying MStuffcars in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    String[] cols = {"mStuffId", "mCatagory", "mStuffmake",
                            "mStuffmodel",
                            "mStuffyear", "mStuffcolor", "mStufffuel_type",
                            "mStuffprice", "mStuffarea", "mStuffcity",
                            "mStuffcountry", "mStuffLatitude", "mStuffLongitude"
                    };
                    Cursor c = myDB.query("MStuffcars", cols, null, null,
                            null, null, null);
                    final int mStuffId = c.getColumnIndexOrThrow("mStuffId");
                    final int mStuffMake =
                            c.getColumnIndexOrThrow("mStuffmake");
                    final int mStuffModel =
                            c.getColumnIndexOrThrow("mStuffmodel");
                    final int mStuffYear =
                            c.getColumnIndexOrThrow("mStuffyear");
                    final int mStuffColor =
                            c.getColumnIndexOrThrow("mStuffcolor");
                    final int mStuffFuel_Type =
                            c.getColumnIndexOrThrow("mStufffuel_type");
                    final int mStuffPrice =
                            c.getColumnIndexOrThrow("mStuffprice");
                    final int mStuffArea =
                            c.getColumnIndexOrThrow("mStuffarea");
                    final int mStuffCity =
                            c.getColumnIndexOrThrow("mStuffcity");
                    final int mStuffcountry =
                            c.getColumnIndexOrThrow("mStuffcountry");
                    final int mStuffLatitude =
                            c.getColumnIndexOrThrow("mStuffLatitude");
                    final int mStuffLongitude =
                            c.getColumnIndexOrThrow("mStuffLongitude");
                    final int mCatagory = c.getColumnIndexOrThrow("mCatagory");
                    myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);");

                    if (c != null)
                    {
                        if (c.isFirst())
                        {
                            myDB.execSQL(
                                    "delete from mStuffdetails where catagory='Cars' OR catagory='Marker';");
                            do
                            {
                                id = c.getString(mStuffId);
                                catagory = c.getString(mCatagory);
                                String mstuffmake = c.getString(mStuffMake);
                                String mstuffmodel = c.getString(mStuffModel);
                                String mstuffyear = c.getString(mStuffYear);
                                String mstuffcolor = c.getString(mStuffColor);
                                String mstufffueltype =
                                        c.getString(mStuffFuel_Type);
                                String mstuffprice = c.getString(mStuffPrice);
                                location = c.getString(mStuffArea) + ", " +
                                        c.getString(mStuffCity) + ", " +
                                        c.getString(mStuffcountry);
                                details = "Make=" + mstuffmake + ", Model=" +
                                        mstuffmodel + ", Year=" + mstuffyear +
                                        ", Color=" + mstuffcolor +
                                        ", FuelType=" + mstufffueltype +
                                        ", Price=" + mstuffprice +
                                        ", Location=" + location;
                                dblatitude = c.getString(mStuffLatitude);
                                dblongitude = c.getString(mStuffLongitude);
                                lat = Double.parseDouble(dblatitude);
                                lon = Double.parseDouble(dblongitude);
                                lat = lat * 1000000;
                                lon = lon * 1000000;
                                latitude = (int) lat;
                                longitude = (int) lon;
                                myDB.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');");

                            }
                            while (c.moveToNext());
                        }
                    }
                    c.close();
                }
                catch (Exception e)
                {
                }
                //Displaying MStuffjewelry in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    String[] cols1 = {"mStuffId", "mCatagory",
                            "mStuffJewelryMake", "mStuffJewelryGender",
                            "mStuffStoneType",
                            "mStuffMetalType", "mStuffWeightRange",
                            "mStuffarea",
                            "mStuffcity", "mStuffcountry", "mStuffLatitude",
                            "mStuffLongitude"
                    };
                    Cursor c1 = myDB.query("MStuffjewelry", cols1, null,
                            null, null,
                            null, null);
                    final int mstuffId1 = c1.getColumnIndexOrThrow("mStuffId");
                    final int mcatagory1 =
                            c1.getColumnIndexOrThrow("mCatagory");
                    final int mStuffJewelryMake1 =
                            c1.getColumnIndexOrThrow("mStuffJewelryMake");
                    final int mStuffJewelryGender1 =
                            c1.getColumnIndexOrThrow("mStuffJewelryGender");
                    final int mStuffStoneType1 =
                            c1.getColumnIndexOrThrow("mStuffStoneType");
                    final int mStuffMetalType1 =
                            c1.getColumnIndexOrThrow("mStuffMetalType");
                    final int mStuffWeightRange1 =
                            c1.getColumnIndexOrThrow("mStuffWeightRange");
                    final int mStuffarea1 =
                            c1.getColumnIndexOrThrow("mStuffarea");
                    final int mstuffcity1 =
                            c1.getColumnIndexOrThrow("mStuffcity");
                    final int mstuffcountry1 =
                            c1.getColumnIndexOrThrow("mStuffcountry");
                    final int mstuffLatitude1 =
                            c1.getColumnIndexOrThrow("mStuffLatitude");
                    final int mstuffLongitude1 =
                            c1.getColumnIndexOrThrow("mStuffLongitude");
                    if (c1 != null)
                    {
                        if (c1.isFirst())
                        {
                            myDB.execSQL(
                                    "delete from mStuffdetails where catagory='Jewelry' OR catagory='Marker';");
                            do
                            {
                                id = c1.getString(mstuffId1);
                                catagory = c1.getString(mcatagory1);
                                String jewelrymake =
                                        c1.getString(mStuffJewelryMake1);
                                String jewelrygender =
                                        c1.getString(mStuffJewelryGender1);
                                String stonetype =
                                        c1.getString(mStuffStoneType1);
                                String metaltype =
                                        c1.getString(mStuffMetalType1);
                                String weightrange =
                                        c1.getString(mStuffWeightRange1);
                                String area = c1.getString(mStuffarea1);
                                String city = c1.getString(mstuffcity1);
                                String country = c1.getString(mstuffcountry1);
                                location = area + ", " + city + ", " + country;
                                details = jewelrymake + jewelrygender +
                                        stonetype + metaltype + weightrange +
                                        ",Location=" + location;
                                dblatitude = c1.getString(mstuffLatitude1);
                                dblongitude = c1.getString(mstuffLongitude1);
                                lat = Double.parseDouble(dblatitude);
                                lon = Double.parseDouble(dblongitude);
                                lat = lat * 1000000;
                                lon = lon * 1000000;
                                latitude = (int) lat;
                                longitude = (int) lon;

                                myDB.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');");

                            }
                            while (c1.moveToNext());
                        }
                    }
                    c1.close();
                }
                catch (Exception e)
                {

                }
                //Displaying MStuffRental in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    String[] cols2 = {"mStuffId", "mCatagory",
                            " mStuffRentalType", "mStuffMisc", "mStuffRate",
                            "mStuffStatus", "mStuffCountry", "mStuffCity",
                            "mStuffArea", "mStuffLatitude", "mStuffLongitude"};
                    Cursor c2 = myDB.query("MStuffrental", cols2, null,
                            null, null,
                            null, null);
                    final int mstuffId3 = c2.getColumnIndexOrThrow("mStuffId");
                    final int mcatagory3 =
                            c2.getColumnIndexOrThrow("mCatagory");
                    final int mStuffRentalType3 =
                            c2.getColumnIndexOrThrow("mStuffRentalType");
                    final int mStuffMisc3 =
                            c2.getColumnIndexOrThrow("mStuffMisc");
                    final int mStuffRate3 =
                            c2.getColumnIndexOrThrow("mStuffRate");
                    final int mStuffStatus3 =
                            c2.getColumnIndexOrThrow("mStuffStatus");
                    final int mStuffCountry3 =
                            c2.getColumnIndexOrThrow("mStuffCountry");
                    final int mStuffCity3 =
                            c2.getColumnIndexOrThrow("mStuffCity");
                    final int mStuffArea3 =
                            c2.getColumnIndexOrThrow("mStuffArea");
                    final int mStuffLatitude3 =
                            c2.getColumnIndexOrThrow("mStuffLatitude");
                    final int mStuffLongitude3 =
                            c2.getColumnIndexOrThrow("mStuffLongitude");

                    if (c2 != null)
                    {
                        if (c2.isFirst())
                        {
                            myDB.execSQL(
                                    "delete from mStuffdetails where catagory='Rental' OR catagory='Marker';");
                            do
                            {
                                id = c2.getString(mstuffId3);
                                catagory = c2.getString(mcatagory3);
                                String rentaltype =
                                        c2.getString(mStuffRentalType3);
                                String rentalmisc = c2.getString(mStuffMisc3);
                                String rentalrate = c2.getString(mStuffRate3);
                                String rentalstatus =
                                        c2.getString(mStuffStatus3);
                                String city = c2.getString(mStuffCity3);
                                String area = c2.getString(mStuffArea3);
                                String country = c2.getString(mStuffCountry3);
                                location = area + ", " + city + ", " + country;
                                details = rentaltype + rentalmisc + rentalrate +
                                        rentalstatus + ",Location=" + location;
                                dblatitude = c2.getString(mStuffLatitude3);
                                dblongitude = c2.getString(mStuffLongitude3);
                                lat = Double.parseDouble(dblatitude);
                                lon = Double.parseDouble(dblongitude);
                                lat = lat * 1000000;
                                lon = lon * 1000000;
                                latitude = (int) lat;
                                longitude = (int) lon;

                                myDB.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');");

                            }
                            while (c2.moveToNext());
                        }
                    }
                    c2.close();
                }
                catch (Exception e)
                {
                }
                //Displaying MStuffRestaurants in Map
                try
                {

                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    String[] cols1 = {"mStuffId", "mCatagory",
                            "mStuffCuisineType", "mStuffCookingMethod",
                            "mStuffDietetic",
                            "mStuffCourseType", "mStuffDishType",
                            "mStuffMainIngredient",
                            "mStuffOccasionOrSeason", "mStuffMiscellaneous",
                            "mStuffArea", "mStuffCity", "mStuffCountry",
                            "mStuffLatitude", "mStuffLongitude"
                    };
                    Cursor c1 = myDB.query("MStuffRestaurants", cols1,
                            null, null, null,
                            null, null);
                    final int mstuffId1 = c1.getColumnIndexOrThrow("mStuffId");
                    final int mcatagory1 =
                            c1.getColumnIndexOrThrow("mCatagory");
                    final int mStuffCuisineType =
                            c1.getColumnIndexOrThrow("mStuffCuisineType");
                    final int mStuffCookingMethod =
                            c1.getColumnIndexOrThrow("mStuffCookingMethod");
                    final int mStuffDietetic =
                            c1.getColumnIndexOrThrow("mStuffDietetic");
                    final int mStuffCourseType =
                            c1.getColumnIndexOrThrow("mStuffCourseType");
                    final int mStuffDishType =
                            c1.getColumnIndexOrThrow("mStuffDishType");
                    final int mStuffMainIngredient =
                            c1.getColumnIndexOrThrow("mStuffMainIngredient");
                    final int mStuffOccasionOrSeason =
                            c1.getColumnIndexOrThrow("mStuffOccasionOrSeason");
                    final int mStuffMiscellaneous =
                            c1.getColumnIndexOrThrow("mStuffMiscellaneous");
                    final int mStuffArea =
                            c1.getColumnIndexOrThrow("mStuffArea");
                    final int mStuffCity =
                            c1.getColumnIndexOrThrow("mStuffCity");
                    final int mStuffCountry =
                            c1.getColumnIndexOrThrow("mStuffCountry");
                    final int mstuffLatitude1 =
                            c1.getColumnIndexOrThrow("mStuffLatitude");
                    final int mstuffLongitude1 =
                            c1.getColumnIndexOrThrow("mStuffLongitude");
                    if (c1 != null)
                    {
                        if (c1.isFirst())
                        {
                            myDB.execSQL(
                                    "delete from mStuffdetails where catagory='Restaurants' OR catagory='Marker';");
                            do
                            {
                                id = c1.getString(mstuffId1);
                                catagory = c1.getString(mcatagory1);
                                String cuisineType =
                                        c1.getString(mStuffCuisineType);
                                String cookingmethod =
                                        c1.getString(mStuffCookingMethod);
                                String dietetic = c1.getString(mStuffDietetic);
                                String courseType =
                                        c1.getString(mStuffCourseType);
                                String dishType = c1.getString(mStuffDishType);
                                String mainIngredient =
                                        c1.getString(mStuffMainIngredient);
                                String occasionOrSeason =
                                        c1.getString(mStuffOccasionOrSeason);
                                String Miscelleneous =
                                        c1.getString(mStuffMiscellaneous);
                                String area = c1.getString(mStuffArea);
                                String city = c1.getString(mStuffCity);
                                String country = c1.getString(mStuffCountry);
                                location = area + ", " + city + ", " + country;
                                details = cuisineType + cookingmethod +
                                        dietetic + courseType + dishType +
                                        mainIngredient + occasionOrSeason +
                                        Miscelleneous + ",Location=" + location;
                                dblatitude = c1.getString(mstuffLatitude1);
                                dblongitude = c1.getString(mstuffLongitude1);
                                lat = Double.parseDouble(dblatitude);
                                lon = Double.parseDouble(dblongitude);
                                lat = lat * 1000000;
                                lon = lon * 1000000;
                                latitude = (int) lat;
                                longitude = (int) lon;

                                myDB.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');");

                            }
                            while (c1.moveToNext());
                        }
                    }
                    c1.close();
                }
                catch (Exception e)
                {
                }
                //Displaying MStuffmovies in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    String[] cols1 = {"mStuffId", "mCatagory",
                            "mStuffMovieType", "mStuffMovieLanguage",
                            "mStuffSeatingStyle",
                            "mStuffMovieArea", "mStuffMovieCity",
                            "mStuffMovieCountry", "mStuffMovieLatitude",
                            "mStuffMovieLongitude"
                    };
                    Cursor c1 = myDB.query("MStuffMovies", cols1, null,
                            null, null,
                            null, null);
                    final int mstuffId = c1.getColumnIndexOrThrow("mStuffId");
                    final int mcatagory = c1.getColumnIndexOrThrow("mCatagory");
                    final int mStuffMovieType =
                            c1.getColumnIndexOrThrow("mStuffMovieType");
                    final int mStuffMovieLanguage =
                            c1.getColumnIndexOrThrow("mStuffMovieLanguage");
                    final int mStuffSeatingStyle =
                            c1.getColumnIndexOrThrow("mStuffSeatingStyle");
                    final int mStuffMovieArea =
                            c1.getColumnIndexOrThrow("mStuffMovieArea");
                    final int mStuffMovieCity =
                            c1.getColumnIndexOrThrow("mStuffMovieCity");
                    final int mStuffMovieCountry =
                            c1.getColumnIndexOrThrow("mStuffMovieCountry");
                    final int mStuffMovieLatitude =
                            c1.getColumnIndexOrThrow("mStuffMovieLatitude");
                    final int mStuffMovieLongitude =
                            c1.getColumnIndexOrThrow("mStuffMovieLongitude");
                    myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);");

                    if (c1 != null)
                    {
                        if (c1.isFirst())
                        {
                            myDB.execSQL(
                                    "delete from mStuffdetails where catagory='Movies' OR catagory='Marker';");
                            do
                            {
                                id = c1.getString(mstuffId);
                                catagory = c1.getString(mcatagory);
                                String movieType =
                                        c1.getString(mStuffMovieType);
                                String movieLanguage =
                                        c1.getString(mStuffMovieLanguage);
                                String seatingStyle =
                                        c1.getString(mStuffSeatingStyle);
                                String area = c1.getString(mStuffMovieArea);
                                String city = c1.getString(mStuffMovieCity);
                                String country =
                                        c1.getString(mStuffMovieCountry);

                                location = area + ", " + city + ", " + country;
                                details = movieType + movieLanguage +
                                        seatingStyle + ",Location=" + location;
                                dblatitude = c1.getString(mStuffMovieLatitude);
                                dblongitude =
                                        c1.getString(mStuffMovieLongitude);
                                lat = Double.parseDouble(dblatitude);
                                lon = Double.parseDouble(dblongitude);
                                lat = lat * 1000000;
                                lon = lon * 1000000;
                                latitude = (int) lat;
                                longitude = (int) lon;

                                myDB.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');");

                            }
                            while (c1.moveToNext());
                        }
                    }
                    c1.close();
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
                //Showing Notification Message
                CharSequence from;
                CharSequence message;
                String tickerText;
                if (response.contains("iarea"))
                {
                    NotificationManager nm = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    from = "Matching Data";
                    message = "Matches Received";
                    tickerText = "Matches Received";
                    Intent appIntent = new Intent();
                    Intent contentIntent =
                            new Intent(context, StatusbarNotification.class);
//    public Notification(Context context, int icon, CharSequence tickerText,
// long when, CharSequence contentTitle, CharSequence contentText,
//                    Intent contentIntent)

                    Notification notif = new Notification(context,
                            R.drawable.mobeegal1,
                            tickerText,
                            System.currentTimeMillis(),
                            from,
                            message,
                            contentIntent);
                    notif.vibrate = new long[]{100, 250, 100, 500};
                    nm.notify(R.string.notification_message, notif);
                }
                Looper.loop();
                Looper.myLooper().quit();
            }
        }.start();
    }

    void dating()
    {
        try
        {

            Cursor dating_Cursor = myDB.query(mY_Dating_TABLE, null,
                    null, null, null, null, null);
            if (dating_Cursor.getCount() > 0)
            {
                try
                {
                    Cursor datingCursor =
                            myDB.query(mY_Dating_TABLE, null,
                                    "queryStatus='true'", null, null, null,
                                    null);
                    int iStuffageColumn =
                            datingCursor.getColumnIndexOrThrow("iage");
                    int iStuffsexColumn =
                            datingCursor.getColumnIndexOrThrow("isex");
                    int iStuffHeightColumn =
                            datingCursor.getColumnIndexOrThrow("iheight");
                    int iStuffWeightColumn =
                            datingCursor.getColumnIndexOrThrow("iweight");
                    int iStuffLocationColumn =
                            datingCursor.getColumnIndexOrThrow("iarea");
                    int iStuffCityColumn =
                            datingCursor.getColumnIndexOrThrow("icity");
                    int iStuffCountryColumn =
                            datingCursor.getColumnIndexOrThrow("icountry");
                    int iStufflatitudeColumn =
                            datingCursor.getColumnIndexOrThrow("ilatitude");
                    int iStufflongitudeColumn =
                            datingCursor.getColumnIndexOrThrow("ilongitude");

                    int uStuffageColumn =
                            datingCursor.getColumnIndexOrThrow("uage");
                    int uStuffsexColumn =
                            datingCursor.getColumnIndexOrThrow("usex");
                    int uStuffHeightColumn =
                            datingCursor.getColumnIndexOrThrow("uheight");
                    int uStuffWeightColumn =
                            datingCursor.getColumnIndexOrThrow("uweight");
                    int uStuffLocationColumn =
                            datingCursor.getColumnIndexOrThrow("uarea");
                    int uStuffCityColumn =
                            datingCursor.getColumnIndexOrThrow("ucity");
                    int uStuffCountryColumn =
                            datingCursor.getColumnIndexOrThrow("ucountry");
                    int uStufflatitudeColumn =
                            datingCursor.getColumnIndexOrThrow("ulatitude");
                    int uStufflongitudeColumn =
                            datingCursor.getColumnIndexOrThrow("ulongitude");

                    // Getting Dating Column
                    String datingcolumn[] = datingCursor.getColumnNames();
                    String datingcolumn1[] = null;
                    String datingcolumn2[] = null;
                    for (int j = 1; j < datingcolumn.length - 2; j++)
                    {
                        datingcolumnal.add(datingcolumn[j]);
                        logger.info(
                                "datingal01 " + j + " = " + datingcolumn[j]);
                    }
                    if (datingcolumnal.size() > 1)
                    {
                        datingcolumn1 = extract1(datingcolumn, 0,
                                datingcolumn.length / 2);
                        datingcolumn2 = extract1(datingcolumn,
                                datingcolumn.length / 2, datingcolumn.length);
                    }
                    for (int j = 1; j < datingcolumn1.length; j++)
                    {
                        //for (int j = 1; j < 10; j++) {
                        idatingcolumnal.add(datingcolumn1[j]);
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                        logger.info("idatingcolumnal " + j + " = " +
                                datingcolumn[j]);
                    }
                    for (int j = 0; j < datingcolumn2.length - 2; j++)
                    {
                        //for (int j = 1; j <10; j++) {
                        udatingcolumnal.add(datingcolumn2[j]);
                        logger.info("udatingcolumnal " + j + " = " +
                                datingcolumn[j]);
                    }
                    // Length
                    int idatingcolumnalLen = idatingcolumnal.size();
                    int udatingcolumnalLen = udatingcolumnal.size();
                    // Check if our result was valid.
                    if (datingCursor != null)
                    {

                        if (datingCursor.isFirst())
                        {

                            do
                            {

                                iStuffAge =
                                        datingCursor.getString(iStuffageColumn);
                                idatingal.add(iStuffAge);

                                iStuffSex =
                                        datingCursor.getString(iStuffsexColumn);
                                idatingal.add(iStuffSex);

                                iStuffHeight = datingCursor
                                        .getString(iStuffHeightColumn);
                                idatingal.add(iStuffHeight);

                                iStuffWeight = datingCursor
                                        .getString(iStuffWeightColumn);
                                idatingal.add(iStuffWeight);

                                iStuffLocation = datingCursor
                                        .getString(iStuffLocationColumn);
                                idatingal.add(iStuffLocation);

                                iStuffCity = datingCursor
                                        .getString(iStuffCityColumn);
                                idatingal.add(iStuffCity);

                                iStuffCountry = datingCursor
                                        .getString(iStuffCountryColumn);
                                idatingal.add(iStuffCountry);

                                iStufflatitude = datingCursor
                                        .getString(iStufflatitudeColumn);
                                idatingal.add(iStufflatitude);

                                iStufflongitude = datingCursor
                                        .getString(iStufflongitudeColumn);
                                idatingal.add(iStufflongitude);


                                uStuffAge =
                                        datingCursor.getString(uStuffageColumn);
                                udatingal.add(uStuffAge);

                                uStuffSex =
                                        datingCursor.getString(uStuffsexColumn);
                                udatingal.add(uStuffSex);

                                uStuffHeight = datingCursor
                                        .getString(uStuffHeightColumn);
                                udatingal.add(uStuffHeight);

                                uStuffWeight = datingCursor
                                        .getString(uStuffWeightColumn);
                                udatingal.add(uStuffWeight);

                                uStuffLocation = datingCursor
                                        .getString(uStuffLocationColumn);
                                udatingal.add(uStuffLocation);

                                uStuffCity = datingCursor
                                        .getString(uStuffCityColumn);
                                udatingal.add(uStuffCity);

                                uStuffCountry = datingCursor
                                        .getString(uStuffCountryColumn);
                                udatingal.add(uStuffCountry);

                                uStufflatitude = datingCursor
                                        .getString(uStufflatitudeColumn);
                                udatingal.add(uStufflatitude);

                                uStufflongitude = datingCursor
                                        .getString(uStufflongitudeColumn);
                                udatingal.add(uStufflongitude);

                                js.object();
                                js.key("iStuff").object();
                                for (int y = 0; y < idatingcolumnalLen; y++)
                                {
                                    //for (int y = 0; y < 9; y++) {
                                    String idatingcolumnalString =
                                            idatingcolumnal.get(y);
                                    logger.info("idating Data" +
                                            idatingcolumnalString + " = " +
                                            idatingal.get(y));
                                    js.key(idatingcolumnalString)
                                            .value(idatingal.get(y));
                                }
                                js.endObject();
                                js.key("uStuff").object();
                                for (int i = 0; i < udatingcolumnalLen; i++)
                                {
                                    //for (int i = 0; i < udatingcolumnalLen; i++) {
                                    String udatingcolumnalString =
                                            udatingcolumnal.get(i);
                                    logger.info("udating Data" +
                                            udatingcolumnalString + "= " +
                                            udatingal.get(i));
                                    js.key(udatingcolumnalString)
                                            .value(udatingal.get(i));
                                }
                                js.endObject();
                                idatingal.clear();
                                udatingal.clear();
                                js.endObject();
                            }
                            while (datingCursor.moveToNext());
                        }
                    }
                    myDB.execSQL(
                            "update category set querystatus='false' where categoryname='Dating'");
                    datingCursor.close();
                }
                catch (JSONException e)
                {
                    logger.info("Error = " + e.getMessage());
                }
            }
        }
        catch (Exception e)
        {

        }
    }

    void datingResponse()
    {
        try
        {
            JSONArray mStuffJsonArray1 = new JSONArray(values);
            int mStuffJsonArraylength1 = mStuffJsonArray1.length();
            for (int l = 0; l <
                    mStuffJsonArraylength1; l++)
            {
                JSONObject mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l);
                JSONArray mStuffinnerJsonArray1 = mStuffinnerJson1.names();
                int mStuffinnerJsonArraylength1 =
                        mStuffinnerJsonArray1.length();

                for (int k = 0; k < mStuffinnerJsonArraylength1; k++)
                {
                    String keys1 = mStuffinnerJsonArray1.getString(k);
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    String values1 = mStuffinnerJson1.getString(keys1);
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1);
                }

                myDB.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffdating" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffAge VARCHAR,mStuffsex VARCHAR, mStuffHeight VARCHAR,mStuffWeight VARCHAR, mStuffArea VARCHAR,mStuffCity VARCHAR, mStuffcountry VARCHAR,mStuffLatitude VARCHAR, mStuffLongitude VARCHAR);");
                myDB.execSQL(
                        "INSERT INTO MStuffdating (mStuffId,mCatagory,mStuffAge,mStuffsex,mStuffHeight,mStuffWeight,mStuffArea,mStuffCity,mStuffcountry,mStuffLatitude,mStuffLongitude)VALUES('" +
                                mStuff0i1.get(7) + "','" + "Dating" + "','" +
                                mStuff0i1.get(6) + "','" + mStuff0i1.get(9) +
                                "','" + mStuff0i1.get(0) + "','" +
                                mStuff0i1.get(1) + "','" + mStuff0i1.get(5) +
                                "','" + mStuff0i1.get(2) + "','" +
                                mStuff0i1.get(8) + "','" + mStuff0i1.get(3) +
                                "','" + mStuff0i1.get(4) + "');");
                mStuff0i1.clear();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
    }

    void matrimony()
    {
        try
        {
            Cursor matrimony_Cursor = myDB.query(mY_Matrimony_TABLE, null,
                    null, null, null, null, null);
            if (matrimony_Cursor.getCount() > 0)
            {
                try
                {

                    Cursor matrimonyCursor =
                            myDB.query(mY_Matrimony_TABLE, null,
                                    "queryStatus='true'", null, null, null,
                                    null);

                    int ireligionColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ireligion");
                    int icasteColumn =
                            matrimonyCursor.getColumnIndexOrThrow("icaste");
                    int iageColumn =
                            matrimonyCursor.getColumnIndexOrThrow("iage");
                    int isexColumn =
                            matrimonyCursor.getColumnIndexOrThrow("isex");
                    int iheightColumn =
                            matrimonyCursor.getColumnIndexOrThrow("iheight");
                    int iweightColumn =
                            matrimonyCursor.getColumnIndexOrThrow("iweight");
                    int icolorColumn =
                            matrimonyCursor.getColumnIndexOrThrow("icolor");
                    int iareaColumn =
                            matrimonyCursor.getColumnIndexOrThrow("iarea");
                    int icityColumn =
                            matrimonyCursor.getColumnIndexOrThrow("icity");
                    int icountryColumn =
                            matrimonyCursor.getColumnIndexOrThrow("icountry");
                    int ilatitudeColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ilatitude");
                    int ilongitudeColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ilongitude");

                    int ureligionColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ureligion");
                    int ucasteColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ucaste");
                    int uageColumn =
                            matrimonyCursor.getColumnIndexOrThrow("uage");
                    int usexColumn =
                            matrimonyCursor.getColumnIndexOrThrow("usex");
                    int uheightColumn =
                            matrimonyCursor.getColumnIndexOrThrow("uheight");
                    int uweightColumn =
                            matrimonyCursor.getColumnIndexOrThrow("uweight");
                    int ucolorColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ucolor");
                    int uareaColumn =
                            matrimonyCursor.getColumnIndexOrThrow("uarea");
                    int ucityColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ucity");
                    int ucountryColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ucountry");
                    int ulatitudeColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ulatitude");
                    int ulongitudeColumn =
                            matrimonyCursor.getColumnIndexOrThrow("ulongitude");

                    // Getting Dating Column
                    String matrimonycolumn[] = matrimonyCursor.getColumnNames();
                    String imatrimonycolumn[] = null;
                    String umatrimonycolumn[] = null;

                    for (int j = 1; j < matrimonycolumn.length - 2; j++)
                    {
                        matrimonycolumnal.add(matrimonycolumn[j]);
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                    }
                    if (matrimonycolumnal.size() > 1)
                    {
                        imatrimonycolumn = extract1(matrimonycolumn, 0,
                                matrimonycolumn.length / 2);
                        umatrimonycolumn = extract1(matrimonycolumn,
                                matrimonycolumn.length / 2,
                                matrimonycolumn.length);
                    }
                    for (int j = 1; j < imatrimonycolumn.length; j++)
                    {
                        iMatrimonycolumnal.add(imatrimonycolumn[j]);
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                    }
                    for (int j = 0; j < umatrimonycolumn.length - 2; j++)
                    {
                        uMatrimonycolumnal.add(umatrimonycolumn[j]);
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                    }
                    // Length
                    int imatrimonycolumnalLen = iMatrimonycolumnal.size();
                    int umatrimonycolumnalLen = uMatrimonycolumnal.size();

                    // Check if our result was valid.
                    if (matrimonyCursor != null)
                    {
                        if (matrimonyCursor.isFirst())
                        {
                            do
                            {
                                iReligion = matrimonyCursor
                                        .getString(ireligionColumn);
                                iMatrimony.add(iReligion);

                                iCaste =
                                        matrimonyCursor.getString(icasteColumn);
                                iMatrimony.add(iCaste);

                                iAge = matrimonyCursor.getString(iageColumn);
                                iMatrimony.add(iAge);

                                iSex = matrimonyCursor.getString(isexColumn);
                                iMatrimony.add(iSex);

                                iHeight = matrimonyCursor
                                        .getString(iheightColumn);
                                iMatrimony.add(iHeight);

                                iWeight = matrimonyCursor
                                        .getString(iweightColumn);
                                iMatrimony.add(iWeight);

                                iColor =
                                        matrimonyCursor.getString(icolorColumn);
                                iMatrimony.add(iColor);

                                iArea = matrimonyCursor.getString(iareaColumn);
                                iMatrimony.add(iArea);

                                iCity = matrimonyCursor.getString(icityColumn);
                                iMatrimony.add(iCity);

                                iCountry = matrimonyCursor
                                        .getString(icountryColumn);
                                iMatrimony.add(iCountry);

                                iLatitude = matrimonyCursor
                                        .getString(ilatitudeColumn);
                                iMatrimony.add(iLatitude);

                                iLongitude = matrimonyCursor
                                        .getString(ilongitudeColumn);
                                iMatrimony.add(iLongitude);

                                uReligion = matrimonyCursor
                                        .getString(ureligionColumn);
                                uMatrimony.add(uReligion);

                                uCaste =
                                        matrimonyCursor.getString(ucasteColumn);
                                uMatrimony.add(uCaste);

                                uAge = matrimonyCursor.getString(uageColumn);
                                uMatrimony.add(uAge);

                                uSex = matrimonyCursor.getString(usexColumn);
                                uMatrimony.add(uSex);

                                uHeight = matrimonyCursor
                                        .getString(uheightColumn);
                                uMatrimony.add(uHeight);

                                uWeight = matrimonyCursor
                                        .getString(uweightColumn);
                                uMatrimony.add(uWeight);

                                uColor =
                                        matrimonyCursor.getString(ucolorColumn);
                                uMatrimony.add(uColor);

                                uArea = matrimonyCursor.getString(uareaColumn);
                                uMatrimony.add(uArea);

                                uCity = matrimonyCursor.getString(ucityColumn);
                                uMatrimony.add(uCity);

                                uCountry = matrimonyCursor
                                        .getString(ucountryColumn);
                                uMatrimony.add(uCountry);

                                uLatitude = matrimonyCursor
                                        .getString(ulatitudeColumn);
                                uMatrimony.add(uLatitude);

                                uLongitude = matrimonyCursor
                                        .getString(ulongitudeColumn);
                                uMatrimony.add(uLongitude);

                                js.object();
                                js.key("iStuff").object();
                                for (int y = 0; y < imatrimonycolumnalLen; y++)
                                {
                                    String imatrimonycolumnalString =
                                            iMatrimonycolumnal.get(y);
                                    // logger.info("dating Data" +idatingcolumnalString // + " = " + iMatrimony.get(y));
                                    js.key(imatrimonycolumnalString)
                                            .value(iMatrimony.get(y));
                                }
                                js.endObject();
                                js.key("uStuff").object();
                                for (int i = 0; i < umatrimonycolumnalLen; i++)
                                {
                                    String umatrimonycolumnalString =
                                            uMatrimonycolumnal.get(i);
                                    // logger.info("dating Data" + udatingcolumnalString
                                    // + " = " + uMatrimony.get(i));
                                    js.key(umatrimonycolumnalString)
                                            .value(uMatrimony.get(i));
                                }
                                js.endObject();
                                iMatrimony.clear();
                                uMatrimony.clear();
                                js.endObject();
                            }
                            while (matrimonyCursor.moveToNext());
                        }
                        myDB.execSQL(
                                "update category set querystatus='false' where categoryname='Matrimony'");
                        matrimonyCursor.close();
                    }

                }
                catch (JSONException e)
                {
                    // logger.info("Error = " + e.getMessage());
                }
                matrimonycolumnal.clear();
                iMatrimonycolumnal.clear();
                uMatrimonycolumnal.clear();
            }

        }
        catch (Exception e)
        {

        }
    }

    void matrimonyResponse()
    {
        try
        {
            JSONArray mStuffJsonArray1 = new JSONArray(values);
            int mStuffJsonArraylength1 = mStuffJsonArray1.length();
            for (int l = 0; l < mStuffJsonArraylength1; l++)
            {
                JSONObject mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l);
                JSONArray mStuffinnerJsonArray1 = mStuffinnerJson1.names();
                int mStuffinnerJsonArraylength1 =
                        mStuffinnerJsonArray1.length();

                for (int k = 0; k < mStuffinnerJsonArraylength1; k++)
                {
                    String keys1 = mStuffinnerJsonArray1.getString(k);
                    // logger.info("mstuff arry key1:" + k + ":" + keys1);
                    String values1 = mStuffinnerJson1.getString(keys1);
                    // logger.info("mstuff arry value1:" + k +":" + values1);
                    mStuff0i1.add(values1);
                }
                myDB.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffmatrimony" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mReligion VARCHAR,mCaste VARCHAR,mStuffAge VARCHAR,mStuffsex VARCHAR,mStuffHeight VARCHAR,mStuffWeight VARCHAR,mColor VARCHAR,mStuffArea VARCHAR,mStuffCity VARCHAR,mStuffcountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR);");
                myDB.execSQL(
                        "INSERT INTO MStuffmatrimony (mStuffId,mCatagory,mReligion,mCaste, mStuffAge, mStuffsex ,mStuffHeight,mStuffWeight ,mColor,mStuffArea ,mStuffCity ,mStuffcountry ,mStuffLatitude,mStuffLongitude) VALUES ('" +
                                mStuff0i1.get(10) + "','" + "Matrimony" +
                                "','" + mStuff0i1.get(8) + "','" +
                                mStuff0i1.get(11) + "','" + mStuff0i1.get(6) +
                                "','" + mStuff0i1.get(12) + "','" +
                                mStuff0i1.get(3) + "','" + mStuff0i1.get(0) +
                                "','" + mStuff0i1.get(9) + "','" +
                                mStuff0i1.get(7) + "','" + mStuff0i1.get(4) +
                                "','" + mStuff0i1.get(2) + "','" +
                                mStuff0i1.get(5) + "','" + mStuff0i1.get(1) +
                                "');");
                mStuff0i1.clear();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
    }

    void cars()
    {
        try
        {
            Cursor cars_Cursor = myDB.query(mY_Cars_TABLE, null,
                    null, null, null, null, null);
            if (cars_Cursor.getCount() > 0)
            {
                try
                {
                    Cursor carsCursor = myDB.query(mY_Cars_TABLE, null,
                            "queryStatus='true'", null, null, null, null);
                    int imakeColumn = carsCursor.getColumnIndexOrThrow("imake");
                    int imodelColumn =
                            carsCursor.getColumnIndexOrThrow("imodel");
                    int iyearColumn = carsCursor.getColumnIndexOrThrow("iyear");
                    int icolorColumn =
                            carsCursor.getColumnIndexOrThrow("icolor");
                    int ifuel_typeColumn =
                            carsCursor.getColumnIndexOrThrow("ifuel_type");
                    int ipriceColumn =
                            carsCursor.getColumnIndexOrThrow("iprice");
                    int iareaColumn = carsCursor.getColumnIndexOrThrow("iarea");
                    int icityColumn = carsCursor.getColumnIndexOrThrow("icity");
                    int icountryColumn =
                            carsCursor.getColumnIndexOrThrow("icountry");
                    int ilatitudeColumn =
                            carsCursor.getColumnIndexOrThrow("ilatitude");
                    int ilongitudeColumn =
                            carsCursor.getColumnIndexOrThrow("ilongitude");

                    int umakeColumn = carsCursor.getColumnIndexOrThrow("umake");
                    int umodelColumn =
                            carsCursor.getColumnIndexOrThrow("umodel");
                    int uyearColumn = carsCursor.getColumnIndexOrThrow("uyear");
                    int ucolorColumn =
                            carsCursor.getColumnIndexOrThrow("ucolor");
                    int ufuel_typeColumn =
                            carsCursor.getColumnIndexOrThrow("ufuel_type");
                    int upriceColumn =
                            carsCursor.getColumnIndexOrThrow("uprice");
                    int uareaColumn = carsCursor.getColumnIndexOrThrow("uarea");
                    int ucityColumn = carsCursor.getColumnIndexOrThrow("ucity");
                    int ucountryColumn =
                            carsCursor.getColumnIndexOrThrow("ucountry");
                    int ulatitudeColumn =
                            carsCursor.getColumnIndexOrThrow("ulatitude");
                    int ulongitudeColumn =
                            carsCursor.getColumnIndexOrThrow("ulongitude");
                    // Getting Cars Column
                    String carscolumn[] = carsCursor.getColumnNames();
                    String icarscolumn[] = null;
                    String ucarscolumn[] = null;

                    for (int j = 1; j < carscolumn.length - 2; j++)
                    {
                        carscolumnal.add(carscolumn[j]);
                        //logger.info("carscolumns:" + carscolumn[j]);
                        // Log.i(".....................", carscolumn[j]);
                    }

                    if (carscolumnal.size() > 1)
                    {
                        icarscolumn =
                                extract1(carscolumn, 0, carscolumn.length / 2);
                        ucarscolumn = extract1(carscolumn,
                                carscolumn.length / 2, carscolumn.length);
                    }

                    for (int j = 1; j < icarscolumn.length; j++)
                    {
                        iCarscolumnal.add(icarscolumn[j]);
                        //logger.info("icarscolumnal:" + icarscolumn[j]);
                    }

                    for (int j = 0; j < ucarscolumn.length - 2; j++)
                    {
                        uCarscolumnal.add(ucarscolumn[j]);
                        // logger.info("ucarscolumnal:" + ucarscolumn[j]);
                    }
                    // Length
                    int icarscolumnalLen = iCarscolumnal.size();
                    int ucarscolumnalLen = uCarscolumnal.size();

                    // Check if our result was valid.
                    if (carsCursor != null)
                    {

                        if (carsCursor.isFirst())
                        {
                            do
                            {
                                iCarMake = carsCursor.getString(imakeColumn);
                                iCars.add(iCarMake);

                                iCarModel = carsCursor.getString(imodelColumn);
                                iCars.add(iCarModel);

                                iCarYear = carsCursor.getString(iyearColumn);
                                iCars.add(iCarYear);

                                iCarColor = carsCursor.getString(icolorColumn);
                                iCars.add(iCarColor);

                                iCarFuel_Type =
                                        carsCursor.getString(ifuel_typeColumn);
                                iCars.add(iCarFuel_Type);

                                iCarPrice = carsCursor.getString(ipriceColumn);
                                iCars.add(iCarPrice);

                                iCarArea = carsCursor.getString(iareaColumn);
                                iCars.add(iCarArea);

                                iCarCity = carsCursor.getString(icityColumn);
                                iCars.add(iCarCity);

                                iCarCountry =
                                        carsCursor.getString(icountryColumn);
                                iCars.add(iCarCountry);

                                iCarLatitude =
                                        carsCursor.getString(ilatitudeColumn);
                                iCars.add(iCarLatitude);

                                iCarLongitude =
                                        carsCursor.getString(ilongitudeColumn);
                                iCars.add(iCarLongitude);

                                uCarMake = carsCursor.getString(umakeColumn);
                                uCars.add(uCarMake);

                                uCarModel = carsCursor.getString(umodelColumn);
                                uCars.add(uCarModel);

                                uCarYear = carsCursor.getString(uyearColumn);
                                uCars.add(uCarYear);

                                uCarColor = carsCursor.getString(ucolorColumn);
                                uCars.add(uCarColor);

                                uCarFuel_Type =
                                        carsCursor.getString(ufuel_typeColumn);
                                uCars.add(uCarFuel_Type);

                                uCarPrice = carsCursor.getString(upriceColumn);
                                uCars.add(uCarPrice);

                                uCarArea = carsCursor.getString(uareaColumn);
                                uCars.add(uCarArea);

                                uCarCity = carsCursor.getString(ucityColumn);
                                uCars.add(uCarCity);

                                uCarCountry =
                                        carsCursor.getString(ucountryColumn);
                                uCars.add(uCarCountry);

                                uCarLatitude =
                                        carsCursor.getString(ulatitudeColumn);
                                uCars.add(uCarLatitude);

                                uCarLongitude =
                                        carsCursor.getString(ulongitudeColumn);
                                uCars.add(uCarLongitude);

                                js.object();
                                js.key("iStuff").object();

                                for (int y = 0; y < icarscolumnalLen; y++)
                                {
                                    String icarscolumnalString =
                                            iCarscolumnal.get(y);
                                    // logger.info("icarscolumnalstring" + icarscolumnalString + " = " + iCars.get(y));
                                    js.key(icarscolumnalString)
                                            .value(iCars.get(y));
                                }
                                js.endObject();
                                js.key("uStuff").object();
                                for (int i = 0; i < ucarscolumnalLen; i++)
                                {
                                    String ucarscolumnalString =
                                            uCarscolumnal.get(i);
                                    //logger.info("ucarscolumnalstring" + ucarscolumnalString + " = " + uCars.get(i));
                                    js.key(ucarscolumnalString)
                                            .value(uCars.get(i));
                                }
                                js.endObject();
                                js.endObject();
                                iCars.clear();
                                uCars.clear();

                            }
                            while (carsCursor.moveToNext());
                        }
                        myDB.execSQL(
                                "update category set querystatus='false' where categoryname='Cars'");
                        carsCursor.close();
                    }
                }
                catch (JSONException e)
                {
                    logger.info("Error = " + e.getMessage());
                }
                carscolumnal.clear();
                iCarscolumnal.clear();
                uCarscolumnal.clear();
            }

        }
        catch (Exception e)
        {

        }
    }

    private void carsResponse()
    {
        try
        {
            JSONArray mStuffJsonArray1 = new JSONArray(values);
            int mStuffJsonArraylength1 = mStuffJsonArray1.length();
            for (int l = 0; l < mStuffJsonArraylength1; l++)
            {
                JSONObject mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l);
                JSONArray mStuffinnerJsonArray1 = mStuffinnerJson1.names();

                int mStuffinnerJsonArraylength1 =
                        mStuffinnerJsonArray1.length();
                for (int k = 0; k < mStuffinnerJsonArraylength1; k++)
                {
                    String keys1 = mStuffinnerJsonArray1.getString(k);
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    String values1 = mStuffinnerJson1.getString(keys1);
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1);
                    logger.info(mStuff0i1.get(k));
                }
                myDB.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffcars" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffmake VARCHAR,mStuffmodel VARCHAR,mStuffyear NUMERIC,mStuffcolor VARCHAR,mStufffuel_type VARCHAR,mStuffprice VARCHAR,mStuffarea VARCHAR,mStuffcity VARCHAR,mStuffcountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );");
                myDB.execSQL(
                        "INSERT INTO MStuffcars (mStuffId,mCatagory,mStuffmake,mStuffmodel,mStuffyear,mStuffcolor,mStufffuel_type,mStuffprice,mStuffarea,mStuffcity,mStuffcountry,mStuffLatitude,mStuffLongitude) VALUES ('" +
                                mStuff0i1.get(10) + "','" + "Cars" + "','" +
                                mStuff0i1.get(7) + "','" + mStuff0i1.get(1) +
                                "','" + mStuff0i1.get(6) + "','" +
                                mStuff0i1.get(9) + "','" + mStuff0i1.get(8) +
                                "','" + mStuff0i1.get(0) + "','" +
                                mStuff0i1.get(5) + "','" + mStuff0i1.get(2) +
                                "','" + mStuff0i1.get(11) + "','" +
                                mStuff0i1.get(3) + "','" + mStuff0i1.get(4) +
                                "');");
                mStuff0i1.clear();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
    }

    private void jewelry()
    {
        try
        {
            Cursor jewelry_Cursor = myDB.query(mY_Jewelry_TABLE, null,
                    null, null, null, null, null);
            if (jewelry_Cursor.getCount() > 0)
            {
                try
                {
                    Cursor jewelryCursor = myDB.query(mY_Jewelry_TABLE,
                            null, "queryStatus='true'", null, null, null, null);
                    int iStuffitemtypeColumn =
                            jewelryCursor.getColumnIndexOrThrow("ijewelry");
                    int iStuffgenderColumn =
                            jewelryCursor.getColumnIndexOrThrow("igender");
                    int iStuffstonetypeColumn =
                            jewelryCursor.getColumnIndexOrThrow("istone");
                    int iStuffmetaltypeColumn =
                            jewelryCursor.getColumnIndexOrThrow("imetal");
                    int iStuffweightColumn =
                            jewelryCursor.getColumnIndexOrThrow("iweight");
                    int iStuffareaColumn =
                            jewelryCursor.getColumnIndexOrThrow("iarea");
                    int iStuffcityColumn =
                            jewelryCursor.getColumnIndexOrThrow("icity");
                    int iStuffcountryColumn =
                            jewelryCursor.getColumnIndexOrThrow("icountry");
                    int iStufflatitudeColumn =
                            jewelryCursor.getColumnIndexOrThrow("ilatitude");
                    int iStufflongitudeColumn =
                            jewelryCursor.getColumnIndexOrThrow("ilongitude");

                    int uStuffitemtypeColumn =
                            jewelryCursor.getColumnIndexOrThrow("ujewelry");
                    int uStuffgenderColumn =
                            jewelryCursor.getColumnIndexOrThrow("ugender");
                    int uStuffstonetypeColumn =
                            jewelryCursor.getColumnIndexOrThrow("ustone");
                    int uStuffmetaltypeColumn =
                            jewelryCursor.getColumnIndexOrThrow("umetal");
                    int uStuffweightColumn =
                            jewelryCursor.getColumnIndexOrThrow("uweight");
                    int uStuffareaColumn =
                            jewelryCursor.getColumnIndexOrThrow("uarea");
                    int uStuffcityColumn =
                            jewelryCursor.getColumnIndexOrThrow("ucity");
                    int uStuffcountryColumn =
                            jewelryCursor.getColumnIndexOrThrow("ucountry");
                    int uStufflatitudeColumn =
                            jewelryCursor.getColumnIndexOrThrow("ulatitude");
                    int uStufflongitudeColumn =
                            jewelryCursor.getColumnIndexOrThrow("ulongitude");
                    // Getting Jewelry Column
                    String jewelrycolumn[] = jewelryCursor.getColumnNames();
                    String ijewelrycolumn[] = null;
                    String ujewelrycolumn[] = null;
                    for (int j = 1; j < jewelrycolumn.length - 2; j++)
                    {
                        jewelrycolumnal.add(jewelrycolumn[j]);
                        logger.info("jewelrycolumn:" + jewelrycolumn[j]);
                    }

                    if (jewelrycolumnal.size() > 1)
                    {
                        ijewelrycolumn = extract1(jewelrycolumn, 0,
                                jewelrycolumn.length / 2);
                        ujewelrycolumn = extract1(jewelrycolumn,
                                jewelrycolumn.length / 2, jewelrycolumn.length);
                    }
                    for (int j = 1; j < ijewelrycolumn.length; j++)
                    {
                        iJewelrycolumnal.add(ijewelrycolumn[j]);
                        logger.info("ijewelrycolumn:" + ijewelrycolumn[j]);
                    }

                    for (int j = 0; j < ujewelrycolumn.length - 2; j++)
                    {
                        uJewelrycolumnal.add(ujewelrycolumn[j]);
                        logger.info("ujewelrycolumn:" + ujewelrycolumn[j]);
                    }
                    // Length
                    int ijewelrycolumnalLen = iJewelrycolumnal.size();
                    int ujewelrycolumnalLen = uJewelrycolumnal.size();

                    if (jewelryCursor != null)
                    {
                        if (jewelryCursor.isFirst())
                        {
                            do
                            {
                                iJewelryType = jewelryCursor
                                        .getString(iStuffitemtypeColumn);
                                iJewelry.add(iJewelryType);

                                iGender = jewelryCursor
                                        .getString(iStuffgenderColumn);
                                iJewelry.add(iGender);

                                iStoneType = jewelryCursor
                                        .getString(iStuffstonetypeColumn);
                                iJewelry.add(iStoneType);

                                iMetalType = jewelryCursor
                                        .getString(iStuffmetaltypeColumn);
                                iJewelry.add(iMetalType);

                                iJewelryWeight = jewelryCursor
                                        .getString(iStuffweightColumn);
                                iJewelry.add(iJewelryWeight);

                                iJewelryArea = jewelryCursor
                                        .getString(iStuffareaColumn);
                                iJewelry.add(iJewelryArea);

                                iJewelryCity = jewelryCursor
                                        .getString(iStuffcityColumn);
                                iJewelry.add(iJewelryCity);

                                iJewelryCountry = jewelryCursor
                                        .getString(iStuffcountryColumn);
                                iJewelry.add(iJewelryCountry);

                                iJewelryLatitude = jewelryCursor
                                        .getString(iStufflatitudeColumn);
                                iJewelry.add(iJewelryLatitude);

                                iJewelryLongitude = jewelryCursor
                                        .getString(iStufflongitudeColumn);
                                iJewelry.add(iJewelryLongitude);

                                uJewelryType = jewelryCursor
                                        .getString(uStuffitemtypeColumn);
                                uJewelry.add(uJewelryType);

                                uGender = jewelryCursor
                                        .getString(uStuffgenderColumn);
                                uJewelry.add(uGender);

                                uStoneType = jewelryCursor
                                        .getString(uStuffstonetypeColumn);
                                uJewelry.add(uStoneType);

                                uMetalType = jewelryCursor
                                        .getString(uStuffmetaltypeColumn);
                                uJewelry.add(uMetalType);

                                uJewelryWeight = jewelryCursor
                                        .getString(uStuffweightColumn);
                                uJewelry.add(uJewelryWeight);

                                uJewelryArea = jewelryCursor
                                        .getString(uStuffareaColumn);
                                uJewelry.add(uJewelryArea);

                                uJewelryCity = jewelryCursor
                                        .getString(uStuffcityColumn);
                                uJewelry.add(uJewelryCity);

                                uJewelryCountry = jewelryCursor
                                        .getString(uStuffcountryColumn);
                                uJewelry.add(uJewelryCountry);

                                uJewelryLatitude = jewelryCursor
                                        .getString(uStufflatitudeColumn);
                                uJewelry.add(uJewelryLatitude);

                                uJewelryLongitude = jewelryCursor
                                        .getString(uStufflongitudeColumn);
                                uJewelry.add(uJewelryLongitude);

                                js.object();
                                js.key("iStuff").object();
                                for (int y = 0; y < ijewelrycolumnalLen; y++)
                                {
                                    String ijewelrycolumnalString =
                                            iJewelrycolumnal.get(y);
                                    logger.info("ijewelrycolumnalString" +
                                            ijewelrycolumnalString + " = " +
                                            iJewelry.get(y));
                                    js.key(ijewelrycolumnalString)
                                            .value(iJewelry.get(y));
                                }
                                js.endObject();
                                js.key("uStuff").object();
                                for (int i = 0; i < ujewelrycolumnalLen; i++)
                                {
                                    String ujewelrycolumnalString =
                                            uJewelrycolumnal.get(i);
                                    logger.info("ujewelrycolumnalString" +
                                            ujewelrycolumnalString + " = " +
                                            uJewelry.get(i));
                                    js.key(ujewelrycolumnalString)
                                            .value(uJewelry.get(i));
                                }
                                js.endObject();
                                js.endObject();
                                iJewelry.clear();
                                uJewelry.clear();
                            }
                            while (jewelryCursor.moveToNext());
                        }
                        myDB.execSQL(
                                "update category set querystatus='false' where categoryname='Jewelry'");
                        jewelryCursor.close();
                    }

                }
                catch (JSONException e)
                {
                    logger.info("Error = " + e.getMessage());
                }
                jewelrycolumnal.clear();
                iJewelrycolumnal.clear();
                uJewelrycolumnal.clear();
            }
        }
        catch (Exception e)
        {

        }
    }

    private void jewelryResponse()
    {
        try
        {
            JSONArray mStuffJsonArray1 = new JSONArray(values);
            logger.info("mStuffinnerJsonArray1" + values);
            int mStuffJsonArraylength1 = mStuffJsonArray1.length();
            for (int l = 0; l < mStuffJsonArraylength1; l++)
            {
                JSONObject mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l);
                JSONArray mStuffinnerJsonArray1 = mStuffinnerJson1.names();

                int mStuffinnerJsonArraylength1 =
                        mStuffinnerJsonArray1.length();
                for (int k = 0; k < mStuffinnerJsonArraylength1; k++)
                {
                    String keys1 = mStuffinnerJsonArray1.getString(k);
                    String values1 = mStuffinnerJson1.getString(keys1);
                    logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1);
                    logger.info(mStuff0i1.get(k));
                }

                myDB.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffjewelry" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffJewelryMake VARCHAR,mStuffJewelryGender VARCHAR,mStuffStoneType NUMERIC,mStuffMetalType VARCHAR,mStuffWeightRange VARCHAR,mStuffarea VARCHAR,mStuffcity VARCHAR,mStuffcountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );");
                myDB.execSQL(
                        "INSERT INTO MStuffjewelry (mStuffId,mCatagory,mStuffJewelryMake,mStuffJewelryGender,mStuffStoneType,mStuffMetalType,mStuffWeightRange,mStuffarea,mStuffcity,mStuffcountry,mStuffLatitude,mStuffLongitude) VALUES ('" +
                                mStuff0i1.get(7) + "','" + "Jewelry" + "','" +
                                mStuff0i1.get(1) + "','" + mStuff0i1.get(6) +
                                "','" + mStuff0i1.get(8) + "','" +
                                mStuff0i1.get(10) + "','" + mStuff0i1.get(0) +
                                "','" + mStuff0i1.get(5) + "','" +
                                mStuff0i1.get(2) + "','" + mStuff0i1.get(9) +
                                "','" + mStuff0i1.get(3) + "','" +
                                mStuff0i1.get(4) + "');");
                mStuff0i1.clear();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
    }

    private void rental()
    {
        try
        {
            Cursor rental_Cursor = myDB.query(mY_home_TABLE, null,
                    null, null, null, null, null);
            if (rental_Cursor.getCount() > 0)
            {
                try
                {
                    Cursor rentalCursor = myDB.query(mY_home_TABLE, null,
                            "queryStatus='true'", null, null, null, null);

                    int iStuffrentaltypeColumn =
                            rentalCursor.getColumnIndexOrThrow("irental");
                    int iStuffrentalmiscColumn =
                            rentalCursor.getColumnIndexOrThrow("imisc");
                    int iStuffrentalrateColumn =
                            rentalCursor.getColumnIndexOrThrow("irate");
                    int iStuffstatusColumn =
                            rentalCursor.getColumnIndexOrThrow("istatus");
                    int iStuffrentalcountryColumn =
                            rentalCursor.getColumnIndexOrThrow("icountry");
                    int iStuffrentalcityColumn =
                            rentalCursor.getColumnIndexOrThrow("icity");
                    int iStuffrentalareaColumn =
                            rentalCursor.getColumnIndexOrThrow("iarea");
                    int iStufflatitudeColumn =
                            rentalCursor.getColumnIndexOrThrow("ilatitude");
                    int iStufflongitudeColumn =
                            rentalCursor.getColumnIndexOrThrow("ilongitude");

                    int uStuffrentaltypeColumn =
                            rentalCursor.getColumnIndexOrThrow("urental");
                    int uStuffrentalmiscColumn =
                            rentalCursor.getColumnIndexOrThrow("umisc");
                    int uStuffrentalrateColumn =
                            rentalCursor.getColumnIndexOrThrow("urate");
                    int uStuffstatusColumn =
                            rentalCursor.getColumnIndexOrThrow("ustatus");
                    int uStuffrentalcountryColumn =
                            rentalCursor.getColumnIndexOrThrow("ucountry");
                    int uStuffrentalcityColumn =
                            rentalCursor.getColumnIndexOrThrow("ucity");
                    int uStuffrentalareaColumn =
                            rentalCursor.getColumnIndexOrThrow("uarea");
                    int uStufflatitudeColumn =
                            rentalCursor.getColumnIndexOrThrow("ulatitude");
                    int uStufflongitudeColumn =
                            rentalCursor.getColumnIndexOrThrow("ulongitude");

                    // Getting Rental Column
                    String rentalcolumn[] = rentalCursor.getColumnNames();
                    String irentalcolumn[] = null;
                    String urentalcolumn[] = null;
                    for (int j = 1; j < rentalcolumn.length - 2; j++)
                    {
                        rentalcolumnal.add(rentalcolumn[j]);
                        //logger.info("rentalcolumns:" + rentalcolumn[j]);
                        // Log.i(".....................", rentalcolumn[j]);
                    }
                    if (rentalcolumnal.size() > 1)
                    {
                        irentalcolumn = extract1(rentalcolumn, 0,
                                rentalcolumn.length / 2);
                        urentalcolumn = extract1(rentalcolumn,
                                rentalcolumn.length / 2, rentalcolumn.length);
                    }
                    for (int j = 1; j < irentalcolumn.length; j++)
                    {
                        iRentalcolumnal.add(irentalcolumn[j]);
                        //logger.info("irentalcolumnal:" + irentalcolumn[j]);
                    }

                    for (int j = 0; j < urentalcolumn.length - 2; j++)
                    {
                        uRentalcolumnal.add(urentalcolumn[j]);
                        // logger.info("urentalcolumnal:" + urentalcolumn[j]);
                    }
                    int irentalcolumnalLen = iRentalcolumnal.size();
                    int urentalcolumnalLen = uRentalcolumnal.size();

                    if (rentalCursor != null)
                    {
                        if (rentalCursor.isFirst())
                        {
                            do
                            {

                                iRentalType = rentalCursor
                                        .getString(iStuffrentaltypeColumn);
                                iRental.add(iRentalType);

                                iRentalMisc = rentalCursor
                                        .getString(iStuffrentalmiscColumn);
                                iRental.add(iRentalMisc);

                                iRentalRaterange = rentalCursor
                                        .getString(iStuffrentalrateColumn);
                                iRental.add(iRentalRaterange);

                                iRentalstatus = rentalCursor
                                        .getString(iStuffstatusColumn);
                                iRental.add(iRentalstatus);


                                iRentalArea = rentalCursor
                                        .getString(iStuffrentalareaColumn);
                                iRental.add(iRentalArea);

                                iRentalCity = rentalCursor
                                        .getString(iStuffrentalcityColumn);
                                iRental.add(iRentalCity);

                                iRentalCountry = rentalCursor
                                        .getString(iStuffrentalcountryColumn);
                                iRental.add(iRentalCountry);

                                iRentallatitude = rentalCursor
                                        .getString(iStufflatitudeColumn);
                                iRental.add(iRentallatitude);

                                iRentallongitude = rentalCursor
                                        .getString(iStufflongitudeColumn);
                                iRental.add(iRentallongitude);

                                uRentalType = rentalCursor
                                        .getString(uStuffrentaltypeColumn);
                                uRental.add(uRentalType);

                                uRentalMisc = rentalCursor
                                        .getString(uStuffrentalmiscColumn);
                                uRental.add(uRentalMisc);

                                uRentalRaterange = rentalCursor
                                        .getString(uStuffrentalrateColumn);
                                uRental.add(uRentalRaterange);

                                uRentalstatus = rentalCursor
                                        .getString(uStuffstatusColumn);
                                uRental.add(uRentalstatus);

                                uRentalArea = rentalCursor
                                        .getString(uStuffrentalareaColumn);
                                uRental.add(uRentalArea);

                                uRentalCity = rentalCursor
                                        .getString(uStuffrentalcityColumn);
                                uRental.add(uRentalCity);

                                uRentalCountry = rentalCursor
                                        .getString(uStuffrentalcountryColumn);
                                uRental.add(uRentalCountry);


                                uRentallatitude = rentalCursor
                                        .getString(uStufflatitudeColumn);
                                uRental.add(uRentallatitude);

                                uRentallongitude = rentalCursor
                                        .getString(uStufflongitudeColumn);
                                uRental.add(uRentallongitude);

                                js.object();
                                js.key("iStuff").object();
                                for (int y = 0; y < irentalcolumnalLen; y++)
                                {
                                    String irentalcolumnalString =
                                            iRentalcolumnal.get(y);
                                    // logger.info("irentalcolumnalString" + irentalcolumnalString + " = " + irental.get(y));
                                    js.key(irentalcolumnalString)
                                            .value(iRental.get(y));
                                }
                                js.endObject();
                                js.key("uStuff").object();
                                for (int i = 0; i < urentalcolumnalLen; i++)
                                {
                                    String urentalcolumnalString =
                                            uRentalcolumnal.get(i);
                                    //logger.info("urentalcolumnalString" + urentalcolumnalString + " = " + urental.get(i));
                                    js.key(urentalcolumnalString)
                                            .value(uRental.get(i));
                                }
                                js.endObject();
                                js.endObject();
                                iRental.clear();
                                uRental.clear();

                            }
                            while (rentalCursor.moveToNext());
                        }
                        myDB.execSQL(
                                "update category set querystatus='false' where categoryname='Rental'");
                        rentalCursor.close();
                    }

                }
                catch (JSONException e)
                {
                    logger.info("Error = " + e.getMessage());
                }
                rentalcolumnal.clear();
                iRentalcolumnal.clear();
                uRentalcolumnal.clear();
            }
        }
        catch (Exception e)
        {

        }
    }

    private void rentalResponse()
    {
        try
        {
            JSONArray mStuffJsonArray1 = new JSONArray(values);
            int mStuffJsonArraylength1 = mStuffJsonArray1.length();
            for (int l = 0; l < mStuffJsonArraylength1; l++)
            {
                JSONObject mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l);
                JSONArray mStuffinnerJsonArray1 = mStuffinnerJson1.names();

                int mStuffinnerJsonArraylength1 =
                        mStuffinnerJsonArray1.length();
                for (int k = 0; k < mStuffinnerJsonArraylength1; k++)
                {
                    String keys1 = mStuffinnerJsonArray1.getString(k);
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    String values1 = mStuffinnerJson1.getString(keys1);
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1);
                    logger.info(mStuff0i1.get(k));
                }
                myDB.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffrental" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR, mStuffRentalType VARCHAR,mStuffMisc VARCHAR,mStuffRate VARCHAR,mStuffStatus VARCHAR,mStuffCountry VARCHAR,mStuffCity VARCHAR,mStuffArea VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );");

                myDB.execSQL(
                        "INSERT INTO MStuffrental(mStuffId , mCatagory , mStuffRentalType ,mStuffMisc ,mStuffRate ,mStuffStatus ,mStuffCountry ,mStuffCity ,mStuffArea ,mStuffLatitude ,mStuffLongitude ) VALUES ('" +
                                mStuff0i1.get(8) + "','" + "Rental" + "','" +
                                mStuff0i1.get(5) + "','" + mStuff0i1.get(2) +
                                "','" + mStuff0i1.get(6) + "','" +
                                mStuff0i1.get(7) + "','" + mStuff0i1.get(9) +
                                "','" + mStuff0i1.get(0) + "','" +
                                mStuff0i1.get(4) + "','" + mStuff0i1.get(1) +
                                "','" + mStuff0i1.get(3) + "');");
                mStuff0i1.clear();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
    }

    private void restaurants()
    {
        try
        {
            Cursor rental_Cursor = myDB.query(mY_Restaurants_TABLE, null,
                    null, null, null, null, null);
            if (rental_Cursor.getCount() > 0)
            {
                try
                {
                    Cursor restaurantsCursor = myDB.query(
                            mY_Restaurants_TABLE, null, "queryStatus='true'",
                            null, null, null, null);

                    int iStuffCuisinetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffCuisinetype");
                    int iStuffCookingMethodColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffCookingMethod");
                    int iStuffDieteticColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("iStuffDietetic");
                    int iStuffCoursetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffCourseType");
                    int iStuffDishtypeColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("iStuffDishType");
                    int iStuffMainIngredientColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffMainIngredient");
                    int iStuffOccasionOrSeasonColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffOccasionOrSeason");
                    int iStuffMiscellaneousColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffMiscellaneous");
                    int iStufflatitudeColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("ilatitude");
                    int iStufflongitudeColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("ilongitude");
                    int iRestaurantsAreaColumn =
                            restaurantsCursor.getColumnIndexOrThrow("iarea");
                    int iRestaurantsCityColumn =
                            restaurantsCursor.getColumnIndexOrThrow("icity");
                    int iRestaurantsCountryColumn =
                            restaurantsCursor.getColumnIndexOrThrow("icountry");

                    int uStuffCuisinetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffCuisinetype");
                    int uStuffCookingMethodColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffCookingMethod");
                    int uStuffDieteticColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("uStuffDietetic");
                    int uStuffCoursetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffCourseType");
                    int uStuffDishtypeColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("uStuffDishType");
                    int uStuffMainIngredientColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffMainIngredient");
                    int uStuffOccasionOrSeasonColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffOccasionOrSeason");
                    int uStuffMiscellaneousColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffMiscellaneous");
                    int uStufflatitudeColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("ulatitude");
                    int uStufflongitudeColumn =
                            restaurantsCursor
                                    .getColumnIndexOrThrow("ulongitude");
                    int uRestaurantsAreaColumn =
                            restaurantsCursor.getColumnIndexOrThrow("uarea");
                    int uRestaurantsCityColumn =
                            restaurantsCursor.getColumnIndexOrThrow("ucity");
                    int uRestaurantsCountryColumn =
                            restaurantsCursor.getColumnIndexOrThrow("ucountry");

                    // Getting restaurants Column
                    String restaurantscolumn[] =
                            restaurantsCursor.getColumnNames();
                    String irestaurantscolumn[] = null;
                    String urestaurantscolumn[] = null;
                    for (int j = 1; j < restaurantscolumn.length - 2; j++)
                    {
                        restaurantscolumnal.add(restaurantscolumn[j]);
                        logger.info(
                                "restaurantscolumn:" + restaurantscolumn[j]);
                        // Log.i(".....................", carscolumn[j]);
                    }
                    if (restaurantscolumnal.size() > 1)
                    {
                        irestaurantscolumn = extract1(restaurantscolumn, 0,
                                restaurantscolumn.length / 2);
                        urestaurantscolumn = extract1(restaurantscolumn,
                                restaurantscolumn.length / 2,
                                restaurantscolumn.length);
                    }
                    for (int j = 1; j < irestaurantscolumn.length; j++)
                    {
                        iRestaurantscolumnal.add(irestaurantscolumn[j]);
                        logger.info(
                                "irestaurantscolumn:" + irestaurantscolumn[j]);
                    }

                    for (int j = 0; j < urestaurantscolumn.length - 2; j++)
                    {
                        uRestaurantscolumnal.add(urestaurantscolumn[j]);
                        logger.info(
                                "urestaurantscolumn:" + urestaurantscolumn[j]);
                    }
                    // Length
                    int irestaurantscolumnalLen = iRestaurantscolumnal.size();
                    int urestaurantscolumnalLen = uRestaurantscolumnal.size();

                    if (restaurantsCursor != null)
                    {
                        if (restaurantsCursor.isFirst())
                        {
                            do
                            {
                                iCuisineType = restaurantsCursor
                                        .getString(iStuffCuisinetypeColumn);
                                iRestaurants.add(iCuisineType);

                                iCookingMethod = restaurantsCursor
                                        .getString(iStuffCookingMethodColumn);
                                iRestaurants.add(iCookingMethod);

                                iDietetic = restaurantsCursor
                                        .getString(iStuffDieteticColumn);
                                iRestaurants.add(iDietetic);

                                iCourseType = restaurantsCursor
                                        .getString(iStuffCoursetypeColumn);
                                iRestaurants.add(iCourseType);

                                iDishType = restaurantsCursor
                                        .getString(iStuffDishtypeColumn);
                                iRestaurants.add(iDishType);

                                iMainIngredient = restaurantsCursor
                                        .getString(iStuffMainIngredientColumn);
                                iRestaurants.add(iMainIngredient);

                                iOccasionOrSeason = restaurantsCursor.getString(
                                        iStuffOccasionOrSeasonColumn);
                                iRestaurants.add(iOccasionOrSeason);

                                iMiscellaneous = restaurantsCursor
                                        .getString(iStuffMiscellaneousColumn);
                                iRestaurants.add(iMiscellaneous);

                                iRestaurantsArea = restaurantsCursor
                                        .getString(iRestaurantsAreaColumn);
                                iRestaurants.add(iRestaurantsArea);

                                iRestaurantsCity = restaurantsCursor
                                        .getString(iRestaurantsCityColumn);
                                iRestaurants.add(iRestaurantsCity);

                                iRestaurantsCountry = restaurantsCursor
                                        .getString(iRestaurantsCountryColumn);
                                iRestaurants.add(iRestaurantsCountry);

                                iRestaurantsLatitude = restaurantsCursor
                                        .getString(iStufflatitudeColumn);
                                iRestaurants.add(iRestaurantsLatitude);

                                iRestaurantsLongitude = restaurantsCursor
                                        .getString(iStufflongitudeColumn);
                                iRestaurants.add(iRestaurantsLongitude);

                                uCuisineType = restaurantsCursor
                                        .getString(uStuffCuisinetypeColumn);
                                uRestaurants.add(uCuisineType);

                                uCookingMethod = restaurantsCursor
                                        .getString(uStuffCookingMethodColumn);
                                uRestaurants.add(uCookingMethod);

                                uDietetic = restaurantsCursor
                                        .getString(uStuffDieteticColumn);
                                uRestaurants.add(uDietetic);

                                uCourseType = restaurantsCursor
                                        .getString(uStuffCoursetypeColumn);
                                uRestaurants.add(uCourseType);

                                uDishType = restaurantsCursor
                                        .getString(uStuffDishtypeColumn);
                                uRestaurants.add(uDishType);

                                uMainIngredient = restaurantsCursor
                                        .getString(uStuffMainIngredientColumn);
                                uRestaurants.add(uMainIngredient);

                                uOccasionOrSeason = restaurantsCursor.getString(
                                        uStuffOccasionOrSeasonColumn);
                                uRestaurants.add(uOccasionOrSeason);

                                uMiscellaneous = restaurantsCursor
                                        .getString(uStuffMiscellaneousColumn);
                                uRestaurants.add(uMiscellaneous);

                                uRestaurantsArea = restaurantsCursor
                                        .getString(uRestaurantsAreaColumn);
                                uRestaurants.add(uRestaurantsArea);

                                uRestaurantsCity = restaurantsCursor
                                        .getString(uRestaurantsCityColumn);
                                uRestaurants.add(uRestaurantsCity);

                                uRestaurantsCountry = restaurantsCursor
                                        .getString(uRestaurantsCountryColumn);
                                uRestaurants.add(uRestaurantsCountry);

                                uRestaurantsLatitude = restaurantsCursor
                                        .getString(uStufflatitudeColumn);
                                uRestaurants.add(uRestaurantsLatitude);

                                uRestaurantsLongitude = restaurantsCursor
                                        .getString(uStufflongitudeColumn);
                                uRestaurants.add(uRestaurantsLongitude);

                                js.object();
                                js.key("iStuff").object();
                                for (int y = 0; y < irestaurantscolumnalLen;
                                        y++)
                                {
                                    String irestautantscolumnalString =
                                            iRestaurantscolumnal.get(y);
                                    logger.info("irestautantscolumnalString" +
                                            irestautantscolumnalString + " = " +
                                            iRestaurants.get(y));
                                    js.key(irestautantscolumnalString)
                                            .value(iRestaurants.get(y));
                                }
                                js.endObject();
                                js.key("uStuff").object();
                                for (int i = 0; i < urestaurantscolumnalLen;
                                        i++)
                                {
                                    String urestaurantscolumnalString =
                                            uRestaurantscolumnal.get(i);
                                    logger.info("urestaurantscolumnalString" +
                                            urestaurantscolumnalString + " = " +
                                            uRestaurants.get(i));
                                    js.key(urestaurantscolumnalString)
                                            .value(uRestaurants.get(i));
                                }
                                js.endObject();
                                js.endObject();
                                iRestaurants.clear();
                                uRestaurants.clear();

                            }
                            while (restaurantsCursor.moveToNext());
                        }
                        myDB.execSQL(
                                "update category set querystatus='false' where categoryname='Restaurants'");
                        restaurantsCursor.close();
                    }
                }
                catch (JSONException e)
                {
                    logger.info("Error = " + e.getMessage());
                }
                restaurantscolumnal.clear();
                iRestaurantscolumnal.clear();
                uRestaurantscolumnal.clear();
            }
        }
        catch (Exception e)
        {

        }
    }

    private void restaurantsResponse()
    {
        try
        {
            JSONArray mStuffJsonArray1 = new JSONArray(values);
            int mStuffJsonArraylength1 = mStuffJsonArray1.length();
            for (int l = 0; l < mStuffJsonArraylength1; l++)
            {
                JSONObject mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l);
                JSONArray mStuffinnerJsonArray1 = mStuffinnerJson1.names();

                int mStuffinnerJsonArraylength1 =
                        mStuffinnerJsonArray1.length();
                for (int k = 0; k < mStuffinnerJsonArraylength1; k++)
                {
                    String keys1 = mStuffinnerJsonArray1.getString(k);
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    String values1 = mStuffinnerJson1.getString(keys1);
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1);
                    logger.info(mStuff0i1.get(k));
                }
                myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                        "MStuffRestaurants" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffCuisineType VARCHAR,mStuffCookingMethod VARCHAR,mStuffDietetic VARCHAR,mStuffCourseType VARCHAR,mStuffDishType VARCHAR,mStuffMainIngredient VARCHAR,mStuffOccasionOrSeason VARCHAR,mStuffMiscellaneous VARCHAR,mStuffArea VARCHAR,mStuffCity VARCHAR,mStuffCountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );");
                myDB.execSQL(
                        "INSERT INTO MStuffRestaurants(mStuffId,mCatagory,mStuffCuisineType,mStuffCookingMethod,mStuffDietetic,mStuffCourseType,mStuffDishType,mStuffMainIngredient,mStuffOccasionOrSeason,mStuffMiscellaneous,mStuffArea,mStuffCity,mStuffCountry,mStuffLatitude,mStuffLongitude) VALUES ('" +
                                mStuff0i1.get(12) + "','" + "Restaurants" +
                                "','" + mStuff0i1.get(6) + "','" +
                                mStuff0i1.get(5) + "','" + mStuff0i1.get(3) +
                                "','" + mStuff0i1.get(0) + "','" +
                                mStuff0i1.get(13) + "','" + mStuff0i1.get(9) +
                                "','" + mStuff0i1.get(11) + "','" +
                                mStuff0i1.get(1) + "','" + mStuff0i1.get(10) +
                                "','" + mStuff0i1.get(7) + "','" +
                                mStuff0i1.get(4) + "','" + mStuff0i1.get(8) +
                                "','" + mStuff0i1.get(2) + "');");

                mStuff0i1.clear();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
    }

    private void movies()
    {
        Cursor moviesCursor = myDB.query(mY_Movies_TABLE, null,
                "queryStatus='true'", null, null, null, null);
        int iMovieTypeColumn = moviesCursor.getColumnIndexOrThrow("imovietype");
        int iMovieLanguageColumn =
                moviesCursor.getColumnIndexOrThrow("imovielanguage");
        int iSeatingStyleColumn =
                moviesCursor.getColumnIndexOrThrow("iseatingstyle");
        int iMovieAreaColumn = moviesCursor.getColumnIndexOrThrow("iarea");
        int iMovieCityColumn = moviesCursor.getColumnIndexOrThrow("icity");
        int iMovieCountryColumn =
                moviesCursor.getColumnIndexOrThrow("icountry");
        int iMovieLatitudeColumn =
                moviesCursor.getColumnIndexOrThrow("ilatitude");
        int iMovieLongitudeColumn =
                moviesCursor.getColumnIndexOrThrow("ilongitude");

        int uMovieTypeColumn = moviesCursor.getColumnIndexOrThrow("umovietype");
        int uMovieLanguageColumn =
                moviesCursor.getColumnIndexOrThrow("umovielanguage");
        int uSeatingStyleColumn =
                moviesCursor.getColumnIndexOrThrow("useatingstyle");
        int uMovieAreaColumn = moviesCursor.getColumnIndexOrThrow("uarea");
        int uMovieCityColumn = moviesCursor.getColumnIndexOrThrow("ucity");
        int uMovieCountryColumn =
                moviesCursor.getColumnIndexOrThrow("ucountry");
        int uMovieLatitudeColumn =
                moviesCursor.getColumnIndexOrThrow("ulatitude");
        int uMovieLongitudeColumn =
                moviesCursor.getColumnIndexOrThrow("ulongitude");
        // Getting movies Column
        String moviescolumn[] = moviesCursor.getColumnNames();
        String imoviescolumn[] = null;
        String umoviescolumn[] = null;
        for (int j = 1; j < moviescolumn.length - 2; j++)
        {
            moviescolumnal.add(moviescolumn[j]);
            logger.info("moviescolumn:" + moviescolumn[j]);
        }
        if (moviescolumnal.size() > 1)
        {
            imoviescolumn = extract1(moviescolumn, 0, moviescolumn.length / 2);
            umoviescolumn = extract1(moviescolumn, moviescolumn.length / 2,
                    moviescolumn.length);
        }
        for (int j = 1; j < imoviescolumn.length; j++)
        {
            iMoviescolumnal.add(imoviescolumn[j]);
            logger.info("imoviescolumn:" + imoviescolumn[j]);
        }

        for (int j = 0; j < umoviescolumn.length - 2; j++)
        {
            uMoviescolumnal.add(umoviescolumn[j]);
            logger.info("umoviescolumn:" + umoviescolumn[j]);
        }
        // Length
        int imoviescolumnalLen = iMoviescolumnal.size();
        int umoviescolumnalLen = uMoviescolumnal.size();
        try
        {
            if (moviesCursor != null)
            {
                if (moviesCursor.isFirst())
                {
                    do
                    {
                        iMovieType = moviesCursor.getString(iMovieTypeColumn);
                        iMovies.add(iMovieType);

                        iMovieLanguage =
                                moviesCursor.getString(iMovieLanguageColumn);
                        iMovies.add(iMovieLanguage);

                        iSeatingStyle =
                                moviesCursor.getString(iSeatingStyleColumn);
                        iMovies.add(iSeatingStyle);

                        iMovieArea = moviesCursor.getString(iMovieAreaColumn);
                        iMovies.add(iMovieArea);

                        iMovieCity = moviesCursor.getString(iMovieCityColumn);
                        iMovies.add(iMovieCity);

                        iMovieCountry =
                                moviesCursor.getString(iMovieCountryColumn);
                        iMovies.add(iMovieCountry);

                        iMovieLatitude =
                                moviesCursor.getString(iMovieLatitudeColumn);
                        iMovies.add(iMovieLatitude);

                        iMovieLongitude =
                                moviesCursor.getString(iMovieLongitudeColumn);
                        iMovies.add(iMovieLongitude);

                        uMovieType = moviesCursor.getString(uMovieTypeColumn);
                        uMovies.add(uMovieType);

                        uMovieLanguage =
                                moviesCursor.getString(uMovieLanguageColumn);
                        uMovies.add(uMovieLanguage);

                        uSeatingStyle =
                                moviesCursor.getString(uSeatingStyleColumn);
                        uMovies.add(uSeatingStyle);

                        uMovieArea = moviesCursor.getString(uMovieAreaColumn);
                        uMovies.add(uMovieArea);

                        uMovieCity = moviesCursor.getString(uMovieCityColumn);
                        uMovies.add(uMovieCity);

                        uMovieCountry =
                                moviesCursor.getString(uMovieCountryColumn);
                        uMovies.add(uMovieCountry);

                        uMovieLatitude =
                                moviesCursor.getString(uMovieLatitudeColumn);
                        uMovies.add(uMovieLatitude);

                        uMovieLongitude =
                                moviesCursor.getString(uMovieLongitudeColumn);
                        uMovies.add(uMovieLongitude);

                        js.object();
                        js.key("iStuff").object();
                        for (int y = 0; y < imoviescolumnalLen; y++)
                        {
                            String imoviescolumnalString =
                                    iMoviescolumnal.get(y);
                            logger.info("imvoiescolumnalString" +
                                    imoviescolumnalString + " = " +
                                    iMovies.get(y));
                            js.key(imoviescolumnalString).value(iMovies.get(y));
                        }
                        js.endObject();
                        js.key("uStuff").object();
                        for (int i = 0; i < umoviescolumnalLen; i++)
                        {
                            String umvoiescolumnalString =
                                    uMoviescolumnal.get(i);
                            logger.info("umvoiescolumnalString" +
                                    umvoiescolumnalString + " = " +
                                    uMovies.get(i));
                            js.key(umvoiescolumnalString).value(uMovies.get(i));
                        }
                        js.endObject();
                        js.endObject();
                        iMovies.clear();
                        uMovies.clear();
                    }
                    while (moviesCursor.moveToNext());
                }
                myDB.execSQL(
                        "update category set querystatus='false' where categoryname='Movies'");
                moviesCursor.close();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
        moviescolumnal.clear();
        iMoviescolumnal.clear();
        uMoviescolumnal.clear();
    }

    private void moviesResponse()
    {
        try
        {
            JSONArray mStuffJsonArray1 = new JSONArray(values);
            int mStuffJsonArraylength1 = mStuffJsonArray1.length();
            for (int l = 0; l < mStuffJsonArraylength1; l++)
            {
                JSONObject mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l);
                JSONArray mStuffinnerJsonArray1 = mStuffinnerJson1.names();

                int mStuffinnerJsonArraylength1 =
                        mStuffinnerJsonArray1.length();
                for (int k = 0; k < mStuffinnerJsonArraylength1; k++)
                {
                    String keys1 = mStuffinnerJsonArray1.getString(k);
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    String values1 = mStuffinnerJson1.getString(keys1);
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1);
                    logger.info(mStuff0i1.get(k));
                }
                myDB.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffmovies" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR, mStuffMovieType VARCHAR,mStuffMovieLanguage VARCHAR,mStuffSeatingStyle VARCHAR,mStuffMovieArea VARCHAR,mStuffMovieCity VARCHAR,mStuffMovieCountry VARCHAR,mStuffMovieLatitude VARCHAR,mStuffMovieLongitude VARCHAR );");
                myDB.execSQL(
                        "INSERT INTO MStuffmovies(mStuffId , mCatagory , mStuffMovieType ,mStuffMovieLanguage ,mStuffSeatingStyle ,mStuffMovieArea ,mStuffMovieCity ,mStuffMovieCountry ,mStuffMovieLatitude ,mStuffMovieLongitude) VALUES ('" +
                                mStuff0i1.get(5) + "','" + "Movies" + "','" +
                                mStuff0i1.get(0) + "','" + mStuff0i1.get(8) +
                                "','" + mStuff0i1.get(7) + "','" +
                                mStuff0i1.get(4) + "','" + mStuff0i1.get(1) +
                                "','" + mStuff0i1.get(6) + "','" +
                                mStuff0i1.get(2) + "','" + mStuff0i1.get(3) +
                                "');");
                mStuff0i1.clear();
            }
        }
        catch (JSONException e)
        {
            logger.info("Error = " + e.getMessage());
        }
    }

    private static String[] extract1(String[] elts, int start, int last)
    {
        String[] ret = new String[last - start];
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = elts[start + i];
        }
        return ret;
    }
}
