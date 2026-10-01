package com.mobeegal.android.util

/*
<!--
$Id:: HttpUtils.java 6 2008-08-12 16:41:53Z muthu.ramadoss                      $: Id of last commit
$Rev:: 6                                                                        $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-12 22:11:53 +0530 (Tue, 12 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.util.Log
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import java.io.BufferedReader
import java.io.InputStreamReader

object HttpUtils {

    @JvmStatic
    fun httpGet(baseUri: String, uriPath: String): String {
        val params: HttpParams = BasicHttpParams()
        HttpConnectionParams.setConnectionTimeout(params, 4000)
        HttpConnectionParams.setSoTimeout(params, 4000)
        return httpGet(baseUri, uriPath, DefaultHttpClient(params))
    }

    @JvmStatic
    fun httpGet(baseUri: String, uriPath: String, client: HttpClient): String {
        var responseString = ""
        try {
            val httpget = HttpGet(baseUri + uriPath)
            val response = client.execute(httpget)

            val `in` = BufferedReader(
                InputStreamReader(response.entity.content), 8192
            )
            var line: String?

            while (`in`.readLine().also { line = it } != null) {
                responseString += line
                //Log.d("serveResponse", line);
            }
        } catch (e: Exception) {
            Log.e("HttpUtils", e.toString() + " " + e.message)
        }
        return responseString
    }

    @JvmStatic
    fun getResponseString(response: HttpResponse?): String? {
        var rResult: String? = null
        if (response != null) {
            try {
                val rStr = StringBuilder()
                val `in` = BufferedReader(
                    InputStreamReader(response.entity.content), 8192
                )
                var line: String?
                while (`in`.readLine().also { line = it } != null) {
                    rStr.append(line)
                }
                rResult = rStr.toString()
                `in`.close()
            } catch (ex: Exception) {
                Log.e("FindAndInstall", "Exception:", ex)
            }
        }
        //Log.d(TAG, "getResponseString: " + rResult);
        return rResult
    }
}
