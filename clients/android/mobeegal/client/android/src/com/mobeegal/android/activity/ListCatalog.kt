package com.mobeegal.android.activity

/*
<!--
$Id:: ListCatalog.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.mobeegal.android.activity.catalogs.Dating
import com.mobeegal.android.util.ViewMenu

class ListCatalog : ListActivity() {

    var gettingcategory: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        val bundles = this.intent.extras
        if (bundles != null) {
            gettingcategory = bundles.getString("passingcatalog")
        }
        listAdapter = SpeechListAdapter(this)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        (listAdapter as SpeechListAdapter).toggle(position)
    }

    private inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context
        private val mTitles = arrayOf(gettingcategory)
        private val mExpanded = booleanArrayOf(true)

        /* How many items are in the data set represented by this Adapter. */
        override fun getCount(): Int {
            return mTitles.size
        }

        /*
         * Get the data item associated with the specified position in the data
         * set.
         */
        override fun getItem(position: Int): Any {
            return position
        }

        /* Get the row id associated with the specified position in the list. */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val sv: SpeechView
            if (convertView == null) {
                sv = SpeechView(
                    mContext, mTitles[position], null,
                    mExpanded[position]
                )
            } else {
                sv = convertView as SpeechView
                sv.setTitle(mTitles[position])
                sv.setExpanded(mExpanded[position])
            }
            return sv
        }

        fun toggle(position: Int) {
            mExpanded[position] = !mExpanded[position]
            notifyDataSetChanged()
        }
    }

    private inner class SpeechView(
        context: Context, title: String?, dialogue: String?,
        expanded: Boolean
    ) : LinearLayout(context) {
        private val mButton: Button
        private val mTitle: TextView

        init {
            this.orientation = VERTICAL
            mTitle = TextView(context)
            addView(
                mTitle, LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT
                )
            )
            mButton = Button(context)
            mButton.text = "Dating"
            addView(
                mButton, LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
            )
            mButton.visibility = if (expanded) VISIBLE else GONE
            mButton.setOnClickListener {
                val mI = Intent(this@ListCatalog, Dating::class.java)
                startActivity(mI)
            }
        }

        fun setTitle(title: String?) {
            mTitle.text = title
        }

        fun setExpanded(expanded: Boolean) {
            mButton.visibility = if (expanded) VISIBLE else GONE
        }
    }

    //	MenuView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    //	Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                //			mStuff Menu
                val stuffCheckintent =
                    Intent(this@ListCatalog, MapResults::class.java)
                startActivityForResult(stuffCheckintent, 0)
                finish()
            }
            2 -> {
                val intent1 =
                    Intent(this@ListCatalog, FindandInstall::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }
            3 -> {
                val settings = Intent(this@ListCatalog, Settings::class.java)
                startActivityForResult(settings, 0)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
