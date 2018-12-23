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

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.room.Room
import android.content.Intent
import android.net.Uri
import ca.allanwang.kau.utils.isNetworkAvailable
import ca.allanwang.kau.utils.isWifiConnected
import com.google.android.apps.muzei.api.Artwork
import com.google.android.apps.muzei.api.MuzeiArtSource
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource
import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.internal.ProtocolConstants
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.helpers.utils.FL
import jahirfiquitiva.libs.frames.helpers.utils.FramesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kext.extensions.formatCorrectly
import jahirfiquitiva.libs.kext.extensions.getAppName
import jahirfiquitiva.libs.kext.extensions.hasContent
import java.util.ArrayList
import java.util.Random
import java.util.concurrent.TimeUnit

@Suppress("LeakingThis")
open class FramesArtSource(name: String) : RemoteMuzeiArtSource(name), LifecycleOwner {
    
    companion object {
        private const val UPDATE_COMMAND_ID = 1001
        private const val SHARE_COMMAND_ID = 1337
    }
    
    private val configs: FramesKonfigs by lazy { FramesKonfigs(this) }
    
    private val lcRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): LifecycleRegistry = lcRegistry
    
    private var wallsVM: WallpapersViewModel? = null
    private var favsDB: FavoritesDatabase? = null
    private var favsVM: FavoritesViewModel? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val restart = it.getBooleanExtra("restart", false)
            val command = it.getStringExtra("service") ?: ""
            val updateCommand = it.getIntExtra(ProtocolConstants.EXTRA_COMMAND_ID, 0)
            if (restart || command.hasContent() || updateCommand == UPDATE_COMMAND_ID)
                tryToUpdate()
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    private fun tryToUpdate() {
        try {
            onTryUpdate(MuzeiArtSource.UPDATE_REASON_USER_NEXT)
        } catch (e: Exception) {
            FL.e("Error updating Muzei: ${e.message}")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        val commands = ArrayList<UserCommand>()
        commands.add(UserCommand(MuzeiArtSource.BUILTIN_COMMAND_ID_NEXT_ARTWORK))
        commands.add(UserCommand(SHARE_COMMAND_ID, getString(R.string.share)))
        setUserCommands(commands)
    }
    
    override fun onCustomCommand(id: Int) {
        super.onCustomCommand(id)
        if (id == SHARE_COMMAND_ID) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT, getString(
                R.string.share_text, currentArtwork.title,
                currentArtwork.byline, getAppName(),
                PLAY_STORE_LINK_PREFIX + packageName))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    
    override fun onTryUpdate(reason: Int) {
        if (configs.functionalDashboard && isNetworkAvailable) {
            if (configs.refreshMuzeiOnWiFiOnly) {
                if (isWifiConnected) executeMuzeiUpdate()
            } else {
                executeMuzeiUpdate()
            }
        }
    }
    
    private fun executeMuzeiUpdate() {
        try {
            wallsVM = WallpapersViewModel()
            wallsVM?.extraObserve {
                if (it.isNotEmpty()) {
                    val realData = getValidWallpapersList(ArrayList(it))
                    if (configs.muzeiCollections.contains("favorites", true)) {
                        favsDB = Room.databaseBuilder(
                            this@FramesArtSource,
                            FavoritesDatabase::class.java,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration().build()
                        favsVM = FavoritesViewModel()
                        favsVM?.extraObserve {
                            realData.addAll(getValidWallpapersList(ArrayList(it)))
                            realData.distinct()
                            if (realData.isNotEmpty()) chooseRandomWallpaperAndPost(realData)
                        }
                        val dao = favsDB?.favoritesDao()
                        if (dao != null) {
                            favsVM?.loadData(dao, true)
                        } else {
                            if (realData.isNotEmpty()) chooseRandomWallpaperAndPost(realData)
                        }
                    } else {
                        if (realData.isNotEmpty()) chooseRandomWallpaperAndPost(realData)
                    }
                }
            }
            wallsVM?.loadData(this, true)
        } catch (e: Exception) {
            FL.e(e.message)
        }
    }
    
    private fun chooseRandomWallpaperAndPost(list: ArrayList<Wallpaper>) {
        list.distinct()
        val randomIndex = getRandomIndex(list.size)
        if (randomIndex < 0 || randomIndex >= list.size) return
        val wallpaper = list[randomIndex]
        publishToMuzei(wallpaper.name, wallpaper.author, wallpaper.url)
        destroyViewModel()
    }
    
    private fun getValidWallpapersList(original: ArrayList<Wallpaper>): ArrayList<Wallpaper> {
        val newList = ArrayList<Wallpaper>()
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
    
    override fun onLowMemory() {
        super.onLowMemory()
        destroyViewModel()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        destroyViewModel()
    }
    
    private fun destroyViewModel() {
        wallsVM?.destroy(this)
        wallsVM = null
        favsVM?.destroy(this)
        favsVM = null
        favsDB?.close()
        favsDB = null
    }
    
    private fun getRandomIndex(maxValue: Int): Int = try {
        Random().nextInt(maxValue)
    } catch (e: Exception) {
        FL.e(e.message)
        0
    }
    
    private fun publishToMuzei(name: String, author: String, url: String) {
        publishArtwork(
            Artwork.Builder().title(name).byline(author).imageUri(
                Uri.parse(url)).viewIntent(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))).build())
        scheduleUpdate(
            System.currentTimeMillis() + convertRefreshIntervalToMillis(
                configs.muzeiRefreshInterval))
        destroyViewModel()
    }
    
    private fun convertRefreshIntervalToMillis(interval: Int): Long {
        when (interval) {
            0 -> return TimeUnit.MINUTES.toMillis(15)
            1 -> return TimeUnit.MINUTES.toMillis(30)
            2 -> return TimeUnit.MINUTES.toMillis(45)
            3 -> return TimeUnit.HOURS.toMillis(1)
            4 -> return TimeUnit.HOURS.toMillis(2)
            5 -> return TimeUnit.HOURS.toMillis(3)
            6 -> return TimeUnit.HOURS.toMillis(6)
            7 -> return TimeUnit.HOURS.toMillis(9)
            8 -> return TimeUnit.HOURS.toMillis(12)
            9 -> return TimeUnit.HOURS.toMillis(18)
            10 -> return TimeUnit.DAYS.toMillis(1)
            11 -> return TimeUnit.DAYS.toMillis(3)
            12 -> return TimeUnit.DAYS.toMillis(7)
            13 -> return TimeUnit.DAYS.toMillis(14)
            else -> return TimeUnit.DAYS.toMillis(1)
        }
    }
}