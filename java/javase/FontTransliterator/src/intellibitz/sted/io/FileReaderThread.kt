package intellibitz.sted.io

import intellibitz.sted.event.IStatusEventSource
import intellibitz.sted.event.IStatusListener
import intellibitz.sted.event.StatusEvent
import intellibitz.sted.event.ThreadEventSourceBase
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.util.logging.Logger

class FileReaderThread : ThreadEventSourceBase, IStatusEventSource {
    var file: File? = null
    var statusListener: IStatusListener? = null
    var statusEvent: StatusEvent? = null

    companion object {
        private val logger = Logger.getLogger("intellibitz.sted.io.FileReaderThread")
    }

    constructor() : super() {
        statusEvent = StatusEvent(this)
    }

    constructor(file: File) : this() {
        this.file = file
    }

    override fun run() {
        fireThreadRunStarted()
        logger.entering(javaClass.name, "run")
        try {
            if (file == null) {
                message = "File is null"
                fireThreadRunFailed()
            } else {
                result = getFileContents(file!!)
                fireThreadRunFinished()
            }
        } catch (e: FileNotFoundException) {
            message = "Cannot Read File - File not found: ${e.message}"
            logger.throwing(javaClass.name, "run", e)
            fireThreadRunFailed()
        } catch (e: IOException) {
            message = "Cannot Read File - IOException: ${e.message}"
            logger.throwing(javaClass.name, "run", e)
            fireThreadRunFailed()
        }
        logger.exiting(javaClass.name, "run")
    }

    @Throws(IOException::class)
    private fun getFileContents(file: File): String {
        var bufferedReader: BufferedReader? = null
        var fileReader: FileReader? = null
        try {
            fileReader = FileReader(file)
            val sz = file.length().toInt()
            val cbuf = CharArray(sz)
            if (sz > 0) {
                bufferedReader = BufferedReader(fileReader, sz)

                var count = 0
                var len = 100
                if (len > sz - count) {
                    len = sz - count
                }
                var offset = count
                logger.finest("File Size: $sz")
                logger.finest("File Offset: $offset")
                logger.finest("File Length to be read: $len")
                while (bufferedReader.read(cbuf, offset, len) > 0) {
                    count += len
                    if (len > sz - count) {
                        len = sz - count
                    }
                    offset = count
                    progress = count
                    fireThreadRunning()
                }
            }
            return String(cbuf)
        } finally {
            if (bufferedReader != null) {
                fileReader?.close()
                bufferedReader.close()
            }
        }
    }

    override fun fireStatusPosted() {
        statusListener?.statusPosted(statusEvent!!)
    }

    override fun addStatusListener(statusListener: IStatusListener) {
        this.statusListener = statusListener
    }
}
