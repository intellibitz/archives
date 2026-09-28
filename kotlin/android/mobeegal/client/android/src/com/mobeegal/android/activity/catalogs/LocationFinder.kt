/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity.catalogs


import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Address
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.maps.GeoPoint
import com.google.android.maps.MapActivity
import com.google.android.maps.MapController
import com.google.android.maps.MapView
import com.google.android.maps.Overlay
import com.google.common.geom.Point
import com.mobeegal.android.R
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient

/**
 * @author Work
 */
class LocationFinder : MapActivity() {

    private var mMapview: MapView? = null
    private var mc: MapController? = null
    private var bubbleIcon: Bitmap? = null
    private var latitudeandlongitude: TextView? = null
    private var k: Int = 0
    private var m: Int = 0
    private var myProgressDialog: ProgressDialog? = null
    private var response: String? = null
    private var addresses: Array<Address?>? = null
    private var myDatabase: SQLiteDatabase? = null
    var getcategory: String? = null
    var getstufftype: String? = null
    private var initialLatitude: Int = 0
    private var initialLongitude: Int = 0
    private var initialZoomLevel: Int = 0
    private var p: Point? = null
    private var tableName: String? = null
    private var subCountryname: String? = null
    private var substring: String? = null
    var getkey: Int = 0

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        // ToDo add your GUI initialization code here
        setContentView(R.layout.locationfinder)
        val b = this.intent.extras
        if (b != null) {
            tableName = b.getString("tablename")
            getkey = b.getInt("key")
        }
        mMapview = findViewById(R.id.map) as MapView
        val mylocation = MyLocationOverlay()
//        OverlayController oc = mMapview.createOverlayController();
        mc = mMapview!!.controller
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val columnname = arrayOf("category", "stufftype")
            val cursor = myDatabase!!
                .query(tableName, columnname, null, null, null, null, null)

