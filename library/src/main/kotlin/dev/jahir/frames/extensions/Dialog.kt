package dev.jahir.frames.extensions

import android.app.Dialog
import android.view.View
import androidx.annotation.IdRes

inline fun <reified T : View> Dialog.findView(
    @IdRes id: Int,
    logException: Boolean = false
): Lazy<T?> {
    return lazy {
        try {
            findViewById<T>(id)
        } catch (e: Exception) {
            if (logException) e.printStackTrace()
            null
        }
    }
}