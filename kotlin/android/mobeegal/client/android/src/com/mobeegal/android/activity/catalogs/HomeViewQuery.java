package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: HomeViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss              $: Id of last commit
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

public class HomeViewQuery
        extends ListActivity
{

    private CheckBoxifiedTextListAdapter itla;
    CheckBoxifiedText checkBoxifiedTextobj[];
    int count;
    int i;
    String text = "";
    SQLiteDatabase myDatabase;
    int size = 20;
    String[] iStuffRentalType = new String[size];
    String[] iStuffMisc = new String[size];
    String[] iStuffRate = new String[size];
    String[] iStuffStatus = new String[size];
    String[] iStuffCountry = new String[size];
    String[] iStuffCity = new String[size];
    String[] iStuffArea = new String[size];
    String[] iStufflatitude = new String[size];
    String[] iStufflongitude = new String[size];
    String[] uStuffRentalType = new String[size];
    String[] uStuffMisc = new String[size];
    String[] uStuffRate = new String[size];
    String[] uStuffStatus = new String[size];
    String[] uStuffCountry = new String[size];
    String[] uStuffCity = new String[size];
    String[] uStuffArea = new String[size];
    String[] queryStatus;// = new String[size];
    int[] HomeId = new int[size];
    int rows = 0;
    Cursor c;
    private boolean[] bArray;
    boolean checkboxStatus;

    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        myDatabase = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        String myCols[] = {"key", "irental", "imisc", "irate", "istatus",
                "icountry", "icity", "iarea", "urental", "umisc", "urate",
                "ustatus", "ucountry", "ucity", "uarea", "queryStatus"};
        c = myDatabase.query("Home", myCols, null, null, null, null, null);
        rows = c.getCount();
        if (rows == 0)
        {
            Toast.makeText(HomeViewQuery.this, R.string.noviewquery,
                    Toast.LENGTH_LONG).show();
        }
        bArray = new boolean[rows];
        queryStatus = new String[rows];


        int idcolumn = c.getColumnIndexOrThrow("key");
        int iStuffRentalTypeColumn = c.getColumnIndexOrThrow("irental");
        int iStuffMiscColumn = c.getColumnIndexOrThrow("imisc");
        int iStuffRateColumn = c.getColumnIndexOrThrow("irate");
        int iStuffStatusColumn = c.getColumnIndexOrThrow("istatus");
        int iStuffCountryColumn = c.getColumnIndexOrThrow("icountry");
        int iStuffCityColumn = c.getColumnIndexOrThrow("icity");
        int iStuffAreaColumn = c.getColumnIndexOrThrow("iarea");


        int uStuffRentalTypeColumn = c.getColumnIndexOrThrow("urental");
        int uStuffMiscColumn = c.getColumnIndexOrThrow("umisc");
        int uStuffRateColumn = c.getColumnIndexOrThrow("urate");
        int uStuffStatusColumn = c.getColumnIndexOrThrow("ustatus");
        int uStuffCountryColumn = c.getColumnIndexOrThrow("ucountry");
        int uStuffCityColumn = c.getColumnIndexOrThrow("ucity");
        int uStuffAreaColumn = c.getColumnIndexOrThrow("uarea");

        int querystatuscolumn = c.getColumnIndexOrThrow("queryStatus");


        if (c != null)
        {
            count = 0;
            if (c.isFirst())
            {
                do
                {
                    int getid = c.getInt(idcolumn);
                    String getiStuffRentalType =
                            c.getString(iStuffRentalTypeColumn);
                    String getiStuffMisc = c.getString(iStuffMiscColumn);
                    String getiStuffRate = c.getString(iStuffRateColumn);
                    String getiStuffStatus = c.getString(iStuffStatusColumn);
                    String getiStuffCountry = c.getString(iStuffCountryColumn);
                    String getiStuffCity = c.getString(iStuffCityColumn);
                    String getiStuffArea = c.getString(iStuffAreaColumn);

                    String getuStuffRentalType =
                            c.getString(uStuffRentalTypeColumn);
                    String getuStuffMisc = c.getString(uStuffMiscColumn);
                    String getuStuffRate = c.getString(uStuffRateColumn);
                    String getuStuffStatus = c.getString(uStuffStatusColumn);
                    String getuStuffCountry = c.getString(uStuffCountryColumn);
                    String getuStuffCity = c.getString(uStuffCityColumn);
                    String getuStuffArea = c.getString(uStuffAreaColumn);

                    String getquerystatus = c.getString(querystatuscolumn);


                    HomeId[count] = getid;
                    iStuffRentalType[count] = getiStuffRentalType;
                    iStuffMisc[count] = getiStuffMisc;
                    iStuffRate[count] = getiStuffRate;
                    iStuffStatus[count] = getiStuffStatus;
                    iStuffCountry[count] = getiStuffCountry;
                    iStuffCity[count] = getiStuffCity;
                    iStuffArea[count] = getiStuffArea;

                    uStuffRentalType[count] = getuStuffRentalType;
                    uStuffMisc[count] = getuStuffMisc;
                    uStuffRate[count] = getuStuffRate;
                    uStuffStatus[count] = getuStuffStatus;
                    uStuffCountry[count] = getuStuffCountry;
                    uStuffCity[count] = getuStuffCity;
                    uStuffArea[count] = getuStuffArea;

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
                    "Owner Detail: RentalType=" + iStuffRentalType[j] +
                            ", Misc=" + iStuffMisc[j] + ", Rate=" +
                            iStuffRate[j] + ", Status=" + iStuffStatus[j] +
                            ", City=" + iStuffCity[j] + " Area=" +
                            iStuffArea[j] + ", Country = " + iStuffCountry[j] +
                            ".\nTenant Detail: RentalType=" +
                            uStuffRentalType[j] + ", Misc=" + uStuffMisc[j] +
                            ", Rate=" + uStuffRate[j] + ", Status=" +
                            uStuffStatus[j] + ", City=" + uStuffCity[j] +
                            " Area=" + uStuffArea[j] + ", Country=" +
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
    /*public class CheckBoxifiedTextListAdapter extends BaseAdapter {
    private Context mContext;
    List<CheckBoxifiedText> mItems = new ArrayList<CheckBoxifiedText>();
    CheckBoxifiedText item;
    public CheckBoxifiedTextListAdapter(Context context) {
    mContext = context;
    }
    public void addItem(CheckBoxifiedText it) {
    mItems.add(it);
    }
    public void setListItems(List<CheckBoxifiedText> lit) {
    mItems = lit;
    }
    public List<CheckBoxifiedText> getListItem() {
    return mItems;
    }
    public int getCount() {
    return mItems.size();
    }
    public Object getItem(int position) {
    return mItems.get(position);
    }
    @Override
    public boolean areAllItemsSelectable() {
    return false;
    }
    public boolean[] getStatus() {
    boolean[] bArray = new boolean[mItems.size()];
    int increment = 0;
    for (CheckBoxifiedText cboxtxt : mItems) {
    bArray[increment] = cboxtxt.getChecked();
    }
    return bArray;
    }
    public void deSelectAll() {
    for (CheckBoxifiedText cboxtxt : mItems) {
    cboxtxt.setChecked(false);
    }
    this.notifyDataSetInvalidated();
    }
    public void selectAll() {
    for (CheckBoxifiedText cboxtxt : mItems) {
    cboxtxt.setChecked(true);
    }
    this.notifyDataSetInvalidated();
    }
    public long getItemId(int position) {
    return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
    CheckBoxifiedTextView btv;
    CheckBoxifiedText ctv;
    if (convertView == null) {
    btv = new CheckBoxifiedTextView(mContext, mItems.get(position), position);
    CheckBoxifiedText src = mItems.get(position);
    } else {
    CheckBoxifiedText src = mItems.get(position);
    btv = (CheckBoxifiedTextView) convertView;
    btv.setText(src.getText());
    btv.setCheckBoxState(src.getChecked());
    }
    return btv;
    }
    }*/

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
    /*public class CheckBoxifiedTextView extends LinearLayout {
    private TextView mText;
    private CheckBox mCheckBox;
    private CheckBoxifiedText mCheckBoxText;
    int increment = 0;
    public CheckBoxifiedTextView(final Context context, CheckBoxifiedText aCheckBoxifiedText, final int position) {
    super(context);
    this.setOrientation(HORIZONTAL);
    mCheckBoxText = aCheckBoxifiedText;
    mCheckBox = new CheckBox(context);
    mCheckBox.setPadding(0, 0, 20, 0);  // 5px to the right
    mCheckBox.setChecked(aCheckBoxifiedText.getChecked());
    mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
    try {
    bArray[position] = mCheckBox.isChecked();
    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]), Toast.LENGTH_LONG).show();
    } catch (Exception e) {
    //bArray[position] = mCheckBox.isChecked();
    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
    //Toast.makeText(context, " position = "+Integer.toString(position)+" rows = "+Integer.toString(rows),Toast.LENGTH_SHORT).show();
    }
    }
    });
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
    public void setText(String words) {
    mText.setText(words);
    }
    public void setCheckBoxState(boolean bool) {
    mCheckBox.setChecked(mCheckBoxText.getChecked());
    mCheckBoxText.setChecked(true);
    }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsViewQueryMenu(menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case 1:
                Intent mapViewintent =
                        new Intent(HomeViewQuery.this, MapResults.class);
                startActivity(mapViewintent);
                break;
            case 2:
                Intent catalogintent =
                        new Intent(HomeViewQuery.this, FindandInstall.class);
                startActivity(catalogintent);

                break;
            case 3:
                Intent settingsintent =
                        new Intent(HomeViewQuery.this, Settings.class);
                startActivity(settingsintent);
                break;
            case 4:
                Intent homeintent = new Intent(HomeViewQuery.this, Home.class);
                startActivity(homeintent);
                break;
            case 5:
                if (rows == 0)
                {
                    Toast.makeText(HomeViewQuery.this, R.string.donequery,
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (bArray[j] == true)
                        {
                            myDatabase.execSQL("update Home set queryStatus='" +
                                    "true" + "' where key=" + HomeId[j] + ";");
                            myDatabase.execSQL(
                                    "update category set querystatus='" +
                                            "true" + "' where categoryname='" +
                                            "Rental" + "';");
                        }
                        else if (bArray[j] == false)
                        {
                            myDatabase.execSQL("update Home set queryStatus='" +
                                    "false" + "' where key=" + HomeId[j] + ";");
                        }
                        Toast.makeText(HomeViewQuery.this,
                                "Boolean = " + Boolean.toString(bArray[j]),
                                Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(HomeViewQuery.this,
                            this.getString(R.string.ShowMessage),
                            Toast.LENGTH_LONG).show();
//                Intent updateintent = new Intent(HomeViewQuery.this, Home.class);
//                startActivity(updateintent);
                    break;
                }
        }
        return super.onOptionsItemSelected(menuItem);
    }
}

