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
import dev.jahir.frames.extensions.context.isNetworkAvailable
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.utils.lazyMutableLiveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Suppress("unused", "RemoveExplicitTypeArguments")
open class WallpapersDataViewModel : ViewModel() {

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

    open fun internalTransformWallpapersToCollections(wallpapers: List<Wallpaper>): List<Collection> {
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

        var usedCovers = ArrayList<String>()
        val actualCollections: ArrayList<Collection> = ArrayList()
        sortedCollectionsNames.forEach { collectionName ->
            val collection = Collection(collectionName)
            wallpapers.filter { it.collections.orEmpty().contains(collectionName, true) }
                .distinctBy { it.url }
                .forEach { collection.push(it) }
            usedCovers = collection.setupCover(usedCovers)
            if (collection.count > 0) actualCollections.add(collection)
        }
        return actualCollections
    }

    private suspend fun transformWallpapersToCollections(wallpapers: List<Wallpaper>): ArrayList<Collection> =
        withContext(IO) { ArrayList(internalTransformWallpapersToCollections(wallpapers)) }

    private suspend fun getWallpapersFromDatabase(context: Context): List<Wallpaper> =
        withContext(IO) {
            try {
                FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.getAllWallpapers()
                    .orEmpty()
            } catch (e: Exception) {
                arrayListOf<Wallpaper>()
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
                delay(10)
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
                listOf<Favorite>()
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

    private suspend fun handleWallpapersData(
        context: Context? = null,
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        newWallpapers: List<Wallpaper> = listOf(),
        force: Boolean = false
    ) {
        context ?: return
        val localWallpapers = try {
            getWallpapersFromDatabase(context)
        } catch (e: Exception) {
            arrayListOf<Wallpaper>()
        }

        val filteredWallpapers = if (newWallpapers.isNotEmpty()) {
            newWallpapers.filter { it.url.hasContent() }.distinctBy { it.url }
        } else localWallpapers

        val areTheSame = areTheSameWallpapersLists(localWallpapers, filteredWallpapers)
        if (areTheSame && wallpapers.isNotEmpty() && !force) return

        val favorites = if (loadFavorites) getFavorites(context) else ArrayList()
        val actualNewWallpapers =
            filteredWallpapers.map { wall ->
                wall.apply {
                    this.isInFavorites = favorites.any { fav -> fav.url == wall.url }
                }
            }

        if (loadCollections) {
            val collections = transformWallpapersToCollections(actualNewWallpapers)
            postCollections(collections)
        }
        postWallpapers(actualNewWallpapers)

        if (loadFavorites) {
            val actualFavorites =
                actualNewWallpapers.filter { wllppr -> favorites.any { fav -> fav.url == wllppr.url } }
            postFavorites(actualFavorites)
        }
        saveWallpapers(context, actualNewWallpapers)
    }

    private suspend fun loadRemoteData(
        context: Context? = null,
        url: String = "",
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        force: Boolean = false
    ) {
        context ?: return
        if (!context.isNetworkAvailable()) return
        if (!url.hasContent()) return
        try {
            val remoteWallpapers = service.getJSON(url)
            handleWallpapersData(context, loadCollections, loadFavorites, remoteWallpapers, force)
        } catch (e: Exception) {
        }
    }

    fun loadData(
        context: Context? = null,
        url: String = "",
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        force: Boolean = false
    ) {
        context ?: return
        viewModelScope.launch {
            handleWallpapersData(context, loadCollections, loadFavorites, listOf(), force)
            loadRemoteData(context, url, loadCollections, loadFavorites, force)
        }
    }

    fun addToFavorites(context: Context?, wallpaper: Wallpaper) {
        context ?: return
        viewModelScope.launch {
            addToFavorites(context, wallpaper)
            delay(10)
            loadData(context, "", loadCollections = false, loadFavorites = true, force = true)
        }
    }

    fun removeFromFavorites(context: Context?, wallpaper: Wallpaper) {
        context ?: return
        viewModelScope.launch {
            removeFromFavorites(context, wallpaper)
            delay(10)
            loadData(context, "", loadCollections = false, loadFavorites = true, force = true)
        }
    }

    private fun repostAllData(context: Context?) {
        context ?: return
        viewModelScope.launch {
            postWallpapers(ArrayList(wallpapers))
            val favorites = getFavorites(context)
            val actualFavorites =
                wallpapers.filter { wllppr -> favorites.any { fav -> fav.url == wllppr.url } }
            postFavorites(actualFavorites)
            postCollections(ArrayList(collections))
        }
    }

    fun repostData(context: Context?, key: Int) {
        context ?: return
        viewModelScope.launch {
            when (key) {
                1 -> postCollections(ArrayList(collections))
                0 -> postWallpapers(ArrayList(wallpapers))
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

    @Suppress("MemberVisibilityCanBePrivate")
    fun postFavorites(result: List<Wallpaper>) {
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

    private fun areTheSameWallpapersLists(
        local: List<Wallpaper>,
        remote: List<Wallpaper>
    ): Boolean {
        try {
            var areTheSame = true
            for ((index, wallpaper) in remote.withIndex()) {
                if (local.indexOf(wallpaper) != index) {
                    areTheSame = false
                    break
                }
            }
            if (!areTheSame) return false
            val difference = ArrayList(remote).apply { removeAll(local) }.size
            return difference <= 0 && remote.size == local.size
        } catch (e: Exception) {
            return false
        }
    }
}