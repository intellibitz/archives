/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.mobeegal.android.R

/**
 * @author mobeegal.in
 */
class MstuffSearch : Activity() {

    private var selectedcatalogs: String? = null
    private var searchItemString: String? = null
    private var checkTextview: String? = null
    private var bundleForTextview: Bundle? = null
    var searchItem: EditText? = null
    var mobeegalDatabase: SQLiteDatabase? = null
    var c: Cursor? = null
    var rows: Int = 0
    var count: Int = 0
    var subscribedCategory: Array<String?>? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.mstuffsearch)
        val catalogs = findViewById(R.id.filters) as Spinner
        searchItem = findViewById(R.id.searchMstuff) as EditText
        bundleForTextview = intent.extras
        if (bundleForTextview != null) {
            checkTextview = bundleForTextview!!.getString("TextView")
        }

        try {
            mobeegalDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val myCols = arrayOf("categoryname")
            c = mobeegalDatabase!!.query(
                "category", myCols, "status='true'",
                null, null, null, null
            )
            rows = c!!.count
            subscribedCategory = arrayOfNulls(rows)
            val categorycolumn = c!!.getColumnIndexOrThrow("categoryname")
            if (c != null) {
                if (c!!.isFirst) {
                    count = 0
                    do {
                        subscribedCategory!![count] = c!!.getString(categorycolumn)
                        count++
                    } while (c!!.moveToNext())
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, subscribedCategory
        )
        catalogs.adapter = adapter
        catalogs.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, v: View?,
                    position: Int, id: Long
                ) {
                    selectedcatalogs = catalogs.selectedItem as String?
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        val submitSearch = findViewById(R.id.searchButton) as Button
        submitSearch.setOnClickListener {
            searchItemString = searchItem!!.text.toString()
            if (checkTextview != null) {
                val intent = Intent(
                    this@MstuffSearch,
                    MstuffTextSearchResults::class.java
                )
                val b = Bundle()
                b.putString("edittext", searchItemString)
                b.putString("spinner", selectedcatalogs)
                intent.putExtras(b)
                startActivityForResult(intent, 0)
            } else {
                val intent = Intent(
                    this@MstuffSearch,
                    MstuffSearchResults::class.java
                )
                val b = Bundle()
                b.putString("edittext", searchItemString)
                b.putString("spinner", selectedcatalogs)
                intent.putExtras(b)
                startActivityForResult(intent, 0)
            }
        }
    }
}
