package com.xhhold.musicblur.view

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.xhhold.musicblur.manager.IMusicInfo
import com.xhhold.musicblur.manager.LyricManager
import com.xhhold.musicblur.model.MusicInfo
import com.xhhold.musicblur.model.NeteaseLyric
import com.xhhold.musicblur.model.NeteaseMusicInfo

class LyricView : View, IMusicInfo {

    private var paint: Paint? = null
    private var musicInfo: MusicInfo? = null
    private var neteaseMusicInfo: NeteaseMusicInfo? = null
    private var neteaseLyric: NeteaseLyric? = null
    private var index = 0

    private var lyricY = 0

    private var valueAnimator: ValueAnimator? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init()
    }

    private fun init() {
        LyricManager.INSTANCE.addCallback(this)
        LyricManager.INSTANCE.refresh()
        paint = Paint()
        paint?.isAntiAlias = true
        paint?.textAlign = Paint.Align.CENTER
        paint?.textSize = 40f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawLyric(canvas)
    }

    private fun drawLyric(canvas: Canvas?) {
        val lyricLines = neteaseLyric?.lyrics ?: return
        for ((i, lyricLine) in lyricLines.withIndex()) {
            val paintSize: Float = paint?.textSize ?: 0f
            val lY = height.toFloat() / 2f - (paintSize * -i) * 2.5f - lyricY
            if (index == i) {
                paint?.color = Color.WHITE
            } else {
                paint?.color = Color.GRAY
            }
            canvas?.drawText(lyricLine.lyric, (width / 2).toFloat(), lY, paint)
            canvas?.drawText(lyricLine.lyricTran ?: "", (width / 2).toFloat(), lY + paintSize,
                    paint)
        }
    }

    override fun onUpdate(index: Int) {
        this.index = index
        val paintSize: Float = paint?.textSize ?: 0f
        post {
            if (valueAnimator != null) {
                valueAnimator?.cancel()
                valueAnimator = null
            }
            valueAnimator = ValueAnimator.ofInt(lyricY, (paintSize * index * 2.5f).toInt())
            valueAnimator?.addUpdateListener {
                val value = it.animatedValue as Int
                lyricY = value
                invalidate()
            }
            valueAnimator?.start()
        }
    }

    override fun onMusicInfoUpdate(musicInfo: MusicInfo?) {
        this.musicInfo = musicInfo
    }

    override fun onNeteaseMusicInfoUpdate(neteaseMusicInfo: NeteaseMusicInfo?) {
        this.neteaseMusicInfo = neteaseMusicInfo
        this.neteaseLyric = neteaseMusicInfo?.lyric
    }

}