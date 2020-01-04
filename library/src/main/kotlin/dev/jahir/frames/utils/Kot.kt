package dev.jahir.frames.utils

import android.os.Handler

fun postDelayed(delay: Long, action: () -> Unit) {
    Handler().postDelayed(action, delay)
}