package com.xhhold.musicblur.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xhhold.musicblur.R
import com.xhhold.musicblur.app.ActivityManager
import com.xhhold.musicblur.util.ExceptionUtil
import android.widget.TextView
import com.xhhold.musicblur.util.ExtraUtil
import kotlinx.android.synthetic.main.activity_bug.*


class BugActivity : AppCompatActivity() {

    private var mThrowable: Throwable? = null

    private var mExceptionInfo: ExceptionUtil? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityManager.INSTANCE.addActivity(this)
        setContentView(R.layout.activity_bug)

        val intent = intent
        if (intent != null) {
            try {
                mThrowable = intent.getSerializableExtra(ExtraUtil.EXTRA_BUG_THROWABLE) as Throwable
            } catch (e: Exception) {

            }
        }

        mExceptionInfo = ExceptionUtil(this)
        try {
            text_detial.text = ""
            mExceptionInfo?.printPhoneInfo(text_detial)
            if (mThrowable != null) {
                mExceptionInfo?.printError(text_detial, mThrowable)
            }
        } catch (e: Exception) {

        }

    }

    override fun onDestroy() {
        ActivityManager.INSTANCE.removeActivity(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        ActivityManager.INSTANCE.exitApp()
        super.onBackPressed()
    }
}
