package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Matrimonylistquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss         $: Id of last commit
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

public class Matrimonylistquery
        extends ListActivity
        implements OnItemClickListener
{

    SQLiteDatabase myDatabase = null;
    int size = 20;
    int[] matrimonyId = new int[size];
    String[] ireligion = new String[size];
    String[] icaste = new String[size];
    String[] iage = new String[size];
    String[] isex = new String[size];
    String[] iheight = new String[size];
    String[] iweight = new String[size];
    String[] icolor = new String[size];
    String[] icountry = new String[size];
    String[] icity = new String[size];
    String[] iarea = new String[size];

    String[] ureligion = new String[size];
    String[] ucaste = new String[size];
    String[] uage = new String[size];
    String[] usex = new String[size];
    String[] uheight = new String[size];
    String[] uweight = new String[size];
    String[] ucolor = new String[size];
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
            String myCols[] = {"key", "ireligion", "icaste", "iage", "isex",
                    "iheight", "iweight", "icolor", "iarea", "icity ",
                    "icountry ", "ureligion", "ucaste", "uage", "usex ",
                    "uheight", "uweight ", "ucolor", "uarea", "ucity",
                    "ucountry", "queryStatus"};
            c = myDatabase.query("Matrimony", myCols, null, null,
                    null, null, null);
            rows = c.getCount();

            int idcolumn = c.getColumnIndexOrThrow("key");
            int religionColumn = c.getColumnIndexOrThrow("ireligion");
            int casteColumn = c.getColumnIndexOrThrow("icaste");
            int ageColumn = c.getColumnIndexOrThrow("iage");
            int sexColumn = c.getColumnIndexOrThrow("isex");
            int heightColumn = c.getColumnIndexOrThrow("iheight");
            int weightColumn = c.getColumnIndexOrThrow("iweight");
            int colorColumn = c.getColumnIndexOrThrow("icolor");
            int countryColumn = c.getColumnIndexOrThrow("icountry");
            int cityColumn = c.getColumnIndexOrThrow("icity");
            int areaColumn = c.getColumnIndexOrThrow("iarea");

            int ureligioncolumn = c.getColumnIndexOrThrow("ureligion");
            int ucastecolumn = c.getColumnIndexOrThrow("ucaste");
            int uagecolumn = c.getColumnIndexOrThrow("uage");
            int usexcolumn = c.getColumnIndexOrThrow("usex");
            int uheightcolumn = c.getColumnIndexOrThrow("uheight");
            int uweightcolumn = c.getColumnIndexOrThrow("uweight");
            int ucolorcolumn = c.getColumnIndexOrThrow("ucolor");
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
                        String getireligion = c.getString(religionColumn);
                        String geticaste = c.getString(casteColumn);
                        String getiage = c.getString(ageColumn);
                        String getisex = c.getString(sexColumn);
                        String getiheight = c.getString(heightColumn);
                        String getiweight = c.getString(weightColumn);
                        String geticolor = c.getString(colorColumn);
                        String geticountry = c.getString(countryColumn);
                        String geticity = c.getString(cityColumn);
                        String getiarea = c.getString(areaColumn);

                        String getureligion = c.getString(ureligioncolumn);
                        String getucaste = c.getString(ucastecolumn);
                        String getuage = c.getString(uagecolumn);
                        String getusex = c.getString(usexcolumn);
                        String getuheight = c.getString(uheightcolumn);
                        String getuweight = c.getString(uweightcolumn);
                        String getucolor = c.getString(ucolorcolumn);
                        String getucountry = c.getString(ucountrycolumn);
                        String getucity = c.getString(ucitycolumn);
                        String getuarea = c.getString(uareacolumn);
                        String getustatus = c.getString(uquerystatuscolumn);


                        matrimonyId[count] = getid;
                        ireligion[count] = getireligion;
                        icaste[count] = geticaste;
                        iage[count] = getiage;
                        isex[count] = getisex;
                        iheight[count] = getiheight;
                        iweight[count] = getiweight;
                        icolor[count] = geticolor;
                        icountry[count] = geticountry;
                        icity[count] = geticity;
                        iarea[count] = getiarea;

                        ureligion[count] = getureligion;
                        ucaste[count] = getucaste;
                        uage[count] = getuage;
                        usex[count] = getusex;
                        uheight[count] = getuheight;
                        uweight[count] = getuweight;
                        ucolor[count] = getucolor;
                        ucountry[count] = getucountry;
                        ucity[count] = getucity;
                        uarea[count] = getuarea;
                        queryStatus[count] = getustatus;

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
                sv = new SpeechView(mContext, "Religion = " +
                        ireligion[position] + " Caste = " + icaste[position] +
                        " Age = " + iage[position] + " Sex = " +
                        isex[position] + " Height = " + iheight[position] +
                        " Weight = " + iweight[position] + " Color = " +
                        icolor[position] + " Area = " + iarea[position] +
                        " City = " + icity[position] + " Country = " +
                        icountry[position],
                        "Religion = " + ureligion[position] + "Caste = " +
                                ucaste[position] + "Age Range = " +
                                uage[position] + "Sex = " + usex[position] +
                                " Height Range = " + uheight[position] +
                                " Weight Range= " + uweight[position] +
                                " Color = " + ucolor[position] + " Area = " +
                                uarea[position] + " City = " + ucity[position] +
                                " Country = " + ucountry[position]);
            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iage[position]);
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
            mTitle.setText("User Profile : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("Partner Profile : " + words);
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
        Intent editDating =
                new Intent(Matrimonylistquery.this, Matrimony.class);
        Bundle passkeyBundle = new Bundle();
        passkeyBundle.putInt("key", matrimonyId[passkeyvalue]);
        editDating.putExtras(passkeyBundle);
        Cursor tempdatingcursor = myDatabase.query("matrimonyposition",
                null, "key=" + matrimonyId[passkeyvalue], null, null, null,
                null);
        if (tempdatingcursor != null)
        {
            if (tempdatingcursor.isFirst())
            {
                int getireligionposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ireligionposition"));
                int geticasteposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "icasteposition"));
                int geticolorposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "icolorposition"));
                int getiageposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "iageposition"));
                int getiheightposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "iheightposition"));
                int getiweightposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "iweightposition"));
                String getisex = tempdatingcursor
                        .getString(
                                tempdatingcursor.getColumnIndexOrThrow("isex"));
                int getureligionposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ureligionposition"));
                int getucasteposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ucasteposition"));
                int getucolorposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "ucolorposition"));
                int getuageposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "uageposition"));
                int getuheightposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "uheightposition"));
                int getuweightposition = tempdatingcursor
                        .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                "uweightposition"));
                String getusex = tempdatingcursor
                        .getString(
                                tempdatingcursor.getColumnIndexOrThrow("usex"));

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
                String getcategory = tempdatingcursor
                        .getString(tempdatingcursor.getColumnIndexOrThrow(
                                "category"));
                String getstufftype = tempdatingcursor.getString(
                        tempdatingcursor.getColumnIndexOrThrow("stufftype"));
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempmatrimony" +
                        " (ireligionposition NUMERIC, icasteposition NUMERIC, iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, icolorposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ureligionposition NUMERIC, ucasteposition NUMERIC, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, ucolorposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");
                myDatabase.execSQL(
                        "INSERT INTO tempmatrimony (ireligionposition, icasteposition, iageposition, iheightposition, iweightposition, icolorposition, iarea, icity, icountry, ureligionposition, ucasteposition, uageposition, uheightposition, uweightposition, ucolorposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                getireligionposition + "," + geticasteposition +
                                "," + getiageposition + "," +
                                getiheightposition + "," + getiweightposition +
                                "," + geticolorposition + ",'" + getiarea +
                                "','" + geticity + "','" + geticountry + "'," +
                                getureligionposition + "," + getucasteposition +
                                "," + getuageposition + "," +
                                getuheightposition + "," + getuweightposition +
                                "," + getucolorposition + ",'" + getuarea +
                                "','" + getucity + "','" + getucountry + "','" +
                                getisex + "','" + getusex + "','" +
                                getilatitude + "','" + getilongitude + "','" +
                                getulatitude + "','" + getulongitude + "','" +
                                "Matrimony" + "', '" + "istuff" + "');");

            }
        }
        startActivityForResult(editDating, 0);
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
                Intent stuffCheckintent = new Intent(Matrimonylistquery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(Matrimonylistquery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(Matrimonylistquery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
