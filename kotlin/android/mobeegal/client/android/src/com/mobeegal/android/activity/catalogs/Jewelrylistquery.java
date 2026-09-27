package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Jewelrylistquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.app.ListActivity;
import android.content.Context;
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
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;

public class Jewelrylistquery
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
                        ;
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
                        iStuffWeight[position] + "Area = " +
                        iStuffArea[position] + " City = " +
                        iStuffCity[position] + "Country= " +
                        iStuffCountry[position],
                        " ItemType = " + uStuffitemtype[position] +
                                " Weight = " + uStuffWeightRange[position] +
                                " Area= " + uStuffArea[position] + " City = " +
                                uStuffCity[position] + " Country = " +
                                uStuffCountry[position]);

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
            mTitle.setText("Merchant Detail : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("Customer Detail : " + words);
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
        String selectid = parent.getItemAtPosition(position).toString();
        int passkeyvalue = Integer.parseInt(selectid);
        Intent editJewelry = new Intent(Jewelrylistquery.this, Jewelry.class);
        Bundle passkeyBundle = new Bundle();
        passkeyBundle.putInt("key", jewelryId[passkeyvalue]);
        editJewelry.putExtras(passkeyBundle);
        Cursor tempdatingcursor = myDatabase.query("JewelryPosition", null,
                "key=" + jewelryId[passkeyvalue], null, null, null, null);
        if (tempdatingcursor != null)
        {
            if (tempdatingcursor.isFirst())
            {
                int getijewelryposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ijewelryposition"));
                int getigenderposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "igenderposition"));
                int getistoneposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "istoneposition"));
                int getimetalposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "imetalposition"));
                int getiweightposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "iweightposition"));

                int getujewelryposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ujewelryposition"));
                int getugenderposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ugenderposition"));
                int getustoneposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ustoneposition"));
                int getumetalposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "umetalposition"));
                int getuweightposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "uweightposition"));

                String getiarea = tempdatingcursor
                        .getString(tempdatingcursor.getColumnIndexOrThrow(
                                "iarea"));
                String geticity = tempdatingcursor
                        .getString(tempdatingcursor.getColumnIndexOrThrow(
                                "icity"));
                String geticountry = tempdatingcursor
                        .getString(tempdatingcursor.getColumnIndexOrThrow(
                                "icountry"));
                String getuarea = tempdatingcursor
                        .getString(tempdatingcursor.getColumnIndexOrThrow(
                                "uarea"));
                String getucity = tempdatingcursor
                        .getString(tempdatingcursor.getColumnIndexOrThrow(
                                "ucity"));
                String getucountry = tempdatingcursor
                        .getString(tempdatingcursor.getColumnIndexOrThrow(
                                "ucountry"));
                String getilatitude = tempdatingcursor.getString(
                        tempdatingcursor.getColumnIndexOrThrow("ilatitude"));
                String getilongitude = tempdatingcursor.getString(
                        tempdatingcursor.getColumnIndexOrThrow("ilongitude"));
                String getulatitude = tempdatingcursor.getString(
                        tempdatingcursor.getColumnIndexOrThrow("ulatitude"));
                String getulongitude = tempdatingcursor.getString(
                        tempdatingcursor.getColumnIndexOrThrow("ulongitude"));
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempjewelry" +
                        "(ijewelryposition NUMERIC, igenderposition NUMERIC, istoneposition NUMERIC, imetalposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ujewelryposition NUMERIC, ugenderposition NUMERIC, ustoneposition NUMERIC, umetalposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
                myDatabase.execSQL(
                        "INSERT INTO tempjewelry (ijewelryposition, igenderposition, istoneposition, imetalposition, iweightposition, iarea, icity,icountry, ujewelryposition, ugenderposition, ustoneposition, umetalposition, uweightposition, uarea, ucity, ucountry, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                getijewelryposition + "," + getigenderposition +
                                "," + getistoneposition + "," +
                                getimetalposition + "," + getiweightposition +
                                ",'" + getiarea + "','" + geticity + "','" +
                                geticountry + "'," + getujewelryposition + "," +
                                getugenderposition + "," + getustoneposition +
                                "," + getumetalposition + "," +
                                getuweightposition + ",'" + getuarea + "','" +
                                getucity + "','" + getucountry + "','" +
                                getilatitude + "','" + getilongitude + "','" +
                                getulatitude + "','" + getulongitude + "','" +
                                "Jewelry" + "','" + "istuff" + "');");
            }
        }
        startActivityForResult(editJewelry, 0);
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
                Intent stuffCheckintent = new Intent(Jewelrylistquery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(Jewelrylistquery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(Jewelrylistquery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
