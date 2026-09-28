package intellibitz.sted.ui

import intellibitz.sted.event.FontMapChangeEvent
import intellibitz.sted.event.FontMapChangeListener
import intellibitz.sted.event.IStatusListener
import intellibitz.sted.event.StatusEvent
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.GCButton
import intellibitz.sted.widgets.MemoryBar
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.StringTokenizer
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class StatusPanel(private val stedWindow: STEDWindow) : JPanel(), TableModelListener, ListSelectionListener, FontMapChangeListener, IStatusListener {
    var progressBar: JProgressBar
        private set
    private var counter: JLabel
    private var flag: JLabel
    private var lock: JLabel
    private var status: JLabel
    private val memoryBar: MemoryBar

    init {
        border = BorderFactory.createLineBorder(foreground)
        val gridBagLayout = GridBagLayout()
        layout = gridBagLayout
        val gridBagConstraints = GridBagConstraints()
        gridBagConstraints.fill = GridBagConstraints.BOTH
        gridBagConstraints.weighty = 1.0
        gridBagConstraints.weightx = 1.0
        gridBagConstraints.gridheight = 1
        gridBagConstraints.gridwidth = 1
        gridBagConstraints.ipadx = 1
        
        status = JLabel()
        gridBagLayout.setConstraints(status, gridBagConstraints)
        add(status)
        
        progressBar = JProgressBar(JProgressBar.HORIZONTAL)
        gridBagConstraints.weightx = 0.0
        gridBagLayout.setConstraints(progressBar, gridBagConstraints)
        add(progressBar)
        
        val panel1 = JPanel()
        panel1.border = BorderFactory.createLineBorder(foreground)
        counter = JLabel()
        initCounter()
        panel1.add(counter)
        gridBagLayout.setConstraints(panel1, gridBagConstraints)
        add(panel1)
        
        val panel2 = JPanel()
        panel2.border = BorderFactory.createLineBorder(foreground)
        flag = JLabel()
        panel2.add(flag)
        gridBagLayout.setConstraints(panel2, gridBagConstraints)
        add(panel2)
        
        val panel3 = JPanel()
        panel3.border = BorderFactory.createLineBorder(foreground)
        lock = JLabel()
        panel3.add(lock)
        gridBagLayout.setConstraints(panel3, gridBagConstraints)
        add(panel3)
        
        memoryBar = MemoryBar()
        gridBagLayout.setConstraints(memoryBar, gridBagConstraints)
        add(memoryBar)
        
        val imageIcon = Resources.getSystemResourceIcon(Resources.getSetting(Resources.ICON_GC))
        val gcButton = GCButton(
            imageIcon!!,
            Resources.getSystemResourceIcon(Resources.getSetting(Resources.ICON_GC2))!!
        )
        gridBagLayout.setConstraints(gcButton, gridBagConstraints)
        add(gcButton)
    }

    fun runMemoryBar() {
        val timer = Timer(2000, memoryBar)
        timer.start()
    }

    fun clear() {
        clearStatus()
        clearProgress()
        initCounter()
        setCleanFlag()
        setLockFlag(true)
    }

    fun setStatus(msg: String?) {
        status.text = Resources.SPACE + (msg ?: "")
    }

    private fun clearStatus() {
        status.text = Resources.EMPTY_STRING
    }

    fun clearProgress() {
        progressBar.isStringPainted = false
        progressBar.minimum = 0
        progressBar.maximum = 0
        progressBar.value = 0
    }

    private fun initCounter() {
        counter.text = COUNTER_INIT
    }

    private fun setTotalCount(total: Int) {
        if (total < 1) {
            initCounter()
        } else {
            val stringTokenizer = StringTokenizer(counter.text, Resources.COLON)
            val stringBuffer = StringBuffer()
            if (stringTokenizer.hasMoreTokens()) {
                stringBuffer.append(stringTokenizer.nextToken())
            } else {
                stringBuffer.append(0.toString())
            }
            stringBuffer.append(Resources.COLON)
            stringBuffer.append(total.toString())
            counter.text = stringBuffer.toString()
        }
    }

    private fun setCurrentCount(curr: Int) {
        if (curr >= 0) {
            val stringBuffer = StringBuffer()
            stringBuffer.append((curr + 1).toString())
            stringBuffer.append(Resources.COLON)
            val stringTokenizer = StringTokenizer(counter.text, Resources.COLON)
            if (stringTokenizer.hasMoreTokens()) {
                stringTokenizer.nextToken()
                stringBuffer.append(stringTokenizer.nextToken())
            }
            counter.text = stringBuffer.toString()
        }
    }

    fun setNeatness(fontMap: FontMap) {
        clearProgress()
        setLockFlag(!fontMap.isFileWritable())
        if (fontMap.isDirty) {
            setDirtyFlag()
        } else {
            setCleanFlag()
        }
    }

    private fun setCleanFlag() {
        flag.icon = Resources.getCleanIcon()
    }

    private fun setDirtyFlag() {
        flag.icon = Resources.getDirtyIcon()
    }

    fun setLockFlag(flag: Boolean) {
        if (flag) {
            lock.icon = Resources.getLockIcon()
        } else {
            lock.icon = Resources.getUnLockIcon()
        }
    }

    override fun tableChanged(e: TableModelEvent) {
        val tableModel = e.source as TableModel
        setTotalCount(tableModel.rowCount)
        stedWindow.desktop?.fontMap?.let { setNeatness(it) }
    }

    override fun valueChanged(e: ListSelectionEvent) {
        val listSelectionModel = e.source as ListSelectionModel
        setCurrentCount(listSelectionModel.maxSelectionIndex)
    }

    override fun stateChanged(e: FontMapChangeEvent) {
        e.fontMap?.let { setNeatness(it) }
    }

    override fun statusPosted(event: StatusEvent) {
        setStatus(event.status)
    }

    companion object {
        private const val COUNTER_INIT = "0:0"
    }
}
