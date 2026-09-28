/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.Intent
import android.graphics.*
import android.media.FaceDetector
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import intellibitz.intellidroid.R
import intellibitz.intellidroid.camera.gallery.IImage
import intellibitz.intellidroid.camera.gallery.IImageList
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.CountDownLatch

/**
 * The activity can crop specific region of interest from an image.
 */
class CropImage : MonitoredActivity() {
    companion object {
        private const val TAG = "CropImage"
    }

    private val mHandler = Handler()
    var mWaitingToPick: Boolean = false // Whether we are wait the user to pick a face.
    var mSaving: Boolean = false // Whether the "save" button is already clicked.
    var mCrop: HighlightView? = null
    // These are various options can be specified in the intent.
    private var mOutputFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG // only used with mSaveUri
    private var mOutputQuality: Int = 100 // only used with mSaveUri and JPEG format
    private var mSaveUri: Uri? = null
    private var mSetWallpaper: Boolean = false
    private var mAspectX: Int = 0
    private var mAspectY: Int = 0
    private var mDoFaceDetection: Boolean = true
    private var mCircleCrop: Boolean = false
    // These options specifiy the output image size and whether we should
    // scale the output to fit it (or just crop it).
    private var mOutputX: Int = 0
    private var mOutputY: Int = 0
    private var mScale: Boolean = false
    private var mScaleUp: Boolean = true
    private lateinit var mImageView: CropImageView
    private lateinit var mContentResolver: ContentResolver
    private var mBitmap: Bitmap? = null
    private var mAllImages: IImageList? = null
    private var mImage: IImage? = null

    private var mOutlineColor: Int = 0
    private var mOutlineCircleColor: Int = 0
    private val mRunFaceDetection = object : Runnable {
        @SuppressWarnings("hiding")
        var mScale: Float = 1F
        var mImageMatrix: Matrix? = null
        var mFaces: Array<FaceDetector.Face?> = arrayOfNulls(3)
        var mNumFaces: Int = 0

        // For each face, we createFileInES a HightlightView for it.
        private fun handleFace(f: FaceDetector.Face) {
            val midPoint = PointF()

            val r = ((f.eyesDistance() * mScale).toInt()) * 2
            f.getMidPoint(midPoint)
            midPoint.x *= mScale
            midPoint.y *= mScale

            val midX = midPoint.x.toInt()
            val midY = midPoint.y.toInt()

            val hv = HighlightView(mImageView, mOutlineColor, mOutlineCircleColor)

            val width = mBitmap!!.width
            val height = mBitmap!!.height

            val imageRect = Rect(0, 0, width, height)

            val faceRect = RectF(midX.toFloat(), midY.toFloat(), midX.toFloat(), midY.toFloat())
            faceRect.inset(-r.toFloat(), -r.toFloat())
            if (faceRect.left < 0) {
                faceRect.inset(-faceRect.left, -faceRect.left)
            }

            if (faceRect.top < 0) {
                faceRect.inset(-faceRect.top, -faceRect.top)
            }

            if (faceRect.right > imageRect.right) {
                faceRect.inset(faceRect.right - imageRect.right,
                        faceRect.right - imageRect.right)
            }

            if (faceRect.bottom > imageRect.bottom) {
                faceRect.inset(faceRect.bottom - imageRect.bottom,
                        faceRect.bottom - imageRect.bottom)
            }

            hv.setup(mImageMatrix!!, imageRect, faceRect, mCircleCrop,
                    mAspectX != 0 && mAspectY != 0)

            mImageView.add(hv)
        }

        // Create a default HightlightView if we found no face in the picture.
        private fun makeDefault() {
            val hv = HighlightView(mImageView, mOutlineColor, mOutlineCircleColor)

            val width = mBitmap!!.width
            val height = mBitmap!!.height

            val imageRect = Rect(0, 0, width, height)

            // make the default size about 4/5 of the width or height
            var cropWidth = Math.min(width, height) * 4 / 5
            var cropHeight = cropWidth

            if (mAspectX != 0 && mAspectY != 0) {
                if (mAspectX > mAspectY) {
                    cropHeight = cropWidth * mAspectY / mAspectX
                } else {
                    cropWidth = cropHeight * mAspectX / mAspectY
                }
            }

            val x = (width - cropWidth) / 2
            val y = (height - cropHeight) / 2

            val cropRect = RectF(x.toFloat(), y.toFloat(), (x + cropWidth).toFloat(), (y + cropHeight).toFloat())
            hv.setup(mImageMatrix!!, imageRect, cropRect, mCircleCrop,
                    mAspectX != 0 && mAspectY != 0)
            mImageView.add(hv)
        }

        // Scale the image down for faster face detection.
        private fun prepareBitmap(): Bitmap? {
            if (mBitmap == null || mBitmap!!.isRecycled) {
                return null
            }

            // 256 pixels wide is enough.
            if (mBitmap!!.width > 256) {
                mScale = 256.0F / mBitmap!!.width
            }
            val matrix = Matrix()
            matrix.setScale(mScale, mScale)
            val faceBitmap = Bitmap.createBitmap(mBitmap!!, 0, 0, mBitmap!!
                    .width, mBitmap!!.height, matrix, true)
            return faceBitmap
        }

        override fun run() {
            mImageMatrix = mImageView.imageMatrix
            val faceBitmap = prepareBitmap()

            mScale = 1.0F / mScale
            if (faceBitmap != null && mDoFaceDetection) {
                val detector = FaceDetector(faceBitmap.width,
                        faceBitmap.height, mFaces.size)
                mNumFaces = detector.findFaces(faceBitmap, mFaces)
            }

            if (faceBitmap != null && faceBitmap != mBitmap) {
                faceBitmap.recycle()
            }

            mHandler.post(object : Runnable {
                override fun run() {
                    mWaitingToPick = mNumFaces > 1
                    if (mNumFaces > 0) {
                        for (i in 0 until mNumFaces) {
                            handleFace(mFaces[i]!!)
                        }
                    } else {
                        makeDefault()
                    }
                    mImageView.invalidate()
                    if (mImageView.mHighlightViews.size == 1) {
                        mCrop = mImageView.mHighlightViews[0]
                        mCrop!!.isFocus = true
                    }

                    if (mNumFaces > 1) {
                        val t = Toast.makeText(this@CropImage,
                                R.string.multiface_crop_help,
                                Toast.LENGTH_SHORT)
                        t.show()
                    }
                }
            })
        }
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        mContentResolver = contentResolver

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.cropimage)

