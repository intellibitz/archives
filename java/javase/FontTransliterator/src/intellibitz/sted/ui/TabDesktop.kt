package intellibitz.sted.ui

import intellibitz.sted.actions.RedoAction
import intellibitz.sted.actions.UndoAction
import intellibitz.sted.event.*
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.io.FileFilterHelper
import intellibitz.sted.io.FontMapReader
import intellibitz.sted.launch.STEDGUI
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.ButtonTabComponent
import java.awt.HeadlessException
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyVetoException
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.Logger
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener
import javax.xml.transform.TransformerException

class TabDesktop : JTabbedPane(), InternalFrameListener, IThreadListener, FontMapChangeListener, ChangeListener, ActionListener, IStatusEventSource, IMessageEventSource {

    private class FrameNumberIndex {
        private val indices: MutableSet<Int> = TreeSet()

        fun addNewIndex(indx: Int): Int {
            val sz = indices.size
            if (!containsIndex(indx) && indx >= sz) {
                indices.add(indx)
                return indx
            } else {
                for (i in 1..sz) {
                    if (!containsIndex(i)) {
                        indices.add(i)
                        return i
                    }
                }
            }
            return indx
        }

        fun removeIndex(indx: Int): Boolean {
            return indices.remove(indx)
        }

        fun containsIndex(indx: Int): Boolean {
            return indices.contains(indx)
        }
    }

    private var desktopPane: JDesktopPane? = null
    private val frameNumberIndex = FrameNumberIndex()
    private val frameCache: MutableMap<String, DesktopFrame> = HashMap()
    val clipboard: MutableMap<String, Collection<*>> = HashMap()
    
    private var statusListener: IStatusListener? = null
    private var statusEvent: StatusEvent? = null
    private var messageListener: IMessageListener? = null
    private var messageEvent: MessageEvent? = null

    fun init() {
        desktopPane = JDesktopPane()
        addChangeListener(this)
        statusEvent = StatusEvent(this)
        messageEvent = MessageEvent(this)
        isVisible = true
    }

    fun load() {}

    fun fireStatusPosted(message: String) {
        statusEvent!!.status = message
        statusListener?.statusPosted(statusEvent!!)
    }

    override fun fireStatusPosted() {
        statusListener?.statusPosted(statusEvent!!)
    }

    override fun addStatusListener(statusListener: IStatusListener) {
        this.statusListener = statusListener
    }

    fun addTab(desktopFrame: DesktopFrame) {
        addTab(desktopFrame.title ?: "", desktopFrame)
    }

    override fun addTab(title: String, component: java.awt.Component) {
        val desktopFrame = component as DesktopFrame
        desktopFrame.title = title
        desktopFrame.removeInternalFrameListener(this)
        desktopFrame.addInternalFrameListener(this)

        desktopFrame.desktopModel?.removeFontMapChangeListener(this)
        desktopFrame.desktopModel?.addFontMapChangeListener(this)

        super.addTab(title, Resources.getSTEDIcon(), desktopFrame, Resources.getResource(Resources.TIP_TAB_FONTMAP))
        initTabComponent(tabCount - 1, title)
        setEnabledAt(tabCount - 1, true)
        selectedIndex = tabCount - 1
        try {
            desktopFrame.isSelected = true
        } catch (e: PropertyVetoException) {
            e.printStackTrace()
        }
    }

    private fun initTabComponent(i: Int, title: String) {
        val buttonTabComponent = ButtonTabComponent("TabComponent $i", this)
        buttonTabComponent.tabTitle.icon = Resources.getCleanIcon()
        buttonTabComponent.tabTitle.text = title
        buttonTabComponent.addActionListener(this)
        setTabComponentAt(i, buttonTabComponent)
    }

    fun closeFontMap(): Int {
        return closeFontMap(fontMapperDesktopFrame!!)
    }

