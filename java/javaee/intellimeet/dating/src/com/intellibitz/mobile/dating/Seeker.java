package com.intellibitz.mobile.dating;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileNotFoundException;

/**
 *
 * @author jyothsna
 */
public class Seeker extends Activity {

    private Dating d;
    /** Called when the activity is first created. */
    int count = 0;
    SQLiteDatabase myDatabase = null;
    String getSeekerAge;
    String getSeekerSex;
    String getSeekerHeight;
    String getSeekerWeight;
    String getSeekerLocation;
    String getPartnerAge;
    String getPartnerSex;
    String getPartnerHeight;
    String getPartnerWeight;
    String getPartnerLocation;
    TextView ageTextView;
    TextView heightTextView;
    TextView weightTextView;
    TextView locationTextView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // ToDo add your GUI initialization code here 
        setContentView(R.layout.main);
        final TabHost tabs = (TabHost) this.findViewById(R.id.tabs);
        tabs.setup();
        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.yourProfile);
        try {
            this.createDatabase("Dating", 1, MODE_PRIVATE, null);
            myDatabase = this.openDatabase("Dating", null);
        } catch (FileNotFoundException e) {
        }
        RadioButton seekerMale = (RadioButton) findViewById(R.id.seekermaleradiobutton);
        seekerMale.setChecked(true);
        one.setIndicator("Your's", this.getResources().getDrawable(R.drawable.yourprofile));
        String[] COUNTRIES = new String[]{
            "Uttar Pradesh", "Maharashtra", "Bihar", "West Bengal", "Andhra Pradesh", "Tamil Nadu", "Madhya Pradesh",
            "Rajasthan", "Karnataka", "Gujarat", "Orissa", "Kerala", "Jharkhand", "Assam", "Punjab", "Haryana", "Chhattisgarh",
            "Delhi", "Jammu and Kashmir", "Uttarakhand ", "Himachal Pradesh", "Tripura", "Manipur", "Meghalaya", "Nagaland",
            "Goa", "Arunachal Pradesh", "Pondicherry", "Chandigarh", "Mizoram", "Sikkim", "Andaman and Nicobar Islands",
            "Dadra and Nagar Haveli", "Daman and Diu", "Lakshadweep"
        };
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.Location);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, COUNTRIES);
        textView.setAdapter(adapter1);
        tabs.addTab(one);
        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.partner);
        two.setIndicator("Partner", this.getResources().getDrawable(R.drawable.partnerprofile));
        final Spinner s1 = (Spinner) findViewById(R.id.agespinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.age, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView parent, View v, int position, long id) {
                getPartnerAge = (String) s1.getSelectedItem();
                if (getPartnerAge.equals("Select Age")) {
                    getPartnerAge = "null";
                }
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });
        final Spinner s2 = (Spinner) findViewById(R.id.heightspinner2);
        adapter = ArrayAdapter.createFromResource(this, R.array.height,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s2.setAdapter(adapter);
        s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView parent, View v, int position, long id) {
                getPartnerHeight = (String) s2.getSelectedItem();
                if (getPartnerHeight.equals("Select Height")) {
                    getPartnerHeight = "null";
                }
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });

        final Spinner s3 = (Spinner) findViewById(R.id.weightspinner3);
        adapter = ArrayAdapter.createFromResource(this, R.array.weight,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s3.setAdapter(adapter);
        s3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView parent, View v, int position, long id) {
                getPartnerWeight = (String) s3.getSelectedItem();
                if (getPartnerWeight.equals("Select Weight")) {
                    getPartnerWeight = "null";
                }
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });

        final Spinner s4 = (Spinner) findViewById(R.id.locnspinner4);
        adapter = ArrayAdapter.createFromResource(this, R.array.location,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s4.setAdapter(adapter);
        s4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView parent, View v, int position, long id) {
                getPartnerLocation = (String) s4.getSelectedItem();
                if (getPartnerLocation.equals("Select Location")) {
                    getPartnerLocation = "null";
                }
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });
        RadioButton partnerSex = (RadioButton) findViewById(R.id.partnerfemaleradiobutton);
        partnerSex.setChecked(true);
        if (partnerSex.isChecked() == true) {
            getPartnerSex = "female";
        } else {
            getPartnerSex = "male";
        }

        tabs.addTab(two);

        final Button button = (Button) findViewById(R.id.Activate);
        button.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {
                if (v.getId() == (R.id.Activate)) {
                    if (count == 0) {
                        ageTextView = (TextView) findViewById(R.id.Age);
                        heightTextView = (TextView) findViewById(R.id.Height);
                        weightTextView = (TextView) findViewById(R.id.Weight);
                        locationTextView = (TextView) findViewById(R.id.Location);
                        RadioButton seekerFemale = (RadioButton) findViewById(R.id.seekerfemaleradiobutton);
                        if (seekerFemale.isChecked() == true) {
                            getSeekerSex = "female";
                        } else {
                            getSeekerSex = "male";
                        }
                        getSeekerAge = ageTextView.getText().toString();
                        getSeekerHeight = heightTextView.getText().toString();
                        getSeekerWeight = weightTextView.getText().toString();
                        getSeekerLocation = locationTextView.getText().toString();
                        if (getSeekerAge.equals("")) {
                            getSeekerAge = "0";
                        }
                        if (getSeekerHeight.equals("")) {
                            getSeekerHeight = "0";
                        }
                        if (getSeekerWeight.equals("")) {
                            getSeekerWeight = "0";
                        }
                        if (getSeekerLocation.equals("")) {
                            getSeekerLocation = "null";
                        }
                        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Seeker" + " (seekerAge INT(3), seekerSex VARCHAR, seekerHeight INT(3), " +
                                "seekerWeight INT(3), seekerLocation VARCHAR);");
                        myDatabase.execSQL("INSERT INTO Seeker (seekerAge, seekerSex, seekerHeight, seekerWeight, " +
                                "seekerLocation) VALUES (" + getSeekerAge + ",'" + getSeekerSex + "'," + getSeekerHeight + "," + getSeekerWeight + ",'" + getSeekerLocation + "');");
                        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Partner" + " (partnerAgeRange VARCHAR, partnerSex VARCHAR, partnerHeightRange VARCHAR," +
                                "partnerWeightRange VARCHAR, partnerLocation VARCHAR);");

                        myDatabase.execSQL("INSERT INTO Partner (partnerAgeRange, partnerSex, partnerHeightRange," +
                                "partnerWeightRange, partnerLocation) VALUES ('" + getPartnerAge + "','" + getPartnerSex +
                                "','" + getPartnerHeight + "','" + getPartnerWeight + "','" + getPartnerLocation + "');");

                        count++;


                        Intent intobject = new Intent(Seeker.this, Dating.class);
                        startActivity(intobject);
                        // Tell the user about what we did.
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(Seeker.this, R.string.repeating_received,
                                Toast.LENGTH_LONG);
                        mToast.show();
                    }
                } else {
                    Toast.makeText(Seeker.this, "Service already activated", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
            Toast mToast;
        });

        tabs.setCurrentTab(0);
    }
} 

  


