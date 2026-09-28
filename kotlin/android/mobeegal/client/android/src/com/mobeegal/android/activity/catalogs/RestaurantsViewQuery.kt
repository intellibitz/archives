package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: RestaurantsViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss       $: Id of last commit
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

class RestaurantsViewQuery : ListActivity() {

    var size: Int = 25
    private var ulongitude1 = arrayOfNulls<String>(size)
    private var ulatitude1 = arrayOfNulls<String>(size)
    private var ucountry1 = arrayOfNulls<String>(size)
    private var ucity1 = arrayOfNulls<String>(size)
    private var uarea1 = arrayOfNulls<String>(size)
    private var uStuffMiscellaneous1 = arrayOfNulls<String>(size)
    private var uStuffOccasionOrSeason1 = arrayOfNulls<String>(size)
    private var uStuffMainIngredient1 = arrayOfNulls<String>(size)
    private var uStuffDishType1 = arrayOfNulls<String>(size)
    private var uStuffDietetic1 = arrayOfNulls<String>(size)
    private var uStuffCourseType1 = arrayOfNulls<String>(size)
    private var uStuffCookingMethod1 = arrayOfNulls<String>(size)
    private var uStuffCuisinetype1 = arrayOfNulls<String>(size)
    private var ilongitude1 = arrayOfNulls<String>(size)
    private var ilatitude1 = arrayOfNulls<String>(size)
    private var icountry1 = arrayOfNulls<String>(size)
    private var icity1 = arrayOfNulls<String>(size)
    private var iarea1 = arrayOfNulls<String>(size)
    private var iStuffMiscellaneous1 = arrayOfNulls<String>(size)
    private var iStuffOccasionOrSeason1 = arrayOfNulls<String>(size)
    private var iStuffMainIngredient1 = arrayOfNulls<String>(size)
    private var iStuffDishType1 = arrayOfNulls<String>(size)
    private var iStuffDietetic1 = arrayOfNulls<String>(size)
    private var iStuffCourseType1 = arrayOfNulls<String>(size)
    private var iStuffCookingMethod1 = arrayOfNulls<String>(size)
    private var iStuffCuisinetype1 = arrayOfNulls<String>(size)
    var myDatabase: SQLiteDatabase? = null
    var c: Cursor? = null
    var rows: Int = 0
    var count: Int = 0
    var text: String = ""
    var checkboxStatus: Boolean = false
    private var bArray: BooleanArray? = null
    private var itla: CheckBoxifiedTextListAdapter? = null
    var checkBoxifiedTextobj: Array<CheckBoxifiedText?>? = null
    var RestaurantId = IntArray(size)
    var queryStatus = arrayOfNulls<String>(size)

    //private Context mContext;
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        myDatabase = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        val myCols = arrayOf(
            "key", "iStuffCuisinetype", "iStuffCookingMethod",
            "iStuffDietetic", "iStuffCourseType", "iStuffDishType",
            "iStuffMainIngredient", "iStuffOccasionOrSeason",
            "iStuffMiscellaneous", "iarea", "icity", "icountry",
            "ilatitude", "ilongitude",
            "uStuffCuisinetype", "uStuffCookingMethod", "uStuffDietetic",
            "uStuffCourseType", "uStuffDishType", "uStuffMainIngredient",
            "uStuffOccasionOrSeason", "uStuffMiscellaneous", "uarea",
            "ucity", "ucountry", "ulatitude", "ulongitude", "queryStatus"
        )
        c = myDatabase!!.query("Restaurants", myCols, null, null, null, null, null)
        rows = c!!.count
        if (rows == 0) {
            Toast.makeText(
                this@RestaurantsViewQuery, R.string.noviewquery,
                Toast.LENGTH_LONG
            ).show()
        }
        bArray = BooleanArray(rows)
        val idcolumn = c!!.getColumnIndexOrThrow("key")
        val iStuffCuisinetype = c!!.getColumnIndexOrThrow("iStuffCuisinetype")
        val iStuffCookingMethod =
            c!!.getColumnIndexOrThrow("iStuffCookingMethod")
        val iStuffCourseType = c!!.getColumnIndexOrThrow("iStuffCourseType")
        val iStuffDietetic = c!!.getColumnIndexOrThrow("iStuffDietetic")
        val iStuffDishType = c!!.getColumnIndexOrThrow("iStuffDishType")
        val iStuffMainIngredient =
            c!!.getColumnIndexOrThrow("iStuffMainIngredient")
        val iStuffOccasionOrSeason =
            c!!.getColumnIndexOrThrow("iStuffOccasionOrSeason")
        val iStuffMiscellaneous =
            c!!.getColumnIndexOrThrow("iStuffMiscellaneous")
        val iarea = c!!.getColumnIndexOrThrow("iarea")
        val icity = c!!.getColumnIndexOrThrow("icity")
        val icountry = c!!.getColumnIndexOrThrow("icountry")
        val ilatitude = c!!.getColumnIndexOrThrow("ilatitude")
        val ilongitude = c!!.getColumnIndexOrThrow("ilongitude")

