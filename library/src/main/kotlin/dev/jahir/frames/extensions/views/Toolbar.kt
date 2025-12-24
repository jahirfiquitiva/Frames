package dev.jahir.frames.extensions.views

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.color
import dev.jahir.frames.extensions.context.resolveColor
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.tint

fun Toolbar.tint(
    @ColorInt color: Int =
        context.resolveColor(
            com.google.android.material.R.attr.colorOnPrimary,
            context.color(R.color.onPrimary)
        )
) {
    tintBackIcon(color)
    tintMenu(color)
}

private fun Toolbar.tintBackIcon(
    @ColorInt color: Int =
        context.resolveColor(
            com.google.android.material.R.attr.colorOnPrimary,
            context.color(R.color.onPrimary)
        )
) {
    (0..childCount).forEach { i ->
        (getChildAt(i) as? ImageButton)?.drawable?.tint(color)
    }
    try {
        val field = Toolbar::class.java.getDeclaredField("mCollapseIcon")
        field.isAccessible = true
        val collapseIcon = field.get(this) as? Drawable
        field.set(this, collapseIcon?.tint(color))
    } catch (e: Exception) {
    }
}

private fun Toolbar.tintMenu(
    @ColorInt color: Int =
        context.resolveColor(
            com.google.android.material.R.attr.colorOnPrimary,
            context.color(R.color.onPrimary)
        )
) {
    menu?.tint(color)
    overflowIcon?.tint(color)
    @SuppressLint("PrivateResource")
    val overflowDescription =
        context.string(com.google.android.material.R.string.exposed_dropdown_menu_content_description)
    val outViews = ArrayList<View>()
    findViewsWithText(outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
    if (outViews.isEmpty()) return
    val overflow = outViews[0] as? AppCompatImageView
    overflow?.setImageDrawable(overflow.drawable.tint(color))
}

private fun Menu.tint(@ColorInt color: Int) {
    (0 until size()).forEach { i ->
        val item = getItem(i)
        item.icon?.tint(color)
        (item.actionView as? SearchView)?.tint(color)
    }
}
