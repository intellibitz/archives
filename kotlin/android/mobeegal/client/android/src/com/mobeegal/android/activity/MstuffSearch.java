/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.mobeegal.android.R;

/**
 * @author mobeegal.in
 */
public class MstuffSearch
        extends Activity
{

    private String selectedcatalogs;
    private String searchItemString;
    private String checkTextview;
    private Bundle bundleForTextview;
    EditText searchItem;
    SQLiteDatabase mobeegalDatabase = null;
    Cursor c;
    int rows;
    int count;
    String[] subscribedCategory;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.mstuffsearch);
        final Spinner catalogs = (Spinner) findViewById(R.id.filters);
        searchItem = (EditText) findViewById(R.id.searchMstuff);
        bundleForTextview = getIntent().getExtras();
        if (bundleForTextview != null)
        {
            checkTextview = bundleForTextview.getString("TextView");
        }

        try
        {
            mobeegalDatabase = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
            String myCols[] = {"categoryname"};
            c = mobeegalDatabase.query("category", myCols, "status='true'",
                    null, null, null, null);
            rows = c.getCount();
            subscribedCategory = new String[rows];
            int categorycolumn = c.getColumnIndexOrThrow("categoryname");
            if (c != null)
            {
                if (c.isFirst())
                {
                    count = 0;
                    do
                    {
                        subscribedCategory[count] = c.getString(categorycolumn);
                        count++;
                    }
                    while (c.moveToNext());
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, subscribedCategory);
        catalogs.setAdapter(adapter);
        catalogs.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener()
                {

                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        selectedcatalogs = (String) catalogs.getSelectedItem();
                    }

                    public void onNothingSelected(AdapterView parent)
                    {
                    }
                });
        Button submitSearch = (Button) findViewById(R.id.searchButton);
        submitSearch.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {
                searchItemString = searchItem.getText().toString();
                if (checkTextview != null)
                {
                    Intent intent = new Intent(MstuffSearch.this,
                            MstuffTextSearchResults.class);
                    Bundle b = new Bundle();
                    b.putString("edittext", searchItemString);
                    b.putString("spinner", selectedcatalogs);
                    intent.putExtras(b);
                    startActivityForResult(intent, 0);
                }
                else
                {
                    Intent intent = new Intent(MstuffSearch.this,
                            MstuffSearchResults.class);
                    Bundle b = new Bundle();
                    b.putString("edittext", searchItemString);
                    b.putString("spinner", selectedcatalogs);
                    intent.putExtras(b);
                    startActivityForResult(intent, 0);
                }
            }
        });
    }
}
