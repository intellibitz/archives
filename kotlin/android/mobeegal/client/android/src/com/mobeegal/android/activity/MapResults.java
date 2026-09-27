package com.mobeegal.android.activity;

/*
<!--
$Id:: MapResults.java 14 2008-08-19 06:36:45Z muthu.ramadoss                 $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.mobeegal.android.MobeegalApplication;
import com.mobeegal.android.R;
import com.mobeegal.android.model.MstuffLocations;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapResults
        extends MapActivity
{
    private LinearLayout hitoverlay;
    private TextView mhittext;
    private LinearLayout hintoverlay;
    private Bitmap bubbleIcon;
    private Bitmap userDatingIcon;
    private Bitmap userMatrimonyIcon;
    private Bitmap userJewelryIcon;
    private Bitmap userRentalIcon;
    private Bitmap userCarsIcon;
    private Bitmap userRestaurantIcon;
    private Bitmap userMoviesIcon;
    private Bitmap datingIcon;
    private Bitmap matrimonyIcon;
    private Bitmap carsIcon;
    private Bitmap rentalIcon;
    private Bitmap jewelryIcon;
    private Bitmap moviesIcon;
    private Bitmap restaurantIcon;
    private Bitmap shadowIcon;
    private Bitmap markerIcon;
    final ArrayList<CharSequence> results = new ArrayList<CharSequence>();
    private List<MstuffLocations> peoples;
    private MapController mc;
    private MstuffLocations selectedMapLocation;
    private SQLiteDatabase myDB;
    private String selectedid;
    private String selectedcatagory;
    private int selectedlatitude;
    private int selectedlongitude;
    private int initialLatitude;
    private int initialLongitude;
    private int initialZoomLevel;
    int j = 2;
    private LinearLayout layoutoverlay;
    private AbsoluteLayout iconslayoutoverlay;
    private TextView mstufftext;
    private TextView mhinttext;
    MapView mapView;
    InitLocationListener mLocationListener;
    static final String LOG_TAG = "MapResults";

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.map);

// todo: fix me, get an apikey from google maps site       
        mapView = new MapView(this, "apisamples");
        mapView.setClickable(true);
        mapView.setEnabled(true);
        mapView.displayZoomControls(true);
        LinearLayout map = (LinearLayout) findViewById(R.id.layout_map);
        map.addView(mapView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        );
        View zoomView = mapView.getZoomControls();
        LinearLayout zoom = (LinearLayout) findViewById(R.id.layout_zoom);
        zoom.addView(zoomView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        );

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

        requestLocationUpdates();
        showMatches();
    }

    void setIcons()
    {
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

    void showMatches()
    {
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


        mc = mapView.getController();

/*
        LocationManager myLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Location location = myLocationManager.getCurrentLocation("gps");
        List<LocationProvider> provider = myLocationManager.getProviders();

//        Log.i("MapResults===========>", location.toString());
        Log.i("MapResults===========>", provider.toString());
*/

