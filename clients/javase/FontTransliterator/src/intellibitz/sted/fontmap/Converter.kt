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
