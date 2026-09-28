package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: DeleteHome.java 14 2008-08-19 06:36:45Z muthu.ramadoss                 $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.ListActivity
import android.content.Context
import android.content.DialogInterface
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

class DeleteHome : ListActivity(), AdapterView.OnItemClickListener {
    var myDatabase: SQLiteDatabase? = null
    var c: Cursor? = null
    var rows = 0
    var count = 0
    var size = 15
    var iStuffRentalType = arrayOfNulls<String>(size)
    var iStuffMisc = arrayOfNulls<String>(size)
    var iStuffRate = arrayOfNulls<String>(size)
    var iStuffCountry = arrayOfNulls<String>(size)
    var iStuffCity = arrayOfNulls<String>(size)
    var iStuffArea = arrayOfNulls<String>(size)
    var uStuffRentalType = arrayOfNulls<String>(size)
    var uStuffMisc = arrayOfNulls<String>(size)
    var uStuffRate = arrayOfNulls<String>(size)
    var uStuffCountry = arrayOfNulls<String>(size)
    var uStuffCity = arrayOfNulls<String>(size)
    var uStuffArea = arrayOfNulls<String>(size)
    var HomeId = IntArray(size)
    var selectid: String? = null

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        listAdapter = SpeechListAdapter(this)
        listView.onItemClickListener = this
    }

    inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context

        init {
            try {
                myDatabase = mContext.openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                //myDatabase = mContext.openDatabase("Mobeegal", null);
                val myCols = arrayOf(
                    "key", "iStuffRentalType", "iStuffMisc",
                    "iStuffRate", "iStuffCountry", "iStuffCity",
                    "iStuffArea", "uStuffRentalType", "uStuffMisc",
                    "uStuffRate", "uStuffCountry", "uStuffCity",
                    "uStuffArea"
                )
                c = myDatabase!!.query("Home", myCols, null, null, null, null, null)
                rows = c!!.count

                val idcolumn = c!!.getColumnIndexOrThrow("key")
                val irentaltypeColumn =
                    c!!.getColumnIndexOrThrow("iStuffRentalType")
                val imiscColumn = c!!.getColumnIndexOrThrow("iStuffMisc")
                val irateColumn = c!!.getColumnIndexOrThrow("iStuffRate")
                val icountryColumn = c!!.getColumnIndexOrThrow("iStuffCountry")
                val icityColumn = c!!.getColumnIndexOrThrow("iStuffCity")
                val iareaColumn = c!!.getColumnIndexOrThrow("iStuffArea")

                val urentaltypeColumn =
                    c!!.getColumnIndexOrThrow("uStuffReantalType")
                val umiscColumn = c!!.getColumnIndexOrThrow("uStuffMisc")
                val urateColumn = c!!.getColumnIndexOrThrow("uStuffRate")
                val ucountryColumn = c!!.getColumnIndexOrThrow("uStuffCountry")
                val ucityColumn = c!!.getColumnIndexOrThrow("uStuffCity")
                val uareaColumn = c!!.getColumnIndexOrThrow("uStuffArea")

                if (c != null) {
                    // count = 0;
                    if (c!!.isFirst) {
                        do {
                            val getid = c!!.getInt(idcolumn)
                            val getirentaltype =
                                c!!.getString(irentaltypeColumn)
                            val getimisc = c!!.getString(imiscColumn)
                            val getirate = c!!.getString(irateColumn)
                            val geticountry = c!!.getString(icountryColumn)
                            val geticity = c!!.getString(icityColumn)
                            val getiarea = c!!.getString(iareaColumn)

                            val geturentaltype =
                                c!!.getString(urentaltypeColumn)
                            val getumisc = c!!.getString(umiscColumn)
                            val geturate = c!!.getString(urateColumn)
                            val getucountry = c!!.getString(ucountryColumn)
                            val getucity = c!!.getString(ucityColumn)
                            val getuarea = c!!.getString(uareaColumn)

                            HomeId[count] = getid
                            iStuffRentalType[count] = getirentaltype
                            iStuffMisc[count] = getimisc
                            iStuffRate[count] = getirate
                            iStuffCountry[count] = geticountry
                            iStuffCity[count] = geticity
                            iStuffArea[count] = getiarea

                            uStuffRentalType[count] = geturentaltype
                            uStuffMisc[count] = getumisc
                            uStuffRate[count] = geturate
                            uStuffCountry[count] = getucountry
                            uStuffCity[count] = getucity
                            uStuffArea[count] = getuarea

                            count++
                        } while (c!!.moveToNext())
                    }
                }
            } catch (ex: Exception) {
                //Toast.makeText(DeleteHome.this, ""+ex, Toast.LENGTH_LONG).show();
                //Logger.getLogger(ListClick.class.getName()).log(Level.SEVERE, null, ex);
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
                    mContext, " RentalType = " +
                            iStuffRentalType[position] + " Misc = " +
                            iStuffMisc[position] + "Rate = " +
                            iStuffRate[position] + " Country = " +
                            iStuffCountry[position] + " Area = " +
                            iStuffArea[position] + " City = " +
                            iStuffCity[position],
                    " uRentalType = " + uStuffRentalType[position] +
                            " uMisc = " + uStuffMisc[position] +
                            "uRate = " + uStuffRate[position] +
                            " uCountry= " + uStuffCountry[position] +
                            " Area = " + uStuffArea[position] + " City = " +
                            uStuffCity[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffRentalType[position])
                sv.setDialogue(uStuffArea[position])
            }
            return sv
        }
    }

    inner class SpeechView(
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
        selectid = parent.getItemAtPosition(position).toString()
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
        //Toast.makeText(DeleteHome.this, selectedId, Toast.LENGTH_LONG).show();
        val okButtonListener = object : DialogInterface.OnClickListener {

            override fun onClick(arg0: DialogInterface, arg1: Int) {
                try {
                    myDatabase =
                        this@DeleteHome.openOrCreateDatabase(
                            "Mobeegal",
                            Context.MODE_PRIVATE, null
                        )
                    myDatabase!!.delete("Home", "key=$selectid", null)
                    if (selectedId == 0 && HomeId[0] != 0) {
                        myDatabase!!.delete("Home", "key=" + HomeId[0], null)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@DeleteHome, "Error", Toast.LENGTH_LONG)
                        .show()
                }
                val intent = Intent(this@DeleteHome, DeleteHome::class.java)
                startActivity(intent)
                finish()
            }
        }
        val cancelButtonListener = object : DialogInterface.OnClickListener {
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
                    this@DeleteHome,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@DeleteHome,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings = Intent(this@DeleteHome, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