            if (cursor != null) {
                if (cursor.isFirst) {
                    getcategory =
                        cursor.getString(
                            cursor.getColumnIndexOrThrow("category")
                        )
                    getstufftype = cursor.getString(
                        cursor.getColumnIndexOrThrow("stufftype")
                    )
                }
            }
        } catch (e: Exception) {

        }
        try {

            val cols = arrayOf("latitude", "longitude", "zoomlevel")
            val c = myDatabase!!.query(
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
            p = Point(initialLatitude, initialLongitude)
//            oc.add(mylocation, true);
//            mc.animateTo(p);
            mc!!.setZoom(initialZoomLevel)

        } catch (ex: Exception) {
        }

        latitudeandlongitude = findViewById(R.id.latandlong) as TextView

//        mc.setZoom(12);
//        Point point1 = new Point(13036036, 80270534);
//        mc.animateTo(point1);
//
//
//        oc.add(mylocation, true);

        val zoomIn = findViewById(R.id.zoomin) as ImageButton
        zoomIn.setOnClickListener(OnClickListener {
            val level = mMapview!!.zoomLevel
            mMapview!!.controller.setZoom(level + 1)
            try {
                myDatabase!!.execSQL(
                    "UPDATE selectedlocation set zoomlevel=" +
                        (level + 1) + ";"
                )

            } catch (exce: Exception) {
            }
        })


        val zoomOut = findViewById(R.id.zoomout) as ImageButton
        zoomOut.setOnClickListener(OnClickListener {
            val level = mMapview!!.zoomLevel
            mMapview!!.controller.setZoom(level - 1)
            try {
                myDatabase!!.execSQL(
                    "UPDATE selectedlocation set zoomlevel=" +
                        (level - 1) + ";"
                )

            } catch (exce: Exception) {
            }
        })
        bubbleIcon = BitmapFactory
            .decodeResource(resources, R.drawable.jewelry_icon)

        val save = findViewById(R.id.savinglocation) as Button
        save.setOnClickListener(View.OnClickListener {
            if (getcategory == "Dating") {
                val locationfinder =
                    Intent(this@LocationFinder, Dating::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Matrimony") {
                val locationfinder =
                    Intent(this@LocationFinder, Matrimony::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Cars") {
                val locationfinder =
                    Intent(this@LocationFinder, Cars::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Rental") {
                val locationfinder =
                    Intent(this@LocationFinder, Home::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Restaurants") {
                val locationfinder =
                    Intent(this@LocationFinder, Restaurants::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Movies") {
                val locationfinder =
                    Intent(this@LocationFinder, Movies::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Jewelry") {
                val locationfinder =
                    Intent(this@LocationFinder, Jewelry::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            }
        })
    }

    override fun isRouteDisplayed(): Boolean {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    private fun write() {
        // TODO Auto-generated method stub

        val trackingLocation = findViewById(R.id.trackinglocation) as Button
        trackingLocation.setOnClickListener(OnClickListener {
            // TODO Auto-generated method stub
            try {
                latitudeandlongitude!!.text = "Searching Location........."
                val http: HttpClient = DefaultHttpClient()
/*
                    PostMethod httpPost = new PostMethod("http://ws.geonames.org/findNearbyPlaceName?lat=" + k / 1E6 + "&lng=" + m / 1E6);
                    //        PostMethod httpPost = new PostMethod("http://ws.geonames.org/findNearestAddress?lat="+k/1E6+"&lng="+m/1E6 );
                    http.executeMethod(httpPost);
*/
//                    response = httpPost.getResponseBodyAsString();
                val a = response!!.indexOf("<name>")
                val str = "<name>"
                val b = response!!.indexOf("</name>")
                val c = response!!.indexOf("<countryName>")
                val d = response!!.indexOf("</countryName>")
                val str1 = "<countryName>"
                val subCountryname =
                    response!!.substring(c + str1.length, d)
                val substring = response!!.substring(a + str.length, b)
                if (getstufftype == "istuff") {
                    myDatabase!!.execSQL(
                        "UPDATE " + tableName +
                            " set iarea='" + substring + "', icountry='" +
                            subCountryname + "', icity='" + "" +
                            "', ilatitude='" + k / 1E6 + "', ilongitude='" +
                            m / 1E6 + "';"
                    )
                } else if (getstufftype == "ustuff") {
                    myDatabase!!.execSQL(
                        "UPDATE " + tableName +
                            " set uarea='" + substring + "', ucountry='" +
                            subCountryname + "', ucity='" + "" +
                            "', ulatitude='" + k / 1E6 + "', ulongitude='" +
                            m / 1E6 + "';"
                    )
                }
                //latitudeandlongitude.setText("Latitude:" + k / 1E6 + "\n" + "Longitude:" + m / 1E6 + "\n " + substring + "\n" + subCountryname);
                latitudeandlongitude!!
                    .setText(substring + "\n" + subCountryname)
//                    Toast.makeText(LocationFinder.this, substring +", "+ subCountryname, Toast.LENGTH_LONG).show();
//                    GmmGeocoder geocoder = new GmmGeocoder(Locale.getDefault());
//                    addresses = geocoder.query(substring, GmmGeocoder.QUERY_TYPE_LOCATION, 0, 0, 180, 360);
                for (i in addresses!!.indices) {
                    val theta = m / 1E6 - addresses!![i]!!.longitude
                    var dist = Math.sin(k / 1E6 * (Math.PI / 180.0)) *
                        Math.sin(
                            addresses!![i]!!.latitude *
                                (Math.PI / 180.0)
                        ) + Math
                        .cos(k / 1E6 * (Math.PI / 180.0)) * Math.cos(
                        addresses!![i]!!.latitude *
                            (Math.PI / 180.0)
                    ) *
                        Math.cos(theta * (Math.PI / 180.0))
                    dist = Math.acos(dist)
                    dist = ((dist / Math.PI) * 180.0)
                    dist = dist * 60 * 1.1515
                    dist = dist * 1.609344
                    if (dist < 100) {
                        if (getstufftype == "istuff") {
//                                myDatabase.execSQL("UPDATE " + tableName + " set iarea='" + substring + "," + addresses[i].getLocality() + "', icountry='" + subCountryname + "', icity='" + addresses[i].getRegion() + "', ilatitude='" + k / 1E6 + "', ilongitude='" + m / 1E6 + "';");
                        } else if (getstufftype == "ustuff") {
//                                myDatabase.execSQL("UPDATE " + tableName + " set uarea='" + substring + "," + addresses[i].getLocality() + "', ucountry='" + subCountryname + "', ucity='" + addresses[i].getRegion() + "', ulatitude='" + k / 1E6 + "', ulongitude='" + m / 1E6 + "';");
                        }
//                            Toast.makeText(LocationFinder.this, substring + "," + addresses[i].getLocality() + "\n" + addresses[i].getRegion() + "," + addresses[i].getCountryName(), Toast.LENGTH_LONG).show();
//                            latitudeandlongitude.setText(substring + "," + addresses[i].getLocality() + "\n" + addresses[i].getRegion() + "," + addresses[i].getCountryName());
                    }
                }
                myDatabase!!.execSQL(
                    "UPDATE selectedlocation set latitude=" +
                        k + ", longitude=" + m + ";"
                )//
            } catch (ex: Exception) {
            }
        })
    }

    inner class MyLocationOverlay : Overlay() {

        private var textPaint: Paint? = null
        private var borderPaint: Paint? = null
        private var innerPaint: Paint? = null
        private var innerPaint1: Paint? = null
        private var selectedIcons: IntArray? = null
        private var count: Int = 0
        var isRemove: Boolean = false

        override fun draw(canvas: Canvas, mapview: MapView, shadow: Boolean) {
            val centre = mMapview!!.mapCenter
            k = centre.latitudeE6
            m = centre.longitudeE6

            canvas.drawText(
                "longitude: " + m / 1E6 +
                    ", latitude: " + k / 1E6,
                5f, 15f, getTextPaint()
            )
            write()
            drawMapLocations(canvas, shadow)

        }

        private fun drawMapLocations(
            canvas: Canvas,
            shadow: Boolean
        ): Boolean {

            if (shadow) {

            } else {
                val coords = IntArray(2)
                val p = Point(k, m)
//                calculator.getPointXY(p, coords);
                canvas.drawBitmap(
                    bubbleIcon!!, (coords[0] - 10).toFloat(),
                    (coords[1] - bubbleIcon!!.height).toFloat(), null
                )
            }
            return false
        }

//        public Paint getInnerPaint() {
//            if (innerPaint == null) {
//                innerPaint = new Paint();
//                innerPaint.setARGB(100, 75, 75, 75); //gray
//                innerPaint.setAntiAlias(true);
//            }
//            return innerPaint;
//        }
//
//        public Paint getInnerPaint1() {
//            if (innerPaint1 == null) {
//                innerPaint1 = new Paint();
//                innerPaint1.setARGB(255, 75, 75, 75); //gray
//                innerPaint1.setAntiAlias(true);
//            }
//            return innerPaint1;
//        }
//
//        public Paint getBorderPaint() {
//
//            if (borderPaint == null) {
//                borderPaint = new Paint();
//                borderPaint.setARGB(255, 255, 255, 255);
//                borderPaint.setAntiAlias(true);
//                borderPaint.setStyle(Style.STROKE);
//                borderPaint.setStrokeWidth(2);
//            }
//            return borderPaint;

        //        }

        fun getTextPaint(): Paint {
            if (textPaint == null) {
                textPaint = Paint()
                textPaint!!.setARGB(255, 0, 0, 0)
                textPaint!!.isAntiAlias = true
            }
            return textPaint!!
        }
    }
}
