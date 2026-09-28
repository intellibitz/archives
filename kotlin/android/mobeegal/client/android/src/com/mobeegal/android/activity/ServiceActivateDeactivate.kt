/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import com.android.internal.telephony.TelephonyProperties
import com.mobeegal.android.R
import com.mobeegal.android.content.MstuffQuery
import com.mobeegal.android.util.HttpUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.json.JSONObject
import org.json.JSONStringer
import java.util.ArrayList
import java.util.logging.Logger

class ServiceActivateDeactivate : Activity() {

    private var deact: RadioButton? = null
    private var act: RadioButton? = null
    private var statusname1: String? = null
    private var servicename1: String? = null
    private var myDB: SQLiteDatabase? = null
    private var strid: String? = null
    private var imsiNumber: String? = null
    private var userIDstring: String? = null
    private var res: String? = null
    private var getService: String? = null
    var results: ArrayList<Any?> = ArrayList()

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.time_settings)
        //Fetching service status values from serviceactivation Table
        myDB = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        try {
            val col = arrayOf("service", "status")
            val c = myDB!!.query(
                "serviceactivation", col, null,
                null, null, null, null
            )
            val servicename = c.getColumnIndexOrThrow("service")
            val statusname = c.getColumnIndexOrThrow("status")

            if (c != null) {
                if (c.isFirst) {
                    do {
                        servicename1 = c.getString(servicename)
                        statusname1 = c.getString(statusname)
                        results.add(servicename1)
                        results.add(statusname1)
                    } while (c.moveToNext())
                } else {
                }
            }
            res = results.toString()
            c.close()
        } catch (e: Exception) {
        }

        act = findViewById(R.id.activateradiobutton) as RadioButton
        deact = findViewById(R.id.deactivateradiobutton) as RadioButton
        if (statusname1.toString() == "1") {
            deact!!.isChecked = true
            act!!.isChecked = false
        } else {
            act!!.isChecked = true
            deact!!.isChecked = false
        }

        val activateButton =
            findViewById(R.id.buttonActivate) as ImageButton
        activateButton.setOnClickListener {
            //Fetching IMSI and UserID from MobeegalUser Table
            try {
                val col1 = arrayOf("IMSI")
                val c = myDB!!.query(
                    "MobeegalUser", col1, null,
                    null, null, null, null
                )

                val imsi = c.getColumnIndexOrThrow("IMSI")
                val userID = c.getColumnIndexOrThrow("UserId")
                if (c != null) {
                    if (c.isFirst) {
                        do {
                            imsiNumber = c.getString(imsi)
                            userIDstring = c.getString(userID)
                            results.add(userIDstring)
                            results.add(imsiNumber)
                        } while (c.moveToNext())
                    }
                }
                val res = results.toString()
                // First time registering and sending IMSI number to server
                val myIMSI = android.os.SystemProperties
                    .get(TelephonyProperties.PROPERTY_SIM_OPERATOR_NUMERIC)
                if (myIMSI != imsiNumber) {
                    val register = "register"
                    val IMSI = "456957013123456"
                    val js = JSONStringer()
                    // old request         js.object().key("action").value(register).key("query").object().key("IMSI").value(IMSI).endObject();
                    /* new request */
                    js.`object`().key("action").value(register).key("group")
                        .value("Nokia").key("query").`object`()
                        .key("IMSI").value(IMSI).endObject()
                    //key("query").object().key("IMSI").value(myIMSI).endObject();
                    js.endObject()
                    val registerJson = js.toString()
                    logger.info("Sending IMSI in JSON = " + registerJson)

                    val httpclient = DefaultHttpClient()
                    val key = "intellibitz"
                    //EncryptionDecryption encryptDecrypt = new EncryptionDecryption();
                    // String encrypted = encryptDecrypt.EncryptionDecryption(registerJson, key);
                    val data =
                        ArrayList<NameValuePair>()
                    data.add(
                        BasicNameValuePair(
                            "data_pack",
                            registerJson
                        )
                    )
                    val httpPost = HttpPost(
                        getString(R.string.CatalogServer)
                    )
                    httpPost.entity =
                        UrlEncodedFormEntity(data, HTTP.UTF_8)
                    val resp = httpclient.execute(httpPost)
                    val response = HttpUtils.getResponseString(resp)
                    //logger.info("encrypted " + encrypted);
                    //logger.info("decrypted " + decrypted);
                    val jo = JSONObject(response)
                    strid = jo.getString("id")
                    myDB!!.execSQL(
                        "INSERT INTO MobeegalUser (IMSI, UserID) VALUES ('" +
                            IMSI + "','" + strid + "');"
                    )
                } else {
                }
                c.close()
            } catch (e: Exception) {
            }
            act = findViewById(R.id.activateradiobutton) as RadioButton
            //Service Activation
            if (act!!.isChecked) {
                getService = "Activate"
                startService(
                    Intent(
                        "com.mobeegal.android.service.REMOTE_SERVICE"
                    )
                )
                myDB!!.execSQL(
                    "update serviceactivation set status='0' where service='deactivate';"
                )
                val intent = Intent(
                    this@ServiceActivateDeactivate,
                    TimeSettings::class.java
                )
                startActivityForResult(intent, 0)
                finish()
            } //Service Deactivation
            else {
                stopService(
                    Intent(
                        "com.mobeegal.android.service.REMOTE_SERVICE"
                    )
                )
                val intent = Intent(
                    this@ServiceActivateDeactivate,
                    MstuffQuery::class.java
                )
                val pi = PendingIntent.getActivity(
                    applicationContext, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                val am =
                    getSystemService(ALARM_SERVICE) as AlarmManager
                am.cancel(pi)
                /*
                    PostMethod httpPost =
                            new PostMethod(getString(R.string.RemoteServer));
                    httpPost.releaseConnection();
                */
                myDB!!.execSQL(
                    "update serviceactivation set status='1' where service='deactivate';"
                )
                val intent1 = Intent(
                    this@ServiceActivateDeactivate,
                    Settings::class.java
                )
                startActivityForResult(intent1, 0)
                finish()
            }
        }
        val clearButton = findViewById(R.id.buttonBack) as ImageButton
        clearButton.setOnClickListener {
            val intent1 = Intent(
                this@ServiceActivateDeactivate,
                Settings::class.java
            )
            startActivityForResult(intent1, 0)
            finish()
        }
    }

    companion object {
        private val logger = Logger.getLogger("Service Active Deactive")
    }
}