        mImageView = findViewById(R.id.image) as CropImageView

        // Work-around for devices incapable of using hardware-accelerated clipPath.
        // (android.view.GLES20Canvas.clipPath)
        //
        // See also:
        // - https://code.google.com/p/android/issues/detail?id=20474
        // - https://github.com/lvillani/android-cropimage/issues/20
        //
        if (Build.VERSION.SDK_INT > 10 && Build.VERSION.SDK_INT < 16) { // >= Gingerbread && < Jelly Bean
            mImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        val intent = intent
        val extras = intent.extras

        if (extras != null) {
            if (extras.getBoolean("circleCrop", false)) {
                mCircleCrop = true
                mAspectX = 1
                mAspectY = 1
                mOutputFormat = Bitmap.CompressFormat.PNG
            }
            mSaveUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT)
            if (mSaveUri != null) {
                val outputFormatString = extras.getString("outputFormat")
                if (outputFormatString != null) {
                    mOutputFormat = Bitmap.CompressFormat.valueOf(
                            outputFormatString)
                }
                mOutputQuality = extras.getInt("outputQuality", 100)
            } else {
                mSetWallpaper = extras.getBoolean("setWallpaper")
            }
            mBitmap = extras.getParcelable("data")
            mAspectX = extras.getInt("aspectX")
            mAspectY = extras.getInt("aspectY")
            mOutputX = extras.getInt("outputX")
            mOutputY = extras.getInt("outputY")
            mOutlineColor = extras.getInt("outlineColor", HighlightView.DEFAULT_OUTLINE_COLOR)
            mOutlineCircleColor = extras.getInt("outlineCircleColor", HighlightView.DEFAULT_OUTLINE_CIRCLE_COLOR)
            mScale = extras.getBoolean("scale", true)
            mScaleUp = extras.getBoolean("scaleUpIfNeeded", true)
            mDoFaceDetection = if (extras.containsKey("noFaceDetection"))
                !extras.getBoolean("noFaceDetection")
            else
                true
        }

