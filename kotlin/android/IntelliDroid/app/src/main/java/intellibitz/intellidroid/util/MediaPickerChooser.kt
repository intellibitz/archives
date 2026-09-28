package intellibitz.intellidroid.util

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import java.io.IOException
import java.util.ArrayList
import java.util.LinkedHashMap

internal object MediaPickerChooser {

    /**
     * Gets an chooser intent that attempts to discover all activities on
     * the device that can be used to select media.
     *
     * @param packageManager Device [PackageManager].
     * @param chooserTitle   Title for the chooser.
     * @param captureFileURI Capture result URI for camera.
     * @return Chooser [Intent]
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun getMediaChooserIntent(
        packageManager: PackageManager,
        chooserTitle: String?,
        captureFileURI: Uri
    ): Intent {
        val intents = getMediaActivityIntents(packageManager, captureFileURI)
        if (intents.isEmpty()) {
            throw IOException("No media applications available on this device.")
        }
        val iterator = intents.iterator()
        val firstIntent = iterator.next()
        val remainingIntents = ArrayList<Intent>()
        while (iterator.hasNext()) {
            remainingIntents.add(iterator.next())
        }
        val chooserIntent = Intent.createChooser(firstIntent, chooserTitle)
        if (remainingIntents.isNotEmpty()) {
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                remainingIntents.toTypedArray<Parcelable>()
            )
        }
        return chooserIntent
    }

    /**
     * Find a collection of all matching media intents on the device.
     *
     * @param packageManager Device [PackageManager].
     * @param captureFileURI Capture result URI for camera.
     * @return Collection of media activity intents.
     */
    private fun getMediaActivityIntents(
        packageManager: PackageManager,
        captureFileURI: Uri
    ): Collection<Intent> {
        val typeCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val typeGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val typeDocuments = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")

        val intents: MutableMap<String, Intent> = LinkedHashMap()

        getIntentActivities(intents, packageManager, typeDocuments, null)
        getIntentActivities(intents, packageManager, typeGallery, null)
        getIntentActivities(intents, packageManager, typeCamera, object : IntentModifier {
            override fun onFoundIntent(intent: Intent) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI)
            }
        })

        return intents.values
    }

    /**
     * Given a filter, createFileInES a list of all matching activities and
     * return them as a list of intents.
     *
     * @param packageManager Device [PackageManager].
     * @param filterIntent   Filter by [Intent].
     * @param modifier       Callback to modify each matched [Intent].
     */
    private fun getIntentActivities(
        intents: MutableMap<String, Intent>,
        packageManager: PackageManager,
        filterIntent: Intent,
        modifier: IntentModifier?
    ) {
        for (resolveInfo in packageManager.queryIntentActivities(filterIntent, 0)) {
            val componentName = ComponentName(
                resolveInfo.activityInfo.packageName,
                resolveInfo.activityInfo.name
            )
            val intent = Intent(filterIntent)
            intent.component = componentName
            intent.setPackage(resolveInfo.activityInfo.packageName)
            modifier?.onFoundIntent(intent)
            intents[resolveInfo.activityInfo.packageName] = intent
        }
    }

    private interface IntentModifier {
        fun onFoundIntent(intent: Intent)
    }
}
