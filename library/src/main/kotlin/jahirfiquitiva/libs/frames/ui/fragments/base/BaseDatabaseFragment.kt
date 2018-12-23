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
package jahirfiquitiva.libs.frames.ui.fragments.base

import android.graphics.Color
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.createHeartIcon
import jahirfiquitiva.libs.frames.ui.activities.base.FavsDbManager
import jahirfiquitiva.libs.kext.extensions.SimpleAnimationListener
import jahirfiquitiva.libs.kext.extensions.applyColorFilter
import jahirfiquitiva.libs.kext.extensions.buildSnackbar
import jahirfiquitiva.libs.kext.extensions.context
import jahirfiquitiva.libs.kext.ui.fragments.ViewModelFragment
import org.jetbrains.anko.runOnUiThread

@Suppress("NAME_SHADOWING")
abstract class BaseDatabaseFragment<in T, in VH : RecyclerView.ViewHolder> : ViewModelFragment<T>() {
    
    companion object {
        private const val ANIMATION_DURATION: Long = 150
    }
    
    private var errorSnackbar: Snackbar? = null
    
    open fun doOnFavoritesChange(data: ArrayList<Wallpaper>) {}
    open fun doOnWallpapersChange(data: ArrayList<Wallpaper>, fromCollectionActivity: Boolean) {}
    
    abstract fun onItemClicked(item: T, holder: VH)
    abstract fun fromCollectionActivity(): Boolean
    abstract fun fromFavorites(): Boolean
    
    internal fun showErrorSnackBar() {
        errorSnackbar?.dismiss()
        errorSnackbar = null
        errorSnackbar = view?.buildSnackbar(getString(R.string.action_error_content))
        errorSnackbar?.view?.findViewById<TextView>(R.id.snackbar_text)?.setTextColor(Color.WHITE)
        errorSnackbar?.show()
    }
    
    internal fun onHeartClicked(heart: ImageView, item: Wallpaper, @ColorInt color: Int) =
        activity?.let {
            (it as? FavsDbManager)?.let {
                animateHeartClick(heart, item, color, !it.isInFavs(item))
            }
        } ?: showErrorSnackBar()
    
    private fun animateHeartClick(
        heart: ImageView,
        item: Wallpaper,
        @ColorInt color: Int,
        check: Boolean
                                 ) {
        context {
            it.runOnUiThread {
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
                                context?.createHeartIcon(check)?.applyColorFilter(color))
                            val nScale = ScaleAnimation(
                                0F, 1F, 0F, 1F, Animation.RELATIVE_TO_SELF, 0.5F,
                                Animation.RELATIVE_TO_SELF, 0.5F)
                            nScale.duration = ANIMATION_DURATION
                            nScale.interpolator = LinearInterpolator()
                            nScale.setAnimationListener(
                                object : SimpleAnimationListener() {
                                    override fun onEnd(animation: Animation) {
                                        super.onEnd(animation)
                                        (activity as? FavsDbManager)?.updateToFavs(item, check, it)
                                            ?: showErrorSnackBar()
                                    }
                                })
                            heart.startAnimation(nScale)
                        }
                    })
                heart.startAnimation(scale)
            }
        }
    }
}