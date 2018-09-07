package com.xhhold.musicblur.model

import android.os.Parcel
import android.os.Parcelable

data class NeteaseLyric(var lyrics: List<LyricLine>?):Parcelable {
    constructor(parcel: Parcel) : this(parcel.createTypedArrayList(LyricLine))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(lyrics)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NeteaseLyric> {
        override fun createFromParcel(parcel: Parcel): NeteaseLyric {
            return NeteaseLyric(parcel)
        }

        override fun newArray(size: Int): Array<NeteaseLyric?> {
            return arrayOfNulls(size)
        }
    }
}