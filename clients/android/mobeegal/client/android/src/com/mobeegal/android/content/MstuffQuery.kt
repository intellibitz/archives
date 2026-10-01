package com.mobeegal.android.content

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.activity.StatusbarNotification
import com.mobeegal.android.util.HttpUtils
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import java.io.FileNotFoundException
import java.io.IOException
import java.util.ArrayList
import java.util.logging.Logger

class MstuffQuery : BroadcastReceiver() {

    var myDB: SQLiteDatabase? = null
    var catalogs: String? = null
    var mY_DATING_DATABASE: String? = "Mobeegal"
    var mY_catalogs_TABLE: String? = "catalogs"
    var mY_category_TABLE: String? = "category"
    var categoryal: ArrayList<Any?> = ArrayList()
    var categoryalc: ArrayList<Any?> = ArrayList()
    var catalogsal: ArrayList<Any?> = ArrayList()
    var js: JSONStringer = JSONStringer()
    var mStuff0i: ArrayList<String> = ArrayList()
    var mStuff0i2: ArrayList<String> = ArrayList()
    var mStuff0i1: ArrayList<String> = ArrayList()
    var values: String? = null
    var keys: String? = null
    var response: String? = null
    var request: String? = null
    var id: String? = ""
    var details: String? = ""
    var location: String? = ""
    var dblatitude: String? = null
    var dblongitude: String? = null
    var latitude: Int = 0
    var longitude: Int = 0
    var latitude1: Int = 0
    var longitude1: Int = 0
    var lat: Double = 0.0
    var lon: Double = 0.0
    var lat1: Double = 0.0
    var lon1: Double = 0.0
    var catagory: String? = null
    //MobeegalUser

    var catalogsname: String? = "catalogname"
    var catalogsstate: String? = "state"
    var categorysname: String? = "categoryname"
    var categorystate: String? = "status"
    //Dating

    var idatingal: ArrayList<Any?> = ArrayList()
    var udatingal: ArrayList<Any?> = ArrayList()
    var datingcolumnal: ArrayList<String> = ArrayList()
    var idatingcolumnal: ArrayList<String> = ArrayList()
    var udatingcolumnal: ArrayList<String> = ArrayList()
    var iStuffAge: String? = null
    var iStuffSex: String? = null
    var iStuffHeight: String? = null
    var iStuffWeight: String? = null
    var iStuffLocation: String? = null
    var iStuffCity: String? = null
    var iStuffCountry: String? = null
    var uStuffAge: String? = null
    var uStuffSex: String? = null
    var uStuffHeight: String? = null
    var uStuffWeight: String? = null
    var uStuffLocation: String? = null
    var uStuffCity: String? = null
    var uStuffCountry: String? = null
    var iStufflatitude: String? = null
    var iStufflongitude: String? = null
    var uStufflatitude: String? = null
    var uStufflongitude: String? = null
    //Matrimony

    var iMatrimony: ArrayList<Any?> = ArrayList()
    var uMatrimony: ArrayList<Any?> = ArrayList()
    var matrimonycolumnal: ArrayList<String> = ArrayList()
    var iMatrimonycolumnal: ArrayList<String> = ArrayList()
    var uMatrimonycolumnal: ArrayList<String> = ArrayList()
    var iReligion: String? = null
    var iCaste: String? = null
    var iColor: String? = null
    var iAge: String? = null
    var iSex: String? = null
    var iHeight: String? = null
    var iWeight: String? = null
    var iArea: String? = null
    var iCity: String? = null
    var iCountry: String? = null
    var iLatitude: String? = null
    var iLongitude: String? = null
    var uReligion: String? = null
    var uCaste: String? = null
    var uColor: String? = null
    var uAge: String? = null
    var uSex: String? = null
    var uHeight: String? = null
    var uWeight: String? = null
    var uArea: String? = null
    var uCity: String? = null
    var uCountry: String? = null
    var uLatitude: String? = null
    var uLongitude: String? = null
    //Cars

    var carscolumnal: ArrayList<String> = ArrayList()
    var iCarscolumnal: ArrayList<String> = ArrayList()
    var uCarscolumnal: ArrayList<String> = ArrayList()
    var iCars: ArrayList<Any?> = ArrayList()
    var uCars: ArrayList<Any?> = ArrayList()
    var iCarMake: String? = null
    var iCarModel: String? = null
    var iCarYear: String? = null
    var iCarColor: String? = null
    var iCarFuel_Type: String? = null
    var iCarPrice: String? = null
    var iCarArea: String? = null
    var iCarCity: String? = null
    var iCarCountry: String? = null
    var iCarLatitude: String? = null
    var iCarLongitude: String? = null
    var uCarMake: String? = null
    var uCarModel: String? = null
    var uCarYear: String? = null
    var uCarColor: String? = null
    var uCarFuel_Type: String? = null
    var uCarPrice: String? = null
    var uCarArea: String? = null
    var uCarCity: String? = null
    var uCarCountry: String? = null
    var uCarLatitude: String? = null
    var uCarLongitude: String? = null
    //Jewelry

    var jewelrycolumnal: ArrayList<String> = ArrayList()
    var iJewelrycolumnal: ArrayList<String> = ArrayList()
    var uJewelrycolumnal: ArrayList<String> = ArrayList()
    var iJewelry: ArrayList<Any?> = ArrayList()
    var uJewelry: ArrayList<Any?> = ArrayList()
    var iJewelryType: String? = null
    var iGender: String? = null
    var iStoneType: String? = null
    var iMetalType: String? = null
    var iJewelryWeight: String? = null
    var iJewelryCountry: String? = null
    var iJewelryCity: String? = null
    var iJewelryArea: String? = null
    var iJewelryLatitude: String? = null
    var iJewelryLongitude: String? = null
    var uJewelryType: String? = null
    var uGender: String? = null
    var uStoneType: String? = null
    var uMetalType: String? = null
    var uJewelryWeight: String? = null
    var uJewelryCountry: String? = null
    var uJewelryCity: String? = null
    var uJewelryArea: String? = null
    var uJewelryLatitude: String? = null
    var uJewelryLongitude: String? = null
    //Rental

    var rentalcolumnal: ArrayList<String> = ArrayList()
    var iRentalcolumnal: ArrayList<String> = ArrayList()
    var uRentalcolumnal: ArrayList<String> = ArrayList()
    var iRental: ArrayList<Any?> = ArrayList()
    var uRental: ArrayList<Any?> = ArrayList()
    var iRentalType: String? = null
    var iRentalMisc: String? = null
    var iRentalRaterange: String? = null
    var iRentalCountry: String? = null
    var iRentalstatus: String? = null
    var iRentalCity: String? = null
    var iRentalArea: String? = null
    var uRentalType: String? = null
    var uRentalMisc: String? = null
    var uRentalRaterange: String? = null
    var uRentalCountry: String? = null
    var uRentalstatus: String? = null
    var uRentalCity: String? = null
    var uRentalArea: String? = null
    var iRentallatitude: String? = null
    var iRentallongitude: String? = null
    var uRentallatitude: String? = null
    var uRentallongitude: String? = null
    //Restaurants

    var restaurantscolumnal: ArrayList<String> = ArrayList()
    var iRestaurantscolumnal: ArrayList<String> = ArrayList()
    var uRestaurantscolumnal: ArrayList<String> = ArrayList()
    var iRestaurants: ArrayList<Any?> = ArrayList()
    var uRestaurants: ArrayList<Any?> = ArrayList()
    var iCuisineType: String? = null
    var iCookingMethod: String? = null
    var iDietetic: String? = null
    var iCourseType: String? = null
    var iDishType: String? = null
    var iMainIngredient: String? = null
    var iOccasionOrSeason: String? = null
    var iMiscellaneous: String? = null
    var iRestaurantsCity: String? = null
    var iRestaurantsArea: String? = null
    var iRestaurantsCountry: String? = null
    var iRestaurantsLatitude: String? = null
    var iRestaurantsLongitude: String? = null
    var uCuisineType: String? = null
    var uCookingMethod: String? = null
    var uDietetic: String? = null
    var uCourseType: String? = null
    var uDishType: String? = null
    var uMainIngredient: String? = null
    var uOccasionOrSeason: String? = null
    var uMiscellaneous: String? = null
    var uRestaurantsCity: String? = null
    var uRestaurantsArea: String? = null
    var uRestaurantsCountry: String? = null
    var uRestaurantsLatitude: String? = null
    var uRestaurantsLongitude: String? = null
    //Movies

    var moviescolumnal: ArrayList<String> = ArrayList()
    var iMoviescolumnal: ArrayList<String> = ArrayList()
    var uMoviescolumnal: ArrayList<String> = ArrayList()
    var iMovies: ArrayList<Any?> = ArrayList()
    var uMovies: ArrayList<Any?> = ArrayList()
    var iMovieType: String? = null
    var iMovieLanguage: String? = null
    var iSeatingStyle: String? = null
    var iMovieArea: String? = null
    var iMovieCity: String? = null
    var iMovieCountry: String? = null
    var iMovieLatitude: String? = null
    var iMovieLongitude: String? = null
    var uMovieType: String? = null
    var uMovieLanguage: String? = null
    var uSeatingStyle: String? = null
    var uMovieArea: String? = null
    var uMovieCity: String? = null
    var uMovieCountry: String? = null
    var uMovieLatitude: String? = null
    var uMovieLongitude: String? = null
    var decrypted: String? = null
    var datingstring: String? = "Dating"
    var matrimonystring: String? = "Matrimony"
    var carsstring: String? = "Cars"
    var jewelrystring: String? = "Jewelry"
    var rentalstring: String? = "Rental"
    var restaurantstring: String? = "Restaurants"
    var moviesstring: String? = "Movies"
    var con: Context? = null

