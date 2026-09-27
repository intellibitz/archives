package com.mobeegal.android.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.content.MstuffQuery;

public class CatalogServiceController
        extends Activity
{

    int count = 0;
    String gettingcategory;
    //public Catalogs welcome1;
    public String result1, result;

    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.catalogservice_controller);
        final Bundle bundles = this.getIntent().getExtras();
        if (bundles != null)
        {
            gettingcategory = bundles.getString("passingcategoryActivation");
        }
        ImageButton ImageButton =
                (ImageButton) findViewById(R.id.buttonActivate);
        ImageButton.setOnClickListener(mStartListener);
        ImageButton = (ImageButton) findViewById(R.id.buttonBack);
        ImageButton.setOnClickListener(mStopListener);
    }

    private OnClickListener mStartListener = new OnClickListener()
    {

        public void onClick(View v)
        {

            if (count == 0)
            {
                startService(new Intent(
                        "com.mobeegal.android.service.REMOTE_SERVICE"));
                count++;
                try
                {
                    Intent intobject1 = new Intent(
                            CatalogServiceController.this, MstuffQuery.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("passingcategory", gettingcategory);
                    intobject1.putExtras(bundle);
                    long firstTime = SystemClock.elapsedRealtime();
                    firstTime += 10 * 1000;
                    AlarmManager alarmmanager =
                            (AlarmManager) getSystemService(ALARM_SERVICE);
                    final PendingIntent pi = PendingIntent.getActivity
                            (getApplicationContext(), 0, intobject1,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmmanager
                            .setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    firstTime, 10 * 1000, pi);
                }
                catch (NullPointerException e)
                {
                }
            }
            else
            {
                Toast.makeText(CatalogServiceController.this,
                        "Service already activated", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private OnClickListener mStopListener = new OnClickListener()
    {

        public void onClick(View v)
        {
            stopService(new Intent(
                    "com.mobeegal.android.service.REMOTE_SERVICE"));
            Intent intent = new Intent(CatalogServiceController.this,
                    MstuffQuery.class);
            AlarmManager alarmmanager =
                    (AlarmManager) getSystemService(ALARM_SERVICE);
            final PendingIntent pi = PendingIntent.getActivity
                    (getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_CANCEL_CURRENT);
            alarmmanager.cancel(pi);
            finish();
        }
    };
}
