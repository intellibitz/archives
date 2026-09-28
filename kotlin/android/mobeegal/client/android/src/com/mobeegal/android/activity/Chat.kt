package com.mobeegal.android.activity

/*
<!--
$Id:: Chat.java 14 2008-08-19 06:36:45Z muthu.ramadoss                          $: Id of last commit
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
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.MeasureSpec
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.util.ViewMenu
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.filter.MessageTypeFilter
import org.jivesoftware.smack.filter.PacketFilter
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import java.util.logging.Level
import java.util.logging.Logger

class Chat : ListActivity(), OnItemClickListener {
    /**
     * Called when the activity is first created.
     */
    private var config: ConnectionConfiguration? = null
    private var conn: XMPPConnection? = null
    private var listener: MessageListener? = null
    private var recipient: EditText? = null
    private val messages: ArrayList<String> = ArrayList()
    private var sendmessagetext: EditText? = null
    private var tv: TextView? = null
    private var setconnection: Button? = null
    private var adapter: ArrayAdapter<String>? = null
    private val mHandler = Handler()
    private var userid: String? = null
    private var mstuffid: String? = null

    override fun onCreate(icicle: Bundle?) {
        try {
            super.onCreate(icicle)
            setContentView(R.layout.chat)
            val b = this.intent.extras
            if (b != null) {
                mstuffid = b.getString("mstuffid")
            }
            tv = findViewById(R.id.recipient) as TextView
            tv!!.text = "Chatting with Userid: $mstuffid"
            sendmessagetext = findViewById(R.id.sendText) as EditText
            // mList = (ListView) this.findViewById(R.id.listMessages);
            adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, messages
            )
            this.listAdapter = adapter
            //getListView().setOnItemSelectedListener(this);
            listView.onItemClickListener = this
            setupListStripes()

            config = ConnectionConfiguration(
                getString(R.string.ChatServer),
                5222,
                getString(R.string.ChatServer)
            )

            conn = XMPPConnection(config)
            try {
                conn!!.connect()
            } catch (e: XMPPException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
            /*
                * if (conn.isConnected()) { Toast.makeText(Chat.this, "connected",
                * Toast.LENGTH_LONG).show(); } else { Toast.makeText(Chat.this,
                * "not connected", Toast.LENGTH_LONG).show(); }
                */
            try {
                val myDB = this.openOrCreateDatabase(
                    "Mobeegal", Context.MODE_PRIVATE, null
                )

                val cols = arrayOf("IMSI", "UserID")
                val c = myDB.query(
                    true, "MobeegalUser", cols,
                    null, null, null, null, null, null
                )
                val id = c.getColumnIndexOrThrow("UserID")
                if (c != null) {
                    if (c.isFirst) {
                        do {
                            userid = c.getString(id)
                        } while (c.moveToNext())
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "No Matches found to Chat. To get matches, fill catalog information and activate the service.",
                    Toast.LENGTH_LONG
                ).show()
            }

            try {
                conn!!.login(userid, userid)
                val present = Presence(Presence.Type.available)
                conn!!.sendPacket(present)
                Toast.makeText(this@Chat, "connected", Toast.LENGTH_LONG)
                    .show()
            } catch (e: XMPPException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                Toast.makeText(
                    this@Chat,
                    "Unable to connect with the chat server",
                    Toast.LENGTH_LONG
                ).show()
            }

            mHandler.post {
                setConnection(conn)
            }

            val sendmessage = findViewById(R.id.sendmessage) as Button
            sendmessage.setOnClickListener {
                val sendingmessage =
                    sendmessagetext!!.text.toString()

                val recipientstring =
                    mstuffid + "@" + getString(R.string.ChatServer)

                val msg =
                    Message(recipientstring, Message.Type.chat)
                msg.body = userid + ":" + sendingmessage
                conn!!.sendPacket(msg)
                val message = "me: $sendingmessage"
                messages.add(0, message)
                sendmessagetext!!.setText(" ")
                listAdapter = adapter
            }
        } catch (ex: Exception) {
            Logger.getLogger(Chat::class.java.name).log(Level.SEVERE, null, ex)
        }
    }

    fun setConnection(conn: XMPPConnection?) {
        this.conn = conn
        if (conn != null) {
            val filter: PacketFilter = MessageTypeFilter(
                Message.Type.chat
            )
            conn.addPacketListener(PacketListener { packet ->
                val messag = packet as Message
                messages.add(0, messag.body)
                // Add the incoming message to the list view
                mHandler.post {
                    this@Chat.listAdapter = adapter
                }
            }, filter)
        }
    }

    private fun setupListStripes() {
        val lineBackgrounds = arrayOfNulls<android.graphics.drawable.Drawable>(2)
        lineBackgrounds[0] = resources.getDrawable(R.drawable.even_stripe)
        lineBackgrounds[1] = resources.getDrawable(R.drawable.odd_stripe)
        val view = this.layoutInflater
            .inflate(android.R.layout.simple_list_item_1, null)
        val v = view.findViewById(android.R.id.text1) as TextView
        v.text = "X"
        v.measure(
            MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY, 30),
            MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, 0)
        )
        val height = v.measuredHeight
//        getListView().setStripes(lineBackgrounds, height);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    // Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                // mStuff Menu
                val stuffCheckintent =
                    Intent(this@Chat, MapResults::class.java)
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 = Intent(this@Chat, FindandInstall::class.java)
                startActivity(intent1)
            }
            3 -> {
                val settings = Intent(this@Chat, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(
        parent: AdapterView<*>, arg1: View?, position: Int,
        arg3: Long
    ) {
        val selectid = parent.selectedItem.toString()
        val selectidarray = selectid.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        mstuffid = selectidarray[0]
        tv!!.text = "Current Userid:   $mstuffid"
    }
}
