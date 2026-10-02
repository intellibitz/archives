package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: DeleteQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
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
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class DeleteQuery : ListActivity(), AdapterView.OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var size = 20
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
    var queryStatus = arrayOfNulls<String>(size)
    var count = 0
    var rows = 0
    var c: Cursor? = null
    var results = ArrayList<String>()

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
                "key", "iage", "isex", "iheight", "iweight",
                "iarea", "icity", "icountry", "uage", "usex", "uheight",
                "uweight", "uarea", "ucity", "ucountry", "queryStatus"
            )
            c = myDatabase!!.query("Dating", myCols, null, null, null, null, null)
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
            val querystatuscolumn = c!!.getColumnIndexOrThrow("queryStatus")

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
                        val getquerystatus = c!!.getString(querystatuscolumn)

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
                        queryStatus[count] = getquerystatus
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
                            "Sex = " + iStuffsex[position] + ", Height = " +
                            iStuffheight[position] + " Weight = " +
                            iStuffweight[position] + " Area = " +
                            iStuffarea[position] + " City = " +
                            iStuffcity[position] + " Country = " +
                            iStuffcountry[position],
                    " Age Range = " + uStuffage[position] + "Sex = " +
                            uStuffsex[position] + ", Height Range = " +
                            uStuffheight[position] + " Weight Range= " +
                            uStuffweight[position] + " Area = " +
                            uStuffarea[position] + " City = " +
                            uStuffcity[position] + " Country = " +
                            uStuffcountry[position] + " QueryStatus = " +
                            queryStatus[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffage[position])
                sv.setDialogue(uStuffarea[position])
            }
            return sv
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

    override fun onItemClick(parent: AdapterView<*>, v: View, position: Int, id: Long) {
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
        val okButtonListener = object : OnClickListener {
            override fun onClick(arg0: DialogInterface, arg1: Int) {
                try {
                    myDatabase =
                        this@DeleteQuery.openOrCreateDatabase(
                            "Mobeegal",
                            Context.MODE_PRIVATE, null
                        )
                    myDatabase!!.delete("Dating", "key=" + datingId[selectedId], null)
                    if (selectedId == 0 && datingId[0] != 0) {
                        myDatabase!!.delete("Dating", "key=" + datingId[0], null)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@DeleteQuery, "Error", Toast.LENGTH_LONG)
                        .show()
                }
                val intent = Intent(this@DeleteQuery, DeleteQuery::class.java)
                startActivity(intent)
                finish()
            }
        }
        val cancelButtonListener = object : OnClickListener {
            // @Override
            override fun onClick(arg0: DialogInterface, arg1: Int) {
                // Do nothing
            }
        }
//        AlertDialog.show(this, "Delete", position, " Do you want to delete the query\n", "OK", okButtonListener, "cancel", cancelButtonListener, false, null);
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
                    this@DeleteQuery,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@DeleteQuery,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings = Intent(this@DeleteQuery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
