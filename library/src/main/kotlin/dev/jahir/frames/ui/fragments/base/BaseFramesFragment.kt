package dev.jahir.frames.ui.fragments.base

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.widgets.EmptyView
import dev.jahir.frames.ui.widgets.EmptyViewRecyclerView

abstract class BaseFramesFragment<T> : Fragment(R.layout.fragment_recyclerview),
    EmptyViewRecyclerView.StateChangeListener {

    internal val originalItems: ArrayList<T> = ArrayList()
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    internal val recyclerView: EmptyViewRecyclerView? by findView(R.id.recycler_view)
    private val emptyView: EmptyView? by findView(R.id.empty_view)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.emptyView = emptyView
        recyclerView?.stateChangeListener = this
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        swipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh)
        swipeRefreshLayout?.setOnRefreshListener { startRefreshing() }
        (activity as? BaseFavoritesConnectedActivity<*>)?.repostData(getRepostKey())
    }

    internal fun setRefreshEnabled(enabled: Boolean) {
        swipeRefreshLayout?.isEnabled = enabled
    }

    internal fun applyFilter(filter: String, closed: Boolean) {
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        updateItemsInAdapter(getFilteredItems(filter, closed))
        if (!closed) scrollToTop()
    }

    private fun startRefreshing() {
        val isRefreshing = swipeRefreshLayout?.isRefreshing ?: false
        if (isRefreshing) stopRefreshing()
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        swipeRefreshLayout?.isRefreshing = true
        (activity as? BaseFavoritesConnectedActivity<*>)?.loadData()
    }

    internal fun stopRefreshing() {
        Handler().postDelayed(10) { swipeRefreshLayout?.isRefreshing = false }
    }

    internal fun scrollToTop() {
        recyclerView?.post { recyclerView?.smoothScrollToPosition(0) }
    }

    override fun onStateChanged(state: EmptyViewRecyclerView.State, emptyView: EmptyView?) {
        if (state == EmptyViewRecyclerView.State.LOADING) emptyView?.setLoading()
    }

    @CallSuper
    open fun updateItems(newItems: ArrayList<T>) {
        this.originalItems.clear()
        this.originalItems.addAll(newItems)
        updateItemsInAdapter(newItems)
        stopRefreshing()
    }

    abstract fun getFilteredItems(filter: String, closed: Boolean): ArrayList<T>
    abstract fun updateItemsInAdapter(items: ArrayList<T>)
    abstract fun getRepostKey(): Int
    abstract fun getTargetActivityIntent(): Intent

    open fun canToggleSystemUIVisibility(): Boolean = true
}