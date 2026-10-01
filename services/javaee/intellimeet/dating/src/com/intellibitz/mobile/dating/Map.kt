/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intellibitz.mobile.dating

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.Menu.Item
import android.widget.Toast
import com.google.android.maps.MapActivity
import com.google.android.maps.MapController
import com.google.android.maps.MapView
import com.google.android.maps.MapView.DeviceType
import com.google.android.maps.Overlay
import com.google.android.maps.OverlayController
import com.google.android.maps.Point

/**
 *
 * @author gunasekaran
 */
class Map : MapActivity() {
    private var mMapView: MapView? = null
    private var p1: Point? = null
    private var p2: Point? = null
    private var p3: Point? = null
    private var p4: Point? = null
    private var p5: Point? = null
    private var bubbleIcon: Bitmap? = null
    private var bubbleIcon1: Bitmap? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.map)
        bubbleIcon = BitmapFactory.decodeResource(resources, R.drawable.bubble)
        bubbleIcon1 = BitmapFactory.decodeResource(resources, R.drawable.bubble1)
        mMapView = findViewById(R.id.map) as MapView
        p1 = Point(12961539, 80186860)
        p2 = Point(12994577, 80199297)
        p3 = Point(13036939, 80230285)
        p4 = Point(13042928, 80232570)
        p5 = Point(13003145, 80253532)
        val mc = mMapView!!.controller
        val mylocation = MyLocationOverlay()
        val oc = mMapView!!.createOverlayController()
        oc.add(mylocation, true)
        mc.animateTo(p1)
        mc.zoomTo(15)
        //Enable Sattelite-Mode, so we will se the
        // Statue of liberty instantly on the screen
        //mMapView.toggleSatellite();
    }

    inner class MyLocationOverlay : Overlay() {

        override fun draw(c: Canvas, calc: PixelCalculator, shadow: Boolean) {
            super.draw(c, calc, shadow)
            val coords = IntArray(2)
            val coords1 = IntArray(2)
            val coords2 = IntArray(2)
            val coords3 = IntArray(2)
            val coords4 = IntArray(2)
            calc.getPointXY(p1, coords)
            val oval = RectF((coords[0] - 7).toFloat(), (coords[1] + 7).toFloat(), (coords[0] + 7).toFloat(), (coords[1] - 7).toFloat())
            val paint = Paint()
            paint.style = Style.FILL
            paint.setARGB(255, 85, 117, 30)
            c.drawText("you", (coords[0] + 9).toFloat(), coords[1].toFloat(), paint)
            paint.setARGB(200, 34, 234, 34)
            //paint.setStrokeWidth(1);
            c.drawBitmap(bubbleIcon, (coords[0] - bubbleIcon!!.width() / 2).toFloat(), (coords[1] - bubbleIcon!!.height()).toFloat(), null as Paint?)
            //c.drawOval(oval, paint);

            calc.getPointXY(p2, coords1)
            val oval1 = RectF((coords1[0] - 7).toFloat(), (coords1[1] + 7).toFloat(), (coords1[0] + 7).toFloat(), (coords1[1] - 7).toFloat())
            val paint1 = Paint()
            paint1.style = Style.FILL
            paint1.setARGB(255, 0, 0, 0)
            calc.getPointXY(p2, coords1)
            c.drawText("Dreammate1", (coords1[0] + 9).toFloat(), coords1[1].toFloat(), paint1)
            paint1.setARGB(80, 255, 0, 0)
            paint1.strokeWidth = 2f
            c.drawLine(coords[0].toFloat(), coords[1].toFloat(), coords1[0].toFloat(), coords1[1].toFloat(), paint1)
            paint1.strokeWidth = 1f
            paint1.setARGB(80, 255, 0, 0)
            c.drawBitmap(bubbleIcon1, (coords1[0] - bubbleIcon!!.width() / 2).toFloat(), (coords1[1] - bubbleIcon!!.height()).toFloat(), null as Paint?)
            //c.drawOval(oval1, paint1);

            calc.getPointXY(p3, coords2)
            val oval2 = RectF((coords2[0] - 7).toFloat(), (coords2[1] + 7).toFloat(), (coords2[0] + 7).toFloat(), (coords2[1] - 7).toFloat())
            val paint2 = Paint()
            paint2.style = Style.FILL
            paint2.setARGB(255, 0, 0, 0)
            // calc.getPointXY(p2, coords1);
            c.drawText("Dreammate2", (coords2[0] + 9).toFloat(), coords2[1].toFloat(), paint2)
            paint2.setARGB(255, 255, 0, 0)
            paint2.strokeWidth = 2f
            c.drawLine(coords[0].toFloat(), coords[1].toFloat(), coords2[0].toFloat(), coords2[1].toFloat(), paint2)
            paint2.strokeWidth = 1f
            paint2.setARGB(80, 255, 0, 0)
            c.drawBitmap(bubbleIcon1, (coords2[0] - bubbleIcon!!.width() / 2).toFloat(), (coords2[1] - bubbleIcon!!.height()).toFloat(), null as Paint?)
            //c.drawOval(oval2, paint2);

            calc.getPointXY(p4, coords3)
            val oval3 = RectF((coords3[0] - 7).toFloat(), (coords3[1] + 7).toFloat(), (coords3[0] + 7).toFloat(), (coords3[1] - 7).toFloat())
            val paint3 = Paint()
            paint3.style = Style.FILL
            paint3.setARGB(255, 0, 0, 0)
            //calc.getPointXY(p4, coords1);
            c.drawText("Dreammate3", (coords3[0] + 9).toFloat(), coords3[1].toFloat(), paint3)
            paint3.setARGB(255, 255, 0, 0)
            paint3.strokeWidth = 2f
            c.drawLine(coords[0].toFloat(), coords[1].toFloat(), coords3[0].toFloat(), coords3[1].toFloat(), paint3)
            paint3.strokeWidth = 1f
            paint3.setARGB(80, 255, 0, 0)
            c.drawBitmap(bubbleIcon1, (coords3[0] - bubbleIcon!!.width() / 2).toFloat(), (coords3[1] - bubbleIcon!!.height()).toFloat(), null as Paint?)
            //c.drawOval(oval3, paint3);

            calc.getPointXY(p5, coords4)
            val oval4 = RectF((coords4[0] - 7).toFloat(), (coords4[1] + 7).toFloat(), (coords4[0] + 7).toFloat(), (coords4[1] - 7).toFloat())
            val paint4 = Paint()
            paint3.style = Style.FILL
            paint3.setARGB(255, 0, 0, 0)
            //calc.getPointXY(p4, coords1);
            c.drawText("Dreammate4", (coords4[0] + 9).toFloat(), coords4[1].toFloat(), paint4)
            paint4.setARGB(255, 255, 0, 0)
            paint4.strokeWidth = 2f
            c.drawLine(coords[0].toFloat(), coords[1].toFloat(), coords4[0].toFloat(), coords4[1].toFloat(), paint4)
            paint4.strokeWidth = 1f
            paint4.setARGB(80, 255, 0, 0)
            c.drawBitmap(bubbleIcon1, (coords4[0] - bubbleIcon!!.width() / 2).toFloat(), (coords4[1] - bubbleIcon!!.height()).toFloat(), null as Paint?)
            //c.drawOval(oval3, paint3);
        }

        override fun onTap(devicetype: DeviceType, p: Point, calculator: PixelCalculator): Boolean {
            this.getHitMapLocation(calculator, p)
            return true
        }

        private fun getHitMapLocation(calculator: PixelCalculator, tapPoint: Point) {
            //  Track which MapLocation was hit...if any
            //MapLocation hitMapLocation = null;

            val hitTestRecr = RectF()
            val screenCoords = IntArray(2)
            calculator.getPointXY(p1, screenCoords)
            hitTestRecr.set((-bubbleIcon1!!.width() / 2).toFloat(), (-bubbleIcon1!!.height()).toFloat(), (bubbleIcon1!!.width() / 2).toFloat(), 0f)
            hitTestRecr.offset(screenCoords[0].toFloat(), screenCoords[1].toFloat())
            calculator.getPointXY(tapPoint, screenCoords)
            if (hitTestRecr.contains(screenCoords[0].toFloat(), screenCoords[1].toFloat())) {
                Toast.makeText(this@Map, "Cant' chat yourself. Look around to find your DreamDate for chatting", Toast.LENGTH_LONG).show()
            }
            val hitTestRecr1 = RectF()
            val screenCoords1 = IntArray(2)
            calculator.getPointXY(p2, screenCoords1)
            hitTestRecr1.set((-bubbleIcon1!!.width() / 2).toFloat(), (-bubbleIcon1!!.height()).toFloat(), (bubbleIcon1!!.width() / 2).toFloat(), 0f)
            hitTestRecr1.offset(screenCoords1[0].toFloat(), screenCoords1[1].toFloat())
            calculator.getPointXY(tapPoint, screenCoords1)
            if (hitTestRecr1.contains(screenCoords1[0].toFloat(), screenCoords1[1].toFloat())) {
                val intent = Intent(this@Map, Chat::class.java)
                startActivity(intent)
            }
            val hitTestRecr2 = RectF()
            val screenCoords2 = IntArray(2)
            calculator.getPointXY(p3, screenCoords2)
            hitTestRecr2.set((-bubbleIcon1!!.width() / 2).toFloat(), (-bubbleIcon1!!.height()).toFloat(), (bubbleIcon1!!.width() / 2).toFloat(), 0f)
            hitTestRecr2.offset(screenCoords2[0].toFloat(), screenCoords2[1].toFloat())
            calculator.getPointXY(tapPoint, screenCoords2)
            if (hitTestRecr2.contains(screenCoords2[0].toFloat(), screenCoords2[1].toFloat())) {
                val intent2 = Intent(this@Map, Chat::class.java)
                startActivity(intent2)
            }
            val hitTestRecr3 = RectF()
            val screenCoords3 = IntArray(2)
            calculator.getPointXY(p4, screenCoords3)
            hitTestRecr3.set((-bubbleIcon1!!.width() / 2).toFloat(), (-bubbleIcon1!!.height()).toFloat(), (bubbleIcon1!!.width() / 2).toFloat(), 0f)
            hitTestRecr3.offset(screenCoords3[0].toFloat(), screenCoords3[1].toFloat())
            calculator.getPointXY(tapPoint, screenCoords3)
            if (hitTestRecr3.contains(screenCoords3[0].toFloat(), screenCoords3[1].toFloat())) {
                val intent = Intent(this@Map, Chat::class.java)
                startActivity(intent)
            }
            val hitTestRecr4 = RectF()
            val screenCoords4 = IntArray(2)
            calculator.getPointXY(p5, screenCoords4)
            hitTestRecr4.set((-bubbleIcon1!!.width() / 2).toFloat(), (-bubbleIcon1!!.height()).toFloat(), (bubbleIcon1!!.width() / 2).toFloat(), 0f)
            hitTestRecr4.offset(screenCoords4[0].toFloat(), screenCoords4[1].toFloat())
            calculator.getPointXY(tapPoint, screenCoords4)
            if (hitTestRecr4.contains(screenCoords4[0].toFloat(), screenCoords4[1].toFloat())) {
                val intent = Intent(this@Map, Chat::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_I) {
            // Zoom In
            val level = mMapView!!.zoomLevel
            mMapView!!.controller.zoomTo(level + 1)

            return true
        } else if (keyCode == KeyEvent.KEYCODE_O) {
            // Zoom Out
            val level = mMapView!!.zoomLevel
            mMapView!!.controller.zoomTo(level - 1)
            return true
        } else if (keyCode == KeyEvent.KEYCODE_S) {
            // Switch on the satellite images
            mMapView!!.toggleSatellite()
            return true
        } else if (keyCode == KeyEvent.KEYCODE_T) {
            // Switch on traffic overlays
            mMapView!!.toggleTraffic()
            return true
        }

        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val ret = super.onCreateOptionsMenu(menu)
        menu.add(0, 1, "Back to Home")
        return ret
    }

    override fun onOptionsItemSelected(item: Item): Boolean {
        when (item.id) {
            1 -> {
                val intent = Intent(this@Map, Dating::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
