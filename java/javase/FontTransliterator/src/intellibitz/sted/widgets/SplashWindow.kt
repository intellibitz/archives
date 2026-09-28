package intellibitz.sted.widgets

import intellibitz.sted.event.IStatusListener
import intellibitz.sted.event.StatusEvent
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JProgressBar
import javax.swing.JWindow

class SplashWindow(component: Component) : JWindow(), IStatusListener {
    private val progress: JProgressBar

    init {
        val contentPane = contentPane
        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        contentPane.add(component)
        progress = JProgressBar(0, 100)
        progress.isStringPainted = true
        contentPane.add(progress)
        pack()
    }

    var progressValue: Int
        get() = progress.value
        set(percent) {
            progress.value = percent
        }

    // alias for compatibility
    fun setProgress(percent: Int) {
        progressValue = percent
    }

    override fun statusPosted(event: StatusEvent) {
        progressValue = Integer.valueOf(event.status)
    }
}
