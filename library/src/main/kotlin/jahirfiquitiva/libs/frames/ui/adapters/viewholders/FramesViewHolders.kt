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
import android.support.v4.view.ViewCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.goneIf
import ca.allanwang.kau.utils.visible
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.data.models.Collection
import jahirfiquitiva.libs.frames.data.models.Wallpaper
import jahirfiquitiva.libs.frames.helpers.extensions.animateColorTransition
import jahirfiquitiva.libs.frames.helpers.extensions.createHeartSelector
import jahirfiquitiva.libs.frames.helpers.extensions.loadFromUrls
import jahirfiquitiva.libs.frames.helpers.extensions.thumbnailColor
import jahirfiquitiva.libs.frames.ui.widgets.CheckableImageView
import jahirfiquitiva.libs.frames.ui.widgets.ParallaxImageView
import jahirfiquitiva.libs.kauextensions.extensions.bestSwatch
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.generatePalette
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.hasContent
import jahirfiquitiva.libs.kauextensions.ui.views.VerticalImageView

class CollectionHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    private var lastPosition = -1
    val img:ParallaxImageView = itemView.findViewById(R.id.collection_picture)
    val detailsBg:LinearLayout = itemView.findViewById(R.id.collection_details)
    val title:TextView = itemView.findViewById(R.id.collection_title)
    val amount:TextView = itemView.findViewById(R.id.collection_walls_number)
    var bitmap:Bitmap? = null
    
    fun setItem(collection:Collection, listener:(Collection, CollectionHolder) -> Unit) {
        with(itemView) {
            setBackgroundColor(context.thumbnailColor)
            detailsBg.setBackgroundColor(context.thumbnailColor)
            ViewCompat.setTransitionName(img, "img_transition_$adapterPosition")
            ViewCompat.setTransitionName(title, "title_transition_$adapterPosition")
            val url = collection.wallpapers.first().url
            val thumb = collection.wallpapers.first().thumbUrl
            loadImage(url, if (thumb.equals(url, true)) "" else thumb)
            title.text = collection.name
            amount.text = (collection.wallpapers.size).toString()
        }
        itemView.setOnClickListener { listener(collection, this) }
    }
    
    private fun loadImage(url:String, thumbUrl:String) {
        val listener = object:RequestListener<Bitmap> {
            override fun onResourceReady(resource:Bitmap?, model:Any?, target:Target<Bitmap>?,
                                         dataSource:DataSource?, isFirstResource:Boolean):Boolean {
                resource?.let {
                    img.invalidate()
                    if (adapterPosition > lastPosition) {
                        img.animateColorTransition()
                        lastPosition = adapterPosition
                    } else {
                        img.clearAnimation()
                        itemView.clearAnimation()
                    }
                    bitmap = it
                    if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                        val color = it.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                        detailsBg.background = null
                        detailsBg.setBackgroundColor(color)
                        title.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                        amount.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                    } else {
                        detailsBg.setBackgroundColor(Color.TRANSPARENT)
                        detailsBg.background =
                                itemView.context.getDrawable(R.drawable.gradient, null)
                        TextViewCompat.setTextAppearance(title, R.style.DetailsText)
                        TextViewCompat.setTextAppearance(amount, R.style.DetailsText_Small)
                    }
                }
                return false
            }
            
            override fun onLoadFailed(e:GlideException?, model:Any?, target:Target<Bitmap>?,
                                      isFirstResource:Boolean):Boolean = false
        }
        img.loadFromUrls(url, thumbUrl, listener)
    }
}

class WallpaperHolder(itemView:View, val showFavIcon:Boolean = true):
        RecyclerView.ViewHolder(itemView) {
    private var lastPosition = -1
    val img:VerticalImageView = itemView.findViewById(R.id.wallpaper_image)
    val detailsBg:LinearLayout = itemView.findViewById(R.id.wallpaper_details)
    val name:TextView = itemView.findViewById(R.id.wallpaper_name)
    val author:TextView = itemView.findViewById(R.id.wallpaper_author)
    val heartIcon:CheckableImageView = itemView.findViewById(R.id.heart_icon)
    var bitmap:Bitmap? = null
    
    fun setItem(wallpaper:Wallpaper, singleTap:(Wallpaper, WallpaperHolder) -> Unit,
                longPress:(Wallpaper, WallpaperHolder) -> Unit,
                heartListener:(CheckableImageView, Wallpaper) -> Unit,
                check:Boolean = false) {
        with(itemView) {
            ViewCompat.setTransitionName(img, "img_transition_$adapterPosition")
            ViewCompat.setTransitionName(name, "name_transition_$adapterPosition")
            ViewCompat.setTransitionName(author, "author_transition_$adapterPosition")
            setBackgroundColor(context.thumbnailColor)
            val url = wallpaper.url
            val thumb = wallpaper.thumbUrl
            loadImage(url, if (thumb.equals(url, true)) "" else thumb, check)
            name.text = wallpaper.name
            author.goneIf(!wallpaper.author.hasContent())
            author.text = wallpaper.author
            if (showFavIcon) {
                heartIcon.setOnClickListener { heartListener(heartIcon, wallpaper) }
                heartIcon.setImageDrawable(itemView.context.createHeartSelector())
                heartIcon.isChecked = check
                heartIcon.visible()
            } else {
                heartIcon.gone()
            }
        }
        itemView.setOnClickListener { singleTap(wallpaper, this) }
        itemView.setOnLongClickListener {
            longPress(wallpaper, this)
            true
        }
    }
    
    private fun loadImage(url:String, thumbUrl:String, check:Boolean) {
        val listener = object:RequestListener<Bitmap> {
            override fun onResourceReady(resource:Bitmap?, model:Any?, target:Target<Bitmap>?,
                                         dataSource:DataSource?, isFirstResource:Boolean):Boolean {
                resource?.let {
                    if (adapterPosition > lastPosition) {
                        img.animateColorTransition()
                        lastPosition = adapterPosition
                    } else {
                        img.clearAnimation()
                        itemView.clearAnimation()
                    }
                    bitmap = it
                    if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                        val color = it.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                        detailsBg.background = null
                        detailsBg.setBackgroundColor(color)
                        heartIcon.setImageDrawable(itemView.context.createHeartSelector(color))
                        name.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                        author.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                    } else {
                        detailsBg.background =
                                itemView.context.getDrawable(R.drawable.gradient, null)
                        heartIcon.setImageDrawable(itemView.context.createHeartSelector())
                        TextViewCompat.setTextAppearance(name, R.style.DetailsText)
                        TextViewCompat.setTextAppearance(author, R.style.DetailsText_Small)
                    }
                    heartIcon.isChecked = check
                }
                return false
            }
            
            override fun onLoadFailed(e:GlideException?, model:Any?, target:Target<Bitmap>?,
                                      isFirstResource:Boolean):Boolean = false
        }
        
        img.loadFromUrls(url, thumbUrl, listener)
    }
}