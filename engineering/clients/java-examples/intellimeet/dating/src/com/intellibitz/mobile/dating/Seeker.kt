package com.intellibitz.mobile.dating

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TabHost
import android.widget.TextView
import android.widget.Toast
import java.io.FileNotFoundException

/**
 *
 * @author jyothsna
 */
class Seeker : Activity() {

    private var d: Dating? = null
    /** Called when the activity is first created. */
    var count = 0
    var myDatabase: SQLiteDatabase? = null
    var getSeekerAge: String? = null
    var getSeekerSex: String? = null
    var getSeekerHeight: String? = null
    var getSeekerWeight: String? = null
    var getSeekerLocation: String? = null
    var getPartnerAge: String? = null
    var getPartnerSex: String? = null
    var getPartnerHeight: String? = null
    var getPartnerWeight: String? = null
    var getPartnerLocation: String? = null
    var ageTextView: TextView? = null
    var heightTextView: TextView? = null
    var weightTextView: TextView? = null
    var locationTextView: TextView? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // ToDo add your GUI initialization code here
        setContentView(R.layout.main)
        val tabs = findViewById(R.id.tabs) as TabHost
        tabs.setup()
        val one = tabs.newTabSpec("one")
        one.setContent(R.id.yourProfile)
        try {
            this.createDatabase("Dating", 1, MODE_PRIVATE, null)
            myDatabase = this.openDatabase("Dating", null)
        } catch (e: FileNotFoundException) {
        }
        val seekerMale = findViewById(R.id.seekermaleradiobutton) as RadioButton
        seekerMale.isChecked = true
        one.setIndicator("Your's", this.resources.getDrawable(R.drawable.yourprofile))
        val COUNTRIES = arrayOf(
            "Uttar Pradesh", "Maharashtra", "Bihar", "West Bengal", "Andhra Pradesh", "Tamil Nadu", "Madhya Pradesh",
            "Rajasthan", "Karnataka", "Gujarat", "Orissa", "Kerala", "Jharkhand", "Assam", "Punjab", "Haryana", "Chhattisgarh",
            "Delhi", "Jammu and Kashmir", "Uttarakhand ", "Himachal Pradesh", "Tripura", "Manipur", "Meghalaya", "Nagaland",
            "Goa", "Arunachal Pradesh", "Pondicherry", "Chandigarh", "Mizoram", "Sikkim", "Andaman and Nicobar Islands",
            "Dadra and Nagar Haveli", "Daman and Diu", "Lakshadweep"
        )
        val textView = findViewById(R.id.Location) as AutoCompleteTextView
        val adapter1 = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, COUNTRIES
        )
        textView.setAdapter(adapter1)
        tabs.addTab(one)
        val two = tabs.newTabSpec("two")
        two.setContent(R.id.partner)
        two.setIndicator("Partner", this.resources.getDrawable(R.drawable.partnerprofile))
        val s1 = findViewById(R.id.agespinner1) as Spinner
        var adapter = ArrayAdapter.createFromResource(
            this, R.array.age, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s1.adapter = adapter
        s1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                getPartnerAge = s1.selectedItem as String
                if (getPartnerAge == "Select Age") {
                    getPartnerAge = "null"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val s2 = findViewById(R.id.heightspinner2) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.height,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s2.adapter = adapter
        s2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                getPartnerHeight = s2.selectedItem as String
                if (getPartnerHeight == "Select Height") {
                    getPartnerHeight = "null"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val s3 = findViewById(R.id.weightspinner3) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.weight,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s3.adapter = adapter
        s3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                getPartnerWeight = s3.selectedItem as String
                if (getPartnerWeight == "Select Weight") {
                    getPartnerWeight = "null"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val s4 = findViewById(R.id.locnspinner4) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.location,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s4.adapter = adapter
        s4.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                getPartnerLocation = s4.selectedItem as String
                if (getPartnerLocation == "Select Location") {
                    getPartnerLocation = "null"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        val partnerSex = findViewById(R.id.partnerfemaleradiobutton) as RadioButton
        partnerSex.isChecked = true
        if (partnerSex.isChecked == true) {
            getPartnerSex = "female"
        } else {
            getPartnerSex = "male"
        }

        tabs.addTab(two)

        val button = findViewById(R.id.Activate) as Button
        button.setOnClickListener(object : Button.OnClickListener {
            var mToast: Toast? = null

            override fun onClick(v: View) {
                if (v.id == R.id.Activate) {
                    if (count == 0) {
                        ageTextView = findViewById(R.id.Age) as TextView
                        heightTextView = findViewById(R.id.Height) as TextView
                        weightTextView = findViewById(R.id.Weight) as TextView
                        locationTextView = findViewById(R.id.Location) as TextView
                        val seekerFemale = findViewById(R.id.seekerfemaleradiobutton) as RadioButton
                        if (seekerFemale.isChecked == true) {
                            getSeekerSex = "female"
                        } else {
                            getSeekerSex = "male"
                        }
                        getSeekerAge = ageTextView!!.text.toString()
                        getSeekerHeight = heightTextView!!.text.toString()
                        getSeekerWeight = weightTextView!!.text.toString()
                        getSeekerLocation = locationTextView!!.text.toString()
                        if (getSeekerAge == "") {
                            getSeekerAge = "0"
                        }
                        if (getSeekerHeight == "") {
                            getSeekerHeight = "0"
                        }
                        if (getSeekerWeight == "") {
                            getSeekerWeight = "0"
                        }
                        if (getSeekerLocation == "") {
                            getSeekerLocation = "null"
                        }
                        myDatabase!!.execSQL(
                            "CREATE TABLE IF NOT EXISTS Seeker" + " (seekerAge INT(3), seekerSex VARCHAR, seekerHeight INT(3), " +
                                "seekerWeight INT(3), seekerLocation VARCHAR);"
                        )
                        myDatabase!!.execSQL(
                            "INSERT INTO Seeker (seekerAge, seekerSex, seekerHeight, seekerWeight, " +
                                "seekerLocation) VALUES (" + getSeekerAge + ",'" + getSeekerSex + "'," + getSeekerHeight + "," + getSeekerWeight + ",'" + getSeekerLocation + "');"
                        )
                        myDatabase!!.execSQL(
                            "CREATE TABLE IF NOT EXISTS Partner" + " (partnerAgeRange VARCHAR, partnerSex VARCHAR, partnerHeightRange VARCHAR," +
                                "partnerWeightRange VARCHAR, partnerLocation VARCHAR);"
                        )

                        myDatabase!!.execSQL(
                            "INSERT INTO Partner (partnerAgeRange, partnerSex, partnerHeightRange," +
                                "partnerWeightRange, partnerLocation) VALUES ('" + getPartnerAge + "','" + getPartnerSex +
                                "','" + getPartnerHeight + "','" + getPartnerWeight + "','" + getPartnerLocation + "');"
                        )

                        count++

                        val intobject = Intent(this@Seeker, Dating::class.java)
                        startActivity(intobject)
                        // Tell the user about what we did.
                        if (mToast != null) {
                            mToast!!.cancel()
                        }
                        mToast = Toast.makeText(
                            this@Seeker, R.string.repeating_received,
                            Toast.LENGTH_LONG
                        )
                        mToast!!.show()
                    }
                } else {
                    Toast.makeText(this@Seeker, "Service already activated", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        })

        tabs.currentTab = 0
    }
}
