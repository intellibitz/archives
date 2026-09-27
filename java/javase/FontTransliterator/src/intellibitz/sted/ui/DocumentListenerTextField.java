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
 * $Id:DocumentListenerTextField.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DocumentListenerTextField.java $
 */

/**
 * $Id:DocumentListenerTextField.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DocumentListenerTextField.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.fontmap.SampleTextConverter;
import intellibitz.sted.util.MenuHandler;
import intellibitz.sted.util.Resources;
import intellibitz.sted.widgets.FontChangeTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentListenerTextField
        extends FontChangeTextField
        implements DocumentListener {
    private SampleTextConverter converter;
    private FontMap fontMap;
    private MapperPanel mapperPanel;
//    private STEDWindow stedWindow;

    public DocumentListenerTextField() {
        super();
    }

/*
    public DocumentListenerTextField(MapperPanel mapperPanel,
            STEDWindow stedWindow)
    {
        this();
        this.mapperPanel = mapperPanel;
        this.stedWindow = stedWindow;
    }
*/

    public void load() {

    }

    public void insertUpdate(DocumentEvent e) {
        convertSampleText(e);
    }

    public void removeUpdate(DocumentEvent e) {
        convertSampleText(e);
    }

    public void changedUpdate(DocumentEvent e) {
        convertSampleText(e);
    }

    private void convertSampleText(DocumentEvent e) {
        if (e.getDocument().getLength() > 0) {
            if (converter == null) {
                converter = new SampleTextConverter(mapperPanel);
            }
            converter.setFontMap(fontMap);
            final MenuHandler menuHandler = MenuHandler.getInstance();
            final JCheckBoxMenuItem preserve =
                    (JCheckBoxMenuItem) menuHandler.getMenuItem
                            (Resources.ACTION_PRESERVE_TAGS);
            converter.setHTMLAware(preserve.isSelected());
            final JCheckBoxMenuItem reverse =
                    (JCheckBoxMenuItem) menuHandler.getMenuItem
                            (Resources.ACTION_TRANSLITERATE_REVERSE);
            converter.setReverseTransliterate(reverse.isSelected());
            SwingUtilities.invokeLater(converter);
        }
    }

    public void setFontMap(FontMap fontMap) {
        this.fontMap = fontMap;
    }

    public void setFontMapperPanel(MapperPanel mapperPanel) {
        this.mapperPanel = mapperPanel;
    }

}

