package intellibitz.sted.launch

import intellibitz.sted.ui.AboutText
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.SplashWindow
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.Toolkit
import java.io.File
import java.util.logging.Logger
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import kotlin.system.exitProcess

class STEDGUI private constructor() {
    init {
        val splashWindow = SplashWindow(AboutText.instance!!)
        centerComponent(splashWindow)
        splashWindow.isVisible = true
        STEDLogManager.logmanager.addLogger(logger)
        splashWindow.progressValue = 10

        stedWindow = STEDWindow()
        stedWindow!!.addStatusListener(splashWindow)
        stedWindow!!.init()
        stedWindow!!.load()
        splashWindow.progressValue = 90
        stedWindow!!.isVisible = true
        val fileName = System.getProperty(Resources.FONTMAP_FILE)
        if (fileName != null && Resources.EMPTY_STRING != fileName) {
            stedWindow!!.desktop!!.openFontMap(File(fileName))
        } else {
            stedWindow!!.desktop!!.newFontMap()
        }
        splashWindow.progressValue = 100
        splashWindow.dispose()
    }

    companion object {
        private val logger = Logger.getLogger(STEDGUI::class.java.name)
        
        @JvmStatic
        var stedWindow: STEDWindow? = null
            private set

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                STEDGUI()
            } catch (e: Exception) {
                e.printStackTrace()
                logger.severe("Exception : " + e.message)
                logger.throwing("intellibitz.sted.launch.STEDGUI", "main", e)
                exitProcess(-1)
            }
        }

        @JvmStatic
        fun getSTEDWindow(component: Component): STEDWindow? {
            if (stedWindow != null) {
                return stedWindow
            }
            var parent: Component?
            var src = component
            do {
                parent = src.parent
                if (parent is STEDWindow) {
                    stedWindow = parent
                    return stedWindow
                }
                if (parent != null) {
                    src = parent
                }
            } while (parent != null)
            return null
        }

        @JvmStatic
        fun busy() {
            stedWindow?.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        }

        @JvmStatic
        fun relax() {
            stedWindow?.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
        }

        @JvmStatic
        fun centerComponent(component: Component) {
            val dimension = Toolkit.getDefaultToolkit().screenSize
            val size = component.size
            component.location = Point(
                (dimension.width - size.width) / 2,
                (dimension.height - size.height) / 2
            )
        }

        @Throws(
            ClassNotFoundException::class,
            InstantiationException::class,
            IllegalAccessException::class,
            UnsupportedLookAndFeelException::class
        )
        @JvmStatic
        fun updateUIWithLAF(lookAndFeel: String, iterator: Iterator<Component>) {
            UIManager.setLookAndFeel(lookAndFeel)
            while (iterator.hasNext()) {
                val component = iterator.next()
                SwingUtilities.updateComponentTreeUI(component)
            }
        }
    }
}
