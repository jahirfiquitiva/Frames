/*
 * Copyright (c) 2017. Jahir Fiquitiva
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

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.persistence.room.Room
import android.content.Intent
import android.net.Uri
import ca.allanwang.kau.utils.isNetworkAvailable
import ca.allanwang.kau.utils.isWifiConnected
import com.google.android.apps.muzei.api.Artwork
import com.google.android.apps.muzei.api.MuzeiArtSource
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource
import com.google.android.apps.muzei.api.UserCommand
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.helpers.utils.PLAY_STORE_LINK_PREFIX
import jahirfiquitiva.libs.frames.providers.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.providers.viewmodels.ListViewModel
import jahirfiquitiva.libs.frames.providers.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.kauextensions.extensions.formatCorrectly
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.printError
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList


@Suppress("NAME_SHADOWING")
class FramesArtSource:RemoteMuzeiArtSource("FramesMuzeiArtSource"), LifecycleRegistryOwner {
    
    private val SHARE_COMMAND_ID = 1337
    
    private val lcRegistry = LifecycleRegistry(this)
    override fun getLifecycle():LifecycleRegistry = lcRegistry
    
    private var wallsVM:WallpapersViewModel? = null
    private var favsDB:FavoritesDatabase? = null
    private var favsVM:FavoritesViewModel? = null
    
    override fun onStartCommand(intent:Intent?, flags:Int, startId:Int):Int {
        intent?.let {
            val restart = it.getBooleanExtra("restart", false)
            val command = it.getStringExtra("service") ?: ""
            if (restart || command.hasContent()) tryToUpdate()
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    fun tryToUpdate() {
        try {
            onTryUpdate(MuzeiArtSource.UPDATE_REASON_USER_NEXT)
        } catch (e:Exception) {
            printError("Error updating Muzei: ${e.message}")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        val commands = ArrayList<UserCommand>()
        commands.add(UserCommand(MuzeiArtSource.BUILTIN_COMMAND_ID_NEXT_ARTWORK))
        commands.add(UserCommand(SHARE_COMMAND_ID, getString(R.string.share)))
        setUserCommands(commands)
    }
    
    override fun onCustomCommand(id:Int) {
        super.onCustomCommand(id)
        if (id == SHARE_COMMAND_ID) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, currentArtwork.title,
                                                         currentArtwork.byline, getAppName(),
                                                         PLAY_STORE_LINK_PREFIX + packageName))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    
    override fun onTryUpdate(reason:Int) {
        if (framesKonfigs.functionalDashboard && isNetworkAvailable) {
            if (framesKonfigs.refreshMuzeiOnWiFiOnly) {
                if (isWifiConnected) executeMuzeiUpdate()
            } else {
                executeMuzeiUpdate()
            }
        }
    }
    
    private fun executeMuzeiUpdate() {
        try {
            wallsVM = WallpapersViewModel()
            wallsVM?.setCustomObserver(object:ListViewModel.CustomObserver<ArrayList<Wallpaper>> {
                override fun onValuePosted(data:ArrayList<Wallpaper>) {
                    if (data.isNotEmpty()) {
                        val realData = getValidWallpapersList(data)
                        
                        if (framesKonfigs.muzeiCollections.contains("favorites", true)) {
                            favsDB = Room.databaseBuilder(this@FramesArtSource,
                                                          FavoritesDatabase::class.java,
                                                          DATABASE_NAME).build()
                            favsVM = FavoritesViewModel()
                            favsVM?.setCustomObserver(
                                    object:ListViewModel.CustomObserver<ArrayList<Wallpaper>> {
                                        override fun onValuePosted(data:ArrayList<Wallpaper>) {
                                            realData.addAll(getValidWallpapersList(data))
                                            if (realData.isEmpty()) return
                                            chooseRandomWallpaperAndPost(realData)
                                        }
                                    })
                            val dao = favsDB?.favoritesDao()
                            if (dao != null) {
                                favsVM?.loadData(dao, true)
                            } else {
                                if (realData.isEmpty()) return
                                chooseRandomWallpaperAndPost(realData)
                            }
                        } else {
                            if (realData.isEmpty()) return
                            chooseRandomWallpaperAndPost(realData)
                        }
                    }
                }
            })
            wallsVM?.loadData(this, true)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    
    private fun chooseRandomWallpaperAndPost(list:ArrayList<Wallpaper>) {
        list.distinct()
        val randomIndex = getRandomIndex(list.size)
        if (randomIndex < 0 || randomIndex >= list.size) return
        val wallpaper = list[randomIndex]
        publishToMuzei(wallpaper.name, wallpaper.author, wallpaper.url)
    }
    
    private fun getValidWallpapersList(original:ArrayList<Wallpaper>):ArrayList<Wallpaper> {
        val newList = ArrayList<Wallpaper>()
        original.forEach { if (validWallpaper(it)) newList.add(it) }
        newList.distinct()
        return newList
    }
    
    private fun validWallpaper(item:Wallpaper):Boolean {
        val collections = item.collections.split("[,|]".toRegex())
        val selected = framesKonfigs.muzeiCollections.split("[,|]".toRegex())
        if (collections.isEmpty() || selected.isEmpty()) return true
        for (collection in collections) {
            val correct = collection.formatCorrectly().replace("_", " ")
            selected.forEach {
                if (!(it.hasContent()) || it.equals(collection, true) || it.equals(correct, true))
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
        wallsVM?.stopTask(true)
        wallsVM?.items?.removeObservers(this)
        wallsVM = null
        favsVM?.stopTask(true)
        favsVM?.items?.removeObservers(this)
        favsVM = null
        favsDB?.close()
        favsDB = null
    }
    
    private fun getRandomIndex(maxValue:Int):Int = try {
        Random().nextInt(maxValue)
    } catch (e:Exception) {
        e.printStackTrace()
        0
    }
    
    private fun publishToMuzei(name:String, author:String, url:String) {
        publishArtwork(Artwork.Builder().title(name).byline(author).imageUri(
                Uri.parse(url)).viewIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url))).build())
        scheduleUpdate(System.currentTimeMillis() + convertRefreshIntervalToMillis(
                framesKonfigs.muzeiRefreshInterval))
        destroyViewModel()
    }
    
    private fun convertRefreshIntervalToMillis(interval:Int):Long {
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