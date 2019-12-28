package dev.jahir.frames.data.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.google.gson.GsonBuilder
import dev.jahir.frames.data.db.FramesDatabase
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.models.Favorite
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.network.WallpapersJSONService
import dev.jahir.frames.extensions.isNetworkAvailable
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class WallpapersDataViewModel : ViewModel() {

    private val favoritesData: MutableLiveData<List<Wallpaper>>? by lazy {
        MutableLiveData<List<Wallpaper>>()
    }
    private val wallpapersData: MutableLiveData<List<Wallpaper>>? by lazy {
        MutableLiveData<List<Wallpaper>>()
    }
    private val collectionsData: MutableLiveData<ArrayList<Collection>>? by lazy {
        MutableLiveData<ArrayList<Collection>>()
    }

    val wallpapers: List<Wallpaper> = wallpapersData?.value.orEmpty()
    val collections: ArrayList<Collection> = ArrayList(collectionsData?.value.orEmpty())
    val favorites: List<Wallpaper> = wallpapersData?.value.orEmpty()

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(WallpapersJSONService::class.java)
    }

    private suspend fun transformWallpapersToCollections(wallpapers: List<Wallpaper>): ArrayList<Collection> =
        withContext(IO) {
            val collections =
                wallpapers.joinToString(",") { it.collections ?: "" }
                    .replace("|", ",")
                    .split(",")
                    .distinct()
            val importantCollectionsNames = listOf(
                "all", "featured", "new", "wallpaper of the day", "wallpaper of the week"
            )
            val sortedCollectionsNames =
                listOf(importantCollectionsNames, collections).flatten().distinct()
            val actualCollections: ArrayList<Collection> = ArrayList()
            sortedCollectionsNames.forEach { collectionName ->
                val collection = Collection(collectionName)
                wallpapers.filter { it.collections.orEmpty().contains(collectionName, true) }
                    .distinctBy { it.url }
                    .forEach { collection.push(it) }
                if (collection.count > 0) actualCollections.add(collection)
            }
            actualCollections
        }

    private suspend fun getWallpapersFromDatabase(context: Context): List<Wallpaper> =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.getAllWallpapers().orEmpty()
        }

    private suspend fun saveWallpapers(context: Context, wallpapers: List<Wallpaper>) =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.insertAll(wallpapers)
        }

    private suspend fun getFavorites(context: Context): List<Favorite> =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.favoritesDao()?.getAllFavorites()
                .orEmpty()
        }

    private suspend fun internalRemoveFromFavorites(context: Context, wallpaper: Wallpaper) =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.favoritesDao()?.delete(Favorite(wallpaper.url))
        }

    private suspend fun internalAddToFavorites(context: Context, wallpaper: Wallpaper) =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.favoritesDao()?.insert(Favorite(wallpaper.url))
        }

    fun loadData(context: Context, url: String = "") {
        viewModelScope.launch {
            val favorites = getFavorites(context)
            var wallpapers = getWallpapersFromDatabase(context)
            if (wallpapers.isEmpty() || context.isNetworkAvailable()) {
                wallpapers = if (url.isNotEmpty() && url.isNotBlank()) {
                    try {
                        service.getJSON(url).filter { it.url.isNotEmpty() }
                    } catch (e: Exception) {
                        listOf<Wallpaper>()
                    }
                } else listOf()
            }
            wallpapers = wallpapers.map { wall ->
                wall.apply { this.isInFavorites = favorites.any { fav -> fav.url == wall.url } }
            }
            postWallpapers(wallpapers)
            saveWallpapers(context, wallpapers)

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
            internalAddToFavorites(context, wallpaper)
            loadData(context)
        }
    }

    fun removeFromFavorites(context: Context?, wallpaper: Wallpaper) {
        context ?: return
        viewModelScope.launch {
            internalRemoveFromFavorites(context, wallpaper)
            loadData(context)
        }
    }

    private fun postWallpapers(result: List<Wallpaper>) {
        wallpapersData?.value = null
        wallpapersData?.postValue(result)
    }

    private fun postCollections(result: ArrayList<Collection>) {
        collectionsData?.value = null
        collectionsData?.postValue(result)
    }

    private fun postFavorites(result: List<Wallpaper>) {
        favoritesData?.value = null
        favoritesData?.postValue(result)
    }

    fun observeWallpapers(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        wallpapersData?.observe(owner, Observer<List<Wallpaper>> { r -> r?.let { onUpdated(it) } })
    }

    fun observeCollections(owner: LifecycleOwner, onUpdated: (ArrayList<Collection>) -> Unit) {
        collectionsData?.observe(
            owner,
            Observer<ArrayList<Collection>> { r -> r?.let { onUpdated(it) } })
    }

    fun observeFavorites(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        favoritesData?.observe(owner, Observer<List<Wallpaper>> { r -> r?.let { onUpdated(it) } })
    }

    fun destroy(owner: LifecycleOwner) {
        wallpapersData?.removeObservers(owner)
        collectionsData?.removeObservers(owner)
        favoritesData?.removeObservers(owner)
    }
}