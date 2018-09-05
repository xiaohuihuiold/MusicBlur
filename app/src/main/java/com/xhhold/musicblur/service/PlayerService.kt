package com.xhhold.musicblur.service

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
import com.xhhold.musicblur.manager.BlurManager
import android.media.AudioManager
import android.util.Log
import com.google.gson.JsonParser
import com.xhhold.musicblur.IMediaController
import com.xhhold.musicblur.IMediaControllerCallback
import com.xhhold.musicblur.model.LyricLine
import com.xhhold.musicblur.model.NeteaseMusicInfo
import com.xhhold.musicblur.util.LrcUtil
import com.xhhold.musicblur.util.MusicUtil
import com.xhhold.musicblur.util.UrlUtil
import okhttp3.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.util.*


class PlayerService : Service(), IMedia, AudioManager.OnAudioFocusChangeListener, Runnable {

    private val iBinder = MediaControllerBinder()
    private val iMediaControllerCallbacks = ArrayList<WeakReference<IMediaControllerCallback>>()
    private val musicList = ArrayList<MusicInfo>()

    //private val audioFocusRequest: AudioFocusRequest=AudioFocusRequest.Builder()

    private var mediaNotificationManager: MediaNotificationManager? = null
    private var mediaPlayerManager: MediaPlayerManager? = null
    private var audioManager: AudioManager? = null
    private var mediaControllerBroadcastReceiver: MediaControllerBroadcastReceiver? = null

    private var isRun = false
    private var runn = true
    private var index: Int = 0
    private var neteaseMusicInfo: NeteaseMusicInfo? = null
    private var lyricLineTemp: LyricLine? = null

    private var obj = java.lang.Object()

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
        Thread(this).start()
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
                if (path?.toLowerCase()?.indexOf("netease") ?: -1 > 0) {
                    musicList.add(MusicInfo(name, artist, path, albumId ?: 0))
                }
            } while (cursor?.moveToNext() == true)
            cursor?.close()
            if (musicList.size > 0) {
                play(musicList[0])
                index = 0
            }
        }.start()
    }

    override fun onBind(intent: Intent): IBinder {
        return iBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        runn = false
        isRun = false
        mediaPlayerManager?.stop()
        iMediaControllerCallbacks.clear()
        unregisterReceiver(mediaControllerBroadcastReceiver)
    }

    override fun next() {
        iBinder.next()
    }

    override fun play() {
        isRun = true
        mediaNotificationManager?.play()
        val bitmap = MusicUtil.getAlbumArt(iBinder.musicInfo.albumID)
        mediaNotificationManager?.setMusicInfo(iBinder.musicInfo)
        mediaNotificationManager?.setAlbumBitmap(bitmap)
        audioManager?.requestAudioFocus(this@PlayerService, AudioManager.STREAM_MUSIC, AudioManager
                .AUDIOFOCUS_GAIN)
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

    override fun run() {
        while (runn) {
            if (!isRun) continue
            update(mediaPlayerManager?.getMusicCurrentTime() ?: 0,
                    mediaPlayerManager?.getMusicDurationTime() ?: 0)
            synchronized(obj) {
                for (callBack in iMediaControllerCallbacks) {
                    callBack.get()?.onUpdateTime(mediaPlayerManager?.getMusicCurrentTime() ?: 0,
                            mediaPlayerManager?.getMusicDurationTime() ?: 0)
                }
            }
        }
    }

    fun update(curr: Int, dura: Int) {
        if (neteaseMusicInfo == null) {
            return
        }
        val neteaseLyrics = neteaseMusicInfo?.lyric?.lyrics ?: return
        var lyricLine: LyricLine? = null
        for (l in neteaseLyrics) {
            if (curr > l.time) {
                lyricLine = l
            }
        }
        if (lyricLineTemp != lyricLine && lyricLine?.lyric != null) {
            //Log.e("Lrc", "test\n${lyricLine.time}\n${lyricLine.lyric}\n${lyricLine.lyricTran}")
            mediaNotificationManager?.update(lyricLine.lyric, lyricLine.lyricTran)
            lyricLineTemp = lyricLine
        }
    }

    private fun play(musicInfo: MusicInfo) {
        for (callBack in iMediaControllerCallbacks) {
            callBack.get()?.onUpdateInfo(musicInfo)
        }
        neteaseMusicInfo = null
        isRun = false
        mediaPlayerManager?.play(musicInfo.path ?: "")
        audioManager?.requestAudioFocus(this@PlayerService, AudioManager.STREAM_MUSIC, AudioManager
                .AUDIOFOCUS_GAIN)
        getNeteaseInfo(musicInfo, false)
    }

    fun getNeteaseInfo(musicInfo: MusicInfo, hasArtist: Boolean) {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(
                UrlUtil.NETEASE_SEARCH +
                        URLDecoder.decode(musicInfo.name +
                                if (hasArtist) musicInfo.artist else "", "utf-8"))
                .build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val jsonObject = JsonParser().parse(response.body()?.string()).asJsonObject
                val jsonArray = jsonObject.get("result").asJsonObject.get("songs").asJsonArray
                if (jsonArray.size() <= 0) {
                    return
                }
                val song = jsonArray[0].asJsonObject
                val id = song.get("id").asInt
                val name = song.get("name").asString
                val lyricRequest = Request.Builder().url(UrlUtil.NETEASE_LYRIC + id).build()
                val lyricCall = okHttpClient.newCall(lyricRequest)
                lyricCall.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {

                    }

                    override fun onResponse(call: Call, response: Response) {
                        val neteaseLyric = LrcUtil.lrc2NeteaseLyric(response.body()?.string())
                        if (neteaseLyric?.lyrics == null || neteaseLyric.lyrics?.size == 0) {
                            if (hasArtist) {
                                return
                            }
                            return getNeteaseInfo(musicInfo, true)
                        }
                        neteaseMusicInfo = NeteaseMusicInfo(id, name, neteaseLyric)
                    }
                })
            }
        })
    }

    inner class MediaControllerBinder : IMediaController.Stub() {
        override fun play() {
            mediaPlayerManager?.play()
            this@PlayerService.play()
        }

        override fun pause() {
            mediaNotificationManager?.pause()
            mediaPlayerManager?.pause()
            isRun = false
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
            synchronized(obj) {
                iMediaControllerCallbacks.add(WeakReference(iMediaControllerCallback))
            }
        }

        override fun removeMediaControllerCallback(iMediaControllerCallback: IMediaControllerCallback) {
            synchronized(obj) {
                var size = iMediaControllerCallbacks.size
                var count = 0
                for (i in 0 until size) {
                    if (iMediaControllerCallbacks[i - count].get() ==
                            iMediaControllerCallback || iMediaControllerCallbacks[i - count].get()
                            == null) {
                        iMediaControllerCallbacks.remove(iMediaControllerCallbacks[i - count])
                        count++
                    }
                }
                for (callBack in iMediaControllerCallbacks) {
                    Log.i("Callback", callBack.get()?.javaClass?.simpleName)
                }
            }
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
