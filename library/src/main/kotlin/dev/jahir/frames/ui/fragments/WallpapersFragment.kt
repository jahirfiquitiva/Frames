package dev.jahir.frames.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.buildTransitionOptions
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.lower
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.ui.activities.CollectionActivity
import dev.jahir.frames.ui.activities.ViewerActivity
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity
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

    internal var isForFavs: Boolean = false
        private set

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
            context?.resources?.getInteger(R.integer.wallpapers_columns_count) ?: 2
        recyclerView?.layoutManager =
            GridLayoutManager(context, columnsCount, GridLayoutManager.VERTICAL, false)
        recyclerView?.addItemDecoration(
            GridSpacingItemDecoration(
                columnsCount, resources.getDimensionPixelSize(R.dimen.grids_spacing)
            )
        )
        recyclerView?.adapter = wallsAdapter
        recyclerView?.state = EmptyViewRecyclerView.State.LOADING
    }

    override fun onStateChanged(state: EmptyViewRecyclerView.State, emptyView: EmptyView?) {
        super.onStateChanged(state, emptyView)
        if (state == EmptyViewRecyclerView.State.EMPTY) {
            if (isForFavs) emptyView?.setImageDrawable(R.drawable.ic_empty_favorites)
            emptyView?.setEmpty(
                context?.getString(
                    if (isForFavs) R.string.no_favorites_found else R.string.no_wallpapers_found
                ) ?: ""
            )
        }
    }

    override fun updateItemsInAdapter(items: ArrayList<Wallpaper>) {
        wallsAdapter.wallpapers = items
    }

    override fun getFilteredItems(filter: String, closed: Boolean): ArrayList<Wallpaper> {
        if (!filter.hasContent()) return originalItems
        return ArrayList(originalItems.filter {
            it.name.lower().contains(filter.lower()) ||
                    it.collections.orEmpty().lower().contains(filter.lower()) ||
                    it.author.lower().contains(filter.lower())
        })
    }

    private fun onFavClick(checked: Boolean, wallpaper: Wallpaper) {
        val updated = (if (checked) {
            (activity as? BaseFavoritesConnectedActivity<*>)?.addToFavorites(wallpaper)
        } else {
            (activity as? BaseFavoritesConnectedActivity<*>)?.removeFromFavorites(wallpaper)
        }) ?: false
        if (updated) (activity as? CollectionActivity)?.setFavoritesModified()
    }

    private fun launchViewer(wallpaper: Wallpaper, holder: WallpaperViewHolder) {
        val options = if (context?.prefs?.animationsEnabled == true) {
            try {
                activity?.let {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        it,
                        *(it.buildTransitionOptions(
                            arrayListOf(holder.image, holder.title, holder.author)
                        ))
                    )
                }
            } catch (e: Exception) {
                null
            }
        } else null
        startActivityForResult(
            Intent(activity, ViewerActivity::class.java)
                .apply {
                    putExtra(WALLPAPER_EXTRA, wallpaper)
                    putExtra(WALLPAPER_IN_FAVS_EXTRA, wallpaper.isInFavorites)
                    putExtra(ViewerActivity.CURRENT_WALL_POSITION, holder.adapterPosition)
                    putExtra(
                        ViewerActivity.LICENSE_CHECK_ENABLED,
                        (activity as? BaseLicenseCheckerActivity<*>)?.licenseCheckEnabled ?: false
                    )
                },
            ViewerActivity.REQUEST_CODE,
            options?.toBundle()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ViewerActivity.REQUEST_CODE &&
            resultCode == ViewerActivity.FAVORITES_MODIFIED_RESULT) {
            (activity as? CollectionActivity)?.setFavoritesModified()
            (activity as? BaseFavoritesConnectedActivity<*>)?.reloadData()
        }
    }

    override fun getRepostKey(): Int = if (isForFavs) 2 else 0

    companion object {
        internal const val TAG = "wallpapers_fragment"
        internal const val FAVS_TAG = "favorites_fragment"

        internal const val WALLPAPER_EXTRA = "wallpaper"
        internal const val WALLPAPER_IN_FAVS_EXTRA = "wallpaper_in_favs"

        @JvmStatic
        fun create(list: ArrayList<Wallpaper> = ArrayList()) =
            WallpapersFragment().apply {
                this.isForFavs = false
                updateItemsInAdapter(list)
            }

        @JvmStatic
        fun createForFavs(list: ArrayList<Wallpaper> = ArrayList()) =
            WallpapersFragment().apply {
                this.isForFavs = true
                updateItemsInAdapter(list)
            }
    }
}