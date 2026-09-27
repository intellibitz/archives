package com.mobeegal.android.activity;

/*
<!--
$Id:: Settings.java 14 2008-08-19 06:36:45Z muthu.ramadoss                   $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.mobeegal.android.R;
import com.mobeegal.android.util.ViewMenu;

public class Settings
        extends Activity
{

    Button activateButton1;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.settings);
        ImageButton activate =
                (ImageButton) findViewById(R.id.activate_deactivate);
        ImageButton preference = (ImageButton) findViewById(R.id.preference);
        ImageButton helpsettings =
                (ImageButton) findViewById(R.id.helpsettings);
        //Button activateButton1 = (Button) findViewById(R.id.buttonActivate);
        activate.setOnClickListener(new ImageButton.OnClickListener()
        {

            //@Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                //	activateButton1.setEnabled(false);
                Intent intent = new Intent(Settings.this,
                        ServiceActivateDeactivate.class);
                startActivityForResult(intent, 0);
            }
        });
        preference.setOnClickListener(new ImageButton.OnClickListener()
        {

            public void onClick(View arg0)
            {
                Intent intent = new Intent(Settings.this, Preferences.class);
                startActivityForResult(intent, 0);
            }
        });
        helpsettings.setOnClickListener(new ImageButton.OnClickListener()
        {

            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                //	activateButton1.setEnabled(false);
                Intent intent = new Intent(Settings.this, Help.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    //	MenuView
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    //Menu Item
    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
//		mStuff Menu
            case 1:
                Intent stuffCheckintent =
                        new Intent(Settings.this, MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                finish();
                break;
            case 2:
                Intent intent1 =
                        new Intent(Settings.this, FindandInstall.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 3:
                Intent settings = new Intent(Settings.this, Settings.class);
                startActivityForResult(settings, 0);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
