package com.androidworks.navsys.wuffit.activity

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.androidworks.navsys.wuffit.R
import com.androidworks.navsys.wuffit.content.Tracker
import com.google.android.maps.GeoPoint
import com.google.android.maps.ItemizedOverlay
import com.google.android.maps.MapActivity
import com.google.android.maps.MapView
import com.google.android.maps.MyLocationOverlay
import com.google.android.maps.Overlay
import com.google.android.maps.OverlayItem
import java.text.SimpleDateFormat
import java.util.Calendar

class DisplayLocation : MapActivity() {

    private var mapView: MapView? = null
    private var myLocationOverlay: MyLocationOverlay? = null
    private var normalTrackerItems: TrackerItemizedOverlay? = null
    private var warningTrackerItems: TrackerItemizedOverlay? = null
    private var itemDetails: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_location)

        findViewById<View>(R.id.main_menu)?.setOnClickListener {
            val intent = Intent("com.androidworks.navsys.wuffit.ACTION_MAIN_MENU").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            startActivity(intent)
        }

        val map = findViewById<MapView>(R.id.display_location)
        mapView = map

        if (map != null) {
            val locOverlay = MyLocationOverlay(this, map)
            myLocationOverlay = locOverlay
            locOverlay.runOnFirstFix {
                map.getController().animateTo(locOverlay.getMyLocation())
            }

            val defaultMarker = ContextCompat.getDrawable(this, android.R.drawable.presence_online)
            val warning = ContextCompat.getDrawable(this, android.R.drawable.presence_busy)
            normalTrackerItems = TrackerItemizedOverlay(defaultMarker)
            warningTrackerItems = TrackerItemizedOverlay(warning)

            val linearLayout = findViewById<LinearLayout>(R.id.zoomview)
            val zoomControls = map.getZoomControls()
            linearLayout?.addView(zoomControls)
        }
    }

    override fun isRouteDisplayed(): Boolean = false

    override fun onResume() {
        super.onResume()
        myLocationOverlay?.enableMyLocation()
        addOverlaysToMap()
    }

    override fun onStop() {
        myLocationOverlay?.disableMyLocation()
        super.onStop()
    }

    private fun addOverlaysToMap() {
        val map = mapView ?: return
        val overlays = map.getOverlays()
        overlays.clear()
        normalTrackerItems?.clear()
        warningTrackerItems?.clear()

        myLocationOverlay?.let { overlays.add(it) }

        val cursor = contentResolver.query(
            Tracker.Locations.CONTENT_URI,
            arrayOf(Tracker.Locations._FID, Tracker.Locations.LOCATION, Tracker.Locations.UPDATE_TIME),
            null,
            null,
            null
        )

        cursor?.use { c ->
            if (c.count > 0) {
                convertGPRMCToOverlayItems(c)
            }
        }

        warningTrackerItems?.let {
            if (it.size() > 0) overlays.add(it)
        }
        normalTrackerItems?.let {
            if (it.size() > 0) overlays.add(it)
        }
    }

    private fun convertGPRMCToOverlayItems(cursor: Cursor) {
        cursor.moveToFirst()
        do {
            val gprmc = cursor.getString(1)
            var loc = "Updated on: <not available>"
            val rt = cursor.getLong(2)
            if (rt > 0) {
                val cal = Calendar.getInstance().apply { timeInMillis = rt }
                val sdf = SimpleDateFormat.getDateTimeInstance()
                loc = "Updated on: ${sdf.format(cal.time)}"
            }

            if (gprmc != null) {
                val id = cursor.getString(0)
                val sel = "${Tracker.Details._ID} = ? "
                val selArgs = arrayOf(id)
                val details = contentResolver.query(
                    Tracker.Details.CONTENT_URI,
                    arrayOf(Tracker.Details.NAME, Tracker.Details.NUMBER),
                    sel,
                    selArgs,
                    null
                )

                if (details == null || details.count == 0) {
                    details?.close()
                    Log.e(TAG, "No Records found in database for tracker id: $id")
                    Toast.makeText(this, "No Records found for tracker id: $id", Toast.LENGTH_LONG).show()
                } else {
                    details.moveToFirst()
                    val detailText = "${details.getString(0)} \n ${details.getString(1)} \n $loc"
                    val item = createTrackerOverlayItem(detailText, gprmc)
                    if (item.isNormal) {
                        normalTrackerItems?.addOverlayItem(item)
                    } else {
                        warningTrackerItems?.addOverlayItem(item)
                    }
                    details.close()
                }
            }
        } while (cursor.moveToNext())
    }

    private fun createTrackerOverlayItem(detail: String, gprmc: String): TrackerOverlayItem {
        val contents = gprmc.split(",")
        var lat = degToDec(contents[4].trim())
        if ("S".equals(contents[5].trim(), ignoreCase = true)) lat = -lat

        var lon = degToDec(contents[6].trim())
        if ("W".equals(contents[7].trim(), ignoreCase = true)) lon = -lon

        val geoPoint = GeoPoint(lat, lon)
        var ts = contents[2].trim().split("\\.")[0]
        if (ts.length >= 6) {
            ts = "${ts.substring(0, 2)}:${ts.substring(2, 4)}:${ts.substring(4, 6)}"
        }

        var ds = contents[10].trim()
        if (ds.length >= 6) {
            ds = "${ds.substring(0, 2)}-${ds.substring(2, 4)}-${ds.substring(4, 6)}"
        }

        val date = " Lat/Long: $geoPoint\n Date: $ds Time: $ts"
        val item = TrackerOverlayItem(geoPoint, detail, date)
        if ("V".equals(contents[3], ignoreCase = true)) {
            item.isNormal = false
        }
        return item
    }

    private fun degToDec(value: String): Int {
        val min = value.substring(value.length - 7)
        val deg = value.substring(0, value.length - min.length)
        return ((deg.toDouble() + (min.toDouble() / 60)) * 1000000).toInt()
    }

    private fun showDetailsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tracker Location Details")
            .setMessage(itemDetails)
            .setCancelable(true)
            .setPositiveButton("Ok", null)
            .show()
    }

    inner class TrackerItemizedOverlay(defaultMarker: Drawable?) :
        ItemizedOverlay<TrackerOverlayItem>(defaultMarker) {

        private val trackerOverlayItems = ArrayList<TrackerOverlayItem>()

        fun addOverlayItem(overlayItem: TrackerOverlayItem) {
            trackerOverlayItems.add(overlayItem)
            populate()
        }

        override fun createItem(i: Int): TrackerOverlayItem = trackerOverlayItems[i]

        override fun size(): Int = trackerOverlayItems.size

        override fun clear() {
            trackerOverlayItems.clear()
        }

        override fun onTap(i: Int): Boolean {
            val res = super.onTap(i)
            val item = getItem(i)
            itemDetails = "${item.title}\n${item.snippet}"
            Log.d(TAG, "Item tapped: $itemDetails")
            showDetailsDialog()
            return res
        }
    }

    class TrackerOverlayItem(
        geoPoint: GeoPoint,
        title: String?,
        snippet: String?
    ) : OverlayItem(geoPoint, title, snippet) {
        var isNormal: Boolean = true
    }

    companion object {
        private const val TAG = "DisplayLocation"
    }
}
