package intellibitz.sted.util

import intellibitz.sted.actions.ItemListenerAction
import intellibitz.sted.actions.LAFAction
import intellibitz.sted.actions.OpenSampleFontMapAction
import intellibitz.sted.actions.ReOpenFontMapAction
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.ui.STEDWindow
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.awt.Component
import java.awt.event.ItemListener
import java.io.IOException
import java.util.*
import java.util.logging.Logger
import javax.swing.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

class MenuHandler private constructor() : DefaultHandler() {
    private var menuBar: JMenuBar? = null
    private var toolBar: JToolBar? = null
    private var popupMenu: JPopupMenu? = null

    companion object {
        @JvmStatic
        val actions: MutableMap<String, Action> = HashMap()
        
        @JvmStatic
        val toolTips: MutableMap<String, String> = HashMap()
        
        @JvmStatic
        val toolButtons: MutableMap<String, AbstractButton> = HashMap()
        
        @JvmStatic
        val menuItems: MutableMap<String, JMenuItem> = HashMap()
        
        @JvmStatic
        val menus: MutableMap<String, JMenu> = HashMap()
        
        @JvmStatic
        val popupMenus: MutableMap<String, JPopupMenu> = HashMap()
        
        @JvmStatic
        val menuBars: MutableMap<String, JMenuBar> = HashMap()
        
        @JvmStatic
        val toolBars: MutableMap<String, JToolBar> = HashMap()
        
        private val stack = Stack<JMenu>()
        private val logger = Logger.getLogger("intellibitz.sted.util.MenuHandler")

        @JvmStatic
        var instance: MenuHandler? = null
            private set
            
        private var lookAndFeelInfos: Array<UIManager.LookAndFeelInfo>? = null

        init {
            try {
                instance = MenuHandler()
                instance!!.loadMenu(Resources.getResource(Resources.MENU_CONFIG_NAME))
            } catch (e: ParserConfigurationException) {
                logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e)
            } catch (e: SAXException) {
                logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e)
            } catch (e: IOException) {
                logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e)
            }
        }

        @JvmStatic
        fun clearReOpenItems(menuHandler: MenuHandler) {
            val menu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            if (menu != null) {
                val sz = menu.menuComponentCount
                var i = sz - 2
                while (i > 0) {
                    val menuItem = menu.getMenuComponent(0)
                    menu.remove(0)
                    menuHandler.removeMenuItem(menuItem.name)
                    i--
                }
                menu.isEnabled = false
            }
        }

        @JvmStatic
        fun addReOpenItem(menu: JMenu, fileName: String) {
            addReOpenItem(menu, fileName, ReOpenFontMapAction(), true)
        }

        @JvmStatic
        fun addSampleFontMapMenuItem(menu: JMenu, fileName: String) {
            addReOpenItem(menu, fileName, OpenSampleFontMapAction(), false)
        }

        @JvmStatic
        fun addReOpenItem(
            menu: JMenu,
            fileName: String?, action: Action, checkInCache: Boolean
        ) {
            val menuHandler = instance!!
            require(!(fileName == null || Resources.EMPTY_STRING == fileName)) { "Invalid File name: $fileName" }
            var menuItem = menuHandler.getMenuItem(fileName)
            // check if the menu item already exists.. if not add new
            // this check is done only if cachecheck is enabled.. opensamplefontmap does not require this
            if (!checkInCache || menuItem == null) {
                menuItem = JMenuItem(fileName)
                action.putValue(Action.NAME, fileName)
                action.putValue(
                    Action.ACTION_COMMAND_KEY,
                    Resources.ACTION_FILE_REOPEN_COMMAND
                )
                menuItem.name = fileName
                menuItem.action = action
                menuHandler.addMenuItem(menuItem)
                // always insert as the first item
                menu.insert(menuItem, 0)
                menu.isEnabled = true
            }
        }

        @JvmStatic
        fun disableMenuItem(menuHandler: MenuHandler, fileName: String) {
            val menu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            if (menu != null) {
                disableMenuItem(menu, fileName)
            }
        }

        fun disableMenuItem(menu: JMenu, name: String) {
            var count = menu.itemCount
            var i = 0
            while (count > Resources.DEFAULT_MENU_COUNT) {
                val menuItem = menu.getItem(i++)
                menuItem?.isEnabled = name != menuItem?.name
                count--
            }
            menu.isEnabled = menu.itemCount > Resources.DEFAULT_MENU_COUNT
        }

        @JvmStatic
        fun enableReOpenItems(menuHandler: MenuHandler) {
            val menu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            if (menu != null) {
                enableReOpenItems(menu)
            }
        }

        @JvmStatic
        fun enableReOpenItems(menu: JMenu) {
            var count = menu.itemCount
            var i = 0
            while (count > Resources.DEFAULT_MENU_COUNT) {
                val menuItem = menu.getItem(i++)
                menuItem?.isEnabled = true
                count--
            }
            menu.isEnabled = menu.itemCount > Resources.DEFAULT_MENU_COUNT
        }

        @JvmStatic
        fun enableItemsInReOpenMenu(
            menuHandler: MenuHandler,
            fontMap: FontMap
        ) {
            val menu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            if (menu != null) {
                if (fontMap.isNew()) {
                    enableReOpenItems(menu)
                } else {
                    fontMap.getFileName().let { disableMenuItem(menu, it) }
                }
            }
        }

        @JvmStatic
        fun getUserOptions(): String {
            val menuItems = MenuHandler.menuItems
            val keys: Iterator<String> = menuItems.keys.iterator()
            val userOptions = StringBuffer()
            while (keys.hasNext()) {
                val name = keys.next()
                val menuItem = menuItems[name]
                val action = menuItem?.action
                if (action is ItemListenerAction) {
                    userOptions.append(name)
                    userOptions.append(Resources.SYMBOL_ASTERISK)
                    userOptions.append(menuItem.isSelected)
                    userOptions.append(Resources.NEWLINE_DELIMITER)
                } else if (action is ReOpenFontMapAction) {
                    userOptions.append(Resources.ACTION_FILE_REOPEN_COMMAND + name.hashCode())
                    userOptions.append(Resources.SYMBOL_ASTERISK)
                    userOptions.append(name)
                    userOptions.append(Resources.NEWLINE_DELIMITER)
                }
            }
            return userOptions.toString()
        }

        @JvmStatic
        fun loadLookAndFeelMenu(stedWindow: STEDWindow?) {
            val menuHandler = instance!!
            lookAndFeelInfos = UIManager.getInstalledLookAndFeels()
            val buttonGroup = ButtonGroup()
            val curLookAndFeel = UIManager.getLookAndFeel()
            for (lookAndFeelInfo in lookAndFeelInfos!!) {
                val menuItem = JRadioButtonMenuItem()
                val lafAction = LAFAction()
                lafAction.setSTEDWindow(stedWindow!!)
                lafAction.putValue(Action.NAME, lookAndFeelInfo.name)
                lafAction.putValue(Action.ACTION_COMMAND_KEY, lookAndFeelInfo.className)
                menuItem.name = lookAndFeelInfo.name
                menuItem.action = lafAction
                menuHandler.addMenuItem(menuItem)
                if (menuItem.name == curLookAndFeel.name) {
                    menuItem.isSelected = true
                }
                buttonGroup.add(menuItem)
                val menu = menuHandler.getMenu(Resources.ACTION_VIEW_LAF)
                menu?.add(menuItem)
            }
        }

        @JvmStatic
        fun isLAF(name: String): Boolean {
            if (lookAndFeelInfos != null) {
                for (lookAndFeelInfo in lookAndFeelInfos!!) {
                    if (lookAndFeelInfo.name == name) {
                        return true
                    }
                }
            }
            return false
        }
    }

    @Throws(SAXException::class, ParserConfigurationException::class, IOException::class)
    private fun loadMenu(xml: String?) {
        if (xml == null) return
        val saxParserFactory = SAXParserFactory.newInstance()
        saxParserFactory.isValidating = true
        val saxParser = saxParserFactory.newSAXParser()
        saxParser.parse(ClassLoader.getSystemResourceAsStream(xml), this)
    }

    fun getMenuBar(name: String): JMenuBar? {
        return menuBars[name]
    }

    fun getToolBar(name: String): JToolBar? {
        return toolBars[name]
    }

    fun getTooltips(): Map<String, String> {
        return toolTips
    }

    fun getImageIcons(): Map<String, ImageIcon> {
        return Resources.imageIcons
    }

    fun getAction(name: String): Action? {
        return actions[name]
    }

    fun getToolButton(name: String): AbstractButton? {
        return toolButtons[name]
    }

    fun getMenu(name: String): JMenu? {
        return menus[name]
    }

    fun getMenuItem(name: String): JMenuItem? {
        return menuItems[name]
    }

    fun removeMenuItem(name: String) {
        menuItems.remove(name)
    }

    fun addMenuItem(menuItem: JMenuItem) {
        if (!menuItems.containsKey(menuItem.name)) {
            menuItems[menuItem.name] = menuItem
        }
    }

    fun getPopupMenu(name: String): JPopupMenu? {
        return popupMenus[name]
    }

    @Throws(SAXException::class)
    override fun startElement(
        uri: String, localName: String,
        qName: String, attributes: Attributes
    ) {
        if ("menubar" == qName) {
            menuBar = JMenuBar()
            menuBar!!.name = attributes.getValue("name")
            val toolBarName = attributes.getValue("toolBarName")
            toolBar = toolBars[toolBarName]
            if (toolBar == null) {
                toolBar = JToolBar(JToolBar.HORIZONTAL)
                toolBar!!.name = toolBarName
            }
        } else if ("menu" == qName) {
            try {
                stack.push(createMenu(attributes))
            } catch (e: Exception) {
                logger.severe("Unable to create Menu Item: " + e.message)
                e.printStackTrace()
            }
        } else if ("popup_menu" == qName) {
            popupMenu = createPopupMenu(attributes)
        } else if ("menuitem" == qName) {
            if (popupMenu == null) {
                val menu = stack.peek()
                menu.add(createMenuItem(attributes))
            } else {
                popupMenu!!.add(createMenuItem(attributes))
            }
        } else if ("menuitemref" == qName) {
            if (popupMenu == null) {
                val menu = stack.peek()
                menu.add(createMenuItemRef(attributes))
            } else {
                popupMenu!!.add(createMenuItemRef(attributes))
            }
        } else if ("seperator" == qName) {
            if (popupMenu == null) {
                val menu = stack.peek()
                menu.addSeparator()
            } else {
                popupMenu!!.addSeparator()
            }
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        if ("menubar" == qName) {
            menuBars[menuBar!!.name] = menuBar!!
            // moved from getToolBar block
            toolBar!!.orientation = JToolBar.HORIZONTAL
            toolBar!!.isFloatable = false
            toolBar!!.isRollover = true
            toolBar!!.add(Box.createVerticalGlue())
            //
            toolBars[toolBar!!.name] = toolBar!!
        } else if ("menu" == qName) {
            val menu = stack.pop()
            if (stack.isEmpty()) {
                toolBar!!.add(Box.createHorizontalStrut(5))
                menuBar!!.add(menu)
            } else {
                val parent = stack.peek()
                parent.add(menu)
            }
            menus[menu.name] = menu
        } else if ("popup_menu" == qName) {
            popupMenus[popupMenu!!.name] = popupMenu!!
        }
    }

    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InstantiationException::class)
    private fun createMenu(attributes: Attributes): JMenu {
        val menu = JMenu()
        val name = attributes.getValue("name")
        menu.name = name
        menu.text = name
        val mnemonic = attributes.getValue("mnemonic")
        if (mnemonic != null && mnemonic.isNotEmpty()) {
            menu.setMnemonic(mnemonic[0])
        }
        val actionName = attributes.getValue("action")
        if (actionName != null) {
            val action = Class.forName(actionName).newInstance() as Action
            action.putValue(Action.NAME, name)
            if (mnemonic != null && mnemonic.isNotEmpty()) {
                action.putValue(Action.MNEMONIC_KEY, mnemonic[0].code)
            }
            menu.action = action
            actions[name] = action
        }
        menu.isEnabled = attributes.getValue("actionEnabled")?.toBoolean() ?: true
        return menu
    }

    private fun createPopupMenu(attributes: Attributes): JPopupMenu {
        val menu = JPopupMenu(attributes.getValue("name"))
        menu.name = attributes.getValue("name")
        return menu
    }

    private fun createMenuItemRef(attributes: Attributes): JMenuItem {
        val menuItem = getMenuItem(attributes.getValue("name"))!!
        val cloned = JMenuItem(menuItem.action)
        cloned.name = menuItem.name
        cloned.text = menuItem.text
        cloned.isSelected = menuItem.isSelected
        cloned.horizontalTextPosition = menuItem.horizontalTextPosition
        val itemListeners = menuItem.itemListeners
        if (itemListeners != null) {
            for (itemListener in itemListeners) {
                cloned.addItemListener(itemListener)
            }
        }
        return cloned
    }

    private fun createMenuItem(attributes: Attributes): JMenuItem? {
        var menuItem: JMenuItem? = null
        try {
            val name = attributes.getValue("name")
            val type = attributes.getValue("type")
            val ic = attributes.getValue("icon")
            val tooltip = attributes.getValue("tooltip")
            val shortcut = attributes.getValue("mnemonic")
            if (tooltip != null) {
                toolTips[name] = tooltip
            }
            val action = Class.forName(attributes.getValue("action")).newInstance() as Action
            action.putValue(Action.NAME, name)
            if (ic != null) {
                val icon = Resources.getSystemResourceIcon(ic)
                action.putValue(Action.SMALL_ICON, icon)
            }
            action.putValue(Action.SHORT_DESCRIPTION, tooltip)
            if (shortcut != null && shortcut.isNotEmpty()) {
                action.putValue(Action.MNEMONIC_KEY, shortcut[0].code)
            }
            action.putValue(
                Action.ACCELERATOR_KEY,
                getAccelerator(attributes.getValue("accelerator"))
            )
            val cmd = attributes.getValue("actionCommand")
            action.putValue(Action.ACTION_COMMAND_KEY, cmd)
            val enabled = attributes.getValue("actionEnabled")
            if (enabled != null) {
                action.isEnabled = java.lang.Boolean.valueOf(enabled)
            }
            menuItem = Class.forName(type).newInstance() as JMenuItem
            menuItem.horizontalTextPosition = SwingConstants.RIGHT
            menuItem.action = action
            menuItem.isSelected = "on".equals(attributes.getValue("actionMode"), ignoreCase = true)
            actions[name] = action
            menuItems[name] = menuItem
            val button = attributes.getValue("toolButton")
            val buttonVisible = attributes.getValue("toolButtonVisible")
            val buttonTextVisible = attributes.getValue("toolButtonTextVisible")
            if ("true".equals(buttonVisible, ignoreCase = true)) {
                val component = Class.forName(button).newInstance() as JComponent
                component.toolTipText = tooltip
                if (component is AbstractButton) {
                    val abstractButton = component
                    abstractButton.action = action
                    if (action is ItemListener) {
                        abstractButton.addItemListener(action as ItemListener)
                    }
                    if ("false".equals(buttonTextVisible, ignoreCase = true)) {
                        abstractButton.text = ""
                    }
                }
                toolBar!!.add(component)
                toolButtons[name] = component as AbstractButton
            }
            if (action is ItemListener) {
                menuItem.addItemListener(action as ItemListener)
            }
        } catch (e: InstantiationException) {
            logger.severe("Unable to create Menu Item: " + e.message)
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            logger.severe("Unable to create Menu Item: " + e.message)
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            logger.severe("Unable to create Menu Item: " + e.message)
            e.printStackTrace()
        }
        return menuItem
    }

    private fun getAccelerator(key: String?): KeyStroke? {
        if (key != null && key.isNotEmpty()) {
            return KeyStroke.getKeyStroke(key)
        }
        return null
    }
}
