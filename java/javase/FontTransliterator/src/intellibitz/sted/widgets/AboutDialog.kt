package intellibitz.sted.widgets

import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.*
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

open class AboutDialog(title: String, aboutDescriptor: Component) : JDialog(), KeyListener, MouseListener {
    private val ok: JButton

    init {
        setTitle(title)
        contentPane.add(aboutDescriptor)
        ok = JButton("ok")
        ok.addActionListener(object : AbstractAction() {
            override fun actionPerformed(evt: ActionEvent) {
                this@AboutDialog.isVisible = false
            }
        })
        ok.addKeyListener(this)
        ok.isFocusable = true
        val _pane = JPanel()
        _pane.add(ok)
        contentPane.add(BorderLayout.SOUTH, _pane)
        rootPane.defaultButton = ok
        isResizable = false
        defaultCloseOperation = HIDE_ON_CLOSE
        pack()
        ok.requestFocus()
        isVisible = false
    }

    fun setOKText(okTitle: String) {
        ok.text = okTitle
    }

    override fun keyPressed(e: KeyEvent) {
        isVisible = false
    }

    override fun keyReleased(e: KeyEvent) {
        isVisible = false
    }

    override fun keyTyped(e: KeyEvent) {
        isVisible = false
    }

    override fun mouseClicked(e: MouseEvent) {
        isVisible = false
    }

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}

    override fun mousePressed(e: MouseEvent) {
        isVisible = false
    }

    override fun mouseReleased(e: MouseEvent) {
        isVisible = false
    }
}
