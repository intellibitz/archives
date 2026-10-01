/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.view

/**
 *
 * @author work
 */

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.LinearLayout

class TransparentPanel : LinearLayout {

    private var innerPaint: Paint? = null
    private var borderPaint: Paint? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        innerPaint = Paint()
        innerPaint!!.setARGB(225, 75, 75, 75) //gray
        innerPaint!!.isAntiAlias = true

        borderPaint = Paint()
        borderPaint!!.setARGB(255, 255, 255, 255)
        borderPaint!!.isAntiAlias = true
        borderPaint!!.style = Style.STROKE
        borderPaint!!.strokeWidth = 2f
    }

    fun setInnerPaint(innerPaint: Paint?) {
        this.innerPaint = innerPaint
    }

    fun setBorderPaint(borderPaint: Paint?) {
        this.borderPaint = borderPaint
    }

    override fun dispatchDraw(canvas: Canvas) {
        val drawRect = RectF()
        drawRect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())

        canvas.drawRoundRect(drawRect, 5f, 5f, innerPaint!!)
        canvas.drawRoundRect(drawRect, 5f, 5f, borderPaint!!)

        super.dispatchDraw(canvas)
    }
}
