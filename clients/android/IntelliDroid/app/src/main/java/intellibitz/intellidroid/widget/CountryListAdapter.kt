package intellibitz.intellidroid.widget

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import intellibitz.intellidroid.R
import intellibitz.intellidroid.bean.Country

class CountryListAdapter(
    private val context: Context,
    var countries: List<Country>
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private fun getResId(drawableName: String): Int {
        return try {
            val res = R.drawable::class.java
            val field = res.getField(drawableName)
            field.getInt(null)
        } catch (e: Exception) {
            Log.e("CountryCodePicker", "Failure to get drawable id.", e)
            -1
        }
    }

    override fun getCount(): Int {
        return countries.size
    }

    override fun getItem(arg0: Int): Any? {
        return null
    }

    override fun getItemId(arg0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val cellView: View
        val cell: Cell
        val country = countries[position]

        if (convertView == null) {
            cell = Cell()
            cellView = inflater.inflate(R.layout.row, parent, false)
            cell.tvCode = cellView.findViewById(R.id.code)
            cell.textView = cellView.findViewById(R.id.row_title)
            cellView.tag = cell
        } else {
            cellView = convertView
            cell = cellView.tag as Cell
        }

        cell.tvCode?.text = country.dialCode
        cell.textView?.text = country.name

        return cellView
    }

    internal class Cell {
        var tvCode: TextView? = null
        var textView: TextView? = null
        var imageView: ImageView? = null
    }
}
