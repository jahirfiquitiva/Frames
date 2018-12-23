/*
 * Copyright (c) 2018. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.frames.data.services

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kext.extensions.formatCorrectly
import jahirfiquitiva.libs.kext.extensions.hasContent
import java.util.Random

@SuppressLint("NewApi")
class FramesArtWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams), LifecycleOwner {
    
    companion object {
        internal fun enqueueLoad() {
            val workManager = WorkManager.getInstance()
            workManager.enqueue(
                OneTimeWorkRequestBuilder<FramesArtWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build())
        }
    }
    
    private val configs: FramesKonfigs by lazy { FramesKonfigs(context) }
    private val client: String by lazy { "${context.packageName}.muzei" }
    
    private val lcRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): LifecycleRegistry = lcRegistry
    
    private var wallsVM: WallpapersViewModel? = null
    private var favsDB: FavoritesDatabase? = null
    private var favsVM: FavoritesViewModel? = null
    
    override fun doWork(): Result {
        try {
            wallsVM = WallpapersViewModel()
            wallsVM?.extraObserve {
                if (it.isNotEmpty()) {
                    val realData = getValidWallpapersList(ArrayList(it))
                    if (configs.muzeiCollections.contains("favorites", true)) {
                        favsDB =
                            Room.databaseBuilder(
                                applicationContext, FavoritesDatabase::class.java, DATABASE_NAME)
                                .fallbackToDestructiveMigration().build()
                        favsVM = FavoritesViewModel()
                        favsVM?.extraObserve {
                            realData.addAll(getValidWallpapersList(ArrayList(it)))
                            realData.distinct()
                            if (realData.isNotEmpty()) postWallpapers(realData)
                        }
                        val dao = favsDB?.favoritesDao()
                        if (dao != null) {
                            favsVM?.loadData(dao, true)
                        } else {
                            if (realData.isNotEmpty()) postWallpapers(realData)
                        }
                    } else {
                        if (realData.isNotEmpty()) postWallpapers(realData)
                    }
                }
            }
            wallsVM?.loadData(applicationContext, true)
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
    
    private fun postWallpapers(wallpapers: ArrayList<Wallpaper>) {
        val providerClient = ProviderContract.getProviderClient(applicationContext, client)
        providerClient.addArtwork(wallpapers.map { wallpaper ->
            Artwork().apply {
                token = wallpaper.url
                title = wallpaper.name
                byline = wallpaper.author
                attribution =
                    if (wallpaper.copyright.hasContent()) wallpaper.copyright else wallpaper.author
                persistentUri = Uri.parse(wallpaper.url)
                webUri = Uri.parse(wallpaper.url)
                metadata = wallpaper.url
            }
        })
        destroyViewModel()
    }
    
    private fun getValidWallpapersList(original: ArrayList<Wallpaper>): ArrayList<Wallpaper> {
        val newList = java.util.ArrayList<Wallpaper>()
        original.forEach { if (validWallpaper(it)) newList.add(it) }
        newList.distinct()
        return newList
    }
    
    private fun validWallpaper(item: Wallpaper): Boolean {
        val collections = item.collections.split("[,|]".toRegex())
        val selected = configs.muzeiCollections.split("[,|]".toRegex())
        if (collections.isEmpty() || selected.isEmpty()) return true
        for (collection in collections) {
            val correct = collection.formatCorrectly().replace("_", " ")
            selected.forEach {
                if (!it.hasContent() || it.equals(collection, true) || it.equals(correct, true))
                    return true
            }
        }
        return false
    }
    
    private fun getRandomIndex(maxValue: Int): Int = try {
        Random().nextInt(maxValue)
    } catch (e: Exception) {
        FL.e(e.message)
        0
    }
    
    private fun destroyViewModel() {
        wallsVM?.destroy(this)
        wallsVM = null
        favsVM?.destroy(this)
        favsVM = null
        favsDB?.close()
        favsDB = null
    }
}