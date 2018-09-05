package com.xhhold.musicblur.util

import android.util.Log
import com.google.gson.JsonParser
import com.xhhold.musicblur.model.LyricLine
import com.xhhold.musicblur.model.MusicInfo
import com.xhhold.musicblur.model.NeteaseLyric
import java.util.ArrayList
import java.util.regex.Pattern

class LrcUtil {
    companion object {

        fun lrc2NeteaseLyric(lrcStr: String?): NeteaseLyric? {
            val pattern = """\[\d{2}:\d{2}\.\d{2,3}\]"""
            val lrcPattern = Pattern.compile(pattern)
            val lyricLines = ArrayList<LyricLine>()
            val lyricJson = JsonParser().parse(lrcStr).asJsonObject

            if (lyricJson.get("lrc") != null) {
                val lrcs = lyricJson.get("lrc").asJsonObject.get("lyric").asString
                        .split("\n")
                for ((i, lrc) in lrcs.withIndex()) {
                    val lrcMatcher = lrcPattern.matcher(lrc)
                    if (!lrcMatcher.find()) continue
                    val time = lrcMatcher.group(0)
                    val line = LyricLine(getTime(time.substring(1, time.length - 1)), lrc.substring(time.length, lrc.length), null)
                    lyricLines.add(line)
                }
            }

            if (lyricJson.get("tlyric") != null && !lyricJson.get("tlyric").asJsonObject.get
                    ("lyric").isJsonNull) {
                val tlyrics = lyricJson.get("tlyric").asJsonObject.get("lyric").asString
                        .split("\n")
                var index = 0
                for ((i, tlyric) in tlyrics.withIndex()) {
                    val tlyriMatcher = lrcPattern.matcher(tlyric)
                    if (!tlyriMatcher.find()) continue
                    val time = tlyriMatcher.group(0)
                    for (j in index until lyricLines.size) {
                        if (lyricLines[j].time == LrcUtil.getTime(time.substring(1, time.length - 1))) {
                            lyricLines[j].lyricTran = tlyric.substring(time.length, tlyric.length)
                            index = j
                            break
                        }
                    }

                }
            }
            /* for(lyricLine in lyricLines){
                 Log.e("Lrc", "\n${lyricLine.lyric}\n${lyricLine.lyricTran}")
             }*/
            return NeteaseLyric(lyricLines)
        }

        fun getTime(time: String): Int {
            val m = time.substring(0, 2).toInt()
            val s = time.substring(3, 5).toInt()
            val ss = time.substring(6, 8).toInt()
            return m * 60 * 1000 + s * 1000+ss*10
        }
    }
}