package com.mobeegal.android.activity.catalogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.mobeegal.android.R

class SettingTheme : Activity() {
    var myDatabase: SQLiteDatabase? = null
    var catalog: Spinner? = null
    var theme: Spinner? = null
    var catalogs: String? = null
    var selectedtheme: Int = 0
    var myIntent: Intent? = null
    var ch: String? = null
    var ch1: Int = 0
    var str1: String? = null
    var str: String? = null
    var passingclass: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        this.setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.theme)
        theme = findViewById(R.id.theme) as Spinner
        val adapter1 = ArrayAdapter.createFromResource(
            this, R.array.selecttheme,
            android.R.layout.simple_spinner_item
        )
        theme!!.adapter = adapter1

        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            myDatabase!!.execSQL(
                "CREATE TABLE IF NOT EXISTS Theme" +
                    " (catalog VARCHAR,theme NUMERIC );"
            )
        } catch (e: Exception) {
            Toast.makeText(
                this@SettingTheme, "Not able to open database",
                Toast.LENGTH_SHORT
            ).show()
        }

        val bundle1 = this.intent.extras
        if (bundle1 != null) {
            // str1 = bundle1.getString("value1");
            passingclass = bundle1.getString("value1")
            ch = bundle1.getString("class")
            ch1 = Integer.parseInt(ch)
        }
        when (ch1) {
            1 -> myIntent = Intent(this@SettingTheme, Jewelry::class.java)
            2 -> myIntent = Intent(this@SettingTheme, Cars::class.java)
            3 -> myIntent = Intent(this@SettingTheme, Dating::class.java)
            4 -> myIntent = Intent(this@SettingTheme, Matrimony::class.java)
            5 -> myIntent = Intent(this@SettingTheme, Restaurants::class.java)
            6 -> myIntent = Intent(this@SettingTheme, Movies::class.java)
            7 -> myIntent = Intent(this@SettingTheme, Home::class.java)
            else -> {
            }
        }

        val ichoose = findViewById(R.id.Set) as Button
        ichoose.setOnClickListener {
            selectedtheme = theme!!.selectedItemPosition
            str = Integer.toString(selectedtheme)
            myDatabase!!.execSQL(
                "INSERT INTO Theme (catalog,theme) VALUES ('" +
                    passingclass + "'," + selectedtheme + ");"
            )
            startActivity(myIntent)
            finish()
        }
    }
}
