/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import com.android.internal.telephony.TelephonyProperties;
import com.mobeegal.android.R;
import com.mobeegal.android.content.MstuffQuery;
import com.mobeegal.android.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ServiceActivateDeactivate
        extends Activity
{

    private RadioButton deact;
    private RadioButton act;
    private String statusname1;
    private String servicename1;
    private SQLiteDatabase myDB;
    private String strid;
    private String imsiNumber;
    private String userIDstring;
    private String res;
    private String getService;
    ArrayList results = new ArrayList();
    private static Logger logger = Logger.getLogger("Service Active Deactive");

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.time_settings);
//Fetching service status values from serviceactivation Table
        myDB = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        try
        {
            String[] col = {"service", "status"};
            Cursor c = myDB.query("serviceactivation", col, null,
                    null, null, null, null);
            int servicename = c.getColumnIndexOrThrow("service");
            int statusname = c.getColumnIndexOrThrow("status");

            if (c != null)
            {
                if (c.isFirst())
                {
                    do
                    {
                        servicename1 = c.getString(servicename);
                        statusname1 = c.getString(statusname);
                        results.add(servicename1);
                        results.add(statusname1);
                    }
                    while (c.moveToNext());
                }
                else
                {
                }
            }
            res = results.toString();
            c.close();
        }
        catch (Exception e)
        {
        }

        act = (RadioButton) findViewById(R.id.activateradiobutton);
        deact = (RadioButton) findViewById(R.id.deactivateradiobutton);
        if (statusname1.toString().equals("1"))
        {
            deact.setChecked(true);
            act.setChecked(false);
        }
        else
        {
            act.setChecked(true);
            deact.setChecked(false);
        }

        ImageButton activateButton =
                (ImageButton) findViewById(R.id.buttonActivate);
        activateButton.setOnClickListener(new ImageButton.OnClickListener()
        {

            public void onClick(View v)
            {
//Fetching IMSI and UserID from MobeegalUser Table
                try
                {
                    String[] col1 = {"IMSI"};
                    Cursor c = myDB.query("MobeegalUser", col1, null,
                            null, null, null, null);

                    int imsi = c.getColumnIndexOrThrow("IMSI");
                    int userID = c.getColumnIndexOrThrow("UserId");
                    if (c != null)
                    {
                        if (c.isFirst())
                        {
                            do
                            {
                                imsiNumber = c.getString(imsi);
                                userIDstring = c.getString(userID);
                                results.add(userIDstring);
                                results.add(imsiNumber);
                            }
                            while (c.moveToNext());
                        }
                    }
                    String res = results.toString();
// First time registering and sending IMSI number to server
                    String myIMSI = android.os.SystemProperties
                            .get(TelephonyProperties.PROPERTY_SIM_OPERATOR_NUMERIC);
                    if (myIMSI != imsiNumber)
                    {
                        String register = "register";
                        String IMSI = "456957013123456";
                        JSONStringer js = new JSONStringer();
                        // old request         js.object().key("action").value(register).key("query").object().key("IMSI").value(IMSI).endObject();
/* new request */
                        js.object().key("action").value(register).key("group")
                                .value("Nokia").key("query").object()
                                .key("IMSI").value(IMSI).endObject();
                        //key("query").object().key("IMSI").value(myIMSI).endObject();
                        js.endObject();
                        String registerJson = js.toString();
                        logger.info("Sending IMSI in JSON = " + registerJson);

                        HttpClient httpclient = new DefaultHttpClient();
                        String key = "intellibitz";
                        //EncryptionDecryption encryptDecrypt = new EncryptionDecryption();
                        // String encrypted = encryptDecrypt.EncryptionDecryption(registerJson, key);
                        ArrayList<NameValuePair> data =
                                new ArrayList<NameValuePair>();
                        data.add(new BasicNameValuePair("data_pack",
                                registerJson));
                        HttpPost httpPost = new HttpPost(
                                getString(R.string.CatalogServer));
                        httpPost.setEntity(
                                new UrlEncodedFormEntity(data, HTTP.UTF_8));
                        HttpResponse resp = httpclient.execute(httpPost);
                        String response = HttpUtils.getResponseString(resp);
                        //logger.info("encrypted " + encrypted);
                        //logger.info("decrypted " + decrypted);
                        JSONObject jo = new JSONObject(response);
                        strid = jo.getString("id");
                        myDB.execSQL(
                                "INSERT INTO MobeegalUser (IMSI, UserID) VALUES ('" +
                                        IMSI + "','" + strid + "');");
                    }
                    else
                    {
                    }
                    c.close();
                }
                catch (Exception e)
                {
                }
                act = (RadioButton) findViewById(R.id.activateradiobutton);
//Service Activation
                if (act.isChecked() == true)
                {
                    getService = "Activate";
                    startService(new Intent(
                            "com.mobeegal.android.service.REMOTE_SERVICE"));
                    myDB.execSQL(
                            "update serviceactivation set status='0' where service='deactivate';");
                    Intent intent = new Intent(ServiceActivateDeactivate.this,
                            TimeSettings.class);
                    startActivityForResult(intent, 0);
                    finish();
                } //Service Deactivation
                else
                {
                    stopService(new Intent(
                            "com.mobeegal.android.service.REMOTE_SERVICE"));
                    Intent intent = new Intent(ServiceActivateDeactivate.this,
                            MstuffQuery.class);
                    PendingIntent pi = PendingIntent.getActivity
                            (getApplicationContext(), 0, intent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager am =
                            (AlarmManager) getSystemService(ALARM_SERVICE);
                    am.cancel(pi);
/*
                    PostMethod httpPost =
                            new PostMethod(getString(R.string.RemoteServer));
                    httpPost.releaseConnection();
*/
                    myDB.execSQL(
                            "update serviceactivation set status='1' where service='deactivate';");
                    Intent intent1 = new Intent(ServiceActivateDeactivate.this,
                            Settings.class);
                    startActivityForResult(intent1, 0);
                    finish();
                }
            }
        });
        ImageButton clearButton = (ImageButton) findViewById(R.id.buttonBack);
        clearButton.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View arg0)
            {
                Intent intent1 = new Intent(ServiceActivateDeactivate.this,
                        Settings.class);
                startActivityForResult(intent1, 0);
                finish();
            }
        });
    }
}
