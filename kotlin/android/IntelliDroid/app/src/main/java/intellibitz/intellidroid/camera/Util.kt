/*
 * Copyright (C) 2009 The Android Open Source Project
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

package intellibitz.intellidroid.camera

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import intellibitz.intellidroid.camera.gallery.IImage
import java.io.Closeable
import java.io.FileDescriptor
import java.io.IOException

/**
 * Collection of utility functions used in this package.
 */
class Util private constructor() {
    companion object {
        const val DIRECTION_LEFT = 0
        const val DIRECTION_RIGHT = 1
        const val DIRECTION_UP = 2
        const val DIRECTION_DOWN = 3
        // Whether we should recycle the input (unless the output is the input).
        const val RECYCLE_INPUT = true
        const val NO_RECYCLE_INPUT = false
        private const val TAG = "Util"
        private var sNullOnClickListener: OnClickListener? = null

        // Rotates the bitmap by the specified degree.
        // If a new bitmap is created, the original bitmap is recycled.
        fun rotate(b: Bitmap?, degrees: Int): Bitmap? {
            if (degrees != 0 && b != null) {
                val m = Matrix()
                m.setRotate(degrees.toFloat(), (b.width / 2).toFloat(), (b.height / 2).toFloat())
                try {
                    val b2 = Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
                    if (b != b2) {
                        b.recycle()
                        return b2
                    }
                } catch (ex: OutOfMemoryError) {
                    // We have no memory to rotate. Return the original bitmap.
                }
            }
            return b
        }

        /*
         * Compute the sample size as a function of minSideLength
         * and maxNumOfPixels.
         * minSideLength is used to specify that minimal width or height of a
         * bitmap.
         * maxNumOfPixels is used to specify the maximal size in pixels that is
         * tolerable in terms of memory usage.
         *
         * The function returns a sample size based on the constraints.
         * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
         * which indicates no care of the corresponding constraint.
         * The functions prefers returning a sample size that
         * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
         *
         * Also, the function rounds up the sample size to a power of 2 or multiple
         * of 8 because BitmapFactory only honors sample size this way.
         * For example, BitmapFactory downsamples an image by 2 even though the
         * request is 3. So we round up the sample size to avoid OOM.
         */
        fun computeSampleSize(options: BitmapFactory.Options, minSideLength: Int, maxNumOfPixels: Int): Int {
            val initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels)

            val roundedSize: Int
            if (initialSize <= 8) {
                roundedSize = 1
                var roundedSizeTemp = roundedSize
                while (roundedSizeTemp < initialSize) {
                    roundedSizeTemp = roundedSizeTemp shl 1
                }
                roundedSize = roundedSizeTemp
            } else {
                roundedSize = (initialSize + 7) / 8 * 8
            }

            return roundedSize
        }

