package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: DeleteMoviesQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss          $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.util.ViewMenu;

public class DeleteMoviesQuery
        extends ListActivity
        implements OnItemClickListener
{
    SQLiteDatabase myDatabase = null;
    Cursor c;
    int rows = 0;
    int count = 0;
    int size = 15;
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
    String[] queryStatus = new String[size];
    int[] MovieId = new int[size];

    //String selectid;
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setListAdapter(new SpeechListAdapter(this));
        getListView().setOnItemClickListener(this);
    }

    public class SpeechListAdapter
            extends BaseAdapter
    {

        public SpeechListAdapter(Context context)
        {

            mContext = context;
            myDatabase = mContext.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String myCols[] = {"key", "imovietype", "imovielanguage",
                    "iseatingstyle", "icountry", "icity", "iarea", "umovietype",
                    "umovielanguage", "useatingstyle", "ucountry", "ucity",
                    "uarea", "queryStatus"};
            c = myDatabase
                    .query("Movies", myCols, null, null, null, null, null);
            rows = c.getCount();

            int idcolumn = c.getColumnIndexOrThrow("key");
            int imovietypeColumn = c.getColumnIndexOrThrow("imovietype");
            int imovielanguageColumn =
                    c.getColumnIndexOrThrow("imovielanguage");
            int imiscColumn = c.getColumnIndexOrThrow("iseatingstyle");
            int icountryColumn = c.getColumnIndexOrThrow("icountry");
            int icityColumn = c.getColumnIndexOrThrow("icity");
            int iareaColumn = c.getColumnIndexOrThrow("iarea");

            int umovietypeColumn = c.getColumnIndexOrThrow("umovietype");
            int umovielanguageColumn =
                    c.getColumnIndexOrThrow("umovielanguage");
            int umiscColumn = c.getColumnIndexOrThrow("useatingstyle");
            int ucountryColumn = c.getColumnIndexOrThrow("ucountry");
            int ucityColumn = c.getColumnIndexOrThrow("ucity");
            int uareaColumn = c.getColumnIndexOrThrow("uarea");
            int querystatuscolumn = c.getColumnIndexOrThrow("querystatus");


            if (c != null)
            {
                // count = 0;
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

        }

        public int getCount()
        {
            return rows;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            SpeechView sv;
            if (convertView == null)
            {
                sv = new SpeechView(mContext, " MovieType = " +
                        iStuffMovieType[position] + "MovieLanguage = " +
                        iStuffMovieLanguage[position] + " Misc = " +
                        iStuffSeatingStyle[position] + " Country = " +
                        iStuffcountry[position] + " Area = " +
                        iStuffarea[position] + " City = " +
                        iStuffcity[position],
                        " uMovieType = " + uStuffMovieType[position] +
                                "uMovieLanguage = " +
                                uStuffMovieLanguage[position] + " uMisc = " +
                                uStuffSeatingStyle[position] + " uCountry= " +
                                uStuffcountry[position] + " Area = " +
                                uStuffarea[position] + " City = " +
                                uStuffcity[position] + " QueryStatus = " +
                                queryStatus[position]);
            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iStuffMovieType[position]);
                sv.setDialogue(uStuffarea[position]);
            }
            return sv;
        }

        private Context mContext;

        public void onItemClick(AdapterView arg0, View arg1, int arg2,
                long arg3)
        {

        }
    }

    public class SpeechView
            extends LinearLayout
    {

        public SpeechView(Context context, String title, String words)
        {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            mTitle.setText("IStuff : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("UStuff : " + words);
            addView(mDialogue, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        public void setTitle(String title)
        {
            mTitle.setText(title);
        }

        public void setDialogue(String words)
        {
            mDialogue.setText(words);
        }

        private TextView mTitle;
        private TextView mDialogue;
    }

    public void onItemClick(AdapterView parent, View v, int position, long id)
    {
        final String selectid = parent.getItemAtPosition(position).toString();
        final int selectedId = Integer.parseInt(selectid);
        if (rows > 0)
        {
            myDatabase.execSQL("update category set querystatus='" + "true" +
                    "' where status='" + "true" + "';");
        }
        else
        {
            myDatabase.execSQL("update category set querystatus='" + "false" +
                    "' where status='" + "true" + "';");
        }
        OnClickListener okButtonListener = new OnClickListener()
        {

            public void onClick(DialogInterface arg0, int arg1)
            {
                try
                {
                    myDatabase = DeleteMoviesQuery.this
                            .openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    myDatabase.delete("Movies", "key=" + MovieId[selectedId],
                            null);
                    if (selectedId == 0 && MovieId[0] != 0)
                    {
                        myDatabase.delete("Movies", "key=" + MovieId[0], null);

                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(DeleteMoviesQuery.this, "Error",
                            Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(DeleteMoviesQuery.this,
                        DeleteMoviesQuery.class);
                startActivity(intent);
                finish();

            }
        };
        OnClickListener cancelButtonListener = new OnClickListener()
        {
            // @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                // Do nothing
            }
        };
//        AlertDialog.show(this, "Delete", position, " Do you want to delete the query\n", "OK", okButtonListener, "cancel", cancelButtonListener, false, null);

    }

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
                Intent stuffCheckintent = new Intent(DeleteMoviesQuery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(DeleteMoviesQuery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(DeleteMoviesQuery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}


