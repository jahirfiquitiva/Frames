package dev.jahir.frames.ui.fragments.base

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.resolveColor
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.resources.lighten
import dev.jahir.frames.extensions.resources.tint
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.widgets.StatefulRecyclerView

abstract class BaseFramesFragment<T> : Fragment(R.layout.fragment_recyclerview),
    StatefulRecyclerView.StateDrawableModifier {

    private val originalItems: ArrayList<T> = ArrayList()
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    var recyclerView: StatefulRecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView?.stateDrawableModifier = this

        recyclerView?.emptyText = getEmptyText()
        recyclerView?.emptyDrawable = getEmptyDrawable()

        recyclerView?.noSearchResultsText = getNoSearchResultsText()
        recyclerView?.noSearchResultsDrawable = getNoSearchResultsDrawable()

        recyclerView?.loadingText = getLoadingText()

        recyclerView?.itemAnimator = DefaultItemAnimator()
        swipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh)
        swipeRefreshLayout?.setOnRefreshListener { startRefreshing() }
        swipeRefreshLayout?.setColorSchemeColors(
            context?.resolveColor(R.attr.colorSecondary, 0) ?: 0
        )
        swipeRefreshLayout?.setProgressBackgroundColorSchemeColor(
            (context?.resolveColor(R.attr.colorSurface, 0) ?: 0).lighten(.1F)
        )
        (activity as? BaseFavoritesConnectedActivity<*>)?.repostWallpapersData(getRepostKey())
    }

    internal fun setRefreshEnabled(enabled: Boolean) {
        swipeRefreshLayout?.isEnabled = enabled
    }

    internal fun applyFilter(filter: String, closed: Boolean) {
        recyclerView?.searching = filter.hasContent() && !closed
        updateItemsInAdapter(
            if (filter.hasContent() && !closed)
                getFilteredItems(ArrayList(originalItems), filter)
            else originalItems
        )
        if (!closed) scrollToTop()
    }

    private fun startRefreshing() {
        val isRefreshing = swipeRefreshLayout?.isRefreshing ?: false
        if (isRefreshing) stopRefreshing()
        swipeRefreshLayout?.isRefreshing = true
        recyclerView?.loading = true
        loadData()
    }

    open fun loadData() {
        (activity as? BaseFavoritesConnectedActivity<*>)?.loadWallpapersData()
    }

    internal fun stopRefreshing() {
        Handler().postDelayed(10) {
            swipeRefreshLayout?.isRefreshing = false
            recyclerView?.loading = false
        }
    }

    internal fun scrollToTop() {
        recyclerView?.post { recyclerView?.smoothScrollToPosition(0) }
    }

    fun updateItems(newItems: ArrayList<T>, stillLoading: Boolean = false) {
        this.originalItems.clear()
        this.originalItems.addAll(newItems)
        updateItemsInAdapter(newItems)
        if (!stillLoading) stopRefreshing()
    }

    override fun modifyDrawable(drawable: Drawable?): Drawable? =
        try {
            drawable?.tint(context?.resolveColor(R.attr.colorOnSurface, 0) ?: 0)
        } catch (e: Exception) {
            drawable
        }

    abstract fun getFilteredItems(originalItems: ArrayList<T>, filter: String): ArrayList<T>
    abstract fun updateItemsInAdapter(items: ArrayList<T>)
    open fun getRepostKey(): Int = -1
    open fun getTargetActivityIntent(): Intent? = null

    @StringRes
    open fun getLoadingText(): Int = R.string.loading

    @StringRes
    open fun getEmptyText(): Int = R.string.nothing_found

    @StringRes
    open fun getNoSearchResultsText(): Int = R.string.no_results_found

    @DrawableRes
    open fun getEmptyDrawable(): Int = R.drawable.ic_empty_section

    @DrawableRes
    open fun getNoSearchResultsDrawable(): Int = R.drawable.ic_empty_results
}