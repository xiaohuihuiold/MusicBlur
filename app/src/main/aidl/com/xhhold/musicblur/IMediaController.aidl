package com.xhhold.musicblur;

import com.xhhold.musicblur.model.MusicInfo;
import com.xhhold.musicblur.IMediaControllerCallback;

interface IMediaController {
    void play();
    void pause();
    void next();
    void previous();
    void seekTo(in int time);
    void setLooping(in boolean isLooping);
    boolean isPlaying();
    boolean isLooping();
    int getMusicCurrentTime();
    int getMusicDurationTime();
    MusicInfo getMusicInfo();
    void addMediaControllerCallback(in IMediaControllerCallback iMediaControllerCallback);
    void removeMediaControllerCallback(in IMediaControllerCallback iMediaControllerCallback);
}
