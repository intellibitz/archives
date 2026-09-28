package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Carsdeletequery.java 14 2008-08-19 06:36:45Z muthu.ramadoss            $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.ListActivity
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
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
import android.widget.Toast
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class Carsdeletequery : ListActivity(), OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var carsId = IntArray(size)
    var imake = arrayOfNulls<String>(size)
    var imodel = arrayOfNulls<String>(size)
    var iyear = arrayOfNulls<String>(size)
    var icolor = arrayOfNulls<String>(size)
    var ifueltype = arrayOfNulls<String>(size)
    var iprice = arrayOfNulls<String>(size)
    var icountry = arrayOfNulls<String>(size)
    var icity = arrayOfNulls<String>(size)
    var iarea = arrayOfNulls<String>(size)

    var umake = arrayOfNulls<String>(size)
    var umodel = arrayOfNulls<String>(size)
    var uyear = arrayOfNulls<String>(size)
    var ucolor = arrayOfNulls<String>(size)
    var ufueltype = arrayOfNulls<String>(size)
    var uprice = arrayOfNulls<String>(size)
    //String[] uStuffcartype = new String[size];
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
                "key", "imake", "imodel", "iyear", "icolor",
                "ifuel_type", "iprice", "icountry", "icity", "iarea",
                "umake", "umodel", "uyear", "ucolor", "ufuel_type",
                "uprice", " ucountry", "ucity", "uarea", "queryStatus"
            )
            c = myDatabase!!.query("Cars", myCols, null, null, null, null, null)
            rows = c!!.count

            val idcolumn = c!!.getColumnIndexOrThrow("key")
            val imakeColumn = c!!.getColumnIndexOrThrow("imake")
            val imodelColumn = c!!.getColumnIndexOrThrow("imodel")
            val iyearColumn = c!!.getColumnIndexOrThrow("iyear")
            val icolorColumn = c!!.getColumnIndexOrThrow("icolor")
            val ifueltypeColumn = c!!.getColumnIndexOrThrow("ifuel_type")
            val ipriceColumn = c!!.getColumnIndexOrThrow("iprice")
            val icountryColumn = c!!.getColumnIndexOrThrow("icountry")
            val icityColumn = c!!.getColumnIndexOrThrow("icity")
            val iareaColumn = c!!.getColumnIndexOrThrow("iarea")

            val umakeColumn = c!!.getColumnIndexOrThrow("umake")
            val umodelColumn = c!!.getColumnIndexOrThrow("umodel")
            val uyearColumn = c!!.getColumnIndexOrThrow("uyear")
            val ucolorColumn = c!!.getColumnIndexOrThrow("ucolor")
            val ufueltypeColumn = c!!.getColumnIndexOrThrow("ufuel_type")
            val upriceColumn = c!!.getColumnIndexOrThrow("uprice")
            val ucountryColumn = c!!.getColumnIndexOrThrow("ucountry")
            val ucityColumn = c!!.getColumnIndexOrThrow("ucity")
            val uareaColumn = c!!.getColumnIndexOrThrow("uarea")
            val uquerystatuscolumn = c!!.getColumnIndexOrThrow("queryStatus")

            if (c != null) {
                count = 0
                if (c!!.isFirst) {
                    do {
                        val getid = c!!.getInt(idcolumn)
                        val getimake = c!!.getString(imakeColumn)
                        val getimodel = c!!.getString(imodelColumn)
                        val getiyear = c!!.getString(iyearColumn)
                        val geticolor = c!!.getString(icolorColumn)
                        val getifueltype = c!!.getString(ifueltypeColumn)
                        val getiprice = c!!.getString(ipriceColumn)
                        val geticountry = c!!.getString(icountryColumn)
                        val geticity = c!!.getString(icityColumn)
                        val getiarea = c!!.getString(iareaColumn)

                        val getumake = c!!.getString(umakeColumn)
                        val getumodel = c!!.getString(umodelColumn)
                        val getuyear = c!!.getString(uyearColumn)
                        val getucolor = c!!.getString(ucolorColumn)
                        val getufueltype = c!!.getString(ufueltypeColumn)
                        val getuprice = c!!.getString(upriceColumn)
                        val getucountry = c!!.getString(ucountryColumn)
                        val getucity = c!!.getString(ucityColumn)
                        val getuarea = c!!.getString(uareaColumn)
                        val getuquerystatus =
                            c!!.getString(uquerystatuscolumn)

                        carsId[count] = getid
                        imake[count] = getimake
                        imodel[count] = getimodel
                        iyear[count] = getiyear
                        icolor[count] = geticolor
                        ifueltype[count] = getifueltype
                        iprice[count] = getiprice
                        icountry[count] = geticountry
                        icity[count] = geticity
                        iarea[count] = getiarea

                        umake[count] = getumake
                        umodel[count] = getumodel
                        uyear[count] = getuyear
                        ucolor[count] = getucolor
                        ufueltype[count] = getufueltype
                        uprice[count] = getuprice
                        ucountry[count] = getucountry
                        ucity[count] = getucity
                        uarea[count] = getuarea
                        queryStatus[count] = getuquerystatus

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
                    mContext,
                    " imake = " + imake[position] + "imodel = " +
                        imodel[position] + "iyear = " +
                        iyear[position] + "icolor = " +
                        icolor[position] + "ifueltype = " +
                        ifueltype[position] + "iprice = " +
                        iprice[position] + " country = " +
                        icountry[position] + " city = " +
                        icity[position] + " area = " + iarea[position],
                    " imake = " + umake[position] + "umodel = " +
                        umodel[position] + "uyear = " +
                        uyear[position] + "ucolor = " +
                        ucolor[position] + "ufueltype = " +
                        ufueltype[position] + "uprice = " +
                        uprice[position] + " country = " +
                        ucountry[position] + " city = " +
                        ucity[position] + " area = " + uarea[position] +
                        " QueryStatus = " + queryStatus[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(imake[position])
                sv.setDialogue(uarea[position])
            }
            return sv
        }

        fun onItemClick(arg0: AdapterView<*>?, arg1: View?, arg2: Int, arg3: Long) {
//          Toast.makeText(mContext, "asdasdsa", Toast.LENGTH_LONG).show();
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
            mTitle.text = "IStuff : $title"
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )

            mDialogue = TextView(context)
            mDialogue.text = "UStuff : $words"
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
        val selectedId = Integer.parseInt(selectid)
        if (rows > 0) {
            myDatabase!!.execSQL(
                "update category set querystatus='" + "true" +
                    "' where status='" + "true" + "';"
            )
        } else {
            myDatabase!!.execSQL(
                "update category set querystatus='" + "false" +
                    "' where status='" + "true" + "';"
            )
        }
        val okButtonListener = OnClickListener { _, _ ->
            try {
                myDatabase = this@Carsdeletequery
                    .openOrCreateDatabase(
                        "Mobeegal",
                        Context.MODE_PRIVATE, null
                    )
                myDatabase!!
                    .delete("Cars", "key=" + carsId[selectedId], null)
                if (selectedId == 0 && carsId[0] != 0) {
                    myDatabase!!.delete("Cars", "key=" + carsId[0], null)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@Carsdeletequery, "Error",
                    Toast.LENGTH_LONG
                ).show()
            }
            val intent =
                Intent(this@Carsdeletequery, Carsdeletequery::class.java)
            startActivity(intent)
            finish()
        }
        val cancelButtonListener = OnClickListener { _, _ ->
            // Do nothing
        }
/*
        AlertDialog.show(this, "Delete", position,
                " Do you want to delete the query\n", "OK", okButtonListener,
                "cancel", cancelButtonListener, false, null);
*/
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
                    this@Carsdeletequery,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@Carsdeletequery,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@Carsdeletequery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
