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
 * $Id: STEDWindow.kt 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/STEDWindow.kt $
 */

/**
 * The sted UI package
 * Contains the swing windows and widgets
 */

package intellibitz.sted.ui

import intellibitz.sted.actions.ExitAction
import intellibitz.sted.actions.ItemListenerAction
import intellibitz.sted.actions.STEDWindowAction
import intellibitz.sted.event.IMessageListener
import intellibitz.sted.event.IStatusEventSource
import intellibitz.sted.event.IStatusListener
import intellibitz.sted.event.IThreadListener
import intellibitz.sted.event.MessageEvent
import intellibitz.sted.event.StatusEvent
import intellibitz.sted.event.ThreadEvent
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.io.FileReaderThread
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import javax.swing.Action
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JProgressBar
import javax.swing.JToolBar
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.ArrayList
import java.util.Map
import java.util.logging.Logger

class STEDWindow : JFrame(), IThreadListener, ChangeListener, IMessageListener, IStatusEventSource {
    private var tabDesktop: TabDesktop? = null
    private var statusPanel: StatusPanel? = null
    private var statusListener: IStatusListener? = null
    private var statusEvent: StatusEvent? = null
    fun init() {
        setTitle(Resources.getSTEDTitle())
        setDefaultLookAndFeelDecorated(true)
        setState(JFrame.MAXIMIZED_BOTH)
        setExtendedState(JFrame.MAXIMIZED_BOTH)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
        setIconImage(Resources.getSTEDImage())
        statusEvent = StatusEvent(this)
        val menuBar: JMenuBar = MenuHandler.getInstance()
        MenuHandler.loadLookAndFeelMenu(this)
        setJMenuBar(menuBar)
        fireStatusPosted("20")
        val container: Container = getContentPane()
        val gridBagLayout: GridBagLayout = GridBagLayout()
        container.setLayout(gridBagLayout)
        val gridBagConstraints: GridBagConstraints = GridBagConstraints()
        gridBagConstraints.gridwidth = 1
        gridBagConstraints.gridheight = 1
        gridBagConstraints.weightx = 1.0
        gridBagConstraints.weighty = 0.0
        gridBagConstraints.gridx = 0
        gridBagConstraints.gridy = 0
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
        fireStatusPosted("30")
        val toolBar: JToolBar = MenuHandler.getInstance()
        gridBagLayout.setConstraints(toolBar, gridBagConstraints)
        container.add(toolBar)
        fireStatusPosted("40")
        tabDesktop = TabDesktop()
        tabDesktop.init()
        fireStatusPosted("50")
        gridBagConstraints.weighty = 1.0
        gridBagConstraints.gridy = 1
        gridBagConstraints.fill = GridBagConstraints.BOTH
        gridBagLayout.setConstraints(tabDesktop, gridBagConstraints)
        container.add(tabDesktop)
        fireStatusPosted("60")
        statusPanel = StatusPanel(this)
        gridBagConstraints.weighty = 0.0
        gridBagConstraints.gridy = 2
        gridBagConstraints.fill = GridBagConstraints.BOTH
        gridBagLayout.setConstraints(statusPanel, gridBagConstraints)
        container.add(statusPanel)
        fireStatusPosted("70")
        setUserOptions()
        addMouseListener(AboutSTED.getInstance())
        var exitAction: ExitAction = ExitAction()
        exitAction.setSTEDWindow(this)
        addWindowListener(exitAction)
        pack()
        logger.finest("successfully intialized STEDWindow")
        fireStatusPosted("80")
    }
    fun load() {
        tabDesktop.addStatusListener(statusPanel)
        var actions: Map<String, Action> = MenuHandler.getInstance()
        for (action in actions.values()) {
            if (STEDWindowAction::class.java) {
                (action as STEDWindowAction)
                (action as STEDWindowAction)
            }
        }
        tabDesktop.load()
        tabDesktop.addChangeListener(this)
        setState(JFrame.MAXIMIZED_HORIZ)
        setExtendedState(JFrame.MAXIMIZED_BOTH)
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
    fun setVisible() {
        super.setVisible(true)
        statusPanel.runMemoryBar()
    }
    fun getStatusPanel(): StatusPanel {
        return statusPanel
    }
    fun getDesktop(): TabDesktop {
        return tabDesktop
    }
    private fun setUserOptions() {
        val menuItems: Map<String, JMenuItem> = MenuHandler.getInstance()
        for (key in menuItems.keySet()) {
            val menuItem: JMenuItem = menuItems.get(key)
            val action: Action = menuItem.getAction()
            if (ItemListenerAction::class.java) {
                val val: String = Resources.getSetting(key)
                if ((val != null)) {
                    val curr: Boolean = Boolean.valueOf(val)
                    if (!action.isEnabled()) {
                        menuItem.setSelected(curr)
                    }
                    else {
                        if ((curr && !menuItem.isSelected())) {
                            menuItem.doClick()
                        }
                        else {
                            if ((menuItem.isSelected() && !curr)) {
                                menuItem.doClick()
                            }
                        }
                    }
                }
            }
        }
        val reopenItems: ArrayList<String> = Resources.getSettingBeginsWith(Resources.ACTION_FILE_REOPEN_COMMAND)
        if (!reopenItems.isEmpty()) {
            val menu: JMenu = MenuHandler.getInstance()
            for (reopenItem in reopenItems) {
                MenuHandler.addReOpenItem(menu, reopenItem)
            }
            menu.setEnabled((menu.getItemCount() > Resources.DEFAULT_MENU_COUNT))
        }
        var sampleFontMapPaths: Array<String> = FileHelper.getSampleFontMapPaths(Resources.getResourceDirPath())
        if ((sampleFontMapPaths.length > 0)) {
            val menu: JMenu = MenuHandler.getInstance()
            for (reopenItem in sampleFontMapPaths) {
                MenuHandler.addSampleFontMapMenuItem(menu, reopenItem)
            }
        }
    }
    override fun threadRunStarted(e: ThreadEvent) {
        var progressBar: JProgressBar = statusPanel.getProgressBar()
        progressBar.setMinimum(0)
        progressBar.setIndeterminate(true)
    }
    override fun threadRunning(e: ThreadEvent) {
        var progressBar: JProgressBar = statusPanel.getProgressBar()
        progressBar.setMaximum(e.getEventSource())
        progressBar.setValue(e.getEventSource())
    }
    override fun threadRunFailed(e: ThreadEvent) {
        JOptionPane.showMessageDialog(this, e.getEventSource())
        var progressBar: JProgressBar = statusPanel.getProgressBar()
        progressBar.setValue(0)
        progressBar.setIndeterminate(false)
    }
    override fun threadRunFinished(e: ThreadEvent) {
        var progressBar: JProgressBar = statusPanel.getProgressBar()
        progressBar.setValue(0)
        progressBar.setIndeterminate(false)
        var source: FileReaderThread = (e.getEventSource() as FileReaderThread)
        statusPanel.setStatus(("Read File: " + source.getFile()))
    }
    override fun stateChanged(e: ChangeEvent) {
        var desktop: TabDesktop = (e.getSource() as TabDesktop)
        var index: Int = desktop.getSelectedIndex()
        if ((index > 1)) {
            var dframe: DesktopFrame = (desktop.getComponentAt(index) as DesktopFrame)
            dframe.getInputFileViewer()
            dframe.getOutputFileViewer()
            var desktopModel: DesktopModel = dframe.getModel()
            val fontMap: FontMap = desktopModel.getFontMap()
            fontMap.removeFontMapChangeListener(statusPanel)
            fontMap.addFontMapChangeListener(statusPanel)
            statusPanel.setLockFlag(!fontMap.isFileWritable())
            statusPanel.setNeatness(fontMap)
            dframe.getMapperPanel()
            dframe.getMapperPanel()
            dframe.getMapperPanel()
            dframe.getMapperPanel()
            dframe.getMapperPanel()
            dframe.getMapperPanel()
            dframe.getMapperPanel()
        }
    }
    override fun messagePosted(event: MessageEvent) {
        JOptionPane.showMessageDialog(this, event.getMessage())
    }

    companion object {
        private val logger: Logger = Logger.getLogger(STEDWindow::class.java)
    }
}
