package intellibitz.sted.ui

import intellibitz.sted.actions.LoadFontAction
import intellibitz.sted.event.*
import intellibitz.sted.fontmap.FontInfo
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.util.Resources
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.io.File
import java.util.ArrayList
import java.util.Arrays
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.EventListenerList

abstract class FontKeypad protected constructor() : JPanel(), ItemListener, FontMapChangeListener, IKeypadEventSource {
    var fontMap: FontMap? = null
        private set
    lateinit var fontSelector: FontList
        private set
    private var keypad: JPanel? = null
    var currentFont: Font? = null
        private set
    val keys = ArrayList<JButton>()
    private var keypadEvent: KeypadEvent? = null
    private var keypadListeners: EventListenerList? = null

    fun init() {
        val titledBorder = TitledBorder(Resources.getResource(Resources.TITLE_KEYPAD))
        titledBorder.titleJustification = TitledBorder.CENTER
        border = titledBorder
        val gridBagLayout = GridBagLayout()
        layout = gridBagLayout
        val gridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
        gridBagConstraints.weightx = 0.0
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER

        val loadFont = JButton(LoadFontAction(this))
        gridBagLayout.setConstraints(loadFont, gridBagConstraints)
        add(loadFont)

        fontSelector = FontList(FontsListModel(Resources.fonts))
        setCurrentFont(fontSelector.getItemAt(0) as String)
        fontSelector.selectedItem = currentFont
        fontSelector.addItemListener(this)
        gridBagLayout.setConstraints(fontSelector, gridBagConstraints)
        add(fontSelector)

        gridBagConstraints.weightx = GridBagConstraints.RELATIVE.toDouble()
        gridBagConstraints.weighty = 1.0
        gridBagConstraints.gridheight = GridBagConstraints.REMAINDER
        gridBagConstraints.fill = GridBagConstraints.BOTH

        keypadListeners = EventListenerList()
        keypadEvent = KeypadEvent(this)

        val fontKeypad = getFontKeypad()
        gridBagLayout.setConstraints(fontKeypad, gridBagConstraints)
        add(fontKeypad)
    }

    fun load() {}

    override fun itemStateChanged(e: ItemEvent) {
        setCurrentFont(e.item.toString())
        resetKeypad()
    }

    internal open fun setCurrentFont(fontName: String) {
        setCurrentFont(Resources.getFont(fontName)?.font)
    }

    internal fun setCurrentFont(font: Font?) {
        if (font == null) {
            fontSelector.selectedIndex = 0
            currentFont = Resources.getFont(fontSelector.selectedItem.toString())?.font
        } else {
            currentFont = font
            fontSelector.selectedItem = currentFont!!.name
        }
        this.font = currentFont
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        fontMap = e.fontMap
        setCurrentFont()
        resetKeypad()
    }

    val selectedFont: String
        get() = fontSelector.selectedItem.toString()

    private fun getFontKeypad(): JScrollPane {
        keypad = JPanel()
        keypad!!.border = BorderFactory.createEmptyBorder()
        val jScrollPane = JScrollPane()
        jScrollPane.viewport.add(keypad)
        return jScrollPane
    }

    private fun resetKeypad() {
        keypad!!.removeAll()
        val gridBagLayout = GridBagLayout()
        keypad!!.layout = gridBagLayout
        val gridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
        gridBagConstraints.weightx = 0.0
        gridBagConstraints.weighty = 0.0
        val numOfGlyphs = currentFont!!.numGlyphs
        var j = 0
        for (i in 0 until FONT_MAX_INDEX) {
            if (j >= numOfGlyphs) break
            val c = i.toChar()
            if (currentFont!!.canDisplay(c)) {
                val cmd = Resources.EMPTY_STRING + c
                val keyButton: JButton
                if (keys.isNotEmpty() && j < keys.size) {
                    keyButton = keys[j]
                } else {
                    keyButton = JButton()
                    keys.add(j, keyButton)
                }
                val actionListeners = keyButton.actionListeners
                if (actionListeners != null && actionListeners.isNotEmpty()) {
                    for (newVar in actionListeners) {
                        keyButton.removeActionListener(newVar)
                    }
                }
                keyButton.font = currentFont
                keyButton.text = cmd
                gridBagConstraints.gridwidth = 1
                if ((j + 1) % KEY_COLUMNS == 0) {
                    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER
                }
                gridBagLayout.setConstraints(keyButton, gridBagConstraints)
                keypad!!.add(keyButton)
                j++
            }
        }
        fireKeypadReset()
        keypad!!.updateUI()
        System.gc()
    }

    override fun fireKeypadReset() {
        val listeners = keypadListeners!!.listenerList
        for (i in listeners.size - 2 downTo 0 step 2) {
            if (listeners[i] === IKeypadListener::class.java) {
                if (keypadEvent == null) {
                    keypadEvent = KeypadEvent(this)
                }
                (listeners[i + 1] as IKeypadListener).keypadReset(keypadEvent!!)
            }
        }
    }

    override fun addKeypadListener(keypadListener: IKeypadListener) {
        keypadListeners!!.add(IKeypadListener::class.java, keypadListener)
    }

    fun removeKeypadListener(keypadListener: IKeypadListener) {
        keypadListeners!!.remove(IKeypadListener::class.java, keypadListener)
    }

    class FontsListModel : DefaultComboBoxModel<String>, ChangeListener {
        private var fonts: Map<String, FontInfo>? = null

        constructor() : super()

        constructor(fonts: Map<String, FontInfo>) : this() {
            setFonts(fonts)
        }

        fun setFonts(fonts: Map<String, FontInfo>) {
            logger.entering(javaClass.name, "setFonts")
            this.fonts = fonts
            refreshFonts()
        }

        private fun refreshFonts() {
            logger.entering(javaClass.name, "refreshFonts")
            removeAllElements()
            val contents = fonts!!.keys.toTypedArray()
            Arrays.sort(contents)
            for (newVar in contents) {
                addElement(newVar)
            }
        }

        override fun stateChanged(e: ChangeEvent) {
            setFonts(Resources.fonts)
            fireContentsChanged(this, 0, fonts!!.size)
        }

        companion object {
            private val logger = Logger.getLogger("intellibitz.sted.ui.FontKeypad\$FontsListModel")
        }
    }

    class FontList(aModel: ComboBoxModel<String>) : JComboBox<String>(aModel), ChangeListener {
        override fun stateChanged(e: ChangeEvent) {
            (model as FontsListModel).stateChanged(e)
            selectedItem = (e as FontListChangeEvent).fontChanged?.name
            updateUI()
        }
    }

    protected abstract fun setCurrentFont()
    abstract fun loadFont(font: File)

    companion object {
        private val KEY_COLUMNS = Resources.getSetting(Resources.KEYPAD_COLUMN_COUNT)?.toInt() ?: 10
        private val FONT_MAX_INDEX = Resources.getSetting(Resources.FONT_CHAR_MAXINDEX)?.toInt() ?: 255
    }
}
