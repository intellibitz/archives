package com.androidworks.navsys.wuffit.activity

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidworks.navsys.wuffit.R
import com.androidworks.navsys.wuffit.WuffITApplication
import com.androidworks.navsys.wuffit.content.Tracker

class SelectTracker : AppCompatActivity() {

    private var footerCountText: TextView? = null
    private var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_tracker)

        listView = findViewById(android.R.id.list)
        val emptyView = findViewById<View>(android.R.id.empty)
        listView?.emptyView = emptyView

        findViewById<View>(R.id.pick_tracker)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, PICK_CONTACT_REQUEST)
        }

        findViewById<View>(R.id.sync_tracker)?.setOnClickListener {
            (application as? WuffITApplication)?.syncTrackers()
            updateTrackersOnView()
        }

        findViewById<View>(R.id.main_menu)?.setOnClickListener {
            val intent = Intent("com.androidworks.navsys.wuffit.ACTION_MAIN_MENU").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            startActivity(intent)
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val footer = inflater.inflate(R.layout.footer_view, null)
        footerCountText = footer.findViewById(R.id.text2)
        listView?.addFooterView(footer)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST) {
            val dataUri = data?.dataString
            if (dataUri == null) {
                Toast.makeText(this, "No Contacts Picked.. Try again", Toast.LENGTH_LONG).show()
            } else {
                Log.d(TAG, dataUri)
                (application as? WuffITApplication)?.storeTrackerId(dataUri)
                updateTrackersOnView()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateTrackersOnView()
    }

    private fun updateTrackersOnView() {
        val cursor = contentResolver.query(
            Tracker.Details.CONTENT_URI,
            arrayOf(Tracker.Details._ID, Tracker.Details.NAME, Tracker.Details.NUMBER),
            null,
            null,
            null
        ) ?: return

        val simpleCursorAdapter = TrackerSimpleAdapter(
            this,
            R.layout.select_tracker_item,
            cursor,
            arrayOf(Tracker.Details.NAME, Tracker.Details.NUMBER),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        footerCountText?.text = "(${cursor.count})"
        listView?.adapter = simpleCursorAdapter
    }

    inner class TrackerSimpleAdapter(
        context: Context,
        layout: Int,
        cursor: Cursor,
        from: Array<String>,
        to: IntArray
    ) : SimpleCursorAdapter(context, layout, cursor, from, to, 0) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val deleteBtn = view.findViewById<View>(R.id.delete)
            val currentCursor = cursor
            currentCursor.moveToPosition(position)
            val id = currentCursor.getString(0)
            deleteBtn?.tag = id
            deleteBtn?.setOnClickListener { v ->
                val tagId = v.tag as? String
                Log.d(TAG, "Delete id: $tagId")
                if (tagId != null) {
                    (application as? WuffITApplication)?.deleteTracker(tagId)
                    updateTrackersOnView()
                }
            }
            return view
        }
    }

    companion object {
        private const val TAG = "SelectTracker"
        private const val PICK_CONTACT_REQUEST = 1
    }
}
