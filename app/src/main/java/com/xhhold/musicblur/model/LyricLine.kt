package com.xhhold.musicblur.model

import android.os.Parcel
import android.os.Parcelable

data class LyricLine(var time: Int, var lyric: String?,var lyricTran: String?):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(time)
        parcel.writeString(lyric)
        parcel.writeString(lyricTran)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LyricLine> {
        override fun createFromParcel(parcel: Parcel): LyricLine {
            return LyricLine(parcel)
        }

        override fun newArray(size: Int): Array<LyricLine?> {
            return arrayOfNulls(size)
        }
    }
}