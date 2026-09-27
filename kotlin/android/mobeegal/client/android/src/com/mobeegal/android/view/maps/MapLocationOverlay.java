/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.view.maps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobeegal.android.R;
import com.mobeegal.android.model.MapLocation;
import com.mobeegal.android.view.MapLocationViewer;

import java.util.Iterator;

/**
 * @author jailani
 */
public class MapLocationOverlay
        extends Overlay
{
    //  Store these as global instances so we don't keep reloading every time
    private Bitmap bubbleIcon, shadowIcon;

    private MapLocationViewer mapView;

    private Paint innerPaint, borderPaint, textPaint;

    //  The currently selected Map Location...if any is selected.  This tracks whether an information  
    //  window should be displayed & where...i.e. whether a user 'clicked' on a known map location
    private MapLocation selectedMapLocation;

    public MapLocationOverlay(MapLocationViewer mapView)
    {

        this.mapView = mapView;

        bubbleIcon = BitmapFactory
                .decodeResource(mapView.getResources(), R.drawable.bubble);
        shadowIcon = BitmapFactory
                .decodeResource(mapView.getResources(), R.drawable.shadow);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView)
    {

        //  Store whether prior popup was displayed so we can call invalidate() & remove it if necessary.
        boolean isRemovePriorPopup = selectedMapLocation != null;

        //  Next test whether a new popup should be displayed
        selectedMapLocation = getHitMapLocation(p);
        if (isRemovePriorPopup || selectedMapLocation != null)
        {
            mapView.invalidate();
        }

        //  Lastly return true if we handled this onTap()
        return selectedMapLocation != null;
    }

    @Override
    public void draw(Canvas canvas, MapView mapview, boolean flag)
    {

        drawMapLocations(canvas, flag);
        drawInfoWindow(canvas, flag);
    }

    /**
     * Test whether an information balloon should be displayed or a prior
     * balloon hidden.
     */
    private MapLocation getHitMapLocation(GeoPoint tapPoint)
    {

        //  Track which MapLocation was hit...if any
        MapLocation hitMapLocation = null;

        RectF hitTestRecr = new RectF();
        int[] screenCoords = new int[2];
        Iterator<MapLocation> iterator = mapView.getMapLocations().iterator();
        while (iterator.hasNext())
        {
            MapLocation testLocation = iterator.next();

            //  Translate the MapLocation's lat/long coordinates to screen coordinates
//	    	calculator.getPointXY(testLocation.getPoint(), screenCoords);

            // Create a 'hit' testing Rectangle w/size and coordinates of our icon
            // Set the 'hit' testing Rectangle with the size and coordinates of our on screen icon
            hitTestRecr.set(-bubbleIcon.getWidth() / 2, -bubbleIcon.getHeight(),
                    bubbleIcon.getWidth() / 2, 0);
            hitTestRecr.offset(screenCoords[0], screenCoords[1]);

            //  Finally test for a match between our 'hit' Rectangle and the location clicked by the user
//    		calculator.getPointXY(tapPoint, screenCoords);
            if (hitTestRecr.contains(screenCoords[0], screenCoords[1]))
            {
                hitMapLocation = testLocation;
                break;
            }
        }

        //  Lastly clear the newMouseSelection as it has now been processed
        tapPoint = null;

        return hitMapLocation;
    }

    private void drawMapLocations(Canvas canvas, boolean shadow)
    {

        Iterator<MapLocation> iterator = mapView.getMapLocations().iterator();
        int[] screenCoords = new int[2];
        while (iterator.hasNext())
        {
            MapLocation location = iterator.next();
//			calculator.getPointXY(location.getPoint(), screenCoords);

            if (shadow)
            {
                //  Only offset the shadow in the y-axis as the shadow is angled so the base is at x=0;
                canvas.drawBitmap(shadowIcon, screenCoords[0],
                        screenCoords[1] - shadowIcon.getHeight(), null);
            }
            else
            {
                canvas.drawBitmap(bubbleIcon,
                        screenCoords[0] - bubbleIcon.getWidth() / 2,
                        screenCoords[1] - bubbleIcon.getHeight(), null);
            }
        }
    }

    private void drawInfoWindow(Canvas canvas, boolean shadow)
    {

        if (selectedMapLocation != null)
        {
            if (shadow)
            {
                //  Skip painting a shadow in this tutorial
            }
            else
            {
                //  First determine the screen coordinates of the selected MapLocation
                int[] selDestinationOffset = new int[2];
//		    	calculator.getPointXY(selectedMapLocation.getPoint(), selDestinationOffset);

                //  Setup the info window with the right size & location
                int INFO_WINDOW_WIDTH = 125;
                int INFO_WINDOW_HEIGHT = 25;
                RectF infoWindowRect =
                        new RectF(0, 0, INFO_WINDOW_WIDTH, INFO_WINDOW_HEIGHT);
                int infoWindowOffsetX =
                        selDestinationOffset[0] - INFO_WINDOW_WIDTH / 2;
                int infoWindowOffsetY = selDestinationOffset[1] -
                        INFO_WINDOW_HEIGHT - bubbleIcon.getHeight();
                infoWindowRect.offset(infoWindowOffsetX, infoWindowOffsetY);

                //  Draw inner info window
                canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());

                //  Draw border for info window
                canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());

                //  Draw the MapLocation's name
                int TEXT_OFFSET_X = 10;
                int TEXT_OFFSET_Y = 15;
                canvas.drawText(selectedMapLocation.getName(),
                        infoWindowOffsetX + TEXT_OFFSET_X,
                        infoWindowOffsetY + TEXT_OFFSET_Y, getTextPaint());
            }
        }
    }

    public Paint getInnerPaint()
    {
        if (innerPaint == null)
        {
            innerPaint = new Paint();
            innerPaint.setARGB(225, 75, 75, 75); //gray
            innerPaint.setAntiAlias(true);
        }
        return innerPaint;
    }

    public Paint getBorderPaint()
    {
        if (borderPaint == null)
        {
            borderPaint = new Paint();
            borderPaint.setARGB(255, 255, 255, 255);
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Style.STROKE);
            borderPaint.setStrokeWidth(2);
        }
        return borderPaint;
    }

    public Paint getTextPaint()
    {
        if (textPaint == null)
        {
            textPaint = new Paint();
            textPaint.setARGB(255, 255, 255, 255);
            textPaint.setAntiAlias(true);
        }
        return textPaint;
    }
}
