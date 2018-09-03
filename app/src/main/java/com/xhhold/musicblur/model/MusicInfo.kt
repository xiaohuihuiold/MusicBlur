package com.xhhold.musicblur.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class MusicInfo(var name: String, var artist: String, var path: String, var albumImage: Bitmap)
    : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(Bitmap::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(path)
        parcel.writeParcelable(albumImage, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MusicInfo> {
        override fun createFromParcel(parcel: Parcel): MusicInfo {
            return MusicInfo(parcel)
        }

        override fun newArray(size: Int): Array<MusicInfo?> {
            return arrayOfNulls(size)
        }
    }

}