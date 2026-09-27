package com.mobeegal.android.activity;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;
import com.mobeegal.android.R;
import com.mobeegal.android.model.IconifiedText;
import com.mobeegal.android.util.HttpUtils;
import com.mobeegal.android.view.IconifiedTextListAdapter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewMedia
        extends ListActivity
{

    private String category, mstuffid;
    private List<IconifiedText> directoryEntries =
            new ArrayList<IconifiedText>();
    private String currentDirectory = "";
    private String[] folders = {"Image", "Audio", "Video"};
    private SQLiteDatabase myDB;
    private String userId = "";
    String[] res;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            Cursor c = myDB.query("MobeegalUser", null, null, null, null, null,
                    null);
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
        setTheme(android.R.style.Theme_Black);
        Bundle b = this.getIntent().getExtras();
        if (b != null)
        {
            category = b.getString("category");
            mstuffid = b.getString("mstuffid");
        }
        fill(folders);
    }

    private void fill(String[] folders2)
    {
        // TODO Auto-generated method stub
        this.directoryEntries.clear();
        /*if (this.currentDirectory != null) {
              this.directoryEntries.add(new IconifiedText("..", getResources()
                      .getDrawable(R.drawable.uponelevel)));
          }*/
        Drawable currentIcon = null;
        for (String currentString : folders)
        {
            currentIcon = getResources().getDrawable(R.drawable.folder);
            this.directoryEntries
                    .add(new IconifiedText(currentString, currentIcon));
        }

        Collections.sort(this.directoryEntries);
        IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this);
        itla.setListItems(this.directoryEntries);
        this.setListAdapter(itla);
    }

    private void fill1(String[] folders2)
    {
        // TODO Auto-generated method stub
        this.directoryEntries.clear();
        if (this.currentDirectory != null)
        {
            this.directoryEntries.add(new IconifiedText("..", getResources()
                    .getDrawable(R.drawable.uponelevel)));
        }
        Drawable currentIcon = null;
        for (String currentString : res)
        {
            if (checkEndsWithInStringArray(currentString, getResources().
                    getStringArray(R.array.fileEndingImage)))
            {
                currentIcon = getResources().getDrawable(R.drawable.image);
            }
            //currentIcon = getResources().getDrawable(R.drawable.folder);
            this.directoryEntries
                    .add(new IconifiedText(currentString, currentIcon));
        }

        Collections.sort(this.directoryEntries);
        IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this);
        itla.setListItems(this.directoryEntries);
        this.setListAdapter(itla);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        //int selectionRowID = (int) this.getSelectionRowID();
        String selectedFileString =
                this.directoryEntries.get(position).getText();
        if (selectedFileString.equals(".."))
        {
            this.upOneLevel();
        }
        else if (selectedFileString.equals("..."))
        {
            fill1(res);
        }
        else if (selectedFileString.equals("Image"))
        {
            requestResponse("Image");

        }
        else if (selectedFileString.equals("Audio"))
        {
            requestResponse("Audio");
        }
        else if (selectedFileString.equals("Video"))
        {
            requestResponse("Video");
        }
        else
        {
            String clickedFile = null;
            viewFile(selectedFileString);
            //clickedFile = new File(this.directoryEntries.get(position).getText());

        }

    }


    private void viewFile(String viewfile)
    {
        if (checkEndsWithInStringArray(viewfile,
                getResources().getStringArray(R.array.fileEndingImage)))
        {
            String uploadingimage = "http://192.168.1.68/" + viewfile;
            Bundle uploadimage = new Bundle();
            Intent myIntent1 = new Intent(ViewMedia.this, UploadGallery.class);
            uploadimage.putString("key", uploadingimage);
            myIntent1.putExtras(uploadimage);
            startActivityForResult(myIntent1, 0);
        }
        else if (checkEndsWithInStringArray(viewfile,
                getResources().getStringArray(R.array.fileEndingVideo)))
        {
            String uploadingFile = "http://192.168.1.68/" + viewfile;
            Bundle uploadfile = new Bundle();
            Intent myIntent1 = new Intent(ViewMedia.this, PlayMedia.class);
            uploadfile.putString("key", uploadingFile);
            uploadfile.putString("key1", "Video File");
            myIntent1.putExtras(uploadfile);
            startActivityForResult(myIntent1, 0);
        }
        else if (checkEndsWithInStringArray(viewfile,
                getResources().getStringArray(R.array.fileEndingAudio)))
        {
            String uploadingFile = "http://192.168.1.68/" + viewfile;
            Bundle uploadfile = new Bundle();
            Intent myIntent1 = new Intent(ViewMedia.this, PlayMedia.class);
            uploadfile.putString("key", uploadingFile);
            uploadfile.putString("key1", "Audio File");
            myIntent1.putExtras(uploadfile);
            startActivityForResult(myIntent1, 0);
        }
        else
        {
            Toast.makeText(ViewMedia.this, "FileFormat not Supported",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void requestResponse(String string)
    {
        // TODO Auto-generated method stub
        HttpClient httpclient = new DefaultHttpClient();
//        NameValuePair[] data = {new NameValuePair("action", "media_view"), new NameValuePair("userid", "499131"), new NameValuePair("matchid", "1234"), new NameValuePair("category", "Dating"), new NameValuePair("media_type", "Image")};
        Part[] parts = {new StringPart("action", "media_view"),
                new StringPart("userid", userId),
                new StringPart("matchid", userId),
                new StringPart("category", "Dating"),
                new StringPart("media_type", string)};


        HttpPost httpPost = null;
        httpPost = new HttpPost(getString(R.string.MediaServer));
        httpPost.setEntity(new MultipartEntity(parts, httpPost.getParams()));

        try
        {
            HttpResponse resp = httpclient.execute(httpPost);
            String response = HttpUtils.getResponseString(resp);
            Log.i("response/////////////////..................................",
                    response);
            res = response.split(",");
            //Log.i("res[0]/////////////////", res[1]);

            fill1(res);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void upOneLevel()
    {
        // TODO Auto-generated method stub
        fill(folders);
    }

    private boolean checkEndsWithInStringArray(String checkItsEnd,
            String[] fileEndings)
    {
        for (String aEnd : fileEndings)
        {
            if (checkItsEnd.endsWith(aEnd))
            {
                return true;
            }
        }
        return false;
    }

}
