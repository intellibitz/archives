package intellibitz.sted.launch

import intellibitz.sted.event.IThreadListener
import intellibitz.sted.event.ThreadEvent
import intellibitz.sted.fontmap.Converter
import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.io.FontMapReader
import intellibitz.sted.util.Resources
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.util.logging.Logger
import javax.xml.parsers.ParserConfigurationException
import kotlin.system.exitProcess

class STEDConsole(args: List<String>) : IThreadListener {
    private var fontMapName: String? = null
    private var inputFileName: String? = null
    private var outputFileName: String? = null
    private var reverse = false
    private var html = false

    init {
        logger = Logger.getLogger("intellibitz.sted.launch.STEDConsole")
        STEDLogManager.logmanager.addLogger(logger)
        loadArgs(args)
        if (fontMapName == null || Resources.EMPTY_STRING == fontMapName) {
            logger!!.info("Invalid FontMap: $fontMapName")
            printUsage()
            exitProcess(1)
        }
        if (inputFileName == null || Resources.EMPTY_STRING == inputFileName) {
            logger!!.info("Invalid Input File: $inputFileName")
            printUsage()
            exitProcess(2)
        }
        if (outputFileName == null || Resources.EMPTY_STRING == outputFileName) {
            logger!!.info("Invalid Output File: $outputFileName")
            printUsage()
            exitProcess(3)
        }
        val input = File(inputFileName!!)
        val output = File(outputFileName!!)
        // create console based FontMap.. no need to readFontMap fonts
        val fontMap = FontMap(File(fontMapName!!), true)
        try {
            FontMapReader.read(fontMap)
            val converter = Converter(fontMap, input, output)
            converter.setReverseTransliterate(reverse)
            converter.setHTMLAware(html)
            converter.addThreadListener(this)
            logger!!.info("Running Transliterator with the following options: ")
            logger!!.info("   FontMap: $fontMapName")
            logger!!.info("   Input: $inputFileName")
            logger!!.info("   Output: $outputFileName")
            logger!!.info("   Preserve Tags in Transliteration: $html")
            logger!!.info("   Reverse Transliteration: $reverse")
            converter.start()
        } catch (e: IOException) {
            e.printStackTrace()
            logger!!.severe("Exception : " + e.message)
            logger!!.throwing("intellibitz.sted.launch.STEDConsole", "Constructor", e)
            exitProcess(-1)
        } catch (e: SAXException) {
            e.printStackTrace()
            logger!!.severe("Exception : " + e.message)
            logger!!.throwing("intellibitz.sted.launch.STEDConsole", "Constructor", e)
            exitProcess(-1)
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
            logger!!.severe("Exception : " + e.message)
            logger!!.throwing("intellibitz.sted.launch.STEDConsole", "Constructor", e)
            exitProcess(-1)
        }
    }

    private fun loadArgs(args: List<String>) {
        for (param in args) {
            if (param.startsWith("-map=")) {
                fontMapName = param.substring(5)
            } else if (param.startsWith("-in=")) {
                inputFileName = param.substring(4)
            } else if (param.startsWith("-out=")) {
                outputFileName = param.substring(5)
            }
        }
        reverse = args.contains("-r") || args.contains("-R")
        html = args.contains("-p") || args.contains("-P")
        if (fontMapName == null) {
            fontMapName = System.getProperty(Resources.FONTMAP_FILE)
        }
        if (inputFileName == null) {
            inputFileName = System.getProperty(Resources.INPUT_FILE)
        }
        if (outputFileName == null) {
            outputFileName = System.getProperty(Resources.OUTPUT_FILE)
        }
    }

    override fun threadRunStarted(e: ThreadEvent) {}

    override fun threadRunning(e: ThreadEvent) {}

    override fun threadRunFailed(e: ThreadEvent) {
        logger!!.severe(e.eventSource.message.toString())
        exitProcess(-1)
    }

    override fun threadRunFinished(e: ThreadEvent) {
        logger!!.info(e.eventSource.message.toString())
        exitProcess(0)
    }

    companion object {
        private var logger: Logger? = null

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                STEDConsole(listOf(*args))
            } catch (e: Exception) {
                e.printStackTrace()
                logger?.severe("Exception : " + e.message)
                logger?.throwing("intellibitz.sted.launch.STEDConsole", "main", e)
                exitProcess(-1)
            }
        }

        private fun printUsage() {
            logger!!.info("STED Console Usage: ")
            logger!!.info(
                "   java -Dfontmap.file='<file>' -Dinput.file='<input>' -Doutput.file='<output>' intellibitz.sted.launch.STEDConsole"
            )
            logger!!.info(" -OR- ")
            logger!!.info("   java intellibitz.sted.launch.STEDConsole -map='<file>' -in='<input>' -out='<output>'")
        }
    }
}