        val uStuffCuisinetype = c!!.getColumnIndexOrThrow("uStuffCuisinetype")
        val uStuffCookingMethod =
            c!!.getColumnIndexOrThrow("uStuffCookingMethod")
        val uStuffCourseType = c!!.getColumnIndexOrThrow("uStuffCourseType")
        val uStuffDietetic = c!!.getColumnIndexOrThrow("uStuffDietetic")
        val uStuffDishType = c!!.getColumnIndexOrThrow("uStuffDishType")
        val uStuffMainIngredient =
            c!!.getColumnIndexOrThrow("uStuffMainIngredient")
        val uStuffOccasionOrSeason =
            c!!.getColumnIndexOrThrow("uStuffOccasionOrSeason")
        val uStuffMiscellaneous =
            c!!.getColumnIndexOrThrow("uStuffMiscellaneous")
        val uarea = c!!.getColumnIndexOrThrow("uarea")
        val ucity = c!!.getColumnIndexOrThrow("ucity")
        val ucountry = c!!.getColumnIndexOrThrow("ucountry")
        val ulatitude = c!!.getColumnIndexOrThrow("ulatitude")
        val ulongitude = c!!.getColumnIndexOrThrow("ulongitude")
        val querystatuscolumn = c!!.getColumnIndexOrThrow("queryStatus")

