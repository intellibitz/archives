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
 * $Id:TransliterateAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/TransliterateAction.java $
 */

/**
 * $Id:TransliterateAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/TransliterateAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.fontmap.Converter;
import intellibitz.sted.ui.STEDWindow;
import intellibitz.sted.ui.TabDesktop;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.io.File;

public class TransliterateAction
        extends TableModelListenerAction {
    public TransliterateAction() {
        super();
    }

    /**
     * This fine grain notification tells listeners the exact range of cells,
     * rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e) {
        setEnabled(((TableModel) e.getSource()).getRowCount() > 0 &&
                getSTEDWindow().getDesktop()
                        .getFontMapperDesktopFrame()
                        .enableConverterIfFilesLoaded());
    }

    public void actionPerformed(ActionEvent e) {
        final STEDWindow stedWindow = getSTEDWindow();
        final File fileToConvert =
                stedWindow.getDesktop()
                        .getDesktopModel().getInputFile();
        final File convertedFile =
                stedWindow.getDesktop()
                        .getDesktopModel().getOutputFile();
        if (fileToConvert == null || convertedFile == null) {
            fireMessagePosted("Select valid files for both input and output");
            return;
        }
        if (fileToConvert.getName().equals(convertedFile.getName())) {
            fireMessagePosted(
                    "Input and Output files are same.. select different files");
            return;
        }
        fireStatusPosted("Begin Converting...");
        final Converter converter = getConverter(stedWindow.getDesktop());
        // the converter does not update the GUI.. so start an independent thread
        // the GUI will listen to the converter and update the status thru event handling -- THREAD SAFE!!!
        converter.start();
        setEnabled(false);
    }

    public Converter getConverter(TabDesktop desktop) {
        final Converter converter =
                new Converter(desktop.getFontMap(),
                        desktop.getDesktopModel().getInputFile(),
                        desktop.getDesktopModel().getOutputFile());
        final JCheckBoxMenuItem preserve =
                (JCheckBoxMenuItem) MenuHandler.getInstance().getMenuItem(
                        Resources.ACTION_PRESERVE_TAGS);
        converter.setHTMLAware(preserve.isSelected());
        final JCheckBoxMenuItem reverse =
                (JCheckBoxMenuItem) MenuHandler.getInstance().getMenuItem(
                        Resources.ACTION_TRANSLITERATE_REVERSE);
        converter.setReverseTransliterate(reverse.isSelected());
        converter.addThreadListener(desktop);
        final TransliterateStopAction stop =
                (TransliterateStopAction) MenuHandler.getInstance().getAction(
                        Resources.ACTION_STOP_NAME);
        stop.setConverter(converter);
        stop.setEnabled(true);
        return converter;
    }
}
