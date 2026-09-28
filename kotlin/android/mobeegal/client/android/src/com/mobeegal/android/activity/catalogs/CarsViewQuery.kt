package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: CarsViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss              $: Id of last commit
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
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class CarsViewQuery : ListActivity() {

    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var c: Cursor? = null
    var rows: Int = 0
    private var bArray: BooleanArray? = null
    var count: Int = 0
    var checkboxStatus: Boolean = false
    var checkBoxifiedTextobj: Array<CheckBoxifiedText?>? = null
    private var itla: CheckBoxifiedTextListAdapter? = null
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

    var ucountry = arrayOfNulls<String>(size)
    var ucity = arrayOfNulls<String>(size)
    var uarea = arrayOfNulls<String>(size)
    var queryStatus = arrayOfNulls<String>(size)

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        myDatabase = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        val myCols = arrayOf(
            "key", "imake", "imodel", "iyear", "icolor",
            "ifuel_type", "iprice", "icountry", "icity", "iarea", "umake",
            "umodel", "uyear", "ucolor", "ufuel_type", "uprice",
            " ucountry", "ucity", "uarea", "queryStatus"
        )
        c = myDatabase!!.query("Cars", myCols, null, null, null, null, null)
        rows = c!!.count
        if (rows == 0) {
            Toast.makeText(
                this@CarsViewQuery, R.string.noviewquery,
                Toast.LENGTH_LONG
            ).show()
        }
        bArray = BooleanArray(rows)
        queryStatus = arrayOfNulls(rows)

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
                    val getuquerystatus = c!!.getString(uquerystatuscolumn)

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
//            if (c1 != null) {
//                count = 0;
//                if (c1.isFirst()) {
//                    do {
//
//
//                        //results.add("IStuff :" + iStuffarea[count] + "UStuff :" + uStuffarea[count]);
//                        count++;
//                    } while (c1.moveToNext());
//                }
//            }
        for (i in 0 until rows) {
            if (queryStatus[i] == "true") {
                bArray!![i] = true
            } else {
                bArray!![i] = false
            }
        }
        checkBoxifiedTextobj = arrayOfNulls(rows)
        itla = CheckBoxifiedTextListAdapter(this)
        for (j in 0 until rows) {
            if (queryStatus[j] == "true") {
                checkboxStatus = true
            } else if (queryStatus[j] == "false") {
                checkboxStatus = false
            }
            checkBoxifiedTextobj!![j] = CheckBoxifiedText(
                "Seller Details : make  = " + imake[j] + ", model = " +
                    imodel[j] + ", year = " + iyear[j] + ", color = " +
                    icolor[j] + ", fueltype = " + ifueltype[j] +
                    ", price = " + iprice[j] + ", area = " + iarea[j] +
                    ", city = " + icity[j] + ", country = " +
                    icountry[j] + ". \nBuyer Details : make = " +
                    umake[j] + ", model = " + umodel[j] + ", year = " +
                    uyear[j] + ", color = " + ucolor[j] +
                    ", fueltype = " + ufueltype[j] + ", price = " +
                    uprice[j] + ", area = " + uarea[j] + ", city = " +
                    ucity[j] + ", country = " + ucountry[j],
                checkboxStatus
            )
            itla!!.addItem(checkBoxifiedTextobj!![j]!!)
        }
        listAdapter = itla
    }

    inner class CheckBoxifiedText(text: String, checked: Boolean) {

        private var mText: String = text
        private var mCheckBox: CheckBox? = null
        private var mChecked: Boolean = checked

        fun setChecked(value: Boolean) {
            this.mChecked = value
        }

        fun getChecked(): Boolean {
            return this.mChecked
        }

        fun getText(): String {
            return mText
        }

        fun getCheckBox(): CheckBox? {
            return mCheckBox
        }
    }

    inner class CheckBoxifiedTextListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context
        var mItems: MutableList<CheckBoxifiedText> = ArrayList()
        var item: CheckBoxifiedText? = null

        fun addItem(it: CheckBoxifiedText) {
            mItems.add(it)
        }

        fun setListItems(lit: MutableList<CheckBoxifiedText>) {
            mItems = lit
        }

        fun getListItem(): List<CheckBoxifiedText> {
            return mItems
        }

        override fun getCount(): Int {
            return mItems.size
        }

        override fun getItem(position: Int): Any {
            return mItems[position]
        }

        fun areAllItemsSelectable(): Boolean {
            return false
        }

        fun getStatus(): BooleanArray {
            val bArray = BooleanArray(mItems.size)
            var increment = 0
            for (cboxtxt in mItems) {
                bArray[increment] = cboxtxt.getChecked()
            }
            return bArray
        }

        fun deSelectAll() {
            for (cboxtxt in mItems) {
                cboxtxt.setChecked(false)
            }
            this.notifyDataSetInvalidated()
        }

        fun selectAll() {
            for (cboxtxt in mItems) {
                cboxtxt.setChecked(true)
            }
            this.notifyDataSetInvalidated()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val btv: CheckBoxifiedTextView

            if (convertView == null) {
                btv = CheckBoxifiedTextView(mContext, mItems[position], position)
                val src = mItems[position]
            } else {
                val src = mItems[position]
                btv = convertView as CheckBoxifiedTextView
                btv.setText(src.getText())
                btv.setCheckBoxState(src.getChecked())
            }
            return btv
        }
    }

    inner class CheckBoxifiedTextView(
        context: Context,
        aCheckBoxifiedText: CheckBoxifiedText,
        position: Int
    ) : LinearLayout(context) {

        private val mText: TextView
        private val mCheckBox: CheckBox
        private val mCheckBoxText: CheckBoxifiedText = aCheckBoxifiedText
        var increment: Int = 0

        init {
            this.orientation = HORIZONTAL
            mCheckBox = CheckBox(context)
            mCheckBox.setPadding(0, 0, 20, 0)  // 5px to the right
            mCheckBox.isChecked = aCheckBoxifiedText.getChecked()
            mCheckBox.setOnCheckedChangeListener(OnCheckedChangeListener { _, _ ->
                try {
                    bArray!![position] = mCheckBox.isChecked
                    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]), Toast.LENGTH_LONG).show();
                } catch (e: Exception) {
                    //bArray[position] = mCheckBox.isChecked();
                    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
                    //Toast.makeText(context, " position = "+Integer.toString(position)+" rows = "+Integer.toString(rows),Toast.LENGTH_SHORT).show();
                }
            })
            addView(
                mCheckBox, LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
            )
            mText = TextView(context)
            mText.text = aCheckBoxifiedText.getText()
            addView(
                mText, LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
            )
        }

        fun setText(words: String?) {
            mText.text = words
        }

        fun setCheckBoxState(bool: Boolean) {
            mCheckBox.isChecked = mCheckBoxText.getChecked()
            mCheckBoxText.setChecked(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsViewQueryMenu(menu)
        return true
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val mapViewintent =
                    Intent(this@CarsViewQuery, MapResults::class.java)
                startActivity(mapViewintent)
            }
            2 -> {
                val catalogintent =
                    Intent(this@CarsViewQuery, FindandInstall::class.java)
                startActivity(catalogintent)
            }
            3 -> {
                val settingsintent =
                    Intent(this@CarsViewQuery, Settings::class.java)
                startActivity(settingsintent)
            }
            4 -> {
                val carintent = Intent(this@CarsViewQuery, Cars::class.java)
                startActivity(carintent)
            }
            5 -> {
                if (rows == 0) {
                    Toast.makeText(
                        this@CarsViewQuery, R.string.donequery,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    for (j in 0 until rows) {
                        if (bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Cars set queryStatus='" +
                                    "true" + "' where key=" + carsId[j] + ";"
                            )
                            myDatabase!!.execSQL(
                                "update category set querystatus='" +
                                    "true" + "' where categoryname='" +
                                    "Cars" + "';"
                            )
                        } else if (!bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Cars set queryStatus='" +
                                    "false" + "' where key=" + carsId[j] + ";"
                            )
                        }
                        Toast.makeText(
                            this@CarsViewQuery,
                            "Boolean = " + java.lang.Boolean.toString(bArray!![j]),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Toast.makeText(
                        this@CarsViewQuery,
                        this.getString(R.string.ShowMessage),
                        Toast.LENGTH_LONG
                    ).show()
//                Intent updateintent = new Intent(CarsViewQuery.this, Cars.class);
//                startActivity(updateintent);
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
