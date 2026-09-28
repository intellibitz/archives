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
import android.content.res.XmlResourceParser
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import android.view.InflateException
import com.androidrocks.bex.R
import com.androidrocks.bex.util.Preferences
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.HashMap

object BookStoreFactory {
    private const val LOG_TAG = "ShelvesParser"
    const val DEFAULT_BOOK_STORE = "all"

    private const val TAG_STORES = "stores"
    private const val TAG_STORE = "store"

    private var sStores: HashMap<String, BooksStore>? = null

    fun get(context: Context): BooksStore {
        val preferences = context.getSharedPreferences(Preferences.NAME, 0)
        val name = preferences.getString(Preferences.KEY_BOOKSTORE, DEFAULT_BOOK_STORE)

        var store = get(context, name)
        if (store == null) {
            val iterator = sStores!!.entries.iterator()
            if (iterator.hasNext()) {
                store = iterator.next().value

                val editor = preferences.edit()
                editor.putString(Preferences.KEY_BOOKSTORE, store.name)
                editor.commit()

                return store
            }
        }

        return store!!
    }

    fun get(context: Context, store: String?): BooksStore? {
        if (sStores == null) {
            sStores = inflate(context, R.xml.bookstores)
        }

        val booksStore = sStores!![store] ?: throw IllegalStateException(
            "The store $store cannot not be found"
        )

        return booksStore
    }

    fun getStores(context: Context): Array<BooksStore> {
        if (sStores == null) {
            sStores = inflate(context, R.xml.bookstores)
        }
        return sStores!!.values.toTypedArray()
    }

    private fun inflate(context: Context, resource: Int): HashMap<String, BooksStore> {
        val stores = HashMap<String, BooksStore>()
        val parser = context.resources.getXml(resource)

        try {
            inflate(context, parser, stores)
        } catch (e: IOException) {
            Log.w(LOG_TAG, "An error occured while loading the bookstores", e)
        } catch (e: XmlPullParserException) {
            Log.w(LOG_TAG, "An error occured while loading the bookstores", e)
        } finally {
            parser.close()
        }

        if (stores.size >= 1) {
            val store = CompoundBooksStore(context, stores)
            stores[store.name] = store
        }

        return stores
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun inflate(
        context: Context, parser: XmlResourceParser,
        stores: HashMap<String, BooksStore>
    ) {

        val attrs = Xml.asAttributeSet(parser)

        var type: Int
        while ((parser.next().also { type = it }) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            // Empty
        }

        if (type != XmlPullParser.START_TAG) {
            throw InflateException(parser.positionDescription + ": No start tag found!")
        }

        val name = parser.name
        if (TAG_STORES == name) {
            parseStores(context, parser, attrs, stores)
        } else {
            throw InflateException(
                parser.positionDescription +
                        ": The root tag must be " + TAG_STORES
            )
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun parseStores(
        context: Context, parser: XmlResourceParser, attrs: AttributeSet,
        stores: HashMap<String, BooksStore>
    ) {

        val depth = parser.depth

        var type: Int
        while (((parser.next().also { type = it }) != XmlPullParser.END_TAG || parser.depth > depth) &&
            type != XmlPullParser.END_DOCUMENT
        ) {

            if (type != XmlPullParser.START_TAG) {
                continue
            }

            val name = parser.name
            if (TAG_STORE == name) {
                addStore(context, parser, attrs, stores)
            }
        }
    }

    private fun addStore(
        context: Context, parser: XmlResourceParser, attrs: AttributeSet,
        stores: HashMap<String, BooksStore>
    ) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.BookStore)

        val name = a.getString(R.styleable.BookStore_name)
        if (TextUtils.isEmpty(name)) {
            throw InflateException(
                parser.positionDescription +
                        ": A store must have a name"
            )
        }

        val label = a.getString(R.styleable.BookStore_label)
        if (TextUtils.isEmpty(label)) {
            throw InflateException(
                parser.positionDescription +
                        ": A store must have a label"
            )
        }

        val storeClass = a.getString(R.styleable.BookStore_storeClass)
        if (TextUtils.isEmpty(name)) {
            throw InflateException(
                parser.positionDescription +
                        ": A store must have a class"
            )
        }

        a.recycle()

        try {
            val klass = Class.forName(storeClass)
            val constructor = klass.getDeclaredConstructor(String::class.java, String::class.java)
            constructor.isAccessible = true

            val store = constructor.newInstance(name, label) as BooksStore
            stores[name!!] = store
        } catch (e: ClassNotFoundException) {
            // Ignore
        } catch (e: NoSuchMethodException) {
            throw InflateException(
                parser.positionDescription +
                        ": The book store " + storeClass + " does not have a matching constructor"
            )
        } catch (e: IllegalAccessException) {
            throw InflateException(
                parser.positionDescription + ": Could not create the "
                        + "book store", e
            )
        } catch (e: InvocationTargetException) {
            throw InflateException(
                parser.positionDescription + ": Could not create the "
                        + "book store", e
            )
        } catch (e: InstantiationException) {
            throw InflateException(
                parser.positionDescription + ": Could not create the "
                        + "book store", e
            )
        }
    }
}
