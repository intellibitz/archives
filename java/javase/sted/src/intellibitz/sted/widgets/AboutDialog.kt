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
 * $Id:AboutDialog.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/widgets/AboutDialog.kt $
 */

package intellibitz.sted.widgets


import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

class AboutDialog : JDialog() {
    private val ok: JButton
    /**
         * Invoked when a key has been pressed. See the class description for {@link
         * java.awt.event.KeyEvent} for a definition of a key pressed event.
         */
    /**
         * Invoked when a key has been released. See the class description for
         * {@link KeyEvent} for a definition of a key released event.
         */
    /**
         * Invoked when a key has been typed. See the class description for {@link
         * KeyEvent} for a definition of a key typed event.
         */
    /**
         * Invoked when the mouse button has been clicked (pressed and released) on
         * a component.
         */
    /**
         * Invoked when the mouse enters a component.
         */
    /**
         * Invoked when the mouse exits a component.
         */
    /**
         * Invoked when a mouse button has been pressed on a component.
         */
    /**
         * Invoked when a mouse button has been released on a component.
         */
    constructor(title: String, aboutDescriptor: Component) : super() {
            setTitle(title)
            getContentPane().add(aboutDescriptor)
            ok = JButton("ok")
            ok.addActionListener(AbstractAction()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    AboutDialog.this.setVisible(false)
                }
            })
            ok.addKeyListener(this)
            ok.setFocusable(true)
            val _pane: JPanel = JPanel()
            _pane.add(ok)
            getContentPane().add(BorderLayout.SOUTH, _pane)
            getRootPane().setDefaultButton(ok)
            setResizable(false)
            setDefaultCloseOperation(pack as JDialog.HIDE_ON_CLOSE)()
            ok.requestFocus()
            setVisible(false)
        }
    fun setOKText(okTitle: String) {
            ok.setText(okTitle)
        }
    override fun keyPressed(e: KeyEvent) {
            setVisible(false)
        }
    override fun keyReleased(e: KeyEvent) {
            setVisible(false)
        }
    override fun keyTyped(e: KeyEvent) {
            setVisible(false)
        }
    override fun mouseClicked(e: MouseEvent) {
            setVisible(false)
        }
    override fun mouseEntered(e: MouseEvent) {
    //        setVisible(false)
        }
    override fun mouseExited(e: MouseEvent) {
    //        setVisible(false)
        }
    override fun mousePressed(e: MouseEvent) {
            setVisible(false)
        }
    override fun mouseReleased(e: MouseEvent) {
            setVisible(false)
        }
}
