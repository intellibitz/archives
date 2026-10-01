package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: DeleteRestaurants.java 14 2008-08-19 06:36:45Z muthu.ramadoss          $: Id of last commit
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

class DeleteRestaurants : ListActivity(), AdapterView.OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var c: Cursor? = null
    var rows = 0
    var count = 0
    var size = 25
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
    var queryStatus = arrayOfNulls<String>(size)
    var RestaurantId = IntArray(size)

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        listAdapter = SpeechListAdapter(this)
        listView.setOnItemClickListener(this)
    }

    inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context

        init {
            //Context mContext = (Context) context;
            myDatabase = mContext.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val myCols = arrayOf(
                "key", "iStuffCuisinetype",
                "iStuffCookingMethod", "iStuffDietetic", "iStuffCourseType",
                "iStuffDishType",
                "iStuffMainIngredient", "iStuffOccasionOrSeason",
                "iStuffMiscellaneous", "iarea", "icity", "icountry",
                "ilatitude", "ilongitude",
                "uStuffCuisinetype", "uStuffCookingMethod",
                "uStuffDietetic", "uStuffCourseType", "uStuffDishType",
                "uStuffMainIngredient",
                "uStuffOccasionOrSeason", "uStuffMiscellaneous", "uarea",
                "ucity", "ucountry", "ulatitude", "ulongitude",
                "queryStatus"
            )
            c = myDatabase!!.query(
                "Restaurants", myCols, null, null, null, null, null
            )
            rows = c!!.count
            // bArray = new boolean[rows];
            val idcolumn = c!!.getColumnIndexOrThrow("key")
            val iStuffCuisinetype =
                c!!.getColumnIndexOrThrow("iStuffCuisinetype")
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

            val uStuffCuisinetype =
                c!!.getColumnIndexOrThrow("uStuffCuisinetype")
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
                        val getiStuffCourseType =
                            c!!.getString(iStuffCourseType)
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
                        val getuStuffCourseType =
                            c!!.getString(uStuffCourseType)
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
                        iStuffOccasionOrSeason1[count] =
                            getiStuffOccasionOrSeason
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
                        uStuffOccasionOrSeason1[count] =
                            getuStuffOccasionOrSeason
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
                    mContext, " Cuisinetype1 = " +
                        iStuffCuisinetype1[position] + " CookingMethod1 = " +
                        iStuffCookingMethod1[position] + " CourseType1 = " +
                        iStuffCourseType1[position] + " Dietetic1 = " +
                        iStuffDietetic1[position] + " DishType1 = " +
                        iStuffDishType1[position] + "MainIngredient1 = " +
                        iStuffMainIngredient1[position] +
                        "OccasionOrSeason1 = " +
                        iStuffOccasionOrSeason1[position] +
                        "Miscellaneous1 = " + iStuffMiscellaneous1[position] +
                        "iarea1 = " + iarea1[position] + "icity1 = " +
                        icity1[position] + "icountry1 = " +
                        icountry1[position] + "ilatitude1 = " +
                        ilatitude1[position] + "ilongitude1 = " +
                        ilongitude1[position], "uCuisinetype1 = " +
                        uStuffCuisinetype1[position] + " uCookingMethod1 = " +
                        uStuffCookingMethod1[position] + " uCourseType1 = " +
                        uStuffCourseType1[position] + " uDietetic1 = " +
                        uStuffDietetic1[position] + " uDishType1 = " +
                        uStuffDishType1[position] + "uMainIngredient1 = " +
                        uStuffMainIngredient1[position] +
                        "uOccasionOrSeason1 = " +
                        uStuffOccasionOrSeason1[position] +
                        "uMiscellaneous1 = " + uStuffMiscellaneous1[position] +
                        "uarea1 = " + uarea1[position] + "ucity1 = " +
                        ucity1[position] + "ucountry1 = " +
                        ucountry1[position] + "ulatitude1 = " +
                        ulatitude1[position] + "ulongitude1 = " +
                        ulongitude1[position] + " QueryStatus = " +
                        queryStatus[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffCuisinetype1[position])
                sv.setDialogue(iStuffDietetic1[position])
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
        val okButtonListener = DialogInterface.OnClickListener { arg0, arg1 ->
            try {
                myDatabase = this@DeleteRestaurants
                    .openOrCreateDatabase(
                        "Mobeegal",
                        Context.MODE_PRIVATE, null
                    )
                myDatabase!!.delete(
                    "Restaurants",
                    "key=" + RestaurantId[selectedId], null
                )
                if (selectedId == 0 && RestaurantId[0] != 0) {
                    myDatabase!!.delete(
                        "Restaurants",
                        "key=" + RestaurantId[0], null
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DeleteRestaurants, "Error",
                    Toast.LENGTH_LONG
                ).show()
            }
            val intent = Intent(
                this@DeleteRestaurants,
                DeleteRestaurants::class.java
            )
            startActivity(intent)
            finish()
        }
        val cancelButtonListener = DialogInterface.OnClickListener { arg0, arg1 ->
            // Do nothing
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
                    this@DeleteRestaurants,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@DeleteRestaurants,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@DeleteRestaurants, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
