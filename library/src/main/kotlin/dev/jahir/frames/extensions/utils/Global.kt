package dev.jahir.frames.extensions.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

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

internal suspend fun ensureBackgroundThreadSuspended(callback: () -> Unit) = withContext(Default) {
    if (isOnMainThread()) {
        Thread { callback() }.start()
    } else {
        callback()
    }
}