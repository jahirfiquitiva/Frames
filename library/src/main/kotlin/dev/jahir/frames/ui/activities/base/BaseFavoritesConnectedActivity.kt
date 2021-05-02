package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.utils.lazyViewModel

abstract class BaseFavoritesConnectedActivity<out P : Preferences> :
    BaseSystemUIVisibilityActivity<P>() {

    open val wallpapersViewModel: WallpapersDataViewModel by lazyViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (shouldLoadFavorites() && canShowFavoritesButton())
            wallpapersViewModel.observeFavorites(this, ::onFavoritesUpdated)
    }

    internal fun addToFavorites(wallpaper: Wallpaper): Boolean {
        if (!canShowFavoritesButton()) return false
        if (canModifyFavorites()) return wallpapersViewModel.addToFavorites(wallpaper)
        onFavoritesLocked()
        return false
    }

    internal fun removeFromFavorites(wallpaper: Wallpaper): Boolean {
        if (!canShowFavoritesButton()) return false
        if (canModifyFavorites()) return wallpapersViewModel.removeFromFavorites(wallpaper)
        onFavoritesLocked()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        wallpapersViewModel.destroy(this)
    }

    internal fun loadWallpapersData(remote: Boolean = false, triggerErrorListener: Boolean = true) {
        wallpapersViewModel.loadData(
            if (remote) getDataUrl() else "",
            loadCollections = shouldLoadCollections(),
            loadFavorites = shouldLoadFavorites() && canShowFavoritesButton(),
            force = true,
            triggerErrorListener = triggerErrorListener
        )
    }

    open fun shouldLoadCollections(): Boolean = true
    open fun shouldLoadFavorites(): Boolean = true
    open fun canShowFavoritesButton(): Boolean = true
    open fun canModifyFavorites(): Boolean = true
    open fun onFavoritesLocked() {}
    open fun onFavoritesUpdated(favorites: List<Wallpaper>) {}
    open fun getDataUrl(): String = string(R.string.json_url)
}
