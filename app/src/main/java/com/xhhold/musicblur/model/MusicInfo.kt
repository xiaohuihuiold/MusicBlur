package com.xhhold.musicblur.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class MusicInfo(var isLocal: Boolean, var name: String?, var artist: String?, var path:
String?, var albumID: Int, var albumImage: Bitmap?) : Parcelable {

    constructor(name: String?, artist: String?, path:
    String?, albumID: Int, albumImage: Bitmap?) : this(true, name, artist, path, albumID, albumImage)

    constructor(name: String?, artist: String?, path:
    String?, albumImage: Bitmap?) : this(true, name, artist, path, 0, albumImage)

    constructor(name: String?, artist: String?, path:
    String?) : this(true, name, artist, path, 0, null)

    constructor(name: String?, artist: String?, path:
    String?, albumID: Int) : this(true, name, artist, path, albumID, null)

    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readParcelable(Bitmap::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isLocal) 1 else 0)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(path)
        parcel.writeInt(albumID)
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