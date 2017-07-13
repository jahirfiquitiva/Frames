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
package jahirfiquitiva.libs.frames.fragments.base

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.models.db.FavoritesDao
import jahirfiquitiva.libs.frames.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.models.viewmodels.CollectionsViewModel
import jahirfiquitiva.libs.frames.models.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.models.viewmodels.WallpapersViewModel
import jahirfiquitiva.libs.frames.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.views.CheckableImageView
import jahirfiquitiva.libs.kauextensions.extensions.printError

abstract class BaseFramesFragment<in T>:BaseViewModelFragment<T>() {

    private lateinit var database:FavoritesDatabase
    private lateinit var wallpapersModel:WallpapersViewModel
    private lateinit var favoritesModel:FavoritesViewModel
    private lateinit var collectionsModel:CollectionsViewModel

    override fun onStart() {
        createDatabase()
        super.onStart()
    }

    fun createDatabase() {
        database = Room.databaseBuilder(context, FavoritesDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries().build()
        collectionsModel = ViewModelProviders.of(activity).get(CollectionsViewModel::class.java)
        wallpapersModel = ViewModelProviders.of(activity).get(WallpapersViewModel::class.java)
        favoritesModel = ViewModelProviders.of(activity).get(FavoritesViewModel::class.java)
    }

    override fun initViewModel() {
        // Do nothing
    }

    override fun registerObserver() {
        collectionsModel.items.observe(this, Observer<ArrayList<Collection>> { data ->
            data?.let { doOnCollectionsChange(it) }
        })
        wallpapersModel.items.observe(this, Observer<ArrayList<Wallpaper>> { data ->
            data?.let { doOnWallpapersChange(it) }
        })
        favoritesModel.items.observe(this, Observer<ArrayList<Wallpaper>> { data ->
            data?.let { doOnFavoritesChange(it) }
        })
    }

    override fun loadDataFromViewModel() {
        wallpapersModel.loadData(context)
        favoritesModel.loadData(getDatabase())
    }

    override fun unregisterObserver() {
        collectionsModel.items.removeObservers(this)
        wallpapersModel.items.removeObservers(this)
        favoritesModel.items.removeObservers(this)
    }

    fun getDatabase():FavoritesDao = database.favoritesDao()

    fun isInFavorites(item:Wallpaper) = favoritesModel.isInFavorites(item)
    fun addToFavorites(item:Wallpaper) = favoritesModel.addToFavorites(item)
    fun removeFromFavorites(item:Wallpaper) = favoritesModel.removeFromFavorites(item)

    fun onHeartClicked(heart:CheckableImageView, item:Wallpaper) {
        if (heart.isChecked) {
            if (!removeFromFavorites(item)) {
                context.printError("Wallpaper not removed from favorites.")
            }
        } else {
            if (!addToFavorites(item)) {
                context.printError("Wallpaper not added to favorites.")
            }
        }
    }

    open fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {}
    open fun doOnWallpapersChange(data:ArrayList<Wallpaper>) {
        collectionsModel.loadData(data)
    }
    open fun doOnCollectionsChange(data:ArrayList<Collection>) {}
}