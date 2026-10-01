/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Zxing project to suit Books-Exchange requirements.
 * Original source from Zxing - http://code.google.com/p/zxing/
 */

/*
 * Copyright 2008 ZXing authors
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
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import com.androidrocks.bex.R

/**
 * @author dswitkin@google.com (Daniel Switkin)
 */
class HelpActivity : Activity() {

    private var mWebView: WebView? = null
    private var mBackButton: Button? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.help)

        mWebView = findViewById<View>(R.id.help_contents) as WebView
        mWebView!!.webViewClient = HelpClient()
        if (icicle != null) {
            mWebView!!.restoreState(icicle)
        } else {
            mWebView!!.loadUrl(DEFAULT_URL)
        }

        mBackButton = findViewById<View>(R.id.back_button) as Button
        mBackButton!!.setOnClickListener(mBackListener)

        val doneButton = findViewById<View>(R.id.done_button) as Button
        doneButton.setOnClickListener(mDoneListener)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(state: Bundle) {
        mWebView!!.saveState(state)
        super.onSaveInstanceState(state)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView!!.canGoBack()) {
                mWebView!!.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private val mBackListener = View.OnClickListener { mWebView!!.goBack() }

    private val mDoneListener = View.OnClickListener { finish() }

    private inner class HelpClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            title = view.title
            mBackButton!!.isEnabled = view.canGoBack()
        }
    }

    companion object {
        private const val DEFAULT_URL = "file:///android_asset/html/index.html"
    }
}
