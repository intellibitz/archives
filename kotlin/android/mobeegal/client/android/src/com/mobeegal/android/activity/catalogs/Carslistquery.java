package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Carslistquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss              $: Id of last commit
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

public class Carslistquery
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
    String[] ilatitude = new String[size];
    String[] ilongitude = new String[size];

    String[] umake = new String[size];
    String[] umodel = new String[size];
    String[] uyear = new String[size];
    String[] ucolor = new String[size];
    String[] ufueltype = new String[size];
    String[] uprice = new String[size];
    String[] ucountry = new String[size];
    String[] ucity = new String[size];
    String[] uarea = new String[size];

    String[] ulatitude = new String[size];
    String[] ulongitude = new String[size];
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
                        count++;
                    }
                    while (c.moveToNext());
                }
            }
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
                sv = new SpeechView(mContext, " make = " + imake[position] +
                        ", model = " + imodel[position] + ", year = " +
                        iyear[position] + ", color = " + icolor[position] +
                        ", fueltype = " + ifueltype[position] + ", price = " +
                        iprice[position] + ", country = " + icountry[position] +
                        ", city = " + icity[position] + ", area = " +
                        iarea[position],
                        "make = " + umake[position] + ", model = " +
                                umodel[position] + ", year = " +
                                uyear[position] + ", color = " +
                                ucolor[position] + ", fueltype = " +
                                ufueltype[position] + ", price = " +
                                uprice[position] + ", country = " +
                                ucountry[position] + ", city = " +
                                ucity[position] + ", area = " +
                                uarea[position]);
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
    }


    private class SpeechView
            extends LinearLayout
    {

        public SpeechView(Context context, String title, String words)
        {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            mTitle.setText("Seller Details : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("Buyer Details : " + words);
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
        Intent editCars = new Intent(Carslistquery.this, Cars.class);
        Bundle passkeyBundle = new Bundle();
        passkeyBundle.putInt("key", carsId[passkeyvalue]);
        editCars.putExtras(passkeyBundle);
        Cursor tempcarscursor = myDatabase.query("CarsPosition", null,
                "key=" + carsId[passkeyvalue], null, null, null, null);
        if (tempcarscursor != null)
        {
            if (tempcarscursor.isFirst())
            {
                int getimakeposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "imakeposition"));
                int getimodelposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "imodelposition"));
                int getiyearposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "iyearposition"));
                int geticolorposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "icolorposition"));
                int getifuel_typeposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ifuelposition"));
                int getipriceposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ipriceposition"));
                String geticity = tempcarscursor
                        .getString(
                                tempcarscursor.getColumnIndexOrThrow("icity"));
                String getiarea = tempcarscursor
                        .getString(
                                tempcarscursor.getColumnIndexOrThrow("iarea"));
                String geticountry = tempcarscursor
                        .getString(tempcarscursor.getColumnIndexOrThrow(
                                "icountry"));
                int getilatitude = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ilatitude"));
                int getilongitude = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ilongitude"));

                int getumakeposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "umakeposition"));
                int getumodelposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "umodelposition"));
                int getuyearposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "uyearposition"));
                int getucolorposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ucolorposition"));
                int getufuel_typeposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ufuelposition"));
                int getupriceposition = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "upriceposition"));
                String getucity = tempcarscursor
                        .getString(
                                tempcarscursor.getColumnIndexOrThrow("ucity"));
                String getuarea = tempcarscursor
                        .getString(
                                tempcarscursor.getColumnIndexOrThrow("uarea"));
                String getucountry = tempcarscursor
                        .getString(tempcarscursor.getColumnIndexOrThrow(
                                "ucountry"));
                int getulatitude = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ulatitude"));
                int getulongitude = tempcarscursor
                        .getInt(tempcarscursor.getColumnIndexOrThrow(
                                "ulongitude"));

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempcars" +
                        " (imakeposition NUMERIC, imodelposition NUMERIC, iyearposition NUMERIC, icolorposition NUMERIC, ifuelposition NUMERIC, ipriceposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR, umakeposition NUMERIC, umodelposition NUMERIC, uyearposition NUMERIC, ucolorposition NUMERIC, ufuelposition NUMERIC, upriceposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,ulatitude VARCHAR,ulongitude NUMERIC, category VARCHAR, stufftype VARCHAR);");
                myDatabase.execSQL(
                        "INSERT INTO tempcars (imakeposition, imodelposition,iyearposition,icolorposition, ifuelposition,ipriceposition, iarea, icity, icountry, ilatitude,ilongitude, umakeposition, umodelposition,uyearposition,ucolorposition, ufuelposition,upriceposition, uarea, ucity, ucountry,ulatitude,ulongitude, category, stufftype) VALUES (" +
                                getimakeposition + "," + getimodelposition +
                                "," + getiyearposition + "," +
                                geticolorposition + "," +
                                getifuel_typeposition + "," +
                                getipriceposition + ",'" + getiarea + "','" +
                                geticity + "','" + geticountry + "','" +
                                getilatitude + "','" + getilongitude + "'," +
                                getumakeposition + "," + getumodelposition +
                                "," + getuyearposition + "," +
                                getucolorposition + "," +
                                getufuel_typeposition + "," +
                                getupriceposition + ",'" + getuarea + "','" +
                                getucity + "','" + getucountry + "','" +
                                getulatitude + "','" + getulongitude + "','" +
                                "Cars" + "', '" + "istuff" + "');");

            }
        }
        startActivityForResult(editCars, 0);
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
                Intent stuffCheckintent = new Intent(Carslistquery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(Carslistquery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(Carslistquery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}

