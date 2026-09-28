package com.mobeegal.android.activity

/*
<!--
$Id:: Subscribe.java 14 2008-08-19 06:36:45Z muthu.ramadoss                  $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.AbsoluteLayout
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.util.HttpUtils
import com.mobeegal.android.util.ViewMenu
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

class Subscribe : Activity() {
    private var categorystatus1: String? = null

    private var categorystatus: Int = 0
    private var catalogid: Int = 0
    private var gettingcatalog: String? = null
    var myDB: SQLiteDatabase? = null
    var httpclient = DefaultHttpClient()
    // public String MY_DATABASE_TABLE = "catalog";
    var str = arrayOfNulls<String>(50)
    var res = arrayOfNulls<String>(4)
    var res1 = arrayOfNulls<String>(10)
    var statusArray: Array<String>? = null
    var catalogname1: String? = null
    var categoryname1: String? = null
    var catalogid1: String? = null
    var catalogvalue: String? = null
    var state1: String? = null
    var status1: String? = null
    var statusres: String? = null
    var serviceact: String? = null
    var serviceact1: String? = null
    var i: Int = 0
    var a: Int = 0
    var b: Int = 0
    var statuscolumn: Int = 0
    var catalogname: Int = 0
    var categoryname: Int = 0
    var state: Int = 0
    var c: Cursor? = null
    var c2: Cursor? = null
    var c3: Cursor? = null
    var ch = arrayOfNulls<CheckBox>(10)// , ch1, ch2, ch3, ch4, ch5, ch6;
    var getcount: Int = 0
    var catalogheight: Int = 0
    var categoryheight: Int = 0
    private var requestcatalog: String? = null
    private var jsonArrayLength: Int = 0
    private var getcountcatname: Int = 0
    private var userIDstring: String? = null

    protected var catname: Int = 0
    protected var modename: Int = 0
    protected var modesname: String? = null
    var js = JSONStringer()
    protected var catsname: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.subscribe)

        val B2 = this.intent.extras
        if (B2 != null) {
            // B2.getString("catalogvalue");
            requestcatalog = B2.getString("requestcatalog")
            // Toast.makeText(Subscribe.this, "Bundle passed : " +
            // requestcatalog, Toast.LENGTH_SHORT).show();
            z = 0
            install()
        }

        try {
            myDB = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )

            val colu = arrayOf("catalogname", "state")
            val c = myDB!!.query(
                requestcatalog + "_catalogs", colu,
                null, null, null, null, null
            )
            val state = c.getColumnIndexOrThrow("state")
            if (c != null) {
                if (c.isFirst) {
                    do {
                        state1 = c.getString(state)
                    } while (c.moveToNext())
                }
            }

            if (state1.toString() == "true") {
                // Toast.makeText(Subscribe.this, "Fetching loop : " +
                // requestcatalog + "_catalogs", Toast.LENGTH_SHORT).show();
                fetch()
            }
            c.close()
        } catch (e: Exception) {
        }

        val subscribe = findViewById(R.id.subscribe) as Button
        subscribe.setOnClickListener { view ->
            try {
                val mobeegalUserCursor = myDB!!.query(
                    "MobeegalUser",
                    null, null, null, null, null, null
                )
                val UseridColumn = mobeegalUserCursor
                    .getColumnIndexOrThrow("UserID")
                if (mobeegalUserCursor != null) {
                    if (mobeegalUserCursor.isFirst) {
                        userIDstring = mobeegalUserCursor
                            .getString(UseridColumn)
                        // logger.info("mobeegalUserID = " + useridColumn);
                        logger.info("user Id :" + userIDstring)
                    }
                }

                // userIDstring= "499132";
                logger.info("user Id :" + userIDstring)
                js.`object`()
                js.key("action").value("catalogs_subscription_type").key(
                    "query"
                ).`object`().key("id").value(userIDstring)
                val column = arrayOf("modes")
                c2 = myDB!!.query(
                    "category", column,
                    "status='true'", null, null, null, "modes"
                )
                //				c2 = myDB.query(true, "category", column, null, null, null,
                //						null, "modes");
                modename = c2!!.getColumnIndexOrThrow("modes")
                getcountcatname = c2!!.count
                logger.info("Modes count :" + getcountcatname)
                if (c2 != null) {
                    b = 0
                    if (c2!!.isFirst) {
                        do {
                            modesname = c2!!.getString(modename)
                            logger.info("Mode name :" + modesname)
                            res1[b] = modesname
                            b++
                        } while (c2!!.moveToNext())
                    }
                }

                for (j in 0 until getcountcatname) {
                    js.key(res1[j]).array()
                    val colus = arrayOf("categoryname")
                    c3 = myDB!!.query(
                        "category", colus,
                        "status='true' and modes='" + res1[j] + "'",
                        null, null, null, null
                    )
                    catname = c3!!.getColumnIndexOrThrow("categoryname")
                    logger.info("c3 count  :" + c3!!.count)
                    if (c3 != null) {
                        if (c3!!.isFirst) {
                            do {
                                catsname = c3!!.getString(catname)
                                logger.info("category name :" + catsname)
                                js.value(catsname)
                            } while (c3!!.moveToNext())
                        }
                    }
                    js.endArray()
                }
                js.endObject().endObject()
                val subscribecatalogs = js.toString()
                logger.info("Subscription catalogs :" + subscribecatalogs)
                val data =
                    ArrayList<NameValuePair>()
                data.add(
                    BasicNameValuePair(
                        "data_pack",
                        subscribecatalogs
                    )
                )
                val httpPost = HttpPost(
                    getString(R.string.CatalogServer)
                )
                httpPost.entity =
                    UrlEncodedFormEntity(data, HTTP.UTF_8)
                val resp = httpclient.execute(httpPost)
                val response = HttpUtils.getResponseString(resp)
                val intent1 = Intent(
                    this@Subscribe,
                    FindandInstall::class.java
                )
                startActivityForResult(intent1, 0)
                c3!!.close()
                c2!!.close()
                mobeegalUserCursor.close()
            } catch (e: Exception) {
                Toast.makeText(this@Subscribe, "" + e, Toast.LENGTH_LONG)
                    .show()
            }
        }

        val selectall = findViewById(R.id.selectall) as Button
        selectall.setOnClickListener { view ->
        }
    }

    // code added
    private fun install() {
        try {
            // this.createDatabase("Mobeegal", 1, MODE_PRIVATE, null);
            myDB = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            myDB!!
                .execSQL(
                    "CREATE TABLE IF NOT EXISTS " +
                        requestcatalog +
                        "_catalogs(catalogID NUMERIC(3),catalogname VARCHAR,state VARCHAR,catalogtype VARCHAR);"
                )
            myDB!!
                .execSQL(
                    "CREATE TABLE IF NOT EXISTS " +
                        requestcatalog +
                        "_category(categoryID NUMERIC(3),catalogID1 NUMERIC(3),categoryname VARCHAR,status VARCHAR,querystatus VARCHAR,catalogtype VARCHAR,modes VARCHAR);"
                )
            myDB!!
                .execSQL(
                    "CREATE TABLE IF NOT EXISTS catalogs(catalogID NUMERIC(3),catalogname VARCHAR,state VARCHAR,catalogtype VARCHAR);"
                )
            myDB!!
                .execSQL(
                    "CREATE TABLE IF NOT EXISTS category(categoryID NUMERIC(3),catalogID1 NUMERIC(3),categoryname VARCHAR,status VARCHAR,querystatus VARCHAR,catalogtype VARCHAR,modes VARCHAR,catalogname VARCHAR);"
                )

            val js = JSONStringer()
            logger.info("HTTP Request:" + requestcatalog)
            js.`object`().key("action").value(requestcatalog)
            js.endObject()
            val catalogJSON = js.toString()
            // String key = "intellibitz";
            // EncryptionDecryption encryptDecrypt = new EncryptionDecryption();
            // String encrypted =
            // encryptDecrypt.EncryptionDecryption(catalogJSON, key);
            val data =
                ArrayList<NameValuePair>()
            data.add(
                BasicNameValuePair(
                    "data_pack",
                    catalogJSON
                )
            )
            val httpPost = HttpPost(
                getString(R.string.CatalogServer)
            )
            httpPost.entity =
                UrlEncodedFormEntity(data, HTTP.UTF_8)
            val resp = httpclient.execute(httpPost)
            gettingcatalog = HttpUtils.getResponseString(resp)
            logger.info("HTTP Request:" + data)
            // String decrypted =
            // encryptDecrypt.EncryptionDecryption(gettingcatalog.trim(), key);
            logger.info("HTTP Response : " + gettingcatalog)
            val catalogJson = JSONObject(gettingcatalog)
            logger.info("JSON Object : " + catalogJson)
            val strid = catalogJson.getString(requestcatalog)
            logger.info("string strid : " + strid)
            val catalogsJson = JSONObject(strid)
            val catalogJsonKey = catalogsJson.names()
            logger.info("HTTP Response Array:" + catalogJsonKey)
            val catalogJsonKeylength = catalogJsonKey.length()
            logger.info("HTTP Response length:" + catalogJsonKeylength)
            val catalogJsonKeyStringArray =
                arrayOfNulls<String>(catalogJsonKeylength)
            val c1 = myDB!!.query(
                requestcatalog + "_catalogs", null,
                null, null, null, null, null
            )
            if (c1.count < catalogJsonKeylength) {
                i = 0
                while (i < catalogJsonKeylength) {
                    val catalogJsonKeyString = catalogJsonKey.getString(i)
                    logger.info("key value : " + catalogJsonKeyString)
                    catalogJsonKeyStringArray[i] = catalogJsonKeyString
                    logger.info("keyArray : " + catalogJsonKeyStringArray[i])
                    catalogvalue = catalogsJson.getString(catalogJsonKeyString)
                    logger.info("keyArrayValue : " + catalogvalue)
                    myDB!!
                        .execSQL(
                            "INSERT INTO " +
                                requestcatalog +
                                "_catalogs (catalogID,catalogname,state,catalogtype) VALUES (" +
                                i + ",'" + catalogJsonKeyStringArray[i] +
                                "','" + "true" + "','" + requestcatalog +
                                "');"
                        )
                    myDB!!
                        .execSQL(
                            "INSERT INTO catalogs (catalogID,catalogname,state,catalogtype) VALUES (" +
                                i +
                                ",'" +
                                catalogJsonKeyStringArray[i] +
                                "','" +
                                "true" +
                                "','" +
                                requestcatalog +
                                "');"
                        )
                    logger.info("key value inserted : " + catalogvalue)
                    val catalogvalueJsonArray = catalogsJson
                        .getJSONArray(catalogJsonKeyString)
                    val jsonArrayLen = catalogvalueJsonArray.length()
                    if (c1.count < jsonArrayLen) {
                        for (j in 0 until jsonArrayLen) {
                            val catalogvalueofvalues = catalogvalueJsonArray
                                .getString(j)
                            logger.info(
                                "Array value of value:" +
                                    catalogvalueofvalues
                            )
                            myDB!!
                                .execSQL(
                                    "INSERT INTO " +
                                        requestcatalog +
                                        "_category (categoryID,catalogID1,categoryname,status,querystatus,catalogtype) VALUES (" +
                                        z + "," + i + ",'" +
                                        catalogvalueofvalues + "','" +
                                        "false" + "','" + "false" + "','" +
                                        requestcatalog + "');"
                                )
                            myDB!!
                                .execSQL(
                                    "INSERT INTO category (categoryID,catalogID1,categoryname,status,querystatus,catalogtype,catalogname) VALUES (" +
                                        z +
                                        "," +
                                        i +
                                        ",'" +
                                        catalogvalueofvalues +
                                        "','" +
                                        "false" +
                                        "','" +
                                        "false" +
                                        "','" +
                                        requestcatalog +
                                        "','" +
                                        catalogJsonKeyStringArray[i] +
                                        "');"
                                )
                            z++
                        }
                    }
                    i++
                }
            }
            c1.close()
        } catch (e: Exception) {
            Log.i("JSON Exception : ", e.message)
        }
    }

    // code added
    private fun fetch() {
        logger.info(" fetch loop ")
        val colu = arrayOf("catalogname", "catalogID")
        c = myDB!!.query(
            requestcatalog + "_catalogs", colu, null, null,
            null, null, null
        )
        val absoluteLayout =
            findViewById(R.id.myTableLayout) as AbsoluteLayout
        catalogname = c!!.getColumnIndexOrThrow("catalogname")
        catalogid = c!!.getColumnIndexOrThrow("catalogID")
        logger.info(" fetch loop values : " + catalogname + catalogid)
        if (c != null) {
            a = 0
            b = 0
            catalogheight = 50
            if (c!!.isFirst) {
                do {
                    catalogname1 = c!!.getString(catalogname)
                    catalogid1 = c!!.getString(catalogid)
                    res[a] = catalogname1
                    val tv = TextView(this@Subscribe)
                    tv.typeface = Typeface.create(res[a], Typeface.BOLD)
                    tv.text = res[a]
                    absoluteLayout.addView(
                        tv, AbsoluteLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT, 50, catalogheight
                        )
                    )
                    /** code add */
                    val column = arrayOf("categoryname", "status")
                    logger.info(" category loop start")
                    c2 = myDB!!.query(
                        requestcatalog + "_category", column,
                        "catalogID1='" + catalogid1 + "'", null, null,
                        null, null
                    )
                    logger.info(" fetch loop start 1 ")
                    getcount = c2!!.count
                    categoryname = c2!!.getColumnIndexOrThrow("categoryname")
                    categorystatus = c2!!.getColumnIndexOrThrow("status")
                    if (c2 != null) {
                        categoryheight = catalogheight + 10
                        if (c2!!.isFirst) {
                            do {
                                categoryname1 = c2!!.getString(categoryname)
                                categorystatus1 = c2!!.getString(categorystatus)
                                res1[b] = categoryname1
                                /** code add */
                                ch[b] = CheckBox(this@Subscribe)
                                ch[b]!!.text = res1[b]
                                ch[b]!!.isFocusable = true
                                if (categorystatus1.equals("true", ignoreCase = true)) {
                                    ch[b]!!.isChecked = true
                                } else {
                                    ch[b]!!.isChecked = false
                                }
                                absoluteLayout.addView(
                                    ch[b],
                                    AbsoluteLayout.LayoutParams(
                                        LayoutParams.WRAP_CONTENT,
                                        LayoutParams.WRAP_CONTENT, 115,
                                        categoryheight
                                    )
                                )
                                categoryheight = categoryheight + 37
                                /** code end */
                                b++
                            } while (c2!!.moveToNext())
                        }
                    }
                    /** code end */
                    a++
                    catalogheight = categoryheight + 5
                } while (c!!.moveToNext())
            }
        }
        c2!!.close()
        c!!.close()
        /** code new added */
        ch[0]!!.setOnClickListener { arg0 ->
            if (ch[0]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=0"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=0"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 0)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }
        ch[1]!!.setOnClickListener { arg0 ->
            if (ch[1]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=1"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=1"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 1)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }
        ch[2]!!.setOnClickListener { arg0 ->
            if (ch[2]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=2"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=2"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 2)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }
        ch[3]!!.setOnClickListener { arg0 ->
            if (ch[3]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=3"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=3"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 3)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }
        ch[4]!!.setOnClickListener { arg0 ->
            if (ch[4]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=4"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=4"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 4)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }
        ch[5]!!.setOnClickListener { arg0 ->
            if (ch[5]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=5"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=5"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 5)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }
        ch[6]!!.setOnClickListener { arg0 ->
            if (ch[6]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=6"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=6"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 6)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }
        ch[7]!!.setOnClickListener { arg0 ->
            if (ch[7]!!.isChecked) {
                myDB!!.execSQL(
                    "update " + requestcatalog +
                        "_category set status='true' where categoryID=7"
                )
                myDB!!
                    .execSQL(
                        "update category set status='true' where catalogtype='" +
                            requestcatalog +
                            "'and categoryID=7"
                    )
                val B1 = Bundle()
                B1.putString("requestcatalog", requestcatalog)
                B1.putInt("requestno", 7)
                val subscribe = Intent(this@Subscribe, Modes::class.java)
                subscribe.putExtras(B1)
                startActivityForResult(subscribe, 0)
            }
        }

        /** code ended */
    }

    // MenuView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    // Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent = Intent(
                    this@Subscribe,
                    MapResults::class.java
                )
                startActivityForResult(stuffCheckintent, 0)
                finish()
            }
            2 -> {
                val intent1 =
                    Intent(this@Subscribe, FindandInstall::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }
            3 -> {
                val settings = Intent(this@Subscribe, Settings::class.java)
                startActivityForResult(settings, 0)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var z: Int = 0
        private val logger = Logger.getLogger("Subscribe")
    }
}
