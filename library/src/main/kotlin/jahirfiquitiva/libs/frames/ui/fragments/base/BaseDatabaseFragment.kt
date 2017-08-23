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
package jahirfiquitiva.libs.frames.ui.fragments.base

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.TextView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDao
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.buildSnackbar
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.providers.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.ui.widgets.CheckableImageView
import jahirfiquitiva.libs.frames.ui.widgets.SimpleAnimationListener
import org.jetbrains.anko.runOnUiThread

@Suppress("NAME_SHADOWING")
abstract class BaseDatabaseFragment<in T, in VH:RecyclerView.ViewHolder>:BaseViewModelFragment<T>() {
    
    internal lateinit var database:FavoritesDatabase
    internal lateinit var favoritesModel:FavoritesViewModel
    
    internal var collection:Collection? = null
    internal var snack:Snackbar? = null
    
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
    
    override fun registerObserver() = favoritesModel.items.observe(this, Observer { data ->
        data?.let { doOnFavoritesChange(it) }
    })
    
    override fun loadDataFromViewModel() {
        arguments?.let {
            val argCollection = it.getParcelable<Collection>("collection")
            argCollection?.let {
                collection = it
            }
        }
        favoritesModel.loadData(getDatabase())
    }
    
    override fun unregisterObserver() {
        favoritesModel.items.removeObservers(this)
        favoritesModel.stopTask()
    }
    
    internal fun onHeartClicked(heart:CheckableImageView, item:Wallpaper) =
            animateHeartClick(heart, item, !heart.isChecked)
    
    open fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {}
    open fun doOnWallpapersChange(data:ArrayList<Wallpaper>,
                                  fromCollectionActivity:Boolean = false) {
    }
    
    internal fun getDatabase():FavoritesDao = database.favoritesDao()
    internal fun isInFavorites(item:Wallpaper) = favoritesModel.isInFavorites(item)
    internal fun addToFavorites(item:Wallpaper) = favoritesModel.addToFavorites(item)
    internal fun removeFromFavorites(item:Wallpaper) = favoritesModel.removeFromFavorites(item)
    
    override fun onItemClicked(item:T) {}
    
    abstract fun onItemClicked(item:T, holder:VH)
    
    private val ANIMATION_DURATION:Long = 250
    private fun animateHeartClick(heart:CheckableImageView, item:Wallpaper,
                                  check:Boolean) = context.runOnUiThread {
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
                            snack?.dismiss()
                            snack = null
                            snack = content.buildSnackbar(
                                    getString(R.string.added_to_favorites, item.name),
                                    Snackbar.LENGTH_SHORT)
                            snack?.view?.findViewById<TextView>(
                                    R.id.snackbar_text)?.setTextColor(Color.WHITE)
                            snack?.show()
                        } else {
                            removeFromFavorites(item)
                            snack?.dismiss()
                            snack = null
                            snack = content.buildSnackbar(
                                    getString(R.string.removed_from_favorites, item.name),
                                    Snackbar.LENGTH_SHORT)
                            snack?.view?.findViewById<TextView>(
                                    R.id.snackbar_text)?.setTextColor(Color.WHITE)
                            snack?.show()
                        }
                    }
                })
                heart.startAnimation(nScale)
            }
        })
        heart.startAnimation(scale)
    }
}