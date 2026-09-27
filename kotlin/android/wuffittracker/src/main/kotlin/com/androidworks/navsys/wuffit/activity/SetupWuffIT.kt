package com.androidworks.navsys.wuffit.activity

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.os.Bundle
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.androidworks.navsys.wuffit.R
import com.androidworks.navsys.wuffit.content.Tracker
import com.androidworks.navsys.wuffit.service.SMSHandler
import java.text.SimpleDateFormat
import java.util.Calendar

class SetupWuffIT : AppCompatActivity() {

    private var msgView: TextView? = null
    private var editmsgView: EditText? = null
    private var setupView: View? = null
    private var footerCountText: TextView? = null
    private var listView: ListView? = null

    private var isBound = false
    private var smsHandler: SMSHandler? = null

    private val smsHandlerConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            smsHandler = (service as SMSHandler.LocalBinder).service
        }

        override fun onServiceDisconnected(className: ComponentName) {
            smsHandler = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_wuffit)

        listView = findViewById(android.R.id.list)
        val emptyView = findViewById<View>(android.R.id.empty)
        listView?.emptyView = emptyView

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val tel = try {
            @Suppress("DEPRECATION")
            telephonyManager?.line1Number
        } catch (e: SecurityException) {
            Log.w(TAG, "Phone number permission not granted: ${e.message}")
            null
        }

        msgView = findViewById(R.id.wuffit_message_text)
        editmsgView = findViewById(R.id.wuffit_message_edittext)

        val currentMsg = msgView?.text?.toString() ?: ""
        if (tel != null) {
            msgView?.text = currentMsg.replaceFirst("@", tel)
        }
        editmsgView?.setText(msgView?.text)

        findViewById<View>(R.id.edit)?.setOnClickListener {
            findViewById<View>(R.id.wuffit_message_edit)?.visibility = View.VISIBLE
            findViewById<View>(R.id.wuffit_message)?.visibility = View.GONE
        }

        val save = findViewById<ImageButton>(R.id.save)
        save?.setOnClickListener {
            findViewById<View>(R.id.wuffit_message_edit)?.visibility = View.GONE
            findViewById<View>(R.id.wuffit_message)?.visibility = View.VISIBLE
        }

        editmsgView?.setOnKeyListener { _, _, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                msgView?.text = editmsgView?.text
            }
            false
        }

        editmsgView?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                save?.performClick()
            }
        }

        setupView = findViewById(R.id.wuffit_setup)
        setupView?.setOnClickListener {
            showSetupAllDialog()
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

        bindService(Intent(this, SMSHandler::class.java), smsHandlerConnection, Context.BIND_AUTO_CREATE)
        isBound = true
    }

    override fun onResume() {
        super.onResume()
        updateTrackersOnView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(smsHandlerConnection)
            isBound = false
        }
    }

    private fun showSetupAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Setup all Trackers")
            .setMessage("This will send SMS to all available trackers. Do you want to continue?")
            .setCancelable(true)
            .setPositiveButton("Ok") { _, _ ->
                val msg = msgView?.text?.toString() ?: ""
                smsHandler?.setupWuffIT(msg)
                updateTrackersOnView()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateTrackersOnView() {
        val cursor = contentResolver.query(
            Tracker.Details.CONTENT_URI,
            arrayOf(Tracker.Details._ID, Tracker.Details.NAME, Tracker.Details.NUMBER),
            null,
            null,
            null
        ) ?: return

        val sz = cursor.count
        footerCountText?.text = "($sz)"
        setupView?.isEnabled = sz > 0

        val setupCursor = contentResolver.query(
            Tracker.Setup.CONTENT_URI,
            arrayOf(Tracker.Setup._FID, Tracker.Setup.SETUP_TIME, Tracker.Setup.REPLY_TIME),
            null,
            null,
            null
        )

        val trackerAdapter = TrackerAdapter(
            this,
            R.layout.setup_tracker_item,
            cursor,
            arrayOf(Tracker.Details.NAME, Tracker.Details.NUMBER, Tracker.Details.NAME, Tracker.Details.NUMBER),
            intArrayOf(android.R.id.text1, android.R.id.text2, R.id.text3, R.id.text4)
        ).apply {
            this.setupCursor = setupCursor
        }
        listView?.adapter = trackerAdapter
    }

    inner class TrackerAdapter(
        context: Context,
        layout: Int,
        cursor: Cursor,
        from: Array<String>,
        to: IntArray
    ) : SimpleCursorAdapter(context, layout, cursor, from, to, 0) {

        var setupCursor: Cursor? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val ib = view.findViewById<View>(R.id.setup)
            val cur = cursor
            cur.moveToPosition(position)
            val tagVal = cur.getString(2) + ":" + cur.getString(0)
            ib?.tag = tagVal
            ib?.setOnClickListener { v ->
                val s = (v.tag as String).split(":")
                val msg = msgView?.text?.toString() ?: ""
                smsHandler?.setupWuffIT(msg, s[0], s[1])
                updateTrackersOnView()
            }
            return view
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            super.bindView(view, context, cursor)
            val setup = view.findViewById<TextView>(R.id.text3)
            val reply = view.findViewById<TextView>(R.id.text4)
            val sc = setupCursor ?: return

            if (sc.count > cursor.position && sc.moveToPosition(cursor.position)) {
                val st = sc.getLong(1)
                val sdf = SimpleDateFormat.getDateTimeInstance()
                if (st > 0) {
                    val cal = Calendar.getInstance().apply { timeInMillis = st }
                    setup?.text = "setup on: ${sdf.format(cal.time)}"
                } else {
                    setup?.text = "setup on: <not available>"
                }

                val rt = sc.getLong(2)
                if (rt > 0) {
                    val cal = Calendar.getInstance().apply { timeInMillis = rt }
                    reply?.text = "reply on: ${sdf.format(cal.time)}"
                    view.findViewById<ImageView>(R.id.status)
                        ?.setImageResource(android.R.drawable.presence_online)
                } else {
                    reply?.text = "reply on: <not available>"
                }
            }
        }
    }

    companion object {
        private const val TAG = "SetupWuffIT"
    }
}
