@file:Suppress("unused")

package dev.jahir.frames.extensions.views

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.color
import dev.jahir.frames.extensions.context.resolveColor
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.ui.activities.base.BaseStoragePermissionRequestActivity

fun View.snackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
): Snackbar {
    val snack = Snackbar.make(this, text, duration)
    try {
        snack.setAnchorView(anchorViewId)
    } catch (e: Exception) {
    }
    val textView: TextView? by snack.view.findView(R.id.snackbar_text)
    textView?.maxLines = 3
    snack.apply(config)
    snack.setBackgroundTint(
        context.resolveColor(
            R.attr.snackbarBackgroundColor,
            context.resolveColor(R.attr.colorSurface, context.color(R.color.surface))
        )
    )
    textView?.setTextColor(
        context.resolveColor(
            R.attr.snackbarTextColor,
            context.resolveColor(R.attr.colorOnSurface, context.color(R.color.onSurface))
        )
    )
    snack.setActionTextColor(
        context.resolveColor(
            R.attr.snackbarButtonColor,
            context.resolveColor(R.attr.colorSecondary, context.color(R.color.accent))
        )
    )
    if (!snack.isShownOrQueued) snack.show()
    return snack
}

fun View.snackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
): Snackbar = snackbar(context.string(text), duration, anchorViewId, config)

fun Activity.snackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = (this as? BaseStoragePermissionRequestActivity<*>)?.snackbarAnchorId
        ?: R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
): Snackbar? {
    (this as? BaseStoragePermissionRequestActivity<*>)?.let {
        try {
            it.currentSnackbar?.dismiss()
        } catch (e: Exception) {
        }
    }
    return contentView?.snackbar(text, duration, anchorViewId, config)
}

fun Activity.snackbar(
    @StringRes text: Int,
    @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = (this as? BaseStoragePermissionRequestActivity<*>)?.snackbarAnchorId
        ?: R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
): Snackbar? = snackbar(string(text), duration, anchorViewId, config)

fun Fragment.snackbar(
    text: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
): Snackbar? =
    activity?.snackbar(text, duration, anchorViewId, config)
        ?: view?.snackbar(text, duration, anchorViewId, config)

fun Fragment.snackbar(
    @StringRes text: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchorViewId: Int = R.id.bottom_navigation,
    config: Snackbar.() -> Unit = {}
): Snackbar? = snackbar(context?.string(text).orEmpty(), duration, anchorViewId, config)

inline val Activity.contentView: View?
    get() = (findViewById(android.R.id.content) as? ViewGroup)?.getChildAt(0)
