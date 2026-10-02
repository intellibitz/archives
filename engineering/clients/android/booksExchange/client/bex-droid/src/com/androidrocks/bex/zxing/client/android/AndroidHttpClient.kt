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

import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.params.HttpClientParams
import org.apache.http.client.protocol.ClientContext
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.params.HttpProtocolParams
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.BasicHttpProcessor
import org.apache.http.protocol.HttpContext
import java.io.IOException

/**
 * <p>Subclass of the Apache [DefaultHttpClient] that is configured with
 * reasonable default settings and registered schemes for Android, and
 * also lets the user add [HttpRequestInterceptor] classes.
 * Don't create this directly, use the [.newInstance] factory method.</p>
 * <p/>
 * <p>This client processes cookies but does not retain them by default.
 * To retain cookies, simply add a cookie store to the HttpContext:
 * <pre>context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);</pre>
 * </p>
 */
class AndroidHttpClient private constructor(ccm: ClientConnectionManager, params: HttpParams) :
    HttpClient {

    private val delegate: HttpClient

    init {
        this.delegate = object : DefaultHttpClient(ccm, params) {
            override fun createHttpProcessor(): BasicHttpProcessor {
                // Add interceptor to prevent making requests from main thread.
                val processor = super.createHttpProcessor()
                processor.addRequestInterceptor(sThreadCheckInterceptor)
                return processor
            }

            override fun createHttpContext(): HttpContext {
                // Same as DefaultHttpClient.createHttpContext() minus the
                // cookie store.
                val context: HttpContext = BasicHttpContext()
                context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, authSchemes)
                context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, cookieSpecs)
                context.setAttribute(ClientContext.CREDS_PROVIDER, credentialsProvider)
                return context
            }
        }
    }

    /**
     * Release resources associated with this client.  You must call this,
     * or significant resources (sockets and memory) may be leaked.
     */
    fun close() {
        connectionManager.shutdown()
    }

    override fun getParams(): HttpParams {
        return delegate.params
    }

    override fun getConnectionManager(): ClientConnectionManager {
        return delegate.connectionManager
    }

    @Throws(IOException::class)
    override fun execute(request: HttpUriRequest): HttpResponse {
        return delegate.execute(request)
    }

    @Throws(IOException::class)
    override fun execute(request: HttpUriRequest, context: HttpContext): HttpResponse {
        return delegate.execute(request, context)
    }

    @Throws(IOException::class)
    override fun execute(target: HttpHost, request: HttpRequest): HttpResponse {
        return delegate.execute(target, request)
    }

    @Throws(IOException::class)
    override fun execute(
        target: HttpHost, request: HttpRequest,
        context: HttpContext
    ): HttpResponse {
        return delegate.execute(target, request, context)
    }

    @Throws(IOException::class)
    override fun <T> execute(
        request: HttpUriRequest,
        responseHandler: ResponseHandler<out T>
    ): T {
        return delegate.execute(request, responseHandler)
    }

    @Throws(IOException::class)
    override fun <T> execute(
        request: HttpUriRequest,
        responseHandler: ResponseHandler<out T>, context: HttpContext
    ): T {
        return delegate.execute(request, responseHandler, context)
    }

    @Throws(IOException::class)
    override fun <T> execute(
        target: HttpHost, request: HttpRequest, responseHandler: ResponseHandler<out T>
    ): T {
        return delegate.execute(target, request, responseHandler)
    }

    @Throws(IOException::class)
    override fun <T> execute(
        target: HttpHost, request: HttpRequest,
        responseHandler: ResponseHandler<out T>,
        context: HttpContext
    ): T {
        return delegate.execute(target, request, responseHandler, context)
    }

    companion object {

        /**
         * Set if HTTP requests are blocked from being executed on this thread
         */
        private val sThreadBlocked: ThreadLocal<Boolean> = ThreadLocal<Boolean>()

        /**
         * Interceptor throws an exception if the executing thread is blocked
         */
        private val sThreadCheckInterceptor = HttpRequestInterceptor { request, context ->
            if (java.lang.Boolean.TRUE == sThreadBlocked.get()) {
                throw RuntimeException("This thread forbids HTTP requests")
            }
        }

        /**
         * Create a new HttpClient with reasonable defaults (which you can update).
         *
         * @param userAgent to report in your HTTP requests.
         * @return AndroidHttpClient for you to use for all your requests.
         */
        @JvmStatic
        fun newInstance(userAgent: String?): AndroidHttpClient {
            val params: HttpParams = BasicHttpParams()

            // Turn off stale checking.  Our connections break all the time anyway,
            // and it's not worth it to pay the penalty of checking every time.
            HttpConnectionParams.setStaleCheckingEnabled(params, false)

            // Default connection and socket timeout of 20 seconds.  Tweak to taste.
            HttpConnectionParams.setConnectionTimeout(params, 20 * 1000)
            HttpConnectionParams.setSoTimeout(params, 20 * 1000)
            HttpConnectionParams.setSocketBufferSize(params, 8192)

            // Don't handle redirects -- return them to the caller.  Our code
            // often wants to re-POST after a redirect, which we must do ourselves.
            HttpClientParams.setRedirecting(params, false)

            // Set the specified user agent and register standard protocols.
            HttpProtocolParams.setUserAgent(params, userAgent)
            val schemeRegistry = SchemeRegistry()
            schemeRegistry.register(
                Scheme(
                    "http",
                    PlainSocketFactory.getSocketFactory(), 80
                )
            )
            schemeRegistry.register(
                Scheme(
                    "https",
                    SSLSocketFactory.getSocketFactory(), 443
                )
            )
            val manager: ClientConnectionManager = ThreadSafeClientConnManager(params, schemeRegistry)

            // We use a factory method to modify superclass initialization
            // parameters without the funny call-a-static-method dance.
            return AndroidHttpClient(manager, params)
        }
    }
}
