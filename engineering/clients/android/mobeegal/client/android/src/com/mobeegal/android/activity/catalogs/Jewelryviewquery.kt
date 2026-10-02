package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Jewelryviewquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
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

class Jewelryviewquery : ListActivity() {

    private var itla: CheckBoxifiedTextListAdapter? = null
    var checkBoxifiedTextobj: Array<CheckBoxifiedText?>? = null
    var count: Int = 0
    var i: Int = 0
    var text: String = ""
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

    var rows: Int = 0
    var c: Cursor? = null
    private var bArray: BooleanArray? = null
    var checkboxStatus: Boolean = false

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        myDatabase = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        //String myCols[] = {"key", "iStuffitemtype", "iStuffWeight", "iStuffArea", "iStuffCity", "iStuffCountry", " uStuffitemtype", "uStuffWeightRange", " uStuffArea", " uStuffCity", " uStuffCountry","queryStatus"};
        c = myDatabase!!.query("Jewelry", null, null, null, null, null, null)
        rows = c!!.count
        if (rows == 0) {
            Toast.makeText(
                this@Jewelryviewquery, R.string.noviewquery,
                Toast.LENGTH_LONG
            ).show()
        }
        bArray = BooleanArray(rows)
        queryStatus = arrayOfNulls(rows)

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
                "Merchant Profile: Itemtype=" + iStuffitemtype[j] +
                    ", Weight=" + iStuffWeight[j] + ", Area=" +
                    iStuffArea[j] + ", City=" + iStuffCity[j] +
                    ", Country=" + iStuffCountry[j] +
                    ".\nCustomer Profile: Itemtype=" +
                    uStuffitemtype[j] + ", Weight=" +
                    uStuffWeightRange[j] + ", Area=" + uStuffArea[j] +
                    ", City=" + uStuffCity[j] + ", Country=" +
                    uStuffCountry[j], checkboxStatus
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
                bArray!![position] = mCheckBox.isChecked
                //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
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
                    Intent(this@Jewelryviewquery, MapResults::class.java)
                startActivity(mapViewintent)
            }
            2 -> {
                val catalogintent =
                    Intent(this@Jewelryviewquery, FindandInstall::class.java)
                startActivity(catalogintent)
            }
            3 -> {
                val settingsintent =
                    Intent(this@Jewelryviewquery, Settings::class.java)
                startActivity(settingsintent)
            }
            4 -> {
                val jewelryintent =
                    Intent(this@Jewelryviewquery, Jewelry::class.java)
                startActivity(jewelryintent)
            }
            5 -> {
                if (rows == 0) {
                    Toast.makeText(
                        this@Jewelryviewquery, R.string.donequery,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    for (j in 0 until rows) {
                        if (bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Jewelry set queryStatus='" +
                                    "true" + "' where key=" +
                                    jewelryId[j] + ";"
                            )
                            myDatabase!!.execSQL(
                                "update category set queryStatus='" +
                                    "true" + "' where categoryname='" +
                                    "Jewelry" + "';"
                            )
                        } else if (!bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Jewelry set queryStatus='" +
                                    "false" + "' where key=" +
                                    jewelryId[j] + ";"
                            )
                        }
                    }
                    Toast.makeText(
                        this@Jewelryviewquery,
                        this.getString(R.string.ShowMessage),
                        Toast.LENGTH_LONG
                    ).show()
//                Intent updateintent = new Intent(Jewelryviewquery.this, Jewelry.class);
//                startActivity(updateintent);
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
