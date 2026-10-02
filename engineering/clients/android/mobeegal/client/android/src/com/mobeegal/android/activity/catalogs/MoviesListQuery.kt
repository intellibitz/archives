package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: MoviesListQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss            $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class MoviesListQuery : ListActivity(), AdapterView.OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var c: Cursor? = null
    var rows = 0
    var count = 0
    var size = 15
    var results = ArrayList<String>()
    var iStuffMovieType = arrayOfNulls<String>(size)
    var iStuffMovieLanguage = arrayOfNulls<String>(size)
    var iStuffSeatingStyle = arrayOfNulls<String>(size)
    var iStuffcountry = arrayOfNulls<String>(size)
    var iStuffcity = arrayOfNulls<String>(size)
    var iStuffarea = arrayOfNulls<String>(size)
    var uStuffMovieType = arrayOfNulls<String>(size)
    var uStuffMovieLanguage = arrayOfNulls<String>(size)
    var uStuffSeatingStyle = arrayOfNulls<String>(size)
    var uStuffcountry = arrayOfNulls<String>(size)
    var uStuffcity = arrayOfNulls<String>(size)
    var uStuffarea = arrayOfNulls<String>(size)
    var movieId = IntArray(size)

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        listAdapter = SpeechListAdapter(this)
        listView.setOnItemClickListener(this)
    }

    private inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context

        init {
            myDatabase = mContext.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val myCols = arrayOf(
                "key", "imovietype", "imovielanguage",
                "iseatingstyle", "icountry", "icity", "iarea", "umovietype",
                "umovielanguage", "useatingstyle", "ucountry", "ucity",
                "uarea", "queryStatus"
            )
            c = myDatabase!!.query("Movies", myCols, null, null, null, null, null)
            rows = c!!.count

            val idcolumn = c!!.getColumnIndexOrThrow("key")
            val imovietypeColumn = c!!.getColumnIndexOrThrow("imovietype")
            val imovielanguageColumn = c!!.getColumnIndexOrThrow("imovielanguage")
            val imiscColumn = c!!.getColumnIndexOrThrow("iseatingstyle")
            val icountryColumn = c!!.getColumnIndexOrThrow("icountry")
            val icityColumn = c!!.getColumnIndexOrThrow("icity")
            val iareaColumn = c!!.getColumnIndexOrThrow("iarea")

            val umovietypeColumn = c!!.getColumnIndexOrThrow("umovietype")
            val umovielanguageColumn = c!!.getColumnIndexOrThrow("umovielanguage")
            val umiscColumn = c!!.getColumnIndexOrThrow("useatingstyle")
            val ucountryColumn = c!!.getColumnIndexOrThrow("ucountry")
            val ucityColumn = c!!.getColumnIndexOrThrow("ucity")
            val uareaColumn = c!!.getColumnIndexOrThrow("uarea")

            if (c != null) {
                count = 0
                if (c!!.isFirst) {
                    do {
                        val getid = c!!.getInt(idcolumn)
                        val getimovietype = c!!.getString(imovietypeColumn)
                        val getimovielanguage = c!!.getString(imovielanguageColumn)
                        val getiseatingstyle = c!!.getString(imiscColumn)
                        val geticountry = c!!.getString(icountryColumn)
                        val geticity = c!!.getString(icityColumn)
                        val getiarea = c!!.getString(iareaColumn)

                        val getumovietype = c!!.getString(umovietypeColumn)
                        val getumovielanguage = c!!.getString(umovielanguageColumn)
                        val getuseatingstyle = c!!.getString(umiscColumn)
                        val getucountry = c!!.getString(ucountryColumn)
                        val getucity = c!!.getString(ucityColumn)
                        val getuarea = c!!.getString(uareaColumn)

                        movieId[count] = getid
                        iStuffMovieType[count] = getimovietype
                        iStuffMovieLanguage[count] = getimovielanguage
                        iStuffSeatingStyle[count] = getiseatingstyle
                        iStuffarea[count] = getiarea
                        iStuffcountry[count] = geticountry
                        iStuffcity[count] = geticity

                        uStuffMovieType[count] = getumovietype
                        uStuffMovieLanguage[count] = getumovielanguage
                        uStuffSeatingStyle[count] = getuseatingstyle
                        uStuffarea[count] = getuarea
                        uStuffcity[count] = getucity
                        uStuffcountry[count] = getucountry

                        count++
                    } while (c!!.moveToNext())
                }
            }
        }

        override fun getCount(): Int {
            return rows
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val sv: SpeechView
            if (convertView == null) {
                sv = SpeechView(
                    mContext, " MovieType = " +
                            iStuffMovieType[position] + ", MovieLanguage = " +
                            iStuffMovieLanguage[position] + ", SeatingStyle = " +
                            iStuffSeatingStyle[position] + ", Area = " +
                            iStuffarea[position] + ", City = " +
                            iStuffcity[position] + ", Country = " +
                            iStuffcountry[position],
                    " MovieType = " + uStuffMovieType[position] +
                            ", MovieLanguage = " +
                            uStuffMovieLanguage[position] +
                            ", SeatingStyle = " +
                            uStuffSeatingStyle[position] + ", Area = " +
                            uStuffarea[position] + ", City = " +
                            uStuffcity[position] + ", Country = " +
                            uStuffcountry[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffMovieType[position])
                sv.setDialogue(uStuffarea[position])
            }
            return sv
        }

        fun onItemClick(arg0: AdapterView<*>?, arg1: View?, arg2: Int, arg3: Long) {
        }
    }

    private inner class SpeechView(
        context: Context, title: String?, words: String?
    ) : LinearLayout(context) {

        private val mTitle: TextView
        private val mDialogue: TextView

        init {
            this.orientation = VERTICAL
            mTitle = TextView(context)
            mTitle.text = "Owner Detail : $title"
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )

            mDialogue = TextView(context)
            mDialogue.text = "Public Detail : $words"
            addView(
                mDialogue, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )
        }

        fun setTitle(title: String?) {
            mTitle.text = title
        }

        fun setDialogue(words: String?) {
            mDialogue.text = words
        }
    }

    override fun onItemClick(parent: AdapterView<*>, v: View, position: Int, id: Long) {
        /* Intent i = new Intent(MoviesListQuery.this, Movies.class);
        startActivity(i);*/
        val selectid = parent.getItemAtPosition(position).toString()
        val passkeyvalue = Integer.parseInt(selectid)
        val editMovies = Intent(this@MoviesListQuery, Movies::class.java)

        val tempmoviescursor = myDatabase!!.query(
            "moviesposition", null,
            "key=" + movieId[passkeyvalue], null, null, null, null
        )
        if (tempmoviescursor != null) {
            if (tempmoviescursor.isFirst) {
                val getiMovieTypeposition = tempmoviescursor.getInt(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "imovietypeposition"
                    )
                )
                val getiMovieLanguageposition = tempmoviescursor.getInt(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "imovielanguageposition"
                    )
                )
                val getiSeatingStyleposition = tempmoviescursor.getInt(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "iseatingstyleposition"
                    )
                )

                val getuMovieTypeposition = tempmoviescursor.getInt(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "umovietypeposition"
                    )
                )
                val getuMovieLanguageposition = tempmoviescursor.getInt(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "umovielanguageposition"
                    )
                )
                val getuSeatingStyleposition = tempmoviescursor.getInt(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "useatingstyleposition"
                    )
                )

                val getiarea = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "iarea"
                    )
                )
                val geticity = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "icity"
                    )
                )
                val geticountry = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "icountry"
                    )
                )

                val getuarea = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "uarea"
                    )
                )
                val getucity = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "ucity"
                    )
                )
                val getucountry = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow(
                        "ucountry"
                    )
                )

                val getilatitude = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow("ilatitude")
                )
                val getilongitude = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow("ilongitude")
                )

                val getulatitude = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow("ulatitude")
                )
                val getulongitude = tempmoviescursor.getString(
                    tempmoviescursor.getColumnIndexOrThrow("ulongitude")
                )
                myDatabase!!.execSQL(
                    "CREATE TABLE IF NOT EXISTS tempmovies" +
                            " (imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, umovielanguageposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,  ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
                )
                myDatabase!!.execSQL(
                    "INSERT INTO tempmovies (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry, ilatitude, ilongitude , umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry, ulatitude, ulongitude, category, stufftype) VALUES (" +
                            getiMovieTypeposition + "," +
                            getiMovieLanguageposition + "," +
                            getiSeatingStyleposition + ",'" + getiarea +
                            "','" + geticity + "','" + geticountry + "'," +
                            getuMovieTypeposition + "," +
                            getuMovieLanguageposition + "," +
                            getuSeatingStyleposition + ",'" + getuarea +
                            "','" + getucity + "','" + getucountry + "','" +
                            getilatitude + "','" + getilongitude + "','" +
                            getulatitude + "','" + getulongitude + "','" +
                            "Movies" + "', '" + "istuff" + "');"
                )

                // myDatabase.execSQL("CREATE TABLE IF NOT EXISTS tempmovies" + " (imovietypeposition NUMERIC, imovielanguageposition NUMERIC, iseatingstyleposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, umovietypeposition NUMERIC, umovielanguageposition NUMERIC, useatingstyleposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR,ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);");
                // myDatabase.execSQL("INSERT INTO tempmovies (imovietypeposition, imovielanguageposition, iseatingstyleposition, iarea, icity, icountry,umovietypeposition, umovielanguageposition, useatingstyleposition, uarea, ucity, ucountry,ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" + getiMovieTypeposition + "," + getiMovieLanguageposition + "," + getiSeatingStyleposition + ",'" + "" + "','" + "" + "','" + "" + "','" +"" +"','" +"" + "'," + getuMovieTypeposition + "," + getuMovieLanguageposition + "," + getuSeatingStyleposition + ",'" + "" + "','" + "" + "','" + "" + "','" + "" + "','" + ""  + "','" + "Movies" + "', '" + "istuff" + "');");
            }
        }
        val passkeyBundle = Bundle()
        passkeyBundle.putInt("key", movieId[passkeyvalue])
        editMovies.putExtras(passkeyBundle)
        startActivityForResult(editMovies, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    // Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent =
                    Intent(this@MoviesListQuery, MapResults::class.java)
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 =
                    Intent(this@MoviesListQuery, FindandInstall::class.java)
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@MoviesListQuery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
