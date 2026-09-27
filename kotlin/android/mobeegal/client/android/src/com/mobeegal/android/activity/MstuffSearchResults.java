package com.mobeegal.android.activity;

/*
<!--
$Id:: MstuffSearchResults.java 14 2008-08-19 06:36:45Z muthu.ramadoss        $: Id of last commit
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
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.mobeegal.android.R;
import com.mobeegal.android.model.MstuffLocations;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gunasekaran
 */
public class MstuffSearchResults
        extends MapActivity
{

    private String selectedcatalogs;
    private String searchItemString;
    private MapView mapview;
    private GeoPoint p1;
    private Bitmap bubbleIcon;
    private Bitmap bubbleIcon1;
    private Bitmap shadowIcon;
    private Bitmap datingIcon;
    private Bitmap matrimonyIcon;
    private Bitmap carsIcon;
    private Bitmap rentalIcon;
    private Bitmap jewelryIcon;
    private Bitmap moviesIcon;
    private Bitmap restaurantIcon;
    private Bitmap select_Icon;
    private String selectedcatagory;
    private Bitmap selectIcon;
    private String servicename1;
    private String res;
    final ArrayList<CharSequence> results = new ArrayList<CharSequence>();
    private ListView viewlist;
    private List<String> locationlist;
    private List<MstuffLocations> peoples;
    private String location;
    private MapController mc;
    private MstuffLocations selectedMapLocation;
    private SQLiteDatabase myDB;
    private String selectedlocation;
    private int selectedlatitude;
    private int selectedlongitude;
    private int initialLatitude;
    private int initialLongitude;
    private int initialZoomLevel;
    private String selectedid;

    @Override
    public void onCreate(Bundle icicle)
    {

        super.onCreate(icicle);
        setContentView(R.layout.mstuffsearchresults);
        mapview = (MapView) findViewById(R.id.mapSearch);
        bubbleIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.bubble);
        bubbleIcon1 = BitmapFactory
                .decodeResource(getResources(), R.drawable.bubble1);
        datingIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.dating_icon);
        matrimonyIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.matrimony_icon);
        rentalIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.rental_icon);
        moviesIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.movies_icon);
        jewelryIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.jewelry_icon);
        restaurantIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.restaurant_icon);
        carsIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.cars_icon);
        select_Icon = BitmapFactory
                .decodeResource(getResources(), R.drawable.select_icon);
        shadowIcon = BitmapFactory
                .decodeResource(getResources(), R.drawable.shadow);
        mc = mapview.getController();

        MyLocationOverlay mylocation = new MyLocationOverlay(this, mapview);
//            OverlayController oc = mapview.createOverlayController();

        myDB = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        String[] cols = {"latitude", "longitude", "zoomlevel"};
        Cursor c = myDB.query("selectedlocation", cols, null, null,
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
        p1 = new GeoPoint(initialLatitude, initialLongitude);
//            oc.add(mylocation, true);
        mc.animateTo(p1);
        mc.setZoom(initialZoomLevel);
        Bundle b = this.getIntent().getExtras();
        if (b != null)
        {
            searchItemString = b.getString("edittext");
            selectedcatalogs = b.getString("spinner");
        }

        ImageButton zoomIn = (ImageButton) findViewById(R.id.zoomin1);
        zoomIn.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                int level = mapview.getZoomLevel();
                mapview.getController().setZoom(level + 1);
                try
                {
                    myDB = openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    myDB.execSQL("UPDATE selectedlocation set zoomlevel=" +
                            (level + 1) + ";");
                }
                catch (Exception exce)
                {
                }
            }
        });


        ImageButton zoomOut = (ImageButton) findViewById(R.id.zoomout1);
        zoomOut.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                int level = mapview.getZoomLevel();
                mapview.getController().setZoom(level - 1);
                try
                {
                    myDB = openOrCreateDatabase("Mobeegal",
                            Context.MODE_PRIVATE, null);
                    myDB.execSQL("UPDATE selectedlocation set zoomlevel=" +
                            (level - 1) + ";");
                }
                catch (Exception exce)
                {
                }
            }
        });

    }

    protected boolean isRouteDisplayed()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<MstuffLocations> getMapLocations1()
    {
        if (peoples == null)
        {
            peoples = new ArrayList<MstuffLocations>();
            myDB = null;

            try
            {
                myDB = this.openOrCreateDatabase("Mobeegal",
                        Context.MODE_PRIVATE, null);
                String cols[] = {"mstuffid", "catagory", "details", "latitude",
                        "longitude", "location"};
                Cursor c = myDB.query("mStuffdetails", cols, null, null,
                        null, null, null);
                int detailsColumn = c.getColumnIndexOrThrow("details");
                int catagoryColumn = c.getColumnIndexOrThrow("catagory");
                int latitudeColumn = c.getColumnIndexOrThrow("latitude");
                int longitudeColumn = c.getColumnIndexOrThrow("longitude");
                int locationColumn = c.getColumnIndexOrThrow("location");
                int useridColumn = c.getColumnIndexOrThrow("mstuffid");
                String details = null;
                int i = 0;
                int j = 0;
                if (c != null)
                {
                    if (c.isFirst())
                    {
                        do
                        {
                            j++;
                            String userid = c.getString(useridColumn);
                            details = c.getString(detailsColumn);
                            String catagory = c.getString(catagoryColumn);
                            int dblatitude = c.getInt(latitudeColumn);
                            int dblongitude = c.getInt(longitudeColumn);
                            String location1 = c.getString(locationColumn);
                            if (details.toLowerCase()
                                    .contains(searchItemString.toLowerCase()) &&
                                    catagory.equalsIgnoreCase(selectedcatalogs))
                            {
                                peoples.add(new MstuffLocations(userid,
                                        catagory, details, dblatitude,
                                        dblongitude, location1));
                                i++;
                            }

                        }
                        while (c.moveToNext());
                        Toast.makeText(MstuffSearchResults.this,
                                i + " Matches found out of " + j + " mStuffs ",
                                Toast.LENGTH_LONG).show();

                    }
                }
            }
            catch (Exception e)
            {

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

    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsSearchMenu(menu);
        return true;
    }

    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {

            case 1:
                Intent stuffCheckintent = new Intent(MstuffSearchResults.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(MstuffSearchResults.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(MstuffSearchResults.this, Settings.class);
                startActivity(settings);
                break;
            case 4:
                Intent mStuffSearchIntent = new Intent(MstuffSearchResults.this,
                        MstuffSearch.class);
                startActivity(mStuffSearchIntent);
                break;
            case 5:
                Intent intent =
                        new Intent(MstuffSearchResults.this, Chat.class);
                Bundle b = new Bundle();
                b.putString("mstuffid", selectedid);
                intent.putExtras(b);
                startActivityForResult(intent, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

