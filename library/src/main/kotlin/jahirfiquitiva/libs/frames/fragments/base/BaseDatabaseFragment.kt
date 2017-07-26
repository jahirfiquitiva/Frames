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
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.models.db.FavoritesDao
import jahirfiquitiva.libs.frames.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.models.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.views.CheckableImageView
import jahirfiquitiva.libs.frames.views.SimpleAnimationListener
import org.jetbrains.anko.runOnUiThread

@Suppress("NAME_SHADOWING")
abstract class BaseDatabaseFragment<in T, in VH:RecyclerView.ViewHolder>:BaseViewModelFragment<T>() {

    internal lateinit var database:FavoritesDatabase
    internal lateinit var favoritesModel:FavoritesViewModel

    internal var collection:Collection? = null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
    }

    open fun initDatabase() {
        database = Room.databaseBuilder(context, FavoritesDatabase::class.java,
                                        DATABASE_NAME).build()
        initViewModel()
    }

    override fun initViewModel() {
        favoritesModel = ViewModelProviders.of(activity).get(FavoritesViewModel::class.java)
    }

    override fun registerObserver() {
        favoritesModel.items.observe(this, Observer<ArrayList<Wallpaper>> { data ->
            data?.let { doOnFavoritesChange(it) }
        })
    }

    override fun loadDataFromViewModel() {
        arguments?.let {
            val argCollection = it.getParcelable<Collection>("collection")
            argCollection?.let {
                if (it is Collection) {
                    collection = it
                }
            }
        }
        favoritesModel.loadData(getDatabase())
    }

    override fun unregisterObserver() {
        favoritesModel.items.removeObservers(this)
        favoritesModel.stopTask()
    }

    internal fun onHeartClicked(heart:CheckableImageView, item:Wallpaper) {
        animateHeartClick(heart, item, !heart.isChecked)
    }

    open fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {}
    open fun doOnWallpapersChange(data:ArrayList<Wallpaper>, fromCollection:Boolean = false) {}

    internal fun getDatabase():FavoritesDao = database.favoritesDao()
    internal fun isInFavorites(item:Wallpaper) = favoritesModel.isInFavorites(item)
    internal fun addToFavorites(item:Wallpaper) = favoritesModel.addToFavorites(item)
    internal fun removeFromFavorites(item:Wallpaper) = favoritesModel.removeFromFavorites(item)

    override fun onItemClicked(item:T) {
        // Do nothing
    }

    abstract fun onItemClicked(item:T, holder:VH)

    private val ANIMATION_DURATION:Long = 250
    private fun animateHeartClick(heart:CheckableImageView, item:Wallpaper, check:Boolean) {
        context.runOnUiThread {
            val scale = ScaleAnimation(1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5f,
                                       Animation.RELATIVE_TO_SELF, 0.5f)
            scale.duration = ANIMATION_DURATION
            scale.interpolator = LinearInterpolator()
            scale.setAnimationListener(object:SimpleAnimationListener() {
                override fun onEnd(animation:Animation) {
                    super.onEnd(animation)
                    heart.isChecked = check
                    val nScale = ScaleAnimation(0F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 0.5f,
                                                Animation.RELATIVE_TO_SELF, 0.5f)
                    nScale.duration = ANIMATION_DURATION
                    nScale.interpolator = LinearInterpolator()
                    nScale.setAnimationListener(object:SimpleAnimationListener() {
                        override fun onEnd(animation:Animation) {
                            super.onEnd(animation)
                            if (check) {
                                addToFavorites(item)
                            } else {
                                removeFromFavorites(item)
                            }
                        }
                    })
                    heart.startAnimation(nScale)
                }
            })
            heart.startAnimation(scale)
        }
    }
}