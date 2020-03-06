package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.utils.Prefs

abstract class BaseFavoritesConnectedActivity<out P : Prefs> : BaseSystemUIVisibilityActivity<P>() {

    internal val wallpapersViewModel: WallpapersDataViewModel by lazy {
        ViewModelProvider(this).get(WallpapersDataViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallpapersViewModel.observeFavorites(this, ::onFavoritesUpdated)
    }

    internal fun addToFavorites(wallpaper: Wallpaper) {
        if (canModifyFavorites()) wallpapersViewModel.addToFavorites(this, wallpaper)
        else onFavoritesLocked()
    }

    internal fun removeFromFavorites(wallpaper: Wallpaper) {
        if (canModifyFavorites()) wallpapersViewModel.removeFromFavorites(this, wallpaper)
        else onFavoritesLocked()
    }

    override fun onDestroy() {
        super.onDestroy()
        wallpapersViewModel.destroy(this)
    }

    internal fun loadData() {
        wallpapersViewModel.loadData(this, getString(R.string.json_url))
    }

    internal fun reloadData() {
        try {
            wallpapersViewModel.loadData(this)
        } catch (e: Exception) {
        }
    }

    internal fun repostData(key: Int) {
        try {
            wallpapersViewModel.repostData(this, key)
        } catch (e: Exception) {
        }
    }

    open fun canModifyFavorites(): Boolean = true
    open fun onFavoritesLocked() {}
    abstract fun onFavoritesUpdated(favorites: List<Wallpaper>)
}