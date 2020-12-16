package com.coofee.ttsdkbug

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.postDelayed({
            val intArrayOf = intArrayOf(0, 1, 2)
            Log.e("MainActivity", "value=${intArrayOf[6]}")
        }, 10_000L)
    }
}