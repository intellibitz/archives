package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: HomeListQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss              $: Id of last commit
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

class HomeListQuery : ListActivity(), OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null

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

    var c: Cursor? = null
    var rows: Int = 0
    var count: Int = 0
    var size: Int = 15
    var HomeId = IntArray(size)
    var irental = arrayOfNulls<String>(size)
    var imisc = arrayOfNulls<String>(size)
    var irate = arrayOfNulls<String>(size)
    var istatus = arrayOfNulls<String>(size)
    var icountry = arrayOfNulls<String>(size)
    var icity = arrayOfNulls<String>(size)
    var iarea = arrayOfNulls<String>(size)

    var urental = arrayOfNulls<String>(size)
    var umisc = arrayOfNulls<String>(size)
    var urate = arrayOfNulls<String>(size)
    var ustatus = arrayOfNulls<String>(size)
    var ucountry = arrayOfNulls<String>(size)
    var ucity = arrayOfNulls<String>(size)
    var uarea = arrayOfNulls<String>(size)

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        listAdapter = SpeechListAdapter(this)
        listView.onItemClickListener = this
    }

    private inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context

        init {
            //Context mContext = (Context) context;
            try {
                myDatabase = mContext.openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                val myCols = arrayOf(
                    "key", "irental", "imisc", "irate",
                    "istatus", "icountry", "icity", "iarea", "urental",
                    "umisc", "urate", "ustatus", "ucountry", "ucity",
                    "uarea", "queryStatus"
                )
                c = myDatabase!!.query("Home", myCols, null, null, null, null, null)
                rows = c!!.count

                val idcolumn = c!!.getColumnIndexOrThrow("key")
                val irentaltypeColumn = c!!.getColumnIndexOrThrow("irental")
                val imiscColumn = c!!.getColumnIndexOrThrow("imisc")
                val irateColumn = c!!.getColumnIndexOrThrow("irate")
                val istatuscolumn = c!!.getColumnIndexOrThrow("istatus")
                val icountryColumn = c!!.getColumnIndexOrThrow("icountry")
                val icityColumn = c!!.getColumnIndexOrThrow("icity")
                val iareaColumn = c!!.getColumnIndexOrThrow("iarea")

                val urentaltypeColumn = c!!.getColumnIndexOrThrow("urental")
                val umiscColumn = c!!.getColumnIndexOrThrow("umisc")
                val urateColumn = c!!.getColumnIndexOrThrow("urate")
                val ustatuscolumn = c!!.getColumnIndexOrThrow("ustatus")
                val ucountryColumn = c!!.getColumnIndexOrThrow("ucountry")
                val ucityColumn = c!!.getColumnIndexOrThrow("ucity")
                val uareaColumn = c!!.getColumnIndexOrThrow("uarea")

                if (c != null) {
                    count = 0
                    if (c!!.isFirst) {
                        do {
                            val getid = c!!.getInt(idcolumn)
                            val getirentaltype =
                                c!!.getString(irentaltypeColumn)
                            val getimisc = c!!.getString(imiscColumn)
                            val getirate = c!!.getString(irateColumn)
                            val getistatus = c!!.getString(istatuscolumn)
                            val geticountry = c!!.getString(icountryColumn)
                            val geticity = c!!.getString(icityColumn)
                            val getiarea = c!!.getString(iareaColumn)

                            val geturentaltype =
                                c!!.getString(urentaltypeColumn)
                            val getumisc = c!!.getString(umiscColumn)
                            val geturate = c!!.getString(urateColumn)
                            val getustatus = c!!.getString(ustatuscolumn)
                            val getucountry = c!!.getString(ucountryColumn)
                            val getucity = c!!.getString(ucityColumn)
                            val getuarea = c!!.getString(uareaColumn)

                            HomeId[count] = getid
                            irental[count] = getirentaltype
                            imisc[count] = getimisc
                            irate[count] = getirate
                            istatus[count] = getistatus
                            icountry[count] = geticountry
                            icity[count] = geticity
                            iarea[count] = getiarea

                            urental[count] = geturentaltype
                            umisc[count] = getumisc
                            urate[count] = geturate
                            ustatus[count] = getustatus
                            ucountry[count] = getucountry
                            ucity[count] = getucity
                            uarea[count] = getuarea

                            count++
                        } while (c!!.moveToNext())
                    }
                }
            } catch (ex: Exception) {
                //Toast.makeText(HomeListQuery.this, ""+ex, Toast.LENGTH_LONG).show();
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
                        irental[position] + " Misc = " + imisc[position] +
                        "Rate = " + irate[position] + "status = " +
                        istatus[position] + " Area = " + iarea[position] +
                        " City = " + icity[position] + " Country = " +
                        icountry[position],
                    " RentalType = " + urental[position] + " Misc = " +
                        umisc[position] + " Rate = " + urate[position] +
                        " Status =" + ustatus[position] + " Area = " +
                        uarea[position] + " City = " + ucity[position] +
                        " Country= " + ucountry[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(irental[position])
                sv.setDialogue(ucity[position])
            }
            return sv
        }

        /* public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {

        }*/
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
            mTitle.text = "Owner Detail : $title"
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )

            mDialogue = TextView(context)
            mDialogue.text = "Tenant Detail : $words"
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
        val editrental = Intent(this@HomeListQuery, Home::class.java)
        val temprentalcursor = myDatabase!!.query(
            "rentalposition", null,
            "key=" + HomeId[passkeyvalue], null, null, null, null
        )

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
                        temprentalcursor.getColumnIndexOrThrow("iarea")
                    )
                    geticity = temprentalcursor.getString(
                        temprentalcursor.getColumnIndexOrThrow("icity")
                    )
                    geticountry = temprentalcursor.getString(
                        temprentalcursor.getColumnIndexOrThrow("icountry")
                    )
                    getuarea = temprentalcursor.getString(
                        temprentalcursor.getColumnIndexOrThrow("uarea")
                    )
                    getucity = temprentalcursor.getString(
                        temprentalcursor.getColumnIndexOrThrow("ucity")
                    )
                    getucountry = temprentalcursor.getString(
                        temprentalcursor.getColumnIndexOrThrow("ucountry")
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
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS temprental" +
                            " (irentalposition NUMERIC, imiscposition NUMERIC, istatusposition NUMERIC,irateposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, urentalposition NUMERIC, umiscposition NUMERIC, ustatusposition NUMERIC,urateposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
                    )
                    myDatabase!!.execSQL(
                        "INSERT INTO temprental (irentalposition,imiscposition,istatusposition,irateposition,iarea,icity,icountry,urentalposition,umiscposition,ustatusposition,urateposition,uarea,ucity,ucountry, ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype ) VALUES (" +
                            getirentalposition + "," +
                            getimiscposition + "," +
                            getistatusposition + "," +
                            getirateposition + ",'" + getiarea + "','" +
                            geticity + "','" + geticountry + "'," +
                            geturentalposition + "," +
                            getumiscposition + "," +
                            getustatusposition + "," +
                            geturateposition + ",'" + getuarea + "','" +
                            getucity + "','" + getucountry + "','" +
                            getilatitude + "','" + getilongitude +
                            "','" + getulatitude + "','" +
                            getulongitude + "','" + "Rental" + "', '" +
                            "istuff" + "');"
                    )
                } while (temprentalcursor.moveToNext())
            }
        }
        val passkeyBundle = Bundle()
        passkeyBundle.putInt("key", HomeId[passkeyvalue])
        editrental.putExtras(passkeyBundle)
        startActivityForResult(editrental, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    // Menu Item
    override fun onMenuItemSelected(i: Int, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            1 -> {
                val stuffCheckintent = Intent(
                    this@HomeListQuery,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@HomeListQuery,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@HomeListQuery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }
}