    fun closeFontMap(desktopFrame: DesktopFrame): Int {
        val i = saveDirty(desktopFrame)
        if (JOptionPane.CANCEL_OPTION != i) {
            var index = selectedIndex
            if (index == -1) {
                index = tabCount - 1
            }
            removeTabFrameAt(index)
            desktopFrame.close()
        }
        enableCloseAction()
        return i
    }

    fun closeFontMap(desktopFrame: DesktopFrame, i: Int): Int {
        val result = saveDirty(desktopFrame)
        if (JOptionPane.CANCEL_OPTION != result) {
            removeTabFrameAt(i)
            desktopFrame.close()
        }
        enableCloseAction()
        return i
    }

    private fun removeTabFrameAt(index: Int): Boolean {
        if (index > -1) {
            val desktopFrame = getComponentAt(index) as DesktopFrame
            val title = desktopFrame.title
            if (title != null && title.startsWith(Resources.ACTION_FILE_NEW_COMMAND)) {
                val indx = getNewIndexNumber(title)
                frameNumberIndex.removeIndex(indx)
            }
            removeTabAt(index)
            return true
        }
        return false
    }

    private fun getNewIndexNumber(title: String): Int {
        return title.substring(Resources.ACTION_FILE_NEW_COMMAND.length).trim().toInt()
    }

    private fun createNewFrameTitle(num: Int): String {
        return Resources.ACTION_FILE_NEW_COMMAND + " " + frameNumberIndex.addNewIndex(num)
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        val desktopFrame = selectedFrame
        val title = desktopFrame!!.title
        if (title != null) {
            val indx = indexOfTab(title)
            if (-1 == indx) {
                addTab(desktopFrame)
            } else {
                setEnabledAt(indx, true)
            }
        } else {
            addTab(desktopFrame)
        }
        
        val buttonTabComponent = getTabComponentAt(selectedIndex) as ButtonTabComponent
        buttonTabComponent.tabTitle.icon = Resources.getDirtyIcon()

        val fontMap = desktopFrame.desktopModel!!.fontMap
        if (fontMap?.fontMapFile != null) {
            buttonTabComponent.tabTitle.text = fontMap.fontMapFile!!.name
        }
        if (fontMap!!.isDirty) {
            buttonTabComponent.tabTitle.icon = Resources.getDirtyIcon()
        } else {
            buttonTabComponent.tabTitle.icon = Resources.getCleanIcon()
        }
        fireStatusPosted(buttonTabComponent.tabTitle.text + " Active")
        this.updateUI()
    }

    override fun stateChanged(e: ChangeEvent) {
        val me = e.source as TabDesktop
        var i = me.selectedIndex
        if (i != -1) {
            val myframe = getComponentAt(i) as DesktopFrame
            desktopPane!!.selectedFrame = myframe
            i = me.selectedIndex
            val buttonTab = getTabComponentAt(i) as ButtonTabComponent?
            var title = myframe.title
            if (buttonTab != null) {
                title = buttonTab.tabTitle.text
            }
            fireStatusPosted("$title Active")
        }
        enableCloseAction()
    }

    private fun enableCloseAction(flag: Boolean) {
        MenuHandler.instance!!.getMenuItem(Resources.ACTION_FILE_CLOSE_COMMAND)?.isEnabled = flag
    }

    private fun enableCloseAction() {
        enableCloseAction(tabCount > 0)
    }

    override fun actionPerformed(e: ActionEvent) {
        val buttonTabComponent = e.source as ButtonTabComponent
        val i = indexOfTabComponent(buttonTabComponent)
        if (i != -1) {
            val desktopFrame = getComponentAt(i) as DesktopFrame
            closeFontMap(desktopFrame, i)
        }
    }

    private val selectedFrame: DesktopFrame?
        get() = desktopPane!!.selectedFrame as? DesktopFrame ?: if (tabCount > 0) getComponentAt(tabCount - 1) as DesktopFrame else null