//        UserLocationOverlay myLocationOverlay = new UserLocationOverlay();
        final MyLocationOverlay myLocationOverlay = new MyLocationOverlay
                (this, mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(new Runnable()
        {
            public void run()
            {
                mapView.getController()
                        .animateTo(myLocationOverlay.getMyLocation());
            }
        });
        mapView.getOverlays().add(myLocationOverlay);
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

    void setIconListeners()
    {
        final ImageView favoriteIcon = (ImageView) findViewById(R.id.favorites);
        final ImageView ignoreIcon = (ImageView) findViewById(R.id.ignore);
        final ImageView chatIcon = (ImageView) findViewById(R.id.chat);
        final ImageView mediaIcon = (ImageView) findViewById(R.id.media);
        favoriteIcon.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {

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
                AnimationSet rootSet = new AnimationSet(true);
                rootSet.setInterpolator(new AccelerateInterpolator());
//                rootSet.setRepeatMode(Animation.NO_REPEAT);

                // Create and add first child, a motion animation.

                // rootSet.addAnimation(trans1);

                ScaleAnimation scale = new ScaleAnimation(1, 0.7f, 1, 0.7f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                scale.setDuration(200);
                scale.setFillAfter(true);
                favoriteIcon.startAnimation(scale);
                Toast.makeText(MapResults.this, "Added as favorites",
                        Toast.LENGTH_SHORT).show();
            }
        });
        ignoreIcon.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                try
                {
                    ScaleAnimation scale = new ScaleAnimation(1, 0.7f, 1, 0.7f,
                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                    scale.setDuration(200);
                    scale.setFillAfter(true);
                    ignoreIcon.startAnimation(scale);
                    myDB = openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    myDB.execSQL("UPDATE selectedlocation set latitude="
                            + selectedlatitude + ", longitude="
                            + selectedlongitude + ";");
                    myDB.delete("mStuffdetails", "mstuffid='" + selectedid
                            + "'", null);
                    Intent i = new Intent(MapResults.this,
                            MapResults.class);
                    startActivityForResult(i, 0);
                }
                finally
                {
                    if (myDB != null)
                    {
                        myDB.close();
                    }
                }
            }
        });
        chatIcon.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                ScaleAnimation scale = new ScaleAnimation(1, 0.7f, 1, 0.7f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                scale.setDuration(200);
                scale.setFillAfter(true);
                chatIcon.startAnimation(scale);
                Intent intent = new Intent(MapResults.this, Chat.class);
                Bundle b = new Bundle();
                b.putString("mstuffid", selectedid);
                intent.putExtras(b);
                startActivityForResult(intent, 0);
            }
        });
        mediaIcon.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                ScaleAnimation scale = new ScaleAnimation(1, 0.7f, 1, 0.7f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                scale.setDuration(200);
                scale.setFillAfter(true);
                mediaIcon.startAnimation(scale);
                Intent intent = new Intent(MapResults.this,
                        ViewMedia.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    protected boolean isRouteDisplayed()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<MstuffLocations> getMapLocations()
    {
        if (peoples == null)
        {
            peoples = new ArrayList<MstuffLocations>();
            myDB = null;
            try
            {
                myDB = this.openOrCreateDatabase("Mobeegal",
                        Context.MODE_PRIVATE, null);
                String cols[] = {"mstuffid", "catagory", "details",
                        "latitude", "longitude", "location"};
                Cursor c = myDB.query("mStuffdetails", cols, null, null,
                        null, null, null);
                int useridColumn = c.getColumnIndexOrThrow("mstuffid");
                int catagoryColumn = c.getColumnIndexOrThrow("catagory");
                int detailsColumn = c.getColumnIndexOrThrow("details");
                int latitudeColumn = c.getColumnIndexOrThrow("latitude");
                int longitudeColumn = c.getColumnIndexOrThrow("longitude");
                int locationColumn = c.getColumnIndexOrThrow("location");
                String details = null;
                if (c != null)
                {
                    if (c.isFirst())
                    {
                        do
                        {
                            String userid = c.getString(useridColumn);
                            String catagory = c.getString(catagoryColumn);
                            details = c.getString(detailsColumn);
                            int dblatitude = c.getInt(latitudeColumn);
                            int dblongitude = c.getInt(longitudeColumn);
                            String location1 = c.getString(locationColumn);
                            try
                            {
                                peoples.add(new MstuffLocations(userid,
                                        catagory, details, dblatitude,
                                        dblongitude, location1));
                            }
                            catch (Exception e)
                            {

                            }
                        }
                        while (c.moveToNext());
                    }
                    else
                    {
                        Toast.makeText(MapResults.this,
                                "No matches found. ", Toast.LENGTH_LONG).show();
                    }
                }
            }
            catch (Exception e)
            {
                Toast.makeText(MapResults.this,
                        " No Matches Found. Activate the service",
                        Toast.LENGTH_LONG).show();
            }
            finally
            {
                if (myDB != null)
                {
                    myDB.close();
                }
            }
        }
        return peoples;
    }

    class UserLocationOverlay
            extends Overlay
    {

        private Paint textPaint;
        private Paint borderPaint;
        private Paint innerPaint;
        private Paint innerPaint1;
        private int[] selectedIcons;
        private int count = 0;
        boolean isRemove;

        @Override
        public boolean onTap(GeoPoint p, MapView mapView)
        {

            // Store whether prior popup was displayed so we can call
            // invalidate() & remove it if necessary.
            boolean isRemovePriorPopup = selectedMapLocation != null;
            // boolean isRemove = selectedIcons != null;
            // Next test whether a new popup should be displayed
            selectedMapLocation = getHitMapLocation(p);

            if (isRemovePriorPopup || selectedMapLocation != null)
            {
                mapView.invalidate();
            }
            // Lastly return true if we handled this onTap()
            return selectedMapLocation != null;
        }

        @Override
        public void draw(Canvas canvas, MapView mapview, boolean shadow)
        {

            drawMapLocations(canvas, shadow);
            drawInfoWindow(canvas, shadow);
            GeoPoint centre = mapView.getMapCenter();
            try
            {
                myDB = openOrCreateDatabase("Mobeegal",
                        Context.MODE_PRIVATE,
                        null);
                myDB.execSQL("UPDATE selectedlocation set latitude="
                        + centre.getLatitudeE6() + ", longitude="
                        + centre.getLongitudeE6() + ";");
            }
            catch (Exception exce)
            {
            }
        }

        /**
         * Test whether an information balloon should be displayed or a prior
         * balloon hidden.
         */
        private MstuffLocations getHitMapLocation(
                GeoPoint tapPoint)
        {

            // Track which MapLocation was hit...if any
            MstuffLocations hitMapLocation = null;
            RectF hitTestRecr = new RectF();
            int[] screenCoords = new int[2];
            Iterator<MstuffLocations> iterator = getMapLocations().iterator();
            while (iterator.hasNext())
            {
                MstuffLocations testLocation = iterator.next();

                // Translate the MapLocation's lat/long coordinates to screen
                // coordinates
                final GeoPoint tp1 = testLocation.getPoint();
/*
                GeoPoint p1 = converter.pixelToRgb()
                        (tp1.getLatitudeE6(), tp1.getLongitudeE6());
                screenCoords[0] = p1.getLatitudeE6();
                screenCoords[1] = p1.getLongitudeE6();
*/

                testLocation.getLocation();
                selectedlatitude = testLocation.getLatitude();
                selectedlongitude = testLocation.getLongitude();
                selectedid = testLocation.getUserid();
                selectedcatagory = testLocation.getCatagory();
                // Create a 'hit' testing Rectangle w/size and coordinates of
                // our icon
                // Set the 'hit' testing Rectangle with the size and coordinates
                // of our on screen icon
                hitTestRecr.set(-bubbleIcon.getWidth() / 2,
                        -bubbleIcon.getHeight(),
                        bubbleIcon.getWidth() / 2, 0);
                hitTestRecr.offset(screenCoords[0], screenCoords[1]);

                // Finally test for a match between our 'hit' Rectangle and the
                // location clicked by the user
//                calculator.getPointXY(tapPoint, screenCoords);
                if (hitTestRecr.contains(screenCoords[0], screenCoords[1]))
                {
                    hitMapLocation = testLocation;
                    break;
                }
                else
                {
                    layoutoverlay.setVisibility(View.GONE);
                    iconslayoutoverlay.setVisibility(View.GONE);
                    // hintoverlay.setVisibility(View.VISIBLE);
                    hitoverlay.setVisibility(View.VISIBLE);
                }
            }
            // Lastly clear the newMouseSelection as it has now been processed
            tapPoint = null;

            return hitMapLocation;
        }

        private void drawMapLocations(Canvas canvas,
                boolean shadow)
        {

            Iterator<MstuffLocations> iterator = getMapLocations().iterator();
            int[] screenCoords = new int[2];
            while (iterator.hasNext())
            {
                MstuffLocations location = iterator.next();
//                calculator.getPointXY(location.getPoint(), screenCoords);
                String select_Category = location.getCatagory();
                if (shadow)
                {
                    // Only offset the shadow in the y-axis as the shadow is
                    // angled so the base is at x=0;
                    canvas.drawBitmap(shadowIcon, screenCoords[0],
                            screenCoords[1] - shadowIcon.getHeight(), null);
                }
                else
                {
                    // canvas.drawBitmap(matrimonyIcon, screenCoords[0] -
                    // bubbleIcon.getWidth() / 2, screenCoords[1] -
                    // bubbleIcon.getHeight(), null);
                    if (select_Category.equals("Dating"))
                    {
                        canvas.drawBitmap(datingIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("Matrimony"))
                    {
                        canvas.drawBitmap(matrimonyIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("Rental"))
                    {
                        canvas.drawBitmap(rentalIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("Restaurants"))
                    {
                        canvas.drawBitmap(restaurantIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("Jewelry"))
                    {
                        canvas.drawBitmap(jewelryIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("Movies"))
                    {
                        canvas.drawBitmap(moviesIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("Cars"))
                    {
                        canvas.drawBitmap(carsIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("Marker"))
                    {
                        canvas.drawBitmap(markerIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("userDating"))
                    {
                        canvas.drawBitmap(userDatingIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("userMatrimony"))
                    {
                        canvas.drawBitmap(userMatrimonyIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("userJewelry"))
                    {
                        canvas.drawBitmap(userJewelryIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("userCars"))
                    {
                        canvas.drawBitmap(userCarsIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("userRental"))
                    {
                        canvas.drawBitmap(userRentalIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("userRestaurant"))
                    {
                        canvas.drawBitmap(userRestaurantIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                    else if (select_Category.equals("userMovies"))
                    {
                        canvas.drawBitmap(userMoviesIcon, screenCoords[0]
                                - bubbleIcon.getWidth() / 2, screenCoords[1]
                                - bubbleIcon.getHeight(), null);
                    }
                }
            }
        }

        private void drawInfoWindow(Canvas canvas,
                boolean shadow)
        {

            if (selectedMapLocation != null)
            {
                if (shadow)
                {
                    // Skip painting a shadow in this tutorial
                }
                else
                {
                    // Toast.makeText(MapResults.this,select_Category +
                    // selectedcatagory,Toast.LENGTH_SHORT).show();
                    if (selectedcatagory.equals("Dating")
                            || selectedcatagory.equals("Matrimony")
                            || selectedcatagory.equals("Rental")
                            || selectedcatagory.equals("Restaurants")
                            || selectedcatagory.equals("Jewelry")
                            || selectedcatagory.equals("Cars"))
                    {
                        hintoverlay.setVisibility(View.GONE);
                        mhinttext.setVisibility(View.GONE);
                        mhittext.setVisibility(View.GONE);
                        RectF drawRect = new RectF();
                        drawRect.set(125, 320, 315, 368);
                        canvas.drawRoundRect(drawRect, 10, 10, getInnerPaint());
                        canvas
                                .drawRoundRect(drawRect, 10, 10,
                                        getBorderPaint());
                        iconslayoutoverlay.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                    }

                    if (selectedcatagory.equals("userDating")
                            || selectedcatagory.equals("userMatrimony")
                            || selectedcatagory.equals("userRental")
                            || selectedcatagory.equals("userRestaurants")
                            || selectedcatagory.equals("userJewelry")
                            || selectedcatagory.equals("userCars"))
                    {
                        hintoverlay.setVisibility(View.GONE);
                        mhinttext.setVisibility(View.GONE);
                        mhittext.setVisibility(View.GONE);
                        iconslayoutoverlay.setVisibility(View.GONE);
                        RectF layoutoverlay1 = new RectF();
                        // layoutoverlay1.set(03,240,120,325);
                        layoutoverlay1.set(02, 371, 317, 428);
                        canvas.drawRoundRect(layoutoverlay1, 10, 10,
                                getInnerPaint());
                        canvas.drawRoundRect(layoutoverlay1, 10, 10,
                                getBorderPaint());
                        layoutoverlay.setVisibility(View.VISIBLE);
                        int[] selDestinationOffset = new int[2];
//                        calculator.getPointXY(selectedMapLocation.getPoint(),
//                                selDestinationOffset);
                        GeoPoint point = new GeoPoint(selectedlatitude,
                                selectedlongitude);
                        mc.setCenter(point);
                        String selectedmStuffdetails = selectedMapLocation
                                .getName();
                        mstufftext.setText(selectedmStuffdetails);
                    }
                    else
                    {
                        mhinttext.setVisibility(View.GONE);
                        mhittext.setVisibility(View.GONE);
                        RectF layoutoverlay1 = new RectF();
                        // layoutoverlay1.set(03,240,120,325);
                        layoutoverlay1.set(02, 371, 317, 428);
                        canvas.drawRoundRect(layoutoverlay1, 10, 10,
                                getInnerPaint());
                        canvas.drawRoundRect(layoutoverlay1, 10, 10,
                                getBorderPaint());
                        layoutoverlay.setVisibility(View.VISIBLE);
                        // First determine the screen coordinates of the
                        // selected MapLocation
                        int[] selDestinationOffset = new int[2];
//                        calculator.getPointXY(selectedMapLocation.getPoint(),
//                                selDestinationOffset);
                        GeoPoint point = new GeoPoint(selectedlatitude,
                                selectedlongitude);
                        mc.setCenter(point);
                        String selectedmStuffdetails = selectedMapLocation
                                .getName();
                        // String detail[] = selectedmStuffdetails.split(",");
                        // String slashdetails = detail[0] + "\n" + detail[1];

                        // mstufftext.setText(selectedmStuffdetails +"\n\n\n"+
                        // getString(R.string.hint));
                        mstufftext.setText(selectedmStuffdetails);

                        // mstuffhint.setVisibility(View.VISIBLE);
                    }
                }
            }
            else
            {
                RectF hintRect = new RectF();
                hintRect.set(02, 395, 317, 428);
                canvas.drawRoundRect(hintRect, 10, 10, getInnerPaint());
                canvas.drawRoundRect(hintRect, 10, 10, getBorderPaint());
                hintoverlay.setVisibility(View.VISIBLE);
                hitoverlay.setVisibility(View.VISIBLE);
                mhinttext.setVisibility(View.VISIBLE);
                mhittext.setVisibility(View.VISIBLE);
                // mhinttext.setText(R.string.hit +"\n"+ R.id.hinttext);
            }
        }

        public Paint getInnerPaint()
        {
            if (innerPaint == null)
            {
                innerPaint = new Paint();
                innerPaint.setARGB(255, 75, 75, 75); // gray
                innerPaint.setAntiAlias(true);
            }
            return innerPaint;
        }

        public Paint getInnerPaint1()
        {
            if (innerPaint1 == null)
            {
                innerPaint1 = new Paint();
                innerPaint1.setARGB(255, 75, 75, 75); // gray
                innerPaint1.setAntiAlias(true);
            }
            return innerPaint1;
        }

        public Paint getBorderPaint()
        {

            if (borderPaint == null)
            {
                borderPaint = new Paint();
                borderPaint.setARGB(255, 255, 255, 255);
                borderPaint.setAntiAlias(true);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(2);
            }
            return borderPaint;
        }

        public Paint getTextPaint()
        {
            if (textPaint == null)
            {
                textPaint = new Paint();
                textPaint.setARGB(255, 255, 255, 0);
                textPaint.setAntiAlias(true);
            }
            return textPaint;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMapMenu(menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {

            case 1:
                Intent stuffCheckintent = new Intent(MapResults.this,
                        MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                break;
            case 2:
                Intent intent1 = new Intent(MapResults.this,
                        FindandInstall.class);
                startActivityForResult(intent1, 0);
                break;
            case 3:
                Intent settings =
                        new Intent(MapResults.this, Settings.class);
                startActivityForResult(settings, 0);
                break;

            case 4:
                myDB.execSQL("update preferences set views='TextView'");
                Intent mStuffTextView = new Intent(MapResults.this,
                        MStuffTextView.class);
                startActivity(mStuffTextView);
                break;

            case 5:
                Intent mStuffsearch = new Intent(MapResults.this,
                        MstuffSearch.class);
                startActivity(mStuffsearch);
                break;

            case 6:
                mapView.setSatellite(true);
                break;

                /*
             * case 7: Intent mediaintent = new Intent(MapResults.this,
             * Uploadmultimedia.class); startActivity(mediaintent);
             *
             * case 8: return exit();
             */
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean exit()
    {
        this.finish();
        this.setResult(0);
        return true;
    }


    public void requestLocationUpdates()
    {
        mLocationListener = new InitLocationListener();
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager
                .requestLocationUpdates(MobeegalApplication.PROVIDER_NAME,
                        3, 5000, mLocationListener);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeLocationUpdates();
    }

    void removeLocationUpdates()
    {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(mLocationListener);
    }

    class InitLocationListener
            implements LocationListener
    {

        public void onLocationChanged(android.location.Location loc)
        {
            if (loc == null)
            {
                Log.e(LOG_TAG, "location changed to null");
            }
            else
            {
                Log.d(LOG_TAG, "location changed : " + loc.toString());
                Log.d(LOG_TAG, "Location updates being received, exiting...");
//                finish();
            }
        }

        public void onStatusChanged(String s, int i, Bundle bundle)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void onStatusChanged(java.lang.String arg0, int arg1)
        {
            // ignore
        }


        public void onProviderEnabled(java.lang.String arg0)
        {
            // ignore
        }


        public void onProviderDisabled(java.lang.String arg0)
        {
            // ignore
        }
    }


}
