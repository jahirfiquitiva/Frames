package dev.jahir.frames.utils.extensions

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment


inline fun <reified T : View> Fragment.findView(@IdRes id: Int, logException: Boolean = false): Lazy<T?> {
    return lazy {
        try {
            view?.findViewById<T>(id)
        } catch (e: Exception) {
            if (logException) e.printStackTrace()
            null
        }
    }
}