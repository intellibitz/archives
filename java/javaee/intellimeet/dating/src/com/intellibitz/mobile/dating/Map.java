/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intellibitz.mobile.dating;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Menu.Item;
import android.widget.Toast;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.DeviceType;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayController;
import com.google.android.maps.Point;

/**
 *
 * @author gunasekaran
 */
public class Map extends MapActivity{
    private MapView mMapView;
    private Point p1,  p2,  p3,  p4, p5;
    private Bitmap bubbleIcon;
    private Bitmap bubbleIcon1;
    @Override
    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);
        setContentView(R.layout.map);
        bubbleIcon = BitmapFactory.decodeResource(getResources(),R.drawable.bubble);
        bubbleIcon1 = BitmapFactory.decodeResource(getResources(),R.drawable.bubble1);
        mMapView = (MapView) findViewById(R.id.map);
        p1 = new Point((int) (12961539), (int) (80186860));
        p2 = new Point((int) (12994577), (int) (80199297));
        p3 = new Point((int) (13036939), (int) (80230285));
        p4 = new Point((int) (13042928), (int) (80232570));  
        p5 = new Point((int) (13003145), (int) (80253532));
        MapController mc = mMapView.getController();
        MyLocationOverlay mylocation = new MyLocationOverlay();
        OverlayController oc = mMapView.createOverlayController();
        oc.add(mylocation, true);
        mc.animateTo(p1);
        mc.zoomTo(15);
        //Enable Sattelite-Mode, so we will se the
        // Statue of liberty instantly on the screen
        //mMapView.toggleSatellite();
    }

    class MyLocationOverlay extends Overlay {

        @Override
        public void draw(Canvas c, PixelCalculator calc, boolean shadow) {
            super.draw(c, calc, shadow);
            int coords[] = new int[2];
            int coords1[] = new int[2];
            int coords2[] = new int[2];
            int coords3[] = new int[2];
            int coords4[] = new int[2];                        
            calc.getPointXY(p1, coords);
            RectF oval = new RectF(coords[0] - 7, coords[1] + 7, coords[0] + 7, coords[1] - 7);
            Paint paint = new Paint();
            paint.setStyle(Style.FILL);
            paint.setARGB(255, 85, 117, 30);
            c.drawText("you", coords[0] + 9, coords[1], paint);
            paint.setARGB(200, 34, 234, 34);
            //paint.setStrokeWidth(1);
            c.drawBitmap(bubbleIcon, coords[0] - bubbleIcon.width() / 2, coords[1] - bubbleIcon.height(), null);
            //c.drawOval(oval, paint);

            calc.getPointXY(p2, coords1);
            RectF oval1 = new RectF(coords1[0] - 7, coords1[1] + 7, coords1[0] + 7, coords1[1] - 7);
            Paint paint1 = new Paint();
            paint1.setStyle(Style.FILL);
            paint1.setARGB(255, 0, 0, 0);
            calc.getPointXY(p2, coords1);
            c.drawText("Dreammate1", coords1[0] + 9, coords1[1], paint1);
            paint1.setARGB(80, 255, 0, 0);
            paint1.setStrokeWidth(2);
            c.drawLine(coords[0], coords[1], coords1[0], coords1[1], paint1);
            paint1.setStrokeWidth(1);
            paint1.setARGB(80, 255, 0, 0);
            c.drawBitmap(bubbleIcon1, coords1[0] - bubbleIcon.width() / 2, coords1[1] - bubbleIcon.height(), null);
            //c.drawOval(oval1, paint1);

            calc.getPointXY(p3, coords2);
            RectF oval2 = new RectF(coords2[0] - 7, coords2[1] + 7, coords2[0] + 7, coords2[1] - 7);
            Paint paint2 = new Paint();
            paint2.setStyle(Style.FILL);
            paint2.setARGB(255, 0, 0, 0);
            // calc.getPointXY(p2, coords1);
            c.drawText("Dreammate2", coords2[0] + 9, coords2[1], paint2);
            paint2.setARGB(255, 255, 0, 0);
            paint2.setStrokeWidth(2);
            c.drawLine(coords[0], coords[1], coords2[0], coords2[1], paint2);
            paint2.setStrokeWidth(1);
            paint2.setARGB(80, 255, 0, 0);
            c.drawBitmap(bubbleIcon1, coords2[0] - bubbleIcon.width() / 2, coords2[1] - bubbleIcon.height(), null);
            //c.drawOval(oval2, paint2);

            calc.getPointXY(p4, coords3);
            RectF oval3 = new RectF(coords3[0] - 7, coords3[1] + 7, coords3[0] + 7, coords3[1] - 7);
            Paint paint3 = new Paint();
            paint3.setStyle(Style.FILL);
            paint3.setARGB(255, 0, 0, 0);
            //calc.getPointXY(p4, coords1);
            c.drawText("Dreammate3", coords3[0] + 9, coords3[1], paint3);
            paint3.setARGB(255, 255, 0, 0);
            paint3.setStrokeWidth(2);
            c.drawLine(coords[0], coords[1], coords3[0], coords3[1], paint3);
            paint3.setStrokeWidth(1);
            paint3.setARGB(80, 255, 0, 0);
            c.drawBitmap(bubbleIcon1, coords3[0] - bubbleIcon.width() / 2, coords3[1] - bubbleIcon.height(), null);
            //c.drawOval(oval3, paint3);

            calc.getPointXY(p5, coords4);
            RectF oval4 = new RectF(coords4[0] - 7, coords4[1] + 7, coords4[0] + 7, coords4[1] - 7);
            Paint paint4 = new Paint();
            paint3.setStyle(Style.FILL);
            paint3.setARGB(255, 0, 0, 0);
            //calc.getPointXY(p4, coords1);
            c.drawText("Dreammate4", coords4[0] + 9, coords4[1], paint4);
            paint4.setARGB(255, 255, 0, 0);
            paint4.setStrokeWidth(2);
            c.drawLine(coords[0], coords[1], coords4[0], coords4[1], paint4);
            paint4.setStrokeWidth(1);
            paint4.setARGB(80, 255, 0, 0);
            c.drawBitmap(bubbleIcon1, coords4[0] - bubbleIcon.width() / 2, coords4[1] - bubbleIcon.height(), null);
            //c.drawOval(oval3, paint3);
            
        }
        @Override
        public boolean onTap(DeviceType devicetype,Point p,PixelCalculator calculator){
               this.getHitMapLocation(calculator,p);
               return true;
            }
    private void  getHitMapLocation(PixelCalculator calculator, Point	tapPoint) {
    	
    	//  Track which MapLocation was hit...if any
    	//MapLocation hitMapLocation = null;
		
                RectF hitTestRecr = new RectF();
		int[] screenCoords = new int[2];
	    	calculator.getPointXY(p1, screenCoords);
    		hitTestRecr.set(-bubbleIcon1.width()/2,-bubbleIcon1.height(),bubbleIcon1.width()/2,0);
    		hitTestRecr.offset(screenCoords[0],screenCoords[1]);
    		calculator.getPointXY(tapPoint, screenCoords);
    		if (hitTestRecr.contains(screenCoords[0],screenCoords[1])) {
    			Toast.makeText(Map.this, "Cant' chat yourself. Look around to find your DreamDate for chatting", Toast.LENGTH_LONG).show();
    		
    		}
                RectF hitTestRecr1 = new RectF();
		int[] screenCoords1 = new int[2];
	    	calculator.getPointXY(p2, screenCoords1);
    		hitTestRecr1.set(-bubbleIcon1.width()/2,-bubbleIcon1.height(),bubbleIcon1.width()/2,0);
    		hitTestRecr1.offset(screenCoords1[0],screenCoords1[1]);
    		calculator.getPointXY(tapPoint, screenCoords1);
    		if (hitTestRecr1.contains(screenCoords1[0],screenCoords1[1])) {
    		Intent intent=new Intent(Map.this,Chat.class);
                   startActivity(intent);
                }
                RectF hitTestRecr2 = new RectF();
		int[] screenCoords2 = new int[2];
	    	calculator.getPointXY(p3, screenCoords2);
    		hitTestRecr2.set(-bubbleIcon1.width()/2,-bubbleIcon1.height(),bubbleIcon1.width()/2,0);
    		hitTestRecr2.offset(screenCoords2[0],screenCoords2[1]);
    		calculator.getPointXY(tapPoint, screenCoords2);
    		if (hitTestRecr2.contains(screenCoords2[0],screenCoords2[1])) {
    			Intent intent2=new Intent(Map.this,Chat.class);
                   startActivity(intent2);
    		}
                RectF hitTestRecr3 = new RectF();
		int[] screenCoords3 = new int[2];
	    	calculator.getPointXY(p4, screenCoords3);
    		hitTestRecr3.set(-bubbleIcon1.width()/2,-bubbleIcon1.height(),bubbleIcon1.width()/2,0);
    		hitTestRecr3.offset(screenCoords3[0],screenCoords3[1]);
    		calculator.getPointXY(tapPoint, screenCoords3);
    		if (hitTestRecr3.contains(screenCoords3[0],screenCoords3[1])) {
    			Intent intent=new Intent(Map.this,Chat.class);
                   startActivity(intent);
    		}
                RectF hitTestRecr4 = new RectF();
		int[] screenCoords4 = new int[2];
	    	calculator.getPointXY(p5, screenCoords4);
    		hitTestRecr4.set(-bubbleIcon1.width()/2,-bubbleIcon1.height(),bubbleIcon1.width()/2,0);
    		hitTestRecr4.offset(screenCoords4[0],screenCoords4[1]);
    		calculator.getPointXY(tapPoint, screenCoords4);
    		if (hitTestRecr4.contains(screenCoords4[0],screenCoords4[1])) {
    			Intent intent=new Intent(Map.this,Chat.class);
                   startActivity(intent);
    		}                                
    	}
    }
    

    @Override
 public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_I) {
            // Zoom In
            int level = mMapView.getZoomLevel();
            mMapView.getController().zoomTo(level + 1);
        
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_O) {
            // Zoom Out
            int level = mMapView.getZoomLevel();
            mMapView.getController().zoomTo(level - 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_S) {
            // Switch on the satellite images
            mMapView.toggleSatellite();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_T) {
            // Switch on traffic overlays
            mMapView.toggleTraffic();
            return true;
        }
        

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.add(0, 1, "Back to Home");
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(Item item) {
        switch (item.getId()) {
            case 1:
                Intent intent = new Intent(Map.this, Dating.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}


