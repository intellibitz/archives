package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Jewelrylistquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
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

class Jewelrylistquery : ListActivity(), OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var jewelryId = IntArray(size)
    var iStuffitemtype = arrayOfNulls<String>(size)
    var iStuffWeight = arrayOfNulls<String>(size)
    var iStuffCountry = arrayOfNulls<String>(size)
    var iStuffCity = arrayOfNulls<String>(size)
    var iStuffArea = arrayOfNulls<String>(size)

    var uStuffitemtype = arrayOfNulls<String>(size)
    var uStuffWeightRange = arrayOfNulls<String>(size)
    var uStuffCountry = arrayOfNulls<String>(size)
    var uStuffCity = arrayOfNulls<String>(size)
    var uStuffArea = arrayOfNulls<String>(size)
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
            c = myDatabase!!.query("Jewelry", null, null, null, null, null, null)
            rows = c!!.count

            val idcolumn = c!!.getColumnIndexOrThrow("key")
            val itemColumn = c!!.getColumnIndexOrThrow("ijewelry")
            val weightColumn = c!!.getColumnIndexOrThrow("iweight")
            val countryColumn = c!!.getColumnIndexOrThrow("icountry")
            val cityColumn = c!!.getColumnIndexOrThrow("icity")
            val areaColumn = c!!.getColumnIndexOrThrow("iarea")

            val uitemcolumn = c!!.getColumnIndexOrThrow("ujewelry")
            val uweightcolumn = c!!.getColumnIndexOrThrow("uweight")
            val ucountrycolumn = c!!.getColumnIndexOrThrow("ucountry")
            val ucitycolumn = c!!.getColumnIndexOrThrow("ucity")
            val uareacolumn = c!!.getColumnIndexOrThrow("uarea")
            val uquerystatuscolumn = c!!.getColumnIndexOrThrow("queryStatus")

            if (c != null) {
                count = 0
                if (c!!.isFirst) {
                    do {
                        val getid = c!!.getInt(idcolumn)
                        val getitem = c!!.getString(itemColumn)
                        val getiweight = c!!.getString(weightColumn)
                        val geticountry = c!!.getString(countryColumn)
                        val geticity = c!!.getString(cityColumn)
                        val getiarea = c!!.getString(areaColumn)
                        val getuitem = c!!.getString(uitemcolumn)
                        val getuweight = c!!.getString(uweightcolumn)
                        val getucountry = c!!.getString(ucountrycolumn)
                        val getucity = c!!.getString(ucitycolumn)
                        val getuarea = c!!.getString(uareacolumn)
                        val getquerystatus = c!!.getString(uquerystatuscolumn)

                        jewelryId[count] = getid
                        iStuffitemtype[count] = getitem
                        iStuffWeight[count] = getiweight
                        iStuffCountry[count] = geticountry
                        iStuffCity[count] = geticity
                        iStuffArea[count] = getiarea

                        uStuffitemtype[count] = getuitem
                        uStuffWeightRange[count] = getuweight
                        uStuffCountry[count] = getucountry
                        uStuffCity[count] = getucity
                        uStuffArea[count] = getuarea
                        queryStatus[count] = getquerystatus

                        count++
                    } while (c!!.moveToNext())
                }
            }
