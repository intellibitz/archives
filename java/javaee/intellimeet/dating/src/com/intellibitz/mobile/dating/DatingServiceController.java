package com.intellibitz.mobile.dating;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import android.app.NotificationManager;

public class DatingServiceController extends Activity {

    Button butActivate;
    Button butDeactivate;
    String storedValues;
    TextView partAge;
    TextView partHeight;
    TextView partWeight;
    TextView partLocation;
    int count = 0;
    NotificationManager mNM;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.dating_controller);
        partAge = new TextView(this);
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        butActivate = (Button) findViewById(R.id.buttonActivate);
        butDeactivate = (Button) findViewById(R.id.buttonBack);
        butDeactivate.setVisibility(Button.INVISIBLE);
        butActivate.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (count == 0) {
                    startService(new Intent(DatingServiceController.this, DatingService.class), null); finish();
                    count++;
                    //butDeactivate.setEnabled(true);
                    butActivate.setVisibility(Button.INVISIBLE);
                    Intent intobject = new Intent(DatingServiceController.this,Dating.class);
            		startActivity(intobject);
                } else {
                    Toast.makeText(DatingServiceController.this, "Service already activated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        butDeactivate.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                stopService(new Intent(DatingServiceController.this, DatingService.class));
                Toast.makeText(DatingServiceController.this, "Service Deactivated", Toast.LENGTH_SHORT).show();
            }

            @SuppressWarnings("unused")
            protected void onDestroy() {
                // Cancel the persistent notification.
                mNM.cancel(R.string.local_service_started);

            }
        });

       

    }
}


