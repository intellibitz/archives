package com.mobeegal.android.activity

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.Gallery
import android.widget.ImageView
import android.widget.Toast
import com.mobeegal.android.R
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class UploadGallery : Activity() {
    var upload_image: String? = null
    private var backButton: Button? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.gallery)
        backButton = findViewById(R.id.back) as Button
        val b = this.intent.extras
        try {
            if (b != null) {
                upload_image = b.getString("key")
            }
        } catch (e: Exception) {
        }
        (findViewById(R.id.gallery) as Gallery)
            .adapter = ImageAdapter(this)

        backButton!!.setOnClickListener {
            finish()
        }
    }

    inner class ImageAdapter(c: Context) : BaseAdapter() {

        private val myContext: Context = c
        private val myRemoteImages = arrayOf(upload_image)

        override fun getCount(): Int {
            return this.myRemoteImages.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val i = ImageView(this.myContext)

            try {
                val aURL = URL(upload_image)
                Toast.makeText(
                    this@UploadGallery, upload_image,
                    Toast.LENGTH_SHORT
                ).show()
                val conn = aURL.openConnection()
                conn.connect()
                val `is`: InputStream = conn.getInputStream()
                val bis = BufferedInputStream(`is`)
                val bm = BitmapFactory.decodeStream(bis)
                bis.close()
                `is`.close()
                i.setImageBitmap(bm)
            } catch (e: IOException) {
                i.setImageResource(R.drawable.icon)
                Log.e("DEBUGTAG", "Remtoe Image Exception", e)
            }

            i.scaleType = ImageView.ScaleType.FIT_CENTER
            i.layoutParams = Gallery.LayoutParams(150, 150)
            return i
        }

        fun getScale(focused: Boolean, offset: Int): Float {
            /* Formula: 1 / (2 ^ offset) */
            return Math.max(0f, 1.0f / Math.pow(2.0, Math.abs(offset).toDouble()).toFloat())
        }
    }
}
