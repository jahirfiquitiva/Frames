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

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.goneIf
import ca.allanwang.kau.utils.visible
import com.bumptech.glide.RequestManager
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.animateColorTransition
import jahirfiquitiva.libs.frames.helpers.extensions.clearChildrenAnimations
import jahirfiquitiva.libs.frames.helpers.extensions.createHeartIcon
import jahirfiquitiva.libs.frames.helpers.extensions.loadWallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.releaseFromGlide
import jahirfiquitiva.libs.frames.helpers.extensions.thumbnailColor
import jahirfiquitiva.libs.frames.helpers.utils.GlideTarget
import jahirfiquitiva.libs.frames.ui.widgets.ParallaxImageView
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.bestSwatch
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.generatePalette
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.ui.views.VerticalImageView

class CollectionHolder(itemView:View):GlideViewHolder(itemView) {
    
    private var hasFaded = false
    
    val img:ParallaxImageView = itemView.findViewById(R.id.collection_picture)
    val detailsBg:LinearLayout = itemView.findViewById(R.id.collection_details)
    val title:TextView = itemView.findViewById(R.id.collection_title)
    val amount:TextView = itemView.findViewById(R.id.collection_walls_number)
    
    fun setItem(manager:RequestManager, collection:Collection,
                listener:(Collection, CollectionHolder) -> Unit) {
        with(itemView) {
            setBackgroundColor(context.thumbnailColor)
            detailsBg.setBackgroundColor(context.thumbnailColor)
            ViewCompat.setTransitionName(img, "coll_transition_$adapterPosition")
            ViewCompat.setTransitionName(title, "title_transition_$adapterPosition")
            val rightCover = collection.bestCover ?: collection.wallpapers.first()
            val url = rightCover.url
            val thumb = rightCover.thumbUrl
            loadImage(manager, url, if (thumb.equals(url, true)) "" else thumb)
            title.text = collection.name
            amount.text = (collection.wallpapers.size).toString()
        }
        itemView.setOnClickListener { listener(collection, this) }
    }
    
    private fun loadImage(manager:RequestManager, url:String, thumbUrl:String) {
        val target = object:GlideTarget(img) {
            override fun onLoadSucceed(resource:Bitmap) {
                img.setImageBitmap(resource)
                if (!hasFaded) {
                    img.animateColorTransition({ hasFaded = true })
                } else {
                    itemView.clearChildrenAnimations()
                }
                if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                    val color = resource.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                    detailsBg.background = null
                    detailsBg.setBackgroundColor(color)
                    title.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                    amount.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                } else {
                    detailsBg.setBackgroundColor(Color.TRANSPARENT)
                    detailsBg.background =
                            itemView.context.getDrawable(R.drawable.gradient, null)
                }
            }
        }
        img.loadWallpaper(manager, url, thumbUrl, false, hasFaded, null, target)
    }
    
    override fun onRecycled() {
        img.releaseFromGlide()
    }
}

class WallpaperHolder(itemView:View, private val showFavIcon:Boolean):
        GlideViewHolder(itemView) {
    private var hasFaded = false
    
    val img:VerticalImageView = itemView.findViewById(R.id.wallpaper_image)
    val detailsBg:LinearLayout = itemView.findViewById(R.id.wallpaper_details)
    val name:TextView = itemView.findViewById(R.id.wallpaper_name)
    val author:TextView = itemView.findViewById(R.id.wallpaper_author)
    val heartIcon:ImageView = itemView.findViewById(R.id.heart_icon)
    var checkedDrawable:Drawable? = null
    var uncheckedDrawable:Drawable? = null
    
    fun setItem(manager:RequestManager, wallpaper:Wallpaper,
                singleTap:(Wallpaper, WallpaperHolder) -> Unit,
                longPress:(Wallpaper, WallpaperHolder) -> Unit,
                heartListener:(ImageView, Wallpaper) -> Unit, check:Boolean) {
        with(itemView) {
            heartIcon.gone()
            
            if (checkedDrawable == null && showFavIcon) {
                checkedDrawable = itemView.context.createHeartIcon(true)
            }
            if (uncheckedDrawable == null && showFavIcon) {
                uncheckedDrawable = itemView.context.createHeartIcon(false)
            }
            
            ViewCompat.setTransitionName(img, "img_transition_$adapterPosition")
            ViewCompat.setTransitionName(name, "name_transition_$adapterPosition")
            ViewCompat.setTransitionName(author, "author_transition_$adapterPosition")
            ViewCompat.setTransitionName(heartIcon, "fav_transition_$adapterPosition")
            setBackgroundColor(context.thumbnailColor)
            val url = wallpaper.url
            val thumb = wallpaper.thumbUrl
            loadImage(manager, url, if (thumb.equals(url, true)) "" else thumb, check)
            name.text = wallpaper.name
            author.goneIf(!wallpaper.author.hasContent())
            author.text = wallpaper.author
            if (showFavIcon) {
                heartIcon.setOnClickListener { heartListener(heartIcon, wallpaper) }
            }
        }
        itemView.setOnClickListener { singleTap(wallpaper, this) }
        itemView.setOnLongClickListener {
            longPress(wallpaper, this)
            true
        }
    }
    
    private fun loadImage(manager:RequestManager, url:String, thumbUrl:String, check:Boolean) {
        val target = object:GlideTarget(img) {
            override fun onLoadSucceed(resource:Bitmap) {
                img.setImageBitmap(resource)
                if (!hasFaded) {
                    img.animateColorTransition({ hasFaded = true })
                } else {
                    itemView.clearChildrenAnimations()
                }
                val rightDrawable = if (check) checkedDrawable else uncheckedDrawable
                if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                    val color = resource.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                    detailsBg.background = null
                    detailsBg.setBackgroundColor(color)
                    name.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                    author.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                    if (showFavIcon)
                        heartIcon.setImageDrawable(rightDrawable?.applyColorFilter(
                                itemView.context.getActiveIconsColorFor(color)))
                } else {
                    detailsBg.background =
                            itemView.context.getDrawable(R.drawable.gradient, null)
                    if (showFavIcon) heartIcon.setImageDrawable(rightDrawable)
                }
                if (showFavIcon) heartIcon.visible()
            }
        }
        img.loadWallpaper(manager, url, thumbUrl, true, hasFaded, null, target)
    }
    
    override fun onRecycled() {
        img.releaseFromGlide()
    }
}

abstract class GlideViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    abstract fun onRecycled()
}