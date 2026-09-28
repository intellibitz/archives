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
 * $Id:Converter.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/Converter.kt $
 */

package intellibitz.sted.fontmap

import intellibitz.sted.event.ThreadEventSourceBase
import intellibitz.sted.event.TransliterateEvent
import intellibitz.sted.util.FileHelper
import java.io.*
import java.util.logging.Logger

open class Converter : ThreadEventSourceBase {
    private var stopRequested: Boolean = false
    private var fontMap: FontMap? = null
    private var fileToConvert: File? = null
    private var convertedFile: File? = null
    private var initialized: Boolean = false
    var isSuccess: Boolean = false
    
    var transliterate: ITransliterate? = null

    companion object {
        private val logger: Logger = Logger.getLogger("intellibitz.sted.fontmap.Converter")
    }

    constructor() : super() {
        threadEvent = TransliterateEvent(this)
        initTransliterator()
    }

    constructor(fontMap: FontMap, input: File, output: File) : this() {
        init(fontMap, input, output)
    }

    private fun init(fontMap: FontMap, input: File, output: File) {
        fileToConvert = input
        convertedFile = output
        initialized = true
        isSuccess = false
        stopRequested = false
        setFontMap(fontMap)
    }

    private fun initTransliterator() {
        if (transliterate == null) {
            transliterate = DefaultTransliterator()
        }
    }

    override fun run() {
        fireThreadRunStarted()
        if (!initialized) {
            throw IllegalThreadStateException("Thread should be initialized.. Call init method before invoking run")
        }
        convertFile()
        initialized = false
        if (isSuccess) {
            fireThreadRunFinished()
        } else {
            fireThreadRunFailed()
        }
    }

    val isReady: Boolean
        get() = fontMap != null && !fontMap!!.entries.isEmpty()

    fun setHTMLAware(flag: Boolean) {
        transliterate?.setHTMLAware(flag)
    }

    private fun convertFile() {
        try {
            FileHelper.fileCopy(fileToConvert!!, fileToConvert!!.absolutePath + ".bakup")
        } catch (e: IOException) {
            message = "Unable to Backup input file: ${e.message}"
            isSuccess = false
            logger.severe("Unable to Backup input file: ${e.message}")
            logger.throwing(javaClass.name, "convertFile", e)
            return
        }
        
        val bufferedReader = try {
            BufferedReader(FileReader(fileToConvert!!))
        } catch (e: FileNotFoundException) {
            message = "File Not Found: ${e.message}"
            isSuccess = false
            logger.severe("Cannot Read - File Not Found: ${e.message}")
            logger.throwing(javaClass.name, "convertFile", e)
            return
        }
        
        val bufferedWriter = try {
            BufferedWriter(FileWriter(convertedFile!!))
        } catch (e: IOException) {
            message = "Cannot create Writer: ${e.message}"
            isSuccess = false
            logger.severe("Cannot Write - IOException: ${e.message}")
            logger.throwing(javaClass.name, "convertFile", e)
            return
        }
        
        try {
            var input: String? = bufferedReader.readLine()
            while (input != null) {
                if (stopRequested) {
                    break
                }
                bufferedWriter.write(transliterate!!.parseLine(input) ?: "")
                bufferedWriter.newLine()
                fireThreadRunning()
                input = bufferedReader.readLine()
            }
        } catch (e: IOException) {
            message = "IOException: ${e.message}"
            isSuccess = false
            logger.severe("IOException - Ceasing Conversion: ${e.message}")
            logger.throwing(javaClass.name, "convertFile", e)
            return
        } finally {
            try {
                bufferedReader.close()
                bufferedWriter.close()
            } catch (e: IOException) {
                message = "Cannot Close Reader/Writer - IOException: ${e.message}"
                isSuccess = false
                logger.severe("Cannot close File Streams - Ceasing Conversion: ${e.message}")
                logger.throwing(javaClass.name, "convertFile", e)
                return
            }
        }
        if (!stopRequested) {
            message = "Transliterate Done."
            isSuccess = true
        }
    }

    fun setReverseTransliterate(flag: Boolean) {
        transliterate?.setReverseTransliterate(flag)
    }

    fun setFontMap(fontMap: FontMap?) {
        this.fontMap = fontMap
        if (fontMap != null) {
            transliterate?.setEntries(fontMap.entries)
        }
    }

    @Synchronized
    fun setStopRequested(flag: Boolean) {
        stopRequested = flag
    }
}
