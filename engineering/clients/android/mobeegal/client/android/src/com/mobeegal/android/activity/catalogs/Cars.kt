package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Cars.java 14 2008-08-19 06:36:45Z muthu.ramadoss                       $: Id of last commit
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

class Cars : Activity() {

    var getiuStuffcartype: String? = null
    var getiStuffcartype: String? = null
    var myDatabase: SQLiteDatabase? = null
    var key: Int = 1
    var adapter: ArrayAdapter<CharSequence>? = null
    var getiStuffMakeType: String? = null
    var getiStuffModelType: String? = null
    var getiStuffYear: String? = null
    var getiStuffColor: String? = null
    var getiStuffFuelType: String? = null
    var getiStuffPrice: String? = null
    var getuStuffMakeType: String? = null
    var getuStuffModelType: String? = null
    var getuStuffYear: String? = null
    var getuStuffColor: String? = null
    var getuStuffFuelType: String? = null
    var getuStuffPrice: String? = null
    var getiStuffMake: String? = null
    var getiStuffModel: String? = null
    var getuStuffMake: String? = null
    var getuStuffModel: String? = null
    var c: Cursor? = null
    var getimakeposition: Int = 0
    var getimodelposition: Int = 0
    var getiyearposition: Int = 0
    var geticolorposition: Int = 0
    var getifuel_typeposition: Int = 0
    var geticity: String? = null
    var getiarea: String? = null
    var geticountry: String? = null
    var getipriceposition: Int = 0
    var getilatitude: String? = null
    var getilongitude: String? = null
    var getumakeposition: Int = 0
    var getumodelposition: Int = 0
    var getucolorposition: Int = 0
    var getufuel_typeposition: Int = 0
    var getucity: String? = null
    var getuarea: String? = null
    var getucountry: String? = null
    var getuyearposition: Int = 0
    var getupriceposition: Int = 0
    var getulatitude: String? = null
    var getulongitude: String? = null
    var getcategory: String? = null
    var getstufftype: String? = null
    var iStuffMake: Spinner? = null
    var iStuffModel: Spinner? = null
    var iStuffyear: Spinner? = null
    var iStuffColor: Spinner? = null
    var iStuffFuelType: Spinner? = null
    var uStuffColor: Spinner? = null
    var uStuffModel: Spinner? = null
    var iStuffCity: Spinner? = null
    var uStuffCity: Spinner? = null
    var iStuffPrice: Spinner? = null
    var uStuffyear: Spinner? = null
    var uStuffMake: Spinner? = null
    var uStuffFuelType: Spinner? = null
    var uStuffPrice: Spinner? = null
    var imakeposition: Int = 0
    var imodelposition: Int = 0
    var iyearposition: Int = 0
    var icolorposition: Int = 0
    var ifuel_typeposition: Int = 0
    var ipriceposition: Int = 0
    var umakeposition: Int = 0
    var umodelposition: Int = 0
    var uyearposition: Int = 0
    var ucolorposition: Int = 0
    var ufuel_typeposition: Int = 0
    var upriceposition: Int = 0
    var iarea: TextView? = null
    var icity: TextView? = null
    var icountry: TextView? = null
    var uarea: TextView? = null
    var ucity: TextView? = null
    var ucountry: TextView? = null
    var fromeditquery: Bundle? = null
    private var getkey: Int = 0
    var theme: Int = 0
    private val carModel = intArrayOf(
        R.array.Audi, R.array.Bentley, R.array.BMW,
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
        R.array.Volkswagen, R.array.Volvo
    )
    private var viewtypename: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempcarscursor = myDatabase!!.query(
                "tempcars", null, null,
                null, null, null, null
            )
        } catch (e: Exception) {
            myDatabase!!
                .execSQL(
                    "CREATE TABLE IF NOT EXISTS tempcars" +
                        " (imakeposition NUMERIC, imodelposition NUMERIC, iyearposition NUMERIC, icolorposition NUMERIC, ifuelposition NUMERIC, ipriceposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR, umakeposition NUMERIC, umodelposition NUMERIC, uyearposition NUMERIC, ucolorposition NUMERIC, ufuelposition NUMERIC, upriceposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
                )
            myDatabase!!
                .execSQL(
                    "INSERT INTO tempcars (imakeposition, imodelposition,iyearposition,icolorposition, ifuelposition,ipriceposition, iarea, icity, icountry, ilatitude,ilongitude, umakeposition, umodelposition,uyearposition,ucolorposition, ufuelposition,upriceposition, uarea, ucity, ucountry,ulatitude,ulongitude, category, stufftype) VALUES (" +
                        imakeposition +
                        "," +
                        imodelposition +
                        "," +
                        iyearposition +
                        "," +
                        icolorposition +
                        "," +
                        ifuel_typeposition +
                        "," +
                        ipriceposition +
                        ",'" +
                        "" +
                        "','" +
                        "" +
                        "','" +
                        "" +
                        "','" +
                        "" +
                        "','" +
                        "" +
                        "'," +
                        umakeposition +
                        "," +
                        umodelposition +
                        "," +
                        uyearposition +
                        "," +
                        ucolorposition +
                        "," +
                        ufuel_typeposition +
                        "," +
                        upriceposition +
                        ",'" +
                        "" +
                        "','" +
                        "" +
                        "','" +
                        "" +
                        "','" +
                        "" +
                        "','" +
                        "" + "','" + "Cars" + "', '" + "istuff" +
                        "');"
                )
        } finally {
            if (myDatabase != null) {
                myDatabase!!.close()
            }
        }
        fromeditquery = this.intent.extras
        if (fromeditquery != null) {
            getkey = fromeditquery!!.getInt("key")
        }

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val themecursor = myDatabase!!.query(
                "Theme", null,
                "catalog='Cars'", null, null, null, null
            )
            if (themecursor != null) {
                if (themecursor.isFirst) {
                    do {
                        theme = themecursor.getInt(
                            themecursor
                                .getColumnIndexOrThrow("theme")
                        )
                        // String catalog =
                        // themecursor.getString(themecursor.getColumnIndexOrThrow("theme"));
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
            this.setTheme(android.R.style.Theme_Dialog)
        }
        if (theme == 3) {
            this.setTheme(android.R.style.Theme_Dialog)
        }
        setContentView(R.layout.cars)
        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.iStuffprofile)
        one.setIndicator("Seller")
        tabs.addTab(one)

        val two = tabs.newTabSpec("two")
        two.setContent(R.id.uStuffprofile)
        two.setIndicator("Buyer")
        tabs.addTab(two)

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempcarscursor = myDatabase!!.query(
                "tempcars", null, null,
                null, null, null, null
            )

            if (tempcarscursor != null) {
                if (tempcarscursor.isFirst) {
                    do {
                        getimakeposition = tempcarscursor.getInt(
                            tempcarscursor
                                .getColumnIndexOrThrow("imakeposition")
                        )
                        getimodelposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "imodelposition"
                                    )
                            )
                        getiyearposition = tempcarscursor.getInt(
                            tempcarscursor
                                .getColumnIndexOrThrow("iyearposition")
                        )
                        geticolorposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "icolorposition"
                                    )
                            )
                        getifuel_typeposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "ifuelposition"
                                    )
                            )
                        getipriceposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "ipriceposition"
                                    )
                            )
                        geticity = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("icity")
                        )
                        getiarea = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("iarea")
                        )
                        geticountry = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("icountry")
                        )
                        getilatitude = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("ilatitude")
                        )
                        getilongitude = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("ilongitude")
                        )

                        getumakeposition = tempcarscursor.getInt(
                            tempcarscursor
                                .getColumnIndexOrThrow("umakeposition")
                        )
                        getumodelposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "umodelposition"
                                    )
                            )
                        getuyearposition = tempcarscursor.getInt(
                            tempcarscursor
                                .getColumnIndexOrThrow("uyearposition")
                        )
                        getucolorposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "ucolorposition"
                                    )
                            )
                        getufuel_typeposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "ufuelposition"
                                    )
                            )
                        getupriceposition = tempcarscursor
                            .getInt(
                                tempcarscursor
                                    .getColumnIndexOrThrow(
                                        "upriceposition"
                                    )
                            )
                        getucity = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("ucity")
                        )
                        getuarea = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("uarea")
                        )
                        getucountry = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("ucountry")
                        )
                        getulatitude = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("ulatitude")
                        )
                        getulongitude = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("ulongitude")
                        )
                        getcategory = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("category")
                        )
                        getstufftype = tempcarscursor.getString(
                            tempcarscursor
                                .getColumnIndexOrThrow("stufftype")
                        )
                    } while (tempcarscursor.moveToNext())
                }
            }
        } catch (e: Exception) {
        }
        if (getstufftype == "istuff") {
            tabs.currentTab = 0
        } else if (getstufftype == "ustuff") {
            tabs.currentTab = 1
        }
        // Creating a tab1 for ISTUFF

        iStuffMake = findViewById(R.id.iStuffMake) as Spinner
        val adapter1 = ArrayAdapter.createFromResource(
            this,
            R.array.MakeType, android.R.layout.simple_spinner_item
        )
        iStuffMake!!.adapter = adapter1
        iStuffMake!!.setSelection(getimakeposition)
        iStuffMake!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffMakeType = iStuffMake!!
                        .selectedItem as String
                    imakeposition =
                        iStuffMake!!.selectedItemPosition
                    myDatabase!!.execSQL(
                        "UPDATE tempcars set imakeposition=" +
                            position
                    )
                    val iStuffModel =
                        findViewById(R.id.iStuffModel) as Spinner
                    val modelAdapter = ArrayAdapter
                        .createFromResource(
                            this@Cars,
                            carModel[position],
                            android.R.layout.simple_spinner_item
                        )
                    iStuffModel.adapter = modelAdapter
                    iStuffModel.setSelection(getimodelposition)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffModel = findViewById(R.id.iStuffModel) as Spinner
        val adapter2 = ArrayAdapter.createFromResource(
            this,
            R.array.Audi, android.R.layout.simple_spinner_item
        )
        iStuffModel!!.adapter = adapter2
        iStuffModel!!.setSelection(getimodelposition)
        iStuffModel!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffModelType = iStuffModel!!
                        .selectedItem as String
                    imodelposition =
                        iStuffModel!!.selectedItemPosition
                    myDatabase!!
                        .execSQL(
                            "UPDATE tempcars set imodelposition=" +
                                position
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffyear = findViewById(R.id.iStuffYear) as Spinner
        val yearAdapter = ArrayAdapter
            .createFromResource(
                this, R.array.Year,
                android.R.layout.simple_spinner_item
            )
        iStuffyear!!.adapter = yearAdapter
        iStuffyear!!.setSelection(getiyearposition)
        iStuffyear!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffYear =
                        iStuffyear!!.selectedItem as String
                    iyearposition =
                        iStuffyear!!.selectedItemPosition
                    myDatabase!!.execSQL(
                        "UPDATE tempcars set iyearposition=" +
                            position
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffColor = findViewById(R.id.iStuffColor) as Spinner
        val colorAdapter = ArrayAdapter
            .createFromResource(
                this, R.array.Color,
                android.R.layout.simple_spinner_item
            )
        iStuffColor!!.adapter = colorAdapter
        iStuffColor!!.setSelection(geticolorposition)
        iStuffColor!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffColor =
                        iStuffColor!!.selectedItem as String
                    icolorposition =
                        iStuffColor!!.selectedItemPosition
                    myDatabase!!
                        .execSQL(
                            "UPDATE tempcars set icolorposition=" +
                                position
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        iStuffFuelType = findViewById(R.id.iStuffFuelType) as Spinner
        val fuel_TypeAdapter = ArrayAdapter
            .createFromResource(
                this, R.array.FuelType,
                android.R.layout.simple_spinner_item
            )
        iStuffFuelType!!.adapter = fuel_TypeAdapter
        iStuffFuelType!!.setSelection(getifuel_typeposition)
        iStuffFuelType!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffFuelType = iStuffFuelType!!
                        .selectedItem as String
                    ifuel_typeposition = iStuffFuelType!!
                        .selectedItemPosition
                    myDatabase!!.execSQL(
                        "UPDATE tempcars set ifuelposition=" +
                            position
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        iStuffPrice = findViewById(R.id.iStuffPrice) as Spinner
        val priceAdapter = ArrayAdapter
            .createFromResource(
                this, R.array.Price,
                android.R.layout.simple_spinner_item
            )
        iStuffPrice!!.adapter = priceAdapter
        iStuffPrice!!.setSelection(getipriceposition)
        iStuffPrice!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffPrice =
                        iStuffPrice!!.selectedItem as String
                    ipriceposition =
                        iStuffPrice!!.selectedItemPosition
                    myDatabase!!
                        .execSQL(
                            "UPDATE tempcars set ipriceposition=" +
                                position
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iarea = findViewById(R.id.iarea) as TextView
        iarea!!.text = getiarea
        icity = findViewById(R.id.icity) as TextView
        icity!!.text = geticity
        icountry = findViewById(R.id.icountry) as TextView
        icountry!!.text = geticountry
        uarea = findViewById(R.id.uarea) as TextView
        uarea!!.text = getuarea
        ucity = findViewById(R.id.ucity) as TextView
        ucity!!.text = getucity
        ucountry = findViewById(R.id.ucountry) as TextView
        ucountry!!.text = getucountry

        // Creating a tab2 for USTUFF
        uStuffMake = findViewById(R.id.uStuffMake) as Spinner
        val adapter10 = ArrayAdapter.createFromResource(
            this,
            R.array.MakeType, android.R.layout.simple_spinner_item
        )
        uStuffMake!!.adapter = adapter10
        uStuffMake!!.setSelection(getumakeposition)
        uStuffMake!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffMakeType = uStuffMake!!
                        .selectedItem as String
                    umakeposition =
                        uStuffMake!!.selectedItemPosition
                    myDatabase!!.execSQL(
                        "UPDATE tempcars set umakeposition=" +
                            position
                    )
                    val uStuffModel =
                        findViewById(R.id.uStuffModel) as Spinner
                    val modelAdapter = ArrayAdapter
                        .createFromResource(
                            this@Cars,
                            carModel[position],
                            android.R.layout.simple_spinner_item
                        )
                    uStuffModel.adapter = modelAdapter
                    uStuffModel.setSelection(getumodelposition)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffModel = findViewById(R.id.uStuffModel) as Spinner
        val adapter20 = ArrayAdapter.createFromResource(
            this,
            R.array.Audi, android.R.layout.simple_spinner_item
        )
        uStuffModel!!.adapter = adapter20
        uStuffModel!!.setSelection(getumodelposition)
        uStuffModel!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    umodelposition =
                        uStuffModel!!.selectedItemPosition
                    getuStuffModelType = uStuffModel!!
                        .selectedItem as String
                    myDatabase!!
                        .execSQL(
                            "UPDATE tempcars set umodelposition=" +
                                position
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        uStuffyear = findViewById(R.id.uStuffYear) as Spinner
        val Adapter = ArrayAdapter.createFromResource(
            this, R.array.Year, android.R.layout.simple_spinner_item
        )
        uStuffyear!!.adapter = Adapter
        uStuffyear!!.setSelection(getuyearposition)
        uStuffyear!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffYear =
                        uStuffyear!!.selectedItem as String
                    uyearposition =
                        uStuffyear!!.selectedItemPosition
                    myDatabase!!.execSQL(
                        "UPDATE tempcars set uyearposition=" +
                            position
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffColor = findViewById(R.id.uStuffColor) as Spinner
        val Adapter19 = ArrayAdapter.createFromResource(
            this, R.array.Color, android.R.layout.simple_spinner_item
        )
        uStuffColor!!.adapter = Adapter19
        uStuffColor!!.setSelection(getucolorposition)
        uStuffColor!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    ucolorposition =
                        uStuffColor!!.selectedItemPosition
                    getuStuffColor =
                        uStuffColor!!.selectedItem as String
                    myDatabase!!
                        .execSQL(
                            "UPDATE tempcars set ucolorposition=" +
                                position
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffFuelType = findViewById(R.id.uStuffFuelType) as Spinner
        val fuel_TypeAdapter1 = ArrayAdapter
            .createFromResource(
                this, R.array.FuelType,
                android.R.layout.simple_spinner_item
            )
        uStuffFuelType!!.adapter = fuel_TypeAdapter1
        uStuffFuelType!!.setSelection(getufuel_typeposition)
        uStuffFuelType!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    ufuel_typeposition = uStuffFuelType!!
                        .selectedItemPosition
                    getuStuffFuelType = uStuffFuelType!!
                        .selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempcars set ufuelposition=" +
                            position
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffPrice = findViewById(R.id.uStuffPrice) as Spinner
        val priceAdapter1 = ArrayAdapter
            .createFromResource(
                this, R.array.Price,
                android.R.layout.simple_spinner_item
            )
        uStuffPrice!!.adapter = priceAdapter1
        uStuffPrice!!.setSelection(getupriceposition)
        uStuffPrice!!
            .onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    upriceposition =
                        uStuffPrice!!.selectedItemPosition
                    getuStuffPrice =
                        uStuffPrice!!.selectedItem as String
                    myDatabase!!
                        .execSQL(
                            "UPDATE tempcars set upriceposition=" +
                                position
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        // added
        val viewtype = arrayOf("views")
        val cur = myDatabase!!.query(
            "Preferences", viewtype, null,
            null, null, null, null
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
                "UPDATE tempcars set stufftype='" +
                    "istuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder = Intent(
                    this@Cars,
                    LocationFinder::class.java
                )
                val b = Bundle()
                b.putString("tablename", "tempcars")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Cars, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempcars")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val uchoose = findViewById(R.id.selectustufflocation) as Button
        uchoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE tempcars set stufftype='" +
                    "ustuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder = Intent(
                    this@Cars,
                    LocationFinder::class.java
                )
                val b = Bundle()
                b.putString("tablename", "tempcars")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Cars, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempcars")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val button = findViewById(R.id.Save) as Button
        button.setOnClickListener {
            getiStuffMakeType = iStuffMake!!.selectedItem as String
            getiStuffModelType = iStuffModel!!.selectedItem as String
            getiStuffYear = iStuffyear!!.selectedItem as String
            getiStuffColor = iStuffColor!!.selectedItem as String
            getiStuffFuelType = iStuffFuelType!!.selectedItem as String
            getiStuffPrice = iStuffPrice!!.selectedItem as String

            imakeposition = iStuffMake!!.selectedItemPosition
            imodelposition = iStuffModel!!.selectedItemPosition
            iyearposition = iStuffyear!!.selectedItemPosition
            icolorposition = iStuffColor!!.selectedItemPosition
            ifuel_typeposition = iStuffFuelType!!.selectedItemPosition
            ipriceposition = iStuffPrice!!.selectedItemPosition

            val idetails = "Cars Icon \n Carmake=" + getiStuffMakeType +
                " model=" + getiStuffModelType + " year=" +
                getiStuffYear + " color=" + getiStuffColor +
                " fuel_type=" + getiStuffFuelType + " price=" +
                getiStuffPrice + " area=" + getiarea + " city=" +
                geticity + " country=" + geticountry

            var getilatitudes = java.lang.Double.parseDouble(getilatitude)
            getilatitudes = getilatitudes * 1E6
            var getilongitudes = java.lang.Double.parseDouble(getilongitude)
            getilongitudes = getilongitudes * 1E6

            if (fromeditquery != null && getkey != 0) {
                myDatabase!!.execSQL(
                    "update Cars set imake='" +
                        getiStuffMakeType + "', imodel='" +
                        getiStuffModelType + "', iyear='" + getiStuffYear +
                        "', icolor='" + getiStuffColor +
                        "', ifuel_type='" + getiStuffFuelType +
                        "', iprice='" + getiStuffPrice + "', iarea='" +
                        getiarea + "', icity='" + geticity +
                        "', icountry='" + geticountry + "', ilatitude='" +
                        getilatitude + "', ilongitude='" + getilongitude +
                        "', umake='" + getuStuffMakeType + "', umodel='" +
                        getuStuffModelType + "', uyear='" + getuStuffYear +
                        "', ucolor='" + getuStuffColor +
                        "', ufuel_type='" + getuStuffFuelType +
                        "', iprice='" + getuStuffPrice + "', uarea='" +
                        getuarea + "', ucity='" + getucity +
                        "', ucountry='" + getucountry + "', ulatitude='" +
                        getulatitude + "', ulongitude='" + getulongitude +
                        "',queryDate=DATE('NOW') where key=" + getkey +
                        ";"
                )
                myDatabase!!.execSQL(
                    "update CarsPosition set imakeposition=" +
                        imakeposition + ", imodelposition=" +
                        imodelposition + ", iyearposition=" +
                        iyearposition + ", icolorposition=" +
                        icolorposition + ", ifuelposition=" +
                        ifuel_typeposition + ", ipriceposition=" +
                        ipriceposition + ", iarea='" + getiarea +
                        "', icity='" + geticity + "', icountry='" +
                        geticountry + "', ilatitude='" + getilatitude +
                        "', ilongitude='" + getilongitude +
                        "', umakeposition=" + umakeposition +
                        ", umodelposition=" + umodelposition +
                        ", uyearposition=" + uyearposition +
                        ", ucolorposition=" + ucolorposition +
                        ", ufuelposition=" + ufuel_typeposition +
                        ", upriceposition=" + upriceposition +
                        ", uarea='" + getuarea + "', ucity='" + getucity +
                        "', ucountry='" + getucountry + "', ulatitude='" +
                        getulatitude + "', ulongitude='" + getulongitude +
                        "' where key=" + getkey + ";"
                )
                myDatabase!!
                    .execSQL(
                        "update category set querystatus='" +
                            "true" + "' where categoryname='" +
                            "Cars" + "';"
                    )
                myDatabase!!.execSQL(
                    "Update mStuffdetails set details='" +
                        idetails + "', latitude='" + getilatitudes +
                        "', longitude='" + getilongitudes +
                        "', location='" + geticountry +
                        "' where catagory='" + "userCars" + "';"
                )
            } else {
                try {
                    myDatabase!!
                        .execSQL(
                            "INSERT INTO Cars (imake,imodel,iyear,icolor,ifuel_type,iprice,iarea,icity,icountry,ilatitude,ilongitude,umake,umodel,uyear,ucolor,ufuel_type,uprice,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES('" +
                                getiStuffMakeType +
                                "','" +
                                getiStuffModelType +
                                "','" +
                                getiStuffYear +
                                "','" +
                                getiStuffColor +
                                "','" +
                                getiStuffFuelType +
                                "','" +
                                getiStuffPrice +
                                "','" +
                                getiarea +
                                "','" +
                                geticity +
                                "','" +
                                geticountry +
                                "','" +
                                getilatitude +
                                "','" +
                                getilongitude +
                                "','" +
                                getuStuffMakeType +
                                "','" +
                                getuStuffModelType +
                                "','" +
                                getuStuffYear +
                                "','" +
                                getuStuffColor +
                                "','" +
                                getuStuffFuelType +
                                "','" +
                                getuStuffPrice +
                                "','" +
                                getuarea +
                                "','" +
                                getucity +
                                "','" +
                                getucountry +
                                "','" +
                                getulatitude +
                                "','" +
                                getulongitude +
                                "','" +
                                "true" + "');"
                        )
                    myDatabase!!.execSQL(
                        "update category set querystatus='" +
                            "true" + "' where categoryname='" + "Cars" +
                            "';"
                    )
                    myDatabase!!
                        .execSQL(
                            "INSERT INTO CarsPosition(imakeposition, imodelposition,iyearposition,icolorposition, ifuelposition,ipriceposition, iarea, icity, icountry, ilatitude,ilongitude, umakeposition, umodelposition,uyearposition,ucolorposition, ufuelposition,upriceposition, uarea, ucity, ucountry,ulatitude,ulongitude, category, stufftype) VALUES (" +
                                imakeposition +
                                "," +
                                imodelposition +
                                "," +
                                iyearposition +
                                "," +
                                icolorposition +
                                "," +
                                ifuel_typeposition +
                                "," +
                                ipriceposition +
                                ",'" +
                                getiarea +
                                "','" +
                                geticity +
                                "','" +
                                geticountry +
                                "','" +
                                getilatitude +
                                "','" +
                                getilongitude +
                                "'," +
                                umakeposition +
                                "," +
                                umodelposition +
                                "," +
                                uyearposition +
                                "," +
                                ucolorposition +
                                "," +
                                ufuel_typeposition +
                                "," +
                                upriceposition +
                                ",'" +
                                getuarea +
                                "','" +
                                getucity +
                                "','" +
                                getucountry +
                                "','" +
                                getulatitude +
                                "','" +
                                getulongitude +
                                "','" +
                                getcategory +
                                "', '" +
                                getstufftype +
                                "');"
                        )
                    myDatabase!!.execSQL(
                        "Update mStuffdetails set details='" +
                            idetails + "', latitude='" + getilatitudes +
                            "', longitude='" + getilongitudes +
                            "', location='" + geticountry +
                            "' where catagory='" + "userCars" + "';"
                    )
                } catch (e: Exception) {
                    myDatabase!!
                        .execSQL(
                            "CREATE TABLE IF NOT EXISTS Cars" +
                                " (key INTEGER PRIMARY KEY,imake VARCHAR,imodel VARCHAR,iyear VARCHAR,icolor VARCHAR,ifuel_type VARCHAR,iprice VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,umake VARCHAR,umodel VARCHAR,uyear VARCHAR,ucolor VARCHAR,ufuel_type VARCHAR,uprice VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);"
                        )
                    myDatabase!!
                        .execSQL(
                            "CREATE TRIGGER insert_querydate_Cars after INSERT on Cars BEGIN update Cars set queryDate=DATE('NOW') WHERE key=new.key; END;"
                        )
                    myDatabase!!
                        .execSQL(
                            "CREATE TRIGGER delete_querydate_Cars before insert on Cars BEGIN delete from Cars where queryDate<DATE('NOW','-7 day');END;"
                        )
                    myDatabase!!
                        .execSQL(
                            "INSERT INTO Cars (imake,imodel,iyear,icolor,ifuel_type,iprice,iarea,icity,icountry,ilatitude,ilongitude,umake,umodel,uyear,ucolor,ufuel_type,uprice,uarea,ucity,ucountry,ulatitude,ulongitude,queryStatus) VALUES('" +
                                getiStuffMakeType +
                                "','" +
                                getiStuffModelType +
                                "','" +
                                getiStuffYear +
                                "','" +
                                getiStuffColor +
                                "','" +
                                getiStuffFuelType +
                                "','" +
                                getiStuffPrice +
                                "','" +
                                getiarea +
                                "','" +
                                geticity +
                                "','" +
                                geticountry +
                                "','" +
                                getilatitude +
                                "','" +
                                getilongitude +
                                "','" +
                                getuStuffMakeType +
                                "','" +
                                getuStuffModelType +
                                "','" +
                                getuStuffYear +
                                "','" +
                                getuStuffColor +
                                "','" +
                                getuStuffFuelType +
                                "','" +
                                getuStuffPrice +
                                "','" +
                                getuarea +
                                "','" +
                                getucity +
                                "','" +
                                getucountry +
                                "','" +
                                getulatitude +
                                "','" +
                                getulongitude +
                                "','" +
                                "true" + "');"
                        )
                    myDatabase!!
                        .execSQL(
                            "CREATE TABLE IF NOT EXISTS CarsPosition" +
                                " (key INTEGER PRIMARY KEY, imakeposition NUMERIC ,imodelposition NUMERIC ,iyearposition NUMERIC,icolorposition NUMERIC , ifuelposition NUMERIC , ipriceposition NUMERIC,iarea VARCHAR, icity VARCHAR, icountry VARCHAR , ilatitude VARCHAR, ilongitude VARCHAR, umakeposition NUMERIC ,umodelposition NUMERIC ,uyearposition NUMERIC , ucolorposition NUMERIC , ufuelposition NUMERIC , upriceposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR , ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
                        )
                    myDatabase!!
                        .execSQL(
                            "INSERT INTO CarsPosition(imakeposition, imodelposition,iyearposition,icolorposition, ifuelposition,ipriceposition, iarea, icity, icountry, ilatitude,ilongitude, umakeposition, umodelposition,uyearposition,ucolorposition, ufuelposition,upriceposition, uarea, ucity, ucountry,ulatitude,ulongitude, category, stufftype) VALUES (" +
                                imakeposition +
                                "," +
                                imodelposition +
                                "," +
                                iyearposition +
                                "," +
                                icolorposition +
                                "," +
                                ifuel_typeposition +
                                "," +
                                ipriceposition +
                                ",'" +
                                getiarea +
                                "','" +
                                geticity +
                                "','" +
                                geticountry +
                                "','" +
                                getilatitude +
                                "','" +
                                getilongitude +
                                "'," +
                                umakeposition +
                                "," +
                                umodelposition +
                                "," +
                                uyearposition +
                                "," +
                                ucolorposition +
                                "," +
                                ufuel_typeposition +
                                "," +
                                upriceposition +
                                ",'" +
                                getuarea +
                                "','" +
                                getucity +
                                "','" +
                                getucountry +
                                "','" +
                                getulatitude +
                                "','" +
                                getulongitude +
                                "','" +
                                getcategory +
                                "', '" +
                                getstufftype +
                                "');"
                        )
                    myDatabase!!.execSQL(
                        "update category set querystatus='" +
                            "true" + "' where categoryname='" + "Cars" +
                            "';"
                    )
                    myDatabase!!.execSQL(
                        "Update mStuffdetails set details='" +
                            idetails + "', latitude='" + getilatitudes +
                            "', longitude='" + getilongitudes +
                            "', location='" + geticountry +
                            "' where catagory='" + "userCars" + "';"
                    )
                }
            }
            myDatabase!!.execSQL("drop table " + "tempcars" + ";")
            val intent = Intent(this@Cars, FindandInstall::class.java)
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
                val intent1 = Intent(this@Cars, MapResults::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }

            2 -> {
                val intent2 = Intent(this@Cars, FindandInstall::class.java)
                startActivityForResult(intent2, 0)
                finish()
            }

            3 -> {
                val intent3 = Intent(this@Cars, Settings::class.java)
                startActivityForResult(intent3, 0)
                finish()
            }

            4 -> {
                val intent = Intent(this@Cars, FindandInstall::class.java)
                startActivityForResult(intent, 0)
                finish()
            }

            5 -> {
                try {
                    val c = myDatabase!!.query(
                        "Cars", null, null, null, null,
                        null, null
                    )
                    val listmStuff =
                        Intent(this@Cars, Carslistquery::class.java)
                    startActivityForResult(listmStuff, 0)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Cars, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            6 -> {
                try {
                    val c = myDatabase!!.query(
                        "Cars", null, null, null, null,
                        null, null
                    )
                    val deletemStuff = Intent(
                        this@Cars,
                        Carsdeletequery::class.java
                    )
                    startActivityForResult(deletemStuff, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Cars, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            7 -> {
                try {
                    val c = myDatabase!!.query(
                        "Cars", null, null, null, null,
                        null, null
                    )
                    val viewQuery =
                        Intent(this@Cars, CarsViewQuery::class.java)
                    startActivityForResult(viewQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Cars, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            8 -> {
                val b1 = Bundle()
                val settheme = Intent(this@Cars, SettingTheme::class.java)
                b1.putString("value1", "Cars")
                b1.putString("class", "2")
                settheme.putExtras(b1)
                startActivityForResult(settheme, 0)
                finish()
                // fall through from Java case 8
                val B1 = Bundle()
                B1.putString("requestcatalog", "Cars")
                val mediaintent =
                    Intent(this@Cars, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }

            9 -> {
                val B1 = Bundle()
                B1.putString("requestcatalog", "Cars")
                val mediaintent =
                    Intent(this@Cars, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
