package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: DeleteHome.java 14 2008-08-19 06:36:45Z muthu.ramadoss                 $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/


import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.util.ViewMenu;

public class DeleteHome
        extends ListActivity
        implements OnItemClickListener
{
    SQLiteDatabase myDatabase = null;
    Cursor c;
    int rows = 0;
    int count = 0;
    int size = 15;
    String[] iStuffRentalType = new String[size];
    String[] iStuffMisc = new String[size];
    String[] iStuffRate = new String[size];
    String[] iStuffCountry = new String[size];
    String[] iStuffCity = new String[size];
    String[] iStuffArea = new String[size];
    String[] uStuffRentalType = new String[size];
    String[] uStuffMisc = new String[size];
    String[] uStuffRate = new String[size];
    String[] uStuffCountry = new String[size];
    String[] uStuffCity = new String[size];
    String[] uStuffArea = new String[size];
    int[] HomeId = new int[size];
    String selectid;

    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setListAdapter(new SpeechListAdapter(this));
        getListView().setOnItemClickListener(this);
    }

    public class SpeechListAdapter
            extends BaseAdapter
    {

        public SpeechListAdapter(Context context)
        {
            //Context mContext = (Context) context;
            mContext = context;
            try
            {
                myDatabase = mContext.openOrCreateDatabase("Mobeegal",
                        Context.MODE_PRIVATE, null);
                //myDatabase = mContext.openDatabase("Mobeegal", null);
                String myCols[] = {"key", "iStuffRentalType", "iStuffMisc",
                        "iStuffRate", "iStuffCountry", "iStuffCity",
                        "iStuffArea", "uStuffRentalType", "uStuffMisc",
                        "uStuffRate", "uStuffCountry", "uStuffCity",
                        "uStuffArea"};
                c = myDatabase
                        .query("Home", myCols, null, null, null, null, null);
                rows = c.getCount();


                int idcolumn = c.getColumnIndexOrThrow("key");
                int irentaltypeColumn =
                        c.getColumnIndexOrThrow("iStuffRentalType");
                int imiscColumn = c.getColumnIndexOrThrow("iStuffMisc");
                int irateColumn = c.getColumnIndexOrThrow("iStuffRate");
                int icountryColumn = c.getColumnIndexOrThrow("iStuffCountry");
                int icityColumn = c.getColumnIndexOrThrow("iStuffCity");
                int iareaColumn = c.getColumnIndexOrThrow("iStuffArea");

                int urentaltypeColumn =
                        c.getColumnIndexOrThrow("uStuffReantalType");
                int umiscColumn = c.getColumnIndexOrThrow("uStuffMisc");
                int urateColumn = c.getColumnIndexOrThrow("uStuffRate");
                int ucountryColumn = c.getColumnIndexOrThrow("uStuffCountry");
                int ucityColumn = c.getColumnIndexOrThrow("uStuffCity");
                int uareaColumn = c.getColumnIndexOrThrow("uStuffArea");

                if (c != null)
                {
                    // count = 0;
                    if (c.isFirst())
                    {
                        do
                        {
                            int getid = c.getInt(idcolumn);
                            String getirentaltype =
                                    c.getString(irentaltypeColumn);
                            String getimisc = c.getString(imiscColumn);
                            String getirate = c.getString(irateColumn);
                            String geticountry = c.getString(icountryColumn);
                            String geticity = c.getString(icityColumn);
                            String getiarea = c.getString(iareaColumn);

                            String geturentaltype =
                                    c.getString(urentaltypeColumn);
                            String getumisc = c.getString(umiscColumn);
                            String geturate = c.getString(urateColumn);
                            String getucountry = c.getString(ucountryColumn);
                            String getucity = c.getString(ucityColumn);
                            String getuarea = c.getString(uareaColumn);

                            HomeId[count] = getid;
                            iStuffRentalType[count] = getirentaltype;
                            iStuffMisc[count] = getimisc;
                            iStuffRate[count] = getirate;
                            iStuffCountry[count] = geticountry;
                            iStuffCity[count] = geticity;
                            iStuffArea[count] = getiarea;


                            uStuffRentalType[count] = geturentaltype;
                            uStuffMisc[count] = getumisc;
                            uStuffRate[count] = geturate;
                            uStuffCountry[count] = getucountry;
                            uStuffCity[count] = getucity;
                            uStuffArea[count] = getuarea;

                            count++;
                        }
                        while (c.moveToNext());
                    }
                }

            }
            catch (Exception ex)
            {
                //Toast.makeText(DeleteHome.this, ""+ex, Toast.LENGTH_LONG).show();
                //Logger.getLogger(ListClick.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public int getCount()
        {
            return rows;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            SpeechView sv;
            if (convertView == null)
            {
                sv = new SpeechView(mContext, " RentalType = " +
                        iStuffRentalType[position] + " Misc = " +
                        iStuffMisc[position] + "Rate = " +
                        iStuffRate[position] + " Country = " +
                        iStuffCountry[position] + " Area = " +
                        iStuffArea[position] + " City = " +
                        iStuffCity[position],
                        " uRentalType = " + uStuffRentalType[position] +
                                " uMisc = " + uStuffMisc[position] +
                                "uRate = " + uStuffRate[position] +
                                " uCountry= " + uStuffCountry[position] +
                                " Area = " + uStuffArea[position] + " City = " +
                                uStuffCity[position]);
            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(iStuffRentalType[position]);
                sv.setDialogue(uStuffArea[position]);
            }
            return sv;
        }

        private Context mContext;


    }

    public class SpeechView
            extends LinearLayout
    {

        public SpeechView(Context context, String title, String words)
        {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            mTitle.setText("IStuff : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("UStuff : " + words);
            addView(mDialogue, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        public void setTitle(String title)
        {
            mTitle.setText(title);
        }

        public void setDialogue(String words)
        {
            mDialogue.setText(words);
        }

        private TextView mTitle;
        private TextView mDialogue;
    }

    public void onItemClick(AdapterView parent, View v, int position, long id)
    {
        selectid = parent.getItemAtPosition(position).toString();
        final int selectedId = Integer.parseInt(selectid);
        if (rows > 0)
        {
            myDatabase.execSQL("update category set querystatus='" + "true" +
                    "' where status='" + "true" + "';");
        }
        else
        {
            myDatabase.execSQL("update category set querystatus='" + "false" +
                    "' where status='" + "true" + "';");
        }
        //Toast.makeText(DeleteHome.this, selectedId, Toast.LENGTH_LONG).show();
        OnClickListener okButtonListener = new OnClickListener()
        {

            public void onClick(DialogInterface arg0, int arg1)
            {
                try
                {
                    myDatabase =
                            DeleteHome.this.openOrCreateDatabase("Mobeegal",
                                    Context.MODE_PRIVATE, null);
                    myDatabase.delete("Home", "key=" + selectid, null);
                    if (selectedId == 0 && HomeId[0] != 0)
                    {
                        myDatabase.delete("Home", "key=" + HomeId[0], null);
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(DeleteHome.this, "Error", Toast.LENGTH_LONG)
                            .show();
                }
                Intent intent = new Intent(DeleteHome.this, DeleteHome.class);
                startActivity(intent);
                finish();

            }
        };
        OnClickListener cancelButtonListener = new OnClickListener()
        {
            // @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                // Do nothing
            }
        };
//        AlertDialog.show(this, "Delete", position, " Do you want to delete the query\n", "OK", okButtonListener, "cancel", cancelButtonListener, false, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    // Menu Item
    @Override
    public boolean onMenuItemSelected(int i, MenuItem item)
    {

        switch (item.getItemId())
        {
            case 1:
                Intent stuffCheckintent = new Intent(DeleteHome.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(DeleteHome.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings = new Intent(DeleteHome.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}


