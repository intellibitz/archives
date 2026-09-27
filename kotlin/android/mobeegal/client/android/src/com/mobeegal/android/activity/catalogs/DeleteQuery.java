package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: DeleteQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
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

import java.util.ArrayList;

public class DeleteQuery
        extends ListActivity
        implements OnItemClickListener
{

    SQLiteDatabase myDatabase = null;
    int size = 20;
    int[] datingId = new int[size];
    String[] iStuffarea = new String[size];
    String[] iStuffage = new String[size];
    String[] iStuffsex = new String[size];
    String[] iStuffheight = new String[size];
    String[] iStuffweight = new String[size];
    String[] iStuffcity = new String[size];
    String[] iStuffcountry = new String[size];

    String[] uStuffarea = new String[size];
    String[] uStuffage = new String[size];
    String[] uStuffsex = new String[size];
    String[] uStuffheight = new String[size];
    String[] uStuffweight = new String[size];
    String[] uStuffcity = new String[size];
    String[] uStuffcountry = new String[size];
    String[] queryStatus = new String[size];
    int count = 0;
    int rows = 0;
    Cursor c;
    ArrayList<String> results = new ArrayList<String>();

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setListAdapter(new SpeechListAdapter(this));
        getListView().setOnItemClickListener(this);
    }

    private class SpeechListAdapter
            extends BaseAdapter
    {

        public SpeechListAdapter(Context context)
        {
            mContext = context;

            myDatabase = mContext.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String myCols[] = {"key", "iage", "isex", "iheight", "iweight",
                    "iarea", "icity", "icountry", "uage", "usex", "uheight",
                    "uweight", "uarea", "ucity", "ucountry", "queryStatus"};
            c = myDatabase
                    .query("Dating", myCols, null, null, null, null, null);
            rows = c.getCount();

            int idcolumn = c.getColumnIndexOrThrow("key");
            int ageColumn = c.getColumnIndexOrThrow("iage");
            int sexColumn = c.getColumnIndexOrThrow("isex");
            int heightColumn = c.getColumnIndexOrThrow("iheight");
            int weightColumn = c.getColumnIndexOrThrow("iweight");
            int areaColumn = c.getColumnIndexOrThrow("iarea");
            int cityColumn = c.getColumnIndexOrThrow("icity");
            int countryColumn = c.getColumnIndexOrThrow("icountry");

            int uagecolumn = c.getColumnIndexOrThrow("uage");
            int usexcolumn = c.getColumnIndexOrThrow("usex");
            int uheightcolumn = c.getColumnIndexOrThrow("uheight");
            int uweightcolumn = c.getColumnIndexOrThrow("uweight");
            int uareacolumn = c.getColumnIndexOrThrow("uarea");
            int ucitycolumn = c.getColumnIndexOrThrow("ucity");
            int ucountrycolumn = c.getColumnIndexOrThrow("ucountry");
            int querystatuscolumn = c.getColumnIndexOrThrow("queryStatus");

            if (c != null)
            {
                count = 0;
                if (c.isFirst())
                {
                    do
                    {
                        int getid = c.getInt(idcolumn);
                        String getiage = c.getString(ageColumn);
                        String getisex = c.getString(sexColumn);
                        String getiheight = c.getString(heightColumn);
                        String getiweight = c.getString(weightColumn);
                        String getiarea = c.getString(areaColumn);
                        String geticity = c.getString(cityColumn);
                        String geticountry = c.getString(countryColumn);
                        String getuage = c.getString(uagecolumn);
                        String getusex = c.getString(usexcolumn);
                        String getuheight = c.getString(uheightcolumn);
                        String getuweight = c.getString(uweightcolumn);
                        String getuarea = c.getString(uareacolumn);
                        String getucity = c.getString(ucitycolumn);
                        String getucountry = c.getString(ucountrycolumn);
                        String getquerystatus = c.getString(querystatuscolumn);

                        datingId[count] = getid;
                        iStuffarea[count] = getiarea;
                        iStuffage[count] = getiage;
                        iStuffsex[count] = getisex;
                        iStuffheight[count] = getiheight;
                        iStuffweight[count] = getiweight;
                        iStuffcountry[count] = geticountry;
                        iStuffcity[count] = geticity;
                        uStuffage[count] = getuage;
                        uStuffsex[count] = getusex;
                        uStuffheight[count] = getuheight;
                        uStuffweight[count] = getuweight;
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
                sv = new SpeechView(mContext, " Age = " + iStuffage[position] +
                        "Sex = " + iStuffsex[position] + ", Height = " +
                        iStuffheight[position] + " Weight = " +
                        iStuffweight[position] + " Area = " +
                        iStuffarea[position] + " City = " +
                        iStuffcity[position] + " Country = " +
                        iStuffcountry[position],
                        " Age Range = " + uStuffage[position] + "Sex = " +
                                uStuffsex[position] + ", Height Range = " +
                                uStuffheight[position] + " Weight Range= " +
                                uStuffweight[position] + " Area = " +
                                uStuffarea[position] + " City = " +
                                uStuffcity[position] + " Country = " +
                                uStuffcountry[position] + " QueryStatus = " +
                                queryStatus[position]);
            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iStuffage[position]);
                sv.setDialogue(uStuffarea[position]);
            }
            return sv;
        }

        private Context mContext;
    }

    private class SpeechView
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
                    myDatabase =
                            DeleteQuery.this.openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    myDatabase.delete("Dating", "key=" + datingId[selectedId],
                            null);
                    if (selectedId == 0 && datingId[0] != 0)
                    {
                        myDatabase.delete("Dating", "key=" + datingId[0], null);
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(DeleteQuery.this, "Error", Toast.LENGTH_LONG)
                            .show();
                }
                Intent intent = new Intent(DeleteQuery.this, DeleteQuery.class);
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
                Intent stuffCheckintent = new Intent(DeleteQuery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(DeleteQuery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings = new Intent(DeleteQuery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
