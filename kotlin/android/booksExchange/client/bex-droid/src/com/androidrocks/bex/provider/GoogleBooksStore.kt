/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2008 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidrocks.bex.provider

import android.net.Uri
import android.text.TextUtils
import android.util.Xml
import android.view.InflateException
import com.androidrocks.bex.util.CookieStore
import com.androidrocks.bex.util.ImageUtilities
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.ParseException
import java.text.SimpleDateFormat

internal class GoogleBooksStore(name: String, label: String) : BooksStore(name, label, API_REST_HOST) {
    private val mLoader: ImageLoader

    init {
        mLoader = GoogleImageLoader()
    }

    override fun createBook(): Book {
        return Book(name, mLoader)
    }

    override fun buildSearchBooksQuery(query: String?): Uri.Builder {
        val uri = buildGetMethod()
        uri.appendQueryParameter(API_ITEM_LOOKUP, query)
        uri.appendQueryParameter(PARAM_START_INDEX, VALUE_START_INDEX)
        uri.appendQueryParameter(PARAM_MAX_RESULTS, VALUE_MAX_RESULTS)
        return uri
    }

    override fun buildFindBookQuery(id: String?): Uri.Builder {
        val uri = buildGetMethod()
        uri.appendQueryParameter(API_ITEM_LOOKUP, id)
        return uri
    }

