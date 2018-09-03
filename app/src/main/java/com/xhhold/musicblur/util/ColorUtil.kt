package com.xhhold.musicblur.util

import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import android.support.v7.graphics.Palette


class ColorUtil {

    companion object {

        private val MIN_CONTRAST_RATIO = 2f

        fun isShouldDark(palette: Palette): Boolean {
            return !isLegibleOnWallpaper(Color.WHITE, palette.swatches)
        }

        private fun isLegibleOnWallpaper(color: Int, wallpaperSwatches: List<Palette.Swatch>): Boolean {
            var legiblePopulation = 0
            var illegiblePopulation = 0
            for (swatch in wallpaperSwatches) {
                if (isLegible(color, swatch.rgb)) {
                    legiblePopulation += swatch.population
                } else {
                    illegiblePopulation += swatch.population
                }
            }
            return legiblePopulation > illegiblePopulation
        }

        private fun isLegible(foreground: Int, background: Int): Boolean {
            var backgrounda = background
            backgrounda = ColorUtils.setAlphaComponent(background, 255)
            return ColorUtils.calculateContrast(foreground, backgrounda) >= MIN_CONTRAST_RATIO
        }
    }
}