/*
 * Copyright (C) 2012 Lorenzo Villani.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellibitz.intellidroid.camera

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

/**
 * By default the following features are enabled, unless you override them by calling setters in the
 * builder:
 * <p>
 * <ul>
 * <li>Scale;</li>
 * <li>Scale up (if needed);</li>
 * <li>Face detection;</li>
 * </ul>
 *
 * @since 1.0.1
 */
// TODO: Circle Crop
// TODO: Set Wallpaper
class CropImageIntentBuilder(
    private val aspectX: Int,
    private val aspectY: Int,
    private val outputX: Int,
    private val outputY: Int,
    private val saveUri: Uri
) {
    constructor(outputX: Int, outputY: Int, saveUri: Uri) : this(DEFAULT_SCALE, DEFAULT_SCALE, outputX, outputY, saveUri)

    var scale: Boolean = true
    var scaleUpIfNeeded: Boolean = true
    var doFaceDetection: Boolean = true
    var circleCrop: Boolean = false
    var outputFormat: String? = null
    var outputQuality: Int = 100
    var sourceImage: Uri? = null
    var bitmap: Bitmap? = null
    var outlineColor: Int = HighlightView.DEFAULT_OUTLINE_COLOR
    var outlineCircleColor: Int = HighlightView.DEFAULT_OUTLINE_CIRCLE_COLOR

    /**
     * Builds the Intent.
     *
     * @param context The application context.
     * @return The newly created intent.
     * @since 1.0.1
     */
    fun getIntent(context: Context): Intent {
        val intent = Intent(context, CropImage::class.java)

        //
        // Required Intent extras.
        //

        intent.putExtra(EXTRA_ASPECT_X, aspectX)
        intent.putExtra(EXTRA_ASPECT_Y, aspectY)
        intent.putExtra(EXTRA_OUTPUT_X, outputX)
        intent.putExtra(EXTRA_OUTPUT_Y, outputY)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, saveUri)

        //
        // Optional Intent Extras
        //

        intent.putExtra(EXTRA_SCALE, scale)
        intent.putExtra(EXTRA_SCALE_UP_IF_NEEDED, scaleUpIfNeeded)
        intent.putExtra(EXTRA_NO_FACE_DETECTION, !doFaceDetection)
        intent.putExtra(EXTRA_CIRCLE_CROP, circleCrop)
        intent.putExtra(EXTRA_OUTPUT_FORMAT, outputFormat)
        intent.putExtra(EXTRA_OUTPUT_QUALITY, outputQuality)
        intent.putExtra(EXTRA_OUTLINE_COLOR, outlineColor)
        intent.putExtra(EXTRA_OUTLINE_CIRCLE_COLOR, outlineCircleColor)

        bitmap?.let { intent.putExtra(EXTRA_BITMAP_DATA, it) }

        sourceImage?.let { intent.data = it }

        return intent
    }

    /**
     * Set the quality for the output file when applicable. This is used for JPEG output
     * format for example.
     *
     * @param outputQuality The quality (0 is smallest file size, 100 is best quality)
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setOutputQuality(outputQuality: Int): CropImageIntentBuilder {
        this.outputQuality = outputQuality

        return this
    }

    /**
     * Scales down the picture.
     *
     * @param scale Whether to scale down the image.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setScale(scale: Boolean): CropImageIntentBuilder {
        this.scale = scale

        return this
    }

    /**
     * Whether to scale up the image if the cropped region is smaller than the output size.
     *
     * @param scaleUpIfNeeded Whether to scale up the image.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setScaleUpIfNeeded(scaleUpIfNeeded: Boolean): CropImageIntentBuilder {
        this.scaleUpIfNeeded = scaleUpIfNeeded

        return this
    }

    /**
     * Performs face detection before allowing users to crop the image.
     *
     * @param doFaceDetection Whether to perform face detection.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setDoFaceDetection(doFaceDetection: Boolean): CropImageIntentBuilder {
        this.doFaceDetection = doFaceDetection

        return this
    }

    /**
     * Sets bitmap data to crop. Please note that this method overrides any source image set by
     * {@link #setSourceImage(Uri)}.
     *
     * @param bitmap The {@link Bitmap} to crop.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setBitmap(bitmap: Bitmap): CropImageIntentBuilder {
        this.bitmap = bitmap

        return this
    }

    /**
     * Sets the Uri of the image to crop. It must be accessible to the calling application/activity.
     *
     * @param sourceImage Uri of the image to crop.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setSourceImage(sourceImage: Uri): CropImageIntentBuilder {
        this.sourceImage = sourceImage

        return this
    }

    /**
     * Whether to crop the image as circle
     *
     * @param circleCrop Whether to crop the image as circle.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setCircleCrop(circleCrop: Boolean): CropImageIntentBuilder {
        this.circleCrop = circleCrop

        return this
    }

    /**
     * Set output format of the image to crop. If not set, JPEG will be used.
     *
     * @param outputFormat Output format of the image to crop, such as JPEG, PNG or WEBP.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.0.1
     */
    fun setOutputFormat(outputFormat: String): CropImageIntentBuilder {
        this.outputFormat = outputFormat

        return this
    }

    /**
     * Set the color for the standard outline.
     *
     * @param color The color to use.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.1.0
     */
    fun setOutlineColor(color: Int): CropImageIntentBuilder {
        outlineColor = color

        return this
    }

    /**
     * Set the color for the face detection outline.
     *
     * @param color The color to use.
     * @return This Builder object to allow for chaining of calls to set methods.
     * @since 1.1.0
     */
    fun setOutlineCircleColor(color: Int): CropImageIntentBuilder {
        outlineCircleColor = color

        return this
    }

    companion object {
        private const val EXTRA_ASPECT_X = "aspectX"
        private const val EXTRA_ASPECT_Y = "aspectY"
        private const val EXTRA_OUTPUT_X = "outputX"
        private const val EXTRA_OUTPUT_Y = "outputY"
        private const val EXTRA_BITMAP_DATA = "data"
        private const val EXTRA_SCALE = "scale"
        private const val EXTRA_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded"
        private const val EXTRA_NO_FACE_DETECTION = "noFaceDetection"
        private const val EXTRA_CIRCLE_CROP = "circleCrop"
        private const val EXTRA_OUTPUT_FORMAT = "outputFormat"
        private const val EXTRA_OUTPUT_QUALITY = "outputQuality"
        private const val EXTRA_OUTLINE_COLOR = "outlineColor"
        private const val EXTRA_OUTLINE_CIRCLE_COLOR = "outlineCircleColor"

        private const val DEFAULT_SCALE = 1
    }
}
