package com.xhhold.musicblur;

import com.xhhold.musicblur.model.MusicInfo;

interface IMediaControllerCallback {
    void onStateChange(in int state);
    void onUpdateTime(in int current,in int duration);
    void onUpdateInfo(in MusicInfo musicInfo);
}
