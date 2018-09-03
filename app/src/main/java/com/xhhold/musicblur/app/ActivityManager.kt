package com.xhhold.musicblur.app

import android.app.Activity
import java.lang.ref.WeakReference
import java.util.*

class ActivityManager private constructor() {

    private val activityList = LinkedList<WeakReference<Activity>>()


    companion object {
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ActivityManager()
        }
    }

    fun addActivity(activity: Activity) {
        activityList.add(WeakReference(activity))
    }

    fun removeActivity(activity: Activity) {
        for (weak in activityList) {
            if (weak.get() == activity) {
                activityList.remove(weak)
            }
        }
    }

    fun exitApp() {
        finishAllActivity()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun finishAllActivity() {
        for (weak in activityList) {
            weak.get()?.finish()
        }
        activityList.clear()
    }

}