package com.mobeegal.android.activity.catalogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.mobeegal.android.R;

public class SettingTheme
        extends Activity
{
    SQLiteDatabase myDatabase = null;
    Spinner catalog;
    Spinner theme;
    String catalogs;
    int selectedtheme;
    Intent myIntent;
    String ch;
    int ch1;
    String str1;
    String str;
    String passingclass;

    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        this.setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.theme);
        theme = (Spinner) findViewById(R.id.theme);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
                this, R.array.selecttheme,
                android.R.layout.simple_spinner_item);
        theme.setAdapter(adapter1);


        try
        {
            myDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Theme" +
                    " (catalog VARCHAR,theme NUMERIC );");
        }
        catch (Exception e)
        {
            Toast.makeText(SettingTheme.this, "Not able to open database",
                    Toast.LENGTH_SHORT).show();

        }

        Bundle bundle1 = this.getIntent().getExtras();
        if (bundle1 != null)
        {
            // str1 = bundle1.getString("value1");
            passingclass = bundle1.getString("value1");
            ch = bundle1.getString("class");
            ch1 = Integer.parseInt(ch);

        }
        switch (ch1)
        {

            case 1:
                myIntent = new Intent(SettingTheme.this, Jewelry.class);
                break;
            case 2:
                myIntent = new Intent(SettingTheme.this, Cars.class);
                break;
            case 3:
                myIntent = new Intent(SettingTheme.this, Dating.class);

                break;
            case 4:
                myIntent = new Intent(SettingTheme.this, Matrimony.class);
                break;
            case 5:
                myIntent = new Intent(SettingTheme.this, Restaurants.class);
                break;
            case 6:
                myIntent = new Intent(SettingTheme.this, Movies.class);
                break;
            case 7:
                myIntent = new Intent(SettingTheme.this, Home.class);
                break;
            default:
                break;

        }


        Button ichoose = (Button) findViewById(R.id.Set);
        ichoose.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {
                selectedtheme = theme.getSelectedItemPosition();
                str = Integer.toString(selectedtheme);
                myDatabase.execSQL(
                        "INSERT INTO Theme (catalog,theme) VALUES ('" +
                                passingclass + "'," + selectedtheme + ");");
                startActivity(myIntent);
                finish();

            }
        }
        );
    }
}
