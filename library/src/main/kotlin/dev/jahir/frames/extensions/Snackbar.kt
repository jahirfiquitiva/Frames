package dev.jahir.frames.extensions

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R

fun View.showSnackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    val snack = Snackbar.make(this, text, duration)
    try {
        snack.setAnchorView(anchorViewId)
    } catch (e: Exception) {
    }
    val textView: TextView? by snack.view.findView(R.id.snackbar_text)
    textView?.maxLines = 3
    snack.apply(config)
    if (!snack.isShownOrQueued) snack.show()
}

fun View.showSnackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    showSnackbar(context.getString(text), duration, anchorViewId, config)
}

fun Activity.showSnackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    contentView?.showSnackbar(text, duration, anchorViewId, config)
}

fun Activity.showSnackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    showSnackbar(getString(text), duration, anchorViewId, config)
}

fun Fragment.showSnackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    activity?.showSnackbar(text, duration, anchorViewId, config)
        ?: view?.showSnackbar(text, duration, anchorViewId, config)
}

fun Fragment.showSnackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
) {
    showSnackbar(context?.getString(text).orEmpty(), duration, anchorViewId, config)
}

inline val Activity.contentView: View?
    get() = (findViewById(android.R.id.content) as? ViewGroup)?.getChildAt(0)