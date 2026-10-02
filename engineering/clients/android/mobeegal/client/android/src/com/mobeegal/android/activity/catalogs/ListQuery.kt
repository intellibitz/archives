package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: ListQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss                  $: Id of last commit
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
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class ListQuery : ListActivity(), OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var datingId = IntArray(size)
    var iStuffarea = arrayOfNulls<String>(size)
    var iStuffage = arrayOfNulls<String>(size)
    var iStuffsex = arrayOfNulls<String>(size)
    var iStuffheight = arrayOfNulls<String>(size)
    var iStuffweight = arrayOfNulls<String>(size)
    var iStuffcity = arrayOfNulls<String>(size)
    var iStuffcountry = arrayOfNulls<String>(size)
    var uStuffarea = arrayOfNulls<String>(size)
    var uStuffage = arrayOfNulls<String>(size)
    var uStuffsex = arrayOfNulls<String>(size)
    var uStuffheight = arrayOfNulls<String>(size)
    var uStuffweight = arrayOfNulls<String>(size)
    var uStuffcity = arrayOfNulls<String>(size)
    var uStuffcountry = arrayOfNulls<String>(size)
    var count: Int = 0
    var c: Cursor? = null
    var rows: Int = 0
    var results: ArrayList<String> = ArrayList()

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        listAdapter = SpeechListAdapter(this)
        listView.onItemClickListener = this
    }

    private inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context

        init {
            myDatabase = mContext.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val myCols = arrayOf(
                "key", "iage", "isex", "iheight", "iweight",
                "iarea", "icity", "icountry", "uage", "usex", "uheight",
                "uweight", "uarea", "ucity", "ucountry", "queryStatus"
            )
            c = myDatabase!!.query(
                "Dating", myCols, null, null, null,
                null, null
            )
            rows = c!!.count

            val idcolumn = c!!.getColumnIndexOrThrow("key")
            val ageColumn = c!!.getColumnIndexOrThrow("iage")
            val sexColumn = c!!.getColumnIndexOrThrow("isex")
            val heightColumn = c!!.getColumnIndexOrThrow("iheight")
            val weightColumn = c!!.getColumnIndexOrThrow("iweight")
            val areaColumn = c!!.getColumnIndexOrThrow("iarea")
            val cityColumn = c!!.getColumnIndexOrThrow("icity")
            val countryColumn = c!!.getColumnIndexOrThrow("icountry")

            val uagecolumn = c!!.getColumnIndexOrThrow("uage")
            val usexcolumn = c!!.getColumnIndexOrThrow("usex")
            val uheightcolumn = c!!.getColumnIndexOrThrow("uheight")
            val uweightcolumn = c!!.getColumnIndexOrThrow("uweight")
            val uareacolumn = c!!.getColumnIndexOrThrow("uarea")
            val ucitycolumn = c!!.getColumnIndexOrThrow("ucity")
            val ucountrycolumn = c!!.getColumnIndexOrThrow("ucountry")

            if (c != null) {
                count = 0
                if (c!!.isFirst) {
                    do {
                        val getid = c!!.getInt(idcolumn)
                        val getiage = c!!.getString(ageColumn)
                        val getisex = c!!.getString(sexColumn)
                        val getiheight = c!!.getString(heightColumn)
                        val getiweight = c!!.getString(weightColumn)
                        val getiarea = c!!.getString(areaColumn)
                        val geticity = c!!.getString(cityColumn)
                        val geticountry = c!!.getString(countryColumn)
                        val getuage = c!!.getString(uagecolumn)
                        val getusex = c!!.getString(usexcolumn)
                        val getuheight = c!!.getString(uheightcolumn)
                        val getuweight = c!!.getString(uweightcolumn)
                        val getuarea = c!!.getString(uareacolumn)
                        val getucity = c!!.getString(ucitycolumn)
                        val getucountry = c!!.getString(ucountrycolumn)

                        datingId[count] = getid
                        iStuffarea[count] = getiarea
                        iStuffage[count] = getiage
                        iStuffsex[count] = getisex
                        iStuffheight[count] = getiheight
                        iStuffweight[count] = getiweight
                        iStuffcountry[count] = geticountry
                        iStuffcity[count] = geticity

                        uStuffage[count] = getuage
                        uStuffsex[count] = getusex
                        uStuffheight[count] = getuheight
                        uStuffweight[count] = getuweight
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
                    mContext, " Age = " + iStuffage[position] +
                        ", Sex = " + iStuffsex[position] + ", Height = " +
                        iStuffheight[position] + ", Weight = " +
                        iStuffweight[position] + ", Area = " +
                        iStuffarea[position] + ", City = " +
                        iStuffcity[position] + ", Country = " +
                        iStuffcountry[position],
                    " Age = " + uStuffage[position] + ", Sex = " +
                        uStuffsex[position] + ", Height = " +
                        uStuffheight[position] + ", Weight = " +
                        uStuffweight[position] + ", Area = " +
                        uStuffarea[position] + ", City = " +
                        uStuffcity[position] + ", Country = " +
                        uStuffcountry[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffage[position])
                sv.setDialogue(uStuffarea[position])
            }
            return sv
        }
    }

    private inner class SpeechView(context: Context, title: String, words: String) :
        LinearLayout(context) {

        private val mTitle: TextView
        private val mDialogue: TextView

        init {
            this.orientation = VERTICAL
            mTitle = TextView(context)
            mTitle.text = "User Profile : $title"
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )

            mDialogue = TextView(context)
            mDialogue.text = "Partner Profile : $words"
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
        val selectid = parent.getItemAtPosition(position).toString()
        val passkeyvalue = Integer.parseInt(selectid)
        val editDating = Intent(this@ListQuery, Dating::class.java)

        val tempdatingcursor = myDatabase!!.query(
            "datingposition",
            null, "key=" + datingId[passkeyvalue], null, null, null, null
        )
        if (tempdatingcursor != null) {
            if (tempdatingcursor.isFirst) {

                val getiageposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "iageposition"
                        )
                    )
                val getiheightposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "iheightposition"
                        )
                    )
                val getiweightposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "iweightposition"
                        )
                    )
                val getisex = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow("isex")
                    )

                val getuageposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "uageposition"
                        )
                    )
                val getuheightposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "uheightposition"
                        )
                    )
                val getuweightposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "uweightposition"
                        )
                    )
                val getusex = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow("usex")
                    )

                val getiarea = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "iarea"
                        )
                    )
                val geticity = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "icity"
                        )
                    )
                val geticountry = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "icountry"
                        )
                    )
                val getuarea = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "uarea"
                        )
                    )
                val getucity = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ucity"
                        )
                    )
                val getucountry = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ucountry"
                        )
                    )
                val getilatitude = tempdatingcursor.getString(
                    tempdatingcursor.getColumnIndexOrThrow("ilatitude")
                )
                val getilongitude = tempdatingcursor.getString(
                    tempdatingcursor.getColumnIndexOrThrow("ilongitude")
                )
                val getulatitude = tempdatingcursor.getString(
                    tempdatingcursor.getColumnIndexOrThrow("ulatitude")
                )
                val getulongitude = tempdatingcursor.getString(
                    tempdatingcursor.getColumnIndexOrThrow("ulongitude")
                )
                myDatabase!!.execSQL(
                    "CREATE TABLE IF NOT EXISTS tempdating" +
                        " (iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
                )
                myDatabase!!.execSQL(
                    "INSERT INTO tempdating (iageposition, iheightposition, iweightposition, iarea, icity, icountry, uageposition, uheightposition, uweightposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                        getiageposition + "," + getiheightposition +
                        "," + getiweightposition + ",'" + getiarea +
                        "','" + geticity + "','" + geticountry + "'," +
                        getuageposition + "," + getuheightposition +
                        "," + getuweightposition + ",'" + getuarea +
                        "','" + getucity + "','" + getucountry + "','" +
                        getisex + "','" + getusex + "','" +
                        getilatitude + "','" + getilongitude + "','" +
                        getulatitude + "','" + getulongitude + "','" +
                        "Dating" + "', '" + "istuff" + "');"
                )
            }
        }
        val passkeyBundle = Bundle()
        passkeyBundle.putInt("key", datingId[passkeyvalue])
        editDating.putExtras(passkeyBundle)
        startActivityForResult(editDating, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    // Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent = Intent(
                    this@ListQuery,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@ListQuery,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings = Intent(this@ListQuery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
