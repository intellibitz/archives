package intellibitz.sted.ui

import intellibitz.sted.event.IMessageEventSource
import intellibitz.sted.event.IMessageListener
import intellibitz.sted.event.MessageEvent
import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.Resources
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.Stack
import java.util.logging.Logger
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLEditorKit

class HelpWindow private constructor() : JFrame(Resources.getResource(Resources.TITLE_HELP)), HyperlinkListener, IMessageEventSource {
    private val textPane: JTextPane
    private val currentPages: Stack<URL> = Stack()
    private val backPages: Stack<URL> = Stack()
    private val forwardPages: Stack<URL> = Stack()
    private val backButton: JButton
    private val forwardButton: JButton
    private val homeButton: JButton
    private var homepage: URL? = null

    private var messageEvent: MessageEvent
    private var messageListener: IMessageListener? = null

    init {
        iconImage = Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP))?.image
        val jToolBar = JToolBar(JToolBar.HORIZONTAL)
        jToolBar.isFloatable = false
        homeButton = JButton(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP_HOME)))
        homeButton.addActionListener { goHome() }
        homeButton.toolTipText = "Table Of Contents"
        jToolBar.add(homeButton)

        backButton = JButton(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP_BACK)))
        backButton.addActionListener { goBack() }
        backButton.toolTipText = "Back"
        jToolBar.add(backButton)

        forwardButton = JButton(Resources.getSystemResourceIcon(Resources.getResource(Resources.ICON_HELP_FORWARD)))
        forwardButton.addActionListener { goForward() }
        forwardButton.toolTipText = "Forward"
        jToolBar.add(forwardButton)

        contentPane.add("North", jToolBar)

        textPane = JTextPane()
        textPane.isEditable = false
        textPane.setSize(400, 400)
        textPane.editorKit = HTMLEditorKit()
        textPane.addHyperlinkListener(this)
        val scroller = JScrollPane()
        scroller.viewport.add(textPane)
        contentPane.add(scroller)

        goHome()
        setSize(textPane.size)
        JFrame.setDefaultLookAndFeelDecorated(true)
        state = MAXIMIZED_HORIZ
        extendedState = MAXIMIZED_BOTH
        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                isVisible = false
            }
        })

        messageEvent = MessageEvent(this)
        pack()
    }

    fun fireMessagePosted(message: String) {
        messageEvent.message = message
        messageListener?.messagePosted(messageEvent)
    }

    override fun fireMessagePosted() {
        messageListener?.messagePosted(messageEvent)
    }

    override fun addMessageListener(messageListener: IMessageListener) {
        this.messageListener = messageListener
    }

    private fun goHome() {
        if (homepage == null) {
            go(HELP_INDEX)
            homepage = textPane.page
        } else {
            setURL(homepage!!)
        }
    }

    private fun goBack() {
        val url = backPages.pop()
        if (currentPages.isEmpty()) {
            forwardPages.push(url)
        } else {
            forwardPages.push(currentPages.pop())
        }
        setPage(url)
    }

    private fun goForward() {
        val url = forwardPages.pop()
        if (currentPages.isEmpty()) {
            backPages.push(url)
        } else {
            backPages.push(currentPages.pop())
        }
        setPage(url)
    }

    private fun setPage(url: URL) {
        try {
            textPane.page = url
            currentPages.push(url)
            setButtonState()
        } catch (e: IOException) {
            logger.severe(e.message)
            fireMessagePosted("Cannot set page " + e.message)
        }
    }

    private fun setURL(url: URL) {
        try {
            textPane.page = url
            if (!currentPages.isEmpty()) {
                backPages.push(currentPages.pop())
            }
            forwardPages.clear()
            currentPages.push(url)
        } catch (e: IOException) {
            logger.severe("Cannot set page $url")
            fireMessagePosted("Cannot set page $url")
        }
        setButtonState()
    }

    private fun setButtonState() {
        backButton.isEnabled = !backPages.isEmpty()
        forwardButton.isEnabled = !forwardPages.isEmpty()
        val index = textPane.page
        homeButton.isEnabled = index != null && index.path.indexOf(HELP_INDEX) == -1
    }

    private fun go(path: String?) {
        try {
            if (path != null) {
                if (path.indexOf(":") == -1) {
                    val file = File(path)
                    setURL(URL("file:///" + file.absolutePath))
                } else {
                    setURL(URL("file:///$path"))
                }
            }
        } catch (e: IOException) {
            logger.throwing(javaClass.name, "actionPerformed", e)
            fireMessagePosted("Cannot go to page - IOException occured: " + e.message)
        }
    }

    private fun go(url: URL) {
        if (url.protocol.startsWith("file")) {
            go(url.path)
        } else if (url.protocol.startsWith("http")) {
            // TODO: NEED TO IMPLEMENT
        } else {
            setURL(url)
        }
    }

    override fun hyperlinkUpdate(e: HyperlinkEvent) {
        if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            go(e.url)
        }
    }

    companion object {
        private val HELP_INDEX = FileHelper.suffixFileSeparator(
            System.getProperty(Resources.STED_HOME_PATH, "./")
        ) + Resources.getResource(Resources.HELP_INDEX)
        private val logger = Logger.getLogger(HelpWindow::class.java.name)

        @JvmStatic
        var instance: HelpWindow? = null
            get() {
                if (field == null) {
                    field = HelpWindow()
                    field!!.setSize(600, 800)
                }
                return field
            }
            private set
    }
}