    fun addListenersToDesktopFrame(desktopFrame: DesktopFrame) {
        val mapperPanel = desktopFrame.mapperPanel!!
        val desktopModel = desktopFrame.desktopModel!!
        val fontMap = desktopModel.fontMap!!
        
        fontMap.removeFontMapChangeListener(mapperPanel)
        fontMap.addFontMapChangeListener(mapperPanel)

        fontMap.removeFontMapChangeListener(mapperPanel.mappingEntryPanel!!)
        fontMap.addFontMapChangeListener(mapperPanel.mappingEntryPanel!!)

        fontMap.removeFontMapChangeListener(desktopFrame)
        fontMap.addFontMapChangeListener(desktopFrame)

        val menuHandler = MenuHandler.instance!!
        val newAction = menuHandler.getAction(Resources.ACTION_FILE_NEW_COMMAND) as FontMapChangeListener
        fontMap.removeFontMapChangeListener(newAction)
        fontMap.addFontMapChangeListener(newAction)

        val reload = menuHandler.getAction(Resources.ACTION_FILE_RELOAD) as FontMapChangeListener?
        if (reload != null) {
            fontMap.removeFontMapChangeListener(reload)
            fontMap.addFontMapChangeListener(reload)
        }

        val save = menuHandler.getAction(Resources.ACTION_FILE_SAVE_COMMAND) as FontMapChangeListener?
        if (save != null) {
            fontMap.removeFontMapChangeListener(save)
            fontMap.addFontMapChangeListener(save)
        }

        val paste = menuHandler.getAction(Resources.ACTION_PASTE_COMMAND) as FontMapChangeListener?
        if (paste != null) {
            fontMap.removeFontMapChangeListener(paste)
            fontMap.addFontMapChangeListener(paste)
        }

        val undo = menuHandler.getAction(Resources.ACTION_UNDO_COMMAND) as UndoAction?
        if (undo != null) {
            fontMap.removeUndoListener(undo)
            undo.isEnabled = false
            fontMap.addUndoListener(undo)
            this.removeChangeListener(undo)
            this.addChangeListener(undo)
        }

        val redo = menuHandler.getAction(Resources.ACTION_REDO_COMMAND) as RedoAction?
        if (redo != null) {
            fontMap.removeRedoListener(redo)
            redo.isEnabled = false
            fontMap.addRedoListener(redo)
            this.removeChangeListener(redo)
            this.addChangeListener(redo)
        }

        val keypad1 = mapperPanel.fontKeypad1
        fontMap.removeFontListChangeListener(keypad1)
        fontMap.addFontListChangeListener(keypad1)
        desktopModel.addFontMapChangeListener(keypad1)

        val keypad2 = mapperPanel.fontKeypad2
        fontMap.removeFontListChangeListener(keypad2)
        fontMap.addFontListChangeListener(keypad2)
        desktopModel.addFontMapChangeListener(keypad2)

        desktopFrame.addInternalFrameListener(menuHandler.getAction(Resources.ACTION_FILE_NEW_COMMAND) as InternalFrameListener?)
        desktopFrame.addInternalFrameListener(menuHandler.getAction(Resources.ACTION_FILE_RELOAD_COMMAND) as InternalFrameListener?)
        desktopFrame.addInternalFrameListener(menuHandler.getAction(Resources.ACTION_FILE_REOPEN_COMMAND) as InternalFrameListener?)
        desktopFrame.addInternalFrameListener(menuHandler.getAction(Resources.ACTION_FILE_SAVEAS_COMMAND) as InternalFrameListener?)
        desktopFrame.addInternalFrameListener(menuHandler.getAction(Resources.ACTION_FILE_CLOSE_COMMAND) as InternalFrameListener?)

        desktopModel.removeFontMapChangeListener(newAction)
        desktopModel.addFontMapChangeListener(newAction)
        
        val reopen = menuHandler.getAction(Resources.ACTION_FILE_REOPEN_COMMAND) as FontMapChangeListener?
        if (reopen != null) {
            desktopModel.removeFontMapChangeListener(reopen)
            desktopModel.addFontMapChangeListener(reopen)
            desktopFrame.addInternalFrameListener(reopen as InternalFrameListener?)
        }
    }

