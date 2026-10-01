package com.mobeegal.android.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mobeegal.android.model.IconifiedText

class IconifiedTextView(context: Context, aIconifiedText: IconifiedText) :
    LinearLayout(context) {

    private val mText: TextView
    private val mIcon: ImageView

    init {
        /* First Icon and the Text to the right (horizontal),
           * not above and below (vertical) */
        this.orientation = HORIZONTAL

        mIcon = ImageView(context)
        mIcon.setImageDrawable(aIconifiedText.getIcon())
        // left, top, right, bottom
        mIcon.setPadding(0, 2, 5, 0) // 5px to the right

        /* At first, add the Icon to ourself
           * (! we are extending LinearLayout) */
        addView(
            mIcon, LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            )
        )

        mText = TextView(context)
        mText.text = aIconifiedText.getText()
        /* Now the text (after the icon) */
        addView(
            mText, LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun setText(words: String?) {
        mText.text = words
    }

    fun setIcon(bullet: Drawable?) {
        mIcon.setImageDrawable(bullet)
    }
}
