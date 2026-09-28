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
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelListener

class MappingEntryPanel : JPanel(), FontMapChangeListener, ItemListener, ListSelectionListener, DocumentListener {
    private var fontMap: FontMap? = null
    private var entryTable: JTable? = null
    private var followedCombo: JComboBox<String>? = null
    private var precededCombo: JComboBox<String>? = null
    private var sym1Combo: JComboBox<String>? = null
    private var sym2Combo: JComboBox<String>? = null
    var splitPane: JSplitPane? = null
        private set
    var mappingTableModel: MappingTableModel? = null
        private set
    var mappingRules: MappingRulesPanel? = null
        private set
    var word1: FontChangeTextField? = null
        private set
    var word2: FontChangeTextField? = null
        private set
    var clearButton: DocumentListenerButton? = null
        private set
    private var addButton: DocumentListenerButton? = null
    private var directMapPopupListener: MappingPopupListener? = null
    var entryAction: EntryAction? = null
        private set

    fun init() {
        val titledBorder = BorderFactory.createTitledBorder(Resources.getResource(Resources.TITLE_MAPPING))
        titledBorder.titleJustification = TitledBorder.CENTER
        border = titledBorder
        val gridBagLayout = GridBagLayout()
        layout = gridBagLayout
        val gridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.BOTH
        gridBagConstraints.weightx = 1.0
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER
        val preview = createWordEntryPanel()
        gridBagLayout.setConstraints(preview, gridBagConstraints)
        add(preview)

        splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        splitPane!!.isOneTouchExpandable = false
        splitPane!!.setDividerLocation(0.7)
        splitPane!!.dividerSize = 0
        splitPane!!.resizeWeight = 1.0

        initTable()

        val scroller = JScrollPane(entryTable)
        gridBagConstraints.weighty = 1.0
        gridBagLayout.setConstraints(splitPane, gridBagConstraints)
        splitPane!!.topComponent = scroller

        mappingRules = MappingRulesPanel()
        mappingRules!!.init()
        mappingTableModel!!.addTableModelListener(mappingRules)
        entryTable!!.selectionModel.addListSelectionListener(this)
        entryTable!!.selectionModel.addListSelectionListener(mappingRules)

        splitPane!!.bottomComponent = mappingRules
        add(splitPane)
        word1!!.requestFocus()
    }

    fun load() {
        mappingRules!!.load()
        directMapPopupListener!!.load()
        loadTable()
        entryTable!!.selectionModel.addListSelectionListener(this)
        entryTable!!.selectionModel.addListSelectionListener(mappingRules)
        word1!!.requestFocus()
    }

    private fun initTable() {
        mappingTableModel = MappingTableModel()
        entryTable = JTable(mappingTableModel)
        entryTable!!.setDefaultRenderer(Any::class.java, MappingTableRenderer())
        entryTable!!.cellSelectionEnabled = true
        entryTable!!.rowSelectionAllowed = true
        entryTable!!.columnSelectionAllowed = false
        entryTable!!.showVerticalLines = false
        entryTable!!.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        entryTable!!.tableHeader.reorderingAllowed = false
        
        sym1Combo = JComboBox()
        sym1Combo!!.isEditable = true
        sym2Combo = JComboBox()
        sym2Combo!!.isEditable = true
        followedCombo = JComboBox()
        followedCombo!!.isEditable = true
        precededCombo = JComboBox()
        precededCombo!!.isEditable = true

        entryTable!!.columnModel.getColumn(0).cellEditor = DefaultCellEditor(sym1Combo)
        entryTable!!.columnModel.getColumn(2).cellEditor = DefaultCellEditor(sym2Combo)
        entryTable!!.columnModel.getColumn(5).cellEditor = DefaultCellEditor(followedCombo)
        entryTable!!.columnModel.getColumn(6).cellEditor = DefaultCellEditor(precededCombo)

        directMapPopupListener = MappingPopupListener()
        entryTable!!.addMouseListener(directMapPopupListener)
        mappingTableModel!!.addTableModelListener(mappingRules)
        mappingTableModel!!.addTableModelListener(word1)
    }

    fun loadTable() {
        addTableModelListeners()
        setTableColumnWidth()
    }

    private fun addTableModelListeners() {
        val actions = MenuHandler.actions.values
        for (action in actions) {
            if (action is TableModelListenerAction) {
                addTableModelListener(action)
            }
            if (action is TableRowsSelectAction) {
                addListSelectionListener(action)
                action.table = entryTable
            }
        }
    }

    fun addTableModelListener(tableModelListener: TableModelListener) {
        mappingTableModel!!.addTableModelListener(tableModelListener)
    }

    fun addListSelectionListener(listSelectionListener: ListSelectionListener) {
        entryTable!!.selectionModel.addListSelectionListener(listSelectionListener)
    }

    private fun setTableColumnWidth() {
        val count = mappingTableModel!!.columnCount
        for (i in 0 until count) {
            entryTable!!.columnModel.getColumn(i).preferredWidth = mappingTableModel!!.getColumnName(i).length
            entryTable!!.columnModel.getColumn(i).sizeWidthToFit()
            entryTable!!.tableHeader.columnModel.getColumn(i).preferredWidth = mappingTableModel!!.getColumnName(i).length
            entryTable!!.tableHeader.columnModel.getColumn(i).sizeWidthToFit()
        }
    }

