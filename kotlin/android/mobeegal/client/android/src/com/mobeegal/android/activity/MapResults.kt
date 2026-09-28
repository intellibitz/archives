package com.mobeegal.android.activity

/*
<!--
$Id:: MapResults.java 14 2008-08-19 06:36:45Z muthu.ramadoss                 $: Id of last commit
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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.AbsoluteLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.maps.GeoPoint
import com.google.android.maps.MapActivity
import com.google.android.maps.MapController
import com.google.android.maps.MapView
import com.google.android.maps.MyLocationOverlay
import com.google.android.maps.Overlay
import com.mobeegal.android.MobeegalApplication
import com.mobeegal.android.R
import com.mobeegal.android.model.MstuffLocations
import com.mobeegal.android.util.ViewMenu

class MapResults : MapActivity() {

    private var hitoverlay: LinearLayout? = null
    private var mhittext: TextView? = null
    private var hintoverlay: LinearLayout? = null
    private var bubbleIcon: Bitmap? = null
    private var userDatingIcon: Bitmap? = null
    private var userMatrimonyIcon: Bitmap? = null
    private var userJewelryIcon: Bitmap? = null
    private var userRentalIcon: Bitmap? = null
    private var userCarsIcon: Bitmap? = null
    private var userRestaurantIcon: Bitmap? = null
    private var userMoviesIcon: Bitmap? = null
    private var datingIcon: Bitmap? = null
    private var matrimonyIcon: Bitmap? = null
    private var carsIcon: Bitmap? = null
    private var rentalIcon: Bitmap? = null
    private var jewelryIcon: Bitmap? = null
    private var moviesIcon: Bitmap? = null
    private var restaurantIcon: Bitmap? = null
    private var shadowIcon: Bitmap? = null
    private var markerIcon: Bitmap? = null
    val results: ArrayList<CharSequence> = ArrayList()
    private var peoples: MutableList<MstuffLocations>? = null
    private var mc: MapController? = null
    private var selectedMapLocation: MstuffLocations? = null
    private var myDB: SQLiteDatabase? = null
    private var selectedid: String? = null
    private var selectedcatagory: String? = null
    private var selectedlatitude: Int = 0
    private var selectedlongitude: Int = 0
    private var initialLatitude: Int = 0
    private var initialLongitude: Int = 0
    private var initialZoomLevel: Int = 0
    var j = 2
    private var layoutoverlay: LinearLayout? = null
    private var iconslayoutoverlay: AbsoluteLayout? = null
    private var mstufftext: TextView? = null
    private var mhinttext: TextView? = null
    var mapView: MapView? = null
    var mLocationListener: InitLocationListener? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.map)

// todo: fix me, get an apikey from google maps site
        mapView = MapView(this, "apisamples")
        mapView!!.isClickable = true
        mapView!!.isEnabled = true
        mapView!!.displayZoomControls(true)
        val map = findViewById(R.id.layout_map) as LinearLayout
        map.addView(
            mapView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        val zoomView = mapView!!.zoomControls
        val zoom = findViewById(R.id.layout_zoom) as LinearLayout
        zoom.addView(
            zoomView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

/*
        layoutoverlay = (LinearLayout) findViewById(R.id.layout);
        iconslayoutoverlay = (AbsoluteLayout) findViewById(R.id.iconslayout);
        hintoverlay = (LinearLayout) findViewById(R.id.hintlayout);
        hitoverlay = (LinearLayout) findViewById(R.id.hitlayout);
        mstufftext = (TextView) findViewById(R.id.detailstext);
        mhinttext = (TextView) findViewById(R.id.hinttext);
        mhittext = (TextView) findViewById(R.id.hittext);
*/

//        setIcons();

