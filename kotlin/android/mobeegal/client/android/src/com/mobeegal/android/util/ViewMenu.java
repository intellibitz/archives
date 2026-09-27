package com.mobeegal.android.util;

/*
<!--
$Id:: ViewMenu.java 11 2008-08-13 14:01:31Z muthu.ramadoss                      $: Id of last commit
$Rev:: 11                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-13 19:31:31 +0530 (Wed, 13 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.view.Menu;
import com.mobeegal.android.R;

public class ViewMenu
{

    private ViewMenu()
    {
    }

    public static void onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, 1, 1, R.string.mstuffs);
        menu.add(0, 2, 2, R.string.catalogs);
        menu.add(0, 3, 3, R.string.settings);
    }

    public static void onCreateOptionsMapMenu(Menu menu)
    {
        menu.add(0, 1, 1, R.string.mstuffs);
        menu.add(0, 2, 2, R.string.catalogs);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.textview);
        menu.add(0, 5, 5, R.string.search);
        menu.add(0, 6, 6, R.string.satelliteview);
/*
        menu.add(0, 1, R.string.mstuffs, "My Stuff");
        menu.add(0, 2, R.string.catalogs, "Catalogs");
        menu.add(0, 3, R.string.settings, "Settings");
        menu.add(0, 4, R.string.shareimage, "Share Image");
        menu.add(0, 5, R.string.shareaudio, "Share Audio");
        menu.add(0, 6, R.string.sharevideo, "Share Video");
*/
        //       menu.add(0, 7, R.string.sharemedia);
        //       menu.add(0, 8, R.string.exits);
    }

    public static void onCreateOptionsTextMenu(Menu menu)
    {
        menu.add(0, 1, 1, R.string.mstuffs);
        menu.add(0, 2, 2, R.string.catalogs);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.favorite);
        menu.add(0, 5, 5, R.string.ignore);
        menu.add(0, 6, 6, R.string.mapview);
        menu.add(0, 7, 7, R.string.search);
        menu.add(0, 8, 8, R.string.chat);
        menu.add(0, 9, 9, R.string.sharemedia);
    }

    public static void onCreateOptionsmStuffMenu(Menu menu)
    {
        menu.add(0, 1, 1, R.string.mstuffs);
        menu.add(0, 2, 2, R.string.catalogs);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.newquery);
        menu.add(0, 5, 5, R.string.editquery);
        menu.add(0, 6, 6, R.string.delete);
        menu.add(0, 7, 7, R.string.viewquery);
        menu.add(0, 8, 8, R.string.setthemes);
        menu.add(0, 9, 9, R.string.sharemedia);
    }

    public static void onCreateOptionsViewQueryMenu(Menu menu)
    {
        menu.add(0, 1, 1, R.string.mstuffs);
        menu.add(0, 2, 2, R.string.catalogs);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.newquery);
        menu.add(0, 5, 5, R.string.done);
    }

    public static void onCreateOptionsSearchMenu(Menu menu)
    {
        menu.add(0, 1, 1, R.string.mstuffs);
        menu.add(0, 2, 2, R.string.catalogs);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.search);
        menu.add(0, 5, 5, R.string.chat);
    }

}
