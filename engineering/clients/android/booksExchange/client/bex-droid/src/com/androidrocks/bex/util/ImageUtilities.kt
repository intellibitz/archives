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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import com.androidrocks.bex.drawable.FastBitmapDrawable
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.SoftReference
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.HashMap

object ImageUtilities {
    private const val LOG_TAG = "ImageUtilities"

    private const val FLAG_DECODE_BITMAP_WITH_SKIA = false

    private const val EDGE_START = 0.0f
    private const val EDGE_END = 4.0f
    private const val EDGE_COLOR_START = 0x7F000000
    private const val EDGE_COLOR_END = 0x00000000
    private val EDGE_PAINT = Paint()

    private const val END_EDGE_COLOR_START = 0x00000000
    private const val END_EDGE_COLOR_END = 0x4F000000
    private val END_EDGE_PAINT = Paint()

    private const val FOLD_START = 5.0f
    private const val FOLD_END = 13.0f
    private const val FOLD_COLOR_START = 0x00000000
    private const val FOLD_COLOR_END = 0x26000000
    private val FOLD_PAINT = Paint()

    private const val SHADOW_RADIUS = 12.0f
    private const val SHADOW_COLOR = 0x99000000.toInt()
    private val SHADOW_PAINT = Paint()

    private val SCALE_PAINT = Paint(
        Paint.ANTI_ALIAS_FLAG or
                Paint.FILTER_BITMAP_FLAG
    )

    private val NULL_DRAWABLE = FastBitmapDrawable(null)

    // TODO: Use a concurrent HashMap to support multiple threads
    private val sArtCache = HashMap<String, SoftReference<FastBitmapDrawable>>()

    @Volatile
    private var sScaleMatrix: Matrix? = null
    private var sLastModifiedFormat: SimpleDateFormat? = null

    init {
        var shader: Shader = LinearGradient(
            EDGE_START, 0.0f, EDGE_END, 0.0f, EDGE_COLOR_START,
            EDGE_COLOR_END, Shader.TileMode.CLAMP
        )
        EDGE_PAINT.shader = shader

        shader = LinearGradient(
            EDGE_START, 0.0f, EDGE_END, 0.0f, END_EDGE_COLOR_START,
            END_EDGE_COLOR_END, Shader.TileMode.CLAMP
        )
        END_EDGE_PAINT.shader = shader

        shader = LinearGradient(
            FOLD_START, 0.0f, FOLD_END, 0.0f, intArrayOf(
                FOLD_COLOR_START, FOLD_COLOR_END, FOLD_COLOR_START
            ), floatArrayOf(0.0f, 0.5f, 1.0f), Shader.TileMode.CLAMP
        )
        FOLD_PAINT.shader = shader

        SHADOW_PAINT.setShadowLayer(SHADOW_RADIUS / 2.0f, 0.0f, 0.0f, SHADOW_COLOR)
        SHADOW_PAINT.isAntiAlias = true
        SHADOW_PAINT.isFilterBitmap = true
        SHADOW_PAINT.color = -0x1000000
        SHADOW_PAINT.style = Paint.Style.FILL
    }

    /**
     * A Bitmap associated with its last modification date. This can be used to check
     * whether the book covers should be downloaded again.
     */
    class ExpiringBitmap {
        var bitmap: Bitmap? = null
        var lastModified: Calendar? = null
    }

    /**
     * Deletes the specified drawable from the cache. Calling this method will remove
     * the drawable from the in-memory cache and delete the corresponding file from the
     * external storage.
     *
     * @param id The id of the drawable to delete from the cache
     */
    @JvmStatic
    fun deleteCachedCover(id: String) {
        File(ImportUtilities.getCacheDirectory(), id).delete()
        sArtCache.remove(id)
    }