    fun createDesktopModel(desktopFrame: DesktopFrame, fontMap: FontMap?): DesktopModel {
        val desktopModel = DesktopModel()
        desktopModel.fontMap = fontMap
        desktopFrame.desktopModel = desktopModel

        addListenersToDesktopFrame(desktopFrame)

        desktopFrame.load()
        return desktopModel
    }

    fun loadFontMap(file: File) {
        selectedFrame?.let { loadFontMap(it, file) }
    }

    fun loadFontMap(desktopFrame: DesktopFrame, file: File) {
        val desktopModel = createDesktopModel(desktopFrame, FontMap(file))
        desktopModel.addFontMapChangeListener(this)
        readFontMap(desktopModel)
        fireStatusPosted("FontMap loaded")
    }

    fun readFontMap(desktopModel: DesktopModel) {
        val fontMap = desktopModel.fontMap
        try {
            val fontMapReader = FontMapReader(fontMap!!)
            fontMapReader.addThreadListener(this)
            SwingUtilities.invokeLater(fontMapReader)
        } catch (e: IllegalArgumentException) {
            fireMessagePosted("Cannot Read FontMap.. Failed: " + e.message)
            logger.severe("Cannot Read FontMap - Illegal Argument " + fontMap!!.fontMapFile!!.absolutePath)
            fireStatusPosted("Cannot Read FontMap - Illegal Argument " + fontMap.fontMapFile!!.absolutePath)
        }
    }

    fun openFontMap() {
        val selectedFile = FileHelper.openFile("Please select FontMap location:", Resources.XML, "STED FontMap files", this)
        if (selectedFile != null) {
            openFontMap(selectedFile)
        }
    }

    fun newFontMap() {
        val desktopFrame = loadNewFontMap()
        val num = tabCount + 1
        addTab(createNewFrameTitle(num), desktopFrame)
        fireStatusPosted("New FontMap")
    }

    fun loadNewFontMap(): DesktopFrame {
        val desktopFrame = createFontMapperDesktopFrame()
        val fontMap = FontMap()
        val desktopModel = createDesktopModel(desktopFrame, fontMap)
        desktopModel.fireFontMapChangedEvent()
        return desktopFrame
    }

    fun reopenFontMap(fileName: String) {
        openFontMap(File(fileName))
    }

    fun reloadFontMap() {
        val selectedFrame = selectedFrame!!
        val selectedFile = selectedFrame.desktopModel!!.fontMap!!.fontMapFile!!
        removeTabFrameAt(selectedIndex)
        frameCache.remove(selectedFrame.desktopModel!!.fontMap!!.getFileName())
        openFontMap(selectedFile)
    }

    fun openFontMap(selectedFile: File) {
        try {
            var desktopFrame = frameCache[selectedFile.absolutePath]
            if (desktopFrame == null) {
                desktopFrame = createFontMapperDesktopFrame()
                loadFontMap(desktopFrame, selectedFile)
                val desktopModel = desktopFrame.desktopModel
                val fontMap = desktopModel!!.fontMap
                frameCache[fontMap!!.getFileName()] = desktopFrame
            }
            add(desktopFrame)
            desktopPane!!.selectedFrame = desktopFrame
            desktopFrame.desktopModel!!.fireFontMapChangedEvent()
        } catch (ex: HeadlessException) {
            logger.throwing("intellibitz.sted.util.FontMapHelper", "readFontMap", ex)
            JOptionPane.showMessageDialog(this, "Invalid FontMap " + selectedFile.absolutePath)
        } catch (ex: IllegalArgumentException) {
            logger.throwing("intellibitz.sted.util.FontMapHelper", "readFontMap", ex)
            JOptionPane.showMessageDialog(this, "Load Failed: " + ex.message)
        }
    }

