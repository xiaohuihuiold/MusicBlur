package com.xhhold.musicblur.handler

import android.content.Context
import android.content.Intent
import com.xhhold.musicblur.activity.BugActivity
import com.xhhold.musicblur.app.ActivityManager
import com.xhhold.musicblur.util.ExtraUtil


class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private var context: Context? = null

    companion object {
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CrashHandler()
        }
    }

    fun init(context: Context) {
        this.context = context
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val intent = Intent(context, BugActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(ExtraUtil.EXTRA_BUG_THROWABLE, e)
        context?.startActivity(intent)
        ActivityManager.INSTANCE.exitApp()
    }

}