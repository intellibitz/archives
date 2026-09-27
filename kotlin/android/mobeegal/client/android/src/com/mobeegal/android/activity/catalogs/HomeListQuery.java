package com.mobeegal.android.activity.catalogs;

/*
<!--
$Id:: HomeListQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss              $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.ListActivity;
import android.content.Context;
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
import com.mobeegal.android.activity.FindandInstall;
import com.mobeegal.android.activity.MapResults;
import com.mobeegal.android.activity.Settings;
import com.mobeegal.android.util.ViewMenu;

public class HomeListQuery
        extends ListActivity
        implements OnItemClickListener
{

    SQLiteDatabase myDatabase = null;

    int irentalposition;
    int imiscposition;
    int istatusposition;
    int irateposition;
    int urentalposition;
    int umiscposition;
    int ustatusposition;
    int urateposition;
    int getirentalposition;
    int getimiscposition;
    int getistatusposition;
    int getirateposition;
    int geturentalposition;
    int getumiscposition;
    int getustatusposition;
    int geturateposition;
    String getiarea;
    String geticity;
    String geticountry;
    String getuarea;
    String getucity;
    String getucountry;
    String getcategory;
    String getstufftype;
    String getilatitude;
    String getilongitude;
    String getulatitude;
    String getulongitude;

    Cursor c;
    int rows = 0;
    int count = 0;
    int size = 15;
    int[] HomeId = new int[size];
    String[] irental = new String[size];
    String[] imisc = new String[size];
    String[] irate = new String[size];
    String[] istatus = new String[size];
    String[] icountry = new String[size];
    String[] icity = new String[size];
    String[] iarea = new String[size];

    String[] urental = new String[size];
    String[] umisc = new String[size];
    String[] urate = new String[size];
    String[] ustatus = new String[size];
    String[] ucountry = new String[size];
    String[] ucity = new String[size];
    String[] uarea = new String[size];


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
                String myCols[] = {"key", "irental", "imisc", "irate",
                        "istatus", "icountry", "icity", "iarea", "urental",
                        "umisc", "urate", "ustatus", "ucountry", "ucity",
                        "uarea", "queryStatus"};
                c = myDatabase
                        .query("Home", myCols, null, null, null, null, null);
                rows = c.getCount();

                int idcolumn = c.getColumnIndexOrThrow("key");
                int irentaltypeColumn = c.getColumnIndexOrThrow("irental");
                int imiscColumn = c.getColumnIndexOrThrow("imisc");
                int irateColumn = c.getColumnIndexOrThrow("irate");
                int istatuscolumn = c.getColumnIndexOrThrow("istatus");
                int icountryColumn = c.getColumnIndexOrThrow("icountry");
                int icityColumn = c.getColumnIndexOrThrow("icity");
                int iareaColumn = c.getColumnIndexOrThrow("iarea");


                int urentaltypeColumn = c.getColumnIndexOrThrow("urental");
                int umiscColumn = c.getColumnIndexOrThrow("umisc");
                int urateColumn = c.getColumnIndexOrThrow("urate");
                int ustatuscolumn = c.getColumnIndexOrThrow("ustatus");
                int ucountryColumn = c.getColumnIndexOrThrow("ucountry");
                int ucityColumn = c.getColumnIndexOrThrow("ucity");
                int uareaColumn = c.getColumnIndexOrThrow("uarea");

                if (c != null)
                {
                    count = 0;
                    if (c.isFirst())
                    {
                        do
                        {
                            int getid = c.getInt(idcolumn);
                            String getirentaltype =
                                    c.getString(irentaltypeColumn);
                            String getimisc = c.getString(imiscColumn);
                            String getirate = c.getString(irateColumn);
                            String getistatus = c.getString(istatuscolumn);
                            String geticountry = c.getString(icountryColumn);
                            String geticity = c.getString(icityColumn);
                            String getiarea = c.getString(iareaColumn);

                            String geturentaltype =
                                    c.getString(urentaltypeColumn);
                            String getumisc = c.getString(umiscColumn);
                            String geturate = c.getString(urateColumn);
                            String getustatus = c.getString(ustatuscolumn);
                            String getucountry = c.getString(ucountryColumn);
                            String getucity = c.getString(ucityColumn);
                            String getuarea = c.getString(uareaColumn);

                            HomeId[count] = getid;
                            irental[count] = getirentaltype;
                            imisc[count] = getimisc;
                            irate[count] = getirate;
                            istatus[count] = getistatus;
                            icountry[count] = geticountry;
                            icity[count] = geticity;
                            iarea[count] = getiarea;


                            urental[count] = geturentaltype;
                            umisc[count] = getumisc;
                            urate[count] = geturate;
                            ustatus[count] = getustatus;
                            ucountry[count] = getucountry;
                            ucity[count] = getucity;
                            uarea[count] = getuarea;

                            count++;
                        }
                        while (c.moveToNext());
                    }
                }

            }
            catch (Exception ex)
            {
                //Toast.makeText(HomeListQuery.this, ""+ex, Toast.LENGTH_LONG).show();
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
                        irental[position] + " Misc = " + imisc[position] +
                        "Rate = " + irate[position] + "status = " +
                        istatus[position] + " Area = " + iarea[position] +
                        " City = " + icity[position] + " Country = " +
                        icountry[position],
                        " RentalType = " + urental[position] + " Misc = " +
                                umisc[position] + " Rate = " + urate[position] +
                                " Status =" + ustatus[position] + " Area = " +
                                uarea[position] + " City = " + ucity[position] +
                                " Country= " + ucountry[position]);

            }
            else
            {
                sv = (SpeechView) convertView;
                sv.setTitle(irental[position]);
                sv.setDialogue(ucity[position]);
            }
            return sv;
        }

        private Context mContext;

        /* public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {

        }*/
    }

    public class SpeechView
            extends LinearLayout
    {

        public SpeechView(Context context, String title, String words)
        {
            super(context);
            this.setOrientation(VERTICAL);
            mTitle = new TextView(context);
            mTitle.setText("Owner Detail : " + title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText("Tenant Detail : " + words);
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

        String selectid = parent.getItemAtPosition(position).toString();
        int passkeyvalue = Integer.parseInt(selectid);
        Intent editrental = new Intent(HomeListQuery.this, Home.class);
        Cursor temprentalcursor = myDatabase.query("rentalposition", null,
                "key=" + HomeId[passkeyvalue], null, null, null, null);

        if (temprentalcursor != null)
        {
            if (temprentalcursor.isFirst())
            {
                do
                {
                    getirentalposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "irentalposition"));
                    getimiscposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "imiscposition"));
                    getistatusposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "istatusposition"));
                    getirateposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "irateposition"));

                    geturentalposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "urentalposition"));
                    getumiscposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "umiscposition"));
                    getustatusposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "ustatusposition"));
                    geturateposition = temprentalcursor
                            .getInt(temprentalcursor.getColumnIndexOrThrow(
                                    "urateposition"));

                    getiarea = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow("iarea"));
                    geticity = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow("icity"));
                    geticountry = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow("icountry"));
                    getuarea = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow("uarea"));
                    getucity = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow("ucity"));
                    getucountry = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow("ucountry"));
                    getilatitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                    "ilatitude"));
                    getilongitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                    "ilongitude"));
                    getulatitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                    "ulatitude"));
                    getulongitude = temprentalcursor.getString(
                            temprentalcursor.getColumnIndexOrThrow(
                                    "ulongitude"));
                    myDatabase.execSQL("CREATE TABLE IF NOT EXISTS temprental" +
                            " (irentalposition NUMERIC, imiscposition NUMERIC, istatusposition NUMERIC,irateposition NUMERIC, iarea VARCHAR, icity VARCHAR, icountry VARCHAR, urentalposition NUMERIC, umiscposition NUMERIC, ustatusposition NUMERIC,urateposition NUMERIC,uarea VARCHAR, ucity VARCHAR, ucountry VARCHAR, ilatitude VARCHAR, ilongitude VARCHAR, ulatitude VARCHAR, ulongitude VARCHAR, category VARCGHAR, stufftype VARCHAR);");
                    myDatabase.execSQL(
                            "INSERT INTO temprental (irentalposition,imiscposition,istatusposition,irateposition,iarea,icity,icountry,urentalposition,umiscposition,ustatusposition,urateposition,uarea,ucity,ucountry, ilatitude , ilongitude , ulatitude , ulongitude , category , stufftype ) VALUES (" +
                                    getirentalposition + "," +
                                    getimiscposition + "," +
                                    getistatusposition + "," +
                                    getirateposition + ",'" + getiarea + "','" +
                                    geticity + "','" + geticountry + "'," +
                                    geturentalposition + "," +
                                    getumiscposition + "," +
                                    getustatusposition + "," +
                                    geturateposition + ",'" + getuarea + "','" +
                                    getucity + "','" + getucountry + "','" +
                                    getilatitude + "','" + getilongitude +
                                    "','" + getulatitude + "','" +
                                    getulongitude + "','" + "Rental" + "', '" +
                                    "istuff" + "');");
                }
                while (temprentalcursor.moveToNext());
            }
        }
        Bundle passkeyBundle = new Bundle();
        passkeyBundle.putInt("key", HomeId[passkeyvalue]);
        editrental.putExtras(passkeyBundle);
        startActivityForResult(editrental, 0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        ViewMenu.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int i, MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case 1:
                Intent stuffCheckintent = new Intent(HomeListQuery.this,
                        MapResults.class);
                startActivity(stuffCheckintent);
                break;
            case 2:
                Intent intent1 = new Intent(HomeListQuery.this,
                        FindandInstall.class);
                startActivity(intent1);
                break;
            case 3:
                Intent settings =
                        new Intent(HomeListQuery.this, Settings.class);
                startActivity(settings);
                break;

        }
        return super.onMenuItemSelected(i,
                menuItem);    //To change body of overridden methods use File | Settings | File Templates.
    }// Menu Item

}



