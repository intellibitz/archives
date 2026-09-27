package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: RestaurantsViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss       $: Id of last commit
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

public class RestaurantsViewQuery
        extends ListActivity
{

    int size = 25;
    private String[] ulongitude1 = new String[size];
    private String[] ulatitude1 = new String[size];
    private String[] ucountry1 = new String[size];
    private String[] ucity1 = new String[size];
    private String[] uarea1 = new String[size];
    private String[] uStuffMiscellaneous1 = new String[size];
    private String[] uStuffOccasionOrSeason1 = new String[size];
    private String[] uStuffMainIngredient1 = new String[size];
    private String[] uStuffDishType1 = new String[size];
    private String[] uStuffDietetic1 = new String[size];
    private String[] uStuffCourseType1 = new String[size];
    private String[] uStuffCookingMethod1 = new String[size];
    private String[] uStuffCuisinetype1 = new String[size];
    private String[] ilongitude1 = new String[size];
    private String[] ilatitude1 = new String[size];
    private String[] icountry1 = new String[size];
    private String[] icity1 = new String[size];
    private String[] iarea1 = new String[size];
    private String[] iStuffMiscellaneous1 = new String[size];
    private String[] iStuffOccasionOrSeason1 = new String[size];
    private String[] iStuffMainIngredient1 = new String[size];
    private String[] iStuffDishType1 = new String[size];
    private String[] iStuffDietetic1 = new String[size];
    private String[] iStuffCourseType1 = new String[size];
    private String[] iStuffCookingMethod1 = new String[size];
    private String[] iStuffCuisinetype1 = new String[size];
    SQLiteDatabase myDatabase = null;
    Cursor c;
    int rows = 0;
    int count = 0;
    String text = "";
    boolean checkboxStatus;
    private boolean[] bArray;
    private CheckBoxifiedTextListAdapter itla;
    CheckBoxifiedText checkBoxifiedTextobj[];
    int[] RestaurantId = new int[size];
    String[] queryStatus = new String[size];

    //private Context mContext;
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        myDatabase = this.openOrCreateDatabase("Mobeegal",
                Context.MODE_PRIVATE, null);
        String myCols[] = {"key", "iStuffCuisinetype", "iStuffCookingMethod",
                "iStuffDietetic", "iStuffCourseType", "iStuffDishType",
                "iStuffMainIngredient", "iStuffOccasionOrSeason",
                "iStuffMiscellaneous", "iarea", "icity", "icountry",
                "ilatitude", "ilongitude",
                "uStuffCuisinetype", "uStuffCookingMethod", "uStuffDietetic",
                "uStuffCourseType", "uStuffDishType", "uStuffMainIngredient",
                "uStuffOccasionOrSeason", "uStuffMiscellaneous", "uarea",
                "ucity", "ucountry", "ulatitude", "ulongitude", "queryStatus"
        };
        c = myDatabase
                .query("Restaurants", myCols, null, null, null, null, null);
        rows = c.getCount();
        if (rows == 0)
        {
            Toast.makeText(RestaurantsViewQuery.this, R.string.noviewquery,
                    Toast.LENGTH_LONG).show();
        }
        bArray = new boolean[rows];
        int idcolumn = c.getColumnIndexOrThrow("key");
        int iStuffCuisinetype = c.getColumnIndexOrThrow("iStuffCuisinetype");
        int iStuffCookingMethod =
                c.getColumnIndexOrThrow("iStuffCookingMethod");
        int iStuffCourseType = c.getColumnIndexOrThrow("iStuffCourseType");
        int iStuffDietetic = c.getColumnIndexOrThrow("iStuffDietetic");
        int iStuffDishType = c.getColumnIndexOrThrow("iStuffDishType");
        int iStuffMainIngredient =
                c.getColumnIndexOrThrow("iStuffMainIngredient");
        int iStuffOccasionOrSeason =
                c.getColumnIndexOrThrow("iStuffOccasionOrSeason");
        int iStuffMiscellaneous =
                c.getColumnIndexOrThrow("iStuffMiscellaneous");
        int iarea = c.getColumnIndexOrThrow("iarea");
        int icity = c.getColumnIndexOrThrow("icity");
        int icountry = c.getColumnIndexOrThrow("icountry");
        int ilatitude = c.getColumnIndexOrThrow("ilatitude");
        int ilongitude = c.getColumnIndexOrThrow("ilongitude");

        int uStuffCuisinetype = c.getColumnIndexOrThrow("uStuffCuisinetype");
        int uStuffCookingMethod =
                c.getColumnIndexOrThrow("uStuffCookingMethod");
        int uStuffCourseType = c.getColumnIndexOrThrow("uStuffCourseType");
        int uStuffDietetic = c.getColumnIndexOrThrow("uStuffDietetic");
        int uStuffDishType = c.getColumnIndexOrThrow("uStuffDishType");
        int uStuffMainIngredient =
                c.getColumnIndexOrThrow("uStuffMainIngredient");
        int uStuffOccasionOrSeason =
                c.getColumnIndexOrThrow("uStuffOccasionOrSeason");
        int uStuffMiscellaneous =
                c.getColumnIndexOrThrow("uStuffMiscellaneous");
        int uarea = c.getColumnIndexOrThrow("uarea");
        int ucity = c.getColumnIndexOrThrow("ucity");
        int ucountry = c.getColumnIndexOrThrow("ucountry");
        int ulatitude = c.getColumnIndexOrThrow("ulatitude");
        int ulongitude = c.getColumnIndexOrThrow("ulongitude");
        int querystatuscolumn = c.getColumnIndexOrThrow("queryStatus");

        if (c != null)
        {
            count = 0;
            if (c.isFirst())
            {
                do
                {
                    int getid = c.getInt(idcolumn);
                    String getiStuffCuisinetype =
                            c.getString(iStuffCuisinetype);
                    String getiStuffCookingMethod =
                            c.getString(iStuffCookingMethod);
                    String getiStuffCourseType = c.getString(iStuffCourseType);
                    String getiStuffDietetic = c.getString(iStuffDietetic);
                    String getiStuffDishType = c.getString(iStuffDishType);
                    String getiStuffMainIngredient =
                            c.getString(iStuffMainIngredient);
                    String getiStuffOccasionOrSeason =
                            c.getString(iStuffOccasionOrSeason);
                    String getiStuffMiscellaneous =
                            c.getString(iStuffMiscellaneous);
                    String getiarea = c.getString(iarea);
                    String geticity = c.getString(icity);
                    String geticountry = c.getString(icountry);
                    String getilatitude = c.getString(ilatitude);
                    String getilongitude = c.getString(ilongitude);

                    String getuStuffCuisinetype =
                            c.getString(uStuffCuisinetype);
                    String getuStuffCookingMethod =
                            c.getString(uStuffCookingMethod);
                    String getuStuffCourseType = c.getString(uStuffCourseType);
                    String getuStuffDietetic = c.getString(uStuffDietetic);
                    String getuStuffDishType = c.getString(uStuffDishType);
                    String getuStuffMainIngredient =
                            c.getString(uStuffMainIngredient);
                    String getuStuffOccasionOrSeason =
                            c.getString(uStuffOccasionOrSeason);
                    String getuStuffMiscellaneous =
                            c.getString(uStuffMiscellaneous);
                    String getuarea = c.getString(uarea);
                    String getucity = c.getString(ucity);
                    String getucountry = c.getString(ucountry);
                    String getulatitude = c.getString(ulatitude);
                    String getulongitude = c.getString(ulongitude);
                    String getquerystatus = c.getString(querystatuscolumn);

                    RestaurantId[count] = getid;
                    iStuffCuisinetype1[count] = getiStuffCuisinetype;
                    iStuffCookingMethod1[count] = getiStuffCookingMethod;
                    iStuffCourseType1[count] = getiStuffCourseType;
                    iStuffDietetic1[count] = getiStuffDietetic;
                    iStuffDishType1[count] = getiStuffDishType;
                    iStuffMainIngredient1[count] = getiStuffMainIngredient;
                    iStuffOccasionOrSeason1[count] = getiStuffOccasionOrSeason;
                    iStuffMiscellaneous1[count] = getiStuffMiscellaneous;
                    iarea1[count] = getiarea;
                    icity1[count] = geticity;
                    icountry1[count] = geticountry;
                    ilatitude1[count] = getilatitude;
                    ilongitude1[count] = getilongitude;

                    uStuffCuisinetype1[count] = getuStuffCuisinetype;
                    uStuffCookingMethod1[count] = getuStuffCookingMethod;
                    uStuffCourseType1[count] = getuStuffCourseType;
                    uStuffDietetic1[count] = getuStuffDietetic;
                    uStuffDishType1[count] = getuStuffDishType;
                    uStuffMainIngredient1[count] = getuStuffMainIngredient;
                    uStuffOccasionOrSeason1[count] = getuStuffOccasionOrSeason;
                    uStuffMiscellaneous1[count] = getuStuffMiscellaneous;
                    uarea1[count] = getuarea;
                    ucity1[count] = getucity;
                    ucountry1[count] = getucountry;
                    ulatitude1[count] = getulatitude;
                    ulongitude1[count] = getulongitude;
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
                    "Owner Detail: Cuisinetype=" + iStuffCuisinetype1[j] +
                            ", CookingMethod=" + iStuffCookingMethod1[j] +
                            ", CourseType=" + iStuffCourseType1[j] +
                            ", Dietetic=" + iStuffDietetic1[j] + ", DishType=" +
                            iStuffDishType1[j] + ", MainIngredient=" +
                            iStuffMainIngredient1[j] + ", OccasionOrSeason=" +
                            iStuffOccasionOrSeason1[j] + ", Miscellaneous=" +
                            iStuffMiscellaneous1[j] + ", area=" + iarea1[j] +
                            ", city=" + icity1[j] + ", country=" +
                            icountry1[j] + ".\nCustomer Detail: Cuisinetype=" +
                            uStuffCuisinetype1[j] + ", CookingMethod= " +
                            uStuffCookingMethod1[j] + ", CourseType= " +
                            uStuffCourseType1[j] + ", Dietetic=" +
                            uStuffDietetic1[j] + ", DishType=" +
                            uStuffDishType1[j] + ", MainIngredient=" +
                            uStuffMainIngredient1[j] + ", OccasionOrSeason=" +
                            uStuffOccasionOrSeason1[j] + ", Miscellaneous1=" +
                            uStuffMiscellaneous1[j] + ", area=" + uarea1[j] +
                            ", city=" + ucity1[j] + ", country=" + ucountry1[j],
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

            /* bArray[position] = mCheckBox.isChecked();
            Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
            }
            });*/
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
                Intent mapViewintent = new Intent(RestaurantsViewQuery.this,
                        MapResults.class);
                startActivity(mapViewintent);
                break;
            case 2:
                Intent catalogintent = new Intent(RestaurantsViewQuery.this,
                        FindandInstall.class);
                startActivity(catalogintent);

                break;
            case 3:
                Intent settingsintent =
                        new Intent(RestaurantsViewQuery.this, Settings.class);
                startActivity(settingsintent);
                break;
            case 4:
                Intent resintent = new Intent(RestaurantsViewQuery.this,
                        Restaurants.class);
                startActivity(resintent);
                break;
            case 5:
                if (rows == 0)
                {
                    Toast.makeText(RestaurantsViewQuery.this,
                            R.string.donequery, Toast.LENGTH_LONG).show();
                }
                else
                {
                    for (int j = 0; j < rows; j++)
                    {
                        if (bArray[j] == true)
                        {
                            myDatabase.execSQL(
                                    "update category set querystatus='" +
                                            "true" + "' where categoryname='" +
                                            "Restaurants" + "';");
                        }
                        if (bArray[j] == false)
                        {
                            myDatabase.execSQL(
                                    "update Restaurants set queryStatus='" +
                                            "false" + "' where key=" + j + ";");
                        }
                    }
                    Toast.makeText(RestaurantsViewQuery.this,
                            this.getString(R.string.ShowMessage),
                            Toast.LENGTH_LONG).show();
//                Intent updateintent = new Intent(RestaurantsViewQuery.this, Restaurants.class);
//                startActivity(updateintent);
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
            
