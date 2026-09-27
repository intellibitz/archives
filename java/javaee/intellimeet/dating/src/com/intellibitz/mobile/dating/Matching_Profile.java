package com.intellibitz.mobile.dating;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Matching_Profile extends ListActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Toast.makeText(this, "The following Dream-mates matches your profile.", Toast.LENGTH_LONG).show();
        setListAdapter(new SpeechListAdapter(this));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ((SpeechListAdapter) getListAdapter()).toggle(position);
        
    }

    private class SpeechListAdapter extends BaseAdapter {

        public SpeechListAdapter(Context context) {
            mContext = context;
        }
        /* How many items are in the data set represented by this Adapter.*/

        public int getCount() {
            return mTitles.length;
        }
        /* Get the data item associated with the specified position in the data set. */

        public Object getItem(int position) {
            return position;
        }
        /*Get the row id associated with the specified position in the list. */

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            SpeechView sv;
            if (convertView == null) {
                sv = new SpeechView(mContext, mTitles[position], mDialogue[position], mExpanded[position]);
            } else {
                sv = (SpeechView) convertView;
                sv.setTitle(mTitles[position]);
                sv.setDialogue(mDialogue[position]);
                sv.setExpanded(mExpanded[position]);
            }

            return sv;
        }

        public void toggle(int position) {
            mExpanded[position] = !mExpanded[position];
            notifyDataSetChanged();
        }
        private Context mContext;
        private String[] mTitles =
                {
            "23",
            "18",
            "19",
            "21",
            "22",
            "23",
            "24",
            "19"
        };
        private String[] mDialogue =
                {
            "FEMALE,23,155 cm,55Kg,T.NAGAR.",
            "FEMALE,18,156 cm,56Kg,T.NAGAR.",
            "FEMALE,19,157 cm, 57Kg,T.NAGAR.",
            "FEMALE,21,158 cm, 45Kg,T.NAGAR.",
            "FEMALE,22,145 cm, 50Kg,T.NAGAR.",
            "FEMALE,23,165 cm, 55Kg,T.NAGAR.",
            "FEMALE,24,160 cm, 52Kg,T.NAGAR.",
            "FEMALE,19,158 cm, 60Kg,T.NAGAR."
        ,
                };
        private   Intent  mI;
          
        

        private boolean[] mExpanded =
        {
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        };
    }

    private class SpeechView extends LinearLayout {
        private Button mButton;

        public SpeechView(Context context, String title, String dialogue, boolean expanded) {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            mTitle.setText(title);
            
           
            addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            mDialogue = new TextView(context);
            mDialogue.setText(dialogue);
            addView(mDialogue, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
            mButton=new Button(context);
            mButton.setText("chat");
            addView(mButton, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
             mButton.setVisibility(expanded ? VISIBLE : GONE);
             mButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent mI = new Intent(Matching_Profile.this,Chat.class);
                startActivity(mI);
            }
            });
        }

       

        public void setTitle(String title) {
            mTitle.setText(title);
        }

        public void setDialogue(String words) {
            mDialogue.setText(words);
        }

        public void setExpanded(boolean expanded) {
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
            mButton.setVisibility(expanded ? VISIBLE : GONE);
        }
        private TextView mTitle;
        private TextView mDialogue;
    }
}
