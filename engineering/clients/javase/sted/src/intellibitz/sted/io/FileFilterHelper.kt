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
 * $Id:FileFilterHelper.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FileFilterHelper.kt $
 */

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
