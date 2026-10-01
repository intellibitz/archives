/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.view.maps

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import com.google.android.maps.GeoPoint
import com.google.android.maps.MapView
import com.google.android.maps.Overlay
import com.mobeegal.android.R
import com.mobeegal.android.model.MapLocation
import com.mobeegal.android.view.MapLocationViewer

/**
 * @author jailani
 */
class MapLocationOverlay(private val mapView: MapLocationViewer) : Overlay() {
    //  Store these as global instances so we don't keep reloading every time
    private val bubbleIcon: Bitmap
    private val shadowIcon: Bitmap

    private var innerPaint: Paint? = null
    private var borderPaint: Paint? = null
    private var textPaint: Paint? = null

    //  The currently selected Map Location...if any is selected.  This tracks whether an information
    //  window should be displayed & where...i.e. whether a user 'clicked' on a known map location
    private var selectedMapLocation: MapLocation? = null

    init {
        bubbleIcon = BitmapFactory
            .decodeResource(mapView.resources, R.drawable.bubble)
        shadowIcon = BitmapFactory
            .decodeResource(mapView.resources, R.drawable.shadow)
    }

    override fun onTap(p: GeoPoint, mapView: MapView): Boolean {
        //  Store whether prior popup was displayed so we can call invalidate() & remove it if necessary.
        val isRemovePriorPopup = selectedMapLocation != null

        //  Next test whether a new popup should be displayed
        selectedMapLocation = getHitMapLocation(p)
        if (isRemovePriorPopup || selectedMapLocation != null) {
            mapView.invalidate()
        }

        //  Lastly return true if we handled this onTap()
        return selectedMapLocation != null
    }

    override fun draw(canvas: Canvas, mapview: MapView, flag: Boolean) {
        drawMapLocations(canvas, flag)
        drawInfoWindow(canvas, flag)
    }

    /**
     * Test whether an information balloon should be displayed or a prior
     * balloon hidden.
     */
    private fun getHitMapLocation(tapPoint: GeoPoint?): MapLocation? {
        var tapPoint = tapPoint
        //  Track which MapLocation was hit...if any
        var hitMapLocation: MapLocation? = null

        val hitTestRecr = RectF()
        val screenCoords = IntArray(2)
        val iterator = mapView.getMapLocations().iterator()
        while (iterator.hasNext()) {
            val testLocation = iterator.next()

            //  Translate the MapLocation's lat/long coordinates to screen coordinates
            //	    	calculator.getPointXY(testLocation.getPoint(), screenCoords);

            // Create a 'hit' testing Rectangle w/size and coordinates of our icon
            // Set the 'hit' testing Rectangle with the size and coordinates of our on screen icon
            hitTestRecr.set(
                (-bubbleIcon.width / 2).toFloat(), (-bubbleIcon.height).toFloat(),
                (bubbleIcon.width / 2).toFloat(), 0f
            )
            hitTestRecr.offset(screenCoords[0].toFloat(), screenCoords[1].toFloat())

            //  Finally test for a match between our 'hit' Rectangle and the location clicked by the user
            //    		calculator.getPointXY(tapPoint, screenCoords);
            if (hitTestRecr.contains(screenCoords[0].toFloat(), screenCoords[1].toFloat())) {
                hitMapLocation = testLocation
                break
            }
        }

        //  Lastly clear the newMouseSelection as it has now been processed
        tapPoint = null

        return hitMapLocation
    }

    private fun drawMapLocations(canvas: Canvas, shadow: Boolean) {
        val iterator = mapView.getMapLocations().iterator()
        val screenCoords = IntArray(2)
        while (iterator.hasNext()) {
            val location = iterator.next()
            //			calculator.getPointXY(location.getPoint(), screenCoords);

            if (shadow) {
                //  Only offset the shadow in the y-axis as the shadow is angled so the base is at x=0;
                canvas.drawBitmap(
                    shadowIcon, screenCoords[0].toFloat(),
                    (screenCoords[1] - shadowIcon.height).toFloat(), null
                )
            } else {
                canvas.drawBitmap(
                    bubbleIcon,
                    (screenCoords[0] - bubbleIcon.width / 2).toFloat(),
                    (screenCoords[1] - bubbleIcon.height).toFloat(), null
                )
            }
        }
    }

    private fun drawInfoWindow(canvas: Canvas, shadow: Boolean) {
        if (selectedMapLocation != null) {
            if (shadow) {
                //  Skip painting a shadow in this tutorial
            } else {
                //  First determine the screen coordinates of the selected MapLocation
                val selDestinationOffset = IntArray(2)
                //		    	calculator.getPointXY(selectedMapLocation.getPoint(), selDestinationOffset);

                //  Setup the info window with the right size & location
                val INFO_WINDOW_WIDTH = 125
                val INFO_WINDOW_HEIGHT = 25
                val infoWindowRect =
                    RectF(0f, 0f, INFO_WINDOW_WIDTH.toFloat(), INFO_WINDOW_HEIGHT.toFloat())
                val infoWindowOffsetX =
                    selDestinationOffset[0] - INFO_WINDOW_WIDTH / 2
                val infoWindowOffsetY = selDestinationOffset[1] -
                    INFO_WINDOW_HEIGHT - bubbleIcon.height
                infoWindowRect.offset(infoWindowOffsetX.toFloat(), infoWindowOffsetY.toFloat())

                //  Draw inner info window
                canvas.drawRoundRect(infoWindowRect, 5f, 5f, getInnerPaint())

                //  Draw border for info window
                canvas.drawRoundRect(infoWindowRect, 5f, 5f, getBorderPaint())

                //  Draw the MapLocation's name
                val TEXT_OFFSET_X = 10
                val TEXT_OFFSET_Y = 15
                canvas.drawText(
                    selectedMapLocation!!.getName(),
                    (infoWindowOffsetX + TEXT_OFFSET_X).toFloat(),
                    (infoWindowOffsetY + TEXT_OFFSET_Y).toFloat(), getTextPaint()
                )
            }
        }
    }

    fun getInnerPaint(): Paint {
        if (innerPaint == null) {
            innerPaint = Paint()
            innerPaint!!.setARGB(225, 75, 75, 75) //gray
            innerPaint!!.isAntiAlias = true
        }
        return innerPaint!!
    }

    fun getBorderPaint(): Paint {
        if (borderPaint == null) {
            borderPaint = Paint()
            borderPaint!!.setARGB(255, 255, 255, 255)
            borderPaint!!.isAntiAlias = true
            borderPaint!!.style = Style.STROKE
            borderPaint!!.strokeWidth = 2f
        }
        return borderPaint!!
    }

    fun getTextPaint(): Paint {
        if (textPaint == null) {
            textPaint = Paint()
            textPaint!!.setARGB(255, 255, 255, 255)
            textPaint!!.isAntiAlias = true
        }
        return textPaint!!
    }
}
