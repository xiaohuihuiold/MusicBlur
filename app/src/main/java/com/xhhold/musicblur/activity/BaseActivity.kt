package com.xhhold.musicblur.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.xhhold.musicblur.IMediaController
import com.xhhold.musicblur.IMediaControllerCallback
import com.xhhold.musicblur.PlayerService
import com.xhhold.musicblur.app.ActivityManager
import com.xhhold.musicblur.manager.BlurManager
import com.xhhold.musicblur.model.MusicInfo


abstract class BaseActivity : AppCompatActivity(), ServiceConnection, IMediaControllerCallback {

    protected var isFirst = true
    protected var iMediaController: IMediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityManager.INSTANCE.addActivity(this)

        bindService(Intent(this, PlayerService::class.java), this, Context.BIND_AUTO_CREATE)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isFirst) {
            applyBlur(findViewById(android.R.id.content))
            BlurManager.INSTANCE.refresh()
            isFirst = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeMediaControllerCallback(this)
        unbindService(this)
        ActivityManager.INSTANCE.removeActivity(this)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        iMediaController = service as IMediaController
        addMediaControllerCallback(this@BaseActivity)
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun onStateChange(state: Int) {

    }

    override fun onUpdateInfo(musicInfo: MusicInfo?) {

    }

    override fun onUpdateTime(current: Long, duration: Long) {

    }

    override fun asBinder(): IBinder? {
        return null
    }

    fun play() {
        iMediaController?.play()
    }

    fun pause() {
        iMediaController?.pause()
    }

    fun next() {
        iMediaController?.next()
    }

    fun previous() {
        iMediaController?.previous()
    }

    fun seekTo(time: Long) {
        iMediaController?.seekTo(time)
    }

    fun setLooping(isLooping: Boolean) {
        iMediaController?.isLooping = isLooping
    }

    fun isPlaying(): Boolean {
        return iMediaController?.isPlaying ?: false
    }

    fun isLooping(): Boolean {
        return iMediaController?.isLooping ?: false
    }

    fun getMusicCurrentTime(): Long {
        return iMediaController?.musicCurrentTime ?: 0
    }

    fun getMusicDurationTime(): Long {
        return iMediaController?.musicDurationTime ?: 0
    }

    fun getMusicInfo(): MusicInfo? {
        return iMediaController?.musicInfo
    }

    fun addMediaControllerCallback(iMediaControllerCallback: IMediaControllerCallback) {
        iMediaController?.addMediaControllerCallback(iMediaControllerCallback)
    }

    fun removeMediaControllerCallback(iMediaControllerCallback: IMediaControllerCallback) {
        iMediaController?.removeMediaControllerCallback(iMediaControllerCallback)
    }

    protected fun exitApp() {
        ActivityManager.INSTANCE.exitApp()
    }

    protected fun applyBlur(vararg views: View) {
        BlurManager.INSTANCE.apply(*views)
    }

    protected fun applyBlurAndOffset(vararg views: View) {
        BlurManager.INSTANCE.applyAndOffset(*views)
    }

}