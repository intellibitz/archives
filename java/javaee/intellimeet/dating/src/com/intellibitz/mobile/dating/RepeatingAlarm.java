/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intellibitz.mobile.dating;

import android.content.Context;
import android.content.Intent;
import android.content.IntentReceiver;
import android.widget.Toast;

/**
 *
 * @author jyothsna
 */
public class RepeatingAlarm extends IntentReceiver
{
    @Override
    public void onReceiveIntent(Context context, Intent intent)
    {
        Toast.makeText(context, R.string.repeating_scheduled, Toast.LENGTH_SHORT).show();
    }
}
