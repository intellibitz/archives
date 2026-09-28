package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Home.java 14 2008-08-19 06:36:45Z muthu.ramadoss                       $: Id of last commit
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

/**
 * @author mobeegal
 */
class Home : Activity() {

    var key: Int = 1
    var myDatabase: SQLiteDatabase? = null
    var getiStuffRentalType: String? = null
    var getiStuffMisc: String? = null
    var getiStuffStatus: String? = null
    var getiStuffRate: String? = null
    var getuStuffRentalType: String? = null
    var getuStuffMisc: String? = null
    var getuStuffStatus: String? = null
    var getuStuffRate: String? = null
    var adapter: ArrayAdapter<*>? = null
    var irentalposition: Int = 0
    var imiscposition: Int = 0
    var istatusposition: Int = 0
    var irateposition: Int = 0
    var urentalposition: Int = 0
    var umiscposition: Int = 0
    var ustatusposition: Int = 0
    var urateposition: Int = 0
    var getirentalposition: Int = 0
    var getimiscposition: Int = 0
    var getistatusposition: Int = 0
    var getirateposition: Int = 0
    var geturentalposition: Int = 0
    var getumiscposition: Int = 0
    var getustatusposition: Int = 0
    var geturateposition: Int = 0
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
    var fromeditquery: Bundle? = null
    var getkey: Int = 0
    var theme: Int = 0
    private var viewtypename: String? = null

