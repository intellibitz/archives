package intellibitz.intellidroid.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.TextView;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;

import java.io.IOException;

/**
 * AsyncTAsk for Image Bitmap
 */
public class AsyncGettingBitmapFromUrl extends AsyncTask<Void, Void, Bitmap> {
    private TextView textView;
    private String url;
    private Context context;

    public AsyncGettingBitmapFromUrl(TextView textView, String url, Context context) {
        super();
        this.textView = textView;
        this.url = url;
        this.context = context;
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
    protected void onPostExecute(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.setBounds(new Rect(0, 0, 60, 60));
        textView.setCompoundDrawables(null, null, drawable, null);
/*
        textDrawable.setBounds(new Rect(0, 0, 80, 80));
        holder.tvTo.setCompoundDrawablesRelative(
                textDrawable, null, null, null);
*/
    }
}
