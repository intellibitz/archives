package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Restaurants.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
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

public class Restaurants
        extends Activity
{

    SQLiteDatabase myDatabase = null;
    String getiStuffCuisinetype;
    String getiStuffCookingMethod;
    String getiStuffDietetic;
    String getiStuffCourseType;
    String getiStuffDishType;
    String getiStuffMainIngredient;
    String getiStuffOccasionOrSeason;
    String getiStuffMiscellaneous;
    String getuStuffCuisinetype;
    String getuStuffCookingMethod;
    String getuStuffDietetic;
    String getuStuffCourseType;
    String getuStuffDishType;
    String getuStuffMainIngredient;
    String getuStuffOccasionOrSeason;
    String getuStuffMiscellaneous;
    String getuStuffFoodType;
    ArrayAdapter<CharSequence> adapter;
    int key = 1;
    int iCuisineType;
    int iCookingMethod;
    int iDietetic;
    int iCourseType;
    int iDishType;
    int iMainIngredient;
    int iOccasionOrSeason;
    int iMiscellaneous;
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
    int uCuisineType;
    int uCookingMethod;
    int uDietetic;
    int uCourseType;
    int uDishType;
    int uMainIngredient;
    int uOccasionOrSeason;
    int uMiscellaneous;
    String getulatitude;
    String getulongitude;
    TextView iarea;
    TextView icity;
    TextView icountry;
    TextView uarea;
    TextView ucity;
    TextView ucountry;
    Bundle fromeditquery;
    int iCuisineTypeposition;
    int iCookingMethodposition;
    int iDieteticposition;
    int iCourseTypeposition;
    int iDishTypeposition;
    int iMainIngredientposition;
    int iOccasionOrSeasonposition;
    int iMiscellaneousposition;
    int uCuisineTypeposition;
    int uCookingMethodposition;
    int uDieteticposition;
    int uCourseTypeposition;
    int uDishTypeposition;
    int uMainIngredientposition;
    int uOccasionOrSeasonposition;
    int uMiscellaneousposition;
    int getkey;
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

        //Opening the database Mobeegal
        try
        {
            //this.createDatabase("Mobeegal", 1, MODE_PRIVATE, null);
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor temprestaurantcursor = myDatabase.query("temprestaurant",
                    null, null, null, null, null, null);
        }
        catch (Exception e1)
        {

            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS temprestaurant" +
                    " (iCuisineTypeposition NUMERIC, iCookingMethodposition NUMERIC, " +
                    "iDieteticposition NUMERIC,iCourseTypeposition NUMERIC,iDishTypeposition NUMERIC,iMainIngredientposition NUMERIC," +
                    "iOccasionOrSeasonposition NUMERIC,iMiscellaneousposition NUMERIC ,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, " +
                    "uCuisineTypeposition NUMERIC, uCookingMethodposition NUMERIC, uDieteticposition NUMERIC,uCourseTypeposition NUMERIC," +
                    "uDishTypeposition NUMERIC,uMainIngredientposition NUMERIC,uOccasionOrSeasonposition NUMERIC,uMiscellaneousposition NUMERIC ," +
                    "uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR," +
                    " category VARCHAR, stufftype VARCHAR);");
            myDatabase.execSQL(
                    "INSERT INTO temprestaurant (iCuisineTypeposition,iCookingMethodposition ,iDieteticposition,iCourseTypeposition," +
                            "iDishTypeposition ,iMainIngredientposition ,iOccasionOrSeasonposition ,iMiscellaneousposition  ,iarea , icity , icountry ," +
                            " uCuisineTypeposition , uCookingMethodposition , uDieteticposition ,uCourseTypeposition ,uDishTypeposition ,uMainIngredientposition ," +
                            "uOccasionOrSeasonposition ,uMiscellaneousposition  ,uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude ," +
                            " category , stufftype  ) VALUES (" + iCuisineType +
                            "," + iCookingMethod + "," + iDietetic + "," +
                            iCourseType + "," + iDishType + "," +
                            "" + iMainIngredient + "," + iOccasionOrSeason +
                            "," + iMiscellaneous + ",'" + "" + "','" + "" +
                            "','" + "" + "'," + uCuisineType + "," +
                            "" + uCookingMethod + "," + uDietetic + "," +
                            uCourseType + "," + uDishType + "," +
                            uMainIngredient + "," + uOccasionOrSeason + "," +
                            "" + uMiscellaneous + ",'" + "" + "','" + "" +
                            "','" + "" + "','" + "" + "','" + "" + "','" + "" +
                            "','" + "" + "','" + "Restaurants" + "', '" +
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
            Cursor themecursor = myDatabase.query("Theme", null,
                    "catalog='Restaurants'", null, null, null, null);
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
        setContentView(R.layout.restaurants);
        //Creating a tab1 for ISTUFF

        final TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.iStuffprofile);
        one.setIndicator("Owner");
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.uStuffprofile);
        two.setIndicator("Customer");
        tabs.addTab(two);

        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor restaurantsCursor = myDatabase.query("temprestaurant", null,
                    null, null, null, null, null);

            if (restaurantsCursor != null)
            {
                if (restaurantsCursor.isFirst())
                {
                    do
                    {
                        iCuisineType = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iCuisineTypeposition"));
                        iCookingMethod = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iCookingMethodposition"));
                        iDietetic = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iDieteticposition"));
                        iCourseType = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iCourseTypeposition"));
                        iDishType = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iDishTypeposition"));
                        iMainIngredient = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iMainIngredientposition"));
                        iOccasionOrSeason = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iOccasionOrSeasonposition"));
                        iMiscellaneous = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "iMiscellaneousposition"));
                        getiarea = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "iarea"));
                        geticity = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "icity"));
                        geticountry = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "icountry"));
                        getilatitude = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "ilatitude"));
                        getilongitude = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "ilongitude"));

                        uCuisineType = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uCuisineTypeposition"));
                        uCookingMethod = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uCookingMethodposition"));
                        uDietetic = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uDieteticposition"));
                        uCourseType = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uCourseTypeposition"));
                        uDishType = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uDishTypeposition"));
                        uMainIngredient = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uMainIngredientposition"));
                        uOccasionOrSeason = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uOccasionOrSeasonposition"));
                        uMiscellaneous = restaurantsCursor
                                .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                        "uMiscellaneousposition"));
                        getuarea = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "uarea"));
                        getucity = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "ucity"));
                        getucountry = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "ucountry"));
                        getulatitude = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "ulatitude"));
                        getulongitude = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "ulongitude"));
                        getstufftype = restaurantsCursor.getString(
                                restaurantsCursor.getColumnIndexOrThrow(
                                        "stufftype"));
                    }
                    while (restaurantsCursor.moveToNext());
                }
            }
            restaurantsCursor.close();
        }
        catch (Exception e)
        {
        }

        final Spinner iStuffCuisinetype =
                (Spinner) findViewById(R.id.iStuffCuisinetype);
        ArrayAdapter<CharSequence> cuisinetypeAdapter =
                ArrayAdapter.createFromResource(
                        this, R.array.TypeofCuisine,
                        android.R.layout.simple_spinner_item);
        iStuffCuisinetype.setAdapter(cuisinetypeAdapter);
        iStuffCuisinetype.setSelection(iCuisineType);
        iStuffCuisinetype.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffCuisinetype =
                                (String) iStuffCuisinetype.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iCuisineTypeposition=" +
                                        position);
                        iCuisineTypeposition = position;
                        if (getiStuffCuisinetype.equals("TypeofCuisine"))
                        {
                            getiStuffCuisinetype = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner iStuffCookingMethod =
                (Spinner) findViewById(R.id.iStuffCookingMethod);
        ArrayAdapter<CharSequence> cookingMethodAdapter =
                ArrayAdapter.createFromResource(
                        this, R.array.CookingMethod,
                        android.R.layout.simple_spinner_item);
        iStuffCookingMethod.setAdapter(cookingMethodAdapter);
        iStuffCookingMethod.setSelection(iCookingMethod);
        iStuffCookingMethod.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffCookingMethod =
                                (String) iStuffCookingMethod.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iCookingMethodposition=" +
                                        position);
                        iCookingMethodposition = position;
                        if (getiStuffCookingMethod.equals("CookingMethod"))
                        {
                            getiStuffCookingMethod = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });


        final Spinner iStuffDietetic =
                (Spinner) findViewById(R.id.iStuffDietetic);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
                this, R.array.DieteticConsiderations,
                android.R.layout.simple_spinner_item);
        iStuffDietetic.setAdapter(adapter4);
        iStuffDietetic.setSelection(iDietetic);
        iStuffDietetic.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffDietetic =
                                (String) iStuffDietetic.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iDieteticposition=" +
                                        position);
                        iDieteticposition = position;
                        if (getiStuffDietetic.equals("DieteticConsiderations"))
                        {
                            getiStuffDietetic = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });


        final Spinner iStuffCourseType =
                (Spinner) findViewById(R.id.iStuffCourseType);
        ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(
                this, R.array.TypeofCourse,
                android.R.layout.simple_spinner_item);
        iStuffCourseType.setAdapter(adapter5);
        iStuffCourseType.setSelection(iCourseType);
        iStuffCourseType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffCourseType =
                                (String) iStuffCourseType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iCourseTypeposition=" +
                                        position);
                        iCourseTypeposition = position;
                        if (getiStuffCourseType.equals("TypeofCourse"))
                        {
                            getiStuffCourseType = "null";
                        }

                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });


        final Spinner iStuffDishType =
                (Spinner) findViewById(R.id.iStuffDishType);
        ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(
                this, R.array.TypeofDish, android.R.layout.simple_spinner_item);
        iStuffDishType.setAdapter(adapter6);
        iStuffDishType.setSelection(iDishType);
        iStuffDishType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffDishType =
                                (String) iStuffDishType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iDishTypeposition=" +
                                        position);
                        iDishTypeposition = position;
                        if (getiStuffDishType.equals("TypeofDish"))
                        {
                            getiStuffDishType = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner iStuffMainIngredient =
                (Spinner) findViewById(R.id.iStuffMainIngredient);
        ArrayAdapter<CharSequence> adapter7 = ArrayAdapter.createFromResource(
                this, R.array.MainIngredient,
                android.R.layout.simple_spinner_item);
        iStuffMainIngredient.setAdapter(adapter7);
        iStuffMainIngredient.setSelection(iMainIngredient);
        iStuffMainIngredient.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffMainIngredient =
                                (String) iStuffMainIngredient.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iMainIngredientposition=" +
                                        position);
                        iMainIngredientposition = position;
                        if (getiStuffMainIngredient.equals("MainIngredient"))
                        {
                            getiStuffMainIngredient = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner iStuffOccasionOrSeason =
                (Spinner) findViewById(R.id.iStuffOccasionOrSeason);
        ArrayAdapter<CharSequence> adapter8 = ArrayAdapter.createFromResource(
                this, R.array.OccasionOrSeason,
                android.R.layout.simple_spinner_item);
        iStuffOccasionOrSeason.setAdapter(adapter8);
        iStuffOccasionOrSeason.setSelection(iOccasionOrSeason);
        iStuffOccasionOrSeason.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffOccasionOrSeason =
                                (String) iStuffOccasionOrSeason
                                        .getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iOccasionOrSeasonposition=" +
                                        position);
                        iOccasionOrSeasonposition = position;
                        if (getiStuffOccasionOrSeason
                                .equals("OccasionOrSeason"))
                        {
                            getiStuffOccasionOrSeason = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner iStuffMiscellaneous =
                (Spinner) findViewById(R.id.iStuffMiscellaneous);
        ArrayAdapter<CharSequence> adapter9 = ArrayAdapter.createFromResource(
                this, R.array.Miscellaneous,
                android.R.layout.simple_spinner_item);
        iStuffMiscellaneous.setAdapter(adapter9);
        iStuffMiscellaneous.setSelection(iMiscellaneous);
        iStuffMiscellaneous.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getiStuffMiscellaneous =
                                (String) iStuffMiscellaneous.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set iMiscellaneousposition=" +
                                        position);
                        iMiscellaneousposition = position;
                        if (getiStuffMiscellaneous.equals("Miscellaneous"))
                        {
                            getiStuffMiscellaneous = "null";
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

        //Creating a tab2 for USTUFF

        final Spinner uStuffCuisinetype =
                (Spinner) findViewById(R.id.uStuffCuisinetype);
        ArrayAdapter<CharSequence> adapterA = ArrayAdapter.createFromResource(
                this, R.array.TypeofCuisine,
                android.R.layout.simple_spinner_item);
        uStuffCuisinetype.setAdapter(adapterA);
        uStuffCuisinetype.setSelection(uCuisineType);
        uStuffCuisinetype.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffCuisinetype =
                                (String) uStuffCuisinetype.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uCuisineTypeposition =" +
                                        position);
                        uCuisineTypeposition = position;
                        if (getuStuffCuisinetype.equals("TypeofCuisine"))
                        {
                            getuStuffCuisinetype = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });


        final Spinner uStuffCookingMethod =
                (Spinner) findViewById(R.id.uStuffCookingMethod);
        ArrayAdapter<CharSequence> adapterB = ArrayAdapter.createFromResource(
                this, R.array.CookingMethod,
                android.R.layout.simple_spinner_item);
        uStuffCookingMethod.setAdapter(adapterB);
        uStuffCookingMethod.setSelection(uCookingMethod);
        uStuffCookingMethod.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffCookingMethod =
                                (String) uStuffCookingMethod.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uCookingMethodposition=" +
                                        position);
                        uCookingMethodposition = position;
                        if (getuStuffCookingMethod.equals("CookingMethod"))
                        {
                            getuStuffCookingMethod = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffDietetic =
                (Spinner) findViewById(R.id.uStuffDietetic);
        ArrayAdapter<CharSequence> adapterC = ArrayAdapter.createFromResource(
                this, R.array.DieteticConsiderations,
                android.R.layout.simple_spinner_item);
        uStuffDietetic.setAdapter(adapterC);
        uStuffDietetic.setSelection(uDietetic);
        uStuffDietetic.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffDietetic =
                                (String) uStuffDietetic.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uDieteticposition=" +
                                        position);
                        uDieteticposition = position;
                        if (getuStuffDietetic.equals("DieteticConsiderations"))
                        {
                            getuStuffDietetic = "null";
                        }

                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffCourseType =
                (Spinner) findViewById(R.id.uStuffCourseType);
        ArrayAdapter<CharSequence> adapterD = ArrayAdapter.createFromResource(
                this, R.array.TypeofCourse,
                android.R.layout.simple_spinner_item);
        uStuffCourseType.setAdapter(adapterD);
        uStuffCourseType.setSelection(uCourseType);
        uStuffCourseType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffCourseType =
                                (String) uStuffCourseType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uCourseTypeposition=" +
                                        position);
                        uCourseTypeposition = position;
                        if (getuStuffCourseType.equals("TypeofCourse"))
                        {
                            getuStuffCourseType = "null";
                        }

                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffDishType =
                (Spinner) findViewById(R.id.uStuffDishType);
        ArrayAdapter<CharSequence> adapterE = ArrayAdapter.createFromResource(
                this, R.array.TypeofDish, android.R.layout.simple_spinner_item);
        uStuffDishType.setAdapter(adapterE);
        uStuffDishType.setSelection(uDishType);
        uStuffDishType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffDishType =
                                (String) uStuffDishType.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uDishTypeposition =" +
                                        position);
                        uDishTypeposition = position;
                        if (getuStuffDishType.equals("TypeofDish"))
                        {
                            getuStuffDishType = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffMainIngredient =
                (Spinner) findViewById(R.id.uStuffMainIngredient);
        ArrayAdapter<CharSequence> adapterF = ArrayAdapter.createFromResource(
                this, R.array.MainIngredient,
                android.R.layout.simple_spinner_item);
        uStuffMainIngredient.setAdapter(adapterF);
        uStuffMainIngredient.setSelection(uMainIngredient);
        uStuffMainIngredient.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffMainIngredient =
                                (String) uStuffMainIngredient.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uMainIngredientposition=" +
                                        position);
                        uMainIngredientposition = position;
                        if (getuStuffMainIngredient.equals("MainIngredient"))
                        {
                            getuStuffMainIngredient = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffOccasionOrSeason =
                (Spinner) findViewById(R.id.uStuffOccasionOrSeason);
        ArrayAdapter<CharSequence> adapterG = ArrayAdapter.createFromResource(
                this, R.array.OccasionOrSeason,
                android.R.layout.simple_spinner_item);
        uStuffOccasionOrSeason.setAdapter(adapterG);
        uStuffOccasionOrSeason.setSelection(uOccasionOrSeason);
        uStuffOccasionOrSeason.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffOccasionOrSeason =
                                (String) uStuffOccasionOrSeason
                                        .getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uOccasionOrSeasonposition=" +
                                        position);
                        uOccasionOrSeasonposition = position;
                        if (getuStuffOccasionOrSeason
                                .equals("OccasionOrSeason"))
                        {
                            getuStuffOccasionOrSeason = "null";
                        }
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });

        final Spinner uStuffMiscellaneous =
                (Spinner) findViewById(R.id.uStuffMiscellaneous);
        ArrayAdapter<CharSequence> adapterH = ArrayAdapter.createFromResource(
                this, R.array.Miscellaneous,
                android.R.layout.simple_spinner_item);
        uStuffMiscellaneous.setAdapter(adapterH);
        uStuffMiscellaneous.setSelection(uMiscellaneous);
        uStuffMiscellaneous.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        getuStuffMiscellaneous =
                                (String) uStuffMiscellaneous.getSelectedItem();
                        myDatabase.execSQL(
                                "UPDATE temprestaurant set uMiscellaneousposition=" +
                                        position);
                        uMiscellaneousposition = position;
                        if (getuStuffMiscellaneous.equals("Miscellaneous"))
                        {
                            getuStuffMiscellaneous = "null";
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
                        "UPDATE temprestaurant set stufftype='" + "istuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Restaurants.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprestaurant");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Restaurants.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprestaurant");
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
                        "UPDATE temprestaurant set stufftype='" + "ustuff'");
                if (viewtypename.equals("MapView"))
                {
                    Intent locationfinder =
                            new Intent(Restaurants.this, LocationFinder.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprestaurant");
                    b.putInt("key", getkey);
                    locationfinder.putExtras(b);
                    startActivityForResult(locationfinder, 0);
                }
                else
                {
                    Intent textview =
                            new Intent(Restaurants.this, TextLocations.class);
                    Bundle b = new Bundle();
                    b.putString("tablename", "temprestaurant");
                    b.putInt("key", getkey);
                    textview.putExtras(b);
                    startActivityForResult(textview, 0);
                }
            }
        });
