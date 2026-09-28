package com.mobeegal.android.activity

/*
<!--
$Id:: MstuffSearchResults.java 14 2008-08-19 06:36:45Z muthu.ramadoss        $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import com.google.android.maps.GeoPoint
import com.google.android.maps.MapActivity
import com.google.android.maps.MapController
import com.google.android.maps.MapView
import com.google.android.maps.MyLocationOverlay
import com.mobeegal.android.R
import com.mobeegal.android.model.MstuffLocations
import com.mobeegal.android.util.ViewMenu

/**
 * @author gunasekaran
 */
class MstuffSearchResults : MapActivity() {

    private var selectedcatalogs: String? = null
    private var searchItemString: String? = null
    private var mapview: MapView? = null
    private var p1: GeoPoint? = null
    private var bubbleIcon: Bitmap? = null
    private var bubbleIcon1: Bitmap? = null
    private var shadowIcon: Bitmap? = null
    private var datingIcon: Bitmap? = null
    private var matrimonyIcon: Bitmap? = null
    private var carsIcon: Bitmap? = null
    private var rentalIcon: Bitmap? = null
    private var jewelryIcon: Bitmap? = null
    private var moviesIcon: Bitmap? = null
    private var restaurantIcon: Bitmap? = null
    private var select_Icon: Bitmap? = null
    private var selectedcatagory: String? = null
    private var selectIcon: Bitmap? = null
    private var servicename1: String? = null
    private var res: String? = null
    val results: ArrayList<CharSequence> = ArrayList()
    private var viewlist: ListView? = null
    private var locationlist: List<String>? = null
    private var peoples: MutableList<MstuffLocations>? = null
    private var location: String? = null
    private var mc: MapController? = null
    private var selectedMapLocation: MstuffLocations? = null
    private var myDB: SQLiteDatabase? = null
    private var selectedlocation: String? = null
    private var selectedlatitude: Int = 0
    private var selectedlongitude: Int = 0
    private var initialLatitude: Int = 0
    private var initialLongitude: Int = 0
    private var initialZoomLevel: Int = 0
    private var selectedid: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.mstuffsearchresults)
        mapview = findViewById(R.id.mapSearch) as MapView
        bubbleIcon = BitmapFactory
            .decodeResource(resources, R.drawable.bubble)
        bubbleIcon1 = BitmapFactory
            .decodeResource(resources, R.drawable.bubble1)
        datingIcon = BitmapFactory
            .decodeResource(resources, R.drawable.dating_icon)
        matrimonyIcon = BitmapFactory
            .decodeResource(resources, R.drawable.matrimony_icon)
        rentalIcon = BitmapFactory
            .decodeResource(resources, R.drawable.rental_icon)
        moviesIcon = BitmapFactory
            .decodeResource(resources, R.drawable.movies_icon)
        jewelryIcon = BitmapFactory
            .decodeResource(resources, R.drawable.jewelry_icon)
        restaurantIcon = BitmapFactory
            .decodeResource(resources, R.drawable.restaurant_icon)
        carsIcon = BitmapFactory
            .decodeResource(resources, R.drawable.cars_icon)
        select_Icon = BitmapFactory
            .decodeResource(resources, R.drawable.select_icon)
        shadowIcon = BitmapFactory
            .decodeResource(resources, R.drawable.shadow)
        mc = mapview!!.controller

        val mylocation = MyLocationOverlay(this, mapview)
//            OverlayController oc = mapview.createOverlayController();

        myDB = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        val cols = arrayOf("latitude", "longitude", "zoomlevel")
        val c = myDB!!.query(
            "selectedlocation", cols, null, null,
            null, null, null
        )
        val latitudeColumn = c.getColumnIndexOrThrow("latitude")
        val longitudeColumn = c.getColumnIndexOrThrow("longitude")
        val zoomlevelColumn = c.getColumnIndexOrThrow("zoomlevel")
        if (c != null) {
            if (c.isFirst) {
                do {
                    initialLatitude = c.getInt(latitudeColumn)
                    initialLongitude = c.getInt(longitudeColumn)
                    initialZoomLevel = c.getInt(zoomlevelColumn)
                } while (c.moveToNext())
            }
        }
        p1 = GeoPoint(initialLatitude, initialLongitude)
