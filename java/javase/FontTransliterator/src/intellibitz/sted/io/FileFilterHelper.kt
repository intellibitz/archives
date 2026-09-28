package intellibitz.sted.io

import java.io.File
import java.util.HashMap
import javax.swing.filechooser.FileFilter

class FileFilterHelper : FileFilter {
    private var filters: MutableMap<String, FileFilterHelper>? = null
    private var descriptionText: String? = null
    private var fullDescription: String? = null
    private var useExtensionsInDescription = true

    constructor() {
        filters = HashMap()
    }

    constructor(extension: String) : this(extension, null)

    constructor(extension: String?, description: String?) : this() {
        if (extension != null) {
            addExtension(extension)
        }
        if (description != null) {
            setDescription(description)
        }
    }

    constructor(filtersArray: Array<String>) : this(filtersArray, null)

    constructor(filtersArray: Array<String>, description: String?) : this() {
        for (newVar in filtersArray) {
            addExtension(newVar)
        }
        if (description != null) {
            setDescription(description)
        }
    }

    override fun accept(f: File?): Boolean {
        if (f != null) {
            if (f.isDirectory) {
                return true
            }
            val extension = getExtension(f)
            if (extension != null && filters?.get(extension) != null) {
                return true
            }
        }
        return false
    }

    fun addExtension(extension: String) {
        if (filters == null) {
            filters = HashMap(5)
        }
        filters!![extension.lowercase()] = this
        fullDescription = null
    }

    override fun getDescription(): String {
        if (fullDescription == null) {
            if (descriptionText == null || isExtensionListInDescription()) {
                fullDescription = if (descriptionText == null) "(" else "$descriptionText ("
                val extensions = filters?.keys?.iterator()
                if (extensions != null && extensions.hasNext()) {
                    fullDescription += "." + extensions.next()
                    while (extensions.hasNext()) {
                        fullDescription += ", ." + extensions.next()
                    }
                }
                fullDescription += ")"
            } else {
                fullDescription = descriptionText
            }
        }
        return fullDescription!!
    }

    fun setDescription(description: String) {
        this.descriptionText = description
        fullDescription = null
    }

    fun setExtensionListInDescription(b: Boolean) {
        useExtensionsInDescription = b
        fullDescription = null
    }

    fun isExtensionListInDescription(): Boolean {
        return useExtensionsInDescription
    }

    companion object {
        fun getExtension(f: File?): String? {
            if (f != null) {
                val filename = f.name
                val i = filename.lastIndexOf('.')
                if (i > 0 && i < filename.length - 1) {
                    return filename.substring(i + 1).lowercase()
                }
            }
            return null
        }
    }
}
