package intellibitz.intellidroid.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

object MediaPickerEncoder {
    private val defaultFormat = Bitmap.CompressFormat.JPEG
    private const val defaultFormatQuality = 100

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(
        bitmap: Bitmap?,
        mimeType: String?,
        format: Bitmap.CompressFormat?,
        quality: Int
    ): String {
        if (bitmap == null) {
            throw IOException("Bitmap cannot be null.")
        }
        if (mimeType == null) {
            throw IOException("Mime type cannot be null.")
        }
        return "data:$mimeType;base64," + asEncodedBase64String(bitmap, format, quality)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(bitmap: Bitmap?, mimeType: String?): String {
        return toDataUrl(bitmap, mimeType, defaultFormat, defaultFormatQuality)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(
        filePath: String?,
        mimeType: String?,
        format: Bitmap.CompressFormat?,
        quality: Int
    ): String {
        if (filePath == null) {
            throw IOException("File path cannot be null.")
        }
        val opt = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(filePath, opt)
        return toDataUrl(bitmap, mimeType ?: opt.outMimeType, format, quality)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(filePath: String?, mimeType: String?): String {
        return toDataUrl(filePath, mimeType, defaultFormat, defaultFormatQuality)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(filePath: String?): String {
        return toDataUrl(filePath, null)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(
        file: File?,
        mimeType: String?,
        format: Bitmap.CompressFormat?,
        quality: Int
    ): String {
        if (file == null) {
            throw IOException("File cannot be null.")
        }
        return toDataUrl(file.absolutePath, mimeType, format, quality)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(file: File?, mimeType: String?): String {
        return toDataUrl(file, mimeType, defaultFormat, defaultFormatQuality)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun toDataUrl(file: File?): String {
        return toDataUrl(file, null)
    }

    @Throws(IOException::class)
    private fun asEncodedBase64String(
        bitmap: Bitmap?,
        format: Bitmap.CompressFormat?,
        quality: Int
    ): String {
        if (bitmap == null || format == null) {
            throw IOException("Cannot encode a null bitmap or compression format.")
        }
        val bitmapData = ByteArrayOutputStream()
        bitmap.compress(format, quality, bitmapData)
        val encodedData = Base64.encodeToString(bitmapData.toByteArray(), Base64.NO_WRAP)
        bitmap.recycle()
        bitmapData.flush()
        bitmapData.close()
        return encodedData
    }
}
