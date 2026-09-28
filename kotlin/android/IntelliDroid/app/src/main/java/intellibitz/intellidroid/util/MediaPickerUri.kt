package intellibitz.intellidroid.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Suppress("UnusedDeclaration")
object MediaPickerUri {

    private const val TAG = "MediaPickerUri"

    private const val AUTHORITY_GOOGLE_PHOTOS = "com.google.android.apps.photos.content"
    private const val AUTHORITY_EXTERNAL_STORAGE = "com.android.externalstorage.documents"
    private const val AUTHORITY_DOWNLOADS_DOCUMENT = "com.android.providers.downloads.documents"
    private const val AUTHORITY_MEDIA_DOCUMENT = "com.android.providers.media.documents"
    private const val AUTHORITY_GOOGLE_PHOTOS_CONTENTPROVIDER = "com.google.android.apps.photos.contentprovider"
    private const val AUTHORITY_GOOGLE_DOCS_STORAGE = "com.google.android.apps.docs.storage"

    @JvmStatic
    @Throws(IOException::class)
    fun resolveToFile(context: Context?, uri: Uri?): File {
        if (context == null) {
            throw IOException("A valid android application context is required.")
        }
        if (uri == null) {
            throw IOException("File URI cannot be null.")
        }
        var path: String? = null
        if (AUTHORITY_GOOGLE_DOCS_STORAGE == uri.authority) {
            throw IOException("Google Docs Storage not supported - Future release only")
        }
        if (AUTHORITY_GOOGLE_PHOTOS_CONTENTPROVIDER == uri.authority) {
            val imageUrlWithAuthority = getImageUrlWithAuthority(context, uri)
            if (imageUrlWithAuthority != null) {
                path = getPath(context, Uri.parse(imageUrlWithAuthority))
            }
        }
        if (path == null) {
            path = getPath(context, uri)
        }
        if (path == null) {
            path = uri.toString()
        }
        if (path == null) {
            throw IOException("File path was not found.")
        }
        if (!isLocal(path)) {
            throw IOException("File path was found, but path must be a local URI.")
        }
        val file = File(path)
        if (!file.exists()) {
            throw IOException("File path was found, but file does not exist." + file.absolutePath)
        }
        return File(path)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun resolveToFile(context: Context, intentUri: Intent, name: String): File {
        val uri = intentUri.getParcelableExtra<Uri>(name)
        return resolveToFile(context, uri)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun resolveToFile(context: Context, intentUri: Intent): File {
        return resolveToFile(context, intentUri, Intent.EXTRA_STREAM)
    }

    @Throws(IOException::class)
    private fun getPath(context: Context, uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (uri.authority == null) {
                    throw IOException("Uri authority cannot be null.")
                }

                val documentId = DocumentsContract.getDocumentId(uri)

                when (uri.authority) {
                    AUTHORITY_EXTERNAL_STORAGE -> {
                        val split = documentId.split(":")
                        val type = split[0]
                        if ("primary".equals(type, ignoreCase = true)) {
                            return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        }
                        throw IOException("Unable to handle non-primary external storage volumes.")
                    }
                    AUTHORITY_DOWNLOADS_DOCUMENT -> {
                        val contentUri = Uri.parse("content://downloads/public_downloads")
                        val contentUriAppended = ContentUris.withAppendedId(contentUri, documentId.toLong())
                        return getDataColumn(context, contentUriAppended, null, null)
                    }
                    AUTHORITY_MEDIA_DOCUMENT -> {
                        val split = documentId.split(":")
                        val type = split[0]
                        var contentUri: Uri? = null
                        if ("image" == type) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == type) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == type) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        return if (contentUri != null) getDataColumn(context, contentUri, selection, selectionArgs) else null
                    }
                    else -> throw IOException("Unknown URI document authority encountered: ${uri.authority}")
                }
            }
        }

        if (uri.scheme == null) {
            return null
        }

        return when (uri.scheme) {
            "content" -> {
                if (AUTHORITY_GOOGLE_PHOTOS == uri.authority) {
                    uri.lastPathSegment
                } else {
                    getDataColumn(context, uri, null, null)
                }
            }
            "file" -> uri.path
            else -> throw IOException("Unknown URI scheme encountered: ${uri.scheme}")
        }
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column))
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    @JvmStatic
    fun toFileUriString(file: File): String {
        return toFileUriString(file.absolutePath)
    }

    @JvmStatic
    fun toFileUriString(file: String): String {
        return "file:$file"
    }

    private fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    @JvmStatic
    fun getImageUrlWithAuthority(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        if (uri.authority != null) {
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                val pathUri = writeToTempImageAndGetPathUri(context, bmp) ?: return null
                return pathUri.toString()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    @JvmStatic
    @Throws(IOException::class)
    fun bitmapToFile(context: Context?, bitmap: Bitmap, file: File) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapdata = bos.toByteArray()
        try {
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
        } catch (ignored: IOException) {
            Log.e(TAG, "bitmapToFile:IOException: $file : ${ignored.message}")
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun bitmapToFile(context: Context, bitmap: Bitmap, filename: String, suffix: String, dir: File): File {
        val temp = File.createTempFile(filename, suffix, dir)
        bitmapToFile(context, bitmap, temp)
        return temp
    }

    @JvmStatic
    @Throws(IOException::class)
    fun bitmapToFile(context: Context, bitmap: Bitmap, filename: String, suffix: String): File {
        return bitmapToFile(context, bitmap, filename, suffix, context.cacheDir)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun bitmapToFile(context: Context, bitmap: Bitmap, filename: String): File {
        return bitmapToFile(context, bitmap, filename, ".jpg", context.cacheDir)
    }

    @JvmStatic
    fun writeToTempImageAndGetPathUri(inContext: Context?, inImage: Bitmap?): Uri? {
        if (inContext == null || inImage == null) return null
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver, inImage, "Title", null
        ) ?: return null
        return Uri.parse(path)
    }

    @JvmStatic
    fun dumpImageMetaData(context: Context, uri: Uri) {
        val cursor = context.contentResolver.query(uri, null, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.i(TAG, "Display Name: $displayName")

                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val size = if (!cursor.isNull(sizeIndex)) {
                    cursor.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.i(TAG, "Size: $size")
            }
        } finally {
            cursor?.close()
        }
    }
}
