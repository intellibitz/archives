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
 * $Id:MenuHandler.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/util/MenuHandler.java $
 */

package intellibitz.sted.util;

import intellibitz.sted.actions.ItemListenerAction;
import intellibitz.sted.actions.LAFAction;
import intellibitz.sted.actions.OpenSampleFontMapAction;
import intellibitz.sted.actions.ReOpenFontMapAction;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.ui.STEDWindow;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.Component;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

public class MenuHandler
        extends DefaultHandler
{
    private static final Map<String, Action> actions =
            new HashMap<String, Action>();
    private static final Map<String, String> toolTips =
            new HashMap<String, String>();
    private static final Map<String, AbstractButton> toolButtons =
            new HashMap<String, AbstractButton>();
    private static final Map<String, JMenuItem> menuItems =
            new HashMap<String, JMenuItem>();
    private static final Map<String, JMenu> menus =
            new HashMap<String, JMenu>();
    private static final Map<String, JPopupMenu> popupMenus =
            new HashMap<String, JPopupMenu>();
    private static final Map<String, JMenuBar> menuBars =
            new HashMap<String, JMenuBar>();
    private static final Map<String, JToolBar> toolBars =
            new HashMap<String, JToolBar>();
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPopupMenu popupMenu;
    private static final Stack<JMenu> stack = new Stack<JMenu>();
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.util.MenuHandler");

    private static MenuHandler menuHandler;
    private static UIManager.LookAndFeelInfo[] lookAndFeelInfos;

    static
    {
        try
        {
            synchronized (MenuHandler.class)
            {
                menuHandler = new MenuHandler();
                menuHandler.loadMenu(
                        Resources.getResource(Resources.MENU_CONFIG_NAME));
            }
        }
        catch (ParserConfigurationException e)
        {
            logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e);
        }
        catch (SAXException e)
        {
            logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e);
        }
        catch (IOException e)
        {
            logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e);
        }

    }

    private MenuHandler()
    {
        super();
    }

    public static Map<String, String> getToolTips()
    {
        return toolTips;
    }

    public static Map<String, AbstractButton> getToolButtons()
    {
        return toolButtons;
    }

    public static Map<String, JMenu> getMenus()
    {
        return menus;
    }

    public static Map<String, JPopupMenu> getPopupMenus()
    {
        return popupMenus;
    }

    public static Map<String, JMenuBar> getMenuBars()
    {
        return menuBars;
    }

    public static Map<String, JToolBar> getToolBars()
    {
        return toolBars;
    }

    public static MenuHandler getInstance()
    {
        return menuHandler;
    }

    private void loadMenu(String xml)
            throws SAXException, ParserConfigurationException, IOException
    {
        final SAXParserFactory saxParserFactory =
                SAXParserFactory.newInstance();
        saxParserFactory.setValidating(true);
        final SAXParser saxParser = saxParserFactory.newSAXParser();
        saxParser.parse(ClassLoader.getSystemResourceAsStream(xml), this);
    }

    public JMenuBar getMenuBar(String name)
    {
        return menuBars.get(name);
    }

    public JToolBar getToolBar(String name)
    {
        return toolBars.get(name);
    }

    public Map<String, JMenuItem> getMenuItems()
    {
        return menuItems;
    }

    public Map<String, String> getTooltips()
    {
        return toolTips;
    }

    public Map<String, Action> getActions()
    {
        return actions;
    }

    public Map<String, ImageIcon> getImageIcons()
    {
        return Resources.imageIcons;
    }

    public Action getAction(String name)
    {
        return actions.get(name);
    }

    public AbstractButton getToolButton(String name)
    {
        return toolButtons.get(name);
    }

    public JMenu getMenu(String name)
    {
        return menus.get(name);
    }

    public JMenuItem getMenuItem(String name)
    {
        return menuItems.get(name);
    }

    public void removeMenuItem(String name)
    {
        menuItems.remove(name);
    }

    public void addMenuItem(JMenuItem menuItem)
    {
        if (!menuItems.containsKey(menuItem.getName()))
        {
            menuItems.put(menuItem.getName(), menuItem);
        }
    }

    public JPopupMenu getPopupMenu(String name)
    {
        return popupMenus.get(name);
    }

    public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
    {
        if ("menubar".equals(qName))
        {
            menuBar = new JMenuBar();
            menuBar.setName(attributes.getValue("name"));
            final String toolBarName = attributes.getValue("toolBarName");
            toolBar = toolBars.get(toolBarName);
            if (toolBar == null)
            {
                toolBar = new JToolBar(JToolBar.HORIZONTAL);
                toolBar.setName(toolBarName);
            }
        }
        else if ("menu".equals(qName))
        {
            try
            {
                stack.push(createMenu(attributes));
            }
            catch (Exception e)
            {
                logger.severe("Unable to create Menu Item: " + e.getMessage());
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        }
        else if ("popup_menu".equals(qName))
        {
            popupMenu = createPopupMenu(attributes);
        }
        else if ("menuitem".equals(qName))
        {
            if (popupMenu == null)
            {
                final JMenu menu = stack.peek();
                menu.add(createMenuItem(attributes));
            }
            else
            {
                popupMenu.add(createMenuItem(attributes));
            }
        }
        else if ("menuitemref".equals(qName))
        {
            if (popupMenu == null)
            {
                final JMenu menu = stack.peek();
                menu.add(createMenuItemRef(attributes));
            }
            else
            {
                popupMenu.add(createMenuItemRef(attributes));
            }
        }
        else if ("seperator".equals(qName))
        {
            if (popupMenu == null)
            {
                final JMenu menu = stack.peek();
                menu.addSeparator();
            }
            else
            {
                popupMenu.addSeparator();
            }
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        if ("menubar".equals(qName))
        {
            menuBars.put(menuBar.getName(), menuBar);
            // moved from getToolBar block
            toolBar.setOrientation(JToolBar.HORIZONTAL);
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.add(Box.createVerticalGlue());
            //
            toolBars.put(toolBar.getName(), toolBar);
        }
        else if ("menu".equals(qName))
        {
            final JMenu menu = stack.pop();
            if (stack.isEmpty())
            {
                toolBar.add(Box.createHorizontalStrut(5));
                menuBar.add(menu);
            }
            else
            {
                final JMenu parent = stack.peek();
                parent.add(menu);
            }
            menus.put(menu.getName(), menu);
        }
        else if ("popup_menu".equals(qName))
        {
            popupMenus.put(popupMenu.getName(), popupMenu);
        }
    }


    private JMenu createMenu(Attributes attributes)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException
    {
        final JMenu menu = new JMenu();
        final String name = attributes.getValue("name");
        menu.setName(name);
        menu.setText(name);
        final String mnemonic = attributes.getValue("mnemonic");
        menu.setMnemonic(mnemonic.charAt(0));
        final String actionName = attributes.getValue("action");
        if (null != actionName)
        {
            final Action action =
                    (Action) Class.forName(actionName).newInstance();
            action.putValue(Action.NAME, name);
            action.putValue(Action.MNEMONIC_KEY, (int) mnemonic.charAt(0));
            menu.setAction(action);
            actions.put(name, action);
        }
        menu.setEnabled(Boolean.valueOf(attributes.getValue("actionEnabled")));
        return menu;
    }

    private JPopupMenu createPopupMenu(Attributes attributes)
    {
        final JPopupMenu menu = new JPopupMenu(attributes.getValue("name"));
        menu.setName(attributes.getValue("name"));
        return menu;
    }

    private JMenuItem createMenuItemRef(Attributes attributes)
    {
        final JMenuItem menuItem = getMenuItem(attributes.getValue("name"));
        final JMenuItem cloned = new JMenuItem(menuItem.getAction());
        cloned.setName(menuItem.getName());
        cloned.setText(menuItem.getText());
        cloned.setSelected(menuItem.isSelected());
        cloned.setHorizontalTextPosition(menuItem.getHorizontalTextPosition());
        final ItemListener[] itemListeners = menuItem.getItemListeners();
        if (itemListeners != null)
        {
            for (final ItemListener itemListener : itemListeners)
            {
                cloned.addItemListener(itemListener);
            }
        }
        return cloned;
    }

    private JMenuItem createMenuItem(Attributes attributes)
    {
        JMenuItem menuItem = null;
        try
        {
            final String name = attributes.getValue("name");
            final String type = attributes.getValue("type");
            final String ic = attributes.getValue("icon");
            final String tooltip = attributes.getValue("tooltip");
            final String shortcut = attributes.getValue("mnemonic");
            toolTips.put(name, tooltip);
            final Action action = (Action) Class
                    .forName(attributes.getValue("action")).newInstance();
            action.putValue(Action.NAME, name);
            if (ic != null)
            {
                final Icon icon = Resources.getSystemResourceIcon(ic);
//                imageIcons.put(name, icon);
                action.putValue(Action.SMALL_ICON, icon);
            }
            action.putValue(Action.SHORT_DESCRIPTION, tooltip);
            if (null != shortcut && shortcut.length() > 0)
            {
                action.putValue(Action.MNEMONIC_KEY, (int) shortcut.charAt(0));
            }
            action.putValue(Action.ACCELERATOR_KEY,
                    getAccelerator(attributes.getValue("accelerator")));
            final String cmd = attributes.getValue("actionCommand");
            action.putValue(Action.ACTION_COMMAND_KEY, cmd);
            final String listener = attributes.getValue("listener");
            if (listener != null)
            {
//                action.addPropertyChangeListener((PropertyChangeListener) Class.forName(listener).newInstance());
            }
            final String enabled = attributes.getValue("actionEnabled");
            if (enabled != null)
            {
                action.setEnabled(Boolean.valueOf(enabled));
            }
            menuItem = (JMenuItem) Class.forName(type).newInstance();
            menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
            menuItem.setAction(action);
            menuItem.setSelected(
                    "on".equalsIgnoreCase(attributes.getValue("actionMode")));
            actions.put(name, action);
            menuItems.put(name, menuItem);
            final String button = attributes.getValue("toolButton");
            final String buttonVisible =
                    attributes.getValue("toolButtonVisible");
            final String buttonTextVisible =
                    attributes.getValue("toolButtonTextVisible");
            if ("true".equalsIgnoreCase(buttonVisible))
            {
                final JComponent component =
                        (JComponent) Class.forName(button).newInstance();
                component.setToolTipText(tooltip);
                if (AbstractButton.class.isInstance(component))
                {
                    final AbstractButton abstractButton =
                            (AbstractButton) component;
                    abstractButton.setAction(action);
                    if (ItemListener.class.isInstance(action))
                    {
                        abstractButton.addItemListener((ItemListener) action);
                    }
                    if ("false".equalsIgnoreCase(buttonTextVisible))
                    {
                        abstractButton.setText("");
                    }
                }
                toolBar.add(component);
                toolButtons.put(name, (AbstractButton) component);
            }
            if (ItemListener.class.isInstance(action))
            {
                menuItem.addItemListener((ItemListener) action);
            }
        }
        catch (InstantiationException e)
        {
            logger.severe("Unable to create Menu Item: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        catch (IllegalAccessException e)
        {
            logger.severe("Unable to create Menu Item: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        catch (ClassNotFoundException e)
        {
            logger.severe("Unable to create Menu Item: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        return menuItem;
    }

    private static KeyStroke getAccelerator(String key)
    {
        if (key != null && key.length() > 0)
        {
            return KeyStroke.getKeyStroke(key);
        }
        return null;
    }


    public static void clearReOpenItems(MenuHandler menuHandler)
    {
        final JMenu menu =
                menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND);
        final int sz = menu.getMenuComponentCount();
        int i = sz - 2;
        while (i > 0)
        {
            final Component menuItem = menu.getMenuComponent(0);
            menu.remove(0);
            menuHandler.removeMenuItem(menuItem.getName());
            i--;
        }
        menu.setEnabled(false);
    }

    public static void addReOpenItem(JMenu menu,
            String fileName)
    {
        addReOpenItem(menu, fileName, new ReOpenFontMapAction(), true);
    }

    public static void addSampleFontMapMenuItem(JMenu menu,
            String fileName)
    {
        addReOpenItem(menu, fileName, new OpenSampleFontMapAction(), false);
    }

    public static void addReOpenItem(JMenu menu,
            String fileName, Action action, boolean checkInCache)
    {
        final MenuHandler menuHandler = getInstance();
        if (null == fileName || Resources.EMPTY_STRING.equals(fileName))
        {
            throw new IllegalArgumentException(
                    "Invalid File name: " + fileName);
        }
        JMenuItem menuItem = menuHandler.getMenuItem(fileName);
        // check if the menu item already exists.. if not add new
        // this check is done only if cachecheck is enabled.. opensamplefontmap does not require this
        if (!checkInCache || menuItem == null)
        {
            menuItem = new JMenuItem(fileName);
            action.putValue(Action.NAME, fileName);
            action.putValue(Action.ACTION_COMMAND_KEY,
                    Resources.ACTION_FILE_REOPEN_COMMAND);
            menuItem.setName(fileName);
            menuItem.setAction(action);
            menuHandler.addMenuItem(menuItem);
            // always insert as the first item
            menu.insert(menuItem, 0);
            menu.setEnabled(true);
        }
    }

    public static void disableMenuItem(MenuHandler menuHandler, String fileName)
    {
        disableMenuItem(
                menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND),
                fileName);
    }

    private static void disableMenuItem(JMenu menu, String name)
    {
        int count = menu.getItemCount();
        int i = 0;
        while (count > Resources.DEFAULT_MENU_COUNT)
        {
            final JMenuItem menuItem = menu.getItem(i++);
            menuItem.setEnabled(!name.equals(menuItem.getName()));
            count--;
        }
        menu.setEnabled(menu.getItemCount() > Resources.DEFAULT_MENU_COUNT);
    }

    public static void enableReOpenItems(MenuHandler menuHandler)
    {
        enableReOpenItems(
                menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND));
    }

    public static void enableReOpenItems(JMenu menu)
    {
        int count = menu.getItemCount();
        int i = 0;
        while (count > Resources.DEFAULT_MENU_COUNT)
        {
            final JMenuItem menuItem = menu.getItem(i++);
            menuItem.setEnabled(true);
            count--;
        }
        menu.setEnabled(menu.getItemCount() > Resources.DEFAULT_MENU_COUNT);
    }

    public static void enableItemsInReOpenMenu(MenuHandler menuHandler,
            FontMap fontMap)
    {
        final JMenu menu =
                menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND);
        if (fontMap.isNew())
        {
            enableReOpenItems(menu);
        }
        else
        {
            disableMenuItem(menu, fontMap.getFileName());
        }
    }

    public static String getUserOptions()
    {
        final Map<String, JMenuItem> menuItems =
                getInstance().getMenuItems();
        final Iterator<String> keys = menuItems.keySet().iterator();
        final StringBuffer userOptions = new StringBuffer();
        while (keys.hasNext())
        {
            final String name = keys.next();
            final JMenuItem menuItem = menuItems.get(name);
            final Action action = menuItem.getAction();
            if (ItemListenerAction.class.isInstance(action))
            {
                userOptions.append(name);
                userOptions.append(Resources.SYMBOL_ASTERISK);
                userOptions.append(menuItem.isSelected());
                userOptions.append(Resources.NEWLINE_DELIMITER);
            }
            else if (ReOpenFontMapAction.class.isInstance(action))
            {
                userOptions.append(
                        Resources.ACTION_FILE_REOPEN_COMMAND + name.hashCode());
                userOptions.append(Resources.SYMBOL_ASTERISK);
                userOptions.append(name);
                userOptions.append(Resources.NEWLINE_DELIMITER);
            }
        }
        return userOptions.toString();
    }

    public static void loadLookAndFeelMenu(STEDWindow stedWindow)
    {
        final MenuHandler menuHandler = getInstance();
        lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
        final ButtonGroup buttonGroup = new ButtonGroup();
        final LookAndFeel curLookAndFeel = UIManager.getLookAndFeel();
        for (final UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos)
        {
            final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
            final LAFAction lafAction = new LAFAction();
            lafAction.setSTEDWindow(stedWindow);
            lafAction.putValue(Action.NAME, lookAndFeelInfo.getName());
            lafAction.putValue(Action.ACTION_COMMAND_KEY,
                    lookAndFeelInfo.getClassName());
            menuItem.setName(lookAndFeelInfo.getName());
            menuItem.setAction(lafAction);
            menuHandler.addMenuItem(menuItem);
            if (menuItem.getName().equals(curLookAndFeel.getName()))
            {
                menuItem.setSelected(true);
            }
            buttonGroup.add(menuItem);
            final JMenu menu = menuHandler.getMenu(Resources.ACTION_VIEW_LAF);
            menu.add(menuItem);
        }
    }

    public static boolean isLAF(String name)
    {
        for (final UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos)
        {
            if (lookAndFeelInfo.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
}