    @Throws(IOException::class)
    override fun parseResponse(`in`: InputStream, responseParser: ResponseParser) {
        val parser = Xml.newPullParser()
        try {
            parser.setInput(InputStreamReader(`in`))

            var type: Int
            while ((parser.next().also { type = it }) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                // Empty
            }

            if (type != XmlPullParser.START_TAG) {
                throw InflateException(
                    parser.positionDescription
                            + ": No start tag found!"
                )
            }

            val name = parser.name
            if (RESPONSE_TAG_FEED == name) {
                responseParser.parseResponse(parser)
            }
        } catch (e: XmlPullParserException) {
            val ioe = IOException("Could not parse the response")
            ioe.initCause(e)
            throw ioe
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun parseBook(parser: XmlPullParser, book: Book): Boolean {
        var type: Int
        var name: String
        var inEntry = false
        var isValid = false
        val depth = parser.depth

        if (RESPONSE_TAG_ENTRY == parser.name) {
            inEntry = true
            isValid = true
        }

        while (((parser.next().also { type = it }) != XmlPullParser.END_TAG || parser.depth > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue
            }

            name = parser.name

            if (RESPONSE_TAG_TOTAL_RESULTS == name) {
                if (parser.next() != XmlPullParser.TEXT || "1" != parser.text) {
                    throw IOException("Invalid request, 1 result is required")
                } else {
                    isValid = true
                }
            } else if (RESPONSE_TAG_IDENTIFIER == name) {
                if (parser.next() == XmlPullParser.TEXT) {
                    var value = parser.text
                    if (value.startsWith("ISBN:")) {
                        value = value.substring(5)
                        when (value.length) {
                            10 -> book.isbn = value
                            13 -> book.ean = value
                        }
                    } else {
                        if (book.internalIdNoPrefix == null) book.setInternalId(value.replace(':', '_'))
                    }
                }
            } else if (RESPONSE_TAG_ENTRY == name) {
                inEntry = true
            } else if (RESPONSE_TAG_TITLE == name && inEntry && TextUtils.isEmpty(book.title)) {
                if (parser.next() == XmlPullParser.TEXT) {
                    book.title = parser.text
                }
            } else if (RESPONSE_TAG_PUBLISHER == name) {
                if (parser.next() == XmlPullParser.TEXT) {
                    book.publisher = parser.text
                }
            } else if (RESPONSE_TAG_CREATOR == name) {
                if (parser.next() == XmlPullParser.TEXT) {
                    book.authors.add(parser.text)
                }
            } else if (RESPONSE_TAG_DESCRIPTION == name) {
                if (parser.next() == XmlPullParser.TEXT) {
                    book.descriptions.add(Description("", parser.text))
                }
            } else if (RESPONSE_TAG_LINK == name) {
                val rel = parser.getAttributeValue(null, RESPONSE_ATTR_REL)
                if (RESPONSE_VALUE_THUMBNAIL == rel) {
                    val url = parser.getAttributeValue(null, RESPONSE_ATTR_HREF)
                    book.images[ImageSize.THUMBNAIL] = url
                    book.images[ImageSize.TINY] = url.replace("zoom=5", "zoom=1")
                } else if (RESPONSE_VALUE_INFO == rel) {
                    book.detailsUrl = parser.getAttributeValue(null, RESPONSE_ATTR_HREF)
                }
            } else if (RESPONSE_TAG_FORMAT == name) {
                if (parser.next() == XmlPullParser.TEXT) {
                    val format = parser.text
                    if (format.endsWith(RESPONSE_VALUE_PAGES_SUFFIX)) {
                        book.pagesCount = format.substring(
                            0,
                            format.length - RESPONSE_VALUE_PAGES_SUFFIX.length
                        ).trim { it <= ' ' }.toInt()
                    }
                }
            } else if (RESPONSE_TAG_DATE == name) {
                if (parser.next() == XmlPullParser.TEXT) {
                    val format = SimpleDateFormat("yyyy-MM-dd")
                    try {
                        book.publicationDate = format.parse(parser.text)
                    } catch (e: ParseException) {
                        // Ignore
                    }
                }
            }
        }

        isValid = isValid && (book.isbn != null || book.ean != null)
        return isValid
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun findNextBook(parser: XmlPullParser): Boolean {
        if (RESPONSE_TAG_ENTRY == parser.name) {
            return true
        }

        var type: Int
        val depth = parser.depth

        while (((parser.next().also { type = it }) != XmlPullParser.END_TAG || parser.depth > depth) && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue
            }

            if (RESPONSE_TAG_ENTRY == parser.name) {
                return true
            }
        }

        return false
    }

    private class GoogleImageLoader : ImageLoader {
        override fun load(url: String): ImageUtilities.ExpiringBitmap {
            val cookie = CookieStore.get().getCookie(url)
            return ImageUtilities.load(url, cookie)
        }
    }

    companion object {
        private const val API_REST_HOST = "books.google.com"
        private const val API_REST_URL = "/books/feeds/volumes"

        private const val API_ITEM_LOOKUP = "q"

        private const val PARAM_MAX_RESULTS = "max-results"
        private const val PARAM_START_INDEX = "start-index"

        private const val VALUE_MAX_RESULTS = "10"
        private const val VALUE_START_INDEX = "1"

        private const val RESPONSE_TAG_FEED = "feed"
        private const val RESPONSE_TAG_ENTRY = "entry"
        private const val RESPONSE_TAG_TOTAL_RESULTS = "totalResults"
        private const val RESPONSE_TAG_IDENTIFIER = "identifier"
        private const val RESPONSE_TAG_TITLE = "title"
        private const val RESPONSE_TAG_PUBLISHER = "publisher"
        private const val RESPONSE_TAG_CREATOR = "creator"
        private const val RESPONSE_TAG_DESCRIPTION = "description"
        private const val RESPONSE_TAG_LINK = "link"
        private const val RESPONSE_TAG_FORMAT = "format"
        private const val RESPONSE_TAG_DATE = "date"

        private const val RESPONSE_ATTR_REL = "rel"
        private const val RESPONSE_ATTR_HREF = "href"

        private const val RESPONSE_VALUE_THUMBNAIL = "http://schemas.google.com/books/2008/thumbnail"
        private const val RESPONSE_VALUE_INFO = "http://schemas.google.com/books/2008/info"
        private const val RESPONSE_VALUE_PAGES_SUFFIX = "pages"

        /**
         * Builds an HTTP GET request for the specified API method. The returned request
         * contains the web service path, and the query parameter for the specified method.
         *
         * @return A Uri.Builder containing the GET path.
         */
        private fun buildGetMethod(): Uri.Builder {
            val builder = Uri.Builder()
            builder.path(API_REST_URL)
            return builder
        }
    }
}
