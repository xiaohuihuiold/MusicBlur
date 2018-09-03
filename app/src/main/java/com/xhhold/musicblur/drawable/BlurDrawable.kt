package com.xhhold.musicblur.drawable

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import com.xhhold.musicblur.manager.BlurManager
import com.xhhold.musicblur.manager.IBlur
import android.content.Context
import android.util.TypedValue
import com.xhhold.musicblur.app.MyApplication.Companion.context


class BlurDrawable(private var view: View) : Drawable(), IBlur {

    private var bitmapO: Bitmap? = null
    private var bitmapN: Bitmap? = null
    private var alphaBitO: Int = 0
    private var alphaBitN: Int = 255
    private var width: Int = 0
    private var height: Int = 0
    private var offsetY: Int = 0
    private var isDarkN: Boolean = false
    private var isDarkO: Boolean = false
    private var isOffset: Boolean = false

    private val paint = Paint()

    init {
        BlurManager.INSTANCE.addBlur(this)
    }

    constructor(view: View, isOffset: Boolean) : this(view) {
        this.isOffset = isOffset
    }

    override fun draw(canvas: Canvas) {
        paint.color = Color.WHITE
        val x = view.x.toInt()
        val y = view.y.toInt()
        val srcRect = Rect(0, 0, width, height)
        val dstRect = Rect()
        dstRect.right = width - x
        dstRect.bottom = height - y - offsetY
        dstRect.left = -x
        dstRect.top = -y - offsetY

        if (bitmapO != null) {
            drawBitmap(canvas, bitmapO, srcRect, dstRect, 255 - alphaBitN, isDarkO, alphaBitO)
        }

        if (bitmapN != null) {
            drawBitmap(canvas, bitmapN, srcRect, dstRect, alphaBitN, isDarkN, alphaBitN)
        }
    }

    private fun drawBitmap(canvas: Canvas, bitmap: Bitmap?, src: Rect, dst: Rect, alpha: Int,
                           isDark: Boolean, alphaBit: Int) {
        paint.alpha = alpha
        canvas.drawBitmap(bitmap, src, dst, paint)
        if (isDark) {
            paint.color = Color.argb((127f * (alphaBit.toFloat() / 255f)).toInt(), 0, 0, 0)
            canvas.drawRect(0f, 0f, view.width.toFloat(), view.height.toFloat(), paint)
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    override fun onUpdate(newBitmap: Bitmap?, oldBitmap: Bitmap?, width: Int, height: Int,
                          dark: Boolean) {
        this.isDarkO = this.isDarkN
        this.isDarkN = dark
        this.bitmapN = newBitmap
        this.bitmapO = oldBitmap
        this.width = width
        this.height = height
        this.alphaBitO = this.alphaBitN
        if (isOffset) {
            val tv = TypedValue()
            if (view.context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
                offsetY = actionBarHeight + getStatusBarHeight(view.context)
            }
        }
        val valueAnimator = ValueAnimator.ofInt(0, 255)
        valueAnimator.duration = 1000
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            alphaBitN = value
            view.invalidate()
        }
        valueAnimator.start()
    }

    override fun runOnUiThread(runnable: () -> Unit) {
        val activity = view.context as Activity
        activity.runOnUiThread(runnable)
    }

    private fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

}