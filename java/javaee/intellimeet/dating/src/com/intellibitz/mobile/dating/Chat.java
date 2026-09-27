/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intellibitz.mobile.dating;

/**
 *
 * @author gunasekaran
 */
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class Chat extends Activity {

    private static final CharSequence[] messages = {};
    private List<CharSequence> allmessages;
    private ArrayAdapter<CharSequence> arraymessages;
    private EditText txtNewMessage;
    private ListView listMessage;
    private TextView textMessage;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.chat);

        txtNewMessage = (EditText) findViewById(R.id.txtNewMessage);
        textMessage = (TextView) findViewById(R.id.message);
        final TextView tv = (TextView) findViewById(R.id.chatmessage);
        listMessage = (ListView) findViewById(R.id.listmessage);
        allmessages = new ArrayList<CharSequence>();
        for (CharSequence curmessage : messages) {
            allmessages.add(curmessage);
        }
        arraymessages = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item, allmessages);
        listMessage.setAdapter(arraymessages);
        final Button btnAddItem = (Button) findViewById(R.id.submit);
        btnAddItem.setOnClickListener(btnAddItemListener);
    }
    private Button.OnClickListener btnAddItemListener =
            new Button.OnClickListener() {

                public void onClick(View v) {
                    CharSequence newmessage = txtNewMessage.getText().toString();
                    txtNewMessage.setText(" ");
                    arraymessages.insertObject(newmessage, 0);
                }
                };
}



