package dev.jahir.frames.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.jahir.frames.R
import dev.jahir.frames.utils.extensions.findView

open class BaseFramesFragment<T> : Fragment() {

    internal var items: ArrayList<T> = ArrayList()

    internal val swipeRefreshLayout: SwipeRefreshLayout? by findView(R.id.swipe_to_refresh)
    internal val recyclerView: RecyclerView? by findView(R.id.items_recyclerview)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false)
    }

    @CallSuper
    open fun updateItems(newItems: ArrayList<T>) {
        this.items.clear()
        this.items.addAll(newItems)
    }
}