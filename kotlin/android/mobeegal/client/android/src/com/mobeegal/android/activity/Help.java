package com.mobeegal.android.activity;

/*
<!--
$Id:: Help.java 14 2008-08-19 06:36:45Z muthu.ramadoss                       $: Id of last commit
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
import com.mobeegal.android.R;
import com.mobeegal.android.util.ViewMenu;

public class Help
        extends Activity
{

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.help);
    }

    //	MenuView
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    //	Menu Item
    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
            case 1:
//			mStuff Menu
                Intent stuffCheckintent =
                        new Intent(Help.this, MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                finish();
                break;
            case 2:
                Intent intent1 = new Intent(Help.this, FindandInstall.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 3:
                Intent settings = new Intent(Help.this, Settings.class);
                startActivity(settings);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