    private fun saveFontMap() {
        val desktopFrame = selectedFrame!!
        var fontMap = desktopFrame.desktopModel!!.fontMap!!
        val fontKeypad1FontName = desktopFrame.mapperPanel!!.fontKeypad1.selectedFont
        val fontKeypad2FontName = desktopFrame.mapperPanel!!.fontKeypad2.selectedFont
        fontMap.font1 = Resources.getFont(fontKeypad1FontName)?.font
        fontMap.font2 = Resources.getFont(fontKeypad2FontName)?.font
        try {
            fontMap = desktopFrame.desktopModel!!.saveFontMap()!!
        } catch (exception: TransformerException) {
            exception.printStackTrace()
            JOptionPane.showMessageDialog(this, fontMap.getFileName() + " cannot create for writing " + exception.message)
        }
    }

    fun saveAction() {
        val fontMap = fontMap
        val selectedFile = fontMap!!.fontMapFile
        if (selectedFile == null) {
            saveAsAction()
        } else if (!selectedFile.canWrite()) {
            try {
                selectedFile.createNewFile()
                fontMap.fontMapFile = selectedFile
            } catch (exception: IOException) {
                JOptionPane.showMessageDialog(this, selectedFile.toString() + " cannot create for writing " + exception.message)
            }
        } else {
            saveFontMap()
        }
    }

