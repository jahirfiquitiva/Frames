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
package jahirfiquitiva.libs.frames.ui.adapters.viewholders

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.visible
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.util.ViewPreloadSizeProvider
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.animateColorTransition
import jahirfiquitiva.libs.frames.helpers.extensions.animateSmoothly
import jahirfiquitiva.libs.frames.helpers.extensions.bestSwatch
import jahirfiquitiva.libs.frames.helpers.extensions.clearChildrenAnimations
import jahirfiquitiva.libs.frames.helpers.extensions.createHeartIcon
import jahirfiquitiva.libs.frames.helpers.extensions.framesKonfigs
import jahirfiquitiva.libs.frames.helpers.extensions.loadWallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.releaseFromGlide
import jahirfiquitiva.libs.frames.helpers.extensions.thumbnailColor
import jahirfiquitiva.libs.frames.helpers.utils.GlideRequestListener
import jahirfiquitiva.libs.kauextensions.extensions.bind
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.dividerColor
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.extensions.withAlpha


const val DETAILS_OPACITY = 0.85F

abstract class FramesWallpaperHolder(itemView:View):GlideViewHolder(itemView) {
    internal var wallpaper:Wallpaper? = null
    abstract internal val img:ImageView
    abstract internal fun getListener():GlideRequestListener<Drawable>
    
    internal fun loadImage(manager:RequestManager, url:String, thumbUrl:String) {
        val hasFaded = wallpaper?.hasFaded ?: true
        img.loadWallpaper(manager, url, thumbUrl, hasFaded, getListener())
    }
    
    override fun doOnRecycle() {
        img.releaseFromGlide()
    }
    
    internal fun whenFaded(ifHasFaded:() -> Unit = {}, ifHasNotFaded:() -> Unit = {}) {
        val hasFaded = wallpaper?.hasFaded ?: true
        if (!hasFaded) ifHasNotFaded()
        else ifHasFaded()
    }
}

class CollectionHolder(itemView:View):FramesWallpaperHolder(itemView) {
    override val img:ImageView
        get() = itemView.findViewById(R.id.collection_picture)
    
    private val detailsBg:LinearLayout by itemView.bind(R.id.collection_details)
    private val title:TextView by itemView.bind(R.id.collection_title)
    private val amount:TextView by itemView.bind(R.id.collection_walls_number)
    
    fun setItem(manager:RequestManager, provider:ViewPreloadSizeProvider<Wallpaper>,
                collection:Collection, listener:(Collection) -> Unit) {
        if (this.wallpaper != collection.bestCover) this.wallpaper = collection.bestCover
        with(itemView) {
            whenFaded(ifHasNotFaded = {
                if (context.framesKonfigs.animationsEnabled) {
                    animateSmoothly(context.dividerColor, context.thumbnailColor,
                                    { setBackgroundColor(it) })
                } else {
                    setBackgroundColor(context.dividerColor)
                }
            })
            
            detailsBg.setBackgroundColor(context.dividerColor)
            val rightCover = collection.bestCover ?: collection.wallpapers.first()
            val url = rightCover.url
            val thumb = rightCover.thumbUrl
            title.text = collection.name
            title.setTextColor(Color.WHITE)
            amount.text = (collection.wallpapers.size).toString()
            amount.setTextColor(Color.WHITE)
            loadImage(manager, url, if (thumb.equals(url, true)) "" else thumb)
            setOnClickListener { listener(collection) }
            provider.setView(img)
        }
    }
    
    override fun getListener():GlideRequestListener<Drawable> {
        return object:GlideRequestListener<Drawable>() {
            override fun onLoadSucceed(resource:Drawable):Boolean {
                img.setImageDrawable(resource)
                
                whenFaded({ itemView.clearChildrenAnimations() }, {
                    if (itemView.context.framesKonfigs.animationsEnabled) {
                        img.animateColorTransition({ wallpaper?.hasFaded = true })
                    } else {
                        itemView.clearChildrenAnimations()
                    }
                })
                
                if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                    val color = resource.bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                    detailsBg.background = null
                    detailsBg.setBackgroundColor(color.withAlpha(DETAILS_OPACITY))
                    title.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                    amount.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                } else {
                    detailsBg.setBackgroundColor(Color.TRANSPARENT)
                    detailsBg.background =
                            itemView.context.getDrawable(R.drawable.gradient, null)
                }
                return true
            }
        }
    }
}

