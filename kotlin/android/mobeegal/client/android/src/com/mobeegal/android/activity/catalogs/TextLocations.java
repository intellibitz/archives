package com.mobeegal.android.activity.catalogs;

//import java.util.logging.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.mobeegal.android.R;

public class TextLocations
        extends Activity
{

    SQLiteDatabase myDB = null;

    String[] res1 = new String[50];
    String[] res3 = new String[50];

    private String tableName;
    private int getkey;
    private String getcategory;
    private String getstufftype;
    //  private static Logger logger = Logger.getLogger("TextLocations");

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.textlocation);
        Bundle b = this.getIntent().getExtras();
        if (b != null)
        {
            tableName = b.getString("tablename");
            getkey = b.getInt("key");
        }

        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String[] columnname = {"category", "stufftype"};
            Cursor cursor = myDB.query(tableName, columnname, null, null, null,
                    null, null);

            if (cursor != null)
            {
                if (cursor.isFirst())
                {
                    getcategory =
                            cursor.getString(
                                    cursor.getColumnIndexOrThrow("category"));
                    getstufftype = cursor.getString(
                            cursor.getColumnIndexOrThrow("stufftype"));
                }
            }
        }
        catch (Exception e)
        {

        }

        // storing values
        /*  if (getstufftype.equals("istuff")) {
            myDatabase.execSQL("UPDATE " + tableName + " set iarea='" + area + "', icountry='" + country + "', icity='" + city + "';");
        } else if (getstufftype.equals("ustuff")) {
            myDatabase.execSQL("UPDATE " + tableName + " set uarea='" + area + "', ucountry='" + country + "', ucity='" + city + "';");
        }*/

//  storing values       
        Button save = (Button) findViewById(R.id.savinglocation);
        save.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {
                if (getcategory.equals("Dating"))
                {
                    Intent locationfinder =
                            new Intent(TextLocations.this, Dating.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Matrimony"))
                {
                    Intent locationfinder =
                            new Intent(TextLocations.this, Matrimony.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Cars"))
                {
                    Intent locationfinder =
                            new Intent(TextLocations.this, Cars.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Rental"))
                {
                    Intent locationfinder =
                            new Intent(TextLocations.this, Home.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Restaurants"))
                {
                    Intent locationfinder =
                            new Intent(TextLocations.this, Restaurants.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Movies"))
                {
                    Intent locationfinder =
                            new Intent(TextLocations.this, Movies.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
                else if (getcategory.equals("Jewelry"))
                {
                    Intent locationfinder =
                            new Intent(TextLocations.this, Jewelry.class);
                    Bundle passkey = new Bundle();
                    passkey.putInt("key", getkey);
                    locationfinder.putExtras(passkey);
                    startActivityForResult(locationfinder, 0);
                    finish();
                }
            }
        });

    }

}