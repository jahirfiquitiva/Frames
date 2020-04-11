package dev.jahir.frames.extensions.utils

import android.os.Handler
import android.os.Looper

fun postDelayed(delay: Long, action: () -> Unit) {
    Handler().postDelayed(action, delay)
}

private fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

internal fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread { callback() }.start()
    } else {
        callback()
    }
}