        if (mBitmap == null) {
            val target = intent.data
            mAllImages = ImageManager.makeImageList(mContentResolver, target,
                    ImageManager.SORT_ASCENDING)
            mImage = mAllImages?.getImageForUri(target)
            if (mImage != null) {
                // Don't read in really large bitmaps. Use the (big) thumbnail
                // instead.
                // TODO when saving the resulting bitmap use the
                // decode/crop/encode api so we don't lose any resolution.
                mBitmap = mImage!!.thumbBitmap(IImage.ROTATE_AS_NEEDED)
            }
        }

        if (mBitmap == null) {
            finish()
            return
        }

        // Make UI fullscreen.
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        findViewById<View>(R.id.discard).setOnClickListener(
                object : View.OnClickListener {
                    override fun onClick(v: View) {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                })

        findViewById<View>(R.id.save).setOnClickListener(
                object : View.OnClickListener {
                    override fun onClick(v: View) {
                        onSaveClicked()
                    }
                })

        startFaceDetection()
    }

    private fun startFaceDetection() {
        if (isFinishing) {
            return
        }

        mImageView.setImageBitmapResetBase(mBitmap!!, true)

        Util.startBackgroundJob(this, null,
                resources.getString(R.string.runningFaceDetection),
                object : Runnable {
                    override fun run() {
                        val latch = CountDownLatch(1)
                        val b = if (mImage != null)
                            mImage!!.fullSizeBitmap(IImage.UNCONSTRAINED,
                                    1024 * 1024)
                        else
                            mBitmap
                        mHandler.post(object : Runnable {
                            override fun run() {
                                if (b != mBitmap && b != null) {
                                    mImageView.setImageBitmapResetBase(b, true)
                                    mBitmap!!.recycle()
                                    mBitmap = b
                                }
                                if (mImageView.scale == 1F) {
                                    mImageView.center(true, true)
                                }
                                latch.countDown()
                            }
                        })
                        try {
                            latch.await()
                        } catch (e: InterruptedException) {
                            throw RuntimeException(e)
                        }
                        mRunFaceDetection.run()
                    }
                }, mHandler)
    }

    private fun onSaveClicked() {
        // TODO this code needs to change to use the decode/crop/encode single
        // step api so that we don't require that the whole (possibly large)
        // bitmap doesn't have to be read into memory
        if (mCrop == null) {
            return
        }

        if (mSaving) return
        mSaving = true

        val croppedImage: Bitmap

        // If the output is required to a specific size, createFileInES an new image
        // with the cropped image in the center and the extra space filled.
        if (mOutputX != 0 && mOutputY != 0 && !mScale) {
            // Don't scale the image but instead fill it so it's the
            // required dimension
            croppedImage = Bitmap.createBitmap(mOutputX, mOutputY,
                    Bitmap.Config.RGB_565)
            val canvas = Canvas(croppedImage)

            val srcRect = mCrop!!.cropRect
            val dstRect = Rect(0, 0, mOutputX, mOutputY)

            val dx = (srcRect.width() - dstRect.width()) / 2
            val dy = (srcRect.height() - dstRect.height()) / 2

            // If the srcRect is too big, use the center part of it.
            srcRect.inset(Math.max(0, dx), Math.max(0, dy))

            // If the dstRect is too big, use the center part of it.
            dstRect.inset(Math.max(0, -dx), Math.max(0, -dy))

            // Draw the cropped bitmap in the center
            canvas.drawBitmap(mBitmap!!, srcRect, dstRect, null)

            // Release bitmap memory as soon as possible
            mImageView.clear()
            mBitmap!!.recycle()
        } else {
            val r = mCrop!!.cropRect

            val width = r.width()
            val height = r.height()

            // If we are circle cropping, we want alpha channel, which is the
            // third param here.
            croppedImage = Bitmap.createBitmap(width, height,
                    if (mCircleCrop)
                        Bitmap.Config.ARGB_8888
                    else
                        Bitmap.Config.RGB_565)

            val canvas = Canvas(croppedImage)
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(mBitmap!!, r, dstRect, null)

            // Release bitmap memory as soon as possible
            mImageView.clear()
            mBitmap!!.recycle()

            if (mCircleCrop) {
                // OK, so what's all this about?
                // Bitmaps are inherently rectangular but we want to return
                // something that's basically a circle.  So we fill in the
                // area around the circle with alpha.  Note the all important
                // PortDuff.Mode.CLEAR.
                val c = Canvas(croppedImage)
                val p = Path()
                p.addCircle(width / 2F, height / 2F, width / 2F,
                        Path.Direction.CW)
                c.clipPath(p, Region.Op.DIFFERENCE)
                c.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
            }

            // If the required dimension is specified, scale the image.
            if (mOutputX != 0 && mOutputY != 0 && mScale) {
                val croppedImageTemp = Util.transform(Matrix(), croppedImage,
                        mOutputX, mOutputY, mScaleUp, Util.RECYCLE_INPUT)
                croppedImage.recycle()
                croppedImage = croppedImageTemp
            }
        }

        mImageView.setImageBitmapResetBase(croppedImage, true)
        mImageView.center(true, true)
        mImageView.mHighlightViews.clear()

        // Return the cropped image directly or save it to the specified URI.
        val myExtras = intent.extras
        if (myExtras != null && (myExtras.getParcelable<Any>("data") != null
                        || myExtras.getBoolean("return-data"))) {
            val extras = Bundle()
            extras.putParcelable("data", croppedImage)
            setResult(RESULT_OK,
                    Intent().setAction("inline-data").putExtras(extras))
            finish()
        } else {
            val b = croppedImage
            val msdId = if (mSetWallpaper)
                R.string.wallpaper
            else
                R.string.savingImage
            Util.startBackgroundJob(this, null,
                    resources.getString(msdId),
                    object : Runnable {
                        override fun run() {
                            saveOutput(b)
                        }
                    }, mHandler)
        }
    }

