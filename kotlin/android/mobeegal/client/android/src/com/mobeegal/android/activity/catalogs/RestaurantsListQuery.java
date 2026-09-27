package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: RestaurantsListQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss       $: Id of last commit
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

public class RestaurantsListQuery
        extends ListActivity
        implements OnItemClickListener
{
    SQLiteDatabase myDatabase = null;
    Cursor c;

    int rows = 0;
    int count = 0;
    int size = 25;
    String[] iStuffCuisinetype = new String[size];
    String[] iStuffCookingMethod = new String[size];
    String[] iStuffDietetic = new String[size];
    String[] iStuffCourseType = new String[size];
    String[] iStuffDishType = new String[size];
    String[] iStuffMainIngredient = new String[size];
    String[] iStuffOccasionOrSeason = new String[size];
    String[] iStuffMiscellaneous = new String[size];
    String[] iRestaurantsArea = new String[size];
    String[] iRestaurantsCity = new String[size];
    String[] iRestaurantsCountry = new String[size];
    String[] iRestaurantslatitude = new String[size];
    String[] iRestaurantslongitude = new String[size];

    String[] uStuffCuisinetype = new String[size];
    String[] uStuffCookingMethod = new String[size];
    String[] uStuffDietetic = new String[size];
    String[] uStuffCourseType = new String[size];
    String[] uStuffDishType = new String[size];
    String[] uStuffMainIngredient = new String[size];
    String[] uStuffOccasionOrSeason = new String[size];
    String[] uStuffMiscellaneous = new String[size];
    String[] uRestaurantsArea = new String[size];
    String[] uRestaurantsCity = new String[size];
    String[] uRestaurantsCountry = new String[size];
    String[] uRestaurantslatitude = new String[size];
    String[] uRestaurantslongitude = new String[size];
    int[] RestaurantId = new int[size];


    int iCuisineType;
    int iCookingMethod;
    int iDietetic;
    int iCourseType;
    int iDishType;
    int iMainIngredient;
    int iOccasionOrSeason;
    int iMiscellaneous;

    String iRestaurantscity;
    String iRestaurantsarea;
    String iRestaurantscountry;
    String getilatitude;
    String getilongitude;
    int uCuisineType;
    int uCookingMethod;
    int uDietetic;
    int uCourseType;
    int uDishType;
    int uMainIngredient;
    int uOccasionOrSeason;
    int uMiscellaneous;
    String uRestaurantscity;
    String uRestaurantsarea;
    String uRestaurantscountry;
    String getulatitude;
    String getulongitude;

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
            try
            {
                myDatabase = mContext.openOrCreateDatabase("Mobeegal",
                        Context.MODE_PRIVATE, null);
                String myCols[] = {"key", "iStuffCuisinetype",
                        "iStuffCookingMethod", "iStuffDietetic",
                        "iStuffCourseType",
                        "iStuffDishType", "iStuffMainIngredient",
                        "iStuffOccasionOrSeason", "iStuffMiscellaneous",
                        "iarea", "icity", "icountry", "ilatitude", "ilongitude",
                        "uStuffCuisinetype", "uStuffCookingMethod",
                        "uStuffDietetic", "uStuffCourseType", "uStuffDishType",
                        "uStuffMainIngredient",
                        "uStuffOccasionOrSeason", "uStuffMiscellaneous",
                        "uarea", "ucity", "ucountry",
                        "ulatitude", "ulongitude", "queryStatus"};
                c = myDatabase.query("Restaurants", myCols, null, null, null,
                        null, null);
                rows = c.getCount();

                int idcolumn = c.getColumnIndexOrThrow("key");
                int iStuffcuisinetype =
                        c.getColumnIndexOrThrow("iStuffCuisinetype");
                int iStuffcookingMethod =
                        c.getColumnIndexOrThrow("iStuffCookingMethod");
                int iStuffdietetic = c.getColumnIndexOrThrow("iStuffDietetic");
                int iStuffcourseType =
                        c.getColumnIndexOrThrow("iStuffCourseType");
                int iStuffdishType = c.getColumnIndexOrThrow("iStuffDishType");
                int iStuffmainIngredient =
                        c.getColumnIndexOrThrow("iStuffMainIngredient");
                int iStuffoccasionOrSeason =
                        c.getColumnIndexOrThrow("iStuffOccasionOrSeason");
                int iStuffmiscellaneous =
                        c.getColumnIndexOrThrow("iStuffMiscellaneous");
                int iRestaurantsarea = c.getColumnIndexOrThrow("iarea");
                int iRestaurantscity = c.getColumnIndexOrThrow("icity");
                int iRestaurantscountry = c.getColumnIndexOrThrow("icountry");


                int uStuffcuisinetype =
                        c.getColumnIndexOrThrow("uStuffCuisinetype");
                int uStuffcookingMethod =
                        c.getColumnIndexOrThrow("uStuffCookingMethod");
                int uStuffdietetic = c.getColumnIndexOrThrow("uStuffDietetic");
                int uStuffcourseType =
                        c.getColumnIndexOrThrow("uStuffCourseType");
                int uStuffdishType = c.getColumnIndexOrThrow("uStuffDishType");
                int uStuffmainIngredient =
                        c.getColumnIndexOrThrow("uStuffMainIngredient");
                int uStuffoccasionOrSeason =
                        c.getColumnIndexOrThrow("uStuffOccasionOrSeason");
                int uStuffmiscellaneous =
                        c.getColumnIndexOrThrow("uStuffMiscellaneous");
                int uRestaurantsarea = c.getColumnIndexOrThrow("uarea");
                int uRestaurantscity = c.getColumnIndexOrThrow("ucity");
                int uRestaurantscountry = c.getColumnIndexOrThrow("ucountry");

                if (c != null)
                {
                    count = 0;
                    if (c.isFirst())
                    {
                        do
                        {
                            int getid = c.getInt(idcolumn);
                            String iCuisineType =
                                    c.getString(iStuffcuisinetype);
                            String iCookingMethod =
                                    c.getString(iStuffcookingMethod);
                            String iDietetic = c.getString(iStuffdietetic);
                            String iCourseType = c.getString(iStuffcourseType);
                            String iDishType = c.getString(iStuffdishType);
                            String iMainIngredient =
                                    c.getString(iStuffmainIngredient);
                            String iOccasionOrSeason =
                                    c.getString(iStuffoccasionOrSeason);
                            String iMiscellaneous =
                                    c.getString(iStuffmiscellaneous);
                            String irestaurantsarea =
                                    c.getString(iRestaurantsarea);
                            String irestaurantscity =
                                    c.getString(iRestaurantscity);
                            String irestaurantscountry =
                                    c.getString(iRestaurantscountry);

                            String uCuisineType =
                                    c.getString(uStuffcuisinetype);
                            String uCookingMethod =
                                    c.getString(uStuffcookingMethod);
                            String uDietetic = c.getString(uStuffdietetic);
                            String uCourseType = c.getString(uStuffcourseType);
                            String uDishType = c.getString(uStuffdishType);
                            String uMainIngredient =
                                    c.getString(uStuffmainIngredient);
                            String uOccasionOrSeason =
                                    c.getString(uStuffoccasionOrSeason);
                            String uMiscellaneous =
                                    c.getString(uStuffmiscellaneous);
                            String urestaurantsarea =
                                    c.getString(uRestaurantsarea);
                            String urestaurantscity =
                                    c.getString(uRestaurantscity);
                            String urestaurantscountry =
                                    c.getString(uRestaurantscountry);


                            RestaurantId[count] = getid;
                            iStuffCuisinetype[count] = iCuisineType;
                            iStuffCookingMethod[count] = iCookingMethod;
                            iStuffDietetic[count] = iDietetic;
                            iStuffCourseType[count] = iCourseType;
                            iStuffDishType[count] = iDishType;
                            iStuffMainIngredient[count] = iMainIngredient;
                            iStuffOccasionOrSeason[count] = iOccasionOrSeason;
                            iStuffMiscellaneous[count] = iMiscellaneous;
                            iRestaurantsArea[count] = irestaurantsarea;
                            iRestaurantsCity[count] = irestaurantscity;
                            iRestaurantsCountry[count] = irestaurantscountry;


                            uStuffCuisinetype[count] = uCuisineType;
                            uStuffCookingMethod[count] = uCookingMethod;
                            uStuffDietetic[count] = uDietetic;
                            uStuffCourseType[count] = uCourseType;
                            uStuffDishType[count] = uDishType;
                            uStuffMainIngredient[count] = uMainIngredient;
                            uStuffOccasionOrSeason[count] = uOccasionOrSeason;
                            uStuffMiscellaneous[count] = uMiscellaneous;
                            uRestaurantsArea[count] = urestaurantsarea;
                            uRestaurantsCity[count] = urestaurantscity;
                            uRestaurantsCountry[count] = urestaurantscountry;

                            count++;
                        }
                        while (c.moveToNext());
                    }
                }

            }
            catch (Exception ex)
            {
                Toast.makeText(RestaurantsListQuery.this, "" + ex,
                        Toast.LENGTH_LONG).show();
                //Logger.getLogger(ListClick.class.getName()).log(Level.SEVERE, null, ex);
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
                sv = new SpeechView(mContext,
                        " ,iCuisineType = " + iStuffCuisinetype[position] +
                                ", iCookingMethod = " +
                                iStuffCookingMethod[position] +
                                ", iDietetic = " + iStuffDietetic[position] +
                                ", iCourseType = " + iStuffCourseType[position]
                                + ",iDishType = " + iStuffDishType[position] +
                                ",iMainIngrediant =" +
                                iStuffMainIngredient[position] +
                                ",iOccasionOrSeason = " +
                                iStuffOccasionOrSeason[position] +
                                ",iMiscellaneous =" +
                                iRestaurantsArea[position] + ",iArea =" +
                                iRestaurantsArea[position] + ",iCity =" +
                                iRestaurantsCity[position] + ",icountry = " +
                                iRestaurantsCountry[position],
                        ",uCuisineType = " + uStuffCuisinetype[position] +
                                " ,uCookingMethod = " +
                                uStuffCookingMethod[position] +
                                " ,uDietetic = " + uStuffDietetic[position] +
                                ", uCourseType = " + uStuffCourseType[position]
                                + ",uDishType = " + uStuffDishType[position] +
                                ",uMainIngrediant =" +
                                uStuffMainIngredient[position] +
                                ",uOccasionOrSeason = " +
                                uStuffOccasionOrSeason[position] +
                                ",uMiscellaneous =" +
                                uRestaurantsArea[position] + ",uArea =" +
                                uRestaurantsArea[position] + ",uCity =" +
                                uRestaurantsCity[position] + ",ucountry =" +
                                uRestaurantsCountry[position]);


            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iStuffCuisinetype[position]);
                sv.setDialogue(uRestaurantsCountry[position]);
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
            mTitle.setText("Owner detail : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("Customer Detail : " + words);
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

        String selectid = parent.getItemAtPosition(position).toString();
        int passkeyvalue = Integer.parseInt(selectid);
        Intent editrestaurants =
                new Intent(RestaurantsListQuery.this, Restaurants.class);
        Cursor restaurantsCursor = myDatabase.query("restaurantsposition", null,
                "key=" + RestaurantId[passkeyvalue], null, null, null, null);
        if (restaurantsCursor != null)
        {
            if (restaurantsCursor.isFirst())
            {
                do
                {
                    //iCuisineTypeposition,iCookingMethodposition ,iDieteticposition,iCourseTypeposition
                    //,iDishTypeposition ,iMainIngredientposition ,iOccasionOrSeasonposition ,iMiscellaneousposition
                    //,iarea , icity , icountry , uCuisineTypeposition , uCookingMethodposition , uDieteticposition ,
                    //uCourseTypeposition ,uDishTypeposition ,uMainIngredientposition ,uOccasionOrSeasonposition ,uMiscellaneousposition
                    //,uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype
                    iCuisineType = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iCuisineTypeposition"));
                    iCookingMethod = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iCookingMethodposition"));
                    iDietetic = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iDieteticposition"));
                    iCourseType = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iCourseTypeposition"));
                    iDishType = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iDishTypeposition"));
                    iMainIngredient = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iMainIngredientposition"));
                    iOccasionOrSeason = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iOccasionOrSeasonposition"));
                    iMiscellaneous = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "iMiscellaneousposition"));
                    iRestaurantsarea = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow("iarea"));
                    iRestaurantscity = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow("icity"));
                    iRestaurantscountry = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                    "icountry"));
                    getilatitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                    "ilatitude"));
                    getilongitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                    "ilongitude"));

                    uCuisineType = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uCuisineTypeposition"));
                    uCookingMethod = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uCookingMethodposition"));
                    uDietetic = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uDieteticposition"));
                    uCourseType = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uCourseTypeposition"));
                    uDishType = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uDishTypeposition"));
                    uMainIngredient = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uMainIngredientposition"));
                    uOccasionOrSeason = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uOccasionOrSeasonposition"));
                    uMiscellaneous = restaurantsCursor
                            .getInt(restaurantsCursor.getColumnIndexOrThrow(
                                    "uMiscellaneousposition"));
                    uRestaurantsarea = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow("uarea"));
                    uRestaurantscity = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow("ucity"));
                    uRestaurantscountry = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                    "ucountry"));
                    getulatitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                    "ulatitude"));
                    getulongitude = restaurantsCursor.getString(
                            restaurantsCursor.getColumnIndexOrThrow(
                                    "ulongitude"));
                    myDatabase.execSQL(
                            "CREATE TABLE IF NOT EXISTS temprestaurant" +
                                    " (iCuisineTypeposition NUMERIC, iCookingMethodposition NUMERIC, iDieteticposition NUMERIC,iCourseTypeposition NUMERIC,iDishTypeposition NUMERIC,iMainIngredientposition NUMERIC,iOccasionOrSeasonposition NUMERIC,iMiscellaneousposition NUMERIC ,iarea VARCHAR, icity VARCHAR, icountry VARCHAR, uCuisineTypeposition NUMERIC, uCookingMethodposition NUMERIC, uDieteticposition NUMERIC,uCourseTypeposition NUMERIC,uDishTypeposition NUMERIC,uMainIngredientposition NUMERIC,uOccasionOrSeasonposition NUMERIC,uMiscellaneousposition NUMERIC ,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");
                    myDatabase.execSQL(
                            "INSERT INTO temprestaurant (iCuisineTypeposition,iCookingMethodposition ,iDieteticposition,iCourseTypeposition,iDishTypeposition ,iMainIngredientposition ,iOccasionOrSeasonposition ,iMiscellaneousposition  ,iarea , icity , icountry , uCuisineTypeposition , uCookingMethodposition , uDieteticposition ,uCourseTypeposition ,uDishTypeposition ,uMainIngredientposition ,uOccasionOrSeasonposition ,uMiscellaneousposition  ,uarea , ucity , ucountry , ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype  ) VALUES (" +
                                    iCuisineType + "," + iCookingMethod + "," +
                                    iDietetic + "," + iCourseType + "," +
                                    iDishType + "," + iMainIngredient + "," +
                                    iOccasionOrSeason + "," + iMiscellaneous +
                                    ",'" + iRestaurantsarea + "','" +
                                    iRestaurantscity + "','" +
                                    iRestaurantscountry + "'," + uCuisineType +
                                    "," + uCookingMethod + "," + uDietetic +
                                    "," + uCourseType + "," + uDishType + "," +
                                    uMainIngredient + "," + uOccasionOrSeason +
                                    "," + uMiscellaneous + ",'" +
                                    uRestaurantsarea + "','" +
                                    uRestaurantscity + "','" +
                                    uRestaurantscountry + "','" + getilatitude +
                                    "','" + getilongitude + "','" +
                                    getulatitude + "','" + getulongitude +
                                    "','" + "Restaurants" + "', '" + "istuff" +
                                    "');");
                }
                while (c.moveToNext());
            }
        }
        Bundle passkeyBundle = new Bundle();
        passkeyBundle.putInt("key", RestaurantId[passkeyvalue]);
        editrestaurants.putExtras(passkeyBundle);
        startActivityForResult(editrestaurants, 0);


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
                Intent stuffCheckintent = new Intent(RestaurantsListQuery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(RestaurantsListQuery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(RestaurantsListQuery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}


