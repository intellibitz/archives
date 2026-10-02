package com.mobeegal.android.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mobeegal.android.model.IconifiedText
import java.util.ArrayList

/**
 * @author Steven Osborn - http://steven.bitsetters.com
 */
class IconifiedTextListAdapter(private val mContext: Context) : BaseAdapter() {

    /**
     * Remember our context so we can use it when constructing views.
     */
    private var mItems: MutableList<IconifiedText> = ArrayList()

    fun addItem(it: IconifiedText) {
        mItems.add(it)
    }

    fun setListItems(lit: MutableList<IconifiedText>) {
        mItems = lit
    }

    /**
     * @return The number of items in the
     */
    override fun getCount(): Int {
        return mItems.size
    }

    override fun getItem(position: Int): Any {
        return mItems[position]
    }

    fun areAllItemsSelectable(): Boolean {
        return false
    }

    fun isSelectable(position: Int): Boolean {
        return mItems[position].isSelectable()
    }

    /**
     * Use the array index as a unique id.
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * @param convertView The old view to overwrite, if one is passed
     * @returns a IconifiedTextView that holds wraps around an IconifiedText
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val btv: IconifiedTextView
        if (convertView == null) {
            btv = IconifiedTextView(mContext, mItems[position])
        } else { // Reuse/Overwrite the View passed
            // We are assuming(!) that it is castable!
            btv = convertView as IconifiedTextView
            btv.setText(mItems[position].getText())
            btv.setIcon(mItems[position].getIcon())
        }
        return btv
    }
}
