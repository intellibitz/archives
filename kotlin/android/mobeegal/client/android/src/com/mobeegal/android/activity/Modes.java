/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import com.mobeegal.android.R;

/**
 * @author work
 */
public class Modes
        extends Activity
{

    private int requestno;
    private int id;
    private String mode = "Basic";
    private RadioButton premium;
    private RadioButton standard;
    private RadioButton basic;
    private String requestcatalog;
    SQLiteDatabase myDB = null;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.modes);

        try
        {
            myDB = this.openOrCreateDatabase("Mobeegal",
                    Context.MODE_PRIVATE, null);
        }
        catch (Exception e)
        {

        }

        basic = (RadioButton) findViewById(R.id.basic);
        basic.setChecked(true);
        basic.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                mode = "Basic";
                id = 0;
            }
        });
        standard = (RadioButton) findViewById(R.id.standard);
        standard.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                mode = "Standard";
                id = 2;
            }
        });
        premium = (RadioButton) findViewById(R.id.premium);
        premium.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View arg0)
            {
                mode = "Premium";
                id = 1;
            }
        });

        Bundle B2 = this.getIntent().getExtras();
        if (B2 != null)
        {
            requestcatalog = B2.getString("requestcatalog");
            requestno = B2.getInt("requestno");
        }

        Button modeok = (Button) findViewById(R.id.modeyes);
        modeok.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View view)
            {
                myDB.execSQL("update " + requestcatalog +
                        "_category set modes='" + mode + "' where categoryID=" +
                        requestno);
                myDB.execSQL("update category set modes='" + mode +
                        "' , categoryID=" + id + " where catalogtype='" +
                        requestcatalog + "'and categoryID=" + requestno);
                // Toast.makeText(Modes.this, "Bundle passed : " + requestcatalog + id, Toast.LENGTH_SHORT).show();
                Bundle B3 = new Bundle();
                B3.putString("requestcatalog", requestcatalog);
                Intent modes = new Intent(Modes.this, Subscribe.class);
                modes.putExtras(B3);
                startActivityForResult(modes, 0);
                finish();
            }
        });

        Button modeno = (Button) findViewById(R.id.modeno);
        modeno.setOnClickListener(new Button.OnClickListener()
        {

            public void onClick(View view)
            {
                myDB.execSQL("update " + requestcatalog +
                        "_category set status='false' where categoryID=" +
                        requestno);
                myDB.execSQL(
                        "update category set status='false' where catalogtype='" +
                                requestcatalog + "'and categoryID=" +
                                requestno);
                Bundle B3 = new Bundle();
                B3.putString("requestcatalog", requestcatalog);
                Intent modes = new Intent(Modes.this, Subscribe.class);
                modes.putExtras(B3);
                startActivityForResult(modes, 0);
                finish();
            }
        });


    }
}
