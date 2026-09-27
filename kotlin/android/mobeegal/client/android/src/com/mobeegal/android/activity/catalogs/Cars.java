package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Cars.java 14 2008-08-19 06:36:45Z muthu.ramadoss                       $: Id of last commit
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

public class Cars
        extends Activity
{

    String getiuStuffcartype;
    String getiStuffcartype;
    SQLiteDatabase myDatabase = null;
    int key = 1;
    ArrayAdapter<CharSequence> adapter;
    String getiStuffMakeType;
    String getiStuffModelType;
    String getiStuffYear;
    String getiStuffColor;
    String getiStuffFuelType;
    String getiStuffPrice;
    String getuStuffMakeType;
    String getuStuffModelType;
    String getuStuffYear;
    String getuStuffColor;
    String getuStuffFuelType;
    String getuStuffPrice;
    String getiStuffMake;
    String getiStuffModel;
    String getuStuffMake;
    String getuStuffModel;
    Cursor c;
    int getimakeposition;
    int getimodelposition;
    int getiyearposition;
    int geticolorposition;
    int getifuel_typeposition;
    String geticity;
    String getiarea;
    String geticountry;
    int getipriceposition;
    String getilatitude;
    String getilongitude;
    int getumakeposition;
    int getumodelposition;
    int getucolorposition;
    int getufuel_typeposition;
    String getucity;
    String getuarea;
    String getucountry;
    int getuyearposition;
    int getupriceposition;
    String getulatitude;
    String getulongitude;
    String getcategory;
    String getstufftype;
    Spinner iStuffMake;
    Spinner iStuffModel;
    Spinner iStuffyear;
    Spinner iStuffColor;
    Spinner iStuffFuelType;
    Spinner uStuffColor;
    Spinner uStuffModel;
    Spinner iStuffCity;
    Spinner uStuffCity;
    Spinner iStuffPrice;
    Spinner uStuffyear;
    Spinner uStuffMake;
    Spinner uStuffFuelType;
    Spinner uStuffPrice;
    int imakeposition;
    int imodelposition;
    int iyearposition;
    int icolorposition;
    int ifuel_typeposition;
    int ipriceposition;
    int umakeposition;
    int umodelposition;
    int uyearposition;
    int ucolorposition;
    int ufuel_typeposition;
    int upriceposition;
    TextView iarea;
    TextView icity;
    TextView icountry;
    TextView uarea;
    TextView ucity;
    TextView ucountry;
    Bundle fromeditquery;
    private int getkey;
    int theme;
    private int[] carModel = {R.array.Audi, R.array.Bentley, R.array.BMW,
            R.array.Chevrolet, R.array.Chrysler, R.array.Daewoo,
            R.array.Ferrari, R.array.Fiat, R.array.Ford,
            R.array.HindustanMotors, R.array.Honda, R.array.Hummer,
            R.array.Hyundai, R.array.ICML, R.array.Lamborghini,
            R.array.LandRover, R.array.Lexus, R.array.Mahindra,
            R.array.MahindraRenault, R.array.Maini, R.array.Maruti,
            R.array.Maserati, R.array.Maybach, R.array.Mercedes,
            R.array.Mitsubishi, R.array.Nissan, R.array.Opel, R.array.Peugeot,
            R.array.Porsche, R.array.RollsRoyce, R.array.SanMotors,
            R.array.Sipani, R.array.Skoda, R.array.Tata, R.array.Toyota,
            R.array.Volkswagen, R.array.Volvo};
    private String viewtypename;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempcarscursor = myDatabase.query("tempcars", null, null,
                    null, null, null, null);
        }
        catch (Exception e)
        {
            myDatabase
                    .execSQL("CREATE TABLE IF NOT EXISTS tempcars"
                            +
                            " (imakeposition NUMERIC, imodelposition NUMERIC, iyearposition NUMERIC, icolorposition NUMERIC, ifuelposition NUMERIC, ipriceposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR, umakeposition NUMERIC, umodelposition NUMERIC, uyearposition NUMERIC, ucolorposition NUMERIC, ufuelposition NUMERIC, upriceposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
            myDatabase
                    .execSQL(
                            "INSERT INTO tempcars (imakeposition, imodelposition,iyearposition,icolorposition, ifuelposition,ipriceposition, iarea, icity, icountry, ilatitude,ilongitude, umakeposition, umodelposition,uyearposition,ucolorposition, ufuelposition,upriceposition, uarea, ucity, ucountry,ulatitude,ulongitude, category, stufftype) VALUES ("
                                    + imakeposition
                                    + ","
                                    + imodelposition
                                    + ","
                                    + iyearposition
                                    + ","
                                    + icolorposition
                                    + ","
                                    + ifuel_typeposition
                                    + ","
                                    + ipriceposition
                                    + ",'"
                                    + ""
                                    + "','"
                                    + ""
                                    + "','"
                                    + ""
                                    + "','"
                                    + ""
                                    + "','"
                                    + ""
                                    + "',"
                                    + umakeposition
                                    + ","
                                    + umodelposition
                                    + ","
                                    + uyearposition
                                    + ","
                                    + ucolorposition
                                    + ","
                                    + ufuel_typeposition
                                    + ","
                                    + upriceposition
                                    + ",'"
                                    + ""
                                    + "','"
                                    + ""
                                    + "','"
                                    + ""
                                    + "','"
                                    + ""
                                    + "','"
                                    + "" + "','" + "Cars" + "', '" + "istuff" +
                                    "');");
        }
        finally
        {
            if (myDatabase != null)
            {
                myDatabase.close();

            }
        }
        fromeditquery = this.getIntent().getExtras();
        if (fromeditquery != null)
        {
            getkey = fromeditquery.getInt("key");
        }

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor themecursor = myDatabase.query("Theme", null,
                    "catalog='Cars'", null, null, null, null);
            if (themecursor != null)
            {
                if (themecursor.isFirst())
                {
                    do
                    {
                        theme = themecursor.getInt(themecursor
                                .getColumnIndexOrThrow("theme"));
                        // String catalog =
                        // themecursor.getString(themecursor.getColumnIndexOrThrow("theme"));
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
        setContentView(R.layout.cars);
        TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.iStuffprofile);
        one.setIndicator("Seller");
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.uStuffprofile);
        two.setIndicator("Buyer");
        tabs.addTab(two);

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor tempcarscursor = myDatabase.query("tempcars", null, null,
                    null, null, null, null);

            if (tempcarscursor != null)
            {
                if (tempcarscursor.isFirst())
                {
                    do
                    {
                        getimakeposition = tempcarscursor.getInt(tempcarscursor
                                .getColumnIndexOrThrow("imakeposition"));
                        getimodelposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "imodelposition"));
                        getiyearposition = tempcarscursor.getInt(tempcarscursor
                                .getColumnIndexOrThrow("iyearposition"));
                        geticolorposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "icolorposition"));
                        getifuel_typeposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "ifuelposition"));
                        getipriceposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "ipriceposition"));
                        geticity = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("icity"));
                        getiarea = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("iarea"));
                        geticountry = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("icountry"));
                        getilatitude = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("ilatitude"));
                        getilongitude = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("ilongitude"));

                        getumakeposition = tempcarscursor.getInt(tempcarscursor
                                .getColumnIndexOrThrow("umakeposition"));
                        getumodelposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "umodelposition"));
                        getuyearposition = tempcarscursor.getInt(tempcarscursor
                                .getColumnIndexOrThrow("uyearposition"));
                        getucolorposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "ucolorposition"));
                        getufuel_typeposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "ufuelposition"));
                        getupriceposition = tempcarscursor
                                .getInt(tempcarscursor
                                        .getColumnIndexOrThrow(
                                        "upriceposition"));
                        getucity = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("ucity"));
                        getuarea = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("uarea"));
                        getucountry = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("ucountry"));
                        getulatitude = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("ulatitude"));
                        getulongitude = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("ulongitude"));
                        getcategory = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("category"));
                        getstufftype = tempcarscursor.getString(tempcarscursor
                                .getColumnIndexOrThrow("stufftype"));

                    }
                    while (tempcarscursor.moveToNext());
                }
            }
        }
        catch (Exception e)
        {
        }
        if (getstufftype.equals("istuff"))
        {
            tabs.setCurrentTab(0);
        }
        else if (getstufftype.equals("ustuff"))
        {
            tabs.setCurrentTab(1);
        }
        // Creating a tab1 for ISTUFF

        iStuffMake = (Spinner) findViewById(R.id.iStuffMake);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this,
                R.array.MakeType, android.R.layout.simple_spinner_item);
        iStuffMake.setAdapter(adapter1);
        iStuffMake.setSelection(getimakeposition);
        iStuffMake
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getiStuffMakeType = (String) iStuffMake
                                        .getSelectedItem();
                                imakeposition =
                                        iStuffMake.getSelectedItemPosition();
                                myDatabase.execSQL(
                                        "UPDATE tempcars set imakeposition="
                                                + position);
                                final Spinner iStuffModel =
                                        (Spinner) findViewById(
                                                R.id.iStuffModel);
                                ArrayAdapter modelAdapter = ArrayAdapter
                                        .createFromResource(Cars.this,
                                                carModel[position],
                                                android.R.layout.simple_spinner_item);
                                iStuffModel.setAdapter(modelAdapter);
                                iStuffModel.setSelection(getimodelposition);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

        iStuffModel = (Spinner) findViewById(R.id.iStuffModel);
        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(this,
                R.array.Audi, android.R.layout.simple_spinner_item);
        iStuffModel.setAdapter(adapter2);
        iStuffModel.setSelection(getimodelposition);
        iStuffModel
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getiStuffModelType = (String) iStuffModel
                                        .getSelectedItem();
                                imodelposition =
                                        iStuffModel.getSelectedItemPosition();
                                myDatabase
                                        .execSQL(
                                                "UPDATE tempcars set imodelposition="
                                                        + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

        iStuffyear = (Spinner) findViewById(R.id.iStuffYear);
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter
                .createFromResource(this, R.array.Year,
                        android.R.layout.simple_spinner_item);
        iStuffyear.setAdapter(yearAdapter);
        iStuffyear.setSelection(getiyearposition);
        iStuffyear
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getiStuffYear =
                                        (String) iStuffyear.getSelectedItem();
                                iyearposition =
                                        iStuffyear.getSelectedItemPosition();
                                myDatabase.execSQL(
                                        "UPDATE tempcars set iyearposition="
                                                + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

        iStuffColor = (Spinner) findViewById(R.id.iStuffColor);
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter
                .createFromResource(this, R.array.Color,
                        android.R.layout.simple_spinner_item);
        iStuffColor.setAdapter(colorAdapter);
        iStuffColor.setSelection(geticolorposition);
        iStuffColor
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getiStuffColor =
                                        (String) iStuffColor.getSelectedItem();
                                icolorposition =
                                        iStuffColor.getSelectedItemPosition();
                                myDatabase
                                        .execSQL(
                                                "UPDATE tempcars set icolorposition="
                                                        + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });
        iStuffFuelType = (Spinner) findViewById(R.id.iStuffFuelType);
        ArrayAdapter<CharSequence> fuel_TypeAdapter = ArrayAdapter
                .createFromResource(this, R.array.FuelType,
                        android.R.layout.simple_spinner_item);
        iStuffFuelType.setAdapter(fuel_TypeAdapter);
        iStuffFuelType.setSelection(getifuel_typeposition);
        iStuffFuelType
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getiStuffFuelType = (String) iStuffFuelType
                                        .getSelectedItem();
                                ifuel_typeposition = iStuffFuelType
                                        .getSelectedItemPosition();
                                myDatabase.execSQL(
                                        "UPDATE tempcars set ifuelposition="
                                                + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });
        iStuffPrice = (Spinner) findViewById(R.id.iStuffPrice);
        ArrayAdapter<CharSequence> priceAdapter = ArrayAdapter
                .createFromResource(this, R.array.Price,
                        android.R.layout.simple_spinner_item);
        iStuffPrice.setAdapter(priceAdapter);
        iStuffPrice.setSelection(getipriceposition);
        iStuffPrice
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getiStuffPrice =
                                        (String) iStuffPrice.getSelectedItem();
                                ipriceposition =
                                        iStuffPrice.getSelectedItemPosition();
                                myDatabase
                                        .execSQL(
                                                "UPDATE tempcars set ipriceposition="
                                                        + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

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

        // Creating a tab2 for USTUFF
        uStuffMake = (Spinner) findViewById(R.id.uStuffMake);
        ArrayAdapter adapter10 = ArrayAdapter.createFromResource(this,
                R.array.MakeType, android.R.layout.simple_spinner_item);
        uStuffMake.setAdapter(adapter10);
        uStuffMake.setSelection(getumakeposition);
        uStuffMake
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getuStuffMakeType = (String) uStuffMake
                                        .getSelectedItem();
                                umakeposition =
                                        uStuffMake.getSelectedItemPosition();
                                myDatabase.execSQL(
                                        "UPDATE tempcars set umakeposition="
                                                + position);
                                final Spinner uStuffModel =
                                        (Spinner) findViewById(
                                                R.id.uStuffModel);
                                ArrayAdapter modelAdapter = ArrayAdapter
                                        .createFromResource(Cars.this,
                                                carModel[position],
                                                android.R.layout.simple_spinner_item);
                                uStuffModel.setAdapter(modelAdapter);
                                uStuffModel.setSelection(getumodelposition);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

        uStuffModel = (Spinner) findViewById(R.id.uStuffModel);
        ArrayAdapter adapter20 = ArrayAdapter.createFromResource(this,
                R.array.Audi, android.R.layout.simple_spinner_item);
        uStuffModel.setAdapter(adapter20);
        uStuffModel.setSelection(getumodelposition);
        uStuffModel
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                umodelposition =
                                        uStuffModel.getSelectedItemPosition();
                                getuStuffModelType = (String) uStuffModel
                                        .getSelectedItem();
                                myDatabase
                                        .execSQL(
                                                "UPDATE tempcars set umodelposition="
                                                        + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });
        uStuffyear = (Spinner) findViewById(R.id.uStuffYear);
        ArrayAdapter<CharSequence> Adapter = ArrayAdapter.createFromResource(
                this, R.array.Year, android.R.layout.simple_spinner_item);
        uStuffyear.setAdapter(Adapter);
        uStuffyear.setSelection(getuyearposition);
        uStuffyear
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                getuStuffYear =
                                        (String) uStuffyear.getSelectedItem();
                                uyearposition =
                                        uStuffyear.getSelectedItemPosition();
                                myDatabase.execSQL(
                                        "UPDATE tempcars set uyearposition="
                                                + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

        uStuffColor = (Spinner) findViewById(R.id.uStuffColor);
        ArrayAdapter<CharSequence> Adapter19 = ArrayAdapter.createFromResource(
                this, R.array.Color, android.R.layout.simple_spinner_item);
        uStuffColor.setAdapter(Adapter19);
        uStuffColor.setSelection(getucolorposition);
        uStuffColor
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                ucolorposition =
                                        uStuffColor.getSelectedItemPosition();
                                getuStuffColor =
                                        (String) uStuffColor.getSelectedItem();
                                myDatabase
                                        .execSQL(
                                                "UPDATE tempcars set ucolorposition="
                                                        + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

        uStuffFuelType = (Spinner) findViewById(R.id.uStuffFuelType);
        ArrayAdapter<CharSequence> fuel_TypeAdapter1 = ArrayAdapter
                .createFromResource(this, R.array.FuelType,
                        android.R.layout.simple_spinner_item);
        uStuffFuelType.setAdapter(fuel_TypeAdapter1);
        uStuffFuelType.setSelection(getufuel_typeposition);
        uStuffFuelType
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                ufuel_typeposition = uStuffFuelType
                                        .getSelectedItemPosition();
                                getuStuffFuelType = (String) uStuffFuelType
                                        .getSelectedItem();
                                myDatabase.execSQL(
                                        "UPDATE tempcars set ufuelposition="
                                                + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });

        uStuffPrice = (Spinner) findViewById(R.id.uStuffPrice);
        ArrayAdapter<CharSequence> priceAdapter1 = ArrayAdapter
                .createFromResource(this, R.array.Price,
                        android.R.layout.simple_spinner_item);
        uStuffPrice.setAdapter(priceAdapter1);
        uStuffPrice.setSelection(getupriceposition);
        uStuffPrice
                .setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener()
                        {

                            public void onItemSelected(AdapterView parent,
                                    View v,
                                    int position, long id)
                            {
                                upriceposition =
                                        uStuffPrice.getSelectedItemPosition();
                                getuStuffPrice =
                                        (String) uStuffPrice.getSelectedItem();
                                myDatabase
                                        .execSQL(
                                                "UPDATE tempcars set upriceposition="
                                                        + position);
                            }

                            public void onNothingSelected(AdapterView parent)
                            {
                            }
                        });
        // added
        String viewtype[] = {"views"};
        Cursor cur = myDatabase.query("Preferences", viewtype, null,
                null, null, null, null);
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
                myDatabase.execSQL("UPDATE tempcars set stufftype='"
                        + "istuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder = new Intent(Cars.this,
                            LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempcars");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Cars.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempcars");
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
                myDatabase.execSQL("UPDATE tempcars set stufftype='"
                        + "ustuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder = new Intent(Cars.this,
                            LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempcars");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Cars.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "tempcars");
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
                getiStuffMakeType = (String) iStuffMake.getSelectedItem();
                getiStuffModelType = (String) iStuffModel.getSelectedItem();
                getiStuffYear = (String) iStuffyear.getSelectedItem();
                getiStuffColor = (String) iStuffColor.getSelectedItem();
                getiStuffFuelType = (String) iStuffFuelType.getSelectedItem();
                getiStuffPrice = (String) iStuffPrice.getSelectedItem();

                imakeposition = iStuffMake.getSelectedItemPosition();
                imodelposition = iStuffModel.getSelectedItemPosition();
                iyearposition = iStuffyear.getSelectedItemPosition();
                icolorposition = iStuffColor.getSelectedItemPosition();
                ifuel_typeposition = iStuffFuelType.getSelectedItemPosition();
                ipriceposition = iStuffPrice.getSelectedItemPosition();

                idetails = "Cars Icon \n Carmake=" + getiStuffMakeType
                        + " model=" + getiStuffModelType + " year="
                        + getiStuffYear + " color=" + getiStuffColor
                        + " fuel_type=" + getiStuffFuelType + " price="
                        + getiStuffPrice + " area=" + getiarea + " city="
                        + geticity + " country=" + geticountry;

                getilatitudes = Double.parseDouble(getilatitude);
                getilatitudes = getilatitudes * 1E6;
                getilongitudes = Double.parseDouble(getilongitude);
                getilongitudes = getilongitudes * 1E6;

                if (fromeditquery != null && getkey != 0)
                {
                    myDatabase.execSQL("update Cars set imake='"
                            + getiStuffMakeType + "', imodel='"
                            + getiStuffModelType + "', iyear='" + getiStuffYear
                            + "', icolor='" + getiStuffColor
                            + "', ifuel_type='" + getiStuffFuelType
                            + "', iprice='" + getiStuffPrice + "', iarea='"
                            + getiarea + "', icity='" + geticity
                            + "', icountry='" + geticountry + "', ilatitude='"
                            + getilatitude + "', ilongitude='" + getilongitude
                            + "', umake='" + getuStuffMakeType + "', umodel='"
                            + getuStuffModelType + "', uyear='" + getuStuffYear
                            + "', ucolor='" + getuStuffColor
                            + "', ufuel_type='" + getuStuffFuelType
                            + "', iprice='" + getuStuffPrice + "', uarea='"
                            + getuarea + "', ucity='" + getucity
                            + "', ucountry='" + getucountry + "', ulatitude='"
                            + getulatitude + "', ulongitude='" + getulongitude
                            + "',queryDate=DATE('NOW') where key=" + getkey
                            + ";");
                    myDatabase.execSQL("update CarsPosition set imakeposition="
                            + imakeposition + ", imodelposition="
                            + imodelposition + ", iyearposition="
                            + iyearposition + ", icolorposition="
                            + icolorposition + ", ifuelposition="
                            + ifuel_typeposition + ", ipriceposition="
                            + ipriceposition + ", iarea='" + getiarea
                            + "', icity='" + geticity + "', icountry='"
                            + geticountry + "', ilatitude='" + getilatitude
                            + "', ilongitude='" + getilongitude
                            + "', umakeposition=" + umakeposition
                            + ", umodelposition=" + umodelposition
                            + ", uyearposition=" + uyearposition
                            + ", ucolorposition=" + ucolorposition
                            + ", ufuelposition=" + ufuel_typeposition
                            + ", upriceposition=" + upriceposition
                            + ", uarea='" + getuarea + "', ucity='" + getucity
                            + "', ucountry='" + getucountry + "', ulatitude='"
                            + getulatitude + "', ulongitude='" + getulongitude
                            + "' where key=" + getkey + ";");
                    myDatabase
                            .execSQL("update category set querystatus='"
                                    + "true" + "' where categoryname='"
                                    + "Cars" + "';");
                    myDatabase.execSQL("Update mStuffdetails set details='"
                            + idetails + "', latitude='" + getilatitudes
                            + "', longitude='" + getilongitudes
                            + "', location='" + geticountry
                            + "' where catagory='" + "userCars" + "';");
                }
                else
                {
                    try
                    {
                        myDatabase
                                .execSQL(
                                        "INSERT INTO Cars (imake,imodel,iyear,icolor,ifuel_type,iprice,iarea,icity,icountry,ilatitude,ilongitude,umake,umodel,uyear,ucolor,ufuel_type,uprice,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES('"
                                                + getiStuffMakeType
                                                + "','"
                                                + getiStuffModelType
                                                + "','"
                                                + getiStuffYear
                                                + "','"
                                                + getiStuffColor
                                                + "','"
                                                + getiStuffFuelType
                                                + "','"
                                                + getiStuffPrice
                                                + "','"
                                                + getiarea
                                                + "','"
                                                + geticity
                                                + "','"
                                                + geticountry
                                                + "','"
                                                + getilatitude
                                                + "','"
                                                + getilongitude
                                                + "','"
                                                + getuStuffMakeType
                                                + "','"
                                                + getuStuffModelType
                                                + "','"
                                                + getuStuffYear
                                                + "','"
                                                + getuStuffColor
                                                + "','"
                                                + getuStuffFuelType
                                                + "','"
                                                + getuStuffPrice
                                                + "','"
                                                + getuarea
                                                + "','"
                                                + getucity
                                                + "','"
                                                + getucountry
                                                + "','"
                                                + getulatitude
                                                + "','"
                                                + getulongitude
                                                + "','"
                                                + "true" + "');");
                        myDatabase.execSQL("update category set querystatus='"
                                + "true" + "' where categoryname='" + "Cars"
                                + "';");
                        myDatabase
                                .execSQL(
                                        "INSERT INTO CarsPosition(imakeposition, imodelposition,iyearposition,icolorposition, ifuelposition,ipriceposition, iarea, icity, icountry, ilatitude,ilongitude, umakeposition, umodelposition,uyearposition,ucolorposition, ufuelposition,upriceposition, uarea, ucity, ucountry,ulatitude,ulongitude, category, stufftype) VALUES ("
                                                + imakeposition
                                                + ","
                                                + imodelposition
                                                + ","
                                                + iyearposition
                                                + ","
                                                + icolorposition
                                                + ","
                                                + ifuel_typeposition
                                                + ","
                                                + ipriceposition
                                                + ",'"
                                                + getiarea
                                                + "','"
                                                + geticity
                                                + "','"
                                                + geticountry
                                                + "','"
                                                + getilatitude
                                                + "','"
                                                + getilongitude
                                                + "',"
                                                + umakeposition
                                                + ","
                                                + umodelposition
                                                + ","
                                                + uyearposition
                                                + ","
                                                + ucolorposition
                                                + ","
                                                + ufuel_typeposition
                                                + ","
                                                + upriceposition
                                                + ",'"
                                                + getuarea
                                                + "','"
                                                + getucity
                                                + "','"
                                                + getucountry
                                                + "','"
                                                + getulatitude
                                                + "','"
                                                + getulongitude
                                                + "','"
                                                + getcategory
                                                + "', '"
                                                + getstufftype
                                                + "');");
                        myDatabase.execSQL("Update mStuffdetails set details='"
                                + idetails + "', latitude='" + getilatitudes
                                + "', longitude='" + getilongitudes
                                + "', location='" + geticountry
                                + "' where catagory='" + "userCars" + "';");
                    }
                    catch (Exception e)
                    {
                        myDatabase
                                .execSQL("CREATE TABLE IF NOT EXISTS Cars"
                                        +
                                        " (key INTEGER PRIMARY KEY,imake VARCHAR,imodel VARCHAR,iyear VARCHAR,icolor VARCHAR,ifuel_type VARCHAR,iprice VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,umake VARCHAR,umodel VARCHAR,uyear VARCHAR,ucolor VARCHAR,ufuel_type VARCHAR,uprice VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
                        myDatabase
                                .execSQL(
                                        "CREATE TRIGGER insert_querydate_Cars after INSERT on Cars BEGIN update Cars set queryDate=DATE('NOW') WHERE key=new.key; END;");
                        myDatabase
                                .execSQL(
                                        "CREATE TRIGGER delete_querydate_Cars before insert on Cars BEGIN delete from Cars where queryDate<DATE('NOW','-7 day');END;");
                        myDatabase
                                .execSQL(
                                        "INSERT INTO Cars (imake,imodel,iyear,icolor,ifuel_type,iprice,iarea,icity,icountry,ilatitude,ilongitude,umake,umodel,uyear,ucolor,ufuel_type,uprice,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES('"
                                                + getiStuffMakeType
                                                + "','"
                                                + getiStuffModelType
                                                + "','"
                                                + getiStuffYear
                                                + "','"
                                                + getiStuffColor
                                                + "','"
                                                + getiStuffFuelType
                                                + "','"
                                                + getiStuffPrice
                                                + "','"
                                                + getiarea
                                                + "','"
                                                + geticity
                                                + "','"
                                                + geticountry
                                                + "','"
                                                + getilatitude
                                                + "','"
                                                + getilongitude
                                                + "','"
                                                + getuStuffMakeType
                                                + "','"
                                                + getuStuffModelType
                                                + "','"
                                                + getuStuffYear
                                                + "','"
                                                + getuStuffColor
                                                + "','"
                                                + getuStuffFuelType
                                                + "','"
                                                + getuStuffPrice
                                                + "','"
                                                + getuarea
                                                + "','"
                                                + getucity
                                                + "','"
                                                + getucountry
                                                + "','"
                                                + getulatitude
                                                + "','"
                                                + getulongitude
                                                + "','"
                                                + "true" + "');");
                        myDatabase
                                .execSQL(
                                        "CREATE TABLE IF NOT EXISTS CarsPosition"
                                                +
                                                " (key INTEGER PRIMARY KEY, imakeposition NUMERIC ,imodelposition NUMERIC ,iyearposition NUMERIC,icolorposition NUMERIC , ifuelposition NUMERIC , ipriceposition NUMERIC,iarea VARCHAR, icity VARCHAR, icountry VARCHAR , ilatitude VARCHAR, ilongitude VARCHAR, umakeposition NUMERIC ,umodelposition NUMERIC ,uyearposition NUMERIC , ucolorposition NUMERIC , ufuelposition NUMERIC , upriceposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR , ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");
                        myDatabase
                                .execSQL(
                                        "INSERT INTO CarsPosition(imakeposition, imodelposition,iyearposition,icolorposition, ifuelposition,ipriceposition, iarea, icity, icountry, ilatitude,ilongitude, umakeposition, umodelposition,uyearposition,ucolorposition, ufuelposition,upriceposition, uarea, ucity, ucountry,ulatitude,ulongitude, category, stufftype) VALUES ("
                                                + imakeposition
                                                + ","
                                                + imodelposition
                                                + ","
                                                + iyearposition
                                                + ","
                                                + icolorposition
                                                + ","
                                                + ifuel_typeposition
                                                + ","
                                                + ipriceposition
                                                + ",'"
                                                + getiarea
                                                + "','"
                                                + geticity
                                                + "','"
                                                + geticountry
                                                + "','"
                                                + getilatitude
                                                + "','"
                                                + getilongitude
                                                + "',"
                                                + umakeposition
                                                + ","
                                                + umodelposition
                                                + ","
                                                + uyearposition
                                                + ","
                                                + ucolorposition
                                                + ","
                                                + ufuel_typeposition
                                                + ","
                                                + upriceposition
                                                + ",'"
                                                + getuarea
                                                + "','"
                                                + getucity
                                                + "','"
                                                + getucountry
                                                + "','"
                                                + getulatitude
                                                + "','"
                                                + getulongitude
                                                + "','"
                                                + getcategory
                                                + "', '"
                                                + getstufftype
                                                + "');");
                        myDatabase.execSQL("update category set querystatus='"
                                + "true" + "' where categoryname='" + "Cars"
                                + "';");
                        myDatabase.execSQL("Update mStuffdetails set details='"
                                + idetails + "', latitude='" + getilatitudes
                                + "', longitude='" + getilongitudes
                                + "', location='" + geticountry
                                + "' where catagory='" + "userCars" + "';");
                    }
                }
                myDatabase.execSQL("drop table " + "tempcars" + ";");
                Intent intent = new Intent(Cars.this, FindandInstall.class);
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
                Intent intent1 = new Intent(Cars.this, MapResults.class);
                startActivityForResult(intent1, 0);
                finish();
                break;

            case 2:
                Intent intent2 = new Intent(Cars.this, FindandInstall.class);
                startActivityForResult(intent2, 0);
                finish();
                break;

            case 3:
                Intent intent3 = new Intent(Cars.this, Settings.class);
                startActivityForResult(intent3, 0);
                finish();
                break;
            case 4:

                Intent intent = new Intent(Cars.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
                break;
            case 5:
                try
                {
                    Cursor c = myDatabase.query("Cars", null, null, null, null,
                            null, null);
                    Intent listmStuff =
                            new Intent(Cars.this, Carslistquery.class);
                    startActivityForResult(listmStuff, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(Cars.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                try
                {
                    Cursor c = myDatabase.query("Cars", null, null, null, null,
                            null, null);
                    Intent deletemStuff = new Intent(Cars.this,
                            Carsdeletequery.class);
                    startActivityForResult(deletemStuff, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Cars.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 7:
                try
                {
                    Cursor c = myDatabase.query("Cars", null, null, null, null,
                            null, null);
                    Intent viewQuery =
                            new Intent(Cars.this, CarsViewQuery.class);
                    startActivityForResult(viewQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Cars.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case 8:
                Bundle b1 = new Bundle();
                Intent settheme = new Intent(Cars.this, SettingTheme.class);
                b1.putString("value1", "Cars");
                b1.putString("class", "2");
                settheme.putExtras(b1);
                startActivityForResult(settheme, 0);
                finish();
            case 9:
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", "Cars");
                Intent mediaintent =
                        new Intent(Cars.this, Uploadmultimedia.class);
                mediaintent.putExtras(B1);
                startActivity(mediaintent);
        }
        return super.onOptionsItemSelected(item);
    }
}
