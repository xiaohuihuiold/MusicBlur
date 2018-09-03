package com.xhhold.musicblur.manager

import android.graphics.Bitmap

interface IBlur {
    fun onUpdate(newBitmap: Bitmap?, oldBitmap: Bitmap?, width: Int, height: Int, dark: Boolean)
    fun runOnUiThread(runnable: () -> Unit)
}