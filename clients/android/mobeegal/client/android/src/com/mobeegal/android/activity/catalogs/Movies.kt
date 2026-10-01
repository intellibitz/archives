package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Movies.java 14 2008-08-19 06:36:45Z muthu.ramadoss                     $: Id of last commit
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

class Movies : Activity() {

    var myDatabase: SQLiteDatabase? = null
    var count: Int = 0
    var getiStuffSeatingStyle: String? = null
    var getiStuffMovieType: String? = null
    var getiStuffMovieLanguage: String? = null
    var getiStuffCountry: String? = null
    var getuStuffSeatingStyle: String? = null
    var getuStuffMovieLanguage: String? = null
    var getuStuffMovieType: String? = null
    var adapter: ArrayAdapter<CharSequence>? = null
    var value1: String? = null
    var getfilepath: String? = null
    var key: Int = 1
    var fromeditquery: Bundle? = null
    private var getkey: Int = 0
    var getiMovieTypeposition: Int = 0
    var getiMovieLanguageposition: Int = 0
    var getiSeatingStylyposition: Int = 0
    var getuMovieTypeposition: Int = 0
    var getuMovieLanguageposition: Int = 0
    var getuSeatingStyleposition: Int = 0
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
    var imovietypeposition: Int = 0
    var imovielanguageposition: Int = 0
    var iseatingstyleposition: Int = 0
    var umovietypeposition: Int = 0
    var umovielanguageposition: Int = 0
    var useatingstyleposition: Int = 0
    var iarea: TextView? = null
    var icity: TextView? = null
    var icountry: TextView? = null
    var uarea: TextView? = null
    var ucity: TextView? = null
    var ucountry: TextView? = null
    var iStuffMovieType: Spinner? = null
    var iStuffMovieLanguage: Spinner? = null
    var iStuffSeatingStyle: Spinner? = null
    var uStuffMovieLanguage: Spinner? = null
    var uStuffSeatingStyle: Spinner? = null
    var theme: Int = 0
    private var viewtypename: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        fromeditquery = this.intent.extras
        if (fromeditquery != null) {
            getkey = fromeditquery!!.getInt("key")
        }
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempmoviescursor = myDatabase!!.query(
                "tempmovies", null, null, null, null, null, null
            )
        } catch (e: Exception) {
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS tempmovies" +
                    " (imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, useatingstyleposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
            )
            myDatabase!!.execSQL(
                "INSERT INTO tempmovies (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry,ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                    imovietypeposition + "," + imovielanguageposition +
                    "," + iseatingstyleposition + ",'" + "" + "','" +
                    "" + "','" + "" + "','" + "" + "','" + "" + "'," +
                    imovietypeposition + "," + imovielanguageposition +
                    "," + iseatingstyleposition + ",'" + "" + "','" +
                    "" + "','" + "" + "','" + "" + "','" + "" + "','" +
                    "Movies" + "', '" + "istuff" + "');"
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
                "catalog='Movies'", null, null, null, null
            )
            if (themecursor != null) {
                if (themecursor.isFirst) {
                    do {
                        theme = themecursor.getInt(
                            themecursor.getColumnIndexOrThrow(
                                "theme"
                            )
                        )
                        // String catalog = themecursor.getString(themecursor.getColumnIndexOrThrow("theme"));
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
        setContentView(R.layout.movies)

        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.iStuffprofile)
        one.setIndicator("Owner")
        tabs.addTab(one)

        val two = tabs.newTabSpec("two")
        two.setContent(R.id.uStuffprofile)
        two.setIndicator("Public")
        tabs.addTab(two)

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val tempmoviescursor = myDatabase!!.query(
                "tempmovies", null, null, null, null, null, null
            )

            if (tempmoviescursor != null) {
                if (tempmoviescursor.isFirst) {
                    do {
                        getiMovieTypeposition = tempmoviescursor.getInt(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "imovietypeposition"
                            )
                        )
                        getiMovieLanguageposition = tempmoviescursor.getInt(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "imovielanguageposition"
                            )
                        )
                        getiSeatingStylyposition = tempmoviescursor.getInt(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "iseatingstyleposition"
                            )
                        )

                        getuMovieTypeposition = tempmoviescursor.getInt(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "umovietypeposition"
                            )
                        )
                        getuMovieLanguageposition = tempmoviescursor.getInt(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "umovielanguageposition"
                            )
                        )
                        getuSeatingStyleposition = tempmoviescursor.getInt(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "useatingstyleposition"
                            )
                        )

                        getiarea = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "iarea"
                            )
                        )
                        geticity = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "icity"
                            )
                        )
                        geticountry = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "icountry"
                            )
                        )
                        getuarea = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "uarea"
                            )
                        )
                        getucity = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "ucity"
                            )
                        )
                        getucountry = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "ucountry"
                            )
                        )
                        getilatitude = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "ilatitude"
                            )
                        )
                        getilongitude = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "ilongitude"
                            )
                        )
                        getulatitude = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "ulatitude"
                            )
                        )
                        getulongitude = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "ulongitude"
                            )
                        )
                        getcategory = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "category"
                            )
                        )
                        getstufftype = tempmoviescursor.getString(
                            tempmoviescursor.getColumnIndexOrThrow(
                                "stufftype"
                            )
                        )
                    } while (tempmoviescursor.moveToNext())
                }
            }
        } catch (e: Exception) {
        }

        // Creating a tab1 for ISTUFF

        iStuffMovieType = findViewById(R.id.iStuffmovietype) as Spinner
        val adapter1 = ArrayAdapter.createFromResource(
            this, R.array.MovieType, android.R.layout.simple_spinner_item
        )
        iStuffMovieType!!.adapter = adapter1
        iStuffMovieType!!.setSelection(getiMovieTypeposition)
        iStuffMovieType!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffMovieType =
                        iStuffMovieType!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmovies set imovietypeposition=" +
                            position
                    )
                    imovietypeposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffMovieLanguage = findViewById(R.id.iStuffmovielanguage) as Spinner
        val adapter2 = ArrayAdapter.createFromResource(
            this, R.array.MovieLanguage,
            android.R.layout.simple_spinner_item
        )
        iStuffMovieLanguage!!.adapter = adapter2
        iStuffMovieLanguage!!.setSelection(getiMovieLanguageposition)
        iStuffMovieLanguage!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffMovieLanguage =
                        iStuffMovieLanguage!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmovies set imovielanguageposition=" +
                            position
                    )
                    imovielanguageposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        iStuffSeatingStyle = findViewById(R.id.iStuffMisc) as Spinner
        val adapter3 = ArrayAdapter.createFromResource(
            this, R.array.SeatingStyle,
            android.R.layout.simple_spinner_item
        )
        iStuffSeatingStyle!!.adapter = adapter3
        iStuffSeatingStyle!!.setSelection(getiSeatingStylyposition)
        iStuffSeatingStyle!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getiStuffSeatingStyle =
                        iStuffSeatingStyle!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmovies set iseatingstyleposition=" +
                            position
                    )
                    iseatingstyleposition = position
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

        // Creating a tab2 for USTUFF

        val uStuffMovieType = findViewById(R.id.uStuffmovietype) as Spinner
        val adapter4 = ArrayAdapter.createFromResource(
            this, R.array.MovieType, android.R.layout.simple_spinner_item
        )
        uStuffMovieType.adapter = adapter4
        uStuffMovieType.setSelection(getuMovieTypeposition)
        uStuffMovieType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffMovieType =
                        uStuffMovieType.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmovies set umovietypeposition=" +
                            position
                    )
                    umovietypeposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffMovieLanguage = findViewById(R.id.uStuffmovielanguage) as Spinner
        val adapter15 = ArrayAdapter.createFromResource(
            this, R.array.MovieLanguage,
            android.R.layout.simple_spinner_item
        )
        uStuffMovieLanguage!!.adapter = adapter15
        uStuffMovieLanguage!!.setSelection(getuMovieLanguageposition)
        uStuffMovieLanguage!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffMovieLanguage =
                        uStuffMovieLanguage!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmovies set umovielanguageposition=" +
                            position
                    )
                    umovielanguageposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        uStuffSeatingStyle = findViewById(R.id.uStuffMisc) as Spinner
        val adapter6 = ArrayAdapter.createFromResource(
            this, R.array.SeatingStyle,
            android.R.layout.simple_spinner_item
        )
        uStuffSeatingStyle!!.adapter = adapter6
        uStuffSeatingStyle!!.setSelection(getuSeatingStyleposition)
        uStuffSeatingStyle!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int, id: Long
                ) {
                    getuStuffSeatingStyle =
                        uStuffSeatingStyle!!.selectedItem as String?
                    myDatabase!!.execSQL(
                        "UPDATE tempmovies set useatingstyleposition=" +
                            position
                    )
                    useatingstyleposition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        // added
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
                "UPDATE tempmovies set stufftype='" + "istuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder = Intent(
                    this@Movies,
                    LocationFinder::class.java
                )
                val b = Bundle()
                b.putString("tablename", "tempmovies")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Movies, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempmovies")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val uchoose = findViewById(R.id.selectustufflocation) as Button
        uchoose.setOnClickListener {
            myDatabase!!.execSQL(
                "UPDATE tempmovies set stufftype='" + "ustuff'"
            )
            if (viewtypename == "MapView") {
                val locationfinder = Intent(
                    this@Movies,
                    LocationFinder::class.java
                )
                val b = Bundle()
                b.putString("tablename", "tempmovies")
                b.putInt("key", getkey)
                locationfinder.putExtras(b)
                startActivityForResult(locationfinder, 0)
            } else {
                val textview =
                    Intent(this@Movies, TextLocations::class.java)
                val b = Bundle()
                b.putString("tablename", "tempmovies")
                b.putInt("key", getkey)
                textview.putExtras(b)
                startActivityForResult(textview, 0)
            }
        }

        val save = findViewById(R.id.Save) as Button
        save.setOnClickListener {
            getiStuffMovieType = iStuffMovieType!!.selectedItem as String?
            getiStuffMovieLanguage =
                iStuffMovieLanguage!!.selectedItem as String?
            getiStuffSeatingStyle =
                iStuffSeatingStyle!!.selectedItem as String?

            imovietypeposition = iStuffMovieType!!.selectedItemPosition
            imovielanguageposition =
                iStuffMovieLanguage!!.selectedItemPosition
            iseatingstyleposition =
                iStuffSeatingStyle!!.selectedItemPosition

            val idetails = "movietype=" + getiStuffMovieType +
                " movielanguage=" + getiStuffMovieLanguage +
                "  seatingstyle=" + getiStuffSeatingStyle + " Area=" +
                getiarea + " City=" + geticity + " country=" +
                geticountry

            var getilatitudes = java.lang.Double.parseDouble(getilatitude)
            getilatitudes = getilatitudes * 1E6
            var getilongitudes = java.lang.Double.parseDouble(getilongitude)
            getilongitudes = getilongitudes * 1E6

            if (fromeditquery != null && getkey != 0) {
                myDatabase!!.execSQL(
                    "UPDATE moviesposition set imovietypeposition=" +
                        imovietypeposition +
                        ", imovielanguageposition=" +
                        imovielanguageposition +
                        ", iseatingstyleposition=" +
                        iseatingstyleposition + ", iarea='" +
                        getiarea + "', icity='" + geticity +
                        "', icountry='" + geticountry +
                        "', umovietypeposition=" +
                        umovietypeposition +
                        ", umovielanguageposition=" +
                        umovielanguageposition +
                        ", useatingstyleposition=" +
                        useatingstyleposition + ", uarea='" +
                        getuarea + "', ucity='" + getucity +
                        "', ucountry='" + getucountry +
                        "', ilatitude='" + getilatitude +
                        "', ilongitude='" + getilongitude +
                        "',ulatitude='" + getulatitude +
                        "',ulongitude='" + getulongitude +
                        "' where key=" + getkey + ";"
                )
                myDatabase!!.execSQL(
                    "UPDATE Movies set imovietype='" +
                        getiStuffMovieType + "', imovielanguage='" +
                        getiStuffMovieLanguage + "', iseatingstyle='" +
                        getiStuffSeatingStyle + "', iarea='" + getiarea +
                        "', icity='" + geticity + "', icountry='" +
                        geticountry + "', umovietype='" +
                        getuStuffMovieType + "', umovielanguage='" +
                        getuStuffMovieLanguage + "', useatingstyle='" +
                        getuStuffSeatingStyle + "', uarea='" + getuarea +
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
                        "' where catagory='" + "userMovies" + "';"
                )
            } else {
                try {
                    myDatabase!!.execSQL(
                        "INSERT INTO Movies(imovietype,imovielanguage,iseatingstyle,iarea,icity,icountry, ilatitude, ilongitude, umovietype,umovielanguage,useatingstyle,uarea,ucity, ucountry, ulatitude, ulongitude, queryStatus) VALUES ('" +
                            getiStuffMovieType + "','" +
                            getiStuffMovieLanguage + "','" +
                            getiStuffSeatingStyle + "','" +
                            getiarea + "','" + geticity + "','" +
                            geticountry + "','" + getilatitude +
                            "','" + getilongitude + "','" +
                            getuStuffMovieType + "','" +
                            getuStuffMovieLanguage + "','" +
                            getuStuffSeatingStyle + "','" +
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
                        "INSERT INTO moviesposition (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,  umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry,ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                            imovietypeposition + "," +
                            imovielanguageposition + "," +
                            iseatingstyleposition + ",'" +
                            getiarea + "','" + geticity + "','" +
                            geticountry + "'," +
                            umovietypeposition + "," +
                            umovielanguageposition + "," +
                            useatingstyleposition + ",'" +
                            getuarea + "','" + getucity + "','" +
                            getucountry + "','" + getilatitude +
                            "','" + getilongitude + "','" +
                            getulatitude + "','" + getulongitude +
                            "','" + "Movies" + "', '" + "istuff" +
                            "');"
                    )
                    myDatabase!!.execSQL(
                        "Update mStuffdetails set details='" +
                            idetails + "', latitude='" +
                            getilatitudes + "', longitude='" +
                            getilongitudes + "', location='" +
                            geticountry +
                            "' where catagory='userMovies';"
                    )
                } catch (exce: Exception) {
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS Movies(key INTEGER PRIMARY KEY,imovietype VARCHAR,imovielanguage VARCHAR,iseatingstyle VARCHAR,iarea VARCHAR,icity VARCHAR,icountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR,umovietype VARCHAR,umovielanguage VARCHAR,useatingstyle VARCHAR, uarea VARCHAR,ucity VARCHAR, ucountry VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, querystatus VARCHAR,queryDate DATE);"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER insert_querydate_Movies after INSERT on Movies BEGIN update Movies set queryDate=DATE('NOW') WHERE key=new.key; END;"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TRIGGER delete_querydate_Movies before insert on Movies BEGIN delete from Movies where queryDate<DATE('NOW','-7 day');END;"
                    )
                    myDatabase!!.execSQL(
                        "INSERT INTO Movies(imovietype,imovielanguage,iseatingstyle,iarea,icity,icountry, ilatitude, ilongitude, umovietype,umovielanguage,useatingstyle,uarea,ucity, ucountry, ulatitude, ulongitude, queryStatus) VALUES ('" +
                            getiStuffMovieType + "','" +
                            getiStuffMovieLanguage + "','" +
                            getiStuffSeatingStyle + "','" +
                            getiarea + "','" + geticity + "','" +
                            geticountry + "','" + getilatitude +
                            "','" + getilongitude + "','" +
                            getuStuffMovieType + "','" +
                            getuStuffMovieLanguage + "','" +
                            getuStuffSeatingStyle + "','" +
                            getuarea + "','" + getucity + "','" +
                            getucountry + "','" + getulatitude +
                            "','" + getulongitude + "','" + "true" +
                            "');"
                    )
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS moviesposition" +
                            " (key INTEGER PRIMARY KEY,imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR,ilatitude VARCHAR, ilongitude VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, useatingstyleposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,  ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
                    )
                    myDatabase!!.execSQL(
                        "INSERT INTO moviesposition (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,ilatitude , ilongitude, umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry, ulatitude, ulongitude, category, stufftype) VALUES (" +
                            imovietypeposition + "," +
                            imovielanguageposition + "," +
                            iseatingstyleposition + ",'" +
                            getiarea + "','" + geticity + "','" +
                            geticountry + "'," +
                            umovietypeposition + "," +
                            umovielanguageposition + "," +
                            useatingstyleposition + ",'" +
                            getuarea + "','" + getucity + "','" +
                            getucountry + "','" + getilatitude +
                            "','" + getilongitude + "','" +
                            getulatitude + "','" + getulongitude +
                            "','" + "Movies" + "', '" + "istuff" +
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
                            "' where catagory='userMovies';"
                    )
                }
            }

            myDatabase!!.execSQL("drop table " + "tempmovies" + ";")
            val intent = Intent(this@Movies, FindandInstall::class.java)
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
                val intent1 = Intent(this@Movies, MapResults::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }

            2 -> {
                val intent2 = Intent(this@Movies, FindandInstall::class.java)
                startActivityForResult(intent2, 0)
                finish()
            }

            3 -> {
                val intent3 = Intent(this@Movies, Settings::class.java)
                startActivityForResult(intent3, 0)
                finish()
            }

            4 -> {
                val intent = Intent(this@Movies, FindandInstall::class.java)
                startActivityForResult(intent, 0)
                finish()
            }

            5 -> {
                try {
                    val tempmoviescursor = myDatabase!!.query(
                        "Movies", null,
                        null, null, null, null, null
                    )
                    val listmStuff =
                        Intent(this@Movies, MoviesListQuery::class.java)
                    startActivityForResult(listmStuff, 0)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Movies, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            6 -> {
                try {
                    val tempmoviescursor = myDatabase!!.query(
                        "Movies", null,
                        null, null, null, null, null
                    )
                    val deleteQuery1 =
                        Intent(this@Movies, DeleteMoviesQuery::class.java)
                    startActivityForResult(deleteQuery1, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Movies, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                finish()
            }

            7 -> {
                try {
                    val tempmoviescursor = myDatabase!!.query(
                        "Movies", null,
                        null, null, null, null, null
                    )
                    val viewQuery =
                        Intent(this@Movies, MoviesViewQuery::class.java)
                    startActivityForResult(viewQuery, 0)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@Movies, "Please fill the form",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            8 -> {
                val b1 = Bundle()
                val settheme = Intent(this@Movies, SettingTheme::class.java)
                b1.putString("value1", "Movies")
                b1.putString("class", "6")
                settheme.putExtras(b1)
                startActivityForResult(settheme, 0)
                finish()
                // fall through from Java case 8
                val B1 = Bundle()
                B1.putString("requestcatalog", "Movies")
                val mediaintent =
                    Intent(this@Movies, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }

            9 -> {
                val B1 = Bundle()
                B1.putString("requestcatalog", "Movies")
                val mediaintent =
                    Intent(this@Movies, Uploadmultimedia::class.java)
                mediaintent.putExtras(B1)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
