package intellibitz.intellidroid.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.data.MessageItem;

import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUrlConnectionParser {
    private static final String TAG = "HttpUrlConnection";

    public static void downloadURLToFile(String url, File file) throws IOException {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        try {
            // Starts the query
            conn.connect();
            inputStream = conn.getInputStream();
            bufferedInputStream = new BufferedInputStream(inputStream);
            fileOutputStream = new FileOutputStream(file);
            byte buffer[] = new byte[1024];
            int read;
            while ((read = bufferedInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, read);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            bufferedInputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        } finally {
            conn.disconnect();
/*
            if (fileOutputStream != null) {
                fileOutputStream.close();
                bufferedInputStream.close();
                inputStream.close();
            }
*/
        }
        Log.e(TAG, "Url: " + url + " saved to file: " + file.getAbsolutePath());
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    public static String getHTTP(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public static String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


    public static JSONObject headHTTP(
            String urlPath, int readTimeOut, int writeTimeOut) {
        HttpURLConnection urlConnection = null;
        JSONObject jsonObject = null;
        try {
            URL url = new URL(urlPath);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(readTimeOut);
            urlConnection.setConnectTimeout(writeTimeOut);
            urlConnection.setRequestMethod("HEAD");
            int responseCode = urlConnection.getResponseCode();
            Log.e(TAG, urlPath + " response: " + responseCode);
            if (200 == responseCode) {
                jsonObject = new JSONObject();
                String type = urlConnection.getHeaderField("Content-Type");
                int len = urlConnection.getHeaderFieldInt("Content-Length", 0);
                jsonObject.put("Content-Type", type);
                jsonObject.put("Content-Length", len);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return jsonObject;
    }

    public static JSONObject postHTTP(
            String urlPath, HashMap<String, String> data, int readTimeOut, int writeTimeOut) {
        InputStream responseStream = null;
        JSONObject jsonObject = null;
        try {
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(readTimeOut);
            urlConnection.setConnectTimeout(writeTimeOut);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            OutputStream os = urlConnection.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQueryData(data));
            writer.flush();
            writer.close();
            urlConnection.connect();

            responseStream = urlConnection.getInputStream();
            jsonObject = getJsonObject(responseStream);

        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.getMessage());
        } finally {
            try {
                if (responseStream != null) {
                    responseStream.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, TAG + e.getMessage());
            }
        }
        return jsonObject;
    }

    public static JSONObject headHTTP(String urlPath, int readTimeOut) {
        return headHTTP(urlPath, readTimeOut, 150000);
    }

    public static JSONObject postHTTP(
            String urlPath, HashMap<String, String> data, int readTimeOut) {
        return postHTTP(urlPath, data, readTimeOut, 150000);
    }

    public static JSONObject headHTTP(String urlPath) {
        return headHTTP(urlPath, 30000);
    }

    public static JSONObject postHTTP(String urlPath, HashMap<String, String> data) {
        return postHTTP(urlPath, data, 30000);
    }

    private static JSONObject getJsonObject(InputStream responseStream) {
        String json = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            json = sb.toString();
            responseStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return jsonObject;
    }

    public static String getQueryData(HashMap<String, String> data) throws
            UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            if (entry != null) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        return result.toString();
    }

    public static Bitmap getBitmapFromURL(String src) throws IOException {
        if (TextUtils.isEmpty(src)) return null;
        Bitmap bitmap = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(src).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        return MainApplicationSingleton.decodeStream(input, 100);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Throwable ignored) {
            Log.e(TAG, TAG + ignored.getMessage());
        }
        return bitmap;
    }

    public static JSONObject
    uploadAttachments(MessageItem item, String url, String uid,
                      String device, String deviceRef, String token) throws
            IOException {
        try {
            String charset = "UTF-8";
            MultipartUtility multipart =
                    new MultipartUtility(url, charset);
            multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device);
            multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid);
            multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token);
            File file = new File(item.getDescription());
            multipart.addFilePart(MainApplicationSingleton.ATTACH_FILE_PARAM, file,
                    file.getAbsolutePath());
            // params comes from the execute() call: params[0] is the url.
            JSONObject response = multipart.finishAsJSON(); // response from server.
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (99 == status) {
                Log.e(TAG, "Attachments Upload ERROR - " + response);
            } else if (1 == status) {
//                    SUCCESS
                MsgChatAttachmentContentProvider.setAttachmentItemFromJson(item, response);
            }
            return response;
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public static class MultipartUtility {
        private static final String LINE_FEED = "\r\n";
        private final String boundary;
        private HttpURLConnection httpConn;
        private String charset;
        private OutputStream outputStream;
        private PrintWriter writer;

        /**
         * This constructor initializes a new HTTP POST request with content type
         * is set to multipart/form-data
         *
         * @param requestURL
         * @param charset
         * @throws IOException
         */
        public MultipartUtility(String requestURL, String charset)
                throws IOException {
            this.charset = charset;

            // creates a unique boundary based on time stamp
            boundary = "===" + System.currentTimeMillis() + "===";

            URL url = new URL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true); // indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            httpConn.setRequestProperty("User-Agent", "IntelliBitz Android Agent");
            httpConn.setRequestProperty("Test", "Bonjour");
            outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                    true);
        }

        /**
         * Adds a form field to the request
         *
         * @param name  field name
         * @param value field value
         */
        public void addFormField(String name, String value) {
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                    .append(LINE_FEED);
            writer.append("Content-Type: text/plain; charset=" + charset).append(
                    LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(value).append(LINE_FEED);
            writer.flush();
        }

        /**
         * Adds a upload file section to the request
         *
         * @param fieldName  name attribute in <input type="file" name="..." />
         * @param uploadFile a File to be uploaded
         * @throws IOException
         */
        public void addFilePart(String fieldName, File uploadFile, String fileName)
                throws IOException {
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append(
                    "Content-Disposition: form-data; name=\"" + fieldName
                            + "\"; filename=\"" + fileName + "\"")
                    .append(LINE_FEED);
            writer.append(
                    "Content-Type: "
                            + URLConnection.guessContentTypeFromName(fileName))
                    .append(LINE_FEED);
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();

            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

            writer.append(LINE_FEED);
            writer.flush();
        }

        /**
         * Adds a header field to the request.
         *
         * @param name  - name of the header field
         * @param value - value of the header field
         */
        public void addHeaderField(String name, String value) {
            writer.append(name + ": " + value).append(LINE_FEED);
            writer.flush();
        }

        /**
         * Completes the request and receives response from the server.
         *
         * @return a list of Strings as response in case the server returned
         * status OK, otherwise an exception is thrown.
         * @throws IOException
         */
        public List<String> finish() throws IOException {
            List<String> response = new ArrayList<String>();

            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();

            // checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpConn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
                reader.close();
                httpConn.disconnect();
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }

            return response;
        }

        public JSONObject finishAsJSON() throws IOException {

            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();

            // checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                return getJsonObject(httpConn.getInputStream());
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
        }
    }


}
