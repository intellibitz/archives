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
 * $Id:MenuHandler.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/util/MenuHandler.kt $
 */

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
import javax.swing.AbstractButton
import javax.swing.Action
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JRadioButtonMenuItem
import javax.swing.JToolBar
import javax.swing.KeyStroke
import javax.swing.LookAndFeel
import javax.swing.UIManager
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import java.awt.Component
import java.awt.event.ItemListener
import java.io.IOException
import java.util.HashMap
import java.util.Iterator
import java.util.Map
import java.util.Stack
import java.util.logging.Logger

class MenuHandler : DefaultHandler() {
    private var menuBar: JMenuBar? = null
    private var toolBar: JToolBar? = null
    private var popupMenu: JPopupMenu? = null
    @Throws(SAXException::class, ParserConfigurationException::class, IOException::class)
    private fun loadMenu(xml: String) {
        val saxParserFactory: SAXParserFactory = SAXParserFactory.newInstance()
        saxParserFactory.setValidating(true)
        val saxParser: SAXParser = saxParserFactory.newSAXParser()
        saxParser.parse(ClassLoader.getSystemResourceAsStream(xml), this)
    }
    fun getMenuBar(name: String): JMenuBar {
        return menuBars.get(name)
    }
    fun getToolBar(name: String): JToolBar {
        return toolBars.get(name)
    }
    fun getMenuItems(): Map<String, JMenuItem> {
        return menuItems
    }
    fun getTooltips(): Map<String, String> {
        return toolTips
    }
    fun getActions(): Map<String, Action> {
        return actions
    }
    fun getImageIcons(): Map<String, ImageIcon> {
        return Resources.imageIcons
    }
    fun getAction(name: String): Action {
        return actions.get(name)
    }
    fun getToolButton(name: String): AbstractButton {
        return toolButtons.get(name)
    }
    fun getMenu(name: String): JMenu {
        return menus.get(name)
    }
    fun getMenuItem(name: String): JMenuItem {
        return menuItems.get(name)
    }
    fun removeMenuItem(name: String) {
        menuItems.remove(name)
    }
    fun addMenuItem(menuItem: JMenuItem) {
        if (!menuItems.containsKey(menuItem.getName())) {
            menuItems.put(menuItem.getName(), menuItem)
        }
    }
    fun getPopupMenu(name: String): JPopupMenu {
        return popupMenus.get(name)
    }
    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if (qName == "menubar") {
            menuBar = JMenuBar()
            menuBar.setName(attributes.getValue("name"))
            val toolBarName: String = attributes.getValue("toolBarName")
            toolBar = toolBars.get(toolBarName)
            if ((toolBar == null)) {
                toolBar = JToolBar(JToolBar.HORIZONTAL)
                toolBar.setName(toolBarName)
            }
        }
        else {
            if (qName == "menu") {
                try {
                    stack.push(createMenu(attributes))
                }
                catch (e: Exception) {
                    logger.severe(("Unable to create Menu Item: " + e.getMessage()))
                    e.printStackTrace()
                }
            }
            else {
                if (qName == "popup_menu") {
                    popupMenu = createPopupMenu(attributes)
                }
                else {
                    if (qName == "menuitem") {
                        if ((popupMenu == null)) {
                            val menu: JMenu = stack.peek()
                            menu.add(createMenuItem(attributes))
                        }
                        else {
                            popupMenu.add(createMenuItem(attributes))
                        }
                    }
                    else {
                        if (qName == "menuitemref") {
                            if ((popupMenu == null)) {
                                val menu: JMenu = stack.peek()
                                menu.add(createMenuItemRef(attributes))
                            }
                            else {
                                popupMenu.add(createMenuItemRef(attributes))
                            }
                        }
                        else {
                            if (qName == "seperator") {
                                if ((popupMenu == null)) {
                                    val menu: JMenu = stack.peek()
                                    menu.addSeparator()
                                }
                                else {
                                    popupMenu.addSeparator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        if (qName == "menubar") {
            menuBars.put(menuBar.getName(), menuBar)
            toolBar.setOrientation(JToolBar.HORIZONTAL)
            toolBar.setFloatable(false)
            toolBar.setRollover(true)
            toolBar.add(Box.createVerticalGlue())
            toolBars.put(toolBar.getName(), toolBar)
        }
        else {
            if (qName == "menu") {
                val menu: JMenu = stack.pop()
                if (stack.isEmpty()) {
                    toolBar.add(Box.createHorizontalStrut(5))
                    menuBar.add(menu)
                }
                else {
                    val parent: JMenu = stack.peek()
                    parent.add(menu)
                }
                menus.put(menu.getName(), menu)
            }
            else {
                if (qName == "popup_menu") {
                    popupMenus.put(popupMenu.getName(), popupMenu)
                }
            }
        }
    }
    @Throws(ClassNotFoundException::class, IllegalAccessException::class, InstantiationException::class)
    private fun createMenu(attributes: Attributes): JMenu {
        val menu: JMenu = JMenu()
        val name: String = attributes.getValue("name")
        menu.setName(name)
        menu.setText(name)
        val mnemonic: String = attributes.getValue("mnemonic")
        menu.setMnemonic(mnemonic.charAt(0))
        val actionName: String = attributes.getValue("action")
        if ((null != actionName)) {
            val action: Action = (Class.forName(actionName) as Action)
            action.putValue(Action.NAME, name)
            action.putValue(Action.MNEMONIC_KEY, (mnemonic.charAt(0) as Int))
            menu.setAction(action)
            actions.put(name, action)
        }
        menu.setEnabled(Boolean.valueOf(attributes.getValue("actionEnabled")))
        return menu
    }
    private fun createPopupMenu(attributes: Attributes): JPopupMenu {
        val menu: JPopupMenu = JPopupMenu(attributes.getValue("name"))
        menu.setName(attributes.getValue("name"))
        return menu
    }
    private fun createMenuItemRef(attributes: Attributes): JMenuItem {
        val menuItem: JMenuItem = getMenuItem(attributes.getValue("name"))
        val cloned: JMenuItem = JMenuItem(menuItem.getAction())
        cloned.setName(menuItem.getName())
        cloned.setText(menuItem.getText())
        cloned.setSelected(menuItem.isSelected())
        cloned.setHorizontalTextPosition(menuItem.getHorizontalTextPosition())
        val itemListeners: Array<ItemListener> = menuItem.getItemListeners()
        if ((itemListeners != null)) {
            for (itemListener in itemListeners) {
                cloned.addItemListener(itemListener)
            }
        }
        return cloned
    }
    private fun createMenuItem(attributes: Attributes): JMenuItem {
        var menuItem: JMenuItem = null
        try {
            val name: String = attributes.getValue("name")
            val type: String = attributes.getValue("type")
            val ic: String = attributes.getValue("icon")
            val tooltip: String = attributes.getValue("tooltip")
            val shortcut: String = attributes.getValue("mnemonic")
            toolTips.put(name, tooltip)
            val action: Action = (Class.forName(attributes.getValue("action")) as Action)
            action.putValue(Action.NAME, name)
            if ((ic != null)) {
                val icon: Icon = Resources.getSystemResourceIcon(ic)
                action.putValue(Action.SMALL_ICON, icon)
            }
            action.putValue(Action.SHORT_DESCRIPTION, tooltip)
            if (((null != shortcut) && (shortcut.length() > 0))) {
                action.putValue(Action.MNEMONIC_KEY, (shortcut.charAt(0) as Int))
            }
            action.putValue(Action.ACCELERATOR_KEY, getAccelerator(attributes.getValue("accelerator")))
            val cmd: String = attributes.getValue("actionCommand")
            action.putValue(Action.ACTION_COMMAND_KEY, cmd)
            val listener: String = attributes.getValue("listener")
            if ((listener != null)) {

            }
            val enabled: String = attributes.getValue("actionEnabled")
            if ((enabled != null)) {
                action.setEnabled(Boolean.valueOf(enabled))
            }
            menuItem = (Class.forName(type) as JMenuItem)
            menuItem.setHorizontalTextPosition(JMenuItem.RIGHT)
            menuItem.setAction(action)
            menuItem.setSelected("on")
            actions.put(name, action)
            menuItems.put(name, menuItem)
            val button: String = attributes.getValue("toolButton")
            val buttonVisible: String = attributes.getValue("toolButtonVisible")
            val buttonTextVisible: String = attributes.getValue("toolButtonTextVisible")
            if (qName == "true") {
                val component: JComponent = (Class.forName(button) as JComponent)
                component.setToolTipText(tooltip)
                if (AbstractButton::class.java) {
                    val abstractButton: AbstractButton = (component as AbstractButton)
                    abstractButton.setAction(action)
                    if (ItemListener::class.java) {
                        abstractButton.addItemListener((action as ItemListener))
                    }
                    if (qName == "false") {
                        abstractButton.setText("")
                    }
                }
                toolBar.add(component)
                toolButtons.put(name, (component as AbstractButton))
            }
            if (ItemListener::class.java) {
                menuItem.addItemListener((action as ItemListener))
            }
        }
        catch (e: InstantiationException) {
            logger.severe(("Unable to create Menu Item: " + e.getMessage()))
            e.printStackTrace()
        }
        catch (e: IllegalAccessException) {
            logger.severe(("Unable to create Menu Item: " + e.getMessage()))
            e.printStackTrace()
        }
        catch (e: ClassNotFoundException) {
            logger.severe(("Unable to create Menu Item: " + e.getMessage()))
            e.printStackTrace()
        }
        return menuItem
    }

    companion object {
        private val actions: MutableMap<String, Action> = HashMap<String, Action>()
        private val toolTips: MutableMap<String, String> = HashMap<String, String>()
        private val toolButtons: MutableMap<String, AbstractButton> = HashMap<String, AbstractButton>()
        private val menuItems: MutableMap<String, JMenuItem> = HashMap<String, JMenuItem>()
        private val menus: MutableMap<String, JMenu> = HashMap<String, JMenu>()
        private val popupMenus: MutableMap<String, JPopupMenu> = HashMap<String, JPopupMenu>()
        private val menuBars: MutableMap<String, JMenuBar> = HashMap<String, JMenuBar>()
        private val toolBars: MutableMap<String, JToolBar> = HashMap<String, JToolBar>()
        private val stack: java.util.Stack<JMenu> = java.util.Stack<JMenu>()
        private val logger: Logger = Logger.getLogger("intellibitz.sted.util.MenuHandler")
        private var menuHandler: MenuHandler? = null
        private var lookAndFeelInfos: Array<UIManager.LookAndFeelInfo>? = null
        @JvmStatic
        fun getToolTips(): Map<String, String> {
            return toolTips
        }
        @JvmStatic
        fun getToolButtons(): Map<String, AbstractButton> {
            return toolButtons
        }
        @JvmStatic
        fun getMenus(): Map<String, JMenu> {
            return menus
        }
        @JvmStatic
        fun getPopupMenus(): Map<String, JPopupMenu> {
            return popupMenus
        }
        @JvmStatic
        fun getMenuBars(): Map<String, JMenuBar> {
            return menuBars
        }
        @JvmStatic
        fun getToolBars(): Map<String, JToolBar> {
            return toolBars
        }
        @JvmStatic
        fun getInstance(): MenuHandler {
            return menuHandler
        }
        private @JvmStatic
        fun getAccelerator(key: String): KeyStroke {
            if (((key != null) && (key.length() > 0))) {
                return KeyStroke.getKeyStroke(key)
            }
            return null
        }
        @JvmStatic
        fun clearReOpenItems(menuHandler: MenuHandler) {
            val menu: JMenu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            val sz: Int = menu.getMenuComponentCount()
            var i: Int = (sz - 2)
            while ((i > 0)) {
                val menuItem: Component = menu.getMenuComponent(0)
                menu.remove(0)
                menuHandler.removeMenuItem(menuItem.getName())
                i--
            }
            menu.setEnabled(false)
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
        fun addReOpenItem(menu: JMenu, fileName: String, action: Action, checkInCache: Boolean) {
            val menuHandler: MenuHandler = getInstance()
            if (((null == fileName) || (Resources.EMPTY_STRING == fileName))) {
                throw IllegalArgumentException(("Invalid File name: " + fileName))
            }
            var menuItem: JMenuItem = menuHandler.getMenuItem(fileName)
            if ((!checkInCache || (menuItem == null))) {
                menuItem = JMenuItem(fileName)
                action.putValue(Action.NAME, fileName)
                action.putValue(Action.ACTION_COMMAND_KEY, Resources.ACTION_FILE_REOPEN_COMMAND)
                menuItem.setName(fileName)
                menuItem.setAction(action)
                menuHandler.addMenuItem(menuItem)
                menu.insert(menuItem, 0)
                menu.setEnabled(true)
            }
        }
        @JvmStatic
        fun disableMenuItem(menuHandler: MenuHandler, fileName: String) {
            disableMenuItem(menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND), fileName)
        }
        private @JvmStatic
        fun disableMenuItem(menu: JMenu, name: String) {
            var count: Int = menu.getItemCount()
            var i: Int = 0
            while ((count > Resources.DEFAULT_MENU_COUNT)) {
                val menuItem: JMenuItem = menu.getItem(i++)
                menuItem.setEnabled((name == menuItem.getName()))
                count--
            }
            menu.setEnabled((menu.getItemCount() > Resources.DEFAULT_MENU_COUNT))
        }
        @JvmStatic
        fun enableReOpenItems(menuHandler: MenuHandler) {
            enableReOpenItems(menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND))
        }
        @JvmStatic
        fun enableReOpenItems(menu: JMenu) {
            var count: Int = menu.getItemCount()
            var i: Int = 0
            while ((count > Resources.DEFAULT_MENU_COUNT)) {
                val menuItem: JMenuItem = menu.getItem(i++)
                menuItem.setEnabled(true)
                count--
            }
            menu.setEnabled((menu.getItemCount() > Resources.DEFAULT_MENU_COUNT))
        }
        @JvmStatic
        fun enableItemsInReOpenMenu(menuHandler: MenuHandler, fontMap: FontMap) {
            val menu: JMenu = menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND)
            if (fontMap.isNew()) {
                enableReOpenItems(menu)
            }
            else {
                disableMenuItem(menu, fontMap.getFileName())
            }
        }
        @JvmStatic
        fun getUserOptions(): String {
            val menuItems: Map<String, JMenuItem> = getInstance()
            val keys: Iterator<String> = menuItems.keySet()
            val userOptions: StringBuffer = StringBuffer()
            while (keys.hasNext()) {
                val name: String = keys.next()
                val menuItem: JMenuItem = menuItems.get(name)
                val action: Action = menuItem.getAction()
                if (ItemListenerAction::class.java) {
                    userOptions.append(name)
                    userOptions.append(Resources.SYMBOL_ASTERISK)
                    userOptions.append(menuItem.isSelected())
                    userOptions.append(Resources.NEWLINE_DELIMITER)
                }
                else {
                    if (ReOpenFontMapAction::class.java) {
                        userOptions.append((Resources.ACTION_FILE_REOPEN_COMMAND + name.hashCode()))
                        userOptions.append(Resources.SYMBOL_ASTERISK)
                        userOptions.append(name)
                        userOptions.append(Resources.NEWLINE_DELIMITER)
                    }
                }
            }
            return userOptions.toString()
        }
        @JvmStatic
        fun loadLookAndFeelMenu(stedWindow: STEDWindow) {
            val menuHandler: MenuHandler = getInstance()
            lookAndFeelInfos = UIManager.getInstalledLookAndFeels()
            val buttonGroup: ButtonGroup = ButtonGroup()
            val curLookAndFeel: LookAndFeel = UIManager.getLookAndFeel()
            for (lookAndFeelInfo in lookAndFeelInfos) {
                val menuItem: JRadioButtonMenuItem = JRadioButtonMenuItem()
                val lafAction: LAFAction = LAFAction()
                lafAction.setSTEDWindow(stedWindow)
                lafAction.putValue(Action.NAME, lookAndFeelInfo.getName())
                lafAction.putValue(Action.ACTION_COMMAND_KEY, lookAndFeelInfo.getClassName())
                menuItem.setName(lookAndFeelInfo.getName())
                menuItem.setAction(lafAction)
                menuHandler.addMenuItem(menuItem)
                if (menuItem.getName()) {
                    menuItem.setSelected(true)
                }
                buttonGroup.add(menuItem)
                val menu: JMenu = menuHandler.getMenu(Resources.ACTION_VIEW_LAF)
                menu.add(menuItem)
            }
        }
        @JvmStatic
        fun isLAF(name: String): Boolean {
            for (lookAndFeelInfo in lookAndFeelInfos) {
                if (lookAndFeelInfo.getName()) {
                    return true
                }
            }
            return false
        }
    }
}
