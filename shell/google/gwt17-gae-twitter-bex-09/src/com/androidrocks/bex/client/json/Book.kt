/**
 *
 */
package com.androidrocks.bex.client.json

import java.io.Serializable
import java.util.ArrayList
import java.util.Date

/**
 * @author muthu
 */
class Book : Serializable {
    var id: String? = null
    var image: String? = null
    var store: String? = null
    var isbn: String? = null
    var ean: String? = null
    var pages: Int = 0
    var title: String? = null
    var detailsUrl: String? = null
    var publisher: String? = null
    var description: String? = null

    var publicationDate: Date? = null
    var lastModified: Date? = null

    var authors: MutableList<String> = ArrayList(1)
}
