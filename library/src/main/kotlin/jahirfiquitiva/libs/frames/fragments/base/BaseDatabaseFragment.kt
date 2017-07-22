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
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import jahirfiquitiva.libs.frames.extensions.run
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.models.db.FavoritesDao
import jahirfiquitiva.libs.frames.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.models.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.models.viewmodels.SimpleWallpapersViewModel
import jahirfiquitiva.libs.frames.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.views.CheckableImageView

abstract class BaseDatabaseFragment<in T, in VH:RecyclerView.ViewHolder>:BaseViewModelFragment<T>() {

    lateinit var database:FavoritesDatabase
    lateinit var simpleWallpapersModel:SimpleWallpapersViewModel
    lateinit var favoritesModel:FavoritesViewModel

    var collection:Collection? = null
        set(value) {
            field = value
            loadWallpapersFromCollection()
        }

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        createDatabase()
    }

    override fun onStart() {
        // createDatabase()
        super.onStart()
    }

    open fun createDatabase() {
        database = Room.databaseBuilder(context, FavoritesDatabase::class.java,
                                        DATABASE_NAME).build()
        simpleWallpapersModel = ViewModelProviders.of(activity).get(
                SimpleWallpapersViewModel::class.java)
        favoritesModel = ViewModelProviders.of(activity).get(FavoritesViewModel::class.java)
    }

    override fun initViewModel() {
        // Do nothing
    }

    override fun registerObserver() {
        simpleWallpapersModel.items.observe(this, Observer<ArrayList<Wallpaper>> { data ->
            data?.let { doOnWallpapersChange(it, true) }
        })
        favoritesModel.items.observe(this, Observer<ArrayList<Wallpaper>> { data ->
            data?.let { doOnFavoritesChange(it) }
        })
    }

    override fun loadDataFromViewModel() {
        favoritesModel.loadData(getDatabase())
        arguments?.let {
            val argCollection = it.getParcelable<Collection>("collection")
            argCollection?.let {
                if (it is Collection) {
                    collection = it
                    loadWallpapersFromCollection()
                }
            }
        }
    }

    override fun unregisterObserver() {
        favoritesModel.items.removeObservers(this)
        simpleWallpapersModel.items.removeObservers(this)
        favoritesModel.stopTask()
        simpleWallpapersModel.stopTask()
    }

    internal fun onHeartClicked(heart:CheckableImageView, item:Wallpaper) {
        if (heart.isChecked) {
            removeFromFavorites(item, { context.run { heart.isChecked = false } })
        } else {
            addToFavorites(item, { context.run { heart.isChecked = true } })
        }
    }

    fun loadWallpapersFromCollection() {
        collection?.let {
            try {
                simpleWallpapersModel.loadData(it)
            } catch (ignored:Exception) {
            }
        }
    }

    open fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {}
    open fun doOnWallpapersChange(data:ArrayList<Wallpaper>, fromCollection:Boolean = false) {}

    internal fun getDatabase():FavoritesDao = database.favoritesDao()
    internal fun isInFavorites(item:Wallpaper) = favoritesModel.isInFavorites(item)

    internal fun addToFavorites(item:Wallpaper, onSuccess:() -> Unit) =
            favoritesModel.addToFavorites(item, onSuccess)

    internal fun removeFromFavorites(item:Wallpaper, onSuccess:() -> Unit) =
            favoritesModel.removeFromFavorites(item, onSuccess)

    override fun onItemClicked(item:T) {
        // Do nothing
    }

    abstract fun onItemClicked(item:T, holder:VH)
}