package com.xhhold.musicblur.manager

import com.xhhold.musicblur.model.MusicInfo
import com.xhhold.musicblur.model.NeteaseMusicInfo

interface IMusicInfo {
    fun onUpdate(index: Int)
    fun onMusicInfoUpdate(musicInfo: MusicInfo?)
    fun onNeteaseMusicInfoUpdate(neteaseMusicInfo: NeteaseMusicInfo?)
}