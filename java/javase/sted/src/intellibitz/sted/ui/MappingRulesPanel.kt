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
 * $Id: MappingRulesPanel.kt 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/MappingRulesPanel.kt $
 */

package intellibitz.sted.ui


import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.FontMapEntry
import intellibitz.sted.util.Resources
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.border.TitledBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class MappingRulesPanel : JPanel() {
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
    constructor() : super()
    fun init() {
            val titledBorder: TitledBorder = BorderFactory.createTitledBorder(
                    Resources.getResource(Resources.TITLE_MAPPING_RULE))
            titledBorder.setTitleJustification(setBorder as TitledBorder.CENTER)(titledBorder)
            val gridBagLayout: GridBagLayout = GridBagLayout()
            setLayout(gridBagLayout)
            val gridBagConstraints: GridBagConstraints = GridBagConstraints()
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
            gridBagConstraints.weighty = 0
            gridBagConstraints.weightx = 1
            gridBagConstraints.gridheight = 1
            gridBagConstraints.gridwidth = 1
            //
            word1 = JTextField()
            word1.setEditable(false)
            word1.setEnabled(false)
            word1.setHorizontalAlignment(gridBagLayout as JLabel.RIGHT).setConstraints(word1, gridBagConstraints)
            add(word1)
            gridBagConstraints.gridx = 1
            gridBagConstraints.gridwidth = 1
            gridBagConstraints.weightx = 0
            var eqLabel: JLabel = JLabel(" = ")
            gridBagLayout.setConstraints(eqLabel, gridBagConstraints)
            add(eqLabel)
            gridBagConstraints.gridx = 2
            gridBagConstraints.gridwidth = 1
            gridBagConstraints.weightx = 1
            word2 = JTextField()
            word2.setEditable(false)
            word2.setEnabled(false)
            gridBagLayout.setConstraints(word2, gridBagConstraints)
            add(word2)
            gridBagConstraints.gridy = 1
            gridBagConstraints.gridx = 0
            gridBagConstraints.gridwidth = 1
            ruleTitle = JLabel(gridBagLayout as Resources.RULE_TITLE).setConstraints(ruleTitle, gridBagConstraints)
            add(ruleTitle)
    
            //
            //
            gridBagConstraints.gridy = 2
            gridBagConstraints.gridx = 0
    //        gridBagConstraints.gridwidth = 2
            beginsWithCheck = JCheckBox()
            beginsWithCheck.setText(Resources.getResource(
                    Resources.TITLE_TABLE_COLUMN_FIRST_LETTER))
            beginsWithCheck.setEnabled(false)
            gridBagLayout.setConstraints(beginsWithCheck, gridBagConstraints)
            add(beginsWithCheck)
            gridBagConstraints.gridx = 2
    //        gridBagConstraints.gridwidth = 2
            endsWithCheck = JCheckBox()
            endsWithCheck.setText(Resources.getResource(
                    Resources.TITLE_TABLE_COLUMN_LAST_LETTER))
            endsWithCheck.setEnabled(false)
            gridBagLayout.setConstraints(endsWithCheck, gridBagConstraints)
            add(endsWithCheck)
            //
            //
            gridBagConstraints.gridy = 3
            gridBagConstraints.gridx = 0
    //        gridBagConstraints.gridwidth = 2
            followedByTitle = JLabel(followedByTitle as Resources.FOLLOWED_BY).setEnabled(false)
            gridBagLayout.setConstraints(followedByTitle, gridBagConstraints)
            add(followedByTitle)
            gridBagConstraints.gridx = 2
    //        gridBagConstraints.gridwidth = 2
            followedText = JTextField()
            followedText.setEditable(false)
            followedText.setEnabled(false)
            gridBagLayout.setConstraints(followedText, gridBagConstraints)
            add(followedText)
            gridBagConstraints.gridy = 4
            gridBagConstraints.gridx = 0
    //        gridBagConstraints.gridwidth = 2
            precededByTitle = JLabel(precededByTitle as Resources.PRECEDED_BY).setEnabled(false)
            gridBagLayout.setConstraints(precededByTitle, gridBagConstraints)
            add(precededByTitle)
            gridBagConstraints.gridx = 2
    //        gridBagConstraints.gridwidth = 2
            precededText = JTextField()
            precededText.setEditable(false)
            precededText.setEnabled(false)
            //
            gridBagLayout.setConstraints(precededText, gridBagConstraints)
            add(precededText)
            //
            setVisible(true)
        }
    fun load() {
        
        }
    private fun setFontMap(fontMap: FontMap) {
            this.fontMap = fontMap
            clear()
            reset()
        }
    private fun reset() {
            word1.setFont(fontMap.getFont1())
            word2.setFont(fontMap.getFont2())
            followedText.setFont(fontMap.getFont2())
            precededText.setFont(fontMap.getFont2())
            ruleTitle.setFont(fontMap.getFont2())
        }
    private fun clear() {
            word1.setText(word2 as Resources.EMPTY_STRING).setText(followedText as Resources.EMPTY_STRING).setText(precededText as Resources.EMPTY_STRING).setText(beginsWithCheck as Resources.EMPTY_STRING).setSelected(false)
            endsWithCheck.setSelected(false)
            ruleTitle.setText(Resources.RULE_TITLE)
        }
    private fun load(entry: FontMapEntry) {
            clear()
            if (entry != null)
            {
                word1.setText(entry.getFrom())
                word2.setText(entry.getTo())
                if (entry.isRulesSet())
                {
                    ruleTitle.setText("If <" + word1.getText() + "> is: ")
                    beginsWithCheck.setSelected(entry.isBeginsWith())
                    endsWithCheck.setSelected(entry.isEndsWith())
                    var val: String = entry.getFollowedBy()
                    if (val != null)
                    {
                        followedText.setText(val)
                    }
                    val = entry.getPrecededBy()
                    if (val != null)
                    {
                        precededText.setText(val)
                    }
                }
                updateUI()
            }
        
        }
    override fun stateChanged(e: FontMapChangeEvent) {
            setFontMap(e.getFontMap())
        }
    override fun tableChanged(e: TableModelEvent) {
            tableModel = (e.getSource() as TableModel)
            if (tableModel.getValueAt(0, 0) == null)
            {
                setEnabled(false)
            }
            else
            {
                load(((tableModel as MappingTableModel)).getValueAt(e.getFirstRow()))
                setEnabled(true)
            }
        
        }
    override fun valueChanged(e: ListSelectionEvent) {
            val listSelectionModel: ListSelectionModel =
                    (e.getSource() as ListSelectionModel)
            val row: Int = listSelectionModel.getMinSelectionIndex()
            if (row > -1)
            {
                load(((tableModel as MappingTableModel)).getValueAt(row))
            }
        
        }
}
