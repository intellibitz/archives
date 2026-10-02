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
 * $Id:HelpWindow.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/HelpWindow.kt $
 */

package intellibitz.sted.ui

import intellibitz.sted.event.IMessageEventSource
import intellibitz.sted.event.IMessageListener
import intellibitz.sted.event.MessageEvent
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.Resources
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.JToolBar
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLEditorKit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.Stack
import java.util.logging.Logger

class HelpWindow : JFrame(), HyperlinkListener, IMessageEventSource {
    private lateinit var textPane: JTextPane
    private lateinit var currentPages: java.util.Stack<URL>
    private lateinit var backPages: java.util.Stack<URL>
    private lateinit var forwardPages: java.util.Stack<URL>
    private lateinit var backButton: JButton
    private lateinit var forwardButton: JButton
    private lateinit var homeButton: JButton
    private var homepage: URL? = null
    private var messageEvent: MessageEvent? = null
    private var messageListener: IMessageListener? = null
    private constructor() : super(Resources.getResource(Resources.TITLE_HELP)) {
        setIconImage(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP)))
        currentPages = java.util.Stack<URL>()
        backPages = java.util.Stack<URL>()
        forwardPages = java.util.Stack<URL>()
        val jToolBar: JToolBar = JToolBar(JToolBar.HORIZONTAL)
        jToolBar.setFloatable(false)
        homeButton = JButton(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP_HOME)))
        homeButton.addActionListener(ActionListener() {    override fun actionPerformed(e: ActionEvent) {
        goHome()
    }
})
        homeButton.setToolTipText("Table Of Contents")
        jToolBar.add(homeButton)
        backButton = JButton(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP_BACK)))
        backButton.addActionListener(ActionListener() {    override fun actionPerformed(e: ActionEvent) {
        goBack()
    }
})
        backButton.setToolTipText("Back")
        jToolBar.add(backButton)
        forwardButton = JButton(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP_FORWARD)))
        forwardButton.addActionListener(ActionListener() {    override fun actionPerformed(e: ActionEvent) {
        goForward()
    }
})
        forwardButton.setToolTipText("Forward")
        jToolBar.add(forwardButton)
        getContentPane()
        textPane = JTextPane()
        textPane.setEditable(false)
        textPane.setSize(400, 400)
        textPane.setEditorKit(HTMLEditorKit())
        textPane.addHyperlinkListener(this)
        val scroller: JScrollPane = JScrollPane()
        scroller.getViewport()
        getContentPane()
        goHome()
        setSize(textPane.getSize())
        setDefaultLookAndFeelDecorated(true)
        setState(JFrame.MAXIMIZED_HORIZ)
        setExtendedState(JFrame.MAXIMIZED_BOTH)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
        addWindowListener(WindowAdapter() {    override fun windowClosing(e: WindowEvent) {
        setVisible(false)
    }
})
        messageEvent = MessageEvent(this)
        pack()
    }
    fun fireMessagePosted(message: String) {
        messageEvent.setMessage(message)
        messageListener.messagePosted(messageEvent)
    }
    fun fireMessagePosted() {
        messageListener.messagePosted(messageEvent)
    }
    fun addMessageListener(messageListener: IMessageListener) {
        this = messageListener
    }
    private fun goHome() {
        if ((homepage == null)) {
            go(HELP_INDEX)
            homepage = textPane.getPage()
        }
        else {
            setURL(homepage)
        }
    }
    private fun goBack() {
        val url: URL = backPages.pop()
        if (currentPages.isEmpty()) {
            forwardPages.push(url)
        }
        else {
            forwardPages.push(currentPages.pop())
        }
        setPage(url)
    }
    private fun goForward() {
        val url: URL = forwardPages.pop()
        if (currentPages.isEmpty()) {
            backPages.push(url)
        }
        else {
            backPages.push(currentPages.pop())
        }
        setPage(url)
    }
    private fun setPage(url: URL) {
        try {
            textPane.setPage(url)
            currentPages.push(url)
            setButtonState()
        }
        catch (e: IOException) {
            logger.severe(e.getMessage())
            fireMessagePosted(("Cannot set page " + e.getMessage()))
        }
    }
    private fun setURL(url: URL) {
        try {
            textPane.setPage(url)
            if (!currentPages.isEmpty()) {
                backPages.push(currentPages.pop())
            }
            forwardPages.clear()
            currentPages.push(url)
        }
        catch (e: IOException) {
            logger.severe(("Cannot set page " + url))
            fireMessagePosted(("Cannot set page " + url))
        }
        setButtonState()
    }
    private fun setButtonState() {
        backButton.setEnabled(!backPages.isEmpty())
        forwardButton.setEnabled(!forwardPages.isEmpty())
        val index: URL = textPane.getPage()
        homeButton.setEnabled(((index != null) && (index.getPath() == 1)))
    }
    private fun go(path: String) {
        try {
            if ((path != null)) {
                if ((path.indexOf(":") == 1)) {
                    val file: File = File(path)
                    setURL(URL(("file:///" + file.getAbsolutePath())))
                }
                else {
                    setURL(URL(("file:///" + path)))
                }
            }
        }
        catch (e: IOException) {
            logger.throwing(getClass(), "actionPerformed", e)
            fireMessagePosted(("Cannot go to page - IOException occured: " + e.getMessage()))
        }
    }
    private fun go(url: URL) {
        if (url.getProtocol()) {
            go(url.getPath())
        }
        else {
            if (url.getProtocol()) {

            }
            else {
                setURL(url)
            }
        }
    }
    override fun hyperlinkUpdate(e: HyperlinkEvent) {
        if ((e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)) {
            go(e.getURL())
        }
    }

    companion object {
        private val HELP_INDEX: String = (FileHelper.suffixFileSeparator(System.getProperty(Resources.STED_HOME_PATH, "../")) + Resources.getResource(Resources.HELP_INDEX))
        private val logger: Logger = Logger.getLogger(HelpWindow::class.java)
        private var helpWindow: HelpWindow? = null
        @Synchronized
        @JvmStatic
        fun getInstance(): HelpWindow {
            if ((helpWindow == null)) {
                helpWindow = HelpWindow()
                helpWindow.setSize(600, 800)
            }
            return helpWindow
        }
    }
}