//                if (c1 != null) {
//                    count = 0;
//                    if (c1.isFirst()) {
//                        do {
//
//
//                            //results.add("IStuff :" + iStuffarea[count] + "UStuff :" + uStuffarea[count]);
//                            count++;
//                        } while (c1.moveToNext());
//                    }
//                }
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
                    mContext, " ItemType = " +
                        iStuffitemtype[position] + " Weight = " +
                        iStuffWeight[position] + "Area = " +
                        iStuffArea[position] + " City = " +
                        iStuffCity[position] + "Country= " +
                        iStuffCountry[position],
                    " ItemType = " + uStuffitemtype[position] +
                        " Weight = " + uStuffWeightRange[position] +
                        " Area= " + uStuffArea[position] + " City = " +
                        uStuffCity[position] + " Country = " +
                        uStuffCountry[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffitemtype[position])
                /*  sv.setDialogue(uStuffArea[position]);*/
            }
            return sv
        }

        fun onItemClick(arg0: AdapterView<*>?, arg1: View?, arg2: Int, arg3: Long) {
        }
    }

    private inner class SpeechView(
        context: Context,
        title: String,
        words: String
    ) : LinearLayout(context) {

        private val mTitle: TextView
        private val mDialogue: TextView

        init {
            this.orientation = VERTICAL
            mTitle = TextView(context)
            mTitle.text = "Merchant Detail : $title"
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )

            mDialogue = TextView(context)
            mDialogue.text = "Customer Detail : $words"
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

    override fun onItemClick(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
        val selectid = parent.getItemAtPosition(position).toString()
        val passkeyvalue = Integer.parseInt(selectid)
        val editJewelry = Intent(this@Jewelrylistquery, Jewelry::class.java)
        val passkeyBundle = Bundle()
        passkeyBundle.putInt("key", jewelryId[passkeyvalue])
        editJewelry.putExtras(passkeyBundle)
        val tempdatingcursor = myDatabase!!.query(
            "JewelryPosition", null,
            "key=" + jewelryId[passkeyvalue], null, null, null, null
        )
        if (tempdatingcursor != null) {
            if (tempdatingcursor.isFirst) {
                val getijewelryposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ijewelryposition"
                        )
                    )
                val getigenderposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "igenderposition"
                        )
                    )
                val getistoneposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "istoneposition"
                        )
                    )
                val getimetalposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "imetalposition"
                        )
                    )
                val getiweightposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "iweightposition"
                        )
                    )

                val getujewelryposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ujewelryposition"
                        )
                    )
                val getugenderposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ugenderposition"
                        )
                    )
                val getustoneposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "ustoneposition"
                        )
                    )
                val getumetalposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "umetalposition"
                        )
                    )
                val getuweightposition = tempdatingcursor
                    .getInt(
                        tempdatingcursor.getColumnIndexOrThrow(
                            "uweightposition"
                        )
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
                    "CREATE TABLE IF NOT EXISTS tempjewelry" +
                        "(ijewelryposition NUMERIC, igenderposition NUMERIC, istoneposition NUMERIC, imetalposition NUMERIC, iweightposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, ujewelryposition NUMERIC, ugenderposition NUMERIC, ustoneposition NUMERIC, umetalposition NUMERIC, uweightposition NUMERIC, uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCHAR, stufftype VARCHAR);"
                )
                myDatabase!!.execSQL(
                    "INSERT INTO tempjewelry (ijewelryposition, igenderposition, istoneposition, imetalposition, iweightposition, iarea, icity,icountry, ujewelryposition, ugenderposition, ustoneposition, umetalposition, uweightposition, uarea, ucity, ucountry, ilatitude, ilongitude, ulatitude, ulongitude, category, stufftype) VALUES (" +
                        getijewelryposition + "," + getigenderposition +
                        "," + getistoneposition + "," +
                        getimetalposition + "," + getiweightposition +
                        ",'" + getiarea + "','" + geticity + "','" +
                        geticountry + "'," + getujewelryposition + "," +
                        getugenderposition + "," + getustoneposition +
                        "," + getumetalposition + "," +
                        getuweightposition + ",'" + getuarea + "','" +
                        getucity + "','" + getucountry + "','" +
                        getilatitude + "','" + getilongitude + "','" +
                        getulatitude + "','" + getulongitude + "','" +
                        "Jewelry" + "','" + "istuff" + "');"
                )
            }
        }
        startActivityForResult(editJewelry, 0)
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
                    this@Jewelrylistquery,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@Jewelrylistquery,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@Jewelrylistquery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
