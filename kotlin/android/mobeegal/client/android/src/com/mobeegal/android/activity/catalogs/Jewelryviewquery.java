package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: Jewelryviewquery.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
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

public class Jewelryviewquery
        extends ListActivity
{

    private CheckBoxifiedTextListAdapter itla;
    CheckBoxifiedText checkBoxifiedTextobj[];
    int count;
    int i;
    String text = "";
    SQLiteDatabase myDatabase = null;
    int size = 20;
    int[] jewelryId = new int[size];
    String[] iStuffitemtype = new String[size];
    String[] iStuffWeight = new String[size];
    String[] iStuffCountry = new String[size];
    String[] iStuffCity = new String[size];
    String[] iStuffArea = new String[size];

    String[] uStuffitemtype = new String[size];
    String[] uStuffWeightRange = new String[size];
    String[] uStuffCountry = new String[size];
    String[] uStuffCity = new String[size];
    String[] uStuffArea = new String[size];
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
        //String myCols[] = {"key", "iStuffitemtype", "iStuffWeight", "iStuffArea", "iStuffCity", "iStuffCountry", " uStuffitemtype", "uStuffWeightRange", " uStuffArea", " uStuffCity", " uStuffCountry","queryStatus"};
        c = myDatabase.query("Jewelry", null, null, null, null, null, null);
        rows = c.getCount();
        if (rows == 0)
        {
            Toast.makeText(Jewelryviewquery.this, R.string.noviewquery,
                    Toast.LENGTH_LONG).show();
        }
        bArray = new boolean[rows];
        queryStatus = new String[rows];

        int idcolumn = c.getColumnIndexOrThrow("key");
        int itemColumn = c.getColumnIndexOrThrow("ijewelry");
        int weightColumn = c.getColumnIndexOrThrow("iweight");
        int countryColumn = c.getColumnIndexOrThrow("icountry");
        int cityColumn = c.getColumnIndexOrThrow("icity");
        int areaColumn = c.getColumnIndexOrThrow("iarea");


        int uitemcolumn = c.getColumnIndexOrThrow("ujewelry");
        int uweightcolumn = c.getColumnIndexOrThrow("uweight");
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
                    String getitem = c.getString(itemColumn);
                    String getiweight = c.getString(weightColumn);
                    String geticountry = c.getString(countryColumn);
                    String geticity = c.getString(cityColumn);
                    String getiarea = c.getString(areaColumn);
                    ;
                    String getuitem = c.getString(uitemcolumn);
                    String getuweight = c.getString(uweightcolumn);
                    String getucountry = c.getString(ucountrycolumn);
                    String getucity = c.getString(ucitycolumn);
                    String getuarea = c.getString(uareacolumn);
                    String getquerystatus = c.getString(uquerystatuscolumn);


                    jewelryId[count] = getid;
                    iStuffitemtype[count] = getitem;
                    iStuffWeight[count] = getiweight;
                    iStuffCountry[count] = geticountry;
                    iStuffCity[count] = geticity;
                    iStuffArea[count] = getiarea;

                    uStuffitemtype[count] = getuitem;
                    uStuffWeightRange[count] = getuweight;
                    uStuffCountry[count] = getucountry;
                    uStuffCity[count] = getucity;
                    uStuffArea[count] = getuarea;
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
                    "Merchant Profile: Itemtype=" + iStuffitemtype[j] +
                            ", Weight=" + iStuffWeight[j] + ", Area=" +
                            iStuffArea[j] + ", City=" + iStuffCity[j] +
                            ", Country=" + iStuffCountry[j] +
                            ".\nCustomer Profile: Itemtype=" +
                            uStuffitemtype[j] + ", Weight=" +
                            uStuffWeightRange[j] + ", Area=" + uStuffArea[j] +
                            ", City=" + uStuffCity[j] + ", Country=" +
                            uStuffCountry[j], checkboxStatus);
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
                Intent mapViewintent =
                        new Intent(Jewelryviewquery.this, MapResults.class);
                startActivity(mapViewintent);
                break;
            case 2:
                Intent catalogintent =
                        new Intent(Jewelryviewquery.this, FindandInstall.class);
                startActivity(catalogintent);

                break;
            case 3:
                Intent settingsintent =
                        new Intent(Jewelryviewquery.this, Settings.class);
                startActivity(settingsintent);
                break;
            case 4:
                Intent jewelryintent =
                        new Intent(Jewelryviewquery.this, Jewelry.class);
                startActivity(jewelryintent);
                break;
            case 5:
                if (rows == 0)
                {
                    Toast.makeText(Jewelryviewquery.this, R.string.donequery,
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    for (int j = 0; j < rows; j++)
                    {

                        if (bArray[j] == true)
                        {
                            myDatabase.execSQL(
                                    "update Jewelry set queryStatus='" +
                                            "true" + "' where key=" +
                                            jewelryId[j] + ";");
                            myDatabase.execSQL(
                                    "update category set queryStatus='" +
                                            "true" + "' where categoryname='" +
                                            "Jewelry" + "';");
                        }
                        else if (bArray[j] == false)
                        {
                            myDatabase.execSQL(
                                    "update Jewelry set queryStatus='" +
                                            "false" + "' where key=" +
                                            jewelryId[j] + ";");
                        }

                    }
                    Toast.makeText(Jewelryviewquery.this,
                            this.getString(R.string.ShowMessage),
                            Toast.LENGTH_LONG).show();
//                Intent updateintent = new Intent(Jewelryviewquery.this, Jewelry.class);
//                startActivity(updateintent);
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