    /**
     * Retrieves a drawable from the book covers cache, identified by the specified id.
     * If the drawable does not exist in the cache, it is loaded and added to the cache.
     * If the drawable cannot be added to the cache, the specified default drwaable is
     * returned.
     *
     * @param id The id of the drawable to retrieve
     * @param defaultCover The default drawable returned if no drawable can be found that
     * matches the id
     *
     * @return The drawable identified by id or defaultCover
     */
    @JvmStatic
    fun getCachedCover(id: String, defaultCover: FastBitmapDrawable?): FastBitmapDrawable? {
        var drawable: FastBitmapDrawable? = null

        val reference = sArtCache[id]
        if (reference != null) {
            drawable = reference.get()
        }

        if (drawable == null) {
            val bitmap = loadCover(id)
            if (bitmap != null) {
                drawable = FastBitmapDrawable(bitmap)
            } else {
                drawable = NULL_DRAWABLE
            }

            sArtCache[id] = SoftReference(drawable)
        }

        return if (drawable === NULL_DRAWABLE) defaultCover else drawable
    }

    /**
     * Removes all the callbacks from the drawables stored in the memory cache. This
     * method must be called from the onDestroy() method of any activity using the
     * cached drawables. Failure to do so will result in the entire activity being
     * leaked.
     */
    @JvmStatic
    fun cleanupCache() {
        for (reference in sArtCache.values) {
            val drawable = reference.get()
            drawable?.callback = null
        }
    }

    /**
     * Loads an image from the specified URL.
     *
     * @param url The URL of the image to load.
     *
     * @return The image at the specified URL or null if an error occured.
     */
    @JvmStatic
    fun load(url: String): ExpiringBitmap {
        return load(url, null)
    }

