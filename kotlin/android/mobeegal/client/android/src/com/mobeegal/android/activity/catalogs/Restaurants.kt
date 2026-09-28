package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Restaurants.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TabHost
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.activity.Uploadmultimedia
import com.mobeegal.android.util.ViewMenu

class Restaurants : Activity() {

    var myDatabase: SQLiteDatabase? = null
    var getiStuffCuisinetype: String? = null
    var getiStuffCookingMethod: String? = null
    var getiStuffDietetic: String? = null
    var getiStuffCourseType: String? = null
    var getiStuffDishType: String? = null
    var getiStuffMainIngredient: String? = null
    var getiStuffOccasionOrSeason: String? = null
    var getiStuffMiscellaneous: String? = null
    var getuStuffCuisinetype: String? = null
    var getuStuffCookingMethod: String? = null
    var getuStuffDietetic: String? = null
    var getuStuffCourseType: String? = null
    var getuStuffDishType: String? = null
    var getuStuffMainIngredient: String? = null
    var getuStuffOccasionOrSeason: String? = null
    var getuStuffMiscellaneous: String? = null
    var getuStuffFoodType: String? = null
    var adapter: ArrayAdapter<CharSequence>? = null
    var key: Int = 1
    var iCuisineType: Int = 0
    var iCookingMethod: Int = 0
    var iDietetic: Int = 0
    var iCourseType: Int = 0
    var iDishType: Int = 0
    var iMainIngredient: Int = 0
    var iOccasionOrSeason: Int = 0
    var iMiscellaneous: Int = 0
    var getiarea: String? = null
    var geticity: String? = null
    var geticountry: String? = null
    var getuarea: String? = null
    var getucity: String? = null
    var getucountry: String? = null
    var getcategory: String? = null
    var getstufftype: String? = null
    var getilatitude: String? = null
    var getilongitude: String? = null
    var uCuisineType: Int = 0
    var uCookingMethod: Int = 0
    var uDietetic: Int = 0
    var uCourseType: Int = 0
    var uDishType: Int = 0
    var uMainIngredient: Int = 0
    var uOccasionOrSeason: Int = 0
    var uMiscellaneous: Int = 0
    var getulatitude: String? = null
    var getulongitude: String? = null
    var iarea: TextView? = null
    var icity: TextView? = null
    var icountry: TextView? = null
    var uarea: TextView? = null
    var ucity: TextView? = null
    var ucountry: TextView? = null
    var fromeditquery: Bundle? = null
    var iCuisineTypeposition: Int = 0
    var iCookingMethodposition: Int = 0
    var iDieteticposition: Int = 0
    var iCourseTypeposition: Int = 0
    var iDishTypeposition: Int = 0
    var iMainIngredientposition: Int = 0
    var iOccasionOrSeasonposition: Int = 0
    var iMiscellaneousposition: Int = 0
    var uCuisineTypeposition: Int = 0
    var uCookingMethodposition: Int = 0
    var uDieteticposition: Int = 0
    var uCourseTypeposition: Int = 0
    var uDishTypeposition: Int = 0
    var uMainIngredientposition: Int = 0
    var uOccasionOrSeasonposition: Int = 0
    var uMiscellaneousposition: Int = 0
    var getkey: Int = 0
    var theme: Int = 0
    private var viewtypename: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        fromeditquery = this.intent.extras
        if (fromeditquery != null) {
            getkey = fromeditquery!!.getInt("key")
        }

