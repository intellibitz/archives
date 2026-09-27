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
 * $Id:AboutDialog.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/widgets/AboutDialog.java $
 */

/**
 * $Id:AboutDialog.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/widgets/AboutDialog.java $
 */

package intellibitz.sted.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Displays the About message.
 */
public class AboutDialog
        extends JDialog
        implements KeyListener,
        MouseListener {
    private final JButton ok;

    public AboutDialog(String title, Component aboutDescriptor) {
        super();
        setTitle(title);
        getContentPane().add(aboutDescriptor);
        ok = new JButton("ok");
        ok.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                AboutDialog.this.setVisible(false);
            }
        });
        ok.addKeyListener(this);
        ok.setFocusable(true);
        final JPanel _pane = new JPanel();
        _pane.add(ok);
        getContentPane().add(BorderLayout.SOUTH, _pane);
        getRootPane().setDefaultButton(ok);
        setResizable(false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        pack();
        ok.requestFocus();
        setVisible(false);
    }

    public void setOKText(String okTitle) {
        ok.setText(okTitle);
    }

    /**
     * Invoked when a key has been pressed. See the class description for {@link
     * java.awt.event.KeyEvent} for a definition of a key pressed event.
     */
    public void keyPressed(KeyEvent e) {
        setVisible(false);
    }

    /**
     * Invoked when a key has been released. See the class description for
     * {@link KeyEvent} for a definition of a key released event.
     */
    public void keyReleased(KeyEvent e) {
        setVisible(false);
    }

    /**
     * Invoked when a key has been typed. See the class description for {@link
     * KeyEvent} for a definition of a key typed event.
     */
    public void keyTyped(KeyEvent e) {
        setVisible(false);
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a component.
     */
    public void mouseClicked(MouseEvent e) {
        setVisible(false);
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {
//        setVisible(false);
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {
//        setVisible(false);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
        setVisible(false);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
        setVisible(false);
    }

}
