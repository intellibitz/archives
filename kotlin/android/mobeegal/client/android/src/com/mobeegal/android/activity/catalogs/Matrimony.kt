package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Matrimony.java 14 2008-08-19 06:36:45Z muthu.ramadoss                  $: Id of last commit
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
import android.widget.CompoundButton
import android.widget.RadioButton
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

class Matrimony : Activity() {

    var getiStuffAge: String? = null
    var getiStuffSex: String? = null
    var getiStuffHeight: String? = null
    var getiStuffWeight: String? = null
    var getiStuffCountry: String? = null
    var getiStuffCity: String? = null
    var getiStuffArea: String? = null
    var getiStuffColor: String? = null
    var getiStufflatitude: String? = null
    var getiStufflongitude: String? = null
    var getuStuffAge: String? = null
    var getuStuffSex: String? = null
    var getuStuffHeight: String? = null
    var getuStuffWeight: String? = null
    var getuStuffColor: String? = null
    var getuStuffCountry: String? = null
    var getuStuffCity: String? = null
    var getuStuffArea: String? = null
    var getiStuffReligion: String? = null
    var getiStuffCaste: String? = null
    var getuStuffReligion: String? = null
    var getuStuffCaste: String? = null
    var getuStufflatitude: String? = null
    var getuStufflongitude: String? = null
    var key: Int = 1
    //int count, j;
    var ireligionposition: Int = 0
    var icasteposition: Int = 0
    var iageposition: Int = 0
    var iheightposition: Int = 0
    var iweightposition: Int = 0
    var icolorposition: Int = 0
    var ureligionposition: Int = 0
    var ucasteposition: Int = 0
    var uageposition: Int = 0
    var uheightposition: Int = 0
    var uweightposition: Int = 0
    var ucolorposition: Int = 0
    var myDatabase: SQLiteDatabase? = null
    var getireligionposition: Int = 0
    var geticasteposition: Int = 0
    var geticolorposition: Int = 0
    var getureligionposition: Int = 0
    var getucasteposition: Int = 0
    var getucolorposition: Int = 0
    var getiageposition: Int = 0
    var getiheightposition: Int = 0
    var getiweightposition: Int = 0
    var getuageposition: Int = 0
    var getuheightposition: Int = 0
    var getuweightposition: Int = 0
    var getisex: String? = null
    var getusex: String? = null
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
    var iarea: TextView? = null
    var icity: TextView? = null
    var icountry: TextView? = null
    var uarea: TextView? = null
    var ucity: TextView? = null
    var ucountry: TextView? = null
    var value1: String? = null
    var v2: String? = null
    var iStuffReligion: Spinner? = null
    var iStuffCaste: Spinner? = null
    var iStuffAge: Spinner? = null
    var iStuffHeight: Spinner? = null
    var iStuffWeight: Spinner? = null
    var iStuffColor: Spinner? = null
    var fromeditquery: Bundle? = null
    var getkey: Int = 0
    var theme: Int = 0
    var catalog: String? = null
    var idetails: String? = null
    var getilatitudes: Double = 0.0
    var getilongitudes: Double = 0.0
    private val religion = intArrayOf(
        R.array.istuffhindu, R.array.istuffmuslim,
        R.array.iStuffChrstian, R.array.istuffsikh
    )
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
                "catalog='Matrimony'", null, null, null, null
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
        setContentView(R.layout.matrimony)

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempcursor = myDatabase!!
                .query("tempmatrimony", null, null, null, null, null, null)
        } catch (e: Exception) {
            //Toast.makeText(Dating.this, "Database not found", Toast.LENGTH_SHORT).show();
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS tempmatrimony" +
                    " (ireligionposition NUMERIC, icasteposition NUMERIC, iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, icolorposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ureligionposition NUMERIC, ucasteposition NUMERIC, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, ucolorposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
            )
            myDatabase!!.execSQL(
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
                    "Matrimony" + "', '" + "istuff" + "');"
            )
        }
        /*finally {
        if (myDatabase != null) {
        myDatabase.close();
        }
        }*/
        fromeditquery = this.intent.extras
        if (fromeditquery != null) {
            getkey = fromeditquery!!.getInt("key")
        }
        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.iStuffprofile)
        one.setIndicator("Your Profile")
        tabs.addTab(one)

        val two = tabs.newTabSpec("two")
        two.setContent(R.id.uStuffprofile)
        two.setIndicator("Partner profile")
        tabs.addTab(two)

        try {
            // myDatabase = this.openDatabase("Mobeegal", null);
            val tempdatingcursor = myDatabase!!
                .query("tempmatrimony", null, null, null, null, null, null)

            if (tempdatingcursor != null) {
                if (tempdatingcursor.isFirst) {
                    do {
                        getireligionposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ireligionposition"
                                )
                            )
                        geticasteposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "icasteposition"
                                )
                            )
                        geticolorposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "icolorposition"
                                )
                            )
                        getiageposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "iageposition"
                                )
                            )
                        getiheightposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "iheightposition"
                                )
                            )
                        getiweightposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "iweightposition"
                                )
                            )
                        getisex = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow("isex")
                        )
                        getureligionposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ureligionposition"
                                )
                            )
                        getucasteposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ucasteposition"
                                )
                            )
                        getucolorposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "ucolorposition"
                                )
                            )
                        getuageposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "uageposition"
                                )
                            )
                        getuheightposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "uheightposition"
                                )
                            )
                        getuweightposition = tempdatingcursor
                            .getInt(
                                tempdatingcursor.getColumnIndexOrThrow(
                                    "uweightposition"
                                )
                            )
                        getusex = tempdatingcursor.getString(
                            tempdatingcursor.getColumnIndexOrThrow("usex")
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
        iStuffReligion = findViewById(R.id.iStuffreligion) as Spinner
        val adapter1 = ArrayAdapter.createFromResource(
            this, R.array.istuffreligion,
            android.R.layout.simple_spinner_item
        )
        iStuffReligion!!.adapter = adapter1
        iStuffReligion!!.setSelection(getireligionposition)
        iStuffReligion!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffReligion =
                        iStuffReligion!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set ireligionposition=" +
                            position
                    )
                    ireligionposition = position
                    //added line
                    val istuffcaste =
                        findViewById(R.id.iStuffcaste) as Spinner
                    val casteadapter = ArrayAdapter
                        .createFromResource(
                            this@Matrimony,
                            religion[position],
                            android.R.layout.simple_spinner_item
                        )
                    istuffcaste.adapter = casteadapter
                    istuffcaste.setSelection(geticasteposition)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffCaste = findViewById(R.id.iStuffcaste) as Spinner
        val adapter2 = ArrayAdapter.createFromResource(
            this, R.array.istuffhindu,
            android.R.layout.simple_spinner_item
        )
        iStuffCaste!!.adapter = adapter2
        iStuffCaste!!.setSelection(geticasteposition)
        iStuffCaste!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffCaste = iStuffCaste!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set icasteposition=" +
                            position
                    )

                    icasteposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffAge = findViewById(R.id.iStuffage) as Spinner
        val adapter3 = ArrayAdapter.createFromResource(
            this, R.array.iStuffage, android.R.layout.simple_spinner_item
        )
        iStuffAge!!.setSelection(getiageposition)
        iStuffAge!!.adapter = adapter3
        iStuffAge!!.setSelection(getiageposition)
        iStuffAge!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffAge = iStuffAge!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set iageposition=" +
                            position
                    )
                    iageposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffSex =
            findViewById(R.id.iStuffmaleradiobutton) as RadioButton
        val iStuffFemale =
            findViewById(R.id.iStufffemaleradiobutton) as RadioButton
        if (getisex == "Male") {
            iStuffSex.isChecked = true
        } else {
            iStuffSex.isChecked = false
            iStuffFemale.isChecked = true
        }
        iStuffSex.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (iStuffSex.isChecked) {
                getisex = "Male"
            } else {
                getisex = "Female"
            }
            myDatabase!!.execSQL(
                "UPDATE tempmatrimony set isex='" + getisex + "'"
            )
        }

        iStuffHeight = findViewById(R.id.iStuffheight) as Spinner
        val adapter4 = ArrayAdapter.createFromResource(
            this, R.array.iStuffheight,
            android.R.layout.simple_spinner_item
        )
        iStuffHeight!!.adapter = adapter4
        iStuffHeight!!.setSelection(getiheightposition)
        iStuffHeight!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffHeight =
                        iStuffHeight!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set iheightposition=" +
                            position
                    )
                    iheightposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        iStuffWeight = findViewById(R.id.iStuffweight) as Spinner
        val adapter5 = ArrayAdapter.createFromResource(
            this, R.array.iStuffweight,
            android.R.layout.simple_spinner_item
        )
        iStuffWeight!!.adapter = adapter5
        iStuffWeight!!.setSelection(getiweightposition)
        iStuffWeight!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffWeight =
                        iStuffWeight!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set iweightposition=" +
                            position
                    )
                    iweightposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffColor = findViewById(R.id.iStuffcolor) as Spinner
        val adapter6 = ArrayAdapter.createFromResource(
            this, R.array.iStuffcolor,
            android.R.layout.simple_spinner_item
        )
        iStuffColor!!.adapter = adapter6
        iStuffColor!!.setSelection(geticolorposition)
        iStuffColor!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffColor = iStuffColor!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set icolorposition=" +
                            position
                    )
                    icolorposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        if (getstufftype == "istuff") {
            tabs.currentTab = 0
        } else if (getstufftype == "ustuff") {
            tabs.currentTab = 1
        }

        iarea = findViewById(R.id.iarea) as TextView
        iarea!!.text = getiarea
        icity = findViewById(R.id.icity) as TextView
        icity!!.text = geticity
        icountry = findViewById(R.id.icountry) as TextView
        icountry!!.text = geticountry

        val uStuffReligion =
            findViewById(R.id.uStuffReligion) as Spinner
        val adapter10 = ArrayAdapter.createFromResource(
            this, R.array.ustuffreligion,
            android.R.layout.simple_spinner_item
        )
        uStuffReligion.adapter = adapter10
        uStuffReligion.setSelection(getureligionposition)
        uStuffReligion.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffReligion =
                        uStuffReligion.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set ureligionposition=" +
                            position
                    )
                    ureligionposition = position

                    //added line
                    val ustuffcaste =
                        findViewById(R.id.uStuffCaste) as Spinner
                    val casteadapter1 = ArrayAdapter
                        .createFromResource(
                            this@Matrimony,
                            religion[position],
                            android.R.layout.simple_spinner_item
                        )
                    ustuffcaste.adapter = casteadapter1
                    ustuffcaste.setSelection(getucasteposition)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        val uStuffCaste = findViewById(R.id.uStuffCaste) as Spinner
        val adapter11 = ArrayAdapter.createFromResource(
            this, R.array.istuffhindu,
            android.R.layout.simple_spinner_item
        )
        uStuffCaste.adapter = adapter11
        uStuffCaste.setSelection(getucasteposition)
        uStuffCaste.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffCaste = uStuffCaste.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set ucasteposition=" +
                            position
                    )
                    ucasteposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }


        val uStuffAge = findViewById(R.id.uStuffage) as Spinner
        val adapter12 = ArrayAdapter.createFromResource(
            this, R.array.age, android.R.layout.simple_spinner_item
        )
        uStuffAge.adapter = adapter12
        uStuffAge.setSelection(getuageposition)
        uStuffAge.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffAge = uStuffAge.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set uageposition=" +
                            position
                    )
                    uageposition = position
                    if (getuStuffAge == "Select Age") {
                        getuStuffAge = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffSex =
            findViewById(R.id.uStufffemaleradiobutton) as RadioButton
        val uStuffFemale =
            findViewById(R.id.uStuffmaleradiobutton) as RadioButton
        if (getusex == "Female") {
            uStuffSex.isChecked = true
        } else {
            uStuffSex.isChecked = false
            uStuffFemale.isChecked = true
        }

        uStuffSex.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (uStuffSex.isChecked) {
                getusex = "Female"
            } else {
                getusex = "Male"
            }
            myDatabase!!.execSQL(
                "UPDATE tempmatrimony set usex='" + getusex + "'"
            )
        }
        val uStuffHeight = findViewById(R.id.uStuffheight) as Spinner
        var adapter = ArrayAdapter.createFromResource(
            this,
            R.array.height, android.R.layout.simple_spinner_item
        )
        uStuffHeight.adapter = adapter
        uStuffHeight.setSelection(getuheightposition)
        uStuffHeight.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffHeight =
                        uStuffHeight.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set uheightposition=" +
                            position
                    )
                    uheightposition = position
                    if (getuStuffHeight == "Select Height") {
                        getuStuffHeight = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffWeight = findViewById(R.id.uStuffweight) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.weight,
            android.R.layout.simple_spinner_item
        )
        uStuffWeight.adapter = adapter
        uStuffWeight.setSelection(getuweightposition)
        uStuffWeight.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffWeight =
                        uStuffWeight.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set uweightposition=" +
                            position
                    )
                    uweightposition = position
                    if (getuStuffWeight == "Select Weight") {
                        getuStuffWeight = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffColor1 = findViewById(R.id.uStuffcolor) as Spinner
        val adapter13 = ArrayAdapter.createFromResource(
            this, R.array.uStuffcolor,
            android.R.layout.simple_spinner_item
        )
        uStuffColor1.adapter = adapter13
        uStuffColor1.setSelection(getucolorposition)
        uStuffColor1.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffColor =
                        uStuffColor1.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmatrimony set ucolorposition=" +
                            position
                    )
                    ucolorposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        uarea = findViewById(R.id.uarea) as TextView
        uarea!!.text = getuarea
        ucity = findViewById(R.id.ucity) as TextView
        ucity!!.text = getucity
        ucountry = findViewById(R.id.ucountry) as TextView
        ucountry!!.text = getucountry

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
                "UPDATE tempmatrimony set stufftype='" + "istuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Matrimony, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "tempmatrimony")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Matrimony, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempmatrimony")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }


        val uchoose = findViewById(R.id.selectustufflocation) as Button
        uchoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE tempmatrimony set stufftype='" + "ustuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Matrimony, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "tempmatrimony")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Matrimony, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempmatrimony")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val button = findViewById(R.id.Save) as Button
        button.setOnClickListener {
            getiStuffAge = iStuffAge!!.selectedItem as String?
            getiStuffHeight = iStuffHeight!!.selectedItem as String?
            getiStuffWeight = iStuffWeight!!.selectedItem as String?
            getiStuffCaste = iStuffCaste!!.selectedItem as String?
            getiStuffColor = iStuffColor!!.selectedItem as String?
            getiStuffReligion = iStuffReligion!!.selectedItem as String?

            ireligionposition = iStuffReligion!!.selectedItemPosition
            icasteposition = iStuffCaste!!.selectedItemPosition
            icolorposition = iStuffColor!!.selectedItemPosition
            iageposition = iStuffAge!!.selectedItemPosition
            iheightposition = iStuffHeight!!.selectedItemPosition
            iweightposition = iStuffWeight!!.selectedItemPosition

            idetails = "Matrimony Icon \n Age=" + getiStuffAge +
                " Height=" + getiStuffHeight + " Weight=" +
                getiStuffWeight + " Caste=" + getiStuffCaste +
                " Color=" + getiStuffColor + " Religion=" +
                getiStuffReligion + " Area=" + getiarea + " City=" +
                geticity + " country=" + geticountry
            getilatitudes = java.lang.Double.parseDouble(getilatitude)
            getilatitudes = getilatitudes * 1E6
            getilongitudes = java.lang.Double.parseDouble(getilongitude)
            getilongitudes = getilongitudes * 1E6
            if (fromeditquery != null && getkey != 0) {
                myDatabase!!.execSQL(
                    "update Matrimony set ireligion='" +
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
                        ";"
                )
                myDatabase!!.execSQL(
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
                        "' where catagory='" + "userMatrimony" + "';"
                )
            } else {
                try {
                    myDatabase!!.execSQL(
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
                            getulongitude + "','" + "true" + "');"
                    )
                    myDatabase!!.execSQL(
                        "update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';"
                    )
                    myDatabase!!.execSQL(
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
                            "istuff" + "');"
                    )
                    myDatabase!!.execSQL(
                        "Update mStuffdetails set details='" +
                            idetails + "', latitude='" +
                            getilatitudes + "', longitude='" +
                            getilongitudes + "', location='" +
                            geticountry + "' where catagory='" +
                            "userMatrimony" + "';"
                    )
                } catch (exce: Exception) {
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS Matrimony (key INTEGER PRIMARY KEY,ireligion VARCHAR,icaste VARCHAR,iage VARCHAR, isex VARCHAR, iheight VARCHAR, " +
                            "iweight VARCHAR,icolor VARCHAR ,iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR,ureligion VARCHAR,ucaste VARCHAR,uage VARCHAR, usex VARCHAR, uheight VARCHAR," +
                            "uweight VARCHAR,ucolor VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR,queryStatus VARCHAR,queryDate DATE);"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER insert_querydate_Matrimony after INSERT on Matrimony BEGIN update Matrimony set queryDate=DATE('NOW') WHERE key=new.key; END;"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER delete_querydate_Matrimony before insert on Matrimony BEGIN delete from Matrimony where queryDate<DATE('NOW','-7 day');END;"
                    )
                    myDatabase!!.execSQL(
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
                            getulongitude + "','" + "true" + "');"
                    )
                    myDatabase!!.execSQL(
                        "update category set querystatus='" +
                            "true" + "' where status='" + "true" + "';"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS matrimonyposition" +
                            " (key INTEGER PRIMARY KEY,ireligionposition NUMERIC, icasteposition NUMERIC, iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, icolorposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ureligionposition NUMERIC, ucasteposition NUMERIC, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC,ucolorposition NUMERIC,  uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
                    )
                    myDatabase!!.execSQL(
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
                            "istuff" + "');"
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
                            "userMatrimony" + "';"
                    )
                }
            }
            myDatabase!!.execSQL("drop table " + "tempmatrimony" + ";")
            val intent =
                Intent(this@Matrimony, FindandInstall::class.java)
            startActivityForResult(intent, 0)
            finish()
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
                    Intent(this@Matrimony, MapResults::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }

            2 -> {
                val intent2 =
                    Intent(this@Matrimony, FindandInstall::class.java)
                startActivityForResult(intent2, 0)
                finish()
            }

            3 -> {
                val intent3 = Intent(this@Matrimony, Settings::class.java)
                startActivityForResult(intent3, 0)
                finish()
            }
            4 -> {
                val intent =
                    Intent(this@Matrimony, FindandInstall::class.java)
                startActivityForResult(intent, 0)
                finish()
            }
            5 -> {
                try {
                    val c = myDatabase!!.query(
                        "Matrimony", null, null, null,
                        null, null, null
                    )
                    val listmStuff = Intent(
                        this@Matrimony,
                        Matrimonylistquery::class.java
                    )
                    startActivityForResult(listmStuff, 0)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Matrimony, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            6 -> {
                try {
                    val c = myDatabase!!.query(
                        "Matrimony", null, null, null,
                        null, null, null
                    )
                    val matrimonydeleteQuery = Intent(
                        this@Matrimony,
                        Matrimonydeletequery::class.java
                    )
                    startActivity(matrimonydeleteQuery)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Matrimony, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            7 -> {
                try {
                    val c = myDatabase!!.query(
                        "Matrimony", null, null, null,
                        null, null, null
                    )
                    val matrimonyviewQuery = Intent(
                        this@Matrimony,
                        Matrimonyviewquery::class.java
                    )
                    startActivity(matrimonyviewQuery)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Matrimony, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            8, 9 -> {
                if (item.itemId == 8) {
                    val b1 = Bundle()
                    val settheme =
                        Intent(this@Matrimony, SettingTheme::class.java)
                    b1.putString("value1", "Matrimony")
                    b1.putString("class", "4")
                    settheme.putExtras(b1)
                    startActivityForResult(settheme, 0)
                    finish()
                }
                val B1 = Bundle()
                B1.putString("requestcatalog", "Matrimony")
                val mediaintent =
                    Intent(this@Matrimony, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
