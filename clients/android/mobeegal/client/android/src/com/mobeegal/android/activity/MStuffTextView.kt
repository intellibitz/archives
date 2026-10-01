package com.mobeegal.android.activity

/*
<!--
$Id:: MStuffTextView.java 14 2008-08-19 06:36:45Z muthu.ramadoss             $: Id of last commit
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

class MStuffTextView : ListActivity(), OnItemSelectedListener, OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var mstuffCursor: Cursor? = null
    var rows: Int = 0
    var count: Int = 0
    var mstuffid: Array<String?>? = null
    var catagory: Array<String?>? = null
    var details: Array<String?>? = null
    var latitude: IntArray? = null
    var longitude: IntArray? = null
    var location: Array<String?>? = null
    var mstufftextArray: Array<MstuffText?>? = null
    var btla: MstuffTextListAdapter? = null
    var selectedposition: Int = 0

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        try {
            myDatabase = this@MStuffTextView.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val columnName = arrayOf(
                "mstuffid", "catagory", "details",
                "latitude", "longitude", "location"
            )
            mstuffCursor = myDatabase!!.query(
                "mStuffdetails", columnName,
                null, null, null, null, null
            )
            rows = mstuffCursor!!.count

            mstuffid = arrayOfNulls(rows)
            catagory = arrayOfNulls(rows)
            details = arrayOfNulls(rows)
            latitude = IntArray(rows)
            longitude = IntArray(rows)
            location = arrayOfNulls(rows)

            val idcolumn = mstuffCursor!!.getColumnIndexOrThrow("mstuffid")
            val catagoryColumn = mstuffCursor!!.getColumnIndexOrThrow("catagory")
            val detailsColumn = mstuffCursor!!.getColumnIndexOrThrow("details")
            val latitudeColumn = mstuffCursor!!.getColumnIndexOrThrow("latitude")
            val longitudeColumn =
                mstuffCursor!!.getColumnIndexOrThrow("longitude")
            val locationColumn = mstuffCursor!!.getColumnIndexOrThrow("location")

            if (mstuffCursor != null) {
                count = 0
                if (mstuffCursor!!.isFirst) {
                    do {
                        mstuffid!![count] = mstuffCursor!!.getString(idcolumn)
                        catagory!![count] =
                            mstuffCursor!!.getString(catagoryColumn)
                        details!![count] = mstuffCursor!!.getString(detailsColumn)
                        latitude!![count] = mstuffCursor!!.getInt(latitudeColumn)
                        longitude!![count] = mstuffCursor!!.getInt(longitudeColumn)
                        location!![count] =
                            mstuffCursor!!.getString(locationColumn)
                        count++
                    } while (mstuffCursor!!.moveToNext())
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this@MStuffTextView, "Sorry, No matches found",
                Toast.LENGTH_SHORT
            ).show()
        }
        btla = MstuffTextListAdapter(this)
        mstufftextArray = arrayOfNulls(rows)

        for (i in 0 until rows) {
            if (catagory!![i].equals("Dating", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.dating_icon)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("Matrimony", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.matrimony_icon)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("Cars", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.cars_icon)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("Jewelry", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.jewelry_icon)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("Restaurants", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.restaurant_icon)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("Movies", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.movies_icon)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("Rental", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.rental_icon)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("Marker", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.marker)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("userDating", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.user)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("userMatrimony", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.user)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("userCars", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.user)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("userJewelry", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.user)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("userRestaurants", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.user)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("userMovies", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.user)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
            if (catagory!![i].equals("userRental", ignoreCase = true)) {
                mstufftextArray!![i] = MstuffText(
                    details!![i] +
                        ", Latitude = " + latitude!![i] + ", Longitude = " +
                        longitude!![i],
                    resources.getDrawable(R.drawable.user)
                )
                btla!!.addItem(mstufftextArray!![i]!!)
            }
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

    override fun onItemSelected(
        parent: AdapterView<*>?, v: View?, position: Int,
        id: Long
    ) {
        selectedposition = parent!!.selectedItemPosition
    }

    override fun onNothingSelected(arg0: AdapterView<*>?) {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsTextMenu(menu)
        return true
    }

    override fun onItemClick(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
        listView.setSelection(position)
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent =
                    Intent(this@MStuffTextView, MapResults::class.java)
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 =
                    Intent(this@MStuffTextView, FindandInstall::class.java)
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@MStuffTextView, Settings::class.java)
                startActivity(settings)
            }
            4 -> {
                try {
                    myDatabase =
                        this@MStuffTextView.openOrCreateDatabase(
                            "Mobeegal",
                            Context.MODE_PRIVATE, null
                        )
                    myDatabase!!.execSQL(
                        "CREATE TABLE IF NOT EXISTS " +
                            "Favourite" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);"
                    )
                    Toast.makeText(
                        this@MStuffTextView, "Added to Favorite ",
                        Toast.LENGTH_LONG
                    ).show()
                    val c = myDatabase!!.query(
                        "mStuffdetails", null,
                        "mstuffid='" + mstuffid!![selectedposition] + "'",
                        null, null, null, null
                    )

                    val idcolumn = c.getColumnIndexOrThrow("mstuffid")
                    val categorycolumn = c.getColumnIndexOrThrow("catagory")
                    val detailsColumn = c.getColumnIndexOrThrow("details")
                    val latitudeColumn = c.getColumnIndexOrThrow("latitude")
                    val longitudeColumn = c.getColumnIndexOrThrow("longitude")
                    val locationColumn = c.getColumnIndexOrThrow("location")
                    if (c != null) {
                        if (c.isFirst) {
                            val mstuffidForFavorite = c.getString(idcolumn)
                            val categoryForFavorite =
                                c.getString(categorycolumn)
                            val detailsForFavorite =
                                c.getString(detailsColumn)
                            val latitudeForFavorite = c.getInt(latitudeColumn)
                            val longitudeForFavorite =
                                c.getInt(longitudeColumn)
                            val locationForFavorite =
                                c.getString(locationColumn)
                            myDatabase!!.execSQL(
                                "INSERT INTO " + "Favourite" +
                                    "  (mstuffid, catagory,  details, latitude, longitude, location)" +
                                    " VALUES ('" + mstuffidForFavorite +
                                    "', '" + categoryForFavorite + "', '" +
                                    detailsForFavorite + "'," +
                                    latitudeForFavorite + "," +
                                    longitudeForFavorite + ",'" +
                                    locationForFavorite + "');"
                            )
                        }
                    }
                    c.close()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MStuffTextView, "Database Not Found...",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    if (myDatabase != null) {
                        myDatabase!!.close()
                    }
                }
            }
            5 -> {
                try {
                    myDatabase =
                        this@MStuffTextView.openOrCreateDatabase(
                            "Mobeegal",
                            Context.MODE_PRIVATE, null
                        )
                    val c = myDatabase!!.query(
                        "mStuffdetails", null,
                        "mstuffid='" + mstuffid!![selectedposition] + "'",
                        null, null, null, null
                    )
                    if (c.count > 0) {
                        myDatabase = this@MStuffTextView
                            .openOrCreateDatabase(
                                "Mobeegal",
                                Context.MODE_PRIVATE, null
                            )
                        myDatabase!!.delete(
                            "mStuffdetails",
                            "mStuffId='" + mstuffid!![selectedposition] + "'",
                            null
                        )
                        Toast.makeText(
                            this@MStuffTextView,
                            "Ignored the selected match from your mStuff",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(
                            Intent(
                                this@MStuffTextView,
                                MStuffTextView::class.java
                            )
                        )
                        finish()
                    } else {
                        Toast.makeText(
                            this@MStuffTextView,
                            "Sorry, no matches to Ignore",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    c.close()
                } catch (ex: Exception) {
                    Toast.makeText(
                        this@MStuffTextView, "Database Not Found...",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    if (myDatabase != null) {
                        myDatabase!!.close()
                    }
                }
            }
            6 -> {
                myDatabase!!.execSQL("update preferences set views='MapView'")
                val mStuffTextView =
                    Intent(this@MStuffTextView, MapResults::class.java)
                startActivity(mStuffTextView)
            }
            7 -> {
                val mStuffsearch =
                    Intent(this@MStuffTextView, MstuffSearch::class.java)
                val searchBundle = Bundle()
                searchBundle.putString("TextView", "TextView")
                mStuffsearch.putExtras(searchBundle)
                startActivityForResult(mStuffsearch, 0)
            }
            8 -> {
                val mStuffchat = Intent(this@MStuffTextView, Chat::class.java)
                val chat = Bundle()
                chat.putString("mstuffid", mstuffid!![selectedposition])
                mStuffchat.putExtras(chat)
                startActivityForResult(mStuffchat, 0)
            }
            9 -> {
                val mediaintent =
                    Intent(this@MStuffTextView, ViewMedia::class.java)
                startActivity(mediaintent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