    fun setFontMap(fontMap: FontMap) {
        this.fontMap = fontMap
        reset()
        firePreviewTableDataChanged()
        sym1Combo!!.updateUI()
        sym2Combo!!.updateUI()
        precededCombo!!.updateUI()
        followedCombo!!.updateUI()
        updateUI()
    }

    private fun reset() {
        clear()
        word1!!.font = fontMap!!.font1
        word2!!.font = fontMap!!.font2
        precededCombo!!.addItem(Resources.EMPTY_STRING)
        followedCombo!!.addItem(Resources.EMPTY_STRING)
        sym1Combo!!.addItem(Resources.EMPTY_STRING)
        sym2Combo!!.addItem(Resources.EMPTY_STRING)
        
        var iterator = fontMap!!.entries.getAllWords()
        while (iterator.hasNext()) {
            val next = iterator.next()
            sym2Combo!!.addItem(next)
            precededCombo!!.addItem(next)
            followedCombo!!.addItem(next)
        }
        
        iterator = fontMap!!.entries.getWord2()
        while (iterator.hasNext()) {
            sym1Combo!!.addItem(iterator.next())
        }

        sym1Combo!!.font = fontMap!!.font1
        sym2Combo!!.font = fontMap!!.font2
        precededCombo!!.font = fontMap!!.font1
        followedCombo!!.font = fontMap!!.font1

        mappingTableModel!!.setFontMap(fontMap)
        val fontPreviewTableRenderer = MappingTableRenderer()
        fontPreviewTableRenderer.setFontMap(fontMap)
        entryTable!!.setDefaultRenderer(Any::class.java, fontPreviewTableRenderer)
        setTableColumnWidth()
    }

    fun clear() {
        sym1Combo!!.removeAllItems()
        sym2Combo!!.removeAllItems()
        followedCombo!!.removeAllItems()
        precededCombo!!.removeAllItems()
    }

    private fun createWordEntryPanel(): JPanel {
        val jPanel = JPanel()
        jPanel.layout = BoxLayout(jPanel, BoxLayout.X_AXIS)

        word1 = FontChangeTextField()
        word1!!.horizontalAlignment = JTextField.RIGHT
        jPanel.add(word1)

        val jLabel = JLabel(" = ")
        jLabel.horizontalAlignment = JLabel.CENTER
        jPanel.add(jLabel)

        word2 = FontChangeTextField()
        word2!!.horizontalAlignment = JTextField.LEFT
        jPanel.add(word2)

        addButton = DocumentListenerButton()
        val sAdd = Resources.getResource(Resources.LABEL_ADD)
        addButton!!.text = sAdd
        addButton!!.isEnabled = false

        entryAction = EntryAction()
        entryAction!!.putValue(Action.NAME, sAdd)
        entryAction!!.putValue(Action.SHORT_DESCRIPTION, "Add Mapping")
        entryAction!!.putValue(Action.MNEMONIC_KEY, 'A'.code)
        entryAction!!.putValue(Action.ACTION_COMMAND_KEY, sAdd)
        entryAction!!.setFontEntryPanel(this)

        addButton!!.addActionListener(entryAction)
        addButton!!.addKeyListener(entryAction)
        jPanel.add(addButton)

        clearButton = DocumentListenerButton()
        val sClear = Resources.getResource(Resources.LABEL_CLEAR)
        clearButton!!.text = sClear
        clearButton!!.isEnabled = false

        val clearFontMapEntryInPreviewAction = EntryClearAction()
        clearFontMapEntryInPreviewAction.putValue(Action.NAME, sClear)
        clearFontMapEntryInPreviewAction.putValue(Action.SHORT_DESCRIPTION, "Clear Mapping")
        clearFontMapEntryInPreviewAction.putValue(Action.MNEMONIC_KEY, 'C'.code)
        clearFontMapEntryInPreviewAction.putValue(Action.ACTION_COMMAND_KEY, sClear)
        clearFontMapEntryInPreviewAction.setFontPreviewPanel(this)

        clearButton!!.addActionListener(clearFontMapEntryInPreviewAction)
        jPanel.add(clearButton)

        word1!!.document.addDocumentListener(clearButton)
        word2!!.document.addDocumentListener(clearButton)
        word1!!.document.addDocumentListener(this)
        word2!!.document.addDocumentListener(this)
        word2!!.addKeyListener(entryAction)

        return jPanel
    }

    val listSelectionModel: ListSelectionModel
        get() = entryTable!!.selectionModel

    fun clearPreviewDisplay() {
        word1!!.text = Resources.EMPTY_STRING
        word2!!.text = Resources.EMPTY_STRING
    }

    private fun firePreviewTableDataChanged() {
        clearPreviewDisplay()
        (entryTable!!.model as MappingTableModel).fireTableDataChanged()
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        val fm = e.fontMap
        if (fm != null) {
            setFontMap(fm)
        }
    }

    override fun itemStateChanged(e: ItemEvent) {
        reset()
    }

    override fun valueChanged(e: ListSelectionEvent) {
        val listSelectionModel = e.source as ListSelectionModel
        val row = listSelectionModel.minSelectionIndex
        if (row > -1) {
            showEntry(mappingTableModel!!.getValueAt(row)!!)
        }
    }

    private fun showEntry(valueAt: FontMapEntry) {
        word1!!.text = valueAt.from
        word2!!.text = valueAt.to
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
        addButton!!.isEnabled = word1!!.text.isNotEmpty() && word2!!.text.isNotEmpty()
    }
}
