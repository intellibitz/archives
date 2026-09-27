package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Movies.java 14 2008-08-19 06:36:45Z muthu.ramadoss                     $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.activity.Uploadmultimedia;
import com.mobeegal.android.util.ViewMenu;

public class Movies
        extends Activity
{

    SQLiteDatabase myDatabase = null;
    int count;
    String getiStuffSeatingStyle;
    String getiStuffMovieType;
    String getiStuffMovieLanguage;
    String getiStuffCountry;
    String getuStuffSeatingStyle;
    String getuStuffMovieLanguage;
    String getuStuffMovieType;
    ArrayAdapter<CharSequence> adapter;
    public String value1;
    String getfilepath;
    int key = 1;
    Bundle fromeditquery;
    int getkey;
    int getiMovieTypeposition;
    int getiMovieLanguageposition;
    int getiSeatingStylyposition;
    int getuMovieTypeposition;
    int getuMovieLanguageposition;
    int getuSeatingStyleposition;
    String getiarea;
    String geticity;
    String geticountry;
    String getuarea;
    String getucity;
    String getucountry;
    String getcategory;
    String getstufftype;
    String getilatitude;
    String getilongitude;
    String getulatitude;
    String getulongitude;
    int imovietypeposition;
    int imovielanguageposition;
    int iseatingstyleposition;
    int umovietypeposition;
    int umovielanguageposition;
    int useatingstyleposition;
    TextView iarea;
    TextView icity;
    TextView icountry;
    TextView uarea;
    TextView ucity;
    TextView ucountry;
    Spinner iStuffMovieType;
    Spinner iStuffMovieLanguage;
    Spinner iStuffSeatingStyle;
    Spinner uStuffMovieLanguage;
    Spinner uStuffSeatingStyle;
    int theme;
    private String viewtypename;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        fromeditquery = this.getIntent().getExtras();
        if (fromeditquery != null)
        {
            getkey = fromeditquery.getInt("key");
        }
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempmoviescursor = myDatabase
                    .query("tempmovies", null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempmovies" +
                    " (imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, useatingstyleposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
            myDatabase.execSQL(
                    "INSERT INTO tempmovies (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry,ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                            imovietypeposition + "," + imovielanguageposition +
                            "," + iseatingstyleposition + ",'" + "" + "','" +
                            "" + "','" + "" + "','" + "" + "','" + "" + "'," +
                            imovietypeposition + "," + imovielanguageposition +
                            "," + iseatingstyleposition + ",'" + "" + "','" +
                            "" + "','" + "" + "','" + "" + "','" + "" + "','" +
                            "Movies" + "', '" + "istuff" + "');");
        }
        finally
        {
            if (myDatabase != null)
            {
                myDatabase.close();
            }
        }

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor themecursor = myDatabase.query("Theme", null,
                    "catalog='Movies'", null, null, null, null);
            if (themecursor != null)
            {
                if (themecursor.isFirst())
                {
                    do
                    {
                        theme = themecursor
                                .getInt(themecursor.getColumnIndexOrThrow(
                                        "theme"));
                        //String catalog = themecursor.getString(themecursor.getColumnIndexOrThrow("theme"));
                    }
                    while (themecursor.moveToNext());
                }
            }
        }
        catch (Exception e1)
        {

        }
        if (theme == 0)
        {
            this.setTheme(android.R.style.Theme_Black);
        }
        if (theme == 1)
        {
            this.setTheme(android.R.style.Theme_Dialog);
        }
        if (theme == 2)
        {
            this.setTheme(android.R.style.Theme);
        }
        if (theme == 3)
        {
            this.setTheme(android.R.style.Theme);
        }
        setContentView(R.layout.movies);

        final TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.iStuffprofile);
        one.setIndicator("Owner");
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.uStuffprofile);
        two.setIndicator("Public");
        tabs.addTab(two);

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempmoviescursor = myDatabase
                    .query("tempmovies", null, null, null, null, null, null);

            if (tempmoviescursor != null)
            {
                if (tempmoviescursor.isFirst())
                {
                    do
                    {
                        getiMovieTypeposition = tempmoviescursor
                                .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                        "imovietypeposition"));
                        getiMovieLanguageposition = tempmoviescursor
                                .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                        "imovielanguageposition"));
                        getiSeatingStylyposition = tempmoviescursor
                                .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                        "iseatingstyleposition"));

                        getuMovieTypeposition = tempmoviescursor
                                .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                        "umovietypeposition"));
                        getuMovieLanguageposition = tempmoviescursor
                                .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                        "umovielanguageposition"));
                        getuSeatingStyleposition = tempmoviescursor
                                .getInt(tempmoviescursor.getColumnIndexOrThrow(
                                        "useatingstyleposition"));

                        getiarea = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "iarea"));
                        geticity = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "icity"));
                        geticountry = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "icountry"));
                        getuarea = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "uarea"));
                        getucity = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "ucity"));
                        getucountry = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "ucountry"));
                        getilatitude = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "ilatitude"));
                        getilongitude = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "ilongitude"));
                        getulatitude = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "ulatitude"));
                        getulongitude = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "ulongitude"));
                        getcategory = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "category"));
                        getstufftype = tempmoviescursor.getString(
                                tempmoviescursor.getColumnIndexOrThrow(
                                        "stufftype"));
                    }
                    while (tempmoviescursor.moveToNext());
                }
            }
        }
        catch (Exception e)
        {
        }

        //Creating a tab1 for ISTUFF

        iStuffMovieType = (Spinner) findViewById(R.id.iStuffmovietype);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                this, R.array.MovieType, android.R.layout.simple_spinner_item);
        iStuffMovieType.setAdapter(adapter1);
        iStuffMovieType.setSelection(getiMovieTypeposition);
        iStuffMovieType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffMovieType =
                                (String) iStuffMovieType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmovies set imovietypeposition=" +
                                        position);
                        imovietypeposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffMovieLanguage = (Spinner) findViewById(R.id.iStuffmovielanguage);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.MovieLanguage,
                android.R.layout.simple_spinner_item);
        iStuffMovieLanguage.setAdapter(adapter2);
        iStuffMovieLanguage.setSelection(getiMovieLanguageposition);
        iStuffMovieLanguage.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffMovieLanguage =
                                (String) iStuffMovieLanguage.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmovies set imovielanguageposition=" +
                                        position);
                        imovielanguageposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffSeatingStyle = (Spinner) findViewById(R.id.iStuffMisc);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this, R.array.SeatingStyle,
                android.R.layout.simple_spinner_item);
        iStuffSeatingStyle.setAdapter(adapter3);
        iStuffSeatingStyle.setSelection(getiSeatingStylyposition);
        iStuffSeatingStyle.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffSeatingStyle =
                                (String) iStuffSeatingStyle.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmovies set iseatingstyleposition=" +
                                        position);
                        iseatingstyleposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        if (getstufftype.equals("istuff"))
        {
            tabs.setCurrentTab(0);
        }
        else if (getstufftype.equals("ustuff"))
        {
            tabs.setCurrentTab(1);
        }

        iarea = (TextView) findViewById(R.id.iarea);
        iarea.setText(getiarea);
        icity = (TextView) findViewById(R.id.icity);
        icity.setText(geticity);
        icountry = (TextView) findViewById(R.id.icountry);
        icountry.setText(geticountry);
        uarea = (TextView) findViewById(R.id.uarea);
        uarea.setText(getuarea);
        ucity = (TextView) findViewById(R.id.ucity);
        ucity.setText(getucity);
        ucountry = (TextView) findViewById(R.id.ucountry);
        ucountry.setText(getucountry);

        //Creating a tab2 for USTUFF

        final Spinner uStuffMovieType =
                (Spinner) findViewById(R.id.uStuffmovietype);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
                this, R.array.MovieType, android.R.layout.simple_spinner_item);
        uStuffMovieType.setAdapter(adapter4);
        uStuffMovieType.setSelection(getuMovieTypeposition);
        uStuffMovieType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffMovieType =
                                (String) uStuffMovieType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmovies set umovietypeposition=" +
                                        position);
                        umovietypeposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        uStuffMovieLanguage = (Spinner) findViewById(R.id.uStuffmovielanguage);
        ArrayAdapter<CharSequence> adapter15 = ArrayAdapter.createFromResource(
                this, R.array.MovieLanguage,
                android.R.layout.simple_spinner_item);
        uStuffMovieLanguage.setAdapter(adapter15);
        uStuffMovieLanguage.setSelection(getuMovieLanguageposition);
        uStuffMovieLanguage.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffMovieLanguage =
                                (String) uStuffMovieLanguage.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmovies set umovielanguageposition=" +
                                        position);
                        umovielanguageposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        uStuffSeatingStyle = (Spinner) findViewById(R.id.uStuffMisc);
        ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(
                this, R.array.SeatingStyle,
                android.R.layout.simple_spinner_item);
        uStuffSeatingStyle.setAdapter(adapter6);
        uStuffSeatingStyle.setSelection(getuSeatingStyleposition);
        uStuffSeatingStyle.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffSeatingStyle =
                                (String) uStuffSeatingStyle.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmovies set useatingstyleposition=" +
                                        position);
                        useatingstyleposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });
        //added
        String viewtype[] = {"views"};
        Cursor cur = myDatabase.query("Preferences", viewtype, null, null,
                null, null, null);
        int viewname = cur.getColumnIndexOrThrow("views");
        if (cur != null)
        {
            if (cur.isFirst())
            {
                do
                {
                    viewtypename = cur.getString(viewname);
                }
                while (cur.moveToNext());
            }
        }
        Button ichoose = (Button) findViewById(R.id.selectistufflocation);
        ichoose.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {
                myDatabase.execSQL(
                        "UPDATE tempmovies set stufftype='" + "istuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Movies.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmovies");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Movies.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmovies");
                    b.putInt("key", getkey);
                    textview.putExtras(b);
                    startActivityForResult(textview, 0);
                }
            }
        });

        Button uchoose = (Button) findViewById(R.id.selectustufflocation);
        uchoose.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {
                myDatabase.execSQL(
                        "UPDATE tempmovies set stufftype='" + "ustuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Movies.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmovies");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Movies.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmovies");
                    b.putInt("key", getkey);
                    textview.putExtras(b);
                    startActivityForResult(textview, 0);
                }
            }
        });

        final Button save = (Button) findViewById(R.id.Save);
        save.setOnClickListener(new Button.OnClickListener()
        {

            private double getilongitudes;
            private double getilatitudes;
            private String idetails;

            public void onClick(View v)
            {
                getiStuffMovieType = (String) iStuffMovieType.getSelectedItem();
                getiStuffMovieLanguage =
                        (String) iStuffMovieLanguage.getSelectedItem();
                getiStuffSeatingStyle =
                        (String) iStuffSeatingStyle.getSelectedItem();

                imovietypeposition = iStuffMovieType.getSelectedItemPosition();
                imovielanguageposition =
                        iStuffMovieLanguage.getSelectedItemPosition();
                iseatingstyleposition =
                        iStuffSeatingStyle.getSelectedItemPosition();

                idetails = "movietype=" + getiStuffMovieType +
                        " movielanguage=" + getiStuffMovieLanguage +
                        "  seatingstyle=" + getiStuffSeatingStyle + " Area=" +
                        getiarea + " City=" + geticity + " country=" +
                        geticountry;

                getilatitudes = Double.parseDouble(getilatitude);
                getilatitudes = getilatitudes * 1E6;
                getilongitudes = Double.parseDouble(getilongitude);
                getilongitudes = getilongitudes * 1E6;

                if (fromeditquery != null && getkey != 0)
                {
                    myDatabase.execSQL(
                            "UPDATE moviesposition set imovietypeposition=" +
                                    imovietypeposition +
                                    ", imovielanguageposition=" +
                                    imovielanguageposition +
                                    ", iseatingstyleposition=" +
                                    iseatingstyleposition + ", iarea='" +
                                    getiarea + "', icity='" + geticity +
                                    "', icountry='" + geticountry +
                                    "', umovietypeposition=" +
                                    umovietypeposition +
                                    ", umovielanguageposition=" +
                                    umovielanguageposition +
                                    ", useatingstyleposition=" +
                                    useatingstyleposition + ", uarea='" +
                                    getuarea + "', ucity='" + getucity +
                                    "', ucountry='" + getucountry +
                                    "', ilatitude='" + getilatitude +
                                    "', ilongitude='" + getilongitude +
                                    "',ulatitude='" + getulatitude +
                                    "',ulongitude='" + getulongitude +
                                    "' where key=" + getkey + ";");
                    myDatabase.execSQL("UPDATE Movies set imovietype='" +
                            getiStuffMovieType + "', imovielanguage='" +
                            getiStuffMovieLanguage + "', iseatingstyle='" +
                            getiStuffSeatingStyle + "', iarea='" + getiarea +
                            "', icity='" + geticity + "', icountry='" +
                            geticountry + "', umovietype='" +
                            getuStuffMovieType + "', umovielanguage='" +
                            getuStuffMovieLanguage + "', useatingstyle='" +
                            getuStuffSeatingStyle + "', uarea='" + getuarea +
                            "', ucity='" + getucity + "', ucountry='" +
                            getucountry + "', ilatitude='" + getilatitude +
                            "', ilongitude='" + getilongitude +
                            "', ulatitude='" + getulatitude +
                            "', ulongitude='" + getulongitude +
                            "',queryDate=DATE('NOW') where key=" + getkey +
                            ";");
                    myDatabase.execSQL("update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';");
                    myDatabase.execSQL("Update mStuffdetails set details='" +
                            idetails + "', latitude='" + getilatitudes +
                            "', longitude='" + getilongitudes +
                            "', location='" + geticountry +
                            "' where catagory='" + "userMovies" + "';");
                }
                else
                {
                    try
                    {
                        myDatabase.execSQL(
                                "INSERT INTO Movies(imovietype,imovielanguage,iseatingstyle,iarea,icity,icountry, ilatitude, ilongitude, umovietype,umovielanguage,useatingstyle,uarea,ucity, ucountry, ulatitude, ulongitude, queryStatus) VALUES ('" +
                                        getiStuffMovieType + "','" +
                                        getiStuffMovieLanguage + "','" +
                                        getiStuffSeatingStyle + "','" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getuStuffMovieType + "','" +
                                        getuStuffMovieLanguage + "','" +
                                        getuStuffSeatingStyle + "','" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getulatitude +
                                        "','" + getulongitude + "','" + "true" +
                                        "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "INSERT INTO moviesposition (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,  umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry,ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                        imovietypeposition + "," +
                                        imovielanguageposition + "," +
                                        iseatingstyleposition + ",'" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "'," +
                                        umovietypeposition + "," +
                                        umovielanguageposition + "," +
                                        useatingstyleposition + ",'" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getulatitude + "','" + getulongitude +
                                        "','" + "Movies" + "', '" + "istuff" +
                                        "');");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry +
                                        "' where catagory='userMovies';");
                    }
                    catch (Exception exce)
                    {
                        myDatabase.execSQL(
                                "CREATE TABLE IF NOT EXISTS Movies(key INTEGER PRIMARY KEY,imovietype VARCHAR,imovielanguage VARCHAR,iseatingstyle VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR,umovietype VARCHAR,umovielanguage VARCHAR,useatingstyle VARCHAR, uarea VARCHAR,ucity VARCHAR, ucountry VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, querystatus VARCHAR,queryDate DATE);");
                        myDatabase.execSQL(
                                "CREATE TRIGGER insert_querydate_Movies after INSERT on Movies BEGIN update Movies set queryDate=DATE('NOW') WHERE key=new.key; END;");
                        myDatabase.execSQL(
                                "CREATE TRIGGER delete_querydate_Movies before insert on Movies BEGIN delete from Movies where queryDate<DATE('NOW','-7 day');END;");
                        myDatabase.execSQL(
                                "INSERT INTO Movies(imovietype,imovielanguage,iseatingstyle,iarea,icity,icountry, ilatitude, ilongitude, umovietype,umovielanguage,useatingstyle,uarea,ucity, ucountry, ulatitude, ulongitude, queryStatus) VALUES ('" +
                                        getiStuffMovieType + "','" +
                                        getiStuffMovieLanguage + "','" +
                                        getiStuffSeatingStyle + "','" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getuStuffMovieType + "','" +
                                        getuStuffMovieLanguage + "','" +
                                        getuStuffSeatingStyle + "','" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getulatitude +
                                        "','" + getulongitude + "','" + "true" +
                                        "');");
                        myDatabase.execSQL(
                                "CREATE TABLE IF NOT EXISTS moviesposition" +
                                        " (key INTEGER PRIMARY KEY,imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR,ilatitude VARCHAR, ilongitude VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, useatingstyleposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,  ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
                        myDatabase.execSQL(
                                "INSERT INTO moviesposition (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,ilatitude , ilongitude, umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                        imovietypeposition + "," +
                                        imovielanguageposition + "," +
                                        iseatingstyleposition + ",'" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "'," +
                                        umovietypeposition + "," +
                                        umovielanguageposition + "," +
                                        useatingstyleposition + ",'" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getulatitude + "','" + getulongitude +
                                        "','" + "Movies" + "', '" + "istuff" +
                                        "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry +
                                        "' where catagory='userMovies';");
                    }
                }

                myDatabase.execSQL("drop table " + "tempmovies" + ";");
                Intent intent = new Intent(Movies.this, FindandInstall.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsmStuffMenu(menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
            case 1:
                Intent intent1 = new Intent(Movies.this, MapResults.class);
                startActivityForResult(intent1, 0);
                finish();
                break;

            case 2:
                Intent intent2 = new Intent(Movies.this, FindandInstall.class);
                startActivityForResult(intent2, 0);
                finish();
                break;

            case 3:
                Intent intent3 = new Intent(Movies.this, Settings.class);
                startActivityForResult(intent3, 0);
                finish();
                break;
            case 4:

                Intent intent = new Intent(Movies.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
                break;
            case 5:
                try
                {
                    Cursor tempmoviescursor = myDatabase.query("Movies", null,
                            null, null, null, null, null);
                    Intent listmStuff =
                            new Intent(Movies.this, MoviesListQuery.class);
                    startActivityForResult(listmStuff, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:

                try
                {
                    Cursor tempmoviescursor = myDatabase.query("Movies", null,
                            null, null, null, null, null);
                    Intent deleteQuery1 =
                            new Intent(Movies.this, DeleteMoviesQuery.class);
                    startActivityForResult(deleteQuery1, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
            case 7:

                try
                {
                    Cursor tempmoviescursor = myDatabase.query("Movies", null,
                            null, null, null, null, null);
                    Intent viewQuery =
                            new Intent(Movies.this, MoviesViewQuery.class);
                    startActivityForResult(viewQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case 8:
                Bundle b1 = new Bundle();
                Intent settheme = new Intent(Movies.this, SettingTheme.class);
                b1.putString("value1", "Movies");
                b1.putString("class", "6");
                settheme.putExtras(b1);
                startActivityForResult(settheme, 0);
                finish();
            case 9:
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", "Movies");
                Intent mediaintent =
                        new Intent(Movies.this, Uploadmultimedia.class);
                mediaintent.putExtras(B1);
                startActivity(mediaintent);
        }
        return super.onOptionsItemSelected(item);
    }
}
