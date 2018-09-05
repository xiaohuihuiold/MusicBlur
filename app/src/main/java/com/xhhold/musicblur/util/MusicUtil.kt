package com.xhhold.musicblur.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.xhhold.musicblur.R
import com.xhhold.musicblur.app.MyApplication

class MusicUtil {
    companion object {
        fun getAlbumArt(album_id: Int): Bitmap? {
            val mUriAlbums = "content://media/external/audio/albums"
            val projection = arrayOf("album_art")
            val cur = MyApplication.context.contentResolver.query(Uri.parse(mUriAlbums + "/" + Integer.toString
            (album_id)),
                    projection, null, null, null)
            var album_art: String? = null
            if (cur.count > 0 && cur.columnCount > 0) {
                cur.moveToNext()
                album_art = cur.getString(0)
            }
            cur.close()
            var bm: Bitmap? = null
            if (album_art != null) {
                bm = BitmapFactory.decodeFile(album_art)
            } else {
                bm = BitmapFactory.decodeResource(MyApplication.context.resources, R.drawable
                        .ic_launcher_foreground)
            }
            return bm
        }
    }
}