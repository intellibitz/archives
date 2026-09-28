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

import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.params.HttpClientParams
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpProtocolParams
import java.io.IOException

object HttpManager {
    private val sClient: DefaultHttpClient

    init {
        val params = BasicHttpParams()
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1)
        HttpProtocolParams.setContentCharset(params, "UTF-8")

        HttpConnectionParams.setStaleCheckingEnabled(params, false)
        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000)
        HttpConnectionParams.setSoTimeout(params, 20 * 1000)
        HttpConnectionParams.setSocketBufferSize(params, 8192)

        HttpClientParams.setRedirecting(params, false)

        HttpProtocolParams.setUserAgent(params, "Shelves/1.1")

        val schemeRegistry = SchemeRegistry()
        schemeRegistry.register(Scheme("http", PlainSocketFactory.getSocketFactory(), 80))
        schemeRegistry.register(Scheme("https", SSLSocketFactory.getSocketFactory(), 443))

        val manager = ThreadSafeClientConnManager(params, schemeRegistry)
        sClient = DefaultHttpClient(manager, params)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun execute(head: HttpHead?): HttpResponse {
        return sClient.execute(head)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun execute(host: HttpHost?, get: HttpGet?): HttpResponse {
        return sClient.execute(host, get)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun execute(get: HttpGet?): HttpResponse {
        return sClient.execute(get)
    }
}
