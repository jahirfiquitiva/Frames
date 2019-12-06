package dev.jahir.frames.data.viewmodels

import android.content.Context
import androidx.lifecycle.*
import dev.jahir.frames.data.db.FramesDatabase
import dev.jahir.frames.data.models.Favorite
import dev.jahir.frames.data.models.Wallpaper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesDataViewModel : ViewModel() {

    private val favoritesData: MutableLiveData<List<Wallpaper>>? by lazy {
        MutableLiveData<List<Wallpaper>>()
    }

    val favorites: List<Wallpaper> = favoritesData?.value.orEmpty()

    private suspend fun getFavoritesFromDatabase(context: Context): List<Favorite> =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.favoritesDao()?.getAllFavorites()
                .orEmpty()
        }

    private suspend fun saveWallpapers(context: Context, wallpapers: List<Wallpaper>) =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.favoritesDao()
                ?.insertAll(wallpapers.map { Favorite(it.url) })
        }

    private suspend fun getWallpapersFromDatabase(context: Context): List<Wallpaper> =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.getAllWallpapers().orEmpty()
        }

    fun loadData(context: Context) {
        viewModelScope.launch {
            val wallpapers = getWallpapersFromDatabase(context)
            val favorites = getFavoritesFromDatabase(context)
            val actualFavorites =
                wallpapers.filter { wllppr -> favorites.any { fav -> fav.url == wllppr.url } }
            postFavorites(actualFavorites)
        }
    }

    private fun postFavorites(result: List<Wallpaper>) {
        favoritesData?.value = null
        favoritesData?.postValue(result)
    }

    fun observeFavorites(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        favoritesData?.observe(owner, Observer<List<Wallpaper>> { r -> r?.let { onUpdated(it) } })
    }

    fun destroy(owner: LifecycleOwner) {
        favoritesData?.removeObservers(owner)
    }
}