package intellibitz.intellidroid.task

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.view.View
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import java.io.IOException

/**
 * AsyncTAsk for Image Bitmap
 */
class BitmapFromUrlTask(
    private var textView: View?,
    private var url: String?,
    private var context: Context?
) : AsyncTask<Void?, Void?, Bitmap?>() {
    private var bitmapFromUrlTaskListener: BitmapFromUrlTaskListener? = null

    fun setBitmapFromUrlTaskListener(groupInfoTaskListener: BitmapFromUrlTaskListener?) {
        this.bitmapFromUrlTaskListener = groupInfoTaskListener
    }

    override fun doInBackground(vararg params: Void?): Bitmap? {
        return try {
            HttpUrlConnectionParser.getBitmapFromURL(url)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onCancelled() {
        bitmapFromUrlTaskListener?.setBitmapFromUrlTaskToNull()
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        bitmapFromUrlTaskListener?.setBitmapFromUrlTaskToNull()
        if (null == bitmap) {
            bitmapFromUrlTaskListener?.onPostBitmapFromUrlExecuteFail(bitmap)
            return
        }
        bitmapFromUrlTaskListener?.onPostBitmapFromUrlExecute(bitmap, textView, context)
    }

    interface BitmapFromUrlTaskListener {
        fun onPostBitmapFromUrlExecute(response: Bitmap?, textView: View?, context: Context?)
        fun onPostBitmapFromUrlExecuteFail(response: Bitmap?)
        fun setBitmapFromUrlTaskToNull()
    }

    companion object {
        private const val TAG = "BitmapFromUrlTask"
    }
}
