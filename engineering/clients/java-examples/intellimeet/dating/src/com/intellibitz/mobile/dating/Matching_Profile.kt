package com.intellibitz.mobile.dating

import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class Matching_Profile : ListActivity() {

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        Toast.makeText(this, "The following Dream-mates matches your profile.", Toast.LENGTH_LONG).show()
        listAdapter = SpeechListAdapter(this)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        (listAdapter as SpeechListAdapter).toggle(position)
    }

    private inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context
        private val mTitles = arrayOf(
            "23",
            "18",
            "19",
            "21",
            "22",
            "23",
            "24",
            "19"
        )
        private val mDialogue = arrayOf(
            "FEMALE,23,155 cm,55Kg,T.NAGAR.",
            "FEMALE,18,156 cm,56Kg,T.NAGAR.",
            "FEMALE,19,157 cm, 57Kg,T.NAGAR.",
            "FEMALE,21,158 cm, 45Kg,T.NAGAR.",
            "FEMALE,22,145 cm, 50Kg,T.NAGAR.",
            "FEMALE,23,165 cm, 55Kg,T.NAGAR.",
            "FEMALE,24,160 cm, 52Kg,T.NAGAR.",
            "FEMALE,19,158 cm, 60Kg,T.NAGAR."
        )
        private var mI: Intent? = null

        private val mExpanded = booleanArrayOf(
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        )

        /* How many items are in the data set represented by this Adapter.*/
        override fun getCount(): Int {
            return mTitles.size
        }

        /* Get the data item associated with the specified position in the data set. */
        override fun getItem(position: Int): Any {
            return position
        }

        /*Get the row id associated with the specified position in the list. */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val sv: SpeechView
            if (convertView == null) {
                sv = SpeechView(mContext, mTitles[position], mDialogue[position], mExpanded[position])
            } else {
                sv = convertView as SpeechView
                sv.setTitle(mTitles[position])
                sv.setDialogue(mDialogue[position])
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
        context: Context,
        title: String,
        dialogue: String,
        expanded: Boolean
    ) : LinearLayout(context) {
        private val mButton: Button
        private val mTitle: TextView
        private val mDialogue: TextView

        init {
            this.orientation = VERTICAL
            mTitle = TextView(context)
            mTitle.text = title

            addView(mTitle, LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT))
            mDialogue = TextView(context)
            mDialogue.text = dialogue
            addView(mDialogue, LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT))
            mDialogue.visibility = if (expanded) VISIBLE else GONE
            mButton = Button(context)
            mButton.text = "chat"
            addView(mButton, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            mButton.visibility = if (expanded) VISIBLE else GONE
            mButton.setOnClickListener {
                val mI = Intent(this@Matching_Profile, Chat::class.java)
                startActivity(mI)
            }
        }

        fun setTitle(title: String) {
            mTitle.text = title
        }

        fun setDialogue(words: String) {
            mDialogue.text = words
        }

        fun setExpanded(expanded: Boolean) {
            mDialogue.visibility = if (expanded) VISIBLE else GONE
            mButton.visibility = if (expanded) VISIBLE else GONE
        }
    }
}
