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

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import com.androidrocks.bex.util.ImageUtilities
import com.androidrocks.bex.util.ImportUtilities

object BooksManager {
    const val BOOK_COVER_WIDTH = 100
    const val BOOK_COVER_HEIGHT = 120

    private val sBookIdSelection: String
    private val sBookSelection: String

    private val sArguments1 = arrayOfNulls<String>(1)
    private val sArguments3 = arrayOfNulls<String>(3)

    private val PROJECTION_ID_IID = arrayOf(
        BooksStore.Book._ID, BooksStore.Book.INTERNAL_ID
    )
    private val PROJECTION_ID = arrayOf(BooksStore.Book._ID)

    init {
        var selection = StringBuilder()
        selection.append(BooksStore.Book.INTERNAL_ID)
        selection.append("=?")
        sBookIdSelection = selection.toString()

        selection = StringBuilder()
        selection.append(sBookIdSelection)
        selection.append(" OR ")
        selection.append(BooksStore.Book.EAN)
        selection.append("=? OR ")
        selection.append(BooksStore.Book.ISBN)
        selection.append("=?")
        sBookSelection = selection.toString()
    }

    fun findBookId(contentResolver: ContentResolver, id: String?): String? {
        var internalId: String? = null
        var c: Cursor? = null

        try {
            val arguments3 = sArguments3
            arguments3[2] = id
            arguments3[1] = arguments3[2]
            arguments3[0] = arguments3[1]
            c = contentResolver.query(
                BooksStore.Book.CONTENT_URI, PROJECTION_ID_IID,
                sBookSelection, arguments3, null
            )
            if (c != null && c.count > 0) {
                if (c.moveToFirst()) {
                    internalId = c.getString(c.getColumnIndexOrThrow(BooksStore.Book.INTERNAL_ID))
                }
            }
        } finally {
            c?.close()
        }

        return internalId
    }

    fun bookExists(contentResolver: ContentResolver, id: String?): Boolean {
        var exists = false
        var c: Cursor? = null

        try {
            val arguments3 = sArguments3
            arguments3[2] = id
            arguments3[1] = arguments3[2]
            arguments3[0] = arguments3[1]
            c = contentResolver.query(
                BooksStore.Book.CONTENT_URI, PROJECTION_ID, sBookSelection,
                arguments3, null
            )
            exists = c != null && c.count > 0
        } finally {
            c?.close()
        }

        return exists
    }

    fun loadAndAddBook(
        resolver: ContentResolver, id: String?,
        booksStore: BooksStore
    ): BooksStore.Book? {

        val book = booksStore.findBook(id)
        if (book != null) {
            var bitmap = book.loadCover(BooksStore.ImageSize.TINY)
            if (bitmap != null) {
                bitmap = ImageUtilities.createBookCover(bitmap, BOOK_COVER_WIDTH, BOOK_COVER_HEIGHT)
                ImportUtilities.addBookCoverToCache(book, bitmap)
            }

            val uri = resolver.insert(BooksStore.Book.CONTENT_URI, book.contentValues)
            if (uri != null) {
                return book
            }
        }

        return null
    }

    fun deleteBook(contentResolver: ContentResolver, bookId: String): Boolean {
        val arguments1 = sArguments1
        arguments1[0] = bookId
        val count = contentResolver.delete(
            BooksStore.Book.CONTENT_URI,
            sBookIdSelection, arguments1
        )
        ImageUtilities.deleteCachedCover(bookId)
        return count > 0
    }

    fun findBook(contentResolver: ContentResolver, id: String?): BooksStore.Book? {
        var book: BooksStore.Book? = null
        var c: Cursor? = null

        try {
            sArguments1[0] = id
            c = contentResolver.query(
                BooksStore.Book.CONTENT_URI, null, sBookIdSelection,
                sArguments1, null
            )
            if (c != null && c.count > 0) {
                if (c.moveToFirst()) {
                    book = BooksStore.Book.fromCursor(c)
                }
            }
        } finally {
            c?.close()
        }

        return book
    }

    fun findBook(contentResolver: ContentResolver, data: Uri?): BooksStore.Book? {
        var book: BooksStore.Book? = null
        var c: Cursor? = null

        try {
            c = contentResolver.query(data!!, null, null, null, null)
            if (c != null && c.count > 0) {
                if (c.moveToFirst()) {
                    book = BooksStore.Book.fromCursor(c)
                }
            }
        } finally {
            c?.close()
        }

        return book
    }
}
