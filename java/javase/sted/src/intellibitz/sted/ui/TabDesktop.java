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
 * $Id: TabDesktop.java 59 2007-05-19 08:11:31Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/TabDesktop.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.actions.RedoAction;
import intellibitz.sted.actions.UndoAction;
import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.event.FontMapReadEvent;
import intellibitz.sted.event.IMessageEventSource;
import intellibitz.sted.event.IMessageListener;
import intellibitz.sted.event.IStatusEventSource;
import intellibitz.sted.event.IStatusListener;
import intellibitz.sted.event.IThreadListener;
import intellibitz.sted.event.MessageEvent;
import intellibitz.sted.event.StatusEvent;
import intellibitz.sted.event.ThreadEvent;
import intellibitz.sted.event.TransliterateEvent;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.io.FileFilterHelper;
import intellibitz.sted.io.FontMapReader;
import intellibitz.sted.launch.STEDGUI;
import intellibitz.sted.util.FileHelper;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;
import intellibitz.sted.widgets.ButtonTabComponent;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.xml.transform.TransformerException;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;


/**
 * Desktop managing FontMap Internal Frame's
 */
public class TabDesktop
        extends JTabbedPane
        implements InternalFrameListener,
        IThreadListener,
        FontMapChangeListener,
        ChangeListener,
        ActionListener,
        IStatusEventSource,
        IMessageEventSource
{


    /**
     * FrameNumberIndex <br> Generates Unique Id for New fontmap frames
     */
    private static class FrameNumberIndex
    {
        private Set<Integer> indices = new TreeSet<Integer>();

        public FrameNumberIndex()
        {
            super();
        }

        /**
         * if the index is new, add it if the index is existing, then find free
         * index and add it
         *
         * @param indx
         * @return
         */
        public int addNewIndex(int indx)
        {
            int sz = indices.size();
            if (!containsIndex(indx) && indx >= sz)
            {
                indices.add(indx);
                return indx;
            }
            else
            {
                for (int i = 1; i <= sz; i++)
                {
                    if (!containsIndex(i))
                    {
                        indices.add(i);
                        return i;
                    }
                }
            }
            return indx;
        }

        public boolean removeIndex(int indx)
        {
            return indices.remove(indx);
        }

        public boolean containsIndex(int indx)
        {
            for (Integer indice : indices)
            {
                if (indice == indx)
                {
                    return true;
                }
            }
            return false;
        }
    }


    private JDesktopPane desktopPane;
    private static final Logger logger =
            Logger.getLogger(TabDesktop.class.getName());
    private FrameNumberIndex frameNumberIndex = new FrameNumberIndex();
    // cache to hold fonmap internal frames used by this desktop
    private Map<String, DesktopFrame> frameCache =
            new HashMap<String, DesktopFrame>();
    // cache to hold new/open fontmaps
    //    private Map<String, FontMap> fontMapCache = new HashMap<String, FontMap>();
    // Tabbed Desktop
    //    private JTabbedPane desktopFrames;
    //    private DesktopFrame fontMapperDesktopFrame;
    // clipboard for fontmap edits
    private Map<String, Collection> clipboard =
            new HashMap<String, Collection>();
    private IStatusListener statusListener;
    private StatusEvent statusEvent;
    private IMessageListener messageListener;
    private MessageEvent messageEvent;


    public TabDesktop()
    {
        super();
    }

    public void init()
    {
        desktopPane = new JDesktopPane();
        // we need to listen to our own events here
        // to update the desktop
        addChangeListener(this);
        statusEvent = new StatusEvent(this);
        messageEvent = new MessageEvent(this);
        setVisible(true);
    }

    public void load()
    {
    }

    public void fireStatusPosted(String message)
    {
        statusEvent.setStatus(message);
        statusListener.statusPosted(statusEvent);
    }

    public void fireStatusPosted()
    {
        statusListener.statusPosted(statusEvent);
    }

    public void addStatusListener(IStatusListener statusListener)
    {
        this.statusListener = statusListener;
    }

    public void addTab(DesktopFrame desktopFrame)
    {
        addTab(desktopFrame.getTitle(), desktopFrame);
    }

    public void addTab(String title,
            DesktopFrame desktopFrame)
    {
        desktopFrame.setTitle(title);

        desktopFrame.removeInternalFrameListener(this);
        desktopFrame.addInternalFrameListener(this);

        desktopFrame.getModel().removeFontMapChangeListener(this);
        desktopFrame.getModel().addFontMapChangeListener(this);

        super.addTab(title, Resources.getSTEDIcon(),
                desktopFrame,
                Resources.getResource(Resources.TIP_TAB_FONTMAP));
        initTabComponent(getTabCount() - 1, title);
        setEnabledAt(getTabCount() - 1, true);
        setSelectedIndex(getTabCount() - 1);
//        setSelectedComponent(desktopFrame);
//        desktopPane.setSelectedFrame(desktopFrame);
        try
        {
            desktopFrame.setSelected(true);
        }
        catch (PropertyVetoException e)
        {
            e.printStackTrace();
        }
//        stedWindow.selectTab();
//        stedWindow.setState(JFrame.MAXIMIZED_BOTH);
//        stedWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        this.updateUI();
    }

    private void initTabComponent(int i, String title)
    {
        ButtonTabComponent buttonTabComponent =
                new ButtonTabComponent("TabComponent " + i, this);
        buttonTabComponent.getTabTitle().setIcon(Resources.getCleanIcon());
        buttonTabComponent.getTabTitle().setText(title);
        buttonTabComponent.addActionListener(this);
        setTabComponentAt(i,
                buttonTabComponent);
    }

    public int closeFontMap()
    {
        return closeFontMap(getFontMapperDesktopFrame());
    }

    public int closeFontMap(DesktopFrame desktopFrame)
    {
        int i = saveDirty(desktopFrame);
        if (JOptionPane.CANCEL_OPTION != i)
        {
            int index = getSelectedIndex();
            // todo: hacking because the first frame not getting selected by default
            if (index == -1)
            {
                index = getTabCount() - 1;
            }
            removeTabFrameAt(index);
            desktopFrame.close();
        }
        // todo: this must be in activated
        // hacking it for now since the first frame we load by default is not
        // getting activated
        enableCloseAction();
        return i;
    }

    public int closeFontMap(DesktopFrame desktopFrame, int i)
    {
        int result = saveDirty(desktopFrame);
        if (JOptionPane.CANCEL_OPTION != result)
        {
            removeTabFrameAt(i);
            desktopFrame.close();
        }
        // todo: this must be in activated
        // hacking it for now since the first frame we load by default is not
        // getting activated
        enableCloseAction();
        return i;
    }

    private boolean removeTabFrameAt(int index)
    {
        if (index > -1)
        {
            DesktopFrame desktopFrame =
                    (DesktopFrame) getComponentAt(index);
            String title = desktopFrame.getTitle();
            if (title.startsWith(Resources.ACTION_FILE_NEW_COMMAND))
            {
                int indx = getNewIndexNumber(title);
                // removes the tab index as generated by the newframeindex
                frameNumberIndex.removeIndex(indx);
            }
            // remove the tab with the tab index supplied
            removeTabAt(index);
            return true;
        }
        return false;
    }

    private int getNewIndexNumber(String title)
    {
        return Integer.valueOf(title.substring(
                Resources.ACTION_FILE_NEW_COMMAND.length()).trim());
    }

    private String createNewFrameTitle(int num)
    {
        return Resources.ACTION_FILE_NEW_COMMAND + " " +
                frameNumberIndex.addNewIndex(num);
    }

    public void stateChanged(FontMapChangeEvent e)
    {

        // select a new tab, if the fontmap is new
        DesktopFrame desktopFrame =
                getSelectedFrame();
        String title = desktopFrame.getTitle();
        if (null != title)
        {
            int indx = indexOfTab(title);
            if (-1 == indx)
            {
                addTab(desktopFrame);
            }
            else
            {
                setEnabledAt(indx, true);
            }
        }
        // if title null, new frame
        else
        {
            addTab(desktopFrame);
        }
        // existing fontmaps, with existing tabs
        ButtonTabComponent buttonTabComponent =
                (ButtonTabComponent) getTabComponentAt(getSelectedIndex());
        buttonTabComponent.getTabTitle().setIcon(Resources.getDirtyIcon());

        FontMap fontMap = desktopFrame.getModel().getFontMap();
        if (fontMap.getFontMapFile() != null)
        {
            buttonTabComponent.getTabTitle()
                    .setText(fontMap.getFontMapFile().getName());
        }
        if (fontMap.isDirty())
        {
            buttonTabComponent.getTabTitle().setIcon(Resources.getDirtyIcon());
        }
        else
        {
            buttonTabComponent.getTabTitle().setIcon(Resources.getCleanIcon());
        }
        fireStatusPosted(
                buttonTabComponent.getTabTitle().getText() + " Active");
//        buttonTabComponent.updateUI();
        this.updateUI();
    }

    /**
     * We are listening to our own events, so we can update the dependent
     * desktop. This is required since the tabbed pane acts as the proxy for the
     * contained desktop pane.
     *
     * @param e The change event of this tabbed pane
     */
    public void stateChanged(ChangeEvent e)
    {
        TabDesktop me = (TabDesktop) e.getSource();
        int i = me.getSelectedIndex();
        if (i != -1)
        {
            DesktopFrame myframe =
                    (DesktopFrame) getComponentAt(i);
            desktopPane.setSelectedFrame(myframe);
            i = me.getSelectedIndex();
            ButtonTabComponent buttonTab =
                    (ButtonTabComponent) getTabComponentAt(i);
            String title = myframe.getTitle();
            if (buttonTab != null)
            {
                title = buttonTab.getTabTitle().getText();
            }
            fireStatusPosted(title + " Active");
        }
        enableCloseAction();
    }

    private void enableCloseAction(boolean flag)
    {
        MenuHandler.getInstance()
                .getMenuItem(Resources.ACTION_FILE_CLOSE_COMMAND)
                .setEnabled(flag);
    }

    private void enableCloseAction()
    {
        if (getTabCount() > 0)
        {
            enableCloseAction(true);
        }
        else
        {
            enableCloseAction(false);
        }
    }

    /**
     * listens to tab button, so when tab button is used for closing sync it
     * with desktop
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e)
    {
        ButtonTabComponent buttonTabComponent =
                (ButtonTabComponent) e.getSource();

        int i = indexOfTabComponent(buttonTabComponent);
        if (i != -1)
        {
            DesktopFrame desktopFrame =
                    (DesktopFrame) getComponentAt(i);
//            closeFrameTab(desktopFrame, i-1);
            closeFontMap(desktopFrame, i);
        }
    }

/*
    private void closeFrameTab (DesktopFrame fontMapperDesktopFrame, int i)
    {
        if (JOptionPane.CANCEL_OPTION !=
                closeFontMap(fontMapperDesktopFrame))
        // cancel button has been pressed during save operation
        // safely return without performing close
        {
            removeTabFrameAt(i);
        }
    }
*/


    private DesktopFrame getSelectedFrame()
    {
        DesktopFrame desktopFrame =
                (DesktopFrame) desktopPane.getSelectedFrame();
        //hack!!
        //todo: the frame must be selected, so we dont do this
//        desktopFrame = (DesktopFrame) getComponentAt(getTabCount()-1);
        return desktopFrame;
    }

    /**
     * sets FontMap and invokes #fireFontMapChangeEvent ()
     *
     * @param desktopFrame
     */
    public void addListenersToDesktopFrame(
            DesktopFrame desktopFrame)
    {
        MapperPanel mapperPanel = desktopFrame.getMapperPanel();
        DesktopModel desktopModel = desktopFrame.getModel();
        FontMap fontMap = desktopModel.getFontMap();
        fontMap.removeFontMapChangeListener(
                mapperPanel);
        fontMap.addFontMapChangeListener(
                mapperPanel);

        fontMap.removeFontMapChangeListener(
                mapperPanel.getMappingEntryPanel());
        fontMap.addFontMapChangeListener(
                mapperPanel.getMappingEntryPanel());

        fontMap.removeFontMapChangeListener(desktopFrame);
        fontMap.addFontMapChangeListener(desktopFrame);

        final MenuHandler menuHandler = MenuHandler.getInstance();
        final Map actions = menuHandler.getActions();
        final FontMapChangeListener newAction = (FontMapChangeListener)
                menuHandler.getAction(Resources.ACTION_FILE_NEW_COMMAND);
        fontMap.removeFontMapChangeListener(newAction);
        fontMap.addFontMapChangeListener(newAction);

        final FontMapChangeListener reload = (FontMapChangeListener) actions
                .get(Resources.ACTION_FILE_RELOAD);
        fontMap.removeFontMapChangeListener(reload);
        fontMap.addFontMapChangeListener(reload);

        final FontMapChangeListener save = (FontMapChangeListener) actions
                .get(Resources.ACTION_FILE_SAVE_COMMAND);
        fontMap.removeFontMapChangeListener(save);
        fontMap.addFontMapChangeListener(save);

        final FontMapChangeListener paste = (FontMapChangeListener) actions
                .get(Resources.ACTION_PASTE_COMMAND);
        fontMap.removeFontMapChangeListener(paste);
        fontMap.addFontMapChangeListener(paste);

        final UndoAction undo = (UndoAction) actions
                .get(Resources.ACTION_UNDO_COMMAND);
        fontMap.removeUndoListener(undo);
        undo.setEnabled(false);
        fontMap.addUndoListener(undo);

        this.removeChangeListener(undo);
        this.addChangeListener(undo);

        final RedoAction redo = (RedoAction) actions
                .get(Resources.ACTION_REDO_COMMAND);
        fontMap.removeRedoListener(redo);
        redo.setEnabled(false);
        fontMap.addRedoListener(redo);

        this.removeChangeListener(redo);
        this.addChangeListener(redo);

        final FontKeypad1 keypad1 = mapperPanel.getFontKeypad1();
        fontMap.removeFontListChangeListener(keypad1);
        fontMap.addFontListChangeListener(keypad1);
        desktopModel.addFontMapChangeListener(keypad1);

        final FontKeypad2 keypad2 = mapperPanel.getFontKeypad2();
        fontMap.removeFontListChangeListener(keypad2);
        fontMap.addFontListChangeListener(keypad2);
        desktopModel.addFontMapChangeListener(keypad2);


        desktopFrame.addInternalFrameListener(
                (InternalFrameListener) menuHandler.getActions()
                        .get(Resources.ACTION_FILE_NEW_COMMAND));
        desktopFrame.addInternalFrameListener(
                (InternalFrameListener) menuHandler.getActions
                        ().get(Resources.ACTION_FILE_RELOAD_COMMAND));
        desktopFrame.addInternalFrameListener(
                (InternalFrameListener) menuHandler.getActions()
                        .get(Resources.ACTION_FILE_REOPEN_COMMAND));
        desktopFrame.addInternalFrameListener(
                (InternalFrameListener) menuHandler.getActions()
                        .get(Resources.ACTION_FILE_SAVEAS_COMMAND));
        desktopFrame.addInternalFrameListener(
                (InternalFrameListener) menuHandler.getActions().get(
                        Resources.ACTION_FILE_CLOSE_COMMAND));

        desktopModel.removeFontMapChangeListener(newAction);
        desktopModel.addFontMapChangeListener(newAction);
        final FontMapChangeListener reopen =
                (FontMapChangeListener) menuHandler.getActions
                        ().get(Resources.ACTION_FILE_REOPEN_COMMAND);
        desktopModel.removeFontMapChangeListener(reopen);
        desktopModel.addFontMapChangeListener(reopen);
        desktopFrame
                .addInternalFrameListener((InternalFrameListener) reopen);


    }

    public DesktopModel createDesktopModel(
            DesktopFrame desktopFrame, FontMap fontMap)
    {
        DesktopModel desktopModel = new DesktopModel();
        desktopModel.setFontMap(fontMap);
        desktopFrame.setModel(desktopModel);

        addListenersToDesktopFrame(desktopFrame);

        desktopFrame.load();
        return desktopModel;
    }

    public void loadFontMap(File file)
    {
        loadFontMap(getSelectedFrame(), file);
    }

    public void loadFontMap(DesktopFrame desktopFrame,
            File file)
    {
        DesktopModel desktopModel = createDesktopModel(
                desktopFrame, new FontMap(file));
        desktopModel.addFontMapChangeListener(this);
        readFontMap(desktopModel);
        fireStatusPosted("FontMap loaded");
    }


    /**
     * reads the FontMap from an external File
     *
     * @param desktopModel
     */
    public void readFontMap(DesktopModel desktopModel)
    {
        //TODO: move this somewhere applicable
//        clear();
//        final String path1 = fontMap.getFont1Path();
//        final String path2 = fontMap.getFont2Path();
//        fontMap.clear();

//        fontMap.clearAll();

//        fontMap.setFontMapFile(file);
//        fontMap.setFont1Path(path1);
//        fontMap.setFont2Path(path2);
        FontMap fontMap = desktopModel.getFontMap();
        try
        {
            final FontMapReader fontMapReader = new FontMapReader(fontMap);
            fontMapReader.addThreadListener(this);
            SwingUtilities.invokeLater(fontMapReader);
        }
        catch (IllegalArgumentException e)
        {
            fireMessagePosted(
                    "Cannot Read FontMap.. Failed: " + e.getMessage());
            logger.severe("Cannot Read FontMap - Illegal Argument " +
                    fontMap.getFontMapFile().getAbsolutePath());
            fireStatusPosted("Cannot Read FontMap - Illegal Argument " +
                    fontMap.getFontMapFile().getAbsolutePath());
        }
    }

    /**
     *
     */
    public void openFontMap()
    {
        final File selectedFile = FileHelper
                .openFile("Please select FontMap location:", Resources.XML,
                        "STED FontMap files", this);
        if (selectedFile != null)
        {
            openFontMap(selectedFile);
        }
    }


    public void newFontMap()
    {
        DesktopFrame desktopFrame =
                loadNewFontMap();
        int num = getTabCount() + 1;
        addTab(createNewFrameTitle(num),
                desktopFrame);
        fireStatusPosted("New FontMap");
    }

    /**
     * creates a new FontMap and loads into the Window if the Window already
     * contains a new FontMap, clears and loads them otherwise, looks up the
     * cache for a new FontMap and load them if not in cache, creates a new
     * FontMap, stores in cache and loads new FontMap
     *
     * @return DesktopFrame the newly added fontmap component
     */
    public DesktopFrame loadNewFontMap()
    {
        DesktopFrame desktopFrame =
                createFontMapperDesktopFrame();
        FontMap fontMap = new FontMap();
        DesktopModel desktopModel =
                createDesktopModel(desktopFrame, fontMap);
//        fontMapCache.put(Resources.ACTION_FILE_NEW_COMMAND, fontMap);
        desktopModel.fireFontMapChangedEvent();
        return desktopFrame;
    }


    public void reopenFontMap(String fileName)
    {
        openFontMap(new File(fileName));
/*
        FontMap fontMap = getFontMap();
        // add it to the reopen menu.. one last check
        // TODO: remove the following.. typically reopen items must be added as part of events
        // only fontmaps with a valid filename can be re-opened
        if (!fontMap.isNew())
        {
            addItemToReOpenMenu(fontMap.getFileName());
        }
        MenuHelper.disableMenuItem(MenuHandler.getInstance(), fileName);
        fireStatusPosted("FontMap Re-Opened");
*/
/*
        if (fontMap == null) {
            fontMap = new FontMap();
            addListenersToDesktopFrame(fontMap);
        } else {
            //TODO: must discard the old fontmap here
//                    fontMap.clearAll();
        }
        // add it to the reopen menu.. one last check
        // TODO: remove the following.. typically reopen items must be added as part of events
        // only fontmaps with a valid filename can be re-opened
        if (!fontMap.isNew()) {
            addItemToReOpenMenu(fontMap.getFileName());
        }
        // clear the old stuff, if any
        fontMap.clear();
//        fontMap.clearAll();
        fontMap.setFontMapFile(new File(fileName));
        MenuHelper.disableMenuItem(stedWindow.getInstance(), fileName);
        stedWindow.getDesktop().readFontMap();
*/
    }

    public void reloadFontMap()
    {
//        readFontMap(getSelectedFrame().getModel());
        DesktopFrame selectedFrame = getSelectedFrame();
        File selectedFile =
                selectedFrame.getModel().getFontMap().getFontMapFile();
//        closeFontMap(selectedFrame);
        removeTabFrameAt(getSelectedIndex());
        frameCache.remove(selectedFrame.getModel().getFontMap().getFileName());
//        fontMapCache
//                .remove(selectedFrame.getModel().getFontMap().getFileName());
        openFontMap(selectedFile);
    }

    public void openFontMap(File selectedFile)
    {
        try
        {
            DesktopFrame desktopFrame =
                    frameCache.get(selectedFile.getAbsolutePath());
            if (desktopFrame == null)
            {
                desktopFrame = createFontMapperDesktopFrame();
                //todo: repeat the logic of the frame creation, init and load
                loadFontMap(desktopFrame, selectedFile);
                DesktopModel desktopModel =
                        desktopFrame.getModel();
                FontMap fontMap = desktopModel.getFontMap();
//                desktopModel.setFontMap(fontMap);
//                fontMapCache.put(fontMap.getFileName(), fontMap);
                frameCache.put(fontMap.getFileName(), desktopFrame);
            }
            // add it to the tabs
            add(desktopFrame);
            desktopPane.setSelectedFrame(desktopFrame);
            desktopFrame.getModel().fireFontMapChangedEvent();
//            showDesktopFrame();
/*
                // try the cache first
                fontMap = (FontMap) fontMapCache.get(selectedFile.getAbsolutePath());
                // if not found.. create a new one
                if (fontMap == null) {
                    readFontMap(selectedFile);
                    fontMapCache.put(fontMap.getFileName(), fontMap);
                } else {
                    fireFontMapChangedEvent();
                    showDesktopFrame();
                }
*/
        }
        catch (HeadlessException ex)
        {
            logger.throwing("intellibitz.sted.util.FontMapHelper",
                    "readFontMap", ex);
            JOptionPane.showMessageDialog(this,
                    "Invalid FontMap " + selectedFile.getAbsolutePath());
        }
        catch (IllegalArgumentException ex)
        {
            logger.throwing("intellibitz.sted.util.FontMapHelper",
                    "readFontMap", ex);
            JOptionPane.showMessageDialog(this,
                    "Load Failed: " + ex.getMessage());
        }
    }


    private void saveFontMap()
    {
        DesktopFrame desktopFrame =
                getSelectedFrame();
        FontMap fontMap = desktopFrame.getModel().getFontMap();
        fontMap.setFont1(desktopFrame.getMapperPanel()
                .getFontKeypad1().getSelectedFont());
        fontMap.setFont2(desktopFrame.getMapperPanel()
                .getFontKeypad2().getSelectedFont());
        try
        {
            fontMap = desktopFrame.getModel().saveFontMap();
            //add it to cache
//            fontMapCache.put(fontMap.getFileName(), fontMap);
//            get
        }
        catch (TransformerException exception)
        {
            exception
                    .printStackTrace();  //To change body of catch statement use Options | File Templates.
            JOptionPane.showMessageDialog(this,
                    fontMap.getFileName() +
                            " cannot create for writing " +
                            exception.getMessage());
        }
//        JOptionPane.showMessageDialog(stedWindow, "saved FontMap in " + selectedFile.getAbsolutePath());
    }

    public void saveAction()
    {
        FontMap fontMap = getFontMap();
        final File selectedFile = fontMap.getFontMapFile();
        if (selectedFile == null)
        {
            // try the save as functionality
            saveAsAction();
        }
        else if (!selectedFile.canWrite())
        {
            try
            {
                selectedFile.createNewFile();
                fontMap.setFontMapFile(selectedFile);
            }
            catch (IOException exception)
            {
                JOptionPane.showMessageDialog(this,
                        selectedFile +
                                " cannot create for writing " +
                                exception.getMessage());
            }
        }
        else
        {
            saveFontMap();
        }
    }

    public int saveAsAction()
    {
        FontMap fontMap = getFontMap();
        final JFileChooser jFileChooser =
                new JFileChooser(System.getProperty("user.dir"));
        final FileFilterHelper fileFilterHelper =
                new FileFilterHelper("xml", "STED FontMap files");
        jFileChooser.setFileFilter(fileFilterHelper);
        final int result = jFileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = jFileChooser.getSelectedFile();
            if (selectedFile.canWrite())
            {
                fontMap.setFontMapFile(selectedFile);
                saveFontMap();
            }
            else
            {
                JOptionPane.showMessageDialog(this,
                        selectedFile +
                                " is NOT Writable");

            }
        }
        return result;
    }

    public int saveDirty()
    {
        return saveDirty(getSelectedFrame());
    }


    public int saveDirty(DesktopFrame desktopFrame)
    {
        int result = JOptionPane.CLOSED_OPTION;
        if (null != desktopFrame)
        {
            FontMap fontMap = desktopFrame.getModel().getFontMap();
            if (fontMap != null && fontMap.isDirty())
            {
                result = JOptionPane.showConfirmDialog(this,
                        "FontMap Changed.. Do you want to save changes?",
                        "Save Changes",
                        JOptionPane.YES_NO_CANCEL_OPTION);
                // if yes, save the fontmap
                if (JOptionPane.YES_OPTION == result)
                {
                    if (fontMap.isNew())
                    {
                        result = saveAsAction();
                        // can be cancelled.. clear the fontmap
                        if (JFileChooser.CANCEL_OPTION == result)
                        {
                            fontMap.clear();
                        }
                    }
                    else
                    {
                        saveAction();
                    }
                }
                else if (JOptionPane.NO_OPTION == result)
                {
                    //TODO: need to do something sensible for NO_OPTION
                    // if no, discard the fontmap
                    // setDirty will fire events..so DONT use!
//                fontMap.setDirty(false);
                    // try clear (need to investigate if this would affect other actions!)
                    fontMap.clear();
                }
            }
        }
        return result;
    }


    public void clear()
    {
        getSelectedFrame().clear();
    }

    public FontMap getFontMap()
    {
        return getSelectedFrame().getModel()
                .getFontMap();
    }

    public void addItemToReOpenMenu(String item)
    {
        final MenuHandler menuHandler = MenuHandler.getInstance();
        final JMenu menu =
                menuHandler.getMenu(Resources.ACTION_FILE_REOPEN_COMMAND);
        MenuHandler.addReOpenItem(menu, item);
        menu.setEnabled(
                menu.getItemCount() != Resources.DEFAULT_MENU_COUNT + 1);
    }


    public DesktopFrame createFontMapperDesktopFrame()
    {
        DesktopFrame desktopFrame =
                new DesktopFrame();
        desktopFrame.addInternalFrameListener(this);
        desktopFrame.init();
        // add it to the desktop
        desktopPane.add(desktopFrame);
        desktopPane.setSelectedFrame(desktopFrame);
        desktopFrame.setEnabled(true);
        desktopFrame.setVisible(true);

        return desktopFrame;
    }

    /**
     * creates a new FontMap and loads into the Window if the Window already
     * contains a new FontMap, clears and loads them otherwise, looks up the
     * cache for a new FontMap and load them if not in cache, creates a new
     * FontMap, stores in cache and loads new FontMap
     * @return
     */
