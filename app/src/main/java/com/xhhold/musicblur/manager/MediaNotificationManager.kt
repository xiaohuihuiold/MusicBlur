package com.xhhold.musicblur.manager

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.MediaSession
import android.os.Build
import com.xhhold.musicblur.R
import com.xhhold.musicblur.activity.PlayerActivity
import com.xhhold.musicblur.app.MyApplication
import com.xhhold.musicblur.model.MusicInfo
import com.xhhold.musicblur.util.ActionUtil

class MediaNotificationManager private constructor() {

    private val CHANNEL_ID = "PlayerServiceChannel"
    private val CHANNEL_NAME = "PlayerService"

    private var service: Service? = null

    private var mediaStyle: Notification.MediaStyle? = null
    private var manager: NotificationManager? = null
    private var channel: NotificationChannel? = null
    private var builder: Notification.Builder? = null
    private var actionPrevious: Notification.Action? = null
    private var actionPlay: Notification.Action? = null
    private var actionPause: Notification.Action? = null
    private var actionNext: Notification.Action? = null

    companion object {
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MediaNotificationManager()
        }
    }

    fun init(service: Service): MediaNotificationManager {
        this.service = service
        manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mediaStyle = Notification.MediaStyle()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, android.app
                    .NotificationManager.IMPORTANCE_NONE)
            manager?.createNotificationChannel(channel)
            builder = Notification.Builder(service, CHANNEL_ID)
            builder?.setColorized(true)
        } else {
            builder = Notification.Builder(service)
        }
        builder?.setContentIntent(PendingIntent.getActivity(service, 0, Intent(service,
                PlayerActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
        builder?.setLargeIcon(BitmapFactory.decodeStream(service.assets.open
        ("images/image1.jpg")))
        builder?.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder?.setContentTitle("标题")
        builder?.setContentText("内容")
        builder?.setTicker("通知")

        actionPrevious = Notification.Action.Builder(R.drawable.ic_skip_previous, "",
                PendingIntent.getBroadcast(service, 0, Intent(ActionUtil.ACTION_NOTIFY_PREVIOUS),
                        PendingIntent.FLAG_UPDATE_CURRENT)).build()
        actionPlay = Notification.Action.Builder(R.drawable.ic_play, "",
                PendingIntent.getBroadcast(service, 0, Intent(ActionUtil.ACTION_NOTIFY_PLAY),
                        PendingIntent.FLAG_UPDATE_CURRENT)).build()
        actionPause = Notification.Action.Builder(R.drawable.ic_pause, "",
                PendingIntent.getBroadcast(service, 0, Intent(ActionUtil.ACTION_NOTIFY_PAUSE),
                        PendingIntent.FLAG_UPDATE_CURRENT)).build()
        actionNext = Notification.Action.Builder(R.drawable.ic_skip_next, "",
                PendingIntent.getBroadcast(service, 0, Intent(ActionUtil.ACTION_NOTIFY_NEXT),
                        PendingIntent.FLAG_UPDATE_CURRENT)).build()

        builder?.setActions(actionPrevious, actionPlay, actionNext)

        builder?.setStyle(mediaStyle)

        mediaStyle?.setShowActionsInCompactView(0, 1, 2)
        val session = MediaSession(service, "sess")
        mediaStyle?.setMediaSession(session.sessionToken)
        builder?.setVisibility(Notification.VISIBILITY_PUBLIC)
        //notification = builder?.build()
        updateNotification()
        return this
    }

    fun play() {
        builder?.setActions(actionPrevious, actionPause, actionNext)
        updateNotification()
    }

    fun pause() {
        builder?.setActions(actionPrevious, actionPlay, actionNext)
        updateNotification()
    }

    fun setMusicInfo(musicInfo: MusicInfo?) {
        setAlbumBitmap(musicInfo?.albumImage, false)
        setTitle(musicInfo?.name, false)
        setText(musicInfo?.artist, false)
    }

    fun setAlbumBitmap(bitmap: Bitmap?) {
        setAlbumBitmap(bitmap, true)
    }

    fun setTitle(title: String?) {
        setTitle(title, true)
    }

    fun setText(text: String?) {
        setText(text, true)
    }

    private fun setAlbumBitmap(bitmap: Bitmap?, update: Boolean) {
        builder?.setLargeIcon(bitmap)
        if (update) {
            updateNotification()
        }
    }

    private fun setTitle(title: String?, update: Boolean) {
        builder?.setContentTitle(title)
        if (update) {
            updateNotification()
        }
    }

    private fun setText(text: String?, update: Boolean) {
        builder?.setContentText(text)
        if (update) {
            updateNotification()
        }
    }

    private fun updateNotification() {
        service?.startForeground(1, builder?.build())
    }

}