@file:Suppress("unused")

package dev.jahir.frames.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IdRes
import androidx.customview.widget.ViewDragHelper

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

fun <T : View> T.showAndAnimate() {
    visible()
    (this as? ImageView)?.startAnimatable()
}

fun <T : View> T.visible(): T {
    visibility = View.VISIBLE
    return this
}

fun <T : View> T.invisible(): T {
    visibility = View.INVISIBLE
    return this
}

fun <T : View> T.gone(): T {
    visibility = View.GONE
    return this
}

fun <T : View> T.invisibleIf(invisible: Boolean): T =
    if (invisible) invisible() else visible()

fun <T : View> T.visibleIf(visible: Boolean): T = if (visible) visible() else gone()

fun <T : View> T.goneIf(gone: Boolean): T = visibleIf(!gone)

inline val View.isVisible: Boolean get() = visibility == View.VISIBLE

inline val View.isInvisible: Boolean get() = visibility == View.INVISIBLE

inline val View.isGone: Boolean get() = visibility == View.GONE

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)

/**
 * Set left margin to a value in px
 */
fun View.setMarginLeft(margin: Int) = setMargins(margin, KAU_LEFT)

/**
 * Set top margin to a value in px
 */
fun View.setMarginTop(margin: Int) = setMargins(margin, KAU_TOP)

/**
 * Set right margin to a value in px
 */
fun View.setMarginRight(margin: Int) = setMargins(margin, KAU_RIGHT)

/**
 * Set bottom margin to a value in px
 */
fun View.setMarginBottom(margin: Int) = setMargins(margin, KAU_BOTTOM)

/**
 * Set left and right margins to a value in px
 */
fun View.setMarginHorizontal(margin: Int) = setMargins(margin, KAU_HORIZONTAL)

/**
 * Set top and bottom margins to a value in px
 */
fun View.setMarginVertical(margin: Int) = setMargins(margin, KAU_VERTICAL)

/**
 * Set all margins to a value in px
 */
fun View.setMargin(margin: Int) = setMargins(margin, KAU_ALL)

/**
 * Base margin setter
 * returns true if setting is successful, false otherwise
 */
private fun View.setMargins(margin: Int, flag: Int): Boolean {
    val p = (layoutParams as? ViewGroup.MarginLayoutParams) ?: return false
    p.setMargins(
        if (flag and KAU_LEFT > 0) margin else p.leftMargin,
        if (flag and KAU_TOP > 0) margin else p.topMargin,
        if (flag and KAU_RIGHT > 0) margin else p.rightMargin,
        if (flag and KAU_BOTTOM > 0) margin else p.bottomMargin
    )
    return true
}

/**
 * Set left padding to a value in px
 */
fun View.setPaddingLeft(padding: Int) = setPadding(padding, KAU_LEFT)

/**
 * Set top padding to a value in px
 */
fun View.setPaddingTop(padding: Int) = setPadding(padding, KAU_TOP)

/**
 * Set right padding to a value in px
 */
fun View.setPaddingRight(padding: Int) = setPadding(padding, KAU_RIGHT)

/**
 * Set bottom padding to a value in px
 */
fun View.setPaddingBottom(padding: Int) = setPadding(padding, KAU_BOTTOM)

/**
 * Set left and right padding to a value in px
 */
fun View.setPaddingHorizontal(padding: Int) = setPadding(padding, KAU_HORIZONTAL)

/**
 * Set top and bottom padding to a value in px
 */
fun View.setPaddingVertical(padding: Int) = setPadding(padding, KAU_VERTICAL)

/**
 * Set all padding to a value in px
 */
fun View.setPadding(padding: Int) = setPadding(padding, KAU_ALL)

/**
 * Base padding setter
 */
private fun View.setPadding(padding: Int, flag: Int) {
    setPadding(
        if (flag and KAU_LEFT > 0) padding else paddingLeft,
        if (flag and KAU_TOP > 0) padding else paddingTop,
        if (flag and KAU_RIGHT > 0) padding else paddingRight,
        if (flag and KAU_BOTTOM > 0) padding else paddingBottom
    )
}

const val KAU_LEFT = ViewDragHelper.EDGE_LEFT
const val KAU_RIGHT = ViewDragHelper.EDGE_RIGHT
const val KAU_TOP = ViewDragHelper.EDGE_TOP
const val KAU_BOTTOM = ViewDragHelper.EDGE_BOTTOM
const val KAU_HORIZONTAL = KAU_LEFT or KAU_RIGHT
const val KAU_VERTICAL = KAU_TOP or KAU_BOTTOM
const val KAU_ALL = KAU_HORIZONTAL or KAU_VERTICAL