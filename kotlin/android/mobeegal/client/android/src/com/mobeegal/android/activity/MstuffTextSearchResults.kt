package com.mobeegal.android.activity

/*
<!--
$Id:: MstuffTextSearchResults.java 14 2008-08-19 06:36:45Z muthu.ramadoss    $: Id of last commit
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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.util.ViewMenu

/**
 * @author mobeegal.in
 */
class MstuffTextSearchResults : ListActivity(), OnItemSelectedListener, OnItemClickListener {

    var mobeegalDatabase: SQLiteDatabase? = null
    var c: Cursor? = null
    var categoryCursor: Cursor? = null
    var results: ArrayList<String>? = null
    var rows = 0
    var count: Int = 0
    var mStuffid: Array<String?>? = null
    private var getCatalog: Bundle? = null
    var passedCatalogValue: String? = null
    var passedSearchValue: String? = null
    var details: String? = null
    var location: String? = null
    var noOfMatches = 0
    var mPhone: TextView? = null
    var selectedposition: Int = 0
    var selectedLocation: String? = null
    var latitude: IntArray? = null
    var longitude: IntArray? = null
    var btla: MstuffTextListAdapter? = null
    var mstufftextArray: Array<MstuffText?>? = null
    var index = 0

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        results = ArrayList()

        getCatalog = this.intent.extras
        if (getCatalog != null) {
            passedCatalogValue = getCatalog!!.getString("spinner")
            passedSearchValue = getCatalog!!.getString("edittext")
        }