        //Opening the database Mobeegal
        try {
            //this.createDatabase("Mobeegal", 1, MODE_PRIVATE, null);
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val temprestaurantcursor = myDatabase!!.query(
                "temprestaurant",
                null, null, null, null, null, null
            )
        } catch (e1: Exception) {
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS temprestaurant" +
                    " (iCuisineTypeposition NUMERIC, iCookingMethodposition NUMERIC, " +
                    "iDieteticposition NUMERIC,iCourseTypeposition NUMERIC,iDishTypeposition NUMERIC,iMainIngredientposition NUMERIC," +
                    "iOccasionOrSeasonposition NUMERIC,iMiscellaneousposition NUMERIC ,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, " +
                    "uCuisineTypeposition NUMERIC, uCookingMethodposition NUMERIC, uDieteticposition NUMERIC,uCourseTypeposition NUMERIC," +
                    "uDishTypeposition NUMERIC,uMainIngredientposition NUMERIC,uOccasionOrSeasonposition NUMERIC,uMiscellaneousposition NUMERIC ," +
                    "uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR," +
                    " category VARCHAR, stufftype VARCHAR);"
            )
            myDatabase!!.execSQL(
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
                    "istuff" + "');"
            )
        } finally {
            myDatabase?.close()
        }

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val themecursor = myDatabase!!.query(
                "Theme", null,
                "catalog='Restaurants'", null, null, null, null
            )
            if (themecursor != null) {
                if (themecursor.isFirst) {
                    do {
                        theme = themecursor.getInt(
                            themecursor.getColumnIndexOrThrow(
                                "theme"
                            )
                        )
                        //String catalog = themecursor.getString(themecursor.getColumnIndexOrThrow("theme"));
                    } while (themecursor.moveToNext())
                }
            }
        } catch (e1: Exception) {
        }
        if (theme == 0) {
            this.setTheme(android.R.style.Theme_Black)
        }
        if (theme == 1) {
            this.setTheme(android.R.style.Theme_Dialog)
        }
        if (theme == 2) {
            this.setTheme(android.R.style.Theme)
        }
        if (theme == 3) {
            this.setTheme(android.R.style.Theme)
        }
        setContentView(R.layout.restaurants)
        //Creating a tab1 for ISTUFF

        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.iStuffprofile)
        one.setIndicator("Owner")
        tabs.addTab(one)

        val two = tabs.newTabSpec("two")
        two.setContent(R.id.uStuffprofile)
        two.setIndicator("Customer")
        tabs.addTab(two)

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val restaurantsCursor = myDatabase!!.query(
                "temprestaurant", null,
                null, null, null, null, null
            )

            if (restaurantsCursor != null) {
                if (restaurantsCursor.isFirst) {
                    do {
                        iCuisineType = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iCuisineTypeposition"
                            )
                        )
                        iCookingMethod = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iCookingMethodposition"
                            )
                        )
                        iDietetic = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iDieteticposition"
                            )
                        )
                        iCourseType = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iCourseTypeposition"
                            )
                        )
                        iDishType = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iDishTypeposition"
                            )
                        )
                        iMainIngredient = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iMainIngredientposition"
                            )
                        )
                        iOccasionOrSeason = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iOccasionOrSeasonposition"
                            )
                        )
                        iMiscellaneous = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iMiscellaneousposition"
                            )
                        )
                        getiarea = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "iarea"
                            )
                        )
                        geticity = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "icity"
                            )
                        )
                        geticountry = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "icountry"
                            )
                        )
                        getilatitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "ilatitude"
                            )
                        )
                        getilongitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "ilongitude"
                            )
                        )

                        uCuisineType = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uCuisineTypeposition"
                            )
                        )
                        uCookingMethod = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uCookingMethodposition"
                            )
                        )
                        uDietetic = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uDieteticposition"
                            )
                        )
                        uCourseType = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uCourseTypeposition"
                            )
                        )
                        uDishType = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uDishTypeposition"
                            )
                        )
                        uMainIngredient = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uMainIngredientposition"
                            )
                        )
                        uOccasionOrSeason = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uOccasionOrSeasonposition"
                            )
                        )
                        uMiscellaneous = restaurantsCursor.getInt(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uMiscellaneousposition"
                            )
                        )
                        getuarea = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "uarea"
                            )
                        )
                        getucity = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "ucity"
                            )
                        )
                        getucountry = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "ucountry"
                            )
                        )
                        getulatitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "ulatitude"
                            )
                        )
                        getulongitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "ulongitude"
                            )
                        )
                        getstufftype = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                "stufftype"
                            )
                        )
                    } while (restaurantsCursor.moveToNext())
                }
            }
            restaurantsCursor.close()
        } catch (e: Exception) {
        }

        val iStuffCuisinetype = findViewById(R.id.iStuffCuisinetype) as Spinner
        val cuisinetypeAdapter = ArrayAdapter.createFromResource(
            this, R.array.TypeofCuisine,
            android.R.layout.simple_spinner_item
        )
        iStuffCuisinetype.adapter = cuisinetypeAdapter
        iStuffCuisinetype.setSelection(iCuisineType)
        iStuffCuisinetype.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffCuisinetype =
                        iStuffCuisinetype.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iCuisineTypeposition=" +
                            position
                    )
                    iCuisineTypeposition = position
                    if (getiStuffCuisinetype == "TypeofCuisine") {
                        getiStuffCuisinetype = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffCookingMethod = findViewById(R.id.iStuffCookingMethod) as Spinner
        val cookingMethodAdapter = ArrayAdapter.createFromResource(
            this, R.array.CookingMethod,
            android.R.layout.simple_spinner_item
        )
        iStuffCookingMethod.adapter = cookingMethodAdapter
        iStuffCookingMethod.setSelection(iCookingMethod)
        iStuffCookingMethod.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffCookingMethod =
                        iStuffCookingMethod.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iCookingMethodposition=" +
                            position
                    )
                    iCookingMethodposition = position
                    if (getiStuffCookingMethod == "CookingMethod") {
                        getiStuffCookingMethod = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffDietetic = findViewById(R.id.iStuffDietetic) as Spinner
        val adapter4 = ArrayAdapter.createFromResource(
            this, R.array.DieteticConsiderations,
            android.R.layout.simple_spinner_item
        )
        iStuffDietetic.adapter = adapter4
        iStuffDietetic.setSelection(iDietetic)
        iStuffDietetic.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffDietetic =
                        iStuffDietetic.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iDieteticposition=" +
                            position
                    )
                    iDieteticposition = position
                    if (getiStuffDietetic == "DieteticConsiderations") {
                        getiStuffDietetic = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffCourseType = findViewById(R.id.iStuffCourseType) as Spinner
        val adapter5 = ArrayAdapter.createFromResource(
            this, R.array.TypeofCourse,
            android.R.layout.simple_spinner_item
        )
        iStuffCourseType.adapter = adapter5
        iStuffCourseType.setSelection(iCourseType)
        iStuffCourseType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffCourseType =
                        iStuffCourseType.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iCourseTypeposition=" +
                            position
                    )
                    iCourseTypeposition = position
                    if (getiStuffCourseType == "TypeofCourse") {
                        getiStuffCourseType = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffDishType = findViewById(R.id.iStuffDishType) as Spinner
        val adapter6 = ArrayAdapter.createFromResource(
            this, R.array.TypeofDish,
            android.R.layout.simple_spinner_item
        )
        iStuffDishType.adapter = adapter6
        iStuffDishType.setSelection(iDishType)
        iStuffDishType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffDishType =
                        iStuffDishType.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iDishTypeposition=" +
                            position
                    )
                    iDishTypeposition = position
                    if (getiStuffDishType == "TypeofDish") {
                        getiStuffDishType = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffMainIngredient = findViewById(R.id.iStuffMainIngredient) as Spinner
        val adapter7 = ArrayAdapter.createFromResource(
            this, R.array.MainIngredient,
            android.R.layout.simple_spinner_item
        )
        iStuffMainIngredient.adapter = adapter7
        iStuffMainIngredient.setSelection(iMainIngredient)
        iStuffMainIngredient.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffMainIngredient =
                        iStuffMainIngredient.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iMainIngredientposition=" +
                            position
                    )
                    iMainIngredientposition = position
                    if (getiStuffMainIngredient == "MainIngredient") {
                        getiStuffMainIngredient = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffOccasionOrSeason = findViewById(R.id.iStuffOccasionOrSeason) as Spinner
        val adapter8 = ArrayAdapter.createFromResource(
            this, R.array.OccasionOrSeason,
            android.R.layout.simple_spinner_item
        )
        iStuffOccasionOrSeason.adapter = adapter8
        iStuffOccasionOrSeason.setSelection(iOccasionOrSeason)
        iStuffOccasionOrSeason.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffOccasionOrSeason =
                        iStuffOccasionOrSeason.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iOccasionOrSeasonposition=" +
                            position
                    )
                    iOccasionOrSeasonposition = position
                    if (getiStuffOccasionOrSeason == "OccasionOrSeason") {
                        getiStuffOccasionOrSeason = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffMiscellaneous = findViewById(R.id.iStuffMiscellaneous) as Spinner
        val adapter9 = ArrayAdapter.createFromResource(
            this, R.array.Miscellaneous,
            android.R.layout.simple_spinner_item
        )
        iStuffMiscellaneous.adapter = adapter9
        iStuffMiscellaneous.setSelection(iMiscellaneous)
        iStuffMiscellaneous.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffMiscellaneous =
                        iStuffMiscellaneous.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set iMiscellaneousposition=" +
                            position
                    )
                    iMiscellaneousposition = position
                    if (getiStuffMiscellaneous == "Miscellaneous") {
                        getiStuffMiscellaneous = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        if (getstufftype == "istuff") {
            tabs.setCurrentTab(0)
        } else if (getstufftype == "ustuff") {
            tabs.setCurrentTab(1)
        }

        iarea = findViewById(R.id.iarea) as TextView
        iarea!!.setText(getiarea)
        icity = findViewById(R.id.icity) as TextView
        icity!!.setText(geticity)
        icountry = findViewById(R.id.icountry) as TextView
        icountry!!.setText(geticountry)
        uarea = findViewById(R.id.uarea) as TextView
        uarea!!.setText(getuarea)
        ucity = findViewById(R.id.ucity) as TextView
        ucity!!.setText(getucity)
        ucountry = findViewById(R.id.ucountry) as TextView
        ucountry!!.setText(getucountry)

        //Creating a tab2 for USTUFF


        val uStuffCuisinetype = findViewById(R.id.uStuffCuisinetype) as Spinner
        val adapterA = ArrayAdapter.createFromResource(
            this, R.array.TypeofCuisine,
            android.R.layout.simple_spinner_item
        )
        uStuffCuisinetype.adapter = adapterA
        uStuffCuisinetype.setSelection(uCuisineType)
        uStuffCuisinetype.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffCuisinetype =
                        uStuffCuisinetype.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uCuisineTypeposition =" +
                            position
                    )
                    uCuisineTypeposition = position
                    if (getuStuffCuisinetype == "TypeofCuisine") {
                        getuStuffCuisinetype = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffCookingMethod = findViewById(R.id.uStuffCookingMethod) as Spinner
        val adapterB = ArrayAdapter.createFromResource(
            this, R.array.CookingMethod,
            android.R.layout.simple_spinner_item
        )
        uStuffCookingMethod.adapter = adapterB
        uStuffCookingMethod.setSelection(uCookingMethod)
        uStuffCookingMethod.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffCookingMethod =
                        uStuffCookingMethod.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uCookingMethodposition=" +
                            position
                    )
                    uCookingMethodposition = position
                    if (getuStuffCookingMethod == "CookingMethod") {
                        getuStuffCookingMethod = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffDietetic = findViewById(R.id.uStuffDietetic) as Spinner
        val adapterC = ArrayAdapter.createFromResource(
            this, R.array.DieteticConsiderations,
            android.R.layout.simple_spinner_item
        )
        uStuffDietetic.adapter = adapterC
        uStuffDietetic.setSelection(uDietetic)
        uStuffDietetic.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffDietetic =
                        uStuffDietetic.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uDieteticposition=" +
                            position
                    )
                    uDieteticposition = position
                    if (getuStuffDietetic == "DieteticConsiderations") {
                        getuStuffDietetic = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffCourseType = findViewById(R.id.uStuffCourseType) as Spinner
        val adapterD = ArrayAdapter.createFromResource(
            this, R.array.TypeofCourse,
            android.R.layout.simple_spinner_item
        )
        uStuffCourseType.adapter = adapterD
        uStuffCourseType.setSelection(uCourseType)
        uStuffCourseType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffCourseType =
                        uStuffCourseType.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uCourseTypeposition=" +
                            position
                    )
                    uCourseTypeposition = position
                    if (getuStuffCourseType == "TypeofCourse") {
                        getuStuffCourseType = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffDishType = findViewById(R.id.uStuffDishType) as Spinner
        val adapterE = ArrayAdapter.createFromResource(
            this, R.array.TypeofDish,
            android.R.layout.simple_spinner_item
        )
        uStuffDishType.adapter = adapterE
        uStuffDishType.setSelection(uDishType)
        uStuffDishType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffDishType =
                        uStuffDishType.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uDishTypeposition =" +
                            position
                    )
                    uDishTypeposition = position
                    if (getuStuffDishType == "TypeofDish") {
                        getuStuffDishType = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffMainIngredient = findViewById(R.id.uStuffMainIngredient) as Spinner
        val adapterF = ArrayAdapter.createFromResource(
            this, R.array.MainIngredient,
            android.R.layout.simple_spinner_item
        )
        uStuffMainIngredient.adapter = adapterF
        uStuffMainIngredient.setSelection(uMainIngredient)
        uStuffMainIngredient.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffMainIngredient =
                        uStuffMainIngredient.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uMainIngredientposition=" +
                            position
                    )
                    uMainIngredientposition = position
                    if (getuStuffMainIngredient == "MainIngredient") {
                        getuStuffMainIngredient = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffOccasionOrSeason = findViewById(R.id.uStuffOccasionOrSeason) as Spinner
        val adapterG = ArrayAdapter.createFromResource(
            this, R.array.OccasionOrSeason,
            android.R.layout.simple_spinner_item
        )
        uStuffOccasionOrSeason.adapter = adapterG
        uStuffOccasionOrSeason.setSelection(uOccasionOrSeason)
        uStuffOccasionOrSeason.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffOccasionOrSeason =
                        uStuffOccasionOrSeason.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uOccasionOrSeasonposition=" +
                            position
                    )
                    uOccasionOrSeasonposition = position
                    if (getuStuffOccasionOrSeason == "OccasionOrSeason") {
                        getuStuffOccasionOrSeason = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffMiscellaneous = findViewById(R.id.uStuffMiscellaneous) as Spinner
        val adapterH = ArrayAdapter.createFromResource(
            this, R.array.Miscellaneous,
            android.R.layout.simple_spinner_item
        )
        uStuffMiscellaneous.adapter = adapterH
        uStuffMiscellaneous.setSelection(uMiscellaneous)
        uStuffMiscellaneous.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffMiscellaneous =
                        uStuffMiscellaneous.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprestaurant set uMiscellaneousposition=" +
                            position
                    )
                    uMiscellaneousposition = position
                    if (getuStuffMiscellaneous == "Miscellaneous") {
                        getuStuffMiscellaneous = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

//added
        val viewtype = arrayOf("views")
        val cur = myDatabase!!.query(
            "Preferences", viewtype, null, null,
            null, null, null
        )
        val viewname = cur.getColumnIndexOrThrow("views")
        if (cur != null) {
            if (cur.isFirst) {
                do {
                    viewtypename = cur.getString(viewname)
                } while (cur.moveToNext())
            }
        }
        val ichoose = findViewById(R.id.selectistufflocation) as Button
        ichoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE temprestaurant set stufftype='" + "istuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Restaurants, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "temprestaurant")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Restaurants, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "temprestaurant")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val uchoose = findViewById(R.id.selectustufflocation) as Button
        uchoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE temprestaurant set stufftype='" + "ustuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Restaurants, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "temprestaurant")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Restaurants, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "temprestaurant")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }
//ended
        val save = findViewById(R.id.Save) as Button
        save.setOnClickListener {
            getiStuffCuisinetype =
                iStuffCuisinetype.selectedItem as String?
            getiStuffCookingMethod =
                iStuffCookingMethod.selectedItem as String?
            getiStuffDietetic = iStuffDietetic.selectedItem as String?
            getiStuffCourseType =
                iStuffCourseType.selectedItem as String?
            getiStuffDishType = iStuffDishType.selectedItem as String?
            getiStuffMainIngredient =
                iStuffMainIngredient.selectedItem as String?
            getiStuffOccasionOrSeason =
                iStuffOccasionOrSeason.selectedItem as String?
            getiStuffMiscellaneous =
                iStuffMiscellaneous.selectedItem as String?

            iCuisineTypeposition =
                iStuffCuisinetype.selectedItemPosition
            iCookingMethodposition =
                iStuffCookingMethod.selectedItemPosition
            iDieteticposition = iStuffDietetic.selectedItemPosition
            iCourseTypeposition =
                iStuffCourseType.selectedItemPosition
            iDishTypeposition = iStuffDishType.selectedItemPosition
            iMainIngredientposition =
                iStuffMainIngredient.selectedItemPosition
            iOccasionOrSeasonposition =
                iStuffOccasionOrSeason.selectedItemPosition
            iMiscellaneousposition =
                iStuffMiscellaneous.selectedItemPosition

            val idetails = "Cuisinetype=" + getiStuffCuisinetype +
                " CookingMethod=" + getiStuffCookingMethod +
                " Dietetic=" + getiStuffDietetic + " CourseType=" +
                getiStuffCourseType + " DishType=" + getiStuffDishType +
                " MainIngredient=" + getiStuffMainIngredient +
                " OccasionOrSeason=" + getiStuffOccasionOrSeason +
                " Miscellaneous=" + getiStuffMiscellaneous + " Area=" +
                getiarea + " City=" + geticity + " country=" +
                geticountry

            var getilatitudes = java.lang.Double.parseDouble(getilatitude)
            getilatitudes = getilatitudes * 1E6
            var getilongitudes = java.lang.Double.parseDouble(getilongitude)
            getilongitudes = getilongitudes * 1E6

            if (fromeditquery != null && getkey != 0) {
                myDatabase!!.execSQL(
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
                        "' where key=" + getkey + ";"
                )
                myDatabase!!.execSQL(
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
                        getkey + ";"
                )
                myDatabase!!.execSQL(
                    "update category set querystatus='" +
                        "true" + "' where status='" + "true" + "';"
                )
                myDatabase!!.execSQL(
                    "Update mStuffdetails set details='" +
                        idetails + "', latitude='" + getilatitudes +
                        "', longitude='" + getilongitudes +
                        "', location='" + geticountry +
                        "' where catagory='" + "userRestaurant" + "';"
                )
            } else {
                try {
                    myDatabase!!.execSQL(
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
                            "');"
                    )
                    myDatabase!!.execSQL(
                        "update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';"
                    )
                    myDatabase!!.execSQL(
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
                            "istuff" + "');"
                    )
                    myDatabase!!.execSQL(
                        "Update mStuffdetails set details='" +
                            idetails + "', latitude='" +
                            getilatitudes + "', longitude='" +
                            getilongitudes + "', location='" +
                            geticountry + "' where catagory='" +
                            "userRestaurant" + "';"
                    )
                } catch (e: Exception) {
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
            myDatabase!!.execSQL("drop table " + "temprestaurant" + ";")
            myDatabase?.close()
            val intent =
                Intent(this@Restaurants, FindandInstall::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsmStuffMenu(menu)
        return true
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val intent1 =
                    Intent(this@Restaurants, MapResults::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }

            2 -> {
                val intent2 =
                    Intent(this@Restaurants, FindandInstall::class.java)
                startActivityForResult(intent2, 0)
                finish()
            }

            3 -> {
                val intent3 = Intent(this@Restaurants, Settings::class.java)
                startActivityForResult(intent3, 0)
                finish()
            }

            4 -> {
                val intent =
                    Intent(this@Restaurants, FindandInstall::class.java)
                startActivityForResult(intent, 0)
                finish()
            }

            5 -> {
                try {
                    val c = myDatabase!!.query(
                        "Restaurants", null, null, null,
                        null, null, null
                    )
                    val listmStuff = Intent(
                        this@Restaurants,
                        RestaurantsListQuery::class.java
                    )
                    startActivityForResult(listmStuff, 0)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Restaurants, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            6 -> {
                try {
                    val c = myDatabase!!.query(
                        "Restaurants", null, null, null,
                        null, null, null
                    )
                    val deleteQuery1 = Intent(
                        this@Restaurants,
                        DeleteRestaurants::class.java
                    )
                    startActivityForResult(deleteQuery1, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Restaurants, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            7 -> {
                try {
                    val c = myDatabase!!.query(
                        "Restaurants", null, null, null,
                        null, null, null
                    )
                    val viewQuery = Intent(
                        this@Restaurants,
                        RestaurantsViewQuery::class.java
                    )
                    startActivityForResult(viewQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Restaurants, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            8 -> {
                val b1 = Bundle()
                val settheme =
                    Intent(this@Restaurants, SettingTheme::class.java)
                b1.putString("value1", "Restaurants")
                b1.putString("class", "5")
                settheme.putExtras(b1)
                startActivityForResult(settheme, 0)
                finish()
                // fall through from Java case 8
                val B1 = Bundle()
                B1.putString("requestcatalog", "Restaurants")
                val mediaintent =
                    Intent(this@Restaurants, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }

            9 -> {
                val B1 = Bundle()
                B1.putString("requestcatalog", "Restaurants")
                val mediaintent =
                    Intent(this@Restaurants, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
