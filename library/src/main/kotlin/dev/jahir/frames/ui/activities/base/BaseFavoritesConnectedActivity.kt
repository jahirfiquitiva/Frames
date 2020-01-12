package dev.jahir.frames.ui.activities.base

import androidx.lifecycle.ViewModelProvider
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.utils.Prefs

abstract class BaseFavoritesConnectedActivity<out P : Prefs> : BaseSystemUIVisibilityActivity<P>() {

    internal val wallpapersViewModel: WallpapersDataViewModel by lazy {
        ViewModelProvider(this).get(WallpapersDataViewModel::class.java)
    }

    internal fun addToFavorites(wallpaper: Wallpaper) {
        wallpapersViewModel.addToFavorites(this, wallpaper)
    }

    internal fun removeFromFavorites(wallpaper: Wallpaper) {
        wallpapersViewModel.removeFromFavorites(this, wallpaper)
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
            e.printStackTrace()
        }
    }
}