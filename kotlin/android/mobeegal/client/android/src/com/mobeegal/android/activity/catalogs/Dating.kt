package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Dating.java 14 2008-08-19 06:36:45Z muthu.ramadoss                     $: Id of last commit
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

class Dating : Activity() {

    var key: Int = 1
    var myDatabase: SQLiteDatabase? = null
    var getistuffage: String? = null
    var getiStuffSex: String? = null
    var getiStuffHeight: String? = null
    var getiStuffWeight: String? = null
    var getuStuffAge: String? = null
    var getuStuffSex: String? = null
    var getuStuffHeight: String? = null
    var getuStuffWeight: String? = null
    var idetails: String? = null
    var adapter: ArrayAdapter<*>? = null
    var iStuffAge: Spinner? = null
    var iStuffSex: RadioButton? = null
    var iStuffHeight: Spinner? = null
    var iStuffWeight: Spinner? = null
    var ustuffage: Spinner? = null
    var uStuffSex: RadioButton? = null
    var uStuffHeight: Spinner? = null
    var uStuffWeight: Spinner? = null
    var iStuffFemale: RadioButton? = null
    var uStuffFemale: RadioButton? = null
    var iageposition: Int = 0
    var iheightposition: Int = 0
    var iweightposition: Int = 0
    var uageposition: Int = 0
    var uheightposition: Int = 0
    var uweightposition: Int = 0
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
    var getilatitudes: Double = 0.0
    var getilongitudes: Double = 0.0
    var iarea: TextView? = null
    var icity: TextView? = null
    var icountry: TextView? = null
    var uarea: TextView? = null
    var ucity: TextView? = null
    var ucountry: TextView? = null
    var fromeditquery: Bundle? = null
    var getkey: Int = 0
    var theme: Int = 0
    private var viewtypename: String? = null

