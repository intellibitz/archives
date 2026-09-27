package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: MoviesViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss            $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.app.ListActivity;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;
import java.util.List;

public class MoviesViewQuery
        extends ListActivity
{

    private CheckBoxifiedTextListAdapter itla;
    CheckBoxifiedText checkBoxifiedTextobj[];
    int count;
    //int j;
    String text = "";
    SQLiteDatabase myDatabase = null;
    int size = 20;
    int rows = 0;
    Cursor c;
    private boolean[] bArray;
    boolean checkboxStatus;
    //CheckBoxifiedText checkBoxifiedTextobj[];
    String[] iStuffMovieType = new String[size];
    String[] iStuffMovieLanguage = new String[size];
    String[] iStuffSeatingStyle = new String[size];
    String[] iStuffcountry = new String[size];
    String[] iStuffcity = new String[size];
    String[] iStuffarea = new String[size];
    String[] uStuffMovieType = new String[size];
    String[] uStuffMovieLanguage = new String[size];
    String[] uStuffSeatingStyle = new String[size];
    String[] uStuffcountry = new String[size];
    String[] uStuffcity = new String[size];
    String[] uStuffarea = new String[size];
    int[] MovieId = new int[size];
    String[] queryStatus;

    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        myDatabase = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        //String myCols[] = {"key","imovietype", "imovielanguage","iseatingstyle", "icountry", "icity","iarea", "umovietype", "umovielanguage","useatingstyle", "ucountry", "ucity", "uarea", "queryStatus"};
        c = myDatabase.query("Movies", null, null, null, null, null, null);
        rows = c.getCount();
        if (rows == 0)
        {
            Toast.makeText(MoviesViewQuery.this, R.string.noviewquery,
                    Toast.LENGTH_LONG).show();
        }
        bArray = new boolean[rows];
        queryStatus = new String[rows];

        int idcolumn = c.getColumnIndexOrThrow("key");
        int imovietypeColumn = c.getColumnIndexOrThrow("imovietype");
        int imovielanguageColumn = c.getColumnIndexOrThrow("imovielanguage");
        int imiscColumn = c.getColumnIndexOrThrow("iseatingstyle");
        int icountryColumn = c.getColumnIndexOrThrow("icountry");
        int icityColumn = c.getColumnIndexOrThrow("icity");
        int iareaColumn = c.getColumnIndexOrThrow("iarea");

        int umovietypeColumn = c.getColumnIndexOrThrow("umovietype");
        int umovielanguageColumn = c.getColumnIndexOrThrow("umovielanguage");
        int umiscColumn = c.getColumnIndexOrThrow("useatingstyle");
        int ucountryColumn = c.getColumnIndexOrThrow("ucountry");
        int ucityColumn = c.getColumnIndexOrThrow("ucity");
        int uareaColumn = c.getColumnIndexOrThrow("uarea");
        int querystatuscolumn = c.getColumnIndexOrThrow("querystatus");

        if (c != null)
        {
            count = 0;
            if (c.isFirst())
            {
                do
                {

                    int getid = c.getInt(idcolumn);
                    String getimovietype = c.getString(imovietypeColumn);
                    String getimovielanguage =
                            c.getString(imovielanguageColumn);
                    String getimisc = c.getString(imiscColumn);
                    String geticountry = c.getString(icountryColumn);
                    String geticity = c.getString(icityColumn);
                    String getiarea = c.getString(iareaColumn);

                    String getumovietype = c.getString(umovietypeColumn);
                    String getumovielanguage =
                            c.getString(umovielanguageColumn);
                    String getumisc = c.getString(umiscColumn);
                    String getucountry = c.getString(ucountryColumn);
                    String getucity = c.getString(ucityColumn);
                    String getuarea = c.getString(uareaColumn);
                    String getquerystatus = c.getString(querystatuscolumn);
                    MovieId[count] = getid;
                    iStuffMovieType[count] = getimovietype;
                    iStuffMovieLanguage[count] = getimovielanguage;
                    iStuffSeatingStyle[count] = getimisc;
                    iStuffarea[count] = getiarea;

                    iStuffcountry[count] = geticountry;
                    iStuffcity[count] = geticity;

                    uStuffMovieType[count] = getumovietype;
                    uStuffMovieLanguage[count] = getumovielanguage;
                    uStuffSeatingStyle[count] = getumisc;
                    uStuffarea[count] = getuarea;
                    uStuffcity[count] = getucity;
                    uStuffcountry[count] = getucountry;
                    queryStatus[count] = getquerystatus;

                    count++;
                }
                while (c.moveToNext());
            }
        }

        for (int i = 0; i < rows; i++)
        {
            if (queryStatus[i].equals("true"))
            {
                bArray[i] = true;
            }
            else
            {
                bArray[i] = false;
            }
        }

        checkBoxifiedTextobj = new CheckBoxifiedText[rows];
        itla = new CheckBoxifiedTextListAdapter(this);
        for (int j = 0; j < rows; j++)
        {
            if (queryStatus[j].equals("true"))
            {
                checkboxStatus = true;
            }
            else if (queryStatus[j].equals("false"))
            {
                checkboxStatus = false;
            }
            checkBoxifiedTextobj[j] = new CheckBoxifiedText(
                    "Owner Detail: MovieType=" + iStuffMovieType[j] +
                            ", MovieLanguage=" + iStuffMovieLanguage[j] +
                            ", SeatingStyle=" + iStuffSeatingStyle[j] +
                            ", Area=" + iStuffarea[j] + ", City=" +
                            iStuffcity[j] + ", Country=" + iStuffcountry[j] +
                            ".\nPublic Detail: MovieType=" +
                            uStuffMovieType[j] + ", MovieLanguage=" +
                            uStuffMovieLanguage[j] + ", SeatingStyle=" +
                            uStuffSeatingStyle[j] + ", Area=" + uStuffarea[j] +
                            ", City=" + uStuffcity[j] + ", Country=" +
                            uStuffcountry[j], checkboxStatus);
            itla.addItem(checkBoxifiedTextobj[j]);
        }
        setListAdapter(itla);
    }

    public class CheckBoxifiedText
    {

        private String mText = "";
        private CheckBox mCheckBox;
        private boolean mChecked;

        public CheckBoxifiedText(String text, boolean checked)
        {
            /* constructor */
            mText = text;
            mChecked = checked;
        }

        public void setChecked(boolean value)
        {
            this.mChecked = value;
        }

        public boolean getChecked()
        {
            return this.mChecked;
        }

        public String getText()
        {
            return mText;
        }

        public CheckBox getCheckBox()
        {
            return mCheckBox;
        }
    }

    public class CheckBoxifiedTextListAdapter
            extends BaseAdapter
    {

        private Context mContext;
        List<CheckBoxifiedText> mItems = new ArrayList<CheckBoxifiedText>();
        CheckBoxifiedText item;

        public CheckBoxifiedTextListAdapter(Context context)
        {
            mContext = context;
        }

        public void addItem(CheckBoxifiedText it)
        {
            mItems.add(it);
        }

        public void setListItems(List<CheckBoxifiedText> lit)
        {
            mItems = lit;
        }

        public List<CheckBoxifiedText> getListItem()
        {
            return mItems;
        }

        public int getCount()
        {
            return mItems.size();
        }

        public Object getItem(int position)
        {
            return mItems.get(position);
        }

        public boolean areAllItemsSelectable()
        {
            return false;
        }

        public boolean[] getStatus()
        {
            boolean[] bArray = new boolean[mItems.size()];
            int increment = 0;
            for (CheckBoxifiedText cboxtxt : mItems)
            {
                bArray[increment] = cboxtxt.getChecked();
            }
            return bArray;
        }

        public void deSelectAll()
        {
            for (CheckBoxifiedText cboxtxt : mItems)
            {
                cboxtxt.setChecked(false);
            }
            this.notifyDataSetInvalidated();
        }

        public void selectAll()
        {
            for (CheckBoxifiedText cboxtxt : mItems)
            {
                cboxtxt.setChecked(true);
            }
            this.notifyDataSetInvalidated();
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            CheckBoxifiedTextView btv;
            CheckBoxifiedText ctv;

            if (convertView == null)
            {
                btv = new CheckBoxifiedTextView(mContext, mItems.get(position),
                        position);
                CheckBoxifiedText src = mItems.get(position);
            }
            else
            {
                CheckBoxifiedText src = mItems.get(position);
                btv = (CheckBoxifiedTextView) convertView;
                btv.setText(src.getText());
                btv.setCheckBoxState(src.getChecked());
            }
            return btv;
        }
    }

    public class CheckBoxifiedTextView
            extends LinearLayout
    {

        private TextView mText;
        private CheckBox mCheckBox;
        private CheckBoxifiedText mCheckBoxText;
        int increment = 0;

        public CheckBoxifiedTextView(final Context context,
                CheckBoxifiedText aCheckBoxifiedText, final int position)
        {
            super(context);
            this.setOrientation(HORIZONTAL);
            mCheckBoxText = aCheckBoxifiedText;
            mCheckBox = new CheckBox(context);
            mCheckBox.setPadding(0, 0, 20, 0);  // 5px to the right
            mCheckBox.setChecked(aCheckBoxifiedText.getChecked());
            mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
            {

                public void onCheckedChanged(CompoundButton arg0, boolean arg1)
                {
                    try
                    {
                        bArray[position] = mCheckBox.isChecked();
                    }
                    catch (Exception e)
                    {
                        bArray[position] = mCheckBox.isChecked();
                    }
                }
            });

            addView(mCheckBox, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            mText = new TextView(context);
            mText.setText(aCheckBoxifiedText.getText());
            addView(mText, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        public void setText(String words)
        {
            mText.setText(words);
        }

        public void setCheckBoxState(boolean bool)
        {
            mCheckBox.setChecked(mCheckBoxText.getChecked());
            mCheckBoxText.setChecked(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsViewQueryMenu(menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {
        switch (item.getItemId())
        {
            case 1:
                Intent mapViewintent =
                        new Intent(MoviesViewQuery.this, MapResults.class);
                startActivity(mapViewintent);
                break;
            case 2:
                Intent catalogintent =
                        new Intent(MoviesViewQuery.this, FindandInstall.class);
                startActivity(catalogintent);

                break;
            case 3:
                Intent settingsintent =
                        new Intent(MoviesViewQuery.this, Settings.class);
                startActivity(settingsintent);
                break;
            case 4:
                Intent moviesintent =
                        new Intent(MoviesViewQuery.this, Movies.class);
                startActivity(moviesintent);
                break;

            case 5:
                if (rows == 0)
                {
                    Toast.makeText(MoviesViewQuery.this, R.string.donequery,
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (bArray[j] == true)
                        {
                            myDatabase.execSQL(
                                    "update Movies set queryStatus='" + "true" +
                                            "' where key=" + MovieId[j] + ";");
                            myDatabase.execSQL(
                                    "update category set querystatus='" +
                                            "true" + "' where categoryname='" +
                                            "Movies" + "';");
                        }
                        else if (bArray[j] == false)
                        {
                            myDatabase.execSQL(
                                    "update Movies set queryStatus='" +
                                            "false" + "' where key=" +
                                            MovieId[j] + ";");
                        }

                    }

                    Toast.makeText(MoviesViewQuery.this,
                            this.getString(R.string.ShowMessage),
                            Toast.LENGTH_LONG).show();
//                Intent updateintent = new Intent(MoviesViewQuery.this, Movies.class);
//                startActivity(updateintent);
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}

