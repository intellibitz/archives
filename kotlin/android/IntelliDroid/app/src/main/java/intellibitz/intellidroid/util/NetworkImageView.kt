package intellibitz.intellidroid.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.ImageLoader.ImageContainer
import com.android.volley.toolbox.ImageLoader.ImageListener
import intellibitz.intellidroid.R

class NetworkImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ImageView(context, attrs, defStyle) {

    companion object {
        @JvmStatic
        fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
            val sbmp: Bitmap = if (bmp.width != radius || bmp.height != radius) {
                Bitmap.createScaledBitmap(bmp, radius, radius, false)
            } else {
                bmp
            }
            val output = Bitmap.createBitmap(
                sbmp.width,
                sbmp.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
            val paint = Paint()
            val rect = Rect(0, 0, sbmp.width, sbmp.height)
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = Color.parseColor("#BAB399")
            canvas.drawCircle(
                sbmp.width / 2f + 0.7f, sbmp.height / 2f + 0.7f,
                sbmp.width / 2f + 0.1f, paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(sbmp, rect, rect, paint)
            return output
        }
    }

    private var mUrl: String? = null
    private var mDefaultImageId = 0
    private var mErrorImageId = 0
    private var mImageLoader: ImageLoader? = null
    private var mImageContainer: ImageContainer? = null
    private var bitmap: Bitmap? = null

    fun setImageUrl(url: String?, imageLoader: ImageLoader?) {
        mUrl = url
        mImageLoader = imageLoader
        loadImageIfNecessary(false)
    }

    fun setDefaultImageResId(defaultImage: Int) {
        mDefaultImageId = defaultImage
    }

    fun setErrorImageResId(errorImage: Int) {
        mErrorImageId = errorImage
    }

    fun loadImageIfNecessary(isInLayoutPass: Boolean) {
        val width = width
        val height = height
        val scaleType = scaleType

        var wrapWidth = false
        var wrapHeight = false
        if (layoutParams != null) {
            wrapWidth = layoutParams.width == LayoutParams.WRAP_CONTENT
            wrapHeight = layoutParams.height == LayoutParams.WRAP_CONTENT
        }

        val isFullyWrapContent = wrapWidth && wrapHeight
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return
        }

        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer!!.cancelRequest()
                mImageContainer = null
            }
            setDefaultImageOrNull()
            return
        }

        if (mImageContainer != null && mImageContainer!!.requestUrl != null) {
            if (mImageContainer!!.requestUrl == mUrl) {
                return
            } else {
                mImageContainer!!.cancelRequest()
                setDefaultImageOrNull()
            }
        }

        val maxWidth = if (wrapWidth) 0 else width
        val maxHeight = if (wrapHeight) 0 else height

        val newContainer = mImageLoader?.get(mUrl,
            object : ImageListener {
                override fun onErrorResponse(error: VolleyError) {
                    if (mErrorImageId != 0) {
                        setImageResource(mErrorImageId)
                    }
                }

                override fun onResponse(response: ImageContainer, isImmediate: Boolean) {
                    if (isImmediate && isInLayoutPass) {
                        post { onResponse(response, false) }
                        return
                    }

                    if (response.bitmap != null) {
                        setImageBitmap(response.bitmap)
                    } else if (mDefaultImageId != 0) {
                        setImageResource(mDefaultImageId)
                    }
                }
            }, maxWidth, maxHeight, scaleType)

        mImageContainer = newContainer
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        loadImageIfNecessary(true)
    }

    override fun onDetachedFromWindow() {
        if (mImageContainer != null) {
            mImageContainer!!.cancelRequest()
            setImageBitmap(null)
            mImageContainer = null
        }
        super.onDetachedFromWindow()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return
        val w = width
        val h = height
        if (w == 0 || h == 0) {
            return
        }
        var b: Bitmap? = null
        if (drawable is BitmapDrawable) {
            b = drawable.bitmap
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.draw(canvas)
            b = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            drawable.draw(c)
        }
        if (b == null) {
            super.onDraw(canvas)
        } else {
            val bmp = b.copy(Bitmap.Config.ARGB_8888, true)
            val roundBitmap = getCroppedBitmap(bmp, h)
            canvas.drawBitmap(roundBitmap, 0f, 0f, null)
        }
    }

    fun setLocalImageBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
    }

    private fun setDefaultImageOrNull() {
        if (null == drawable) {
            val defaultBitmap = BitmapFactory.decodeResource(
                resources, R.drawable.ic_audiotrack_dark
            )
            setLocalImageBitmap(defaultBitmap)
        } else {
            try {
                val bmp = MainApplicationSingleton.drawableToBitmap(drawable)
                if (bmp != null) setLocalImageBitmap(bmp)
            } catch (ignored: Throwable) {
            }
        }
        if (mDefaultImageId != 0) {
            setImageResource(mDefaultImageId)
        } else if (bitmap != null) {
            setImageBitmap(bitmap)
        } else {
            setImageBitmap(null)
        }
    }
}
