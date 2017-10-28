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

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.arch.persistence.room.Room
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.ColorInt
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import jahirfiquitiva.libs.archhelpers.ui.fragments.ViewModelFragment
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDao
import jahirfiquitiva.libs.frames.data.models.db.FavoritesDatabase
import jahirfiquitiva.libs.frames.helpers.extensions.createHeartIcon
import jahirfiquitiva.libs.frames.helpers.utils.DATABASE_NAME
import jahirfiquitiva.libs.frames.providers.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.kauextensions.extensions.SimpleAnimationListener
import jahirfiquitiva.libs.kauextensions.extensions.actv
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.buildSnackbar
import jahirfiquitiva.libs.kauextensions.extensions.ctxt
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.runOnUiThread

@Suppress("NAME_SHADOWING")
abstract class BaseDatabaseFragment<in T, in VH : RecyclerView.ViewHolder> :
        ViewModelFragment<T>() {
    
    internal var database: FavoritesDatabase? = null
    internal var favoritesModel: FavoritesViewModel? = null
    
    internal var snack: Snackbar? = null
    
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
        initViewModel()
    }
    
    private fun initDatabase() {
        if (!(ctxt.getBoolean(R.bool.isFrames))) return
        if (database == null) {
            database = Room.databaseBuilder(
                    ctxt, FavoritesDatabase::class.java,
                    DATABASE_NAME).fallbackToDestructiveMigration().build()
        }
    }
    
    override fun initViewModel() {
        initFavoritesViewModel()
    }
    
    private fun initFavoritesViewModel() {
        if (!(ctxt.getBoolean(R.bool.isFrames))) return
        if (database == null) initDatabase()
        if (favoritesModel == null) {
            favoritesModel = ViewModelProviders.of(actv).get(FavoritesViewModel::class.java)
        }
    }
    
    override fun registerObserver() {
        initFavoritesViewModel()
        favoritesModel?.observe(
                this, {
            doOnFavoritesChange(ArrayList(it))
        })
    }
    
    override fun loadDataFromViewModel() {
        initFavoritesViewModel()
        getDatabase()?.let { favoritesModel?.loadData(it, true) }
    }
    
    @CallSuper
    override fun unregisterObserver() {
        favoritesModel?.destroy(this)
    }
    
    internal fun onHeartClicked(heart: ImageView, item: Wallpaper, @ColorInt color: Int) =
            animateHeartClick(heart, item, color, !isInFavorites(item))
    
    open fun doOnFavoritesChange(data: ArrayList<Wallpaper>) {}
    open fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {}
    
    internal fun getDatabase(): FavoritesDao? = database?.favoritesDao()
    
    internal fun isInFavorites(item: Wallpaper): Boolean =
            favoritesModel?.isInFavorites(item) == true
    
    internal fun addToFavorites(item: Wallpaper) =
            getDatabase()?.let {
                favoritesModel?.addToFavorites(it, item, { showErrorSnackbar() })
            } ?: showErrorSnackbar()
    
    internal fun removeFromFavorites(item: Wallpaper) =
            getDatabase()?.let {
                favoritesModel?.removeFromFavorites(it, item, { showErrorSnackbar() })
            } ?: showErrorSnackbar()
    
    abstract fun onItemClicked(item: T, holder: VH)
    abstract fun fromCollectionActivity(): Boolean
    abstract fun fromFavorites(): Boolean
    
    private val ANIMATION_DURATION: Long = 150
    private fun animateHeartClick(
            heart: ImageView, item: Wallpaper, @ColorInt color: Int,
            check: Boolean
                                 ) = ctxt.runOnUiThread {
        val scale = ScaleAnimation(
                1F, 0F, 1F, 0F, Animation.RELATIVE_TO_SELF, 0.5F,
                Animation.RELATIVE_TO_SELF, 0.5F)
        scale.duration = ANIMATION_DURATION
        scale.interpolator = LinearInterpolator()
        scale.setAnimationListener(
                object : SimpleAnimationListener() {
                    override fun onEnd(animation: Animation) {
                        super.onEnd(animation)
                        heart.setImageDrawable(
                                ctxt.createHeartIcon(check)?.applyColorFilter(color))
                        val nScale = ScaleAnimation(
                                0F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 0.5F,
                                Animation.RELATIVE_TO_SELF, 0.5F)
                        nScale.duration = ANIMATION_DURATION
                        nScale.interpolator = LinearInterpolator()
                        nScale.setAnimationListener(
                                object : SimpleAnimationListener() {
                                    override fun onEnd(animation: Animation) {
                                        super.onEnd(animation)
                                        postToFavorites(item, check)
                                    }
                                })
                        heart.startAnimation(nScale)
                    }
                })
        heart.startAnimation(scale)
    }
    
    internal fun postToFavorites(item: Wallpaper, check: Boolean) {
        try {
            if (check) addToFavorites(item) else removeFromFavorites(item)
            showFavsSnackbar(check, item.name)
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorSnackbar()
        }
    }
    
    private fun showFavsSnackbar(added: Boolean, name: String) {
        showSnackBar(
                getString(
                        if (added) R.string.added_to_favorites else R.string.removed_from_favorites,
                        name))
    }
    
    internal fun showErrorSnackbar() {
        showSnackBar(getString(R.string.action_error_content))
    }
    
    private fun showSnackBar(text: String) {
        snack?.dismiss()
        snack = null
        snack = view?.buildSnackbar(text, Snackbar.LENGTH_SHORT)
        snack?.view?.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
        snack?.show()
    }
}