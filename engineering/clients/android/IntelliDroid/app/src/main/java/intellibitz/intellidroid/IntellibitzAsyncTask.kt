package intellibitz.intellidroid

import android.content.Context
import android.os.AsyncTask
import java.lang.ref.SoftReference

abstract class IntellibitzAsyncTask(context: Context?) : AsyncTask<Any?, Any?, Any?>() {
    companion object {
        const val TAG = "IntellibitzAsyncTask"
    }

    @JvmField
    protected val context: SoftReference<Context?> = SoftReference(context)
    @JvmField
    protected var contextRef: Context? = context
    @JvmField
    protected var success: Boolean = false

    fun getContextRef(): Context? {
        var ctx = context.get()
        if (ctx == null) ctx = contextRef
        return ctx
    }

    fun releaseContext() {
        contextRef = null
        context.clear()
    }

    override fun doInBackground(vararg params: Any?): Any? {
        return null
    }

    override fun onPostExecute(success: Any?) {
        if (success is Boolean) {
            this.success = success
        }
    }
}