    //  String catalog;
    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        fromeditquery = this.intent.extras
        if (fromeditquery != null) {
            getkey = fromeditquery!!.getInt("key")
        }
        // ToDo add your GUI initialization code here
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempdatingcursor = myDatabase!!
                .query("tempdating", null, null, null, null, null, null)
        } catch (e: Exception) {
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS tempdating" +
                    " (iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
            )
            myDatabase!!.execSQL(
                "INSERT INTO tempdating (iageposition, iheightposition, iweightposition, iarea, icity, icountry, uageposition, uheightposition, uweightposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                    iageposition + "," + iheightposition + "," +
                    iweightposition + ",'" + "" + "','" + "" + "','" +
                    "" + "'," + uageposition + "," + uheightposition +
                    "," + uweightposition + ",'" + "" + "','" + "" +
                    "','" + "" + "','" + "Male" + "','" + "Female" +
                    "','" + "" + "','" + "" + "','" + "" + "','" + "" +
                    "','" + "Dating" + "', '" + "istuff" + "');"
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
                "catalog='Dating'", null, null, null, null
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
        setContentView(R.layout.mstuff)
        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.iStuffprofile)
        one.setIndicator("Your Profile")
        tabs.addTab(one)

        val two = tabs.newTabSpec("two")
        two.setContent(R.id.uStuff)
        two.setIndicator("Partner Profile")
        tabs.addTab(two)

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempdatingcursor = myDatabase!!
                .query("tempdating", null, null, null, null, null, null)

            if (tempdatingcursor != null) {
                if (tempdatingcursor.isFirst) {
                    do {
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
        iStuffAge = findViewById(R.id.iStuffage) as Spinner
        val adapter1 = ArrayAdapter.createFromResource(
            this, R.array.iStuffage, android.R.layout.simple_spinner_item
        )
        iStuffAge!!.adapter = adapter1
        iStuffAge!!.setSelection(getiageposition)
        iStuffAge!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getistuffage = iStuffAge!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempdating set iageposition=" +
                            position
                    )
                    iageposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffSex = findViewById(R.id.iStuffmaleradiobutton) as RadioButton
        iStuffFemale = findViewById(R.id.iStufffemaleradiobutton) as RadioButton
        if (getisex == "Male") {
            iStuffSex!!.isChecked = true
        } else {
            iStuffSex!!.isChecked = false
            iStuffFemale!!.isChecked = true
        }

        iStuffSex!!.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (iStuffSex!!.isChecked) {
                getisex = "Male"
            } else {
                getisex = "Female"
            }
            myDatabase!!.execSQL(
                "UPDATE tempdating set isex='" + getisex + "'"
            )
        }

        iStuffHeight = findViewById(R.id.iStuffheight) as Spinner
        val adapter3 = ArrayAdapter.createFromResource(
            this, R.array.iStuffheight,
            android.R.layout.simple_spinner_item
        )
        iStuffHeight!!.adapter = adapter3
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
                        "UPDATE tempdating set iheightposition=" +
                            position
                    )
                    iheightposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        iStuffWeight = findViewById(R.id.iStuffweight) as Spinner
        val adapter4 = ArrayAdapter.createFromResource(
            this, R.array.iStuffweight,
            android.R.layout.simple_spinner_item
        )
        iStuffWeight!!.adapter = adapter4
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
                        "UPDATE tempdating set iweightposition=" +
                            position
                    )
                    iweightposition = position
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
        uarea = findViewById(R.id.uarea) as TextView
        uarea!!.text = getuarea
        ucity = findViewById(R.id.ucity) as TextView
        ucity!!.text = getucity
        ucountry = findViewById(R.id.ucountry) as TextView
        ucountry!!.text = getucountry
        ustuffage = findViewById(R.id.agespinner1) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.age, android.R.layout.simple_spinner_item
        )
        ustuffage!!.adapter = adapter
        ustuffage!!.setSelection(getuageposition)
        ustuffage!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffAge = ustuffage!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempdating set uageposition=" +
                            position
                    )
                    uageposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffSex = findViewById(R.id.uStufffemaleradiobutton) as RadioButton
        uStuffFemale = findViewById(R.id.uStuffmaleradiobutton) as RadioButton
        if (getusex == "Female") {
            uStuffSex!!.isChecked = true
        } else {
            uStuffSex!!.isChecked = false
            uStuffFemale!!.isChecked = true
        }

        uStuffSex!!.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (uStuffSex!!.isChecked) {
                getusex = "Female"
            } else {
                getusex = "Male"
            }
            myDatabase!!.execSQL(
                "UPDATE tempdating set usex='" + getusex + "'"
            )
        }

        uStuffHeight = findViewById(R.id.uheight) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.height,
            android.R.layout.simple_spinner_item
        )
        uStuffHeight!!.adapter = adapter
        uStuffHeight!!.setSelection(getuheightposition)
        uStuffHeight!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffHeight =
                        uStuffHeight!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempdating set uheightposition=" +
                            position
                    )
                    uheightposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffWeight = findViewById(R.id.uweight) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.weight,
            android.R.layout.simple_spinner_item
        )
        uStuffWeight!!.adapter = adapter
        uStuffWeight!!.setSelection(getuweightposition)
        uStuffWeight!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffWeight =
                        uStuffWeight!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempdating set uweightposition=" +
                            position
                    )
                    uweightposition = position
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
                "UPDATE tempdating set stufftype='" + "istuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Dating, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "tempdating")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Dating, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempdating")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val uchoose = findViewById(R.id.selectustufflocation) as Button
        uchoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE tempdating set stufftype='" + "ustuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Dating, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "tempdating")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Dating, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempdating")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val button = findViewById(R.id.Save) as Button
        button.setOnClickListener {
            getistuffage = iStuffAge!!.selectedItem as String?
            getiStuffHeight = iStuffHeight!!.selectedItem as String?
            getiStuffWeight = iStuffWeight!!.selectedItem as String?
            iageposition = iStuffAge!!.selectedItemPosition
            iheightposition = iStuffHeight!!.selectedItemPosition
            iweightposition = iStuffWeight!!.selectedItemPosition
            idetails = "DatingIcon \n Age=" + getistuffage + " Sex=" +
                getisex + " Height=" + getiStuffHeight + " Weight=" +
                getiStuffWeight + " Area=" + getiarea + " City=" +
                geticity + " Country=" + geticountry
            getilatitudes = java.lang.Double.parseDouble(getilatitude)
            getilatitudes = getilatitudes * 1E6
            getilongitudes = java.lang.Double.parseDouble(getilongitude)
            getilongitudes = getilongitudes * 1E6

            if (fromeditquery != null && getkey != 0) {
                myDatabase!!.execSQL(
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
                        ";"
                )
                myDatabase!!.execSQL(
                    "UPDATE Dating set iage='" +
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
                        "' where catagory='" + "userDating" + "';"
                )
            } else {
                try {
                    myDatabase!!.execSQL(
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
                            getulongitude + "','" + "true" + "');"
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
                            "userDating" + "';"
                    )
                    myDatabase!!.execSQL(
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
                            "Dating" + "', '" + "istuff" + "');"
                    )
                } catch (exce: Exception) {
                    //Toast.makeText(Dating.this, idetails, Toast.LENGTH_LONG).show();
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS Dating" +
                            " (key INTEGER PRIMARY KEY,iage VARCHAR, isex VARCHAR, iheight VARCHAR, " +
                            "iweight VARCHAR, iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR," +
                            "uage VARCHAR, usex VARCHAR, uheight VARCHAR," +
                            "uweight VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, queryStatus VARCHAR,queryDate DATE);"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER insert_querydate_Dating after INSERT on Dating BEGIN update Dating set queryDate=DATE('NOW') WHERE key=new.key; END;"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER delete_querydate_Dating before insert on Dating BEGIN delete from Dating where queryDate<DATE('NOW','-7 day');END;"
                    )
                    myDatabase!!.execSQL(
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
                            getulongitude + "','" + "true" + "');"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS datingposition" +
                            " (key INTEGER PRIMARY KEY,iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
                    )
                    myDatabase!!.execSQL(
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
                            "Dating" + "', '" + "istuff" + "');"
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
                            "userDating" + "';"
                    )
                }
            }
            myDatabase!!.execSQL("drop table " + "tempdating" + ";")
            val intent = Intent(this@Dating, FindandInstall::class.java)
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
                val intent1 = Intent(this@Dating, MapResults::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }

            2 -> {
                val intent2 = Intent(this@Dating, FindandInstall::class.java)
                startActivityForResult(intent2, 0)
                finish()
            }
            3 -> {
                val intent3 = Intent(this@Dating, Settings::class.java)
                startActivityForResult(intent3, 0)
                finish()
            }
            4 -> {
                val intent = Intent(this@Dating, FindandInstall::class.java)
                startActivityForResult(intent, 0)
                finish()
            }
            5 -> {
                try {
                    val c = myDatabase!!.query(
                        "Dating", null, null, null,
                        null, null, null
                    )
                    val listmStuff =
                        Intent(this@Dating, ListQuery::class.java)
                    startActivityForResult(listmStuff, 0)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Dating, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            6 -> {
                try {
                    val c = myDatabase!!.query(
                        "Dating", null, null, null,
                        null, null, null
                    )
                    val deleteQuery =
                        Intent(this@Dating, DeleteQuery::class.java)
                    startActivityForResult(deleteQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Dating, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            7 -> {
                try {
                    val c = myDatabase!!.query(
                        "Dating", null, null, null,
                        null, null, null
                    )
                    val viewQuery = Intent(this@Dating, ViewQuery::class.java)
                    startActivityForResult(viewQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Dating, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            8, 9 -> {
                if (item.itemId == 8) {
                    val b1 = Bundle()
                    val settheme = Intent(this@Dating, SettingTheme::class.java)
                    b1.putString("value1", "Dating")
                    b1.putString("class", "3")
                    settheme.putExtras(b1)
                    startActivityForResult(settheme, 0)
                    finish()
                }
                val B1 = Bundle()
                B1.putString("requestcatalog", "Dating")
                val mediaintent =
                    Intent(this@Dating, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
