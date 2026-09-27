package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Home.java 14 2008-08-19 06:36:45Z muthu.ramadoss                       $: Id of last commit
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

/**
 * @author mobeegal
 */
public class Home
        extends Activity
{

    int key = 1;
    SQLiteDatabase myDatabase = null;
    String getiStuffRentalType;
    String getiStuffMisc;
    String getiStuffStatus;
    String getiStuffRate;
    String getuStuffRentalType;
    String getuStuffMisc;
    String getuStuffStatus;
    String getuStuffRate;
    ArrayAdapter adapter;
    int irentalposition;
    int imiscposition;
    int istatusposition;
    int irateposition;
    int urentalposition;
    int umiscposition;
    int ustatusposition;
    int urateposition;
    int getirentalposition;
    int getimiscposition;
    int getistatusposition;
    int getirateposition;
    int geturentalposition;
    int getumiscposition;
    int getustatusposition;
    int geturateposition;
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
    TextView iarea;
    TextView icity;
    TextView icountry;
    TextView uarea;
    TextView ucity;
    TextView ucountry;
    Bundle fromeditquery;
    int getkey;
    int theme;
    private String viewtypename;

    // String catalog;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor temprentalcursor = myDatabase
                    .query("temprental", null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS temprental" +
                    " (irentalposition NUMERIC, imiscposition NUMERIC, istatusposition NUMERIC, irateposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, urentalposition NUMERIC, umiscposition NUMERIC, ustatusposition NUMERIC,urateposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
            myDatabase.execSQL(
                    "INSERT INTO temprental (irentalposition,imiscposition , istatusposition , irateposition , iarea , icity , icountry , urentalposition , umiscposition , ustatusposition ,urateposition , uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype ) VALUES (" +
                            irentalposition + "," + imiscposition + "," +
                            istatusposition + "," + irateposition + ",'" + "" +
                            "','" + "" + "','" + "" + "'," + urentalposition +
                            "," + umiscposition + "," + ustatusposition + "," +
                            urateposition + ",'" + "" + "','" + "" + "','" +
                            "" + "','" + "" + "','" + "" + "','" + "" + "','" +
                            "" + "','" + "Rental" + "', '" + "istuff" + "');");
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
                    "catalog='Home'", null, null, null, null);
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
            this.setTheme(android.R.style.Theme_Dialog);
        }
        if (theme == 3)
        {
            this.setTheme(android.R.style.Theme_Dialog);
        }
        setContentView(R.layout.home);

        final TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.iStuffprofile);
        one.setIndicator("Owner");
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.uStuffprofile);
        two.setIndicator("Tenant");
        tabs.addTab(two);
        fromeditquery = this.getIntent().getExtras();
        if (fromeditquery != null)
        {
            getkey = fromeditquery.getInt("key");
        }

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor temprentalcursor = myDatabase
                    .query("temprental", null, null, null, null, null, null);

            if (temprentalcursor != null)
            {
                if (temprentalcursor.isFirst())
                {
                    do
                    {
                        getirentalposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "irentalposition"));
                        getimiscposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "imiscposition"));
                        getistatusposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "istatusposition"));
                        getirateposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "irateposition"));

                        geturentalposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "urentalposition"));
                        getumiscposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "umiscposition"));
                        getustatusposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "ustatusposition"));
                        geturateposition = temprentalcursor
                                .getInt(temprentalcursor.getColumnIndexOrThrow(
                                        "urateposition"));

                        getiarea = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "iarea"));
                        geticity = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "icity"));
                        geticountry = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "icountry"));
                        getuarea = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "uarea"));
                        getucity = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "ucity"));
                        getucountry = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "ucountry"));
                        getilatitude = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "ilatitude"));
                        getilongitude = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "ilongitude"));
                        getulatitude = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "ulatitude"));
                        getulongitude = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "ulongitude"));
                        getcategory = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "category"));
                        getstufftype = temprentalcursor.getString(
                                temprentalcursor.getColumnIndexOrThrow(
                                        "stufftype"));
                    }
                    while (temprentalcursor.moveToNext());
                }
            }
        }
        catch (Exception e)
        {
        }


        final Spinner iStuffRentalType =
                (Spinner) findViewById(R.id.iStuffRentalType);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                this, R.array.iStuffRentalType,
                android.R.layout.simple_spinner_item);
        iStuffRentalType.setAdapter(adapter1);
        iStuffRentalType.setSelection(getirentalposition);
        iStuffRentalType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffRentalType =
                                (String) iStuffRentalType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set irentalposition=" +
                                        position);
                        irentalposition = position;

                        if (getiStuffRentalType.equals("Select Rental"))
                        {
                            getiStuffRentalType = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner iStuffMisc = (Spinner) findViewById(R.id.iStuffMisc);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this, R.array.iStuffMiscellaneous,
                android.R.layout.simple_spinner_item);
        iStuffMisc.setAdapter(adapter3);
        iStuffMisc.setSelection(getimiscposition);
        iStuffMisc.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffMisc = (String) iStuffMisc.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set imiscposition=" +
                                        position);
                        imiscposition = position;
                        if (getiStuffMisc.equals("Select Misc"))
                        {
                            getiStuffMisc = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner iStuffRate = (Spinner) findViewById(R.id.iStuffRate);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
                this, R.array.iStuffRate, android.R.layout.simple_spinner_item);
        iStuffRate.setAdapter(adapter4);
        iStuffRate.setSelection(getirateposition);
        iStuffRate.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffRate = (String) iStuffRate.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set irateposition=" +
                                        position);
                        irateposition = position;
                        if (getiStuffRate.equals("Select Rate"))
                        {
                            getiStuffRate = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner iStuffStatus = (Spinner) findViewById(R.id.iStuffStatus);
        ArrayAdapter<CharSequence> adapter15 = ArrayAdapter.createFromResource(
                this, R.array.iStuffStatus,
                android.R.layout.simple_spinner_item);
        iStuffStatus.setAdapter(adapter15);
        iStuffStatus.setSelection(getistatusposition);
        iStuffStatus.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffStatus =
                                (String) iStuffStatus.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set istatusposition=" +
                                        position);
                        istatusposition = position;
                        if (getiStuffStatus.equals("Select Status"))
                        {
                            getiStuffStatus = "null";
                        }
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

        final Spinner uStuffRentalType =
                (Spinner) findViewById(R.id.uStuffRentalType);
        ArrayAdapter<CharSequence> adapter8 = ArrayAdapter.createFromResource(
                this, R.array.iStuffRentalType,
                android.R.layout.simple_spinner_item);
        uStuffRentalType.setAdapter(adapter8);
        uStuffRentalType.setSelection(geturentalposition);
        uStuffRentalType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffRentalType =
                                (String) uStuffRentalType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set urentalposition=" +
                                        position);
                        urentalposition = position;
                        if (getuStuffRentalType.equals("Select RentalType"))
                        {
                            getuStuffRentalType = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffMisc = (Spinner) findViewById(R.id.uStuffMisc);
        ArrayAdapter<CharSequence> adapter9 = ArrayAdapter.createFromResource(
                this, R.array.iStuffMiscellaneous,
                android.R.layout.simple_spinner_item);
        uStuffMisc.setAdapter(adapter9);
        uStuffMisc.setSelection(getumiscposition);
        uStuffMisc.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffMisc = (String) uStuffMisc.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set umiscposition=" +
                                        position);
                        umiscposition = position;
                        if (getuStuffMisc.equals("Select Misc"))
                        {
                            getuStuffMisc = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffRate = (Spinner) findViewById(R.id.uStuffRate);
        ArrayAdapter<CharSequence> adapter10 = ArrayAdapter.createFromResource(
                this, R.array.uStuffRate, android.R.layout.simple_spinner_item);
        uStuffRate.setAdapter(adapter10);
        uStuffRate.setSelection(geturateposition);
        uStuffRate.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffRate = (String) uStuffRate.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set urateposition=" +
                                        position);
                        urateposition = position;
                        if (getuStuffRate.equals("Select Rate"))
                        {
                            getuStuffRate = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffStatus = (Spinner) findViewById(R.id.uStuffStatus);
        ArrayAdapter<CharSequence> adapter17 = ArrayAdapter.createFromResource(
                this, R.array.iStuffStatus,
                android.R.layout.simple_spinner_item);
        uStuffStatus.setAdapter(adapter17);
        uStuffStatus.setSelection(getustatusposition);
        uStuffStatus.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {
                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffStatus =
                                (String) uStuffStatus.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprental set ustatusposition=" +
                                        position);
                        ustatusposition = position;
                        if (getuStuffStatus.equals("Select Status"))
                        {
                            getuStuffStatus = "null";
                        }
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
                        "UPDATE temprental set stufftype='" + "istuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Home.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprental");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Home.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprental");
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
                        "UPDATE temprental set stufftype='" + "ustuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Home.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprental");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Home.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprental");
                    b.putInt("key", getkey);
                    textview.putExtras(b);
                    startActivityForResult(textview, 0);
                }
            }
        });

        final Button button = (Button) findViewById(R.id.Save);
        button.setOnClickListener(new Button.OnClickListener()
        {

            private double getilongitudes;
            private double getilatitudes;
            private String idetails;

            public void onClick(View v)
            {
                getiStuffRentalType =
                        (String) iStuffRentalType.getSelectedItem();
                getiStuffMisc = (String) iStuffMisc.getSelectedItem();
                getiStuffStatus = (String) iStuffStatus.getSelectedItem();
                getiStuffRate = (String) iStuffRate.getSelectedItem();

                irentalposition = iStuffRentalType.getSelectedItemPosition();
                imiscposition = iStuffMisc.getSelectedItemPosition();
                istatusposition = iStuffStatus.getSelectedItemPosition();
                irateposition = iStuffRate.getSelectedItemPosition();

                idetails = "rentaltype=" + getiStuffRentalType + " imisc=" +
                        getiStuffMisc + " rate=" + getiStuffRate + " status=" +
                        getiStuffStatus + " Area=" + getiarea + " City=" +
                        geticity + " country=" + geticountry;

                getilatitudes = Double.parseDouble(getilatitude);
                getilatitudes = getilatitudes * 1E6;
                getilongitudes = Double.parseDouble(getilongitude);
                getilongitudes = getilongitudes * 1E6;


                if (fromeditquery != null && getkey != 0)
                {
                    myDatabase.execSQL("update Home set irental='" +
                            getiStuffRentalType + "', imisc ='" +
                            getiStuffMisc + "',irate='" + getiStuffRate +
                            "',istatus='" + getiStuffStatus + "',iarea='" +
                            getiarea + "',icity='" + geticity + "',icountry='" +
                            geticountry + "',urental='" + getuStuffRentalType +
                            "', umisc ='" + getuStuffMisc + "',urate='" +
                            getuStuffRate + "',ustatus='" + getuStuffStatus +
                            "',uarea='" + getuarea + "',ucity='" + getucity +
                            "',ucountry='" + getucountry + "',ilatitude = '" +
                            getilatitude + "',ilongitude='" + getilongitude +
                            "',ulatitude='" + getulatitude + "',ulongitude='" +
                            getulongitude +
                            "',queryDate=DATE('NOW') where key = " + getkey +
                            ";");
                    myDatabase.execSQL(
                            "update rentalposition set irentalposition=" +
                                    irentalposition + ", imiscposition=" +
                                    imiscposition + ", irateposition=" +
                                    irateposition + ", istatusposition=" +
                                    istatusposition + ", iarea='" + getiarea +
                                    "', icity='" + geticity + "', icountry='" +
                                    geticountry + "', ilatitude='" +
                                    getilatitude + "', ilongitude='" +
                                    getilongitude + "', urentalposition=" +
                                    urentalposition + ", umiscposition=" +
                                    umiscposition + ", urateposition=" +
                                    urateposition + ", ustatusposition=" +
                                    ustatusposition + ", uarea='" + getuarea +
                                    "', ucity='" + getucity + "', ucountry='" +
                                    getucountry + "', ulatitude='" +
                                    getulatitude + "', ulongitude='" +
                                    getulongitude + "' where key=" + getkey +
                                    ";");
                    myDatabase.execSQL("update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';");
                    myDatabase.execSQL("Update mStuffdetails set details='" +
                            idetails + "', latitude='" + getilatitudes +
                            "', longitude='" + getilongitudes +
                            "', location='" + geticountry +
                            "' where catagory='userRental';");
                }
                else
                {
                    try
                    {
                        myDatabase.execSQL(
                                "INSERT INTO Home (irental, imisc, irate, istatus, " +
                                        "iarea,icity,icountry,ilatitude,ilongitude," +
                                        "urental, umisc, urate," +
                                        "ustatus, uarea,ucity,ucountry,ulatitude,ulongitude, queryStatus) VALUES ('" +
                                        getiStuffRentalType + "','" +
                                        getiStuffMisc + "','" + getiStuffRate +
                                        "','" + getiStuffStatus + "','" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getuStuffRentalType + "','" +
                                        getuStuffMisc + "','" + getuStuffRate +
                                        "','" + getuStuffStatus + "','" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getulatitude +
                                        "','" + getulongitude + "','" + "true" +
                                        "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "INSERT INTO rentalposition (irentalposition , imiscposition , irateposition , istatusposition  ,iarea , icity , icountry , urentalposition , umiscposition , urateposition ,ustatusposition , uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype) VALUES (" +
                                        irentalposition + "," + imiscposition +
                                        "," + irateposition + "," +
                                        istatusposition + ",'" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "'," + urentalposition + "," +
                                        umiscposition + "," + urateposition +
                                        "," + ustatusposition + ",'" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getulatitude + "','" + getulongitude +
                                        "','" + "Rental" + "', '" + "istuff" +
                                        "');");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry +
                                        "' where catagory='userRental';");
                    }
                    catch (Exception exce)
                    {
                        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Home" +
                                " (key INTEGER PRIMARY KEY,irental VARCHAR, imisc VARCHAR, irate VARCHAR, " +
                                "istatus VARCHAR, iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR," +
                                "urental VARCHAR, umisc VARCHAR, urate VARCHAR," +
                                "ustatus VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, queryStatus VARCHAR,queryDate DATE);");
                        myDatabase.execSQL(
                                "CREATE TRIGGER insert_querydate_Home after INSERT on Home BEGIN update Home set queryDate=DATE('NOW') WHERE key=new.key; END;");
                        myDatabase.execSQL(
                                "CREATE TRIGGER delete_querydate_Home before insert on Home BEGIN delete from Home where queryDate<DATE('NOW','-7 day');END;");
                        myDatabase.execSQL(
                                "INSERT INTO Home (irental, imisc, irate, istatus, " +
                                        "iarea,icity,icountry,ilatitude,ilongitude," +
                                        "urental, umisc, urate," +
                                        "ustatus, uarea,ucity,ucountry,ulatitude,ulongitude, queryStatus) VALUES ('" +
                                        getiStuffRentalType + "','" +
                                        getiStuffMisc + "','" + getiStuffRate +
                                        "','" + getiStuffStatus + "','" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getuStuffRentalType + "','" +
                                        getuStuffMisc + "','" + getuStuffRate +
                                        "','" + getuStuffStatus + "','" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getulatitude +
                                        "','" + getulongitude + "','" + "true" +
                                        "');");
                        myDatabase.execSQL(
                                "CREATE TABLE IF NOT EXISTS rentalposition" +
                                        " (key INTEGER PRIMARY KEY,irentalposition NUMERIC, imiscposition NUMERIC, irateposition NUMERIC, istatusposition NUMERIC,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, urentalposition NUMERIC, umiscposition NUMERIC, urateposition NUMERIC,ustatusposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
                        myDatabase.execSQL(
                                "INSERT INTO rentalposition (irentalposition , imiscposition , irateposition , istatusposition  ,iarea , icity , icountry , urentalposition , umiscposition , urateposition ,ustatusposition , uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype) VALUES (" +
                                        irentalposition + "," + imiscposition +
                                        "," + irateposition + "," +
                                        istatusposition + ",'" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "'," + urentalposition + "," +
                                        umiscposition + "," + urateposition +
                                        "," + ustatusposition + ",'" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getulatitude + "','" + getulongitude +
                                        "','" + "Rental" + "', '" + "istuff" +
                                        "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry +
                                        "' where catagory='userRental';");
                    }
                }

                myDatabase.execSQL("drop table " + "temprental" + ";");
                Intent intent = new Intent(Home.this, FindandInstall.class);
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
                Intent intent1 = new Intent(Home.this, MapResults.class);
                startActivityForResult(intent1, 0);
                finish();
                break;

            case 2:
                Intent intent2 = new Intent(Home.this, FindandInstall.class);
                startActivityForResult(intent2, 0);
                finish();
                break;
            case 3:
                Intent intent3 = new Intent(Home.this, Settings.class);
                startActivityForResult(intent3, 0);
                finish();
                break;
            case 4:
                Intent intent = new Intent(Home.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
                finish();
                break;
            case 5:
                try
                {
                    Cursor c = myDatabase
                            .query("Home", null, null, null, null, null, null);
                    Intent listmStuff =
                            new Intent(Home.this, HomeListQuery.class);
                    startActivityForResult(listmStuff, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(Home.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                try
                {
                    Cursor c = myDatabase
                            .query("Home", null, null, null, null, null, null);
                    Intent deleteQuery =
                            new Intent(Home.this, DeleteQuery.class);
                    startActivityForResult(deleteQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Home.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 7:
                try
                {
                    Cursor c = myDatabase
                            .query("Home", null, null, null, null, null, null);
                    Intent viewQuery =
                            new Intent(Home.this, HomeViewQuery.class);
                    startActivityForResult(viewQuery, 0);
                    finish();

                }
                catch (Exception e)
                {
                    Toast.makeText(Home.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }

                break;

            case 8:
                Bundle b1 = new Bundle();
                Intent settheme = new Intent(Home.this, SettingTheme.class);
                b1.putString("value1", "Home");
                b1.putString("class", "7");
                settheme.putExtras(b1);
                startActivityForResult(settheme, 0);
                finish();
            case 9:
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", "Home");
                Intent mediaintent =
                        new Intent(Home.this, Uploadmultimedia.class);
                mediaintent.putExtras(B1);
                startActivity(mediaintent);
        }
        return super.onOptionsItemSelected(item);
    }
}
