package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: MoviesViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss            $: Id of last commit
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

class MoviesViewQuery : ListActivity() {

    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var c: Cursor? = null
    var rows: Int = 0
    private var bArray: BooleanArray? = null
    var count: Int = 0
    var checkboxStatus: Boolean = false
    var checkBoxifiedTextobj: Array<CheckBoxifiedText?>? = null
    private var itla: CheckBoxifiedTextListAdapter? = null
    var iStuffMovieType = arrayOfNulls<String>(size)
    var iStuffMovieLanguage = arrayOfNulls<String>(size)
    var iStuffSeatingStyle = arrayOfNulls<String>(size)
    var iStuffcountry = arrayOfNulls<String>(size)
    var iStuffcity = arrayOfNulls<String>(size)
    var iStuffarea = arrayOfNulls<String>(size)
    var uStuffMovieType = arrayOfNulls<String>(size)
    var uStuffMovieLanguage = arrayOfNulls<String>(size)
    var uStuffSeatingStyle = arrayOfNulls<String>(size)
    var uStuffcountry = arrayOfNulls<String>(size)
    var uStuffcity = arrayOfNulls<String>(size)
    var uStuffarea = arrayOfNulls<String>(size)
    var MovieId = IntArray(size)
    var queryStatus: Array<String?>? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        myDatabase = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        //String myCols[] = {"key","imovietype", "imovielanguage","iseatingstyle", "icountry", "icity","iarea", "umovietype", "umovielanguage","useatingstyle", "ucountry", "ucity", "uarea", "queryStatus"};
        c = myDatabase!!.query("Movies", null, null, null, null, null, null)
        rows = c!!.count
        if (rows == 0) {
            Toast.makeText(
                this@MoviesViewQuery, R.string.noviewquery,
                Toast.LENGTH_LONG
            ).show()
        }
        bArray = BooleanArray(rows)
        queryStatus = arrayOfNulls(rows)

        val idcolumn = c!!.getColumnIndexOrThrow("key")
        val imovietypeColumn = c!!.getColumnIndexOrThrow("imovietype")
        val imovielanguageColumn = c!!.getColumnIndexOrThrow("imovielanguage")
        val imiscColumn = c!!.getColumnIndexOrThrow("iseatingstyle")
        val icountryColumn = c!!.getColumnIndexOrThrow("icountry")
        val icityColumn = c!!.getColumnIndexOrThrow("icity")
        val iareaColumn = c!!.getColumnIndexOrThrow("iarea")

        val umovietypeColumn = c!!.getColumnIndexOrThrow("umovietype")
        val umovielanguageColumn = c!!.getColumnIndexOrThrow("umovielanguage")
        val umiscColumn = c!!.getColumnIndexOrThrow("useatingstyle")
        val ucountryColumn = c!!.getColumnIndexOrThrow("ucountry")
        val ucityColumn = c!!.getColumnIndexOrThrow("ucity")
        val uareaColumn = c!!.getColumnIndexOrThrow("uarea")
        val querystatuscolumn = c!!.getColumnIndexOrThrow("querystatus")

        if (c != null) {
            count = 0
            if (c!!.isFirst) {
                do {

                    val getid = c!!.getInt(idcolumn)
                    val getimovietype = c!!.getString(imovietypeColumn)
                    val getimovielanguage =
                        c!!.getString(imovielanguageColumn)
                    val getimisc = c!!.getString(imiscColumn)
                    val geticountry = c!!.getString(icountryColumn)
                    val geticity = c!!.getString(icityColumn)
                    val getiarea = c!!.getString(iareaColumn)

                    val getumovietype = c!!.getString(umovietypeColumn)
                    val getumovielanguage =
                        c!!.getString(umovielanguageColumn)
                    val getumisc = c!!.getString(umiscColumn)
                    val getucountry = c!!.getString(ucountryColumn)
                    val getucity = c!!.getString(ucityColumn)
                    val getuarea = c!!.getString(uareaColumn)
                    val getquerystatus = c!!.getString(querystatuscolumn)
                    MovieId[count] = getid
                    iStuffMovieType[count] = getimovietype
                    iStuffMovieLanguage[count] = getimovielanguage
                    iStuffSeatingStyle[count] = getimisc
                    iStuffarea[count] = getiarea

                    iStuffcountry[count] = geticountry
                    iStuffcity[count] = geticity

                    uStuffMovieType[count] = getumovietype
                    uStuffMovieLanguage[count] = getumovielanguage
                    uStuffSeatingStyle[count] = getumisc
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
                "Owner Detail: MovieType=" + iStuffMovieType[j] +
                    ", MovieLanguage=" + iStuffMovieLanguage[j] +
                    ", SeatingStyle=" + iStuffSeatingStyle[j] +
                    ", Area=" + iStuffarea[j] + ", City=" +
                    iStuffcity[j] + ", Country=" + iStuffcountry[j] +
                    ".\nPublic Detail: MovieType=" +
                    uStuffMovieType[j] + ", MovieLanguage=" +
                    uStuffMovieLanguage[j] + ", SeatingStyle=" +
                    uStuffSeatingStyle[j] + ", Area=" + uStuffarea[j] +
                    ", City=" + uStuffcity[j] + ", Country=" +
                    uStuffcountry[j], checkboxStatus
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
                } catch (e: Exception) {
                    bArray!![position] = mCheckBox.isChecked
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
                    Intent(this@MoviesViewQuery, MapResults::class.java)
                startActivity(mapViewintent)
            }
            2 -> {
                val catalogintent =
                    Intent(this@MoviesViewQuery, FindandInstall::class.java)
                startActivity(catalogintent)
            }
            3 -> {
                val settingsintent =
                    Intent(this@MoviesViewQuery, Settings::class.java)
                startActivity(settingsintent)
            }
            4 -> {
                val moviesintent =
                    Intent(this@MoviesViewQuery, Movies::class.java)
                startActivity(moviesintent)
            }
            5 -> {
                if (rows == 0) {
                    Toast.makeText(
                        this@MoviesViewQuery, R.string.donequery,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    for (j in 0 until rows) {
                        if (bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Movies set queryStatus='" + "true" +
                                    "' where key=" + MovieId[j] + ";"
                            )
                            myDatabase!!.execSQL(
                                "update category set querystatus='" +
                                    "true" + "' where categoryname='" +
                                    "Movies" + "';"
                            )
                        } else if (!bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Movies set queryStatus='" +
                                    "false" + "' where key=" +
                                    MovieId[j] + ";"
                            )
                        }
                    }

                    Toast.makeText(
                        this@MoviesViewQuery,
                        this.getString(R.string.ShowMessage),
                        Toast.LENGTH_LONG
                    ).show()
//                Intent updateintent = new Intent(MoviesViewQuery.this, Movies.class);
//                startActivity(updateintent);
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
