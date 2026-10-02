/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import com.mobeegal.android.R

/**
 * @author work
 */
class Modes : Activity() {

    private var requestno: Int = 0
    private var id: Int = 0
    private var mode = "Basic"
    private var premium: RadioButton? = null
    private var standard: RadioButton? = null
    private var basic: RadioButton? = null
    private var requestcatalog: String? = null
    var myDB: SQLiteDatabase? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.modes)

        try {
            myDB = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
        } catch (e: Exception) {
        }

        basic = findViewById(R.id.basic) as RadioButton
        basic!!.isChecked = true
        basic!!.setOnClickListener {
            mode = "Basic"
            id = 0
        }
        standard = findViewById(R.id.standard) as RadioButton
        standard!!.setOnClickListener {
            mode = "Standard"
            id = 2
        }
        premium = findViewById(R.id.premium) as RadioButton
        premium!!.setOnClickListener {
            mode = "Premium"
            id = 1
        }

        val B2 = this.intent.extras
        if (B2 != null) {
            requestcatalog = B2.getString("requestcatalog")
            requestno = B2.getInt("requestno")
        }

        val modeok = findViewById(R.id.modeyes) as Button
        modeok.setOnClickListener {
            myDB!!.execSQL(
                "update " + requestcatalog +
                    "_category set modes='" + mode + "' where categoryID=" +
                    requestno
            )
            myDB!!.execSQL(
                "update category set modes='" + mode +
                    "' , categoryID=" + id + " where catalogtype='" +
                    requestcatalog + "'and categoryID=" + requestno
            )
            // Toast.makeText(Modes.this, "Bundle passed : " + requestcatalog + id, Toast.LENGTH_SHORT).show();
            val B3 = Bundle()
            B3.putString("requestcatalog", requestcatalog)
            val modes = Intent(this@Modes, Subscribe::class.java)
            modes.putExtras(B3)
            startActivityForResult(modes, 0)
            finish()
        }

        val modeno = findViewById(R.id.modeno) as Button
        modeno.setOnClickListener {
            myDB!!.execSQL(
                "update " + requestcatalog +
                    "_category set status='false' where categoryID=" +
                    requestno
            )
            myDB!!.execSQL(
                "update category set status='false' where catalogtype='" +
                    requestcatalog + "'and categoryID=" +
                    requestno
            )
            val B3 = Bundle()
            B3.putString("requestcatalog", requestcatalog)
            val modes = Intent(this@Modes, Subscribe::class.java)
            modes.putExtras(B3)
            startActivityForResult(modes, 0)
            finish()
        }
    }
}
