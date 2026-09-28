package intellibitz.sted.widgets

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.plaf.basic.BasicButtonUI

class ButtonTabComponent(title: String, pane: JTabbedPane?) : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)), ActionListener {
    val tabTitle: JLabel
    protected var actionListener: ActionListener? = null

    init {
        requireNotNull(pane) { "TabbedPane is null" }
        isOpaque = false

        tabTitle = JLabel(title)
        add(tabTitle)
        tabTitle.border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
        
        val tabButton = TabButton()
        tabButton.addActionListener(this)
        add(tabButton)
        
        border = BorderFactory.createEmptyBorder(2, 0, 0, 0)
    }

    override fun actionPerformed(e: ActionEvent) {
        e.source = this
        actionListener?.actionPerformed(e)
    }

    fun addActionListener(actionListener: ActionListener) {
        this.actionListener = actionListener
    }

    private inner class TabButton : JButton() {
        init {
            val size = 17
            preferredSize = Dimension(size, size)
            toolTipText = "close this tab"
            setUI(BasicButtonUI())
            isContentAreaFilled = false
            isFocusable = false
            border = BorderFactory.createEtchedBorder()
            isBorderPainted = false
            addMouseListener(buttonMouseListener)
            isRolloverEnabled = true
        }

        override fun updateUI() {}

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g.create() as Graphics2D
            if (model.isPressed) {
                g2.translate(1, 1)
            }
            g2.stroke = BasicStroke(2f)
            g2.color = Color.BLACK
            if (model.isRollover) {
                g2.color = Color.MAGENTA
            }
            val delta = 6
            g2.drawLine(delta, delta, width - delta - 1, height - delta - 1)
            g2.drawLine(width - delta - 1, delta, delta, height - delta - 1)
            g2.dispose()
        }
    }

    companion object {
        private val buttonMouseListener: MouseListener = object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                val component = e.component
                if (component is AbstractButton) {
                    component.isBorderPainted = true
                }
            }

            override fun mouseExited(e: MouseEvent) {
                val component = e.component
                if (component is AbstractButton) {
                    component.isBorderPainted = false
                }
            }
        }
    }
}