/*
    public void loadNewFontMap() {
        // FIRST TIME
        // create a new frame and set the fontmap
        // OTHER THAN FIRST TIME
        // get the frame from the cache, set it as current
        clear();
        // check the current FontMap first
        if (fontMap != null && fontMap.isNew()) {
            // clear the existing fontmap contents.. no need to clear the listeners
            fontMap.clear();
//                stedWindow.addListenersToDesktopFrame(fontMap);
//                stedWindow.createDesktopModel();
            fireFontMapChangedEvent();
        } else {
            // try the cache
            FontMap fMap = (FontMap) fontMapCache.get(Resources.ACTION_FILE_NEW_COMMAND);
            // if not found, create new one
            if (fMap == null) {
                fMap = new FontMap();
                fontMapCache.put(Resources.ACTION_FILE_NEW_COMMAND, fMap);
                addListenersToDesktopFrame(fMap);
            } else if (!fMap.isNew()) {
                fMap = new FontMap();
                fontMapCache.put(Resources.ACTION_FILE_NEW_COMMAND, fMap);
                addListenersToDesktopFrame(fMap);
            } else {
                fontMap.clear();
//                stedWindow.addListenersToDesktopFrame(fontMap);
//                stedWindow.createDesktopModel();
                fireFontMapChangedEvent();
            }
        }
        stedWindow.showDesktop();
    }
*/

    /**
     * @return
     */
    public Map<String, Collection> getClipboard()
    {
        return clipboard;
    }

    public void addToClipboard(String entry, Collection value)
    {
        clipboard.put(entry, value);
        fireStatusPosted("Copied Fontmap Entries");
    }

    public DesktopFrame getFontMapperDesktopFrame()
    {
        return getSelectedFrame();
    }

    public DesktopModel getDesktopModel()
    {
        return getFontMapperDesktopFrame().getModel();
    }

    public String getFrameTitle()
    {
        DesktopFrame desktopFrame =
                getSelectedFrame();
        if (null == desktopFrame)
        {
            return null;
        }
        else
        {
            return desktopFrame.getTitle();
        }
    }


    public void fireMessagePosted(String message)
    {
        messageEvent.setMessage(message);
        messageListener.messagePosted(messageEvent);
    }

    public void fireMessagePosted()
    {
        messageListener.messagePosted(messageEvent);
    }

    public void addMessageListener(IMessageListener messageListener)
    {
        this.messageListener = messageListener;
    }


    public void threadRunStarted(ThreadEvent e)
    {
        STEDGUI.busy();
    }

    public void threadRunning(ThreadEvent e)
    {

    }

    public void threadRunFailed(ThreadEvent e)
    {
        STEDGUI.busy();
        String message = e.getEventSource().getMessage().toString();
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
        // if FontMapReader..
        if (FontMapReadEvent.class.isInstance(e))
        {
            closeFontMap();
        }
        // if Transliterator..
        if (TransliterateEvent.class.isInstance(e))
        {
            // enable the convert action
            MenuHandler.getInstance()
                    .getAction(Resources.ACTION_CONVERT_NAME)
                    .setEnabled(true);
            // disable the stop button
            MenuHandler.getInstance().getAction(Resources.ACTION_STOP_NAME)
                    .setEnabled(false);
        }
        fireStatusPosted(message);
        STEDGUI.relax();
    }

    public void threadRunFinished(ThreadEvent e)
    {
        // if FontMapReader..
        if (FontMapReadEvent.class.isInstance(e))
        {
            DesktopModel desktopModel =
                    getSelectedFrame().getModel();
            final FontMap fontMap = desktopModel.getFontMap();
            fontMap.setDirty(false);
            desktopModel.fireFontMapChangedEvent();
            fireStatusPosted("FontMap Loaded");
        }
        // if Transliterator..
        else if (TransliterateEvent.class.isInstance(e))
        {
            // readFontMap the converted file
            getSelectedFrame().readOutputFile();
            // enable the convert action
            MenuHandler.getInstance()
                    .getAction(Resources.ACTION_CONVERT_NAME)
                    .setEnabled(true);
            // disable the stop button
            MenuHandler.getInstance().getAction(Resources.ACTION_STOP_NAME)
                    .setEnabled(false);
            fireStatusPosted("Transliterate Done");
        }
        STEDGUI.relax();
    }


    public void internalFrameClosing(InternalFrameEvent e)
    {
        DesktopFrame desktopFrame =
                (DesktopFrame) e.getInternalFrame();
        if (JOptionPane.CANCEL_OPTION != saveDirty(desktopFrame))
        {
            // when the frame closes, close the tab
            int index = getSelectedIndex();
            removeTabFrameAt(index);
            desktopFrame.close();
        }
        // todo: this must be in activated
        // hacking it for now since the first frame we load by default is not
        // getting activated
        enableCloseAction();
    }

    public void internalFrameActivated(InternalFrameEvent e)
    {
        // enable the view mapping and sample when the internal frame is shown
        MenuHandler.getInstance()
                .getMenuItem(Resources.ACTION_VIEW_MAPPING)
                .setEnabled(true);
        MenuHandler.getInstance()
                .getMenuItem(Resources.ACTION_VIEW_SAMPLE)
                .setEnabled(true);
        DesktopFrame desktopFrame =
                (DesktopFrame) e.getInternalFrame();
        desktopFrame.setEnabledFontMapTab(true);
        enableCloseAction();
    }

    public void internalFrameOpened(InternalFrameEvent e)
    {
        // todo: this must be in activated
        // hacking it for now since the first frame we load by default is not
        // getting activated
        enableCloseAction();
    }

    public void internalFrameClosed(InternalFrameEvent e)
    {
        // todo: this must be in activated
        // hacking it for now since the first frame we load by default is not
        // getting activated
        enableCloseAction();
    }

    public void internalFrameIconified(InternalFrameEvent e)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void internalFrameDeiconified(InternalFrameEvent e)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void internalFrameDeactivated(InternalFrameEvent e)
    {
    }


}