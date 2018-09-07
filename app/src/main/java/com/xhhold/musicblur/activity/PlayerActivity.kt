package com.xhhold.musicblur.activity

import android.os.Bundle
import com.xhhold.musicblur.R

import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setSupportActionBar(toolbar)
    }

    override fun onUpdateTime(current: Int, duration: Int) {

    }

}
