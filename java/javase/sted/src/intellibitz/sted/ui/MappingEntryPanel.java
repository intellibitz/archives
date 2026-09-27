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
 * $Id: MappingEntryPanel.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/MappingEntryPanel.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.actions.EntryAction;
import intellibitz.sted.actions.EntryClearAction;
import intellibitz.sted.actions.TableModelListenerAction;
import intellibitz.sted.actions.TableRowsSelectAction;
import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.event.MappingPopupListener;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.fontmap.FontMapEntry;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;
import intellibitz.sted.widgets.DocumentListenerButton;
import intellibitz.sted.widgets.FontChangeTextField;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Map;

public class MappingEntryPanel
        extends JPanel
        implements FontMapChangeListener,
        ItemListener,
        ListSelectionListener,
        DocumentListener
{
    private FontMap fontMap;
    private JTable entryTable;
    private JComboBox followedCombo;
    private JComboBox precededCombo;
    private JComboBox sym1Combo;
    private JComboBox sym2Combo;
    private JSplitPane splitPane;
    private MappingTableModel mappingTableModel;
    private MappingRulesPanel mappingRules;
    private FontChangeTextField word1;
    private FontChangeTextField word2;
    private DocumentListenerButton clearButton;
    private DocumentListenerButton addButton;
    private MappingPopupListener directMapPopupListener;
    private EntryAction entryAction;

    public MappingEntryPanel()
    {
        super();
    }

    public void init()
    {
        final TitledBorder titledBorder = BorderFactory.createTitledBorder(
                Resources.getResource(Resources.TITLE_MAPPING));
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        setBorder(titledBorder);
        final GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        final JPanel preview = createWordEntryPanel();
        gridBagLayout.setConstraints(preview, gridBagConstraints);
        //
        add(preview);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(0.7d);
        splitPane.setDividerSize(0);
        // the top component gets all the extra spaces
        splitPane.setResizeWeight(1);
        initTable();
        final JScrollPane scroller = new JScrollPane(entryTable);
        gridBagConstraints.weighty = 1;
        gridBagLayout.setConstraints(splitPane, gridBagConstraints);
        //
        splitPane.setTopComponent(scroller);
        mappingRules = new MappingRulesPanel();
        mappingRules.init();
        mappingTableModel.addTableModelListener(mappingRules);
        entryTable.getSelectionModel().addListSelectionListener(this);
        entryTable.getSelectionModel().addListSelectionListener(mappingRules);
        //
        splitPane.setBottomComponent(mappingRules);
        add(splitPane);
        word1.requestFocus();
    }

    public void load()
    {
        mappingRules.load();
        directMapPopupListener.load();
        loadTable();
        entryTable.getSelectionModel().addListSelectionListener(this);
        entryTable.getSelectionModel().addListSelectionListener(mappingRules);
        word1.requestFocus();
    }

    private void initTable()
    {
        // Create a model of the data.
        mappingTableModel = new MappingTableModel();
        entryTable = new JTable(mappingTableModel);
        entryTable.setDefaultRenderer
                (Object.class, new MappingTableRenderer());
        entryTable.setCellSelectionEnabled(true);
        entryTable.setColumnSelectionAllowed(false);
        entryTable.setShowVerticalLines(false);
        entryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        entryTable.getTableHeader().setReorderingAllowed(false);
        sym1Combo = new JComboBox();
        sym1Combo.setEditable(true);
        sym2Combo = new JComboBox();
        sym2Combo.setEditable(true);
        followedCombo = new JComboBox();
        followedCombo.setEditable(true);
        precededCombo = new JComboBox();
        precededCombo.setEditable(true);
        entryTable.getColumnModel().getColumn(0)
                .setCellEditor(new DefaultCellEditor(sym1Combo));
        entryTable.getColumnModel().getColumn(2)
                .setCellEditor(new DefaultCellEditor(sym2Combo));
        entryTable.getColumnModel().getColumn(5)
                .setCellEditor(new DefaultCellEditor(followedCombo));
        entryTable.getColumnModel().getColumn(6)
                .setCellEditor(new DefaultCellEditor(precededCombo));
        directMapPopupListener =
                new MappingPopupListener();
        entryTable.addMouseListener(directMapPopupListener);
        mappingTableModel.addTableModelListener(mappingRules);
        mappingTableModel.addTableModelListener(word1);
    }

    public void loadTable()
    {
        addTableModelListeners();
        setTableColumnWidth();
    }

    public MappingRulesPanel getMappingRules()
    {
        return mappingRules;
    }

    private void addTableModelListeners()
    {
        final Map<String, Action> actions =
                MenuHandler.getInstance().getActions();
        for (Action action : actions.values())
        {
            if (TableModelListenerAction.class.isInstance(action))
            {
                addTableModelListener((TableModelListener) action);
            }
            if (TableRowsSelectAction.class.isInstance(action))
            {
                addListSelectionListener((ListSelectionListener) action);
                ((TableRowsSelectAction) action).setTable(entryTable);
            }
        }
    }

    public void addTableModelListener(TableModelListener tableModelListener)
    {
        mappingTableModel.addTableModelListener(tableModelListener);
    }

    public void addListSelectionListener(
            ListSelectionListener listSelectionListener)
    {
        entryTable.getSelectionModel()
                .addListSelectionListener(listSelectionListener);
    }

    private void setTableColumnWidth()
    {
        final int count = mappingTableModel.getColumnCount();
        for (int i = 0; i < count; i++)
        {
            switch (i)
            {
                default:
                    entryTable.getColumnModel().getColumn(i).setPreferredWidth
                            (mappingTableModel.getColumnName(i).length());
                    entryTable.getColumnModel().getColumn(i).sizeWidthToFit();
                    entryTable.getTableHeader().getColumnModel().getColumn(i)
                            .setPreferredWidth
                                    (mappingTableModel
                                            .getColumnName(i).length());
                    entryTable.getTableHeader().getColumnModel().getColumn(i)
                            .sizeWidthToFit();

            }
        }
    }

    public void setFontMap(FontMap fontMap)
    {
        this.fontMap = fontMap;
        reset();
        firePreviewTableDataChanged();
        sym1Combo.updateUI();
        sym2Combo.updateUI();
        precededCombo.updateUI();
        followedCombo.updateUI();
        updateUI();
    }

    private void reset()
    {
        clear();
        word1.setFont(fontMap.getFont1());
        word2.setFont(fontMap.getFont2());
        precededCombo.addItem(Resources.EMPTY_STRING);
        followedCombo.addItem(Resources.EMPTY_STRING);
        sym1Combo.addItem(Resources.EMPTY_STRING);
        sym2Combo.addItem(Resources.EMPTY_STRING);
        final Iterator<String> iterator = fontMap.getEntries().getAllWords();
        while (iterator.hasNext())
        {
            final Object next = iterator.next();
            sym2Combo.addItem(next);
            precededCombo.addItem(next);
            followedCombo.addItem(next);
        }
        final Iterator<String> iter = fontMap.getEntries().getWord2();
        while (iter.hasNext())
        {
            sym1Combo.addItem(iter.next());
        }
        sym1Combo.setFont(fontMap.getFont1());
        sym2Combo.setFont(fontMap.getFont2());
        precededCombo.setFont(fontMap.getFont1());
        followedCombo.setFont(fontMap.getFont1());
        mappingTableModel.setFontMap(fontMap);
        final MappingTableRenderer fontPreviewTableRenderer =
                new MappingTableRenderer();
        fontPreviewTableRenderer.setFontMap(fontMap);
        entryTable.setDefaultRenderer(Object.class, fontPreviewTableRenderer);
        setTableColumnWidth();
    }

    public void clear()
    {
        sym1Combo.removeAllItems();
        sym2Combo.removeAllItems();
        followedCombo.removeAllItems();
        precededCombo.removeAllItems();
    }


    private JPanel createWordEntryPanel()
    {
        final JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        word1 = new FontChangeTextField();
        word1.setHorizontalAlignment(JTextField.RIGHT);
        //
        jPanel.add(word1);
        final JLabel jLabel = new JLabel(" = ");
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        //
        jPanel.add(jLabel);
        word2 = new FontChangeTextField();
        word2.setHorizontalAlignment(JTextField.LEFT);
        //
        jPanel.add(word2);
        addButton = new DocumentListenerButton();
        final String sAdd = Resources.getResource(Resources.LABEL_ADD);
        addButton.setText(sAdd);
        addButton.setEnabled(false);
        entryAction = new EntryAction();
        entryAction.putValue(Action.NAME, sAdd);
        entryAction.putValue(Action.SHORT_DESCRIPTION, "Add Mapping");
        entryAction.putValue(Action.MNEMONIC_KEY, (int) 'A');
        entryAction.putValue(Action.ACTION_COMMAND_KEY, sAdd);
        entryAction.setFontEntryPanel(this);
        addButton.addActionListener(entryAction);
        addButton.addKeyListener(entryAction);
        //
        jPanel.add(addButton);
        clearButton = new DocumentListenerButton();
        final String sClear = Resources.getResource(Resources.LABEL_CLEAR);
        clearButton.setText(sClear);
        clearButton.setEnabled(false);
        final EntryClearAction clearFontMapEntryInPreviewAction =
                new EntryClearAction();
        clearFontMapEntryInPreviewAction.putValue(Action.NAME, sClear);
        clearFontMapEntryInPreviewAction
                .putValue(Action.SHORT_DESCRIPTION, "Clear Mapping");
        clearFontMapEntryInPreviewAction
                .putValue(Action.MNEMONIC_KEY, (int) 'C');
        clearFontMapEntryInPreviewAction
                .putValue(Action.ACTION_COMMAND_KEY, sClear);
        clearFontMapEntryInPreviewAction.setFontPreviewPanel(this);
        clearButton.addActionListener(clearFontMapEntryInPreviewAction);
        //
        jPanel.add(clearButton);

        word1.getDocument().addDocumentListener(clearButton);
        word2.getDocument().addDocumentListener(clearButton);
        word1.getDocument().addDocumentListener(this);
        word2.getDocument().addDocumentListener(this);

        word2.addKeyListener(entryAction);
        return jPanel;
    }

    public MappingTableModel getMappingTableModel()
    {
        return mappingTableModel;
    }

    public ListSelectionModel getListSelectionModel()
    {
        return entryTable.getSelectionModel();
    }

    public DocumentListenerButton getClearButton()
    {
        return clearButton;
    }

    public EntryAction getEntryAction()
    {
        return entryAction;
    }

    public void clearPreviewDisplay()
    {
        word1.setText(Resources.EMPTY_STRING);
        word2.setText(Resources.EMPTY_STRING);
    }

    private void firePreviewTableDataChanged()
    {
        clearPreviewDisplay();
        ((MappingTableModel) entryTable.getModel()).fireTableDataChanged();
    }

    public JSplitPane getSplitPane()
    {
        return splitPane;
    }

    public FontChangeTextField getWord1()
    {
        return word1;
    }

    public FontChangeTextField getWord2()
    {
        return word2;
    }

    public void stateChanged(FontMapChangeEvent e)
    {
        setFontMap(e.getFontMap());
    }

    public void itemStateChanged(ItemEvent e)
    {
        reset();
    }

    /**
     * Called whenever the value of the selection changes.
     *
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e)
    {
        final ListSelectionModel listSelectionModel =
                (ListSelectionModel) e.getSource();
        final int row = listSelectionModel.getMinSelectionIndex();
        if (row > -1)
        {
            showEntry(mappingTableModel.getValueAt(row));
        }
    }

    private void showEntry(FontMapEntry valueAt)
    {
        word1.setText(valueAt.getFrom());
        word2.setText(valueAt.getTo());
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(DocumentEvent e)
    {
        toggleAdd();
    }

    /**
     * Gives notification that there was an insert into the document.  The range
     * given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(DocumentEvent e)
    {
        toggleAdd();
    }

    /**
     * Gives notification that a portion of the document has been removed.  The
     * range is given in terms of what the view last saw (that is, before
     * updating sticky positions).
     *
     * @param e the document event
     */
    public void removeUpdate(DocumentEvent e)
    {
        toggleAdd();
    }

    private void toggleAdd()
    {
        addButton.setEnabled(word1.getText().length() > 0 &&
                word2.getText().length() > 0);
    }

}


