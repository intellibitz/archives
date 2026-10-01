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
 * $Id: TabDesktop.kt 59 2007-05-19 08:11:31Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/TabDesktop.kt $
 */

package intellibitz.sted.ui

import intellibitz.sted.actions.RedoAction
import intellibitz.sted.actions.UndoAction
import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.event.FontMapReadEvent
import intellibitz.sted.event.IMessageEventSource
import intellibitz.sted.event.IMessageListener
import intellibitz.sted.event.IStatusEventSource
import intellibitz.sted.event.IStatusListener
import intellibitz.sted.event.IThreadListener
import intellibitz.sted.event.MessageEvent
import intellibitz.sted.event.StatusEvent
import intellibitz.sted.event.ThreadEvent
import intellibitz.sted.event.TransliterateEvent
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.io.FileFilterHelper
import intellibitz.sted.io.FontMapReader
import intellibitz.sted.launch.STEDGUI
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.ButtonTabComponent
import javax.swing.JDesktopPane
import javax.swing.JFileChooser
import javax.swing.JMenu
import javax.swing.JOptionPane
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.InternalFrameEvent
import javax.swing.event.InternalFrameListener
import javax.xml.transform.TransformerException
import java.awt.HeadlessException
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyVetoException
import java.io.File
import java.io.IOException
import java.util.Collection
import java.util.HashMap
import java.util.Map
import java.util.Set
import java.util.TreeSet
import java.util.logging.Logger

