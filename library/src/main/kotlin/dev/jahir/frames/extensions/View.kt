package dev.jahir.frames.extensions

import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.core.view.postDelayed

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

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visibleIf(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.goneIf(gone: Boolean) {
    visibility = if (gone) View.GONE else View.VISIBLE
}

fun View.showAndAnimate() {
    visible()
    (this as? ImageView)?.let {
        postDelayed(250) { (it.drawable as? Animatable)?.start() }
    }
}