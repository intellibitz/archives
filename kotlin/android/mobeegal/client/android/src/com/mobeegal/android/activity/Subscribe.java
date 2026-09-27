package com.mobeegal.android.activity;

/*
<!--
$Id:: Subscribe.java 14 2008-08-19 06:36:45Z muthu.ramadoss                  $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.util.HttpUtils;
import com.mobeegal.android.util.ViewMenu;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Subscribe
        extends Activity
{
    private String categorystatus1;

    private int categorystatus;
    private int catalogid;
    static int z;
    private String gettingcatalog;
    SQLiteDatabase myDB = null;
    HttpClient httpclient = new DefaultHttpClient();
    // public String MY_DATABASE_TABLE = "catalog";
    String[] str = new String[50];
    String[] res = new String[4];
    String[] res1 = new String[10];
    String[] statusArray;
    String catalogname1, categoryname1;
    String catalogid1, catalogvalue, state1, status1, statusres, serviceact,
            serviceact1;
    int i, a, b = 0, statuscolumn;
    int catalogname, categoryname, state;
    private static Logger logger = Logger.getLogger("Subscribe");
    Cursor c;
    Cursor c2;
    Cursor c3;
    CheckBox ch[] = new CheckBox[10];// , ch1, ch2, ch3, ch4, ch5, ch6;
    int getcount, catalogheight, categoryheight;
    private String requestcatalog;
    private int jsonArrayLength;
    private int getcountcatname;
    private String userIDstring;

    protected int catname;
    protected int modename;
    protected String modesname;
    JSONStringer js = new JSONStringer();
    protected String catsname;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.subscribe);

        Bundle B2 = this.getIntent().getExtras();
        if (B2 != null)
        {
            // B2.getString("catalogvalue");
            requestcatalog = B2.getString("requestcatalog");
            // Toast.makeText(Subscribe.this, "Bundle passed : " +
            // requestcatalog, Toast.LENGTH_SHORT).show();
            z = 0;
            install();
        }

        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);

            String[] colu = {"catalogname", "state"};
            Cursor c = myDB.query(requestcatalog + "_catalogs", colu,
                    null, null, null, null, null);
            int state = c.getColumnIndexOrThrow("state");
            if (c != null)
            {
                if (c.isFirst())
                {
                    do
                    {
                        state1 = c.getString(state);
                    }
                    while (c.moveToNext());
                }
            }

            if (state1.toString().equals("true"))
            {
                // Toast.makeText(Subscribe.this, "Fetching loop : " +
                // requestcatalog + "_catalogs", Toast.LENGTH_SHORT).show();
                fetch();
            }
            c.close();
        }
        catch (Exception e)
        {

        }

        Button subscribe = (Button) findViewById(R.id.subscribe);
        subscribe.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View view)
            {
                try
                {
                    Cursor mobeegalUserCursor = myDB.query("MobeegalUser",
                            null, null, null, null, null, null);
                    int UseridColumn = mobeegalUserCursor
                            .getColumnIndexOrThrow("UserID");
                    if (mobeegalUserCursor != null)
                    {
                        if (mobeegalUserCursor.isFirst())
                        {
                            userIDstring = mobeegalUserCursor
                                    .getString(UseridColumn);
                            // logger.info("mobeegalUserID = " + useridColumn);
                            logger.info("user Id :" + userIDstring);
                        }
                    }

                    // userIDstring= "499132";
                    logger.info("user Id :" + userIDstring);
                    js.object();
                    js.key("action").value("catalogs_subscription_type").key(
                            "query").object().key("id").value(userIDstring);
                    String[] column = {"modes"};
                    c2 = myDB.query("category", column,
                            "status='true'", null, null, null, "modes");
                    //				c2 = myDB.query(true, "category", column, null, null, null,
                    //						null, "modes");
                    modename = c2.getColumnIndexOrThrow("modes");
                    getcountcatname = c2.getCount();
                    logger.info("Modes count :" + getcountcatname);
                    if (c2 != null)
                    {
                        b = 0;
                        if (c2.isFirst())
                        {
                            do
                            {
                                modesname = c2.getString(modename);
                                logger.info("Mode name :" + modesname);
                                res1[b] = modesname;
                                b++;
                            }
                            while (c2.moveToNext());
                        }
                    }

                    for (int j = 0; j < getcountcatname; j++)
                    {
                        js.key(res1[j]).array();
                        String[] colus = {"categoryname"};
                        c3 = myDB.query("category", colus,
                                "status='true' and modes='" + res1[j] + "'",
                                null, null, null, null);
                        catname = c3.getColumnIndexOrThrow("categoryname");
                        logger.info("c3 count  :" + c3.getCount());
                        if (c3 != null)
                        {
                            if (c3.isFirst())
                            {
                                do
                                {
                                    catsname = c3.getString(catname);
                                    logger.info("category name :" + catsname);
                                    js.value(catsname);
                                }
                                while (c3.moveToNext());
                            }
                        }
                        js.endArray();
                    }
                    js.endObject().endObject();
                    String subscribecatalogs = js.toString();
                    logger.info("Subscription catalogs :" + subscribecatalogs);
                    ArrayList<NameValuePair> data =
                            new ArrayList<NameValuePair>();
                    data.add(new BasicNameValuePair("data_pack",
                            subscribecatalogs));
                    HttpPost httpPost = new HttpPost(
                            getString(R.string.CatalogServer));
                    httpPost.setEntity(
                            new UrlEncodedFormEntity(data, HTTP.UTF_8));
                    HttpResponse resp = httpclient.execute(httpPost);
                    String response = HttpUtils.getResponseString(resp);
                    Intent intent1 = new Intent(Subscribe.this,
                            FindandInstall.class);
                    startActivityForResult(intent1, 0);
                    c3.close();
                    c2.close();
                    mobeegalUserCursor.close();
                }
                catch (Exception e)
                {
                    Toast.makeText(Subscribe.this, "" + e, Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        Button selectall = (Button) findViewById(R.id.selectall);
        selectall.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View view)
            {

            }
        });
    }

    // code added
    private void install()
    {
        try
        {
            // this.createDatabase("Mobeegal", 1, MODE_PRIVATE, null);
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            myDB
                    .execSQL("CREATE TABLE IF NOT EXISTS "
                            + requestcatalog
                            +
                            "_catalogs(catalogID NUMERIC(3),catalogname VARCHAR,state VARCHAR,catalogtype VARCHAR);");
            myDB
                    .execSQL("CREATE TABLE IF NOT EXISTS "
                            + requestcatalog
                            +
                            "_category(categoryID NUMERIC(3),catalogID1 NUMERIC(3),categoryname VARCHAR,status VARCHAR,querystatus VARCHAR,catalogtype VARCHAR,modes VARCHAR);");
            myDB
                    .execSQL(
                            "CREATE TABLE IF NOT EXISTS catalogs(catalogID NUMERIC(3),catalogname VARCHAR,state VARCHAR,catalogtype VARCHAR);");
            myDB
                    .execSQL(
                            "CREATE TABLE IF NOT EXISTS category(categoryID NUMERIC(3),catalogID1 NUMERIC(3),categoryname VARCHAR,status VARCHAR,querystatus VARCHAR,catalogtype VARCHAR,modes VARCHAR,catalogname VARCHAR);");

            JSONStringer js = new JSONStringer();
            logger.info("HTTP Request:" + requestcatalog);
            js.object().key("action").value(requestcatalog);
            js.endObject();
            String catalogJSON = js.toString();
            // String key = "intellibitz";
            // EncryptionDecryption encryptDecrypt = new EncryptionDecryption();
            // String encrypted =
            // encryptDecrypt.EncryptionDecryption(catalogJSON, key);
            ArrayList<NameValuePair> data =
                    new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("data_pack",
                    catalogJSON));
            HttpPost httpPost = new HttpPost(
                    getString(R.string.CatalogServer));
            httpPost.setEntity(
                    new UrlEncodedFormEntity(data, HTTP.UTF_8));
            HttpResponse resp = httpclient.execute(httpPost);
            gettingcatalog = HttpUtils.getResponseString(resp);
            logger.info("HTTP Request:" + data);
            // String decrypted =
            // encryptDecrypt.EncryptionDecryption(gettingcatalog.trim(), key);
            logger.info("HTTP Response : " + gettingcatalog);
            JSONObject catalogJson = new JSONObject(gettingcatalog);
            logger.info("JSON Object : " + catalogJson);
            String strid = catalogJson.getString(requestcatalog);
            logger.info("string strid : " + strid);
            JSONObject catalogsJson = new JSONObject(strid);
            JSONArray catalogJsonKey = catalogsJson.names();
            logger.info("HTTP Response Array:" + catalogJsonKey);
            int catalogJsonKeylength = catalogJsonKey.length();
            logger.info("HTTP Response length:" + catalogJsonKeylength);
            String[] catalogJsonKeyStringArray =
                    new String[catalogJsonKeylength];
            Cursor c1 = myDB.query(requestcatalog + "_catalogs", null,
                    null, null, null, null, null);
            if (c1.getCount() < catalogJsonKeylength)
            {
                for (i = 0; i < catalogJsonKeylength; i++)
                {
                    String catalogJsonKeyString = catalogJsonKey.getString(i);
                    logger.info("key value : " + catalogJsonKeyString);
                    catalogJsonKeyStringArray[i] = catalogJsonKeyString;
                    logger.info("keyArray : " + catalogJsonKeyStringArray[i]);
                    catalogvalue = catalogsJson.getString(catalogJsonKeyString);
                    logger.info("keyArrayValue : " + catalogvalue);
                    myDB
                            .execSQL("INSERT INTO "
                                    + requestcatalog
                                    +
                                    "_catalogs (catalogID,catalogname,state,catalogtype) VALUES ("
                                    + i + ",'" + catalogJsonKeyStringArray[i]
                                    + "','" + "true" + "','" + requestcatalog
                                    + "');");
                    myDB
                            .execSQL(
                                    "INSERT INTO catalogs (catalogID,catalogname,state,catalogtype) VALUES ("
                                            + i
                                            + ",'"
                                            + catalogJsonKeyStringArray[i]
                                            + "','"
                                            + "true"
                                            + "','"
                                            + requestcatalog
                                            + "');");
                    logger.info("key value inserted : " + catalogvalue);
                    JSONArray catalogvalueJsonArray = catalogsJson
                            .getJSONArray(catalogJsonKeyString);
                    int jsonArrayLen = catalogvalueJsonArray.length();
                    if (c1.getCount() < jsonArrayLen)
                    {
                        for (int j = 0; j < jsonArrayLen; j++)
                        {
                            String catalogvalueofvalues = catalogvalueJsonArray
                                    .getString(j);
                            logger.info("Array value of value:"
                                    + catalogvalueofvalues);
                            myDB
                                    .execSQL("INSERT INTO "
                                            + requestcatalog
                                            +
                                            "_category (categoryID,catalogID1,categoryname,status,querystatus,catalogtype) VALUES ("
                                            + z + "," + i + ",'"
                                            + catalogvalueofvalues + "','"
                                            + "false" + "','" + "false" + "','"
                                            + requestcatalog + "');");
                            myDB
                                    .execSQL(
                                            "INSERT INTO category (categoryID,catalogID1,categoryname,status,querystatus,catalogtype,catalogname) VALUES ("
                                                    + z
                                                    + ","
                                                    + i
                                                    + ",'"
                                                    + catalogvalueofvalues
                                                    + "','"
                                                    + "false"
                                                    + "','"
                                                    + "false"
                                                    + "','"
                                                    + requestcatalog
                                                    + "','"
                                                    +
                                                    catalogJsonKeyStringArray[i]
                                                    + "');");
                            z++;
                        }
                    }
                }
            }
            c1.close();
        }
        catch (Exception e)
        {
            Log.i("JSON Exception : ", e.getMessage());
        }
    }

    // code added
    private void fetch()
    {
        logger.info(" fetch loop ");
        String[] colu = {"catalogname", "catalogID"};
        c = myDB.query(requestcatalog + "_catalogs", colu, null, null,
                null, null, null);
        AbsoluteLayout absoluteLayout =
                (AbsoluteLayout) findViewById(R.id.myTableLayout);
        catalogname = c.getColumnIndexOrThrow("catalogname");
        catalogid = c.getColumnIndexOrThrow("catalogID");
        logger.info(" fetch loop values : " + catalogname + catalogid);
        if (c != null)
        {
            a = 0;
            b = 0;
            catalogheight = 50;
            if (c.isFirst())
            {
                do
                {
                    catalogname1 = c.getString(catalogname);
                    catalogid1 = c.getString(catalogid);
                    res[a] = catalogname1;
                    TextView tv = new TextView(Subscribe.this);
                    tv.setTypeface(Typeface.create(res[a], Typeface.BOLD));
                    tv.setText(res[a]);
                    absoluteLayout.addView(tv, new AbsoluteLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT, 50, catalogheight));
                    /** code add */
                    String[] column = {"categoryname", "status"};
                    logger.info(" category loop start");
                    c2 = myDB.query(requestcatalog + "_category", column,
                            "catalogID1='" + catalogid1 + "'", null, null,
                            null, null);
                    logger.info(" fetch loop start 1 ");
                    getcount = c2.getCount();
                    categoryname = c2.getColumnIndexOrThrow("categoryname");
                    categorystatus = c2.getColumnIndexOrThrow("status");
                    if (c2 != null)
                    {
                        categoryheight = catalogheight + 10;
                        if (c2.isFirst())
                        {
                            do
                            {
                                categoryname1 = c2.getString(categoryname);
                                categorystatus1 = c2.getString(categorystatus);
                                res1[b] = categoryname1;
                                /** code add */
                                ch[b] = new CheckBox(Subscribe.this);
                                ch[b].setText(res1[b]);
                                ch[b].setFocusable(true);
                                if (categorystatus1.equalsIgnoreCase("true"))
                                {
                                    ch[b].setChecked(true);
                                }
                                else
                                {
                                    ch[b].setChecked(false);
                                }
                                absoluteLayout.addView(ch[b],
                                        new AbsoluteLayout.LayoutParams(
                                                LayoutParams.WRAP_CONTENT,
                                                LayoutParams.WRAP_CONTENT, 115,
                                                categoryheight));
                                categoryheight = categoryheight + 37;
                                /** code end */
                                b++;
                            }
                            while (c2.moveToNext());
                        }
                    }
                    /** code end */
                    a++;
                    catalogheight = categoryheight + 5;
                }
                while (c.moveToNext());
            }
        }
        c2.close();
        c.close();
        /** code new added */
        ch[0].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[0].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=0");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=0");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 0);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });
        ch[1].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[1].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=1");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=1");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 1);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });
        ch[2].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[2].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=2");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=2");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 2);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });
        ch[3].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[3].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=3");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=3");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 3);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });
        ch[4].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[4].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=4");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=4");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 4);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });
        ch[5].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[5].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=5");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=5");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 5);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });
        ch[6].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[6].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=6");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=6");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 6);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });
        ch[7].setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                if (ch[7].isChecked() == true)
                {
                    myDB.execSQL("update " + requestcatalog
                            + "_category set status='true' where categoryID=7");
                    myDB
                            .execSQL(
                                    "update category set status='true' where catalogtype='"
                                            + requestcatalog +
                                            "'and categoryID=7");
                    Bundle B1 = new Bundle();
                    B1.putString("requestcatalog", requestcatalog);
                    B1.putInt("requestno", 7);
                    Intent subscribe = new Intent(Subscribe.this, Modes.class);
                    subscribe.putExtras(B1);
                    startActivityForResult(subscribe, 0);
                }
            }
        });

        /** code ended */
    }

    // MenuView
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    // Menu Item
    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
            case 1:
                Intent stuffCheckintent = new Intent(Subscribe.this,
                        MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                finish();
                break;
            case 2:
                Intent intent1 =
                        new Intent(Subscribe.this, FindandInstall.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 3:
                Intent settings = new Intent(Subscribe.this, Settings.class);
                startActivityForResult(settings, 0);
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
