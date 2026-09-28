package intellibitz.intellidroid.company

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import intellibitz.intellidroid.R
import intellibitz.intellidroid.bean.Company

class CompanyListAdapter(
    private var context: Context?,
    var companies: List<Company>
) : BaseAdapter() {

    var inflater: LayoutInflater? = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater

    private fun getResId(drawableName: String): Int {
        try {
            val res = R.drawable::class.java
            val field = res.getField(drawableName)
            return field.getInt(null)
        } catch (e: Exception) {
            Log.e("CompanyCodePicker", "Failure to get drawable id.", e)
        }
        return -1
    }

    override fun getCount(): Int {
        return companies.size
    }

    override fun getItem(arg0: Int): Any {
        return companies[arg0]
    }

    override fun getItemId(arg0: Int): Long {
        return companies[arg0].typeCode?.hashCode()?.toLong() ?: 0L
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var cellView = convertView
        val cell: Cell
        val company = companies[position]

        if (convertView == null) {
            cell = Cell()
            cellView = inflater?.inflate(R.layout.row, null)
            cell.tvCode = cellView?.findViewById(R.id.code)
            cell.textView = cellView?.findViewById(R.id.row_title)
            cellView?.tag = cell
        } else {
            cell = cellView?.tag as Cell
        }

        cell.tvCode?.text = company.typeCode
        cell.textView?.text = company.type

        return cellView!!
    }

    internal class Cell {
        @JvmField
        var tvCode: TextView? = null
        @JvmField
        var textView: TextView? = null
        @JvmField
        var imageView: ImageView? = null
    }
}
