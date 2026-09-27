package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: MoviesListQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss            $: Id of last commit
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

public class MoviesListQuery
        extends ListActivity
        implements OnItemClickListener
{
    SQLiteDatabase myDatabase = null;
    Cursor c;
    int rows = 0;
    int count = 0;
    int size = 15;
    ArrayList<String> results = new ArrayList<String>();
    String[] iStuffMovieType = new String[size];
    String[] iStuffMovieLanguage = new String[size];
    String[] iStuffSeatingStyle = new String[size];

    String[] iStuffcountry = new String[size];
    String[] iStuffcity = new String[size];
    String[] iStuffarea = new String[size];

    String[] uStuffMovieType = new String[size];
    String[] uStuffMovieLanguage = new String[size];
    String[] uStuffSeatingStyle = new String[size];

    String[] uStuffcountry = new String[size];
    String[] uStuffcity = new String[size];
    String[] uStuffarea = new String[size];
    int[] MovieId = new int[size];

    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setListAdapter(new SpeechListAdapter(this));
        getListView().setOnItemClickListener(this);
    }

    public class SpeechListAdapter
            extends BaseAdapter
    {

        public SpeechListAdapter(Context context)
        {
            //Context mContext = (Context) context;
            mContext = context;
            myDatabase = mContext.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String myCols[] = {"key", "imovietype", "imovielanguage",
                    "iseatingstyle", "icountry", "icity", "iarea", "umovietype",
                    "umovielanguage", "useatingstyle", "ucountry", "ucity",
                    "uarea", "queryStatus"};
            c = myDatabase
                    .query("Movies", myCols, null, null, null, null, null);
            rows = c.getCount();

            int idcolumn = c.getColumnIndexOrThrow("key");
            int imovietypeColumn = c.getColumnIndexOrThrow("imovietype");
            int imovielanguageColumn =
                    c.getColumnIndexOrThrow("imovielanguage");
            int imiscColumn = c.getColumnIndexOrThrow("iseatingstyle");
            int icountryColumn = c.getColumnIndexOrThrow("icountry");
            int icityColumn = c.getColumnIndexOrThrow("icity");
            int iareaColumn = c.getColumnIndexOrThrow("iarea");


            int umovietypeColumn = c.getColumnIndexOrThrow("umovietype");
            int umovielanguageColumn =
                    c.getColumnIndexOrThrow("umovielanguage");
            int umiscColumn = c.getColumnIndexOrThrow("useatingstyle");
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
                        String getimovietype = c.getString(imovietypeColumn);
                        String getimovielanguage =
                                c.getString(imovielanguageColumn);
                        String getiseatingstyle = c.getString(imiscColumn);
                        String geticountry = c.getString(icountryColumn);
                        String geticity = c.getString(icityColumn);
                        String getiarea = c.getString(iareaColumn);

                        String getumovietype = c.getString(umovietypeColumn);
                        String getumovielanguage =
                                c.getString(umovielanguageColumn);
                        String getuseatingstyle = c.getString(umiscColumn);
                        String getucountry = c.getString(ucountryColumn);
                        String getucity = c.getString(ucityColumn);
                        String getuarea = c.getString(uareaColumn);

                        MovieId[count] = getid;
                        iStuffMovieType[count] = getimovietype;
                        iStuffMovieLanguage[count] = getimovielanguage;
                        iStuffSeatingStyle[count] = getiseatingstyle;
                        iStuffarea[count] = getiarea;

                        iStuffcountry[count] = geticountry;
                        iStuffcity[count] = geticity;

                        uStuffMovieType[count] = getumovietype;
                        uStuffMovieLanguage[count] = getumovielanguage;
                        uStuffSeatingStyle[count] = getuseatingstyle;
                        uStuffarea[count] = getuarea;
                        uStuffcity[count] = getucity;
                        uStuffcountry[count] = getucountry;

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

                sv = new SpeechView(mContext, " MovieType = " +
                        iStuffMovieType[position] + ", MovieLanguage = " +
                        iStuffMovieLanguage[position] + ", SeatingStyle = " +
                        iStuffSeatingStyle[position] + ", Area = " +
                        iStuffarea[position] + ", City = " +
                        iStuffcity[position] + ", Country = " +
                        iStuffcountry[position],
                        " MovieType = " + uStuffMovieType[position] +
                                ", MovieLanguage = " +
                                uStuffMovieLanguage[position] +
                                ", SeatingStyle = " +
                                uStuffSeatingStyle[position] + ", Area = " +
                                uStuffarea[position] + ", City = " +
                                uStuffcity[position] + ", Country = " +
                                uStuffcountry[position]);

            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iStuffMovieType[position]);
                sv.setDialogue(uStuffarea[position]);
            }
            return sv;
        }

        private Context mContext;

        public void onItemClick(AdapterView arg0, View arg1, int arg2,
                long arg3)
        {

        }
    }

    public class SpeechView
            extends LinearLayout
    {

        public SpeechView(Context context, String title, String words)
        {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            mTitle.setText("Owner Detail : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("Public Detail : " + words);
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
        /* Intent i = new Intent(MoviesListQuery.this, Movies.class);
        startActivity(i);*/
        String selectid = parent.getItemAtPosition(position).toString();
        int passkeyvalue = Integer.parseInt(selectid);
        Intent editMovies = new Intent(MoviesListQuery.this, Movies.class);

        Cursor tempmoviescursor = myDatabase.query("moviesposition", null,
                "key=" + MovieId[passkeyvalue], null, null, null, null);
        if (tempmoviescursor != null)
        {
            if (tempmoviescursor.isFirst())
            {

                int getiMovieTypeposition = tempmoviescursor
                        .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                "imovietypeposition"));
                int getiMovieLanguageposition = tempmoviescursor
                        .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                "imovielanguageposition"));
                int getiSeatingStyleposition = tempmoviescursor
                        .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                "iseatingstyleposition"));

                int getuMovieTypeposition = tempmoviescursor
                        .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                "umovietypeposition"));
                int getuMovieLanguageposition = tempmoviescursor
                        .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                "umovielanguageposition"));
                int getuSeatingStyleposition = tempmoviescursor
                        .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                "useatingstyleposition"));

                String getiarea = tempmoviescursor
                        .getString(tempmoviescursor.getColumnIndexOrThrow(
                                "iarea"));
                String geticity = tempmoviescursor
                        .getString(tempmoviescursor.getColumnIndexOrThrow(
                                "icity"));
                String geticountry = tempmoviescursor
                        .getString(tempmoviescursor.getColumnIndexOrThrow(
                                "icountry"));

                String getuarea = tempmoviescursor
                        .getString(tempmoviescursor.getColumnIndexOrThrow(
                                "uarea"));
                String getucity = tempmoviescursor
                        .getString(tempmoviescursor.getColumnIndexOrThrow(
                                "ucity"));
                String getucountry = tempmoviescursor
                        .getString(tempmoviescursor.getColumnIndexOrThrow(
                                "ucountry"));

                String getilatitude = tempmoviescursor.getString(
                        tempmoviescursor.getColumnIndexOrThrow("ilatitude"));
                String getilongitude = tempmoviescursor.getString(
                        tempmoviescursor.getColumnIndexOrThrow("ilongitude"));

                String getulatitude = tempmoviescursor.getString(
                        tempmoviescursor.getColumnIndexOrThrow("ulatitude"));
                String getulongitude = tempmoviescursor.getString(
                        tempmoviescursor.getColumnIndexOrThrow("ulongitude"));
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempmovies" +
                        " (imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, umovielanguageposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,  ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");
                myDatabase.execSQL(
                        "INSERT INTO tempmovies (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry, ilatitude, ilongitude , umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                getiMovieTypeposition + "," +
                                getiMovieLanguageposition + "," +
                                getiSeatingStyleposition + ",'" + getiarea +
                                "','" + geticity + "','" + geticountry + "'," +
                                getuMovieTypeposition + "," +
                                getuMovieLanguageposition + "," +
                                getuSeatingStyleposition + ",'" + getuarea +
                                "','" + getucity + "','" + getucountry + "','" +
                                getilatitude + "','" + getilongitude + "','" +
                                getulatitude + "','" + getulongitude + "','" +
                                "Movies" + "', '" + "istuff" + "');");

                // myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempmovies" + " (imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, useatingstyleposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
                // myDatabase.execSQL("INSERT INTO tempmovies (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry,ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" + getiMovieTypeposition + "," + getiMovieLanguageposition + "," + getiSeatingStyleposition + ",'" + "" + "','" + "" + "','" + "" + "','" +"" +"','" +"" + "'," + getuMovieTypeposition + "," + getuMovieLanguageposition + "," + getuSeatingStyleposition + ",'" + "" + "','" + "" + "','" + "" + "','" + "" + "','" + ""  + "','" + "Movies" + "', '" + "istuff" + "');");

            }
        }
        Bundle passkeyBundle = new Bundle();
        passkeyBundle.putInt("key", MovieId[passkeyvalue]);
        editMovies.putExtras(passkeyBundle);
        startActivityForResult(editMovies, 0);
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
                Intent stuffCheckintent = new Intent(MoviesListQuery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(MoviesListQuery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(MoviesListQuery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
          
