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

package com.androidrocks.bex.util

import android.graphics.Bitmap
import com.androidrocks.bex.provider.BooksStore
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList

object ImportUtilities {
    private const val CACHE_DIRECTORY = "shelves/books"
    private const val IMPORT_FILE = "library.txt"

    @JvmStatic
    val cacheDirectory: File
        get() = IOUtilities.getExternalFile(CACHE_DIRECTORY)

    @Throws(IOException::class)
    @JvmStatic
    fun loadItems(): ArrayList<String> {
        val list = ArrayList<String>()

        val importFile = IOUtilities.getExternalFile(IMPORT_FILE)
        if (!importFile.exists()) return list

        var `in`: BufferedReader? = null
        try {
            `in` = BufferedReader(
                InputStreamReader(FileInputStream(importFile)),
                IOUtilities.IO_BUFFER_SIZE
            )

            var line: String?

            // Read the CSV headers
            `in`.readLine()

            while ((`in`.readLine().also { line = it }) != null) {
                val index = line!!.indexOf('\t')
                val length = line!!.length

                // Only one field, we grab the entire line
                if (index == -1 && length > 0) {
                    list.add(line!!)
                    // Only one field, the first one is empty
                } else if (index != length - 1) {
                    list.add(line!!.substring(index + 1))
                    // We have two fields or the second one is empty
                } else {
                    list.add(line!!.substring(0, index))
                }
            }
        } finally {
            IOUtilities.closeStream(`in`)
        }

        return list
    }

    @JvmStatic
    fun addBookCoverToCache(book: BooksStore.Book, bitmap: Bitmap): Boolean {
        val cacheDirectory: File
        try {
            cacheDirectory = ensureCache()
        } catch (e: IOException) {
            return false
        }

        val coverFile = File(cacheDirectory, book.internalId)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(coverFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: FileNotFoundException) {
            return false
        } finally {
            IOUtilities.closeStream(out)
        }

        return true
    }

    @Throws(IOException::class)
    private fun ensureCache(): File {
        val cacheDirectory = cacheDirectory
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
            File(cacheDirectory, ".nomedia").createNewFile()
        }
        return cacheDirectory
    }
}
