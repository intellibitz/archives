package intellibitz.intellidroid;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.SoftReference;

/**
 */
public abstract class IntellibitzAsyncTask extends
        AsyncTask<Object, Object, Object> {
    public static final String TAG = "IntellibitzAsyncTask";

    protected final SoftReference<Context> context;
    protected Context contextRef;
    protected boolean success = false;

    public IntellibitzAsyncTask(Context context) {
        this.context = new SoftReference<>(context);
        this.contextRef = context;
    }

    public Context getContextRef() {
        Context context = this.context.get();
        if (null == context) context = this.contextRef;
        return context;
    }

    public void releaseContext() {
        this.contextRef = null;
        context.clear();
    }

    @Override
    protected Object doInBackground(Object... params) {
        return null;
    }

    @Override
    protected void onPostExecute(final Object success) {
        if (success instanceof Boolean) {
            this.success = (Boolean) success;
        }
    }

}