        private fun computeInitialSampleSize(options: BitmapFactory.Options, minSideLength: Int, maxNumOfPixels: Int): Int {
            val w = options.outWidth.toDouble()
            val h = options.outHeight.toDouble()

            val lowerBound = if (maxNumOfPixels == IImage.UNCONSTRAINED) 1 else Math.ceil(Math.sqrt(w * h / maxNumOfPixels)).toInt()
            val upperBound = if (minSideLength == IImage.UNCONSTRAINED) 128 else Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength)).toInt()

            if (upperBound < lowerBound) {
                // return the larger one when there is no overlapping zone.
                return lowerBound
            }

            if ((maxNumOfPixels == IImage.UNCONSTRAINED) && (minSideLength == IImage.UNCONSTRAINED)) {
                return 1
            } else if (minSideLength == IImage.UNCONSTRAINED) {
                return lowerBound
            } else {
                return upperBound
            }
        }

        fun transform(scaler: Matrix?, source: Bitmap, targetWidth: Int, targetHeight: Int, scaleUp: Boolean, recycle: Boolean): Bitmap {
            val deltaX = source.width - targetWidth
            val deltaY = source.height - targetHeight
            if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
                /*
                 * In this case the bitmap is smaller, at least in one dimension,
                 * than the target.  Transform it by placing as much of the image
                 * as possible into the target and leaving the top/bottom or
                 * left/right (or both) black.
                 */
                val b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val c = Canvas(b2)

                val deltaXHalf = Math.max(0, deltaX / 2)
                val deltaYHalf = Math.max(0, deltaY / 2)
                val src = Rect(deltaXHalf, deltaYHalf, deltaXHalf + Math.min(targetWidth, source.width), deltaYHalf + Math.min(targetHeight, source.height))
                val dstX = (targetWidth - src.width()) / 2
                val dstY = (targetHeight - src.height()) / 2
                val dst = Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY)
                c.drawBitmap(source, src, dst, null)
                if (recycle) {
                    source.recycle()
                }
                return b2
            }
            val bitmapWidthF = source.width.toFloat()
            val bitmapHeightF = source.height.toFloat()

            val bitmapAspect = bitmapWidthF / bitmapHeightF
            val viewAspect = targetWidth.toFloat() / targetHeight

            var scalerVar = scaler
            if (bitmapAspect > viewAspect) {
                val scale = targetHeight / bitmapHeightF
                if (scale < .9F || scale > 1F) {
                    scalerVar?.setScale(scale, scale)
                } else {
                    scalerVar = null
                }
            } else {
                val scale = targetWidth / bitmapWidthF
                if (scale < .9F || scale > 1F) {
                    scalerVar?.setScale(scale, scale)
                } else {
                    scalerVar = null
                }
            }

            val b1: Bitmap
            if (scalerVar != null) {
                // this is used for minithumb and crop, so we want to filter here.
                b1 = Bitmap.createBitmap(source, 0, 0, source.width, source.height, scalerVar, true)
            } else {
                b1 = source
            }

            if (recycle && b1 != source) {
                source.recycle()
            }

            val dx1 = Math.max(0, b1.width - targetWidth)
            val dy1 = Math.max(0, b1.height - targetHeight)

            val b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight)

            if (b2 != b1) {
                if (recycle || b1 != source) {
                    b1.recycle()
                }
            }

            return b2
        }

        fun <T> indexOf(array: Array<T>, s: T): Int {
            for (i in array.indices) {
                if (array[i] == s) {
                    return i
                }
            }
            return -1
        }

        fun closeSilently(c: Closeable?) {
            c?.close()
        }

        fun closeSilently(c: ParcelFileDescriptor?) {
            c?.close()
        }

        /**
         * Make a bitmap from a given Uri.
         *
         * @param uri
         */
        fun makeBitmap(minSideLength: Int, maxNumOfPixels: Int, uri: Uri, cr: ContentResolver, useNative: Boolean): Bitmap? {
            var input: ParcelFileDescriptor? = null
            try {
                input = cr.openFileDescriptor(uri, "r")
                var options: BitmapFactory.Options? = null
                if (useNative) {
                    options = createNativeAllocOptions()
                }
                return makeBitmap(minSideLength, maxNumOfPixels, uri, cr, input, options)
            } catch (ex: IOException) {
                return null
            } finally {
                closeSilently(input)
            }
        }

        fun makeBitmap(minSideLength: Int, maxNumOfPixels: Int, pfd: ParcelFileDescriptor, useNative: Boolean): Bitmap? {
            var options: BitmapFactory.Options? = null
            if (useNative) {
                options = createNativeAllocOptions()
            }
            return makeBitmap(minSideLength, maxNumOfPixels, null, null, pfd, options)
        }

        fun makeBitmap(minSideLength: Int, maxNumOfPixels: Int, uri: Uri?, cr: ContentResolver?, pfd: ParcelFileDescriptor?, options: BitmapFactory.Options?): Bitmap? {
            try {
                var pfdVar = pfd
                if (pfdVar == null) pfdVar = makeInputStream(uri, cr)
                if (pfdVar == null) return null
                var optionsVar = options
                if (optionsVar == null) optionsVar = BitmapFactory.Options()

                val fd = pfdVar.fileDescriptor
                optionsVar.inJustDecodeBounds = true
                BitmapManager.instance().decodeFileDescriptor(fd, optionsVar)
                if (optionsVar.mCancel || optionsVar.outWidth == -1 || optionsVar.outHeight == -1) {
                    return null
                }
                optionsVar.inSampleSize = computeSampleSize(optionsVar, minSideLength, maxNumOfPixels)
                optionsVar.inJustDecodeBounds = false

                optionsVar.inDither = false
                optionsVar.inPreferredConfig = Bitmap.Config.ARGB_8888
                return BitmapManager.instance().decodeFileDescriptor(fd, optionsVar)
            } catch (ex: OutOfMemoryError) {
                Log.e(TAG, "Got oom exception ", ex)
                return null
            } finally {
                closeSilently(pfd)
            }
        }

        private fun makeInputStream(uri: Uri?, cr: ContentResolver?): ParcelFileDescriptor? {
            try {
                return cr?.openFileDescriptor(uri!!, "r")
            } catch (ex: IOException) {
                return null
            }
        }

        @Synchronized
        fun getNullOnClickListener(): OnClickListener {
            if (sNullOnClickListener == null) {
                sNullOnClickListener = OnClickListener { }
            }
            return sNullOnClickListener!!
        }

        fun Assert(cond: Boolean) {
            if (!cond) {
                throw AssertionError()
            }
        }

        fun equals(a: String?, b: String?): Boolean {
            // return true if both string are null or the content equals
            return a == b || a == b
        }

        fun startBackgroundJob(activity: MonitoredActivity, title: String, message: String, job: Runnable, handler: Handler) {
            // Make the progress dialog uncancelable, so that we can gurantee
            // the thread will be done before the activity getting destroyed.
            val dialog = ProgressDialog.show(activity, title, message, true, false)
            Thread(BackgroundJob(activity, job, dialog, handler)).start()
        }

        // Returns an intent which is used for "set as" menu items.
        fun createSetAsIntent(image: IImage): Intent {
            val u = image.fullSizeImageUri()
            val intent = Intent(Intent.ACTION_ATTACH_DATA)
            intent.setDataAndType(u, image.mimeType)
            intent.putExtra("mimeType", image.mimeType)
            return intent
        }

        // Returns Options that set the puregeable flag for Bitmap decode.
        fun createNativeAllocOptions(): BitmapFactory.Options {
            return BitmapFactory.Options()
        }
    }

    private class BackgroundJob private constructor(private val mActivity: MonitoredActivity, private val mJob: Runnable, private val mDialog: ProgressDialog, private val mHandler: Handler) : MonitoredActivity.LifeCycleAdapter(), Runnable {
        private val mCleanupRunner = Runnable {
            mActivity.removeLifeCycleListener(this@BackgroundJob)
            if (mDialog.window != null) mDialog.dismiss()
        }

        override fun run() {
            try {
                mJob.run()
            } finally {
                mHandler.post(mCleanupRunner)
            }
        }

        override fun onActivityDestroyed(activity: MonitoredActivity) {
            // We get here only when the onDestroyed being called before
            // the mCleanupRunner. So, run it now and remove it from the queue
            mCleanupRunner.run()
            mHandler.removeCallbacks(mCleanupRunner)
        }

        override fun onActivityStopped(activity: MonitoredActivity) {
            mDialog.hide()
        }

        override fun onActivityStarted(activity: MonitoredActivity) {
            mDialog.show()
        }
    }
}
