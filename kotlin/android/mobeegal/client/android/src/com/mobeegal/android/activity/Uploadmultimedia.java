package com.mobeegal.android.activity;

/*
<!--
$Id:: Uploadmultimedia.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;
import com.mobeegal.android.R;
import com.mobeegal.android.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public class Uploadmultimedia
        extends Activity
{

    String upload_audioFile;
    String upload_videoFile;
    String upload_image;
    TextView uploadaudioFilename;
    TextView uploadvideoFilename;
    private static Logger logger = Logger.getLogger("Testcatalogs1");
    public String value1;
    public String v2;
    private ImageView i;
    SQLiteDatabase myDatabase = null;
    int count;
    int key = 0;
    String[] image1;
    String[] image2;
    private String viewimage1;
    private String viewaudio1;
    private String viewvideo1;
    private Button uploadbutton;
    private Spinner multimediaSpinner;
    ArrayAdapter<CharSequence> adapter;
    private String selectedcatalogs;
    Bundle b;
    int rows;
    int count1;
    Cursor c;
    String[] subscribedCategory;
    HttpClient httpclient;
    //    PostMethod httpPost;
    File file;
    FilePart filePart;
    String userId;
    private String requestcatalog;
    private String catalogs;
    private TextView category;
    private HttpPost httpPost;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.multimedia);
        Bundle B2 = this.getIntent().getExtras();
        if (B2 != null)
        {
            requestcatalog = B2.getString("requestcatalog");
            catalogs = requestcatalog;
        }
        category = (TextView) findViewById(R.id.catalogmedia);
        category.setText(catalogs);
        uploadaudioFilename = (TextView) findViewById(R.id.uplaoaudio);
        uploadvideoFilename = (TextView) findViewById(R.id.uplaovideo);
        uploadbutton = (Button) findViewById(R.id.sendmedia);
        //    multimediaSpinner = (Spinner) findViewById(R.id.multimediacategory);
        i = (ImageView) findViewById(R.id.catalogimgupload);
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
        }
        catch (Exception e1)
        {
        }
        try
        {

            String[] col = {"iStuffImage", "iStuffVideo", "iStuffAudio"};
            Cursor c = myDatabase
                    .query("Upload", col, null, null, null, null, null);
            int viewimage = c.getColumnIndexOrThrow("iStuffImage");
            int viewvideo = c.getColumnIndexOrThrow("iStuffVideo");
            int viewaudio = c.getColumnIndexOrThrow("iStuffAudio");

            if (c != null)
            {
                if (c.isFirst())
                {
                    do
                    {
                        viewimage1 = c.getString(viewimage);
                        viewaudio1 = c.getString(viewaudio);
                        viewvideo1 = c.getString(viewvideo);
                    }
                    while (c.moveToNext());
                }
            }
            if (c.getCount() == 0)
            {
                b = this.getIntent().getExtras();

            }
            else
            {
                try
                {
                    URL aURL = new URL("file://" + viewimage1);

                    try
                    {
                        URLConnection conn = aURL.openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        Bitmap bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        is.close();
                        i.setImageBitmap(bm);

                    }
                    catch (IOException ioe)
                    {


                    }
                }
                catch (MalformedURLException exc)
                {

                }
                try
                {
                    uploadaudioFilename.setText(viewaudio1);
                }
                catch (Exception e)
                {
                }
                try
                {
                    uploadvideoFilename.setText(viewvideo1);
                }
                catch (Exception e1)
                {
                }

            }

        }
        catch (Exception e)
        {
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Upload" +
                    " (iStuffImage VARCHAR,iStuffVideo VARCHAR,iStuffAudio VARCHAR,status VARCHAR);");
            myDatabase.execSQL(
                    "INSERT INTO Upload (status) VALUES ('" + "upload" + "');");

        }
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor c = myDatabase
                    .query("MobeegalUser", null, null, null, null, null, null);
            if (c != null)
            {
                if (c.isFirst())
                {
                    userId = c.getString(c.getColumnIndexOrThrow("UserID"));
                    //Toast.makeText(this, userId, Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception e)
        {

        }
        /*try {

            String myCols[] = {"categoryname"};
            c = myDatabase.query(true, "category", myCols, "status='true'", null, null, null, null);
            rows = c.getCount();
            subscribedCategory = new String[rows];
            int categorycolumn = c.getColumnIndexOrThrow("categoryname");
            if (c != null) {
                if (c.isFirst()) {
                    count = 0;
                    do {
                        subscribedCategory[count1] = c.getString(categorycolumn);
                        count1++;
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            Toast.makeText(Uploadmultimedia.this, "", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> categoryadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subscribedCategory);
        multimediaSpinner.setAdapter(categoryadapter);
        multimediaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView parent, View v,
                    int position, long id) {
                selectedcatalogs = (String) multimediaSpinner.getSelectedItem();
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });*/
        b = this.getIntent().getExtras();
        try
        {
            if (b != null)
            {
                value1 = b.getString("key1");
                if (value1.equals("1"))
                {
                    upload_audioFile = b.getString("key");
                    uploadaudioFilename.setText(upload_audioFile);
                    myDatabase.execSQL("UPDATE Upload set iStuffAudio='" +
                            upload_audioFile + "' where status='upload';");

                }
                else if (value1.equals("0"))
                {
                    upload_image = b.getString("key");

                    try
                    {
                        URL aURL = new URL("file://" + upload_image);

                        try
                        {
                            URLConnection conn = aURL.openConnection();
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            BufferedInputStream bis =
                                    new BufferedInputStream(is);
                            Bitmap bm = BitmapFactory.decodeStream(bis);
                            bis.close();
                            is.close();
                            i.setImageBitmap(bm);
                            myDatabase.execSQL(
                                    "UPDATE Upload set iStuffImage='" +
                                            upload_image +
                                            "' where status='upload';");

                        }
                        catch (IOException ioe)
                        {

                        }
                    }
                    catch (MalformedURLException exc)
                    {

                    }

                }
                else
                {
                    upload_videoFile = b.getString("key");
                    uploadvideoFilename.setText(upload_videoFile);
                    myDatabase.execSQL("UPDATE Upload set iStuffVideo='" +
                            upload_videoFile + "' where status='upload';");

                }
            }
        }
        catch (NullPointerException e)
        {
            logger.info("error message = " + e.getMessage());
        }
        uploadbutton.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                if ((upload_image == null) && (upload_audioFile == null) &&
                        (upload_videoFile == null))
                {
                    Toast.makeText(Uploadmultimedia.this, "No File Selected",
                            Toast.LENGTH_SHORT).show();
                }
                else if (upload_image != null)
                {
                    sendtoServer(upload_image, "Image");
                    upload_image = null;
                }
                else if (upload_audioFile != null)
                {
                    sendtoServer(upload_audioFile, "Audio");
                    upload_audioFile = null;
                }
                else if (upload_videoFile != null)
                {
                    sendtoServer(upload_videoFile, "Video");
                    upload_videoFile = null;
                }
                else
                {
                    Toast.makeText(Uploadmultimedia.this, "Select a file.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void sendtoServer(String string, String media)
    {

        try
        {
            file = new File(string);
            httpclient = new DefaultHttpClient();

            httpPost = new HttpPost(
                    getApplicationContext().getString(R.string.MediaServer));
//            httpPost = new PostMethod(getString(R.string.MediaServer));
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Connection Lost", Toast.LENGTH_SHORT).show();
        }
        try
        {
            filePart = new FilePart("userfile", file);
        }
        catch (FileNotFoundException e1)
        {
            // TODO Auto-generated catch block
            Toast.makeText(this, "no file", Toast.LENGTH_SHORT).show();
        }
        Part[] parts = {new StringPart("action", "file_upload"),
                new StringPart("userid", userId),
                new StringPart("category", requestcatalog),
                new StringPart("media_type", media), filePart};
        httpPost.setEntity(
                new MultipartEntity(parts, httpPost.getParams()));

        try
        {
            HttpResponse resp = httpclient.execute(httpPost);
            String res = HttpUtils.getResponseString(resp);
            Log.i("............", res);
            Toast.makeText(this, "Successfully posted to server",
                    Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Uplaoading failed.", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.add(0, 1, R.string.mstuffs, "My Stuff");
        menu.add(0, 2, R.string.catalogs, "Catalogs");
        menu.add(0, 3, R.string.settings, "Settings");
        menu.add(0, 4, R.string.shareimage, "Share Image");
        menu.add(0, 5, R.string.shareaudio, "Share Audio");
        menu.add(0, 6, R.string.sharevideo, "Share Video");
        return ret;
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {

            case 1:
                Intent stuffCheckintent =
                        new Intent(Uploadmultimedia.this, MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 =
                        new Intent(Uploadmultimedia.this, FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(Uploadmultimedia.this, Settings.class);
                startActivity(settings);
                break;

            case 4:

                Bundle b1 = new Bundle();
                Intent upload =
                        new Intent(Uploadmultimedia.this, AndroidBrowser.class);
                b1.putString("value1", "0");
                upload.putExtras(b1);
                startActivityForResult(upload, 0);

                break;
            case 5:

                Bundle b2 = new Bundle();
                Intent uploadaudiofiles =
                        new Intent(Uploadmultimedia.this, AndroidBrowser.class);
                b2.putString("value1", "1");
                uploadaudiofiles.putExtras(b2);
                startActivityForResult(uploadaudiofiles, 0);
                break;
            case 6:

                Bundle b3 = new Bundle();
                Intent uploadvideofiles =
                        new Intent(Uploadmultimedia.this, AndroidBrowser.class);
                b3.putString("value1", "2");
                uploadvideofiles.putExtras(b3);
                startActivityForResult(uploadvideofiles, 0);
                break;
        }
        return super.onOptionsItemSelected(item);


    }
}