//            oc.add(mylocation, true);
        mc!!.animateTo(p1)
        mc!!.setZoom(initialZoomLevel)
        val b = this.intent.extras
        if (b != null) {
            searchItemString = b.getString("edittext")
            selectedcatalogs = b.getString("spinner")
        }

        val zoomIn = findViewById(R.id.zoomin1) as ImageButton
        zoomIn.setOnClickListener {
            val level = mapview!!.zoomLevel
            mapview!!.controller.setZoom(level + 1)
            try {
                myDB = openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                myDB!!.execSQL(
                    "UPDATE selectedlocation set zoomlevel=" +
                        (level + 1) + ";"
                )
            } catch (exce: Exception) {
            }
        }

        val zoomOut = findViewById(R.id.zoomout1) as ImageButton
        zoomOut.setOnClickListener {
            val level = mapview!!.zoomLevel
            mapview!!.controller.setZoom(level - 1)
            try {
                myDB = openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                myDB!!.execSQL(
                    "UPDATE selectedlocation set zoomlevel=" +
                        (level - 1) + ";"
                )
            } catch (exce: Exception) {
            }
        }
    }

    override fun isRouteDisplayed(): Boolean {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    fun getMapLocations1(): List<MstuffLocations>? {
        if (peoples == null) {
            peoples = ArrayList()
            myDB = null

            try {
                myDB = this.openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                val cols = arrayOf(
                    "mstuffid", "catagory", "details", "latitude",
                    "longitude", "location"
                )
                val c = myDB!!.query(
                    "mStuffdetails", cols, null, null,
                    null, null, null
                )
                val detailsColumn = c.getColumnIndexOrThrow("details")
                val catagoryColumn = c.getColumnIndexOrThrow("catagory")
                val latitudeColumn = c.getColumnIndexOrThrow("latitude")
                val longitudeColumn = c.getColumnIndexOrThrow("longitude")
                val locationColumn = c.getColumnIndexOrThrow("location")
                val useridColumn = c.getColumnIndexOrThrow("mstuffid")
                var details: String? = null
                var i = 0
                var j = 0
                if (c != null) {
                    if (c.isFirst) {
                        do {
                            j++
                            val userid = c.getString(useridColumn)
                            details = c.getString(detailsColumn)
                            val catagory = c.getString(catagoryColumn)
                            val dblatitude = c.getInt(latitudeColumn)
                            val dblongitude = c.getInt(longitudeColumn)
                            val location1 = c.getString(locationColumn)
                            if (details!!.toLowerCase()
                                    .contains(searchItemString!!.toLowerCase()) &&
                                catagory.equals(selectedcatalogs, ignoreCase = true)
                            ) {
                                peoples!!.add(
                                    MstuffLocations(
                                        userid,
                                        catagory, details, dblatitude,
                                        dblongitude, location1
                                    )
                                )
                                i++
                            }
                        } while (c.moveToNext())
                        Toast.makeText(
                            this@MstuffSearchResults,
                            "$i Matches found out of $j mStuffs ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
            } finally {
                if (myDB != null) {
                    myDB!!.close()
                }
            }
        }
        return peoples
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsSearchMenu(menu)
        return true
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent = Intent(
                    this@MstuffSearchResults,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@MstuffSearchResults,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@MstuffSearchResults, Settings::class.java)
                startActivity(settings)
            }
            4 -> {
                val mStuffSearchIntent = Intent(
                    this@MstuffSearchResults,
                    MstuffSearch::class.java
                )
                startActivity(mStuffSearchIntent)
            }
            5 -> {
                val intent =
                    Intent(this@MstuffSearchResults, Chat::class.java)
                val b = Bundle()
                b.putString("mstuffid", selectedid)
                intent.putExtras(b)
                startActivityForResult(intent, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
