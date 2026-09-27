package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Carsdeletequery.java 14 2008-08-19 06:36:45Z muthu.ramadoss            $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;

public class Carsdeletequery
        extends ListActivity
        implements OnItemClickListener
{

    SQLiteDatabase myDatabase = null;
    int size = 20;
    int[] carsId = new int[size];
    String[] imake = new String[size];
    String[] imodel = new String[size];
    String[] iyear = new String[size];
    String[] icolor = new String[size];
    String[] ifueltype = new String[size];
    String[] iprice = new String[size];
    String[] icountry = new String[size];
    String[] icity = new String[size];
    String[] iarea = new String[size];

    String[] umake = new String[size];
    String[] umodel = new String[size];
    String[] uyear = new String[size];
    String[] ucolor = new String[size];
    String[] ufueltype = new String[size];
    String[] uprice = new String[size];
    //String[] uStuffcartype = new String[size];
    String[] ucountry = new String[size];
    String[] ucity = new String[size];
    String[] uarea = new String[size];
    String[] queryStatus = new String[size];

    int count = 0;
    Cursor c;
    int rows = 0;
    ArrayList<String> results = new ArrayList<String>();

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setListAdapter(new SpeechListAdapter(this));
        getListView().setOnItemClickListener(this);
    }

    private class SpeechListAdapter
            extends BaseAdapter
    {

        public SpeechListAdapter(Context context)
        {
            mContext = context;

            myDatabase = mContext.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String myCols[] = {"key", "imake", "imodel", "iyear", "icolor",
                    "ifuel_type", "iprice", "icountry", "icity", "iarea",
                    "umake", "umodel", "uyear", "ucolor", "ufuel_type",
                    "uprice", " ucountry", "ucity", "uarea", "queryStatus"};
            c = myDatabase.query("Cars", myCols, null, null, null, null, null);
            rows = c.getCount();

            int idcolumn = c.getColumnIndexOrThrow("key");
            int imakeColumn = c.getColumnIndexOrThrow("imake");
            int imodelColumn = c.getColumnIndexOrThrow("imodel");
            int iyearColumn = c.getColumnIndexOrThrow("iyear");
            int icolorColumn = c.getColumnIndexOrThrow("icolor");
            int ifueltypeColumn = c.getColumnIndexOrThrow("ifuel_type");
            int ipriceColumn = c.getColumnIndexOrThrow("iprice");
            int icountryColumn = c.getColumnIndexOrThrow("icountry");
            int icityColumn = c.getColumnIndexOrThrow("icity");
            int iareaColumn = c.getColumnIndexOrThrow("iarea");


            int umakeColumn = c.getColumnIndexOrThrow("umake");
            int umodelColumn = c.getColumnIndexOrThrow("umodel");
            int uyearColumn = c.getColumnIndexOrThrow("uyear");
            int ucolorColumn = c.getColumnIndexOrThrow("ucolor");
            int ufueltypeColumn = c.getColumnIndexOrThrow("ufuel_type");
            int upriceColumn = c.getColumnIndexOrThrow("uprice");
            int ucountryColumn = c.getColumnIndexOrThrow("ucountry");
            int ucityColumn = c.getColumnIndexOrThrow("ucity");
            int uareaColumn = c.getColumnIndexOrThrow("uarea");
            int uquerystatuscolumn = c.getColumnIndexOrThrow("queryStatus");


            if (c != null)
            {
                count = 0;
                if (c.isFirst())
                {
                    do
                    {
                        int getid = c.getInt(idcolumn);
                        String getimake = c.getString(imakeColumn);
                        String getimodel = c.getString(imodelColumn);
                        String getiyear = c.getString(iyearColumn);
                        String geticolor = c.getString(icolorColumn);
                        String getifueltype = c.getString(ifueltypeColumn);
                        String getiprice = c.getString(ipriceColumn);
                        String geticountry = c.getString(icountryColumn);
                        String geticity = c.getString(icityColumn);
                        String getiarea = c.getString(iareaColumn);


                        String getumake = c.getString(umakeColumn);
                        String getumodel = c.getString(umodelColumn);
                        String getuyear = c.getString(uyearColumn);
                        String getucolor = c.getString(ucolorColumn);
                        String getufueltype = c.getString(ufueltypeColumn);
                        String getuprice = c.getString(upriceColumn);
                        String getucountry = c.getString(ucountryColumn);
                        String getucity = c.getString(ucityColumn);
                        String getuarea = c.getString(uareaColumn);
                        String getuquerystatus =
                                c.getString(uquerystatuscolumn);


                        carsId[count] = getid;
                        imake[count] = getimake;
                        imodel[count] = getimodel;
                        iyear[count] = getiyear;
                        icolor[count] = geticolor;
                        ifueltype[count] = getifueltype;
                        iprice[count] = getiprice;
                        icountry[count] = geticountry;
                        icity[count] = geticity;
                        iarea[count] = getiarea;

                        umake[count] = getumake;
                        umodel[count] = getumodel;
                        uyear[count] = getuyear;
                        ucolor[count] = getucolor;
                        ufueltype[count] = getufueltype;
                        uprice[count] = getuprice;
                        ucountry[count] = getucountry;
                        ucity[count] = getucity;
                        uarea[count] = getuarea;
                        queryStatus[count] = getuquerystatus;

                        count++;
                    }
                    while (c.moveToNext());
                }
            }
//                if (c1 != null) {
//                    count = 0;
//                    if (c1.isFirst()) {
//                        do {
//
//
//                            //results.add("IStuff :" + iStuffarea[count] + "UStuff :" + uStuffarea[count]);
//                            count++;
//                        } while (c1.moveToNext());
//                    }
//                }
        }

        public int getCount()
        {
            return rows;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            SpeechView sv;
            if (convertView == null)
            {
                sv = new SpeechView(mContext,
                        " imake = " + imake[position] + "imodel = " +
                                imodel[position] + "iyear = " +
                                iyear[position] + "icolor = " +
                                icolor[position] + "ifueltype = " +
                                ifueltype[position] + "iprice = " +
                                iprice[position] + " country = " +
                                icountry[position] + " city = " +
                                icity[position] + " area = " + iarea[position],
                        " imake = " + umake[position] + "umodel = " +
                                umodel[position] + "uyear = " +
                                uyear[position] + "ucolor = " +
                                ucolor[position] + "ufueltype = " +
                                ufueltype[position] + "uprice = " +
                                uprice[position] + " country = " +
                                ucountry[position] + " city = " +
                                ucity[position] + " area = " + uarea[position] +
                                " QueryStatus = " + queryStatus[position]);

            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(imake[position]);
                sv.setDialogue(uarea[position]);
            }
            return sv;
        }

        private Context mContext;

        public void onItemClick(AdapterView arg0, View arg1, int arg2,
                long arg3)
        {
//          Toast.makeText(mContext, "asdasdsa", Toast.LENGTH_LONG).show();
        }
    }

    private class SpeechView
            extends LinearLayout
    {

        public SpeechView(Context context, String title, String words)
        {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            mTitle.setText("IStuff : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("UStuff : " + words);
            addView(mDialogue, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        public void setTitle(String title)
        {
            mTitle.setText(title);
        }

        public void setDialogue(String words)
        {
            mDialogue.setText(words);
        }

        private TextView mTitle;
        private TextView mDialogue;
    }

    public void onItemClick(AdapterView parent, View v, int position, long id)
    {
        final String selectid = parent.getItemAtPosition(position).toString();
        final int selectedId = Integer.parseInt(selectid);
        if (rows > 0)
        {
            myDatabase.execSQL("update category set querystatus='" + "true" +
                    "' where status='" + "true" + "';");
        }
        else
        {
            myDatabase.execSQL("update category set querystatus='" + "false" +
                    "' where status='" + "true" + "';");
        }
        OnClickListener okButtonListener = new OnClickListener()
        {

            public void onClick(DialogInterface arg0, int arg1)
            {
                try
                {
                    myDatabase = Carsdeletequery.this
                            .openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    myDatabase
                            .delete("Cars", "key=" + carsId[selectedId], null);
                    if (selectedId == 0 && carsId[0] != 0)
                    {
                        myDatabase.delete("Cars", "key=" + carsId[0], null);
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(Carsdeletequery.this, "Error",
                            Toast.LENGTH_LONG).show();
                }
                Intent intent =
                        new Intent(Carsdeletequery.this, Carsdeletequery.class);
                startActivity(intent);
                finish();

            }
        };
        OnClickListener cancelButtonListener = new OnClickListener()
        {
            // @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                // Do nothing
            }
        };
/*
        AlertDialog.show(this, "Delete", position,
                " Do you want to delete the query\n", "OK", okButtonListener,
                "cancel", cancelButtonListener, false, null);
*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    // Menu Item
    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
            case 1:
                Intent stuffCheckintent = new Intent(Carsdeletequery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(Carsdeletequery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(Carsdeletequery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
