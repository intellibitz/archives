package intellibitz.sted.util

import intellibitz.sted.io.FileFilterHelper
import java.awt.Component
import java.io.*
import java.util.logging.Logger
import javax.swing.JFileChooser
import javax.swing.JOptionPane

object FileHelper {
    private val logger = Logger.getLogger("intellibitz.sted.util.FileHelper")

    @JvmStatic
    fun openFont(parent: Component?): File? {
        return openFile("Please select Font location:", "ttf", "Fonts", parent)
    }

    @JvmStatic
    fun alertAndOpenFont(alert: String, parent: Component?): File? {
        JOptionPane.showMessageDialog(
            parent, alert, "Missing Resource",
            JOptionPane.WARNING_MESSAGE
        )
        return openFont(parent)
    }

    @JvmStatic
    fun openFile(
        title: String, extension: String,
        description: String,
        parent: Component?
    ): File? {
        val jFileChooser = JFileChooser(System.getProperty("user.dir"))
        jFileChooser.dialogTitle = title
        val fileFilterHelper = FileFilterHelper(extension, description)
        jFileChooser.fileFilter = fileFilterHelper
        val result = jFileChooser.showOpenDialog(parent)
        if (result == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.selectedFile
        }
        return null
    }

    @JvmStatic
    fun suffixFileSeparator(path: String): String {
        var newPath = path
        if (!newPath.endsWith(File.separator)) {
            newPath += File.separator
        }
        return newPath
    }

    @Throws(IOException::class)
    @JvmStatic
    fun fileCopy(source: File, dest: String) {
        val newFile = File(dest)
        val buffer = ByteArray(4096)
        val inputStream = FileInputStream(source)
        val outputStream = FileOutputStream(newFile)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            outputStream.write(buffer, 0, len)
        }
        inputStream.close()
        outputStream.close()
    }

    @Throws(FileNotFoundException::class)
    @JvmStatic
    fun getInputStream(file: File): InputStream? {
        logger.entering(Resources::class.java.name, "getInputStream", file)
        var inputStream: InputStream?
        if (file.isAbsolute) {
            logger.finest(
                "file is absolute.. using ClassLoader.getSystemResourceAsStream " +
                        file.absolutePath
            )
            inputStream = ClassLoader.getSystemResourceAsStream(file.absolutePath)
        } else {
            val resource = file.path.replace('\\', '/')
            logger.finest(
                "file is relative.. using Resources.class.getClass().getResourceAsStream with $resource"
            )
            inputStream = ClassLoader.getSystemResourceAsStream(resource)
        }
        if (inputStream == null) {
            inputStream = FileInputStream(file)
        }
        logger.exiting(
            Resources::class.java.name, "getInputStream",
            inputStream
        )
        return inputStream
    }

    @JvmStatic
    fun getSampleFontMapPaths(dir: String): Array<String> {
        val dirFile = File(dir)
        require(dirFile.isDirectory) { "$dir Is not a directory" }
        val files = dirFile.list { _, name -> name.endsWith("xml") }
        if (files != null && files.isNotEmpty()) {
            for (i in files.indices) {
                if (!files[i].contains(dir)) {
                    files[i] = dir + files[i]
                }
            }
        }
        return files ?: emptyArray()
    }
}
