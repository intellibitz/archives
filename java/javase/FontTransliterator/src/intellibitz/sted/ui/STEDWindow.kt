package intellibitz.sted.ui

import intellibitz.sted.actions.ExitAction
import intellibitz.sted.actions.ItemListenerAction
import intellibitz.sted.actions.STEDWindowAction
import intellibitz.sted.event.*
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.io.FileReaderThread
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.ArrayList
import java.util.logging.Logger
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class STEDWindow : JFrame(), IThreadListener, ChangeListener, IMessageListener, IStatusEventSource {
    var desktop: TabDesktop? = null
        private set
    var statusPanel: StatusPanel? = null
        private set
    private var statusListener: IStatusListener? = null
    private var statusEvent: StatusEvent? = null

    fun init() {
        title = Resources.getSTEDTitle()
        JFrame.setDefaultLookAndFeelDecorated(true)
        state = MAXIMIZED_BOTH
        extendedState = MAXIMIZED_BOTH
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        iconImage = Resources.getSTEDImage()

        statusEvent = StatusEvent(this)

        val menuBar = MenuHandler.instance!!.getMenuBar(Resources.MENUBAR_STED)
        MenuHandler.loadLookAndFeelMenu(this)
        jMenuBar = menuBar
        fireStatusPosted("20")

        val container = contentPane
        val gridBagLayout = GridBagLayout()
        container.layout = gridBagLayout
        val gridBagConstraints = GridBagConstraints()
        gridBagConstraints.gridwidth = 1
        gridBagConstraints.gridheight = 1
        gridBagConstraints.weightx = 1.0
        gridBagConstraints.weighty = 0.0
        gridBagConstraints.gridx = 0
        gridBagConstraints.gridy = 0
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL
        fireStatusPosted("30")

        val toolBar = MenuHandler.instance!!.getToolBar(Resources.MENUBAR_STED)
        gridBagLayout.setConstraints(toolBar, gridBagConstraints)
        container.add(toolBar)
        fireStatusPosted("40")

        desktop = TabDesktop()
        desktop!!.init()
        fireStatusPosted("50")
        gridBagConstraints.weighty = 1.0
        gridBagConstraints.gridy = 1
        gridBagConstraints.fill = GridBagConstraints.BOTH
        gridBagLayout.setConstraints(desktop, gridBagConstraints)
        container.add(desktop)
        fireStatusPosted("60")

        statusPanel = StatusPanel(this)
        gridBagConstraints.weighty = 0.0
        gridBagConstraints.gridy = 2
        gridBagConstraints.fill = GridBagConstraints.BOTH
        gridBagLayout.setConstraints(statusPanel, gridBagConstraints)
        container.add(statusPanel)
        fireStatusPosted("70")

        setUserOptions()
        addMouseListener(AboutSTED.instance)

        val exitAction = ExitAction()
        exitAction.setSTEDWindow(this)
        addWindowListener(exitAction)

        pack()
        logger.finest("successfully intialized STEDWindow")
        fireStatusPosted("80")
    }

    fun load() {
        desktop!!.addStatusListener(statusPanel!!)

        val actions = MenuHandler.actions
        for (action in actions.values) {
            if (action is STEDWindowAction) {
                action.addStatusListener(statusPanel!!)
                action.addMessageListener(this)
            }
        }

        desktop!!.load()
        desktop!!.addChangeListener(this)

        state = MAXIMIZED_HORIZ
        extendedState = MAXIMIZED_BOTH
    }

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

    fun setVisible() {
        super.setVisible(true)
        statusPanel!!.runMemoryBar()
    }

    private fun setUserOptions() {
        val menuItems = MenuHandler.menuItems
        for (key in menuItems.keys) {
            val menuItem = menuItems[key]!!
            val action = menuItem.action
            if (action is ItemListenerAction) {
                val valStr = Resources.getSetting(key)
                if (valStr != null) {
                    val curr = java.lang.Boolean.valueOf(valStr)
                    if (!action.isEnabled) {
                        menuItem.setSelected(curr)
                    } else if (curr && !menuItem.isSelected) {
                        menuItem.doClick()
                    } else if (menuItem.isSelected && !curr) {
                        menuItem.doClick()
                    }
                }
            }
        }
        val reopenItems = Resources.getSettingBeginsWith(Resources.ACTION_FILE_REOPEN_COMMAND)
        if (reopenItems.isNotEmpty()) {
            val menu = MenuHandler.instance!!.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            for (reopenItem in reopenItems) {
                MenuHandler.addReOpenItem(menu!!, reopenItem)
            }
            menu!!.isEnabled = menu.itemCount > Resources.DEFAULT_MENU_COUNT
        }

        val sampleFontMapPaths = FileHelper.getSampleFontMapPaths(Resources.getResourceDirPath())
        if (sampleFontMapPaths.isNotEmpty()) {
            val menu = MenuHandler.instance!!.getMenu(Resources.MENU_SAMPLES_NAME)
            for (reopenItem in sampleFontMapPaths) {
                MenuHandler.addSampleFontMapMenuItem(menu!!, reopenItem)
            }
        }
    }

    override fun threadRunStarted(e: ThreadEvent) {
        val progressBar = statusPanel!!.progressBar
        progressBar.minimum = 0
        progressBar.isIndeterminate = true
    }

    override fun threadRunning(e: ThreadEvent) {
        val progressBar = statusPanel!!.progressBar
        progressBar.maximum = e.eventSource.progressMaximum
        progressBar.value = e.eventSource.progress
    }

    override fun threadRunFailed(e: ThreadEvent) {
        JOptionPane.showMessageDialog(this, e.eventSource.message)
        val progressBar = statusPanel!!.progressBar
        progressBar.value = 0
        progressBar.isIndeterminate = false
    }

    override fun threadRunFinished(e: ThreadEvent) {
        val progressBar = statusPanel!!.progressBar
        progressBar.value = 0
        progressBar.isIndeterminate = false
        val source = e.eventSource as FileReaderThread
        statusPanel!!.setStatus("Read File: " + source.file)
    }

    override fun stateChanged(e: ChangeEvent) {
        val desktopTab = e.source as TabDesktop
        val index = desktopTab.selectedIndex
        if (index > -1) {
            val dframe = desktopTab.getComponentAt(index) as DesktopFrame
            dframe.inputFileViewer?.addThreadListener(this)
            dframe.outputFileViewer?.addThreadListener(this)

            val desktopModel = dframe.desktopModel
            val fontMap = desktopModel!!.fontMap!!

            fontMap.removeFontMapChangeListener(statusPanel!!)
            fontMap.addFontMapChangeListener(statusPanel!!)

            statusPanel!!.setLockFlag(!fontMap.isFileWritable())
            statusPanel!!.setNeatness(fontMap)
            
            val mappingEntryPanel = dframe.mapperPanel!!.mappingEntryPanel
            mappingEntryPanel.entryAction!!.addStatusListener(statusPanel!!)
            mappingEntryPanel.entryAction!!.addMessageListener(this)
            mappingEntryPanel.mappingTableModel!!.addMessageListener(this)

            mappingEntryPanel.mappingTableModel!!.removeTableModelListener(statusPanel!!)
            mappingEntryPanel.listSelectionModel.removeListSelectionListener(statusPanel!!)
            
            mappingEntryPanel.mappingTableModel!!.addTableModelListener(statusPanel!!)
            mappingEntryPanel.listSelectionModel.addListSelectionListener(statusPanel!!)
        }
    }

    override fun messagePosted(event: MessageEvent) {
        JOptionPane.showMessageDialog(this, event.message)
    }

    companion object {
        private val logger = Logger.getLogger(STEDWindow::class.java.name)
    }
}
