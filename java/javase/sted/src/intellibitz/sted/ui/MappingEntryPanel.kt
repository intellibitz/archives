/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 *
 * Visit http://www.gnu.org/ for GPL License terms.
 */

/**
 * $Id: MappingEntryPanel.kt 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/MappingEntryPanel.kt $
 */

package intellibitz.sted.ui

import intellibitz.sted.actions.EntryAction
import intellibitz.sted.actions.EntryClearAction
import intellibitz.sted.actions.TableModelListenerAction
import intellibitz.sted.actions.TableRowsSelectAction
import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.event.MappingPopupListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.DocumentListenerButton
import intellibitz.sted.widgets.FontChangeTextField
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.DefaultCellEditor
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.border.TitledBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelListener
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.util.Iterator
import java.util.Map

class MappingEntryPanel : JPanel(), FontMapChangeListener, ItemListener, ListSelectionListener, DocumentListener {
    private var fontMap: FontMap? = null
    private var entryTable: JTable? = null
    private var followedCombo: JComboBox? = null
    private var precededCombo: JComboBox? = null
    private var sym1Combo: JComboBox? = null
    private var sym2Combo: JComboBox? = null
    private var splitPane: JSplitPane? = null
    private var mappingTableModel: MappingTableModel? = null
    private var mappingRules: MappingRulesPanel? = null
    private var word1: FontChangeTextField? = null
    private var word2: FontChangeTextField? = null
    private var clearButton: DocumentListenerButton? = null
    private var addButton: DocumentListenerButton? = null
    private var directMapPopupListener: MappingPopupListener? = null
    private var entryAction: EntryAction? = null
    fun init() {
        val titledBorder: TitledBorder = BorderFactory.createTitledBorder(Resources.getResource(Resources.TITLE_MAPPING))
        titledBorder.setTitleJustification(TitledBorder.CENTER)
        setBorder(titledBorder)
        val gridBagLayout: GridBagLayout = GridBagLayout()
        setLayout(gridBagLayout)
        val gridBagConstraints: GridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.BOTH
        gridBagConstraints.weightx = 1
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER
        val preview: JPanel = createWordEntryPanel()
        gridBagLayout.setConstraints(preview, gridBagConstraints)
        add(preview)
        splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        splitPane.setOneTouchExpandable(false)
        splitPane.setDividerLocation(0.7d)
        splitPane.setDividerSize(0)
        splitPane.setResizeWeight(1)
        initTable()
        val scroller: JScrollPane = JScrollPane(entryTable)
        gridBagConstraints.weighty = 1
        gridBagLayout.setConstraints(splitPane, gridBagConstraints)
        splitPane.setTopComponent(scroller)
        mappingRules = MappingRulesPanel()
        mappingRules.init()
        mappingTableModel.addTableModelListener(mappingRules)
        entryTable.getSelectionModel()
        entryTable.getSelectionModel()
        splitPane.setBottomComponent(mappingRules)
        add(splitPane)
        word1.requestFocus()
    }
    fun load() {
        mappingRules.load()
        directMapPopupListener.load()
        loadTable()
        entryTable.getSelectionModel()
        entryTable.getSelectionModel()
        word1.requestFocus()
    }
    private fun initTable() {
        mappingTableModel = MappingTableModel()
        entryTable = JTable(mappingTableModel)
        entryTable.setDefaultRenderer(Object::class.java, MappingTableRenderer())
        entryTable.setCellSelectionEnabled(true)
        entryTable.setColumnSelectionAllowed(false)
        entryTable.setShowVerticalLines(false)
        entryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS)
        entryTable.getTableHeader()
        sym1Combo = JComboBox()
        sym1Combo.setEditable(true)
        sym2Combo = JComboBox()
        sym2Combo.setEditable(true)
        followedCombo = JComboBox()
        followedCombo.setEditable(true)
        precededCombo = JComboBox()
        precededCombo.setEditable(true)
        entryTable.getColumnModel()
        entryTable.getColumnModel()
        entryTable.getColumnModel()
        entryTable.getColumnModel()
        directMapPopupListener = MappingPopupListener()
        entryTable.addMouseListener(directMapPopupListener)
        mappingTableModel.addTableModelListener(mappingRules)
        mappingTableModel.addTableModelListener(word1)
    }
    fun loadTable() {
        addTableModelListeners()
        setTableColumnWidth()
    }
    fun getMappingRules(): MappingRulesPanel {
        return mappingRules
    }
    private fun addTableModelListeners() {
        val actions: Map<String, Action> = MenuHandler.getInstance()
        for (action in actions.values()) {
            if (TableModelListenerAction::class.java) {
                addTableModelListener((action as TableModelListener))
            }
            if (TableRowsSelectAction::class.java) {
                addListSelectionListener((action as ListSelectionListener))
                (action as TableRowsSelectAction)
            }
        }
    }
    fun addTableModelListener(tableModelListener: TableModelListener) {
        mappingTableModel.addTableModelListener(tableModelListener)
    }
    fun addListSelectionListener(listSelectionListener: ListSelectionListener) {
        entryTable.getSelectionModel()
    }
    private fun setTableColumnWidth() {
        val count: Int = mappingTableModel.getColumnCount()
        var i: Int = 0
        while ((i < count)) {
            when (i) {
                else -> {
                    entryTable.getColumnModel()
                    entryTable.getColumnModel()
                    entryTable.getTableHeader()
                    entryTable.getTableHeader()
                }
            }
            i++
        }
    }
    fun setFontMap(fontMap: FontMap) {
        this = fontMap
        reset()
        firePreviewTableDataChanged()
        sym1Combo.updateUI()
        sym2Combo.updateUI()
        precededCombo.updateUI()
        followedCombo.updateUI()
        updateUI()
    }
    private fun reset() {
        clear()
        word1.setFont(fontMap.getFont1())
        word2.setFont(fontMap.getFont2())
        precededCombo.addItem(Resources.EMPTY_STRING)
        followedCombo.addItem(Resources.EMPTY_STRING)
        sym1Combo.addItem(Resources.EMPTY_STRING)
        sym2Combo.addItem(Resources.EMPTY_STRING)
        val iterator: Iterator<String> = fontMap.getEntries()
        while (iterator.hasNext()) {
            val next: Object = iterator.next()
            sym2Combo.addItem(next)
            precededCombo.addItem(next)
            followedCombo.addItem(next)
        }
        val iter: Iterator<String> = fontMap.getEntries()
        while (iter.hasNext()) {
            sym1Combo.addItem(iter.next())
        }
        sym1Combo.setFont(fontMap.getFont1())
        sym2Combo.setFont(fontMap.getFont2())
        precededCombo.setFont(fontMap.getFont1())
        followedCombo.setFont(fontMap.getFont1())
        mappingTableModel.setFontMap(fontMap)
        val fontPreviewTableRenderer: MappingTableRenderer = MappingTableRenderer()
        fontPreviewTableRenderer.setFontMap(fontMap)
        entryTable.setDefaultRenderer(Object::class.java, fontPreviewTableRenderer)
        setTableColumnWidth()
    }
    fun clear() {
        sym1Combo.removeAllItems()
        sym2Combo.removeAllItems()
        followedCombo.removeAllItems()
        precededCombo.removeAllItems()
    }
    private fun createWordEntryPanel(): JPanel {
        val jPanel: JPanel = JPanel()
        jPanel.setLayout(BoxLayout(jPanel, BoxLayout.X_AXIS))
        word1 = FontChangeTextField()
        word1.setHorizontalAlignment(JTextField.RIGHT)
        jPanel.add(word1)
        val jLabel: JLabel = JLabel(" = ")
        jLabel.setHorizontalAlignment(JLabel.CENTER)
        jPanel.add(jLabel)
        word2 = FontChangeTextField()
        word2.setHorizontalAlignment(JTextField.LEFT)
        jPanel.add(word2)
        addButton = DocumentListenerButton()
        val sAdd: String = Resources.getResource(Resources.LABEL_ADD)
        addButton.setText(sAdd)
        addButton.setEnabled(false)
        entryAction = EntryAction()
        entryAction.putValue(Action.NAME, sAdd)
        entryAction.putValue(Action.SHORT_DESCRIPTION, "Add Mapping")
        entryAction.putValue(Action.MNEMONIC_KEY, ('A' as Int))
        entryAction.putValue(Action.ACTION_COMMAND_KEY, sAdd)
        entryAction.setFontEntryPanel(this)
        addButton.addActionListener(entryAction)
        addButton.addKeyListener(entryAction)
        jPanel.add(addButton)
        clearButton = DocumentListenerButton()
        val sClear: String = Resources.getResource(Resources.LABEL_CLEAR)
        clearButton.setText(sClear)
        clearButton.setEnabled(false)
        val clearFontMapEntryInPreviewAction: EntryClearAction = EntryClearAction()
        clearFontMapEntryInPreviewAction.putValue(Action.NAME, sClear)
        clearFontMapEntryInPreviewAction.putValue(Action.SHORT_DESCRIPTION, "Clear Mapping")
        clearFontMapEntryInPreviewAction.putValue(Action.MNEMONIC_KEY, ('C' as Int))
        clearFontMapEntryInPreviewAction.putValue(Action.ACTION_COMMAND_KEY, sClear)
        clearFontMapEntryInPreviewAction.setFontPreviewPanel(this)
        clearButton.addActionListener(clearFontMapEntryInPreviewAction)
        jPanel.add(clearButton)
        word1.getDocument()
        word2.getDocument()
        word1.getDocument()
        word2.getDocument()
        word2.addKeyListener(entryAction)
        return jPanel
    }
    fun getMappingTableModel(): MappingTableModel {
        return mappingTableModel
    }
    fun getListSelectionModel(): ListSelectionModel {
        return entryTable.getSelectionModel()
    }
    fun getClearButton(): DocumentListenerButton {
        return clearButton
    }
    fun getEntryAction(): EntryAction {
        return entryAction
    }
    fun clearPreviewDisplay() {
        word1.setText(Resources.EMPTY_STRING)
        word2.setText(Resources.EMPTY_STRING)
    }
    private fun firePreviewTableDataChanged() {
        clearPreviewDisplay()
        (entryTable.getModel() as MappingTableModel)
    }
    fun getSplitPane(): JSplitPane {
        return splitPane
    }
    fun getWord1(): FontChangeTextField {
        return word1
    }
    fun getWord2(): FontChangeTextField {
        return word2
    }
    override fun stateChanged(e: FontMapChangeEvent) {
        setFontMap(e.getFontMap())
    }
    override fun itemStateChanged(e: ItemEvent) {
        reset()
    }
    override fun valueChanged(e: ListSelectionEvent) {
        val listSelectionModel: ListSelectionModel = (e.getSource() as ListSelectionModel)
        val row: Int = listSelectionModel.getMinSelectionIndex()
        if ((row > 1)) {
            showEntry(mappingTableModel.getValueAt(row))
        }
    }
    private fun showEntry(valueAt: FontMapEntry) {
        word1.setText(valueAt.getFrom())
        word2.setText(valueAt.getTo())
    }
    override fun changedUpdate(e: DocumentEvent) {
        toggleAdd()
    }
    override fun insertUpdate(e: DocumentEvent) {
        toggleAdd()
    }
    override fun removeUpdate(e: DocumentEvent) {
        toggleAdd()
    }
    private fun toggleAdd() {
        addButton.setEnabled(((word1.getText() > 0) && (word2.getText() > 0)))
    }
}