        try {
            mobeegalDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val myCols = arrayOf(
                "mstuffid", "details", "latitude", "longitude",
                "location"
            )
            c = mobeegalDatabase!!.query(
                "mStuffdetails", myCols, null,
                null, null, null, null
            )
            rows = c!!.count - 3
            btla = MstuffTextListAdapter(this)
            mstufftextArray = arrayOfNulls(rows)

            mStuffid = arrayOfNulls(rows)
            latitude = IntArray(rows)
            longitude = IntArray(rows)
            val useridColumn = c!!.getColumnIndexOrThrow("mstuffid")
            val detailsColumn = c!!.getColumnIndexOrThrow("details")
            val latitudeColumn = c!!.getColumnIndexOrThrow("latitude")
            val longitudeColumn = c!!.getColumnIndexOrThrow("longitude")
            if (c != null) {
                if (c!!.isFirst) {
                    count = 0

                    do {
                        mStuffid!![count] = c!!.getString(useridColumn)
                        latitude!![count] = c!!.getInt(latitudeColumn)
                        longitude!![count] = c!!.getInt(longitudeColumn)
                        details = c!!.getString(detailsColumn)
                        if (details!!.toLowerCase()
                                .contains(passedSearchValue!!.toLowerCase())
                        ) {
                            if (passedCatalogValue == "Dating") {
                                val column =
                                    arrayOf("details", "latitude", "longitude")
                                categoryCursor = mobeegalDatabase!!
                                    .query(
                                        "mStuffdetails", column,
                                        "catagory='Dating'", null, null,
                                        null,
                                        null
                                    )
                                val detailsColumndating = categoryCursor!!
                                    .getColumnIndexOrThrow("details")
                                val latitudeColumndating = categoryCursor!!
                                    .getColumnIndexOrThrow("latitude")
                                val longitudeColumndating = categoryCursor!!
                                    .getColumnIndexOrThrow("longitude")
                                if (categoryCursor != null) {
                                    index = 0
                                    if (categoryCursor!!.isFirst) {
                                        do {
                                            val detailsdating =
                                                categoryCursor!!.getString(
                                                    detailsColumndating
                                                )
                                            val latitudedating = categoryCursor!!
                                                .getInt(latitudeColumndating)
                                            val longitudedating = categoryCursor!!
                                                .getInt(longitudeColumndating)
                                            mstufftextArray!![index] =
                                                MstuffText(
                                                    detailsdating +
                                                        ", Latitude = " +
                                                        Integer.toString(
                                                            latitudedating
                                                        ) +
                                                        ", Longitude = " +
                                                        Integer.toString(
                                                            longitudedating
                                                        ),
                                                    resources.getDrawable(
                                                        R.drawable.dating_icon
                                                    )
                                                )
                                            btla!!.addItem(
                                                mstufftextArray!![index]!!
                                            )
                                            index++
                                        } while (categoryCursor!!.moveToNext())
                                    }
                                    break
                                }
                            }

                            if (passedCatalogValue == "Matrimony") {
                                val column =
                                    arrayOf("details", "latitude", "longitude")
                                categoryCursor = mobeegalDatabase!!
                                    .query(
                                        "mStuffdetails", column,
                                        "catagory='Matrimony'", null,
                                        null,
                                        null, null
                                    )
                                val detailsColumnmatrimony = categoryCursor!!
                                    .getColumnIndexOrThrow("details")
                                val latitudeColumnmatrimony = categoryCursor!!
                                    .getColumnIndexOrThrow("latitude")
                                val longitudeColumnmatrimony = categoryCursor!!
                                    .getColumnIndexOrThrow("longitude")
                                if (categoryCursor != null) {
                                    index = 0
                                    if (categoryCursor!!.isFirst) {
                                        do {
                                            val detailsdating =
                                                categoryCursor!!.getString(
                                                    detailsColumnmatrimony
                                                )
                                            val latitudedating = categoryCursor!!
                                                .getInt(latitudeColumnmatrimony)
                                            val longitudedating = categoryCursor!!
                                                .getInt(longitudeColumnmatrimony)
                                            mstufftextArray!![index] =
                                                MstuffText(
                                                    detailsdating +
                                                        ", Latitude = " +
                                                        Integer.toString(
                                                            latitudedating
                                                        ) +
                                                        ", Longitude = " +
                                                        Integer.toString(
                                                            longitudedating
                                                        ),
                                                    resources.getDrawable(
                                                        R.drawable.matrimony_icon
                                                    )
                                                )
                                            btla!!.addItem(
                                                mstufftextArray!![index]!!
                                            )
                                            index++
                                        } while (categoryCursor!!.moveToNext())
                                    }
                                    break
                                }
                            }

                            if (passedCatalogValue == "Jewelry") {
                                val column =
                                    arrayOf("details", "latitude", "longitude")
                                categoryCursor = mobeegalDatabase!!
                                    .query(
                                        "mStuffdetails", column,
                                        "catagory='Jewelry'", null,
                                        null, null,
                                        null
                                    )
                                val detailsColumnjewelry = categoryCursor!!
                                    .getColumnIndexOrThrow("details")
                                val latitudeColumnjewelry = categoryCursor!!
                                    .getColumnIndexOrThrow("latitude")
                                val longitudeColumnjewelry = categoryCursor!!
                                    .getColumnIndexOrThrow("longitude")
                                if (categoryCursor != null) {
                                    index = 0
                                    if (categoryCursor!!.isFirst) {
                                        do {
                                            val detailsdating =
                                                categoryCursor!!.getString(
                                                    detailsColumnjewelry
                                                )
                                            val latitudedating = categoryCursor!!
                                                .getInt(latitudeColumnjewelry)
                                            val longitudedating = categoryCursor!!
                                                .getInt(longitudeColumnjewelry)
                                            mstufftextArray!![index] =
                                                MstuffText(
                                                    detailsdating +
                                                        ", Latitude = " +
                                                        Integer.toString(
                                                            latitudedating
                                                        ) +
                                                        ", Longitude = " +
                                                        Integer.toString(
                                                            longitudedating
                                                        ),
                                                    resources.getDrawable(
                                                        R.drawable.jewelry_icon
                                                    )
                                                )
                                            btla!!.addItem(
                                                mstufftextArray!![index]!!
                                            )
                                            index++
                                        } while (categoryCursor!!.moveToNext())
                                    }
                                    break
                                }
                            }

                            if (passedCatalogValue == "Cars") {
                                val column =
                                    arrayOf("details", "latitude", "longitude")
                                categoryCursor = mobeegalDatabase!!
                                    .query(
                                        "mStuffdetails", column,
                                        "catagory='Cars'", null, null,
                                        null,
                                        null
                                    )
                                val detailsColumncars = categoryCursor!!
                                    .getColumnIndexOrThrow("details")
                                val latitudeColumncars = categoryCursor!!
                                    .getColumnIndexOrThrow("latitude")
                                val longitudeColumncars = categoryCursor!!
                                    .getColumnIndexOrThrow("longitude")
                                if (categoryCursor != null) {
                                    index = 0
                                    if (categoryCursor!!.isFirst) {
                                        do {
                                            val detailsdating =
                                                categoryCursor!!.getString(
                                                    detailsColumncars
                                                )
                                            val latitudedating = categoryCursor!!
                                                .getInt(latitudeColumncars)
                                            val longitudedating = categoryCursor!!
                                                .getInt(longitudeColumncars)
                                            mstufftextArray!![index] =
                                                MstuffText(
                                                    detailsdating +
                                                        ", Latitude = " +
                                                        Integer.toString(
                                                            latitudedating
                                                        ) +
                                                        ", Longitude = " +
                                                        Integer.toString(
                                                            longitudedating
                                                        ),
                                                    resources.getDrawable(
                                                        R.drawable.cars_icon
                                                    )
                                                )
                                            btla!!.addItem(
                                                mstufftextArray!![index]!!
                                            )
                                            index++
                                        } while (categoryCursor!!.moveToNext())
                                    }
                                    break
                                }
                            }
                        }
                        count++
                    } while (c!!.moveToNext())
                    Toast.makeText(
                        this@MstuffTextSearchResults, "$index" +
                            " Matches found out of $rows mStuffs ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@MstuffTextSearchResults, "Error",
                Toast.LENGTH_LONG
            ).show()
        }

        listAdapter = btla
        listView.onItemSelectedListener = this
        listView.onItemClickListener = this
    }

    inner class MstuffText(text: String?, bullet: Drawable?) {

        private var mText: String? = text
        private var mBullet: Drawable? = bullet
        private var mSelectable = true

        fun isSelectable(): Boolean {
            return mSelectable
        }

        fun setSelectable(selectable: Boolean) {
            mSelectable = selectable
        }

        fun getText(): String? {
            return mText
        }

        fun setText(text: String?) {
            mText = text
        }

        fun setBullet(bullet: Drawable?) {
            mBullet = bullet
        }

        fun getBullet(): Drawable? {
            return mBullet
        }
    }

    inner class MstuffTextListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context
        private var mItems: MutableList<MstuffText> = ArrayList()

        fun addItem(bt: MstuffText) {
            mItems.add(bt)
        }

        fun setListItems(bti: MutableList<MstuffText>) {
            mItems = bti
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

        fun isSelectable(position: Int): Boolean {
            return mItems[position].isSelectable()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val btv: MstuffTextView
            if (convertView == null) {
                btv = MstuffTextView(
                    mContext,
                    mItems[position].getText(),
                    mItems[position].getBullet()
                )
            } else {
                btv = convertView as MstuffTextView
                btv.setText(mItems[position].getText())
                btv.setBullet(mItems[position].getBullet())
            }
            return btv
        }
    }

    inner class MstuffTextView(
        context: Context,
        text: String?,
        bullet: Drawable?
    ) : LinearLayout(context) {

        private val mText: TextView
        private val mBullet: ImageView

        init {
            this.orientation = HORIZONTAL
            mBullet = ImageView(context)
            mBullet.setImageDrawable(bullet)
            // left, top, right, bottom
            mBullet.setPadding(0, 2, 5, 0)
            addView(
                mBullet, LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
            )
            mText = TextView(context)
            mText.text = text
            addView(
                mText, LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
            )
        }

        fun setText(words: String?) {
            mText.text = words
        }

        fun setBullet(bullet: Drawable?) {
            mBullet.setImageDrawable(bullet)
        }
    }

    override fun onItemClick(parent1: AdapterView<*>?, v: View?, position: Int, id: Long) {
        listView.setSelection(position)
    }

    override fun onItemSelected(
        parent: AdapterView<*>?, v: View?, position: Int,
        id: Long
    ) {
        selectedposition = parent!!.selectedItemPosition
    }

    override fun onNothingSelected(arg0: AdapterView<*>?) {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsSearchMenu(menu)
        return true
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent = Intent(
                    this@MstuffTextSearchResults, MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@MstuffTextSearchResults,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings = Intent(
                    this@MstuffTextSearchResults,
                    Settings::class.java
                )
                startActivity(settings)
            }
            4 -> {
                val mStuffSearchIntent = Intent(
                    this@MstuffTextSearchResults,
                    MstuffSearch::class.java
                )
                val searchBundle = Bundle()
                searchBundle.putString("TextView", "TextView")
                mStuffSearchIntent.putExtras(searchBundle)
                startActivityForResult(mStuffSearchIntent, 0)
                startActivity(mStuffSearchIntent)
            }
            5 -> {
                val intent =
                    Intent(this@MstuffTextSearchResults, Chat::class.java)
                val b = Bundle()
                b.putString("mstuffid", mStuffid!![selectedposition])
                intent.putExtras(b)
                startActivityForResult(intent, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
