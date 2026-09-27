
package com.intellibitz.mobile.dating;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.content.Intent;


public class Partner extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.partner);

        Spinner s1 = (Spinner) findViewById(R.id.agespinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.age, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);

        Spinner s2 = (Spinner) findViewById(R.id.heightspinner2);
        adapter = ArrayAdapter.createFromResource(this, R.array.height,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s2.setAdapter(adapter);

        Spinner s3 = (Spinner) findViewById(R.id.weightspinner3);
        adapter = ArrayAdapter.createFromResource(this, R.array.weight,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s3.setAdapter(adapter);

        Spinner s4 = (Spinner) findViewById(R.id.locnspinner4);
        adapter = ArrayAdapter.createFromResource(this, R.array.location,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s4.setAdapter(adapter);

        Button sendbutton = (Button) findViewById(R.id.send);
        if(sendbutton!=null)
        {
        sendbutton.setOnClickListener(new Button.OnClickListener(){
        public void onClick(View V)
        	{
             Intent intobj = new Intent(Partner.this,DatingServiceController.class);
             startActivity(intobj);
            }
        });
        }
        Button backbutton = (Button) findViewById(R.id.back);
        if(backbutton!=null)
        {
        backbutton.setOnClickListener(new Button.OnClickListener(){
        public void onClick(View V)
        	{
             Intent intobj = new Intent(Partner.this,Seeker.class);
             startActivity(intobj);
            }
        });
        }
      }
    }


