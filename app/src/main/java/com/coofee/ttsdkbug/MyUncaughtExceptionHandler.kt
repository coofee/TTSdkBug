package com.coofee.ttsdkbug

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

class MyUncaughtExceptionHandler(
    private val name: String,
) : Thread.UncaughtExceptionHandler {

    companion object {
        val counter = AtomicInteger(0)
    }

    private val handler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    private val preHandlerName = if (handler is MyUncaughtExceptionHandler) {
        handler.name
    } else {
        handler?.toString()
    }

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
        counter.incrementAndGet()
        Log.e("MyUncaughtExceptionHandler", "attach $name, pre handler=$preHandlerName")
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val decrementAndGet = counter.decrementAndGet()
        Log.e(
            "MyUncaughtExceptionHandler",
            "uncaughtException by $name, pre handler=$preHandlerName",
            e
        )

        if (decrementAndGet == 0) {
            Log.e("MyUncaughtExceptionHandler", "all of MyUncaughtExceptionHandler has invoked.")
        } else {
            Log.e(
                "MyUncaughtExceptionHandler",
                "left $decrementAndGet MyUncaughtExceptionHandler doest not invoke."
            )
        }
        handler?.uncaughtException(t, e)
    }


}