    fun saveAsAction(): Int {
        val fontMap = fontMap
        val jFileChooser = JFileChooser(System.getProperty("user.dir"))
        val fileFilterHelper = FileFilterHelper("xml", "STED FontMap files")
        jFileChooser.fileFilter = fileFilterHelper
        val result = jFileChooser.showSaveDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = jFileChooser.selectedFile
            var ok = false
            if (!selectedFile.canWrite()) {
                try {
                    ok = selectedFile.createNewFile()
                } catch (exception: IOException) {
                    JOptionPane.showMessageDialog(this, selectedFile.toString() + " cannot create for writing " + exception.message)
                }
            } else {
                ok = true
            }
            if (ok) {
                fontMap!!.fontMapFile = selectedFile
                saveFontMap()
            } else {
                JOptionPane.showMessageDialog(this, selectedFile.toString() + " is NOT Writable")
            }
        }
        return result
    }

    fun saveDirty(): Int {
        return selectedFrame?.let { saveDirty(it) } ?: JOptionPane.CLOSED_OPTION
    }

    fun saveDirty(desktopFrame: DesktopFrame?): Int {
        var result = JOptionPane.CLOSED_OPTION
        if (desktopFrame != null) {
            val fontMap = desktopFrame.desktopModel!!.fontMap
            if (fontMap != null && fontMap.isDirty) {
                result = JOptionPane.showConfirmDialog(
                    this,
                    "FontMap Changed.. Do you want to save changes?",
                    "Save Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION
                )
                if (JOptionPane.YES_OPTION == result) {
                    if (fontMap.isNew()) {
                        result = saveAsAction()
                        if (JFileChooser.CANCEL_OPTION == result) {
                            fontMap.clear()
                        }
                    } else {
                        saveAction()
                    }
                } else if (JOptionPane.NO_OPTION == result) {
                    fontMap.clear()
                }
            }
        }
        return result
    }

    fun clear() {
        selectedFrame?.clear()
    }

    val fontMap: FontMap?
        get() = selectedFrame?.desktopModel?.fontMap

    fun addItemToReOpenMenu(item: String?) {
        val menuHandler = MenuHandler.instance!!
        val menu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
        MenuHandler.addReOpenItem(menu!!, item!!)
        menu!!.isEnabled = menu.itemCount != Resources.DEFAULT_MENU_COUNT + 1
    }

    fun createFontMapperDesktopFrame(): DesktopFrame {
        val desktopFrame = DesktopFrame()
        desktopFrame.addInternalFrameListener(this)
        desktopFrame.init()
        desktopPane!!.add(desktopFrame)
        desktopPane!!.selectedFrame = desktopFrame
        desktopFrame.isEnabled = true
        desktopFrame.isVisible = true
        return desktopFrame
    }

    fun addToClipboard(entry: String, value: Collection<*>) {
        clipboard[entry] = value
        fireStatusPosted("Copied Fontmap Entries")
    }

    val fontMapperDesktopFrame: DesktopFrame?
        get() = selectedFrame

    val desktopModel: DesktopModel?
        get() = fontMapperDesktopFrame?.desktopModel

    val frameTitle: String?
        get() = selectedFrame?.title

    private fun fireMessagePosted(message: String) {
        messageEvent!!.message = message
        messageListener?.messagePosted(messageEvent!!)
    }

    override fun fireMessagePosted() {
        messageListener?.messagePosted(messageEvent!!)
    }

    override fun addMessageListener(messageListener: IMessageListener) {
        this.messageListener = messageListener
    }

    override fun threadRunStarted(e: ThreadEvent) {
        STEDGUI.busy()
    }

    override fun threadRunning(e: ThreadEvent) {
    }

    override fun threadRunFailed(e: ThreadEvent) {
        STEDGUI.busy()
        val message = e.eventSource.message.toString()
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
        if (e is FontMapReadEvent) {
            closeFontMap()
        }
        if (e is TransliterateEvent) {
            MenuHandler.instance!!.getAction(Resources.ACTION_CONVERT_NAME)!!.isEnabled = true
            MenuHandler.instance!!.getAction(Resources.ACTION_STOP_NAME)!!.isEnabled = false
        }
        fireStatusPosted(message)
        STEDGUI.relax()
    }

    override fun threadRunFinished(e: ThreadEvent) {
        if (e is FontMapReadEvent) {
            val desktopModel = selectedFrame!!.desktopModel!!
            val fontMap = desktopModel.fontMap!!
            fontMap.isDirty = false
            desktopModel.fireFontMapChangedEvent()
            fireStatusPosted("FontMap Loaded")
        } else if (e is TransliterateEvent) {
            selectedFrame!!.readOutputFile()
            MenuHandler.instance!!.getAction(Resources.ACTION_CONVERT_NAME)!!.isEnabled = true
            MenuHandler.instance!!.getAction(Resources.ACTION_STOP_NAME)!!.isEnabled = false
            fireStatusPosted("Transliterate Done")
        }
        STEDGUI.relax()
    }

    override fun internalFrameClosing(e: InternalFrameEvent) {
        val desktopFrame = e.internalFrame as DesktopFrame
        if (JOptionPane.CANCEL_OPTION != saveDirty(desktopFrame)) {
            val index = selectedIndex
            removeTabFrameAt(index)
            desktopFrame.close()
        }
        enableCloseAction()
    }

    override fun internalFrameActivated(e: InternalFrameEvent) {
        MenuHandler.instance!!.getMenuItem(Resources.ACTION_VIEW_MAPPING)!!.isEnabled = true
        MenuHandler.instance!!.getMenuItem(Resources.ACTION_VIEW_SAMPLE)!!.isEnabled = true
        val desktopFrame = e.internalFrame as DesktopFrame
        desktopFrame.setEnabledFontMapTab(true)
        enableCloseAction()
    }

    override fun internalFrameOpened(e: InternalFrameEvent) {
        enableCloseAction()
    }

    override fun internalFrameClosed(e: InternalFrameEvent) {
        enableCloseAction()
    }

    override fun internalFrameIconified(e: InternalFrameEvent) {}
    override fun internalFrameDeiconified(e: InternalFrameEvent) {}
    override fun internalFrameDeactivated(e: InternalFrameEvent) {}

    companion object {
        private val logger = Logger.getLogger(TabDesktop::class.java.name)
    }
}
