package dev.jahir.frames.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.extensions.buildTransitionOptions
import dev.jahir.frames.ui.activities.BaseFramesActivity
import dev.jahir.frames.ui.activities.ViewerActivity
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.adapters.WallpapersAdapter
import dev.jahir.frames.ui.decorations.GridSpacingItemDecoration
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.ui.viewholders.WallpaperViewHolder
import dev.jahir.frames.ui.widgets.EmptyView
import dev.jahir.frames.ui.widgets.EmptyViewRecyclerView
import dev.jahir.frames.utils.onClick
import dev.jahir.frames.utils.onFavClick
import dev.jahir.frames.utils.wallpapersAdapter

class WallpapersFragment : BaseFramesFragment<Wallpaper>() {

    private var isForFavs: Boolean = false
    private var wallsViewModel: WallpapersDataViewModel? = null

    private val wallsAdapter: WallpapersAdapter by lazy {
        wallpapersAdapter {
            onClick { wall, holder -> launchViewer(wall, holder) }
            onFavClick { checked, wallpaper ->
                this@WallpapersFragment.onFavClick(checked, wallpaper)
            }
        }
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

    override fun onStateChanged(state: EmptyViewRecyclerView.State, emptyView: EmptyView?) {
        super.onStateChanged(state, emptyView)
        if (state == EmptyViewRecyclerView.State.EMPTY) {
            if (isForFavs) emptyView?.setImageDrawable(R.drawable.ic_empty_favorites)
            emptyView?.setEmpty(
                context?.getString(
                    if (isForFavs) R.string.no_favorites_found
                    else R.string.no_wallpapers_found
                ) ?: ""
            )
        }
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

    private fun launchViewer(wallpaper: Wallpaper, holder: WallpaperViewHolder) {
        val options = try {
            activity?.let {
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    it,
                    *(it.buildTransitionOptions(
                        arrayListOf(holder.image, holder.title, holder.author)
                    ))
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        startActivityForResult(
            Intent(activity, ViewerActivity::class.java)
                .apply {
                    putExtra(WALLPAPER_EXTRA, wallpaper)
                    putExtra(WALLPAPER_IN_FAVS_EXTRA, wallpaper.isInFavorites)
                    putExtra(ViewerActivity.CURRENT_WALL_POSITION, holder.adapterPosition)
                },
            ViewerActivity.REQUEST_CODE,
            options?.toBundle()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ViewerActivity.REQUEST_CODE &&
            resultCode == ViewerActivity.FAVORITES_MODIFIED_RESULT)
            (activity as? BaseFavoritesConnectedActivity)?.reloadData()
    }

    companion object {
        internal const val TAG = "Wallpapers"
        internal const val FAVS_TAG = "Favorites"

        internal const val WALLPAPER_EXTRA = "wallpaper"
        internal const val WALLPAPER_IN_FAVS_EXTRA = "wallpaper_in_favs"

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