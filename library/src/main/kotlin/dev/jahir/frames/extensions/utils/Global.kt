package dev.jahir.frames.extensions.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

class SafeHandler : Handler(Looper.myLooper() ?: Looper.getMainLooper())

fun postDelayed(delay: Long, action: () -> Unit) {
    SafeHandler().postDelayed(action, delay)
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
    ensureBackgroundThread(callback)
}
