package com.intellibitz.mobile.dating;

import java.io.FileNotFoundException;
import android.database.sqlite.*;
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
import android.app.AlarmManager;
import android.os.SystemClock;

public class MatchingProfile extends ListActivity {

	  SQLiteDatabase myDatabase = null;
	  String profile1;
	  String profile2;
	  String profile3;
	  String profile4;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
          try{
        	  this.createDatabase("Matching", 1, MODE_PRIVATE,null);
              myDatabase =this.openDatabase("Matching",null);
          }catch(FileNotFoundException e){}
         Toast.makeText(this, "The following Dream-mates matches your profile.", Toast.LENGTH_LONG).show();
         Intent intobject1 = new Intent(MatchingProfile.this,RepeatingAlarm.class);
         long firstTime = SystemClock.elapsedRealtime();
         firstTime += 10*1000;

        // Schedule the alarm!
         AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
         am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, 10*1000, intobject1);
         setTheme(android.R.style.Theme_Dialog);
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
                sv = new SpeechView(mContext, mTitles[position],mDialogue[position], mExpanded[position]);
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
                "SWEETY",
                "BABLI",
                "LOLO",
                "AISH",
        };
      private String[] mDialogue =
      {
        		profile1="FEMALE,23,155 cm,55Kg,St.ThomasMount",
        		profile2="FEMALE,18,156 cm,56Kg,TNagar-Renganathan Street",
        		profile3="FEMALE,19,157 cm, 57Kg,TNagar-Panagalpark.",
        		profile4="FEMALE,21,158 cm, 45Kg,Adyar.",
     };
        private boolean[] mExpanded =
        {
            false,
            false,
            false,
            false,
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

            addView(mButton, new LinearLayout.LayoutParams(50,35));
            mButton.setVisibility(expanded ? VISIBLE : GONE);
            mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	 myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Profile"
            			         +"(match1 VARCHAR, match2 VARCHAR, match3 VARCHAR, match4 VARCHAR);");
            	 myDatabase.execSQL("INSERT INTO Profile(match1,match2,match3,match4)" +
            	 		           " VALUES  ('"+profile1+"','"+profile2+"','"+profile3+"','"+profile4+"');");
            	 Intent intent = new Intent(MatchingProfile.this,RepeatingAlarm.class);
                 AlarmManager am =(AlarmManager)getSystemService(ALARM_SERVICE);
                 am.cancel(intent);
                Intent mI = new Intent(MatchingProfile.this,Chat.class);
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


