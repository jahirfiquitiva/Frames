package dev.jahir.frames.extensions

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R

fun View.showSnackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    config: Snackbar.() -> Unit = {}
) {
    val snack = Snackbar.make(this, text, duration)
    snack.apply(config)
    val textView: TextView? by snack.view.findView(R.id.snackbar_text)
    textView?.maxLines = 3
    if (!snack.isShownOrQueued) snack.show()
}


fun View.showSnackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    config: Snackbar.() -> Unit = {}
) {
    showSnackbar(context.getString(text), duration, config)
}

fun Activity.showSnackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    config: Snackbar.() -> Unit = {}
) {
    window.decorView.showSnackbar(text, duration, config)
}

fun Activity.showSnackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    config: Snackbar.() -> Unit = {}
) {
    showSnackbar(getString(text), duration, config)
}

fun Fragment.showSnackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    config: Snackbar.() -> Unit = {}
) {
    activity?.showSnackbar(text, duration, config)
        ?: view?.showSnackbar(text, duration, config)
}

fun Fragment.showSnackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    config: Snackbar.() -> Unit = {}
) {
    showSnackbar(context?.getString(text).orEmpty(), duration, config)
}