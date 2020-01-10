package dev.jahir.frames.utils

import android.os.Handler
import android.os.Looper

fun postDelayed(delay: Long, action: () -> Unit) {
    Handler().postDelayed(action, delay)
}

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread { callback() }.start()
    } else {
        callback()
    }
}