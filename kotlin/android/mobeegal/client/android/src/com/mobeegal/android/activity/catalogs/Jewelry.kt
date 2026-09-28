package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Jewelry.java 14 2008-08-19 06:36:45Z muthu.ramadoss                    $: Id of last commit
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

class Jewelry : Activity() {

    var getiStuffitem: String? = null
    var getiStuffgender: String? = null
    var getiStuffstonetype: String? = null
    var getiStuffmetaltype: String? = null
    var getiStuffWeight: String? = null
    var getiStuffCountry: String? = null
    var getuStuffgender: String? = null
    var getuStuffstonetype: String? = null
    var getuStuffmetaltype: String? = null
    var getuStuffitem: String? = null
    var getuStuffCity: String? = null
    var getuStuffCountry: String? = null
    var getuStuffArea: String? = null
    var getuStuffWeight: String? = null
    var ijewelryposition: Int = 0
    var igenderposition: Int = 0
    var istoneposition: Int = 0
    var imetalposition: Int = 0
    var iweightposition: Int = 0
    var ujewelryposition: Int = 0
    var ugenderposition: Int = 0
    var ustoneposition: Int = 0
    var umetalposition: Int = 0
    var uweightposition: Int = 0
    var getijewelryposition: Int = 0
    var getigenderposition: Int = 0
    var getistoneposition: Int = 0
    var getimetalposition: Int = 0
    var getiweightposition: Int = 0
    var getujewelryposition: Int = 0
    var getugenderposition: Int = 0
    var getustoneposition: Int = 0
    var getumetalposition: Int = 0
    var getuweightposition: Int = 0
    var iarea: String? = null
    var icity: String? = null
    var icountry: String? = null
    var uarea: String? = null
    var ucity: String? = null
    var ucountry: String? = null
    var ilatitude: String? = null
    var ilongitude: String? = null
    var ulatitude: String? = null
    var ulongitude: String? = null
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
    var getulatitude: String? = null
    var getulongitude: String? = null
    var iareatextview: TextView? = null
    var icitytextview: TextView? = null
    var icountrytextview: TextView? = null
    var uareatextview: TextView? = null
    var ucitytextview: TextView? = null
    var ucountrytextview: TextView? = null
    var iStuffitem: Spinner? = null
    var iStuffgender: Spinner? = null
    var iStuffmetaltype: Spinner? = null
    var iStuffstone: Spinner? = null
    var iStuffWeight: Spinner? = null
    var uStuffitem: Spinner? = null
    var uStuffgender: Spinner? = null
    var uStuffmetaltype: Spinner? = null
    var uStuffstone: Spinner? = null
    var uStuffweight: Spinner? = null
    var uStuffWeight: Spinner? = null
    var key: Int = 1
    var fromeditquery: Bundle? = null
    var getkey: Int = 0
    var theme: Int = 0
    var catalog: String? = null
    val results = ArrayList<Any>()
    var adapter: ArrayAdapter<CharSequence>? = null
    var myDatabase: SQLiteDatabase? = null
    private var viewtypename: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val themecursor = myDatabase!!.query(
                "Theme", null,
                "catalog='Jewelry'", null, null, null, null
            )
            if (themecursor != null) {
                if (themecursor.isFirst) {
                    do {
                        theme = themecursor
                            .getInt(
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
            this.setTheme(android.R.style.Theme_Dialog)
        }
        if (theme == 3) {
            this.setTheme(android.R.style.Theme_Dialog)
        }

        setContentView(R.layout.jewelry)
        fromeditquery = this.intent.extras
        if (fromeditquery != null) {
            getkey = fromeditquery!!.getInt("key")
        }
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempdatingcursor = myDatabase!!
                .query("tempjewelry", null, null, null, null, null, null)
        } catch (e: Exception) {
            // TODO: handle exception
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS tempjewelry" +
                    "(ijewelryposition NUMERIC, igenderposition NUMERIC, istoneposition NUMERIC, imetalposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ujewelryposition NUMERIC, ugenderposition NUMERIC, ustoneposition NUMERIC, umetalposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
            )
            myDatabase!!.execSQL(
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
                    "istuff" + "');"
            )
        } finally {
            if (myDatabase != null) {
                myDatabase!!.close()
            }
        }

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempdatingcursor = myDatabase!!
                .query("tempjewelry", null, null, null, null, null, null)

            if (tempdatingcursor != null) {
                if (tempdatingcursor.isFirst) {
                    do {
                        getijewelryposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ijewelryposition"
                                )
                            )
                        getigenderposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "igenderposition"
                                )
                            )
                        getistoneposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "istoneposition"
                                )
                            )
                        getimetalposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "imetalposition"
                                )
                            )
                        getiweightposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "iweightposition"
                                )
                            )

                        getujewelryposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ujewelryposition"
                                )
                            )
                        getugenderposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ugenderposition"
                                )
                            )
                        getustoneposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ustoneposition"
                                )
                            )
                        getumetalposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "umetalposition"
                                )
                            )
                        getuweightposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "uweightposition"
                                )
                            )

                        getiarea = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "iarea"
                            )
                        )
                        geticity = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "icity"
                            )
                        )
                        geticountry = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "icountry"
                            )
                        )
                        getuarea = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "uarea"
                            )
                        )
                        getucity = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "ucity"
                            )
                        )
                        getucountry = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "ucountry"
                            )
                        )
                        getilatitude = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "ilatitude"
                            )
                        )
                        getilongitude = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "ilongitude"
                            )
                        )
                        getulatitude = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "ulatitude"
                            )
                        )
                        getulongitude = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "ulongitude"
                            )
                        )
                        getcategory = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "category"
                            )
                        )
                        getstufftype = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow(
                                "stufftype"
                            )
                        )
                    } while (tempdatingcursor.moveToNext())
                }
            }
        } catch (e: Exception) {
        }

        //Creating a tab1 for ISTUFF

        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.iStuffprofile)
        one.setIndicator("Merchant")

        iStuffitem = findViewById(R.id.iStuffitemtype) as Spinner
        val adapter1 = ArrayAdapter.createFromResource(
            this, R.array.jewelrytype,
            android.R.layout.simple_spinner_item
        )
        iStuffitem!!.adapter = adapter1
        iStuffitem!!.setSelection(getijewelryposition)
        iStuffitem!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffitem = iStuffitem!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set ijewelryposition=" +
                            position
                    )
                    ijewelryposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffgender = findViewById(R.id.iStuffgender) as Spinner
        val adaptergen = ArrayAdapter.createFromResource(
            this, R.array.jewelrygender,
            android.R.layout.simple_spinner_item
        )

        iStuffgender!!.adapter = adaptergen
        iStuffgender!!.setSelection(getigenderposition)
        iStuffgender!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffgender =
                        iStuffgender!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set igenderposition=" +
                            position
                    )
                    igenderposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffstone = findViewById(R.id.iStuffstonetype) as Spinner
        val adapterstone =
            ArrayAdapter.createFromResource(
                this, R.array.stonetype,
                android.R.layout.simple_spinner_item
            )
        iStuffstone!!.adapter = adapterstone
        iStuffstone!!.setSelection(getistoneposition)
        iStuffstone!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffstonetype =
                        iStuffstone!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set istoneposition=" +
                            position
                    )
                    istoneposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffmetaltype = findViewById(R.id.iStuffmetaltype) as Spinner
        val adaptermetal =
            ArrayAdapter.createFromResource(
                this, R.array.metaltype,
                android.R.layout.simple_spinner_item
            )

        iStuffmetaltype!!.adapter = adaptermetal
        iStuffmetaltype!!.setSelection(getimetalposition)
        iStuffmetaltype!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffmetaltype =
                        iStuffmetaltype!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set imetalposition=" +
                            position
                    )
                    imetalposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffWeight = findViewById(R.id.iStuffweight) as Spinner
        val adapter2 = ArrayAdapter.createFromResource(
            this, R.array.istuffweightingram,
            android.R.layout.simple_spinner_item
        )
        iStuffWeight!!.adapter = adapter2
        iStuffWeight!!.setSelection(getiweightposition)
        iStuffWeight!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffWeight =
                        iStuffWeight!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set iweightposition=" +
                            position
                    )
                    iweightposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iareatextview = findViewById(R.id.iarea) as TextView
        iareatextview!!.text = getiarea
        icitytextview = findViewById(R.id.icity) as TextView
        icitytextview!!.text = geticity
        icountrytextview = findViewById(R.id.icountry) as TextView
        icountrytextview!!.text = geticountry
        uareatextview = findViewById(R.id.uarea) as TextView
        uareatextview!!.text = getuarea
        ucitytextview = findViewById(R.id.ucity) as TextView
        ucitytextview!!.text = getucity
        ucountrytextview = findViewById(R.id.ucountry) as TextView
        ucountrytextview!!.text = getucountry
        tabs.addTab(one)

        //Creating a tab2 for USTUFF

        val two = tabs.newTabSpec("two")
        two.setContent(R.id.uStuffprofile)
        two.setIndicator("Customer")
        uStuffitem = findViewById(R.id.uStuffitemtype) as Spinner
        val adapter6 = ArrayAdapter.createFromResource(
            this, R.array.jewelrytype,
            android.R.layout.simple_spinner_item
        )
        uStuffitem!!.adapter = adapter6
        uStuffitem!!.setSelection(getujewelryposition)
        uStuffitem!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffitem = uStuffitem!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set ujewelryposition=" +
                            position
                    )
                    ujewelryposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        // =================== attributes added for ustuff ======================//

        uStuffgender = findViewById(R.id.uStuffgender) as Spinner
        val adaptergen1 =
            ArrayAdapter.createFromResource(
                this, R.array.jewelrygender,
                android.R.layout.simple_spinner_item
            )

        uStuffgender!!.adapter = adaptergen1
        uStuffgender!!.setSelection(getugenderposition)
        uStuffgender!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffgender =
                        uStuffgender!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set ugenderposition=" +
                            position
                    )
                    ugenderposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffstone = findViewById(R.id.uStuffstonetype) as Spinner
        val adapterstone1 =
            ArrayAdapter.createFromResource(
                this, R.array.stonetype,
                android.R.layout.simple_spinner_item
            )
        uStuffstone!!.adapter = adapterstone1
        uStuffstone!!.setSelection(getustoneposition)
        uStuffstone!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffstonetype =
                        uStuffstone!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set ustoneposition=" +
                            position
                    )
                    ustoneposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffmetaltype = findViewById(R.id.uStuffmetaltype) as Spinner
        val adaptermetal1 =
            ArrayAdapter.createFromResource(
                this, R.array.metaltype,
                android.R.layout.simple_spinner_item
            )
        uStuffmetaltype!!.adapter = adaptermetal1
        uStuffmetaltype!!.setSelection(getumetalposition)
        uStuffmetaltype!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffmetaltype =
                        uStuffmetaltype!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set umetalposition=" +
                            position
                    )
                    umetalposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffWeight = findViewById(R.id.uStuffweight) as Spinner
        val adapter7 = ArrayAdapter.createFromResource(
            this, R.array.ustuffweightingram,
            android.R.layout.simple_spinner_item
        )
        uStuffWeight!!.adapter = adapter7
        uStuffWeight!!.setSelection(getuweightposition)
        uStuffWeight!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffWeight =
                        uStuffWeight!!.selectedItem as String
                    myDatabase!!.execSQL(
                        "UPDATE tempjewelry set uweightposition=" +
                            position
                    )
                    uweightposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        tabs.addTab(two)
        if (getstufftype == "istuff") {
            tabs.currentTab = 0
        } else {
            tabs.currentTab = 1
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
                "UPDATE tempjewelry set stufftype='" + "istuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Jewelry, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "tempjewelry")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Jewelry, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempjewelry")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val uchoose = findViewById(R.id.selectustufflocation) as Button
        uchoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE tempjewelry set stufftype='" + "ustuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Jewelry, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "tempjewelry")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Jewelry, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempjewelry")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val button = findViewById(R.id.Save) as Button
        button.setOnClickListener {
            var getilongitudes: Double = 0.0
            var getilatitudes: Double = 0.0
            var idetails: String? = null

            getiStuffitem = iStuffitem!!.selectedItem as String
            getiStuffgender = iStuffgender!!.selectedItem as String
            getiStuffstonetype = iStuffstone!!.selectedItem as String
            getiStuffmetaltype = iStuffmetaltype!!.selectedItem as String
            getiStuffWeight = iStuffWeight!!.selectedItem as String

            ijewelryposition = iStuffitem!!.selectedItemPosition
            igenderposition = iStuffgender!!.selectedItemPosition
            istoneposition = iStuffstone!!.selectedItemPosition
            imetalposition = iStuffmetaltype!!.selectedItemPosition
            iweightposition = iStuffWeight!!.selectedItemPosition

            idetails = "jewelryitem=" + getiStuffitem + " gender=" +
                getiStuffgender + " stone=" + getiStuffstonetype +
                " metal=" + getiStuffmetaltype + " weight=" +
                getiStuffWeight + " Area=" + getiarea + " City=" +
                geticity + " country=" + geticountry

            getilatitudes = java.lang.Double.parseDouble(getilatitude)
            getilatitudes = getilatitudes * 1E6
            getilongitudes = java.lang.Double.parseDouble(getilongitude)
            getilongitudes = getilongitudes * 1E6

            if (fromeditquery != null && getkey != 0) {
                myDatabase!!.execSQL(
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
                        ";"
                )
                myDatabase!!.execSQL(
                    "UPDATE Jewelry set ijewelry='" +
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
                        ";"
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
                        "' where catagory='" + "userJewelry" + "';"
                )
            } else {
                try {
                    myDatabase!!.execSQL(
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
                            getulongitude + "','" + "true" + "');"
                    )
                    myDatabase!!.execSQL(
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
                            "','" + getulongitude + "');"
                    )
                    myDatabase!!.execSQL(
                        "update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';"
                    )
                    myDatabase!!.execSQL(
                        "Update mStuffdetails set details='" +
                            idetails + "', latitude='" +
                            getilatitudes + "', longitude='" +
                            getilongitudes + "', location='" +
                            geticountry + "' where catagory='" +
                            "userJewelry" + "';"
                    )
                } catch (e: Exception) {
                    myDatabase!!
                        .execSQL(
                            "CREATE TABLE IF NOT EXISTS Jewelry" +
                                " (key INTEGER PRIMARY KEY,ijewelry VARCHAR,igender VARCHAR,istone VARCHAR,imetal VARCHAR, iweight VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,ujewelry VARCHAR,ugender VARCHAR,ustone VARCHAR,umetal VARCHAR,uweight VARCHAR,uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);"
                        )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER insert_querydate_Jewelry after INSERT on Jewelry BEGIN update Jewelry set queryDate=DATE('NOW') WHERE key=new.key; END;"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER delete_querydate_Jewelry before insert on Jewelry BEGIN delete from Jewelry where queryDate<DATE('NOW','-7 day');END;"
                    )
                    myDatabase!!.execSQL(
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
                            getulongitude + "','" + "true" + "');"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS JewelryPosition" +
                            "(key INTEGER PRIMARY KEY, ijewelryposition NUMERIC, igenderposition NUMERIC, istoneposition NUMERIC, imetalposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ujewelryposition NUMERIC, ugenderposition NUMERIC, ustoneposition NUMERIC, umetalposition NUMERIC, uweightposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR);"
                    )
                    myDatabase!!.execSQL(
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
                            "','" + getulongitude + "');"
                    )
                    myDatabase!!.execSQL(
                        "update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';"
                    )
                    myDatabase!!.execSQL(
                        "Update mStuffdetails set details='" +
                            idetails + "', latitude='" +
                            getilatitudes + "', longitude='" +
                            getilongitudes + "', location='" +
                            geticountry + "' where catagory='" +
                            "userJewelry" + "';"
                    )
                }
            }

            myDatabase!!.execSQL("drop table " + "tempjewelry" + ";")
            val intent = Intent(this@Jewelry, FindandInstall::class.java)
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
                val intent1 = Intent(this@Jewelry, MapResults::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }

            2 -> {
                val intentToFindandinstall =
                    Intent(this@Jewelry, FindandInstall::class.java)
                startActivityForResult(intentToFindandinstall, 0)
                finish()
            }

            3 -> {
                val intent3 = Intent(this@Jewelry, Settings::class.java)
                startActivityForResult(intent3, 0)
                finish()
            }

            4 -> {
                val intent = Intent(this@Jewelry, FindandInstall::class.java)
                startActivityForResult(intent, 0)
                finish()
            }

            5 -> {
                try {
                    val c = myDatabase!!.query(
                        "Jewelry", null, null, null,
                        null, null, null
                    )
                    val listmStuff =
                        Intent(this@Jewelry, Jewelrylistquery::class.java)
                    startActivityForResult(listmStuff, 0)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Jewelry, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            6 -> {
                try {
                    val c = myDatabase!!.query(
                        "Jewelry", null, null, null,
                        null, null, null
                    )
                    val jewelrydeleteQuery =
                        Intent(this@Jewelry, Jewelrydeletequery::class.java)
                    startActivityForResult(jewelrydeleteQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Jewelry, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            7 -> {
                try {
                    val c = myDatabase!!.query(
                        "Jewelry", null, null, null,
                        null, null, null
                    )
                    val jewelryviewQuery =
                        Intent(this@Jewelry, Jewelryviewquery::class.java)
                    startActivityForResult(jewelryviewQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Jewelry, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            8 -> {
                val b1 = Bundle()
                val settheme = Intent(this@Jewelry, SettingTheme::class.java)
                b1.putString("value1", "Jewelry")
                b1.putString("class", "1")
                settheme.putExtras(b1)
                startActivityForResult(settheme, 0)
                finish()
                // fall through from Java case 8
                val B1 = Bundle()
                B1.putString("requestcatalog", "Jewelry")
                val mediaintent =
                    Intent(this@Jewelry, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }

            9 -> {
                val B1 = Bundle()
                B1.putString("requestcatalog", "Jewelry")
                val mediaintent =
                    Intent(this@Jewelry, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
