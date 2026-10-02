package com.mobeegal.android.activity.catalogs

/*
<!--
$Id:: HomeViewQuery.java 14 2008-08-19 06:36:45Z muthu.ramadoss              $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.activity.FindandInstall
import com.mobeegal.android.activity.MapResults
import com.mobeegal.android.activity.Settings
import com.mobeegal.android.util.ViewMenu

class HomeViewQuery : ListActivity() {

    private var itla: CheckBoxifiedTextListAdapter? = null
    var checkBoxifiedTextobj: Array<CheckBoxifiedText?>? = null
    var count: Int = 0
    var i: Int = 0
    var text: String = ""
    var myDatabase: SQLiteDatabase? = null
    var size: Int = 20
    var iStuffRentalType = arrayOfNulls<String>(size)
    var iStuffMisc = arrayOfNulls<String>(size)
    var iStuffRate = arrayOfNulls<String>(size)
    var iStuffStatus = arrayOfNulls<String>(size)
    var iStuffCountry = arrayOfNulls<String>(size)
    var iStuffCity = arrayOfNulls<String>(size)
    var iStuffArea = arrayOfNulls<String>(size)
    var iStufflatitude = arrayOfNulls<String>(size)
    var iStufflongitude = arrayOfNulls<String>(size)
    var uStuffRentalType = arrayOfNulls<String>(size)
    var uStuffMisc = arrayOfNulls<String>(size)
    var uStuffRate = arrayOfNulls<String>(size)
    var uStuffStatus = arrayOfNulls<String>(size)
    var uStuffCountry = arrayOfNulls<String>(size)
    var uStuffCity = arrayOfNulls<String>(size)
    var uStuffArea = arrayOfNulls<String>(size)
    var queryStatus = arrayOfNulls<String>(size)
    var HomeId = IntArray(size)
    var rows: Int = 0
    var c: Cursor? = null
    private var bArray: BooleanArray? = null
    var checkboxStatus: Boolean = false

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        myDatabase = this.openOrCreateDatabase(
            "Mobeegal",
            Context.MODE_PRIVATE, null
        )
        val myCols = arrayOf(
            "key", "irental", "imisc", "irate", "istatus",
            "icountry", "icity", "iarea", "urental", "umisc", "urate",
            "ustatus", "ucountry", "ucity", "uarea", "queryStatus"
        )
        c = myDatabase!!.query("Home", myCols, null, null, null, null, null)
        rows = c!!.count
        if (rows == 0) {
            Toast.makeText(
                this@HomeViewQuery, R.string.noviewquery,
                Toast.LENGTH_LONG
            ).show()
        }
        bArray = BooleanArray(rows)
        queryStatus = arrayOfNulls(rows)


        val idcolumn = c!!.getColumnIndexOrThrow("key")
        val iStuffRentalTypeColumn = c!!.getColumnIndexOrThrow("irental")
        val iStuffMiscColumn = c!!.getColumnIndexOrThrow("imisc")
        val iStuffRateColumn = c!!.getColumnIndexOrThrow("irate")
        val iStuffStatusColumn = c!!.getColumnIndexOrThrow("istatus")
        val iStuffCountryColumn = c!!.getColumnIndexOrThrow("icountry")
        val iStuffCityColumn = c!!.getColumnIndexOrThrow("icity")
        val iStuffAreaColumn = c!!.getColumnIndexOrThrow("iarea")


        val uStuffRentalTypeColumn = c!!.getColumnIndexOrThrow("urental")
        val uStuffMiscColumn = c!!.getColumnIndexOrThrow("umisc")
        val uStuffRateColumn = c!!.getColumnIndexOrThrow("urate")
        val uStuffStatusColumn = c!!.getColumnIndexOrThrow("ustatus")
        val uStuffCountryColumn = c!!.getColumnIndexOrThrow("ucountry")
        val uStuffCityColumn = c!!.getColumnIndexOrThrow("ucity")
        val uStuffAreaColumn = c!!.getColumnIndexOrThrow("uarea")

        val querystatuscolumn = c!!.getColumnIndexOrThrow("queryStatus")


        if (c != null) {
            count = 0
            if (c!!.isFirst) {
                do {
                    val getid = c!!.getInt(idcolumn)
                    val getiStuffRentalType =
                        c!!.getString(iStuffRentalTypeColumn)
                    val getiStuffMisc = c!!.getString(iStuffMiscColumn)
                    val getiStuffRate = c!!.getString(iStuffRateColumn)
                    val getiStuffStatus = c!!.getString(iStuffStatusColumn)
                    val getiStuffCountry = c!!.getString(iStuffCountryColumn)
                    val getiStuffCity = c!!.getString(iStuffCityColumn)
                    val getiStuffArea = c!!.getString(iStuffAreaColumn)

                    val getuStuffRentalType =
                        c!!.getString(uStuffRentalTypeColumn)
                    val getuStuffMisc = c!!.getString(uStuffMiscColumn)
                    val getuStuffRate = c!!.getString(uStuffRateColumn)
                    val getuStuffStatus = c!!.getString(uStuffStatusColumn)
                    val getuStuffCountry = c!!.getString(uStuffCountryColumn)
                    val getuStuffCity = c!!.getString(uStuffCityColumn)
                    val getuStuffArea = c!!.getString(uStuffAreaColumn)

                    val getquerystatus = c!!.getString(querystatuscolumn)


                    HomeId[count] = getid
                    iStuffRentalType[count] = getiStuffRentalType
                    iStuffMisc[count] = getiStuffMisc
                    iStuffRate[count] = getiStuffRate
                    iStuffStatus[count] = getiStuffStatus
                    iStuffCountry[count] = getiStuffCountry
                    iStuffCity[count] = getiStuffCity
                    iStuffArea[count] = getiStuffArea

                    uStuffRentalType[count] = getuStuffRentalType
                    uStuffMisc[count] = getuStuffMisc
                    uStuffRate[count] = getuStuffRate
                    uStuffStatus[count] = getuStuffStatus
                    uStuffCountry[count] = getuStuffCountry
                    uStuffCity[count] = getuStuffCity
                    uStuffArea[count] = getuStuffArea

                    queryStatus[count] = getquerystatus


                    count++
                } while (c!!.moveToNext())
            }
        }
        for (i in 0 until rows) {
            if (queryStatus[i] == "true") {
                bArray!![i] = true
            } else {
                bArray!![i] = false
            }
        }


        checkBoxifiedTextobj = arrayOfNulls(rows)
        itla = CheckBoxifiedTextListAdapter(this)
        for (j in 0 until rows) {
            if (queryStatus[j] == "true") {
                checkboxStatus = true
            } else if (queryStatus[j] == "false") {
                checkboxStatus = false
            }
            checkBoxifiedTextobj!![j] = CheckBoxifiedText(
                "Owner Detail: RentalType=" + iStuffRentalType[j] +
                    ", Misc=" + iStuffMisc[j] + ", Rate=" +
                    iStuffRate[j] + ", Status=" + iStuffStatus[j] +
                    ", City=" + iStuffCity[j] + " Area=" +
                    iStuffArea[j] + ", Country = " + iStuffCountry[j] +
                    ".\nTenant Detail: RentalType=" +
                    uStuffRentalType[j] + ", Misc=" + uStuffMisc[j] +
                    ", Rate=" + uStuffRate[j] + ", Status=" +
                    uStuffStatus[j] + ", City=" + uStuffCity[j] +
                    " Area=" + uStuffArea[j] + ", Country=" +
                    uStuffCountry[j], checkboxStatus
            )
            itla!!.addItem(checkBoxifiedTextobj!![j]!!)
        }
        listAdapter = itla
    }

    inner class CheckBoxifiedText(text: String, checked: Boolean) {

        private var mText: String = text
        private var mCheckBox: CheckBox? = null
        private var mChecked: Boolean = checked

        fun setChecked(value: Boolean) {
            this.mChecked = value
        }

        fun getChecked(): Boolean {
            return this.mChecked
        }

        fun getText(): String {
            return mText
        }

        fun getCheckBox(): CheckBox? {
            return mCheckBox
        }
    }

    inner class CheckBoxifiedTextListAdapter(context: Context) : BaseAdapter() {

        private val mContext: Context = context
        var mItems: MutableList<CheckBoxifiedText> = ArrayList()
        var item: CheckBoxifiedText? = null

        fun addItem(it: CheckBoxifiedText) {
            mItems.add(it)
        }

        fun setListItems(lit: MutableList<CheckBoxifiedText>) {
            mItems = lit
        }

        fun getListItem(): List<CheckBoxifiedText> {
            return mItems
        }

        override fun getCount(): Int {
            return mItems.size
        }

        override fun getItem(position: Int): Any {
            return mItems[position]
        }

        fun areAllItemsSelectable(): Boolean {
            return false
        }

        fun getStatus(): BooleanArray {
            val bArray = BooleanArray(mItems.size)
            var increment = 0
            for (cboxtxt in mItems) {
                bArray[increment] = cboxtxt.getChecked()
            }
            return bArray
        }

        fun deSelectAll() {
            for (cboxtxt in mItems) {
                cboxtxt.setChecked(false)
            }
            this.notifyDataSetInvalidated()
        }

        fun selectAll() {
            for (cboxtxt in mItems) {
                cboxtxt.setChecked(true)
            }
            this.notifyDataSetInvalidated()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val btv: CheckBoxifiedTextView

            if (convertView == null) {
                btv = CheckBoxifiedTextView(mContext, mItems[position], position)
                val src = mItems[position]
            } else {
                val src = mItems[position]
                btv = convertView as CheckBoxifiedTextView
                btv.setText(src.getText())
                btv.setCheckBoxState(src.getChecked())
            }
            return btv
        }
    }
    /*public class CheckBoxifiedTextListAdapter extends BaseAdapter {
    private Context mContext;
    List<CheckBoxifiedText> mItems = new ArrayList<CheckBoxifiedText>();
    CheckBoxifiedText item;
    public CheckBoxifiedTextListAdapter(Context context) {
    mContext = context;
    }
    public void addItem(CheckBoxifiedText it) {
    mItems.add(it);
    }
    public void setListItems(List<CheckBoxifiedText> lit) {
    mItems = lit;
    }
    public List<CheckBoxifiedText> getListItem() {
    return mItems;
    }
    public int getCount() {
    return mItems.size();
    }
    public Object getItem(int position) {
    return mItems.get(position);
    }
    @Override
    public boolean areAllItemsSelectable() {
    return false;
    }
    public boolean[] getStatus() {
    boolean[] bArray = new boolean[mItems.size()];
    int increment = 0;
    for (CheckBoxifiedText cboxtxt : mItems) {
    bArray[increment] = cboxtxt.getChecked();
    }
    return bArray;
    }
    public void deSelectAll() {
    for (CheckBoxifiedText cboxtxt : mItems) {
    cboxtxt.setChecked(false);
    }
    this.notifyDataSetInvalidated();
    }
    public void selectAll() {
    for (CheckBoxifiedText cboxtxt : mItems) {
    cboxtxt.setChecked(true);
    }
    this.notifyDataSetInvalidated();
    }
    public long getItemId(int position) {
    return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
    CheckBoxifiedTextView btv;
    CheckBoxifiedText ctv;
    if (convertView == null) {
    btv = new CheckBoxifiedTextView(mContext, mItems.get(position), position);
    CheckBoxifiedText src = mItems.get(position);
    } else {
    CheckBoxifiedText src = mItems.get(position);
    btv = (CheckBoxifiedTextView) convertView;
    btv.setText(src.getText());
    btv.setCheckBoxState(src.getChecked());
    }
    return btv;
    }
    }*/

    inner class CheckBoxifiedTextView(
        context: Context,
        aCheckBoxifiedText: CheckBoxifiedText,
        position: Int
    ) : LinearLayout(context) {

        private val mText: TextView
        private val mCheckBox: CheckBox
        private val mCheckBoxText: CheckBoxifiedText = aCheckBoxifiedText
        var increment: Int = 0

        init {
            this.orientation = HORIZONTAL
            mCheckBox = CheckBox(context)
            mCheckBox.setPadding(0, 0, 20, 0)  // 5px to the right
            mCheckBox.isChecked = aCheckBoxifiedText.getChecked()
            mCheckBox.setOnCheckedChangeListener(OnCheckedChangeListener { _, _ ->
                try {
                    bArray!![position] = mCheckBox.isChecked
                    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]), Toast.LENGTH_LONG).show();
                } catch (e: Exception) {
                    //bArray[position] = mCheckBox.isChecked();
                    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
                    //Toast.makeText(context, " position = "+Integer.toString(position)+" rows = "+Integer.toString(rows),Toast.LENGTH_SHORT).show();
                }
            })
            addView(
                mCheckBox, LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
            )
            mText = TextView(context)
            mText.text = aCheckBoxifiedText.getText()
            addView(
                mText, LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
            )
        }

        fun setText(words: String?) {
            mText.text = words
        }

        fun setCheckBoxState(bool: Boolean) {
            mCheckBox.isChecked = mCheckBoxText.getChecked()
            mCheckBoxText.setChecked(true)
        }
    }
    /*public class CheckBoxifiedTextView extends LinearLayout {
    private TextView mText;
    private CheckBox mCheckBox;
    private CheckBoxifiedText mCheckBoxText;
    int increment = 0;
    public CheckBoxifiedTextView(final Context context, CheckBoxifiedText aCheckBoxifiedText, final int position) {
    super(context);
    this.setOrientation(HORIZONTAL);
    mCheckBoxText = aCheckBoxifiedText;
    mCheckBox = new CheckBox(context);
    mCheckBox.setPadding(0, 0, 20, 0);  // 5px to the right
    mCheckBox.setChecked(aCheckBoxifiedText.getChecked());
    mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
    try {
    bArray[position] = mCheckBox.isChecked();
    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]), Toast.LENGTH_LONG).show();
    } catch (Exception e) {
    //bArray[position] = mCheckBox.isChecked();
    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
    //Toast.makeText(context, " position = "+Integer.toString(position)+" rows = "+Integer.toString(rows),Toast.LENGTH_SHORT).show();
    }
    }
    });
    bArray[position] = mCheckBox.isChecked();
    //Toast.makeText(context, " position = " + Boolean.toString(bArray[0]) + Boolean.toString(bArray[1]) + Boolean.toString(bArray[2]), Toast.LENGTH_LONG).show();
    }
    });
    addView(mCheckBox, new LinearLayout.LayoutParams(
    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    mText = new TextView(context);
    mText.setText(aCheckBoxifiedText.getText());
    addView(mText, new LinearLayout.LayoutParams(
    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
    public void setText(String words) {
    mText.setText(words);
    }
    public void setCheckBoxState(boolean bool) {
    mCheckBox.setChecked(mCheckBoxText.getChecked());
    mCheckBoxText.setChecked(true);
    }
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsViewQueryMenu(menu)
        return true
    }

    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                val mapViewintent =
                    Intent(this@HomeViewQuery, MapResults::class.java)
                startActivity(mapViewintent)
            }
            2 -> {
                val catalogintent =
                    Intent(this@HomeViewQuery, FindandInstall::class.java)
                startActivity(catalogintent)
            }
            3 -> {
                val settingsintent =
                    Intent(this@HomeViewQuery, Settings::class.java)
                startActivity(settingsintent)
            }
            4 -> {
                val homeintent = Intent(this@HomeViewQuery, Home::class.java)
                startActivity(homeintent)
            }
            5 -> {
                if (rows == 0) {
                    Toast.makeText(
                        this@HomeViewQuery, R.string.donequery,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    for (j in 0 until rows) {
                        if (bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Home set queryStatus='" +
                                    "true" + "' where key=" + HomeId[j] + ";"
                            )
                            myDatabase!!.execSQL(
                                "update category set querystatus='" +
                                    "true" + "' where categoryname='" +
                                    "Rental" + "';"
                            )
                        } else if (!bArray!![j]) {
                            myDatabase!!.execSQL(
                                "update Home set queryStatus='" +
                                    "false" + "' where key=" + HomeId[j] + ";"
                            )
                        }
                        Toast.makeText(
                            this@HomeViewQuery,
                            "Boolean = " + java.lang.Boolean.toString(bArray!![j]),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Toast.makeText(
                        this@HomeViewQuery,
                        this.getString(R.string.ShowMessage),
                        Toast.LENGTH_LONG
                    ).show()
//                Intent updateintent = new Intent(HomeViewQuery.this, Home.class);
//                startActivity(updateintent);
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
