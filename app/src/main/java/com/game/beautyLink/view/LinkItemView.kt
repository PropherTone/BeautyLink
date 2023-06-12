package com.game.beautyLink.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.game.beautyLink.R

class LinkItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var backBitmap: Bitmap? = null
    private var itemBitmap: Bitmap? = null
    private val rect = Rect()
    private val itemRect = Rect()
    private var padding = 0
    private val shaderPaint = Paint().apply {
        color = Color.parseColor("#E5A96D")
    }
    private var isActive = false

    fun setIcon(mipmap: Int, onActive: (Boolean, LinkItemView) -> Unit) {
        backBitmap = BitmapFactory.decodeResource(resources, R.mipmap.link_item_back)
        itemBitmap = BitmapFactory.decodeResource(resources, mipmap)
        setOnClickListener {
            shaderPaint.maskFilter = if (!isActive) {
                BlurMaskFilter(padding.toFloat(), BlurMaskFilter.Blur.SOLID)
            } else null
            isActive = !isActive
            onActive(isActive, this)
            invalidate()
        }
        invalidate()
    }

    fun negative() {
        shaderPaint.maskFilter = if (!isActive) {
            BlurMaskFilter(padding.toFloat(), BlurMaskFilter.Blur.SOLID)
        } else null
        isActive = !isActive
        invalidate()
    }

    fun clear() {
        backBitmap = null
        itemBitmap = null
        setOnClickListener(null)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        padding = w / 12
        rect.set(padding, padding, w - padding, h - padding)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        backBitmap?.let { canvas?.drawBitmap(it, null, rect, shaderPaint) }
        itemBitmap?.let {
            itemRect.left = (rect.width() - it.width) / 2 + padding
            itemRect.top = (rect.height() - it.height) / 2 + padding
            itemRect.right = it.width + itemRect.left - padding
            itemRect.bottom = it.height + itemRect.top - padding
            canvas?.drawBitmap(it, null, itemRect, null)
        }
    }

}