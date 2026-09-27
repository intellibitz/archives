package com.mobeegal.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.mobeegal.android.R;

public class Response
        extends Activity
{

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.response);
        TextView resText = (TextView) findViewById(R.id.res);
        Bundle bun = this.getIntent().getExtras();
        String s = bun.getString("serverResponse");
        // String s1 = bun.getString("serverRes");
        resText.setText(s);
    }
}
