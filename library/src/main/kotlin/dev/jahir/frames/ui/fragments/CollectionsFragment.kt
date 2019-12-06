package dev.jahir.frames.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.ui.adapters.CollectionsAdapter
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment

class CollectionsFragment : BaseFramesFragment<Collection>() {

    private val collsAdapter: CollectionsAdapter by lazy { CollectionsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val columnsCount = context?.resources?.getInteger(R.integer.collections_columns_count) ?: 1

        recyclerView?.adapter = collsAdapter
        recyclerView?.layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)

        swipeRefreshLayout?.setOnRefreshListener {
            swipeRefreshLayout?.isRefreshing = false
        }
    }

    override fun updateItems(newItems: ArrayList<Collection>) {
        super.updateItems(newItems)
        collsAdapter.collections = items
        collsAdapter.notifyDataSetChanged()
    }

    companion object {
        internal const val TAG = "Collections"

        @JvmStatic
        fun create(list: ArrayList<Collection> = ArrayList()) = CollectionsFragment().apply {
            updateItems(list)
        }
    }
}