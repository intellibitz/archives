/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity.catalogs;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.common.geom.Point;
import com.mobeegal.android.R;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author Work
 */
public class LocationFinder
        extends MapActivity
{

    private MapView mMapview;
    private MapController mc;
    private Bitmap bubbleIcon;
    private TextView latitudeandlongitude;
    private int k;
    private int m;
    private ProgressDialog myProgressDialog;
    private String response;
    private Address[] addresses = null;
    private SQLiteDatabase myDatabase = null;
    String getcategory;
    String getstufftype;
    private int initialLatitude;
    private int initialLongitude;
    private int initialZoomLevel;
    private Point p;
    private String tableName;
    private String subCountryname;
    private String substring;
    int getkey;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here
        setContentView(R.layout.locationfinder);
        Bundle b = this.getIntent().getExtras();
        if (b != null)
        {
            tableName = b.getString("tablename");
            getkey = b.getInt("key");
        }
        mMapview = (MapView) findViewById(R.id.map);
        MyLocationOverlay mylocation = new MyLocationOverlay();
//        OverlayController oc = mMapview.createOverlayController();
        mc = mMapview.getController();
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String[] columnname = {"category", "stufftype"};
            Cursor cursor = myDatabase
                    .query(tableName, columnname, null, null, null, null, null);

            if (cursor != null)
            {
                if (cursor.isFirst())
                {
                    getcategory =
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow("category"));
                    getstufftype = cursor.getString(
                            cursor.getColumnIndexOrThrow("stufftype"));
                }
            }
        }
        catch (Exception e)
        {

        }
        try
        {

            String cols[] = {"latitude", "longitude", "zoomlevel"};
            Cursor c = myDatabase.query("selectedlocation", cols, null, null,
                    null, null, null);
            int latitudeColumn = c.getColumnIndexOrThrow("latitude");
            int longitudeColumn = c.getColumnIndexOrThrow("longitude");
            int zoomlevelColumn = c.getColumnIndexOrThrow("zoomlevel");
            if (c != null)
            {
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
            }
            p = new Point(initialLatitude, initialLongitude);
//            oc.add(mylocation, true);
//            mc.animateTo(p);
            mc.setZoom(initialZoomLevel);

        }
        catch (Exception ex)
        {
        }

        latitudeandlongitude = (TextView) findViewById(R.id.latandlong);

