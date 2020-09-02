package com.andy.globe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.andy.globe.util.StatusBarHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarHelper.translucent(this)
        StatusBarHelper.setStatusBarDarkMode(this)
        val fragment = GlobeFragment()
        supportFragmentManager.beginTransaction().add(R.id.globe_frame, fragment).commit()
    }
}
