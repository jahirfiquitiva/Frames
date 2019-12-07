package dev.jahir.frames.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.ui.activities.BaseFramesActivity
import dev.jahir.frames.ui.adapters.WallpapersAdapter
import dev.jahir.frames.ui.decorations.GridSpacingItemDecoration
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment

class WallpapersFragment : BaseFramesFragment<Wallpaper>() {

    var wallsViewModel: WallpapersDataViewModel? = null

    private val wallsAdapter: WallpapersAdapter by lazy {
        WallpapersAdapter { checked, wallpaper -> onFavClick(checked, wallpaper) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val columnsCount =
            context?.resources?.getInteger(R.integer.min_wallpapers_columns_count) ?: 2

        recyclerView?.adapter = wallsAdapter
        recyclerView?.layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
        recyclerView?.addItemDecoration(
            GridSpacingItemDecoration(
                columnsCount, resources.getDimensionPixelSize(R.dimen.grids_spacing)
            )
        )
        recyclerView?.itemAnimator = DefaultItemAnimator()
    }

    override fun updateItems(newItems: ArrayList<Wallpaper>) {
        super.updateItems(newItems)
        wallsAdapter.wallpapers = items
        wallsAdapter.notifyDataSetChanged()
        stopRefreshing()
    }

    private fun onFavClick(checked: Boolean, wallpaper: Wallpaper) {
        if (checked) {
            (activity as? BaseFramesActivity)?.addToFavorites(wallpaper)
                ?: wallsViewModel?.addToFavorites(context, wallpaper)
        } else {
            (activity as? BaseFramesActivity)?.removeFromFavorites(wallpaper)
                ?: wallsViewModel?.removeFromFavorites(context, wallpaper)
        }
    }

    companion object {
        internal const val TAG = "Wallpapers"
        internal const val FAVS_TAG = "Favorites"

        @JvmStatic
        fun create(
            list: ArrayList<Wallpaper> = ArrayList(),
            wallsViewModel: WallpapersDataViewModel? = null
        ) = WallpapersFragment().apply {
            updateItems(list)
            this.wallsViewModel = wallsViewModel
        }
    }
}