//        mc.setZoom(12);
//        Point point1 = new Point(13036036, 80270534);
//        mc.animateTo(point1);
//
//
//        oc.add(mylocation, true);

        ImageButton zoomIn = (ImageButton) findViewById(R.id.zoomin);
        zoomIn.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                int level = mMapview.getZoomLevel();
                mMapview.getController().setZoom(level + 1);
                try
                {
                    myDatabase.execSQL(
                            "UPDATE selectedlocation set zoomlevel=" +
                                    (level + 1) + ";");

                }
                catch (Exception exce)
                {
                }
            }
        });


        ImageButton zoomOut = (ImageButton) findViewById(R.id.zoomout);
        zoomOut.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                int level = mMapview.getZoomLevel();
                mMapview.getController().setZoom(level - 1);
                try
                {
                    myDatabase.execSQL(
                            "UPDATE selectedlocation set zoomlevel=" +
                                    (level - 1) + ";");

                }
                catch (Exception exce)
                {
                }
            }
        });
        bubbleIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.jewelry_icon);

        Button save = (Button) findViewById(R.id.savinglocation);
        save.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {
                if (getcategory.equals("Dating"))
                {
                    Intent locationfinder =
                            new Intent(LocationFinder.this, Dating.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Matrimony"))
                {
                    Intent locationfinder =
                            new Intent(LocationFinder.this, Matrimony.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Cars"))
                {
                    Intent locationfinder =
                            new Intent(LocationFinder.this, Cars.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Rental"))
                {
                    Intent locationfinder =
                            new Intent(LocationFinder.this, Home.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Restaurants"))
                {
                    Intent locationfinder =
                            new Intent(LocationFinder.this, Restaurants.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Movies"))
                {
                    Intent locationfinder =
                            new Intent(LocationFinder.this, Movies.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Jewelry"))
                {
                    Intent locationfinder =
                            new Intent(LocationFinder.this, Jewelry.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
            }
        });
    }

    protected boolean isRouteDisplayed()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void write()
    {
        // TODO Auto-generated method stub

        Button trackingLocation = (Button) findViewById(R.id.trackinglocation);
        trackingLocation.setOnClickListener(new OnClickListener()
        {

            //           @Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                try
                {
                    latitudeandlongitude.setText("Searching Location.........");
                    HttpClient http = new DefaultHttpClient();
/*
                    PostMethod httpPost = new PostMethod("http://ws.geonames.org/findNearbyPlaceName?lat=" + k / 1E6 + "&lng=" + m / 1E6);
                    //        PostMethod httpPost = new PostMethod("http://ws.geonames.org/findNearestAddress?lat="+k/1E6+"&lng="+m/1E6 );
                    http.executeMethod(httpPost);
*/
//                    response = httpPost.getResponseBodyAsString();
                    int a = response.indexOf("<name>");
                    String str = "<name>";
                    int b = response.indexOf("</name>");
                    int c = response.indexOf("<countryName>");
                    int d = response.indexOf("</countryName>");
                    String str1 = "<countryName>";
                    String subCountryname =
                            response.substring(c + str1.length(), d);
                    String substring = response.substring(a + str.length(), b);
                    if (getstufftype.equals("istuff"))
                    {
                        myDatabase.execSQL("UPDATE " + tableName +
                                " set iarea='" + substring + "', icountry='" +
                                subCountryname + "', icity='" + "" +
                                "', ilatitude='" + k / 1E6 + "', ilongitude='" +
                                m / 1E6 + "';");
                    }
                    else if (getstufftype.equals("ustuff"))
                    {
                        myDatabase.execSQL("UPDATE " + tableName +
                                " set uarea='" + substring + "', ucountry='" +
                                subCountryname + "', ucity='" + "" +
                                "', ulatitude='" + k / 1E6 + "', ulongitude='" +
                                m / 1E6 + "';");
                    }
                    //latitudeandlongitude.setText("Latitude:" + k / 1E6 + "\n" + "Longitude:" + m / 1E6 + "\n " + substring + "\n" + subCountryname);
                    latitudeandlongitude
                            .setText(substring + "\n" + subCountryname);
//                    Toast.makeText(LocationFinder.this, substring +", "+ subCountryname, Toast.LENGTH_LONG).show();
//                    GmmGeocoder geocoder = new GmmGeocoder(Locale.getDefault());
//                    addresses = geocoder.query(substring, GmmGeocoder.QUERY_TYPE_LOCATION, 0, 0, 180, 360);
                    for (int i = 0; i < addresses.length; i++)
                    {
                        double theta = m / 1E6 - addresses[i].getLongitude();
                        double dist = Math.sin(k / 1E6 * (Math.PI / 180.0)) *
                                Math.sin(addresses[i].getLatitude() *
                                        (Math.PI / 180.0)) + Math
                                .cos(k / 1E6 * (Math.PI / 180.0)) * Math.cos(
                                addresses[i].getLatitude() *
                                        (Math.PI / 180.0)) *
                                Math.cos(theta * (Math.PI / 180.0));
                        dist = Math.acos(dist);
                        dist = ((dist / Math.PI) * 180.0);
                        dist = dist * 60 * 1.1515;
                        dist = dist * 1.609344;
                        if (dist < 100)
                        {
                            if (getstufftype.equals("istuff"))
                            {
//                                myDatabase.execSQL("UPDATE " + tableName + " set iarea='" + substring + "," + addresses[i].getLocality() + "', icountry='" + subCountryname + "', icity='" + addresses[i].getRegion() + "', ilatitude='" + k / 1E6 + "', ilongitude='" + m / 1E6 + "';");
                            }
                            else if (getstufftype.equals("ustuff"))
                            {
//                                myDatabase.execSQL("UPDATE " + tableName + " set uarea='" + substring + "," + addresses[i].getLocality() + "', ucountry='" + subCountryname + "', ucity='" + addresses[i].getRegion() + "', ulatitude='" + k / 1E6 + "', ulongitude='" + m / 1E6 + "';");
                            }
//                            Toast.makeText(LocationFinder.this, substring + "," + addresses[i].getLocality() + "\n" + addresses[i].getRegion() + "," + addresses[i].getCountryName(), Toast.LENGTH_LONG).show();
//                            latitudeandlongitude.setText(substring + "," + addresses[i].getLocality() + "\n" + addresses[i].getRegion() + "," + addresses[i].getCountryName());
                        }
                    }
                    myDatabase.execSQL("UPDATE selectedlocation set latitude=" +
                            k + ", longitude=" + m + ";");//
                }
                catch (Exception ex)
                {
                }
            }
        });
    }

    public class MyLocationOverlay
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
        public void draw(Canvas canvas, MapView mapview, boolean shadow)
        {
            GeoPoint centre = mMapview.getMapCenter();
            k = centre.getLatitudeE6();
            m = centre.getLongitudeE6();

            canvas.drawText("longitude: " + m / 1E6 +
                    ", latitude: " + k / 1E6,
                    5, 15, getTextPaint());
            write();
            drawMapLocations(canvas, shadow);

        }

        private boolean drawMapLocations(Canvas canvas,
                boolean shadow)
        {

            if (shadow)
            {

            }
            else
            {
                int[] coords = new int[2];
                Point p = new Point(k, m);
//                calculator.getPointXY(p, coords);
                canvas.drawBitmap(bubbleIcon, coords[0] - 10,
                        coords[1] - bubbleIcon.getHeight(), null);
            }
            return false;
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

        public Paint getTextPaint()
        {
            if (textPaint == null)
            {
                textPaint = new Paint();
                textPaint.setARGB(255, 0, 0, 0);
                textPaint.setAntiAlias(true);
            }
            return textPaint;
        }
    }
}
