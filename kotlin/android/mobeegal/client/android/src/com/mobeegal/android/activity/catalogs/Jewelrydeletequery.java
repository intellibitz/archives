package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Jewelrydeletequery.java 14 2008-08-19 06:36:45Z muthu.ramadoss         $: Id of last commit
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

public class Jewelrydeletequery
        extends ListActivity
        implements OnItemClickListener
{

    SQLiteDatabase myDatabase = null;
    int size = 20;
    int[] jewelryId = new int[size];
    String[] iStuffitemtype = new String[size];
    String[] iStuffWeight = new String[size];
    String[] iStuffCountry = new String[size];
    String[] iStuffCity = new String[size];
    String[] iStuffArea = new String[size];

    String[] uStuffitemtype = new String[size];
    String[] uStuffWeightRange = new String[size];
    String[] uStuffCountry = new String[size];
    String[] uStuffCity = new String[size];
    String[] uStuffArea = new String[size];
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
            //String myCols[] = {"key", "iStuffitemtype", "iStuffWeight", "iStuffArea", "iStuffCity", "iStuffCountry", " uStuffitemtype", "uStuffWeightRange", " uStuffArea", " uStuffCity", " uStuffCountry", "queryStatus"};
            c = myDatabase.query("Jewelry", null, null, null, null, null, null);
            rows = c.getCount();

            int idcolumn = c.getColumnIndexOrThrow("key");
            int itemColumn = c.getColumnIndexOrThrow("ijewelry");
            int weightColumn = c.getColumnIndexOrThrow("iweight");
            int countryColumn = c.getColumnIndexOrThrow("icountry");
            int cityColumn = c.getColumnIndexOrThrow("icity");
            int areaColumn = c.getColumnIndexOrThrow("iarea");


            int uitemcolumn = c.getColumnIndexOrThrow("ujewelry");
            int uweightcolumn = c.getColumnIndexOrThrow("uweight");
            int ucountrycolumn = c.getColumnIndexOrThrow("ucountry");
            int ucitycolumn = c.getColumnIndexOrThrow("ucity");
            int uareacolumn = c.getColumnIndexOrThrow("uarea");
            int uquerystatuscolumn = c.getColumnIndexOrThrow("queryStatus");


            if (c != null)
            {
                count = 0;
                if (c.isFirst())
                {
                    do
                    {
                        int getid = c.getInt(idcolumn);
                        String getitem = c.getString(itemColumn);
                        String getiweight = c.getString(weightColumn);
                        String geticountry = c.getString(countryColumn);
                        String geticity = c.getString(cityColumn);
                        String getiarea = c.getString(areaColumn);

                        String getuitem = c.getString(uitemcolumn);
                        String getuweight = c.getString(uweightcolumn);
                        String getucountry = c.getString(ucountrycolumn);
                        String getucity = c.getString(ucitycolumn);
                        String getuarea = c.getString(uareacolumn);
                        String getquerystatus = c.getString(uquerystatuscolumn);


                        jewelryId[count] = getid;
                        iStuffitemtype[count] = getitem;
                        iStuffWeight[count] = getiweight;
                        iStuffCountry[count] = geticountry;
                        iStuffCity[count] = geticity;
                        iStuffArea[count] = getiarea;

                        uStuffitemtype[count] = getuitem;
                        uStuffWeightRange[count] = getuweight;
                        uStuffCountry[count] = getucountry;
                        uStuffCity[count] = getucity;
                        uStuffArea[count] = getuarea;
                        queryStatus[count] = getquerystatus;

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
                sv = new SpeechView(mContext, " ItemType = " +
                        iStuffitemtype[position] + " Weight = " +
                        iStuffWeight[position] + " Area = " +
                        iStuffArea[position] + " City = " +
                        iStuffCity[position] + "Country = " +
                        iStuffCountry[position],
                        " ItemType = " + uStuffitemtype[position] +
                                "  Weight = " + uStuffWeightRange[position] +
                                " Area= " + uStuffArea[position] + " City = " +
                                uStuffCity[position] + "Country= " +
                                uStuffCountry[position] + " Query Status= " +
                                queryStatus[position]);

            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iStuffitemtype[position]);
                /*  sv.setDialogue(uStuffArea[position]);*/
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
                    myDatabase = Jewelrydeletequery.this
                            .openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    myDatabase.delete("Jewelry", "key=" + jewelryId[selectedId],
                            null);
                    if (selectedId == 0 && jewelryId[0] != 0)
                    {
                        myDatabase
                                .delete("Jewelry", "key=" + jewelryId[0], null);
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(Jewelrydeletequery.this, "Error",
                            Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(Jewelrydeletequery.this,
                        Jewelrydeletequery.class);
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
//        AlertDialog.show(this, "Delete", position, " Do you want to delete the query\n", "OK", okButtonListener, "cancel", cancelButtonListener, false, null);

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
                Intent stuffCheckintent = new Intent(Jewelrydeletequery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(Jewelrydeletequery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(Jewelrydeletequery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
