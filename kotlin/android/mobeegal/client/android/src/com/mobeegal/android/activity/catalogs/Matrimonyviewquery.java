package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Matrimonyviewquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss         $: Id of last commit
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

public class Matrimonyviewquery
        extends ListActivity
{

    private CheckBoxifiedTextListAdapter itla;
    CheckBoxifiedText checkBoxifiedTextobj[];
    int count;
    int i;
    String text = "";
    SQLiteDatabase myDatabase = null;
    int size = 20;
    int[] matrimonyId = new int[size];
    String[] ireligion = new String[size];
    String[] icaste = new String[size];
    String[] iage = new String[size];
    String[] isex = new String[size];
    String[] iheight = new String[size];
    String[] iweight = new String[size];
    String[] icolor = new String[size];
    String[] icountry = new String[size];
    String[] icity = new String[size];
    String[] iarea = new String[size];

    String[] ureligion = new String[size];
    String[] ucaste = new String[size];
    String[] uage = new String[size];
    String[] usex = new String[size];
    String[] uheight = new String[size];
    String[] uweight = new String[size];
    String[] ucolor = new String[size];
    String[] ucountry = new String[size];
    String[] ucity = new String[size];
    String[] uarea = new String[size];
    String[] queryStatus = new String[size];

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

        String myCols[] = {"key", "ireligion", "icaste", "iage", "isex",
                "iheight", "iweight", "icolor", "iarea", "icity ",
                "icountry ", "ureligion", "ucaste", "uage", "usex ",
                "uheight", "uweight", "ucolor", "uarea", "ucity",
                "ucountry", "queryStatus"};

        c = myDatabase.query("Matrimony", myCols, null, null, null,
                null, null);
        rows = c.getCount();
        if (rows == 0)
        {
            Toast.makeText(Matrimonyviewquery.this, R.string.noviewquery,
                    Toast.LENGTH_LONG).show();
        }
        bArray = new boolean[rows];
        queryStatus = new String[rows];

        int idcolumn = c.getColumnIndexOrThrow("key");
        int religionColumn = c.getColumnIndexOrThrow("ireligion");
        int casteColumn = c.getColumnIndexOrThrow("icaste");
        int ageColumn = c.getColumnIndexOrThrow("iage");
        int sexColumn = c.getColumnIndexOrThrow("isex");
        int heightColumn = c.getColumnIndexOrThrow("iheight");
        int weightColumn = c.getColumnIndexOrThrow("iweight");
        int colorColumn = c.getColumnIndexOrThrow("icolor");
        int countryColumn = c.getColumnIndexOrThrow("icountry");
        int cityColumn = c.getColumnIndexOrThrow("icity");
        int areaColumn = c.getColumnIndexOrThrow("iarea");

        int ureligioncolumn = c.getColumnIndexOrThrow("ureligion");
        int ucastecolumn = c.getColumnIndexOrThrow("ucaste");
        int uagecolumn = c.getColumnIndexOrThrow("uage");
        int usexcolumn = c.getColumnIndexOrThrow("usex");
        int uheightcolumn = c.getColumnIndexOrThrow("uheight");
        int uweightcolumn = c.getColumnIndexOrThrow("uweight");
        int ucolorcolumn = c.getColumnIndexOrThrow("ucolor");
        int ucountrycolumn = c.getColumnIndexOrThrow("ucountry");
        int ucitycolumn = c.getColumnIndexOrThrow("ucity");
        int uareacolumn = c.getColumnIndexOrThrow("uarea");
        int uquerystatuscolumn = c.getColumnIndexOrThrow("queryStatus");

        if (c != null)
        {
            count = 0;
            if (c.isFirst())
            {
                do
                {
                    int getid = c.getInt(idcolumn);
                    String getireligion = c.getString(religionColumn);
                    String geticaste = c.getString(casteColumn);
                    String getiage = c.getString(ageColumn);
                    String getisex = c.getString(sexColumn);
                    String getiheight = c.getString(heightColumn);
                    String getiweight = c.getString(weightColumn);
                    String geticolor = c.getString(colorColumn);
                    String geticountry = c.getString(countryColumn);
                    String geticity = c.getString(cityColumn);
                    String getiarea = c.getString(areaColumn);

                    String getureligion = c.getString(ureligioncolumn);
                    String getucaste = c.getString(ucastecolumn);
                    String getuage = c.getString(uagecolumn);
                    String getusex = c.getString(usexcolumn);
                    String getuheight = c.getString(uheightcolumn);
                    String getuweight = c.getString(uweightcolumn);
                    String getucolor = c.getString(ucolorcolumn);
                    String getucountry = c.getString(ucountrycolumn);
                    String getucity = c.getString(ucitycolumn);
                    String getuarea = c.getString(uareacolumn);
                    String getustatus = c.getString(uquerystatuscolumn);

                    matrimonyId[count] = getid;
                    ireligion[count] = getireligion;
                    icaste[count] = geticaste;
                    iage[count] = getiage;
                    isex[count] = getisex;
                    iheight[count] = getiheight;
                    iweight[count] = getiweight;
                    icolor[count] = geticolor;
                    icountry[count] = geticountry;
                    icity[count] = geticity;
                    iarea[count] = getiarea;

                    ureligion[count] = getureligion;
                    ucaste[count] = getucaste;
                    uage[count] = getuage;
                    usex[count] = getusex;
                    uheight[count] = getuheight;
                    uweight[count] = getuweight;
                    ucolor[count] = getucolor;
                    ucountry[count] = getucountry;
                    ucity[count] = getucity;
                    uarea[count] = getuarea;
                    queryStatus[count] = getustatus;
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
                    "User Profile: Religion=" + ireligion[j] + ", Caste=" +
                            icaste[j] + ", Age=" + iage[j] + ", Sex=" +
                            isex[j] + ", Height=" + iheight[j] + ", Weight=" +
                            iweight[j] + ", Color=" + icolor[j] + ", Area=" +
                            iarea[j] + ", City=" + icity[j] + ", Country=" +
                            icountry[j] + ".\nPartner Profile: Religion=" +
                            ureligion[j] + ", Caste=" + ucaste[j] + ", Age=" +
                            uage[j] + ", Sex=" + usex[j] + ", Height=" +
                            uheight[j] + ", Weight=" + uweight[j] + ", Color=" +
                            ucolor[j] + ", Area=" + uarea[j] + ", City=" +
                            ucity[j] + ", Country=" + ucountry[j],
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
                    bArray[position] = mCheckBox.isChecked();
                    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
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
                Intent mapViewintent = new Intent(Matrimonyviewquery.this,
                        MapResults.class);
                startActivity(mapViewintent);
                break;
            case 2:
                Intent catalogintent = new Intent(Matrimonyviewquery.this,
                        FindandInstall.class);
                startActivity(catalogintent);

                break;
            case 3:
                Intent settingsintent =
                        new Intent(Matrimonyviewquery.this, Settings.class);
                startActivity(settingsintent);
                break;
            case 4:
                Intent matrimonyintent =
                        new Intent(Matrimonyviewquery.this, Matrimony.class);
                startActivity(matrimonyintent);
                break;
            case 5:
                if (rows == 0)
                {
                    Toast.makeText(Matrimonyviewquery.this, R.string.donequery,
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (bArray[j] == true)
                        {
                            myDatabase.execSQL(
                                    "update Matrimony set queryStatus='" +
                                            "true" + "' where key=" +
                                            matrimonyId[j] + ";");
                            myDatabase.execSQL(
                                    "update category set querystatus='" +
                                            "true" + "' where categoryname='" +
                                            "Matrimony" + "';");
                        }
                        else if (bArray[j] == false)
                        {
                            myDatabase.execSQL(
                                    "update Matrimony set queryStatus='" +
                                            "false" + "' where key=" +
                                            matrimonyId[j] + ";");
                        }
                        Toast.makeText(Matrimonyviewquery.this,
                                "Boolean = " + Boolean.toString(bArray[j]),
                                Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(Matrimonyviewquery.this,
                            this.getString(R.string.ShowMessage),
                            Toast.LENGTH_LONG).show();
//                Intent updateintent = new Intent(Matrimonyviewquery.this, Matrimony.class);
//                startActivity(updateintent);
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