//        setIconListeners();

        requestLocationUpdates()
        showMatches()
    }

    fun setIcons() {
/*
        bubbleIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.bubble);
        BitmapFactory.decodeResource(getResources(), R.drawable.bubble1);
        datingIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.dating_icon);
        matrimonyIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.matrimony_icon);
        rentalIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.rental_icon);
        moviesIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.movies_icon);
        jewelryIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.jewelry_icon);
        restaurantIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.restaurant_icon);
        carsIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.cars_icon);
        BitmapFactory.decodeResource(getResources(), R.drawable.select_icon);
        shadowIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.shadow);
        BitmapFactory.decodeResource(getResources(), R.drawable.about_enabled);
        markerIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.marker);
        userDatingIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.user);
        userMatrimonyIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.user);
        userJewelryIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.user);
        userRentalIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.user);
        userCarsIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.user);
        userMoviesIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.user);
        userRestaurantIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.user);
*/
    }

    fun showMatches() {
        /*
           * GEOCODER FOR LOCATION BASED SEARCH............. THIS CONVERTS
           * LOCATION NAME INTO LATITUDE AND LONGITUDE Button searchlocation =
           * (Button) findViewById(R.id.searchlocation);
           * searchlocation.setOnClickListener(new OnClickListener() { public void
           * onClick(View arg0) { try { location =
           * locationname.getText().toString(); GmmGeocoder geocoder = new
           * GmmGeocoder(Locale.getDefault()); Address[] addresses =
           * geocoder.query(location, GmmGeocoder.QUERY_TYPE_LOCATION, 0, 0, 180,
           * 360); String s1 = addresses[0].toString(); String[] strArray =
           * s1.split(","); for (int i = 0; i < strArray.length; i++) { if
           * (strArray[i].contains("latitude=")) { strArray2 = strArray[i]; } if
           * (strArray[i].contains("longitude=")) { strArray3 = strArray[i]; } }
           * String[] latArray = strArray2.split("="); String[] longArray =
           * strArray3.split("="); latitude = Double.parseDouble(latArray[1]);
           * longitude = Double.parseDouble(longArray[1]); latitude = latitude *
           * 1000000; longitude = longitude * 1000000; int latitude1 = (int)
           * latitude; int longitude1 = (int) longitude; point = new Point((int)
           * (latitude1), (int) (longitude1)); mc.centerMapTo(point, true);
           * locationname.setText(""); } catch (IOException ex) { } catch
           * (NullPointerException enul) { Toast.makeText(MapResults.this,
           * "Place not found, sorry. Try with some other place or give correct
           * spelling..", Toast.LENGTH_LONG).show(); } } });
           */

        mc = mapView!!.controller

/*
        LocationManager myLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Location location = myLocationManager.getCurrentLocation("gps");
        List<LocationProvider> provider = myLocationManager.getProviders();

//        Log.i("MapResults===========>", location.toString());
        Log.i("MapResults===========>", provider.toString());
*/

//        UserLocationOverlay myLocationOverlay = new UserLocationOverlay();
        val myLocationOverlay = MyLocationOverlay(this, mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.runOnFirstFix {
            mapView!!.controller
                .animateTo(myLocationOverlay.myLocation)
        }
        mapView!!.overlays.add(myLocationOverlay)
//        OverlayController oc = mapView.createOverlayController();
/*
        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String cols[] = {"latitude", "longitude", "zoomlevel"};
            Cursor c = myDB.query("selectedlocation", cols, null, null,
                    null, null, null);
            int latitudeColumn = c.getColumnIndexOrThrow("latitude");
            int longitudeColumn = c.getColumnIndexOrThrow("longitude");
            int zoomlevelColumn = c.getColumnIndexOrThrow("zoomlevel");
            if (c.isFirst())
            {
                do
                {
                    initialLatitude = c.getInt(latitudeColumn);
                    initialLongitude = c.getInt(longitudeColumn);
                    initialZoomLevel = c.getInt(zoomlevelColumn);
                }
                while (c.moveToNext());
            }
            GeoPoint p1 = new GeoPoint(initialLatitude, initialLongitude);
//            oc.add(myLocationOverlay, true);
            mc.animateTo(p1);
            mc.setZoom(initialZoomLevel);

        }
        catch (Exception ex)
        {
            Logger.getLogger(MapResults.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
*/
    }

    fun setIconListeners() {
        val favoriteIcon = findViewById(R.id.favorites) as ImageView
        val ignoreIcon = findViewById(R.id.ignore) as ImageView
        val chatIcon = findViewById(R.id.chat) as ImageView
        val mediaIcon = findViewById(R.id.media) as ImageView
        favoriteIcon.setOnClickListener {
            /*
                     * ScaleAnimation scale = new ScaleAnimation(1, 0.7f, 1, 0.7f,
                     * ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                     * ScaleAnimation.RELATIVE_TO_SELF, 0.5f); public class
                     * UserLocationOverlay extends Overlay { private Paint textPaint;
                     * private Paint borderPaint; private Paint innerPaint; private
                     * Paint innerPaint1; private int[] selectedIcons; private int
                     * count = 0; boolean isRemove; scale.setDuration(50);
                     * scale.setFillAfter(true); favoriteIcon.startAnimation(scale);
                     */
            val rootSet = AnimationSet(true)
            rootSet.interpolator = AccelerateInterpolator()
//                rootSet.setRepeatMode(Animation.NO_REPEAT);

            // Create and add first child, a motion animation.

            // rootSet.addAnimation(trans1);

            val scale = ScaleAnimation(
                1f, 0.7f, 1f, 0.7f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            )
            scale.duration = 200
            scale.fillAfter = true
            favoriteIcon.startAnimation(scale)
            Toast.makeText(
                this@MapResults, "Added as favorites",
                Toast.LENGTH_SHORT
            ).show()
        }
        ignoreIcon.setOnClickListener {
            try {
                val scale = ScaleAnimation(
                    1f, 0.7f, 1f, 0.7f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                )
                scale.duration = 200
                scale.fillAfter = true
                ignoreIcon.startAnimation(scale)
                myDB = openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                myDB!!.execSQL(
                    "UPDATE selectedlocation set latitude=" +
                        selectedlatitude + ", longitude=" +
                        selectedlongitude + ";"
                )
                myDB!!.delete(
                    "mStuffdetails", "mstuffid='" + selectedid +
                        "'", null
                )
                val i = Intent(
                    this@MapResults,
                    MapResults::class.java
                )
                startActivityForResult(i, 0)
            } finally {
                if (myDB != null) {
                    myDB!!.close()
                }
            }
        }
        chatIcon.setOnClickListener {
            val scale = ScaleAnimation(
                1f, 0.7f, 1f, 0.7f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            )
            scale.duration = 200
            scale.fillAfter = true
            chatIcon.startAnimation(scale)
            val intent = Intent(this@MapResults, Chat::class.java)
            val b = Bundle()
            b.putString("mstuffid", selectedid)
            intent.putExtras(b)
            startActivityForResult(intent, 0)
        }
        mediaIcon.setOnClickListener {
            val scale = ScaleAnimation(
                1f, 0.7f, 1f, 0.7f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            )
            scale.duration = 200
            scale.fillAfter = true
            mediaIcon.startAnimation(scale)
            val intent = Intent(
                this@MapResults,
                ViewMedia::class.java
            )
            startActivityForResult(intent, 0)
        }
    }

    override fun isRouteDisplayed(): Boolean {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    fun getMapLocations(): List<MstuffLocations>? {
        if (peoples == null) {
            peoples = ArrayList()
            myDB = null
            try {
                myDB = this.openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                val cols = arrayOf(
                    "mstuffid", "catagory", "details",
                    "latitude", "longitude", "location"
                )
                val c = myDB!!.query(
                    "mStuffdetails", cols, null, null,
                    null, null, null
                )
                val useridColumn = c.getColumnIndexOrThrow("mstuffid")
                val catagoryColumn = c.getColumnIndexOrThrow("catagory")
                val detailsColumn = c.getColumnIndexOrThrow("details")
                val latitudeColumn = c.getColumnIndexOrThrow("latitude")
                val longitudeColumn = c.getColumnIndexOrThrow("longitude")
                val locationColumn = c.getColumnIndexOrThrow("location")
                var details: String? = null
                if (c != null) {
                    if (c.isFirst) {
                        do {
                            val userid = c.getString(useridColumn)
                            val catagory = c.getString(catagoryColumn)
                            details = c.getString(detailsColumn)
                            val dblatitude = c.getInt(latitudeColumn)
                            val dblongitude = c.getInt(longitudeColumn)
                            val location1 = c.getString(locationColumn)
                            try {
                                peoples!!.add(
                                    MstuffLocations(
                                        userid,
                                        catagory, details, dblatitude,
                                        dblongitude, location1
                                    )
                                )
                            } catch (e: Exception) {
                            }
                        } while (c.moveToNext())
                    } else {
                        Toast.makeText(
                            this@MapResults,
                            "No matches found. ", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MapResults,
                    " No Matches Found. Activate the service",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                if (myDB != null) {
                    myDB!!.close()
                }
            }
        }
        return peoples
    }

    inner class UserLocationOverlay : Overlay() {

        private var textPaint: Paint? = null
        private var borderPaint: Paint? = null
        private var innerPaint: Paint? = null
        private var innerPaint1: Paint? = null
        private var selectedIcons: IntArray? = null
        private var count = 0
        var isRemove: Boolean = false

        override fun onTap(p: GeoPoint, mapView: MapView): Boolean {
            // Store whether prior popup was displayed so we can call
            // invalidate() & remove it if necessary.
            val isRemovePriorPopup = selectedMapLocation != null
            // boolean isRemove = selectedIcons != null;
            // Next test whether a new popup should be displayed
            selectedMapLocation = getHitMapLocation(p)

            if (isRemovePriorPopup || selectedMapLocation != null) {
                mapView.invalidate()
            }
            // Lastly return true if we handled this onTap()
            return selectedMapLocation != null
        }

        override fun draw(canvas: Canvas, mapview: MapView, shadow: Boolean) {
            drawMapLocations(canvas, shadow)
            drawInfoWindow(canvas, shadow)
            val centre = mapView!!.mapCenter
            try {
                myDB = openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE,
                    null
                )
                myDB!!.execSQL(
                    "UPDATE selectedlocation set latitude=" +
                        centre.latitudeE6 + ", longitude=" +
                        centre.longitudeE6 + ";"
                )
            } catch (exce: Exception) {
            }
        }

        /**
         * Test whether an information balloon should be displayed or a prior
         * balloon hidden.
         */
        private fun getHitMapLocation(tapPoint: GeoPoint?): MstuffLocations? {
            var tapPoint = tapPoint
            // Track which MapLocation was hit...if any
            var hitMapLocation: MstuffLocations? = null
            val hitTestRecr = RectF()
            val screenCoords = IntArray(2)
            val iterator = getMapLocations()!!.iterator()
            while (iterator.hasNext()) {
                val testLocation = iterator.next()

                // Translate the MapLocation's lat/long coordinates to screen
                // coordinates
                val tp1 = testLocation.getPoint()
/*
                GeoPoint p1 = converter.pixelToRgb()
                        (tp1.getLatitudeE6(), tp1.getLongitudeE6());
                screenCoords[0] = p1.getLatitudeE6();
                screenCoords[1] = p1.getLongitudeE6();
*/

                testLocation.getLocation()
                selectedlatitude = testLocation.getLatitude()
                selectedlongitude = testLocation.getLongitude()
                selectedid = testLocation.getUserid()
                selectedcatagory = testLocation.getCatagory()
                // Create a 'hit' testing Rectangle w/size and coordinates of
                // our icon
                // Set the 'hit' testing Rectangle with the size and coordinates
                // of our on screen icon
                hitTestRecr.set(
                    (-bubbleIcon!!.width / 2).toFloat(),
                    (-bubbleIcon!!.height).toFloat(),
                    (bubbleIcon!!.width / 2).toFloat(), 0f
                )
                hitTestRecr.offset(screenCoords[0].toFloat(), screenCoords[1].toFloat())

                // Finally test for a match between our 'hit' Rectangle and the
                // location clicked by the user
//                calculator.getPointXY(tapPoint, screenCoords);
                if (hitTestRecr.contains(screenCoords[0].toFloat(), screenCoords[1].toFloat())) {
                    hitMapLocation = testLocation
                    break
                } else {
                    layoutoverlay!!.visibility = View.GONE
                    iconslayoutoverlay!!.visibility = View.GONE
                    // hintoverlay.setVisibility(View.VISIBLE);
                    hitoverlay!!.visibility = View.VISIBLE
                }
            }
            // Lastly clear the newMouseSelection as it has now been processed
            tapPoint = null

            return hitMapLocation
        }

        private fun drawMapLocations(canvas: Canvas, shadow: Boolean) {
            val iterator = getMapLocations()!!.iterator()
            val screenCoords = IntArray(2)
            while (iterator.hasNext()) {
                val location = iterator.next()
//                calculator.getPointXY(location.getPoint(), screenCoords);
                val select_Category = location.getCatagory()
                if (shadow) {
                    // Only offset the shadow in the y-axis as the shadow is
                    // angled so the base is at x=0;
                    canvas.drawBitmap(
                        shadowIcon!!, screenCoords[0].toFloat(),
                        (screenCoords[1] - shadowIcon!!.height).toFloat(), null
                    )
                } else {
                    // canvas.drawBitmap(matrimonyIcon, screenCoords[0] -
                    // bubbleIcon.getWidth() / 2, screenCoords[1] -
                    // bubbleIcon.getHeight(), null);
                    if (select_Category == "Dating") {
                        canvas.drawBitmap(
                            datingIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "Matrimony") {
                        canvas.drawBitmap(
                            matrimonyIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "Rental") {
                        canvas.drawBitmap(
                            rentalIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "Restaurants") {
                        canvas.drawBitmap(
                            restaurantIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "Jewelry") {
                        canvas.drawBitmap(
                            jewelryIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "Movies") {
                        canvas.drawBitmap(
                            moviesIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "Cars") {
                        canvas.drawBitmap(
                            carsIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "Marker") {
                        canvas.drawBitmap(
                            markerIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "userDating") {
                        canvas.drawBitmap(
                            userDatingIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "userMatrimony") {
                        canvas.drawBitmap(
                            userMatrimonyIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "userJewelry") {
                        canvas.drawBitmap(
                            userJewelryIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "userCars") {
                        canvas.drawBitmap(
                            userCarsIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "userRental") {
                        canvas.drawBitmap(
                            userRentalIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "userRestaurant") {
                        canvas.drawBitmap(
                            userRestaurantIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    } else if (select_Category == "userMovies") {
                        canvas.drawBitmap(
                            userMoviesIcon!!,
                            (screenCoords[0] - bubbleIcon!!.width / 2).toFloat(),
                            (screenCoords[1] - bubbleIcon!!.height).toFloat(), null
                        )
                    }
                }
            }
        }

        private fun drawInfoWindow(canvas: Canvas, shadow: Boolean) {
            if (selectedMapLocation != null) {
                if (shadow) {
                    // Skip painting a shadow in this tutorial
                } else {
                    // Toast.makeText(MapResults.this,select_Category +
                    // selectedcatagory,Toast.LENGTH_SHORT).show();
                    if (selectedcatagory == "Dating"
                        || selectedcatagory == "Matrimony"
                        || selectedcatagory == "Rental"
                        || selectedcatagory == "Restaurants"
                        || selectedcatagory == "Jewelry"
                        || selectedcatagory == "Cars"
                    ) {
                        hintoverlay!!.visibility = View.GONE
                        mhinttext!!.visibility = View.GONE
                        mhittext!!.visibility = View.GONE
                        val drawRect = RectF()
                        drawRect.set(125f, 320f, 315f, 368f)
                        canvas.drawRoundRect(drawRect, 10f, 10f, getInnerPaint())
                        canvas
                            .drawRoundRect(
                                drawRect, 10f, 10f,
                                getBorderPaint()
                            )
                        iconslayoutoverlay!!.visibility = View.VISIBLE
                    } else {
                    }

                    if (selectedcatagory == "userDating"
                        || selectedcatagory == "userMatrimony"
                        || selectedcatagory == "userRental"
                        || selectedcatagory == "userRestaurants"
                        || selectedcatagory == "userJewelry"
                        || selectedcatagory == "userCars"
                    ) {
                        hintoverlay!!.visibility = View.GONE
                        mhinttext!!.visibility = View.GONE
                        mhittext!!.visibility = View.GONE
                        iconslayoutoverlay!!.visibility = View.GONE
                        val layoutoverlay1 = RectF()
                        // layoutoverlay1.set(03,240,120,325);
                        layoutoverlay1.set(2f, 371f, 317f, 428f)
                        canvas.drawRoundRect(
                            layoutoverlay1, 10f, 10f,
                            getInnerPaint()
                        )
                        canvas.drawRoundRect(
                            layoutoverlay1, 10f, 10f,
                            getBorderPaint()
                        )
                        layoutoverlay!!.visibility = View.VISIBLE
                        val selDestinationOffset = IntArray(2)
//                        calculator.getPointXY(selectedMapLocation.getPoint(),
//                                selDestinationOffset);
                        val point = GeoPoint(
                            selectedlatitude,
                            selectedlongitude
                        )
                        mc!!.setCenter(point)
                        val selectedmStuffdetails = selectedMapLocation!!
                            .getName()
                        mstufftext!!.text = selectedmStuffdetails
                    } else {
                        mhinttext!!.visibility = View.GONE
                        mhittext!!.visibility = View.GONE
                        val layoutoverlay1 = RectF()
                        // layoutoverlay1.set(03,240,120,325);
                        layoutoverlay1.set(2f, 371f, 317f, 428f)
                        canvas.drawRoundRect(
                            layoutoverlay1, 10f, 10f,
                            getInnerPaint()
                        )
                        canvas.drawRoundRect(
                            layoutoverlay1, 10f, 10f,
                            getBorderPaint()
                        )
                        layoutoverlay!!.visibility = View.VISIBLE
                        // First determine the screen coordinates of the
                        // selected MapLocation
                        val selDestinationOffset = IntArray(2)
//                        calculator.getPointXY(selectedMapLocation.getPoint(),
//                                selDestinationOffset);
                        val point = GeoPoint(
                            selectedlatitude,
                            selectedlongitude
                        )
                        mc!!.setCenter(point)
                        val selectedmStuffdetails = selectedMapLocation!!
                            .getName()
                        // String detail[] = selectedmStuffdetails.split(",");
                        // String slashdetails = detail[0] + "\n" + detail[1];

                        // mstufftext.setText(selectedmStuffdetails +"\n\n\n"+
                        // getString(R.string.hint));
                        mstufftext!!.text = selectedmStuffdetails

                        // mstuffhint.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                val hintRect = RectF()
                hintRect.set(2f, 395f, 317f, 428f)
                canvas.drawRoundRect(hintRect, 10f, 10f, getInnerPaint())
                canvas.drawRoundRect(hintRect, 10f, 10f, getBorderPaint())
                hintoverlay!!.visibility = View.VISIBLE
                hitoverlay!!.visibility = View.VISIBLE
                mhinttext!!.visibility = View.VISIBLE
                mhittext!!.visibility = View.VISIBLE
                // mhinttext.setText(R.string.hit +"\n"+ R.id.hinttext);
            }
        }

        fun getInnerPaint(): Paint {
            if (innerPaint == null) {
                innerPaint = Paint()
                innerPaint!!.setARGB(255, 75, 75, 75) // gray
                innerPaint!!.isAntiAlias = true
            }
            return innerPaint!!
        }

        fun getInnerPaint1(): Paint {
            if (innerPaint1 == null) {
                innerPaint1 = Paint()
                innerPaint1!!.setARGB(255, 75, 75, 75) // gray
                innerPaint1!!.isAntiAlias = true
            }
            return innerPaint1!!
        }

        fun getBorderPaint(): Paint {
            if (borderPaint == null) {
                borderPaint = Paint()
                borderPaint!!.setARGB(255, 255, 255, 255)
                borderPaint!!.isAntiAlias = true
                borderPaint!!.style = Paint.Style.STROKE
                borderPaint!!.strokeWidth = 2f
            }
            return borderPaint!!
        }

        fun getTextPaint(): Paint {
            if (textPaint == null) {
                textPaint = Paint()
                textPaint!!.setARGB(255, 255, 255, 0)
                textPaint!!.isAntiAlias = true
            }
            return textPaint!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMapMenu(menu)
        return true
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent = Intent(
                    this@MapResults,
                    MapResults::class.java
                )
                startActivityForResult(stuffCheckintent, 0)
            }
            2 -> {
                val intent1 = Intent(
                    this@MapResults,
                    FindandInstall::class.java
                )
                startActivityForResult(intent1, 0)
            }
            3 -> {
                val settings =
                    Intent(this@MapResults, Settings::class.java)
                startActivityForResult(settings, 0)
            }
            4 -> {
                myDB!!.execSQL("update preferences set views='TextView'")
                val mStuffTextView = Intent(
                    this@MapResults,
                    MStuffTextView::class.java
                )
                startActivity(mStuffTextView)
            }
            5 -> {
                val mStuffsearch = Intent(
                    this@MapResults,
                    MstuffSearch::class.java
                )
                startActivity(mStuffsearch)
            }
            6 -> {
                mapView!!.isSatellite = true
            }
            /*
             * case 7: Intent mediaintent = new Intent(MapResults.this,
             * Uploadmultimedia.class); startActivity(mediaintent);
             *
             * case 8: return exit();
             */
        }
        return super.onOptionsItemSelected(item)
    }

    private fun exit(): Boolean {
        this.finish()
        this.setResult(0)
        return true
    }

    fun requestLocationUpdates() {
        mLocationListener = InitLocationListener()
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager
            .requestLocationUpdates(
                MobeegalApplication.PROVIDER_NAME,
                3, 5000f, mLocationListener
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }

    fun removeLocationUpdates() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(mLocationListener)
    }

    inner class InitLocationListener : LocationListener {

        override fun onLocationChanged(loc: android.location.Location?) {
            if (loc == null) {
                Log.e(LOG_TAG, "location changed to null")
            } else {
                Log.d(LOG_TAG, "location changed : " + loc.toString())
                Log.d(LOG_TAG, "Location updates being received, exiting...")
//                finish();
            }
        }

        override fun onStatusChanged(s: String?, i: Int, bundle: Bundle?) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        fun onStatusChanged(arg0: String?, arg1: Int) {
            // ignore
        }

        override fun onProviderEnabled(arg0: String?) {
            // ignore
        }

        override fun onProviderDisabled(arg0: String?) {
            // ignore
        }
    }

    companion object {
        const val LOG_TAG = "MapResults"
    }
}
