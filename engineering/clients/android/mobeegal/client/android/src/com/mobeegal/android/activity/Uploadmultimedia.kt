package com.mobeegal.android.activity

/*
<!--
$Id:: Uploadmultimedia.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
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
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.android.internal.http.multipart.FilePart
import com.android.internal.http.multipart.MultipartEntity
import com.android.internal.http.multipart.Part
import com.android.internal.http.multipart.StringPart
import com.mobeegal.android.R
import com.mobeegal.android.util.HttpUtils
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import java.io.BufferedInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.logging.Logger

class Uploadmultimedia : Activity() {

    var upload_audioFile: String? = null
    var upload_videoFile: String? = null
    var upload_image: String? = null
    var uploadaudioFilename: TextView? = null
    var uploadvideoFilename: TextView? = null
    var value1: String? = null
    var v2: String? = null
    private var i: ImageView? = null
    var myDatabase: SQLiteDatabase? = null
    var count: Int = 0
    var key: Int = 0
    var image1: Array<String>? = null
    var image2: Array<String>? = null
    private var viewimage1: String? = null
    private var viewaudio1: String? = null
    private var viewvideo1: String? = null
    private var uploadbutton: Button? = null
    private var multimediaSpinner: Spinner? = null
    var adapter: ArrayAdapter<CharSequence>? = null
    private var selectedcatalogs: String? = null
    var b: Bundle? = null
    var rows: Int = 0
    var count1: Int = 0
    var c: Cursor? = null
    var subscribedCategory: Array<String>? = null
    var httpclient: HttpClient? = null
    //    PostMethod httpPost;
    var file: File? = null
    var filePart: FilePart? = null
    var userId: String? = null
    private var requestcatalog: String? = null
    private var catalogs: String? = null
    private var category: TextView? = null
    private var httpPost: HttpPost? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.multimedia)
        val B2 = this.intent.extras
        if (B2 != null) {
            requestcatalog = B2.getString("requestcatalog")
            catalogs = requestcatalog
        }
        category = findViewById(R.id.catalogmedia) as TextView
        category!!.text = catalogs
        uploadaudioFilename = findViewById(R.id.uplaoaudio) as TextView
        uploadvideoFilename = findViewById(R.id.uplaovideo) as TextView
        uploadbutton = findViewById(R.id.sendmedia) as Button
        //    multimediaSpinner = (Spinner) findViewById(R.id.multimediacategory);
        i = findViewById(R.id.catalogimgupload) as ImageView
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
        } catch (e1: Exception) {
        }
        try {
            val col = arrayOf("iStuffImage", "iStuffVideo", "iStuffAudio")
            val c = myDatabase!!
                .query("Upload", col, null, null, null, null, null)
            val viewimage = c.getColumnIndexOrThrow("iStuffImage")
            val viewvideo = c.getColumnIndexOrThrow("iStuffVideo")
            val viewaudio = c.getColumnIndexOrThrow("iStuffAudio")

            if (c != null) {
                if (c.isFirst) {
                    do {
                        viewimage1 = c.getString(viewimage)
                        viewaudio1 = c.getString(viewaudio)
                        viewvideo1 = c.getString(viewvideo)
                    } while (c.moveToNext())
                }
            }
            if (c.count == 0) {
                b = this.intent.extras
            } else {
                try {
                    val aURL = URL("file://" + viewimage1)

                    try {
                        val conn = aURL.openConnection()
                        conn.connect()
                        val `is` = conn.getInputStream()
                        val bis = BufferedInputStream(`is`)
                        val bm = BitmapFactory.decodeStream(bis)
                        bis.close()
                        `is`.close()
                        i!!.setImageBitmap(bm)
                    } catch (ioe: IOException) {
                    }
                } catch (exc: MalformedURLException) {
                }
                try {
                    uploadaudioFilename!!.text = viewaudio1
                } catch (e: Exception) {
                }
                try {
                    uploadvideoFilename!!.text = viewvideo1
                } catch (e1: Exception) {
                }
            }
        } catch (e: Exception) {
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS Upload" +
                    " (iStuffImage VARCHAR,iStuffVideo VARCHAR,iStuffAudio VARCHAR,status VARCHAR);"
            )
            myDatabase!!.execSQL(
                "INSERT INTO Upload (status) VALUES ('" + "upload" + "');"
            )
        }
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val c = myDatabase!!
                .query("MobeegalUser", null, null, null, null, null, null)
            if (c != null) {
                if (c.isFirst) {
                    userId = c.getString(c.getColumnIndexOrThrow("UserID"))
                    //Toast.makeText(this, userId, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (e: Exception) {
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
        b = this.intent.extras
        try {
            if (b != null) {
                value1 = b!!.getString("key1")
                if (value1 == "1") {
                    upload_audioFile = b!!.getString("key")
                    uploadaudioFilename!!.text = upload_audioFile
                    myDatabase!!.execSQL(
                        "UPDATE Upload set iStuffAudio='" +
                            upload_audioFile + "' where status='upload';"
                    )
                } else if (value1 == "0") {
                    upload_image = b!!.getString("key")

                    try {
                        val aURL = URL("file://" + upload_image)

                        try {
                            val conn = aURL.openConnection()
                            conn.connect()
                            val `is` = conn.getInputStream()
                            val bis =
                                BufferedInputStream(`is`)
                            val bm = BitmapFactory.decodeStream(bis)
                            bis.close()
                            `is`.close()
                            i!!.setImageBitmap(bm)
                            myDatabase!!.execSQL(
                                "UPDATE Upload set iStuffImage='" +
                                    upload_image +
                                    "' where status='upload';"
                            )
                        } catch (ioe: IOException) {
                        }
                    } catch (exc: MalformedURLException) {
                    }
                } else {
                    upload_videoFile = b!!.getString("key")
                    uploadvideoFilename!!.text = upload_videoFile
                    myDatabase!!.execSQL(
                        "UPDATE Upload set iStuffVideo='" +
                            upload_videoFile + "' where status='upload';"
                    )
                }
            }
        } catch (e: NullPointerException) {
            logger.info("error message = " + e.message)
        }
        uploadbutton!!.setOnClickListener { arg0 ->
            if ((upload_image == null) && (upload_audioFile == null) &&
                (upload_videoFile == null)
            ) {
                Toast.makeText(
                    this@Uploadmultimedia, "No File Selected",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (upload_image != null) {
                sendtoServer(upload_image!!, "Image")
                upload_image = null
            } else if (upload_audioFile != null) {
                sendtoServer(upload_audioFile!!, "Audio")
                upload_audioFile = null
            } else if (upload_videoFile != null) {
                sendtoServer(upload_videoFile!!, "Video")
                upload_videoFile = null
            } else {
                Toast.makeText(
                    this@Uploadmultimedia, "Select a file.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun sendtoServer(string: String, media: String) {
        try {
            file = File(string)
            httpclient = DefaultHttpClient()

            httpPost = HttpPost(
                applicationContext.getString(R.string.MediaServer)
            )
            //            httpPost = new PostMethod(getString(R.string.MediaServer));
        } catch (ex: Exception) {
            Toast.makeText(this, "Connection Lost", Toast.LENGTH_SHORT).show()
        }
        try {
            filePart = FilePart("userfile", file)
        } catch (e1: FileNotFoundException) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "no file", Toast.LENGTH_SHORT).show()
        }
        val parts = arrayOf<Part>(
            StringPart("action", "file_upload"),
            StringPart("userid", userId),
            StringPart("category", requestcatalog),
            StringPart("media_type", media), filePart!!
        )
        httpPost!!.entity =
            MultipartEntity(parts, httpPost!!.params)

        try {
            val resp = httpclient!!.execute(httpPost)
            val res = HttpUtils.getResponseString(resp)
            Log.i("............", res)
            Toast.makeText(
                this, "Successfully posted to server",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Uplaoading failed.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val ret = super.onCreateOptionsMenu(menu)
        menu.add(0, 1, R.string.mstuffs, "My Stuff")
        menu.add(0, 2, R.string.catalogs, "Catalogs")
        menu.add(0, 3, R.string.settings, "Settings")
        menu.add(0, 4, R.string.shareimage, "Share Image")
        menu.add(0, 5, R.string.shareaudio, "Share Audio")
        menu.add(0, 6, R.string.sharevideo, "Share Video")
        return ret
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val stuffCheckintent =
                    Intent(this@Uploadmultimedia, MapResults::class.java)
                startActivity(stuffCheckintent)
            }
            2 -> {
                val intent1 =
                    Intent(this@Uploadmultimedia, FindandInstall::class.java)
                startActivity(intent1)
            }
            3 -> {
                val settings =
                    Intent(this@Uploadmultimedia, Settings::class.java)
                startActivity(settings)
            }
            4 -> {
                val b1 = Bundle()
                val upload =
                    Intent(this@Uploadmultimedia, AndroidBrowser::class.java)
                b1.putString("value1", "0")
                upload.putExtras(b1)
                startActivityForResult(upload, 0)
            }
            5 -> {
                val b2 = Bundle()
                val uploadaudiofiles =
                    Intent(this@Uploadmultimedia, AndroidBrowser::class.java)
                b2.putString("value1", "1")
                uploadaudiofiles.putExtras(b2)
                startActivityForResult(uploadaudiofiles, 0)
            }
            6 -> {
                val b3 = Bundle()
                val uploadvideofiles =
                    Intent(this@Uploadmultimedia, AndroidBrowser::class.java)
                b3.putString("value1", "2")
                uploadvideofiles.putExtras(b3)
                startActivityForResult(uploadvideofiles, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val logger = Logger.getLogger("Testcatalogs1")
    }
}
