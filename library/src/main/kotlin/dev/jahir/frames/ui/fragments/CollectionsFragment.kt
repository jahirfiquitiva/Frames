package dev.jahir.frames.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.lower
import dev.jahir.frames.ui.activities.CollectionActivity
import dev.jahir.frames.ui.activities.ViewerActivity
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.adapters.CollectionsAdapter
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.ui.widgets.EmptyView
import dev.jahir.frames.ui.widgets.EmptyViewRecyclerView

class CollectionsFragment : BaseFramesFragment<Collection>() {

    private val collsAdapter: CollectionsAdapter by lazy { CollectionsAdapter { onClicked(it) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.adapter = collsAdapter
        val columnsCount = context?.resources?.getInteger(R.integer.collections_columns_count) ?: 1
        recyclerView?.layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
    }

    override fun updateItems(newItems: ArrayList<Collection>) {
        collsAdapter.collections = newItems
        collsAdapter.notifyDataSetChanged()
        super.updateItems(newItems)
    }

    override fun internalApplyFilter(
        filter: String,
        originalItems: ArrayList<Collection>,
        closed: Boolean
    ) {
        super.internalApplyFilter(filter, originalItems, closed)
        if (filter.hasContent()) {
            updateItems(ArrayList(originalItems.filter {
                it.name.lower().contains(filter.lower())
            }))
        } else {
            updateItems(originalItems)
        }
    }

    override fun onStateChanged(state: EmptyViewRecyclerView.State, emptyView: EmptyView?) {
        super.onStateChanged(state, emptyView)
        if (state == EmptyViewRecyclerView.State.EMPTY) {
            emptyView?.setEmpty(context?.getString(R.string.no_collections_found) ?: "")
        }
    }

    private fun onClicked(collection: Collection) {
        startActivityForResult(Intent(activity, CollectionActivity::class.java).apply {
            putExtra(COLLECTION_EXTRA, collection.name)
        }, CollectionActivity.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CollectionActivity.REQUEST_CODE &&
            resultCode == ViewerActivity.FAVORITES_MODIFIED_RESULT) {
            (activity as? BaseFavoritesConnectedActivity<*>)?.reloadData()
        }
    }

    companion object {
        internal const val TAG = "collections_fragment"
        internal const val COLLECTION_EXTRA = "collection"

        @JvmStatic
        fun create(list: ArrayList<Collection> = ArrayList()) =
            CollectionsFragment().apply { updateItems(list) }
    }
}