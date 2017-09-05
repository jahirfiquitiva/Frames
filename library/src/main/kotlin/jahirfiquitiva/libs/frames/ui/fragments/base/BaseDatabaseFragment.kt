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
import android.widget.ImageView
import android.widget.TextView
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDao
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.buildSnackbar
import jahirfiquitiva.libs.frames.helpers.extensions.createHeartIcon
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.providers.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.ui.widgets.SimpleAnimationListener
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import org.jetbrains.anko.runOnUiThread

@Suppress("NAME_SHADOWING")
abstract class BaseDatabaseFragment<in T, in VH:RecyclerView.ViewHolder>:BaseViewModelFragment<T>() {
    
    internal var database:FavoritesDatabase? = null
    internal var favoritesModel:FavoritesViewModel? = null
    
    internal var snack:Snackbar? = null
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
        initViewModel()
    }
    
    private fun initDatabase() {
        if (!(context.getBoolean(R.bool.isFrames))) return
        if (database == null) {
            database = Room.databaseBuilder(context, FavoritesDatabase::class.java,
                                            DATABASE_NAME).fallbackToDestructiveMigration().build()
        }
    }
    
    override fun initViewModel() {
        initFavoritesViewModel()
    }
    
    private fun initFavoritesViewModel() {
        if (!(context.getBoolean(R.bool.isFrames))) return
        if (database == null) initDatabase()
        if (favoritesModel == null) {
            favoritesModel = ViewModelProviders.of(activity).get(FavoritesViewModel::class.java)
        }
    }
    
    override fun registerObserver() {
        initFavoritesViewModel()
        favoritesModel?.items?.observe(this, Observer { data ->
            data?.let { doOnFavoritesChange(it) }
        })
    }
    
    override fun loadDataFromViewModel() {
        initFavoritesViewModel()
        getDatabase()?.let { favoritesModel?.loadData(it, true) }
    }
    
    override fun unregisterObserver() {
        favoritesModel?.items?.removeObservers(this)
        favoritesModel?.stopTask(true)
    }
    
    internal fun onHeartClicked(heart:ImageView, item:Wallpaper) =
            animateHeartClick(heart, item, !isInFavorites(item))
    
    open fun doOnFavoritesChange(data:ArrayList<Wallpaper>) {}
    open fun doOnWallpapersChange(data:ArrayList<Wallpaper>, fromCollectionActivity:Boolean) {}
    
    internal fun getDatabase():FavoritesDao? = database?.favoritesDao()
    
    internal fun isInFavorites(item:Wallpaper):Boolean =
            favoritesModel?.isInFavorites(item) ?: false
    
    internal fun addToFavorites(item:Wallpaper) =
            favoritesModel?.addToFavorites(item, { showErrorSnackbar() })
    
    internal fun removeFromFavorites(item:Wallpaper) =
            favoritesModel?.removeFromFavorites(item, { showErrorSnackbar() })
    
    abstract fun onItemClicked(item:T, holder:VH)
    abstract fun fromCollectionActivity():Boolean
    abstract fun fromFavorites():Boolean
    
    private val ANIMATION_DURATION:Long = 150
    private fun animateHeartClick(heart:ImageView, item:Wallpaper,
                                  check:Boolean) = context.runOnUiThread {
        val scale = ScaleAnimation(1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5f,
                                   Animation.RELATIVE_TO_SELF, 0.5f)
        scale.duration = ANIMATION_DURATION
        scale.interpolator = LinearInterpolator()
        scale.setAnimationListener(object:SimpleAnimationListener() {
            override fun onEnd(animation:Animation) {
                super.onEnd(animation)
                heart.setImageDrawable(context.createHeartIcon(check))
                val nScale = ScaleAnimation(0F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 0.5f,
                                            Animation.RELATIVE_TO_SELF, 0.5f)
                nScale.duration = ANIMATION_DURATION
                nScale.interpolator = LinearInterpolator()
                nScale.setAnimationListener(object:SimpleAnimationListener() {
                    override fun onEnd(animation:Animation) {
                        super.onEnd(animation)
                        postToFavorites(item, check)
                    }
                })
                heart.startAnimation(nScale)
            }
        })
        heart.startAnimation(scale)
    }
    
    internal fun postToFavorites(item:Wallpaper, check:Boolean) {
        snack?.dismiss()
        snack = null
        try {
            if (check) addToFavorites(item) else removeFromFavorites(item)
            snack = content.buildSnackbar(
                    getString(
                            if (check) R.string.added_to_favorites else R.string.removed_from_favorites,
                            item.name),
                    Snackbar.LENGTH_SHORT)
            snack?.view?.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
            snack?.show()
        } catch (e:Exception) {
            e.printStackTrace()
            showErrorSnackbar()
        }
    }
    
    internal fun showErrorSnackbar() {
        snack?.dismiss()
        snack = null
        snack = content.buildSnackbar(getString(R.string.action_error_content),
                                      Snackbar.LENGTH_SHORT)
        snack?.view?.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
        snack?.show()
    }
}