        if (c != null) {
            count = 0
            if (c!!.isFirst) {
                do {
                    val getid = c!!.getInt(idcolumn)
                    val getiStuffCuisinetype =
                        c!!.getString(iStuffCuisinetype)
                    val getiStuffCookingMethod =
                        c!!.getString(iStuffCookingMethod)
                    val getiStuffCourseType = c!!.getString(iStuffCourseType)
                    val getiStuffDietetic = c!!.getString(iStuffDietetic)
                    val getiStuffDishType = c!!.getString(iStuffDishType)
                    val getiStuffMainIngredient =
                        c!!.getString(iStuffMainIngredient)
                    val getiStuffOccasionOrSeason =
                        c!!.getString(iStuffOccasionOrSeason)
                    val getiStuffMiscellaneous =
                        c!!.getString(iStuffMiscellaneous)
                    val getiarea = c!!.getString(iarea)
                    val geticity = c!!.getString(icity)
                    val geticountry = c!!.getString(icountry)
                    val getilatitude = c!!.getString(ilatitude)
                    val getilongitude = c!!.getString(ilongitude)

                    val getuStuffCuisinetype =
                        c!!.getString(uStuffCuisinetype)
                    val getuStuffCookingMethod =
                        c!!.getString(uStuffCookingMethod)
                    val getuStuffCourseType = c!!.getString(uStuffCourseType)
                    val getuStuffDietetic = c!!.getString(uStuffDietetic)
                    val getuStuffDishType = c!!.getString(uStuffDishType)
                    val getuStuffMainIngredient =
                        c!!.getString(uStuffMainIngredient)
                    val getuStuffOccasionOrSeason =
                        c!!.getString(uStuffOccasionOrSeason)
                    val getuStuffMiscellaneous =
                        c!!.getString(uStuffMiscellaneous)
                    val getuarea = c!!.getString(uarea)
                    val getucity = c!!.getString(ucity)
                    val getucountry = c!!.getString(ucountry)
                    val getulatitude = c!!.getString(ulatitude)
                    val getulongitude = c!!.getString(ulongitude)
                    val getquerystatus = c!!.getString(querystatuscolumn)

                    RestaurantId[count] = getid
                    iStuffCuisinetype1[count] = getiStuffCuisinetype
                    iStuffCookingMethod1[count] = getiStuffCookingMethod
                    iStuffCourseType1[count] = getiStuffCourseType
                    iStuffDietetic1[count] = getiStuffDietetic
                    iStuffDishType1[count] = getiStuffDishType
                    iStuffMainIngredient1[count] = getiStuffMainIngredient
                    iStuffOccasionOrSeason1[count] = getiStuffOccasionOrSeason
                    iStuffMiscellaneous1[count] = getiStuffMiscellaneous
                    iarea1[count] = getiarea
                    icity1[count] = geticity
                    icountry1[count] = geticountry
                    ilatitude1[count] = getilatitude
                    ilongitude1[count] = getilongitude

                    uStuffCuisinetype1[count] = getuStuffCuisinetype
                    uStuffCookingMethod1[count] = getuStuffCookingMethod
                    uStuffCourseType1[count] = getuStuffCourseType
                    uStuffDietetic1[count] = getuStuffDietetic
                    uStuffDishType1[count] = getuStuffDishType
                    uStuffMainIngredient1[count] = getuStuffMainIngredient
                    uStuffOccasionOrSeason1[count] = getuStuffOccasionOrSeason
                    uStuffMiscellaneous1[count] = getuStuffMiscellaneous
                    uarea1[count] = getuarea
                    ucity1[count] = getucity
                    ucountry1[count] = getucountry
                    ulatitude1[count] = getulatitude
                    ulongitude1[count] = getulongitude
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
                "Owner Detail: Cuisinetype=" + iStuffCuisinetype1[j] +
                    ", CookingMethod=" + iStuffCookingMethod1[j] +
                    ", CourseType=" + iStuffCourseType1[j] +
                    ", Dietetic=" + iStuffDietetic1[j] + ", DishType=" +
                    iStuffDishType1[j] + ", MainIngredient=" +
                    iStuffMainIngredient1[j] + ", OccasionOrSeason=" +
                    iStuffOccasionOrSeason1[j] + ", Miscellaneous=" +
                    iStuffMiscellaneous1[j] + ", area=" + iarea1[j] +
                    ", city=" + icity1[j] + ", country=" +
                    icountry1[j] + ".\nCustomer Detail: Cuisinetype=" +
                    uStuffCuisinetype1[j] + ", CookingMethod= " +
                    uStuffCookingMethod1[j] + ", CourseType= " +
                    uStuffCourseType1[j] + ", Dietetic=" +
                    uStuffDietetic1[j] + ", DishType=" +
                    uStuffDishType1[j] + ", MainIngredient=" +
                    uStuffMainIngredient1[j] + ", OccasionOrSeason=" +
                    uStuffOccasionOrSeason1[j] + ", Miscellaneous1=" +
                    uStuffMiscellaneous1[j] + ", area=" + uarea1[j] +
                    ", city=" + ucity1[j] + ", country=" + ucountry1[j],
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

            /* bArray[position] = mCheckBox.isChecked();
            Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
            }
            });*/
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
                val mapViewintent = Intent(
                    this@RestaurantsViewQuery,
                    MapResults::class.java
                )
                startActivity(mapViewintent)
            }
            2 -> {
                val catalogintent = Intent(
                    this@RestaurantsViewQuery,
                    FindandInstall::class.java
                )
                startActivity(catalogintent)
            }
            3 -> {
                val settingsintent =
                    Intent(this@RestaurantsViewQuery, Settings::class.java)
                startActivity(settingsintent)
            }
            4 -> {
                val resintent = Intent(
                    this@RestaurantsViewQuery,
                    Restaurants::class.java
                )
                startActivity(resintent)
            }
            5 -> {
                if (rows == 0) {
                    Toast.makeText(
                        this@RestaurantsViewQuery,
                        R.string.donequery, Toast.LENGTH_LONG
                    ).show()
                } else {
                    for (j in 0 until rows) {
                        if (bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update category set querystatus='" +
                                    "true" + "' where categoryname='" +
                                    "Restaurants" + "';"
                            )
                        }
                        if (!bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Restaurants set queryStatus='" +
                                    "false" + "' where key=" + j + ";"
                            )
                        }
                    }
                    Toast.makeText(
                        this@RestaurantsViewQuery,
                        this.getString(R.string.ShowMessage),
                        Toast.LENGTH_LONG
                    ).show()
//                Intent updateintent = new Intent(RestaurantsViewQuery.this, Restaurants.class);
//                startActivity(updateintent);
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
