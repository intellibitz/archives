package com.mobeegal.android.model

import android.graphics.drawable.Drawable

class IconifiedText(
    private var mText: String = "",
    private var mIcon: Drawable?
) : Comparable<IconifiedText> {

    private var mSelectable = true

    fun isSelectable(): Boolean {
        return mSelectable
    }

    fun setSelectable(selectable: Boolean) {
        mSelectable = selectable
    }

    fun getText(): String {
        return mText
    }

    fun setText(text: String) {
        mText = text
    }

    fun setIcon(icon: Drawable?) {
        mIcon = icon
    }

    fun getIcon(): Drawable? {
        return mIcon
    }

    //	@Override
    override fun compareTo(other: IconifiedText): Int {
        if (this.mText != null) {
            return this.mText.compareTo(other.getText())
        } else {
            throw IllegalArgumentException()
        }
    }
}
