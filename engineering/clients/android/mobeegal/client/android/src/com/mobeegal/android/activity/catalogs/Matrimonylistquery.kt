package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Matrimonylistquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss         $: Id of last commit
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

class Matrimonylistquery : ListActivity(), OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var matrimonyId = IntArray(size)
    var ireligion = arrayOfNulls<String>(size)
    var icaste = arrayOfNulls<String>(size)
    var iage = arrayOfNulls<String>(size)
    var isex = arrayOfNulls<String>(size)
    var iheight = arrayOfNulls<String>(size)
    var iweight = arrayOfNulls<String>(size)
    var icolor = arrayOfNulls<String>(size)
    var icountry = arrayOfNulls<String>(size)
    var icity = arrayOfNulls<String>(size)
    var iarea = arrayOfNulls<String>(size)

    var ureligion = arrayOfNulls<String>(size)
    var ucaste = arrayOfNulls<String>(size)
    var uage = arrayOfNulls<String>(size)
    var usex = arrayOfNulls<String>(size)
    var uheight = arrayOfNulls<String>(size)
    var uweight = arrayOfNulls<String>(size)
    var ucolor = arrayOfNulls<String>(size)
    var ucountry = arrayOfNulls<String>(size)
    var ucity = arrayOfNulls<String>(size)
    var uarea = arrayOfNulls<String>(size)
    var queryStatus = arrayOfNulls<String>(size)

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
                "key", "ireligion", "icaste", "iage", "isex",
                "iheight", "iweight", "icolor", "iarea", "icity ",
                "icountry ", "ureligion", "ucaste", "uage", "usex ",
                "uheight", "uweight ", "ucolor", "uarea", "ucity",
                "ucountry", "queryStatus"
            )
            c = myDatabase!!.query(
                "Matrimony", myCols, null, null,
                null, null, null
            )
            rows = c!!.count

            val idcolumn = c!!.getColumnIndexOrThrow("key")
            val religionColumn = c!!.getColumnIndexOrThrow("ireligion")
            val casteColumn = c!!.getColumnIndexOrThrow("icaste")
            val ageColumn = c!!.getColumnIndexOrThrow("iage")
            val sexColumn = c!!.getColumnIndexOrThrow("isex")
            val heightColumn = c!!.getColumnIndexOrThrow("iheight")
            val weightColumn = c!!.getColumnIndexOrThrow("iweight")
            val colorColumn = c!!.getColumnIndexOrThrow("icolor")
            val countryColumn = c!!.getColumnIndexOrThrow("icountry")
            val cityColumn = c!!.getColumnIndexOrThrow("icity")
            val areaColumn = c!!.getColumnIndexOrThrow("iarea")

            val ureligioncolumn = c!!.getColumnIndexOrThrow("ureligion")
            val ucastecolumn = c!!.getColumnIndexOrThrow("ucaste")
            val uagecolumn = c!!.getColumnIndexOrThrow("uage")
            val usexcolumn = c!!.getColumnIndexOrThrow("usex")
            val uheightcolumn = c!!.getColumnIndexOrThrow("uheight")
            val uweightcolumn = c!!.getColumnIndexOrThrow("uweight")
            val ucolorcolumn = c!!.getColumnIndexOrThrow("ucolor")
            val ucountrycolumn = c!!.getColumnIndexOrThrow("ucountry")
            val ucitycolumn = c!!.getColumnIndexOrThrow("ucity")
            val uareacolumn = c!!.getColumnIndexOrThrow("uarea")
            val uquerystatuscolumn = c!!.getColumnIndexOrThrow("queryStatus")

            if (c != null) {
                count = 0
                if (c!!.isFirst) {
                    do {
                        val getid = c!!.getInt(idcolumn)
                        val getireligion = c!!.getString(religionColumn)
                        val geticaste = c!!.getString(casteColumn)
                        val getiage = c!!.getString(ageColumn)
                        val getisex = c!!.getString(sexColumn)
                        val getiheight = c!!.getString(heightColumn)
                        val getiweight = c!!.getString(weightColumn)
                        val geticolor = c!!.getString(colorColumn)
                        val geticountry = c!!.getString(countryColumn)
                        val geticity = c!!.getString(cityColumn)
                        val getiarea = c!!.getString(areaColumn)

                        val getureligion = c!!.getString(ureligioncolumn)
                        val getucaste = c!!.getString(ucastecolumn)
                        val getuage = c!!.getString(uagecolumn)
                        val getusex = c!!.getString(usexcolumn)
                        val getuheight = c!!.getString(uheightcolumn)
                        val getuweight = c!!.getString(uweightcolumn)
                        val getucolor = c!!.getString(ucolorcolumn)
                        val getucountry = c!!.getString(ucountrycolumn)
                        val getucity = c!!.getString(ucitycolumn)
                        val getuarea = c!!.getString(uareacolumn)
                        val getustatus = c!!.getString(uquerystatuscolumn)


                        matrimonyId[count] = getid
                        ireligion[count] = getireligion
                        icaste[count] = geticaste
                        iage[count] = getiage
                        isex[count] = getisex
                        iheight[count] = getiheight
                        iweight[count] = getiweight
                        icolor[count] = geticolor
                        icountry[count] = geticountry
                        icity[count] = geticity
                        iarea[count] = getiarea

                        ureligion[count] = getureligion
                        ucaste[count] = getucaste
                        uage[count] = getuage
                        usex[count] = getusex
                        uheight[count] = getuheight
                        uweight[count] = getuweight
                        ucolor[count] = getucolor
                        ucountry[count] = getucountry
                        ucity[count] = getucity
                        uarea[count] = getuarea
                        queryStatus[count] = getustatus

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
                    mContext, "Religion = " +
                        ireligion[position] + " Caste = " + icaste[position] +
                        " Age = " + iage[position] + " Sex = " +
                        isex[position] + " Height = " + iheight[position] +
                        " Weight = " + iweight[position] + " Color = " +
                        icolor[position] + " Area = " + iarea[position] +
                        " City = " + icity[position] + " Country = " +
                        icountry[position],
                    "Religion = " + ureligion[position] + "Caste = " +
                        ucaste[position] + "Age Range = " +
                        uage[position] + "Sex = " + usex[position] +
                        " Height Range = " + uheight[position] +
                        " Weight Range= " + uweight[position] +
                        " Color = " + ucolor[position] + " Area = " +
                        uarea[position] + " City = " + ucity[position] +
                        " Country = " + ucountry[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iage[position])
                sv.setDialogue(uarea[position])
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
        val editDating =
            Intent(this@Matrimonylistquery, Matrimony::class.java)
        val passkeyBundle = Bundle()
        passkeyBundle.putInt("key", matrimonyId[passkeyvalue])
        editDating.putExtras(passkeyBundle)
        val tempdatingcursor = myDatabase!!.query(
            "matrimonyposition",
            null, "key=" + matrimonyId[passkeyvalue], null, null, null,
            null
        )
        if (tempdatingcursor != null) {
            if (tempdatingcursor.isFirst) {
                val getireligionposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ireligionposition"
                        )
                    )
                val geticasteposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "icasteposition"
                        )
                    )
                val geticolorposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "icolorposition"
                        )
                    )
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
                val getureligionposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ureligionposition"
                        )
                    )
                val getucasteposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ucasteposition"
                        )
                    )
                val getucolorposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ucolorposition"
                        )
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
                val getcategory = tempdatingcursor
                    .getString(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "category"
                        )
                    )
                val getstufftype = tempdatingcursor.getString(
                    tempdatingcursor.getColumnIndexOrThrow("stufftype")
                )
                myDatabase!!.execSQL(
                    "CREATE TABLE IF NOT EXISTS tempmatrimony" +
                        " (ireligionposition NUMERIC, icasteposition NUMERIC, iageposition NUMERIC, iheightposition NUMERIC, iweightposition NUMERIC, icolorposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ureligionposition NUMERIC, ucasteposition NUMERIC, uageposition NUMERIC, uheightposition NUMERIC, uweightposition NUMERIC, ucolorposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, isex VARCHAR, usex VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
                )
                myDatabase!!.execSQL(
                    "INSERT INTO tempmatrimony (ireligionposition, icasteposition, iageposition, iheightposition, iweightposition, icolorposition, iarea, icity, icountry, ureligionposition, ucasteposition, uageposition, uheightposition, uweightposition, ucolorposition, uarea, ucity, ucountry, isex, usex, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                        getireligionposition + "," + geticasteposition +
                        "," + getiageposition + "," +
                        getiheightposition + "," + getiweightposition +
                        "," + geticolorposition + ",'" + getiarea +
                        "','" + geticity + "','" + geticountry + "'," +
                        getureligionposition + "," + getucasteposition +
                        "," + getuageposition + "," +
                        getuheightposition + "," + getuweightposition +
                        "," + getucolorposition + ",'" + getuarea +
                        "','" + getucity + "','" + getucountry + "','" +
                        getisex + "','" + getusex + "','" +
                        getilatitude + "','" + getilongitude + "','" +
                        getulatitude + "','" + getulongitude + "','" +
                        "Matrimony" + "', '" + "istuff" + "');"
                )

            }
        }
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
                    this@Matrimonylistquery,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@Matrimonylistquery,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@Matrimonylistquery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
