package intellibitz.sted.ui

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.event.FontMapEntriesChangeEvent
import intellibitz.sted.event.IFontMapEntriesChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import java.beans.PropertyVetoException
import java.io.File
import java.util.logging.Logger
import javax.swing.*
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener

class DesktopFrame : JInternalFrame("FontMapperInternalFrame", false, true, false, false),
    TableModelListener, FontMapChangeListener, IFontMapEntriesChangeListener {
    
    private var tabbedPane: JTabbedPane? = null
    var mapperPanel: MapperPanel? = null
        private set
    var inputFileViewer: FileViewer? = null
        private set
    var outputFileViewer: FileViewer? = null
        private set
    var desktopModel: DesktopModel? = null

    fun init() {
        mapperPanel = MapperPanel()
        mapperPanel!!.init()
        setNormalIcon()
        tabbedPane = JTabbedPane(JTabbedPane.BOTTOM)
        tabbedPane!!.border = BorderFactory.createRaisedBevelBorder()
        tabbedPane!!.addTab(
            Resources.getResource(Resources.TITLE_TAB_FONTMAP),
            Resources.getSTEDIcon(),
            mapperPanel,
            Resources.getResource(Resources.TIP_TAB_FONTMAP)
        )
        inputFileViewer = FileViewer(
            Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_INPUT))
        )
        tabbedPane!!.addTab(
            Resources.getResource(Resources.TITLE_TAB_INPUT),
            Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_INPUT)),
            inputFileViewer
        )
        outputFileViewer = FileViewer(
            Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_OUTPUT))
        )
        tabbedPane!!.addTab(
            Resources.getResource(Resources.TITLE_TAB_OUTPUT),
            Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_OUTPUT)),
            outputFileViewer
        )
        tabbedPane!!.setEnabledAt(0, false)
        tabbedPane!!.setEnabledAt(1, false)
        tabbedPane!!.setEnabledAt(2, false)

        contentPane.add(tabbedPane)
        bounds = mapperPanel!!.bounds
        location = mapperPanel!!.location
        pack()
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        isVisible = true
    }

    fun load() {
        mapperPanel!!.load()
        mapperPanel!!.mappingEntryPanel!!.addTableModelListener(this)
        mapperPanel!!.setSampleInput(Resources.getResource(Resources.SAMPLE_INPUT_TEXT))
        tabbedPane!!.setToolTipTextAt(
            1,
            MenuHandler.toolTips[Resources.ACTION_SELECT_INPUT_FILE_COMMAND]
        )
        tabbedPane!!.setToolTipTextAt(
            2,
            MenuHandler.toolTips[Resources.ACTION_SELECT_OUTPUT_FILE_COMMAND]
        )
    }

    fun setModel(model: DesktopModel) {
        desktopModel = model
        desktopModel!!.addFontMapChangeListener(this)
        desktopModel!!.addFontMapChangeListener(mapperPanel!!)
        desktopModel!!.addFontMapChangeListener(mapperPanel!!.mappingEntryPanel!!)
        desktopModel!!.addFontMapChangeListener(mapperPanel!!.mappingEntryPanel!!.mappingRules!!)
    }

    private fun setNormalIcon() {
        frameIcon = Resources.getCleanIcon()
    }

    private fun setEditIcon() {
        frameIcon = Resources.getDirtyIcon()
    }

    fun clear() {
        mapperPanel!!.clear()
        desktopModel!!.clear()
        title = Resources.EMPTY_STRING
        setNormalIcon()
    }

    private fun setInternalFrameSelected(flag: Boolean) {
        try {
            isSelected = flag
        } catch (e: PropertyVetoException) {
            logger.throwing(javaClass.name, "setInternalFrameSelected", e)
            e.printStackTrace()
        }
    }

    fun showFrame() {
        isVisible = true
        tabbedPane!!.isVisible = true
        setFrameTitle(desktopModel!!.fontMap!!)
        try {
            isMaximum = true
        } catch (e: PropertyVetoException) {
            logger.throwing(javaClass.name, "showFrame", e)
            e.printStackTrace()
        }
        mapperPanel!!.mappingEntryPanel!!.word1!!.requestFocus()
    }

    private fun hideFrame() {
        setInternalFrameSelected(false)
        isVisible = false
    }

    fun enableTabs(flag: Boolean) {
        val count = tabbedPane!!.tabCount
        if (count > 0) {
            for (i in 0 until count) {
                tabbedPane!!.setEnabledAt(i, flag)
            }
        }
    }

    fun close() {
        hideFrame()
        enableTabs(false)
        val menuHandler = MenuHandler.instance!!
        menuHandler.getMenuItem(Resources.ACTION_VIEW_MAPPING)?.isEnabled = false
        menuHandler.getMenuItem(Resources.ACTION_VIEW_SAMPLE)?.isEnabled = false
        menuHandler.getMenuItem(Resources.ACTION_PASTE_COMMAND)?.isEnabled = false
        menuHandler.getMenuItem(Resources.ACTION_DELETE_COMMAND)?.isEnabled = false
        menuHandler.getMenuItem(Resources.ACTION_SELECT_ALL_COMMAND)?.isEnabled = false
        menuHandler.getMenuItem(Resources.ACTION_CUT_COMMAND)?.isEnabled = false
        menuHandler.getMenuItem(Resources.ACTION_COPY_COMMAND)?.isEnabled = false
    }

    fun setEnabledFontMapTab(flag: Boolean) {
        if (tabbedPane!!.tabCount > 0) {
            tabbedPane!!.setEnabledAt(0, flag)
        }
    }

    override fun tableChanged(e: TableModelEvent) {
        if (desktopModel!!.fontMap!!.isDirty) {
            setEditIcon()
        }
    }

    private fun setFileIcon(fontMap: FontMap) {
        if (fontMap.isDirty) {
            setEditIcon()
        } else {
            setNormalIcon()
        }
    }

    fun setFrameTitle(fontMap: FontMap) {
        if (fontMap.getFileName().isNotEmpty()) {
            title = fontMap.fontMapFile!!.absolutePath
        }
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        val fontMap = e.fontMap
        setFrameTitle(fontMap!!)
        setFileIcon(fontMap)
        inputFileViewer!!.font = fontMap.font1
        outputFileViewer!!.font = fontMap.font2
        enableConverterIfFilesLoaded()
    }

    fun enableConverterIfFilesLoaded(): Boolean {
        val flag = desktopModel!!.isReadyForTransliteration
        MenuHandler.actions[Resources.ACTION_CONVERT_NAME]?.isEnabled = flag
        return flag
    }

    fun setInputFile(file: File?) {
        desktopModel!!.inputFile = file
        if (file != null) {
            readFile(1)
        }
    }

    private fun readFile(index: Int) {
        tabbedPane!!.setEnabledAt(index, true)
        tabbedPane!!.selectedIndex = index
        when (index) {
            1 -> {
                inputFileViewer!!.setFileName(desktopModel!!.inputFile!!.absolutePath)
                inputFileViewer!!.readFile()
            }
            2 -> readOutputFile()
            else -> {}
        }
    }

    fun setOutputFile(file: File?) {
        desktopModel!!.outputFile = file
        if (file != null) {
            readFile(2)
        }
    }

    fun readOutputFile() {
        if (desktopModel!!.outputFile != null) {
            outputFileViewer!!.setFileName(desktopModel!!.outputFile!!.absolutePath)
            outputFileViewer!!.readFile()
        }
    }

    override fun stateChanged(e: FontMapEntriesChangeEvent) {
        desktopModel!!.fireFontMapChangedEvent()
    }

    companion object {
        private val logger = Logger.getLogger("intellibitz.sted.ui.DesktopFrame")
    }
}
