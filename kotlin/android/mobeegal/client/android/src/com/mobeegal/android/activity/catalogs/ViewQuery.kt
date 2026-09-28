package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: ViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss                     $: Id of last commit
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

class ViewQuery : ListActivity() {

    private var itla: CheckBoxifiedTextListAdapter? = null
    var checkBoxifiedTextobj: Array<CheckBoxifiedText?>? = null
    var count: Int = 0
    var i: Int = 0
    var text: String = ""
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
    var queryStatus: Array<String?>? = null // = new String[size];
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
        val myCols = arrayOf(
            "key", "iage", "isex", "iheight", "iweight", "iarea",
            "icity", "icountry", "uage", "usex", "uheight", "uweight",
            "uarea", "ucity", "ucountry", "queryStatus"
        )
        c = myDatabase!!.query("Dating", myCols, null, null, null, null, null)
        rows = c!!.count
        if (rows == 0) {
            Toast.makeText(
                this@ViewQuery, R.string.noviewquery,
                Toast.LENGTH_LONG
            ).show()
        }
        bArray = BooleanArray(rows)
        queryStatus = arrayOfNulls(rows)

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
                    queryStatus!![count] = getquerystatus
                    count++
                } while (c!!.moveToNext())
            }
        }
        for (i in 0 until rows) {
            if (queryStatus!![i] == "true") {
                bArray!![i] = true
            } else {
                bArray!![i] = false
            }
        }
        checkBoxifiedTextobj = arrayOfNulls(rows)
        itla = CheckBoxifiedTextListAdapter(this)
        for (j in 0 until rows) {
            if (queryStatus!![j] == "true") {
                checkboxStatus = true
            } else if (queryStatus!![j] == "false") {
                checkboxStatus = false
            }
            checkBoxifiedTextobj!![j] = CheckBoxifiedText(
                "User Profile: Age=" + iStuffage[j] + ", Sex=" +
                    iStuffsex[j] + ", Height=" + iStuffheight[j] +
                    ", Weight=" + iStuffweight[j] + ", Area=" +
                    iStuffarea[j] + ", City=" + iStuffcity[j] +
                    ", Country=" + iStuffcountry[j] +
                    ".\nParther Profile: Age=" + uStuffage[j] +
                    ", Sex=" + uStuffsex[j] + ", Height=" +
                    uStuffheight[j] + ", Weight=" + uStuffweight[j] +
                    ", Area=" + uStuffarea[j] + ", City=" +
                    uStuffcity[j] + ", Country=" + uStuffcountry[j],
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
                    Intent(this@ViewQuery, MapResults::class.java)
                startActivity(mapViewintent)
            }
            2 -> {
                val catalogintent =
                    Intent(this@ViewQuery, FindandInstall::class.java)
                startActivity(catalogintent)
            }
            3 -> {
                val settingsintent =
                    Intent(this@ViewQuery, Settings::class.java)
                startActivity(settingsintent)
            }
            4 -> {
                val datingintent = Intent(this@ViewQuery, Dating::class.java)
                startActivity(datingintent)
            }
            5 -> {
                if (rows == 0) {
                    Toast.makeText(
                        this@ViewQuery, R.string.donequery,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    for (j in 0 until rows) {
                        if (bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Dating set queryStatus='" + "true" +
                                    "' where key=" + datingId[j] + ";"
                            )
                            myDatabase!!.execSQL(
                                "update category set querystatus='" +
                                    "true" + "' where categoryname='" +
                                    "Dating" + "';"
                            )
                        } else if (!bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Dating set queryStatus='" +
                                    "false" + "' where key=" +
                                    datingId[j] + ";"
                            )
                        }
                        Toast.makeText(
                            this@ViewQuery,
                            "Boolean = " + java.lang.Boolean.toString(bArray!![j]),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Toast.makeText(
                        this@ViewQuery,
                        this.getString(R.string.ShowMessage),
                        Toast.LENGTH_LONG
                    ).show()
                    //  finish();
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