    private fun saveOutput(croppedImage: Bitmap) {
        if (mSaveUri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = mContentResolver.openOutputStream(mSaveUri!!)
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, mOutputQuality, outputStream)
                }
            } catch (ex: IOException) {
                // TODO: report error to caller
                Log.e(TAG, "Cannot open file: " + mSaveUri, ex)
            } finally {
                Util.closeSilently(outputStream)
            }
            val extras = Bundle()
            setResult(RESULT_OK, Intent(mSaveUri.toString())
                    .putExtras(extras))
        } else if (mSetWallpaper) {
            try {
                WallpaperManager.getInstance(this).setBitmap(croppedImage)
                setResult(RESULT_OK)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to set wallpaper.", e)
                setResult(RESULT_CANCELED)
            }
        } else {
            val extras = Bundle()
            extras.putString("rect", mCrop!!.cropRect.toString())

            val oldPath = File(mImage!!.dataPath)
            val directory = File(oldPath.parent)

            var x = 0
            var fileName = oldPath.name
            fileName = fileName.substring(0, fileName.lastIndexOf("."))

            // Try file-1.jpg, file-2.jpg, ... until we find a filename which
            // does not exist yet.
            while (true) {
                x += 1
                val candidate = directory.toString()
                        + "/" + fileName + "-" + x + ".jpg"
                val exists = File(candidate).exists()
                if (!exists) {
                    break
                }
            }

            try {
                val degree = intArrayOf(0)
                val newUri = ImageManager.addImage(
                        mContentResolver,
                        mImage!!.title,
                        mImage!!.dateTaken,
                        null,    // TODO this null is going to cause us to lose
                        // the location (gps).
                        directory.toString(), fileName + "-" + x + ".jpg",
                        croppedImage, null,
                        degree)

                setResult(RESULT_OK, Intent()
                        .setAction(newUri.toString())
                        .putExtras(extras))
            } catch (ex: Exception) {
                // basically ignore this or put up
                // some ui saying we failed
                Log.e(TAG, "store image fail, continue anyway", ex)
            }
        }

        val b = croppedImage
        mHandler.post(object : Runnable {
            override fun run() {
                mImageView.clear()
                b.recycle()
            }
        })

        finish()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        mAllImages?.close()
        super.onDestroy()
    }
}
