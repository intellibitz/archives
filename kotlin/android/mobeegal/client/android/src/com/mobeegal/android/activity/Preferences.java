package com.mobeegal.android.activity;

/*
<!--
$Id:: Preferences.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TabHost;
import com.mobeegal.android.R;
import com.mobeegal.android.content.SendLocation;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;

/**
 * @author http://mobeegal/in
 */
public class Preferences
        extends Activity
{

    private String settimename1;
    private String res;
    private long timeid;
    private SQLiteDatabase myDatabase;
    private String serviceInterval;
    private String getservicerequestinterval;
    private String TimeInterval;
    private String getmStuffView;
    private String MStuffView;
    private String time;
    private String views;
    private String viewname1;
    private String servicename1;
    private String timename1;
    final ArrayList<String> results = new ArrayList();
    RadioButton mStuffView;
    RadioButton mStufftext;
    RadioButton servicerequestinterval;
    RadioButton servicerequestmanual;
    Spinner timeinterval;
    Spinner timeintervalLocation;
    String gettime;
    int getPosition;
    int getPositionLocation;
    Number elapsedtime;
    public String getTimeInterval = "";
    public Settings set;
    public String[] interval;
    int firstvalue;
    String secondvalue;
    String gettingcategory;
    CheckBox auto;
    CheckBox manual;
    RadioButton turnon;
    RadioButton turnoff;
    LocationManager myLocationManager;
    Location loc;
    Double lat;
    Double lng;
    long numericValue;
    String lbstatestatus = null;
    String lbservice = null;
    String lbstate = null;
    String lbtime = null;
    String lbservicestatus = null;
    String getTime;
    String timeIntervalLocation;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.preferences);
        Log.i("Preferences", "1");
        myDatabase = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        String[] col = {"views", "service", "time", "settime", "lbservice",
                "lbstate", "lbtimesettings"};
        Cursor c = myDatabase.query("Preferences", col, null, null,
                null, null, null);
        int viewname = c.getColumnIndexOrThrow("views");
        int servicename = c.getColumnIndexOrThrow("service");
        int timename = c.getColumnIndexOrThrow("time");
        int settimename = c.getColumnIndexOrThrow("settime");
        int lbservicecolumn = c.getColumnIndexOrThrow("lbservice");
        int lbstatecolumn = c.getColumnIndexOrThrow("lbstate");
        int lbtimecolumn = c.getColumnIndexOrThrow("lbtimesettings");
        Log.i("Preferences", "2");
        if (c != null)
        {
            if (c.isFirst())
            {
                do
                {
                    viewname1 = c.getString(viewname);
                    servicename1 = c.getString(servicename);
                    timename1 = c.getString(timename);
                    settimename1 = c.getString(settimename);
                    lbservice = c.getString(lbservicecolumn);
                    lbstate = c.getString(lbstatecolumn);
                    lbtime = c.getString(lbtimecolumn);
                    results.add(viewname1);
                    results.add(servicename1);
                    results.add(timename1);
                    results.add(settimename1);
                }
                while (c.moveToNext());
            }
        }
        Log.i("Preferences", "3");
        res = results.toString();
        c.close();
        Log.i("Preferences", "4");
        TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();

        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.mStuffView);
        one.setIndicator("mStuffView");
        tabs.addTab(one);
        mStuffView = (RadioButton) findViewById(R.id.mapviewradiobutton);
        mStufftext = (RadioButton) findViewById(R.id.textviewradiobutton);

        if (viewname1.toString().equals("MapView"))
        {
            mStuffView.setChecked(true);
            mStufftext.setChecked(false);
        }
        else
        {
            mStufftext.setChecked(true);
            mStuffView.setChecked(false);
        }
        Log.i("Preferences", "5");
        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.servicerequestinterval);
        two.setIndicator("Service request interval");
        tabs.addTab(two);

        timeinterval = (Spinner) findViewById(R.id.timeinterval);
        timeinterval.setVisibility(View.INVISIBLE);
        servicerequestinterval =
                (RadioButton) findViewById(R.id.autoradiobutton);
        servicerequestmanual =
                (RadioButton) findViewById(R.id.manualradiobutton);


        Log.i("Preferences", "6");
        TabHost.TabSpec locationbroadcasting =
                tabs.newTabSpec("Location broadcasting");
        locationbroadcasting.setContent(R.id.Locationbroadcasting);
        locationbroadcasting.setIndicator("Location broadcasting");

        myLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        loc = myLocationManager.getLastKnownLocation("gps");
        lat = loc.getLatitude() * 1E6;
        lng = loc.getLongitude() * 1E6;
        timeintervalLocation =
                (Spinner) findViewById(R.id.timeintervallocation);

        ArrayAdapter adapterLocation = ArrayAdapter.createFromResource(
                this, R.array.timeperiod, android.R.layout.simple_spinner_item);
        timeintervalLocation.setAdapter(adapterLocation);
        int getpos = Integer.parseInt(lbtime);
        timeintervalLocation.setSelection(getpos);
        turnon = (RadioButton) findViewById(R.id.turnonradiobutton);
        turnoff = (RadioButton) findViewById(R.id.turnoffradiobutton);
        auto = (CheckBox) findViewById(R.id.auto);
        manual = (CheckBox) findViewById(R.id.manual);
        auto.setVisibility(CheckBox.INVISIBLE);
        manual.setVisibility(CheckBox.INVISIBLE);
        timeintervalLocation.setVisibility(Spinner.INVISIBLE);
        Log.i("Preferences", "7");
        turnon.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                if (turnon.isChecked())
                {
                    lbservicestatus = "on";
                    auto.setVisibility(CheckBox.VISIBLE);
                    manual.setVisibility(CheckBox.VISIBLE);
                }
                else
                {
                    auto.setVisibility(CheckBox.INVISIBLE);
                    manual.setVisibility(CheckBox.INVISIBLE);
                }
                manual.setOnCheckedChangeListener(new OnCheckedChangeListener()
                {

                    public void onCheckedChanged(CompoundButton arg0,
                            boolean arg1)
                    {
                        if (manual.isChecked())
                        {
                            auto.setChecked(false);
                            lbstatestatus = "manual";
                            timeintervalLocation.setVisibility(Spinner.VISIBLE);
                        }
                        else
                        {
                            timeintervalLocation
                                    .setVisibility(Spinner.INVISIBLE);
                        }
                    }
                });
            }
        });

        turnoff.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                if (turnoff.isChecked())
                {
                    lbservicestatus = "off";
                    lbstatestatus = "auto";
                    auto.setVisibility(CheckBox.INVISIBLE);
                    manual.setVisibility(CheckBox.INVISIBLE);
                    timeintervalLocation.setVisibility(Spinner.INVISIBLE);
                }
            }
        });
        Log.i("Preferences", "9");
        auto.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                if (auto.isChecked())
                {
                    manual.setChecked(false);
                    timeintervalLocation.setVisibility(Spinner.INVISIBLE);
                    lbstatestatus = "auto";
                }
            }
        });
        tabs.addTab(locationbroadcasting);
        if (servicename1.toString().equals("Auto"))
        {
            servicerequestinterval.setChecked(true);
            servicerequestmanual.setChecked(false);
            timeinterval = (Spinner) findViewById(R.id.timeinterval);
            timeinterval.setVisibility(View.INVISIBLE);
        }
        else
        {
            servicerequestmanual.setChecked(true);
            servicerequestinterval.setChecked(false);
            timeinterval = (Spinner) findViewById(R.id.timeinterval);
            timeinterval.setVisibility(View.VISIBLE);
        }

        if (lbservice.equals("off"))
        {
            turnoff.setChecked(true);
            auto.setVisibility(CheckBox.INVISIBLE);
            manual.setVisibility(CheckBox.INVISIBLE);
            timeintervalLocation.setVisibility(Spinner.INVISIBLE);
        }
        else if (lbservice.equals("on"))
        {
            turnon.setChecked(true);
            auto.setVisibility(CheckBox.VISIBLE);
            manual.setVisibility(CheckBox.VISIBLE);
            if (lbstate.equals("manual"))
            {
                manual.setChecked(true);
                timeintervalLocation.setVisibility(Spinner.VISIBLE);
            }
            else if (lbstate.equals("auto"))
            {
                auto.setChecked(true);
                timeintervalLocation.setVisibility(Spinner.INVISIBLE);
            }
        }
        Log.i("Preferences", "10");
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
                this, R.array.timeperiod, android.R.layout.simple_spinner_item);
        timeinterval.setAdapter(adapter1);
        int pos = Integer.parseInt(timename1);
        timeinterval.setSelection(pos);
        timeinterval.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {

                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final RadioButton manualRadiobutton =
                (RadioButton) findViewById(R.id.manualradiobutton);
        if (manualRadiobutton.isEnabled())
        {
            manualRadiobutton
                    .setOnClickListener(new RadioButton.OnClickListener()
                    {

                        public void onClick(View arg0)
                        {
                            timeinterval.setVisibility(View.VISIBLE);
                        }
                    });
        }
        Log.i("Preferences", "11");
        final RadioButton autoRadiobutton =
                (RadioButton) findViewById(R.id.autoradiobutton);
        if (autoRadiobutton.isEnabled())
        {
            autoRadiobutton.setOnClickListener(new RadioButton.OnClickListener()
            {
                // default setting 1 minute and getting mstuff from server
                public void onClick(View arg0)
                {
                    timeinterval.setVisibility(View.INVISIBLE);
                }
            });
        }
        tabs.setCurrentTab(0);

        final long startTime = SystemClock.elapsedRealtime();
        final Button savebutton = (Button) findViewById(R.id.save);
        savebutton.setOnClickListener(new Button.OnClickListener()
        {

            private String getsettime;

            public void onClick(View v)
            {
                Log.i("Preferences", "12");
                String latitudeString = Integer.toString(lat.intValue());
                String longitudeString = Integer.toString(lng.intValue());
                Log.i("Preferences", "13");
                getTime = (String) timeintervalLocation.getSelectedItem();
                String[] strArray = getTime.split(" ");
                numericValue = Long.parseLong(strArray[0]);
                if (turnoff.isChecked())
                {
                    AlarmManager alarmmanager =
                            (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent =
                            new Intent(Preferences.this, SendLocation.class);
                    final PendingIntent pi = PendingIntent.getActivity
                            (getApplicationContext(), 0, intent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmmanager.cancel(pi);
                }
                if (lbstatestatus.equals("auto") &&
                        lbservicestatus.equals("on"))
                {
                    Intent intobject2 =
                            new Intent(Preferences.this, SendLocation.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("latitude", latitudeString);
                    bundle.putString("longitude", longitudeString);
                    intobject2.putExtras(bundle);
                    AlarmManager alarmmanager =
                            (AlarmManager) getSystemService(ALARM_SERVICE);
                    final PendingIntent pi = PendingIntent.getActivity
                            (getApplicationContext(), 0, intobject2,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmmanager.setRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime,
                            60 * 1000, pi);
                }
                if (lbstatestatus.equals("manual") &&
                        lbservicestatus.equals("on"))
                {
                    if (strArray[1].equals("Minutes"))
                    {
                        Intent intobject2 = new Intent(Preferences.this,
                                SendLocation.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("latitude", latitudeString);
                        bundle.putString("longitude", longitudeString);
                        intobject2.putExtras(bundle);
                        AlarmManager alarmmanager =
                                (AlarmManager) getSystemService(ALARM_SERVICE);
                        final PendingIntent pi = PendingIntent.getActivity
                                (getApplicationContext(), 0, intobject2,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        alarmmanager.setRepeating(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                startTime, numericValue * 60 * 1000,
                                pi);
                    }
                    else if (strArray[1].equals("Hours"))
                    {
                        Intent intobject2 = new Intent(Preferences.this,
                                SendLocation.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("latitude", latitudeString);
                        bundle.putString("longitude", longitudeString);
                        intobject2.putExtras(bundle);
                        AlarmManager alarmmanager =
                                (AlarmManager) getSystemService(ALARM_SERVICE);
                        final PendingIntent pi = PendingIntent.getActivity
                                (getApplicationContext(), 0, intobject2,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        alarmmanager.setRepeating(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                startTime, numericValue * 60 * 60 * 1000,
                                pi);
                    }
                    else if (strArray[1].equals("Day"))
                    {
                        Intent intobject2 = new Intent(Preferences.this,
                                SendLocation.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("latitude", latitudeString);
                        bundle.putString("longitude", longitudeString);
                        intobject2.putExtras(bundle);
                        AlarmManager alarmmanager =
                                (AlarmManager) getSystemService(ALARM_SERVICE);
                        final PendingIntent pi = PendingIntent.getActivity
                                (getApplicationContext(), 0, intobject2,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        alarmmanager.setRepeating(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                startTime, numericValue * 24 * 60 * 60 * 1000,
                                pi);
                    }
                }
                // Based on the time interval sending request to server for matchin stuffs
                mStuffView =
                        (RadioButton) findViewById(R.id.mapviewradiobutton);
                getPosition = timeinterval.getSelectedItemPosition();
                getPositionLocation =
                        timeintervalLocation.getSelectedItemPosition();
                timeIntervalLocation = Integer.toString(getPositionLocation);
                TimeInterval = Integer.toString(getPosition);
                getsettime = (String) timeinterval.getSelectedItem();
                if (mStuffView.isChecked())
                {
                    getmStuffView = "MapView";
                }
                else
                {
                    getmStuffView = "TextView";
                }
                servicerequestinterval =
                        (RadioButton) findViewById(R.id.autoradiobutton);
                if (servicerequestinterval.isChecked())
                {
                    getservicerequestinterval = "Auto";
                }
                else
                {
                    getservicerequestinterval = "Manual";
                }
                myDatabase.execSQL("UPDATE Preferences set views='" +
                        getmStuffView + "',service='" +
                        getservicerequestinterval + "',time='" + TimeInterval +
                        "',settime='" + getsettime + "',lbservice='" +
                        lbservicestatus + "',lbstate='" + lbstatestatus +
                        "',lbtimesettings='" + timeIntervalLocation +
                        "' where preference=preference");
                finish();
            }
        });
    }

    // MenuView
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    //  Menu Item
    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
//  mStuff Menu
            case 1:
                Intent stuffCheckintent =
                        new Intent(Preferences.this, MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                finish();
                break;
            case 2:
                Intent intent1 =
                        new Intent(Preferences.this, FindandInstall.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 3:
                Intent settings = new Intent(Preferences.this, Settings.class);
                startActivity(settings);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
