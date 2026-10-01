package org.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

abstract class LicenseTask : DefaultTask() {
    @Input
    val fileName = project.rootDir.toString() + "/license.txt"

    @TaskAction
    fun action() {
        // Read the license text
        val licenseText = File(fileName).readText()
        // Walk the directories looking for java files
        File(project.rootDir.toString()).walk().forEach {
            if (it.extension == "java") {
                // Read the source code
                var ins: InputStream = it.inputStream()
                var content = ins.readBytes().toString(Charset.defaultCharset())
                // Write the license and the source code to the file
                it.writeText(licenseText + content)
            }
        }
    }
}