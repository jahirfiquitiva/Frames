package dev.jahir.frames.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.jahir.frames.data.db.FramesDatabase
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.models.Favorite
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.network.framesHttpClient
import dev.jahir.frames.data.network.parseWallpapersJson
import dev.jahir.frames.extensions.context.isNetworkAvailable
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.utils.context
import dev.jahir.frames.extensions.utils.lazyMutableLiveData
import dev.jahir.frames.extensions.utils.postDelayed
import dev.jahir.frames.extensions.utils.tryToObserve
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONException
import java.io.IOException

internal typealias CollectionWithWallpapers = Pair<String, List<Wallpaper>>

@Suppress("unused", "RemoveExplicitTypeArguments")
open class WallpapersDataViewModel(application: Application) : AndroidViewModel(application) {

    private val wallpapersData: MutableLiveData<List<Wallpaper>> by lazyMutableLiveData()
    val wallpapers: List<Wallpaper>
        get() = wallpapersData.value.orEmpty()

    private val collectionsData: MutableLiveData<List<Collection>> by lazyMutableLiveData()
    val collections: List<Collection>
        get() = collectionsData.value.orEmpty()

    private val favoritesData: MutableLiveData<List<Wallpaper>> by lazyMutableLiveData()
    val favorites: List<Wallpaper>
        get() = favoritesData.value.orEmpty()

    internal var whenReady: (() -> Unit)? = null
    internal var errorListener: ((error: DataError) -> Unit)? = null

    open fun internalTransformWallpapersToCollections(wallpapers: List<Wallpaper>): List<Collection> {
        val collectionsMap: MutableMap<String, CollectionWithWallpapers> = hashMapOf()
        wallpapers.forEach { wallpaper ->
            val wallpaperCollections = (wallpaper.collections ?: "").replace("|", ",")
                .split(",")
                .map { it.trim() }
                .distinct()
            wallpaperCollections.forEach { collection ->
                val collectionKey = collection.lowercase()
                val currentItems = collectionsMap[collectionKey]?.second.orEmpty()
                collectionsMap[collectionKey] =
                    Pair(collection, ArrayList(currentItems).apply {
                        add(wallpaper)
                    })
            }
        }

        val importantCollectionsNames = arrayOf(
            "all", "featured", "new", "wallpaper of the day", "wallpaper of the week"
        )
        val sortedCollections = arrayListOf<CollectionWithWallpapers?>()
        importantCollectionsNames.forEach { key ->
            sortedCollections.add(collectionsMap[key])
            collectionsMap.remove(key)
        }
        sortedCollections.addAll(collectionsMap.map { it.value })

        var usedCovers = ArrayList<String>()
        return sortedCollections
            .filterNotNull()
            .filter { it.second.isNotEmpty() }
            .map { it ->
                Collection(it.first, wallpapers = it.second.distinctBy { it.url }).apply {
                    usedCovers = setupCover(usedCovers)
                }
            }
    }

    private suspend fun transformWallpapersToCollections(wallpapers: List<Wallpaper>): List<Collection> =
        withContext(IO) { internalTransformWallpapersToCollections(wallpapers) }

    private suspend fun getWallpapersFromDatabase(): List<Wallpaper> = withContext(IO) {
        try {
            FramesDatabase.getAppDatabase(context)?.wallpapersDao()
                ?.getAllWallpapers().orEmpty()
        } catch (e: Exception) {
            arrayListOf<Wallpaper>()
        }
    }

    private suspend fun saveWallpapers(wallpapers: List<Wallpaper>) = withContext(IO) {
        try {
            val database = FramesDatabase.getAppDatabase(context) ?: return@withContext
            val wallpapersDao = database.wallpapersDao() ?: return@withContext
            // One transaction, so readers never see the table empty between both calls
            database.runInTransaction {
                wallpapersDao.nuke()
                wallpapersDao.insertAll(wallpapers)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun internalAddToLocalFavorites(wallpapers: List<Wallpaper>): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()
            ?.insertAll(wallpapers.map { Favorite(it.url) })
        return true
    }

    private fun internalNukeAllLocalFavorites(): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.nuke()
        return true
    }

    open suspend fun internalGetFavorites(): List<Favorite> =
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.getAllFavorites().orEmpty()

    open suspend fun internalAddToFavorites(wallpaper: Wallpaper): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.insert(Favorite(wallpaper.url))
        return true
    }

