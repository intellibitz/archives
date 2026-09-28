package com.mobeegal.android.activity

import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import com.android.internal.http.multipart.MultipartEntity
import com.android.internal.http.multipart.Part
import com.android.internal.http.multipart.StringPart
import com.mobeegal.android.R
import com.mobeegal.android.model.IconifiedText
import com.mobeegal.android.util.HttpUtils
import com.mobeegal.android.view.IconifiedTextListAdapter
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import java.io.IOException
import java.util.Collections

class ViewMedia : ListActivity() {

    private var category: String? = null
    private var mstuffid: String? = null
    private val directoryEntries: MutableList<IconifiedText> =
        ArrayList()
    private var currentDirectory = ""
    private val folders = arrayOf("Image", "Audio", "Video")
    private var myDB: SQLiteDatabase? = null
    private var userId = ""
    var res: Array<String>? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        try {
            myDB = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val c = myDB!!.query(
                "MobeegalUser", null, null, null, null, null,
                null
            )
            if (c != null) {
                if (c.isFirst) {
                    userId = c.getString(c.getColumnIndexOrThrow("UserID"))
                    //Toast.makeText(this, userId, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (e: Exception) {
        }
        setTheme(android.R.style.Theme_Black)
        val b = this.intent.extras
        if (b != null) {
            category = b.getString("category")
            mstuffid = b.getString("mstuffid")
        }
        fill(folders)
    }

    private fun fill(folders2: Array<String>) {
        // TODO Auto-generated method stub
        this.directoryEntries.clear()
        /*if (this.currentDirectory != null) {
              this.directoryEntries.add(new IconifiedText("..", getResources()
                      .getDrawable(R.drawable.uponelevel)));
          }*/
        var currentIcon: Drawable? = null
        for (currentString in folders) {
            currentIcon = resources.getDrawable(R.drawable.folder)
            this.directoryEntries
                .add(IconifiedText(currentString, currentIcon))
        }

        Collections.sort(this.directoryEntries)
        val itla = IconifiedTextListAdapter(this)
        itla.setListItems(this.directoryEntries)
        this.listAdapter = itla
    }

    private fun fill1(folders2: Array<String>?) {
        // TODO Auto-generated method stub
        this.directoryEntries.clear()
        if (this.currentDirectory != null) {
            this.directoryEntries.add(
                IconifiedText(
                    "..", resources
                        .getDrawable(R.drawable.uponelevel)
                )
            )
        }
        var currentIcon: Drawable? = null
        for (currentString in res!!) {
            if (checkEndsWithInStringArray(
                    currentString, resources
                        .getStringArray(R.array.fileEndingImage)
                )
            ) {
                currentIcon = resources.getDrawable(R.drawable.image)
            }
            //currentIcon = getResources().getDrawable(R.drawable.folder);
            this.directoryEntries
                .add(IconifiedText(currentString, currentIcon))
        }

        Collections.sort(this.directoryEntries)
        val itla = IconifiedTextListAdapter(this)
        itla.setListItems(this.directoryEntries)
        this.listAdapter = itla
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        //int selectionRowID = (int) this.getSelectionRowID();
        val selectedFileString =
            this.directoryEntries[position].getText()
        if (selectedFileString == "..") {
            this.upOneLevel()
        } else if (selectedFileString == "...") {
            fill1(res)
        } else if (selectedFileString == "Image") {
            requestResponse("Image")
        } else if (selectedFileString == "Audio") {
            requestResponse("Audio")
        } else if (selectedFileString == "Video") {
            requestResponse("Video")
        } else {
            val clickedFile: String? = null
            viewFile(selectedFileString)
            //clickedFile = new File(this.directoryEntries.get(position).getText());
        }
    }

    private fun viewFile(viewfile: String) {
        if (checkEndsWithInStringArray(
                viewfile,
                resources.getStringArray(R.array.fileEndingImage)
            )
        ) {
            val uploadingimage = "http://192.168.1.68/" + viewfile
            val uploadimage = Bundle()
            val myIntent1 = Intent(this@ViewMedia, UploadGallery::class.java)
            uploadimage.putString("key", uploadingimage)
            myIntent1.putExtras(uploadimage)
            startActivityForResult(myIntent1, 0)
        } else if (checkEndsWithInStringArray(
                viewfile,
                resources.getStringArray(R.array.fileEndingVideo)
            )
        ) {
            val uploadingFile = "http://192.168.1.68/" + viewfile
            val uploadfile = Bundle()
            val myIntent1 = Intent(this@ViewMedia, PlayMedia::class.java)
            uploadfile.putString("key", uploadingFile)
            uploadfile.putString("key1", "Video File")
            myIntent1.putExtras(uploadfile)
            startActivityForResult(myIntent1, 0)
        } else if (checkEndsWithInStringArray(
                viewfile,
                resources.getStringArray(R.array.fileEndingAudio)
            )
        ) {
            val uploadingFile = "http://192.168.1.68/" + viewfile
            val uploadfile = Bundle()
            val myIntent1 = Intent(this@ViewMedia, PlayMedia::class.java)
            uploadfile.putString("key", uploadingFile)
            uploadfile.putString("key1", "Audio File")
            myIntent1.putExtras(uploadfile)
            startActivityForResult(myIntent1, 0)
        } else {
            Toast.makeText(
                this@ViewMedia, "FileFormat not Supported",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestResponse(string: String) {
        // TODO Auto-generated method stub
        val httpclient = DefaultHttpClient()
        //        NameValuePair[] data = {new NameValuePair("action", "media_view"), new NameValuePair("userid", "499131"), new NameValuePair("matchid", "1234"), new NameValuePair("category", "Dating"), new NameValuePair("media_type", "Image")};
        val parts = arrayOf<Part>(
            StringPart("action", "media_view"),
            StringPart("userid", userId),
            StringPart("matchid", userId),
            StringPart("category", "Dating"),
            StringPart("media_type", string)
        )

        var httpPost: HttpPost? = null
        httpPost = HttpPost(getString(R.string.MediaServer))
        httpPost.entity = MultipartEntity(parts, httpPost.params)

        try {
            val resp = httpclient.execute(httpPost)
            val response = HttpUtils.getResponseString(resp)
            Log.i(
                "response/////////////////..................................",
                response
            )
            res = response.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            //Log.i("res[0]/////////////////", res[1]);

            fill1(res)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun upOneLevel() {
        // TODO Auto-generated method stub
        fill(folders)
    }

    private fun checkEndsWithInStringArray(
        checkItsEnd: String,
        fileEndings: Array<String>
    ): Boolean {
        for (aEnd in fileEndings) {
            if (checkItsEnd.endsWith(aEnd)) {
                return true
            }
        }
        return false
    }
}
