package com.xhhold.musicblur.manager

import com.xhhold.musicblur.model.MusicInfo
import com.xhhold.musicblur.model.NeteaseMusicInfo
import java.lang.ref.WeakReference

class LyricManager private constructor() : IMusicInfo {

    private var musicInfo: MusicInfo? = null
    private var neteaseMusicInfo: NeteaseMusicInfo? = null

    private val iMusicInfos = ArrayList<WeakReference<IMusicInfo>>()

    private val obj = java.lang.Object()

    companion object {
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            LyricManager()
        }
    }

    override fun onUpdate(index: Int) {
        synchronized(obj) {
            check()
            for (iMusicInfo in iMusicInfos) {
                iMusicInfo.get()?.onUpdate(index)
            }
        }
    }

    override fun onMusicInfoUpdate(musicInfo: MusicInfo?) {
        synchronized(obj) {
            this.musicInfo = musicInfo
            check()
            for (iMusicInfo in iMusicInfos) {
                iMusicInfo.get()?.onMusicInfoUpdate(musicInfo)
            }
        }
    }

    override fun onNeteaseMusicInfoUpdate(neteaseMusicInfo: NeteaseMusicInfo?) {
        synchronized(obj) {
            this.neteaseMusicInfo = neteaseMusicInfo
            check()
            for (iMusicInfo in iMusicInfos) {
                iMusicInfo.get()?.onNeteaseMusicInfoUpdate(neteaseMusicInfo)
            }
        }
    }

    fun addCallback(iMusicInfo: IMusicInfo) {
        synchronized(obj) {
            iMusicInfos.add(WeakReference(iMusicInfo))
        }
    }

    fun removeCallback(iMusicInfo: IMusicInfo) {
        synchronized(obj) {
            val size = iMusicInfos.size
            var count = 0
            for (i in 0 until size) {
                if (iMusicInfos[i - count].get() == iMusicInfo || iMusicInfos[i - count].get() == null) {
                    iMusicInfos.remove(iMusicInfos[i - count])
                    count++
                }
            }
        }
    }

    fun refresh() {
        onMusicInfoUpdate(musicInfo)
        onNeteaseMusicInfoUpdate(neteaseMusicInfo)
    }

    private fun check() {
        val size = iMusicInfos.size
        var count = 0
        for (i in 0 until size) {
            if (iMusicInfos[i - count].get() == null) {
                iMusicInfos.remove(iMusicInfos[i - count])
                count++
            }
        }
    }
}