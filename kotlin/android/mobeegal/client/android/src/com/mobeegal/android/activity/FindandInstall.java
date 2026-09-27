package com.mobeegal.android.activity;

/*
<!--
$Id:: FindandInstall.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
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
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author work
 */
public class FindandInstall
        extends Activity
{

    private String[] res1 = new String[4];
    private String catalogname1;
    private int b = 0;
    private int getcountcatname;
    private int catalogname;
    private String catalogvalue;
    private int i;
    SQLiteDatabase myDB = null;
    private String gettingcatalog;
    HttpClient httpclient = new DefaultHttpClient();
    static int z = 0;
    static int r = 0;
    Cursor c;
    private String requestcatalog = "";
    Button catalogbutton, catalogbutton1;
    private int n = 0;
    private String strid;
    private static Logger logger = Logger.getLogger("find and install");

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.catalogs);
        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String[] col = {"UserID"};
            Cursor c = myDB.query("MobeegalUser", col, null, null, null, null,
                    null);
            if (c.getCount() == 0)
            {
                String register = "register";
                String IMSI = "456957013123457";
                JSONStringer js = new JSONStringer();
// new request 
                js.object().key("action").value(register).key("group")
                        .value("Nokia").key("query").object().key("IMSI")
                        .value(IMSI).endObject();
                // key("query").object().key("IMSI").value(myIMSI).endObject();
                js.endObject();
                String registerJson = js.toString();
                logger.info("Sending IMSI in JSON = " + registerJson);
                HttpClient httpclient = new DefaultHttpClient();
                String key = "intellibitz";
                // EncryptionDecryption encryptDecrypt = new
                // EncryptionDecryption();
                // String encrypted =
                // encryptDecrypt.EncryptionDecryption(registerJson, key);
                ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
                data.add(new BasicNameValuePair("data_pack",
                        registerJson));
                HttpPost httpPost = new HttpPost(
                        getString(R.string.CatalogServer));
                httpPost.setEntity(new UrlEncodedFormEntity(data, HTTP.UTF_8));
                HttpResponse resp = httpclient.execute(httpPost);
                String response = HttpUtils.getResponseString(resp);
//                String request = httpPost.getQueryString();
                // String decrypted =
                // encryptDecrypt.EncryptionDecryption(response.trim(), key);
//                logger.info("request =" + request);
                logger.info("response =" + response);
                // logger.info("encrypted " + encrypted);
                // logger.info("decrypted " + decrypted);
                JSONObject jo = new JSONObject(response);
                strid = jo.getString("id");
                myDB.execSQL(
                        "INSERT INTO MobeegalUser (IMSI, UserID) VALUES ('" +
                                IMSI + "','" + strid + "');");
                startService(new Intent(
                        "com.mobeegal.android.service.REMOTE_SERVICE"));
                myDB.execSQL(
                        "update serviceactivation set status='0' where service='deactivate';");
            }
            c.close();
        }
        catch (Exception e)
        {
            //	logger.info(" IMSI in catch block");
        }

        Button mostpopular = (Button) findViewById(R.id.mostpopular);
        mostpopular.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View view)
            {
                requestcatalog = "mostpopular";
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", requestcatalog);
                Intent install = new Intent(FindandInstall.this,
                        Subscribe.class);
                install.putExtras(B1);
                startActivityForResult(install, 0);
            }
        });

        Button popular = (Button) findViewById(R.id.popular);
        popular.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View view)
            {
                requestcatalog = "popular";
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", requestcatalog);
                Intent install = new Intent(FindandInstall.this,
                        Subscribe.class);
                install.putExtras(B1);
                startActivityForResult(install, 0);
            }
        });

        Button otherpopular = (Button) findViewById(R.id.otherpopular);
        otherpopular.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View view)
            {
                requestcatalog = "othercatalog";
                Bundle B1 = new Bundle();
                B1.putString("requestcatalog", requestcatalog);
                Intent install = new Intent(FindandInstall.this,
                        Subscribe.class);
                install.putExtras(B1);
                startActivityForResult(install, 0);
            }
        });

        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String[] colu = {"modes"};
            c = myDB.query("category", colu, "status='true'", null, null,
                    null, "modes");
            catalogname = c.getColumnIndexOrThrow("modes");
            getcountcatname = c.getCount();

            if (c != null)
            {
                b = 0;
                if (c.isFirst())
                {
                    do
                    {
                        catalogname1 = c.getString(catalogname);
                        res1[b] = catalogname1;
                        b++;

                    }
                    while (c.moveToNext());
                }
            }
            c.close();
            GridView g = (GridView) findViewById(R.id.myGrid);
            g.setAdapter(new ImageAdapter(this));
        }
        catch (Exception e)
        {
            // Toast.makeText(Catalogs.this, "" + e, Toast.LENGTH_LONG).show();
        }
    }

    public class ImageAdapter
            extends BaseAdapter
    {

        public ImageAdapter(Context c)
        {
            mContext = c;
        }

        public int getCount()
        {
            return getcountcatname;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(final int position, View convertView,
                ViewGroup parent)
        {

            for (n = 0; n < getcountcatname; n++)
            {
                catalogbutton = new Button(mContext);
                catalogbutton.setOnClickListener(new Button.OnClickListener()
                {

                    public void onClick(View view)
                    {
                        logger.info("position :" + position);
                        Bundle categoryBundle = new Bundle();
                        categoryBundle.putInt("position", position);
                        Intent install = new Intent(mContext,
                                CategoryList.class);
                        install.putExtras(categoryBundle);
                        startActivityForResult(install, 0);
                    }
                });
                catalogbutton.setLayoutParams(new Gallery.LayoutParams(80, 50));
                catalogbutton.setPadding(9, 9, 9, 9);
                catalogbutton.setText(res1[position]);
                return catalogbutton;
            }
            return parent;
        }

        private Context mContext;
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
                Intent stuffCheckintent = new Intent(FindandInstall.this,
                        MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                finish();
                break;
            case 2:
                Intent intent1 = new Intent(FindandInstall.this,
                        FindandInstall.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 3:
                Intent settings =
                        new Intent(FindandInstall.this, Settings.class);
                startActivityForResult(settings, 0);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
