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

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.androidrocks.bex.R
import java.util.HashMap
import java.util.regex.Pattern

class BooksProvider : ContentProvider() {

    private var mOpenHelper: SQLiteOpenHelper? = null

    private var mKeyPrefixes: Array<Pattern>? = null
    private var mKeySuffixes: Array<Pattern>? = null

    override fun onCreate(): Boolean {
        mOpenHelper = DatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {

        val qb = SQLiteQueryBuilder()

        when (URI_MATCHER.match(uri)) {
            SEARCH -> {
                qb.tables = "books"
                val query = uri.lastPathSegment
                if (!TextUtils.isEmpty(query)) {
                    qb.appendWhere(BooksStore.Book.AUTHORS + " LIKE ")
                    qb.appendWhereEscapeString("%$query%")
                    qb.appendWhere(" OR ")
                    qb.appendWhere(BooksStore.Book.TITLE + " LIKE ")
                    qb.appendWhereEscapeString("%$query%")
                }
                qb.setProjectionMap(SUGGESTION_PROJECTION_MAP)
            }
            BOOKS -> qb.tables = "books"
            BOOK_ID -> {
                qb.tables = "books"
                qb.appendWhere("_id=" + uri.pathSegments[1])
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        // If no sort order is specified use the default
        val orderBy = if (TextUtils.isEmpty(sortOrder)) {
            BooksStore.Book.DEFAULT_SORT_ORDER
        } else {
            sortOrder
        }

        val db = mOpenHelper!!.readableDatabase
        val c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy)
        c.setNotificationUri(context!!.contentResolver, uri)

        return c
    }

    override fun getType(uri: Uri): String? {
        when (URI_MATCHER.match(uri)) {
            BOOKS -> return "vnd.android.cursor.dir/vnd.org.curiouscreature.provider.shelves"
            BOOK_ID -> return "vnd.android.cursor.item/vnd.org.curiouscreature.provider.shelves"
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun insert(uri: Uri, initialValues: ContentValues?): Uri? {
        val values: ContentValues

        if (initialValues != null) {
            values = ContentValues(initialValues)
            values.put(
                BooksStore.Book.SORT_TITLE,
                keyFor(values.getAsString(BooksStore.Book.TITLE))
            )
        } else {
            values = ContentValues()
        }

        if (URI_MATCHER.match(uri) != BOOKS) {
            throw IllegalArgumentException("Unknown URI $uri")
        }

        val db = mOpenHelper!!.writableDatabase
        val rowId = db.insert("books", BooksStore.Book.TITLE, values)
        if (rowId > 0) {
            val insertUri = ContentUris.withAppendedId(BooksStore.Book.CONTENT_URI, rowId)
            context!!.contentResolver.notifyChange(uri, null)
            return insertUri
        }

        throw SQLException("Failed to insert row into $uri")
    }

    private fun keyFor(nameParam: String?): String {
        var name = nameParam ?: ""

        name = name.trim { it <= ' ' }.lowercase()

        if (mKeyPrefixes == null) {
            val resources = context!!.resources
            val keyPrefixes = resources.getStringArray(R.array.prefixes)
            val count = keyPrefixes.size

            mKeyPrefixes = Array(count) { i ->
                Pattern.compile("^" + keyPrefixes[i] + "\\s+")
            }
        }

        if (mKeySuffixes == null) {
            val resources = context!!.resources
            val keySuffixes = resources.getStringArray(R.array.suffixes)
            val count = keySuffixes.size

            mKeySuffixes = Array(count) { i ->
                Pattern.compile("\\s*" + keySuffixes[i] + "$")
            }
        }

        val prefixes = mKeyPrefixes!!
        for (prefix in prefixes) {
            val matcher = prefix.matcher(name)
            if (matcher.find()) {
                name = name.substring(matcher.end())
                break
            }
        }

        val suffixes = mKeySuffixes!!
        for (suffix in suffixes) {
            val matcher = suffix.matcher(name)
            if (matcher.find()) {
                name = name.substring(0, matcher.start())
                break
            }
        }

        return name
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = mOpenHelper!!.writableDatabase

        val count: Int
        when (URI_MATCHER.match(uri)) {
            BOOKS -> count = db.delete("books", selection, selectionArgs)
            BOOK_ID -> {
                val segment = uri.pathSegments[1]
                count = db.delete(
                    "books", BooksStore.Book._ID + "=" + segment +
                            if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "",
                    selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }

        context!!.contentResolver.notifyChange(uri, null)

        return count
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE books ("
                        + BooksStore.Book._ID + " INTEGER PRIMARY KEY, "
                        + BooksStore.Book.INTERNAL_ID + " TEXT, "
                        + BooksStore.Book.EAN + " TEXT, "
                        + BooksStore.Book.ISBN + " TEXT, "
                        + BooksStore.Book.TITLE + " TEXT, "
                        + BooksStore.Book.SORT_TITLE + " TEXT, "
                        + BooksStore.Book.AUTHORS + " TEXT, "
                        + BooksStore.Book.PUBLISHER + " TEXT, "
                        + BooksStore.Book.REVIEWS + " TEXT, "
                        + BooksStore.Book.PAGES + " INTEGER, "
                        + BooksStore.Book.PUBLICATION + " TEXT, "
                        + BooksStore.Book.LAST_MODIFIED + " INTEGER, "
                        + BooksStore.Book.DETAILS_URL + " TEXT, "
                        + BooksStore.Book.TINY_URL + " TEXT);"
            )
            db.execSQL("CREATE INDEX bookIndexTitle ON books(" + BooksStore.Book.SORT_TITLE + ");")
            db.execSQL("CREATE INDEX bookIndexAuthors ON books(" + BooksStore.Book.AUTHORS + ");")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
                        newVersion + ", which will destroy all old data"
            )

            db.execSQL("DROP TABLE IF EXISTS books")
            onCreate(db)
        }
    }

    companion object {
        private const val LOG_TAG = "BooksProvider"

        private const val DATABASE_NAME = "books.db"
        private const val DATABASE_VERSION = 1

        private const val SEARCH = 1
        private const val BOOKS = 2
        private const val BOOK_ID = 3

        private const val AUTHORITY = "com.androidrocks.bex.shelves"

        private val URI_MATCHER: UriMatcher
        init {
            URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH)
            URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH)
            URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH)
            URI_MATCHER.addURI(AUTHORITY, "books", BOOKS)
            URI_MATCHER.addURI(AUTHORITY, "books/#", BOOK_ID)
        }

        private val SUGGESTION_PROJECTION_MAP: HashMap<String, String>
        init {
            SUGGESTION_PROJECTION_MAP = HashMap()
            SUGGESTION_PROJECTION_MAP[SearchManager.SUGGEST_COLUMN_TEXT_1] = BooksStore.Book.TITLE + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1
            SUGGESTION_PROJECTION_MAP[SearchManager.SUGGEST_COLUMN_TEXT_2] = BooksStore.Book.AUTHORS + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2
            SUGGESTION_PROJECTION_MAP[SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID] = BooksStore.Book._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
            SUGGESTION_PROJECTION_MAP[BooksStore.Book._ID] = BooksStore.Book._ID
        }
    }
}