class TabDesktop : JTabbedPane(), InternalFrameListener, IThreadListener, FontMapChangeListener, ChangeListener, ActionListener, IStatusEventSource, IMessageEventSource {
    private var desktopPane: JDesktopPane? = null
    private var frameNumberIndex: FrameNumberIndex = FrameNumberIndex()
    private var frameCache: MutableMap<String, DesktopFrame> = HashMap<String, DesktopFrame>()
    private var clipboard: MutableMap<String, Collection> = HashMap<String, Collection>()
    private var statusListener: IStatusListener? = null
    private var statusEvent: StatusEvent? = null
    private var messageListener: IMessageListener? = null
    private var messageEvent: MessageEvent? = null
    fun init() {
        desktopPane = JDesktopPane()
        addChangeListener(this)
        statusEvent = StatusEvent(this)
        messageEvent = MessageEvent(this)
        setVisible(true)
    }
    fun load() {

    }
    fun fireStatusPosted(message: String) {
        statusEvent.setStatus(message)
        statusListener.statusPosted(statusEvent)
    }
    fun fireStatusPosted() {
        statusListener.statusPosted(statusEvent)
    }
    fun addStatusListener(statusListener: IStatusListener) {
        this = statusListener
    }
    fun addTab(desktopFrame: DesktopFrame) {
        addTab(desktopFrame.getTitle(), desktopFrame)
    }
    fun addTab(title: String, desktopFrame: DesktopFrame) {
        desktopFrame.setTitle(title)
        desktopFrame.removeInternalFrameListener(this)
        desktopFrame.addInternalFrameListener(this)
        desktopFrame.getModel()
        desktopFrame.getModel()
        super.addTab(title, Resources.getSTEDIcon(), desktopFrame, Resources.getResource(Resources.TIP_TAB_FONTMAP))
        initTabComponent((getTabCount() - 1), title)
        setEnabledAt((getTabCount() - 1), true)
        setSelectedIndex((getTabCount() - 1))
        try {
            desktopFrame.setSelected(true)
        }
        catch (e: PropertyVetoException) {
            e.printStackTrace()
        }
    }
    private fun initTabComponent(i: Int, title: String) {
        var buttonTabComponent: ButtonTabComponent = ButtonTabComponent(("TabComponent " + i), this)
        buttonTabComponent.getTabTitle()
        buttonTabComponent.getTabTitle()
        buttonTabComponent.addActionListener(this)
        setTabComponentAt(i, buttonTabComponent)
    }
    fun closeFontMap(): Int {
        return closeFontMap(getFontMapperDesktopFrame())
    }
    fun closeFontMap(desktopFrame: DesktopFrame): Int {
        var i: Int = saveDirty(desktopFrame)
        if ((JOptionPane.CANCEL_OPTION != i)) {
            var index: Int = getSelectedIndex()
            if ((index == 1)) {
                index = (getTabCount() - 1)
            }
            removeTabFrameAt(index)
            desktopFrame.close()
        }
        enableCloseAction()
        return i
    }
    fun closeFontMap(desktopFrame: DesktopFrame, i: Int): Int {
        var result: Int = saveDirty(desktopFrame)
        if ((JOptionPane.CANCEL_OPTION != result)) {
            removeTabFrameAt(i)
            desktopFrame.close()
        }
        enableCloseAction()
        return i
    }
    private fun removeTabFrameAt(index: Int): Boolean {
        if ((index > 1)) {
            var desktopFrame: DesktopFrame = (getComponentAt(index) as DesktopFrame)
            var title: String = desktopFrame.getTitle()
            if (title.startsWith(Resources.ACTION_FILE_NEW_COMMAND)) {
                var indx: Int = getNewIndexNumber(title)
                frameNumberIndex.removeIndex(indx)
            }
            removeTabAt(index)
            return true
        }
        return false
    }
    private fun getNewIndexNumber(title: String): Int {
        return Integer.valueOf(title.substring(Resources.ACTION_FILE_NEW_COMMAND.length()))
    }
    private fun createNewFrameTitle(num: Int): String {
        return ((Resources.ACTION_FILE_NEW_COMMAND + " ") + frameNumberIndex.addNewIndex(num))
    }
    override fun stateChanged(e: FontMapChangeEvent) {
        var desktopFrame: DesktopFrame = getSelectedFrame()
        var title: String = desktopFrame.getTitle()
        if ((null != title)) {
            var indx: Int = indexOfTab(title)
            if ((1 == indx)) {
                addTab(desktopFrame)
            }
            else {
                setEnabledAt(indx, true)
            }
        }
        else {
            addTab(desktopFrame)
        }
        var buttonTabComponent: ButtonTabComponent = (getTabComponentAt(getSelectedIndex()) as ButtonTabComponent)
        buttonTabComponent.getTabTitle()
        var fontMap: FontMap = desktopFrame.getModel()
        if ((fontMap.getFontMapFile() != null)) {
            buttonTabComponent.getTabTitle()
        }
        if (fontMap.isDirty()) {
            buttonTabComponent.getTabTitle()
        }
        else {
            buttonTabComponent.getTabTitle()
        }
        fireStatusPosted((buttonTabComponent.getTabTitle() + " Active"))
        this
    }
    override fun stateChanged(e: ChangeEvent) {
        var me: TabDesktop = (e.getSource() as TabDesktop)
        var i: Int = me.getSelectedIndex()
        if ((i != 1)) {
            var myframe: DesktopFrame = (getComponentAt(i) as DesktopFrame)
            desktopPane.setSelectedFrame(myframe)
            i = me.getSelectedIndex()
            var buttonTab: ButtonTabComponent = (getTabComponentAt(i) as ButtonTabComponent)
            var title: String = myframe.getTitle()
            if ((buttonTab != null)) {
                title = buttonTab.getTabTitle()
            }
            fireStatusPosted((title + " Active"))
        }
        enableCloseAction()
    }
    private fun enableCloseAction(flag: Boolean) {
        MenuHandler.getInstance()
    }
    private fun enableCloseAction() {
        if ((getTabCount() > 0)) {
            enableCloseAction(true)
        }
        else {
            enableCloseAction(false)
        }
    }
    override fun actionPerformed(e: ActionEvent) {
        var buttonTabComponent: ButtonTabComponent = (e.getSource() as ButtonTabComponent)
        var i: Int = indexOfTabComponent(buttonTabComponent)
        if ((i != 1)) {
            var desktopFrame: DesktopFrame = (getComponentAt(i) as DesktopFrame)
            closeFontMap(desktopFrame, i)
        }
    }
    private fun getSelectedFrame(): DesktopFrame {
        var desktopFrame: DesktopFrame = (desktopPane.getSelectedFrame() as DesktopFrame)
        return desktopFrame
    }
    fun addListenersToDesktopFrame(desktopFrame: DesktopFrame) {
        var mapperPanel: MapperPanel = desktopFrame.getMapperPanel()
        var desktopModel: DesktopModel = desktopFrame.getModel()
        var fontMap: FontMap = desktopModel.getFontMap()
        fontMap.removeFontMapChangeListener(mapperPanel)
        fontMap.addFontMapChangeListener(mapperPanel)
        fontMap.removeFontMapChangeListener(mapperPanel.getMappingEntryPanel())
        fontMap.addFontMapChangeListener(mapperPanel.getMappingEntryPanel())
        fontMap.removeFontMapChangeListener(desktopFrame)
        fontMap.addFontMapChangeListener(desktopFrame)
        val menuHandler: MenuHandler = MenuHandler.getInstance()
        val actions: Map = menuHandler.getActions()
        val newAction: FontMapChangeListener = (menuHandler.getAction(Resources.ACTION_FILE_NEW_COMMAND) as FontMapChangeListener)
        fontMap.removeFontMapChangeListener(newAction)
        fontMap.addFontMapChangeListener(newAction)
        val reload: FontMapChangeListener = (actions.get(Resources.ACTION_FILE_RELOAD) as FontMapChangeListener)
        fontMap.removeFontMapChangeListener(reload)
        fontMap.addFontMapChangeListener(reload)
        val save: FontMapChangeListener = (actions.get(Resources.ACTION_FILE_SAVE_COMMAND) as FontMapChangeListener)
        fontMap.removeFontMapChangeListener(save)
        fontMap.addFontMapChangeListener(save)
        val paste: FontMapChangeListener = (actions.get(Resources.ACTION_PASTE_COMMAND) as FontMapChangeListener)
        fontMap.removeFontMapChangeListener(paste)
        fontMap.addFontMapChangeListener(paste)
        val undo: UndoAction = (actions.get(Resources.ACTION_UNDO_COMMAND) as UndoAction)
        fontMap.removeUndoListener(undo)
        undo.setEnabled(false)
        fontMap.addUndoListener(undo)
        this
        this
        val redo: RedoAction = (actions.get(Resources.ACTION_REDO_COMMAND) as RedoAction)
        fontMap.removeRedoListener(redo)
        redo.setEnabled(false)
        fontMap.addRedoListener(redo)
        this
        this
        val keypad1: FontKeypad1 = mapperPanel.getFontKeypad1()
        fontMap.removeFontListChangeListener(keypad1)
        fontMap.addFontListChangeListener(keypad1)
        desktopModel.addFontMapChangeListener(keypad1)
        val keypad2: FontKeypad2 = mapperPanel.getFontKeypad2()
        fontMap.removeFontListChangeListener(keypad2)
        fontMap.addFontListChangeListener(keypad2)
        desktopModel.addFontMapChangeListener(keypad2)
        desktopFrame.addInternalFrameListener((menuHandler.getActions() as InternalFrameListener))
        desktopFrame.addInternalFrameListener((menuHandler.getActions() as InternalFrameListener))
        desktopFrame.addInternalFrameListener((menuHandler.getActions() as InternalFrameListener))
        desktopFrame.addInternalFrameListener((menuHandler.getActions() as InternalFrameListener))
        desktopFrame.addInternalFrameListener((menuHandler.getActions() as InternalFrameListener))
        desktopModel.removeFontMapChangeListener(newAction)
        desktopModel.addFontMapChangeListener(newAction)
        val reopen: FontMapChangeListener = (menuHandler.getActions() as FontMapChangeListener)
        desktopModel.removeFontMapChangeListener(reopen)
        desktopModel.addFontMapChangeListener(reopen)
        desktopFrame.addInternalFrameListener((reopen as InternalFrameListener))
    }
    fun createDesktopModel(desktopFrame: DesktopFrame, fontMap: FontMap): DesktopModel {
        var desktopModel: DesktopModel = DesktopModel()
        desktopModel.setFontMap(fontMap)
        desktopFrame.setModel(desktopModel)
        addListenersToDesktopFrame(desktopFrame)
        desktopFrame.load()
        return desktopModel
    }
    fun loadFontMap(file: File) {
        loadFontMap(getSelectedFrame(), file)
    }
    fun loadFontMap(desktopFrame: DesktopFrame, file: File) {
        var desktopModel: DesktopModel = createDesktopModel(desktopFrame, FontMap(file))
        desktopModel.addFontMapChangeListener(this)
        readFontMap(desktopModel)
        fireStatusPosted("FontMap loaded")
    }
    fun readFontMap(desktopModel: DesktopModel) {
        var fontMap: FontMap = desktopModel.getFontMap()
        try {
            val fontMapReader: FontMapReader = FontMapReader(fontMap)
            fontMapReader.addThreadListener(this)
            SwingUtilities.invokeLater(fontMapReader)
        }
        catch (e: IllegalArgumentException) {
            fireMessagePosted(("Cannot Read FontMap.. Failed: " + e.getMessage()))
            logger.severe(("Cannot Read FontMap - Illegal Argument " + fontMap.getFontMapFile()))
            fireStatusPosted(("Cannot Read FontMap - Illegal Argument " + fontMap.getFontMapFile()))
        }
    }
    fun openFontMap() {
        val selectedFile: File = FileHelper.openFile("Please select FontMap location:", Resources.XML, "STED FontMap files", this)
        if ((selectedFile != null)) {
            openFontMap(selectedFile)
        }
    }
    fun newFontMap() {
        var desktopFrame: DesktopFrame = loadNewFontMap()
        var num: Int = (getTabCount() + 1)
        addTab(createNewFrameTitle(num), desktopFrame)
        fireStatusPosted("New FontMap")
    }
    fun loadNewFontMap(): DesktopFrame {
        var desktopFrame: DesktopFrame = createFontMapperDesktopFrame()
        var fontMap: FontMap = FontMap()
        var desktopModel: DesktopModel = createDesktopModel(desktopFrame, fontMap)
        desktopModel.fireFontMapChangedEvent()
        return desktopFrame
    }
    fun reopenFontMap(fileName: String) {
        openFontMap(File(fileName))
    }
    fun reloadFontMap() {
        var selectedFrame: DesktopFrame = getSelectedFrame()
        var selectedFile: File = selectedFrame.getModel()
        removeTabFrameAt(getSelectedIndex())
        frameCache.remove(selectedFrame.getModel())
        openFontMap(selectedFile)
    }
    fun openFontMap(selectedFile: File) {
        try {
            var desktopFrame: DesktopFrame = frameCache.get(selectedFile.getAbsolutePath())
            if ((desktopFrame == null)) {
                desktopFrame = createFontMapperDesktopFrame()
                loadFontMap(desktopFrame, selectedFile)
                var desktopModel: DesktopModel = desktopFrame.getModel()
                var fontMap: FontMap = desktopModel.getFontMap()
                frameCache.put(fontMap.getFileName(), desktopFrame)
            }
            add(desktopFrame)
            desktopPane.setSelectedFrame(desktopFrame)
            desktopFrame.getModel()
        }
        catch (ex: HeadlessException) {
            logger.throwing("intellibitz.sted.util.FontMapHelper", "readFontMap", ex)
            JOptionPane.showMessageDialog(this, ("Invalid FontMap " + selectedFile.getAbsolutePath()))
        }
        catch (ex: IllegalArgumentException) {
            logger.throwing("intellibitz.sted.util.FontMapHelper", "readFontMap", ex)
            JOptionPane.showMessageDialog(this, ("Load Failed: " + ex.getMessage()))
        }
    }
    private fun saveFontMap() {
        var desktopFrame: DesktopFrame = getSelectedFrame()
        var fontMap: FontMap = desktopFrame.getModel()
        fontMap.setFont1(desktopFrame.getMapperPanel())
        fontMap.setFont2(desktopFrame.getMapperPanel())
        try {
            fontMap = desktopFrame.getModel()
        }
        catch (exception: TransformerException) {
            exception.printStackTrace()
            JOptionPane.showMessageDialog(this, ((fontMap.getFileName() + " cannot create for writing ") + exception.getMessage()))
        }
    }
    fun saveAction() {
        var fontMap: FontMap = getFontMap()
        val selectedFile: File = fontMap.getFontMapFile()
        if ((selectedFile == null)) {
            saveAsAction()
        }
        else {
            if (!selectedFile.canWrite()) {
                try {
                    selectedFile.createNewFile()
                    fontMap.setFontMapFile(selectedFile)
                }
                catch (exception: IOException) {
                    JOptionPane.showMessageDialog(this, ((selectedFile + " cannot create for writing ") + exception.getMessage()))
                }
            }
            else {
                saveFontMap()
            }
        }
    }
    fun saveAsAction(): Int {
        var fontMap: FontMap = getFontMap()
        val jFileChooser: JFileChooser = JFileChooser(System.getProperty("user.dir"))
        val fileFilterHelper: FileFilterHelper = FileFilterHelper("xml", "STED FontMap files")
        jFileChooser.setFileFilter(fileFilterHelper)
        val result: Int = jFileChooser.showSaveDialog(this)
        if ((result == JFileChooser.APPROVE_OPTION)) {
            var selectedFile: File = jFileChooser.getSelectedFile()
            if (selectedFile.canWrite()) {
                fontMap.setFontMapFile(selectedFile)
                saveFontMap()
            }
            else {
                JOptionPane.showMessageDialog(this, (selectedFile + " is NOT Writable"))
            }
        }
        return result
    }
    fun saveDirty(): Int {
        return saveDirty(getSelectedFrame())
    }
    fun saveDirty(desktopFrame: DesktopFrame): Int {
        var result: Int = JOptionPane.CLOSED_OPTION
        if ((null != desktopFrame)) {
            var fontMap: FontMap = desktopFrame.getModel()
            if (((fontMap != null) && fontMap.isDirty())) {
                result = JOptionPane.showConfirmDialog(this, "FontMap Changed.. Do you want to save changes?", "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION)
                if ((JOptionPane.YES_OPTION == result)) {
                    if (fontMap.isNew()) {
                        result = saveAsAction()
                        if ((JFileChooser.CANCEL_OPTION == result)) {
                            fontMap.clear()
                        }
                    }
                    else {
                        saveAction()
                    }
                }
                else {
                    if ((JOptionPane.NO_OPTION == result)) {
                        fontMap.clear()
                    }
                }
            }
        }
        return result
    }
    fun clear() {
        getSelectedFrame()
    }
    fun getFontMap(): FontMap {
        return getSelectedFrame()
    }
    fun addItemToReOpenMenu(item: String) {
        val menuHandler: MenuHandler = MenuHandler.getInstance()
        val menu: JMenu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
        MenuHandler.addReOpenItem(menu, item)
        menu.setEnabled((menu.getItemCount() != (Resources.DEFAULT_MENU_COUNT + 1)))
    }
    fun createFontMapperDesktopFrame(): DesktopFrame {
        var desktopFrame: DesktopFrame = DesktopFrame()
        desktopFrame.addInternalFrameListener(this)
        desktopFrame.init()
        desktopPane.add(desktopFrame)
        desktopPane.setSelectedFrame(desktopFrame)
        desktopFrame.setEnabled(true)
        desktopFrame.setVisible(true)
        return desktopFrame
    }
    fun getClipboard(): Map<String, Collection> {
        return clipboard
    }
    fun addToClipboard(entry: String, value: Collection) {
        clipboard.put(entry, value)
        fireStatusPosted("Copied Fontmap Entries")
    }
    fun getFontMapperDesktopFrame(): DesktopFrame {
        return getSelectedFrame()
    }
    fun getDesktopModel(): DesktopModel {
        return getFontMapperDesktopFrame()
    }
    fun getFrameTitle(): String {
        var desktopFrame: DesktopFrame = getSelectedFrame()
        if ((null == desktopFrame)) {
            return null
        }
        else {
            return desktopFrame.getTitle()
        }
    }
    fun fireMessagePosted(message: String) {
        messageEvent.setMessage(message)
        messageListener.messagePosted(messageEvent)
    }
    fun fireMessagePosted() {
        messageListener.messagePosted(messageEvent)
    }
    fun addMessageListener(messageListener: IMessageListener) {
        this = messageListener
    }
    override fun threadRunStarted(e: ThreadEvent) {
        STEDGUI.busy()
    }
    override fun threadRunning(e: ThreadEvent) {

    }
    override fun threadRunFailed(e: ThreadEvent) {
        STEDGUI.busy()
        var message: String = e.getEventSource()
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
        if (FontMapReadEvent::class.java) {
            closeFontMap()
        }
        if (TransliterateEvent::class.java) {
            MenuHandler.getInstance()
            MenuHandler.getInstance()
        }
        fireStatusPosted(message)
        STEDGUI.relax()
    }
    override fun threadRunFinished(e: ThreadEvent) {
        if (FontMapReadEvent::class.java) {
            var desktopModel: DesktopModel = getSelectedFrame()
            val fontMap: FontMap = desktopModel.getFontMap()
            fontMap.setDirty(false)
            desktopModel.fireFontMapChangedEvent()
            fireStatusPosted("FontMap Loaded")
        }
        else {
            if (TransliterateEvent::class.java) {
                getSelectedFrame()
                MenuHandler.getInstance()
                MenuHandler.getInstance()
                fireStatusPosted("Transliterate Done")
            }
        }
        STEDGUI.relax()
    }
    fun internalFrameClosing(e: InternalFrameEvent) {
        var desktopFrame: DesktopFrame = (e.getInternalFrame() as DesktopFrame)
        if ((JOptionPane.CANCEL_OPTION != saveDirty(desktopFrame))) {
            var index: Int = getSelectedIndex()
            removeTabFrameAt(index)
            desktopFrame.close()
        }
        enableCloseAction()
    }
    fun internalFrameActivated(e: InternalFrameEvent) {
        MenuHandler.getInstance()
        MenuHandler.getInstance()
        var desktopFrame: DesktopFrame = (e.getInternalFrame() as DesktopFrame)
        desktopFrame.setEnabledFontMapTab(true)
        enableCloseAction()
    }
    fun internalFrameOpened(e: InternalFrameEvent) {
        enableCloseAction()
    }
    fun internalFrameClosed(e: InternalFrameEvent) {
        enableCloseAction()
    }
    fun internalFrameIconified(e: InternalFrameEvent) {

    }
    fun internalFrameDeiconified(e: InternalFrameEvent) {

    }
    fun internalFrameDeactivated(e: InternalFrameEvent) {

    }
    class FrameNumberIndex {
    private var indices: Set<Integer> = TreeSet<Integer>()
    constructor() : super()
    fun addNewIndex(indx: Int): Int {
        var sz: Int = indices.size()
        if ((!containsIndex(indx) && (indx >= sz))) {
            indices.add(indx)
            return indx
        }
        else {
            var i: Int = 1
            while ((i <= sz)) {
                if (!containsIndex(i)) {
                    indices.add(i)
                    return i
                }
                i++
            }
        }
        return indx
    }
    fun removeIndex(indx: Int): Boolean {
        return indices.remove(indx)
    }
    fun containsIndex(indx: Int): Boolean {
        for (indice in indices) {
            if ((indice == indx)) {
                return true
            }
        }
        return false
    }
    }

    companion object {
        private val logger: Logger = Logger.getLogger(TabDesktop::class.java)
    }
}
