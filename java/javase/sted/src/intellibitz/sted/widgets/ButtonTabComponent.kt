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
 * $Id:ButtonTabComponent.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/widgets/ButtonTabComponent.kt $
 */

package intellibitz.sted.widgets

import javax.swing.AbstractButton
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.plaf.basic.BasicButtonUI
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

class ButtonTabComponent : JPanel(), ActionListener {
    private var tabTitle: JLabel? = null
    protected var actionListener: ActionListener = null
    constructor(title: String, pane: JTabbedPane) : super(FlowLayout(FlowLayout.LEFT, 0, 0)) {
        if ((pane == null)) {
            throw NullPointerException("TabbedPane is null")
        }
        setOpaque(false)
        tabTitle = JLabel(title)
        add(tabTitle)
        tabTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5))
        var tabButton: JButton = TabButton()
        tabButton.addActionListener(this)
        add(tabButton)
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0))
    }
    fun getTabTitle(): JLabel {
        return tabTitle
    }
    override fun actionPerformed(e: ActionEvent) {
        e.setSource(this)
        actionListener.actionPerformed(e)
    }
    fun addActionListener(actionListener: ActionListener) {
        this = actionListener
    }
    class TabButton : JButton() {
    constructor() : super() {
        var size: Int = 17
        setPreferredSize(Dimension(size, size))
        setToolTipText("close this tab")
        setUI(BasicButtonUI())
        setContentAreaFilled(false)
        setFocusable(false)
        setBorder(BorderFactory.createEtchedBorder())
        setBorderPainted(false)
        addMouseListener(buttonMouseListener)
        setRolloverEnabled(true)
    }
    fun updateUI() {

    }
    override protected fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        var g2: Graphics2D = (g.create() as Graphics2D)
        if (getModel()) {
            g2.translate(1, 1)
        }
        g2.setStroke(BasicStroke(2))
        g2.setColor(Color.BLACK)
        if (getModel()) {
            g2.setColor(Color.MAGENTA)
        }
        var delta: Int = 6
        g2.drawLine(delta, delta, ((getWidth() - delta) - 1), ((getHeight() - delta) - 1))
        g2.drawLine(((getWidth() - delta) - 1), delta, delta, ((getHeight() - delta) - 1))
        g2.dispose()
    }
    }

    companion object {
        private val buttonMouseListener: MouseListener = MouseAdapter() {    override fun mouseEntered(e: MouseEvent) {
        var component: Component = e.getComponent()
        if ((component is AbstractButton)) {
            var button: AbstractButton = (component as AbstractButton)
            button.setBorderPainted(true)
        }
    }
    override fun mouseExited(e: MouseEvent) {
        var component: Component = e.getComponent()
        if ((component is AbstractButton)) {
            var button: AbstractButton = (component as AbstractButton)
            button.setBorderPainted(false)
        }
    }
}
    }
}
