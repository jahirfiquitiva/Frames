package dev.jahir.frames.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.context.integer
import dev.jahir.frames.extensions.resources.lower
import dev.jahir.frames.ui.activities.CollectionActivity
import dev.jahir.frames.ui.activities.ViewerActivity
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.adapters.CollectionsAdapter
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment

open class CollectionsFragment : BaseFramesFragment<Collection>() {

    private val collectionsAdapter: CollectionsAdapter by lazy { CollectionsAdapter { onClicked(it) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val columnsCount = context?.integer(R.integer.collections_columns_count, 1) ?: 1
        recyclerView?.layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
        recyclerView?.adapter = collectionsAdapter
    }

    override fun updateItemsInAdapter(items: ArrayList<Collection>) {
        collectionsAdapter.collections = items
    }

    override fun getFilteredItems(
        originalItems: ArrayList<Collection>,
        filter: String
    ): ArrayList<Collection> =
        ArrayList(originalItems.filter { it.name.lower().contains(filter.lower()) })

    open fun onClicked(collection: Collection) {
        startActivityForResult(
            getTargetActivityIntent()
                .apply {
                    putExtra(CollectionActivity.COLLECTION_KEY, collection)
                    putExtra(CollectionActivity.COLLECTION_NAME_KEY, collection.name)
                }, CollectionActivity.REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CollectionActivity.REQUEST_CODE &&
            resultCode == ViewerActivity.FAVORITES_MODIFIED_RESULT) {
            (activity as? BaseFavoritesConnectedActivity<*>)?.reloadWallpapersData()
        }
    }

    override fun getRepostKey(): Int = 1

    override fun getTargetActivityIntent(): Intent =
        Intent(activity, CollectionActivity::class.java)

    override fun getEmptyText(): Int = R.string.no_collections_found

    companion object {
        const val TAG = "collections_fragment"

        @JvmStatic
        fun create(list: ArrayList<Collection> = ArrayList()) =
            CollectionsFragment().apply { updateItemsInAdapter(list) }
    }
}