package intellibitz.sted.ui

import intellibitz.sted.fontmap.FontMap
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

internal class MappingTableRenderer : DefaultTableCellRenderer() {
    private var fontMap: FontMap? = null

    fun setFontMap(fontMap: FontMap?) {
        this.fontMap = fontMap
    }

    override fun getTableCellRendererComponent(
        table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val renderer = super.getTableCellRendererComponent(
            table, value, isSelected,
            hasFocus, row, column
        ) as DefaultTableCellRenderer
        
        fontMap?.let {
            when (column) {
                0 -> {
                    renderer.font = it.font1
                    renderer.horizontalAlignment = JLabel.RIGHT
                }
                1 -> {
                    renderer.horizontalAlignment = JLabel.CENTER
                    renderer.isFocusable = false
                }
                2 -> {
                    renderer.font = it.font2
                    renderer.horizontalAlignment = JLabel.LEFT
                }
                5, 6 -> renderer.font = it.font1
                else -> renderer.horizontalAlignment = JLabel.CENTER
            }
        }
        return renderer
    }
}
