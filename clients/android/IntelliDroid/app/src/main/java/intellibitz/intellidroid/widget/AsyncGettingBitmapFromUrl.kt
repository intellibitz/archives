package intellibitz.intellidroid.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.widget.TextView
import intellibitz.intellidroid.util.HttpUrlConnectionParser
import java.io.IOException

/**
 * AsyncTask for Image Bitmap
 */
class AsyncGettingBitmapFromUrl(
    private val textView: TextView?,
    private val url: String?,
    private val context: Context?
) : AsyncTask<Void, Void, Bitmap?>() {

    override fun doInBackground(vararg params: Void?): Bitmap? {
        val u = url ?: return null
        return try {
            HttpUrlConnectionParser.getBitmapFromURL(u)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        val ctx = context ?: return
        val tv = textView ?: return
        if (bitmap != null) {
            val drawable = BitmapDrawable(ctx.resources, bitmap)
            drawable.setBounds(Rect(0, 0, 60, 60))
            tv.setCompoundDrawables(null, null, drawable, null)
        }
    }
}
