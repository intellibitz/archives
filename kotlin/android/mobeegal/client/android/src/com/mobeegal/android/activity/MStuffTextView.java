package com.mobeegal.android.activity;

/*
<!--
$Id:: MStuffTextView.java 14 2008-08-19 06:36:45Z muthu.ramadoss             $: Id of last commit
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

public class MStuffTextView
        extends ListActivity
        implements OnItemSelectedListener, OnItemClickListener
{

    SQLiteDatabase myDatabase = null;
    Cursor mstuffCursor;
    int rows;
    int count;
    String[] mstuffid;
    String[] catagory;
    String[] details;
    int[] latitude;
    int[] longitude;
    String[] location;
    MstuffText mstufftextArray[];
    MstuffTextListAdapter btla;
    int selectedposition;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        try
        {
            myDatabase = MStuffTextView.this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String columnName[] = {"mstuffid", "catagory", "details",
                    "latitude", "longitude", "location"};
            mstuffCursor = myDatabase.query("mStuffdetails", columnName,
                    null, null, null, null, null);
            rows = mstuffCursor.getCount();

            mstuffid = new String[rows];
            catagory = new String[rows];
            details = new String[rows];
            latitude = new int[rows];
            longitude = new int[rows];
            location = new String[rows];

            int idcolumn = mstuffCursor.getColumnIndexOrThrow("mstuffid");
            int catagoryColumn = mstuffCursor.getColumnIndexOrThrow("catagory");
            int detailsColumn = mstuffCursor.getColumnIndexOrThrow("details");
            int latitudeColumn = mstuffCursor.getColumnIndexOrThrow("latitude");
            int longitudeColumn =
                    mstuffCursor.getColumnIndexOrThrow("longitude");
            int locationColumn = mstuffCursor.getColumnIndexOrThrow("location");

            if (mstuffCursor != null)
            {
                count = 0;
                if (mstuffCursor.isFirst())
                {
                    do
                    {
                        mstuffid[count] = mstuffCursor.getString(idcolumn);
                        catagory[count] =
                                mstuffCursor.getString(catagoryColumn);
                        details[count] = mstuffCursor.getString(detailsColumn);
                        latitude[count] = mstuffCursor.getInt(latitudeColumn);
                        longitude[count] = mstuffCursor.getInt(longitudeColumn);
                        location[count] =
                                mstuffCursor.getString(locationColumn);
                        count++;
                    }
                    while (mstuffCursor.moveToNext());
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(MStuffTextView.this, "Sorry, No matches found",
                    Toast.LENGTH_SHORT).show();
        }
        btla = new MstuffTextListAdapter(this);
        mstufftextArray = new MstuffText[rows];

        for (int i = 0; i < rows; i++)
        {
            if (catagory[i].equalsIgnoreCase("Dating"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.dating_icon));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("Matrimony"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.matrimony_icon));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("Cars"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.cars_icon));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("Jewelry"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.jewelry_icon));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("Restaurants"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.restaurant_icon));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("Movies"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.movies_icon));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("Rental"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.rental_icon));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("Marker"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.marker));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("userDating"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.user));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("userMatrimony"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.user));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("userCars"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.user));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("userJewelry"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.user));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("userRestaurants"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.user));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("userMovies"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.user));
                btla.addItem(mstufftextArray[i]);
            }
            if (catagory[i].equalsIgnoreCase("userRental"))
            {
                mstufftextArray[i] = new MstuffText(details[i] +
                        ", Latitude = " + latitude[i] + ", Longitude = " +
                        longitude[i],
                        getResources().getDrawable(R.drawable.user));
                btla.addItem(mstufftextArray[i]);
            }

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
        ViewMenu.onCreateOptionsTextMenu(menu);
        return true;
    }

    public void onItemClick(AdapterView parent, View v, int position, long id)
    {
        getListView().setSelection(position);
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {

            case 1:
                Intent stuffCheckintent =
                        new Intent(MStuffTextView.this, MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 =
                        new Intent(MStuffTextView.this, FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(MStuffTextView.this, Settings.class);
                startActivity(settings);
                break;
            case 4:
                try
                {
                    myDatabase =
                            MStuffTextView.this.openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    myDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                            "Favourite" +
                            " (mstuffid VARCHAR, catagory VARCHAR, details VARCHAR, latitude NUMERIC,  longitude NUMERIC, location VARCHAR);");
                    Toast.makeText(MStuffTextView.this, "Added to Favorite ",
                            Toast.LENGTH_LONG).show();
                    Cursor c = myDatabase.query("mStuffdetails", null,
                            "mstuffid='" + mstuffid[selectedposition] + "'",
                            null, null, null, null);

                    int idcolumn = c.getColumnIndexOrThrow("mstuffid");
                    int categorycolumn = c.getColumnIndexOrThrow("catagory");
                    int detailsColumn = c.getColumnIndexOrThrow("details");
                    int latitudeColumn = c.getColumnIndexOrThrow("latitude");
                    int longitudeColumn = c.getColumnIndexOrThrow("longitude");
                    int locationColumn = c.getColumnIndexOrThrow("location");
                    if (c != null)
                    {
                        if (c.isFirst())
                        {
                            String mstuffidForFavorite = c.getString(idcolumn);
                            String categoryForFavorite =
                                    c.getString(categorycolumn);
                            String detailsForFavorite =
                                    c.getString(detailsColumn);
                            int latitudeForFavorite = c.getInt(latitudeColumn);
                            int longitudeForFavorite =
                                    c.getInt(longitudeColumn);
                            String locationForFavorite =
                                    c.getString(locationColumn);
                            myDatabase.execSQL("INSERT INTO " + "Favourite" +
                                    "  (mstuffid, catagory,  details, latitude, longitude, location)" +
                                    " VALUES ('" + mstuffidForFavorite +
                                    "', '" + categoryForFavorite + "', '" +
                                    detailsForFavorite + "'," +
                                    latitudeForFavorite + "," +
                                    longitudeForFavorite + ",'" +
                                    locationForFavorite + "');");
                        }
                    }
                    c.close();
                }
                catch (Exception e)
                {
                    Toast.makeText(MStuffTextView.this, "Database Not Found...",
                            Toast.LENGTH_LONG).show();
                }
                finally
                {
                    if (myDatabase != null)
                    {
                        myDatabase.close();
                    }
                }

                break;
            case 5:
                try
                {
                    myDatabase =
                            MStuffTextView.this.openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    Cursor c = myDatabase.query("mStuffdetails", null,
                            "mstuffid='" + mstuffid[selectedposition] + "'",
                            null, null, null, null);
                    if (c.getCount() > 0)
                    {
                        myDatabase = MStuffTextView.this
                                .openOrCreateDatabase("Mobeegal",
                                        Context.MODE_PRIVATE, null);
                        myDatabase.delete("mStuffdetails",
                                "mStuffId='" + mstuffid[selectedposition] + "'",
                                null);
                        Toast.makeText(MStuffTextView.this,
                                "Ignored the selected match from your mStuff",
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MStuffTextView.this,
                                MStuffTextView.class));
                        finish();
                    }
                    else
                    {
                        Toast.makeText(MStuffTextView.this,
                                "Sorry, no matches to Ignore",
                                Toast.LENGTH_SHORT).show();
                    }
                    c.close();
                }
                catch (Exception ex)
                {
                    Toast.makeText(MStuffTextView.this, "Database Not Found...",
                            Toast.LENGTH_LONG).show();
                }
                finally
                {
                    if (myDatabase != null)
                    {
                        myDatabase.close();
                    }
                }
                break;
            case 6:
                myDatabase.execSQL("update preferences set views='MapView'");
                Intent mStuffTextView =
                        new Intent(MStuffTextView.this, MapResults.class);
                startActivity(mStuffTextView);
                break;
            case 7:

                Intent mStuffsearch =
                        new Intent(MStuffTextView.this, MstuffSearch.class);
                Bundle searchBundle = new Bundle();
                searchBundle.putString("TextView", "TextView");
                mStuffsearch.putExtras(searchBundle);
                startActivityForResult(mStuffsearch, 0);
                break;
            case 8:
                Intent mStuffchat = new Intent(MStuffTextView.this, Chat.class);
                Bundle chat = new Bundle();
                chat.putString("mstuffid", mstuffid[selectedposition]);
                mStuffchat.putExtras(chat);
                startActivityForResult(mStuffchat, 0);
                break;
            case 9:
                Intent mediaintent =
                        new Intent(MStuffTextView.this, ViewMedia.class);
                startActivity(mediaintent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