    // String catalog;
    /**
     * Called when the activity is first created.
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        // ToDo add your GUI initialization code here
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val temprentalcursor = myDatabase!!
                .query("temprental", null, null, null, null, null, null)
        } catch (e: Exception) {
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS temprental" +
                    " (irentalposition NUMERIC, imiscposition NUMERIC, istatusposition NUMERIC, irateposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, urentalposition NUMERIC, umiscposition NUMERIC, ustatusposition NUMERIC,urateposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
            )
            myDatabase!!.execSQL(
                "INSERT INTO temprental (irentalposition,imiscposition , istatusposition , irateposition , iarea , icity , icountry , urentalposition , umiscposition , ustatusposition ,urateposition , uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype ) VALUES (" +
                    irentalposition + "," + imiscposition + "," +
                    istatusposition + "," + irateposition + ",'" + "" +
                    "','" + "" + "','" + "" + "'," + urentalposition +
                    "," + umiscposition + "," + ustatusposition + "," +
                    urateposition + ",'" + "" + "','" + "" + "','" +
                    "" + "','" + "" + "','" + "" + "','" + "" + "','" +
                    "" + "','" + "Rental" + "', '" + "istuff" + "');"
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
            val themecursor = myDatabase!!.query(
                "Theme", null,
                "catalog='Home'", null, null, null, null
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
        setContentView(R.layout.home)

        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.iStuffprofile)
        one.setIndicator("Owner")
        tabs.addTab(one)

        val two = tabs.newTabSpec("two")
        two.setContent(R.id.uStuffprofile)
        two.setIndicator("Tenant")
        tabs.addTab(two)
        fromeditquery = this.intent.extras
        if (fromeditquery != null) {
            getkey = fromeditquery!!.getInt("key")
        }

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val temprentalcursor = myDatabase!!
                .query("temprental", null, null, null, null, null, null)

            if (temprentalcursor != null) {
                if (temprentalcursor.isFirst) {
                    do {
                        getirentalposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "irentalposition"
                                )
                            )
                        getimiscposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "imiscposition"
                                )
                            )
                        getistatusposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "istatusposition"
                                )
                            )
                        getirateposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "irateposition"
                                )
                            )

                        geturentalposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "urentalposition"
                                )
                            )
                        getumiscposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "umiscposition"
                                )
                            )
                        getustatusposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "ustatusposition"
                                )
                            )
                        geturateposition = temprentalcursor
                            .getInt(
                                temprentalcursor.getColumnIndexOrThrow(
                                    "urateposition"
                                )
                            )

                        getiarea = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "iarea"
                            )
                        )
                        geticity = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "icity"
                            )
                        )
                        geticountry = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "icountry"
                            )
                        )
                        getuarea = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "uarea"
                            )
                        )
                        getucity = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "ucity"
                            )
                        )
                        getucountry = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "ucountry"
                            )
                        )
                        getilatitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "ilatitude"
                            )
                        )
                        getilongitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "ilongitude"
                            )
                        )
                        getulatitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "ulatitude"
                            )
                        )
                        getulongitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "ulongitude"
                            )
                        )
                        getcategory = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "category"
                            )
                        )
                        getstufftype = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                "stufftype"
                            )
                        )
                    } while (temprentalcursor.moveToNext())
                }
            }
        } catch (e: Exception) {
        }


        val iStuffRentalType =
            findViewById(R.id.iStuffRentalType) as Spinner
        val adapter1 = ArrayAdapter.createFromResource(
            this, R.array.iStuffRentalType,
            android.R.layout.simple_spinner_item
        )
        iStuffRentalType.adapter = adapter1
        iStuffRentalType.setSelection(getirentalposition)
        iStuffRentalType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffRentalType =
                        iStuffRentalType.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set irentalposition=" +
                            position
                    )
                    irentalposition = position

                    if (getiStuffRentalType == "Select Rental") {
                        getiStuffRentalType = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffMisc = findViewById(R.id.iStuffMisc) as Spinner
        val adapter3 = ArrayAdapter.createFromResource(
            this, R.array.iStuffMiscellaneous,
            android.R.layout.simple_spinner_item
        )
        iStuffMisc.adapter = adapter3
        iStuffMisc.setSelection(getimiscposition)
        iStuffMisc.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffMisc = iStuffMisc.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set imiscposition=" +
                            position
                    )
                    imiscposition = position
                    if (getiStuffMisc == "Select Misc") {
                        getiStuffMisc = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffRate = findViewById(R.id.iStuffRate) as Spinner
        val adapter4 = ArrayAdapter.createFromResource(
            this, R.array.iStuffRate, android.R.layout.simple_spinner_item
        )
        iStuffRate.adapter = adapter4
        iStuffRate.setSelection(getirateposition)
        iStuffRate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffRate = iStuffRate.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set irateposition=" +
                            position
                    )
                    irateposition = position
                    if (getiStuffRate == "Select Rate") {
                        getiStuffRate = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val iStuffStatus = findViewById(R.id.iStuffStatus) as Spinner
        val adapter15 = ArrayAdapter.createFromResource(
            this, R.array.iStuffStatus,
            android.R.layout.simple_spinner_item
        )
        iStuffStatus.adapter = adapter15
        iStuffStatus.setSelection(getistatusposition)
        iStuffStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffStatus =
                        iStuffStatus.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set istatusposition=" +
                            position
                    )
                    istatusposition = position
                    if (getiStuffStatus == "Select Status") {
                        getiStuffStatus = "null"
                    }
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

        val uStuffRentalType =
            findViewById(R.id.uStuffRentalType) as Spinner
        val adapter8 = ArrayAdapter.createFromResource(
            this, R.array.iStuffRentalType,
            android.R.layout.simple_spinner_item
        )
        uStuffRentalType.adapter = adapter8
        uStuffRentalType.setSelection(geturentalposition)
        uStuffRentalType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffRentalType =
                        uStuffRentalType.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set urentalposition=" +
                            position
                    )
                    urentalposition = position
                    if (getuStuffRentalType == "Select RentalType") {
                        getuStuffRentalType = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffMisc = findViewById(R.id.uStuffMisc) as Spinner
        val adapter9 = ArrayAdapter.createFromResource(
            this, R.array.iStuffMiscellaneous,
            android.R.layout.simple_spinner_item
        )
        uStuffMisc.adapter = adapter9
        uStuffMisc.setSelection(getumiscposition)
        uStuffMisc.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffMisc = uStuffMisc.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set umiscposition=" +
                            position
                    )
                    umiscposition = position
                    if (getuStuffMisc == "Select Misc") {
                        getuStuffMisc = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffRate = findViewById(R.id.uStuffRate) as Spinner
        val adapter10 = ArrayAdapter.createFromResource(
            this, R.array.uStuffRate, android.R.layout.simple_spinner_item
        )
        uStuffRate.adapter = adapter10
        uStuffRate.setSelection(geturateposition)
        uStuffRate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffRate = uStuffRate.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set urateposition=" +
                            position
                    )
                    urateposition = position
                    if (getuStuffRate == "Select Rate") {
                        getuStuffRate = "null"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val uStuffStatus = findViewById(R.id.uStuffStatus) as Spinner
        val adapter17 = ArrayAdapter.createFromResource(
            this, R.array.iStuffStatus,
            android.R.layout.simple_spinner_item
        )
        uStuffStatus.adapter = adapter17
        uStuffStatus.setSelection(getustatusposition)
        uStuffStatus.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffStatus =
                        uStuffStatus.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE temprental set ustatusposition=" +
                            position
                    )
                    ustatusposition = position
                    if (getuStuffStatus == "Select Status") {
                        getuStuffStatus = "null"
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
                "UPDATE temprental set stufftype='" + "istuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Home, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "temprental")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Home, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "temprental")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val uchoose = findViewById(R.id.selectustufflocation) as Button
        uchoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE temprental set stufftype='" + "ustuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder =
                    Intent(this@Home, LocationFinder::class.java)
                val b = Bundle()
                b.putString("tablename", "temprental")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Home, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "temprental")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val button = findViewById(R.id.Save) as Button
        button.setOnClickListener(object : Button.OnClickListener {

            private var getilongitudes: Double = 0.0
            private var getilatitudes: Double = 0.0
            private var idetails: String? = null

            override fun onClick(v: View?) {
                getiStuffRentalType =
                    iStuffRentalType.selectedItem as String?
                getiStuffMisc = iStuffMisc.selectedItem as String?
                getiStuffStatus = iStuffStatus.selectedItem as String?
                getiStuffRate = iStuffRate.selectedItem as String?

                irentalposition = iStuffRentalType.selectedItemPosition
                imiscposition = iStuffMisc.selectedItemPosition
                istatusposition = iStuffStatus.selectedItemPosition
                irateposition = iStuffRate.selectedItemPosition

                idetails = "rentaltype=" + getiStuffRentalType + " imisc=" +
                    getiStuffMisc + " rate=" + getiStuffRate + " status=" +
                    getiStuffStatus + " Area=" + getiarea + " City=" +
                    geticity + " country=" + geticountry

                getilatitudes = java.lang.Double.parseDouble(getilatitude)
                getilatitudes = getilatitudes * 1E6
                getilongitudes = java.lang.Double.parseDouble(getilongitude)
                getilongitudes = getilongitudes * 1E6


                if (fromeditquery != null && getkey != 0) {
                    myDatabase!!.execSQL(
                        "update Home set irental='" +
                            getiStuffRentalType + "', imisc ='" +
                            getiStuffMisc + "',irate='" + getiStuffRate +
                            "',istatus='" + getiStuffStatus + "',iarea='" +
                            getiarea + "',icity='" + geticity + "',icountry='" +
                            geticountry + "',urental='" + getuStuffRentalType +
                            "', umisc ='" + getuStuffMisc + "',urate='" +
                            getuStuffRate + "',ustatus='" + getuStuffStatus +
                            "',uarea='" + getuarea + "',ucity='" + getucity +
                            "',ucountry='" + getucountry + "',ilatitude = '" +
                            getilatitude + "',ilongitude='" + getilongitude +
                            "',ulatitude='" + getulatitude + "',ulongitude='" +
                            getulongitude +
                            "',queryDate=DATE('NOW') where key = " + getkey +
                            ";"
                    )
                    myDatabase!!.execSQL(
                        "update rentalposition set irentalposition=" +
                            irentalposition + ", imiscposition=" +
                            imiscposition + ", irateposition=" +
                            irateposition + ", istatusposition=" +
                            istatusposition + ", iarea='" + getiarea +
                            "', icity='" + geticity + "', icountry='" +
                            geticountry + "', ilatitude='" +
                            getilatitude + "', ilongitude='" +
                            getilongitude + "', urentalposition=" +
                            urentalposition + ", umiscposition=" +
                            umiscposition + ", urateposition=" +
                            urateposition + ", ustatusposition=" +
                            ustatusposition + ", uarea='" + getuarea +
                            "', ucity='" + getucity + "', ucountry='" +
                            getucountry + "', ulatitude='" +
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
                            "' where catagory='userRental';"
                    )
                } else {
                    try {
                        myDatabase!!.execSQL(
                            "INSERT INTO Home (irental, imisc, irate, istatus, " +
                                "iarea,icity,icountry,ilatitude,ilongitude," +
                                "urental, umisc, urate," +
                                "ustatus, uarea,ucity,ucountry,ulatitude,ulongitude, queryStatus) VALUES ('" +
                                getiStuffRentalType + "','" +
                                getiStuffMisc + "','" + getiStuffRate +
                                "','" + getiStuffStatus + "','" +
                                getiarea + "','" + geticity + "','" +
                                geticountry + "','" + getilatitude +
                                "','" + getilongitude + "','" +
                                getuStuffRentalType + "','" +
                                getuStuffMisc + "','" + getuStuffRate +
                                "','" + getuStuffStatus + "','" +
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
                            "INSERT INTO rentalposition (irentalposition , imiscposition , irateposition , istatusposition  ,iarea , icity , icountry , urentalposition , umiscposition , urateposition ,ustatusposition , uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype) VALUES (" +
                                irentalposition + "," + imiscposition +
                                "," + irateposition + "," +
                                istatusposition + ",'" + getiarea +
                                "','" + geticity + "','" + geticountry +
                                "'," + urentalposition + "," +
                                umiscposition + "," + urateposition +
                                "," + ustatusposition + ",'" +
                                getuarea + "','" + getucity + "','" +
                                getucountry + "','" + getilatitude +
                                "','" + getilongitude + "','" +
                                getulatitude + "','" + getulongitude +
                                "','" + "Rental" + "', '" + "istuff" +
                                "');"
                        )
                        myDatabase!!.execSQL(
                            "Update mStuffdetails set details='" +
                                idetails + "', latitude='" +
                                getilatitudes + "', longitude='" +
                                getilongitudes + "', location='" +
                                geticountry +
                                "' where catagory='userRental';"
                        )
                    } catch (exce: Exception) {
                        myDatabase!!.execSQL(
                            "CREATE TABLE IF NOT EXISTS Home" +
                                " (key INTEGER PRIMARY KEY,irental VARCHAR, imisc VARCHAR, irate VARCHAR, " +
                                "istatus VARCHAR, iarea VARCHAR,icity VARCHAR,icountry VARCHAR,ilatitude VARCHAR,ilongitude VARCHAR," +
                                "urental VARCHAR, umisc VARCHAR, urate VARCHAR," +
                                "ustatus VARCHAR, uarea VARCHAR,ucity VARCHAR,ucountry VARCHAR,ulatitude VARCHAR,ulongitude VARCHAR, queryStatus VARCHAR,queryDate DATE);"
                        )
                        myDatabase!!.execSQL(
                            "CREATE TRIGGER insert_querydate_Home after INSERT on Home BEGIN update Home set queryDate=DATE('NOW') WHERE key=new.key; END;"
                        )
                        myDatabase!!.execSQL(
                            "CREATE TRIGGER delete_querydate_Home before insert on Home BEGIN delete from Home where queryDate<DATE('NOW','-7 day');END;"
                        )
                        myDatabase!!.execSQL(
                            "INSERT INTO Home (irental, imisc, irate, istatus, " +
                                "iarea,icity,icountry,ilatitude,ilongitude," +
                                "urental, umisc, urate," +
                                "ustatus, uarea,ucity,ucountry,ulatitude,ulongitude, queryStatus) VALUES ('" +
                                getiStuffRentalType + "','" +
                                getiStuffMisc + "','" + getiStuffRate +
                                "','" + getiStuffStatus + "','" +
                                getiarea + "','" + geticity + "','" +
                                geticountry + "','" + getilatitude +
                                "','" + getilongitude + "','" +
                                getuStuffRentalType + "','" +
                                getuStuffMisc + "','" + getuStuffRate +
                                "','" + getuStuffStatus + "','" +
                                getuarea + "','" + getucity + "','" +
                                getucountry + "','" + getulatitude +
                                "','" + getulongitude + "','" + "true" +
                                "');"
                        )
                        myDatabase!!.execSQL(
                            "CREATE TABLE IF NOT EXISTS rentalposition" +
                                " (key INTEGER PRIMARY KEY,irentalposition NUMERIC, imiscposition NUMERIC, irateposition NUMERIC, istatusposition NUMERIC,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, urentalposition NUMERIC, umiscposition NUMERIC, urateposition NUMERIC,ustatusposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
                        )
                        myDatabase!!.execSQL(
                            "INSERT INTO rentalposition (irentalposition , imiscposition , irateposition , istatusposition  ,iarea , icity , icountry , urentalposition , umiscposition , urateposition ,ustatusposition , uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype) VALUES (" +
                                irentalposition + "," + imiscposition +
                                "," + irateposition + "," +
                                istatusposition + ",'" + getiarea +
                                "','" + geticity + "','" + geticountry +
                                "'," + urentalposition + "," +
                                umiscposition + "," + urateposition +
                                "," + ustatusposition + ",'" +
                                getuarea + "','" + getucity + "','" +
                                getucountry + "','" + getilatitude +
                                "','" + getilongitude + "','" +
                                getulatitude + "','" + getulongitude +
                                "','" + "Rental" + "', '" + "istuff" +
                                "');"
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
                                geticountry +
                                "' where catagory='userRental';"
                        )
                    }
                }

                myDatabase!!.execSQL("drop table " + "temprental" + ";")
                val intent = Intent(this@Home, FindandInstall::class.java)
                startActivity(intent)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsmStuffMenu(menu)
        return true
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {

        when (item.itemId) {
            1 -> {
                val intent1 = Intent(this@Home, MapResults::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }

            2 -> {
                val intent2 = Intent(this@Home, FindandInstall::class.java)
                startActivityForResult(intent2, 0)
                finish()
            }
            3 -> {
                val intent3 = Intent(this@Home, Settings::class.java)
                startActivityForResult(intent3, 0)
                finish()
            }
            4 -> {
                val intent = Intent(this@Home, FindandInstall::class.java)
                startActivityForResult(intent, 0)
                finish()
                finish()
            }
            5 -> {
                try {
                    val c = myDatabase!!
                        .query("Home", null, null, null, null, null, null)
                    val listmStuff =
                        Intent(this@Home, HomeListQuery::class.java)
                    startActivityForResult(listmStuff, 0)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Home, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            6 -> {
                try {
                    val c = myDatabase!!
                        .query("Home", null, null, null, null, null, null)
                    val deleteQuery =
                        Intent(this@Home, DeleteQuery::class.java)
                    startActivityForResult(deleteQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Home, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            7 -> {
                try {
                    val c = myDatabase!!
                        .query("Home", null, null, null, null, null, null)
                    val viewQuery =
                        Intent(this@Home, HomeViewQuery::class.java)
                    startActivityForResult(viewQuery, 0)
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(
                        this@Home, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            8 -> {
                val b1 = Bundle()
                val settheme = Intent(this@Home, SettingTheme::class.java)
                b1.putString("value1", "Home")
                b1.putString("class", "7")
                settheme.putExtras(b1)
                startActivityForResult(settheme, 0)
                finish()
                // fall through from Java case 8
                val B1 = Bundle()
                B1.putString("requestcatalog", "Home")
                val mediaintent =
                    Intent(this@Home, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
            9 -> {
                val B1 = Bundle()
                B1.putString("requestcatalog", "Home")
                val mediaintent =
                    Intent(this@Home, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
