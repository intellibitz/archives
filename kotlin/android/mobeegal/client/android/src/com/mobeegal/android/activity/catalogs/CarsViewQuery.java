package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: CarsViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss              $: Id of last commit
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

public class CarsViewQuery
        extends ListActivity
{

    SQLiteDatabase myDatabase = null;
    int size = 20;
    Cursor c;
    int rows = 0;
    private boolean[] bArray;
    int count;
    boolean checkboxStatus;
    CheckBoxifiedText checkBoxifiedTextobj[];
    private CheckBoxifiedTextListAdapter itla;
    int[] carsId = new int[size];
    String[] imake = new String[size];
    String[] imodel = new String[size];
    String[] iyear = new String[size];
    String[] icolor = new String[size];
    String[] ifueltype = new String[size];
    String[] iprice = new String[size];
    String[] icountry = new String[size];
    String[] icity = new String[size];
    String[] iarea = new String[size];

    String[] umake = new String[size];
    String[] umodel = new String[size];
    String[] uyear = new String[size];
    String[] ucolor = new String[size];
    String[] ufueltype = new String[size];
    String[] uprice = new String[size];

    String[] ucountry = new String[size];
    String[] ucity = new String[size];
    String[] uarea = new String[size];
    String[] queryStatus = new String[size];

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        myDatabase = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        String myCols[] = {"key", "imake", "imodel", "iyear", "icolor",
                "ifuel_type", "iprice", "icountry", "icity", "iarea", "umake",
                "umodel", "uyear", "ucolor", "ufuel_type", "uprice",
                " ucountry", "ucity", "uarea", "queryStatus"};
        c = myDatabase.query("Cars", myCols, null, null, null, null, null);
        rows = c.getCount();
        if (rows == 0)
        {
            Toast.makeText(CarsViewQuery.this, R.string.noviewquery,
                    Toast.LENGTH_LONG).show();
        }
        bArray = new boolean[rows];
        queryStatus = new String[rows];


        int idcolumn = c.getColumnIndexOrThrow("key");
        int imakeColumn = c.getColumnIndexOrThrow("imake");
        int imodelColumn = c.getColumnIndexOrThrow("imodel");
        int iyearColumn = c.getColumnIndexOrThrow("iyear");
        int icolorColumn = c.getColumnIndexOrThrow("icolor");
        int ifueltypeColumn = c.getColumnIndexOrThrow("ifuel_type");
        int ipriceColumn = c.getColumnIndexOrThrow("iprice");
        int icountryColumn = c.getColumnIndexOrThrow("icountry");
        int icityColumn = c.getColumnIndexOrThrow("icity");
        int iareaColumn = c.getColumnIndexOrThrow("iarea");


        int umakeColumn = c.getColumnIndexOrThrow("umake");
        int umodelColumn = c.getColumnIndexOrThrow("umodel");
        int uyearColumn = c.getColumnIndexOrThrow("uyear");
        int ucolorColumn = c.getColumnIndexOrThrow("ucolor");
        int ufueltypeColumn = c.getColumnIndexOrThrow("ufuel_type");
        int upriceColumn = c.getColumnIndexOrThrow("uprice");
        int ucountryColumn = c.getColumnIndexOrThrow("ucountry");
        int ucityColumn = c.getColumnIndexOrThrow("ucity");
        int uareaColumn = c.getColumnIndexOrThrow("uarea");
        int uquerystatuscolumn = c.getColumnIndexOrThrow("queryStatus");

        if (c != null)
        {
            count = 0;
            if (c.isFirst())
            {
                do
                {
                    int getid = c.getInt(idcolumn);
                    String getimake = c.getString(imakeColumn);
                    String getimodel = c.getString(imodelColumn);
                    String getiyear = c.getString(iyearColumn);
                    String geticolor = c.getString(icolorColumn);
                    String getifueltype = c.getString(ifueltypeColumn);
                    String getiprice = c.getString(ipriceColumn);
                    String geticountry = c.getString(icountryColumn);
                    String geticity = c.getString(icityColumn);
                    String getiarea = c.getString(iareaColumn);


                    String getumake = c.getString(umakeColumn);
                    String getumodel = c.getString(umodelColumn);
                    String getuyear = c.getString(uyearColumn);
                    String getucolor = c.getString(ucolorColumn);
                    String getufueltype = c.getString(ufueltypeColumn);
                    String getuprice = c.getString(upriceColumn);
                    String getucountry = c.getString(ucountryColumn);
                    String getucity = c.getString(ucityColumn);
                    String getuarea = c.getString(uareaColumn);
                    String getuquerystatus = c.getString(uquerystatuscolumn);


                    carsId[count] = getid;
                    imake[count] = getimake;
                    imodel[count] = getimodel;
                    iyear[count] = getiyear;
                    icolor[count] = geticolor;
                    ifueltype[count] = getifueltype;
                    iprice[count] = getiprice;
                    icountry[count] = geticountry;
                    icity[count] = geticity;
                    iarea[count] = getiarea;

                    umake[count] = getumake;
                    umodel[count] = getumodel;
                    uyear[count] = getuyear;
                    ucolor[count] = getucolor;
                    ufueltype[count] = getufueltype;
                    uprice[count] = getuprice;
                    ucountry[count] = getucountry;
                    ucity[count] = getucity;
                    uarea[count] = getuarea;
                    queryStatus[count] = getuquerystatus;

                    count++;
                }
                while (c.moveToNext());
            }
        }
//            if (c1 != null) {
//                count = 0;
//                if (c1.isFirst()) {
//                    do {
//
//
//                        //results.add("IStuff :" + iStuffarea[count] + "UStuff :" + uStuffarea[count]);
//                        count++;
//                    } while (c1.moveToNext());
//                }
//            }
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
                    "Seller Details : make  = " + imake[j] + ", model = " +
                            imodel[j] + ", year = " + iyear[j] + ", color = " +
                            icolor[j] + ", fueltype = " + ifueltype[j] +
                            ", price = " + iprice[j] + ", area = " + iarea[j] +
                            ", city = " + icity[j] + ", country = " +
                            icountry[j] + ". \nBuyer Details : make = " +
                            umake[j] + ", model = " + umodel[j] + ", year = " +
                            uyear[j] + ", color = " + ucolor[j] +
                            ", fueltype = " + ufueltype[j] + ", price = " +
                            uprice[j] + ", area = " + uarea[j] + ", city = " +
                            ucity[j] + ", country = " + ucountry[j],
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
                        new Intent(CarsViewQuery.this, MapResults.class);
                startActivity(mapViewintent);
                break;
            case 2:
                Intent catalogintent =
                        new Intent(CarsViewQuery.this, FindandInstall.class);
                startActivity(catalogintent);

                break;
            case 3:
                Intent settingsintent =
                        new Intent(CarsViewQuery.this, Settings.class);
                startActivity(settingsintent);
                break;
            case 4:
                Intent carintent = new Intent(CarsViewQuery.this, Cars.class);
                startActivity(carintent);
                break;
            case 5:
                if (rows == 0)
                {
                    Toast.makeText(CarsViewQuery.this, R.string.donequery,
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (bArray[j] == true)
                        {
                            myDatabase.execSQL("update Cars set queryStatus='" +
                                    "true" + "' where key=" + carsId[j] + ";");
                            myDatabase.execSQL(
                                    "update category set querystatus='" +
                                            "true" + "' where categoryname='" +
                                            "Cars" + "';");
                        }
                        else if (bArray[j] == false)
                        {
                            myDatabase.execSQL("update Cars set queryStatus='" +
                                    "false" + "' where key=" + carsId[j] + ";");
                        }
                        Toast.makeText(CarsViewQuery.this,
                                "Boolean = " + Boolean.toString(bArray[j]),
                                Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(CarsViewQuery.this,
                            this.getString(R.string.ShowMessage),
                            Toast.LENGTH_LONG).show();
//                Intent updateintent = new Intent(CarsViewQuery.this, Cars.class);
//                startActivity(updateintent);
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }


}