    open suspend fun internalRemoveFromFavorites(wallpaper: Wallpaper): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.delete(Favorite(wallpaper.url))
        return true
    }

    suspend fun addAllToLocalFavorites(wallpapers: List<Wallpaper>): Boolean = withContext(IO) {
        try {
            internalAddToLocalFavorites(wallpapers)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun nukeLocalFavorites(): Boolean = withContext(IO) {
        try {
            internalNukeAllLocalFavorites()
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getFavorites(): List<Favorite> = withContext(IO) {
        val result = try {
            internalGetFavorites()
        } catch (e: Exception) {
            listOf<Favorite>()
        }
        result
    }

    private suspend fun safeAddToFavorites(wallpaper: Wallpaper): Boolean = withContext(IO) {
        try {
            internalAddToFavorites(wallpaper)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun safeRemoveFromFavorites(wallpaper: Wallpaper): Boolean = withContext(IO) {
        try {
            internalRemoveFromFavorites(wallpaper)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun handleWallpapersData(
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        newWallpapers: List<Wallpaper> = listOf(),
        force: Boolean = false,
        isPreload: Boolean = false,
    ) {
        val localWallpapers = try {
            getWallpapersFromDatabase()
        } catch (e: Exception) {
            arrayListOf<Wallpaper>()
        }

        val filteredWallpapers = if (newWallpapers.isNotEmpty()) {
            newWallpapers.filter { it.url.hasContent() }.distinctBy { it.url }
        } else localWallpapers

        val favorites = if (loadFavorites) getFavorites() else ArrayList()
        val actualNewWallpapers =
            filteredWallpapers.map { wall ->
                wall.apply {
                    this.isInFavorites = favorites.any { fav -> fav.url == wall.url }
                }
            }

        if (loadCollections) {
            val newCollections = transformWallpapersToCollections(actualNewWallpapers)
            val areTheSameCollections = areTheSameLists(collections, newCollections)
            if (!areTheSameCollections || collections.isEmpty() || force)
                postCollections(newCollections)
        }

        val areTheSameWallpapers = areTheSameLists(localWallpapers, actualNewWallpapers)
        if (!areTheSameWallpapers || wallpapers.isEmpty() || force) {
            if (!(localWallpapers.isEmpty() && actualNewWallpapers.isEmpty() && isPreload))
                postWallpapers(actualNewWallpapers)
        }


        if (loadFavorites) {
            val actualFavorites =
                actualNewWallpapers.filter { wllppr -> favorites.any { fav -> fav.url == wllppr.url } }
            val areTheSameFavorites = areTheSameLists(favorites, actualFavorites)
            if (!areTheSameFavorites || favorites.isEmpty() || force)
                postFavorites(actualFavorites)
        }
        if (!areTheSameWallpapers) saveWallpapers(actualNewWallpapers)
        postDelayed(10) { whenReady?.invoke() }
    }

    // An HTTP error throws instead of returning the error page as JSON
    private suspend fun fetchJson(url: String): String = withContext(IO) {
        framesHttpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code} for $url")
            response.body?.string().orEmpty()
        }
    }

    private suspend fun loadRemoteData(
        url: String = "",
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        force: Boolean = false,
        triggerErrorListener: Boolean = true,
    ) {
        if (!url.hasContent()) return
        val isLocalFile = url.startsWith("file://")
        if (!isLocalFile && !context.isNetworkAvailable()) {
            if (triggerErrorListener) errorListener?.invoke(DataError.NoNetwork)
            handleWallpapersData()
            return
        }
        try {
            val json = if (isLocalFile) {
                context.assets?.open(url.replace("file:///android_asset/", ""))
                    ?.bufferedReader()
                    ?.use { it.readText() }
            } else {
                fetchJson(url)
            }
            val remoteWallpapers = parseWallpapersJson(json.orEmpty())
            handleWallpapersData(loadCollections, loadFavorites, remoteWallpapers, force)
        } catch (e: Exception) {
            if (triggerErrorListener && e is JSONException)
                errorListener?.invoke(DataError.MalformedJson)
        }
    }

    fun loadData(
        url: String = "",
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        force: Boolean = false,
        triggerErrorListener: Boolean = true,
    ) {
        viewModelScope.launch {
            delay(10)
            handleWallpapersData(loadCollections, loadFavorites, listOf(), force, true)
            loadRemoteData(url, loadCollections, loadFavorites, force, triggerErrorListener)
        }
    }

    fun addToFavorites(wallpaper: Wallpaper): Boolean {
        var success = false
        viewModelScope.launch {
            success = safeAddToFavorites(wallpaper)
            delay(10)
            loadData("", loadCollections = false, loadFavorites = true, force = true)
        }
        return success
    }

    fun removeFromFavorites(wallpaper: Wallpaper): Boolean {
        var success = false
        viewModelScope.launch {
            success = safeRemoveFromFavorites(wallpaper)
            delay(10)
            loadData("", loadCollections = false, loadFavorites = true, force = true)
        }
        return success
    }

    private fun postWallpapers(result: List<Wallpaper>) {
        wallpapersData.value = null
        wallpapersData.postValue(result)
    }

    private fun postCollections(result: List<Collection>) {
        collectionsData.value = null
        collectionsData.postValue(result)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun postFavorites(result: List<Wallpaper>) {
        favoritesData.value = null
        favoritesData.postValue(result)
    }

    fun observeWallpapers(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        wallpapersData.tryToObserve(owner, onUpdated)
    }

    fun observeCollections(owner: LifecycleOwner, onUpdated: (List<Collection>) -> Unit) {
        collectionsData.tryToObserve(owner, onUpdated)
    }

    fun observeFavorites(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        favoritesData.tryToObserve(owner, onUpdated)
    }

    fun destroy(owner: LifecycleOwner) {
        wallpapersData.removeObservers(owner)
        collectionsData.removeObservers(owner)
        favoritesData.removeObservers(owner)
    }

    suspend fun findWallpaper(url: String?): Wallpaper? =
        withContext(IO) {
            if (url == null) return@withContext null
            FramesDatabase.getAppDatabase(context)?.wallpapersDao()
                ?.getWallpaperByUrl(url)
        }

    // Same elements in the same order. Linear, unlike an indexOf per element
    private fun <T> areTheSameLists(local: List<T>, remote: List<T>): Boolean = local == remote

    enum class DataError {
        None, MalformedJson, NoNetwork, Unknown
    }
}