    override fun onReceive(context: Context, intent: Intent)
    {
        con = context
        object : Thread()
        {

            private var catalogsindex: Int = 0

            override fun run()
            {
                Looper.prepare()
                try
                {
                    myDB = context.openOrCreateDatabase(mY_DATING_DATABASE!!,
                            Context.MODE_PRIVATE, null)

                    val mobeegalUserCursor = myDB!!.query(mY_MobeegalUser_TABLE, null,
                                    null, null, null, null, null)
                    val UseridColumn = mobeegalUserCursor.getColumnIndexOrThrow("UserID")
                    var useridColumn: String? = null
                    if (mobeegalUserCursor != null)
                    {
                        if (mobeegalUserCursor.isFirst)
                        {
                            useridColumn =
                                    mobeegalUserCursor.getString(UseridColumn)
                                    // logger.info("mobeegalUserID = " + useridColumn);
                        }
                    }

                    /*val catalogcolumn = arrayOfNulls<String>(1)
                    catalogcolumn[0] = "catalogname"
                    val testquery = "select categoryname,catalogname from category,catalogs where category.catalogID1" + "=" + "catalogs.catalogID and category.querystatus='true'"
                    val testquerycolumn = arrayOfNulls<String>(0)
                    val catalogsCursor = myDB!!.rawQuery(testquery, testquerycolumn)
                    mobeegalUserCursor.close()
                    if (catalogsCursor.isFirst) {
                        do {
                            val catalogs = catalogsCursor.getString(1)
                            // logger.info("catalogs = " + catalogs);
                            catalogsal.add(catalogs)
                        } while (catalogsCursor.moveToNext())
                    }
                    catalogsCursor.close();*/
                    // added new
                    val catalogcolumn = arrayOf("catalogname")
                    val catalogsCursor = myDB!!.query("category",
                            catalogcolumn, "querystatus='true'",
                            null, null, null, "catalogname")
                    catalogsindex =
                            catalogsCursor.getColumnIndexOrThrow("catalogname")
                    if (catalogsCursor.isFirst)
                    {
                        do
                        {
                            val catalogs = catalogsCursor.getString(catalogsindex)
                            //           logger.info("catalogs = " + catalogs);
                            catalogsal.add(catalogs)
                        }
                        while (catalogsCursor.moveToNext())
                    }
                    catalogsCursor.close()

                    // using category cursor
                    /* val categorycolumn = arrayOfNulls<String>(1)
                    categorycolumn[0] = "categoryname"
                    val categorysc = myDB!!.query(true, "category", categorycolumn,
                            "querystatus='true'", null, null, null, null)
                    if (categorysc.isFirst) {
                        do {
                            val categorys = categorysc.getString(0)
                            categoryal.add(categorys)
                        } while (categorysc.moveToNext())
                    }
                    categorysc.close();*/
                    val categorycolumn = arrayOfNulls<String>(1)
                    categorycolumn[0] = "categoryname"
                    val categoryscs = myDB!!.query("category",
                            categorycolumn, "querystatus='true'", null, null,
                            null, "categoryname")
                            // using category cursor
                    if (categoryscs.isFirst)
                    {
                        do
                        {
                            val categorys = categoryscs.getString(0)
                            categoryalc.add(categorys)
                            //  logger.info("categoryscs loop do-while : " + categoryalc.size);
                            //  logger.info("categoryscs loop do-while : " + categoryalc.isEmpty());
                        }
                        while (categoryscs.moveToNext())
                    }
                    categoryscs.close()

                    if (categoryalc.isEmpty())
                    {
                        js.object()
                        js.key("action").value("my_mstuff").key("query")
                                .object().key("id").value(useridColumn)
                                .endObject()
                        js.endObject()
                    }
                    else
                    {

                        js.object()
                        js.key("action").value("mstuff").key("query").object()
                                .key("id").value(useridColumn)
                                .key("mStuff_Query").array()
                        for (l in 0 until catalogsal.size)
                        {
                            js.object()
                            js.key("catalog").value(catalogsal.get(l))
                            // logger.info("catalogs " + l + " = " + catalogsal.get(l));
                            // js.key("catagory").value(categoryal.get(j)).key( "mStuff_query_criteria").array();

                            categorycolumn[0] = "categoryname"
                            val categorys = myDB!!.query("category",
                                    categorycolumn,
                                    "querystatus='true' and catalogname ='" +
                                            catalogsal.get(l) + "'", null, null,
                                    null, null)
                            if (categorys.isFirst)
                            {
                                do
                                {
                                    val category = categorys.getString(0)
                                    categoryal.add(category)
                                    //        logger.info("category loop  : " + categorys);
                                }
                                while (categorys.moveToNext())
                            }
                            for (i in 0 until categoryal.size)
                            {
                                js.key("catagory").value(categoryal.get(l))
                                        .key("mStuff_query_criteria").array()
                                        // logger.info("category " + l + " = " + categoryal.get(l));

                                if (datingstring == categoryal.get(l))
                                {
                                // Calling Dating Method
                                    dating()
                                }
                                else if (matrimonystring == categoryal.get(l))
                                {
                                // Calling Matrimony Method
                                    matrimony()
                                }
                                else if (carsstring == categoryal.get(l))
                                {
                                //Calling Cars Method
                                    cars()
                                }
                                else if (jewelrystring == categoryal.get(l))
                                {
                                //Calling Jewelry Method
                                    jewelry()
                                }
                                else if (rentalstring == categoryal.get(l))
                                {
                                //calling rental method
                                    rental()
                                }
                                else if (restaurantstring == categoryal.get(l))
                                {
                                //Calling Restaurant Method
                                    restaurants()
                                }
                                else if (moviesstring == categoryal.get(l))
                                {
                                //Calling Movies Method
                                    movies()
                                }
                                js.endArray()
                            }
                            js.endObject()
                        }
                        js.endArray()
                        js.endObject()
                        js.endObject()
                    }
                    val query = js.toString()
                    /*for (w in 0 until categoryal.size) {
                    logger.info(" categoryal  = " + categoryal.get(w))
                    myDB!!.execSQL("update category set querystatus='" + "false" + "' where categoryname='" + categoryal.get(w) + "';")
                    }*/
                    categoryal.clear()
                    catalogsal.clear()
                    logger.info(" Query = " + query)
                    val httpclient: HttpClient = DefaultHttpClient()
                    var key: String? = "intellibitz"
                    val data: ArrayList<NameValuePair> = ArrayList()
                    data.add(BasicNameValuePair("data_pack",
                            query))
                    val httpPost = HttpPost(
                            context.getString(R.string.RemoteServer))
                    httpPost.setEntity(
                            UrlEncodedFormEntity(data, HTTP.UTF_8))
                    val resp = httpclient.execute(httpPost)
                    response = HttpUtils.getResponseString(resp)
                    //EncryptionDecryption encryptDecrypt = EncryptionDecryption();
                    //val encrypted = encryptDecrypt.EncryptionDecryption(query, key);
                    //                    request = httpPost.getQueryString();
                    //decrypted = encryptDecrypt.EncryptionDecryption(response.trim(), key);
/*
                    Log.i("from client side..........................................",
                            data)
*/
                    Log.i("Server response.........................", response)
                    //logger.info("Request " + encrypted);
                    //logger.info("Decrypted Response " + decrypted);
                    //val response1 = "{\"mStuff\":[{\"catalog\":\"People\",\"category\":\"Dating\",\"result\":[]},{\"catalog\":\"People\",\"category\":\"Matrimony\",\"result\":[{\"id\":\"417391\",\"ireligion\":\"Hindu\",\"icaste\":\"BrahminShivalli\",\"iage\":\"34\",\"isex\":\"male\",\"iheight\":\"159\",\"iweight\":\"92\",\"icolor\":\"Average\",\"iarea\":\"Chandigarh\",\"icity\":\"Chandigarh\",\"icountry\":\"India\",\"ilatitude\":\"30.773958\",\"ilongitude\":\"76.801544\"},{\"id\":\"426585\",\"ireligion\":\"Christian\",\"icaste\":\"Evangelical\",\"iage\":\"35\",\"isex\":\"female\",\"iheight\":\"150\",\"iweight\":\"86\",\"icolor\":\"Average\",\"iarea\":\"padmala\",\"icity\":\"Kolhapur\",\"icountry\":\"India\",\"ilatitude\":\"16.695763\",\"ilongitude\":\"74.231132\"},{\"id\":\"475875\",\"ireligion\":\"Hindu\",\"icaste\":\"Kunbi\",\"iage\":\"31\",\"isex\":\"female\",\"iheight\":\"194\",\"iweight\":\"67\",\"icolor\":\"Any\",\"iarea\":\"Tirupathi\",\"icity\":\"Tirupathi\",\"icountry\":\"India\",\"ilatitude\":\"34.707762\",\"ilongitude\":\"-95.51566\"}]}]}";
                    // val response2 = "{\"mStuff\":[{\"catalog\":\"People\",\"category\":\"Dating\",\"result\":[{\"id\":\"378644\",\"iage\":\"42\",\"isex\":\"male\",\"iheight\":\"177\",\"iweight\":\"78\",\"iarea\":\"Kala_Danda\",\"icity\":\"Allahabad\",\"icountry\":\"India\",\"ilatitude\":\"25.437831\",\"ilongitude\":\"81.816344\"},{\"id\":\"391606\",\"iage\":\"40\",\"isex\":\"female\",\"iheight\":\"150\",\"iweight\":\"105\",\"iarea\":\"Munnar\",\"icity\":\"Munnar\",\"icountry\":\"India\",\"ilatitude\":\"0.999637\",\"ilongitude\":\"77.085548\"}]},{\"catalog\":\"People\",\"category\":\"Matrimony\",\"result\":[{\"id\":\"417391\",\"ireligion\":\"Hindu\",\"icaste\":\"BrahminShivalli\",\"iage\":\"34\",\"isex\":\"male\",\"iheight\":\"159\",\"iweight\":\"92\",\"icolor\":\"Average\",\"iarea\":\"Chandigarh\",\"icity\":\"Chandigarh\",\"icountry\":\"India\",\"ilatitude\":\"30.773958\",\"ilongitude\":\"76.801544\"},{\"id\":\"426585\",\"ireligion\":\"Christian\",\"icaste\":\"Evangelical\",\"iage\":\"35\",\"isex\":\"female\",\"iheight\":\"150\",\"iweight\":\"86\",\"icolor\":\"Average\",\"iarea\":\"padmala\",\"icity\":\"Kolhapur\",\"icountry\":\"India\",\"ilatitude\":\"16.695763\",\"ilongitude\":\"74.231132\"},{\"id\":\"475875\",\"ireligion\":\"Hindu\",\"icaste\":\"Kunbi\",\"iage\":\"31\",\"isex\":\"female\",\"iheight\":\"194\",\"iweight\":\"67\",\"icolor\":\"Any\",\"iarea\":\"Tirupathi\",\"icity\":\"Tirupathi\",\"icountry\":\"India\",\"ilatitude\":\"34.707762\",\"ilongitude\":\"-95.51566\"}]}]}";
                    val responseJson = JSONObject(response)
                    val mStuff = responseJson.getString("mStuff")
                    // logger.info("mstuff:" + mStuff);
                    val mStuffJsonArray = JSONArray(mStuff)
                    val mStuffJsonArraylength = mStuffJsonArray.length()
                    // logger.info("arry lenght :" + mStuffJsonArraylength);
                    // logger.info("mstuff:" + mStuff);

                    for (i in 0 until mStuffJsonArraylength)
                    {
                        val mStuffinnerJson = mStuffJsonArray.getJSONObject(i)
                        val mStuffinnerJsonArray = mStuffinnerJson.names()
                        val mStuffinnerJsonArraylength = mStuffinnerJsonArray.length()
                        // logger.info("arry lenghtiner :" + mStuffinnerJsonArraylength);
                        for (j in 0 until mStuffinnerJsonArraylength)
                        {
                            keys = mStuffinnerJsonArray.getString(j)
                            mStuff0i2.add(keys)
                            // logger.info("mstuff arry key:" + j + ":" + keys);
                            val cc = mStuff0i2.get(j)
                            // logger.info("keys in list :" + cc);
                            values = mStuffinnerJson.getString(keys)
                            // logger.info("mstuff arry value:" + j + ":" + values);
                            mStuff0i.add(values)
                        }
                        logger.info("keys in list length:" + mStuff0i2.size)
                        logger.info("values in list length:" + mStuff0i.size)
                        if (mStuff0i.contains("Matrimony"))
                        {
                            values = mStuff0i.get(1)
                            logger.info("inside contain matrimony" + values)
                            matrimonyResponse()
                        }
                        else if (mStuff0i.contains("Dating"))
                        {
                            values = mStuff0i.get(1)
                            logger.info("inside contain dating" + values)
                            datingResponse()
                        }
                        else if (mStuff0i.contains("Cars"))
                        {
                            values = mStuff0i.get(1)
                            logger.info("inside contain cars" + values)
                            carsResponse()
                        }
                        else if (mStuff0i.contains("Jewelry"))
                        {
                            values = mStuff0i.get(1)
                            logger.info("inside contain jewelry" + values)
                            jewelryResponse()
                        }
                        else if (mStuff0i.contains("Rental"))
                        {
                            values = mStuff0i.get(1)
                            logger.info("inside contain rental" + values)
                            rentalResponse()
                        }
                        else if (mStuff0i.contains("Restaurants"))
                        {
                            values = mStuff0i.get(1)
                            logger.info("inside contain Restaurants" + values)
                            restaurantsResponse()
                        }
                        else if (mStuff0i.contains("Movies"))
                        {
                            values = mStuff0i.get(1)
                            logger.info("inside contain Movies" + values)
                            moviesResponse()
                        }
                        mStuff0i2.clear()
                        mStuff0i.clear()
                    }
                }
                catch (e: FileNotFoundException)
                {
                    logger.info("Error = " + e.message)
                }
                catch (e: SQLiteException)
                {
                    logger.info("Error = " + e.message)
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG)
                            .show()
                }
                catch (e: JSONException)
                {
                    logger.info("Error = " + e.message)
                }
                catch (e: IOException)
                {
                    Toast.makeText(context, "Unable to Connect to Server",
                            Toast.LENGTH_LONG).show()
                }
                //Displaying MStuffdating in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null)
                    val cols = arrayOf("mStuffId", "mCatagory", "mStuffAge", "mStuffsex",
                                    "mStuffHeight", "mStuffWeight",
                                    "mStuffArea", "mStuffCity",
                                    "mStuffcountry", "mStuffLatitude",
                                    "mStuffLongitude")
                    val c = myDB!!.query("MStuffdating", cols, null,
                            null, null, null, null)

                    val mStuffId = c.getColumnIndexOrThrow("mStuffId")
                    val mStuffAge = c.getColumnIndexOrThrow("mStuffAge")
                    val mStuffHeight = c.getColumnIndexOrThrow("mStuffHeight")
                    val mStuffWeight = c.getColumnIndexOrThrow("mStuffWeight")
                    val mStuffArea = c.getColumnIndexOrThrow("mStuffArea")
                    val mStuffCity = c.getColumnIndexOrThrow("mStuffCity")
                    val mStuffcountry = c.getColumnIndexOrThrow("mStuffcountry")
                    val mStuffLatitude = c.getColumnIndexOrThrow("mStuffLatitude")
                    val mStuffLongitude = c.getColumnIndexOrThrow("mStuffLongitude")
                    val mStuffsex = c.getColumnIndexOrThrow("mStuffsex")
                    val mCatagory = c.getColumnIndexOrThrow("mCatagory")
                    myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);")

                    if (c != null)
                    {
                        if (c.isFirst)
                        {
                            myDB!!.execSQL(
                                    "delete from mStuffdetails where catagory='Dating' OR catagory='Marker';")
                            do
                            {
                                id = c.getString(mStuffId)
                                catagory = c.getString(mCatagory)
                                location = c.getString(mStuffArea) + ", " +
                                        c.getString(mStuffCity) + ", " +
                                        c.getString(mStuffcountry)
                                details = "Age=" + c.getString(mStuffAge) +
                                        ", Sex=" + c.getString(mStuffsex) +
                                        ", Height=" +
                                        c.getString(mStuffHeight) +
                                        ", Weight=" +
                                        c.getString(mStuffWeight) +
                                        ", Location=" + location
                                dblatitude = c.getString(mStuffLatitude)
                                dblongitude = c.getString(mStuffLongitude)
                                lat = dblatitude!!.toDouble()
                                lon = dblongitude!!.toDouble()
                                lat = lat * 1000000
                                lon = lon * 1000000
                                latitude = lat.toInt()
                                longitude = lon.toInt()
                                myDB!!.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');")

                            }
                            while (c.moveToNext())
                        }
                    }
                    // myDB!!.execSQL("update category set querystatus='" + "false" + "' where categoryname='" + "Dating" + "';");
                    c.close()
                }
                catch (e: Exception)
                {
                }
                //Displaying MStuffmatrimony in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null)
                    val cols1 = arrayOf("mStuffId", "mCatagory", "mReligion",
                            "mCaste", "mStuffAge",
                            "mStuffsex", "mStuffHeight", "mStuffWeight",
                            "mColor", "mStuffArea",
                            "mStuffCity", "mStuffcountry", "mStuffLatitude",
                            "mStuffLongitude")
                    val c1 = myDB!!.query("MStuffmatrimony", cols1, null,
                            null, null, null, null)
                    val mstuffId1 = c1.getColumnIndexOrThrow("mStuffId")
                    val mcatagory1 = c1.getColumnIndexOrThrow("mCatagory")
                    val mReligion1 = c1.getColumnIndexOrThrow("mReligion")
                    val mcaste1 = c1.getColumnIndexOrThrow("mCaste")
                    val mstuffAge1 = c1.getColumnIndexOrThrow("mStuffAge")
                    val mstuffsex1 = c1.getColumnIndexOrThrow("mStuffsex")
                    val mstuffHeight1 = c1.getColumnIndexOrThrow("mStuffHeight")
                    val mstuffWeight1 = c1.getColumnIndexOrThrow("mStuffWeight")
                    val mColor1 = c1.getColumnIndexOrThrow("mColor")
                    val mstuffArea1 = c1.getColumnIndexOrThrow("mStuffArea")
                    val mstuffCity1 = c1.getColumnIndexOrThrow("mStuffCity")
                    val mstuffcountry1 = c1.getColumnIndexOrThrow("mStuffcountry")
                    val mstuffLatitude1 = c1.getColumnIndexOrThrow("mStuffLatitude")
                    val mstuffLongitude1 = c1.getColumnIndexOrThrow("mStuffLongitude")
                    myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);")

                    if (c1 != null)
                    {
                        if (c1.isFirst)
                        {
                            myDB!!.execSQL(
                                    "delete from mStuffdetails where catagory='Matrimony' OR catagory='Marker';")
                            do
                            {
                                id = c1.getString(mstuffId1)
                                catagory = c1.getString(mcatagory1)
                                val religion = c1.getString(mReligion1)
                                val caste = c1.getString(mcaste1)
                                val Age = c1.getString(mstuffAge1)
                                val sex = c1.getString(mstuffsex1)
                                val height = c1.getString(mstuffHeight1)
                                val weight = c1.getString(mstuffWeight1)
                                val color = c1.getString(mColor1)
                                val area = c1.getString(mstuffArea1)
                                val city = c1.getString(mstuffCity1)
                                val country = c1.getString(mstuffcountry1)
                                location = area + ", " + city + ", " + country
                                details = "Religion=" + religion + ", Caste=" +
                                        caste + ", Age=" + Age + ", Sex=" +
                                        sex + ", Height=" + height +
                                        ", Weight=" + weight + ", Color=" +
                                        color + ", Location=" + location
                                dblatitude = c1.getString(mstuffLatitude1)
                                dblongitude = c1.getString(mstuffLongitude1)
                                lat = dblatitude!!.toDouble()
                                lon = dblongitude!!.toDouble()
                                lat = lat * 1000000
                                lon = lon * 1000000
                                latitude = lat.toInt()
                                longitude = lon.toInt()

                                myDB!!.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');")

                            }
                            while (c1.moveToNext())
                        }
                    }
                    c1.close()
                }
                catch (e: Exception)
                {
                }
                //Displaying MStuffcars in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null)
                    val cols = arrayOf("mStuffId", "mCatagory", "mStuffmake",
                            "mStuffmodel",
                            "mStuffyear", "mStuffcolor", "mStufffuel_type",
                            "mStuffprice", "mStuffarea", "mStuffcity",
                            "mStuffcountry", "mStuffLatitude", "mStuffLongitude")
                    val c = myDB!!.query("MStuffcars", cols, null, null,
                            null, null, null)
                    val mStuffId = c.getColumnIndexOrThrow("mStuffId")
                    val mStuffMake = c.getColumnIndexOrThrow("mStuffmake")
                    val mStuffModel = c.getColumnIndexOrThrow("mStuffmodel")
                    val mStuffYear = c.getColumnIndexOrThrow("mStuffyear")
                    val mStuffColor = c.getColumnIndexOrThrow("mStuffcolor")
                    val mStuffFuel_Type = c.getColumnIndexOrThrow("mStufffuel_type")
                    val mStuffPrice = c.getColumnIndexOrThrow("mStuffprice")
                    val mStuffArea = c.getColumnIndexOrThrow("mStuffarea")
                    val mStuffCity = c.getColumnIndexOrThrow("mStuffcity")
                    val mStuffcountry = c.getColumnIndexOrThrow("mStuffcountry")
                    val mStuffLatitude = c.getColumnIndexOrThrow("mStuffLatitude")
                    val mStuffLongitude = c.getColumnIndexOrThrow("mStuffLongitude")
                    val mCatagory = c.getColumnIndexOrThrow("mCatagory")
                    myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);")

                    if (c != null)
                    {
                        if (c.isFirst)
                        {
                            myDB!!.execSQL(
                                    "delete from mStuffdetails where catagory='Cars' OR catagory='Marker';")
                            do
                            {
                                id = c.getString(mStuffId)
                                catagory = c.getString(mCatagory)
                                val mstuffmake = c.getString(mStuffMake)
                                val mstuffmodel = c.getString(mStuffModel)
                                val mstuffyear = c.getString(mStuffYear)
                                val mstuffcolor = c.getString(mStuffColor)
                                val mstufffueltype = c.getString(mStuffFuel_Type)
                                val mstuffprice = c.getString(mStuffPrice)
                                location = c.getString(mStuffArea) + ", " +
                                        c.getString(mStuffCity) + ", " +
                                        c.getString(mStuffcountry)
                                details = "Make=" + mstuffmake + ", Model=" +
                                        mstuffmodel + ", Year=" + mstuffyear +
                                        ", Color=" + mstuffcolor +
                                        ", FuelType=" + mstufffueltype +
                                        ", Price=" + mstuffprice +
                                        ", Location=" + location
                                dblatitude = c.getString(mStuffLatitude)
                                dblongitude = c.getString(mStuffLongitude)
                                lat = dblatitude!!.toDouble()
                                lon = dblongitude!!.toDouble()
                                lat = lat * 1000000
                                lon = lon * 1000000
                                latitude = lat.toInt()
                                longitude = lon.toInt()
                                myDB!!.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');")

                            }
                            while (c.moveToNext())
                        }
                    }
                    c.close()
                }
                catch (e: Exception)
                {
                }
                //Displaying MStuffjewelry in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null)
                    val cols1 = arrayOf("mStuffId", "mCatagory",
                            "mStuffJewelryMake", "mStuffJewelryGender",
                            "mStuffStoneType",
                            "mStuffMetalType", "mStuffWeightRange",
                            "mStuffarea",
                            "mStuffcity", "mStuffcountry", "mStuffLatitude",
                            "mStuffLongitude")
                    val c1 = myDB!!.query("MStuffjewelry", cols1, null,
                            null, null,
                            null, null)
                    val mstuffId1 = c1.getColumnIndexOrThrow("mStuffId")
                    val mcatagory1 = c1.getColumnIndexOrThrow("mCatagory")
                    val mStuffJewelryMake1 = c1.getColumnIndexOrThrow("mStuffJewelryMake")
                    val mStuffJewelryGender1 = c1.getColumnIndexOrThrow("mStuffJewelryGender")
                    val mStuffStoneType1 = c1.getColumnIndexOrThrow("mStuffStoneType")
                    val mStuffMetalType1 = c1.getColumnIndexOrThrow("mStuffMetalType")
                    val mStuffWeightRange1 = c1.getColumnIndexOrThrow("mStuffWeightRange")
                    val mStuffarea1 = c1.getColumnIndexOrThrow("mStuffarea")
                    val mstuffcity1 = c1.getColumnIndexOrThrow("mStuffcity")
                    val mstuffcountry1 = c1.getColumnIndexOrThrow("mStuffcountry")
                    val mstuffLatitude1 = c1.getColumnIndexOrThrow("mStuffLatitude")
                    val mstuffLongitude1 = c1.getColumnIndexOrThrow("mStuffLongitude")
                    if (c1 != null)
                    {
                        if (c1.isFirst)
                        {
                            myDB!!.execSQL(
                                    "delete from mStuffdetails where catagory='Jewelry' OR catagory='Marker';")
                            do
                            {
                                id = c1.getString(mstuffId1)
                                catagory = c1.getString(mcatagory1)
                                val jewelrymake = c1.getString(mStuffJewelryMake1)
                                val jewelrygender = c1.getString(mStuffJewelryGender1)
                                val stonetype = c1.getString(mStuffStoneType1)
                                val metaltype = c1.getString(mStuffMetalType1)
                                val weightrange = c1.getString(mStuffWeightRange1)
                                val area = c1.getString(mStuffarea1)
                                val city = c1.getString(mstuffcity1)
                                val country = c1.getString(mstuffcountry1)
                                location = area + ", " + city + ", " + country
                                details = jewelrymake + jewelrygender +
                                        stonetype + metaltype + weightrange +
                                        ",Location=" + location
                                dblatitude = c1.getString(mstuffLatitude1)
                                dblongitude = c1.getString(mstuffLongitude1)
                                lat = dblatitude!!.toDouble()
                                lon = dblongitude!!.toDouble()
                                lat = lat * 1000000
                                lon = lon * 1000000
                                latitude = lat.toInt()
                                longitude = lon.toInt()

                                myDB!!.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');")

                            }
                            while (c1.moveToNext())
                        }
                    }
                    c1.close()
                }
                catch (e: Exception)
                {

                }
                //Displaying MStuffRental in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null)
                    val cols2 = arrayOf("mStuffId", "mCatagory",
                            " mStuffRentalType", "mStuffMisc", "mStuffRate",
                            "mStuffStatus", "mStuffCountry", "mStuffCity",
                            "mStuffArea", "mStuffLatitude", "mStuffLongitude")
                    val c2 = myDB!!.query("MStuffrental", cols2, null,
                            null, null,
                            null, null)
                    val mstuffId3 = c2.getColumnIndexOrThrow("mStuffId")
                    val mcatagory3 = c2.getColumnIndexOrThrow("mCatagory")
                    val mStuffRentalType3 = c2.getColumnIndexOrThrow("mStuffRentalType")
                    val mStuffMisc3 = c2.getColumnIndexOrThrow("mStuffMisc")
                    val mStuffRate3 = c2.getColumnIndexOrThrow("mStuffRate")
                    val mStuffStatus3 = c2.getColumnIndexOrThrow("mStuffStatus")
                    val mStuffCountry3 = c2.getColumnIndexOrThrow("mStuffCountry")
                    val mStuffCity3 = c2.getColumnIndexOrThrow("mStuffCity")
                    val mStuffArea3 = c2.getColumnIndexOrThrow("mStuffArea")
                    val mStuffLatitude3 = c2.getColumnIndexOrThrow("mStuffLatitude")
                    val mStuffLongitude3 = c2.getColumnIndexOrThrow("mStuffLongitude")

                    if (c2 != null)
                    {
                        if (c2.isFirst)
                        {
                            myDB!!.execSQL(
                                    "delete from mStuffdetails where catagory='Rental' OR catagory='Marker';")
                            do
                            {
                                id = c2.getString(mstuffId3)
                                catagory = c2.getString(mcatagory3)
                                val rentaltype = c2.getString(mStuffRentalType3)
                                val rentalmisc = c2.getString(mStuffMisc3)
                                val rentalrate = c2.getString(mStuffRate3)
                                val rentalstatus = c2.getString(mStuffStatus3)
                                val city = c2.getString(mStuffCity3)
                                val area = c2.getString(mStuffArea3)
                                val country = c2.getString(mStuffCountry3)
                                location = area + ", " + city + ", " + country
                                details = rentaltype + rentalmisc + rentalrate +
                                        rentalstatus + ",Location=" + location
                                dblatitude = c2.getString(mStuffLatitude3)
                                dblongitude = c2.getString(mStuffLongitude3)
                                lat = dblatitude!!.toDouble()
                                lon = dblongitude!!.toDouble()
                                lat = lat * 1000000
                                lon = lon * 1000000
                                latitude = lat.toInt()
                                longitude = lon.toInt()

                                myDB!!.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');")

                            }
                            while (c2.moveToNext())
                        }
                    }
                    c2.close()
                }
                catch (e: Exception)
                {
                }
                //Displaying MStuffRestaurants in Map
                try
                {

                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null)
                    val cols1 = arrayOf("mStuffId", "mCatagory",
                            "mStuffCuisineType", "mStuffCookingMethod",
                            "mStuffDietetic",
                            "mStuffCourseType", "mStuffDishType",
                            "mStuffMainIngredient",
                            "mStuffOccasionOrSeason", "mStuffMiscellaneous",
                            "mStuffArea", "mStuffCity", "mStuffCountry",
                            "mStuffLatitude", "mStuffLongitude")
                    val c1 = myDB!!.query("MStuffRestaurants", cols1,
                            null, null, null,
                            null, null)
                    val mstuffId1 = c1.getColumnIndexOrThrow("mStuffId")
                    val mcatagory1 = c1.getColumnIndexOrThrow("mCatagory")
                    val mStuffCuisineType = c1.getColumnIndexOrThrow("mStuffCuisineType")
                    val mStuffCookingMethod = c1.getColumnIndexOrThrow("mStuffCookingMethod")
                    val mStuffDietetic = c1.getColumnIndexOrThrow("mStuffDietetic")
                    val mStuffCourseType = c1.getColumnIndexOrThrow("mStuffCourseType")
                    val mStuffDishType = c1.getColumnIndexOrThrow("mStuffDishType")
                    val mStuffMainIngredient = c1.getColumnIndexOrThrow("mStuffMainIngredient")
                    val mStuffOccasionOrSeason = c1.getColumnIndexOrThrow("mStuffOccasionOrSeason")
                    val mStuffMiscellaneous = c1.getColumnIndexOrThrow("mStuffMiscellaneous")
                    val mStuffArea = c1.getColumnIndexOrThrow("mStuffArea")
                    val mStuffCity = c1.getColumnIndexOrThrow("mStuffCity")
                    val mStuffCountry = c1.getColumnIndexOrThrow("mStuffCountry")
                    val mstuffLatitude1 = c1.getColumnIndexOrThrow("mStuffLatitude")
                    val mstuffLongitude1 = c1.getColumnIndexOrThrow("mStuffLongitude")
                    if (c1 != null)
                    {
                        if (c1.isFirst)
                        {
                            myDB!!.execSQL(
                                    "delete from mStuffdetails where catagory='Restaurants' OR catagory='Marker';")
                            do
                            {
                                id = c1.getString(mstuffId1)
                                catagory = c1.getString(mcatagory1)
                                val cuisineType = c1.getString(mStuffCuisineType)
                                val cookingmethod = c1.getString(mStuffCookingMethod)
                                val dietetic = c1.getString(mStuffDietetic)
                                val courseType = c1.getString(mStuffCourseType)
                                val dishType = c1.getString(mStuffDishType)
                                val mainIngredient = c1.getString(mStuffMainIngredient)
                                val occasionOrSeason = c1.getString(mStuffOccasionOrSeason)
                                val Miscelleneous = c1.getString(mStuffMiscellaneous)
                                val area = c1.getString(mStuffArea)
                                val city = c1.getString(mStuffCity)
                                val country = c1.getString(mStuffCountry)
                                location = area + ", " + city + ", " + country
                                details = cuisineType + cookingmethod +
                                        dietetic + courseType + dishType +
                                        mainIngredient + occasionOrSeason +
                                        Miscelleneous + ",Location=" + location
                                dblatitude = c1.getString(mstuffLatitude1)
                                dblongitude = c1.getString(mstuffLongitude1)
                                lat = dblatitude!!.toDouble()
                                lon = dblongitude!!.toDouble()
                                lat = lat * 1000000
                                lon = lon * 1000000
                                latitude = lat.toInt()
                                longitude = lon.toInt()

                                myDB!!.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');")

                            }
                            while (c1.moveToNext())
                        }
                    }
                    c1.close()
                }
                catch (e: Exception)
                {
                }
                //Displaying MStuffmovies in Map
                try
                {
                    myDB = context.openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null)
                    val cols1 = arrayOf("mStuffId", "mCatagory",
                            "mStuffMovieType", "mStuffMovieLanguage",
                            "mStuffSeatingStyle",
                            "mStuffMovieArea", "mStuffMovieCity",
                            "mStuffMovieCountry", "mStuffMovieLatitude",
                            "mStuffMovieLongitude")
                    val c1 = myDB!!.query("MStuffMovies", cols1, null,
                            null, null,
                            null, null)
                    val mstuffId = c1.getColumnIndexOrThrow("mStuffId")
                    val mcatagory = c1.getColumnIndexOrThrow("mCatagory")
                    val mStuffMovieType = c1.getColumnIndexOrThrow("mStuffMovieType")
                    val mStuffMovieLanguage = c1.getColumnIndexOrThrow("mStuffMovieLanguage")
                    val mStuffSeatingStyle = c1.getColumnIndexOrThrow("mStuffSeatingStyle")
                    val mStuffMovieArea = c1.getColumnIndexOrThrow("mStuffMovieArea")
                    val mStuffMovieCity = c1.getColumnIndexOrThrow("mStuffMovieCity")
                    val mStuffMovieCountry = c1.getColumnIndexOrThrow("mStuffMovieCountry")
                    val mStuffMovieLatitude = c1.getColumnIndexOrThrow("mStuffMovieLatitude")
                    val mStuffMovieLongitude = c1.getColumnIndexOrThrow("mStuffMovieLongitude")
                    myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "mStuffdetails" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);")

                    if (c1 != null)
                    {
                        if (c1.isFirst)
                        {
                            myDB!!.execSQL(
                                    "delete from mStuffdetails where catagory='Movies' OR catagory='Marker';")
                            do
                            {
                                id = c1.getString(mstuffId)
                                catagory = c1.getString(mcatagory)
                                val movieType = c1.getString(mStuffMovieType)
                                val movieLanguage = c1.getString(mStuffMovieLanguage)
                                val seatingStyle = c1.getString(mStuffSeatingStyle)
                                val area = c1.getString(mStuffMovieArea)
                                val city = c1.getString(mStuffMovieCity)
                                val country = c1.getString(mStuffMovieCountry)

                                location = area + ", " + city + ", " + country
                                details = movieType + movieLanguage +
                                        seatingStyle + ",Location=" + location
                                dblatitude = c1.getString(mStuffMovieLatitude)
                                dblongitude =
                                        c1.getString(mStuffMovieLongitude)
                                lat = dblatitude!!.toDouble()
                                lon = dblongitude!!.toDouble()
                                lat = lat * 1000000
                                lon = lon * 1000000
                                latitude = lat.toInt()
                                longitude = lon.toInt()

                                myDB!!.execSQL("INSERT INTO " + "mStuffdetails" +
                                        " (mstuffid, catagory,  details, latitude, longitude, location)" +
                                        " VALUES ('" + id + "','" + catagory +
                                        "','" + details + "'," + latitude +
                                        "," + longitude + ",'" + location +
                                        "');")

                            }
                            while (c1.moveToNext())
                        }
                    }
                    c1.close()
                }
                catch (e: Exception)
                {
                }
                finally
                {
                    if (myDB != null)
                    {
                        myDB.close()
                    }
                }
                //Showing Notification Message
                var from: CharSequence? = null
                var message: CharSequence? = null
                var tickerText: String? = null
                if (response!!.contains("iarea"))
                {
                    val nm = context
                            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    from = "Matching Data"
                    message = "Matches Received"
                    tickerText = "Matches Received"
                    val appIntent = Intent()
                    val contentIntent = Intent(context, StatusbarNotification::class.java)
                    //    public Notification(Context context, int icon, CharSequence tickerText,
                    // long when, CharSequence contentTitle, CharSequence contentText,
                    //                    Intent contentIntent)

                    val notif = Notification(context,
                            R.drawable.mobeegal1,
                            tickerText,
                            System.currentTimeMillis(),
                            from,
                            message,
                            contentIntent)
                    notif.vibrate = longArrayOf(100, 250, 100, 500)
                    nm.notify(R.string.notification_message, notif)
                }
                Looper.loop()
                Looper.myLooper()!!.quit()
            }
        }.start()
    }

    fun dating()
    {
        try
        {

            val dating_Cursor = myDB!!.query(mY_Dating_TABLE, null,
                    null, null, null, null, null)
            if (dating_Cursor.getCount() > 0)
            {
                try
                {
                    val datingCursor = myDB!!.query(mY_Dating_TABLE, null,
                                    "queryStatus='true'", null, null, null,
                                    null)
                    val iStuffageColumn = datingCursor.getColumnIndexOrThrow("iage")
                    val iStuffsexColumn = datingCursor.getColumnIndexOrThrow("isex")
                    val iStuffHeightColumn = datingCursor.getColumnIndexOrThrow("iheight")
                    val iStuffWeightColumn = datingCursor.getColumnIndexOrThrow("iweight")
                    val iStuffLocationColumn = datingCursor.getColumnIndexOrThrow("iarea")
                    val iStuffCityColumn = datingCursor.getColumnIndexOrThrow("icity")
                    val iStuffCountryColumn = datingCursor.getColumnIndexOrThrow("icountry")
                    val iStufflatitudeColumn = datingCursor.getColumnIndexOrThrow("ilatitude")
                    val iStufflongitudeColumn = datingCursor.getColumnIndexOrThrow("ilongitude")

                    val uStuffageColumn = datingCursor.getColumnIndexOrThrow("uage")
                    val uStuffsexColumn = datingCursor.getColumnIndexOrThrow("usex")
                    val uStuffHeightColumn = datingCursor.getColumnIndexOrThrow("uheight")
                    val uStuffWeightColumn = datingCursor.getColumnIndexOrThrow("uweight")
                    val uStuffLocationColumn = datingCursor.getColumnIndexOrThrow("uarea")
                    val uStuffCityColumn = datingCursor.getColumnIndexOrThrow("ucity")
                    val uStuffCountryColumn = datingCursor.getColumnIndexOrThrow("ucountry")
                    val uStufflatitudeColumn = datingCursor.getColumnIndexOrThrow("ulatitude")
                    val uStufflongitudeColumn = datingCursor.getColumnIndexOrThrow("ulongitude")

                    // Getting Dating Column
                    var datingcolumn = datingCursor.getColumnNames()
                    var datingcolumn1: Array<String>? = null
                    var datingcolumn2: Array<String>? = null
                    for (j in 1 until datingcolumn.size - 2)
                    {
                        datingcolumnal.add(datingcolumn[j])
                        logger.info(
                                "datingal01 " + j + " = " + datingcolumn[j])
                    }
                    if (datingcolumnal.size > 1)
                    {
                        datingcolumn1 = extract1(datingcolumn, 0,
                                datingcolumn.size / 2)
                        datingcolumn2 = extract1(datingcolumn,
                                datingcolumn.size / 2, datingcolumn.size)
                    }
                    for (j in 1 until datingcolumn1!!.size)
                    {
                    //for (j in 1 until 10) {
                        idatingcolumnal.add(datingcolumn1!![j])
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                        logger.info("idatingcolumnal " + j + " = " +
                                datingcolumn[j])
                    }
                    for (j in 0 until datingcolumn2!!.size - 2)
                    {
                    //for (j in 1 until 10) {
                        udatingcolumnal.add(datingcolumn2!![j])
                        logger.info("udatingcolumnal " + j + " = " +
                                datingcolumn[j])
                    }
                    // Length
                    val idatingcolumnalLen = idatingcolumnal.size
                    val udatingcolumnalLen = udatingcolumnal.size
                    // Check if our result was valid.
                    if (datingCursor != null)
                    {

                        if (datingCursor.isFirst)
                        {

                            do
                            {

                                iStuffAge =
                                        datingCursor.getString(iStuffageColumn)
                                idatingal.add(iStuffAge)

                                iStuffSex =
                                        datingCursor.getString(iStuffsexColumn)
                                idatingal.add(iStuffSex)

                                iStuffHeight = datingCursor
                                        .getString(iStuffHeightColumn)
                                idatingal.add(iStuffHeight)

                                iStuffWeight = datingCursor
                                        .getString(iStuffWeightColumn)
                                idatingal.add(iStuffWeight)

                                iStuffLocation = datingCursor
                                        .getString(iStuffLocationColumn)
                                idatingal.add(iStuffLocation)

                                iStuffCity = datingCursor
                                        .getString(iStuffCityColumn)
                                idatingal.add(iStuffCity)

                                iStuffCountry = datingCursor
                                        .getString(iStuffCountryColumn)
                                idatingal.add(iStuffCountry)

                                iStufflatitude = datingCursor
                                        .getString(iStufflatitudeColumn)
                                idatingal.add(iStufflatitude)

                                iStufflongitude = datingCursor
                                        .getString(iStufflongitudeColumn)
                                idatingal.add(iStufflongitude)

                                uStuffAge =
                                        datingCursor.getString(uStuffageColumn)
                                udatingal.add(uStuffAge)

                                uStuffSex =
                                        datingCursor.getString(uStuffsexColumn)
                                udatingal.add(uStuffSex)

                                uStuffHeight = datingCursor
                                        .getString(uStuffHeightColumn)
                                udatingal.add(uStuffHeight)

                                uStuffWeight = datingCursor
                                        .getString(uStuffWeightColumn)
                                udatingal.add(uStuffWeight)

                                uStuffLocation = datingCursor
                                        .getString(uStuffLocationColumn)
                                udatingal.add(uStuffLocation)

                                uStuffCity = datingCursor
                                        .getString(uStuffCityColumn)
                                udatingal.add(uStuffCity)

                                uStuffCountry = datingCursor
                                        .getString(uStuffCountryColumn)
                                udatingal.add(uStuffCountry)

                                uStufflatitude = datingCursor
                                        .getString(uStufflatitudeColumn)
                                udatingal.add(uStufflatitude)

                                uStufflongitude = datingCursor
                                        .getString(uStufflongitudeColumn)
                                udatingal.add(uStufflongitude)

                                js.object()
                                js.key("iStuff").object()
                                for (y in 0 until idatingcolumnalLen)
                                {
                                //for (y in 0 until 9) {
                                    val idatingcolumnalString = idatingcolumnal.get(y)
                                    logger.info("idating Data" +
                                            idatingcolumnalString + " = " +
                                            idatingal.get(y))
                                    js.key(idatingcolumnalString)
                                            .value(idatingal.get(y))
                                }
                                js.endObject()
                                js.key("uStuff").object()
                                for (i in 0 until udatingcolumnalLen)
                                {
                                //for (i in 0 until udatingcolumnalLen) {
                                    val udatingcolumnalString = udatingcolumnal.get(i)
                                    logger.info("udating Data" +
                                            udatingcolumnalString + "= " +
                                            udatingal.get(i))
                                    js.key(udatingcolumnalString)
                                            .value(udatingal.get(i))
                                }
                                js.endObject()
                                idatingal.clear()
                                udatingal.clear()
                                js.endObject()
                            }
                            while (datingCursor.moveToNext())
                        }
                    }
                    myDB!!.execSQL(
                            "update category set querystatus='false' where categoryname='Dating'")
                    datingCursor.close()
                }
                catch (e: JSONException)
                {
                    logger.info("Error = " + e.message)
                }
            }
        }
        catch (e: Exception)
        {

        }
    }

    fun datingResponse()
    {
        try
        {
            val mStuffJsonArray1 = JSONArray(values)
            val mStuffJsonArraylength1 = mStuffJsonArray1.length()
            for (l in 0 until mStuffJsonArraylength1)
            {
                val mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l)
                val mStuffinnerJsonArray1 = mStuffinnerJson1.names()
                val mStuffinnerJsonArraylength1 = mStuffinnerJsonArray1.length()

                for (k in 0 until mStuffinnerJsonArraylength1)
                {
                    val keys1 = mStuffinnerJsonArray1.getString(k)
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    val values1 = mStuffinnerJson1.getString(keys1)
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1)
                }

                myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffdating" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffAge VARCHAR,mStuffsex VARCHAR, mStuffHeight VARCHAR,mStuffWeight VARCHAR, mStuffArea VARCHAR,mStuffCity VARCHAR, mStuffcountry VARCHAR,mStuffLatitude VARCHAR, mStuffLongitude VARCHAR);")
                myDB!!.execSQL(
                        "INSERT INTO MStuffdating (mStuffId,mCatagory,mStuffAge,mStuffsex,mStuffHeight,mStuffWeight,mStuffArea,mStuffCity,mStuffcountry,mStuffLatitude,mStuffLongitude)VALUES('" +
                                mStuff0i1.get(7) + "','" + "Dating" + "','" +
                                mStuff0i1.get(6) + "','" + mStuff0i1.get(9) +
                                "','" + mStuff0i1.get(0) + "','" +
                                mStuff0i1.get(1) + "','" + mStuff0i1.get(5) +
                                "','" + mStuff0i1.get(2) + "','" +
                                mStuff0i1.get(8) + "','" + mStuff0i1.get(3) +
                                "','" + mStuff0i1.get(4) + "');")
                mStuff0i1.clear()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
    }

    fun matrimony()
    {
        try
        {
            val matrimony_Cursor = myDB!!.query(mY_Matrimony_TABLE, null,
                    null, null, null, null, null)
            if (matrimony_Cursor.getCount() > 0)
            {
                try
                {

                    val matrimonyCursor = myDB!!.query(mY_Matrimony_TABLE, null,
                                    "queryStatus='true'", null, null, null,
                                    null)

                    val ireligionColumn = matrimonyCursor.getColumnIndexOrThrow("ireligion")
                    val icasteColumn = matrimonyCursor.getColumnIndexOrThrow("icaste")
                    val iageColumn = matrimonyCursor.getColumnIndexOrThrow("iage")
                    val isexColumn = matrimonyCursor.getColumnIndexOrThrow("isex")
                    val iheightColumn = matrimonyCursor.getColumnIndexOrThrow("iheight")
                    val iweightColumn = matrimonyCursor.getColumnIndexOrThrow("iweight")
                    val icolorColumn = matrimonyCursor.getColumnIndexOrThrow("icolor")
                    val iareaColumn = matrimonyCursor.getColumnIndexOrThrow("iarea")
                    val icityColumn = matrimonyCursor.getColumnIndexOrThrow("icity")
                    val icountryColumn = matrimonyCursor.getColumnIndexOrThrow("icountry")
                    val ilatitudeColumn = matrimonyCursor.getColumnIndexOrThrow("ilatitude")
                    val ilongitudeColumn = matrimonyCursor.getColumnIndexOrThrow("ilongitude")

                    val ureligionColumn = matrimonyCursor.getColumnIndexOrThrow("ureligion")
                    val ucasteColumn = matrimonyCursor.getColumnIndexOrThrow("ucaste")
                    val uageColumn = matrimonyCursor.getColumnIndexOrThrow("uage")
                    val usexColumn = matrimonyCursor.getColumnIndexOrThrow("usex")
                    val uheightColumn = matrimonyCursor.getColumnIndexOrThrow("uheight")
                    val uweightColumn = matrimonyCursor.getColumnIndexOrThrow("uweight")
                    val ucolorColumn = matrimonyCursor.getColumnIndexOrThrow("ucolor")
                    val uareaColumn = matrimonyCursor.getColumnIndexOrThrow("uarea")
                    val ucityColumn = matrimonyCursor.getColumnIndexOrThrow("ucity")
                    val ucountryColumn = matrimonyCursor.getColumnIndexOrThrow("ucountry")
                    val ulatitudeColumn = matrimonyCursor.getColumnIndexOrThrow("ulatitude")
                    val ulongitudeColumn = matrimonyCursor.getColumnIndexOrThrow("ulongitude")

                    // Getting Dating Column
                    var matrimonycolumn = matrimonyCursor.getColumnNames()
                    var imatrimonycolumn: Array<String>? = null
                    var umatrimonycolumn: Array<String>? = null

                    for (j in 1 until matrimonycolumn.size - 2)
                    {
                        matrimonycolumnal.add(matrimonycolumn[j])
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                    }
                    if (matrimonycolumnal.size > 1)
                    {
                        imatrimonycolumn = extract1(matrimonycolumn, 0,
                                matrimonycolumn.size / 2)
                        umatrimonycolumn = extract1(matrimonycolumn,
                                matrimonycolumn.size / 2,
                                matrimonycolumn.size)
                    }
                    for (j in 1 until imatrimonycolumn!!.size)
                    {
                        iMatrimonycolumnal.add(imatrimonycolumn!![j])
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                    }
                    for (j in 0 until umatrimonycolumn!!.size - 2)
                    {
                        uMatrimonycolumnal.add(umatrimonycolumn!![j])
                        // logger.info("datingal01 " + j + " = " + datingcolumn[j]);
                    }
                    // Length
                    val imatrimonycolumnalLen = iMatrimonycolumnal.size
                    val umatrimonycolumnalLen = uMatrimonycolumnal.size

                    // Check if our result was valid.
                    if (matrimonyCursor != null)
                    {
                        if (matrimonyCursor.isFirst)
                        {
                            do
                            {
                                iReligion = matrimonyCursor
                                        .getString(ireligionColumn)
                                iMatrimony.add(iReligion)

                                iCaste =
                                        matrimonyCursor.getString(icasteColumn)
                                iMatrimony.add(iCaste)

                                iAge = matrimonyCursor.getString(iageColumn)
                                iMatrimony.add(iAge)

                                iSex = matrimonyCursor.getString(isexColumn)
                                iMatrimony.add(iSex)

                                iHeight = matrimonyCursor
                                        .getString(iheightColumn)
                                iMatrimony.add(iHeight)

                                iWeight = matrimonyCursor
                                        .getString(iweightColumn)
                                iMatrimony.add(iWeight)

                                iColor =
                                        matrimonyCursor.getString(icolorColumn)
                                iMatrimony.add(iColor)

                                iArea = matrimonyCursor.getString(iareaColumn)
                                iMatrimony.add(iArea)

                                iCity = matrimonyCursor.getString(icityColumn)
                                iMatrimony.add(iCity)

                                iCountry = matrimonyCursor
                                        .getString(icountryColumn)
                                iMatrimony.add(iCountry)

                                iLatitude = matrimonyCursor
                                        .getString(ilatitudeColumn)
                                iMatrimony.add(iLatitude)

                                iLongitude = matrimonyCursor
                                        .getString(ilongitudeColumn)
                                iMatrimony.add(iLongitude)

                                uReligion = matrimonyCursor
                                        .getString(ureligionColumn)
                                uMatrimony.add(uReligion)

                                uCaste =
                                        matrimonyCursor.getString(ucasteColumn)
                                uMatrimony.add(uCaste)

                                uAge = matrimonyCursor.getString(uageColumn)
                                uMatrimony.add(uAge)

                                uSex = matrimonyCursor.getString(usexColumn)
                                uMatrimony.add(uSex)

                                uHeight = matrimonyCursor
                                        .getString(uheightColumn)
                                uMatrimony.add(uHeight)

                                uWeight = matrimonyCursor
                                        .getString(uweightColumn)
                                uMatrimony.add(uWeight)

                                uColor =
                                        matrimonyCursor.getString(ucolorColumn)
                                uMatrimony.add(uColor)

                                uArea = matrimonyCursor.getString(uareaColumn)
                                uMatrimony.add(uArea)

                                uCity = matrimonyCursor.getString(ucityColumn)
                                uMatrimony.add(uCity)

                                uCountry = matrimonyCursor
                                        .getString(ucountryColumn)
                                uMatrimony.add(uCountry)

                                uLatitude = matrimonyCursor
                                        .getString(ulatitudeColumn)
                                uMatrimony.add(uLatitude)

                                uLongitude = matrimonyCursor
                                        .getString(ulongitudeColumn)
                                uMatrimony.add(uLongitude)

                                js.object()
                                js.key("iStuff").object()
                                for (y in 0 until imatrimonycolumnalLen)
                                {
                                    val imatrimonycolumnalString = iMatrimonycolumnal.get(y)
                                    // logger.info("dating Data" +idatingcolumnalString // + " = " + iMatrimony.get(y));
                                    js.key(imatrimonycolumnalString)
                                            .value(iMatrimony.get(y))
                                }
                                js.endObject()
                                js.key("uStuff").object()
                                for (i in 0 until umatrimonycolumnalLen)
                                {
                                    val umatrimonycolumnalString = uMatrimonycolumnal.get(i)
                                    // logger.info("dating Data" + udatingcolumnalString
                                    // + " = " + uMatrimony.get(i));
                                    js.key(umatrimonycolumnalString)
                                            .value(uMatrimony.get(i))
                                }
                                js.endObject()
                                iMatrimony.clear()
                                uMatrimony.clear()
                                js.endObject()
                            }
                            while (matrimonyCursor.moveToNext())
                        }
                        myDB!!.execSQL(
                                "update category set querystatus='false' where categoryname='Matrimony'")
                        matrimonyCursor.close()
                    }

                }
                catch (e: JSONException)
                {
                // logger.info("Error = " + e.message);
                }
                matrimonycolumnal.clear()
                iMatrimonycolumnal.clear()
                uMatrimonycolumnal.clear()
            }

        }
        catch (e: Exception)
        {

        }
    }

    fun matrimonyResponse()
    {
        try
        {
            val mStuffJsonArray1 = JSONArray(values)
            val mStuffJsonArraylength1 = mStuffJsonArray1.length()
            for (l in 0 until mStuffJsonArraylength1)
            {
                val mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l)
                val mStuffinnerJsonArray1 = mStuffinnerJson1.names()
                val mStuffinnerJsonArraylength1 = mStuffinnerJsonArray1.length()

                for (k in 0 until mStuffinnerJsonArraylength1)
                {
                    val keys1 = mStuffinnerJsonArray1.getString(k)
                    // logger.info("mstuff arry key1:" + k + ":" + keys1);
                    val values1 = mStuffinnerJson1.getString(keys1)
                    // logger.info("mstuff arry value1:" + k +":" + values1);
                    mStuff0i1.add(values1)
                }
                myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffmatrimony" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mReligion VARCHAR,mCaste VARCHAR,mStuffAge VARCHAR,mStuffsex VARCHAR,mStuffHeight VARCHAR,mStuffWeight VARCHAR,mColor VARCHAR,mStuffArea VARCHAR,mStuffCity VARCHAR,mStuffcountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR);")
                myDB!!.execSQL(
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
                                "');")
                mStuff0i1.clear()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
    }

    fun cars()
    {
        try
        {
            val cars_Cursor = myDB!!.query(mY_Cars_TABLE, null,
                    null, null, null, null, null)
            if (cars_Cursor.getCount() > 0)
            {
                try
                {
                    val carsCursor = myDB!!.query(mY_Cars_TABLE, null,
                            "queryStatus='true'", null, null, null, null)
                    val imakeColumn = carsCursor.getColumnIndexOrThrow("imake")
                    val imodelColumn = carsCursor.getColumnIndexOrThrow("imodel")
                    val iyearColumn = carsCursor.getColumnIndexOrThrow("iyear")
                    val icolorColumn = carsCursor.getColumnIndexOrThrow("icolor")
                    val ifuel_typeColumn = carsCursor.getColumnIndexOrThrow("ifuel_type")
                    val ipriceColumn = carsCursor.getColumnIndexOrThrow("iprice")
                    val iareaColumn = carsCursor.getColumnIndexOrThrow("iarea")
                    val icityColumn = carsCursor.getColumnIndexOrThrow("icity")
                    val icountryColumn = carsCursor.getColumnIndexOrThrow("icountry")
                    val ilatitudeColumn = carsCursor.getColumnIndexOrThrow("ilatitude")
                    val ilongitudeColumn = carsCursor.getColumnIndexOrThrow("ilongitude")

                    val umakeColumn = carsCursor.getColumnIndexOrThrow("umake")
                    val umodelColumn = carsCursor.getColumnIndexOrThrow("umodel")
                    val uyearColumn = carsCursor.getColumnIndexOrThrow("uyear")
                    val ucolorColumn = carsCursor.getColumnIndexOrThrow("ucolor")
                    val ufuel_typeColumn = carsCursor.getColumnIndexOrThrow("ufuel_type")
                    val upriceColumn = carsCursor.getColumnIndexOrThrow("uprice")
                    val uareaColumn = carsCursor.getColumnIndexOrThrow("uarea")
                    val ucityColumn = carsCursor.getColumnIndexOrThrow("ucity")
                    val ucountryColumn = carsCursor.getColumnIndexOrThrow("ucountry")
                    val ulatitudeColumn = carsCursor.getColumnIndexOrThrow("ulatitude")
                    val ulongitudeColumn = carsCursor.getColumnIndexOrThrow("ulongitude")
                    // Getting Cars Column
                    var carscolumn = carsCursor.getColumnNames()
                    var icarscolumn: Array<String>? = null
                    var ucarscolumn: Array<String>? = null

                    for (j in 1 until carscolumn.size - 2)
                    {
                        carscolumnal.add(carscolumn[j])
                        //logger.info("carscolumns:" + carscolumn[j]);
                        // Log.i(".....................", carscolumn[j]);
                    }

                    if (carscolumnal.size > 1)
                    {
                        icarscolumn =
                                extract1(carscolumn, 0, carscolumn.size / 2)
                        ucarscolumn = extract1(carscolumn,
                                carscolumn.size / 2, carscolumn.size)
                    }

                    for (j in 1 until icarscolumn!!.size)
                    {
                        iCarscolumnal.add(icarscolumn!![j])
                        //logger.info("icarscolumnal:" + icarscolumn!![j]);
                    }

                    for (j in 0 until ucarscolumn!!.size - 2)
                    {
                        uCarscolumnal.add(ucarscolumn!![j])
                        // logger.info("ucarscolumnal:" + ucarscolumn!![j]);
                    }
                    // Length
                    val icarscolumnalLen = iCarscolumnal.size
                    val ucarscolumnalLen = uCarscolumnal.size

                    // Check if our result was valid.
                    if (carsCursor != null)
                    {

                        if (carsCursor.isFirst)
                        {
                            do
                            {
                                iCarMake = carsCursor.getString(imakeColumn)
                                iCars.add(iCarMake)

                                iCarModel = carsCursor.getString(imodelColumn)
                                iCars.add(iCarModel)

                                iCarYear = carsCursor.getString(iyearColumn)
                                iCars.add(iCarYear)

                                iCarColor = carsCursor.getString(icolorColumn)
                                iCars.add(iCarColor)

                                iCarFuel_Type =
                                        carsCursor.getString(ifuel_typeColumn)
                                iCars.add(iCarFuel_Type)

                                iCarPrice = carsCursor.getString(ipriceColumn)
                                iCars.add(iCarPrice)

                                iCarArea = carsCursor.getString(iareaColumn)
                                iCars.add(iCarArea)

                                iCarCity = carsCursor.getString(icityColumn)
                                iCars.add(iCarCity)

                                iCarCountry =
                                        carsCursor.getString(icountryColumn)
                                iCars.add(iCarCountry)

                                iCarLatitude =
                                        carsCursor.getString(ilatitudeColumn)
                                iCars.add(iCarLatitude)

                                iCarLongitude =
                                        carsCursor.getString(ilongitudeColumn)
                                iCars.add(iCarLongitude)

                                uCarMake = carsCursor.getString(umakeColumn)
                                uCars.add(uCarMake)

                                uCarModel = carsCursor.getString(umodelColumn)
                                uCars.add(uCarModel)

                                uCarYear = carsCursor.getString(uyearColumn)
                                uCars.add(uCarYear)

                                uCarColor = carsCursor.getString(ucolorColumn)
                                uCars.add(uCarColor)

                                uCarFuel_Type =
                                        carsCursor.getString(ufuel_typeColumn)
                                uCars.add(uCarFuel_Type)

                                uCarPrice = carsCursor.getString(upriceColumn)
                                uCars.add(uCarPrice)

                                uCarArea = carsCursor.getString(uareaColumn)
                                uCars.add(uCarArea)

                                uCarCity = carsCursor.getString(ucityColumn)
                                uCars.add(uCarCity)

                                uCarCountry =
                                        carsCursor.getString(ucountryColumn)
                                uCars.add(uCarCountry)

                                uCarLatitude =
                                        carsCursor.getString(ulatitudeColumn)
                                uCars.add(uCarLatitude)

                                uCarLongitude =
                                        carsCursor.getString(ulongitudeColumn)
                                uCars.add(uCarLongitude)

                                js.object()
                                js.key("iStuff").object()

                                for (y in 0 until icarscolumnalLen)
                                {
                                    val icarscolumnalString = iCarscolumnal.get(y)
                                    // logger.info("icarscolumnalstring" + icarscolumnalString + " = " + iCars.get(y));
                                    js.key(icarscolumnalString)
                                            .value(iCars.get(y))
                                }
                                js.endObject()
                                js.key("uStuff").object()
                                for (i in 0 until ucarscolumnalLen)
                                {
                                    val ucarscolumnalString = uCarscolumnal.get(i)
                                    //logger.info("ucarscolumnalstring" + ucarscolumnalString + " = " + uCars.get(i));
                                    js.key(ucarscolumnalString)
                                            .value(uCars.get(i))
                                }
                                js.endObject()
                                js.endObject()
                                iCars.clear()
                                uCars.clear()

                            }
                            while (carsCursor.moveToNext())
                        }
                        myDB!!.execSQL(
                                "update category set querystatus='false' where categoryname='Cars'")
                        carsCursor.close()
                    }
                }
                catch (e: JSONException)
                {
                    logger.info("Error = " + e.message)
                }
                carscolumnal.clear()
                iCarscolumnal.clear()
                uCarscolumnal.clear()
            }

        }
        catch (e: Exception)
        {

        }
    }

    fun carsResponse()
    {
        try
        {
            val mStuffJsonArray1 = JSONArray(values)
            val mStuffJsonArraylength1 = mStuffJsonArray1.length()
            for (l in 0 until mStuffJsonArraylength1)
            {
                val mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l)
                val mStuffinnerJsonArray1 = mStuffinnerJson1.names()

                val mStuffinnerJsonArraylength1 = mStuffinnerJsonArray1.length()
                for (k in 0 until mStuffinnerJsonArraylength1)
                {
                    val keys1 = mStuffinnerJsonArray1.getString(k)
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    val values1 = mStuffinnerJson1.getString(keys1)
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1)
                    logger.info(mStuff0i1.get(k))
                }
                myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffcars" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffmake VARCHAR,mStuffmodel VARCHAR,mStuffyear NUMERIC,mStuffcolor VARCHAR,mStufffuel_type VARCHAR,mStuffprice VARCHAR,mStuffarea VARCHAR,mStuffcity VARCHAR,mStuffcountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );")
                myDB!!.execSQL(
                        "INSERT INTO MStuffcars (mStuffId,mCatagory,mStuffmake,mStuffmodel,mStuffyear,mStuffcolor,mStufffuel_type,mStuffprice,mStuffarea,mStuffcity,mStuffcountry,mStuffLatitude,mStuffLongitude) VALUES ('" +
                                mStuff0i1.get(10) + "','" + "Cars" + "','" +
                                mStuff0i1.get(7) + "','" + mStuff0i1.get(1) +
                                "','" + mStuff0i1.get(6) + "','" +
                                mStuff0i1.get(9) + "','" + mStuff0i1.get(8) +
                                "','" + mStuff0i1.get(0) + "','" +
                                mStuff0i1.get(5) + "','" + mStuff0i1.get(2) +
                                "','" + mStuff0i1.get(11) + "','" +
                                mStuff0i1.get(3) + "','" + mStuff0i1.get(4) +
                                "');")
                mStuff0i1.clear()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
    }

    fun jewelry()
    {
        try
        {
            val jewelry_Cursor = myDB!!.query(mY_Jewelry_TABLE, null,
                    null, null, null, null, null)
            if (jewelry_Cursor.getCount() > 0)
            {
                try
                {
                    val jewelryCursor = myDB!!.query(mY_Jewelry_TABLE,
                            null, "queryStatus='true'", null, null, null, null)
                    val iStuffitemtypeColumn = jewelryCursor.getColumnIndexOrThrow("ijewelry")
                    val iStuffgenderColumn = jewelryCursor.getColumnIndexOrThrow("igender")
                    val iStuffstonetypeColumn = jewelryCursor.getColumnIndexOrThrow("istone")
                    val iStuffmetaltypeColumn = jewelryCursor.getColumnIndexOrThrow("imetal")
                    val iStuffweightColumn = jewelryCursor.getColumnIndexOrThrow("iweight")
                    val iStuffareaColumn = jewelryCursor.getColumnIndexOrThrow("iarea")
                    val iStuffcityColumn = jewelryCursor.getColumnIndexOrThrow("icity")
                    val iStuffcountryColumn = jewelryCursor.getColumnIndexOrThrow("icountry")
                    val iStufflatitudeColumn = jewelryCursor.getColumnIndexOrThrow("ilatitude")
                    val iStufflongitudeColumn = jewelryCursor.getColumnIndexOrThrow("ilongitude")

                    val uStuffitemtypeColumn = jewelryCursor.getColumnIndexOrThrow("ujewelry")
                    val uStuffgenderColumn = jewelryCursor.getColumnIndexOrThrow("ugender")
                    val uStuffstonetypeColumn = jewelryCursor.getColumnIndexOrThrow("ustone")
                    val uStuffmetaltypeColumn = jewelryCursor.getColumnIndexOrThrow("umetal")
                    val uStuffweightColumn = jewelryCursor.getColumnIndexOrThrow("uweight")
                    val uStuffareaColumn = jewelryCursor.getColumnIndexOrThrow("uarea")
                    val uStuffcityColumn = jewelryCursor.getColumnIndexOrThrow("ucity")
                    val uStuffcountryColumn = jewelryCursor.getColumnIndexOrThrow("ucountry")
                    val uStufflatitudeColumn = jewelryCursor.getColumnIndexOrThrow("ulatitude")
                    val uStufflongitudeColumn = jewelryCursor.getColumnIndexOrThrow("ulongitude")
                    // Getting Jewelry Column
                    var jewelrycolumn = jewelryCursor.getColumnNames()
                    var ijewelrycolumn: Array<String>? = null
                    var ujewelrycolumn: Array<String>? = null
                    for (j in 1 until jewelrycolumn.size - 2)
                    {
                        jewelrycolumnal.add(jewelrycolumn[j])
                        logger.info("jewelrycolumn:" + jewelrycolumn[j])
                    }

                    if (jewelrycolumnal.size > 1)
                    {
                        ijewelrycolumn = extract1(jewelrycolumn, 0,
                                jewelrycolumn.size / 2)
                        ujewelrycolumn = extract1(jewelrycolumn,
                                jewelrycolumn.size / 2, jewelrycolumn.size)
                    }
                    for (j in 1 until ijewelrycolumn!!.size)
                    {
                        iJewelrycolumnal.add(ijewelrycolumn!![j])
                        logger.info("ijewelrycolumn:" + ijewelrycolumn!![j])
                    }

                    for (j in 0 until ujewelrycolumn!!.size - 2)
                    {
                        uJewelrycolumnal.add(ujewelrycolumn!![j])
                        logger.info("ujewelrycolumn:" + ujewelrycolumn!![j])
                    }
                    // Length
                    val ijewelrycolumnalLen = iJewelrycolumnal.size
                    val ujewelrycolumnalLen = uJewelrycolumnal.size

                    if (jewelryCursor != null)
                    {
                        if (jewelryCursor.isFirst)
                        {
                            do
                            {
                                iJewelryType = jewelryCursor
                                        .getString(iStuffitemtypeColumn)
                                iJewelry.add(iJewelryType)

                                iGender = jewelryCursor
                                        .getString(iStuffgenderColumn)
                                iJewelry.add(iGender)

                                iStoneType = jewelryCursor
                                        .getString(iStuffstonetypeColumn)
                                iJewelry.add(iStoneType)

                                iMetalType = jewelryCursor
                                        .getString(iStuffmetaltypeColumn)
                                iJewelry.add(iMetalType)

                                iJewelryWeight = jewelryCursor
                                        .getString(iStuffweightColumn)
                                iJewelry.add(iJewelryWeight)

                                iJewelryArea = jewelryCursor
                                        .getString(iStuffareaColumn)
                                iJewelry.add(iJewelryArea)

                                iJewelryCity = jewelryCursor
                                        .getString(iStuffcityColumn)
                                iJewelry.add(iJewelryCity)

                                iJewelryCountry = jewelryCursor
                                        .getString(iStuffcountryColumn)
                                iJewelry.add(iJewelryCountry)

                                iJewelryLatitude = jewelryCursor
                                        .getString(iStufflatitudeColumn)
                                iJewelry.add(iJewelryLatitude)

                                iJewelryLongitude = jewelryCursor
                                        .getString(iStufflongitudeColumn)
                                iJewelry.add(iJewelryLongitude)

                                uJewelryType = jewelryCursor
                                        .getString(uStuffitemtypeColumn)
                                uJewelry.add(uJewelryType)

                                uGender = jewelryCursor
                                        .getString(uStuffgenderColumn)
                                uJewelry.add(uGender)

                                uStoneType = jewelryCursor
                                        .getString(uStuffstonetypeColumn)
                                uJewelry.add(uStoneType)

                                uMetalType = jewelryCursor
                                        .getString(uStuffmetaltypeColumn)
                                uJewelry.add(uMetalType)

                                uJewelryWeight = jewelryCursor
                                        .getString(uStuffweightColumn)
                                uJewelry.add(uJewelryWeight)

                                uJewelryArea = jewelryCursor
                                        .getString(uStuffareaColumn)
                                uJewelry.add(uJewelryArea)

                                uJewelryCity = jewelryCursor
                                        .getString(uStuffcityColumn)
                                uJewelry.add(uJewelryCity)

                                uJewelryCountry = jewelryCursor
                                        .getString(uStuffcountryColumn)
                                uJewelry.add(uJewelryCountry)

                                uJewelryLatitude = jewelryCursor
                                        .getString(uStufflatitudeColumn)
                                uJewelry.add(uJewelryLatitude)

                                uJewelryLongitude = jewelryCursor
                                        .getString(uStufflongitudeColumn)
                                uJewelry.add(uJewelryLongitude)

                                js.object()
                                js.key("iStuff").object()
                                for (y in 0 until ijewelrycolumnalLen)
                                {
                                    val ijewelrycolumnalString = iJewelrycolumnal.get(y)
                                    logger.info("ijewelrycolumnalString" +
                                            ijewelrycolumnalString + " = " +
                                            iJewelry.get(y))
                                    js.key(ijewelrycolumnalString)
                                            .value(iJewelry.get(y))
                                }
                                js.endObject()
                                js.key("uStuff").object()
                                for (i in 0 until ujewelrycolumnalLen)
                                {
                                    val ujewelrycolumnalString = uJewelrycolumnal.get(i)
                                    logger.info("ujewelrycolumnalString" +
                                            ujewelrycolumnalString + " = " +
                                            uJewelry.get(i))
                                    js.key(ujewelrycolumnalString)
                                            .value(uJewelry.get(i))
                                }
                                js.endObject()
                                js.endObject()
                                iJewelry.clear()
                                uJewelry.clear()
                            }
                            while (jewelryCursor.moveToNext())
                        }
                        myDB!!.execSQL(
                                "update category set querystatus='false' where categoryname='Jewelry'")
                        jewelryCursor.close()
                    }

                }
                catch (e: JSONException)
                {
                    logger.info("Error = " + e.message)
                }
                jewelrycolumnal.clear()
                iJewelrycolumnal.clear()
                uJewelrycolumnal.clear()
            }
        }
        catch (e: Exception)
        {

        }
    }

    fun jewelryResponse()
    {
        try
        {
            val mStuffJsonArray1 = JSONArray(values)
            logger.info("mStuffinnerJsonArray1" + values)
            val mStuffJsonArraylength1 = mStuffJsonArray1.length()
            for (l in 0 until mStuffJsonArraylength1)
            {
                val mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l)
                val mStuffinnerJsonArray1 = mStuffinnerJson1.names()

                val mStuffinnerJsonArraylength1 = mStuffinnerJsonArray1.length()
                for (k in 0 until mStuffinnerJsonArraylength1)
                {
                    val keys1 = mStuffinnerJsonArray1.getString(k)
                    val values1 = mStuffinnerJson1.getString(keys1)
                    logger.info("mstuff arry value1:" + k + ":" + values1)
                    mStuff0i1.add(values1)
                    logger.info(mStuff0i1.get(k))
                }

                myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffjewelry" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffJewelryMake VARCHAR,mStuffJewelryGender VARCHAR,mStuffStoneType NUMERIC,mStuffMetalType VARCHAR,mStuffWeightRange VARCHAR,mStuffarea VARCHAR,mStuffcity VARCHAR,mStuffcountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );")
                myDB!!.execSQL(
                        "INSERT INTO MStuffjewelry (mStuffId,mCatagory,mStuffJewelryMake,mStuffJewelryGender,mStuffStoneType,mStuffMetalType,mStuffWeightRange,mStuffarea,mStuffcity,mStuffcountry,mStuffLatitude,mStuffLongitude) VALUES ('" +
                                mStuff0i1.get(7) + "','" + "Jewelry" + "','" +
                                mStuff0i1.get(1) + "','" + mStuff0i1.get(6) +
                                "','" + mStuff0i1.get(8) + "','" +
                                mStuff0i1.get(10) + "','" + mStuff0i1.get(0) +
                                "','" + mStuff0i1.get(5) + "','" +
                                mStuff0i1.get(2) + "','" + mStuff0i1.get(9) +
                                "','" + mStuff0i1.get(3) + "','" +
                                mStuff0i1.get(4) + "');")
                mStuff0i1.clear()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
    }

    fun rental()
    {
        try
        {
            val rental_Cursor = myDB!!.query(mY_home_TABLE, null,
                    null, null, null, null, null)
            if (rental_Cursor.getCount() > 0)
            {
                try
                {
                    val rentalCursor = myDB!!.query(mY_home_TABLE, null,
                            "queryStatus='true'", null, null, null, null)

                    val iStuffrentaltypeColumn = rentalCursor.getColumnIndexOrThrow("irental")
                    val iStuffrentalmiscColumn = rentalCursor.getColumnIndexOrThrow("imisc")
                    val iStuffrentalrateColumn = rentalCursor.getColumnIndexOrThrow("irate")
                    val iStuffstatusColumn = rentalCursor.getColumnIndexOrThrow("istatus")
                    val iStuffrentalcountryColumn = rentalCursor.getColumnIndexOrThrow("icountry")
                    val iStuffrentalcityColumn = rentalCursor.getColumnIndexOrThrow("icity")
                    val iStuffrentalareaColumn = rentalCursor.getColumnIndexOrThrow("iarea")
                    val iStufflatitudeColumn = rentalCursor.getColumnIndexOrThrow("ilatitude")
                    val iStufflongitudeColumn = rentalCursor.getColumnIndexOrThrow("ilongitude")

                    val uStuffrentaltypeColumn = rentalCursor.getColumnIndexOrThrow("urental")
                    val uStuffrentalmiscColumn = rentalCursor.getColumnIndexOrThrow("umisc")
                    val uStuffrentalrateColumn = rentalCursor.getColumnIndexOrThrow("urate")
                    val uStuffstatusColumn = rentalCursor.getColumnIndexOrThrow("ustatus")
                    val uStuffrentalcountryColumn = rentalCursor.getColumnIndexOrThrow("ucountry")
                    val uStuffrentalcityColumn = rentalCursor.getColumnIndexOrThrow("ucity")
                    val uStuffrentalareaColumn = rentalCursor.getColumnIndexOrThrow("uarea")
                    val uStufflatitudeColumn = rentalCursor.getColumnIndexOrThrow("ulatitude")
                    val uStufflongitudeColumn = rentalCursor.getColumnIndexOrThrow("ulongitude")

                    // Getting Rental Column
                    var rentalcolumn = rentalCursor.getColumnNames()
                    var irentalcolumn: Array<String>? = null
                    var urentalcolumn: Array<String>? = null
                    for (j in 1 until rentalcolumn.size - 2)
                    {
                        rentalcolumnal.add(rentalcolumn[j])
                        //logger.info("rentalcolumns:" + rentalcolumn[j]);
                        // Log.i(".....................", rentalcolumn[j]);
                    }
                    if (rentalcolumnal.size > 1)
                    {
                        irentalcolumn = extract1(rentalcolumn, 0,
                                rentalcolumn.size / 2)
                        urentalcolumn = extract1(rentalcolumn,
                                rentalcolumn.size / 2, rentalcolumn.size)
                    }
                    for (j in 1 until irentalcolumn!!.size)
                    {
                        iRentalcolumnal.add(irentalcolumn!![j])
                        //logger.info("irentalcolumnal:" + irentalcolumn!![j]);
                    }

                    for (j in 0 until urentalcolumn!!.size - 2)
                    {
                        uRentalcolumnal.add(urentalcolumn!![j])
                        // logger.info("urentalcolumnal:" + urentalcolumn!![j]);
                    }
                    val irentalcolumnalLen = iRentalcolumnal.size
                    val urentalcolumnalLen = uRentalcolumnal.size

                    if (rentalCursor != null)
                    {
                        if (rentalCursor.isFirst)
                        {
                            do
                            {

                                iRentalType = rentalCursor
                                        .getString(iStuffrentaltypeColumn)
                                iRental.add(iRentalType)

                                iRentalMisc = rentalCursor
                                        .getString(iStuffrentalmiscColumn)
                                iRental.add(iRentalMisc)

                                iRentalRaterange = rentalCursor
                                        .getString(iStuffrentalrateColumn)
                                iRental.add(iRentalRaterange)

                                iRentalstatus = rentalCursor
                                        .getString(iStuffstatusColumn)
                                iRental.add(iRentalstatus)

                                iRentalArea = rentalCursor
                                        .getString(iStuffrentalareaColumn)
                                iRental.add(iRentalArea)

                                iRentalCity = rentalCursor
                                        .getString(iStuffrentalcityColumn)
                                iRental.add(iRentalCity)

                                iRentalCountry = rentalCursor
                                        .getString(iStuffrentalcountryColumn)
                                iRental.add(iRentalCountry)

                                iRentallatitude = rentalCursor
                                        .getString(iStufflatitudeColumn)
                                iRental.add(iRentallatitude)

                                iRentallongitude = rentalCursor
                                        .getString(iStufflongitudeColumn)
                                iRental.add(iRentallongitude)

                                uRentalType = rentalCursor
                                        .getString(uStuffrentaltypeColumn)
                                uRental.add(uRentalType)

                                uRentalMisc = rentalCursor
                                        .getString(uStuffrentalmiscColumn)
                                uRental.add(uRentalMisc)

                                uRentalRaterange = rentalCursor
                                        .getString(uStuffrentalrateColumn)
                                uRental.add(uRentalRaterange)

                                uRentalstatus = rentalCursor
                                        .getString(uStuffstatusColumn)
                                uRental.add(uRentalstatus)

                                uRentalArea = rentalCursor
                                        .getString(uStuffrentalareaColumn)
                                uRental.add(uRentalArea)

                                uRentalCity = rentalCursor
                                        .getString(uStuffrentalcityColumn)
                                uRental.add(uRentalCity)

                                uRentalCountry = rentalCursor
                                        .getString(uStuffrentalcountryColumn)
                                uRental.add(uRentalCountry)

                                uRentallatitude = rentalCursor
                                        .getString(uStufflatitudeColumn)
                                uRental.add(uRentallatitude)

                                uRentallongitude = rentalCursor
                                        .getString(uStufflongitudeColumn)
                                uRental.add(uRentallongitude)

                                js.object()
                                js.key("iStuff").object()
                                for (y in 0 until irentalcolumnalLen)
                                {
                                    val irentalcolumnalString = iRentalcolumnal.get(y)
                                    // logger.info("irentalcolumnalString" + irentalcolumnalString + " = " + irental.get(y));
                                    js.key(irentalcolumnalString)
                                            .value(iRental.get(y))
                                }
                                js.endObject()
                                js.key("uStuff").object()
                                for (i in 0 until urentalcolumnalLen)
                                {
                                    val urentalcolumnalString = uRentalcolumnal.get(i)
                                    //logger.info("urentalcolumnalString" + urentalcolumnalString + " = " + urental.get(i));
                                    js.key(urentalcolumnalString)
                                            .value(uRental.get(i))
                                }
                                js.endObject()
                                js.endObject()
                                iRental.clear()
                                uRental.clear()

                            }
                            while (rentalCursor.moveToNext())
                        }
                        myDB!!.execSQL(
                                "update category set querystatus='false' where categoryname='Rental'")
                        rentalCursor.close()
                    }

                }
                catch (e: JSONException)
                {
                    logger.info("Error = " + e.message)
                }
                rentalcolumnal.clear()
                iRentalcolumnal.clear()
                uRentalcolumnal.clear()
            }
        }
        catch (e: Exception)
        {

        }
    }

    fun rentalResponse()
    {
        try
        {
            val mStuffJsonArray1 = JSONArray(values)
            val mStuffJsonArraylength1 = mStuffJsonArray1.length()
            for (l in 0 until mStuffJsonArraylength1)
            {
                val mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l)
                val mStuffinnerJsonArray1 = mStuffinnerJson1.names()

                val mStuffinnerJsonArraylength1 = mStuffinnerJsonArray1.length()
                for (k in 0 until mStuffinnerJsonArraylength1)
                {
                    val keys1 = mStuffinnerJsonArray1.getString(k)
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    val values1 = mStuffinnerJson1.getString(keys1)
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1)
                    logger.info(mStuff0i1.get(k))
                }
                myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffrental" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR, mStuffRentalType VARCHAR,mStuffMisc VARCHAR,mStuffRate VARCHAR,mStuffStatus VARCHAR,mStuffCountry VARCHAR,mStuffCity VARCHAR,mStuffArea VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );")

                myDB!!.execSQL(
                        "INSERT INTO MStuffrental(mStuffId , mCatagory , mStuffRentalType ,mStuffMisc ,mStuffRate ,mStuffStatus ,mStuffCountry ,mStuffCity ,mStuffArea ,mStuffLatitude ,mStuffLongitude ) VALUES ('" +
                                mStuff0i1.get(8) + "','" + "Rental" + "','" +
                                mStuff0i1.get(5) + "','" + mStuff0i1.get(2) +
                                "','" + mStuff0i1.get(6) + "','" +
                                mStuff0i1.get(7) + "','" + mStuff0i1.get(9) +
                                "','" + mStuff0i1.get(0) + "','" +
                                mStuff0i1.get(4) + "','" + mStuff0i1.get(1) +
                                "','" + mStuff0i1.get(3) + "');")
                mStuff0i1.clear()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
    }

    fun restaurants()
    {
        try
        {
            val rental_Cursor = myDB!!.query(mY_Restaurants_TABLE, null,
                    null, null, null, null, null)
            if (rental_Cursor.getCount() > 0)
            {
                try
                {
                    val restaurantsCursor = myDB!!.query(
                            mY_Restaurants_TABLE, null, "queryStatus='true'",
                            null, null, null, null)

                    val iStuffCuisinetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffCuisinetype")
                    val iStuffCookingMethodColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffCookingMethod")
                    val iStuffDieteticColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("iStuffDietetic")
                    val iStuffCoursetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffCourseType")
                    val iStuffDishtypeColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("iStuffDishType")
                    val iStuffMainIngredientColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffMainIngredient")
                    val iStuffOccasionOrSeasonColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffOccasionOrSeason")
                    val iStuffMiscellaneousColumn = restaurantsCursor
                            .getColumnIndexOrThrow("iStuffMiscellaneous")
                    val iStufflatitudeColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("ilatitude")
                    val iStufflongitudeColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("ilongitude")
                    val iRestaurantsAreaColumn = restaurantsCursor.getColumnIndexOrThrow("iarea")
                    val iRestaurantsCityColumn = restaurantsCursor.getColumnIndexOrThrow("icity")
                    val iRestaurantsCountryColumn = restaurantsCursor.getColumnIndexOrThrow("icountry")

                    val uStuffCuisinetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffCuisinetype")
                    val uStuffCookingMethodColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffCookingMethod")
                    val uStuffDieteticColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("uStuffDietetic")
                    val uStuffCoursetypeColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffCourseType")
                    val uStuffDishtypeColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("uStuffDishType")
                    val uStuffMainIngredientColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffMainIngredient")
                    val uStuffOccasionOrSeasonColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffOccasionOrSeason")
                    val uStuffMiscellaneousColumn = restaurantsCursor
                            .getColumnIndexOrThrow("uStuffMiscellaneous")
                    val uStufflatitudeColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("ulatitude")
                    val uStufflongitudeColumn = restaurantsCursor
                                    .getColumnIndexOrThrow("ulongitude")
                    val uRestaurantsAreaColumn = restaurantsCursor.getColumnIndexOrThrow("uarea")
                    val uRestaurantsCityColumn = restaurantsCursor.getColumnIndexOrThrow("ucity")
                    val uRestaurantsCountryColumn = restaurantsCursor.getColumnIndexOrThrow("ucountry")

                    // Getting restaurants Column
                    var restaurantscolumn = restaurantsCursor.getColumnNames()
                    var irestaurantscolumn: Array<String>? = null
                    var urestaurantscolumn: Array<String>? = null
                    for (j in 1 until restaurantscolumn.size - 2)
                    {
                        restaurantscolumnal.add(restaurantscolumn[j])
                        logger.info(
                                "restaurantscolumn:" + restaurantscolumn[j])
                                // Log.i(".....................", carscolumn[j]);
                    }
                    if (restaurantscolumnal.size > 1)
                    {
                        irestaurantscolumn = extract1(restaurantscolumn, 0,
                                restaurantscolumn.size / 2)
                        urestaurantscolumn = extract1(restaurantscolumn,
                                restaurantscolumn.size / 2,
                                restaurantscolumn.size)
                    }
                    for (j in 1 until irestaurantscolumn!!.size)
                    {
                        iRestaurantscolumnal.add(irestaurantscolumn!![j])
                        logger.info(
                                "irestaurantscolumn:" + irestaurantscolumn!![j])
                    }

                    for (j in 0 until urestaurantscolumn!!.size - 2)
                    {
                        uRestaurantscolumnal.add(urestaurantscolumn!![j])
                        logger.info(
                                "urestaurantscolumn:" + urestaurantscolumn!![j])
                    }
                    // Length
                    val irestaurantscolumnalLen = iRestaurantscolumnal.size
                    val urestaurantscolumnalLen = uRestaurantscolumnal.size

                    if (restaurantsCursor != null)
                    {
                        if (restaurantsCursor.isFirst)
                        {
                            do
                            {
                                iCuisineType = restaurantsCursor
                                        .getString(iStuffCuisinetypeColumn)
                                iRestaurants.add(iCuisineType)

                                iCookingMethod = restaurantsCursor
                                        .getString(iStuffCookingMethodColumn)
                                iRestaurants.add(iCookingMethod)

                                iDietetic = restaurantsCursor
                                        .getString(iStuffDieteticColumn)
                                iRestaurants.add(iDietetic)

                                iCourseType = restaurantsCursor
                                        .getString(iStuffCoursetypeColumn)
                                iRestaurants.add(iCourseType)

                                iDishType = restaurantsCursor
                                        .getString(iStuffDishtypeColumn)
                                iRestaurants.add(iDishType)

                                iMainIngredient = restaurantsCursor
                                        .getString(iStuffMainIngredientColumn)
                                iRestaurants.add(iMainIngredient)

                                iOccasionOrSeason = restaurantsCursor.getString(
                                        iStuffOccasionOrSeasonColumn)
                                iRestaurants.add(iOccasionOrSeason)

                                iMiscellaneous = restaurantsCursor
                                        .getString(iStuffMiscellaneousColumn)
                                iRestaurants.add(iMiscellaneous)

                                iRestaurantsArea = restaurantsCursor
                                        .getString(iRestaurantsAreaColumn)
                                iRestaurants.add(iRestaurantsArea)

                                iRestaurantsCity = restaurantsCursor
                                        .getString(iRestaurantsCityColumn)
                                iRestaurants.add(iRestaurantsCity)

                                iRestaurantsCountry = restaurantsCursor
                                        .getString(iRestaurantsCountryColumn)
                                iRestaurants.add(iRestaurantsCountry)

                                iRestaurantsLatitude = restaurantsCursor
                                        .getString(iStufflatitudeColumn)
                                iRestaurants.add(iRestaurantsLatitude)

                                iRestaurantsLongitude = restaurantsCursor
                                        .getString(iStufflongitudeColumn)
                                iRestaurants.add(iRestaurantsLongitude)

                                uCuisineType = restaurantsCursor
                                        .getString(uStuffCuisinetypeColumn)
                                uRestaurants.add(uCuisineType)

                                uCookingMethod = restaurantsCursor
                                        .getString(uStuffCookingMethodColumn)
                                uRestaurants.add(uCookingMethod)

                                uDietetic = restaurantsCursor
                                        .getString(uStuffDieteticColumn)
                                uRestaurants.add(uDietetic)

                                uCourseType = restaurantsCursor
                                        .getString(uStuffCoursetypeColumn)
                                uRestaurants.add(uCourseType)

                                uDishType = restaurantsCursor
                                        .getString(uStuffDishtypeColumn)
                                uRestaurants.add(uDishType)

                                uMainIngredient = restaurantsCursor
                                        .getString(uStuffMainIngredientColumn)
                                uRestaurants.add(uMainIngredient)

                                uOccasionOrSeason = restaurantsCursor.getString(
                                        uStuffOccasionOrSeasonColumn)
                                uRestaurants.add(uOccasionOrSeason)

                                uMiscellaneous = restaurantsCursor
                                        .getString(uStuffMiscellaneousColumn)
                                uRestaurants.add(uMiscellaneous)

                                uRestaurantsArea = restaurantsCursor
                                        .getString(uRestaurantsAreaColumn)
                                uRestaurants.add(uRestaurantsArea)

                                uRestaurantsCity = restaurantsCursor
                                        .getString(uRestaurantsCityColumn)
                                uRestaurants.add(uRestaurantsCity)

                                uRestaurantsCountry = restaurantsCursor
                                        .getString(uRestaurantsCountryColumn)
                                uRestaurants.add(uRestaurantsCountry)

                                uRestaurantsLatitude = restaurantsCursor
                                        .getString(uStufflatitudeColumn)
                                uRestaurants.add(uRestaurantsLatitude)

                                uRestaurantsLongitude = restaurantsCursor
                                        .getString(uStufflongitudeColumn)
                                uRestaurants.add(uRestaurantsLongitude)

                                js.object()
                                js.key("iStuff").object()
                                for (y in 0 until irestaurantscolumnalLen)
                                {
                                    val irestautantscolumnalString = iRestaurantscolumnal.get(y)
                                    logger.info("irestautantscolumnalString" +
                                            irestautantscolumnalString + " = " +
                                            iRestaurants.get(y))
                                    js.key(irestautantscolumnalString)
                                            .value(iRestaurants.get(y))
                                }
                                js.endObject()
                                js.key("uStuff").object()
                                for (i in 0 until urestaurantscolumnalLen)
                                {
                                    val urestaurantscolumnalString = uRestaurantscolumnal.get(i)
                                    logger.info("urestaurantscolumnalString" +
                                            urestaurantscolumnalString + " = " +
                                            uRestaurants.get(i))
                                    js.key(urestaurantscolumnalString)
                                            .value(uRestaurants.get(i))
                                }
                                js.endObject()
                                js.endObject()
                                iRestaurants.clear()
                                uRestaurants.clear()

                            }
                            while (restaurantsCursor.moveToNext())
                        }
                        myDB!!.execSQL(
                                "update category set querystatus='false' where categoryname='Restaurants'")
                        restaurantsCursor.close()
                    }
                }
                catch (e: JSONException)
                {
                    logger.info("Error = " + e.message)
                }
                restaurantscolumnal.clear()
                iRestaurantscolumnal.clear()
                uRestaurantscolumnal.clear()
            }
        }
        catch (e: Exception)
        {

        }
    }

    fun restaurantsResponse()
    {
        try
        {
            val mStuffJsonArray1 = JSONArray(values)
            val mStuffJsonArraylength1 = mStuffJsonArray1.length()
            for (l in 0 until mStuffJsonArraylength1)
            {
                val mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l)
                val mStuffinnerJsonArray1 = mStuffinnerJson1.names()

                val mStuffinnerJsonArraylength1 = mStuffinnerJsonArray1.length()
                for (k in 0 until mStuffinnerJsonArraylength1)
                {
                    val keys1 = mStuffinnerJsonArray1.getString(k)
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    val values1 = mStuffinnerJson1.getString(keys1)
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1)
                    logger.info(mStuff0i1.get(k))
                }
                myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " +
                        "MStuffRestaurants" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR,mStuffCuisineType VARCHAR,mStuffCookingMethod VARCHAR,mStuffDietetic VARCHAR,mStuffCourseType VARCHAR,mStuffDishType VARCHAR,mStuffMainIngredient VARCHAR,mStuffOccasionOrSeason VARCHAR,mStuffMiscellaneous VARCHAR,mStuffArea VARCHAR,mStuffCity VARCHAR,mStuffCountry VARCHAR,mStuffLatitude VARCHAR,mStuffLongitude VARCHAR );")
                myDB!!.execSQL(
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
                                "','" + mStuff0i1.get(2) + "');")

                mStuff0i1.clear()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
    }

    fun movies()
    {
        val moviesCursor = myDB!!.query(mY_Movies_TABLE, null,
                "queryStatus='true'", null, null, null, null)
        val iMovieTypeColumn = moviesCursor.getColumnIndexOrThrow("imovietype")
        val iMovieLanguageColumn = moviesCursor.getColumnIndexOrThrow("imovielanguage")
        val iSeatingStyleColumn = moviesCursor.getColumnIndexOrThrow("iseatingstyle")
        val iMovieAreaColumn = moviesCursor.getColumnIndexOrThrow("iarea")
        val iMovieCityColumn = moviesCursor.getColumnIndexOrThrow("icity")
        val iMovieCountryColumn = moviesCursor.getColumnIndexOrThrow("icountry")
        val iMovieLatitudeColumn = moviesCursor.getColumnIndexOrThrow("ilatitude")
        val iMovieLongitudeColumn = moviesCursor.getColumnIndexOrThrow("ilongitude")

        val uMovieTypeColumn = moviesCursor.getColumnIndexOrThrow("umovietype")
        val uMovieLanguageColumn = moviesCursor.getColumnIndexOrThrow("umovielanguage")
        val uSeatingStyleColumn = moviesCursor.getColumnIndexOrThrow("useatingstyle")
        val uMovieAreaColumn = moviesCursor.getColumnIndexOrThrow("uarea")
        val uMovieCityColumn = moviesCursor.getColumnIndexOrThrow("ucity")
        val uMovieCountryColumn = moviesCursor.getColumnIndexOrThrow("ucountry")
        val uMovieLatitudeColumn = moviesCursor.getColumnIndexOrThrow("ulatitude")
        val uMovieLongitudeColumn = moviesCursor.getColumnIndexOrThrow("ulongitude")
        // Getting movies Column
        var moviescolumn = moviesCursor.getColumnNames()
        var imoviescolumn: Array<String>? = null
        var umoviescolumn: Array<String>? = null
        for (j in 1 until moviescolumn.size - 2)
        {
            moviescolumnal.add(moviescolumn[j])
            logger.info("moviescolumn:" + moviescolumn[j])
        }
        if (moviescolumnal.size > 1)
        {
            imoviescolumn = extract1(moviescolumn, 0, moviescolumn.size / 2)
            umoviescolumn = extract1(moviescolumn, moviescolumn.size / 2,
                    moviescolumn.size)
        }
        for (j in 1 until imoviescolumn!!.size)
        {
            iMoviescolumnal.add(imoviescolumn!![j])
            logger.info("imoviescolumn:" + imoviescolumn!![j])
        }

        for (j in 0 until umoviescolumn!!.size - 2)
        {
            uMoviescolumnal.add(umoviescolumn!![j])
            logger.info("umoviescolumn:" + umoviescolumn!![j])
        }
        // Length
        val imoviescolumnalLen = iMoviescolumnal.size
        val umoviescolumnalLen = uMoviescolumnal.size
        try
        {
            if (moviesCursor != null)
            {
                if (moviesCursor.isFirst)
                {
                    do
                    {
                        iMovieType = moviesCursor.getString(iMovieTypeColumn)
                        iMovies.add(iMovieType)

                        iMovieLanguage =
                                moviesCursor.getString(iMovieLanguageColumn)
                        iMovies.add(iMovieLanguage)

                        iSeatingStyle =
                                moviesCursor.getString(iSeatingStyleColumn)
                        iMovies.add(iSeatingStyle)

                        iMovieArea = moviesCursor.getString(iMovieAreaColumn)
                        iMovies.add(iMovieArea)

                        iMovieCity = moviesCursor.getString(iMovieCityColumn)
                        iMovies.add(iMovieCity)

                        iMovieCountry =
                                moviesCursor.getString(iMovieCountryColumn)
                        iMovies.add(iMovieCountry)

                        iMovieLatitude =
                                moviesCursor.getString(iMovieLatitudeColumn)
                        iMovies.add(iMovieLatitude)

                        iMovieLongitude =
                                moviesCursor.getString(iMovieLongitudeColumn)
                        iMovies.add(iMovieLongitude)

                        uMovieType = moviesCursor.getString(uMovieTypeColumn)
                        uMovies.add(uMovieType)

                        uMovieLanguage =
                                moviesCursor.getString(uMovieLanguageColumn)
                        uMovies.add(uMovieLanguage)

                        uSeatingStyle =
                                moviesCursor.getString(uSeatingStyleColumn)
                        uMovies.add(uSeatingStyle)

                        uMovieArea = moviesCursor.getString(uMovieAreaColumn)
                        uMovies.add(uMovieArea)

                        uMovieCity = moviesCursor.getString(uMovieCityColumn)
                        uMovies.add(uMovieCity)

                        uMovieCountry =
                                moviesCursor.getString(uMovieCountryColumn)
                        uMovies.add(uMovieCountry)

                        uMovieLatitude =
                                moviesCursor.getString(uMovieLatitudeColumn)
                        uMovies.add(uMovieLatitude)

                        uMovieLongitude =
                                moviesCursor.getString(uMovieLongitudeColumn)
                        uMovies.add(uMovieLongitude)

                        js.object()
                        js.key("iStuff").object()
                        for (y in 0 until imoviescolumnalLen)
                        {
                            val imoviescolumnalString = iMoviescolumnal.get(y)
                            logger.info("imvoiescolumnalString" +
                                    imoviescolumnalString + " = " +
                                    iMovies.get(y))
                            js.key(imoviescolumnalString).value(iMovies.get(y))
                        }
                        js.endObject()
                        js.key("uStuff").object()
                        for (i in 0 until umoviescolumnalLen)
                        {
                            val umvoiescolumnalString = uMoviescolumnal.get(i)
                            logger.info("umvoiescolumnalString" +
                                    umvoiescolumnalString + " = " +
                                    uMovies.get(i))
                            js.key(umvoiescolumnalString).value(uMovies.get(i))
                        }
                        js.endObject()
                        js.endObject()
                        iMovies.clear()
                        uMovies.clear()
                    }
                    while (moviesCursor.moveToNext())
                }
                myDB!!.execSQL(
                        "update category set querystatus='false' where categoryname='Movies'")
                moviesCursor.close()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
        moviescolumnal.clear()
        iMoviescolumnal.clear()
        uMoviescolumnal.clear()
    }

    fun moviesResponse()
    {
        try
        {
            val mStuffJsonArray1 = JSONArray(values)
            val mStuffJsonArraylength1 = mStuffJsonArray1.length()
            for (l in 0 until mStuffJsonArraylength1)
            {
                val mStuffinnerJson1 = mStuffJsonArray1.getJSONObject(l)
                val mStuffinnerJsonArray1 = mStuffinnerJson1.names()

                val mStuffinnerJsonArraylength1 = mStuffinnerJsonArray1.length()
                for (k in 0 until mStuffinnerJsonArraylength1)
                {
                    val keys1 = mStuffinnerJsonArray1.getString(k)
                    // logger.info("mstuff arry key1:" + k + ":"+ keys1);
                    val values1 = mStuffinnerJson1.getString(keys1)
                    // logger.info("mstuff arry value1:" + k + ":" + values1);
                    mStuff0i1.add(values1)
                    logger.info(mStuff0i1.get(k))
                }
                myDB!!.execSQL("CREATE TABLE IF NOT EXISTS " + "MStuffmovies" +
                        " (mStuffId VARCHAR, mCatagory VARCHAR, mStuffMovieType VARCHAR,mStuffMovieLanguage VARCHAR,mStuffSeatingStyle VARCHAR,mStuffMovieArea VARCHAR,mStuffMovieCity VARCHAR,mStuffMovieCountry VARCHAR,mStuffMovieLatitude VARCHAR,mStuffMovieLongitude VARCHAR );")
                myDB!!.execSQL(
                        "INSERT INTO MStuffmovies(mStuffId , mCatagory , mStuffMovieType ,mStuffMovieLanguage ,mStuffSeatingStyle ,mStuffMovieArea ,mStuffMovieCity ,mStuffMovieCountry ,mStuffMovieLatitude ,mStuffMovieLongitude) VALUES ('" +
                                mStuff0i1.get(5) + "','" + "Movies" + "','" +
                                mStuff0i1.get(0) + "','" + mStuff0i1.get(8) +
                                "','" + mStuff0i1.get(7) + "','" +
                                mStuff0i1.get(4) + "','" + mStuff0i1.get(1) +
                                "','" + mStuff0i1.get(6) + "','" +
                                mStuff0i1.get(2) + "','" + mStuff0i1.get(3) +
                                "');")
                mStuff0i1.clear()
            }
        }
        catch (e: JSONException)
        {
            logger.info("Error = " + e.message)
        }
    }

    companion object {
        private val logger = Logger.getLogger("MStuffQuery")
        private val mY_MobeegalUser_TABLE = "MobeegalUser"
        private val mY_Dating_TABLE = "Dating"
        private val mY_Matrimony_TABLE = "Matrimony"
        private val mY_Cars_TABLE = "Cars"
        private val mY_Jewelry_TABLE = "Jewelry"
        private val mY_home_TABLE = "Home"
        private val mY_Restaurants_TABLE = "Restaurants"
        private val mY_Movies_TABLE = "Movies"

        @JvmStatic
        fun extract1(elts: Array<String>, start: Int, last: Int): Array<String> {
            val ret = Array(last - start) { "" }
            for (i in ret.indices) {
                ret[i] = elts[start + i]
            }
            return ret
        }
    }
}
