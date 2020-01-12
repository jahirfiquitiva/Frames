package dev.jahir.frames.ui.fragments.base

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.widgets.EmptyView
import dev.jahir.frames.ui.widgets.EmptyViewRecyclerView

open class BaseFramesFragment<T> : Fragment(), EmptyViewRecyclerView.StateChangeListener {

    internal var items: ArrayList<T> = ArrayList()

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    internal val recyclerView: EmptyViewRecyclerView? by findView(R.id.recycler_view)
    private val emptyView: EmptyView? by findView(R.id.empty_view)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.emptyView = emptyView
        recyclerView?.stateChangeListener = this
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        swipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh)
        swipeRefreshLayout?.setOnRefreshListener { startRefreshing() }
    }

    internal fun setRefreshEnabled(enabled: Boolean) {
        swipeRefreshLayout?.isEnabled = enabled
    }

    internal fun applyFilter(filter: String, originalItems: ArrayList<T>, closed: Boolean) {
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        internalApplyFilter(filter, originalItems, closed)
        if (!closed) scrollToTop()
    }

    internal open fun internalApplyFilter(
        filter: String,
        originalItems: ArrayList<T>,
        closed: Boolean
    ) {
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
        this.items.clear()
        this.items.addAll(newItems)
        stopRefreshing()
    }
}