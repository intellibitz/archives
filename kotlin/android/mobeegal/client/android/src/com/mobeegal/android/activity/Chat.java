package com.mobeegal.android.activity;

/*
<!--
$Id:: Chat.java 14 2008-08-19 06:36:45Z muthu.ramadoss                          $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.util.ViewMenu;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat
        extends ListActivity
        implements OnItemClickListener
{
    /**
     * Called when the activity is first created.
     */
    private ConnectionConfiguration config;
    private XMPPConnection conn;
    private MessageListener listener;
    private EditText recipient;
    private ArrayList<String> messages = new ArrayList();
    private EditText sendmessagetext;
    private TextView tv;
    private Button setconnection;
    private ArrayAdapter<String> adapter;
    private Handler mHandler = new Handler();
    private String userid;
    private String mstuffid;

    @Override
    public void onCreate(Bundle icicle)
    {
        try
        {
            super.onCreate(icicle);
            setContentView(R.layout.chat);
            Bundle b = this.getIntent().getExtras();
            if (b != null)
            {
                mstuffid = b.getString("mstuffid");

            }
            tv = (TextView) findViewById(R.id.recipient);
            tv.setText("Chatting with Userid: " + mstuffid);
            sendmessagetext = (EditText) findViewById(R.id.sendText);
            // mList = (ListView) this.findViewById(R.id.listMessages);
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, messages);
            this.setListAdapter(adapter);
            //getListView().setOnItemSelectedListener(this);
            getListView().setOnItemClickListener(this);
            setupListStripes();


            config = new ConnectionConfiguration(getString(R.string.ChatServer),
                    5222,
                    getString(R.string.ChatServer));

            conn = new XMPPConnection(config);
            try
            {
                conn.connect();
            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            /*
                * if (conn.isConnected()) { Toast.makeText(Chat.this, "connected",
                * Toast.LENGTH_LONG).show(); } else { Toast.makeText(Chat.this,
                * "not connected", Toast.LENGTH_LONG).show(); }
                */
            try
            {
                SQLiteDatabase myDB = this.openOrCreateDatabase
                        ("Mobeegal", Context.MODE_PRIVATE, null);

                String[] cols = {"IMSI", "UserID"};
                Cursor c = myDB.query(true, "MobeegalUser", cols,
                        null, null, null, null, null, null);
                int id = c.getColumnIndexOrThrow("UserID");
                if (c != null)
                {
                    if (c.isFirst())
                    {
                        do
                        {
                            userid = c.getString(id);
                        }
                        while (c.moveToNext());
                    }
                }
            }
            catch (Exception e)
            {
                Toast.makeText(this,
                        "No Matches found to Chat. To get matches, fill catalog information and activate the service.",
                        Toast.LENGTH_LONG).show();

            }

            try
            {
                conn.login(userid, userid);
                Presence present = new Presence(Presence.Type.available);
                conn.sendPacket(present);
                Toast.makeText(Chat.this, "connected", Toast.LENGTH_LONG)
                        .show();

            }
            catch (XMPPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(Chat.this,
                        "Unable to connect with the chat server",
                        Toast.LENGTH_LONG)
                        .show();
            }

            mHandler.post(new Runnable()
            {

                public void run()
                {
                    setConnection(conn);
                }
            });

            Button sendmessage = (Button) findViewById(R.id.sendmessage);
            sendmessage.setOnClickListener(new OnClickListener()
            {

                public void onClick(View arg0)
                {

                    String sendingmessage =
                            sendmessagetext.getText().toString();

                    String recipientstring =
                            mstuffid + "@" + getString(R.string.ChatServer);

                    Message msg =
                            new Message(recipientstring, Message.Type.chat);
                    msg.setBody(userid + ":" + sendingmessage);
                    conn.sendPacket(msg);
                    String message = "me: " + sendingmessage;
                    messages.add(0, message);
                    sendmessagetext.setText(" ");
                    setListAdapter(adapter);

                }
            });
        }
        catch (Exception ex)
        {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public void setConnection(XMPPConnection conn)
    {
        this.conn = conn;
        if (conn != null)
        {

            PacketFilter filter = new MessageTypeFilter(
                    org.jivesoftware.smack.packet.Message.Type.chat);
            conn.addPacketListener(new PacketListener()
            {
                public void processPacket(Packet packet)
                {
                    Message messag = (Message) packet;
                    messages.add(0, messag.getBody());
                    // Add the incoming message to the list view
                    mHandler.post(new Runnable()
                    {
                        public void run()
                        {
                            Chat.this.setListAdapter(adapter);
                        }
                    });
                }

            }, filter);

        }

    }

    private void setupListStripes()
    {
        Drawable[] lineBackgrounds = new Drawable[2];
        lineBackgrounds[0] = getResources().getDrawable(R.drawable.even_stripe);
        lineBackgrounds[1] = getResources().getDrawable(R.drawable.odd_stripe);
        View view = this.getLayoutInflater()
                .inflate(android.R.layout.simple_list_item_1, null);
        TextView v = (TextView) view.findViewById(android.R.id.text1);
        v.setText("X");
        v.measure(MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY, 30),
                MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, 0));
        int height = v.getMeasuredHeight();
//        getListView().setStripes(lineBackgrounds, height);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    // Menu Item

    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
            case 1:
                // mStuff Menu
                Intent stuffCheckintent =
                        new Intent(Chat.this, MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(Chat.this, FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings = new Intent(Chat.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(AdapterView parent, View arg1, int position,
            long arg3)
    {

        String selectid = parent.getSelectedItem().toString();
        String selectidarray[] = selectid.split(":");
        mstuffid = selectidarray[0];
        tv.setText("Current Userid:   " + mstuffid);
    }

}
