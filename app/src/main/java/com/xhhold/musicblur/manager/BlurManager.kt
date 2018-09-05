package com.xhhold.musicblur.manager

import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.support.v7.graphics.Palette
import android.util.Log
import android.view.View
import com.xhhold.musicblur.app.MyApplication
import com.xhhold.musicblur.drawable.BlurDrawable
import com.xhhold.musicblur.util.ColorUtil
import com.xhhold.musicblur.util.ScreenUtil
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class BlurManager private constructor() {

    private val blurs = ArrayList<WeakReference<IBlur>>()
    private var newBitmap: Bitmap? = null
    private var oldBitmap: Bitmap? = null
    private var width: Int = 0
    private var height: Int = 0
    private var isDark: Boolean = false

    private var executor = Executors.newSingleThreadExecutor()

    companion object {
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BlurManager()
        }
    }

    fun addBlur(iBlur: IBlur) {
        blurs.add(WeakReference(iBlur))
    }

    private fun update() {
        val size = blurs.size
        var count = 0
        for (i in 0 until size) {
            if (blurs[i - count].get() == null) {
                blurs.removeAt(i - count)
                count++
                continue
            }
            val iBlur = blurs[i - count].get()
            iBlur?.runOnUiThread {
                iBlur.onUpdate(newBitmap, oldBitmap, width, height, isDark)
            }
        }
    }

    fun refresh() {
        update()
    }

    fun updateBitmap(newBitmap: Bitmap?) {
        if (newBitmap == null) return
        executor.execute {
            refreshWH()
            val temp = width.toFloat() / height.toFloat()
            var x = 0f
            var y = 0f
            val w: Float
            val h: Float
            val bitw = newBitmap.width.toFloat()
            val bith = newBitmap.height.toFloat()
            if (bitw < bith) {
                w = bitw
                h = bitw / temp
                y = (bith - h) / 2.0f
            } else {
                h = bith
                w = bith * temp
                x = (bitw - w) / 2.0f
            }
            if (x < 0 || y < 0 || w <= 0 || h <= 0) {

            } else {
                val bitmap = Bitmap.createBitmap(newBitmap, x.toInt(), y.toInt(), w.toInt(), h.toInt())
                this.oldBitmap = this.newBitmap
                this.newBitmap = Bitmap.createScaledBitmap(rsBlur(bitmap, 25, 0.20f), width, height,
                        false)
                bitmap.recycle()
                isDark = ColorUtil.isShouldDark(Palette.from(newBitmap).clearFilters().generate())
                update()
            }
        }
    }

    fun rsBlur(source: Bitmap, radius: Int, scale: Float): Bitmap {
        val width = Math.round(source.width * scale)
        val height = Math.round(source.height * scale)
        val inputBmp = Bitmap.createScaledBitmap(source, width, height, false)
        val renderScript = RenderScript.create(MyApplication.context)
        val input = Allocation.createFromBitmap(renderScript, inputBmp)
        val output = Allocation.createTyped(renderScript, input.type)
        val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setInput(input)
        scriptIntrinsicBlur.setRadius(radius.toFloat())
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(inputBmp)
        renderScript.destroy()
        return inputBmp
    }

    fun apply(vararg views: View) {
        for (view in views) {
            view.background = BlurDrawable(view)
        }
    }

    fun applyAndOffset(vararg views: View) {
        for (view in views) {
            view.background = BlurDrawable(view, true)
        }
    }

    private fun refreshWH() {
        val displayMetrics = MyApplication.context.resources.displayMetrics
        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels + ScreenUtil.getNavigationBarHeight() + ScreenUtil.getStatusBarHeight()
        Log.e("WH", "w:$width,h:$height")
    }
}