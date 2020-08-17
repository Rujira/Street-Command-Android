package com.codinghub.apps.streetcommand.camerautil

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup

class OtherViewPort : ViewGroup {

    constructor(context: Context?) : super(context)
    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int = 0) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    override fun dispatchDraw(canvas: Canvas) {

        super.dispatchDraw(canvas)
//        val viewportMargin = 160
//
//        val topMargin = 280
//
//        val eraser = Paint()
//        eraser.isAntiAlias = true
//        eraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
//        val width = width.toFloat() - viewportMargin
//        val height = width * 1.4.toFloat()
//        val rect = RectF(viewportMargin.toFloat(), topMargin.toFloat(), width, height)
//
//        val viewportCornerRadius = width / 2
//        val frame = RectF(
//            viewportMargin.toFloat() - 2,
//            topMargin.toFloat() - 2,
//            width + 4,
//            height + 4
//        )
//        val path = Path()
//        val stroke = Paint()
//        stroke.isAntiAlias = true
//        stroke.strokeWidth = 4f
//        stroke.color = Color.WHITE
//        stroke.style = Paint.Style.STROKE
//        path.addRoundRect(
//            frame,
//            viewportCornerRadius.toFloat(),
//            viewportCornerRadius.toFloat(),
//            Path.Direction.CW
//        )
//        canvas.drawPath(path, stroke)
//        canvas.drawRoundRect(
//            rect,
//            viewportCornerRadius.toFloat(),
//            viewportCornerRadius.toFloat(),
//            eraser
//        )

    }
}