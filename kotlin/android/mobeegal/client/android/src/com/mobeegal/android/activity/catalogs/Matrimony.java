package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Matrimony.java 14 2008-08-19 06:36:45Z muthu.ramadoss                  $: Id of last commit
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

public class Matrimony
        extends Activity
{

    String getiStuffAge;
    String getiStuffSex;
    String getiStuffHeight;
    String getiStuffWeight;
    String getiStuffCountry;
    String getiStuffCity;
    String getiStuffArea;
    String getiStuffColor;
    String getiStufflatitude = null;
    String getiStufflongitude = null;
    String getuStuffAge;
    String getuStuffSex;
    String getuStuffHeight;
    String getuStuffWeight;
    String getuStuffColor;
    String getuStuffCountry;
    String getuStuffCity;
    String getuStuffArea;
    String getiStuffReligion;
    String getiStuffCaste;
    String getuStuffReligion;
    String getuStuffCaste;
    String getuStufflatitude = null;
    String getuStufflongitude = null;
    int key = 1;
    //int count, j;
    int ireligionposition;
    int icasteposition;
    int iageposition;
    int iheightposition;
    int iweightposition;
    int icolorposition;
    int ureligionposition;
    int ucasteposition;
    int uageposition;
    int uheightposition;
    int uweightposition;
    int ucolorposition;
    SQLiteDatabase myDatabase = null;
    int getireligionposition;
    int geticasteposition;
    int geticolorposition;
    int getureligionposition;
    int getucasteposition;
    int getucolorposition;
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
    TextView iarea;
    TextView icity;
    TextView icountry;
    TextView uarea;
    TextView ucity;
    TextView ucountry;
    public String value1;
    public String v2;
    Spinner iStuffReligion;
    Spinner iStuffCaste;
    Spinner iStuffAge;
    Spinner iStuffHeight;
    Spinner iStuffWeight;
    Spinner iStuffColor;
    Bundle fromeditquery;
    int getkey;
    int theme;
    String catalog;
    String idetails;
    double getilatitudes;
    double getilongitudes;
    private int[] religion = {R.array.istuffhindu, R.array.istuffmuslim,
            R.array.iStuffChrstian, R.array.istuffsikh};
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
                    "catalog='Matrimony'", null, null, null, null);
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
        setContentView(R.layout.matrimony);

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempcursor = myDatabase
                    .query("tempmatrimony", null, null, null, null, null, null);
        }
        catch (Exception e)
        {
            //Toast.makeText(Dating.this, "Database not found", Toast.LENGTH_SHORT).show();
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempmatrimony" +
                    " (ireligionposition NUMERIC, icasteposition NUMERIC, iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, icolorposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ureligionposition NUMERIC, ucasteposition NUMERIC, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, ucolorposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");
            myDatabase.execSQL(
                    "INSERT INTO tempmatrimony (ireligionposition, icasteposition, iageposition, iheightposition, iweightposition, icolorposition, iarea, icity, icountry, ureligionposition, ucasteposition, uageposition, uheightposition, uweightposition, ucolorposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                            ireligionposition + "," + icasteposition + "," +
                            iageposition + "," + iheightposition + "," +
                            iweightposition + "," + icolorposition + ",'" + "" +
                            "','" + "" + "','" + "" + "'," + ureligionposition +
                            "," + ucasteposition + "," + uageposition + "," +
                            uheightposition + "," + uweightposition + "," +
                            ucolorposition + ",'" + "" + "','" + "" + "','" +
                            "" + "','" + "Male" + "','" + "Female" + "','" +
                            "" + "','" + "" + "','" + "" + "','" + "" + "','" +
                            "Matrimony" + "', '" + "istuff" + "');");
        }
        /*finally {
        if (myDatabase != null) {
        myDatabase.close();
        }
        }*/
        fromeditquery = this.getIntent().getExtras();
        if (fromeditquery != null)
        {
            getkey = fromeditquery.getInt("key");
        }
        final TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.iStuffprofile);
        one.setIndicator("Your Profile");
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.uStuffprofile);
        two.setIndicator("Partner profile");
        tabs.addTab(two);

        try
        {
            // myDatabase = this.openDatabase("Mobeegal", null);
            Cursor tempdatingcursor = myDatabase
                    .query("tempmatrimony", null, null, null, null, null, null);

            if (tempdatingcursor != null)
            {
                if (tempdatingcursor.isFirst())
                {
                    do
                    {
                        getireligionposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ireligionposition"));
                        geticasteposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "icasteposition"));
                        geticolorposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "icolorposition"));
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
                        getureligionposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ureligionposition"));
                        getucasteposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ucasteposition"));
                        getucolorposition = tempdatingcursor
                                .getInt(tempdatingcursor.getColumnIndexOrThrow(
                                        "ucolorposition"));
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
        iStuffReligion = (Spinner) findViewById(R.id.iStuffreligion);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
                this, R.array.istuffreligion,
                android.R.layout.simple_spinner_item);
        iStuffReligion.setAdapter(adapter1);
        iStuffReligion.setSelection(getireligionposition);
        iStuffReligion.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffReligion =
                                (String) iStuffReligion.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set ireligionposition=" +
                                        position);
                        ireligionposition = position;
                        //added line
                        final Spinner istuffcaste =
                                (Spinner) findViewById(R.id.iStuffcaste);
                        ArrayAdapter casteadapter = ArrayAdapter
                                .createFromResource(Matrimony.this,
                                        religion[position],
                                        android.R.layout.simple_spinner_item);
                        istuffcaste.setAdapter(casteadapter);
                        istuffcaste.setSelection(geticasteposition);
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffCaste = (Spinner) findViewById(R.id.iStuffcaste);
        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(
                this, R.array.istuffhindu,
                android.R.layout.simple_spinner_item);
        iStuffCaste.setAdapter(adapter2);
        iStuffCaste.setSelection(geticasteposition);
        iStuffCaste.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffCaste = (String) iStuffCaste.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set icasteposition=" +
                                        position);

                        icasteposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffAge = (Spinner) findViewById(R.id.iStuffage);
        ArrayAdapter adapter3 = ArrayAdapter.createFromResource(
                this, R.array.iStuffage, android.R.layout.simple_spinner_item);
        iStuffAge.setSelection(getiageposition);
        iStuffAge.setAdapter(adapter3);
        iStuffAge.setSelection(getiageposition);
        iStuffAge.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffAge = (String) iStuffAge.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set iageposition=" +
                                        position);
                        iageposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final RadioButton iStuffSex =
                (RadioButton) findViewById(R.id.iStuffmaleradiobutton);
        final RadioButton iStuffFemale =
                (RadioButton) findViewById(R.id.iStufffemaleradiobutton);
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
                        "UPDATE tempmatrimony set isex='" + getisex + "'");
            }
        });

        iStuffHeight = (Spinner) findViewById(R.id.iStuffheight);
        ArrayAdapter adapter4 = ArrayAdapter.createFromResource(
                this, R.array.iStuffheight,
                android.R.layout.simple_spinner_item);
        iStuffHeight.setAdapter(adapter4);
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
                                "UPDATE tempmatrimony set iheightposition=" +
                                        position);
                        iheightposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });
        iStuffWeight = (Spinner) findViewById(R.id.iStuffweight);
        ArrayAdapter adapter5 = ArrayAdapter.createFromResource(
                this, R.array.iStuffweight,
                android.R.layout.simple_spinner_item);
        iStuffWeight.setAdapter(adapter5);
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
                                "UPDATE tempmatrimony set iweightposition=" +
                                        position);
                        iweightposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        iStuffColor = (Spinner) findViewById(R.id.iStuffcolor);
        ArrayAdapter adapter6 = ArrayAdapter.createFromResource(
                this, R.array.iStuffcolor,
                android.R.layout.simple_spinner_item);
        iStuffColor.setAdapter(adapter6);
        iStuffColor.setSelection(geticolorposition);
        iStuffColor.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffColor = (String) iStuffColor.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set icolorposition=" +
                                        position);
                        icolorposition = position;
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

        final Spinner uStuffReligion =
                (Spinner) findViewById(R.id.uStuffReligion);
        ArrayAdapter adapter10 = ArrayAdapter.createFromResource(
                this, R.array.ustuffreligion,
                android.R.layout.simple_spinner_item);
        uStuffReligion.setAdapter(adapter10);
        uStuffReligion.setSelection(getureligionposition);
        uStuffReligion.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffReligion =
                                (String) uStuffReligion.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set ureligionposition=" +
                                        position);
                        ureligionposition = position;

                        //added line
                        final Spinner ustuffcaste =
                                (Spinner) findViewById(R.id.uStuffCaste);
                        ArrayAdapter casteadapter1 = ArrayAdapter
                                .createFromResource(Matrimony.this,
                                        religion[position],
                                        android.R.layout.simple_spinner_item);
                        ustuffcaste.setAdapter(casteadapter1);
                        ustuffcaste.setSelection(getucasteposition);
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });
        final Spinner uStuffCaste = (Spinner) findViewById(R.id.uStuffCaste);
        ArrayAdapter adapter11 = ArrayAdapter.createFromResource(
                this, R.array.istuffhindu,
                android.R.layout.simple_spinner_item);
        uStuffCaste.setAdapter(adapter11);
        uStuffCaste.setSelection(getucasteposition);
        uStuffCaste.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffCaste = (String) uStuffCaste.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set ucasteposition=" +
                                        position);
                        ucasteposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });


        final Spinner uStuffAge = (Spinner) findViewById(R.id.uStuffage);
        ArrayAdapter adapter12 = ArrayAdapter.createFromResource(
                this, R.array.age, android.R.layout.simple_spinner_item);
        uStuffAge.setAdapter(adapter12);
        uStuffAge.setSelection(getuageposition);
        uStuffAge.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffAge = (String) uStuffAge.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set uageposition=" +
                                        position);
                        uageposition = position;
                        if (getuStuffAge.equals("Select Age"))
                        {
                            getuStuffAge = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final RadioButton uStuffSex =
                (RadioButton) findViewById(R.id.uStufffemaleradiobutton);
        RadioButton uStuffFemale =
                (RadioButton) findViewById(R.id.uStuffmaleradiobutton);
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
                        "UPDATE tempmatrimony set usex='" + getusex + "'");
            }
        });
        final Spinner uStuffHeight = (Spinner) findViewById(R.id.uStuffheight);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
                R.array.height, android.R.layout.simple_spinner_item);
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
                                "UPDATE tempmatrimony set uheightposition=" +
                                        position);
                        uheightposition = position;
                        if (getuStuffHeight.equals("Select Height"))
                        {
                            getuStuffHeight = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffWeight = (Spinner) findViewById(R.id.uStuffweight);
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
                                "UPDATE tempmatrimony set uweightposition=" +
                                        position);
                        uweightposition = position;
                        if (getuStuffWeight.equals("Select Weight"))
                        {
                            getuStuffWeight = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffColor1 = (Spinner) findViewById(R.id.uStuffcolor);
        ArrayAdapter adapter13 = ArrayAdapter.createFromResource(
                this, R.array.uStuffcolor,
                android.R.layout.simple_spinner_item);
        uStuffColor1.setAdapter(adapter13);
        uStuffColor1.setSelection(getucolorposition);
        uStuffColor1.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffColor =
                                (String) uStuffColor1.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE tempmatrimony set ucolorposition=" +
                                        position);
                        ucolorposition = position;
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });
        uarea = (TextView) findViewById(R.id.uarea);
        uarea.setText(getuarea);
        ucity = (TextView) findViewById(R.id.ucity);
        ucity.setText(getucity);
        ucountry = (TextView) findViewById(R.id.ucountry);
        ucountry.setText(getucountry);

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
                        "UPDATE tempmatrimony set stufftype='" + "istuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Matrimony.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmatrimony");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Matrimony.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmatrimony");
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
                        "UPDATE tempmatrimony set stufftype='" + "ustuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Matrimony.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmatrimony");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Matrimony.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempmatrimony");
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
                getiStuffAge = (String) iStuffAge.getSelectedItem();
                getiStuffHeight = (String) iStuffHeight.getSelectedItem();
                getiStuffWeight = (String) iStuffWeight.getSelectedItem();
                getiStuffCaste = (String) iStuffCaste.getSelectedItem();
                getiStuffColor = (String) iStuffColor.getSelectedItem();
                getiStuffReligion = (String) iStuffReligion.getSelectedItem();

                ireligionposition = iStuffReligion.getSelectedItemPosition();
                icasteposition = iStuffCaste.getSelectedItemPosition();
                icolorposition = iStuffColor.getSelectedItemPosition();
                iageposition = iStuffAge.getSelectedItemPosition();
                iheightposition = iStuffHeight.getSelectedItemPosition();
                iweightposition = iStuffWeight.getSelectedItemPosition();

                idetails = "Matrimony Icon \n Age=" + getiStuffAge +
                        " Height=" + getiStuffHeight + " Weight=" +
                        getiStuffWeight + " Caste=" + getiStuffCaste +
                        " Color=" + getiStuffColor + " Religion=" +
                        getiStuffReligion + " Area=" + getiarea + " City=" +
                        geticity + " country=" + geticountry;
                getilatitudes = Double.parseDouble(getilatitude);
                getilatitudes = getilatitudes * 1E6;
                getilongitudes = Double.parseDouble(getilongitude);
                getilongitudes = getilongitudes * 1E6;
                if (fromeditquery != null && getkey != 0)
                {
                    myDatabase.execSQL("update Matrimony set ireligion='" +
                            getiStuffReligion + "', icaste ='" +
                            getiStuffCaste + "', iage='" + getiStuffAge +
                            "', isex='" + getisex + "', iheight='" +
                            getiStuffHeight + "', iweight='" + getiStuffWeight +
                            "', icolor='" + getiStuffColor + "', iarea='" +
                            getiarea + "', icity='" + geticity +
                            "', icountry='" + geticountry + "', ureligion='" +
                            getuStuffReligion + "', ucaste ='" +
                            getuStuffCaste + "', uage='" + getuStuffAge +
                            "', usex='" + getusex + "', uheight='" +
                            getuStuffHeight + "', uweight='" + getuStuffWeight +
                            "', ucolor='" + getuStuffColor + "', uarea='" +
                            getuarea + "', ucity='" + getucity +
                            "', ucountry='" + getucountry + "', ilatitude='" +
                            getilatitude + "', ilongitude='" + getilongitude +
                            "', ulatitude='" + getulatitude +
                            "', ulongitude='" + getulongitude +
                            "',queryDate=DATE('NOW') where key=" + getkey +
                            ";");
                    myDatabase.execSQL(
                            "update matrimonyposition set ireligionposition=" +
                                    ireligionposition + ", icasteposition=" +
                                    icasteposition + ", iageposition=" +
                                    iageposition + ", iheightposition=" +
                                    iheightposition + ", iweightposition=" +
                                    iweightposition + ", icolorposition=" +
                                    icolorposition + ", ureligionposition=" +
                                    ureligionposition + ", ucasteposition=" +
                                    ucasteposition + ", uageposition=" +
                                    uageposition + ", uheightposition=" +
                                    uheightposition + ", uweightposition=" +
                                    uweightposition + ", ucolorposition=" +
                                    ucolorposition + ", isex='" + getisex +
                                    "', usex='" + getusex + "', ilatitude='" +
                                    getilatitude + "', ilongitude='" +
                                    getilongitude + "', ulatitude='" +
                                    getulatitude + "', ulongitude='" +
                                    getulongitude + "' where key=" + getkey +
                                    ";");
                    myDatabase.execSQL("update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';");
                    myDatabase.execSQL("Update mStuffdetails set details='" +
                            idetails + "', latitude='" + getilatitudes +
                            "', longitude='" + getilongitudes +
                            "', location='" + geticountry +
                            "' where catagory='" + "userMatrimony" + "';");
                }
                else
                {
                    try
                    {
                        myDatabase.execSQL(
                                "INSERT INTO Matrimony (ireligion,icaste,iage, isex, iheight, iweight,icolor," +
                                        "iarea,icity,icountry,ilatitude,ilongitude,ureligion,ucaste,uage, usex, uheight,uweight,ucolor,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES ('" +
                                        getiStuffReligion + "','" +
                                        getiStuffCaste + "','" + getiStuffAge +
                                        "','" + getisex + "','" +
                                        getiStuffHeight + "','" +
                                        getiStuffWeight + "','" +
                                        getiStuffColor + "','" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" +
                                        getuStuffReligion + "','" +
                                        getuStuffCaste + "','" + getuStuffAge +
                                        "','" + getusex + "','" +
                                        getuStuffHeight + "','" +
                                        getuStuffWeight + "','" +
                                        getuStuffColor + "','" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getulatitude + "','" +
                                        getulongitude + "','" + "true" + "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "INSERT INTO matrimonyposition (ireligionposition, icasteposition, iageposition, iheightposition, iweightposition, icolorposition, iarea, icity, icountry, ureligionposition, ucasteposition, uageposition, uheightposition, uweightposition,ucolorposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                        ireligionposition + "," +
                                        icasteposition + "," + iageposition +
                                        "," + iheightposition + "," +
                                        iweightposition + "," + icolorposition +
                                        ",'" + getiarea + "','" + geticity +
                                        "','" + geticountry + "'," +
                                        ureligionposition + "," +
                                        ucasteposition + "," + uageposition +
                                        "," + uheightposition + "," +
                                        uweightposition + "," + ucolorposition +
                                        ",'" + getuarea + "','" + getucity +
                                        "','" + getucountry + "','" + getisex +
                                        "','" + getusex + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getulatitude + "','" + getulongitude +
                                        "','" + "Matrimony" + "', '" +
                                        "istuff" + "');");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry + "' where catagory='" +
                                        "userMatrimony" + "';");
                    }
                    catch (Exception exce)
                    {
                        myDatabase.execSQL(
                                "CREATE TABLE IF NOT EXISTS Matrimony (key INTEGER PRIMARY KEY,ireligion VARCHAR,icaste VARCHAR,iage VARCHAR, isex VARCHAR, iheight VARCHAR, " +
                                        "iweight VARCHAR,icolor VARCHAR ,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,ureligion VARCHAR,ucaste VARCHAR,uage VARCHAR, usex VARCHAR, uheight VARCHAR," +
                                        "uweight VARCHAR,ucolor VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
                        myDatabase.execSQL(
                                "CREATE TRIGGER insert_querydate_Matrimony after INSERT on Matrimony BEGIN update Matrimony set queryDate=DATE('NOW') WHERE key=new.key; END;");
                        myDatabase.execSQL(
                                "CREATE TRIGGER delete_querydate_Matrimony before insert on Matrimony BEGIN delete from Matrimony where queryDate<DATE('NOW','-7 day');END;");
                        myDatabase.execSQL(
                                "INSERT INTO Matrimony (ireligion,icaste,iage, isex, iheight, iweight,icolor," +
                                        "iarea,icity,icountry,ilatitude,ilongitude,ureligion,ucaste,uage, usex, uheight,uweight,ucolor,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES ('" +
                                        getiStuffReligion + "','" +
                                        getiStuffCaste + "','" + getiStuffAge +
                                        "','" + getisex + "','" +
                                        getiStuffHeight + "','" +
                                        getiStuffWeight + "','" +
                                        getiStuffColor + "','" + getiarea +
                                        "','" + geticity + "','" + geticountry +
                                        "','" + getilatitude + "','" +
                                        getilongitude + "','" +
                                        getuStuffReligion + "','" +
                                        getuStuffCaste + "','" + getuStuffAge +
                                        "','" + getusex + "','" +
                                        getuStuffHeight + "','" +
                                        getuStuffWeight + "','" +
                                        getuStuffColor + "','" + getuarea +
                                        "','" + getucity + "','" + getucountry +
                                        "','" + getulatitude + "','" +
                                        getulongitude + "','" + "true" + "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "CREATE TABLE IF NOT EXISTS matrimonyposition" +
                                        " (key INTEGER PRIMARY KEY,ireligionposition NUMERIC, icasteposition NUMERIC, iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, icolorposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ureligionposition NUMERIC, ucasteposition NUMERIC, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC,ucolorposition NUMERIC,  uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");
                        myDatabase.execSQL(
                                "INSERT INTO matrimonyposition (ireligionposition, icasteposition, iageposition, iheightposition, iweightposition, icolorposition, iarea, icity, icountry, ureligionposition, ucasteposition, uageposition, uheightposition, uweightposition,ucolorposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                                        ireligionposition + "," +
                                        icasteposition + "," + iageposition +
                                        "," + iheightposition + "," +
                                        iweightposition + "," + icolorposition +
                                        ",'" + getiarea + "','" + geticity +
                                        "','" + geticountry + "'," +
                                        ureligionposition + "," +
                                        ucasteposition + "," + uageposition +
                                        "," + uheightposition + "," +
                                        uweightposition + "," + ucolorposition +
                                        ",'" + getuarea + "','" + getucity +
                                        "','" + getucountry + "','" + getisex +
                                        "','" + getusex + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getulatitude + "','" + getulongitude +
                                        "','" + "Matrimony" + "', '" +
                                        "istuff" + "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry + "' where catagory='" +
                                        "userMatrimony" + "';");
                    }
                }
                myDatabase.execSQL("drop table " + "tempmatrimony" + ";");
                Intent intent =
                        new Intent(Matrimony.this, FindandInstall.class);
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
                Intent intent1 =
                        new Intent(Matrimony.this, MapResults.class);
                startActivityForResult(intent1, 0);
                finish();
                break;

            case 2:
                Intent intent2 =
                        new Intent(Matrimony.this, FindandInstall.class);
                startActivityForResult(intent2, 0);
                finish();
                break;

            case 3:
                Intent intent3 = new Intent(Matrimony.this, Settings.class);
                startActivityForResult(intent3, 0);
                finish();
                break;
            case 4:
                Intent intent =
                        new Intent(Matrimony.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
                break;
            case 5:
                try
                {
                    Cursor c = myDatabase.query("Matrimony", null, null, null,
                            null, null, null);
                    Intent listmStuff = new Intent(Matrimony.this,
                            Matrimonylistquery.class);
                    startActivityForResult(listmStuff, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(Matrimony.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                try
                {
                    Cursor c = myDatabase.query("Matrimony", null, null, null,
                            null, null, null);
                    Intent matrimonydeleteQuery = new Intent(Matrimony.this,
                            Matrimonydeletequery.class);
                    startActivity(matrimonydeleteQuery);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Matrimony.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 7:
                try
                {
                    Cursor c = myDatabase.query("Matrimony", null, null, null,
                            null, null, null);
                    Intent matrimonyviewQuery = new Intent(Matrimony.this,
                            Matrimonyviewquery.class);
                    startActivity(matrimonyviewQuery);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Matrimony.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 8:
                Bundle b1 = new Bundle();
                Intent settheme =
                        new Intent(Matrimony.this, SettingTheme.class);
                b1.putString("value1", "Matrimony");
                b1.putString("class", "4");
                settheme.putExtras(b1);
                startActivityForResult(settheme, 0);
                finish();
            case 9:
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", "Matrimony");
                Intent mediaintent =
                        new Intent(Matrimony.this, Uploadmultimedia.class);
                mediaintent.putExtras(B1);
                startActivity(mediaintent);
        }
        return super.onOptionsItemSelected(item);
    }
}
