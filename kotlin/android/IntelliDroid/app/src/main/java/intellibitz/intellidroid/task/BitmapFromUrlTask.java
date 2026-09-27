package intellibitz.intellidroid.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;

import java.io.IOException;

/**
 * AsyncTAsk for Image Bitmap
 */
public class BitmapFromUrlTask extends AsyncTask<Void, Void, Bitmap> {
    private static final String TAG = "BitmapFromUrlTask";
    private BitmapFromUrlTaskListener bitmapFromUrlTaskListener;
    private View textView;
    private String url;
    private Context context;

    public BitmapFromUrlTask(View view, String url, Context context) {
        super();
        this.textView = view;
        this.url = url;
        this.context = context;
    }

    public void setBitmapFromUrlTaskListener(BitmapFromUrlTaskListener groupInfoTaskListener) {
        this.bitmapFromUrlTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            return HttpUrlConnectionParser.getBitmapFromURL(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCancelled() {
        bitmapFromUrlTaskListener.setBitmapFromUrlTaskToNull();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        bitmapFromUrlTaskListener.setBitmapFromUrlTaskToNull();
        if (null == bitmap) {
            bitmapFromUrlTaskListener.onPostBitmapFromUrlExecuteFail(bitmap);
            return;
        }
        bitmapFromUrlTaskListener.onPostBitmapFromUrlExecute(bitmap, textView, context);
    }

    public interface BitmapFromUrlTaskListener {
        void onPostBitmapFromUrlExecute(Bitmap response, View textView, Context context);

        void onPostBitmapFromUrlExecuteFail(Bitmap response);

        void setBitmapFromUrlTaskToNull();
    }
}
