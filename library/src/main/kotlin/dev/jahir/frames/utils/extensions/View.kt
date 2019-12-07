package dev.jahir.frames.utils.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)

inline fun <reified T : View> View.findView(@IdRes id: Int, logException: Boolean = false): Lazy<T?> {
    return lazy {
        try {
            findViewById<T>(id)
        } catch (e: Exception) {
            if (logException) e.printStackTrace()
            null
        }
    }
}