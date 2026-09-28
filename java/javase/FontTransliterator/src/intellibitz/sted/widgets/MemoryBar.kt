package intellibitz.sted.widgets

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JProgressBar

class MemoryBar : JProgressBar(0, 0), ActionListener {
    private var totalMemory: Long = 0
    private val runtime: Runtime
    private val meg: Long = 1000000

    init {
        isStringPainted = true
        runtime = Runtime.getRuntime()
        showMemoryStatus()
    }

    private fun showMemoryStatus() {
        totalMemory = runtime.totalMemory() / meg
        val free = runtime.freeMemory() / meg
        val current = totalMemory - free
        val value = "${current}M of ${totalMemory}M"
        string = value
        maximum = totalMemory.toInt()
        setValue(current.toInt())
    }

    override fun actionPerformed(e: ActionEvent) {
        showMemoryStatus()
    }
}
