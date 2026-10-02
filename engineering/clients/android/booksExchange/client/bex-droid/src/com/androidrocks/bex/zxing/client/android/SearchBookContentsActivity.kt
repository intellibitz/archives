/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Zxing project to suit Books-Exchange requirements.
 * Original source from Zxing - http://code.google.com/p/zxing/
 */

/*
 * Copyright (C) 2008 ZXing authors
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

package com.androidrocks.bex.zxing.client.android

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.androidrocks.bex.R
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI

@Suppress("DEPRECATION")
class SearchBookContentsActivity : Activity() {

    private var mNetworkThread: NetworkThread? = null
    private var mISBN: String? = null
    private var mQueryTextView: EditText? = null
    private var mQueryButton: Button? = null
    private var mResultListView: ListView? = null
    private var mHeaderView: TextView? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Make sure that expired cookies are removed on launch.
        CookieSyncManager.createInstance(this)
        CookieManager.getInstance().removeExpiredCookie()

        val intent = intent
        if (intent == null || (!intent.action.equals(Intents.SearchBookContents.ACTION) &&
                    !intent.action.equals(Intents.SearchBookContents.DEPRECATED_ACTION))
        ) {
            finish()
            return
        }

        mISBN = intent.getStringExtra(Intents.SearchBookContents.ISBN)
        title = getString(R.string.sbc_name) + ": ISBN " + mISBN

        setContentView(R.layout.search_book_contents)
        mQueryTextView = findViewById<View>(R.id.query_text_view) as EditText

        val initialQuery = intent.getStringExtra(Intents.SearchBookContents.QUERY)
        if (initialQuery != null && initialQuery.length > 0) {
            // Populate the search box but don't trigger the search
            mQueryTextView!!.setText(initialQuery)
        }
        mQueryTextView!!.setOnKeyListener(mKeyListener)

        mQueryButton = findViewById<View>(R.id.query_button) as Button
        mQueryButton!!.setOnClickListener(mButtonListener)

        mResultListView = findViewById<View>(R.id.result_list_view) as ListView
        val factory = LayoutInflater.from(this)
        mHeaderView = factory.inflate(
            R.layout.search_book_contents_header,
            mResultListView, false
        ) as TextView
        mResultListView!!.addHeaderView(mHeaderView)
    }

    override fun onResume() {
        super.onResume()
        mQueryTextView!!.selectAll()
    }

    override fun onConfigurationChanged(config: Configuration) {
        // Do nothing, this is to prevent the activity from being restarted when the keyboard opens.
        super.onConfigurationChanged(config)
    }

    val mHandler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            when (message.what) {
                R.id.search_book_contents_succeeded -> {
                    handleSearchResults(message.obj as JSONObject)
                    resetForNewQuery()
                }

                R.id.search_book_contents_failed -> {
                    resetForNewQuery()
                    mHeaderView!!.setText(R.string.msg_sbc_failed)
                }
            }
        }
    }

    private fun resetForNewQuery() {
        mNetworkThread = null
        mQueryTextView!!.isEnabled = true
        mQueryTextView!!.selectAll()
        mQueryButton!!.isEnabled = true
    }

    private val mButtonListener = View.OnClickListener { launchSearch() }

    private val mKeyListener = View.OnKeyListener { view, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            launchSearch()
            return@OnKeyListener true
        }
        false
    }

    private fun launchSearch() {
        if (mNetworkThread == null) {
            val query = mQueryTextView!!.text.toString()
            if (query != null && query.length > 0) {
                mNetworkThread = NetworkThread(mISBN, query, mHandler)
                mNetworkThread!!.start()
                mHeaderView!!.setText(R.string.msg_sbc_searching_book)
                mResultListView!!.adapter = null
                mQueryTextView!!.isEnabled = false
                mQueryButton!!.isEnabled = false
            }
        }
    }

    // Currently there is no way to distinguish between a query which had no results and a book
    // which is not searchable - both return zero results.
    private fun handleSearchResults(json: JSONObject) {
        try {
            val count = json.getInt("number_of_results")
            mHeaderView!!.text = "Found " + (if (count == 1) "1 result" else "$count results")
            if (count > 0) {
                val results = json.getJSONArray("search_results")
                SearchBookContentsResult.query = mQueryTextView!!.text.toString()
                val items: MutableList<SearchBookContentsResult?> = ArrayList<SearchBookContentsResult?>(count)
                for (x in 0 until count) {
                    items.add(parseResult(results.getJSONObject(x)))
                }
                mResultListView!!.adapter = SearchBookContentsAdapter(this, items)
            } else {
                val searchable = json.optString("searchable")
                if ("false" == searchable) {
                    mHeaderView!!.setText(R.string.msg_sbc_book_not_searchable)
                }
                mResultListView!!.adapter = null
            }
        } catch (e: JSONException) {
            Log.e(TAG, e.toString())
            mResultListView!!.adapter = null
            mHeaderView!!.setText(R.string.msg_sbc_failed)
        }
    }

    // Available fields: page_number, page_id, page_url, snippet_text
    private fun parseResult(json: JSONObject): SearchBookContentsResult {
        try {
            var pageNumber = json.getString("page_number")
            if (pageNumber.length > 0) {
                pageNumber = getString(R.string.msg_sbc_page) + ' ' + pageNumber
            } else {
                // This can happen for text on the jacket, and possibly other reasons.
                pageNumber = getString(R.string.msg_sbc_unknown_page)
            }

            // Remove all HTML tags and encoded characters. Ideally the server would do this.
            var snippet = json.optString("snippet_text")
            var valid = true
            if (snippet.length > 0) {
                snippet = snippet.replace("\\<.*?\\>".toRegex(), "")
                snippet = snippet.replace("&lt;".toRegex(), "<")
                snippet = snippet.replace("&gt;".toRegex(), ">")
                snippet = snippet.replace("&#39;".toRegex(), "'")
                snippet = snippet.replace("&quot;".toRegex(), "\"")
            } else {
                snippet = '('.toString() + getString(R.string.msg_sbc_snippet_unavailable) + ')'
                valid = false
            }
            return SearchBookContentsResult(pageNumber, snippet, valid)
        } catch (e: JSONException) {
            // Never seen in the wild, just being complete.
            return SearchBookContentsResult(getString(R.string.msg_sbc_no_page_returned), "", false)
        }
    }

    private class NetworkThread(
        private val mISBN: String?,
        private val mQuery: String,
        private val mHandler: Handler
    ) : Thread() {
        override fun run() {
            var client: AndroidHttpClient? = null
            try {
                // These return a JSON result which describes if and where the query was found. This API may
                // break or disappear at any time in the future. Since this is an API call rather than a
                // website, we don't use LocaleManager to change the TLD.
                val uri = URI(
                    "http", null, "www.google.com", -1, "/books", "vid=isbn" + mISBN +
                            "&jscmd=SearchWithinVolume2&q=" + mQuery, null
                )
                val get = HttpGet(uri)
                get.setHeader("cookie", getCookie(uri.toString()))
                client = AndroidHttpClient.newInstance(USER_AGENT)
                val response = client.execute(get)
                if (response.statusLine.statusCode == 200) {
                    val entity = response.entity
                    val jsonHolder = ByteArrayOutputStream()
                    entity.writeTo(jsonHolder)
                    jsonHolder.flush()
                    val json = JSONObject(jsonHolder.toString(getEncoding(entity)))
                    jsonHolder.close()

                    val message = Message.obtain(mHandler, R.id.search_book_contents_succeeded)
                    message.obj = json
                    message.sendToTarget()
                } else {
                    Log.e(TAG, "HTTP returned " + response.statusLine.statusCode + " for " + uri)
                    val message = Message.obtain(mHandler, R.id.search_book_contents_failed)
                    message.sendToTarget()
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                val message = Message.obtain(mHandler, R.id.search_book_contents_failed)
                message.sendToTarget()
            } finally {
                client?.close()
            }
        }

        // Book Search requires a cookie to work, which we store persistently. If the cookie does
        // not exist, this could be the first search or it has expired. Either way, do a quick HEAD
        // request to fetch it, save it via the CookieSyncManager to flash, then return it.
        private fun getCookie(url: String): String? {
            var cookie = CookieManager.getInstance().getCookie(url)
            if (cookie == null || cookie.length == 0) {
                Log.v(TAG, "Book Search cookie was missing or expired")
                val head = HttpHead(url)
                val client = AndroidHttpClient.newInstance(USER_AGENT)
                try {
                    val response = client.execute(head)
                    if (response.statusLine.statusCode == 200) {
                        val cookies = response.getHeaders("set-cookie")
                        for (theCookie in cookies) {
                            CookieManager.getInstance().setCookie(url, theCookie.value)
                        }
                        CookieSyncManager.getInstance().sync()
                        cookie = CookieManager.getInstance().getCookie(url)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                }
                client.close()
            }
            return cookie
        }

        companion object {
            private fun getEncoding(entity: HttpEntity): String {
                // FIXME: The server is returning ISO-8859-1 but the content is actually windows-1252.
                // Once Jeff fixes the HTTP response, remove this hardcoded value and go back to getting
                // the encoding dynamically.
                return "windows-1252"
                //            HeaderElement[] elements = entity.getContentType().getElements();
//            if (elements != null && elements.length > 0) {
//                String encoding = elements[0].getParameterByName("charset").getValue();
//                if (encoding != null && encoding.length() > 0) {
//                    return encoding;
//                }
//            }
//            return "UTF-8";
            }
        }
    }

    companion object {
        private const val TAG = "SearchBookContents"
        private const val USER_AGENT = "ZXing/1.5 (Android)"
    }
}
