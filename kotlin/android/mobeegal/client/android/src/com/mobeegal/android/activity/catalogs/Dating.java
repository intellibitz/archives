package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Dating.java 14 2008-08-19 06:36:45Z muthu.ramadoss                     $: Id of last commit
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
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

public class Dating
        extends Activity
{

    int key = 1;
    SQLiteDatabase myDatabase = null;
    String getistuffage;
    String getiStuffSex;
    String getiStuffHeight;
    String getiStuffWeight;
    String getuStuffAge;
    String getuStuffSex;
    String getuStuffHeight;
    String getuStuffWeight;
    String idetails;
    ArrayAdapter adapter;
    Spinner iStuffAge;
    RadioButton iStuffSex;
    Spinner iStuffHeight;
    Spinner iStuffWeight;
    Spinner ustuffage;
    RadioButton uStuffSex;
    Spinner uStuffHeight;
    Spinner uStuffWeight;
    RadioButton iStuffFemale;
    RadioButton uStuffFemale;
    int iageposition;
    int iheightposition;
    int iweightposition;
    int uageposition;
    int uheightposition;
    int uweightposition;
    int getiageposition;
    int getiheightposition;
    int getiweightposition;
    int getuageposition;
    int getuheightposition;
    int getuweightposition;
    String getisex;
    String getusex;
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
    double getilatitudes;
    double getilongitudes;
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

    //  String catalog;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        fromeditquery = this.getIntent().getExtras();
        if (fromeditquery != null)
        {
            getkey = fromeditquery.getInt("key");
        }
        // ToDo add your GUI initialization code here
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempdatingcursor = myDatabase
                    .query("tempdating", null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempdating" +
                    " (iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
            myDatabase.execSQL(
                    "INSERT INTO tempdating (iageposition, iheightposition, iweightposition, iarea, icity, icountry, uageposition, uheightposition, uweightposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                            iageposition + "," + iheightposition + "," +
                            iweightposition + ",'" + "" + "','" + "" + "','" +
                            "" + "'," + uageposition + "," + uheightposition +
                            "," + uweightposition + ",'" + "" + "','" + "" +
                            "','" + "" + "','" + "Male" + "','" + "Female" +
                            "','" + "" + "','" + "" + "','" + "" + "','" + "" +
                            "','" + "Dating" + "', '" + "istuff" + "');");
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
                    "catalog='Dating'", null, null, null, null);
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
        setContentView(R.layout.mstuff);
        final TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.iStuffprofile);
        one.setIndicator("Your Profile");
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.uStuff);
        two.setIndicator("Partner Profile");
        tabs.addTab(two);

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempdatingcursor = myDatabase
                    .query("tempdating", null, null, null, null, null, null);

            if (tempdatingcursor != null)
            {
                if (tempdatingcursor.isFirst())
                {
                    do
                    {
                        getiageposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "iageposition"));
                        getiheightposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "iheightposition"));
                        getiweightposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "iweightposition"));
                        getisex = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow("isex"));

                        getuageposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "uageposition"));
                        getuheightposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "uheightposition"));
                        getuweightposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "uweightposition"));
                        getusex = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow("usex"));

                        getiarea = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "iarea"));
                        geticity = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "icity"));
                        geticountry = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "icountry"));
                        getuarea = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "uarea"));
                        getucity = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "ucity"));
                        getucountry = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "ucountry"));
                        getilatitude = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "ilatitude"));
                        getilongitude = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "ilongitude"));
                        getulatitude = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "ulatitude"));
                        getulongitude = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "ulongitude"));
                        getcategory = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "category"));
                        getstufftype = tempdatingcursor.getString(
                                tempdatingcursor.getColumnIndexOrThrow(
                                        "stufftype"));
                    }
                    while (tempdatingcursor.moveToNext());
                }
            }
        }
        catch (Exception e)
        {
        }
        iStuffAge = (Spinner) findViewById(R.id.iStuffage);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
                this, R.array.iStuffage, android.R.layout.simple_spinner_item);
        iStuffAge.setAdapter(adapter1);
        iStuffAge.setSelection(getiageposition);
        iStuffAge.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getistuffage = (String) iStuffAge.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempdating set iageposition=" +
                                        position);
                        iageposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffSex = (RadioButton) findViewById(R.id.iStuffmaleradiobutton);
        iStuffFemale = (RadioButton) findViewById(R.id.iStufffemaleradiobutton);
        if (getisex.equals("Male"))
        {
            iStuffSex.setChecked(true);
        }
        else
        {
            iStuffSex.setChecked(false);
            iStuffFemale.setChecked(true);
        }

        iStuffSex.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                if (iStuffSex.isChecked())
                {
                    getisex = "Male";
                }
                else
                {
                    getisex = "Female";
                }
                myDatabase.execSQL(
                        "UPDATE tempdating set isex='" + getisex + "'");
            }
        });

        iStuffHeight = (Spinner) findViewById(R.id.iStuffheight);
        ArrayAdapter adapter3 = ArrayAdapter.createFromResource(
                this, R.array.iStuffheight,
                android.R.layout.simple_spinner_item);
        iStuffHeight.setAdapter(adapter3);
        iStuffHeight.setSelection(getiheightposition);
        iStuffHeight.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffHeight =
                                (String) iStuffHeight.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempdating set iheightposition=" +
                                        position);
                        iheightposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });
        iStuffWeight = (Spinner) findViewById(R.id.iStuffweight);
        ArrayAdapter adapter4 = ArrayAdapter.createFromResource(
                this, R.array.iStuffweight,
                android.R.layout.simple_spinner_item);
        iStuffWeight.setAdapter(adapter4);
        iStuffWeight.setSelection(getiweightposition);
        iStuffWeight.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffWeight =
                                (String) iStuffWeight.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempdating set iweightposition=" +
                                        position);
                        iweightposition = position;

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
        ustuffage = (Spinner) findViewById(R.id.agespinner1);
        adapter = ArrayAdapter.createFromResource(
                this, R.array.age, android.R.layout.simple_spinner_item);
        ustuffage.setAdapter(adapter);
        ustuffage.setSelection(getuageposition);
        ustuffage.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffAge = (String) ustuffage.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempdating set uageposition=" +
                                        position);
                        uageposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        uStuffSex = (RadioButton) findViewById(R.id.uStufffemaleradiobutton);
        uStuffFemale = (RadioButton) findViewById(R.id.uStuffmaleradiobutton);
        if (getusex.equals("Female"))
        {
            uStuffSex.setChecked(true);
        }
        else
        {
            uStuffSex.setChecked(false);
            uStuffFemale.setChecked(true);
        }

        uStuffSex.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                if (uStuffSex.isChecked())
                {
                    getusex = "Female";

                }
                else
                {
                    getusex = "Male";
                }
                myDatabase.execSQL(
                        "UPDATE tempdating set usex='" + getusex + "'");
            }
        });

        uStuffHeight = (Spinner) findViewById(R.id.uheight);
        adapter = ArrayAdapter.createFromResource(this, R.array.height,
                android.R.layout.simple_spinner_item);
        uStuffHeight.setAdapter(adapter);
        uStuffHeight.setSelection(getuheightposition);
        uStuffHeight.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffHeight =
                                (String) uStuffHeight.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempdating set uheightposition=" +
                                        position);
                        uheightposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        uStuffWeight = (Spinner) findViewById(R.id.uweight);
        adapter = ArrayAdapter.createFromResource(this, R.array.weight,
                android.R.layout.simple_spinner_item);
        uStuffWeight.setAdapter(adapter);
        uStuffWeight.setSelection(getuweightposition);
        uStuffWeight.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffWeight =
                                (String) uStuffWeight.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempdating set uweightposition=" +
                                        position);
                        uweightposition = position;
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
                        "UPDATE tempdating set stufftype='" + "istuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Dating.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempdating");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Dating.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempdating");
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
                        "UPDATE tempdating set stufftype='" + "ustuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Dating.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempdating");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Dating.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempdating");
                    b.putInt("key", getkey);
                    textview.putExtras(b);
                    startActivityForResult(textview, 0);
                }
            }
        });

        final Button button = (Button) findViewById(R.id.Save);
        button.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View v)
            {
                getistuffage = (String) iStuffAge.getSelectedItem();
                getiStuffHeight = (String) iStuffHeight.getSelectedItem();
                getiStuffWeight = (String) iStuffWeight.getSelectedItem();
                iageposition = iStuffAge.getSelectedItemPosition();
                iheightposition = iStuffHeight.getSelectedItemPosition();
                iweightposition = iStuffWeight.getSelectedItemPosition();
                idetails = "DatingIcon \n Age=" + getistuffage + " Sex=" +
                        getisex + " Height=" + getiStuffHeight + " Weight=" +
                        getiStuffWeight + " Area=" + getiarea + " City=" +
                        geticity + " Country=" + geticountry;
                getilatitudes = Double.parseDouble(getilatitude);
                getilatitudes = getilatitudes * 1E6;
                getilongitudes = Double.parseDouble(getilongitude);
                getilongitudes = getilongitudes * 1E6;

                if (fromeditquery != null && getkey != 0)
                {
                    myDatabase.execSQL(
                            "UPDATE datingposition set iageposition=" +
                                    iageposition + ", iheightposition=" +
                                    iheightposition + ", iweightposition=" +
                                    iweightposition + ", iarea='" + getiarea +
                                    "', icity='" + geticity + "', icountry='" +
                                    geticountry + "', uageposition=" +
                                    uageposition + ", uheightposition=" +
                                    uheightposition + ", uweightposition=" +
                                    uweightposition + ", uarea='" + getuarea +
                                    "', ucity='" + getucity + "', ucountry='" +
                                    getucountry + "', isex='" + getisex +
                                    "', usex='" + getusex + "', ilatitude='" +
                                    getilatitude + "', ilongitude='" +
                                    getilongitude + "',ulatitude='" +
                                    getulatitude + "',ulongitude='" +
                                    getulongitude + "' where key=" + getkey +
                                    ";");
                    myDatabase.execSQL("UPDATE Dating set iage='" +
                            getistuffage + "', isex='" + getisex +
                            "', iheight='" + getiStuffHeight + "', iweight='" +
                            getiStuffWeight + "', iarea='" + getiarea +
                            "', icity='" + geticity + "', icountry='" +
                            geticountry + "', uage='" + getuStuffAge +
                            "', usex='" + getusex + "', uheight='" +
                            getuStuffHeight + "', uweight='" + getuStuffWeight +
                            "', uarea='" + getuarea + "', ucity='" + getucity +
                            "', ucountry='" + getucountry + "', ilatitude='" +
                            getilatitude + "', ilongitude='" + getilongitude +
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
                            "' where catagory='" + "userDating" + "';");
                }
                else
                {
                    try
                    {
                        myDatabase.execSQL(
                                "INSERT INTO Dating (iage, isex, iheight, iweight, " +
                                        "iarea,icity,icountry,ilatitude,ilongitude," +
                                        "uage, usex, uheight," +
                                        "uweight, uarea,ucity,ucountry,ulatitude,ulongitude, queryStatus) VALUES ('" +
                                        getistuffage + "','" + getisex + "','" +
                                        getiStuffHeight + "','" +
                                        getiStuffWeight + "','" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getuStuffAge +
                                        "','" + getusex + "','" +
                                        getuStuffHeight + "','" +
                                        getuStuffWeight + "','" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getulatitude + "','" +
                                        getulongitude + "','" + "true" + "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry + "' where catagory='" +
                                        "userDating" + "';");
                        myDatabase.execSQL(
                                "INSERT INTO datingposition (iageposition, iheightposition, iweightposition, iarea, icity, icountry, uageposition, uheightposition, uweightposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                        iageposition + "," + iheightposition +
                                        "," + iweightposition + ",'" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "'," + uageposition +
                                        "," + uheightposition + "," +
                                        uweightposition + ",'" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getisex + "','" + getusex +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getulatitude +
                                        "','" + getulongitude + "','" +
                                        "Dating" + "', '" + "istuff" + "');");
                    }
                    catch (Exception exce)
                    {
                        //Toast.makeText(Dating.this, idetails, Toast.LENGTH_LONG).show();
                        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Dating" +
                                " (key INTEGER PRIMARY KEY,iage VARCHAR, isex VARCHAR, iheight VARCHAR, " +
                                "iweight VARCHAR, iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR," +
                                "uage VARCHAR, usex VARCHAR, uheight VARCHAR," +
                                "uweight VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, queryStatus VARCHAR,queryDate DATE);");
                        myDatabase.execSQL(
                                "CREATE TRIGGER insert_querydate_Dating after INSERT on Dating BEGIN update Dating set queryDate=DATE('NOW') WHERE key=new.key; END;");
                        myDatabase.execSQL(
                                "CREATE TRIGGER delete_querydate_Dating before insert on Dating BEGIN delete from Dating where queryDate<DATE('NOW','-7 day');END;");
                        myDatabase.execSQL(
                                "INSERT INTO Dating (iage, isex, iheight, iweight, " +
                                        "iarea,icity,icountry,ilatitude,ilongitude," +
                                        "uage, usex, uheight," +
                                        "uweight, uarea,ucity,ucountry,ulatitude,ulongitude, queryStatus) VALUES ('" +
                                        getistuffage + "','" + getisex + "','" +
                                        getiStuffHeight + "','" +
                                        getiStuffWeight + "','" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getuStuffAge +
                                        "','" + getusex + "','" +
                                        getuStuffHeight + "','" +
                                        getuStuffWeight + "','" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getulatitude + "','" +
                                        getulongitude + "','" + "true" + "');");
                        myDatabase.execSQL(
                                "CREATE TABLE IF NOT EXISTS datingposition" +
                                        " (key INTEGER PRIMARY KEY,iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
                        myDatabase.execSQL(
                                "INSERT INTO datingposition (iageposition, iheightposition, iweightposition, iarea, icity, icountry, uageposition, uheightposition, uweightposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                        iageposition + "," + iheightposition +
                                        "," + iweightposition + ",'" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "'," + uageposition +
                                        "," + uheightposition + "," +
                                        uweightposition + ",'" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getisex + "','" + getusex +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getulatitude +
                                        "','" + getulongitude + "','" +
                                        "Dating" + "', '" + "istuff" + "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry + "' where catagory='" +
                                        "userDating" + "';");
                    }
                }
                myDatabase.execSQL("drop table " + "tempdating" + ";");
                Intent intent = new Intent(Dating.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
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
                Intent intent1 = new Intent(Dating.this, MapResults.class);
                startActivityForResult(intent1, 0);
                finish();
                break;

            case 2:
                Intent intent2 = new Intent(Dating.this, FindandInstall.class);
                startActivityForResult(intent2, 0);
                finish();
                break;
            case 3:
                Intent intent3 = new Intent(Dating.this, Settings.class);
                startActivityForResult(intent3, 0);
                finish();
                break;
            case 4:
                Intent intent = new Intent(Dating.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
                break;
            case 5:
                try
                {
                    Cursor c = myDatabase.query("Dating", null, null, null,
                            null, null, null);
                    Intent listmStuff =
                            new Intent(Dating.this, ListQuery.class);
                    startActivityForResult(listmStuff, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(Dating.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                try
                {
                    Cursor c = myDatabase.query("Dating", null, null, null,
                            null, null, null);
                    Intent deleteQuery =
                            new Intent(Dating.this, DeleteQuery.class);
                    startActivityForResult(deleteQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Dating.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 7:
                try
                {
                    Cursor c = myDatabase.query("Dating", null, null, null,
                            null, null, null);
                    Intent viewQuery = new Intent(Dating.this, ViewQuery.class);
                    startActivityForResult(viewQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Dating.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 8:
                Bundle b1 = new Bundle();
                Intent settheme = new Intent(Dating.this, SettingTheme.class);
                b1.putString("value1", "Dating");
                b1.putString("class", "3");
                settheme.putExtras(b1);
                startActivityForResult(settheme, 0);
                finish();
            case 9:
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", "Dating");
                Intent mediaintent =
                        new Intent(Dating.this, Uploadmultimedia.class);
                mediaintent.putExtras(B1);
                startActivity(mediaintent);
        }
        return super.onOptionsItemSelected(item);
    }
}
