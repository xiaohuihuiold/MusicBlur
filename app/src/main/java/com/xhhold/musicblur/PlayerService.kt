package com.xhhold.musicblur

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.xhhold.musicblur.manager.IMedia
import com.xhhold.musicblur.manager.MediaNotificationManager
import com.xhhold.musicblur.manager.MediaPlayerManager
import com.xhhold.musicblur.model.MusicInfo
import com.xhhold.musicblur.util.ActionUtil
import android.provider.MediaStore
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import com.xhhold.musicblur.manager.BlurManager
import android.media.AudioManager


class PlayerService : Service(), IMedia, AudioManager.OnAudioFocusChangeListener {

    private val iBinder = MediaControllerBinder()
    private val iMediaControllerCallbacks = ArrayList<IMediaControllerCallback>()
    private val musicList = ArrayList<MusicInfo>()

    private var mediaNotificationManager: MediaNotificationManager? = null
    private var mediaPlayerManager: MediaPlayerManager? = null
    private var audioManager: AudioManager? = null
    private var mediaControllerBroadcastReceiver: MediaControllerBroadcastReceiver? = null

    //private var isRun = false
    private var runn = true
    private var index: Int = 0

    override fun onCreate() {
        super.onCreate()
        mediaNotificationManager = MediaNotificationManager.INSTANCE.init(this)
        mediaPlayerManager = MediaPlayerManager.INSTANCE.init(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        mediaControllerBroadcastReceiver = MediaControllerBroadcastReceiver()

        audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager
                .AUDIOFOCUS_GAIN)

        val intentFilter = IntentFilter()
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_NEXT)
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_PREVIOUS)
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_PLAY)
        intentFilter.addAction(ActionUtil.ACTION_NOTIFY_PAUSE)
        registerReceiver(mediaControllerBroadcastReceiver, intentFilter)

        Thread {
            val resolver = contentResolver
            val cursor: Cursor? = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    null, null, null)
            cursor?.moveToFirst()
            do {
                val name = cursor?.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artist = cursor?.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val path = cursor?.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val albumId = cursor?.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                //Log.e("name",name)
                if (path?.toLowerCase()?.indexOf("netease") ?: -1 > 0) {
                    musicList.add(MusicInfo(name, artist, path, albumId ?: 0, null))
                }
            } while (cursor?.moveToNext() == true)
            cursor?.close()
            if (musicList.size > 0) {
                play(musicList[0])
                index = 0
                //iBinder.pause()
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

    override fun next() {
        iBinder.next()
    }

    override fun play() {
        mediaNotificationManager?.play()
        val bitmap = getAlbumArt(iBinder.musicInfo.albumID)
        mediaNotificationManager?.setMusicInfo(iBinder.musicInfo)
        mediaNotificationManager?.setAlbumBitmap(bitmap)
        BlurManager.INSTANCE.updateBitmap(bitmap)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                iBinder.pause()
                audioManager?.abandonAudioFocus(this)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                iBinder.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                iBinder.play()
            }
        }
    }

    private fun play(musicInfo: MusicInfo) {
        mediaPlayerManager?.play(musicInfo.path ?: "")
        audioManager?.requestAudioFocus(this@PlayerService, AudioManager.STREAM_MUSIC, AudioManager
                .AUDIOFOCUS_GAIN)
    }

    private fun getAlbumArt(album_id: Int): Bitmap? {
        val mUriAlbums = "content://media/external/audio/albums"
        val projection = arrayOf("album_art")
        val cur = contentResolver.query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null)
        var album_art: String? = null
        if (cur.count > 0 && cur.columnCount > 0) {
            cur.moveToNext()
            album_art = cur.getString(0)
        }
        cur.close()
        var bm: Bitmap? = null
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art)
        } else {
            bm = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
        }
        return bm
    }

    inner class MediaControllerBinder : IMediaController.Stub() {
        override fun play() {
            mediaNotificationManager?.play()
            mediaPlayerManager?.play()
            audioManager?.requestAudioFocus(this@PlayerService, AudioManager.STREAM_MUSIC, AudioManager
                    .AUDIOFOCUS_GAIN)
        }

        override fun pause() {
            mediaNotificationManager?.pause()
            mediaPlayerManager?.pause()
        }

        override fun next() {
            index++
            if (index >= musicList.size) {
                index = 0
            }
            play(musicList[index])
        }

        override fun previous() {
            index--
            if (index < 0) {
                index = musicList.size - 1
            }
            play(musicList[index])
        }

        override fun seekTo(time: Int) {
            mediaPlayerManager?.seekTo(time)
        }

        override fun setLooping(isLooping: Boolean) {
            mediaPlayerManager?.setLooping(isLooping)
        }

        override fun isPlaying(): Boolean {
            return mediaPlayerManager?.isPlaying() ?: false
        }

        override fun isLooping(): Boolean {
            return mediaPlayerManager?.isLooping() ?: false
        }

        override fun getMusicCurrentTime(): Int {
            return mediaPlayerManager?.getMusicCurrentTime() ?: 0
        }

        override fun getMusicDurationTime(): Int {
            return mediaPlayerManager?.getMusicDurationTime() ?: 0
        }

        override fun getMusicInfo(): MusicInfo {
            return musicList[index]
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
