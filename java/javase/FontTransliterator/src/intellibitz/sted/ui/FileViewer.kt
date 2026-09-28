package intellibitz.sted.ui

import intellibitz.sted.event.IThreadListener
import intellibitz.sted.event.ThreadEvent
import intellibitz.sted.io.FileReaderThread
import intellibitz.sted.util.Resources
import java.awt.Font
import java.io.File
import javax.swing.*

class FileViewer : JInternalFrame, IThreadListener {
    private var editorPane: JEditorPane? = null
    private var file: File? = null
    private var fileReaderThread: FileReaderThread? = null

    constructor() : super(Resources.EMPTY_STRING, false, false, false, false) {
        init()
    }

    constructor(icon: Icon?) : this() {
        frameIcon = icon
    }

    fun init() {
        border = BorderFactory.createEtchedBorder()
        editorPane = JEditorPane()
        editorPane!!.isEditable = false
        val scroller = JScrollPane()
        scroller.viewport.add(editorPane)
        val contentPane = contentPane
        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        contentPane.add(scroller)
        fileReaderThread = FileReaderThread()
        fileReaderThread!!.addThreadListener(this)
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
    }

    override fun setFont(font: Font) {
        super.setFont(font)
        editorPane?.font = font
    }

    fun setFileName(fileName: String) {
        title = fileName
        file = File(fileName)
    }

    fun addThreadListener(threadListener: IThreadListener) {
        fileReaderThread!!.addThreadListener(threadListener)
    }

    fun readFile() {
        fileReaderThread!!.file = file
        SwingUtilities.invokeLater(fileReaderThread)
    }

    override fun threadRunStarted(e: ThreadEvent) {}

    override fun threadRunning(e: ThreadEvent) {}

    override fun threadRunFailed(e: ThreadEvent) {}

    override fun threadRunFinished(e: ThreadEvent) {
        editorPane!!.text = e.eventSource.result.toString()
    }
}
