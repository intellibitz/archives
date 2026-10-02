package com.mobeegal.android.activity

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.mobeegal.android.R

class Response : Activity() {

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.response)
        val resText = findViewById(R.id.res) as TextView
        val bun = this.intent.extras
        val s = bun!!.getString("serverResponse")
        // String s1 = bun.getString("serverRes");
        resText.text = s
    }
}
