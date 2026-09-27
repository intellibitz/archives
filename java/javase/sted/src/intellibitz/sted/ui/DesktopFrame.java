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
 * $Id: DesktopFrame.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DesktopFrame.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.event.FontMapChangeEvent;
import intellibitz.sted.event.FontMapChangeListener;
import intellibitz.sted.event.FontMapEntriesChangeEvent;
import intellibitz.sted.event.IFontMapEntriesChangeListener;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.logging.Logger;

/**
 * Contains FontMap and all its related Entities.
 */
public class DesktopFrame
        extends JInternalFrame
        implements TableModelListener,
        FontMapChangeListener,
        IFontMapEntriesChangeListener
{
    // to hold all the different tabs.. FontMap, Input File Viewer, Output etc.,
    private JTabbedPane tabbedPane;
    // panel showing the FontMap
    private MapperPanel mapperPanel;
    // input editor pane
    private FileViewer inputFileViewer;
    // output editor pane
    private FileViewer outputFileViewer;
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.ui.DesktopFrame");
    private DesktopModel desktopModel;

    public DesktopFrame()
    {
        super("FontMapperInternalFrame", false, true, false, false);
    }

    public void init()
    {
        mapperPanel = new MapperPanel();
        mapperPanel.init();
        setNormalIcon();
        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.setBorder(BorderFactory.createRaisedBevelBorder());
        // adds the font mapper panel as one of the tab
        tabbedPane.addTab(Resources.getResource(Resources.TITLE_TAB_FONTMAP),
                Resources.getSTEDIcon(),
                mapperPanel,
                Resources.getResource(Resources.TIP_TAB_FONTMAP));
        inputFileViewer =
                new FileViewer(
                        Resources.getSystemResourceIcon(Resources.getResource(
                                Resources.ICON_FILE_INPUT)));

        // adds the input file viewer as one of the tab
        tabbedPane.addTab(Resources.getResource(Resources.TITLE_TAB_INPUT),
                Resources.getSystemResourceIcon(Resources.getResource(
                        Resources.ICON_FILE_INPUT)),
                inputFileViewer);
        outputFileViewer =
                new FileViewer(
                        Resources.getSystemResourceIcon(Resources.getResource(
                                Resources.ICON_FILE_OUTPUT)));

        // adds the output file viewer as one of the tab
        tabbedPane.addTab(Resources.getResource(Resources.TITLE_TAB_OUTPUT),
                Resources.getSystemResourceIcon(Resources.getResource(
                        Resources.ICON_FILE_OUTPUT)),
                outputFileViewer);

        // enables all the tabs
        tabbedPane.setEnabledAt(0, false);
        tabbedPane.setEnabledAt(1, false);
        tabbedPane.setEnabledAt(2, false);

        getContentPane().add(tabbedPane);
        setBounds(mapperPanel.getBounds());
        setLocation(mapperPanel.getLocation());
        pack();
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        setVisible(true);
    }

    public void load()
    {
        mapperPanel.load();
        mapperPanel.getMappingEntryPanel().addTableModelListener(this);
        mapperPanel.setSampleInput(
                Resources.getResource(Resources.SAMPLE_INPUT_TEXT));

        tabbedPane.setToolTipTextAt(1, MenuHandler.getToolTips().get(
                Resources.ACTION_SELECT_INPUT_FILE_COMMAND));
        tabbedPane.setToolTipTextAt(2, MenuHandler.getToolTips().get(
                Resources.ACTION_SELECT_OUTPUT_FILE_COMMAND));

    }

    public FileViewer getInputFileViewer()
    {
        return inputFileViewer;
    }

    public FileViewer getOutputFileViewer()
    {
        return outputFileViewer;
    }

    public DesktopModel getModel()
    {
        return desktopModel;
    }

    public void setModel(DesktopModel model)
    {
        desktopModel = model;
        desktopModel
                .addFontMapChangeListener(this);
        desktopModel
                .addFontMapChangeListener(mapperPanel);
        desktopModel
                .addFontMapChangeListener(mapperPanel.getMappingEntryPanel());
        desktopModel
                .addFontMapChangeListener(
                        mapperPanel.getMappingEntryPanel().getMappingRules());
    }

    private void setNormalIcon()
    {
        setFrameIcon(Resources.getCleanIcon());
    }

    private void setEditIcon()
    {
        setFrameIcon(Resources.getDirtyIcon());
    }

    public void clear()
    {
        mapperPanel.clear();
        desktopModel.clear();
        setTitle(Resources.EMPTY_STRING);
        setNormalIcon();
    }

    private void setInternalFrameSelected(boolean flag)
    {
        try
        {
            setSelected(flag);
        }
        catch (PropertyVetoException e)
        {
            logger.throwing(getClass().getName(), "setInternalFrameSelected",
                    e);
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

    public void showFrame()
    {
        setVisible(true);
        tabbedPane.setVisible(true);
//        tabbedPane.setSelectedIndex(0);
        setFrameTitle(getModel().getFontMap());
        try
        {
            setMaximum(true);
        }
        catch (PropertyVetoException e)
        {
            logger.throwing(getClass().getName(), "showFrame", e);
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        mapperPanel.getMappingEntryPanel().getWord1().requestFocus();
//        setBounds(tabbedPane.getBounds());
    }

    private void hideFrame()
    {
        setInternalFrameSelected(false);
        setVisible(false);
    }

    public MapperPanel getMapperPanel()
    {
        return mapperPanel;
    }

    /**
     * @param flag boolean to enable or disable the tabs
     */
    public void enableTabs(boolean flag)
    {
        final int count = tabbedPane.getTabCount();
        if (count > 0)
        {
            for (int i = 0; i < count; i++)
            {
                tabbedPane.setEnabledAt(i, flag);
            }
        }
//        tabbedPane.setVisible(flag);
    }

    public void close()
    {
        hideFrame();
        enableTabs(false);
        final MenuHandler menuHandler = MenuHandler.getInstance();
        menuHandler.getMenuItem(Resources.ACTION_VIEW_MAPPING)
                .setEnabled(false);
        menuHandler.getMenuItem(Resources.ACTION_VIEW_SAMPLE).setEnabled(false);
        menuHandler.getMenuItem(Resources.ACTION_PASTE_COMMAND)
                .setEnabled(false);
        menuHandler.getMenuItem(Resources.ACTION_DELETE_COMMAND)
                .setEnabled(false);
        menuHandler.getMenuItem(Resources.ACTION_SELECT_ALL_COMMAND)
                .setEnabled(false);
        menuHandler.getMenuItem(Resources.ACTION_CUT_COMMAND).setEnabled(false);
        menuHandler.getMenuItem(Resources.ACTION_COPY_COMMAND)
                .setEnabled(false);
    }


    public void setEnabledFontMapTab
            (boolean flag)
    {
        if (tabbedPane.getTabCount() > 0)
        {
            tabbedPane.setEnabledAt(0, flag);
        }
    }

    /**
     * This fine grain notification tells listeners the exact range of cells,
     * rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e)
    {
        if (getModel().getFontMap().isDirty())
        {
            setEditIcon();
        }
    }

    private void setFileIcon(FontMap fontMap)
    {
        if (fontMap.isDirty())
        {
            setEditIcon();
        }
        else
        {
            setNormalIcon();
        }
    }

    public void setFrameTitle(FontMap fontMap)
    {
        if (fontMap.getFontMapFile() != null)
        {
            setTitle(fontMap.getFontMapFile().getAbsolutePath());
        }
    }

    /**
     * called when the FontMap state is changed.. typically when a FontMapEntry
     * is added to FontMap
     *
     * @param e FontMapChangeEvent
     */
    public void stateChanged(FontMapChangeEvent e)
    {
        final FontMap fontMap = e.getFontMap();
        setFrameTitle(fontMap);
        setFileIcon(fontMap);
        inputFileViewer.setFont(fontMap.getFont1());
        outputFileViewer.setFont(fontMap.getFont2());
        enableConverterIfFilesLoaded();
    }

    public boolean enableConverterIfFilesLoaded()
    {
        boolean flag = desktopModel.isReadyForTransliteration();
        MenuHandler.getInstance().getActions()
                .get(Resources.ACTION_CONVERT_NAME).setEnabled(flag);
        return flag;
    }


    public void setInputFile(File file)
    {
        desktopModel.setInputFile(file);
        if (file != null)
        {
            readFile(1);
        }
    }

    private void readFile(int index)
    {
//        STEDGUI.busy();
        tabbedPane.setEnabledAt(index, true);
        tabbedPane.setSelectedIndex(index);
        switch (index)
        {
            case 1:
                inputFileViewer.setFileName(
                        desktopModel.getInputFile().getAbsolutePath());
                inputFileViewer.readFile();
                break;
            case 2:
                readOutputFile();
                break;
            default:
                break;
        }
//        STEDGUI.relax();
    }

    public void setOutputFile(File file)
    {
        desktopModel.setOutputFile(file);
        if (file != null)
        {
            readFile(2);
        }
    }

    //TODO: change this to private and add this as the respective listener
    public void readOutputFile()
    {
        if (desktopModel.getOutputFile() != null)
        {
            outputFileViewer.setFileName(
                    desktopModel.getOutputFile().getAbsolutePath());
            outputFileViewer.readFile();
        }
    }

    public void stateChanged(FontMapEntriesChangeEvent e)
    {
        desktopModel.fireFontMapChangedEvent();
    }

}


