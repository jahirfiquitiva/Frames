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
import dev.jahir.frames.ui.activities.BaseFramesActivity
import dev.jahir.frames.ui.widgets.EmptyView
import dev.jahir.frames.ui.widgets.EmptyViewRecyclerView
import dev.jahir.frames.extensions.findView

open class BaseFramesFragment<T> : Fragment() {

    internal var items: ArrayList<T> = ArrayList()

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    internal val recyclerView: EmptyViewRecyclerView? by findView(R.id.items_recyclerview)
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
        swipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh)
        swipeRefreshLayout?.setOnRefreshListener { startRefreshing() }
    }

    private fun startRefreshing() {
        val isRefreshing = swipeRefreshLayout?.isRefreshing ?: false
        if (isRefreshing) stopRefreshing()
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
        swipeRefreshLayout?.isRefreshing = true
        (activity as? BaseFramesActivity)?.loadData()
    }

    internal fun stopRefreshing() {
        Handler().postDelayed(10) { swipeRefreshLayout?.isRefreshing = false }
    }

    @CallSuper
    open fun updateItems(newItems: ArrayList<T>) {
        this.items.clear()
        this.items.addAll(newItems)
        stopRefreshing()
    }
}