package intellibitz.intellidroid.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import intellibitz.intellidroid.camera.CropImageIntentBuilder
import java.io.File
import java.io.IOException

@Suppress("UnusedDeclaration")
object MediaPicker {

    const val NAME = "media_picker"

    @JvmStatic
    fun openMediaChooser(activity: Activity, title: String, result: OnError) {
        openMediaChooser(activity, null, title, result)
    }

    @JvmStatic
    fun openMediaChooser(activity: Activity, @StringRes title: Int, result: OnError) {
        openMediaChooser(activity, activity.getString(title), result)
    }

    @JvmStatic
    fun openMediaChooser(fragment: Fragment, title: String, result: OnError) {
        openMediaChooser(null, fragment, title, result)
    }

    @JvmStatic
    fun openMediaChooser(fragment: Fragment, @StringRes title: Int, result: OnError) {
        openMediaChooser(fragment, fragment.getString(title), result)
    }

    private fun openMediaChooser(activity: Activity?, fragment: Fragment?, title: String, result: OnError) {
        try {
            val context = activity ?: fragment?.context ?: return
            val captureFileURI = createTempImageFileAndPersistUri(activity, fragment)
            val intent = MediaPickerChooser.getMediaChooserIntent(context.packageManager, title, captureFileURI)
            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_CHOOSER.code)
        } catch (e: IOException) {
            result.onError(e)
        }
    }

    @JvmStatic
    fun startForCamera(activity: Activity, result: OnError) {
        startForCamera(activity, null, result)
    }

    @JvmStatic
    fun startForCamera(fragment: Fragment, result: OnError) {
        startForCamera(null, fragment, result)
    }

    private fun startForCamera(activity: Activity?, fragment: Fragment?, result: OnError) {
        try {
            val captureFileURI = createTempImageFileAndPersistUri(activity, fragment)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI)
            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_CAPTURE.code)
        } catch (e: IOException) {
            result.onError(e)
        }
    }

    @JvmStatic
    fun startForGallery(activity: Activity, result: OnError) {
        startForGallery(activity, null, result)
    }

    @JvmStatic
    fun startForGallery(fragment: Fragment, result: OnError) {
        startForGallery(null, fragment, result)
    }

    private fun startForGallery(activity: Activity?, fragment: Fragment?, result: OnError) {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_GALLERY.code)
        } catch (e: IOException) {
            result.onError(e)
        }
    }

    @JvmStatic
    fun startForDocuments(fragment: Fragment, result: OnError) {
        startForDocuments(null, fragment, result)
    }

    @JvmStatic
    fun startForDocuments(activity: Activity, result: OnError) {
        startForDocuments(activity, null, result)
    }

    private fun startForDocuments(activity: Activity?, fragment: Fragment?, result: OnError) {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_DOCUMENTS.code)
        } catch (e: IOException) {
            result.onError(e)
        }
    }

    @JvmStatic
    fun startForImageCrop(fragment: Fragment, uri: Uri, outputWidth: Int, outputHeight: Int, colorInt: Int, result: OnError) {
        startForImageCrop(null, fragment, uri, outputWidth, outputHeight, colorInt, result)
    }

    @JvmStatic
    fun startForImageCrop(fragment: Fragment, file: File, outputWidth: Int, outputHeight: Int, colorInt: Int, result: OnError) {
        startForImageCrop(fragment, Uri.fromFile(file), outputWidth, outputHeight, colorInt, result)
    }

    @JvmStatic
    fun startForImageCrop(activity: Activity, uri: Uri, outputWidth: Int, outputHeight: Int, colorInt: Int, result: OnError) {
        startForImageCrop(activity, null, uri, outputWidth, outputHeight, colorInt, result)
    }

    @JvmStatic
    fun startForImageCrop(activity: Activity, file: File, outputWidth: Int, outputHeight: Int, colorInt: Int, result: OnError) {
        startForImageCrop(activity, null, Uri.fromFile(file), outputWidth, outputHeight, colorInt, result)
    }

    private fun startForImageCrop(
        activity: Activity?,
        fragment: Fragment?,
        uri: Uri,
        outputWidth: Int,
        outputHeight: Int,
        colorInt: Int,
        result: OnError
    ) {
        try {
            val captureFileURI = createTempImageFileAndPersistUri(activity, fragment)
            val intentBuilder = CropImageIntentBuilder(outputWidth, outputHeight, captureFileURI)
            intentBuilder.setSourceImage(uri)
            intentBuilder.setDoFaceDetection(false)
            intentBuilder.setOutlineCircleColor(colorInt)
            intentBuilder.setOutlineColor(colorInt)
            intentBuilder.setScaleUpIfNeeded(true)

            val context = activity ?: fragment?.context ?: return
            startFor(activity, fragment, intentBuilder.getIntent(context), MediaPickerRequest.REQUEST_CROP.code)
        } catch (e: IOException) {
            result.onError(e)
        }
    }

    @Throws(IOException::class)
    private fun startFor(activity: Activity?, fragment: Fragment?, intent: Intent, requestCode: Int) {
        try {
            activity?.startActivityForResult(intent, requestCode)
            fragment?.startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            throw IOException("No application available for media picker.")
        }
    }

    @JvmStatic
    fun handleActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?, result: OnResult) {
        val request = MediaPickerRequest.create(requestCode) ?: return

        try {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val uri = handleActivityUriResult(context, request, data)
                    result.onSuccess(MediaPickerUri.resolveToFile(context, uri), request)
                }
                Activity.RESULT_CANCELED -> {
                    result.onCancelled()
                }
                else -> {
                    throw IOException("Bad activity result code: $resultCode, for request code: $requestCode")
                }
            }
        } catch (e: IOException) {
            result.onError(e)
        }
    }

    @Throws(IOException::class)
    private fun handleActivityUriResult(context: Context, request: MediaPickerRequest, data: Intent?): Uri? {
        return when (request) {
            MediaPickerRequest.REQUEST_CAPTURE,
            MediaPickerRequest.REQUEST_CROP -> {
                getCaptureFileUriAndClear(context)
            }
            MediaPickerRequest.REQUEST_CHOOSER -> {
                if (data != null && data.data != null) {
                    data.data
                } else {
                    getCaptureFileUriAndClear(context)
                }
            }
            MediaPickerRequest.REQUEST_DOCUMENTS,
            MediaPickerRequest.REQUEST_GALLERY -> {
                if (data == null || data.data == null) {
                    throw IOException("Picker returned no data result.")
                }
                data.data
            }
            else -> throw IOException("Picker returned unknown request.")
        }
    }

    @Throws(IOException::class)
    private fun createTempImageFileAndPersistUri(activity: Activity?, fragment: Fragment?): Uri {
        val context = activity ?: fragment?.context ?: throw IOException("Context is null")
        val captureFileURI = Uri.fromFile(MediaPickerFile.createWithSuffix(".jpg"))
        persistUri(context, captureFileURI.toString())
        return captureFileURI
    }

    private fun persistUri(context: Context, uri: String?) {
        val sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(NAME, uri)
        editor.apply()
    }

    private fun getCaptureFileUriAndClear(context: Context): Uri? {
        val uriString = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(NAME, null)
        if (uriString != null) {
            persistUri(context, null)
            return Uri.parse(uriString)
        }
        return null
    }

    fun interface OnError {
        fun onError(e: IOException)
    }

    interface OnResult : OnError {
        fun onSuccess(mediaFile: File, request: MediaPickerRequest)
        fun onCancelled()

        abstract class Default : OnResult {
            override fun onCancelled() {}
            override fun onError(e: IOException) {}
        }
    }
}
