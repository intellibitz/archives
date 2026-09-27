package com.mobeegal.android.activity;

/*
<!--
$Id:: ListCatalog.java 14 2008-08-19 06:36:45Z muthu.ramadoss                $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.mobeegal.android.activity.catalogs.Dating;
import com.mobeegal.android.util.ViewMenu;

public class ListCatalog
        extends ListActivity
{

    String gettingcategory;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        final Bundle bundles = this.getIntent().getExtras();
        if (bundles != null)
        {
            gettingcategory = bundles.getString("passingcatalog");
        }
        setListAdapter(new SpeechListAdapter(this));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        ((SpeechListAdapter) getListAdapter()).toggle(position);
    }

    private class SpeechListAdapter
            extends BaseAdapter
    {

        public SpeechListAdapter(Context context)
        {
            mContext = context;
        }

        /* How many items are in the data set represented by this Adapter. */
        public int getCount()
        {
            return mTitles.length;
        }

        /*
         * Get the data item associated with the specified position in the data
         * set.
         */
        public Object getItem(int position)
        {
            return position;
        }

        /* Get the row id associated with the specified position in the list. */
        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            SpeechView sv;
            if (convertView == null)
            {
                sv = new SpeechView(mContext, mTitles[position], null,
                        mExpanded[position]);
            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(mTitles[position]);
                sv.setExpanded(mExpanded[position]);

            }
            return sv;
        }

        public void toggle(int position)
        {
            mExpanded[position] = !mExpanded[position];
            notifyDataSetChanged();
        }

        private Context mContext;
        private String[] mTitles = {gettingcategory,};

        private boolean[] mExpanded = {true,};


    }

    private class SpeechView
            extends LinearLayout
    {
        private Button mButton;

        public SpeechView(Context context, String title, String dialogue,
                boolean expanded)
        {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            mButton = new Button(context);
            mButton.setText("Dating");
            addView(mButton, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            mButton.setVisibility(expanded ? VISIBLE : GONE);
            mButton.setOnClickListener(new OnClickListener()
            {

                public void onClick(View v)
                {

                    Intent mI = new Intent(ListCatalog.this, Dating.class);
                    startActivity(mI);
                }
            });
        }

        public void setTitle(String title)
        {
            mTitle.setText(title);
        }

        public void setExpanded(boolean expanded)
        {
            mButton.setVisibility(expanded ? VISIBLE : GONE);
        }

        private TextView mTitle;
    }

    //	MenuView
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    //	Menu Item
    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {
        switch (item.getItemId())
        {
            case 1:
//			mStuff Menu
                Intent stuffCheckintent =
                        new Intent(ListCatalog.this, MapResults.class);
                startActivityForResult(stuffCheckintent, 0);
                finish();
                break;
            case 2:
                Intent intent1 =
                        new Intent(ListCatalog.this, FindandInstall.class);
                startActivityForResult(intent1, 0);
                finish();
                break;
            case 3:
                Intent settings = new Intent(ListCatalog.this, Settings.class);
                startActivityForResult(settings, 0);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
