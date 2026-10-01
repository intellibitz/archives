package com.mobeegal.android.activity.catalogs

//import java.util.logging.Logger;

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.mobeegal.android.R

class TextLocations : Activity() {

    var myDB: SQLiteDatabase? = null

    var res1 = arrayOfNulls<String>(50)
    var res3 = arrayOfNulls<String>(50)

    private var tableName: String? = null
    private var getkey: Int = 0
    private var getcategory: String? = null
    private var getstufftype: String? = null
    //  private static Logger logger = Logger.getLogger("TextLocations");

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.textlocation)
        val b = this.intent.extras
        if (b != null) {
            tableName = b.getString("tablename")
            getkey = b.getInt("key")
        }

        try {
            myDB = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val columnname = arrayOf("category", "stufftype")
            val cursor = myDB!!.query(
                tableName, columnname, null, null, null,
                null, null
            )

            if (cursor != null) {
                if (cursor.isFirst) {
                    getcategory =
                        cursor.getString(
                            cursor.getColumnIndexOrThrow("category")
                        )
                    getstufftype = cursor.getString(
                        cursor.getColumnIndexOrThrow("stufftype")
                    )
                }
            }
        } catch (e: Exception) {
        }

        // storing values
        /*  if (getstufftype.equals("istuff")) {
            myDatabase.execSQL("UPDATE " + tableName + " set iarea='" + area + "', icountry='" + country + "', icity='" + city + "';");
        } else if (getstufftype.equals("ustuff")) {
            myDatabase.execSQL("UPDATE " + tableName + " set uarea='" + area + "', ucountry='" + country + "', ucity='" + city + "';");
        }*/

        //  storing values
        val save = findViewById(R.id.savinglocation) as Button
        save.setOnClickListener {
            if (getcategory == "Dating") {
                val locationfinder =
                    Intent(this@TextLocations, Dating::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Matrimony") {
                val locationfinder =
                    Intent(this@TextLocations, Matrimony::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Cars") {
                val locationfinder =
                    Intent(this@TextLocations, Cars::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Rental") {
                val locationfinder =
                    Intent(this@TextLocations, Home::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Restaurants") {
                val locationfinder =
                    Intent(this@TextLocations, Restaurants::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Movies") {
                val locationfinder =
                    Intent(this@TextLocations, Movies::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            } else if (getcategory == "Jewelry") {
                val locationfinder =
                    Intent(this@TextLocations, Jewelry::class.java)
                val passkey = Bundle()
                passkey.putInt("key", getkey)
                locationfinder.putExtras(passkey)
                startActivityForResult(locationfinder, 0)
                finish()
            }
        }
    }
}
