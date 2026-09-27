package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: DeleteRestaurants.java 14 2008-08-19 06:36:45Z muthu.ramadoss          $: Id of last commit
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

public class DeleteRestaurants
        extends ListActivity
        implements OnItemClickListener
{

    SQLiteDatabase myDatabase = null;
    Cursor c;
    int rows = 0;
    int count = 0;
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
    String[] queryStatus = new String[size];
    int[] RestaurantId = new int[size];

    @Override
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
            //Context mContext = (Context) context;
            mContext = context;
            myDatabase = mContext.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String myCols[] = {"key", "iStuffCuisinetype",
                    "iStuffCookingMethod", "iStuffDietetic", "iStuffCourseType",
                    "iStuffDishType",
                    "iStuffMainIngredient", "iStuffOccasionOrSeason",
                    "iStuffMiscellaneous", "iarea", "icity", "icountry",
                    "ilatitude", "ilongitude",
                    "uStuffCuisinetype", "uStuffCookingMethod",
                    "uStuffDietetic", "uStuffCourseType", "uStuffDishType",
                    "uStuffMainIngredient",
                    "uStuffOccasionOrSeason", "uStuffMiscellaneous", "uarea",
                    "ucity", "ucountry", "ulatitude", "ulongitude",
                    "queryStatus"
            };
            c = myDatabase
                    .query("Restaurants", myCols, null, null, null, null, null);
            rows = c.getCount();
            // bArray = new boolean[rows];
            int idcolumn = c.getColumnIndexOrThrow("key");
            int iStuffCuisinetype =
                    c.getColumnIndexOrThrow("iStuffCuisinetype");
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

            int uStuffCuisinetype =
                    c.getColumnIndexOrThrow("uStuffCuisinetype");
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
                        String getiStuffCourseType =
                                c.getString(iStuffCourseType);
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
                        String getuStuffCourseType =
                                c.getString(uStuffCourseType);
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
                        iStuffOccasionOrSeason1[count] =
                                getiStuffOccasionOrSeason;
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
                        uStuffOccasionOrSeason1[count] =
                                getuStuffOccasionOrSeason;
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
                sv = new SpeechView(mContext, " Cuisinetype1 = " +
                        iStuffCuisinetype1[position] + " CookingMethod1 = " +
                        iStuffCookingMethod1[position] + " CourseType1 = " +
                        iStuffCourseType1[position] + " Dietetic1 = " +
                        iStuffDietetic1[position] + " DishType1 = " +
                        iStuffDishType1[position] + "MainIngredient1 = " +
                        iStuffMainIngredient1[position] +
                        "OccasionOrSeason1 = " +
                        iStuffOccasionOrSeason1[position] +
                        "Miscellaneous1 = " + iStuffMiscellaneous1[position] +
                        "iarea1 = " + iarea1[position] + "icity1 = " +
                        icity1[position] + "icountry1 = " +
                        icountry1[position] + "ilatitude1 = " +
                        ilatitude1[position] + "ilongitude1 = " +
                        ilongitude1[position], "uCuisinetype1 = " +
                        uStuffCuisinetype1[position] + " uCookingMethod1 = " +
                        uStuffCookingMethod1[position] + " uCourseType1 = " +
                        uStuffCourseType1[position] + " uDietetic1 = " +
                        uStuffDietetic1[position] + " uDishType1 = " +
                        uStuffDishType1[position] + "uMainIngredient1 = " +
                        uStuffMainIngredient1[position] +
                        "uOccasionOrSeason1 = " +
                        uStuffOccasionOrSeason1[position] +
                        "uMiscellaneous1 = " + uStuffMiscellaneous1[position] +
                        "uarea1 = " + uarea1[position] + "ucity1 = " +
                        ucity1[position] + "ucountry1 = " +
                        ucountry1[position] + "ulatitude1 = " +
                        ulatitude1[position] + "ulongitude1 = " +
                        ulongitude1[position] + " QueryStatus = " +
                        queryStatus[position]);
            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iStuffCuisinetype1[position]);
                sv.setDialogue(iStuffDietetic1[position]);
            }
            return sv;
        }

        private Context mContext;
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
                    myDatabase = DeleteRestaurants.this
                            .openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    myDatabase.delete("Restaurants",
                            "key=" + RestaurantId[selectedId], null);
                    if (selectedId == 0 && RestaurantId[0] != 0)
                    {
                        myDatabase.delete("Restaurants",
                                "key=" + RestaurantId[0], null);
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(DeleteRestaurants.this, "Error",
                            Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(DeleteRestaurants.this,
                        DeleteRestaurants.class);
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
                Intent stuffCheckintent = new Intent(DeleteRestaurants.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(DeleteRestaurants.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(DeleteRestaurants.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}

