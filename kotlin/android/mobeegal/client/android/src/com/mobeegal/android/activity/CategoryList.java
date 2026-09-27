package com.mobeegal.android.activity;

/*
<!--
$Id:: CategoryList.java 14 2008-08-19 06:36:45Z muthu.ramadoss                  $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity;
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
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.Toast;
import com.mobeegal.android.R;
import com.mobeegal.android.activity.catalogs.CarsViewQuery;
import com.mobeegal.android.activity.catalogs.HomeViewQuery;
import com.mobeegal.android.activity.catalogs.Jewelryviewquery;
import com.mobeegal.android.activity.catalogs.Matrimonyviewquery;
import com.mobeegal.android.activity.catalogs.MoviesViewQuery;
import com.mobeegal.android.activity.catalogs.RestaurantsViewQuery;
import com.mobeegal.android.activity.catalogs.ViewQuery;
import com.mobeegal.android.util.ViewMenu;

import java.util.logging.Logger;

public class CategoryList
        extends Activity
{

    SQLiteDatabase myDatabase = null;
    String categoryname1;
    int b = 0, n, d = 0;
    int categoryname, getcountcatname, getcountcatname1, getcatalogID;
    int size;
    Cursor c2, c3;
    int position;
    String[] res1 = new String[50];
    String[] res3 = new String[50];
    private static Logger logger = Logger.getLogger("categorylist");

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.categorylist);
        Bundle categoryBundle = getIntent().getExtras();
        if (categoryBundle != null)
        {
            position = categoryBundle.getInt("position");
        }
        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String[] column = {"modes"};
            c2 = myDatabase.query("category", column, "status='true'",
                    null, null, null, "modes");
            categoryname = c2.getColumnIndexOrThrow("modes");
            getcountcatname = c2.getCount();
            logger.info("Modes :" + getcountcatname);
            if (c2 != null)
            {
                b = 0;
                if (c2.isFirst())
                {
                    do
                    {
                        categoryname1 = c2.getString(categoryname);
                        logger.info("Modes :" + categoryname1);
                        res1[b] = categoryname1;
                        b++;
                    }
                    while (c2.moveToNext());
                }
            }
            String[] listcat = {"categoryname", "categoryID"};
//for(int j) {
            c3 = myDatabase.query("category", listcat,
                    "status='true' and categoryID=" + position, null, null,
                    null, null);

            categoryname = c3.getColumnIndexOrThrow("categoryname");
            getcatalogID = c3.getColumnIndexOrThrow("categoryID");
            getcountcatname1 = c3.getCount();
            if (c3 != null)
            {
                d = 0;
                if (c3.isFirst())
                {
                    do
                    {
                        logger.info("position :" + position);
                        if (position == c3.getInt(getcatalogID))
                        {
                            size++;
                        }
                        categoryname1 = c3.getString(categoryname);
                        logger.info("catalog name :" + categoryname1);
                        res3[d] = categoryname1;
                        d++;
                    }
                    while (c3.moveToNext());
                }
            }
            //       }
            GridView g = (GridView) findViewById(R.id.myGrid1);
            if (c3.getCount() == 0)
            {
                Toast.makeText(CategoryList.this,
                        "Sorry, No category subscribed", Toast.LENGTH_LONG)
                        .show();
            }
            else
            {
                g.setAdapter(new ImageAdapter(this));
            }
            c3.close();
            c2.close();
        }
        catch (Exception e)
        {
            Toast.makeText(CategoryList.this, "" + e, Toast.LENGTH_LONG).show();
        }
    }

    public class ImageAdapter
            extends BaseAdapter
    {

        public ImageAdapter(Context c)
        {
            mContext = c;
        }

        public int getCount()
        {
            return size;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(final int position, View convertView,
                ViewGroup parent)
        {

            for (n = size; n > 0; n--)
            {

                Button categorybutton = new Button(mContext);
                categorybutton
                        .setLayoutParams(new Gallery.LayoutParams(110, 50));
                categorybutton.setPadding(10, 10, 11, 13);
                categorybutton.setText(res3[position]);

                categorybutton.setOnClickListener(new Button.OnClickListener()
                {

                    public void onClick(View view)
                    {

                        try
                        {
                            if (res3[position].equalsIgnoreCase("Dating"))
                            {
                                Intent category1 =
                                        new Intent(mContext, ViewQuery.class);
                                startActivityForResult(category1, 0);
                                // finish();
                            }
                            else
                            if (res3[position].equalsIgnoreCase("Matrimony"))
                            {
                                Intent category2 = new Intent(mContext,
                                        Matrimonyviewquery.class);
                                startActivityForResult(category2, 0);
                                // finish();
                            }
                            else if (res3[position].equalsIgnoreCase("Jewelry"))
                            {
                                Intent category3 = new Intent(mContext,
                                        Jewelryviewquery.class);
                                startActivityForResult(category3, 0);
                                //finish();
                            }
                            else
                            if (res3[position].equalsIgnoreCase("Restaurants"))
                            {
                                Intent category3 = new Intent(mContext,
                                        RestaurantsViewQuery.class);
                                startActivityForResult(category3, 0);
                                //finish();
                            }
                            else if (res3[position].equalsIgnoreCase("Movies"))
                            {
                                Intent category3 = new Intent(mContext,
                                        MoviesViewQuery.class);
                                startActivityForResult(category3, 0);

                            }
                            else if (res3[position].equalsIgnoreCase("Rental"))
                            {
                                Intent category3 = new Intent(mContext,
                                        HomeViewQuery.class);
                                startActivity(category3);
                                //finish();
                            }
                            else if (res3[position].equalsIgnoreCase("Cars"))
                            {
                                Intent category3 = new Intent(mContext,
                                        CarsViewQuery.class);
                                startActivityForResult(category3, 0);
                                //finish();
                            }
                            else
                            {
                                Toast.makeText(CategoryList.this,
                                        "No Category Profile Found",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(CategoryList.this, "" + e,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
                return categorybutton;
            }
            return parent;
        }

        private Context mContext;
    }

    // MenuView
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
                Intent stuffCheckintent = new Intent(CategoryList.this,
                        MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                finish();
                break;
            case 2:
                Intent intent1 = new Intent(CategoryList.this,
                        FindandInstall.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 3:
                Intent settings = new Intent(CategoryList.this, Settings.class);
                startActivityForResult(settings, 0);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
