package intellibitz.sted

import intellibitz.sted.launch.STEDConsole
import intellibitz.sted.launch.STEDGUI
import intellibitz.sted.launch.STEDLogManager
import java.util.logging.Logger

object Main {
    private var logger: Logger? = null

    @JvmStatic
    fun main(args: Array<String>) {
        logger = Logger.getLogger("intellibitz.sted.Main")
        STEDLogManager.logmanager.addLogger(logger)
        if (args.isNotEmpty()) {
            val param1 = args[0]
            // launch Console
            if (param1.lowercase().startsWith("-c")) {
                logger?.info("Launching STED Console: ")
                val len = args.size
                val args1 = Array(len - 1) { "" }
                System.arraycopy(args, 1, args1, 0, len - 1)
                STEDConsole.main(args1)
            }
        } else {
            logger?.info("Launching STED GUI: ")
            // launch GUI
            STEDGUI.main(args)
        }
    }
}
