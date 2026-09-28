package com.mobeegal.android.activity

/*
<!--
$Id:: FindandInstall.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.Gallery
import android.widget.GridView
import com.mobeegal.android.R
import com.mobeegal.android.util.HttpUtils
import com.mobeegal.android.util.ViewMenu
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.json.JSONObject
import org.json.JSONStringer
import java.util.logging.Logger

/**
 * @author work
 */
class FindandInstall : Activity() {

    private val res1 = arrayOfNulls<String>(4)
    private var catalogname1: String? = null
    private var b = 0
    private var getcountcatname: Int = 0
    private var catalogname: Int = 0
    private var catalogvalue: String? = null
    private var i: Int = 0
    var myDB: SQLiteDatabase? = null
    private var gettingcatalog: String? = null
    var httpclient: HttpClient = DefaultHttpClient()
    var c: Cursor? = null
    private var requestcatalog = ""
    var catalogbutton: Button? = null
    var catalogbutton1: Button? = null
    private var n = 0
    private var strid: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.catalogs)
        try {
            myDB = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val col = arrayOf("UserID")
            val c = myDB!!.query(
                "MobeegalUser", col, null, null, null, null,
                null
            )
            if (c.count == 0) {
                val register = "register"
                val IMSI = "456957013123457"
                val js = JSONStringer()
// new request
                js.`object`().key("action").value(register).key("group")
                    .value("Nokia").key("query").`object`().key("IMSI")
                    .value(IMSI).endObject()
                // key("query").object().key("IMSI").value(myIMSI).endObject();
                js.endObject()
                val registerJson = js.toString()
                logger.info("Sending IMSI in JSON = $registerJson")
                val httpclient: HttpClient = DefaultHttpClient()
                val key = "intellibitz"
                // EncryptionDecryption encryptDecrypt = new
                // EncryptionDecryption();
                // String encrypted =
                // encryptDecrypt.EncryptionDecryption(registerJson, key);
                val data = ArrayList<NameValuePair>()
                data.add(
                    BasicNameValuePair(
                        "data_pack",
                        registerJson
                    )
                )
                val httpPost = HttpPost(
                    getString(R.string.CatalogServer)
                )
                httpPost.entity = UrlEncodedFormEntity(data, HTTP.UTF_8)
                val resp = httpclient.execute(httpPost)
                val response = HttpUtils.getResponseString(resp)
//                String request = httpPost.getQueryString();
                // String decrypted =
                // encryptDecrypt.EncryptionDecryption(response.trim(), key);
//                logger.info("request =" + request);
                logger.info("response =$response")
                // logger.info("encrypted " + encrypted);
                // logger.info("decrypted " + decrypted);
                val jo = JSONObject(response)
                strid = jo.getString("id")
                myDB!!.execSQL(
                    "INSERT INTO MobeegalUser (IMSI, UserID) VALUES ('" +
                        IMSI + "','" + strid + "');"
                )
                startService(
                    Intent(
                        "com.mobeegal.android.service.REMOTE_SERVICE"
                    )
                )
                myDB!!.execSQL(
                    "update serviceactivation set status='0' where service='deactivate';"
                )
            }
            c.close()
        } catch (e: Exception) {
            //	logger.info(" IMSI in catch block");
        }

        val mostpopular = findViewById(R.id.mostpopular) as Button
        mostpopular.setOnClickListener {
            requestcatalog = "mostpopular"
            val B1 = Bundle()
            B1.putString("requestcatalog", requestcatalog)
            val install = Intent(
                this@FindandInstall,
                Subscribe::class.java
            )
            install.putExtras(B1)
            startActivityForResult(install, 0)
        }

        val popular = findViewById(R.id.popular) as Button
        popular.setOnClickListener {
            requestcatalog = "popular"
            val B1 = Bundle()
            B1.putString("requestcatalog", requestcatalog)
            val install = Intent(
                this@FindandInstall,
                Subscribe::class.java
            )
            install.putExtras(B1)
            startActivityForResult(install, 0)
        }

        val otherpopular = findViewById(R.id.otherpopular) as Button
        otherpopular.setOnClickListener {
            requestcatalog = "othercatalog"
            val B1 = Bundle()
            B1.putString("requestcatalog", requestcatalog)
            val install = Intent(
                this@FindandInstall,
                Subscribe::class.java
            )
            install.putExtras(B1)
            startActivityForResult(install, 0)
        }

        try {
            myDB = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val colu = arrayOf("modes")
            c = myDB!!.query(
                "category", colu, "status='true'", null, null,
                null, "modes"
            )
            catalogname = c!!.getColumnIndexOrThrow("modes")
            getcountcatname = c!!.count

            if (c != null) {
                b = 0
                if (c!!.isFirst) {
                    do {
                        catalogname1 = c!!.getString(catalogname)
                        res1[b] = catalogname1
                        b++
                    } while (c!!.moveToNext())
                }
            }
            c!!.close()
            val g = findViewById(R.id.myGrid) as GridView
            g.adapter = ImageAdapter(this)
        } catch (e: Exception) {
            // Toast.makeText(Catalogs.this, "" + e, Toast.LENGTH_LONG).show();
        }
    }

    inner class ImageAdapter(c: Context) : BaseAdapter() {

        private val mContext: Context = c

        override fun getCount(): Int {
            return getcountcatname
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(
            position: Int, convertView: View?,
            parent: ViewGroup?
        ): View {
            n = 0
            while (n < getcountcatname) {
                catalogbutton = Button(mContext)
                catalogbutton!!.setOnClickListener {
                    logger.info("position :$position")
                    val categoryBundle = Bundle()
                    categoryBundle.putInt("position", position)
                    val install = Intent(
                        mContext,
                        CategoryList::class.java
                    )
                    install.putExtras(categoryBundle)
                    startActivityForResult(install, 0)
                }
                catalogbutton!!.layoutParams = Gallery.LayoutParams(80, 50)
                catalogbutton!!.setPadding(9, 9, 9, 9)
                catalogbutton!!.text = res1[position]
                return catalogbutton!!
            }
            return parent!!
        }
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
                    this@FindandInstall,
                    MapResults::class.java
                )
                startActivityForResult(stuffCheckintent, 0)
                finish()
            }
            2 -> {
                val intent1 = Intent(
                    this@FindandInstall,
                    FindandInstall::class.java
                )
                startActivityForResult(intent1, 0)
                finish()
            }
            3 -> {
                val settings =
                    Intent(this@FindandInstall, Settings::class.java)
                startActivityForResult(settings, 0)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmField
        var z = 0
        @JvmField
        var r = 0
        private val logger = Logger.getLogger("find and install")
    }
}
