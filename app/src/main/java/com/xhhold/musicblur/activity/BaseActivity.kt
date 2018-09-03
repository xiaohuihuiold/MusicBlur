package com.xhhold.musicblur.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.xhhold.musicblur.IMediaController
import com.xhhold.musicblur.IMediaControllerCallback
import com.xhhold.musicblur.service.PlayerService
import com.xhhold.musicblur.app.ActivityManager
import com.xhhold.musicblur.manager.BlurManager
import com.xhhold.musicblur.model.MusicInfo
import android.support.v7.app.AlertDialog
import com.xhhold.musicblur.R
import com.xhhold.musicblur.util.PermissionUtil


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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onPermissions(requestCode, PermissionUtil.checkPermissions(grantResults))
    }

    open protected fun onPermissions(requestCode: Int, isGranted: Boolean) {
        if (!isGranted) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.activity_base_dialog_permission_message))
            builder.setPositiveButton(getString(R.string.activity_base_dialog_permission_manual)) { dialog, which ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:$packageName")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                startActivity(intent)
            }
            builder.setNegativeButton(getString(R.string.base_cancel)) { dialog, which -> finish() }
            builder.show()
        }
    }

    /**
     *
     * 注册权限.
     *
     * 创建时间: 2018/3/15 0015
     * <br></br>
     *
     *注册权限
     *
     * @param requestCode 注册code
     * @param permissions 需要注册的权限
     * @return boolean true为已经授权
     */

    fun requestPermissions(requestCode: Int, permissions: Array<String>): Boolean {
        return PermissionUtil.request(this, permissions, requestCode)
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

    fun seekTo(time: Int) {
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

    fun getMusicCurrentTime(): Int {
        return iMediaController?.musicCurrentTime ?: 0
    }

    fun getMusicDurationTime(): Int {
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