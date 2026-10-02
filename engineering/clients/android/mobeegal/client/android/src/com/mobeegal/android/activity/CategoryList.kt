package com.mobeegal.android.activity

/*
<!--
$Id:: CategoryList.java 14 2008-08-19 06:36:45Z muthu.ramadoss                  $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity
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
import android.widget.Button
import android.widget.Gallery
import android.widget.GridView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.activity.catalogs.CarsViewQuery
import com.mobeegal.android.activity.catalogs.HomeViewQuery
import com.mobeegal.android.activity.catalogs.Jewelryviewquery
import com.mobeegal.android.activity.catalogs.Matrimonyviewquery
import com.mobeegal.android.activity.catalogs.MoviesViewQuery
import com.mobeegal.android.activity.catalogs.RestaurantsViewQuery
import com.mobeegal.android.activity.catalogs.ViewQuery
import com.mobeegal.android.util.ViewMenu
import java.util.logging.Logger

class CategoryList : Activity() {

    var myDatabase: SQLiteDatabase? = null
    var categoryname1: String? = null
    var b: Int = 0
    var n: Int = 0
    var d: Int = 0
    var categoryname: Int = 0
    var getcountcatname: Int = 0
    var getcountcatname1: Int = 0
    var getcatalogID: Int = 0
    var size: Int = 0
    var c2: Cursor? = null
    var c3: Cursor? = null
    var position: Int = 0
    var res1 = arrayOfNulls<String>(50)
    var res3 = arrayOfNulls<String>(50)

    companion object {
        private val logger: Logger = Logger.getLogger("categorylist")
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.categorylist)
        val categoryBundle = intent.extras
        if (categoryBundle != null) {
            position = categoryBundle.getInt("position")
        }
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val column = arrayOf("modes")
            c2 = myDatabase!!.query(
                "category", column, "status='true'",
                null, null, null, "modes"
            )
            categoryname = c2!!.getColumnIndexOrThrow("modes")
            getcountcatname = c2!!.count
            logger.info("Modes :$getcountcatname")
            if (c2 != null) {
                b = 0
                if (c2!!.isFirst) {
                    do {
                        categoryname1 = c2!!.getString(categoryname)
                        logger.info("Modes :$categoryname1")
                        res1[b] = categoryname1
                        b++
                    } while (c2!!.moveToNext())
                }
            }
            val listcat = arrayOf("categoryname", "categoryID")
//for(int j) {
            c3 = myDatabase!!.query(
                "category", listcat,
                "status='true' and categoryID=$position", null, null,
                null, null
            )

            categoryname = c3!!.getColumnIndexOrThrow("categoryname")
            getcatalogID = c3!!.getColumnIndexOrThrow("categoryID")
            getcountcatname1 = c3!!.count
            if (c3 != null) {
                d = 0
                if (c3!!.isFirst) {
                    do {
                        logger.info("position :$position")
                        if (position == c3!!.getInt(getcatalogID)) {
                            size++
                        }
                        categoryname1 = c3!!.getString(categoryname)
                        logger.info("catalog name :$categoryname1")
                        res3[d] = categoryname1
                        d++
                    } while (c3!!.moveToNext())
                }
            }
            //       }
            val g = findViewById(R.id.myGrid1) as GridView
            if (c3!!.count == 0) {
                Toast.makeText(
                    this@CategoryList,
                    "Sorry, No category subscribed", Toast.LENGTH_LONG
                ).show()
            } else {
                g.adapter = ImageAdapter(this)
            }
            c3!!.close()
            c2!!.close()
        } catch (e: Exception) {
            Toast.makeText(this@CategoryList, "" + e, Toast.LENGTH_LONG).show()
        }
    }

    inner class ImageAdapter(c: Context) : BaseAdapter() {

        private val mContext: Context = c

        override fun getCount(): Int {
            return size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(
            position: Int, convertView: View?,
            parent: ViewGroup?
        ): View {
            for (nn in size downTo 1) {
                n = nn
                val categorybutton = Button(mContext)
                categorybutton.layoutParams = Gallery.LayoutParams(110, 50)
                categorybutton.setPadding(10, 10, 11, 13)
                categorybutton.text = res3[position]

                categorybutton.setOnClickListener {
                    try {
                        if (res3[position].equals("Dating", ignoreCase = true)) {
                            val category1 =
                                Intent(mContext, ViewQuery::class.java)
                            startActivityForResult(category1, 0)
                            // finish();
                        } else if (res3[position].equals("Matrimony", ignoreCase = true)) {
                            val category2 = Intent(
                                mContext,
                                Matrimonyviewquery::class.java
                            )
                            startActivityForResult(category2, 0)
                            // finish();
                        } else if (res3[position].equals("Jewelry", ignoreCase = true)) {
                            val category3 = Intent(
                                mContext,
                                Jewelryviewquery::class.java
                            )
                            startActivityForResult(category3, 0)
                            //finish();
                        } else if (res3[position].equals("Restaurants", ignoreCase = true)) {
                            val category3 = Intent(
                                mContext,
                                RestaurantsViewQuery::class.java
                            )
                            startActivityForResult(category3, 0)
                            //finish();
                        } else if (res3[position].equals("Movies", ignoreCase = true)) {
                            val category3 = Intent(
                                mContext,
                                MoviesViewQuery::class.java
                            )
                            startActivityForResult(category3, 0)
                        } else if (res3[position].equals("Rental", ignoreCase = true)) {
                            val category3 = Intent(
                                mContext,
                                HomeViewQuery::class.java
                            )
                            startActivity(category3)
                            //finish();
                        } else if (res3[position].equals("Cars", ignoreCase = true)) {
                            val category3 = Intent(
                                mContext,
                                CarsViewQuery::class.java
                            )
                            startActivityForResult(category3, 0)
                            //finish();
                        } else {
                            Toast.makeText(
                                this@CategoryList,
                                "No Category Profile Found",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@CategoryList, "" + e,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                return categorybutton
            }
            return parent!!
        }
    }

    // MenuView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    // Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent = Intent(
                    this@CategoryList,
                    MapResults::class.java
                )
                startActivityForResult(stuffCheckintent, 0)
                finish()
            }
            2 -> {
                val intent1 = Intent(
                    this@CategoryList,
                    FindandInstall::class.java
                )
                startActivityForResult(intent1, 0)
                finish()
            }
            3 -> {
                val settings = Intent(this@CategoryList, Settings::class.java)
                startActivityForResult(settings, 0)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
