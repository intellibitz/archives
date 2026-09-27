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
 * $Id: FontKeypad.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/FontKeypad.java $
 */

/**
 * $Id: FontKeypad.java 56 2007-05-19 06:47:59Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/FontKeypad.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.actions.LoadFontAction;
import intellibitz.sted.event.*;
import intellibitz.sted.fontmap.FontInfo;
import intellibitz.sted.fontmap.FontMap;
import intellibitz.sted.util.Resources;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

/**
 * FontKeypad holds font dropdown and keypad for selecting characters
 */
public abstract class FontKeypad
        extends JPanel
        implements ItemListener,
        FontMapChangeListener,
        IKeypadEventSource {
    private FontMap fontMap;
    private FontList fontSelector;
    private JPanel keypad;
    private Font currentFont;
    private final ArrayList<JButton> keys = new ArrayList<JButton>();
    private static final int KEY_COLUMNS = Integer.parseInt(
            Resources.getSetting(Resources.KEYPAD_COLUMN_COUNT));
    private static final int FONT_MAX_INDEX = Integer.parseInt(
            Resources.getSetting(Resources.FONT_CHAR_MAXINDEX));
    private KeypadEvent keypadEvent;
    private EventListenerList keypadListeners;

    protected FontKeypad() {
        super();
    }

    public void init() {
        final TitledBorder titledBorder =
                new TitledBorder(Resources.getResource(Resources.TITLE_KEYPAD));
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        setBorder(titledBorder);
        final GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;

        JButton loadFont = new JButton(new LoadFontAction(this));
        gridBagLayout.setConstraints(loadFont, gridBagConstraints);
        add(loadFont);

        fontSelector =
                new FontList(new FontsListModel(Resources.getFonts()));
        setCurrentFont((String) fontSelector.getItemAt(0));
        fontSelector.setSelectedItem(currentFont);
        fontSelector.addItemListener(this);
        gridBagLayout.setConstraints(fontSelector, gridBagConstraints);
        add(fontSelector);

        //

        gridBagConstraints.weightx = GridBagConstraints.RELATIVE;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.gridheight = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        keypadListeners = new EventListenerList();
        keypadEvent = new KeypadEvent(this);

        final JComponent fontKeypad = getFontKeypad();
        gridBagLayout.setConstraints(fontKeypad, gridBagConstraints);
        //
        add(fontKeypad);
    }

    public void load() {
    }

    public ArrayList<JButton> getKeys() {
        return keys;
    }

    public void itemStateChanged(ItemEvent e) {
        setCurrentFont(e.getItem().toString());
        resetKeypad();
    }

    void setCurrentFont(String fontName) {
        setCurrentFont(Resources.getFont(fontName).getFont());
    }

    void setCurrentFont(Font font) {
        if (font == null) {
            fontSelector.setSelectedIndex(0);
            currentFont = Resources
                    .getFont(fontSelector.getSelectedItem().toString())
                    .getFont();
        } else {
            currentFont = font;
            fontSelector.setSelectedItem(currentFont.getName());
        }
        setFont(currentFont);
    }

    public void stateChanged(FontMapChangeEvent e) {
        fontMap = e.getFontMap();
        setCurrentFont();
        resetKeypad();
    }


    public String getSelectedFont() {
        return fontSelector.getSelectedItem().toString();
    }

    private JScrollPane getFontKeypad() {
        keypad = new JPanel();
        keypad.setBorder(BorderFactory.createEmptyBorder());
//        resetKeypad();
        final JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.getViewport().add(keypad);
        return jScrollPane;
    }

    private void resetKeypad() {
        keypad.removeAll();
        final GridBagLayout gridBagLayout = new GridBagLayout();
        keypad.setLayout(gridBagLayout);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 0;
        final int numOfGlyphs = currentFont.getNumGlyphs();
        for (int i = 0, j = 0; i < FONT_MAX_INDEX && j < numOfGlyphs; i++) {
            final char c = (char) i;
            if (currentFont.canDisplay(c)) {
                final String cmd = Resources.EMPTY_STRING + c;
                final JButton keyButton;
                if (!keys.isEmpty() && j < keys.size()) {
                    keyButton = keys.get(j);
                } else {
                    keyButton = new JButton();
                    keys.add(j, keyButton);
                }
                // remove all the action listeners previously added
                // only 1 action listener to be added per button
                final ActionListener[] actionListeners =
                        keyButton.getActionListeners();
                if (actionListeners != null && actionListeners.length > 0) {
                    for (final ActionListener newVar : actionListeners) {
                        keyButton.removeActionListener(newVar);
                    }
                }
                keyButton.setFont(currentFont);
                keyButton.setText(cmd);
                gridBagConstraints.gridwidth = 1;
                if ((j + 1) % KEY_COLUMNS == 0) {
                    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
                }
                gridBagLayout.setConstraints(keyButton, gridBagConstraints);
                keypad.add(keyButton);
                j++;
            }
        }
//        addKeypadListener();
        fireKeypadReset();
        keypad.updateUI();
        // garbage collect
        System.gc();
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    public void fireKeypadReset() {
        // Guaranteed to return a non-null array
        final Object[] listeners = keypadListeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == IKeypadListener.class) {
                // Lazily create the event:
                if (keypadEvent == null) {
                    keypadEvent = new KeypadEvent(this);
                }
                ((IKeypadListener) listeners[i + 1])
                        .keypadReset(keypadEvent);
            }
        }
    }

    public void addKeypadListener(IKeypadListener keypadListener) {
        keypadListeners.add(IKeypadListener.class, keypadListener);
    }

    public void removeKeypadListener(IKeypadListener keypadListener) {
        keypadListeners.remove(IKeypadListener.class, keypadListener);
    }


    FontMap getFontMap() {
        return fontMap;
    }

    Font getCurrentFont() {
        return currentFont;
    }

    public FontList getFontSelector() {
        return fontSelector;
    }

    static public class FontsListModel
            extends DefaultComboBoxModel
            implements ChangeListener {
        private Map<String, FontInfo> fonts;
        private static final Logger logger = Logger.getLogger(
                "intellibitz.sted.ui.FontKeypad$FontsListModel");

        public FontsListModel() {
            super();
        }

        public FontsListModel(Map<String, FontInfo> fonts) {
            this();
            setFonts(fonts);
        }

        public void setFonts(Map<String, FontInfo> fonts) {
            logger.entering(getClass().getName(), "setFonts");
            this.fonts = fonts;
            refreshFonts();
        }

        private void refreshFonts() {
            logger.entering(getClass().getName(), "refreshFonts");
            removeAllElements();
            final Object[] contents = fonts.keySet().toArray();
            Arrays.sort(contents);
            for (final Object newVar : contents) {
                addElement(newVar);
            }
        }

        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        public void stateChanged(ChangeEvent e) {
            setFonts(Resources.getFonts());
            fireContentsChanged(this, 0, fonts.size());
        }
    }

    static public class FontList
            extends JComboBox
            implements ChangeListener {
        /**
         * Creates a <code>JComboBox</code> that takes it's items from an
         * existing <code>ComboBoxModel</code>.  Since the
         * <code>ComboBoxModel</code> is provided, a combo box created using
         * this constructor does not create a default combo box model and may
         * impact how the insert, remove and add methods behave.
         *
         * @param aModel the <code>ComboBoxModel</code> that provides the
         *               displayed list of items
         * @see DefaultComboBoxModel
         */
        public FontList(ComboBoxModel aModel) {
            super(aModel);    //To change body of overriden methods use Options | File Templates.
        }

        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        public void stateChanged(ChangeEvent e) {
            ((FontsListModel) getModel()).stateChanged(e);
            setSelectedItem(
                    ((FontListChangeEvent) e).getFontChanged().getName());
            updateUI();
        }
    }

/*
    public void setStedWindow(STEDWindow stedWindow)
    {
        this.stedWindow = stedWindow;
    }
*/

    abstract protected void setCurrentFont();

//    abstract protected void addKeypadListener();

    abstract public void loadFont(File font);

}

