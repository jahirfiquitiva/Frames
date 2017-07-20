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

package jahirfiquitiva.libs.frames.holders

import android.graphics.Bitmap
import android.support.v4.view.ViewCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.goneIf
import ca.allanwang.kau.utils.visible
import com.bumptech.glide.request.target.BitmapImageViewTarget
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.extensions.createHeartSelector
import jahirfiquitiva.libs.frames.extensions.loadFromUrls
import jahirfiquitiva.libs.frames.extensions.loadFromUrlsIntoTarget
import jahirfiquitiva.libs.frames.models.Collection
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.views.CheckableImageView
import jahirfiquitiva.libs.frames.views.ParallaxImageView
import jahirfiquitiva.libs.kauextensions.extensions.*
import jahirfiquitiva.libs.kauextensions.ui.views.LandscapeImageView
import jahirfiquitiva.libs.kauextensions.ui.views.VerticalImageView

class CollectionHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    val img:ParallaxImageView = itemView.findViewById(R.id.collection_picture)
    val detailsBg:LinearLayout = itemView.findViewById(R.id.collection_details)
    val title:TextView = itemView.findViewById(R.id.collection_title)
    val amount:TextView = itemView.findViewById(R.id.collection_walls_number)
    val progress:ProgressBar = itemView.findViewById(R.id.loading)
    var bitmap:Bitmap? = null

    fun setItem(collection:Collection, listener:(Collection, CollectionHolder) -> Unit) {
        with(itemView) {
            progress.gone()
            ViewCompat.setTransitionName(img, "img_transition_$adapterPosition")
            ViewCompat.setTransitionName(title, "title_transition_$adapterPosition")
            val url = collection.wallpapers.first().url
            val thumb = collection.wallpapers.first().thumbUrl
            if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                loadImage(url, if (thumb.equals(url, true)) "" else thumb)
            } else {
                TextViewCompat.setTextAppearance(title, R.style.DetailsText)
                TextViewCompat.setTextAppearance(amount, R.style.DetailsText_Small)
                img.loadFromUrls(url, if (thumb.equals(url, true)) "" else thumb)
            }
            img.parallaxEnabled = true
            title.text = collection.name
            amount.text = (collection.wallpapers.size).toString()
        }
        itemView.setOnClickListener { listener(collection, this) }
    }

    fun loadImage(url:String, thumbUrl:String) {
        val target = object:BitmapImageViewTarget(img) {
            override fun setResource(resource:Bitmap?) {
                resource?.let {
                    super.setResource(it)
                    bitmap = it
                    val color = it.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                    detailsBg.background = null
                    detailsBg.setBackgroundColor(color)
                    title.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                    amount.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                    img.invalidate()
                }
            }
        }
        img.loadFromUrlsIntoTarget(url, thumbUrl, target)
    }
}

class WallpaperHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    val img:VerticalImageView = itemView.findViewById(R.id.wallpaper_image)
    val detailsBg:LinearLayout = itemView.findViewById(R.id.wallpaper_details)
    val name:TextView = itemView.findViewById(R.id.wallpaper_name)
    val author:TextView = itemView.findViewById(R.id.wallpaper_author)
    val heartIcon:CheckableImageView = itemView.findViewById(R.id.heart_icon)
    val progress:ProgressBar = itemView.findViewById(R.id.loading)
    var bitmap:Bitmap? = null

    fun setItem(wallpaper:Wallpaper, listener:(Wallpaper, WallpaperHolder) -> Unit,
                heartListener:(CheckableImageView, Wallpaper) -> Unit,
                check:Boolean = false) {
        with(itemView) {
            ViewCompat.setTransitionName(img, "img_transition_$adapterPosition")
            ViewCompat.setTransitionName(name, "name_transition_$adapterPosition")
            ViewCompat.setTransitionName(author, "author_transition_$adapterPosition")
            val url = wallpaper.url
            val thumb = wallpaper.thumbUrl
            loadImage(url, if (thumb.equals(url, true)) "" else thumb, check)
            name.text = wallpaper.name
            author.goneIf(wallpaper.author.isEmpty() || wallpaper.author.isBlank())
            author.text = wallpaper.author
            heartIcon.setOnClickListener {
                heartListener(heartIcon, wallpaper)
            }
        }
        itemView.setOnClickListener { listener(wallpaper, this) }
    }

    fun loadImage(url:String, thumbUrl:String, check:Boolean) {
        val target = object:BitmapImageViewTarget(img) {
            override fun setResource(resource:Bitmap?) {
                resource?.let {
                    progress.gone()
                    super.setResource(it)
                    bitmap = it
                    if (itemView.context.getBoolean(R.bool.enable_colored_tiles)) {
                        val color = it.generatePalette().bestSwatch?.rgb ?: itemView.context.cardBackgroundColor
                        detailsBg.background = null
                        detailsBg.setBackgroundColor(color)
                        heartIcon.setImageDrawable(itemView.context.createHeartSelector(color))
                        name.setTextColor(itemView.context.getPrimaryTextColorFor(color))
                        author.setTextColor(itemView.context.getSecondaryTextColorFor(color))
                    } else {
                        heartIcon.setImageDrawable(itemView.context.createHeartSelector())
                        TextViewCompat.setTextAppearance(name, R.style.DetailsText)
                        TextViewCompat.setTextAppearance(author, R.style.DetailsText_Small)
                    }
                    heartIcon.visible()
                    heartIcon.isChecked = check
                }
            }
        }
        img.loadFromUrlsIntoTarget(url, thumbUrl, target)
    }
}