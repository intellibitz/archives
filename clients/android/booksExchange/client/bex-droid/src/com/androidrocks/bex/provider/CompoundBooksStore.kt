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

import android.content.Context
import android.net.Uri
import com.androidrocks.bex.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap

internal class CompoundBooksStore(context: Context, stores: HashMap<String, BooksStore>) :
    BooksStore(STORE_NAME, context.getString(R.string.bookstore_all), "") {
    
    private val mStores: Array<BooksStore> = stores.values.toTypedArray()

    override fun findBook(id: String?): Book? {
        var book: Book? = null

        for (store in mStores) {
            book = store.findBook(id)
            if (book != null) break
        }

        return book
    }

    override fun searchBooks(query: String?, listener: BookSearchListener): ArrayList<Book>? {
        val books = ArrayList<Book>(20)

        for (store in mStores) {
            val results = store.searchBooks(query, listener)
            if (results != null) books.addAll(results)
        }

        return books
    }

    override fun buildSearchBooksQuery(query: String?): Uri.Builder {
        throw UnsupportedOperationException()
    }

    override fun buildFindBookQuery(id: String?): Uri.Builder {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun parseResponse(`in`: InputStream, responseParser: ResponseParser) {
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun parseBook(parser: XmlPullParser, book: Book): Boolean {
        return false
    }

    @Throws(XmlPullParserException::class, IOException::class)
    override fun findNextBook(parser: XmlPullParser): Boolean {
        return false
    }

    companion object {
        private const val STORE_NAME = "all"
    }
}
