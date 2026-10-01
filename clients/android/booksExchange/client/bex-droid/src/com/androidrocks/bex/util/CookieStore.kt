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

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpHead
import java.io.IOException

class CookieStore private constructor() {

    fun getCookie(url: String): String? {
        val cookieManager = CookieManager.getInstance()
        var cookie = cookieManager.getCookie(url)

        if (cookie == null || cookie.length == 0) {
            val head = HttpHead(url)
            var entity: HttpEntity? = null
            try {
                val response = HttpManager.execute(head)
                if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                    entity = response.entity

                    val cookies = response.getHeaders("set-cookie")
                    for (cooky in cookies) {
                        cookieManager.setCookie(url, cooky.value)
                    }

                    CookieSyncManager.getInstance().sync()
                    cookie = cookieManager.getCookie(url)
                }
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Could not retrieve cookie", e)
            } finally {
                if (entity != null) {
                    try {
                        entity.consumeContent()
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "Could not retrieve cookie", e)
                    }
                }
            }
        }

        return cookie
    }

    companion object {
        private const val LOG_TAG = "Shelves"

        private val sCookieStore: CookieStore = CookieStore()

        @JvmStatic
        fun initialize(context: Context?) {
            CookieSyncManager.createInstance(context)
            CookieManager.getInstance().removeExpiredCookie()
        }

        @JvmStatic
        fun get(): CookieStore {
            return sCookieStore
        }
    }
}
