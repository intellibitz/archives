package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: RestaurantsListQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss       $: Id of last commit
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
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class RestaurantsListQuery : ListActivity(), AdapterView.OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var c: Cursor? = null

    var rows = 0
    var count = 0
    var size = 25
    var iStuffCuisinetype = arrayOfNulls<String>(size)
    var iStuffCookingMethod = arrayOfNulls<String>(size)
    var iStuffDietetic = arrayOfNulls<String>(size)
    var iStuffCourseType = arrayOfNulls<String>(size)
    var iStuffDishType = arrayOfNulls<String>(size)
    var iStuffMainIngredient = arrayOfNulls<String>(size)
    var iStuffOccasionOrSeason = arrayOfNulls<String>(size)
    var iStuffMiscellaneous = arrayOfNulls<String>(size)
    var iRestaurantsArea = arrayOfNulls<String>(size)
    var iRestaurantsCity = arrayOfNulls<String>(size)
    var iRestaurantsCountry = arrayOfNulls<String>(size)
    var iRestaurantslatitude = arrayOfNulls<String>(size)
    var iRestaurantslongitude = arrayOfNulls<String>(size)

    var uStuffCuisinetype = arrayOfNulls<String>(size)
    var uStuffCookingMethod = arrayOfNulls<String>(size)
    var uStuffDietetic = arrayOfNulls<String>(size)
    var uStuffCourseType = arrayOfNulls<String>(size)
    var uStuffDishType = arrayOfNulls<String>(size)
    var uStuffMainIngredient = arrayOfNulls<String>(size)
    var uStuffOccasionOrSeason = arrayOfNulls<String>(size)
    var uStuffMiscellaneous = arrayOfNulls<String>(size)
    var uRestaurantsArea = arrayOfNulls<String>(size)
    var uRestaurantsCity = arrayOfNulls<String>(size)
    var uRestaurantsCountry = arrayOfNulls<String>(size)
    var uRestaurantslatitude = arrayOfNulls<String>(size)
    var uRestaurantslongitude = arrayOfNulls<String>(size)
    var restaurantId = IntArray(size)

    var iCuisineType = 0
    var iCookingMethod = 0
    var iDietetic = 0
    var iCourseType = 0
    var iDishType = 0
    var iMainIngredient = 0
    var iOccasionOrSeason = 0
    var iMiscellaneous = 0

    var iRestaurantscity: String? = null
    var iRestaurantsarea: String? = null
    var iRestaurantscountry: String? = null
    var getilatitude: String? = null
    var getilongitude: String? = null
    var uCuisineType = 0
    var uCookingMethod = 0
    var uDietetic = 0
    var uCourseType = 0
    var uDishType = 0
    var uMainIngredient = 0
    var uOccasionOrSeason = 0
    var uMiscellaneous = 0
    var uRestaurantscity: String? = null
    var uRestaurantsarea: String? = null
    var uRestaurantscountry: String? = null
    var getulatitude: String? = null
    var getulongitude: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        listAdapter = SpeechListAdapter(this)
        listView.setOnItemClickListener(this)
    }

    private inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context

        init {
            try {
                myDatabase = mContext.openOrCreateDatabase(
                    "Mobeegal",
                    Context.MODE_PRIVATE, null
                )
                val myCols = arrayOf(
                    "key", "iStuffCuisinetype",
                    "iStuffCookingMethod", "iStuffDietetic",
                    "iStuffCourseType",
                    "iStuffDishType", "iStuffMainIngredient",
                    "iStuffOccasionOrSeason", "iStuffMiscellaneous",
                    "iarea", "icity", "icountry", "ilatitude", "ilongitude",
                    "uStuffCuisinetype", "uStuffCookingMethod",
                    "uStuffDietetic", "uStuffCourseType", "uStuffDishType",
                    "uStuffMainIngredient",
                    "uStuffOccasionOrSeason", "uStuffMiscellaneous",
                    "uarea", "ucity", "ucountry",
                    "ulatitude", "ulongitude", "queryStatus"
                )
                c = myDatabase!!.query(
                    "Restaurants", myCols, null, null, null,
                    null, null
                )
                rows = c!!.count

                val idcolumn = c!!.getColumnIndexOrThrow("key")
                val iStuffcuisinetype =
                    c!!.getColumnIndexOrThrow("iStuffCuisinetype")
                val iStuffcookingMethod =
                    c!!.getColumnIndexOrThrow("iStuffCookingMethod")
                val iStuffdietetic = c!!.getColumnIndexOrThrow("iStuffDietetic")
                val iStuffcourseType =
                    c!!.getColumnIndexOrThrow("iStuffCourseType")
                val iStuffdishType = c!!.getColumnIndexOrThrow("iStuffDishType")
                val iStuffmainIngredient =
                    c!!.getColumnIndexOrThrow("iStuffMainIngredient")
                val iStuffoccasionOrSeason =
                    c!!.getColumnIndexOrThrow("iStuffOccasionOrSeason")
                val iStuffmiscellaneous =
                    c!!.getColumnIndexOrThrow("iStuffMiscellaneous")
                val iRestaurantsarea = c!!.getColumnIndexOrThrow("iarea")
                val iRestaurantscity = c!!.getColumnIndexOrThrow("icity")
                val iRestaurantscountry = c!!.getColumnIndexOrThrow("icountry")

                val uStuffcuisinetype =
                    c!!.getColumnIndexOrThrow("uStuffCuisinetype")
                val uStuffcookingMethod =
                    c!!.getColumnIndexOrThrow("uStuffCookingMethod")
                val uStuffdietetic = c!!.getColumnIndexOrThrow("uStuffDietetic")
                val uStuffcourseType =
                    c!!.getColumnIndexOrThrow("uStuffCourseType")
                val uStuffdishType = c!!.getColumnIndexOrThrow("uStuffDishType")
                val uStuffmainIngredient =
                    c!!.getColumnIndexOrThrow("uStuffMainIngredient")
                val uStuffoccasionOrSeason =
                    c!!.getColumnIndexOrThrow("uStuffOccasionOrSeason")
                val uStuffmiscellaneous =
                    c!!.getColumnIndexOrThrow("uStuffMiscellaneous")
                val uRestaurantsarea = c!!.getColumnIndexOrThrow("uarea")
                val uRestaurantscity = c!!.getColumnIndexOrThrow("ucity")
                val uRestaurantscountry = c!!.getColumnIndexOrThrow("ucountry")

                if (c != null) {
                    count = 0
                    if (c!!.isFirst) {
                        do {
                            val getid = c!!.getInt(idcolumn)
                            val iCuisineType =
                                c!!.getString(iStuffcuisinetype)
                            val iCookingMethod =
                                c!!.getString(iStuffcookingMethod)
                            val iDietetic = c!!.getString(iStuffdietetic)
                            val iCourseType = c!!.getString(iStuffcourseType)
                            val iDishType = c!!.getString(iStuffdishType)
                            val iMainIngredient =
                                c!!.getString(iStuffmainIngredient)
                            val iOccasionOrSeason =
                                c!!.getString(iStuffoccasionOrSeason)
                            val iMiscellaneous =
                                c!!.getString(iStuffmiscellaneous)
                            val irestaurantsarea =
                                c!!.getString(iRestaurantsarea)
                            val irestaurantscity =
                                c!!.getString(iRestaurantscity)
                            val irestaurantscountry =
                                c!!.getString(iRestaurantscountry)

                            val uCuisineType =
                                c!!.getString(uStuffcuisinetype)
                            val uCookingMethod =
                                c!!.getString(uStuffcookingMethod)
                            val uDietetic = c!!.getString(uStuffdietetic)
                            val uCourseType = c!!.getString(uStuffcourseType)
                            val uDishType = c!!.getString(uStuffdishType)
                            val uMainIngredient =
                                c!!.getString(uStuffmainIngredient)
                            val uOccasionOrSeason =
                                c!!.getString(uStuffoccasionOrSeason)
                            val uMiscellaneous =
                                c!!.getString(uStuffmiscellaneous)
                            val urestaurantsarea =
                                c!!.getString(uRestaurantsarea)
                            val urestaurantscity =
                                c!!.getString(uRestaurantscity)
                            val urestaurantscountry =
                                c!!.getString(uRestaurantscountry)

                            restaurantId[count] = getid
                            iStuffCuisinetype[count] = iCuisineType
                            iStuffCookingMethod[count] = iCookingMethod
                            iStuffDietetic[count] = iDietetic
                            iStuffCourseType[count] = iCourseType
                            iStuffDishType[count] = iDishType
                            iStuffMainIngredient[count] = iMainIngredient
                            iStuffOccasionOrSeason[count] = iOccasionOrSeason
                            iStuffMiscellaneous[count] = iMiscellaneous
                            iRestaurantsArea[count] = irestaurantsarea
                            iRestaurantsCity[count] = irestaurantscity
                            iRestaurantsCountry[count] = irestaurantscountry

                            uStuffCuisinetype[count] = uCuisineType
                            uStuffCookingMethod[count] = uCookingMethod
                            uStuffDietetic[count] = uDietetic
                            uStuffCourseType[count] = uCourseType
                            uStuffDishType[count] = uDishType
                            uStuffMainIngredient[count] = uMainIngredient
                            uStuffOccasionOrSeason[count] = uOccasionOrSeason
                            uStuffMiscellaneous[count] = uMiscellaneous
                            uRestaurantsArea[count] = urestaurantsarea
                            uRestaurantsCity[count] = urestaurantscity
                            uRestaurantsCountry[count] = urestaurantscountry

                            count++
                        } while (c!!.moveToNext())
                    }
                }
            } catch (ex: Exception) {
                Toast.makeText(
                    this@RestaurantsListQuery, "" + ex,
                    Toast.LENGTH_LONG
                ).show()
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
                    mContext,
                    " ,iCuisineType = " + iStuffCuisinetype[position] +
                            ", iCookingMethod = " +
                            iStuffCookingMethod[position] +
                            ", iDietetic = " + iStuffDietetic[position] +
                            ", iCourseType = " + iStuffCourseType[position]
                            + ",iDishType = " + iStuffDishType[position] +
                            ",iMainIngrediant =" +
                            iStuffMainIngredient[position] +
                            ",iOccasionOrSeason = " +
                            iStuffOccasionOrSeason[position] +
                            ",iMiscellaneous =" +
                            iRestaurantsArea[position] + ",iArea =" +
                            iRestaurantsArea[position] + ",iCity =" +
                            iRestaurantsCity[position] + ",icountry = " +
                            iRestaurantsCountry[position],
                    ",uCuisineType = " + uStuffCuisinetype[position] +
                            " ,uCookingMethod = " +
                            uStuffCookingMethod[position] +
                            " ,uDietetic = " + uStuffDietetic[position] +
                            ", uCourseType = " + uStuffCourseType[position]
                            + ",uDishType = " + uStuffDishType[position] +
                            ",uMainIngrediant =" +
                            uStuffMainIngredient[position] +
                            ",uOccasionOrSeason = " +
                            uStuffOccasionOrSeason[position] +
                            ",uMiscellaneous =" +
                            uRestaurantsArea[position] + ",uArea =" +
                            uRestaurantsArea[position] + ",uCity =" +
                            uRestaurantsCity[position] + ",ucountry =" +
                            uRestaurantsCountry[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffCuisinetype[position])
                sv.setDialogue(uRestaurantsCountry[position])
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
            mTitle.text = "Owner detail : $title"
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )

            mDialogue = TextView(context)
            mDialogue.text = "Customer Detail : $words"
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
        val passkeyvalue = Integer.parseInt(selectid)
        val editrestaurants =
            Intent(this@RestaurantsListQuery, Restaurants::class.java)
        val restaurantsCursor = myDatabase!!.query(
            "restaurantsposition", null,
            "key=" + restaurantId[passkeyvalue], null, null, null, null
        )
        if (restaurantsCursor != null) {
            if (restaurantsCursor.isFirst) {
                do {
                    //iCuisineTypeposition,iCookingMethodposition ,iDieteticposition,iCourseTypeposition
                    //,iDishTypeposition ,iMainIngredientposition ,iOccasionOrSeasonposition ,iMiscellaneousposition
                    //,iarea , icity , icountry , uCuisineTypeposition , uCookingMethodposition , uDieteticposition ,
                    //uCourseTypeposition ,uDishTypeposition ,uMainIngredientposition ,uOccasionOrSeasonposition ,uMiscellaneousposition
                    //,uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype
                    iCuisineType = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iCuisineTypeposition"
                        )
                    )
                    iCookingMethod = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iCookingMethodposition"
                        )
                    )
                    iDietetic = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iDieteticposition"
                        )
                    )
                    iCourseType = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iCourseTypeposition"
                        )
                    )
                    iDishType = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iDishTypeposition"
                        )
                    )
                    iMainIngredient = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iMainIngredientposition"
                        )
                    )
                    iOccasionOrSeason = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iOccasionOrSeasonposition"
                        )
                    )
                    iMiscellaneous = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "iMiscellaneousposition"
                        )
                    )
                    iRestaurantsarea = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow("iarea")
                    )
                    iRestaurantscity = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow("icity")
                    )
                    iRestaurantscountry = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "icountry"
                        )
                    )
                    getilatitude = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "ilatitude"
                        )
                    )
                    getilongitude = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "ilongitude"
                        )
                    )

                    uCuisineType = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uCuisineTypeposition"
                        )
                    )
                    uCookingMethod = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uCookingMethodposition"
                        )
                    )
                    uDietetic = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uDieteticposition"
                        )
                    )
                    uCourseType = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uCourseTypeposition"
                        )
                    )
                    uDishType = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uDishTypeposition"
                        )
                    )
                    uMainIngredient = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uMainIngredientposition"
                        )
                    )
                    uOccasionOrSeason = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uOccasionOrSeasonposition"
                        )
                    )
                    uMiscellaneous = restaurantsCursor.getInt(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "uMiscellaneousposition"
                        )
                    )
                    uRestaurantsarea = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow("uarea")
                    )
                    uRestaurantscity = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow("ucity")
                    )
                    uRestaurantscountry = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "ucountry"
                        )
                    )
                    getulatitude = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "ulatitude"
                        )
                    )
                    getulongitude = restaurantsCursor.getString(
                        restaurantsCursor.getColumnIndexOrThrow(
                            "ulongitude"
                        )
                    )
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS temprestaurant" +
                                " (iCuisineTypeposition NUMERIC, iCookingMethodposition NUMERIC, iDieteticposition NUMERIC,iCourseTypeposition NUMERIC,iDishTypeposition NUMERIC,iMainIngredientposition NUMERIC,iOccasionOrSeasonposition NUMERIC,iMiscellaneousposition NUMERIC ,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uCuisineTypeposition NUMERIC, uCookingMethodposition NUMERIC, uDieteticposition NUMERIC,uCourseTypeposition NUMERIC,uDishTypeposition NUMERIC,uMainIngredientposition NUMERIC,uOccasionOrSeasonposition NUMERIC,uMiscellaneousposition NUMERIC ,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);"
                    )
                    myDatabase!!.execSQL(
                        "INSERT INTO temprestaurant (iCuisineTypeposition,iCookingMethodposition ,iDieteticposition,iCourseTypeposition,iDishTypeposition ,iMainIngredientposition ,iOccasionOrSeasonposition ,iMiscellaneousposition  ,iarea , icity , icountry , uCuisineTypeposition , uCookingMethodposition , uDieteticposition ,uCourseTypeposition ,uDishTypeposition ,uMainIngredientposition ,uOccasionOrSeasonposition ,uMiscellaneousposition  ,uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype  ) VALUES (" +
                                iCuisineType + "," + iCookingMethod + "," +
                                iDietetic + "," + iCourseType + "," +
                                iDishType + "," + iMainIngredient + "," +
                                iOccasionOrSeason + "," + iMiscellaneous +
                                ",'" + iRestaurantsarea + "','" +
                                iRestaurantscity + "','" +
                                iRestaurantscountry + "'," + uCuisineType +
                                "," + uCookingMethod + "," + uDietetic +
                                "," + uCourseType + "," + uDishType + "," +
                                uMainIngredient + "," + uOccasionOrSeason +
                                "," + uMiscellaneous + ",'" +
                                uRestaurantsarea + "','" +
                                uRestaurantscity + "','" +
                                uRestaurantscountry + "','" + getilatitude +
                                "','" + getilongitude + "','" +
                                getulatitude + "','" + getulongitude +
                                "','" + "Restaurants" + "', '" + "istuff" +
                                "');"
                    )
                } while (c!!.moveToNext())
            }
        }
        val passkeyBundle = Bundle()
        passkeyBundle.putInt("key", restaurantId[passkeyvalue])
        editrestaurants.putExtras(passkeyBundle)
        startActivityForResult(editrestaurants, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    // Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent =
                    Intent(this@RestaurantsListQuery, MapResults::class.java)
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 =
                    Intent(this@RestaurantsListQuery, FindandInstall::class.java)
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@RestaurantsListQuery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
