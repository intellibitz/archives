package com.mobeegal.android.activity;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.activity.catalogs.Cars;
import com.mobeegal.android.activity.catalogs.Dating;
import com.mobeegal.android.activity.catalogs.Home;
import com.mobeegal.android.activity.catalogs.Jewelry;
import com.mobeegal.android.activity.catalogs.Matrimony;
import com.mobeegal.android.activity.catalogs.Movies;
import com.mobeegal.android.activity.catalogs.Restaurants;
import com.mobeegal.android.model.IconifiedText;
import com.mobeegal.android.view.IconifiedTextListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class AndroidFileBrowser
        extends ListActivity
{

    private static Logger logger = Logger.getLogger("Testcatalogs");

    private enum DISPLAYMODE
    {

        ABSOLUTE, RELATIVE;
    }

    private int position;
    protected static final int SUB_ACTIVITY_REQUEST_CODE = 1337;
    public String str1, str2;
    private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
    private List<IconifiedText> directoryEntries =
            new ArrayList<IconifiedText>();
    private File currentDirectory = new File("/");
    private String ch;
    private int ch1;
    private Intent myIntent;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Black);
        browseToRoot();
        this.setSelection(0);

        Bundle bundle1 = this.getIntent().getExtras();
        if (bundle1 != null)
        {
            str1 = bundle1.getString("value1");
            ch = bundle1.getString("class");
            ch1 = Integer.parseInt(ch);

        }
        switch (ch1)
        {

            case 1:
                myIntent = new Intent(AndroidFileBrowser.this, Jewelry.class);
                break;
            case 2:
                myIntent = new Intent(AndroidFileBrowser.this, Cars.class);
                break;
            case 3:
                myIntent = new Intent(AndroidFileBrowser.this, Dating.class);
                break;
            case 4:
                myIntent = new Intent(AndroidFileBrowser.this, Matrimony.class);
                break;
            case 5:
                myIntent =
                        new Intent(AndroidFileBrowser.this, Restaurants.class);
                break;
            case 6:
                myIntent = new Intent(AndroidFileBrowser.this, Movies.class);
                break;
            case 7:
                myIntent = new Intent(AndroidFileBrowser.this, Home.class);
                break;
            default:
                break;

        }

    }

    private void browseToRoot()
    {
        browseTo(new File("/"));
    }

    private void upOneLevel()
    {
        if (this.currentDirectory.getParent() != null)
        {
            this.browseTo(this.currentDirectory.getParentFile());
        }
    }

    private void browseTo(final File aDirectory)
    {

        if (this.displayMode == DISPLAYMODE.RELATIVE)
        {
            this.setTitle(aDirectory.getAbsolutePath() + " :: ");
        }
        if (aDirectory.isDirectory())
        {
            this.currentDirectory = aDirectory;
            fill(aDirectory.listFiles());
        }
        else
        {
            OnClickListener okButtonListener = new OnClickListener()
            {

                public void onClick(DialogInterface arg0, int arg1)
                {
                    AndroidFileBrowser.this.openFile(aDirectory);
                }
            };
            OnClickListener viewButtonListener = new OnClickListener()
            {

                public void onClick(DialogInterface arg0, int arg1)
                {
                    AndroidFileBrowser.this.playFile(aDirectory);
//					Intent playIntent = new Intent(AndroidFileBrowser.this, PlayMedia.class);
//                                        startActivityForResult(playIntent, 0);
                }
            };

            OnClickListener cancelButtonListener = new OnClickListener()
            {
                // @Override
                public void onClick(DialogInterface arg0, int arg1)
                {
                    // Do nothing ^^
                }
            };
//            AlertDialog.show(this, "Upload", position, " Do you want to Upload ?\n", "Upload", okButtonListener, "cancel", cancelButtonListener, "view", viewButtonListener, false, null);

        }
    }

    private void openFile(File aFile)
    {
        String filename = aFile.getName();

        if (checkEndsWithInStringArray(filename,
                getResources().getStringArray(R.array.fileEndingImage)))
        {
            String uploadingFile = "file://" + aFile.getAbsolutePath();
            Bundle uploadfile = new Bundle();
            uploadfile.putString("key", uploadingFile);
            uploadfile.putString("key1", str1);
            logger.info("count = " + str1);
            myIntent.putExtras(uploadfile);
            startActivityForResult(myIntent, 0);
        }
        else if (checkEndsWithInStringArray(filename,
                getResources().getStringArray(R.array.fileEndingVideo)))
        {
            String uploadingFile = "file://" + aFile.getAbsolutePath();
            Bundle uploadfile = new Bundle();
            // Intent myIntent = new Intent(AndroidFileBrowser.this,Cars.class);
            uploadfile.putString("key", uploadingFile);
            uploadfile.putString("key1", str1);
            logger.info("count = " + str1);
            myIntent.putExtras(uploadfile);
            startActivityForResult(myIntent, 0);
        }
        else if (checkEndsWithInStringArray(filename,
                getResources().getStringArray(R.array.fileEndingAudio)))
        {
            String uploadingFile = "file://" + aFile.getAbsolutePath();
            Bundle uploadfile = new Bundle();
            // Intent myIntent = new Intent(AndroidFileBrowser.this,Cars.class);
            uploadfile.putString("key", uploadingFile);
            uploadfile.putString("key1", str1);
            logger.info("count = " + str1);
            myIntent.putExtras(uploadfile);
            startActivityForResult(myIntent, 0);
        }
        else
        {
            Toast.makeText(AndroidFileBrowser.this, "FileFormat not Supported",
                    Toast.LENGTH_SHORT).show();
        }


    }

    private void playFile(File aFile)
    {
        String filename = aFile.getName();
        if (checkEndsWithInStringArray(filename,
                getResources().getStringArray(R.array.fileEndingImage)))
        {
            String uploadingimage = "file://" + aFile.getAbsolutePath();
            Bundle uploadimage = new Bundle();
            Intent myIntent1 =
                    new Intent(AndroidFileBrowser.this, UploadGallery.class);
            uploadimage.putString("key", uploadingimage);
            uploadimage.putString("key1", str1);
            logger.info("count = " + str1);
            myIntent1.putExtras(uploadimage);
            startActivityForResult(myIntent1, 0);
        }
        else if (checkEndsWithInStringArray(filename,
                getResources().getStringArray(R.array.fileEndingVideo)))
        {
            String uploadingFile = "file://" + aFile.getAbsolutePath();
            Bundle uploadfile = new Bundle();
            Intent myIntent1 =
                    new Intent(AndroidFileBrowser.this, PlayMedia.class);
            uploadfile.putString("key", uploadingFile);
            uploadfile.putString("key1", "Video File");
            myIntent1.putExtras(uploadfile);
            startActivityForResult(myIntent1, 0);
        }
        else if (checkEndsWithInStringArray(filename,
                getResources().getStringArray(R.array.fileEndingAudio)))
        {
            String uploadingFile = "file://" + aFile.getAbsolutePath();
            Bundle uploadfile = new Bundle();
            Intent myIntent1 =
                    new Intent(AndroidFileBrowser.this, PlayMedia.class);
            uploadfile.putString("key", uploadingFile);
            uploadfile.putString("key1", "Audio File");
            myIntent1.putExtras(uploadfile);
            startActivityForResult(myIntent1, 0);
        }
        else
        {
            Toast.makeText(AndroidFileBrowser.this, "FileFormat not Supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void fill(File[] files)
    {
        this.directoryEntries.clear();

        // Add the "." == "current directory"
        this.directoryEntries.add(new IconifiedText(
                ".",
                getResources().getDrawable(R.drawable.folder)));
        // and the ".." == 'Up one level'
        if (this.currentDirectory.getParent() != null)
        {
            this.directoryEntries.add(new IconifiedText(
                    "..",
                    getResources().getDrawable(R.drawable.uponelevel)));
        }

        Drawable currentIcon = null;
        for (File currentFile : files)
        {
            if (currentFile.isDirectory())
            {
                currentIcon = getResources().getDrawable(R.drawable.folder);
            }
            else
            {
                String fileName = currentFile.getName();

                if (checkEndsWithInStringArray(fileName, getResources().
                        getStringArray(R.array.fileEndingImage)))
                {
                    currentIcon = getResources().getDrawable(R.drawable.image);
                }
                else if (checkEndsWithInStringArray(fileName, getResources().
                        getStringArray(R.array.fileEndingWebText)))
                {
                    currentIcon =
                            getResources().getDrawable(R.drawable.webtext);
                }
                else if (checkEndsWithInStringArray(fileName, getResources().
                        getStringArray(R.array.fileEndingPackage)))
                {
                    currentIcon = getResources().getDrawable(R.drawable.packed);
                }
                else if (checkEndsWithInStringArray(fileName, getResources().
                        getStringArray(R.array.fileEndingVideo)))
                {
                    currentIcon = getResources().getDrawable(R.drawable.video);
                }
                else if (checkEndsWithInStringArray(fileName, getResources().
                        getStringArray(R.array.fileEndingAudio)))
                {
                    currentIcon = getResources().getDrawable(R.drawable.audio);
                }
                else
                {
                    currentIcon = getResources().getDrawable(R.drawable.text);
                }
            }
            switch (this.displayMode)
            {
                case ABSOLUTE:
                    /* On absolute Mode, we show the full path */
                    this.directoryEntries.add(new IconifiedText(
                            currentFile.getPath(), currentIcon));
                    break;
                case RELATIVE:
                    /* On relative Mode, we have to cut the
                     * current-path at the beginning */
                    int currentPathStringLenght =
                            this.currentDirectory.getAbsolutePath().length();
                    this.directoryEntries.add(new IconifiedText(
                            currentFile.getAbsolutePath().
                                    substring(currentPathStringLenght),
                            currentIcon));

                    break;
            }
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
        if (selectedFileString.equals("."))
        {
            // Refresh
            this.browseTo(this.currentDirectory);
        }
        else if (selectedFileString.equals(".."))
        {
            this.upOneLevel();
        }
        else if (selectedFileString.equals("data"))
        {
            this.browseTo(new File("/data/misc/"));
        }
        else
        {
            File clickedFile = null;
            switch (this.displayMode)
            {
                case RELATIVE:
                    clickedFile = new File(this.currentDirectory
                            .getAbsolutePath() +
                            this.directoryEntries.get(position).getText());
                    break;
                case ABSOLUTE:
                    clickedFile = new File(
                            this.directoryEntries.get(position).getText());
                    break;
            }
            if (clickedFile != null)
            {
                this.browseTo(clickedFile);
            }
        }
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
    //added image view
}
