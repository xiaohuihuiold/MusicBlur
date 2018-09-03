package com.xhhold.musicblur.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.xhhold.musicblur.service.PlayerService
import com.xhhold.musicblur.handler.CrashHandler
import com.xhhold.musicblur.manager.BlurManager

class MyApplication : Application() {

    companion object {
        lateinit var refWatcher: RefWatcher
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        refWatcher = LeakCanary.install(this)
        context = applicationContext

        CrashHandler.INSTANCE.init(context)

        val bitmap = BitmapFactory.decodeStream(assets.open("images/image1.jpg"))
        BlurManager.INSTANCE.updateBitmap(bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, PlayerService::class.java))
        } else {
            startService(Intent(this, PlayerService::class.java))
        }
    }

}