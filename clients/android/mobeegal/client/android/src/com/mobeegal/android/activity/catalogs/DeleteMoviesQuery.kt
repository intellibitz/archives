package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: DeleteMoviesQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss          $: Id of last commit
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

class DeleteMoviesQuery : ListActivity(), AdapterView.OnItemClickListener {

    var myDatabase: SQLiteDatabase? = null
    var c: Cursor? = null
    var rows = 0
    var count = 0
    var size = 15
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
    var queryStatus = arrayOfNulls<String>(size)
    var MovieId = IntArray(size)

    //String selectid;
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        listAdapter = SpeechListAdapter(this)
        listView.onItemClickListener = this
    }

    inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context

        init {
            myDatabase = mContext.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val myCols = arrayOf(
                "key", "imovietype", "imovielanguage",
                "iseatingstyle", "icountry", "icity", "iarea", "umovietype",
                "umovielanguage", "useatingstyle", "ucountry", "ucity",
                "uarea", "queryStatus"
            )
            c = myDatabase!!.query("Movies", myCols, null, null, null, null, null)
            rows = c!!.count

            val idcolumn = c!!.getColumnIndexOrThrow("key")
            val imovietypeColumn = c!!.getColumnIndexOrThrow("imovietype")
            val imovielanguageColumn =
                c!!.getColumnIndexOrThrow("imovielanguage")
            val imiscColumn = c!!.getColumnIndexOrThrow("iseatingstyle")
            val icountryColumn = c!!.getColumnIndexOrThrow("icountry")
            val icityColumn = c!!.getColumnIndexOrThrow("icity")
            val iareaColumn = c!!.getColumnIndexOrThrow("iarea")

            val umovietypeColumn = c!!.getColumnIndexOrThrow("umovietype")
            val umovielanguageColumn =
                c!!.getColumnIndexOrThrow("umovielanguage")
            val umiscColumn = c!!.getColumnIndexOrThrow("useatingstyle")
            val ucountryColumn = c!!.getColumnIndexOrThrow("ucountry")
            val ucityColumn = c!!.getColumnIndexOrThrow("ucity")
            val uareaColumn = c!!.getColumnIndexOrThrow("uarea")
            val querystatuscolumn = c!!.getColumnIndexOrThrow("querystatus")

            if (c != null) {
                // count = 0;
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
                    mContext, " MovieType = " +
                        iStuffMovieType[position] + "MovieLanguage = " +
                        iStuffMovieLanguage[position] + " Misc = " +
                        iStuffSeatingStyle[position] + " Country = " +
                        iStuffcountry[position] + " Area = " +
                        iStuffarea[position] + " City = " +
                        iStuffcity[position],
                    " uMovieType = " + uStuffMovieType[position] +
                        "uMovieLanguage = " +
                        uStuffMovieLanguage[position] + " uMisc = " +
                        uStuffSeatingStyle[position] + " uCountry= " +
                        uStuffcountry[position] + " Area = " +
                        uStuffarea[position] + " City = " +
                        uStuffcity[position] + " QueryStatus = " +
                        queryStatus[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(iStuffMovieType[position])
                sv.setDialogue(uStuffarea[position])
            }
            return sv
        }

        fun onItemClick(arg0: AdapterView<*>?, arg1: View?, arg2: Int, arg3: Long) {
        }
    }

    inner class SpeechView(context: Context, title: String?, words: String?) : LinearLayout(context) {

        private val mTitle: TextView
        private val mDialogue: TextView

        init {
            this.orientation = VERTICAL
            mTitle = TextView(context)
            mTitle.text = "IStuff : " + title
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )

            mDialogue = TextView(context)
            mDialogue.text = "UStuff : " + words
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
        val okButtonListener = object : DialogInterface.OnClickListener {
            override fun onClick(arg0: DialogInterface?, arg1: Int) {
                try {
                    myDatabase = this@DeleteMoviesQuery
                        .openOrCreateDatabase(
                            "Mobeegal",
                            Context.MODE_PRIVATE, null
                        )
                    myDatabase!!.delete("Movies", "key=" + MovieId[selectedId], null)
                    if (selectedId == 0 && MovieId[0] != 0) {
                        myDatabase!!.delete("Movies", "key=" + MovieId[0], null)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@DeleteMoviesQuery, "Error",
                        Toast.LENGTH_LONG
                    ).show()
                }
                val intent = Intent(
                    this@DeleteMoviesQuery,
                    DeleteMoviesQuery::class.java
                )
                startActivity(intent)
                finish()
            }
        }
        val cancelButtonListener = object : DialogInterface.OnClickListener {
            // @Override
            override fun onClick(arg0: DialogInterface?, arg1: Int) {
                // Do nothing
            }
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
                    this@DeleteMoviesQuery,
                    MapResults::class.java
                )
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(
                    this@DeleteMoviesQuery,
                    FindandInstall::class.java
                )
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@DeleteMoviesQuery, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
