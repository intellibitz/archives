/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import com.mobeegal.android.content.MstuffQuery;

import java.util.ArrayList;

/**
 * @author jyothsna
 */
public class TimeSettings
        extends Activity
{

    private String querystatusString;
    private String secondvalue;
    private int firstvalue;
    private String[] interval;
    private String getTimeInterval;
    private String res;
    private String gettime1;
    private String timename1;
    private String servicename2;
    private String viewname1;
    private SQLiteDatabase myDatabase;
    ArrayList results = new ArrayList();
    private Handler mHandler = new Handler();
    Intent intobject2;
    long firstTime2;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String[] col = {"views", "service", "time", "settime"};
            Cursor c1 = myDatabase.query("Preferences", col, null, null,
                    null, null, null);
            int viewnamepref = c1.getColumnIndexOrThrow("views");
            int servicenamepref = c1.getColumnIndexOrThrow("service");
            int timenamepref = c1.getColumnIndexOrThrow("time");
            int gettimepref = c1.getColumnIndexOrThrow("settime");
            if (c1 != null)
            {
                if (c1.isFirst())
                {
                    do
                    {
                        viewname1 = c1.getString(viewnamepref);
                        servicename2 = c1.getString(servicenamepref);
                        timename1 = c1.getString(timenamepref);
                        gettime1 = c1.getString(gettimepref);
                        results.add(viewname1);
                        results.add(servicename2);
                        results.add(timename1);
                        results.add(gettime1);
                    }
                    while (c1.moveToNext());
                }
            }
            res = results.toString();
        }
        catch (NullPointerException ne)
        {
        }
        try
        {
            getTimeInterval = gettime1;
            Log.i("gettime", getTimeInterval);
// splitting the time
            interval = getTimeInterval.split(" ");
            if (interval != null)
            {
                firstvalue = Integer.parseInt(interval[0]);
                secondvalue = interval[1].toString();
            }
            if ((interval != null) && (servicename2.equals("Auto") &&
                    (secondvalue.equals("Seconds"))))
            {

                intobject2 = new Intent(TimeSettings.this, MstuffQuery.class);
                firstTime2 = SystemClock.elapsedRealtime();
                firstTime2 += 10 * 1000;
                final PendingIntent pi = PendingIntent.getActivity
                        (getApplicationContext(), 0, intobject2,
                                PendingIntent.FLAG_CANCEL_CURRENT);
                mHandler.post(new Runnable()
                {

                    public void run()
                    {
                        AlarmManager alarmmanager =
                                (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmmanager.setRepeating(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                firstTime2, 10 * 1000, pi);
                    }
                });
            }
            if ((interval != null) && (servicename2.equals("Manual")))
            {
                startService(new Intent(
                        "com.mobeegal.android.service.REMOTE_SERVICE"));
                if (secondvalue.equals("Minutes"))
                {
                    intobject2 =
                            new Intent(TimeSettings.this, MstuffQuery.class);
                    firstTime2 = SystemClock.elapsedRealtime();
                    firstTime2 += firstvalue * 60 * 1000;
                    final PendingIntent pi = PendingIntent.getActivity
                            (getApplicationContext(), 0, intobject2,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    mHandler.post(new Runnable()
                    {

                        public void run()
                        {
                            AlarmManager alarmmanager =
                                    (AlarmManager) getSystemService(
                                            ALARM_SERVICE);
                            alarmmanager.setRepeating(
                                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    firstTime2, firstvalue * 60 * 1000,
                                    pi);
                        }
                    });
                }
                else if (secondvalue.equals("Hours"))
                {
                    intobject2 =
                            new Intent(TimeSettings.this, MstuffQuery.class);
                    firstTime2 = SystemClock.elapsedRealtime();
                    firstTime2 += firstvalue * 60 * 60 * 1000;
                    final PendingIntent pi = PendingIntent.getActivity
                            (getApplicationContext(), 0, intobject2,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    mHandler.post(new Runnable()
                    {

                        public void run()
                        {
                            AlarmManager alarmmanager =
                                    (AlarmManager) getSystemService(
                                            ALARM_SERVICE);
                            alarmmanager.setRepeating(
                                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    firstTime2, firstvalue * 60 * 60 * 1000,
                                    pi);
                        }
                    });
                }
                else if (secondvalue.equals("Day"))
                {
                    intobject2 =
                            new Intent(TimeSettings.this, MstuffQuery.class);
                    firstTime2 = SystemClock.elapsedRealtime();
                    firstTime2 += firstvalue * 24 * 60 * 60 * 1000;
                    final PendingIntent pi = PendingIntent.getActivity
                            (getApplicationContext(), 0, intobject2,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    mHandler.post(new Runnable()
                    {

                        public void run()
                        {
                            AlarmManager alarmmanager =
                                    (AlarmManager) getSystemService(
                                            ALARM_SERVICE);
                            alarmmanager.setRepeating(
                                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    firstTime2,
                                    firstvalue * 24 * 60 * 60 * 1000,
                                    pi);
                        }
                    });
                }
            }
        }
        catch (NullPointerException e)
        {
            Log.i("gettime1", getTimeInterval);

        }
        catch (ArrayIndexOutOfBoundsException ae)
        {
        }
        Intent intentObj = new Intent(TimeSettings.this, Settings.class);
        startActivityForResult(intentObj, 0);
        finish();
    }
}
