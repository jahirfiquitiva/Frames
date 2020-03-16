package dev.jahir.frames.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.GridLayoutManager
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.buildTransitionOptions
import dev.jahir.frames.extensions.lower
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.ui.activities.CollectionActivity
import dev.jahir.frames.ui.activities.ViewerActivity
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.activities.base.BaseLicenseCheckerActivity
import dev.jahir.frames.ui.activities.base.BaseSystemUIVisibilityActivity
import dev.jahir.frames.ui.adapters.WallpapersAdapter
import dev.jahir.frames.ui.decorations.GridSpacingItemDecoration
import dev.jahir.frames.ui.fragments.base.BaseFramesFragment
import dev.jahir.frames.ui.viewholders.WallpaperViewHolder
import dev.jahir.frames.ui.widgets.EmptyView
import dev.jahir.frames.ui.widgets.EmptyViewRecyclerView
import dev.jahir.frames.utils.onClick
import dev.jahir.frames.utils.onFavClick
import dev.jahir.frames.utils.wallpapersAdapter

open class WallpapersFragment : BaseFramesFragment<Wallpaper>() {

    var isForFavs: Boolean = false

    private val wallsAdapter: WallpapersAdapter by lazy {
        wallpapersAdapter(
            canShowFavoritesButton(),
            (activity as? BaseFavoritesConnectedActivity<*>)?.canModifyFavorites() ?: true
        ) {
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

    override fun getFilteredItems(
        originalItems: ArrayList<Wallpaper>,
        filter: String,
        closed: Boolean
    ): ArrayList<Wallpaper> =
        ArrayList(originalItems.filter {
            it.name.lower().contains(filter.lower()) ||
                    it.collections.orEmpty().lower().contains(filter.lower()) ||
                    it.author.lower().contains(filter.lower())
        })

    private fun onFavClick(checked: Boolean, wallpaper: Wallpaper) {
        var updated = false
        (activity as? BaseFavoritesConnectedActivity<*>)?.let {
            if (it.canModifyFavorites()) {
                updated =
                    if (checked) it.addToFavorites(wallpaper) else it.removeFromFavorites(wallpaper)
            } else {
                it.onFavoritesLocked()
            }
        }
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
            getTargetActivityIntent()
                .apply {
                    putExtra(
                        BaseSystemUIVisibilityActivity.CAN_TOGGLE_SYSTEMUI_VISIBILITY_KEY,
                        canToggleSystemUIVisibility()
                    )
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
            (activity as? BaseFavoritesConnectedActivity<*>)?.reloadWallpapersData()
        }
    }

    override fun getRepostKey(): Int = if (isForFavs) 2 else 0
    override fun getTargetActivityIntent(): Intent = Intent(activity, ViewerActivity::class.java)

    open fun notifyCanModifyFavorites(canModify: Boolean = true) {
        wallsAdapter.canModifyFavorites = canModify
        wallsAdapter.notifyDataSetChanged()
    }

    open fun canShowFavoritesButton(): Boolean = true

    companion object {
        const val TAG = "wallpapers_fragment"
        const val FAVS_TAG = "favorites_fragment"

        internal const val WALLPAPER_EXTRA = "wallpaper"
        internal const val WALLPAPER_IN_FAVS_EXTRA = "wallpaper_in_favs"

        @JvmStatic
        fun create(
            list: ArrayList<Wallpaper> = ArrayList(),
            canModifyFavorites: Boolean = true
        ) = WallpapersFragment().apply {
            this.isForFavs = false
            notifyCanModifyFavorites(canModifyFavorites)
            updateItemsInAdapter(list)
        }

        @JvmStatic
        fun createForFavs(
            list: ArrayList<Wallpaper> = ArrayList(),
            canModifyFavorites: Boolean = true
        ) = WallpapersFragment().apply {
            this.isForFavs = true
            notifyCanModifyFavorites(canModifyFavorites)
            updateItemsInAdapter(list)
        }
    }
}