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
        wallpapersViewModel.observeFavorites(this, ::onFavoritesUpdated)
    }

    internal fun addToFavorites(wallpaper: Wallpaper): Boolean {
        if (!canShowFavoritesButton()) return false
        if (canModifyFavorites()) {
            wallpapersViewModel.addToFavorites(this, wallpaper)
            return true
        }
        onFavoritesLocked()
        return false
    }

    internal fun removeFromFavorites(wallpaper: Wallpaper): Boolean {
        if (!canShowFavoritesButton()) return false
        if (canModifyFavorites()) {
            wallpapersViewModel.removeFromFavorites(this, wallpaper)
            return true
        }
        onFavoritesLocked()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        wallpapersViewModel.destroy(this)
    }

    internal fun loadWallpapersData() {
        wallpapersViewModel.loadData(this, getDataUrl())
    }

    internal fun reloadWallpapersData() {
        try {
            wallpapersViewModel.loadData(this)
        } catch (e: Exception) {
        }
    }

    internal fun repostWallpapersData(key: Int) {
        try {
            wallpapersViewModel.repostData(this, key)
        } catch (e: Exception) {
        }
    }

    open fun canShowFavoritesButton(): Boolean = true
    open fun canModifyFavorites(): Boolean = true
    open fun onFavoritesLocked() {}
    open fun onFavoritesUpdated(favorites: List<Wallpaper>) {}
    open fun getDataUrl(): String = string(R.string.json_url)
}