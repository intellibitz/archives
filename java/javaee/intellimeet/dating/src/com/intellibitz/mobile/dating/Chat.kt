/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intellibitz.mobile.dating

/**
 *
 * @author gunasekaran
 */
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import java.util.ArrayList

class Chat : Activity() {

    private var allmessages: MutableList<CharSequence>? = null
    private var arraymessages: ArrayAdapter<CharSequence>? = null
    private var txtNewMessage: EditText? = null
    private var listMessage: ListView? = null
    private var textMessage: TextView? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.chat)

        txtNewMessage = findViewById(R.id.txtNewMessage) as EditText
        textMessage = findViewById(R.id.message) as TextView
        val tv = findViewById(R.id.chatmessage) as TextView
        listMessage = findViewById(R.id.listmessage) as ListView
        allmessages = ArrayList()
        for (curmessage in messages) {
            allmessages!!.add(curmessage)
        }
        arraymessages = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, allmessages
        )
        listMessage!!.adapter = arraymessages
        val btnAddItem = findViewById(R.id.submit) as Button
        btnAddItem.setOnClickListener(btnAddItemListener)
    }

    private val btnAddItemListener =
        Button.OnClickListener {
            val newmessage: CharSequence = txtNewMessage!!.text.toString()
            txtNewMessage!!.setText(" ")
            arraymessages!!.insertObject(newmessage, 0)
        }

    companion object {
        private val messages = arrayOf<CharSequence>()
    }
}
