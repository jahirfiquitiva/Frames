package dev.jahir.frames.data.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.google.gson.GsonBuilder
import dev.jahir.frames.data.db.FramesDatabase
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.data.network.WallpapersJSONService
import dev.jahir.frames.utils.extensions.isNetworkAvailable
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class WallpapersDataViewModel : ViewModel() {

    private val wallpapersData: MutableLiveData<List<Wallpaper>>? by lazy {
        MutableLiveData<List<Wallpaper>>()
    }

    private val collectionsData: MutableLiveData<ArrayList<Collection>>? by lazy {
        MutableLiveData<ArrayList<Collection>>()
    }

    val wallpapers: List<Wallpaper> = wallpapersData?.value.orEmpty()
    val collections: ArrayList<Collection> = ArrayList(collectionsData?.value.orEmpty())

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
            val actualCollections: ArrayList<Collection> = ArrayList()
            collections.forEach { collectionName ->
                val collection = Collection(collectionName)
                wallpapers
                    .filter { it.collections.orEmpty().contains(collectionName) }
                    .forEach { collection.push(it) }
                actualCollections.add(collection)
            }
            actualCollections
        }

    private suspend fun getWallpapersFromDatabase(context: Context): List<Wallpaper> =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.getAllWallpapers()
                .orEmpty()
        }

    private suspend fun saveWallpapers(context: Context, wallpapers: List<Wallpaper>) =
        withContext(IO) {
            FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.insertAll(wallpapers)
        }

    fun loadData(context: Context, url: String) {
        viewModelScope.launch {
            var wallpapers = getWallpapersFromDatabase(context)
            if (wallpapers.isEmpty() || context.isNetworkAvailable()) {
                wallpapers = try {
                    service.getJSON(url).filter { it.url.isNotEmpty() }
                } catch (e: Exception) {
                    listOf()
                }
            }
            postWallpapers(wallpapers)
            saveWallpapers(context, wallpapers)
            val collections = transformWallpapersToCollections(wallpapers)
            postCollections(collections)
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

    fun observeWallpapers(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        wallpapersData?.observe(owner, Observer<List<Wallpaper>> { r -> r?.let { onUpdated(it) } })
    }

    fun observeCollections(owner: LifecycleOwner, onUpdated: (ArrayList<Collection>) -> Unit) {
        collectionsData?.observe(
            owner,
            Observer<ArrayList<Collection>> { r -> r?.let { onUpdated(it) } })
    }

    fun destroy(owner: LifecycleOwner) {
        wallpapersData?.removeObservers(owner)
        collectionsData?.removeObservers(owner)
    }
}