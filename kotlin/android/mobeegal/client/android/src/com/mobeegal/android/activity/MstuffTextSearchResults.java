package com.mobeegal.android.activity;

/*
<!--
$Id:: MstuffTextSearchResults.java 14 2008-08-19 06:36:45Z muthu.ramadoss    $: Id of last commit
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.util.ViewMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mobeegal.in
 */
public class MstuffTextSearchResults
        extends ListActivity
        implements OnItemSelectedListener, OnItemClickListener
{

    SQLiteDatabase mobeegalDatabase = null;
    Cursor c;
    Cursor categoryCursor;
    ArrayList<String> results;
    int rows = 0;
    int count;
    String[] mStuffid;
    private Bundle getCatalog;
    String passedCatalogValue;
    String passedSearchValue;
    String details;
    String location;
    int noOfMatches = 0;
    TextView mPhone;
    int selectedposition;
    String selectedLocation;
    int[] latitude;
    int[] longitude;
    MstuffTextListAdapter btla;
    MstuffText mstufftextArray[];
    int index = 0;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        results = new ArrayList<String>();

        getCatalog = this.getIntent().getExtras();
        if (getCatalog != null)
        {
            passedCatalogValue = getCatalog.getString("spinner");
            passedSearchValue = getCatalog.getString("edittext");
        }

        try
        {
            mobeegalDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String myCols[] = {"mstuffid", "details", "latitude", "longitude",
                    "location"};
            c = mobeegalDatabase.query("mStuffdetails", myCols, null,
                    null, null, null, null);
            rows = c.getCount() - 3;
            btla = new MstuffTextListAdapter(this);
            mstufftextArray = new MstuffText[rows];

            mStuffid = new String[rows];
            latitude = new int[rows];
            longitude = new int[rows];
            int useridColumn = c.getColumnIndexOrThrow("mstuffid");
            int detailsColumn = c.getColumnIndexOrThrow("details");
            int latitudeColumn = c.getColumnIndexOrThrow("latitude");
            int longitudeColumn = c.getColumnIndexOrThrow("longitude");
            if (c != null)
            {
                if (c.isFirst())
                {
                    count = 0;

                    do
                    {
                        mStuffid[count] = c.getString(useridColumn);
                        latitude[count] = c.getInt(latitudeColumn);
                        longitude[count] = c.getInt(longitudeColumn);
                        details = c.getString(detailsColumn);
                        if (details.toLowerCase()
                                .contains(passedSearchValue.toLowerCase()))
                        {

                            if (passedCatalogValue.equals("Dating"))
                            {
                                String column[] =
                                        {"details", "latitude", "longitude"};
                                categoryCursor = mobeegalDatabase
                                        .query("mStuffdetails", column,
                                                "catagory='Dating'", null, null,
                                                null,
                                                null);
                                int detailsColumndating = categoryCursor
                                        .getColumnIndexOrThrow("details");
                                int latitudeColumndating = categoryCursor
                                        .getColumnIndexOrThrow("latitude");
                                int longitudeColumndating = categoryCursor
                                        .getColumnIndexOrThrow("longitude");
                                if (categoryCursor != null)
                                {
                                    index = 0;
                                    if (categoryCursor.isFirst())
                                    {
                                        do
                                        {
                                            String detailsdating =
                                                    categoryCursor.getString(
                                                            detailsColumndating);
                                            int latitudedating = categoryCursor
                                                    .getInt(latitudeColumndating);
                                            int longitudedating = categoryCursor
                                                    .getInt(longitudeColumndating);
                                            mstufftextArray[index] =
                                                    new MstuffText(
                                                            detailsdating +
                                                                    ", Latitude = " +
                                                                    Integer.toString(
                                                                            latitudedating) +
                                                                    ", Longitude = " +
                                                                    Integer.toString(
                                                                            longitudedating),
                                                            getResources().getDrawable(
                                                                    R.drawable.dating_icon));
                                            btla.addItem(
                                                    mstufftextArray[index]);
                                            index++;
                                        }
                                        while (categoryCursor.moveToNext());
                                    }
                                    break;
                                }
                            }

                            if (passedCatalogValue.equals("Matrimony"))
                            {
                                String column[] =
                                        {"details", "latitude", "longitude"};
                                categoryCursor = mobeegalDatabase
                                        .query("mStuffdetails", column,
                                                "catagory='Matrimony'", null,
                                                null,
                                                null, null);
                                int detailsColumnmatrimony = categoryCursor
                                        .getColumnIndexOrThrow("details");
                                int latitudeColumnmatrimony = categoryCursor
                                        .getColumnIndexOrThrow("latitude");
                                int longitudeColumnmatrimony = categoryCursor
                                        .getColumnIndexOrThrow("longitude");
                                if (categoryCursor != null)
                                {
                                    index = 0;
                                    if (categoryCursor.isFirst())
                                    {
                                        do
                                        {
                                            String detailsdating =
                                                    categoryCursor.getString(
                                                            detailsColumnmatrimony);
                                            int latitudedating = categoryCursor
                                                    .getInt(latitudeColumnmatrimony);
                                            int longitudedating = categoryCursor
                                                    .getInt(longitudeColumnmatrimony);
                                            mstufftextArray[index] =
                                                    new MstuffText(
                                                            detailsdating +
                                                                    ", Latitude = " +
                                                                    Integer.toString(
                                                                            latitudedating) +
                                                                    ", Longitude = " +
                                                                    Integer.toString(
                                                                            longitudedating),
                                                            getResources().getDrawable(
                                                                    R.drawable.matrimony_icon));
                                            btla.addItem(
                                                    mstufftextArray[index]);
                                            index++;
                                        }
                                        while (categoryCursor.moveToNext());
                                    }
                                    break;
                                }
                            }

                            if (passedCatalogValue.equals("Jewelry"))
                            {
                                String column[] =
                                        {"details", "latitude", "longitude"};
                                categoryCursor = mobeegalDatabase
                                        .query("mStuffdetails", column,
                                                "catagory='Jewelry'", null,
                                                null, null,
                                                null);
                                int detailsColumnjewelry = categoryCursor
                                        .getColumnIndexOrThrow("details");
                                int latitudeColumnjewelry = categoryCursor
                                        .getColumnIndexOrThrow("latitude");
                                int longitudeColumnjewelry = categoryCursor
                                        .getColumnIndexOrThrow("longitude");
                                if (categoryCursor != null)
                                {
                                    index = 0;
                                    if (categoryCursor.isFirst())
                                    {
                                        do
                                        {
                                            String detailsdating =
                                                    categoryCursor.getString(
                                                            detailsColumnjewelry);
                                            int latitudedating = categoryCursor
                                                    .getInt(latitudeColumnjewelry);
                                            int longitudedating = categoryCursor
                                                    .getInt(longitudeColumnjewelry);
                                            mstufftextArray[index] =
                                                    new MstuffText(
                                                            detailsdating +
                                                                    ", Latitude = " +
                                                                    Integer.toString(
                                                                            latitudedating) +
                                                                    ", Longitude = " +
                                                                    Integer.toString(
                                                                            longitudedating),
                                                            getResources().getDrawable(
                                                                    R.drawable.jewelry_icon));
                                            btla.addItem(
                                                    mstufftextArray[index]);
                                            index++;
                                        }
                                        while (categoryCursor.moveToNext());
                                    }
                                    break;
                                }
                            }

                            if (passedCatalogValue.equals("Cars"))
                            {
                                String column[] =
                                        {"details", "latitude", "longitude"};
                                categoryCursor = mobeegalDatabase
                                        .query("mStuffdetails", column,
                                                "catagory='Cars'", null, null,
                                                null,
                                                null);
                                int detailsColumncars = categoryCursor
                                        .getColumnIndexOrThrow("details");
                                int latitudeColumncars = categoryCursor
                                        .getColumnIndexOrThrow("latitude");
                                int longitudeColumncars = categoryCursor
                                        .getColumnIndexOrThrow("longitude");
                                if (categoryCursor != null)
                                {
                                    index = 0;
                                    if (categoryCursor.isFirst())
                                    {
                                        do
                                        {
                                            String detailsdating =
                                                    categoryCursor.getString(
                                                            detailsColumncars);
                                            int latitudedating = categoryCursor
                                                    .getInt(latitudeColumncars);
                                            int longitudedating = categoryCursor
                                                    .getInt(longitudeColumncars);
                                            mstufftextArray[index] =
                                                    new MstuffText(
                                                            detailsdating +
                                                                    ", Latitude = " +
                                                                    Integer.toString(
                                                                            latitudedating) +
                                                                    ", Longitude = " +
                                                                    Integer.toString(
                                                                            longitudedating),
                                                            getResources().getDrawable(
                                                                    R.drawable.cars_icon));
                                            btla.addItem(
                                                    mstufftextArray[index]);
                                            index++;
                                        }
                                        while (categoryCursor.moveToNext());
                                    }
                                    break;
                                }
                            }

                        }
                        count++;
                    }
                    while (c.moveToNext());
                    Toast.makeText(MstuffTextSearchResults.this, index +
                            " Matches found out of " + rows + " mStuffs ",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(MstuffTextSearchResults.this, "Error",
                    Toast.LENGTH_LONG).show();
        }

        setListAdapter(btla);
        getListView().setOnItemSelectedListener(this);
        getListView().setOnItemClickListener(this);
    }

    public class MstuffText
            extends Object
    {

        private String mText;
        private Drawable mBullet;
        private boolean mSelectable = true;

        public MstuffText(String text, Drawable bullet)
        {
            mBullet = bullet;
            mText = text;
        }

        public boolean isSelectable()
        {
            return mSelectable;
        }

        public void setSelectable(boolean selectable)
        {
            mSelectable = selectable;
        }

        public String getText()
        {
            return mText;
        }

        public void setText(String text)
        {
            mText = text;
        }

        public void setBullet(Drawable bullet)
        {
            mBullet = bullet;
        }

        public Drawable getBullet()
        {
            return mBullet;
        }
    }

    public class MstuffTextListAdapter
            extends BaseAdapter
    {

        private Context mContext;
        private List<MstuffText> mItems;

        public MstuffTextListAdapter(Context context)
        {
            mContext = context;
            mItems = new ArrayList<MstuffText>();
        }

        void addItem(MstuffText bt)
        {
            mItems.add(bt);
        }

        void setListItems(List<MstuffText> bti)
        {
            mItems = bti;
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

        public boolean isSelectable(int position)
        {
            return mItems.get(position).isSelectable();
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            MstuffTextView btv;
            if (convertView == null)
            {
                btv = new MstuffTextView(mContext,
                        mItems.get(position).getText(),
                        mItems.get(position).getBullet());
            }
            else
            {
                btv = (MstuffTextView) convertView;
                btv.setText(mItems.get(position).getText());
                btv.setBullet(mItems.get(position).getBullet());
            }
            return btv;
        }
    }

    public class MstuffTextView
            extends LinearLayout
    {

        private TextView mText;
        private ImageView mBullet;

        public MstuffTextView(Context context, String text, Drawable bullet)
        {
            super(context);
            this.setOrientation(HORIZONTAL);
            mBullet = new ImageView(context);
            mBullet.setImageDrawable(bullet);
            // left, top, right, bottom
            mBullet.setPadding(0, 2, 5, 0);
            addView(mBullet, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            mText = new TextView(context);
            mText.setText(text);
            addView(mText, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        public void setText(String words)
        {
            mText.setText(words);
        }

        public void setBullet(Drawable bullet)
        {
            mBullet.setImageDrawable(bullet);
        }
    }

    public void onItemClick(AdapterView parent1, View v, int position, long id)
    {
        getListView().setSelection(position);
    }

    public void onItemSelected(AdapterView parent, View v, int position,
            long id)
    {
        selectedposition = parent.getSelectedItemPosition();
    }

    public void onNothingSelected(AdapterView arg0)
    {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsSearchMenu(menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {

            case 1:

                Intent stuffCheckintent = new Intent(
                        MstuffTextSearchResults.this, MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:

                Intent intent1 = new Intent(MstuffTextSearchResults.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:

                Intent settings = new Intent(MstuffTextSearchResults.this,
                        Settings.class);
                startActivity(settings);
                break;
            case 4:

                Intent mStuffSearchIntent = new Intent(
                        MstuffTextSearchResults.this, MstuffSearch.class);
                Bundle searchBundle = new Bundle();
                searchBundle.putString("TextView", "TextView");
                mStuffSearchIntent.putExtras(searchBundle);
                startActivityForResult(mStuffSearchIntent, 0);
                startActivity(mStuffSearchIntent);
                break;
            case 5:

                Intent intent =
                        new Intent(MstuffTextSearchResults.this, Chat.class);
                Bundle b = new Bundle();
                b.putString("mstuffid", mStuffid[selectedposition]);
                intent.putExtras(b);
                startActivityForResult(intent, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
