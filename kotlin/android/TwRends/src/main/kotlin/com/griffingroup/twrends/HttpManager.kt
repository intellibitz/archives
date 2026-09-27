package com.griffingroup.twrends

import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

object HttpManager {
    private const val TAG = "HttpManager"
    private const val USER_AGENT = "com.griffingroup.twrends.AndroidClient/1.1"
    private const val TIMEOUT_MS = 20_000

    @JvmStatic
    @Throws(IOException::class)
    fun execute(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept-Charset", "UTF-8")
                requestMethod = "GET"
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { stream ->
                    IOUtils.toString(stream, "UTF-8")
                }
            } else {
                Log.e(TAG, "HTTP error response code: ${connection.responseCode}")
                null
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Socket timeout: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Execution error: ${e.message}")
            null
        }
    }
}
