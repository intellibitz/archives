package intellibitz.sted.ui

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.Resources

import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel
import java.awt.*

class MappingRulesPanel : JPanel(), TableModelListener, FontMapChangeListener, ListSelectionListener {
    private var fontMap: FontMap? = null
    private var followedText: JTextField? = null
    private var precededText: JTextField? = null
    private var word2: JTextField? = null
    private var word1: JTextField? = null
    private var beginsWithCheck: JCheckBox? = null
    private var endsWithCheck: JCheckBox? = null
    private var tableModel: TableModel? = null
    private var ruleTitle: JLabel? = null
    private var followedByTitle: JLabel? = null
    private var precededByTitle: JLabel? = null

    fun init() {
        val titledBorder = BorderFactory.createTitledBorder(Resources.getResource(Resources.TITLE_MAPPING_RULE))
        titledBorder.titleJustification = TitledBorder.CENTER
        border = titledBorder

        val gridBagLayout = GridBagLayout()
        layout = gridBagLayout

        val gridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
        gridBagConstraints.weighty = 0.0
        gridBagConstraints.weightx = 1.0
        gridBagConstraints.gridheight = 1
        gridBagConstraints.gridwidth = 1
        
        word1 = JTextField()
        word1!!.isEditable = false
        word1!!.isEnabled = false
        word1!!.horizontalAlignment = SwingConstants.RIGHT
        gridBagLayout.setConstraints(word1, gridBagConstraints)
        add(word1)

        gridBagConstraints.gridx = 1
        gridBagConstraints.gridwidth = 1
        gridBagConstraints.weightx = 0.0
        val eqLabel = JLabel(" = ")
        gridBagLayout.setConstraints(eqLabel, gridBagConstraints)
        add(eqLabel)

        gridBagConstraints.gridx = 2
        gridBagConstraints.gridwidth = 1
        gridBagConstraints.weightx = 1.0
        word2 = JTextField()
        word2!!.isEditable = false
        word2!!.isEnabled = false
        gridBagLayout.setConstraints(word2, gridBagConstraints)
        add(word2)

        gridBagConstraints.gridy = 1
        gridBagConstraints.gridx = 0
        gridBagConstraints.gridwidth = 1
        ruleTitle = JLabel(Resources.RULE_TITLE)
        gridBagLayout.setConstraints(ruleTitle, gridBagConstraints)
        add(ruleTitle)

        gridBagConstraints.gridy = 2
        gridBagConstraints.gridx = 0
        beginsWithCheck = JCheckBox()
        beginsWithCheck!!.text = Resources.getResource(Resources.TITLE_TABLE_COLUMN_FIRST_LETTER)
        beginsWithCheck!!.isEnabled = false
        gridBagLayout.setConstraints(beginsWithCheck, gridBagConstraints)
        add(beginsWithCheck)

        gridBagConstraints.gridx = 2
        endsWithCheck = JCheckBox()
        endsWithCheck!!.text = Resources.getResource(Resources.TITLE_TABLE_COLUMN_LAST_LETTER)
        endsWithCheck!!.isEnabled = false
        gridBagLayout.setConstraints(endsWithCheck, gridBagConstraints)
        add(endsWithCheck)

        gridBagConstraints.gridy = 3
        gridBagConstraints.gridx = 0
        followedByTitle = JLabel(Resources.FOLLOWED_BY)
        followedByTitle!!.isEnabled = false
        gridBagLayout.setConstraints(followedByTitle, gridBagConstraints)
        add(followedByTitle)

        gridBagConstraints.gridx = 2
        followedText = JTextField()
        followedText!!.isEditable = false
        followedText!!.isEnabled = false
        gridBagLayout.setConstraints(followedText, gridBagConstraints)
        add(followedText)

        gridBagConstraints.gridy = 4
        gridBagConstraints.gridx = 0
        precededByTitle = JLabel(Resources.PRECEDED_BY)
        precededByTitle!!.isEnabled = false
        gridBagLayout.setConstraints(precededByTitle, gridBagConstraints)
        add(precededByTitle)

        gridBagConstraints.gridx = 2
        precededText = JTextField()
        precededText!!.isEditable = false
        precededText!!.isEnabled = false
        gridBagLayout.setConstraints(precededText, gridBagConstraints)
        add(precededText)
        
        isVisible = true
    }

    fun load() {}

    private fun setFontMap(fontMap: FontMap?) {
        this.fontMap = fontMap
        clear()
        reset()
    }

    private fun reset() {
        fontMap?.let {
            word1!!.font = it.font1
            word2!!.font = it.font2
            followedText!!.font = it.font2
            precededText!!.font = it.font2
            ruleTitle!!.font = it.font2
        }
    }

    private fun clear() {
        word1!!.text = Resources.EMPTY_STRING
        word2!!.text = Resources.EMPTY_STRING
        followedText!!.text = Resources.EMPTY_STRING
        precededText!!.text = Resources.EMPTY_STRING
        beginsWithCheck!!.isSelected = false
        endsWithCheck!!.isSelected = false
        ruleTitle!!.text = Resources.RULE_TITLE
    }

    private fun load(entry: FontMapEntry?) {
        clear()
        if (entry != null) {
            word1!!.text = entry.from
            word2!!.text = entry.to
            if (entry.isRulesSet()) {
                ruleTitle!!.text = "If <" + word1!!.text + "> is: "
                beginsWithCheck!!.isSelected = entry.beginsWith
                endsWithCheck!!.isSelected = entry.endsWith
                var valStr = entry.followedBy
                if (valStr != null) {
                    followedText!!.text = valStr
                }
                valStr = entry.precededBy
                if (valStr != null) {
                    precededText!!.text = valStr
                }
            }
            updateUI()
        }
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        setFontMap(e.fontMap)
    }

    override fun tableChanged(e: TableModelEvent) {
        tableModel = e.source as TableModel
        if (tableModel!!.getValueAt(0, 0) == null) {
            isEnabled = false
        } else {
            load((tableModel as MappingTableModel).getValueAt(e.firstRow))
            isEnabled = true
        }
    }

    override fun valueChanged(e: ListSelectionEvent) {
        val listSelectionModel = e.source as ListSelectionModel
        val row = listSelectionModel.minSelectionIndex
        if (row > -1 && tableModel != null) {
            load((tableModel as MappingTableModel).getValueAt(row))
        }
    }
}
