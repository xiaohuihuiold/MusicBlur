package com.xhhold.musicblur.util

import com.xhhold.musicblur.app.MyApplication


class ScreenUtil {
    companion object {
        fun getStatusBarHeight(): Int {
            val resource = MyApplication.context.resources;
            val resourceId = resource.getIdentifier("status_bar_height", "dimen", "android")
            return resource.getDimensionPixelSize(resourceId)
        }

        fun getNavigationBarHeight(): Int {
            val resources = MyApplication.context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return resources.getDimensionPixelSize(resourceId)
        }
    }
}