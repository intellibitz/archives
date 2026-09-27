/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import com.mobeegal.android.R;

/**
 * @author jyothsna
 */
public class DisplayingMStuff
        extends Activity
{
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(R.string.notification_message);
        finish();
    }
}

