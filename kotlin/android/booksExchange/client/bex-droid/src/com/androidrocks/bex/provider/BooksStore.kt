/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2008 Google Inc.
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

import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log
import com.androidrocks.bex.util.HttpManager
import com.androidrocks.bex.util.ImageUtilities
import com.androidrocks.bex.util.TextUtilities
import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.GregorianCalendar
import java.util.HashMap

/**
 * Utility class to load books from a books store.
 */
abstract class BooksStore(
    val name: String,
    val label: String,
    private val mHost: String
) {

    enum class ImageSize {
        // SWATCH,
        // SMALL,
        THUMBNAIL,
        TINY,
        // MEDIUM,
        // LARGE
    }

    class Description(val source: String, val content: String) {
        override fun toString(): String {
            // TODO: We should be storing reviews in a separate table
            return "<p class=\".source\">$source</p>\n<p class=\".content\">$content</p>"
        }
    }

    class Book : Parcelable, BaseColumns {
        var isbn: String? = null
        var ean: String? = null
        private var mInternalId: String? = null
        var images: MutableMap<ImageSize, String>
        var authors: MutableList<String>
        var pagesCount: Int = 0
        var title: String? = null
        var publicationDate: Date? = null
        var descriptions: MutableList<Description>
        var detailsUrl: String? = null
        var publisher: String? = null
        var lastModified: Calendar? = null

        private var mStorePrefix: String
        private var mLoader: ImageLoader? = null

        constructor() : this("", null)

        internal constructor(storePrefix: String, loader: ImageLoader?) {
            mStorePrefix = storePrefix
            mLoader = loader
            images = HashMap(6)
            authors = ArrayList(1)
            descriptions = ArrayList()
        }

        private constructor(`in`: Parcel) {
            isbn = `in`.readString()
            ean = `in`.readString()
            mInternalId = `in`.readString()
            title = `in`.readString()
            authors = ArrayList(1)
            `in`.readStringList(authors)
            
            mStorePrefix = ""
            images = HashMap(6)
            descriptions = ArrayList()
        }

        val internalId: String
            get() = mStorePrefix + mInternalId

        val internalIdNoPrefix: String?
            get() = mInternalId
            
        fun setInternalId(id: String?) {
            mInternalId = id
        }

        fun getImageUrl(size: ImageSize): String? {
            return images[size]
        }

        fun loadCover(size: ImageSize): Bitmap? {
            val url = images[size] ?: return null

            val expiring = if (mLoader == null) {
                ImageUtilities.load(url)
            } else {
                mLoader!!.load(url)
            }
            lastModified = expiring.lastModified

            return expiring.bitmap
        }

        val contentValues: ContentValues
            get() {
                val format = SimpleDateFormat("MMMM yyyy")
                val values = ContentValues()

                values.put(INTERNAL_ID, mStorePrefix + mInternalId)
                values.put(EAN, ean)
                values.put(ISBN, isbn)
                values.put(TITLE, title)
                values.put(AUTHORS, TextUtilities.join(authors, ", "))
                values.put(PUBLISHER, publisher)
                values.put(REVIEWS, TextUtilities.join(descriptions, "\n\n"))
                values.put(PAGES, pagesCount)
                if (lastModified != null) {
                    values.put(LAST_MODIFIED, lastModified!!.timeInMillis)
                }
                values.put(PUBLICATION, if (publicationDate != null) format.format(publicationDate!!) else "")
                values.put(DETAILS_URL, detailsUrl)
                values.put(TINY_URL, images[ImageSize.TINY])

                return values
            }

        override fun toString(): String {
            return "Book[ISBN=$isbn, EAN=$ean, IID=$mInternalId]"
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(isbn)
            dest.writeString(ean)
            dest.writeString(mInternalId)
            dest.writeString(title)
            dest.writeStringList(authors)
        }

        companion object {
            val CONTENT_URI: Uri = Uri.parse("content://shelves/books")

            const val DEFAULT_SORT_ORDER = "sort_title ASC"

            const val INTERNAL_ID = "internal_id"
            const val EAN = "ean"
            const val ISBN = "isbn"
            const val TITLE = "title"
            const val SORT_TITLE = "sort_title"
            const val AUTHORS = "authors"
            const val PUBLISHER = "publisher"
            const val REVIEWS = "reviews"
            const val PAGES = "pages"
            const val LAST_MODIFIED = "last_modified"
            const val PUBLICATION = "publication"
            const val DETAILS_URL = "details_url"
            const val TINY_URL = "tiny_url"
            const val _ID = BaseColumns._ID

            @JvmField
            val CREATOR: Parcelable.Creator<Book> = object : Parcelable.Creator<Book> {
                override fun createFromParcel(`in`: Parcel): Book {
                    return Book(`in`)
                }

                override fun newArray(size: Int): Array<Book?> {
                    return arrayOfNulls(size)
                }
            }

            fun fromCursor(c: Cursor): Book {
                val book = Book()

                book.mInternalId = c.getString(c.getColumnIndexOrThrow(INTERNAL_ID))
                book.ean = c.getString(c.getColumnIndexOrThrow(EAN))
                book.isbn = c.getString(c.getColumnIndexOrThrow(ISBN))
                book.title = c.getString(c.getColumnIndexOrThrow(TITLE))
                Collections.addAll(
                    book.authors,
                    *c.getString(c.getColumnIndexOrThrow(AUTHORS)).split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                )
                book.publisher = c.getString(c.getColumnIndexOrThrow(PUBLISHER))
                book.descriptions.add(
                    Description(
                        "",
                        c.getString(c.getColumnIndexOrThrow(REVIEWS))
                    )
                )
                book.pagesCount = c.getInt(c.getColumnIndexOrThrow(PAGES))

                val calendar = GregorianCalendar.getInstance()
                calendar.timeInMillis = c.getLong(c.getColumnIndexOrThrow(LAST_MODIFIED))
                book.lastModified = calendar

                val format = SimpleDateFormat("MMMM yyyy")
                try {
                    book.publicationDate = format.parse(
                        c.getString(
                            c.getColumnIndexOrThrow(PUBLICATION)
                        )
                    )
                } catch (e: ParseException) {
                    // Ignore
                }

                book.detailsUrl = c.getString(c.getColumnIndexOrThrow(DETAILS_URL))
                book.images[ImageSize.TINY] = c.getString(c.getColumnIndexOrThrow(TINY_URL))

                return book
            }
        }
    }

    /**
     * Finds the book with the specified id.
     *
     * @param id The id of the book to find (ISBN-10, ISBN-13, etc.)
     *
     * @return A Book instance if the book was found or null otherwise.
     */
    fun findBook(id: String?): Book? {
        val uri = buildFindBookQuery(id)

        val get = HttpGet(uri.build().toString())
        val book = createBook()
        val result = BooleanArray(1)

        try {
            executeRequest(HttpHost(mHost, 80, "http"), get, object : ResponseHandler {
                @Throws(IOException::class)
                override fun handleResponse(`in`: InputStream) {
                    parseResponse(`in`, object : ResponseParser {
                        @Throws(XmlPullParserException::class, IOException::class)
                        override fun parseResponse(parser: XmlPullParser) {
                            result[0] = parseBook(parser, book)
                        }
                    })
                }
            })

            if (TextUtils.isEmpty(book.ean) && id != null && id.length == 13) {
                book.ean = id
            } else if (TextUtils.isEmpty(book.isbn) && id != null && id.length == 10) {
                book.isbn = id
            }

            return if (result[0]) book else null
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not find the item with ISBN/EAN: $id")
        }

        return null
    }

    /**
     * Searchs for books that match the provided query.
     *
     * @param query The free form query used to search for books.
     *
     * @return A list of Book instances if query was successful or null otherwise.
     */
    fun searchBooks(query: String?, listener: BookSearchListener): ArrayList<Book>? {
        val uri = buildSearchBooksQuery(query)
        val get = HttpGet(uri.build().toString())
        val books = ArrayList<Book>(10)

        try {
            executeRequest(HttpHost(mHost, 80, "http"), get, object : ResponseHandler {
                @Throws(IOException::class)
                override fun handleResponse(`in`: InputStream) {
                    parseResponse(`in`, object : ResponseParser {
                        @Throws(XmlPullParserException::class, IOException::class)
                        override fun parseResponse(parser: XmlPullParser) {
                            parseBooks(parser, books, listener)
                        }
                    })
                }
            })

            return books
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not perform search with query: $query", e)
        }

        return null
    }

    /**
     * Constructs the query used to search for books. The query can be any combination
     * of keywords. The store is free to interpret the keywords in any way.
     *
     * @param query A free form text query to search for books.
     *
     * @return The Uri to the list of books matching the query.
     */
    internal abstract fun buildSearchBooksQuery(query: String?): Uri.Builder

    /**
     * Constructs the query used to find a book identified by its id. The unique
     * identifier should be either the EAN (ISBN-13) or ISBN (ISBN-10) of the book
     * to find.
     *
     * @param id The EAN or ISBN of the book to find.
     *
     * @return The Uri to the books details for this book store.
     */
    internal abstract fun buildFindBookQuery(id: String?): Uri.Builder

    /**
     * Parses a valid XML response from the specified input stream. This method must
     * invoke parse[ResponseParser.parseResponse] if
     * the XML response is valid, or throw an exception if it is not.
     *
     * @param in The input stream containing the response sent by the web service.
     * @param responseParser The parser to use when the response is valid.
     *
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    internal abstract fun parseResponse(`in`: InputStream, responseParser: ResponseParser)

    /**
     * Parses a book from the XML input stream.
     *
     * @param parser The XML parser to use to parse the book.
     * @param book The book object to put the parsed data in.
     *
     * @return True if the book could correctly be parsed, false otherwise.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    internal abstract fun parseBook(parser: XmlPullParser, book: Book): Boolean

    /**
     * Finds the next book entry in the XML input stream.
     *
     * @param parser The XML parser to use to parse the book.
     *
     * @return True if a book was found, false otherwise.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    internal abstract fun findNextBook(parser: XmlPullParser): Boolean

    /**
     * Creates an instance of [com.androidrocks.bex.provider.BooksStore.Book]
     * with this book store's name.
     *
     * @return A new instance of Book.
     */
    internal fun createBook(): Book {
        return Book(name, null)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseBooks(
        parser: XmlPullParser, books: ArrayList<Book>,
        listener: BookSearchListener
    ) {
        var type: Int
        while ((parser.next().also { type = it }) != XmlPullParser.END_TAG && type != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG) {
                continue
            }

            if (findNextBook(parser)) {
                val book = createBook()
                if (parseBook(parser, book)) {
                    books.add(book)
                    listener.onBookFound(book, books)
                }
            }
        }
    }

    /**
     * Executes an HTTP request on a REST web service. If the response is ok, the content
     * is sent to the specified response handler.
     *
     * @param host
     * @param get The GET request to executed.
     * @param handler The handler which will parse the response.
     *
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    private fun executeRequest(
        host: HttpHost, get: HttpGet, handler: ResponseHandler
    ) {
        var entity: HttpEntity? = null
        try {
            val response = HttpManager.execute(host, get)
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                entity = response.entity
                val `in` = entity.content
                handler.handleResponse(`in`)
            }
        } finally {
            if (entity != null) {
                entity.consumeContent()
            }
        }
    }

    /**
     * Response handler used with [BooksStore.executeRequest].
     * The handler is invoked when a response is sent by the server. The response is made
     * available as an input stream.
     */
    internal interface ResponseHandler {
        /**
         * Processes the responses sent by the HTTP server following a GET request.
         *
         * @param in The stream containing the server's response.
         *
         * @throws java.io.IOException
         */
        @Throws(IOException::class)
        fun handleResponse(`in`: InputStream)
    }

    /**
     * Response parser. When the request returns a valid response, this parser
     * is invoked to process the XML response.
     */
    internal interface ResponseParser {
        /**
         * Processes the XML response sent by the web service after a successful request.
         *
         * @param parser The parser containing the XML responses.
         *
         * @throws org.xmlpull.v1.XmlPullParserException
         * @throws java.io.IOException
         */
        @Throws(XmlPullParserException::class, IOException::class)
        fun parseResponse(parser: XmlPullParser)
    }

    /**
     * Interface used to load images with an expiring date. The expiring date is handled by
     * the image cache to check for updated images from time to time.
     */
    internal interface ImageLoader {
        /**
         * Load the specified URL as a Bitmap and associates an expiring date to it.
         *
         * @param url The URL of the image to load.
         *
         * @return The Bitmap decoded from the URL and an expiration date.
         */
        fun load(url: String): ImageUtilities.ExpiringBitmap
    }

    /**
     * Listener invoked by
     * [com.androidrocks.bex.provider.BooksStore.searchBooks].
     */
    interface BookSearchListener {
        /**
         * Invoked whenever a book was found by the search operation.
         *
         * @param book The book yield by the search query.
         * @param books The books found so far, including `book`.
         */
        fun onBookFound(book: Book, books: ArrayList<Book>)
    }

    companion object {
        const val LOG_TAG = "Shelves"
    }
}
