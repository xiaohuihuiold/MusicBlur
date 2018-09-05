package com.xhhold.musicblur.manager

import android.media.MediaPlayer
import java.lang.ref.WeakReference

class MediaPlayerManager private constructor() : MediaPlayer.OnCompletionListener {

    private var iMedia: WeakReference<IMedia?>? = null
    private var mediaPlayer: MediaPlayer? = null
    private var path: String? = null
    private var isPrepared = false

    companion object {
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MediaPlayerManager()
        }
    }

    fun init(iMedia: IMedia): MediaPlayerManager {
        this.iMedia = WeakReference(iMedia)
        return this
    }

    fun play(path: String) {
        stop()
        check()
        this.path = path
        mediaPlayer?.setDataSource(path)
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener {
            isPrepared = true
            mediaPlayer?.start()
            iMedia?.get()?.play()
        }
    }

    fun play() {
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stop() {
        isPrepared = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

    }

    fun seekTo(time: Int) {
        mediaPlayer?.seekTo(time)
    }

    fun setLooping(isLooping: Boolean) {
        mediaPlayer?.isLooping = isLooping
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun isLooping(): Boolean {
        return mediaPlayer?.isLooping ?: false
    }

    fun getMusicCurrentTime(): Int {
        if (!isPrepared) return 0
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getMusicDurationTime(): Int {
        if (!isPrepared) return 0
        return mediaPlayer?.duration ?: 0
    }

    override fun onCompletion(mp: MediaPlayer?) {
        iMedia?.get()?.next()
    }

    private fun check() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setOnCompletionListener(this)
        }
    }

}