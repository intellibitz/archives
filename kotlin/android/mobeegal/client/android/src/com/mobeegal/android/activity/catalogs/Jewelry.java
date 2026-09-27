package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Jewelry.java 14 2008-08-19 06:36:45Z muthu.ramadoss                    $: Id of last commit
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

import java.util.ArrayList;

public class Jewelry
        extends Activity
{

    String getiStuffitem;
    String getiStuffgender;
    String getiStuffstonetype;
    String getiStuffmetaltype;
    String getiStuffWeight;
    String getiStuffCountry;
    String getuStuffgender;
    String getuStuffstonetype;
    String getuStuffmetaltype;
    String getuStuffitem;
    String getuStuffCity;
    String getuStuffCountry;
    String getuStuffArea;
    String getuStuffWeight;
    int ijewelryposition;
    int igenderposition;
    int istoneposition;
    int imetalposition;
    int iweightposition;
    int ujewelryposition;
    int ugenderposition;
    int ustoneposition;
    int umetalposition;
    int uweightposition;
    int getijewelryposition;
    int getigenderposition;
    int getistoneposition;
    int getimetalposition;
    int getiweightposition;
    int getujewelryposition;
    int getugenderposition;
    int getustoneposition;
    int getumetalposition;
    int getuweightposition;
    String iarea;
    String icity;
    String icountry;
    String uarea;
    String ucity;
    String ucountry;
    String ilatitude;
    String ilongitude;
    String ulatitude;
    String ulongitude;
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
    TextView iareatextview;
    TextView icitytextview;
    TextView icountrytextview;
    TextView uareatextview;
    TextView ucitytextview;
    TextView ucountrytextview;
    Spinner iStuffitem;
    Spinner iStuffgender;
    Spinner iStuffmetaltype;
    Spinner iStuffstone;
    Spinner iStuffWeight;
    Spinner uStuffitem;
    Spinner uStuffgender;
    Spinner uStuffmetaltype;
    Spinner uStuffstone;
    Spinner uStuffweight;
    Spinner uStuffWeight;
    int key = 1;
    Bundle fromeditquery;
    int getkey;
    int theme;
    String catalog;
    final ArrayList results = new ArrayList();
    ArrayAdapter<CharSequence> adapter;
    SQLiteDatabase myDatabase = null;
    private String viewtypename;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor themecursor = myDatabase.query("Theme", null,
                    "catalog='Jewelry'", null, null, null, null);
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

        setContentView(R.layout.jewelry);
        fromeditquery = this.getIntent().getExtras();
        if (fromeditquery != null)
        {
            getkey = fromeditquery.getInt("key");
        }
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempdatingcursor = myDatabase
                    .query("tempjewelry", null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempjewelry" +
                    "(ijewelryposition NUMERIC, igenderposition NUMERIC, istoneposition NUMERIC, imetalposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ujewelryposition NUMERIC, ugenderposition NUMERIC, ustoneposition NUMERIC, umetalposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
            myDatabase.execSQL(
                    "INSERT INTO tempjewelry (ijewelryposition, igenderposition, istoneposition, imetalposition, iweightposition, iarea, icity,icountry, ujewelryposition, ugenderposition, ustoneposition, umetalposition, uweightposition, uarea, ucity, ucountry, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                            ijewelryposition + "," + igenderposition + "," +
                            istoneposition + "," + imetalposition + "," +
                            iweightposition + ",'" + iarea + "','" + icity +
                            "','" + icountry + "'," + ujewelryposition + "," +
                            ugenderposition + "," + ustoneposition + "," +
                            umetalposition + "," + uweightposition + ",'" +
                            uarea + "','" + ucity + "','" + ucountry + "','" +
                            ilatitude + "','" + ilongitude + "','" + ulatitude +
                            "','" + ulongitude + "','" + "Jewelry" + "','" +
                            "istuff" + "');");
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
            Cursor tempdatingcursor = myDatabase
                    .query("tempjewelry", null, null, null, null, null, null);

            if (tempdatingcursor != null)
            {
                if (tempdatingcursor.isFirst())
                {
                    do
                    {
                        getijewelryposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ijewelryposition"));
                        getigenderposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "igenderposition"));
                        getistoneposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "istoneposition"));
                        getimetalposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "imetalposition"));
                        getiweightposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "iweightposition"));

                        getujewelryposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ujewelryposition"));
                        getugenderposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ugenderposition"));
                        getustoneposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ustoneposition"));
                        getumetalposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "umetalposition"));
                        getuweightposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "uweightposition"));

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

        //Creating a tab1 for ISTUFF

        TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.iStuffprofile);
        one.setIndicator("Merchant");

        iStuffitem = (Spinner) findViewById(R.id.iStuffitemtype);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                this, R.array.jewelrytype,
                android.R.layout.simple_spinner_item);
        iStuffitem.setAdapter(adapter1);
        iStuffitem.setSelection(getijewelryposition);
        iStuffitem.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffitem = (String) iStuffitem.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set ijewelryposition=" +
                                        position);
                        ijewelryposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffgender = (Spinner) findViewById(R.id.iStuffgender);
        ArrayAdapter<CharSequence> adaptergen = ArrayAdapter.createFromResource(
                this, R.array.jewelrygender,
                android.R.layout.simple_spinner_item);

        iStuffgender.setAdapter(adaptergen);
        iStuffgender.setSelection(getigenderposition);
        iStuffgender.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffgender =
                                (String) iStuffgender.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set igenderposition=" +
                                        position);
                        igenderposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffstone = (Spinner) findViewById(R.id.iStuffstonetype);
        ArrayAdapter<CharSequence> adapterstone =
                ArrayAdapter.createFromResource(
                        this, R.array.stonetype,
                        android.R.layout.simple_spinner_item);
        iStuffstone.setAdapter(adapterstone);
        iStuffstone.setSelection(getistoneposition);
        iStuffstone.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffstonetype =
                                (String) iStuffstone.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set istoneposition=" +
                                        position);
                        istoneposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffmetaltype = (Spinner) findViewById(R.id.iStuffmetaltype);
        ArrayAdapter<CharSequence> adaptermetal =
                ArrayAdapter.createFromResource(
                        this, R.array.metaltype,
                        android.R.layout.simple_spinner_item);

        iStuffmetaltype.setAdapter(adaptermetal);
        iStuffmetaltype.setSelection(getimetalposition);
        iStuffmetaltype.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffmetaltype =
                                (String) iStuffmetaltype.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set imetalposition=" +
                                        position);
                        imetalposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffWeight = (Spinner) findViewById(R.id.iStuffweight);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.istuffweightingram,
                android.R.layout.simple_spinner_item);
        iStuffWeight.setAdapter(adapter2);
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
                                "UPDATE tempjewelry set iweightposition=" +
                                        position);
                        iweightposition = position;

                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iareatextview = (TextView) findViewById(R.id.iarea);
        iareatextview.setText(getiarea);
        icitytextview = (TextView) findViewById(R.id.icity);
        icitytextview.setText(geticity);
        icountrytextview = (TextView) findViewById(R.id.icountry);
        icountrytextview.setText(geticountry);
        uareatextview = (TextView) findViewById(R.id.uarea);
        uareatextview.setText(getuarea);
        ucitytextview = (TextView) findViewById(R.id.ucity);
        ucitytextview.setText(getucity);
        ucountrytextview = (TextView) findViewById(R.id.ucountry);
        ucountrytextview.setText(getucountry);
        tabs.addTab(one);

        //Creating a tab2 for USTUFF

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.uStuffprofile);
        two.setIndicator("Customer");
        uStuffitem = (Spinner) findViewById(R.id.uStuffitemtype);
        ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(
                this, R.array.jewelrytype,
                android.R.layout.simple_spinner_item);
        uStuffitem.setAdapter(adapter6);
        uStuffitem.setSelection(getujewelryposition);
        uStuffitem.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffitem = (String) uStuffitem.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set ujewelryposition=" +
                                        position);
                        ujewelryposition = position;

                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        // =================== attributes added for ustuff ======================//

        uStuffgender = (Spinner) findViewById(R.id.uStuffgender);
        ArrayAdapter<CharSequence> adaptergen1 =
                ArrayAdapter.createFromResource(
                        this, R.array.jewelrygender,
                        android.R.layout.simple_spinner_item);

        uStuffgender.setAdapter(adaptergen1);
        uStuffgender.setSelection(getugenderposition);
        uStuffgender.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffgender =
                                (String) uStuffgender.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set ugenderposition=" +
                                        position);
                        ugenderposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        uStuffstone = (Spinner) findViewById(R.id.uStuffstonetype);
        ArrayAdapter<CharSequence> adapterstone1 =
                ArrayAdapter.createFromResource(
                        this, R.array.stonetype,
                        android.R.layout.simple_spinner_item);
        uStuffstone.setAdapter(adapterstone1);
        uStuffstone.setSelection(getustoneposition);
        uStuffstone.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffstonetype =
                                (String) uStuffstone.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set ustoneposition=" +
                                        position);
                        ustoneposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        uStuffmetaltype = (Spinner) findViewById(R.id.uStuffmetaltype);
        ArrayAdapter<CharSequence> adaptermetal1 =
                ArrayAdapter.createFromResource(
                        this, R.array.metaltype,
                        android.R.layout.simple_spinner_item);
        uStuffmetaltype.setAdapter(adaptermetal1);
        uStuffmetaltype.setSelection(getumetalposition);
        uStuffmetaltype.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffmetaltype =
                                (String) uStuffmetaltype.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempjewelry set umetalposition=" +
                                        position);
                        umetalposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });


        uStuffWeight = (Spinner) findViewById(R.id.uStuffweight);
        ArrayAdapter<CharSequence> adapter7 = ArrayAdapter.createFromResource(
                this, R.array.ustuffweightingram,
                android.R.layout.simple_spinner_item);
        uStuffWeight.setAdapter(adapter7);
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
                                "UPDATE tempjewelry set uweightposition=" +
                                        position);
                        uweightposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });
        tabs.addTab(two);
        if (getstufftype.equals("istuff"))
        {
            tabs.setCurrentTab(0);
        }
        else
        {
            tabs.setCurrentTab(1);
        }

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
                        "UPDATE tempjewelry set stufftype='" + "istuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Jewelry.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempjewelry");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Jewelry.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempjewelry");
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
                        "UPDATE tempjewelry set stufftype='" + "ustuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Jewelry.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempjewelry");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Jewelry.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempjewelry");
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
                getiStuffitem = (String) iStuffitem.getSelectedItem();
                getiStuffgender = (String) iStuffgender.getSelectedItem();
                getiStuffstonetype = (String) iStuffstone.getSelectedItem();
                getiStuffmetaltype = (String) iStuffmetaltype.getSelectedItem();
                getiStuffWeight = (String) iStuffWeight.getSelectedItem();

                ijewelryposition = iStuffitem.getSelectedItemPosition();
                igenderposition = iStuffgender.getSelectedItemPosition();
                istoneposition = iStuffstone.getSelectedItemPosition();
                imetalposition = iStuffmetaltype.getSelectedItemPosition();
                iweightposition = iStuffWeight.getSelectedItemPosition();

                idetails = "jewelryitem=" + getiStuffitem + " gender=" +
                        getiStuffgender + " stone=" + getiStuffstonetype +
                        " metal=" + getiStuffmetaltype + " weight=" +
                        getiStuffWeight + " Area=" + getiarea + " City=" +
                        geticity + " country=" + geticountry;

                getilatitudes = Double.parseDouble(getilatitude);
                getilatitudes = getilatitudes * 1E6;
                getilongitudes = Double.parseDouble(getilongitude);
                getilongitudes = getilongitudes * 1E6;


                if (fromeditquery != null && getkey != 0)
                {
                    myDatabase.execSQL(
                            "UPDATE JewelryPosition set ijewelryposition=" +
                                    ijewelryposition + ", igenderposition=" +
                                    igenderposition + ", istoneposition=" +
                                    istoneposition + ", imetalposition=" +
                                    imetalposition + ", iweightposition=" +
                                    iweightposition + ", iarea='" + getiarea +
                                    "', icity='" + geticity + "', icountry='" +
                                    geticountry + "', ujewelryposition=" +
                                    ujewelryposition + ", ugenderposition=" +
                                    ugenderposition + ", ustoneposition=" +
                                    ustoneposition + ", umetalposition=" +
                                    umetalposition + ", uweightposition=" +
                                    uweightposition + ",uarea='" + getuarea +
                                    "', ucity='" + getucity + "', ucountry='" +
                                    getucountry + "', ilatitude='" +
                                    getilatitude + "', ilongitude='" +
                                    getilongitude + "',ulatitude='" +
                                    getulatitude + "',ulongitude='" +
                                    getulongitude + "' where key=" + getkey +
                                    ";");
                    myDatabase.execSQL("UPDATE Jewelry set ijewelry='" +
                            getiStuffitem + "', igender='" + getiStuffgender +
                            "', istone='" + getiStuffstonetype + "', imetal='" +
                            getiStuffmetaltype + "', iweight='" +
                            getiStuffWeight + "', iarea='" + getiarea +
                            "', icity='" + geticity + "', icountry='" +
                            geticountry + "', ujewelry='" + getuStuffitem +
                            "', ugender='" + getuStuffgender + "', ustone='" +
                            getuStuffstonetype + "', umetal='" +
                            getuStuffmetaltype + "',uweight='" +
                            getuStuffWeight + "', uarea='" + getuarea +
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
                            "' where catagory='" + "userJewelry" + "';");
                }
                else
                {
                    try
                    {
                        myDatabase.execSQL(
                                "INSERT INTO Jewelry (ijewelry,igender,istone ,imetal ,iweight,iarea,icity,icountry,ilatitude, ilongitude,ujewelry,ugender ,ustone ,umetal,uweight,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES ('" +
                                        getiStuffitem + "','" +
                                        getiStuffgender + "','" +
                                        getiStuffstonetype + "','" +
                                        getiStuffmetaltype + "','" +
                                        getiStuffWeight + "','" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getuStuffitem +
                                        "','" + getuStuffgender + "','" +
                                        getuStuffstonetype + "','" +
                                        getuStuffmetaltype + "','" +
                                        getuStuffWeight + "','" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getulatitude + "','" +
                                        getulongitude + "','" + "true" + "');");
                        myDatabase.execSQL(
                                "INSERT INTO JewelryPosition (ijewelryposition, igenderposition, istoneposition, imetalposition, iweightposition, iarea, icity,icountry, ujewelryposition, ugenderposition, ustoneposition, umetalposition, uweightposition, uarea, ucity, ucountry, ilatitude, ilongitude, ulatitude, ulongitude) VALUES (" +
                                        ijewelryposition + "," +
                                        igenderposition + "," + istoneposition +
                                        "," + imetalposition + "," +
                                        iweightposition + ",'" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "'," + ujewelryposition + "," +
                                        ugenderposition + "," + ustoneposition +
                                        "," + umetalposition + "," +
                                        uweightposition + ",'" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getulatitude +
                                        "','" + getulongitude + "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry + "' where catagory='" +
                                        "userJewelry" + "';");
                    }
                    catch (Exception e)
                    {
                        myDatabase
                                .execSQL("CREATE TABLE IF NOT EXISTS Jewelry" +
                                        " (key INTEGER PRIMARY KEY,ijewelry VARCHAR,igender VARCHAR,istone VARCHAR,imetal VARCHAR, iweight VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,ujewelry VARCHAR,ugender VARCHAR,ustone VARCHAR,umetal VARCHAR,uweight VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
                        myDatabase.execSQL(
                                "CREATE TRIGGER insert_querydate_Jewelry after INSERT on Jewelry BEGIN update Jewelry set queryDate=DATE('NOW') WHERE key=new.key; END;");
                        myDatabase.execSQL(
                                "CREATE TRIGGER delete_querydate_Jewelry before insert on Jewelry BEGIN delete from Jewelry where queryDate<DATE('NOW','-7 day');END;");
                        myDatabase.execSQL(
                                "INSERT INTO Jewelry (ijewelry,igender,istone ,imetal ,iweight,iarea,icity,icountry,ilatitude, ilongitude,ujewelry,ugender ,ustone ,umetal,uweight,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES ('" +
                                        getiStuffitem + "','" +
                                        getiStuffgender + "','" +
                                        getiStuffstonetype + "','" +
                                        getiStuffmetaltype + "','" +
                                        getiStuffWeight + "','" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getuStuffitem +
                                        "','" + getuStuffgender + "','" +
                                        getuStuffstonetype + "','" +
                                        getuStuffmetaltype + "','" +
                                        getuStuffWeight + "','" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getulatitude + "','" +
                                        getulongitude + "','" + "true" + "');");
                        myDatabase.execSQL(
                                "CREATE TABLE IF NOT EXISTS JewelryPosition" +
                                        "(key INTEGER PRIMARY KEY, ijewelryposition NUMERIC, igenderposition NUMERIC, istoneposition NUMERIC, imetalposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ujewelryposition NUMERIC, ugenderposition NUMERIC, ustoneposition NUMERIC, umetalposition NUMERIC, uweightposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR);");
                        myDatabase.execSQL(
                                "INSERT INTO JewelryPosition (ijewelryposition, igenderposition, istoneposition, imetalposition, iweightposition, iarea, icity,icountry, ujewelryposition, ugenderposition, ustoneposition, umetalposition, uweightposition, uarea, ucity, ucountry, ilatitude, ilongitude, ulatitude, ulongitude) VALUES (" +
                                        ijewelryposition + "," +
                                        igenderposition + "," + istoneposition +
                                        "," + imetalposition + "," +
                                        iweightposition + ",'" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "'," + ujewelryposition + "," +
                                        ugenderposition + "," + ustoneposition +
                                        "," + umetalposition + "," +
                                        uweightposition + ",'" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" + getulatitude +
                                        "','" + getulongitude + "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry + "' where catagory='" +
                                        "userJewelry" + "';");
                    }
                }

                myDatabase.execSQL("drop table " + "tempjewelry" + ";");
                Intent intent = new Intent(Jewelry.this, FindandInstall.class);
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
                Intent intent1 = new Intent(Jewelry.this, MapResults.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 2:
                Intent intentToFindandinstall =
                        new Intent(Jewelry.this, FindandInstall.class);
                startActivityForResult(intentToFindandinstall, 0);
                finish();
                break;

            case 3:
                Intent intent3 = new Intent(Jewelry.this, Settings.class);
                startActivityForResult(intent3, 0);
                finish();
                break;
            case 4:

                Intent intent = new Intent(Jewelry.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
                break;
            case 5:
                try
                {
                    Cursor c = myDatabase.query("Jewelry", null, null, null,
                            null, null, null);
                    Intent listmStuff =
                            new Intent(Jewelry.this, Jewelrylistquery.class);
                    startActivityForResult(listmStuff, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(Jewelry.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case 6:
                try
                {
                    Cursor c = myDatabase.query("Jewelry", null, null, null,
                            null, null, null);
                    Intent jewelrydeleteQuery =
                            new Intent(Jewelry.this, Jewelrydeletequery.class);
                    startActivityForResult(jewelrydeleteQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Jewelry.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case 7:
                try
                {
                    Cursor c = myDatabase.query("Jewelry", null, null, null,
                            null, null, null);
                    Intent jewelryviewQuery =
                            new Intent(Jewelry.this, Jewelryviewquery.class);
                    startActivityForResult(jewelryviewQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Jewelry.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case 8:
                Bundle b1 = new Bundle();
                Intent settheme = new Intent(Jewelry.this, SettingTheme.class);
                b1.putString("value1", "Jewelry");
                b1.putString("class", "1");
                settheme.putExtras(b1);
                startActivityForResult(settheme, 0);
                finish();
            case 9:
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", "Jewelry");
                Intent mediaintent =
                        new Intent(Jewelry.this, Uploadmultimedia.class);
                mediaintent.putExtras(B1);
                startActivity(mediaintent);

        }
        return super.onOptionsItemSelected(item);

    }
}

