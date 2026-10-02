package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: Matrimonyviewquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss         $: Id of last commit
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
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class Matrimonyviewquery : ListActivity() {

    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var c: Cursor? = null
    var rows: Int = 0
    private var bArray: BooleanArray? = null
    var count: Int = 0
    var checkboxStatus: Boolean = false
    var checkBoxifiedTextobj: Array<CheckBoxifiedText?>? = null
    private var itla: CheckBoxifiedTextListAdapter? = null
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

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        myDatabase = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )

        val myCols = arrayOf(
            "key", "ireligion", "icaste", "iage", "isex",
            "iheight", "iweight", "icolor", "iarea", "icity ",
            "icountry ", "ureligion", "ucaste", "uage", "usex ",
            "uheight", "uweight", "ucolor", "uarea", "ucity",
            "ucountry", "queryStatus"
        )

        c = myDatabase!!.query("Matrimony", myCols, null, null, null, null, null)
        rows = c!!.count
        if (rows == 0) {
            Toast.makeText(
                this@Matrimonyviewquery, R.string.noviewquery,
                Toast.LENGTH_LONG
            ).show()
        }
        bArray = BooleanArray(rows)
        queryStatus = arrayOfNulls(rows)

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
                "User Profile: Religion=" + ireligion[j] + ", Caste=" +
                    icaste[j] + ", Age=" + iage[j] + ", Sex=" +
                    isex[j] + ", Height=" + iheight[j] + ", Weight=" +
                    iweight[j] + ", Color=" + icolor[j] + ", Area=" +
                    iarea[j] + ", City=" + icity[j] + ", Country=" +
                    icountry[j] + ".\nPartner Profile: Religion=" +
                    ureligion[j] + ", Caste=" + ucaste[j] + ", Age=" +
                    uage[j] + ", Sex=" + usex[j] + ", Height=" +
                    uheight[j] + ", Weight=" + uweight[j] + ", Color=" +
                    ucolor[j] + ", Area=" + uarea[j] + ", City=" +
                    ucity[j] + ", Country=" + ucountry[j],
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
                    Intent(this@Matrimonyviewquery, MapResults::class.java)
                startActivity(mapViewintent)
            }
            2 -> {
                val catalogintent =
                    Intent(this@Matrimonyviewquery, FindandInstall::class.java)
                startActivity(catalogintent)
            }
            3 -> {
                val settingsintent =
                    Intent(this@Matrimonyviewquery, Settings::class.java)
                startActivity(settingsintent)
            }
            4 -> {
                val matrimonyintent =
                    Intent(this@Matrimonyviewquery, Matrimony::class.java)
                startActivity(matrimonyintent)
            }
            5 -> {
                if (rows == 0) {
                    Toast.makeText(
                        this@Matrimonyviewquery, R.string.donequery,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    for (j in 0 until rows) {
                        if (bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Matrimony set queryStatus='" +
                                    "true" + "' where key=" +
                                    matrimonyId[j] + ";"
                            )
                            myDatabase!!.execSQL(
                                "update category set querystatus='" +
                                    "true" + "' where categoryname='" +
                                    "Matrimony" + "';"
                            )
                        } else if (!bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Matrimony set queryStatus='" +
                                    "false" + "' where key=" +
                                    matrimonyId[j] + ";"
                            )
                        }
                        Toast.makeText(
                            this@Matrimonyviewquery,
                            "Boolean = " + java.lang.Boolean.toString(bArray!![j]),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Toast.makeText(
                        this@Matrimonyviewquery,
                        this.getString(R.string.ShowMessage),
                        Toast.LENGTH_LONG
                    ).show()
//                Intent updateintent = new Intent(Matrimonyviewquery.this, Matrimony.class);
//                startActivity(updateintent);
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