    /**
     * Loads an image from the specified URL with the specified cookie.
     *
     * @param url The URL of the image to load.
     * @param cookie The cookie to use to load the image.
     *
     * @return The image at the specified URL or null if an error occured.
     */
    @JvmStatic
    fun load(url: String, cookie: String?): ExpiringBitmap {
        val expiring = ExpiringBitmap()

        val get = HttpGet(url)
        if (cookie != null) get.setHeader("cookie", cookie)

        var entity: HttpEntity? = null
        try {
            val response = HttpManager.execute(get)
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                setLastModified(expiring, response)

                entity = response.entity

                var `in`: InputStream? = null
                var out: OutputStream? = null

                try {
                    `in` = entity.content
                    if (FLAG_DECODE_BITMAP_WITH_SKIA) {
                        expiring.bitmap = BitmapFactory.decodeStream(`in`)
                    } else {
                        val dataStream = ByteArrayOutputStream()
                        out = BufferedOutputStream(dataStream, IOUtilities.IO_BUFFER_SIZE)
                        IOUtilities.copy(`in`, out)
                        out.flush()

                        val data = dataStream.toByteArray()
                        expiring.bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    }
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "Could not load image from $url", e)
                } finally {
                    IOUtilities.closeStream(`in`)
                    IOUtilities.closeStream(out)
                }
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not load image from $url", e)
        } finally {
            if (entity != null) {
                try {
                    entity.consumeContent()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "Could not load image from $url", e)
                }
            }
        }

        return expiring
    }

    private fun setLastModified(expiring: ExpiringBitmap, response: HttpResponse) {
        expiring.lastModified = null

        val header = response.getFirstHeader("Last-Modified")
        if (header == null) return

        if (sLastModifiedFormat == null) {
            sLastModifiedFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
        }

        val calendar = GregorianCalendar.getInstance()
        try {
            calendar.time = sLastModifiedFormat!!.parse(header.value)
            expiring.lastModified = calendar
        } catch (e: ParseException) {
            // Ignore
        }
    }

    /**
     * Return the same image with a shadow, scaled by the specified amount..
     *
     * @param bitmap The bitmap to decor with a shadow
     * @param width The target width of the decored bitmap
     * @param height The target height of the decored bitmap
     *
     * @return A new Bitmap based on the original bitmap
     */
    @JvmStatic
    fun createShadow(bitmap: Bitmap?, width: Int, height: Int): Bitmap? {
        if (bitmap == null) return null

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val scale = Math.min(
            width.toFloat() / bitmapWidth.toFloat(),
            height.toFloat() / bitmapHeight.toFloat()
        )

        val scaledWidth = (bitmapWidth * scale).toInt()
        val scaledHeight = (bitmapHeight * scale).toInt()

        return createScaledBitmap(
            bitmap, scaledWidth, scaledHeight,
            SHADOW_RADIUS, false, SHADOW_PAINT
        )
    }

    /**
     * Create a book cover with the specified bitmap. This method applies several
     * lighting effects to the original bitmap and returns a new decored bitmap.
     *
     * @param bitmap The bitmap to decor with lighting effects
     * @param width The target width of the decored bitmap
     * @param height The target height of the decored bitmap
     *
     * @return A new Bitmap based on the original bitmap
     */
    @JvmStatic
    fun createBookCover(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        val scale = Math.min(
            width.toFloat() / bitmapWidth.toFloat(),
            height.toFloat() / bitmapHeight.toFloat()
        )

        val scaledWidth = (bitmapWidth * scale).toInt()
        val scaledHeight = (bitmapHeight * scale).toInt()

        val decored = createScaledBitmap(
            bitmap, scaledWidth, scaledHeight,
            SHADOW_RADIUS, true, SHADOW_PAINT
        )
        val canvas = Canvas(decored)

        canvas.translate(SHADOW_RADIUS / 2.0f, SHADOW_RADIUS / 2.0f)
        canvas.drawRect(EDGE_START, 0.0f, EDGE_END, scaledHeight.toFloat(), EDGE_PAINT)
        canvas.drawRect(FOLD_START, 0.0f, FOLD_END, scaledHeight.toFloat(), FOLD_PAINT)
        //noinspection PointlessArithmeticExpression
        canvas.translate(scaledWidth - (EDGE_END - EDGE_START), 0.0f)
        canvas.drawRect(EDGE_START, 0.0f, EDGE_END, scaledHeight.toFloat(), END_EDGE_PAINT)

        return decored
    }

    private fun loadCover(id: String): Bitmap? {
        val file = File(ImportUtilities.getCacheDirectory(), id)
        if (file.exists()) {
            var stream: InputStream? = null
            try {
                stream = FileInputStream(file)
                return BitmapFactory.decodeStream(stream, null, null)
            } catch (e: FileNotFoundException) {
                // Ignore
            } finally {
                IOUtilities.closeStream(stream)
            }
        }
        return null
    }

    private fun createScaledBitmap(
        src: Bitmap, dstWidth: Int, dstHeight: Int,
        offset: Float, clipShadow: Boolean, paint: Paint?
    ): Bitmap {

        var m: Matrix?
        synchronized(Bitmap::class.java) {
            m = sScaleMatrix
            sScaleMatrix = null
        }

        if (m == null) {
            m = Matrix()
        }

        val width = src.width
        val height = src.height
        val sx = dstWidth / width.toFloat()
        val sy = dstHeight / height.toFloat()
        m!!.setScale(sx, sy)

        val b = createBitmap(src, 0, 0, width, height, m, offset, clipShadow, paint)

        synchronized(Bitmap::class.java) {
            sScaleMatrix = m
        }

        return b
    }

    private fun createBitmap(
        source: Bitmap, x: Int, y: Int, width: Int,
        height: Int, m: Matrix?, offset: Float, clipShadow: Boolean, paint: Paint?
    ): Bitmap {
        var paint = paint

        var scaledWidth = width
        var scaledHeight = height

        val canvas = Canvas()
        canvas.translate(offset / 2.0f, offset / 2.0f)

        val bitmap: Bitmap

        val from = Rect(x, y, x + width, y + height)
        val to = RectF(0f, 0f, width.toFloat(), height.toFloat())

        if (m == null || m.isIdentity) {
            bitmap = Bitmap.createBitmap(
                scaledWidth + offset.toInt(),
                scaledHeight + (if (clipShadow) (offset / 2.0f) else offset).toInt(),
                Bitmap.Config.ARGB_8888
            )
            paint = null
        } else {
            val mapped = RectF()
            m.mapRect(mapped, to)

            scaledWidth = Math.round(mapped.width())
            scaledHeight = Math.round(mapped.height())

            bitmap = Bitmap.createBitmap(
                scaledWidth + offset.toInt(),
                scaledHeight + (if (clipShadow) (offset / 2.0f) else offset).toInt(),
                Bitmap.Config.ARGB_8888
            )
            canvas.translate(-mapped.left, -mapped.top)
            canvas.concat(m)
        }

        canvas.setBitmap(bitmap)
        if (paint != null) {
            canvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
        }
        canvas.drawBitmap(source, from, to, SCALE_PAINT)

        return bitmap
    }
}
