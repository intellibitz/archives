package intellibitz.sted.launch

import intellibitz.sted.util.FileHelper
import intellibitz.sted.util.Resources
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.util.logging.LogManager

object STEDLogManager {
    private var _logManager: LogManager? = null

    @JvmStatic
    val logmanager: LogManager
        get() {
            if (_logManager == null) {
                _logManager = LogManager.getLogManager()
                try {
                    _logManager!!.readConfiguration(
                        BufferedInputStream(
                            FileHelper.getInputStream(
                                File(
                                    FileHelper.suffixFileSeparator(
                                        System.getProperty(Resources.LOG_PATH, "./log/")
                                    ) + Resources.getResource(Resources.LOG_CONFIG_NAME)
                                )
                            )
                        )
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
            return _logManager!!
        }
}
