package com.mobeegal.android.activity

/*
<!--
$Id:: Preferences.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TabHost
import com.mobeegal.android.R
import com.mobeegal.android.content.SendLocation
import com.mobeegal.android.util.ViewMenu
import java.util.ArrayList

/**
 * @author http://mobeegal/in
 */
class Preferences : Activity() {

    private var settimename1: String? = null
    private var res: String? = null
    private var timeid: Long = 0
    private var myDatabase: SQLiteDatabase? = null
    private var serviceInterval: String? = null
    private var getservicerequestinterval: String? = null
    private var TimeInterval: String? = null
    private var getmStuffView: String? = null
    private var MStuffView: String? = null
    private var time: String? = null
    private var views: String? = null
    private var viewname1: String? = null
    private var servicename1: String? = null
    private var timename1: String? = null
    val results: ArrayList<String?> = ArrayList()
    var mStuffView: RadioButton? = null
    var mStufftext: RadioButton? = null
    var servicerequestinterval: RadioButton? = null
    var servicerequestmanual: RadioButton? = null
    var timeinterval: Spinner? = null
    var timeintervalLocation: Spinner? = null
    var gettime: String? = null
    var getPosition: Int = 0
    var getPositionLocation: Int = 0
    var elapsedtime: Number? = null
    var getTimeInterval: String? = ""
    var set: Settings? = null
    var interval: Array<String>? = null
    var firstvalue: Int = 0
    var secondvalue: String? = null
    var gettingcategory: String? = null
    var auto: CheckBox? = null
    var manual: CheckBox? = null
    var turnon: RadioButton? = null
    var turnoff: RadioButton? = null
    var myLocationManager: LocationManager? = null
    var loc: Location? = null
    var lat: Double? = null
    var lng: Double? = null
    var numericValue: Long = 0
    var lbstatestatus: String? = null
    var lbservice: String? = null
    var lbstate: String? = null
    var lbtime: String? = null
    var lbservicestatus: String? = null
    var getTime: String? = null
    var timeIntervalLocation: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.preferences)
        Log.i("Preferences", "1")
        myDatabase = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        val col = arrayOf(
            "views", "service", "time", "settime", "lbservice",
            "lbstate", "lbtimesettings"
        )
        val c = myDatabase!!.query(
            "Preferences", col, null, null,
            null, null, null
        )
        val viewname = c.getColumnIndexOrThrow("views")
        val servicename = c.getColumnIndexOrThrow("service")
        val timename = c.getColumnIndexOrThrow("time")
        val settimename = c.getColumnIndexOrThrow("settime")
        val lbservicecolumn = c.getColumnIndexOrThrow("lbservice")
        val lbstatecolumn = c.getColumnIndexOrThrow("lbstate")
        val lbtimecolumn = c.getColumnIndexOrThrow("lbtimesettings")
        Log.i("Preferences", "2")
        if (c != null) {
            if (c.isFirst) {
                do {
                    viewname1 = c.getString(viewname)
                    servicename1 = c.getString(servicename)
                    timename1 = c.getString(timename)
                    settimename1 = c.getString(settimename)
                    lbservice = c.getString(lbservicecolumn)
                    lbstate = c.getString(lbstatecolumn)
                    lbtime = c.getString(lbtimecolumn)
                    results.add(viewname1)
                    results.add(servicename1)
                    results.add(timename1)
                    results.add(settimename1)
                } while (c.moveToNext())
            }
        }
        Log.i("Preferences", "3")
        res = results.toString()
        c.close()
        Log.i("Preferences", "4")
        val tabs = this.findViewById(R.id.tabs) as TabHost
        tabs.setup()

        val one = tabs.newTabSpec("one")
        one.setContent(R.id.mStuffView)
        one.setIndicator("mStuffView")
        tabs.addTab(one)
        mStuffView = findViewById(R.id.mapviewradiobutton) as RadioButton
        mStufftext = findViewById(R.id.textviewradiobutton) as RadioButton

        if (viewname1.toString() == "MapView") {
            mStuffView!!.isChecked = true
            mStufftext!!.isChecked = false
        } else {
            mStufftext!!.isChecked = true
            mStuffView!!.isChecked = false
        }
        Log.i("Preferences", "5")
        val two = tabs.newTabSpec("two")
        two.setContent(R.id.servicerequestinterval)
        two.setIndicator("Service request interval")
        tabs.addTab(two)

        timeinterval = findViewById(R.id.timeinterval) as Spinner
        timeinterval!!.visibility = View.INVISIBLE
        servicerequestinterval =
            findViewById(R.id.autoradiobutton) as RadioButton
        servicerequestmanual =
            findViewById(R.id.manualradiobutton) as RadioButton


        Log.i("Preferences", "6")
        val locationbroadcasting =
            tabs.newTabSpec("Location broadcasting")
        locationbroadcasting.setContent(R.id.Locationbroadcasting)
        locationbroadcasting.setIndicator("Location broadcasting")

        myLocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        loc = myLocationManager!!.getLastKnownLocation("gps")
        lat = loc!!.latitude * 1E6
        lng = loc!!.longitude * 1E6
        timeintervalLocation =
            findViewById(R.id.timeintervallocation) as Spinner

        val adapterLocation = ArrayAdapter.createFromResource(
            this, R.array.timeperiod, android.R.layout.simple_spinner_item
        )
        timeintervalLocation!!.adapter = adapterLocation
        val getpos = Integer.parseInt(lbtime)
        timeintervalLocation!!.setSelection(getpos)
        turnon = findViewById(R.id.turnonradiobutton) as RadioButton
        turnoff = findViewById(R.id.turnoffradiobutton) as RadioButton
        auto = findViewById(R.id.auto) as CheckBox
        manual = findViewById(R.id.manual) as CheckBox
        auto!!.visibility = CheckBox.INVISIBLE
        manual!!.visibility = CheckBox.INVISIBLE
        timeintervalLocation!!.visibility = Spinner.INVISIBLE
        Log.i("Preferences", "7")
        turnon!!.setOnCheckedChangeListener { arg0, arg1 ->
            if (turnon!!.isChecked) {
                lbservicestatus = "on"
                auto!!.visibility = CheckBox.VISIBLE
                manual!!.visibility = CheckBox.VISIBLE
            } else {
                auto!!.visibility = CheckBox.INVISIBLE
                manual!!.visibility = CheckBox.INVISIBLE
            }
            manual!!.setOnCheckedChangeListener { arg0, arg1 ->
                if (manual!!.isChecked) {
                    auto!!.isChecked = false
                    lbstatestatus = "manual"
                    timeintervalLocation!!.visibility = Spinner.VISIBLE
                } else {
                    timeintervalLocation!!
                        .visibility = Spinner.INVISIBLE
                }
            }
        }

        turnoff!!.setOnCheckedChangeListener { arg0, arg1 ->
            if (turnoff!!.isChecked) {
                lbservicestatus = "off"
                lbstatestatus = "auto"
                auto!!.visibility = CheckBox.INVISIBLE
                manual!!.visibility = CheckBox.INVISIBLE
                timeintervalLocation!!.visibility = Spinner.INVISIBLE
            }
        }
        Log.i("Preferences", "9")
        auto!!.setOnCheckedChangeListener { arg0, arg1 ->
            if (auto!!.isChecked) {
                manual!!.isChecked = false
                timeintervalLocation!!.visibility = Spinner.INVISIBLE
                lbstatestatus = "auto"
            }
        }
        tabs.addTab(locationbroadcasting)
        if (servicename1.toString() == "Auto") {
            servicerequestinterval!!.isChecked = true
            servicerequestmanual!!.isChecked = false
            timeinterval = findViewById(R.id.timeinterval) as Spinner
            timeinterval!!.visibility = View.INVISIBLE
        } else {
            servicerequestmanual!!.isChecked = true
            servicerequestinterval!!.isChecked = false
            timeinterval = findViewById(R.id.timeinterval) as Spinner
            timeinterval!!.visibility = View.VISIBLE
        }

        if (lbservice == "off") {
            turnoff!!.isChecked = true
            auto!!.visibility = CheckBox.INVISIBLE
            manual!!.visibility = CheckBox.INVISIBLE
            timeintervalLocation!!.visibility = Spinner.INVISIBLE
        } else if (lbservice == "on") {
            turnon!!.isChecked = true
            auto!!.visibility = CheckBox.VISIBLE
            manual!!.visibility = CheckBox.VISIBLE
            if (lbstate == "manual") {
                manual!!.isChecked = true
                timeintervalLocation!!.visibility = Spinner.VISIBLE
            } else if (lbstate == "auto") {
                auto!!.isChecked = true
                timeintervalLocation!!.visibility = Spinner.INVISIBLE
            }
        }
        Log.i("Preferences", "10")
        val adapter1 = ArrayAdapter.createFromResource(
            this, R.array.timeperiod, android.R.layout.simple_spinner_item
        )
        timeinterval!!.adapter = adapter1
        val pos = Integer.parseInt(timename1)
        timeinterval!!.setSelection(pos)
        timeinterval!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val manualRadiobutton =
            findViewById(R.id.manualradiobutton) as RadioButton
        if (manualRadiobutton.isEnabled) {
            manualRadiobutton
                .setOnClickListener {
                    timeinterval!!.visibility = View.VISIBLE
                }
        }
        Log.i("Preferences", "11")
        val autoRadiobutton =
            findViewById(R.id.autoradiobutton) as RadioButton
        if (autoRadiobutton.isEnabled) {
            autoRadiobutton.setOnClickListener {
                // default setting 1 minute and getting mstuff from server
                timeinterval!!.visibility = View.INVISIBLE
            }
        }
        tabs.currentTab = 0

        val startTime = SystemClock.elapsedRealtime()
        val savebutton = findViewById(R.id.save) as Button
        savebutton.setOnClickListener {
            Log.i("Preferences", "12")
            val latitudeString = Integer.toString(lat!!.toInt())
            val longitudeString = Integer.toString(lng!!.toInt())
            Log.i("Preferences", "13")
            getTime = timeintervalLocation!!.selectedItem as String
            val strArray = getTime!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            numericValue = java.lang.Long.parseLong(strArray[0])
            if (turnoff!!.isChecked) {
                val alarmmanager =
                    getSystemService(ALARM_SERVICE) as AlarmManager
                val intent =
                    Intent(this@Preferences, SendLocation::class.java)
                val pi = PendingIntent.getActivity(
                    applicationContext, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                alarmmanager.cancel(pi)
            }
            if (lbstatestatus == "auto" &&
                lbservicestatus == "on"
            ) {
                val intobject2 =
                    Intent(this@Preferences, SendLocation::class.java)
                val bundle = Bundle()
                bundle.putString("latitude", latitudeString)
                bundle.putString("longitude", longitudeString)
                intobject2.putExtras(bundle)
                val alarmmanager =
                    getSystemService(ALARM_SERVICE) as AlarmManager
                val pi = PendingIntent.getActivity(
                    applicationContext, 0, intobject2,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                alarmmanager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime,
                    (60 * 1000).toLong(), pi
                )
            }
            if (lbstatestatus == "manual" &&
                lbservicestatus == "on"
            ) {
                if (strArray[1] == "Minutes") {
                    val intobject2 = Intent(
                        this@Preferences,
                        SendLocation::class.java
                    )
                    val bundle = Bundle()
                    bundle.putString("latitude", latitudeString)
                    bundle.putString("longitude", longitudeString)
                    intobject2.putExtras(bundle)
                    val alarmmanager =
                        getSystemService(ALARM_SERVICE) as AlarmManager
                    val pi = PendingIntent.getActivity(
                        applicationContext, 0, intobject2,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    alarmmanager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        startTime, numericValue * 60 * 1000,
                        pi
                    )
                } else if (strArray[1] == "Hours") {
                    val intobject2 = Intent(
                        this@Preferences,
                        SendLocation::class.java
                    )
                    val bundle = Bundle()
                    bundle.putString("latitude", latitudeString)
                    bundle.putString("longitude", longitudeString)
                    intobject2.putExtras(bundle)
                    val alarmmanager =
                        getSystemService(ALARM_SERVICE) as AlarmManager
                    val pi = PendingIntent.getActivity(
                        applicationContext, 0, intobject2,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    alarmmanager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        startTime, numericValue * 60 * 60 * 1000,
                        pi
                    )
                } else if (strArray[1] == "Day") {
                    val intobject2 = Intent(
                        this@Preferences,
                        SendLocation::class.java
                    )
                    val bundle = Bundle()
                    bundle.putString("latitude", latitudeString)
                    bundle.putString("longitude", longitudeString)
                    intobject2.putExtras(bundle)
                    val alarmmanager =
                        getSystemService(ALARM_SERVICE) as AlarmManager
                    val pi = PendingIntent.getActivity(
                        applicationContext, 0, intobject2,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    alarmmanager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        startTime, numericValue * 24 * 60 * 60 * 1000,
                        pi
                    )
                }
            }
            // Based on the time interval sending request to server for matchin stuffs
            mStuffView =
                findViewById(R.id.mapviewradiobutton) as RadioButton
            getPosition = timeinterval!!.selectedItemPosition
            getPositionLocation =
                timeintervalLocation!!.selectedItemPosition
            timeIntervalLocation = Integer.toString(getPositionLocation)
            TimeInterval = Integer.toString(getPosition)
            val getsettime = timeinterval!!.selectedItem as String
            if (mStuffView!!.isChecked) {
                getmStuffView = "MapView"
            } else {
                getmStuffView = "TextView"
            }
            servicerequestinterval =
                findViewById(R.id.autoradiobutton) as RadioButton
            if (servicerequestinterval!!.isChecked) {
                getservicerequestinterval = "Auto"
            } else {
                getservicerequestinterval = "Manual"
            }
            myDatabase!!.execSQL(
                "UPDATE Preferences set views='" +
                    getmStuffView + "',service='" +
                    getservicerequestinterval + "',time='" + TimeInterval +
                    "',settime='" + getsettime + "',lbservice='" +
                    lbservicestatus + "',lbstate='" + lbstatestatus +
                    "',lbtimesettings='" + timeIntervalLocation +
                    "' where preference=preference"
            )
            finish()
        }
    }

    // MenuView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    //  Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            //  mStuff Menu
            1 -> {
                val stuffCheckintent =
                    Intent(this@Preferences, MapResults::class.java)
                startActivityForResult(stuffCheckintent, 0)
                finish()
            }
            2 -> {
                val intent1 =
                    Intent(this@Preferences, FindandInstall::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }
            3 -> {
                val settings = Intent(this@Preferences, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
