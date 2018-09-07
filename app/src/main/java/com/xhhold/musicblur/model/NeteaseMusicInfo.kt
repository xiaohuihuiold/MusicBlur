package com.xhhold.musicblur.model

import android.os.Parcel
import android.os.Parcelable

data class NeteaseMusicInfo(var id: Int?, var name: String?, var lyric: NeteaseLyric?):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readParcelable(NeteaseLyric::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeParcelable(lyric, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NeteaseMusicInfo> {
        override fun createFromParcel(parcel: Parcel): NeteaseMusicInfo {
            return NeteaseMusicInfo(parcel)
        }

        override fun newArray(size: Int): Array<NeteaseMusicInfo?> {
            return arrayOfNulls(size)
        }
    }
}