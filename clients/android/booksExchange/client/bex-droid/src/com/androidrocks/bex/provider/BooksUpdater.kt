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
import android.content.ContentValues
import android.content.Context
import android.os.Process
import android.util.Log
import com.androidrocks.bex.util.HttpManager
import com.androidrocks.bex.util.ImageUtilities
import com.androidrocks.bex.util.ImportUtilities
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.HashMap
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class BooksUpdater(context: Context) : Runnable {

    private val mQueue: BlockingQueue<String> = ArrayBlockingQueue(12)
    private val mResolver: ContentResolver = context.contentResolver
    private val mLastModifiedFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
    private val mSelection: String = BooksStore.Book._ID + "=?"
    private val mArguments = arrayOfNulls<String>(1)
    private val mValues = ContentValues()

    private var mThread: Thread? = null
    @Volatile
    private var mStopped = false

    fun start() {
        if (mThread == null) {
            mStopped = false
            mThread = Thread(this, "BooksUpdater")
            mThread!!.start()
        }
    }

    fun stop() {
        if (mThread != null) {
            mStopped = true
            mThread!!.interrupt()
            mThread = null
        }
    }

    fun offer(vararg books: String?) {
        for (bookId in books) {
            if (bookId != null) mQueue.offer(bookId)
        }
    }

    fun clear() {
        mQueue.clear()
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        val expiring = ImageUtilities.ExpiringBitmap()

        while (!mStopped) {
            try {
                val bookId = mQueue.take()

                val lastCheck = sLastChecks[bookId]
                if (lastCheck != null && lastCheck + ONE_DAY >= System.currentTimeMillis()) {
                    continue
                }
                sLastChecks[bookId] = System.currentTimeMillis()

                val book = BooksManager.findBook(mResolver, bookId)
                if (book == null || book.lastModified == null ||
                    book.getImageUrl(BooksStore.ImageSize.TINY) == null
                ) {
                    continue
                }

                if (bookCoverUpdated(book, expiring) && expiring.lastModified != null) {
                    ImageUtilities.deleteCachedCover(bookId)
                    val bitmap = book.loadCover(BooksStore.ImageSize.TINY)
                    ImportUtilities.addBookCoverToCache(book, bitmap)

                    mValues.put(
                        BooksStore.Book.LAST_MODIFIED,
                        expiring.lastModified!!.timeInMillis
                    )
                    mArguments[0] = bookId
                    mResolver.update(BooksStore.Book.CONTENT_URI, mValues, mSelection, mArguments)
                }

                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
    }

    private fun bookCoverUpdated(book: BooksStore.Book, expiring: ImageUtilities.ExpiringBitmap): Boolean {
        expiring.lastModified = null
        val get = HttpGet(book.getImageUrl(BooksStore.ImageSize.TINY))

        var entity: HttpEntity? = null
        try {
            val response = HttpManager.execute(get)
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                entity = response.entity

                val header = response.getFirstHeader("Last-Modified")
                if (header != null) {
                    val calendar = GregorianCalendar.getInstance()
                    try {
                        calendar.time = mLastModifiedFormat.parse(header.value)
                        expiring.lastModified = calendar
                        return calendar.after(book.lastModified)
                    } catch (e: ParseException) {
                        return false
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not check modification date for $book", e)
        } finally {
            if (entity != null) {
                try {
                    entity.consumeContent()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "Could not check modification date for $book", e)
                }
            }
        }

        return false
    }

    companion object {
        private const val LOG_TAG = "BooksUpdater"

        private const val ONE_DAY = (24 * 60 * 60 * 1000).toLong()

        private val sLastChecks = HashMap<String, Long>()
    }
}
