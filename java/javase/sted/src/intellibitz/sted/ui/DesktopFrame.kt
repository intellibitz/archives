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
 * $Id: DesktopFrame.kt 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DesktopFrame.kt $
 */

package intellibitz.sted.ui

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.event.FontMapEntriesChangeEvent
import intellibitz.sted.event.IFontMapEntriesChangeListener
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import javax.swing.BorderFactory
import javax.swing.JInternalFrame
import javax.swing.JTabbedPane
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import java.beans.PropertyVetoException
import java.io.File
import java.util.logging.Logger

class DesktopFrame : JInternalFrame(), TableModelListener, FontMapChangeListener, IFontMapEntriesChangeListener {
    private var tabbedPane: JTabbedPane? = null
    private var mapperPanel: MapperPanel? = null
    private var inputFileViewer: FileViewer? = null
    private var outputFileViewer: FileViewer? = null
    private var desktopModel: DesktopModel? = null
    fun init() {
        mapperPanel = MapperPanel()
        mapperPanel.init()
        setNormalIcon()
        tabbedPane = JTabbedPane(JTabbedPane.BOTTOM)
        tabbedPane.setBorder(BorderFactory.createRaisedBevelBorder())
        tabbedPane.addTab(Resources.getResource(Resources.TITLE_TAB_FONTMAP), Resources.getSTEDIcon(), mapperPanel, Resources.getResource(Resources.TIP_TAB_FONTMAP))
        inputFileViewer = FileViewer(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_INPUT)))
        tabbedPane.addTab(Resources.getResource(Resources.TITLE_TAB_INPUT), Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_INPUT)), inputFileViewer)
        outputFileViewer = FileViewer(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_OUTPUT)))
        tabbedPane.addTab(Resources.getResource(Resources.TITLE_TAB_OUTPUT), Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_FILE_OUTPUT)), outputFileViewer)
        tabbedPane.setEnabledAt(0, false)
        tabbedPane.setEnabledAt(1, false)
        tabbedPane.setEnabledAt(2, false)
        getContentPane()
        setBounds(mapperPanel.getBounds())
        setLocation(mapperPanel.getLocation())
        pack()
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE)
        setVisible(true)
    }
    fun load() {
        mapperPanel.load()
        mapperPanel.getMappingEntryPanel()
        mapperPanel.setSampleInput(Resources.getResource(Resources.SAMPLE_INPUT_TEXT))
        tabbedPane.setToolTipTextAt(1, MenuHandler.getToolTips())
        tabbedPane.setToolTipTextAt(2, MenuHandler.getToolTips())
    }
    fun getInputFileViewer(): FileViewer {
        return inputFileViewer
    }
    fun getOutputFileViewer(): FileViewer {
        return outputFileViewer
    }
    fun getModel(): DesktopModel {
        return desktopModel
    }
    fun setModel(model: DesktopModel) {
        desktopModel = model
        desktopModel.addFontMapChangeListener(this)
        desktopModel.addFontMapChangeListener(mapperPanel)
        desktopModel.addFontMapChangeListener(mapperPanel.getMappingEntryPanel())
        desktopModel.addFontMapChangeListener(mapperPanel.getMappingEntryPanel())
    }
    private fun setNormalIcon() {
        setFrameIcon(Resources.getCleanIcon())
    }
    private fun setEditIcon() {
        setFrameIcon(Resources.getDirtyIcon())
    }
    fun clear() {
        mapperPanel.clear()
        desktopModel.clear()
        setTitle(Resources.EMPTY_STRING)
        setNormalIcon()
    }
    private fun setInternalFrameSelected(flag: Boolean) {
        try {
            setSelected(flag)
        }
        catch (e: PropertyVetoException) {
            logger.throwing(getClass(), "setInternalFrameSelected", e)
            e.printStackTrace()
        }
    }
    fun showFrame() {
        setVisible(true)
        tabbedPane.setVisible(true)
        setFrameTitle(getModel())
        try {
            setMaximum(true)
        }
        catch (e: PropertyVetoException) {
            logger.throwing(getClass(), "showFrame", e)
            e.printStackTrace()
        }
        mapperPanel.getMappingEntryPanel()
    }
    private fun hideFrame() {
        setInternalFrameSelected(false)
        setVisible(false)
    }
    fun getMapperPanel(): MapperPanel {
        return mapperPanel
    }
    fun enableTabs(flag: Boolean) {
        val count: Int = tabbedPane.getTabCount()
        if ((count > 0)) {
            var i: Int = 0
            while ((i < count)) {
                tabbedPane.setEnabledAt(i, flag)
                i++
            }
        }
    }
    fun close() {
        hideFrame()
        enableTabs(false)
        val menuHandler: MenuHandler = MenuHandler.getInstance()
        menuHandler.getMenuItem(Resources.ACTION_VIEW_MAPPING)
        menuHandler.getMenuItem(Resources.ACTION_VIEW_SAMPLE)
        menuHandler.getMenuItem(Resources.ACTION_PASTE_COMMAND)
        menuHandler.getMenuItem(Resources.ACTION_DELETE_COMMAND)
        menuHandler.getMenuItem(Resources.ACTION_SELECT_ALL_COMMAND)
        menuHandler.getMenuItem(Resources.ACTION_CUT_COMMAND)
        menuHandler.getMenuItem(Resources.ACTION_COPY_COMMAND)
    }
    fun setEnabledFontMapTab(flag: Boolean) {
        if ((tabbedPane.getTabCount() > 0)) {
            tabbedPane.setEnabledAt(0, flag)
        }
    }
    override fun tableChanged(e: TableModelEvent) {
        if (getModel()) {
            setEditIcon()
        }
    }
    private fun setFileIcon(fontMap: FontMap) {
        if (fontMap.isDirty()) {
            setEditIcon()
        }
        else {
            setNormalIcon()
        }
    }
    fun setFrameTitle(fontMap: FontMap) {
        if ((fontMap.getFontMapFile() != null)) {
            setTitle(fontMap.getFontMapFile())
        }
    }
    override fun stateChanged(e: FontMapChangeEvent) {
        val fontMap: FontMap = e.getFontMap()
        setFrameTitle(fontMap)
        setFileIcon(fontMap)
        inputFileViewer.setFont(fontMap.getFont1())
        outputFileViewer.setFont(fontMap.getFont2())
        enableConverterIfFilesLoaded()
    }
    fun enableConverterIfFilesLoaded(): Boolean {
        var flag: Boolean = desktopModel.isReadyForTransliteration()
        MenuHandler.getInstance()
        return flag
    }
    fun setInputFile(file: File) {
        desktopModel.setInputFile(file)
        if ((file != null)) {
            readFile(1)
        }
    }
    private fun readFile(index: Int) {
        tabbedPane.setEnabledAt(index, true)
        tabbedPane.setSelectedIndex(index)
        when (index) {
            1 -> {
                inputFileViewer.setFileName(desktopModel.getInputFile())
                inputFileViewer.readFile()
                break
            }
            2 -> {
                readOutputFile()
                break
            }
            else -> {
                break
            }
        }
    }
    fun setOutputFile(file: File) {
        desktopModel.setOutputFile(file)
        if ((file != null)) {
            readFile(2)
        }
    }
    fun readOutputFile() {
        if ((desktopModel.getOutputFile() != null)) {
            outputFileViewer.setFileName(desktopModel.getOutputFile())
            outputFileViewer.readFile()
        }
    }
    override fun stateChanged(e: FontMapEntriesChangeEvent) {
        desktopModel.fireFontMapChangedEvent()
    }

    companion object {
        private val logger: Logger = Logger.getLogger("intellibitz.sted.ui.DesktopFrame")
    }
}
