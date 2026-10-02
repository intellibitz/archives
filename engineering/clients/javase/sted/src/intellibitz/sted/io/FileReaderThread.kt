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
 * $Id:FileReaderThread.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FileReaderThread.kt $
 */

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