class WallpaperHolder(itemView:View, private val showFavIcon:Boolean):
        FramesWallpaperHolder(itemView) {
    
    private var shouldCheck = false
    private var heartColor = 0
    
    override val img:ImageView
        get() = itemView.findViewById(R.id.wallpaper_image)
    
    val name:TextView by itemView.bind(R.id.wallpaper_name)
    val author:TextView by itemView.bind(R.id.wallpaper_author)
    val heartIcon:ImageView by itemView.bind(R.id.heart_icon)
    private val detailsBg:LinearLayout by itemView.bind(R.id.wallpaper_details)
    
    fun setItem(manager:RequestManager, provider:ViewPreloadSizeProvider<Wallpaper>,
                wallpaper:Wallpaper, singleTap:(Wallpaper, WallpaperHolder) -> Unit,
                longClick:(Wallpaper) -> Unit,
                heartListener:(ImageView, Wallpaper, Int) -> Unit, check:Boolean) {
        if (this.wallpaper != wallpaper) this.wallpaper = wallpaper
        with(itemView) {
            detailsBg.setBackgroundColor(context.dividerColor)
            heartIcon.setImageDrawable(null)
            heartIcon.gone()
            if (shouldCheck != check) shouldCheck = check
            
            ViewCompat.setTransitionName(img, "img_transition_$adapterPosition")
            ViewCompat.setTransitionName(name, "name_transition_$adapterPosition")
            ViewCompat.setTransitionName(author, "author_transition_$adapterPosition")
            ViewCompat.setTransitionName(heartIcon, "fav_transition_$adapterPosition")
            
            whenFaded(ifHasNotFaded = {
                if (context.framesKonfigs.animationsEnabled) {
                    animateSmoothly(context.dividerColor, context.thumbnailColor,
                                    { setBackgroundColor(it) })
                } else {
                    setBackgroundColor(context.dividerColor)
                }
            })
            
            val url = wallpaper.url
            val thumb = wallpaper.thumbUrl
            
            name.text = wallpaper.name
            name.setTextColor(Color.WHITE)
            heartColor = Color.WHITE
            if (wallpaper.author.hasContent()) {
                author.text = wallpaper.author
                author.setTextColor(Color.WHITE)
            } else {
                author.gone()
            }
            
            if (showFavIcon) {
                heartIcon.setOnClickListener { heartListener(heartIcon, wallpaper, heartColor) }
            }
            
            loadImage(manager, url, if (thumb.equals(url, true)) "" else thumb)
            provider.setView(img)
        }
        itemView.setOnClickListener { singleTap(wallpaper, this) }
        itemView.setOnLongClickListener {
            longClick(wallpaper)
            true
        }
    }
    
    override fun getListener():GlideRequestListener<Drawable> {
        return object:GlideRequestListener<Drawable>() {
            override fun onLoadSucceed(resource:Drawable):Boolean {
                img.setImageDrawable(resource)
                whenFaded({ itemView.clearChildrenAnimations() },
                          {
                              if (itemView.context.framesKonfigs.animationsEnabled) {
                                  img.animateColorTransition { wallpaper?.hasFaded = true }
                              } else {
                                  itemView.clearChildrenAnimations()
                              }
                          })
                
                if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                    val color = resource.bestSwatch?.rgb ?: itemView.context.dividerColor
                    detailsBg.background = null
                    detailsBg.setBackgroundColor(color.withAlpha(DETAILS_OPACITY))
                    name.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                    author.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                    if (showFavIcon) {
                        heartColor = itemView.context.getActiveIconsColorFor(color)
                        heartIcon.setImageDrawable(
                                itemView.context.createHeartIcon(shouldCheck).tint(heartColor))
                    }
                } else {
                    detailsBg.background =
                            itemView.context.getDrawable(R.drawable.gradient, null)
                }
                
                if (showFavIcon) heartIcon.visible()
                return true
            }
        }
    }
}

abstract class GlideViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    abstract fun doOnRecycle()
}

abstract class GlideSectionedViewHolder(itemView:View):SectionedViewHolder(itemView) {
    abstract fun doOnRecycle()
}