package dev.jahir.frames.extensions.views

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.color
import dev.jahir.frames.extensions.context.resolveColor
import dev.jahir.frames.extensions.resources.withAlpha

fun FastScrollRecyclerView.attachSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout?) {
    swipeRefreshLayout ?: return
    setOnFastScrollStateChangeListener(object : OnFastScrollStateChangeListener {
        override fun onFastScrollStart() {
            swipeRefreshLayout.isEnabled = false
        }

        override fun onFastScrollStop() {
            swipeRefreshLayout.isEnabled = true
        }
    })
}

fun FastScrollRecyclerView.tint() {
    val trackColor = context.resolveColor(
        com.google.android.material.R.attr.colorOnSurface,
        context.color(R.color.onSurface)
    )
    setThumbColor(
        context.resolveColor(
            com.google.android.material.R.attr.colorSecondary,
            context.color(R.color.accent)
        )
    )
    setThumbInactiveColor(trackColor.withAlpha(.5F))
    setTrackColor(trackColor.withAlpha(.3F))
}
