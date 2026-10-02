package intellibitz.intellidroid.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.data.MessageItem
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Reader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.util.ArrayList
import java.util.HashMap

class HttpUrlConnectionParser {

    companion object {
        private const val TAG = "HttpUrlConnection"

        @JvmStatic
        @Throws(IOException::class)
        fun downloadURLToFile(url: String, file: File) {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.readTimeout = 10000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            try {
                conn.connect()
                val inputStream = conn.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                val fileOutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var read: Int
                while (bufferedInputStream.read(buffer).also { read = it } > 0) {
                    fileOutputStream.write(buffer, 0, read)
                }
                fileOutputStream.flush()
                fileOutputStream.close()
                bufferedInputStream.close()
                inputStream.close()
            } catch (e: FileNotFoundException) {
                throw IOException(e)
            } finally {
                conn.disconnect()
            }
            Log.e(TAG, "Url: $url saved to file: ${file.absolutePath}")
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getHTTP(myurl: String): String {
            var `is`: InputStream? = null
            val len = 500
            try {
                val url = URL(myurl)
                val conn = url.openConnection() as HttpURLConnection
                conn.readTimeout = 10000
                conn.connectTimeout = 15000
                conn.requestMethod = "GET"
                conn.doInput = true
                conn.connect()
                val response = conn.responseCode
                Log.d(TAG, "The response is: $response")
                `is` = conn.inputStream
                return readIt(`is`, len)
            } finally {
                `is`?.close()
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readIt(stream: InputStream, len: Int): String {
            val reader: Reader = InputStreamReader(stream, "UTF-8")
            val buffer = CharArray(len)
            reader.read(buffer)
            return String(buffer)
        }

        @JvmStatic
        @JvmOverloads
        fun headHTTP(urlPath: String, readTimeOut: Int = 30000, writeTimeOut: Int = 150000): JSONObject? {
            var urlConnection: HttpURLConnection? = null
            var jsonObject: JSONObject? = null
            try {
                val url = URL(urlPath)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.readTimeout = readTimeOut
                urlConnection.connectTimeout = writeTimeOut
                urlConnection.requestMethod = "HEAD"
                val responseCode = urlConnection.responseCode
                Log.e(TAG, "$urlPath response: $responseCode")
                if (200 == responseCode) {
                    jsonObject = JSONObject()
                    val type = urlConnection.getHeaderField("Content-Type")
                    val len = urlConnection.getHeaderFieldInt("Content-Length", 0)
                    jsonObject.put("Content-Type", type)
                    jsonObject.put("Content-Length", len)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, e.message ?: "")
            } finally {
                urlConnection?.disconnect()
            }
            return jsonObject
        }

        @JvmStatic
        @JvmOverloads
        fun postHTTP(
            urlPath: String,
            data: HashMap<String, String>,
            readTimeOut: Int = 30000,
            writeTimeOut: Int = 150000
        ): JSONObject? {
            var responseStream: InputStream? = null
            var jsonObject: JSONObject? = null
            try {
                val url = URL(urlPath)
                val urlConnection = url.openConnection() as HttpURLConnection

                urlConnection.readTimeout = readTimeOut
                urlConnection.connectTimeout = writeTimeOut
                urlConnection.requestMethod = "POST"
                urlConnection.doInput = true
                urlConnection.doOutput = true

                val os = urlConnection.outputStream
                val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                writer.write(getQueryData(data))
                writer.flush()
                writer.close()
                urlConnection.connect()

                responseStream = urlConnection.inputStream
                jsonObject = getJsonObject(responseStream)
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, TAG + e.message)
            } finally {
                try {
                    responseStream?.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    Log.e(TAG, TAG + e.message)
                }
            }
            return jsonObject
        }

        @JvmStatic
        private fun getJsonObject(responseStream: InputStream?): JSONObject? {
            if (responseStream == null) return null
            var json: String? = null
            try {
                val reader = BufferedReader(InputStreamReader(responseStream, "iso-8859-1"), 8)
                val sb = java.lang.StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                json = sb.toString()
                responseStream.close()
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, e.message ?: "")
            }

            var jsonObject: JSONObject? = null
            try {
                if (json != null) {
                    jsonObject = JSONObject(json)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, e.message ?: "")
            }
            return jsonObject
        }

        @JvmStatic
        @Throws(UnsupportedEncodingException::class)
        fun getQueryData(data: HashMap<String, String>): String {
            val result = java.lang.StringBuilder()
            var first = true
            for ((key, value) in data) {
                if (first) first = false else result.append("&")
                result.append(URLEncoder.encode(key, "UTF-8"))
                result.append("=")
                result.append(URLEncoder.encode(value, "UTF-8"))
            }
            return result.toString()
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getBitmapFromURL(src: String?): Bitmap? {
            if (TextUtils.isEmpty(src)) return null
            var bitmap: Bitmap? = null
            try {
                val connection = URL(src).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val options = BitmapFactory.Options()
                options.inSampleSize = 2
                bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            } catch (ignored: Throwable) {
                Log.e(TAG, TAG + ignored.message)
            }
            return bitmap
        }

        @JvmStatic
        @Throws(IOException::class)
        fun uploadAttachments(
            item: MessageItem,
            url: String,
            uid: String,
            device: String,
            deviceRef: String,
            token: String
        ): JSONObject? {
            try {
                val charset = "UTF-8"
                val multipart = MultipartUtility(url, charset)
                multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device)
                multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef)
                multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid)
                multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token)
                val file = File(item.description)
                multipart.addFilePart(MainApplicationSingleton.ATTACH_FILE_PARAM, file, file.absolutePath)
                val response = multipart.finishAsJSON()
                val status = response.getInt(MainApplicationSingleton.STATUS_PARAM)
                if (99 == status) {
                    Log.e(TAG, "Attachments Upload ERROR - $response")
                } else if (1 == status) {
                    MsgChatAttachmentContentProvider.setAttachmentItemFromJson(item, response)
                }
                return response
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.e(TAG, e.message ?: "")
            }
            return null
        }
    }

    class MultipartUtility @Throws(IOException::class) constructor(
        requestURL: String,
        private val charset: String
    ) {
        companion object {
            private const val LINE_FEED = "\r\n"
        }

        private val boundary: String = "===" + System.currentTimeMillis() + "==="
        private val httpConn: HttpURLConnection
        private val outputStream: OutputStream
        private val writer: PrintWriter

        init {
            val url = URL(requestURL)
            httpConn = url.openConnection() as HttpURLConnection
            httpConn.useCaches = false
            httpConn.doOutput = true
            httpConn.doInput = true
            httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            httpConn.setRequestProperty("User-Agent", "IntelliBitz Android Agent")
            httpConn.setRequestProperty("Test", "Bonjour")
            outputStream = httpConn.outputStream
            writer = PrintWriter(OutputStreamWriter(outputStream, charset), true)
        }

        fun addFormField(name: String, value: String?) {
            writer.append("--$boundary").append(LINE_FEED)
            writer.append("Content-Disposition: form-data; name=\"$name\"").append(LINE_FEED)
            writer.append("Content-Type: text/plain; charset=$charset").append(LINE_FEED)
            writer.append(LINE_FEED)
            writer.append(value ?: "").append(LINE_FEED)
            writer.flush()
        }

        @Throws(IOException::class)
        fun addFilePart(fieldName: String, uploadFile: File, fileName: String) {
            writer.append("--$boundary").append(LINE_FEED)
            writer.append("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"")
                .append(LINE_FEED)
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED)
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED)
            writer.append(LINE_FEED)
            writer.flush()

            val inputStream = FileInputStream(uploadFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            inputStream.close()

            writer.append(LINE_FEED)
            writer.flush()
        }

        fun addHeaderField(name: String, value: String) {
            writer.append("$name: $value").append(LINE_FEED)
            writer.flush()
        }

        @Throws(IOException::class)
        fun finish(): List<String> {
            val response = ArrayList<String>()

            writer.append(LINE_FEED).flush()
            writer.append("--$boundary--").append(LINE_FEED)
            writer.close()

            val status = httpConn.responseCode
            if (status == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(httpConn.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let { response.add(it) }
                }
                reader.close()
                httpConn.disconnect()
            } else {
                throw IOException("Server returned non-OK status: $status")
            }

            return response
        }

        @Throws(IOException::class)
        fun finishAsJSON(): JSONObject {
            writer.append(LINE_FEED).flush()
            writer.append("--$boundary--").append(LINE_FEED)
            writer.close()

            val status = httpConn.responseCode
            if (status == HttpURLConnection.HTTP_OK) {
                return getJsonObject(httpConn.inputStream) ?: JSONObject()
            } else {
                throw IOException("Server returned non-OK status: $status")
            }
        }
    }
}
