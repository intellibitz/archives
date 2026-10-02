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
 * $Id: FontKeypad.kt 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/FontKeypad.kt $
 */

package intellibitz.sted.ui

import intellibitz.sted.actions.LoadFontAction
import intellibitz.sted.event.FontListChangeEvent
import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.event.IKeypadEventSource
import intellibitz.sted.event.IKeypadListener
import intellibitz.sted.event.KeypadEvent
import intellibitz.sted.fontmap.FontInfo
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.util.Resources
import javax.swing.BorderFactory
import javax.swing.ComboBoxModel
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.EventListenerList
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.Map
import java.util.logging.Logger

abstract class FontKeypad : JPanel(), ItemListener, FontMapChangeListener, IKeypadEventSource {
    private var fontMap: FontMap? = null
    private var fontSelector: FontList? = null
    private var keypad: JPanel? = null
    private var currentFont: Font? = null
    private val keys: ArrayList<JButton> = ArrayList<JButton>()
    private var keypadEvent: KeypadEvent? = null
    private var keypadListeners: EventListenerList? = null
    fun init() {
        val titledBorder: TitledBorder = TitledBorder(Resources.getResource(Resources.TITLE_KEYPAD))
        titledBorder.setTitleJustification(TitledBorder.CENTER)
        setBorder(titledBorder)
        val gridBagLayout: GridBagLayout = GridBagLayout()
        setLayout(gridBagLayout)
        val gridBagConstraints: GridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
        gridBagConstraints.weightx = 0
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER
        var loadFont: JButton = JButton(LoadFontAction(this))
        gridBagLayout.setConstraints(loadFont, gridBagConstraints)
        add(loadFont)
        fontSelector = FontList(FontsListModel(Resources.getFonts()))
        setCurrentFont((fontSelector.getItemAt(0) as String))
        fontSelector.setSelectedItem(currentFont)
        fontSelector.addItemListener(this)
        gridBagLayout.setConstraints(fontSelector, gridBagConstraints)
        add(fontSelector)
        gridBagConstraints.weightx = GridBagConstraints.RELATIVE
        gridBagConstraints.weighty = 1
        gridBagConstraints.gridheight = GridBagConstraints.REMAINDER
        gridBagConstraints.fill = GridBagConstraints.BOTH
        keypadListeners = EventListenerList()
        keypadEvent = KeypadEvent(this)
        val fontKeypad: JComponent = getFontKeypad()
        gridBagLayout.setConstraints(fontKeypad, gridBagConstraints)
        add(fontKeypad)
    }
    fun load() {

    }
    fun getKeys(): ArrayList<JButton> {
        return keys
    }
    override fun itemStateChanged(e: ItemEvent) {
        setCurrentFont(e.getItem())
        resetKeypad()
    }
    fun setCurrentFont(fontName: String) {
        setCurrentFont(Resources.getFont(fontName))
    }
    fun setCurrentFont(font: Font) {
        if ((font == null)) {
            fontSelector.setSelectedIndex(0)
            currentFont = Resources.getFont(fontSelector.getSelectedItem())
        }
        else {
            currentFont = font
            fontSelector.setSelectedItem(currentFont.getName())
        }
        setFont(currentFont)
    }
    override fun stateChanged(e: FontMapChangeEvent) {
        fontMap = e.getFontMap()
        setCurrentFont()
        resetKeypad()
    }
    fun getSelectedFont(): String {
        return fontSelector.getSelectedItem()
    }
    private fun getFontKeypad(): JScrollPane {
        keypad = JPanel()
        keypad.setBorder(BorderFactory.createEmptyBorder())
        val jScrollPane: JScrollPane = JScrollPane()
        jScrollPane.getViewport()
        return jScrollPane
    }
    private fun resetKeypad() {
        keypad.removeAll()
        val gridBagLayout: GridBagLayout = GridBagLayout()
        keypad.setLayout(gridBagLayout)
        val gridBagConstraints: GridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
        gridBagConstraints.weightx = 0
        gridBagConstraints.weighty = 0
        val numOfGlyphs: Int = currentFont.getNumGlyphs()
        var i: Int = 0
        var j: Int = 0
        while (((i < FONT_MAX_INDEX) && (j < numOfGlyphs))) {
            val c: Char = i.toChar()
            if (currentFont.canDisplay(c)) {
                val cmd: String = (Resources.EMPTY_STRING + c)
                val keyButton: JButton
                if ((!keys.isEmpty() && (j < keys.size()))) {
                    keyButton = keys.get(j)
                }
                else {
                    keyButton = JButton()
                    keys.add(j, keyButton)
                }
                val actionListeners: Array<ActionListener> = keyButton.getActionListeners()
                if (((actionListeners != null) && (actionListeners.size > 0))) {
                    for (newVar in actionListeners) {
                        keyButton.removeActionListener(newVar)
                    }
                }
                keyButton.setFont(currentFont)
                keyButton.setText(cmd)
                gridBagConstraints.gridwidth = 1
                if ((((j + 1) % KEY_COLUMNS) == 0)) {
                    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER
                }
                gridBagLayout.setConstraints(keyButton, gridBagConstraints)
                keypad.add(keyButton)
                j++
            }
            i++
        }
        fireKeypadReset()
        keypad.updateUI()
        System.gc()
    }
    fun fireKeypadReset() {
        val listeners: Array<Object> = keypadListeners.getListenerList()
        var i: Int = (listeners.size - 2)
        while ((i >= 0)) {
            if ((listeners[i] == IKeypadListener::class.java)) {
                if ((keypadEvent == null)) {
                    keypadEvent = KeypadEvent(this)
                }
                (listeners[(i + 1)] as IKeypadListener).keypadReset(keypadEvent)
            }
            i -= 2
        }
    }
    fun addKeypadListener(keypadListener: IKeypadListener) {
        keypadListeners.add(IKeypadListener::class.java, keypadListener)
    }
    fun removeKeypadListener(keypadListener: IKeypadListener) {
        keypadListeners.remove(IKeypadListener::class.java, keypadListener)
    }
    fun getFontMap(): FontMap {
        return fontMap
    }
    fun getCurrentFont(): Font {
        return currentFont
    }
    fun getFontSelector(): FontList {
        return fontSelector
    }
    protected abstract fun setCurrentFont()
    abstract fun loadFont(font: File)
    class FontsListModel : DefaultComboBoxModel(), ChangeListener {
    private var fonts: Map<String, FontInfo>? = null
    constructor(fonts: Map<String, FontInfo>) : super() {
        setFonts(fonts)
    }
    fun setFonts(fonts: Map<String, FontInfo>) {
        logger.entering(getClass(), "setFonts")
        this = fonts
        refreshFonts()
    }
    private fun refreshFonts() {
        logger.entering(getClass(), "refreshFonts")
        removeAllElements()
        val contents: Array<Object> = fonts.keySet()
        Arrays.sort(contents)
        for (newVar in contents) {
            addElement(newVar)
        }
    }
    override fun stateChanged(e: ChangeEvent) {
        setFonts(Resources.getFonts())
        fireContentsChanged(this, 0, fonts.size())
    }

    companion object {
        private val logger: Logger = Logger.getLogger("intellibitz.sted.ui.FontKeypad$FontsListModel")
    }
    }
    class FontList : JComboBox(), ChangeListener {
    constructor(aModel: ComboBoxModel) : super(aModel)
    override fun stateChanged(e: ChangeEvent) {
        (getModel() as FontsListModel)
        setSelectedItem((e as FontListChangeEvent))
        updateUI()
    }
    }

    companion object {
        private val KEY_COLUMNS: Int = Integer.parseInt(Resources.getSetting(Resources.KEYPAD_COLUMN_COUNT))
        private val FONT_MAX_INDEX: Int = Integer.parseInt(Resources.getSetting(Resources.FONT_CHAR_MAXINDEX))
    }
}
