package com.coofee.ttsdkbug

import android.app.Application
import android.util.Log
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdSdk


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        UncaughtExceptionHandlerStats.startTimerTask()

        MyUncaughtExceptionHandler("handler1")

        Thread {
            Thread.sleep(1000L)
            MyUncaughtExceptionHandler("handler2")
        }.start()

        MyUncaughtExceptionHandler("handler3")

        var root = UncaughtExceptionHandlerStats.dump()
        Log.e(UncaughtExceptionHandlerStats.TAG, "before ttsdk init, root=$root")

        // 使用 MyUncaughtExceptionHandler 过滤日志。
        // 1. 把穿山甲sdk初始化注释掉，最后一条日志是：MyUncaughtExceptionHandler: all of MyUncaughtExceptionHandler has invoked.
        // 2. 启用穿山甲sdk之后，最后一条日志是：MyUncaughtExceptionHandler: left 3 MyUncaughtExceptionHandler doest not invoke.
        // 综上，可以看到有3个handler没有收到崩溃回调。
        TTAdSdk.init(
            this, TTAdConfig.Builder()
                .appId("xxxx")
                .appName("sjsjjsjsjsjjsjs")
                .useTextureView(false)
                .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                .allowShowNotify(true)
                .allowShowPageWhenScreenLock(false)
                .debug(BuildConfig.DEBUG)
                .directDownloadNetworkType(
                    TTAdConstant.NETWORK_STATE_WIFI,
                    TTAdConstant.NETWORK_STATE_MOBILE
                )
                .supportMultiProcess(false)
                .build()
        )

        root = UncaughtExceptionHandlerStats.dump()
        Log.e(UncaughtExceptionHandlerStats.TAG, "after ttsdk init root=$root")

        Thread {
            MyUncaughtExceptionHandler("handler4")
        }.start()

        Thread {
            MyUncaughtExceptionHandler("handler5")
        }.start()

    }
}