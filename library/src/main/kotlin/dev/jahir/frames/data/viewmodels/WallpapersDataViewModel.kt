package dev.jahir.frames.data.viewmodels

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import dev.jahir.frames.data.db.FramesDatabase
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.models.Favorite
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.network.WallpapersJSONService
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.isNetworkAvailable
import dev.jahir.frames.extensions.lazyMutableLiveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Suppress("unused")
abstract class WallpapersDataViewModel : ViewModel() {

    private val wallpapersData: MutableLiveData<List<Wallpaper>> by lazyMutableLiveData()
    val wallpapers: List<Wallpaper>
        get() = wallpapersData.value.orEmpty()

    private val collectionsData: MutableLiveData<ArrayList<Collection>> by lazyMutableLiveData()
    val collections: ArrayList<Collection>
        get() = ArrayList(collectionsData.value.orEmpty())

    private val favoritesData: MutableLiveData<List<Wallpaper>> by lazyMutableLiveData()
    val favorites: List<Wallpaper>
        get() = favoritesData.value.orEmpty()

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(WallpapersJSONService::class.java)
    }

    abstract fun internalTransformWallpapersToCollections(wallpapers: List<Wallpaper>): List<Collection>

    private suspend fun transformWallpapersToCollections(wallpapers: List<Wallpaper>): ArrayList<Collection> =
        withContext(IO) { ArrayList(internalTransformWallpapersToCollections(wallpapers)) }

    private suspend fun getWallpapersFromDatabase(context: Context): List<Wallpaper> =
        withContext(IO) {
            try {
                FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.getAllWallpapers()
                    .orEmpty()
            } catch (e: Exception) {
                arrayListOf()
            }
        }

    private suspend fun deleteAllWallpapers(context: Context) =
        withContext(IO) {
            try {
                FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.nuke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private suspend fun saveWallpapers(context: Context, wallpapers: List<Wallpaper>) =
        withContext(IO) {
            try {
                deleteAllWallpapers(context)
                FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.insertAll(wallpapers)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun internalAddToLocalFavorites(
        context: Context,
        wallpapers: List<Wallpaper>
    ): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()
            ?.insertAll(wallpapers.map { Favorite(it.url) })
        return true
    }

    private fun internalNukeAllLocalFavorites(context: Context): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.nuke()
        return true
    }

    open suspend fun internalGetFavorites(context: Context): List<Favorite> =
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.getAllFavorites().orEmpty()

    open suspend fun internalAddToFavorites(context: Context, wallpaper: Wallpaper): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.insert(Favorite(wallpaper.url))
        return true
    }

    open suspend fun internalRemoveFromFavorites(context: Context, wallpaper: Wallpaper): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.delete(Favorite(wallpaper.url))
        return true
    }

    suspend fun addAllToLocalFavorites(context: Context, wallpapers: List<Wallpaper>): Boolean =
        withContext(IO) {
            try {
                internalAddToLocalFavorites(context, wallpapers)
            } catch (e: Exception) {
                false
            }
        }

    suspend fun nukeLocalFavorites(context: Context): Boolean =
        withContext(IO) {
            try {
                internalNukeAllLocalFavorites(context)
            } catch (e: Exception) {
                false
            }
        }

    private suspend fun getFavorites(context: Context): List<Favorite> =
        withContext(IO) {
            val result = try {
                internalGetFavorites(context)
            } catch (e: Exception) {
                listOf()
            }
            result
        }

    private suspend fun addToFavorites(context: Context, wallpaper: Wallpaper): Boolean =
        withContext(IO) {
            try {
                internalAddToFavorites(context, wallpaper)
            } catch (e: Exception) {
                false
            }
        }

    private suspend fun removeFromFavorites(context: Context, wallpaper: Wallpaper): Boolean =
        withContext(IO) {
            try {
                internalRemoveFromFavorites(context, wallpaper)
            } catch (e: Exception) {
                false
            }
        }

    fun loadData(context: Context, url: String = "") {
        viewModelScope.launch {
            val favorites = getFavorites(context)

            val remoteWallpapers: List<Wallpaper> =
                if (context.isNetworkAvailable() && url.hasContent()) {
                    try {
                        service.getJSON(url)
                    } catch (e: Exception) {
                        arrayListOf()
                    }
                } else arrayListOf()

            val localWallpapers = try {
                getWallpapersFromDatabase(context)
            } catch (e: Exception) {
                arrayListOf()
            }

            val wallpapers =
                (if (remoteWallpapers.isNotEmpty()) remoteWallpapers else localWallpapers)
                    .filter { it.url.hasContent() }
                    .distinctBy { it.url }
                    .map { wall ->
                        wall.apply {
                            this.isInFavorites = favorites.any { fav -> fav.url == wall.url }
                        }
                    }

            saveWallpapers(context, wallpapers)
            postWallpapers(wallpapers)

            val actualFavorites =
                wallpapers.filter { wllppr -> favorites.any { fav -> fav.url == wllppr.url } }
            postFavorites(actualFavorites)

            val collections = transformWallpapersToCollections(wallpapers)
            postCollections(collections)
        }
    }

    fun addToFavorites(context: Context?, wallpaper: Wallpaper) {
        context ?: return
        viewModelScope.launch {
            addToFavorites(context, wallpaper)
            loadData(context)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun removeFromFavorites(context: Context?, wallpaper: Wallpaper) {
        context ?: return
        viewModelScope.launch {
            removeFromFavorites(context, wallpaper)
            try {
                Thread.sleep(10)
            } catch (e: Exception) {
            }
            loadData(context)
        }
    }

    private fun repostAllData(context: Context?) {
        context ?: return
        viewModelScope.launch {
            postWallpapers(wallpapers)
            val favorites = getFavorites(context)
            val actualFavorites =
                wallpapers.filter { wllppr -> favorites.any { fav -> fav.url == wllppr.url } }
            postFavorites(actualFavorites)
            postCollections(collections)
        }
    }

    fun repostData(context: Context?, key: Int) {
        context ?: return
        viewModelScope.launch {
            when (key) {
                1 -> postCollections(collections)
                0 -> postWallpapers(wallpapers)
                else -> repostAllData(context)
            }
        }
    }

    private fun postWallpapers(result: List<Wallpaper>) {
        wallpapersData.value = null
        wallpapersData.postValue(result)
    }

    private fun postCollections(result: ArrayList<Collection>) {
        collectionsData.value = null
        collectionsData.postValue(result)
    }

    private fun postFavorites(result: List<Wallpaper>) {
        favoritesData.value = null
        favoritesData.postValue(result)
    }

    fun observeWallpapers(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        wallpapersData.observe(owner, Observer { r -> r?.let { onUpdated(it) } })
    }

    fun observeCollections(owner: LifecycleOwner, onUpdated: (ArrayList<Collection>) -> Unit) {
        collectionsData.observe(owner, Observer { r -> r?.let { onUpdated(it) } })
    }

    fun observeFavorites(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        favoritesData.observe(owner, Observer { r -> r?.let { onUpdated(it) } })
    }

    fun destroy(owner: LifecycleOwner) {
        wallpapersData.removeObservers(owner)
        collectionsData.removeObservers(owner)
        favoritesData.removeObservers(owner)
    }
}