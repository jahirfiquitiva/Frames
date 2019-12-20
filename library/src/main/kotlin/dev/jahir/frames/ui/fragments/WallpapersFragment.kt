package dev.jahir.frames.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.ui.activities.BaseFramesActivity
import dev.jahir.frames.ui.activities.ViewerActivity
import dev.jahir.frames.ui.adapters.WallpapersAdapter
import dev.jahir.frames.ui.decorations.GridSpacingItemDecoration
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.utils.onClick
import dev.jahir.frames.utils.onFavClick
import dev.jahir.frames.utils.wallpapersAdapter

class WallpapersFragment : BaseFramesFragment<Wallpaper>() {

    private var isForFavs: Boolean = false
    private var wallsViewModel: WallpapersDataViewModel? = null
    private val wallsAdapter: WallpapersAdapter by lazy {
        wallpapersAdapter {
            onClick { launchViewer(it) }
            onFavClick { checked, wallpaper ->
                this@WallpapersFragment.onFavClick(checked, wallpaper)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView?.emptyText = context?.getString(
            if (isForFavs) R.string.no_favorites_found
            else R.string.no_wallpapers_found
        ) ?: ""
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

    private fun launchViewer(wallpaper: Wallpaper) {
        startActivity(
            Intent(activity, ViewerActivity::class.java)
                .apply {
                    putExtra(WALLPAPER_NAME_EXTRA, wallpaper.name)
                    putExtra(WALLPAPER_AUTHOR_EXTRA, wallpaper.author)
                    putExtra(WALLPAPER_URL_EXTRA, wallpaper.url)
                    putExtra(WALLPAPER_THUMB_EXTRA, wallpaper.thumbnail)
                }
        )
    }

    companion object {
        internal const val TAG = "Wallpapers"
        internal const val FAVS_TAG = "Favorites"

        internal const val WALLPAPER_NAME_EXTRA = "wallpaper_name"
        internal const val WALLPAPER_AUTHOR_EXTRA = "wallpaper_author"
        internal const val WALLPAPER_URL_EXTRA = "wallpaper_url"
        internal const val WALLPAPER_THUMB_EXTRA = "wallpaper_thumb"

        @JvmStatic
        fun create(
            list: ArrayList<Wallpaper> = ArrayList(),
            wallsViewModel: WallpapersDataViewModel? = null
        ) = WallpapersFragment().apply {
            updateItems(list)
            this.wallsViewModel = wallsViewModel
        }

        @JvmStatic
        fun createForFavs(
            list: ArrayList<Wallpaper> = ArrayList(),
            wallsViewModel: WallpapersDataViewModel? = null
        ) = create(list, wallsViewModel).apply { this.isForFavs = true }
    }
}