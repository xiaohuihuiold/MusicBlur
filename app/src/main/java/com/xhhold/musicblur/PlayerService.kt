package com.xhhold.musicblur

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.IBinder
import com.xhhold.musicblur.manager.BlurManager
import com.xhhold.musicblur.manager.MediaNotificationManager
import com.xhhold.musicblur.model.MusicInfo
import com.xhhold.musicblur.util.ActionUtil


class PlayerService : Service() {

    private val iBinder = MediaControllerBinder()
    private val iMediaControllerCallbacks = ArrayList<IMediaControllerCallback>()

    private var mediaNotificationManager: MediaNotificationManager? = null
    private var mediaControllerBroadcastReceiver: MediaControllerBroadcastReceiver? = null

    private var isRun = false
    private var runn = true

    override fun onCreate() {
        super.onCreate()
        mediaNotificationManager = MediaNotificationManager.INSTANCE.init(this)
        mediaControllerBroadcastReceiver = MediaControllerBroadcastReceiver()

        val intentFilter = IntentFilter()
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_NEXT)
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_PREVIOUS)
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_PLAY)
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_PAUSE)
        registerReceiver(mediaControllerBroadcastReceiver, intentFilter)

        Thread {
            var i = 1
            while (runn) {
                if (isRun) {
                    i++
                    if (i >= 3) {
                        i = 1
                    }
                    BlurManager.INSTANCE.updateBitmap(BitmapFactory.decodeStream(assets.open
                    ("images/image$i.jpg")))
                    mediaNotificationManager?.setTitle("images/image$i.jpg")
                    mediaNotificationManager?.setAlbumBitmap(BitmapFactory.decodeStream(assets.open
                    ("images/image$i.jpg")))
                    Thread.sleep(2000)
                }
            }
        }.start()
    }

    override fun onBind(intent: Intent): IBinder {
        return iBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        runn = false
        unregisterReceiver(mediaControllerBroadcastReceiver)
        iMediaControllerCallbacks.clear()
    }

    inner class MediaControllerBinder : IMediaController.Stub() {
        override fun play() {
            mediaNotificationManager?.play()
            isRun = true
        }

        override fun pause() {
            mediaNotificationManager?.pause()
            isRun = false
        }

        override fun next() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun previous() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun seekTo(time: Long) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun setLooping(isLooping: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isPlaying(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isLooping(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMusicCurrentTime(): Long {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMusicDurationTime(): Long {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMusicInfo(): MusicInfo {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addMediaControllerCallback(iMediaControllerCallback: IMediaControllerCallback) {
            iMediaControllerCallbacks.add(iMediaControllerCallback)
        }

        override fun removeMediaControllerCallback(iMediaControllerCallback: IMediaControllerCallback) {
            iMediaControllerCallbacks.remove(iMediaControllerCallback)
        }

    }

    inner class MediaControllerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ActionUtil.ACTION_NOTIFY_PLAY -> {
                    iBinder.play()
                }
                ActionUtil.ACTION_NOTIFY_PAUSE -> {
                    iBinder.pause()
                }
                ActionUtil.ACTION_NOTIFY_PREVIOUS -> {
                    iBinder.previous()
                }
                ActionUtil.ACTION_NOTIFY_NEXT -> {
                    iBinder.next()
                }
            }
        }
    }

}
