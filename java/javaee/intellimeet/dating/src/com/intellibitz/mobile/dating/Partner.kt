package com.intellibitz.mobile.dating

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

class Partner : Activity() {

    /** Called when the activity is first created. */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        setContentView(R.layout.partner)

        val s1 = findViewById(R.id.agespinner1) as Spinner
        var adapter = ArrayAdapter.createFromResource(
            this, R.array.age, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s1.adapter = adapter

        val s2 = findViewById(R.id.heightspinner2) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.height,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s2.adapter = adapter

        val s3 = findViewById(R.id.weightspinner3) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.weight,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s3.adapter = adapter

        val s4 = findViewById(R.id.locnspinner4) as Spinner
        adapter = ArrayAdapter.createFromResource(
            this, R.array.location,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        s4.adapter = adapter

        val sendbutton = findViewById(R.id.send) as Button?
        if (sendbutton != null) {
            sendbutton.setOnClickListener {
                val intobj = Intent(this@Partner, DatingServiceController::class.java)
                startActivity(intobj)
            }
        }
        val backbutton = findViewById(R.id.back) as Button?
        if (backbutton != null) {
            backbutton.setOnClickListener {
                val intobj = Intent(this@Partner, Seeker::class.java)
                startActivity(intobj)
            }
        }
    }
}