//ended
        final Button save = (Button) findViewById(R.id.Save);
        save.setOnClickListener(new Button.OnClickListener()
        {

            private double getilongitudes;
            private double getilatitudes;
            private String idetails;

            public void onClick(View v)
            {
                getiStuffCuisinetype =
                        (String) iStuffCuisinetype.getSelectedItem();
                getiStuffCookingMethod =
                        (String) iStuffCookingMethod.getSelectedItem();
                getiStuffDietetic = (String) iStuffDietetic.getSelectedItem();
                getiStuffCourseType =
                        (String) iStuffCourseType.getSelectedItem();
                getiStuffDishType = (String) iStuffDishType.getSelectedItem();
                getiStuffMainIngredient =
                        (String) iStuffMainIngredient.getSelectedItem();
                getiStuffOccasionOrSeason =
                        (String) iStuffOccasionOrSeason.getSelectedItem();
                getiStuffMiscellaneous =
                        (String) iStuffMiscellaneous.getSelectedItem();

                iCuisineTypeposition =
                        iStuffCuisinetype.getSelectedItemPosition();
                iCookingMethodposition =
                        iStuffCookingMethod.getSelectedItemPosition();
                iDieteticposition = iStuffDietetic.getSelectedItemPosition();
                iCourseTypeposition =
                        iStuffCourseType.getSelectedItemPosition();
                iDishTypeposition = iStuffDishType.getSelectedItemPosition();
                iMainIngredientposition =
                        iStuffMainIngredient.getSelectedItemPosition();
                iOccasionOrSeasonposition =
                        iStuffOccasionOrSeason.getSelectedItemPosition();
                iMiscellaneousposition =
                        iStuffMiscellaneous.getSelectedItemPosition();

                idetails = "Cuisinetype=" + getiStuffCuisinetype +
                        " CookingMethod=" + getiStuffCookingMethod +
                        " Dietetic=" + getiStuffDietetic + " CourseType=" +
                        getiStuffCourseType + " DishType=" + getiStuffDishType +
                        " MainIngredient=" + getiStuffMainIngredient +
                        " OccasionOrSeason=" + getiStuffOccasionOrSeason +
                        " Miscellaneous=" + getiStuffMiscellaneous + " Area=" +
                        getiarea + " City=" + geticity + " country=" +
                        geticountry;

                getilatitudes = Double.parseDouble(getilatitude);
                getilatitudes = getilatitudes * 1E6;
                getilongitudes = Double.parseDouble(getilongitude);
                getilongitudes = getilongitudes * 1E6;


                if (fromeditquery != null && getkey != 0)
                {
                    myDatabase.execSQL(
                            "UPDATE restaurantsposition set iCuisineTypeposition=" +
                                    iCuisineTypeposition +
                                    ", iCookingMethodposition=" +
                                    iCookingMethodposition +
                                    ", iDieteticposition=" + iDieteticposition +
                                    ",iCourseTypeposition= " +
                                    iCourseTypeposition +
                                    ",iDishTypeposition= " + iDishTypeposition +
                                    ",iMainIngredientposition=" +
                                    iMainIngredientposition +
                                    ",iOccasionOrSeasonposition= " +
                                    iOccasionOrSeasonposition +
                                    ",iMiscellaneousposition=" +
                                    iMiscellaneousposition + ",iarea='" +
                                    getiarea + "', icity='" + geticity +
                                    "', icountry='" + geticountry +
                                    "',uCuisineTypeposition=" +
                                    uCuisineTypeposition +
                                    ", uCookingMethodposition=" +
                                    uCookingMethodposition +
                                    ", uDieteticposition=" + uDieteticposition +
                                    ",uCourseTypeposition= " +
                                    uCourseTypeposition +
                                    ",uDishTypeposition= " + uDishTypeposition +
                                    ",uMainIngredientposition=" +
                                    uMainIngredientposition +
                                    ",uOccasionOrSeasonposition= " +
                                    uOccasionOrSeasonposition +
                                    ",uMiscellaneousposition=" +
                                    uMiscellaneousposition + ", uarea='" +
                                    getuarea + "', ucity='" + getucity +
                                    "', ucountry='" + getucountry +
                                    "',  ilatitude='" + getilatitude +
                                    "', ilongitude='" + getilongitude +
                                    "',ulatitude='" + getulatitude +
                                    "',ulongitude='" + getulongitude +
                                    "' where key=" + getkey + ";");
                    myDatabase.execSQL(
                            "UPDATE  Restaurants set iStuffCuisinetype='" +
                                    getiStuffCuisinetype +
                                    "', iStuffCookingMethod='" +
                                    getiStuffCookingMethod +
                                    "', iStuffDietetic='" + getiStuffDietetic +
                                    "', iStuffCourseType='" +
                                    getiStuffCourseType + "',iStuffDishType='" +
                                    getiStuffDishType +
                                    "',iStuffMainIngredient = '" +
                                    getiStuffMainIngredient +
                                    "',iStuffOccasionOrSeason='" +
                                    getiStuffOccasionOrSeason +
                                    "',iStuffMiscellaneous='" +
                                    getiStuffMiscellaneous + "', iarea='" +
                                    getiarea + "', icity='" + geticity +
                                    "', icountry='" + geticountry +
                                    "', uStuffCuisinetype='" +
                                    getuStuffCuisinetype +
                                    "', uStuffCookingMethod='" +
                                    getuStuffCookingMethod +
                                    "', uStuffDietetic='" + getuStuffDietetic +
                                    "', uStuffCourseType='" +
                                    getuStuffCourseType + "',uStuffDishType='" +
                                    getuStuffDishType +
                                    "',uStuffMainIngredient = '" +
                                    getuStuffMainIngredient +
                                    "',uStuffOccasionOrSeason='" +
                                    getuStuffOccasionOrSeason +
                                    "',uStuffMiscellaneous='" +
                                    getuStuffMiscellaneous + "', uarea='" +
                                    getuarea + "', ucity='" + getucity +
                                    "', ucountry='" + getucountry +
                                    "', ilatitude='" + getilatitude +
                                    "', ilongitude='" + getilongitude +
                                    "', ulatitude='" + getulatitude +
                                    "', ulongitude='" + getulongitude +
                                    "',queryDate=DATE('NOW') where key=" +
                                    getkey + ";");
                    myDatabase.execSQL("update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';");
                    myDatabase.execSQL("Update mStuffdetails set details='" +
                            idetails + "', latitude='" + getilatitudes +
                            "', longitude='" + getilongitudes +
                            "', location='" + geticountry +
                            "' where catagory='" + "userRestaurant" + "';");
                }
                else
                {
                    try
                    {
                        myDatabase.execSQL(
                                "INSERT INTO Restaurants(iStuffCuisinetype,iStuffCookingMethod,iStuffDietetic,iStuffCourseType,iStuffDishType,iStuffMainIngredient,iStuffOccasionOrSeason,iStuffMiscellaneous,iarea,icity,icountry,ilatitude,ilongitude,uStuffCuisinetype,uStuffCookingMethod,uStuffDietetic,uStuffCourseType,uStuffDishType,uStuffMainIngredient,uStuffOccasionOrSeason,uStuffMiscellaneous,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES ('" +
                                        getiStuffCuisinetype + "','" +
                                        getiStuffCookingMethod + "','" +
                                        getiStuffDietetic + "','" +
                                        getiStuffCourseType + "','" +
                                        getiStuffDishType + "','" +
                                        getiStuffMainIngredient + "','" +
                                        getiStuffOccasionOrSeason + "','" +
                                        getiStuffMiscellaneous + "','" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getuStuffCuisinetype + "','" +
                                        getuStuffCookingMethod + "','" +
                                        getuStuffDietetic + "','" +
                                        getuStuffCourseType + "','" +
                                        getuStuffDishType + "','" +
                                        getuStuffMainIngredient + "','" +
                                        getuStuffOccasionOrSeason + "','" +
                                        getuStuffMiscellaneous + "','" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getulatitude +
                                        "','" + getulongitude + "','" + "true" +
                                        "');");
                        myDatabase.execSQL("update category set querystatus='" +
                                "true" + "' where status='" + "true" + "';");
                        myDatabase.execSQL(
                                "INSERT INTO restaurantsposition (iCuisineTypeposition,iCookingMethodposition ,iDieteticposition,iCourseTypeposition,iDishTypeposition ,iMainIngredientposition ,iOccasionOrSeasonposition ,iMiscellaneousposition  ,iarea , icity , icountry , uCuisineTypeposition , uCookingMethodposition , uDieteticposition ,uCourseTypeposition ,uDishTypeposition ,uMainIngredientposition ,uOccasionOrSeasonposition ,uMiscellaneousposition  ,uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype  ) VALUES (" +
                                        iCuisineTypeposition + "," +
                                        iCookingMethodposition + "," +
                                        iDieteticposition + "," +
                                        iCourseTypeposition + "," +
                                        iDishTypeposition + "," +
                                        iMainIngredientposition + "," +
                                        iOccasionOrSeasonposition + "," +
                                        iMiscellaneousposition + ",'" +
                                        getiarea + "','" + geticity + "','" +
                                        geticountry + "'," +
                                        uCuisineTypeposition + "," +
                                        uCookingMethodposition + "," +
                                        uDieteticposition + "," +
                                        uCourseTypeposition + "," +
                                        uDishTypeposition + "," +
                                        uMainIngredientposition + "," +
                                        uOccasionOrSeasonposition + "," +
                                        uMiscellaneousposition + ",'" +
                                        getuarea + "','" + getucity + "','" +
                                        getucountry + "','" + getilatitude +
                                        "','" + getilongitude + "','" +
                                        getulatitude + "','" + getulongitude +
                                        "','" + "Restaurants" + "', '" +
                                        "istuff" + "');");
                        myDatabase.execSQL(
                                "Update mStuffdetails set details='" +
                                        idetails + "', latitude='" +
                                        getilatitudes + "', longitude='" +
                                        getilongitudes + "', location='" +
                                        geticountry + "' where catagory='" +
                                        "userRestaurant" + "';");
                    }
                    catch (Exception e)
                    {
//                        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Restaurants(key INTEGER PRIMARY KEY,iStuffCuisinetype VARCHAR,iStuffCookingMethod VARCHAR,iStuffDietetic VARCHAR,iStuffCourseType VARCHAR,iStuffDishType VARCHAR,iStuffMainIngredient VARCHAR,iStuffOccasionOrSeason VARCHAR,iStuffMiscellaneous VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,uStuffCuisinetype VARCHAR,uStuffCookingMethod VARCHAR,uStuffDietetic VARCHAR,uStuffCourseType VARCHAR,uStuffDishType VARCHAR,uStuffMainIngredient VARCHAR,uStuffOccasionOrSeason VARCHAR,uStuffMiscellaneous VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);");
//                        myDatabase.execSQL("CREATE TRIGGER insert_querydate_Restaurants after INSERT on Restaurants BEGIN update Restaurants set queryDate=DATE('NOW') WHERE key=new.key; END;");
//                        myDatabase.execSQL("CREATE TRIGGER delete_querydate_Restaurants before insert on Restaurants BEGIN delete from Restaurants where queryDate<DATE('NOW','-7 day');END;");
//                        myDatabase.execSQL("INSERT INTO Restaurants(iStuffCuisinetype,iStuffCookingMethod,iStuffDietetic,iStuffCourseType,iStuffDishType,iStuffMainIngredient,iStuffOccasionOrSeason,iStuffMiscellaneous,iarea,icity,icountry,ilatitude,ilongitude,uStuffCuisinetype,uStuffCookingMethod,uStuffDietetic,uStuffCourseType,uStuffDishType,uStuffMainIngredient,uStuffOccasionOrSeason,uStuffMiscellaneous,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES ('" + getiStuffCuisinetype + "','" + getiStuffCookingMethod + "','" + getiStuffDietetic + "','" + getiStuffCourseType + "','" + getiStuffDishType + "','" + getiStuffMainIngredient + "','" + getiStuffOccasionOrSeason + "','" + getiStuffMiscellaneous + "','" + getiarea + "','" + geticity + "','" + geticountry + "','" + getilatitude + "','" + getilongitude + "','" + getuStuffCuisinetype + "','" + getuStuffCookingMethod + "','" + getuStuffDietetic + "','" + getuStuffCourseType + "','" + getuStuffDishType + "','" + getuStuffMainIngredient + "','" + getuStuffOccasionOrSeason + "','" + getuStuffMiscellaneous + "','" + getuarea + "','" + getucity + "','" + getucountry + "','" + getulatitude + "','" + getulongitude + "','" + "true" + "');");
//                        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS restaurantsposition" + " (key INTEGER PRIMARY KEY,iCuisineTypeposition NUMERIC, iCookingMethodposition NUMERIC, " +
//                                "iDieteticposition NUMERIC,iCourseTypeposition NUMERIC,iDishTypeposition NUMERIC,iMainIngredientposition NUMERIC," +
//                                "iOccasionOrSeasonposition NUMERIC,iMiscellaneousposition NUMERIC ,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, " +
//                                "uCuisineTypeposition NUMERIC, uCookingMethodposition NUMERIC, uDieteticposition NUMERIC,uCourseTypeposition NUMERIC," +
//                                "uDishTypeposition NUMERIC,uMainIngredientposition NUMERIC,uOccasionOrSeasonposition NUMERIC,uMiscellaneousposition NUMERIC ," +
//                                "uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR," +
//                                " category VARCHAR, stufftype VARCHAR);");
//                        myDatabase.execSQL("INSERT INTO restaurantsposition (iCuisineTypeposition,iCookingMethodposition ,iDieteticposition,iCourseTypeposition,iDishTypeposition ,iMainIngredientposition ,iOccasionOrSeasonposition ,iMiscellaneousposition  ,iarea , icity , icountry , uCuisineTypeposition , uCookingMethodposition , uDieteticposition ,uCourseTypeposition ,uDishTypeposition ,uMainIngredientposition ,uOccasionOrSeasonposition ,uMiscellaneousposition  ,uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype  ) VALUES (" + iCuisineTypeposition + "," + iCookingMethodposition + "," + iDieteticposition + "," + iCourseTypeposition + "," + iDishTypeposition + "," + iMainIngredientposition + "," + iOccasionOrSeasonposition + "," + iMiscellaneousposition + ",'" + getiarea + "','" + geticity + "','" + geticountry + "'," + uCuisineTypeposition + "," + uCookingMethodposition + "," + uDieteticposition + "," + uCourseTypeposition + "," + uDishTypeposition + "," + uMainIngredientposition + "," + uOccasionOrSeasonposition + "," + uMiscellaneousposition + ",'" + getuarea + "','" + getucity + "','" + getucountry + "','" + getilatitude + "','" + getilongitude + "','" + getulatitude + "','" + getulongitude + "','" + "Restaurants" + "', '" + "istuff" + "');");
//                        myDatabase.execSQL("update category set querystatus='" + "true" + "' where status='" + "true" + "';");
//                        myDatabase.execSQL("Update mStuffdetails set details='" + idetails + "', latitude='" + getilatitudes + "', longitude='" + getilongitudes + "', location='" + geticountry + "' where catagory='" + "userRestaurant" + "';");
                    }
                }
                myDatabase.execSQL("drop table " + "temprestaurant" + ";");
                if (myDatabase != null)
                {
                    myDatabase.close();
                }
                Intent intent =
                        new Intent(Restaurants.this, FindandInstall.class);
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
                Intent intent1 =
                        new Intent(Restaurants.this, MapResults.class);
                startActivityForResult(intent1, 0);
                finish();
                break;

            case 2:
                Intent intent2 =
                        new Intent(Restaurants.this, FindandInstall.class);
                startActivityForResult(intent2, 0);
                finish();
                break;

            case 3:
                Intent intent3 = new Intent(Restaurants.this, Settings.class);
                startActivityForResult(intent3, 0);
                finish();
                break;
            case 4:

                Intent intent =
                        new Intent(Restaurants.this, FindandInstall.class);
                startActivityForResult(intent, 0);
                finish();
                break;
            case 5:
                try
                {
                    Cursor c = myDatabase.query("Restaurants", null, null, null,
                            null, null, null);
                    Intent listmStuff = new Intent(Restaurants.this,
                            RestaurantsListQuery.class);
                    startActivityForResult(listmStuff, 0);
                }
                catch (Exception e)
                {
                    Toast.makeText(Restaurants.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                try
                {
                    Cursor c = myDatabase.query("Restaurants", null, null, null,
                            null, null, null);
                    Intent deleteQuery1 = new Intent(Restaurants.this,
                            DeleteRestaurants.class);
                    startActivityForResult(deleteQuery1, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Restaurants.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 7:
                try
                {
                    Cursor c = myDatabase.query("Restaurants", null, null, null,
                            null, null, null);
                    Intent viewQuery = new Intent(Restaurants.this,
                            RestaurantsViewQuery.class);
                    startActivityForResult(viewQuery, 0);
                    finish();
                }
                catch (Exception e)
                {
                    Toast.makeText(Restaurants.this, "Please fill the form",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 8:
                Bundle b1 = new Bundle();
                Intent settheme =
                        new Intent(Restaurants.this, SettingTheme.class);
                b1.putString("value1", "Restaurants");
                b1.putString("class", "5");
                settheme.putExtras(b1);
                startActivityForResult(settheme, 0);
                finish();
            case 9:
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", "Restaurants");
                Intent mediaintent =
                        new Intent(Restaurants.this, Uploadmultimedia.class);
                mediaintent.putExtras(B1);
                startActivity(mediaintent);
        }
        return super.onOptionsItemSelected(item);
    }
}



