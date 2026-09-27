package com.mobeegal.android.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import com.mobeegal.android.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class UploadGallery
        extends Activity
{
    String upload_image;
    private Button backButton;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.gallery);
        backButton = (Button) findViewById(R.id.back);
        Bundle b = this.getIntent().getExtras();
        try
        {
            if (b != null)
            {
                upload_image = b.getString("key");
            }
        }
        catch (Exception e)
        {

        }
        ((Gallery) findViewById(R.id.gallery))
                .setAdapter(new ImageAdapter(this));

        backButton.setOnClickListener(new OnClickListener()
        {

            public void onClick(View arg0)
            {

                finish();
            }
        });
    }

    public class ImageAdapter
            extends BaseAdapter
    {

        private Context myContext;
        private String[] myRemoteImages = {
                upload_image
        };

        public ImageAdapter(Context c)
        {
            this.myContext = c;
        }

        public int getCount()
        {
            return this.myRemoteImages.length;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView i = new ImageView(this.myContext);

            try
            {
                URL aURL = new URL(upload_image);
                Toast.makeText(UploadGallery.this, upload_image,
                        Toast.LENGTH_SHORT).show();
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                Bitmap bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
                i.setImageBitmap(bm);
            }
            catch (IOException e)
            {
                i.setImageResource(R.drawable.icon);
                Log.e("DEBUGTAG", "Remtoe Image Exception", e);
            }

            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i.setLayoutParams(new Gallery.LayoutParams(150, 150));
            return i;
        }

        public float getScale(boolean focused, int offset)
        {
            /* Formula: 1 / (2 ^ offset) */
            return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
        }


    }


}

