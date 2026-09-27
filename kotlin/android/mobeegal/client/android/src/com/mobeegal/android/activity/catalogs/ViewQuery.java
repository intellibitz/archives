package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: ViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss                     $: Id of last commit
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
import com.mobeegal.android.R.string;
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;
import java.util.List;

public class ViewQuery
        extends ListActivity
{

    private CheckBoxifiedTextListAdapter itla;
    CheckBoxifiedText checkBoxifiedTextobj[];
    int count;
    int i;
    String text = "";
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
    String[] queryStatus;// = new String[size];
    int rows = 0;
    Cursor c;
    private boolean[] bArray;
    boolean checkboxStatus;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        myDatabase = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        String myCols[] = {"key", "iage", "isex", "iheight", "iweight", "iarea",
                "icity", "icountry", "uage", "usex", "uheight", "uweight",
                "uarea", "ucity", "ucountry", "queryStatus"};
        c = myDatabase.query("Dating", myCols, null, null, null, null, null);
        rows = c.getCount();
        if (rows == 0)
        {
            Toast.makeText(ViewQuery.this, string.noviewquery,
                    Toast.LENGTH_LONG).show();
        }
        bArray = new boolean[rows];
        queryStatus = new String[rows];

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
                    "User Profile: Age=" + iStuffage[j] + ", Sex=" +
                            iStuffsex[j] + ", Height=" + iStuffheight[j] +
                            ", Weight=" + iStuffweight[j] + ", Area=" +
                            iStuffarea[j] + ", City=" + iStuffcity[j] +
                            ", Country=" + iStuffcountry[j] +
                            ".\nParther Profile: Age=" + uStuffage[j] +
                            ", Sex=" + uStuffsex[j] + ", Height=" +
                            uStuffheight[j] + ", Weight=" + uStuffweight[j] +
                            ", Area=" + uStuffarea[j] + ", City=" +
                            uStuffcity[j] + ", Country=" + uStuffcountry[j],
                    checkboxStatus);
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
                        //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]), Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e)
                    {
                        //bArray[position] = mCheckBox.isChecked();
                        //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
                        //Toast.makeText(context, " position = "+Integer.toString(position)+" rows = "+Integer.toString(rows),Toast.LENGTH_SHORT).show();
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
                        new Intent(ViewQuery.this, MapResults.class);
                startActivity(mapViewintent);
                break;
            case 2:
                Intent catalogintent =
                        new Intent(ViewQuery.this, FindandInstall.class);
                startActivity(catalogintent);
                break;
            case 3:
                Intent settingsintent =
                        new Intent(ViewQuery.this, Settings.class);
                startActivity(settingsintent);
                break;
            case 4:
                Intent datingintent = new Intent(ViewQuery.this, Dating.class);
                startActivity(datingintent);
                break;
            case 5:
                if (rows == 0)
                {
                    Toast.makeText(ViewQuery.this, R.string.donequery,
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (bArray[j] == true)
                        {
                            myDatabase.execSQL(
                                    "update Dating set queryStatus='" + "true" +
                                            "' where key=" + datingId[j] + ";");
                            myDatabase.execSQL(
                                    "update category set querystatus='" +
                                            "true" + "' where categoryname='" +
                                            "Dating" + "';");
                        }
                        else if (bArray[j] == false)
                        {
                            myDatabase.execSQL(
                                    "update Dating set queryStatus='" +
                                            "false" + "' where key=" +
                                            datingId[j] + ";");
                        }
                        Toast.makeText(ViewQuery.this,
                                "Boolean = " + Boolean.toString(bArray[j]),
                                Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(ViewQuery.this,
                            this.getString(R.string.ShowMessage),
                            Toast.LENGTH_LONG).show();
                    //  finish();
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
