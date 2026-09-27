/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * <p>
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 * <p>
 * Visit http://www.gnu.org/ for GPL License terms.
 * <p>
 * $Id: STEDWindow.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/STEDWindow.java $
 * <p>
 * The sted UI package
 * Contains the swing windows and widgets
 */

/**
 * $Id: STEDWindow.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/STEDWindow.java $
 */

/**
 * The sted UI package
 * Contains the swing windows and widgets
 */
package intellibitz.sted.ui;

import intellibitz.sted.actions.ExitAction;
import intellibitz.sted.actions.ItemListenerAction;
import intellibitz.sted.actions.STEDWindowAction;
import intellibitz.sted.event.*;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.io.FileReaderThread;
import intellibitz.sted.util.FileHelper;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * STEDWindow
 */

public class STEDWindow
        extends JFrame
        implements IThreadListener,
        ChangeListener,
        IMessageListener,
        IStatusEventSource {
    // desktop to show fontmap
    private TabDesktop tabDesktop;
    private StatusPanel statusPanel;
    private static final Logger logger =
            Logger.getLogger(STEDWindow.class.getName());
    private IStatusListener statusListener;
    private StatusEvent statusEvent;

    public STEDWindow() {
        super();
    }

    public void init() {
        setTitle(Resources.getSTEDTitle());
        setDefaultLookAndFeelDecorated(true);
        setState(JFrame.MAXIMIZED_BOTH);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Resources.getSTEDImage());

        statusEvent = new StatusEvent(this);

        final JMenuBar menuBar =
                MenuHandler.getInstance().getMenuBar(Resources.MENUBAR_STED);
        MenuHandler.loadLookAndFeelMenu(this);

        // load the menubar for the application
        setJMenuBar(menuBar);
        fireStatusPosted("20");

        final Container container = getContentPane();
        final GridBagLayout gridBagLayout = new GridBagLayout();
        container.setLayout(gridBagLayout);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        fireStatusPosted("30");

        final JToolBar toolBar =
                MenuHandler.getInstance().getToolBar(Resources.MENUBAR_STED);
        gridBagLayout.setConstraints(toolBar, gridBagConstraints);

        // adds the toolbar for the app
        container.add(toolBar);
        fireStatusPosted("40");

        tabDesktop = new TabDesktop();
        tabDesktop.init();
        fireStatusPosted("50");
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        // adds the desktop directly
        gridBagLayout.setConstraints(tabDesktop, gridBagConstraints);

        container.add(tabDesktop);
        fireStatusPosted("60");

        statusPanel = new StatusPanel(this);
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagLayout.setConstraints(statusPanel, gridBagConstraints);

        // adds the status bar
        container.add(statusPanel);
        fireStatusPosted("70");
        setUserOptions();
        addMouseListener(AboutSTED.getInstance());

        ExitAction exitAction = new ExitAction();
        exitAction.setSTEDWindow(STEDWindow.this);
        addWindowListener(exitAction);

        pack();
        logger.finest("successfully intialized STEDWindow");
        fireStatusPosted("80");
    }

    public void load() {
        // status panel added as status listener to recieve status messages
        tabDesktop.addStatusListener(statusPanel);

        Map<String, Action> actions = MenuHandler.getInstance().getActions();
        for (Action action : actions.values()) {
            if (STEDWindowAction.class.isInstance(action)) {
                ((STEDWindowAction) action).addStatusListener(statusPanel);
                ((STEDWindowAction) action).addMessageListener(this);
            }
        }

        tabDesktop.load();
        tabDesktop.addChangeListener(this);

        setState(JFrame.MAXIMIZED_HORIZ);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void fireStatusPosted(String message) {
        statusEvent.setStatus(message);
        statusListener.statusPosted(statusEvent);
    }

    public void fireStatusPosted() {
        statusListener.statusPosted(statusEvent);
    }

    public void addStatusListener(IStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void setVisible() {
        super.setVisible(true);
        statusPanel.runMemoryBar();
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public TabDesktop getDesktop() {
        return tabDesktop;
    }

    private void setUserOptions() {
        final Map<String, JMenuItem> menuItems =
                MenuHandler.getInstance().getMenuItems();
        for (String key : menuItems.keySet()) {
            final JMenuItem menuItem = menuItems.get(key);
            final Action action = menuItem.getAction();
            if (ItemListenerAction.class.isInstance(action)) {
                final String val = Resources.getSetting(key);
                if (val != null) {
                    final boolean curr = Boolean.valueOf(val);
                    // first check for state for disabled menu items
                    if (!action.isEnabled()) {
                        menuItem.setSelected(curr);
                    } else if (curr && !menuItem.isSelected()) {
                        // if user option is set, and the menu item is already not selected
                        menuItem.doClick();
                    } else if (menuItem.isSelected() && !curr) {
                        menuItem.doClick();
                    }
                }
            }
        }
        final ArrayList<String> reopenItems = Resources
                .getSettingBeginsWith(Resources.ACTION_FILE_REOPEN_COMMAND);
        if (!reopenItems.isEmpty()) {
            final JMenu menu =
                    MenuHandler.getInstance()
                            .getMenu(Resources.ACTION_FILE_REOPEN_COMMAND);
            for (String reopenItem : reopenItems) {
                MenuHandler.addReOpenItem(menu, reopenItem);
            }
            menu.setEnabled(menu.getItemCount() > Resources.DEFAULT_MENU_COUNT);
        }

        // set the sample fontmap action
        String[] sampleFontMapPaths = FileHelper
                .getSampleFontMapPaths(Resources.getResourceDirPath());
        if (sampleFontMapPaths.length > 0) {
            final JMenu menu =
                    MenuHandler.getInstance()
                            .getMenu(Resources.MENU_SAMPLES_NAME);
            for (String reopenItem : sampleFontMapPaths) {
                MenuHandler.addSampleFontMapMenuItem(menu, reopenItem);
            }
        }
    }

    public void threadRunStarted(ThreadEvent e) {
        JProgressBar progressBar = statusPanel.getProgressBar();
        progressBar.setMinimum(0);
        progressBar.setIndeterminate(true);
    }

    public void threadRunning(ThreadEvent e) {
        //TODO: set the progress bar maximum only once, typically when the thread starts
        JProgressBar progressBar = statusPanel.getProgressBar();
        progressBar.setMaximum(e.getEventSource().getProgressMaximum());
        progressBar.setValue(e.getEventSource().getProgress());
    }

    public void threadRunFailed(ThreadEvent e) {
        JOptionPane.showMessageDialog(this, e.getEventSource().getMessage());
        JProgressBar progressBar = statusPanel.getProgressBar();
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
    }

    public void threadRunFinished(ThreadEvent e) {
        JProgressBar progressBar = statusPanel.getProgressBar();
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
        FileReaderThread source = (FileReaderThread) e.getEventSource();
        statusPanel.setStatus("Read File: " + source.getFile());
    }

    /**
     * @param e ChangeEvent published by TabDesktop
     */
    public void stateChanged(ChangeEvent e) {
        TabDesktop desktop = (TabDesktop) e.getSource();
        int index = desktop.getSelectedIndex();
        if (index > -1) {
            DesktopFrame dframe =
                    (DesktopFrame) desktop.getComponentAt(
                            index);
            dframe.getInputFileViewer().addThreadListener(this);
            dframe.getOutputFileViewer().addThreadListener(this);

            // update the lock icon
            DesktopModel desktopModel =
                    dframe.getModel();
            final FontMap fontMap = desktopModel.getFontMap();

            fontMap.removeFontMapChangeListener(statusPanel);
            fontMap.addFontMapChangeListener(statusPanel);

            statusPanel.setLockFlag(!fontMap.isFileWritable());
            // update the clean/dirty flag
            statusPanel.setNeatness(fontMap);
            dframe.getMapperPanel().getMappingEntryPanel().getEntryAction()
                    .addStatusListener(statusPanel);

            dframe.getMapperPanel().getMappingEntryPanel().getEntryAction()
                    .addMessageListener(this);
            dframe.getMapperPanel().getMappingEntryPanel()
                    .getMappingTableModel().addMessageListener(this);

            // remove and add.. so getting added only once
            dframe.getMapperPanel().getMappingEntryPanel().
                    getMappingTableModel()
                    .removeTableModelListener(statusPanel);
            dframe.getMapperPanel().getMappingEntryPanel().
                    getListSelectionModel()
                    .removeListSelectionListener(statusPanel);
            dframe.getMapperPanel().getMappingEntryPanel().
                    getMappingTableModel().addTableModelListener(statusPanel);
            dframe.getMapperPanel().getMappingEntryPanel().
                    getListSelectionModel()
                    .addListSelectionListener(statusPanel);
        }
    }

    public void messagePosted(MessageEvent event) {
        JOptionPane.showMessageDialog(this, event.getMessage());
    }
}


