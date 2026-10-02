package com.intellibitz.mobile.dating

import android.app.AlarmManager
import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.io.FileNotFoundException

class MatchingProfile : ListActivity() {

    var myDatabase: SQLiteDatabase? = null
    var profile1: String? = null
    var profile2: String? = null
    var profile3: String? = null
    var profile4: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        try {
            this.createDatabase("Matching", 1, MODE_PRIVATE, null)
            myDatabase = this.openDatabase("Matching", null)
        } catch (e: FileNotFoundException) {
        }
        Toast.makeText(this, "The following Dream-mates matches your profile.", Toast.LENGTH_LONG).show()
        val intobject1 = Intent(this@MatchingProfile, RepeatingAlarm::class.java)
        var firstTime = SystemClock.elapsedRealtime()
        firstTime += (10 * 1000).toLong()

        // Schedule the alarm!
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        am.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            firstTime, (10 * 1000).toLong(), intobject1
        )
        setTheme(android.R.style.Theme_Dialog)
        listAdapter = SpeechListAdapter(this)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        (listAdapter as SpeechListAdapter).toggle(position)
    }

    private inner class SpeechListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context
        private val mTitles = arrayOf(
            "SWEETY",
            "BABLI",
            "LOLO",
            "AISH"
        )
        private val mDialogue = arrayOf(
            run {
                profile1 = "FEMALE,23,155 cm,55Kg,St.ThomasMount"
                profile1!!
            },
            run {
                profile2 = "FEMALE,18,156 cm,56Kg,TNagar-Renganathan Street"
                profile2!!
            },
            run {
                profile3 = "FEMALE,19,157 cm, 57Kg,TNagar-Panagalpark."
                profile3!!
            },
            run {
                profile4 = "FEMALE,21,158 cm, 45Kg,Adyar."
                profile4!!
            }
        )
        private val mExpanded = booleanArrayOf(
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

            addView(mButton, LayoutParams(50, 35))
            mButton.visibility = if (expanded) VISIBLE else GONE
            mButton.setOnClickListener {
                myDatabase!!.execSQL(
                    "CREATE TABLE IF NOT EXISTS Profile" +
                        "(match1 VARCHAR, match2 VARCHAR, match3 VARCHAR, match4 VARCHAR);"
                )
                myDatabase!!.execSQL(
                    "INSERT INTO Profile(match1,match2,match3,match4)" +
                        " VALUES  ('" + profile1 + "','" + profile2 + "','" + profile3 + "','" + profile4 + "');"
                )
                val intent = Intent(this@MatchingProfile, RepeatingAlarm::class.java)
                val am = getSystemService(ALARM_SERVICE) as AlarmManager
                am.cancel(intent)
                val mI = Intent(this@MatchingProfile, Chat::class.java)
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
